/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.xtest.plugin;

import java.io.*;

/*
 * PluginManagerStorageSupport.java
 *
 * Class used to store PluginManager (serialized to bytestream) in a file,
 * so it can be retrieved for usage from different classloaders.
 *
 * Created on July 23, 2003, 2:08 PM
 *
 * @author  mb115822
 */
public class PluginManagerStorageSupport {
    
     private static final String PLUGIN_MANAGER_SERIALIZED_FILE_NAME = System.getProperty("java.io.tmpdir")+File.separator+"xtestPluginManager.ser";
     // property name to indicate that PluginManager was not yet serialized in this JVM
     private static final String PLUGIN_MANAGER_SYSTEM_PROPERTY_NAME="xtest.internal.serialized.pluginManager";

    
    /** Creates a new instance of PluginManagerStorageSupport */
    private PluginManagerStorageSupport() {
    }
    
     // static methods for storing/retrieving PluginManager, so it survives classloader change
    public static PluginManager retrievePluginManager() {
        File serializedFile = new File(PLUGIN_MANAGER_SERIALIZED_FILE_NAME);
        if(System.getProperty(PLUGIN_MANAGER_SYSTEM_PROPERTY_NAME) != null && serializedFile.exists()) {
            try {
                FileInputStream in = new FileInputStream(serializedFile);
                ObjectInputStream s = new ObjectInputStream(in);
                Object pluginManagerObject = s.readObject();
                if (pluginManagerObject instanceof PluginManager) {
                    return (PluginManager)pluginManagerObject;
                }
            } catch (FileNotFoundException fnfe) {
                System.out.println("Exception when retrieving PluginManager: "+fnfe);
                fnfe.printStackTrace();
            } catch (IOException ioe) {
                System.out.println("Exception when retrieving PluginManager: "+ioe);
                ioe.printStackTrace();
            } catch (ClassNotFoundException cnfe) {
                System.out.println("Exception when retrieving PluginManager: "+cnfe);
                cnfe.printStackTrace();
            }
        }
        return null;
    }
    
    public static void storePluginManager(PluginManager pluginManager) throws IOException {
        FileOutputStream out = new FileOutputStream(PLUGIN_MANAGER_SERIALIZED_FILE_NAME);
        ObjectOutputStream s = new ObjectOutputStream(out);
        s.writeObject(pluginManager);
        s.close();
        out.close();
        // indicate the PluginManager was stored
        System.setProperty(PLUGIN_MANAGER_SYSTEM_PROPERTY_NAME, "true");
    }
    
}
