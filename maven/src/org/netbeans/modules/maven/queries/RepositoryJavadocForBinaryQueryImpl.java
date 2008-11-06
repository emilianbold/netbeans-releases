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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 *
 * JavadocForBinaryQueryImplementation implementation
 * for items in the maven repository. It checks the artifact and
 * looks for the same artifact but of type "javadoc.jar" or "javadoc"
 * @author  Milos Kleint
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation.class, position=999)
public class RepositoryJavadocForBinaryQueryImpl implements JavadocForBinaryQueryImplementation {
    
    public RepositoryJavadocForBinaryQueryImpl() {
    }
    
    /**
     * Find any Javadoc corresponding to the given classpath root containing
     * Java classes.
     * <p>
     * Any absolute URL may be used but typically it will use the <code>file</code>
     * protocol for directory entries and <code>jar</code> protocol for JAR entries
     * (e.g. <samp>jar:file:/tmp/foo.jar!/</samp>).
     * </p>
     * @param binaryRoot the class path root of Java class files
     * @return a result object encapsulating the roots and permitting changes to
     *         be listened to, or null if the binary root is not recognized
     */
    public JavadocForBinaryQuery.Result findJavadoc(URL url) {
        URL binRoot = url;
        if ("jar".equals(url.getProtocol())) { //NOI18N
            binRoot = FileUtil.getArchiveFile(url);
        } else {
            // null for directories.
            return null;
        }
        FileObject jarFO = URLMapper.findFileObject(binRoot);
        if (jarFO != null) {
            File jarFile = FileUtil.toFile(jarFO);
            if (jarFile != null) {
//                String name = jarFile.getName();
                File parent = jarFile.getParentFile();
                if (parent != null) {
                    File parentParent = parent.getParentFile();
                    if (parentParent != null) {
                        // each repository artifact should have this structure
                        String artifact = parentParent.getName();
                        String version = parent.getName();
                        File javadoc = new File(parent, artifact + "-" + version + "-javadoc.jar"); //NOI18N
                        if (javadoc.exists() || 
                           (jarFile.getName().startsWith(artifact) && jarFile.getName().contains(version))) { //#121657
                            return new DocResult(javadoc);
                        }
                    }
                }
            }
        }
        return null;
        
    }
    
    private class DocResult implements JavadocForBinaryQuery.Result  {
        private File file;
        private final List<ChangeListener> listeners;

        public DocResult(File javadoc) {
            file = javadoc;
            listeners = new ArrayList<ChangeListener>();
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
        
        public java.net.URL[] getRoots() {
            try {
                if (file.exists()) {
                    if (!FileUtil.isArchiveFile(FileUtil.toFileObject(file))) {
                        //#124175  ignore any jar files that are not jar files (like when downloaded file is actually an error html page).
                        Logger.getLogger(RepositoryJavadocForBinaryQueryImpl.class.getName()).info("The following javadoc jar in repository is not really a jar file: " + file.getAbsolutePath()); //NOI18N
                        return new URL[0];
                    }
                    
                    URL[] url = new URL[2];
                    // maven2 puts the javadocs in apidocs/ folder in the jar, 
                    // however there are non-maven built javadocs that have the docs in the root.
                    // don't examine here, just return 2 places..
                    url[0] = FileUtil.getArchiveRoot(file.toURI().toURL());
                    url[1] = new URL(url[0] + "apidocs/"); //NOI18N

                    //TODO there are also some other possible layout in the remote repository
                    // eg. jung/jung jas javadoc i "doc" subfolder, not sure we can cater for everything..
                    return url;
                }
            } catch (MalformedURLException exc) {
                ErrorManager.getDefault().notify(exc);
            }
            return new URL[0];
        }
        
    }
    
}
