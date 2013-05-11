package com.owentech.DevDrawer.activities;

import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.IActivityManager;
import android.content.Context;
import android.content.DialogInterface;
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
import com.owentech.DevDrawer.utils.RootFeatures;

import java.util.*;

public class LocaleSwitcher extends Activity implements TextWatcher
{
	ListView localeListView;
	List<String> localeAutoCompleteList;
	AutoCompleteTextView addLocaleEditText;
	PartialMatchAdapter partialMatchAdapter;
	ImageView addImageView;
	Database database;
	LocaleListAdapter localeListAdapter;
	Button defaultLocale;
	Locale baseLocale;
	SharedPreferences sharedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.locale_switcher);

		setTitle("Locale Switcher");

		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		setupView();

		if (sharedPreferences.getString("baseLocale", null) == null) {
			setBaseLocaleFromSystem();
			saveLocaleToSharedPrefs();
			setDefaultLocaleButtonText();
		}
		else {
			getLocaleFromSharedPrefs();
			setDefaultLocaleButtonText();
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

				AlertDialog.Builder builder = new AlertDialog.Builder(LocaleSwitcher.this)
						.setTitle(getResources().getString(R.string.change_locale_dialog_title))
						.setMessage(getResources().getString(R.string.change_locale_dialog_text) +
								LocaleSwitcher.this.getResources().getConfiguration().locale.getDisplayLanguage() + " ?")
						.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialogInterface, int i)
							{
							 	setBaseLocaleFromSystem();
								saveLocaleToSharedPrefs();
								setDefaultLocaleButtonText();

							}
						});

				AlertDialog alertDialog = builder.create();
				alertDialog.show();

				return true;
			}
		});

		database = new Database(this);

		localeListAdapter = new LocaleListAdapter(this);
		localeListView.setAdapter(localeListAdapter);

		localeAutoCompleteList = localeAutoCompleteList();
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

	private void setupView()
	{
		defaultLocale = (Button) findViewById(R.id.defaultLocaleButton);
		localeListView = (ListView) findViewById(R.id.localeListView);
		addImageView = (ImageView) findViewById(R.id.addButton);
		addLocaleEditText = (AutoCompleteTextView) findViewById(R.id.addLocaleEditText);
	}

	// Method to re-populate the ListView
	private void updateListView()
	{
		localeListAdapter = null;
		localeListAdapter = new LocaleListAdapter(this);
		localeListView.setAdapter(localeListAdapter);
		localeListAdapter.notifyDataSetChanged();
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

        } catch (SecurityException e) {
            // need a little root magic to change it on Android 4.2
            final Context context = getApplicationContext();
            RootFeatures.changeSystemLocale(getPackageCodePath(), language, country, new RootFeatures.Listener() {
                @Override
                public void onFinished(boolean result) {
                    if (result == false) {
                        Toast.makeText(context, "No root access available", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } catch (Exception e) {
			Log.e("LS", "Error while changing the language!", e);
		}
	}

	private void setBaseLocaleFromSystem()
	{
		baseLocale = getResources().getConfiguration().locale;
	}

	private void saveLocaleToSharedPrefs()
	{
		Gson gson = new Gson();
		String jsonLocale = gson.toJson(baseLocale, Locale.class);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString("baseLocale", jsonLocale);
		editor.commit();
	}

	private void getLocaleFromSharedPrefs()
	{
		Gson gson = new Gson();
		baseLocale = gson.fromJson(sharedPreferences.getString("baseLocale", null), Locale.class);
	}

	private void setDefaultLocaleButtonText()
	{
		defaultLocale.setText(baseLocale.getDisplayLanguage(baseLocale) + " (" + baseLocale.getLanguage() + ")");
	}
}
