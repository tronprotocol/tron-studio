package org.tron.studio.ui;

import com.alibaba.fastjson.JSONObject;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import org.apache.commons.lang3.StringUtils;
import org.spongycastle.util.encoders.Hex;
import org.tron.abi.FunctionReturnDecoder;
import org.tron.abi.datatypes.Function;
import org.tron.api.GrpcAPI;
import org.tron.common.utils.ByteArray;
import org.tron.core.Wallet;
import org.tron.core.capsule.TransactionCapsule;
import org.tron.protos.Protocol;
import org.tron.studio.ShareData;
import org.tron.studio.TransactionHistoryItem;
import org.tron.studio.TransactionHistoryItem.Type;

import javax.annotation.PostConstruct;
import java.util.List;

public class TransactionHistoryController {

    public JFXListView<Object> transactionHistoryListView;


    @PostConstruct
    public void initialize() {
        ShareData.addTransactionAction.addListener((observable, oldValue, newValue) -> {
            TransactionHistoryItem item = ShareData.transactionHistory.get(newValue);
            if (item.getType() == Type.InfoString) {
                transactionHistoryListView.getItems().add(new Label(item.getInfoString()));
            } else {
                JFXListView<Object> subList = createSubList(newValue);
                transactionHistoryListView.getItems().add(subList);
            }
        });
    }

    private GridPane createDetailPanel(String transactionHistoryId) {
        TransactionHistoryItem item = ShareData.transactionHistory.get(transactionHistoryId);
        GrpcAPI.TransactionExtention lastTransactionExtention = ShareData.wallet.getLastTransactionExtention();
        Protocol.Transaction lastTransaction = ShareData.wallet.getLastTransaction();
        String transactionId;
        ObservableList<TransactionDetail> detailTableData = null;
        if (lastTransactionExtention.getConstantResultCount() > 0) {
            transactionId = Hex.toHexString(lastTransactionExtention.getTxid().toByteArray());

            StringBuilder rawBuilder = new StringBuilder();
            lastTransactionExtention.getConstantResultList().forEach(result -> {
                rawBuilder.append(Hex.toHexString(result.toByteArray()));
            });

            Function function = item.getFunction();
            List<org.tron.abi.datatypes.Type> output = null;
            StringBuilder outputBuilder = new StringBuilder();
            if (function != null) {
                output = FunctionReturnDecoder.decode(rawBuilder.toString(), function.getOutputParameters());
                for (int i = 0; i < output.size(); i++) {
                    outputBuilder.append(output.get(i).getValue());
                    if (i != output.size() - 1) {
                        outputBuilder.append(",");
                    }
                }
            }

            detailTableData = FXCollections.observableArrayList(
                    new TransactionDetail("transaction_id", transactionId),
                    new TransactionDetail("contract_result", function == null ? rawBuilder.toString() : outputBuilder.toString())
            );
        } else {
            transactionId = Hex.toHexString(new TransactionCapsule(lastTransaction).getTransactionId().getBytes());

            Protocol.TransactionInfo transactionInfo = ShareData.wallet.getTransactionInfoById(transactionId).get();
            StringBuilder rawBuilder = new StringBuilder();
            transactionInfo.getContractResultList().forEach(result -> {
                rawBuilder.append(Hex.toHexString(result.toByteArray()));
            });

            Function function = item.getFunction();
            List<org.tron.abi.datatypes.Type> output = null;
            StringBuilder outputBuilder = new StringBuilder();
            if (function != null) {
                output = FunctionReturnDecoder.decode(rawBuilder.toString(), function.getOutputParameters());
                for (int i = 0; i < output.size(); i++) {
                    outputBuilder.append(output.get(i).getValue());
                    if (i != output.size() - 1) {
                        outputBuilder.append(",");
                    }
                }
            }

            detailTableData = FXCollections.observableArrayList(
                    new TransactionDetail("transaction_id", transactionId),
                    new TransactionDetail("fee", Long.toString(transactionInfo.getFee())),
                    new TransactionDetail("block_number", Long.toString(transactionInfo.getBlockNumber())),
                    new TransactionDetail("time_stamp", Long.toString(transactionInfo.getBlockTimeStamp())),
                    new TransactionDetail("result", transactionInfo.getResult().equals(Protocol.TransactionInfo.code.SUCESS) ? "success" : "fail"),
                    new TransactionDetail("result_message", ByteArray.toStr(transactionInfo.getResMessage().toByteArray())),
                    new TransactionDetail("contract_result", function == null ? rawBuilder.toString() : outputBuilder.toString()),
                    new TransactionDetail("contract_address", Wallet.encode58Check(transactionInfo.getContractAddress().toByteArray())),
                    new TransactionDetail("energy_usage", Long.toString(transactionInfo.getReceipt().getEnergyUsage())),
                    new TransactionDetail("energy_fee(sun)", Long.toString(transactionInfo.getReceipt().getEnergyFee())),
                    new TransactionDetail("origin_energy_usage", Long.toString(transactionInfo.getReceipt().getOriginEnergyUsage())),
                    new TransactionDetail("energy_usage_total", Long.toString(transactionInfo.getReceipt().getEnergyUsageTotal())),
                    new TransactionDetail("net_usage", Long.toString(transactionInfo.getReceipt().getNetUsage())),
                    new TransactionDetail("net_fee", Long.toString(transactionInfo.getReceipt().getNetFee()))
            );
        }
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(20);
        ColumnConstraints columnConstraints0 = new ColumnConstraints();
        columnConstraints0.setHgrow(Priority.NEVER);
        ColumnConstraints columnConstraints1 = new ColumnConstraints();
        columnConstraints1.setHgrow(Priority.ALWAYS);
        gridPane.getColumnConstraints().add(columnConstraints0);
        gridPane.getColumnConstraints().add(columnConstraints1);

        for (int i = 0; i < detailTableData.size(); i++) {
            TransactionDetail detail = detailTableData.get(i);
            gridPane.add(new Label(detail.keyProperty.get()), 0, i);
            gridPane.add(new Label(detail.valueProperty.get()), 1, i);
        }
        gridPane.setUserData(detailTableData);
        return gridPane;
    }

