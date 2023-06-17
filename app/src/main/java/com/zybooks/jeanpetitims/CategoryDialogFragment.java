package com.zybooks.jeanpetitims;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class CategoryDialogFragment extends DialogFragment {

    // Host activity must implement
    public interface OnCategoryEnteredListener {
        void onCategoryEntered(String category);
    }

    private OnCategoryEnteredListener mListener;

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {

        final EditText categoryEditText = new EditText(getActivity());
        categoryEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        categoryEditText.setMaxLines(1);

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.category)
                .setView(categoryEditText)
                .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String category = categoryEditText.getText().toString();
                        mListener.onCategoryEntered(category.trim());
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (OnCategoryEnteredListener) context;
    }
}