
import android.app.Application;
import android.view.MenuInflater;
import android.widget.SearchView;
import com.vidhi.sodemoapp.DBHandler;
import com.vidhi.sodemoapp.MainActivity;
import com.vidhi.sodemoapp.R;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.tester.android.view.TestMenu;
import org.robolectric.tester.android.view.TestMenuItem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class MenuInflaterTest extends DisplayTestInfo{

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

        try{
            beforeTest ("canRetrieveMenuListAndFindMenuItemById");
            TestMenu menu = new TestMenu(mainActivity);
            new MenuInflater(mainActivity).inflate(R.menu.menu, menu);
            TestMenuItem testMenuItem = (TestMenuItem) menu.getItem(0);
            assertEquals("@android:drawable/ic_menu_search", testMenuItem.getTitle().toString());
        }
        catch(Exception e) {
            showException ("canRetrieveMenuListAndFindMenuItemById", e);
        }
    }

    @Test
    public void canRetrieveSearchView(){
        try{
            beforeTest("canRetrieveSearchView");
            TestMenu menu = new TestMenu(mainActivity);
            new MenuInflater(mainActivity).inflate(R.menu.menu, menu);
            TestMenuItem  searchMenuItem =(TestMenuItem) menu.findItem(R.id.action_search);
            searchMenuItem.expandActionView();
            searchView = (SearchView) searchMenuItem.getActionView();
            assertNotNull(searchView);
        }
        catch(Exception e) {
            showException ("canRetrieveSearchView", e);
        }
    }
}