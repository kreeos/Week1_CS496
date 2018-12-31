package com.example.week1;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TestPagerAdapter extends FragmentPagerAdapter {
    private static int PAGE_NUMBER = 3;

    public TestPagerAdapter(FragmentManager fm){
        super(fm);
    }

    //page에 보여줄 items 가져오기
    @Override
    public Fragment getItem(int position){
        switch (position){
            case 0:
                return contacts.newInstance();
            case 1:
                return gallery.newInstance();
            case 2:
                return translator.newInstance();
            default:
                return null;
        }
    }
    //page number define
    @Override
    public int getCount() {
        return PAGE_NUMBER;
    }


    @Override
    public CharSequence getPageTitle(int position){
        switch (position){
            case 0:
                return "[ Contacts ]";
            case 1:
                return "[ Gallery ]";
            case 2:
                return "[ Translator ]";
            default:
                return null;
        }
    }
}