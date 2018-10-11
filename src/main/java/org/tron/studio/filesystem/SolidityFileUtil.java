package org.tron.studio.filesystem;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j(topic = "SolidityFileUtil")
public class SolidityFileUtil {

    private static final String SUFFIX = ".sol";
    private static final String SAMPLE_CONTRACT_FILE = "/template/Sample.sol";
    private static final String EMPTY_CONTRACT_FILE = "/template/Empty.sol";

    public static List<File> getFileNameList() {
        List<File> list = new ArrayList<>();
        try {
            Stream<Path> fileList = Files.list(Paths.get(Config.SOLIDITY_SOURCE_PATH).toAbsolutePath());
            fileList.forEach(item -> {
                if (item.toFile().getName().endsWith(SUFFIX)) {
                    list.add(item.toFile());
                }
            });
        } catch (IOException e) {
            logger.error("Failed to get file from {}", Config.SOLIDITY_SOURCE_PATH);
        }
        if (list.isEmpty()) {
            InputStream stream = SolcFileUtil.class.getResourceAsStream(SAMPLE_CONTRACT_FILE);
            File sampleFile = new File(Config.SOLIDITY_SOURCE_PATH, "Sample.sol");
            try {
                FileUtils.copyInputStreamToFile(stream, sampleFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            list.add(sampleFile);
        }
        return list;
    }

    public static String formatFileName(String fileName) {
        if (!fileName.endsWith(SUFFIX)) {
            fileName += SUFFIX;
        }
        return fileName;
    }

    public static void createNewFile(String fileName) {
        fileName = formatFileName(fileName);
        File newFile = new File(Config.SOLIDITY_SOURCE_PATH, fileName.trim());
        StringBuilder builder = new StringBuilder();
        InputStream stream = SolcFileUtil.class.getResourceAsStream(EMPTY_CONTRACT_FILE);
        try {
            FileUtils.copyInputStreamToFile(stream, newFile);
        } catch (IOException e) {
            logger.error("Failed create {} {}", newFile.getAbsolutePath(), e);
        }
    }

    public static File getExistFile(String fileName) {
        File file = new File(fileName.trim());
        if (!file.exists())
            return new File(Config.SOLIDITY_SOURCE_PATH, fileName.trim());
        return file;
    }

    public static String getSourceCode(String fileName) {
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

    public static String getSourcePath() {
        return Config.SOLIDITY_SOURCE_PATH;
    }
}
