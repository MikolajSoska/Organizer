package ms.organizer.activities;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ms.organizer.R;
import ms.organizer.adapters.IngredientsWithQuantityAdapter;
import ms.organizer.database.fridge.FridgeItem;
import ms.organizer.database.fridge.FridgeViewModel;
import ms.organizer.database.fridge.PlannedIngredient;
import ms.organizer.database.ingredients.Ingredient;
import ms.organizer.database.ingredients.IngredientWithQuantity;
import ms.organizer.database.meals.Meal;
import ms.organizer.database.meals.MealsViewModel;
import ms.organizer.database.recipes.Recipe;
import ms.organizer.database.recipes.RecipesViewModel;
import ms.organizer.database.shoppinglist.ShoppingListViewModel;
import ms.organizer.dialogs.ChooseIngredientDialog;
import ms.organizer.dialogs.IngredientMenuDialog;
import ms.organizer.dialogs.RecipePickerDialog;
import ms.organizer.utility.OrganizerUtility;

/**
 * Created by Miki on 10.10.2018.
 */

public class Fridge extends AppCompatActivity implements ChooseIngredientDialog.ChooseIngredientDialogListener, IngredientMenuDialog.IngredientMenuDialogListener {
    private FridgeViewModel fridgeViewModel;
    private MealsViewModel mealsViewModel;
    private RecipesViewModel recipesViewModel;
    private RecipePickerDialog recipePickerDialog;
    private ChooseIngredientDialog chooseIngredientDialog;
    private MenuItem addMealButton;
    private String mealType;
    private String tag;
    private boolean enableMenu;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fridge);

        final TextView plannedText = findViewById(R.id.planned_ingredients_text);

        RecyclerView ingredientsInFridge = findViewById(R.id.ingredients_in_fridge);
        final IngredientsWithQuantityAdapter adapter = new IngredientsWithQuantityAdapter(this, true);
        ingredientsInFridge.setAdapter(adapter);
        ingredientsInFridge.setLayoutManager(new LinearLayoutManager(this));

        final RecyclerView plannedIngredients = findViewById(R.id.planned_ingredients);
        final IngredientsWithQuantityAdapter plannedAdapter = new IngredientsWithQuantityAdapter(this, false);
        plannedIngredients.setAdapter(plannedAdapter);
        plannedIngredients.setLayoutManager(new LinearLayoutManager(this));

        fridgeViewModel = ViewModelProviders.of(this).get(FridgeViewModel.class);
        fridgeViewModel.getFromFridge().observe(this, new Observer<List<IngredientWithQuantity>>() {
            @Override
            public void onChanged(@Nullable List<IngredientWithQuantity> ingredients) {
                if (ingredients != null) {
                    enableMenu = ingredients.size() != 0;
                    if (addMealButton != null)
                        addMealButton.setEnabled(enableMenu);
                    adapter.setIngredients(ingredients);
                }
            }
        });

        fridgeViewModel.getPlannedIngredients().observe(this, new Observer<List<IngredientWithQuantity>>() {
            @Override
            public void onChanged(@Nullable List<IngredientWithQuantity> ingredients) {
                if (ingredients != null && ingredients.size() > 0) {
                    plannedText.setVisibility(View.VISIBLE);
                    plannedIngredients.setVisibility(View.VISIBLE);
                }
                else {
                    plannedText.setVisibility(View.GONE);
                    plannedIngredients.setVisibility(View.GONE);
                }
                plannedAdapter.setIngredients(ingredients);
            }
        });

        mealsViewModel = ViewModelProviders.of(this).get(MealsViewModel.class);
        recipesViewModel = ViewModelProviders.of(this).get(RecipesViewModel.class);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.fridge_menu, menu);
        addMealButton = menu.findItem(R.id.add_meal);
        addMealButton.setEnabled(enableMenu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.add_shopping) {
            tag = "AddShoppingDialog";
            chooseIngredientDialog = new ChooseIngredientDialog();
            chooseIngredientDialog.show(getSupportFragmentManager(), tag);
        } else if (id == R.id.add_meal) {
            pickMealType();
        } else if (id == R.id.clear_fridge) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.clear_fridge_alert)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            fridgeViewModel.clearFridge();
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }).create().show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void pickMealType() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.pick_meal_type)
                .setItems(R.array.meal_types_array, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                mealType = "breakfast";
                                break;
                            case 1:
                                mealType = "dinner";
                                break;
                            case 2:
                                mealType = "supper";
                                break;
                        }
                        pickMeal();
                    }
                }).create().show();
    }

    private void pickMeal() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.using_recipe)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        recipePickerDialog = new RecipePickerDialog();
                        recipePickerDialog.show(getSupportFragmentManager(), "RecipePickerDialog");
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tag = "AddMealDialog";
                        chooseIngredientDialog = new ChooseIngredientDialog();
                        chooseIngredientDialog.show(getSupportFragmentManager(), tag);
                    }
                }).create().show();
    }

    public void onRecipePicked(Recipe recipe) {
        try {
            List<IngredientWithQuantity> recipeIngredients = recipesViewModel.getRecipeIngredients(recipe.getRecipeID());
            ArrayList<FridgeItem> newQuantities = new ArrayList<>(recipeIngredients.size());
            boolean isCanceled = false;

            for (IngredientWithQuantity ingredient : recipeIngredients) {
                int ID = ingredient.getIngredientID();
                double newQuantity = fridgeViewModel.getItemQuantity(ID) - ingredient.getQuantity();
                if (newQuantity >= 0) {
                    newQuantities.add(new FridgeItem(ID, newQuantity));
                } else {
                    OrganizerUtility.makeToast(this, getString(R.string.not_enough_ingredients));
                    isCanceled = true;
                    break;
                }
            }
            if (!isCanceled) {
                fridgeViewModel.updateQuantities(newQuantities);
                mealsViewModel.insert(new Meal(mealType, OrganizerUtility.getCurrentDate(), recipe.getRecipeID(), 0, false));
                recipePickerDialog.dismiss();
                OrganizerUtility.makeToast(this, getString(R.string.meal_added));
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private double updatePlannedIngredients(int ingredientID, double quantity) {
        try {
            List<PlannedIngredient> meals = fridgeViewModel.getPlannedMealsWithoutIngredient(ingredientID);
            for (PlannedIngredient ingredient : meals) {
                if (quantity > 0) {
                    double neededQuantity = ingredient.getQuantity();
                    final ShoppingListViewModel shoppingListViewModel = ViewModelProviders.of(this).get(ShoppingListViewModel.class);
                    final int mealID = ingredient.getMealID();
                    if (quantity >= neededQuantity) {
                        fridgeViewModel.planIngredient(ingredientID, ingredient.getMealID(), neededQuantity);
                        shoppingListViewModel.delete(ingredientID, mealID);
                    }
                    else {
                        fridgeViewModel.planIngredient(ingredientID, ingredient.getMealID(), quantity);
                        shoppingListViewModel.addToShoppingList(ingredientID, mealID, -quantity);
                    }
                    quantity -= neededQuantity;
                }
                else
                    break;
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return quantity;
    }

    @Override
    public void onDialogPositiveClick(IngredientWithQuantity ingredient, double newQuantity) {
        int ingredientID = ingredient.getIngredientID();
        switch (tag) {
            case "AddShoppingDialog":
                fridgeViewModel

                double quantity = updatePlannedIngredients(ingredientID, newQuantity * ingredient.getShopUnitQuantity());
                if (quantity > 0)
                    fridgeViewModel.addToFridge(ingredientID, quantity);
                break;
            case "AddMealDialog":
                fridgeViewModel.addToFridge(ingredientID, -newQuantity);
                mealsViewModel.insert(new Meal(mealType,OrganizerUtility.getCurrentDate(), ingredientID, newQuantity, false));
                OrganizerUtility.makeToast(this, getString(R.string.meal_added));
                break;
            case "EditQuantityDialog":
                fridgeViewModel.updateQuantity(ingredientID, newQuantity);
                break;
        }
    }

    @Override
    public void onIngredientPicked(IngredientWithQuantity ingredient) {
        if ("AddShoppingDialog".equals(tag)) {
            chooseIngredientDialog.setSelectedIngredient(ingredient, false);
        } else {
            chooseIngredientDialog.setSelectedIngredient(ingredient, true);
        }
    }

    @Override
    public void onDialogClick(Ingredient ingredient, int which) {
        if (which == 0)
            fridgeViewModel.delete(ingredient.getIngredientID());
        else {
            Bundle bundle = new Bundle();
            bundle.putSerializable("ingredient", ingredient);
            tag = "EditQuantityDialog";

            chooseIngredientDialog = new ChooseIngredientDialog();
            chooseIngredientDialog.setArguments(bundle);
            chooseIngredientDialog.show(getSupportFragmentManager(), tag);
        }
    }
}
