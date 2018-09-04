package org.tron.studio.ui;

import com.alibaba.fastjson.JSON;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import lombok.extern.slf4j.Slf4j;
import org.tron.common.runtime.vm.DataWord;
import org.tron.common.runtime.vm.trace.Op;
import org.tron.common.runtime.vm.trace.ProgramTrace;
import org.tron.studio.ShareData;
import org.tron.studio.debug.VMStatus;
import org.tron.studio.filesystem.VmTraceFileUtil;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Slf4j
public class RightTabDebugController implements Initializable {

    public JFXListView debugInstructionsList;
    public JFXListView debugStackList;
    public JFXTextField transactionIdTextField;

    private List<String> instructionsInfo = new ArrayList<>();

    private int currentIndex = 0;
    private VMStatus vmStatus;

    public void initialize(URL location, ResourceBundle resources) {
        ShareData.debugTransactionAction.addListener((observable, oldValue, transactionId) -> {
            List<File> traceFileList = VmTraceFileUtil.getFileNameList();
            Optional<File> traceFile = traceFileList.stream().filter(file -> file.getName().startsWith(transactionId)).findFirst();
            if (!traceFile.isPresent()) {
                return;
            }
            String traceContent = VmTraceFileUtil.getTraceContent(traceFile.get().getName());
            ProgramTrace programTrace = JSON.parseObject(traceContent, ProgramTrace.class);
            String contractAddress = programTrace.getContractAddress();
            vmStatus = new VMStatus();
            for (Op op : programTrace.getOps()) {
                vmStatus.addStatus(new VMStatus.StatusItem(contractAddress, op.getCode(), op.getDeep(), op.getPc(), op.getEnergy(), op.getActions()));
                logger.error(op.toString());
                instructionsInfo.add("" + op.getPc() + ": " + op.getCode().name());
            }

            currentIndex = 0;
            transactionIdTextField.setText(transactionId);
            debugStackList.getItems().clear();
            debugInstructionsList.getItems().clear();
            debugInstructionsList.getItems().addAll(instructionsInfo);
        });
    }

    public void onClickPlay(MouseEvent mouseEvent) {
        currentIndex = 0;
        List<String> stacks = new ArrayList<>();
        VMStatus.StatusItem statusItem = vmStatus.getStatusItem(currentIndex);
        List<DataWord> items = statusItem.stack.stream().collect(Collectors.toList());
        for (int i = items.size() - 1; i >= 0; i--) {
            stacks.add("" + i + ": " + items.get(i).toString());
        }
        debugStackList.getItems().clear();
        debugStackList.getItems().addAll(stacks);
        debugStackList.setPrefHeight(stacks.size() * 40);

        debugInstructionsList.scrollTo("" + statusItem.pc + ": " + statusItem.code.name());
    }

    public void onClickStop(MouseEvent mouseEvent) {
        currentIndex = 0;
        debugStackList.getItems().clear();
    }

    public void onClickBackward(MouseEvent mouseEvent) {
        List<String> stacks = new ArrayList<>();
        currentIndex--;
        if (currentIndex < 0) {
            currentIndex = 0;
        }
        VMStatus.StatusItem statusItem = vmStatus.getStatusItem(currentIndex);
        List<DataWord> items = statusItem.stack.stream().collect(Collectors.toList());
        for (int i = items.size() - 1; i >= 0; i--) {
            stacks.add("" + i + ": " + items.get(i).toString());
        }
        debugStackList.getItems().clear();
        debugStackList.getItems().addAll(stacks);
        debugStackList.setPrefHeight(stacks.size() * 40);

        debugInstructionsList.scrollTo("" + statusItem.pc + ": " + statusItem.code.name());
    }

    public void onClickForward(MouseEvent mouseEvent) {
        List<String> stacks = new ArrayList<>();
        currentIndex++;
        if (currentIndex >= vmStatus.getStatusItemSize()) {
            currentIndex = vmStatus.getStatusItemSize() - 1;
        }
        VMStatus.StatusItem statusItem = vmStatus.getStatusItem(currentIndex);
        List<DataWord> items = statusItem.stack.stream().collect(Collectors.toList());
        for (int i = items.size() - 1; i >= 0; i--) {
            stacks.add("" + i + ": " + items.get(i).toString());
        }
        debugStackList.getItems().clear();
        debugStackList.getItems().addAll(stacks);
        debugStackList.setPrefHeight(stacks.size() * 40);

        debugInstructionsList.scrollTo("" + statusItem.pc + ": " + statusItem.code.name());
    }
}