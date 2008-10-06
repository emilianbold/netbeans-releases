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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.settings;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.SharedClassObject;
import org.openide.util.Utilities;

/**
 * Settings for the C/C++/Fortran. The compile/build options stored
 * in this class are <B>default</B> options which will be applied to new files.
 * Once a file has been created its options may diverge from the defaults. A
 * files options do not change if these default options are changed.
 */

public class CppSettings extends SharedClassObject {

    /** serial uid */
    static final long serialVersionUID = -2942467713237077336L;

    public static final int DEFAULT_PARSING_DELAY = 2000;
    private static final boolean DEFAULT_FORTRAN_ENABLED = true;   // disable Fortran by default

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
    public static final String PROP_ARRAY_REPEAT_THRESHOLD = "arrayRepeatThreshold"; // NOI18N
    
    /** The resource bundle for the form editor */
    public static ResourceBundle bundle;
    
//    private String path = null;

    private static CppSettings cppSettings = null;

    /** Initialize each property */
    @Override
    protected void initialize() {
	super.initialize();
//        if (Boolean.getBoolean("netbeans.cnd.enable_fortran")) { // NOI18N
//            setFortranEnabled(true); // DEBUG
//        }
    }
    
    /** Return the signleton cppSettings */
    public static CppSettings getDefault() {
        // See IZ 120502
        if (cppSettings == null) {
            cppSettings = (CppSettings) findObject(CppSettings.class, true);
    }
        return cppSettings;
    }
    
//    /**
//     * Return the local version of $PATH. This masquerades as a property but isn't!
//     * The reason it isn't is that we don't want persistance beyond the existing IDE
//     * session.
//     *
//     * @returns Current value of the path (as a String)
//     */
//    public String getPath() {
//        if (path == null) {
//            path = Path.getPathAsString();
//        }
//        return path;
//    }
//    
//    public void setPath(String p) {
//        if (!p.equals(path) && p.length() > 0) {
//            path = p;
//        }
//    }
    
    public String getCompilerSetName() {
        //String name = (String) getProperty(PROP_COMPILER_SET_NAME);
        String name = getPreferences().get(PROP_COMPILER_SET_NAME, null);
        if (name == null) {
            return "";
        } else {
            return name;
        }
    }
    
    public void setCompilerSetName(String name) {
        String n = getCompilerSetName();
        if (n == null || !n.equals(name)) {
            //putProperty(PROP_COMPILER_SET_NAME, name, true);
            getPreferences().put(PROP_COMPILER_SET_NAME, name);
            firePropertyChange(PROP_COMPILER_SET_NAME, n, name);
        }
    }
    
//    public String getCompilerSetDirectories() {
//        //return (String) getProperty(PROP_COMPILER_SET_DIRECTORIES);
//        return getPreferences().get(PROP_COMPILER_SET_DIRECTORIES, null);
//    }
//    
//    public void setCompilerSetDirectories(String name) {
//        String n = getCompilerSetDirectories();
//        if (n == null || !n.equals(name)) {
//            //putProperty(PROP_COMPILER_SET_DIRECTORIES, name, true);
//            getPreferences().put(PROP_COMPILER_SET_DIRECTORIES, name);
//            firePropertyChange(PROP_COMPILER_SET_DIRECTORIES, n, name);
//        }
//    }
//    
//    public String getMakeName() {
//        //String name = (String) getProperty(PROP_MAKE_NAME);
//        String name = getPreferences().get(PROP_MAKE_NAME, null);
//        if (name == null) {
//            return "make"; // NOI18N
//        } else {
//            return name;
//        }
//    }
//    
//    public void setMakeName(String name) {
//        String n = getMakeName();
//        if (!n.equals(name)) {
//            //putProperty(PROP_MAKE_NAME, name, true);
//            getPreferences().put(PROP_MAKE_NAME, name);
//            firePropertyChange(PROP_MAKE_NAME, n, name);
//        }
//    }
//    
//    /**
//     * Get the current make path. If this isnt' set but make name is, do a path search and
//     * set the make path too.
//     *
//     * @returns Path to the make program
//     */
//    public String getMakePath() {
//        String p = getPreferences().get(PROP_MAKE_PATH, null);
//        if (p == null) {
//            //String name = (String) getProperty(PROP_MAKE_NAME);
//            String name = getPreferences().get(PROP_MAKE_NAME, null);
//            if (name != null) {
//                StringTokenizer tok = new StringTokenizer(Path.getPathAsString(), File.pathSeparator);
//                while (tok.hasMoreTokens()) {
//                    String d = tok.nextToken();
//                    File file = new File(d, name);
//                    if (file.exists()) {
//                        p = file.getAbsolutePath();
//                        //putProperty(PROP_MAKE_PATH, p, true); 
//                        getPreferences().put(PROP_MAKE_PATH, p);
//                        firePropertyChange(PROP_MAKE_PATH, null, p);
//                        return p;
//                    }
//                }
//            }
//            if (Utilities.isWindows()) {
//                return "C:\\Cygwin\\bin\\make.exe"; // NOI18N
//            } else if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS) {
//                return "/usr/ccs/bin/make"; // NOI18N
//            } else { // pick /usr/bin/make as a default value
//                return "/usr/bin/make"; // NOI18N
//            }
//        } else {
//            return p;
//        }
//    }
//    
//    public void setMakePath(String path) {
//        String p = getMakePath();
//        if (!p.equals(path)) {
//            //putProperty(PROP_MAKE_PATH, path, true);
//            getPreferences().put(PROP_MAKE_PATH, path);
//            firePropertyChange(PROP_MAKE_PATH, p, path);
//        }
//    }
//    
//    /*
//     * Returns full path if no spaces otherwise return just base name.
//     * See IZ 116463 for details
//     */
//    public String getMakeCommand() {
//        String makeCommand = getMakePath();
//        if (makeCommand.indexOf(' ') >= 0) {
//            // Strip path
//            makeCommand = IpeUtils.getBaseName(makeCommand);
//        }
//        return makeCommand;
//    }
    
