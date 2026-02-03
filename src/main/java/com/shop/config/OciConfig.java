package com.shop.config; // 패키지 경로 확인!

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.FileInputStream;
import java.io.IOException;

@Configuration // 이 어노테이션이 있어야 스프링이 읽습니다.
public class OciConfig {

    @Value("${oci.tenant-id}") private String tenantId;
    @Value("${oci.user-id}") private String userId;
    @Value("${oci.fingerprint}") private String fingerprint;
    @Value("${oci.private-key-path}") private String privateKeyPath;
    @Value("${oci.region}") private String region;
//클라우드 환경의 오라클DB접속을 위해 필요
    @Bean // 이 어노테이션이 있어야 ObjectStorage를 주입할 수 있습니다.
    public ObjectStorage objectStorage() throws IOException {
        AuthenticationDetailsProvider provider = SimpleAuthenticationDetailsProvider.builder()
                .tenantId(tenantId)
                .userId(userId)
                .fingerprint(fingerprint)
                .privateKeySupplier(() -> {
                    try {
                        return new FileInputStream(privateKeyPath);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .build();

        return ObjectStorageClient.builder()
                .region(Region.fromRegionId(region))
                .build(provider);
    }
}
