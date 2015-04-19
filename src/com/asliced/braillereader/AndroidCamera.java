package com.asliced.braillereader;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.asliced.braillereaderapp.R;

//public class CameraTestActivity extends Activity {
public class AndroidCamera extends Activity {
	// Our variables
	private CameraPreview cv;
	private DrawView dv;
	private FrameLayout alParent;
	private Camera camera;
	private boolean flashOn = false;

	public static ProgressDialog progressDialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);

		/*
		 * Set the screen orientation to landscape, because the camera preview
		 * will be in landscape, and if we don't do this, then we will get a
		 * streached image.
		 */
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		// requesting to turn the title OFF
		// requestWindowFeature(Window.FEATURE_NO_TITLE);

		// making it full screen
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		try{
			DotProcessor.getDic(getResources().openRawResource(R.raw.braille));
		} catch(Exception e){
			e.printStackTrace();
			this.finish();
		}
	}

	@SuppressWarnings("deprecation")
	public void Load(){
		// Try to get the camera
		camera = getCameraInstance();

		// If the camera was received, create the app
		if(camera != null){
			Parameters p = camera.getParameters();
			p.setFocusMode(Parameters.FOCUS_MODE_MACRO);
		    List<Size> sl = p.getSupportedPictureSizes();
		    int min = 0;
		    int minW = sl.get(0).width;
		    int i=0;
		    for(Size s : sl){
		    	if(s.width < minW && s.width >= 640){
		    		minW = s.width;
		    		min = i;
		    	}
		    	i++;
		    }
		    
		    System.out.println("CAMMMMMMMMMMMEEEEEEEERAAAAAAAAAAAA: " + sl.get(min).width + " : " + sl.get(min).height);
		    p.setPictureSize(sl.get(min).width, sl.get(min).height);
		    camera.setParameters(p);
			camera.autoFocus(new AutoFocusCallback(){
				@Override
				public void onAutoFocus(boolean success, Camera camera){
					System.out.println("HEY");
				}
			});
			camera.setDisplayOrientation(90);

			/*
			 * Create our layout in order to layer the draw view on top of the
			 * camera preview.
			 */
			alParent = new FrameLayout(this);
			alParent.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

			// Create a new camera view and add it to the layout
			cv = new CameraPreview(this, camera);
			alParent.addView(cv);

			// Create a new draw view and add it to the layout
			dv = new DrawView(this);
			alParent.addView(dv);

			Button myButton = new Button(this);
			myButton.setText("Take Picture");
			
			new Timer().schedule(new TimerTask(){
				@Override
				public void run(){
					try{
						camera.takePicture(null, null, new PhotoHandler(getApplicationContext(), dv));
					} catch(Exception e){
						
					}
				}
			}, 500, 800);
			
			myButton.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v){
					progressDialog = ProgressDialog.show(v.getContext(), "Please Wait",
								"Processing image...", true);	

					camera.takePicture(null, null, new PhotoHandler(getApplicationContext(), dv));
				}
			});

//			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
//					LinearLayout.LayoutParams.WRAP_CONTENT);
			//alParent.addView(myButton, params);

			// Set the layout as the apps content view
			setContentView(alParent);
		}
		// If the camera was not received, close the app
		else{
			Toast toast = Toast
					.makeText(getApplicationContext(), "Unable to find camera. Closing.", Toast.LENGTH_SHORT);
			toast.show();
			finish();
		}
	}

	/* This method is strait for the Android API */
	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance(){
		Camera c = null;

		try{
			c = Camera.open();// attempt to get a Camera instance
		} catch(Exception e){
			// Camera is not available (in use or does not exist)
			e.printStackTrace();
		}
		return c; // returns null if camera is unavailable
	}

	/*
	 * Override the onPause method so that we can release the camera when the
	 * app is closing.
	 */
	@Override
	protected void onPause(){
		super.onPause();

		if(cv != null){
			cv.onPause();
			cv = null;
		}
	}

	/*
	 * We call Load in our Resume method, because the app will close if we call
	 * it in onCreate
	 */
	@Override
	protected void onResume(){
		super.onResume();

		Load();
	}

	private void toggleFlash(){
		if(camera != null){
			if(flashOn){
				Parameters p = camera.getParameters();
				p.setFlashMode(Parameters.FLASH_MODE_OFF);
				camera.setParameters(p);
				flashOn = false;
			} else{
				Parameters p = camera.getParameters();
				p.setFlashMode(Parameters.FLASH_MODE_TORCH);
				camera.setParameters(p);
				flashOn = true;
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent e){
		if(e.getAction() == MotionEvent.ACTION_DOWN){
			camera.autoFocus(new AutoFocusCallback(){
				@Override
				public void onAutoFocus(boolean success, Camera camera){
					System.out.println("AUTOFOCUSING");
				}
			});
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.android_camera, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()){
			case R.id.flashtoggle:
				toggleFlash();
				return true;
			case R.id.threshold_up:
				PhotoHandler.threshold += 10;
				System.out.println("THRESHOLD: " + PhotoHandler.threshold);
				return true;
			case R.id.threshold_down:
				PhotoHandler.threshold -= 10;
				System.out.println("THRESHOLD: " + PhotoHandler.threshold);
				return true;
			case R.id.threshold_up2:
				PhotoHandler.threshold += 50;
				System.out.println("THRESHOLD: " + PhotoHandler.threshold);
				return true;
			case R.id.threshold_down2:
				PhotoHandler.threshold -= 50;
				System.out.println("THRESHOLD: " + PhotoHandler.threshold);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}