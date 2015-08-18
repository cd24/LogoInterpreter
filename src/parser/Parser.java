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
    public static boolean debugging = true;

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
            return interpret(parseTree.getChild(0));
        }
        else if (parseTree.isNamed("num")){
            return parseNum(parseTree);
        }
        else if (parseTree.isNamed("expr")) {
            return interpret(parseTree.getChild(0));
        }
        else if (parseTree.isNamed("assignment")){
            handleAssignment(parseTree);
        }
        else if (parseTree.isNamed("variable")) {
            return getVariable(parseTree);
        }
        else if (parseTree.isNamed("addSubOp")){
            return performAdd(parseTree);
        }
        else if (parseTree.isNamed("multDivOp")){
            return performMultiply(parseTree);
        }
        else if (parseTree.isNamed("paren")){
            return processStatement(parseTree);
        }
        else if (parseTree.isNamed("num")){
            return getNum(parseTree);
        }
        else if (parseTree.isNamed("loop")){
            evaluateLoop(parseTree);
        }
        else if (parseTree.isNamed("block")){
            interpret(parseTree.getNamedChild("lines"));
        }
        else if (parseTree.isNamed("functionCall")) {
            callFunction(parseTree);
        }
        else if (parseTree.isNamed("if") || parseTree.isNamed("ifelse")){
            evaluateBoolean(parseTree);
        }
        else if (parseTree.isNamed("functionDecl")){
            declareFunction(parseTree);
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
        if (Turtle.isPenDown()) {
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
            if (!Turtle.isPenDown()){
                Turtle.setPenDown(true);
            }
        }
        else if (commandName.equals("pu") ||  commandName.equals("penup")){
            if (Turtle.isPenDown()){
                Turtle.setPenDown(false);
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

    private int getVariable(Tree varTree){
        if (variables.containsKey(varTree.toString())){
            return variables.get(varTree.toString());
        }
        if (Controller.publicVariables.containsKey(varTree.toString())){
            return Controller.publicVariables.get(varTree.toString());
        }
        return 0;
    }

    private int parseNum(Tree numTree){
        if (numTree.hasNamed("variable")){
            return interpret(numTree.getChild(0));
        }
        return Integer.parseInt(numTree.toString());
    }

    private int performAdd(Tree addition){
        if (addition.hasNamed("addSubD")){
            if (addition.getNamedChild("addSubD").toString().equals("+")) {
                return interpret(addition.getChild(0)) + interpret(addition.getNamedChild("multDivOp"));
            }
            else if (addition.getNamedChild("addSubD").toString().equals("-")){
                return interpret(addition.getChild(0)) - interpret(addition.getNamedChild("multDivOp"));
            }
            return 0;
        }
        else {
            return interpret(addition.getChild(0));
        }
    }

    private int performMultiply(Tree multi){
        if (multi.getNumChildren() > 1){
            if (multi.getNamedChild("multDivD").toString().equals("*")){
                return interpret(multi.getChild(0)) * interpret(multi.getLastChild());
            }
            else {
                return interpret(multi.getChild(0)) / interpret(multi.getLastChild());
            }
        }
        else {
            return interpret(multi.getChild(0));
        }
    }

    private void handleAssignment(Tree assignment){
        String variableName = assignment.getNamedChild("variable").toString();
        Integer value = interpret(assignment.getNamedChild("addSubOp"));
        Controller.publicVariables.put(variableName, value);
    }

    private int getNum(Tree number){
        if (number.hasNamed("variable")){
            return interpret(number.getChild(0));
        }
        else {
            return Integer.parseInt(number.toString());
        }
    }

    private void evaluateLoop(Tree loop){
        int numRepeats = interpret(loop.getNamedChild("addSubOp"));
        System.out.println("Called, looping for " + numRepeats + " times!");
        for (int i = 0; i < numRepeats; ++i){
            interpret(loop.getNamedChild("block"));
        }
    }

    private void callFunction(Tree function){
        variableStack.push((HashMap)variables.clone());
        if (function.hasNamed("functionCall")){
            interpret(function.getNamedChild("functionCall"));
            handleCommand(function);
        }
        ArrayList vars = getVarValues(function.getNamedChild("fvars"));
        log("Calling function: " + function.getNamedChild("fname").toString() + " with " + vars.size() + " variables");
        for (Object var : vars){
            log("\t" + var.toString());
        }
        handleCommand(function);
        variables = variableStack.pop();
    }

    private void evaluateBoolean(Tree boolExpr){
        boolean condition = evaluateBooleanExpression(boolExpr.getNamedChild("boolCond"));
        if (condition) {
            interpret(boolExpr.getNamedChild("block"));
        }
        else  if (boolExpr.isNamed("ifelse")){
            interpret(boolExpr.getNamedChild("elseBlock"));
        }
    }

    private void declareFunction(Tree func){
        String functionName = func.getNamedChild("fname").toString();
        log("Declaring function: " + functionName);
        ArrayList<String> variables = setVariableName(func.getNamedChild("funcVars"));
        functions.put(functionName, variables);
        functionBlocks.put(functionName, func.getNamedChild("block"));
    }

    private int processStatement(Tree statement){
        if (statement.hasNamed("num")) {
            return interpret(statement.getNamedChild("num"));
        }
        else {
            return interpret(statement.getChild(0));
        }
    }

    private void customFunctionCall(String name, ArrayList<Integer> values){
        if (functions.containsKey(name)) {
            ArrayList<String> variables = this.functions.get(name);
            if (values.size() == variables.size()) {
                Tree block = functionBlocks.get(name);
                for (int i = 0; i < values.size(); ++i) {
                    System.out.println("VariableName: " + variables.get(i) + ", Value: " + values.get(i));
                    this.variables.put(variables.get(i), values.get(i));
                }
                interpret(block);
            }
            else {
                parent.showError("Incorrect number of variables for function (" + name + ")\n\t expected " + variables.size() + " but received " + values.size());
            }
        }
        else {
            parent.showError("Function (" + name + ") is not declared");
        }
    }

    private void log(String message){
        if(debugging)
            System.out.println(message);
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
