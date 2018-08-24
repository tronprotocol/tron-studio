package org.tron.studio;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.tron.studio.ui.SolidityHighlight;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
public class MainController {
    public TabPane rightContentTabPane;
    public TabPane codeAreaTabPane;

    public Tab defaultCodeAreaTab;
    public CodeArea defaultCodeArea;

    private String defaultContractFile = "/template/Ballot.sol";

    @PostConstruct
    public void initialize() throws IOException {
        StringBuilder builder = new StringBuilder();
        try {
            Files.lines(Paths.get(getClass().getResource(defaultContractFile).getPath())).forEach(line -> {
                builder.append(line).append(System.getProperty("line.separator"));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        defaultCodeAreaTab.setText(defaultContractFile);
        //Just not allow to close the default tab
        defaultCodeAreaTab.setClosable(false);

        defaultCodeArea = (CodeArea) defaultCodeAreaTab.getContent();
        new SolidityHighlight(defaultCodeArea).highlight();
        defaultCodeArea.replaceText(0, 0, builder.toString());
        defaultCodeArea.setParagraphGraphicFactory(LineNumberFactory.get(defaultCodeArea));

        SingleSelectionModel<Tab> selectionModel = codeAreaTabPane.getSelectionModel();

        ShareData.currentContractFileName.addListener((observable, oldValue, newValue) -> {
            for (Tab tab : codeAreaTabPane.getTabs()) {
                if(StringUtils.equals(tab.getText(), newValue)) {
                    selectionModel.select(tab);
                }
            }
        });

        ShareData.newContractFileName.addListener((observable, oldValue, newValue) -> {
            try {
                Tab codeTab = new Tab();
                codeTab.setText(newValue);
                codeTab.setClosable(true);
                CodeArea codeArea = FXMLLoader.load(getClass().getResource("ui/code_area.fxml"));
                codeTab.setContent(codeArea);
                codeAreaTabPane.getTabs().add(codeTab);

                StringBuilder templateBuilder = new StringBuilder();
                templateBuilder.append("pragma solidity ^0.4.24;").append("\n");
                templateBuilder.append("contract ").append(newValue).append(" {").append("\n");
                templateBuilder.append("}").append("\n");

                new SolidityHighlight(codeArea).highlight();
                codeArea.replaceText(0, 0, templateBuilder.toString());
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
