package org.tron.studio;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.tron.studio.filesystem.SolidityFileUtil;
import org.tron.studio.ui.SolidityHighlight;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class MainController {
    public TabPane rightContentTabPane;
    public TabPane codeAreaTabPane;

    public Tab defaultCodeAreaTab;
    public CodeArea defaultCodeArea;


    private int contractFileNum = 0;
    private String previousValue = "";

    @PostConstruct
    public void initialize() throws IOException {
        List<File> files = SolidityFileUtil.getFileNameList();
        File defaultContractFile = files.get(0);

        StringBuilder builder = new StringBuilder();
        try {
            Files.lines(Paths.get(defaultContractFile.getAbsolutePath())).forEach(line -> {
                builder.append(line).append(System.getProperty("line.separator"));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        contractFileNum = ShareData.allContractFileName.size();

        // Add default contract into the list of contracts
        //ShareData.allContractFileName.get().add(defaultContractFile);

        defaultCodeAreaTab.setText(defaultContractFile.getName());
        //Just not allow to close the default tab
        defaultCodeAreaTab.setClosable(false);

        defaultCodeArea = (CodeArea) defaultCodeAreaTab.getContent();
        new SolidityHighlight(defaultCodeArea).highlight();
        defaultCodeArea.replaceText(0, 0, builder.toString());
        defaultCodeArea.setParagraphGraphicFactory(LineNumberFactory.get(defaultCodeArea));

        previousValue = ShareData.currentContractName.get();

        SingleSelectionModel<Tab> selectionModel = codeAreaTabPane.getSelectionModel();

        ShareData.deleteContract.addListener((observable, oldValue, currentContractName) -> {
            for (Tab tab : codeAreaTabPane.getTabs()) {
                if (StringUtils.equals(tab.getText(), currentContractName)) {
                    codeAreaTabPane.getTabs().remove(tab);
                    break;
                }
            }
        });

        ShareData.currentContractFileName.addListener((observable, oldValue, currentContractName) -> {
            boolean alreadyOpen = false;
            for (Tab tab : codeAreaTabPane.getTabs()) {
                if (StringUtils.equals(tab.getText(), currentContractName)) {
                    selectionModel.select(tab);
                    alreadyOpen = true;
                }
            }
            if (!alreadyOpen) {
                newTab(currentContractName);
            }
            previousValue = currentContractName;
        });

        ShareData.newContractFileName.addListener((observable, oldValue, newValue) -> {
            newTab(newValue);
        });

        ShareData.openContract.addListener((observable, oldValue, newValue) ->{
            String file_path = ShareData.openContract.get();
            File newFile = new File(file_path);

            Tab newTab = setTap(newFile);
            ShareData.currentContractName.set(newFile.getName());
            ShareData.allContractFileName.add(newFile.getName());
            selectionModel.select(newTab);
        });

        ShareData.debugTransactionAction.addListener((observable, oldValue, newValue) -> {
            rightContentTabPane.getSelectionModel().selectLast();
        });
    }

    private Tab setTap(File file) {
        Tab tab = new Tab();

        CodeArea codeArea = new CodeArea();
        // Print new file in codearea
        StringBuilder builder = new StringBuilder();
        try {
            Files.lines(Paths.get(file.getAbsolutePath())).forEach(line -> {
                builder.append(line).append(System.getProperty("line.separator"));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        tab.setText(file.getName());
        //Just not allow to close the default tab
        tab.setClosable(true);
        tab.setContent(codeArea);
        codeAreaTabPane.getTabs().add(tab);

        new SolidityHighlight(codeArea).highlight();
        codeArea.replaceText(0, 0, builder.toString());
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

        ShareData.allContractFileName.add(file.getName());
        ShareData.currentContractName.set(file.getName());

        return tab;
    }

    private void newTab(String tabName) {
        try {
            Tab codeTab = new Tab();
            codeTab.setText(tabName);
            codeTab.setClosable(true);
            CodeArea codeArea = FXMLLoader.load(getClass().getResource("ui/code_area.fxml"));
            codeTab.setContent(codeArea);
            codeAreaTabPane.getTabs().add(codeTab);

            String sourceCode = SolidityFileUtil.getSourceCode(tabName);

            new SolidityHighlight(codeArea).highlight();
            codeArea.insertText(0, sourceCode);
            codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

            codeAreaTabPane.getSelectionModel().select(codeTab);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private String debugContract() {
        String address = ShareData.wallet.getClass().getName();
        String msg = String.format("from: %s", address);
        return msg;
    }


}
