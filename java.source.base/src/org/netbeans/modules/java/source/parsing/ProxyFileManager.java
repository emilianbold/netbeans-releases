/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.java.source.parsing;

import com.sun.tools.javac.api.ClientCodeWrapper.Trusted;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.netbeans.modules.java.source.util.Iterators;

/**
 *
 * @author Tomas Zezula
 */
@Trusted
public final class ProxyFileManager implements JavaFileManager {

    private static final Logger LOG = Logger.getLogger(ProxyFileManager.class.getName());

    private static final Location ALL = new Location () {
        @Override
        public String getName() { return "ALL";}   //NOI18N

        @Override
        public boolean isOutputLocation() { return false; }
    };

    /**
     * Workaround to allow Filer ask for getFileForOutput for StandardLocation.SOURCE_PATH
     * which is not allowed but Filer does not allow write anyway => safe to do it.
     */
    private static final Location SOURCE_PATH_WRITE = new Location () {
        @Override
        public String getName() { return "SOURCE_PATH_WRITE"; }  //NOI18N
        @Override
        public boolean isOutputLocation() { return false;}
    };

    private final Configuration cfg;
    private final Object ownerThreadLock = new Object();
    private JavaFileObject lastInfered;
    private String lastInferedResult;
    //@GuardedBy("ownerThreadLock")
    private Thread ownerThread;


    /** Creates a new instance of ProxyFileManager */
    public ProxyFileManager(
            @NonNull Configuration cfg) {
        assert cfg != null;
        this.cfg = cfg;
    }


    @Override
    @NonNull
    public Iterable<JavaFileObject> list(
            @NonNull final Location l,
            @NonNull final String packageName,
            @NonNull final Set<JavaFileObject.Kind> kinds,
            final boolean recurse) throws IOException {
        checkSingleOwnerThread();
        try {
            final JavaFileManager[] fms = cfg.getFileManagers (l, null);
            List<Iterable<JavaFileObject>> iterables = new ArrayList<>(fms.length);
            for (JavaFileManager fm : fms) {
                iterables.add(fm.list(l, packageName, kinds, recurse));
            }
            final Iterable<JavaFileObject> result = Iterators.chained(iterables);
            if (LOG.isLoggable(Level.FINER)) {
                final StringBuilder urls = new StringBuilder ();
                for (JavaFileObject jfo : result ) {
                    urls.append(jfo.toUri().toString());
                    urls.append(", ");  //NOI18N
                }
                LOG.log(
                    Level.FINER,
                    "List {0} Package: {1} Kinds: {2} -> {3}", //NOI18N
                    new Object[] {
                        l,
                        packageName,
                        kinds,
                        urls
                    });
            }
            return result;
        } finally {
            clearOwnerThread();
        }
    }

    @Override
    @CheckForNull
    public FileObject getFileForInput(
            @NonNull final Location l,
            @NonNull final String packageName,
            @NonNull final String relativeName) throws IOException {
        checkSingleOwnerThread();
        try {
            JavaFileManager[] fms = cfg.getFileManagers(l, null);
            for (JavaFileManager fm : fms) {
                FileObject result = fm.getFileForInput(l, packageName, relativeName);
                if (result != null) {
                    return result;
                }
            }
            return null;
        } finally {
            clearOwnerThread();
        }
    }

    @Override
    @CheckForNull
    public FileObject getFileForOutput(
            @NonNull final Location l,
            @NonNull final String packageName,
            @NonNull final String relativeName,
            @NullAllowed final FileObject sibling)
        throws IOException, UnsupportedOperationException, IllegalArgumentException {
        checkSingleOwnerThread();
        try {
            JavaFileManager[] fms = cfg.getFileManagers(
                    l == StandardLocation.SOURCE_PATH ?
                        SOURCE_PATH_WRITE : l,
                    null);
            assert fms.length <= 1;
            if (fms.length == 0) {
                throw new UnsupportedOperationException("No JavaFileManager for location: " + l);  //NOI18N
            } else {
                return mark(
                        fms[0].getFileForOutput(l, packageName, relativeName, sibling),
                        l);
            }
        } finally {
            clearOwnerThread();
        }
    }

    @Override
    @CheckForNull
    public ClassLoader getClassLoader (@NonNull final Location l) {
        checkSingleOwnerThread();
        try {
            return null;
        } finally {
            clearOwnerThread();
        }
    }

