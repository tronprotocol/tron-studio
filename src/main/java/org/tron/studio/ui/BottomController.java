package org.tron.studio.ui;

import com.jfoenix.controls.JFXButton;
import io.grpc.StatusRuntimeException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import lombok.extern.slf4j.Slf4j;
import org.tron.protos.Protocol;
import org.tron.studio.ShareData;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j(topic = "BottomController")
public class BottomController {
    public JFXButton nowBlockButton;
    public JFXButton balanceButton;
    public JFXButton rpcRestartButton;

    private ScheduledExecutorService syncExecutor = Executors.newSingleThreadScheduledExecutor();
    private AtomicInteger counter = new AtomicInteger();

    @FXML
    public void initialize() {

        syncExecutor.scheduleWithFixedDelay(() -> {
            if (ShareData.wallet == null || ShareData.wallet.getRpcCli() == null) {
                return;
            }
            Platform.runLater(() -> {
                try {
                    nowBlockButton.setText("Now Block:" + Long.toString(ShareData.wallet.getBlock(-1).getBlockHeader().getRawData().getNumber()));

                    Protocol.Account account = ShareData.wallet.queryAccount();
                    balanceButton.setText("Balance:" + Long.toString(account.getBalance() / 1_000_000));
                } catch (StatusRuntimeException e) {
                    logger.info("Connecting {}", counter.incrementAndGet());
                } catch (Exception t) {
                    logger.error("Error in getNowBlock {}" + t.getMessage(), t);
                }
            });
        }, 2_000, 500, TimeUnit.MILLISECONDS);
    }
}
