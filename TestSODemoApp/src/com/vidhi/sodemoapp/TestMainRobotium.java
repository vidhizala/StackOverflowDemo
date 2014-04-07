package com.vidhi.sodemoapp;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.SearchView;
import com.robotium.solo.Solo;
import junit.framework.Assert;

public class TestMainRobotium extends ActivityInstrumentationTestCase2<MainActivity> {
    private Solo solo;

    public TestMainRobotium() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        solo = new Solo(getInstrumentation(), getActivity());
    }

    public void testSearchView() {

        SearchView searchView = (SearchView) solo.getView(R.id.action_search);
        solo.clickOnView(searchView);
        solo.enterText(0, "sqlite");
        solo.sendKey(Solo.ENTER);

        Assert.assertTrue(solo.searchText("Please wait..."));

        solo.sleep(1000);

    }


    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }
}