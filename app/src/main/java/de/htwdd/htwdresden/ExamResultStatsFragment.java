package de.htwdd.htwdresden;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import de.htwdd.htwdresden.adapter.ExamStatsAdapter;
import de.htwdd.htwdresden.types.ExamResult;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Fragment zur Anzeige der Statistik der Prüfungsergebnisse
 */
public class ExamResultStatsFragment extends Fragment {
    private Realm realm;
    private RealmResults<ExamResult> allExamResults;
    private RealmChangeListener<RealmResults<ExamResult>> realmChangeListener;

    public ExamResultStatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        final View mLayout = inflater.inflate(R.layout.listview_swipe_refresh, container, false);
        realm = Realm.getDefaultInstance();

        // Refresh ausschalten
        final SwipeRefreshLayout swipeRefreshLayout = mLayout.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setEnabled(false);

        // Hinweismeldung wenn keine Ergebnisse vorliegen
        final TextView message = mLayout.findViewById(R.id.message_info);
        message.setText(R.string.exams_result_no_results);

        // Adapter erstellen und an Liste anhängen
        final ExamStatsAdapter adapter = new ExamStatsAdapter(getActivity(), realm);
        final ListView listView = mLayout.findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setEmptyView(message);

        // Auf Änderungen an der Datenbank hören
        realmChangeListener = new RealmChangeListener<RealmResults<ExamResult>>() {
            @Override
            public void onChange(@NonNull final RealmResults<ExamResult> element) {
                adapter.notifyDataSetChanged();
            }
        };
        allExamResults = realm.where(ExamResult.class).findAll();
        allExamResults.addChangeListener(realmChangeListener);

        return mLayout;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        allExamResults.removeChangeListener(realmChangeListener);
        realm.close();
    }
}
