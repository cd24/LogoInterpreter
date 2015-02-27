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

/**
 * Created by John McAvey on 2/19/2015.
 */
public class Parser implements Runnable{

    private ImageView turtleImage;
    private LogoGrammar grammar;
    private Turtle turtle = Turtle.getInstance();
    private HashMap<String, Integer> variables;
    private HashMap<String, ArrayList<String>> functions;
    private HashMap<String, Tree> funcVars;
    private ObservableList<Node> canvasChildren;
    private String commands;

    public Parser(ImageView turtle, ObservableList<Node> children, String commands){
        this.turtleImage = turtle;
        this.canvasChildren = children;
        grammar = new LogoGrammar();
        variables = new HashMap<>();
        functions = new HashMap<>();
        funcVars = new HashMap<>();
        this.commands = commands;
        System.out.println("Command: " + commands);
    }

    public void parse() throws ParseException {
        Tree parseTree = grammar.parse(commands);
        this.interpret(parseTree);
    }

    private int interpret(Tree parseTree) {
        System.out.println("Tree name: " + parseTree.getName() + ", String value: " + parseTree.toString());
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
                interpret(parseTree.getNamedChild("block"));
            }
        }

        else if (parseTree.isNamed("block")){
            interpret(parseTree.getNamedChild("lines"));
        }

        else if (parseTree.isNamed("functionCall")) {
            if (parseTree.hasNamed("functionCall")){
                handleCommand(parseTree);
                interpret(parseTree.getNamedChild("functionCall"));
            }
            else {
                handleCommand(parseTree);
            }
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
            funcVars.put(name, parseTree.getNamedChild("block"));
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
            line.setFill(Controller.penColor);
            canvasChildren.add(line);
            Controller.lines.add(line);
        }

        turtleImage.setTranslateX(end.getX() - Turtle.CENTER_X);
        turtleImage.setTranslateY(end.getY() - Turtle.CENTER_Y);
        turtleImage.setRotate(turtle.getRotation() + 90);


    }

    public void handleCommand(Tree parseTree){
        String commandName = parseTree.getNamedChild("fname").toString();
        if (commandName.equals("fd")){
            int moveDist = getVarValues(parseTree.getNamedChild("fvars")).get(0);
            Point2D currentPos = turtle.asPoint();
            Point2D newPos = turtle.moveForward(moveDist);

            makeLine(currentPos, newPos);
        }
        else if (commandName.equals("bk")){
            int moveDist = getVarValues(parseTree.getNamedChild("fvars")).get(0);
            Point2D currentPos = turtle.asPoint();
            Point2D newPos = turtle.moveBackward(moveDist);

            makeLine(currentPos, newPos);
        }
        else if (commandName.equals("lt")){
            int rotateAngle = getVarValues(parseTree.getNamedChild("fvars")).get(0);
            Point2D currentPos = turtle.asPoint();
            Point2D newPos = turtle.turnLeft(rotateAngle);

            makeLine(currentPos, newPos);
        }
        else if (commandName.equals("rt")){
            int rotateAngle = getVarValues(parseTree.getNamedChild("fvars")).get(0);
            Point2D currentPos = turtle.asPoint();
            Point2D newPos = turtle.turnRight(rotateAngle);

            makeLine(currentPos, newPos);
        }
        else if (commandName.equals("pd")){
            if (!Controller.isPenDown()){
                Controller.setPenDown(true);
            }
        }
        else if (commandName.equals("pu")){
            if (Controller.isPenDown()){
                Controller.setPenDown(false);
            }
        }
        else if (commandName.equals("home")){
            turtleImage.setTranslateX(0);
            turtleImage.setTranslateY(0);
        }
        else if (commandName.equals("cs")){
            for (Line line : Controller.lines) {
                canvasChildren.remove(line);
            }
            this.turtleImage.setTranslateY(0);
            this.turtleImage.setTranslateX(0);
            this.turtleImage.setRotate(90);

            Turtle.getInstance().toHome();
        }
        else if (commandName.equals("st")){
            turtleImage.setVisible(true);
        }
        else if (commandName.equals("ht")){
            turtleImage.setVisible(false);
        }
        else {
            ArrayList<Integer> values = getVarValues(parseTree.getNamedChild("fvars"));
            String funcName = parseTree.getNamedChild("fname").toString();
            if (functions.containsKey(funcName)) {
                ArrayList<String> variables = this.functions.get(funcName);
                if (values.size() == variables.size()) {
                    Tree block = funcVars.get(funcName);
                    for (int i = 0; i < values.size(); ++i) {
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
            //ToDo: get information back to the user... somehow.
            e.printStackTrace();
        }
    }
}
