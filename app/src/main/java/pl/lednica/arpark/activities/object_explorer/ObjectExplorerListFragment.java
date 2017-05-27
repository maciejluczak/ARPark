package pl.lednica.arpark.activities.object_explorer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import pl.lednica.arpark.R;
import pl.lednica.arpark.helpers.ObjectModel;

public class ObjectExplorerListFragment extends Fragment {

    private static final String ARG_PARAM1 = "object_in_fragment";

    private ObjectModel objectModel;
    RecyclerView recyclerView;

    ObjectExplorerListFragmentAdapter adapter;
    public ObjectExplorerListFragment() {
        // Required empty public constructor
    }


    public static ObjectExplorerListFragment newInstance(ObjectModel objectModel) {
        ObjectExplorerListFragment fragment = new ObjectExplorerListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, objectModel);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            objectModel = (ObjectModel) getArguments().getSerializable(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_object_explorer_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new ObjectExplorerListFragmentAdapter(objectModel,getActivity(),getActivity().getApplicationContext());
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }


}
