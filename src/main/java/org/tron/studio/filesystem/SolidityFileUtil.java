package org.tron.studio.filesystem;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j(topic = "SolidityFileUtil")
public class SolidityFileUtil {


    private static final String sampleContractFile = "/template/Sample.sol";
    private static final String emptyContractFile = "/template/Empty.sol";

    static {
        File dir = new File(Config.SOLIDITY_SOURCE_PATH);
        dir.mkdirs();
    }

    public static List<File> getFileNameList() {
        List<File> list = new ArrayList<>();
        try {
            Stream<Path> fileList = Files.list(Paths.get(Config.SOLIDITY_SOURCE_PATH).toAbsolutePath());
            fileList.forEach(item -> {
                if (item.toFile().getName().endsWith(".sol")) {
                    list.add(item.toFile());
                }
            });
        } catch (IOException e) {
            logger.error("Failed to get file from {}", Config.SOLIDITY_SOURCE_PATH);
            return list;
        }
        if (list.isEmpty()) {
            File sampleFile = new File(Config.SOLIDITY_SOURCE_PATH, "Sample.sol");
            StringBuilder builder = new StringBuilder();
            try {
                Files.lines(Paths.get(SolidityFileUtil.class.getResource(sampleContractFile).getPath())).forEach(line -> {
                    builder.append(line).append(System.getProperty("line.separator"));
                });
                Files.write(Paths.get(sampleFile.toURI()), builder.toString().getBytes(), StandardOpenOption.CREATE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public static void createNewFile(String fileName) {
        File newFile = new File(Config.SOLIDITY_SOURCE_PATH, fileName.trim());
        StringBuilder builder = new StringBuilder();
        try {
            Files.lines(Paths.get(SolidityFileUtil.class.getResource(emptyContractFile).getPath())).forEach(line -> {
                builder.append(line).append(System.getProperty("line.separator"));
            });
            Files.write(Paths.get(newFile.toURI()), builder.toString().getBytes(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            logger.error("Failed create {}", newFile.getAbsolutePath());
        }
    }

    public static File getExistFile(String fileName) {
        return new File(Config.SOLIDITY_SOURCE_PATH, fileName.trim());
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
}
