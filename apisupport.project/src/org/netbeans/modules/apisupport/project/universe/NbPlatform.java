/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.universe;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.netbeans.modules.apisupport.project.*;
import org.openide.util.NbBundle;

/**
 * Represents one NetBeans platform, i.e. installation of the NB platform or IDE
 * or some derivative product.
 * Has a code id and can have associated sources and Javadoc, just like e.g. Java platforms.
 * 
 * @author Jesse Glick
 */
public final class NbPlatform implements Comparable {
    
    private static final String PLATFORM_PREFIX = "nbplatform."; // NOI18N
    private static final String PLATFORM_DEST_DIR_SUFFIX = ".netbeans.dest.dir"; // NOI18N
    private static final String PLATFORM_LABEL_SUFFIX = ".label"; // NOI18N
    private static final String PLATFORM_SOURCES_SUFFIX = ".sources"; // NOI18N
    private static final String PLATFORM_JAVADOC_SUFFIX = ".javadoc"; // NOI18N
    private static final String PLATFORM_HARNESS_DIR_SUFFIX = ".harness.dir"; // NOI18N
    private static final String PLATFORM_ID_DEFAULT = "default"; // NOI18N
    
    private static SortedSet/*<NbPlatform>*/ platforms;

    /**
     * Reset cached info so unit tests can start from scratch.
     */
    public static void reset() {
        platforms = null;
    }
    
