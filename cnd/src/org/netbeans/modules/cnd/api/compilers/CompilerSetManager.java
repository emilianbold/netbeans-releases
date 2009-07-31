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
package org.netbeans.modules.cnd.api.compilers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.cnd.api.compilers.CompilerSet.CompilerFlavor;
import org.netbeans.modules.cnd.api.compilers.ToolchainManager.AlternativePath;
import org.netbeans.modules.cnd.api.compilers.ToolchainManager.ToolchainDescriptor;
import org.netbeans.modules.cnd.api.compilers.ToolchainManager.CompilerDescriptor;
import org.netbeans.modules.cnd.api.compilers.ToolchainManager.ToolDescriptor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.api.utils.Path;
import org.netbeans.modules.cnd.compilers.impl.ToolchainManagerImpl;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.NamedRunnable;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.modules.ModuleInfo;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.TaskListener;
import org.openide.util.Utilities;

/**
 * Manage a set of CompilerSets. The CompilerSets are dynamically created based on which compilers
 * are found in the user's $PATH variable.
 */
public class CompilerSetManager {

    // Legacy defines for CND 5.5 compiler set definitions
    // used in DBX, so don't remove please!
    public static final int SUN_COMPILER_SET = 0;
    public static final int GNU_COMPILER_SET = 1;
    
    private static enum State {
        STATE_PENDING,
        STATE_COMPLETE,
        STATE_UNINITIALIZED
    }

    /** TODO: deprecate and remove */
    public static final String LOCALHOST = "localhost"; // NOI18N

    /* Persistance information */
    private static final double csm_version = 1.1;
    private static final String CSM = "csm."; // NOI18N
    private static final String VERSION = "version"; // NOI18N
    private static final String NO_SETS = ".noOfSets"; // NOI18N
    private static final String SET_NAME = ".setName."; // NOI18N
    private static final String SET_FLAVOR = ".setFlavor."; // NOI18N
    private static final String SET_DIRECTORY = ".setDirectory."; // NOI18N
    private static final String SET_AUTO = ".autoGenerated."; // NOI18N
    private static final String SET_DEFAULT = ".defaultSet"; // NOI18N
    private static final String SET_PLATFORM = ".setPlatform."; // NOI18N
    private static final String NO_TOOLS = ".noOfTools."; // NOI18N
    private static final String TOOL_NAME = ".toolName."; // NOI18N
    private static final String TOOL_DISPLAYNAME = ".toolDisplayName."; // NOI18N
    private static final String TOOL_KIND = ".toolKind."; // NOI18N
    private static final String TOOL_PATH = ".toolPath."; // NOI18N
    private static final String TOOL_FLAVOR = ".toolFlavor."; // NOI18N
    private static HashMap<ExecutionEnvironment, CompilerSetManager> managers = new HashMap<ExecutionEnvironment, CompilerSetManager>();
    private final static Object MASTER_LOCK = new Object();
    private static CompilerProvider compilerProvider = null;
    private static String cygwinBase;
//    private static String mingwBase;
    private static String msysBase;
    public static final String SunExpress = "SunStudioExpress"; // NOI18N
    public static final String Sun12 = "SunStudio_12"; // NOI18N
    public static final String Sun11 = "SunStudio_11"; // NOI18N
    public static final String Sun10 = "SunStudio_10"; // NOI18N
    public static final String Sun = "SunStudio"; // NOI18N
    public static final String GNU = "GNU"; // NOI18N
    private List<CompilerSet> sets = new ArrayList<CompilerSet>();
    private final ExecutionEnvironment executionEnvironment;
    private volatile State state;
    private int platform = -1;
    private Task remoteInitialization;
    private static final Logger log = Logger.getLogger("cnd.remote.logger"); // NOI18N

    /**
     * Find or create a default CompilerSetManager for the given key. A default
     * CSM is one which is active in the system. A non-default is one which gets
     * created but has no affect unless its made default.
     *
     * For instance, the Build Tools tab (on C/C++ Tools->Options) creates a non-Default
     * CSM and only makes it default if the OK button is pressed. If Cancel is pressed,
     * it never becomes default.
     *
     * @param env specifies execution environment
     * @return A default CompilerSetManager for the given key
     */
    public static CompilerSetManager getDefault(ExecutionEnvironment env) {
        return getDefaultImpl(env, true);
    }

   private static CompilerSetManager getDefaultImpl(ExecutionEnvironment env, boolean initialize) {
        CompilerSetManager csm = null;
        boolean no_compilers = false;

        synchronized (MASTER_LOCK) {
            csm = managers.get(env);
            if (csm == null) {
                csm = restoreFromDisk(env);
                if (csm != null && csm.getDefaultCompilerSet() == null) {
                    csm.setDefaltCompilerSet();
                    csm.saveToDisk();
                }
            }
            if (csm == null) {
                csm = new CompilerSetManager(env, initialize);
                if (csm.isValid()) {
                    csm.saveToDisk();
                } else if (!csm.isPending() && !csm.isUninitialized()) {
                    no_compilers = true;
                }
            }
            if (csm != null) {
                managers.put(env, csm);
            }
        }

        if (no_compilers) {
            // workaround to fix IZ#164028: Full IDE freeze when opening GizmoDemo project on Linux
            // we postpone dialog displayer until EDT is free to process
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    DialogDescriptor dialogDescriptor = new DialogDescriptor(
                            new NoCompilersPanel(),
                            getString("NO_COMPILERS_FOUND_TITLE"),
                            true,
                            new Object[]{DialogDescriptor.OK_OPTION},
                            DialogDescriptor.OK_OPTION,
                            DialogDescriptor.BOTTOM_ALIGN,
                            null,
                            null);
                    DialogDisplayer.getDefault().notify(dialogDescriptor);
                }
            });
        }
        return csm;
    }

    public static CompilerSetManager getDefault() {
        return getDefault(ExecutionEnvironmentFactory.getLocal());
    }

    /** Create a CompilerSetManager which may be registered at a later time via CompilerSetManager.setDefault() */
    public static CompilerSetManager create(ExecutionEnvironment env) {
        CompilerSetManager newCsm = new CompilerSetManager(env);
        if (newCsm.getCompilerSets().size() == 1 && newCsm.getCompilerSets().get(0).getName().equals(CompilerSet.None)) {
            newCsm.remove(newCsm.getCompilerSets().get(0));
        }
        return newCsm;
    }

    /** Replace the default CompilerSetManager. Let registered listeners know its been updated */
    public static void setDefaults(Collection<CompilerSetManager> csms) {
        synchronized (MASTER_LOCK) {
            // TODO: not remove, only replace now...
//            for (CompilerSetManager oldCsm : managers.values()) {
//                // erase old info
//                getPreferences().remove(CSM + ExecutionEnvironmentFactory.getHostKey(oldCam.executionEnvironment) + NO_SETS);
//            }
//            managers.clear();
            for (CompilerSetManager csm : csms) {
                if (csm.getCompilerSets().size() == 0) { // No compilers found
                    csm.add(CompilerSet.createEmptyCompilerSet(csm.getPlatform()));
                }
                csm.saveToDisk();
                managers.put(csm.executionEnvironment, csm);
            }
        }
    }

    /**
     * Get the Cygwin base directory from Cygwin.xml (toolchain definition, which users the Windows registry) or the user's path
     */
    public static String getCygwinBase() {
        if (cygwinBase == null) {
            ToolchainManagerImpl tcm = ToolchainManager.getImpl();
            ToolchainDescriptor td = tcm.getToolchain("Cygwin", PlatformTypes.PLATFORM_WINDOWS); // NOI18N
            if (td != null) {
                String cygwinBin = tcm.getBaseFolder(td, PlatformTypes.PLATFORM_WINDOWS);
                if (cygwinBin != null) {
                    cygwinBase = cygwinBin.substring(0, cygwinBin.length() - 4).replace("\\", "/"); // NOI18N
                }
            }
            if (cygwinBase == null) {
                for (String dir : Path.getPath()) {
                    dir = dir.toLowerCase().replace("\\", "/"); // NOI18N
                    if (dir.contains("cygwin")) { // NOI18N
                        if (dir.endsWith("/")) { // NOI18N
                            dir = dir.substring(0, dir.length() - 1);
                        }
                        if (dir.toLowerCase().endsWith("/usr/bin")) { // NOI18N
                            cygwinBase = dir.substring(0, dir.length() - 8);
                            break;
                        } else if (dir.toLowerCase().endsWith("/bin")) { // NOI18N
                            cygwinBase = dir.substring(0, dir.length() - 4);
                            break;
                        }
                    }
                }
            }
        }
        return cygwinBase;
    }

