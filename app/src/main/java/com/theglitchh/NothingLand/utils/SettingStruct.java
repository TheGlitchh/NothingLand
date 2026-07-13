package com.theglitchh.NothingLand.utils;

import android.content.Context;
import java.util.ArrayList;

public class SettingStruct {
    public static final int TYPE_TOGGLE = 0;
    public static final int TYPE_CUSTOM = 1;

    public String title;
    public String category;
    public int type;

    public SettingStruct(String title, String category) {
        this(title, category, TYPE_TOGGLE);
    }

    public SettingStruct(String title, String category, int type) {
        this.title = title;
        this.category = category;
        this.type = type;
    }

    public boolean onAttach(Context ctx) {
        return false;
    }

    public void onCheckChanged(boolean checked, Context ctx) {}

    public void onClick(Context c) {}
}
