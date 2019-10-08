package ms.organizer.database.fridge;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import ms.organizer.database.OrganizerDatabase;
import ms.organizer.database.ingredients.IngredientWithQuantity;
import ms.organizer.database.shoppinglist.ShoppingListItem;

public class FridgeRepository {
    private FridgeDAO fridgeDAO;
    private LiveData<List<IngredientWithQuantity>> ingredients;
    private LiveData<List<IngredientWithQuantity>> plannedIngredients;

    FridgeRepository(Application application) {
        OrganizerDatabase db = OrganizerDatabase.getDatabase(application);
        fridgeDAO = db.fridgeDAO();
        ingredients = fridgeDAO.getFromFridge();
        plannedIngredients = fridgeDAO.getPlannedIngredients();
    }

    LiveData<List<IngredientWithQuantity>> getFromFridge() {
        return ingredients;
    }

    LiveData<List<IngredientWithQuantity>> getPlannedIngredients() {
        return plannedIngredients;
    }

    void delete(int ingredientID) {
        new DeleteFromFridgeAsyncTask(fridgeDAO).execute(new FridgeItem(ingredientID, 0));
    }

    void clearFridge() {
        new DeleteFromFridgeAsyncTask(fridgeDAO).execute();
    }

    void addToFridge(int ingredientID, double quantity) {
        new UpdateAsyncTask(fridgeDAO).execute(0.0, (double)ingredientID, quantity);
    }

    void updateQuantity(int ingredientID, double quantity) {
        new UpdateAsyncTask(fridgeDAO).execute(1.0, (double)ingredientID, quantity);
    }

    void updatePlannedQuantity(int ingredientID, int mealID, double newQuantity) {
        new UpdateAsyncTask(fridgeDAO).execute(2.0, (double) ingredientID, (double) mealID, newQuantity);
    }

    void updateQuantities(List<FridgeItem> items) {
        new UpdateAllAsyncTask(fridgeDAO).execute(items);
    }

    void addToFridge(final FridgeItem... items) {
        new AddToFridgeAsyncTask(fridgeDAO).execute(items.toArray(new FridgeItem[0]));
    }

    void insertFromShoppingList(List<ShoppingListItem> items,int ingredientID, double quantity) {
        new InsertFromShoppingListAsyncTask(fridgeDAO).execute(items, ingredientID, quantity);
    }

    void planIngredient(int ingredientID, int mealID, double plannedQuantity, double newFridgeQuantity) {
        planIngredient(ingredientID, mealID, plannedQuantity);
        if (newFridgeQuantity > 0)
            updateQuantity(ingredientID, newFridgeQuantity);
        else
            delete(ingredientID);
    }

    void planIngredient(int ingredientID, int mealID, double plannedQuantity) {
        new InsertToPlannedAsyncTask(fridgeDAO).execute((double) ingredientID, (double) mealID, plannedQuantity);
    }

    void cancelIngredient(int ingredientID, int mealID) {
        new DeleteFromPlannedAsyncTask(fridgeDAO).execute(new PlannedIngredient(ingredientID, mealID, 0));
    }

    void cancelIngredients(int mealID) {
        new CancelAsyncTask(fridgeDAO).execute(mealID);
    }

    double getItemQuantity(int ingredientID) throws ExecutionException, InterruptedException {
        return new GetAsyncTask(fridgeDAO).execute(0, ingredientID).get();
    }

    double getPlannedQuantity(int mealID, int ingredientId) throws ExecutionException, InterruptedException {
        return new GetAsyncTask(fridgeDAO).execute(1, mealID, ingredientId).get();
    }

    Double getPlannedIngredientQuantityInList(int ingredientID) throws ExecutionException, InterruptedException {
        return new GetAsyncTask(fridgeDAO).execute(2, ingredientID).get();
    }

    List<PlannedIngredient> getSpecifiedPlannedIngredients(int ingredientID, int mealID) throws ExecutionException, InterruptedException {
        return new GetListAsyncTask(fridgeDAO).execute(ingredientID, mealID).get();
    }

    List<PlannedIngredient> getPlannedMealsWithoutIngredient(int ingredientID) throws ExecutionException, InterruptedException {
        return new GetMealsWithoutIngredientAsyncTask(fridgeDAO).execute(ingredientID).get();
    }

