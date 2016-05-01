package rfbonett.myapplication;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Rich on 4/23/16.
 */
public class ScreenshotManager {
    private ExecutorService service;
    private Future currentTask;
    private String directory;
    private String filename;
    private int screenshot_index;
    private Process process;

    public ScreenshotManager() {
        screenshot_index = 0;
        directory = "sdcard/Screenshots/";
        filename = "screenshot" + screenshot_index + ".png";
        service = Executors.newCachedThreadPool();
        try {
            process = Runtime.getRuntime().exec("su", null, null);
        } catch (Exception e) {
            Log.e("ScreenshotTask", "Could not start process! Check su permissions.");
        }
        service.submit(new ScreenshotOnDisplayChangeTask());
    }

    public Screenshot takeScreenshot() {

        screenshot_index += 1;
        filename = "screenshot" + screenshot_index + ".png";
        service.submit(new ScreenshotTask(directory + filename));
        return new Screenshot(directory + filename);
    }

    class ScreenshotOnDisplayChangeTask implements Runnable {

        private int size = 40000;
        private OutputStream os;
        private byte[] last = new byte[size];
        private byte[] res = new byte[size];
        @Override
        public void run() {
            os = process.getOutputStream();
            while(true) {
                if (displayHasChanged()) {
                    Log.v("ScreenshotTask", "Display Change Registered!");
                    try {
                        os.write(("/system/bin/screencap -p sdcard/testing.png\n").getBytes("ASCII"));
                        os.flush();
                    } catch (Exception e) {
                        Log.e("ScreenshotTask", "Error taking screenshot.");
                    }
                }
                else {
                    Log.v("ScreenshotTask", "No Change!");
                }
                try {
                    Thread.sleep(10000);
                } catch (Exception e) {}
            }
        }

        private boolean displayHasChanged() {
            try {
                Process check = Runtime.getRuntime().exec("su", null, null);
                InputStream cis = check.getInputStream();
                OutputStream cos = check.getOutputStream();
                cos.write(("cat dev/graphics/fb0 \n").getBytes("ASCII"));
                cos.flush();
                cos.close();
                //check.waitFor();
                while (cis.read(res) > 0) {
                    //cis.read(res);
                    Log.v("DisplayChange", Arrays.toString(res));
                    Thread.sleep(100);
                }
            } catch (Exception e) {}
            if (Arrays.equals(last, res)) {
                return false;
            }
            last = res.clone();
            return true;
        }
    }


    class ScreenshotTask implements Runnable {

        private String filename;

        public ScreenshotTask(String filename) {
            this.filename = filename;
        }

        @Override
        public void run() {
            try {
                OutputStream os = process.getOutputStream();
                os.write(("/system/bin/screencap -p " + filename + "\n").getBytes("ASCII"));
                os.flush();
                os.close();
            } catch (Exception e) {
                Log.e("ScreenshotTask", "Error taking screenshot.");
            }
        }

    }
}


class Screenshot {
    private String location;

    public Screenshot(String saveFile) {
        location = saveFile;
    }


    public Bitmap getBitmap() {
        return null;
    }


    public String getLocation() {
        return "Screenshot: " + location;
    }


}