package ms.organizer.activities;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import ms.organizer.R;
import ms.organizer.database.ingredients.IngredientWithQuantity;
import ms.organizer.database.ingredients.IngredientsViewModel;
import ms.organizer.database.recipes.Recipe;
import ms.organizer.database.recipes.RecipesViewModel;
import ms.organizer.utility.OrganizerUtility;

public class MainScreen extends AppCompatActivity implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);

        CardView recipesButton = findViewById(R.id.recipes);
        CardView fridgeButton = findViewById(R.id.fridge);
        CardView shoppingListButton = findViewById(R.id.shopping_list);
        CardView mealScheduleButton = findViewById(R.id.meal_schedule);
        recipesButton.setOnClickListener(this);
        fridgeButton.setOnClickListener(this);
        shoppingListButton.setOnClickListener(this);
        mealScheduleButton.setOnClickListener(this);

        if (!OrganizerUtility.isExternalStoragePermissionGranted(this)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
        downloadNewRecipes();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.recipes:
                Intent recipeIntent = new Intent(this, Recipes.class);
                startActivity(recipeIntent);
                break;
            case R.id.fridge:
                Intent fridgeIntent = new Intent(this, Fridge.class);
                startActivity(fridgeIntent);
                break;
            case R.id.shopping_list:
                Intent shoppingListIntent = new Intent(this, ShoppingList.class);
                startActivity(shoppingListIntent);
                break;
            case R.id.meal_schedule:
                Intent mealScheduleIntent = new Intent(this, MealSchedule.class);
                startActivity(mealScheduleIntent);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (OrganizerUtility.isExternalStorageWritable()) {
                        OrganizerUtility.makeToast(this, getString(R.string.external_storage_permission_granted));
                        File mainFolder = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name));
                        if (!mainFolder.exists())
                            if (!mainFolder.mkdirs())
                                OrganizerUtility.makeToast(this, getString(R.string.mkdirs_error));
                            else
                                MediaScannerConnection.scanFile(this, new String[]{mainFolder.toString()}, null, null);
                    } else {
                        OrganizerUtility.makeToast(this, getString(R.string.external_storage_unwritable));
                    }
                } else
                    OrganizerUtility.makeToast(this, getString(R.string.permission_not_granted));
            }
        }
    }

    private void downloadNewRecipes() {
        if (OrganizerUtility.isExternalStoragePermissionGranted(this)) {
            if (OrganizerUtility.isExternalStorageWritable()) {
                File recipesFolder = new File(Environment.getExternalStorageDirectory() + "/" + getString(R.string.app_name), getString(R.string.recipes_folder));
                if (!recipesFolder.exists()) {
                    if (!recipesFolder.mkdirs())
                        OrganizerUtility.makeToast(this, getString(R.string.mkdirs_error));
                    else {
                        MediaScannerConnection.scanFile(this, new String[]{recipesFolder.toString()}, null, null);
                        File recipesImagesFolder = new File(recipesFolder, getString(R.string.images_folder));
                        if (!recipesImagesFolder.mkdirs())
                            OrganizerUtility.makeToast(this, getString(R.string.mkdirs_error));
                        else
                            MediaScannerConnection.scanFile(this, new String[]{recipesImagesFolder.toString()}, null, null);

                        File recipesBackupFolder = new File(recipesFolder, getString(R.string.backup_folder));
                        if (!recipesBackupFolder.mkdirs())
                            OrganizerUtility.makeToast(this, getString(R.string.mkdirs_error));
                        else {
                            MediaScannerConnection.scanFile(this, new String[]{recipesBackupFolder.toString()}, null, null);
                            System.out.println("dupppa");
                            //TODO naprawa systemu plikow

//                            File temporary = OrganizerUtility.createTemporaryFile(recipesBackupFolder);
//                            if (temporary != null) {
//                                MediaScannerConnection.scanFile(this, new String[]{recipesBackupFolder.toString(), temporary.toString()}, null, null);
//                                if (!temporary.delete())
//                                    OrganizerUtility.makeToast(this, getString(R.string.mkdirs_error));
//                            }
//                            else
//                                OrganizerUtility.makeToast(this, getString(R.string.mkdirs_error));
                        }
                    }

                    File ingredientsFolder = new File(Environment.getExternalStorageDirectory() + "/" + getString(R.string.app_name), getString(R.string.ingredients_folder));
                    if (!ingredientsFolder.exists()) {
                        if (!ingredientsFolder.mkdirs())
                            OrganizerUtility.makeToast(this, getString(R.string.mkdirs_error));
                        else
                            MediaScannerConnection.scanFile(this, new String[]{ingredientsFolder.toString()}, null, null);
                    }
                }
                File[] recipeFiles = recipesFolder.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return !pathname.isDirectory();
                    }
                });
                if (recipeFiles.length != 0) {
                    try {
                        RecipesViewModel recipesViewModel = ViewModelProviders.of(this).get(RecipesViewModel.class);
                        IngredientsViewModel ingredientsViewModel = ViewModelProviders.of(this).get(IngredientsViewModel.class);

                        for (File file : recipeFiles) {
                            Scanner scanner = new Scanner(file);
                            StringBuilder recipeString = new StringBuilder();

                            while (scanner.hasNextLine())
                                recipeString.append(scanner.nextLine());

                            JSONObject recipeJSON = new JSONObject(recipeString.toString());
                            JSONArray ingredientsArray = recipeJSON.getJSONArray("selectedIngredients");

                            Recipe recipe = new Recipe(recipeJSON.getString("title"), recipeJSON.getString("description"));
                            ArrayList<IngredientWithQuantity> ingredients = new ArrayList<>();

                            for (int i = 0; i < ingredientsArray.length(); i++) {
                                JSONObject ingredientJSON = ingredientsArray.getJSONObject(i);
                                String ingredientName = ingredientJSON.getString("name");
                                String recipeUnit = ingredientJSON.getString("recipe_unit");
                                String shopUnit = ingredientJSON.getString("shop_unit");
                                double shopUnitQuantity = ingredientJSON.getDouble("shop_unit_quantity");
                                double quantity = ingredientJSON.getDouble("recipe_quantity");

                                IngredientWithQuantity ingredient = new IngredientWithQuantity(ingredientName, recipeUnit, shopUnit, shopUnitQuantity, quantity);
                                ingredientsViewModel.insert(ingredient);
                                ingredients.add(ingredient);
                            }

                            recipesViewModel.insert(recipe, ingredients);
                            if (!file.delete())
                                OrganizerUtility.makeToast(this, getString(R.string.delete_file_error));
                        }
                    } catch (FileNotFoundException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else
                OrganizerUtility.makeToast(this, getString(R.string.external_storage_unwritable));
        } else
            OrganizerUtility.makeToast(this, getString(R.string.external_storage_permission_not_granted));
    }
}