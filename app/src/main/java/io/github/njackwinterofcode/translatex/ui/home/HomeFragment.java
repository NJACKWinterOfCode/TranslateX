package io.github.njackwinterofcode.translatex.ui.home;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import io.github.njackwinterofcode.translatex.R;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private Button translate;
    private Spinner spinner2;
    private ImageView mice;
    private List<String> languagesName;
    private TextInputLayout textToTranslate;
    TextInputEditText rst ;
    private static final int SPEECH_RECOGNITION_CODE = 100;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        translate = root.findViewById(R.id.translateButton);
        rst = root.findViewById(R.id.rsult);
        mice=root.findViewById(R.id.mice);
        textToTranslate = root.findViewById(R.id.texttotranslate);
        translate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                translateX(textToTranslate.getEditText().getText().toString());
            }
        });

        // spinner object
        Spinner spinner1 = (Spinner) root.findViewById(R.id.spinner1);
        spinner2 = (Spinner) root.findViewById(R.id.spinner2);

        Set<Integer> languages = FirebaseTranslateLanguage.getAllLanguages();
        languagesName = new ArrayList<>();
        for(int x: languages)
            languagesName.add(FirebaseTranslateLanguage.languageCodeForLanguage(x));


        //Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(getContext (),
                R.array.input_language, android.R.layout.simple_spinner_item);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, languagesName);

        //Specify the layout to use when the list of choices appears
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                speak();
            }
        });
        //Apply the adapter to the spinner
        spinner1.setAdapter(adapter1);
        spinner2.setAdapter(dataAdapter);
        return root;
    }

    private void speak() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Hi Speak Something");

        try{
            startActivityForResult(intent,SPEECH_RECOGNITION_CODE);

        }catch(Exception e){

            Toast.makeText(getContext(),""+e.getMessage(),Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode){
            case SPEECH_RECOGNITION_CODE:{
                if(resultCode == RESULT_OK && null!=data){

                    //getting text array from voice intent

                    ArrayList<String> list = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    rst.setText(list.get(0));
                }
                break;
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    public void translateX(final String str) {
        FirebaseTranslatorOptions options =
                new FirebaseTranslatorOptions.Builder()
                        .setSourceLanguage(FirebaseTranslateLanguage.EN)
                        .setTargetLanguage(FirebaseTranslateLanguage.languageForLanguageCode(spinner2.getSelectedItem().toString()))
                        .build();
        final FirebaseTranslator englishGermanTranslator =
                FirebaseNaturalLanguage.getInstance().getTranslator(options);

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build();
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Downloading langauge Model for translation");
        progressDialog.setMessage("Please wait while the download is in progress");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        englishGermanTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void v) {
                                // Model downloaded successfully. Okay to start translating.
                                // (Set a flag, unhide the translation UI, etc.)
                                progressDialog.dismiss();
                                englishGermanTranslator.translate(str)
                                        .addOnSuccessListener(new OnSuccessListener<String>() {
                                            @Override
                                            public void onSuccess(String s) {
                                                Toast.makeText(getActivity(), s, Toast.LENGTH_LONG).show();
                                            }
                                        });
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Model couldn’t be downloaded or other internal error.
                                // ...
                                progressDialog.hide();
                                Toast.makeText(getActivity(), "Unable to translate", Toast.LENGTH_SHORT).show();
                            }
                        });

    }
}