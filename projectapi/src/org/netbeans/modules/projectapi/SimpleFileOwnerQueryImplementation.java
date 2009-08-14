/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.projectapi;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.FileOwnerQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;
import org.openide.util.WeakSet;

/**
 * Finds a project by searching the directory tree.
 * @author Jesse Glick
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.project.FileOwnerQueryImplementation.class, position=100)
public class SimpleFileOwnerQueryImplementation implements FileOwnerQueryImplementation {
    private static final Logger LOG = Logger.getLogger(SimpleFileOwnerQueryImplementation.class.getName());
    
    /** Do nothing */
    public SimpleFileOwnerQueryImplementation() {}
    
    public Project getOwner(URI fileURI) {
        // Try to find a FileObject for it.
        URI test = fileURI;
        FileObject file;
        do {
            file = uri2FileObject(test);
            test = goUp(test);
        } while (file == null && test != null);
        if (file == null) {
            return null;
        }
        return getOwner(file);
    }
    
    private final Set<FileObject> warnedAboutBrokenProjects = new WeakSet<FileObject>();
        
    private Reference<FileObject> lastFoundKey = null;
    private Reference<Project> lastFoundValue = null;
    
    /**
     * 
     * #111892
     */
    public void resetLastFoundReferences() {
        synchronized (this) {
            lastFoundValue = null;
            lastFoundKey = null;
        }
    }
    
    
    public Project getOwner(FileObject f) {
        try {
            if (f.getFileSystem().isDefault()) {
                LOG.log(Level.INFO, null, new IllegalStateException("Call to FOQ on SFS file " + f.getPath()));
            }
        } catch (FileStateInvalidException ex) {
            LOG.log(Level.INFO, null, ex);
        }
        while (f != null) {
            synchronized (this) {
                if (lastFoundKey != null && lastFoundKey.get() == f) {
                    Project p = lastFoundValue.get();
                    if (p != null) {
                        return p;
                    }
                }
            }
            boolean folder = f.isFolder();
            if (folder) {
                Project p;
                try {
                    p = ProjectManager.getDefault().findProject(f);
                } catch (IOException e) {
                    // There is a project here, but we cannot load it...
                    if (warnedAboutBrokenProjects.add(f)) { // #60416
                        LOG.log(Level.FINE, "Cannot load project.", e); //NOI18N
                    }
                    return null;
                }
                if (p != null) {
                    synchronized (this) {
                        lastFoundKey = new WeakReference<FileObject>(f);
                        lastFoundValue = new WeakReference<Project>(p);
                    }
                    return p;
                }
            }
            
            if (!externalOwners.isEmpty() && (folder || externalRootsIncludeNonFolders)) {
                Reference<FileObject> externalOwnersReference = externalOwners.get(fileObject2URI(f));

                if (externalOwnersReference != null) {
                    FileObject externalOwner = externalOwnersReference.get();

                    if (externalOwner != null && externalOwner.isValid()) {
                        try {
                            // Note: will be null if there is no such project.
                            Project p = ProjectManager.getDefault().findProject(externalOwner);
                            synchronized (this) {
                                lastFoundKey = new WeakReference<FileObject>(f);
                                lastFoundValue = new WeakReference<Project>(p);
                            }
                            return p;
                        } catch (IOException e) {
                            // There is a project there, but we cannot load it...
                            LOG.log(Level.FINE, "Cannot load project.", e); //NOI18N
                            return null;
                        }
                    }
                }
            }
            if (!deserializedExternalOwners.isEmpty() && (folder || externalRootsIncludeNonFolders)) {
                FileObject externalOwner = deserializedExternalOwners.get(fileObject2URI(f));
                if (externalOwner != null && externalOwner.isValid()) {
                    try {
                        // Note: will be null if there is no such project.
                        Project p = ProjectManager.getDefault().findProject(externalOwner);
                        synchronized (this) {
                            lastFoundKey = new WeakReference<FileObject>(f);
                            lastFoundValue = new WeakReference<Project>(p);
                        }
                        return p;
                    } catch (IOException e) {
                        // There is a project there, but we cannot load it...
                        LOG.log(Level.FINE, "Cannot load project.", e); //NOI18N
                        return null;
                    }
                }
            }
            
            f = f.getParent();
        }
        return null;
    }
    
    /**
     * Map from external source roots to the owning project directories.
     */
    private static final Map<URI,Reference<FileObject>> externalOwners =
        Collections.synchronizedMap(new WeakHashMap<URI,Reference<FileObject>>());
    
    private static final Map<URI,FileObject> deserializedExternalOwners =
        Collections.synchronizedMap(new HashMap<URI,FileObject>());
    
    private static boolean externalRootsIncludeNonFolders = false;
    private static final Map<FileObject,Collection<URI>> project2External =
        Collections.synchronizedMap(new WeakHashMap<FileObject,Collection<URI>>());
    
    
    static void deserialize() {
        try {
            Preferences p = NbPreferences.forModule(SimpleFileOwnerQueryImplementation.class).node("externalOwners");
            for (String name : p.keys()) {
                URL u = new URL(p.get(name, null));
                URI i = new URI(name);
                deserializedExternalOwners.put(i, URLMapper.findFileObject(u));
            }
        } catch (Exception ex) {
            LOG.log(Level.INFO, null, ex);
        }
        try {
            NbPreferences.forModule(SimpleFileOwnerQueryImplementation.class).node("externalOwners").removeNode();
        } catch (BackingStoreException ex) {
            LOG.log(Level.INFO, null, ex);
        }
    }
    
    static void serialize() {
        try {
            Preferences p = NbPreferences.forModule(SimpleFileOwnerQueryImplementation.class).node("externalOwners");
            for (URI uri : externalOwners.keySet()) {
               Reference<FileObject> fo = externalOwners.get(uri);
               FileObject fileObject = fo.get();
               if (fileObject != null) {
                    p.put(uri.toString(), fileObject.getURL().toExternalForm()); 
               }
            }
        } catch (Exception ex) {
            LOG.log(Level.WARNING, null, ex);
        }
        
    }
    
    /** @see FileOwnerQuery#reset */
    public static void reset() {
        externalOwners.clear();
    }
    
    private static URI fileObject2URI(FileObject f) {
        try {
            return URI.create(f.getURL().toString());
        } catch (FileStateInvalidException e) {
            throw (IllegalArgumentException) new IllegalArgumentException(e.toString()).initCause(e);
        }
    }
    
    /** @see FileOwnerQuery#markExternalOwner */
    public static void markExternalOwnerTransient(FileObject root, Project owner) {
        markExternalOwnerTransient(fileObject2URI(root), owner);
    }
    
    /** @see FileOwnerQuery#markExternalOwner */
    public static void markExternalOwnerTransient(URI root, Project owner) {
        externalRootsIncludeNonFolders |= !root.getPath().endsWith("/");
        if (owner != null) {
            FileObject fo = owner.getProjectDirectory();
            externalOwners.put(root, new WeakReference<FileObject>(fo));
            deserializedExternalOwners.remove(root);
            synchronized (project2External) {
                FileObject prjDir = owner.getProjectDirectory();
                Collection<URI> roots = project2External.get (prjDir);
                if (roots == null) {
                    roots = new LinkedList<URI>();
                    project2External.put(prjDir, roots);
                }
                roots.add (root);                
            }
        } else {
            Reference<FileObject> ownerReference = externalOwners.remove(root);
            
            if (ownerReference != null) {
                FileObject ownerFO = ownerReference.get();
                
                if (ownerFO != null) {
                    synchronized (project2External) {
                        Collection<URI> roots = project2External.get(ownerFO);
                        if (roots != null) {
                            roots.remove(root);
                            if (roots.size() == 0) {
                                project2External.remove(ownerFO);
                            }
                        }
                    }
                }
            }
        }
    }
    
    private static FileObject uri2FileObject(URI u) {
        URL url;
        try {
            url = u.toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            assert false : u;
            return null;
        }
        return URLMapper.findFileObject(url);
    }
    
    private static URI goUp(URI u) {
        assert u.isAbsolute() : u;
        assert u.getFragment() == null : u;
        assert u.getQuery() == null : u;
        // XXX isn't there any easier way to do this?
        // Using getPath in the new path does not work; nbfs: URLs break. (#39613)
        // On the other hand, nbfs: URLs are not really used any more, so do we care?
        String path = u.getPath();
        if (path == null || path.equals("/")) { // NOI18N
            return null;
        }
        String us = u.toString();
        if (us.endsWith("/")) { // NOI18N
            us = us.substring(0, us.length() - 1);
            assert path.endsWith("/"); // NOI18N
            path = path.substring(0, path.length() - 1);
        }
        int idx = us.lastIndexOf('/');
        assert idx != -1 : path;
        if (path.lastIndexOf('/') == 0) {
            us = us.substring(0, idx + 1);
        } else {
            us = us.substring(0, idx);
        }
        URI nue;
        try {
            nue = new URI(us);
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
        if (WINDOWS) {
            String pth = nue.getPath();
            // check that path is not "/C:" or "/"
            if ((pth.length() == 3 && pth.endsWith(":")) ||
                (pth.length() == 1 && pth.endsWith("/"))) {
                return null;
            }
        }
        assert nue.isAbsolute() : nue;
        assert u.toString().startsWith(nue.toString()) : "not a parent: " + nue + " of " + u;
        return nue;
    }
    private static final boolean WINDOWS = Utilities.isWindows();
    
}
