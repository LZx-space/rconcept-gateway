package com.rconcept.gateway.fileupload.application;

import com.rconcept.gateway.infrastructure.support.SftpClusterHelper;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author LZx
 * @since 2021/1/5
 */
@Service
public class FileServiceImpl implements FileService {

    private final SftpClusterHelper sftpClusterHelper;

    public FileServiceImpl(SftpClusterHelper sftpClusterHelper) {
        this.sftpClusterHelper = sftpClusterHelper;
    }

    @Override
    public Mono<String> uploadSingle(Mono<FilePart> filePartMono) {
        return filePartMono.map(filePart -> {
            Path tempFile;
            try {
                String date = DateTimeFormatter.ISO_DATE.format(LocalDate.now());
                tempFile = Files.createTempFile(date + "_", filePart.filename());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            AsynchronousFileChannel channel;
            try {
                channel = AsynchronousFileChannel.open(tempFile, StandardOpenOption.WRITE);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            DataBufferUtils.write(filePart.content(), channel, 0).subscribe();
            return tempFile;
        }).map(tempFile -> {
            // TODO check size、format etc.
            String filename = tempFile.toFile().getName();
            InputStream inputStream = null;
            try {
                inputStream = Files.newInputStream(tempFile);
                sftpClusterHelper.upload(inputStream, filename);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    Files.delete(tempFile);
                } catch (IOException ignored) {
                }
            }
            return filename;
        });
    }

}
