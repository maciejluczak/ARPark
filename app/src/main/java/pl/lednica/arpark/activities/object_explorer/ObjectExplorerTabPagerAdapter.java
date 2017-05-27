package pl.lednica.arpark.activities.object_explorer;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 24.03.2017.
 */

public class ObjectExplorerTabPagerAdapter extends FragmentPagerAdapter {

    private List<String> tabsTitlesList =new ArrayList<>();
    private List<Fragment> tabsList = new ArrayList<>();

    public ObjectExplorerTabPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addTab(Fragment tabFrgment, String tabTitle){
        tabsList.add(tabFrgment);
        tabsTitlesList.add(tabTitle);
    }

    @Override
    public Fragment getItem(int position) {
        return tabsList.get(position);
    }

    @Override
    public int getCount() {
        return tabsList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabsTitlesList.get(position);
    }
}
