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
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import ms.organizer.R;
import ms.organizer.adapters.MealScheduleAdapter;
import ms.organizer.database.fridge.FridgeViewModel;
import ms.organizer.database.fridge.PlannedIngredient;
import ms.organizer.database.ingredients.Ingredient;
import ms.organizer.database.ingredients.IngredientWithQuantity;
import ms.organizer.database.meals.Meal;
import ms.organizer.database.meals.MealsViewModel;
import ms.organizer.database.recipes.Recipe;
import ms.organizer.database.recipes.RecipesViewModel;
import ms.organizer.database.shoppinglist.ShoppingListItem;
import ms.organizer.database.shoppinglist.ShoppingListViewModel;
import ms.organizer.dialogs.ChooseIngredientDialog;
import ms.organizer.dialogs.OrganizerDatePickerDialog;
import ms.organizer.dialogs.RecipePickerDialog;
import ms.organizer.utility.OrganizerUtility;

public class MealSchedule extends AppCompatActivity implements OrganizerDatePickerDialog.DatePickerDialogListener, ChooseIngredientDialog.ChooseIngredientDialogListener {
    private RecipesViewModel recipesViewModel;
    private MealsViewModel mealsViewModel;
    private FridgeViewModel fridgeViewModel;
    private ShoppingListViewModel shoppingListViewModel;
    private RecipePickerDialog recipePickerDialog;
    private ChooseIngredientDialog chooseIngredientDialog;
    private Hashtable<String, MealScheduleAdapter> adapters;
    private Calendar calendar;
    private TextView dateView;
    private String pickedType;

    private final static String[] MEAL_TYPES = {"breakfast", "dinner", "supper"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meal_schedule);

        calendar = Calendar.getInstance();
        dateView = findViewById(R.id.meal_schedule_date);
        setDate();

        adapters = new Hashtable<>(MEAL_TYPES.length);
        for (String type : MEAL_TYPES) {
            prepareMealsList(type);
        }

        recipesViewModel = ViewModelProviders.of(this).get(RecipesViewModel.class);
        mealsViewModel = ViewModelProviders.of(this).get(MealsViewModel.class);
        fridgeViewModel = ViewModelProviders.of(this).get(FridgeViewModel.class);
        shoppingListViewModel = ViewModelProviders.of(this).get(ShoppingListViewModel.class);
        setMealsLists();

        final ImageButton previousDayButton = findViewById(R.id.previous_day);
        final ImageButton nextDayButton= findViewById(R.id.next_day);

        previousDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                setDate();
                setMealsLists();
            }
        });

        nextDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                setDate();
                setMealsLists();
            }
        });
    }

    private void setDate() {
        Date currentDate = calendar.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMM yyyy", Locale.getDefault());
        String dateString = dateFormat.format(currentDate);

        dateView.setText(dateString);
    }

    private String getCurrentDate() {
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        final int month = calendar.get(Calendar.MONTH);
        final int year = calendar.get(Calendar.YEAR);

        return (day + "-" + month + "-" + year);
    }

    private void prepareMealsList(final String type) {
        int viewID = getResources().getIdentifier(type + "_list", "id", getPackageName());
        int nameID = getResources().getIdentifier(type, "string", getPackageName());
        int iconID = getResources().getIdentifier("icon_" + type, "drawable", getPackageName());

        View mealView = findViewById(viewID);

        TextView mealType = mealView.findViewById(R.id.meal_type);
        mealType.setText(getString(nameID));
        mealType.setCompoundDrawablesWithIntrinsicBounds(iconID, 0, 0, 0);

        RecyclerView mealList = mealView.findViewById(R.id.meal_list);
        MealScheduleAdapter adapter = new MealScheduleAdapter(this);
        mealList.setAdapter(adapter);
        mealList.setLayoutManager(new LinearLayoutManager(this));
        adapters.put(type, adapter);

        ImageButton addMealButton = mealView.findViewById(R.id.add_meal_button);
        addMealButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickedType = type;
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setMessage(R.string.using_recipe)
                        .setTitle(R.string.plan_meal)
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
                                chooseIngredientDialog = new ChooseIngredientDialog();
                                chooseIngredientDialog.show(getSupportFragmentManager(), "PlanMealDialog");
                            }
                        }).create().show();
            }
        });
    }

    @Override
    public void onIngredientPicked(IngredientWithQuantity ingredient) {
        chooseIngredientDialog.setSelectedIngredient(ingredient, true);
    }

    @Override
    public void onDialogPositiveClick(IngredientWithQuantity ingredient, final double newQuantity) {
        final int ingredientID = ingredient.getIngredientID();
        final Meal meal = new Meal(pickedType, getCurrentDate(), ingredientID, newQuantity, true);
        try {
            final int mealID = mealsViewModel.insertAndGetID(meal);
            final double currentQuantity = fridgeViewModel.getItemQuantity(ingredientID);
            if (newQuantity > currentQuantity) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.shopping_list_updated)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                shoppingListViewModel.addToShoppingList(ingredientID, mealID, -(currentQuantity - newQuantity));
                            }
                        })
                        .create()
                        .show();
                if (currentQuantity > 0)
                    fridgeViewModel.planIngredient(ingredientID, mealID, currentQuantity, 0);
            } else
                fridgeViewModel.planIngredient(ingredientID, mealID, newQuantity, currentQuantity - newQuantity);
            OrganizerUtility.makeToast(getApplicationContext(), getString(R.string.meal_planned));
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void planMeal(final Recipe recipe) {
        recipePickerDialog.dismiss();
        try {
            Meal meal = new Meal(pickedType, getCurrentDate(), recipe.getRecipeID(), 0, true);
            int mealID = mealsViewModel.insertAndGetID(meal);
            final List<IngredientWithQuantity> recipeIngredients = recipesViewModel.getRecipeIngredients(recipe.getRecipeID());
            final ArrayList<ShoppingListItem> shoppingList = new ArrayList<>();
            boolean ingredientsNeeded = false;

            for (IngredientWithQuantity ingredient : recipeIngredients) {
                int ID = ingredient.getIngredientID();
                double currentQuantity = fridgeViewModel.getItemQuantity(ID);
                double newQuantity = currentQuantity - ingredient.getQuantity();
                if (newQuantity < 0) {
                    ingredientsNeeded = true;
                    shoppingList.add(new ShoppingListItem(ID, mealID, -newQuantity));
                    if (currentQuantity > 0)
                        fridgeViewModel.planIngredient(ID, mealID, currentQuantity, 0);
                } else
                    fridgeViewModel.planIngredient(ID, mealID, ingredient.getQuantity(), newQuantity);
            }

            if (ingredientsNeeded) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.shopping_list_updated)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                shoppingListViewModel.insertAll(shoppingList);
                            }
                        })
                        .create()
                        .show();
            }
            OrganizerUtility.makeToast(getApplicationContext(), getString(R.string.meal_planned));

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            OrganizerUtility.makeToast(this, getString(R.string.get_ingredients_error));
        }
    }

    public void addPlannedMeal(final Meal meal) {
        try {
            final int mealID = meal.getMealID();
            if (meal.getQuantity() != 0) {
                final int ingredientID = meal.getID();
                final double quantity = fridgeViewModel.getPlannedQuantity(mealID, ingredientID);

                if (quantity == meal.getQuantity()) {
                    fridgeViewModel.cancelIngredient(ingredientID, mealID);
                    mealsViewModel.addPlannedMeal(mealID);
                    OrganizerUtility.makeToast(this, getString(R.string.meal_added));
                } else {
                    double neededQuantity = meal.getQuantity() - quantity;
                    final List<PlannedIngredient> availableIngredients = getAvailableIngredients(ingredientID, mealID, neededQuantity);

                    if (availableIngredients != null) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setMessage(R.string.planned_to_other_meals);
                        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                double neededQuantity = meal.getQuantity() - quantity;
                                updatePlannedIngredients(availableIngredients, neededQuantity);
                                shoppingListViewModel.deleteAllMealIngredients(mealID);
                                fridgeViewModel.cancelIngredient(ingredientID, mealID);
                                mealsViewModel.addPlannedMeal(mealID);
                                OrganizerUtility.makeToast(getApplicationContext(), getString(R.string.meal_added));
                            }
                        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).create().show();
                    } else
                        OrganizerUtility.makeToast(this, getString(R.string.not_enough_ingredients));
                }
            } else {
                final List<IngredientWithQuantity> recipeIngredients = recipesViewModel.getRecipeIngredients(meal.getID());
                final SparseArray<Double> neededQuantities = new SparseArray<>();
                final SparseArray<List<PlannedIngredient>> availableIngredientsLists = new SparseArray<>();

                for (IngredientWithQuantity ingredient : recipeIngredients) {
                    int ingredientID = ingredient.getIngredientID();
                    double quantity = fridgeViewModel.getPlannedQuantity(mealID, ingredientID);
                    if (quantity < ingredient.getQuantity()) {
                        double neededQuantity = ingredient.getQuantity() - quantity;
                        List<PlannedIngredient> availableIngredients = getAvailableIngredients(ingredientID, mealID, neededQuantity);

                        if (availableIngredients != null) {
                            availableIngredientsLists.put(ingredientID, availableIngredients);
                            neededQuantities.put(ingredientID, ingredient.getQuantity() - quantity);
                        } else {
                            OrganizerUtility.makeToast(this, getString(R.string.not_enough_ingredients));
                            return;
                        }
                    }
                }

                if (neededQuantities.size() != 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(R.string.planned_to_other_meals);
                    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            for (int i = 0; i < availableIngredientsLists.size(); i++) {
                                int ingredientID = availableIngredientsLists.keyAt(i);
                                List<PlannedIngredient> availableIngredients = availableIngredientsLists.get(ingredientID);
                                double neededQuantity = neededQuantities.get(ingredientID);

                                updatePlannedIngredients(availableIngredients, neededQuantity);
                            }
                            shoppingListViewModel.deleteAllMealIngredients(mealID);
                            for (IngredientWithQuantity ingredient : recipeIngredients)
                                fridgeViewModel.cancelIngredient(ingredient.getIngredientID(), mealID);

                            mealsViewModel.addPlannedMeal(mealID);
                            OrganizerUtility.makeToast(getApplicationContext(), getString(R.string.meal_added));
                        }
                    }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }).create().show();
                } else {
                    for (IngredientWithQuantity ingredient : recipeIngredients)
                        fridgeViewModel.cancelIngredient(ingredient.getIngredientID(), mealID);

                    mealsViewModel.addPlannedMeal(mealID);
                    OrganizerUtility.makeToast(this, getString(R.string.meal_added));
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            OrganizerUtility.makeToast(this, getString(R.string.get_ingredients_error));
        }
    }

    @Nullable
    private List<PlannedIngredient> getAvailableIngredients(int ingredientID, int mealID, double neededQuantity) throws ExecutionException, InterruptedException {
        List<PlannedIngredient> ingredients = fridgeViewModel.getSpecifiedPlannedIngredients(ingredientID, mealID);

        if (!ingredients.isEmpty()) {
            final ArrayList<PlannedIngredient> availableIngredients = new ArrayList<>();

            for (PlannedIngredient ingredient : ingredients) {
                availableIngredients.add(ingredient);
                neededQuantity -= ingredient.getQuantity();
                if (neededQuantity <= 0) {
                    return availableIngredients;
                }
            }
        }
        return null;
    }

    private void updatePlannedIngredients(List<PlannedIngredient> ingredients, double neededQuantity) {
        for (PlannedIngredient ingredient : ingredients) {
            if (neededQuantity <= 0)
                break;
            else if (neededQuantity - ingredient.getQuantity() < 0) {
                double newQuantity = ingredient.getQuantity() - neededQuantity;
                fridgeViewModel.updatePlannedQuantity(ingredient.getIngredientID(), ingredient.getMealID(), newQuantity);
            } else
                fridgeViewModel.cancelIngredient(ingredient.getIngredientID(), ingredient.getMealID());

            shoppingListViewModel.addToShoppingList(ingredient.getIngredientID(), ingredient.getMealID(), neededQuantity);
            neededQuantity -= ingredient.getQuantity();
        }
    }

    private void setMealsLists() {
        for (String type : MEAL_TYPES) {
            setMealsList(type);
        }
    }

    private void setMealsList(final String type) {
        mealsViewModel.getMeals(getCurrentDate(), type).observe(this, new Observer<List<Meal>>() {
            @Override
            public void onChanged(@Nullable List<Meal> meals) {
                Objects.requireNonNull(adapters.get(type)).setMeals(meals);
            }
        });
    }

    public void deleteMeal(Meal meal) {
        if (meal.isPlanned()) {
            int mealID = meal.getMealID();
            shoppingListViewModel.deleteAllMealIngredients(mealID);
            fridgeViewModel.cancelIngredients(mealID);
        }
        mealsViewModel.delete(meal);
    }

    public Recipe getRecipe(int recipeID) {
        try {
            return mealsViewModel.getRecipe(recipeID);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Ingredient getIngredient(int ingredientID) {
        try {
            return mealsViewModel.getIngredient(ingredientID);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.meal_schedule_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.date_picker) {
            Bundle date = new Bundle();
            date.putInt("day", calendar.get(Calendar.DAY_OF_MONTH));
            date.putInt("month", calendar.get(Calendar.MONTH));
            date.putInt("year", calendar.get(Calendar.YEAR));

            OrganizerDatePickerDialog dialog = new OrganizerDatePickerDialog();
            dialog.setArguments(date);
            dialog.show(getSupportFragmentManager(), "DatePickerDialog");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDateSet(int day, int month, int year) {
        calendar.set(year, month, day);
        setDate();
        setMealsLists();
    }
}
