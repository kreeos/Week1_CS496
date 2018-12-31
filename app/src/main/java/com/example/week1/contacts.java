package com.example.week1;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class contacts extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    SwipeRefreshLayout swipeLayout;

    public final int EDIT_CONTACT_ACTIVITY_CODE = 1;
    /* global variables */
    private int currentIndex = -1;
    List<HashMap<String, String>> contactList = new ArrayList<HashMap<String, String>>();
    SimpleAdapter simpleAdapter;

    String name;
    Button button;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.tab1_contacts, container, false);

        /* initialize contactList from contacts.json and from phone contacts */

        /* sort list now that contactList is done */
        //sortList();


        /* listener for ListView - invokes SlidingUpPanelLayout */
        ListView listview = rootView.findViewById(R.id.contacts_listview);
        listview.setOnItemClickListener(listviewListener);

        /* adapter for ListView */
        simpleAdapter = new SimpleAdapter(
                getActivity(), contactList, android.R.layout.simple_list_item_2,
                new String[]{"name", "number"}, new int[]{android.R.id.text1, android.R.id.text2}
        );

        FloatingActionButton addButton = rootView.findViewById(R.id.fab_add);
        addButton.setOnClickListener(addButtonListener);

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {

            getContactsFromPhone();
            listview.setAdapter(simpleAdapter);
        }

        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.activity_main_swipe_refresh_layout);
        swipeLayout.setOnRefreshListener(this);

        return rootView;

    }

    //fragment static method의 객체 생성용 instance
    public static contacts newInstance() {
        Bundle args = new Bundle();

        contacts fragment = new contacts();
        fragment.setArguments(args);
        return fragment;
    }




    /* FUNCTION: reads phone contacts */
    public void getContactsFromPhone() {
        contactList.clear(); // called here since this function is called before getContactsFromJSON
        String phoneNumber = null;
        String contact_id;
        ContentResolver contentResolver = getContext().getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC");

        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    contact_id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
                    if (hasPhoneNumber > 0) {
                        Cursor phoneCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{contact_id}, null
                        );
                        while (phoneCursor.moveToNext()) {
                            phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            break; // only read first phone number
                        }
                        phoneCursor.close();
                    }
                    contactList.add(createContact(contact_id, name, phoneNumber));
                }

                cursor.close();
            }
        } catch (NullPointerException e) {}
    }


    private void writeContact() {
        String id = contactList.get(currentIndex).get("id");
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        ContentResolver contentResolver = getContext().getContentResolver();
        Cursor cursor = contentResolver.query(uri, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id, null, null);
        try {
            if (cursor.moveToFirst()) {
                long idContact = cursor.getLong(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                Intent intent = new Intent(Intent.ACTION_EDIT, ContactsContract.Contacts.CONTENT_URI);
                Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, idContact);
                intent.setData(contactUri);
                startActivityForResult(intent, EDIT_CONTACT_ACTIVITY_CODE);
            } else {
                Toast.makeText(getContext(), "Contact cannot be edited", Toast.LENGTH_LONG).show();
            }

            cursor.close();
        } catch (NullPointerException e) {}
    }




    /* FUNCTION: create a contact instance given name and number */
    private HashMap<String, String> createContact(String id, String name, String number) {
        HashMap<String, String> contactItem = new HashMap<String, String>();
        contactItem.put("id", id);
        contactItem.put("name", name);
        contactItem.put("number", number);
        return contactItem;
    }


    /* LISTENER: clicking listview element invokes SlidingUpPanelLayout */
    AdapterView.OnItemClickListener listviewListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
            currentIndex = position;

            CharSequence options[] = new CharSequence[]{"Call Contact", "Edit Contact"};

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
            alertDialogBuilder.setTitle("Options");

            alertDialogBuilder.setItems(options, dialogListener);
            alertDialogBuilder.show();
        }
    };

    AlertDialog.OnClickListener dialogListener = new AlertDialog.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int index) {
            switch (index) {
                case 0:
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    String number = contactList.get(currentIndex).get("number");
                    callIntent.setData(Uri.parse("tel:" + number));
                    startActivity(callIntent);
                    break;
                case 1:
                    writeContact();
                    break;
//                case 2:
//                    deleteContact();
//                    break;
            }
        }
    };

    View.OnClickListener addButtonListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent("android.intent.action.INSERT", ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(intent, EDIT_CONTACT_ACTIVITY_CODE);
        }
    };


    @Override
    public void onRefresh() {
        if (MainActivity.checkPermissions(getActivity(), Manifest.permission.WRITE_CONTACTS) || MainActivity.checkPermissions(getActivity(), Manifest.permission.READ_CONTACTS)) {
            getContactsFromPhone();
        }
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commit();
    }


}