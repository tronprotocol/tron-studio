package org.tron.studio.ui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import lombok.extern.slf4j.Slf4j;
import org.fxmisc.richtext.CodeArea;
import org.tron.studio.ShareData;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Slf4j(topic = "CodeAreaController")
public class CodeAreaController {
    public CodeArea codeArea;

    @PostConstruct
    public void initialize() throws IOException {
        codeArea.textProperty().addListener((observable, oldValue, newValue) -> {
            ShareData.currentContractSourceCode.setValue(newValue);
        });
    }
}
