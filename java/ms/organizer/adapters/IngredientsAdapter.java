package ms.organizer.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import ms.organizer.R;
import ms.organizer.database.ingredients.Ingredient;
import ms.organizer.database.ingredients.IngredientWithQuantity;
import ms.organizer.dialogs.ChooseIngredientDialog;
import ms.organizer.dialogs.IngredientMenuDialog;
import ms.organizer.utility.OrganizerUtility;

/**
 * Created by Miki on 2018-09-08.
 */

public class IngredientsAdapter extends RecyclerView.Adapter<IngredientsAdapter.IngredientsViewHolder> {
    class IngredientsViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout ingredientView;
        private final TextView ingredientName;
        private final ImageView ingredientIcon;

        private IngredientsViewHolder(View itemView) {
            super(itemView);
            ingredientView = itemView.findViewById(R.id.ingredient_view);
            ingredientName = itemView.findViewById(R.id.ingredient_name);
            ingredientIcon = itemView.findViewById(R.id.ingredient_icon);
        }
    }

    private final LayoutInflater inflater;
    private List<? extends Ingredient> ingredients;
    private boolean isDialog;


    public IngredientsAdapter(Context context, boolean isDialog) {
        this.isDialog = isDialog;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public IngredientsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.ingredients_item, parent, false);
        return new IngredientsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final IngredientsViewHolder holder, int position) {
        if (ingredients != null) {
            final Context context = inflater.getContext();
            final Ingredient current = ingredients.get(position);

            OrganizerUtility.setIngredientIcon(context, current.getName(), holder.ingredientIcon, 60);
            holder.ingredientName.setText(current.getName());
            if (isDialog) {
                holder.ingredientView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((ChooseIngredientDialog.ChooseIngredientDialogListener)context).onIngredientPicked(new IngredientWithQuantity(current));
                    }
                });
            }
            else {
                holder.ingredientView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("ingredient", current);
//                        bundle.putString("name", current.getName());
//                        bundle.putString("unit", current.getRecipeUnit());
//                        bundle.putString("subunit", current.getShopUnit());
//                        bundle.putDouble("subunit_quantity", current.getShopUnitQuantity());

                        IngredientMenuDialog dialog = new IngredientMenuDialog();
                        dialog.setArguments(bundle);
                        dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "IngredientMenuDialog");
                        return true;
                    }
                });
            }
        }
        else
            holder.ingredientName.setText(R.string.no_ingredients);
    }

    public void setIngredients(List<? extends Ingredient> ingredients) {
        this.ingredients = ingredients;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if(ingredients != null)
            return ingredients.size();
        else
            return 0;
    }
}
