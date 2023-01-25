import java.util.ArrayList;
import java.util.List;

class Point {

    public double x;
    public double y;

    public Point(double _x, double _y) {
        x = _x;
        y = _y;
    }
}

class KeyFrame {
    KeyFrame(List<Integer> _toTrack, double _angle, double _leniency) {
        toTrack = _toTrack;
        angle = _angle;
        leniency = _leniency;
    }

    List<Integer> toTrack = new ArrayList<>();
    double angle;
    double leniency;

    void Print() {
        System.out.print(toTrack + "\n");
        System.out.print(angle + "\n");
        System.out.print(leniency + "\n");
    }
}

public class test {

    List<KeyFrame> points = new ArrayList<>();
    int gestureIndex = 0;

    double GetDistance(Point start, Point end) {

        double xDistance2 = Math.pow(start.x - end.x, 2);
        double yDistance2 = Math.pow(start.y - end.y, 2);

        double distance = Math.pow(xDistance2 + yDistance2, 0.5);

        return distance;
    }

    double GetAnglePoints(Point trackStart, Point trackMid, Point trackEnd) {

        double aLength = GetDistance(trackStart, trackMid);
        double bLength = GetDistance(trackMid, trackEnd);
        double cLength = GetDistance(trackEnd, trackStart);

        return GetAngleLengths(aLength, bLength, cLength);
    }

    double GetAngleLengths(double a, double b, double c) {

        // derivation of cosine rule
        // c^2 = a^2+b^2 - 2ab Cos(C)
        // 0 = a^2+b^2 - c^2 - 2ab Cos(C)
        // 2ab Cos(C) = a^2+b^2 - c^2
        // Cos(C) = (a^2+b^2 - c^2) / 2ab
        // C = Cos-1( (a^2+b^2 - c^2) / 2ab)

        double C_radian = Math.acos((Math.pow(a, 2) + Math.pow(b, 2) - Math.pow(c, 2)) / (2 * a * b));
        double C_degrees = Math.toDegrees(C_radian);
        return C_degrees;
    }

    double WithinAngle() {
        return 0;
    }

    public static void main(String[] args) {
        List<KeyFrame> points = new ArrayList<>();

        List<Integer> pointsTemp = new ArrayList<>();
        pointsTemp.add(16);
        pointsTemp.add(14);
        pointsTemp.add(12);
        points.add(new KeyFrame(pointsTemp, 20, 10));
        points.add(new KeyFrame(pointsTemp, 150, 10));
        points.add(new KeyFrame(pointsTemp, 20, 10));

        for (int i = 0; i < points.size(); i++) {
            points.get(i).Print();
            System.out.print("\n");
        }
    }
}