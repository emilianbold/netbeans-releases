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

package org.netbeans.api.java.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.java.classpath.ClassPathAccessor;
import org.netbeans.modules.java.classpath.SimplePathResourceImplementation;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.FilteringPathResourceImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 * ClassPath objects should be used to access contents of the ClassPath, searching
 * for resources, objects reachable using the ClassPath at runtime. It is intended
 * to replace some of the functionality of <link>org.openide.filesystems.Repository</link>.
 * <BR>
 * ClassPath instances should be used to map from Java-style resource names
 * to FileObject (NetBeans-style resource) and vice versa. It should be also used
 * whenever the operation requires inspection of development or runtime project
 * environment instead. The service supports either searching in the classpath
 * resource space, properly hiding resources as the ClassLoader would do at runtime.
 * It can effectively say whether a FileObject is within the reach of a ClassPath
 * or whether it is <I>reachable</I> (visible to a ClassLoader). One can translate
 * filenames to resource names and vice versa.
 * <P>
 * A client may obtain a ClassPath instance using
 * <code>ClassPath.getClassPath(id)</code> static method, where the ID is an
 * abstract name for the classpath wanted. There are some predefined classpath
 * names predefined as symbolic constants, following individual types of services
 * (compiler, debugger, executor). Names are not limited to the listed ones; an extension
 * module might add its own private classpath type.
 */
public final class ClassPath {

    static  {
        ClassPathAccessor.DEFAULT = new ClassPathAccessor() {
            public ClassPath createClassPath(ClassPathImplementation spiClasspath) {
                return new ClassPath(spiClasspath);
            }
            public ClassPathImplementation getClassPathImpl(ClassPath cp) {
                return cp == null ? null : cp.impl;
            }
        };
    }

    /**
     * Classpath setting for executing things. This type can be used to learn
     * runtime time classpath for execution of the file in question.
     * <p class="nonnormative">
     * It corresponds to the <code>-classpath</code> option to <code>java</code>
     * (the Java launcher): i.e. all compiled classes outside the JRE that
     * will be needed to run the program, or at least to load a certain class.
     * It may also be thought of as corresponding to the list of URLs in a
     * <code>URLClassLoader</code> (plus URLs present in parent class loaders
     * but excluding the bootstrap and extension class loaders).
     * </p>
     */
    public static final String EXECUTE = "classpath/execute";

    /**
     * Classpath for debugging things
     * @deprecated Probably useless.
     */
    @Deprecated
    public static final String DEBUG = "classpath/debug";

    /**
     * ClassPath for compiling things. This type can be used to learn
     * compilation time classpath for the file in question.
     * <p class="nonnormative">
     * It corresponds to the <code>-classpath</code> option to <code>javac</code>:
     * i.e. already-compiled classes which some new sources need to compile against,
     * besides what is already in the JRE.
     * </p>
     */
    public static final String COMPILE = "classpath/compile";

    /**
     * ClassPath for project sources. This type can be used to learn
     * package root of the file in question.
     * <div class="nonnormative">
     * <p>
     * It is similar to the <code>-sourcepath</code> option of <code>javac</code>.
     * </p>
     * <p>
     * For typical source files, the sourcepath will consist of one element:
     * the package root of the source file. If more than one package root is
     * to be compiled together, all the sources should share a sourcepath
     * with multiple roots.
     * </p>
     * <p>
     * Note that each source file for which editor code completion (and similar
     * actions) should work should have a classpath of this type.
     * </p>
     * </div>
     * @since org.netbeans.api.java/1 1.4
     */
    public static final String SOURCE = "classpath/source";

    /**
     * Boot ClassPath of the JDK. This type can be used to learn boot classpath
     * which should be used for the file in question.
     * <p class="nonnormative">
     * It corresponds to the <code>-Xbootclasspath</code> and <code>-Xext</code>
     * options to <code>java</code> (the Java launcher): i.e. all compiled
     * classes in the JRE that will be needed to run the program.
     * It may also be thought of as corresponding to the classes loadable
     * by the primordial bootstrap class loader <em>plus</em> the standard
     * extension and endorsed-library class loaders; i.e. class loaders lying
     * below the regular application startup loader and any custom loaders.
     * Generally there ought to be a single boot classpath for the entire
     * application.
     * </p>
     * @since org.netbeans.api.java/1 1.4
     */
    public static final String BOOT = "classpath/boot";

    /**
     * Name of the "roots" property
     */
    public static final String PROP_ROOTS   = "roots";

