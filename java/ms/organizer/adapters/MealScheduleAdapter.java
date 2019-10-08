package ms.organizer.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import ms.organizer.R;
import ms.organizer.activities.MealSchedule;
import ms.organizer.database.ingredients.Ingredient;
import ms.organizer.database.meals.Meal;
import ms.organizer.database.recipes.Recipe;
import ms.organizer.utility.OrganizerUtility;

public class MealScheduleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private class RecipeItemViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout layout;
        private final TextView recipeTitle;
//        private final TextView recipeDetails;
        private final ImageView recipeImage;
        private final View divider;

        private RecipeItemViewHolder(View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.linear_layout);
            recipeTitle = itemView.findViewById(R.id.recipe_title);
//            recipeDetails = itemView.findViewById(R.id.recipe_details);
            recipeImage = itemView.findViewById(R.id.recipe_image);
            divider = itemView.findViewById(R.id.divider);
        }
    }

    private class IngredientItemViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout layout;
        private final TextView ingredientName;
        private final TextView ingredientQuantity;
        private final ImageView ingredientIcon;
        private final View divider;

        private IngredientItemViewHolder(View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.linear_layout);
            ingredientName = itemView.findViewById(R.id.ingredient_name);
            ingredientQuantity = itemView.findViewById(R.id.ingredient_quantity);
            ingredientIcon = itemView.findViewById(R.id.ingredient_icon);
            divider = itemView.findViewById(R.id.divider);
        }
    }

    private final Context context;
    private List<Meal> meals;

    private static final int RECIPE_ITEM = 0;
    private static final int INGREDIENT_ITEM = 1;

    public MealScheduleAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        if (meals.get(position).getQuantity() == 0)
            return RECIPE_ITEM;
        else
            return INGREDIENT_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == RECIPE_ITEM) {
            View itemView = inflater.inflate(R.layout.recipe_meal_item, parent, false);
            return new RecipeItemViewHolder(itemView);
        }
        else {
            View itemView = inflater.inflate(R.layout.ingredient_meal_item, parent, false);
            return  new IngredientItemViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (meals != null) {
            final Meal current = meals.get(position);
            MealSchedule mealSchedule = (MealSchedule) context;

            if (holder.getItemViewType() == RECIPE_ITEM) {
                Recipe recipe = mealSchedule.getRecipe(current.getID());
                if (recipe != null) {
                    final String title = recipe.getTitle();
                    RecipeItemViewHolder viewHolder = (RecipeItemViewHolder) holder;

                    viewHolder.recipeTitle.setText(title);
                    OrganizerUtility.setRecipeIcon(context, title, viewHolder.recipeImage);

                    showDivider(viewHolder.divider, position);

                    viewHolder.layout.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            menuDialog(current, title);
                            return true;
                        }
                    });

                    if (current.isPlanned())
                        viewHolder.layout.setAlpha(0.2f);
                    else
                        viewHolder.layout.setAlpha(1);
                }
            }

            else {
                Ingredient ingredient = mealSchedule.getIngredient(current.getID());
                if (ingredient != null) {
                    final String name = ingredient.getName();
                    IngredientItemViewHolder viewHolder = (IngredientItemViewHolder) holder;

                    viewHolder.ingredientName.setText(OrganizerUtility.formatIngredientName(name));
                    OrganizerUtility.setIngredientIcon(context, name, viewHolder.ingredientIcon, 60);

                    viewHolder.ingredientQuantity.setText(OrganizerUtility.formatQuantityWithUnit(current.getQuantity(), ingredient.getRecipeUnit()));

                    showDivider(viewHolder.divider, position);

                    viewHolder.layout.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            menuDialog(current, name);
                            return true;
                        }
                    });

                    if (current.isPlanned())
                        viewHolder.layout.setAlpha(0.2f);
                    else
                        viewHolder.layout.setAlpha(1);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        if (meals != null)
            return meals.size();
        else
            return 0;
    }

    public void setMeals(List<Meal> meals) {
        this.meals = meals;
        notifyDataSetChanged();
    }

    private void showDivider(View divider, int position) {
        if (position == meals.size() - 1)
            divider.setVisibility(View.INVISIBLE);
        else
            divider.setVisibility(View.VISIBLE);
    }

    private void menuDialog(final Meal meal, String name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(OrganizerUtility.formatIngredientName(name));
        if (meal.isPlanned()) {
            builder.setItems(new String[]{context.getString(R.string.add_meal), context.getString(R.string.delete)}, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (i == 0)
                        ((MealSchedule)context).addPlannedMeal(meal);
                    else
                        ((MealSchedule)context).deleteMeal(meal);
                }
            }).create().show();
        }
        else {
            builder.setItems(new String[]{context.getString(R.string.delete)}, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ((MealSchedule)context).deleteMeal(meal);
                }
            }).create().show();
        }
    }
}
