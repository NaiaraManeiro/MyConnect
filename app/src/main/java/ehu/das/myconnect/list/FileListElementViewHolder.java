package ehu.das.myconnect.list;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ehu.das.myconnect.R;

public class FileListElementViewHolder extends RecyclerView.ViewHolder {

    public TextView name;
    public ImageView fileType;


    public FileListElementViewHolder(@NonNull View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.fileName);
        fileType = itemView.findViewById(R.id.fileTypeIV);
    }
}
