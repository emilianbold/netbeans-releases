/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.java.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.EventListener;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.LinkedList;
import java.io.IOException;

import org.openide.cookies.InstanceCookie;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.Repository;


/**
 * ClassPath objects should be used to access contents of the ClassPath, searching
 * for resources, objects reachable using the ClassPath at runtime. It is intended
 * to replace some of the functionality of <link>org.openide.filesystems.Repository</link>.
 * <BR>
 * ClassPath instances should be used to map from java-style resource names 
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
 * (compiler, debugger, executor). There is at most one classpath instance for
 * a given name, but names are not limited to the manifested ones; an extension
 * module might add its own private classpath in the future. ClassPath instances
 * for the predefined names are <I>always available</I>, so <code>getClassPath</code>
 * never returns <code>null</code>.
 *
 * @author Svatopluk Dedic <sdedic@netbeans.org>
 */
public abstract class ClassPath {
    /**
     * Classpath setting for executing things
     */
    public static final String EXECUTE = "classpath/execute";
    
    /**
     * Classpath for debugging things
     */
    public static final String DEBUG = "classpath/debug";
    
    /**
     * ClassPath for compiling things
     */
    public static final String COMPILE = "classpath/compile";
    
    /**
     * Name of the "roots" property
     */
    public static final String PROP_ROOTS   = "roots";
    
    /**
     * Name of the "entries" property
     */
    public static final String PROP_ENTRIES = "entries";
    
    /**
     * Retrieves valid roots of ClassPath, in the proper order.
     * If there's an entry in the ClassPath, which cannot be accessed,
     * its root is not returned by this method. FileObjects returned
     * are all folders.
     * @return array of roots (folders) of the classpath. Never returns
     * null.
     */
    public abstract FileObject[]  getRoots();
    
    /**
     * Returns list of classpath entries from the ClassPath definition.
     * The implementation must ensure that modifications done to the List are
     * banned or at least not reflected in other Lists returned by this ClassPath 
     * instance. Clients must assume that the returned value is immutable.
     * @return list of definition entries (Entry instances)
     */
    public abstract List entries();
    
    ClassPath() {
        // just to prevent subclassing ;-) Will disappear in 4.0 release.
    }

    /**
     * Returns a FileObject for the specified resource. May return null,
     * if the resource does not exist, or is not reachable through
     * this ClassPath.<BR>
     * If the <i>resourceName</i> identifies a package, this method will
     * return the <code>FileObject</code> for the first <I>package fragment</I>
     * in the <code>ClassPath</code>
     * Note: do not pass names starting with slash to this method.
     * @param resourceName name of the resource as it would be passed
     * to <link>java.lang.ClassLoader.getResource()</link>.
     * @return FileObject for the resource, or null if the resource cannot
     * be found in this ClassPath.
     */
    public final FileObject findResource(String resourceName) {
	FileObject[] roots = getRoots();
        return findResourceImpl(roots, new int[] { 0 }, parseResourceName(resourceName));
    }
    
    /**
     * Gives out an ordered collection containing all FileObjects, which correspond
     * to a given ResourceName; only the first one is seen by the ClassLoader
     * at runtime or can be linked against.  The resource name uses slashes ('/')
     * as folder separator and must not start with slash.
     * @param resourceName resource name
     * @return list of resources identified by the given name.
     */
    public final List findAllResources(String resourceName) {
	FileObject[] roots = getRoots();
        List l = new ArrayList(roots.length);
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
     * (it is hidden by some other resource), the resource name is still returned.
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
        if (owner == f) 
            return ""; // NOI18N
        String ownerName = owner.toString();
        int onl = ownerName.length();
        String partName;
        if (includeExt)
            partName = f.getPackageNameExt(dirSep, '.');
        else
            partName = f.getPackageName(dirSep);
        return onl == 0 ? partName : partName.substring(onl + 1);
    }
    
    /**
     * Finds a root in this ClassPath, that owns the given file. File resources, that
     * are not reachable (they are hidden by other resources) are still considered
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
        List parts = new LinkedList();
        for (FileObject f = resource; f != null; f = f.getParent()) {
            parts.add(f);
        } 
        for (int i = 0; i < roots.length; i++) {
            FileObject rc = roots[i];
            try {
                if (rc.getFileSystem() != resource.getFileSystem())
                    continue;
            } catch (FileStateInvalidException ex) {
                // just ignore.
            }
            if (parts.contains(rc))
                return rc;
        }
        return null;
    }
    
    /**
     * Convenience method, which checks whether a FileObject lies on this
     * classpath. It is an equivalent of <code>getResourceName(f) != null</code>
     * @return true, if the parameter is inside one of the classpath subtrees,
     * false otherwise.
     * @param f the FileObject to check
     */
    public final boolean contains(FileObject f) {
        return findOwnerRoot(f) != null;
    }
    
    /**
     * Determines if the resource is <i>visible</i> in the classpath,
     * that is if the file will be reached when a process attempts to
     * load a resource of that name. It will return false when the resource
     * is not contained in the classpath.
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
     */
    public final synchronized void addPropertyChangeListener(PropertyChangeListener l) {
	if (propSupport == null)
	    propSupport = new PropertyChangeSupport(this);
	propSupport.addPropertyChangeListener(l);
    }
    
