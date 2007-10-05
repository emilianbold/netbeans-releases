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

/*
 * PluginsInitTask.java
 *
 * Takes care of plugin initialization - this task initializes PluginManager and
 * because of the way how Ant creates tasks (each task has it's own classloader),
 * it stored it as a serialized byte stream in system properties (for usage by other
 * tasks ...)
 *
 *
 * Created on July 22, 2003, 5:25 PM
 */

package org.netbeans.xtest.plugin;


import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;

import java.io.*;
import java.util.*;

/**
 * @author mb115822
 */
public class PluginsInitTask extends Task {
    
    

    
    private File pluginsHome;
    private String[] preferredPlugins;
    
    public void setPluginsHome(File pluginsHome) {
        this.pluginsHome = pluginsHome;
    }
    
    public void setPreferredPlugins(String preferredPluginsString) {
        // need to 
        StringTokenizer st = new StringTokenizer(preferredPluginsString," \t\n\r\f,;:");
        ArrayList tokenList = new ArrayList();
        while (st.hasMoreTokens()) {
            tokenList.add(st.nextToken());
        }
        if (tokenList.size() > 0) {
            preferredPlugins = (String[]) tokenList.toArray(new String[0]);
        }
    }
    
   
    
    
    
    public void execute() throws BuildException {
        // load PluginManager task by system classloader !!!
        /*
        try {
            Class.forName("org.netbeans.xtest.plugin.PluginManager");
            Thread.currentThread().getContextClassLoader().getSystemClassLoader().loadClass("org.netbeans.xtest.plugin.PluginManager");
            AntClassLoader.getSystemClassLoader().loadClass("org.netbeans.xtest.plugin.PluginManager");
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            throw new BuildException("Cannot load PM class!!! ",cnfe);
        }
         **/
        
        // because of the way how Ant treats tasks - PluginManager have to be stored
        // in system properties (which is just a hashtable, so it can store an object
        PluginManager mgr = PluginManagerStorageSupport.retrievePluginManager();
        //System.out.println("MGR is "+mgr);
        if (mgr != null) {
            //System.out.println("Got PluginManager !!!");
            return;
        }
        // 
        log("Creating XTest plugin manager");
        PluginManager pluginManager = PluginManager.getPluginManager();
        // get xtest.home ...
        String xtestHomeProperty = this.getProject().getProperty("xtest.home");
        File xtestHome = new File(xtestHomeProperty);        
        try {            
            
            log("Registering XTest plugins in "+pluginsHome);
            pluginManager.registerPlugins(xtestHome, pluginsHome);
            // register preferred plugins (if applicable)
            if (preferredPlugins != null) {
                log("Registering preferred plugins");
                pluginManager.registerPreferredPlugins(preferredPlugins);
            }
            // ok everything should be initialized - let's store plugin manager in a safe place
            PluginManagerStorageSupport.storePluginManager(pluginManager);            
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new BuildException("Cannot register XTest plugins. Reason: "+ioe.getMessage(),ioe);
        } catch (PluginConfigurationException pce) {
            pce.printStackTrace();
            throw new BuildException("Cannot register XTest plugins. Reason: "+pce.getMessage(),pce);
        }
    }
    
}
