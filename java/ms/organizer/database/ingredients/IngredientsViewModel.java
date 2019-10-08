package ms.organizer.database.ingredients;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

public class IngredientsViewModel extends AndroidViewModel {
    private IngredientsRepository ingredientsRepository;
    private LiveData<List<Ingredient>> ingredients;

    public IngredientsViewModel(Application application) {
        super(application);
        ingredientsRepository = new IngredientsRepository(application);
        ingredients = ingredientsRepository.getIngredients();
    }

    public LiveData<List<Ingredient>> getIngredients() {
        return ingredients;
    }

    public void insert(Ingredient ingredient) {
        ingredientsRepository.insert(ingredient);
    }

    public void delete(Ingredient ingredient) {
        ingredientsRepository.delete(ingredient);
    }

    public void update(Ingredient ingredient) {
        ingredientsRepository.update(ingredient);
    }
}
