package com.shop.service;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value; // ⭐️ @Value를 위한 import
import com.shop.config.OciConfig;
@Service
@RequiredArgsConstructor
@Log
public class FileService {
    private final ObjectStorage objectStorage;//// OciConfig에서 생성한 Bean 주입

    @Value("${oci.namespace}")
    private String namespace;
    //로컬 하드디스크 저장 방식 주석처리
    /*
    public String uploadFile(String uploadPath, String originalFileName, byte[] fileData) throws Exception{
        UUID uuid = UUID.randomUUID();
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String savedFileName = uuid.toString() + extension;
        String fileUploadFullUrl = uploadPath + "/" + savedFileName;
        FileOutputStream fos = new FileOutputStream(fileUploadFullUrl);
        fos.write(fileData);
        fos.close();
        return savedFileName;
    }*/

    // 변경 방식: OCI Object Storage에 저장
    public String uploadFile(String bucketName, String originalFileName, byte[] fileData) throws Exception {
        UUID uuid = UUID.randomUUID();
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String savedFileName = uuid.toString() + extension;

        // 1. 전송할 파일 데이터를 입력 스트림으로 변환
        InputStream inputStream = new ByteArrayInputStream(fileData);

        // 2. OCI 서버에 전송 요청 생성
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .namespaceName(namespace) // @Value로 받은 네임스페이스
                .bucketName(bucketName)    // 매개변수로 받은 버킷명 (shop-bucket)
                .objectName(savedFileName)
                .putObjectBody(inputStream)
                .build();

        // 3. 실제로 클라우드에 전송 (API 호출)
        objectStorage.putObject(putObjectRequest);

        return savedFileName;
    }

    public void deleteFile(String filePath) throws Exception{
        File deleteFile = new File(filePath);
        if(deleteFile.exists()) {
            deleteFile.delete();
            log.info("파일을 삭제하였습니다.");
        } else {
            log.info("파일이 존재하지 않습니다.");
        }
    }

}