    public String getGdbName() {
        //String name = (String) getProperty(PROP_GDB_NAME);
        String name = getPreferences().get(PROP_GDB_NAME, null);
        if (name == null) {
            return "gdb"; // NOI18N
        } else {
            return name;
        }
    }
    
    public void setGdbName(String name) {
        String n = getGdbName();
        if (!n.equals(name)) {
            //putProperty(PROP_GDB_NAME, name, true);
            getPreferences().put(PROP_GDB_NAME, name);
            firePropertyChange(PROP_GDB_NAME, n, name);
        }
    }
    
    public String getGdbPath() {
        //String path = (String) getProperty(PROP_GDB_PATH);
        String p = getPreferences().get(PROP_GDB_PATH, null);
        if (p == null) {
            if (Utilities.isWindows()) {
                return "C:\\Cygwin\\bin\\gdb.exe"; // NOI18N
            } else {
                return "/usr/bin/gdb"; // NOI18N
            }
        } else {
            return p;
        }
    }
    
    public void setGdbPath(String path) {
        String p = getGdbPath();
        if (!p.equals(path)) {
            //putProperty(PROP_GDB_PATH, path, true);
            getPreferences().put(PROP_GDB_PATH, path);
            firePropertyChange(PROP_GDB_PATH, p, path);
        }
    }
    
//    public String getCCompilerName() {
//        //String name = (String) getProperty(PROP_C_COMPILER_NAME);
//        String name = getPreferences().get(PROP_C_COMPILER_NAME, null);
//        if (name == null) {
//            return getCompilerSetName().startsWith("Sun") ? "cc" : "gcc"; // NOI18N
//        } else {
//            return name;
//        }
//    }
//    
//    public void setCCompilerName(String name) {
//        String n = getCCompilerName();
//        if (!n.equals(name)) {
//            //putProperty(PROP_C_COMPILER_NAME, name, true);
//            getPreferences().put(PROP_C_COMPILER_NAME, name);
//            firePropertyChange(PROP_C_COMPILER_NAME, n, name);
//        }
//    }
//    
//    public String getCppCompilerName() {
//        //String name = (String) getProperty(PROP_CPP_COMPILER_NAME);
//        String name = getPreferences().get(PROP_CPP_COMPILER_NAME, null);
//        if (name == null) {
//            return getCompilerSetName().startsWith("Sun") ? "CC" : "g++"; // NOI18N
//        } else {
//            return name;
//        }
//    }
//    
//    public void setCppCompilerName(String name) {
//        String n = getCppCompilerName();
//        if (!n.equals(name)) {
//            //putProperty(PROP_CPP_COMPILER_NAME, name, true);
//            getPreferences().put(PROP_CPP_COMPILER_NAME, name);
//            firePropertyChange(PROP_CPP_COMPILER_NAME, n, name);
//        }
//    }
//    
//    public String getFortranCompilerName() {
//        //String name = (String) getProperty(PROP_FORTRAN_COMPILER_NAME);
//        String name = getPreferences().get(PROP_FORTRAN_COMPILER_NAME, null);
//        if (name == null) {
//            return getCompilerSetName().startsWith("Sun") ? "f90" : "g77"; // NOI18N
//        } else {
//            return name;
//        }
//    }
//    
//    public void setFortranCompilerName(String name) {
//        String n = getFortranCompilerName();
//        if (!n.equals(name)) {
//            //putProperty(PROP_FORTRAN_COMPILER_NAME, name, true);
//            getPreferences().put(PROP_FORTRAN_COMPILER_NAME, name);
//            firePropertyChange(PROP_FORTRAN_COMPILER_NAME, n, name);
//        }
//    }

