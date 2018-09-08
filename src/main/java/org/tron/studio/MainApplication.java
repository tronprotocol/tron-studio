package org.tron.studio;

import com.jfoenix.controls.*;
import com.jfoenix.svg.SVGGlyphLoader;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
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

import javafx.scene.Node;
import javafx.stage.Popup;
import javafx.geometry.Insets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javafx.event.ActionEvent;
import org.tron.studio.ui.BorderSlideBar;

@Slf4j
public class MainApplication extends Application {

  public static MainApplication instance;
  public Stage primaryStage;

  public static Args cfgArgs;
  public static org.tron.common.application.Application appT;

  final Popup searchPopup = new Popup();
  private int spacingSize = 3;

  //private List<List<Integer>> matchingPos = new ArrayList<>();

  public static class SearchText {
    public List<List<Integer>> matchingPos = new ArrayList<>();
    public int currentIndex = 0;
    public String keyword = "";
  }

  public static SearchText searchTextInfo = new SearchText();


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

    ShareData.scene = scene;

    final ObservableList<String> stylesheets = scene.getStylesheets();
    stylesheets.addAll(getClass().getResource("/css/jfoenix-fonts.css").toExternalForm(),
        getClass().getResource("/css/jfoenix-design.css").toExternalForm(),
        getClass().getResource("/css/jfoenix-main-demo.css").toExternalForm(),
            getClass().getResource("/css/keywords.css").toExternalForm());
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

  private void addShotcut(Scene scene)
  {
    // Add find shotcut
    KeyCombination kc = new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN);
    Runnable rn = ()-> {

      VBox vbox = new VBox();
      TabPane codeAreaTabPane = (TabPane)scene.lookup("#codeAreaTabPane");

      if (searchPopup.isShowing())
      {
        vbox = (VBox) searchPopup.getContent().get(0);

        if (vbox.getChildren().size() == 2)
        {
          vbox.getChildren().remove(1);
          return;
        }

        HBox hboxReplace = new HBox();
        hboxReplace.setPadding(new Insets(spacingSize, spacingSize, spacingSize, spacingSize));
        hboxReplace.setSpacing(2);
        hboxReplace.setStyle("-fx-background-color: #CFCFCF;");

        JFXTextField replaceText = new JFXTextField();
        replaceText.setPrefSize(100, 5);
        replaceText.setStyle("-fx-background-color: #ffffff;-fx-font-size: 11px;");

        Button replaceBtn = new Button("Replace");
        replaceBtn.setPrefSize(80, 7);
        replaceBtn.setStyle("-fx-font-size: 11px;");

        Button replaceAllBtn = new Button("All");
        replaceAllBtn.setPrefSize(50, 7);
        replaceAllBtn.setStyle("-fx-font-size: 11px;");

        hboxReplace.getChildren().addAll(replaceText, replaceBtn, replaceAllBtn);
        vbox.getChildren().add(hboxReplace);

        searchPopup.getContent().clear();
        searchPopup.getContent().add(vbox);
        searchPopup.show(codeAreaTabPane,520,145);
        return;
      }

      vbox.setPadding(new Insets(spacingSize, spacingSize, spacingSize, spacingSize));
      vbox.setSpacing(2);
      vbox.setStyle("-fx-background-color: #CFCFCF;");

      HBox hbox = new HBox();
      hbox.setPadding(new Insets(spacingSize, spacingSize, spacingSize, spacingSize));
      hbox.setSpacing(2);
      hbox.setStyle("-fx-background-color: #CFCFCF;");

      JFXTextField searchText = new JFXTextField();
      searchText.setPrefSize(100, 5);
      searchText.setStyle("-fx-background-color: #ffffff;-fx-font-size: 11px;");

      searchText.addEventFilter(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
        @Override
        public void handle(KeyEvent event) {
          String keyword = searchText.getText();
          searchWord(keyword);
          searchTextInfo.currentIndex = 0;

          showMatchingWords();
        }
      });

