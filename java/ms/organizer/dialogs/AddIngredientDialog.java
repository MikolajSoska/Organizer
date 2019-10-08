package ms.organizer.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import java.util.Locale;
import java.util.Objects;

import ms.organizer.R;
import ms.organizer.database.ingredients.Ingredient;
import ms.organizer.utility.OrganizerUtility;

/**
 * Created by Miki on 2018-09-08.
 */

public class AddIngredientDialog extends DialogFragment {
    private int ingredientID;
    private String name;
    private String recipeUnit;
    private String shopUnit;
    private double shopUnitQuantity;

    public interface AddIngredientDialogListener {
        void addIngredient(Ingredient ingredient);
        void editIngredient(Ingredient ingredient);
    }

    AddIngredientDialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (AddIngredientDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement AddIngredientDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_ingredient_dialog, null);

        final TextInputLayout ingredientNameLayout = view.findViewById(R.id.name);
        final TextInputLayout recipeUnitLayout = view.findViewById(R.id.recipe_unit);
        final TextInputLayout shopUnitLayout = view.findViewById(R.id.shop_unit);
        final TextInputLayout shopUnitQuantityLayout = view.findViewById(R.id.subunit_quantity);

        final CheckBox subunitCheckBox = view.findViewById(R.id.subunit_checkbox);

        if (name != null) {
            Objects.requireNonNull(ingredientNameLayout.getEditText()).setText(OrganizerUtility.formatIngredientName(name));
            Objects.requireNonNull(recipeUnitLayout.getEditText()).setText(recipeUnit);
            Objects.requireNonNull(shopUnitLayout.getEditText()).setText(shopUnit);
            Objects.requireNonNull(shopUnitQuantityLayout.getEditText()).setText(String.format(Locale.getDefault(), "%.2f", shopUnitQuantity));
            subunitCheckBox.setChecked(true);
            shopUnitLayout.setVisibility(View.VISIBLE);
            shopUnitQuantityLayout.setVisibility(View.VISIBLE);
        }

        subunitCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (subunitCheckBox.isChecked()) {
                    shopUnitLayout.setVisibility(View.VISIBLE);
                    shopUnitQuantityLayout.setVisibility(View.VISIBLE);
                }
                else {
                    shopUnitLayout.setVisibility(View.GONE);
                    shopUnitQuantityLayout.setVisibility(View.GONE);
                }
            }
        });

        builder.setTitle(R.string.add_ingredient);
        final Dialog dialog = builder.setView(view)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getDialog().cancel();
                    }
                }).create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialogInterface) {
                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String newName = Objects.requireNonNull(ingredientNameLayout.getEditText()).getText().toString().toLowerCase();
                        String newRecipeUnit = Objects.requireNonNull(recipeUnitLayout.getEditText()).getText().toString().toLowerCase();
                        String newShopUnit = Objects.requireNonNull(shopUnitLayout.getEditText()).getText().toString().toLowerCase();
                        String newShopUnitQuantity = Objects.requireNonNull(shopUnitQuantityLayout.getEditText()).getText().toString();

                        String errorLabel = getString(R.string.field_required);
                        boolean error = false;

                        if (newName.isEmpty()) {
                            ingredientNameLayout.setError(errorLabel);
                            error = true;
                        }
                        if (newRecipeUnit.isEmpty()) {
                            recipeUnitLayout.setError(errorLabel);
                            error = true;
                        }
                        if (subunitCheckBox.isChecked()) {
                            if (newShopUnit.isEmpty()) {
                                shopUnitLayout.setError(errorLabel);
                                error = true;
                            }
                            if (newShopUnitQuantity.isEmpty()) {
                                shopUnitQuantityLayout.setError(errorLabel);
                                error = true;
                            }
                        }
                        else {
                            newShopUnit = newRecipeUnit;
                            newShopUnitQuantity = "1.0";
                        }

                        if (!error){
                            if (getTag().equals("AddIngredientDialog"))
                                listener.addIngredient(new Ingredient(0, newName, newRecipeUnit, newShopUnit, Double.parseDouble(newShopUnitQuantity)));
                            else {
                                listener.editIngredient(new Ingredient(ingredientID, newName, newRecipeUnit, newShopUnit, Double.parseDouble(newShopUnitQuantity)));
                            }
                            dialog.dismiss();
                        }
                    }
                });
            }
        });
        return dialog;
    }

    @Override
    public void setArguments(Bundle args) {
        ingredientID = args.getInt("ID");
        name = args.getString("name");
        recipeUnit = args.getString("recipe_unit");
        shopUnit = args.getString("shop_unit");
        shopUnitQuantity = args.getDouble("shop_unit_quantity");
    }
}
