/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


/*
 * PluginManagerStorageSupport.java
 *
 * Class used to store PluginManager (serialized to bytestream) in System Properties,
 * so it can be retrieved for usage from different classloaders.
 *
 * Created on July 23, 2003, 2:08 PM
 */

package org.netbeans.xtest.plugin;

import java.io.*;

/**
 *
 * @author  mb115822
 */
public class PluginManagerStorageSupport {
    
     private static final String PLUGIN_MANAGER_SYSTEM_PROPERTY_NAME="xtest.internal.serialized.pluginManager";
     
    
    /** Creates a new instance of PluginManagerStorageSupport */
    private PluginManagerStorageSupport() {
    }
    
     // static methods for storing/retrieving PluginManager, so it survives classloader change
    public static PluginManager retrievePluginManager() {
        Object obj = System.getProperties().get(PLUGIN_MANAGER_SYSTEM_PROPERTY_NAME);
        if (obj != null) {
            if (obj instanceof ByteArrayOutputStream) {
                try {
                    ByteArrayOutputStream baos = (ByteArrayOutputStream) obj;
                    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                    ObjectInputStream p = new ObjectInputStream(bais);
                    Object pobj = p.readObject();
                    bais.close();
                    if (pobj instanceof PluginManager) {
                        return (PluginManager)pobj;
                    }
                } catch (IOException ioe) {
                    System.out.println("Exception when retrieving PluginManager: "+ioe);
                    ioe.printStackTrace();
                } catch (ClassNotFoundException cnfe) {
                    System.out.println("Exception when retrieving PluginManager: "+cnfe);
                    cnfe.printStackTrace();
                }
            }
        }
        // else return null;
        return null;
    }
    
    public static void storePluginManager(PluginManager pluginManager) throws IOException {
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream p = new ObjectOutputStream(baos);
        p.writeObject(pluginManager);
        baos.close();

        System.getProperties().put(PLUGIN_MANAGER_SYSTEM_PROPERTY_NAME, baos);
    }
    
}
