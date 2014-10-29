package com.evilgenius.testproject;

import android.app.ActionBar;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBarActivity;

public class BaseActivity extends ActionBarActivity {
	ActionBar actionbar;
	public void InitActionbar()
	{
		actionbar = getActionBar();
		actionbar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.main_normal)));
//		 Enabling Back navigation on Action Bar icon
		actionbar.setNavigationMode(ActionBar.DISPLAY_HOME_AS_UP);
		actionbar.setDisplayHomeAsUpEnabled(true);
	}
	public void InitGui()
	{
		
	}
}