    /**
     * Name of the "entries" property
     */
    public static final String PROP_ENTRIES = "entries";
    
    /**
     * Property to be fired when include/exclude set changes.
     * @see FilteringPathResourceImplementation
     * @since org.netbeans.api.java/1 1.13
     */
    public static final String PROP_INCLUDES = "includes";

    /**
     * The empty ClassPath.
     * Contains no entries and never fires events.
     * @since 1.24
     */
    public static final ClassPath EMPTY = new ClassPath();

    private static final Logger LOG = Logger.getLogger(ClassPath.class.getName());
    
    private static final Lookup.Result<? extends ClassPathProvider> implementations =
        Lookup.getDefault().lookupResult(ClassPathProvider.class);

    private final ClassPathImplementation impl;
    private final Throwable caller;
    private FileObject[] rootsCache;
    /**
     * Associates entry roots with the matching filter, if there is one.
     * XXX not quite right since we could have the same root added twice with two different
     * filters. But why would you do that?
     */
    private Map<FileObject,FilteringPathResourceImplementation> root2Filter = new WeakHashMap<FileObject,FilteringPathResourceImplementation>();
    private PropertyChangeListener pListener;
    private PropertyChangeListener weakPListener;
    private RootsListener rootsListener;
    private List<ClassPath.Entry> entriesCache;
    private long invalidEntries;    //Lamport ordering of events
    private long invalidRoots;      //Lamport ordering of events

    /**
     * Retrieves valid roots of ClassPath, in the proper order.
     * If there's an entry in the ClassPath, which cannot be accessed,
     * its root is not returned by this method. FileObjects returned
     * are all folders.
     * Note that this method ignores {@link FilteringPathResourceImplementation includes and excludes}.
     * @return array of roots (folders) of the classpath. Never returns
     * null.
     */
    public FileObject[]  getRoots() {
        long current;
        synchronized (this) {
            if (rootsCache != null) {
                return this.rootsCache;
            }
            current = this.invalidRoots;
        }
        List<ClassPath.Entry> entries = this.entries();
        FileObject[] ret;
        synchronized (this) {
            if (this.invalidRoots == current) {
                if (rootsCache == null || rootsListener == null) {
                    attachRootsListener();
                    this.rootsCache = createRoots (entries);
                }
                ret = this.rootsCache;
            }
            else {
                ret = createRoots (entries);
            }
        }
        assert ret != null;
        return ret;
    }

    private FileObject[] createRoots (final List<ClassPath.Entry> entries) {
        final List<FileObject> l = new ArrayList<FileObject> ();
        final Set<URL> listenOn = new HashSet<URL>();
        for (Entry entry : entries) {
            listenOn.add(entry.getURL());
            FileObject fo = entry.getRoot();
            if (fo != null) {
                l.add(fo);
                root2Filter.put(fo, entry.filter);
            }
        }
        final RootsListener rL = this.getRootsListener();
        if (rL != null) {
            rL.addRoots (listenOn);
        }
        return l.toArray (new FileObject[l.size()]);
    }

    /**
     * Returns list of classpath entries from the ClassPath definition.
     * The implementation must ensure that modifications done to the List are
     * banned or at least not reflected in other Lists returned by this ClassPath
     * instance. Clients must assume that the returned value is immutable.
     * @return list of definition entries (Entry instances)
     */
    public  List<ClassPath.Entry> entries() {
        long current;
        synchronized (this) {
            if (this.entriesCache != null) {
                return this.entriesCache;
            }
            current = this.invalidEntries;
        }
        List<? extends PathResourceImplementation> resources = impl.getResources();
        if (resources == null)
            throw new NullPointerException (
                "ClassPathImplementation.getResources() returned null. ClassPathImplementation.class: "
                + impl.getClass ().toString () + " ClassPathImplementation: " + impl.toString ()
            );
        final List<Object[]> snapshot = new ArrayList<Object[]>();
        for (PathResourceImplementation pr : resources) {
            snapshot.add(new Object[] {pr, pr.getRoots()});
        }
        List<ClassPath.Entry> result;
        synchronized (this) {
            if (this.invalidEntries == current) {
                if (this.entriesCache == null) {                    
                    this.entriesCache = createEntries (snapshot);
                }
                result = this.entriesCache;
            }
            else {                
                result = createEntries (snapshot);
            }         
        }
        assert result != null;
        return result;
    }
    
