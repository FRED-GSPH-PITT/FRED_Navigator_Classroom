/**
 * FRED Navigator
 * Copyright (c) 2010-2013, University of Pittsburgh Public Health Dynamics Lab
 * This code was created by David Galloway, Jack Paparian, and Kirsten Taing
 * Licensed under the BSD 3-Clause license.  See the file "LICENSE" for 
 * more information.
 */

/**
 * File: FredNavigatorTopic1.java
 * Date last modified: 10/16/13 
 *
 * The functionality of Topic 1
 */

package edu.pitt.phdl.frednavigator.topic1;

import edu.pitt.phdl.frednavigator.FredNavigator;
import edu.pitt.phdl.frednavigator.FredNavigatorContext;
import edu.pitt.phdl.frednavigator.htmlviewer.HtmlControl;
import edu.pitt.phdl.frednavigator.media.MediaControl;
import edu.pitt.phdl.frednavigator.util.FredNavigatorConstants;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.logging.Level;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Popup;

public class Topic1Controller implements Initializable {

  String r0Value = "1.5";
  String baselineR0Value = "1.5";
  String fileVariable;
  int currentQuestion = 0;

  MediaPlayer mediaPlayer;

  @FXML
  private TabPane tabPaneTopic1;
  @FXML
  private Tab tabInfo;
  @FXML
  private Tab tabSEIR;
  @FXML
  private BorderPane brdrPaneSEIR;
  @FXML
  private Tab tabIncidence;
  @FXML
  private BorderPane brdrPaneIncidence;
  @FXML
  private Tab tabAttackRate;
  @FXML
  private BorderPane brdrPaneAttackRate;
  @FXML 
  private Tab tabPrevalence;
  @FXML 
  private Tab tabPrediction;
  @FXML
  private BorderPane brdrPanePrevalence;
  @FXML
  private Label lblR0;
  @FXML
  private Slider r0Slider;
  @FXML
  private Button btnPlayMovie;
  
  //Prediction Tab controls
  @FXML
  private VBox vBoxPrediction;
  @FXML
  private HBox hBoxPredictionNext;
  @FXML
  private Button btnPredictionNext;
  
  @FXML
  private MenuItem mnItmSetBaseline;
  @FXML
  private MenuItem mnItmToggleBaseline;
  
  private Node activeBaseline0;
  private Node activeBaseline1;
  private boolean baselineVisible;
  
  @FXML
  private Circle buttonAR, buttonARs, buttonC, buttonCs, buttonP;
  @FXML
  private Label labelAR, labelARs, labelC, labelCs, labelP;
    
  //Prediction Tab Dynamic Controls
  private ImageView predictionImgVw;
  private Text questionTxt;
  private Text answerTxt;
  private HBox hBoxPredictionImg;
  private HBox hBoxQuestion;
  private HBox hBoxAnswer;
  
  /**
   * Closes the application
   * 
   * @param event 
   */
  @FXML
  private void closeApplication(ActionEvent event)
  {
    System.exit(0);
  }
    
  /**
   * Go back to the homepage
   * 
   * @param event 
   */
  @FXML
  private void startPage(ActionEvent event)
  {
    try
    {
      Parent homepageLayout;
      homepageLayout = FXMLLoader.load(this.getClass().getResource(FredNavigatorConstants.HOMEPAGE_FXML));
      Scene hompageScene = new Scene(homepageLayout);
      FredNavigatorContext.getInstance().getFredNavigatorStage().setScene(hompageScene);
      FredNavigatorContext.getInstance().getFredNavigatorStage().setTitle(FredNavigatorConstants.HOMEPAGE_TITLE_TEXT);
      FredNavigatorContext.getInstance().getFredNavigatorStage().setResizable(false);
    }
    catch (IOException ex)
    {
      if(FredNavigator.isLoggingEnabled)
      {
        FredNavigator.LOGGER.log(Level.SEVERE, ex.getMessage());
      }
    }
  }
  
  /**
   * Show the about page
   * 
   */
  @FXML
  private void aboutPage(ActionEvent event)
  {
    Scene htmlScene = new Scene(new HtmlControl("about_fred.html"), 800, 700, Color.web("#666970"));
    FredNavigatorContext.getInstance().getFredHtmlStage().setTitle("About FRED");
    FredNavigatorContext.getInstance().getFredHtmlStage().setScene(htmlScene);
    FredNavigatorContext.getInstance().getFredHtmlStage().show();
    FredNavigatorContext.getInstance().getFredNavigatorStage().toBack();
    FredNavigatorContext.getInstance().getFredHtmlStage().toFront();
  }
    
