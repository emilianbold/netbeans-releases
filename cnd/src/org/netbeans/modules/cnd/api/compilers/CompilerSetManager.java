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
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.netbeans.modules.cnd.api.compilers.CompilerSet.CompilerFlavor;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.api.utils.Path;
import org.netbeans.modules.cnd.compilers.DefaultCompilerProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * Manage a set of CompilerSets. The CompilerSets are dynamically created based on which compilers
 * are found in the user's $PATH variable.
 */
public class CompilerSetManager implements PlatformTypes {
    
    /* Legacy defines for CND 5.5 compiler set definitions */
    public static final int SUN_COMPILER_SET = 0;
    public static final int GNU_COMPILER_SET = 1;
    
    public static final Object STATE_PENDING = "state_pending"; // NOI18N
    public static final Object STATE_COMPLETE = "state_complete"; // NOI18N
    
    public static final String LOCALHOST = "localhost"; // NOI18N
    
    private static final String gcc_pattern = "([a-zA-z][a-zA-Z0-9_]*-)*gcc(([-.]\\d){2,4})?(\\.exe)?"; // NOI18N
    private static final String gpp_pattern = "([a-zA-z][a-zA-Z0-9_]*-)*g\\+\\+(([-.]\\d){2,4})?(\\.exe)?$"; // NOI18N
    private static final String cc_pattern = "([a-zA-z][a-zA-Z0-9_]*-)*cc(([-.]\\d){2,4})?(\\.exe)?$"; // NOI18N
    private static final String CC_pattern = "([a-zA-z][a-zA-Z0-9_]*-)*CC(([-.]\\d){2,4})?$"; // NOI18N
    private static final String fortran_pattern = "([a-zA-z][a-zA-Z0-9_]*-)*[fg](77|90|95|fortran)(([-.]\\d){2,4})?(\\.exe)?"; // NOI18N
    private static final String make_pattern = "([a-zA-z][a-zA-Z0-9_]*-)*([dg]make|make)(([-.]\\d){2,4})?(\\.exe)?$"; // NOI18N
    private static final String debugger_pattern = "([a-zA-z][a-zA-Z0-9_]*-)*(gdb|dbx)(([-.]\\d){2,4})?(\\.exe)?$"; // NOI18N
    
    /* Persistance information */
    private static final double csm_version = 1.1;
    private static final String CSM = "csm."; // NOI18N
    private static final String VERSION = "version"; // NOI18N
    private static final String NO_SETS = ".noOfSets"; // NOI18N
    private static final String SET_NAME = ".setName."; // NOI18N
    private static final String CURRENT_SET_NAME = ".currentSetName"; // NOI18N
    private static final String SET_FLAVOR = ".setFlavor."; // NOI18N
    private static final String SET_DIRECTORY = ".setDirectory."; // NOI18N
    private static final String SET_AUTO = ".autoGenerated."; // NOI18N
    private static final String SET_PLATFORM = ".setPlatform."; // NOI18N
    private static final String NO_TOOLS = ".noOfTools."; // NOI18N
    private static final String TOOL_NAME = ".toolName."; // NOI18N
    private static final String TOOL_DISPLAYNAME = ".toolDisplayName."; // NOI18N
    private static final String TOOL_KIND = ".toolKind."; // NOI18N
    private static final String TOOL_PATH = ".toolPath."; // NOI18N
    private static final String TOOL_FLAVOR = ".toolFlavor."; // NOI18N
    
    private static CompilerFilenameFilter gcc_filter;
    private static CompilerFilenameFilter gpp_filter;
    private static CompilerFilenameFilter cc_filter;
    private static CompilerFilenameFilter CC_filter;
    private static CompilerFilenameFilter fortran_filter;
    private static CompilerFilenameFilter make_filter;
    private static CompilerFilenameFilter debugger_filter;
    private static HashMap<String, CompilerSetManager> managers = new HashMap<String, CompilerSetManager>();
    private final static Object MASTER_LOCK = new Object();
    private static CompilerProvider compilerProvider = null;
    
    public static final String Sun12 = "SunStudio_12"; // NOI18N
    public static final String Sun11 = "SunStudio_11"; // NOI18N
    public static final String Sun10 = "SunStudio_10"; // NOI18N
    public static final String Sun = "SunStudio"; // NOI18N
    public static final String GNU = "GNU"; // NOI18N
    