    @Override
    public void flush() throws IOException {
        checkSingleOwnerThread();
        try {
            for (JavaFileManager fm : cfg.getFileManagers(ALL, null)) {
                fm.flush();
            }
        } finally {
            clearOwnerThread();
        }
    }

    @Override
    public void close() throws IOException {
        checkSingleOwnerThread();
        try {
            for (JavaFileManager fm : cfg.getFileManagers(ALL, null)) {
                fm.close();
            }
        } finally {
            clearOwnerThread();
        }
    }

    @Override
    public int isSupportedOption(@NonNull final String string) {
        checkSingleOwnerThread();
        try {
            return -1;
        } finally {
            clearOwnerThread();
        }
    }

    @Override
    public boolean handleOption (
            @NonNull final String current,
            @NonNull final Iterator<String> remains) {
        checkSingleOwnerThread();
        try {
            boolean isSourceElement;
            if (AptSourceFileManager.ORIGIN_FILE.equals(current)) {
                if (!remains.hasNext()) {
                    throw new IllegalArgumentException("The apt-source-root requires folder.");    //NOI18N
                }
                final String sib = remains.next();
                if(sib.length() != 0) {
                    final URL sibling = asURL(sib);
                    final boolean inSourceRoot =
                        cfg.getProcessorGeneratedFiles().findSibling(Collections.singleton(sibling)) != null;
                    cfg.getSiblings().push(sibling, inSourceRoot);
                } else {
                    cfg.getSiblings().pop();
                }
                return true;
            } else if ((isSourceElement=AptSourceFileManager.ORIGIN_SOURCE_ELEMENT_URL.equals(current)) ||
                       AptSourceFileManager.ORIGIN_RESOURCE_ELEMENT_URL.equals(current)) {
                if (remains.hasNext()) {
                    final Collection<? extends URL> urls = asURLs(remains);
                    URL sibling = cfg.getProcessorGeneratedFiles().findSibling(urls);
                    boolean inSourceRoot = true;
                    if (sibling == null) {
                        sibling = cfg.getSiblings().getProvider().getSibling();
                        inSourceRoot = cfg.getSiblings().getProvider().isInSourceRoot();
                    }
                    cfg.getSiblings().push(sibling, inSourceRoot);
                    if (LOG.isLoggable(Level.INFO) && isSourceElement && urls.size() > 1) {
                        final StringBuilder sb = new StringBuilder();
                        for (URL url : urls) {
                            if (sb.length() > 0) {
                                sb.append(", ");    //NOI18N
                            }
                            sb.append(url);
                        }
                        LOG.log(
                            Level.FINE,
                            "Multiple source files passed as ORIGIN_SOURCE_ELEMENT_URL: {0}; using: {1}",  //NOI18N
                            new Object[]{
                                sb,
                                cfg.getSiblings().getProvider().getSibling()
                            });
                    }
                } else {
                   cfg.getSiblings().pop();
                }
                return true;
            }
            final Collection<String> defensiveCopy = copy(remains);
            for (JavaFileManager m : cfg.getFileManagers(ALL, current)) {
                if (m.handleOption(current, defensiveCopy.iterator())) {
                    return true;
                }
            }
            return false;
        } finally {
            clearOwnerThread();
        }
    }

    @Override
    public boolean hasLocation(@NonNull final Location location) {
        checkSingleOwnerThread();
        try {
            return cfg.hasLocations(location);
        } finally {
            clearOwnerThread();
        }
    }

    @Override
    @CheckForNull
    public Location getModuleLocation(Location location, String moduleName) throws IOException {
        checkSingleOwnerThread();
        try {
            final JavaFileManager[] jfms = cfg.getFileManagers(location, null);
            return jfms.length == 0 ?
                    null :
                    jfms[0].getModuleLocation(location, moduleName);
        } finally {
            clearOwnerThread();
        }
    }

    @Override
    @CheckForNull
    public Location getModuleLocation(Location location, JavaFileObject fo, String pkgName) throws IOException {
        checkSingleOwnerThread();
        try {
            final JavaFileManager[] jfms = cfg.getFileManagers(location, null);
            return jfms.length == 0 ?
                    null :
                    jfms[0].getModuleLocation(location, fo, pkgName);
        } finally {
            clearOwnerThread();
        }
    }

    @Override
    @CheckForNull
    public String inferModuleName(@NonNull final Location location) throws IOException {
        checkSingleOwnerThread();
        try {
            final JavaFileManager[] jfms = cfg.getFileManagers(location, null);
            return jfms.length == 0 ?
                    null :
                    jfms[0].inferModuleName(location);
        } finally {
            clearOwnerThread();
        }
    }

