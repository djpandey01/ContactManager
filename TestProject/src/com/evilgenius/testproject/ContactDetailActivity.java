package com.evilgenius.testproject;


import java.io.FileNotFoundException;
import java.io.IOException;

import com.evilgenius.Helper.Contact;
import com.evilgenius.Helper.ContactsQuery;
import com.evilgenius.Helper.ImageLoader;
import com.evilgenius.Helper.Utils;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.Contacts.Photo;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ContactDetailActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

	private Uri mContactUri;
	private ImageLoader mImageLoader;
	private ImageView mImageView,mBackground;
	TextView tvName,tvPhoneNo,tvNote;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_detail);
		InitActionbar();
		tvName = (TextView) findViewById(R.id.tv_user_name);
		tvPhoneNo = (TextView) findViewById(R.id.tv_user_number);
		tvNote = (TextView) findViewById(R.id.tv_user_note);

		InitImageLoader();

		// Fetch the data Uri from the intent provided to this activity
		final Uri uri = getIntent().getData();
		if (Utils.hasHoneycomb()) {
			mContactUri = uri;
		} else {
			// For versions earlier than Android 3.0, stores a contact Uri that's constructed from
			// contactLookupUri. Later on, the resulting Uri is combined with
			// Contacts.Data.CONTENT_DIRECTORY to map to the provided contact. It's done
			// differently for these earlier versions because Contacts.Data.CONTENT_DIRECTORY works
			// differently for Android versions before 3.0.
			mContactUri = Contacts.lookupContact(getContentResolver(),
					uri);
		}		
		mImageView = (ImageView)findViewById(R.id.profile_image);
		mBackground = (ImageView) findViewById(R.id.iv_profile_background);
		mImageLoader.loadImage(mContactUri, mImageView);
		mImageLoader.loadImage(mContactUri, mBackground);

		getSupportLoaderManager().restartLoader(ContactDetailQuery.QUERY_ID, null, this);
		getSupportLoaderManager().restartLoader(ContactAddressQuery.QUERY_ID, null, this);
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.contact_detail, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		switch (id) {
		// Two main queries to load the required information
		case ContactDetailQuery.QUERY_ID:
			// This query loads main contact details, see
			// ContactDetailQuery for more information.
			//            	return ContactsQuery.retrieveContactNumber(this,mContactUri);
			return new CursorLoader(this, mContactUri,
					null,
					null, null, null);
		case ContactAddressQuery.QUERY_ID:
			// This query loads contact address details, see
			// ContactAddressQuery for more information.
			final Uri uri = Uri.withAppendedPath(mContactUri, Contacts.Data.CONTENT_DIRECTORY);
			return new CursorLoader(this, uri,
					ContactAddressQuery.PROJECTION,
					ContactAddressQuery.SELECTION,
					null, null);
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

		// If this fragment was cleared while the query was running
		// eg. from from a call like setContact(uri) then don't do
		// anything.
		if (mContactUri == null) {
			return;
		}
		Contact contact = ContactsQuery.retrieveContactNumber(this, data);
		if(contact!= null)
		{
			String name ="";
			if(contact.get_firstName() != null)
				name += contact.get_firstName();
			if(contact.get_lastName() != null)
				name += contact.get_lastName();
			tvName.setText(name);
			tvPhoneNo.setText(contact.get_phoneNo());
			tvNote.setText(contact.get_note());
			setTitle(name);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// Nothing to do here. The Cursor does not need to be released as it was never directly
		// bound to anything (like an adapter).
	}
	/**
	 * This interface defines constants used by contact retrieval queries.
	 */
	public interface ContactDetailQuery {
		// A unique query ID to distinguish queries being run by the
		// LoaderManager.
		final static int QUERY_ID = 1;

		// The query projection (columns to fetch from the provider)
		@SuppressLint("InlinedApi")
		final static String[] PROJECTION = {
			Contacts._ID,
			Utils.hasHoneycomb() ? Contacts.DISPLAY_NAME_PRIMARY : Contacts.DISPLAY_NAME,
		};

		// The query column numbers which map to each value in the projection
		final static int ID = 0;
		final static int DISPLAY_NAME = 1;
	}

	/**
	 * This interface defines constants used by address retrieval queries.
	 */
	public interface ContactAddressQuery {
		// A unique query ID to distinguish queries being run by the
		// LoaderManager.
		final static int QUERY_ID = 2;

		// The query projection (columns to fetch from the provider)
		final static String[] PROJECTION = {
			StructuredPostal._ID,
			StructuredPostal.FORMATTED_ADDRESS,
			StructuredPostal.TYPE,
			StructuredPostal.LABEL,
		};

		// The query selection criteria. In this case matching against the
		// StructuredPostal content mime type.
		final static String SELECTION =
				Data.MIMETYPE + "='" + StructuredPostal.CONTENT_ITEM_TYPE + "'";

		// The query column numbers which map to each value in the projection
		final static int ID = 0;
		final static int ADDRESS = 1;
		final static int TYPE = 2;
		final static int LABEL = 3;
	}
}
