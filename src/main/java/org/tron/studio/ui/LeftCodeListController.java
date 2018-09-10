package org.tron.studio.ui;

import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.studio.MainApplication;
import org.tron.studio.ShareData;
import org.tron.studio.filesystem.SolidityFileUtil;
import org.tron.studio.utils.FileNameFieldValidator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class LeftCodeListController {
    static final Logger logger = LoggerFactory.getLogger(RightTabCompileController.class);

    public JFXTreeTableView<FileName> fileNameTable;
    public JFXTreeTableColumn<FileName, String> fileNameColumn;

    private ObservableList<FileName> fileNameData;

    private String lastCodeContent;
    private String lastFileName;

    private final FileChooser fileChooser = new FileChooser();
    private ScheduledExecutorService autoSaveExecutor = Executors.newSingleThreadScheduledExecutor();

    @FXML
    public void initialize() {
        //监听新建的合约列表，
        ShareData.newContractFileName.addListener((observable, oldValue, newValue) -> {
            List<File> files = SolidityFileUtil.getFileNameList();
            fileNameData.clear();
            files.forEach(file -> {
                fileNameData.add(new FileName(file.getName()));
            });
        });

        autoSaveExecutor.scheduleWithFixedDelay(() -> {
            saveContractContent();
        }, 5_000, 1_000, TimeUnit.MILLISECONDS);

        setupCellValueFactory(fileNameColumn, FileName::fileNameProperty);
        fileNameData = FXCollections.observableArrayList();
        fileNameTable.setRoot(new RecursiveTreeItem<>(fileNameData, RecursiveTreeObject::getChildren));
        fileNameTable.setShowRoot(false);

        fileNameTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        File fileName = SolidityFileUtil.getExistFile(newSelection.getValue().fileName.getValue());
                        ShareData.currentContractFileName.set(fileName.getName());
                        ShareData.allContractFileName.add(fileName.getName());
                    }
                });

        ContextMenu contextMenu = new ContextMenu();
        MenuItem delMenu = new MenuItem("Delete");
        contextMenu.getItems().add(delMenu);


        delMenu.setOnAction(event -> {

            //Not allowed to delete last file
            if(SolidityFileUtil.getFileNameList().size() == 1) {
                return;
            }

            // Get current file name
            String selectedFile = fileNameTable.getSelectionModel().selectedItemProperty().getValue().getValue().fileName.get();
            ShareData.deleteContract.set(selectedFile);

            int currentIndex = -1;
            for (int i = 0; i < SolidityFileUtil.getFileNameList().size(); i++) {
                if (StringUtils.equals(SolidityFileUtil.getFileNameList().get(i).getName(), selectedFile)) {
                    currentIndex = i;
                    break;
                }
            }

            if (currentIndex == -1) {
                return;
            }

            ShareData.allContractFileName.remove(selectedFile);

            for (FileName filename : fileNameData) {
                if (filename.fileName.get().contains(selectedFile)) {
                    fileNameData.remove(filename);
                    break;
                }
            }

            for (File file : SolidityFileUtil.getFileNameList()) {
                if (org.apache.commons.lang3.StringUtils.equals(file.getName(), selectedFile)) {
                    SolidityFileUtil.getFileNameList().remove(file);
                    if (file.delete()) {
                        logger.info(String.format("%s is deleted", file.getName()));
                    } else {
                        logger.info(String.format("Deleting %s failed", file.getName()));
                    }
                }
            }

            int fileCounter = SolidityFileUtil.getFileNameList().size();
            int nextCurrentIndex = currentIndex;

            if (fileCounter != 0) {
                if (currentIndex > fileCounter - 1) {
                    nextCurrentIndex = currentIndex + 1;
                }

                fileNameTable.getSelectionModel().select(nextCurrentIndex);
                ShareData.currentContractFileName.set(fileNameTable.getSelectionModel().getSelectedItem().getValue().fileName.getValue());
            } else {
                logger.info("No file to show");
            }
        });

        fileNameTable.setContextMenu(contextMenu);

        List<File> files = SolidityFileUtil.getFileNameList();
        ShareData.newContractFileName.set(files.get(0).getName());
        ShareData.allContractFileName.add(files.get(0).getName());
    }

    public void createContract(MouseEvent mouseEvent) {
        JFXDialog dialog = new JFXDialog();
        JFXDialogLayout layout = new JFXDialogLayout();
        layout.setPrefWidth(800);
        layout.setHeading(new Label("Create Smart Contract"));
        JFXTextField contractFileNameTextField = new JFXTextField();
        contractFileNameTextField.setPromptText("Please input contract name: Example.sol");
        contractFileNameTextField.setValidators(new FileNameFieldValidator());
        contractFileNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            contractFileNameTextField.validate();
        });
        layout.setBody(contractFileNameTextField);
        dialog.setContent(layout);
        JFXButton closeButton = new JFXButton("OK");
        closeButton.getStyleClass().add("dialog-accept");
        closeButton.setOnAction(event -> {
            try {
                if (!contractFileNameTextField.validate()) {
                    return;
                }
                String contractFileName = contractFileNameTextField.getText();
                contractFileName = SolidityFileUtil.formatFileName(contractFileName);
                SolidityFileUtil.createNewFile(contractFileName);
                ShareData.newContractFileName.set(contractFileName);
                ShareData.allContractFileName.get().add(contractFileName);
            } catch (Exception e) {
                logger.error("Failed: {}", e);
                return;
            }
            dialog.close();
        });
        layout.setActions(closeButton);
        dialog.show((StackPane) MainApplication.instance.primaryStage.getScene().getRoot());
    }

    public void saveContract(MouseEvent mouseEvent) {
        logger.info("save contract");
        saveContractContent();
    }

    private void saveContractContent() {
        try {
            VirtualizedScrollPane virScrollPane = (VirtualizedScrollPane) ShareData.currentContractTab.getContent();
            String content = ((CodeArea) virScrollPane.getContent()).getText();
            String filename = ShareData.currentContractTab.getText();
            if (StringUtils.equals(lastFileName, filename) && StringUtils.equals(content, lastCodeContent)) {
                return;
            }

            for (File file : SolidityFileUtil.getFileNameList()) {
                if (StringUtils.equals(file.getName(), filename)) {
                    try {
                        BufferedWriter out = new BufferedWriter(new FileWriter(file));
                        out.write(content);
                        out.close();
                        logger.info(String.format("%s", file));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
            lastFileName = filename;
            lastCodeContent = content;
        } catch (Exception e) {
            logger.error("Failed to saveContractContent {}", e);
        }
    }

    private void openFile(File file) {
        try {
            ShareData.openContractFileName.set(file.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openContract(MouseEvent mouseEvent) {
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            openFile(file);

            String fileName = file.getName();
            fileName = SolidityFileUtil.formatFileName(fileName);
            SolidityFileUtil.createNewFile(fileName);
            ShareData.newContractFileName.set(fileName);
            ShareData.allContractFileName.get().add(fileName);
            ShareData.currentContractName.set(fileName);
        }
    }

    private <T> void setupCellValueFactory(JFXTreeTableColumn<FileName, T> column, Function<FileName, ObservableValue<T>> mapper) {
        column.setCellValueFactory((TreeTableColumn.CellDataFeatures<FileName, T> param) -> {
            if (column.validateValue(param)) {
                return mapper.apply(param.getValue().getValue());
            } else {
                return column.getComputedValue(param);
            }
        });
    }

    static final class FileName extends RecursiveTreeObject<FileName> {

        final StringProperty fileName;

        FileName(String fileName) {
            this.fileName = new SimpleStringProperty(fileName);
        }

        StringProperty fileNameProperty() {
            return fileName;
        }
    }
}