    private static class DeleteFromFridgeAsyncTask extends AsyncTask<FridgeItem, Void, Void> {
        private FridgeDAO asyncTaskDAO;

        DeleteFromFridgeAsyncTask(FridgeDAO dao) {
            asyncTaskDAO = dao;
        }

        @Override
        protected Void doInBackground(FridgeItem... fridgeItems) {
            if (fridgeItems.length != 0)
                asyncTaskDAO.delete(fridgeItems[0]);
            else
                asyncTaskDAO.clearFridge();
            return null;
        }
    }

    private static class AddToFridgeAsyncTask extends AsyncTask<FridgeItem, Void, Void> {
        private FridgeDAO asyncTaskDAO;

        AddToFridgeAsyncTask(FridgeDAO dao) {
            asyncTaskDAO = dao;
        }

        @Override
        protected Void doInBackground(FridgeItem... items) {
            for (final FridgeItem item : items) {
                final int ingredientID = item.getIngredientID();
                final List<PlannedIngredient> plannedIngredients = asyncTaskDAO.getPlannedIngredients(ingredientID);
                double quantity = item.getQuantity();

                for (final PlannedIngredient ingredient : plannedIngredients) {
                    final int mealID = ingredient.getMealID();
                    final double quantityInList = ingredient.getQuantityInList();
                    if (quantityInList < quantity) {
                        asyncTaskDAO.addFromListToFridge(ingredientID, mealID, quantityInList, 0);
                        quantity -= quantityInList;
                    }
                    else {
                        asyncTaskDAO.addFromListToFridge(ingredientID, mealID, quantity, quantityInList - quantity);
                        quantity = 0;
                        break;
                    }
                }

                //todo sprawdzic pozniej czy get fridge item mozna na boolean
                if (quantity > 0) {
                    if (asyncTaskDAO.getFridgeItem(ingredientID) != null)
                        asyncTaskDAO.addToFridge(ingredientID, quantity);
                    else
                        asyncTaskDAO.insert(new FridgeItem(ingredientID, quantity));
                }
            }
            //todo
            //dodawanie do lodowki zrobic tak zeby tworzylo shoppinglistitemy i dodawalo przez tą funkcje
            //wtedy wszystko bedzie w jednym miejscu i bedzie sprawdzac od razu zaplanowane. mozna nazwe zmienic na koniec czy cos

            return null;
        }
    }


    private static class DeleteFromPlannedAsyncTask extends AsyncTask<PlannedIngredient, Void, Void> {
        private FridgeDAO asyncTaskDAO;

        DeleteFromPlannedAsyncTask(FridgeDAO dao) {
            asyncTaskDAO = dao;
        }

        @Override
        protected Void doInBackground(PlannedIngredient... plannedIngredients) {
            asyncTaskDAO.delete(plannedIngredients[0]);
            return null;
        }
    }

    private static class CancelAsyncTask extends AsyncTask<Integer, Void, Void> {
        private FridgeDAO asyncTaskDAO;

        CancelAsyncTask(FridgeDAO dao) {
            this.asyncTaskDAO = dao;
        }

        @Override
        protected Void doInBackground(Integer... integers) {
            int mealID = integers[0];
            List<PlannedIngredient> ingredients = asyncTaskDAO.getMealPlannedIngredients(mealID);
            if (ingredients != null) {
                for (PlannedIngredient ingredient : ingredients) {
                    int ingredientID = ingredient.getIngredientID();
                    if (asyncTaskDAO.getFridgeItem(ingredientID) != null)
                        asyncTaskDAO.addToFridge(ingredientID, ingredient.getQuantity());
                    else
                        asyncTaskDAO.insert(new FridgeItem(ingredientID, ingredient.getQuantity()));
                }
                asyncTaskDAO.deletePlannedIngredients(mealID);
            }

            return null;
        }
    }

    private static class UpdateAsyncTask extends AsyncTask<Double, Void, Void> {
        private FridgeDAO asyncTaskDAO;

        UpdateAsyncTask(FridgeDAO dao) {
            asyncTaskDAO = dao;
        }

