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

package org.netbeans.spi.queries;

import org.openide.filesystems.FileObject;

/**
 * Determine whether files should be shared (for example in a VCS) or are intended
 * to be unshared.
 * Could be implemented e.g. by project types which know that certain files or folders in
 * a project (e.g. <samp>src/</samp>) are intended for VCS sharing while others
 * (e.g. <samp>build/</samp>) are not.
 * @see org.netbeans.api.queries.SharabilityQuery
 * @author Jesse Glick
 */
public interface SharabilityQueryImplementation {
    
    /**
     * Check whether an existing file or directory should be shared.
     * If true, it ought to be committed to a VCS if the user is using one.
     * If false, it is either a disposable build product, or a per-user
     * private file which is important but should not be shared.
     * <p>If a directory is sharable, its children may or may not be sharable.
     * But if it is not sharable, its children must not be sharable either.
     * @param file a file to check for sharability
     * @return true to mark a file sharable, false to be nonsharable, or null if
     *         there is no answer
     */
    Boolean isSharable(FileObject file);
    
    /**
     * Check whether a potential new file or directory should be shared.
     * Similar to {@link #isSharable(FileObject)}.
     * <p>This method may be needed by VCS filesystems which need to know whether
     * a new file or directory should be shared <em>before</em> creating it on disk.
     * In principle they could override e.g. {@link FileObject#createData} to first
     * create the file object, then check whether it is sharable, and finally create
     * it on disk in the appropriate mode. However this is not supported by
     * {@link org.openide.filesystems.AbstractFileSystem} and so may be too awkward.
     * <p>It is not currently possible for client code to create a <code>FileObject</code>
     * representing a file which does not exist; see issue #27817.
     * @param parent a directory where a new file or directory might go
     * @param childName a simple file name (with extension if applicable) representing a
     *                  file or directory that might be created
     * @param directory if true, the proposed child is a directory, else a file
     * @return true to mark a file sharable, false to be nonsharable, or null if
     *         there is no answer
     */
    Boolean willBeSharable(FileObject parent, String childName, boolean directory);
    
}
