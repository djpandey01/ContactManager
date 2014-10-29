package com.evilgenius.Helper;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author Gabriele Porcelli
 * 
 *         Example. 
 *         FirstTimePreference prefFirstTime = new FirstTimePreference(getApplicationContext()); 
 *         if (prefFirstTime.runTheFirstNTimes("myKey" , 3)) {
 *         Toast.makeText(this,"Test myKey & coutdown: "+ prefFirstTime.getCountDown("myKey"),Toast.LENGTH_LONG).show(); }
 */

public class SharedPreference {

	private static final int INT_ERROR = -1;
	public static final String PREFERENCES_KEY = "FirstKeyPreferences";
	
	
	public static final int SIZE = 3;
	public static final String SIZE_KEY = "SIZEKEY";
	private final SharedPreferences firstTimePreferences;

	public SharedPreference(Context context) {
		firstTimePreferences = context.getSharedPreferences(
				PREFERENCES_KEY, Context.MODE_PRIVATE);
	}
	public String[] getLastestDate()
	{
		String[] recents = new String[SIZE];
		int size = firstTimePreferences.getInt(SIZE_KEY, 0);
		for(Integer i=0;i<size;i++)  
	    {
			recents[i] = firstTimePreferences.getString("Recent_"+i.toString(),"");
	    }
		
		return recents;
	}
	public void setLastestDate(String[] date)
	{
		SharedPreferences.Editor editor = firstTimePreferences.edit();
		editor.putInt(SIZE_KEY, date.length);
		for(Integer i=0;i<date.length;i++)  
	    {
			editor.remove("Recent_" + i.toString());
			editor.putString("Recent_" + i.toString(), date[i]);  
	    }
		editor.commit();
	}
	public void AddMore(String uri)
	{
		int size = firstTimePreferences.getInt(SIZE_KEY, 0);
		int newsize = Math.min(SIZE,size+1);
		String[] recents = new String[newsize];
		
		if(newsize == SIZE)
			for(Integer i=0;i<newsize-1;i++)  
			{
				recents[i] = firstTimePreferences.getString("Recent_"+((Integer)(i+1)).toString(),"");
			}
		recents[newsize-1] = uri;
		setLastestDate(recents);
	}
	
}