    /**
     * Removes the listener registered by <code>addPropertyChangeListener</code>/
     */
    public final void removePropertyChangeListener(PropertyChangeListener l) {
	if (propSupport != null)
	    propSupport.removePropertyChangeListener(l);
    }

    /**
     * Retrieves the instance of ClassPath type identified by `id', which is
     * configured for the FileObject's owner. If <code>null</code> is passed
     * instead of FileObject, the active project's default ClassPath is retrieved.
     * The method uses <code>FileObject</code> instead <code>org.openide.loaders.DataObject</code>,
     * since <code>org.openide.loaders.*</code> contents are going to be deprecated soon.
     * @param f the file, whose classpath settings should be returned,
     * if null then active project's setting is retrieved.<br>
     * The method is permitted to return null, if:<ul>
     * <li>the path type (id parameter) is not know to the system
     * <li>the path type is not defined for the given FileObject
     * </ul> 
     * @param id the type of the ClassPath
     * @return Path of the desired type for the given FileObject, or <code>null</code>, if
     * the path type is not supported for that FileObject.
     */
    public static ClassPath getClassPath(FileObject f, String id) {
        return getClassPathImpl(id);
    }
    
    /**
     * Fires a property change event on the specified property, notifying the
     * old and new values.
     * @param what name of the property
     * @param oldV old value
     * @param newV new value
     */
    final void firePropertyChange(String what, Object oldV, Object newV) {
	if (propSupport == null)
	    return;
	propSupport.firePropertyChange(what, oldV, newV);
    }
    
    /**
     * Represents an individual entry in the ClassPath. An entry is a description
     * of a folder, which is one of the ClassPath roots. Since the Entry does not
     * control the folder's lifetime, the folder may be removed and the entry
     * becomes invalid. It's also expected that ClassPath implementations may
     * use other ClassPath entries as default or base for themselves, so Entries
     * may be propagated between ClassPaths.
     *
     * <I>Please note:</I>
     * This class is not intended to be implemented by regular clients. The project
     * infrastructure and / or java language support modules should implement it
     * and offer its services to others. <B>Do not implement this class</B>
     */
    public abstract class Entry {
        /**
         * Returns the ClassPath instance, which defines/introduces the Entry.
         * Note that the return value may differ from the ClassPath instance,
         * that produced this Entry from its <code>entries()</code> method.
         * @return the ClassPath that defines the entry.
         */
        public abstract ClassPath   getDefiningClassPath();
        
        /**
         * The method returns the root folder represented by the Entry.
         * If the folder does not exist, or the folder is not readable, 
         * the method may return null.
         * @return classpath entry root folder
         */
        public abstract FileObject  getRoot();
        
        /**
         * @return true, iff the Entry refers to an existing and readable
         * folder.
         */
        public abstract boolean isValid();
        
        /**
         * Retrieves the error condition of the Entry. The method will return
         * null, if the <code>getRoot()</code> would return a FileObject.
         * @return error condition for this Entry or null if the Entry is OK.
         */
        public abstract IOException getError();

        Entry() {
            // prevent subclassing and also exposing impl details.
        }
    }
    
    //-------------------- Implementation details ------------------------//
    /**
     * Prefix for the factory registration folder
     */
    private static final String REGISTRATION_PREFIX = "org-netbeans-modules-java/";
    
    private PropertyChangeSupport   propSupport;
    
    private static ClassPath getClassPathImpl(String id) {
        return RepositoryL.get().getClassPath(id);
    }
    
    /**
     * Returns an array of pairs of strings, first string in the pair is the 
     * name, the next one is either the extension or null.
     */
    private static String[] parseResourceName(String name) {
        Collection parsed = new ArrayList(name.length() / 4);
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
        return (String[])parsed.toArray(new String[parsed.size()]);
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
    private static FileObject findResourceImpl(FileObject[] roots,
        int[] rootIndex, String[] nameComponents) {
        int ridx;
        FileObject f = null;
        for (ridx = rootIndex[0]; ridx < roots.length && f == null; ridx++) {
            f = findPath(roots[ridx], nameComponents);
        }
        rootIndex[0] = ridx;
        return f;
    }

    private static final java.lang.ref.Reference EMPTY_REF = new java.lang.ref.SoftReference(null);

    private java.lang.ref.Reference refClassLoader = EMPTY_REF;

    /* package private */synchronized void resetClassLoader(ClassLoader cl) {
        if (refClassLoader.get() == cl)
            refClassLoader = EMPTY_REF;
    }

    /**
     * Returns a ClassLoader for loading classes from this ClassPath. If `cache' is false, then
     * the method will always return a new initialized instance of ClassLoader. If that parameter is true,
     * the method may return a ClassLoader which survived from a previous call.
     *
     * @param cache True, if a new ClassLoader is requested
     * @return ClassLoader that loads classes
     * @since 1.2.1
     */
    public final synchronized ClassLoader getClassLoader(boolean cache) {
        Object o = refClassLoader.get();
        if (!cache || o == null) {
            o = new ClassLoaderSupport(this);
            refClassLoader = new java.lang.ref.SoftReference(o);
        }
        return (ClassLoader)o;
    }
}
