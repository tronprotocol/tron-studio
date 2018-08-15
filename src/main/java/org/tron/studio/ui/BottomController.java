package org.tron.studio.ui;

import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.fxml.FXML;
import lombok.extern.slf4j.Slf4j;
import org.tron.studio.ShareData;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class BottomController {
    public JFXButton nowBlockButton;
    public JFXButton rpcServerButton;

    private ScheduledExecutorService syncExecutor = Executors.newSingleThreadScheduledExecutor();

    @FXML
    public void initialize() {

        syncExecutor.scheduleWithFixedDelay(() -> {
            try {
                if (ShareData.wallet == null) {
                    return;
                }
                Platform.runLater(() -> nowBlockButton.setText("Now Block:" + Long.toString(ShareData.wallet.getNowBlock().getBlockHeader().getRawData().getNumber())));
            } catch (Throwable t) {
                logger.error("Error in getNowBlock {}" + t.getMessage(), t);
            }
        }, 2000, 500, TimeUnit.MILLISECONDS);
    }
}
