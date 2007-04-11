/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.settings;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import org.netbeans.modules.cnd.api.utils.Path;
import org.openide.ErrorManager;
import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Settings for the C/C++/Fortran. The compile/build options stored
 * in this class are <B>default</B> options which will be applied to new files.
 * Once a file has been created its options may diverge from the defaults. A
 * files options do not change if these default options are changed.
 */

public class CppSettings extends SystemOption {

    /** serial uid */
    static final long serialVersionUID = -2942467713237077336L;

    public static final int DEFAULT_PARSING_DELAY = 2000;
    private static final boolean DEFAULT_FORTRAN_ENABLED = false;   // disable Fortran by default

    // Properties
    public static final String PROP_PARSING_DELAY = "parsingDelay"; //NOI18N
    public static final String PROP_REPLACEABLE_STRINGS_TABLE = "replaceableStringsTable"; //NOI18N
    public static final String PROP_FREE_FORMAT_FORTRAN = "freeFormatFortran"; // NOI18N
    public static final String PROP_FORTRAN_ENABLED = "fortranEnabled"; // NOI18N
    public static final String PROP_MAKE_NAME = "makeName"; // NOI18N
    public static final String PROP_MAKE_PATH = "makePath"; // NOI18N
    public static final String PROP_GDB_NAME = "gdbName"; // NOI18N
    public static final String PROP_GDB_PATH = "gdbPath"; // NOI18N
    public static final String PROP_COMPILER_SET_NAME = "compilerSetName"; // NOI18N
    public static final String PROP_COMPILER_SET_DIRECTORIES = "compilerSetDirectories"; // NOI18N
    public static final String PROP_C_COMPILER_NAME = "cCompilerName"; // NOI18N
    public static final String PROP_CPP_COMPILER_NAME = "cppCompilerName"; // NOI18N
    public static final String PROP_FORTRAN_COMPILER_NAME = "fortranCompilerName"; // NOI18N
    public static final String PROP_GDB_REQUIRED = "gdbRequired"; // NOI18N
    public static final String PROP_C_REQUIRED = "cRequired"; // NOI18N
    public static final String PROP_CPP_REQUIRED = "cppRequired"; // NOI18N
    public static final String PROP_FORTRAN_REQUIRED = "fortranRequired"; // NOI18N
    
    /** The resource bundle for the form editor */
    public static ResourceBundle bundle;
    
    private String path = null;


    /** Initialize each property */
    protected void initialize() {
	super.initialize();
    }

    /** Return the signleton cppSettings */
    public static CppSettings getDefault() {
	return (CppSettings) findObject(CppSettings.class, true);
    }
    
    /**
     * Return the local version of $PATH. This masquerades as a property but isn't!
     * The reason it isn't is that we don't want persistance beyond the existing IDE
     * session.
     *
     * @returns Current value of the path (as a String)
     */
    public String getPath() {
        if (path == null) {
            path = Path.getPathAsString();
        }
        return path;
    }
    
    public void setPath(String p) {
        if (!p.equals(path) && p.length() > 0) {
            path = p;
        }
    }
    
    public String getCompilerSetName() {
        String name = (String) getProperty(PROP_COMPILER_SET_NAME);
        if (name == null) {
            return "";
        } else {
            return name;
        }
    }
    
    public void setCompilerSetName(String name) {
        String n = getCompilerSetName();
        if (n == null || !n.equals(name)) {
            putProperty(PROP_COMPILER_SET_NAME, name, true);
        }
    }
    
    public String getCompilerSetDirectories() {
        return (String) getProperty(PROP_COMPILER_SET_DIRECTORIES);
    }
    
    public void setCompilerSetDirectories(String name) {
        String n = getCompilerSetDirectories();
        if (n == null || !n.equals(name)) {
            putProperty(PROP_COMPILER_SET_DIRECTORIES, name, true);
        }
    }
    
    public String getMakeName() {
        String name = (String) getProperty(PROP_MAKE_NAME);
        if (name == null) {
            return "make"; // NOI18N
        } else {
            return name;
        }
    }
    
    public void setMakeName(String name) {
        String n = getMakeName();
        if (!n.equals(name)) {
            putProperty(PROP_MAKE_NAME, name, true);
        }
    }
    
