package org.insa.cipherdit.postGenerator;

import static org.insa.cipherdit.postGenerator.postGenerator.hashPost;

import org.insa.cipherdit.databaseCheck.databaseCheck;
import org.insa.cipherdit.reddit.things.RedditPost;

public class postGetter {
    static databaseCheck databaseChecker;
    public postGetter(databaseCheck checker){
        //databaseCheck databaseChecker = new databaseCheck("test");
        databaseChecker = checker;
    }

    //TODO: check if it has a hidden message and get it from dropbox (or else)
    public static RedditPost getPostToRender(RedditPost src)
    {
        String id = hashPost(src);

        RedditPost newSrc = databaseChecker.post(id);

        if (newSrc == null) return src;
        else return newSrc;
    }

}
