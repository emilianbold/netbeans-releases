/*
 *                         Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with 
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the Ant module
 * The Initial Developer of the Original Code is Jayme C. Edwards.
 * Portions created by Jayme C. Edwards are Copyright (c) 2001.
 * All Rights Reserved.
 *
 * Contributor(s): Jesse Glick.
 */

package org.apache.tools.ant.module;

import java.io.File;
import java.util.HashMap;
import java.util.Properties;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.*;
import org.openide.compiler.*;
import org.openide.execution.*;
import org.openide.filesystems.FileSystemCapability;
import org.openide.options.SystemOption;
import org.openide.util.*;

import org.apache.tools.ant.module.api.IntrospectedInfo;
import org.apache.tools.ant.module.bridge.AntBridge;
import org.apache.tools.ant.module.loader.AntCompilerSupport;
import org.apache.tools.ant.module.run.AntExecutor;
import org.openide.modules.InstalledFileLocator;

public class AntSettings extends SystemOption implements ChangeListener {

    public static final String PROP_VERBOSITY = "verbosity"; // NOI18N
    public static final String PROP_PROPERTIES = "properties"; // NOI18N
    public static final String PROP_SAVE_ALL = "saveAll"; // NOI18N
    public static final String PROP_CUSTOM_DEFS = "customDefs"; // NOI18N
    public static final String PROP_COMPILER = "compiler"; // NOI18N
    public static final String PROP_EXECUTOR = "executor"; // NOI18N
    public static final String PROP_REUSE_OUTPUT = "reuseOutput"; // NOI18N
    public static final String PROP_ANT_VERSION = "antVersion"; // NOI18N
    public static final String PROP_INPUT_HANDLER = "inputHandler"; // NOI18N
    public static final String PROP_ANT_HOME = "antHome"; // NOI18N
    public static final String PROP_EXTRA_CLASSPATH = "extraClasspath"; // NOI18N
    
    private static final String DEF_CLASS_PATH = "netbeans.class.path"; // NOI18N
    private static final String DEF_BOOTCLASS_PATH = "netbeans.bootclass.path"; // NOI18N
    private static final String DEF_LIBRARY_PATH = "netbeans.library.path"; // NOI18N
    private static final String DEF_FILESYSTEMS_PATH = "netbeans.filesystems.path"; // NOI18N

    private static final long serialVersionUID = -4457782585534082966L;
    
    protected void initialize () {
        super.initialize();
        setVerbosity(2 /*Project.MSG_INFO*/);
        Properties p = new Properties ();
        // Enable hyperlinking for Jikes:
        p.setProperty ("build.compiler.emacs", "true"); // NOI18N
        p.setProperty ("build.sysclasspath", "ignore"); // #9527 NOI18N
        setProperties (p);
        setReuseOutput (false);
        setSaveAll (true);
        setCustomDefs (new IntrospectedInfo ());
        setCompiler (AntCompilerSupport.NoCompiler.NO_COMPILER);
        Executor exec = Executor.find (AntExecutor.class);
        if (exec == null)
            exec = new AntExecutor();
        setExecutor (exec);
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
        HashMap m = (HashMap)getProperty(PROP_PROPERTIES);
        Properties p = new Properties();
        p.putAll(m);
        p.setProperty (DEF_CLASS_PATH, getClassPath(NbClassPath.createClassPath ()));
        p.setProperty (DEF_BOOTCLASS_PATH, getClassPath(NbClassPath.createBootClassPath ()));
        p.setProperty (DEF_LIBRARY_PATH, getClassPath(NbClassPath.createLibraryPath ()));
        p.setProperty (DEF_FILESYSTEMS_PATH, getClassPath(NbClassPath.createRepositoryPath (FileSystemCapability.EXECUTE)));
        return p;
    }
    
    // See #17148: NbClassPath sticks extra quotes on classpaths for some reason.
    private static String getClassPath(NbClassPath p) {
        String s = p.getClassPath();
        if (s.startsWith("\"") && s.endsWith("\"")) { // NOI18N
            return s.substring(1, s.length() - 1);
        } else {
            return s;
        }
    }

    public void setProperties (Properties p) {
        // #16003: don't actually store a Properties; store something else,
        // to ensure that SystemOption will store what we actually keep here,
        // which will *not* contain the r/o properties DEF_CLASS_PATH etc.
        // Old serialized settings will still call this setter and so we
        // will upgrade storage; subsequent saves will only serialize the smaller
        // HashMap, without all of these useless keys.
        HashMap m = new HashMap(p);
        m.remove(DEF_CLASS_PATH);
        m.remove(DEF_BOOTCLASS_PATH);
        m.remove(DEF_LIBRARY_PATH);
        m.remove(DEF_FILESYSTEMS_PATH);
        putProperty (PROP_PROPERTIES, m, true);
    }
    