    private ArrayList<CompilerSet> sets = new ArrayList();
    private final String hkey;
    private Object state;
    private int platform = -1;
    private int current;
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
     * @param key Either user@host or localhost
     * @return A default CompilerSetManager for the given key
     */
    public static CompilerSetManager getDefault(String key) {
        CompilerSetManager csm = null;
        boolean no_compilers = false;
        
        synchronized (MASTER_LOCK) {
            csm = managers.get(key);
            if (csm == null) {
                csm = restoreFromDisk(key);
            }
            if (csm == null) {
                csm = new CompilerSetManager(key);
                if (csm.isValid()) {
                    csm.current = 0;
                    String csName = getPreferences().get(CSM + key + CURRENT_SET_NAME, null);
                    if (csName == null) {
                        csName = csm.getCompilerSet(0).getName();
                    }
                    csm.saveToDisk();
                } else if (!csm.isPending()) {
                    no_compilers = true;
                }
            }
            if (csm != null) { 
                managers.put(key, csm);
            }
        }
        if (no_compilers) {
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
        return csm;
    }
    
    public static CompilerSetManager getDefault() {
	return getDefault(LOCALHOST);
    }
    
    /** Create a CompilerSetManager which may be registered at a later time via CompilerSetManager.setDefault() */
    public static CompilerSetManager create() {
        CompilerSetManager csm;
        synchronized (MASTER_LOCK) {
            csm = new CompilerSetManager(LOCALHOST);
        }
        return csm;
    }
    
    /** Replace the default CompilerSetManager. Let registered listeners know its been updated */
    public static synchronized void setDefault(CompilerSetManager csm) {
        if (csm.getCompilerSets().size() == 0) { // No compilers found
            csm.add(CompilerSet.createEmptyCompilerSet());
        }
        synchronized (MASTER_LOCK) {
            csm.saveToDisk();
            managers.put(csm.hkey, csm);
        }
    }
    
    private CompilerSetManager(String key) {
        hkey = key;
        state = STATE_PENDING;
        init();
    }
    
    private CompilerSetManager(String hkey, ArrayList<CompilerSet> sets, int current) {
        this.hkey = hkey;
        this.sets = sets;
        this.current = current;
        state = STATE_COMPLETE;
        if (hkey.equals(LOCALHOST)) {
            platform = computeLocalPlatform();
        }
    }
    
    private void init() {
        if (hkey.equals(LOCALHOST)) {
            platform = computeLocalPlatform();
            initCompilerFilters();
            initCompilerSets(Path.getPath());
            state = STATE_COMPLETE;
        } else {
            log.fine("CSM.init: initializing remote compiler set for: " + hkey);
            initRemoteCompilerSets(hkey);
        }
    }
    
    public boolean isValid() {
        return sets.size() > 0 && !sets.get(0).getName().equals(CompilerSet.None);
    }
    
    public boolean isPending() {
        return state == STATE_PENDING;
    }

    public int getPlatform() {
        if (platform < 0) {
            if (hkey.equals(LOCALHOST)) {
                platform = computeLocalPlatform();
            } else {
                waitForCompletion();
            }
        }
        return platform;
    }
    
    public void waitForCompletion() {
        while (isPending()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }
    }
    
    private static int computeLocalPlatform() {
        String os = System.getProperty("os.name"); // NOI18N
        
        if (os.equals("SunOS")) {
            return System.getProperty("os.arch").equals("x86") ? PLATFORM_SOLARIS_INTEL : PLATFORM_SOLARIS_SPARC; // NOI18N
        } else if (os.startsWith("Windows ")) {
            return PLATFORM_WINDOWS;
        } else if (os.toLowerCase().contains("linux")) {
            return PLATFORM_LINUX;
        } else if (os.toLowerCase().contains("mac")) {
            return PLATFORM_MACOSX;
        } else {
            return PLATFORM_GENERIC;
        }
    }
    
    public CompilerSetManager deepCopy() {
        waitForCompletion(); // in case its a remote connection...
        // FIXUP: need a real deep copy..
        CompilerSetManager copy = new CompilerSetManager(hkey, new ArrayList<CompilerSet>(), current);
        for (CompilerSet set : getCompilerSets()) {
            copy.add(set.createCopy());
        }
        return copy;
    }
    
    public String getUniqueCompilerSetName(String baseName) {
        int n = 0;
        String suggestedName = baseName;
        while (true) {
            suggestedName = baseName + (n > 0 ? ("_" + n) : ""); // NOI18N
            if (getCompilerSet(suggestedName) != null) {
                n++;
            }
            else {
                break;
            }
        }
        return suggestedName;
    }
    
    /** Search $PATH for all desired compiler sets and initialize cbCompilerSet and spCompilerSets */
    public void initCompilerSets(ArrayList<String> dirlist) {
        HashSet flavors = new HashSet();
        
        for (String path : dirlist) {
            if (path.equals("/usr/ucb")) { // NOI18N
                // Don't look here.
                continue;
            }
            if (!IpeUtils.isPathAbsolute(path)) {
                path = FileUtil.normalizeFile(new File(path)).getAbsolutePath();
            }
            File dir = new File(path);
            if (dir.isDirectory()&& isACompilerSetFolder(dir)) {
                ArrayList<String> list = new ArrayList<String>();
                if (new File(dir, "cc").exists()) // NOI18N
                    list.add("cc"); // NOI18N
                if (new File(dir, "gcc").exists()) // NOI18N
                    list.add("gcc"); // NOI18N
                CompilerSet.CompilerFlavor flavor = CompilerSet.getCompilerSetFlavor(dir.getAbsolutePath(), (String[])list.toArray(new String[list.size()]));
                if (flavors.contains(flavor)) {
                    // Skip ...
                    continue;
                }
                flavors.add(flavor);
                CompilerSet cs = null;
                if (path.contains("msys")) { // NOI18N)
                    cs = getCompilerSet(CompilerFlavor.MinGW);
                    if (cs != null          )
                        initCompilerSet(path, cs);
                }
                if (cs == null) {
                    cs = CompilerSet.getCustomCompilerSet(dir.getAbsolutePath(), flavor, flavor.toString());
                    cs.setAutoGenerated(true);
                    initCompilerSet(path, cs);
                    add(cs);
                }
            }
        }
        completeCompilerSets();
    }
    
    /** Initialize remote CompilerSets */
    private void initRemoteCompilerSets(final String key) {
        final CompilerSetProvider provider = (CompilerSetProvider) Lookup.getDefault().lookup(CompilerSetProvider.class);
        if (provider != null) {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    provider.init(key);
                    platform = provider.getPlatform();
                    log.fine("CSM.initRemoveCompileSets: platform = " + platform);
                    getPreferences().putInt(CSM + hkey + SET_PLATFORM, platform);
                    while (provider.hasMoreCompilerSets()) {
                        String data = provider.getNextCompilerSetData();
                        log.fine("CSM.initRemoveCompileSets: line = [" + data + "]");
                        int i1 = data.indexOf(';');
                        int i2 = data.indexOf(';', i1 + 1);
                        String flavor = data.substring(0, i1);
                        String path = data.substring(i1 + 1, i2);
                        String tools = data.substring(i2 + 1);
                        CompilerSet cs = new CompilerSet(CompilerFlavor.toFlavor(flavor), path, flavor);
                        StringTokenizer st = new StringTokenizer(tools, ";"); // NOI18N
                        while (st.hasMoreTokens()) {
                            String name = st.nextToken();
                            int kind = -1;
                            String p = path + '/' + name;
                            if (flavor.startsWith("Sun")) { // NOI18N
                                if (name.equals("cc")) { // NOI18N
                                    kind = Tool.CCompiler;
                                } else if (name.equals("CC")) { // NOI18N
                                    kind = Tool.CCCompiler;
                                } else if (name.equals("dmake")) { // NOI18N
                                    kind = Tool.MakeTool;
                                } else if (name.startsWith("gdb=")) { // NOI18N
                                    kind = Tool.DebuggerTool;
                                    i1 = name.indexOf('=');
                                    p = name.substring(i1 + 1);
                                }
                            } else {
                                if (name.equals("gcc")) { // NOI18N
                                    kind = Tool.CCompiler;
                                } else if (name.equals("g++")) { // NOI18N
                                    kind = Tool.CCCompiler;
                                } else if (name.equals("make") ||  // NOI18N
                                        ((platform == PLATFORM_SOLARIS_INTEL || platform == PLATFORM_SOLARIS_SPARC) &&
                                                name.equals("gmake"))) { // NOI18N
                                    kind = Tool.MakeTool;
                                } else if (name.equals("gdb")) { // NOI18N
                                    kind = Tool.DebuggerTool;
                                } else if (name.startsWith("gdb=")) { // NOI18N
                                    kind = Tool.DebuggerTool;
                                    i1 = name.indexOf('=');
                                    p = name.substring(i1 + 1);
                                }
                            }
                            if (kind != -1) {
                                cs.addTool(key, name, p, kind);
                            }
                        }
                        add(cs);
                    }
                    // TODO: this should be upgraded to error reporting
                    // about absence of tool chain on remote host
                    // also compilersetmanager without compiler sets
                    // should be handled gracefully
                    log.fine("CSM.initRemoteCompilerSets: Found " + sets.size() + " compiler sets");
                    state = STATE_COMPLETE;
                }
            });
        } else {
            throw new IllegalStateException();
        }
    }
    
    public void initCompilerSet(CompilerSet cs) {
        initCompilerSet(cs.getDirectory(), cs);
        completeCompilerSet(hkey, cs);
    }
    
    public void reInitCompilerSet(CompilerSet cs, String path) {
        cs.reparent(path);
        initCompilerSet(cs);
    }
    
    private void initCompilerSet(String path, CompilerSet cs) {
        initCompiler(gcc_filter, "gcc", Tool.CCompiler, path, cs); // NOI18N
        initCompiler(gpp_filter, "g++", Tool.CCCompiler, path, cs); // NOI18N
        initCompiler(cc_filter, "cc", Tool.CCompiler, path, cs); // NOI18N
        initFortranCompiler(fortran_filter, Tool.FortranCompiler, path, cs); // NOI18N
        if (Utilities.isUnix()) {  // CC and cc are the same on Windows, so skip this step on Windows
            initCompiler(CC_filter, "CC", Tool.CCCompiler, path, cs); // NOI18N
        }
        initMakeTool(make_filter, Tool.MakeTool, path, cs); // NOI18N
        initDebuggerTool(debugger_filter, Tool.DebuggerTool, path, cs); // NOI18N
    }
    
    /**
     * Check whether folder is a compilerset. It needs at least one C or C++ compiler.
     * @param folder
     * @return
     */
    private boolean isACompilerSetFolder(File folder) {
        String[] compilerNames = new String[] {"gcc", "g++", "cc", "CC"}; // NOI18N
        if (folder.getPath().contains("msys") && new File(folder, "make.exe").exists()) { // NOI18N
            return true;
        }
        for (int i = 0; i < compilerNames.length; i++) {
            if (new File(folder, compilerNames[i]).exists() || new File(folder, compilerNames[i] + ".exe").exists()) // NOI18N
                return true;
        }
        return false;
    }
    
    private void initCompiler(CompilerFilenameFilter filter, String best, int kind, String path, CompilerSet cs) {
        File dir = new File(path);
        String[] list = dir.list(filter);

        if (list != null && list.length > 0) {
            if (cs == null) {
                CompilerFlavor flavor = CompilerSet.getCompilerSetFlavor(dir.getAbsolutePath(), list);
                cs = getCompilerSet(flavor);
                if (cs != null && !cs.getDirectory().equals(path))
                    return;
                cs = CompilerSet.getCompilerSet(dir.getAbsolutePath(), list);
                add(cs);
                if (cs.findTool(kind) != null) {
                    // Only one tool of each kind in a cs
                    return;
                }
            }
            for (String name : list) {
                File file = new File(dir, name);
                if (file.exists() && !file.isDirectory() && (name.equals(best) || name.equals(best + ".exe"))) { // NOI18N
                    cs.addTool(hkey, name, file.getAbsolutePath(), kind);
                    break;
                }
            }
        }
    }
    
    private void initFortranCompiler(CompilerFilenameFilter filter, int kind, String path, CompilerSet cs) {
        File dir = new File(path);
        String[] list = dir.list(filter);
        String[] best = {
            "gfortran", // NOI18N
            "g95", // NOI18N
            "g90", // NOI18N
            "g77", // NOI18N
            "ffortran", // NOI18N
            "f95", // NOI18N
            "f90", // NOI18N
            "f77", // NOI18N
        };

        if (list != null && list.length > 0) {
            if (cs == null) {
                CompilerFlavor flavor = CompilerSet.getCompilerSetFlavor(dir.getAbsolutePath(), list);
                cs = getCompilerSet(flavor);
                if (cs != null && !cs.getDirectory().equals(path))
                    return;
                cs = CompilerSet.getCompilerSet(dir.getAbsolutePath(), list);
                add(cs);
        //            for (String name : list) {
        //                File file = new File(dir, name);
        //                if (file.exists()) {
        //                    for (int i = 0; i < best.length; i++) {
        //                        if (name.equals(best[i]) || name.equals(best[i] + ".exe")) { // NOI18N
        //                            cs.addTool(name, path, kind);
        //                        }
        //                    }
        //                }
        //            }
            }
            for (int i = 0; i < best.length; i++) {
                String name = best[i];
                if (isWindows()) {
                    name = name + ".exe"; // NOI18N
                }
                File file = new File(dir, name);
                if (file.exists() && !file.isDirectory()) {
                    cs.addTool(hkey, name, file.getAbsolutePath(), kind);
                    return;
                }
            }
        }
    }
    
    private void initMakeTool(CompilerFilenameFilter filter, int kind, String path, CompilerSet cs) {
        File dir = new File(path);
        String[] list = dir.list(filter);
        String[] best = {
            "dmake", // NOI18N
            "gmake", // NOI18N
            "make", // NOI18N
        };

        if (list != null && list.length > 0) {
            if (cs == null) {
                cs = null;
                if (path.contains("msys")) { // NOI18N
                    // use minGW cs
                    cs = getCompilerSet(CompilerFlavor.MinGW);
                }
                if (cs == null) {
                    CompilerFlavor flavor = CompilerSet.getCompilerSetFlavor(dir.getAbsolutePath(), list);
                    cs = getCompilerSet(flavor);
                    if (cs != null && !cs.getDirectory().equals(path))
                        return;
                    cs = CompilerSet.getCompilerSet(dir.getAbsolutePath(), list);
                        add(cs);
                }
            }
            for (int i = 0; i < best.length; i++) {
                String name = best[i];
                if (isWindows()) {
                    name = name + ".exe"; // NOI18N
                }
                File file = new File(dir, name);
                if (file.exists() && !file.isDirectory()) { // NOI18N
                    cs.addTool(hkey, name, file.getAbsolutePath(), kind);
                    return;
                }
            }
        }
    }
    
    private void initDebuggerTool(CompilerFilenameFilter filter, int kind, String path, CompilerSet cs) {
        File dir = new File(path);
        String[] list = dir.list(filter);
        String[] best;
        if (IpeUtils.isGdbEnabled()) {
            best = new String[] {
                "gdb", // NOI18N
            };
        }
        else {
            // Assume dbxgui
            best = new String[] {
                "dbx", // NOI18N
            };
        }

        if (list != null && list.length > 0) {
            if (cs == null) {
                CompilerFlavor flavor = CompilerSet.getCompilerSetFlavor(dir.getAbsolutePath(), list);
                cs = getCompilerSet(flavor);
                if (cs != null && !cs.getDirectory().equals(path))
                    return;
                cs = CompilerSet.getCompilerSet(dir.getAbsolutePath(), list);
                add(cs);
            }
            for (int i = 0; i < best.length; i++) {
                String name = best[i];
                if (isWindows()) {
                    name = name + ".exe"; // NOI18N
                }
                File file = new File(dir, name);
                if (file.exists() && !file.isDirectory()) { // NOI18N
                    cs.addTool(hkey, name, file.getAbsolutePath(), kind);
                    return;
                }
            }
        }
    }
    
    /**
     * If a compiler set doesn't have one of each compiler types, add a "No compiler"
     * tool. If selected, this will tell the build validation things are OK.
     */
    private void completeCompilerSets() {
        CompilerSet sun = getCompilerSet(CompilerFlavor.Sun);
        if (sun == null) {
            // find 'best' Sun set and copy it
            sun = getCompilerSet(CompilerFlavor.Sun12);
            if (sun == null)
                sun = getCompilerSet(CompilerFlavor.Sun11);
            if (sun == null)
                sun = getCompilerSet(CompilerFlavor.Sun10);
            if (sun == null)
                sun = getCompilerSet(CompilerFlavor.Sun9);
            if (sun == null)
                sun = getCompilerSet(CompilerFlavor.Sun8);
            if (sun != null) {
                sun = sun.createCopy();
                sun.setName(CompilerFlavor.Sun.toString());
                sun.setFlavor(CompilerFlavor.Sun);
                sun.setAsDefault(false);
                sun.setAutoGenerated(true);
                add(sun);
            }
        }
        
        for (CompilerSet cs : sets) {
            completeCompilerSet(hkey, cs);
        }
        
        if (sets.size() == 0) { // No compilers found
            add(CompilerSet.createEmptyCompilerSet());
        }
    }
    
    private static void completeCompilerSet(String hkey, CompilerSet cs) {
        if (cs.getTool(Tool.CCompiler) == null) {
            cs.addTool(hkey, "", "", Tool.CCompiler);
        }
        if (cs.getTool(Tool.CCCompiler) == null) {
            cs.addTool(hkey, "", "", Tool.CCCompiler);
        }
        if (cs.getTool(Tool.FortranCompiler) == null) {
            cs.addTool(hkey, "", "", Tool.FortranCompiler);
        }
        if (cs.findTool(Tool.MakeTool) == null) {
            String path = Path.findCommand("make"); // NOI18N
            if (path != null)
                cs.addNewTool(hkey, IpeUtils.getBaseName(path), IpeUtils.getDirName(path), Tool.MakeTool);
        }
        if (cs.getTool(Tool.MakeTool) == null) {
                cs.addTool(hkey, "", "", Tool.MakeTool);
        }
        if (cs.findTool(Tool.DebuggerTool) == null) {
            String path;
            if (IpeUtils.isGdbEnabled()) {
                path = Path.findCommand("gdb"); // NOI18N
            }
            else {
                path = Path.findCommand("dbx"); // NOI18N
            }
            if (path != null)
                cs.addNewTool(hkey, IpeUtils.getBaseName(path), path, Tool.DebuggerTool);
        }
        if (cs.getTool(Tool.DebuggerTool) == null) {
                cs.addTool(hkey, "", "", Tool.DebuggerTool);
        }
        
    }
    
    private void initCompilerFilters() {
        gcc_filter = new CompilerFilenameFilter(gcc_pattern);
        gpp_filter = new CompilerFilenameFilter(gpp_pattern);
        cc_filter = new CompilerFilenameFilter(cc_pattern);
        fortran_filter = new CompilerFilenameFilter(fortran_pattern);
        if (Utilities.isUnix()) {
            CC_filter = new CompilerFilenameFilter(CC_pattern);
        }
        make_filter = new CompilerFilenameFilter(make_pattern);
        debugger_filter = new CompilerFilenameFilter(debugger_pattern);
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
            if (current == idx) {
                current = 0; // switch to 0th
            } else if (current > idx) {
                current--; // adjust accordingly
            }
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
        waitForCompletion();
        for (CompilerSet cs : sets) {
            if (cs.getName().equals(name) && cs.getDisplayName().equals(dname)) {
                return cs;
            }
        }
        return null;
    }

    public CompilerSet getCompilerSet(int idx) {
        waitForCompletion();
        if (idx >= 0 && idx < sets.size())
            return sets.get(idx);
        else
            return null;
    }
    
    public CompilerSet getCurrentCompilerSet() {
        return sets.get(current);
    }
    
    public void setCurrentCompilerSet(int current) {
        this.current = current;
    }
    
    public void setCurrentCompilerSet(String name) {
        int i = 0;
        for (CompilerSet cs : sets) {
            if (cs.getName().equals(name)) {
                current = i;
                getPreferences().putInt(CSM + hkey + CURRENT_SET_NAME, current);
                return;
            }
            i++;
        }
    }
    
    public List<CompilerSet> getCompilerSets() {
        return sets;
    }
    
    public List<String> getCompilerSetDisplayNames() {
        ArrayList<String> names = new ArrayList();
        for (CompilerSet cs : getCompilerSets()) {
            names.add(cs.getDisplayName());
        }
        return names;
    }
    
    public List<String> getCompilerSetNames() {
        ArrayList<String> names = new ArrayList();
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
            if (cs.isDefault())
                return cs;
        }
        return null;
    }
    
    /**
     * Check if the gdb module is enabled. Don't show the gdb line if it isn't.
     *
     * @return true if the gdb module is enabled, false if missing or disabled
     */
    protected boolean isGdbEnabled() {
        Iterator iter = Lookup.getDefault().lookup(new Lookup.Template(ModuleInfo.class)).allInstances().iterator();
        while (iter.hasNext()) {
            ModuleInfo info = (ModuleInfo) iter.next();
            if (info.getCodeNameBase().equals("org.netbeans.modules.cnd.debugger.gdb") && info.isEnabled()) { // NOI18N
                return true;
            }
        }
        return false;
    }
    
    /** Special FilenameFilter which should recognize different variations of supported compilers */
    private class CompilerFilenameFilter implements FilenameFilter {
        
        Pattern pc = null;
        
        public CompilerFilenameFilter(String pattern) {
            try {
                pc = Pattern.compile(pattern);
            } catch (PatternSyntaxException ex) {
            }
        }
        
        public boolean accept(File dir, String name) {
            return pc != null && pc.matcher(name).matches();
        }
    }
    
    private static CompilerProvider getCompilerProvider() {
        if (compilerProvider == null) {
            compilerProvider = (CompilerProvider) Lookup.getDefault().lookup(CompilerProvider.class);
        }
        if (compilerProvider == null) {
            compilerProvider = new DefaultCompilerProvider();
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
        getPreferences().putDouble(CSM + VERSION, csm_version);
        getPreferences().putInt(CSM + hkey + NO_SETS, sets.size());
        getPreferences().putInt(CSM + hkey + CURRENT_SET_NAME, current);
        getPreferences().putInt(CSM + hkey + SET_PLATFORM, platform);
        int setCount = 0;
        for (CompilerSet cs : getCompilerSets()) {
            getPreferences().put(CSM + hkey + SET_NAME + setCount, cs.getName());
            getPreferences().put(CSM + hkey + SET_FLAVOR + setCount, cs.getCompilerFlavor().toString());
            getPreferences().put(CSM + hkey + SET_DIRECTORY + setCount, cs.getDirectory());
            getPreferences().putBoolean(CSM + hkey + SET_AUTO + setCount, cs.isAutoGenerated());
            List<Tool> tools = cs.getTools();
            getPreferences().putInt(CSM + hkey + NO_TOOLS + setCount, tools.size());
            int toolCount = 0;
            for (Tool tool : tools) {
                getPreferences().put(CSM + hkey + TOOL_NAME + setCount+ '.' + toolCount, tool.getName());
                getPreferences().put(CSM + hkey + TOOL_DISPLAYNAME + '-' + setCount+ '.' + toolCount, tool.getDisplayName());
                getPreferences().putInt(CSM + hkey + TOOL_KIND + setCount+ '.' + toolCount, tool.getKind());
                getPreferences().put(CSM + hkey + TOOL_PATH + setCount+ '.' + toolCount, tool.getPath());
                getPreferences().put(CSM + hkey + TOOL_FLAVOR + setCount+ '.' + toolCount, tool.getFlavor().toString());
                toolCount++;
            }
            setCount++;
        }
    }
        
    public static CompilerSetManager restoreFromDisk(String hkey) {
        double version = getPreferences().getDouble(CSM + VERSION, 1.0);
        if (version == 1.0 && hkey.equals(LOCALHOST)) {
            return restoreFromDisk10();
        }
        
        int noSets = getPreferences().getInt(CSM + hkey + NO_SETS, -1);
        if (noSets < 0) {
            return null;
        }
        int current = getPreferences().getInt(CSM + hkey + CURRENT_SET_NAME, 0);
        int pform = getPreferences().getInt(CSM + hkey + SET_PLATFORM, -1);
        if (pform < 0) {
            if (hkey.equals(LOCALHOST)) {
                pform = computeLocalPlatform();
            }
        }
        
        ArrayList<CompilerSet> css = new ArrayList<CompilerSet>();
        for (int setCount = 0; setCount < noSets; setCount++) {
            String setName = getPreferences().get(CSM + hkey + SET_NAME + setCount, null);
            String setFlavorName = getPreferences().get(CSM + hkey + SET_FLAVOR + setCount, null);
            CompilerFlavor flavor = null;
            if (setFlavorName != null) {
                flavor = CompilerFlavor.toFlavor(setFlavorName);
            }
            String setDirectory = getPreferences().get(CSM + hkey + SET_DIRECTORY + setCount, null);
            if (setName == null || setFlavorName == null || flavor == null) {
                // FIXUP: error
                continue;
            }
            Boolean auto = getPreferences().getBoolean(CSM + hkey + SET_AUTO + setCount, false);
            CompilerSet cs = new CompilerSet(flavor, setDirectory, setName);
            cs.setAutoGenerated(auto);
            int noTools = getPreferences().getInt(CSM + hkey + NO_TOOLS + setCount, -1);
            for (int toolCount = 0; toolCount < noTools; toolCount++) {
                String toolName = getPreferences().get(CSM + hkey + TOOL_NAME + setCount + '.' + toolCount, null);
                String toolDisplayName = getPreferences().get(CSM + hkey + TOOL_DISPLAYNAME + '-' + setCount+ '.' + toolCount, null);
                int toolKind = getPreferences().getInt(CSM + hkey + TOOL_KIND + setCount + '.' + toolCount, -1);
                String toolPath = getPreferences().get(CSM + hkey + TOOL_PATH + setCount + '.' + toolCount, null);
                String toolFlavorName = getPreferences().get(CSM + hkey + TOOL_FLAVOR + setCount + '.' + toolCount, null);
                CompilerFlavor toolFlavor = null;
                if (toolFlavorName != null) {
                    toolFlavor = CompilerFlavor.toFlavor(toolFlavorName);
                }
                Tool tool = getCompilerProvider().createCompiler(hkey, toolFlavor, toolKind, "", toolDisplayName, toolPath);
                tool.setName(toolName);
                cs.addTool(tool);
            }
            completeCompilerSet(hkey, cs);
            css.add(cs);
        }
        
        CompilerSetManager csm = new CompilerSetManager(hkey, css, current);
        csm.current = getPreferences().getInt(CSM + hkey + CURRENT_SET_NAME, 0);
        csm.platform = pform;
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
                flavor = CompilerFlavor.toFlavor(setFlavorName);
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
                    toolFlavor = CompilerFlavor.toFlavor(toolFlavorName);
                }
                Tool tool = getCompilerProvider().createCompiler(LOCALHOST, toolFlavor, toolKind, "", toolDisplayName, toolPath);
                tool.setName(toolName);
                cs.addTool(tool);
            }
            completeCompilerSet(CompilerSetManager.LOCALHOST, cs);
            css.add(cs);
        }
        CompilerSetManager csm = new CompilerSetManager(LOCALHOST, css, 0);
        csm.platform = computeLocalPlatform();
        return csm;
    }
    
    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(CompilerSetManager.class, s);
    }

    private boolean isWindows() {
        return platform == PLATFORM_WINDOWS;
    }
    

    public static boolean useFakeRemoteCompilerSet = Boolean.getBoolean("cnd.remote.fakeCompilerSet");
    public static CompilerSet fakeRemoteCS = new FakeRemoteCompilerSet();
    
    private static class FakeRemoteCompilerSet extends CompilerSet {

        @Override
        public String getName() {
            return "fakeRemote";
        }

        @Override
        public Tool getTool(int kind) {
            switch (kind) {
                case Tool.MakeTool: return fakeMake;
                case Tool.CCompiler: return fakeC;
                case Tool.CCCompiler: return fakeCC;
                case Tool.FortranCompiler: return fakeFortran;
            }
            return null;
        }

        @Override
        public Tool getTool(String name) {
            throw new UnsupportedOperationException();
        }
        
        private Tool fakeMake = new Tool("fake", CompilerFlavor.GNU, Tool.MakeTool, "", "fakeMake", "/usr/sfw/bin/gmake"); 
        private Tool fakeC = new Tool("fake", CompilerFlavor.GNU, Tool.CCompiler, "", "fakeGcc", "/usr/sfw/bin/gcc"); 
        private Tool fakeCC = new Tool("fake", CompilerFlavor.GNU, Tool.CCCompiler, "", "fakeG++", "/usr/sfw/bin/g++"); 
        private Tool fakeFortran = new Tool("fake", CompilerFlavor.GNU, Tool.FortranCompiler, "", "veryFakeFortran", "/usr/sfw/bin/g++"); 
        
    }
}
