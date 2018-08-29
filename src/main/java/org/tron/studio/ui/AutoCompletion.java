package org.tron.studio.ui;

import com.jfoenix.controls.JFXPopup;
import javafx.scene.control.Label;
import javafx.stage.Popup;
import org.fxmisc.richtext.CodeArea;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

import javafx.event.EventHandler;
import org.fxmisc.richtext.PopupAlignment;
import javafx.scene.control.ListView;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class AutoCompletion {
    final static int ROW_HEIGHT = 24;
    private static JFXPopup popup = new JFXPopup();
    private static boolean isPopupshowing = false;

    private static String[] keywords = {
            "class",
            "function",
            "int",
            "case",
            "contract",
            "while",
            "for"
    };

    public static void autoComplete(CodeArea textArea)
    {
        textArea.caretColumnProperty().addListener((observable, oldValue, newValue) -> {
            int caretCol = textArea.getCaretColumn();
            int currPara = textArea.getCurrentParagraph();
            int currPos = textArea.getCaretPosition();

            String subStr = "";
            for (int i = 1; i <= caretCol; i++)
            {
                String currCha = textArea.getText(currPara, caretCol - i,
                        currPara, caretCol - i + 1);
                if (currCha.equals(" ")) {break;}
                subStr += currCha;
            }
            subStr = new StringBuilder(subStr).reverse().toString();
            int startCol = caretCol - subStr.length();

            System.out.println(String.format("substr: %s", subStr));

            if (subStr.length() != 0)
            {
                ObservableList<String> candWords = FXCollections.observableArrayList();
                for(String keyword: keywords)
                {
                    if (keyword.startsWith(subStr) && !keyword.equals(subStr))
                    {
                        candWords.add(keyword);
                    }
                }
                System.out.println(String.format("cand: %s", candWords));
                if (candWords.size() != 0)
                {
                    setList(textArea, candWords, currPara, startCol);
                }
            }
        });
    }

    private static void setList(CodeArea textArea, ObservableList<String> candwords,
                                int currentPara, int startCol) {
        ListView<String> list = new ListView<String>();
        list.setPrefHeight(candwords.size() * ROW_HEIGHT + 2);
        list.setItems(candwords);

        list.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                // Your action here
                System.out.println(String.format("start: %d end: %d", startCol, startCol+newValue.length()));
                textArea.replaceText(currentPara, startCol, currentPara,
                        startCol+newValue.length(), newValue);
                System.out.println("complete");
            }
        });

        if (isPopupshowing)
        {
            textArea.clear();
        }

        popup.setPopupContent(list);
        textArea.setPopupWindow(popup);
        textArea.setPopupAlignment(PopupAlignment.SELECTION_BOTTOM_RIGHT);
        popup.show(textArea);
    }
}
