import com.vidhi.sodemoapp.DataController;
import com.vidhi.sodemoapp.MainActivity;
import com.vidhi.sodemoapp.QuestionInfo;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.*;
import java.util.ArrayList;


@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class TestMockHttpResponse extends DisplayTestInfo{

    private DataController dataController;
    private ArrayList<QuestionInfo> dummyObject;
    private MainActivity mainActivity;
    private String fileName;
    private JSONObject jObject;

    @Before
    public void setup() {

        setFileName(this.getClass().getName());
        mainActivity = Robolectric.buildActivity(MainActivity.class).create().get();
        fileName = System.getProperty("fileName");
        dataController = new DataController(mainActivity);

        String rawData = readFile(fileName);
        try {
            jObject = new JSONObject(rawData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println("Reading dummy data file : " + fileName);

    }

    @Test
    public void testMockHttpRequestAbsurdQuery() throws Exception{

        beforeTest("testMockHttpRequestAbsurdQuery");
        try{
            dummyObject = dataController.convertResponse(jObject.get("testMockHttpRequestAbsurdQuery").toString());
            Assert.assertNull(dummyObject);
        }catch(Exception e){
            showException ("testMockHttpRequestAbsurdQuery", e);
        }
    }

    public String readFile(String filename){
        File file = new File(filename);
        StringBuilder line = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String text = null;
            while ((text = reader.readLine()) != null) {
                line.append(text);
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally
        {
            try {
                if (reader != null){
                    reader.close();
                }
            }
            catch (IOException e)
            {
                showException ("readFile", e);
            }
        }
        return line.toString();
    }

}
