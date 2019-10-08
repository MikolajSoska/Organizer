package ms.organizer.utility;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import ms.organizer.R;

public class OrganizerUtility {
    public static void makeToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static boolean isExternalStoragePermissionGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public static String formatIngredientName(String name) {
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    public static String formatQuantityWithUnit(double quantity, String unit) {
        if (quantity == 0)
            return "";
        else if (quantity == (int) quantity)
            return  (int) quantity + " " + unit;
        else
            return quantity + " " + unit;
    }

//    public static void setIngredientIcon(Context context, String name, TextView ingredientView) {
//        File ingredientFolder = new File(Environment.getExternalStorageDirectory() + "/" + context.getString(R.string.app_name), context.getString(R.string.ingredients_folder));
//        File ingredientIcon = new File(ingredientFolder, "icon_" + name + ".png");
//        if (ingredientIcon.exists()) {
//            Drawable icon = new BitmapDrawable(context.getResources(), BitmapFactory.decodeFile(ingredientIcon.getPath()));
//            ingredientView.setCompoundDrawablesWithIntrinsicBounds(icon,null,null,null);
//        }
//        else {
//            String iconName = "icon_" + name.charAt(0);
//            int id = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
//            ingredientView.setCompoundDrawablesWithIntrinsicBounds(id,0,0,0);
//        }
//    }

    public static void setIngredientIcon(Context context, String name, ImageView view, int size) {
        File ingredientFolder = new File(Environment.getExternalStorageDirectory() + "/" + context.getString(R.string.app_name), context.getString(R.string.ingredients_folder));
        name = name.toLowerCase().replaceAll(" ", "_");
        File ingredientIcon = new File(ingredientFolder, "icon_" + name + ".png");
        Resources resources = context.getResources();

        if (ingredientIcon.exists()) {
            Bitmap bitmap = new BitmapDrawable(resources, BitmapFactory.decodeFile(ingredientIcon.getPath())).getBitmap();
            int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, resources.getDisplayMetrics());
            int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, resources.getDisplayMetrics());

            Bitmap image = Bitmap.createScaledBitmap(bitmap, width, height, false);
            view.setImageBitmap(image);
        }
    }

    public static void setRecipeIcon(Context context, String title, ImageView view) {
        String imageName = title.toLowerCase().replace(" ", "_");
        Resources resources = context.getResources();

        File recipeImageFolder = new File(Environment.getExternalStorageDirectory() + "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.recipes_folder), context.getString(R.string.images_folder));
        File recipeImage = new File(recipeImageFolder, imageName + ".jpg");

        if (recipeImage.exists()) {
            Bitmap bitmap = new BitmapDrawable(resources, BitmapFactory.decodeFile(recipeImage.getPath())).getBitmap();
            float screenWidth = getScreenWidth((AppCompatActivity) context);
            int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, screenWidth, resources.getDisplayMetrics());
            int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, screenWidth / 2, resources.getDisplayMetrics());

            Bitmap image = Bitmap.createScaledBitmap(bitmap, width, height, false);
            view.setImageBitmap(image);
        }
    }

    private static float getScreenWidth(AppCompatActivity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float density = activity.getResources().getDisplayMetrics().density;

        return metrics.widthPixels / density;
    }

    public static String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        return (day + "-" + month + "-" + year);
    }

//    public static File createTemporaryFile(File parent) {
//        try {
//            File temporary = new File(parent, "temporary");
//            if (!temporary.createNewFile())
//                return temporary;
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
}
