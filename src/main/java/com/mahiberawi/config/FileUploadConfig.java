package com.mahiberawi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadConfig {
    private String directory;
    private long maxSize;
    private Map<String, List<String>> allowedTypes;
    private ImageConfig image;

    public static class ImageConfig {
        private int maxWidth;
        private int maxHeight;
        private int thumbnailWidth;
        private int thumbnailHeight;
        private String format;
        private int quality;

        // Getters and setters
        public int getMaxWidth() { return maxWidth; }
        public void setMaxWidth(int maxWidth) { this.maxWidth = maxWidth; }
        public int getMaxHeight() { return maxHeight; }
        public void setMaxHeight(int maxHeight) { this.maxHeight = maxHeight; }
        public int getThumbnailWidth() { return thumbnailWidth; }
        public void setThumbnailWidth(int thumbnailWidth) { this.thumbnailWidth = thumbnailWidth; }
        public int getThumbnailHeight() { return thumbnailHeight; }
        public void setThumbnailHeight(int thumbnailHeight) { this.thumbnailHeight = thumbnailHeight; }
        public String getFormat() { return format; }
        public void setFormat(String format) { this.format = format; }
        public int getQuality() { return quality; }
        public void setQuality(int quality) { this.quality = quality; }
    }

    // Getters and setters
    public String getDirectory() { return directory; }
    public void setDirectory(String directory) { this.directory = directory; }
    public long getMaxSize() { return maxSize; }
    public void setMaxSize(long maxSize) { this.maxSize = maxSize; }
    public Map<String, List<String>> getAllowedTypes() { return allowedTypes; }
    public void setAllowedTypes(Map<String, List<String>> allowedTypes) { this.allowedTypes = allowedTypes; }
    public ImageConfig getImage() { return image; }
    public void setImage(ImageConfig image) { this.image = image; }
} 