    /**
     * Get the current make path. If this isnt' set but make name is, do a path search and
     * set the make path too.
     *
     * @returns Path to the make program
     */
    public String getMakePath() {
        String path = (String) getProperty(PROP_MAKE_PATH);
        if (path == null) {
            String name = (String) getProperty(PROP_MAKE_NAME);
            if (name != null) {
                StringTokenizer tok = new StringTokenizer(getPath(), File.pathSeparator);
                while (tok.hasMoreTokens()) {
                    String d = tok.nextToken();
                    File file = new File(d, name);
                    if (file.exists()) {
                        path = file.getAbsolutePath();
                        putProperty(PROP_MAKE_PATH, path, true);
                        return path;
                    }
                }
            }
            if (Utilities.isWindows()) {
                return "C:\\Cygwin\\bin\\make.exe"; // NOI18N
            } else if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS) {
                return "/usr/ccs/bin/make"; // NOI18N
            } else { // pick /usr/bin/make as a default value
                return "/usr/bin/make"; // NOI18N
            }
        } else {
            return path;
        }
    }
    
    public void setMakePath(String path) {
        String p = getMakePath();
        if (!p.equals(path)) {
            putProperty(PROP_MAKE_PATH, path, true);
        }
    }
    
    public String getGdbName() {
        String name = (String) getProperty(PROP_GDB_NAME);
        if (name == null) {
            return "gdb"; // NOI18N
        } else {
            return name;
        }
    }
    
    public void setGdbName(String name) {
        String n = getGdbName();
        if (!n.equals(name)) {
            putProperty(PROP_GDB_NAME, name, true);
        }
    }
    
    public String getGdbPath() {
        String path = (String) getProperty(PROP_GDB_PATH);
        if (path == null) {
            if (Utilities.isWindows()) {
                return "C:\\Cygwin\\bin\\gdb.exe"; // NOI18N
            } else {
                return "/usr/bin/gdb"; // NOI18N
            }
        } else {
            return path;
        }
    }
    
    public void setGdbPath(String path) {
        String p = getGdbPath();
        if (!p.equals(path)) {
            putProperty(PROP_GDB_PATH, path, true);
        }
    }
    
    public String getCCompilerName() {
        String name = (String) getProperty(PROP_C_COMPILER_NAME);
        if (name == null) {
            return getCompilerSetName().equals("Sun") ? "cc" : "gcc"; // NOI18N
        } else {
            return name;
        }
    }
    
    public void setCCompilerName(String name) {
        String n = getCCompilerName();
        if (!n.equals(name)) {
            putProperty(PROP_C_COMPILER_NAME, name, true);
        }
    }
    
    public String getCppCompilerName() {
        String name = (String) getProperty(PROP_CPP_COMPILER_NAME);
        if (name == null) {
            return getCompilerSetName().equals("Sun") ? "CC" : "g++"; // NOI18N
        } else {
            return name;
        }
    }
    
    public void setCppCompilerName(String name) {
        String n = getCppCompilerName();
        if (!n.equals(name)) {
            putProperty(PROP_CPP_COMPILER_NAME, name, true);
        }
    }
    
    public String getFortranCompilerName() {
        String name = (String) getProperty(PROP_FORTRAN_COMPILER_NAME);
        if (name == null) {
            return getCompilerSetName().equals("Sun") ? "f90" : "g77"; // NOI18N
        } else {
            return name;
        }
    }
    
    public void setFortranCompilerName(String name) {
        String n = getFortranCompilerName();
        if (!n.equals(name)) {
            putProperty(PROP_FORTRAN_COMPILER_NAME, name, true);
        }
    }

    /**
     * Gets the delay time for the start of the parsing.
     * @return The time in milis
     */
    public int getParsingDelay() {
        Integer delay = (Integer)getProperty(PROP_PARSING_DELAY);
        if (delay == null)
            return DEFAULT_PARSING_DELAY;
        return delay.intValue();
    }

    /**
     * Sets the delay time for the start of the parsing.
     * @param delay The time in milis
     */
    public void setParsingDelay(int delay) {
        if (delay != 0 && delay < 1000) {
            IllegalArgumentException e = new IllegalArgumentException();
	    ErrorManager.getDefault().annotate(e, getString("INVALID_AUTO_PARSING_DELAY"));
	    //ErrorManager.getDefault().notify(e);
	    throw e;
	}
        putProperty(PROP_PARSING_DELAY, new Integer(delay));
    }

    /**
     * Sets the replaceable strings table - used during instantiating
     * from template.
     */
    public void setReplaceableStringsTable(String table) {
        String t = getReplaceableStringsTable();
        if (t.equals(table))
            return;
        putProperty(PROP_REPLACEABLE_STRINGS_TABLE, table, true);
    }

    /**
     * Gets the replacable strings table - used during instantiating
     * from template.
     */
    public String getReplaceableStringsTable() {
        String table = (String)getProperty(PROP_REPLACEABLE_STRINGS_TABLE);
        if (table == null) {
            return "USER=" + System.getProperty("user.name"); // NOI18N
        } else {
            return table;
        }
    }


    /**
     * Gets the replaceable table as the Properties class.
     * @return the properties
     */
    public Properties getReplaceableStringsProps() {
        Properties props = new Properties();
        
        try {
            props.load(new ByteArrayInputStream(getReplaceableStringsTable().getBytes()));
        }
        catch (IOException e) {
        }
        return props;
    }
    
    /**
     * Find out if Fortran is enabled
     *
     * @return true if its enabled
     */
    public boolean isFortranEnabled() {
        Boolean b = (Boolean) getProperty(PROP_FORTRAN_ENABLED);
        return b == null ? DEFAULT_FORTRAN_ENABLED : b.booleanValue();
    }
    
    /**
     * Set value of PROP_FORTRAN_ENABLED property.
     *
     * @param enabled Value to set property to
     */
    public void setFortranEnabled(boolean enabled) {
        putProperty(PROP_FORTRAN_ENABLED, Boolean.valueOf(enabled), true);
    }
    

     public boolean isFreeFormatFortran(){
         Boolean b = (Boolean)getProperty(PROP_FREE_FORMAT_FORTRAN);
         if( b == null ){
             try{
                 // Need to go through SystemClassLoader :(
                 Class fSettingsDefaults = Class.forName(
                     "org.netbeans.modules.cnd.editor.fortran.FSettingsDefaults", // NOI18N
                     true, (ClassLoader) Lookup.getDefault().lookup(ClassLoader.class)
                 );
                 java.lang.reflect.Field defaultFreeFormat =
                     fSettingsDefaults.getField("defaultFreeFormat"); // NOI18N
                 b = (defaultFreeFormat.getBoolean(null))?Boolean.TRUE:Boolean.FALSE;
             }catch(Exception e){
                 // let's cheat, we know the default is TRUE (from FSettingsDefault)
                 b = Boolean.TRUE;
             }
             putProperty(PROP_FREE_FORMAT_FORTRAN, b);
         }
         return b.booleanValue();
     }

     public void setFreeFormatFortran(boolean state){
         putProperty(PROP_FREE_FORMAT_FORTRAN, state ? Boolean.TRUE : Boolean.FALSE);
     }
    
    public boolean isGdbRequired() {
        Boolean b = (Boolean) getProperty(PROP_GDB_REQUIRED);
        return b == null ? false : b.booleanValue();
    }
    
    public void setGdbRequired(boolean enabled) {
        putProperty(PROP_GDB_REQUIRED, Boolean.valueOf(enabled));
    }
    
    public boolean isCRequired() {
        Boolean b = (Boolean) getProperty(PROP_C_REQUIRED);
        return b == null ? true : b.booleanValue();
    }
    
    public void setCRequired(boolean enabled) {
        putProperty(PROP_C_REQUIRED, Boolean.valueOf(enabled));
    }
    
    public boolean isCppRequired() {
        Boolean b = (Boolean) getProperty(PROP_CPP_REQUIRED);
        return b == null ? true : b.booleanValue();
    }
    
    public void setCppRequired(boolean enabled) {
        putProperty(PROP_CPP_REQUIRED, Boolean.valueOf(enabled));
    }
    
    public boolean isFortranRequired() {
        Boolean b = (Boolean) getProperty(PROP_FORTRAN_REQUIRED);
        return b == null ? false : b.booleanValue();
    }
    
    public void setFortranRequired(boolean enabled) {
        putProperty(PROP_FORTRAN_REQUIRED, Boolean.valueOf(enabled));
    }

    /**
     * Get the display name.
     *
     *  @return value of OPTION_CPP_SETTINGS_NAME
     */
    public String displayName () {
	return getString("OPTION_CPP_SETTINGS_NAME");		        //NOI18N
    }
    
    public HelpCtx getHelpCtx () {
	return new HelpCtx ("Welcome_opt_editing_sources");	        //NOI18N
    }

    public ResourceBundle getBundle() {
        return bundle;
    }
    
    /** @return localized string */
    static String getString(String s) {
	if (bundle == null) {
	    bundle = NbBundle.getBundle(CppSettings.class);
	}
	return bundle.getString(s);
    }
}
