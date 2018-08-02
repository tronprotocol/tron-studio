package org.tron.studio;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.scene.layout.HBox;
import org.fxmisc.richtext.CodeArea;

public class MainController implements Initializable {
    public CodeArea codeArea;
    public HBox rootPanel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        StringBuilder builder = new StringBuilder();
        try {
            Files.lines(Paths.get(getClass().getResource("/template/Ballot.sol").toURI())).forEach(line -> {
                builder.append(line).append(System.getProperty("line.separator"));
            });
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        codeArea.insertText(0, builder.toString());
    }

}