    @Override
    @NonNull
    public Iterable<Set<Location>> listModuleLocations(@NonNull final Location location) throws IOException {
        checkSingleOwnerThread();
        try {
            final JavaFileManager[] jfms = cfg.getFileManagers(location, null);
            return jfms.length == 0 ?
                    Collections.<Set<Location>>emptySet() :
                    jfms[0].listModuleLocations(location);
        } finally {
            clearOwnerThread();
        }
    }

    @Override
    @CheckForNull
    public JavaFileObject getJavaFileForInput (
            @NonNull final Location l,
            @NonNull final String className,
            @NonNull final JavaFileObject.Kind kind) throws IOException {
        checkSingleOwnerThread();
        try {
            JavaFileManager[] fms = cfg.getFileManagers (l, null);
            for (JavaFileManager fm : fms) {
                JavaFileObject result = fm.getJavaFileForInput(l,className,kind);
                if (result != null) {
                    return result;
                }
            }
            return null;
        } finally {
            clearOwnerThread();
        }
    }

    @Override
    @CheckForNull
    public JavaFileObject getJavaFileForOutput(
            @NonNull final Location l,
            @NonNull final String className,
            @NonNull final JavaFileObject.Kind kind,
            @NonNull final FileObject sibling)
        throws IOException, UnsupportedOperationException, IllegalArgumentException {
        checkSingleOwnerThread();
        try {
            final JavaFileManager[] fms = cfg.getFileManagers (l, null);
            assert fms.length <= 1;
            if (fms.length == 0) {
                throw new UnsupportedOperationException("No JavaFileManager for location: " + l);  //NOI18N
            } else {
                return mark (
                        fms[0].getJavaFileForOutput (l, className, kind, sibling),
                        l);
            }
        } finally {
            clearOwnerThread();
        }
    }


    @Override
    @CheckForNull
    public String inferBinaryName(
            @NonNull final JavaFileManager.Location location,
            @NonNull final JavaFileObject javaFileObject) {
        checkSingleOwnerThread();
        try {
            assert javaFileObject != null;
            //If cached return it dirrectly
            if (javaFileObject == lastInfered) {
                return lastInferedResult;
            }
            String result;
            //If instanceof FileObject.Base no need to delegate it
            if (javaFileObject instanceof InferableJavaFileObject) {
                final InferableJavaFileObject ifo = (InferableJavaFileObject) javaFileObject;
                result = ifo.inferBinaryName();
                if (result != null) {
                    this.lastInfered = javaFileObject;
                    this.lastInferedResult = result;
                    return result;
                }
            }
            //Ask delegates to infer the binary name
            JavaFileManager[] fms = cfg.getFileManagers (location, null);
            for (JavaFileManager fm : fms) {
                result = fm.inferBinaryName (location, javaFileObject);
                if (result != null && result.length() > 0) {
                    this.lastInfered = javaFileObject;
                    this.lastInferedResult = result;
                    return result;
                }
            }
            return null;
        } finally {
            clearOwnerThread();
        }
    }

    @Override
    public boolean isSameFile(FileObject fileObject, FileObject fileObject0) {
        checkSingleOwnerThread();
        try {
            final JavaFileManager[] fms = cfg.getFileManagers(ALL, null);
            for (JavaFileManager fm : fms) {
                if (fm.isSameFile(fileObject, fileObject0)) {
                    return true;
                }
            }
            return fileObject.toUri().equals (fileObject0.toUri());
        } finally {
            clearOwnerThread();
        }
    }

