package org.fossasia.openevent.app.core.main;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;

import org.fossasia.openevent.app.R;
import org.fossasia.openevent.app.common.mvp.view.BaseInjectActivity;
import org.fossasia.openevent.app.core.auth.AuthActivity;
import org.fossasia.openevent.app.core.organizer.detail.OrganizerDetailActivity;
import org.fossasia.openevent.app.data.event.Event;
import org.fossasia.openevent.app.data.auth.model.User;
import org.fossasia.openevent.app.databinding.MainActivityBinding;
import org.fossasia.openevent.app.databinding.MainNavHeaderBinding;
import org.fossasia.openevent.app.ui.BackPressHandler;
import org.fossasia.openevent.app.ui.ViewUtils;

import javax.inject.Inject;

import dagger.Lazy;

public class MainActivity extends BaseInjectActivity<MainPresenter> implements NavigationView.OnNavigationItemSelectedListener, MainView {

    public static final String EVENT_KEY = "event";
    private long eventId = -1;

    @Inject
    Lazy<MainPresenter> presenterProvider;
    @Inject
    BackPressHandler backPressHandler;

    private FragmentNavigator fragmentNavigator;
    private DrawerNavigator drawerNavigator;

    private MainActivityBinding binding;
    private MainNavHeaderBinding headerBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.main_activity);

        headerBinding = MainNavHeaderBinding.bind(binding.navView.getHeaderView(0));

        setSupportActionBar(binding.main.toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.main.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        binding.navView.setNavigationItemSelectedListener(this);

        binding.navView.getMenu().setGroupVisible(R.id.subMenu, false);
        fragmentNavigator = new FragmentNavigator(getSupportFragmentManager(), eventId);
        drawerNavigator = new DrawerNavigator(this, fragmentNavigator, getPresenter());

        headerBinding.profile.setOnClickListener(view -> startActivity(new Intent(this, OrganizerDetailActivity.class)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        getPresenter().attach(this);
        getPresenter().start();
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else if (fragmentNavigator.isDashboardActive()) {
            backPressHandler.onBackPressed(this, super::onBackPressed);
        } else {
            fragmentNavigator.back();
            binding.navView.getMenu().findItem(R.id.nav_dashboard).setChecked(true);
            getSupportActionBar().setTitle(R.string.dashboard);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        binding.drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

                binding.navView.setCheckedItem(item.getItemId());
                drawerNavigator.selectItem(item);
                binding.drawerLayout.removeDrawerListener(this);
            }
        });
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public Lazy<MainPresenter> getPresenterProvider() {
        return presenterProvider;
    }

    @Override
    public void setEventId(long eventId) {
        this.eventId = eventId;
        fragmentNavigator.setEventId(eventId);
        binding.navView.getMenu().setGroupVisible(R.id.subMenu, true);
    }

    @Override
    public void showEventList() {
        loadFragment(R.id.nav_events);
    }

    @Override
    public void showDashboard() {
        loadFragment(R.id.nav_dashboard);
    }

    @Override
    public void showOrganizer(User organizer) {
        headerBinding.setUser(organizer);
    }

    @Override
    public void invalidateDateViews() {
        headerBinding.invalidateAll();
    }

    @Override
    public void showResult(Event event) {
        headerBinding.setEvent(event);
    }

    @Override
    public void onLogout() {
        startActivity(new Intent(this, AuthActivity.class));
        finish();
    }

    @Override
    public void showError(String error) {
        ViewUtils.showSnackbar(binding.getRoot(), error);
    }

    private void loadFragment(int navItemId) {
        binding.navView.setCheckedItem(navItemId);

        fragmentNavigator.loadFragment(navItemId);
    }
}
