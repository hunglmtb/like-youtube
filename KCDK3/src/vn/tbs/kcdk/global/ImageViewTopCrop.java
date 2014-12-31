package vn.tbs.kcdk.global;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * @author doriancussen
 */
public class ImageViewTopCrop extends ImageView
{
    private float mOriginScaleFactor = -1;
	private boolean mKeepRatio = false;

	public ImageViewTopCrop(Context context)
    {
        super(context);
        setScaleType(ScaleType.MATRIX);
    }

    public ImageViewTopCrop(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setScaleType(ScaleType.MATRIX);
    }

    public ImageViewTopCrop(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        setScaleType(ScaleType.MATRIX);
    }
    
    

    public void setOriginScaleFactor(float mOriginScaleFactor) {
		this.mOriginScaleFactor = mOriginScaleFactor;
	}

	public void setKeepRatio(boolean mKeepRatio) {
		this.mKeepRatio = mKeepRatio;
	}

	@Override
    protected boolean setFrame(int l, int t, int r, int b)
    {
    	if (mKeepRatio) {
            setScaleType(ScaleType.MATRIX);
    		Matrix matrix = getImageMatrix();
    		float xscaleFactor = getWidth()/(float)getDrawable().getIntrinsicWidth();
    		float yscaleFactor = getHeight()/(float)getDrawable().getIntrinsicHeight();
    		float scaleFactor = Math.min(xscaleFactor, yscaleFactor);
    		mOriginScaleFactor = Math.max(scaleFactor, mOriginScaleFactor);
    		
    		float frameWidth = r - l;
//		float frameHeight = b - t;
    		
    		float originalImageWidth = (float)getDrawable().getIntrinsicWidth();
//		float originalImageHeight = (float)getDrawable().getIntrinsicHeight();
    		float newImageWidth = originalImageWidth * mOriginScaleFactor;
//		float newImageHeight = originalImageHeight * mOriginScaleFactor;
    		matrix.setScale(mOriginScaleFactor, mOriginScaleFactor, 0, 0);
    		matrix.postTranslate((frameWidth - newImageWidth) /2, 0);
    		setImageMatrix(matrix);
			
		}
    	else{
            setScaleType(ScaleType.FIT_CENTER);

    	}
        return super.setFrame(l, t, r, b);
    }
}
