package com.appgyver.xwalkiframes;

import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkView;

import android.app.Activity;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebResourceResponse;
import android.widget.RelativeLayout;

import java.io.IOException;
import java.io.InputStream;

public class XWalkViewActivity extends Activity {
    private final String TAG = "poc";

    private final static boolean IS_TEXTURE_VIEW_ENABLED = true;

    private final static String TARGET_URL = "http://localhost/parent.html";

    private final static int HOW_MANY = 3;

    private final static int WIDTH = 1000;
    private final static int HEIGHT = 1000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Having debugger doesn't have an effect on the behaviour
        XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, true);

        XWalkPreferences.setValue(XWalkPreferences.ANIMATABLE_XWALK_VIEW, IS_TEXTURE_VIEW_ENABLED);

        RelativeLayout root = new RelativeLayout(this);

        loadXWalkViews(root);

        setContentView(root);

    }


    private void loadXWalkViews(RelativeLayout root) {
        for (int i = 0; i < HOW_MANY; i++) {
            XWalkView xWalkView = new XWalkView(this, this);
            xWalkView.setResourceClient(initResourceClient(xWalkView));
            xWalkView.setX(getX(i));
            xWalkView.setY(getY(i));
            xWalkView.load(TARGET_URL, null);

            root.addView(xWalkView, WIDTH, HEIGHT);
        }
    }

    private int getY(int i) {
        return (i % 5) * 100;
    }

    private int getX(int i) {
        return i * 25;
    }

    private XWalkResourceClient initResourceClient(XWalkView xWalkView) {
        return new XWalkResourceClient(xWalkView) {
            @Override
            public WebResourceResponse shouldInterceptLoadRequest(XWalkView view, String uriString) {
                try {
                    Uri uri = Uri.parse(uriString);

                    if (isLocalhostRequest(uri)) {
                        Log.d(TAG, "Will intercept request " + uri.getPath());
                        InputStream data = getAssetFile(uri.getPath().substring(1)); // strip leading slah

                        if (data == null) {
                            Log.wtf(TAG, "No content for intercepted request: " + uriString);
                        }

                        return new WebResourceResponse("text/html", "UTF-8", data);
                    }
                } catch (Throwable thw) {
                    Log.wtf(TAG, "Exception trying to get resource for intercepted request: "
                            + uriString, thw);
                }

                Log.d(TAG, "Did not intercept request");
                return null;
            }

        };
    }
    private InputStream getAssetFile(String path) {
        InputStream data;
        AssetManager assetManager = getApplicationContext().getAssets();

        try {
            Log.d(TAG, "Open asset " + path);
            data = assetManager.open(path);
        } catch (IOException e) {
            data = null;
        }

        return data;
    }

    public boolean isLocalhostRequest(Uri uri) {
        return uri.getScheme().equals("http") && uri.getHost().equals("localhost");
    }

}
