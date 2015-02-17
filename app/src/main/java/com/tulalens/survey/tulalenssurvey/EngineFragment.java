package com.tulalens.survey.tulalenssurvey;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EngineFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EngineFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EngineFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private String mObjectID;
    private OnFragmentInteractionListener mListener;
    private ParseObject mSurvey;
    private ParseObject mCurrentScreen;
    private ParseQuery mScreenQuery;
    private View mCurrentView;
    private int screenCount;
    private ViewGroup mContainer;
    private LayoutInflater inflater;
    private String sessionID;

    // TODO: Rename and change types and number of parameters
    public static EngineFragment newInstance(String objectID) {
        EngineFragment fragment = new EngineFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, objectID);
        fragment.setArguments(args);
        return fragment;
    }

    public EngineFragment() {
        // Required empty public constructor
    }

    public LinearLayout getLayout(ParseObject screen){

        String type = screen.getString("type");
        LinearLayout layout;

        int id;
        switch(type){
            case "text":
                id = R.layout.text_screen;
                break;
            case "choice_single":
                id = R.layout.single_choice_screen;
                break;
            case "choice_multiple":
                id = R.layout.multiple_choice_screen;
                break;
            case "integer":
                id = R.layout.integer_screen;
                break;
            default:
                id = R.layout.text_screen;
                break;
        }

        layout = (LinearLayout) getActivity().getLayoutInflater().inflate(id, null, false);

        TextView question = (TextView) layout.findViewById(R.id.question);
        question.setId(1);
        question.setText(screen.getString("question"));

        Button nextButton = (Button) layout.findViewById(R.id.next_button);
        if(screen.getInt("screen_number") < screenCount) {
            nextButton.setText(getString(R.string.button_next));
            nextButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    nextScreen();
                }
            });
        } else {
            nextButton.setText(getString(R.string.button_end));
            nextButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    endSurvey();
                }
            });
        }
        nextButton.setId(2);

        if(type.equals("choice_single") || type.equals("choice_multiple")){
            List<String> choices = screen.getList("options");
            LinearLayout ll = new LinearLayout(getActivity());
            ll.setOrientation(LinearLayout.VERTICAL);
            if(type.equals("choice_single")) {
                RadioGroup radioGroup = (RadioGroup) layout.findViewById(R.id.radio_group);
                for (int i = 0; i < choices.size(); i++) {
                    RadioButton rdbtn = new RadioButton(getActivity());
                    rdbtn.setId(i + 15);
                    rdbtn.setText(choices.get(i));
                    radioGroup.addView(rdbtn);
                }
            } else {
                for (int i = 0; i < choices.size(); i++) {
                    CheckBox check = new CheckBox(getActivity());
                    check.setId(i + 15);
                    check.setText(choices.get(i));
                    ll.addView(check);
                }
                ((ViewGroup) layout.findViewById(R.id.multiple_buttons)).addView(ll);
            }
        } else if (type.equals("text")){
            EditText input = (EditText) layout.findViewById(R.id.text_input);
            input.setId(3);
        } else if (type.equals("integer")){

            EditText input = (EditText) layout.findViewById(R.id.integer);
            input.setId(3);
        }

        return layout;
    }

    public int saveSurvey(){
        String type = mCurrentScreen.getString("type");
        ParseObject object = new ParseObject("ScreenAnswer");
        object.put("screen", mCurrentScreen);
        int nextScreen = mCurrentScreen.getInt("screen_number") + 1;
        object.put("collector", ParseUser.getCurrentUser());
        object.put("session", sessionID);

        if (type.equals("text") || type.equals("integer")) {
            EditText text = (EditText) mCurrentView.findViewById(3);
            String value = text.getText().toString();
            object.put("response", value);
        } else if (type.equals("choice_single") || type.equals("choice_multiple")){
            List<String> options = mCurrentScreen.getList("options");
            if(type.equals("choice_single")){
                int idx = -1;
                for(int i=0;i < options.size(); i++){
                    RadioButton check = (RadioButton) mCurrentView.findViewById(i + 15);
                    if(check.isChecked()){
                        idx = i;
                    }
                }
                if(idx == -1){
                    return -1;
                }
                object.put("choice", options.get(idx));
                List<Integer> skips = mCurrentScreen.getList("next_screen");
                if(skips != null){
                    nextScreen = skips.get(idx);
                }
            } else if (type.equals("choice_multiple")){
                ArrayList<String> selectedChoices = new ArrayList<String>();
                for(int i=0;i < options.size(); i++){
                    CheckBox check = (CheckBox) mCurrentView.findViewById(i + 15);
                    if(check.isChecked()){
                        selectedChoices.add(options.get(i));
                    }
                }
                object.put("choices", selectedChoices);
            }
        }
        object.saveEventually();
        return nextScreen;
    }

    public void endSurvey(){
        saveSurvey();
        MainPage page = (MainPage) getActivity();
        page.onNavigationDrawerItemSelected(0);
    }

    public void setupScreen(){
        LinearLayout layout = getLayout(mCurrentScreen);
        replaceView(mCurrentView, layout);
        mCurrentView = layout;
    }

    public void nextScreen(){
        int screenNumber = saveSurvey();
        if(screenNumber == -1){
            Toast.makeText(getActivity(), "Please fill in a value.", Toast.LENGTH_LONG).show();
        } else {
            mCurrentScreen = getScreen(screenNumber);
            setupScreen();
        }
    }

    public static ViewGroup getParent(View view) {
        return (ViewGroup)view.getParent();
    }

    public static void removeView(View view) {
        ViewGroup parent = getParent(view);
        if(parent != null) {
            parent.removeView(view);
        }
    }

    public static void replaceView(View currentView, View newView) {
        ViewGroup parent = getParent(currentView);
        if(parent == null) {
            return;
        }
        final int index = parent.indexOfChild(currentView);
        removeView(currentView);
        removeView(newView);
        parent.addView(newView, index);
    }

    public ParseQuery getQuery(){
        ParseQuery screenQuery = new ParseQuery("Screen");
        screenQuery.fromLocalDatastore();
        screenQuery.whereEqualTo("survey", mSurvey);
        screenQuery.orderByAscending("screen_number");
        return screenQuery;
    }

    public ParseObject getScreen(int screenNumber){
        ParseQuery screenQuery = getQuery();
        screenQuery.whereEqualTo("screen_number", screenNumber);
        ParseObject screen;
        try{
            screen = screenQuery.getFirst();
        } catch (ParseException e){
            return null;
        }
        return screen;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mObjectID = getArguments().getString(ARG_PARAM1);
        }

        ParseQuery query = new ParseQuery("Survey");
        query.fromLocalDatastore();
        try {
            mSurvey = query.get(mObjectID);
        } catch (ParseException e){
            return;
        }

        ParseQuery screenQuery = getQuery();
        try {
            mCurrentScreen = screenQuery.getFirst();
        } catch (ParseException e){
            return;
        }
        try {
            screenCount = screenQuery.count();
        } catch (ParseException e){
            return;
        }

        sessionID = UUID.randomUUID().toString();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_engine, container, false);
        mContainer = container;
        ViewTreeObserver observer = v.getViewTreeObserver();

        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                v.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                mCurrentView = v;
                setupScreen();
            }
        });
        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onEngineFragmentInteraction("Click");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onEngineFragmentInteraction(String data);
    }

}
