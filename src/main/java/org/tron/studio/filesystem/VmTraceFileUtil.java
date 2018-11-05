package org.tron.studio.filesystem;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j(topic = "VmTraceFileUtil")
public class VmTraceFileUtil {

    private static final String SUFFIX = ".json";

    public static List<File> getFileNameList() {
        List<File> list = new ArrayList<>();
        try {
            Stream<Path> fileList = Files.list(Paths.get(Config.TRACE_PATH).toAbsolutePath());
            fileList.forEach(item -> {
                if (item.toFile().getName().endsWith(SUFFIX)) {
                    list.add(item.toFile());
                }
            });
        } catch (IOException e) {
            logger.error("Failed to get file from {}", Config.TRACE_PATH);
        }
        return list;
    }

    public static File getExistFile(String fileName) {
        return new File(Config.TRACE_PATH, fileName.trim());
    }

    public static String getTraceContent(String fileName) {
        File existFile = getExistFile(fileName);
        StringBuilder builder = new StringBuilder();
        try {
            Files.lines(Paths.get(existFile.toURI())).forEach(line -> {
                builder.append(line).append(System.getProperty("line.separator"));
            });
        } catch (IOException e) {
            logger.error("Failed get {}", existFile.getAbsolutePath());
        }
        return builder.toString();
    }

    public static String getTracePath() {
        return Config.TRACE_PATH;
    }
}
