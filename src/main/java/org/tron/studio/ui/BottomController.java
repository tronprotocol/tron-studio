package org.tron.studio.ui;

import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.fxml.FXML;
import lombok.extern.slf4j.Slf4j;
import org.tron.api.GrpcAPI.AccountResourceMessage;
import org.tron.protos.Protocol;
import org.tron.studio.ShareData;

import java.math.BigDecimal;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j(topic = "BottomController")
public class BottomController {
    public JFXButton nowBlockButton;
    public JFXButton balanceButton;
    public JFXButton energyButton;
    public JFXButton rpcRestartButton;

    private ScheduledExecutorService syncExecutor = Executors.newSingleThreadScheduledExecutor();
    private AtomicInteger counter = new AtomicInteger();

    @FXML
    public void initialize() {

        syncExecutor.scheduleWithFixedDelay(() -> {
            Protocol.Account account = null;
            AccountResourceMessage accountResource = null;
            try {
                if (ShareData.wallet == null || ShareData.wallet.getRpcCli() == null) {
                    return;
                }
                account = ShareData.wallet.queryAccount();
                accountResource = ShareData.wallet
                        .getAccountResource(account.getAddress().toByteArray());
            } catch (Exception e) {
                logger.info("Connecting {}", counter.incrementAndGet());
                return;
            }

            String balance = new BigDecimal(account.getBalance()).divide(new BigDecimal(1_000_000)).toPlainString();
            String blockNumber = Long.toString(ShareData.wallet.getBlock(-1).getBlockHeader().getRawData().getNumber());
            String energy = Long.toString(accountResource.getEnergyLimit() - accountResource.getEnergyUsed());
            Platform.runLater(() -> {
                nowBlockButton.setText("Now Block:" + blockNumber);
                balanceButton.setText("Balance:" + balance);
                energyButton.setText("Energy Left:" + energy);
            });
        }, 2_000, 500, TimeUnit.MILLISECONDS);
    }
}
