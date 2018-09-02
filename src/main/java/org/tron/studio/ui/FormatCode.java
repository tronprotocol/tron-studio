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
                autointent("");
                releasedEnterKey = false;
            }
        });

        codeArea.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                System.out.println(e.getCode());
                if (e.getCode() == KeyCode.TAB) {
                    String s = StringUtils.repeat(' ', tabWidth);
                    codeArea.insertText(codeArea.getCaretPosition(), s);
                    e.consume();
                } else if (e.getCode() == KeyCode.ENTER)
                {
                    releasedEnterKey = true;
                } else if (e.getCode() == KeyCode.OPEN_BRACKET)
                {
                    // auto complete }
                    /*
                    int currentIntentLevel = calIntentLevel(currentPara, false);
                    String intentStr = StringUtils.repeat(' ',
                            tabWidth * (currentIntentLevel - 1));
                    intentStr = "\n" + intentStr + "}";
                    codeArea.insertText(currentPara,codeArea.getCaretColumn()+1, intentStr);
                    */
                }
            }
        });

        correctIntent();
    }

    public void correctIntent()
    {
        calIntentLevel(codeArea.getParagraphs().size(), true);
    }

    private int calIntentLevel(int endPara, boolean correctIntent)
    {
        int intentLevel = 0;

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
                intentLevel -= 1;
            }

            if (correctIntent)
            {
                currentLine = StringUtils.repeat(' ', tabWidth*intentLevel) + currentLine;
                codeArea.replaceText(i,0,i,lineLength,currentLine);
            }

            if (currentLine.endsWith("{"))
            {
                intentLevel += 1;
            }
        }
        return intentLevel;
    }

    public void autointent(String str)
    {
        // auto intent
        // Get last character in previous line
        int currentIntent = calIntentLevel(currentPara, false);

        String intentStr = StringUtils.repeat(' ', tabWidth * currentIntent);
        codeArea.insertText(currentPara,0,intentStr + str);
    }
}
