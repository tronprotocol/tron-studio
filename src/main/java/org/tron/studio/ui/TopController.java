package org.tron.studio.ui;

import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import org.tron.studio.MainApplication;

import javax.annotation.PostConstruct;
import java.io.IOException;

public class TopController {
    public Pane titlePane;
    private double xOffset;
    private double yOffset;
    private double xInit;
    private double yInit;

    @PostConstruct
    public void initialize() throws IOException {
        titlePane.setOnMousePressed(e -> {
            xOffset = e.getScreenX();
            yOffset = e.getScreenY();
            xInit = MainApplication.instance.primaryStage.getX();
            yInit = MainApplication.instance.primaryStage.getY();
        });
        titlePane.setOnMouseDragged(e -> {
            MainApplication.instance.primaryStage.setX(xInit + e.getScreenX() - xOffset);
            MainApplication.instance.primaryStage.setY(yInit + e.getScreenY() - yOffset);
        });
    }

    public void onClickMin(MouseEvent mouseEvent) {
    }

    public void onClickMax(MouseEvent mouseEvent) {
    }

    public void onClickClose(MouseEvent mouseEvent) {
    }
}
