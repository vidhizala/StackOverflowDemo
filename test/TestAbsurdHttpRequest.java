import com.vidhi.sodemoapp.DBHandler;
import com.vidhi.sodemoapp.HttpClient;
import com.vidhi.sodemoapp.MainActivity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;



@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class TestAbsurdHttpRequest {

    private DBHandler dbHandler;
    private HttpClient httpClient;
    private MainActivity mainActivity;
    private String absurdInput = "dsadasdasdad";

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
        System.out.println ("Function Name: " + functionName);
        System.out.println ("Source file name: TestAbsurdHttpRequest.java");
        System.out.println ("Testing started...");

    }

    public void showException (String functionName, Exception e) {
        System.out.println ("\n------------------\n Exception Details \n------------------");
        System.out.println ("Function Name: " + functionName);
        System.out.println ("Source file name: TestAbsurdHttpRequest.java");
        System.out.println ("Exception: \n" + e .getMessage ());
    }

    @Test
    public void testAbsurdHttpRequest() throws Exception {
        try {

            beforeTest ("AbsurdHttpRequest");
            httpClient.setQuery(absurdInput);
            String returnData = httpClient.sendPost();
            boolean returnResult = dbHandler.convertResponse(returnData);
            assertThat(httpClient.getResponseCode(), equalTo(200));
            assertFalse(returnResult);
        }
        catch(Exception e) {
            showException ("AbsurdHttpRequest", e);
        }
    }
}
