/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
