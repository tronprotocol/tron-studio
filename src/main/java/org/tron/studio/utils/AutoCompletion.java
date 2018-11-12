package org.tron.studio.utils;

import com.jfoenix.controls.JFXPopup;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.PopupAlignment;
import org.tron.studio.ShareData;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AutoCompletion {
    final static int ROW_HEIGHT = 24;
    private static final String PATH_TO_KEYWORDS = "/keywords/solidity.txt";
    private static JFXPopup popup = new JFXPopup();
    private static ListView<String> list = new ListView<String>();
    private static List<String> keywords = new ArrayList<>();
    private int startCol = 0;
    private int currentPara = 0;
    private int subStrSize = 0;
    private CodeArea textArea;

    public AutoCompletion(CodeArea textArea) {
        this.textArea = textArea;

        keywords = readKeywords();

        textArea.setPopupAlignment(PopupAlignment.SELECTION_BOTTOM_RIGHT);
        popup.setPopupContent(list);
        textArea.setPopupWindow(popup);

        this.textArea.caretColumnProperty().addListener((observable, oldValue, currentContractName) -> {
            startCol = textArea.getCaretColumn();
            currentPara = textArea.getCurrentParagraph();
        });

        list.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() != KeyCode.ENTER) {
                    return;
                }

                VirtualizedScrollPane virScrollPane = (VirtualizedScrollPane) ShareData.currentContractTab.getContent();
                CodeArea currentCodeArea = (CodeArea) virScrollPane.getContent();

                String curentValue = list.getSelectionModel().getSelectedItem();
                String currCha = "";
                if (startCol != 0) {
                    currCha = currentCodeArea.getText(currentPara, startCol - 1,
                            currentPara, startCol);
                }

                if (currCha.length() == 0 || currCha.equals(" ")) {
                    popup.hide();
                    return;
                }

                currentCodeArea.insertText(currentPara, startCol, curentValue.substring(subStrSize));
                popup.hide();
            }
        });
    }

    public void autoComplete(CodeArea textArea) {
        textArea.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (!ShareData.enableAutoComplete) return;
                String subStr = "";
                for (int i = 1; i <= startCol; i++) {
                    String currCha = textArea.getText(currentPara, startCol - i,
                            currentPara, startCol - i + 1);
                    if (currCha.trim().length() == 0) {
                        break;
                    }
                    subStr += currCha;
                }
                subStr = new StringBuilder(subStr).reverse().toString();

                boolean hidePopup = true;
                if (subStr.length() != 0) {
                    ObservableList<String> candWords = FXCollections.observableArrayList();
                    List<String> refWords = new ArrayList<>();
                    refWords.addAll(keywords);
                    refWords.addAll(extractRefWords(textArea));
                    for (String keyword : refWords) {
                        if (keyword.startsWith(subStr) && keyword.length() != subStr.length()) {
                            candWords.add(keyword);
                        }
                    }
                    if (candWords.size() != 0) {
                        hidePopup = false;
                        subStrSize = subStr.length();
                        setList(textArea, candWords);
                    }
                }

                if (hidePopup && popup.isShowing()) {
                    popup.hide();
                }
            }
        });
    }

    private List<String> extractRefWords(CodeArea textArea) {
        List<String> refWords = new ArrayList<>();
        boolean exitFun = false;
        boolean enterFun = false;
        boolean exitContract = false;

        for (int i = currentPara; i >= 0; i--) {
            String line = textArea.getText(i);
            line = line.trim();
            line = line.replaceAll("[\t+\n+\\s+{();]", " ");

            String[] words = line.split("\\s");
            for (String word : words) {
                if (word.trim().length() == 0) continue;
                if (!enterFun && exitFun && word.equals("}")) {
                    enterFun = true;
                    exitFun = false;
                }
                if (word.equals("function")) {
                    enterFun = false;
                    exitFun = true;
                }
                if (word.equals("contract")) {
                    exitContract = true;
                    break;
                }
                if (keywords.contains(word) || refWords.contains(word)) {
                    continue;
                }
                if (enterFun) {
                    break;
                }
                refWords.add(word);
            }

            if (exitContract) {
                break;
            }
        }

        return refWords;
    }

    private List<String> readKeywords() {
        Stream<String> lines = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream(PATH_TO_KEYWORDS))).lines();
        String[] words = lines.collect(Collectors.joining("|")).split("\\|");

        List<String> keywords = new ArrayList<>();
        for (String word : words) {
            keywords.add(word);
        }
        return keywords;
    }

    private void setList(CodeArea textArea, ObservableList<String> candwords) {
        list.setPrefHeight(candwords.size() * ROW_HEIGHT + 2);
        list.setItems(candwords);

        popup.setPopupContent(list);
        popup.show(textArea);
    }
}
