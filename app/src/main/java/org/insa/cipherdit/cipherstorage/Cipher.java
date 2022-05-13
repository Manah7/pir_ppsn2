package org.insa.cipherdit.cipherstorage;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.common.primitives.Bytes;

//import org.insa.cipherdit.reddit.things.RedditPost;

import java.io.BufferedWriter;
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

    private String dir_post_files = "./post_files";

    // PUBLIC INTERFACE FOR CipherStorage
    public void setupCipher() throws Exception {
        cpabe = new Cpabe();
        // Setting up public parameter and master key
        cpabe.setup(pubfile, mskfile);

    }

    public void setupAES() throws Exception {
        aesE = new EncryptAES();
        aesD = new DecryptAES();
    }

    public File Cipher(String post, List<String> attributes) {

        // Transform the RedditPost into a file
        // Create the file
        // String file_name = null; //pb avec le nom des fichiers
        // File post_file = new File.createTempFile(this.dir_post_files, file_name);
        // Open an output stream
        // ObjectOutputStream oos = new ObjectOutputStream(new
        // FileOutputStream(post_file)) ;
        // serialization de l'objet
        // ObjectOutputStream oos = new ObjectOutputStream(post) ;
        // ByteArrayOutputStream()
        // oos.writeObject(post) ;
        // oos.close();
        // now the file contains the serialized post

        // encryption and storage

        // return encrypted_file;
        return null;
    }

    //
    public boolean CheckAccess(String ciphered_file_path) {
        return false;
    }

    public RedditPost Decipher(String ciphered_file_path) {

        // decrypter

        // File decyphered_file = new File(deciphered_file_path) ; // � remplir

        // ouverture d'un flux sur un fichier
        // ObjectInputStream ois = new ObjectInputStream(new
        // FileInputStream(decyphered_file)) ;
        // d�s�rialization de l'objet
        // RedditPost result = (RedditPost)ois.readObject() ;

        // return result;
        return null;
    }

    public class CipherCouple {
        /*
         * Une cle K generee a partir d'un hash du fichier de départ et encryptee avec
         * CPABE
         * & le fichier encrypte avec AES
         */
        private String ABE_EncKey;
        private String AES_EncFile;

        @RequiresApi(api = Build.VERSION_CODES.O)
        public void CipherCouple(String file_path, String policy) throws Exception {
            /*
             * Generate Key from File hash (De Ludo, la cle ne vient pas du Hash, AES en
             * cree une)
             * Encrypt Key with CPABE using attributes
             * Encrypt File with AES
             */
            // Generate Key from File hash + Encrypt Key with CPABE
            byte[] file_bytes = GetBytesFromPath(file_path);
            // Creating the MessageDisgest object

            // Generating private key

            // AES
            aesE.init();
            AES_EncFile = aesE.encryptAES(file_bytes);
            String keyAES = aesE.exportKey();

            Path encpath = Files.createFile(Paths.get(dir + "enckey"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(encpath.toFile()));
            writer.write("clabonnecle" + keyAES);

            // Possible probleme avec le fait que AES requiert une cle + une matric IV j'ai
            // mis les deux methode init et export necessaire si l'une ou l'autre ne marche
            // pas
            // mais si on a besoin des deux il faudra modif des truc.

            // cpabe.keygen(pubfile,prvfile,mskfile,ATTRIBUTES); wrong spot
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
        private int KEY_SIZE = 128;
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

        private byte[] decryptAES(String encryptedMessage, SecretKey key) throws Exception {
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
    static private EncryptAES aesE;
    static private DecryptAES aesD;

    // CPABE related
    static private Cpabe cpabe;
    static private String dir = "app/src/main/java/org/insa/cipherdit/cipherstorage";
    static private String pubfile = dir + "/pub_key";
    static private String mskfile = dir + "/master_key";
    static private String prvfile = dir + "/prv_key";

    private String ATTRIBUTES = "";

    private String ListToAttributeString(List<String> str_list) {
        String R = "";
        for (String at : str_list) {
            R += " " + at;
        }
        return R;
    }

    private byte[] GetBytesFromPath(String path) throws IOException {
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
    }

}
