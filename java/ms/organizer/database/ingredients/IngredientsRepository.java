package ms.organizer.database.ingredients;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

import ms.organizer.database.OrganizerDatabase;

public class IngredientsRepository {
    private IngredientsDAO ingredientsDAO;
    private LiveData<List<Ingredient>> ingredients;

    IngredientsRepository(Application application) {
        OrganizerDatabase db = OrganizerDatabase.getDatabase(application);
        ingredientsDAO = db.ingredientsDAO();
        ingredients = ingredientsDAO.getIngredients();
    }

    LiveData<List<Ingredient>> getIngredients() {
        return ingredients;
    }

    void insert(Ingredient ingredient) {
        new InsertAsyncTask(ingredientsDAO).execute(ingredient);
    }

    void delete(Ingredient ingredient) {
        new DeleteAsyncTask(ingredientsDAO).execute(ingredient);
    }

    void update(Ingredient ingredient) {
        new UpdateAsyncTask(ingredientsDAO).execute(ingredient);
    }

    private static class InsertAsyncTask extends AsyncTask<Ingredient, Void, Void> {
        private IngredientsDAO asyncTaskDAO;

        InsertAsyncTask(IngredientsDAO dao) {
            asyncTaskDAO = dao;
        }

        @Override
        protected Void doInBackground(Ingredient... ingredients) {
            asyncTaskDAO.insert(ingredients[0]);
            return null;
        }
    }

    private static class DeleteAsyncTask extends AsyncTask<Ingredient, Void, Void> {
        private IngredientsDAO asyncTaskDAO;

        DeleteAsyncTask(IngredientsDAO dao) {
            asyncTaskDAO = dao;
        }

        @Override
        protected Void doInBackground(Ingredient... ingredients) {
            asyncTaskDAO.delete(ingredients[0]);
            return null;
        }
    }

    private static class UpdateAsyncTask extends AsyncTask<Ingredient, Void, Void> {
        private IngredientsDAO asyncTaskDAO;

        UpdateAsyncTask(IngredientsDAO dao) {
            asyncTaskDAO = dao;
        }

        @Override
        protected Void doInBackground(Ingredient... ingredients) {
            asyncTaskDAO.update(ingredients[0]);
            return null;
        }
    }
}
