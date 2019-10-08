package ms.organizer.activities;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

import ms.organizer.adapters.IngredientsAdapter;
import ms.organizer.R;
import ms.organizer.database.ingredients.Ingredient;
import ms.organizer.database.ingredients.IngredientsViewModel;
import ms.organizer.dialogs.AddIngredientDialog;
import ms.organizer.dialogs.IngredientMenuDialog;

/**
 * Created by Miki on 2018-03-23.
 */

public class Ingredients extends AppCompatActivity implements AddIngredientDialog.AddIngredientDialogListener, IngredientMenuDialog.IngredientMenuDialogListener {
    private IngredientsViewModel ingredientsViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ingredients);

        RecyclerView recyclerView = findViewById(R.id.ingredients_list);
        final IngredientsAdapter adapter = new IngredientsAdapter(this, false);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ingredientsViewModel = ViewModelProviders.of(this).get(IngredientsViewModel.class);
        ingredientsViewModel.getIngredients().observe(this, new Observer<List<Ingredient>>() {
            @Override
            public void onChanged(@Nullable List<Ingredient> ingredients) {
                adapter.setIngredients(ingredients);
            }
        });

        FloatingActionButton addIngredientButton = findViewById(R.id.add_ingredient);
        addIngredientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddIngredientDialog dialog = new AddIngredientDialog();
                dialog.show(getSupportFragmentManager(), "AddIngredientDialog");
            }
        });
    }

    @Override
    public void addIngredient(Ingredient ingredient) {
        ingredientsViewModel.insert(ingredient);
    }

    @Override
    public void editIngredient(Ingredient ingredient) {
        ingredientsViewModel.update(ingredient);
    }

    @Override
    public void onDialogClick(Ingredient ingredient, int which) {
        if (which == 0) {
            ingredientsViewModel.delete(ingredient);
        }
        else {
            Bundle bundle = new Bundle();
            bundle.putInt("ID", ingredient.getIngredientID());
            bundle.putString("name", ingredient.getName());
            bundle.putString("recipe_unit", ingredient.getRecipeUnit());
            bundle.putString("shop_unit", ingredient.getShopUnit());
            bundle.putDouble("shop_unit_quantity", ingredient.getShopUnitQuantity());

            AddIngredientDialog editDialog = new AddIngredientDialog();
            editDialog.setArguments(bundle);
            editDialog.show(getSupportFragmentManager(), "EditIngredientDialog");
        }
    }
}
