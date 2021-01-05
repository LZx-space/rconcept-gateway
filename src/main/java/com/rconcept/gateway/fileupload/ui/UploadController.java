package com.rconcept.gateway.fileupload.ui;

import com.rconcept.gateway.fileupload.application.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author LZx
 * @since 2020/12/30
 */
@Slf4j
@RestController
public class UploadController {

    private final FileService fileService;

    public UploadController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * 单文件上传
     *
     * @param filePartMono {@link Mono<FilePart>}
     * @return Mono<访问路径>
     */
    @PostMapping("/upload")
    public Mono<String> uploadSingle(@RequestPart("file") Mono<FilePart> filePartMono) {
        return fileService.uploadSingle(filePartMono);
    }

}
