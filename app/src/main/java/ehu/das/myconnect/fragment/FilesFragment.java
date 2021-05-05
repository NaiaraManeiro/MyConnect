package ehu.das.myconnect.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import ehu.das.myconnect.R;
import ehu.das.myconnect.list.FilesListAdapter;


public class FilesFragment extends Fragment {

    public FilesFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_files, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        RecyclerView rv = getActivity().findViewById(R.id.fileListRV);
        List<String> fileTypes = new ArrayList<>();
        fileTypes.add("folder");
        fileTypes.add("folder");
        fileTypes.add("folder");
        fileTypes.add("file");
        List<String> fileNames = new ArrayList<>();
        fileNames.add("Descargas");
        fileNames.add("Imágenes");
        fileNames.add("Escritorio");
        fileNames.add("err.log");
        FilesListAdapter fla = new FilesListAdapter(fileNames, fileTypes);
        rv.setAdapter(fla);
        GridLayoutManager layout = new GridLayoutManager(getActivity(), 4, GridLayoutManager.VERTICAL, false);
        rv.setLayoutManager(layout);
    }
}