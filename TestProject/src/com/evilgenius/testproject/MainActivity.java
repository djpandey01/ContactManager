package com.evilgenius.testproject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import com.evilgenius.Helper.CircularImageView;
import com.evilgenius.Helper.Contact;
import com.evilgenius.Helper.ContactsQuery;
import com.evilgenius.Helper.ImageLoader;
import com.evilgenius.Helper.SharedPreference;
import com.evilgenius.Helper.Utils;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Contacts.Photo;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends BaseActivity implements OnClickListener,LoaderManager.LoaderCallbacks<Cursor>  {

	private static final int REQUEST_ADD = 101;
	public static Contact[] contacts;
	public static SharedPreference sharedPref ;
	private ImageLoader mImageLoader;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		sharedPref = new SharedPreference(this);
		InitGui();
		InitImageLoader() ;
		getSupportLoaderManager().initLoader(ContactsQuery.QUERY_ID, null, this);

		String[] recents = sharedPref.getLastestDate();
		(new ContactLoader()).execute(recents);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		//		if (id == R.id.action_search) {
		//			return true;
		//		}
		return super.onOptionsItemSelected(item);
	}
	public void InitGui()
	{
		(findViewById(R.id.btn_membook)).setOnClickListener(this);
		(findViewById(R.id.profile_image)).setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.btn_membook:
			GoToContactList();
			break;
		case R.id.profile_image:
			GoToContactAdd();
			break;

		default:
			break;
		}
	}

	private void GoToContactAdd() {
		Intent intent = new Intent(this, ContactAddActivity.class);
		startActivityForResult(intent,REQUEST_ADD);
	}

	private void GoToContactList() {
		Intent intent = new Intent(this,ContactsListActivity.class);
		startActivity(intent);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {

		// If this is the loader for finding contacts in the Contacts Provider
		// (the only one supported)
		if (id == ContactsQuery.QUERY_ID) {
			Uri contentUri;

			// There are two types of searches, one which displays all contacts and
			// one which filters contacts by a search query. If mSearchTerm is set
			// then a search query has been entered and the latter should be used.

			contentUri = ContactsQuery.CONTENT_URI;

			// Returns a new CursorLoader for querying the Contacts table. No arguments are used
			// for the selection clause. The search string is either encoded onto the content URI,
			// or no contacts search string is used. The other search criteria are constants. See
			// the ContactsQuery interface.
			return new CursorLoader(this,
					contentUri,
					ContactsQuery.PROJECTION,
					ContactsQuery.SELECTION,
					null,
					ContactsQuery.SORT_ORDER);
		}

		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// This swaps the new cursor into the adapter.
		Cursor c = data; 
		contacts = new Contact[data.getCount()];
		int i =0;
		if (c != null) {
			while(c.moveToNext()) {
				contacts[i] = new Contact();
				contacts[i].setID(data.getInt(ContactsQuery.ID));
				contacts[i].set_firstName(data.getString(ContactsQuery.DISPLAY_NAME));
				contacts[i].set_imgUri(data.getString(ContactsQuery.PHOTO_THUMBNAIL_DATA));
				i++;
			}
			c.close();
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		if (loader.getId() == ContactsQuery.QUERY_ID) {
			// When the loader is being reset, clear the cursor from the adapter. This allows the
			// cursor resources to be freed.
			//            mAdapter.swapCursor(null);
		}
	}
	private void InitImageLoader() {
		/*
		 * The ImageLoader takes care of loading and resizing images asynchronously into the
		 * ImageView. More thorough sample code demonstrating background image loading as well as
		 * details on how it works can be found in the following Android Training class:
		 * http://developer.android.com/training/displaying-bitmaps/
		 */
		mImageLoader = new ImageLoader(this, getLargestScreenDimension()) {
			@Override
			protected Bitmap processBitmap(Object data) {
				// This gets called in a background thread and passed the data from
				// ImageLoader.loadImage().
				return loadContactPhoto((Uri) data, getImageSize());

			}
		};

		// Set a placeholder loading image for the image loader
		mImageLoader.setLoadingImage(R.drawable.ic_contact_picture_180_holo_light);

		// Tell the image loader to set the image directly when it's finished loading
		// rather than fading in
		mImageLoader.setImageFadeIn(false);
	}
	public class ContactLoader extends AsyncTask<String, Void, Contact[]>{
		@Override
		protected  Contact[] doInBackground(String... params) {
			if(params == null) return null;

			Contact[] lstContacts = new Contact[params.length];
			for(int i = 0 ; i < params.length;i++)
				try{
					
					lstContacts[i] = ContactsQuery.retrieveContactNumber(MainActivity.this, params[i]);

				}catch(Exception e){
					Log.d("", "");
				}

			return lstContacts;

		}      

		@Override
		protected void onPostExecute( Contact[] contacts) {
			if(contacts == null || contacts.length==0) return;

			LinearLayout view = (LinearLayout) findViewById(R.id.ll_recents);
			
			OnClickListener clicklistener = new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					try{
						Contact contact = (Contact) v.getTag();
						Intent intent = new Intent(MainActivity.this, ContactDetailActivity.class);
			            intent.setData(Uri.parse(contact.get_imgUri()));
			            startActivity(intent);
					}catch(Exception e){}
				}
			};
			for(int i =0 ; i < contacts.length;i++)
			{
				if(contacts[i] == null) continue;
				try{
					LinearLayout ll = new LinearLayout(MainActivity.this);
					ll.setOrientation(LinearLayout.VERTICAL);
					ll.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
					ll.setGravity(Gravity.CENTER_HORIZONTAL);
					CircularImageView iv = new CircularImageView(MainActivity.this);
					iv.setLayoutParams(new LayoutParams(getResources().getDimensionPixelSize(R.dimen.recent_size), getResources().getDimensionPixelSize(R.dimen.recent_size)));
//					iv.setImageResource(R.drawable.ic_contact_picture_180_holo_light);
					TextView tv = new TextView(MainActivity.this);
					tv.setGravity(Gravity.CENTER_HORIZONTAL);
					tv.setLayoutParams(new LayoutParams(getResources().getDimensionPixelSize(R.dimen.recent_size),LayoutParams.WRAP_CONTENT));
					mImageLoader.loadImage(Uri.parse(contacts[i].get_imgUri()),iv);
					String name = "";
					if(contacts[i].get_firstName()  != null)
						name += contacts[i].get_firstName() ;
					if(contacts[i].get_lastName() != null )
						name += contacts[i].get_lastName() ;
					tv.setText(name);
					
					ll.setTag(contacts[i]);
					ll.setOnClickListener(clicklistener);
					
					ll.addView(iv);
					ll.addView(tv);
					view.addView(ll);
					
					
				}catch(Exception e){
					Log.d("", "");
				}
			}
			view.invalidate();
		}

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}
	private int getLargestScreenDimension() {
		// Gets a DisplayMetrics object, which is used to retrieve the display's pixel height and
		// width
		final DisplayMetrics displayMetrics = new DisplayMetrics();

		// Retrieves a displayMetrics object for the device's default display
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		final int height = displayMetrics.heightPixels;
		final int width = displayMetrics.widthPixels;

		// Returns the larger of the two values
		return height > width ? height : width;
	}
	/**
	 * Decodes and returns the contact's thumbnail image.
	 * @param contactUri The Uri of the contact containing the image.
	 * @param imageSize The desired target width and height of the output image in pixels.
	 * @return If a thumbnail image exists for the contact, a Bitmap image, otherwise null.
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private Bitmap loadContactPhoto(Uri contactUri, int imageSize) {

		// Ensures the Fragment is still added to an activity. As this method is called in a
		// background thread, there's the possibility the Fragment is no longer attached and
		// added to an activity. If so, no need to spend resources loading the contact photo.
		//	    	if (!isAdded() || this == null) {
		//	    		return null;
		//	    	}

		// Instantiates a ContentResolver for retrieving the Uri of the image
		final ContentResolver contentResolver = getContentResolver();

		// Instantiates an AssetFileDescriptor. Given a content Uri pointing to an image file, the
		// ContentResolver can return an AssetFileDescriptor for the file.
		AssetFileDescriptor afd = null;

		if (Utils.hasICS()) {
			// On platforms running Android 4.0 (API version 14) and later, a high resolution image
			// is available from Photo.DISPLAY_PHOTO.
			try {
				// Constructs the content Uri for the image
				Uri displayImageUri = Uri.withAppendedPath(contactUri, Photo.DISPLAY_PHOTO);

				// Retrieves an AssetFileDescriptor from the Contacts Provider, using the
				// constructed Uri
				afd = contentResolver.openAssetFileDescriptor(displayImageUri, "r");
				// If the file exists
				if (afd != null) {
					// Reads and decodes the file to a Bitmap and scales it to the desired size
					return ImageLoader.decodeSampledBitmapFromDescriptor(
							afd.getFileDescriptor(), imageSize, imageSize);
				}
			} catch (FileNotFoundException e) {
				// Catches file not found exceptions
				if (BuildConfig.DEBUG) {
					// Log debug message, this is not an error message as this exception is thrown
					// when a contact is legitimately missing a contact photo (which will be quite
					// frequently in a long contacts list).
				}
			} finally {
				// Once the decode is complete, this closes the file. You must do this each time
				// you access an AssetFileDescriptor; otherwise, every image load you do will open
				// a new descriptor.
				if (afd != null) {
					try {
						afd.close();
					} catch (IOException e) {
						// Closing a file descriptor might cause an IOException if the file is
						// already closed. Nothing extra is needed to handle this.
					}
				}
			}
		}

		// If the platform version is less than Android 4.0 (API Level 14), use the only available
		// image URI, which points to a normal-sized image.
		try {
			// Constructs the image Uri from the contact Uri and the directory twig from the
			// Contacts.Photo table
			Uri imageUri = Uri.withAppendedPath(contactUri, Photo.CONTENT_DIRECTORY);

			// Retrieves an AssetFileDescriptor from the Contacts Provider, using the constructed
			// Uri
			afd = getContentResolver().openAssetFileDescriptor(imageUri, "r");

			// If the file exists
			if (afd != null) {
				// Reads the image from the file, decodes it, and scales it to the available screen
				// area
				return ImageLoader.decodeSampledBitmapFromDescriptor(
						afd.getFileDescriptor(), imageSize, imageSize);
			}
		} catch (FileNotFoundException e) {
			// Catches file not found exceptions
			if (BuildConfig.DEBUG) {
				// Log debug message, this is not an error message as this exception is thrown
				// when a contact is legitimately missing a contact photo (which will be quite
				// frequently in a long contacts list).
			}
		} finally {
			// Once the decode is complete, this closes the file. You must do this each time you
			// access an AssetFileDescriptor; otherwise, every image load you do will open a new
			// descriptor.
			if (afd != null) {
				try {
					afd.close();
				} catch (IOException e) {
					// Closing a file descriptor might cause an IOException if the file is
					// already closed. Ignore this.
				}
			}
		}

		// If none of the case selectors match, returns null.
		return null;
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQUEST_ADD)
		{
			String[] recents = sharedPref.getLastestDate();
			(new ContactLoader()).execute(recents);
		}
	}
}
