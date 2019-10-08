package ms.organizer.dialogs;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.ExecutionException;

import ms.organizer.R;
import ms.organizer.adapters.IngredientsWithQuantityAdapter;
import ms.organizer.database.ingredients.IngredientWithQuantity;
import ms.organizer.database.recipes.RecipesViewModel;
import ms.organizer.utility.OrganizerUtility;

public class RecipeLayoutDialog extends DialogFragment {
    private int recipeID;
    private String title;
    private String description;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.recipe, container, false);
        Context context = getContext();

        if (title != null) {
            try {
                RecipesViewModel recipesViewModel = ViewModelProviders.of(getActivity()).get(RecipesViewModel.class);

                Toolbar toolbar = view.findViewById(R.id.toolbar);
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dismiss();
                    }
                });

                TextView recipeTitle = view.findViewById(R.id.recipe_title);
                recipeTitle.setText(title);

                RecyclerView ingredients = view.findViewById(R.id.recipe_ingredients);
                final IngredientsWithQuantityAdapter adapter = new IngredientsWithQuantityAdapter(context, false);
                List<IngredientWithQuantity> ingredientWithQuantities = recipesViewModel.getRecipeIngredients(recipeID);
                adapter.setIngredients(recipesViewModel.getRecipeIngredients(recipeID));
                ingredients.setAdapter(adapter);
                ingredients.setLayoutManager(new LinearLayoutManager(context));

                TextView descriptionView = view.findViewById(R.id.recipe_description);
                descriptionView.setText(description);

                ImageView image = view.findViewById(R.id.recipe_image);
                image.requestFocus();
                OrganizerUtility.setRecipeIcon(context, title, image);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        else {
            OrganizerUtility.makeToast(getContext(), getString(R.string.recipe_open_error));
            dismiss();
        }

        return view;
    }

    @Override
    public void setArguments(Bundle args) {
        recipeID = args.getInt("id");
        title = args.getString("title");
        description = args.getString("description");
    }
}
