package utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {
    
    private FileUtils() {

    }
    
    public static String readFile(Path path) throws IOException {
        byte[] bytes = Files.readAllBytes(path);
        return new String(bytes, StandardCharsets.UTF_8);
    }
    
    public static void writeFile(Path path, String content) throws IOException {
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
    }
    
    public static boolean hasExtension(Path path, String extension) {
        String fileName = path.getFileName().toString();
        return fileName.toLowerCase().endsWith("." + extension.toLowerCase());
    }
}