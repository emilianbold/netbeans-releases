/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.execution;

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.PermissionCollection;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Collections;
import java.util.jar.Manifest;

import org.openide.ErrorManager;
import org.openide.filesystems.*;
import org.openide.util.Lookup;
import org.openide.windows.InputOutput;

// ClassLoader constructs URL (NbfsURL) in this way:
// "protocol"://"fs_name"#"package"/"name.extension" // NOI18N
/** A class loader which is capable of loading classes from the Repository.
* Classes loaded from file systems in the repository are handled by {@link NbfsStreamHandlerFactory}.
*
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
        super (
            createRootURLs (FileSystemCapability.EXECUTE.fileSystems ()),
            systemClassLoader()
        );
    }

    /** Create a new class loader retrieving classes from the core IDE as well as the Repository,
    * and redirecting system I/O.
     * @param io an I/O tab in the Output Window
     * @see org.openide.filesystems.Repository#getFileSystems
     * @deprecated Misuses classpath.
     */
    public NbClassLoader(InputOutput io) {
        this();
        inout = io;
    }

    /** Create a new class loader retrieving classes from the core IDE as well as specified file systems.
     * @param fileSystems file systems to load classes from
     * @deprecated Misuses classpath.
    */
    public NbClassLoader (FileSystem[] fileSystems) {
        this(fileSystems, systemClassLoader());
    }

    /** Create a new class loader.
     * @param fileSystems file systems to load classes from
     * @param parent fallback class loader
     * @deprecated Misuses classpath.
    */
    public NbClassLoader (FileSystem[] fileSystems, ClassLoader parent) {
        super (
            createRootURLs (Collections.enumeration (Arrays.asList (fileSystems))),
            parent
        );
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
                    FileObject root = NbfsURLConnection.decodeURL (urls[i]);
                    if (root == null) continue; // pretty normal, e.g. non-nbfs: URL
                    if (! root.isRoot ()) continue; // only want to load from roots of FSs
                    try {
                        FileSystem fs = root.getFileSystem ();
                        //System.err.println (fs.toString () + ": " + fs.getDisplayName ());
                        FileObject fo = fs.findResource (resource);
                        if (fo != null) {
                            // Got it. If there is an associated manifest, load it.
                            FileObject manifo = fs.findResource ("META-INF/MANIFEST.MF"); // NOI18N
                            if (manifo == null) manifo = fs.findResource ("meta-inf/manifest.mf"); // NOI18N
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
                pc.add(new java.security.AllPermission());
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
        Enumeration enum = src.elements();
        
        while (enum.hasMoreElements()) {
            target.add((java.security.Permission) enum.nextElement());
        }
    }


    /** Creates urls for filesystems.
    * @param enumeration of FileSystems
    * @return array of urls
    */
    private static URL[] createRootURLs (Enumeration en) {
        ArrayList list = new ArrayList ();
        while (en.hasMoreElements ()) {
            FileSystem fs = (FileSystem)en.nextElement ();
            try {
                list.add (fs.getRoot ().getURL ());
            } catch (FileStateInvalidException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        return (URL[])list.toArray (new URL[list.size()]);
    }
}