    @SuppressWarnings("unchecked")
    @NonNull
    private <T extends javax.tools.FileObject> T mark(
            @NonNull final T result,
            @NonNull final JavaFileManager.Location l) throws MalformedURLException {
        boolean valid = true;
        ProcessorGenerated.Type type = null;
        if (l == StandardLocation.CLASS_OUTPUT) {
            type = ProcessorGenerated.Type.RESOURCE;
        } else if (l == StandardLocation.SOURCE_OUTPUT) {
            type = ProcessorGenerated.Type.SOURCE;
        }
        if (cfg.getSiblings().getProvider().hasSibling() &&
            cfg.getSiblings().getProvider().isInSourceRoot()) {
            if (type == ProcessorGenerated.Type.SOURCE) {
                cfg.getProcessorGeneratedFiles().register(
                    cfg.getSiblings().getProvider().getSibling(),
                    result,
                    type);
            } else if (type == ProcessorGenerated.Type.RESOURCE) {
                try {
                    result.openInputStream().close();
                } catch (IOException ioe) {
                    //Marking only created files
                    cfg.getProcessorGeneratedFiles().register(
                        cfg.getSiblings().getProvider().getSibling(),
                        result,
                        type);
                }
            }
            if (!FileObjects.isValidFileName(result)) {
                LOG.log(
                    Level.WARNING,
                    "Cannot write Annotation Processor generated file: {0} ({1})",   //NOI18N
                    new Object[] {
                        result.getName(),
                        result.toUri()
                    });
                valid = false;
            }
        }
        return valid && (cfg.getProcessorGeneratedFiles().canWrite() || !cfg.getSiblings().getProvider().hasSibling()) ?
                result :
                (T) FileObjects.nullWriteFileObject((InferableJavaFileObject)result);    //safe - NullFileObject subclass of both JFO and FO.
    }

    private void checkSingleOwnerThread() {
        final Thread currentThread = Thread.currentThread();
        synchronized (ownerThreadLock) {
            if (ownerThread == null) {
                ownerThread = currentThread;
            } else if (ownerThread != currentThread) {
                //Dump both stacks and throw ISE.
                throw new ConcurrentModificationException(
                    String.format(
                        "Current owner: %s, New Owner: %s", //NOI18N
                        Arrays.asList(ownerThread.getStackTrace()),
                        Arrays.asList(currentThread.getStackTrace())));
            }
        }
    }

    private void clearOwnerThread() {
        synchronized (ownerThreadLock) {
            ownerThread = null;
        }
    }

    private static URL asURL(final String url) throws IllegalArgumentException {
        try {
            return new URL(url);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("Invalid path argument: " + url, ex);    //NOI18N
        }
    }

    private static Collection<? extends URL> asURLs(Iterator<? extends String> surls) {
        final ArrayDeque<URL> result = new ArrayDeque<>();
        while (surls.hasNext()) {
            final String surl = surls.next();
            if (FileObjects.JAVA.equals(FileObjects.getExtension(surl))) {
                result.add(asURL(surl));
            }
        }
        return result;
    }

    private static<T> Collection<T> copy(final Iterator<? extends T> from) {
        if (!from.hasNext()) {
            return Collections.<T>emptyList();
        } else {
            final List<T> result = new LinkedList<>();
            while (from.hasNext()) {
                result.add(from.next());
            }
            return result;
        }
    }


    public static final class Configuration {
        private static final int BOOT = 0;
        private static final int COMPILE = BOOT + 1;
        private static final int OUTPUT = COMPILE + 1;
        private static final int TREE_LOADER = OUTPUT + 1;
        private static final int SRC = TREE_LOADER + 1;
        private static final int APT_SRC = SRC + 1;
        private static final int MEM = APT_SRC + 1;
        private static final int MODULE_PLAT = MEM + 1;

        private static final JavaFileManager[] EMPTY = new JavaFileManager[0];

        private final CachingArchiveProvider cap;
        private final ClassPath boot;
        private final ClassPath bootCached;
        private final ClassPath compiledCached;
        private final ClassPath srcCached;
        private final ClassPath outputCached;
        private final ClassPath aptSrcCached;
        private final Map<Location,Entry> fileManagers;
        private final JavaFileManager[] emitted;

        private final SiblingSource siblings;
        private final FileManagerTransaction fmTx;
        private final ProcessorGenerated processorGeneratedFiles;

        private boolean useModifiedFiles = true;
        private JavaFileFilterImplementation filter;
        private boolean ignoreExcludes;

        private Configuration(
                @NonNull final ClassPath boot,
                @NonNull final ClassPath bootCached,
                @NonNull final ClassPath compiledCached,
                @NonNull final ClassPath srcCached,
                @NonNull final ClassPath outputCached,
                @NonNull final ClassPath aptSrcCached,
                @NonNull final SiblingSource siblings,
                @NonNull final FileManagerTransaction fmTx,
                @NonNull final ProcessorGenerated processorGeneratedFiles) {
            assert boot != null;
            assert bootCached != null;
            assert compiledCached != null;
            assert srcCached != null;
            assert outputCached != null;
            assert aptSrcCached != null;
            assert siblings != null;
            assert fmTx != null;
            assert processorGeneratedFiles != null;
            this.cap = CachingArchiveProvider.getDefault();
            this.boot = boot;
            this.bootCached = bootCached;
            this.compiledCached = compiledCached;
            this.srcCached = srcCached;
            this.outputCached = outputCached;
            this.aptSrcCached = aptSrcCached;
            this.siblings = siblings;
            this.fmTx = fmTx;
            this.processorGeneratedFiles = processorGeneratedFiles;
            this.fileManagers = createFactories();
            this.emitted = new JavaFileManager[7];
        }

