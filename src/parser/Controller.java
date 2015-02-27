package parser;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

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
    TextField singleCommand;
    @FXML
    TextArea passedCommands;
    @FXML
    TextArea editor;

    @FXML
    Pane canvas;

    @FXML
    ImageView turtleImage;

    @FXML
    ColorPicker lineColorPicker;

    @FXML
    Slider turtleScale;

    double turtleScaleX, turtleScaleY;

    Turtle turtle = Turtle.getInstance();

    Thread evalThread = new Thread();

    public static boolean penDown = true;

    public static Color penColor = Color.BLACK;

    public static HashMap<String, Integer> publicVariables = new HashMap<>();

    public static ArrayList<Line> lines = new ArrayList<>();


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

        canvas.requestFocus();
        System.out.println("X, Y: " + starting.getX() + " , " + starting.getY());
        System.out.println("Height, Width: " + canvas.getHeight() + " , " + canvas.getWidth());
    }

    @FXML
    void moveForward() {
        this.updateTurtle(turtle.asPoint(), turtle.moveForward(10));
    }
    @FXML
    void moveLeft() {
        this.updateTurtle(turtle.asPoint(), turtle.turnLeft(45));
    }
    @FXML
    void moveRight() {
        this.updateTurtle(turtle.asPoint(), turtle.turnRight(45));
    }
    @FXML
    void moveBackward() {
        this.updateTurtle(turtle.asPoint(), turtle.moveBackward(10));
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
        if (penDown) {
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

    public static boolean isPenDown(){
        return penDown;
    }

    public static void setPenDown(boolean down){
        penDown = down;
    }
}
