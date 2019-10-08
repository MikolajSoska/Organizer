package ms.organizer.database.fridge;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import ms.organizer.database.ingredients.IngredientWithQuantity;

@Dao
public interface FridgeDAO {

    @Insert
    void insert(FridgeItem item);

    @Insert
    void insert(PlannedIngredient ingredient);

    @Delete
    void delete(FridgeItem item);

    @Delete
    void delete(PlannedIngredient ingredient);

    @Query("DELETE FROM fridge")
    void clearFridge();

    @Query("DELETE FROM planned_ingredients WHERE mealID = :mealID")
    void deletePlannedIngredients(int mealID);

    @Query("UPDATE fridge SET quantity = quantity + :quantity WHERE ingredientID = :ingredientID")
    void addToFridge(int ingredientID, double quantity);

    @Query("UPDATE fridge SET quantity = :quantity WHERE ingredientID = :ingredientID")
    void updateQuantity(int ingredientID, double quantity);

    @Query("UPDATE planned_ingredients SET quantity = :newQuantity WHERE mealID = :mealID AND ingredientID = :ingredientID")
    void updatePlannedQuantity(int ingredientID, int mealID, double newQuantity);

    @Update
    void updateQuantities(List<FridgeItem> items);

    @Update
    void updateQuantity(FridgeItem item);

    @Query("UPDATE planned_ingredients SET quantityInFridge = quantityInFridge + :quantityToAdd " +
            "AND quantityInList = :newQuantityInList WHERE ingredientID = :ingredientID AND mealID = :mealID")
    void addFromListToFridge(final int ingredientID, final int mealID, final double quantityToAdd, final double newQuantityInList);

    @Query("SELECT * FROM fridge NATURAL JOIN ingredients ORDER BY name")
    LiveData<List<IngredientWithQuantity>> getFromFridge();

    @Query("SELECT ingredientID, name, recipeUnit, shopUnit, shopUnitQuantity, SUM(quantity) AS quantity " +
            "FROM planned_ingredients NATURAL JOIN ingredients GROUP BY ingredientID ORDER BY name")
    LiveData<List<IngredientWithQuantity>> getPlannedIngredients();

    @Query("SELECT * FROM fridge WHERE ingredientID = :ingredientID")
    FridgeItem getFridgeItem(int ingredientID);

    @Query("SELECT * FROM planned_ingredients WHERE mealID = :mealID AND ingredientID = :ingredientId")
    PlannedIngredient getPlannedIngredient(int mealID, int ingredientId);

    @Query("SELECT * FROM planned_ingredients WHERE ingredientID = :ingredientID AND mealID <> :mealID")
    List<PlannedIngredient> getSpecifiedPlannedIngredients(int ingredientID, int mealID);

    @Query("SELECT * FROM planned_ingredients WHERE mealID = :mealID")
    List<PlannedIngredient> getMealPlannedIngredients(int mealID);

    @Query("SELECT ID AS ingredientID, mealID, quantity FROM meals WHERE planned = 1 AND quantity <> 0 AND ID = :ingredientID")
    List<PlannedIngredient> getPlannedMealsWithSpecifiedIngredient(int ingredientID);

    @Query("SELECT ingredientID, mealID, iir.quantity FROM meals INNER JOIN ingredients_in_recipe AS iir ON ID = recipeID " +
            "WHERE planned = 1 AND meals.quantity = 0 AND ingredientID = :ingredientID")
    List<PlannedIngredient> getPlannedRecipesWithSpecifiedIngredient(int ingredientID);

    @Query("SELECT * FROM planned_ingredients WHERE ingredientID = :ingredientID AND quantityInList > 0")
    List<PlannedIngredient> getPlannedIngredients(int ingredientID);

    @Query("SELECT SUM(quantityInList) FROM planned_ingredients WHERE ingredientID = :ingredientID")
    Double getPlannedIngredientQuantityInList(int ingredientID);
}
