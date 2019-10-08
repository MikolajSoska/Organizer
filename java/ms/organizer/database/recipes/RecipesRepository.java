package ms.organizer.database.recipes;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ms.organizer.database.OrganizerDatabase;
import ms.organizer.database.ingredients.IngredientWithQuantity;

public class RecipesRepository {
    private RecipesDAO recipesDAO;
    private LiveData<List<Recipe>> recipes;

    RecipesRepository(Application application) {
        OrganizerDatabase db = OrganizerDatabase.getDatabase(application);
        recipesDAO = db.recipesDAO();
        recipes = recipesDAO.getRecipes();
    }

    LiveData<List<Recipe>> getRecipes() {
        return recipes;
    }

    List<IngredientWithQuantity> getRecipeIngredients(int recipeID) throws ExecutionException, InterruptedException {
        return new GetIngredientsAsyncTask(recipesDAO).execute(recipeID).get();
    }

    void insert(Recipe recipe, ArrayList<IngredientWithQuantity> ingredients) {
        new InsertAsyncTask(recipesDAO).execute(recipe, ingredients);
    }

    void delete(Recipe recipe) {
        new DeleteAsyncTask(recipesDAO).execute(recipe);
    }


    private static class InsertAsyncTask extends AsyncTask<Object, Void, Void> {
        private RecipesDAO asyncTaskDAO;

        InsertAsyncTask(RecipesDAO dao) {
            asyncTaskDAO = dao;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Void doInBackground(Object... objects) {
            Recipe recipe = (Recipe) objects[0];
            ArrayList<IngredientWithQuantity> ingredients = (ArrayList<IngredientWithQuantity>) objects[1];

            asyncTaskDAO.insert(recipe);
            int ID = asyncTaskDAO.getRecipeID(recipe.getTitle());

            for (IngredientWithQuantity ingredient : ingredients)
                if (ingredient.getIngredientID() == 0)
                    asyncTaskDAO.insert(new IngredientInRecipe(ID, asyncTaskDAO.getIngredientIDByName(ingredient.getName()), ingredient.getQuantity()));
                else
                    asyncTaskDAO.insert(new IngredientInRecipe(ID, ingredient.getIngredientID(), ingredient.getQuantity()));

            return null;
        }
    }

    private static class DeleteAsyncTask extends AsyncTask<Recipe, Void, Void> {
        private RecipesDAO asyncTaskDAO;

        DeleteAsyncTask(RecipesDAO dao) {
            asyncTaskDAO = dao;
        }

        @Override
        protected Void doInBackground(Recipe... recipes) {
            asyncTaskDAO.deleteAllFromRecipe(recipes[0].getRecipeID());
            asyncTaskDAO.delete(recipes[0]);

            return null;
        }
    }

    private static class GetIngredientsAsyncTask extends AsyncTask<Integer, Void, List<IngredientWithQuantity>> {
        private RecipesDAO asyncTaskDAO;

        GetIngredientsAsyncTask(RecipesDAO dao) {
            asyncTaskDAO = dao;
        }

        @Override
        protected List<IngredientWithQuantity> doInBackground(Integer... integers) {
            return asyncTaskDAO.getRecipeIngredients(integers[0]);
        }
    }
}
