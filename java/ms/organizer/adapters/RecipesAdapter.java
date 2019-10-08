package ms.organizer.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ms.organizer.R;
import ms.organizer.activities.Fridge;
import ms.organizer.activities.MealSchedule;
import ms.organizer.activities.Recipes;
import ms.organizer.dialogs.RecipeLayoutDialog;
import ms.organizer.database.recipes.Recipe;
import ms.organizer.utility.OrganizerUtility;

public class RecipesAdapter extends RecyclerView.Adapter<RecipesAdapter.RecipesViewHolder> {
    class RecipesViewHolder extends RecyclerView.ViewHolder {
        private final CardView recipeCard;
        private final ImageView recipeImage;
        private final TextView recipeTitle;
        private final Button openRecipeButton;

        private RecipesViewHolder(View itemView) {
            super(itemView);
            recipeCard = itemView.findViewById(R.id.recipe_card);
            recipeImage = itemView.findViewById(R.id.recipe_item_image);
            recipeTitle = itemView.findViewById(R.id.recipe_item_title);
            openRecipeButton = itemView.findViewById(R.id.recipe_item_open_button);
        }
    }

    private final LayoutInflater inflater;
    private List<Recipe> recipes;

    public RecipesAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    @Override
    public RecipesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.recipes_item, parent, false);
        return new RecipesViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final RecipesViewHolder holder, final int position) {
        if (recipes != null) {
            final Context context = inflater.getContext();
            final Recipe current = recipes.get(position);

            final String recipeName = current.getTitle();
            holder.recipeTitle.setText(recipeName);

            OrganizerUtility.setRecipeIcon(context, recipeName, holder.recipeImage);


            holder.openRecipeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("id", current.getRecipeID());
                    bundle.putString("title", recipeName);
                    bundle.putString("description", current.getDescription());

                    RecipeLayoutDialog recipeLayoutDialog = new RecipeLayoutDialog();
                    recipeLayoutDialog.setArguments(bundle);
                    recipeLayoutDialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "RecipeLayoutDialog");
                }
            });

            holder.recipeCard.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(recipeName);
                    builder.setItems(new CharSequence[]{context.getString(R.string.delete)}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((Recipes) context).deleteRecipe(current);
                        }
                    });
                    builder.create().show();

                    return true;
                }
            });

            if (context.getClass().getName().equals(Fridge.class.getName())) {
                holder.recipeCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((Fridge)context).onRecipePicked(current);
                    }
                });
            }
            else if (context.getClass().getName().equals(MealSchedule.class.getName())) {
                holder.recipeCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MealSchedule)context).planMeal(current);
                    }
                });
            }
        }
    }

    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (recipes != null)
            return recipes.size();
        else
            return 0;
    }
}
