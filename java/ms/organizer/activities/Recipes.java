package ms.organizer.activities;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ms.organizer.R;
import ms.organizer.adapters.RecipesAdapter;
import ms.organizer.database.ingredients.Ingredient;
import ms.organizer.database.ingredients.IngredientWithQuantity;
import ms.organizer.database.recipes.Recipe;
import ms.organizer.database.recipes.RecipesViewModel;
import ms.organizer.dialogs.AddRecipeDialog;
import ms.organizer.dialogs.ChooseIngredientDialog;
import ms.organizer.dialogs.IngredientMenuDialog;
import ms.organizer.utility.OrganizerUtility;

/**
 * Created by Miki on 2018-02-24.
 */

public class Recipes extends AppCompatActivity implements ChooseIngredientDialog.ChooseIngredientDialogListener,
        AddRecipeDialog.AddRecipeDialogListener, IngredientMenuDialog.IngredientMenuDialogListener {
    private AddRecipeDialog dialog;
    private RecipesViewModel recipesViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipes);

        FloatingActionButton addRecipeButton = findViewById(R.id.add_recipe_button);
        addRecipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog = new AddRecipeDialog();
                dialog.show(getSupportFragmentManager(), "AddRecipeDialog");
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recipes_recycler_view);
        final RecipesAdapter adapter = new RecipesAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recipesViewModel = ViewModelProviders.of(this).get(RecipesViewModel.class);
        recipesViewModel.getRecipes().observe(this, new Observer<List<Recipe>>() {
            @Override
            public void onChanged(@Nullable List<Recipe> recipes) {
                adapter.setRecipes(recipes);
            }
        });
    }

    public void deleteRecipe(Recipe recipe) {
        recipesViewModel.delete(recipe);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.recipes_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(this, Ingredients.class);
        startActivity(intent);

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDialogPositiveClick(IngredientWithQuantity ingredient, double newQuantity) {
        ingredient.setQuantity(newQuantity);
        dialog.addIngredient(ingredient);
    }

    @Override
    public void onIngredientPicked(IngredientWithQuantity ingredient) {
        dialog.setSelectedIngredient(ingredient);
    }

    @Override
    public void onRecipeAdded(String title, ArrayList<IngredientWithQuantity> ingredients, String description) {
        try {
            JSONArray ingredientsArray = new JSONArray();
            for (IngredientWithQuantity ingredient : ingredients) {
                JSONObject JSONingredient = new JSONObject();
                JSONingredient.put("name", ingredient.getName());
                JSONingredient.put("recipe_unit", ingredient.getRecipeUnit());
                JSONingredient.put("shop_unit", ingredient.getShopUnit());
                JSONingredient.put("shop_unit_quantity", ingredient.getShopUnitQuantity());
                JSONingredient.put("recipe_quantity", ingredient.getQuantity());
                ingredientsArray.put(JSONingredient);
            }

            JSONObject recipeObject = new JSONObject();
            recipeObject.put("title", title);
            recipeObject.put("selectedIngredients", ingredientsArray);
            recipeObject.put("description", description);

            File recipesFolder = new File(Environment.getExternalStorageDirectory() + "/" + getString(R.string.app_name) + "/" + getString(R.string.recipes_folder), getString(R.string.backup_folder));
            String fileName = title.toLowerCase().replace(" ", "_") + ".recipe";
            File recipeFile = new File(recipesFolder, fileName);

            FileWriter writer = new FileWriter(recipeFile);
            writer.write(recipeObject.toString(2));
            writer.close();

            SharedPreferences.Editor editor = getSharedPreferences("read_files", Context.MODE_PRIVATE).edit();
            editor.putBoolean(fileName, true);
            editor.apply();

            recipesViewModel.insert(new Recipe(title, description), ingredients);
            OrganizerUtility.makeToast(this, getString(R.string.recipe_added));
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDialogClick(Ingredient ingredient, int which) {
        if (which == 0)
            dialog.deleteIngredient((IngredientWithQuantity) ingredient);
        else
            dialog.editIngredient((IngredientWithQuantity) ingredient);
    }
}