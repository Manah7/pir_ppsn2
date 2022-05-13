package org.insa.cipherdit.cipherstorage;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.common.primitives.Bytes;

import org.insa.cipherdit.reddit.things.RedditPost;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.List;
import co.junwei.cpabe.Cpabe;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.util.Base64;

/**
 * TODO par le groupe qui fait ABE+AES
 * Permet de gérer l'encryption
 */

public class Cipher {

    // PUBLIC INTERFACE FOR CipherStorage
    public void setupCipher() throws Exception {
        cpabe = new Cpabe();
        // Setting up public parameter and master key
        cpabe.setup(pubfile, mskfile);

        //ADDING A CHECK

    }

    public String generatePrivateKey(List<String> attributes){

        cpabe.dec(pubfile,prvfile,mskfile,ListToAttributeString(attributes));
        BufferedReader br = new BufferedReader(new FileReader(prvfile));
        return br.readLine();

    }

    public boolean Encrypt(byte[] file_bytes, List<String> attributes) {

        counter++;
        try{

            CipherCouple CC = new CipherCouple(file_bytes, ListToPolicyString(attributes));

            //Upload CipherCouple.AES_EncFile CipherCouple.ABE_EncKey

            return true;
        } catch(E Exception){
            return false;
        }
    }

    public byte[] Decrypt(File ABE_EncKey, String Pseudo) {

        counter++;
        try{

            String ABE_Key = ""; //GET KEY FUNCTION PPSN2

            String AES_Key = CheckAccess(ABE_Enckey, ABE_Key);

            if (AES_Key == null){return null;} 

            //DL FILE

            aesD.init(AES_Key.substring(11));

            return aesD.decryptAES(encryptedMessage);; 
        } catch(e Exception){
            return null;
        }

    }

    private String CheckAccess(File ABE_Enckey, String ABE_Key) {
        String file = "AES_Key" + Integer.toString(counter); 
        Path encpathkey = Files.createFile(Paths.get(dir + "ABE_Key"));
        BufferedWriter writer = new BufferedWriter(new FileWriter(encpathkey.toFile()));
        writer.write(ABE_Key);
        cpabe.dec(pubfile,ABE_Enckey.getAbsolutePath().toString(),encpathkey.toString(),new File.createTempFile(dir_post_files,file));

        BufferedReader br = new BufferedReader(new FileReader(file));
        String AES_Key = br.readLine();

        if(!AES_Key.startsWith("clabonnecle")){AES_Key = null;}

        return AES_Key;
    }

    

    public class CipherCouple {
        /*
         * Une cle K generee a partir d'un hash du fichier de départ et encryptee avec
         * CPABE
         * & le fichier encrypte avec AES
         */
        private String ABE_EncKey = dir + "/EncKey";
        private String AES_EncFile;

        @RequiresApi(api = Build.VERSION_CODES.O)
        public CipherCouple(byte[] file_bytes, String policy) throws Exception {

            // AES
            aesE.init();
            String AESString = aesE.encryptAES(file_bytes);
            String keyAES = aesE.exportKey();

            Path encpathfile = Files.createFile(Paths.get(dir + "/EncFile"));
            BufferedWriter writer1 = new BufferedWriter(new FileWriter(encpathfile.toFile()));
            writer.write(AESString);

            Path encpathkey = Files.createFile(Paths.get(dir + "/NotCryptedKey"));
            BufferedWriter writer2 = new BufferedWriter(new FileWriter(encpathkey.toFile()));
            writer.write("clabonnecle" + keyAES);
            
            //ABE
            cpabe.enc(pubfile, policy, encpath.toString(), ABE_EncKey);

        }

        // Pour le groupe d'Aubry
        public String getCipheredKey() {
            return ABE_EncKey;
        }

        public String getCipheredFile() {
            return AES_EncFile;
        }
    }

    /**
     * Possible KEY_SIZE values are 128, 192 and 256
     * Possible T_LEN values are 128, 120, 112, 104 and 96
     */

