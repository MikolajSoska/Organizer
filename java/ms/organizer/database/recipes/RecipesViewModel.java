package ms.organizer.database.recipes;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ms.organizer.database.ingredients.IngredientWithQuantity;

public class RecipesViewModel extends AndroidViewModel {
    private RecipesRepository recipesRepository;
    private LiveData<List<Recipe>> recipes;

    public RecipesViewModel(Application application) {
        super(application);
        recipesRepository = new RecipesRepository(application);
        recipes = recipesRepository.getRecipes();
    }

    public LiveData<List<Recipe>> getRecipes() {
        return recipes;
    }

    public List<IngredientWithQuantity> getRecipeIngredients(int recipeID) throws ExecutionException, InterruptedException {
        return recipesRepository.getRecipeIngredients(recipeID);
    }

    public void insert(Recipe recipe, ArrayList<IngredientWithQuantity> ingredients) {
        recipesRepository.insert(recipe, ingredients);
    }

    public void delete(Recipe recipe) {
        recipesRepository.delete(recipe);
    }
}
