package ms.organizer.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.ArrayList;

import ms.organizer.R;
import ms.organizer.adapters.IngredientsWithQuantityAdapter;
import ms.organizer.database.ingredients.IngredientWithQuantity;
import ms.organizer.utility.OrganizerUtility;

/**
 * Created by Miki on 2018-02-24.
 */

public class AddRecipeDialog extends DialogFragment {
    private EditText recipeTitle;
    private EditText recipeDescription;
    private ArrayList<IngredientWithQuantity> ingredients;
    private ChooseIngredientDialog dialog;

    public interface AddRecipeDialogListener {
        void onRecipeAdded(String title, ArrayList<IngredientWithQuantity> ingredients, String description);
    }

    AddRecipeDialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (AddRecipeDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement AddRecipeDialogListener");
        }
    }

    private IngredientsWithQuantityAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.add_recipe, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        recipeTitle = view.findViewById(R.id.add_title);
        recipeDescription = view.findViewById(R.id.add_description);
        ingredients = new ArrayList<>();

        ImageButton addIngredientButton = view.findViewById(R.id.add_ingredient_button);
        addIngredientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new ChooseIngredientDialog();
                dialog.show(getFragmentManager(), "AddToRecipeDialog");
            }
        });

        RecyclerView ingredientsList = view.findViewById(R.id.ingredients_list);
        adapter = new IngredientsWithQuantityAdapter(getContext(), true);
        ingredientsList.setAdapter(adapter);
        ingredientsList.setLayoutManager(new LinearLayoutManager(getContext()));

        ArrayList<IngredientWithQuantity> noIngredients = new ArrayList<>();
        noIngredients.add(new IngredientWithQuantity(getString(R.string.no_ingredients), "", "", 0, 0));
        adapter.setIngredients(noIngredients);

        Button addButton = view.findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = recipeTitle.getText().toString();
                String description = recipeDescription.getText().toString();
                if (!title.isEmpty() && !description.isEmpty()) {
                    if (!ingredients.isEmpty()) {
                        listener.onRecipeAdded(title, ingredients, description);
                        dismiss();
                    } else
                        OrganizerUtility.makeToast(v.getContext(), getString(R.string.at_least_one_ingredient));
                } else
                    OrganizerUtility.makeToast(v.getContext(), getString(R.string.fill_all_fields));
            }
        });

        return view;
    }

    public void addIngredient(IngredientWithQuantity ingredient) {
        int index = ingredients.indexOf(ingredient);

        if (index != -1) {
            ingredients.remove(index);
            ingredients.add(index, ingredient);
        } else
            ingredients.add(ingredient);

        adapter.setIngredients(ingredients);
    }

    public void deleteIngredient(IngredientWithQuantity ingredient) {
        ingredients.remove(ingredient);
        adapter.setIngredients(ingredients);
    }

    public void editIngredient(IngredientWithQuantity ingredient) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("ingredient", ingredient);

        ChooseIngredientDialog dialog = new ChooseIngredientDialog();
        dialog.setArguments(bundle);
        dialog.show(getFragmentManager(), "EditQuantityDialog");
    }

    public void setSelectedIngredient(IngredientWithQuantity ingredient) {
        dialog.setSelectedIngredient(ingredient, true);
    }
}
