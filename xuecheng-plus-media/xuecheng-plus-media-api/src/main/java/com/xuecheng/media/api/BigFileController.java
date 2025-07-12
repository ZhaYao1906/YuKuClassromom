package com.xuecheng.media.api;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Api(value = "大文件上传接口",tags = "大文件上传接口")
@RestController
public class BigFileController {

    @Autowired
    MediaFileService mediaFileService;

    /** ----- 最后的返回结果是通用的结果RestResponse ----- **/
    @ApiOperation(value = "文件上传前检查文件")
    @PostMapping("/upload/checkfile")
    public RestResponse<Boolean> checkfile(@RequestParam("fileMd5") String fileMd5) throws Exception {
        RestResponse<Boolean> booleanRestResponse = mediaFileService.checkFile(fileMd5);
        return booleanRestResponse;
    }
    @ApiOperation(value = "分块文件上传前的检测")
    @PostMapping("/upload/checkchunk")
    public RestResponse<Boolean> checkchunk(@RequestParam("fileMd5") String fileMd5, @RequestParam("chunk") int chunk) throws Exception {
        RestResponse<Boolean> booleanRestResponse = mediaFileService.checkChunk(fileMd5,chunk);
        return booleanRestResponse;
    }
    @ApiOperation(value = "上传分块文件")
    @PostMapping("/upload/uploadchunk")
    public RestResponse uploadchunk(@RequestParam("file") MultipartFile file, @RequestParam("fileMd5") String fileMd5, @RequestParam("chunk") int chunk) throws Exception {
        //创建一个临时文件
        File tempFile = File.createTempFile("minio", ".temp");
        file.transferTo(tempFile);
        //文件路径
        String localFilePath = tempFile.getAbsolutePath();
        RestResponse restResponse = mediaFileService.uploadChunk(fileMd5, chunk, localFilePath);
        return restResponse;
    }

    /**
     * 合并文件
     * @param fileMd5 通过Md5拿到分块文件的地址
     * @param fileName 文件名
     * @param chunkTotal 分块个数
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "合并文件")
    @PostMapping("/upload/mergechunks")
    public RestResponse mergechunks(@RequestParam("fileMd5") String fileMd5, @RequestParam("fileName") String fileName, @RequestParam("chunkTotal") int chunkTotal) throws Exception {
        Long companyId = 1232141425L;
        //文件信息对象
        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        uploadFileParamsDto.setFilename(fileName);
        uploadFileParamsDto.setTags("视频文件");
        uploadFileParamsDto.setFileType("001002");
        RestResponse restResponse = mediaFileService.mergechunks(companyId, fileMd5, chunkTotal, uploadFileParamsDto);
        return restResponse;
    }





}
