package org.tron.studio.ui;

import com.alibaba.fastjson.JSON;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXRippler;
import com.jfoenix.controls.JFXTextField;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import lombok.extern.slf4j.Slf4j;
import org.tron.common.runtime.vm.trace.Op;
import org.tron.common.runtime.vm.trace.ProgramTrace;
import org.tron.studio.ShareData;
import org.tron.studio.filesystem.VmTraceFileUtil;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

@Slf4j
public class RightTabDebugController implements Initializable {

    public JFXListView debugList;
    public JFXTextField transactionIdTextField;

    public RightTabDebugController() {

    }

    public void initialize(URL location, ResourceBundle resources) {
        ShareData.debugTransactionAction.addListener((observable, oldValue, transactionId) -> {

            List<File> traceFileList = VmTraceFileUtil.getFileNameList();
            Optional<File> traceFile = traceFileList.stream().filter(file -> file.getName().startsWith(transactionId)).findFirst();
            if (!traceFile.isPresent()) {
               return;
            }
            String traceContent = VmTraceFileUtil.getTraceContent(traceFile.get().getName());
            ProgramTrace programTrace = JSON.parseObject(traceContent, ProgramTrace.class);
            for (Op op : programTrace.getOps()) {
                logger.error(op.toString());
            }

            transactionIdTextField.setText(transactionId);
            String[] nodes = {"Instructions", "Solidity Locals",
                    "Solidity State", "Step detail", "Stack",
                    "Storage completely loaded", "Memory", "Call Data", "Call Stack",
                    "Return Value", "Full Storages Changes"};

            String[] labels = {"test"};

            debugList.getItems().clear();
            for (String nodename : nodes) {
                JFXListView<Object> subList = createList(labels, nodename);
                debugList.getItems().add(subList);
            }
        });
    }

    public void onClickDebug(ActionEvent actionEvent) {
    }

    private JFXListView<Object> createList(String[] labels, String groundName) {
        JFXListView<Object> subList = new JFXListView<>();

        for (String labelText :
                labels) {
            subList.getItems().add(new Label(labelText));
        }

        Node node = new HBox();
        ((HBox) node).getChildren().add(new Label(groundName));
        JFXRippler ripper = new JFXRippler();
        ripper.setStyle(":cons-rippler1");
        ripper.setPosition(JFXRippler.RipplerPos.BACK);

        StackPane pane = new StackPane();
        pane.setStyle(":-fx-padding: 2;");

        MaterialDesignIconView copyIcon = new MaterialDesignIconView();
        copyIcon.setGlyphName("CONTENT_COPY");
        copyIcon.setStyleClass("icon");
        pane.getChildren().add(copyIcon);

        ripper.getChildren().add(pane);

        ((HBox) node).getChildren().add(ripper);
        subList.setGroupnode(node);

        return subList;
    }

    public void onClickPlay(MouseEvent mouseEvent) {

    }

    public void onClickStop(MouseEvent mouseEvent) {
    }

    public void onClickStepOverBack(MouseEvent mouseEvent) {
    }

    public void onClickStepBack(MouseEvent mouseEvent) {
    }

    public void onClickStepInto(MouseEvent mouseEvent) {
    }

    public void onClickOverForward(MouseEvent mouseEvent) {
    }
}