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

    /*
    @param: String addr: url of target testnet
    @return: url with headers.
        EX: input: 127.0.0.1
            output: http://127.0.0.1
            input: https://abc.def.ghi
            return: https://abc.def.hgi
     */
    private String url_validation(String addr){
        for (String header : CONSTANTS.URL_HEAD){
            if (addr.matches("(?i)" + header + ".*"))
                return addr;
        }
        return "http://" + addr;
    }

    /*
    @param: String addr: an url of target testnet
    @return: boolean indicate if address provide is valid or not
             A valid address url contains only ascii characters
     */
    private boolean isIP(String addr) {

        addr = this.url_validation(addr.toLowerCase());
        return addr.matches("\\A\\p{ASCII}*\\z");
    }

    @Override
    public String getMessage() {
        return "Invalid IP";
    }

}