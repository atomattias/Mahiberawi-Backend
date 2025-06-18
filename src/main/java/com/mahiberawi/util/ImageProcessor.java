package com.mahiberawi.util;

import com.mahiberawi.config.FileUploadConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

@Component
public class ImageProcessor {
    private final FileUploadConfig config;

    public ImageProcessor(FileUploadConfig config) {
        this.config = config;
    }

    public byte[] processImage(MultipartFile file) throws IOException {
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new IOException("Invalid image file");
        }

        // Resize image if needed
        BufferedImage resizedImage = resizeImage(originalImage);

        // Convert to specified format and compress
        return compressImage(resizedImage);
    }

    public byte[] createThumbnail(MultipartFile file) throws IOException {
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new IOException("Invalid image file");
        }

        // Create thumbnail
        BufferedImage thumbnail = resizeImage(originalImage, 
            config.getImage().getThumbnailWidth(), 
            config.getImage().getThumbnailHeight());

        // Convert to specified format and compress
        return compressImage(thumbnail);
    }

    private BufferedImage resizeImage(BufferedImage originalImage) {
        return resizeImage(originalImage, 
            config.getImage().getMaxWidth(), 
            config.getImage().getMaxHeight());
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // Calculate new dimensions while maintaining aspect ratio
        double aspectRatio = (double) originalWidth / originalHeight;
        int newWidth = targetWidth;
        int newHeight = targetHeight;

        if (originalWidth > originalHeight) {
            newHeight = (int) (targetWidth / aspectRatio);
        } else {
            newWidth = (int) (targetHeight * aspectRatio);
        }

        // Create new image with the calculated dimensions
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, originalImage.getType());
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g.dispose();

        return resizedImage;
    }

    private byte[] compressImage(BufferedImage image) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        String format = config.getImage().getFormat().toLowerCase();

        if (format.equals("jpeg") || format.equals("jpg")) {
            // Use JPEG compression
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
            ImageWriter writer = writers.next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(config.getImage().getQuality() / 100f);

            ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream);
            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, null), param);
            writer.dispose();
            ios.close();
        } else {
            // Use default compression
            ImageIO.write(image, format, outputStream);
        }

        return outputStream.toByteArray();
    }
} 