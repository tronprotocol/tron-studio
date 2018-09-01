package org.tron.studio.ui;

import com.alibaba.fastjson.JSON;
import com.google.gson.JsonObject;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXRippler;
import com.jfoenix.controls.JFXTextField;
import com.sun.javafx.tools.packager.PackagerException;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.tron.common.runtime.vm.trace.Op;
import org.tron.common.runtime.vm.trace.ProgramTrace;
import org.tron.studio.ShareData;
import org.tron.studio.debug.VMStatus;
import org.tron.studio.filesystem.VmTraceFileUtil;

import java.io.File;
import java.net.URL;
import java.nio.file.attribute.FileTime;
import java.util.*;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.Files;

@Slf4j
public class RightTabDebugController implements Initializable {

    public JFXListView debugList;
    public JFXTextField transactionIdTextField;

    private List<String> instructionsInfo = new ArrayList<>();
    private List<String> solidityLocals = new ArrayList<>();
    private List<Node> solidityState = new ArrayList<>();
    private List<String> stepDetails = new ArrayList<>();
    private List<String> stacks = new ArrayList<>();
    private List<String> storageLoaded = new ArrayList<>();
    private List<String> memory = new ArrayList<>();
    private List<String> callData = new ArrayList<>();
    private List<String> callStack = new ArrayList<>();
    private List<String> returnValue = new ArrayList<>();
    private List<String> fullStorageChanges = new ArrayList<>();

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
            VMStatus vmStatus = new VMStatus();
            for (Op op : programTrace.getOps()) {
                vmStatus.addStatus(new VMStatus.StatusItem(op.getCode(), op.getDeep(), op.getPc(), op.getEnergy(), op.getActions()));
                logger.error(op.toString());
            }

            transactionIdTextField.setText(transactionId);
            String[] nodes = {"Instructions", "Solidity Locals",
                    "Solidity State", "Step detail", "Stack",
                    "Storage completely loaded", "Memory", "Call Data", "Call Stack",
                    "Return Value", "Full Storages Changes"};

            getTransDetails(transactionId);

            Map<String, Object> details = new HashMap<>();
            details.put("Instructions", instructionsInfo);
            details.put("Solidity Locals", solidityLocals);
            details.put("Solidity State", solidityState);
            details.put("Step detail", stepDetails);
            details.put("Stack", stacks);
            details.put("Storage completely loaded", storageLoaded);
            details.put("Memory", memory);
            details.put("Call Data", callData);
            details.put("Call Stack", callStack);
            details.put("Return Value", returnValue);
            details.put("Full Storages Changes", fullStorageChanges);

            debugList.getItems().clear();

            for (String nodename : nodes) {
                JFXListView<Object> subList = createList(nodename, details.get(nodename));
                debugList.getItems().add(subList);
            }
        });
    }

    private void getTransDetails(String transId)
    {
        List<File> files = new ArrayList<>();
        for (File file: VmTraceFileUtil.getFileNameList())
        {
            if (file.getName().contains(transId))
                files.add(file);
        }

        Long lastestTime = (long)0;
        File lastestFile = null;
        try {

            for (File file: files)
            {
                BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                Long fileTime = attr.creationTime().toMillis();
                if (fileTime > lastestTime)
                {
                    lastestTime = fileTime;
                    lastestFile = file;
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        if (lastestFile == null) return;

        try {
            String transContenxt = VmTraceFileUtil.getTraceContent(lastestFile.getName());
            JSONParser  parser = new JSONParser();
            JSONObject jsonObject = (JSONObject)parser.parse(transContenxt);
            JSONArray opsArray = (JSONArray)jsonObject.get("ops");

            for (Object ops: opsArray)
            {
                JSONObject opsJson = (JSONObject)ops;
                instructionsInfo.add(String.format("%04d %s", opsJson.get("pc"), opsJson.get("code")));
            }

        } catch (Exception e)
        {
            e.printStackTrace();
        }

        solidityState = createSolidityStateNode();

    }

    private List<Node> createSolidityStateNode()
    {
        List<Node> details = new ArrayList<>();
        details.add(new Label("chairperson:"));
        details.add(new Label("0x0000000000000000000"));

        String voterStr = "voters: mapping(address => struct Ballot.Voter)";
        List<Object> voterDetail = new ArrayList<>();
        voterDetail.add("nothing");
        JFXListView<Object> voter = createSolidityStateSubNode(voterStr, voterDetail);

        details.add(voter);

        String proposalStr = "proposals: struct Ballot.Proposal[]";
        List<Object> proposalDetail = new ArrayList<>();
        voterDetail.add("length: 0");
        JFXListView<Object> proposals = createSolidityStateSubNode(proposalStr, proposalDetail);

        details.add(proposals);

        return details;
    }

    private JFXListView<Object> createSolidityStateSubNode(String nodename, List<Object> details)
    {
        JFXListView<Object> subList = new JFXListView<>();

        for(Object detail: details)
        {
            subList.getItems().add(detail);
        }

        Node node = new HBox();
        ((HBox) node).getChildren().add(new Label(nodename));
        JFXRippler ripper = new JFXRippler();
        ripper.setStyle(":cons-rippler1");
        ripper.setPosition(JFXRippler.RipplerPos.BACK);

        ((HBox) node).getChildren().add(ripper);
        subList.setGroupnode(node);

        return subList;
    }

    public void onClickDebug(ActionEvent actionEvent) {
    }

    private JFXListView<Object> createList(String groundName, Object detail) {

        List<Object> nodeDetails = (List<Object>)detail;

        if (nodeDetails.size() == 0)
        {
            nodeDetails.add("nothing");
        }

        JFXListView<Object> subList = new JFXListView<>();

        for (Object labelText :
                nodeDetails) {
            if (labelText instanceof String)
                subList.getItems().add(new Label((String) labelText));
            else if(labelText instanceof Node)
                subList.getItems().add(labelText);
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