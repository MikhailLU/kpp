package my_crazy_bird;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

/** Class NewGameInterface */
public class Game implements Runnable {

  public Stage gamestage;
  public Scene scene;
  public static Pane appRoot = new Pane();
  public Pane resultPane = new Pane();

  public boolean replayGame = false;

  public static int widthScreen, heightScreen;

  public AnimationTimer timer;

  public static boolean humanPlaying;

  public Bird bird;

  public static ArrayList<Wall> walls = new ArrayList<>();
  public static final int DISTANSE_BETWEEN_WALLS = 300;
  public static final int NUMBER_OF_WALLS = 4;
  public static final int NUMBER_OF_CLOUDS = 5;
  public int wallNumber;
  public int wallsPassed = 0;
  public int hole;

  public static int score = 0;
  public int numOfWalls = 0;
  public static Score scoreLabel = new Score("" + score);

  public ImageView groundview, sun;
  public ImageView[] cloud = new ImageView[5];
  public static final String GROUND_PICTURE = "ground.png";
  public static final String SUN_PICTURE = "sun.png";
  public static final String CLOUD_PICTURE = "cloud.png";

  public boolean gameOver = false;
  boolean pauseGame = false;
  Text helpTextDownPause = new Text();
  public int rateOfWalls;
  public static final int RATE_OF_WALLS_EASY = 3;
  public static final int RATE_OF_WALLS_NORM = 4;
  public static final int RATE_OF_WALLS_HARD = 6;

  public int modeOfGame;
  public static final int EASY_MODE = 0;
  public static final int NORMAL_MODE = 1;
  public static final int HARD_MODE = 2;

  public static final int HOLE_EASY = 230;
  public static final int HOLE_NORMAL = 210;
  public static final int HOLE_HARD = 180;

  public Text[] pauseText = new Text[2];

  FileWorking fw = new FileWorking();
  public String fileFromLoad;
  static ReplayEnum re;
  double counterOfTime = 0;
  boolean flagStop = false;
  boolean flagExit = false;
  Timer time = new java.util.Timer();
  Replay replay = new Replay();
  String tempFile;
  
  Notation notation = new Notation();
  List<Double> list = new ArrayList<>();

  public void startGame(Stage stage, int w, int h, int m, boolean _hp, boolean rp,
      String fileFromLoading) {
    widthScreen = w;
    heightScreen = h;
    humanPlaying = _hp;
    gamestage = stage;
    replayGame = rp;
    if (!replayGame) {
      modeOfGame = m;
    } else {
      this.fileFromLoad = fileFromLoading;
      modeOfGame = fw.readModeFromFile(fileFromLoad);
      flagExit = false;
      flagStop = false;
    }
    run();
  }

  @Override
  public void run() {
    checkOnMode();
    counterOfTime = 0;
    timer = new AnimationTimer() {
      @Override
      public void handle(long now) {
        update();
        if (!replayGame) {
          counterOfTime = counterOfTime + 0.001;
        }
      }
    };
    /** start time */
    timer.start();
    newGameContent();
    setActionOnScene();
    gamestage.setScene(scene);
    gamestage.show();
    if (!replayGame) {
      scene.setOnMouseClicked(e -> {
        if (!gameOver) {
          if (humanPlaying) {
            timer.stop();
            fw.writeInFile(ReplayEnum.getType(ReplayEnum.TIME), counterOfTime, tempFile);
            fw.writeInFile(ReplayEnum.getType(ReplayEnum.FLAPPY), tempFile);
            timer.start();
            bird.jumpflappy();
          }
        } else {
          intitalAddingItemesToGame();
        }
      });
    }
    if (replayGame) {
      replay.start(this, bird, fileFromLoad);
    }
  }

