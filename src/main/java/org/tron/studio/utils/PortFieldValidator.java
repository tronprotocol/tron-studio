package org.tron.studio.utils;

import com.jfoenix.validation.base.ValidatorBase;
import javafx.beans.DefaultProperty;
import javafx.scene.control.TextInputControl;

@DefaultProperty(value = "icon")
public class PortFieldValidator extends ValidatorBase {

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
            if (!isPort(textField.getText())) {
                hasErrors.set(true);
                return;
            }
            hasErrors.set(false);
        }
    }

    private boolean isPort(String portStr) {
        try {
            long port = Long.parseLong(portStr);
            if (port > 0 && port < 65536) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    @Override
    public String getMessage() {
        return "Invalid Port";
    }
}