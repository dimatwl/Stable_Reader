package org.vudroid.core;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.vudroid.R;
import org.vudroid.core.presentation.BrowserAdapter;
import org.vudroid.core.presentation.UriBrowserAdapter;

import java.io.File;
import java.io.FileFilter;

public abstract class BaseBrowserActivity extends Activity
{
    private BrowserAdapter adapter;
    private static final String CURRENT_DIRECTORY = "currentDirectory";
    private final AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener()
    {
        @SuppressWarnings({"unchecked"})
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
        {
            final File file = ((AdapterView<BrowserAdapter>)adapterView).getAdapter().getItem(i);
            if (file.isDirectory())
            {
                setCurrentDir(file);
            }
            else
            {
                showDocument(file);
            }
        }
    };
    private UriBrowserAdapter recentAdapter;
    private ViewerPreferences viewerPreferences;
    protected final FileFilter filter;

    private TabHost mTabHost;

    public BaseBrowserActivity()
    {
        this.filter = createFileFilter();
    }

    protected abstract FileFilter createFileFilter();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browser);
        viewerPreferences = new ViewerPreferences(this);
        final ListView browseList = initBrowserListView();
        final ListView recentListView = initRecentListView();
        mTabHost = (TabHost) findViewById(R.id.browserTabHost);
        mTabHost.setup();
        setupTab(browseList, "Browse");
        setupTab(recentListView, "Recent");
        //android:background="@drawable/background"
    }

    private void setupTab(final View view, final String tag) {
        Log.d("setupTab", "begin!!!!!!!!!!!");
        View tabview = createTabView(mTabHost.getContext(), tag);
        Log.d("setupTab", "1!!!!!!!!!!!");
        TabHost.TabContentFactory tabContentFactory = new TabHost.TabContentFactory() {
            public View createTabContent(String s) {
                return  view;
            }
        };
        Log.d("setupTab", "2!!!!!!!!!!!");
        TabHost.TabSpec setContent = mTabHost.newTabSpec(tag);
        Log.d("setupTab", "3!!!!!!!!!!!");
        setContent.setIndicator(tabview);
        Log.d("setupTab", "4!!!!!!!!!!!");
        setContent.setContent(tabContentFactory);

        //TabHost.TabSpec setContent = mTabHost.newTabSpec(tag).setIndicator(tag).setContent(tabContentFactory);
        Log.d("setupTab", "5!!!!!!!!!!!");
        mTabHost.addTab(setContent);
        Log.d("setupTab", "end!!!!!!!!!!!");
    }
    private static View createTabView(final Context context, final String text) {
        Log.d("createTabView", "begin!!!!!!!!!!!");
        View view = LayoutInflater.from(context).inflate(R.layout.tabsbg, null);
        Log.d("createTabView", "1!!!!!!!!!!!");
        TextView tv = (TextView) view.findViewById(R.id.tabsText);
        Log.d("createTabView", "2!!!!!!!!!!!");
        tv.setText(text);
        Log.d("createTabView", "end!!!!!!!!!!!");
        return view;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        final File sdcardPath = new File("/sdcard");
        if (sdcardPath.exists())
        {
            setCurrentDir(sdcardPath);
        }
        else
        {
            setCurrentDir(new File("/"));
        }
        if (savedInstanceState != null)
        {
            final String absolutePath = savedInstanceState.getString(CURRENT_DIRECTORY);
            if (absolutePath != null)
            {
                setCurrentDir(new File(absolutePath));
            }
        }
    }

    private ListView initBrowserListView()
    {
        final ListView listView = new ListView(this);
        adapter = new BrowserAdapter(this, filter);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(onItemClickListener);
        listView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        return listView;
    }

    private ListView initRecentListView()
    {
        ListView listView = new ListView(this);
        recentAdapter = new UriBrowserAdapter();
        listView.setAdapter(recentAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @SuppressWarnings({"unchecked"})
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                showDocument(((AdapterView<UriBrowserAdapter>) adapterView).getAdapter().getItem(i));
            }
        });
        listView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        return listView;
    }

    private void showDocument(File file)
    {
        showDocument(Uri.fromFile(file));
    }

    protected abstract void showDocument(Uri uri);

    private void setCurrentDir(File newDir)
    {
        adapter.setCurrentDirectory(newDir);
        getWindow().setTitle(newDir.getAbsolutePath());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString(CURRENT_DIRECTORY, adapter.getCurrentDirectory().getAbsolutePath());
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        recentAdapter.setUris(viewerPreferences.getRecent());
    }
}
