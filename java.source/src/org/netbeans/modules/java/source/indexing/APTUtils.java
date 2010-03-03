/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.source.indexing;

import com.sun.tools.javac.util.Context;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.processing.Processor;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.ClassPath.Entry;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.queries.AnnotationProcessingQuery;
import org.netbeans.api.java.queries.AnnotationProcessingQuery.Result;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Lahoda, Dusan Balek
 */
public class APTUtils implements ChangeListener, PropertyChangeListener {

    private static final Logger LOG = Logger.getLogger(APTUtils.class.getName());
    private static final String PROCESSOR_PATH = "processorPath"; //NOI18N
    private static final String APT_ENABLED = "aptEnabled"; //NOI18N
    private static final String ANNOTATION_PROCESSORS = "annotationProcessors"; //NOI18N
    private static final Map<FileObject,Reference<APTUtils>> map = new WeakHashMap<FileObject,Reference<APTUtils>>();
    private static final Lookup HARDCODED_PROCESSORS = Lookups.forPath("Editors/text/x-java/AnnotationProcessors");
    private final FileObject root;
    private final ClassPath processorPath;
    private final AnnotationProcessingQuery.Result aptOptions;

    private APTUtils(FileObject root, ClassPath preprocessorPath, AnnotationProcessingQuery.Result aptOptions) {
        this.root = root;
        this.processorPath = preprocessorPath;
        this.aptOptions = aptOptions;
    }

    public static APTUtils get(final FileObject root) {
        if (root == null) {
            return null;
        }
        Reference<APTUtils> utilsRef = map.get(root);
        APTUtils utils = utilsRef != null ? utilsRef.get() : null;
        if (utils == null) {
            ClassPath pp = ClassPath.getClassPath(root, JavaClassPathConstants.PROCESSOR_PATH);
            if (pp == null) {
                return null;
            }
            Result options = AnnotationProcessingQuery.getAnnotationProcessingOptions(root);
            utils = new APTUtils(root, pp, options);
            pp.addPropertyChangeListener(WeakListeners.propertyChange(utils, pp));
            options.addChangeListener(WeakListeners.change(utils, options));
            map.put(root, new WeakReference<APTUtils>(utils));
        }
        return utils;
    }

    public boolean aptEnabled() {
        return aptOptions.annotationProcessingEnabled();
    }

    public Collection<? extends Processor> resolveProcessors() {
        List<URL> urls = new LinkedList<URL>();
        for (Entry e : processorPath.entries()) {
            urls.add(e.getURL());
        }
        ClassLoader cl = new URLClassLoader(urls.toArray(new URL[0]), new BypassOpenIDEUtilClassLoader(Context.class.getClassLoader()));
        Collection<Processor> result = lookupProcessors(cl);
        return result;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        verifyAttributes(root, false);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        verifyAttributes(root, false);
    }

    private Collection<Processor> lookupProcessors(ClassLoader cl) {
        Iterable<? extends String> processorNames = aptOptions.annotationProcessorsToRun();
        if (processorNames == null) {
            processorNames = getProcessorNames(cl);
        }
        List<Processor> result = new LinkedList<Processor>();
        for (String name : processorNames) {
            try {
                Class<?> clazz = Class.forName(name, true, cl);
                Object instance = clazz.newInstance();
                if (instance instanceof Processor) {
                    result.add((Processor) instance);
                }
            } catch (ThreadDeath td) {
                throw td;
            } catch (Throwable t) {
                LOG.log(Level.FINE, null, t);
            }
        }
        result.addAll(HARDCODED_PROCESSORS.lookupAll(Processor.class));
        return result;
    }

    private Iterable<? extends String> getProcessorNames(ClassLoader cl) {
        Collection<String> result = new LinkedList<String>();
        try {
            Enumeration<URL> resources = cl.getResources("META-INF/services/" + Processor.class.getName()); //NOI18N
            while (resources.hasMoreElements()) {
                BufferedReader ins = null;
                try {
                    ins = new BufferedReader(new InputStreamReader(resources.nextElement().openStream(), "UTF-8")); //NOI18N
                    String line;
                    while ((line = ins.readLine()) != null) {
                        int hash = line.indexOf('#');
                        line = hash != (-1) ? line.substring(0, hash) : line;
                        line = line.trim();
                        if (line.length() > 0) {
                            result.add(line);
                        }
                    }
                } catch (IOException ex) {
                    LOG.log(Level.FINE, null, ex);
                } finally {
                    if (ins != null) {
                        ins.close();
                    }
                }
            }
        } catch (IOException ex) {
            LOG.log(Level.FINE, null, ex);
        }
        return result;
    }

    boolean verifyAttributes(FileObject fo, boolean allFilesIndexing) {
        try {
            URL url = fo.getURL();
            if (JavaIndex.ensureAttributeValue(url, PROCESSOR_PATH, processorPath.toString()) && !allFilesIndexing) {
                JavaIndex.LOG.fine("forcing reindex due to processor path change"); //NOI18N
                IndexingManager.getDefault().refreshIndex(url, null);
                return true;
            }
            if (JavaIndex.ensureAttributeValue(url, APT_ENABLED, aptOptions.annotationProcessingEnabled() ? Boolean.TRUE.toString() : null) && !allFilesIndexing) {
                JavaIndex.LOG.fine("forcing reindex due to change in annotation processing options"); //NOI18N
                IndexingManager.getDefault().refreshIndex(url, null);
                return true;
            }
            if (JavaIndex.ensureAttributeValue(url, ANNOTATION_PROCESSORS, encodeToStirng(aptOptions.annotationProcessorsToRun())) && !allFilesIndexing) {
                JavaIndex.LOG.fine("forcing reindex due to change in annotation processors"); //NOI18N
                IndexingManager.getDefault().refreshIndex(url, null);
                return true;
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return false;
    }

    private String encodeToStirng(Iterable<? extends String> strings) {
        if (strings == null)
            return null;
        StringBuilder sb = new StringBuilder();
        for (Iterator it = strings.iterator(); it.hasNext();) {
            sb.append(it.next());
            if (it.hasNext())
                sb.append(',');
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    //keep synchronized with libs.javacapi/manifest.mf and libs.javacimpl/manifest.mf
    //when adding new packages, double-check the quick path in loadClass below:
    private static final Iterable<? extends String> javacPackages = Arrays.asList("com.sun.javadoc.", "com.sun.source.", "javax.annotation.processing.", "javax.lang.model.", "javax.tools.", "com.sun.tools.javac.", "com.sun.tools.javadoc.");
    private static final class BypassOpenIDEUtilClassLoader extends ClassLoader {
        private final ClassLoader contextCL;
        public BypassOpenIDEUtilClassLoader(ClassLoader contextCL) {
            super(getSystemClassLoader().getParent());
            this.contextCL = contextCL;
        }

        @Override
        protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            //the 5-th letter of all interesting packages is either 's' or 'x'
            //using that to prevent (possibly expensive) loop through javacPackages:
            char f = name.length() > 4 ? name.charAt(4) : '\0';

            if (f == 'x' || f == 's') {
                for (String pack : javacPackages) {
                    if (name.startsWith(pack)) {
                        return contextCL.loadClass(name);
                    }
                }
            }
            
            return super.loadClass(name, resolve);
        }

        //getResource and getResources of module classloaders do not return resources from parent's META-INF, so no need to override them
    }
}
