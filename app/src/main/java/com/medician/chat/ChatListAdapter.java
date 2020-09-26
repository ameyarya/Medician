package com.medician.chat;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.medician.R;

import java.util.List;

public class ChatListAdapter extends ArrayAdapter {

    private final Activity context;
    private final List<String> nameArray;
    private final List<String> lastMsgArray;

    ChatListAdapter(Activity context, List<String> nameArrayParam, List<String> lastMsgArrayParam) {

        super(context, R.layout.chatuserlistview_row, nameArrayParam);
        this.context = context;
        this.nameArray = nameArrayParam;
        this.lastMsgArray = lastMsgArrayParam;

    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.chatuserlistview_row, null,true);

        TextView nameID = rowView.findViewById(R.id.chatUserName);
        TextView userID = rowView.findViewById(R.id.lastMsg);

        nameID.setText(nameArray.get(position));
        userID.setText(lastMsgArray.get(position));

        return rowView;

    }

}