// Note: This method is written but commented out because there currently are no users. Its written because
//       its possible that there will be users someday and if/when that happens, simply uncomment this code.
//
//    /**
//     * Get the MinGW base directory from MinGW.xml (toolchain definition, which users the Windows registry) or the user's path
//     */
//    public static String getMinGWBase() {
//        if (mingwBase == null) {
//            ToolchainManager tcm = ToolchainManager.getInstance();
//            ToolchainDescriptor td = tcm.getToolchain("MinGW", PlatformTypes.PLATFORM_WINDOWS); // NOI18N
//            if (td != null) {
//                String mingwBin = tcm.getBaseFolder(td, PlatformTypes.PLATFORM_WINDOWS);
//                mingwBase = mingwBin.substring(0, msysBin.length() - 4).replace("\\", "/"); // NOI18N
//                String msysBin = tcm.getCommandFolder(td, PlatformTypes.PLATFORM_WINDOWS);
//                msysBase = msysBin.substring(0, msysBin.length() - 4).replace("\\", "/"); // NOI18N
//            }
//            if (mingwBase == null) {
//                for (String dir : Path.getPath()) {
//                    dir = dir.toLowerCase().replace("\\", "/"); // NOI18N
//                    if (dir.contains("mingw")) { // NOI18N
//                        if (dir.toLowerCase().endsWith("/usr/bin")) { // NOI18N
//                            mingwBase = dir.substring(0, dir.length() - 8);
//                            break;
//                        } else if (dir.toLowerCase().endsWith("/bin")) { // NOI18N
//                            mingwBase = dir.substring(0, dir.length() - 4);
//                            break;
//                        }
//                    }
//                }
//            }
//        }
//        return mingwBase;
//    }
    /**
     * Get the MSys base directory from MinGW.xml (toolchain definition, which users the Windows registry) or the user's path
     */
    public static String getMSysBase() {
        if (msysBase == null) {
            ToolchainManagerImpl tcm = ToolchainManager.getImpl();
            for(ToolchainDescriptor td : tcm.getToolchains(PlatformTypes.PLATFORM_WINDOWS)){
                if (td != null) {
                    String msysBin = tcm.getCommandFolder(td, PlatformTypes.PLATFORM_WINDOWS);
                    if (msysBin != null) {
                        msysBase = msysBin.substring(0, msysBin.length() - 4).replace("\\", "/"); // NOI18N
                        break;
                    }
                }
            }
            if (msysBase == null) {
                for (String dir : Path.getPath()) {
                    dir = dir.toLowerCase().replace("\\", "/"); // NOI18N
                    if (dir.contains("/msys/1.0") && dir.toLowerCase().contains("/bin")) { // NOI18N
                        msysBase = dir.substring(0, dir.length() - 4);
                        break;
                    }
                }
            }
        }
        return msysBase;
    }

    private CompilerSetManager(ExecutionEnvironment env) {
        this(env, true);
    }

    private CompilerSetManager(ExecutionEnvironment env, final boolean initialize) {
        //if (log.isLoggable(Level.FINEST)) {
        //    log.log(Level.FINEST, "CompilerSetManager CTOR A @" + System.identityHashCode(this) + ' ' + env + ' ' + initialize, new Exception()); //NOI18N
        //}
        executionEnvironment = env;
        if (initialize) {
            state = State.STATE_PENDING;
        } else {
            state = State.STATE_UNINITIALIZED;
            return;
        }
        if (executionEnvironment.isLocal()) {
            platform = computeLocalPlatform();
            initCompilerSets(Path.getPath());
            state = State.STATE_COMPLETE;
        } else {
            final AtomicReference<Thread> threadRef = new AtomicReference<Thread>();
            final String progressMessage = NbBundle.getMessage(getClass(), "PROGRESS_TEXT", env.getDisplayName());
            final ProgressHandle progressHandle = ProgressHandleFactory.createHandle(
                    progressMessage,
                    new Cancellable() {
                        public boolean cancel() {
                            Thread thread = threadRef.get();
                            if (thread != null) {
                                thread.interrupt();
                            }
                            return true;
                        }

            });
            log.fine("CSM.init: initializing remote compiler set @" + System.identityHashCode(this) + " for: " + toString());
            progressHandle.start();
            RequestProcessor.getDefault().post(new NamedRunnable(progressMessage) {
                protected @Override void runImpl() {
                    threadRef.set(Thread.currentThread());
                    try {
                        initRemoteCompilerSets(false, initialize);
                    } finally {
                        progressHandle.finish();
                    }
                }
            });
        }
    }

    private CompilerSetManager(ExecutionEnvironment env, List<CompilerSet> sets, int platform) {
        //if (log.isLoggable(Level.FINEST)) {
        //    log.log(Level.FINEST, "CompilerSetManager CTOR B @" + System.identityHashCode(this) + ' '  + sets + ' ' + platform, new Exception()); //NOI18N
        //}
        this.executionEnvironment = env;
        this.sets = sets;
        this.platform = platform;
        if(env.isRemote() && isEmpty()) {
            this.state = State.STATE_UNINITIALIZED;
            log.fine("CSM restoring from pref: Adding empty CS to host " + toString());
            add(CompilerSet.createEmptyCompilerSet(platform));
        } else {
            this.state = State.STATE_COMPLETE;
        }
    }

    public boolean isValid() {
        return sets.size() > 0 && !sets.get(0).getName().equals(CompilerSet.None);
    }

    public boolean isPending() {
        return state == State.STATE_PENDING;
    }

    public boolean isUninitialized() {
        return state == State.STATE_UNINITIALIZED;
    }

    public boolean isComplete() {
        return state == State.STATE_COMPLETE;
    }

    /** CAUTION: this is a slow method. It should NOT be called from the EDT thread */
    public synchronized void initialize(boolean save, boolean runCompilerSetDataLoader) {
        if (isUninitialized()) {
            log.fine("CSM.getDefault: Doing remote setup from EDT?" + SwingUtilities.isEventDispatchThread());
            this.sets.clear();
            initRemoteCompilerSets(true, runCompilerSetDataLoader);
            if (remoteInitialization != null) {
                remoteInitialization.waitFinished();
                remoteInitialization = null;
            }
        }
        if (save) {
            synchronized (MASTER_LOCK) {
                saveToDisk();
            }
        }
    }

    public int getPlatform() {
        if (platform < 0) {
            if (executionEnvironment.isLocal()) {
                platform = computeLocalPlatform();
            } else {
                //waitForCompletion();
                if (isPending()) {
                    log.warning("calling getPlatform() on uninitializad " + getClass().getSimpleName());
                }
            }
        }
        return platform == -1 ? PlatformTypes.PLATFORM_NONE : platform;
    }

    private String getPlatformName(int platform) {
        switch (platform) {
            case PlatformTypes.PLATFORM_LINUX:
                return "linux"; // NOI18N
            case PlatformTypes.PLATFORM_SOLARIS_SPARC:
                return "sun_sparc"; // NOI18N
            case PlatformTypes.PLATFORM_SOLARIS_INTEL:
                return "sun_intel"; // NOI18N
            case PlatformTypes.PLATFORM_WINDOWS:
                return "windows"; // NOI18N
            case PlatformTypes.PLATFORM_MACOSX:
                return "mac"; // NOI18N
            default:
                return "none"; // NOI18N
        }
    }

    public static int computeLocalPlatform() {
        String os = System.getProperty("os.name"); // NOI18N

        if (os.equals("SunOS")) { // NOI18N
            return System.getProperty("os.arch").equals("x86") ? PlatformTypes.PLATFORM_SOLARIS_INTEL : PlatformTypes.PLATFORM_SOLARIS_SPARC; // NOI18N
        } else if (os.startsWith("Windows ")) { // NOI18N
            return PlatformTypes.PLATFORM_WINDOWS;
        } else if (os.toLowerCase().contains("linux")) { // NOI18N
            return PlatformTypes.PLATFORM_LINUX;
        } else if (os.toLowerCase().contains("mac") || os.startsWith("Darwin")) { // NOI18N
            return PlatformTypes.PLATFORM_MACOSX;
        } else {
            return PlatformTypes.PLATFORM_GENERIC;
        }
    }

    public static CompilerSetManager getDeepCopy(ExecutionEnvironment execEnv, boolean initialize) {
        return getDefaultImpl(execEnv, initialize).deepCopy();
    }

    private CompilerSetManager deepCopy() {
        //waitForCompletion();
        if (isPending()) {
            log.warning("calling deepCopy() on uninitializad " + getClass().getSimpleName());
        }
        List<CompilerSet> setsCopy = new ArrayList<CompilerSet>();
        for (CompilerSet set : getCompilerSets()) {
            setsCopy.add(set.createCopy());
        }
        CompilerSetManager copy = new CompilerSetManager(executionEnvironment, setsCopy, this.platform);
        return copy;
    }

    public String getUniqueCompilerSetName(String baseName) {
        int n = 0;
        String suggestedName = baseName;
        while (true) {
            suggestedName = baseName + (n > 0 ? ("_" + n) : ""); // NOI18N
            if (getCompilerSet(suggestedName) != null) {
                n++;
            } else {
                break;
            }
        }
        return suggestedName;
    }

    /** Search $PATH for all desired compiler sets and initialize cbCompilerSet and spCompilerSets */
    private void initCompilerSets(ArrayList<String> dirlist) {
        Set<CompilerFlavor> flavors = new HashSet<CompilerFlavor>();
        initKnownCompilers(getPlatform(), flavors);
        dirlist = appendDefaultLocations(getPlatform(), dirlist);
        for (String path : dirlist) {
            if (path.equals("/usr/ucb")) { // NOI18N
                // Don't look here.
                continue;
            }
            if (!IpeUtils.isPathAbsolute(path)) {
                path = CndFileUtils.normalizeAbsolutePath(new File(path).getAbsolutePath());
            }
            File dir = new File(path);
            if (dir.isDirectory()) {
                for (CompilerFlavor flavor : CompilerSet.getCompilerSetFlavor(dir.getAbsolutePath(), getPlatform())) {
                    if (!flavors.contains(flavor)) {
                        flavors.add(flavor);
                        CompilerSet cs = CompilerSet.getCustomCompilerSet(dir.getAbsolutePath(), flavor, flavor.toString());
                        cs.setAutoGenerated(true);
                        if (initCompilerSet(path, cs, false)){
                            add(cs);
                        }
                    }
                }
            }
        }
        completeCompilerSets();
    }

    /**
     * Since many toolchains have default locations, append them to the path (on a per-platform basis)
     * if they aren't already in the list.
     *
     * @param platform The platform we're running on
     * @param dirlist An ArrayList of the current PATH
     * @return A possibly modified ArrayList
     */
    private ArrayList<String> appendDefaultLocations(int platform, ArrayList<String> dirlist) {
        for (ToolchainDescriptor d : ToolchainManager.getImpl().getToolchains(platform)) {
            Map<String, List<String>> map = d.getDefaultLocations();
            if (map != null) {
                String pname = getPlatformName(platform);
                List<String> list = map.get(pname);
                if (list != null ) {
                    for (String dir : list){
                        if (!dirlist.contains(dir)){
                            dirlist.add(dir);
                        }
                    }
                }
            }
        }
        return dirlist;
    }

    private void setDefaltCompilerSet() {
        // Look for compiler set with lowest position and set it as the default 
        List<ToolchainDescriptor> toolchainDescriptors = ToolchainManager.getImpl().getAllToolchains();
        int pos = -1;
        for (CompilerSet cs : sets) {
            ToolchainDescriptor toolchainDescriptor = cs.getCompilerFlavor().getToolchainDescriptor();
            int newPos = toolchainDescriptors.indexOf(toolchainDescriptor);
            if (newPos >= 0 && (pos == -1 || newPos < pos)) {
                pos = newPos;
                setDefault(cs);
            }
        }
        if (pos >= 0) {
            return;
        }
        
        if (!sets.isEmpty()) {
            setDefault(sets.get(0));
        } else {
            add(CompilerSet.createEmptyCompilerSet(getPlatform()));
        }
    }

    private void initKnownCompilers(int platform, Set<CompilerFlavor> flavors) {
        for (ToolchainDescriptor d : ToolchainManager.getImpl().getToolchains(platform)) {
            String base = ToolchainManager.getImpl().getBaseFolder(d, platform);
            if (base != null) {
                File folder = new File(base);
                if (folder.exists() && folder.isDirectory()) {
                    CompilerFlavor flavor = CompilerFlavor.toFlavor(d.getName(), platform);
                    if (flavor != null) { // #158084 NPE
                        flavors.add(flavor);
                        CompilerSet cs = CompilerSet.getCustomCompilerSet(folder.getAbsolutePath(), flavor, flavor.toString());
                        cs.setAutoGenerated(true);
                        if (initCompilerSet(base, cs, true)){
                            add(cs);
                        }
                    } else {
                        log.warning("NULL compiler flavor for " + d.getName() + " on platform " + platform);
                    }
                }
            }
        }
    }

    public List<CompilerSet> findRemoteCompilerSets(String path) {
        final CompilerSetProvider provider = CompilerSetProviderFactory.createNew(executionEnvironment);
        String[] arData = provider.getCompilerSetData(path);
        List<CompilerSet> css = new ArrayList<CompilerSet>();
        if (arData != null) {
            for (String data : arData) {
                if (data != null && data.length() > 0) {
                    CompilerSet cs = parseCompilerSetString(platform, data);
                    if (cs != null) {
                        css.add(cs);
                    }
                }
            }
        }
        return css;
    }

    private CompilerSet parseCompilerSetString(int platform, String data) {
        log.fine("CSM.initRemoteCompileSets: line = [" + data + "]");
        // to emulate #158088
        if (Boolean.getBoolean("cnd.remote.emulate.npe")) {
            CompilerSet cs = null;
            cs.addDirectory(data);
        }

        String flavor;
        String path;
        StringTokenizer st = new StringTokenizer(data, ";"); // NOI18N
        try {
            flavor = st.nextToken();
            path = st.nextToken();
        } catch (NoSuchElementException ex) {
            log.warning("Malformed compilerSetString: " + data);
            return null;
        }
        CompilerFlavor compilerFlavor = CompilerFlavor.toFlavor(flavor, platform);
        if (compilerFlavor == null) { // #158084
            log.warning("NULL compiler flavor for " + flavor + " on platform " + platform);
            return null;
        }
        CompilerSet cs = new CompilerSet(compilerFlavor, path, flavor);
        while (st.hasMoreTokens()) {
            String name = st.nextToken();
            int kind = -1;
            String p = path + '/' + name;
            if (flavor.startsWith("Sun")) { // NOI18N
                if (name.equals("cc")) { // NOI18N
                    kind = Tool.CCompiler;
                } else if (name.equals("CC")) { // NOI18N
                    kind = Tool.CCCompiler;
                } else if (name.equals("f95") || name.equals("f90")) { // NOI18N
                    kind = Tool.FortranCompiler;
                } else if (name.startsWith("as=")) { // NOI18N
                    kind = Tool.Assembler;
                    p = name.substring(name.indexOf('=') + 1);
                } else if (name.equals("dmake")) { // NOI18N
                    kind = Tool.MakeTool;
                } else if (name.startsWith("gdb=")) { // NOI18N
                    kind = Tool.DebuggerTool;
                    p = name.substring(name.indexOf('=') + 1);
                }
            } else {
                if (name.equals("gcc")) { // NOI18N
                    kind = Tool.CCompiler;
                } else if (name.equals("g++")) { // NOI18N
                    kind = Tool.CCCompiler;
                } else if (name.equals("g77") || name.equals("gfortran")) { // NOI18N
                    kind = Tool.FortranCompiler;
                } else if (name.equals("as") || name.equals("gas")) { // NOI18N
                    kind = Tool.Assembler;
                } else if (name.equals("make") || // NOI18N
                        ((platform == PlatformTypes.PLATFORM_SOLARIS_INTEL || platform == PlatformTypes.PLATFORM_SOLARIS_SPARC) &&
                        name.equals("gmake"))) { // NOI18N
                    kind = Tool.MakeTool;
                } else if (name.equals("gdb")) { // NOI18N
                    kind = Tool.DebuggerTool;
                } else if (name.startsWith("gdb=")) { // NOI18N
                    kind = Tool.DebuggerTool;
                    p = name.substring(name.indexOf('=') + 1);
                }
            }
            if (kind != -1) {
                cs.addTool(executionEnvironment, name, p, kind);
            }
        }
        return cs;
    }

    /** Initialize remote CompilerSets */
    private synchronized void initRemoteCompilerSets(boolean connect, final boolean runCompilerSetDataLoader) {

        //if (log.isLoggable(Level.FINEST)) {
        //    String text = String.format("\n\n---------- IRCS @%d remoteInitialization=%s state=%s writer=%b\n", //NOI18N
        //            System.identityHashCode(this), remoteInitialization, state, CompilerSetReporter.canReport());
        //    new Exception(text).printStackTrace();
        //}

        // NB: function itself is synchronized!
        if (state == State.STATE_COMPLETE) {
            return;
        }
        if (remoteInitialization != null) {
            return;
        }
        ServerRecord record = ServerList.get(executionEnvironment);
        assert record != null;

        log.fine("CSM.initRemoteCompilerSets for " + executionEnvironment + " [" + state + "]");
        final boolean wasOffline = record.isOffline();
        if (wasOffline) {
            CompilerSetReporter.report("CSM_Conn", false, executionEnvironment.getHost()); //NOI18N
        }
        record.validate(connect);
        if (record.isOnline()) {
            if (wasOffline) {
                CompilerSetReporter.report("CSM_Done"); //NOI18N
            }
            // NB: function itself is synchronized!
            remoteInitialization = RequestProcessor.getDefault().post(new Runnable() {

                @SuppressWarnings("unchecked")
                public void run() {
                    //if (log.isLoggable(Level.FINEST)) {
                    //    System.err.printf("\n\n###########\n###### %b @%d #######\n############\n\n",
                    //            CompilerSetReporter.canReport(),System.identityHashCode(CompilerSetManager.this));
                    //}
                    try {
                        final CompilerSetProvider provider = CompilerSetProviderFactory.createNew(executionEnvironment);
                        assert provider != null;
                        provider.init();
                        platform = provider.getPlatform();
                        CompilerSetReporter.report("CSM_ValPlatf", true, PlatformTypes.toString(platform)); //NOI18N
                        CompilerSetReporter.report("CSM_LFTC"); //NOI18N
                        log.fine("CSM.initRemoteCompileSets: platform = " + platform);
                        getPreferences().putInt(CSM + ExecutionEnvironmentFactory.toUniqueID(executionEnvironment) +
                                SET_PLATFORM, platform);
                        while (provider.hasMoreCompilerSets()) {
                            String data = provider.getNextCompilerSetData();
                            CompilerSet cs = parseCompilerSetString(platform, data);
                            if (cs != null) {
                                CompilerSetReporter.report("CSM_Found", true, cs.getDisplayName(), cs.getDirectory());//NOI18N
                                add(cs);
                            } else if(CompilerSetReporter.canReport()) {
                                CompilerSetReporter.report("CSM_Err", true, data);//NOI18N
                            }
                        }
                        completeCompilerSets(platform);
                        log.fine("CSM.initRemoteCompilerSets: Found " + sets.size() + " compiler sets");
                        if (sets.size() == 0) {
                            CompilerSetReporter.report("CSM_Done_NF"); //NOI18N
                        } else {
                            CompilerSetReporter.report("CSM_Done_OK", true,  sets.size());//NOI18N
                        }
                        // NB: function itself is synchronized!
                        state = State.STATE_COMPLETE;
                        CompilerSetReporter.report("CSM_Conigured");//NOI18N
                        if (runCompilerSetDataLoader) {
                            finishInitialization();
                        }
                    } catch (Throwable thr) {
                        // otherwise STATE_PENDING hangs forever - see #158088
                        // NB: function itself is synchronized!
                        state = State.STATE_UNINITIALIZED; //STATE_ERROR;
                        log.log(Level.FINE, "Error initiaizing compiler set @" + hashCode() + //NOI18N
                            " on " + executionEnvironment, thr); //NOI18N
                        CompilerSetReporter.report("CSM_Fail"); //NOI18N
                        add(CompilerSet.createEmptyCompilerSet(PlatformTypes.PLATFORM_NONE));
                    }
                }

            });
        } else {
            CompilerSetReporter.report("CSM_Fail");//NOI18N
            // create empty CSM
            log.fine("CSM.initRemoteCompilerSets: Adding empty CS to OFFLINE host " + executionEnvironment);
            add(CompilerSet.createEmptyCompilerSet(PlatformTypes.PLATFORM_NONE));
            // NB: function itself is synchronized!
            state = State.STATE_UNINITIALIZED; //STATE_ERROR;
        }
    }

    public void finishInitialization() {
        CompilerSetProvider provider = CompilerSetProviderFactory.createNew(executionEnvironment);
        List<CompilerSet> setsCopy = new ArrayList<CompilerSet>(sets);
        Runnable compilerSetDataLoader = provider.createCompilerSetDataLoader(setsCopy);
        CndUtils.assertFalse(compilerSetDataLoader == null);
        if (compilerSetDataLoader != null) {
            RequestProcessor.Task task = RequestProcessor.getDefault().create(compilerSetDataLoader);
            task.addTaskListener(new TaskListener() {
                public void taskFinished(org.openide.util.Task task) {
                    log.fine("Code Model Ready for " + CompilerSetManager.this.toString());
                    // FIXUP: this server has been probably deleted; TODO: provide return statis from loader
                    if (!ServerList.get(executionEnvironment).isDeleted()) {
                        CompilerSetManagerEvents.get(executionEnvironment).runTasks();
                    }
                }
            });
            task.schedule(0);
        }
    }

    public void initCompilerSet(CompilerSet cs) {
        initCompilerSet(cs.getDirectory(), cs, false);
        completeCompilerSet(executionEnvironment, cs, sets);
    }

    public void reInitCompilerSet(CompilerSet cs, String path) {
        cs.reparent(path);
        initCompilerSet(cs);
    }

    private boolean initCompilerSet(String path, CompilerSet cs, boolean known) {
        CompilerFlavor flavor = cs.getCompilerFlavor();
        ToolchainDescriptor d = flavor.getToolchainDescriptor();
        if (d != null && ToolchainManager.getImpl().isMyFolder(path, d, getPlatform(), known)) {
            CompilerDescriptor compiler = d.getC();
            if (compiler != null && !compiler.skipSearch()) {
                initCompiler(Tool.CCompiler, path, cs, compiler.getNames());
            }
            compiler = d.getCpp();
            if (compiler != null && !compiler.skipSearch()) {
                initCompiler(Tool.CCCompiler, path, cs, compiler.getNames());
            }
            compiler = d.getFortran();
            if (compiler != null && !compiler.skipSearch()) {
                initCompiler(Tool.FortranCompiler, path, cs, compiler.getNames());
            }
            compiler = d.getAssembler();
            if (compiler != null && !compiler.skipSearch()) {
                initCompiler(Tool.Assembler, path, cs, compiler.getNames());
            }
            if (d.getMake() != null && !d.getMake().skipSearch()){
                initCompiler(Tool.MakeTool, path, cs, d.getMake().getNames());
            }
            if (d.getDebugger() != null && !d.getDebugger().skipSearch()){
                initCompiler(Tool.DebuggerTool, path, cs, d.getDebugger().getNames());
            }
            if (d.getQMake() != null && !d.getQMake().skipSearch()){
                initCompiler(Tool.QMakeTool, path, cs, d.getQMake().getNames());
            }
            if (d.getCMake() != null && !d.getCMake().skipSearch()){
                initCompiler(Tool.CMakeTool, path, cs, d.getCMake().getNames());
            }
            return true;
        }
        return false;
    }

    private void initCompiler(int kind, String path, CompilerSet cs, String[] names) {
        File dir = new File(path);
        if (cs.findTool(kind) != null) {
            // Only one tool of each kind in a cs
            return;
        }
        for (String name : names) {
            File file = new File(dir, name);
            if (file.exists() && !file.isDirectory()) {
                cs.addTool(executionEnvironment, name, file.getAbsolutePath(), kind);
                return;
            }
            file = new File(dir, name + ".exe"); // NOI18N
            if (file.exists() && !file.isDirectory()) {
                cs.addTool(executionEnvironment, name, file.getAbsolutePath(), kind);
                return;
            }
            File file2 = new File(dir, name + ".exe.lnk"); // NOI18N
            if (file2.exists() && !file2.isDirectory()) {
                cs.addTool(executionEnvironment, name, file.getAbsolutePath(), kind);
                return;
            }
        }
    }

    /**
     * If a compiler set doesn't have one of each compiler types, add a "No compiler"
     * tool. If selected, this will tell the build validation things are OK.
     */
    private void completeCompilerSets() {
        for (CompilerSet cs : sets) {
            completeCompilerSet(executionEnvironment, cs, sets);
        }
        if (sets.size() == 0) { // No compilers found
            add(CompilerSet.createEmptyCompilerSet(getPlatform()));
        } else {
            setDefaltCompilerSet();
        }

        completeCompilerSets(getPlatform());
    }

    private void completeCompilerSets(int platform) {
        // Make sure 'SunStudio' set exists if just one other Sun Studio set found.
        CompilerSet sun = getCompilerSet("SunStudio"); // NOI18N
        if (sun == null) {
            // find 'best' Sun set and copy it
            sun = getCompilerSet("SunStudioExpress"); // NOI18N
            if (sun == null) {
                sun = getCompilerSet("SunStudio_12.1"); // NOI18N
            }
            if (sun == null) {
                sun = getCompilerSet("SunStudio_12"); // NOI18N
            }
            if (sun == null) {
                sun = getCompilerSet("SunStudio_11"); // NOI18N
            }
            if (sun == null) {
                sun = getCompilerSet("SunStudio_10"); // NOI18N
            }
            if (sun == null) {
                sun = getCompilerSet("SunStudio_9"); // NOI18N
            }
            if (sun == null) {
                sun = getCompilerSet("SunStudio_8"); // NOI18N
            }
            if (sun != null) {
                sun = sun.createCopy();
                sun.setName("SunStudio"); // NOI18N
                CompilerFlavor flavor = CompilerFlavor.toFlavor("SunStudio", platform); // NOI18N
                if (flavor != null) { // #158084 NPE
                    sun.setFlavor(flavor); // NOI18N
                    sun.setAutoGenerated(true);
                    add(sun);
                }
            }
        }
    }

    private static Tool autoComplete(ExecutionEnvironment env, CompilerSet cs, List<CompilerSet> sets, ToolDescriptor descriptor, int tool){
        if (descriptor != null) {
            AlternativePath[] paths = descriptor.getAlternativePath();
            if (paths != null && paths.length > 0) {
                for(AlternativePath p : paths){
                    switch(p.getKind()){
                        case PATH:
                        {
                            StringTokenizer st = new StringTokenizer(p.getPath(),";,"); // NOI18N
                            while(st.hasMoreTokens()){
                                String method = st.nextToken();
                                if ("$PATH".equals(method)){ // NOI18N
                                    for(String name : descriptor.getNames()){
                                        String path = findCommand(name);
                                        if (path != null) {
                                            if (notSkipedName(cs, descriptor, path, name)) {
                                                return cs.addNewTool(env, IpeUtils.getBaseName(path), path, tool);
                                            }
                                        }
                                    }
                                } else if ("$MSYS".equals(method)){ // NOI18N
                                    for(String name : descriptor.getNames()){
                                        String dir = getMSysBase();
                                        if (dir != null) {
                                            String path = findCommand(name, dir+"/bin"); // NOI18N
                                            if (path != null) {
                                                if (notSkipedName(cs, descriptor, path, name)) {
                                                    return cs.addNewTool(env, IpeUtils.getBaseName(path), path, tool);
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    for(String name : descriptor.getNames()){
                                        String path = findCommand(name, method);
                                        if (path != null) {
                                            return cs.addNewTool(env, IpeUtils.getBaseName(path), path, tool);
                                        }
                                    }
                                }
                            }
                            break;
                        }
                        case TOOL_FAMILY:
                        {
                            StringTokenizer st = new StringTokenizer(p.getPath(),";,"); // NOI18N
                            while(st.hasMoreTokens()){
                                String method = st.nextToken();
                                for(CompilerSet s : sets){
                                    if (s != cs) {
                                        for(String family : s.getCompilerFlavor().getToolchainDescriptor().getFamily()){
                                            if (family.equals(method)){
                                                Tool other = s.findTool(tool);
                                                if (other != null){
                                                    return cs.addNewTool(env, other.getName(), other.getPath(), tool);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        }
                        case TOOL_NAME:
                        {
                            StringTokenizer st = new StringTokenizer(p.getPath(),";,"); // NOI18N
                            while(st.hasMoreTokens()){
                                String method = st.nextToken();
                                for(CompilerSet s : sets){
                                    if (s != cs) {
                                        String name = s.getCompilerFlavor().getToolchainDescriptor().getName();
                                        if (name.equals(method) || "*".equals(method)){ // NOI18N
                                            Tool other = s.findTool(tool);
                                            if (other != null){
                                                return cs.addNewTool(env, other.getName(), other.getPath(), tool);
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
        return cs.addTool(env, "", "", tool); // NOI18N
    }

    private static boolean notSkipedName(CompilerSet cs, ToolDescriptor descriptor, String path, String name){
        if (!descriptor.skipSearch()) {
            return true;
        }
        String s = cs.getDirectory()+"/"+name; // NOI18N
        s = s.replaceAll("\\\\", "/"); // NOI18N
        path = path.replaceAll("\\\\", "/"); // NOI18N
        return !path.startsWith(s);
    }

    private static String findCommand(String name) {
        String path = Path.findCommand(name);
        if (path == null) {
            String dir = getMSysBase();
            if (dir != null) {
                path = findCommand(name, dir+"/bin"); // NOI18N
            }
        }
        return path;
    }

    private static String findCommand(String cmd, String dir) {
        File file;
        String cmd2 = null;
        if (cmd.length() > 0) {
            if (Utilities.isWindows() && !cmd.endsWith(".exe")) { // NOI18N
                cmd2 = cmd + ".exe"; // NOI18N
            }

            file = new File(dir, cmd);
            if (file.exists()) {
                return file.getAbsolutePath();
            } else {
                if (Utilities.isWindows() && cmd.endsWith(".exe")){// NOI18N
                    File file2 = new File(dir, cmd+".lnk");// NOI18N
                    if (file2.exists()) {
                        return file.getAbsolutePath();
                    }
                }
            }
            if (cmd2 != null) {
                file = new File(dir, cmd2);
                if (file.exists()) {
                    return file.getAbsolutePath();
                }
                File file2 = new File(dir, cmd2+".lnk");// NOI18N
                if (file2.exists()) {
                    return file.getAbsolutePath();
                }
            }
        }
        return null;
    }

    private static void completeCompilerSet(ExecutionEnvironment env, CompilerSet cs, List<CompilerSet> sets) {
        if (cs.findTool(Tool.CCompiler) == null) {
            autoComplete(env, cs, sets, cs.getCompilerFlavor().getToolchainDescriptor().getC(), Tool.CCompiler);
        }
        if (cs.findTool(Tool.CCCompiler) == null) {
            autoComplete(env, cs, sets, cs.getCompilerFlavor().getToolchainDescriptor().getCpp(), Tool.CCCompiler);
        }
        if (cs.findTool(Tool.FortranCompiler) == null) {
            autoComplete(env, cs, sets, cs.getCompilerFlavor().getToolchainDescriptor().getFortran(), Tool.FortranCompiler);
        }
        if (cs.findTool(Tool.Assembler) == null) {
            autoComplete(env, cs, sets, cs.getCompilerFlavor().getToolchainDescriptor().getAssembler(), Tool.Assembler);
        }
        if (cs.findTool(Tool.MakeTool) == null) {
            autoComplete(env, cs, sets, cs.getCompilerFlavor().getToolchainDescriptor().getMake(), Tool.MakeTool);
        }
        if (cs.findTool(Tool.DebuggerTool) == null) {
            autoComplete(env, cs, sets, cs.getCompilerFlavor().getToolchainDescriptor().getDebugger(), Tool.DebuggerTool);
        }
        if (cs.findTool(Tool.QMakeTool) == null) {
            autoComplete(env, cs, sets, cs.getCompilerFlavor().getToolchainDescriptor().getQMake(), Tool.QMakeTool);
        }
        if (cs.findTool(Tool.CMakeTool) == null) {
            autoComplete(env, cs, sets, cs.getCompilerFlavor().getToolchainDescriptor().getCMake(), Tool.CMakeTool);
        }
    }

    /**
     * Add a CompilerSet to this CompilerSetManager. Make sure it doesn't get added multiple times.
     *
     * @param cs The CompilerSet to (possibly) add
     */
    public void add(CompilerSet cs) {
//        String csdir = cs.getDirectory();

        if (sets.size() == 1 && sets.get(0).getName().equals(CompilerSet.None)) {
            sets.remove(0);
        }
//        if (cs.isAutoGenerated()) {
//            for (CompilerSet cs2 : sets) {
//                if (cs2.getDirectory().equals(csdir)) {
//                    return;
//                }
//            }
//        }
        sets.add(cs);
        if (sets.size() == 1) {
            setDefault(cs);
        }
    }

    public final boolean isEmpty() {
        if ((sets.size() == 0) ||
                (sets.size() == 1 && sets.get(0).getName().equals(CompilerSet.None))) {
            return true;
        }
        return false;
    }

    /**
     * Remove a CompilerSet from this CompilerSetManager. Use caution with this method. Its primary
     * use is to remove temporary CompilerSets which were added to represent missing compiler sets. In
     * that context, they're removed immediately after showing the ToolsPanel after project open.
     *
     * @param cs The CompilerSet to (possibly) remove
     */
    public void remove(CompilerSet cs) {
        int idx = sets.indexOf(cs);
        if (idx >= 0) {
            sets.remove(idx);
        }
    }

    public CompilerSet getCompilerSet(CompilerFlavor flavor) {
        return getCompilerSet(flavor.toString());
    }

    public CompilerSet getCompilerSet(String name) {
        for (CompilerSet cs : sets) {
            if (cs.getName().equals(name)) {
                return cs;
            }
        }
        return null;
    }

    public CompilerSet getCompilerSetByDisplayName(String name) {
        for (CompilerSet cs : sets) {
            if (cs.getDisplayName().equals(name)) {
                return cs;
            }
        }
        return null;
    }

    public CompilerSet getCompilerSetByPath(String path) {
        for (CompilerSet cs : sets) {
            if (cs.getDirectory().equals(path)) {
                return cs;
            }
        }
        return null;
    }

    public CompilerSet getCompilerSet(String name, String dname) {
        //waitForCompletion();
        if (isPending()) {
            log.warning("calling getCompilerSet() on uninitializad " + getClass().getSimpleName());
        }
        for (CompilerSet cs : sets) {
            if (cs.getName().equals(name) && cs.getDisplayName().equals(dname)) {
                return cs;
            }
        }
        return null;
    }

    public CompilerSet getCompilerSet(int idx) {
        //waitForCompletion();
        if (isPending()) {
            log.warning("calling getCompilerSet() on uninitializad " + getClass().getSimpleName());
        }
        if (idx >= 0 && idx < sets.size()) {
            return sets.get(idx);
        }
        return null;
    }

    public List<CompilerSet> getCompilerSets() {
        return sets;
    }

    public List<String> getCompilerSetDisplayNames() {
        List<String> names = new ArrayList<String>();
        for (CompilerSet cs : getCompilerSets()) {
            names.add(cs.getDisplayName());
        }
        return names;
    }

    public List<String> getCompilerSetNames() {
        List<String> names = new ArrayList<String>();
        for (CompilerSet cs : getCompilerSets()) {
            names.add(cs.getName());
        }
        return names;
    }

    public void setDefault(CompilerSet newDefault) {
        boolean set = false;
        for (CompilerSet cs : getCompilerSets()) {
            cs.setAsDefault(false);
            if (cs == newDefault) {
                newDefault.setAsDefault(true);
                set = true;
            }
        }
        if (!set && sets.size() > 0) {
            getCompilerSet(0).setAsDefault(true);
        }
    }

    public CompilerSet getDefaultCompilerSet() {
        for (CompilerSet cs : getCompilerSets()) {
            if (cs.isDefault()) {
                return cs;
            }
        }
        return null;
    }

    /** TODO: deprecate and remove */
    public static String getDefaultDevelopmentHost() {
        return ExecutionEnvironmentFactory.toUniqueID(getDefaultExecutionEnvironment());
    }

    public static ExecutionEnvironment getDefaultExecutionEnvironment() {
        return ServerList.getDefaultRecord().getExecutionEnvironment();
    }

    /**
     * Check if the gdb module is enabled. Don't show the gdb line if it isn't.
     *
     * @return true if the gdb module is enabled, false if missing or disabled
     */
    protected boolean isGdbEnabled() {
        Lookup.Result<ModuleInfo> providers = Lookup.getDefault().lookup(new Lookup.Template<ModuleInfo>(ModuleInfo.class));
        for (ModuleInfo info : providers.allInstances()) {
            if (info.getCodeNameBase().equals("org.netbeans.modules.cnd.debugger.gdb") && info.isEnabled()) { // NOI18N
                return true;
            }
        }
        return false;
    }

    private static CompilerProvider getCompilerProvider() {
        if (compilerProvider == null) {
            compilerProvider = CompilerProvider.getInstance();
        }
        return compilerProvider;
    }

    /*
     * Persistence ...
     */
    private static Preferences getPreferences() {
        return NbPreferences.forModule(CompilerSetManager.class);
    }

    public void saveToDisk() {
        if (!sets.isEmpty() && getPlatform() != PlatformTypes.PLATFORM_GENERIC) {
            getPreferences().putDouble(CSM + VERSION, csm_version);
            String executionEnvironmentKey = ExecutionEnvironmentFactory.toUniqueID(executionEnvironment);
            getPreferences().putInt(CSM + executionEnvironmentKey + NO_SETS, sets.size());
            getPreferences().putInt(CSM + executionEnvironmentKey + SET_PLATFORM, getPlatform());
            int setCount = 0;
            for (CompilerSet cs : getCompilerSets()) {
                getPreferences().put(CSM + executionEnvironmentKey + SET_NAME + setCount, cs.getName());
                getPreferences().put(CSM + executionEnvironmentKey + SET_FLAVOR + setCount, cs.getCompilerFlavor().toString());
                getPreferences().put(CSM + executionEnvironmentKey + SET_DIRECTORY + setCount, cs.getDirectory());
                getPreferences().putBoolean(CSM + executionEnvironmentKey + SET_AUTO + setCount, cs.isAutoGenerated());
                getPreferences().putBoolean(CSM + executionEnvironmentKey + SET_DEFAULT + setCount, cs.isDefault());
                List<Tool> tools = cs.getTools();
                getPreferences().putInt(CSM + executionEnvironmentKey + NO_TOOLS + setCount, tools.size());
                int toolCount = 0;
                for (Tool tool : tools) {
                    getPreferences().put(CSM + executionEnvironmentKey + TOOL_NAME + setCount + '.' + toolCount, tool.getName());
                    getPreferences().put(CSM + executionEnvironmentKey + TOOL_DISPLAYNAME + '-' + setCount + '.' + toolCount, tool.getDisplayName());
                    getPreferences().putInt(CSM + executionEnvironmentKey + TOOL_KIND + setCount + '.' + toolCount, tool.getKind());
                    getPreferences().put(CSM + executionEnvironmentKey + TOOL_PATH + setCount + '.' + toolCount, tool.getPath());
                    getPreferences().put(CSM + executionEnvironmentKey + TOOL_FLAVOR + setCount + '.' + toolCount, tool.getFlavor().toString());
                    toolCount++;
                }
                setCount++;
            }
        }
    }

    public static CompilerSetManager restoreFromDisk(ExecutionEnvironment env) {
        double version = getPreferences().getDouble(CSM + VERSION, 1.0);
        if (version == 1.0 && env.isLocal()) {
            return restoreFromDisk10();
        }
        String executionEnvironmentKey = ExecutionEnvironmentFactory.toUniqueID(env);
        int noSets = getPreferences().getInt(CSM + executionEnvironmentKey + NO_SETS, -1);
        if (noSets < 0) {
            return null;
        }
        int pform = getPreferences().getInt(CSM + executionEnvironmentKey + SET_PLATFORM, -1);
        if (pform < 0) {
            if (env.isLocal()) {
                pform = computeLocalPlatform();
            }
        }

        ArrayList<CompilerSet> css = new ArrayList<CompilerSet>();
        for (int setCount = 0; setCount < noSets; setCount++) {
            String setName = getPreferences().get(CSM + executionEnvironmentKey + SET_NAME + setCount, null);
            String setFlavorName = getPreferences().get(CSM + executionEnvironmentKey + SET_FLAVOR + setCount, null);
            CompilerFlavor flavor = null;
            if (setFlavorName != null) {
                flavor = CompilerFlavor.toFlavor(setFlavorName, pform);
            }
            String setDirectory = getPreferences().get(CSM + executionEnvironmentKey + SET_DIRECTORY + setCount, null);
            if (setName == null || setFlavorName == null || flavor == null) {
                // FIXUP: error
                continue;
            }
            Boolean auto = getPreferences().getBoolean(CSM + executionEnvironmentKey + SET_AUTO + setCount, false);
            Boolean isDefault = getPreferences().getBoolean(CSM + executionEnvironmentKey + SET_DEFAULT + setCount, false);
            CompilerSet cs = new CompilerSet(flavor, setDirectory, setName);
            cs.setAutoGenerated(auto);
            cs.setAsDefault(isDefault);
            int noTools = getPreferences().getInt(CSM + executionEnvironmentKey + NO_TOOLS + setCount, -1);
            for (int toolCount = 0; toolCount < noTools; toolCount++) {
                String toolName = getPreferences().get(CSM + executionEnvironmentKey + TOOL_NAME + setCount + '.' + toolCount, null);
                String toolDisplayName = getPreferences().get(CSM + executionEnvironmentKey + TOOL_DISPLAYNAME + '-' + setCount + '.' + toolCount, null);
                int toolKind = getPreferences().getInt(CSM + executionEnvironmentKey + TOOL_KIND + setCount + '.' + toolCount, -1);
                String toolPath = getPreferences().get(CSM + executionEnvironmentKey + TOOL_PATH + setCount + '.' + toolCount, null);
                String toolFlavorName = getPreferences().get(CSM + executionEnvironmentKey + TOOL_FLAVOR + setCount + '.' + toolCount, null);
                CompilerFlavor toolFlavor = null;
                if (toolFlavorName != null) {
                    toolFlavor = CompilerFlavor.toFlavor(toolFlavorName, pform);
                }
                Tool tool = getCompilerProvider().createCompiler(env, toolFlavor, toolKind, "", toolDisplayName, toolPath);
                tool.setName(toolName);
                cs.addTool(tool);
            }
            completeCompilerSet(env, cs, css);
            css.add(cs);
        }

        CompilerSetManager csm = new CompilerSetManager(env, css, pform);
        return csm;
    }

    public static CompilerSetManager restoreFromDisk10() {
        int noSets = getPreferences().getInt(CSM + NO_SETS, -1);
        if (noSets < 0) {
            return null;
        }

        ArrayList<CompilerSet> css = new ArrayList<CompilerSet>();
        getPreferences().remove(CSM + NO_SETS);
        for (int setCount = 0; setCount < noSets; setCount++) {
            String setName = getPreferences().get(CSM + SET_NAME + setCount, null);
            getPreferences().remove(CSM + SET_NAME + setCount);
            String setFlavorName = getPreferences().get(CSM + SET_FLAVOR + setCount, null);
            getPreferences().remove(CSM + SET_FLAVOR + setCount);
            CompilerFlavor flavor = null;
            if (setFlavorName != null) {
                flavor = CompilerFlavor.toFlavor(setFlavorName, PlatformTypes.getDefaultPlatform());
            }
            String setDirectory = getPreferences().get(CSM + SET_DIRECTORY + setCount, null);
            getPreferences().remove(CSM + SET_DIRECTORY + setCount);
            if (setName == null || setFlavorName == null || flavor == null) {
                // FIXUP: error
                continue;
            }
            Boolean auto = getPreferences().getBoolean(CSM + SET_AUTO + setCount, false);
            getPreferences().remove(CSM + SET_AUTO + setCount);
            CompilerSet cs = new CompilerSet(flavor, setDirectory, setName);
            cs.setAutoGenerated(auto);
            int noTools = getPreferences().getInt(CSM + NO_TOOLS + setCount, -1);
            getPreferences().remove(CSM + NO_TOOLS + setCount);
            for (int toolCount = 0; toolCount < noTools; toolCount++) {
                String toolName = getPreferences().get(CSM + TOOL_NAME + setCount + '.' + toolCount, null);
                String toolDisplayName = getPreferences().get(CSM + TOOL_DISPLAYNAME + '-' + setCount + '.' + toolCount, null);
                int toolKind = getPreferences().getInt(CSM + TOOL_KIND + setCount + '.' + toolCount, -1);
                String toolPath = getPreferences().get(CSM + TOOL_PATH + setCount + '.' + toolCount, null);
                String toolFlavorName = getPreferences().get(CSM + TOOL_FLAVOR + setCount + '.' + toolCount, null);
                getPreferences().remove(CSM + TOOL_NAME + setCount + '.' + toolCount);
                getPreferences().remove(CSM + TOOL_DISPLAYNAME + '-' + setCount + '.' + toolCount);
                getPreferences().remove(CSM + TOOL_KIND + setCount + '.' + toolCount);
                getPreferences().remove(CSM + TOOL_PATH + setCount + '.' + toolCount);
                getPreferences().remove(CSM + TOOL_FLAVOR + setCount + '.' + toolCount);
                CompilerFlavor toolFlavor = null;
                if (toolFlavorName != null) {
                    toolFlavor = CompilerFlavor.toFlavor(toolFlavorName, PlatformTypes.getDefaultPlatform());
                }
                Tool tool = getCompilerProvider().createCompiler(ExecutionEnvironmentFactory.getLocal(),
                        toolFlavor, toolKind, "", toolDisplayName, toolPath); //NOI18N
                tool.setName(toolName);
                cs.addTool(tool);
            }
            completeCompilerSet(ExecutionEnvironmentFactory.getLocal(), cs, css);
            css.add(cs);
        }
        CompilerSetManager csm = new CompilerSetManager(
                ExecutionEnvironmentFactory.getLocal(),
                css, computeLocalPlatform());
        return csm;
    }

    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(CompilerSetManager.class, s);
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("CSM for ").append(executionEnvironment.toString()); // NOI18N
        out.append(" with toolchains:["); // NOI18N
        for (CompilerSet compilerSet : sets) {
            out.append(compilerSet.getName()).append(" "); // NOI18N
        }
        out.append("]"); // NOI18N
        out.append(" platform:").append(PlatformTypes.toString(platform)); // NOI18N
        out.append(" in state ").append(state.toString()); // NOI18N
        return out.toString();
    }

    public ExecutionEnvironment getExecutionEnvironment() {
        return executionEnvironment;
    }
}
