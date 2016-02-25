/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jshell.env;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.jshell.support.ShellSession;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;
import org.openide.util.NbBundle;


/**
 * Registration of all running JShells. Each project can have zero to many
 * JShell
 * 
 * @author sdedic
 */
public class ShellRegistry {
    private static ShellRegistry INSTANCE = new ShellRegistry();
    
    private ShellRegistry() {}
    
    /**
     * Root of the trash area; individual JShell subtrees are beneath it.
     */
    private FileObject  trashRoot;
    
    private void createAndCleanTrashArea() throws IOException {
        if (trashRoot != null) {
            return;
        }
        FileObject r = FileUtil.toFileObject(Places.getCacheSubdirectory("jshell"));
        if (r == null) {
            throw new IOException("Unable to create cache for generated snippets");
        }
        for (FileObject f : r.getChildren()) {
            f.delete();
        }
        trashRoot = r;
    }
    
    public static ShellRegistry get() {
        return INSTANCE;
    }
    
    private Map<Project, JShellEnvironment>     projectSessions = new HashMap<>();
    private Map<FileObject, JShellEnvironment>  fileIndex = new HashMap<>();
    private JShellEnvironment                   defaultSession;
    
    private FileObject  createCacheRoot() throws IOException {
        List<FileObject> roots = fileIndex.keySet().stream().map((f) -> f.getParent()).collect(Collectors.toList());
        Set<FileObject> existing = new HashSet<>(Arrays.asList(trashRoot.getChildren()));
        existing.removeAll(roots);
        if (!existing.isEmpty()) {
            // reuse an existing root
            FileObject r = existing.iterator().next();
            for (FileObject c : r.getChildren()) {
                c.delete();
            }
            return r;
        }
        while (true) {
            String n = FileUtil.findFreeFolderName(trashRoot, "junk");
            try {
                return trashRoot.createFolder(n);
            } catch (IOException ex) {
                // only ignore exceptions where the 'free name' actually exists.
                if (trashRoot.getFileObject(n) == null) {
                    throw ex;
                }
            }
        }
    }
    
    @NbBundle.Messages({
        "# {0} - project name",
        "ShellSession_CleanProject=JShell - project {0}"
    })
    public JShellEnvironment openSession(Project p) throws IOException {
        synchronized (this) {
            createAndCleanTrashArea();
            JShellEnvironment s = projectSessions.get(p);
            if (s != null) {
                return s;
            }
        }
        String dispName = Bundle.ShellSession_CleanProject(
                ProjectUtils.getInformation(p).getDisplayName());
        
        synchronized (this) {
            JShellEnvironment env = projectSessions.get(p);
            if (env != null) {
                return env;
            }
        }
        FileObject r = createCacheRoot();
        JShellEnvironment s = new JShellEnvironment(p, dispName, r); // may throw IOE
        synchronized (this) {
            JShellEnvironment ret  = projectSessions.computeIfAbsent(p, (proj) -> {
                fileIndex.put(s.getConsoleFile(), s);
                return s;
            });
            if (s != ret) {
                return ret;
            }
        }
        s.start();
        return s;
    }
    
    public JShellEnvironment get(FileObject consoleFile) {
        synchronized (this) {
            return fileIndex.get(consoleFile);
        }
    }
    
    void closed(JShellEnvironment env) {
        Project p = env.getProject();
        if (p == null) {
            // PENDING
            return;
        }
        FileObject wRoot = env.getWorkRoot();
        JShellEnvironment current;
        
        synchronized (this) {
            current = fileIndex.get(env.getConsoleFile());
            if (current == env) {
                fileIndex.remove(env.getConsoleFile());
                projectSessions.remove(p);
            } else {
                return;
            }
        }
        GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, new ClassPath[] { 
            env.getSourcePath()
        });
    }
    
    public JShellEnvironment openDefaultSession() {
        return null;
    }
    
    /**
     * Finds a JShell console file for the given JShell work file. If the passed
     * file is not in any active JShell work area, returns {@code null}. 
     * 
     * @param f
     * @return 
     */
    public JShellEnvironment getOwnerEnvironment(FileObject f) {
        if (trashRoot == null || !FileUtil.isParentOf(trashRoot, f)) {
            return null;
        }
        List<JShellEnvironment> env;
        
        synchronized (this) {
            env = new ArrayList<>(fileIndex.values());
        }
        for (JShellEnvironment e : env) {
            FileObject wr = e.getWorkRoot();
            if (wr == f || FileUtil.isParentOf(e.getWorkRoot(), f)) {
                return e;
            }
        }
        return null;
    }
}