  /**
   * Plays the movie
   * 
   */
  private void play()
  {
    MediaPlayer.Status status = this.mediaPlayer.getStatus();
    if (status == MediaPlayer.Status.UNKNOWN
            || status == MediaPlayer.Status.HALTED)
    {
      return;
    }

    if (status == MediaPlayer.Status.PAUSED
            || status == MediaPlayer.Status.STOPPED
            || status == MediaPlayer.Status.READY)
    {
      this.mediaPlayer.play();
    }
  }
  
  /**
   * Lets the user set the values for the baseline curves
   */
  @FXML
  private void setBaselineValues()
  {
    this.baselineR0Value = this.r0Value;
    
    ObservableList<Tab> tabs = this.tabPaneTopic1.getTabs();
    Tab selectedTab = null;
    for(Tab tab : tabs)
    {
      if(tab.isSelected())
      {
        selectedTab = tab;
        break;
      }
    }
    this.makeLineChartOnSelectedTab(selectedTab);
  }
  
  /**
   * Once the user clicks Begin to start the quiz, this navigates to each of the questions
   * 
   */
  @FXML
  private void navigatePredictionsTab()
  {
    if(this.predictionImgVw == null)
    {
      int indx = vBoxPrediction.getChildren().indexOf(this.hBoxPredictionNext);
      this.vBoxPrediction.getChildren().remove(0, indx);
      
      this.predictionImgVw = new ImageView();
      this.questionTxt = new Text();
      this.answerTxt = new Text();
      this.hBoxPredictionImg = new HBox();
      this.hBoxQuestion = new HBox();
      this.hBoxAnswer = new HBox();
      
      this.predictionImgVw.setFitHeight(400);
      this.predictionImgVw.setFitWidth(500);
      this.hBoxPredictionImg.getChildren().add(this.predictionImgVw);
      this.hBoxPredictionImg.setAlignment(Pos.CENTER);
      
      this.questionTxt.setWrappingWidth(750);
      this.questionTxt.getStyleClass().add("predictions_question_text");
      this.hBoxQuestion.getChildren().add(this.questionTxt);
      this.hBoxQuestion.setAlignment(Pos.CENTER);
              
      this.answerTxt.setWrappingWidth(750);
      this.answerTxt.getStyleClass().add("predictions_answer_text");
      this.hBoxAnswer.getChildren().add(this.answerTxt);
      this.hBoxAnswer.setAlignment(Pos.CENTER);
      this.hBoxAnswer.setPadding(new Insets(10.0, 10.0, 10.0, 10.0));
      
      this.vBoxPrediction.getChildren().add(0, this.hBoxAnswer);
      this.vBoxPrediction.getChildren().add(0, this.hBoxQuestion);     
      this.vBoxPrediction.getChildren().add(0, this.hBoxPredictionImg);
    }   
    
    if(this.btnPredictionNext.getText().equals("Begin"))
    {
      this.btnPredictionNext.setText("Next");
      this.predictionImgVw.setImage(new Image(this.getClass().getResourceAsStream("/edu/pitt/phdl/"
              + "frednavigator/resource/image/" 
              + FredNavigator.topicOneQuestions[this.currentQuestion].getImageFileName())));
      this.questionTxt.setText(FredNavigator.topicOneQuestions[this.currentQuestion].getQuestionText());
      this.answerTxt.setText(FredNavigator.topicOneQuestions[this.currentQuestion].getAnswerText().trim().replaceAll(" +", " "));
      this.currentQuestion++;
    }
    else if(this.currentQuestion < FredNavigator.topicOneQuestions.length)
    {
      this.predictionImgVw.setImage(new Image(this.getClass().getResourceAsStream("/edu/pitt/phdl/"
              + "frednavigator/resource/image/" 
              + FredNavigator.topicOneQuestions[this.currentQuestion].getImageFileName())));
      this.questionTxt.setText(FredNavigator.topicOneQuestions[this.currentQuestion].getQuestionText());
      this.answerTxt.setText(FredNavigator.topicOneQuestions[this.currentQuestion].getAnswerText().trim().replaceAll(" +", " "));
      
      if(this.currentQuestion == FredNavigator.topicOneQuestions.length - 1)
      {
        this.btnPredictionNext.setText("Finished!");
      }
      
      this.currentQuestion++;
    } 
    else
    {
      //Do Nothing
    }
  }

