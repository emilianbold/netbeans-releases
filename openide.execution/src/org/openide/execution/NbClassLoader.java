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

package org.openide.execution;

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.Manifest;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;
import org.openide.windows.InputOutput;

/** A class loader which is capable of loading classes from the Repository.
 * XXX the only useful thing this class does is effectively make
 * ExecutionEngine.createPermissions public! Consider deprecating this class...
 * @see <a href="@JAVA/API@/org/netbeans/api/java/classpath/ClassPath.html#getClassLoader(boolean)"><code>ClassPath.getClassLoader(...)</code></a>
* @author Ales Novak, Petr Hamernik, Jaroslav Tulach, Ian Formanek
*/
public class NbClassLoader extends URLClassLoader {
    /** I/O for classes defined by this classloader. May be <code>null</code>. */
    protected InputOutput inout;
    /** Cached PermissionCollections returned from ExecutionEngine. */
    private HashMap permissionCollections;
    /** Default permissions */
    private PermissionCollection defaultPermissions;
    
    private static ClassLoader systemClassLoader() {
        return (ClassLoader)Lookup.getDefault().lookup(ClassLoader.class);
    }
    
    /** Create a new class loader retrieving classes from the core IDE as well as the Repository.
     * @see FileSystemCapability#EXECUTE
     * @see FileSystemCapability#fileSystems
     * @deprecated Misuses classpath.
    */
    public NbClassLoader () {
        super(new URL[0], systemClassLoader());
    }

    /** Create a new class loader retrieving classes from the core IDE as well as the Repository,
    * and redirecting system I/O.
     * @param io an I/O tab in the Output Window
     * @see org.openide.filesystems.Repository#getFileSystems
     * @deprecated Misuses classpath.
     */
    public NbClassLoader(InputOutput io) {
        super(new URL[0], systemClassLoader());
        inout = io;
    }
    
    /**
     * Create a new class loader retrieving classes from a set of package roots.
     * @param roots a set of package roots
     * @param parent a parent loader
     * @param io an I/O tab in the Output Window, or null
     * @throws FileStateInvalidException if some of the roots are not valid
     * @since XXX
     */
    public NbClassLoader(FileObject[] roots, ClassLoader parent, InputOutput io) throws FileStateInvalidException {
        super(createRootURLs(roots), parent);
        inout = io;
    }

    /** Create a new class loader retrieving classes from the core IDE as well as specified file systems.
     * @param fileSystems file systems to load classes from
     * @deprecated Misuses classpath.
    */
    public NbClassLoader (FileSystem[] fileSystems) {
        super(new URL[0], systemClassLoader(), null);
        Thread.dumpStack();
    }

    /** Create a new class loader.
     * @param fileSystems file systems to load classes from
     * @param parent fallback class loader
     * @deprecated Misuses classpath.
    */
    public NbClassLoader (FileSystem[] fileSystems, ClassLoader parent) {
        super(new URL[0], parent);
        Thread.dumpStack();
    }

    /** Create a URL to a resource specified by name.
    * Same behavior as in the super method, but handles names beginning with a slash.
    * @param name resource name
    * @return URL to that resource or <code>null</code>
    */
    public URL getResource (String name) {
        return super.getResource (name.startsWith ("/") ? name.substring (1) : name); // NOI18N
    }