    /**
     * Get a sorted set of all registered platforms.
     */
    public static SortedSet/*<NbPlatform>*/ getPlatforms() {
        if (platforms == null) {
            platforms = new TreeSet();
            EditableProperties p = PropertyUtils.getGlobalProperties();
            Iterator keys = p.keySet().iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                if (key.startsWith(PLATFORM_PREFIX) && key.endsWith(PLATFORM_DEST_DIR_SUFFIX)) {
                    String id = key.substring(PLATFORM_PREFIX.length(), key.length() - PLATFORM_DEST_DIR_SUFFIX.length());
                    String label = p.getProperty(PLATFORM_PREFIX + id + PLATFORM_LABEL_SUFFIX);
                    String destdir = p.getProperty(key);
                    String sources = p.getProperty(PLATFORM_PREFIX + id + PLATFORM_SOURCES_SUFFIX);
                    String javadoc = p.getProperty(PLATFORM_PREFIX + id + PLATFORM_JAVADOC_SUFFIX);
                    platforms.add(new NbPlatform(id, label, FileUtil.normalizeFile(new File(destdir)), findURLs(sources), findURLs(javadoc)));
                }
            }
        }
        return platforms;
    }

    /**
     * Get the default platform.
     * @return the default platform, if there is one (usually should be)
     */
    public static NbPlatform getDefaultPlatform() {
        return NbPlatform.getPlatformByID(PLATFORM_ID_DEFAULT);
    }
    
    /**
     * Find a platform by its ID.
     * @param id an ID (as in {@link #getID})
     * @return the platform with that ID, or null
     */
    public static NbPlatform getPlatformByID(String id) {
        Iterator it = getPlatforms().iterator();
        while (it.hasNext()) {
            NbPlatform p = (NbPlatform) it.next();
            if (p.getID().equals(id)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Find a platform by its installation directory.
     * If there is a registered platform for that directory, returns it.
     * Otherwise will create an anonymous platform ({@link #getID} will be null).
     * An anonymous platform might have sources associated with it;
     * currently this will be true in case the dest dir is nbbuild/netbeans/ inside a netbeans.org checkout.
     * @param the installation directory (as in {@link #getDestDir})
     * @return the platform with that destination directory
     */
    public static NbPlatform getPlatformByDestDir(File destDir) {
        Iterator it = getPlatforms().iterator();
        while (it.hasNext()) {
            NbPlatform p = (NbPlatform) it.next();
            if (p.getDestDir().equals(destDir)) {
                return p;
            }
        }
        URL[] sources = new URL[0];
        if (destDir.getName().equals("netbeans")) { // NOI18N
            File parent = destDir.getParentFile();
            if (parent != null && parent.getName().equals("nbbuild")) { // NOI18N
                File superparent = parent.getParentFile();
                if (superparent != null && ModuleList.isNetBeansOrg(superparent)) {
                    sources = new URL[] {Util.urlForDir(superparent)};
                }
            }
        }
        // XXX might also check OpenProjectList for NbModuleProject's and/or SuiteProject's with a matching
        // dest dir and look up property 'sources' to use; TBD whether Javadoc could also be handled in a
        // similar way
        return new NbPlatform(null, null, destDir, sources, new URL[0]);
    }
    
    public static void addPlatform(final String id, final File destdir, final String label) throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws IOException {
                    EditableProperties props = PropertyUtils.getGlobalProperties();
                    props.setProperty(PLATFORM_PREFIX + id + PLATFORM_DEST_DIR_SUFFIX, destdir.getAbsolutePath());
                    props.setProperty(PLATFORM_PREFIX + id + PLATFORM_HARNESS_DIR_SUFFIX,
                            "${nbplatform." + id + ".netbeans.dest.dir}/harness"); // NOI18N
                    props.setProperty(PLATFORM_PREFIX + id + PLATFORM_LABEL_SUFFIX, label);
                    PropertyUtils.putGlobalProperties(props);
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
        getPlatforms().add(new NbPlatform(id, label, FileUtil.normalizeFile(destdir),
                findURLs(null), findURLs(null)));
    }
    
    public static void removePlatform(final NbPlatform plaf) throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws IOException {
                    EditableProperties props = PropertyUtils.getGlobalProperties();
                    props.remove(PLATFORM_PREFIX + plaf.getID() + PLATFORM_DEST_DIR_SUFFIX);
                    props.remove(PLATFORM_PREFIX + plaf.getID() + PLATFORM_HARNESS_DIR_SUFFIX);
                    props.remove(PLATFORM_PREFIX + plaf.getID() + PLATFORM_LABEL_SUFFIX);
                    props.remove(PLATFORM_PREFIX + plaf.getID() + PLATFORM_SOURCES_SUFFIX);
                    props.remove(PLATFORM_PREFIX + plaf.getID() + PLATFORM_JAVADOC_SUFFIX);
                    PropertyUtils.putGlobalProperties(props);
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
        getPlatforms().remove(plaf);
    }
    
    private final String id;
    private String label;
    private File nbdestdir;
    private URL[] sourceRoots;
    private URL[] javadocRoots;
    private List/*<ModuleList>*/ listsForSources;
    
    private NbPlatform(String id, String label, File nbdestdir, URL[] sources, URL[] javadoc) {
        this.id = id;
        this.label = label;
        this.nbdestdir = nbdestdir;
        this.sourceRoots = sources;
        this.javadocRoots = javadoc;
    }
    
    private static URL[] findURLs(String path) {
        if (path == null) {
            return new URL[0];
        }
        String[] pieces = PropertyUtils.tokenizePath(path);
        URL[] urls = new URL[pieces.length];
        for (int i = 0; i < pieces.length; i++) {
            // XXX perhaps also support http: URLs somehow?
            urls[i] = Util.urlForDirOrJar(FileUtil.normalizeFile(new File(pieces[i])));
        }
        return urls;
    }
    
    /**
     * Get a unique ID for this platform.
     * Used e.g. in <code>nbplatform.active</code> in <code>platform.properties</code>.
     * @return a unique ID, or null for anonymous platforms
     */
    public String getID() {
        return id;
    }
    
    /**
     * Check if this is the default platform.
     * @return true for the one default platform
     */
    public boolean isDefault() {
        return id.equals(PLATFORM_ID_DEFAULT);
    }

    /**
     * Get a display label suitable for the user.
     * If not set, {@link #computeDisplayName} is used.
     * The {@link #isDefault default platform} is specially marked.
     * @return a display label
     */
    public String getLabel() {
        if (label == null) {
            try {
                label = isValid() ? computeDisplayName(nbdestdir) :
                    NbBundle.getMessage(NbPlatform.class, "MSG_InvalidPlatform",  // NOI18N
                        getDestDir().getAbsolutePath());
            } catch (IOException e) {
                Util.err.notify(ErrorManager.INFORMATIONAL, e);
                label = nbdestdir.getAbsolutePath();
            }
        }
        if (isDefault()) {
            return label + " (Default)"; // XXX I18N
        } else {
            return label;
        }
    }

    /**
     * Get the installation directory.
     * @return the installation directory
     */
    public File getDestDir() {
        return nbdestdir;
    }
    
    public void setDestDir(File destdir) {
        this.nbdestdir = destdir;
        // XXX write build.properties too
    }
    
    /**
     * Get associated source roots for this platform.
     * Each root could be a netbeans.org CVS checkout or a module suite project directory.
     * @return a list of source root URLs (may be empty but not null)
     */
    public URL[] getSourceRoots() {
        return sourceRoots;
    }

    /**
     * Add given source root to the current source root list and save the
     * result into the global properties in the <em>userdir</em> (see {@link
     * PropertyUtils#putGlobalProperties})
     */
    public void addSourceRoot(URL root) throws IOException {
        URL[] newSourceRoots = new URL[sourceRoots.length + 1];
        System.arraycopy(sourceRoots, 0, newSourceRoots, 0, sourceRoots.length);
        newSourceRoots[sourceRoots.length] = root;
        setSourceRoots(newSourceRoots);
    }
    
    /**
     * Remove given source roots from the current source root list and save the
     * result into the global properties in the <em>userdir</em> (see {@link
     * PropertyUtils#putGlobalProperties})
     */
    public void removeSourceRoots(URL[] urlsToRemove) throws IOException {
        Collection newSources = new ArrayList(Arrays.asList(sourceRoots));
        newSources.removeAll(Arrays.asList(urlsToRemove));
        URL[] sources = new URL[newSources.size()];
        setSourceRoots((URL[]) newSources.toArray(sources));
    }
    
    public void moveSourceRootUp(int indexToUp) throws IOException {
        if (indexToUp <= 0) {
            return; // nothing needs to be done
        }
        URL[] newSourceRoots = new URL[sourceRoots.length];
        System.arraycopy(sourceRoots, 0, newSourceRoots, 0, sourceRoots.length);
        newSourceRoots[indexToUp - 1] = sourceRoots[indexToUp];
        newSourceRoots[indexToUp] = sourceRoots[indexToUp - 1];
        setSourceRoots(newSourceRoots);
    }
    
    public void moveSourceRootDown(int indexToDown) throws IOException {
        if (indexToDown >= (sourceRoots.length - 1)) {
            return; // nothing needs to be done
        }
        URL[] newSourceRoots = new URL[sourceRoots.length];
        System.arraycopy(sourceRoots, 0, newSourceRoots, 0, sourceRoots.length);
        newSourceRoots[indexToDown + 1] = sourceRoots[indexToDown];
        newSourceRoots[indexToDown] = sourceRoots[indexToDown + 1];
        setSourceRoots(newSourceRoots);
    }
    
    public void setSourceRoots(URL[] roots) throws IOException {
        putGlobalProperty(
                PLATFORM_PREFIX + getID() + PLATFORM_SOURCES_SUFFIX,
                urlsToAntPath(roots));
        sourceRoots = roots;
        listsForSources = null;
    }
    
    /**
     * Get associated Javadoc roots for this platform.
     * Each root may contain some Javadoc sets in the usual format as subdirectories,
     * where the subdirectory is named acc. to the code name base of the module it
     * is documenting (using '-' in place of '.').
     * @return a list of Javadoc root URLs (may be empty but not null)
     */
    public URL[] getJavadocRoots() {
        return javadocRoots;
    }

    /**
     * Add given javadoc root to the current javadoc root list and save the
     * result into the global properties in the <em>userdir</em> (see {@link
     * PropertyUtils#putGlobalProperties})
     */
    public void addJavadocRoot(URL root) throws IOException {
        URL[] newJavadocRoots = new URL[javadocRoots.length + 1];
        System.arraycopy(javadocRoots, 0, newJavadocRoots, 0, javadocRoots.length);
        newJavadocRoots[javadocRoots.length] = root;
        setJavadocRoots(newJavadocRoots);
    }
    
    /**
     * Remove given javadoc roots from the current javadoc root list and save
     * the result into the global properties in the <em>userdir</em> (see
     * {@link PropertyUtils#putGlobalProperties})
     */
    public void removeJavadocRoots(URL[] urlsToRemove) throws IOException {
        Collection newJavadocs = new ArrayList(Arrays.asList(javadocRoots));
        newJavadocs.removeAll(Arrays.asList(urlsToRemove));
        URL[] javadocs = new URL[newJavadocs.size()];
        setJavadocRoots((URL[]) newJavadocs.toArray(javadocs));
    }
    
    public void moveJavadocRootUp(int indexToUp) throws IOException {
        if (indexToUp <= 0) {
            return; // nothing needs to be done
        }
        URL[] newJavadocRoots = new URL[javadocRoots.length];
        System.arraycopy(javadocRoots, 0, newJavadocRoots, 0, javadocRoots.length);
        newJavadocRoots[indexToUp - 1] = javadocRoots[indexToUp];
        newJavadocRoots[indexToUp] = javadocRoots[indexToUp - 1];
        setJavadocRoots(newJavadocRoots);
    }
    
    public void moveJavadocRootDown(int indexToDown) throws IOException {
        if (indexToDown >= (javadocRoots.length - 1)) {
            return; // nothing needs to be done
        }
        URL[] newJavadocRoots = new URL[javadocRoots.length];
        System.arraycopy(javadocRoots, 0, newJavadocRoots, 0, javadocRoots.length);
        newJavadocRoots[indexToDown + 1] = javadocRoots[indexToDown];
        newJavadocRoots[indexToDown] = javadocRoots[indexToDown + 1];
        setJavadocRoots(newJavadocRoots);
    }
    
    public void setJavadocRoots(URL[] roots) throws IOException {
        putGlobalProperty(
                PLATFORM_PREFIX + getID() + PLATFORM_JAVADOC_SUFFIX,
                urlsToAntPath(roots));
        javadocRoots = roots;
    }
    
    /** 
     * Test whether this platform is valid or not. See
     * {@link #isPlatformDirectory}
     */
    public boolean isValid() {
        return NbPlatform.isPlatformDirectory(getDestDir());
    }

    private String urlsToAntPath(URL[] urls) {
        StringBuffer path = new StringBuffer();
        for (int i = 0; i < urls.length; i++) {
            path.append(urls[i].getFile());
            if (i != urls.length - 1) {
                path.append(':'); // NOI18N
            }
        }
        return path.toString();
    }
    
    private void putGlobalProperty(final String key, final String value) throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws IOException {
                    EditableProperties props = PropertyUtils.getGlobalProperties();
                    if ("".equals(value)) { // NOI18N
                        props.remove(key);
                    } else {
                        props.setProperty(key, value);
                    }
                    PropertyUtils.putGlobalProperties(props);
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
    }
    
    /**
     * Find sources for a module JAR file contained in this destination directory.
     * @param jar a JAR file in the destination directory
     * @return the directory of sources for this module (a project directory), or null
     */
    public File getSourceLocationOfModule(File jar) {
        if (listsForSources == null) {
            listsForSources = new ArrayList();
            for (int i = 0; i < sourceRoots.length; i++) {
                URL u = sourceRoots[i];
                if (!u.getProtocol().equals("file")) { // NOI18N
                    continue;
                }
                File dir = new File(URI.create(u.toExternalForm()));
                if (dir.isDirectory()) {
                    try {
                        if (ModuleList.isNetBeansOrg(dir)) {
                            listsForSources.add(ModuleList.findOrCreateModuleListFromNetBeansOrgSources(dir));
                        } else {
                            listsForSources.add(ModuleList.findOrCreateModuleListFromSuiteWithoutBinaries(dir));
                        }
                    } catch (IOException e) {
                        Util.err.notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
            }
        }
        Iterator it = listsForSources.iterator();
        while (it.hasNext()) {
            ModuleList l = (ModuleList) it.next();
            Set/*<ModuleEntry>*/ entries = l.getAllEntries();
            Iterator it2 = entries.iterator();
            while (it2.hasNext()) {
                ModuleEntry entry = (ModuleEntry) it2.next();
                if (!entry.getJarLocation().equals(jar)) {
                    continue;
                }
                File src = entry.getSourceLocation();
                if (src != null && src.isDirectory()) {
                    return src;
                }
            }
        }
        return null;
    }
    
    /**
     * Returns (naturally sorted) array of all module entries pertaining to
     * <code>this</code> NetBeans platform. This is just a convenient delegate
     * to the {@link ModuleList#findOrCreateModuleListFromBinaries}.
     */
    public ModuleEntry[] getModules() {
        try {
            SortedSet/*<ModuleEntry>*/ set = new TreeSet(
                    ModuleList.findOrCreateModuleListFromBinaries(getDestDir()).getAllEntries());
            ModuleEntry[] entries = new ModuleEntry[set.size()];
            set.toArray(entries);
            return entries;
        } catch (IOException e) {
            Util.err.notify(ErrorManager.EXCEPTION, e);
            return new ModuleEntry[0];
        }
    }
    
    private static File findCoreJar(File destdir) {
        File[] subdirs = destdir.listFiles();
        if (subdirs != null) {
            for (int i = 0; i < subdirs.length; i++) {
                if (!subdirs[i].isDirectory()) {
                    continue;
                }
                if (!subdirs[i].getName().startsWith("platform")) { // NOI18N
                    continue;
                }
                File coreJar = new File(subdirs[i], "core" + File.separatorChar + "core.jar"); // NOI18N
                if (coreJar.isFile()) {
                    return coreJar;
                }
            }
        }
        return null;
    }
    
    /**
     * Check whether a given directory on disk is a valid destdir as per {@link #getDestDir}.
     * @param destdir a candidate directory
     * @return true if it can be used as a platform
     */
    public static boolean isPlatformDirectory(File destdir) {
        return findCoreJar(destdir) != null;
    }
    
    /**
     * Find a display name for a NetBeans platform on disk.
     * @param destdir a dir passing {@link #isPlatformDirectory}
     * @return a display name
     * @throws IllegalArgumentException if {@link #isPlatformDirectory} was false
     * @throws IOException if its labelling info could not be read
     */
    public static String computeDisplayName(File destdir) throws IOException {
        File coreJar = findCoreJar(destdir);
        if (coreJar == null) {
            throw new IllegalArgumentException(destdir.getAbsolutePath());
        }
        JarFile jf = new JarFile(coreJar);
        try {
            // first try to find the Bundle for 4.2+ (after openide split)
            ZipEntry bundle = jf.getEntry("org/netbeans/core/startup/Bundle.properties"); // NOI18N
            if (bundle == null) {
                // might be <4.2 (before openide split)
                bundle = jf.getEntry("org/netbeans/core/Bundle.properties"); // NOI18N
            }
            if (bundle == null) {
                throw new IOException(coreJar.getAbsolutePath());
            }
            Properties props = new Properties();
            InputStream is = jf.getInputStream(bundle);
            try {
                props.load(is);
            } finally {
                is.close();
            }
            String currVer = props.getProperty("currentVersion"); // NOI18N
            if (currVer == null) {
                throw new IOException(coreJar.getAbsolutePath());
            }
            String implVers = jf.getManifest().getMainAttributes().getValue("OpenIDE-Module-Implementation-Version"); // NOI18N
            if (implVers == null) {
                throw new IOException(coreJar.getAbsolutePath());
            }
            return MessageFormat.format(currVer, new Object[] {implVers});
        } finally {
            jf.close();
        }
    }
    
    public int compareTo(Object o) {
        return Collator.getInstance().compare(
                getLabel(), ((NbPlatform) o).getLabel());
    }
    
    public String toString() {
        return "NbPlatform[" + getID() + ":" + getDestDir() + "]"; // NOI18N;
    }
    
}
