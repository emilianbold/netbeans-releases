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

import java.util.List;
import org.openide.util.Exceptions;


/**
 * Support class for impl. of FileChangeListener
 * @author  rm111737
 */
class FCLSupport {
    enum Op {DATA_CREATED, FOLDER_CREATED, FILE_CHANGED, FILE_DELETED, FILE_RENAMED, ATTR_CHANGED}

    /** listeners */
    ListenerList<FileChangeListener> listeners;

    /* Add new listener to this object.
    * @param l the listener
    */
    synchronized final void addFileChangeListener(FileChangeListener fcl) {
        if (listeners == null) {
            listeners = new ListenerList<FileChangeListener>();
        }

        listeners.add(fcl);
    }

    /* Remove listener from this object.
    * @param l the listener
    */
    synchronized final void removeFileChangeListener(FileChangeListener fcl) {
        if (listeners != null) {
            listeners.remove(fcl);
        }
    }

    final void dispatchEvent(FileEvent fe, Op operation) {
        List<FileChangeListener> fcls;

        synchronized (this) {
            if (listeners == null) {
                return;
            }

            fcls = listeners.getAllListeners();
        }

        for (FileChangeListener l : fcls) {
            dispatchEvent(l, fe, operation);
        }
    }

    final static void dispatchEvent(FileChangeListener fcl, FileEvent fe, Op operation) {
        try {
            switch (operation) {
                case DATA_CREATED:
                    fcl.fileDataCreated(fe);
                    break;
                case FOLDER_CREATED:
                    fcl.fileFolderCreated(fe);
                    break;
                case FILE_CHANGED:
                    fcl.fileChanged(fe);
                    break;
                case FILE_DELETED:
                    fcl.fileDeleted(fe);
                    break;
                case FILE_RENAMED:
                    fcl.fileRenamed((FileRenameEvent) fe);
                    break;
                case ATTR_CHANGED:
                    fcl.fileAttributeChanged((FileAttributeEvent) fe);
                    break;
                default:
                    throw new AssertionError(operation);
            }
        } catch (RuntimeException x) {
            Exceptions.printStackTrace(x);
        }
    }

    /** @return true if there is a listener
    */
    synchronized final boolean hasListeners() {
        return listeners != null && listeners.hasListeners();
    }
}
