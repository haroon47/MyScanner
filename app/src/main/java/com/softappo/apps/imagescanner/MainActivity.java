package com.softappo.apps.imagescanner;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.karan.churi.PermissionManager.PermissionManager;
import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    PermissionManager manager;
    private AdView mAdView;

    InterstitialAd interstitialAd;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        manager = new PermissionManager() {};
        manager.checkAndRequestPermissions(this);

        mAdView = (AdView)findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        interstitialAd.loadAd(new AdRequest.Builder().build());


    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        manager.checkResult(requestCode,permissions,grantResults);

       // ArrayList<String> granted = manager.getStatus().get(0).granted;
        ArrayList<String> denied = manager.getStatus().get(0).denied;

        if(denied.isEmpty())
        {
            Toast.makeText(getApplicationContext(),"Permissions enabled.",Toast.LENGTH_SHORT).show();
        }

    }



    public void openCamera(View v)
    {
        if(interstitialAd.isLoaded())
        {
            interstitialAd.show();
            interstitialAd.setAdListener(new AdListener()
            {
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    int REQUEST_CODE = 99;
                    int preference = ScanConstants.OPEN_CAMERA;
                    Intent intent = new Intent(getApplicationContext(), ScanActivity.class);
                    intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference);
                    startActivityForResult(intent, REQUEST_CODE);
                    interstitialAd.loadAd(new AdRequest.Builder().build());
                }
            });

        }
        else
        {
            int REQUEST_CODE = 99;
            int preference = ScanConstants.OPEN_CAMERA;
            Intent intent = new Intent(this, ScanActivity.class);
            intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference);
            startActivityForResult(intent, REQUEST_CODE);
            interstitialAd.loadAd(new AdRequest.Builder().build());
        }

    }

    public void openGallery(View v)
    {
        if(!interstitialAd.isLoaded())
        {
            interstitialAd.loadAd(new AdRequest.Builder().build());
        }

        if(interstitialAd.isLoaded())
        {
            interstitialAd.show();
            interstitialAd.setAdListener(new AdListener()
            {
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    int REQUEST_CODE = 99;
                    int preference = ScanConstants.OPEN_MEDIA;
                    Intent intent = new Intent(getApplicationContext(), ScanActivity.class);
                    intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference);
                    startActivityForResult(intent, REQUEST_CODE);
                    interstitialAd.loadAd(new AdRequest.Builder().build());
                }
            });
        }

        else
        {
            int REQUEST_CODE = 99;
            int preference = ScanConstants.OPEN_MEDIA;
            Intent intent = new Intent(this, ScanActivity.class);
            intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference);
            startActivityForResult(intent, REQUEST_CODE);
            interstitialAd.loadAd(new AdRequest.Builder().build());
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 99 && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getExtras().getParcelable(ScanConstants.SCANNED_RESULT);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                getContentResolver().delete(uri, null, null);
              //  scannedImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



    }
}
