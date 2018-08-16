package org.tron.studio;

import com.jfoenix.svg.SVGGlyphLoader;
import java.io.IOException;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.tron.common.application.ApplicationFactory;
import org.tron.core.Constant;
import org.tron.core.Wallet;
import org.tron.core.config.DefaultConfig;
import org.tron.core.config.args.Args;
import org.tron.core.services.RpcApiService;
import org.tron.core.services.WitnessService;
import org.tron.core.services.http.FullNodeHttpApiService;

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
    primaryStage.initStyle(StageStyle.UNDECORATED);

    Scene scene = new Scene(root, 1024, 576);
    final ObservableList<String> stylesheets = scene.getStylesheets();
    stylesheets.addAll(getClass().getResource("/css/jfoenix-fonts.css").toExternalForm(),
        getClass().getResource("/css/jfoenix-design.css").toExternalForm(),
        getClass().getResource("/css/jfoenix-main-demo.css").toExternalForm());
    primaryStage.setScene(scene);

    primaryStage.show();
  }

  private static void startFullNode() {
    logger.info("Full node running.");
    if (cfgArgs.isHelp()) {
      logger.info("Here is the help message.");
      return;
    }

    DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
    beanFactory.setAllowCircularReferences(false);
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
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
    ShareData.wallet = context.getBean(Wallet.class);
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
