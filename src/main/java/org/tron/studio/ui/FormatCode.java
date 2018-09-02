package org.tron.studio.ui;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.Paragraph;
import org.reactfx.collection.LiveList;

public class FormatCode {

    CodeArea codeArea = new CodeArea();
    private static int tabWidth = 2;
    private int startCol = 0;
    private int currentPara = 0;
    private boolean releasedEnterKey = false;

    public FormatCode(CodeArea codeArea)
    {
        this.codeArea = codeArea;
    }

    public void formatAllCode()
    {
        this.codeArea.caretColumnProperty().addListener((observable, oldValue, currentContractName) -> {
            startCol = codeArea.getCaretColumn();
            currentPara = codeArea.getCurrentParagraph();
            if (releasedEnterKey)
            {
                releasedEnterKey = false;
                autoindent();
            }
        });

        codeArea.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                if (e.getCode() == KeyCode.TAB) {
                    String s = StringUtils.repeat(' ', tabWidth);
                    codeArea.insertText(codeArea.getCaretPosition(), s);
                    e.consume();
                } else if (e.getCode() == KeyCode.ENTER)
                {
                    releasedEnterKey = true;
                }
            }
        });

        correctIndent();
    }

    public void correctIndent()
    {
        calIndentLevel(codeArea.getParagraphs().size(), true);
    }

    private int calIndentLevel(int endPara, boolean correctIndent)
    {
        int indentLevel = 0;

        for (int i = 0; i < endPara; i++)
        {
            String currentLine = codeArea.getText(i);
            int lineLength = currentLine.length();
            // intentLevel+1 when entering a block or new line in a sentence
            currentLine = currentLine.trim();

            // merge multi-spaces into one space
            currentLine = currentLine.replaceAll("( +)"," ");

            if (currentLine.endsWith("}"))
            {
                indentLevel -= 1;
            }

            if (correctIndent)
            {
                currentLine = StringUtils.repeat(' ', tabWidth*indentLevel) + currentLine;
                codeArea.replaceText(i,0,i,lineLength,currentLine);
            }

            if (currentLine.endsWith("{"))
            {
                indentLevel += 1;
            }
        }
        return indentLevel;
    }

    public void autoindent()
    {
        // auto intent
        // Get last character in previous line
        int currentIndent = calIndentLevel(currentPara, false);
        String currLine = codeArea.getText(currentPara);
        int currLineLength = currLine.length();
        currLine = currLine.trim();

        if (currLine.equals("}"))
        {
            currentIndent -= 1;
        }

        if (currLine.length() > 0)
        {
            currLine = StringUtils.repeat(' ', tabWidth * currentIndent) + currLine;
            codeArea.replaceText(currentPara,0, currentPara, currLineLength, currLine);
        } else {
            String indentStr = StringUtils.repeat(' ', tabWidth * currentIndent);
            codeArea.insertText(currentPara,0,indentStr);
        }
    }
}
