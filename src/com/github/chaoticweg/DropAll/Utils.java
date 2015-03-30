package com.github.chaoticweg.DropAll;

public class Utils {

    public static String parseItemFromUpText(String uptext) {
        // TODO
        String tmp = uptext.split("/")[0];

        int index = tmp.indexOf(" ");
        if (index >= 0)
            tmp = tmp.substring(tmp.indexOf(" "));

        return tmp.trim();
    }

}