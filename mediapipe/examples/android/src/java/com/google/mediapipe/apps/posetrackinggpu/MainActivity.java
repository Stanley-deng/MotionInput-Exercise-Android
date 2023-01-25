package com.google.mediapipe.apps.posetrackinggpu;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.TextView;
import android.view.View;
import android.util.Log;
import android.content.Context;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmark;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmarkList;
import com.google.mediapipe.framework.AndroidPacketCreator;
import com.google.mediapipe.framework.Packet;
import com.google.mediapipe.framework.PacketGetter;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList; 
import java.util.Map;
import java.io.*;
import java.io.OutputStreamWriter;
import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;


class Point {

  public double x;
  public double y;

  public Point(double _x, double _y) {
    x = _x;
    y = _y;
  }
}

class KeyFrame {

  List<Integer> toTrack = new ArrayList<>();
  double angle;
  double leniency;

  KeyFrame(List<Integer> _toTrack, double _angle, double _leniency) {
    toTrack = _toTrack;
    angle = _angle;
    leniency = _leniency;
  }
}


public class MainActivity extends com.google.mediapipe.apps.basic.MainActivity {
  private static final String TAG = "MainActivity";
  private static final String OUTPUT_LANDMARKS_STREAM_NAME = "pose_landmarks";

  //TextView textViewC;
  TextView textViewD;


  List<KeyFrame> points = new ArrayList<>();
  int gestureIndex = 0;
  boolean start = false;
  boolean finished = false;

  void SetupKeyFrames() {

    List<Integer> pointsTemp = new ArrayList<>();
    pointsTemp.add(15);
    pointsTemp.add(13);
    pointsTemp.add(11);
    points.add(new KeyFrame(pointsTemp, 20, 20));
    points.add(new KeyFrame(pointsTemp, 150, 20));
    points.add(new KeyFrame(pointsTemp, 20, 20));

  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    //textViewC = (TextView)findViewById(R.id.textViewC);
    textViewD = (TextView)findViewById(R.id.textViewD);

    SetupKeyFrames();

    processor.addPacketCallback(OUTPUT_LANDMARKS_STREAM_NAME, (packet) -> {

      try {

        byte[] landmarksRaw = PacketGetter.getProtoBytes(packet);
        NormalizedLandmarkList poseLandmarks = NormalizedLandmarkList.parseFrom(landmarksRaw);
        
        if (start == true){
          Tracking(poseLandmarks);
        }
        
      } catch (InvalidProtocolBufferException exception) {
        Log.e(TAG, "Failed to get proto.", exception);
      }
    });
  }

  private void Tracking(NormalizedLandmarkList poseLandmarks){  

    //if the gesture tracking hasn't finished yet
    if (!finished) {

      //outputs what frame 
      textViewD.setText("Index: " + gestureIndex);
      
      if (WithinAngle(poseLandmarks)) {
      //checks if the angle fits the current gesture keyframe
        
        //increases the keyframe
        gestureIndex++;
        
        //checks if the gesture is finished
        if (gestureIndex >= points.size())
          finished = true;
      }
    } else {

      //outputs that the gesture is finished
      start = false;
      textViewD.setText("Finished!");
    }
  }

  //Uses pythagoras to get the distance between two points
  double GetDistance(Point start, Point end) {

    double xDistance2 = Math.pow(start.x - end.x, 2);
    double yDistance2 = Math.pow(start.y - end.y, 2);

    double distance = Math.pow(xDistance2 + yDistance2, 0.5);

    return distance;
  }

  //returns the angle between three points (the middle point)
  double GetAnglePoints(Point trackStart, Point trackMid, Point trackEnd) {

    double aLength = GetDistance(trackStart, trackMid);
    double bLength = GetDistance(trackMid, trackEnd);
    double cLength = GetDistance(trackEnd, trackStart);

    return GetAngleLengths(aLength, bLength, cLength);
  }

  //returns the angle between three lenghts (angle opposite length c)
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

  //checks if the angle of the keyframe indexes are within the target angle
  boolean WithinAngle(NormalizedLandmarkList landMarks) {

    KeyFrame point = points.get(gestureIndex);
    List<Integer> toTrack  = point.toTrack;
    double targetAngle = point.angle;
    double leniency = point.leniency;
    
    // timeLimit = point.get("timeLimit")
    
    // #checks how long since
    // timeDifference = time.time() - prevGestureTime
    // if (timeLimit == -1 or timeDifference <= timeLimit):
    
    Point start = new Point(landMarks.getLandmarkList().get(toTrack.get(0)).getX(), landMarks.getLandmarkList().get(toTrack.get(0)).getY());
    Point mid = new Point(landMarks.getLandmarkList().get(toTrack.get(1)).getX(), landMarks.getLandmarkList().get(toTrack.get(1)).getY());
    Point end = new Point(landMarks.getLandmarkList().get(toTrack.get(2)).getX(), landMarks.getLandmarkList().get(toTrack.get(2)).getY());
    
    double elbowAngle = GetAnglePoints(start, mid, end);
    //textViewC.setText("Elbow angle: " + elbowAngle + "");
    
    if (elbowAngle > targetAngle - leniency && elbowAngle < targetAngle + leniency) {
      return true;
    }

    // #if the timelimit was set and the time taken is too long
    // if (timeLimit != -1 and timeDifference > timeLimit):
    // global gestureIndex
    // gestureIndex = 0

    // elif (timeLimit != -1 and timeDifference < timeLimit):

    // timeLeft = round(timeLimit - timeDifference, 2)
    // timeString = "Time left: " + str(timeLeft) + "s"
    // #outputs the time left
    // cv2.putText(image, timeString, (700, 70),
    // cv2.FONT_HERSHEY_COMPLEX, 1, (0, 255, 0), 2)
    // return False

    return false;
  }

  //start detecting gesture
  public void startDetecting(View v){
    start = true;
  }

  //resets the gesture
  public void detectGesture(View v){
    finished = false;
    gestureIndex = 0;
    textViewD.setText("Press start");
  }
}