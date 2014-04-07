import android.app.Activity;
import android.util.Log;


import com.vidhi.sodemoapp.DBHandler;
import com.vidhi.sodemoapp.HttpClient;
import com.vidhi.sodemoapp.MainActivity;
import com.vidhi.sodemoapp.QuestionInfo;
import static org.junit.Assert.*;

import dalvik.annotation.TestTarget;
import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import java.io.*;
import java.lang.AssertionError;
import java.lang.Exception;
import java.lang.System;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Assert;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.CoreMatchers.*;

import org.robolectric.tester.org.apache.http.FakeHttpLayer;



@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class TestAuthenticHttpRequest {

    private DBHandler dbHandler;
    private HttpClient httpClient;
    private MainActivity mainActivity;

    @Before
    public void setup() {


        Robolectric.getFakeHttpLayer().interceptHttpRequests(false);
        Robolectric.getFakeHttpLayer().interceptResponseContent(false);

        mainActivity = Robolectric.buildActivity(MainActivity.class).create().get();
        httpClient = new HttpClient();
        dbHandler = new DBHandler(mainActivity.getApplicationContext(), null, null, 1);


    }

    public void beforeTest (String functionName) throws Exception {
        System.out.println ("\n\n------------------\n Function Details \n------------------");
        System.out.println ("Function Name: "+ functionName);
        System.out.println ("Source file name: TestAuthenticHttpRequest.java");
        System.out.println ("Testing started...");

    }

    public void showException (String functionName, Exception e) {
        System.out.println ("\n------------------\n Exception Details \n------------------");
        System.out.println ("Function Name: "+ functionName);
        System.out.println ("Source file name: TestAuthenticHttpRequest.java");
        System.out.println ("Exception: \n"+e.getMessage ());
    }

    @Test
    public void testAuthenticHttpRequest() throws Exception {
        try {
            beforeTest ("AuthenticHttpRequest");
            httpClient.setQuery("java");
            String returnData = httpClient.sendPost();
            assertThat(httpClient.getResponseCode(), equalTo(200));
            boolean returnResult = dbHandler.convertResponse(returnData);
            assertTrue(returnResult);
        }
        catch(Exception e) {
            showException ("AuthenticHttpRequest", e);
        }
    }
}