      MaterialDesignIconView upIcon = new MaterialDesignIconView();
      upIcon.setGlyphName("CHEVRON_UP");
      upIcon.setSize("1.5em");
      upIcon.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
          if (searchTextInfo.currentIndex != 0)
          {
            searchTextInfo.currentIndex -= 1;
          } else
          {
            searchTextInfo.currentIndex = searchTextInfo.matchingPos.size() - 1;
          }
        }
      });

      MaterialDesignIconView downIcon = new MaterialDesignIconView();
      downIcon.setGlyphName("CHEVRON_DOWN");
      downIcon.setSize("1.5em");

      downIcon.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
          if (searchTextInfo.currentIndex == (searchTextInfo.matchingPos.size() - 1))
          {
            searchTextInfo.currentIndex = 0;
          } else
          {
            searchTextInfo.currentIndex += 1;
          }
        }
      });

      Button buttonAll= new Button("All");
      buttonAll.setPrefSize(50, 7);
      buttonAll.setStyle("-fx-font-size: 11px;");

      buttonAll.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
          searchTextInfo.currentIndex = -1;
        }
      });

      MaterialDesignIconView closeIcon = new MaterialDesignIconView();
      closeIcon.setGlyphName("CLOSE");
      downIcon.setSize("1.5em");

      closeIcon.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
          searchTextInfo.matchingPos.clear();
          searchTextInfo.currentIndex = -1;
        }
      });

      hbox.getChildren().addAll(searchText, upIcon, downIcon, buttonAll, closeIcon);

      vbox.getChildren().add(hbox);
      searchPopup.getContent().add(vbox);
      searchPopup.show(codeAreaTabPane,550,145);

      closeIcon.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
          searchPopup.hide();
        }
      });
    };
    scene.getAccelerators().put(kc, rn);
  }

  private void searchWord(String keyword)
  {
    if (keyword.length() == 0)
      return;

    searchTextInfo.matchingPos.clear();

    CodeArea currentCodeArea = (CodeArea)ShareData.currentContractTab.getContent();
    int paraNum = currentCodeArea.getParagraphs().size();

    for (int i = 0; i < paraNum; i++)
    {
      String currentLine = currentCodeArea.getText(i);
      //String[] words = regulizeLine(currentLine).split(" ");
      int wordsInt = currentLine.length();
      int keywordLength = keyword.length();
      int loopNum = wordsInt - keywordLength;

      for (int j = 0; j < loopNum; j++)
      {
        String curWord = currentLine.substring(j,j + keywordLength);
        if (curWord.equals(keyword))
        {
          // Save position
          List<Integer> pos = new ArrayList<>();
          pos.add(i);
          pos.add(j);
          searchTextInfo.matchingPos.add(pos);
        }
      }
    }
  }

  private void showMatchingWords()
  {
    CodeArea codeArea = (CodeArea)ShareData.currentContractTab.getContent();

    System.out.println(searchTextInfo.matchingPos);
    if (searchTextInfo == null || searchTextInfo.matchingPos.size() == 0)
    {
      return;
    }

    for (List<Integer> pos: searchTextInfo.matchingPos)
    {
      StyleSpansBuilder<Collection<String>> spansBuilder
              = new StyleSpansBuilder<>();
      spansBuilder.add(Collections.singleton("spell-error"), searchTextInfo.keyword.length());
      codeArea.setStyleSpans(pos.get(0), pos.get(1), spansBuilder.create());
      System.out.println(String.format("%s: %d, %d", searchTextInfo.keyword, pos.get(0), pos.get(1)));
    }

    // Show current matching word
    List<Integer> curerntPos = searchTextInfo.matchingPos.get(searchTextInfo.currentIndex);
    StyleSpansBuilder<Collection<String>> spansBuilder
            = new StyleSpansBuilder<>();
    spansBuilder.add(Collections.singleton("spell-error"), searchTextInfo.keyword.length());
    codeArea.setStyleSpans(curerntPos.get(0), curerntPos.get(1), spansBuilder.create());
  }
}
