
import android.app.Application;

import android.view.*;

import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import org.robolectric.tester.android.view.TestMenu;
import org.robolectric.tester.android.view.TestMenuItem;
import android.app.Instrumentation;
import org.robolectric.annotation.Config;
import com.vidhi.sodemoapp.R;
import com.vidhi.sodemoapp.MainActivity;
import org.robolectric.RoboInstrumentation;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.shadows.ShadowView;
import org.junit.runner.RunWith;

import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;


import java.lang.System;
import java.util.ArrayList;

import com.vidhi.sodemoapp.DBHandler;
import com.vidhi.sodemoapp.QuestionInfo;


import static org.junit.Assert.*;

import android.view.KeyEvent;


@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class MenuInflaterTest {

    private MainActivity mainActivity;
    private Application context;
    private SearchView searchView;
    private DBHandler dbHandler;

    @Before
    public void setup() {
        mainActivity = Robolectric.buildActivity(MainActivity.class).create().get();
        context = mainActivity.getApplication();
    }

    @Test
    public void canRetrieveMenuListAndFindMenuItemById() {
        TestMenu menu = new TestMenu(mainActivity);
        new MenuInflater(mainActivity).inflate(R.menu.menu, menu);
        TestMenuItem testMenuItem = (TestMenuItem) menu.getItem(0);
        assertEquals("@android:drawable/ic_menu_search", testMenuItem.getTitle().toString());
    }

    @Test
    public void canRetrieveSearchView(){
        TestMenu menu = new TestMenu(mainActivity);
        new MenuInflater(mainActivity).inflate(R.menu.menu, menu);
        TestMenuItem  searchMenuItem =(TestMenuItem) menu.findItem(R.id.action_search);
        searchMenuItem.expandActionView();
        searchView = (SearchView) searchMenuItem.getActionView();
        assertNotNull(searchView);
    }
}