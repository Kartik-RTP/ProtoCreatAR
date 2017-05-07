package com.kartikgupta.myapplication.helper;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.kartikgupta.myapplication.R;
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

/**
 * Created by kartik on 1/5/17.
 */

public class NetworkConnection {

    private static final String TAG = NetworkConnection.class.getSimpleName();
    private static final String RECIEVED_MAGIC_BYTES = "recieved_magic_bytes" ;
    private static final String MAGIC_DATA = "magic_data";
    private Future<WebSocket> mFutureWebSocket;
    private String mURL;
    private Context mContext;
    private WebSocket mWebSocket;

    NetworkConnection(Context context){
        mContext=context;
        initializeConnection();

    }


    public void initializeConnection() {
        mURL = PreferenceManager.getDefaultSharedPreferences(mContext)
                .getString(mContext.getResources().getString(R.string.pref_url_key),mContext.getResources().getString(R.string.pref_url_default));

        initializeWebSocket();
    }

    private void initializeWebSocket() {
        mFutureWebSocket =  AsyncHttpClient.getDefaultInstance().websocket(/*"ws://10.20.1.25:8080"*/mURL, null, new AsyncHttpClient.WebSocketConnectCallback() {
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
                        Log.d(TAG,"recieved some magic");
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

        mWebSocket=mFutureWebSocket.tryGet();
    }


    public void sendFrameBytes(byte[] imageBytes) {
       // initializeWebSocket();
       // mFutureWebSocket.tryGet().send(imageBytes);


         if(mFutureWebSocket ==null || mFutureWebSocket.isCancelled()){
            Log.d(TAG,"mFutureWebSocket is maybe null");
             initializeWebSocket();
         }

         if(mWebSocket!=null){
             mWebSocket.send(imageBytes);
         }else{
             Log.d(TAG,"WebSocket is null , trying alternative way");
             mFutureWebSocket.tryGet().send(imageBytes);
         }

/*
        WebSocket webtemp = mFutureWebSocket.tryGet();

//        webtemp.send(imageBytes);
//        webtemp.isBuffering();
        if(webtemp!=null){
  //          mFutureWebSocket.tryGet().send(imageBytes);
            webtemp.send(imageBytes);
            Log.d(TAG,"trying to send bytes");
        }else{
            Log.d(TAG,"webSocket is null");
        }
*/
    }
}
