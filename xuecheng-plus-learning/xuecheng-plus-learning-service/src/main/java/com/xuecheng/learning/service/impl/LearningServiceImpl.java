package com.xuecheng.learning.service.impl;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.feignclient.MediaServiceClient;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.service.LearningService;
import com.xuecheng.learning.service.MyCourseTablesService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 在线学习接口
 */
@Slf4j
@Service
public class LearningServiceImpl implements LearningService {

    @Autowired
    MyCourseTablesService myCourseTablesService;

    @Autowired
    ContentServiceClient contentServiceClient;

    @Autowired
    MediaServiceClient mediaServiceClient;

    @Override
    public RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId) {
        //查询课程信息
        CoursePublish coursePublish = contentServiceClient.getCoursepublish(courseId);
        //判断如果为null，则不再继续
        if(coursePublish == null){
            return RestResponse.validfail("课程不存在！");
        }
        //根据课程计划id（teachplanId）去查询课程计划信息，如果is_preview的值为1表示支持试学

        if(StringUtils.isNotEmpty(userId)){ //用户已登录
            /**
             *学习资格状态 {"code":"702001","desc":"正常学习"},
             *{"code":"702002","desc":"没有选课或选课后没有支付"},
             *{"code":"702003","desc":"已过期需要申请续期或重新支付"}
             */
            XcCourseTablesDto learningStatus = myCourseTablesService.getLearningStatus(userId, courseId);//通过我的课程表方法，获取其学习资格
            String learnStatus = learningStatus.getLearnStatus();
            if("702002".equals(learnStatus)){
                return RestResponse.validfail("无法学习，因为没有选课或选课后没有支付");
            }else if("702003".equals(learnStatus)){
                return RestResponse.validfail("已过期，需要申请续期或重新支付");
            }else{
                //有资格学习，要返回视频的播放地址,远程调用媒资获取视频播放地址
                RestResponse<String> playUrlByMedia = mediaServiceClient.getPlayUrlByMediaId(mediaId);
                return playUrlByMedia;
            }
        }

        //用户未登录，看看是不是免费课程，如果是免费课程的话，直接学习
        String charge = coursePublish.getCharge();
        if("201000".equals(charge)){
            //有资格学习，要返回视频的播放地址,远程调用媒资获取视频播放地址
            RestResponse<String> playUrlByMedia = mediaServiceClient.getPlayUrlByMediaId(mediaId);
            return playUrlByMedia;
        }

        return RestResponse.validfail("该课程没有选课");
    }
}
