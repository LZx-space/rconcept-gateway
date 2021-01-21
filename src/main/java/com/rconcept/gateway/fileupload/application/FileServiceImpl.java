package com.rconcept.gateway.fileupload.application;

import com.rconcept.gateway.infrastructure.support.SftpClusterHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
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
@Slf4j
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
            sftpClusterHelper.upload(tempFile, "test/t1/");
            return tempFile.getFileName().toString();
        });
    }

}
