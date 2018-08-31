package org.tron.studio.ui;

import com.jfoenix.controls.JFXPopup;
import javafx.scene.input.KeyCode;
import org.fxmisc.richtext.CodeArea;

import org.fxmisc.richtext.PopupAlignment;
import javafx.scene.control.ListView;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.scene.input.KeyEvent;
import javafx.event.*;
import java.util.Map;
import java.util.HashMap;

public class AutoCompletion {
    final static int ROW_HEIGHT = 24;
    private static JFXPopup popup = new JFXPopup();
    private static ListView<String> list = new ListView<String>();
    private int startCol = 0;
    private int currentPara = 0;
    private int subStrSize = 0;
    private CodeArea textArea;

    Map dictionary = new HashMap();

    private static String[] keywords = {
            "class",
            "function",
            "int",
            "case",
            "contract",
            "while",
            "for"
    };

    public AutoCompletion(CodeArea textArea){
        this.textArea = textArea;

        dictionary.put("{", "\n}");
        dictionary.put("(", " )");
        dictionary.put("\"", "\"");

        extractAllwords(textArea,0,0);
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
                if (event.getCode() != KeyCode.ENTER)
                {
                    return;
                }

                String curentValue = list.getSelectionModel().getSelectedItem();
                String currCha = textArea.getText(currentPara, startCol-1,
                        currentPara, startCol);
                if (currCha.equals(" "))
                {
                    popup.hide();
                    return;
                }

                textArea.insertText(currentPara, startCol, curentValue.substring(subStrSize));
                popup.hide();
            }
        });

        list.setOnKeyTyped(new EventHandler<KeyEvent>(){
            @Override
            public void handle(KeyEvent event) {

            }
        });
    }

    public void autoComplete(CodeArea textArea)
    {
        textArea.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                extractAllwords(textArea,0,0);

                String subStr = "";
                for (int i = 1; i <= startCol; i++)
                {
                    String currCha = textArea.getText(currentPara, startCol - i,
                            currentPara, startCol - i + 1);
                    if (currCha.trim().length() == 0) {break;}
                    subStr += currCha;
                }
                subStr = new StringBuilder(subStr).reverse().toString();

                boolean hidePopup = true;
                if (subStr.length() != 0)
                {
                    ObservableList<String> candWords = FXCollections.observableArrayList();
                    for(String keyword: keywords)
                    {
                        if (keyword.startsWith(subStr) && keyword.length() != subStr.length())
                        {
                            candWords.add(keyword);
                        }
                    }
                    if (candWords.size() != 0)
                    {
                        hidePopup = false;
                        subStrSize = subStr.length();
                        setList(textArea, candWords);
                    }
                }

                if (hidePopup && popup.getPopupContent() != null)
                {
                    popup.hide();
                }
            }
        });
    }

    private void extractAllwords(CodeArea textArea, int startPos, int endPos)
    {
        String allText;
        if (endPos == 0)
        {
            allText = textArea.getText();
        } else
        {
            allText = textArea.getText(startPos, endPos);
        }

        allText = allText.replaceAll("[\t+\n+\\s+{}()]", " ");
        allText = allText.replaceAll("\\s+"," ");
        System.out.println(allText);
    }

    private void setList(CodeArea textArea, ObservableList<String> candwords) {
        list.setPrefHeight(candwords.size() * ROW_HEIGHT + 2);
        list.setItems(candwords);

        popup.setPopupContent(list);
        popup.show(textArea);
    }
}
