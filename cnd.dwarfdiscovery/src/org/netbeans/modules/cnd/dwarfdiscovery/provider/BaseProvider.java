/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.dwarfdiscovery.provider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProvider;
import org.netbeans.modules.cnd.discovery.api.Progress;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.discovery.api.ProviderProperty;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.netbeans.modules.cnd.discovery.wizard.api.support.ProjectBridge;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Alexander Simon
 */
public abstract class BaseProvider implements DiscoveryProvider {
    
    public static final String RESTRICT_SOURCE_ROOT = "restrict_source_root"; // NOI18N
    public static final String RESTRICT_COMPILE_ROOT = "restrict_compile_root"; // NOI18N
    private final AtomicBoolean isStoped = new AtomicBoolean(false);
    private final Interrupter stopIterrupter;
    private Interrupter projectInterrupter;
    private RelocatablePathMapperImpl mapper;
    private CompilerSettings myCommpilerSettings;

    public BaseProvider() {
        stopIterrupter = new Interrupter() {

            @Override
            public boolean cancelled() {
                if (isStoped.get()) {
                    return true;
                }
                Interrupter aProjectInterrupter = projectInterrupter;
                if (aProjectInterrupter != null && aProjectInterrupter.cancelled()) {
                    return true;
                }
                return false;
            }
        };
    }
    
    public final void init(ProjectProxy project) {
        myCommpilerSettings = new CompilerSettings(project);
        mapper = new RelocatablePathMapperImpl(project);
    }

    public final void store(ProjectProxy project) {
        mapper.save();
    }

    @Override
    public boolean isApplicable(ProjectProxy project) {
        return true;
    }
    
    @Override
    public final boolean cancel() {
        isStoped.set(true);
        return true;
    }

    protected final void resetStopInterrupter(Interrupter projectInterrupter) {
        this.projectInterrupter = projectInterrupter;
        isStoped.set(false);
    }

    protected final Interrupter getStopInterrupter() {
        return stopIterrupter;
    }
    
    protected final FileSystem getFileSystem(ProjectProxy project) {
        if (project != null) {
            Project p = project.getProject();
            if (p != null) {                
                return RemoteFileUtil.getProjectSourceFileSystem(p);
            }
        }
        return CndFileUtils.getLocalFileSystem();
    }
    
    protected final RelocatablePathMapper getRelocatablePathMapper() {
        return mapper;
    }

    protected final FileObject resolvePath(ProjectProxy project, String buildArtifact, final FileSystem fileSystem, SourceFileProperties f, String name) {
        FileObject fo = fileSystem.findResource(name);
        if (!(f instanceof Relocatable)) {
            return fo;
        }
        RelocatablePathMapper.FS fs = new FSImpl(fileSystem);
        String sourceRoot = null;
        if (project != null) {
            sourceRoot = project.getSourceRoot();
            if (sourceRoot != null && sourceRoot.length() < 2) {
                sourceRoot = null;
            }
        }
        if (sourceRoot == null) {
            sourceRoot = PathUtilities.getDirName(buildArtifact);
            if (sourceRoot != null && sourceRoot.length() < 2) {
                sourceRoot = null;
            }
        }
        if (sourceRoot != null) {
            sourceRoot = sourceRoot.replace('\\', '/');
        }
        if (fo == null || !fo.isValid()) {
            RelocatablePathMapper.ResolvedPath resolvedPath = mapper.getPath(name);
            if (resolvedPath == null) {
                if (sourceRoot != null) {
                    if (mapper.discover(fs, sourceRoot, name)) {
                        resolvedPath = mapper.getPath(name);
                        fo = fileSystem.findResource(resolvedPath.getPath());
                        if (fo != null && fo.isValid() && fo.isData()) {
                            ((Relocatable) f).resetItemPath(resolvedPath, mapper, fs);
                            return fo;
                        }
                    }
                }
            } else {
                fo = fileSystem.findResource(resolvedPath.getPath());
                if (fo != null && fo.isValid() && fo.isData()) {
                    ((Relocatable) f).resetItemPath(resolvedPath, mapper, fs);
                    return fo;
                }
            }
        }
        if (fo != null && fo.isData()) {
            name = fo.getPath();
            RelocatablePathMapper.ResolvedPath resolvedPath = mapper.getPath(name);
            if (resolvedPath == null) {
                if (sourceRoot != null) {
                    if (!name.startsWith(sourceRoot)) {
                        if (mapper.discover(fs, sourceRoot, name)) {
                            resolvedPath = mapper.getPath(name);
                            FileObject resolved = fileSystem.findResource(resolvedPath.getPath());
                            if (resolved != null && resolved.isValid() && resolved.isData()) {
                                ((Relocatable) f).resetItemPath(resolvedPath, mapper, fs);
                                return resolved;
                            }
                        }
                    }
                }
            } else {
                FileObject resolved = fileSystem.findResource(resolvedPath.getPath());
                if (resolved != null && resolved.isValid() && resolved.isData()) {
                    ((Relocatable) f).resetItemPath(resolvedPath, mapper, fs);
                    return resolved;
                }
            }
            sourceRoot = null;
            if (project != null) {
                sourceRoot = project.getSourceRoot();
                if (sourceRoot != null && sourceRoot.length() < 2) {
                    sourceRoot = null;
                }
            }
            if (sourceRoot == null) {
                sourceRoot = PathUtilities.getBaseName(name);
                if (sourceRoot != null && sourceRoot.length() < 2) {
                    sourceRoot = null;
                }
            }
            if (sourceRoot != null) {
                ((Relocatable) f).resolveIncludePaths(sourceRoot, mapper, fs);
            }
            return fo;
        }
        return null;
    }
    
