package ms.organizer.database.recipes;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.io.Serializable;

@Entity(tableName = "recipes", indices = {@Index(value = {"title"}, unique = true)})
public class Recipe implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int recipeID;

    @NonNull
    private String title;

    @NonNull
    private String description;

    public Recipe(int recipeID, @NonNull String title, @NonNull String description) {
        this.recipeID = recipeID;
        this.title = title;
        this.description = description;
    }

    @Ignore
    public Recipe(@NonNull String title, @NonNull String description) {
        this.recipeID = 0;
        this.title = title;
        this.description = description;
    }

    public int getRecipeID() {
        return recipeID;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    @NonNull
    public String getDescription() {
        return description;
    }

    public void setRecipeID(int recipeID) {
        this.recipeID = recipeID;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    public void setDescription(@NonNull String description) {
        this.description = description;
    }
}
