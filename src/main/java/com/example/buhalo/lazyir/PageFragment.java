package com.example.buhalo.lazyir;

import android.app.Activity;
import android.content.ClipData;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.support.v7.appcompat.R.id.image;
import static com.example.buhalo.lazyir.R.string.configure;

/**
 * Created by buhalo on 28.01.17.
 */

public class PageFragment extends Fragment {

    public static final String ARG_PAGE = "ARG_PAGE";

    public static List<Button> buttons = new ArrayList<>();

    public static Map<Integer,Button> buttonMap = new HashMap<>();

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
        System.out.println("on create view");
        View viewTouch = null;
        rootView = inflater.inflate(R.layout.activity_main,container,false);
        activity = (Activity) rootView.getContext();
        tabLayout = (TabLayout) activity.findViewById(R.id.sliding_tabs);

        viewTouch = inflater.inflate(R.layout.content_main, container, false);
        view = inflater.inflate(R.layout.fragment_page, container, false);
        final RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.fr_pg);
        final  ViewGroup vv = (ViewGroup) viewTouch;
        layout.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                final int action = event.getAction();
                System.out.println("Dropidze1 " + action);
                switch(action) {
                    case DragEvent.ACTION_DRAG_LOCATION:
                        final int x = (int)event.getX();
                        final int y = (int)event.getY();
                        System.out.println(("prefs X=" + String.valueOf(x) + " / Y=" + String.valueOf(y)));
                        break;
                    case DragEvent.ACTION_DROP:
                        System.out.println("Dropidze");
                        System.out.println();
                        final Button rpl = new Button(((Button)vv.getChildAt(0)).getContext());
                        //  rpl.setLayoutParams(((Button)v).getLayoutParams());
                        Button receivedButton = (Button)event.getLocalState();
                        if(event.getClipData().getItemAt(0).getText().equals("4"))
                        {
                            rpl.setId(View.generateViewId());
                        }
                        else
                        {
                            rpl.setId(receivedButton.getId());
                        }
                        rpl.setText(receivedButton.getText());
                        rpl.setX(event.getX()-receivedButton.getWidth()/2);
                        rpl.setY(event.getY()-receivedButton.getHeight()/2);
                        rpl.setWidth(receivedButton.getWidth());
                        rpl.setHeight(receivedButton.getHeight());
                        System.out.println(("!!!!!!!!!!!!FIAL!!!!!!!!!!!prefs X=" + String.valueOf(event.getX()) + " / Y=" + String.valueOf(event.getY())));
                        rpl.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                if(event.getAction() == MotionEvent.ACTION_DOWN)
                                {

                                    tabLayout.getTabAt(2).select();
                                    ClipData data = ClipData.newPlainText("", "");
                                    View.DragShadowBuilder shadow = new View.DragShadowBuilder(rpl);
                                    v.startDrag(data, shadow, rpl, 0);
                                    layout.removeView(rpl);
                                    ImageButton imgButton = (ImageButton)layout.findViewById(R.id.imageButton4);
                                    imgButton.setVisibility(View.VISIBLE);
                                }


                                return false;
                            }
                        });
                        ImageButton imgButton = (ImageButton)layout.findViewById(R.id.imageButton4);
                        System.out.println(rpl.getY());
                        System.out.println(rpl.getHeight());
                        System.out.println("000000000000");
                        System.out.println(imgButton.getY());
                        if(rpl.getY()+rpl.getHeight() >= imgButton.getY() && imgButton.getY()>0)
                        {
                            if(buttonMap.containsKey(rpl.getId()))
                            {
                                buttonMap.remove(rpl.getId());
                            }
                            layout.removeView(rpl);
                        }
                        else
                        {
                            layout.addView(rpl);
                            if(!buttonMap.containsKey(rpl.getId()))
                            {
                                buttonMap.put(rpl.getId(),rpl);
                            }
                        }

                        imgButton.setVisibility(View.INVISIBLE);
                        break;

                }
                return true;
            }
        });


        if(mPage == 1)
        {
            view = inflater.inflate(R.layout.content_main, container, false);


        }
        else if(mPage == 4)
        {



            final View tab3view = inflater.inflate(R.layout.fragment_page, container, false);

            view = inflater.inflate(R.layout.tab_selector, container, false);

            ViewGroup vv4 = (ViewGroup) view;
            for(int i=0;i< vv4.getChildCount();i++)
            {
              final Button mv4 = (Button)vv4.getChildAt(i);
                mv4.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {

                        if(event.getAction() == MotionEvent.ACTION_DOWN)
                        {

                            tabLayout.getTabAt(2).select();
                            ClipData data = ClipData.newPlainText("tab", "4");
                            View.DragShadowBuilder shadow = new View.DragShadowBuilder(mv4);
                            v.startDrag(data, shadow, mv4, 0);
                        }


                        return false;
                    }
                });
            }

        }
        else if(mPage == 3)
        {


            for(int i = 0;i<vv.getChildCount();i++)
            {

                Button mv =(Button)vv.getChildAt(i);
                    final Button rpl = new Button(mv.getContext());
                    rpl.setText(mv.getText());
                    rpl.setLayoutParams(mv.getLayoutParams());
                    rpl.setId(mv.getId());
                    rpl.setY(mv.getY());
                    rpl.setX(mv.getX());
                    rpl.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if (event.getAction() == MotionEvent.ACTION_DOWN) {

                                tabLayout.getTabAt(2).select();
                                ClipData data = ClipData.newPlainText("", "");
                                View.DragShadowBuilder shadow = new View.DragShadowBuilder(rpl);
                                v.startDrag(data, shadow, rpl, 0);
                                layout.removeView(rpl);
                                ImageButton imgButton = (ImageButton) layout.findViewById(R.id.imageButton4);
                                imgButton.setVisibility(View.VISIBLE);
                            }


                            return false;
                        }
                    });
