/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.apache.tools.ant.module;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.module.api.IntrospectedInfo;
import org.apache.tools.ant.module.bridge.AntBridge;
import org.apache.tools.ant.module.spi.AutomaticExtraClasspathProvider;
import org.openide.ErrorManager;
import org.openide.execution.NbClassPath;
import org.openide.modules.InstalledFileLocator;
import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

public class AntSettings extends SystemOption implements ChangeListener {

    public static final String PROP_VERBOSITY = "verbosity"; // NOI18N
    public static final String PROP_PROPERTIES = "properties"; // NOI18N
    public static final String PROP_SAVE_ALL = "saveAll"; // NOI18N
    public static final String PROP_CUSTOM_DEFS = "customDefs"; // NOI18N
    public static final String PROP_ANT_VERSION = "antVersion"; // NOI18N
    public static final String PROP_ANT_HOME = "antHome"; // NOI18N
    public static final String PROP_EXTRA_CLASSPATH = "extraClasspath"; // NOI18N
    public static final String PROP_AUTOMATIC_EXTRA_CLASSPATH = "automaticExtraClasspath"; // NOI18N
    public static final String PROP_AUTO_CLOSE_TABS = "autoCloseTabs"; // NOI18N
    public static final String PROP_ALWAYS_SHOW_OUTPUT = "alwaysShowOutput"; // NOI18N
    
    private static final long serialVersionUID = -4457782585534082966L;
    
    /**
     * Transient value of ${ant.home} unless otherwise set.
     * @see "#43522"
     */
    private static File defaultAntHome = null;
    
    protected void initialize () {
        super.initialize();
        setVerbosity(2 /*Project.MSG_INFO*/);
        Properties p = new Properties ();
        // Enable hyperlinking for Jikes:
        p.setProperty ("build.compiler.emacs", "true"); // NOI18N
        setProperties (p);
        setSaveAll (true);
        setCustomDefs (new IntrospectedInfo ());
        setAutoCloseTabs(true); // #47753
        setAlwaysShowOutput(false);
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
        return p;
    }
    
    public void setProperties (Properties p) {
        HashMap m = new HashMap(p);
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
        ii.addChangeListener(WeakListeners.change(this, ii));
        // Ideally would also remove listener from old one, but in practice
        // identity of this object never changes so it does not really matter...
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
    
    static File getDefaultAntHome() {
        if (defaultAntHome == null) {
            File antJar = InstalledFileLocator.getDefault().locate("ant/lib/ant.jar", "org.apache.tools.ant.module", false); // NOI18N
            assert antJar != null : "Missing binding for ant/lib/ant.jar in InstalledFileLocator";
            defaultAntHome = antJar.getParentFile().getParentFile();
            if (AntModule.err.isLoggable(ErrorManager.INFORMATIONAL)) {
                AntModule.err.log("getDefaultAntHome: " + defaultAntHome);
            }
        }
        assert defaultAntHome != null;
        return defaultAntHome;
    }
    
    /**
     * Get the Ant installation to use.
     */
    public File getAntHomeWithDefault() {
        File f = getAntHome();
        if (AntModule.err.isLoggable(ErrorManager.INFORMATIONAL)) {
            AntModule.err.log("getAntHomeWithDefault: antHome=" + f);
        }
        if (f == null) {
            // Not explicitly configured. Check default.
            f = getDefaultAntHome();
        }
        assert f != null;
        return f;
    }

    /**
     * For serialization only.
     * May return null.
     * Use {@link #getAntHomeWithDefault} instead.
     */
    public File getAntHome() {
        return (File)getProperty(PROP_ANT_HOME);
    }
    
    public void setAntHome(File f) {
        if (AntModule.err.isLoggable(ErrorManager.INFORMATIONAL)) {
            AntModule.err.log("setAntHome: " + f);
        }
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
    
    public NbClassPath getAutomaticExtraClasspath() {
        NbClassPath p = (NbClassPath)getProperty(PROP_AUTOMATIC_EXTRA_CLASSPATH);
        if (p != null) {
            return p;
        } else {
            return defaultAutomaticExtraClasspath();
        }
    }
    
    public void setAutomaticExtraClasspath(NbClassPath p) {
        putProperty(PROP_AUTOMATIC_EXTRA_CLASSPATH, p, true);
    }
    
    private NbClassPath defAECP = null;
    private Lookup.Result aecpResult = null;
    
    private synchronized NbClassPath defaultAutomaticExtraClasspath() {
        if (aecpResult == null) {
            aecpResult = Lookup.getDefault().lookup(new Lookup.Template(AutomaticExtraClasspathProvider.class));
            aecpResult.addLookupListener(new LookupListener() {
                public void resultChanged(LookupEvent ev) {
                    defAECP = null;
                    firePropertyChange(PROP_AUTOMATIC_EXTRA_CLASSPATH, null, null);
                }
            });
        }
        if (defAECP == null) {
            List/*<File>*/ items = new ArrayList();
            Iterator it = aecpResult.allInstances().iterator();
            while (it.hasNext()) {
                AutomaticExtraClasspathProvider provider = (AutomaticExtraClasspathProvider)it.next();
                items.addAll(Arrays.asList((Object[])provider.getClasspathItems()));
            }
            defAECP = new NbClassPath((File[])items.toArray(new File[items.size()]));
        }
        return defAECP;
    }
    
    public boolean getAutoCloseTabs() {
        return ((Boolean) getProperty(PROP_AUTO_CLOSE_TABS)).booleanValue();
    }
    
    public void setAutoCloseTabs(boolean b) {
        putProperty(PROP_AUTO_CLOSE_TABS, Boolean.valueOf(b), true);
    }
    
    public boolean getAlwaysShowOutput() {
        return ((Boolean) getProperty(PROP_ALWAYS_SHOW_OUTPUT)).booleanValue();
    }
    
    public void setAlwaysShowOutput(boolean b) {
        putProperty(PROP_ALWAYS_SHOW_OUTPUT, Boolean.valueOf(b), true);
    }
    
}
