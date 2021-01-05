package com.rconcept.gateway.infrastructure.support;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.rconcept.gateway.infrastructure.util.SftpUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author LZx
 * @since 2020/12/31
 */
@Component
@EnableConfigurationProperties(SftpClusterProperties.class)
public class SftpClusterHelper {

    private final SftpClusterProperties clusterProperties;

    public SftpClusterHelper(SftpClusterProperties clusterProperties) {
        this.clusterProperties = clusterProperties;
    }

    /**
     * 上传文件
     *
     * @param inputStream      文件流
     * @param abstractFilePath 相对于各SFTP各自的基本目录的文件路径
     */
    public void upload(InputStream inputStream, String abstractFilePath) {
        clusterProperties.getRemotes().forEach(remoteInfo -> {
            Session session = null;
            try {
                session = SftpUtils.session(remoteInfo);
                String remoteAbsoluteFile = remoteInfo.getSftpBaseDir() + abstractFilePath;
                SftpUtils.uploadFile(session, inputStream, remoteAbsoluteFile, clusterProperties.getConnectionTimeoutMillis());
            } catch (JSchException | SftpException e) {
                throw new RuntimeException(e);
            } finally {
                if (session != null) {
                    session.disconnect();
                }
                try {
                    inputStream.reset();
                } catch (IOException ignored) {
                }
            }
        });
    }

}
