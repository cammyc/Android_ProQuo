package com.scalpr.scalpr;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.lapism.searchview.SearchHistoryTable;
import com.lapism.searchview.SearchItem;
import com.lapism.searchview.SearchView;
import com.scalpr.scalpr.Helpers.UserHelper;
import com.scalpr.scalpr.Objects.HttpResponseListener;
import com.scalpr.scalpr.Objects.User;

import org.w3c.dom.Text;


public abstract class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    protected static final String EXTRA_KEY_VERSION = "version";
    protected static final String EXTRA_KEY_THEME = "theme";

    protected static final int NAV_ITEM_INVALID = -1;
    protected static final int NAV_ITEM_TOOLBAR = 0;
    protected static final int NAV_ITEM_TOOLBAR_DARK = 1;
    protected static final int NAV_ITEM_MENU_ITEM = 2;
    protected static final int NAV_ITEM_MENU_ITEM_DARK = 3;
    protected static final int NAV_ITEM_TOGGLE = 4;
    protected static final int NAV_ITEM_HISTORY_TOGGLE = 5;
    protected static final int NAV_ITEM_FILTERS = 6;

    private static final String EXTRA_KEY_VERSION_MARGINS = "version_margins";
    private static final String EXTRA_KEY_TEXT = "text";

    protected SearchView mSearchView = null;
    protected DrawerLayout mDrawerLayout = null;
    protected Toolbar mToolbar = null;
    final Context context = this;
    private SearchHistoryTable mHistoryDatabase;

    protected boolean userIsLoggedIn;
    protected User loggedInUser;
    protected UserHelper loginHelp;
    protected FloatingActionButton mFab = null;

    // ---------------------------------------------------------------------------------------------
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        getToolbar();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setDrawer();
        setNavigationView(userIsLoggedIn);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_account_settings) {
            Intent intent = new Intent(getApplicationContext(), EditAccountDetailsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_login) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        }else if(id == R.id.nav_log_out){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    context);

            // set title
            alertDialogBuilder.setTitle("Logout");

            // set dialog message
            alertDialogBuilder
                    .setMessage("Are you sure you want to logout?")
                    .setCancelable(false)
                    .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            dialog.dismiss();

                            HttpResponseListener listener = new HttpResponseListener() {
                                @Override
                                public void requestStarted() {

                                }

                                @Override
                                public void requestCompleted(String response) {
                                    if(response.equals("1")){
                                        loginHelp.Logout(); //if no token just log out
                                    }else{
                                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                                context);

                                        // set title
                                        alertDialogBuilder.setTitle("Logout Error");

                                        // set dialog message
                                        alertDialogBuilder
                                                .setMessage("Unable to logout without a network connection.")
                                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.dismiss();
                                                    }
                                                })
                                                .setCancelable(false);

                                        // create alert dialog
                                        final AlertDialog alertDialog = alertDialogBuilder.create();
                                        alertDialog.show();
                                    }
                                }

                                @Override
                                public void requestEndedWithError(VolleyError error) {
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                            context);

                                    // set title
                                    alertDialogBuilder.setTitle("Logout Error");

                                    // set dialog message
                                    alertDialogBuilder
                                            .setMessage("Unable to logout without a network connection.")
                                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.dismiss();
                                                }
                                            })
                                            .setCancelable(false);

                                    // create alert dialog
                                    final AlertDialog alertDialog = alertDialogBuilder.create();
                                    alertDialog.show();
                                }
                            };

                            String token = loginHelp.getFirebaseToken();

                            if(token != null) {
                                loginHelp.removeFirebaseLoginToken(listener, token);
                            }else{
                                loginHelp.Logout(); //if no token just log out
                            }
                        }
                    })
                    .setNegativeButton("No",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.setCanceledOnTouchOutside(true);
            alertDialog.show();
        }else if(id == R.id.nav_my_tickets){
            Intent intent = new Intent(getApplicationContext(), EditAttractions.class);
            startActivity(intent);
        }else if (id == R.id.nav_search){
            mSearchView.open(true);
        }else if (id == R.id.nav_post_ticket){
            mFab.performClick();
        }else if (id == R.id.nav_contact_us){
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"support@proquoapp.com"});
            String specs = "";

            try {
                specs += "Manufacturer: " + Build.MANUFACTURER + "\n";
                specs += "Brand: " + Build.BRAND + "\n";
                specs += "Model: " + Build.MODEL + "\n";
                specs += "SDK: " + Build.VERSION.SDK_INT + "\n";
                specs += "OS Version: " + Build.VERSION.RELEASE + "\n";
                i.putExtra(Intent.EXTRA_TEXT , specs);

                startActivity(Intent.createChooser(i, "Select your primary email application..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, "Please Download an email application.", Toast.LENGTH_SHORT).show();
            }
        }
        else if (id == R.id.nav_messages){
            Intent intent = new Intent(getApplicationContext(), ConversationsActivity.class);
            startActivity(intent);
        }

//        transaction.replace(R.id.fragment_container, LoginFragment.newInstance("",""));
//        transaction.addToBackStack(null);
//        transaction.commit();
        mDrawerLayout.closeDrawer(GravityCompat.START); // mDrawer.closeDrawers();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    /*
                if (mSearchView != null && mSearchView.isSearchOpen()) {
                // mSearchView.close(true);
    */

    // ---------------------------------------------------------------------------------------------
    protected int getNavItem() {
        return NAV_ITEM_INVALID;
    }

    private void getToolbar() {
        if (mToolbar == null) {
            mToolbar = (Toolbar) findViewById(R.id.toolbar);
            if (mToolbar != null) {
                mToolbar.setNavigationContentDescription(getResources().getString(R.string.app_name));
                setSupportActionBar(mToolbar);
            }
        }
    }

    // ---------------------------------------------------------------------------------------------


    private void setDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout != null) {
            mDrawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() { // new DrawerLayout.DrawerListener();
                @Override
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    invalidateOptionsMenu();
                    if (mSearchView != null && mSearchView.isSearchOpen()) {
                        mSearchView.close(true);
                    }
                }

                @Override
                public void onDrawerClosed(View drawerView) {
                    super.onDrawerClosed(drawerView);
                    invalidateOptionsMenu();
                }
            });
        }
    }

    private void setNavigationView(boolean userIsLoggedIn) {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        if(userIsLoggedIn){
            navigationView.inflateMenu(R.menu.nav_menu_logged_in);
        }else{
            navigationView.inflateMenu(R.menu.nav_menu_logged_out);
        }

        updateMenuHeader(loginHelp.getLoggedInUser());

        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
            if (getNavItem() > -1) {
                navigationView.getMenu().getItem(getNavItem()).setChecked(true);
            }
        }
    }

    protected void updateMenuHeader(User updatedUser){
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerLayout = navigationView.getHeaderView(0);


        TextView tvInitials = (TextView) headerLayout.findViewById(R.id.tvNavInitials);
        TextView tvName = (TextView) headerLayout.findViewById(R.id.tvNavFullName);

        if(updatedUser.getUserID() != 0){
            tvInitials.setText(updatedUser.getFirstName().toUpperCase().substring(0,1) + updatedUser.getLastName().toUpperCase().substring(0,1) + "");

            tvName.setText(updatedUser.getFirstName() + " " + updatedUser.getLastName());
        }else{
            tvInitials.setText("?");

            tvName.setText("Not Logged In");
        }

    }

    protected void setSearchView() {
        mHistoryDatabase = new SearchHistoryTable(this);

        mSearchView = (SearchView) findViewById(R.id.searchView);
        if (mSearchView != null) {
            mSearchView.setVersion(SearchView.VERSION_TOOLBAR);
            mSearchView.setVersionMargins(SearchView.VERSION_MARGINS_TOOLBAR_BIG);
            mSearchView.setHint(R.string.search);
            mSearchView.setTextSize(16);
            mSearchView.setDivider(false);
            mSearchView.setVoice(true);
//            mSearchView.setVoiceText("Set permission on Android 6+ !");
            mSearchView.setAnimationDuration(SearchView.ANIMATION_DURATION);
            mSearchView.setShadowColor(ContextCompat.getColor(this, R.color.search_shadow_layout));
            mSearchView.setShouldClearOnClose(false);
            mSearchView.setShouldClearOnOpen(false);


            mSearchView.setOnOpenCloseListener(new SearchView.OnOpenCloseListener() {
                @Override
                public void onOpen() {
                }

                @Override
                public void onClose() {
                }
            });

//            if (mSearchView.getAdapter() == null) {
//                List<SearchItem> suggestionsList = new ArrayList<>();
//
//                SearchAdapter searchAdapter = new SearchAdapter(this, suggestionsList);
//                searchAdapter.addOnItemClickListener(new SearchAdapter.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(View view, int position) {
//                        TextView textView = (TextView) view.findViewById(R.id.textView_item_text);
//                        String query = textView.getText().toString();
//                        getData(query, position);
//                        mSearchView.setQuery(query);
//                        mSearchView.close(false);
//                    }
//                });
//                mSearchView.setAdapter(searchAdapter);
//            }

            /*
            List<SearchFilter> filter = new ArrayList<>();
            filter.add(new SearchFilter("Filter1", true));
            filter.add(new SearchFilter("Filter2", true));
            mSearchView.setFilters(filter);
            //use mSearchView.getFiltersStates() to consider filter when performing search
            */
        }
    }

    protected void customSearchView() {
        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mSearchView.setVersion(extras.getInt(EXTRA_KEY_VERSION));
            mSearchView.setVersionMargins(extras.getInt(EXTRA_KEY_VERSION_MARGINS));
            mSearchView.setTheme(extras.getInt(EXTRA_KEY_THEME), true);
            mSearchView.setTextInput(extras.getString(EXTRA_KEY_TEXT));
        }
    }

    @CallSuper
    protected void getData(String text, int position) {
        try{
            mHistoryDatabase.addItem(new SearchItem(text));
        }catch (Exception ex){
            Log.d("ERROR", ex.toString());
        }
/*
        Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
        intent.putExtra(EXTRA_KEY_VERSION, SearchView.VERSION_TOOLBAR);
        intent.putExtra(EXTRA_KEY_VERSION_MARGINS, SearchView.VERSION_MARGINS_TOOLBAR_SMALL);
        intent.putExtra(EXTRA_KEY_THEME, SearchView.THEME_LIGHT);
        intent.putExtra(EXTRA_KEY_TEXT, text);
        startActivity(intent);*/

        //Toast.makeText(getApplicationContext(), text + ", position: " + position, Toast.LENGTH_SHORT).show();
    }

    protected void setNightMode(@AppCompatDelegate.NightMode int nightMode) {
        AppCompatDelegate.setDefaultNightMode(nightMode);
        recreate();
    }

}