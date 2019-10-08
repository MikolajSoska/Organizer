package ms.organizer.activities;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

import ms.organizer.R;
import ms.organizer.adapters.ShoppingListAdapter;
import ms.organizer.database.fridge.FridgeItem;
import ms.organizer.database.fridge.FridgeViewModel;
import ms.organizer.database.ingredients.Ingredient;
import ms.organizer.database.ingredients.IngredientWithQuantity;
import ms.organizer.database.shoppinglist.ShoppingListIngredient;
import ms.organizer.database.shoppinglist.ShoppingListViewModel;
import ms.organizer.dialogs.ChooseIngredientDialog;
import ms.organizer.dialogs.IngredientMenuDialog;
import ms.organizer.utility.OrganizerUtility;

/**
 * Created by Miki on 28.10.2018.
 */

public class ShoppingList extends AppCompatActivity implements ChooseIngredientDialog.ChooseIngredientDialogListener, IngredientMenuDialog.IngredientMenuDialogListener {
    private ShoppingListViewModel shoppingListViewModel;
    private ShoppingListAdapter adapter;
    private ChooseIngredientDialog dialog;
    private MenuItem addToFridgeButton;
    private String tag;
    private boolean enableButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shopping_list);

        RecyclerView ingredientsInList = findViewById(R.id.ingredients_in_list);
        shoppingListViewModel = ViewModelProviders.of(this).get(ShoppingListViewModel.class);
        adapter = new ShoppingListAdapter(this, ingredientsInList, shoppingListViewModel);

        ingredientsInList.setAdapter(adapter);
        ingredientsInList.setLayoutManager(new LinearLayoutManager(this));

        shoppingListViewModel.getFromShoppingList().observe(this, new Observer<List<ShoppingListIngredient>>() {
            @Override
            public void onChanged(@Nullable List<ShoppingListIngredient> ingredients) {
                adapter.setItems(ingredients);
                setButtonEnable(shoppingListViewModel.getCheckedItemsSize() > 0);
            }
        });

    }

    @Override
    public void onIngredientPicked(IngredientWithQuantity ingredient) {
        dialog.setSelectedIngredient(ingredient, false);
    }

    @Override
    public void onDialogPositiveClick(IngredientWithQuantity ingredient, double quantity) {
        final int ingredientID = ingredient.getIngredientID();
        if (quantity < 0) {
            final double newQuantity = -(ingredient.getQuantity() + quantity);
            if (newQuantity > 0)
                shoppingListViewModel.addToShoppingList(ingredientID, (int) newQuantity, false);
            else
                shoppingListViewModel.removeFromShoppingList(ingredientID, (int) -newQuantity, false);
        } else
            shoppingListViewModel.addToShoppingList(ingredientID, (int) quantity, false);
    }

    @Override
    public void onDialogClick(Ingredient ingredient, int which) {
        final IngredientWithQuantity ingredientWithQuantity = (IngredientWithQuantity) ingredient;
        final int ingredientID = ingredientWithQuantity.getIngredientID();
        final double itemQuantity = ingredientWithQuantity.getQuantity();
        final double quantityToModify = shoppingListViewModel.getQuantityToModify(ingredientID);

        if (which == 0) {
            if (quantityToModify > 0.) {
                shoppingListViewModel.removeFromShoppingList(ingredientID, quantityToModify, false);
                if (quantityToModify < itemQuantity)
                    OrganizerUtility.makeToast(this, getString(R.string.delete_constraints));
            } else
                OrganizerUtility.makeToast(this, getString(R.string.cant_delete));
        } else {
            if (quantityToModify < itemQuantity) {
                ingredientWithQuantity.setQuantity(quantityToModify);
                OrganizerUtility.makeToast(this, getString(R.string.edit_constraints));
            }

            Bundle bundle = new Bundle();
            bundle.putSerializable("ingredient", ingredientWithQuantity);
            tag = "EditQuantityInListDialog";

            dialog = new ChooseIngredientDialog();
            dialog.setArguments(bundle);
            dialog.show(getSupportFragmentManager(), tag);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.shopping_list_menu, menu);
        addToFridgeButton = menu.findItem(R.id.add_to_fridge);
        addToFridgeButton.setEnabled(enableButton);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.add_to_fridge) {
            final List<FridgeItem> checkedItems = shoppingListViewModel.getCheckedItems();

            if (checkedItems != null) {
                final FridgeViewModel fridgeViewModel = ViewModelProviders.of(this).get(FridgeViewModel.class);

                fridgeViewModel.addToFridge(checkedItems.toArray(new FridgeItem[0]));
                shoppingListViewModel.deleteCheckedIngredients();
                OrganizerUtility.makeToast(this, getString(R.string.ingredients_added) + " " + checkedItems.size());
            }
        } else {
            tag = "AddToListDialog";
            dialog = new ChooseIngredientDialog();
            dialog.show(getSupportFragmentManager(), tag);
        }

        return super.onOptionsItemSelected(item);
    }

    private void setButtonEnable(boolean enable) {
        if (addToFridgeButton != null)
            addToFridgeButton.setEnabled(enable);
        else
            enableButton = enable;
    }
}