  /**
   * Opens the window for playing the movie
   * 
   */
  @FXML
  private void openMovie()
  {
    try
    {
      File tmpFile = new File(FredNavigator.pathToFRED + "RESULTS" 
                + FredNavigatorConstants.FS + "JOB" + FredNavigatorConstants.FS 
                + FredNavigator.keyToJob.get("R0=" + this.r0Value)
                + FredNavigatorConstants.FS + "DATA" + FredNavigatorConstants.FS
                + "REPORTS" + FredNavigatorConstants.FS + "gaia_input_animated_R0=" 
                + this.r0Value + "_P_sum.mp4");
      
      // I don't think we need this line.
      //URL tmpUrl = tmpFile.toURI().toURL();
        
      String mediaUrl = tmpFile.toURI().toString();
      this.mediaPlayer = new MediaPlayer(new Media(mediaUrl));

      this.mediaPlayer.setAutoPlay(true);
      MediaControl mediaControl = new MediaControl(this.mediaPlayer);
      mediaControl.setMinSize(720, 420);
      mediaControl.setPrefSize(720, 420);
      mediaControl.setMaxSize(720, 420);
      Group group = new Group();
      Scene scene = new Scene(group);
     
      FredNavigatorContext.getInstance().getFredMediaStage().setScene(scene);
      group.getChildren().add(mediaControl);

      FredNavigatorContext.getInstance().getFredMediaStage().show();
      FredNavigatorContext.getInstance().getFredNavigatorStage().toBack();
      FredNavigatorContext.getInstance().getFredMediaStage().toFront();
      
      this.play();
    }
    
    // Changed this to any exception because I wanted to remove a redundant line above
    catch(Exception ex)
    {
      if(FredNavigator.isLoggingEnabled)
      {
        FredNavigator.LOGGER.log(Level.SEVERE, ex.getMessage());
      }
    }
  }
  
  /**
   * Creates a popup when you hover over a line in the chart
   * 
   * @param series          The line
   * @param description     What the popup should contain
   */
  private void setPopupOnHover(final XYChart.Series series, final String description) {
      final Popup popup = new Popup();
      
      series.getNode().setOnMouseEntered(new EventHandler<MouseEvent>() {
          @Override
          public void handle(MouseEvent event) {
              series.getNode().setEffect(new Glow(.5));
              
              Label popupText = new Label();
              popupText.setText(description);
              popupText.setStyle("-fx-background-color: #FFFFFF");
              popup.getContent().clear();
              popup.getContent().add(popupText);
      
              popup.setX(event.getScreenX());
              popup.setY(event.getScreenY() - 20);
              popup.show(FredNavigatorContext.getInstance().getFredNavigatorStage());
          }
      });
      
      series.getNode().setOnMouseExited(new EventHandler<MouseEvent>() {
          @Override
          public void handle(MouseEvent event) {
              series.getNode().setEffect(null);
              popup.hide();
          }
      });
  }
  
