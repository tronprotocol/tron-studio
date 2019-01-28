package org.tron.studio.utils;

import com.jfoenix.validation.base.ValidatorBase;
import javafx.beans.DefaultProperty;
import javafx.scene.control.TextInputControl;
import org.tron.studio.CONSTANTS;
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
            if (!isIP(textField.getText())) {
                hasErrors.set(true);
                return;
            }
            hasErrors.set(false);
        }
    }

    private String url_validation(String addr){
        for (String header : CONSTANTS.URL_HEAD){
            if (addr.matches("(?i)" + header + ".*"))
                return addr;
        }
        return "http://" + addr;
    }

    private boolean isIP(String addr) {
        if (!addr.matches("\\A\\p{ASCII}*\\z"))
            return false;
        addr = this.url_validation(addr.toLowerCase());
        return true;
    }

    @Override
    public String getMessage() {
        return "Invalid IP";
    }

}