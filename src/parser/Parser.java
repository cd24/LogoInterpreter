package parser;


import edu.hendrix.grambler.ParseException;
import edu.hendrix.grambler.Tree;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Line;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

/**
 * Created by John McAvey on 2/19/2015.
 */
public class Parser implements Runnable{

    private ImageView turtleImage;
    private LogoGrammar grammar;
    private Turtle turtle = Turtle.getInstance();
    private HashMap<String, Integer> variables;
    private HashMap<String, ArrayList<String>> functions;
    private HashMap<String, Tree> functionBlocks;
    private ObservableList<Node> canvasChildren;
    private String commands;
    private Stack<HashMap<String, Integer>> variableStack;
    public Controller parent;

    public Parser(ImageView turtle, ObservableList<Node> children, String commands){
        this.turtleImage = turtle;
        this.canvasChildren = children;
        grammar = new LogoGrammar();
        variables = new HashMap<>();
        functions = new HashMap<>();
        functionBlocks = new HashMap<>();
        this.commands = commands;
        this.variableStack = new Stack<>();
        System.out.println("Command: " + commands);
    }

    public void parse() throws ParseException {
        Tree parseTree = grammar.parse(commands);
        this.interpret(parseTree);
    }

    private int interpret(Tree parseTree) {
        if (parseTree.isNamed("lines")){
            if (parseTree.getNumChildren() == 1){
                interpret(parseTree.getChild(0));
            }
            else {
                interpret(parseTree.getNamedChild("lines"));
                interpret(parseTree.getNamedChild("line"));
            }

        }
        else if (parseTree.isNamed("line")){
            interpret(parseTree.getChild(0));
        }
        else if (parseTree.isNamed("num")){
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
            if (parseTree.hasNamed("addSubD")){
                if (parseTree.getNamedChild("addSubD").toString().equals("+")) {
                    return interpret(parseTree.getChild(0)) + interpret(parseTree.getNamedChild("multDivOp"));
                }
                else if (parseTree.getNamedChild("addSubD").toString().equals("-")){
                    return interpret(parseTree.getChild(0)) - interpret(parseTree.getNamedChild("multDivOp"));
                }
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
            if (parseTree.hasNamed("num")) {
                return interpret(parseTree.getNamedChild("num"));
            }
            else {
                return interpret(parseTree.getChild(0));
            }
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
                interpret(parseTree.getNamedChild("block"));
            }
        }

        else if (parseTree.isNamed("block")){
            interpret(parseTree.getNamedChild("lines"));
        }

        else if (parseTree.isNamed("functionCall")) {
            variableStack.push(variables);
            if (parseTree.hasNamed("functionCall")){
                interpret(parseTree.getNamedChild("functionCall"));
                handleCommand(parseTree);
            }
            else {
                handleCommand(parseTree);
            }
            variables = variableStack.pop();
        }

        else if (parseTree.isNamed("if")){
            boolean condition = evaluateBooleanExpression(parseTree.getNamedChild("boolNor"));
            if (condition){
                interpret(parseTree.getNamedChild("block"));
            }
        }

        else if (parseTree.isNamed("ifelse")){
            boolean condition = evaluateBooleanExpression(parseTree.getNamedChild("boolCond"));
            if (condition) {
                interpret(parseTree.getNamedChild("block"));
            }
            else {
                interpret(parseTree.getNamedChild("elseBlock"));
            }
        }

        else if (parseTree.isNamed("functionDecl")){
            ArrayList<String> variables = setVariableName(parseTree.getNamedChild("funcVars"));
            String name = parseTree.getNamedChild("fname").toString();
            functions.put(name, variables);
            functionBlocks.put(name, parseTree.getNamedChild("block"));
        }

        return 0;
    }


    public boolean evaluateBooleanExpression(Tree parseTree){
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
            line.setStroke(Controller.penColor);
            canvasChildren.add(line);
            Controller.lines.add(line);
        }