  private void setActionOnScene() {

    scene = new Scene(appRoot, widthScreen, heightScreen);
    scene.setOnKeyReleased(event -> {
      if (event.getCode() == KeyCode.ESCAPE) {
        if (!replayGame) {
          fw.writeInFile(ReplayEnum.getType(ReplayEnum.TIME), counterOfTime, tempFile);
          fw.writeInFile(ReplayEnum.getType(ReplayEnum.ESC), tempFile);
          try {
            fw.saveFile(tempFile, score);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
        if (replayGame) {
          replay.refreshReplay();
        }
        backToMenu();
      }
    });
    /** checking for pause */
    scene.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.F10) {
        if (!gameOver) {
          if (pauseGame) {
            appRoot.getChildren().removeAll(pauseText[0], pauseText[1]);
            pauseGame = false;
            startAll();
          } else {
            printPause();
            stopAll();
            pauseGame = true;
          }
        }
      }
    });
  }

  private void checkOnMode() {
    switch (modeOfGame) {
      case EASY_MODE: {
        hole = HOLE_EASY;
        rateOfWalls = RATE_OF_WALLS_EASY;
        break;
      }
      case NORMAL_MODE: {
        hole = HOLE_NORMAL;
        rateOfWalls = RATE_OF_WALLS_NORM;
        break;
      }
      case HARD_MODE: {
        hole = HOLE_HARD;
        rateOfWalls = RATE_OF_WALLS_HARD;
        break;
      }
    }
  }

  /** printing finish result */
  private void printResult() {
    if (!replayGame) {
      fw.writeInFile(ReplayEnum.getType(ReplayEnum.TIME), counterOfTime, tempFile);
      fw.writeInFile(ReplayEnum.getType(ReplayEnum.GAMEOVER), tempFile);
      counterOfTime = 0;
    }
    if (replayGame) {
      flagStop = true;
    }

    int[] sizeOfresultPane = {widthScreen / 3, 5 * heightScreen / 2};
    int[] coordofresultPane = {widthScreen / 3, heightScreen / 3};
    int[] offsets = {4, -2};
    double[] sizeOfRectangle = {widthScreen / 3, 2 * heightScreen / 5};
    int[] sizeofEllipse = {100, 20};
    double[] coordofEllipse = {widthScreen / 6, 3 * heightScreen / 50};
    double[] coordOfTextGameOver = {5 * widthScreen / 47, 10 * heightScreen / 147};
    double[] coordOfTextMyScore = {widthScreen / 18, heightScreen / 5};
    double[] coordOfTextBestScore = {widthScreen / 18, 3 * heightScreen / 10};

    resultPane.setPrefWidth(sizeOfresultPane[0]);
    resultPane.setPrefHeight(sizeOfresultPane[1]);
    resultPane.setTranslateX(coordofresultPane[0]);
    resultPane.setTranslateY(coordofresultPane[1]);

    DropShadow dp = new DropShadow();
    dp.setColor(Color.BLACK);
    dp.setOffsetX(offsets[0]);
    dp.setOffsetY(offsets[1]);

    DropShadow ds = new DropShadow();
    ds.setColor(Color.BLACK);
    ds.setOffsetY(offsets[0]);

    Rectangle rect = new Rectangle();
    rect.setWidth(sizeOfRectangle[0]);
    rect.setHeight(sizeOfRectangle[1]);
    rect.setFill(Color.GOLD);
    rect.setStroke(Color.BLACK);
    rect.setStrokeWidth(2);
    rect.setEffect(ds);

    Ellipse el = new Ellipse(sizeofEllipse[0], sizeofEllipse[1]);
    el.setTranslateX(coordofEllipse[0]);
    el.setTranslateY(coordofEllipse[1]);
    el.setFill(Color.ORANGE);
    el.setEffect(null);
    el.setEffect(dp);

    Text goText = new Text("GAME OVER");
    goText.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 20));
    goText.setFill(Color.DARKRED);
    goText.setTranslateX(coordOfTextGameOver[0]);
    goText.setTranslateY(coordOfTextGameOver[1]);
    goText.setTextAlignment(TextAlignment.CENTER);
    goText.setStroke(Color.BLACK);

    Text myScore = new Text();
    myScore.setText("SCORE : " + score);
    myScore.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 30));
    myScore.setFill(Color.WHITE);
    myScore.setTranslateX(coordOfTextMyScore[0]);
    myScore.setTranslateY(coordOfTextMyScore[1]);
    myScore.setStroke(Color.BLACK);

    checkResultAndWrite();

    Text bestScore = new Text();
    bestScore.setText("BEST : " + Rating.getResult(modeOfGame));
    bestScore.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 30));
    bestScore.setFill(Color.WHITE);
    bestScore.setTranslateX(coordOfTextBestScore[0]);
    bestScore.setTranslateY(coordOfTextBestScore[1]);
    bestScore.setStroke(Color.BLACK);

    resultPane.getChildren().addAll(rect, myScore, bestScore);
    resultPane.getChildren().addAll(el, goText);
    appRoot.getChildren().addAll(resultPane);
    if (!humanPlaying && !replayGame) {
      fw.writeInFile(ReplayEnum.getType(ReplayEnum.TIME), counterOfTime, tempFile);
      fw.writeInFile(ReplayEnum.getType(ReplayEnum.ESC), tempFile);
      try {
        fw.saveFile(tempFile, score);
      } catch (Exception e) {
        e.printStackTrace();
      }
      timer.stop();
      counterOfTime = 0;
      intitalAddingItemesToGame();
    }
  }

  private String getString(int a) {
    return "" + a;
  }

  /** Save and Read in file */
  private void checkResultAndWrite() {
    if (Rating.getResult(modeOfGame).length() < getString(score).length()) {
      Rating.setResult(getString(score), modeOfGame);
    } else if (Rating.getResult(modeOfGame).length() > getString(score).length()) {
      return;
    } else if (Rating.getResult(modeOfGame).length() == getString(score).length()) {
      if (Rating.getResult(modeOfGame).compareTo(getString(score)) < 0) {
        Rating.setResult(getString(score), modeOfGame);
      }
    }
  }

  /** printing pause message */
  private void printPause() {

    int number_of_msgs = 2;
    int coordXofPauseText = widthScreen / 3;
    int[] coordYofPauseText = {heightScreen / 3 + 40, heightScreen / 3 + 80};

    for (int i = 0; i < number_of_msgs; i++) {
      pauseText[i] = new Text();
    }
    pauseText[0].setText("PAUSE");
    pauseText[1].setText("Press F10 to continue...");
    pauseText[0].setTranslateY(coordYofPauseText[0]);
    pauseText[1].setTranslateY(coordYofPauseText[1]);
    pauseText[0].setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 100));
    pauseText[1].setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 50));
    for (int i = 0; i < number_of_msgs; i++) {
      pauseText[i].setTranslateX(coordXofPauseText);
      pauseText[i].setFill(Color.WHITE);
      appRoot.getChildren().addAll(pauseText[i]);
    }
  }

  /** back to menu */
  public void backToMenu() {
    timer.stop();
    wallsPassed = 0;
    gameOver = false;
    humanPlaying = false;
    if (!replayGame) {
      counterOfTime = 0;
    }
    gamestage.setScene(new Scene(Menu.createContent()));
    gamestage.show();
  }

  /** Pushing items to Scene */
  private void newGameContent() {
    appRoot = new Pane();
    appRoot.setPrefSize(widthScreen, heightScreen);
    appRoot.setStyle("-fx-background-color: #4EC0CA");
    bird = new Bird(modeOfGame);
    appRoot.getChildren().addAll(bird.getGraphics(), scoreLabel);
    intitalAddingItemesToGame();
  }

  /** Update in game */
  public void update() {

    double rateOfSun = 0.3;

    if (replayGame && flagExit) {
      backToMenu();
    }
    /** Checking disappearing of walls */
    for (Wall w : walls) {
      if (w.getTranslateX() + w.getWallWidth() < 0) {
        walls.remove(w);
        wallsPassed--;
        wallNumber--;
        // two walls passed
        if (wallNumber % 2 == 0) {
          if (!replayGame) {
            addWall();
          }
        }
        break;
      }
    }
    /** Checking disappering of clouds */
    for (int i = 0; i < NUMBER_OF_CLOUDS; i++) {
      if (cloud[i].getTranslateX() + cloud[i].getFitWidth() < 0) {
        cloud[i].setTranslateX(widthScreen);
      }
      cloud[i].setTranslateX(cloud[i].getTranslateX() - 1);
    }
    if (sun.getTranslateX() + sun.getFitWidth() < 0) {
      sun.setTranslateX(widthScreen);
    } else {
      sun.setTranslateX(sun.getTranslateX() - rateOfSun);
    }
    scoreLabel.setText("" + score);
    checkColission();
  }

  public void stopAll() {
    if (replayGame) {
      replay.timer.stop();
    }
    timer.stop();
    bird.stopBird();
    for (Wall w : walls) {
      w.stopWall();
    }
  }

  public void startAll() {
    if (replayGame) {
      replay.timer.start();
    }
    timer.start();
    bird.startBird();
    for (Wall w : walls) {
      w.startWall();
    }
  }

  void addWallFromReplay(int numberOfLines) {

    final int COORD_Y_FILE = 0;
    final int HEIGHT_FILE = 1;

    int heightTopWall = (int) fw.readFromFile(fileFromLoad, numberOfLines)[HEIGHT_FILE];
    int heightDownWall = (int) fw.readFromFile(fileFromLoad, numberOfLines + 1)[HEIGHT_FILE];
    Wall wTop = new Wall(heightTopWall, rateOfWalls);
    Wall wDown = new Wall(heightDownWall, rateOfWalls);
    if (wallNumber == 0) {
      wTop.setTranslateX(widthScreen);
      wDown.setTranslateX(widthScreen);
    } else {
      wTop.setTranslateX((walls.get(wallNumber - 1).getTranslateX() + DISTANSE_BETWEEN_WALLS));
      wDown.setTranslateX((walls.get(wallNumber - 1).getTranslateX() + DISTANSE_BETWEEN_WALLS));
    }

    wTop.setTopCoordinate(fw.readFromFile(fileFromLoad, numberOfLines)[HEIGHT_FILE]);
    wTop.setTranslateY(fw.readFromFile(fileFromLoad, numberOfLines)[COORD_Y_FILE]);
    walls.add(wTop);
    wallNumber++;
    list.clear();
    list.add(wTop.getTranslateX());
    list.add(wTop.getTop());
    System.out.println(notation.parseNotation(list));

    wDown.setTopCoordinate(4 * heightScreen / 5 - heightDownWall);
    wDown.setTranslateY(4 * heightScreen / 5 - heightDownWall);
    walls.add(wDown);
    wallNumber++;
    list.clear();
    list.add(wDown.getTranslateX());
    list.add(wDown.getTop());
    System.out.println(notation.parseNotation(list));

    appRoot.getChildren().addAll(wTop, wDown);
  }

  /** adding wall */
  void addWall() {

    int heightTop;
    int rangeOfWallHeight = 4 * heightScreen / 5 - hole;
    int minHeight = heightScreen / 15;
    int maxHeight = heightScreen / 3;
    heightTop = new Random().nextInt(rangeOfWallHeight);
    if (heightTop < minHeight) {
      heightTop = minHeight;
    }
    if (heightTop > maxHeight) {
      heightTop = maxHeight;
    }
    int heightDown = rangeOfWallHeight - heightTop;
    if (numOfWalls % 12 == 0 && numOfWalls > 0) {
      heightDown += new Random().nextInt(200);
    }

    Wall wTop = new Wall(heightTop, rateOfWalls);
    Wall wDown = new Wall(heightDown, rateOfWalls);
    if (wallNumber == 0) {
      wTop.setTranslateX(widthScreen);
      wDown.setTranslateX(widthScreen);
    } else {
      wTop.setTranslateX((walls.get(wallNumber - 1).getTranslateX() + DISTANSE_BETWEEN_WALLS));
      wDown.setTranslateX((walls.get(wallNumber - 1).getTranslateX() + DISTANSE_BETWEEN_WALLS));
    }
    wTop.setTopCoordinate(heightTop);
    wTop.setTranslateY(0);
    walls.add(wTop);
    list.clear();
    list.add(wTop.getTranslateX());
    list.add(wTop.getTop());
    System.out.println(notation.parseNotation(list));
    fw.writeInFile(ReplayEnum.getType(ReplayEnum.TIME), counterOfTime, tempFile);
    fw.writeInFile(ReplayEnum.getType(ReplayEnum.WALL), wTop.getTranslateY(), heightTop, tempFile);
    wallNumber++;
    numOfWalls++;
    
    
    wDown.setTopCoordinate(4 * heightScreen / 5 - heightDown);
    wDown.setTranslateY(4 * heightScreen / 5 - heightDown);
    walls.add(wDown);
    fw.writeInFile(ReplayEnum.getType(ReplayEnum.WALL), wDown.getTranslateY(), heightDown,
        tempFile);
    list.clear();
    list.add(wDown.getTranslateX());
    list.add(wDown.getTop());
    System.out.println(notation.parseNotation(list));
    
    wallNumber++;
    numOfWalls++;

    appRoot.getChildren().addAll(wTop, wDown);

  }

  /** Check Bird collision with ground,walls,sky, etc. */
  void checkColission() {

    int boundOfGroundY = 4 * heightScreen / 5;
    if (walls.isEmpty()) {
      return;
    }
    double wallWidth = walls.get(wallsPassed).getWallWidth();


    /** CHECK IF PLAYS BOT */
    if (!humanPlaying) {
      checkColissionForBot();
    }
    /** CHECK IF PLAYS HUMAN */

    /** With LeftSide */
    if (bird.getGraphics().getBoundsInParent()
        .intersects(walls.get(wallsPassed).getBoundsInParent())
        || bird.getGraphics().getBoundsInParent()
            .intersects(walls.get(wallsPassed + 1).getBoundsInParent())) {
      stopAll();
      gameOver = true;
      printResult();
      return;
    }
    /** With top and bottom of walls */
    if (bird.getGraphics().getTranslateX() > walls.get(wallsPassed).getTranslateX()
        && bird.getGraphics().getTranslateX() < walls.get(wallsPassed).getTranslateX() + wallWidth
        && bird.getGraphics().getTranslateY() < walls.get(wallsPassed).getTop() - 5
        || bird.getGraphics().getTranslateX() > walls.get(wallsPassed + 1).getTranslateX()
            && bird.getGraphics().getTranslateX() < walls.get(wallsPassed + 1).getTranslateX()
                + wallWidth
            && bird.getGraphics().getTranslateY() > walls.get(wallsPassed + 1).getTop() + 5) {
      stopAll();
      gameOver = true;
      printResult();
      return;
    }
    /** Incrementing Score */
    if (bird.getGraphics().getTranslateX() >= walls.get(wallsPassed).getTranslateX() + wallWidth / 2
        && bird.getGraphics().getTranslateX() >= walls.get(wallsPassed + 1).getTranslateX()
            + wallWidth / 2) {
      scoreLabel.setText("" + score);
      score++;
      wallsPassed += 2;
      return;
    }
    /** With ground */
    if (bird.getGraphics().getTranslateY() + bird.getHeight() > boundOfGroundY) {
      stopAll();
      bird.startFall();
      gameOver = true;
      printResult();
      return;
    }
    /** With Sky */
    if (bird.getGraphics().getTranslateY() < 0) {
      bird.getGraphics().setTranslateY(bird.getGraphics().getTranslateY() - 2);
      bird.startFall();
      return;
    }
  }

  /** check collision for bot */
  void checkColissionForBot() {
    if (bird.getGraphics().getTranslateY() + 2 * hole / 5 > walls.get(wallsPassed + 1).getTop()) {
      if (!replayGame) {
        fw.writeInFile(ReplayEnum.getType(ReplayEnum.TIME), counterOfTime, tempFile);
        fw.writeInFile(ReplayEnum.getType(ReplayEnum.FLAPPY), tempFile);
      }
      bird.jumpflappy();
      return;
    }
  }

  /** refresh items and pushing again */
  void intitalAddingItemesToGame() {
    if (!replayGame) {
      tempFile = fw.getTempFile();
      fw.freeFiles(tempFile);
      fw.writeInFile(ReplayEnum.getType(ReplayEnum.MODE), modeOfGame, tempFile);
    }
    walls.clear();
    appRoot.getChildren().clear();
    scoreLabel.setText("0");
    wallsPassed = 0;
    score = 0;
    numOfWalls = 0;
    gameOver = false;
    settingLayoutBird();
    settingLayoutSun();
    settingLayoutClouds();
    settingLayoutWalls();
    loadGroundPicture();
    loadPauseText();
    bird.stopAndPlayBird();
    timer.start();
    appRoot.getChildren().addAll(groundview, bird.getGraphics(), scoreLabel, helpTextDownPause);
  }

  public void settingLayoutBird() {
    double[] coordOfBird = {widthScreen / 6, 3 * heightScreen / 10};
    bird.getGraphics().setTranslateX(coordOfBird[0]);
    bird.getGraphics().setTranslateY(coordOfBird[1]);
  }

  public void settingLayoutSun() {
    int[] coordOfSun = {widthScreen / 2, 0};
    int[] sizeOfSun = {3 * heightScreen / 20, 3 * heightScreen / 20};
    sun = new ImageView();
    try (InputStream is = Files.newInputStream(Paths.get(SUN_PICTURE))) {
      sun = new ImageView(new Image(is));
      sun.setTranslateX(coordOfSun[0]);
      sun.setTranslateY(coordOfSun[1]);
      sun.setFitWidth(sizeOfSun[0]);
      sun.setFitHeight(sizeOfSun[1]);
      sun.setOpacity(0.6);
    } catch (IOException e) {
      System.err.println("Caught IOException: " + e.getMessage());
      return;
    }
    appRoot.getChildren().addAll(sun);
  }

  public void settingLayoutClouds() {
    int[] sizeOfCloud = {widthScreen / 3, heightScreen / 5};
    int randNumX = 700;
    int randNumY = 20;
    int distX = 100;
    int distY = 10;
    /** Setting a layout of clouds */
    for (int i = 0; i < NUMBER_OF_CLOUDS; i++) {
      try (InputStream is = Files.newInputStream(Paths.get(CLOUD_PICTURE))) {
        cloud[i] = new ImageView(new Image(is));
        cloud[i].setTranslateX(i * new Random().nextInt(randNumX) + distX);
        cloud[i].setTranslateY(i * new Random().nextInt(randNumY) + distY);
        cloud[i].setFitWidth(sizeOfCloud[0]);
        cloud[i].setFitHeight(sizeOfCloud[1]);
        cloud[i].setOpacity(0.6);
        appRoot.getChildren().addAll(cloud[i]);
      } catch (IOException e) {
        System.err.println("Caught IOException: " + e.getMessage());
        return;
      }
    }
  }

  private void settingLayoutWalls() {
    /** Setting a layout of walls */
    wallNumber = 0;
    if (!replayGame) {
      for (int i = 0; i < NUMBER_OF_WALLS; i++) {
        addWall();
      }
    }
  }

  private void loadPauseText() {
    double[] coordOfHelpingText = {20, heightScreen - 2 * heightScreen / 25};
    helpTextDownPause.setText("F10 - Pause\nEsc - Back To Menu");
    helpTextDownPause.setTranslateX(coordOfHelpingText[0]);
    helpTextDownPause.setTranslateY(coordOfHelpingText[1]);
    helpTextDownPause.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 20));
    helpTextDownPause.setFill(Color.WHITE);
  }

  private void loadGroundPicture() {
    int[] coordXofGround = {0, 4 * heightScreen / 5 - 1};
    groundview = new ImageView();
    try (InputStream is = Files.newInputStream(Paths.get(GROUND_PICTURE))) {
      groundview = new ImageView(new Image(is));
      groundview.setTranslateX(coordXofGround[0]);
      groundview.setTranslateY(coordXofGround[1]);
      groundview.setFitWidth(widthScreen);
      groundview.setFitHeight(heightScreen / 5);
    } catch (IOException e) {
      System.err.println("Caught IOException: " + e.getMessage());
      return;
    }
  }
}
