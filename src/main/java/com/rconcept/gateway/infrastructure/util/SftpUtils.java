package com.rconcept.gateway.infrastructure.util;

import com.jcraft.jsch.*;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

/**
 * @author LZx
 * @since 2020/12/31
 */
public class SftpUtils {

    private SftpUtils() {
    }

    /**
     * 创建会话，<strong style="color:red;">不再使用后必须关闭</strong>
     *
     * @param remoteInfo 连接用户
     * @return 连接会话
     * @throws JSchException JSch异常
     */
    public static Session session(RemoteInfo remoteInfo) throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(remoteInfo.username, remoteInfo.host, remoteInfo.port);
        session.setPassword(remoteInfo.password);
        session.setConfig("StrictHostKeyChecking", "no");
        return session;
    }

    /**
     * 上传文件到远程服务器
     *
     * @param session                 会话
     * @param inputStream             待上传文件的流
     * @param remoteFile              上传文件绝对路径
     * @param connectionTimeoutMillis 连接超时时间
     */
    public static void uploadFile(Session session, InputStream inputStream, String remoteFile, int connectionTimeoutMillis) throws JSchException, SftpException {
        if (!session.isConnected()) {
            session.connect(connectionTimeoutMillis);
        }
        ChannelSftp channelSftp = channelSftp(session, connectionTimeoutMillis);
        channelSftp.setFilenameEncoding("UTF-8");
        try {
            channelSftp.put(inputStream, remoteFile);
        } finally {
            channelSftp.disconnect();
        }
    }

    /**
     * 上传文件到远程服务器
     *
     * @param session                 会话
     * @param targetFile              待上传文件
     * @param remoteDir               上传目的地文件夹
     * @param connectionTimeoutMillis 连接超时时间
     */
    public static void uploadFile(Session session, String targetFile, String remoteDir, int connectionTimeoutMillis) throws JSchException, SftpException {
        if (!session.isConnected()) {
            session.connect(connectionTimeoutMillis);
        }
        ChannelSftp channelSftp = channelSftp(session, connectionTimeoutMillis);
        channelSftp.setFilenameEncoding("UTF-8");
        try {
            channelSftp.put(targetFile, remoteDir);
        } finally {
            channelSftp.disconnect();
        }
    }

    /**
     * @param session                 会话
     * @param remoteDir               远程目标文件夹
     * @param connectionTimeoutMillis 连接超时时间
     * @return 所有LS出来实体的集合
     * @throws JSchException
     * @throws SftpException
     */
    @SuppressWarnings("unchecked")
    public static Vector<ChannelSftp.LsEntry> ls(Session session, final String remoteDir, int connectionTimeoutMillis) throws JSchException, SftpException {
        if (!session.isConnected()) {
            session.connect(connectionTimeoutMillis);
        }
        ChannelSftp channelSftp = channelSftp(session, connectionTimeoutMillis);
        channelSftp.setFilenameEncoding("UTF-8");
        try {
            return channelSftp.ls(remoteDir);
        } finally {
            channelSftp.disconnect();
        }
    }

    /**
     * 执行命令
     * <pre style="color:green;">
     * Available commands:           <strong style="color:red;">* means unimplemented command.</strong><br>
     * cd path                       Change remote directory to 'path'<br>
     * lcd path                      Change local directory to 'path'<br>
     * chgrp grp path                Change group of file 'path' to 'grp'<br>
     * chmod mode path               Change permissions of file 'path' to 'mode'<br>
     * chown own path                Change owner of file 'path' to 'own'<br>
     * df [path]                     Display statistics for current directory or filesystem containing 'path'<br>
     * get remote-path [local-path]  Download file<br>
     * get-resume remote-path [local-path]  Resume to download file.<br>
     * get-append remote-path [local-path]  Append remote file to local file<br>
     * hardlink oldpath newpath      Hardlink remote file<br>
     * *lls [ls-options [path]]      Display local directory listing<br>
     * ln oldpath newpath            Symlink remote file<br>
     * *lmkdir path                  Create local directory<br>
     * lpwd                          Print local working directory<br>
     * ls [path]                     Display remote directory listing<br>
     * *lumask umask                 Set local umask to 'umask'<br>
     * mkdir path                    Create remote directory<br>
     * put local-path [remote-path]  Upload file<br>
     * put-resume local-path [remote-path]  Resume to upload file<br>
     * put-append local-path [remote-path]  Append local file to remote file.<br>
     * pwd                           Display remote working directory<br>
     * stat path                     Display info about path<br>
     * exit                          Quit sftp<br>
     * quit                          Quit sftp<br>
     * rename oldpath newpath        Rename remote file<br>
     * rmdir path                    Remove remote directory<br>
     * rm path                       Delete remote file<br>
     * symlink oldpath newpath       Symlink remote file<br>
     * readlink path                 Check the target of a symbolic link<br>
     * realpath path                 Canonicalize the path<br>
     * rekey                         Key re-exchanging<br>
     * compression level             Packet compression will be enabled<br>
     * version                       Show SFTP version<br>
     * </pre>
     *
     * @param session                 会话
     * @param command                 命令
     * @param successOut              成功的输出流
     * @param errorOut                异常的输出流
     * @param connectionTimeoutMillis 连接超时时间
     * @return 0成功 1异常
     * @throws JSchException
     * @throws IOException
     * @throws InterruptedException
     */
    public static int execCommand(Session session, String command, OutputStream successOut, OutputStream errorOut, int connectionTimeoutMillis) throws JSchException, InterruptedException, IOException {
        if (!session.isConnected()) {
            session.connect(connectionTimeoutMillis);
        }
        ChannelExec channelExec = channelExec(session, command, errorOut, connectionTimeoutMillis);
        try (InputStream is = channelExec.getInputStream()) {
            byte[] buf = new byte[1024];
            while (true) {
                while (is.available() > 0) {
                    int len = is.read(buf);
                    if (len < 0) {
                        break;
                    } else {
                        successOut.write(buf, 0, len);
                    }
                }
                if (channelExec.isClosed()) {
                    if (is.available() > 0) {
                        continue;
                    }
                    return channelExec.getExitStatus();
                }
                Thread.sleep(500);
            }
        } finally {
            if (successOut != null) {
                successOut.flush();
                successOut.close();
            }
            channelExec.disconnect();
        }
    }

    /**
     * 创建SFTP通道
     *
     * @param session                 会话
     * @param connectionTimeoutMillis 连接超时时间
     * @return {@link ChannelSftp}
     * @throws JSchException 创建channel异常或者连接异常
     */
    private static ChannelSftp channelSftp(Session session, int connectionTimeoutMillis) throws JSchException {
        ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect(connectionTimeoutMillis);
        return channel;
    }

    /**
     * 创建执行命令的通道
     *
     * @param session                 会话
     * @param command                 命令
     * @param errorOutputStream       异常输出流
     * @param connectionTimeoutMillis 连接超时时间
     * @return {@link ChannelExec}
     * @throws JSchException 创建channel异常或者连接异常
     */
    private static ChannelExec channelExec(Session session, String command, OutputStream errorOutputStream, int connectionTimeoutMillis) throws JSchException {
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        channel.setInputStream(null);
        channel.setErrStream(errorOutputStream);
        channel.connect(connectionTimeoutMillis);
        return channel;
    }

    /**
     * 用户信息
     */
    @Getter
    @Setter
    public static class RemoteInfo {

        private String username;

        private String password;

        private String host;

        private int port;

        /**
         * Sftp或其子目录的绝对路径，总是以/开头和结束
         */
        private String sftpBaseDir;

    }

}
