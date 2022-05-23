package com.example.self.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.self.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import model.Journal;

public class JournalsAdapter extends RecyclerView.Adapter<JournalsAdapter.ViewHolder> {
    private Context context;
    private List<Journal> journalList;

    public JournalsAdapter(Context context, List<Journal> journalList) {
        this.context = context;
        this.journalList = journalList;
    }

    @NonNull
    @Override
    public JournalsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.journal_row, parent, false);

        return new ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull JournalsAdapter.ViewHolder holder, int position) {
        Journal journal = journalList.get(position);

        holder.title.setText(journal.getTitle());
        holder.thoughts.setText(journal.getThought());
        Picasso.get().load(journal.getImageUrl())
                .fit()
                .into(holder.image);
        holder.dateAdded.setText((String) DateUtils.getRelativeTimeSpanString(
                journal.getTimeAdded().getSeconds()*1000));
    }

    @Override
    public int getItemCount() {
        return journalList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title, thoughts, dateAdded;
        public ImageView image;
        String  userId, userEmail;
        public ViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);
            context = ctx;
            title = itemView.findViewById(R.id.journal_title_list);
            thoughts = itemView.findViewById(R.id.journal_thought_list);
            dateAdded = itemView.findViewById(R.id.journal_timestamp_list);
            image = itemView.findViewById(R.id.journal_image_list);
        }
    }
}
