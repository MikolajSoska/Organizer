package ms.organizer.database.meals;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutionException;

import ms.organizer.database.ingredients.Ingredient;
import ms.organizer.database.recipes.Recipe;

public class MealsViewModel extends AndroidViewModel {
    private MealsRepository mealsRepository;

    public MealsViewModel(Application application) {
        super(application);
        mealsRepository = new MealsRepository(application);
    }

    public LiveData<List<Meal>> getMeals(String date, String type) {
        return mealsRepository.getMeals(date, type);
    }

    public Recipe getRecipe(int recipeID) throws ExecutionException, InterruptedException {
        return mealsRepository.getRecipe(recipeID);
    }

    public Ingredient getIngredient(int ingredientID) throws ExecutionException, InterruptedException {
        return mealsRepository.getIngredient(ingredientID);
    }

    public void insert(Meal meal) {
        mealsRepository.insert(meal);
    }

    public int insertAndGetID(Meal meal) throws ExecutionException, InterruptedException {
        return mealsRepository.insertAndGetID(meal);
    }

    public void delete(Meal meal) {
        mealsRepository.delete(meal);
    }

    public void addPlannedMeal(int mealId) {
        mealsRepository.addPlannedMeal(mealId);
    }
}
