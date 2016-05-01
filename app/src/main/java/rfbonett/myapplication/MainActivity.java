package rfbonett.myapplication;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.drawable.Drawable;
import android.renderscript.ScriptGroup;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private ScreenshotManager sm;
    private TextView t;
    private TextView slideBarText;
    private Process su;
    private Button scrButton;
    private InputStream is;
    private ExecutorService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SeekBar s = (SeekBar) findViewById(R.id.slideBar);
        scrButton = (Button) findViewById(R.id.screenshotButton);
        slideBarText = (TextView) findViewById(R.id.slideBarText);
        //s.setOnSeekBarChangeListener(new SlideBarListener());

        //sm = new ScreenshotManager();
        t = (TextView) findViewById(R.id.display);
        OutputStream outputStream;
        try {
            su = Runtime.getRuntime().exec("su", null, null);
            outputStream = su.getOutputStream();
            outputStream.write(("cat dev/input/event5 &").getBytes("ASCII"));
            outputStream.flush();
            outputStream.close();
            is = su.getInputStream();
            service = Executors.newSingleThreadExecutor();
            service.submit(new GetEventTask());
            Log.v("Main", "Gogogadget!");
        } catch (Exception e) {}
        /*
        try {
            su = Runtime.getRuntime().exec("su", null, null);
            outputStream = su.getOutputStream();
            outputStream.write(("/system/bin/getevent -t > sdcard/events.txt &").getBytes("ASCII"));
            outputStream.flush();
            //printEvents();
        } catch (IOException e) {
            Log.v("A", "Error2");
        } */
    }


    class GetEventTask implements Runnable {
        private byte[] res = new byte[16];

        @Override
        public void run() {
            Log.v("GetEventTask", "Starting!");
            try {
                while(is.read(res) > 0) {
                    Log.v("GetEventTask", format(res));
                }
            } catch (Exception e) {Log.v("GetEventTask", "Whoops! " + e.getMessage());}
            Log.v("GetEventTask", "Done!");
        }

        private String format(byte[] val) {
            ByteBuffer buf = ByteBuffer.wrap(val);
            String time = String.valueOf(buf.getInt(0)) + "." + String.valueOf(buf.getInt(1));
            //String type = String.valueOf(buf.getShort())
            return time;
        }
    }






    public void fireScreenshot(View v) {
        //Screenshot scr = sm.takeScreenshot();
        //Log.v("Main", scr.getLocation());
        //t.setText(scr.getLocation());
        /*try {
            byte bit = 0;
            FileOutputStream f = new FileOutputStream("sdcard/events.txt");
            for (int i = 0; i < 4096; i++) {
                f.write(bit);
            }
        } catch (Exception e) {Log.v("Main", "File not found" + e.getMessage());} */

        OutputStream os = su.getOutputStream();
        /*
        try {
            for (int i = 0; i < 4096; i++) {
                os.write(("sendevent /dev/input/event0 0 0 0").getBytes("ASCII"));
                os.flush();
            }
        } catch (Exception e) {Log.v("Main", "Error: " + e.getMessage());} */
        try {
            os.write(("killall cat\n").getBytes("ASCII"));
            os.flush();
            os.write(("exit\n").getBytes("ASCII"));
            os.flush();
            os.close();
            su.waitFor();
        } catch (Exception e) {
            Log.v("GetEventManager", "Error stopping GetEvent process: " + e.getMessage());
        }
    }

    class GetEvent implements Runnable {
        private InputStream input;
        private byte[] buffer = new byte[4096];

        public void run() {
            getEvents();
        }

        public void getEvents() {



        }

        public void printEvents() {
            StringBuilder s = new StringBuilder();
            try {
                while (input.available() <= 0) {
                    Log.v("Main", "Sleeping");
                    Thread.sleep(100);
                }
                while (input.read(buffer) > 0) {
                    Log.v("Main", "Val: " + String.valueOf(buffer));
                }
            } catch (Exception e) {}
        }
    }
    void readFully(InputStream is) throws IOException {
        Log.v("A", "Reading!");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Log.v("A", "D");
        byte[] buffer = new byte[1024];
        Log.v("A", "E");
        int length = 0;
        while ((length = is.read(buffer)) != -1) {
            Log.v("A", "F");
            baos.write(buffer, 0, length);
            Log.v("A", "C");
        }
        Log.v("A", "Done reading!");
        Log.v("A", baos.toString("UTF-8"));
    }


    class SlideBarListener implements SeekBar.OnSeekBarChangeListener {

        private int minimum = 4;
        private int maximum = 96;
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            Log.v("Main", "I: " + i);
            if (i <= minimum) {
                seekBar.setProgress(minimum);
            }
            if (i >= maximum) {
                scrButton.setClickable(false);
                remove(scrButton);
                remove(slideBarText);
                remove(seekBar);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(final SeekBar seekBar) {
            if (seekBar.getProgress() < maximum) {
                ValueAnimator anim = ValueAnimator.ofInt(seekBar.getProgress(), minimum);
                anim.setDuration(5*(seekBar.getProgress()));
                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int animProgress = (Integer) animation.getAnimatedValue();
                        seekBar.setProgress(animProgress);
                    }
                });
                anim.start();
            }
        }
    }

    private void remove(final View view) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        anim.setDuration(400);
        anim.start();
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.GONE);
            }
        }, 400);
    }

    private void rotate(View view) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f);
        anim.setDuration(100);
        anim.setRepeatCount(ObjectAnimator.INFINITE);
        anim.start();
    }

}
