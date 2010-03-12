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

package org.netbeans.modules.cnd.dwarfdiscovery.provider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProvider;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.discovery.api.DiscoveryUtils;
import org.netbeans.modules.cnd.discovery.api.Progress;
import org.netbeans.modules.cnd.discovery.api.ProviderProperty;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnit;
import org.netbeans.modules.cnd.dwarfdump.Dwarf;
import org.netbeans.modules.cnd.dwarfdump.dwarfconsts.LANG;
import org.netbeans.modules.cnd.dwarfdump.exception.WrongFileFormatException;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
public abstract class BaseDwarfProvider implements DiscoveryProvider {
    
    private static final boolean TRACE_READ_EXCEPTIONS = Boolean.getBoolean("cnd.dwarfdiscovery.trace.read.errors"); // NOI18N
    private static final boolean FULL_TRACE = Boolean.getBoolean("cnd.dwarfdiscovery.trace.read.source"); // NOI18N
    public static final String RESTRICT_SOURCE_ROOT = "restrict_source_root"; // NOI18N
    public static final String RESTRICT_COMPILE_ROOT = "restrict_compile_root"; // NOI18N
    protected AtomicBoolean isStoped = new AtomicBoolean(false);
    
    public BaseDwarfProvider() {
    }
    
    @Override
    public boolean isApplicable(ProjectProxy project) {
        return true;
    }
    
    @Override
    public void stop() {
        isStoped.set(true);
    }

