package com.example.smartpantrymanager;

import android.app.Activity;
import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Sets up the bottom navigation bar shared by the three main screens
 * (Pantry, Recipes, Settings) so the same code is not repeated in each Activity.
 */
public class NavigationHelper {

    /**
     * Highlights the current screen's tab and opens the other screens with Intents.
     *
     * @param activity       the screen that owns the navigation bar
     * @param selectedItemId the menu id of that screen's own tab
     */
    public static void setup(Activity activity, int selectedItemId) {
        BottomNavigationView nav = activity.findViewById(R.id.bottomNav);

        // Select this screen's tab first (the listener is attached afterwards)
        nav.setSelectedItemId(selectedItemId);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedItemId) {
                return true; // already on this screen
            }

            Class<?> target;
            if (id == R.id.nav_pantry) {
                target = MainActivity.class;
            } else if (id == R.id.nav_suggested) {
                target = SuggestedRecipesActivity.class;
            } else {
                target = SettingsActivity.class;
            }

            // Bring an existing screen to the front instead of stacking copies
            Intent intent = new Intent(activity, target);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            activity.startActivity(intent);
            activity.overridePendingTransition(0, 0);

            // false keeps this screen's own tab highlighted if the user comes back
            return false;
        });
    }
}