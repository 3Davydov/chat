package com.daniel.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesReader {

    Properties prop;

    public PropertiesReader() {
        prop = new Properties();
    }

    public void getAllProperties(String propFileName) {

        InputStream inputPropFile;
        inputPropFile = PropertiesReader.class.getResourceAsStream(propFileName);
        try{
            prop.load(inputPropFile);
        }
        catch (IOException err) {
            System.out.println("ERROR DURING LOADING FILE " + inputPropFile.toString());
        }
    }

    public boolean getLogFlag() {
        return Boolean.valueOf(prop.getProperty("logSale"));
    }

    public int getPort() {
        return Integer.valueOf(prop.getProperty("port"));
    }

    public int getRecentMessagesCount() {
        return Integer.valueOf(prop.getProperty("recentMessages"));
    }

    public String getProtocol() {
        return String.valueOf(prop.getProperty("protocol"));
    }
}