//                    buttonMap.put(rpl.getId(),rpl);

                layout.addView(rpl);
            }
            if(buttonMap != null && buttonMap.size()>0)
            {
                for(Button temp : buttonMap.values())
                {
                    System.out.println("Sozdaju button " + temp.getId());
                  final Button rpl = new Button(((Button)vv.getChildAt(0)).getContext());
                    rpl.setText(temp.getText());
                    rpl.setId(temp.getId());
                    rpl.setY(temp.getY());
                    rpl.setX(temp.getX());
                    rpl.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if(event.getAction() == MotionEvent.ACTION_DOWN)
                            {

                                tabLayout.getTabAt(2).select();
                                ClipData data = ClipData.newPlainText("", "");
                                View.DragShadowBuilder shadow = new View.DragShadowBuilder(rpl);
                                v.startDrag(data, shadow, rpl, 0);
                                layout.removeView(rpl);
                                ImageButton imgButton = (ImageButton)layout.findViewById(R.id.imageButton4);
                                imgButton.setVisibility(View.VISIBLE);
                            }


                            return false;
                        }
                    });
                    layout.addView(rpl);
                }

            }
        }
        else
        {
            view = inflater.inflate(R.layout.fragment_page2, container, false);
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
//       marginParams.setMargins(left, top, 0, 0);
//        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(marginParams);
//        btnTag.setLayoutParams(layoutParams);
//        RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.dada);
//        layout.addView(btnTag);
//        buttons.add(btnTag);



//mv4.setOnDragListener(new View.OnDragListener() {
//@Override
//public boolean onDrag(View v, DragEvent event) {
//final int action = event.getAction();
//        System.out.println(event.getX());
//        System.out.println(event.getY());
//        switch(action) {
//        case DragEvent.ACTION_DRAG_LOCATION:
//final int x = (int)event.getX();
//final int y = (int)event.getY();
//        System.out.println(("prefs X=" + String.valueOf(x) + " / Y=" + String.valueOf(y)));
//        break;
//        case DragEvent.ACTION_DRAG_ENDED:
//        System.out.println("ja");
//        Button nv = new Button(tab3view.getContext());
//        nv.setText(mv4.getText());
//        nv.setId(View.generateViewId());
//        RelativeLayout.LayoutParams lj = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
//        nv.setLayoutParams(lj);
//        int left = (int) event.getX() - (v.getWidth());
//        int top = (int) event.getY() - (v.getHeight());
//
////                                        nv.setX(event.getX());
////                                        nv.setY(event.getY());
//        System.out.println("-------------------------------");
//        System.out.println(event.getX());
//        System.out.println(event.getY());
//        System.out.println("-------------------------------");
//        nv.setLeft(left);
//        nv.setTop(top);
//        buttons.add(nv);
//        break;
//
//        }
//        return true;
//
//        }
//        });



//if(event.getAction() == MotionEvent.ACTION_MOVE)
//        {
//        //     return true;
////                            System.out.println("-------------------------------");
////                            int left = (int) event.getRawX() - (v.getWidth());
////                            int top = (int) event.getRawY() - (v.getHeight());
////                            System.out.println(left);
////                            System.out.println(top);
////                            System.out.println("-------------------------------");
//        }
//        if(event.getAction() == MotionEvent.ACTION_UP)
//        {
////                            System.out.println("ja tutTT");
////                            Button nv = new Button(tab3view.getContext());
////                            nv.setText(mv4.getText());
////                            nv.setId(View.generateViewId());
////                          //  RelativeLayout.LayoutParams lj = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
////                            nv.setLayoutParams(v.getLayoutParams());
////                            int left = (int) event.getRawX();
////                            int top = (int) event.getRawY() ;
////                            int bottom = (int) v.getBottom();
////                            int right = (int ) v.getRight();
//////                            nv.setLeft(left);
//////                            nv.setTop(top);
//////                            nv.setBottom(bottom);
//////                            nv.setRight(right);
////                            int[] location = new int[2];
////                            v.getLocationOnScreen(location);
////                        //    v.getl
////                            float screenX = event.getRawX();
////                            float screenY = event.getRawY();
////                            float viewX = screenX - location[0];
////                            float viewY = screenY - location[1];
////
//////                            float screenX = event.getX();
//////                            float screenY = event.getY();
//////                            float viewX = screenX - v.getLeft();
//////                            float viewY = screenY - v.getTop();
////                            nv.setX(viewX);
////                            nv.setY(viewY);
////                            buttons.add(nv);
//        }
//
//rpl.setOnLongClickListener(new View.OnLongClickListener() {
//@Override
//public boolean onLongClick(View v) {
//        ImageButton imgButton = new ImageButton(rpl.getContext());
//        imgButton.setImageResource(R.drawable.btn_close);
//        imgButton.setLayoutParams(new ViewGroup.LayoutParams(50,50));
//        imgButton.setX(rpl.getX()+rpl.getWidth()-30);
//        imgButton.setY(rpl.getY()-15);
//        imgButton.setBackgroundColor(Color.TRANSPARENT);
//        imgButton.setOnClickListener(new View.OnClickListener() {
//@Override
//public void onClick(View v) {
//        layout.removeView(v);
//        layout.removeView(rpl);
//        buttons.remove(v);
//        }
//        });
//        layout.addView(imgButton);
//        return false;
//        }
//        });
