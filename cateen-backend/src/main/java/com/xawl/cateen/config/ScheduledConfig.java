package com.xawl.cateen.config;

import com.xawl.cateen.service.storage.ImageUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 定时任务配置
 */
@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class ScheduledConfig {

    private final ImageUploadService imageUploadService;

    /**
     * 每小时清理一次过期的分片上传会话
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void cleanupExpiredUploadSessions() {
        log.info("开始清理过期的分片上传会话");
        try {
            imageUploadService.cleanupExpiredSessions();
        } catch (Exception e) {
            log.error("清理过期分片上传会话失败", e);
        }
    }
}
