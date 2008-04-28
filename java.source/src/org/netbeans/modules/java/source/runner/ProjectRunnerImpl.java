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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.ClassPath.Entry;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.java.source.usages.Index;
import org.netbeans.spi.java.project.runner.ProjectRunnerImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 *
 * @author Jan Lahoda
 */
public class ProjectRunnerImpl implements ProjectRunnerImplementation {
    
    private static final Logger LOG = Logger.getLogger(ProjectRunnerImpl.class.getName());

    public void run(JavaPlatform p, Properties props, FileObject toRun) throws IOException {
        FileObject java = p.findTool("java");
        File javaFile = java != null ? FileUtil.toFile(java) : null;
        
        if (javaFile == null) {
            throw new IOException();
        }
        
        String jvmArgs = props.getProperty("run.jvmargs");
        String args = props.getProperty("application.args");
        
        ClassPath exec = ClassPath.getClassPath(toRun, ClassPath.EXECUTE);
        ClassPath source = ClassPath.getClassPath(toRun, ClassPath.SOURCE);
        
        LOG.log(Level.FINE, "execute classpath={0}", exec);
        
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
        
        List<String> params = new LinkedList<String>();
        
        params.add(javaFile.getAbsolutePath());
        if (jvmArgs != null)
            params.addAll(Arrays.asList(jvmArgs.split(" ")));//TODO: correct spliting
        params.add("-classpath");
        params.add(cp.toString());
        params.add(source.getResourceName(toRun, '.', false));
        if (args != null)
            params.addAll(Arrays.asList(args.split(" ")));//TODO: correct spliting
        
        InputOutput io = IOProvider.getDefault().getIO("Run " + toRun.getNameExt(), false);
        
        io.getOut().reset();
        io.select();
        
        LOG.log(Level.FINE, "arguments={0}", params);
        
        Process process = Runtime.getRuntime().exec(params.toArray(new String[0]));
        
        new StreamCopier(new InputStreamReader(process.getInputStream()), io.getOut()).start();
        new StreamCopier(new InputStreamReader(process.getErrorStream()), io.getErr()).start();
//        new StreamCopier(io.getIn(), new OutputStreamWriter(process.getOutputStream())).start();
    }

    private File[] translate(URL entry) {
        try {
            if (FileUtil.isArchiveFile(entry)) {
                entry = FileUtil.getArchiveRoot(entry);
            }
            
            SourceForBinaryQuery.Result r = SourceForBinaryQuery.findSourceRoots(entry);
            
            if (r.getRoots().length > 0) {
                List<File> translated = new LinkedList<File>();
                
                for (FileObject source : r.getRoots()) {
                    File sourceFile = FileUtil.toFile(source);
                    File cache = Index.getClassFolder(source.getURL(), true);

                    if (cache != null) {
                        translated.add(cache);
                    }
                    
                    if (sourceFile != null) {
                        translated.add(sourceFile);
                    }
                }
                
                return translated.toArray(new File[0]);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        FileObject fo = URLMapper.findFileObject(entry);//?

        if (fo == null) {
            return new File[0];
        }

        File f = FileUtil.toFile(fo);
        
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
}
