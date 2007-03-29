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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.api.compilers;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.netbeans.modules.cnd.api.compilers.CompilerSet.CompilerFlavor;
import org.netbeans.modules.cnd.api.utils.Path;
import org.netbeans.modules.cnd.settings.CppSettings;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 * Manage a set of CompilerSets. The CompilerSets are dynamically created based on which compilers
 * are found in the user's $PATH variable.
 */
public class CompilerSetManager {
    
    private static final String gcc_pattern = "([a-zA-z][a-zA-Z0-9_]*-)*gcc(([-.]\\d){2,4})?(\\.exe)?"; // NOI18N
    private static final String gpp_pattern = "([a-zA-z][a-zA-Z0-9_]*-)*g\\+\\+(([-.]\\d){2,4})?(\\.exe)?$"; // NOI18N
    private static final String cc_pattern = "([a-zA-z][a-zA-Z0-9_]*-)*cc(([-.]\\d){2,4})?(\\.exe)?$"; // NOI18N
    private static final String CC_pattern = "([a-zA-z][a-zA-Z0-9_]*-)*cc(([-.]\\d){2,4})?(\\.exe)?$";
    private static final String fortran_pattern = "([a-zA-z][a-zA-Z0-9_]*-)*[fg](77|90|95)(([-.]\\d){2,4})?(\\.exe)?"; // NOI18N
    
    private CompilerFilenameFilter gcc_filter;
    private CompilerFilenameFilter gpp_filter;
    private CompilerFilenameFilter cc_filter;
    private CompilerFilenameFilter CC_filter;
    private CompilerFilenameFilter fortran_filter;
    
    private ArrayList<CompilerSet> sets = new ArrayList();
    private ArrayList<String> dirlist;
    
    private CompilerFlavor defaultCompilerFlavor = CompilerFlavor.Unknown;
    
    private static CompilerSetManager instance = null;
    
    public CompilerSetManager() {
        dirlist = Path.getPath();
        initCompilerFilters();
        initCompilerSets();
    }
    
    public CompilerSetManager(ArrayList dirlist) {
        this.dirlist = dirlist;
        initCompilerFilters();
        initCompilerSets();
    }
    
    public static CompilerSetManager getDefault() {
        if (instance == null) {
            instance = new CompilerSetManager();
        }
        return instance;
    }
    
    /**
     * Replace the default CompilerSetManager. Let registered listeners know its been updated.
     */
    public static void setDefault(CompilerSetManager csm) {
        instance = csm;
    }
    
    /** Search $PATH for all desired compiler sets and initialize cbCompilerSet and spCompilerSets */
    private void initCompilerSets() {
        
        for (String path : dirlist) {
            File dir = new File(path);
            if (dir.isDirectory()) {
                initCompiler(gcc_filter, Tool.CCompiler, path);
                initCompiler(gpp_filter, Tool.CCCompiler, path);
                initCompiler(cc_filter, Tool.CCompiler, path);
                if (Utilities.isUnix()) {  // CC and cc are the same on Windows, so skip this step on Windows
                    initCompiler(CC_filter, Tool.CCCompiler, path);
                }
                if (isFortranEnabled()) {
                    initCompiler(fortran_filter, Tool.FortranCompiler, path);
                }
            }
        }
    }
    
    private void initCompiler(CompilerFilenameFilter filter, int kind, String path) {
            File dir = new File(path);
            String[] list = dir.list(filter);
            
            if (list != null && list.length > 0) {
                CompilerSet cs = CompilerSet.getCompilerSet(dir.getAbsolutePath(), list);
                add(cs); // register the CompilerSet with the CompilerSetManager
                for (String name : list) {
                    File file = new File(dir, name);
                    if (file.exists()) {
                        cs.addTool(name, path, kind);
                    }
                }
            }
    }
    
    private void initCompilerFilters() {
        gcc_filter = new CompilerFilenameFilter(gcc_pattern);
        gpp_filter = new CompilerFilenameFilter(gpp_pattern);
        cc_filter = new CompilerFilenameFilter(cc_pattern);
        if (Utilities.isUnix()) {
            CC_filter = new CompilerFilenameFilter(CC_pattern);
        }
    }
    
    /**
     * Add a CompilerSet to this CompilerSetManager. Make sure it doesn't get added multiple times.
     *
     * @param cs The CompilerSet to (possibly) add
     */
    public void add(CompilerSet cs) {
        
        if (sets.isEmpty()) {
            // Use the 1st CompilerSet created (ie, first in the user's PATTH) as default
            defaultCompilerFlavor = cs.getCompilerFlavor();
        }
        if (!sets.contains(cs)) {
            sets.add(cs);
        }
    }
    
    public CompilerSet getCompilerSet(String name) {
        for (CompilerSet cs : sets) {
            if (cs.getName().equals(name)) {
                return cs;
            }
        }
        return sets.size() > 0 ? sets.get(0) : null;
    }
    
    public CompilerSet getCompilerSet(int cs) {
        assert cs <= sets.size();
        return sets.get(cs);
    }
    
    public List<CompilerSet> getCompilerSets() {
        return sets;
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
            if (info.getCodeNameBase().equals("org.netbeans.modules.cnd.debugger.gdb") && info.isEnabled()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if the gdb module is enabled. Don't show the gdb line if it isn't.
     *
     * @return true if the gdb module is enabled, false if missing or disabled
     */
    protected boolean isFortranEnabled() {
        return CppSettings.getDefault().isFortranEnabled();
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
}
