package com.vidhi.sodemoapp;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.SearchView;
import com.robotium.solo.Solo;
import junit.framework.Assert;

public class TestAbsurdQueryWithNetwork extends ActivityInstrumentationTestCase2<MainActivity> {
    private Solo solo;
    private String absurdInput = "dsadasdasdad";

    public TestAbsurdQueryWithNetwork() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        solo = new Solo(getInstrumentation(), getActivity());
    }


    public void testAbsurdQueryWithNetwork() {

        SearchView searchView = (SearchView) solo.getView(R.id.action_search);
        solo.clickOnView(searchView);
        solo.enterText(0, absurdInput);
        solo.sendKey(Solo.ENTER);

        Assert.assertTrue(solo.searchText("Please wait..."));

        assertTrue(solo.waitForText("No results found matching your query. Please check spelling and try again."));

        solo.sleep(1000);
    }


    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }
}