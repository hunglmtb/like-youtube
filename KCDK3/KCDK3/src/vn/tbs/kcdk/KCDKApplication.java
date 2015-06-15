package vn.tbs.kcdk;

import vn.tbs.kcdk.global.Common;
import android.app.Application;
import android.content.SharedPreferences;

import com.novoda.imageloader.core.ImageManager;
import com.novoda.imageloader.core.LoaderSettings;
import com.novoda.imageloader.core.LoaderSettings.SettingsBuilder;
import com.novoda.imageloader.core.cache.LruBitmapCache;
import com.novoda.imageloader.core.model.ImageTagFactory;

public class KCDKApplication extends Application {

    /**
     * It is possible to keep a static reference across the
     * application of the image loader.
     */
    private static ImageManager imageManager;
    private static final int SIZE = 400;
    
    private static KCDKApplication instance;

    public static KCDKApplication getInstance() {
        return instance;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        normalImageManagerSettings();
    }

    /**
     * Normal image manager settings
     */
    private void normalImageManagerSettings() {
        imageManager = new ImageManager(this, new SettingsBuilder()
                .withCacheManager(new LruBitmapCache(this))
                .build(this));
    }

    /**
     * There are different settings that you can use to customize
     * the usage of the image loader for your application.
     */
    @SuppressWarnings("unused")
    private void verboseImageManagerSettings() {
        SettingsBuilder settingsBuilder = new SettingsBuilder();

        //You can force the urlConnection to disconnect after every call.
        settingsBuilder.withDisconnectOnEveryCall(true);

        //We have different types of cache, check cache package for more info
        settingsBuilder.withCacheManager(new LruBitmapCache(this));

        //You can set a specific read timeout
        settingsBuilder.withReadTimeout(30000);

        //You can set a specific connection timeout
        settingsBuilder.withConnectionTimeout(30000);

        //You can disable the multi-threading ability to download image 
        settingsBuilder.withAsyncTasks(false);
        		
        //You can set a specific directory for caching files on the sdcard
        //settingsBuilder.withCacheDir(new File("/something"));

        //Setting this to false means that file cache will use the url without the query part
        //for the generation of the hashname
        settingsBuilder.withEnableQueryInHashGeneration(false);

        LoaderSettings loaderSettings = settingsBuilder.build(this);
        imageManager = new ImageManager(this, loaderSettings);
    }

    /**
     * Convenient method of access the imageLoader
     */
    public static ImageManager getImageLoader() {
        return imageManager;
    }

	public static ImageTagFactory getImageTagFactory() {
		ImageTagFactory imageTagFactory = ImageTagFactory.newInstance(SIZE, SIZE, R.drawable.empty_photo);
        imageTagFactory.setSaveThumbnail(false);
		return imageTagFactory;
	}
	
	
	public String getUserKey() {
		SharedPreferences shared = getSharedPreferences(getPackageName(), MODE_PRIVATE);
		String userKey = shared.getString(Common.USER_KEY, null);
		return userKey;
	}
	
	public void saveUserKey(String userKey) {
		if (Common.validateString(userKey)) {
			SharedPreferences shared = getSharedPreferences(getPackageName(), MODE_PRIVATE);
			SharedPreferences.Editor editor = shared.edit();
			editor.putString(Common.USER_KEY, userKey);
			editor.commit();
		}
	}

}
