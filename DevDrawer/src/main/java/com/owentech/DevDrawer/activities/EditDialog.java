package com.owentech.DevDrawer.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.owentech.DevDrawer.R;
import com.owentech.DevDrawer.utils.Constants;

/**
 * Created with IntelliJ IDEA.
 * User: owent
 * Date: 31/01/2013
 * Time: 20:58
 * To change this template use File | Settings | File Templates.
 */
public class EditDialog extends Activity
{

	EditText editText;
	Button changeButton;

	String originalText;
	String id;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_dialog);

		editText = (EditText) findViewById(R.id.editDialogEditText);
		changeButton = (Button) findViewById(R.id.changeButton);

		Bundle bundle = getIntent().getExtras();

		originalText = bundle.getString("text");
		id = bundle.getString("id");

		editText.setText(originalText);

		// Change button sends a result back to MainActivity
		changeButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{

				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putString("newText", editText.getText().toString());
				bundle.putString("id", id);
				intent.putExtras(bundle);

				setResult(Constants.EDIT_DIALOG_CHANGE, intent);
				finish();

			}
		});

	}

	@Override
	protected void onStop()
	{
		super.onStop();
		finish();
	}
}