  /**
   * Baseline either becomes hidden or visible
   * 
   */
  @FXML
  private void toggleBaselineVisible()
  {
    if (this.activeBaseline0 != null)
    {
        if (!baselineVisible)
        {
            baselineVisible = true;
            
            this.activeBaseline0.setVisible(true);
            if (activeBaseline1 != null)
                this.activeBaseline1.setVisible(true);
            this.buttonAR.setStyle("-fx-background-color: #33B3FF; -fx-fill: #33B3FF;");
            this.buttonARs.setStyle("-fx-background-color: #FF3333; -fx-fill: #FF3333;");
            this.buttonC.setStyle("-fx-background-color: #33B3FF; -fx-fill: #33B3FF;");
            this.buttonCs.setStyle("-fx-background-color: #FF3333; -fx-fill: #FF3333;");
            this.buttonP.setStyle("-fx-background-color: #33B3FF; -fx-fill: #33B3FF;");
            
            this.labelAR.setStyle("-fx-fill: #000000;");
            this.labelARs.setStyle("-fx-fill: #000000;");
            this.labelC.setStyle("-fx-fill: #000000;");
            this.labelCs.setStyle("-fx-fill: #000000;");
            this.labelP.setStyle("-fx-fill: #000000;");
            
            this.mnItmToggleBaseline.setText("Hide Baseline");
        }
        else
        {
            baselineVisible = false;
            
            this.activeBaseline0.setVisible(false);
            if (activeBaseline1 != null)
                this.activeBaseline1.setVisible(false);
            this.buttonAR.setStyle("-fx-background-color: #999999; -fx-fill: #999999;");
            this.buttonARs.setStyle("-fx-background-color: #999999; -fx-fill: #999999;");
            this.buttonC.setStyle("-fx-background-color: #999999; -fx-fill: #999999;");
            this.buttonCs.setStyle("-fx-background-color: #999999; -fx-fill: #999999;");
            this.buttonP.setStyle("-fx-background-color: #999999; -fx-fill: #999999;");
            
            this.labelAR.setStyle("-fx-text-fill: #999999;");
            this.labelARs.setStyle("-fx-text-fill: #999999;");
            this.labelC.setStyle("-fx-text-fill: #999999;");
            this.labelCs.setStyle("-fx-text-fill: #999999;");
            this.labelP.setStyle("-fx-text-fill: #999999;");
            
            this.mnItmToggleBaseline.setText("Show Baseline");
        }
    }
  }
  
