package org.insa.cipherdit.cipherstorage;

import org.insa.cipherdit.reddit.things.RedditPost;
import java.util.List;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * TODO par PPSN 1
 * Permet de gérer ABE et le stockage
 */

public class CipherStorage {
    //PUBLIC INTERFACE FOR PPSN2

    private Cipher cipher;

    public void init() {
        cipher = new Cipher();
        setupCipher();
    }

    // TODO Fonction appelée lors d'un envoi de posts
    // Note : la casse des mots "id" viens de stegano et "ID" est respectée
    public void postSend (RedditPost post, List<String> attributes, String id_steg) {
        byte[] postinbyte = RedditPostToByteArray(post);

        //Encrypt
        boolean success = cipher.Encrypt(postinbyte, attributes);

        if (!success){
            //throw erreur ?
        }

    }

    // ID is reference to stored Object
    public RedditPost postGet(String id_steg, String cpabe_private_key) {
        
        //Recup the ABEKEy file cpabe_key

        //byte[] postinbyte = c.Decrypt(cpabe_key_private_key, cpabe_key);
        //RedditPost post =  ByteArrayToRedditPost(postinbyte)
        return post;
    }

    /*---------------------------------------------------------------------*/
    //PRIVATE


    //This function convert a RedditPost into a byte array
    private byte[] RedditPostToByteArray(RedditPost post) {
        byte[] postinbyte= null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;

        try {
            oos = new ObjectOutputStream(bos);
            oos.writeObject(post);
            oos.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        postinbyte = bos.toByteArray();
        try {
            bos.close();
        } catch (IOException e) {
            System.out.println("Error when closing the output stream.");
        }

        return postinbyte;
    }

    //This function convert a byte array into a RedditPost
    private RedditPost ByteArrayToRedditPost(byte[] postinbyte) {
        RedditPost post= null;
        ByteArrayInputStream bis = new ByteArrayInputStream(postinbyte);
        ObjectInputStream ois = null;

        try {
            ois = new ObjectInputStream(bis);
            post = (RedditPost) ois.readObject();
            ois.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return post;
    }
}
