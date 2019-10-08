package ms.organizer.database.recipes;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;

import ms.organizer.database.ingredients.Ingredient;

@Entity(tableName = "ingredients_in_recipe", primaryKeys = {"recipeID", "ingredientID"},
        indices = {@Index(value = {"recipeID"}), @Index(value = {"ingredientID"})}, foreignKeys = {
        @ForeignKey(entity = Recipe.class, parentColumns = "recipeID", childColumns = "recipeID",
                onDelete = ForeignKey.RESTRICT, onUpdate = ForeignKey.CASCADE),
        @ForeignKey(entity = Ingredient.class, parentColumns = "ingredientID", childColumns = "ingredientID",
                onDelete = ForeignKey.RESTRICT, onUpdate = ForeignKey.CASCADE)})
public class IngredientInRecipe {
    private int recipeID;
    private int ingredientID;
    private double quantity;

    public IngredientInRecipe(int recipeID, int ingredientID, double quantity) {
        this.recipeID = recipeID;
        this.ingredientID = ingredientID;
        this.quantity = quantity;
    }

    public int getRecipeID() {
        return recipeID;
    }

    public int getIngredientID() {
        return ingredientID;
    }

    public double getQuantity() {
        return quantity;
    }
}
