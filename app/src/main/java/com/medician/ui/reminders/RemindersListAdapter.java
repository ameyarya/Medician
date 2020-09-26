package com.medician.ui.reminders;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.medician.R;

import java.util.List;

public class RemindersListAdapter extends ArrayAdapter {

    private final Activity context;
    private final List<String> nameArray;
    private final List<String> dateArray;

    public RemindersListAdapter(Activity context, List<String> nameArrayParam, List<String> dateArrayParam) {

        super(context, R.layout.reminderslistview_row, nameArrayParam);
        this.context = context;
        this.nameArray = nameArrayParam;
        this.dateArray = dateArrayParam;

    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.reminderslistview_row, null,true);

        TextView nameID = rowView.findViewById(R.id.MedName);
        TextView daysID = rowView.findViewById(R.id.MedDate);

        nameID.setText(nameArray.get(position));
        daysID.setText(dateArray.get(position));

        return rowView;

    }

}
