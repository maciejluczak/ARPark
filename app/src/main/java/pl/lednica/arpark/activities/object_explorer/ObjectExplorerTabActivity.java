package pl.lednica.arpark.activities.object_explorer;

import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import pl.lednica.arpark.R;
import pl.lednica.arpark.activities.MainActivity;
import pl.lednica.arpark.helpers.ObjectJsonUtils;
import pl.lednica.arpark.helpers.ObjectModel;
import pl.lednica.arpark.opengl_based_3d_engine.LightColorRenderer;
import pl.lednica.arpark.opengl_based_3d_engine.LightTextureRenderer;
import pl.lednica.arpark.opengl_based_3d_engine.ObjectExplorerView;

/**
 * Created by Maciej on 2017-03-24.
 * Aktywność prezętująca listę obiektów i informacje historyczne o nich.
 * Po urucomienie modułu 3D sprawdza czy jest odpowiednia wersja OpenGLES,
 * tworzy widok GLSurfaceView i przypisuje
 * do niego właściwą klasę odpowiadającą za renderowanie obiektów 3D
 */

public class ObjectExplorerTabActivity extends AppCompatActivity {

    /**
     * Pola przechowujące informacje potrzebne do załadowania obiektów z JSON
     * i przeglądanie informacji
     */
    RecyclerView recyclerView;
    TabLayout tabs;
    ViewPager pages;
    ObjectExplorerTabPagerAdapter pagerAdapter;
    ArrayList<ObjectModel> objects;
    ObjectJsonUtils jsonUtils;
    public Integer tabID=0;
    private final static String INTENT_OBJECT_EXTRA = "object_3d_extra";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        jsonUtils  = new ObjectJsonUtils(this.getApplicationContext());
        objects = jsonUtils.getObjectsList();
        setContentView(R.layout.activity_object_explorer_tab);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        tabs = (TabLayout) findViewById(R.id.tab_layout);
        pages = (ViewPager) findViewById(R.id.view_pager);
        createPagerFragments();
        pages.setAdapter(pagerAdapter);
        tabs.setupWithViewPager(pages);

        //Przycisk uruchamiający moduł 3d
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tabID = pages.getCurrentItem();
                Log.e(INTENT_OBJECT_EXTRA, tabID.toString());

                    Class destinationClass = ObjectExplorer3DActivity.class;
                    Intent intentToStart3DActivity = new Intent(getApplicationContext(), destinationClass);
                    intentToStart3DActivity.putExtra(INTENT_OBJECT_EXTRA, objects.get(pages.getCurrentItem()));
                    startActivity(intentToStart3DActivity);

            }
        });

    }

    private void createPagerFragments() {

        pagerAdapter = new ObjectExplorerTabPagerAdapter(getSupportFragmentManager());
        for(int i=0; i< objects.size();i++){
            ObjectExplorerListFragment fragment = ObjectExplorerListFragment.newInstance(objects.get(i));
            pagerAdapter.addTab( fragment, objects.get(i).getName());
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
        finish();
    }

}
