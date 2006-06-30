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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

    // property name to save file name and to indicate that PluginManager was not yet serialized in this JVM
    private static final String PLUGIN_MANAGER_SYSTEM_PROPERTY_NAME="xtest.internal.serialized.pluginManager";
     
    
    /** Creates a new instance of PluginManagerStorageSupport */
    private PluginManagerStorageSupport() {
    }
    
     // static methods for storing/retrieving PluginManager, so it survives classloader change
    public static PluginManager retrievePluginManager() {
        String filename = System.getProperty(PLUGIN_MANAGER_SYSTEM_PROPERTY_NAME);
        if(filename != null) {
            File serializedFile = new File(filename);
            if(serializedFile.exists()) {
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
        }
        return null;
    }
    
    public static void storePluginManager(PluginManager pluginManager) throws IOException {
        String filename = System.getProperty("java.io.tmpdir")+File.separator+
                          "xtestPluginManager"+System.currentTimeMillis()+".ser";
        File file = new File(filename);
        file.deleteOnExit();
        FileOutputStream out = new FileOutputStream(file);
        ObjectOutputStream s = new ObjectOutputStream(out);
        s.writeObject(pluginManager);
        s.close();
        out.close();
        // set property to unique file name to be used when retrieving it and to indicate 
        // the PluginManager was stored
        System.setProperty(PLUGIN_MANAGER_SYSTEM_PROPERTY_NAME, filename);
    }
    
}
