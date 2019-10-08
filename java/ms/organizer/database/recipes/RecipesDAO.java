package ms.organizer.database.recipes;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import ms.organizer.database.ingredients.IngredientWithQuantity;


@Dao
public interface RecipesDAO {

    @Insert
    void insert(Recipe recipe);

    @Insert
    void insert(IngredientInRecipe ingredientInRecipe);

    @Delete
    void delete(Recipe recipe);

    @Delete
    void delete(IngredientInRecipe ingredientInRecipe);

    @Query("DELETE FROM ingredients_in_recipe WHERE recipeID = :recipeID")
    void deleteAllFromRecipe(int recipeID);

    @Query("SELECT * FROM recipes ORDER BY title ASC")
    LiveData<List<Recipe>> getRecipes();

    @Query("SELECT ingredientID, name, recipeUnit, shopUnit, shopUnitQuantity, quantity FROM ingredients NATURAL JOIN ingredients_in_recipe WHERE recipeID = :recipeID")
    List<IngredientWithQuantity> getRecipeIngredients(int recipeID);

    @Query("SELECT recipeID FROM recipes WHERE title = :title")
    int getRecipeID(String title);

    @Query("SELECT ingredientID FROM ingredients WHERE name = :name")
    int getIngredientIDByName(String name);
}
