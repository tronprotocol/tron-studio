package org.tron.studio.utils;

import com.jfoenix.validation.base.ValidatorBase;
import javafx.beans.DefaultProperty;
import javafx.scene.control.TextInputControl;
import org.spongycastle.util.encoders.Hex;
import org.tron.common.crypto.ECKey;

@DefaultProperty(value = "icon")
public class PrivateKeyFieldValidator extends ValidatorBase {

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

    private boolean isPort(String privateKeyStr) {
        try {
            ECKey ecKey = ECKey.fromPrivate(Hex.decode(privateKeyStr));
            if (ecKey != null) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    @Override
    public String getMessage() {
        return "Invalid Private Key";
    }
}