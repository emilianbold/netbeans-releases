/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.truffle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.debugger.jpda.truffle.access.CurrentPCInfo;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import org.netbeans.modules.debugger.jpda.truffle.source.Source;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martin
 */
@SourcePathProvider.Registration(path = "netbeans-JPDASession")
public class FirstSourceURLProvider extends SourcePathProvider {
    
    private static final String[] NO_SOURCE_ROOTS = new String[]{};
    
    //private static final String pathPrefix = "jdk/nashorn/internal/scripts/";   // NOI18N
    private static final String TRUFFLE_ACCESSOR_CLASS_NAME = "org.netbeans.modules.debugger.jpda.backend.truffle.JPDATruffleAccessor"; // NOI18N
    private static final String TRUFFLE_ACCESSOR_PATH = "org/netbeans/modules/debugger/jpda/backend/truffle/JPDATruffleAccessor"; // NOI18N
    
    private final ContextProvider contextProvider;
    private final JPDADebugger debugger;
    private SourcePathProvider sourcePath;
    private Set<FileObject> rootDirs;
    private final Object rootDirsLock = new Object();
    
    public FirstSourceURLProvider(ContextProvider contextProvider) {
        this.contextProvider = contextProvider;
        debugger = contextProvider.lookupFirst(null, JPDADebugger.class);
    }

    @Override
    public String getURL(String relativePath, boolean global) {
        if ("com/oracle/truffle/api/vm/PolyglotEngine.java".equals(relativePath)) {
        //if (TRUFFLE_ACCESSOR_PATH.equals(relativePath)) {
            CurrentPCInfo currentPCInfo = TruffleAccess.getCurrentPCInfo(debugger);
            if (currentPCInfo != null) {
                return currentPCInfo.getSourcePosition().getSource().getUrl().toExternalForm();
            }
        }
        /*
        if (relativePath.startsWith(pathPrefix)) {
            relativePath = relativePath.substring(pathPrefix.length());
            synchronized (rootDirsLock) {
                if (rootDirs == null) {
                    sourcePath = getSourcePathProvider();
                    sourcePath.addPropertyChangeListener(new SourcePathListener());
                    rootDirs = computeModuleRoots();
                }
                for (FileObject root : rootDirs) {
                    FileObject fo = root.getFileObject(relativePath);
                    if (fo != null) {
                        return fo.toURL().toExternalForm();
                    }
                }
            }
        }
        */
        return null;
    }
    
    public String getURL(JPDAClassType clazz, String stratum) {
        //if (TRUFFLE_ACCESSOR_CLASS_NAME.equals(clazz.getName())) {
        if ("com.oracle.truffle.api.vm.PolyglotEngine".equals(clazz.getName())) {
            CurrentPCInfo currentPCInfo = TruffleAccess.getCurrentPCInfo(debugger);
            if (currentPCInfo != null) {
                Source source = currentPCInfo.getSourcePosition().getSource();
                if (source != null) {
                    URL url = source.getUrl();
                    if (url != null) {
                        return url.toExternalForm();
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String getRelativePath(String url, char directorySeparator, boolean includeExtension) {
        return null;
    }

    @Override
    public String[] getSourceRoots() {
        return NO_SOURCE_ROOTS;
    }

    @Override
    public void setSourceRoots(String[] sourceRoots) {
    }

    @Override
    public String[] getOriginalSourceRoots() {
        return NO_SOURCE_ROOTS;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
    }
    
    private SourcePathProvider getSourcePathProvider() {
        List<? extends SourcePathProvider> spps = contextProvider.lookup(null, SourcePathProvider.class);
        for (SourcePathProvider spp : spps) {
            if ("SourcePathProviderImpl".equals(spp.getClass().getSimpleName())) {
                return spp;
            }
        }
        throw new RuntimeException("No SourcePathProviderImpl");
    }
    
    private Set<FileObject> computeModuleRoots() {
        Set<FileObject> projectDirectories = new LinkedHashSet<>();
        String[] sourceRoots = sourcePath.getSourceRoots();
        for (String src : sourceRoots) {
            FileObject fo = getFileObject(src);
            if (fo == null) {
                continue;
            }
            Project p = getProject(fo);
            if (p == null) {
                continue;
            }
            projectDirectories.add(p.getProjectDirectory());
        }
        return projectDirectories;
    }
    
    /**
     * Returns FileObject for given String.
     */
    private static FileObject getFileObject (String file) {
        File f = new File (file);
        FileObject fo = FileUtil.toFileObject (FileUtil.normalizeFile(f));
        //String path = null;
        if (fo == null && file.contains("!/")) {
            int index = file.indexOf("!/");
            f = new File(file.substring(0, index));
            fo = FileUtil.toFileObject (f);
            //path = file.substring(index + "!/".length());
        }
        /*
        if (fo != null && FileUtil.isArchiveFile (fo)) {
            fo = FileUtil.getArchiveRoot (fo);
            if (path !=null) {
                fo = fo.getFileObject(path);
            }
        }*/
        return fo;
    }
    
    private Project getProject(FileObject fo) {
        FileObject f = fo;
        while (f != null) {
            f = f.getParent();
            if (f != null && ProjectManager.getDefault().isProject(f)) {
                break;
            }
        }
        if (f != null) {
            try {
                return ProjectManager.getDefault().findProject(f);
            } catch (IOException | IllegalArgumentException ex) {
            }
        }
        return null;
    }
    
    private class SourcePathListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            synchronized (rootDirsLock) {
                if (rootDirs != null) {
                    // If initialized already, recompute:
                    rootDirs = computeModuleRoots();
                }
            }
        }
        
    }

}
