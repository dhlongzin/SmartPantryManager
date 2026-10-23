# Smart Pantry Manager

A Java Android application that helps reduce food waste by tracking the ingredients you already have at home and suggesting recipes you can cook **strictly** from those leftovers  no missing items, no shopping list required.


---

## Concept

You manage a digital pantry (name, quantity, unit, optional expiry date). The app seeds a collection of recipes on first run. The **Suggested Recipes** screen runs strict matching logic: a recipe appears only if every required ingredient is present in the pantry in sufficient quantity (with basic unit conversion support, e.g. ml ↔ tbsp).

This strict-matching rule is the core business logic of the app.

---

## Features

| Area | What it does |
|------|----------------|
| **Pantry** | Add, edit, and delete ingredients. List shown in a `RecyclerView`. Swipe to delete with confirmation dialog. |
| **Expiry** | Optional expiry date on items. Expired / expiring-soon items are highlighted on the pantry list. Toggle for expiry warnings in Settings. |
| **Recipes** | 18+ recipes pre-seeded into SQLite on first launch (name, steps, required ingredients + quantities/units). |
| **Suggested Recipes** | Lists only recipes the user can make *right now* from current pantry stock. Empty state when nothing matches. |
| **Recipe Detail** | Full ingredient list and preparation steps for a selected recipe (opened via Intent from suggestions). |
| **Settings** | Preference for expiry warnings (stored in `SharedPreferences`). |
| **Navigation** | Bottom navigation bar across Pantry, Suggested Recipes, and Settings. |
| **Theme** | Black and white Material theme with bordered ingredient cards. |

---

## Database

**Choice: SQLite** (via Android’s `SQLiteOpenHelper`).

**Why SQLite?**

- Local-only app; no need for a remote server or account system.
- Built into Android no extra SDKs or network permission.
- Reliable offline persistence between sessions.
- Relational model fits the data well: ingredients, recipes, and a junction table for recipe ingredients with foreign keys and cascade delete.
- Easy to seed on first run and to query for matching logic.

### Schema (simplified)

- **ingredient** — `id`, `name`, `quantity`, `unit`, `expiry_date`
- **recipe** — `id`, `name`, `steps`
- **recipe_ingredient** — `id`, `recipe_id` (FK → recipe), `ingredient_name`, `required_qty`, `unit`

Matching is done in `RecipeMatcher` by normalising names (case/spacing/singularisation) and converting compatible units to a common base before comparing available vs required quantity.

---

## Screens / Activities

1. **MainActivity** — Pantry list + FAB to add item  
2. **AddEditIngredientActivity** — Create or update an ingredient (validation + date picker)  
3. **SuggestedRecipesActivity** — Strict-matched recipe list  
4. **RecipeDetailActivity** — Recipe ingredients + method  
5. **SettingsActivity** — Expiry warning toggle  

---

## Tech stack

- **Language:** Java  
- **Min SDK:** 24  
- **UI:** AndroidX, Material Components, `RecyclerView`, bottom navigation  
- **Persistence:** SQLite (`DatabaseHelper`) + `SharedPreferences` for settings  
- **Architecture:** Multiple Activities + Intents; adapters for lists; helper classes for navigation and recipe matching  

---

## Setup & run instructions

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (Hedgehog or newer recommended)
- JDK 11+
- Android SDK with API 24+ and a recent compile SDK
- An emulator (API 24+) or a physical device with USB debugging enabled

### Steps

1. **Clone the repository**
   ```bash
   git clone (https://github.com/dhlongzin/SmartPantryManager)
   cd SmartPantryManager
   ```

2. **Open in Android Studio**  
   File → Open → select the project folder (the one that contains `settings.gradle` and the `app` module).

3. **Sync Gradle**  
   Let Android Studio download dependencies and sync the project. Resolve any SDK prompts if they appear.

4. **Run**
   - Select an emulator or connected device.
   - Click **Run** (green play button) or press `Shift+F10`.
   - The app launches on the Pantry screen. Recipes are seeded automatically on first open.

5. **Try the core flow**
   - Add a few ingredients that match a known recipe (e.g. common pantry items from the seed data).
   - Open **Suggested Recipes** via the bottom nav  matching recipes should appear.
   - Remove a critical ingredient and return to Suggested Recipes that recipe should disappear (strict match).
   - Swipe an item on the pantry list to delete (with confirmation).
   - Close and reopen the app to confirm data persists.

### Optional: command-line build

```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

---

## Project structure (main parts)

```
app/src/main/
├── java/com/example/smartpantrymanager/
│   ├── MainActivity.java
│   ├── AddEditIngredientActivity.java
│   ├── SuggestedRecipesActivity.java
│   ├── RecipeDetailActivity.java
│   ├── SettingsActivity.java
│   ├── DatabaseHelper.java      # SQLite schema, CRUD, seed data
│   ├── RecipeMatcher.java       # Strict matching + unit conversion
│   ├── Ingredient.java / Recipe.java / ...
│   └── *Adapter.java            # RecyclerView adapters
└── res/
    ├── layout/                  # activity_*, item_*
    ├── menu/bottom_nav_menu.xml
    └── values/                  # themes, colors, strings
```

---

