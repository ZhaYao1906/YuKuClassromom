package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;

import java.io.File;

/**
 * @description 媒资文件管理业务类
 */
public interface MediaFileService {

   /**
   * @description 媒资文件查询方法
   * @param pageParams 分页参数
   * @param queryMediaParamsDto 查询条件
   * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
   * @author Mr.M
   * @date 2022/9/10 8:57
   */
   public PageResult<MediaFiles> queryMediaFiels(Long companyId,PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

 /**
  * 上传图片的接口
  * @param companyId 机构的id 和上传课程一样
  * @param uploadFileParamsDto 存储文件的相关信息
  * @param localFilePath 本地文件路径
  * @return
  */
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto,String localFilePath,String objectName);
    public MediaFiles addMediaFilesToDb(Long companyId,String fileMd5,UploadFileParamsDto uploadFileParamsDto,String bucket,String objectName);

    /**
     * 检查文件是否存在
     * @param fileMd5 文件的md5
     * @return
     */
    public RestResponse<Boolean> checkFile(String fileMd5);

    /**
     * 文件存在的情况下
     * 检查分块是否存在
     * @param fileMd5 文件的MD5
     * @param chunkIndex 分块序号
     * @return
     */
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);
    /**
      * @description 上传分块
      * @param fileMd5  文件md5
      * @param chunk  分块序号
      * @param localChunkFilePath 件字节
      */
    public RestResponse uploadChunk(String fileMd5,int chunk,String localChunkFilePath);

   /**
    * @description 合并分块
    * @param companyId  机构id
    * @param fileMd5  文件md5
    * @param chunkTotal 分块总和
    * @param uploadFileParamsDto 文件信息
    */
   public RestResponse mergechunks(Long companyId,String fileMd5,int chunkTotal,UploadFileParamsDto uploadFileParamsDto);
    /**
     * 从minio下载文件
     * @param bucket 桶
     * @param objectName 对象名称
     * @return 下载后的文件
     */
    public File downloadFileFromMinIO(String bucket, String objectName);
    /**
     * 将文件上传到minio
     * @param localFilePath 文件本地路径
     * @param mimeType 媒体类型
     * @param bucket 桶
     * @param objectName 对象名
     * @return
     */
    public boolean addMediaFilesToMinIO(String localFilePath,String mimeType,String bucket, String objectName);

    public MediaFiles getFileById(String mediaId);
}