    public class EncryptAES {

        // AES Setup
        private SecretKey key;
        private int KEY_SIZE = 256;
        private int T_LEN = 128;
        private byte[] IV;

        public void init() throws Exception {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(KEY_SIZE);
            key = generator.generateKey();
        }

        public String encryptAES(byte[] fileInBytes) throws Exception {
            Cipher encryptionCipher = Cipher.getInstance("AES/GCM/NoPadding");
            encryptionCipher.init(Cipher.ENCRYPT_MODE, key);
            IV = encryptionCipher.getIV();
            byte[] encryptedBytes = encryptionCipher.doFinal(messageInBytes);
            return encode(encryptedBytes);
        }

        public String exportKey() {
            return encode(key.getEncoded());
        }

        public String exportKeys() {
            ArrayList<String> keys = new ArrayList<>();
            keys.add(encode(IV));
            keys.add(encode(key.getEncoded()));
            return keys;
        }

        private String encode(byte[] data) {
            return Base64.getEncoder().encodeToString(data);
        }

        private byte[] decode(String data) {
            return Base64.getDecoder().decode(data);
        }
    }

    public class DecryptAES {

        // AES Setup
        private SecretKey key;
        private int KEY_SIZE = 128;
        private int T_LEN = 128;
        private byte[] IV;

        public void init(String secretKey) {
            key = new SecretKeySpec(decode(secretKey), "AES");
            encryptionCipher.init(Cipher.ENCRYPT_MODE, key);
            IV = encryptionCipher.getIV();
        }

        public void init2(String secretKey, String IV) {
            key = new SecretKeySpec(decode(secretKey), "AES");
            this.IV = decode(IV);
        }

        private byte[] decryptAES(String encryptedMessage) throws Exception {
            byte[] messageInBytes = decode(encryptedMessage);
            Cipher decryptionCipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(T_LEN, IV);
            decryptionCipher.init(Cipher.DECRYPT_MODE, key, spec);
            byte[] decryptedBytes = decryptionCipher.doFinal(messageInBytes);
            return decryptedBytes;
        }

        private String encode(byte[] data) {
            return Base64.getEncoder().encodeToString(data);
        }

        private byte[] decode(String data) {
            return Base64.getDecoder().decode(data);
        }
    }

    public static void main(String[] args) {
        try {
            byte[] messageInBytes = "test".getBytes();
            aesE.init();
            String AES_EncFile = aesE.encryptAES(messageInBytes);
            String keyAES = aesE.exportKey();
            aesD.init(keyAES);
            byte[] message = aesD.decryptAES(AES_EncFile, keyAES);
            System.err.println("Decrypted Message : " + String(message));
        } catch (Exception ignored) {
        }
    }

    /*---------------------------------------------------------------------*/
    // PRIVATE

    // AES related
    static private EncryptAES aesE = new EncryptAES();;
    static private DecryptAES aesD = new DecryptAES();;

    // CPABE related
    static private Cpabe cpabe;
    static private String dir = "app/src/main/java/org/insa/cipherdit/cipherstorage";
    static private String pubfile = dir + "/pub_key";
    static private String mskfile = dir + "/master_key";
    static private String prvfile = dir + "/prv_key";
    static private String dir_post_files = dir + "/post_files";
    static private int counter = 0;

    private String ATTRIBUTES = "";

    private String ListToAttributeString(List<String> str_list) {
        String R = "";
        for (String at : str_list) {
            R += " " + at;
        }
        return R;
    }

    private String ListToPolicyString(List<String> str_list) {
        int count = 0;
        String R = "";
        for (String at : str_list) {
            count++;
            R += " " + at;
        }
        R+= " " + Integer.toString(count) + "of" + Integer.toString(count);
        return R;
    }

    /* private byte[] GetBytesFromPath(String path) throws IOException {
        File file = new File(path);
        byte[] bytes = new byte[(int) file.length()];
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            // read file into bytes[]
            fis.read(bytes);
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
        return bytes;
    } */

}
