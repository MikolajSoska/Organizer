package ms.organizer.database.shoppinglist;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;

import ms.organizer.database.ingredients.Ingredient;

@Entity(tableName = "shopping_list",
        primaryKeys = {"ingredientID", "checked"},
        foreignKeys = { @ForeignKey(entity = Ingredient.class,
                parentColumns = "ingredientID", childColumns = "ingredientID",
                onDelete = ForeignKey.RESTRICT, onUpdate = ForeignKey.CASCADE)})
public class ShoppingListItem {

    private int ingredientID;
    private double quantity;
    private boolean checked;
    private int position;

    public ShoppingListItem(int ingredientID, double quantity, boolean checked, int position) {
        this.ingredientID = ingredientID;
        this.quantity = quantity;
        this.checked = checked;
        this.position = position;
    }

    @Ignore
    public ShoppingListItem(int ingredientID, double quantity, int position) {
        this(ingredientID, quantity, false, position);
    }

    public int getIngredientID() {
        return ingredientID;
    }

    public double getQuantity() {
        return quantity;
    }

    public boolean isChecked() {
        return checked;
    }

    public int getPosition() {
        return position;
    }
}
