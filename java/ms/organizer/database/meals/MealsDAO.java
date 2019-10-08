package ms.organizer.database.meals;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import ms.organizer.database.ingredients.Ingredient;
import ms.organizer.database.recipes.Recipe;

@Dao
public interface MealsDAO {

    @Insert
    long insert(Meal meal);

    @Delete
    void delete(Meal meal);

    @Query("UPDATE meals SET quantity = quantity + :quantity WHERE mealID = :mealID")
    void updateQuantity(int mealID, double quantity);

    @Query("UPDATE meals SET planned = 0 WHERE mealID = :mealId")
    void addPlannedMeal(int mealId);

    @Query("SELECT * FROM meals WHERE date = :date AND type = :type")
    LiveData<List<Meal>> getMeals(String date, String type);

    @Query("SELECT recipeID, title, description FROM meals NATURAL JOIN recipes WHERE recipeID = :recipeID")
    Recipe getRecipe(int recipeID);

    @Query("SELECT ingredientID, name, recipeUnit, shopUnit, shopUnitQuantity FROM meals NATURAL JOIN ingredients WHERE ingredientID = :ingredientID")
    Ingredient getIngredient(int ingredientID);

    @Query("SELECT * FROM meals WHERE type = :type AND date = :date AND ID = :ID AND planned = :planned AND quantity <> 0")
    Meal mealExists(String type, String date, int ID, boolean planned);
}
