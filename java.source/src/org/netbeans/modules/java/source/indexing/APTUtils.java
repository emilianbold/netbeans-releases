/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.source.indexing;

import com.sun.tools.javac.util.Context;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
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
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.queries.AnnotationProcessingQuery;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.modules.java.source.parsing.CachingArchiveClassLoader;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.impl.indexing.PathRegistry;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
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
    private static final String SOURCE_LEVEL_ROOT = "sourceLevel"; //NOI18N
    private static final Map<URL, APTUtils> knownSourceRootsMap = new HashMap<URL, APTUtils>();
    private static final Map<FileObject, Reference<APTUtils>> auxiliarySourceRootsMap = new WeakHashMap<FileObject, Reference<APTUtils>>();
    private static final Lookup HARDCODED_PROCESSORS = Lookups.forPath("Editors/text/x-java/AnnotationProcessors");
    private static final boolean DISABLE_CLASSLOADER_CACHE = Boolean.getBoolean("java.source.aptutils.disable.classloader.cache");
    private final FileObject root;
    private final ClassPath processorPath;
    private final AnnotationProcessingQuery.Result aptOptions;
    private final SourceLevelQuery.Result sourceLevel;
    private volatile ClassLoaderRef classLoaderCache;

    private APTUtils(FileObject root, ClassPath preprocessorPath, AnnotationProcessingQuery.Result aptOptions, SourceLevelQuery.Result sourceLevel) {
        this.root = root;
        this.processorPath = preprocessorPath;
        this.aptOptions = aptOptions;
        this.sourceLevel = sourceLevel;
    }

    public static APTUtils get(final FileObject root) {
        if (root == null) {
            return null;
        }

        URL rootUrl;
        
        try {
            rootUrl = root.getURL();
        } catch (FileStateInvalidException ex) {
            LOG.log(Level.FINE, null, ex);
            return null;
        }

        if (knownSourceRootsMap.containsKey(rootUrl)) {
            APTUtils utils = knownSourceRootsMap.get(rootUrl);

            if (utils == null) {
                knownSourceRootsMap.put(rootUrl, utils = create(root));
            }

            return utils;
        }

        Reference<APTUtils> utilsRef = auxiliarySourceRootsMap.get(root);
        APTUtils utils = utilsRef != null ? utilsRef.get() : null;

        if (utils == null) {
            auxiliarySourceRootsMap.put(root, new WeakReference<APTUtils>(utils = create(root)));
        }

        return utils;
    }

    public static void sourceRootRegistered(FileObject root, URL rootURL) {
        if (   root == null
            || knownSourceRootsMap.containsKey(rootURL)
            //XXX hack unknown roots are also scanned (and consequently registered), but never sourceRootUnregistered(rootsRemoved):
            || PathRegistry.getDefault().getUnknownRoots().contains(rootURL)) {
            return;
        }

        Reference<APTUtils> utilsRef = auxiliarySourceRootsMap.remove(root);
        APTUtils utils = utilsRef != null ? utilsRef.get() : null;

        knownSourceRootsMap.put(rootURL, utils);
    }

    public static void sourceRootUnregistered(Iterable<? extends URL> roots) {
        for (URL root : roots) {
            knownSourceRootsMap.remove(root);
        }
        //XXX hack make sure we are not holding APTUtils for any unknown roots
        //just in case something goes wrong:
        for (URL unknown : PathRegistry.getDefault().getUnknownRoots()) {
            knownSourceRootsMap.remove(unknown);
        }
    }

    private static APTUtils create(FileObject root) {
        ClassPath pp = ClassPath.getClassPath(root, JavaClassPathConstants.PROCESSOR_PATH);
        if (pp == null) {
            return null;
        }
        AnnotationProcessingQuery.Result options = AnnotationProcessingQuery.getAnnotationProcessingOptions(root);
        SourceLevelQuery.Result sourceLevel = SourceLevelQuery.getSourceLevel2(root);
        APTUtils utils = new APTUtils(root, pp, options, sourceLevel);
        pp.addPropertyChangeListener(WeakListeners.propertyChange(utils, pp));
        pp.getRoots();//so that the ClassPath starts listening on the filesystem
        options.addChangeListener(WeakListeners.change(utils, options));
        if (sourceLevel.supportsChanges())
            sourceLevel.addChangeListener(WeakListeners.change(utils, sourceLevel));

        return utils;
    }

    public boolean aptEnabledOnScan() {
        return aptOptions.annotationProcessingEnabled().contains(AnnotationProcessingQuery.Trigger.ON_SCAN);
    }

    public boolean aptEnabledInEditor() {
        return aptOptions.annotationProcessingEnabled().contains(AnnotationProcessingQuery.Trigger.IN_EDITOR);
    }

    public Collection<? extends Processor> resolveProcessors(boolean onScan) {
        ClassLoader cl;
        final ClassLoaderRef cache = classLoaderCache;
        if (cache == null || (cl=cache.get(root)) == null) {
            cl = CachingArchiveClassLoader.forClassPath(processorPath, new BypassOpenIDEUtilClassLoader(Context.class.getClassLoader()));
            classLoaderCache = !DISABLE_CLASSLOADER_CACHE ? new ClassLoaderRef(cl, root) : null;
        }
        Collection<Processor> result = lookupProcessors(cl, onScan);
        return result;
    }

    public Map<? extends String, ? extends String> processorOptions() {
        return aptOptions.processorOptions();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        try {
            if (verifyAttributes(root, true)) {
                IndexingManager.getDefault().refreshIndex(root.getURL(), Collections.<URL>emptyList(), false);
            }
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ClassPath.PROP_ROOTS.equals(evt.getPropertyName())) {
            classLoaderCache = null;
        }
        try {
            if (verifyAttributes(root, true)) {
                IndexingManager.getDefault().refreshIndex(root.getURL(), Collections.<URL>emptyList(), false);
            }
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private Collection<Processor> lookupProcessors(ClassLoader cl, boolean onScan) {
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
        if (!onScan)
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

    boolean verifyAttributes(FileObject fo, boolean checkOnly) {
        if (fo == null)
            return false;
        boolean vote = false;
        try {
            URL url = fo.getURL();
            if (JavaIndex.ensureAttributeValue(url, SOURCE_LEVEL_ROOT, sourceLevel.getSourceLevel(), checkOnly)) {
                JavaIndex.LOG.fine("forcing reindex due to source level change"); //NOI18N
                vote = true;
                if (checkOnly) {
                    return vote;
                }
            }
            boolean apEnabledOnScan = aptOptions.annotationProcessingEnabled().contains(AnnotationProcessingQuery.Trigger.ON_SCAN);
            if (JavaIndex.ensureAttributeValue(url, APT_ENABLED, apEnabledOnScan ? Boolean.TRUE.toString() : null, checkOnly)) {
                JavaIndex.LOG.fine("forcing reindex due to change in annotation processing options"); //NOI18N
                vote = true;
                if (checkOnly) {
                    return vote;
                }
            }
            if (!apEnabledOnScan) {
                //no need to check further:
                return vote;
            }
            if (JavaIndex.ensureAttributeValue(url, PROCESSOR_PATH, pathToString(processorPath), checkOnly)) {
                JavaIndex.LOG.fine("forcing reindex due to processor path change"); //NOI18N
                vote = true;
                if (checkOnly) {
                    return vote;
                }
            }
            if (JavaIndex.ensureAttributeValue(url, ANNOTATION_PROCESSORS, encodeToStirng(aptOptions.annotationProcessorsToRun()), checkOnly)) {
                JavaIndex.LOG.fine("forcing reindex due to change in annotation processors"); //NOI18N
                vote = true;
                if (checkOnly) {
                    return vote;
                }
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return vote;
    }

    @NonNull
    private static String pathToString(@NonNull final ClassPath cp) {
        final StringBuilder b = new StringBuilder();
        for (FileObject fo : cp.getRoots()) {
            final URL u = fo.toURL();
            final File f = FileUtil.archiveOrDirForURL(u);
            if (f != null) {
                if (b.length() > 0) {
                    b.append(File.pathSeparatorChar);
                }
                b.append(f.getAbsolutePath());
            } else {
                if (b.length() > 0) {
                    b.append(File.pathSeparatorChar);
                }
                b.append(u);
            }
        }
        return b.toString();
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

    private static final class ClassLoaderRef extends SoftReference<ClassLoader> implements Runnable {

        private final long timeStamp;
        private final String rootPath;

        public ClassLoaderRef(final ClassLoader cl, final FileObject root) {
            super(cl, Utilities.activeReferenceQueue());
            this.timeStamp = getTimeStamp(root);
            this.rootPath = FileUtil.getFileDisplayName(root);
            LOG.log(Level.FINER, "ClassLoader for root {0} created.", new Object[]{rootPath});  //NOI18N
        }

        public ClassLoader get(final FileObject root) {
            final long curTimeStamp = getTimeStamp(root);
            return curTimeStamp == timeStamp ? get() : null;
        }

        @Override
        public void run() {
            LOG.log(Level.FINER, "ClassLoader for root {0} freed.", new Object[] {rootPath});   //NOI18N
        }

        private static long getTimeStamp(final FileObject root) {
            final FileObject archiveFile = FileUtil.getArchiveFile(root);
            return archiveFile != null ? root.lastModified().getTime() : -1L;
        }
    }
}
