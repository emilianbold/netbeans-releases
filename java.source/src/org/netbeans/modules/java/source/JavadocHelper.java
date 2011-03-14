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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.source;

import com.sun.tools.javac.code.Symbol.ClassSymbol;
import java.awt.EventQueue;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.RequestProcessor;

/**
 * Utilities to assist with retrieval of Javadoc text.
 */
public class JavadocHelper {

    private static final Logger LOG = Logger.getLogger(JavadocHelper.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(JavadocHelper.class.getName(),1);
    
    private JavadocHelper() {}
    
    /**
     * A reopenable stream of text from a particular location.
     * You <em>must</em> either call {@link #close}, or call {@link #openStream}
     * (and {@linkplain InputStream#close close} it) at least once.
     */
    public static final class TextStream {
        private final URL url;
        private InputStream stream;
        private byte[] cache;
        /**
         * Creates a text stream from a given URL with no preopened stream.
         * @param url a URL
         */
        public TextStream(URL url) {
            this.url = url;
        }
        TextStream(URL url, InputStream stream) {
            this(url);
            this.stream = stream;
        }
        /**
         * Location of the text.
         * @return its (possibly network) location
         */
        public URL getLocation() {
            return url;
        }
        /**
         * Close any preopened stream without reading it.
         */
        public synchronized void close() {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException x) {
                    LOG.log(Level.INFO, null, x);
                }
                stream = null;
            }
        }
        /**
         * Open a stream.
         * (Might have already been opened but not read, in which case the preexisting stream is used.)
         * @return a stream, which you are obliged to close
         * @throws IOException if there is a problem reopening the stream
         */
        public synchronized InputStream openStream() throws IOException {
            if (cache != null) {
                LOG.log(Level.FINE, "loaded cached content for {0}", url);
                return new ByteArrayInputStream(cache);
            }
            assert !isRemote() || !EventQueue.isDispatchThread();
            InputStream uncached;
            if (stream != null) {
                uncached = stream;
                stream = null;
            } else {
                uncached = JavadocHelper.openStream(url);
            }
            if (isRemote()) {
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream(20 * 1024); // typical size for Javadoc page?
                    FileUtil.copy(uncached, baos);
                    cache = baos.toByteArray();
                } finally {
                    uncached.close();
                }
                LOG.log(Level.FINE, "cached content for {0} ({1}k)", new Object[] {url, cache.length / 1024});
                return new ByteArrayInputStream(cache);
            } else {
                return uncached;
            }
        }
        /**
         * @return true if this looks to be a web location
         */
        public boolean isRemote() {
            return JavadocHelper.isRemote(url);
        }
    }

    private static boolean isRemote(URL url) {
        return url.getProtocol().startsWith("http"); // NOI18N
    }
    
    /**
     * Like {@link URL#openStream} but uses the platform's user JAR cache ({@code ArchiveURLMapper}) when available.
     * @param url a url to open
     * @return its input stream
     * @throws IOException for the usual reasons
     */
    public static InputStream openStream(URL url) throws IOException {
        if (url.getProtocol().equals("jar")) { // NOI18N
            FileObject f = URLMapper.findFileObject(url);
            if (f != null) {
                return f.getInputStream();
            }
        }
        if (url.getProtocol().startsWith("http")) { // NOI18N
            LOG.log(Level.FINE, "opening network stream: {0}", url);
        }
        return url.openStream();
    }
    
    private static final Map<Element,TextStream> cachedJavadoc = new WeakHashMap<Element,TextStream>();
    
    /**
     * Richer version of {@link SourceUtils#getJavadoc}.
     * Finds {@link URL} of a javadoc page for given element when available. This method
     * uses {@link JavadocForBinaryQuery} to find the javadoc page for the give element.
     * For {@link PackageElement} it returns the package-summary.html for given package.
     * @param element to find the Javadoc for
     * @param cancel a Callable to signal cancel request
     * @return the javadoc page or null when the javadoc is not available.
     */
    public static TextStream getJavadoc(Element element, final @NullAllowed Callable<Boolean> cancel) {
        return getJavadoc(element, true, cancel);
    }

    /**
     * Richer version of {@link SourceUtils#getJavadoc}.
     * Finds {@link URL} of a javadoc page for given element when available. This method
     * uses {@link JavadocForBinaryQuery} to find the javadoc page for the give element.
     * For {@link PackageElement} it returns the package-summary.html for given package.
     * @param element to find the Javadoc for
     * @param allowRemoteJavadoc true if non-local javadoc sources should be enabled
     * @param cancel a Callable to signal cancel request
     * @return the javadoc page or null when the javadoc is not available.
     */
    public static TextStream getJavadoc(Element element, boolean allowRemoteJavadoc, final @NullAllowed Callable<Boolean> cancel) {
        synchronized (cachedJavadoc) {
            TextStream result = cachedJavadoc.get(element);
            if (result != null) {
                LOG.log(Level.FINE, "cache hit on {0}", result.getLocation());
                return result;
            }
        }
        TextStream result = doGetJavadoc(element, allowRemoteJavadoc, cancel);
        synchronized (cachedJavadoc) {
            cachedJavadoc.put(element, result);
        }
        return result;
    }

    /**
     * Richer version of {@link SourceUtils#getJavadoc}.
     * Finds {@link URL} of a javadoc page for given element when available. This method
     * uses {@link JavadocForBinaryQuery} to find the javadoc page for the give element.
     * For {@link PackageElement} it returns the package-summary.html for given package.
     * @param element to find the Javadoc for
     * @return the javadoc page or null when the javadoc is not available.
     */
    public static TextStream getJavadoc(Element element) {
        return getJavadoc(element, null);
    }

    @org.netbeans.api.annotations.common.SuppressWarnings(value="DMI_COLLECTION_OF_URLS"/*,justification="URLs have never host part"*/)
    private static TextStream doGetJavadoc(final Element element, final boolean allowRemoteJavadoc, final Callable<Boolean> cancel) {
        if (element == null) {
            throw new IllegalArgumentException("Cannot pass null as an argument of the SourceUtils.getJavadoc"); // NOI18N
        }
        ClassSymbol clsSym = null;
        String pkgName;
        String pageName;
        boolean buildFragment = false;
        if (element.getKind() == ElementKind.PACKAGE) {
            List<? extends Element> els = element.getEnclosedElements();
            for (Element e : els) {
                if (e.getKind().isClass() || e.getKind().isInterface()) {
                    clsSym = (ClassSymbol) e;
                    break;
                }
            }
            if (clsSym == null) {
                return null;
            }
            pkgName = FileObjects.convertPackage2Folder(((PackageElement) element).getQualifiedName().toString());
            pageName = PACKAGE_SUMMARY;
        } else {
            Element e = element;
            StringBuilder sb = new StringBuilder();
            while (e.getKind() != ElementKind.PACKAGE) {
                if (e.getKind().isClass() || e.getKind().isInterface()) {
                    if (sb.length() > 0) {
                        sb.insert(0, '.');
                    }
                    sb.insert(0, e.getSimpleName());
                    if (clsSym == null) {
                        clsSym = (ClassSymbol) e;
                    }
                }
                e = e.getEnclosingElement();
            }
            if (clsSym == null) {
                return null;
            }
            pkgName = FileObjects.convertPackage2Folder(((PackageElement) e).getQualifiedName().toString());
            pageName = sb.toString();
            buildFragment = element != clsSym;
        }

        if (clsSym.completer != null) {
            clsSym.complete();
        }
        if (clsSym.classfile != null) {
            try {
                final URL classFile = clsSym.classfile.toUri().toURL();
                final String pkgNameF = pkgName;
                final String pageNameF = pageName;
                final CharSequence fragment = buildFragment ? getFragment(element) : null;
                final Future<TextStream> future = RP.submit(new Callable<TextStream>() {
                    @Override
                    public TextStream call() throws Exception {
                        return findJavadoc(classFile, pkgNameF, pageNameF, fragment, allowRemoteJavadoc);
                    }
                });
                do {
                    if (cancel != null && cancel.call()) {
                        break;
                    }
                    try {
                        return future.get(100, TimeUnit.MILLISECONDS);
                    } catch (TimeoutException timeOut) {
                        //Retry
                    }
                } while (true);
            } catch (Exception e) {
                LOG.log(Level.INFO, null, e);
            }
        }
        return null;
    }

    private static final String PACKAGE_SUMMARY = "package-summary"; // NOI18N

    private static TextStream findJavadoc(
            final URL classFile,
            final String pkgName,
            final String pageName,
            final CharSequence fragment,
            final boolean allowRemoteJavadoc) {

        URL sourceRoot = null;
        Set<URL> binaries = new HashSet<URL>();
        try {
            FileObject fo = URLMapper.findFileObject(classFile);
            StringTokenizer tk = new StringTokenizer(pkgName, "/"); // NOI18N
            for (int i = 0; fo != null && i <= tk.countTokens(); i++) {
                fo = fo.getParent();
            }
            if (fo != null) {
                URL url = fo.getURL();
                sourceRoot = JavaIndex.getSourceRootForClassFolder(url);
                if (sourceRoot == null) {
                    binaries.add(url);
                } else {
                    // sourceRoot may be a class root in reality
                    binaries.add(sourceRoot);
                }
            }
            if (sourceRoot != null) {
                FileObject sourceFo = URLMapper.findFileObject(sourceRoot);
                if (sourceFo != null) {
                    ClassPath exec = ClassPath.getClassPath(sourceFo, ClassPath.EXECUTE);
                    ClassPath compile = ClassPath.getClassPath(sourceFo, ClassPath.COMPILE);
                    ClassPath source = ClassPath.getClassPath(sourceFo, ClassPath.SOURCE);
                    if (exec == null) {
                        exec = compile;
                        compile = null;
                    }
                    if (exec != null && source != null) {
                        Set<URL> roots = new HashSet<URL>();
                        for (ClassPath.Entry e : exec.entries()) {
                            roots.add(e.getURL());
                        }
                        if (compile != null) {
                            for (ClassPath.Entry e : compile.entries()) {
                                roots.remove(e.getURL());
                            }
                        }
                        List<FileObject> sourceRoots = Arrays.asList(source.getRoots());
                        out:
                        for (URL e : roots) {
                            FileObject[] res = SourceForBinaryQuery.findSourceRoots(e).getRoots();
                            for (FileObject r : res) {
                                if (sourceRoots.contains(r)) {
                                    binaries.add(e);
                                    continue out;
                                }
                            }
                        }
                    }
                }
            }

            for (URL binary : binaries) {
                JavadocForBinaryQuery.Result javadocResult = JavadocForBinaryQuery.findJavadoc(binary);
                URL[] result = javadocResult.getRoots();
                for (URL root : result) {
                    if (!root.toExternalForm().endsWith("/")) { // NOI18N
                        LOG.log(Level.WARNING, "JavadocForBinaryQuery.Result: {0} returned non-folder URL: {1}, ignoring",
                                new Object[] {javadocResult.getClass(), root.toExternalForm()});
                        continue;
                    }
                    if (!allowRemoteJavadoc && isRemote(root)) {
                        continue;
                    }
                    URL url = new URL(root, pkgName + "/" + pageName + ".html");
                    InputStream is = null;
                    String rootS = root.toString();
                    boolean useKnownGoodRoots = result.length == 1 && isRemote(url);
                    if (useKnownGoodRoots && knownGoodRoots.contains(rootS)) {
                        LOG.log(Level.FINE, "assumed valid Javadoc stream at {0}", url);
                    } else {
                        try {
                            is = openStream(url);
                            if (useKnownGoodRoots) {
                                knownGoodRoots.add(rootS);
                                LOG.log(Level.FINE, "found valid Javadoc stream at {0}", url);
                            }
                        } catch (IOException x) {
                            LOG.log(Level.FINE, "invalid Javadoc stream at {0}: {1}", new Object[] {url, x});
                            continue;
                        }
                    }
                    if (fragment != null && fragment.length() > 0) {
                        try {
                            // Javadoc fragments may contain chars that must be escaped to comply with RFC 2396.
                            // Unfortunately URLEncoder escapes almost everything but
                            // spaces replaces with '+' char which is wrong so it is
                            // replaced with "%20"escape sequence here.
                            String encodedfragment = URLEncoder.encode(fragment.toString(), "UTF-8"); // NOI18N
                            encodedfragment = encodedfragment.replace("+", "%20"); // NOI18N
                            return new TextStream(new URI(url.toExternalForm() + '#' + encodedfragment).toURL(), is);
                        } catch (URISyntaxException x) {
                            LOG.log(Level.INFO, null, x);
                        } catch (UnsupportedEncodingException x) {
                            LOG.log(Level.INFO, null, x);
                        } catch (MalformedURLException x) {
                            LOG.log(Level.INFO, null, x);
                        }
                    }
                    return new TextStream(url, is);
                }
            }

        } catch (MalformedURLException x) {
            LOG.log(Level.INFO, null, x);
        } catch (FileStateInvalidException x) {
            LOG.log(Level.INFO, null, x);
        }
        return null;
    }
    
    /**
     * {@code ElementJavadoc} currently will check every class in an API set if you keep on using code completion.
     * We do not want to make a new network connection each time, especially if src.zip supplies the Javadoc anyway.
     * Assume that if one class can be found, they all can.
     */
    private static final Set<String> knownGoodRoots = Collections.synchronizedSet(new HashSet<String>());

    private static CharSequence getFragment(Element e) {
        StringBuilder sb = new StringBuilder();
        if (!e.getKind().isClass() && !e.getKind().isInterface()) {
            if (e.getKind() == ElementKind.CONSTRUCTOR) {
                sb.append(e.getEnclosingElement().getSimpleName());
            } else {
                sb.append(e.getSimpleName());
            }
            if (e.getKind() == ElementKind.METHOD || e.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement ee = (ExecutableElement) e;
                sb.append('('); // NOI18N
                for (Iterator<? extends VariableElement> it = ee.getParameters().iterator(); it.hasNext();) {
                    VariableElement param = it.next();
                    appendType(sb, param.asType(), ee.isVarArgs() && !it.hasNext());
                    if (it.hasNext()) {
                        sb.append(", ");
                    }
                }
                sb.append(')'); // NOI18N
            }
        }
        return sb;
    }
    
    private static void appendType(StringBuilder sb, TypeMirror type, boolean varArg) {
        switch (type.getKind()) {
        case ARRAY:
            appendType(sb, ((ArrayType) type).getComponentType(), false);
            sb.append(varArg ? "..." : "[]"); // NOI18N
            break;
        case DECLARED:
            sb.append(((TypeElement) ((DeclaredType) type).asElement()).getQualifiedName());
            break;
        default:
            sb.append(type);
        }
    }

}
