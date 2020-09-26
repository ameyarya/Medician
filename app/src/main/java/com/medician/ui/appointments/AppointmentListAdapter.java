package com.medician.ui.appointments;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.medician.R;

import java.util.List;

public class AppointmentListAdapter extends ArrayAdapter {

    private final Activity context;
    private final List<String> nameArray;
    private final List<String> addressArray;

    public AppointmentListAdapter(Activity context, List<String> nameArrayParam, List<String> addressArrayParam) {

        super(context, R.layout.appointmentlistview_rowuser, nameArrayParam);
        this.context = context;
        this.nameArray = nameArrayParam;
        this.addressArray = addressArrayParam;

    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.appointmentlistview_rowuser, null,true);

        TextView appNameID = rowView.findViewById(R.id.nameAppRow);
        TextView appAddID = rowView.findViewById(R.id.addressAppRow);

        appNameID.setText(nameArray.get(position));
        appAddID.setText(addressArray.get(position));

        return rowView;

    }

}
