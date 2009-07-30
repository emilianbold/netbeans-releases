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

package org.netbeans.modules.maven.queries;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author mkleint
 */
public class MavenBinaryForSourceQueryImpl implements BinaryForSourceQueryImplementation {
    private final NbMavenProjectImpl project;
    private HashMap<URL, Res> results;
    
    /** Creates a new instance of MavenBinaryForSourceQueryImpl */
    public MavenBinaryForSourceQueryImpl(NbMavenProjectImpl prj) {
        project = prj;
        results = new HashMap<URL, Res>();
    }
    
    /**
     * Returns the binary root(s) for a given source root.
     * <p>
     * The returned BinaryForSourceQuery.Result must be a singleton. It means that for
     * repeated calling of this method with the same recognized root the method has to
     * return the same instance of BinaryForSourceQuery.Result.<br>
     * The typical implemantation of the findBinaryRoots contains 3 steps:
     * <ol>
     * <li>Look into the cache if there is already a result for the root, if so return it</li>
     * <li>Check if the sourceRoot is recognized, if not return null</li>
     * <li>Create a new BinaryForSourceQuery.Result for the sourceRoot, put it into the cache
     * and return it.</li>
     * </ol>
     * </p>
     * <p>
     * Any absolute URL may be used but typically it will use the <code>file</code>
     * protocol for directory entries and <code>jar</code> protocol for JAR entries
     * (e.g. <samp>jar:file:/tmp/foo.jar!/</samp>).
     * </p>
     * @param sourceRoot the source path root
     * @return a result object encapsulating the answer or null if the sourceRoot is not recognized
     */    
    public BinaryForSourceQuery.Result findBinaryRoots(URL url) {
        if (results.containsKey(url)) {
            return results.get(url);
        }
        if ("file".equals(url.getProtocol())) { //NOI18N
            try {
                Res toReturn = null;
                File fil = new File(url.toURI());
                fil = FileUtil.normalizeFile(fil);
                MavenProject mav = project.getOriginalMavenProject();
                String src = mav.getBuild() != null ? mav.getBuild().getSourceDirectory() : null;
                String testSrc = mav.getBuild() != null ? mav.getBuild().getTestSourceDirectory() : null;
                File srcFile = src != null ? FileUtil.normalizeFile(new File(src)) : null;
                File testSrcFile = testSrc != null ? FileUtil.normalizeFile(new File(testSrc)) : null;
                toReturn = checkRoot(fil, srcFile, testSrcFile);
                if (toReturn == null) {
                    URI[] gens = project.getGeneratedSourceRoots();
                    for (URI gen : gens) {
                        // assume generated sources are not test..
                        toReturn = checkRoot(fil, gen, null);
                        if (toReturn != null) {
                            break;
                        }
                    }
                }
                if (toReturn == null) {
                    toReturn = checkRoot(fil, project.getScalaDirectory(false), project.getScalaDirectory(true));
                }
                if (toReturn == null) {
                    toReturn = checkRoot(fil, project.getGroovyDirectory(false), project.getGroovyDirectory(true));
                }
                if (toReturn != null) {
                    results.put(url, toReturn);
                }
                return toReturn;
            }
            catch (URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }

    private Res checkRoot(File root, File source, File test) {
        if (source != null && source.equals(root)) {
            return new Res(false, project);
        }
        if (test != null && test.equals(root)) {
            return new Res(true, project);
        }
        return null;
    }

    private Res checkRoot(File root, URI source, URI test) {
        return checkRoot(root,
                         source != null ? FileUtil.normalizeFile(new File(source)) : null,
                         test != null ? FileUtil.normalizeFile(new File(test)) : null);
    }

    
    private static class Res implements BinaryForSourceQuery.Result {
        private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
        private NbMavenProjectImpl project;
        private boolean isTest;
        Res(boolean test, NbMavenProjectImpl prj) {
            isTest = test;
            project = prj;

        }
        
         /**
         * Get the binary roots.         
         * @return array of roots of compiled classes (may be empty but not null)
         */       
        public URL[] getRoots() {
            try         {
                String binary = isTest ? project.getOriginalMavenProject().getBuild().getTestOutputDirectory()
                                       : project.getOriginalMavenProject().getBuild().getOutputDirectory();
                File binFile = FileUtil.normalizeFile(new java.io.File(binary));

                return new java.net.URL[]{binFile.toURI().toURL()};
            }
            catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
            return new URL[0];
        }   

        public void addChangeListener(ChangeListener changeListener) {
            synchronized (listeners) {
                listeners.add(changeListener);
            }
        }
        
        public void removeChangeListener(ChangeListener changeListener) {
            synchronized (listeners) {
                listeners.remove(changeListener);
            }
        }
        
        void fireChanged() {
            List<ChangeListener> lists = new ArrayList<ChangeListener>();
            synchronized(listeners) {
                lists.addAll(listeners);
            }
            for (ChangeListener listen : lists) {
                listen.stateChanged(new ChangeEvent(this));
            }
        }
    }
    
}
