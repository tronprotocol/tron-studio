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

        ShareData.currentContractFileName.addListener((observable, oldValue, newValue) -> {
            if (contractFileNum > ShareData.allContractFileName.size())
            {
                for (Tab tab : codeAreaTabPane.getTabs()) {
                    if(StringUtils.equals(tab.getText(), previousValue))
                    {
                        codeAreaTabPane.getTabs().remove(tab);
                        break;
                    }
                }
            }

            for (Tab tab : codeAreaTabPane.getTabs()) {
                if(StringUtils.equals(tab.getText(), newValue)) {
                    selectionModel.select(tab);
                }
            }
            previousValue = newValue;
            contractFileNum = ShareData.allContractFileName.size();
        });

        ShareData.newContractFileName.addListener((observable, oldValue, newValue) -> {
            try {
                Tab codeTab = new Tab();
                codeTab.setText(newValue);
                codeTab.setClosable(true);
                CodeArea codeArea = FXMLLoader.load(getClass().getResource("ui/code_area.fxml"));
                codeTab.setContent(codeArea);
                codeAreaTabPane.getTabs().add(codeTab);

                String sourceCode = SolidityFileUtil.getSourceCode(newValue);

                new SolidityHighlight(codeArea).highlight();
                codeArea.insertText(0, sourceCode);
                codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

                codeAreaTabPane.getSelectionModel().select(codeTab);
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        });

        ShareData.debugTransactionAction.addListener((observable, oldValue, newValue) -> {
            rightContentTabPane.getSelectionModel().selectLast();
        });
    }

    private String debugContract() {
        String address = ShareData.wallet.getClass().getName();
        String msg = String.format("from: %s", address);
        return msg;
    }


}
