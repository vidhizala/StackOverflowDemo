import android.os.Handler;
import android.os.Message;
import com.vidhi.sodemoapp.HttpClient;
import com.vidhi.sodemoapp.MainActivity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

import static junit.framework.Assert.assertEquals;


@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class TestAuthenticHttpRequest extends DisplayTestInfo{

    private HttpClient httpClient;
    private MainActivity mainActivity;

    @Before
    public void setup() {
        setFileName(this.getClass().getName());

        mainActivity = Robolectric.buildActivity(MainActivity.class).create().get();
        Robolectric.getFakeHttpLayer().interceptHttpRequests(false);
        Robolectric.getFakeHttpLayer().interceptResponseContent(false);
        httpClient = new HttpClient();
    }

    @Test
    public void testAuthenticHttpRequest() throws Exception {
        try {

            beforeTest ("testAuthenticHttpRequest");
            HashMap data = new HashMap();
            data.put("query", "html");
            data.put("site", "stackoverflow");
            httpClient.sendHttpRequest("GET", "http://api.stackexchange.com/2.2/search?", data, dummyHandler, dummyHandler);
            Thread.sleep(1000); // this timeout is necessary for preventing next test's http call to overlap this one
        }
        catch(Exception e) {
            showException ("testAuthenticHttpRequest", e);
        }
    }

    private Handler dummyHandler = new Handler(){
        public void handleMessage(Message msg) {
            HashMap result = (HashMap) msg.obj;
            int responseCode = Integer.parseInt(result.get("responseCode").toString());
            assertEquals(responseCode, 200);
        }
    };
}