    /**
     * Gets the delay time for the start of the parsing.
     * @return The time in milis
     */
    public int getParsingDelay() {
        //Integer delay = (Integer)getProperty(PROP_PARSING_DELAY);
        int delay = getPreferences().getInt(PROP_PARSING_DELAY, DEFAULT_PARSING_DELAY);
        return delay;
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
        //putProperty(PROP_PARSING_DELAY, new Integer(delay));
        int oldValue = getParsingDelay();
        getPreferences().putInt(PROP_PARSING_DELAY, delay);
        firePropertyChange(PROP_PARSING_DELAY, new Integer(oldValue), new Integer(delay));
    }

    /**
     * Sets the replaceable strings table - used during instantiating
     * from template.
     */
    public void setReplaceableStringsTable(String table) {
        String t = getReplaceableStringsTable();
        if (t.equals(table))
            return;
        //putProperty(PROP_REPLACEABLE_STRINGS_TABLE, table, true);
        getPreferences().put(PROP_REPLACEABLE_STRINGS_TABLE, table);
        firePropertyChange(PROP_REPLACEABLE_STRINGS_TABLE, t, table);
    }

    /**
     * Gets the replacable strings table - used during instantiating
     * from template.
     */
    public String getReplaceableStringsTable() {
        //String table = (String)getProperty(PROP_REPLACEABLE_STRINGS_TABLE);
        String table = getPreferences().get(PROP_REPLACEABLE_STRINGS_TABLE, null);
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
        //Boolean b = (Boolean) getProperty(PROP_FORTRAN_ENABLED);
        boolean b = getPreferences().getBoolean(PROP_FORTRAN_ENABLED, DEFAULT_FORTRAN_ENABLED);
        return b;
    }
    
    /**
     * Set value of PROP_FORTRAN_ENABLED property.
     *
     * @param enabled Value to set property to
     */
    public void setFortranEnabled(boolean enabled) {
        //putProperty(PROP_FORTRAN_ENABLED, Boolean.valueOf(enabled), true);
        boolean oldValue = isFortranEnabled();
        getPreferences().putBoolean(PROP_FORTRAN_ENABLED, enabled);
        firePropertyChange(PROP_FORTRAN_ENABLED, new Boolean(oldValue), new Boolean(enabled));
    }
    

    /*
     * use FortranCodeStyle.get(doc).isFreeFormatFortran();
     */
    @Deprecated
    public boolean isFreeFormatFortran(){
        //Boolean b = (Boolean)getProperty(PROP_FREE_FORMAT_FORTRAN);
        boolean b = getPreferences().getBoolean(PROP_FREE_FORMAT_FORTRAN, true);
//         if( b == null ){
//             try{
//                 // Need to go through SystemClassLoader :(
//                 Class fSettingsDefaults = Class.forName(
//                     "org.netbeans.modules.cnd.editor.fortran.FSettingsDefaults", // NOI18N
//                     true, (ClassLoader) Lookup.getDefault().lookup(ClassLoader.class)
//                 );
//                 java.lang.reflect.Field defaultFreeFormat =
//                     fSettingsDefaults.getField("defaultFreeFormat"); // NOI18N
//                 b = (defaultFreeFormat.getBoolean(null))?Boolean.TRUE:Boolean.FALSE;
//             }catch(Exception e){
//                 // let's cheat, we know the default is TRUE (from FSettingsDefault)
//                 b = Boolean.TRUE;
//             }
//             putProperty(PROP_FREE_FORMAT_FORTRAN, b);
//         }
//         return b.booleanValue();'
        return b;
     }

