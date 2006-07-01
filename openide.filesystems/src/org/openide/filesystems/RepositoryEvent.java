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
package org.openide.filesystems;


/** Event describing adding a filesystem to, or removing a filesystem from, the filesystem pool.
*
* @author Jaroslav Tulach
* @version 0.10 November 4, 1997
*/
public class RepositoryEvent extends java.util.EventObject {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 5466690014963965717L;

    /** the modifying filesystem */
    private FileSystem fileSystem;

    /** added or removed */
    private boolean add;

    /** Create a new filesystem pool event.
    * @param fsp filesystem pool that is being modified
    * @param fs filesystem that is either being added or removed
    * @param add <CODE>true</CODE> if the filesystem is added,
    *    <CODE>false</CODE> if removed
    */
    public RepositoryEvent(Repository fsp, FileSystem fs, boolean add) {
        super(fsp);
        this.fileSystem = fs;
        this.add = add;
    }

    /** Getter for the filesystem pool that is modified.
    * @return the filesystem pool
    */
    public Repository getRepository() {
        return (Repository) getSource();
    }

    /** Getter for the filesystem that is added or removed.
    * @return the filesystem
    */
    public FileSystem getFileSystem() {
        return fileSystem;
    }

    /** Is the filesystem added or removed?
    * @return <CODE>true</CODE> if the filesystem is added, <code>false</code> if removed
    */
    public boolean isAdded() {
        return add;
    }
}
