package com.nvx.tools.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.crypto.hash.Md5Hash;

import com.google.common.io.Files;

public class FileUtil {
    
    public static final String IMG_EXT_PNG = ".png";
    public static final String IMG_EXT_JPG = ".jpg";
    public static final String IMG_EXT_GIF = ".gif";
    
    private static int hashIterations = 1;

    public static boolean isImgExt(String file) {
        if (StringUtils.isEmpty(file) || file.length() < 5) {
            return false;
        }
        
        if (file.endsWith(IMG_EXT_JPG) || file.endsWith(IMG_EXT_PNG) || file.endsWith(IMG_EXT_GIF)) {
            return true;
        }
        
        return false;
    }
    
    public static boolean checkImgDimensions(File imgFile, int height, int width) throws IOException {
        final BufferedImage img = ImageIO.read(imgFile);
        if (img.getHeight() > height || img.getWidth() > width) {
            return false;
        }
        return true;
    }
    
    public static String hashFile(File file) throws IOException {
        return hashFile(Files.toByteArray(file), "");
    }
    
    public static String hashFile(File file, String salt) throws IOException {
        return hashFile(Files.toByteArray(file), salt);
    }
    
    public static String hashFile(byte[] file) throws IOException {
        return hashFile(file, "");
    }
    
    public static String hashFile(byte[] file, String salt) throws IOException {
        Md5Hash hasher = StringUtils.isEmpty(salt) ? new Md5Hash(file) : new Md5Hash(file, salt);
        hasher.setIterations(hashIterations);
        return hasher.toString();
    }
    
    public static void copyFile(File src, String basePath, String filename) throws IOException {
        final String bp = basePath.endsWith(File.separator) ? basePath : basePath + File.separator;
        
        final File dir = new File(bp);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("Unable to create directories needed for storing the file.");
            }
        }
        Files.copy(src, new File(bp + filename));
    }
}