    private List<ClassPath.Entry> createEntries (final List<Object[]> resources) {
            List<ClassPath.Entry> cache = new ArrayList<ClassPath.Entry> ();
            for (Object[] pair : resources) {
                PathResourceImplementation pr = (PathResourceImplementation) pair[0];
                URL[] roots = (URL[]) pair[1];
                pr.removePropertyChangeListener(weakPListener);
                pr.addPropertyChangeListener(weakPListener = WeakListeners.propertyChange(pListener, pr));
                for (URL root : roots) {
                    if (!(pr instanceof SimplePathResourceImplementation)) { // ctor already checks these things
                        SimplePathResourceImplementation.verify(root, " From: " + pr.getClass().getName(), caller);
                    }
                    cache.add(new Entry(root,
                            pr instanceof FilteringPathResourceImplementation ? (FilteringPathResourceImplementation) pr : null));
                }
            }
            return Collections.unmodifiableList(cache);
    }

    private ClassPath (ClassPathImplementation impl) {
        if (impl == null)
            throw new IllegalArgumentException ();
        this.propSupport = new PropertyChangeSupport(this);
        this.impl = impl;
        this.pListener = new SPIListener ();
        this.impl.addPropertyChangeListener (weakPListener = WeakListeners.propertyChange(this.pListener, this.impl));
        caller = new IllegalArgumentException();
    }

    private ClassPath() {
        this.propSupport = new PropertyChangeSupport(this) {
            @Override
            public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {}
            @Override
            public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {}
            @Override
            public void firePropertyChange(PropertyChangeEvent evt) {}
        };
        this.impl = new ClassPathImplementation() {
            public List<? extends PathResourceImplementation> getResources() {
                return Collections.emptyList();
            }
            public void addPropertyChangeListener(PropertyChangeListener listener) {}
            public void removePropertyChangeListener(PropertyChangeListener listener) {}
        };
        this.pListener = new SPIListener ();
        caller = new IllegalArgumentException();
    }

    /**
     * Returns a FileObject for the specified resource. May return null,
     * if the resource does not exist, or is not reachable through
     * this ClassPath.<BR>
     * If the <i>resourceName</i> identifies a package, this method will
     * return the <code>FileObject</code> for the first <I>package fragment</I>
     * in the <code>ClassPath</code>.
     * {@link FilteringPathResourceImplementation} may cause an actual file
     * beneath a registered root to not be returned.
     * Note: do not pass names starting with slash to this method.
     * @param resourceName name of the resource as it would be passed
     *                     to {@link ClassLoader#getResource}
     * @return FileObject for the resource, or null if the resource cannot
     * be found in this ClassPath.
     */
    public final FileObject findResource(String resourceName) {
        return findResourceImpl(getRoots(), new int[] {0}, parseResourceName(resourceName));
    }

    /**
     * Gives out an ordered collection containing all FileObjects, which correspond
     * to a given ResourceName; only the first one is seen by the ClassLoader
     * at runtime or can be linked against.  The resource name uses slashes ('/')
     * as folder separator and must not start with slash.
     * {@link FilteringPathResourceImplementation} may cause an actual file
     * beneath a registered root to not be returned.
     * @param resourceName resource name
     * @return list of resources identified by the given name.
     */
    public final List<FileObject> findAllResources(String resourceName) {
	FileObject[] roots = getRoots();
        List<FileObject> l = new ArrayList<FileObject>(roots.length);
        int[] idx = new int[] { 0 };
        String[] namec = parseResourceName(resourceName);
        while (idx[0] < roots.length) {
            FileObject f = findResourceImpl(roots, idx, namec);
            if (f != null)
                l.add(f);
        }
        return l;
    }

    /**
     * Creates a suitable resource name for the given FileObject within the
     * classpath. The method will return <code>null</code> if the fileobject
     * is not underneath any of classpath roots.<BR>
     * The returned name uses uses slashes ('/') as folder separators and
     * dot ('.') to separate file name and its extension.
     * Note that if the file object is in the classpath subtree, but is not reachable
     * (it is hidden by some other resource, or {@link FilteringPathResourceImplementation excluded}), the resource name is still returned.
     * @return Java-style resource name for the given file object (the empty string for the package root itself), or null if not
     * within the classpath
     * @param f FileObject whose resource name is requested
     */
    public final String getResourceName(FileObject f) {
	return getResourceName(f, '/', true);
    }

