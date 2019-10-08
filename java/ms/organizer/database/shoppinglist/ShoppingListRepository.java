package ms.organizer.database.shoppinglist;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;
import java.util.concurrent.ExecutionException;

import ms.organizer.database.OrganizerDatabase;
import ms.organizer.database.fridge.FridgeItem;

class ShoppingListRepository {
    private ShoppingListDAO shoppingListDAO;
    private LiveData<List<ShoppingListIngredient>> ingredients;

    ShoppingListRepository(Application application) {
        OrganizerDatabase db = OrganizerDatabase.getDatabase(application);
        shoppingListDAO = db.shoppingListDAO();
        ingredients = shoppingListDAO.getFromShoppingList();
    }

    LiveData<List<ShoppingListIngredient>> getFromShoppingList() {
        return ingredients;
    }


    void addToShoppingList(int ingredientID, double quantity, boolean checked) {
        new AddAsyncTask(shoppingListDAO).execute((double) ingredientID, quantity, checked ? 1. : 0.);
    }

    void removeFromShoppingList(int ingredientID, double quantity, boolean checked) {
        new RemoveAsyncTask(shoppingListDAO).execute((double) ingredientID, quantity, checked ? 1. : 0.);
    }

    void deleteCheckedItems() {
        new DeleteCheckedAsyncTask(shoppingListDAO).execute();
    }

    void setItemsChecked(int ingredientID, boolean checked) {
        new SetAsyncTask(shoppingListDAO).execute(0, ingredientID, checked ? 1 : 0);
    }

    void changeItemPosition(final int ingredientID, final int position) {
        new SetAsyncTask(shoppingListDAO).execute(1, ingredientID, position);
    }

    boolean itemExistInShoppingList(int ingredientID) throws ExecutionException, InterruptedException {
        return new ExistsAsyncTask(shoppingListDAO).execute(ingredientID).get();
    }

    List<ShoppingListItem> getShoppingListItems(int ingredientID) throws ExecutionException, InterruptedException {
        return new GetListAsyncTask(shoppingListDAO).execute(0, ingredientID).get();
    }

    List<FridgeItem> getCheckedItems() throws ExecutionException, InterruptedException {
        return new GetListAsyncTask(shoppingListDAO).execute(1).get();
    }

    double getUnplannedIngredientQuantity(int ingredientID) throws ExecutionException, InterruptedException {
        return new GetAsyncTask(shoppingListDAO).execute(0, ingredientID).get();
    }

    double getQuantityToModify(int ingredientID) throws ExecutionException, InterruptedException {
        return new GetAsyncTask(shoppingListDAO).execute(1, ingredientID).get();
    }

    int getCheckedItemsSize() throws ExecutionException, InterruptedException {
        return new GetAsyncTask(shoppingListDAO).execute(2).get().intValue();
    }

    private static class AddAsyncTask extends AsyncTask<Double, Void, Void> {
        private ShoppingListDAO asyncTaskDAO;

        AddAsyncTask(ShoppingListDAO dao) {
            asyncTaskDAO = dao;
        }

        @Override
        protected Void doInBackground(Double... doubles) {
            final int ingredientID = doubles[0].intValue();
            final double quantity = doubles[1];
            final boolean checked = doubles[2] > 0;

            if (asyncTaskDAO.getItemQuantity(ingredientID, checked) != null)
                asyncTaskDAO.addToShoppingList(ingredientID, quantity, checked);
            else
                asyncTaskDAO.insert(new ShoppingListItem(ingredientID, quantity, checked, asyncTaskDAO.getNewPosition(false)));

            return null;
        }
    }

    private static class RemoveAsyncTask extends AsyncTask<Double, Void, Void> {
        private ShoppingListDAO asyncTaskDAO;

        RemoveAsyncTask(ShoppingListDAO dao) {
            asyncTaskDAO = dao;
        }

