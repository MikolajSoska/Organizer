package ms.organizer.database.shoppinglist;

import android.support.annotation.NonNull;

import ms.organizer.database.fridge.FridgeItem;
import ms.organizer.database.ingredients.IngredientWithQuantity;

public class ShoppingListIngredient extends IngredientWithQuantity {
    private final boolean checked;

    public ShoppingListIngredient(int ingredientID, @NonNull String name, @NonNull String shopUnit, double shopUnitQuantity, double quantity, boolean checked) {
        super(ingredientID, name, "", shopUnit, shopUnitQuantity, quantity);
        this.checked = checked;
    }

    public FridgeItem toFridgeItem() {
        final int ingredientID = super.getIngredientID();
        final double quantity = super.getQuantity() * super.getShopUnitQuantity();

        return new FridgeItem(ingredientID, quantity);
    }

    public boolean isChecked() {
        return checked;
    }
}