    abstract protected List<SourceFileProperties> getSourceFileProperties(String objFileName, Map<String, SourceFileProperties> map, ProjectProxy project, Set<String> dlls, List<String> buildArtifacts, CompileLineStorage storage);
    
    protected void before() {
    }

    protected void after() {
    }
    
    protected final List<SourceFileProperties> getSourceFileProperties(String[] objFileName, Progress progress, ProjectProxy project,
            Set<String> dlls, List<String> buildArtifacts, CompileLineStorage storage){
        try{
            before();
            Map<String,SourceFileProperties> map = new ConcurrentHashMap<String,SourceFileProperties>();
            if (objFileName.length == 1) {
                try {
                    processObjectFile(objFileName[0], map, progress, project, dlls, buildArtifacts, storage);
                } catch (Throwable ex) {
                    ex.printStackTrace(System.err);
                }
            } else {
                CountDownLatch countDownLatch = new CountDownLatch(objFileName.length);
                RequestProcessor rp = new RequestProcessor("Parallel analyzing", CndUtils.getNumberCndWorkerThreads()); // NOI18N
                for (String file : objFileName) {
                    MyRunnable r = new MyRunnable(countDownLatch, file, map, progress, project, dlls, buildArtifacts, storage);
                    rp.post(r);
                }
                try {
                    countDownLatch.await();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            List<SourceFileProperties> list = new ArrayList<SourceFileProperties>();
            list.addAll(map.values());
            return list;
        } finally {
            PathCache.dispose();
            getCommpilerSettings().dispose();
            after();
        }
    }
    
    private boolean processObjectFile(String file, Map<String, SourceFileProperties> map, Progress progress, ProjectProxy project, Set<String> dlls, List<String> buildArtifacts, CompileLineStorage storage) {
        if (isStoped.get()) {
            return true;
        }
        ProjectBridge bridge = null;
        if (project.getProject() != null) {
            bridge = new ProjectBridge(project.getProject());
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
        FileSystem fileSystem  = getFileSystem(project);
        for (SourceFileProperties f : getSourceFileProperties(file, map, project, dlls, buildArtifacts, storage)) {
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
            FileObject fo = resolvePath(project, file, fileSystem, f, name);
            if (fo == null) {
                if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                    DwarfSource.LOG.log(Level.FINE, "Not Exist {0}", name); // NOI18N
                }
                continue;
            }
            boolean skip = false;
            if (restrictCompileRoot != null) {
                if (f.getCompilePath() != null && !f.getCompilePath().startsWith(restrictCompileRoot)) {
                    skip = true;
                    if (bridge != null) {
                        String relPath = bridge.getRelativepath(fo.getPath());
                        Item item = bridge.getProjectItem(relPath);
                        if (item != null) {
                            skip = false;
                        }
                    }
                }
            }
            if (skip) {
                if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                    DwarfSource.LOG.log(Level.FINE, "Skiped {0}", name); // NOI18N
                }
                continue;
            }

            name = fo.getPath();
            SourceFileProperties existed = map.get(name);
            if (existed == null) {
                map.put(name, f);
            } else {
                // Duplicated
                if (existed.getUserInludePaths().size() < f.getUserInludePaths().size()) {
                    map.put(name, f);
                } else if (existed.getUserInludePaths().size() == f.getUserInludePaths().size()) {
                    if (existed.getUserMacros().size() < f.getUserMacros().size()) {
                        map.put(name, f);
                    } else if (existed.getUserMacros().size() == f.getUserMacros().size()) {
                        if (macrosWeight(existed) < macrosWeight(f)) {
                            map.put(name, f);
                        } else {
                            // ignore
                        }
                    } else {
                        // ignore
                    }
                } else {
                    // ignore
                }
            }
        }
        if (progress != null) {
            synchronized(progress) {
                progress.increment(file);
            }
        }
        return false;
    }
    
    private int macrosWeight(SourceFileProperties f) {
        int sum = 0;
        for(String m : f.getUserMacros().keySet()) {
            for(int i = 0; i < m.length(); i++) {
                sum += m.charAt(i);
            }
        }
        return sum;
    }
        
    public final CompilerSettings getCommpilerSettings(){
        return myCommpilerSettings;
    }   
    
    private class MyRunnable implements Runnable {
        private final String file;
        private final Map<String, SourceFileProperties> map;
        private final Progress progress;
        private final CountDownLatch countDownLatch;
        private final ProjectProxy project;
        private final Set<String> dlls;
        private final List<String> buildArtifacts;
        private final CompileLineStorage storage;

        private MyRunnable(CountDownLatch countDownLatch, String file, Map<String, SourceFileProperties> map, Progress progress, ProjectProxy project,
                Set<String> dlls, List<String> buildArtifacts, CompileLineStorage storage){
            this.file = file;
            this.map = map;
            this.progress = progress;
            this.countDownLatch = countDownLatch;
            this.project = project;
            this.dlls = dlls;
            this.buildArtifacts = buildArtifacts;
            this.storage = storage;
        }
        @Override
        public void run() {
            try {
                if (!isStoped.get()) {
                    Thread.currentThread().setName("Parallel analyzing "+file); // NOI18N
                    processObjectFile(file, map, progress, project, dlls, buildArtifacts, storage);
                }
            } finally {
                countDownLatch.countDown();
            }
        }
    }
    
}
