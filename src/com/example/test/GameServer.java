package com.example.test;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import android.os.Process;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Objects;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by cymwin18 on 4/24/15.
 */
interface IGameServer {
    public int sendRequest(String req);
    public String recvResponse();
    public String getIpAddr();
    public void startSocketServer(String ipAddr);
    public void connSocketServer(String ipAddr);
}

public class GameServer extends Service {

    private Socket mSocket = null;

    @Override
    public IBinder onBind(Intent args) {
        return new GameBinder();
    }

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            long endTime = System.currentTimeMillis() + 5*1000;
            while (System.currentTimeMillis() < endTime) {
                synchronized (this) {
                    try {
                        wait(endTime - System.currentTimeMillis());
                    } catch (Exception e) {
                    }
                }
            }
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1);
        }
    }

    @Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }

    private class GameBinder extends Binder implements  IGameServer{
        public int sendRequest(String req) {
            if (mSocket == null) {
                return -1;
            }

            try {
                OutputStream outputStream = mSocket.getOutputStream();
                int i = 0;
                byte data[] = new byte[1024 * 4];

                outputStream.write(req.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }

            return 0;
        }

        public String recvResponse() {
            String ret = "";

            if (mSocket == null) {
                return null;
            }

            InputStream inputStream = null;
            try {
                inputStream = mSocket.getInputStream();
                int i = 0;
                byte data[] = new byte[1024 * 4];

                while ((i = inputStream.read(data)) != 1) {
                    ret += new String(data, 0, i);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return ret;
        }

        public String getIpAddr() {
            try {
                java.net.InetAddress test = java.net.InetAddress.getByName("localhost");
                String ipAddr = test.getLocalHost().getHostAddress();
                Log.i("Yangming", ipAddr);
                return ipAddr;
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return new String("NULL");
        }

        public void startSocketServer(String ipAddr) {
            new ServerThread().start();
        }

        public void connSocketServer(String ipAddr) {
            try {
                mSocket = new Socket(ipAddr,8888);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //线程类
    private class ServerThread extends Thread {
        public void run() {
            //声明一个ServerSocket对象
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(8888);
                //调用 ServerSocket对象的accept()方法接收客户端所发送的请求
                //accept()这个方法是一个阻塞的方法，如果客户端没有发送请求，那么代码运行到这里被阻塞，停在这里不再向下运行了，一直等待accept()函数的返回,这时候突然客户端发送一个请求，那个这个方法就会返回Socket对象，
                //Socket对象代表服务器端和客户端之间的一个连接
                mSocket = serverSocket.accept();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
