package ms.organizer.database.ingredients;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.io.Serializable;

@Entity(tableName = "ingredients", indices = {@Index(value = {"name"}, unique = true)})
public class Ingredient implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int ingredientID;

    @NonNull
    private String name;

    @NonNull
    private String recipeUnit;

    @NonNull
    private String shopUnit;

    private double shopUnitQuantity;

    public Ingredient(int ingredientID, @NonNull String name, @NonNull String recipeUnit, @NonNull String shopUnit, double shopUnitQuantity) {
        this.ingredientID = ingredientID;
        this.name = name;
        this.recipeUnit = recipeUnit;
        this.shopUnit = shopUnit;
        this.shopUnitQuantity = shopUnitQuantity;
    }

    public int getIngredientID() {
        return ingredientID;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getRecipeUnit() {
        return recipeUnit;
    }

    @NonNull
    public String getShopUnit() {
        return shopUnit;
    }

    public double getShopUnitQuantity() {
        return shopUnitQuantity;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass() == obj.getClass())
            return ingredientID == ((Ingredient)obj).getIngredientID();
        else
            return false;
    }
}
