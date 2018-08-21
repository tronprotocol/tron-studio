package org.tron.studio;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import lombok.extern.slf4j.Slf4j;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.tron.studio.ui.SolidityHighlight;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
public class MainController {
    public CodeArea codeArea;
    public TabPane codeAreaTabPane;

    public Tab defaultTab;
    public TabPane rightContentTabPane;

    @PostConstruct
    public void initialize() throws IOException {
        StringBuilder builder = new StringBuilder();
        try {
            Files.lines(Paths.get(getClass().getResource("/template/Ballot.sol").getPath())).forEach(line -> {
                builder.append(line).append(System.getProperty("line.separator"));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        defaultTab.setOnCloseRequest(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                int tabs_size = codeAreaTabPane.getTabs().size();
                System.out.println(tabs_size);
                if (tabs_size == 1) {
                    // Create blank file when closing last file
                    ShareData.newContractFileName.set("/template/Ballot.sol");
                }
            }
        });

        new SolidityHighlight(codeArea).highlight();
        codeArea.replaceText(0, 0, builder.toString());
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

        SingleSelectionModel<Tab> selectionModel = codeAreaTabPane.getSelectionModel();

        ShareData.selectFile.addListener((observable, oldValue, newValue) -> {
            selectionModel.select(ShareData.currentFileIndex);
        });

        ShareData.debugTransactionAction.addListener((observable, oldValue, newValue) -> {
            rightContentTabPane.getSelectionModel().selectLast();
        });

        ShareData.newContractFileName.addListener((observable, oldValue, newValue) -> {
            try {
                Tab codeTab = FXMLLoader.load(getClass().getResource("ui/code_panel.fxml"));
                codeTab.setText(newValue);
                codeTab.setClosable(true);

                codeTab.setOnCloseRequest(new EventHandler<Event>() {
                    @Override
                    public void handle(Event event) {
                        int tabs_size = codeAreaTabPane.getTabs().size();
                        System.out.println(tabs_size);
                        if (tabs_size == 1) {
                            // Create blank file when closing last file
                            ShareData.newContractFileName.set("/template/Ballot.sol");
                        }
                    }
                });

                codeAreaTabPane.getTabs().add(codeTab);
                StringBuilder templateBuilder = new StringBuilder();
                templateBuilder.append("pragma solidity ^0.4.0;").append("\n");
                templateBuilder.append("contract ").append(newValue).append(" {").append("\n");
                templateBuilder.append("}").append("\n");
                CodeArea codeArea = (CodeArea) codeTab.getContent();

                new SolidityHighlight(codeArea).highlight();
                codeArea.replaceText(0, 0, templateBuilder.toString());
                codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

                codeAreaTabPane.getSelectionModel().select(codeTab);
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        });
    }

    private String debugContract() {
        String address = ShareData.wallet.getClass().getName();
        String msg = String.format("from: %s", address);
        return msg;
    }


}
