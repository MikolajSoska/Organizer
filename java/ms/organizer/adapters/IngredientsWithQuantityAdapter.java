package ms.organizer.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import ms.organizer.R;
import ms.organizer.database.ingredients.IngredientWithQuantity;
import ms.organizer.dialogs.IngredientMenuDialog;
import ms.organizer.utility.OrganizerUtility;

/**
 * Created by Miki on 08.10.2018.
 */

public class IngredientsWithQuantityAdapter extends RecyclerView.Adapter<IngredientsWithQuantityAdapter.IngredientsWithQuantityViewHolder> {
    class IngredientsWithQuantityViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ingredientIcon;
        private final TextView ingredientName;
        private final TextView ingredientQuantity;
        private final LinearLayout layout;

        private IngredientsWithQuantityViewHolder(View itemView) {
            super(itemView);
            ingredientIcon = itemView.findViewById(R.id.ingredient_icon);
            ingredientName = itemView.findViewById(R.id.ingredient_name);
            ingredientQuantity = itemView.findViewById(R.id.ingredient_quantity);
            layout = itemView.findViewById(R.id.ingredient_with_quantity_layout);
        }
    }

    private final LayoutInflater inflater;
    private List<IngredientWithQuantity> ingredients;
    private boolean isMenuEnabled;

    public IngredientsWithQuantityAdapter(Context context, boolean isMenuEnabled) {
        inflater = LayoutInflater.from(context);
        this.isMenuEnabled = isMenuEnabled;
    }

    @Override
    public IngredientsWithQuantityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.ingredients_with_quantity_item, parent, false);
        return new IngredientsWithQuantityViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final IngredientsWithQuantityViewHolder holder, int position) {
        final Context context = inflater.getContext();
        if (holder != null){
            final IngredientWithQuantity current = ingredients.get(position);

            OrganizerUtility.setIngredientIcon(context, current.getName(), holder.ingredientIcon, 40);
            holder.ingredientName.setText(OrganizerUtility.formatIngredientName(current.getName()));
            holder.ingredientQuantity.setText(OrganizerUtility.formatQuantityWithUnit(current.getQuantity(), current.getRecipeUnit()));

            if (isMenuEnabled) {
                if (!current.getRecipeUnit().equals("")) {
                    TypedValue outValue = new TypedValue();
                    context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                    holder.layout.setBackgroundResource(outValue.resourceId);

                    holder.layout.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("ingredient", current);

                            IngredientMenuDialog dialog = new IngredientMenuDialog();
                            dialog.setArguments(bundle);
                            dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "IngredientMenuDialog");
                            return true;
                        }
                    });
                }
            }
        }
    }

    public void setIngredients(List<IngredientWithQuantity> ingredients) {
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
