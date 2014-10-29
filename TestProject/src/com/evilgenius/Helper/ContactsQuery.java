package com.evilgenius.Helper;

import java.net.URI;
import java.util.ArrayList;

import com.evilgenius.testproject.MainActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;
import android.widget.Toast;

/**
 * This interface defines constants for the Cursor and CursorLoader, based on constants defined
 * in the {@link android.provider.ContactsContract.Contacts} class.
 */
public class ContactsQuery {

	// An identifier for the loader
	public final static int QUERY_ID = 1;

	// A content URI for the Contacts table
	public final static Uri CONTENT_URI = Contacts.CONTENT_URI;

	// The search/filter query Uri
	public final static Uri FILTER_URI = Contacts.CONTENT_FILTER_URI;

	// The selection clause for the CursorLoader query. The search criteria defined here
	// restrict results to contacts that have a display name and are linked to visible groups.
	// Notice that the search on the string provided by the user is implemented by appending
	// the search string to CONTENT_FILTER_URI.
	@SuppressLint("InlinedApi")
	public final static String SELECTION =
	(Utils.hasHoneycomb() ? Contacts.DISPLAY_NAME_PRIMARY : Contacts.DISPLAY_NAME) +
	"<>''" + " AND " + Contacts.IN_VISIBLE_GROUP + "=1";

	// The desired sort order for the returned Cursor. In Android 3.0 and later, the primary
	// sort key allows for localization. In earlier versions. use the display name as the sort
	// key.
	@SuppressLint("InlinedApi")
	public final static String SORT_ORDER =
	Utils.hasHoneycomb() ? Contacts.SORT_KEY_PRIMARY : Contacts.DISPLAY_NAME;

	// The projection for the CursorLoader query. This is a list of columns that the Contacts
	// Provider should return in the Cursor.
	@SuppressLint("InlinedApi")
	public
	final static String[] PROJECTION = {

		// The contact's row id
		Contacts._ID,

		// A pointer to the contact that is guaranteed to be more permanent than _ID. Given
		// a contact's current _ID value and LOOKUP_KEY, the Contacts Provider can generate
		// a "permanent" contact URI.
		Contacts.LOOKUP_KEY,

		// In platform version 3.0 and later, the Contacts table contains
		// DISPLAY_NAME_PRIMARY, which either contains the contact's displayable name or
		// some other useful identifier such as an email address. This column isn't
		// available in earlier versions of Android, so you must use Contacts.DISPLAY_NAME
		// instead.
		Utils.hasHoneycomb() ? Contacts.DISPLAY_NAME_PRIMARY : Contacts.DISPLAY_NAME,

				// In Android 3.0 and later, the thumbnail image is pointed to by
				// PHOTO_THUMBNAIL_URI. In earlier versions, there is no direct pointer; instead,
				// you generate the pointer from the contact's ID value and constants defined in
				// android.provider.ContactsContract.Contacts.
				Utils.hasHoneycomb() ? Contacts.PHOTO_THUMBNAIL_URI : Contacts._ID,
						Contacts.HAS_PHONE_NUMBER,
						// The sort order column for the returned Cursor, used by the AlphabetIndexer
						SORT_ORDER,
	};

	// The query column numbers which map to each value in the projection
	public final static int ID = 0;
	public final static int LOOKUP_KEY = 1;
	public final static int DISPLAY_NAME = 2;
	public final static int PHOTO_THUMBNAIL_DATA = 3;
	public final static int SORT_KEY = 4;

