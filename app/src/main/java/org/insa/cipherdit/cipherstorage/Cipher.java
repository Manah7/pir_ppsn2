package org.insa.cipherdit.cipherstorage;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.common.primitives.Bytes;

import org.insa.cipherdit.reddit.things.RedditPost;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.List;
import co.junwei.cpabe.Cpabe;


/**
 * TODO par le groupe qui fait ABE+AES
 * Permet de gérer l'encryption
 */

public class Cipher {

    //PUBLIC INTERFACE FOR CipherStorage
    public void setupCipher() throws Exception{
        cpabe = new Cpabe() ;
        // Setting up public parameter and master key
        cpabe.setup(pubfile,mskfile) ;
    }


    public Object Cipher(RedditPost post, List<String> attributes){
        return null ;
    }

    //
    public boolean CheckAccess(Object ciphered){
        return false ;
    }

    public RedditPost Decipher(Object ciphered){
        return null ;
    }

    public class CipherCouple {
        /*
        Une cle K generee a partir d'un hash du fichier de départ et encryptee avec CPABE
        & le fichier encrypte avec AES
         */
        private String ABE_EncKey ;
        private String AES_EncFile ;
        @RequiresApi(api = Build.VERSION_CODES.O)
        public void CipherCouple(String file_path, String policy) throws Exception {
            /*
            Generate Key from File hash
            Encrypt Key with CPABE using attributes
            Encrypt File with AES
             */
            // Generate Key from File hash + Encrypt Key with CPABE
            byte[] file_bytes = GetBytesFromPath(file_path);
            //Creating the MessageDisgest object
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            //Passing data
            md.update(file_bytes);
            Path encpath = Files.createFile(Paths.get(dir+"enckey") ) ;
            BufferedWriter writer = new BufferedWriter(new FileWriter(encpath.toFile())) ;
            writer.write(file_bytes.toString());
            //Generating private key
            cpabe.keygen(pubfile,prvfile,mskfile,ATTRIBUTES);
            cpabe.enc(pubfile,policy,encpath.toString(),ABE_EncKey);

            //AES

        }

        //Pour le groupe d'Aubry
        public String getCipheredKey() {
            return ABE_EncKey;
        }
        public String getCipheredFile() {
            return AES_EncFile;
        }
    }

    /*---------------------------------------------------------------------*/
    //PRIVATE

    //CPABE related
    static private Cpabe cpabe ;
    static private String dir = "app/src/main/java/org/insa/cipherdit/cipherstorage" ;
    static private String pubfile = dir + "/pub_key" ;
    static private String mskfile = dir + "/master_key" ;
    static private String prvfile = dir + "/prv_key" ;

    private String ATTRIBUTES = "" ;

    private String ListToAttributeString(List<String> str_list){
        String R = "" ;
        for (String at: str_list) {
            R += " "+at ;
        }
        return R ;
    }

    private byte[] GetBytesFromPath(String path) throws IOException {
        File file = new File(path) ;
        byte[] bytes = new byte[(int) file.length()] ;
        FileInputStream fis = null ;
        try {
            fis = new FileInputStream(file);
            //read file into bytes[]
            fis.read(bytes);
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
        return bytes ;
    }

}
