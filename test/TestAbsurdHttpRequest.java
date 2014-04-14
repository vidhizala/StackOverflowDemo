import android.os.Handler;
import android.os.Message;
import com.vidhi.sodemoapp.DataController;
import com.vidhi.sodemoapp.HttpClient;
import com.vidhi.sodemoapp.MainActivity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class TestAbsurdHttpRequest extends DisplayTestInfo{

    private DataController dataController;
    private HttpClient httpClient;
    private MainActivity mainActivity;
    private String absurdInput = "dsadasdasdad";

    @Before
    public void setup() {
        setFileName(this.getClass().getName());
        mainActivity = Robolectric.buildActivity(MainActivity.class).create().get();
        Robolectric.getFakeHttpLayer().interceptHttpRequests(false);
        Robolectric.getFakeHttpLayer().interceptResponseContent(false);
        httpClient = new HttpClient();
        dataController = new DataController(mainActivity);
    }

    @Test
    public void testAbsurdHttpRequest() throws Exception {
        try {

            beforeTest ("testAbsurdHttpRequest");
            HashMap data = new HashMap();
            data.put("query", absurdInput);
            data.put("site", "stackoverflow");
            httpClient.sendHttpRequest("GET", "http://api.stackexchange.com/2.2/search?", data, dummyHandler, dummyHandler);
            Thread.sleep(1000); // this timeout is necessary for preventing next test's http call to overlap this one
        }
        catch(Exception e) {
            showException ("testAbsurdHttpRequest", e);
        }
    }

    private Handler dummyHandler = new Handler(){
        public void handleMessage(Message msg) {
            HashMap result = (HashMap) msg.obj;
            int responseCode = Integer.parseInt(result.get("responseCode").toString());

            //response Code should be 200
            assertEquals(responseCode, 200);

            //but no actual data should come in the "questions" part
            assertNull(dataController.convertResponse(result.get("response").toString()));
        }
    };

}
