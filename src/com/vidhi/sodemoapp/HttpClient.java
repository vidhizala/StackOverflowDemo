package com.vidhi.sodemoapp;

/**
 * Created by vidhi on 3/24/14.
 */

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class HttpClient {

/**
 * Function to send http request to API wit user's search string
 * Response returned is first converted to an array of QuestionInfos and then passed to dbHandler for storing in database
 *
 */
public void sendHttpRequest(final String method, final String url, final HashMap data, final Handler successHandler, final Handler failureHandler) {

    new Thread(new Runnable() {
        public void run() {

            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
            HttpConnectionParams.setSoTimeout(httpParams, 10000);
            org.apache.http.client.HttpClient httpClient = new DefaultHttpClient(httpParams);
            HttpResponse response = null;

            try {
                    if(method == "GET"){

                        // Set HTTP parameters
                        List formparams = new ArrayList();
                        formparams.add(new BasicNameValuePair("intitle", data.get("query").toString()));
                        formparams.add(new BasicNameValuePair("site", data.get("site").toString()));
                        String paramString = URLEncodedUtils.format(formparams, "utf-8");

                        HttpGet httpGetRequest = new HttpGet(url+paramString);
                        httpGetRequest.addHeader("Content-Type", "application/x-www-form-urlencoded");
                        httpGetRequest.setHeader("Accept", "application/json");

                        long t = System.currentTimeMillis();
                        response = (HttpResponse) httpClient.execute(httpGetRequest);
                        Log.d(MainActivity.TAG, "HTTPResponse received in [" + (System.currentTimeMillis()-t) + "ms]");

                        int responseCode = response.getStatusLine().getStatusCode();
                        Log.d(MainActivity.TAG, "status code =" + responseCode);

                        HttpEntity entity = response.getEntity();

                        if (entity != null) {
                            // Read the content stream
                            InputStream inputStream = entity.getContent();
                            Header contentEncoding = response.getFirstHeader("Content-Encoding");
                            if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
                                inputStream = new GZIPInputStream(inputStream);
                            }

                            String resultString = null;

                            try{
                                // convert content stream to a String
                                resultString = convertStreamToString(inputStream);
                            }catch(Exception e){
                                HashMap messageResponse = new HashMap();
                                messageResponse.put("responseCode", responseCode);
                                Message httpMessage = failureHandler.obtainMessage();
                                httpMessage.obj = messageResponse;
                                failureHandler.sendMessage(httpMessage);

                            }finally{
                                inputStream.close();
                            }
                            HashMap messageResponse = new HashMap();
                            messageResponse.put("responseCode", responseCode);
                            messageResponse.put("response", resultString);
                            Message httpMessage = successHandler.obtainMessage();
                            httpMessage.obj = messageResponse;
                            successHandler.sendMessage(httpMessage);
                         }
                    }

            }catch (Exception e){
                Log.d(MainActivity.TAG, "Error occured: ", e);
                HashMap messageResponse = new HashMap();
                messageResponse.put("responseCode", 0);
                Message httpMessage = failureHandler.obtainMessage();
                httpMessage.obj = messageResponse;
                failureHandler.sendMessage(httpMessage);
            }finally{
                httpClient.getConnectionManager().shutdown();
            }
        }

    }).start();
}

    private static String convertStreamToString(InputStream inputStream) throws IOException {
		/*
		 * To convert the InputStream to String we use the BufferedReader.readLine()
		 * method. We iterate until the BufferedReader return null which means
		 * there's no more data to read. Each line will appended to a StringBuilder
		 * and returned as String.
		 *
		 */
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line + "\n");
        }
        inputStream.close();
        return stringBuilder.toString();
    }


}