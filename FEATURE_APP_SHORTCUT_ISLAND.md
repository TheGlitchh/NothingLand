# App Shortcut Island Feature

## Overview
The App Shortcut Island is a new plugin for NothingLand that adds quick app launcher functionality to the Dynamic Island. Users can long-tap the closed island to expand it and see their selected apps, then tap any app to launch it instantly.

## Features

### 1. **Quick App Launcher**
   - Long-press the Dynamic Island to expand and show up to 6 selected apps
   - Single tap any app icon to launch it
   - Apps are displayed in a 3-column grid for easy access

### 2. **App Selection**
   - Open the settings from the main app or tap the shortcut island settings
   - Search through all installed apps (excludes system apps by default)
   - Select up to 6 apps to display in the island
   - Selections are saved in SharedPreferences for persistence

### 3. **Smart Defaults**
   - If no apps are selected, the plugin loads popular apps as defaults:
     - Chrome
     - WhatsApp
     - Google Messages
     - Spotify
     - Google Play Services
     - Google Maps

### 4. **Visual Integration**
   - App icons and names displayed clearly
   - Seamless integration with the existing Dynamic Island blur effect
   - Responsive grid layout that adapts to screen size

## Architecture

### Files Created

#### 1. **AppShortcutIsland.java**
   - Main plugin class extending `BasePlugin`
   - Handles plugin lifecycle (onCreate, onBind, onUnbind, onDestroy)
   - Manages app selection persistence
   - Implements long-press to expand functionality

#### 2. **AppShortcutAdapter.java**
   - GridView adapter for displaying apps
   - Handles app item clicks
   - Manages icon and label rendering

#### 3. **AppShortcutPickerActivity.java**
   - Activity for selecting which apps to display
   - Implements search/filter functionality
   - Enforces maximum 6 app selection limit
   - Saves selections to SharedPreferences

#### 4. **Layouts**
   - `app_shortcut_island_layout.xml` - Main container for the island grid
   - `app_shortcut_grid_item.xml` - Individual app icon + label item
   - `activity_app_shortcut_picker.xml` - App picker activity UI

#### 5. **Resources**
   - `strings.xml` - All string resources for the feature
   - `AndroidManifest.xml` - Updated with AppShortcutPickerActivity

## How It Works

### User Flow

```
1. Long-press Dynamic Island
   ↓
2. OverlayService detects long-press (ViewConfiguration.getLongPressTimeout())
   ↓
3. AppShortcutIsland plugin is enqueued to display
   ↓
4. Grid of selected apps appears
   ↓
5. User taps an app
   ↓
6. App launches via PackageManager.getLaunchIntentForPackage()
   ↓
7. Island collapses automatically
```

### Configuration Flow

```
1. Open NothingLand main app
   ↓
2. Navigate to "Configure App Shortcuts" setting
   ↓
3. AppShortcutPickerActivity opens
   ↓
4. User searches and selects up to 6 apps
   ↓
5. Click "Save"
   ↓
6. Selection persisted to SharedPreferences
   ↓
7. Island updates on next long-press
```

## Technical Details

### SharedPreferences Keys
- `app_shortcut_selected_apps` - Set of selected package names
- Maximum apps: 6
- Auto-saves when user completes selection

### Permissions Required
- `QUERY_ALL_PACKAGES` - Already in manifest for NothingLand

### Touch Handling
The feature integrates with existing OverlayService touch handling:
- Long-press triggers `expandOverlay()` → `AppShortcutIsland.onExpand()`
- Single tap on app triggers `launchApp(packageName)`
- Swipe up dismisses the island

### Integration Points

1. **OverlayService Touch Events (lines 376-441)**
   - Long press → expandOverlay() → enqueues AppShortcutIsland plugin
   - Island expands and shows app grid

2. **Plugin Queue System**
   - AppShortcutIsland uses existing `enqueue()` / `dequeue()` system
   - Allows stacking with other plugins

3. **View Binding**
   - Uses ConstraintLayout with ID `R.id.binded`
   - Automatically positioned and animated by OverlayService

## Customization Options

### Maximum Apps
Edit `AppShortcutIsland.java` line 39:
```java
private static final int MAX_SHORTCUTS = 6; // Change this value
```

### Default Apps
Edit `AppShortcutIsland.java` lines 189-200:
```java
String[] defaultPackages = {
    "com.android.chrome",
    "com.whatsapp",
    // Add or modify package names here
};
```

### Grid Columns
Edit `app_shortcut_island_layout.xml`:
```xml
<GridView
    android:numColumns="3" <!-- Change this value -->
    ...
```

## Testing

### Manual Testing Steps

1. **Build and Install**
   ```bash
   ./gradlew installDebug
   ```

2. **Enable Accessibility Service**
   - Settings → Accessibility → NothingLand
   - Toggle ON

3. **Test Long-Press**
   - Look at Dynamic Island at top of screen
   - Long-press for 500ms
   - Should see app grid expand

4. **Test App Launch**
   - Tap any app in the grid
   - App should launch
   - Island should collapse

5. **Test Configuration**
   - Open NothingLand main app
   - Tap "Configure App Shortcuts"
   - Select different apps
   - Click Save
   - Verify changes on next long-press

## Future Enhancements

- [ ] Favorite apps for quick access
- [ ] Custom app ordering by dragging
- [ ] Different grid layouts (2x2, 2x3, etc.)
- [ ] App usage statistics to auto-select frequently used apps
- [ ] Widget support to show badges (unread counts, etc.)
- [ ] Custom colors per app
- [ ] Gesture shortcuts (e.g., swipe right for specific app)

## Troubleshooting

### Apps Don't Show
1. Verify apps are not system apps
2. Check that apps are installed
3. Try clearing app data and reconfiguring

### Long-press Not Working
1. Ensure Accessibility Service is enabled
2. Check that plugin is enabled in app settings
3. Verify OverlayService is running

### App Won't Launch
1. Verify app is still installed
2. Check app permissions
3. Ensure LaunchIntent exists for the package

## Code Quality

- **Error Handling**: Try-catch blocks for PackageManager operations
- **Memory**: Reuses ViewHolder in adapter
- **Performance**: Lazy loading of app list, search filtering
- **UI**: Material Design components, responsive layouts
- **Permissions**: Already handled by existing NothingLand manifest

## Version History

### v1.0 (Initial Release)
- Long-press to expand functionality
- App selection UI with search
- Up to 6 app shortcuts
- Default app list
- Persistent storage