    /* Needs to be overridden so that packages are correctly defined
       based on manifest for e.g. JarFileSystem's in the repository.
       Otherwise URLClassLoader, not understanding nbfs:/..../foo.jar,
       would simply define packages loaded from such a URL with no
       particular info. We want it to have specification version and
       all that good stuff. */
    protected Class findClass (final String name) throws ClassNotFoundException {
        if (name.indexOf ('.') != -1) {
            String pkg = name.substring (0, name.lastIndexOf ('.'));
            if (getPackage (pkg) == null) {
                String resource = name.replace ('.', '/') + ".class"; // NOI18N
                URL[] urls = getURLs ();
                for (int i = 0; i < urls.length; i++) {
                    //System.err.println (urls[i].toString ());
                    FileObject root = URLMapper.findFileObject(urls[i]);
                    if (root == null) {
                        continue; // pretty normal, e.g. non-nbfs: URL
                    }
                    try {
                        FileObject fo = root.getFileObject(resource);
                        if (fo != null) {
                            // Got it. If there is an associated manifest, load it.
                            FileObject manifo = root.getFileObject("META-INF/MANIFEST.MF"); // NOI18N
                            if (manifo == null) manifo = root.getFileObject("meta-inf/manifest.mf"); // NOI18N
                            if (manifo != null) {
                                //System.err.println (manifo.toString () + " " + manifo.getClass ().getName () + " " + manifo.isValid ());
                                Manifest mani = new Manifest ();
                                InputStream is = manifo.getInputStream ();
                                try {
                                    mani.read (is);
                                } finally {
                                    is.close ();
                                }
                                definePackage (pkg, mani, urls[i]);
                            }
                            break;
                        }
                    } catch (IOException ioe) {
                        ErrorManager err = ErrorManager.getDefault();
                        err.annotate (ioe, urls[i].toString ());
                        err.notify (ErrorManager.INFORMATIONAL, ioe);
                        continue;
                    }
                }
            }
        }
        return super.findClass (name);
    }
    
    /** Sets a PermissionsCollectio which will be used
     * for ProtectionDomain of newly created classes.
     *
     * @param defaultPerms
     */
    public void setDefaultPermissions(PermissionCollection defaultPerms) {
        if (defaultPerms != null && !defaultPerms.isReadOnly()) {
            defaultPerms.setReadOnly();
        }
        this.defaultPermissions = defaultPerms;
    }

    /* @return a PermissionCollection for given CodeSource. */
    protected final synchronized PermissionCollection getPermissions(CodeSource cs) {

        if (permissionCollections != null) {
            PermissionCollection pc = (PermissionCollection) permissionCollections.get(cs);
            if (pc != null) {
                return pc;
            }
        }

        return createPermissions(cs, inout);
    }

    /**
    * @param cs CodeSource
    * @param inout InputOutput passed to @seeExecutionEngine#createPermissions(java.security.CodeSource, org.openide.windows.InpuOutput).
    * @return a PermissionCollection for given CodeSource.
    */
    private PermissionCollection createPermissions(CodeSource cs, InputOutput inout) {
        PermissionCollection pc;
        if (inout == null) {
            if (defaultPermissions != null) {
                pc = defaultPermissions;
            } else {
                pc = super.getPermissions(cs);
            }
        } else {
            ExecutionEngine engine = ExecutionEngine.getDefault();
            pc = engine.createPermissions(cs, inout);
            if (defaultPermissions != null) {
                addAllPermissions(pc, defaultPermissions);
            } else {
                pc.add(new AllPermission());
            }
        }
        if (permissionCollections == null) {
            permissionCollections = new HashMap(7);
        }
        permissionCollections.put(cs, pc);
        return pc;
    }
    
    /**
     * Copies all permissions from <tt>src</tt> into <tt>target</tt>
     *
     * @param target To where put permissions
     * @param src From where take paermissions
     */
    private static void addAllPermissions(PermissionCollection target, PermissionCollection src) {
        Enumeration e = src.elements();
        
        while (e.hasMoreElements()) {
            target.add((Permission) e.nextElement());
        }
    }


    /**
     * Creates URLs for file objects.
     * @param roots file roots
     * @return array of URLs
     */
    private static URL[] createRootURLs(FileObject[] roots) throws FileStateInvalidException {
        URL[] urls = new URL[roots.length];
        for (int i = 0; i < roots.length; i++) {
            urls[i] = roots[i].getURL();
        }
        return urls;
    }
}
