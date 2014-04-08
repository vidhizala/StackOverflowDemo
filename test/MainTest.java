import com.vidhi.sodemoapp.DBHandler;
import com.vidhi.sodemoapp.MainActivity;
import com.vidhi.sodemoapp.QuestionInfo;
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
public class MainTest extends DisplayTestInfo{

    private DBHandler dbHandler;
    private ArrayList<QuestionInfo> dummyObject;
    private MainActivity mainActivity;

    @Before
    public void setup() {
        mainActivity = Robolectric.buildActivity(MainActivity.class).create().get();
        dbHandler = new DBHandler(mainActivity.getApplicationContext(), null, null, 1);
        String fileName = System.getProperty("fileName");

        System.out.println("Reading dummy data file : " + fileName);
        String rawData = readFile(fileName);
        if (dbHandler.convertResponse(rawData)) {
            long returnID = dbHandler.addToMaster("html");
            dbHandler.storeData(returnID);
        }
        dummyObject = dbHandler.getQuestionInfos(); //get the object, the one that is added in db
    }

    @Test
    public void testGetHttpResponse() throws Exception{

        beforeTest("testGetHttpResponse");
        try{
            ArrayList<QuestionInfo> dbFetchedObject = dbHandler.fetchFromDatabase("html"); //get data from db and convert it to object for comparison
            Assert.assertEquals(dummyObject, dbFetchedObject);
        }catch(Exception e){
            showException ("testGetHttpResponse", e);
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
                if (reader !=null){
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
