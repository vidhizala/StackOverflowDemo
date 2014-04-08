package com.vidhi.sodemoapp;


import android.test.ActivityInstrumentationTestCase2;
import android.widget.SearchView;
import com.robotium.solo.Solo;

/**
 * Created by vidhi on 4/8/14.
 */
public class TestAbsurdQueryWithoutNetwork extends ActivityInstrumentationTestCase2<MainActivity> {

    private Solo solo;
    private String absurdInput = "dsadasdasdad";

    public TestAbsurdQueryWithoutNetwork() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        solo = new Solo(getInstrumentation(), getActivity());
    }

    public void testAbsurdQueryWithoutNetwork() {

        SearchView searchView = (SearchView) solo.getView(R.id.action_search);
        solo.clickOnView(searchView);
        solo.enterText(0, absurdInput);
        solo.sendKey(Solo.ENTER);
        assertTrue(solo.waitForText("No Response from Server. Please check your connectivity and try again."));
        solo.sleep(1000);
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }
}
