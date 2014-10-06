package hu.bme.aut.smeyelframework.events;

import android.app.Activity;
import android.os.Bundle;

import hu.bme.aut.smeyelframework.camera.CameraThread;
import hu.bme.aut.smeyelframework.communication.autrar.model.RarItem;

/**
 * Created on 2014.09.22..
 *
 * @author Ákos Pap
 */
public class Events {

    public static class Command extends RarItem {

    }

    public static class Query extends RarItem {

    }

    public static class ChangeOperatingMode {
        public Class<? extends Activity> targetActivity;
        public Bundle args;

        public ChangeOperatingMode(Class<? extends Activity> targetActivity, Bundle args) {
            this.targetActivity = targetActivity;
            this.args = args;
        }
    }

    public static class TakePicture {
        public CameraThread.PictureRequest request;

        public TakePicture(CameraThread.PictureRequest request) {
            this.request = request;
        }
    }

    public static class PictureTaken {
        public CameraThread.PictureRequest request;

        public PictureTaken(CameraThread.PictureRequest request) {
            this.request = request;
        }
    }
}
