/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.filesystems;

import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

import java.io.File;
import java.io.FileNotFoundException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


/** Mapper from FileObject -> URL.
 * Should be registered in default lookup. For details see {@link Lookup#getDefault()}.
 * For all methods, if the passed-in file object is the root folder
 * of some filesystem, then it is assumed that any valid file object
 * in that filesystem may also have a URL constructed for it by means
 * of appending the file object's resource path to the URL of the root
 * folder. If this cannot work for all file objects on the filesystem,
 * the root folder must not be assigned a URL of that type. nbfs: URLs
 * of course always work correctly in this regard.
 * @since 2.16
 */
public abstract class URLMapper {
    /**
     * URL which works inside this VM.
     * Not guaranteed to work outside the VM (though it may).
     */
    public static final int INTERNAL = 0;

    /**
     * URL which works inside this machine.
     * Not guaranteed to work from other machines (though it may).
     * <div class="nonnormative">
     * Typical protocols used: <code>file</code> for disk files (see {@link File#toURI});
     * <code>jar</code> to wrap other URLs (e.g. <samp>jar:file:/some/thing.jar!/some/entry</samp>).
     * </div>
     */
    public static final int EXTERNAL = 1;

    /** URL which works from networked machines.*/
    public static final int NETWORK = 2;

    /** results with URLMapper instances*/
    private static Lookup.Result result;
    private static final List CACHE_JUST_COMPUTING = new ArrayList();
    private static final ThreadLocal threadCache = new ThreadLocal();

    static {
        result = Lookup.getDefault().lookup(new Lookup.Template(URLMapper.class));
        result.addLookupListener(
            new LookupListener() {
                public void resultChanged(LookupEvent ev) {
                    synchronized (URLMapper.class) {
                        cache = null;
                    }
                }
            }
        );
    }

    /** Basic impl. for JarFileSystem, LocalFileSystem, MultiFileSystem */
    private static URLMapper defMapper;

    /** Cache of all available URLMapper instances. */
    private static List /*<URLMapper>*/ cache;

    /** Find a good URL for this file object which works according to type:
     * -inside this VM
     * - inside this machine
     * - from networked machines
     * @return a suitable URL, or null
     */
    public static URL findURL(FileObject fo, int type) {
        URL retVal;

        /** secondly registered URLMappers are asked to resolve URL */
        Iterator instances = getInstances().iterator();

        while (instances.hasNext()) {
            URLMapper mapper = (URLMapper) instances.next();

            if (mapper == getDefault()) {
                continue;
            }

            retVal = mapper.getURL(fo, type);

            if (retVal != null) {
                return retVal;
            }
        }

        /** first basic implementation */
        retVal = getDefault().getURL(fo, type);

        if (retVal != null) {
            return retVal;
        }

        /** if not resolved yet then internal URL with nbfs protocol is returned */
        if (type == INTERNAL) {
            try {
                retVal = FileURL.encodeFileObject(fo);
            } catch (FileStateInvalidException iex) {
                return null;
            }
        }

        return retVal;
    }

    /** Get a good URL for this file object which works according to type:
     * -inside this VM
     * - inside this machine
     * - from networked machines
     * The implementation can't use neither {@link FileUtil#toFile} nor {@link FileUtil#toFileObject}
     * otherwise StackOverflowError maybe thrown.
     * @return a suitable URL, or null
     */
    public abstract URL getURL(FileObject fo, int type);

    /** Find an array of FileObjects for this url
     * Zero or more FOs may be returned.
     *
     * For each returned FO, it must be true that FO -> URL gives the
     * exact URL which was passed in, but depends on appripriate type
     * <code> findURL(FileObject fo, int type) </code>.
     * @param url to wanted FileObjects
     * @return a suitable arry of FileObjects, or empty array if not successful
     * @since  2.22
     * @deprecated Use {@link #findFileObject} instead.
     */
    public static FileObject[] findFileObjects(URL url) {
        /** first basic implementation */
        Set retSet = new LinkedHashSet();
        FileObject[] retVal = null;

        Iterator instances = getInstances().iterator();

        while (instances.hasNext()) {
            URLMapper mapper = (URLMapper) instances.next();

            if (mapper == getDefault()) {
                continue;
            }

            retVal = mapper.getFileObjects(url);

            if (retVal != null) {
                retSet.addAll(Arrays.asList(retVal));
            }
        }

        retVal = getDefault().getFileObjects(url);

        if (retVal != null) {
            retSet.addAll(Arrays.asList(retVal));
        }

        retVal = new FileObject[retSet.size()];
        retSet.toArray(retVal);

        return retVal;
    }

    /** Find an appropiate instance of FileObject that addresses this url
     *
     * @param url url to be converted to file object
     * @return file object corresponding to url or null if no one was found
     * @since  4.29
     */
    public static FileObject findFileObject(URL url) {
        if (url == null) {
            throw new NullPointerException("Cannot pass null URL to URLMapper.findFileObject"); // NOI18N
        }

        /** first basic implementation */
        FileObject[] results = null;

        Iterator instances = getInstances().iterator();

        while (instances.hasNext() && ((results == null) || (results.length == 0))) {
            URLMapper mapper = (URLMapper) instances.next();

            if (mapper == getDefault()) {
                continue;
            }

            results = mapper.getFileObjects(url);
        }

        /** first basic implementation */
        if ((results == null) || (results.length == 0)) {
            results = getDefault().getFileObjects(url);
        }

        return ((results != null) && (results.length > 0)) ? results[0] : null;
    }

