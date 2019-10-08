package ms.organizer.database.shoppinglist;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.support.annotation.Nullable;

import java.util.List;

import ms.organizer.database.fridge.FridgeItem;
import ms.organizer.database.ingredients.IngredientWithQuantity;

@Dao
public interface ShoppingListDAO {

    @Insert
    void insert(ShoppingListItem item);

    @Query("DELETE FROM shopping_list WHERE ingredientID = :ingredientID AND checked = :checked")
    void delete(int ingredientID, boolean checked);

    @Query("DELETE FROM shopping_list WHERE checked = 1")
    void deleteCheckedItems();

    @Query("UPDATE shopping_list SET quantity = quantity + :quantity WHERE ingredientID = :ingredientID AND checked = :checked")
    void addToShoppingList(int ingredientID, double quantity, boolean checked);

    @Query("UPDATE shopping_list SET quantity = quantity - :quantity WHERE ingredientID = :ingredientID AND checked = :checked")
    void removeFromShoppingList(int ingredientID, double quantity, boolean checked);

    @Query("UPDATE shopping_list SET checked = :checked AND position = :newPosition WHERE ingredientID = :ingredientID")
    void setItemChecked(int ingredientID, boolean checked, int newPosition);

    @Query("UPDATE shopping_list SET position = :position WHERE ingredientID = :ingredientID")
    void changeItemPosition(final int ingredientID, final int position);

    @Query("SELECT ingredientID, name, shopUnit, shopUnitQuantity, quantity, checked, position " +
            "FROM shopping_list NATURAL JOIN ingredients ORDER BY checked, position")
    LiveData<List<ShoppingListIngredient>> getFromShoppingList();

    @Query("SELECT ingredientID, quantity *  FROM shopping_list NATURAL WHERE checked = 1")
    List<FridgeItem> getCheckedItems();

    @Query("SELECT * FROM shopping_list WHERE ingredientID = :ingredientID AND mealID IS :mealID")
    ShoppingListItem getShoppingListItem(int ingredientID, @Nullable Integer mealID);

    @Query("SELECT quantity FROM shopping_list WHERE ingredientID = :ingredientID AND checked = :checked")
    Double getItemQuantity(int ingredientID, boolean checked);

    @Query("SELECT quantity - SUM(quantityInList) FROM shopping_list NATURAL JOIN planned_ingredients WHERE ingredientID = :ingredientID AND checked = 0")
    Double getQuantityToModify(int ingredientID);

    @Query("SELECT MAX(position) + 1 FROM shopping_list WHERE checked = :checked")
    int getNewPosition(final boolean checked);

    @Query("SELECT COUNT(*) FROM shopping_list WHERE checked = 1")
    int getCheckedItemsSize();

    @Query("SELECT 1 WHERE EXISTS (SELECT * FROM shopping_list WHERE ingredientID = :ingredientID)")
    boolean itemExistInShoppingList(int ingredientID);

    @Query("SELECT * FROM shopping_list WHERE ingredientID = :ingredientID AND mealID IS NOT NULL")
    List<ShoppingListItem> getShoppingListItems(int ingredientID);
}
