package hu.bme.aut.smeyelframework.functions;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import hu.bme.aut.smeyelframework.BaseActivity;
import hu.bme.aut.smeyelframework.R;
import hu.bme.aut.smeyelframework.camera.CameraHelper;
import hu.bme.aut.smeyelframework.communication.autrar.MessageHandler;
import hu.bme.aut.smeyelframework.communication.autrar.MessageHandlerRepo;
import hu.bme.aut.smeyelframework.communication.autrar.MessageType;
import hu.bme.aut.smeyelframework.communication.autrar.StreamCommunicator;
import hu.bme.aut.smeyelframework.communication.autrar.model.RarContainer;
import hu.bme.aut.smeyelframework.communication.autrar.model.RarItem;
import hu.bme.aut.smeyelframework.communication.autrar.model.Types;
import hu.bme.aut.smeyelframework.timing.Timing;

public class CameraPreviewActivity extends BaseActivity {

    public static final String TAG = "CameraPreviewActivity";

    private SurfaceView preview;
    private CameraHelper cameraHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_preview);

        preview = (SurfaceView) findViewById(R.id.cameraPreview);
        cameraHelper = new CameraHelper(preview);

        MessageHandlerRepo.registerHandler(
                new MessageType(Types.Subject.TAKE_PICTURE, Types.Action.COMMAND),
                new MessageHandler() {
                    @Override
                    public void handleMessage(RarItem msg, Socket socket) throws IOException {
                        long desiredTickstamp = Timing.getCurrentTickstamp();
                        if (Types.Type.TIMESTAMP.equals(msg.getType())) {
                            long timestampFromMsg = (long) Math.floor(msg.getValues().get(0));
                            long timeDelta = timestampFromMsg - System.currentTimeMillis();
                            Log.i(TAG, "Scheduled takePicture in " + timeDelta + " ms.");
                            desiredTickstamp = Timing.getTickStampAtDelta(timeDelta);
                        }

                        cameraHelper.requestPicture(
                                new CameraHelper.PictureRequest(
                                        desiredTickstamp,
                                        new PictureListener(socket)
                                )
                        );
                    }
                }
        );

        final BurstPictureListener burstPictureListener = new BurstPictureListener();

        findViewById(R.id.take5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < 6; i++) {
                    cameraHelper.requestPicture(new CameraHelper.PictureRequest(0, burstPictureListener));
                }
            }
        });

        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                burstPictureListener.save();
            }
        });
    }

    private class BurstPictureListener implements CameraHelper.PictureTakenListener {

        List<CameraHelper.PictureRequest> images = new ArrayList<>(5);

        @Override
        public void onPictureTaken(CameraHelper.PictureRequest request) {
            images.add(request);
        }

        public void save() {
            StringBuilder sb = new StringBuilder("Delays were: ");
            long ts = images.get(0).takenTickstamp;
            for (CameraHelper.PictureRequest pr : images) {
                sb.append(Timing.getTickStampAtDelta(pr.takenTickstamp - ts)).append(" ");
                ts = pr.takenTickstamp;
            }
            Log.i(TAG, sb.toString());

            File sd = Environment.getExternalStorageDirectory();
            File dir = new File(sd, "SMEyeL/Framework");
            dir.mkdirs();

            for (CameraHelper.PictureRequest pr : images) {
                File f = new File(dir, "burst_" + pr.takenTickstamp + ".jpg");

                try {
                    FileOutputStream fos = new FileOutputStream(f);
                    fos.write(pr.image);
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            images.clear();
        }
    }

    private class PictureListener implements CameraHelper.PictureTakenListener {

        private final Socket socket;

        private PictureListener(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void onPictureTaken(CameraHelper.PictureRequest request) {

            long delay = request.takenTickstamp - request.desiredTickstamp;
            Log.i(TAG, "Picture taken. Delay was " + delay + " ticks " +
                    "which means " + Timing.asMillis(delay) + " ms.");

            RarItem item = new RarItem();
            item.setSubject(Types.Subject.CAMERA_IMAGE);
            item.setAction(Types.Action.INFO);
            item.setBinarySize(request.image.length);

            RarContainer container = new RarContainer();
            container.addItem(item);
            container.addPayload(request.image);

//            StringCommunicator sc = new StringCommunicator();
//            sc.send(container);

            try {
                new StreamCommunicator(socket.getOutputStream()).send(container);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
