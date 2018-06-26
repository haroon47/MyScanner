package com.scanlibrary;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.theartofdev.edmodo.cropper.CropImage;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

import static android.app.Activity.RESULT_OK;


public class ResultFragment extends Fragment {

    private View view;
    private ImageView scannedImageView;
    private Button doneButton;
    private Bitmap original;
    private Button originalButton;
    private Button MagicColorButton;
    private Button grayModeButton;
    private Button bwButton;
    private ImageButton rotateButton;
    private Bitmap transformed;
    InterstitialAd interstitialAd;

    Handler handler;


    Bitmap mybitmap;

    public ResultFragment() {
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.result_layout, null);
        init();
        return view;
    }

    private void init() {
        interstitialAd = new InterstitialAd(getActivity());
        interstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        interstitialAd.loadAd(new AdRequest.Builder().build());

        handler = new Handler();

        scannedImageView = (ImageView) view.findViewById(R.id.scannedImage);
        originalButton = (Button) view.findViewById(R.id.original);
        originalButton.setOnClickListener(new OriginalButtonClickListener());
        MagicColorButton = (Button) view.findViewById(R.id.magicColor);
        MagicColorButton.setOnClickListener(new MagicColorButtonClickListener());
        grayModeButton = (Button) view.findViewById(R.id.grayMode);
        grayModeButton.setOnClickListener(new GrayButtonClickListener());
        bwButton = (Button) view.findViewById(R.id.BWMode);
        rotateButton = (ImageButton)view.findViewById(R.id.button2);
        bwButton.setOnClickListener(new BWButtonClickListener());
         topOriginal = getBitmap();
        setScannedImage(topOriginal);
        doneButton = (Button) view.findViewById(R.id.doneButton);
        doneButton.setOnClickListener(new DoneButtonClickListener());
        rotateButton.setOnClickListener(new RotateButtonClickListener());
    }
    Uri myuri;
    Bitmap topOriginal;
    private Bitmap getBitmap() {
         myuri = getUri();
        try {
            original = Utils.getBitmap(getActivity(), myuri);
            getActivity().getContentResolver().delete(myuri, null, null);
            return original;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Uri getUri() {
        Uri uri = getArguments().getParcelable(ScanConstants.SCANNED_RESULT);
        return uri;
    }

    public void setScannedImage(Bitmap scannedImage) {

        scannedImageView.setImageBitmap(scannedImage);
    }

    private class RotateButtonClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
         //   mybitmap = transformed;
         //   if (mybitmap == null) {
         //       mybitmap = original;
         //   }


            Matrix matrix = new Matrix();

            matrix.postRotate(90);

            Bitmap rotatedBitmap = Bitmap.createBitmap(topOriginal , 0, 0, topOriginal.getWidth(), topOriginal.getHeight(), matrix, true);

            scannedImageView.setImageBitmap(rotatedBitmap);
            transformed = rotatedBitmap;
        }
    }

    private class DoneButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
          //  showProgressDialog(getResources().getString(R.string.loading));


                                try {
                                    Intent data = new Intent();
                                      mybitmap = transformed;
                                    if (mybitmap == null) {
                                        mybitmap = original;
                                    }

                                    // save the bitmap to the external directory


                                    // Alert Dialog , if image  then saveimage , if pdf then savePDf

                                    Uri uri = Utils.getUri(getActivity(), mybitmap);
                                    data.putExtra(ScanConstants.SCANNED_RESULT, uri);
                                    getActivity().setResult(RESULT_OK, data);



                                  //  Toast.makeText(getActivity(),"Above alertDialog",Toast.LENGTH_SHORT).show();
                                    AlertDialog.Builder optionBuilder = new AlertDialog.Builder(getActivity());
                                    optionBuilder.setMessage("Select the output format.")
                                            .setCancelable(false)
                                            .setPositiveButton("Image", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    saveImage(mybitmap);


                                                    original.recycle();

                                                }
                                            })
                                            .setNegativeButton("PDF", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    // pdf

                                                    savePDF(mybitmap);

                                                    original.recycle();


                                                }
                                            });

                                    AlertDialog alert = optionBuilder.create();
                                    alert.setTitle("SAVE");
                                    alert.show();



                                } catch (Exception e) {
                                    e.printStackTrace();
                                }



            }



        }

        private void savePDF(Bitmap finalBitmap)
        {
            PdfDocument pdfDocument = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(finalBitmap.getWidth(),finalBitmap.getHeight(),1).create();

            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();
            paint.setColor(Color.parseColor("#FFFFFF"));
            canvas.drawPaint(paint);


            finalBitmap = Bitmap.createScaledBitmap(finalBitmap,finalBitmap.getWidth(),finalBitmap.getHeight(),true);
            paint.setColor(Color.BLUE);
            canvas.drawBitmap(finalBitmap,0,0,null);
            pdfDocument.finishPage(page);


            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateTime = sdf.format(Calendar.getInstance().getTime());

            File root = new File(Environment.getExternalStorageDirectory(),"Image PDF Scanner");
            if(!root.exists())
            {
                root.mkdir();
            }

            File file = new File(root,dateTime+ "_image.pdf");
            try
            {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                pdfDocument.writeTo(fileOutputStream);


            }catch(IOException e)
            {
                e.printStackTrace();
            }
            finally {
                pdfDocument.close();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // dismissDialog();
                        Toast.makeText(getActivity(),"PDF saved successfully.",Toast.LENGTH_SHORT).show();
                        getActivity().finish();
                    }
                });

            }

        }



    private void saveImage(Bitmap finalBitmap)
    {
        final String path = Utils.insertImage(getActivity().getContentResolver(), finalBitmap, System.currentTimeMillis() + "_profile.jpg", null);
        if (!TextUtils.isEmpty(path)) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                   // dismissDialog();
                    Toast.makeText(getActivity(),"Image saved to gallery",Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
            });
        } else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                 //   dismissDialog();
                    Toast.makeText(getActivity(),"Unable to save image",Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
            });

        }
    }


    private class BWButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(final View v)
        {

            if(transformed == null)
            {
              //  Uri myURI = getUri();
                String path = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), original,null, null);
                Uri image1= Uri.parse(path);


                CropImage.activity(image1)
                        .start(getActivity().getApplicationContext(),ResultFragment.this);

                original = mybitmap;
            }
            else
            {
                String path = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), transformed,null, null);
                Uri image1= Uri.parse(path);

                CropImage.activity(image1)
                        .start(getActivity().getApplicationContext(),ResultFragment.this);
                transformed = mybitmap;


            }

        }
    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                try
                {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), resultUri);
                    topOriginal = bitmap;
                }catch(IOException e)
                {
                    e.printStackTrace();
                }

                scannedImageView.setImageURI(resultUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(getActivity(),error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class MagicColorButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
         //   showProgressDialog(getResources().getString(R.string.applying_filter));
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        transformed = ((ScanActivity) getActivity()).getMagicColorBitmap(original);
                    } catch (final OutOfMemoryError e) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                transformed = original;
                                scannedImageView.setImageBitmap(original);
                                e.printStackTrace();
                             //   dismissDialog();
                                onClick(v);
                            }
                        });
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            scannedImageView.setImageBitmap(transformed);
                         //   dismissDialog();
                        }
                    });
                }
            });
        }
    }

    private class OriginalButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            try {

                if(interstitialAd.isLoaded())
                {
                    interstitialAd.show();
                    interstitialAd.setAdListener(new AdListener()
                    {
                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                        //    showProgressDialog(getResources().getString(R.string.applying_filter));

                            scannedImageView.setImageBitmap(original);
                            transformed = original;
                        //    dismissDialog();
                            interstitialAd.loadAd(new AdRequest.Builder().build());
                        }
                    });
                }
                else
                {
                //    showProgressDialog(getResources().getString(R.string.applying_filter));

                    scannedImageView.setImageBitmap(original);
                //    dismissDialog();
                    interstitialAd.loadAd(new AdRequest.Builder().build());
                    transformed = original;
                }

            } catch (OutOfMemoryError e) {
                e.printStackTrace();
             //   dismissDialog();
            }
        }
    }

    private class GrayButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v)
        {

         //   showProgressDialog(getResources().getString(R.string.applying_filter));
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        transformed = ((ScanActivity) getActivity()).getGrayBitmap(original);
                    } catch (final OutOfMemoryError e) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                transformed = original;
                                scannedImageView.setImageBitmap(original);
                                e.printStackTrace();
                           //     dismissDialog();
                                onClick(v);
                            }
                        });
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            scannedImageView.setImageBitmap(transformed);
                      //      dismissDialog();
                        }
                    });
                }
            });
        }
    }


}