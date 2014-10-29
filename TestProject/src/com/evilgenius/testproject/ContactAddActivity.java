package com.evilgenius.testproject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.evilgenius.Helper.Contact;
import com.evilgenius.Helper.ContactsQuery;
import com.evilgenius.Helper.Utils;

import android.support.v7.app.ActionBarActivity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class ContactAddActivity extends BaseActivity {

	private static final int CAMERA_REQUEST = 1;
	private static final int PICK_FROM_GALLERY = 2;
	public static final int MEDIA_TYPE_IMAGE = 1;
	final static int PIC_CROP = 3;
	
	Contact contact = new Contact();
	private Uri picUri;
	private File mediaFile;
	private File sendFile;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_add);
		InitActionbar();
		(findViewById(R.id.contact_takepic)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/**
				 * open dialog for choose camera/gallery
				 */

				final String[] option = new String[] { "Take from Camera",
						"Select from Gallery" };
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(ContactAddActivity.this,
						android.R.layout.select_dialog_item, option);
				AlertDialog.Builder builder = new AlertDialog.Builder(ContactAddActivity.this);

				builder.setTitle("Select Option");
				builder.setAdapter(adapter, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Log.e("Selected Item", String.valueOf(which));
						if (which == 0) {
							onImgProfile() ;
//							callCamera();
						}
						if (which == 1) {
							callGallery();
						}

					}
				});
				builder.show();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.contact_add, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_check) {
			SaveContact();
			return true;
		}
		if(id == android.R.id.home)
		{
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	void SaveContact()
	{
		EditText firstName = (EditText) findViewById(R.id.et_first_name);
		EditText lastName = (EditText) findViewById(R.id.et_last_name);
		EditText phoneNo = (EditText) findViewById(R.id.et_user_number);
		EditText note = (EditText) findViewById(R.id.et_user_note);
		contact.set_firstName( firstName.getText().toString());
		contact.set_lastName(lastName.getText().toString());
		contact.set_phoneNo(phoneNo.getText().toString());
		contact.set_note(note.getText().toString());
		
		if(contact == null ||
				contact.get_phoneNo() == null ||  contact.get_firstName() == null ||
				contact.get_phoneNo().equals("") ||  contact.get_firstName().equals(""))
		{
			Utils.ShowWarningDialog(this, "Some fields be missed!");
			return;
		}
		Boolean isSucceed = ContactsQuery.addContact(this, contact);
		if(isSucceed)
			
			
			finish();
		else
			Utils.ShowWarningDialog(this, "Add contact failed!");
	}
	/**
	 * On activity result
	 */
	void onImgProfile() {
	    Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

	    picUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

	    captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, picUri);

	    startActivityForResult(captureIntent, CAMERA_REQUEST);
	}


	private Uri getOutputMediaFileUri(int type) {
	    return Uri.fromFile(getOutputMediaFile(type));
	}

	private File getOutputMediaFile(int type) {

	    File mediaStorageDir = new File(
	            Environment.getExternalStorageDirectory(), "MyCameraApp");

	    if (!mediaStorageDir.exists()) {
	        if (!mediaStorageDir.mkdirs()) {
	            Log.d("MyCameraApp", "failed to create directory");
	            return null;
	        }
	    }

	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
	            .format(new Date());

	    if (type == MEDIA_TYPE_IMAGE) {
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator
	                + "IMG_" + timeStamp + ".jpg");
	    } else {
	        return null;
	    }

	    return mediaFile;
	}



	    @SuppressWarnings("unchecked")
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    // TODO Auto-generated method stub
	    super.onActivityResult(requestCode, resultCode, data);

	    if (requestCode == CAMERA_REQUEST) {
	        // if (Build.VERSION.SDK_INT < 19) {
	        try {
	            if (mediaFile.exists()) {
	                performCrop();
	                // new SavePhotoData().execute();
	            }

	        } catch (Exception e) {
	            // TODO: handle exception

	        }
	        // }

	    } else if (requestCode == PICK_FROM_GALLERY) {

	        try {
	            picUri = data.getData();
	            Log.i("uri", "" + picUri);
	            performCrop();
	        } catch (Exception e) {
	            // TODO: handle exception

	        }

	    } else if (requestCode == PIC_CROP) {
	        // get the returned data

	        try {
	            Bundle extras = data.getExtras();

	            // get the cropped bitmap
	            Bitmap thePic = extras.getParcelable("data");
	            // retrieve a reference to the ImageView

	            // display the returned cropped image
	            OnTakePhotoSuccess(thePic);
	            
	            File mediaStorageDir = new File(
	                    Environment.getExternalStorageDirectory(),
	                    "MyCameraApp");

	            if (!mediaStorageDir.exists()) {
	                if (!mediaStorageDir.mkdirs()) {
	                    Log.d("MyCameraApp", "failed to create directory");

	                }
	            }

	            // Create a media file name
	            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
	                    .format(new Date());

	            sendFile = new File(mediaStorageDir.getPath() + File.separator
	                    + "IMG_" + timeStamp + ".png");

	            FileOutputStream fOut = new FileOutputStream(sendFile);

	            thePic.compress(Bitmap.CompressFormat.PNG, 85, fOut);
	            fOut.flush();
	            fOut.close();

	        } catch (Exception e) {
	            e.printStackTrace();

	        }
	    }

	    if (resultCode == 3) {
	        Bundle b = data.getExtras();
	        b.getString("msg");
	    }
	};



	private void performCrop() {
	    // take care of exceptions
	    try {
	        // call the standard crop action intent (the user device may not
	        // support it)
	        try {
	            Intent cropIntent = new Intent("com.android.camera.action.CROP");
	            // indicate image type and Uri
	            cropIntent.setDataAndType(picUri, "image/*");
	            // set crop properties
	            cropIntent.putExtra("crop", "true");
	            // indicate aspect of desired crop
	            cropIntent.putExtra("aspectX", 1);
	            cropIntent.putExtra("aspectY", 1);
	            // indicate output X and Y
	            cropIntent.putExtra("outputX", 256);
	            cropIntent.putExtra("outputY", 256);
	            // retrieve data on return
	            cropIntent.putExtra("return-data", true);
	            // start the activity - we handle returning in onActivityResult
	            startActivityForResult(cropIntent, PIC_CROP);
	        } catch (Exception e) {
	            // TODO: handle exception
	        }
	    }
	    // respond to users whose devices do not support the crop action
	    catch (ActivityNotFoundException anfe) {
	        // display an error message
	        String errorMessage = "Whoops - your device doesn't support the crop action!";
	        Toast toast = Toast.makeText(getApplicationContext(), errorMessage,
	                Toast.LENGTH_SHORT);
	        toast.show();
	    }
	}
	public void OnTakePhotoSuccess(Bitmap yourImage)
	{
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		yourImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
		byte imageInByte[] = stream.toByteArray();
		contact.setImage(imageInByte);
		ByteArrayInputStream imageStream = new ByteArrayInputStream(imageInByte);
		((ImageView)findViewById(R.id.contact_takepic)).setImageBitmap(yourImage);	}
