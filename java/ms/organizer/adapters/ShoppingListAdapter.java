package ms.organizer.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import ms.organizer.R;
import ms.organizer.database.shoppinglist.ShoppingListIngredient;
import ms.organizer.database.shoppinglist.ShoppingListViewModel;
import ms.organizer.dialogs.IngredientMenuDialog;
import ms.organizer.utility.ItemGestureCallback;
import ms.organizer.utility.OrganizerUtility;

/**
 * Created by Miki on 28.10.2018.
 */

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ShoppingListViewHolder>
        implements ItemGestureCallback.ItemGestureHelperAdapter {

    class ShoppingListViewHolder extends RecyclerView.ViewHolder {
        private final ImageButton dragButton;
        private final CheckBox checkBox;
        private final ImageView ingredientIcon;
        private final TextView ingredientName;
        private final TextView ingredientQuantity;
        private final LinearLayout layout;

        private ShoppingListViewHolder(View itemView) {
            super(itemView);
            dragButton = itemView.findViewById(R.id.drag_button);
            checkBox = itemView.findViewById(R.id.check_box);
            ingredientIcon = itemView.findViewById(R.id.ingredient_icon);
            ingredientName = itemView.findViewById(R.id.ingredient_name);
            ingredientQuantity = itemView.findViewById(R.id.ingredient_quantity);
            layout = itemView.findViewById(R.id.shopping_list_item_layout);
        }
    }

    private final LayoutInflater inflater;
    private List<ShoppingListIngredient> items;
    private ItemTouchHelper helper;
    private ShoppingListViewModel shoppingListViewModel;

    public ShoppingListAdapter(Context context, RecyclerView recyclerView, ShoppingListViewModel shoppingListViewModel) {
        inflater = LayoutInflater.from(context);
        this.shoppingListViewModel = shoppingListViewModel;

        final ItemGestureCallback callback = new ItemGestureCallback(this, false, false);
        helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(recyclerView);
    }

    @Override
    public ShoppingListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.shopping_list_item, parent, false);
        return new ShoppingListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ShoppingListViewHolder holder, final int position) {
        if (holder != null) {
            final Context context = inflater.getContext();
            final ShoppingListIngredient current = items.get(position);
            final String quantityWithUnit = current.getQuantity() + " " + current.getShopUnit();

            OrganizerUtility.setIngredientIcon(context, current.getName(), holder.ingredientIcon, 40);
            holder.ingredientName.setText(OrganizerUtility.formatIngredientName(current.getName()));
            holder.ingredientQuantity.setText(quantityWithUnit);

            final boolean isChecked = current.isChecked();

            holder.checkBox.setChecked(isChecked);
            holder.dragButton.setEnabled(!isChecked);
            setStrikethrough(holder.ingredientName, isChecked);
            setStrikethrough(holder.ingredientQuantity, isChecked);

            holder.dragButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    helper.startDrag(holder);
                    return true;
                }
            });

            holder.layout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (!holder.checkBox.isChecked()) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("ingredient", current);

                        IngredientMenuDialog dialog = new IngredientMenuDialog();
                        dialog.setArguments(bundle);
                        dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "IngredientMenuDialog");
                    } else {
                        OrganizerUtility.makeToast(context, context.getString(R.string.cant_modify_checked_ingredient));
                    }
                    return true;
                }
            });

            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    final int position = holder.getAdapterPosition();
                    if (((CheckBox) v).isChecked()) {
                        shoppingListViewModel.setIngredientChecked(current.getIngredientID(), true);
                        notifyItemMoved(position, items.size() - 1);
                    } else {
                        shoppingListViewModel.setIngredientChecked(current.getIngredientID(), false);
                        notifyItemMoved(position, items.size() - shoppingListViewModel.getCheckedItemsSize() - 1);
                    }

                }
            });

            holder.dragButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    helper.startDrag(holder);

                    return true;
                }
            });
        }
    }

    public void setItems(List<ShoppingListIngredient> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (items != null)
            return items.size();
        else
            return 0;
    }

    private void setStrikethrough(final TextView view, final boolean checked) {
        final Drawable icon = view.getCompoundDrawables()[0];
        if (checked) {
            view.setPaintFlags(view.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            view.setTextColor(Color.LTGRAY);

            if (icon != null)
                icon.setAlpha(50);
        }
        else {
            view.setPaintFlags(view.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            view.setTextColor(Color.GRAY);

            if (icon != null)
                icon.setAlpha(255);
        }
    }

    @Override
    public void onItemMove(final int fromPosition, final int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++)
                Collections.swap(items, i, i + 1);
        }
        else {
            for (int i = fromPosition; i > toPosition; i--)
                Collections.swap(items, i, i - 1);
        }
        notifyItemMoved(fromPosition, toPosition);
        updatePositions();
    }

    private void updatePositions() {
        for (int position = 0; position < items.size(); position++) {
            final int ingredientID = items.get(position).getIngredientID();
            shoppingListViewModel.changeItemPosition(ingredientID, position);
        }
    }
}
