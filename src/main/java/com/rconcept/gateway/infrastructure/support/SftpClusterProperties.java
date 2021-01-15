package com.rconcept.gateway.infrastructure.support;

import com.rconcept.gateway.infrastructure.util.SftpUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LZx
 * @since 2020/12/31
 */
@RefreshScope
@ConfigurationProperties(prefix = SftpClusterProperties.SFTP_CLUSTER_PREFIX)
@Getter
@Setter
public class SftpClusterProperties implements InitializingBean {

    static final String SFTP_CLUSTER_PREFIX = "sftp.cluster";

    private static final String PATH_SEPARATOR = "/";

    /**
     * 连接超时时间，包括创建Session和Channel
     */
    private int connectionTimeoutMillis = 1000;

    /**
     * SFTP连接配置信息集合
     */
    private List<SftpUtils.Server> remotes = new ArrayList<>();

    @Override
    public void afterPropertiesSet() {
        if (remotes.size() == 0) {
            throw new IllegalArgumentException(SftpClusterProperties.SFTP_CLUSTER_PREFIX + ".remotes至少需要一个SFTP连接配置");
        }
        remotes.forEach(e -> {
            String sftpBaseDir = e.getSftpBaseDir();
            if (!StringUtils.hasLength(sftpBaseDir)) {
                throw new IllegalArgumentException("目的SFTP文件夹不能为空");
            }
            if (!sftpBaseDir.startsWith(PATH_SEPARATOR)) {
                throw new IllegalArgumentException("目的SFTP文件夹路径必须以/开始");
            }
            if (!sftpBaseDir.endsWith(PATH_SEPARATOR)) {
                throw new IllegalArgumentException("目的SFTP基础文件夹路径必须以/结束");
            }
        });
    }
}
