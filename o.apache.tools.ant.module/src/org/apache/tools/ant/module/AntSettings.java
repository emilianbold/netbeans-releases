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

import org.openide.execution.NbClassPath;
import org.openide.filesystems.FileSystemCapability;
import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.apache.tools.ant.Project;

import org.apache.tools.ant.module.api.IntrospectedInfo;

public class AntSettings extends SystemOption {

    public static final String PROP_VERBOSITY = "verbosity"; // NOI18N
    public static final String PROP_PROPERTIES = "properties"; // NOI18N
    public static final String PROP_SAVE_ALL = "saveAll"; // NOI18N
    public static final String PROP_CUSTOM_DEFS = "customDefs"; // NOI18N

    private static final String DEF_CLASS_PATH = "netbeans.class.path"; // NOI18N
    private static final String DEF_BOOTCLASS_PATH = "netbeans.bootclass.path"; // NOI18N
    private static final String DEF_LIBRARY_PATH = "netbeans.library.path"; // NOI18N
    private static final String DEF_FILESYSTEMS_PATH = "netbeans.filesystems.path"; // NOI18N

    private static final long serialVersionUID = -4457782585534082966L;
    
    protected void initialize () {
        setVerbosity (Project.MSG_INFO);
        Properties p = new Properties ();
        // Enable hyperlinking for Jikes:
        p.setProperty ("build.compiler.emacs", "true"); // NOI18N
        String dummy = "irrelevant"; // NOI18N
        p.setProperty (DEF_CLASS_PATH, dummy);
        p.setProperty (DEF_BOOTCLASS_PATH, dummy);
        p.setProperty (DEF_LIBRARY_PATH, dummy);
        p.setProperty (DEF_FILESYSTEMS_PATH, dummy);
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
        Properties p = (Properties) getProperty (PROP_PROPERTIES);
        if (p.containsKey (DEF_CLASS_PATH)) {
            p.setProperty (DEF_CLASS_PATH, NbClassPath.createClassPath ().getClassPath ());
        }
        if (p.containsKey (DEF_BOOTCLASS_PATH)) {
            p.setProperty (DEF_BOOTCLASS_PATH, NbClassPath.createBootClassPath ().getClassPath ());
        }
        if (p.containsKey (DEF_LIBRARY_PATH)) {
            p.setProperty (DEF_LIBRARY_PATH, NbClassPath.createLibraryPath ().getClassPath ());
        }
        if (p.containsKey (DEF_FILESYSTEMS_PATH)) {
            p.setProperty (DEF_FILESYSTEMS_PATH, NbClassPath.createRepositoryPath (FileSystemCapability.EXECUTE).getClassPath ());
        }
        return p;
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
