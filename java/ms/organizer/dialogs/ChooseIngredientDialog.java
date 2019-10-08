package ms.organizer.dialogs;

import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ms.organizer.R;
import ms.organizer.adapters.IngredientsAdapter;
import ms.organizer.database.fridge.FridgeViewModel;
import ms.organizer.database.ingredients.Ingredient;
import ms.organizer.database.ingredients.IngredientWithQuantity;
import ms.organizer.database.ingredients.IngredientsViewModel;
import ms.organizer.utility.OrganizerUtility;

public class ChooseIngredientDialog extends DialogFragment {
    private String tag;
    private IngredientWithQuantity ingredient;

    private ImageView ingredientIcon;
    private TextView ingredientName;
    private TextView ingredientUnit;
    private EditText ingredientQuantity;
    private AlertDialog listDialog;


    public interface ChooseIngredientDialogListener {
        void onDialogPositiveClick(IngredientWithQuantity ingredient, double quantity);
        void onIngredientPicked(IngredientWithQuantity ingredient);
    }

    ChooseIngredientDialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (ChooseIngredientDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement ChooseIngredientDialogListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (tag.contains("Edit")) {
            double quantity = ingredient.getQuantity();
            if ((int) quantity == quantity)
                ingredientQuantity.setText(String.valueOf((int) ingredient.getQuantity()));
            else
                ingredientQuantity.setText(String.valueOf(ingredient.getQuantity()));
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.choose_ingredient_dialog, null);
        tag = getTag();

        ingredientIcon = view.findViewById(R.id.ingredient_icon);
        ingredientName = view.findViewById(R.id.ingredient_name);
        ingredientUnit = view.findViewById(R.id.ingredient_unit);
        ingredientQuantity = view.findViewById(R.id.ingredient_quantity);


        if (tag.contains("Edit")) {
            ingredientName.setClickable(false);
            setSelectedIngredient(ingredient, !tag.contains("List"));
        }
        else {
            View listView = inflater.inflate(R.layout.choose_ingredient_list, null);

            Toolbar toolbar = listView.findViewById(R.id.toolbar);
            toolbar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listDialog.dismiss();
                }
            });

            RecyclerView ingredientList = listView.findViewById(R.id.ingredients_list);
            ingredientList.setLayoutManager(new LinearLayoutManager(getContext()));
            final IngredientsAdapter adapter = new IngredientsAdapter(getContext(), true);
            ingredientList.setAdapter(adapter);

            AlertDialog.Builder listBuilder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.FullScreenDialogStyle));
            listBuilder.setView(listView);

            if (tag.equals("AddMealDialog")) {
                FridgeViewModel fridgeViewModel = ViewModelProviders.of(this).get(FridgeViewModel.class);
                fridgeViewModel.getFromFridge().observe(this, new Observer<List<IngredientWithQuantity>>() {
                    @Override
                    public void onChanged(@Nullable List<IngredientWithQuantity> ingredientWithQuantities) {
                        adapter.setIngredients(ingredientWithQuantities);
                    }
                });
            }
            else {
                IngredientsViewModel ingredientsViewModel = ViewModelProviders.of(this).get(IngredientsViewModel.class);
                ingredientsViewModel.getIngredients().observe(this, new Observer<List<Ingredient>>() {
                    @Override
                    public void onChanged(@Nullable List<Ingredient> ingredients) {
                        adapter.setIngredients(ingredients);
                    }
                });
            }
            listDialog = listBuilder.create();

            ingredientName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listDialog.show();
                }
            });
        }

        if (tag.equals("AddToListDialog") || tag.equals("EditQuantityInListDialog")) {
            ingredientQuantity.setInputType(InputType.TYPE_CLASS_NUMBER);
        }

        switch (tag) {
            case "AddShoppingDialog":
                builder.setTitle(R.string.add_shopping);
                break;
            case "AddMealDialog":
                builder.setTitle(R.string.add_meal);
                break;
            case "AddToListDialog":
                builder.setTitle(R.string.add_to_shopping_list);
                break;
            case "EditQuantityDialog":
            case "EditQuantityInListDialog":
                builder.setTitle(R.string.edit_quantity);
                break;
            case "PlanMealDialog":
                builder.setTitle(R.string.plan_meal);
                break;
            case "AddToRecipeDialog":
                builder.setTitle(R.string.add_to_recipe);
                break;
        }

        final Dialog dialog = builder.setView(view)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getDialog().cancel();
                    }
                })
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String quantity = ingredientQuantity.getText().toString();

                        if (quantity.isEmpty())
                            OrganizerUtility.makeToast(getContext(), getString(R.string.fill_all_fields));
                        else if (Double.parseDouble(quantity) == 0.0)
                            OrganizerUtility.makeToast(getContext(), getString(R.string.zero_quantity));
                        else if (tag.equals("AddMealDialog") && Double.parseDouble(quantity) > ingredient.getQuantity())
                            OrganizerUtility.makeToast(getContext(), getString(R.string.quantity_too_large));
                        else {
                            if (tag.equals("EditQuantityInListDialog"))
                                listener.onDialogPositiveClick(ingredient, -Double.parseDouble(quantity));
                            else
                                listener.onDialogPositiveClick(ingredient, Double.parseDouble(quantity));
                            dialog.dismiss();
                        }

                    }
                });
            }
        });
        return dialog;
    }

    public void setSelectedIngredient(IngredientWithQuantity ingredient, boolean useRecipeUnit) {
        if (listDialog != null) {
            listDialog.dismiss();
            this.ingredient = ingredient;
        }

        ingredientName.setText(OrganizerUtility.formatIngredientName(ingredient.getName()));
        ingredientName.setTextColor(Color.argb(222, 0, 0,0 ));
        OrganizerUtility.setIngredientIcon(getContext(), ingredient.getName(), ingredientIcon, 60);

        if (useRecipeUnit)
            ingredientUnit.setText(ingredient.getRecipeUnit());
        else
            ingredientUnit.setText(ingredient.getShopUnit());
        ingredientQuantity.setVisibility(View.VISIBLE);
        ingredientQuantity.setText(null);
    }

    @Override
    public void setArguments(Bundle args) {
        ingredient = (IngredientWithQuantity) args.getSerializable("ingredient");
    }
}
