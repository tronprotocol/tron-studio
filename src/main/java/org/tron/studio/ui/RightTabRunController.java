package org.tron.studio.ui;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.tron.abi.FunctionEncoder;
import org.tron.abi.datatypes.Type;
import org.tron.abi.datatypes.generated.AbiTypes;
import org.tron.api.GrpcAPI.TransactionExtention;
import org.tron.core.Wallet;
import org.tron.core.capsule.TransactionCapsule;
import org.tron.core.exception.CancelException;
import org.tron.core.services.http.Util;
import org.tron.keystore.CipherException;
import org.tron.protos.Protocol.Transaction;
import org.tron.studio.ShareData;
import org.tron.studio.TransactionHistoryItem;
import org.tron.studio.solc.CompilationResult;
import org.tron.studio.solc.CompilationResult.ContractMetadata;
import org.tron.studio.utils.AbiUtil;
import org.tron.studio.walletserver.WalletClient;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class RightTabRunController implements Initializable {

    static final Logger logger = LoggerFactory.getLogger(RightTabRunController.class);
    public JFXComboBox<String> environmentComboBox;
    public JFXComboBox<String> contractComboBox;
    public JFXComboBox<String> accountComboBox;
    public JFXTextField feeLimitTextField;
    public JFXTextField valueTextField;
    public JFXComboBox feeUnitComboBox;
    public JFXComboBox valueUnitComboBox;
    public JFXTextField userPayRatio;
    public JFXListView deployedContractList;

    public JFXTextField constructorParaTextField;

    private static String DEFAULT_FEE_LIMIT = String.valueOf(100);
    private static String DEFAULT_VALUE = String.valueOf(0);
    private static String DEFAULT_RATIO = String.valueOf(100);

    public void initialize(URL location, ResourceBundle resources) {
        environmentComboBox.setItems(FXCollections.observableArrayList(
                "Local TVM",
                "Test Net",
                "Main Net"
        ));
        environmentComboBox.getSelectionModel().selectFirst();

        accountComboBox.setItems(FXCollections.observableArrayList(ShareData.testAccount.keySet()));
        accountComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                    String address = newValue;
                    String privateKey = ShareData.testAccount.get(address);
                    ShareData.wallet = new WalletClient(Hex.decode(privateKey));
                }
        );
        accountComboBox.getSelectionModel().selectFirst();

        feeUnitComboBox.setItems(FXCollections.observableArrayList(
                "TRX",
                "SUN"
        ));
        feeUnitComboBox.getSelectionModel().selectFirst();

        valueUnitComboBox.setItems(FXCollections.observableArrayList(
                "TRX",
                "SUN"
        ));
        valueUnitComboBox.getSelectionModel().selectFirst();

        feeLimitTextField.setText(DEFAULT_FEE_LIMIT);
        valueTextField.setText(DEFAULT_VALUE);
        userPayRatio.setText(DEFAULT_RATIO);

        reloadContract();
    }

    private void reloadContract() {
        ShareData.currentContractFileName.addListener((observable, oldValue, newValue) -> {
            List<String> contractNameList = new ArrayList<>();
            if (newValue != null) {
                CompilationResult compilationResult = ShareData.getCompilationResult(newValue);
                compilationResult.getContracts().forEach(contractResult -> {
                    JSONObject metaData = JSON.parseObject(contractResult.metadata);
                    JSONObject compilationTarget = metaData.getJSONObject("settings")
                            .getJSONObject("compilationTarget");
                    compilationTarget.forEach((sol, value) -> {
                        contractNameList.add((String) value);
                    });
                });
            }
            contractComboBox.setItems(FXCollections.observableArrayList(
                    contractNameList
            ));
            contractComboBox.getSelectionModel().selectFirst();
        });
    }

    public void onClickDeploy(ActionEvent actionEvent) {
        CompilationResult result = ShareData
                .getCompilationResult(ShareData.currentContractFileName.get());
        if (result == null) {
            logger.error("No CompilationResult found");
            return;
        }
        String currentContractName = contractComboBox.valueProperty().get();

        ContractMetadata currentContract = result.getContract(currentContractName);

        boolean deployContractResult = false;
        StringBuilder bin = new StringBuilder(currentContract.bin);

        {
            //Find out constructor, and encode constructor parameter, then append it to the end of bytecode
            List<JSONObject> abiJson = JSONArray.parseArray(currentContract.abi, JSONObject.class);
            Optional<JSONObject> constructorJSONObject = abiJson.stream()
                    .filter(entry -> StringUtils.equalsIgnoreCase("constructor", entry.getString("type")))
                    .findFirst();
            if (constructorJSONObject.isPresent()) {
                JSONObject constructorJSON = constructorJSONObject.get();
                JSONArray inputsArray = constructorJSON.getJSONArray("inputs");
                List<String> constructorParaValue = Arrays
                        .asList(constructorParaTextField.getText().split(","));
                if (!inputsArray.isEmpty()) {
                    List<Type> constructorPara = new ArrayList<>();
                    for (int i = 0; i < inputsArray.size(); i++) {
                        JSONObject inputType = inputsArray.getJSONObject(i);
                        String value = i < constructorParaValue.size() ? constructorParaValue.get(i) : "0";
                        value = StringUtils.isNoneEmpty(value) ? value : "0";
                        Type tp = AbiTypes.getTypeWithValue(inputType.getString("type"), value);
                        constructorPara.add(tp);
                    }
                    String encodedConstructor = FunctionEncoder.encodeConstructor(constructorPara);
                    bin.append(encodedConstructor);
                }
            }
        }
        try {
            long callValue = Long.parseLong(valueTextField.getText());
            if (valueUnitComboBox.getSelectionModel().getSelectedIndex() == 0) {
                callValue *= 1_000_000;
            }
            long feeLimit = Long.parseLong(feeLimitTextField.getText());
            if (feeUnitComboBox.getSelectionModel().getSelectedIndex() == 0) {
                feeLimit *= 1_000_000;
            }
            deployContractResult = ShareData.wallet
                    .deployContract(currentContractName, currentContract.abi, bin.toString(),
                            feeLimit, callValue,
                            Long.parseLong(userPayRatio.getText()), null);
        } catch (IOException | CipherException | CancelException e) {
            logger.error("Failed to deployContract{} {}", e.getMessage(), e);
            return;
        }

        if (!deployContractResult) {
            String uuid = UUID.randomUUID().toString();
            addTransactionHistoryItem(uuid, new TransactionHistoryItem(TransactionHistoryItem.Type.ERROR,
                    "Failed to deployContract. Please check tron.log"));
            return;
        }

        TransactionExtention transactionExtention = ShareData.wallet.getLastTransactionExtention();
        String transactionId = Hex.toHexString(transactionExtention.getTxid().toByteArray());
        if (!transactionExtention.getResult().getResult()) {
            addTransactionHistoryItem(transactionId, new TransactionHistoryItem(
                    TransactionHistoryItem.Type.ERROR,
                    String.format("Unable to get last TransactionExtention: %s",
                            transactionExtention.getResult().getMessage().toStringUtf8())));
            return;
        }

        ShareData.currentContractName.set(currentContractName);
        ShareData.currentAccount = accountComboBox.valueProperty().get();
        ShareData.currentValue = valueTextField.getText();

        Transaction transaction = ShareData.wallet.getLastTransaction();
        transactionId = Hex.toHexString(new TransactionCapsule(transaction).getTransactionId().getBytes());
        addTransactionHistoryItem(transactionId, new TransactionHistoryItem(TransactionHistoryItem.Type.Transaction, transaction));

        ShareData.currentContractName.set(currentContractName);

        byte[] ownerAddress = Wallet.decodeFromBase58Check(accountComboBox.getSelectionModel().getSelectedItem());
        byte[] contractAddress = Util.generateContractAddress(transaction, ownerAddress);
        deployedContractList.getItems()
                .add(getContractRunPanel(currentContractName, contractAddress, currentContract.abi));

    }

    private JFXListView getContractRunPanel(String contractName, byte[] contractAddress, String abi) {
        JFXListView listView = new JFXListView();
        listView.getStyleClass().add("sublist");

        HBox title = new HBox();
        String contractAddressString = Wallet.encode58Check(contractAddress);
        Label transactionLabel = new Label(String.format("%s %s", contractName, contractAddressString));
        title.getChildren().add(transactionLabel);
        listView.setGroupnode(title);

        List<String> abiJson = JSONArray.parseArray(abi, String.class);
        GridPane gridPane = new GridPane();
        gridPane.setHgap(5);
        gridPane.setVgap(5);
        ColumnConstraints columnConstraints0 = new ColumnConstraints();
        columnConstraints0.setHgrow(Priority.NEVER);
        ColumnConstraints columnConstraints1 = new ColumnConstraints();
        columnConstraints1.setHgrow(Priority.ALWAYS);
        gridPane.getColumnConstraints().add(columnConstraints0);
        gridPane.getColumnConstraints().add(columnConstraints1);
        int index = 0;
        for (String entry : abiJson) {
            JSONObject entryJson = JSONObject.parseObject(entry);
            if (StringUtils.equalsIgnoreCase("function", entryJson.getString("type"))) {
                JFXButton functionButton = new JFXButton(entryJson.getString("name"));
                functionButton.getStyleClass().add("custom-jfx-button-raised-fix-width");

                JFXTextField parameterText = new JFXTextField();
                gridPane.add(functionButton, 0, index);
                gridPane.add(parameterText, 1, index);

                JSONArray inputsJsonArray = entryJson.getJSONArray("inputs");
                StringBuilder parameterPromot = new StringBuilder();

                StringBuilder methodStr = new StringBuilder(entryJson.getString("name"));
                methodStr.append("(");
                if (inputsJsonArray != null && inputsJsonArray.size() > 0) {
                    for (int j = 0; j < inputsJsonArray.size(); j++) {
                        JSONObject inputItem = inputsJsonArray.getJSONObject(j);
                        String inputName = inputItem.getString("name");
                        String type = inputItem.getString("type");
                        parameterPromot.append(type).append(" ").append(inputName);
                        methodStr.append(type);
                        if (j != inputsJsonArray.size() - 1) {
                            parameterPromot.append(", ");
                            methodStr.append(",");
                        }
                    }
                } else {
                    parameterText.setVisible(false);
                }
                methodStr.append(")");
                parameterText.setPromptText(parameterPromot.toString());
                functionButton.setOnAction(new ClickTriggerAction(contractAddress, methodStr.toString(), parameterText));
            }
            index++;
        }
        listView.getItems().add(gridPane);
        return listView;
    }


    private void addTransactionHistoryItem(String id, TransactionHistoryItem item) {
        ShareData.transactionHistory.put(id, item);
        ShareData.addTransactionAction.set(id);
    }

    public void onClickClear(MouseEvent actionEvent) {
        deployedContractList.getItems().clear();

    }

    public void onClickAddAddress(MouseEvent mouseEvent) {
        logger.debug("onClickAddAddress");
    }

    public void onClickCopyAddress(MouseEvent mouseEvent) {
    }

    class ClickTriggerAction implements EventHandler<ActionEvent> {

        byte[] contractAddress;
        String methodStr;
        JFXTextField parameterText;

        public ClickTriggerAction(byte[] contractAddress, String methodStr, JFXTextField parameterText) {
            this.contractAddress = contractAddress;
            this.methodStr = methodStr;
            this.parameterText = parameterText;
        }

        @Override
        public void handle(ActionEvent event) {
            long callValue = Long.parseLong(valueTextField.getText());
            if (valueUnitComboBox.getSelectionModel().getSelectedIndex() == 0) {
                callValue *= 1_000_000;
            }
            long feeLimit = Long.parseLong(feeLimitTextField.getText());
            if (feeUnitComboBox.getSelectionModel().getSelectedIndex() == 0) {
                feeLimit *= 1_000_000;
            }
            try {
                byte[] data = Hex.decode(AbiUtil.parseMethod(methodStr, parameterText.getText().trim(), false));
                ShareData.wallet.triggerContract(contractAddress, callValue, data, feeLimit);
                processTriggerContractResult();
            } catch (IOException | CipherException | CancelException e) {
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        private void processTriggerContractResult() {
            TransactionExtention transactionExtention = ShareData.wallet.getLastTransactionExtention();
            String transactionId = Hex.toHexString(transactionExtention.getTxid().toByteArray());
            if (!transactionExtention.getResult().getResult()) {
                addTransactionHistoryItem(transactionId, new TransactionHistoryItem(
                        TransactionHistoryItem.Type.ERROR,
                        String.format("Unable to get last TransactionExtention: %s",
                                transactionExtention.getResult().getMessage().toStringUtf8())));
                return;
            }

            Transaction transaction = ShareData.wallet.getLastTransaction();
            transactionId = Hex.toHexString(new TransactionCapsule(transaction).getTransactionId().getBytes());
            addTransactionHistoryItem(transactionId, new TransactionHistoryItem(TransactionHistoryItem.Type.Transaction, transaction));
        }
    }
}