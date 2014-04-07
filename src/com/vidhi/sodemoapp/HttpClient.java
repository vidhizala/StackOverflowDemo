package com.vidhi.sodemoapp;

/**
 * Created by vidhi on 3/24/14.
 */

import android.util.Log;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
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

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
        String formattedDate = sdf.format(date);
        Log.d("soappdemo","Post request starts:" + formattedDate);

        try {
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPostRequest = new HttpPost("http://api.stackoverflow.com/1.1/search?intitle="+this.getQuery());

            // Set HTTP parameters
            httpPostRequest.setHeader("Accept", "application/json");
            httpPostRequest.setHeader("Content-type", "application/json");

            long t = System.currentTimeMillis();
            HttpResponse response = (HttpResponse) httpclient.execute(httpPostRequest);
            Log.d("soappdemo", "HTTPResponse received in [" + (System.currentTimeMillis()-t) + "ms]");

            // Get hold of the response entity (-> the data):
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                // Read the content stream
                InputStream instream = entity.getContent();
                Header contentEncoding = response.getFirstHeader("Content-Encoding");
                if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
                    instream = new GZIPInputStream(instream);
                }
                int statuscode = response.getStatusLine().getStatusCode();
                Log.d("soappdemo", "status code ="+statuscode );

                this.setResponseCode(statuscode);

                // convert content stream to a String
                String resultString= convertStreamToString(instream);
                instream.close();
                return resultString;
            }

        }
        catch (Exception e)
        {
            Log.d("sodemoapp", "error :" + e.getMessage());
        }
        return null;


    }

    private static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the BufferedReader.readLine()
		 * method. We iterate until the BufferedReader return null which means
		 * there's no more data to read. Each line will appended to a StringBuilder
		 * and returned as String.
		 *
		 */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            Log.d("sodemoapp", "error :"+e.getMessage());
        } finally {
            try {
                is.close();
            } catch (IOException e) {
               Log.d("sodemoapp", "error :"+e.getMessage());
            }
        }
        Log.d("soappdemo", "returning string ="+sb.toString());

        return sb.toString();
    }


}