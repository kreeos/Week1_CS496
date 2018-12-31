package com.example.week1;
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity{


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if(MainActivity.checkPermissions(this, Manifest.permission.WRITE_CONTACTS)||MainActivity.checkPermissions(this, Manifest.permission.READ_CONTACTS)){

        }
        else{
            MainActivity.requestExternalPermissions(this);
        }


        //viewpager and tabs loading on activity_main
        setContentView(R.layout.activity_main);

        TestPagerAdapter mTestPagerAdapter = new TestPagerAdapter(
                getSupportFragmentManager()
        );
        final ViewPager mViewPager = (ViewPager) findViewById(R.id.view_pager);

        mViewPager.setAdapter(mTestPagerAdapter);

        TabLayout mTab = (TabLayout) findViewById(R.id.tabs);
        mTab.setupWithViewPager(mViewPager);



    }



    static final int REQUEST_PERMISSIONS = 1;
    private static String[] PERMISSIONS = {
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.RECORD_AUDIO
    };

    public static boolean checkPermissions(Activity activity, String permission) {
        int permissionResult = ActivityCompat.checkSelfPermission(activity, permission);
        if (permissionResult == PackageManager.PERMISSION_GRANTED) return true;
        else return false;
    }


    public static void requestExternalPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity, PERMISSIONS, REQUEST_PERMISSIONS);
    }



    public static boolean verifyPermission(int[] grantresults){
        if(grantresults.length<1){
            return false;
        }
        for(int result: grantresults){
            if(result != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){

        if(requestCode == MainActivity.REQUEST_PERMISSIONS){
            if(MainActivity.verifyPermission(grantResults)){

                TestPagerAdapter mTestPagerAdapter = new TestPagerAdapter(
                        getSupportFragmentManager()
                );
                ViewPager mViewPager = (ViewPager) findViewById(R.id.view_pager);

                mViewPager.setAdapter(mTestPagerAdapter);

                TabLayout mTab = (TabLayout) findViewById(R.id.tabs);
                mTab.setupWithViewPager(mViewPager);
            }
        }
        else{
            super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        }
    }


}









