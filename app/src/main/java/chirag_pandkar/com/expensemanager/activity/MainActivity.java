package chirag_pandkar.com.expensemanager.activity;

import android.Manifest;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import chirag_pandkar.com.expensemanager.R;
import chirag_pandkar.com.expensemanager.adapter.DrawerListViewAdapter;
import chirag_pandkar.com.expensemanager.adapter.HomeViewPagerAdapter;
import chirag_pandkar.com.expensemanager.database.ExpenseDatabaseHelper;
import chirag_pandkar.com.expensemanager.presenter.NavigationDrawerPresenter;
import chirag_pandkar.com.expensemanager.table.notification.FillExpenseNotificationScheduler;
import chirag_pandkar.com.expensemanager.view.NavigationDrawerItemView;


public class MainActivity extends FragmentActivity implements NavigationDrawerItemView, ActionBar.TabListener {

    public static final int ADD_NEW_CAT = 9991;
    public static final int DELETE_CAT = 9992;
    private static final int REQUEST_CAMERA_CODE = 100;
    private static Boolean isNotificationScheduled = false;
    private ActionBar actionBar;
    private ViewPager viewPager;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private DrawerLayout drawerLayout;
    private HomeViewPagerAdapter homeViewPagerAdapter;

    public static boolean isNumber(String string) {
        String pattern = "^[-+]?[0-9]*\\.?[0-9]+$";
        return Pattern.matches(pattern, string);
    }

    private static String processTextHorizontally(Text text) {
        StringBuilder result = new StringBuilder();
        List<Text.TextBlock> textBlocks = text.getTextBlocks();
        textBlocks.sort(Comparator.comparingInt(o -> o.getBoundingBox().top));
        for (Text.TextBlock textBlock : textBlocks) {
            List<Text.Line> lines = textBlock.getLines();
            lines.sort(Comparator.comparingInt(o -> o.getBoundingBox().left));
            for (Text.Line line : lines) {
                result.append(line.getText()).append(" ");
            }
            result.append(System.lineSeparator());
        }
        return result.toString();
    }

    public static String getTotal(String text) {
        List<String> keywords = Arrays.asList("total", "total amount", "subtotal", "charge", "amount", "net", "amt", "Balance");
        List<String> suspects = new ArrayList<>();
        String[] lines = text.split("\n");

        String amtVocab = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 ,.";

        for (String line : lines) {
            StringBuilder newString = new StringBuilder();
            for (char chr : line.toCharArray()) {
                if (amtVocab.contains(String.valueOf(chr))) {
                    newString.append(chr);
                }
            }

            boolean temp = false;
            for (String key : keywords) {
                int ratio = 80;//fuzzyPartialRatio(newString.toString().toLowerCase(), key);
                if (ratio >= 70) {
                    temp = true;
                    break;
                }
            }

            if (temp) {
                suspects.add(newString.toString());
            }
        }

        List<String> reversedSuspects = new ArrayList<>(suspects);
        Collections.reverse(reversedSuspects);

        List<Double> amount = new ArrayList<>();
        for (String string : reversedSuspects) {
            String[] li = string.trim().split("\\s+");
            for (String word : li) {
                try {
                    double value = Double.parseDouble(word);
                    amount.add(value);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return (!amount.isEmpty()) ? "NIL" : String.valueOf(Collections.max(amount));
    }

    public static int fuzzyPartialRatio(String str1, String str2) {
        str1 = str1.toLowerCase();
        str2 = str2.toLowerCase();

        int len1 = str1.length();
        int len2 = str2.length();

        int[][] matrix = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            matrix[i][0] = i;
        }

        for (int j = 0; j <= len2; j++) {
            matrix[0][j] = j;
        }

        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    matrix[i][j] = matrix[i - 1][j - 1];
                } else {
                    matrix[i][j] = Math.min(matrix[i - 1][j], matrix[i][j - 1]) + 1;
                }
            }
        }

        int maxLen = Math.max(len1, len2);
        int similarity = maxLen - matrix[len1][len2];

