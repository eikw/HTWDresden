package de.htwdd.htwdresden;


import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import de.htwdd.htwdresden.classes.Const;
import de.htwdd.htwdresden.classes.VolleyDownloader;
import de.htwdd.htwdresden.classes.semesterplan.FreeDay;
import de.htwdd.htwdresden.classes.semesterplan.SemesterPlan;
import de.htwdd.htwdresden.interfaces.INavigation;


/**
 * Fragement zur Übersicht aller wichtigen Uni-Einrichtungen
 */
public class ManagementFragment extends Fragment {

    public static final String SHARED_PREFS_SEMESTER_PLAN_JSON = "de.htwdd.htwdresden.SemestePlanJSON";
    public static final String SHARED_PREFS_CATCH_DATE = "de.htwdd.htwdresden.catchDate";

    public ManagementFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_management, container, false);

        // Setze Toolbartitle
        ((INavigation) getActivity()).setTitle(getResources().getString(R.string.navi_uni_administration));

        CardView management_office = (CardView) view.findViewById(R.id.management_office);
        management_office.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.htw-dresden.de/hochschule/zentrale-verwaltung-dezernate/dezernat-studienangelegenheiten/studentensekretariat.html"));
                getActivity().startActivity(browserIntent);
            }
        });

        CardView management_printing = (CardView) view.findViewById(R.id.management_printing);
        management_printing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.htw-dresden.de/intern/technik-druck-it-dienste/drucken-kopieren.html"));
                getActivity().startActivity(browserIntent);
            }
        });

        CardView management_examination_office = (CardView) view.findViewById(R.id.management_examination_office);
        management_examination_office.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.htw-dresden.de/hochschule/zentrale-verwaltung-dezernate/dezernat-studienangelegenheiten/pruefungsamt.html"));
                getActivity().startActivity(browserIntent);
            }
        });

        CardView management_stura = (CardView) view.findViewById(R.id.management_stura);
        management_stura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.stura.htw-dresden.de/"));
                getActivity().startActivity(browserIntent);
            }
        });



        final SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

        final Response.Listener<JSONArray> jsonArrayListener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject semesterPlanJSON = response.getJSONObject(i);
                        SemesterPlan s = new SemesterPlan(semesterPlanJSON);
                        System.out.println(s.isThisSemester(getYear(), getSemester()));
                        if (s.isThisSemester(getYear(), getSemester())) {
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString(SHARED_PREFS_SEMESTER_PLAN_JSON, semesterPlanJSON.toString());
                            editor.commit();
                            System.out.println(s);
                            setSemesterPlanView(s, getView());
                        }
                    } catch (JSONException e) {
                        Log.e("JSON SEMESTERPLAN", "JSON IS BROKEN");
                        e.printStackTrace();
                    }
                }
            }
        };

        final Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Bestimme Fehlermeldung
                int responseCode = VolleyDownloader.getResponseCode(error);

                // Fehlermeldung anzeigen
                String message;
                switch (responseCode) {
                    case Const.internet.HTTP_TIMEOUT:
                        message = getString(R.string.info_internet_timeout);
                        break;
                    case Const.internet.HTTP_NO_CONNECTION:
                    case Const.internet.HTTP_NOT_FOUND:
                        message = getString(R.string.info_internet_no_connection);
                        break;
                    case Const.internet.HTTP_NETWORK_ERROR:
                    default:
                        message = getString(R.string.info_internet_error);
                }
            }
        };

        final String url = "https://www2.htw-dresden.de/~app/API/semesterplan.json";

        long currentTime = System.currentTimeMillis();


        long highScore = sharedPref.getLong(SHARED_PREFS_CATCH_DATE, -1);
        System.out.println("Long: " + highScore);
        if (!sharedPref.contains(SHARED_PREFS_CATCH_DATE)) {
            //System.out.println("NO VALUE YET");
            writeToSharedPrefAndSendReq(jsonArrayListener, errorListener, url, sharedPref);
        } else {
            //System.out.println("VALUE exists");
            //wenn mehr als 3 Wochen dann mach Req wieder
            if ((currentTime - sharedPref.getLong(SHARED_PREFS_CATCH_DATE, -1)) >= Const.semesterPlanUpdater.UPDATE_INTERVAL) {
                writeToSharedPrefAndSendReq(jsonArrayListener, errorListener, url, sharedPref);
            } else System.out.println("TAKEN FROM SCHARED_PREFS");
        }

        updateSemPlanView(view, sharedPref);

        final SwipeRefreshLayout swipeRefrSemPlan= (SwipeRefreshLayout) view.findViewById(R.id.swipeRefr_SemPlan);
        swipeRefrSemPlan.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i("REFRESH CALLED", "onRefresh called from SwipeRefreshLayout");

                        writeToSharedPrefAndSendReq(jsonArrayListener, errorListener, url, sharedPref);
                        updateSemPlanView(view, sharedPref);
                        swipeRefrSemPlan.setRefreshing(false);
                    }
                }
        );

        return view;
    }

    private void updateSemPlanView(View view, SharedPreferences sharedPref) {
        try {
            String sJSON = sharedPref.getString(SHARED_PREFS_SEMESTER_PLAN_JSON, "LEEEEEEEEEEEERRRRRRRRR");
            System.out.println(sJSON);
            JSONObject jsonObject = new JSONObject(sJSON);
            SemesterPlan semesterPlan = new SemesterPlan(jsonObject);
            setSemesterPlanView(semesterPlan, view);
        } catch (JSONException e) {
            System.out.println("StundenPlanJSON not ready yet");
        }
    }

    private int getYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    private void setSemesterPlanView(SemesterPlan s, View view) {
        final TextView semesterplanBezeichnung = (TextView) view.findViewById(R.id.semesterplan_Bezeichnung);
        final TextView semesterplanLecturePeriod = (TextView) view.findViewById(R.id.semesterplan_lecturePeriod);
        final TextView semesterplanFreieTage = (TextView) view.findViewById(R.id.semesterplan_freieTage);
        final TextView semesterplanPruefPeriod = (TextView) view.findViewById(R.id.semesterplan_pruefPeriod);
        final TextView semesterplanRegistration = (TextView) view.findViewById(R.id.semesterplan_reregistration);

        semesterplanBezeichnung.setText(s.getBezeichnung());
        semesterplanLecturePeriod.setText(s.getLecturePeriod().toString());
        semesterplanFreieTage.setText("");
        for (FreeDay f : s.getFreeDays()) {
            semesterplanFreieTage.append(f.toString());
        }
        semesterplanPruefPeriod.setText(s.getExamsPeriod().toString());
        semesterplanRegistration.setText(s.getReregistration().toString());
    }

    private void writeToSharedPrefAndSendReq(Response.Listener<JSONArray> jsonArrayListener, Response.ErrorListener errorListener, String url, SharedPreferences sharedPref) {
        long currentTime = System.currentTimeMillis();
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(SHARED_PREFS_CATCH_DATE, currentTime);
        editor.commit();
        sendRequest(jsonArrayListener, errorListener, url);
    }

    private void sendRequest(Response.Listener<JSONArray> jsonArrayListener, Response.ErrorListener errorListener, String url) {
        JsonArrayRequest jsObjRequest = new JsonArrayRequest(url, jsonArrayListener, errorListener);
        VolleyDownloader.getInstance(getActivity()).addToRequestQueue(jsObjRequest);
    }

    private String getSemester() {
        int mounth = Calendar.getInstance().get(Calendar.MONTH);
        mounth = mounth + 1;
        if (mounth > 2 && mounth < 9) return "S";
        return "W";
    }
}
