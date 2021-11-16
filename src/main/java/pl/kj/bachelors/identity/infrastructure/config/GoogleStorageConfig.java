package pl.kj.bachelors.identity.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "google-storage")
public class GoogleStorageConfig {
    private String projectId;
    private String localCredentialsPath;
    private String bucketName;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getLocalCredentialsPath() {
        return localCredentialsPath;
    }

    public void setLocalCredentialsPath(String localCredentialsPath) {
        this.localCredentialsPath = localCredentialsPath;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }
}
