package com.xuecheng.media;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 测试大文件上传的一些方法
 */
public class BigFileTest {
    /**
     * 分块
     */
    @Test
    public void testChunk() throws IOException {
        //源文件
        File sourceFile = new File("C:\\Users\\34111\\Videos\\test.mp4");
        //分块文件存储路径
        String chunkFilePath = "C:\\Users\\34111\\Videos\\chunk\\";
        //分块文件大小 0.5M
        int chunkSize = 1024*512;
        //分块文件个数
        int chunkNum = (int)Math.ceil(sourceFile.length()*1.0/chunkSize);
        //使用流从源文件中读取数据，向分块文件中写数据
         RandomAccessFile raf_r = new RandomAccessFile(sourceFile,"r");
         //缓冲区
        byte[] bytes = new byte[1024];
        for (int i = 0; i < chunkNum; i++) {
            //分段存储，1号文件存1，0号文件存0，保存的目录就是分块文件的存储路径的目录
            File chunkFile = new File(chunkFilePath + i);
            //分块文件的写入流
            RandomAccessFile raf_rw = new RandomAccessFile(chunkFile,"rw");
            int len = -1;
            while((len=raf_r.read(bytes)) != -1){
                raf_rw.write(bytes,0,len);
                if(chunkFile.length() >= chunkSize) break;
            }
            raf_rw.close(); //使用完后要关闭
        }
        raf_r.close();


    }

    /**
     * 将分块后的文件合并
     */
    @Test
    public void testMerge(){
        //源文件
        File sourceFile = new File("C:\\Users\\34111\\Videos\\test.mp4");
        //分块文件存储路径
        String chunkFilePath = "C:\\Users\\34111\\Videos\\chunk\\";
        //合并后的文件
        File mergeFile = new File("");
    }

}
