package org.tron.studio.filesystem;

import java.io.File;

//Package visible
//Do not change to public
class Config {
    public static String SOLIDITY_SOURCE_PATH = System.getProperty("user.home") + "/TronStudio/source/";
    public static String TRACE_PATH = System.getProperty("user.home") + "/TronStudio/trace/";

    static {
        {
            File dir = new File(Config.SOLIDITY_SOURCE_PATH);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
        {
            File dir = new File(Config.TRACE_PATH);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
    }
}
