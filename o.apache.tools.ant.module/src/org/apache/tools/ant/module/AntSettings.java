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

package org.apache.tools.ant.module;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import org.apache.tools.ant.module.api.IntrospectedInfo;
import org.apache.tools.ant.module.bridge.AntBridge;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AutomaticExtraClasspathProvider;
import org.openide.ErrorManager;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbPreferences;

public class AntSettings {

    private static final String PROP_VERBOSITY = "verbosity"; // NOI18N
    private static final String PROP_PROPERTIES = "properties"; // NOI18N
    private static final String PROP_SAVE_ALL = "saveAll"; // NOI18N
    private static final String PROP_CUSTOM_DEFS = "customDefs"; // NOI18N
    private static final String PROP_ANT_VERSION = "antVersion"; // NOI18N
    public static final String PROP_ANT_HOME = "antHome"; // NOI18N
    public static final String PROP_EXTRA_CLASSPATH = "extraClasspath"; // NOI18N
    public static final String PROP_AUTOMATIC_EXTRA_CLASSPATH = "automaticExtraClasspath"; // NOI18N
    private static final String PROP_AUTO_CLOSE_TABS = "autoCloseTabs"; // NOI18N
    private static final String PROP_ALWAYS_SHOW_OUTPUT = "alwaysShowOutput"; // NOI18N

    private AntSettings() {}

    private static Preferences prefs() {
        return NbPreferences.forModule(AntSettings.class);
    }

    public static int getVerbosity() {
        return prefs().getInt(PROP_VERBOSITY, AntEvent.LOG_INFO);
    }

    public static void setVerbosity(int v) {
        prefs().putInt(PROP_VERBOSITY, v);
    }

    public static Map<String,String> getProperties() {
        Map<String,String> p = new HashMap<String,String>();
        // Enable hyperlinking for Jikes by default:
        for (String pair : prefs().get(PROP_PROPERTIES, "build.compiler.emacs=true").split("\n")) { // NOI18N
            String[] nameval = pair.split("=", 2); // NOI18N
            p.put(nameval[0], nameval[1]);
        }
        return p;
    }

    public static void setProperties(Map<String,String> p) {
        if (!(p instanceof SortedMap)) {
            p = new TreeMap<String,String>(p);
        }
        StringBuilder b = new StringBuilder();
        for (Map.Entry<String,String> pair : p.entrySet()) {
            if (b.length() > 0) {
                b.append('\n');
            }
            b.append(pair.getKey());
            b.append('=');
            b.append(pair.getValue());
        }
        prefs().put(PROP_PROPERTIES, b.toString());
    }

    public static boolean getSaveAll() {
        return prefs().getBoolean(PROP_SAVE_ALL, true);
    }

    public static void setSaveAll(boolean sa) {
        prefs().putBoolean(PROP_SAVE_ALL, sa);
    }

    private static IntrospectedInfo customDefs;
    static {
        new IntrospectedInfo(); // trigger IntrospectedInfo static block
    }
    public static synchronized IntrospectedInfo getCustomDefs() {
        if (customDefs == null) {
            customDefs = IntrospectedInfoSerializer.instance.load(prefs().node(PROP_CUSTOM_DEFS));
        }
        return customDefs;
    }

    public static synchronized void setCustomDefs(IntrospectedInfo ii) {
        IntrospectedInfoSerializer.instance.store(prefs().node(PROP_CUSTOM_DEFS), ii);
        customDefs = ii;
    }

    private static String antVersion;
    // #14993: read-only property for the version of Ant
    public static String getAntVersion() {
        if (antVersion == null) {
            antVersion = AntBridge.getInterface().getAntVersion();
        }
        return antVersion;
    }

    /**
     * Transient value of ${ant.home} unless otherwise set.
     * @see "#43522"
     */
    private static File defaultAntHome = null;

