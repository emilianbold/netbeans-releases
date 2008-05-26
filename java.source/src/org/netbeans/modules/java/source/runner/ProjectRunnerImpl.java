/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.runner;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.ClassPath.Entry;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.BuildArtifactMapper;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.spi.java.project.runner.ProjectRunnerImplementation;
import org.openide.LifecycleManager;
import org.openide.execution.ExecutionEngine;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 *
 * @author Jan Lahoda
 */
public class ProjectRunnerImpl implements ProjectRunnerImplementation {
    
    private static final Logger LOG = Logger.getLogger(ProjectRunnerImpl.class.getName());

    private static final Map<InputOutput, ReRunAction> rerunActions = new WeakHashMap<InputOutput, ReRunAction>();
    private static final Map<InputOutput, String> freeTabs = new WeakHashMap<InputOutput, String>();
    
    public void run(JavaPlatform p, Properties props, FileObject toRun) throws IOException {
        Builder b = new RunBuilder(p, props, toRun);

        execute(b);
    }
    
    public void test(JavaPlatform p, List<FileObject> toRun) throws IOException {
        if (toRun.isEmpty()) {
            throw new IllegalArgumentException();
        }

        Builder b = new TestBuilder(p, toRun);

        execute(b);
    }
    
    private static void execute(InputOutput io, List<String> params) throws IOException {
        LOG.log(Level.FINE, "arguments={0}", params);

        Process process = Runtime.getRuntime().exec(params.toArray(new String[0]));

        new StreamCopier(new InputStreamReader(process.getInputStream()), io.getOut()).start();
        new StreamCopier(new InputStreamReader(process.getErrorStream()), io.getErr()).start();
//        new StreamCopier(io.getIn(), new OutputStreamWriter(process.getOutputStream())).start();
        
        try {
            process.waitFor();
        } catch (InterruptedException ex) {
            throw (IOException) new IOException(ex.getMessage()).initCause(ex);
        }
    }

    private static File findJavaTool(JavaPlatform p) throws IOException {
        FileObject java = p.findTool("java");
        File javaFile = java != null ? FileUtil.toFile(java) : null;

        if (javaFile == null) {
            throw new IOException();
        }

        return javaFile;
    }

    private static String translate(ClassPath exec) {
        StringBuilder cp = new StringBuilder();
        boolean first = true;

        for (Entry e : exec.entries()) {
            File[] files = translate(e.getURL());

            if (files.length == 0) {
                //TODO: log
                LOG.log(Level.FINE, "cannot translate {0} to file", e.getURL().toExternalForm());
                continue;
            }

            for (File f : files) {
                if (!first) {
                    cp.append(File.pathSeparatorChar);
                }

                cp.append(f.getAbsolutePath());
                first = false;
            }
        }

        return cp.toString();
    }
    
