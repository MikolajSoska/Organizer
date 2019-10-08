package ms.organizer.database.fridge;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutionException;

import ms.organizer.database.ingredients.IngredientWithQuantity;
import ms.organizer.database.shoppinglist.ShoppingListItem;

public class FridgeViewModel extends AndroidViewModel {
    private FridgeRepository fridgeRepository;
    private LiveData<List<IngredientWithQuantity>> ingredients;
    private LiveData<List<IngredientWithQuantity>> plannedIngredients;

    public FridgeViewModel(Application application) {
        super(application);
        fridgeRepository = new FridgeRepository(application);
        ingredients = fridgeRepository.getFromFridge();
        plannedIngredients = fridgeRepository.getPlannedIngredients();
    }

    public LiveData<List<IngredientWithQuantity>> getFromFridge() {
        return ingredients;
    }

    public LiveData<List<IngredientWithQuantity>> getPlannedIngredients() {
        return plannedIngredients;
    }

    public void delete(int ingredientID) {
        fridgeRepository.delete(ingredientID);
    }

    public void clearFridge() {
        fridgeRepository.clearFridge();
    }

    public double getItemQuantity(int ingredientID) throws ExecutionException, InterruptedException {
        return fridgeRepository.getItemQuantity(ingredientID);
    }

    public double getPlannedQuantity(int mealID, int ingredientID) throws ExecutionException, InterruptedException {
        return fridgeRepository.getPlannedQuantity(mealID, ingredientID);
    }

    public Double getPlannedIngredientQuantityInList(int ingredientID) {
        try {
            return fridgeRepository.getPlannedIngredientQuantityInList(ingredientID);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<PlannedIngredient> getSpecifiedPlannedIngredients(int ingredientID, int mealID) throws ExecutionException, InterruptedException {
        return fridgeRepository.getSpecifiedPlannedIngredients(ingredientID, mealID);
    }

    public List<PlannedIngredient> getPlannedMealsWithoutIngredient(int ingredientID) throws ExecutionException, InterruptedException {
        return fridgeRepository.getPlannedMealsWithoutIngredient(ingredientID);
    }

    public void addToFridge(int ingredientID, double quantity) {
        fridgeRepository.addToFridge(ingredientID, quantity);
    }

    public void updateQuantity(int ingredientID, double quantity) {
        fridgeRepository.updateQuantity(ingredientID, quantity);
    }

    public void updatePlannedQuantity(int ingredientID, int mealID, double newQuantity) {
        fridgeRepository.updatePlannedQuantity(ingredientID, mealID, newQuantity);
    }

    public void updateQuantities(List<FridgeItem> items) {
        fridgeRepository.updateQuantities(items);
    }

    public void addToFridge(final FridgeItem... items) {
        fridgeRepository.addToFridge(items);
    }

    public void insertFromShoppingList(List<ShoppingListItem> items, int ingredientID, double quantity) {
        fridgeRepository.insertFromShoppingList(items, ingredientID, quantity);
    }

    public void planIngredient(int ingredientID, int mealID, double plannedQuantity, double newFridgeQuantity) {
        fridgeRepository.planIngredient(ingredientID, mealID, plannedQuantity, newFridgeQuantity);
    }

    public void planIngredient(int ingredientID, int mealID, double plannedQuantity) {
        fridgeRepository.planIngredient(ingredientID, mealID, plannedQuantity);
    }

    public void cancelIngredient(int ingredientID, int mealID) {
        fridgeRepository.cancelIngredient(ingredientID, mealID);
    }

    public void cancelIngredients(int mealID) {
        fridgeRepository.cancelIngredients(mealID);
    }
}
