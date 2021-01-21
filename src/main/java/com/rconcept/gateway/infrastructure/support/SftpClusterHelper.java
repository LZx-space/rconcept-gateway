package com.rconcept.gateway.infrastructure.support;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.rconcept.gateway.infrastructure.util.SftpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author LZx
 * @since 2020/12/31
 */
@Slf4j
@Component
@EnableConfigurationProperties(SftpClusterProperties.class)
public class SftpClusterHelper {

    private static List<Session> sessions;

    private final SftpClusterProperties clusterProperties;

    public SftpClusterHelper(SftpClusterProperties clusterProperties) {
        this.clusterProperties = clusterProperties;
        sessions = clusterProperties.getServers().stream().map(server -> {
            try {
                return SftpUtils.session(server);
            } catch (JSchException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }

    /**
     * 上传文件，<strong style="color:red;">对于单个SFTP服务器来说，其最大会话数是有限制的，
     * 如果以后创建会话失败时注意，考虑到目标服务器是不变的，此时需要不断开session而池化他们</strong>
     *
     * @param srcFilePath          上传文件的本地绝对路径
     * @param abstractDirOnBaseDir 相对于各SFTP各自的基本目录的文件路径
     */
    public void upload(Path srcFilePath, String abstractDirOnBaseDir) {
        Objects.requireNonNull(srcFilePath, "参数[srcFilePath]不能为空");
        Objects.requireNonNull(abstractDirOnBaseDir, "参数[abstractDirOnBaseDir]不能为空");
        sessions.stream().parallel().forEach(session -> {
            try {
                log.info("准备上传");
                String fileAbsolutePath = srcFilePath.toAbsolutePath().toString();
                SftpUtils.uploadFile(session, fileAbsolutePath, abstractDirOnBaseDir, true, clusterProperties.getConnectionTimeoutMillis());
                log.info("上传完毕");
            } catch (JSchException | SftpException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
