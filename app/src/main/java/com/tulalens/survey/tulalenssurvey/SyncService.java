package com.tulalens.survey.tulalenssurvey;

import android.app.Activity;
import android.content.Context;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.tulalens.survey.tulalenssurvey.dummy.DummyContent;

import java.util.List;

/**
 * Created by vik on 2/16/15.
 */
public class SyncService {
    private Activity mActivity;
    private OnSyncInteractionListener mListener;
    private static String SURVEYS_LABEL = "surveys";
    private static String SCREENS_LABEL = "screens";

    public SyncService(Activity activity) {
        mActivity = activity;

        try {
            mListener = (OnSyncInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnSyncInteractionListener");
        }
    }

    public boolean doSync(){
        ParseQuery query = new ParseQuery("Survey");
        query.whereEqualTo("use", true);
        List<ParseObject> surveyList;
        try {
           surveyList = query.find();
        } catch(ParseException e) {
            return false;
        }
        // Release any objects previously pinned for this query.
        try {
            ParseObject.unpinAll(SCREENS_LABEL, surveyList);
        } catch(ParseException error){

        }

        // Add the latest results for this query to the cache.
        try {
            ParseObject.pinAll(SURVEYS_LABEL, surveyList);
        }  catch(ParseException e) {
            return false;
        }
        ParseQuery screenQuery;
        List<ParseObject> screenList;
        for (ParseObject survey : surveyList) {
            screenQuery = new ParseQuery("Screen");
            screenQuery.whereEqualTo("survey", survey);
            try {
                screenList = screenQuery.find();
                try {
                    ParseObject.pinAll(SCREENS_LABEL, screenList);
                } catch(ParseException error){

                }
            } catch (ParseException e){

            }

        }
        mListener.onSyncComplete(true);
        return true;
    }

    public interface OnSyncInteractionListener {
        // TODO: Update argument type and name
        public void onSyncComplete(Boolean status);
    }
}
