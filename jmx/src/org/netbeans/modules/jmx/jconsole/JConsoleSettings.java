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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.jmx.jconsole;

import java.io.File;
import java.util.HashMap;
import java.util.Properties;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.*;
//import org.openide.filesystems.FileSystemCapability;
import org.openide.options.SystemOption;
import org.openide.util.*;


public class JConsoleSettings extends SystemOption implements ChangeListener
{
    static final long serialVersionUID = -1055706114162507505L;

    private static final String PROP_POLLING     = "POLLING"; // NOI18N
    private static final String PROP_TILE   = "TILE"; // NOI18N
    private static final String PROP_CLASSPATH  = "CLASSPATH"; // NOI18N
    private static final String PROP_PLUGINSPATH  = "PLUGINS"; // NOI18N
    private static final String PROP_VM_OPTIONS = "VM_OPTIONS"; //NOI18N
    private static final String PROP_OTHER_ARGS = "OTHER_ARGS"; //NOI18N
    private static final String PROP_URL = "DEFAULT_URL"; //NOI18N
    
    public static String NETBEANS_CLASS_PATH;
    
    private static Boolean greater;
    static {
         NETBEANS_CLASS_PATH = System.getProperty("java.class.path");// NOI18N
    }
    
    protected void initialize () 
    {
        super.initialize();
        
        setPolling(4);
        setTile(Boolean.TRUE);   
    }
    
    public static boolean isNetBeansJVMGreaterThanJDK15() {
        if(greater == null) {
            //Check if we are running on 1.6 minimum
            try {
                Class.forName("javax.swing.SwingWorker");// NOI18N
               greater = true;
            }catch(ClassNotFoundException e) {
                greater = false;
            }
        }
        return greater;
    }
    
    public String displayName ()
    {
        return "JConsole settings";// NOI18N
    }    

    public static JConsoleSettings getDefault () 
    {
        return (JConsoleSettings) findObject (JConsoleSettings.class, true);
    }
    
    public void stateChanged (ChangeEvent e)
    {
    }
    
    public Boolean getTile()
    {
        return (Boolean)getProperty(PROP_TILE);
    }

    public void setTile(Boolean tile)
    {
        putProperty(PROP_TILE, tile);
    }
    
    public String getClassPath()
    {
        return (String)getProperty(PROP_CLASSPATH);
    }

    public void setClassPath(String value)
    {
        putProperty(PROP_CLASSPATH, value);
    }
    
    public String getPluginsPath()
    {
        return (String)getProperty(PROP_PLUGINSPATH);
    }

    public void setPluginsPath(String value)
    {
        putProperty(PROP_PLUGINSPATH, value);
    }
    
    public Integer getPolling()
    {
        Integer target = (Integer)getProperty (PROP_POLLING);
        return target;
    }
    
    public void setPolling(Integer polling)
    {   
        putProperty(PROP_POLLING, polling, true);
    }
    
    public String getVMOptions()
    {
        String other = (String)getProperty (PROP_VM_OPTIONS);
        return other;
    }
    
    public void setVMOptions(String other)
    {
        putProperty (PROP_VM_OPTIONS, other, true);
    }
    
    public String getOtherArgs()
    {
        return (String)getProperty (PROP_OTHER_ARGS);
    }
    
    public void setOtherArgs(String other)
    {
        putProperty (PROP_OTHER_ARGS, other, true);
    }
    
    public String getDefaultUrl()
    {
        String url = (String)getProperty (PROP_URL);
        return url;
    }
    
    public void setDefaultUrl(String url)
    {
        putProperty (PROP_URL, url, true);
    }
    
    public HelpCtx getHelpCtx () {
        return new HelpCtx("jconsole_standalone"); // NOI18N
    }
}
