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


/** Adapter for changes in <code>FileObject</code>s. Can be attached to any {@link FileObject}.
*
* @see FileChangeListener
*
* @author Jaroslav Tulach, Petr Hamernik
* @version 0.15 December 14, 1997
*/
public class FileChangeAdapter extends Object implements FileChangeListener {
    /** Fired when a new folder is created. This action can only be
    * listened to in folders containing the created folder up to the root of
    * filesystem.
    *
    * @param fe the event describing context where action has taken place
    */
    public void fileFolderCreated(FileEvent fe) {
    }

    /** Fired when a new file is created. This action can only be
    * listened in folders containing the created file up to the root of
    * filesystem.
    *
    * @param fe the event describing context where action has taken place
    */
    public void fileDataCreated(FileEvent fe) {
    }

    /** Fired when a file is changed.
    * @param fe the event describing context where action has taken place
    */
    public void fileChanged(FileEvent fe) {
    }

    /** Fired when a file is deleted.
    * @param fe the event describing context where action has taken place
    */
    public void fileDeleted(FileEvent fe) {
    }

    /** Fired when a file is renamed.
    * @param fe the event describing context where action has taken place
    *           and the original name and extension.
    */
    public void fileRenamed(FileRenameEvent fe) {
    }

    /** Fired when a file attribute is changed.
    * @param fe the event describing context where action has taken place,
    *           the name of attribute and the old and new values.
    */
    public void fileAttributeChanged(FileAttributeEvent fe) {
    }
}
