package com.tulalens.survey.tulalenssurvey;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

/**
 * Created by vik on 2/16/15.
 */
public class SurveyAdapter extends ParseQueryAdapter implements AdapterView.OnItemClickListener {

    public SurveyAdapter(Context context) {

        super(context, new ParseQueryAdapter.QueryFactory<ParseObject>() {
            public ParseQuery<ParseObject> create() {
                // Here we can configure a ParseQuery to our heart's desire.
                ParseQuery query = new ParseQuery("Survey");
                query.whereEqualTo("use", true);
                query.orderByAscending("survey_number");
                query.fromLocalDatastore();
                return query;
            }
        });
    }


    public String get(int position){
        return this.getItem(position).getObjectId();
    }

    @Override
    public View getItemView(ParseObject object, View cellview, ViewGroup parent) {
        if (cellview == null) {

            cellview = View.inflate(getContext(), R.layout.adapter_item, null);

        }

        TextView name = (TextView) cellview.findViewById(R.id.name);
        TextView description = (TextView) cellview.findViewById(R.id.description);

        name.setText(object.getString("name"));
        description.setText(object.getString("description"));

        return cellview;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d("Survey", "Survey item clicked");
    }
}