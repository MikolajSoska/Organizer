package ms.organizer.database.meals;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "meals")
public class Meal {

    @PrimaryKey(autoGenerate = true)
    private int mealID;

    @NonNull
    private String type;

    @NonNull
    private String date;

    private int ID;
    private double quantity;
    private boolean planned;

    public Meal(int mealID, @NonNull String type, @NonNull String date, int ID, double quantity, boolean planned) {
        this.mealID = mealID;
        this.type = type;
        this.date = date;
        this.ID = ID;
        this.quantity = quantity;
        this.planned = planned;
    }

    @Ignore
    public Meal(@NonNull String type, @NonNull String date, int ID, double quantity, boolean planned) {
        this.mealID = 0;
        this.type = type;
        this.date = date;
        this.ID = ID;
        this.quantity = quantity;
        this.planned = planned;
    }

    public int getMealID() {
        return mealID;
    }

    @NonNull
    public String getType() {
        return type;
    }

    @NonNull
    public String getDate() {
        return date;
    }

    public int getID() {
        return ID;
    }

    public double getQuantity() {
        return quantity;
    }

    public boolean isPlanned() {
        return planned;
    }

    public void setMealID(int mealID) {
        this.mealID = mealID;
    }

    public void setType(@NonNull String type) {
        this.type = type;
    }

    public void setDate(@NonNull String date) {
        this.date = date;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public void setPlanned(boolean planned) {
        this.planned = planned;
    }
}