    protected List<SourceFileProperties> getSourceFileProperties(String[] objFileName, Progress progress, ProjectProxy project){
        CountDownLatch countDownLatch = new CountDownLatch(objFileName.length);
        RequestProcessor rp = new RequestProcessor("Parallel analyzing", CndUtils.getNumberCndWorkerThreads()); // NOI18N
        try{
            Map<String,SourceFileProperties> map = new ConcurrentHashMap<String,SourceFileProperties>();
            for (String file : objFileName) {
                MyRunnable r = new MyRunnable(countDownLatch, file, map, progress, project);
                rp.post(r);
            }
            try {
                countDownLatch.await();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            List<SourceFileProperties> list = new ArrayList<SourceFileProperties>();
            list.addAll(map.values());
            return list;
        } finally {
            PathCache.dispose();
            grepBase.clear();
            grepBase = new ConcurrentHashMap<String, GrepEntry>();
            getCommpilerSettings().dispose();
        }
    }

    private boolean processObjectFile(String file, Map<String, SourceFileProperties> map, Progress progress, ProjectProxy project) {
        if (isStoped.get()) {
            return true;
        }
        String restrictSourceRoot = null;
        ProviderProperty p = getProperty(RESTRICT_SOURCE_ROOT);
        if (p != null) {
            String s = (String) p.getValue();
            if (s.length() > 0) {
                restrictSourceRoot = CndFileUtils.normalizeFile(new File(s)).getAbsolutePath();
            }
        }
        String restrictCompileRoot = null;
        p = getProperty(RESTRICT_COMPILE_ROOT);
        if (p != null) {
            String s = (String) p.getValue();
            if (s.length() > 0) {
                restrictCompileRoot = CndFileUtils.normalizeFile(new File(s)).getAbsolutePath();
            }
        }
        for (SourceFileProperties f : getSourceFileProperties(file, map, project)) {
            if (isStoped.get()) {
                break;
            }
            String name = f.getItemPath();
            if (name == null) {
                continue;
            }
            if (restrictSourceRoot != null) {
                if (!name.startsWith(restrictSourceRoot)) {
                    continue;
                }
            }
            if (restrictCompileRoot != null) {
                if (f.getCompilePath() != null && !f.getCompilePath().startsWith(restrictCompileRoot)) {
                    continue;
                }
            }
            if (new File(name).exists()) {
                SourceFileProperties existed = map.get(name);
                if (existed == null) {
                    map.put(name, f);
                } else {
                    // Duplicated
                    if (existed.getUserInludePaths().size() < f.getUserInludePaths().size()) {
                        map.put(name, f);
                    }
                }
            } else {
                if (FULL_TRACE) {
                    System.out.println("Not Exist " + name); // NOI18N
                } //NOI18N
            }
        }
        if (progress != null) {
            synchronized(progress) {
                progress.increment();
            }
        }
        return false;
    }
    
    protected int sizeComilationUnit(String objFileName){
        int res = 0;
        Dwarf dump = null;
        try{
            dump = new Dwarf(objFileName);
            Iterator<CompilationUnit> iterator = dump.iteratorCompilationUnits();
            while (iterator.hasNext()) {
                CompilationUnit cu = iterator.next();
                if (cu != null) {
                    if (cu.getRoot() == null || cu.getSourceFileName() == null) {
                        continue;
                    }
                    String lang = cu.getSourceLanguage();
                    if (lang == null) {
                        continue;
                    }
                    if (LANG.DW_LANG_C.toString().equals(lang) ||
                            LANG.DW_LANG_C89.toString().equals(lang) ||
                            LANG.DW_LANG_C99.toString().equals(lang)) {
                        res++;
                    } else if (LANG.DW_LANG_C_plus_plus.toString().equals(lang)) {
                        res++;
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            // Skip Exception
        } catch (WrongFileFormatException ex) {
            // Skip Exception
        } catch (IOException ex) {
            // Skip Exception
        } catch (Exception ex) {
            // Skip Exception
        } finally {
            if (dump != null) {
                dump.dispose();
            }
        }
        return res;
    }
    
    protected List<SourceFileProperties> getSourceFileProperties(String objFileName, Map<String, SourceFileProperties> map, ProjectProxy project) {
        List<SourceFileProperties> list = new ArrayList<SourceFileProperties>();
        Dwarf dump = null;
        try {
            if (FULL_TRACE) {
                System.out.println("Process file " + objFileName);  // NOI18N
            }
            dump = new Dwarf(objFileName);
            Iterator<CompilationUnit> iterator = dump.iteratorCompilationUnits();
            while (iterator.hasNext()) {
                CompilationUnit cu = iterator.next();
                if (cu != null) {
                    if (isStoped.get()) {
                        break;
                    }
                    if (cu.getRoot() == null || cu.getSourceFileName() == null) {
                        if (TRACE_READ_EXCEPTIONS) {
                            System.out.println("Compilation unit has broken name in file " + objFileName);  // NOI18N
                        }
                        continue;
                    }
                    String lang = cu.getSourceLanguage();
                    if (lang == null) {
                        if (TRACE_READ_EXCEPTIONS) {
                            System.out.println("Compilation unit has unresolved language in file " + objFileName + "for " + cu.getSourceFileName());  // NOI18N
                        }
                        continue;
                    }
                    DwarfSource source = null;
                    if (LANG.DW_LANG_C.toString().equals(lang)
                            || LANG.DW_LANG_C89.toString().equals(lang)
                            || LANG.DW_LANG_C99.toString().equals(lang)) {
                        source = new DwarfSource(cu, false, getCommpilerSettings(), grepBase);
                    } else if (LANG.DW_LANG_C_plus_plus.toString().equals(lang)) {
                        source = new DwarfSource(cu, true, getCommpilerSettings(), grepBase);
                    } else {
                        if (FULL_TRACE) {
                            System.out.println("Unknown language: " + lang);  // NOI18N
                        }
                        // Ignore other languages
                    }
                    if (source != null) {
                        String name = source.getItemPath();
                        SourceFileProperties old = map.get(name);
                        if (old != null && old.getUserInludePaths().size() > 0) {
                            if (FULL_TRACE) {
                                System.out.println("Compilation unit already exist. Skip " + name);  // NOI18N
                            }
                            // do not process processed item
                            continue;
                        }
                        source.process(cu);
                        if (source.getCompilePath() == null) {
                            if (TRACE_READ_EXCEPTIONS) {
                                System.out.println("Compilation unit has NULL compile path in file " + objFileName);  // NOI18N
                            }
                            continue;
                        }
                        list.add(source);
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            // Skip Exception
            if (TRACE_READ_EXCEPTIONS) {
                System.out.println("File not found " + objFileName + ": " + ex.getMessage());  // NOI18N
            }
        } catch (WrongFileFormatException ex) {
            if (TRACE_READ_EXCEPTIONS) {
                System.out.println("Unsuported format of file " + objFileName + ": " + ex.getMessage());  // NOI18N
            }
        } catch (IOException ex) {
            if (TRACE_READ_EXCEPTIONS) {
                System.err.println("Exception in file " + objFileName);  // NOI18N
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            if (TRACE_READ_EXCEPTIONS) {
                System.err.println("Exception in file " + objFileName);  // NOI18N
                ex.printStackTrace();
            }
        } finally {
            if (dump != null) {
                dump.dispose();
            }
        }
        return list;
    }

    private Map<String,GrepEntry> grepBase = new ConcurrentHashMap<String, GrepEntry>();
    
    public CompilerSettings getCommpilerSettings(){
        return myCommpilerSettings;
    }
    
    public void setCommpilerSettings(ProjectProxy project) {
        myCommpilerSettings = new CompilerSettings(project);
    }
    private CompilerSettings myCommpilerSettings;

    public static class GrepEntry {
        List<String> includes = new ArrayList<String>();
        String firstMacro = null;
        int firstMacroLine = -1;
    }

    public static class CompilerSettings{
        private final List<String> systemIncludePathsC;
        private final List<String> systemIncludePathsCpp;
        private final Map<String,String> systemMacroDefinitionsC;
        private final Map<String,String> systemMacroDefinitionsCpp;
        private Map<String,String> normalizedPaths = new ConcurrentHashMap<String, String>();
        private final String compileFlavor;
        private final String cygwinDriveDirectory;
        private final boolean isWindows;
        
        public CompilerSettings(ProjectProxy project){
            systemIncludePathsCpp = DiscoveryUtils.getSystemIncludePaths(project, true);
            systemIncludePathsC = DiscoveryUtils.getSystemIncludePaths(project,false);
            systemMacroDefinitionsCpp = DiscoveryUtils.getSystemMacroDefinitions(project, true);
            systemMacroDefinitionsC = DiscoveryUtils.getSystemMacroDefinitions(project,false);
            compileFlavor = DiscoveryUtils.getCompilerFlavor(project);
            isWindows = Utilities.isWindows();
            if (isWindows) {
                cygwinDriveDirectory = DiscoveryUtils.getCygwinDrive(project);
            } else {
                cygwinDriveDirectory = null;
            }
        }
        
        public List<String> getSystemIncludePaths(boolean isCPP) {
            if (isCPP) {
                return systemIncludePathsCpp;
            } else {
                return systemIncludePathsC;
            }
        }
        
        public Map<String,String> getSystemMacroDefinitions(boolean isCPP) {
            if (isCPP) {
                return systemMacroDefinitionsCpp;
            } else {
                return systemMacroDefinitionsC;
            }
        }
        
        public String getNormalizedPath(String path){
            String res = normalizedPaths.get(path);
            if (res == null) {
                res = normalizePath(path);
                normalizedPaths.put(path,res);
            }
            return res;
        }

        protected String normalizePath(String path){
            path = CndFileUtils.normalizeFile(new File(path)).getAbsolutePath();
            if (Utilities.isWindows()) {
                path = path.replace('\\', '/');
            }
            return path;
        }

        public String getCompileFlavor() {
            return compileFlavor;
        }

        public String getCygwinDrive() {
            return cygwinDriveDirectory;
        }

        public boolean isWindows(){
            return isWindows;
        }

        private void dispose(){
            systemIncludePathsC.clear();
            systemIncludePathsCpp.clear();
            systemMacroDefinitionsC.clear();
            systemMacroDefinitionsCpp.clear();
            normalizedPaths.clear();
            normalizedPaths = new ConcurrentHashMap<String, String>();
        }
    }

    private class MyRunnable implements Runnable {
        private String file;
        private Map<String, SourceFileProperties> map;
        private Progress progress;
        private CountDownLatch countDownLatch;
        private ProjectProxy project;

        private MyRunnable(CountDownLatch countDownLatch, String file, Map<String, SourceFileProperties> map, Progress progress, ProjectProxy project){
            this.file = file;
            this.map = map;
            this.progress = progress;
            this.countDownLatch = countDownLatch;
            this.project = project;
        }
        @Override
        public void run() {
            try {
                if (!isStoped.get()) {
                    Thread.currentThread().setName("Parallel analyzing "+file); // NOI18N
                    processObjectFile(file, map, progress, project);
                }
            } finally {
                countDownLatch.countDown();
            }
        }
    }
}
