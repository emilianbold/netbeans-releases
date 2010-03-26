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

package org.netbeans.modules.apisupport.project.universe;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Represents one NetBeans platform, i.e. installation of the NB platform or IDE
 * or some derivative product.
 * Has a code id and can have associated sources and Javadoc, just like e.g. Java platforms.
 *
 * @author Jesse Glick
 */
public final class NbPlatform implements SourceRootsProvider, JavadocRootsProvider {
    
    private static final String PLATFORM_PREFIX = "nbplatform."; // NOI18N
    private static final String PLATFORM_DEST_DIR_SUFFIX = ".netbeans.dest.dir"; // NOI18N
    private static final String PLATFORM_LABEL_SUFFIX = ".label"; // NOI18N
    public static final String PLATFORM_SOURCES_SUFFIX = ".sources"; // NOI18N
    public static final String PLATFORM_JAVADOC_SUFFIX = ".javadoc"; // NOI18N
    private static final String PLATFORM_HARNESS_DIR_SUFFIX = ".harness.dir"; // NOI18N
    public static final String PLATFORM_ID_DEFAULT = "default"; // NOI18N
    
    private static volatile Set<NbPlatform> platforms;
    
    private final PropertyChangeSupport pcs;
    private final SourceRootsSupport srs;
    private final JavadocRootsSupport jrs;

    static {
        final File install = NbPlatform.defaultPlatformLocation();
        if (install != null) {
            class Init implements Runnable {
                public void run() {
                    if (ProjectManager.mutex().isWriteAccess()) {
                        EditableProperties p = PropertyUtils.getGlobalProperties();
                        String installS = install.getAbsolutePath();
                        p.setProperty("nbplatform.default.netbeans.dest.dir", installS); // NOI18N
                        if (!p.containsKey("nbplatform.default.harness.dir")) { // NOI18N
                            p.setProperty("nbplatform.default.harness.dir", "${nbplatform.default.netbeans.dest.dir}/harness"); // NOI18N
                        }
                        try {
                            PropertyUtils.putGlobalProperties(p);
                        } catch (IOException e) {
                            Util.err.notify(ErrorManager.INFORMATIONAL, e);
                        }
                    } else {
                        ProjectManager.mutex().postWriteRequest(this);
                    }
                }
            }
            try {
                RequestProcessor.getDefault().post(new Init()).waitFinished(1000);
            } catch (InterruptedException ex) {
                // OK
            }
        }
    }
    
    /**
     * Reset cached info so unit tests can start from scratch.
     * <p><b>Do not use outside of tests!</b> Concurrent call may cause
     * {@link #getPlatformsInternal()} to fail.</p>
     */
    public static void reset() {
        platforms = null;
    }
    
    /**
     * Get a set of all registered platforms.
     */
    public static Set<NbPlatform> getPlatforms() {
        Set<NbPlatform> plafs = getPlatformsInternal();
        synchronized (plafs) {
            return new HashSet<NbPlatform>(plafs);
        }
    }

