/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.picklist.DefaultPicklistModel;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.api.xml.XMLDecoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoder;
import org.netbeans.modules.cnd.makeproject.api.configurations.ComboStringConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationAuxObject;
import org.netbeans.modules.cnd.makeproject.api.configurations.IntConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakefileConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.StringConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ui.ComboStringNodeProp;
import org.netbeans.modules.cnd.makeproject.api.configurations.ui.IntNodeProp;
import org.netbeans.modules.cnd.makeproject.configurations.ui.StringNodeProp;
import org.netbeans.modules.cnd.makeproject.runprofiles.RunProfileXMLCodec;
import org.netbeans.modules.cnd.makeproject.runprofiles.ui.EnvPanel;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.Path;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
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
    public static final String PROP_RUNCOMMAND_CHANGED = "runcommand-ch"; // NOI18N
    public static final String DEFAULT_RUN_COMMAND = "\"${OUTPUT_PATH}\""; // NOI18N
    private PropertyChangeSupport pcs = null;
    private boolean needSave = false;
    // Where this profile is keept
    //private Profiles parent;
    // Clone
    private RunProfile cloneOf;
    // Default Profile. One and only one profile is the default.
    private boolean defaultProfile;
    // Run Directory. Relative or absolute.
    private String baseDir; // can be null; in this case configuration is asked for it
    private String runDir;  // relative (to baseDir) or absolute
    // Should start a build before executing/debugging.
    private boolean buildFirst;
    // Environment
    private Env environment;
    // Run Command
    private ComboStringConfiguration runCommand;
    private StringConfiguration arguments; // hidden property (used by dbxtool)

    private String dorun;
    public static final int CONSOLE_TYPE_DEFAULT = 0;
    public static final int CONSOLE_TYPE_EXTERNAL = 1;
    public static final int CONSOLE_TYPE_OUTPUT_WINDOW = 2;
    public static final int CONSOLE_TYPE_INTERNAL = 3;
    private static final String[] consoleTypeNames = {
        //getString("ConsoleType_Default"), // NOI18N // Carefull: names no longer match CONSOLE_TYPE. Cleanup when debugger works again.
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
    private final MakeConfiguration makeConfiguration;
    private static final Logger LOGGER = Logger.getLogger("org.netbeans.modules.cnd.makeproject"); // NOI18N

    private RunProfile(String baseDir, int platform, PropertyChangeSupport pcs, int initialConsoleType, MakeConfiguration makeConfiguration) {
        this.platform = platform;
        this.baseDir = baseDir;
        this.pcs = pcs;
        this.makeConfiguration = makeConfiguration;
        initializeImpl(initialConsoleType);
    }
    
    /**
     * creation of help run profiles to be executed in output window
     */
    public RunProfile(String baseDir, int platform, MakeConfiguration makeConfiguration) {
        this(baseDir, platform, null, CONSOLE_TYPE_OUTPUT_WINDOW, makeConfiguration);
        CndUtils.assertNotNull(baseDir, "null baseDir"); //NOI18N
    }

    public RunProfile(MakeConfiguration makeConfiguration, PropertyChangeSupport pcs) {
        //TODO: PlatformTypes.getDefaultPlatform() it's not always right
        this(null, PlatformTypes.getDefaultPlatform(), pcs, getDefaultConsoleType(), makeConfiguration);
        CndUtils.assertNotNull(makeConfiguration, "null makeConfiguration"); //NOI18N
    }

    @Override
    public final void initialize() {
       initializeImpl(getDefaultConsoleType());
    }

    private void initializeImpl(int initialConsoleType) {
        //parent = null;
        environment = getDefaultEnv();
        defaultProfile = false;
        runDir = ""; // NOI18N

        runCommand = getDefaultRunCommand();
        arguments =  getDefaultArguments();
        buildFirst = true;
        if (makeConfiguration != null && makeConfiguration.isMakefileConfiguration()) {
            // #225018 - when create project from existing source Build First should be "OFF"
            // RunProfile is kept in private => for unmanaged projects shared in VCS it is missed.
            // By default do not rebuild makefile based projects
            buildFirst = false;
        }
        dorun = getDorunScript();
        termPaths = new HashMap<String, String>();
        termOptions = new HashMap<String, String>();
        consoleType = getConsoleTypeConfiguration(initialConsoleType);
        terminalType = getDefaultTerminalType();
        removeInstrumentation = getDefaultRemoveInstrumentation();
        clearChanged();
    }
    
    private ComboStringConfiguration getDefaultRunCommand() {
        DefaultPicklistModel list = new DefaultPicklistModel(10);
        list.addElement(DEFAULT_RUN_COMMAND);
        return new ComboStringConfiguration(null, DEFAULT_RUN_COMMAND, list); // NOI18N
        
    }
    
    private StringConfiguration getDefaultArguments() {
        return new StringConfiguration(null, "");
    }
    
    private Env getDefaultEnv() {
        return new Env();
    }
    
    private IntConfiguration getDefaultConsoleTypeConfiguration() {
        return new IntConfiguration(null, getDefaultConsoleType(), consoleTypeNames, null);        
    }
    
    private IntConfiguration getConsoleTypeConfiguration(int initialConsoleType) {
        return new IntConfiguration(null, initialConsoleType, consoleTypeNames, null);        
    }    
    
    private IntConfiguration getDefaultTerminalType() {
        return new IntConfiguration(null, 0, setTerminalTypeNames(), null);        
    }
    
    private IntConfiguration getDefaultRemoveInstrumentation() {
        return new IntConfiguration(null, REMOVE_INSTRUMENTATION_ASK, removeInstrumentationNames, null);
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
        File file = InstalledFileLocator.getDefault().locate("bin/dorun.sh", "org.netbeans.modules.cnd", false); // NOI18N
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
                termOptions.put(name, "--workdir " + escapeDir(getBaseDir()) + " -e \"" + dorun + // NOI18N
                        "\" -p \"" + getString("LBL_RunPrompt") + "\" -f \"{0}\" {1} {2}"); // NOI18N
                if (termPaths.get(def) == null) {
                    termPaths.put(def, termPath);
                    termOptions.put(def, "--workdir " + escapeDir(getBaseDir()) + " -e \"" + dorun + // NOI18N
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

    private int getArgIndex() {
        return getRunCommand().getValue().indexOf(" "); // NOI18N // FIXUP <=== need a better check
    }

    private String getRunBinary() {
        int argIndex = getArgIndex();
        if (argIndex > 0) {
            return getRunCommand().getValue().substring(0, argIndex);
        }
        else {
            return getRunCommand().getValue();
        }
    }

    private String getRunArgs() {
        int argIndex = getArgIndex();
        if (argIndex > 0) {
            return getRunCommand().getValue().substring(argIndex+1);
        }
        else {
            return "";
        }
    }

    // Args ...
    public void setArgs(String argsFlat) {
        String runBinary = getRunBinary();
        String oldArgsFlat = getRunArgs();
        getRunCommand().setValue(runBinary + " " + argsFlat); // NOI18N
        arguments.setValue(argsFlat);
        if (pcs != null && !CndPathUtilities.sameString(oldArgsFlat, argsFlat)) {
            pcs.firePropertyChange(PROP_RUNARGS_CHANGED, oldArgsFlat, argsFlat);
        }
        needSave = true;
    }

    public void setArgs(String[] argsArray) {
        String argsFlat = ""; // NOI18N
        for (int i = 0; i < argsArray.length; i++) {
            argsFlat += CndPathUtilities.quoteIfNecessary(argsArray[i]);
            if (i < (argsArray.length - 1)) {
                argsFlat += " "; // NOI18N
            }
        }
        setArgs(argsFlat);
    }

    public void setArgsRaw(String argsFlat) {
        String runBinary = getRunBinary();
        getRunCommand().setValue(runBinary + " " + argsFlat); // NOI18N
        needSave = true;
    }

    public String getArgsFlat() {
        if ("on".equals(System.getProperty("spro.dbxtool"))) { // NOI18N
            return getArguments();
        }
        else {
            return getRunArgs();
        }
    }

    public String[] getArgsArray() {
        return Utilities.parseParameters(getArgsFlat());
    }

    /*
     * Gets base directory. Base directory is always set and is always absolute.
     * Base directory is what run directory is relative to, if it is relative.
     */
    public String getBaseDir() {
        if (baseDir != null) {
            return baseDir;
        } else {
            CndUtils.assertNotNullInConsole(makeConfiguration, "makeConfiguration"); //NOI18N
            if (makeConfiguration != null) {
                return makeConfiguration.getBaseDir();
            } else {
                return null;
            }
        }
    }
        
    /*
     * Sets base directory. Base directory should  always be set and is always absolute.
     * Base directory is what run directory is relative to if it is relative.
     */
    public void setBaseDir(String baseDir) {
        assert baseDir != null && CndPathUtilities.isPathAbsolute(baseDir);
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
        String runDir2 = getRunDir();
        
        // #198813 - Can not run or debug a project if the project folder is not in the source root
        // If this is a makefile based project, and project metadata resides in a separate dir and the field is empty -
        // just return build  directory.
        // TODO: common solution for
        // #198843 - Wrong run directory when creating a project from existing sources
        if (runDir2.length() == 0 && makeConfiguration != null) {
            FileObject baseFO = makeConfiguration.getBaseFSPath().getFileObject();
            if (baseFO != null) {
                FileObject[] children = baseFO.getChildren();
                if (children != null && children.length == 1 && children[0].getNameExt().equals("nbproject")) { // NOI18N
                    MakefileConfiguration mc = makeConfiguration.getMakefileConfiguration();
                    if (mc != null) {
                        return mc.getAbsBuildCommandWorkingDir();
                    }
                }
            }
        }
        
        if (runDir2.length() == 0) {
            runDir2 = "."; // NOI18N
        }
        runDir2 = runDir2.trim();
        if (makeConfiguration != null && (runDir2.startsWith("~/") || runDir2.startsWith("~\\") || runDir2.equals("~"))) { // NOI18N
            try {
                if (makeConfiguration.getDevelopmentHost().getExecutionEnvironment().isLocal()) {
                    runDir2 = HostInfoUtils.getHostInfo(makeConfiguration.getDevelopmentHost().getExecutionEnvironment()).getUserDirFile().getAbsolutePath() + runDir2.substring(1);
                } else {
                    runDir2 = HostInfoUtils.getHostInfo(makeConfiguration.getDevelopmentHost().getExecutionEnvironment()).getUserDir() + runDir2.substring(1);
                }
            } catch (IOException ex) {
                Logger.getLogger(RunProfile.class.getName()).log(Level.INFO, "", ex);  // NOI18N
            } catch (CancellationException ex) {
                Logger.getLogger(RunProfile.class.getName()).log(Level.INFO, "", ex);  // NOI18N
            }
        }
        if (CndPathUtilities.isPathAbsolute(runDir2)) {
            runDirectory = runDir2;
        } else {
            runDirectory = getBaseDir() + "/" + runDir2; // NOI18N
        }
        
        try {
            String canonicalDir = FileSystemProvider.getCanonicalPath(makeConfiguration.getFileSystemHost(), runDirectory);
            CndUtils.assertNotNullInConsole(canonicalDir, "Can not canonicalize " + runDirectory); //NOI18N
            if (canonicalDir == null) {
                return runDirectory;
            } else {
                return canonicalDir;
            }
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "Exception when getting canonical run directory:", ex); //NOI18N
            return runDirectory;
        }
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
        setRunDir(CndPathUtilities.toAbsoluteOrRelativePath(getBaseDir(), newRunDir));
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

    // Run Command

public boolean isSimpleRunCommand() {
        String rc = runCommand.getValue().trim();
        if (rc.startsWith("\"${")) { // default..... // NOI18N
            return true;
        } else if (makeConfiguration != null && makeConfiguration.isLibraryConfiguration()) {
            return true;
        } else  {
            return false;
        }
    } 

    public ComboStringConfiguration getRunCommand() {
        return runCommand;
    }

    public void setRunCommand(ComboStringConfiguration runCommand) {
        String oldArgsFlat = getArgsFlat();
        this.runCommand = runCommand;
        String argsFlat = getArgsFlat();
        if (pcs != null && !CndPathUtilities.sameString(oldArgsFlat, argsFlat)) {
            pcs.firePropertyChange(PROP_RUNARGS_CHANGED, oldArgsFlat, argsFlat);
        }
    }

    public IntConfiguration getConsoleType() {
        return consoleType;
    }

    public void setConsoleType(IntConfiguration consoleType) {
        this.consoleType = consoleType;
    }

    public static int getDefaultConsoleType() {
        return CONSOLE_TYPE_INTERNAL;
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
        Env oldEnv = getEnvironment();
        String oldArgs = getArgsFlat();

        RunProfile p = (RunProfile) profileAuxObject;
        setDefault(p.isDefault());
        //setArgs(p.getArgsFlat());
        setBaseDir(p.getBaseDir());
        setRunDir(p.getRunDir());
        getRunCommand().assign(p.getRunCommand());
        //runCommandPicklist = p.getRunCommand().getPicklist();
        setConfigurationArguments(p.getConfigurationArguments().clone());
        if (pcs != null && !CndPathUtilities.sameString(oldArgs, getArgsFlat())) {
            pcs.firePropertyChange(PROP_RUNARGS_CHANGED, oldArgs, getArgsFlat());
        }
        //setRawRunDirectory(p.getRawRunDirectory());
        setBuildFirst(p.getBuildFirst());
        getEnvironment().assign(p.getEnvironment());
        if (pcs != null && !oldEnv.toString().equals(environment.toString())) {
            pcs.firePropertyChange(PROP_ENVVARS_CHANGED, oldEnv, environment);
        }
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
        RunProfile p = new RunProfile(getBaseDir(), this.platform, (MakeConfiguration) conf);
        //p.setParent(getParent());
        p.setCloneOf(this);
        p.setDefault(isDefault());
        p.setRunDir(getRunDir());
        //fix for #229873 - NullPointerException at org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile.clone
        //do not user get.. when clone, use private variable        
        p.setRunCommand(runCommand == null? getDefaultRunCommand() : runCommand.clone());
        p.setConfigurationArguments(arguments == null ? getDefaultArguments() : arguments.clone());
        p.setBuildFirst(getBuildFirst());
        p.setEnvironment(environment == null ? getDefaultEnv() : environment.clone());
        p.setConsoleType(consoleType == null ? getDefaultConsoleTypeConfiguration() : consoleType.clone());        
        p.setTerminalType(terminalType == null ? getDefaultTerminalType() : terminalType.clone());
        p.setRemoveInstrumentation(removeInstrumentation == null ? getDefaultRemoveInstrumentation() : removeInstrumentation.clone());
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
        StringNodeProp argumentsNodeprop;

        Sheet.Set set = new Sheet.Set();
        set.setName("General"); // NOI18N
        set.setDisplayName(getString("GeneralName"));
        set.setShortDescription(getString("GeneralTT"));

        String runComboHintSuffix = null;

        ExecutionEnvironment targetEnv = makeConfiguration.getDevelopmentHost().getExecutionEnvironment();
        if (!isWindows() && HostInfoUtils.isHostInfoAvailable(targetEnv)) {
            try {
                String shell = HostInfoUtils.getHostInfo(targetEnv).getShell();
                if (shell != null) {
                    shell = CndPathUtilities.getBaseName(shell);
                    runComboHintSuffix = NbBundle.getMessage(RunProfile.class, "ShellSyntaxSupported", shell); // NOI18N
                }
            } catch (IOException ex) {
            } catch (CancellationException ex) {
            }
        }

        String runComboName = getString("RunCommandName"); // NOI18N
        String runComboHint = getString("RunCommandHint"); // NOI18N

        if (runComboHintSuffix != null) {
            runComboHint = runComboHint.concat("<br>").concat(runComboHintSuffix); // NOI18N
        }

        set.put(new ComboStringNodeProp(getRunCommand(), true, runComboName, runComboHint));
        set.put(new RunDirectoryNodeProp());
        set.put(argumentsNodeprop = new StringNodeProp(getConfigurationArguments(), "", "Arguments", getString("ArgumentsName"), getString("ArgumentsHint"))); // NOI18N
        argumentsNodeprop.setHidden(true);
        set.put(new EnvNodeProp());
        set.put(new BuildFirstNodeProp());
        ConsoleIntNodeProp consoleTypeNP = new ConsoleIntNodeProp(getConsoleType(), true, "ConsoleType", //NOI18N
                getString("ConsoleType_LBL"), getString("ConsoleType_HINT")); // NOI18N
        set.put(consoleTypeNP);
        final IntNodeProp terminalTypeNP = new IntNodeProp(getTerminalType(), true, "TerminalType", //NOI18N
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
            updateTerminalTypeState(terminalTypeNP, consoleTypeNames[((Integer) consoleTypeNP.getValue())-1]);
        }

        // TODO: this is a quick and durty "hack".
        // don't show "remove instrumentation" property in the panel
        // until we have cnd.tha module

        if (thaSupportEnabled()) {
            set.put(new IntNodeProp(getRemoveInstrumentation(), true, "RemoveInstrumentation", // NOI18N
                    getString("RemoveInstrumentation_LBL"), getString("RemoveInstrumentation_HINT"))); // NOI18N
        }

        sheet.put(set);

        return sheet;
    }

    private static void updateTerminalTypeState(IntNodeProp terminalTypeNP, String value) {
        terminalTypeNP.setCanWrite(consoleTypeNames[CONSOLE_TYPE_EXTERNAL-1].equals(value));
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

    /**
     * @return the arguments
     * This property is hidden by default. Don't use it!
     */
    public StringConfiguration getConfigurationArguments() {
        return arguments;
    }

    /**
     * @param arguments the arguments to set
     * This property is hidden by default. Don't use it!
     */
    public void setConfigurationArguments(StringConfiguration arguments) {
        this.arguments = arguments;
    }


    /**
     * @deprecated This property is hidden by default. Don't use it!
     */
    @Deprecated
    public String getArguments() {
        return arguments.getValue();
    }

    /**
     * @deprecated This property is hidden by default. Don't use it!
     */
    @Deprecated
    public void setArguments(String val) {
        String oldArgs = arguments.getValue();
        arguments.setValue(val);
        if (pcs != null && !CndPathUtilities.sameString(oldArgs, val)) {
            pcs.firePropertyChange(PROP_RUNARGS_CHANGED, oldArgs, val);
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
            String path = CndPathUtilities.toAbsoluteOrRelativePath(getBaseDir(), v);
            path = CndPathUtilities.normalizeSlashes(path);
            setRunDir(path);
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            String seed;
            String runDir2 = getRunDir();
            if (runDir2.length() == 0) {
                runDir2 = "."; // NOI18N
            }
            if (CndPathUtilities.isPathAbsolute(runDir2)) {
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
            FileSystem fs = (makeConfiguration == null) ? CndFileUtils.getLocalFileSystem() : makeConfiguration.getSourceFileSystem();
            return new DirectoryChooserPanel(seed, this, propenv, fs);
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