    /**
     * Computes a resource name for the FileObject, which uses `pathSep' character
     * as a directory separator. The resource name can be returned without the file
     * extension, if desired. Note that parent folder names are always returned with
     * extension, if they have some.
     * @param f FileObject whose resource name is requested.
     * @param dirSep directory separator character
     * @param includeExt whether the FileObject's extension should be included in the result
     * @return resource name for the given FileObject (the empty string for the package root itself) or null
     */
    public final String getResourceName(FileObject f, char dirSep, boolean includeExt) {
        FileObject owner = findOwnerRoot(f);
        if (owner == null)
            return null;
        String partName = FileUtil.getRelativePath(owner, f);
        assert partName != null;
        if (!includeExt) {
            int index = partName.lastIndexOf('.');
            //Dot at the beginning is not handled as an extension (hidden file)
            if (index > 0 && index > partName.lastIndexOf('/')+1) {
                partName = partName.substring (0, index);
            }
        }
        if (dirSep!='/') {
            partName = partName.replace('/',dirSep);
        }
        return partName;
    }

    /**
     * Finds a root in this ClassPath, that owns the given file. File resources, that
     * are not reachable (they are hidden by other resources, or {@link FilteringPathResourceImplementation} excluded) are still considered
     * to be part of the classpath and "owned" by one of its roots.
     * <br>
     * <b>Note:</b> This implementation assumes that the FileSystem hosting a classpath root
     * contains the entire classpath subtree rooted at that root folder.
     * @return classpath root, which hosts the specified resouce. It can return null,
     * if the resource is not within the ClassPath contents.
     * @param resource resource to find root for.
     */
    public final FileObject findOwnerRoot(FileObject resource) {
	FileObject[] roots = getRoots();
        Set<FileObject> rootsSet = new HashSet<FileObject>(Arrays.asList(roots));
        for (FileObject f = resource; f != null; f = f.getParent()) {
            if (rootsSet.contains(f)) {
                return f;
            }
        }
        return null;
    }

    /**
     * Checks whether a FileObject lies on this classpath.
     * {@link FilteringPathResourceImplementation} is considered.
     * @return true, if the parameter is inside one of the classpath subtrees,
     * false otherwise.
     * @param f the FileObject to check
     */
    public final boolean contains(FileObject f) {
        FileObject root = findOwnerRoot(f);
        if (root == null) {
            return false;
        }
        FilteringPathResourceImplementation filter = root2Filter.get(root);
        if (filter == null) {
            return true;
        }
        String path = FileUtil.getRelativePath(root, f);
        assert path != null : "could not find " + f + " in " + root;
        if (f.isFolder()) {
            path += "/"; // NOI18N
        }
        try {
            return filter.includes(root.getURL(), path);
        } catch (FileStateInvalidException x) {
            throw new AssertionError(x);
        }
    }

    /**
     * Determines if the resource is <i>visible</i> in the classpath,
     * that is if the file will be reached when a process attempts to
     * load a resource of that name. It will return false when the resource
     * is not contained in the classpath, or the resource is {@link FilteringPathResourceImplementation excluded}.
     * @param resource the resource whose visibility should be tested
     * @return true, if the resource is contained in the classpath and visible;
     * false otherwise.
     */
    public final boolean isResourceVisible(FileObject resource) {
        String resourceName = getResourceName(resource);
        if (resourceName == null)
            return false;
        return findResource(resourceName) == resource;
    }

