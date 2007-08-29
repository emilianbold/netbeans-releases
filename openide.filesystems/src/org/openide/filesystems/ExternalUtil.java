/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.filesystems;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
        ex.initCause(stack);
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

    /** Initializes the context and errManager
     */
    private static void initialize() {
        if (!isInitialized()) {
            Lookup l = Lookup.getDefault();
            Repository rep = l.lookup(org.openide.filesystems.Repository.class);
            setRepository(rep);
        }
    }

    private static synchronized boolean isInitialized() {
        return repository != null;
    }

    /**
     * @param rep may be null
     */
    private static synchronized void setRepository(Repository rep) {
        repository = rep;

        if (repository == null) {
            // if not provided use default one
            repository = new Repository(new MainFS());
        }
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
