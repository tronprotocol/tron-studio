package org.tron.studio.utils;

import com.jfoenix.validation.base.ValidatorBase;
import javafx.beans.DefaultProperty;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.TextInputControl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@DefaultProperty(value = "icon")
public class IPFieldValidator extends ValidatorBase {

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
            if(!isIP(textField.getText())) {
                hasErrors.set(true);
                return;
            }
            hasErrors.set(false);
        }
    }

    private boolean isIP(String addr) {
        if (addr.length() < 7 || addr.length() > 15 || "".equals(addr)) {
            return false;
        }
        String rexp = "(?=(\\b|\\D))(((\\d{1,2})|(1\\d{1,2})|(2[0-4]\\d)|(25[0-5]))\\.){3}((\\d{1,2})|(1\\d{1,2})|(2[0-4]\\d)|(25[0-5]))(?=(\\b|\\D))";
        Pattern pat = Pattern.compile(rexp);
        Matcher mat = pat.matcher(addr);
        return mat.find();
    }

    @Override
    public String getMessage() {
        return "Invalid IP";
    }
}