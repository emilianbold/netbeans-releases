/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.iep.model.lib;


import java.io.FileInputStream;
import java.io.InputStream;

import java.net.URL;

import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * Reads a global configuration file and stores the variables
 *
 * @see java.util.Properties
 */
public class Configuration {
    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(Configuration.class.getName());

    /**
     * Stores the global properties
     */
    private static ConfigProperties mProps;

    static {
        mProps = new ConfigProperties();
        try {
            InputStream fis = IOUtil.getResourceAsStream("config/config.properties");
            mProps.load(fis);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            //mLog.debug("============IEP Properties Beg===========");
            //mProps.store(System.out, "");
            //mLog.debug("============IEP Properties End===========");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns an Enumeration of all keys found in the configuration file
     *
     * @return An Enumeration of all keys found in the configuration file
     */
    public static Enumeration getAllKeys() {
        return mProps.propertyNames();
    }

    /**
     * Returns a copy of the global properties
     *
     * @return The properties value
     */
    public static java.util.Properties getProperties() {
        return new java.util.Properties(mProps);
    }

    /**
     * set an entry after file load
     *
     * @param s1 property entry
     * @param s2 property entry
     */
    public static void setPropertyEntry(String s1, String s2) {
        if ((s1 != null) && (s2 != null)) {
            mProps.setProperty(s1, s2);
        }
    }

    /**
     * Returns a String value corresponding to the config variables specified
     * by name
     *
     * @param name Config variable to find in the config file
     *
     * @return Value found in the config file for the variable specified by
     *         name
     */
    public static String getVarByName(String name) {
        String val = mProps.getProperty(name);
        return (val != null) ? val.trim() : null;
    }

    /**
     * Returns a Collection of String values corresponding to the config
     * variables specified. The raw config entry is separated by the the
     * separator String.
     *
     * @param name Description of the Parameter
     *
     * @return The varListByName value
     */
    public static LinkedList getVarListByName(String name) {
        return getVarListByName(name, ",");
    }

    /**
     * Returns a Collection of String values corresponding to the config
     * variables specified. The raw config entry is separated by the the
     * separator String.
     *
     * @param name Description of the Parameter
     * @param separator Description of the Parameter
     *
     * @return The varListByName value
     */
    public static LinkedList getVarListByName(String name, String separator) {
        LinkedList varList = new LinkedList();
        String unparsedVar = getVarByName(name);

        // This means that the var didn't exist in mos.properties
        // It's better to return an empty LinkedList than null
        if (unparsedVar != null) {
            StringTokenizer token = new StringTokenizer(unparsedVar, separator);
            while (token.hasMoreTokens()) {
                varList.add(token.nextToken().trim());
            }
        }

        return varList;
    }

    /**
     * main() method this is used as a test method for this class
     *
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        try {
            Enumeration enumVar = mProps.keys();

            while (enumVar.hasMoreElements()) {
                String keyVar = (String) enumVar.nextElement();
                String valVar = (String) mProps.getProperty(keyVar);

                mLog.info(" >>> key:  " + keyVar + " >>> value:   "
                                   + valVar);
            }
        } catch (Exception de) {
            System.err.println("\r\n ERROR:   " + de.getMessage());
        }
    }
}