    public boolean getSaveAll () {
        return ((Boolean) getProperty (PROP_SAVE_ALL)).booleanValue ();
    }
    
    public void setSaveAll (boolean sa) {
        putProperty (PROP_SAVE_ALL, sa ? Boolean.TRUE : Boolean.FALSE, true);
    }
    
    public IntrospectedInfo getCustomDefs () {
        return (IntrospectedInfo) getProperty (PROP_CUSTOM_DEFS);
    }
    
    public void setCustomDefs (IntrospectedInfo ii) {
        putProperty (PROP_CUSTOM_DEFS, ii, true);
        ii.addChangeListener(WeakListener.change(this, ii));
        // Ideally would also remove listener from old one, but in practice
        // identity of this object never changes so it does not really matter...
    }

    /** @return CompilerType */
    public CompilerType getCompiler() {
        CompilerType.Handle compilerType = (CompilerType.Handle) getProperty (PROP_COMPILER);
        CompilerType c = (CompilerType) compilerType.getServiceType ();
        if (c != null) {
            return c;
        } else {
            // #15256
            return AntCompilerSupport.NoCompiler.NO_COMPILER;
        }
    }

    /** Uses given CompilerType */
    public void setCompiler(CompilerType ct) {
        putProperty(PROP_COMPILER, new CompilerType.Handle(ct), true);
    }

    /** @return Executor */
    public Executor getExecutor() {
        ServiceType.Handle serviceType = (ServiceType.Handle) getProperty(PROP_EXECUTOR);
        Executor e = (Executor) serviceType.getServiceType();
        if (e != null) {
            return e;
        } else {
            return new AntExecutor();
        }
    }

    /** sets an executor */
    public void setExecutor(Executor ct) {
        putProperty(PROP_EXECUTOR, new ServiceType.Handle(ct), true);
    }
    
    /** If true, Ant Execution uses always the same Output tab. */
    public boolean getReuseOutput () {
        return ((Boolean) getProperty (PROP_REUSE_OUTPUT)).booleanValue ();
    }
    /** Sets the reuseOutput property. */
    public void setReuseOutput (boolean b) {
        putProperty (PROP_REUSE_OUTPUT, b ? Boolean.TRUE : Boolean.FALSE, true);
    }
    
    // #14993: read-only property for the version of Ant
    public String getAntVersion() {
        String v = (String)getProperty(PROP_ANT_VERSION);
        if (v == null) {
            v = AntBridge.getInterface().getAntVersion();
            putProperty(PROP_ANT_VERSION, v, false);
        }
        return v;
    }
    
    public void stateChanged(ChangeEvent e) {
        // [PENDING] Should not be necessary, but see #15825.
        firePropertyChange(PROP_CUSTOM_DEFS, null, null);
    }
    
    public String getInputHandler() {
        String str = (String)getProperty (PROP_INPUT_HANDLER);
        if (str == null)
            str = "";
        return str;
    }

    public void setInputHandler(String inputHandler) {
        putProperty (PROP_INPUT_HANDLER, inputHandler, true);
    }
    
    public File getAntHome() {
        File f = (File)getProperty(PROP_ANT_HOME);
        if (f == null) {
            f = InstalledFileLocator.getDefault().locate("ant", "org.apache.tools.ant.module", false); // NOI18N
            putProperty(PROP_ANT_HOME, f, false);
        }
        return f;
    }
    
    public void setAntHome(File f) {
        putProperty(PROP_ANT_HOME, f, true);
        putProperty(PROP_ANT_VERSION, null, false);
        firePropertyChange(PROP_ANT_VERSION, null, null);
    }
    
    public NbClassPath getExtraClasspath() {
        NbClassPath p = (NbClassPath)getProperty(PROP_EXTRA_CLASSPATH);
        if (p == null) {
            // XXX could perhaps populate with xerces.jar:dom-ranges.jar
            // However currently there is no sure way to get the "good" Xerces
            // from libs/xerces (rather than the messed-up one from xml/tax)
            // without hardcoding the JAR name, which seems unwise since it is
            // definitely subject to change.
            p = new NbClassPath(new File[0]);
            putProperty(PROP_EXTRA_CLASSPATH, p, false);
        }
        return p;
    }
    
    public void setExtraClasspath(NbClassPath p) {
        putProperty(PROP_EXTRA_CLASSPATH, p, true);
    }
    
}
