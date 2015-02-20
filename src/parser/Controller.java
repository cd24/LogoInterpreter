package parser;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;

import java.net.URL;
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

    Turtle turtle = Turtle.getInstance();

    Thread evalThread = new Thread();



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
    }

    void updateTurtle(Point2D start, Point2D end){
        createLine(start, end);

        turtleImage.setTranslateY(end.getY() - Turtle.CANVAS_MIDDLE_Y);
        turtleImage.setTranslateX(end.getX() - Turtle.CANVAS_MIDDLE_X);
        turtleImage.setRotate(turtle.getRotation() + 90);

        System.out.println("X, Y: " + end.getX() + ", " + end.getY() + ", " + turtle.getRotation());
    }

    void createLine(Point2D start, Point2D end){
        Line connector = new Line();
        connector.setStartX(start.getX());
        connector.setStartY(start.getY());
        connector.setEndX(end.getX());
        connector.setEndY(end.getY());
        canvas.getChildren().add(connector);
    }

    @FXML
    void issueCommand(String command){
        Parser parser = new Parser(turtleImage, canvas.getChildren(), command);
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
}