     public void setFreeFormatFortran(boolean state){
        //putProperty(PROP_FREE_FORMAT_FORTRAN, state ? Boolean.TRUE : Boolean.FALSE);
        boolean oldValue = isFreeFormatFortran();
        getPreferences().putBoolean(PROP_FREE_FORMAT_FORTRAN, state);
        firePropertyChange(PROP_FREE_FORMAT_FORTRAN, new Boolean(oldValue), new Boolean(state));
     }
    
    public boolean isGdbRequired() {
        //Boolean b = (Boolean) getProperty(PROP_GDB_REQUIRED);
        //return b == null ? false : b.booleanValue();
        return getPreferences().getBoolean(PROP_GDB_REQUIRED, false);
    }
    
    public void setGdbRequired(boolean enabled) {
        //putProperty(PROP_GDB_REQUIRED, Boolean.valueOf(enabled));
        boolean oldValue = isGdbRequired();
        getPreferences().putBoolean(PROP_GDB_REQUIRED, enabled);
        firePropertyChange(PROP_GDB_REQUIRED, new Boolean(oldValue), new Boolean(enabled));
    }
    
//    public boolean isCRequired() {
//        //Boolean b = (Boolean) getProperty(PROP_C_REQUIRED);
//        //return b == null ? true : b.booleanValue();
//        return getPreferences().getBoolean(PROP_C_REQUIRED, true);
//    }
//    
//    public void setCRequired(boolean enabled) {
//        //putProperty(PROP_C_REQUIRED, Boolean.valueOf(enabled));
//        boolean oldValue = isCRequired();
//        getPreferences().putBoolean(PROP_C_REQUIRED, enabled);
//        firePropertyChange(PROP_C_REQUIRED, new Boolean(oldValue), new Boolean(enabled));
//    }
//    
//    public boolean isCppRequired() {
//        //Boolean b = (Boolean) getProperty(PROP_CPP_REQUIRED);
//        //return b == null ? true : b.booleanValue();
//        return getPreferences().getBoolean(PROP_CPP_REQUIRED, true);
//    }
//    
//    public void setCppRequired(boolean enabled) {
//        //putProperty(PROP_CPP_REQUIRED, Boolean.valueOf(enabled));
//        boolean oldValue = isCppRequired();
//        getPreferences().putBoolean(PROP_CPP_REQUIRED, enabled);
//        firePropertyChange(PROP_CPP_REQUIRED, new Boolean(oldValue), new Boolean(enabled));
//    }
//    
//    public boolean isFortranRequired() {
//        //Boolean b = (Boolean) getProperty(PROP_FORTRAN_REQUIRED);
//        //return b == null ? false : b.booleanValue();
//        return getPreferences().getBoolean(PROP_FORTRAN_REQUIRED, false);
//    }
//    
//    public void setFortranRequired(boolean enabled) {
//        //putProperty(PROP_FORTRAN_REQUIRED, Boolean.valueOf(enabled));
//        boolean oldValue = isFortranRequired();
//        getPreferences().putBoolean(PROP_FORTRAN_REQUIRED, enabled);
//        firePropertyChange(PROP_FORTRAN_REQUIRED, new Boolean(oldValue), new Boolean(enabled));
//    }
    
    public int getArrayRepeatThreshold() {
        return getPreferences().getInt(PROP_ARRAY_REPEAT_THRESHOLD, 10);
    }
    
    public void setArrayRepeatThreshold(int arrayRepeatThreshold) {
        int art = getArrayRepeatThreshold();
        if (art != arrayRepeatThreshold) {
            getPreferences().putInt(PROP_ARRAY_REPEAT_THRESHOLD, arrayRepeatThreshold);
            firePropertyChange(PROP_ARRAY_REPEAT_THRESHOLD, art, arrayRepeatThreshold);
        }
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

    private Preferences getPreferences() {
        return NbPreferences.forModule(CppSettings.class);
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
