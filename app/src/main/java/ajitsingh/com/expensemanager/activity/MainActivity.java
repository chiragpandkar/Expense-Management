package ajitsingh.com.expensemanager.activity;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ajitsingh.com.expensemanager.R;
import ajitsingh.com.expensemanager.adapter.DrawerListViewAdapter;
import ajitsingh.com.expensemanager.adapter.HomeViewPagerAdapter;
import ajitsingh.com.expensemanager.presenter.NavigationDrawerPresenter;
import ajitsingh.com.expensemanager.table.notification.FillExpenseNotificationScheduler;
import ajitsingh.com.expensemanager.view.NavigationDrawerItemView;


public class MainActivity extends FragmentActivity implements NavigationDrawerItemView, ActionBar.TabListener {

  private ActionBar actionBar;
  private ViewPager viewPager;
  private ActionBarDrawerToggle actionBarDrawerToggle;
  private DrawerLayout drawerLayout;
  public static final int ADD_NEW_CAT = 9991;
  public static final int DELETE_CAT = 9992;
  private static Boolean isNotificationScheduled = false;
  private HomeViewPagerAdapter homeViewPagerAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    configureDrawer();
    configureActionBar();
    if (!isNotificationScheduled) scheduleReminder();
  }

  @Override
  public void render(Fragment fragment) {
    FragmentManager fragmentManager = getSupportFragmentManager();
    Fragment existingFragment = fragmentManager.findFragmentById(R.id.main_frame);

    if(existingFragment != null && existingFragment.getClass() == fragment.getClass()) return;

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
    return true;
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
    if (id == R.id.action_delete_category) {
      Intent intent = new Intent(this, DeleteCategoryActivity.class);
      startActivityForResult(intent, DELETE_CAT);
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
      // Get the selected image URI
      Uri imageUri = data.getData();

      // Use the ML Kit Text Recognition API to extract text from the image
      InputImage image = null;
      try {
        image = InputImage.fromFilePath(this, imageUri);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      // Create a TextRecognizerOptions object with default settings
      TextRecognizerOptions options = new TextRecognizerOptions.Builder().build();

// Get a TextRecognizer instance with the specified options
      TextRecognizer recognizer = TextRecognition.getClient(options);
      Task<Text> result = recognizer.process(image)
              .addOnSuccessListener(new OnSuccessListener<Text>() {
                @Override
                public void onSuccess(Text visionText) {
                  // Extract the text from the image
                  String extractedText = visionText.getText();
                  Toast.makeText(MainActivity.this, "extractedText: " + extractedText, Toast.LENGTH_LONG).show();

                  // Search for the keyword "total" in the extracted text
                  if (extractedText.toLowerCase().contains("total")) {
                    Toast.makeText(MainActivity.this, "contains: " + extractedText, Toast.LENGTH_LONG).show();
                    Log.d("ggh", "Total value: " + extractedText);
                    String[] s = extractedText.split(" ");
                    for (int i = 0; i < s.length; i++) {
                      String s1 = s[i];
                      if (s1.equals("Total")) {
                        Toast.makeText(MainActivity.this, "Req: " + s[i + 1], Toast.LENGTH_LONG).show();
                      }

                    }
                    Pattern pattern = Pattern.compile("Total\\\\s+(\\\\d+)");
                    Matcher matcher = pattern.matcher(extractedText);


                    if (matcher.find()) {
                      String totalValue = matcher.group(1);
                      Toast.makeText(MainActivity.this, "Total: " + totalValue, Toast.LENGTH_LONG).show();
                    } else {
                      Toast.makeText(MainActivity.this, "Total: Not found", Toast.LENGTH_LONG).show();

                    }
                    // Found the keyword "total", do something with the result
                    // For example, you can use regular expressions to extract the value of the total
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
  }

//  public Bitmap matToBitmap(Mat mat) {
//    Bitmap bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
//    Utils.matToBitmap(mat, bitmap);
//    return bitmap;
//  }
//  private void performOCR(Uri imageUri) {
//    try {
//      // Load the selected image using the imageUri
//      Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
//
//      Mat grayscaleImage = new Mat();
//      Imgproc.cvtColor(new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC4, new Scalar(0)), grayscaleImage, Imgproc.COLOR_RGBA2GRAY);
//
//
//
//      // Create a Tesseract instance
//      TessBaseAPI tessBaseAPI = new TessBaseAPI();
//      tessBaseAPI.init(getExternalFilesDir(null).getAbsolutePath(), "eng");
//      tessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
//
//      // Perform OCR on the grayscale image
//      tessBaseAPI.setImage(matToBitmap(grayscaleImage));
//      String text = tessBaseAPI.getUTF8Text();
//      tessBaseAPI.end();
//
//      // Extract the required values from the OCR text
//      String total = extractTotalValue(text);
//
//      // Display the extracted values
//      Toast.makeText(this, "Total: " + total, Toast.LENGTH_SHORT).show();
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//  }

  private String extractTotalValue(String text) {
    // Implement your logic to extract the total value from the OCR text
    // This can be done using regular expressions or other string manipulation techniques
    // Here's an example implementation using regex:

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
    drawerLayout = (DrawerLayout) findViewById(R.id.drawer);

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

    ListView drawerList = (ListView) findViewById(R.id.drawer_list);
    drawerList.setAdapter(new DrawerListViewAdapter(this));

    onDrawerItemSelected();
  }

  private void onDrawerItemSelected() {
    final ListView drawerList = (ListView) findViewById(R.id.drawer_list);
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
        FrameLayout mainFrame = (FrameLayout) findViewById(R.id.main_frame);
        mainFrame.bringToFront();
      }
    });
  }

  private void configureActionBar() {
    actionBar = getActionBar();
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

    viewPager = (ViewPager) findViewById(R.id.view_pager);
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
    // Create a new Intent to open the gallery
    Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

    // Create a new Intent to open the camera
    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

    // Create a chooser Intent to let the user select between the camera and gallery
    Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Image");
    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { cameraIntent });

    // Start the chooser activity
    startActivityForResult(chooserIntent, 0);
  }

}
