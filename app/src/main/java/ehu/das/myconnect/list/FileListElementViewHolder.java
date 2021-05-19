package ehu.das.myconnect.list;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ehu.das.myconnect.R;
import ehu.das.myconnect.dialog.OnClickRecycleView;

public class FileListElementViewHolder extends RecyclerView.ViewHolder {

    public TextView name;
    public ImageView fileType;


    public FileListElementViewHolder(@NonNull View itemView, OnClickRecycleView onClickRecycleView) {
        super(itemView);
        name = itemView.findViewById(R.id.fileName);
        fileType = itemView.findViewById(R.id.fileTypeIV);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickRecycleView.onItemClick(getAdapterPosition());
            }
        });

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onClickRecycleView.onItemLongClick(getAdapterPosition());
                return false;
            }
        });
    }
}
