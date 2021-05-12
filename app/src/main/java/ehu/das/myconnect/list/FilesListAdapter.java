package ehu.das.myconnect.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ehu.das.myconnect.R;
import ehu.das.myconnect.dialog.OnClickRecycleView;

public class FilesListAdapter extends RecyclerView.Adapter<FileListElementViewHolder> {

    private final List<String> fileNames;
    private final List<String> fileTypes;
    private OnClickRecycleView onClickRecycleView;

    public FilesListAdapter(List<String> fileNames, List<String> fileTypes, OnClickRecycleView onClickRecycleView) {
        this.fileTypes = fileTypes;
        this.fileNames = fileNames;
        this.onClickRecycleView = onClickRecycleView;
    }

    @NonNull
    @Override
    public FileListElementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View filaLayout = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_element_layout, null);
        return new FileListElementViewHolder(filaLayout, this.onClickRecycleView);
    }

    @Override
    public void onBindViewHolder(@NonNull FileListElementViewHolder holder, int position) {
        if (fileTypes.get(position).equals("folder")){
            holder.fileType.setImageResource(R.drawable.folder);
        } else {
            holder.fileType.setImageResource(R.drawable.file);
        }
        holder.name.setText(fileNames.get(position));
    }


    @Override
    public int getItemCount() {
        return fileNames.size();
    }
}
