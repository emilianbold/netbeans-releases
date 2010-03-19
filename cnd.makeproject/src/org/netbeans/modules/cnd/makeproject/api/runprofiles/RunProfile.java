/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.cnd.makeproject.api.runprofiles;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationAuxObject;
import org.netbeans.modules.cnd.makeproject.runprofiles.RunProfileXMLCodec;
import org.netbeans.modules.cnd.makeproject.runprofiles.ui.EnvPanel;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.nativeexecution.api.util.Path;
import org.netbeans.modules.cnd.api.xml.XMLDecoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoder;
import org.netbeans.modules.cnd.makeproject.api.configurations.IntConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.configurations.ui.ListenableIntNodeProp;
import org.netbeans.modules.cnd.makeproject.api.configurations.ui.IntNodeProp;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public final class RunProfile implements ConfigurationAuxObject {

    private static final boolean NO_EXEPTION = Boolean.getBoolean("org.netbeans.modules.cnd.makeproject.api.runprofiles");
    public static final String PROFILE_ID = "runprofile"; // NOI18N
    /** Property name: runargs (args, cd, etc.) have changed */
    public static final String PROP_RUNARGS_CHANGED = "runargs-ch"; // NOI18N
    public static final String PROP_RUNDIR_CHANGED = "rundir-ch"; // NOI18N
    public static final String PROP_ENVVARS_CHANGED = "envvars-ch"; // NOI18N
    private PropertyChangeSupport pcs = null;
    private boolean needSave = false;
    // Where this profile is keept
    //private Profiles parent;
    // Clone
    private RunProfile cloneOf;
    // Default Profile. One and only one profile is the default.
    private boolean defaultProfile;
    // Arguments. Quoted flat representation.
    private String argsFlat;
    private boolean argsFlatValid = false;
    // Argumants. Array form.
    private String[] argsArray;
    private boolean argsArrayValid = false;
    // Run Directory. Relative or absolute.
    private String baseDir; // Alwasy set, always absolute
    private String runDir;  // relative (to baseDir) or absolute
    // Should start a build before executing/debugging.
    private boolean buildFirst;
    // Environment
    private Env environment;
    private String dorun;
    public static final int CONSOLE_TYPE_DEFAULT = 0;
    public static final int CONSOLE_TYPE_EXTERNAL = 1;
    public static final int CONSOLE_TYPE_OUTPUT_WINDOW = 2;
    public static final int CONSOLE_TYPE_INTERNAL = 3;
    private static final String[] consoleTypeNames = {
        getString("ConsoleType_Default"), // NOI18N
        getString("ConsoleType_External"), // NOI18N
        getString("ConsoleType_Output"), // NOI18N
        getString("ConsoleType_Internal"), // NOI18N
    };
    private IntConfiguration consoleType;
    private IntConfiguration terminalType;
    private HashMap<String, String> termPaths;
    private HashMap<String, String> termOptions;
    private final int platform;
    public static final int REMOVE_INSTRUMENTATION_ASK = 0;
    public static final int REMOVE_INSTRUMENTATION_YES = 1;
    public static final int REMOVE_INSTRUMENTATION_NO = 2;
    private static final String[] removeInstrumentationNames = {
        getString("RemoveInstrumentation_Ask"), // NOI18N
        getString("RemoveInstrumentation_Yes"), // NOI18N
        getString("RemoveInstrumentation_No"), // NOI18N
    };
    private IntConfiguration removeInstrumentation;

    public RunProfile(String baseDir, int platform) {
        this.platform = platform;
        this.baseDir = baseDir;
        this.pcs = null;
        initialize();
    }

    public RunProfile(String baseDir, PropertyChangeSupport pcs) {
        platform = PlatformTypes.getDefaultPlatform(); //TODO: it's not always right
        this.baseDir = baseDir;
        this.pcs = pcs;
        initialize();
    }

    @Override
    public final void initialize() {
        //parent = null;
        environment = new Env();
        defaultProfile = false;
        argsFlat = ""; // NOI18N
        argsFlatValid = true;
        argsArrayValid = false;
        runDir = ""; // NOI18N
        buildFirst = true;
        dorun = getDorunScript();
        termPaths = new HashMap<String, String>();
        termOptions = new HashMap<String, String>();
        consoleType = new IntConfiguration(null, CONSOLE_TYPE_DEFAULT, consoleTypeNames, null);
        terminalType = new IntConfiguration(null, 0, setTerminalTypeNames(), null);
        removeInstrumentation = new IntConfiguration(null, REMOVE_INSTRUMENTATION_ASK, removeInstrumentationNames, null);
        clearChanged();
    }

    private String escapeDir(String dir) {
        if (dir != null) {
            dir = dir.trim();
            String quote = "\""; //NOI18N
            if (!dir.startsWith(quote)) { //NOI18N
                dir = quote + dir + quote; //NOI18N
            }
        }
        return dir;
    }

    private String getDorunScript() {
        File file = InstalledFileLocator.getDefault().locate("bin/dorun.sh", null, false); // NOI18N
        if (file != null && file.exists()) {
            return file.getAbsolutePath();
        } else {
            if (!NO_EXEPTION) {
                throw new IllegalStateException(getString("Err_MissingDorunScript")); // NOI18N
            }
            return null;
        }
    }

    private boolean isWindows() {
        return platform == PlatformTypes.PLATFORM_WINDOWS;
    }

    private String[] setTerminalTypeNames() {
        List<String> list = new ArrayList<String>();
        String def = getString("TerminalType_Default"); // NOI18N
        String name;
        String termPath;

        list.add(def);
        if (isWindows()) {
            String term = getString("TerminalType_CommandWindow"); // NOI18N
            list.add(term);
            termPaths.put(term, "cmd.exe"); // NOI18N
            termPaths.put(def, "cmd.exe"); // NOI18N
            termOptions.put(term, "/c start sh \"" + dorun + "\" -p \"" + getString("LBL_RunPrompt") + " \" -f \"{0}\" {1} {2}"); // NOI18N
            termOptions.put(def, "/c start sh \"" + dorun + "\" -p \"" + getString("LBL_RunPrompt") + " \" -f \"{0}\" {1} {2}"); // NOI18N
        } else {
            // Start with the user's $PATH. Append various other directories and look
            // for gnome-terminal, konsole, and xterm.
            String path = Path.getPathAsString() +
                    ":/usr/X11/bin:/usr/X/bin:/usr/X11R6/bin:/opt/gnome/bin" + // NOI18N
                    ":/usr/gnome/bin:/opt/kde/bin:/opt/kde4/bin:/opt/kde3/bin:/usr/kde/bin:/usr/openwin/bin"; // NOI18N

            termPath = searchPath(path, "gnome-terminal", "/usr/bin"); // NOI18N
            if (termPath != null) {
                name = getString("TerminalType_GNOME"); // NOI18N
                list.add(name);
                termPaths.put(name, termPath);
                termPaths.put(def, termPath);
                String opts = "--disable-factory --hide-menubar " + "--title=\"{1} {3}\" " + // NOI18N
                        "-x \"" + dorun + "\" -p \"" + getString("LBL_RunPrompt") + "\" " + // NOI18N
                        "-f \"{0}\" {1} {2}"; // NOI18N
                termOptions.put(name, opts);
                termOptions.put(def, opts);
            }
            termPath = searchPath(path, "konsole"); // NOI18N
            if (termPath != null) {
                name = getString("TerminalType_KDE"); // NOI18N
                list.add(name);
                termPaths.put(name, termPath);
                termOptions.put(name, "--workdir " + escapeDir(baseDir) + " -e \"" + dorun + // NOI18N
                        "\" -p \"" + getString("LBL_RunPrompt") + "\" -f \"{0}\" {1} {2}"); // NOI18N
                if (termPaths.get(def) == null) {
                    termPaths.put(def, termPath);
                    termOptions.put(def, "--workdir " + escapeDir(baseDir) + " -e \"" + dorun + // NOI18N
                            "\" -p \"" + getString("LBL_RunPrompt") + "\" -f \"{0}\" {1} {2}"); // NOI18N
                }
            }
            termPath = searchPath(path, "xterm", Utilities.getOperatingSystem() == Utilities.OS_SOLARIS ? // NOI18N
                    "/usr/openwin/bin" : "/usr/bin"); // NOI18N
            if (termPath != null) {
                name = getString("TerminalType_XTerm"); // NOI18N
                list.add(name);
                termPaths.put(name, termPath);
                termOptions.put(name, "-e \"" + dorun + "\" -p \"" + getString("LBL_RunPrompt") + "\" -f \"{0}\" {1} {2}"); // NOI18N
                if (termPaths.get(def) == null) {
                    termPaths.put(def, termPath);
                    termOptions.put(def, "-e \"" + dorun + "\" -p \"" + getString("LBL_RunPrompt") + "\" -f \"{0}\" {1} {2}"); // NOI18N
                }
            }
            if (termPaths.get(def) == null) {
                list.add(getString("TerminalType_None")); // NOI18N
            }
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * Search an augmented $PATH (the user's $PATH plus various standard locations
     * for a specific terminal emulater.
     *
     * @param path The path to search for program "term"
     * @param term The terminal program we're searching for
     * @returns Either a path to the specified term or null
     */
    private String searchPath(String path, String term) {
        return searchPath(path, term, null);
    }

    /**
     * Search an augmented $PATH (the user's $PATH plus various standard locations
     * for a specific terminal emulater.
     *
     * @param path The path to search for program "term"
     * @param term The terminal program we're searching for
     * @defaultPath A possible default path to check before searching the entire path
     * @returns Either a path to the specified term or null
     */
    private String searchPath(final String path, final String term, String defaultPath) {

        if (defaultPath != null) {
            File file = new File(defaultPath, term);
            if (file.exists()) {
                return file.getAbsolutePath();
            }
        }
//        System.err.println("RP.searchPath: Doing PATH search for " + term);
        final String[] patharray = new String[1];
        patharray[0] = null;

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                StringTokenizer st = new StringTokenizer(path, ":"); // NOI18N

                while (st.hasMoreTokens()) {
                    String dir = st.nextToken();
                    File file = new File(dir, term);
                    if (file.exists()) {
                        patharray[0] = file.getAbsolutePath();
                        break;
                    }
                }
            }
        });
        thread.start();
        try {
            thread.join(5000);
        } catch (InterruptedException ex) {
        }
        return patharray[0];
    }

    public String getTerminalPath() {
        return termPaths.get(getTerminalType().getName());
    }

    public String getTerminalOptions() {
        return termOptions.get(getTerminalType().getName());
    }

    @Override
    public boolean shared() {
        return false;
    }

    /**
     * Returns an unique id (String) used to retrive this object from the
     * pool of aux objects
     * OLD:
     * and for storing the object in xml form and
     * parsing the xml code to restore the object.
     */
    @Override
    public String getId() {
        return PROFILE_ID;
    }

    // Set if this profile is a clone of another profile (not set for copy)
    public void setCloneOf(RunProfile profile) {
        this.cloneOf = profile;
    }

    public RunProfile getCloneOf() {
        return cloneOf;
    }

    // Default Profile ...
    public boolean isDefault() {
        return defaultProfile;
    }

    public void setDefault(boolean b) {
        defaultProfile = b;
    }

    // Args ...
    public void setArgs(String argsFlat) {
        String oldArgsFlat = getArgsFlat();
        this.argsFlat = argsFlat;
        argsFlatValid = true;
        argsArrayValid = false;
        if (pcs != null && !CndPathUtilitities.sameString(oldArgsFlat, argsFlat)) {
            pcs.firePropertyChange(PROP_RUNARGS_CHANGED, oldArgsFlat, argsFlat);
        }
        needSave = true;
    }

    public void setArgs(String[] argsArray) {
        String[] oldArgsArray = getArgsArray();
        this.argsArray = argsArray;
        argsFlatValid = false;
        argsArrayValid = true;
        if (pcs != null && !CndPathUtilitities.sameStringArray(oldArgsArray, argsArray)) {
            pcs.firePropertyChange(PROP_RUNARGS_CHANGED, oldArgsArray, argsArray);
        }
        needSave = true;
    }

    public void setArgsRaw(String argsFlat) {
        this.argsFlat = argsFlat;
        argsFlatValid = true;
        argsArrayValid = false;
        needSave = true;
    }

    public String getArgsFlat() {
        if (!argsFlatValid) {
            argsFlat = ""; // NOI18N
            for (int i = 0; i < argsArray.length; i++) {
                argsFlat += CndPathUtilitities.quoteIfNecessary(argsArray[i]);
                if (i < (argsArray.length - 1)) {
                    argsFlat += " "; // NOI18N
                }
            }
            argsFlatValid = true;
        }
        return argsFlat;
    }

    public String[] getArgsArray() {
        if (!argsArrayValid) {
            argsArray = Utilities.parseParameters(argsFlat);
            argsArrayValid = true;
        }
        return argsArray;
    }

    /*
     * as array shifted one and executable as arg 0
     */
    public String[] getArgv(String ex) {
        String[] argsArrayShifted = new String[getArgsArray().length + 1];
        argsArrayShifted[0] = ex;
        System.arraycopy(getArgsArray(), 0, argsArrayShifted, 1, getArgsArray().length);
        return argsArrayShifted;
    }

    /*
     * Gets base directory. Base directory is always set and is always absolute.
     * Base directory is what run directory is relative to, if it is relative.
     */
    public String getBaseDir() {
        return baseDir;
    }

    /*
     * Sets base directory. Base directory should  always be set and is always absolute.
     * Base directory is what run directory is relative to if it is relative.
     */
    public void setBaseDir(String baseDir) {
        assert baseDir != null && CndPathUtilitities.isPathAbsolute(baseDir);
        this.baseDir = baseDir;
    }

    /*
     * Gets run directory.
     * Run Directory is either absolute or relative (to base directory).
     */
    public String getRunDir() {
        if (runDir == null) {
            runDir = ""; // NOI18N
        }
        return runDir;
    }

    /*
     * sets run directory.
     * Run Directory is either absolute or relative (to base directory).
     */
    public void setRunDir(String runDir) {
        if (runDir == null) {
            runDir = ""; // NOI18N
        }
        if (this.runDir != null && this.runDir.equals(runDir)) {
            return;
        }
        this.runDir = runDir;
        if (pcs != null) {
            pcs.firePropertyChange(PROP_RUNDIR_CHANGED, null, this);
        }
        needSave = true;
    }

    /*
     * Gets absolute run directory.
     */
    public String getRunDirectory() {
        String runDirectory;
        String runDirectoryCanonicalPath;
        String runDir2 = getRunDir();
        if (runDir2.length() == 0) {
            runDir2 = "."; // NOI18N
        }
        if (CndPathUtilitities.isPathAbsolute(runDir2)) {
            runDirectory = runDir2;
        } else {
            runDirectory = getBaseDir() + "/" + runDir2; // NOI18N
        }

        // convert to canonical path
        File runDirectoryFile = new File(runDirectory);
        if (!runDirectoryFile.exists() || !runDirectoryFile.isDirectory()) {
            return runDirectory; // ??? FIXUP
        }
        try {
            runDirectoryCanonicalPath = runDirectoryFile.getCanonicalPath();
        } catch (IOException ioe) {
            runDirectoryCanonicalPath = runDirectory;
        }
        return runDirectoryCanonicalPath;
    }

    /*
     * Sets run directory.
     * If new run directory is relative, just set it.
     * If new run directory is absolute, convert to relative if already relative,
     * othervise just set it.
     */
    public void setRunDirectory(String newRunDir) {
        if (newRunDir == null || newRunDir.length() == 0) {
            newRunDir = "."; // NOI18N
        }
        setRunDir(CndPathUtilitities.toAbsoluteOrRelativePath(getBaseDir(), newRunDir));
    }

    // Should Build ...
    public void setBuildFirst(boolean buildFirst) {
        this.buildFirst = buildFirst;
    }

    public boolean getBuildFirst() {
        return buildFirst;
    }

    // Environment
    public Env getEnvironment() {
        return environment;
    }

    public void setEnvironment(Env env) {
        Env oldEnv = environment;
        this.environment = env;
        if (pcs != null && !environment.equals(oldEnv)) {
            pcs.firePropertyChange(PROP_ENVVARS_CHANGED, oldEnv, environment);
        }
    }

    public IntConfiguration getConsoleType() {
        return consoleType;
    }

    public void setConsoleType(IntConfiguration consoleType) {
        this.consoleType = consoleType;
    }

    public static int getDefaultConsoleType() {
        return CONSOLE_TYPE_EXTERNAL;
    }

    public IntConfiguration getTerminalType() {
        if (terminalType.getName().equals(getString("TerminalType_None"))) { // NOI18N
            return null;
        } else {
            return terminalType;
        }
    }

    public void setTerminalType(IntConfiguration terminalType) {
        this.terminalType = terminalType;
    }

    public IntConfiguration getRemoveInstrumentation() {
        return removeInstrumentation;
    }

    public void setRemoveInstrumentation(IntConfiguration removeInstrumentation) {
        this.removeInstrumentation = removeInstrumentation;
    }

    // Misc...
    /**
     * Saves this profile *and* all other profiles of the same parent to disk
     */
    public void saveToDisk() {
        /*
        if (parent != null) {
        parent.saveToDisk();
        }
         */
    }

    /**
     *  Adds property change listener.
     *  @param l new listener.
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (pcs != null) {
            pcs.addPropertyChangeListener(l);
        }
    }

    /**
     *  Removes property change listener.
     *  @param l removed listener.
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (pcs != null) {
            pcs.removePropertyChangeListener(l);
        }
    }

    //
    // XML codec support
    // This stuff ends up in <projectdir>/nbproject/private/profiles.xml
    //
    @Override
    public XMLDecoder getXMLDecoder() {
        return new RunProfileXMLCodec(this);
    }

    @Override
    public XMLEncoder getXMLEncoder() {
        return new RunProfileXMLCodec(this);
    }

    /**
     * Responsible for saving the object in xml format.
     * It should save the object in the following format using the id
     * string from getId():
     * <id-string>
     *     <...
     *     <...
     * </id-string>
     */
    /* OLD
    public void writeElement(PrintWriter pw, int indent, Object object) {
    RunProfileHelper.writeProfileBlock(pw, indent, this);
    }
     */
    /**
     * Responsible for parsing the xml code created from above and
     * for restoring the state of the object (but not the object itself).
     * Refer to the Sax parser documentation for details.
     */
    /* OLD
    public void startElement(String namespaceURI, String localName, String element, Attributes atts) {
    RunProfileHelper.startElement(this, element, atts);
    }
     */
    /* OLD
    public void endElement(String uri, String localName, String qName, String currentText) {
    RunProfileHelper.endElement(this, qName, currentText);
    }
     */
    // interface ProfileAuxObject
    @Override
    public boolean hasChanged() {
        return needSave;
    }

    // interface ProfileAuxObject
    @Override
    public void clearChanged() {
        needSave = false;
    }

    @Override
    public void assign(ConfigurationAuxObject profileAuxObject) {
        if (!(profileAuxObject instanceof RunProfile)) {
            // FIXUP: exception ????
            System.err.print("Profile - assign: Profile object type expected - got " + profileAuxObject); // NOI18N
            return;
        }
        RunProfile p = (RunProfile) profileAuxObject;
        setDefault(p.isDefault());
        setArgs(p.getArgsFlat());
        setBaseDir(p.getBaseDir());
        setRunDir(p.getRunDir());
        //setRawRunDirectory(p.getRawRunDirectory());
        setBuildFirst(p.getBuildFirst());
        getEnvironment().assign(p.getEnvironment());
        getConsoleType().assign(p.getConsoleType());
        getTerminalType().assign(p.getTerminalType());
        getRemoveInstrumentation().assign(p.getRemoveInstrumentation());
    }

    /**
     * Clones the profile.
     * All fields are cloned except for 'parent'.
     */
    @Override
    public RunProfile clone(Configuration conf) {
        RunProfile p = new RunProfile(getBaseDir(), this.platform);
        //p.setParent(getParent());
        p.setCloneOf(this);
        p.setDefault(isDefault());
        p.setArgs(getArgsFlat());
        p.setRunDir(getRunDir());
        //p.setRawRunDirectory(getRawRunDirectory());
        p.setBuildFirst(getBuildFirst());
        p.setEnvironment(getEnvironment().clone());
        p.setConsoleType(getConsoleType().clone());
        p.setTerminalType(getTerminalType().clone());
        p.setRemoveInstrumentation(getRemoveInstrumentation().clone());
        return p;
    }

    public Sheet getSheet(boolean disableConsoleTypeSelection) {
        return createSheet(disableConsoleTypeSelection);
    }

    public Sheet getSheet() {
        return createSheet(false);
    }

    private Sheet createSheet(boolean disableConsoleTypeSelection) {
        Sheet sheet = new Sheet();

        Sheet.Set set = new Sheet.Set();
        set.setName("General"); // NOI18N
        set.setDisplayName(getString("GeneralName"));
        set.setShortDescription(getString("GeneralTT"));
        set.put(new ArgumentsNodeProp());
        set.put(new RunDirectoryNodeProp());
        set.put(new EnvNodeProp());
        set.put(new BuildFirstNodeProp());
        ListenableIntNodeProp consoleTypeNP = new ListenableIntNodeProp(getConsoleType(), true, null,
                getString("ConsoleType_LBL"), getString("ConsoleType_HINT")); // NOI18N
        set.put(consoleTypeNP);
        final IntNodeProp terminalTypeNP = new IntNodeProp(getTerminalType(), true, null,
                getString("TerminalType_LBL"), getString("TerminalType_HINT")); // NOI18N
        set.put(terminalTypeNP);
        if (disableConsoleTypeSelection) {
            terminalTypeNP.setCanWrite(false);
            consoleTypeNP.setCanWrite(false);
        } else {

            consoleTypeNP.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    String value = (String) evt.getNewValue();
                    updateTerminalTypeState(terminalTypeNP, value);
                }
            });
            // because IntNodeProb has "setValue(String)" and "Integer getValue()"...
            updateTerminalTypeState(terminalTypeNP, consoleTypeNames[(Integer) consoleTypeNP.getValue()]);
        }

        // TODO: this is a quick and durty "hack".
        // don't show "remove instrumentation" property in the panel
        // until we have cnd.tha module

        if (thaSupportEnabled()) {
            set.put(new IntNodeProp(getRemoveInstrumentation(), true, null,
                    getString("RemoveInstrumentation_LBL"), getString("RemoveInstrumentation_HINT"))); // NOI18N
        }

        sheet.put(set);

        return sheet;
    }

    private static void updateTerminalTypeState(IntNodeProp terminalTypeNP, String value) {
        terminalTypeNP.setCanWrite(consoleTypeNames[CONSOLE_TYPE_EXTERNAL].equals(value) ||
                consoleTypeNames[CONSOLE_TYPE_DEFAULT].equals(value) && getDefaultConsoleType() == CONSOLE_TYPE_EXTERNAL);
    }

    private static String getString(String s) {
        return NbBundle.getMessage(RunProfile.class, s);
    }
    private static Boolean hasTHAModule = null;

    private static synchronized boolean thaSupportEnabled() {
        if (hasTHAModule == null) {
            hasTHAModule = Boolean.FALSE;

            FileObject fsRoot = FileUtil.getConfigRoot();
            // Not to introduce an additional dependency on dlight modules
            // just use absolute path
            FileObject thaConfig = fsRoot.getFileObject("DLight/Configurations/THA"); // NOI18N
            hasTHAModule = thaConfig != null && thaConfig.isFolder();
        }

        return hasTHAModule.booleanValue();
    }

    private class ArgumentsNodeProp extends PropertySupport<String> {

        public ArgumentsNodeProp() {
            super("Arguments", String.class, getString("ArgumentsName"), getString("ArgumentsHint"), true, true); // NOI18N
        }

        @Override
        public String getValue() {
            return getArgsFlat();
        }

        @Override
        public void setValue(String v) {
            setArgs(v);
        }
    }

    private class RunDirectoryNodeProp extends PropertySupport<String> {

        public RunDirectoryNodeProp() {
            super("Run Directory", String.class, getString("RunDirectoryName"), getString("RunDirectoryHint"), true, true); // NOI18N
        }

        @Override
        public String getValue() {
            return getRunDir();
        }

        @Override
        public void setValue(String v) {
            String path = CndPathUtilitities.toAbsoluteOrRelativePath(getBaseDir(), v);
            path = CndPathUtilitities.normalize(path);
            setRunDir(path);
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            String seed;
            String runDir2 = getRunDir();
            if (runDir2.length() == 0) {
                runDir2 = "."; // NOI18N
            }
            if (CndPathUtilitities.isPathAbsolute(runDir2)) {
                seed = runDir2;
            } else {
                seed = getBaseDir() + File.separatorChar + runDir2;
            }
            return new DirEditor(seed);
        }
    }

    private class DirEditor extends PropertyEditorSupport implements ExPropertyEditor {

        private PropertyEnv propenv;
        private String seed;

        public DirEditor(String seed) {
            this.seed = seed;
        }

        @Override
        public void setAsText(String text) {
            setRunDir(text);
        }

        @Override
        public String getAsText() {
            return getRunDir();
        }

        @Override
        public Object getValue() {
            return getRunDir();
        }

        @Override
        public void setValue(Object v) {
            setRunDir((String) v);
        }

        @Override
        public boolean supportsCustomEditor() {
            return true;
        }

        @Override
        public java.awt.Component getCustomEditor() {
            return new DirectoryChooserPanel(seed, this, propenv);
        }

        @Override
        public void attachEnv(PropertyEnv propenv) {
            this.propenv = propenv;
        }
    }

    private class BuildFirstNodeProp extends PropertySupport<Boolean> {

        public BuildFirstNodeProp() {
            super("Build First", Boolean.class, getString("BuildFirstName"), getString("BuildFirstHint"), true, true); // NOI18N
        }

        @Override
        public Boolean getValue() {
            return Boolean.valueOf(getBuildFirst());
        }

        @Override
        public void setValue(Boolean v) {
            setBuildFirst((v).booleanValue());
        }
    }

    private class EnvNodeProp extends PropertySupport<Env> {

        public EnvNodeProp() {
            super("Environment", Env.class, getString("EnvironmentName"), getString("EnvironmentHint"), true, true); // NOI18N
        }

        @Override
        public Env getValue() {
            return getEnvironment();
        }

        @Override
        public void setValue(Env v) {
            getEnvironment().assign(v);
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            return new EnvEditor(getEnvironment().clone());
        }

        @Override
        public Object getValue(String attributeName) {
            if (attributeName.equals("canEditAsText")) { // NOI18N
                return Boolean.FALSE;
            }
            return super.getValue(attributeName);
        }
    }

    private static class EnvEditor extends PropertyEditorSupport implements ExPropertyEditor {

        private Env env;
        private PropertyEnv propenv;

        public EnvEditor(Env env) {
            this.env = env;
        }

        @Override
        public void setAsText(String text) {
        }

        @Override
        public String getAsText() {
            return env.toString();
        }

        @Override
        public java.awt.Component getCustomEditor() {
            return new EnvPanel(env, this, propenv);
        }

        @Override
        public boolean supportsCustomEditor() {
            return true;
        }

        @Override
        public void attachEnv(PropertyEnv propenv) {
            this.propenv = propenv;
        }
    }
}