    private static final Object lock = new Object();
    /**
     * Returns lazily initialized set of known platforms.
     * Returned set is synchronized, so you must synchronize on it when iterating, like this:
     * <pre>
     * Set&lt;NbPlatform&gt; plafs = getPlatformsInternal();
     * synchronized (plafs) {
     *   for (NbPlatform plaf : plafs) {
     *     // ...
     *   }
     * }</pre>
     * Note: do not pass returned set outside of NbPlatform class
     * @return
     */
    private static Set<NbPlatform> getPlatformsInternal() {
        Map<String,String> p = null;
        if (platforms == null) {
            // evaluator and prop. provider must be obtained outside of synchronized section,
            // as it acquires PM.mutex() read lock internally and can deadlock
            // when getPlatformsInternal() is called from PM.mutex() write lock;
            // see issue #173345
            p = PropertyUtils.sequentialPropertyEvaluator(null, PropertyUtils.globalPropertyProvider()).getProperties();
        }
        synchronized (lock) {
            if (platforms == null) {
                platforms = Collections.synchronizedSet(new HashSet<NbPlatform>());
                if (p == null) { // #115909
                    p = Collections.emptyMap();
                }
                boolean foundDefault = false;
                for (Map.Entry<String, String> entry : p.entrySet()) {
                    String key = entry.getKey();
                    if (key.startsWith(PLATFORM_PREFIX) && key.endsWith(PLATFORM_DEST_DIR_SUFFIX)) {
                        String id = key.substring(PLATFORM_PREFIX.length(), key.length() - PLATFORM_DEST_DIR_SUFFIX.length());
                        String label = p.get(PLATFORM_PREFIX + id + PLATFORM_LABEL_SUFFIX);
                        String destdir = entry.getValue();
                        String harnessdir = p.get(PLATFORM_PREFIX + id + PLATFORM_HARNESS_DIR_SUFFIX);
                        String sources = p.get(PLATFORM_PREFIX + id + PLATFORM_SOURCES_SUFFIX);
                        String javadoc = p.get(PLATFORM_PREFIX + id + PLATFORM_JAVADOC_SUFFIX);
                        File destdirF = FileUtil.normalizeFile(new File(destdir));
                        File harness;
                        if (harnessdir != null) {
                            harness = FileUtil.normalizeFile(new File(harnessdir));
                        } else {
                            harness = findHarness(destdirF);
                        }
                        platforms.add(new NbPlatform(id, label, destdirF, harness, Util.findURLs(sources), Util.findURLs(javadoc)));
                        foundDefault |= id.equals(PLATFORM_ID_DEFAULT);
                    }
                }
                if (!foundDefault) {
                    File loc = defaultPlatformLocation();
                    if (loc != null) {
                        platforms.add(new NbPlatform(PLATFORM_ID_DEFAULT, null, loc, findHarness(loc), new URL[0], new URL[0]));
                    }
                }
                if (Util.err.isLoggable(ErrorManager.INFORMATIONAL)) {
                    Util.err.log("NbPlatform initial list: " + platforms);
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
     * Get the location of the default platform, or null.
     */
    public static File defaultPlatformLocation() {
        // XXX cache the result?
        // Semi-arbitrary platform* component.
        File bootJar = InstalledFileLocator.getDefault().locate("core/core.jar", "org.netbeans.core.startup", false); // NOI18N
        if (bootJar == null) {
            if (Util.err.isLoggable(ErrorManager.INFORMATIONAL)) {
                Util.err.log("no core/core.jar");
            }
            return null;
        }
        // Semi-arbitrary harness component.
        File harnessJar = InstalledFileLocator.getDefault().locate("modules/org-netbeans-modules-apisupport-harness.jar", "org.netbeans.modules.apisupport.harness", false); // NOI18N
        if (harnessJar == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, "Cannot resolve default platform. " + // NOI18N
                    "Probably either \"org.netbeans.modules.apisupport.harness\" module is missing or is corrupted."); // NOI18N
            return null;
        }
        File loc = harnessJar.getParentFile().getParentFile().getParentFile();
        try {
            if (!loc.getCanonicalFile().equals(bootJar.getParentFile().getParentFile().getParentFile().getCanonicalFile())) {
                // Unusual installation structure, punt.
                if (Util.err.isLoggable(ErrorManager.INFORMATIONAL)) {
                    Util.err.log("core.jar & harness.jar locations do not match: " + bootJar + " vs. " + harnessJar);
                }
                return null;
            }
        } catch (IOException x) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, x);
        }
        // Looks good.
        return FileUtil.normalizeFile(loc);
    }
    
    /**
     * Find a platform by its ID.
     * @param id an ID (as in {@link #getID})
     * @return the platform with that ID, or null
     */
    public static NbPlatform getPlatformByID(String id) {
        for (NbPlatform p : getPlatformsInternal()) {
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
        Set<NbPlatform> plafs = getPlatformsInternal();
        synchronized (plafs) {
            for (NbPlatform p : plafs) {
                // DEBUG only
//                int dif = p.getDestDir().compareTo(destDir);

                if (p.getDestDir().equals(destDir)) {
                    return p;
                }
            }
        }
        URL[] sources = new URL[0];
        if (destDir.getName().equals("netbeans")) { // NOI18N
            File parent = destDir.getParentFile();
            if (parent != null && parent.getName().equals("nbbuild")) { // NOI18N
                File superparent = parent.getParentFile();
                if (superparent != null && ModuleList.isNetBeansOrg(superparent)) {
                    sources = new URL[] {FileUtil.urlForArchiveOrDir(superparent)};
                }
            }
        }
        // XXX might also check OpenProjectList for NbModuleProject's and/or SuiteProject's with a matching
        // dest dir and look up property 'sources' to use; TBD whether Javadoc could also be handled in a
        // similar way
        return new NbPlatform(null, null, destDir, findHarness(destDir), sources, new URL[0]);
    }
    
    /**
     * Find the location of the harness inside a platform.
     * Guaranteed to be a child directory (but might not exist yet).
     */
    private static File findHarness(File destDir) {
        File[] kids = destDir.listFiles();
        if (kids != null) {
            for (int i = 0; i < kids.length; i++) {
                if (isHarness(kids[i])) {
                    return kids[i];
                }
            }
        }
        return new File(destDir, "harness"); // NOI18N
    }
    
    /**
     * Check whether a given directory is really a valid harness.
     */
    public static boolean isHarness(File dir) {
        return new File(dir, "modules" + File.separatorChar + "org-netbeans-modules-apisupport-harness.jar").isFile(); // NOI18N
    }
    
    /**
     * Returns whether the platform within the given directory is already
     * registered.
     */
    public static boolean contains(File destDir) {
        boolean contains = false;
        Set<NbPlatform> plafs = getPlatformsInternal();
        synchronized (plafs) {
            for (NbPlatform p : plafs) {
                if (p.getDestDir().equals(destDir)) {
                    contains = true;
                    break;
                }
            }
        }
        return contains;
    }
    
    /**
     * Register a new platform.
     * @param id unique ID string for the platform
     * @param destdir destination directory (i.e. top-level directory beneath which there are clusters)
     * @param label display label
     * @return the created platform
     * @throws IOException in case of problems (e.g. destination directory does not exist)
     */
    public static NbPlatform addPlatform(final String id, final File destdir, final String label) throws IOException {
        return addPlatform(id, destdir, findHarness(destdir), label);
    }
    
    /**
     * Register a new platform.
     * @param id unique ID string for the platform
     * @param destdir destination directory (i.e. top-level directory beneath which there are clusters)
     * @param harness harness directory
     * @param label display label
     * @return the created platform
     * @throws IOException in case of problems (e.g. destination directory does not exist)
     */
    public static NbPlatform addPlatform(final String id, final File destdir, final File harness, final String label) throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws IOException {
                    if (getPlatformByID(id) != null) {
                        throw new IOException("ID " + id + " already taken");
                    }
                    EditableProperties props = PropertyUtils.getGlobalProperties();
                    String plafDestDir = PLATFORM_PREFIX + id + PLATFORM_DEST_DIR_SUFFIX;
                    props.setProperty(plafDestDir, destdir.getAbsolutePath());
                    if (!destdir.isDirectory()) {
                        throw new FileNotFoundException(destdir.getAbsolutePath());
                    }
                    storeHarnessLocation(id, destdir, harness, props);
                    props.setProperty(PLATFORM_PREFIX + id + PLATFORM_LABEL_SUFFIX, label);
                    PropertyUtils.putGlobalProperties(props);
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
        NbPlatform plaf = new NbPlatform(id, label, FileUtil.normalizeFile(destdir), harness,
                Util.findURLs(null), Util.findURLs(null));
        getPlatformsInternal().add(plaf);
        if (Util.err.isLoggable(ErrorManager.INFORMATIONAL)) {
            Util.err.log("NbPlatform added: " + plaf);
        }
        return plaf;
    }
    
    private static void storeHarnessLocation(String id, File destdir, File harness, EditableProperties props) {
        String harnessDirKey = PLATFORM_PREFIX + id + PLATFORM_HARNESS_DIR_SUFFIX;
        if (harness.equals(findHarness(destdir))) {
            // Common case.
            String plafDestDir = PLATFORM_PREFIX + id + PLATFORM_DEST_DIR_SUFFIX;
            props.setProperty(harnessDirKey, "${" + plafDestDir + "}/" + harness.getName()); // NOI18N
        } else if (getDefaultPlatform() != null && harness.equals(getDefaultPlatform().getHarnessLocation())) {
            // Also common.
            props.setProperty(harnessDirKey, "${" + PLATFORM_PREFIX + PLATFORM_ID_DEFAULT + PLATFORM_HARNESS_DIR_SUFFIX + "}"); // NOI18N
        } else {
            // Some random location.
            props.setProperty(harnessDirKey, harness.getAbsolutePath());
        }
    }
    
    public static void removePlatform(final NbPlatform plaf) throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws IOException {
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
        getPlatformsInternal().remove(plaf);
        if (Util.err.isLoggable(ErrorManager.INFORMATIONAL)) {
            Util.err.log("NbPlatform removed: " + plaf);
        }
    }
    
    private final String id;
    private String label;
    private File nbdestdir;
    private File harness;
    private HarnessVersion harnessVersion;
    
    private NbPlatform(String id, String label, File nbdestdir, File harness, URL[] sources, URL[] javadoc) {
        this.id = id;
        this.label = label;
        this.nbdestdir = nbdestdir;
        this.harness = harness;
        pcs = new PropertyChangeSupport(this);
        srs = new SourceRootsSupport(sources, this);
        srs.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                // re-fire
                pcs.firePropertyChange(evt);
            }
        });
        jrs = new JavadocRootsSupport(javadoc, this);
        jrs.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                // re-fire
                pcs.firePropertyChange(evt);
            }
        });
    }
    
    /**
     * Get a unique ID for this platform.
     * Used e.g. in <code>nbplatform.active</code> in <code>platform.properties</code>.
     * @return a unique ID, or <code>null</code> for <em>anonymous</em>
     *         platforms (see {@link #getPlatformByDestDir}).
     */
    public String getID() {
        return id;
    }

    /**
     * Check if this is the default platform.
     * @return true for the one default platform
     */
    public boolean isDefault() {
        return PLATFORM_ID_DEFAULT.equals(id);
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
                Logger.getLogger(NbPlatform.class.getName()).log(Level.FINE, "could not get label for " + nbdestdir, e);
                label = nbdestdir.getAbsolutePath();
            }
        }
        if (isDefault()) {
            return NbBundle.getMessage(NbPlatform.class, "LBL_default_platform", label);
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
     * Get javadoc which should by default be associated with the default platform.
     */
    public URL[] getDefaultJavadocRoots() {
        if (! isDefault())
            return null;
        // javadoc can be built in the meantime, don't cache
        File apidocsZip = InstalledFileLocator.getDefault().locate("docs/NetBeansAPIs.zip", "org.netbeans.modules.apisupport.apidocs", true); // NOI18N
        if (apidocsZip != null) {
            return new URL[] {FileUtil.urlForArchiveOrDir(apidocsZip)};
        }
        return new URL[0];
    }

    public void addJavadocRoot(URL root) throws IOException {
        jrs.addJavadocRoot(root);
    }

    public URL[] getJavadocRoots() {
        return jrs.getJavadocRoots();
    }

    public void moveJavadocRootDown(int indexToDown) throws IOException {
        jrs.moveJavadocRootDown(indexToDown);
    }

    public void moveJavadocRootUp(int indexToUp) throws IOException {
        jrs.moveJavadocRootUp(indexToUp);
    }

    public void removeJavadocRoots(URL[] urlsToRemove) throws IOException {
        jrs.removeJavadocRoots(urlsToRemove);
    }

    public void setJavadocRoots(URL[] roots) throws IOException {
        putGlobalProperty(
                PLATFORM_PREFIX + getID() + PLATFORM_JAVADOC_SUFFIX,
                Util.urlsToAntPath(roots));
        jrs.setJavadocRoots(roots);
    }

    private URL[] defaultSourceRoots;

    /**
     * Get any sources which should by default be associated with the default platform.
     */
    public @Override URL[] getDefaultSourceRoots() {
        if (! isDefault()) {
            return null;
        }
        // location of platform won't change, safe to cache
        if (defaultSourceRoots != null) {
            return defaultSourceRoots;
        }
        defaultSourceRoots = new URL[0];
        File loc = getDestDir();
        if (loc.getName().equals("netbeans") && loc.getParentFile().getName().equals("nbbuild")) { // NOI18N
            try {
                defaultSourceRoots = new URL[] {loc.getParentFile().getParentFile().toURI().toURL()};
            } catch (MalformedURLException e) {
                assert false : e;
            }
        }
        return defaultSourceRoots;
    }

    /**
     * Get associated source roots for this platform.
     * Each root could be a netbeans.org source checkout or a module suite project directory.
     * @return a list of source root URLs (may be empty but not null)
     */
    public URL[] getSourceRoots() {
        return srs.getSourceRoots();
    }
    
    /**
     * Add given source root to the current source root list and save the
     * result into the global properties in the <em>userdir</em> (see {@link
     * PropertyUtils#putGlobalProperties})
     */
    public void addSourceRoot(URL root) throws IOException {
        srs.addSourceRoot(root);
    }
    
    /**
     * Remove given source roots from the current source root list and save the
     * result into the global properties in the <em>userdir</em> (see {@link
     * PropertyUtils#putGlobalProperties})
     */
    public void removeSourceRoots(URL[] urlsToRemove) throws IOException {
        srs.removeSourceRoots(urlsToRemove);
    }
    
    public void moveSourceRootUp(int indexToUp) throws IOException {
        srs.moveSourceRootUp(indexToUp);
    }
    
    public void moveSourceRootDown(int indexToDown) throws IOException {
        srs.moveSourceRootDown(indexToDown);
    }
    
    public void setSourceRoots(URL[] roots) throws IOException {
        putGlobalProperty(
                PLATFORM_PREFIX + getID() + PLATFORM_SOURCES_SUFFIX,
                Util.urlsToAntPath(roots));
        srs.setSourceRoots(roots);
    }
    
    /**
     * Test whether this platform is valid or not. See
     * {@link #isPlatformDirectory}
     */
    public boolean isValid() {
        return NbPlatform.isPlatformDirectory(getDestDir());
    }
    
    private void putGlobalProperty(final String key, final String value) throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws IOException {
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
        return srs.getSourceLocationOfModule(jar);
    }
    
    /**
     * Returns (naturally sorted) array of all module entries pertaining to
     * <code>this</code> NetBeans platform. This is just a convenient delegate
     * to the {@link ModuleList#findOrCreateModuleListFromBinaries}.
     *
     * This may be a time-consuming method, consider using much faster
     * ModuleList#getModules instead, which doesn't sort the modules. Do not call
     * from AWT thread (not checked so that it may be used in tests).
     */
    public ModuleEntry[] getSortedModules() {
        SortedSet<ModuleEntry> set = new TreeSet<ModuleEntry>(getModules());
        ModuleEntry[] entries = new ModuleEntry[set.size()];
        set.toArray(entries);
        return entries;
    }

    /**
     * Returns a set of all module entries pertaining to
     * <code>this</code> NetBeans platform. This is just a convenient delegate
     * to the {@link ModuleList#findOrCreateModuleListFromBinaries}.
     */
    public Set<ModuleEntry> getModules() {
        if (nbd)
        try {
            return ModuleList.findOrCreateModuleListFromBinaries(nbdestdir).getAllEntries();
        } catch (IOException e) {
            Util.err.notify(e);
            return Collections.emptySet();
        }
    }

    /**
     * Gets a module from the platform by name.
     */
    public ModuleEntry getModule(String cnb) {
        try {
            return ModuleList.findOrCreateModuleListFromBinaries(nbdestdir).getEntry(cnb);
        } catch (IOException e) {
            Util.err.notify(e);
            return null;
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
    
    public static boolean isSupportedPlatform(File destdir) {
        boolean valid = false;
        File coreJar = findCoreJar(destdir);
        if (coreJar != null) {
            String platformDir = coreJar.getParentFile().getParentFile().getName();
            assert platformDir.startsWith("platform"); // NOI18N
            String version = platformDir.substring("platform".length());
            valid = /* NB 6.9+ */version.isEmpty() || Integer.parseInt(version) >= 6;
        }
        return valid;
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
        String currVer, implVers;
        JarFile jf = new JarFile(coreJar);
        try {
            currVer = findCurrVer(jf, "");
            if (currVer == null) {
                throw new IOException(coreJar.getAbsolutePath());
            }
            implVers = jf.getManifest().getMainAttributes().getValue("OpenIDE-Module-Build-Version"); // NOI18N
            if (implVers == null) {
                implVers = jf.getManifest().getMainAttributes().getValue("OpenIDE-Module-Implementation-Version"); // NOI18N
            }
            if (implVers == null) {
                throw new IOException(coreJar.getAbsolutePath());
            }
        } finally {
            jf.close();
        }
        // Also check in localizing bundles for 'currentVersion', since it may be branded.
        // We do not know what the runtime branding will be, so look for anything.
        File[] clusters = destdir.listFiles();
        BRANDED_CURR_VER: if (clusters != null) {
            for (int i = 0; i < clusters.length; i++) {
                File coreLocaleDir = new File(clusters[i], "core" + File.separatorChar + "locale"); // NOI18N
                if (!coreLocaleDir.isDirectory()) {
                    continue;
                }
                String[] kids = coreLocaleDir.list();
                if (kids != null) {
                    for (int j = 0; j < kids.length; j++) {
                        String name = kids[j];
                        String prefix = "core"; // NOI18N
                        String suffix = ".jar"; // NOI18N
                        if (!name.startsWith(prefix) || !name.endsWith(suffix)) {
                            continue;
                        }
                        String infix = name.substring(prefix.length(), name.length() - suffix.length());
                        int uscore = infix.lastIndexOf('_');
                        if (uscore == -1) {
                            // Malformed.
                            continue;
                        }
                        String lastPiece = infix.substring(uscore + 1);
                        if (Arrays.asList(Locale.getISOCountries()).contains(lastPiece) ||
                                (!lastPiece.equals("nb") && Arrays.asList(Locale.getISOLanguages()).contains(lastPiece))) { // NOI18N
                            // Probably a localization, not a branding... so skip it. (We want to show English only.)
                            // But hardcode support for branding 'nb' since this is also Norwegian Bokmal, apparently!
                            // XXX should this try to use Locale.getDefault() localization if possible?
                            continue;
                        }
                        jf = new JarFile(new File(coreLocaleDir, name));
                        try {
                            String brandedCurrVer = findCurrVer(jf, infix);
                            if (brandedCurrVer != null) {
                                currVer = brandedCurrVer;
                                break BRANDED_CURR_VER;
                            }
                        } finally {
                            jf.close();
                        }
                    }
                }
            }
        }
        return MessageFormat.format(currVer, new Object[] {implVers});
    }
    private static String findCurrVer(JarFile jar, String infix) throws IOException {
        // first try to find the Bundle for 5.0+ (after openide split)
        ZipEntry bundle = jar.getEntry("org/netbeans/core/startup/Bundle" + infix + ".properties"); // NOI18N
        if (bundle == null) {
            // might be <5.0 (before openide split)
            bundle = jar.getEntry("org/netbeans/core/Bundle" + infix + ".properties"); // NOI18N
        }
        if (bundle == null) {
            return null;
        }
        Properties props = new Properties();
        InputStream is = jar.getInputStream(bundle);
        try {
            props.load(is);
        } finally {
            is.close();
        }
        return props.getProperty("currentVersion"); // NOI18N
    }
    
    /**
     * Returns whether the given label (see {@link #getLabel}) is valid.
     * <em>Valid</em> label must be non-null and must not be used by any
     * already defined platform.
     */
    public static boolean isLabelValid(String supposedLabel) {
        if (supposedLabel == null) {
            return false;
        }
        for (NbPlatform p : NbPlatform.getPlatforms()) {
            String label = p.getLabel();
            if (supposedLabel.equals(label)) {
                return false;
            }
        }
        return true;
    }
    
    public @Override String toString() {
        return "NbPlatform[" + getID() + ":" + getDestDir() + ";sources=" + Arrays.asList(getSourceRoots()) + ";javadoc=" + Arrays.asList(getJavadocRoots()) + "]"; // NOI18N;
    }
    
    /**
     * Get the version of this platform's harness.
     */
    public HarnessVersion getHarnessVersion() {
        if (harnessVersion != null) {
            return harnessVersion;
        }
        if (!isValid()) {
            return harnessVersion = HarnessVersion.UNKNOWN;
        }
        File harnessJar = new File(harness, "modules" + File.separatorChar + "org-netbeans-modules-apisupport-harness.jar"); // NOI18N
        if (harnessJar.isFile()) {
            try {
                JarFile jf = new JarFile(harnessJar);
                try {
                    String spec = jf.getManifest().getMainAttributes().getValue(ManifestManager.OPENIDE_MODULE_SPECIFICATION_VERSION);
                    if (spec != null) {
                        SpecificationVersion v = new SpecificationVersion(spec);
                        return harnessVersion = HarnessVersion.forHarnessModuleVersion(v);
                    }
                } finally {
                    jf.close();
                }
            } catch (IOException e) {
                Util.err.notify(ErrorManager.INFORMATIONAL, e);
            } catch (NumberFormatException e) {
                Util.err.notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return harnessVersion = HarnessVersion.UNKNOWN;
    }
    
    /**
     * Get the current location of this platform's harness
     */
    public File getHarnessLocation() {
        return harness;
    }
    
    /**
     * Get the location of the harness bundled with this platform.
     */
    public File getBundledHarnessLocation() {
        return findHarness(nbdestdir);
    }
    
    /**
     * Set a new location for this platform's harness.
     */
    public void setHarnessLocation(final File harness) throws IOException {
        if (harness.equals(this.harness)) {
            return;
        }
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws IOException {
                    EditableProperties props = PropertyUtils.getGlobalProperties();
                    storeHarnessLocation(id, nbdestdir, harness, props);
                    PropertyUtils.putGlobalProperties(props);
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
        this.harness = harness;
        harnessVersion = null;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
    
}
