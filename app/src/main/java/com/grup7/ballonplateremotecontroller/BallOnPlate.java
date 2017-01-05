package com.grup7.ballonplateremotecontroller;

import android.os.Bundle;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
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
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BallOnPlate extends Activity implements OnItemClickListener {

    private Button changeEquilibrium;
    private Button rectangle;
    private Button circle;
    private Button infinity;
    private TextView text;
    private String sendText="1_+220_-999$";
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
        setContentView(R.layout.activity_ball_on_plate);
        liste=(ListView)findViewById(R.id.listem);
        text=(TextView)findViewById(R.id.liste);
        liste.setOnItemClickListener(this);
        adaptorlist= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,0);
        liste.setAdapter(adaptorlist);
        blt = BluetoothAdapter.getDefaultAdapter();
        eslesen = new ArrayList<String>();
        filtre = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        aygitlar = new ArrayList<BluetoothDevice>();

        receiver = new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO Auto-generated method stub
                String action = intent.getAction();

                if(BluetoothDevice.ACTION_FOUND.equals(action)){

                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    aygitlar.add(device);
                    String s = "";

                    for(int a = 0; a < eslesen.size(); a++){
                        if(device.getName().equals(eslesen.get(a))){

                            s = "(Eşlesti)";
                            break;
                        }
                    }

                    adaptorlist.add(device.getName()+""+s+""+"\n"+device.getAddress());
                }

                else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
                    if(blt.getState() == blt.STATE_OFF){
                        bltac();
                    }
                }

            }


        };
        registerReceiver(receiver, filtre);
        filtre = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(receiver, filtre);

        if(blt==null){
            Toast.makeText(getApplicationContext(), "Bluetooth aygıtları bulunamadı",  Toast.LENGTH_SHORT).show();
            text.setText("Bluetooth aygiti yok");
            finish();
        }

        else{
            if(!blt.isEnabled()){
                bltac();
            }

            getPairedDevices();
            startDiscovery();

            changeEquilibrium =(Button) findViewById(R.id.ledon);
            changeEquilibrium.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    try {


                        changeEquilibrium();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block

                    }
                }
            });

            rectangle =(Button) findViewById(R.id.ledoff);
            rectangle.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    try {

                        rectangle();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block

                    }
                }
            });

            circle =(Button) findViewById(R.id.circle);
            circle.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    try {

                        circle();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block

                    }
                }
            });

            infinity =(Button) findViewById(R.id.infinity);
            infinity.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    try {

                        infinity();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block

                    }
                }
            });
        }
        thread = new Thread() {
            public void run() {
                while (true) {
                    try {
                        dataSendCounter=0;
                        int i=0;
                        String s;
                        while(i<sendText.length()){

                            s=Character.toString(sendText.charAt(i));
                            outstream.write(s.getBytes());
                            ++i;
                            Thread.sleep(10);
                            dataSendCounter++;
                        }
                    } catch (Exception e) {

                    }
                }
            }

        };
        thread.start();


    }
    protected void onDestroy() {
        super.onDestroy();
        try {
            sendText="!!!!!!!!!!!!";
            while(dataSendCounter<sendText.length())
            {
                outstream.write(Character.toString(sendText.charAt(dataSendCounter)).getBytes());
                dataSendCounter++;
                Thread.sleep(10);}
            thread.join();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    protected void onStop() {
        super.onDestroy();
        try {
            sendText="!!!!!!!!!!!!";
            while(dataSendCounter<sendText.length())
            {
                outstream.write(Character.toString(sendText.charAt(dataSendCounter)).getBytes());
                dataSendCounter++;
                Thread.sleep(10);}
            thread.join();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onResume(){
        super.onResume();


        try {
            //Toast.makeText(getApplicationContext(), "Kontrol işlemi için bluetoothun açık olması gerekir.", Toast.LENGTH_SHORT).show();
            //  while(true) outstream.write(sendText.getBytes());
        }catch(Exception e)
        {}
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

    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                            long arg3) {
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
            text.setText("Cihazlar eþleþmedi");
        }
    }

    private class ConnectThread extends Thread {

        private final BluetoothSocket mmSocket;
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


            while (true) {
                try {

                    buffer = new byte[1024];
                    bytes = mmInStream.read(buffer);

                    mHandler.obtainMessage(mesajoku, bytes, -1, buffer)
                            .sendToTarget();

                } catch (IOException e) {
                    break;
                }
            }
        }


        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }





    public void changeEquilibrium() throws IOException {
        sendText="1_+220_-999$";
        //sendText="1";
        // outstream.write("1".getBytes());

    }
    public void rectangle() throws IOException {
        // outstream.write("2".getBytes());
        sendText="3_+000_-100$";
        //  sendText="2";
    }
    public void circle() throws IOException {
        // outstream.write("3".getBytes());
        sendText="4_+012_-000$";
        // sendText="3";
    }
    public void infinity() throws IOException {
        // outstream.write("4".getBytes());
        sendText="2_+000_-055$";
        //sendText="4";
    }


}

