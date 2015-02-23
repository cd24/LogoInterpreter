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
        System.out.println("Tree name: " + parseTree.getName() + ", String value: " + parseTree.toString());
        if (parseTree.isNamed("lines") || parseTree.isNamed("line")){
            return interpret(parseTree.getChild(0));
        }
        if (parseTree.isNamed("num")){
            if (parseTree.hasNamed("variable")){
                return interpret(parseTree.getChild(0));
            }
            return Integer.parseInt(parseTree.toString());
        }
        else if (parseTree.isNamed("expr")) {
            return interpret(parseTree.getChild(0));
        }

        else if (parseTree.isNamed("assignment")){
            String variableName = parseTree.getNamedChild("variable").toString();
            Integer value = interpret(parseTree.getNamedChild("addSubOp"));
            variables.put(variableName, value);
            Controller.publicVariables.put(variableName, value);
        }

        else if (parseTree.isNamed("variable")) {
            if (variables.containsKey(parseTree.toString())){
                return variables.get(parseTree.toString());
            }
            else if (Controller.publicVariables.containsKey(parseTree.toString())){
                return Controller.publicVariables.get(parseTree.toString());
            }
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
            return interpret(parseTree.getChild(0));
        }

        else if (parseTree.isNamed("num")){
            if (parseTree.hasNamed("variable")){
                return interpret(parseTree.getChild(0));
            }
            else {
                return Integer.parseInt(parseTree.toString());
            }
        }

        else if (parseTree.isNamed("loop")){
            int numRepeats = interpret(parseTree.getNamedChild("addSubOp"));
            System.out.println("Called, looping for " + numRepeats + " times!");
            for (int i = 0; i < numRepeats; ++i){
                interpret(parseTree.getNamedChild("cmd"));
            }
        }

        else if (parseTree.isNamed("cmd")) {
            if (parseTree.getNumChildren() == 1){
                handleCommand(parseTree);
            }
            else {
                interpret(parseTree.getNamedChild("cmd"));
                handleCommand(parseTree);
            }
        }

        else if (parseTree.isNamed("if")){
            boolean condition = evaluateBooleanExpression(parseTree.getNamedChild("boolNor"));
            System.out.println("HERE :)  Evalutating condition to: " + condition);
            if (condition){
                interpret(parseTree.getNamedChild("block"));
            }
        }

        else if (parseTree.isNamed("ifelse")){
            boolean condition = evaluateBooleanExpression(parseTree.getNamedChild("boolNor"));
            if (condition) {
                interpret(parseTree.getNamedChild("block"));
            }
            else {
                interpret(parseTree.getNamedChild("elseBlock"));
            }
        }
        return 0;
    }


    public boolean evaluateBooleanExpression(Tree parseTree){
        //todo: implement
        if (parseTree.isNamed("boolCond")){
            int first = interpret(parseTree.getChild(0));
            int second = interpret(parseTree.getLastChild());

            String comparator = parseTree.getNamedChild("boolOp").toString();
            System.out.println("First, second, operator " + first + ", " + second + ", " + comparator);

            if (comparator.equals(">")){
                return first > second;
            }
            else if (comparator.equals("<")){
                return first < second;
            }
            else if (comparator.equals(">=")){
                return first >= second;
            }
            else if (comparator.equals("<=")){
                return first <= second;
            }
            else if (comparator.equals("==")){
                return first == second;
            }
        }

        else if (parseTree.isNamed("boolNor")){
            if (parseTree.getNumChildren() == 1){
                return evaluateBooleanExpression(parseTree.getChild(0));
            }
            else {
                return !(evaluateBooleanExpression(parseTree.getChild(0)) || evaluateBooleanExpression(parseTree.getLastChild()));
            }
        }
        else if (parseTree.isNamed("boolOr")){
            if (parseTree.getNumChildren() == 1){
                return evaluateBooleanExpression(parseTree.getChild(0));
            }
            else {
                return evaluateBooleanExpression(parseTree.getChild(0)) || evaluateBooleanExpression(parseTree.getLastChild());
            }
        }
        else if (parseTree.isNamed("boolAnd")){
            if (parseTree.getNumChildren() == 1){
                return evaluateBooleanExpression(parseTree.getChild(0));
            }
            else {
                return evaluateBooleanExpression(parseTree.getChild(0)) && evaluateBooleanExpression(parseTree.getLastChild());
            }
        }
        return false;
    }

    public void makeLine(Point2D current, Point2D end){
        if (Controller.isPenDown()) {
            Line line = new Line();
            line.setStartX(current.getX());
            line.setStartY(current.getY());
            line.setEndX(end.getX());
            line.setEndY(end.getY());
            line.setId("line");
            line.setFill(Controller.penColor);
            canvasChildren.add(line);
            Controller.lines.add(line);
        }

        turtleImage.setTranslateX(end.getX() - Turtle.CENTER_X);
        turtleImage.setTranslateY(end.getY() - Turtle.CENTER_Y);
        turtleImage.setRotate(turtle.getRotation() + 90);


    }

    public void handleCommand(Tree parseTree){
        if (parseTree.hasNamed("fd")){
            parseTree = parseTree.getNamedChild("fd");
            int moveDist = interpret(parseTree.getNamedChild("addSubOp"));
            Point2D currentPos = turtle.asPoint();
            Point2D newPos = turtle.moveForward(moveDist);

            makeLine(currentPos, newPos);
        }
        else if (parseTree.hasNamed("bk")){
            parseTree = parseTree.getNamedChild("bk");
            int moveDist = interpret(parseTree.getNamedChild("addSubOp"));
            Point2D currentPos = turtle.asPoint();
            Point2D newPos = turtle.moveBackward(moveDist);

            makeLine(currentPos, newPos);
        }
        else if (parseTree.hasNamed("lt")){
            parseTree = parseTree.getNamedChild("lt");
            int rotateAngle = interpret(parseTree.getNamedChild("addSubOp"));
            Point2D currentPos = turtle.asPoint();
            Point2D newPos = turtle.turnLeft(rotateAngle);

            makeLine(currentPos, newPos);
        }
        else if (parseTree.hasNamed("rt")){
            parseTree = parseTree.getNamedChild("rt");
            int rotateAngle = interpret(parseTree.getNamedChild("addSubOp"));
            Point2D currentPos = turtle.asPoint();
            Point2D newPos = turtle.turnRight(rotateAngle);

            makeLine(currentPos, newPos);
        }
        else if (parseTree.hasNamed("pd")){
            if (!Controller.isPenDown()){
                Controller.setPenDown(true);
            }
        }
        else if (parseTree.hasNamed("pu")){
            if (Controller.isPenDown()){
                Controller.setPenDown(false);
            }
        }
        else if (parseTree.hasNamed("home")){
            turtleImage.setTranslateX(0);
            turtleImage.setTranslateY(0);
        }
        else if (parseTree.hasNamed("cs")){
            for (Line line : Controller.lines) {
                canvasChildren.remove(line);
            }
            this.turtleImage.setTranslateY(0);
            this.turtleImage.setTranslateX(0);
            this.turtleImage.setRotate(90);

            Turtle.getInstance().toHome();
        }
        else if (parseTree.hasNamed("st")){
            turtleImage.setVisible(true);
        }
        else if (parseTree.hasNamed("ht")){
            turtleImage.setVisible(false);
        }
    }

    public void run(){
        try {
            parse();
        } catch (ParseException e) {
            //ToDo: get information back to the user... somehow.
            e.printStackTrace();
        }
    }
}