    private JFXListView<Object> createSubList(String transactionHistoryId) {
        TransactionHistoryItem transactionHistoryItem = ShareData.transactionHistory.get(transactionHistoryId);
        JFXListView<Object> subList = new JFXListView<>();

        GridPane gridPane = createDetailPanel(transactionHistoryId);
        subList.getItems().add(gridPane);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setSpacing(5);

        MaterialDesignIconView copyIcon = new MaterialDesignIconView();
        copyIcon.setGlyphName("BUG");
        copyIcon.setStyleClass("icon");
        hBox.getChildren().add(copyIcon);

        String acountAddr = ShareData.currentAccount;
        acountAddr = StringUtils.left(acountAddr, 10) + "...";

        String currentContract = ShareData.currentContractName.get();
        String currentValue = ShareData.currentValue;

        Function function = transactionHistoryItem.getFunction();
        String debugInfo = "[vm] from: %s to: %s.%s value:%s hash:%s";
        debugInfo = String.format(debugInfo,
                acountAddr,
                currentContract, function == null ? "" : function.getName(),
                currentValue,
                StringUtils.left(transactionHistoryId, 10) + "...");
        Label debugInfoLabel = new Label(debugInfo);
        hBox.getChildren().add(debugInfoLabel);

        Region region1 = new Region();
        hBox.getChildren().add(region1);
        HBox.setHgrow(region1, Priority.ALWAYS);
        JFXButton debugBtn = new JFXButton("Debug");
        debugBtn.getStyleClass().add("custom-jfx-button-raised-fix-width");
        JFXButton copyBtn = new JFXButton("Copy Info");
        copyBtn.getStyleClass().add("custom-jfx-button-raised-fix-width");
        hBox.getChildren().add(copyBtn);
        hBox.getChildren().add(debugBtn);
        Region region2 = new Region();
        region2.setPrefWidth(30);
        hBox.getChildren().add(region2);

        debugBtn.setOnAction(event -> {
            ShareData.debugTransactionAction.set(transactionHistoryId);
        });
        copyBtn.setOnAction(event -> {
            JSONObject result = new JSONObject();
            ObservableList<TransactionDetail> detailPanelData = (ObservableList<TransactionDetail>) gridPane.getUserData();
            for (TransactionDetail detailPanelDatum : detailPanelData) {
                result.put(detailPanelDatum.keyProperty.get(), detailPanelDatum.valueProperty.get());
            }
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(result.toJSONString());
            clipboard.setContent(clipboardContent);
        });

        subList.setGroupnode(hBox);

        return subList;
    }

    public void onClickDelete(MouseEvent mouseEvent) {
        transactionHistoryListView.getItems().clear();
    }

    private <T> void setupCellValueFactory(JFXTreeTableColumn<TransactionDetail, T> column, java.util.function.Function<TransactionDetail, ObservableValue<T>> mapper) {
        column.setCellValueFactory((TreeTableColumn.CellDataFeatures<TransactionDetail, T> param) -> {
            if (column.validateValue(param)) {
                return mapper.apply(param.getValue().getValue());
            } else {
                return column.getComputedValue(param);
            }
        });
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

}
