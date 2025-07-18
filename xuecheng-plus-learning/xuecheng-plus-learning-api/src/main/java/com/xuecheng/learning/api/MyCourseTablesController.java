package com.xuecheng.learning.api;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.service.MyCourseTablesService;
import com.xuecheng.learning.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Mr.M
 * @version 1.0
 * @description 我的课程表接口
 * @date 2022/10/25 9:40
 */

@Api(value = "我的课程表接口", tags = "我的课程表接口")
@Slf4j
@RestController
public class MyCourseTablesController {
    @Autowired
    MyCourseTablesService myCourseTablesService;


    @ApiOperation("添加选课")
    @PostMapping("/choosecourse/{courseId}")
    public XcChooseCourseDto addChooseCourse(@PathVariable("courseId") Long courseId) {
        //添加选课，最后要存的除了课程id，还有用户id
        SecurityUtil.XcUser user = SecurityUtil.getUser();//当前登录的用户
        if(user == null){
            XueChengPlusException.cast("用户未登录");
        }
        String userId = user.getId();//用户id
        //添加选课
        XcChooseCourseDto xcChooseCourseDto = myCourseTablesService.addChooseCourse(userId,courseId);

        return xcChooseCourseDto;
    }

    @ApiOperation("查询学习资格")
    @PostMapping("/choosecourse/learnstatus/{courseId}")
    public XcCourseTablesDto getLearnstatus(@PathVariable("courseId") Long courseId) {
        SecurityUtil.XcUser user = SecurityUtil.getUser();//当前登录的用户
        if(user == null){
            XueChengPlusException.cast("用户未登录");
        }
        String userId = user.getId();//用户id
        XcCourseTablesDto learningStatus = myCourseTablesService.getLearningStatus(userId,courseId);
        return learningStatus;

    }

    @ApiOperation("我的课程表")
    @GetMapping("/mycoursetable")
    public PageResult<XcCourseTables> mycoursetable(MyCourseTableParams params) {
        SecurityUtil.XcUser user = SecurityUtil.getUser();//当前登录的用户
        if(user == null){
            XueChengPlusException.cast("用户未登录");
        }
        String userId = user.getId();//用户id
        params.setUserId(userId);

        PageResult<XcCourseTables> mycoursetables = myCourseTablesService.mycourestables(params);
        return mycoursetables;
    }

}
