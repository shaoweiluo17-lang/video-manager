package com.videomanager.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5 工具类
 */
public class Md5Util {
    
    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();
    
    /**
     * 计算字符串的 MD5
     */
    public static String encode(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(str.getBytes());
            return bytesToHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("没有找到MD5算法", e);
        }
    }
    
    /**
     * 计算文件的 MD5
     */
    public static String encodeFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int bytesRead;
            
            while ((bytesRead = fis.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
            
            return bytesToHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("没有找到MD5算法", e);
        }
    }
    
    /**
     * 计算文件的 MD5（带进度回调）
     */
    public static String encodeFile(File file, ProgressCallback callback) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalRead = 0;
            long fileSize = file.length();
            
            while ((bytesRead = fis.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
                totalRead += bytesRead;
                if (callback != null && fileSize > 0) {
                    callback.onProgress((int)(totalRead * 100 / fileSize));
                }
            }
            
            return bytesToHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("没有找到MD5算法", e);
        }
    }
    
    /**
     * byte 数组转十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_CHARS[v >>> 4];
            hexChars[i * 2 + 1] = HEX_CHARS[v & 0x0F];
        }
        return new String(hexChars);
    }
    
    /**
     * 进度回调接口
     */
    public interface ProgressCallback {
        void onProgress(int percent);
    }
}
