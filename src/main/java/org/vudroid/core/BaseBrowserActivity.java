package org.vudroid.core;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.vudroid.R;
import org.vudroid.core.presentation.AuthorAdapter;
import org.vudroid.core.presentation.BrowserAdapter;
import org.vudroid.core.presentation.UriBrowserAdapter;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Map;

import static android.R.layout;

public abstract class BaseBrowserActivity extends Activity {
    private BrowserAdapter browserAdapter;
    private AuthorAdapter authorAdapter;
    String authorsList[] = {"Mordkovich", "Vilenkin"};
    private Map<Integer, String[]> authorsMap = new HashMap<Integer, String[]>();
    private boolean isAuthorFileOpen = false;
    private ListView authorsListView;

    private static final String CURRENT_DIRECTORY = "currentDirectory";
    private final AdapterView.OnItemClickListener onItemClickListener =
            new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    switch (adapterView.getId()) {
                        case R.id.AuthorsList:
                            if(!isAuthorFileOpen){
                                int authorId = (int) ((AdapterView<ArrayAdapter>) adapterView).getAdapter().getItemId(i);
                                ((AdapterView<ArrayAdapter>) adapterView).setAdapter(null);
                                ((AdapterView<ArrayAdapter>) adapterView).setAdapter(new ArrayAdapter<String>(BaseBrowserActivity.this,
                                    layout.simple_list_item_1, authorsMap.get(authorId)));
                                isAuthorFileOpen = true;
                            }
                            break;
                        default:
                            final File file = ((AdapterView<BrowserAdapter>) adapterView).getAdapter().getItem(i);
                            if (file.isDirectory()) {
                                setCurrentDir(file);
                            } else {
                                showDocument(file);
                            }
                            break;
                    }
                }
            };
    private UriBrowserAdapter recentAdapter;
    private ViewerPreferences viewerPreferences;
    protected final FileFilter filter;

    private TabHost mTabHost;

    public BaseBrowserActivity() {
        this.filter = createFileFilter();
    }

    protected abstract FileFilter createFileFilter();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browser);
        viewerPreferences = new ViewerPreferences(this);
        final ListView browseList = initBrowserListView();
        final ListView recentListView = initRecentListView();
        final ListView authorListView = initAuthorsListView();
        initAuthorsMap();
        mTabHost = (TabHost) findViewById(R.id.browserTabHost);
        mTabHost.setup();
        setupTab(browseList, "Browse");
        setupTab(recentListView, "Recent");
        setupTab(authorListView, "Authors");
    }

    private void setupTab(final View view, final String tag) {
        View tabview = createTabView(mTabHost.getContext(), tag);
        TabHost.TabContentFactory tabContentFactory = new TabHost.TabContentFactory() {
            public View createTabContent(String s) {
                return view;
            }
        };
        TabHost.TabSpec setContent = mTabHost.newTabSpec(tag);
        setContent.setIndicator(tabview);
        setContent.setContent(tabContentFactory);

        //TabHost.TabSpec setContent = mTabHost.newTabSpec(tag).setIndicator(tag).setContent(tabContentFactory);
        mTabHost.addTab(setContent);
    }

    private static View createTabView(final Context context, final String text) {
        View view = LayoutInflater.from(context).inflate(R.layout.tabsbg, null);
        TextView tv = (TextView) view.findViewById(R.id.tabsText);
        tv.setText(text);
        return view;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        final File sdcardPath = new File("/sdcard");
        if (sdcardPath.exists()) {
            setCurrentDir(sdcardPath);
        } else {
            setCurrentDir(new File("/"));
        }
        if (savedInstanceState != null) {
            final String absolutePath = savedInstanceState.getString(CURRENT_DIRECTORY);
            if (absolutePath != null) {
                setCurrentDir(new File(absolutePath));
            }
        }
    }

    private ListView initBrowserListView() {
        final ListView listView = new ListView(this);
        browserAdapter = new BrowserAdapter(this, filter);
        listView.setAdapter(browserAdapter);
        listView.setOnItemClickListener(onItemClickListener);
        listView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        return listView;
    }

    private ListView initRecentListView() {
        ListView listView = new ListView(this);
        recentAdapter = new UriBrowserAdapter();
        listView.setAdapter(recentAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressWarnings({"unchecked"})
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showDocument(((AdapterView<UriBrowserAdapter>) adapterView).getAdapter().getItem(i));
            }
        });
        listView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        return listView;
    }

    private ListView initAuthorsListView() {
        /*final ListView listView = new ListView(this);
        authorAdapter = new AuthorAdapter(this, filter);
        listView.setAdapter(authorAdapter);
        listView.setOnItemClickListener(onItemClickListener);
        listView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        return listView;   */
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        final ListView listView = (ListView) layoutInflater.inflate(R.layout.authors, null, false);
        listView.setAdapter(new ArrayAdapter<String>(this, layout.simple_list_item_1, authorsList));
        listView.setOnItemClickListener(onItemClickListener);
        authorsListView = listView;
        return listView;
    }

    private void initAuthorsMap() {
        String booksList0[] = {"Алгебра 7 класс_Задачник_Мордкович_2001.djvu"};
        authorsMap.put(0, booksList0);
        String booksList1[] = {"Н.Я.Виленкин. Популярная комбинаторика.djvu"};
        authorsMap.put(1, booksList1);
    }

    private void showDocument(File file) {
        showDocument(Uri.fromFile(file));
    }

    protected abstract void showDocument(Uri uri);

    private void setCurrentDir(File newDir) {
        browserAdapter.setCurrentDirectory(newDir);
        getWindow().setTitle(newDir.getAbsolutePath());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && isAuthorFileOpen) {
            authorsListView.setAdapter(null);
            authorsListView.setAdapter(new ArrayAdapter<String>(this, layout.simple_list_item_1, authorsList));
            isAuthorFileOpen = false;
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CURRENT_DIRECTORY, browserAdapter.getCurrentDirectory().getAbsolutePath());
    }

    @Override
    protected void onResume() {
        super.onResume();
        recentAdapter.setUris(viewerPreferences.getRecent());
    }
}
