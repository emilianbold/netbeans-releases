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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.java.repl.Utils;
import org.netbeans.modules.jshell.support.ShellSession;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Task;
import org.openide.util.WeakListeners;

/**
 * 
 * @author sdedic
 */
public class JShellEnvironment {
    public static final String PROP_DOCUMENT = "document";
    
    /**
     * filesystem which holds Snippets and the console file
     */
    private FileObject        workRoot;
    
    /**
     * The console file
     */
    private FileObject        consoleFile;
    
    private StyledDocument    consoleDocument;
    
    private ShellSession      shellSession;
    
    private final Project     project;
    
    private ClasspathInfo     classpathInfo;

    private JavaPlatform      platform;
    
    private String            displayName;
    
    private ClassPath         sourcePath;
    
    private PropertyChangeSupport supp = new PropertyChangeSupport(this);
    
    public JShellEnvironment(Project project, String displayName, FileObject workRoot) throws IOException {
        this.project = project;
        this.displayName = displayName;
        this.workRoot = workRoot;
        init();
    }
    
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        this.supp.addPropertyChangeListener(pcl);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        this.supp.removePropertyChangeListener(pcl);
    }
    
    public Project getProject() {
        return project;
    }

    public JavaPlatform getPlatform() {
        return platform;
    }

    public String getDisplayName() {
        return displayName;
    }
    
    private class L implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (EditorCookie.Observable.PROP_DOCUMENT.equals(evt.getPropertyName())) {
            }
        }
    }
    
    private L inst;
    
    private void init() throws IOException {
        workRoot.setAttribute("jshell.scratch", true);
        consoleFile = workRoot.createData("console.jsh");
        
        EditorCookie cake = consoleFile.getLookup().lookup(EditorCookie.class);
        consoleDocument = cake.openDocument();
        
        EditorCookie.Observable eob = consoleFile.getLookup().lookup(EditorCookie.Observable.class);
        eob.addPropertyChangeListener(WeakListeners.propertyChange(inst = new L(), eob));

        if (project != null) {
            for (SourceGroup sg : ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
                if (Utils.isNormalRoot(sg)) {
                    platform = Utils.findPlatform(ClassPath.getClassPath(sg.getRootFolder(), ClassPath.BOOT));
                }
            }
        }
        if (platform == null) {
            platform = JavaPlatform.getDefault();
        }
    }
    
    public synchronized void start() {
        if (shellSession != null) {
            return;
        }
        JavaPlatform platformTemp = JavaPlatformManager.getDefault().getDefaultPlatform();
        final Set<URL> roots = new HashSet<>();
        
        if (project != null) {
            for (SourceGroup sg : ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
                if (Utils.isNormalRoot(sg)) {
                    platformTemp = Utils.findPlatform(ClassPath.getClassPath(sg.getRootFolder(), ClassPath.BOOT));

                    roots.addAll(Utils.to2Roots(ClassPath.getClassPath(sg.getRootFolder(), ClassPath.EXECUTE)));
                }
            }
        }

        URL[] uRoots = roots.toArray(new URL[roots.size()]);
        /*
        repl = Lookup.getDefault().lookup(REPL.Factory.class).createREPL(
                platform, new PrintWriter(out), uRoots);
        */
        ClasspathInfo cpi;
        
        sourcePath = ClassPathSupport.createClassPath(workRoot);

        if (project != null) {
            cpi = ClasspathInfo.create(project.getProjectDirectory());
        } else {
            cpi = ClasspathInfo.create(
                    platformTemp.getBootstrapLibraries(),
                    platformTemp.getStandardLibraries(),
                    sourcePath);
        }
        this.classpathInfo = cpi;
        
        GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, new ClassPath[] { 
            sourcePath
        });
        
        URL url = URLMapper.findURL(workRoot, URLMapper.INTERNAL);
        IndexingManager.getDefault().refreshIndexAndWait(url, null, true);
        
        boolean hasIndex = org.netbeans.modules.java.source.indexing.JavaIndex.hasSourceCache(url,true);
        
        shellSession = ShellSession.createSession(this);
        shellSession.start();
    }
    
    public void reset() {
        EditorCookie cake = consoleFile.getLookup().lookup(EditorCookie.class);
        StyledDocument doc = cake.getDocument();
        this.consoleDocument = doc;
        ShellSession nss = ShellSession.createSession(this);
        nss.start();
        this.shellSession = nss;
    }
    
    public ShellSession getSession() {
        return shellSession;
    }

    public ClasspathInfo getClasspathInfo() {
        return classpathInfo;
    }
    
    public FileObject getWorkRoot() {
        return workRoot;
    }
    
    public FileObject getConsoleFile() {
        return consoleFile;
    }

    public Document getConsoleDocument() {
        EditorCookie cake = consoleFile.getLookup().lookup(EditorCookie.class);
        return cake == null ? null : cake.getDocument();
    }
    
    public ClassPath getSourcePath() {
        return sourcePath;
    }

    /**
     * Must be called on JShell shutdown to clean up resources. Should
     * be called after all 
     */
    public Task shutdown() throws IOException {
        Task t = shellSession.closeSession();
        t.addTaskListener((e) -> {
            try {
                // try to close the dataobject
                DataObject d = DataObject.find(getConsoleFile());
                EditorCookie cake = d.getLookup().lookup(EditorCookie.class);
                cake.close();
                // discard the dataobject
                consoleFile.delete();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            ShellRegistry.get().closed(this);
        });
        return t;
    }
    
    public void open() throws IOException {
        DataObject d = DataObject.find(getConsoleFile());
        EditorCookie cake = d.getLookup().lookup(EditorCookie.class);
        // force open
        cake.open();
        EditorCookie.Observable oo = d.getLookup().lookup(EditorCookie.Observable.class);
        assert oo != null;
        oo.addPropertyChangeListener((e) -> {
            if (EditorCookie.Observable.PROP_OPENED_PANES.equals(e.getPropertyName())) {
                if (cake.getOpenedPanes() == null) {
                    try {
                        shutdown();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                
            }
        });
    }
}
