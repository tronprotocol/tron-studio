package org.tron.studio.ui;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.richtext.CodeArea;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FormatCode {

    CodeArea codeArea;
    private static int tabWidth = 2;
    private int startCol = 0;
    private int currentPara = 0;
    private boolean releasedEnterKey = false;

    private static final String PATH_TO_KEYWORDS  = "/keywords/solidity.txt";

    private static List<String> keywords = new ArrayList<>();

    public FormatCode(CodeArea codeArea)
    {
        this.codeArea = codeArea;
        keywords = readKeywords();
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

    public  void spellCheckerAllContent()
    {
        int paraSize = codeArea.getParagraphs().size();
        List<String> varContract = new ArrayList<>();
        List<String> varFunc = new ArrayList<>();
        int bracketsNumContract = 0;
        int bracketsNumFunc = 0;
        boolean inContract = false;
        boolean inFunc = false;

        List<String> globalVariables = new ArrayList<>();
        List<String> localVariables = new ArrayList<>();

        for (int i = 0; i < paraSize; i++)
        {
            // check each line
            String currentLine = codeArea.getText(i);
            String orgLine = currentLine;

            currentLine = currentLine.replaceAll("\\{"," { ");
            currentLine = currentLine.replaceAll("}"," } ");
            currentLine = currentLine.replaceAll("\\("," ( ");
            currentLine = currentLine.replaceAll("\\)"," ) ");
            currentLine = currentLine.replaceAll(","," , ");
            currentLine = currentLine.replaceAll(";"," ; ");

            currentLine = currentLine.replaceAll("( +)"," ").trim();

            String[] words = currentLine.split(" ");

            if ( words.length == 0 ) continue;

            // check contract
            if (words[0].equals("contract")) inContract = true;
            if (inContract)
            {
                if (currentLine.contains("{")) bracketsNumContract += 1;
                if (currentLine.contains("}")) bracketsNumContract -= 1;
            }
            if(bracketsNumContract == 0)
            {
                inContract = false;
                globalVariables.clear();
            }

            // check function
            if (words[0].equals("function")) inFunc = true;
            if (inFunc)
            {
                if (currentLine.contains("{")) bracketsNumFunc += 1;
                if (currentLine.contains("}")) bracketsNumFunc -= 1;
            }
            if (bracketsNumFunc == 0)
            {
                inFunc = false;
                varFunc.clear();
            }

            String preWord = null;
            String currWord = null;
            for (String word: words)
            {
                if (preWord == null) {
                    preWord = word;
                    continue;
                }

                if (keywords.contains(preWord) && !keywords.contains(word))
                {
                    if (inContract && !inFunc)
                        varContract.add(word);
                    else
                        varFunc.add(word);
                    continue;
                }

                if (!keywords.contains(preWord) && !varContract.contains(word)
                        && !varFunc.contains(word))
                {
                    // spell error
                    int startIndex = orgLine.indexOf(word);
                    codeArea.setStyleClass(startIndex, startIndex+word.length(),
                            "spell-error");
                }
            }
        }
    }

    private List<String> readKeywords()
    {
        Stream<String> lines = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream(PATH_TO_KEYWORDS))).lines();
        String[] words = lines.collect(Collectors.joining("|")).split("\\|");

        List<String> keywords = new ArrayList<>();
        for (String word: words)
        {
            keywords.add(word);
        }
        return keywords;
    }
}