    /**
     * Adds a property change listener to the bean.
     * @param l a listener to add
     */
    public final synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        attachRootsListener ();        
        propSupport.addPropertyChangeListener(l);
    }

    /**
     * Removes the listener registered by {@link addPropertyChangeListener}.
     * @param l a listener to remove
     */
    public final void removePropertyChangeListener(PropertyChangeListener l) {        
        propSupport.removePropertyChangeListener(l);        
    }

    /**
     * Find the classpath of a given type, if any, defined for a given file.
     * <p>This method may return null, if:</p>
     * <ul>
     * <li>the path type (<code>id</code> parameter) is not recognized
     * <li>the path type is not defined for the given file object
     * </ul>
     * <p>
     * Generally you may pass either an individual Java file, or the root of
     * a Java package tree, interchangeably, since in most cases all files
     * in a given tree will share a single classpath.
     * </p>
     * <p class="nonnormative">
     * Typically classpaths for files are defined by the owning project, but
     * there may be other ways classpaths are defined. See {@link ClassPathProvider}
     * for more details.
     * </p>
     * @param f the file, whose classpath settings should be returned (may <em>not</em> be null as of org.netbeans.api.java/1 1.4)
     * @param id the type of the classpath (e.g. {@link #COMPILE})
     * @return classpath of the desired type for the given file object, or <code>null</code>, if
     *         there is no classpath available
     * @see ClassPathProvider
     */
    public static ClassPath getClassPath(FileObject f, String id) {
        if (f == null) {
            // What else can we do?? Backwards compatibility only.
            Thread.dumpStack();
            return null;
        }
        for (ClassPathProvider impl  : implementations.allInstances()) {
            ClassPath cp = impl.findClassPath(f, id);
            if (cp != null) {
                LOG.log(Level.FINE, "getClassPath({0}, {1}) -> {2} from {3}", new Object[] {f, id, cp, impl});
                return cp;
            }
        }
        LOG.log(Level.FINE, "getClassPath({0}, {1}) -> nil", new Object[] {f, id});
        return null;
    }

    /**
     * Fires a property change event on the specified property, notifying the
     * old and new values.
     * @param what name of the property
     * @param oldV old value
     * @param newV new value
     */
    final void firePropertyChange(final String what, final Object oldV, final Object newV, final Object propagationId) {	
        final PropertyChangeEvent event = new PropertyChangeEvent (this, what, oldV, newV);
        event.setPropagationId(propagationId);
	propSupport.firePropertyChange(event);
    }

    /**
     * Policy for handling path items which cannot be converted into the desired format.
     * @see #toString(ClassPath.PathConversionMode)
     * @since org.netbeans.api.java/1 1.15
     */
    public enum PathConversionMode {
        /**
         * Drop entry silently.
         */
        SKIP,
        /**
         * Entry is dropped but a warning is logged.
         */
        WARN,
        /**
         * {@link #toString(ClassPath.PathConversionMode)} will fail with an {@link IllegalArgumentException}.
         */
        FAIL,
        /**
         * The entry is simply displayed as a URL.
         * Useful for debug logging or unit tests.
         * Used by {@link ClassPath#toString}.
         */
        PRINT,
    }

    /**
     * Render this classpath in the conventional format used by the Java launcher.
     * @param conversionMode policy for converting unusual entries
     * @return a conventionally-formatted representation of the classpath
     * @since org.netbeans.api.java/1 1.15
     * @see File#pathSeparator
     * @see FileUtil#archiveOrDirForURL
     * @see ClassPathSupport#createClassPath(String)
     */
    public String toString(PathConversionMode conversionMode) {
        StringBuilder b = new StringBuilder();
        for (Entry e : entries()) {
            URL u = e.getURL();
            File f = FileUtil.archiveOrDirForURL(u);
            if (f != null) {
                if (b.length() > 0) {
                    b.append(File.pathSeparatorChar);
                }
                b.append(f.getAbsolutePath());
            } else {
                switch (conversionMode) {
                case SKIP:
                    break;
                case PRINT:
                    if (b.length() > 0) {
                        b.append(File.pathSeparatorChar);
                    }
                    b.append(u);
                    break;
                case WARN:
                    LOG.log(Level.WARNING, "Encountered untranslatable classpath entry: {0}", u);
                    break;
                case FAIL:
                    throw new IllegalArgumentException("Encountered untranslatable classpath entry: " + u); // NOI18N
                default:
                    assert false : conversionMode;
                }
            }
        }
        return b.toString();
    }

    public @Override String toString() {
        return toString(PathConversionMode.PRINT);
    }

    /**
     * Represents an individual entry in the ClassPath. An entry is a description
     * of a folder, which is one of the ClassPath roots. Since the Entry does not
     * control the folder's lifetime, the folder may be removed and the entry
     * becomes invalid. It's also expected that ClassPath implementations may
     * use other ClassPath entries as default or base for themselves, so Entries
     * may be propagated between ClassPaths.
     */
    public final class Entry {

        private final URL url;
        private FileObject root;
        private IOException lastError;
        private FilteringPathResourceImplementation filter;

        /**
         * Returns the ClassPath instance, which defines/introduces the Entry.
         * Note that the return value may differ from the ClassPath instance,
         * that produced this Entry from its <code>entries()</code> method.
         * @return the ClassPath that defines the entry.
         */
        public ClassPath   getDefiningClassPath() {
            return ClassPath.this;
        }

        /**
         * The method returns the root folder represented by the Entry.
         * If the folder does not exist, or the folder is not readable,
         * the method may return null.
         * @return classpath entry root folder
         */
        public  FileObject  getRoot() {
            synchronized (this) {
                if (root != null && root.isValid()) {
                    return root;
                }
            }
            FileObject _root = URLMapper.findFileObject(this.url);            
            synchronized (this) {
                if (root == null || !root.isValid()) {
                    if (_root == null) {
                        this.lastError = new IOException(MessageFormat.format("The package root {0} does not exist or can not be read.",
                            new Object[] {this.url}));
                        return null;
                    }
                    else if (_root.isData()) {
                        throw new IllegalArgumentException ("Invalid ClassPath root: "+this.url+". The root must be a folder.");
                    }
                    root = _root;
                }
                return root;
            }
        }

        /**
         * @return true, iff the Entry refers to an existing and readable
         * folder.
         */
        public boolean isValid() {
            FileObject r = getRoot();
            return r != null && r.isValid();
        }

        /**
         * Retrieves the error condition of the Entry. The method will return
         * null, if the <code>getRoot()</code> would return a FileObject.
         * @return error condition for this Entry or null if the Entry is OK.
         */
        public IOException getError() {
            IOException error = this.lastError;
            this.lastError = null;
            return error;
        }

        /**
         * Returns URL of the class path root.
         * This method is generally safer than {@link #getRoot} as
         * it can be called even if the root does not currently exist.
         * @return URL
         * @since org.netbeans.api.java/1 1.4
         */
        public URL getURL () {
            return this.url;
        }

        /**
         * Check whether a file is included in this entry.
         * @param resource a path relative to @{link #getURL} (must be terminated with <samp>/</samp> if a non-root folder)
         * @return true if it is {@link FilteringPathResourceImplementation#includes included}
         * @since org.netbeans.api.java/1 1.13
         */
        public boolean includes(String resource) {
            return filter == null || filter.includes(url, resource);
        }

        /**
         * Check whether a file is included in this entry.
         * @param file a URL beneath @{link #getURL}
         * @return true if it is {@link FilteringPathResourceImplementation#includes included}
         * @throws IllegalArgumentException in case the argument is not beneath {@link #getURL}
         * @since org.netbeans.api.java/1 1.13
         */
        public boolean includes(URL file) {
            if (!file.toExternalForm().startsWith(url.toExternalForm())) {
                throw new IllegalArgumentException(file + " not in " + url);
            }
            URI relative;
            try {
                relative = url.toURI().relativize(file.toURI());
            } catch (URISyntaxException x) {
                throw new AssertionError(x);
            }
            assert !relative.isAbsolute() : "could not locate " + file + " in " + url;
            return filter == null || filter.includes(url, relative.toString());
        }

        /**
         * Check whether a file is included in this entry.
         * @param file a file inside @{link #getRoot}
         * @return true if it is {@link FilteringPathResourceImplementation#includes included}
         * @throws IllegalArgumentException in case the argument is not beneath {@link #getRoot}, or {@link #getRoot} is null
         * @since org.netbeans.api.java/1 1.13
         */
        public boolean includes(FileObject file) {
            FileObject r = getRoot();
            if (r == null) {
                throw new IllegalArgumentException("no root in " + url);
            }
            String path = FileUtil.getRelativePath(r, file);
            if (path == null) {
                if (!file.isValid()) {
                    //#130998:IllegalArgumentException when switching tabs
                    return false;
                }
                throw new IllegalArgumentException(file + " (valid: " + file.isValid() + ") not in " + r + " (valid: " + r.isValid() + ")"); //NOI18N
            }
            if (file.isFolder()) {
                path += "/"; // NOI18N
            }
            return filter == null || filter.includes(url, path);
        }

        Entry(URL url, FilteringPathResourceImplementation filter) {
            this.url = url;
            this.filter = filter;
        }

        public @Override String toString() {
            return "Entry[" + url + "]"; // NOI18N
        }
        
        @Override
        public boolean equals (Object other) {
            if (other instanceof ClassPath.Entry) {
                return Utilities.compareObjects(((ClassPath.Entry)other).url, this.url);
            }
            return false;
        }
        
        @Override
        public int hashCode () {
            return this.url == null ? 0 : this.url.hashCode();
        }
    }

    //-------------------- Implementation details ------------------------//

    private final PropertyChangeSupport propSupport;
    
    
    /**
     * Attaches the listener to the ClassPath.entries.
     * Not synchronized, HAS TO be called from the synchronized block!
     */
    private void attachRootsListener () {
        if (this.rootsListener == null) {
            assert this.rootsCache == null;
            this.rootsListener = new RootsListener (this);
        }
    }

    /**
     * Returns an array of pairs of strings, first string in the pair is the
     * name, the next one is either the extension or null.
     */
    private static String[] parseResourceName(String name) {
        Collection<String> parsed = new ArrayList<String>(name.length() / 4);
        char[] chars = name.toCharArray();
        char ch;
        int pos = 0;
        int dotPos = -1;
        int startPos = 0;

        while (pos < chars.length) {
            ch = chars[pos];
            switch (ch) {
                case '.':
                    dotPos = pos;
                    break;
                case '/':
                    // end of name component
                    if (dotPos != -1) {
                        parsed.add(name.substring(startPos, dotPos));
                        parsed.add(name.substring(dotPos + 1, pos));
                    } else {
                        parsed.add(name.substring(startPos, pos));
                        parsed.add(null);
                    }
                    // reset variables:
                    startPos = pos + 1;
                    dotPos = -1;
                    break;
            }
            pos++;
        }
        // if the resource name ends with '/', just ignore the empty component
        if (pos > startPos) {
            if (dotPos != -1) {
                parsed.add(name.substring(startPos, dotPos));
                parsed.add(name.substring(dotPos + 1, pos));
            } else {
                parsed.add(name.substring(startPos, pos));
                parsed.add(null);
            }
        }
        if ((parsed.size()  % 2) != 0) {
            System.err.println("parsed size is not even!!");
            System.err.println("input = " + name);
        }
        return parsed.toArray(new String[parsed.size()]);
    }

    /**
     * Finds a path underneath the `parent'. Name parts is an array of string pairs,
     * the first String in a pair is the basename, the second is the extension or null
     * for no extension.
     */
    private static FileObject findPath(FileObject parent, String[] nameParts) {
        FileObject child;

        for (int i = 0; i < nameParts.length && parent != null; i += 2, parent = child) {
            child = parent.getFileObject(nameParts[i], nameParts[i + 1]);
        }
        return parent;
    }

    /**
     * Searches for a resource in one or more roots, gives back the index of
     * the first untouched root.
     */
    private FileObject findResourceImpl(FileObject[] roots,
        int[] rootIndex, String[] nameComponents) {
        int ridx;
        FileObject f = null;
        for (ridx = rootIndex[0]; ridx < roots.length && f == null; ridx++) {
            f = findPath(roots[ridx], nameComponents);
            FilteringPathResourceImplementation filter = root2Filter.get(roots[ridx]);
            if (filter != null) {
                try {
                    if (f != null) {
                        String path = FileUtil.getRelativePath(roots[ridx], f);
                        assert path != null;
                        if (f.isFolder()) {
                            path += "/"; // NOI18N
                        }
                        if (!filter.includes(roots[ridx].getURL(), path)) {
                            f = null;
                        }
                    }
                } catch (FileStateInvalidException x) {
                    throw new AssertionError(x);
                }
            }
        }
        rootIndex[0] = ridx;
        return f;
    }

    private static final Reference<ClassLoader> EMPTY_REF = new SoftReference<ClassLoader>(null);

    private Reference<ClassLoader> refClassLoader = EMPTY_REF;

    /* package private */synchronized void resetClassLoader(ClassLoader cl) {
        if (refClassLoader.get() == cl)
            refClassLoader = EMPTY_REF;
    }

    /**
     * Returns a ClassLoader for loading classes from this ClassPath.
     * <p>
     * If <code>cache</code> is false, then
     * the method will always return a new class loader. If that parameter is true,
     * the method may return a loader which survived from a previous call to the same <code>ClassPath</code>.
     *
     * @param cache true if it is permissible to cache class loaders between calls
     * @return class loader which uses the roots in this class path to search for classes and resources
     * @since 1.2.1
     */
    public final synchronized ClassLoader getClassLoader(boolean cache) {
        // XXX consider adding ClassLoader and/or InputOutput and/or PermissionCollection params
        ClassLoader o = refClassLoader.get();
        if (!cache || o == null) {
            o = ClassLoaderSupport.create(this);
            refClassLoader = new SoftReference<ClassLoader>(o);
        }
        return o;
    }


    private class SPIListener implements PropertyChangeListener {
        private Object propIncludesPropagationId;
        public void propertyChange(PropertyChangeEvent evt) {
            String prop = evt.getPropertyName();
            if (ClassPathImplementation.PROP_RESOURCES.equals(prop) || PathResourceImplementation.PROP_ROOTS.equals(prop)) {
                synchronized (ClassPath.this) {
                    if (rootsListener != null) {
                        rootsListener.removeAllRoots ();
                    }
                    entriesCache = null;
                    rootsCache = null;
                    invalidEntries++;
                    invalidRoots++;
                }
                firePropertyChange (PROP_ENTRIES,null,null,null);
                firePropertyChange (PROP_ROOTS,null,null,null);
            } else if (FilteringPathResourceImplementation.PROP_INCLUDES.equals(prop)) {
                boolean fire;
                synchronized (this) {
                    Object id = evt.getPropagationId();
                    fire = propIncludesPropagationId == null || !propIncludesPropagationId.equals(id);
                    propIncludesPropagationId = id;
                }
                if (fire) {
                    firePropertyChange(PROP_INCLUDES, null, null, evt.getPropagationId());
                }
            }
            if (ClassPathImplementation.PROP_RESOURCES.equals(prop)) {
                final List<? extends PathResourceImplementation> resources = impl.getResources();
                if (resources == null) {
                    LOG.log(Level.WARNING, "ClassPathImplementation.getResources cannot return null; impl class: {0}", impl.getClass().getName());
                    return;
                }
                for (PathResourceImplementation pri : resources) {
                    pri.removePropertyChangeListener(weakPListener);
                    pri.addPropertyChangeListener(weakPListener = WeakListeners.propertyChange(pListener, pri));
                }
            }
        }
    }


    private synchronized RootsListener getRootsListener () {
        return this.rootsListener;
    }


    private static class RootsListener extends WeakReference<ClassPath> implements FileChangeListener, Runnable {

        private final Set<File> roots;

        private RootsListener (ClassPath owner) {
            super (owner, Utilities.activeReferenceQueue());
            roots = new HashSet<File> ();
        }

        public void addRoots (final Set<? extends URL> urls) {
            Parameters.notNull("urls",urls);    //NOI18N
            final Set<File> newRoots = new HashSet<File>();
            for (URL url : urls) {
                if ("jar".equals(url.getProtocol())) { //NOI18N
                    url = FileUtil.getArchiveFile(url);
                }
                if (!"file".equals(url.getProtocol())) { // NOI18N
                    // Try to convert nbinst to file
                    FileObject file = URLMapper.findFileObject(url);
                    if (file != null) {
                        URL external = URLMapper.findURL(file, URLMapper.EXTERNAL);
                        if (external != null) {
                            url = external;
                        }
                    }
                }
                try {
                    //todo: Ignore non file urls, we can try to url->fileobject->url
                    //if it becomes a file.
                    if ("file".equals(url.getProtocol())) { //NOI18N
                        newRoots.add(new File(url.toURI()));
                    }
                } catch (IllegalArgumentException e) {
                    LOG.log(Level.WARNING, "Unexpected URL <{0}>: {1}", new Object[] {url, e});
                    //pass
                } catch (URISyntaxException e) {
                    LOG.log(Level.WARNING, "Invalid URL: {0}", url);
                    //pass
                }
            }
            synchronized (this) {
                final Set<File> toRemove = new HashSet<File>(roots);
                toRemove.removeAll(newRoots);
                final Set<File> toAdd = new HashSet<File>(newRoots);
                toAdd.removeAll(roots);
                for (File root : toRemove) {
                    FileUtil.removeFileChangeListener(this, root);
                    roots.remove(root);
                }
                for (File root : toAdd) {
                    FileUtil.addFileChangeListener(this, root);
                    roots.add (root);
                }
            }
        }


        public synchronized void removeAllRoots () {
            for (File root : roots) {
                FileUtil.removeFileChangeListener(this, root);
            }
            this.roots.clear();
        }

        public void fileFolderCreated(FileEvent fe) {
            this.processEvent (fe);
        }

        public void fileDataCreated(FileEvent fe) {
            this.processEvent (fe);
        }

        public void fileChanged(FileEvent fe) {
            processEvent(fe);
        }

        public void fileDeleted(FileEvent fe) {
            this.processEvent (fe);
        }

        public void fileRenamed(FileRenameEvent fe) {
            this.processEvent (fe);
        }

        public void fileAttributeChanged(FileAttributeEvent fe) {
        }

        public void run() {
            removeAllRoots();
        }

        private void processEvent (FileEvent fe) {
            final ClassPath cp = get();
            if (cp == null) {
                return;
            }
            synchronized (cp) {
                cp.rootsCache = null;
                cp.invalidRoots++;
            }
            cp.firePropertyChange(PROP_ROOTS,null,null,null);
        }
    }
}
