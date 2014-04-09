package com.vidhi.sodemoapp;

/**
 * Created by vidhi on 3/24/14.
 */

import android.util.Log;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class HttpClient {

    private String query;
    private int responseCode;

    public int getResponseCode() {
        return responseCode;
    }
    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }
    public String getQuery() {
        return query;
    }
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * Function to send http request to API wit user's search string
     * Response returned is first converted to an array of QuestionInfos and then passed to dbHandler for storing in database
     *
     */
    public String sendPost() {

        DefaultHttpClient httpClient = new DefaultHttpClient();
        try {
            HttpPost httpPostRequest = new HttpPost("http://api.stackoverflow.com/1.1/search");

            // Set HTTP parameters
            List formparams = new ArrayList();
            formparams.add(new BasicNameValuePair("intitle", this.getQuery()));
            UrlEncodedFormEntity entityParam = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPostRequest.setEntity(entityParam);
            httpPostRequest.addHeader("Content-Type","application/x-www-form-urlencoded");
            httpPostRequest.setHeader("Accept", "application/json");

            long t = System.currentTimeMillis();
            HttpResponse response = (HttpResponse) httpClient.execute(httpPostRequest);
            Log.d(MainActivity.TAG, "HTTPResponse received in [" + (System.currentTimeMillis()-t) + "ms]");

            // Get hold of the response entity (-> the data):
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                // Read the content stream
                InputStream inputStream = entity.getContent();
                Header contentEncoding = response.getFirstHeader("Content-Encoding");
                if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
                    inputStream = new GZIPInputStream(inputStream);
                }
                int statusCode = response.getStatusLine().getStatusCode();
                Log.d(MainActivity.TAG, "status code ="+statusCode);

                this.setResponseCode(statusCode);

                // convert content stream to a String
                String resultString= convertStreamToString(inputStream);
                inputStream.close();
                return resultString;
            }

        }catch (Exception e)
        {
            Log.d(MainActivity.TAG, "Error occured: ", e);

        }finally{
            httpClient.getConnectionManager().shutdown();
        }
        return null;


    }

    private static String convertStreamToString(InputStream inputStream) {
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
        try {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }
        } catch (IOException e) {
            Log.d(MainActivity.TAG, "error :", e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
               Log.d(MainActivity.TAG, "error :", e);
            }
        }
        return stringBuilder.toString();
    }


}