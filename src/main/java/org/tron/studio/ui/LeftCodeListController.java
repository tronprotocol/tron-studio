package org.tron.studio.ui;

import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.studio.ShareData;
import org.tron.studio.filesystem.SolidityFileUtil;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class LeftCodeListController {
    static final Logger logger = LoggerFactory.getLogger(RightTabCompileController.class);

    public JFXTreeTableView<FileName> fileNameTable;
    public JFXTreeTableColumn<FileName, String> fileNameColumn;
    public StackPane dialogRoot;

    private ObservableList<FileName> fileNameData;

    //private MaterialDesignIconView newContract;

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
        setupCellValueFactory(fileNameColumn, FileName::fileNameProperty);
        fileNameData = FXCollections.observableArrayList();
        fileNameTable.setRoot(new RecursiveTreeItem<>(fileNameData, RecursiveTreeObject::getChildren));
        fileNameTable.setShowRoot(false);

        fileNameTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                File fileName = SolidityFileUtil.getExistFile(newSelection.getValue().fileName.getValue());
                ShareData.currentContractFileName.set(fileName.getName());
            }
        });

        ContextMenu cm = new ContextMenu();
        MenuItem delMenu = new MenuItem("Delete");
        cm.getItems().add(delMenu);

        delMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                int currentIndex = ShareData.allContractFileName.indexOf(ShareData.currentContractFileName.get());

                ShareData.allContractFileName.remove(ShareData.currentContractFileName.get());
                fileNameData.remove(currentIndex);

                int file_num = ShareData.allContractFileName.get().size();
                int nextCurrentIndex = 0;

                if (file_num != 0) {
                    if (currentIndex < file_num - 1) {
                        nextCurrentIndex = currentIndex + 1;
                    } else {
                        nextCurrentIndex = file_num - 1;
                    }

                    ShareData.currentContractFileName.set(ShareData.allContractFileName.get(nextCurrentIndex));
                    fileNameTable.getSelectionModel().select(nextCurrentIndex);

                } else {
                    logger.info("No file to show");
                }
            }
        });

        fileNameTable.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent t) {
                if (t.getButton() == MouseButton.SECONDARY) {
                    cm.show(fileNameTable, t.getScreenX(), t.getScreenY());
                }
            }
        });

        List<File> files = SolidityFileUtil.getFileNameList();
        ShareData.newContractFileName.set(files.get(0).getName());
        ShareData.allContractFileName.add(files.get(0).getName());
    }

    private String showDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create Smart Contract");
        dialog.setHeaderText("Please input contract name");
        dialog.setContentText("Contract Name:");

        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    public void createContract(MouseEvent mouseEvent) {
        String contractFileName = showDialog();
        if (contractFileName == null) {
            return;
        }
        contractFileName = SolidityFileUtil.formatFileName(contractFileName);
        SolidityFileUtil.createNewFile(contractFileName);
        ShareData.newContractFileName.set(contractFileName);
        ShareData.allContractFileName.get().add(contractFileName);
    }

    public void saveContract(MouseEvent mouseEvent) {
        System.out.println("save contract");
    }

    public void openContract(MouseEvent mouseEvent) {
        System.out.println("save contract");
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
