package ms.organizer.database.shoppinglist;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.Nullable;

import java.util.List;
import java.util.concurrent.ExecutionException;

import ms.organizer.database.fridge.FridgeItem;

public class ShoppingListViewModel extends AndroidViewModel {
    private ShoppingListRepository shoppingListRepository;
    private LiveData<List<ShoppingListIngredient>> ingredients;

    public ShoppingListViewModel(Application application) {
        super(application);
        shoppingListRepository = new ShoppingListRepository(application);
        ingredients = shoppingListRepository.getFromShoppingList();
    }

    public LiveData<List<ShoppingListIngredient>> getFromShoppingList() {
        return ingredients;
    }

    public void addToShoppingList(int ingredientID, double quantity, boolean checked) {
        shoppingListRepository.addToShoppingList(ingredientID, quantity, checked);
    }

    public void removeFromShoppingList(int ingredientID, double quantity, boolean checked) {
        shoppingListRepository.removeFromShoppingList(ingredientID, quantity, checked);
    }

    public void deleteCheckedIngredients() {
        shoppingListRepository.deleteCheckedItems();
    }

    public void setIngredientChecked(int ingredientID, boolean checked) {
        shoppingListRepository.setItemsChecked(ingredientID, checked);
    }

    public void changeItemPosition(final int ingredientID, final int position) {
        shoppingListRepository.changeItemPosition(ingredientID, position);
    }

    public boolean itemExistInShoppingList(int ingredientID) throws ExecutionException, InterruptedException {
        return shoppingListRepository.itemExistInShoppingList(ingredientID);
    }

    public List<ShoppingListItem> getShoppingListItems(int ingredientID) throws ExecutionException, InterruptedException {
        return shoppingListRepository.getShoppingListItems(ingredientID);
    }

    @Nullable
    public List<FridgeItem> getCheckedItems() {
        try {
            return shoppingListRepository.getCheckedItems();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public double getUnplannedIngredientQuantity(int ingredientID) throws ExecutionException, InterruptedException {
        return shoppingListRepository.getUnplannedIngredientQuantity(ingredientID);
    }

    public double getQuantityToModify(int ingredientID) {
        try {
            return shoppingListRepository.getQuantityToModify(ingredientID);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return 0.;
        }
    }

    public int getCheckedItemsSize() {
        try {
            return shoppingListRepository.getCheckedItemsSize();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
