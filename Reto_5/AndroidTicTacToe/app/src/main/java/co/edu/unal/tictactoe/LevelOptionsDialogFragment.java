package co.edu.unal.tictactoe;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import static co.edu.unal.tictactoe.MainActivity.DIALOG_DIFFICULTY_ID;

public class LevelOptionsDialogFragment extends DialogFragment {

    private String selected;

    private static LevelOptionsDialogFragment instance;

    public static LevelOptionsDialogFragment newInstance(){
        if(instance == null){
            instance = new LevelOptionsDialogFragment();
        }
        return instance;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        final String[] levels = getActivity().getResources().getStringArray(R.array.difficulty_levels);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setSingleChoiceItems(R.array.difficulty_levels, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selected = levels[which];
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getActivity(), levels[which], Toast.LENGTH_SHORT).show();
            }
        });

        return builder.create();
    }
}
