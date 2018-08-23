package org.tron.studio.ui;

import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.spongycastle.util.encoders.Hex;
import org.tron.api.GrpcAPI;
import org.tron.core.capsule.TransactionCapsule;
import org.tron.protos.Protocol;
import org.tron.studio.ShareData;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.UUID;
import java.util.function.Function;

public class TransactionHistoryController {

    public JFXListView<Object> transactionHistoryListView;


    @PostConstruct
    public void initialize() throws IOException {
        ShareData.addTransactionAction.addListener((observable, oldValue, newValue) -> {
            String currentContractName = ShareData.currentContractName.get();
            String transactionHeadMsg = String.format("creation of %s pending...", currentContractName);
            transactionHistoryListView.getItems().add(new Label(transactionHeadMsg));

            String[] labels = {"labels"};
            JFXListView<Object> subList = createSubList(labels);
            transactionHistoryListView.getItems().add(subList);

            /**
             int transationResCnt = ShareData.currentTransactionExtention.getConstantResultCount();

             if (transationResCnt == 0)
             {
             int signatureCount = ShareData.currentTransactionExtention.getTransaction().getSignatureCount();
             } **/
        });
    }


    private JFXTreeTableView<TransactionDetail> createDetailTable() {

        JFXTreeTableView<TransactionDetail> detailTable = new JFXTreeTableView<>();
        JFXTreeTableColumn<TransactionDetail, String> keyCol = new JFXTreeTableColumn<>("Item");
        JFXTreeTableColumn<TransactionDetail, String> valueCol = new JFXTreeTableColumn<>("Value");
        detailTable.getColumns().add(keyCol);
        detailTable.getColumns().add(valueCol);

        setupCellValueFactory(keyCol, TransactionDetail::keyProperty);
        setupCellValueFactory(valueCol, TransactionDetail::valueProperty);

        GrpcAPI.TransactionExtention lastTransactionExtention = ShareData.wallet.getLastTransactionExtention();
        Protocol.Transaction lastTransaction = ShareData.wallet.getLastTransaction();
        String transactionId;
        int hashcode;
        if(lastTransactionExtention.getConstantResultCount() > 0) {
            transactionId = Hex.toHexString(lastTransactionExtention.getTxid().toByteArray());
            hashcode = lastTransactionExtention.hashCode();
        } else {
            transactionId = Hex.toHexString(new TransactionCapsule(lastTransaction).getTransactionId().getBytes());
            hashcode = lastTransaction.hashCode();

        }

        System.out.println(hashcode);

        ObservableList<TransactionDetail> detailTableData = FXCollections.observableArrayList(
                new TransactionDetail("transaction id", transactionId)
        );
        detailTable.setRoot(new RecursiveTreeItem<>(detailTableData, RecursiveTreeObject::getChildren));
        detailTable.setShowRoot(false);

        return detailTable;
    }

    private JFXListView<Object> createSubList(String[] labels) {
        JFXListView<Object> subList = new JFXListView<>();

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

        String acountAddr = ShareData.currentAccount;
        acountAddr = acountAddr.substring(0,5) + "..." + acountAddr.substring(acountAddr.length()-5);

        String currentContract = ShareData.currentContractName.get();
        String currentValue = ShareData.currentValue;

        String debugInfo = "[vm] from: %s to: %s.(consructor)\n value:%s data: xxxxx. logs:0 hash:xxxx";
        debugInfo = String.format(debugInfo, acountAddr, currentContract, currentValue);
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

    static final class TransactionDetail extends RecursiveTreeObject<TransactionDetail> {

        final StringProperty keyProperty;
        final StringProperty valueProperty;

        TransactionDetail(String key, String val) {
            this.keyProperty = new SimpleStringProperty(key);
            this.valueProperty = new SimpleStringProperty(val);
        }

        StringProperty keyProperty() {
            return keyProperty;
        }

        StringProperty valueProperty() {
            return valueProperty;
        }
    }

    private <T> void setupCellValueFactory(JFXTreeTableColumn<TransactionDetail, T> column, Function<TransactionDetail, ObservableValue<T>> mapper) {
        column.setCellValueFactory((TreeTableColumn.CellDataFeatures<TransactionDetail, T> param) -> {
            if (column.validateValue(param)) {
                return mapper.apply(param.getValue().getValue());
            } else {
                return column.getComputedValue(param);
            }
        });
    }

}