        return (int) ((similarity / (double) maxLen) * 100);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        configureDrawer();
        configureActionBar();
        if (!isNotificationScheduled) scheduleReminder();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 400);
        }
    }

    @Override
    public void render(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment existingFragment = fragmentManager.findFragmentById(R.id.main_frame);
        if (existingFragment != null && existingFragment.getClass() == fragment.getClass()) return;
        fragmentManager.beginTransaction()
                .addToBackStack(MainActivity.class.getSimpleName())
                .replace(R.id.main_frame, fragment, fragment.getClass().getSimpleName())
                .commit();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    }

    @Override
    public void goToHome() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
        if (backStackEntryCount == 0)
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setTitle(R.string.app_name);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem menuUpdateLimit = menu.findItem(R.id.menu_update_limit);
        menuUpdateLimit.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                showUpdateLimitDialog(); // Call the method to show the update limit dialog
                return true;
            }
        });
        return true;
    }

    private void showUpdateLimitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Monthly Limit");
        builder.setMessage("Enter the new monthly limit:");
        ExpenseDatabaseHelper expenseDatabaseHelper = new ExpenseDatabaseHelper(MainActivity.this);
        EditText limitInput = new EditText(this);
        limitInput.setText(expenseDatabaseHelper.getExpenseLimit().toString());
        builder.setView(limitInput);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String limitString = limitInput.getText().toString().trim();
                if (!TextUtils.isEmpty(limitString)) {
                    Double limit = Double.parseDouble(limitString);
                    expenseDatabaseHelper.updateExpenseLimit(limit);
                } else {
                    Toast.makeText(MainActivity.this, "Invalid limit. Please enter a valid value.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_category) {
            Intent intent = new Intent(this, AddCategoryActivity.class);
            startActivityForResult(intent, ADD_NEW_CAT);
            return true;
        }
        return actionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_NEW_CAT) {
            viewPager.setAdapter(new HomeViewPagerAdapter(getSupportFragmentManager()));
            viewPager.setCurrentItem(0);
        }
        if (requestCode == DELETE_CAT) {
            viewPager.setAdapter(new HomeViewPagerAdapter(getSupportFragmentManager()));
            viewPager.setCurrentItem(0);
        }
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            InputImage image = getInputImage(data);
            ExtractEndAmount(image);
        }
    }

    private void ExtractEndAmount(InputImage image) {
        TextRecognizerOptions options = new TextRecognizerOptions.Builder().build();
        TextRecognizer recognizer = TextRecognition.getClient(options);
        Task<Text> result = recognizer.process(image)
                .addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text visionText) {
                        String extractedText = visionText.getText();
                        String total = null;
                        try {
                            total = getTotalVal(extractedText);
                        } catch (Exception e) {
                        }
                        if (total != null) {
                            Toast.makeText(MainActivity.this, "Amount found " + total, Toast.LENGTH_LONG).show();
                            EditText amountEditText = findViewById(R.id.amount);
                            amountEditText.setText(total);
                        } else {
                            Toast.makeText(MainActivity.this, "Amount Not found, Please Enter Manually", Toast.LENGTH_LONG).show();

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle any errors
                    }
                });
    }

    @Nullable
    private InputImage getInputImage(Intent data) {
        Uri imageUri = data.getData();
        InputImage image = null;
        try {
            if (imageUri == null) {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                image = InputImage.fromBitmap(imageBitmap, 0);
            } else {
                image = InputImage.fromFilePath(this, imageUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Issue For Camera", Toast.LENGTH_LONG).show();

        }
        return image;
    }

    private String getTotalVal(String extractedText) {
        List<String> keywords = Arrays.asList("total", "total amount", "total anount","netamt", "subtotal", "charge", "amount:", "amount", "anount", "net", "amt","amt.", "Balance", "grand total", "bill amount", "total due", "payment", "net pay", "payable");
        String[] array = extractedText.split("[ \n]");
        Double res = 0D;
        for (int i = 0; i < array.length; i++) {
            String cur = array[i].trim().toLowerCase(Locale.ROOT);
            if (keywords.contains(cur)) {
                String val = getVal(array, i);
                double v = Double.parseDouble(val);
                res = Math.max(res, v);
            }
        }
        if (res == 0D) return null;
        return res + "";
    }

    private String getVal(String[] array, int i) {
        int j = 0;
        try {
            for (j = i; j < array.length; j++) {
                if (isNumber(array[j])) {
                    return array[j];
                }
            }
        } catch (Exception e) {
            getVal(array, j + 1);
        }
        return null;
    }

    private String extractTotalValue(String text) {
        String total = "";
        Pattern pattern = Pattern.compile("total:\\s*(\\d+(\\.\\d{1,2})?)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            total = matcher.group(1);
        }
        return total;
    }


    public void onExpenseAdded() {
        viewPager.setAdapter(homeViewPagerAdapter);
        actionBar.setSelectedNavigationItem(1);
        new FillExpenseNotificationScheduler().scheduleLimitRemainder(this);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    private void configureDrawer() {
        drawerLayout = findViewById(R.id.drawer);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.app_name, R.string.action_settings) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                drawerView.bringToFront();
            }
        };
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        drawerLayout.setDrawerShadow(R.mipmap.drawer_shadow, GravityCompat.START);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        ListView drawerList = findViewById(R.id.drawer_list);
        drawerList.setAdapter(new DrawerListViewAdapter(this));

        onDrawerItemSelected();
    }

    private void onDrawerItemSelected() {
        final ListView drawerList = findViewById(R.id.drawer_list);
        final NavigationDrawerPresenter navigationDrawerPresenter = new NavigationDrawerPresenter(this);
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String[] drawerItems = getResources().getStringArray(R.array.drawer_items);
                drawerList.setItemChecked(position, true);
                getActionBar().setTitle(drawerItems[position]);
                drawerLayout.closeDrawer(GravityCompat.START);
                drawerList.bringToFront();
                navigationDrawerPresenter.onItemSelected(drawerItems[position]);
                FrameLayout mainFrame = findViewById(R.id.main_frame);
                mainFrame.bringToFront();
            }
        });
    }

    private void configureActionBar() {
        actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        viewPager = findViewById(R.id.view_pager);
        homeViewPagerAdapter = new HomeViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(homeViewPagerAdapter);

        addTabs();

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
            }

            @Override
            public void onPageSelected(int i) {
                actionBar.setSelectedNavigationItem(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });
    }

    private void addTabs() {
        ActionBar.Tab addNewExpenseTab = actionBar.newTab();
        addNewExpenseTab.setTabListener(this);
        addNewExpenseTab.setText("Add New");
        actionBar.addTab(addNewExpenseTab);

        ActionBar.Tab todayTab = actionBar.newTab();
        todayTab.setTabListener(this);
        todayTab.setText("Today");
        actionBar.addTab(todayTab);

    }

    private void scheduleReminder() {
        new FillExpenseNotificationScheduler().schedule(this);
        isNotificationScheduled = true;
    }

    public void onSelectImageClick(View view) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Image");
        Intent chooserCameraIntent = Intent.createChooser(cameraIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{cameraIntent});
        startActivityForResult(chooserIntent, 0);
    }


}
