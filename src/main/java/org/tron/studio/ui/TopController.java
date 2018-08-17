package org.tron.studio.ui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import org.tron.studio.MainApplication;

import javax.annotation.PostConstruct;
import java.io.IOException;
import com.sun.javafx.util.Utils;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;

public class TopController {
    public Pane titlePane;
    private double xOffset;
    private double yOffset;
    private double xInit;
    private double yInit;
    private Rectangle2D backupWindowBounds;

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
        MainApplication.instance.primaryStage.setIconified(true);
    }

    public void onClickMax(MouseEvent mouseEvent) {
        Stage stage = MainApplication.instance.primaryStage;
        final double stageY = Utils.isMac() ? stage.getY() - 22 : stage.getY(); // TODO Workaround for RT-13980
        final Screen screen = Screen.getScreensForRectangle(stage.getX(), stageY, 1, 1).get(0);      // line 42
        Rectangle2D bounds = screen.getVisualBounds();
        if (bounds.getMinX() == stage.getX() && bounds.getMinY() == stageY &&
                bounds.getWidth() == stage.getWidth() && bounds.getHeight() == stage.getHeight()) {
            if (backupWindowBounds != null) {
                stage.setX(backupWindowBounds.getMinX());
                stage.setY(backupWindowBounds.getMinY());
                stage.setWidth(backupWindowBounds.getWidth());
                stage.setHeight(backupWindowBounds.getHeight());
            }
        } else {
            backupWindowBounds = new Rectangle2D(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
            final double newStageY = Utils.isMac() ? screen.getVisualBounds().getMinY() + 22 : screen.getVisualBounds().getMinY(); // TODO Workaround for RT-13980
            stage.setX(screen.getVisualBounds().getMinX());
            stage.setY(newStageY);
            stage.setWidth(screen.getVisualBounds().getWidth());
            stage.setHeight(screen.getVisualBounds().getHeight());
        }
    }

    public void onClickClose(MouseEvent mouseEvent) {
        Platform.exit();
    }
}
