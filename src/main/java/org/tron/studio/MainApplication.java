package org.tron.studio;

import com.jfoenix.controls.*;
import com.jfoenix.svg.SVGGlyphLoader;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.tron.common.application.ApplicationFactory;
import org.tron.common.application.TronApplicationContext;
import org.tron.core.Constant;
import org.tron.core.config.DefaultConfig;
import org.tron.core.config.args.Args;
import org.tron.core.services.RpcApiService;
import org.tron.core.services.WitnessService;
import org.tron.core.services.http.FullNodeHttpApiService;

import javafx.stage.Popup;
import javafx.geometry.Insets;
import java.io.IOException;

import javafx.event.ActionEvent;
import org.fxmisc.flowless.VirtualizedScrollPane;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.nio.file.Paths;

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
    cfgArgs.setDebug(false);
    launch(args);
  }
    private static void writeIntoXML(){
        try {

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("Environment");
            doc.appendChild(rootElement);

            for(String i : ShareData.saved_network.keySet()){
                Element network = doc.createElement("network");
                rootElement.appendChild(network);
                Attr attr = doc.createAttribute("id");
                attr.setValue(i.trim());
                network.setAttributeNode(attr);
                Element ip = doc.createElement("ip");
                ip.appendChild(doc.createTextNode(ShareData.saved_network.get(i).url));
                network.appendChild(ip);

                Element port = doc.createElement("port");
                port.appendChild(doc.createTextNode(Integer.toString(ShareData.saved_network.get(i).port)));
                network.appendChild(port);


            }

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            File fXmlFile = new File(Paths.get(System.getProperty("user.home"), "TronStudio", "record", "network.xml").toString());
            StreamResult result = new StreamResult(fXmlFile);

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);

            transformer.transform(source, result);

            System.out.println("File saved!");

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }
    }

  @Override
  public void stop() throws Exception {
      writeIntoXML();
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
    primaryStage.getIcons().add(new Image(MainApplication.class.getResourceAsStream("/images/icon.png")));

    Screen screen = Screen.getPrimary();
    Rectangle2D bounds = screen.getVisualBounds();
    Scene scene = new Scene(root, bounds.getWidth() * 0.8, bounds.getHeight() * 0.8);

    final ObservableList<String> stylesheets = scene.getStylesheets();
    stylesheets.addAll(getClass().getResource("/css/jfoenix-fonts.css").toExternalForm(),
        getClass().getResource("/css/jfoenix-design.css").toExternalForm(),
        getClass().getResource("/css/jfoenix-main-demo.css").toExternalForm(),
            getClass().getResource("/css/keywords.css").toExternalForm());
    primaryStage.setScene(scene);
    primaryStage.show();
    ShareData.sceneObjectProperty.set(scene);
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
    System.out.println("********register application shutdown hook********");
    Runtime.getRuntime().addShutdownHook(new Thread(app::shutdown));
      logger.info("stopped");
  }

}