        @Override
        protected Void doInBackground(Double... doubles) {
            final int ingredientID = doubles[0].intValue();
            final double itemQuantity = asyncTaskDAO.getItemQuantity(ingredientID, false);
            double quantityToDelete = doubles[1];

            if (itemQuantity > quantityToDelete)
                asyncTaskDAO.removeFromShoppingList(ingredientID, quantityToDelete, false);
            else {
                asyncTaskDAO.delete(ingredientID, false);
                final double checkedQuantity = asyncTaskDAO.getItemQuantity(ingredientID, true);
                quantityToDelete -= itemQuantity;
                if (checkedQuantity - quantityToDelete > 0)
                    asyncTaskDAO.removeFromShoppingList(ingredientID, quantityToDelete, true);
                else
                    asyncTaskDAO.delete(ingredientID, true);

            }

            return null;
        }
    }

    private static class DeleteCheckedAsyncTask extends AsyncTask<Void, Void, Void> {
        private ShoppingListDAO asyncTaskDAO;

        DeleteCheckedAsyncTask(ShoppingListDAO dao) {
            asyncTaskDAO = dao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            asyncTaskDAO.deleteCheckedItems();

            return null;
        }
    }

    private static class SetAsyncTask extends AsyncTask<Integer, Void, Void> {
        private ShoppingListDAO asyncTaskDAO;

        SetAsyncTask(ShoppingListDAO dao) {
            asyncTaskDAO = dao;
        }

        @Override
        protected Void doInBackground(Integer... integers) {
            final int which = integers[0];
            final int ingredientID = integers[1];

            if (which == 0) {
                final boolean checked = integers[2] > 0;
                final int newPosition = asyncTaskDAO.getNewPosition(checked);
                asyncTaskDAO.setItemChecked(ingredientID, checked, newPosition);
            } else {
                final int newPosition = integers[2];
                asyncTaskDAO.changeItemPosition(ingredientID, newPosition);
            }

            return null;
        }
    }

    //todo sprawdzic te ponizej
    private static class ExistsAsyncTask extends AsyncTask<Integer, Void, Boolean> {
        private ShoppingListDAO asyncTaskDAO;

        ExistsAsyncTask(ShoppingListDAO dao) {
            asyncTaskDAO = dao;
        }

        @Override
        protected Boolean doInBackground(Integer... integers) {
            return asyncTaskDAO.itemExistInShoppingList(integers[0]);
        }
    }

    private static class GetListAsyncTask extends AsyncTask<Integer, Void, List<ShoppingListItem>> {
        private ShoppingListDAO asyncTaskDAO;

        GetListAsyncTask(ShoppingListDAO dao) {
            asyncTaskDAO = dao;
        }

        @Override
        protected List<ShoppingListItem> doInBackground(Integer... integers) {
            final int which = integers[0];
            if (which == 0)
                return asyncTaskDAO.getShoppingListItems(integers[1]);
            else
                return asyncTaskDAO.getCheckedItems();
        }
    }

    private static class GetAsyncTask extends AsyncTask<Integer, Void, Double> {
        private ShoppingListDAO asyncTaskDAO;

        GetAsyncTask(ShoppingListDAO dao) {
            asyncTaskDAO = dao;
        }

        @Override
        protected Double doInBackground(Integer... integers) {
            final int which = integers[0];
            final int ingredientID = integers[1];

            if (which == 0) {
                ShoppingListItem item = asyncTaskDAO.getShoppingListItem(ingredientID, null);
                if (item != null)
                    return item.getQuantity();
                else
                    return 0.;
            } else if (which == 1) {
                final Double quantityToDelete = asyncTaskDAO.getQuantityToModify(ingredientID);
                final Double itemQuantity = asyncTaskDAO.getItemQuantity(ingredientID, false);
                if (quantityToDelete != null) {
                    return Math.min(quantityToDelete + asyncTaskDAO.getItemQuantity(ingredientID, true), itemQuantity);
                } else
                    return itemQuantity;
            } else {
                return (double) asyncTaskDAO.getCheckedItemsSize();
            }
        }
    }
}
