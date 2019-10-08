package ms.organizer.database.fridge;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

import ms.organizer.database.ingredients.Ingredient;

@Entity(tableName = "fridge", foreignKeys = {
        @ForeignKey(entity = Ingredient.class, parentColumns = "ingredientID", childColumns = "ingredientID",
                onDelete = ForeignKey.RESTRICT, onUpdate = ForeignKey.CASCADE)})
public class FridgeItem {

    @PrimaryKey
    private int ingredientID;
    private double quantity;

    public FridgeItem(int ingredientID, double quantity) {
        this.ingredientID = ingredientID;
        this.quantity = quantity;
    }

    public int getIngredientID() {
        return ingredientID;
    }

    public double getQuantity() {
        return quantity;
    }
}
