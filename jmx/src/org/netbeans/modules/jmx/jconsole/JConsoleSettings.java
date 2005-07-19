/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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
    private static final String PROP_VM_OPTIONS = "VM_OPTIONS"; //NOI18N
    private static final String PROP_URL = "DEFAULT_URL"; //NOI18N
    private String cp;
    
    protected void initialize () 
    {
        super.initialize();
        
        setPolling(4);
        setTile(Boolean.TRUE);
        cp = System.getProperty("java.class.path");// NOI18N
        setClassPath(cp);
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
        //In case original classpath has been erased
        if(value == null) value = cp;
        if (!value.contains(cp))
            value = cp + value;
        
        putProperty(PROP_CLASSPATH, value);
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
