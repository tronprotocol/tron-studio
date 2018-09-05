package org.tron.studio;

import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.svg.SVGGlyphLoader;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.tron.common.application.ApplicationFactory;
import org.tron.common.application.TronApplicationContext;
import org.tron.common.net.udp.handler.EventHandler;
import org.tron.core.Constant;
import org.tron.core.config.DefaultConfig;
import org.tron.core.config.args.Args;
import org.tron.core.services.RpcApiService;
import org.tron.core.services.WitnessService;
import org.tron.core.services.http.FullNodeHttpApiService;

import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCode;
import javafx.stage.Popup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import java.io.IOException;
import javafx.event.ActionEvent;

@Slf4j
public class MainApplication extends Application {

  public static MainApplication instance;
  public Stage primaryStage;

  public static Args cfgArgs;
  public static org.tron.common.application.Application appT;

  public static void main(String[] args) {
    Args.setParam(args, Constant.TESTNET_CONF);
    cfgArgs = Args.getInstance();
    cfgArgs.setWitness(true);
    cfgArgs.setDebug(true);
    launch(args);
  }

  @Override
  public void stop() throws Exception {
    stopFullNode();
    super.stop();
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    instance = this;
    this.primaryStage = primaryStage;

    new Thread(() -> {
      try {
        SVGGlyphLoader.loadGlyphsFont(getClass().getResourceAsStream("/fonts/icomoon.svg"),
            "icomoon.svg");
      } catch (IOException ioExc) {
        ioExc.printStackTrace();
      }
    }).start();
    new Thread(MainApplication::startFullNode).start();

    Parent root = FXMLLoader.load(getClass().getResource("application.fxml"));
    primaryStage.setTitle("Tron Studio");

    Screen screen = Screen.getPrimary();
    Rectangle2D bounds = screen.getVisualBounds();
    Scene scene = new Scene(root, bounds.getWidth(), bounds.getHeight());

    addShotcut(scene);

    final ObservableList<String> stylesheets = scene.getStylesheets();
    stylesheets.addAll(getClass().getResource("/css/jfoenix-fonts.css").toExternalForm(),
        getClass().getResource("/css/jfoenix-design.css").toExternalForm(),
        getClass().getResource("/css/jfoenix-main-demo.css").toExternalForm(),
            getClass().getResource("/css/keywords.css").toExternalForm());
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  private void addShotcut(Scene scene)
  {
    // Add find shotcut
    KeyCombination kc = new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN);
    Runnable rn = ()-> {
      HBox hbox = new HBox();
      hbox.setPadding(new Insets(5, 5, 5, 5));
      hbox.setSpacing(10);
      hbox.setStyle("-fx-background-color: #CFCFCF;");

      JFXTextField searchText = new JFXTextField();
      searchText.setPrefSize(100, 15);

      Button buttonUp = new Button("Up");
      buttonUp.setPrefSize(80, 15);
      Button buttonDown= new Button("Down");
      buttonDown.setPrefSize(80, 15);
      Button buttonAll= new Button("All");
      buttonAll.setPrefSize(80, 15);

      Button buttonClose= new Button("Close");
      buttonClose.setPrefSize(80, 15);

      hbox.getChildren().addAll(searchText, buttonUp, buttonDown, buttonAll, buttonClose);

      final Popup popup = new Popup();
      popup.setX(600);
      popup.setY(100);
      popup.getContent().add(hbox);
      popup.show(MainApplication.instance.primaryStage);

      buttonClose.setOnAction((e) -> {
        popup.hide();
      });



    };
    scene.getAccelerators().put(kc, rn);
  }

  private static void startFullNode() {
    logger.info("Full node running.");
    if (cfgArgs.isHelp()) {
      logger.info("Here is the help message.");
      return;
    }

    DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
    beanFactory.setAllowCircularReferences(false);
    TronApplicationContext context = new TronApplicationContext(
        beanFactory);
    context.register(DefaultConfig.class);
    context.refresh();

    appT = ApplicationFactory.create(context);
    shutdown(appT);
    //appT.init(cfgArgs);

    RpcApiService rpcApiService = context.getBean(RpcApiService.class);
    appT.addService(rpcApiService);
    if (cfgArgs.isWitness()) {
      appT.addService(new WitnessService(appT, context));
    }

    //http
    FullNodeHttpApiService httpApiService = context.getBean(FullNodeHttpApiService.class);
    appT.addService(httpApiService);

    appT.initServices(cfgArgs);
    appT.startServices();
    appT.startup();

    rpcApiService.blockUntilShutdown();
  }

  private static void stopFullNode() {
    appT.shutdownServices();
    appT.shutdown();
    System.exit(0);
  }


  public static void shutdown(final org.tron.common.application.Application app) {
    logger.info("********register application shutdown hook********");
    Runtime.getRuntime().addShutdownHook(new Thread(app::shutdown));
  }
}
