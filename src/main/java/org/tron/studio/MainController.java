package org.tron.studio;

import com.google.rpc.Code;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.Subscription;
import org.tron.studio.filesystem.SolidityFileUtil;
import org.tron.studio.ui.CodeParserUtil;
import org.tron.studio.ui.SolidityHighlight;
import org.tron.studio.ui.AutoCompletion;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.BreakIterator;
import java.time.Duration;
import java.util.*;

@Slf4j
public class MainController {
    public TabPane rightContentTabPane;
    public TabPane codeAreaTabPane;

    public Tab defaultCodeAreaTab;
    public CodeArea defaultCodeArea;


    private int contractFileNum = 0;
    private String previousValue = "";

    private static final Set<String> dictionary = new HashSet<String>();

    @PostConstruct
    public void initialize() throws IOException {
        List<File> files = SolidityFileUtil.getFileNameList();
        File defaultContractFile = files.get(0);
        ShareData.currentContractFileName.set(defaultContractFile.getName());

        StringBuilder builder = new StringBuilder();
        try {
            Files.lines(Paths.get(defaultContractFile.getAbsolutePath())).forEach(line -> {
                builder.append(line).append(System.getProperty("line.separator"));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        contractFileNum = ShareData.allContractFileName.size();

        defaultCodeAreaTab.setText(defaultContractFile.getName());
        //Just not allow to close the default tab
        defaultCodeAreaTab.setClosable(false);

        defaultCodeArea = (CodeArea) defaultCodeAreaTab.getContent();
        new SolidityHighlight(defaultCodeArea).highlight();
        //spellChecking(defaultCodeArea);
        AutoCompletion autocomp = new AutoCompletion(defaultCodeArea);
        autocomp.autoComplete(defaultCodeArea);

        defaultCodeArea.replaceText(0, 0, builder.toString());

        defaultCodeArea.setParagraphGraphicFactory(LineNumberFactory.get(defaultCodeArea));

        previousValue = ShareData.currentContractName.get();

        ShareData.currentContractTab = defaultCodeAreaTab;

        SingleSelectionModel<Tab> codeAreaTabPaneSelectionModel = codeAreaTabPane.getSelectionModel();

        ShareData.deleteContract.addListener((observable, oldValue, currentContractName) -> {
            for (Tab tab : codeAreaTabPane.getTabs()) {
                if (StringUtils.equals(tab.getText(), currentContractName)) {
                    codeAreaTabPane.getTabs().remove(tab);
                    break;
                }
            }
        });

        ShareData.currentContractFileName.addListener((observable, oldValue, currentContractName) -> {
            boolean alreadyOpen = false;
            for (Tab tab : codeAreaTabPane.getTabs()) {
                if (StringUtils.equals(tab.getText(), currentContractName)) {
                    codeAreaTabPaneSelectionModel.select(tab);
                    alreadyOpen = true;
                }
            }
            if (!alreadyOpen) {
                createTabForFileSystemFile(currentContractName);
            }
            previousValue = currentContractName;
        });

        ShareData.newContractFileName.addListener((observable, oldValue, newValue) -> {
            createTabForFileSystemFile(newValue);
        });

        ShareData.openContractFileName.addListener((observable, oldValue, newValue) ->{
            String filePath = ShareData.openContractFileName.get();
            File newFile = new File(filePath);

            Tab newTab = setTab(newFile);
            ShareData.currentContractName.set(newFile.getName());
            ShareData.allContractFileName.add(newFile.getName());
            codeAreaTabPaneSelectionModel.select(newTab);
        });

        ShareData.debugTransactionAction.addListener((observable, oldValue, newValue) -> {
            rightContentTabPane.getSelectionModel().selectLast();
        });
    }

    private Tab setTab(File file) {
        Tab tab = new Tab();
        logger.info("set tab");
        CodeArea codeArea = new CodeArea();
        // Print new file in codearea
        StringBuilder builder = new StringBuilder();
        try {
            Files.lines(Paths.get(file.getAbsolutePath())).forEach(line -> {
                builder.append(line).append(System.getProperty("line.separator"));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        AutoCompletion autoCompletion = new AutoCompletion(codeArea);
        autoCompletion.autoComplete(codeArea);
        tab.setText(file.getName());
        //Just not allow to close the default tab
        tab.setClosable(true);
        tab.setContent(codeArea);
        codeAreaTabPane.getTabs().add(tab);

        new SolidityHighlight(codeArea).highlight();
        codeArea.replaceText(0, 0, builder.toString());
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

        ShareData.allContractFileName.add(file.getName());
        ShareData.currentContractName.set(file.getName());
        ShareData.currentContractTab = tab;

        return tab;
    }

    private void createTabForFileSystemFile(String fileName) {
        try {
            Tab codeTab = new Tab();
            codeTab.setText(fileName);
            codeTab.setClosable(true);
            CodeArea codeArea = FXMLLoader.load(getClass().getResource("ui/code_area.fxml"));

            AutoCompletion autoCompletion = new AutoCompletion(codeArea);
            autoCompletion.autoComplete(codeArea);
            codeTab.setContent(codeArea);
            codeAreaTabPane.getTabs().add(codeTab);

            String sourceCode = SolidityFileUtil.getSourceCode(fileName);

            new SolidityHighlight(codeArea).highlight();
            codeArea.insertText(0, sourceCode);
            codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

            codeAreaTabPane.getSelectionModel().select(codeTab);
            ShareData.currentContractTab = codeTab;
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void spellChecking(CodeArea textArea)
    {
        Subscription cleanupWhenFinished = textArea.plainTextChanges()
                .successionEnds(Duration.ofMillis(500))
                .subscribe(change -> {
                    textArea.setStyleSpans(0, computeHighlighting(textArea.getText()));
                    CodeParserUtil.checkInvalidWords(textArea);
                });

        // call when no longer need it: `cleanupWhenFinished.unsubscribe();`

        // load the dictionary
        try (InputStream input = getClass().getResourceAsStream("/keywords/solidity.txt");
             BufferedReader br = new BufferedReader(new InputStreamReader(input))) {
            String line;
            while ((line = br.readLine()) != null) {
                dictionary.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        BreakIterator wb = BreakIterator.getWordInstance();
        wb.setText(text);

        int lastIndex = wb.first();
        int lastKwEnd = 0;
        String previousWord = null;
        while(lastIndex != BreakIterator.DONE) {
            int firstIndex = lastIndex;
            lastIndex = wb.next();

            if (lastIndex != BreakIterator.DONE
                    && Character.isLetterOrDigit(text.charAt(firstIndex))) {
                String word = text.substring(firstIndex, lastIndex).toLowerCase();

                if (!dictionary.contains(word) && (previousWord == null
                        || !dictionary.contains(previousWord))) {
                    spansBuilder.add(Collections.emptyList(), firstIndex - lastKwEnd);
                    spansBuilder.add(Collections.singleton("underlined"), lastIndex - firstIndex);
                    lastKwEnd = lastIndex;
                }
                previousWord = word;
            }
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);

        return spansBuilder.create();
    }
}
