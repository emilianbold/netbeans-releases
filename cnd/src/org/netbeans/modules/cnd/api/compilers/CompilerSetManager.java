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
package org.netbeans.modules.cnd.api.compilers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.TaskListener;

/**
 * Manage a set of CompilerSets. The CompilerSets are dynamically created based on which compilers
 * are found in the user's $PATH variable.
 */
public class CompilerSetManager {
    
    private static final Logger log = Logger.getLogger("cnd.remote.logger"); // NOI18N

    private static final HashMap<ExecutionEnvironment, CompilerSetManager> managers = new HashMap<ExecutionEnvironment, CompilerSetManager>();
    private static final Object MASTER_LOCK = new Object();

    // CopyOnWriteArrayList because of IZ#175647
    private List<CompilerSet> sets = new ArrayList<CompilerSet>();

    private final ExecutionEnvironment executionEnvironment;
    private volatile State state;
    private int platform = -1;
    private Task initializationTask;

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
    
    public void saveToDisk() {
        CompilerSetPreferences.saveToDisk(this);
    }

    private static CompilerSetManager getDefaultImpl(ExecutionEnvironment env, boolean initialize) {
        CompilerSetManager csm = null;
        boolean no_compilers = false;

        synchronized (MASTER_LOCK) {
            csm = managers.get(env);
            if (csm == null) {
                csm = CompilerSetPreferences.restoreFromDisk(env);
                if (csm != null && csm.getDefaultCompilerSet() == null) {
                    CompilerSetPreferences.saveToDisk(csm);
                }
            }
            if (csm == null) {
                csm = new CompilerSetManager(env, initialize);
                if (csm.isValid()) {
                    CompilerSetPreferences.saveToDisk(csm);
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
    public static void setManagers(Collection<CompilerSetManager> csms) {
        synchronized (MASTER_LOCK) {
            CompilerSetPreferences.clearPersistence();
            managers.clear();
            for (CompilerSetManager csm : csms) {
                csm.completeCompilerSets();
                CompilerSetPreferences.saveToDisk(csm);
                managers.put(csm.executionEnvironment, csm);
            }
        }
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
            platform = CompilerSetUtils.computeLocalPlatform();
            initCompilerSets(Path.getPath());
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

    CompilerSetManager(ExecutionEnvironment env, List<CompilerSet> sets, int platform) {
        //if (log.isLoggable(Level.FINEST)) {
        //    log.log(Level.FINEST, "CompilerSetManager CTOR B @" + System.identityHashCode(this) + ' '  + sets + ' ' + platform, new Exception()); //NOI18N
        //}
        this.executionEnvironment = env;
        this.sets = sets;
        this.platform = platform;
        completeCompilerSets();
        if(env.isRemote() && isEmpty()) {
            this.state = State.STATE_UNINITIALIZED;
            log.fine("CSM restoring from pref: Adding empty CS to host " + toString());
        } else {
            this.state = State.STATE_COMPLETE;
        }
    }

    public static final String getRemoteScript(String path) {
        return ToolchainScriptGenerator.generateScript(path);
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
        CndUtils.assertNonUiThread();
        if (isUninitialized()) {
            log.fine("CSM.getDefault: Doing remote setup from EDT?" + SwingUtilities.isEventDispatchThread());
            this.sets.clear();
            initRemoteCompilerSets(true, runCompilerSetDataLoader);
            if (initializationTask != null) {
                initializationTask.waitFinished();
                initializationTask = null;
            }
        }
        if (save) {
            synchronized (MASTER_LOCK) {
                CompilerSetPreferences.saveToDisk(this);
            }
        }
    }

    public int getPlatform() {
        if (platform < 0) {
            if (executionEnvironment.isLocal()) {
                platform = CompilerSetUtils.computeLocalPlatform();
            } else {
                if (isPending()) {
                    log.warning("calling getPlatform() on uninitializad " + getClass().getSimpleName());
                }
            }
        }
        return platform == -1 ? PlatformTypes.PLATFORM_NONE : platform;
    }

    public static CompilerSetManager getDeepCopy(ExecutionEnvironment execEnv, boolean initialize) {
        return getDefaultImpl(execEnv, initialize).deepCopy();
    }

    private CompilerSetManager deepCopy() {
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

    private Collection<FolderDescriptor> getPaths(ToolchainDescriptor d, CompilerFlavor flavor, ArrayList<String> dirlist) {
        LinkedHashSet<FolderDescriptor> dirs = new LinkedHashSet<FolderDescriptor>();
        // path from regestry
        String base = ToolchainManager.getImpl().getBaseFolder(d, getPlatform());
        if (base != null) {
            dirs.add(new FolderDescriptor(base, true));
        }
        // path from env
        for (String p : dirlist) {
            dirs.add(new FolderDescriptor(p, false));
        }
        // path from default location
        Map<String, List<String>> map = d.getDefaultLocations();
        if (map != null) {
            List<String> list = map.get(CompilerSetUtils.getPlatformName(getPlatform()));
            if (list != null) {
                for (String p : list) {
                    dirs.add(new FolderDescriptor(p, true));
                }
            }
        }
        // path from plugins
        String path = ToolChainPathProvider.getDefault().getPath(flavor);
        if (path != null) {
            dirs.add(new FolderDescriptor(path, true));
        }
        return dirs;
    }

    /** Search $PATH for all desired compiler sets and initialize cbCompilerSet and spCompilerSets */
    private synchronized void initCompilerSets(final ArrayList<String> dirlist) {
        // NB: function itself is synchronized!
        if (state == State.STATE_COMPLETE) {
            return;
        }
        if (initializationTask != null) {
            return;
        }
        String progressMessage = NbBundle.getMessage(getClass(), "PROGRESS_TEXT", executionEnvironment.getDisplayName()); // NOI18N
        ProgressHandle progressHandle = ProgressHandleFactory.createHandle(progressMessage);
        progressHandle.start();
        initializationTask = RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                initCompilerSetsImpl(dirlist);
            }
        });
        initializationTask.waitFinished();
        initializationTask = null;
        progressHandle.finish();
    }

    /** Search $PATH for all desired compiler sets and initialize cbCompilerSet and spCompilerSets */
    private void initCompilerSetsImpl(ArrayList<String> dirlist) {
        Set<CompilerFlavor> flavors = new HashSet<CompilerFlavor>();
        String SunStudioPath = System.getProperty("spro.bin");        // NB: function itself is synchronized!

        if (SunStudioPath != null) {
            File folder = new File(SunStudioPath);
            if (folder.isDirectory()) {
                for(ToolchainDescriptor d : ToolchainManager.getImpl().getToolchains(getPlatform())) {
                    if (d.isAbstract()) {
                        continue;
                    }
                    CompilerFlavor flavor = CompilerFlavor.toFlavor(d.getName(), getPlatform());
                    if (flavor == null) {
                        continue;
                    }
                    if (flavors.contains(flavor)) {
                        continue;
                    }
                    CompilerSet cs = CompilerSet.getCustomCompilerSet(folder.getAbsolutePath(), flavor, flavor.toString());
                    cs.setAutoGenerated(true);
                    if (initCompilerSet(SunStudioPath, cs, true)){
                        flavors.add(flavor);
                        addUnsafe(cs);
                        cs.setSunStudioDefault(true);
                    }
                }
            }
        }
        Loop:for(ToolchainDescriptor d : ToolchainManager.getImpl().getToolchains(getPlatform())) {
            if (d.isAbstract()) {
                continue;
            }
            CompilerFlavor flavor = CompilerFlavor.toFlavor(d.getName(), getPlatform());
            if (flavor == null) {
                continue;
            }
            if (flavors.contains(flavor)) {
                continue;
            }
            for (FolderDescriptor folderDescriptor : getPaths(d, flavor, dirlist)) {
                String path = folderDescriptor.path;
                if (path.equals("/usr/ucb")) { // NOI18N
                    // Don't look here.
                    continue;
                }
                if (!IpeUtils.isPathAbsolute(path)) {
                    path = CndFileUtils.normalizeAbsolutePath(new File(path).getAbsolutePath());
                }
                File dir = new File(path);
                if (dir.isDirectory()) {
                    if (ToolchainManager.getImpl().isMyFolder(dir.getAbsolutePath(), d, getPlatform(), folderDescriptor.knownFolder)){
                        if (d.getModuleID() == null && !d.isAbstract()) {
                            CompilerSet cs = CompilerSet.getCustomCompilerSet(dir.getAbsolutePath(), flavor, flavor.toString());
                            cs.setAutoGenerated(true);
                            if (initCompilerSet(path, cs, folderDescriptor.knownFolder)){
                                flavors.add(flavor);
                                addUnsafe(cs);
                                continue Loop;
                            }
                        }
                    }
                }
            }
        }
        addFakeCompilerSets();
        completeCompilerSets();
        state = State.STATE_COMPLETE;
    }

    /**
     * Since many toolchains have default locations, append them to the path (on a per-platform basis)
     * if they aren't already in the list.
     *
     * @param platform The platform we're running on
     * @param dirlist An ArrayList of the current PATH
     * @return A possibly modified ArrayList
     */
    static ArrayList<String> appendDefaultLocations(int platform, ArrayList<String> dirlist) {
        for (ToolchainDescriptor d : ToolchainManager.getImpl().getToolchains(platform)) {
            if (d.isAbstract()) {
                continue;
            }
            Map<String, List<String>> map = d.getDefaultLocations();
            if (map != null) {
                String pname = CompilerSetUtils.getPlatformName(platform);
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
        for (CompilerSet cs : sets) {
            if (cs.isDefault()) {
                return;
            }
        }
        CompilerSet bestCandidate = null;
        for (CompilerSet cs : sets) {
            if (cs.isSunCompiler()) {
                if ("OracleSolarisStudio".equals(cs.getName())) { // NOI18N
                    setDefault(cs);
                    return;
                }
                if (bestCandidate == null) {
                    bestCandidate = cs;
                }
            }
        }
        if (bestCandidate != null) {
            setDefault(bestCandidate);
            return;
        }
        if (!sets.isEmpty()) {
            setDefault(sets.get(0));
        }
    }

    public List<CompilerSet> findRemoteCompilerSets(String path) {
        ServerRecord record = ServerList.get(executionEnvironment);
        assert record != null;
	record.validate(true);
	if (!record.isOnline()) {
            return Collections.<CompilerSet>emptyList();
        }
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
        for(CompilerSet cs : css) {
            completeCompilerSet(executionEnvironment, cs, css);
        }
        return css;
    }

    private CompilerSet parseCompilerSetString(int platform, String data) {
        log.fine("CSM.initRemoteCompileSets: line = [" + data + "]"); // NOI18N
        String flavor;
        String path;
        StringTokenizer st = new StringTokenizer(data, ";"); // NOI18N
        try {
            flavor = st.nextToken();
            path = st.nextToken();
        } catch (NoSuchElementException ex) {
            log.warning("Malformed compilerSetString: " + data); // NOI18N
            return null;
        }
        CompilerFlavor compilerFlavor = CompilerFlavor.toFlavor(flavor, platform);
        if (compilerFlavor == null) { // #158084
            log.warning("NULL compiler flavor for " + flavor + " on platform " + platform); // NOI18N
            return null;
        }
        CompilerSet cs = new CompilerSet(compilerFlavor, path, flavor);
        while (st.hasMoreTokens()) {
            String name = st.nextToken();
            int i = name.indexOf('='); // NOI18N
            if (i < 0) {
                continue;
            }
            String tool = name.substring(0,i);
            String p = name.substring(i + 1);
            i = name.lastIndexOf('/');
            if (i < 0) {
                i = name.lastIndexOf('\\');
            }
            if (i > 0) {
                name = name.substring(i+1);
            }
            int kind = -1;
            if (tool.equals("c")){ // NOI18N
                kind = Tool.CCompiler;
            } else if (tool.equals("cpp")){ // NOI18N
                kind = Tool.CCCompiler;
            } else if (tool.equals("fortran")){ // NOI18N
                kind = Tool.FortranCompiler;
            } else if (tool.equals("assembler")){ // NOI18N
                kind = Tool.Assembler;
            } else if (tool.equals("make")){ // NOI18N
                kind = Tool.MakeTool;
            } else if (tool.equals("debugger")){ // NOI18N
                kind = Tool.DebuggerTool;
            } else if (tool.equals("cmake")){ // NOI18N
                kind = Tool.CMakeTool;
            } else if (tool.equals("qmake")){ // NOI18N
                kind = Tool.QMakeTool;
            } else if (tool.equals("c(PATH)")){ // NOI18N
                cs.addPathCandidate(Tool.CCompiler, p);
            } else if (tool.equals("cpp(PATH)")){ // NOI18N
                cs.addPathCandidate(Tool.CCCompiler, p);
            } else if (tool.equals("fortran(PATH)")){ // NOI18N
                cs.addPathCandidate(Tool.FortranCompiler, p);
            } else if (tool.equals("assembler(PATH)")){ // NOI18N
                cs.addPathCandidate(Tool.Assembler, p);
            } else if (tool.equals("make(PATH)")){ // NOI18N
                cs.addPathCandidate(Tool.MakeTool, p);
            } else if (tool.equals("debugger(PATH)")){ // NOI18N
                cs.addPathCandidate(Tool.DebuggerTool, p);
            } else if (tool.equals("cmake(PATH)")){ // NOI18N
                cs.addPathCandidate(Tool.CMakeTool, p);
            } else if (tool.equals("qmake(PATH)")){ // NOI18N
                cs.addPathCandidate(Tool.QMakeTool, p);
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
        if (initializationTask != null) {
            return;
        }
        ServerRecord record = ServerList.get(executionEnvironment);
        assert record != null;

        log.fine("CSM.initRemoteCompilerSets for " + executionEnvironment + " [" + state + "]"); // NOI18N
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
            initializationTask = RequestProcessor.getDefault().post(new Runnable() {

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
                        log.fine("CSM.initRemoteCompileSets: platform = " + platform); // NOI18N
                        CompilerSetPreferences.putEnv(executionEnvironment, platform);
                        while (provider.hasMoreCompilerSets()) {
                            String data = provider.getNextCompilerSetData();
                            CompilerSet cs = parseCompilerSetString(platform, data);
                            if (cs != null) {
                                CompilerSetReporter.report("CSM_Found", true, cs.getDisplayName(), cs.getDirectory()); //NOI18N
                                addUnsafe(cs);
                                for (Tool tool : cs.getTools()) {
                                    if (! tool.isReady()) {
                                        CompilerSetReporter.report("CSM_Initializing_Tool", false, tool.getDisplayName()); //NOI18N
                                        tool.waitReady(true);
                                        CompilerSetReporter.report("CSM_Done"); //NOI18N
                                    }
                                }
                            } else if(CompilerSetReporter.canReport()) {
                                CompilerSetReporter.report("CSM_Err", true, data);//NOI18N
                            }
                        }
                        completeCompilerSets();

                        log.fine("CSM.initRemoteCompilerSets: Found " + sets.size() + " compiler sets"); // NOI18N
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
                        completeCompilerSets();
                    }
                }

            });
        } else {
            CompilerSetReporter.report("CSM_Fail");//NOI18N
            // create empty CSM
            log.fine("CSM.initRemoteCompilerSets: Adding empty CS to OFFLINE host " + executionEnvironment);
            completeCompilerSets();
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

    private void addFakeCompilerSets() {
        for (CompilerFlavor flavor : CompilerFlavor.getFlavors(getPlatform())) {
            ToolchainDescriptor descriptor = flavor.getToolchainDescriptor();
            if (descriptor.getUpdateCenterUrl() != null && descriptor.getModuleID() != null) {
                boolean found = false;
                mainLoop:for (CompilerSet cs : sets) {
                    for(String family : cs.getCompilerFlavor().getToolchainDescriptor().getFamily()){
                        for(String f : flavor.getToolchainDescriptor().getFamily()){
                            if (family.equals(f)) {
                                found = true;
                                break mainLoop;
                            }
                        }
                    }
                }
                if (!found) {
                    CompilerSet fake = CompilerSet.getCustomCompilerSet(null, flavor, null);
                    fake.setAutoGenerated(true);
                    addUnsafe(fake);
                }
            }
        }
    }

    /**
     * If a compiler set doesn't have one of each compiler types, add a "No compiler"
     * tool. If selected, this will tell the build validation things are OK.
     */
    private void completeCompilerSets() {
        if (sets.size() == 0) { // No compilers found
            addUnsafe(CompilerSet.createEmptyCompilerSet(PlatformTypes.PLATFORM_NONE));
        }
        for (CompilerSet cs : sets) {
            completeCompilerSet(executionEnvironment, cs, sets);
        }
        completeSunStudioCompilerSet(getPlatform());
        setDefaltCompilerSet();
        Collections.<CompilerSet>sort(sets, new Comparator<CompilerSet>(){
            public int compare(CompilerSet o1, CompilerSet o2) {
                return o1.getCompilerFlavor().getToolchainDescriptor().getName().compareTo(o2.getCompilerFlavor().getToolchainDescriptor().getName());
            }
        });
        completeCompilerSetsSettings(false);
    }

    private void completeCompilerSetsSettings(boolean reset) {
        for (CompilerSet cs : sets) {
            for (Tool tool : cs.getTools()) {
                if (!tool.isReady()) {
                    tool.waitReady(reset);
                }
            }
        }
    }

    private void completeSunStudioCompilerSet(int platform) {
        CompilerSet bestCandidate = null;
        for(CompilerSet cs : sets) {
            if (cs.isSunStudioDefault()){
                bestCandidate = cs;
                break;
            }
        }
        // find 'best' Sun set and copy it
        CompilerSet sun = getCompilerSet("OracleSolarisStudio"); // NOI18N
        if (sun != null) {
            return;
        }
        if (bestCandidate == null) {
            bestCandidate = getCompilerSet("OracleSolarisStudio_12.2"); // NOI18N
        }
        if (bestCandidate == null) {
            bestCandidate = getCompilerSet("OracleSolarisStudioExpress"); // NOI18N
            if (bestCandidate != null && bestCandidate.getCompilerFlavor().getToolchainDescriptor().getDisplayName().indexOf("10") < 0) { // NOI18N
                bestCandidate = null;
            }
        }
        if (bestCandidate == null) {
            bestCandidate = getCompilerSet("SunStudio_12.1"); // NOI18N
        }
        if (bestCandidate == null) {
            bestCandidate = getCompilerSet("SunStudioExpress"); // NOI18N
        }
        if (bestCandidate == null) {
            bestCandidate = getCompilerSet("SunStudioLite"); // NOI18N
        }
        if (bestCandidate == null) {
            bestCandidate = getCompilerSet("SunStudio_12"); // NOI18N
        }
        if (bestCandidate == null) {
            bestCandidate = getCompilerSet("SunStudio_11"); // NOI18N
        }
        if (bestCandidate == null) {
            bestCandidate = getCompilerSet("SunStudio_10"); // NOI18N
        }
        if (bestCandidate == null) {
            bestCandidate = getCompilerSet("SunStudio_9"); // NOI18N
        }
        if (bestCandidate == null) {
            bestCandidate = getCompilerSet("SunStudio_8"); // NOI18N
        }
        if (bestCandidate == null) {
            return;
        }
        if (bestCandidate.isUrlPointer()) {
            return;
        }
        CompilerSet bestCandidateCopy = bestCandidate.createCopy();
        bestCandidateCopy.setName("OracleSolarisStudio"); // NOI18N
        CompilerFlavor flavor = CompilerFlavor.toFlavor("SunStudio", platform); // NOI18N
        if (flavor != null) { // #158084 NPE
            bestCandidateCopy.setFlavor(flavor);
            bestCandidateCopy.setAutoGenerated(true);
            addUnsafe(bestCandidateCopy);
            if (bestCandidate.isDefault()) {
                bestCandidateCopy.setAsDefault(false);
            }
            // Also add a SunStudio set for backward compatibility reasons
            CompilerSet sunStudio = getCompilerSet("SunStudio"); // NOI18N
            if (sunStudio == null) {
                sunStudio = bestCandidateCopy.createCopy();
                sunStudio.setName("SunStudio"); // NOI18N
                sunStudio.setFlavor(flavor);
                sunStudio.setAutoGenerated(true);
                addUnsafe(sunStudio);
            }
        }
    }

    private static Tool autoComplete(ExecutionEnvironment env, CompilerSet cs, List<CompilerSet> sets, ToolDescriptor descriptor, int tool){
        if (descriptor != null && !cs.isUrlPointer()) {
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
                                    if (env.isLocal()) {
                                        for(String name : descriptor.getNames()){
                                            String path = CompilerSetUtils.findCommand(name);
                                            if (path != null) {
                                                if (notSkipedName(cs, descriptor, path, name)) {
                                                    return cs.addNewTool(env, IpeUtils.getBaseName(path), path, tool);
                                                }
                                            }
                                        }
                                     } else {
                                        String path = cs.getPathCandidate(tool);
                                        if (path != null) {
                                            String name = IpeUtils.getBaseName(path);
                                            if (notSkipedName(cs, descriptor, path, name)) {
                                                return cs.addNewTool(env, name, path, tool);
                                            }
                                        }
                                    }
                                } else if ("$MSYS".equals(method)){ // NOI18N
                                    if (env.isLocal()) {
                                        for(String name : descriptor.getNames()){
                                            String dir = CompilerSetUtils.getMSysBase();
                                            if (dir != null) {
                                                String path = CompilerSetUtils.findCommand(name, dir+"/bin"); // NOI18N
                                                if (path != null) {
                                                    if (notSkipedName(cs, descriptor, path, name)) {
                                                        return cs.addNewTool(env, IpeUtils.getBaseName(path), path, tool);
                                                    }
                                                }
                                            }
                                        }
                                     } else {
                                        // TODO
                                    }
                                } else {
                                    if (env.isLocal()) {
                                        for(String name : descriptor.getNames()){
                                            String path = CompilerSetUtils.findCommand(name, method);
                                            if (path != null) {
                                                return cs.addNewTool(env, IpeUtils.getBaseName(path), path, tool);
                                            }
                                        }
                                    } else {
                                        // TODO
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

    static void completeCompilerSet(ExecutionEnvironment env, CompilerSet cs, List<CompilerSet> sets) {
        //if (cs.findTool(Tool.CCompiler) == null && cs.findTool(Tool.CCCompiler) == null) {
        //    // do not complete empty tool
        //    return;
        //}
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
        if (sets.size() == 1 && sets.get(0).getName().equals(CompilerSet.None)) {
            sets.remove(0);
        }
        sets.add(cs);
        if (sets.size() == 1) {
            setDefault(cs);
        }
        completeCompilerSets();
    }

    private void addUnsafe(CompilerSet cs) {
        if (sets.size() == 1 && sets.get(0).getName().equals(CompilerSet.None)) {
            sets.remove(0);
        }
        sets.add(cs);
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
        return new ArrayList<CompilerSet>(sets);
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

    private static enum State {
        STATE_PENDING,
        STATE_COMPLETE,
        STATE_UNINITIALIZED
    }

    private static final class FolderDescriptor {
        private final String path;
        private final boolean knownFolder;
        private FolderDescriptor(String path, boolean knownFolder){
            this.path = path;
            this.knownFolder = knownFolder;
        }

        @Override
        public int hashCode() {
            return path.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof FolderDescriptor) {
                return path.equals(((FolderDescriptor)obj).path);
            }
            return false;
        }
    }
}
