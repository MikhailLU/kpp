
/**
 * My Game FlappyBird written by Java with JavaFX 
 * @author Mikhail N.
 */

package my_crazy_bird;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

/** class Menu */
public class Menu extends Application {

  /** Declaration of Stage */
  private static Stage stage; 
  /** Declaration of Scene */
  private static Scene scene;  

  /**Width of Window*/
  public static final int W = 900; 
  /**Height of Window*/
  public static final int H = 500;  

  /**SubMenu for MenuItems*/
  public static SubMenu mainMenu; 
  /**MenuBox for SubMenu*/
  public static MenuBox menuBox;  
  /**Declaration of new GamePlaying Process*/
  static NewGameInterface gi = new NewGameInterface();  
  /**Declaration of Result of Game*/
  public static Rating rating = new Rating(); 
  /**For music and sounds*/
  static MediaPlayer mediaPlayer, soundPlayer; 
  /**For volume managing*/
  static Slider volumeslider = new Slider(); 
  /**Initial level of volume*/
  static double level_Of_volume = 0.2;  
  public static ButtonVolume btn_volume;

  public static final int EASYMODE = 0;
  public static final int NORMALMODE = 1;
  public static final int HARDMODE = 2;

  public static boolean humanPlaying = true;
  
  public static boolean replayGameis = false;

  public final static String STYLE = "Style.css";
  public final static String TITLE = "Crazy bird";
  public final static String MENU_TITLE = "F L A P P Y  B I R D";
  public final static String BIRD_MAIN = "birdmain.png";
  public final static String PETUH_SOUND = "petuh.mp3";
  public final static String MAIN_THEME = "maintheme.mp3";
  public final static String VOLUME_PNG = "volume.png";
  public final static String NOT_VOLUME_PNG = "notvolume.png";
  public final static String REPLAY_TXT = "replay.txt";
 
  public static FileWorking fw = new FileWorking();

  public static void main(String[] args) {
    launch(args);
  }

