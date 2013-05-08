package com.owentech.DevDrawer.activities;

import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.google.gson.Gson;
import com.owentech.DevDrawer.R;
import com.owentech.DevDrawer.adapters.LocaleListAdapter;
import com.owentech.DevDrawer.adapters.PartialMatchAdapter;
import com.owentech.DevDrawer.utils.Database;

import java.util.*;

public class LocaleSwitcher extends Activity implements TextWatcher
{

	ListView localeListView;
	List<HashMap<String, String>> localeList;
	List<String> localeAutoCompleteList;
	AutoCompleteTextView addLocaleEditText;
	PartialMatchAdapter partialMatchAdapter;
	ImageView addImageView;
	Database database;
	LocaleListAdapter localeListAdapter;
	Button defaultLocale;
	String baseLang, baseLangName;
	Locale baseLocale;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.locale_switcher);

		setTitle("Locale Switcher");

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		defaultLocale = (Button) findViewById(R.id.defaultLocaleButton);

		if (sharedPreferences.getString("baseLocale", null) == null)
		{

			baseLocale = getResources().getConfiguration().locale;

			Gson gson = new Gson();
			String jsonLocale = gson.toJson(baseLocale, Locale.class);
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putString("baseLocale", jsonLocale);
			editor.commit();
			defaultLocale.setText(baseLocale.getDisplayLanguage() + " (" + baseLocale.getLanguage() + ")");

		}
		else
		{
			Gson gson = new Gson();
			baseLocale = gson.fromJson(sharedPreferences.getString("baseLocale", null), Locale.class);
			defaultLocale.setText(baseLocale.getDisplayLanguage(baseLocale) + " (" + baseLocale.getLanguage() + ")");
		}

		defaultLocale.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if (baseLocale != null)
				{
					switchLocales(baseLocale.getLanguage(), baseLocale.getCountry());
				}
			}
		});

		defaultLocale.setOnLongClickListener(new View.OnLongClickListener()
		{
			@Override
			public boolean onLongClick(View view)
			{
				Toast.makeText(LocaleSwitcher.this, "TODO: Dialog to change default Locale", Toast.LENGTH_SHORT).show();
				return true;
			}
		});

		database = new Database(this);

		localeListView = (ListView) findViewById(R.id.localeListView);
		localeListAdapter = new LocaleListAdapter(this);
		localeListView.setAdapter(localeListAdapter);

		addImageView = (ImageView) findViewById(R.id.addButton);
		localeList = getLocales();
		localeAutoCompleteList = localeAutoCompleteList();
		addLocaleEditText = (AutoCompleteTextView) findViewById(R.id.addLocaleEditText);
		partialMatchAdapter = new PartialMatchAdapter(this, localeAutoCompleteList);
		addLocaleEditText.setAdapter(partialMatchAdapter);
		addLocaleEditText.addTextChangedListener(this);

		addImageView.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
			 	database.addLocale(addLocaleEditText.getText().toString());
				updateListView();
				addLocaleEditText.setText("");
			}
		});

		localeListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long l)
			{
				String language = database.getLocales().get(position);
				language = language.substring(language.indexOf('(') + 1);
				language = language.substring(0, language.indexOf(')'));

				String[] localeArray = language.split("_");
				//Toast.makeText(LocaleSwitcher.this, "Lang: " + localeArray[0] + " Country: " + localeArray[1] , Toast.LENGTH_SHORT).show();

				if(localeArray.length > 1)
				{
					switchLocales(localeArray[0], localeArray[1]);
				}
				else
				{
					switchLocales(localeArray[0], null);
				}
			}
		});

	}

	// Method to re-populate the ListView
	public void updateListView()
	{
		localeListAdapter = null;
		localeListAdapter = new LocaleListAdapter(this);
		localeListView.setAdapter(localeListAdapter);
		localeListAdapter.notifyDataSetChanged();
	}

	private List<HashMap<String, String>> getLocales()
	{

		List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

		Locale baseLocale = new Locale("en");

		String[] languageCodes = getAssets().getLocales();
		Arrays.sort(languageCodes);

		for (String langCode : languageCodes)
		{
			Locale locale = new Locale(langCode);

			HashMap<String, String> hashMap = new HashMap<String, String>();
			hashMap.put("code", langCode);
			hashMap.put("name", locale.getDisplayName(baseLocale));
		}

		return list;

	}

	private List<String> localeAutoCompleteList()
	{

		List<String> list = new ArrayList<String>();

		Locale baseLocale = new Locale("en");

		String[] languageCodes = getAssets().getLocales();
		Arrays.sort(languageCodes);

		for (String langCode : languageCodes)
		{
			Locale locale = new Locale(langCode);
			list.add(locale.getDisplayName(baseLocale) + " (" + langCode + ")");
		}

		return list;

	}

	@Override
	public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3)
	{}

	@Override
	public void onTextChanged(CharSequence charSequence, int i, int i2, int i3)
	{
		partialMatchAdapter.getFilter().filter(charSequence.toString());
	}

	@Override
	public void afterTextChanged(Editable editable)
	{}

	private void switchLocales(String language, String country)
	{
		Locale locale;

		if (country != null)
		{
			locale = new Locale(language, country);
		}
		else
		{
			locale = new Locale(language);
		}


		try {
			IActivityManager am = ActivityManagerNative.getDefault();

			Configuration config = am.getConfiguration();
			config.locale = locale;
			am.updateConfiguration(config);


		} catch (Exception e) {
			Log.e("LS", "Error while changing the language!", e);
		}
	}
}
