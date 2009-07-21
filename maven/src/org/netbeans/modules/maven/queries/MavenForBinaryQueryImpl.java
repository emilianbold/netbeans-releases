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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 * SourceForBinary and JavadocForBinary query impls.
 * @author  Milos Kleint 
 */
public class MavenForBinaryQueryImpl implements SourceForBinaryQueryImplementation,
        JavadocForBinaryQueryImplementation {
    
    private final NbMavenProjectImpl project;
    private final HashMap<String, BinResult> map;
    /** Creates a new instance of MavenSourceForBinaryQueryImpl */
    public MavenForBinaryQueryImpl(NbMavenProjectImpl proj) {
        project = proj;
        map = new HashMap<String, BinResult>();
        NbMavenProject.addPropertyChangeListener(proj, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if (NbMavenProjectImpl.PROP_PROJECT.equals(event.getPropertyName())) {
                    synchronized (map) {
                        for (BinResult res : map.values()) {
                            if (!Arrays.equals(res.getCached(), res.getRoots())) {
                                res.fireChanged();
                            }
                        }
                    }
                }
            }
        });
    }
    /**
     * Returns the source root(s) for a given binary root.
     * <p>
     * Any absolute URL may be used but typically it will use the <code>file</code>
     * protocol for directory entries and <code>jar</code> protocol for JAR entries
     * (e.g. <samp>jar:file:/tmp/foo.jar!/</samp>).
     * </p>
     * @param binaryRoot the class path root of Java class files
     * @return a list of source roots; may be empty but not null
     */
    public SourceForBinaryQuery.Result findSourceRoots(URL url) {
        synchronized (map) {
            BinResult toReturn = map.get(url.toString());
            if (toReturn != null) {
                return toReturn;
            }
            if (url.getProtocol().equals("jar") && checkURL(url) != -1) { //NOI18N
                toReturn = new BinResult(url);
            }
            if (url.getProtocol().equals("file")) { //NOI18N
                int result = checkURL(url);
                if (result == 1 || result == 0) {
                    toReturn = new BinResult(url);
                }
            }
            if (toReturn != null) {
                map.put(url.toString(), toReturn);
            }
            return toReturn;
        }
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
        if (checkURL(url) != -1) {
            return new DocResult(url);
        }
        return null;
    }
    
    /**
     * -1 - not found
     * 0 - source
     * 1 - test source
     */
    private int checkURL(URL url) {
        if ("file".equals(url.getProtocol())) { //NOI18N
            // true for directories.
            try {
                MavenProject proj = project.getOriginalMavenProject();
                if (proj != null) {
                    Build build = proj.getBuild();
                    if (build != null && build.getOutputDirectory() != null) {
                        File src = FileUtil.normalizeFile(new File(build.getOutputDirectory()));
                        URL srcUrl = src.toURI().toURL();
                        if  (!srcUrl.toExternalForm().endsWith("/")) { //NOI18N
                            srcUrl = new URL(srcUrl.toExternalForm() + "/"); //NOI18N
                        }
                        
                        if (url.equals(srcUrl)) {
                            return 0;
                        }
                        File test = FileUtil.normalizeFile(new File(build.getTestOutputDirectory()));
                        // can be null in some obscrure cases.
                        URL testsrcUrl = test.toURI().toURL();
                        if  (!testsrcUrl.toExternalForm().endsWith("/")) { //NOI18N
                            testsrcUrl = new URL(testsrcUrl.toExternalForm() + "/"); //NOI18N
                        }
                        
                        if (url.equals(testsrcUrl)) {
                            return 1;
                        }
                    }
                }
            } catch (MalformedURLException exc) {
                ErrorManager.getDefault().notify(exc);
            }
            return -1;
        }
        if ("jar".equals(url.getProtocol()) //NOI18N
                && !Boolean.getBoolean("mevenide.projectLinksDisable")) { //possible fix for MEVENIDE-535   //NOI18N
            URL binRoot = FileUtil.getArchiveFile(url);
            File file = new File(URI.create(binRoot.toString()));
            String filepath = file.getAbsolutePath().replace('\\', '/'); //NOI18N
            String path = project.getArtifactRelativeRepositoryPath();
            int ret = filepath.endsWith(path) ? 0 : -1;
            if (ret == -1) {
                path = project.getTestArtifactRelativeRepositoryPath();
                ret = filepath.endsWith(path) ? 1 : -1;
            }
            return ret;
        }
        return -1;
    }
    
    private FileObject[] getSrcRoot() {
        Collection<FileObject> toReturn = new HashSet<FileObject>();
        List srcRoots = project.getOriginalMavenProject().getCompileSourceRoots();
        Iterator it = srcRoots.iterator();
        while (it.hasNext()) {
            String item = (String)it.next();
            FileObject fo = FileUtilities.convertStringToFileObject(item);
            if (fo != null) {
                toReturn.add(fo);
            }
        }
        URI[] genRoots = project.getGeneratedSourceRoots();
        for (int i = 0; i < genRoots.length; i++) {
            FileObject fo = FileUtilities.convertURItoFileObject(genRoots[i]);
            if (fo != null) {
                toReturn.add(fo);
            }
        }
//        URI scala = project.getScalaDirectory(false);
//        FileObject scalafo = FileUtilities.convertURItoFileObject(scala);
//        if (scalafo != null) {
//            toReturn.add(scalafo);
//        }
//        URI groovy = project.getGroovyDirectory(false);
//        FileObject groovyfo = FileUtilities.convertURItoFileObject(groovy);
//        if (groovyfo != null) {
//            toReturn.add(groovyfo);
//        }

        URI[] res = project.getResources(false);
        for (int i = 0; i < res.length; i++) {
            FileObject fo = FileUtilities.convertURItoFileObject(res[i]);
            if (fo != null) {
                boolean ok = true;
                //#166655 resource root cannot contain the real java/xxx roots
                for (FileObject form : toReturn) {
                    if (FileUtil.isParentOf(fo, form)) {
                        ok = false;
                        break;
                    }
                }
                if (ok) {
                    toReturn.add(fo);
                }
            }
        }
        return toReturn.toArray(new FileObject[toReturn.size()]);
    }
    
    private FileObject[] getTestSrcRoot() {
        Collection<FileObject> toReturn = new HashSet<FileObject>();
        List srcRoots = project.getOriginalMavenProject().getTestCompileSourceRoots();
        Iterator it = srcRoots.iterator();
        while (it.hasNext()) {
            String item = (String)it.next();
            FileObject fo = FileUtilities.convertStringToFileObject(item);
            if (fo != null) {
                toReturn.add(fo);
            }
        }
//        URI scala = project.getScalaDirectory(true);
//        FileObject scalafo = FileUtilities.convertURItoFileObject(scala);
//        if (scalafo != null) {
//            toReturn.add(scalafo);
//        }
//        URI groovy = project.getGroovyDirectory(true);
//        FileObject groovyfo = FileUtilities.convertURItoFileObject(groovy);
//        if (groovyfo != null) {
//            toReturn.add(groovyfo);
//        }

        URI[] res = project.getResources(true);
        for (int i = 0; i < res.length; i++) {
            FileObject fo = FileUtilities.convertURItoFileObject(res[i]);
            if (fo != null) {
                boolean ok = true;
                //#166655 resource root cannot contain the real java/xxx roots
                for (FileObject form : toReturn) {
                    if (FileUtil.isParentOf(fo, form)) {
                        ok = false;
                        break;
                    }
                }
                if (ok) {
                    toReturn.add(fo);
                }
            }
        }
        return toReturn.toArray(new FileObject[toReturn.size()]);
    }
    
    
    private URL[] getJavadocRoot() {
        //TODO shall we delegate to "possibly" generated javadoc in project or in site?
        return new URL[0];
    }
    
    
    private class BinResult implements SourceForBinaryQuery.Result  {
        private URL url;
        private final List<ChangeListener> listeners;
        private FileObject[] results;
        private FileObject[] cached = null;
        
        public BinResult(URL urlParam) {
            url = urlParam;
            listeners = new ArrayList<ChangeListener>();
        }
        
        public FileObject[] getRoots() {
            int xxx = checkURL(url);
            if (xxx == 0) {
                results = getSrcRoot();
            } else if (xxx == 1) {
                results = getTestSrcRoot();
            } else {
                results = new FileObject[0];
            }
//            System.out.println("src bin result for =" + url + " length=" + results.length);
            cached = results;
            return results;
        }
        
        public FileObject[] getCached() {
            return cached;
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
    
    private class DocResult implements JavadocForBinaryQuery.Result  {
        private URL url;
        private URL[] results;
        private final List<ChangeListener> listeners;
        
        public DocResult(URL urlParam) {
            url = urlParam;
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
        
        void fireChanged() {
            List<ChangeListener> lists = new ArrayList<ChangeListener>();
            synchronized(listeners) {
                lists.addAll(listeners);
            }
            for (ChangeListener listen : lists) {
                listen.stateChanged(new ChangeEvent(this));
            }
        }
        
        public java.net.URL[] getRoots() {
            if (checkURL(url) != -1) {
                results = getJavadocRoot();
            } else {
                results = new URL[0];
            }
            return results;
        }
        
    }
    
}
