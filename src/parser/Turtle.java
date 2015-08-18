package parser;


import javafx.geometry.Point2D;

public class Turtle {

    public static final double CENTER_X = 309.0, CENTER_Y = 215.0;
    public static boolean penDown = true;
    private static Turtle ourInstance = new Turtle();
    private double heading, x, y;

    public static Turtle getInstance() {
        return ourInstance;
    }

    private Turtle() {
        heading = 0;
        x = 0;
        y = 0;
    }

    public static boolean isPenDown(){
        return penDown;
    }

    public static void setPenDown(boolean down){
        penDown = down;
    }

    public Point2D updateForCanvasSized(double height, double width){
        this.x = width/2;
        this.y = height/2;
        return this.asPoint();
    }

    public Point2D moveForward(double units) {
        double newX = Math.cos(this.heading) * units;
        double newY = Math.sin(this.heading) * units;

        this.x += newX;
        this.y += newY;

        return this.asPoint();
    }

    public Point2D moveBackward(double units){
        double newX = Math.cos(this.heading) * units;
        double newY = Math.sin(this.heading) * units;

        this.x -= newX;
        this.y -= newY;

        return this.asPoint();
    }

    public Point2D turnLeft(double theta){
        this.heading -= Math.toRadians(theta);
        return this.asPoint();
    }

    public Point2D turnRight(double theta){
        this.heading += Math.toRadians(theta);
        return this.asPoint();
    }

    public Point2D asPoint() {
        Point2D point = new Point2D(x, y);
        return point;
    }

    public void toHome() {
        this.x = CENTER_X;
        this.y = CENTER_Y;
        this.heading = 0;
    }

    public double getRotation(){
        return Math.toDegrees(this.heading);
    }
}