//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		if (resultCode != RESULT_OK)
//			return;
//
//		switch (requestCode) {
//		case CAMERA_REQUEST:
//
//			Bundle extras = data.getExtras();
//
//			if (extras != null) {
//				Bitmap yourImage = extras.getParcelable("data");
//				// convert bitmap to byte
//				ByteArrayOutputStream stream = new ByteArrayOutputStream();
//				yourImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
//				byte imageInByte[] = stream.toByteArray();
//				contact.setImage(imageInByte);
//				ByteArrayInputStream imageStream = new ByteArrayInputStream(imageInByte);
//		        Bitmap theImage = BitmapFactory.decodeStream(imageStream);
//		        ((ImageView)findViewById(R.id.contact_takepic)).setImageBitmap(theImage);
////				Log.e("output before conversion", imageInByte.toString());
////				// Inserting Contacts
////				Log.d("Insert: ", "Inserting ..");
////				db.addContact(new Contact("Android", imageInByte));
////				Intent i = new Intent(this,
////						Memo_book.class);
////				startActivity(i);
////				finish();
//
//			}
//			break;
//		case PICK_FROM_GALLERY:
//			Bundle extras2 = data.getExtras();
//
//			if (extras2 != null) {
//				Bitmap yourImage = extras2.getParcelable("data");
//				// convert bitmap to byte
//				ByteArrayOutputStream stream = new ByteArrayOutputStream();
//				yourImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
//				byte imageInByte[] = stream.toByteArray();
//				contact.setImage(imageInByte);
//				ByteArrayInputStream imageStream = new ByteArrayInputStream(imageInByte);
//		        Bitmap theImage = BitmapFactory.decodeStream(imageStream);
//		        ((ImageView)findViewById(R.id.contact_takepic)).setImageBitmap(theImage);
////				Log.e("output before conversion", imageInByte.toString());
////				// Inserting Contacts
////				Log.d("Insert: ", "Inserting ..");
////				db.addContact(new Contact("Android", imageInByte));
////				Intent i = new Intent(this,
////						Memo_book.class);
////				startActivity(i);
////				finish();
//			}
//
//			break;
//		}
//	}

	/**
	 * open camera method
	 */
	public void callCamera() {
		Intent cameraIntent = new Intent(
				android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		cameraIntent.putExtra("crop", "true");
		cameraIntent.putExtra("aspectX", 0);
		cameraIntent.putExtra("aspectY", 0);
		cameraIntent.putExtra("outputX", 200);
		cameraIntent.putExtra("outputY", 150);
		startActivityForResult(cameraIntent, CAMERA_REQUEST);

	}

	/**
	 * open gallery method
	 */

	public void callGallery() {
		Intent 		intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
//		intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 0);
		intent.putExtra("aspectY", 0);
		intent.putExtra("outputX", 200);
		intent.putExtra("outputY", 150);
		intent.putExtra("return-data", true);
		

		 intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
				 getOutputMediaFileUri(MEDIA_TYPE_IMAGE));

		startActivityForResult(
				Intent.createChooser(intent, "Complete action using"),
				PICK_FROM_GALLERY);

	}
}
