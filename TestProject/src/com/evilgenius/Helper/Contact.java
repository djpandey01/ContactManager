package com.evilgenius.Helper;

public class Contact {

	// private variables
	long _id;
	private String _lookupkey;
	String _firstName;
	String _lastName;
	String _imgUri;
	private String _phoneNo;
	byte[] _image;
	private String _note;
	// Empty constructor
	public Contact() {

	}

	// constructor
	public Contact(int keyId, String name, byte[] image) {
		this._id = keyId;
		this._firstName = name;
		this._image = image;

	}
	// constructor
		public Contact(int keyId, String name, String imageUri) {
			this._id = keyId;
			this._firstName = name;
			this.set_imgUri(imageUri);

		}
	public Contact(String name, byte[] image) {
		this._firstName = name;
		this._image = image;

	}
	public Contact(int keyId) {
		this._id = keyId;

	}

	// getting ID
	public long getID() {
		return this._id;
	}

	// setting id
	public void setID(int keyId) {
		this._id = keyId;
	}

	// getting name
	public String get_firstName() {
		return this._firstName;
	}

	// setting name
	public void set_firstName(String name) {
		this._firstName = name;
	}

	// getting phone number
	public byte[] getImage() {
		return this._image;
	}

	// setting phone number
	public void setImage(byte[] image) {
		this._image = image;
	}

	public String get_imgUri() {
		return _imgUri;
	}

	public void set_imgUri(String _imgUri) {
		this._imgUri = _imgUri;
	}

	public String get_phoneNo() {
		return _phoneNo;
	}

	public void set_phoneNo(String _phoneNo) {
		this._phoneNo = _phoneNo;
	}

	public String get_note() {
		return _note;
	}

	public void set_note(String _note) {
		this._note = _note;
	}

	public String get_lastName() {
		return _lastName;
	}

	public void set_lastName(String _lastName) {
		this._lastName = _lastName;
	}

	public String get_lookupkey() {
		return _lookupkey;
	}

	public void set_lookupkey(String _lookupkey) {
		this._lookupkey = _lookupkey;
	}
}
