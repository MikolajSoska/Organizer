package ms.organizer.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import ms.organizer.database.fridge.FridgeDAO;
import ms.organizer.database.fridge.FridgeItem;
import ms.organizer.database.fridge.PlannedIngredient;
import ms.organizer.database.ingredients.Ingredient;
import ms.organizer.database.ingredients.IngredientsDAO;
import ms.organizer.database.meals.Meal;
import ms.organizer.database.meals.MealsDAO;
import ms.organizer.database.recipes.IngredientInRecipe;
import ms.organizer.database.recipes.Recipe;
import ms.organizer.database.recipes.RecipesDAO;
import ms.organizer.database.shoppinglist.ShoppingListDAO;
import ms.organizer.database.shoppinglist.ShoppingListItem;


/**
 * Created by Miki on 2018-09-07.
 */

@Database(entities = {Ingredient.class, Recipe.class, IngredientInRecipe.class, Meal.class, FridgeItem.class, ShoppingListItem.class, PlannedIngredient.class},
        version = 1, exportSchema = false)
public abstract class OrganizerDatabase extends RoomDatabase {
    public abstract IngredientsDAO ingredientsDAO();
    public abstract RecipesDAO recipesDAO();
    public abstract MealsDAO mealsDAO();
    public abstract FridgeDAO fridgeDAO();
    public abstract ShoppingListDAO shoppingListDAO();
    public static OrganizerDatabase database;

    public static OrganizerDatabase getDatabase(final Context context) {
        if(database == null) {
            synchronized (OrganizerDatabase.class) {
                if(database == null) {
                    database = Room.databaseBuilder(context.getApplicationContext(), OrganizerDatabase.class, "organizer_database").build();
                }
            }
        }
        return database;
    }
}
