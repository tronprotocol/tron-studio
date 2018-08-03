package org.tron.studio.ui;

import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialog.DialogTransition;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import java.util.Optional;
import java.util.function.Function;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.studio.MainController;
import org.tron.studio.ShareData;

public class LeftCodeListController {
    static final Logger logger = LoggerFactory.getLogger(RightTabCompileController.class);

    public JFXTreeTableView<FileName> fileNameTable;
    public JFXTreeTableColumn<FileName, String> fileNameColumn;
    public StackPane dialogRoot;

    private ObservableList<FileName> fileNameData;

    @FXML
    public void initialize() {

        //监听新建的合约列表，
        ShareData.newContractName.addListener((observable, oldValue, newValue) -> {
            fileNameData.add(new FileName(newValue));
        });
        setupCellValueFactory(fileNameColumn, FileName::fileNameProperty);
        fileNameData =  FXCollections.observableArrayList();
        fileNameTable.setRoot(new RecursiveTreeItem<>(fileNameData, RecursiveTreeObject::getChildren));
        fileNameTable.setShowRoot(false);

        ShareData.newContractName.set("/template/Ballot.sol");
    }

    private String showDialog()  {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create Smart Contract");
        dialog.setHeaderText("Please input contract name");
        dialog.setContentText("Contract Name:");

        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    public void createContract(ActionEvent actionEvent) {
        String contractName = showDialog();
        if(contractName == null) {
            return;
        }
        ShareData.newContractName.set(contractName);
        ShareData.allContractName.get().add(contractName);
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
