package ms.organizer.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import ms.organizer.R;
import ms.organizer.database.ingredients.Ingredient;
import ms.organizer.utility.OrganizerUtility;

/**
 * Created by Miki on 2018-09-15.
 */

public class IngredientMenuDialog extends DialogFragment {
    private Ingredient ingredient;

    public interface IngredientMenuDialogListener {
        void onDialogClick(Ingredient ingredient, int which);
    }

    IngredientMenuDialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (IngredientMenuDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement IngredientMenuDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(OrganizerUtility.formatIngredientName(ingredient.getName()))
               .setItems(R.array.ingredient_menu_array, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {
                        listener.onDialogClick(ingredient, i);
                   }
               });
        return builder.create();
    }

    @Override
    public void setArguments(Bundle args) {
        ingredient = (Ingredient) args.getSerializable("ingredient");
    }
}
