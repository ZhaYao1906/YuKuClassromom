package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 ** 视频任务处理类
 */
@Slf4j
@Component
public class VideoTask {
    @Autowired
    MediaFileProcessService mediaFileProcessService;
    @Autowired
    MediaFileService mediaFileService;
    @Value("${videoprocess.ffmpegpath}")
    private String ffmpegpath;
    /**
     * 分片广播形式---视频处理任务
     */
    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();//执行器的序号，从0开始
        int shardTotal = XxlJobHelper.getShardTotal();//执行器总数
        //Step1、查询待处理任务
        int processors = Runtime.getRuntime().availableProcessors();//确定CPU的核心数
        //查询待处理的任务
        List<MediaProcess> mediaProcessList = mediaFileProcessService.getMediaProcessList(shardIndex,shardTotal,processors);
        int size = mediaProcessList.size();
        log.debug("取到视频处理任务数:"+size);
        if(size<=0){
            return;
        }
        //创建线程池
        ExecutorService executorService = Executors.newFixedThreadPool(size);
        //使用的计数器
        CountDownLatch countDownLatch = new CountDownLatch(size);
        //遍历列表，列表中都是任务
        mediaProcessList.forEach(mediaProcess -> {
            //将任务加入线程池
            executorService.execute(()->{
                try {
                    //Step2、开启任务(通过数据库的乐观锁实现)，也就是争强任务
                    Long taskId = mediaProcess.getId();//拿到任务id
                    String fileId = mediaProcess.getFileId();
                    boolean b = mediaFileProcessService.startTask(taskId);
                    if (!b) {
                        log.debug("抢占任务失败，任务id:{}", taskId);
                        return;
                    }
                    //Step3、执行视频转码
                    //3.1 下载minio视频到本地
                    String bucket = mediaProcess.getBucket();
                    String objectName = mediaProcess.getFilePath();
                    File file = mediaFileService.downloadFileFromMinIO(bucket, objectName);
                    if (file == null) {
                        log.debug("下载视频出错，任务id：{},bucket:{},objectName:{}", taskId, bucket, objectName);
                        //保存任务处理失败的结果
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "下载视频到本地失败");
                        return;
                    }
                    //源avi视频的路径
                    String video_path = file.getAbsolutePath();
                    //转换后mp4文件的名称
                    String mp4_name = fileId + ".mp4";
                    //转换后mp4文件的路径
                    //先创建一个临时文件，作为转换后的文件
                    File mp4File = null;
                    try {
                        mp4File = File.createTempFile("minio", ".mp4");
                    } catch (IOException e) {
                        log.debug("创建临时文件异常,{}", e.getMessage());
                        //保存任务处理失败的结果
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "创建临时文件异常");
                        return;
                    }
                    String mp4_path = mp4File.getAbsolutePath();
                    //创建工具类对象
                    Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegpath, video_path, mp4_name, mp4_path);
                    //开始视频转换，成功将返回success,失败返回失败原因
                    String result = videoUtil.generateMp4();
                    if (!result.equals("success")) {
                        log.debug("视频转码失败,原因:{},bucket:{},objectName:{},", result, bucket, objectName);
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, result);
                        return;
                    }
                    //Step4、上传到Minio
                    boolean b1 = mediaFileService.addMediaFilesToMinIO(mp4File.getAbsolutePath(), "video/mp4", bucket, objectName);
                    if (!b1) {
                        log.debug("上传mp4到minio失败,taskid:{}", taskId);
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "上传mp4到minio失败");
                        return;
                    }
                    //Step5、保存任务的处理结果
                    String url = getFilePath(fileId, ".mp4");
                    mediaFileProcessService.saveProcessFinishStatus(taskId, "2", fileId, url, "创建临时文件异常");
                }finally {
                    //计算器减去1
                    countDownLatch.countDown();
                }
            });
        });

        //阻塞,指定最大限制的等待时间，阻塞最多等待一定的时间后就解除阻塞
        countDownLatch.await(30, TimeUnit.MINUTES);
    }

    private String getFilePath(String fileMd5,String fileExt){
        return fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
    }

}
