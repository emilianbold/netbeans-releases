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

import java.util.EventObject;


/** Event describing a change in annotation of files.
*
* @author Jaroslav Tulach
*/
public final class FileStatusEvent extends EventObject {
    static final long serialVersionUID = -6428208118782405291L;

    /** changed files */
    private java.util.Set files;

    /** icon changed? */
    private boolean icon;

    /** name changed? */
    private boolean name;

    /** Creates new FileStatusEvent
    * @param fs filesystem that causes the event
    * @param files set of FileObjects that has been changed
    * @param icon has icon changed?
    * @param name has name changed?
    */
    public FileStatusEvent(FileSystem fs, java.util.Set files, boolean icon, boolean name) {
        super(fs);
        this.files = files;
        this.icon = icon;
        this.name = name;
    }

    /** Creates new FileStatusEvent
    * @param fs filesystem that causes the event
    * @param file file object that has been changed
    * @param icon has icon changed?
    * @param name has name changed?
    */
    public FileStatusEvent(FileSystem fs, FileObject file, boolean icon, boolean name) {
        this(fs, java.util.Collections.singleton(file), icon, name);
    }

    /** Creates new FileStatusEvent. This does not specify the
    * file that changed annotation, assuming that everyone should update
    * its annotation. Please notice that this can be time consuming
    * and should be fired only when really necessary.
    *
    * @param fs filesystem that causes the event
    * @param icon has icon changed?
    * @param name has name changed?
    */
    public FileStatusEvent(FileSystem fs, boolean icon, boolean name) {
        this(fs, (java.util.Set) null, icon, name);
    }

    /** Getter for filesystem that caused the change.
    * @return filesystem
    */
    public FileSystem getFileSystem() {
        return (FileSystem) getSource();
    }

    /** Is the change change of name?
    */
    public boolean isNameChange() {
        return name;
    }

    /** Do the files changed their icons?
    */
    public boolean isIconChange() {
        return icon;
    }

    /** Check whether the given file has been changed.
    * @param file file to check
    * @return true if the file has been affected by the change
    */
    public boolean hasChanged(FileObject file) {
        if (files == null) {
            // all files on source filesystem are said to change
            try {
                return file.getFileSystem() == getSource();
            } catch (FileStateInvalidException ex) {
                // invalid files should not be changed
                return false;
            }
        } else {
            // specified set of files, so check it
            return files.contains(file);
        }
    }
}
