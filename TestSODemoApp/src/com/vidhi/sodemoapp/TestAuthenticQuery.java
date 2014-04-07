package com.vidhi.sodemoapp;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.Adapter;
import android.widget.ListView;
import android.widget.SearchView;
import com.robotium.solo.Solo;
import junit.framework.Assert;

public class TestAuthenticQuery extends ActivityInstrumentationTestCase2<MainActivity> {
    private Solo solo;

    public TestAuthenticQuery() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        solo = new Solo(getInstrumentation(), getActivity());
    }


    public void testAuthenticQuery() {

        SearchView searchView = (SearchView) solo.getView(R.id.action_search);
        solo.clickOnView(searchView);
        solo.enterText(0, "sqlite");
        solo.sendKey(Solo.ENTER);

        Assert.assertTrue(solo.searchText("Please wait..."));

        solo.waitForView(R.id.list);
        solo.sleep(4000);
        ListView view = (ListView) solo.getView(R.id.list);
        Adapter adapter = view.getAdapter();
        assertNotSame(view.getAdapter().getCount(), 0);

        QuestionInfo questionInfo = (QuestionInfo) (adapter.getItem(0));
        Assert.assertTrue(solo.searchText(questionInfo.getQuestion()));
        solo.sleep(1000);
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }
}