    /** Get an array of FileObjects for this url. There is no reason to return array
     * with size greater than one because method {@link #findFileObject findFileObject}
     * uses just first element (next elements won't be accepted anyway).
     * The implementation can't use neither {@link FileUtil#toFile} nor {@link FileUtil#toFileObject}
     * otherwise StackOverflowError maybe thrown.
     * <p class="nonnormative"> There isn't necessary to return array here.
     * The only one reason is just backward compatibility.</p>
     * @param url to wanted FileObjects
     * @return an array of FileObjects with size no greater than one, or null
     * @since  2.22*/
    public abstract FileObject[] getFileObjects(URL url);

    /** this method is expeceted to be invoked to create instance of URLMapper,
     * because of method invocation (attr name="instanceCreate"
     * methodvalue="org.openide.filesystems.URLMapper.getDefault")*/
    private static URLMapper getDefault() {
        synchronized (URLMapper.class) {
            if (defMapper == null) {
                defMapper = new DefaultURLMapper();
            }

            return defMapper;
        }
    }

    /** Returns all available instances of URLMapper.
     * @return list of URLMapper instances
     */
    private static List getInstances() {
        synchronized (URLMapper.class) {
            if (cache != null) {
                if ((cache != CACHE_JUST_COMPUTING) || (threadCache.get() == CACHE_JUST_COMPUTING)) {
                    return cache;
                }
            }

            // Set cache to empty array here to prevent infinite loop.
            // See issue #41358, #43359                    
            cache = CACHE_JUST_COMPUTING;
            threadCache.set(CACHE_JUST_COMPUTING);
        }

        ArrayList res = null;

        try {
            res = new ArrayList(result.allInstances());

            return res;
        } finally {
            synchronized (URLMapper.class) {
                if (cache == CACHE_JUST_COMPUTING) {
                    cache = res;
                }

                threadCache.set(null);
            }
        }
    }

    /*** Basic impl. for JarFileSystem, LocalFileSystem, MultiFileSystem */
    private static class DefaultURLMapper extends URLMapper {
        DefaultURLMapper() {
        }

        // implements  URLMapper.getFileObjects(URL url)
        public FileObject[] getFileObjects(URL url) {
            String prot = url.getProtocol();

            if (prot.equals("nbfs")) { //// NOI18N

                FileObject retVal = FileURL.decodeURL(url);

                return (retVal == null) ? null : new FileObject[] { retVal };
            }

            if (prot.equals("jar")) { //// NOI18N

                return getFileObjectsForJarProtocol(url);
            }

            if (prot.equals("file")) { //// NOI18N

                File f = toFile(url);

                if (f != null) {
                    FileObject[] foRes = findFileObjectsInRepository(f);

                    if ((foRes != null) && (foRes.length > 0)) {
                        return foRes;
                    }
                }
            }

            return null;
        }

        private FileObject[] findFileObjectsInRepository(File f) {
            if (!f.equals(FileUtil.normalizeFile(f))) {
                throw new IllegalArgumentException(
                    "Parameter file was not " + // NOI18N
                    "normalized. Was " + f + " instead of " + FileUtil.normalizeFile(f)
                ); // NOI18N
            }

            Enumeration en = Repository.getDefault().getFileSystems();
            LinkedList list = new LinkedList();
            String fileName = f.getAbsolutePath();

            while (en.hasMoreElements()) {
                FileSystem fs = (FileSystem) en.nextElement();
                String rootName = null;
                FileObject fsRoot = fs.getRoot();
                File root = findFileInRepository(fsRoot);

                if (root == null) {
                    Object rootPath = fsRoot.getAttribute("FileSystem.rootPath"); //NOI18N

                    if ((rootPath != null) && (rootPath instanceof String)) {
                        rootName = (String) rootPath;
                    } else {
                        continue;
                    }
                }

                if (rootName == null) {
                    rootName = root.getAbsolutePath();
                }

                /**root is parent of file*/
                if (fileName.indexOf(rootName) == 0) {
                    String res = fileName.substring(rootName.length()).replace(File.separatorChar, '/');
                    FileObject fo = fs.findResource(res);
                    File file2Fo = (fo != null) ? findFileInRepository(fo) : null;

                    if ((fo != null) && (file2Fo != null) && f.equals(file2Fo)) {
                        if (fo.getClass().toString().indexOf("org.netbeans.modules.masterfs.MasterFileObject") != -1) { //NOI18N
                            list.addFirst(fo);
                        } else {
                            list.addLast(fo);
                        }
                    }
                }
            }

            FileObject[] results = new FileObject[list.size()];
            list.toArray(results);

            return results;
        }