  /**
   * Draws the graph that corresponds to the newly selected tab
   * 
   * @param selectedTab 
   */
  private void makeLineChartOnSelectedTab(Tab selectedTab)
  {
    //If we are on the SEIR tab
    if(selectedTab == this.tabSEIR)
    {
      NumberAxis xAxis = new NumberAxis();
      NumberAxis yAxis = new NumberAxis(0.0, 1200000.0, 100000.0);
      xAxis.setLabel("Days");
      yAxis.setLabel("Number of People");


      //creating the chart
      LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
      lineChart.setTitle("SEIR: R0=" + this.r0Value);
      lineChart.setPrefWidth(1000);
      HBox hBox = new HBox();
      hBox.getChildren().add(lineChart);
      
      XYChart.Series seriesS = makeLineChartSeries("S", "Susceptible");
      lineChart.getData().add(seriesS);
      XYChart.Series seriesE = makeLineChartSeries("E", "Exposed");
      lineChart.getData().add(seriesE);
      XYChart.Series seriesI = makeLineChartSeries("I", "Infectious");
      lineChart.getData().add(seriesI);
      XYChart.Series seriesR = makeLineChartSeries("R", "Recovered");
      lineChart.getData().add(seriesR);
      
      lineChart.setCreateSymbols(false);
      lineChart.setLegendVisible(false);
      this.brdrPaneSEIR.setCenter(hBox);
      
      activeBaseline0 = null;
      activeBaseline1 = null;
      
      setPopupOnHover(seriesS, "Susceptible: R0 = " + this.r0Value);
      setPopupOnHover(seriesE, "Exposed: R0 = " + this.r0Value);
      setPopupOnHover(seriesI, "Infectious: R0 = " + this.r0Value);
      setPopupOnHover(seriesR, "Recovered: R0 = " + this.r0Value);
    } 
    else if(selectedTab == this.tabAttackRate) 
    {
      NumberAxis xAxis = new NumberAxis();
      NumberAxis yAxis = new NumberAxis(0.0, 100.0, 5.0);
      xAxis.setLabel("Days");
      yAxis.setLabel("% Infected");

      //creating the chart
      LineChart <Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
      lineChart.setTitle("Attack Rate: R0=" + this.r0Value);
      lineChart.setPrefWidth(1000);
      HBox hBox = new HBox();
      hBox.getChildren().add(lineChart);

      XYChart.Series seriesAR = makeLineChartSeries("AR", "Attack Rate");
      lineChart.getData().add(seriesAR);
      XYChart.Series seriesARs = makeLineChartSeries("ARs", "Clinical Attack Rate");
      lineChart.getData().add(seriesARs);
      
      XYChart.Series baseAR = makeLineChartSeries("AR", "Attack Rate Baseline", this.baselineR0Value);
      lineChart.getData().add(baseAR);
      
      XYChart.Series baseARs = makeLineChartSeries("ARs", "Clinical Attack Rate Baseline", this.baselineR0Value);
      lineChart.getData().add(baseARs);
      
      baseAR.getNode().getStyleClass().add("baseline0");
      baseARs.getNode().getStyleClass().add("baseline1"); 
      
      activeBaseline0 = baseAR.getNode();
      activeBaseline1 = baseARs.getNode();
      
      // Check if baseline should be shown or not
      if (!baselineVisible) 
      {
          baseAR.getNode().setVisible(false);
          baseARs.getNode().setVisible(false);
      }
      
      lineChart.setLegendVisible(false);
      lineChart.setCreateSymbols(false);
      this.brdrPaneAttackRate.setCenter(hBox);
      
      setPopupOnHover(seriesAR, "Attack Rate: R0 = " + this.r0Value);
      setPopupOnHover(seriesARs, "Clinical Attack Rate: R0 = " + this.r0Value);
      setPopupOnHover(baseAR, "Attack Rate Baseline: R0 = " + this.baselineR0Value);
      setPopupOnHover(baseARs, "Clinical Attack Rate Baseline: R0 = " + this.baselineR0Value);
    } 
    else if(selectedTab == this.tabPrevalence) 
    {
      NumberAxis xAxis = new NumberAxis();
      NumberAxis yAxis = new NumberAxis(0.0, 550000.0, 50000.0);
      xAxis.setLabel("Days");
      yAxis.setLabel("Number of People");

      //creating the chart
      LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
      lineChart.setTitle("Prevalence: R0=" + this.r0Value);
      lineChart.setPrefWidth(1000);
      HBox hBox = new HBox();
      hBox.getChildren().add(lineChart);

      XYChart.Series seriesP = makeLineChartSeries("P", "Prevalence");
      lineChart.getData().add(seriesP);
      XYChart.Series baseP = makeLineChartSeries("P", "Prevalence Baseline", this.baselineR0Value);
      lineChart.getData().add(baseP);
      baseP.getNode().getStyleClass().add("baseline0");
      
      activeBaseline0 = baseP.getNode();
      activeBaseline1 = null;
      
      // Check if baseline should be shown or not
      if (!baselineVisible) 
      {
          baseP.getNode().setVisible(false);
      }
      
      lineChart.setCreateSymbols(false);
      lineChart.setLegendVisible(false);
      this.brdrPanePrevalence.setCenter(hBox);
      
      setPopupOnHover(seriesP, "Prevalence: R0 = " + this.r0Value);
      setPopupOnHover(baseP, "Prevalence Baseline: R0 = " + this.baselineR0Value);
    } 
    else if(selectedTab == this.tabIncidence) 
    {
      NumberAxis xAxis = new NumberAxis();
      NumberAxis yAxis = new NumberAxis(0.0, 120000.0, 10000.0);
      xAxis.setLabel("Days");
      yAxis.setLabel("Number of People");

      //creating the chart
      LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
      lineChart.setTitle("Incidence: R0=" + this.r0Value);
      lineChart.setPrefWidth(1000);
      HBox hBox = new HBox();
      hBox.getChildren().add(lineChart);
      
      XYChart.Series seriesC = makeLineChartSeries("C", "Incidence");
      lineChart.getData().add(seriesC);
      XYChart.Series seriesCs = makeLineChartSeries("Cs", "Symptomatic Incidence");
      lineChart.getData().add(seriesCs);
      
      XYChart.Series baseC = makeLineChartSeries("C", "Incidence Baseline", this.baselineR0Value);
      lineChart.getData().add(baseC);
      
      XYChart.Series baseCs = makeLineChartSeries("Cs", "Symptomatic Incidence Baseline", this.baselineR0Value);
      lineChart.getData().add(baseCs);
      
      baseC.getNode().getStyleClass().add("baseline0"); 
      baseCs.getNode().getStyleClass().add("baseline1"); 
      
      activeBaseline0 = baseC.getNode();
      activeBaseline1 = baseCs.getNode();
      
      // Check if baseline should be shown or not
      if (!baselineVisible) 
      {
          baseC.getNode().setVisible(false);
          baseCs.getNode().setVisible(false);
      }
      
      lineChart.setCreateSymbols(false);
      lineChart.setLegendVisible(false);
      this.brdrPaneIncidence.setCenter(hBox);
      
      setPopupOnHover(seriesC, "Incidence: R0 = " + this.r0Value);
      setPopupOnHover(seriesCs, "Symptomatic Incidence: R0 = " + this.r0Value);
      setPopupOnHover(baseC, "Incidence Baseline: R0 = " + this.baselineR0Value);
      setPopupOnHover(baseCs, "Symptomatic Incidence Baseline: R0 = " + this.baselineR0Value);
    } 

  }
  
