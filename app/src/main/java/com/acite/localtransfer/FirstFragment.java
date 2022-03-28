package com.acite.localtransfer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.acite.localtransfer.databinding.FragmentFirstBinding;

import java.io.IOException;
import java.time.Duration;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private ActivityResultLauncher<String> mPickFile = registerForActivityResult(
                        new ActivityResultContracts.GetContent(), result -> {
                if (result != null) {
                    String path = getFileRealNameFromUri(getContext(),result);

                    Toast.makeText(getContext(), "Begin Transfer" + path, Toast.LENGTH_SHORT).show();
                    Conversation Conv=new Conversation();
                    ProcShow SD = new ProcShow();
                    SD.show(getChildFragmentManager(), "NoticeDialogFragment");
                    try {
                        Conv.TransFile(result, path, getActivity(), new Runnable() {
                            @Override
                            public void run() {
                                SD.dismiss();
                            }
                        });
                    }catch
                    (IOException ex){
                        Toast.makeText(getContext(), "Error:" + ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(getContext(), "User Canceled the Transfer", Toast.LENGTH_SHORT).show();
                }
            });
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Conversation.TransMsg(binding.editTextTextMultiLine.getText().toString());
            }
        });

        binding.file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPickFile.launch("*/*");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public static String getFileRealNameFromUri(Context context, Uri fileUri) {
        if (context == null || fileUri == null) return null;
        DocumentFile documentFile = DocumentFile.fromSingleUri(context, fileUri);
        if (documentFile == null) return null;
        return documentFile.getName();
    }
}