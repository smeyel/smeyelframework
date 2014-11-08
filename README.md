# SMEyeLFramework

The new SMEyeL framework designed for smartphone interoperation and the RAR protocol.

It's a general application, that is easy to extend for various tasks and experiments, and has built-in support for the AutRar protocoll.

## Usage

Activities of the application should extend BaseActivity (or implement it's functionality of notifying the Application).  There are no limitations other than this.

#### Communication
To register for handling a specific type of message, you sholud implement the  `communication.autrar.MessageHandler` interface, and call MessageHandlerRepo.registerHandler() with the type of the message and the handler.

```java
MessageHandlerRepo.registerHandler(
        new MessageType(Types.Subject.TAKE_PICTURE, Types.Action.COMMAND),
        new MessageHandler() {
            @Override
            public void handleMessage(RarItem msg, Socket socket) throws IOException {
                /* Do something with msg. */
                /* Can also send response through socket */
            }
        }
);
```

To send a specific message back, you can use the various `Communicator` classes, especially `StreamCommunicator`.
Example:

```java
RarItem item = new RarItem();
item.setSubject(Types.Subject.CAMERA_IMAGE);
item.setAction(Types.Action.INFO);
item.setBinarySize(image.length);

RarContainer container = new RarContainer();
container.addItem(item);
container.addPayload(image);
// socket comes from the above example
new StreamCommunicator(socket.getOutputStream()).send(container);
```

#### Camera handling
The class `CameraHelper` helps with handling the camera, taking pictures, etc.

1. Simply instantiate a CameraHelper object with a SurfaceView

   ```java
   SurfaceView preview = (SurfaceView) findViewById(R.id.cameraPreview);
   CameraHelper cameraHelper = new CameraHelper(preview);
   ```
2. Create a CameraHelper.PictureTakenListener to receive the image

   ```java
   private class PictureListener implements CameraHelper.PictureTakenListener {

        @Override
        public void onPictureTaken(CameraHelper.PictureRequest request) {
            /* Use the time information in the request */
            long delay = request.takenTickstamp - request.desiredTickstamp;
            
            /* Do something with the image in request.image */
        }
    }
   ```
3. Ask for a picture

   ```java
   cameraHelper.requestPicture(
           new CameraHelper.PictureRequest(
                   desiredTickstamp, // or 0 if immediately
                   new PictureListener()
           )
   );
   ```
