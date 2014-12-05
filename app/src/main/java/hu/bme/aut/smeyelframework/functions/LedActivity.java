package hu.bme.aut.smeyelframework.functions;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import hu.bme.aut.smeyelframework.R;
import hu.bme.aut.smeyelframework.communication.autrar.MessageHandler;
import hu.bme.aut.smeyelframework.communication.autrar.MessageHandlerRepo;
import hu.bme.aut.smeyelframework.communication.autrar.MessageType;
import hu.bme.aut.smeyelframework.communication.autrar.StreamCommunicator;
import hu.bme.aut.smeyelframework.communication.autrar.model.RarItem;
import hu.bme.aut.smeyelframework.communication.autrar.model.Types;
import hu.bme.aut.smeyelframework.timing.Timing;

public class LedActivity extends Activity {

    private TextView displayTv;
    private ToggleButton modeToggle;

    private JavaCameraView cameraImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led);

        displayTv = (TextView) findViewById(R.id.displayTv);

        modeToggle = (ToggleButton) findViewById(R.id.modeToggle);
        modeToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mode = isChecked ? Mode.MEASURE_BRIGHTNESS : Mode.FIND_LED;

                if (mode == Mode.MEASURE_BRIGHTNESS) {
                    resetHistory();
                } else {
                    if (sendResponse != null) {
                        new Thread(sendResponse).start();
                    }
                }
            }
        });

        cameraImage = (JavaCameraView) findViewById(R.id.cameraImage);
        cameraImage.setCvCameraViewListener(cvListener);
        cameraImage.enableView();

        MessageHandlerRepo.registerHandler(
                new MessageType(Types.Subject.TIMESYNC, Types.Action.COMMAND),
                new MessageHandler() {
                    @Override
                    public void handleMessage(RarItem msg, final Socket socket) throws IOException {
                        sendResponse = new Runnable() {
                            @Override
                            public void run() {
                                RarItem item = new RarItem();
                                item.setAction(Types.Action.INFO);
                                item.setSubject(Types.Subject.TIMESYNC);
                                List<Double> values = new ArrayList<Double>(history.length);
                                for (int i = 0; i < historyIdx; i++) {
                                    values.add((double) history[i].brightness);
                                    values.add((double) history[i].timestamp);
                                }
                                item.setValues(values);

                                try{
                                    new StreamCommunicator(socket.getOutputStream()).send(item);
                                } catch (IOException e) {
                                    Log.e("LedActivity", "Cannot send timesync response!", e);
                                }
                            }
                        };
                        changeMode(Mode.MEASURE_BRIGHTNESS);
                        resetHistory();
                    }
                }
        );

    }

    private void resetHistory() {
        for (int i = 0; i < history.length; i++) {
            history[i] = null;
        }
        historyIdx = 0;
    }

    Runnable sendResponse = null;

    Point brightest = new Point();
    volatile int currentBrightness = -1;
    volatile Mode mode = Mode.FIND_LED;

    FrameData[] history = new FrameData[1_000];
    int historyIdx = 0;

    CameraBridgeViewBase.CvCameraViewListener2 cvListener = new CameraBridgeViewBase.CvCameraViewListener2() {
        Mat rgba;

        @Override
        public void onCameraViewStarted(int width, int height) {
            rgba = new Mat(height, width, CvType.CV_8UC4);
        }

        @Override
        public void onCameraViewStopped() {
            rgba.release();
        }

        @Override
        public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

            // get as soon as possible
            final long timestamp = Timing.getCurrentTimestampUs();

            rgba = inputFrame.rgba();

            switch (mode) {
                case FIND_LED:
                    findLed(rgba.getNativeObjAddr(), brightest);
                    break;
                case MEASURE_BRIGHTNESS:
                    currentBrightness = brightnessAt(rgba.getNativeObjAddr(), brightest);
                    if (historyIdx < history.length) {
                        history[historyIdx++] = new FrameData(currentBrightness, timestamp);
                    } else {
                        changeMode(Mode.FIND_LED);
                    }
                    break;
            }

            LedActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    display();
                }
            });

            return rgba;
        }
    };

    void changeMode(final Mode newMode) {
        if (mode == newMode) return;

        LedActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mode = newMode;
                modeToggle.setChecked(mode == Mode.MEASURE_BRIGHTNESS);
            }
        });
        if (newMode == Mode.FIND_LED) {
            if (sendResponse != null) {
                new Thread(sendResponse).start();
            }
        }
    }

    void display() {
        switch (mode) {
            case FIND_LED:
                displayTv.setText(String.format("(%d,%d)", brightest.x, brightest.y));
                break;
            case MEASURE_BRIGHTNESS:
                displayTv.setText(String.format("%d", currentBrightness));
                break;
        }
    }


    private native void findLed(long imageAddr, Point result);
    private native int brightnessAt(long imageAddr, Point position);

    private enum Mode {
        FIND_LED,
        MEASURE_BRIGHTNESS
    }

    private static class FrameData {
        public int brightness;
        public long timestamp;

        private FrameData(int brightness, long timestamp) {
            this.brightness = brightness;
            this.timestamp = timestamp;
        }
    }

}
