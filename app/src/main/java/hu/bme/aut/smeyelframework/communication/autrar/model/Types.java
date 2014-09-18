package hu.bme.aut.smeyelframework.communication.autrar.model;

/**
 * Created on 2014.09.18..
 *
 * @author Ákos Pap
 */
public class Types {

    public static class Action {
        public static final String INFO = "info";
        public static final String COMMAND = "command";
        public static final String QUERY = "query";
        public static final String CONFIRM = "confirm";
        public static final String ERROR = "error";
    }

    public static class Subject {
        public static final String MOVE_TO = "moveTo";
        public static final String OBSTACLE = "obstacle";
        public static final String START = "start";
        public static final String SET_SPEED = "setSpeed";
        public static final String ROBOT_MODEL = "robotModel";
        public static final String CAMERA_IMAGE = "cameraImage";
    }

    public static class Type {
        public static final String NONE = "none";
        public static final String POINT_2D = "point2d";
        public static final String CONFIG_2D = "config2d";
        public static final String RECT_2D = "rect2d";
        public static final String CONE_3D = "cone3d";
        public static final String POLYGON_2D = "polygon2d";
        public static final String JPEG = "jpeg";
        public static final String MAT = "Mat";
    }
}
