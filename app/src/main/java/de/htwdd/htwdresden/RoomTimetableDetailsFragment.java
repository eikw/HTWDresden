package de.htwdd.htwdresden;


import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
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
 *
 * @author Kay Förster
 */
public class RoomTimetableDetailsFragment extends Fragment {
    private List<TabItem> mTabs = new ArrayList<>();
    private String room;

    public RoomTimetableDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle bundle = getArguments();
        final Bundle bundleCurrentWeek = new Bundle();
        final Bundle bundleNextWeek = new Bundle();
        final Calendar calendar = GregorianCalendar.getInstance(Locale.GERMANY);
        bundleCurrentWeek.putInt(Const.BundleParams.TIMETABLE_WEEK, calendar.get(Calendar.WEEK_OF_YEAR));
        calendar.add(Calendar.WEEK_OF_YEAR, 1);
        bundleNextWeek.putInt(Const.BundleParams.TIMETABLE_WEEK, calendar.get(Calendar.WEEK_OF_YEAR));

        room = bundle.getString(Const.BundleParams.ROOM_TIMETABLE_ROOM, "");
        bundleCurrentWeek.putString(Const.BundleParams.ROOM_TIMETABLE_ROOM, room);
        bundleNextWeek.putString(Const.BundleParams.ROOM_TIMETABLE_ROOM, room);

        mTabs.add(new TabItem(
                getResources().getString(R.string.timetable_tab_current_week, bundleCurrentWeek.getInt(Const.BundleParams.TIMETABLE_WEEK)),
                RoomTimetableOverviewFragment.class,
                bundleCurrentWeek
        ));
        mTabs.add(new TabItem(
                getResources().getString(R.string.timetable_tab_next_week, bundleNextWeek.getInt(Const.BundleParams.TIMETABLE_WEEK)),
                RoomTimetableOverviewFragment.class,
                bundleNextWeek
        ));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_tabs, container, false);
        final ViewPager viewPager = (ViewPager) view.findViewById(R.id.viewpager);

        // Setze Title der Toolbar
        ((INavigation)getActivity()).setTitle(getResources().getString(R.string.room_timetable_details_title, room));

        // Adapter für Tabs erstellen und an View hängen
        viewPager.setAdapter( new ViewPagerAdapter(getFragmentManager(), mTabs));

        // TabLayout "stylen"
        final TabLayout tabLayout = (TabLayout) view.findViewById(R.id.sliding_tabs);
        // Setze feste Anzahl an Tabs (Tabs wirken nicht abgeklatscht)
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        // Tabs nahmen immer die ganze Breite ein
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(viewPager);

        return view;
    }
}
