# Android Form Gotchas

## Relevant Files
- [activity_main.xml](https://github.com/MauroHorie/Android-Form-Gotchas/blob/master/app/src/main/res/layout/activity_main.xml)
- [MainActivity.java](https://github.com/MauroHorie/Android-Form-Gotchas/blob/master/app/src/main/java/com/mkhorie/formgotchas/MainActivity.java)
- [AndroidManifest.xml](https://github.com/MauroHorie/Android-Form-Gotchas/blob/master/app/src/main/AndroidManifest.xml)

## Specs

What we are trying to accomplish:

 1. None of the EditTexts should gain focus when the page opens. The soft keyboard should also not open when the page first opens.
 2. The page content should be scrollable when the soft keyboard opens
 3. The content should be inside a 248dp-width window
 4. The content should be centered horizontally and vertically
 5. There should be a 40dp minimum padding between the content and the top and bottom edges of the screen
 6. In landscape orientation, the EditText is not full-screen
 7. The action button in each EditText will move focus to the following EditText, except for the last EditText, which will submit the form
 8. Tapping outside an EditText closes the soft keyboard.
 9. The status bar should be transparent

## Solutions

#### 1. None of the EditTexts should gain focus when the page opens. The soft keyboard should also not open when the page first opens

The solution is to consume the focus at the root level of the layout:
```xml
<android.support.design.widget.CoordinatorLayout
    ...
    android:focusableInTouchMode="true"
```

There are other solutions online pointing to:
```xml
<!-- In AndroidManifest.xml -->
<activity
    ...
    android:windowSoftInputMode="stateHidden"
```
but this only hides the keyboard. It does not prevent the first EditText from gaining focus. This leads to undesired effects such as the cursor being present in the EditText. 
<br/>
<br/>

#### 2. The page content should be scrollable when the soft keyboard opens

This requires a combination of two things: 

1. Declare the Activity's soft input mode as ```adjustResize```.
```xml
<!-- In AndroidManifest.xml -->
<activity
    ...
    android:windowSoftInputMode="adjustResize"
```

2. Add the content into a ```ScrollView``` and set its height to ```wrap_content```.
```xml
<android.support.design.widget.CoordinatorLayout
    ...>
    <ScrollView
        android:id="@+id/scrollView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_gravity="center">
```
<br/>

#### 3. The content should be inside a 248dp-width window

We simply set the dimension in ```dimens.xml``` as 
```
<resources>
    <dimen name="form_width">248dp</dimen>
```

and referenced in the layout file: (although the width could be directly coded in the layout file)

```xml
<android.support.design.widget.CoordinatorLayout
    ...>
    <ScrollView
        ...>
        <FrameLayout
            android:layout_width="@dimen/form_width"
            android:layout_height="wrap_content">
```
<br/> 

#### 4. The content should be centered horizontally and vertically

To center the content vertically, we set the ```ScrollView``` to ```wrap_content``` vertically and we set its ```layout_gravity``` to ```center```.

```xml
android:layout_height="wrap_content"
android:layout_gravity="center"
```

Note that the ```android:layout_width``` was set to ```match_parent```. That is because we want the scroll bar to be at the edge of the screen.

To center the content horizontally, we simply set the content container as a ```FrameLayout``` and we center it horizontally. 

```xml
<android.support.design.widget.CoordinatorLayout
    ...>
    <ScrollView
        ...>
        <FrameLayout
            android:layout_width="@dimen/form_width"
            android:layout_height="wrap_content"
            android:layout_gravity="center_horizontal">
```
<br/> 

#### 5. There should be a 40dp minimum padding between the content and the top and bottom edges of the screen

To achieve this, the first thing that came to mind was to set ```padding``` or ```margin``` to the ```FrameLayout```. However, this caused the layout resizing caused by the soft keyboard to incorrectly reposition some elements. That is because the FrameLayout is a direct child to the ```ScrollView```.

The solution was to move the ```padding``` one level down and we set it to the ```LinearLayout``` immediately inside the ```FrameLayout```.

```xml
<android.support.design.widget.CoordinatorLayout
    ...>
    <ScrollView
        ...>
        <FrameLayout
            android:layout_width="@dimen/form_width"
            android:layout_height="wrap_content"
            android:layout_gravity="center_horizontal"
            /*padding or margin here causes problems*/
            ...>
            <LinearLayout
                android:layout_height="wrap_content"
                android:layout_width="match_parent"
                android:paddingTop="40dp"
                android:paddingBottom="40dp"
                android:orientation="vertical">
```
<br/>

#### 6. In landscape orientation, the EditText is not full-screen

Simply add the ```flagNoExtractUi``` IME option to the ```EditText```.
```xml
android:imeOptions="flagNoExtractUi"
```
<br/>

#### 7. The action button in each EditText will move focus to the following EditText, except for the last EditText, which will submit the form

Simply add the action to the ```imeOption``` to the ```EditText```.
```xml
android:imeOptions="flagNoExtractUi|actionNext"
```

and
```xml
android:imeOptions="flagNoExtractUi|actionGo"
```

and implement the ```TextView.OnEditorActionListener``` interface in the ```Activity```.

```java
public class MainActivity extends AppCompatActivity implements   TextView.OnEditorActionListener {
    ...
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ...
        EditText repeatPasswordEditText = (EditText) findViewById(R.id.editTextRepeatPassword);
        repeatPasswordEditText.setOnEditorActionListener(this);
        ...
    }
    ...
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        switch(v.getId()) {
            case R.id.editTextRepeatPassword:
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    signUp();
                    return true;
                }
                break;
        }
        return false;
    }
```
<br/>

#### 8. Tapping outside an EditText closes the soft keyboard.

When tapping outside an EditText, the user could be tapping either the ```ScrollView``` or the ```CoordinatorLayout``` since the ```ScrollView```'s height is set to ```wrap_content```. 

Our solution consists of 1) making the CoordinatorLayout clickable, and 2) faking it and making the ScrollView clickable by adding a clickable layer immediately underneath it. 

```xml
<!-- Slightly refactored from earlier -->
<android.support.design.widget.CoordinatorLayout
    ...>
    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        ...>
        <!-- Note that the FrameLayout has the same size as the ScrollView -->
        <FrameLayout
            android:id="@+id/touchContainer"
            android:layout_width="match_parent"
            android:layout_height="wrap_content">
            <LinearLayout
                android:layout_height="wrap_content"
                android:layout_width="@dimen/form_width"
                android:layout_gravity="center_horizontal"
                android:paddingTop="40dp"
                android:paddingBottom="40dp"
                android:orientation="vertical">
```

and in MainActivity.java:

```java
public class MainActivity extends AppCompatActivity implements ..., View.OnClickListener {
    ...
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ...
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.layoutRoot);
        coordinatorLayout.setOnClickListener(this);
        ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
        scrollView.setOnTouchListener(this);
    }
    ...
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.layoutRoot:
            case R.id.touchContainer:
                closeSoftKeyboard();
                break;
            ...
        }
    }
    ...
    private void closeSoftKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.clearFocus();
        }
    }
```
<br/>

#### 9. The status bar should be transparent

Add the style in styles.xml. The ```colorPrimaryDark``` property is the status bar color.
```xml
<resources>
    ...
    <!-- Name it whatever you want -->
    <style name="AppTheme.NoActionBar.FullScreen" parent="Theme.AppCompat.Light.NoActionBar">
        <item name="android:windowBackground">@color/windowBackground</item>
        <item name="colorPrimaryDark">@android:color/transparent</item>
    </style>
```

Don't forget to add the color to colors.xml
```xml
<resources>
    <color name="windowBackground">#AAAAAA</color>
```

Then apply to the ```Activity``` in the AndroidManifest.xml:
```xml
<manifest
    ...>
    <application 
        ...>
        <activity
            ...
            android:theme="@style/AppTheme.NoActionBar.FullScreen"
```