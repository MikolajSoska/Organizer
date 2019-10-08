package ms.organizer.database.meals;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;
import java.util.concurrent.ExecutionException;

import ms.organizer.database.OrganizerDatabase;
import ms.organizer.database.ingredients.Ingredient;
import ms.organizer.database.recipes.Recipe;

public class MealsRepository {
    private MealsDAO mealsDAO;

    MealsRepository(Application application) {
        OrganizerDatabase db = OrganizerDatabase.getDatabase(application);
        mealsDAO = db.mealsDAO();
    }

    LiveData<List<Meal>> getMeals(String date, String type) {
        return mealsDAO.getMeals(date, type);
    }

    void insert(Meal meal) {
        new InsertAsyncTask(mealsDAO).execute(meal);
    }

    int insertAndGetID(Meal meal) throws ExecutionException, InterruptedException {
        return new InsertAsyncTask(mealsDAO).execute(meal).get();
    }

    void delete(Meal meal) {
        new DeleteAsyncTask(mealsDAO).execute(meal);
    }

    Recipe getRecipe(int recipeID) throws ExecutionException, InterruptedException {
        return new GetRecipeAsyncTask(mealsDAO).execute(recipeID).get();
    }

    Ingredient getIngredient(int ingredientID) throws ExecutionException, InterruptedException {
        return new GetIngredientAsyncTask(mealsDAO).execute(ingredientID).get();
    }

    void addPlannedMeal(int mealId) {
        new UpdateAsyncTask(mealsDAO).execute(mealId);
    }

    private static class InsertAsyncTask extends AsyncTask<Meal, Void, Integer> {
        private MealsDAO asyncTaskDAO;

        InsertAsyncTask(MealsDAO dao) {
            asyncTaskDAO = dao;
        }

        @Override
        protected Integer doInBackground(Meal... meals) {
            Meal meal = meals[0];
            String type = meal.getType();
            String date = meal.getDate();
            int ID = meal.getID();
            boolean planned = meal.isPlanned();

            Meal mealExists = asyncTaskDAO.mealExists(type, date, ID, planned);
            if (mealExists != null && meal.getQuantity() > 0) {
                asyncTaskDAO.updateQuantity(mealExists.getMealID(), meal.getQuantity());
                return mealExists.getMealID();
            }
            else
                return (int) asyncTaskDAO.insert(meal);
        }
    }

    private static class DeleteAsyncTask extends AsyncTask<Meal, Void, Void> {
        private MealsDAO asyncTaskDAO;

        DeleteAsyncTask(MealsDAO dao) {
            asyncTaskDAO = dao;
        }

        @Override
        protected Void doInBackground(Meal... meals) {
            asyncTaskDAO.delete(meals[0]);
            return null;
        }
    }

    private static class GetRecipeAsyncTask extends AsyncTask<Integer, Void, Recipe> {
        private MealsDAO asyncTaskDAO;

        GetRecipeAsyncTask(MealsDAO dao) {
            asyncTaskDAO = dao;
        }

        @Override
        protected Recipe doInBackground(Integer... integers) {
            return asyncTaskDAO.getRecipe(integers[0]);
        }
    }

    private static class GetIngredientAsyncTask extends AsyncTask<Integer, Void, Ingredient> {
        private MealsDAO asyncTaskDAO;

        GetIngredientAsyncTask(MealsDAO dao) {
            asyncTaskDAO = dao;
        }

        @Override
        protected Ingredient doInBackground(Integer... integers) {
            return asyncTaskDAO.getIngredient(integers[0]);
        }
    }

    private static class UpdateAsyncTask extends AsyncTask<Integer, Void, Void> {
        private MealsDAO asyncTaskDao;

        UpdateAsyncTask(MealsDAO dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Integer... integers) {
            asyncTaskDao.addPlannedMeal(integers[0]);

            return null;
        }
    }
}
