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

    static {
        File dir = new File(Config.SOLIDITY_SOURCE_PATH);
        dir.mkdirs();
    }

    public static List<File> getFileNameList() {
        List<File> list = new ArrayList<>();
        try {
            Stream<Path> fileList = Files.list(Paths.get(Config.SOLIDITY_SOURCE_PATH).toAbsolutePath());
            fileList.forEach(item -> list.add(item.toFile()));
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
}