        public void setUseModifiedFiles(final boolean useModifiedFiles) {
            this.useModifiedFiles = useModifiedFiles;
        }

        public boolean isUseModifiedFiles() {
            return this.useModifiedFiles;
        }

        public void setFilter(@NullAllowed final JavaFileFilterImplementation filter) {
            this.filter = filter;
        }

        @CheckForNull
        public JavaFileFilterImplementation getFilter() {
            return this.filter;
        }

        public void setIgnoreExcludes(final boolean ignoreExcludes) {
            this.ignoreExcludes = ignoreExcludes;
        }

        public boolean isIgnoreExcludes() {
            return this.ignoreExcludes;
        }

        @NonNull
        JavaFileManager[] getFileManagers(@NonNull Location location, @NullAllowed String hint) {
            if (location.getClass() == ModuleLocation.class) {
                location = ((ModuleLocation)location).getBaseLocation();
            }
            if (location == ALL) {
                //Todo: create factories with options when there are more than one option.
                if (TreeLoaderOutputFileManager.OUTPUT_ROOT.equals(hint)) {
                    createTreeLoaderFileManager();
                }
                final List<JavaFileManager> res = new ArrayList<>(emitted.length);
                for (JavaFileManager jfm : emitted) {
                    if (jfm != null) {
                        res.add(jfm);
                    }
                }
                return res.toArray(new JavaFileManager[res.size()]);
            } else {
                final Entry result = fileManagers.get(location);
                return result == null ?
                        EMPTY :
                        result.get();
            }
        }

        boolean hasLocations(@NonNull final Location l) {
            final Entry e = fileManagers.get(l);
            return e != null ?
                    e.hasLocation() :
                    false;
        }

        @NonNull
        SiblingSource getSiblings() {
            return siblings;
        }

        @NonNull
        ProcessorGenerated getProcessorGeneratedFiles() {
            return processorGeneratedFiles;
        }

        @NonNull
        private Map<Location,Entry> createFactories() {
            final Map<Location,Entry> m = new HashMap<>();
            m.put(StandardLocation.PLATFORM_CLASS_PATH, new Entry(() -> new JavaFileManager[] {createBootFileManager()}));
            m.put(StandardLocation.CLASS_PATH, new Entry(() -> {
                    final JavaFileManager compile = createCompileFileManager();
                    final JavaFileManager output = createOutputFileManager();
                    return output == null ?
                        new JavaFileManager[] {compile}:
                        new JavaFileManager[] {output, compile};
            }));
            m.put(StandardLocation.SOURCE_PATH, new Entry(() -> {
                    final JavaFileManager src = createSrcFileManager();
                    final JavaFileManager mem = createMemFileManager();
                    return src == null ?
                        EMPTY :
                        mem == null ?
                            new JavaFileManager[] {src}:
                        new JavaFileManager[] {
                            src,
                            mem};
            }));
            m.put(StandardLocation.CLASS_OUTPUT, new Entry(() -> {
                    final JavaFileManager output = createOutputFileManager();
                    final JavaFileManager treeLoader = createTreeLoaderFileManager();
                    return output == null ?
                        new JavaFileManager[] {treeLoader} :
                        new JavaFileManager[] {treeLoader, output};
            }));
            m.put(StandardLocation.SOURCE_OUTPUT, new Entry(() -> {
                    final JavaFileManager aptSrcOut = createAptSrcOutputFileManager();
                    return aptSrcOut == null ?
                        EMPTY:
                        new JavaFileManager[] {aptSrcOut};
            }));
            m.put(SOURCE_PATH_WRITE, new Entry(() -> {
                    final JavaFileManager src = createSrcFileManager();
                    return src == null ?
                        EMPTY:
                        new JavaFileManager[] {src};
            }));
            m.put(StandardLocation.SYSTEM_MODULE_PATH, new Entry(() -> new JavaFileManager[] {createSystemModuleFileManager()}));
            return m;
        }

        @NonNull
        private JavaFileManager createBootFileManager() {
            if (emitted[BOOT] == null) {
                emitted[BOOT] = new CachingFileManager (cap, bootCached, true, true);
            }
            return emitted[BOOT];
        }