  /**
   * Draws a new line on the graph whenever we change a variable to a different value
   * @param variable    symbol for the variable that is changing
   * @param seriesName  actual name of the variable
   * @return 
   */
  private XYChart.Series makeLineChartSeries(String variable, String seriesName)
  {
      return makeLineChartSeries(variable, seriesName, this.r0Value);
  }

  /**
   * Draws a new line on the graph whenever we change a variable to a different value
   * @param variable    symbol for the variable that is changing
   * @param seriesName  actual name of the variable
   * @param r0value     the r0 value associated with the variable
   * @return 
   */
  private XYChart.Series makeLineChartSeries(String variable, String seriesName, String chosenR0Value)
  {
    //defining a series
    XYChart.Series series = new XYChart.Series();
    series.setName(seriesName);

    String dataFile =
            FredNavigator.pathToFRED + "RESULTS" + FredNavigatorConstants.FS 
            + "JOB" + FredNavigatorConstants.FS 
            + FredNavigator.keyToJob.get("R0=" + chosenR0Value)
            + FredNavigatorConstants.FS + "DATA" + FredNavigatorConstants.FS 
            + "REPORTS" + FredNavigatorConstants.FS + variable + "_daily-0.dat";

    Scanner scanner = FredNavigator.getFileScanner(dataFile);
    for (int i = 0; i < 7; i++)
    {
      scanner.next();
    }

    while (scanner.hasNext())
    {
      double x = scanner.nextDouble();
      double y = scanner.nextDouble();
      series.getData().add(new XYChart.Data(x, y));

      //These variables are not used. It is needed so the
      //scanner can skip ahead
      for (int i = 0; i < 6; i++)
      {
        scanner.next();
      }
    }

    return series;
  }
  
  /**
   * This is run when Topic 1 is opened
   * 
   * @param url
   * @param rb
   */
  @Override
  public void initialize(URL url, ResourceBundle rb)
  {      
    baselineVisible = true;  
      
    // This checks the slider to see if it's moved to a different tick
    this.r0Slider.valueProperty().addListener(new ChangeListener<Number>() 
    {
      @Override
      public void changed(ObservableValue<? extends Number> ov,
              Number old_val, Number new_val)
      {
        double new_val_double = new_val.doubleValue();
        String new_val_string = (new_val_double * 10) + "";
        //Test if slider has moved to the next slider tick
        if(new_val_string.charAt(3) == '0')
        {
          r0Value = new_val_string.charAt(0) + "."
                  + new_val_string.charAt(1);
          
          ObservableList<Tab> tabs = tabPaneTopic1.getTabs();
          Tab selectedTab = null;
          for(Tab tab : tabs) {
            if(tab.isSelected()) 
            {
              selectedTab = tab;
              break;
            }
          }
          makeLineChartOnSelectedTab(selectedTab);
        }
      }
    });
    
    //Add Change Listener to Tab Pane so that when the user switches tabs, it will have the correct graph
    // already populated
    this.tabPaneTopic1.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() 
    {
      @Override 
      public void changed(ObservableValue<? extends Tab> tab, Tab oldTab, Tab newTab) 
      {
        makeLineChartOnSelectedTab(newTab);
        if(newTab == tabInfo || newTab == tabPrediction) 
        {
          r0Slider.setDisable(true);
          btnPlayMovie.setDisable(true);   
          lblR0.setDisable(true);
        }
        else
        {
          r0Slider.setDisable(false);
          btnPlayMovie.setDisable(false);
          lblR0.setDisable(false);
        }
        
        if(newTab == tabInfo || newTab == tabPrediction || newTab == tabSEIR)
        {
          mnItmSetBaseline.setDisable(true);
          mnItmToggleBaseline.setDisable(true);
        }
        else
        {
          mnItmSetBaseline.setDisable(false);
          mnItmToggleBaseline.setDisable(false);
        }
      }
    });

  }
}