    private static synchronized File getDefaultAntHome() {
        if (defaultAntHome == null) {
            File antJar = InstalledFileLocator.getDefault().locate("ant/lib/ant.jar", "org.apache.tools.ant.module", false); // NOI18N
            if (antJar == null) {
                return null;
            }
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
     * Might be null!
     */
    public static File getAntHome() {
        String h = prefs().get(PROP_ANT_HOME, null);
        if (AntModule.err.isLoggable(ErrorManager.INFORMATIONAL)) {
            AntModule.err.log("getAntHomeWithDefault: antHome=" + h);
        }
        if (h != null) {
            return new File(h);
        } else {
            // Not explicitly configured. Check default.
            return getDefaultAntHome();
        }
    }

    public static void setAntHome(File f) {
        if (f != null && f.equals(getDefaultAntHome())) {
            f = null;
        }
        if (AntModule.err.isLoggable(ErrorManager.INFORMATIONAL)) {
            AntModule.err.log("setAntHome: " + f);
        }
        if (f != null) {
            prefs().put(PROP_ANT_HOME, f.getAbsolutePath());
        } else {
            prefs().remove(PROP_ANT_HOME);
        }
        antVersion = null;
        firePropertyChange(PROP_ANT_HOME);
    }

    public static List<File> getExtraClasspath() {
        // XXX could perhaps populate with xerces.jar:dom-ranges.jar
        // However currently there is no sure way to get the "good" Xerces
        // from libs/xerces (rather than the messed-up one from xml/tax)
        // without hardcoding the JAR name, which seems unwise since it is
        // definitely subject to change.
        List<File> files = new ArrayList<File>();
        for (String f : prefs().get(PROP_EXTRA_CLASSPATH, "").split(Pattern.quote(File.pathSeparator))) {
            files.add(new File(f));
        }
        return files;
    }

    public static void setExtraClasspath(List<File> p) {
        StringBuilder b = new StringBuilder();
        for (File f : p) {
            if (b.length() > 0) {
                b.append(File.pathSeparatorChar);
            }
            b.append(f);
        }
        prefs().put(PROP_EXTRA_CLASSPATH, b.toString());
        firePropertyChange(PROP_EXTRA_CLASSPATH);
    }

    private static List<File> defAECP = null;
    private static Lookup.Result<AutomaticExtraClasspathProvider> aecpResult = null;

    public static synchronized List<File> getAutomaticExtraClasspath() {
        if (aecpResult == null) {
            aecpResult = Lookup.getDefault().lookupResult(AutomaticExtraClasspathProvider.class);
            aecpResult.addLookupListener(new LookupListener() {
                public void resultChanged(LookupEvent ev) {
                    defAECP = null;
                    firePropertyChange(PROP_AUTOMATIC_EXTRA_CLASSPATH);
                }
            });
        }
        if (defAECP == null) {
            defAECP = new ArrayList<File>();
            for (AutomaticExtraClasspathProvider provider : aecpResult.allInstances()) {
                defAECP.addAll(Arrays.asList(provider.getClasspathItems()));
            }
        }
        return defAECP;
    }

    public static boolean getAutoCloseTabs() {
        return prefs().getBoolean(PROP_AUTO_CLOSE_TABS, /*#47753*/ true);
    }

    public static void setAutoCloseTabs(boolean b) {
        prefs().putBoolean(PROP_AUTO_CLOSE_TABS, b);
    }

    public static boolean getAlwaysShowOutput() {
        return prefs().getBoolean(PROP_ALWAYS_SHOW_OUTPUT, /* #87801 */false);
    }

    public static void setAlwaysShowOutput(boolean b) {
        prefs().putBoolean(PROP_ALWAYS_SHOW_OUTPUT, b);
    }

    private static final PropertyChangeSupport pcs = new PropertyChangeSupport(AntSettings.class);

    public static void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public static void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    private static void firePropertyChange(String prop) {
        pcs.firePropertyChange(prop, null, null);
    }

    public static abstract class IntrospectedInfoSerializer {
        public static IntrospectedInfoSerializer instance;
        public abstract IntrospectedInfo load(Preferences node);
        public abstract void store(Preferences node, IntrospectedInfo info);
    }

}
