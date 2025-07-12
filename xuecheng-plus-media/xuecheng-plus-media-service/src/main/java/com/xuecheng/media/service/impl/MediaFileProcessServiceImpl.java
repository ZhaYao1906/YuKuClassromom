package com.xuecheng.media.service.impl;

import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaFileProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class MediaFileProcessServiceImpl implements MediaFileProcessService {
    @Autowired
    MediaProcessMapper mediaProcessMapper;
    @Autowired
    MediaFilesMapper mediaFilesMapper;
    @Autowired
    MediaProcessHistoryMapper mediaProcessHistoryMapper;

    /**
     * 查询待处理任务
     * @param shardIndex 分片序号
     * @param shardTotal 分片总数
     * @param count 获取记录数
     * @return
     */
    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        List<MediaProcess> mediaProcesses = mediaProcessMapper.selectListByShardIndex(shardTotal, shardIndex, count);
        return mediaProcesses;
    }

    /**
     * 开启一个任务
     * @param id 任务id
     * @return
     */
    public boolean startTask(long id) {
        int result = mediaProcessMapper.startTask(id);
        return result<=0?false:true;
    }
    /**
     * @description 保存任务结果
     * 更新任务的结果，一般来说，处理的是media_process这张表（也可能会用到media_process_history）
     * @param taskId  任务id
     * @param status 任务状态
     * @param fileId  文件id
     * @param url url
     * @param errorMsg 错误信息
     */
    @Override
    public void saveProcessFinishStatus(Long taskId,String status,String fileId,String url,String errorMsg){
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);//要更新的任务
        if(mediaProcess == null) return;
        /** ------ 如果任务执行失败  ------ **/
        if(status.equals("3")){
            //更新MediaProcess的状态
            mediaProcess.setStatus("3");
            mediaProcess.setFailCount(mediaProcess.getFailCount()+1);//失败次数+1
            mediaProcess.setErrormsg(errorMsg);
            mediaProcessMapper.updateById(mediaProcess);
            return;
        }
        /** ------ 如果任务执行成功 ------ **/
        //step1、更新media_file表中的url
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        mediaFiles.setUrl(url);
        mediaFilesMapper.updateById(mediaFiles);
        //step2、更新MediaProcess的状态，将任务插入到media_process_history中
        mediaProcess.setStatus("2");
        mediaProcess.setFinishDate(LocalDateTime.now());
        mediaProcess.setUrl(url);
        mediaProcessMapper.updateById(mediaProcess);
        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess,mediaProcessHistory);
        mediaProcessHistoryMapper.insert(mediaProcessHistory);
        //step3、从MediaProcess中删除当前任务
        mediaProcessMapper.deleteById(taskId);
    }
}
