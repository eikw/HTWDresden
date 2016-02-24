package de.htwdd.htwdresden;


import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import de.htwdd.htwdresden.classes.Const;
import de.htwdd.htwdresden.classes.LessonHelper;
import de.htwdd.htwdresden.database.DatabaseManager;
import de.htwdd.htwdresden.database.TimetableUserDAO;
import de.htwdd.htwdresden.interfaces.INavigation;
import de.htwdd.htwdresden.types.Lesson;


/**
 * Fragment für den Schnelleinstieg in die App
 */
public class OverviewFragment extends Fragment {
    private View mLayout;

    public OverviewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mLayout = inflater.inflate(R.layout.fragment_overview, container, false);

        // Setze Toolbartitle
        ((INavigation) getActivity()).setTitle(getResources().getString(R.string.navi_overview));

        // Onclick-Listener
        CardView timetable = (CardView) mLayout.findViewById(R.id.overview_timetable);
        timetable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((INavigation) getActivity()).goToNavigationItem(R.id.navigation_timetable);
            }
        });

        // Stundenplan anzeigen
        showTimetable();

        return mLayout;
    }

    @Override
    public void onResume() {
        super.onResume();
        showTimetable();
    }

    private void showTimetable() {
        Calendar calendar = GregorianCalendar.getInstance();
        TimetableUserDAO timetableUserDAO = new TimetableUserDAO(new DatabaseManager(getActivity()));
        final String[] lessonType = mLayout.getResources().getStringArray(R.array.lesson_type);
        final SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
        final int offset = TimeZone.getDefault().getOffset(new GregorianCalendar().getTimeInMillis());

        // TextViews bestimmen
        final TextView overview_lessons_current_remaining = (TextView) mLayout.findViewById(R.id.overview_lessons_current_remaining);
        final TextView overview_lessons_current_tag = (TextView) mLayout.findViewById(R.id.overview_lessons_current_tag);
        final TextView overview_lessons_current_type = (TextView) mLayout.findViewById(R.id.overview_lessons_current_type);
        final TextView overview_lessons_next_remaining = (TextView) mLayout.findViewById(R.id.overview_lessons_next_remaining);
        final TableRow overview_lessons_busy_plan = (TableRow) mLayout.findViewById(R.id.overview_lessons_busy_plan);
        final LinearLayout overview_lessons_list = (LinearLayout) mLayout.findViewById(R.id.overview_lessons_list);

        // TextViews zurücksetzen
        overview_lessons_current_type.setText(R.string.overview_lessons_noLesson);
        overview_lessons_current_remaining.setVisibility(View.GONE);
        overview_lessons_busy_plan.setVisibility(View.VISIBLE);

        // Aktuelle Stunde bestimmen
        int currentDS = Const.Timetable.getCurrentDS(null);

        // Ist aktuell Vorlesungszeit?
        if (currentDS != 0 && calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            // Suche nach aktuell möglichen Stunden
            ArrayList<Lesson> lessons = timetableUserDAO.getByDS(calendar.get(Calendar.WEEK_OF_YEAR), calendar.get(Calendar.DAY_OF_WEEK) - 1, currentDS);

            if (lessons.size() > 0) {
                overview_lessons_current_remaining.setVisibility(View.VISIBLE);

                // Suche nach einer passenden Veranstaltung
                LessonHelper lessonHelper = new LessonHelper();
                int single = lessonHelper.searchLesson(lessons, calendar.get(Calendar.WEEK_OF_YEAR));

                switch (single) {
                    case 0:
                        // keine passende Stunde gefunden
                        overview_lessons_current_tag.setText(null);
                        overview_lessons_current_remaining.setVisibility(View.GONE);
                        break;
                    case 1:
                        // Genau eine passende Stunden gefunden
                        overview_lessons_current_tag.setText(lessonHelper.lesson.getTag());
                        overview_lessons_current_type.setText(
                                mLayout.getResources().getString(
                                        R.string.timetable_ds_list_simple,
                                        lessonType[lessonHelper.lesson.getTypeInt()],
                                        lessonHelper.lesson.getRooms()));
                        break;
                    case 2:
                        // mehrere passende Stunden gefunden
                        overview_lessons_current_tag.setText(R.string.timetable_moreLessons);
                        break;
                }

                // Verbleibende Zeit anzeigen
                long difference = TimeUnit.MINUTES.convert(Const.Timetable.getMillisecondsWithoutDate(calendar) - (Const.Timetable.endDS[currentDS - 1].getTime() + offset), TimeUnit.MILLISECONDS);
                if (difference < 0)
                    overview_lessons_current_remaining.setText(String.format(getResources().getString(R.string.overview_lessons_remaining_end), -difference));
                else
                    overview_lessons_current_remaining.setText(String.format(getResources().getString(R.string.overview_lessons_remaining_final), difference));
            }
        }

        // Suche nach nächster Veranstaltung
        Calendar calendarNextLesson = GregorianCalendar.getInstance();
        LessonHelper lessonHelper = new LessonHelper();
        int nextDS = currentDS;
        int single;

        // Vorlesungszeit vorbei? Dann auf nächsten Tag springen
        if (Const.Timetable.getMillisecondsWithoutDate(calendar) > Const.Timetable.endDS[7 - 1].getTime()) {
            nextDS = 0;
            calendarNextLesson.add(Calendar.DAY_OF_YEAR, 1);
        }

        do {
            // DS erhöhen
            if ((++nextDS) % 7 == 0) {
                nextDS = 1;
                calendarNextLesson.add(Calendar.DAY_OF_YEAR, 1);
            }

            // Lade Stunde aus DB
            ArrayList<Lesson> lessons = timetableUserDAO.getByDS(calendarNextLesson.get(Calendar.WEEK_OF_YEAR), calendarNextLesson.get(Calendar.DAY_OF_WEEK) - 1, nextDS);

            // Suche nach passender Stunde
            single = lessonHelper.searchLesson(lessons, calendarNextLesson.get(Calendar.WEEK_OF_YEAR));
        }
        // Suche solange nach einer passenden Stunde bis eine Stunde gefunden wurde. Nach über zwei Tagen wird die Suche abgebrochen
        while (single == 0 && (calendarNextLesson.get(Calendar.WEEK_OF_YEAR) - calendar.get(Calendar.WEEK_OF_YEAR)) < 2);

        // Wurde eine nächste Stunde gefunden?
        if (single > 0) {
            //Zeit-Abstand berechen und Stunde anzeigen
            int differenceDay = Math.abs(calendarNextLesson.get(Calendar.DAY_OF_YEAR) - calendar.get(Calendar.DAY_OF_YEAR));

            switch (differenceDay) {
                case 0:
                    long minuten = TimeUnit.MINUTES.convert(Const.Timetable.beginDS[nextDS - 1].getTime() + offset - Const.Timetable.getMillisecondsWithoutDate(calendar), TimeUnit.MILLISECONDS);
                    overview_lessons_next_remaining.setText(String.format(getResources().getString(R.string.overview_lessons_remaining_start), minuten));
                    break;
                case 1:
                    overview_lessons_next_remaining.setText(getString(
                            R.string.overview_lessons_tomorrow,
                            getString(R.string.timetable_ds_list_simple, format.format(Const.Timetable.beginDS[nextDS - 1]), format.format(Const.Timetable.endDS[nextDS - 1]))
                    ));
                    // DS nicht mehr anzeigen
                    currentDS = 0;
                    break;
                default:
                    final String[] nameOfDays = DateFormatSymbols.getInstance().getWeekdays();
                    overview_lessons_next_remaining.setText(getString(
                            R.string.overview_lessons_future,
                            nameOfDays[calendarNextLesson.get(Calendar.DAY_OF_WEEK)],
                            getString(R.string.timetable_ds_list_simple, format.format(Const.Timetable.beginDS[nextDS - 1]), format.format(Const.Timetable.endDS[nextDS - 1]))
                    ));

                    // Vorschau ausblenden
                    overview_lessons_busy_plan.setVisibility(View.GONE);
                    break;
            }

            // Art und Name anzeigen
            if (single == 1) {
                TextView overview_lessons_next_tag = (TextView) mLayout.findViewById(R.id.overview_lessons_next_tag);
                TextView overview_lessons_next_type = (TextView) mLayout.findViewById(R.id.overview_lessons_next_type);

                overview_lessons_next_tag.setText(lessonHelper.lesson.getTag());
                overview_lessons_next_type.setText(
                        mLayout.getResources().getString(
                                R.string.timetable_ds_list_simple,
                                lessonType[lessonHelper.lesson.getTypeInt()],
                                lessonHelper.lesson.getRooms()));
            } else {
                TextView overview_lessons_next_tag = (TextView) mLayout.findViewById(R.id.overview_lessons_next_tag);
                overview_lessons_next_tag.setText(R.string.timetable_moreLessons);
            }
        }

        // Stundenplanvorschau
        if (overview_lessons_busy_plan.getVisibility() == View.VISIBLE) {
            // Daten für Stundenplan-Vorschau
            String[] values = new String[7];
            for (int i = 1; i < 8; i++) {
                ArrayList<Lesson> lessons = timetableUserDAO.getByDS(calendarNextLesson.get(Calendar.WEEK_OF_YEAR), calendarNextLesson.get(Calendar.DAY_OF_WEEK) - 1, i);

                // Suche nach passender Stunde
                single = lessonHelper.searchLesson(lessons, calendar.get(Calendar.WEEK_OF_YEAR));

                switch (single) {
                    case 0:
                        values[i - 1] = "";
                        break;
                    case 1:
                        values[i - 1] = lessonHelper.lesson.getTag() + " (" + lessonHelper.lesson.getType() + ")";
                        break;
                    case 2:
                        values[i - 1] = getResources().getString(R.string.timetable_moreLessons);
                        break;
                }
            }
            // Stundenplanvorschau erstellen
            LessonHelper.createSimpleDayOverviewLayout(getActivity(), overview_lessons_list, null, values, currentDS);
        }
    }
}
