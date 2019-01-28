package org.tron.studio.utils;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.richtext.CodeArea;
import org.tron.studio.ShareData;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FormatCode {

    private static final String PATH_TO_KEYWORDS = "/keywords/solidity.txt";
    private static int tabWidth = 2;
    private static List<String> keywords = new ArrayList<>();
    CodeArea codeArea;
    private int startCol = 0;
    private int currentPara = 0;
    private boolean releasedEnterKey = false;

    public FormatCode(CodeArea codeArea) {
        this.codeArea = codeArea;
        keywords = readKeywords();

        formatAllCode();
        //spellCheckerAllContent(codeArea.getParagraphs().size());
    }

    public void formatAllCode() {

        this.codeArea.caretColumnProperty().addListener((observable, oldValue, currentContractName) -> {
            startCol = codeArea.getCaretColumn();
            currentPara = codeArea.getCurrentParagraph();
            if (releasedEnterKey) {
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
                } else if (e.getCode() == KeyCode.ENTER) {
                    releasedEnterKey = true;
                }

                // Check spell
                // spellCheckerAllContent(codeArea.getParagraphs().size());

            }
        });

        correctIndent();
    }

    public void correctIndent() {
        calIndentLevel(codeArea.getParagraphs().size(), true);
    }

    private int calIndentLevel(int endPara, boolean correctIndent) {
        int indentLevel = 0;

        for (int i = 0; i < endPara; i++) {
            String currentLine = codeArea.getText(i);
            int lineLength = currentLine.length();
            // intentLevel+1 when entering a block or new line in a sentence
            currentLine = currentLine.trim();

            // merge multi-spaces into one space
            currentLine = currentLine.replaceAll("( +)", " ");

            if (currentLine.endsWith("}")) {
                indentLevel -= 1;
            }

            if (correctIndent) {
                currentLine = StringUtils.repeat(' ', tabWidth * indentLevel) + currentLine;
                codeArea.replaceText(i, 0, i, lineLength, currentLine);
            }

            if (currentLine.endsWith("{")) {
                indentLevel += 1;
            }
        }
        return indentLevel;
    }

    public void autoindent() {
        // auto intent
        // Get last character in previous line
        int currentIndent = calIndentLevel(currentPara, false);
        String currLine = codeArea.getText(currentPara);
        int currLineLength = currLine.length();
        currLine = currLine.trim();

        if (currLine.equals("}")) {
            currentIndent -= 1;
        }

        if (currLine.length() > 0) {
            currLine = StringUtils.repeat(' ', tabWidth * currentIndent) + currLine;
            codeArea.replaceText(currentPara, 0, currentPara, currLineLength, currLine);
        } else {
            String indentStr = StringUtils.repeat(' ', tabWidth * currentIndent);
            codeArea.insertText(currentPara, 0, indentStr);
        }
    }

    public void spellCheckerAllContent(int endParaNo) {
        //int paraSize = codeArea.getParagraphs().size();

        // Clear error list
        ShareData.missInfoList.clear();

        List<String> varContract = new ArrayList<>();
        List<String> varFunc = new ArrayList<>();
        int bracketsNumContract = 0;
        int bracketsNumFunc = 0;
        boolean inContract = false;
        boolean inFunc = false;

        boolean inCommont = false;
        int commentStartPara = 0;
        int commentStartIndex = 0;
        String commentStartLine = "";
        String commentStartWord = "";

        for (int i = 0; i < endParaNo; i++) {
            // check each line
            String currentLine = codeArea.getText(i);
            String[] words = regulizeLine(currentLine).split(" ");

            if (words.length == 0) continue;
            if (words[0].startsWith("//") || words[0].equals("pragma")) continue;

            // check contract
            if (words[0].equals("contract")) inContract = true;
            if (inContract) {
                if (currentLine.contains("{")) bracketsNumContract += 1;
                if (currentLine.contains("}")) {
                    bracketsNumContract -= 1;
                    if (bracketsNumContract == 0) {
                        inContract = false;
                        varContract.clear();
                    }
                }
            }

            // check function
            if (words[0].equals("function")) inFunc = true;
            if (inFunc) {
                if (currentLine.contains("{")) bracketsNumFunc += 1;
                if (currentLine.contains("}")) {
                    bracketsNumFunc -= 1;
                    if (bracketsNumFunc == 0) {
                        inFunc = false;
                        varFunc.clear();
                    }
                }
            }

            String preWord = null;
            boolean interuptFlg = false;
            for (String word : words) {
                if (word.endsWith("*/") && inCommont) {
                    inCommont = false;
                }

                if (word.startsWith("/*")) {
                    inCommont = true;
                    commentStartIndex = currentLine.indexOf(word);
                    commentStartPara = i;
                    commentStartWord = word;
                    commentStartLine = currentLine;
                    break;
                }

                if (word.startsWith("//") || inCommont) continue;

                if (StringUtils.isNumeric(word)) continue;
                word = word.replaceAll("[*/]+", "");

                if (!word.matches("[A-Za-z0-9]+")) {
                    interuptFlg = true;
                    continue;
                }

                if (preWord == null) {

                    if (!keywords.contains(word)
                            && (inContract && !inFunc && !varContract.contains(word)
                            || inFunc && !varFunc.contains(word) && !varContract.contains(word))) {
                        // spell error
                        int startIndex = currentLine.indexOf(word);
                        setErrorStyle(word, i, startIndex);
                        findSpellErrorInLine(word, currentLine, startIndex, i);
                    } else {
                        preWord = word;
                    }
                    continue;
                }

                if (keywords.contains(preWord) && !keywords.contains(word)) {
                    if (inContract && !inFunc && varContract.contains(word)
                            || inFunc && varFunc.contains(word)) {
                        // spell error
                        int startIndex = currentLine.indexOf(word);
                        setErrorStyle(word, i, startIndex);
                        findSpellErrorInLine(word, currentLine, startIndex, i);
                    } else if (inContract && !inFunc || preWord.equals("function"))
                        varContract.add(word);
                    else
                        varFunc.add(word);
                }

                if (!keywords.contains(preWord) && !varContract.contains(word)
                        && !varFunc.contains(word) && !interuptFlg) {
                    // spell error
                    int startIndex = currentLine.indexOf(word);
                    setErrorStyle(word, i, startIndex);
                    findSpellErrorInLine(word, currentLine, startIndex, i);
                }

                if (interuptFlg) interuptFlg = false;

                preWord = word;
            }
        }

        if (inCommont) {
            setErrorStyle(commentStartWord, commentStartPara, commentStartIndex);
            findSpellErrorInLine(commentStartWord, commentStartLine, commentStartIndex, commentStartPara);
        }
    }

    private void findSpellErrorInLine(String word, String strLine, int startIndex, int curPara) {
        int endInd = strLine.length() - word.length() + 1;
        for (int m = startIndex + word.length(); m < endInd; m++) {
            String curCha = strLine.substring(m, m + word.length());
            String preCha = strLine.substring(m - 1, m - 1);
            String nextCha = "";
            if (m + word.length() + 1 <= strLine.length())
                nextCha = strLine.substring(m + word.length() + 1, m + word.length() + 1);

            if (curCha.equals(word) && !preCha.matches("[A-Za-z0-9]")
                    && !nextCha.matches("[A-Za-z0-9]")) {
                setErrorStyle(word, curPara, m);
            }
        }
    }

    private String regulizeLine(String str) {
        str = str.replaceAll("\\+|-|=|&|\\|", " ");
        str = str.replaceAll("\\{", " { ");
        str = str.replaceAll("}", " } ");
        str = str.replaceAll("\\(", " ( ");
        str = str.replaceAll("\\)", " ) ");
        str = str.replaceAll(",", " , ");
        str = str.replaceAll(";", " ; ");

        return str.replaceAll("( +)", " ").trim();
    }

    private void setErrorStyle(String missWord, int paraNo, int startNo) {

        MissInfo missInfo = new MissInfo();
        missInfo.missWord = missWord;
        missInfo.paraNo = paraNo;
        missInfo.startNo = startNo;

        ShareData.missInfoList.add(missInfo);
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

    public class MissInfo {
        public String missWord;
        public int paraNo;
        public int startNo;
    }
}
