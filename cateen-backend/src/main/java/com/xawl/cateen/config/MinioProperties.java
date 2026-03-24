package com.xawl.cateen.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    /**
     * Whether to enable MinIO for image uploads.
     */
    private boolean enabled = false;

    /**
     * MinIO API endpoint used by backend.
     * Example: http://127.0.0.1:9000
     */
    private String endpoint;

    /**
     * Public base URL used to build the returned object URL.
     * If empty, {@link #endpoint} will be used.
     */
    private String publicUrl;

    private String accessKey;

    private String secretKey;

    private Bucket bucket = new Bucket();

    @Data
    public static class Bucket {
        /**
         * Bucket for admin (management) uploads.
         */
        private String admin = "admin";

        /**
         * Bucket for mini program uploads.
         */
        private String small = "small";

        /**
         * Bucket for video uploads.
         */
        private String video = "video";
    }
}

