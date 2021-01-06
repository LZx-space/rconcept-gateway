package com.rconcept.gateway.infrastructure.support;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.rconcept.gateway.infrastructure.util.SftpUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Objects;

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
     * 上传文件，<strong style="color:red;">对于单个SFTP服务器来说，其最大会话数是有限制的，
     * 如果以后创建会话失败时注意，考虑到目标服务器是不变的，此时需要不断开session而池化他们</strong>
     *
     * @param srcFile              上传文件的本地绝对路径
     * @param abstractBasePathFile 相对于各SFTP各自的基本目录的文件路径
     */
    public void upload(String srcFile, String abstractBasePathFile) {
        Objects.requireNonNull(srcFile, "参数[srcFile]不能为空");
        Objects.requireNonNull(abstractBasePathFile, "参数[abstractBasePathFile]不能为空");
        clusterProperties.getRemotes().stream().parallel().forEach(remoteInfo -> {
            Session session = null;
            try {
                session = SftpUtils.session(remoteInfo);
                String remoteAbsoluteFilename = buildRemoteAbsoluteFilename(remoteInfo.getSftpBaseDir(), abstractBasePathFile);
                SftpUtils.uploadFile(session, srcFile, remoteAbsoluteFilename, clusterProperties.getConnectionTimeoutMillis());
            } catch (JSchException | SftpException e) {
                throw new RuntimeException(e);
            } finally {
                if (session != null) {
                    session.disconnect();
                }
            }
        });
    }

    /**
     * 获取SFTP上文件的绝对路径
     *
     * @param sftpBaseDir          SFTP基础目录
     * @param abstractBasePathFile 上传文件的相对路径，含文件名
     * @return SFTP服务器上文件的绝对路径
     */
    private String buildRemoteAbsoluteFilename(String sftpBaseDir, String abstractBasePathFile) {
        return sftpBaseDir +
                (abstractBasePathFile.startsWith("/") ? abstractBasePathFile.substring(1) : abstractBasePathFile);
    }


}
