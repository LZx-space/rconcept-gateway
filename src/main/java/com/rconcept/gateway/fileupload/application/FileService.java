package com.rconcept.gateway.fileupload.application;

import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

/**
 * @author LZx
 * @since 2021/1/5
 */
public interface FileService {

    /**
     * 上传单个文件
     *
     * @param filePart {@link FilePart}
     * @return 文件名
     */
    Mono<String> uploadSingle(Mono<FilePart> filePart);

}
