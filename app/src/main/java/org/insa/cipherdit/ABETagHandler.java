package org.insa.cipherdit;

import android.content.Context;

import androidx.preference.CheckBoxPreference;
import androidx.preference.PreferenceCategory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ABETagHandler {
    static ArrayList<CheckBoxPreference> tags = new ArrayList<CheckBoxPreference>();
    static  String fileName;
    static File fileDir;
    PreferenceCategory category;
    Context context;

    public ABETagHandler(PreferenceCategory categoryPar, Context context) {
        category = categoryPar;
        fileName = context.getString(R.string.abetags_file);
        fileDir = context.getFilesDir();

        importTagsFromFile();

    }

    public void importTagsFromFile(){
        String text = "";
            StringBuilder textbuild = new StringBuilder();

            try {
                BufferedReader br = new BufferedReader(new FileReader(new File(fileDir,fileName)));
                String line;

                while ((line = br.readLine()) != null) {
                    textbuild.append(line);
                    textbuild.append('\n');
                }
                br.close();
                text = textbuild.toString();

            /*
            }catch (IOException e) {
                //You'll need to add proper error handling here
            }



            InputStream fileInput = new FileInputStream(new File(fileDir,fileName));
            Scanner scanner = new Scanner(fileInput);
            text = scanner.useDelimiter("\\A").next();
            fileInput.close();*/
            System.out.println("Fichier tag importé");
        } catch (IOException e) {
            System.out.println("Pas de fichiers de stockage des tags : création");
            // Si le fichier existe pas on le crée
            File newTagFile = new File(fileDir, fileName);
            try {
                newTagFile.createNewFile();
                System.out.println("Fichier créé");
                return;
            }catch(IOException e1){
                //TODO handle error
            }
        }

        JSONObject json = null;
        JSONArray jsonArray = null;
        try {
            System.out.println("Contenu du JSON : "+text);
            System.out.println("Ouverture du fichier JSON");

            json = new JSONObject(text);
            jsonArray = json.getJSONArray("tags");
            System.out.println("Array des tags récupérée");
            JSONObject jsonArrayEntry = null;
            for(int i=0; i<jsonArray.length();i++){
                jsonArrayEntry = jsonArray.getJSONObject(i);
                System.out.println("Lecture d'un tag :");
                addNewTag(jsonArrayEntry.getString("tag-name"),category,jsonArrayEntry.getBoolean("checked"));
                System.out.println("1 tag importé");
            }
            System.out.println("Tags importés");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    static void updateTags(){
        try {

            JSONArray array = new JSONArray();
            for(CheckBoxPreference tag : tags){
                JSONObject entry = new JSONObject();
                entry.put("tag-name",tag.getTitle());
                entry.put("checked",tag.isChecked());
                array.put(entry);
            }
            JSONObject tags = new JSONObject();
            tags.put("tags", array);

            FileWriter fileWriter = new FileWriter(new File(fileDir,fileName));
            fileWriter.write(tags.toString());
            fileWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //TODO problème avec la catégorie qui est nulle : à régler
    public static void  addNewTag(String tagName, PreferenceCategory category, boolean status) {
        CheckBoxPreference checkbox = new CheckBoxPreference(category.getContext());
        checkbox.setKey("pref_chk_"+tagName);
        checkbox.setTitle(tagName);
        checkbox.setChecked(status);
        checkbox.setOnPreferenceChangeListener((pref, value) -> {
            System.out.println("tag "+pref.getTitle()+" : "+value);
            updateTags();
            return true;
        });
        tags.add(checkbox);
        category.addPreference(checkbox);
        updateTags();
    }

    public static ArrayList<String> getTags(boolean selected){
        ArrayList<String> taglist = new ArrayList<String>();
        for(CheckBoxPreference checkbox : tags){
            if(!selected || checkbox.isChecked()){
                taglist.add(checkbox.getTitle().toString());
            }
        }
        return taglist;
    }

}
