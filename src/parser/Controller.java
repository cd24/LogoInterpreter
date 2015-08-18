package parser;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;


public class Controller implements Initializable{

    @FXML
    Button forward;
    @FXML
    Button backward;
    @FXML
    Button left;
    @FXML
    Button right;
    @FXML
    Button reset;
    @FXML
    TabPane tabs;

    @FXML
    TextField singleCommand;
    @FXML
    TextArea passedCommands;
    @FXML
    TextArea editor;
    @FXML
    TextArea errors;

    @FXML
    Pane canvas;

    @FXML
    ImageView turtleImage;

    @FXML
    ColorPicker lineColorPicker;

    @FXML
    Slider turtleScale;
    @FXML
    Slider buttonRotation;
    @FXML
    Slider buttonDistance;

    double turtleScaleX, turtleScaleY;

    Turtle turtle = Turtle.getInstance();

    Thread evalThread = new Thread();

    public static Color penColor = Color.BLACK;

    public static HashMap<String, Integer> publicVariables = new HashMap<>();

    public static ArrayList<Line> lines = new ArrayList<>();

    public FileManager manager;


    void initialize() {
        System.out.println("Called");
        passedCommands.setEditable(false);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        passedCommands.setEditable(false);

        Point2D starting = turtle.updateForCanvasSized(430, 618);

        canvas.setOnKeyPressed((KeyEvent event) -> {
            switch (event.getCode()){
                case UP:
                    moveForward();
                    break;
                case DOWN:
                    moveBackward();
                    break;
                case LEFT:
                    moveLeft();
                    break;
                case RIGHT:
                    moveRight();
                    break;
            }
        });

        lineColorPicker.setValue(Color.BLACK);

        turtleScaleX = turtleImage.getScaleX()/2;
        turtleScaleY = turtleImage.getScaleY()/2;

        manager = new FileManager(passedCommands.getScene());

        canvas.requestFocus();
        System.out.println("X, Y: " + starting.getX() + " , " + starting.getY());
        System.out.println("Height, Width: " + canvas.getHeight() + " , " + canvas.getWidth());
    }

    @FXML
    void moveForward() {
        this.updateTurtle(turtle.asPoint(), turtle.moveForward(buttonDistance.getValue()));
    }
    @FXML
    void moveLeft() {
        this.updateTurtle(turtle.asPoint(), turtle.turnLeft(buttonRotation.getValue()));
    }
    @FXML
    void moveRight() {
        this.updateTurtle(turtle.asPoint(), turtle.turnRight(buttonRotation.getValue()));
    }
    @FXML
    void moveBackward() {
        this.updateTurtle(turtle.asPoint(), turtle.moveBackward(buttonDistance.getValue()));
    }
    @FXML
    void resetBoard() {
        if (evalThread.isAlive()){
            evalThread.interrupt();
        }
        clearScreen();
    }

    @FXML
    void changeColor() {
        Controller.penColor = lineColorPicker.getValue();
    }

    void updateTurtle(Point2D start, Point2D end){
        createLine(start, end);

        turtleImage.setTranslateY(end.getY() - Turtle.CENTER_Y);
        turtleImage.setTranslateX(end.getX() - Turtle.CENTER_X);
        turtleImage.setRotate(turtle.getRotation() + 90);

        System.out.println("X, Y: " + end.getX() + ", " + end.getY() + ", " + turtle.getRotation());
    }

    void createLine(Point2D start, Point2D end){
        if (Turtle.penDown) {
            Line connector = new Line();
            connector.setStartX(start.getX());
            connector.setStartY(start.getY());
            connector.setEndX(end.getX());
            connector.setEndY(end.getY());
            connector.setStroke(Controller.penColor);
            canvas.getChildren().add(connector);
            lines.add(connector);
        }
    }

    @FXML
    void issueCommand(String command){
        Parser parser = new Parser(turtleImage, canvas.getChildren(), command.toLowerCase()); //lower for case-insensitive
        parser.parent = this;
        Platform.runLater(parser);
        passedCommands.appendText(command + "\n");
    }

    @FXML
    void giveSingleCommand() {
        if (singleCommand.getText().length() > 0) {
            issueCommand(singleCommand.getText());
        }
        singleCommand.setText("");
    }

    @FXML
    void clearScreen() {
        for (Line line : lines){
            canvas.getChildren().remove(line);
        }

        turtleImage.setTranslateX(0);
        turtleImage.setTranslateY(0);
        turtleImage.setRotate(90);

        Turtle.getInstance().toHome();
    }

    @FXML
    void runScript() {
        String script = editor.getText();
        issueCommand(script);
    }

    @FXML
    public void changeTurtleScale() {
        double newScale = turtleScale.getValue();
        turtleImage.setScaleX(turtleScaleX * newScale);
        turtleImage.setScaleY(turtleScaleY * newScale);
    }

    @FXML
    void resetDefaultSettings() {
        this.turtleScale.setValue(0.5);
        this.buttonDistance.setValue(10);
        this.buttonRotation.setValue(45);
        this.lineColorPicker.setValue(Color.BLACK);
    }

    @FXML
    void saveToFile() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text File", "*.txt"));
        File path = fc.showSaveDialog(turtleImage.getScene().getWindow());
        manager.writeToFile(path, editor.getText());
    }

    @FXML
    void readFromFile(){
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text File", "*.txt"));
        File path = fc.showOpenDialog(turtleImage.getScene().getWindow());
        String text = manager.readFromFile(path);
        if (text.length() > 0){
            editor.setText(text);
        }
    }

    @FXML
    void saveToImage() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPEG Image", "*.jpeg"));
        File path = fc.showSaveDialog(turtleImage.getScene().getWindow());
        turtleImage.setVisible(false);
        try {
            WritableImage image = canvas.snapshot(new SnapshotParameters(), null);
            BufferedImage bi = SwingFXUtils.fromFXImage(image, null);
            BufferedImage rgbvals = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.OPAQUE);
            Graphics2D graphics2D = rgbvals.createGraphics();
            graphics2D.drawImage(bi, 0, 0, null);
            ImageIO.write(rgbvals, "jpeg", path);
            graphics2D.dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }
        turtleImage.setVisible(true);
    }

    @FXML
    void interruptThread() {
        //todo: thread, then allow interrupting.
    }

    public void showError(String error){
        errors.appendText(error + "\n");
        tabs.getSelectionModel().selectLast();
    }

    public String loadHelp(){
        URL pathToHelp = getClass().getResource("turtleInstructions.txt");
        File helpFile = new File(pathToHelp.getPath());
        String help = manager.readFromFile(helpFile);
        return help;
    }

    public void showHelp(){
        //todo: make modal
        showError(loadHelp());
    }

}
