package org.insa.cipherdit.cipherstorage;

import org.insa.cipherdit.reddit.things.RedditPost;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;

/**
 * TODO par le groupe d'Aubry
 * Permet de gérer l'API Dropbox
 */

public class Storage {

    public class DownloadPost {
        String pseudo;
        String ABE_link;
        String AES_link;
    }

    // We read the only 3 lines of the file we have and we create a DownloadPost
    public DownloadPost read(String filepath) {
        DownloadPost new_post = new DownloadPost();
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            new_post.pseudo = br.readLine();
            new_post.ABE_link = br.readLine();
            new_post.AES_link = br.readLine();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return new_post;
    }

    // From a DownloadPost we only write 3 lines in the file with the filepath given
    public void writeTranslation(String filepath, String pseudo, String ABE_link, String AES_link) {

        FileWriter fw = null;
        try {
            fw = new FileWriter(filepath);
            fw.write(pseudo);
            fw.write(pseudo);
            fw.write(pseudo);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    // Here we are going to look for the threads
    public boolean ThreadFinder(DbxClientV2 Client, String thread_name){

        Boolean found_thread = false;
        // List the different folders
        try {
            ListFolderResult result = Client.files().listFolder("");
            while(!found_thread) {
                for (Metadata metadata : result.getEntries()){
                    if(metadata.getName() == thread_name) {
                        found_thread = true;
                    }
                }

                if(!result.getHasMore()){
                    break;
                }
                result = Client.files().listFolderContinue(result.getCursor());
            }
        } catch (DbxException e) {
            e.printStackTrace();
        }

        return found_thread;
    }


    public static String createFolder(DbxClientV2 Client, String FolderName){
        String URL = null;
        try {
            Client.files().createFolder("/"+FolderName);
            URL= Client.sharing().createSharedLinkWithSettings("/"+FolderName).getUrl();

        } catch (DbxException e) {
            e.printStackTrace();
        }
        System.out.println("URL du dossier crée : " +URL);
        return URL;

    }

    public static void downloadFile(DbxClientV2 Client, String FilePath, String SharedURL){
        OutputStream downloadFile = null;
        try {
            downloadFile = new FileOutputStream(FilePath);
            Client.sharing().getSharedLinkFile(SharedURL).download(downloadFile);
            downloadFile.close();
        } catch (FileNotFoundException | DbxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println("Fichier qui a été téléchargé !");
    }

    public static String uploadFile(DbxClientV2 Client, File localFile, String PathDB){
        String URL = null;
        try (InputStream in = new FileInputStream(localFile)){
            FileMetadata metadata = Client.files().uploadBuilder(PathDB)
                    .withMode(WriteMode.ADD)
                    .withClientModified(new Date(localFile.lastModified()))
                    .uploadAndFinish(in);

            URL= Client.sharing().createSharedLinkWithSettings(PathDB+localFile.getName()).getUrl();
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UploadErrorException e) {
            e.printStackTrace();
        } catch (DbxException e) {
            e.printStackTrace();
        }
        System.out.println("URL du fichier upload : " + URL);

        return URL;
    }


    

}
