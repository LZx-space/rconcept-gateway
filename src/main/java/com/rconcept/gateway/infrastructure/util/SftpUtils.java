package com.rconcept.gateway.infrastructure.util;

import com.jcraft.jsch.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * @author LZx
 * @since 2020/12/31
 */
@Slf4j
public class SftpUtils {

    private SftpUtils() {
    }

    /**
     * 创建会话
     * <ul style="color:red;">
     *     <li>一个目标服务器一个会话即可</li>
     *     <li>一个目标服务器的多次操作，多个channel即可</li>
     *     <li>不再使用后必须关闭，但通常来讲非一次性行为可以不关闭等待下次使用</li>
     * </ul>
     *
     * @param server 连接服务器的信息
     * @return 连接会话
     * @throws JSchException JSch异常
     */
    public static Session session(Server server) throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(server.username, server.host, server.port);
        session.setPassword(server.password);
        session.setConfig("StrictHostKeyChecking", "no");
        return session;
    }

    /**
     * 上传文件到远程服务器
     *
     * @param session                 会话
     * @param inputStream             待上传文件的流
     * @param remoteAbstractFile      相对SFTP根目录的文件路径
     * @param connectionTimeoutMillis 连接超时时间
     */
    public static void uploadFile(Session session, InputStream inputStream, String remoteAbstractFile, int connectionTimeoutMillis) throws JSchException, SftpException {
        ChannelSftp channelSftp = channelSftp(session, connectionTimeoutMillis);
        channelSftp.setFilenameEncoding("UTF-8");
        try {
            channelSftp.put(inputStream, remoteAbstractFile);
        } finally {
            channelSftp.disconnect();
        }
    }

    /**
     * 上传文件到远程服务器<br>
     * <strong style="color:red;">推荐一个目标服务器使用一个会话</strong>
     *
     * @param session       会话
     * @param targetFile    待上传文件
     * @param remoteDir     SFTP的文件夹
     * @param timeoutMillis 连接超时时间
     */
    public static void uploadFile(Session session, String targetFile, String remoteDir, boolean mkdir, int timeoutMillis) throws JSchException, SftpException {
        ChannelSftp channelSftp = channelSftp(session, timeoutMillis);
        channelSftp.setFilenameEncoding("UTF-8");
        try {
            long start = System.currentTimeMillis();
            if (mkdir) {
                cdOrMkdirThenCd(channelSftp, remoteDir);
                channelSftp.put(targetFile, "./");
            } else {
                channelSftp.put(targetFile, remoteDir);
            }
            log.info("--上传文件-\t{}", System.currentTimeMillis() - start);
        } finally {
            channelSftp.disconnect();
        }
    }

    /**
     * @param session                 会话
     * @param remoteDir               远程目标文件夹
     * @param connectionTimeoutMillis 连接超时时间
     * @return 所有LS出来实体的集合
     * @throws JSchException JSCH的异常
     * @throws SftpException SFTP来源的异常
     */
    @SuppressWarnings("unchecked")
    public static Vector<ChannelSftp.LsEntry> ls(Session session, final String remoteDir, int connectionTimeoutMillis) throws JSchException, SftpException {
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
     * @throws JSchException        JSCH的异常
     * @throws IOException          读取命令执行结果异常
     * @throws InterruptedException 休眠读取命令执行结果的线程时被打断
     */
    public static int execCommand(Session session, String command, OutputStream successOut, OutputStream errorOut, int connectionTimeoutMillis) throws JSchException, InterruptedException, IOException {
        ChannelExec channelExec = channelExec(session, command, errorOut, connectionTimeoutMillis);
        try (InputStream is = channelExec.getInputStream()) {
            byte[] buf = new byte[512];
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
                Thread.sleep(100);
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
     * @param session       会话
     * @param timeoutMillis 连接超时时间
     * @return {@link ChannelSftp}
     * @throws JSchException 创建channel异常或者连接异常
     */
    public static ChannelSftp channelSftp(Session session, int timeoutMillis) throws JSchException {
        synchronized (session.getHost()) {
            if (!session.isConnected()) {
                session.connect(timeoutMillis);
            }
        }
        ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect(timeoutMillis);
        return channel;
    }

    /**
     * 创建执行命令的通道
     *
     * @param session           会话
     * @param command           命令
     * @param errorOutputStream 异常输出流
     * @param timeoutMillis     连接超时时间
     * @return {@link ChannelExec}
     * @throws JSchException 创建channel异常或者连接异常
     */
    public static ChannelExec channelExec(Session session, String command, OutputStream errorOutputStream, int timeoutMillis) throws JSchException {
        synchronized (session.getHost()) {
            if (!session.isConnected()) {
                session.connect(timeoutMillis);
            }
        }
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        channel.setInputStream(null);
        channel.setErrStream(errorOutputStream);
        channel.connect(timeoutMillis);
        return channel;
    }

    /**
     * cd目录，如果目录不存在则mkdir再cd，因此cd和mkdir块将同步，所以推荐直接上传到预先创建的文件夹
     * 并使用{@link ChannelSftp#put(String, String)}这类直接指定多级路径目录的方法
     *
     * @param channelSftp channel
     * @param remoteDir   远端文件夹路径
     * @throws SftpException cd/mkdir异常
     */
    private static void cdOrMkdirThenCd(ChannelSftp channelSftp, String remoteDir) throws SftpException {
        remoteDir = remoteDir.replace("\\", "/");
        boolean absoluteDir = remoteDir.startsWith("/");
        String tmpDir = absoluteDir ? remoteDir.substring(1) : remoteDir;
        String[] dirs = tmpDir.split("/");
        for (int i = 0, len = dirs.length; i < len; i++) {
            String dir = i == 0 && absoluteDir ? "/" + dirs[i] : dirs[i];
            // 避免不同线程进入不存在的目录时，另一个线程正在创建该目录
            synchronized (SftpUtils.class) {
                try {
                    channelSftp.cd(dir);
                } catch (SftpException e) {
                    if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                        channelSftp.mkdir(dir);
                        channelSftp.cd(dir);
                        continue;
                    }
                    throw e;
                }
            }
        }
    }

    /**
     * 用户信息
     */
    @Getter
    @Setter
    public static class Server {

        private String username;

        private String password;

        private String host;

        private int port;

    }

    public static void main(String[] args) throws JSchException {
        Server server = new Server();
        server.username = "lzx";
        server.password = "lzx";
        server.host = "127.0.0.1";
        server.port = 22;


        Session session = SftpUtils.session(server);
        AtomicInteger count = new AtomicInteger();
        try {
            IntStream.range(1, 5).parallel().forEach(value -> {
                synchronized (Integer.valueOf(1)) {
                    System.out.println("--s------");
                    System.out.println("-----\t" + value);
                    System.out.println("--e------\n");
                }
//                try {
//                    SftpUtils.uploadFile(session, "C:\\Users\\LZx\\Desktop\\PoolException.java", "test/t1/t2", true, 3000);
//                    count.getAndIncrement();
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
            });
        } finally {
            System.out.println("---count-" + count.get());
            session.disconnect();
        }
    }

}