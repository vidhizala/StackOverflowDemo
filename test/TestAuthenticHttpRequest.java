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
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;



@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class TestAuthenticHttpRequest extends DisplayTestInfo{

    private DBHandler dbHandler;
    private HttpClient httpClient;
    private MainActivity mainActivity;

    @Before
    public void setup() {

        setFileName(this.getClass().getName());
        Robolectric.getFakeHttpLayer().interceptHttpRequests(false);
        Robolectric.getFakeHttpLayer().interceptResponseContent(false);

        mainActivity = Robolectric.buildActivity(MainActivity.class).create().get();
        httpClient = new HttpClient();
        dbHandler = new DBHandler(mainActivity.getApplicationContext(), null, null, 1);


    }

    @Test
    public void testAuthenticHttpRequest() throws Exception {
        try {
            beforeTest ("testAuthenticHttpRequest");
            httpClient.setResponseCode(0);
            httpClient.setQuery("java");
            String returnData = httpClient.sendPost();
            assertThat(httpClient.getResponseCode(), equalTo(200));
            boolean returnResult = dbHandler.convertResponse(returnData);
            assertTrue(returnResult);
        }
        catch(Exception e) {
            showException ("testAuthenticHttpRequest", e);
        }
    }
}
