package ms.organizer.database.ingredients;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface IngredientsDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Ingredient ingredient);

    @Delete
    void delete(Ingredient ingredient);

    @Update
    void update(Ingredient ingredient);

    @Query("SELECT * FROM ingredients ORDER BY name ASC")
    LiveData<List<Ingredient>> getIngredients();
}
