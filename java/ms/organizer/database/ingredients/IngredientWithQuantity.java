package ms.organizer.database.ingredients;

import android.arch.persistence.room.Ignore;
import android.support.annotation.NonNull;

public class IngredientWithQuantity extends Ingredient {

    private double quantity;

    public IngredientWithQuantity(int ingredientID, @NonNull String name, @NonNull String recipeUnit, @NonNull String shopUnit, double shopUnitQuantity, double quantity) {
        super(ingredientID, name, recipeUnit, shopUnit, shopUnitQuantity);
        this.quantity = quantity;
    }

    @Ignore
    public IngredientWithQuantity(@NonNull String name, @NonNull String recipeUnit, @NonNull String shopUnit, double shopUnitQuantity, double quantity) {
        super(0, name, recipeUnit, shopUnit, shopUnitQuantity);
        this.quantity = quantity;
    }

    @Ignore
    public IngredientWithQuantity(@NonNull Ingredient ingredient) {
        super(ingredient.getIngredientID(), ingredient.getName(), ingredient.getRecipeUnit(), ingredient.getShopUnit(), ingredient.getShopUnitQuantity());
        this.quantity = 0;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }
}
