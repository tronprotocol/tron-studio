package org.tron.studio.ui;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.tron.studio.ShareData;
import org.tron.studio.solc.CompilationResult;
import org.tron.studio.walletserver.WalletClient;

public class RightTabRunController implements Initializable {

  static final Logger logger = LoggerFactory.getLogger(RightTabRunController.class);
  public JFXComboBox environmentComboBox;
  public JFXComboBox unitComboBox;
  public JFXComboBox contractComboBox;
  public JFXComboBox<String> accountComboBox;
  public JFXTextField feeLimitTextField;
  public JFXTextField valueTextField;

  private static String DEFAULT_FEE_LIMIT = "1000000";
  private static String DEFAULT_VALUE = "0";

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

    unitComboBox.setItems(FXCollections.observableArrayList(
        "TRX",
        "SUN"
    ));
    unitComboBox.getSelectionModel().selectFirst();

    feeLimitTextField.setText(DEFAULT_FEE_LIMIT);
    valueTextField.setText(DEFAULT_VALUE);
    reloadContract();
  }

  private void reloadContract() {
    ShareData.currentContractName.addListener((observable, oldValue, newValue) -> {
      CompilationResult compilationResult = ShareData.getCompilationResult(newValue);
      List<String> contractNameList = new ArrayList<>();
      compilationResult.getContracts().forEach(contractResult -> {
        JSONObject metaData = JSON.parseObject(contractResult.metadata);
        JSONObject compilationTarget = metaData.getJSONObject("settings")
            .getJSONObject("compilationTarget");
        compilationTarget.forEach((sol, value) -> {
          contractNameList.add((String) value);
        });


      });
      contractComboBox.setItems(FXCollections.observableArrayList(
          contractNameList
      ));
      contractComboBox.getSelectionModel().selectFirst();
    });
  }

  public void onClickDeploy(ActionEvent actionEvent) {
//    ShareData.wallet.deployContract();
  }

  public void onClickLoad(ActionEvent actionEvent) {
  }

  public void onClickClear(ActionEvent actionEvent) {
  }

  public void onClickAddAddress(MouseEvent mouseEvent) {
    logger.debug("onClickAddAddress");
  }

  public void onClickCopyAddress(MouseEvent mouseEvent) {
  }
}