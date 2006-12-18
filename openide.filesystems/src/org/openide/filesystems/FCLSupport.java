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
    public static final int DATA_CREATED = 0;
    public static final int FOLDER_CREATED = 1;
    public static final int FILE_CHANGED = 2;
    public static final int FILE_DELETED = 3;
    public static final int FILE_RENAMED = 4;
    public static final int ATTR_CHANGED = 5;

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

    final void dispatchEvent(FileEvent fe, int operation) {
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

    final static void dispatchEvent(FileChangeListener fcl, FileEvent fe, int operation) {
        try {
            switch (operation) {
                case FCLSupport.DATA_CREATED:
                    fcl.fileDataCreated(fe);
                    break;
                case FCLSupport.FOLDER_CREATED:
                    fcl.fileFolderCreated(fe);
                    break;
                case FCLSupport.FILE_CHANGED:
                    fcl.fileChanged(fe);
                    break;
                case FCLSupport.FILE_DELETED:
                    fcl.fileDeleted(fe);
                    break;
                case FCLSupport.FILE_RENAMED:
                    fcl.fileRenamed((FileRenameEvent) fe);
                    break;
                case FCLSupport.ATTR_CHANGED:
                    fcl.fileAttributeChanged((FileAttributeEvent) fe);
                    break;
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