        @Override
        protected Void doInBackground(Double... doubles) {
            int ingredientID = doubles[1].intValue();
            if (doubles[0] == 0.0) {
                if (asyncTaskDAO.getFridgeItem(ingredientID) != null)
                    asyncTaskDAO.addToFridge(ingredientID, doubles[2]);
                else
                    asyncTaskDAO.insert(new FridgeItem(ingredientID, doubles[2]));
            }
            else if (doubles[0] == 1.0)
                asyncTaskDAO.updateQuantity(ingredientID, doubles[2]);
            else
                asyncTaskDAO.updatePlannedQuantity(ingredientID, doubles[2].intValue(), doubles[3]);

            return null;
        }
    }

    private static class UpdateAllAsyncTask extends AsyncTask<List<FridgeItem>, Void, Void> {
        private FridgeDAO asyncTaskDAO;

        UpdateAllAsyncTask(FridgeDAO dao) {
            asyncTaskDAO = dao;
        }

        @SafeVarargs
        @Override
        protected final Void doInBackground(List<FridgeItem>... items) {
            for (FridgeItem item : items[0]) {
                if (item.getQuantity() > 0)
                    asyncTaskDAO.updateQuantity(item);
                else
                    asyncTaskDAO.delete(item);
            }

            return null;
        }
    }

    private static class GetAsyncTask extends AsyncTask<Integer, Void, Double> {
        private FridgeDAO asyncTaskDAO;

        GetAsyncTask(FridgeDAO dao) {
            asyncTaskDAO = dao;
        }

        @Override
        protected Double doInBackground(Integer... integers) {
            if (integers[0] == 0) {
                FridgeItem item = asyncTaskDAO.getFridgeItem(integers[1]);
                if (item == null)
                    return 0.;
                else
                    return item.getQuantity();
            }
            else if (integers[0] == 1) {
                PlannedIngredient item = asyncTaskDAO.getPlannedIngredient(integers[1], integers[2]);
                if (item == null)
                    return 0.;
                else
                    return item.getQuantity();
            }
            else
                return asyncTaskDAO.getPlannedIngredientQuantityInList(integers[1]);
        }
    }

    private static class GetListAsyncTask extends AsyncTask<Integer, Void, List<PlannedIngredient>> {
        private FridgeDAO asyncTaskDAO;

        GetListAsyncTask(FridgeDAO dao) {
            asyncTaskDAO = dao;
        }

        @Override
        protected List<PlannedIngredient> doInBackground(Integer... integers) {
            return asyncTaskDAO.getSpecifiedPlannedIngredients(integers[0], integers[1]);
        }
    }

    private static class GetMealsWithoutIngredientAsyncTask extends AsyncTask<Integer, Void, List<PlannedIngredient>> {
        private FridgeDAO asyncTaskDAO;

        GetMealsWithoutIngredientAsyncTask(FridgeDAO dao) {
            asyncTaskDAO = dao;
        }

        @Override
        protected List<PlannedIngredient> doInBackground(Integer... integers) {
            int ingredientID = integers[0];
            List<PlannedIngredient> meals = asyncTaskDAO.getPlannedMealsWithSpecifiedIngredient(ingredientID);
            meals.addAll(asyncTaskDAO.getPlannedRecipesWithSpecifiedIngredient(ingredientID));

            ArrayList<PlannedIngredient> mealsWithoutIngredient = new ArrayList<>();
            for (PlannedIngredient ingredient : meals) {
                PlannedIngredient plannedIngredient = asyncTaskDAO.getPlannedIngredient(ingredient.getMealID(), ingredientID);
                if (plannedIngredient != null) {
                    double plannedQuantity = plannedIngredient.getQuantity();
                    double neededQuantity = ingredient.getQuantity();
                    if (plannedQuantity != neededQuantity) {
                        ingredient.setQuantity(neededQuantity - plannedQuantity);
                    }
                    else
                        continue;
                }
                mealsWithoutIngredient.add(ingredient);
            }

            mealsWithoutIngredient.sort(new Comparator<PlannedIngredient>() {
                @Override
                public int compare(PlannedIngredient o1, PlannedIngredient o2) {
                    return o1.getMealID() - o2.getMealID();
                }
            });

            return mealsWithoutIngredient;
        }
    }
}
