package org.tron.studio.ui;

import com.jfoenix.controls.JFXTextField;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.tron.studio.ShareData;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Slf4j(topic = "CodeAreaController")
public class CodeAreaController {
    private final Popup searchPopup = new Popup();
    private final SearchText searchTextInfo = new SearchText();
    private final int spacingSize = 3;
    public CodeArea codeArea;

    @PostConstruct
    public void initialize() throws IOException {
        codeArea.textProperty().addListener((observable, oldValue, newValue) -> {
            ShareData.currentContractSourceCode.set(newValue);
        });
        ShareData.sceneObjectProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                addShortcut(codeArea);
            }
        });
    }

    private void addShortcut(CodeArea scene) {
        // Add find shotcut
        KeyCombination ctrlCombination = new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN);
        KeyCombination metaCombination = new KeyCodeCombination(KeyCode.F, KeyCombination.META_DOWN);
        Runnable rn = () -> {

            VBox vbox = new VBox();

            if (searchPopup.isShowing()) {
                vbox = (VBox) searchPopup.getContent().get(0);

                if (vbox.getChildren().size() == 2) {
                    vbox.getChildren().remove(1);
                    return;
                }

                HBox hboxReplace = new HBox();
                hboxReplace.setPadding(new Insets(0, spacingSize, 0, spacingSize));
                hboxReplace.setSpacing(1);
                hboxReplace.setStyle("-fx-background-color: #CFCFCF;");

                JFXTextField replaceText = new JFXTextField();
                replaceText.setPrefSize(100, 5);
                replaceText.setStyle("-fx-background-color: #ffffff;");

                Button replaceBtn = new Button("Replace");
                replaceBtn.setPrefSize(80, 5);
                replaceBtn.setStyle("-fx-font-size: 11px;");

                replaceBtn.setOnAction(event -> {
                    if (searchTextInfo.currentIndex < 0) return;

                    String replacement = replaceText.getText();
                    VirtualizedScrollPane virScrollPane = (VirtualizedScrollPane) ShareData.currentContractTab.getContent();
                    CodeArea currentCodeArea = (CodeArea) virScrollPane.getContent();

                    List<Integer> currentPos = searchTextInfo.matchingPos.get(searchTextInfo.currentIndex);
                    String currentKeyword = searchTextInfo.keyword;
                    currentCodeArea.replaceText(currentPos.get(0), currentPos.get(1),
                            currentPos.get(0), currentPos.get(1) + currentKeyword.length(), replacement);

                    searchTextInfo.matchingPos.remove(searchTextInfo.currentIndex);
                    searchTextInfo.currentIndex += 1;

                    if (searchTextInfo.matchingPos.size() - 1 < searchTextInfo.currentIndex) {
                        searchTextInfo.currentIndex = 0;
                    }
                });

                Button replaceAllBtn = new Button("All");
                replaceAllBtn.setPrefSize(50, 5);
                replaceAllBtn.setStyle("-fx-font-size: 11px;");

                replaceAllBtn.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent event) {
                        String replacement = replaceText.getText();
                        VirtualizedScrollPane virScrollPane = (VirtualizedScrollPane) ShareData.currentContractTab.getContent();
                        CodeArea currentCodeArea = (CodeArea) virScrollPane.getContent();
                        String currentKeyword = searchTextInfo.keyword;

                        for (List<Integer> cuPos : searchTextInfo.matchingPos) {
                            currentCodeArea.replaceText(cuPos.get(0), cuPos.get(1),
                                    cuPos.get(0), cuPos.get(1) + currentKeyword.length(), replacement);
                        }

                        searchTextInfo.matchingPos.clear();
                        searchTextInfo.currentIndex = -1;
                    }
                });

                hboxReplace.getChildren().addAll(replaceText, replaceBtn, replaceAllBtn);
                vbox.getChildren().add(hboxReplace);

                searchPopup.getContent().clear();
                searchPopup.getContent().add(vbox);
                searchPopup.show(codeArea, 520, 145);
                return;
            }

            vbox.setPadding(new Insets(spacingSize, spacingSize, spacingSize, spacingSize));
            vbox.setSpacing(2);
            vbox.setStyle("-fx-background-color: #CFCFCF;");

            HBox hbox = new HBox();
            hbox.setPadding(new Insets(spacingSize, spacingSize, spacingSize, spacingSize));
            hbox.setSpacing(2);
            hbox.setStyle("-fx-background-color: #CFCFCF;");

            JFXTextField searchText = new JFXTextField();
            searchText.setPrefSize(100, 5);
            searchText.setStyle("-fx-background-color: #ffffff;");

            searchText.addEventFilter(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    String keyword = searchText.getText();
                    searchTextInfo.keyword = keyword;
                    searchWord(keyword);
                    searchTextInfo.currentIndex = 0;

                    showMatchingWords();
                }
            });

            MaterialDesignIconView upIcon = new MaterialDesignIconView();
            upIcon.setGlyphName("CHEVRON_UP");
            upIcon.setSize("1.5em");
            upIcon.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (searchTextInfo.currentIndex != 0) {
                        searchTextInfo.currentIndex -= 1;
                    } else {
                        searchTextInfo.currentIndex = searchTextInfo.matchingPos.size() - 1;
                    }
                }
            });

            MaterialDesignIconView downIcon = new MaterialDesignIconView();
            downIcon.setGlyphName("CHEVRON_DOWN");
            downIcon.setSize("1em");

            downIcon.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (searchTextInfo.currentIndex == (searchTextInfo.matchingPos.size() - 1)) {
                        searchTextInfo.currentIndex = 0;
                    } else {
                        searchTextInfo.currentIndex += 1;
                    }
                }
            });

            MaterialDesignIconView closeIcon = new MaterialDesignIconView();
            closeIcon.setGlyphName("CLOSE");
            downIcon.setSize("1.5em");

            closeIcon.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    searchTextInfo.matchingPos.clear();
                    searchTextInfo.currentIndex = -1;
                }
            });

            hbox.getChildren().addAll(searchText, upIcon, downIcon, closeIcon);

            vbox.getChildren().add(hbox);
            searchPopup.getContent().add(vbox);
            searchPopup.show(codeArea, 550, 145);

            closeIcon.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    searchPopup.hide();
                }
            });
        };
        codeArea.getScene().getAccelerators().put(ctrlCombination, rn);
        codeArea.getScene().getAccelerators().put(metaCombination, rn);
    }

    private void searchWord(String keyword) {
        if (StringUtils.isEmpty(keyword))
            return;

        searchTextInfo.matchingPos.clear();

        VirtualizedScrollPane virScrollPane = (VirtualizedScrollPane) ShareData.currentContractTab.getContent();
        CodeArea currentCodeArea = (CodeArea) virScrollPane.getContent();
        int paraNum = currentCodeArea.getParagraphs().size();

        for (int i = 0; i < paraNum; i++) {
            String currentLine = currentCodeArea.getText(i);
            //String[] words = regulizeLine(currentLine).split(" ");
            int wordsInt = currentLine.length();
            int keywordLength = keyword.length();
            int loopNum = wordsInt - keywordLength;

            for (int j = 0; j < loopNum; j++) {
                String curWord = currentLine.substring(j, j + keywordLength);
                if (curWord.equals(keyword)) {
                    // Save position
                    List<Integer> pos = new ArrayList<>();
                    pos.add(i);
                    pos.add(j);
                    searchTextInfo.matchingPos.add(pos);
                }
            }
        }
    }

    public void showMatchingWords() {
        VirtualizedScrollPane virScrollPane = (VirtualizedScrollPane) ShareData.currentContractTab.getContent();
        CodeArea codeArea = (CodeArea) virScrollPane.getContent();

        if (searchTextInfo == null || searchTextInfo.matchingPos.size() == 0) {
            return;
        }

        for (List<Integer> pos : searchTextInfo.matchingPos) {
            StyleSpansBuilder<Collection<String>> spansBuilder
                    = new StyleSpansBuilder<>();
            spansBuilder.add(Collections.singleton("match-word"), searchTextInfo.keyword.length());
            codeArea.setStyleSpans(pos.get(0), pos.get(1), spansBuilder.create());
        }

        // Show current matching word
        List<Integer> curerntPos = searchTextInfo.matchingPos.get(searchTextInfo.currentIndex);
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        spansBuilder.add(Collections.singleton("current-match-word"), searchTextInfo.keyword.length());
        codeArea.setStyleSpans(curerntPos.get(0), curerntPos.get(1), spansBuilder.create());
    }


    public static class SearchText {
        public List<List<Integer>> matchingPos = new ArrayList<>();
        public int currentIndex = 0;
        public String keyword = "";
    }

}