        @NonNull
        private JavaFileManager createCompileFileManager() {
            if (emitted[COMPILE] == null) {
                emitted[COMPILE] = new CachingFileManager (cap, compiledCached, false, true);
            }
            return emitted[COMPILE];
        }

        @CheckForNull
        private JavaFileManager createSrcFileManager() {
            if (emitted[SRC] == null) {
                final boolean hasSources = this.srcCached != ClassPath.EMPTY;
                emitted[SRC] = hasSources ?
                    (!useModifiedFiles ?
                        new CachingFileManager (cap, srcCached, filter, false, ignoreExcludes) :
                        new SourceFileManager (srcCached, ignoreExcludes)) :
                    null;
            }
            return emitted[SRC];
        }

        @CheckForNull
        private JavaFileManager createOutputFileManager() {
            if (emitted[OUTPUT] == null) {
                final boolean hasSources = this.srcCached != ClassPath.EMPTY;
                emitted[OUTPUT] = hasSources ?
                        new OutputFileManager(
                            cap,
                            outputCached,
                            srcCached,
                            this.aptSrcCached,
                            siblings.getProvider(),
                            fmTx) :
                        null;
            }
            return emitted[OUTPUT];
        }

        @NonNull
        private JavaFileManager createTreeLoaderFileManager() {
            if (emitted[TREE_LOADER] == null) {
                emitted[TREE_LOADER] = new TreeLoaderOutputFileManager(cap, fmTx);
            }
            return emitted[TREE_LOADER];
        }

        @CheckForNull
        private JavaFileManager createAptSrcOutputFileManager() {
            if (emitted[APT_SRC] == null) {
            final boolean hasAptSources = this.aptSrcCached != ClassPath.EMPTY;
            emitted[APT_SRC] = hasAptSources ?
                    new AptSourceFileManager(
                            srcCached,
                            aptSrcCached,
                            siblings.getProvider(),
                            fmTx) :
                    null;
            }
            return emitted[APT_SRC];
        }

        @CheckForNull
        private JavaFileManager createMemFileManager() {
            return emitted[MEM];
        }

        @NonNull
        private JavaFileManager createSystemModuleFileManager() {
            if (emitted[MODULE_PLAT] == null) {
                emitted[MODULE_PLAT] = new ModuleFileManager(cap, boot, true);
            }
            return emitted[MODULE_PLAT];
        }

        @NonNull
        public static Configuration create (
                @NonNull final ClassPath boot,
                @NonNull final ClassPath bootCached,
                @NonNull final ClassPath compiledCached,
                @NonNull final ClassPath srcCached,
                @NonNull final ClassPath outputCached,
                @NonNull final ClassPath aptSrcCached,
                @NonNull final SiblingSource siblings,
                @NonNull final FileManagerTransaction fmTx,
                @NonNull final ProcessorGenerated processorGeneratedFiles) {
            return new Configuration(
                boot,
                bootCached,
                compiledCached,
                srcCached,
                outputCached,
                aptSrcCached,
                siblings,
                fmTx,
                processorGeneratedFiles);
        }

        private static final class Entry {
            private static final Predicate<JavaFileManager> ALL = (m)->true;

            private JavaFileManager[] fileManagers;
            private Supplier<JavaFileManager[]> factory;
            private final Predicate<JavaFileManager> filter;

            private Entry(@NonNull final Supplier<JavaFileManager[]> factory) {
                this(factory, null);
            }

            private Entry(
                    @NonNull final Supplier<JavaFileManager[]> factory,
                    @NullAllowed final Predicate<JavaFileManager> filter) {
                assert factory != null;
                this.factory = factory;
                this.filter = filter == null ? ALL : filter;
            }

            boolean hasLocation() {
                if (filter == ALL) {
                    return true;
                }
                return get().length > 0;
            }

            @NonNull
            JavaFileManager[] get() {
                JavaFileManager[] res;
                 if (fileManagers != null) {
                    res = fileManagers;
                } else {
                    fileManagers = factory.get();
                    assert fileManagers != null;
                    factory = null;
                    res = fileManagers;
                }
                if (filter != ALL) {
                    final List<JavaFileManager> filtered = new ArrayList<>(res.length);
                    for (JavaFileManager jfm : res) {
                        if (filter.test(jfm)) {
                            filtered.add(jfm);
                        }
                    }
                    res = filtered.toArray(new JavaFileManager[filtered.size()]);
                }
                return res;
            }
        }
    }
}
