package org.tron.studio.utils;

import com.jfoenix.validation.base.ValidatorBase;
import javafx.beans.DefaultProperty;
import javafx.scene.control.TextInputControl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@DefaultProperty(value = "icon")
public class FileNameFieldValidator extends ValidatorBase {

    @Override
    protected void eval() {
        if (srcControl.get() instanceof TextInputControl) {
            evalTextInputField();
        } else {
            hasErrors.set(true);
        }
    }

    private void evalTextInputField() {
        TextInputControl textField = (TextInputControl) srcControl.get();
        if (textField.getText() == null || textField.getText().isEmpty()) {
            hasErrors.set(true);
        } else {
            if (!isFileName(textField.getText())) {
                hasErrors.set(true);
                return;
            }
            hasErrors.set(false);
        }
    }

    private boolean isFileName(String fileName) {
        if (fileName.length() < 1 || fileName.length() > 16 || "".equals(fileName)) {
            return false;
        }
        if(!fileName.endsWith(".sol")) {
            return false;
        }
        String rexp = "\\w*.sol";
        Pattern pat = Pattern.compile(rexp);
        Matcher mat = pat.matcher(fileName);
        return mat.find();
    }

    @Override
    public String getMessage() {
        return "Invalid contract file name, eg: Example.sol";
    }
}