    private static File[] translate(URL entry) {
        try {
            if (FileUtil.isArchiveFile(entry)) {
                entry = FileUtil.getArchiveRoot(entry);
            }
            
            SourceForBinaryQuery.Result r = SourceForBinaryQuery.findSourceRoots(entry);
            
            if (r.getRoots().length > 0) {
                List<File> translated = new LinkedList<File>();
                
                for (FileObject source : r.getRoots()) {
                    File sourceFile = FileUtil.toFile(source);
                    BuildArtifactMapper.ensureBuilt(sourceFile.toURI().toURL());
                    
                    for (URL binary : BinaryForSourceQuery.findBinaryRoots(source.getURL()).getRoots()) {
                        FileObject binaryFO = URLMapper.findFileObject(binary);
                        File cache = binaryFO != null ? FileUtil.toFile(binaryFO) : null;

                        if (cache != null) {
                            translated.add(cache);
                        }

                        if (sourceFile != null) {
                            translated.add(sourceFile);
                        }
                    }
                }
                
                return translated.toArray(new File[0]);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        File f = FileUtil.archiveOrDirForURL(entry);
        
        if (f != null) {
            return new File[] {f};
        } else {
            return new File[0];
        }
    }
    
    private static final class StreamCopier extends Thread {//XXX: Thread vs. RequestProcessor?

        private BufferedReader in;
        private OutputWriter out;

        public StreamCopier(Reader in, OutputWriter out) {
            this.in = new BufferedReader(in);
            this.out = out;
        }
        
        @Override
        public void run() {
            String line;
            
            try {
                while ((line = in.readLine()) != null) {
                    out.println(line);
                }
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
            
            try {
                in.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            
            out.close();
        }
        
    }
    
    private static final class ReRunAction extends AbstractAction {

        private String name;
        private InputOutput inout;
        private Builder b;

        public ReRunAction() {
            putValue(NAME, "ReRun");
            putValue(SMALL_ICON, new ImageIcon(Utilities.loadImage("/org/netbeans/modules/java/resources/rerun.png")));
        }

        public void actionPerformed(ActionEvent e) {
            final ProgressHandle handle = ProgressHandleFactory.createHandle("Building");
            
            handle.start();
            
            class Exec implements Runnable {
                public void run() {
                    try {
                        inout.getOut().reset();
                        inout.select();

                        b.build(inout);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    } finally {
                        handle.finish();
                        synchronized (ProjectRunnerImpl.class) {
                            setEnabled(true);
                            freeTabs.put(inout, name);
                        }
                    }
                }
            }
            
            ExecutionEngine.getDefault().execute(name, new Exec(), inout);
        }

        void runAndRemember(String name, InputOutput inout, Builder b) {
            this.name = name;
            this.inout = inout;
            this.b = b;
            
            actionPerformed(null);
        }

    }
    
    private static interface Builder {
        public String getName();
        public void build(InputOutput io) throws IOException;
    }
    
    private static final class RunBuilder implements Builder {

        private final JavaPlatform p;
        private final Properties props;
        private final FileObject toRun;

        public RunBuilder(JavaPlatform p, Properties props, FileObject toRun) {
            this.p = p;
            this.props = props;
            this.toRun = toRun;
        }
        
        public String getName() {
            return "Run " + toRun.getNameExt();
        }

        public void build(InputOutput io) throws IOException {
            io.getOut().println("Building");
            
            LifecycleManager.getDefault().saveAll();
            File javaFile = findJavaTool(p);

            String jvmArgs = props.getProperty("run.jvmargs");
            String args = props.getProperty("application.args");

            ClassPath exec = ClassPath.getClassPath(toRun, ClassPath.EXECUTE);
            ClassPath source = ClassPath.getClassPath(toRun, ClassPath.SOURCE);

            LOG.log(Level.FINE, "execute classpath={0}", exec);

            String cp = translate(exec);

            List<String> params = new LinkedList<String>();

            params.add(javaFile.getAbsolutePath());
            if (jvmArgs != null)
                params.addAll(Arrays.asList(jvmArgs.split(" ")));//TODO: correct spliting
            params.add("-classpath");
            params.add(cp);
            params.add(source.getResourceName(toRun, '.', false));
            if (args != null)
                params.addAll(Arrays.asList(args.split(" ")));//TODO: correct spliting

            io.getOut().println("Running");
            
            execute(io, params);
        }
        
    }
    
    private static final class TestBuilder implements Builder {

        private JavaPlatform p;
        private List<FileObject> toRun;

        public TestBuilder(JavaPlatform p, List<FileObject> toRun) {
            this.p = p;
            this.toRun = toRun;
        }
        
        public String getName() {
            return "Test";
        }

        public void build(InputOutput io) throws IOException {
            io.getOut().println("Building");
            
            LifecycleManager.getDefault().saveAll();
            File javaFile = findJavaTool(p);

            FileObject firstFile = toRun.iterator().next();
            ClassPath exec = ClassPath.getClassPath(firstFile, ClassPath.EXECUTE);
            ClassPath source = ClassPath.getClassPath(firstFile, ClassPath.SOURCE);

            LOG.log(Level.FINE, "execute classpath={0}", exec);

            String cp = translate(exec);

            List<String> params = new LinkedList<String>();

            params.add(javaFile.getAbsolutePath());
            params.add("-classpath");
            params.add(cp);
            params.add("junit.textui.TestRunner");
            //XXX: should run all the files:
            params.add(source.getResourceName(firstFile, '.', false));

            io.getOut().println("Running");
            
            execute(io, params);
        }
        
    }

    private void execute(Builder b) {
        ReRunAction rerunAction = null;
        InputOutput inout = null;
        String name = b.getName();

        synchronized (ProjectRunnerImpl.class) {
            for (Map.Entry<InputOutput, String> tab : freeTabs.entrySet()) {
                if (name.equals(tab.getValue())) {
                    inout = tab.getKey();
                    rerunAction = rerunActions.get(inout);
                    freeTabs.remove(inout);
                    break;
                }
            }

            if (inout == null) {
                rerunAction = new ReRunAction();
                inout = IOProvider.getDefault().getIO(name, new Action[]{rerunAction});
                rerunActions.put(inout, rerunAction);
            }
            
            rerunAction.setEnabled(false);
        }

        rerunAction.runAndRemember(name, inout, b);
    }
}
