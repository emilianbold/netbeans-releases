/*
 *                         Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with 
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the Ant module
 * The Initial Developer of the Original Code is Jayme C. Edwards.
 * Portions created by Jayme C. Edwards are Copyright (c) 2000.
 * All Rights Reserved.
 *
 * Contributor(s): Jesse Glick.
 */

package org.apache.tools.ant.module;

import java.util.Properties;

import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.apache.tools.ant.Project;

public class AntSettings extends SystemOption {

    public static final String PROP_VERBOSITY = "verbosity";
    public static final String PROP_PROPERTIES = "properties";
    public static final String PROP_SAVE_ALL = "saveAll";
    public static final String PROP_CUSTOM_DEFS = "customDefs";

    private static final long serialVersionUID = -4457782585534082966L;
    
    protected void initialize () {
        setVerbosity (Project.MSG_INFO);
        Properties p = new Properties ();
        // Enable hyperlinking for Jikes:
        p.setProperty ("build.compiler.emacs", "true"); // NOI18N
        setProperties (p);
        setSaveAll (true);
        setCustomDefs (new IntrospectedInfo ());
    }

    public String displayName () {
        return NbBundle.getMessage (AntSettings.class, "LBL_settings");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.apache.tools.ant.module.settings");
    }

    public static AntSettings getDefault () {
        return (AntSettings) findObject (AntSettings.class, true);
    }

    public int getVerbosity () {
        return ((Integer) getProperty (PROP_VERBOSITY)).intValue ();
    }

    public void setVerbosity (int v) {
        putProperty (PROP_VERBOSITY, new Integer (v), true);
    }

    public Properties getProperties () {
        return (Properties) getProperty (PROP_PROPERTIES);
    }

    public void setProperties (Properties p) {
        putProperty (PROP_PROPERTIES, p, true);
    }
    
    public boolean getSaveAll () {
        return ((Boolean) getProperty (PROP_SAVE_ALL)).booleanValue ();
    }
    
    public void setSaveAll (boolean sa) {
        putProperty (PROP_SAVE_ALL, new Boolean (sa), true);
    }
    
    public IntrospectedInfo getCustomDefs () {
        return (IntrospectedInfo) getProperty (PROP_CUSTOM_DEFS);
    }
    
    public void setCustomDefs (IntrospectedInfo ii) {
        putProperty (PROP_CUSTOM_DEFS, ii, true);
    }

}
