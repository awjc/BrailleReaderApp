package com.asliced.braillereader;

//import static com.googlecode.javacv.cpp.opencv_core.*;
//import static com.googlecode.javacv.cpp.opencv_imgproc.*;
//import static com.googlecode.javacv.cpp.opencv_highgui.*;

import static com.googlecode.javacv.cpp.opencv_core.CV_AA;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSize;
import static com.googlecode.javacv.cpp.opencv_core.cvPoint;
import static com.googlecode.javacv.cpp.opencv_core.cvPointFrom32f;
import static com.googlecode.javacv.cpp.opencv_core.cvRectangle;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_HOUGH_GRADIENT;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_RGB2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_RGB2RGBA;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvHoughCircles;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Environment;
import android.util.Pair;

import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvPoint2D32f;
import com.googlecode.javacv.cpp.opencv_core.CvPoint3D32f;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class PhotoHandler implements PictureCallback {
//	private boolean trimBlobs = true;

	public static int threshold = 100;
	
//	private final Context context;
	private DrawView dv;

	public PhotoHandler(Context context, DrawView dv) {
//		this.context = context;
		this.dv = dv;
	}

	@Override
	public void onPictureTaken(final byte[] data, Camera camera){
		// IplImage img = processImage(data, size.width, size.height);
		// IplImage img = cvCreateImage(cvSize(100, 100), 8, 1);
		// IplImage img = cvDecodeImage(cvMat(1, data.length, CV_8UC1, new
		// BytePointer(data)));
		//
		// cvSaveImage("/storage/sdcard0/test.bmp", img);

		new Thread(new Runnable(){
			@Override
			public void run(){
//				String filename = "/storage/sdcard0/temppics/"
//						+ new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(new Date()) + ".jpg";
				Bitmap bitmap = getImage(data);
				int roil = (int) (dv.getROILeft()*bitmap.getWidth());
				int roit = (int) (dv.getROITop()*bitmap.getHeight());
				int roiw = (int) (dv.getROIRight()*bitmap.getWidth() - roil);
				int roih = (int) (dv.getROIBottom()*bitmap.getHeight() - roit);
				
				Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, roil, roit, roiw, roih);
				IplImage image = bmpToIpl(croppedBitmap);
			
				Pair<IplImage, List<Point>> processed = processImage(image);
				IplImage processed2 = IplImage.create(cvGetSize(image), 8, 4);
//				cvCvtColor(processed, processed2, CV_RGB2RGBA);
				cvCvtColor(processed.first, processed2, CV_RGB2RGBA);
				
				List<Point> dots = processed.second;
				
				String result = null;
				try{
					result = DotProcessor.processDots(dots);
					System.out.println("FINAL RESULT: " + result);
				} catch(Exception e){
					e.printStackTrace();
				} finally {
					dv.setString(result);
				}
				
				dv.setBitmap(iplToBMP(croppedBitmap.getWidth(), croppedBitmap.getHeight(), processed2));
				dv.postInvalidate();
							
				croppedBitmap.recycle();
				bitmap.recycle();
		

				if(AndroidCamera.progressDialog != null){
					AndroidCamera.progressDialog.dismiss();
				}
			}
		}).start();
	}
	
	private Bitmap iplToBMP(int width, int height, IplImage img){
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		ByteBuffer bb = img.getByteBuffer();
		bitmap.copyPixelsFromBuffer(bb);
		
		return bitmap;
	}
	
	private static class Circle{
		private CvPoint p;
//		private int radius;
		
		public Circle(CvPoint p, int radius){
			this.p = p;
//			this.radius = radius;
		}
	}


	private Pair<IplImage, List<Point>> processImage(IplImage img){
		IplImage gray = cvCreateImage(cvGetSize(img), 8, 1);
		cvCvtColor(img, gray, CV_RGB2GRAY);
//		cvSmooth(gray, gray, CV_GAUSSIAN, 3);

//		cvCanny(gray, edged, threshold, 3 * threshold, 3);
//		IplConvKernel structel = opencv_imgproc.cvCreateStructuringElementEx(3, 3, 1, 1, opencv_imgproc.CV_SHAPE_CROSS,
//				null);
//		IplConvKernel structel2 = opencv_imgproc.cvCreateStructuringElementEx(3, 3, 1, 1,
//				opencv_imgproc.CV_SHAPE_ELLIPSE, null);
//		cvDilate(edged, edged, structel, 1);
//		cvErode(edged, edged, structel2, 1);

		// IplImage finalimg = findDots(edged);
//		IplImage finalimg = edged;
		
		CvMemStorage mem = CvMemStorage.create();

		CvSeq circles = cvHoughCircles(gray, // Input image
				mem, // Memory Storage
				CV_HOUGH_GRADIENT, // Detection method
				1, // Inverse ratio of resolution
				10, // Minimum distance between the centers of the detected
					// circles
				threshold + 1, // Higher threshold for canny edge detector
				8, // Threshold at the center detection stage
				3, // min radius
				10 // max radius
		);
		
		List<Circle> circlez = new ArrayList<Circle>();
		
		for(int i = 0; i < circles.total(); i++){
			CvPoint3D32f circle = new CvPoint3D32f(cvGetSeqElem(circles, i));
			CvPoint center = cvPointFrom32f(new CvPoint2D32f(circle.x(), circle.y()));
			int radius = Math.round(circle.z());
			circlez.add(new Circle(center, radius));
		}
	
		int maxCircles = 100;
		int nCirc = 0;
		IplImage src = IplImage.create(cvGetSize(img), 8, 3);
//		cvCircle(src, new CvPoint2D32f(pts), arg2, arg3, arg4, arg5, arg6)
//		Random rand = new Random();
		for(int i=0; i < circlez.size() && nCirc < maxCircles; i++, nCirc++){
//				double r = rand.nextDouble()*100 + 15;
//				double g = rand.nextDouble()*100 + 15;
//				double b = rand.nextDouble()*100 + 15;
//				cvCircle(src, circlez.get(i).p, circlez.get(i).radius, CV_RGB(r, g, b), 6, CV_AA, 0);
//			cvCircle(src, circlez.get(i).p, circlez.get(i).radius, CvScalar.RED, 6, CV_AA, 0);
//				cvCircle(src, circlez.get(i).p, 5, CvScalar.RED, 3, CV_AA, 0);
			Circle ci = circlez.get(i);
			int r = 1;
			CvPoint p1 = cvPoint(ci.p.x() - r, ci.p.y() - r);
			CvPoint p2 = cvPoint(ci.p.x() + r*2, ci.p.y() + r*2);
			cvRectangle(src, p1, p2, CvScalar.RED, 10, CV_AA, 0);
		}
		
		
		List<Point> dots = new ArrayList<Point>();
		for(int i=0; i < circlez.size(); i++){
			Circle ci = circlez.get(i);
			dots.add(new Point(ci.p.x(), ci.p.y()));
		}
		
//		for(int i=0; i < circlez.size(); i++){
//			System.out.println("CCCCCCCCCIIIIIIIRRRRRRCCCCCCCLLLLLLLLEEEEEZZZZZZZZZ " + circlez.get(i).p.x() + " : " + circlez.get(i).p.y());
//		}
//		System.out.println("NUMBER OF CIRCLES: " + circlez.size());
		return new Pair<IplImage, List<Point>>(src, dots);
	}

	private IplImage bmpToIpl(Bitmap bitmap){
		IplImage image = IplImage.create(bitmap.getWidth(), bitmap.getHeight(), opencv_core.IPL_DEPTH_8U, 4);
		bitmap.copyPixelsToBuffer(image.getByteBuffer());
		return image;
	}

	private Bitmap getImage(byte[] data){
		InputStream is = new ByteArrayInputStream(data);
		Bitmap bmp = BitmapFactory.decodeStream(is);
		// Getting width & height of the given image.
		if(bmp != null){
			int w = bmp.getWidth();
			int h = bmp.getHeight();
			// Setting post rotate to 90
			Matrix mtx = new Matrix();
			mtx.postRotate(90);
			// Rotating Bitmap
			Bitmap rotatedBMP = Bitmap.createBitmap(bmp, 0, 0, w, h, mtx, true);

			return rotatedBMP;
		}
		
		return null;
	}

	public File getPictureDir(){
		File sdDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		return new File(sdDir, "CameraAPIDemo");
	}
}