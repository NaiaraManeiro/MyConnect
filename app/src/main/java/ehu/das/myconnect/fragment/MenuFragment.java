package ehu.das.myconnect.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import ehu.das.myconnect.R;
import ehu.das.myconnect.list.ViewPagerAdapter;
import ehu.das.myconnect.list.ZoomOutPageTransformer;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MenuFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MenuFragment extends Fragment {

    private ViewPager mPager;
    private ViewPagerAdapter pagerAdapter;
    private int[] tabs = {R.id.filesFragment, R.id.terminalTab, R.id.terminalTab, R.id.terminalTab};
    private int[] tabsLayout = {R.id.filesFragment, R.id.scriptsTab, R.id.monitoringTab, R.id.terminalTab};
    public MenuFragment() {
        // Required empty public constructor
    }

    public static MenuFragment newInstance(String param1, String param2) {
        MenuFragment fragment = new MenuFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPager = getActivity().findViewById(R.id.viewPager);
        pagerAdapter = new ViewPagerAdapter(getActivity().getSupportFragmentManager());
        pagerAdapter.addFragment(new FilesFragment(),"rutinas");
        pagerAdapter.addFragment(new TerminalFragment(),"scripts");
        pagerAdapter.addFragment(new TerminalFragment(),"monitoreo");
        pagerAdapter.addFragment(new TerminalFragment(),"terminal");
        mPager.setAdapter(pagerAdapter);
        mPager.setCurrentItem(0);
        mPager.setPageTransformer(true, new ZoomOutPageTransformer());
        TabLayout tabslay = getActivity().findViewById(R.id.menuTabs);
        tabslay.getTabAt(0).setId(R.id.filesTab);
        tabslay.getTabAt(1).setId(R.id.scriptsTab);
        tabslay.getTabAt(2).setId(R.id.monitoringTab);
        tabslay.getTabAt(3).setId(R.id.terminalTab);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {
            }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
                TabLayout navegacion = getActivity().findViewById(R.id.menuTabs);
                // navegacion.setSelectedTabIndicator(tabsLayout[position]);
                TabLayout.Tab tabView = tabslay.getTabAt(position);
                tabView.select();
            }
        });

        /**View placeholder = getActivity().findViewById(R.id.menuFragmentPlaceholder);

        TabLayout tabs = getActivity().findViewById(R.id.menuTabs);
        tabs.getTabAt(0).setId(R.id.filesTab);
        tabs.getTabAt(3).setId(R.id.terminalTab);
        TabLayout.TabView filesTab = getActivity().findViewById(R.id.filesTab);
        TabLayout.TabView terminalTab = getActivity().findViewById(R.id.terminalTab);
        filesTab.setOnClickListener(v -> {
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.menuFragmentPlaceholder, new FilesFragment());
            ft.addToBackStack(null);
            ft.commit();
        });
        terminalTab.setOnClickListener(v -> {
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.menuFragmentPlaceholder, new TerminalFragment());
            ft.addToBackStack(null);
            ft.commit();
        });*/
    }
}