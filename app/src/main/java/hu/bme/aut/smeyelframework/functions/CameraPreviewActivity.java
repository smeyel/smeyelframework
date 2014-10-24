package hu.bme.aut.smeyelframework.functions;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.net.Socket;

import hu.bme.aut.smeyelframework.R;
import hu.bme.aut.smeyelframework.camera.CameraThread;
import hu.bme.aut.smeyelframework.communication.autrar.MessageHandler;
import hu.bme.aut.smeyelframework.communication.autrar.MessageHandlerRepo;
import hu.bme.aut.smeyelframework.communication.autrar.MessageType;
import hu.bme.aut.smeyelframework.communication.autrar.StreamCommunicator;
import hu.bme.aut.smeyelframework.communication.autrar.StringCommunicator;
import hu.bme.aut.smeyelframework.communication.autrar.model.RarContainer;
import hu.bme.aut.smeyelframework.communication.autrar.model.RarItem;
import hu.bme.aut.smeyelframework.communication.autrar.model.Types;
import hu.bme.aut.smeyelframework.events.EventActivity;
import hu.bme.aut.smeyelframework.timing.Timing;

public class CameraPreviewActivity extends EventActivity {

    private ImageView preview;
    private CameraThread.PreviewListener previewListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_preview);

        preview = (ImageView) findViewById(R.id.preview);

        previewListener = new CameraThread.PreviewListener() {
            @Override
            public void onPreview(byte[] imgData) {
                byte[] jpeg = getCameraThread().convertYuv2Jpeg(imgData);
                Bitmap image = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);
                preview.setImageBitmap(image);
            }
        };

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

                        getCameraThread().requestPicture(
                                new CameraThread.PictureRequest(
                                        desiredTickstamp,
                                        new PictureListener(socket)
                                )
                        );
                    }
                }
        );
    }

    private class PictureListener implements CameraThread.PictureTakenListener {

        private final Socket socket;

        private PictureListener(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void onPictureTaken(CameraThread.PictureRequest request) {

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

            StringCommunicator sc = new StringCommunicator();
            sc.send(container);
            Log.i(TAG, "Sending: " + sc.toString());

            try {
                new StreamCommunicator(socket.getOutputStream()).send(container);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getCameraThread().registerPreviewListener(previewListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getCameraThread().unregisterPreviewListener(previewListener);
    }



    @Override
    protected boolean needsCameraThread() {
        return true;
    }
}
