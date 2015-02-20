package parser;


import edu.hendrix.grambler.ParseException;
import edu.hendrix.grambler.Tree;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Line;

import java.util.HashMap;

/**
 * Created by John McAvey on 2/19/2015.
 */
public class Parser implements Runnable{

    private ImageView turtleImage;
    private LogoGrammar grammar;
    private Turtle turtle = Turtle.getInstance();
    private HashMap<String, Integer> variables;
    private ObservableList<Node> canvasChildren;
    private String commands;

    public Parser(ImageView turtle, ObservableList<Node> children, String commands){
        this.turtleImage = turtle;
        this.canvasChildren = children;
        grammar = new LogoGrammar();
        variables = new HashMap<>();
        this.commands = commands;
        System.out.println("Command: " + commands);
    }

    public void parse() throws ParseException {
        Tree parseTree = grammar.parse(commands);
        this.interpret(parseTree);
    }

    private int interpret(Tree parseTree) {
        if (parseTree.isNamed("lines") || parseTree.isNamed("line")){
            return interpret(parseTree.getChild(0));
        }
        if (parseTree.isNamed("num")){
            if (parseTree.hasNamed("variable")){
                return interpret(parseTree.getChild(0));
            }
            else {
                return Integer.parseInt(parseTree.toString());
            }
        }
        else if (parseTree.isNamed("expr")) {
            if (parseTree.hasNamed("assignment")){
                return interpret(parseTree.getChild(0));
            }

            else if (parseTree.hasNamed("cmd")){
                return interpret(parseTree.getChild(0));
            }
        }

        else if (parseTree.isNamed("assignment")){
            String variableName = parseTree.getNamedChild("variable").toString();
            Integer value = interpret(parseTree.getNamedChild("addSubOp"));
            variables.put(variableName, value);
        }

        else if (parseTree.isNamed("variable")) {
            return variables.get(parseTree.toString());
        }

        else if (parseTree.isNamed("addSubOp")){
            if (parseTree.getNumChildren() > 1){
                return interpret(parseTree.getChild(0)) + interpret(parseTree.getLastChild());
            }
            else {
                return interpret(parseTree.getChild(0));
            }
        }

        else if (parseTree.isNamed("multDivOp")){
            if (parseTree.getNumChildren() > 1){
                if (parseTree.getNamedChild("multDivD").toString().equals("*")){
                    return interpret(parseTree.getChild(0)) * interpret(parseTree.getLastChild());
                }
                else {
                    return interpret(parseTree.getChild(0)) / interpret(parseTree.getLastChild());
                }
            }
            else {
                return interpret(parseTree.getChild(0));
            }
        }

        else if (parseTree.isNamed("paren")){
            if (parseTree.hasNamed("num")){
                return Integer.parseInt(parseTree.getNamedChild("num").toString());
            }
            else {
                return interpret(parseTree.getChild(0));
            }
        }

        else if (parseTree.isNamed("cmd")) {
            if (parseTree.hasNamed("fd")){
                parseTree = parseTree.getChild(0);
                int moveDist = interpret(parseTree.getNamedChild("addSubOp"));
                Point2D currentPos = turtle.asPoint();
                Point2D newPos = turtle.moveForward(moveDist);

                makeLine(currentPos, newPos);
            }
            else if (parseTree.hasNamed("bk")){
                parseTree = parseTree.getChild(0);
                int moveDist = interpret(parseTree.getNamedChild("addSubOp"));
                Point2D currentPos = turtle.asPoint();
                Point2D newPos = turtle.moveBackward(moveDist);

                makeLine(currentPos, newPos);
            }
            else if (parseTree.hasNamed("lt")){
                parseTree = parseTree.getChild(0);
                int rotateAngle = interpret(parseTree.getNamedChild("addSubOp"));
                Point2D currentPos = turtle.asPoint();
                Point2D newPos = turtle.turnLeft(rotateAngle);

                makeLine(currentPos, newPos);
            }
            else if (parseTree.hasNamed("rt")){
                parseTree = parseTree.getChild(0);
                int rotateAngle = interpret(parseTree.getNamedChild("addSubOp"));
                Point2D currentPos = turtle.asPoint();
                Point2D newPos = turtle.turnRight(rotateAngle);

                makeLine(currentPos, newPos);
            }
            else if (parseTree.hasNamed("pd")){

            }
            else if (parseTree.hasNamed("pu")){

            }
            else if (parseTree.hasNamed("home")){

            }
            else if (parseTree.hasNamed("cs")){

            }
            else if (parseTree.hasNamed("st")){

            }
            else if (parseTree.hasNamed("ht")){

            }
        }
        return 0;
    }

    public void makeLine(Point2D current, Point2D end){
        Line line = new Line();
        line.setStartX(current.getX());
        line.setStartY(current.getY());
        line.setEndX(end.getX());
        line.setEndY(end.getY());

        turtleImage.setTranslateX(end.getX() - Turtle.CANVAS_MIDDLE_X);
        turtleImage.setTranslateY(end.getY() - Turtle.CANVAS_MIDDLE_Y);
        turtleImage.setRotate(turtle.getRotation() + 90);
        
        canvasChildren.add(line);
    }

    public void run(){
        try {
            parse();
        } catch (ParseException e) {
            //ToDo: get information back to the user... somehow.
        }
    }
}
