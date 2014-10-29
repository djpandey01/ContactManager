package com.evilgenius.testproject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.evilgenius.Helper.*;
public class Memo_book extends BaseActivity {

	private ListView dataList;
	private com.evilgenius.Helper.DataBaseHandler db;
	private ArrayList<Contact> imageArry;
	private com.evilgenius.Helper.ContactImageAdapter imageAdapter;
	private static final int CAMERA_REQUEST = 1;
	private static final int PICK_FROM_GALLERY = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_memo_book);
		InitActionbar();
		
		
//		dataList = (ListView) findViewById(R.id.listview);
//		/**
//		 * create DatabaseHandler object
//		 */
		db = new DataBaseHandler(this);
		/**
		 * Reading and getting all records from database
		 */
		List<Contact> contacts = db.getAllContacts();
		for (Contact cn : contacts) {
			String log = "ID:" + cn.getID() + " Name: " + cn.get_firstName()
					+ " ,Image: " + cn.getImage();

			// Writing Contacts to log
			Log.d("Result: ", log);
			// add contacts data in arrayList
			imageArry.add(cn);

		}
		/**
		 * Set Data base Item into listview
		 */
		imageAdapter = new ContactImageAdapter(this, R.layout.screen_list,
				imageArry);
		dataList.setAdapter(imageAdapter);
		/**
		 * go to next activity for detail image
		 */
		dataList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					final int position, long id) {
//				imageName = imageArry.get(position).getImage();
//				imageId = imageArry.get(position).getID();
//
//				Log.d("Before Send:****", imageName + "-" + imageId);
//				// convert byte to bitmap
//				ByteArrayInputStream imageStream = new ByteArrayInputStream(
//						imageName);
//				theImage = BitmapFactory.decodeStream(imageStream);
//				Intent intent = new Intent(SQLiteDemoActivity.this,
//						DisplayImageActivity.class);
//				intent.putExtra("imagename", theImage);
//				intent.putExtra("imageid", imageId);
//				startActivity(intent);

			}
		});
		/**
		 * open dialog for choose camera/gallery
		 */

		final String[] option = new String[] { "Take from Camera",
				"Select from Gallery" };
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.select_dialog_item, option);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle("Select Option");
		builder.setAdapter(adapter, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Log.e("Selected Item", String.valueOf(which));
				if (which == 0) {
					callCamera();
				}
				if (which == 1) {
					callGallery();
				}

			}
		});
	}
	/**
	 * On activity result
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK)
			return;

		switch (requestCode) {
		case CAMERA_REQUEST:

			Bundle extras = data.getExtras();

			if (extras != null) {
				Bitmap yourImage = extras.getParcelable("data");
				// convert bitmap to byte
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				yourImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
				byte imageInByte[] = stream.toByteArray();
				Log.e("output before conversion", imageInByte.toString());
				// Inserting Contacts
				Log.d("Insert: ", "Inserting ..");
				db.addContact(new Contact("Android", imageInByte));
				Intent i = new Intent(this,
						Memo_book.class);
				startActivity(i);
				finish();

			}
			break;
		case PICK_FROM_GALLERY:
			Bundle extras2 = data.getExtras();

			if (extras2 != null) {
				Bitmap yourImage = extras2.getParcelable("data");
				// convert bitmap to byte
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				yourImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
				byte imageInByte[] = stream.toByteArray();
				Log.e("output before conversion", imageInByte.toString());
				// Inserting Contacts
				Log.d("Insert: ", "Inserting ..");
				db.addContact(new Contact("Android", imageInByte));
				Intent i = new Intent(this,
						Memo_book.class);
				startActivity(i);
				finish();
			}

			break;
		}
	}

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
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 0);
		intent.putExtra("aspectY", 0);
		intent.putExtra("outputX", 200);
		intent.putExtra("outputY", 150);
		intent.putExtra("return-data", true);
		startActivityForResult(
				Intent.createChooser(intent, "Complete action using"),
				PICK_FROM_GALLERY);

	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.memo_book, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_search) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
