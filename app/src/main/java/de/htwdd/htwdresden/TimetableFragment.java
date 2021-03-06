package de.htwdd.htwdresden;


import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import de.htwdd.htwdresden.adapter.ViewPagerAdapter;
import de.htwdd.htwdresden.classes.Const;
import de.htwdd.htwdresden.interfaces.INavigation;
import de.htwdd.htwdresden.types.TabItem;


/**
 * Fragment, welches verschiedene Tabs mit einzelnen Wochen enthält
 */
public class TimetableFragment extends Fragment {
    private List<TabItem> mTabs = new ArrayList<>();
    private ViewPagerAdapter pagerAdapter;
    private final Bundle bundleCurrentWeek = new Bundle();
    private final Bundle bundleNextWeek = new Bundle();

    public TimetableFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Kalenderwoche für Bundle bestimmen
        final Calendar calendar = GregorianCalendar.getInstance(Locale.GERMANY);
        bundleCurrentWeek.putInt(Const.BundleParams.TIMETABLE_WEEK, calendar.get(Calendar.WEEK_OF_YEAR));
        calendar.add(Calendar.WEEK_OF_YEAR, 1);
        bundleNextWeek.putInt(Const.BundleParams.TIMETABLE_WEEK, calendar.get(Calendar.WEEK_OF_YEAR));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, @Nullable final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_tabs, container, false);
        final ViewPager viewPager = view.findViewById(R.id.viewpager);

        // Setze Title der Toolbar
        ((INavigation)getActivity()).setTitle(getResources().getString(R.string.navi_timetable));
        // Optionsmenü aktivieren
        setHasOptionsMenu(true);

        // Zustand wiederherstellen
        if (savedInstanceState != null) {
            bundleCurrentWeek.putAll(savedInstanceState);
            bundleNextWeek.putAll(savedInstanceState);
        }

        // Adapter für Tabs erstellen und an View hängen
        replaceTabs();
        pagerAdapter = new ViewPagerAdapter(getChildFragmentManager(), mTabs);
        viewPager.setAdapter(pagerAdapter);

        // TabLayout "stylen"
        final TabLayout tabLayout = view.findViewById(R.id.sliding_tabs);
        // Setze feste Anzahl an Tabs (Tabs wirken nicht abgeklatscht)
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        // Tabs nahmen immer die ganze Breite ein
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(viewPager);

        return view;
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(Const.BundleParams.TIMETABLE_FILTER_CURRENT_WEEK, bundleCurrentWeek.getBoolean(Const.BundleParams.TIMETABLE_FILTER_CURRENT_WEEK, true));
        outState.putBoolean(Const.BundleParams.TIMETABLE_FILTER_SHOW_HIDDEN, bundleCurrentWeek.getBoolean(Const.BundleParams.TIMETABLE_FILTER_SHOW_HIDDEN, false));
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.menu_timetable, menu);
        menu.findItem(R.id.menu_filter_week).setChecked(bundleCurrentWeek.getBoolean(Const.BundleParams.TIMETABLE_FILTER_CURRENT_WEEK, true));
        menu.findItem(R.id.menu_filter_hidden).setChecked(bundleCurrentWeek.getBoolean(Const.BundleParams.TIMETABLE_FILTER_SHOW_HIDDEN, false));
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int itemId = item.getItemId();
        // Item Status umdrehen
        final boolean filterWeek = !item.isChecked();
        item.setChecked(filterWeek);

        switch (itemId) {
            case R.id.menu_filter_week:
                // Parameter für Fragments aktualisieren
                bundleCurrentWeek.putBoolean(Const.BundleParams.TIMETABLE_FILTER_CURRENT_WEEK, filterWeek);
                bundleNextWeek.putBoolean(Const.BundleParams.TIMETABLE_FILTER_CURRENT_WEEK, filterWeek);
                // Tabs ersetzen
                replaceTabs();
                pagerAdapter.notifyDataSetChanged();
                return true;
            case R.id.menu_filter_hidden:
                // Parameter für Fragments aktualisieren
                bundleCurrentWeek.putBoolean(Const.BundleParams.TIMETABLE_FILTER_SHOW_HIDDEN, filterWeek);
                bundleNextWeek.putBoolean(Const.BundleParams.TIMETABLE_FILTER_SHOW_HIDDEN, filterWeek);
                // Tabs ersetzen
                replaceTabs();
                pagerAdapter.notifyDataSetChanged();
                return true;
            case R.id.menu_timetable_reset:
                TimetableResetDialogFragment.newInstance().show(getFragmentManager(), "timetableResetDialog");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void replaceTabs() {
        final Resources resources = getResources();
        mTabs.clear();

        if (bundleCurrentWeek.getBoolean(Const.BundleParams.TIMETABLE_FILTER_CURRENT_WEEK, true)) {
            mTabs.add(new TabItem(
                    resources.getString(R.string.timetable_tab_current_week, bundleCurrentWeek.getInt(Const.BundleParams.TIMETABLE_WEEK)),
                    TimetableOverviewFragment.class,
                    bundleCurrentWeek
            ));
            mTabs.add(new TabItem(
                    resources.getString(R.string.timetable_tab_next_week, bundleNextWeek.getInt(Const.BundleParams.TIMETABLE_WEEK)),
                    TimetableOverviewFragment.class,
                    bundleNextWeek
            ));
        } else {
            mTabs.add(new TabItem(
                    resources.getString(R.string.timetable_tab_even_week),
                    TimetableOverviewFragment.class,
                    bundleCurrentWeek
            ));
            mTabs.add(new TabItem(
                    resources.getString(R.string.timetable_tab_odd_week),
                    TimetableOverviewFragment.class,
                    bundleNextWeek
            ));
        }
    }
}