        // implements  URLMapper.getURL(FileObject fo, int type)
        public URL getURL(FileObject fo, int type) {
            return getURLBasicImpl(fo, type);
        }

        private static URL getURLBasicImpl(FileObject fo, int type) {
            if (fo == null) {
                return null;
            }

            if (type == NETWORK) {
                return null;
            }

            if (fo instanceof MultiFileObject && (type == INTERNAL)) {
                // Stick to nbfs protocol, otherwise URL calculations
                // get messed up. See #39613.
                return null;
            }

            File fFile = findFileInRepository(fo);

            if (fFile != null) {
                try {
                    return toURL(fFile, fo);
                } catch (MalformedURLException mfx) {
                    assert false : mfx;

                    return null;
                }
            }

            URL retURL = null;
            FileSystem fs = null;

            try {
                fs = fo.getFileSystem();
            } catch (FileStateInvalidException fsex) {
                return null;
            }

            if (fs instanceof JarFileSystem) {
                JarFileSystem jfs = (JarFileSystem) fs;
                File f = jfs.getJarFile();

                if (f == null) {
                    return null;
                }

                try {
                    // XXX fo.getPath() needs to be escaped
                    retURL = new URL(
                            "jar:" + f.toURI() + "!/" + fo.getPath() + // NOI18N
                            ((fo.isFolder() && !fo.isRoot()) ? "/" : "")
                        ); // NOI18N
                } catch (MalformedURLException mfx) {
                    mfx.printStackTrace();

                    return null;
                }
            } else if (fs instanceof XMLFileSystem) {
                URL retVal = null;

                try {
                    retVal = ((XMLFileSystem) fs).getURL(fo.getPath());

                    if (retVal == null) {
                        return null;
                    }

                    if (type == INTERNAL) {
                        return retVal;
                    }

                    boolean isInternal = retVal.getProtocol().startsWith("nbres"); //NOI18N

                    if ((type == EXTERNAL) && !isInternal) {
                        return retVal;
                    }

                    return null;
                } catch (FileNotFoundException fnx) {
                    return null;
                }
            }

            return retURL;
        }

        private static URL toURL(File fFile, FileObject fo)
        throws MalformedURLException {
            URL retVal = null;

            if (fo.isFolder() && !fo.isValid()) {
                String urlDef = fFile.toURI().toURL().toExternalForm();
                String pathSeparator = "/"; //NOI18N

                if (!urlDef.endsWith(pathSeparator)) {
                    retVal = new URL(urlDef + pathSeparator);
                }
            }

            return (retVal == null) ? fFile.toURI().toURL() : retVal;
        }

        private static File findFileInRepository(FileObject fo) {
            File f = (File) fo.getAttribute("java.io.File"); // NOI18N

            return (f != null) ? FileUtil.normalizeFile(f) : null;
        }

        private static FileObject[] getFileObjectsForJarProtocol(URL url) {
            FileObject retVal = null;
            JarURLParser jarUrlParser = new JarURLParser(url);
            File file = jarUrlParser.getJarFile();
            String entryName = jarUrlParser.getEntryName();

            if (file != null) {
                JarFileSystem fs = findJarFileSystem(file);

                if (fs != null) {
                    if (entryName == null) {
                        entryName = ""; // #39190: root of JAR
                    }

                    retVal = fs.findResource(entryName);
                }
            }

            return (retVal == null) ? null : new FileObject[] { retVal };
        }

        private static JarFileSystem findJarFileSystem(File jarFile) {
            JarFileSystem retVal = null;
            Enumeration en = Repository.getDefault().getFileSystems();

            while (en.hasMoreElements()) {
                FileSystem fs = (FileSystem) en.nextElement();

                if (fs instanceof JarFileSystem) {
                    File fsJarFile = ((JarFileSystem) fs).getJarFile();

                    if (fsJarFile.equals(jarFile)) {
                        retVal = (JarFileSystem) fs;

                        break;
                    }
                }
            }

            return retVal;
        }

        private static File toFile(URL u) {
            if (u == null) {
                throw new NullPointerException();
            }

            try {
                URI uri = new URI(u.toExternalForm());

                return FileUtil.normalizeFile(new File(uri));
            } catch (URISyntaxException use) {
                // malformed URL
                return null;
            } catch (IllegalArgumentException iae) {
                // not a file: URL
                return null;
            }
        }

        private static class JarURLParser {
            private File jarFile;
            private String entryName;

            JarURLParser(URL originalURL) {
                parse(originalURL);
            }

            /** copy & pasted from JarURLConnection.parse*/
            void parse(URL originalURL) {
                String spec = originalURL.getFile();

                int separator = spec.indexOf('!');

                if (separator != -1) {
                    try {
                        jarFile = toFile(new URL(spec.substring(0, separator++)));
                        entryName = null;
                    } catch (MalformedURLException e) {
                        return;
                    }

                    /* if ! is the last letter of the innerURL, entryName is null */
                    if (++separator != spec.length()) {
                        entryName = spec.substring(separator, spec.length());
                    }
                }
            }

            File getJarFile() {
                return jarFile;
            }

            String getEntryName() {
                return entryName;
            }
        }
    }
}
