package com.kartikgupta.myapplication.helper;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.kartikgupta.myapplication.MagicData;
import com.kartikgupta.myapplication.R;
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

import java.io.IOException;

/**
 * Created by kartik on 1/5/17.
 */

public class NetworkConnection {

    private static final String TAG = NetworkConnection.class.getSimpleName();
    private static final String RECIEVED_MAGIC_BYTES = "recieved_magic_bytes" ;
    private static final String MAGIC_DATA = "magic_data";
    private Future<WebSocket> mWebSocket;
    private String mURL;
    private Context mContext;

    NetworkConnection(Context context){
        mContext=context;
    }


    public void initializeConnection() {
        mURL = PreferenceManager.getDefaultSharedPreferences(mContext)
                .getString(mContext.getResources().getString(R.string.pref_url_key),mContext.getResources().getString(R.string.pref_url_default));

        initializeWebSocket();
    }

    private void initializeWebSocket() {
        mWebSocket =  AsyncHttpClient.getDefaultInstance().websocket(/*"ws://10.20.1.25:8080"*/mURL, null, new AsyncHttpClient.WebSocketConnectCallback() {
            @Override
            public void onCompleted(Exception ex, WebSocket webSocket) {
                if (ex != null) {
                    ex.printStackTrace();
                    return;
                }
                webSocket.send("a string");
                // webSocket.send(new String(temp));

                webSocket.setStringCallback(new WebSocket.StringCallback() {
                    public void onStringAvailable(String s) {
                        System.out.println("I got a string: " + s);
                    }
                });

                webSocket.setDataCallback(new DataCallback() {
                    public void onDataAvailable(DataEmitter emitter, ByteBufferList byteBufferList) {
                        System.out.println("I got some bytes!");
                        Intent intent = new Intent(RECIEVED_MAGIC_BYTES);
                        intent.putExtra(MAGIC_DATA,byteBufferList.getAllByteArray());
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                        // note that this data has been read
                        byteBufferList.recycle();
                    }
                });
            }
        });
    }


    public void sendFrameBytes(byte[] imageBytes) {
        if(mWebSocket==null || mWebSocket.isCancelled()){initializeWebSocket();}
        WebSocket webtemp = mWebSocket.tryGet();
        if(webtemp!=null){mWebSocket.tryGet().send(imageBytes);
            Log.d(TAG,"trying to send bytes");
        }
    }
}
