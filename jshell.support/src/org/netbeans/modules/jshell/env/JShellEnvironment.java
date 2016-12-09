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
import java.io.FilterReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.Document;
import jdk.jshell.JShell;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jshell.model.ConsoleEvent;
import org.netbeans.modules.jshell.model.ConsoleListener;
import org.netbeans.modules.jshell.project.ShellProjectUtils;
import org.netbeans.modules.jshell.support.JShellGenerator;
import org.netbeans.modules.jshell.support.ShellSession;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Pair;
import org.openide.util.Task;
import org.openide.util.WeakListeners;
import org.openide.util.io.ReaderInputStream;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * Encapsulates the IDE environment for the JShell. There are two implementations; one which works with a
 * Project and another, which works without it.
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
    
    private ShellSession      shellSession;
    
    private final Project     project;
    
    private ClasspathInfo     classpathInfo;

    private JavaPlatform      platform;
    
    private String            displayName;
    
    private ConfigurableClasspath  userClassPathImpl = new ConfigurableClasspath();
    
    private ClassPath         userLibraryPath = ClassPathFactory.createClassPath(userClassPathImpl);
    
    private ClassPath         snippetClassPath;
    
    private InputOutput       inputOutput;
    
    /**
     * True, if this environment controls the IO
     */
    private boolean           controlsIO;
    
    private boolean           closed;
    
    private List<ShellListener>   shellListeners = new ArrayList<>();
    
    private Lookup            envLookup;
    
    private final ShellL            shellL = new ShellL();
    
    protected JShellEnvironment(Project project, String displayName) {
        this.project = project;
        this.displayName = displayName;
    }
    
    public void appendClassPath(FileObject f) {
        userClassPathImpl.append(f);
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
    
    void init(FileObject workRoot) throws IOException {
        this.workRoot = workRoot;
        workRoot.setAttribute("jshell.scratch", true);
        consoleFile = workRoot.createData("console.jsh");
        
        EditorCookie.Observable eob = consoleFile.getLookup().lookup(EditorCookie.Observable.class);
        inst = new L();
        eob.addPropertyChangeListener(WeakListeners.propertyChange(inst, eob));

        platform = org.netbeans.modules.jshell.project.ShellProjectUtils.findPlatform(project);
    }
    
    protected InputOutput createInputOutput() {
        return null;
    }
    
    private PrintStream outStream;
    private PrintStream errStream;
    
    public InputStream getInputStream() throws IOException {
        if (inputOutput == null) {
            throw new IllegalStateException("not started");
        }
        return new ReaderInputStream(
                new FilterReader(inputOutput.getIn()) {
                    @Override
                    public void close() throws IOException {
                        // do not close the input, JShell may be reset.
                    }
                }, "UTF-8" // NOI18N
        );
    }
    
    public Lookup getLookup() {
        synchronized (this) {
            if (envLookup == null) {
                envLookup = new ProxyLookup(
                        consoleFile.getLookup(),
                        project == null ? 
                                Lookup.getDefault() :
                                project.getLookup()
                );
            }
        }
        return envLookup;
    }
    
    public PrintStream getOutputStream() throws IOException {
        if (inputOutput == null) {
            throw new IllegalStateException("not started");
        }
        synchronized (this) {
            if (outStream == null) {
                outStream = new PrintStream(new WriterOutputStream(inputOutput.getOut())) {
                    @Override
                    public void close() {
                        // suppress close
                    }
                };
            }
        }
        return outStream;
    }
    
    public PrintStream getErrorStream() throws IOException  {
        if (inputOutput == null) {
            throw new IllegalStateException("not started");
        }
        synchronized (this) {
            if (errStream == null) {
                errStream = new PrintStream(
                    new WriterOutputStream(inputOutput.getOut())) {
                    @Override
                    public void close() {
                        // suppress close
                    }
                    
                };
            }
        }
        return errStream;
    }
    
    public JShell.Builder customizeJShell(JShell.Builder b) {
        return b;
    }
    
    private volatile boolean starting;

    public synchronized void start() throws IOException {
        assert workRoot != null;
        if (shellSession != null) {
            return;
        }
        inputOutput = createInputOutput();
        if (inputOutput == null) {
            inputOutput = IOProvider.getDefault().getIO(displayName, false);
            controlsIO = true;
        }
        JavaPlatform platformTemp = getPlatform();
        final List<URL> roots = new ArrayList<>();
        FileObject root = ShellProjectUtils.findProjectRoots(getProject(), roots);
        ClasspathInfo cpi;
        
        snippetClassPath = ClassPathSupport.createClassPath(workRoot);

        if (root != null) {
            ClasspathInfo projectInfo = ClasspathInfo.create(root);
            ClasspathInfo.Builder bld = new ClasspathInfo.Builder(
                    projectInfo.getClassPath(ClasspathInfo.PathKind.BOOT)
            );
            ClassPath source = projectInfo.getClassPath(ClasspathInfo.PathKind.SOURCE);
            ClassPath compile = ClassPathSupport.createProxyClassPath(
                        ClassPathSupport.createClassPath(roots.toArray(new URL[roots.size()])),
                        userLibraryPath,
                        projectInfo.getClassPath(ClasspathInfo.PathKind.COMPILE)
                    );
            
            bld.setClassPath(compile)
                //.setSourcePath(source)
                    ;
            
            ClassPath modBoot = projectInfo.getClassPath(ClasspathInfo.PathKind.MODULE_BOOT);
            ClassPath modClass = projectInfo.getClassPath(ClasspathInfo.PathKind.MODULE_CLASS);
            ClassPath modCompile = projectInfo.getClassPath(ClasspathInfo.PathKind.MODULE_COMPILE);
            
            bld.
                setModuleBootPath(modBoot).
                setModuleClassPath(modClass).
                setModuleCompilePath(modCompile);
            cpi = bld.build();
            /*
            cpi = ClasspathInfo.create(
                    projectInfo.getClassPath(ClasspathInfo.PathKind.BOOT),
                    ClassPathSupport.createProxyClassPath(
                        ClassPathSupport.createClassPath(roots.toArray(new URL[roots.size()])),
                        userLibraryPath,
                        projectInfo.getClassPath(ClasspathInfo.PathKind.COMPILE)
                    ),
                    projectInfo.getClassPath(ClasspathInfo.PathKind.SOURCE)
            );
            */
        } else {
            cpi = ClasspathInfo.create(platformTemp.getBootstrapLibraries(),
                    platformTemp.getStandardLibraries(),
                    ClassPath.EMPTY);
        }
        this.classpathInfo = cpi;
        forceOpenDocument();
        doStartAndFire(ShellSession.createSession(this));
    }

    private void fireShellStatus(ShellEvent event) {
        List<ShellListener> ll;
        synchronized (this) {
            ll = new ArrayList<>(this.shellListeners);
        }
        if (ll.isEmpty()) {
            return;
        }
        ll.stream().forEach(l -> l.shellStatusChanged(event));
    }

    private void fireShellStarted(ShellEvent event) {
        List<ShellListener> ll;
        synchronized (this) {
            ll = new ArrayList<>(this.shellListeners);
        }
        if (ll.isEmpty()) {
            return;
        }
        ShellEvent e = event == null ? new ShellEvent(this) : event;
        ll.stream().forEach(l -> l.shellStarted(e));
    }
    
    /**
     * Determines if the environment can be reset. If it returns false,
     * then {@link #reset} will possibly reset the JShell, but will not
     * put it in a usable shape. For example without a running VM, the JShell
     * will not be able to define or execute snippets (or even its own startup).
     * 
     * @return if the environment can be reset.
     */
    public boolean canReset() {
        return true;
    }
    
    public void reset() {
        assert workRoot != null;
        doStartAndFire(ShellSession.createSession(this));
    }
    
    private void fireExecuting(ShellSession session, boolean start) {
        List<ShellListener> ll;
        synchronized (this) {
            ll = new ArrayList<>(this.shellListeners);
        }
        if (ll.isEmpty()) {
            return;
        }
        ShellEvent e = new ShellEvent(this, session, 
                start ? ShellStatus.EXECUTE : ShellStatus.READY, false);
        ll.stream().forEach(l -> l.shellStatusChanged(e));
    }
    
    private void doStartAndFire(ShellSession nss) {
        this.shellSession = nss;
        starting = true;
        Pair<ShellSession, Task> res = nss.start();
        nss.getModel().addConsoleListener(new ConsoleListener() {
            @Override
            public void executing(ConsoleEvent e) {
                fireExecuting(nss, e.isStart());
            }

            @Override
            public void sectionCreated(ConsoleEvent e) {}

            @Override
            public void sectionUpdated(ConsoleEvent e) {}

            @Override
            public void closed(ConsoleEvent e) {}
        });
        ShellSession previous = res.first();
        if (previous != null) {
            previous.removePropertyChangeListener(shellL);
        }
        nss.addPropertyChangeListener(shellL);
        ShellEvent event = new ShellEvent(this, nss, previous);
        fireShellStatus(event);
        
        res.second().addTaskListener(e -> {
            starting = false;
            fireShellStarted(event);
            fireShellStatus(event);
        });
        
    }
    
    public synchronized ShellStatus getStatus() {
        ShellSession session = this.shellSession;
        
        if (session == null) {
            return ShellStatus.INIT;
        }
        if (starting) {
            return ShellStatus.STARTING;
        } else if (closed) {
            return ShellStatus.SHUTDOWN;
        }
        if (session.getModel().isExecute()) {
            return ShellStatus.EXECUTE;
        } else if (session.isValid()) {
            return ShellStatus.READY;
        } else {
            return ShellStatus.DISCONNECTED;
        }
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
    
    private Document forceOpenDocument() throws IOException {
        EditorCookie cake = consoleFile.getLookup().lookup(EditorCookie.class);
        return cake == null ? null : cake.openDocument();
    }

    public Document getConsoleDocument() {
        EditorCookie cake = consoleFile.getLookup().lookup(EditorCookie.class);
        return cake == null ? null : cake.getDocument();
    }
    
    public ClassPath getSnippetClassPath() {
        return snippetClassPath;
    }
    
    public ClassPath getUserLibraryPath() {
        return userLibraryPath;
    }
    
    /**
     * Must be called on JShell shutdown to clean up resources. Should
     * be called after all 
     */
    public Task shutdown() throws IOException {
        Task t = shellSession.closeSession();
        t.addTaskListener((e) -> {
            postCloseCleanup();
        });
        return t;
    }
    
    private void postCloseCleanup() {
        try {
            // try to close the dataobject
            DataObject d = DataObject.find(getConsoleFile());
            EditorCookie cake = d.getLookup().lookup(EditorCookie.class);
            cake.close();
            // discard the dataobject
            synchronized (this) {
                if (document == null) {
                    return;
                }
                document = null;
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (controlsIO) {
            inputOutput.closeInputOutput();
        }
        ShellRegistry.get().closed(this);
    }
    
    private Document document;
    
    public void open() throws IOException {
        assert workRoot != null;
        DataObject d = DataObject.find(getConsoleFile());
        EditorCookie cake = d.getLookup().lookup(EditorCookie.class);
        // force open
        if (shellSession == null) {
            start();
            cake.open();
            document = cake.openDocument();
        } else {
            cake.open();
            document = cake.openDocument();
            return;
        }
        if (inputOutput != null) {
            inputOutput.select();
        }
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
        inputOutput.select();
    }
    
    public void notifyDisconnected(ShellSession old, boolean remoteClose) {
        List<ShellListener> ll;
        ShellSession s;
        synchronized (this) {
            s = this.shellSession;
            if (s == null || closed) {
                return;
            }
            ll = new ArrayList<>(shellListeners);
        }
        old.notifyClosed(this, remoteClose);
        ShellEvent e = new ShellEvent(this, s, ShellStatus.DISCONNECTED, remoteClose);
        ll.stream().forEach(l -> l.shellStatusChanged(e));
    }
    
    protected void notifyShutdown(boolean remote) {
        List<ShellListener> ll;
        ShellSession s;
        
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
            ll = new ArrayList<>(shellListeners);
            s = this.shellSession;
        }
        if (s != null) {
            s.notifyClosed(this, remote);
        }
        ShellEvent e = new ShellEvent(this);
        ll.stream().forEach(l -> l.shellShutdown(e));
        if (controlsIO && inputOutput != null) {
            try {
                inputOutput.getIn().close();
                inputOutput.getOut().close();
                inputOutput.getErr().close();
            } catch (IOException ex) {
                // expected
            }
        }
    }
    
    public boolean isClosed() {
        return closed;
    }
    
    public void addShellListener(ShellListener l) {
        synchronized (this) {
            this.shellListeners.add(l);
            if (!closed) {
                return;
            }
        }
        // notify the listener as soon as it is registered, since we're closed already.
        l.shellShutdown(new ShellEvent(this));
    }
    
    public synchronized void removeShellListener(ShellListener l) {
        this.shellListeners.remove(l);
    }
    
    public JShellGenerator createExecutionEnv() {
        return null;
    }
    
    public boolean closeDeadEditor() {
        if (getStatus() == ShellStatus.SHUTDOWN) {
            return closeEditor();
        } else {
            return false;
        }
    }
    
    public boolean closeEditor() {
        EditorCookie cake = getConsoleFile().getLookup().lookup(EditorCookie.class);
        if (cake == null) {
            return true;
        }
        return cake.close();
    }
    
    public String getMode() {
        return "launch"; // NOI18N
    }
    
    public JShell getShell() {
        ShellSession s = shellSession;
        return s == null ? null : s.getShell();
    }
    
    class ShellL implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (ShellSession.PROP_ENGINE.equals(evt.getPropertyName())) {
                ShellEvent ev = new ShellEvent(JShellEnvironment.this, shellSession, shellSession);
                fireShellStarted(ev);
            }
        }
        
    }
}
