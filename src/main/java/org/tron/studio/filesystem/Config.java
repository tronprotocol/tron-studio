package org.tron.studio.filesystem;

import java.io.File;

//Package visible
//Do not change to public
class Config {
    public static String SOLIDITY_SOURCE_PATH = System.getProperty("user.home") + "/solidity/";


    static {
        File dir = new File(Config.SOLIDITY_SOURCE_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
}