        turtleImage.setTranslateX(end.getX() - Turtle.CENTER_X);
        turtleImage.setTranslateY(end.getY() - Turtle.CENTER_Y);
        turtleImage.setRotate(turtle.getRotation() + 90);


    }

    public void handleCommand(Tree parseTree){
        String commandName = parseTree.getNamedChild("fname").toString();
        if (commandName.equals("fd") || commandName.equals("forward")){
            if (parseTree.hasNamed("fvars")){
                ArrayList<Integer> moves = getVarValues(parseTree.getNamedChild("fvars"));
                int moveDist = moves.get(0);
                Point2D currentPos = turtle.asPoint();
                Point2D newPos = turtle.moveForward(moveDist);

                makeLine(currentPos, newPos);
            }
            else {
                parent.showError(commandName + " requires an argument, none provided");
            }

        }
        else if (commandName.equals("bk") || commandName.equals("backward")){
            if (parseTree.hasNamed("fvars")){
                ArrayList<Integer> moves = getVarValues(parseTree.getNamedChild("fvars"));
                int moveDist = moves.get(0);
                Point2D currentPos = turtle.asPoint();
                Point2D newPos = turtle.moveBackward(moveDist);

                makeLine(currentPos, newPos);
            }
            else {
                parent.showError(commandName + " requires an argument, none provided");
            }
        }
        else if (commandName.equals("lt") || commandName.equals("left")){
            if (parseTree.hasNamed("fvars")) {
                ArrayList<Integer> moves = getVarValues(parseTree.getNamedChild("fvars"));
                int rotateAngle = moves.get(0);
                Point2D currentPos = turtle.asPoint();
                Point2D newPos = turtle.turnLeft(rotateAngle);

                makeLine(currentPos, newPos);
            }
            else {
                parent.showError(commandName + " requires an argument, none provided");
            }
        }
        else if (commandName.equals("rt") || commandName.equals("right")){
            if (parseTree.hasNamed("fvars")) {
                ArrayList<Integer> moves = getVarValues(parseTree.getNamedChild("fvars"));
                int rotateAngle = moves.get(0);
                Point2D currentPos = turtle.asPoint();
                Point2D newPos = turtle.turnRight(rotateAngle);

                makeLine(currentPos, newPos);
            }
            else {
                parent.showError(commandName + " requires an argument, none provided");
            }
        }
        else if (commandName.equals("pd") || commandName.equals("pendown")){
            if (!Controller.isPenDown()){
                Controller.setPenDown(true);
            }
        }
        else if (commandName.equals("pu") ||  commandName.equals("penup")){
            if (Controller.isPenDown()){
                Controller.setPenDown(false);
            }
        }
        else if (commandName.equals("home")){
            turtleImage.setTranslateX(0);
            turtleImage.setTranslateY(0);
        }
        else if (commandName.equals("cs") || commandName.equals("clearscreen")){
            for (Line line : Controller.lines) {
                canvasChildren.remove(line);
            }
            this.turtleImage.setTranslateY(0);
            this.turtleImage.setTranslateX(0);
            this.turtleImage.setRotate(90);

            Turtle.getInstance().toHome();
        }
        else if (commandName.equals("st") || commandName.equals("showturtle")){
            turtleImage.setVisible(true);
        }
        else if (commandName.equals("ht") || commandName.equals("hideturtle")){
            turtleImage.setVisible(false);
        }
        else {
            ArrayList<Integer> values = getVarValues(parseTree.getNamedChild("fvars"));
            if (functions.containsKey(commandName)) {
                ArrayList<String> variables = this.functions.get(commandName);
                if (values.size() == variables.size()) {
                    Tree block = functionBlocks.get(commandName);
                    for (int i = 0; i < values.size(); ++i) {
                        System.out.println("VariableName: " + variables.get(i) + ", Value: " + values.get(i));
                        this.variables.put(variables.get(i), values.get(i));
                    }
                    interpret(block);
                }
            }

        }
    }

    public ArrayList<Integer> getVarValues(Tree tree){
        ArrayList<Integer> ints = new ArrayList<>();
        int currVal = interpret(tree.getNamedChild("addSubOp"));
        ints.add(currVal);
        if (tree.hasNamed("fvars")){
            ints.addAll(getVarValues(tree.getNamedChild("fvars")));
        }
        return ints;
    }

    public ArrayList<String> setVariableName(Tree tree){
        ArrayList<String> variables = new ArrayList<>();
        variables.add(tree.getNamedChild("variable").toString());
        if (tree.hasNamed("funcVars")){
            variables.addAll(setVariableName(tree.getNamedChild("funcVars")));
        }

        return variables;
    }

    public void run(){
        try {
            parse();
        } catch (ParseException e) {
            if (parent != null){
                parent.showError(e.toString());
            }
            else {
                e.printStackTrace();
            }
        }
    }
}
