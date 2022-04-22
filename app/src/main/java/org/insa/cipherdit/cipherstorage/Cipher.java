package org.insa.cipherdit.poststorage;

import org.insa.cipherdit.reddit.things.RedditPost;
import java.util.List;
import co.junwei.cpabe.Cpabe;


/**
 * TODO par le groupe qui fait ABE+AES
 * Permet de gérer l'encryption
 */

public class Cipher {

    //PUBLIC INTERFACE FOR CipherStorage
    public void setupCipher(){
        cpabe = new Cpabe() ;
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
        private Bytes CipheredKey ;
        private Bytes CipheredFile ;
        public void CipherCouple(Object File, List<String> attributes){
            /*
            Generate Key from File hash
            Encrypt Key with CPABE using attributes
            Encrypt File with AES
             */
            MessageDigest.getInstance("SHA-256").digest(File.getBytes);
            cpabe.() ;
        }

        //Pour le groupe d'Aubry
        public Object getCipheredKey() {
            return CipheredKey;
        }
        public Object getCipheredFile() {
            return CipheredFile;
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

    private String DEFAULT_POLICY = "" ;



}