	public static Contact retrieveContactNumber(Activity activity,Cursor cursor) {

		Contact contact = new Contact();
		//
		//  Find contact based on name.
		//
		
		ContentResolver cr = activity.getContentResolver();
		//		Cursor cursor = cr.query(uri, null,null, null, null);
		if (cursor.moveToFirst()) {
			String contactId = 
					cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
			contact._id = Long.parseLong(contactId);
			contact.set_lookupkey(cursor.getString(cursor.getColumnIndex( ContactsContract.Contacts.LOOKUP_KEY)));
			String displayname = 
					cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			contact.set_firstName(displayname);
			
			//
			//  Get all phone numbers.
			//

			//			String whereName = ContactsContract.Data.MIMETYPE + " = ? AND " 
			//			+ ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID + " = " + contactId; 
			//			String[] whereNameParams = new String[] { StructuredName.CONTENT_ITEM_TYPE};
			//			Cursor nameCur = cr.query(Data.CONTENT_URI, null, whereName, whereNameParams, null);
			Cursor phoneCur = cr.query(Phone.CONTENT_URI,null,
					Phone.CONTACT_ID + " = " + contactId,null,
					null);
			while (phoneCur.moveToNext()) {
				try{

					String number = phoneCur.getString(phoneCur.getColumnIndex(Phone.NUMBER));
					if(number != null && !number.equals(""))
						contact.set_phoneNo(number);

					break;
				}catch(Exception e){
					Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG);
				}
			}
			phoneCur.close();


           
			Cursor noteCursor  = cr.query(Data.CONTENT_URI,
                        null,
                        Note.CONTACT_ID + "=? AND "
                                + Data.MIMETYPE + "=?",
                                
                                new String[] {contactId ,
                    					Note.CONTENT_ITEM_TYPE}, null);

                while (noteCursor.moveToNext()) {
    				try{
                    String note = noteCursor.getString(noteCursor.getColumnIndex(Note.NOTE));
                    if(note != null && !note.equals(""))
                    	contact.set_note(note);
    				}catch(Exception e){}
                }
            noteCursor.close();
			
			String whereName = ContactsContract.Data.MIMETYPE
					+ " = ? AND "
					+ ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID
					+ " = ?";
			String[] whereNameParams = new String[] {
					ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
					contactId };
			Cursor nameCur = cr
					.query(ContactsContract.Data.CONTENT_URI,
							null,
							whereName,
							whereNameParams,
							ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME);
			while (nameCur.moveToNext()) {
				try{
					String fname = nameCur.getString(nameCur.getColumnIndex(StructuredName.GIVEN_NAME));
					if(fname != null && !fname.equals(""))
						contact.set_firstName(fname);
					else
						contact.set_firstName("");
					String lname = nameCur.getString(nameCur.getColumnIndex(StructuredName.FAMILY_NAME));
					if(lname != null && !lname.equals(""))
						contact.set_lastName(lname);
					else
						contact.set_lastName("");
				}catch(Exception e){
					Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG);
				}

			}
			nameCur.close();


		}else
			return null;
		cursor.close();
		return contact;
	}
	public static Contact retrieveContactNumber(Activity activity,String contacturi) {

		//
		//  Find contact based on name.
		//
		Uri uri = Uri.parse(contacturi);
		
		ContentResolver cr = activity.getContentResolver();
		Cursor cursor = cr.query(uri, null,null, null, null);
		Contact contact =retrieveContactNumber(activity, cursor);
		
		Uri imguri = Contacts.getLookupUri(
                contact.getID(),
                contact.get_lookupkey());

		contact._imgUri = imguri.toString();
		return contact;
	}
	public static Boolean addContact(Activity activity, Contact contact) {
		ArrayList<ContentProviderOperation> op_list = new ArrayList<ContentProviderOperation>(); 
		op_list.add(ContentProviderOperation.newInsert(
				ContactsContract.RawContacts.CONTENT_URI)
				.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null) 
				.withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null) 
				//.withValue(RawContacts.AGGREGATION_MODE, RawContacts.AGGREGATION_MODE_DEFAULT) 
				.build()); 

		// first and last names 
		op_list.add(ContentProviderOperation.newInsert(Data.CONTENT_URI) 
				.withValueBackReference(Data.RAW_CONTACT_ID, 0) 
				.withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE) 
				.withValue(StructuredName.GIVEN_NAME, contact._firstName) 
				.withValue(StructuredName.FAMILY_NAME, contact._lastName) 
				.build()); 
		op_list.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
				.withValueBackReference(Data.RAW_CONTACT_ID, 0) 
				.withValue(Data.MIMETYPE, Note.CONTENT_ITEM_TYPE)
	            .withValue(Note.NOTE, contact.get_note())
	            .build());
		op_list.add(ContentProviderOperation.newInsert(Data.CONTENT_URI) 
				.withValueBackReference(Data.RAW_CONTACT_ID, 0) 
				.withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
				.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.get_phoneNo())
				.withValue(ContactsContract.CommonDataKinds.Phone.TYPE, Phone.TYPE_MOBILE)
				.build());
		


		op_list.add(ContentProviderOperation
				.newInsert(ContactsContract.Data.CONTENT_URI)
				.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
				.withValue(
						ContactsContract.Data.MIMETYPE,
						ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
						.withValue(ContactsContract.CommonDataKinds.Photo.DATA15, contact._image)
						.build());
		try{ 
			ContentProviderResult[] results =  activity.getContentResolver().applyBatch(ContactsContract.AUTHORITY, op_list);
			Log.d("ResultContacts",	 results.toString());
			MainActivity.sharedPref.AddMore(results[results.length-1].uri.toString());
		}catch(Exception e){ 
			e.printStackTrace();
			return false;
		} 
		return true;
	}

}
