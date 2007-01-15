/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.project;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation;
import org.netbeans.spi.project.FileOwnerQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 * Find the project which owns a file.
 * <p>
 * There is a default implementation of {@link org.netbeans.spi.project.FileOwnerQueryImplementation}
 * which considers a file owned by the project corresponding to the nearest enclosing
 * project directory or marked external owner, if such a directory exists. But
 * other implementations can be registered to lookup as well.
 * @author Jesse Glick
 */
public class FileOwnerQuery {
    
    // XXX acquire the appropriate ProjectManager.mutex for the duration of calls

    private static Lookup.Result<FileOwnerQueryImplementation> implementations;

    /** Cache of all available FileOwnerQueryImplementation instances. */
    private static List<FileOwnerQueryImplementation> cache;
    
    private FileOwnerQuery() {}

    /**
     * Find the project, if any, which "owns" the given file.
     * @param file the file (generally on disk)
     * @return a project which contains it, or null if there is no known project containing it
     */
    public static Project getOwner(FileObject file) {
        if (file == null) {
            throw new NullPointerException("Passed null to FileOwnerQuery.getOwner(FileObject)"); // NOI18N
        }
        FileObject archiveRoot = FileUtil.getArchiveFile(file);
        if (archiveRoot != null) {
            file = archiveRoot;
        }
        for (FileOwnerQueryImplementation q : getInstances()) {
            Project p = q.getOwner(file);
            if (p != null) {
                return p;
            }
        }
        return null;
    }

    /**
     * Find the project, if any, which "owns" the given URI.
     * @param uri the uri to the file (generally on disk); must be absolute and not opaque
     * @return a project which contains it, or null if there is no known project containing it
     * @throws IllegalArgumentException if the URI is relative or opaque
     */
    public static Project getOwner(URI uri) {
        if (uri.isOpaque() && "jar".equalsIgnoreCase(uri.getScheme())) {    //NOI18N
            // XXX the following is bogus; should use FileUtil methods
            String schemaPart = uri.getSchemeSpecificPart();
            int index = schemaPart.lastIndexOf ('!');                       //NOI18N
            if (index>0) {
                schemaPart = schemaPart.substring(0,index);
            }
            // XXX: schemaPart can contains spaces. create File first and 
            // then convert it to URI.
            try {
                //#85137 - # character in uri path seems to cause problems, because it's not escaped. test added.
                schemaPart = schemaPart.replace("#", "%23");
                uri = new URI(schemaPart);
            } catch (URISyntaxException ex) {
                try {
                    URL u = new URL(schemaPart);
                    // XXX bad to ever use new File(URL.getPath()):
                    uri = new File(u.getPath()).toURI();
                } catch (MalformedURLException ex2) {
                    ex2.printStackTrace();
                    assert false : schemaPart;
                    return null;
                }
            }
        }
        else if (!uri.isAbsolute() || uri.isOpaque()) {
            throw new IllegalArgumentException("Bad URI: " + uri); // NOI18N
        }
        for (FileOwnerQueryImplementation q : getInstances()) {
            Project p = q.getOwner(uri);
            if (p != null) {
                return p;
            }
        }
        return null;
    }
    
    /**
     * Intended for use from unit tests. Clears internal state such as
     * external file owners.
     */
    static void reset() {
        SimpleFileOwnerQueryImplementation.reset();
    }
    
    /**
     * Simplest algorithm for marking external file owners, which just keeps
     * a transient memory cache of this information.
     * The external marking is only guaranteed to be good during this VM session
     * for as long as the external file root is retained in memory as a
     * <code>FileObject</code>. For this reason, a project which makes such a
     * marking when it is created is obliged to hold a reference to the external
     * file root for as long as the project itself is in memory, to ensure that
     * it continues to work.
     */
    public static final int EXTERNAL_ALGORITHM_TRANSIENT = 0;
    
