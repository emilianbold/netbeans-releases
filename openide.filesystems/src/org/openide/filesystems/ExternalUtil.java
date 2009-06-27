/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.openide.filesystems;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbCollections;

/** Contains utility methods to deal with repository and error manager,
* so we do not need to directly contact
*
* @author Jaroslav Tulach
*/
final class ExternalUtil extends Object {
    /** value for the repository & error manager */
    static Repository repository;

    /** Static method to find the Repository to use.
     * @return Repository instance
     */
    public static Repository getRepository() {
        initialize();

        return repository;
    }

    /** Notifies an exception.
     */
    public static void exception(Exception ex) {
        LOG.log(Level.INFO, null, ex);
    }

    /** Copies anotation.
     * @param newEx new exception to annotate
     * @param oldEx old exception to take annotation from
     * @return newEx
     */
    public static Throwable copyAnnotation(Throwable newEx, Throwable oldEx) {
        return newEx.initCause(oldEx);
    }

    /** Annotates the exception with a message.
     */
    public static void annotate(Throwable ex, String msg) {
        Exceptions.attachLocalizedMessage(ex, msg);
    }

    /** Annotates the exception with a message.
     */
    public static Throwable annotate(Throwable ex, Throwable stack) {
        Throwable orig = ex;
        while (ex.getCause() != null) {
            ex = ex.getCause();
        }
        try {
            ex.initCause(stack);
        } catch (IllegalStateException ise) {
            // #164760 - fallback when initCause fails (e.g. for ClassNotFoundException)
            Exception e = new Exception(ex.getMessage(), stack);
            e.setStackTrace(ex.getStackTrace());
            return e;
        }
        return orig;
    }

    private static Logger LOG = Logger.getLogger("org.openide.filesystems"); // NOI18N
    /** Logs a text.
     */
    public static void log(String msg) {
        LOG.fine(msg);
    }

    /** Loads a class of given name
     * @param name name of the class
     * @return the class
     * @exception ClassNotFoundException if class was not found
     */
    public static Class findClass(String name) throws ClassNotFoundException {
        initialize();

        ClassLoader c = Lookup.getDefault().lookup(ClassLoader.class);

        if (c == null) {
            return Class.forName(name);
        } else {
            return Class.forName(name, true, c);
        }
    }

    private static final AtomicReference<Object> ADD_FS = new AtomicReference<Object>();
    static {
        ADD_FS.set(ADD_FS);
    }
    static synchronized final boolean addFileSystemDelayed(FileSystem fs) {
        return !ADD_FS.compareAndSet(ADD_FS, fs);
    }
    
    
    /** Initializes the context and errManager
     */
    private static void initialize() {
        Lookup lkp = Lookup.getDefault();
        
        Repository r;
        synchronized (ExternalUtil.class) {
            r = repository;
        }

        if (r == null) {
            Repository registeredRepository = lkp.lookup(Repository.class);
            Repository realRepository = assignRepository(registeredRepository);
            
            
            FileSystem fs = (FileSystem)ADD_FS.getAndSet(null);
            if (fs != null) {
                addFS(realRepository, fs);
            }
        }
    }
    
    @SuppressWarnings("deprecation")
    private static void addFS(Repository r, FileSystem fs) {
        r.addFileSystem(fs);
    }

    private static synchronized boolean isInitialized() {
        return repository != null;
    }

    /**
     * @param rep may be null
     */
    private static synchronized Repository assignRepository(Repository rep) {
        repository = rep;

        if (repository == null) {
            // if not provided use default one
            repository = new Repository(new MainFS());
        }
        
        return repository;
    }
    
    private static final class MainFS extends MultiFileSystem implements LookupListener {
        private static final Lookup.Result<FileSystem> ALL = Lookup.getDefault().lookupResult(FileSystem.class);
        private static final FileSystem MEMORY = FileUtil.createMemoryFileSystem();
        private static final XMLFileSystem layers = new XMLFileSystem();
        
        public MainFS() {
            ALL.addLookupListener(this);
            List<URL> layerUrls = new ArrayList<URL>();
            ClassLoader l = Thread.currentThread().getContextClassLoader();
            try {
                for (URL manifest : NbCollections.iterable(l.getResources("META-INF/MANIFEST.MF"))) { // NOI18N
                    InputStream is = manifest.openStream();
                    try {
                        Manifest mani = new Manifest(is);
                        String layerLoc = mani.getMainAttributes().getValue("OpenIDE-Module-Layer"); // NOI18N
                        if (layerLoc != null) {
                            URL layer = l.getResource(layerLoc);
                            if (layer != null) {
                                layerUrls.add(layer);
                            } else {
                                LOG.warning("No such layer: " + layerLoc);
                            }
                        }
                    } finally {
                        is.close();
                    }
                }
                for (URL generatedLayer : NbCollections.iterable(l.getResources("META-INF/generated-layer.xml"))) { // NOI18N
                    layerUrls.add(generatedLayer);
                }
                layers.setXmlUrls(layerUrls.toArray(new URL[layerUrls.size()]));
                LOG.log(Level.FINE, "Loading classpath layers: {0}", layerUrls);
            } catch (Exception x) {
                LOG.log(Level.WARNING, "Setting layer URLs: " + layerUrls, x);
            }
            resultChanged(null); // run after add listener - see PN1 in #26338
        }
        
        private static FileSystem[] computeDelegates() {
            List<FileSystem> arr = new ArrayList<FileSystem>();
            arr.add(MEMORY);
            arr.add(layers);
            arr.addAll(ALL.allInstances());
            return arr.toArray(new FileSystem[0]);
        }
        
    
        public void resultChanged(LookupEvent ev) {
            setDelegates(computeDelegates());
        }
    } // end of MainFS
}
