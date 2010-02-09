/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import org.openide.util.Utilities;

/**
 * A ProxyClassLoader capable of loading classes from a set of jar files
 * and local directories.
 *
 * @author  Petr Nejedly
 */
public class JarClassLoader extends ProxyClassLoader {
    private static Stamps cache;
    static Archive archive = new Archive(); 

    static void initializeCache() {
        cache = Stamps.getModulesJARs();
        archive = new Archive(cache);
    }
    
    /**
     * Creates a new archive or updates existing archive with the necessary
     * resources gathered so far. It also stops gatheing and serving
     * additional request, if it was still doing so.
     */    
    public static void saveArchive() {
        if (cache != null) {
            try {
                archive.save(cache);
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, "saving archive", ioe);
            }
        } else {
            archive.stopGathering();
            archive.stopServing();
        }
    }
    
    static {
        ProxyURLStreamHandlerFactory.register();
    }
    
    private static final Logger LOGGER = Logger.getLogger(JarClassLoader.class.getName());

    private Source[] sources;
    private Module module;
    
    /** Creates new JarClassLoader.
     * Gives transitive flag as true.
     */
    public JarClassLoader(List<File> files, ClassLoader[] parents) {
        this(files, parents, true, null);
    }
    
    public JarClassLoader(List<File> files, ClassLoader[] parents, boolean transitive) {
        this(files, parents, transitive, null);
    }
    /** Creates new JarClassLoader.
     * @since org.netbeans.core/1 > 1.6
     * @see ProxyClassLoader#ProxyClassLoader(ClassLoader[],boolean)
     */
    public JarClassLoader(List<File> files, ClassLoader[] parents, boolean transitive, Module mod) {
        super(parents, transitive);
        this.module = mod;
        List<Source> l = new ArrayList<Source>(files.size());
        try {
            for (File file : files) {
                l.add(Source.create(file, this));
            }
        } catch (IOException exc) {
            throw new IllegalArgumentException(exc.getMessage());
        }
        sources = l.toArray(new Source[l.size()]);
        // overlaps with old packages doesn't matter,PCL uses sets.
        addCoveredPackages(getCoveredPackages(module, sources));
    }

    final void addURL(URL location) throws IOException, URISyntaxException {
        File f = new File(location.toURI());
        assert f.exists() : "URL must be existing local file: " + location;

        List<Source> arr = new ArrayList<Source>(Arrays.asList(sources));
        arr.add(new JarSource(f));

        synchronized (sources) {
            sources = arr.toArray(new Source[arr.size()]);
        }

        // overlaps with old packages doesn't matter,PCL uses sets.
        addCoveredPackages(getCoveredPackages(module, sources));
    }

    /** Allows to specify the right permissions, OneModuleClassLoader does it differently.
     */
    protected PermissionCollection getPermissions( CodeSource cs ) {           
        return Policy.getPolicy().getPermissions(cs);       
    }        
    
    
    protected Package definePackage(String name, Manifest man, URL url)
	throws IllegalArgumentException
    {
        if (man == null ) {
            return definePackage(name, null, null, null, null, null, null, null);
        }
        
	String path = name.replace('.', '/').concat("/"); // NOI18N
	Attributes spec = man.getAttributes(path);
        Attributes main = man.getMainAttributes();
	
        String specTitle = getAttr(spec, main, Name.SPECIFICATION_TITLE);
        String implTitle = getAttr(spec, main, Name.IMPLEMENTATION_TITLE);
        String specVersion = getAttr(spec, main, Name.SPECIFICATION_VERSION);
        String implVersion = getAttr(spec, main, Name.IMPLEMENTATION_VERSION);
        String specVendor = getAttr(spec, main, Name.SPECIFICATION_VENDOR);
        String implVendor = getAttr(spec, main, Name.IMPLEMENTATION_VENDOR);
        String sealed      = getAttr(spec, main, Name.SEALED);

        URL sealBase = "true".equalsIgnoreCase(sealed) ? url : null; // NOI18N
	return definePackage(name, specTitle, specVersion, specVendor,
			     implTitle, implVersion, implVendor, sealBase);
    }

    private static String getAttr(Attributes spec, Attributes main, Name name) {
        String val = null;
        if (spec != null) val = spec.getValue (name);
        if (val == null && main != null) val = main.getValue (name);
        return val;
    }

    private Boolean patchingBytecode;
    @Override
    protected Class doLoadClass(String pkgName, String name) {
        String path = name.replace('.', '/').concat(".class"); // NOI18N
        
        // look up the Sources and return a class based on their content
        for( int i=0; i<sources.length; i++ ) {
            Source src = sources[i];
            byte[] data = src.getClassData(path);
            if (data == null) continue;

            synchronized (sources) {
                if (patchingBytecode == null) {
                    patchingBytecode = findResource("META-INF/.bytecodePatched") != null; // NOI18N
                    if (patchingBytecode) {
                        LOGGER.log(Level.FINE, "Patching bytecode in {0}", this);
                    }
                }
            }
            if (patchingBytecode) {
                try {
                    data = PatchByteCode.patch(data);
                } catch (Exception x) {
                    LOGGER.log(Level.INFO, "Could not bytecode-patch " + name, x);
                }
            }
            
            // Note that we assume that if we are defining a class in this package,
            // we should also define the package! Thus recurse==false.
            // However special packages might be defined in a parent and then we want
            // to have the same Package object, proper sealing check, etc.; so be safe,
            // overhead is probably small (check in parents, nope, check super which
            // delegates to system loaders).
            Package pkg = getPackageFast(pkgName, true);
            if (pkg != null) {
                // XXX full sealing check, URLClassLoader does something more
                if (pkg.isSealed() && !pkg.isSealed(src.getURL())) throw new SecurityException("sealing violation"); // NOI18N
            } else {
                Manifest man = module == null || src != sources[0] ? src.getManifest() : module.getManifest();
                try {
                    definePackage(pkgName, man, src.getURL());
                } catch (IllegalArgumentException x) {
                    // #156478: possibly a race condition defining packages in parallel parents? Ignore.
                    LOGGER.log(Level.FINE, null, x);
                }
            }

            return defineClass (name, data, 0, data.length, src.getProtectionDomain());
        } 
        return null;
    }
    // look up the jars and return a resource based on a content of jars
    @Override
    public URL findResource(String name) {
        for( int i=0; i<sources.length; i++ ) {
            URL item = sources[i].getResource(name);
            if (item != null) return item;
        }
	return null;
    }

    @Override
    public Enumeration<URL> findResources(String name) {
        Vector<URL> v = new Vector<URL>(3);
        // look up the jars and return a resource based on a content of jars

        for( int i=0; i<sources.length; i++ ) {
            URL item = sources[i].getResource(name);
            if (item != null) v.add(item);
        }
        return v.elements();
    }
    
    public @Override void destroy() {
        super.destroy ();
        for (Source src : sources) {
            try {
                src.destroy();
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, "could not destroy " + src, ioe);
            }
        }
    }

    /** package-private method useful only for testing.
     * Used from JarClassLoaderTest to force close before reopening. */
    void releaseJars() throws IOException {
        for (Source src : sources) {
            if (src instanceof JarSource) {
                ((JarSource)src).doCloseJar();
            }
        }
    }

    static abstract class Source {
        private URL url;
        private ProtectionDomain pd;
        protected JarClassLoader jcl;
        private static Map<String,Source> sources = new HashMap<String, Source>();
        
        public Source(URL url) {
            this.url = url;
        }
        
        public final URL getURL() {
            return url;
        }

        public abstract String getPath();
        
        public final ProtectionDomain getProtectionDomain() {
            if (pd == null) {
                CodeSource cs = new CodeSource(url, (Certificate[])null);
                pd = new ProtectionDomain(cs, jcl.getPermissions(cs));
            }
            return pd;
        }
  
        public final URL getResource(String name) {
            try {
                return doGetResource(name);
            } catch (Exception e) {
                // can't get the resource. E.g. already closed JarFile
                LOGGER.log(Level.FINE, null, e);
            }
            return null;
        }
        
        protected abstract URL doGetResource(String name) throws IOException;
        
        public final byte[] getClassData(String path) {
            try {
                return readClass(path);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "looking up " + path, e);
            }
            return null;
        }

        protected abstract byte[] readClass(String path) throws IOException;

        public Manifest getManifest() {
            return null;
        }

        protected abstract void listCoveredPackages(Set<String> known, StringBuffer save);
        
        protected void destroy() throws IOException {
            // relatively slow (millis instead of micros),
            // but rare enough to not matter
            sources.values().remove(this);
        }
        
        static Source create(File f, JarClassLoader jcl) throws IOException {
            boolean directory;
            if (f.getName().endsWith("jar")) {
                directory = false;
            } else {
                directory = f.isDirectory();
            }
            Source src = directory ? new DirSource(f) : new JarSource(f);
            src.jcl = jcl;
            // should better use the same string as other indexes
            // this way, there are currently 3 similar long Strings per
            // JarClassLoader instance - its URL, its identifier
            // in Archive.sources map and this one
            sources.put(src.getPath(), src);
            return src;
        }

        @Override
        public String toString() {
            return url.toString();
        }

    }

    static class JarSource extends Source {
        private String resPrefix;
        private File file;

        private Future<JarFile> fjar;
        private boolean dead;
        private int requests;
        private int used;
        /** #141110: expensive to repeatedly look for them */
        private final Set<String> nonexistentResources = Collections.synchronizedSet(new HashSet<String>());
        
        JarSource(File file) throws IOException {
            this(file, toURI(file));
        }
        private JarSource(File file, String resPrefix) throws IOException {
            super(new URL(resPrefix)); // NOI18N
            this.resPrefix = resPrefix; // NOI18N;
            this.file = file;
        }

        public String getPath() {
            return file.getPath();
        }

        private static String toURI(final File file) {
            class VFile extends File {
                public VFile() {
                    super(file.getPath());
                }

                @Override
                public boolean isDirectory() {
                    return false;
                }

                @Override
                public File getAbsoluteFile() {
                    return this;
                }
            }
            return "jar:" + new VFile().toURI() + "!/"; // NOI18N
        }

        @Override
        public Manifest getManifest() {
            try {
                byte[] arr = archive.getData(this, "META-INF/MANIFEST.MF");
                if (arr == null) {
                    return null;
                }
                return new Manifest(new ByteArrayInputStream(arr));
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Cannot read manifest for " + getPath(), ex);
                return null;
            }
        }
        
        JarFile getJarFile(final String forWhat) throws IOException {
            FutureTask<JarFile> init = null;
            synchronized(sources) {
                requests++;
                used++;
                if (fjar == null) {
                    fjar = sources.get(this);
                    if (fjar == null) {
                        fjar = init = new FutureTask<JarFile>(new Callable<JarFile>() {
                            public JarFile call() throws IOException {
                                long now = System.currentTimeMillis();
                                JarFile ret = new JarFile(file, false);
                                long took = System.currentTimeMillis() - now;
                                opened(JarClassLoader.JarSource.this, forWhat);
                                if (took > 500) {
                                    LOGGER.log(Level.WARNING, "Opening " + file + " took " + took + " ms"); // NOI18N
                                }
                                return ret;
                            }
                        });
                        sources.put(this, fjar);
                    }
                }
            }
            if (init != null) init.run();
            return callGet();
        }
        
        private void releaseJarFile() {
            synchronized(sources) {
                assert used > 0;
                used--;
            }
        }
        
        
        protected URL doGetResource(String name) throws IOException  {
            byte[] buf = archive.getData(this, name);
            if (buf == null) return null;
            LOGGER.log(Level.FINER, "Loading {0} from {1}", new Object[] {name, file.getPath()});
            try {
                return new URL(resPrefix + new URI(null, name, null).getRawPath());
            } catch (URISyntaxException x) {
                throw (IOException) new IOException(name + " in " + resPrefix + ": " + x.toString()).initCause(x);
            }
        }
        
        protected byte[] readClass(String path) throws IOException {
            return archive.getData(this, path);
        }
        
        public byte[] resource(String path) throws IOException {
            if (nonexistentResources.contains(path)) {
                return null;
            }
            JarFile jf;
            try {
                jf = getJarFile(path);
            } catch (ZipException ex) {
                LOGGER.log(Level.INFO, "Cannot open " + file, ex);
                return null;
            }
            try {
                ZipEntry ze = jf.getEntry(path);
                if (ze == null) {
                    nonexistentResources.add(path);
                    return null;
                }

                LOGGER.log(Level.FINER, "Loading {0} from {1}", new Object[] {path, file.getPath()});
            
                int len = (int)ze.getSize();
                byte[] data = new byte[len];
                InputStream is = jf.getInputStream(ze);
                int count = 0;
                while (count < len) {
                    count += is.read(data, count, len-count);
                }
                return data;
            } finally {
                releaseJarFile();
            }
        }


        protected void listCoveredPackages(Set<String> known, StringBuffer save) {
            try {
                JarFile src = getJarFile("pkg");

                Enumeration<JarEntry> en = src.entries();
                while (en.hasMoreElements()) {
                    JarEntry je = en.nextElement();
                    if (! je.isDirectory()) {
                        String itm = je.getName();
                        int slash = itm.lastIndexOf('/');
                        String pkg = slash > 0 ? itm.substring(0, slash).replace('/','.') : "";
                        if (known.add(pkg)) save.append(pkg).append(',');
                        if (itm.startsWith("META-INF/")) {
                                String res = itm.substring(8); // "/services/pkg.Service"
                                if (known.add(res)) save.append(res).append(',');
                        }
                    }
                }
            } catch (ZipException x) { // Unix
                LOGGER.log(Level.INFO, "Cannot open " + file, x);
            } catch (FileNotFoundException x) { // Windows
                LOGGER.log(Level.INFO, "Cannot open " + file, x);
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, "problems with " + file, ioe);
            } finally {
                releaseJarFile();
            }
        }

        
        @Override
        protected void destroy() throws IOException {
            super.destroy();
            assert dead == false : "Already had dead JAR: " + file;
            
            File orig = file;

            if (!orig.isFile()) {
                // Can happen when a test module is deleted:
                // the physical JAR has already been deleted
                // when the module was disabled. In this case it
                // is possible that a classloader request for something
                // in the JAR could still come in. Does it matter?
                // See comment in Module.cleanup.
                return;
            }
            
            String name = orig.getName();
            String prefix, suffix;
            int idx = name.lastIndexOf('.');
            if (idx == -1) {
                prefix = name;
                suffix = null;
            } else {
                prefix = name.substring(0, idx);
                suffix = name.substring(idx);
            }
            
            while (prefix.length() < 3) prefix += "x"; // NOI18N
            File temp = File.createTempFile(prefix, suffix);
            temp.deleteOnExit();

            InputStream is = new FileInputStream(orig);
            try {
                OutputStream os = new FileOutputStream(temp);
                try {
                    byte[] buf = new byte[4096];
                    int j;
                    while ((j = is.read(buf)) != -1) {
                        os.write(buf, 0, j);
                    }
                } finally {
                    os.close();
                }
            } finally {
                is.close();
            }
 
            doCloseJar();
            file = temp;
            dead = true;
            LOGGER.log(Level.FINE, "#21114: replacing {0} with {1}", new Object[] {orig, temp});
        }
        
        private JarFile callGet() throws IOException {
            for (;;) {
                try {
                    return fjar.get();
                } catch (InterruptedException ex) {
                    // ignore and retry
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    if (cause instanceof IOException) {
                        // This is important for telling general IOException from ZipException
                        // down the stack.
                        throw (IOException)cause;
                    } else {
                        throw (IOException)new IOException().initCause(cause);
                    }
                }
            }
        }

        private void doCloseJar() throws IOException {
            JarFile jar = null;
            synchronized(sources) {
                if (fjar != null) {
                    jar = callGet();
                    if (sources.remove(this) == null) {
                        LOGGER.warning("Can't remove " + this);
                    }
                    LOGGER.log(Level.FINE, "Closing JAR {0}", jar.getName());
                    fjar = null;
                    LOGGER.log(Level.FINE, "Remaining open JARs: {0}", sources.size());
                }
            }
            if (jar != null) jar.close();
        }

        /** Delete any temporary JARs we were holding on to.
         * Also close any other JARs in our list.
         */
        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            
            doCloseJar();

            if (dead) {
                LOGGER.log(Level.FINE, "#21114: closing and deleting temporary JAR {0}", file);
                if (file.isFile() && !file.delete()) {
                    LOGGER.log(Level.FINE, "(but failed to delete {0})", file);
                }
            }
        }

        // JarFile pool tracking
        private static final Map<JarSource, Future<JarFile>> sources = new HashMap<JarSource, Future<JarFile>>();
        private static int LIMIT = Integer.getInteger("org.netbeans.JarClassLoader.limit_fd", 300);

        static void opened(JarSource source, String forWhat) {
            synchronized (sources) {
                if (sources.size() > LIMIT) {
                    // close something
                    JarSource toClose = toClose();
                    try {
                        toClose.doCloseJar();
                    } catch (IOException ioe) {
                        LOGGER.log(Level.INFO, "closing " + toClose, ioe);
                    }
                }
                LOGGER.log(Level.FINE, "Opening module JAR {0} for {1}", new Object[] {source.file, forWhat});
                LOGGER.log(Level.FINE, "Currently open JARs: {0}", sources.size());
            }
        }

        // called under lock(sources) 
        private static JarSource toClose() { 
            assert Thread.holdsLock(sources);
             
            int min = Integer.MAX_VALUE; 
            JarSource candidate = null; 
            for (JarSource act : sources.keySet()) {
                // aging: slight exponential decay of all opened sources?
                act.requests = 5*act.requests/6;
                
                if (act.used > 0) continue;
                if (act.requests < min) { 
                    min = act.requests; 
                    candidate = act; 
                } 
            } 
             
            assert candidate != null; 
            return candidate; 
        }

        public String getIdentifier() {
            return getURL().toExternalForm();
        }
    }

    static class DirSource extends Source {
        File dir;
        
        DirSource(File file) throws MalformedURLException {
            super(file.toURI().toURL());
            dir = file;
        }
        
        public String getPath() {
            return dir.getPath();
        }

        protected URL doGetResource(String name) throws MalformedURLException {
            File resFile = new File(dir, name);
            return resFile.exists() ? resFile.toURI().toURL() : null;
        }
        
        protected byte[] readClass(String path) throws IOException {
            File clsFile = new File(dir, path.replace('/', File.separatorChar));
            if (!clsFile.exists()) return null;
            
            int len = (int)clsFile.length();
            byte[] data = new byte[len];
            InputStream is = new FileInputStream(clsFile);
            try {
                int count = 0;
                while (count < len) {
                    count += is.read(data, count, len - count);
                }
                return data;
            } finally {
                is.close();
            }
        }
        
        protected void listCoveredPackages(Set<String> known, StringBuffer save) {
            appendAllChildren(known, save, dir, "");
        }
        
        private static void appendAllChildren(Set<String> known, StringBuffer save, File dir, String prefix) {
            boolean populated = false;
            for (File f : dir.listFiles()) {
                if (f.isDirectory()) {
                    appendAllChildren(known, save, f, prefix + f.getName() + '.');
                } else {
                    populated = true;
                    if (prefix.startsWith("META-INF.")) {
                       String res = prefix.substring(8).replace('.', '/').concat(f.getName());
                       if (known.add(res)) save.append(res).append(',');
                    }
                }
            }
            if (populated) {
                String pkg = prefix;
                if (pkg.endsWith(".")) pkg = pkg.substring(0, pkg.length()-1);
                if (known.add(pkg)) save.append(pkg).append(',');
            }
        }
    }
    
    private static Iterable<String> getCoveredPackages(Module mod, Source[] sources) {
        Set<String> known = new HashSet<String>();
        Manifest m = mod == null ? null : mod.getManifest();
        if (m != null) {
            Attributes attr = m.getMainAttributes();
            String pack = attr.getValue("Covered-Packages");
            if (pack != null) {
                known.addAll(Arrays.asList(pack.split(",", -1)));
                return known;
            }
        }
        
        // not precomputed/cached, analyze
        StringBuffer save = new StringBuffer();
        for (Source s : sources) s.listCoveredPackages(known, save);

        if (save.length() > 0) save.setLength(save.length()-1);
        if (m != null) {
            Attributes attr = m.getMainAttributes();
            attr.putValue("Covered-Packages", save.toString());
        }
        return known;
    }
    
    /**
     * URLStreamHandler for res protocol
     */
    static class ResURLStreamHandler extends URLStreamHandler {

        private final URLStreamHandler originalJarHandler;

        ResURLStreamHandler(URLStreamHandler originalJarHandler) {
            this.originalJarHandler = originalJarHandler;
        }

        /**
         * Creates URLConnection for URL with res protocol.
         * @param u URL for which the URLConnection should be created
         * @return URLConnection
         * @throws IOException
         */
        protected JarURLConnection openConnection(URL u) throws IOException {
            String url = u.getFile();//toExternalForm();
            int bang = url.indexOf("!/");
            if (bang == -1) {
                throw new IOException("Malformed JAR-protocol URL: " + u);
            }
            int from;
            if (url.startsWith("file:")) {
                from = Utilities.isWindows() ? 6 : 5;
            } else {
                from = 0;
            }
            String jar = url.substring(from, bang).replace('/', File.separatorChar);
            Source _src = Source.sources.get(jar);
            if (_src == null) {
                try {
                    Method m = URLStreamHandler.class.getDeclaredMethod("openConnection", URL.class);
                    m.setAccessible(true);
                    return (JarURLConnection) m.invoke(originalJarHandler, u);
                } catch (Exception e) {
                    throw (IOException) new IOException(e.toString()).initCause(e);
                }
            }
            String _name = url.substring(bang + 2);
            try {
                _name = new URI(_name).getPath();
            } catch (URISyntaxException x) {
                throw (IOException) new IOException("Decoding " + u + ": " + x).initCause(x);
            }
            return new ResURLConnection (u, _src, _name);
        }

        @Override
        protected void parseURL(URL u, String spec, int start, int limit) {
            if (spec.startsWith("/")) {
                setURL(u, "jar", null, 0, null, null, u.getFile().replaceFirst("!/.+$", "!" + spec), u.getQuery(), u.getRef()); // NOI18N
            } else {
                super.parseURL(u, spec, start, limit);
            }
        }

    }

    /** URLConnection for URL with res protocol.
     *
     */
    private static class ResURLConnection extends JarURLConnection {
        private JarSource src;
        private final String name;
        private byte[] data;
        private InputStream iStream;

        /**
         * Creates new URLConnection
         * @param url the parameter for which the connection should be
         * created
         */
        private ResURLConnection(URL url, Source src, String name) throws MalformedURLException {
            super(url);
            this.src = (JarSource)src;
            this.name = name;
        }

        private boolean isFolder() {
            return name.length() == 0 || name.endsWith("/");
        }

        public void connect() throws IOException {
            if (isFolder()) {
                return; // #139087: odd but harmless
            }
            if (data == null) {
                data = src.getClassData(name);
                if (data == null) {
                    throw new FileNotFoundException(getURL().toString());
                }
            }
        }

        @Override
        public long getLastModified() {
            return Stamps.getModulesJARs().lastModified();
        }

        @Override
        public String getContentType() {
            String contentType = guessContentTypeFromName(name);
            if (contentType == null) {
                contentType = "content/unknown";
            }
            return contentType;
        }

        public @Override int getContentLength() {
            if (isFolder()) {
                return -1;
            }
            try {
                this.connect();
                return data.length;
            } catch (IOException e) {
                return -1;
            }
        }


        public @Override InputStream getInputStream() throws IOException {
            if (isFolder()) {
                throw new IOException("Cannot open a folder"); // NOI18N
            }
            this.connect();
            if (iStream == null) iStream = new ByteArrayInputStream(data);
            return iStream;
        }

        @Override
        public JarFile getJarFile() throws IOException {
            return new JarFile(src.file); // #134424
        }
    }
}
