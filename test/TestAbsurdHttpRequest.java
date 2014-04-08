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
public class TestAbsurdHttpRequest extends DisplayTestInfo{

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

    @Test
    public void testAbsurdHttpRequest() throws Exception {
        try {

            beforeTest ("testAbsurdHttpRequest");
            httpClient.setQuery(absurdInput);
            String returnData = httpClient.sendPost();
            boolean returnResult = dbHandler.convertResponse(returnData);
            assertThat(httpClient.getResponseCode(), equalTo(200));
            assertFalse(returnResult);
        }
        catch(Exception e) {
            showException ("testAbsurdHttpRequest", e);
        }
    }
}