    /**
     * Mark an external folder or file as being owned by a particular project.
     * After this call is made, for the duration appropriate to the selected
     * algorithm, that folder or file and its ancestors will be considered owned
     * by the project (if any) matching the named project directory, except in
     * the case that a lower enclosing project directory can be found.
     * <p class="nonnormative">
     * Typical usage would be to call this method for each external source root
     * of a project (if any) as soon as the project is loaded, if a transient
     * algorithm is selected, or only when the project is created, if a reliable
     * persistent algorithm is selected.
     * </p>
     * @param root a folder or a file which should be considered part of a project
     * @param owner a project which should be considered to own that folder tree
     *              (any prior marked external owner is overridden),
     *              or null to cancel external ownership for this folder root
     * @param algorithm an algorithm to use for retaining this information;
     *                  currently may only be {@link #EXTERNAL_ALGORITHM_TRANSIENT}
     * @throws IllegalArgumentException if the root or owner is null, if an unsupported
     *                                  algorithm is requested,
     *                                  if the root is already a project directory,
     *                                  or if the root is already equal to or inside the owner's
     *                                  project directory (it may however be an ancestor)
     * @see <a href="@org-netbeans-modules-project-ant@/org/netbeans/spi/project/support/ant/SourcesHelper.html"><code>SourcesHelper</code></a>
     */
    public static void markExternalOwner(FileObject root, Project owner, int algorithm) throws IllegalArgumentException {
        switch (algorithm) {
        case EXTERNAL_ALGORITHM_TRANSIENT:
            // XXX check args
            SimpleFileOwnerQueryImplementation.markExternalOwnerTransient(root, owner);
            break;
        default:
            throw new IllegalArgumentException("No such algorithm: " + algorithm); // NOI18N
        }
    }
    
    /**
     * Mark an external URI (folder or file) as being owned by a particular project.
     * After this call is made, for the duration appropriate to the selected
     * algorithm, that folder or file and its ancestors will be considered owned
     * by the project (if any) matching the named project directory, except in
     * the case that a lower enclosing project directory can be found.
     * <p class="nonnormative">
     * Typical usage would be to call this method for each external source root
     * of a project (if any) as soon as the project is loaded, if a transient
     * algorithm is selected, or only when the project is created, if a reliable
     * persistent algorithm is selected.
     * </p>
     * @param root an URI of a folder or a file which should be considered part of a project
     * @param owner a project which should be considered to own that folder tree
     *              (any prior marked external owner is overridden),
     *              or null to cancel external ownership for this folder root
     * @param algorithm an algorithm to use for retaining this information;
     *                  currently may only be {@link #EXTERNAL_ALGORITHM_TRANSIENT}
     * @throws IllegalArgumentException if the root or owner is null, if an unsupported
     *                                  algorithm is requested,
     *                                  if the root is already a project directory,
     *                                  or if the root is already equal to or inside the owner's
     *                                  project directory (it may however be an ancestor)
     * @see <a href="@org-netbeans-modules-project-ant@/org/netbeans/spi/project/support/ant/SourcesHelper.html"><code>SourcesHelper</code></a>
     */
    public static void markExternalOwner(URI root, Project owner, int algorithm) throws IllegalArgumentException {
        switch (algorithm) {
        case EXTERNAL_ALGORITHM_TRANSIENT:
            // XXX check args
            SimpleFileOwnerQueryImplementation.markExternalOwnerTransient(root, owner);
            break;
        default:
            throw new IllegalArgumentException("No such algorithm: " + algorithm); // NOI18N
        }
    }
    
    /* TBD whether this is necessary:
    public static FileObject getMarkedExternalOwner(FileObject root) {}
     */

    private static synchronized List<FileOwnerQueryImplementation> getInstances() {
        if (implementations == null) {
            implementations = Lookup.getDefault().lookupResult(FileOwnerQueryImplementation.class);
            implementations.addLookupListener(new LookupListener() {
                public void resultChanged (LookupEvent ev) {
                    synchronized (FileOwnerQuery.class) {
                        cache = null;
                    }
                }});
        }
        if (cache == null) {
            cache = new ArrayList<FileOwnerQueryImplementation>(implementations.allInstances());
        }
        return cache;
    }
    
}
