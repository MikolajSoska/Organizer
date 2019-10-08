package ms.organizer.database.fridge;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;

import java.util.Objects;

import ms.organizer.database.ingredients.Ingredient;
import ms.organizer.database.meals.Meal;
import ms.organizer.database.shoppinglist.ShoppingListItem;

@Entity(tableName = "planned_ingredients", primaryKeys = {"ingredientID", "mealID"},
        indices = {@Index(value = {"ingredientID"}), @Index(value = {"mealID"})}, foreignKeys = {
        @ForeignKey(entity = Ingredient.class, parentColumns = "ingredientID", childColumns = "ingredientID",
                onDelete = ForeignKey.RESTRICT, onUpdate = ForeignKey.CASCADE),
        @ForeignKey(entity = Meal.class, parentColumns = "mealID", childColumns = "mealID",
                onDelete = ForeignKey.RESTRICT, onUpdate = ForeignKey.CASCADE)})
public class PlannedIngredient {

    private int ingredientID;
    private int mealID;
    private double quantityInFridge;
    private double quantityInList;

    public PlannedIngredient(int ingredientID, int mealID, double quantityInFridge, double quantityInList) {
        this.ingredientID = ingredientID;
        this.mealID = mealID;
        this.quantityInFridge = quantityInFridge;
        this.quantityInList = quantityInList;

    }

    //todo
    public PlannedIngredient(ShoppingListItem item) {
        this.ingredientID = item.getIngredientID();
        this.mealID = Objects.requireNonNull(item.getMealID());
        this.quantity = item.getQuantity();
    }

    public int getIngredientID() {
        return ingredientID;
    }

    public int getMealID() {
        return mealID;
    }

    public double getQuantityInFridge() {
        return quantityInFridge;
    }

    public double getQuantityInList() {
        return quantityInList;
    }

    //todo
    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    //todo
    @Override
    public String toString() {
        return "[" + ingredientID + " " + mealID + " " + quantity + "]";
    }
}
