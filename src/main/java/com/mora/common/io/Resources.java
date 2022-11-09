/**
 * Resources.java
 * 
 * @author  Veera
 * @version 1.0
 * @since   2022-11-07 
 */
package com.mora.common.io;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Resources {
    private static final Logger logger = LogManager.getLogger(Resources.class);

    /**
     * Returns the given file absolute path if exists in resources
     * 
     * @param fileName
     * @return
     */
    public static String getFilePath(String fileName) {
        String absolutePath = "";
        try {
            Resources resourcesaa = new Resources();
            File file = resourcesaa.getFileFromResource(fileName);
            absolutePath = file.getPath();
            logger.debug("Resources - Get File Path - " + absolutePath);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Resources - Get File Path - Error - ", e);
        }
        return absolutePath;
    }

    private File getFileFromResource(String fileName) throws URISyntaxException {

        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return new File(resource.toURI());
        }

    }

}