  @Override public void start(Stage primaryStage) throws Exception {

    stage = primaryStage;
    primaryStage.setResizable(false);
    scene = new Scene(createContent(), W, H); // Creating Scene
    scene.getStylesheets().add(getClass().getResource(STYLE).toExternalForm());
    primaryStage.setTitle(TITLE);
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  /** Pushing items on Pane */

  public static Parent createContent() {

    Pane root = new Pane();
    root.setPrefSize(W, H);
    /** Add a Color YELLOW to Pane	*/
    root.setStyle("-fx-background-color: #FFFF00"); 
    /** Create rectangle for Dividing pane on 2 parts */
    Rectangle rect = new Rectangle(W + W/18, H / 2 + H/10); 
    /** Add a Color BLUE to Pane*/
    rect.setFill(Color.CYAN); 
    rect.setTranslateX(0);
    rect.setTranslateY(H / 2);
    /**Creating title in Menu*/
    Title title = new Title(MENU_TITLE); // Creating title in Menu

    MenuItem newGame = new MenuItem("NEW GAME");
    MenuItem botGame = new MenuItem("BOT GAME");
    MenuItem replayGame = new MenuItem("REPLAY");
    MenuItem statistics = new MenuItem("RATING");
    MenuItem exitGame = new MenuItem("EXIT");
    mainMenu = new SubMenu(newGame, botGame,replayGame, statistics, exitGame);
    
    MenuItem easy = new MenuItem("EASY");
    MenuItem normal = new MenuItem("NORMAL");
    MenuItem difficult = new MenuItem("HARD");
    MenuItem complexBack = new MenuItem("BACK");
    // Adding to NewGameMenu
    SubMenu newGameMenu = new SubMenu(easy, normal, difficult,complexBack);

    MenuItem easyResult = new MenuItem("BEST EASY :" + Rating.getResult(EASYMODE));
    MenuItem normalResult = new MenuItem("BEST NORMAL :" + Rating.getResult(NORMALMODE));
    MenuItem difficultResult = new MenuItem("BEST DIFFICULT :" + Rating.getResult(HARDMODE));
    MenuItem Back = new MenuItem("BACK");
    SubMenu statisticsMenu = new SubMenu();
    statisticsMenu.addMenu(easyResult, normalResult, difficultResult,Back);
    /** Initial Setting a MainMenu*/
    menuBox = new MenuBox(mainMenu);

    /**Setting a handlers for special MenuItems*/

    /** Loading MenuItems of NewGame*/
    newGame.setOnMouseClicked(new EventHandler<MouseEvent>() {
      public void handle(MouseEvent mE) {
        menuBox.setSubMenu(newGameMenu);
        humanPlaying = true;
       }
     });
    botGame.setOnMouseClicked(new EventHandler<MouseEvent>() {
      public void handle(MouseEvent me) {
        menuBox.setSubMenu(newGameMenu);
        humanPlaying = false;
      }
    });
    replayGame.setOnMouseClicked(new EventHandler<MouseEvent>() {
      public void handle(MouseEvent me) {
        replayGameis = true;
        humanPlaying = true;
        mediaPlayer.stop();
        int mode = 0;
        gi.startGame(stage, W, H, mode, humanPlaying,replayGameis);
      }
    });
    
    /** Loading MenuItems of StatisticsMenu*/
    statistics.setOnMouseClicked(event -> {
      menuBox.setSubMenu(statisticsMenu);
    }); 
    exitGame.setOnMouseClicked(event -> {
      System.exit(0);
    }); // Exit from Game
    /** Return to MainMenu*/
    Back.setOnMouseClicked(event -> {
      menuBox.setSubMenu(mainMenu);
    }); 
    /** Return to MainMenu*/
    complexBack.setOnMouseClicked(event -> {
      menuBox.setSubMenu(mainMenu);
    });
    /** Playing music in Main Menu*/
    playMainTheme(); 
    /**Creating Button volume*/
    btn_volume = new ButtonVolume(); 
    /**Calling a method which check action with this button*/
    btn_volume.checkAction(); 
    /**Add settings to volumeController*/
    initializeVolumeSlider();  
    /**Check start NewGame*/
    checkOnstartGame(easy, normal, difficult); 

    root.getChildren().addAll(rect, menuBox, title, btn_volume, volumeslider, getImageOfBird());
    return root;
  }

  public static void initializeVolumeSlider() {
    int[] coordOfVolumeSlider = {
      W / 36, H - H / 3 - H / 30
    };
    volumeslider.setMin(0);
    volumeslider.setMax(1);
    volumeslider.setValue(level_Of_volume);
    volumeslider.setOrientation(Orientation.VERTICAL);
    volumeslider.setTranslateX(coordOfVolumeSlider[0]);
    volumeslider.setTranslateY(coordOfVolumeSlider[1]);
    volumeslider.valueProperty().addListener((ov, old_val, new_val) -> {
      mediaPlayer.setVolume((double) new_val);
      level_Of_volume = (double) new_val;
      btn_volume.checkAction();
    });
  }

  private static void checkOnstartGame(	MenuItem easy, MenuItem normal,	MenuItem difficult) {
    easy.setOnMouseClicked(new EventHandler<MouseEvent>() {
       public void handle(MouseEvent mE) {
         mediaPlayer.stop();
         /** Start newGame in Easy Way*/
         replayGameis = false;
         fw.writeInFile(ReplayEnum.getType(ReplayEnum.MODE),EASYMODE, REPLAY_TXT);
         gi.startGame(stage, W, H, EASYMODE, humanPlaying,replayGameis);
        }
     });
    normal.setOnMouseClicked(new EventHandler<MouseEvent>() {
      public void handle(MouseEvent mE) {
        mediaPlayer.stop();
        /** Start newGame in Normal Way*/
        replayGameis = false;
        fw.writeInFile(ReplayEnum.getType(ReplayEnum.MODE),NORMALMODE, REPLAY_TXT);
        gi.startGame(stage, W, H, NORMALMODE, humanPlaying,replayGameis);
      }
    });
    difficult.setOnMouseClicked(new EventHandler<MouseEvent>() {
      public void handle(MouseEvent mE) {
        mediaPlayer.stop();
        /** Start newGame in Hard Way*/
        replayGameis = false;
        fw.writeInFile(ReplayEnum.getType(ReplayEnum.MODE),HARDMODE, REPLAY_TXT);
        gi.startGame(stage, W, H, HARDMODE, humanPlaying,replayGameis);
      }
    });
  }
  /**Playing music*/
  private static void playMainTheme() {
    String path = MAIN_THEME;
    Media media = new Media(new File(path).toURI().toString());
    mediaPlayer = new MediaPlayer(media);
    mediaPlayer.setAutoPlay(true);
    mediaPlayer.setVolume(level_Of_volume);
    mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
  }

  private static ImageView getImageOfBird() {

    int[] sizeOfMainBird = {
      5 * W / 18, 3 * H / 5
    };
    int[] coordOfMainBird = {
      W - W / 3 + W / 18, H / 4
    };
    int rateOfScale = 500;
    int offset = 4;
    double increase = 1.1;

    ImageView imgMainBird;
    /** Loading bird in Main Menu*/
    try (InputStream is = Files.newInputStream(Paths.get(BIRD_MAIN))) {
      imgMainBird = new ImageView(new Image(is));
      imgMainBird.setFitWidth(sizeOfMainBird[0]);
      imgMainBird.setFitHeight(sizeOfMainBird[1]);
      imgMainBird.setTranslateX(coordOfMainBird[0]);
      imgMainBird.setTranslateY(coordOfMainBird[1]);

      DropShadow shadow = new DropShadow();
      shadow.setColor(Color.RED);
      shadow.setOffsetX(offset);
      shadow.setOffsetY(offset);

      imgMainBird.setOnMouseEntered(event -> {
        ScaleTransition st = new ScaleTransition(Duration.millis(rateOfScale), 
                                                 imgMainBird);
        imgMainBird.setEffect(shadow);
        st.setToX(increase);
        st.setToY(increase);
        st.play();
      });
      imgMainBird.setOnMouseExited(event -> {
        ScaleTransition st = new ScaleTransition(Duration.millis(rateOfScale),
                                                 imgMainBird);
        imgMainBird.setEffect(null);
        st.setToX(1);
        st.setToY(1);
        st.play();
      });

      imgMainBird.setOnMouseClicked(event -> {
        int cycle_count = 6;
        double deviation = -W / 45;
        int Rduration = 90;
        double volume = 0.1;

        String path = PETUH_SOUND;
        Media mediaSound = new Media(new File(path).toURI().toString());
        soundPlayer = new MediaPlayer(mediaSound);
        soundPlayer.setVolume(volume);
        soundPlayer.play();
        RotateTransition rt = new RotateTransition(Duration.millis(Rduration),
                                                   imgMainBird);
        rt.setByAngle(deviation);
        rt.setCycleCount(cycle_count);
        rt.setAutoReverse(true);
        rt.play();
      });
    } catch (IOException e) {
        System.err.println("Caught IOException: " +  e.getMessage());
        return null;
    }
    return imgMainBird;
  }

  /** class ButtonVolume*/
  private static class ButtonVolume extends Button {
    ImageView imgVolumeButton;
    double sizeOfButton = H / 25;
    double[] coordOfButton = {
      H / 20, H - 2 * H / 25
    };
    double prevvolume = level_Of_volume;

    public ButtonVolume() {
      try (InputStream is = Files.newInputStream(Paths.get(VOLUME_PNG))) {
        imgVolumeButton = new ImageView(new Image(is));
        imgVolumeButton.setFitHeight(sizeOfButton);
        imgVolumeButton.setFitWidth(sizeOfButton);
        setTranslateX(coordOfButton[0]);
        setTranslateY(coordOfButton[1]);
        setGraphic(imgVolumeButton);
      } catch (IOException e) {
          System.err.println("Caught IOException: " +  e.getMessage());
          return;
      }
    }
    public void loadVolumePng() {
      try (InputStream is = Files.newInputStream(Paths.get(VOLUME_PNG))) {
        imgVolumeButton = new ImageView(new Image(is));
        imgVolumeButton.setFitHeight(sizeOfButton);
        imgVolumeButton.setFitWidth(sizeOfButton);
        setGraphic(imgVolumeButton);
      } catch (IOException e) {
          System.err.println("Caught IOException: " +  e.getMessage());
          return;
      }
    }
    public void loadNotVolumePng() {
      try (InputStream is = Files.newInputStream(Paths.get(NOT_VOLUME_PNG))) {
        imgVolumeButton = new ImageView(new Image(is));
        imgVolumeButton.setFitHeight(sizeOfButton);
        imgVolumeButton.setFitWidth(sizeOfButton);
        setGraphic(imgVolumeButton);
      } catch (IOException e) {
          System.err.println("Caught IOException: " +  e.getMessage()); 
          return;
      }
    }
    /** Checking on action from button*/
    public void checkAction() {
      if (level_Of_volume > 0) {
        loadVolumePng();
      } else {
          loadNotVolumePng();
      }
      setOnAction(new EventHandler<ActionEvent>() {
        @Override public void handle(ActionEvent event) {
          if (level_Of_volume > 0) {
            loadNotVolumePng();
            prevvolume = level_Of_volume;
            level_Of_volume = 0;
            volumeslider.setValue(level_Of_volume);
            mediaPlayer.setVolume(level_Of_volume);
          } else {
            loadVolumePng();
            level_Of_volume = prevvolume;
            volumeslider.setValue(level_Of_volume);
            mediaPlayer.setVolume(level_Of_volume);
          }
        }
      });
    }
  }

  private static class SubMenu extends VBox {
    int spacing = 15;
    public SubMenu(MenuItem... items) {
      setSpacing(spacing);
      setLayoutX(W / 3);
      setLayoutY(H / 3);
      for (MenuItem item : items) {
        getChildren().addAll(item);
      }
    }
    int add_spacing = 15;
    double[] layout = {
      W / 3 - W / 9, H / 3 + H / 50
    };
    public void addMenu(MenuItem... items) {
      setSpacing(add_spacing);
      setLayoutX(layout[0]);
      setLayoutY(layout[1]);
      for (MenuItem item : items) {
        getChildren().addAll(item);
      }
    }
  }
  
  private static class MenuBox extends Pane {
    static SubMenu subMenu;
    int[] coordOfRectangle = {
      W / 3, 3 * H / 5
    };
    public MenuBox(SubMenu subMenu) {
      MenuBox.subMenu = subMenu;

      Rectangle bg = new Rectangle();
      bg.setLayoutX(coordOfRectangle[0]);
      bg.setLayoutY(coordOfRectangle[1]);
      getChildren().addAll(bg, subMenu);
    }
    public void setSubMenu(SubMenu subMenu) {
      getChildren().remove(MenuBox.subMenu);
      MenuBox.subMenu = subMenu;
      getChildren().add(MenuBox.subMenu);
    }
  }

  private static class MenuItem extends StackPane {
    public MenuItem(String name) {
      Text text = new Text(name);
      text.setStroke(Color.BLACK);
      text.setStrokeWidth(1);
      text.setFill(Color.RED);
      text.setFont(Font.font("Comic Sans", FontWeight.BOLD, 40));

      setAlignment(Pos.CENTER);

      setOnMouseEntered(event -> {
        text.setFill(Color.WHITE);
      });
      setOnMouseExited(event -> {
        text.setFill(Color.RED);
      });
      getChildren().addAll(text);
    }
  }

  private static class Title extends StackPane {
    double[] coordOfTitle = {
      W / 5, H / 25
    };
    int offset = 3;

    public Title(String name) {
      Text text = new Text(name);
      text.setTranslateX(coordOfTitle[0]);
      text.setTranslateY(coordOfTitle[1]);
      text.setFill(Color.CHARTREUSE);
      text.setStroke(Color.BLACK);
      text.setStrokeWidth(2);
      text.setFont(Font.font("Times New Roman", FontWeight.BOLD, 60));

      DropShadow ds = new DropShadow();
      ds.setOffsetY(offset);

      setOnMouseEntered(event -> {
        text.setEffect(ds);
        text.setFill(Color.WHITE);
      });
      setOnMouseExited(event -> {
        text.setFill(Color.CHARTREUSE);
        text.setEffect(null);
      });
      getChildren().addAll(text);
    }
  }
}
