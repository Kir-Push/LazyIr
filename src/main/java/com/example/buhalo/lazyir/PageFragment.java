package com.example.buhalo.lazyir;

import android.app.Activity;
import android.content.ClipData;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.support.v7.appcompat.R.id.image;
import static com.example.buhalo.lazyir.R.string.configure;

/**
 * Created by buhalo on 28.01.17.
 */

public class PageFragment extends Fragment {

    public static final String ARG_PAGE = "ARG_PAGE";

    public static List<Button> buttons = new ArrayList<>();

    private int mPage;

    private  TabLayout tabLayout;

    private Activity activity;

    private View rootView;

    public static PageFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        PageFragment fragment = new PageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = null;
        View viewTouch = null;
        if(mPage == 1)
        {
            view = inflater.inflate(R.layout.content_main, container, false);

        }
        else if(mPage == 4)
        {

            rootView = inflater.inflate(R.layout.activity_main,container,false);
            activity = (Activity) rootView.getContext();
            tabLayout = (TabLayout) activity.findViewById(R.id.sliding_tabs);


            view = inflater.inflate(R.layout.tab_selector, container, false);
            ViewGroup vv4 = (ViewGroup) view;
            for(int i=0;i< vv4.getChildCount();i++)
            {
              final Button mv4 = (Button)vv4.getChildAt(i);
                mv4.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {

                        ClipData data = ClipData.newPlainText("", "");
                        View.DragShadowBuilder shadow = new View.DragShadowBuilder(mv4);
                        v.startDrag(data, shadow, null, 0);

                        tabLayout.getTabAt(2).select();

                        if(event.getAction() == MotionEvent.ACTION_MOVE)
                        {
                        return true;
                        }

                        if(event.getAction() == MotionEvent.ACTION_UP)
                        {

                        }
                        return false;
                    }
                });
            }

        }
        else if(mPage == 3)
        {
            viewTouch = inflater.inflate(R.layout.content_main, container, false);
            view = inflater.inflate(R.layout.fragment_page, container, false);
            RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.fr_pg);
            ViewGroup vv = (ViewGroup) viewTouch;
            for(int i = 0;i<vv.getChildCount();i++)
            {
                Button mv =(Button)vv.getChildAt(i);
                Button rpl = new Button(mv.getContext());
                rpl.setText(mv.getText());
                rpl.setLayoutParams(mv.getLayoutParams());
                rpl.setId(mv.getId());
                rpl.setY(mv.getY());
                rpl.setX(mv.getX());
                layout.addView(rpl);
            }
            if(buttons != null && buttons.size()<0)
            {
                System.out.println("tut jest");
                layout.addView(buttons.get(0));
            }
        }
        else
        {
            view = inflater.inflate(R.layout.fragment_page2, container, false);
           // TextView textView = (TextView) view;
           // textView.setText(R.string.configure);
        }

        return view;
    }


}


//                        ClipData data = ClipData.newPlainText("", "");
//                        View.DragShadowBuilder shadow = new View.DragShadowBuilder(tv);
//                        v.startDrag(data, shadow, null, 0);
//                        return true;

//        Button btnTag = new Button(view.getContext());
//        btnTag.setText("Button! " + 25);
//        btnTag.setId(1 + 1 + (2 * 4));
//        int left = (int) me.getRawX() - (v.getWidth());
//        int top = (int) me.getRawY() - (v.getHeight());
//        ViewGroup.MarginLayoutParams marginParams = new ViewGroup.MarginLayoutParams(tv.getLayoutParams());
//        marginParams.setMargins(left, top, 0, 0);
//        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(marginParams);
//        btnTag.setLayoutParams(layoutParams);
//        RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.dada);
//        layout.addView(btnTag);
//        buttons.add(btnTag);
