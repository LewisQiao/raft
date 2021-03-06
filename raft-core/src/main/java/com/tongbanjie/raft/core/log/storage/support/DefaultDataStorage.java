package com.tongbanjie.raft.core.log.storage.support;

import com.tongbanjie.raft.core.constant.RaftConstant;
import com.tongbanjie.raft.core.exception.RaftException;
import com.tongbanjie.raft.core.log.storage.DataStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/***
 *  默认日志存储实现
 * @author banxia
 * @date 2017-11-14 17:17:52
 */
public class DefaultDataStorage implements DataStorage {


    private final static Logger log = LoggerFactory.getLogger(DefaultDataStorage.class);


    private String storePath = RaftConstant.dataStorePath;
    private String storeFile = RaftConstant.dataStoreFile;
    private FileOutputStream fileOutputStream;

    public DefaultDataStorage() {

        this(RaftConstant.dataStorePath, RaftConstant.dataStoreFile);
    }

    public DefaultDataStorage(String storePath, String storeFile) {

        this.storePath = storePath + File.separator + "log";
        this.storeFile = storeFile;

        this.createStorePath();
        this.createStoreFile();
        try {
            this.fileOutputStream = new FileOutputStream(this.storePath + "/" + this.storeFile, true);
        } catch (FileNotFoundException e) {
            log.error(String.format(">>>>>create log file:%s, fileOutPutStream error", this.storePath + "/" + this.storeFile), e);
        }
    }

    /**
     * 创建日志存储路径
     */
    private void createStorePath() {

        File path = new File(this.storePath);

        try {

            if (!path.exists()) {
                path.mkdirs();
            }
        } catch (Exception e) {
            log.error(String.format("can not create path:%s error", this.storePath), e);
            throw new RaftException(String.format("can not create path:%s error", this.storePath), e);
        }
    }


    /**
     * 创建日志存储文件
     *
     * @notice 如: .raft
     */
    private void createStoreFile() {

        File file = new File(this.storePath + "/" + this.storeFile);

        try {

            if (!file.exists()) {

                file.createNewFile();
            }
        } catch (Exception e) {
            log.error(String.format("can not create log file:%s error", this.storeFile), e);
            throw new RaftException(String.format("can not create log file:%s error", this.storeFile), e);
        }
    }

    /***
     * 从日志存储介质读取所有的日志数据
     * @return
     */
    public byte[] readAll() {

        ByteArrayOutputStream outputStream = null;
        FileInputStream fileInputStream = null;
        try {

            outputStream = new ByteArrayOutputStream();
            fileInputStream = new FileInputStream(this.storePath + "/" + this.storeFile);

            int len = 0;
            byte[] buffer = new byte[1024];
            while ((len = fileInputStream.read(buffer)) > 0) {

                outputStream.write(buffer, 0, len);
            }

            outputStream.flush();
            outputStream.close();
            fileInputStream.close();
            byte[] body = outputStream.toByteArray();

            return body;


        } catch (Exception e) {

            throw new RaftException(String.format("read all fail from log file :%s/%s", this.storePath, this.storeFile), e);

        } finally {

            try {


                if (outputStream != null) {
                    outputStream.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (Exception e1) {
                // ignore
            }
        }
    }

    /**
     * 将raft日志持久化
     *
     * @param data 日志数据
     * @return
     */
    public boolean writeToStore(byte[] data) {

        try {

            this.fileOutputStream.write(data);
            this.fileOutputStream.flush();
            return true;
        } catch (Exception e) {

            log.error("writeToStore fail", e);
            throw new RaftException(String.format("writeToStore fail"), e);
        }

    }
}
