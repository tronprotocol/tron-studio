package org.tron.studio.utils;
import com.jfoenix.validation.base.ValidatorBase;
import javafx.beans.DefaultProperty;
import javafx.scene.control.TextInputControl;
import org.tron.studio.ShareData;


public class NetworkNameValidator extends ValidatorBase {

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
            if (!isNameAvaiable(textField.getText())) {
                hasErrors.set(true);
                return;
            }
            hasErrors.set(false);
        }
    }

    private boolean isNameAvaiable(String name) {
        return ShareData.saved_network.get(name.trim()) == null;
    }

    @Override
    public String getMessage() {
        return "Network Name Already Existing";
    }
}