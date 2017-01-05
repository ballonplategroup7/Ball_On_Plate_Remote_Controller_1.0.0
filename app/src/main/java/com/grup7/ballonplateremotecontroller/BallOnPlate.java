package com.grup7.ballonplateremotecontroller;

import android.bluetooth.BluetoothA2dp;
import android.os.Bundle;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

public class MainActivity extends Activity implements OnItemClickListener {
    // Kotlin
    // Android NDK
    private Button changeEquilibrium;
    private Button rectangle;
    private Button circle;
    private Button infinity;
    private TextView text;
    private String sendText="1_+000_-000$";
    private LinearLayout touchArea;
    OutputStream outstream;
    ArrayAdapter<String> adaptorlist;
    ListView liste;
    BluetoothAdapter blt;
    Set<BluetoothDevice> arraydevice;
    ArrayList<String> eslesen;
    ArrayList<BluetoothDevice> aygitlar;
    public static final UUID mmuuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    protected static final int baglanti = 0;
    protected static final int mesajoku = 1;
    IntentFilter filtre;
    BroadcastReceiver receiver;
    private Thread thread;
    private int dataSendCounter=0;
    private int buton=0;
    private BluetoothSocket mmSocket;
    private int ss=0;
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch(msg.what){
                case baglanti:

                    ConnectedThread connectedThread = new ConnectedThread((BluetoothSocket)msg.obj);
                    Toast.makeText(getApplicationContext(), "Baglandı", Toast.LENGTH_SHORT).show();
                    String s = "Bluetooth Durumu: Baglantı kuruldu.";
                    text.setText(s);
                    break;
                case mesajoku:
                    byte[] readBuf = (byte[])msg.obj;
                    String string = new String(readBuf);
                    Toast.makeText(getApplicationContext(), string,  Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        liste=(ListView)findViewById(R.id.listem);
        text=(TextView)findViewById(R.id.liste);
        liste.setOnItemClickListener(this);
        adaptorlist= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,0);
        liste.setAdapter(adaptorlist);
        blt = BluetoothAdapter.getDefaultAdapter();
        eslesen = new ArrayList<String>();
        filtre = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        aygitlar = new ArrayList<BluetoothDevice>();
        try {
            receiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    // TODO Auto-generated method stub
                    String action = intent.getAction();

                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                        aygitlar.add(device);
                        String s = "";

                        for (int a = 0; a < eslesen.size(); ++a) {
                            if (device.getName().equals(eslesen.get(a))) {

                                s = "(Eşlesti)";
                                break;
                            }
                        }

                        adaptorlist.add(device.getName() + "" + s + "" + "\n" + device.getAddress());
                    } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                        if (blt.getState() == blt.STATE_OFF) {
                            bltac();
                        }
                    }
                }


            };
            registerReceiver(receiver, filtre);
            filtre = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(receiver, filtre);

            if (blt == null) {
                Toast.makeText(getApplicationContext(), "Bluetooth aygıtları bulunamadı", Toast.LENGTH_SHORT).show();
                text.setText("Baglantı kurulamadı.");
                finish();
            } else {
                if (!blt.isEnabled()) {
                    bltac();
                }

                getPairedDevices();
                startDiscovery();

                changeEquilibrium = (Button) findViewById(R.id.konum);
                changeEquilibrium.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View v) {
                        changeEquilibrium();
                    }
                });

                rectangle = (Button) findViewById(R.id.rectangle);
                rectangle.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View v) {
                        rectangle();
                    }
                });

                circle = (Button) findViewById(R.id.circle);
                circle.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View v) {
                        circle();
                    }
                });

                infinity = (Button) findViewById(R.id.infinity);
                infinity.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        infinity();
                    }
                });

                touchArea = (LinearLayout) findViewById(R.id.touchArea);
                touchArea.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int x = 0;
                        int y = 0, i = 0;
                        String sendX = "+000", sendY = "+000", tempX, tempY;

                        StringBuilder a = new StringBuilder();
                        if(buton==1) {
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    ss=1;
                                    x = (int) map(event.getX(), 0, 660, -100, +100);
                                    y = (int) map(event.getY(), 0, 400, -80, 80);
                                    y = y * -1;
                                    tempX = Integer.toString(x);
                                    tempY = Integer.toString(y);
                                    i = 0;
                                    while (tempX.length() > i) {

                                        if (a.length() == 0) {
                                            if (tempX.charAt(i) == '-') {
                                                a.append(tempX.charAt(0));
                                                ++i;

                                            } else
                                                a.append("+");

                                            continue;
                                        }
                                        a.append(tempX.charAt(i));
                                        ++i;
                                    }
                                    while (a.length() < 4)
                                        a.insert(1, "0");
                                    sendX = a.toString();
                                    a.delete(0, a.length());
                                    i = 0;
                                    while (tempY.length() > i) {

                                        if (a.length() == 0) {
                                            if (tempY.charAt(i) == '-') {
                                                a.append(tempY.charAt(0));
                                                ++i;
                                            } else
                                                a.append("+");
                                            continue;
                                        }
                                        a.append(tempY.charAt(i));
                                        ++i;
                                    }
                                    while (a.length() < 4)
                                        a.insert(1, "0");
                                    sendY = a.toString();
                                    a.delete(0, a.length());

                                    sendText = "1_" + sendX + "_" + sendY + "$";
                                    Log.i("Action_down= ", sendText);
                                    break;
                                case MotionEvent.ACTION_UP:
                                    ss=0;
                                    x = (int) map(event.getX(), 0, 660, -100, +100);
                                    y = (int) map(event.getY(), 0, 400, -80, 80);
                                    y = y * -1;
                                    tempX = Integer.toString(x);
                                    tempY = Integer.toString(y);
                                    i = 0;
                                    while (tempX.length() > i) {

                                        if (a.length() == 0) {
                                            if (tempX.charAt(i) == '-') {
                                                a.append(tempX.charAt(0));
                                                ++i;

                                            } else
                                                a.append("+");

                                            continue;
                                        }
                                        a.append(tempX.charAt(i));
                                        ++i;
                                    }
                                    while (a.length() < 4)
                                        a.insert(1, "0");
                                    sendX = a.toString();
                                    a.delete(0, a.length());
                                    i = 0;
                                    while (tempY.length() > i) {

                                        if (a.length() == 0) {
                                            if (tempY.charAt(i) == '-') {
                                                a.append(tempY.charAt(0));
                                                ++i;
                                            } else
                                                a.append("+");
                                            continue;
                                        }
                                        a.append(tempY.charAt(i));
                                        ++i;
                                    }
                                    while (a.length() < 4)
                                        a.insert(1, "0");
                                    sendY = a.toString();
                                    a.delete(0, a.length());

                                    sendText = "1_" + sendX + "_" + sendY + "$";
                                    Log.i("Action_up= ", sendText);
                                    break;
                                case MotionEvent.ACTION_MOVE:
                                    ss=1;
                                    x = (int) map(event.getX(), 0, 660, -100, +100);
                                    y = (int) map(event.getY(), 0, 400, -80, 80);
                                    y = y * -1;
                                    tempX = Integer.toString(x);
                                    tempY = Integer.toString(y);
    /*                                sendX = Integer.toString((int)event.getX());
                                    sendY = Integer.toString((int)event.getY());
    */
                                    i = 0;
                                    while (tempX.length() > i) {

                                        if (a.length() == 0) {
                                            if (tempX.charAt(i) == '-') {
                                                a.append(tempX.charAt(0));
                                                ++i;

                                            } else
                                                a.append("+");

                                            continue;
                                        }
                                        a.append(tempX.charAt(i));
                                        ++i;
                                    }
                                    while (a.length() < 4)
                                        a.insert(1, "0");
                                    sendX = a.toString();
                                    a.delete(0, a.length());
                                    i = 0;
                                    while (tempY.length() > i) {

                                        if (a.length() == 0) {
                                            if (tempY.charAt(i) == '-') {
                                                a.append(tempY.charAt(0));
                                                ++i;
                                            } else
                                                a.append("+");
                                            continue;
                                        }
                                        a.append(tempY.charAt(i));
                                        ++i;
                                    }
                                    while (a.length() < 4)
                                        a.insert(1, "0");
                                    sendY = a.toString();
                                    a.delete(0, a.length());
                                    sendText = "1_" + sendX + "_" + sendY + "$";
                                    Log.i("Action_move= ", sendText);
                                    break;
                            }
                        }
                        else
                        {
                            if(event.getAction()==event.ACTION_DOWN)
                                ss=1;
                            else if(event.getAction()==event.ACTION_UP)
                                ss=0;
                        }
                            return true;
                        }


                });

                thread = new Thread() {
                    public void run() {
                       // String sendText2=new String();
                        while (true) {
                            try {
                                if(ss==1) {
                                    if (blt.isEnabled()) {

                                        dataSendCounter = 0;
                                        int i = 0;
                                        String s;

                                       /* if(!sendText.equals(sendText2)){
                                            sendText2=sendText;
                                        }*/

                                        while (i < sendText.length()) {

                                            s = Character.toString(sendText.charAt(i));
                                           // s="1";
                                            outstream.write(s.getBytes());
                                            ++i;
                                            Log.i("Gonderildi= ", sendText);


                                            thread.sleep(5);
                                            dataSendCounter++;
                                        }
                                        Thread.sleep(100);
                                       // outstream.flush();
                                    }
                                }
                                } catch (Exception e) {

                            }
                        }
                    }

                };
                thread.start();
            }
        }catch(Exception e){
            Log.i("yakaladim","patliyordu");
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        try {
            if(blt.isEnabled()){
                sendText="!!!!!!!!!!!!";
                while(dataSendCounter<sendText.length())
                {
                    outstream.write(Character.toString(sendText.charAt(dataSendCounter)).getBytes());
                    dataSendCounter++;
                    Thread.sleep(10);
                }
            }
            thread.join();
            mmSocket.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void onStop() {
        super.onDestroy();
        try {
            if(blt.isEnabled()){
                sendText="!!!!!!!!!!!!";
                while(dataSendCounter<sendText.length())
                {
                    outstream.write(Character.toString(sendText.charAt(dataSendCounter)).getBytes());
                    dataSendCounter++;
                    Thread.sleep(5);
                }
            }


        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch(Exception e){}
    }

    private void startDiscovery() {
        // TODO Auto-generated method stub

        blt.cancelDiscovery();
        blt.startDiscovery();

    }
    private void bltac() {
        // TODO Auto-generated method stub
        Intent intent =new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, 1);
    }

    private void getPairedDevices() {
        // TODO Auto-generated method stub
        arraydevice = blt.getBondedDevices();
        if(arraydevice.size()>0){
            for(BluetoothDevice device:arraydevice){
                eslesen.add(device.getName());
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_CANCELED){
            Toast.makeText(getApplicationContext(), "Kontrol işlemi için bluetoothun açık olması gerekir.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        // TODO Auto-generated method stub

        if(blt.isDiscovering()){
            blt.cancelDiscovery();
        }
        if(adaptorlist.getItem(arg2).contains("Eşlesti")){
            BluetoothDevice selectedDevice = aygitlar.get(arg2);
            ConnectThread connect = new ConnectThread(selectedDevice);
            connect.start();
        }
        else{
            Toast.makeText(getApplicationContext(), "Cihazlar eşlesmedi",  Toast.LENGTH_SHORT).show();
            text.setText("Cihazlar eşlesmedi.");
        }

    }

    private class ConnectThread extends Thread {


        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {

            BluetoothSocket tmp = null;
            mmDevice = device;

            try {

                tmp = device.createRfcommSocketToServiceRecord(mmuuid);
            } catch (IOException e) {
            }
            mmSocket = tmp;

        }
        public void run() {
            blt.cancelDiscovery();
            try {
                mmSocket.connect();
            } catch (IOException connectException) {

                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }
            mHandler.obtainMessage(baglanti, mmSocket).sendToTarget();
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    private class ConnectedThread extends Thread {

        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            outstream = tmpOut;
        }

        public void run() {
            byte[] buffer;
            int bytes;


         /*   while (true) {
                try {

                    buffer = new byte[1024];
                    bytes = mmInStream.read(buffer);

                    mHandler.obtainMessage(mesajoku, bytes, -1, buffer)
                            .sendToTarget();

                } catch (IOException e) {
                    break;
                }
            }*/
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    float map(float x, float in_min, float in_max, float out_min, float out_max)
    {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }
    public void changeEquilibrium(){
        sendText="1_+000_+000$";

        buton=1;
        //sendText="1";
        // outstream.write("1".getBytes());

    }
    public void rectangle() {
        // outstream.write("2".getBytes());
        sendText="3_+000_+000$";

        buton=2;
        //  sendText="2";
    }
    public void circle(){
        // outstream.write("3".getBytes());
        sendText="4_+000_+000$";

        buton=3;
        // sendText="3";
    }
    public void infinity() {
        // outstream.write("4".getBytes());
        sendText="2_+000_+000$";

        buton=4;
        //sendText="4";
    }
}


