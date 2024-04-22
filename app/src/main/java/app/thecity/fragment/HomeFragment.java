package app.thecity.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import app.thecity.AppConfig;
import app.thecity.R;
import app.thecity.activity.ActivityMain;
import app.thecity.activity.ActivityMaps;
import app.thecity.activity.ActivityPlaceDetail;
import app.thecity.activity.ActivitySearch;
import app.thecity.data.DatabaseHandler;
import app.thecity.model.Place;
import app.thecity.widget.GeneralInfoCard;

public class HomeFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ScrollView root = (ScrollView) inflater.inflate(R.layout.home_fragment, null);

        DatabaseHandler db = new DatabaseHandler(getActivity());

        LinearLayout suggestView = root.findViewById(R.id.suggest_scroll_view);
        LinearLayout exploreView = root.findViewById(R.id.explore_scroll_view);

        for(Place place : db.getPlacesByPage(-1, AppConfig.general.limit_loadmore, 0)) {
            GeneralInfoCard card = new GeneralInfoCard(getContext());
            card.setWidth(247);
            card.setHeight(300);
            card.setPlace(place);
            card.update();
            card.setOnClickListener((e) -> {
                ActivityPlaceDetail.navigate(ActivityMain.getInstance(), e, place);
            });
            suggestView.addView(card);
        }

        for(Place place : db.getPlacesByPage(-1, AppConfig.general.limit_loadmore, 0)) {
            GeneralInfoCard card = new GeneralInfoCard(getContext());
            card.setWidth(247);
            card.setHeight(300);
            card.setPlace(place);
            card.update();
            card.setOnClickListener((e) -> {
                ActivityPlaceDetail.navigate(ActivityMain.getInstance(), e, place);
            });
            exploreView.addView(card);
        }

        EditText searchBar = root.findViewById(R.id.search_bar);
        searchBar.setInputType(InputType.TYPE_NULL);
        searchBar.setOnFocusChangeListener((e, focus) -> {
            if(focus) {
                Intent i = new Intent(getActivity(), ActivitySearch.class);
                startActivity(i);
                e.clearFocus();
            }
        });

        return root;
    }
}
