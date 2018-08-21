package org.tron.studio.ui;

import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXRippler;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.tron.studio.ShareData;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.UUID;

public class TransactionHistoryController {

    public JFXListView<Object> transactionHistoryListView;


    @PostConstruct
    public void initialize() throws IOException {
        ShareData.addTransactionAction.addListener((observable, oldValue, newValue) -> {
            String currentContractName = ShareData.currentContractName.get();
            String transactionHeadMsg = String.format("creation of %s pending...", currentContractName);
            transactionHistoryListView.getItems().add(new Label(transactionHeadMsg));

            String[] labels = {"labels"};

            JFXListView<Object> subList = createList(labels);

            transactionHistoryListView.getItems().add(subList);

            /**
             int transationResCnt = ShareData.currentTransactionExtention.getConstantResultCount();

             if (transationResCnt == 0)
             {
             int signatureCount = ShareData.currentTransactionExtention.getTransaction().getSignatureCount();
             } **/
        });
    }


    private TransactionTableView<TransactionDetail> createDetailTable() {
        TransactionTableView<TransactionDetail> table = new TransactionTableView<TransactionDetail>();

        TableColumn<TransactionDetail, String> fieldNameCol = new TableColumn("Field Name");
        fieldNameCol.setMinWidth(50);
        fieldNameCol.setCellValueFactory(
                new PropertyValueFactory<TransactionDetail, String>("fieldName"));

        TableColumn<TransactionDetail, String> valueCol = new TableColumn("Value");
        valueCol.setMinWidth(200);
        valueCol.setCellValueFactory(
                new PropertyValueFactory<TransactionDetail, String>("value"));

        // dummy data
        final ObservableList<TransactionDetail> data =
                FXCollections.observableArrayList(
                        new TransactionDetail("status", "0x1 Transaction mined and execution succeed"),
                        new TransactionDetail("transaction hash", "0xdbc3c61781c3f61d09339b98a553a3d1ddf9a00e5888574c8cba1c7b00a81a62"),
                        new TransactionDetail("contract address", "0x692a70d2e424a56d2c6c27aa97d1a86395877b3a")
                );

        table.setItems(data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().addAll(fieldNameCol, valueCol);

        return table;
    }

    private JFXListView<Object> createList(String[] labels) {
        JFXListView<Object> subList = new JFXListView<>();

        // Create table to show details of transaction


        //subList.getItems().add(new Label("details"));
        subList.getItems().add(createDetailTable());

        Node node = new HBox();

        JFXRippler ripper = new JFXRippler();
        ripper.setStyle(":cons-rippler1");
        ripper.setPosition(JFXRippler.RipplerPos.FRONT);

        StackPane pane = new StackPane();
        pane.setStyle(":-fx-padding: 2;");

        MaterialDesignIconView copyIcon = new MaterialDesignIconView();
        copyIcon.setGlyphName("BUG");
        copyIcon.setStyleClass("icon");
        pane.getChildren().add(copyIcon);

        ripper.getChildren().add(pane);

        ((HBox) node).getChildren().add(ripper);

        String debugInfo = "[vm] from: xxxx to:Ballot.xxxx \n value:0 data: xxxxx. logs:0 hash:xxxx";
        Label debugInfoLabel = new Label(debugInfo);
        ((HBox) node).getChildren().add(debugInfoLabel);

        Button debugBtn = new Button("Debug");
        ((HBox) node).getChildren().add(debugBtn);

        debugBtn.setOnAction(event -> {
            ShareData.debugTransactionAction.set(UUID.randomUUID().toString());
        });

        subList.setGroupnode(node);

        return subList;
    }

    public static class TransactionDetail {
        private final SimpleStringProperty fieldName;
        private final SimpleStringProperty value;

        private TransactionDetail(String fieldName, String value) {
            this.fieldName = new SimpleStringProperty(fieldName);
            this.value = new SimpleStringProperty(value);
        }

        public String getFieldName() {
            return fieldName.get();
        }

        public void setFieldName(String fieldName) {
            this.fieldName.set(fieldName);
        }

        public String getValue() {
            return value.get();
        }

        public void setValue(String value) {
            this.value.set(value);
        }
    }

    class TransactionTableView<T> extends TableView<T> {
        @Override
        public void resize(double width, double height) {
            super.resize(width, height);
            Pane header = (Pane) lookup("TableHeaderRow");
            header.setMinHeight(0);
            header.setPrefHeight(0);
            header.setMaxHeight(0);
            header.setVisible(false);
        }
    }

}
