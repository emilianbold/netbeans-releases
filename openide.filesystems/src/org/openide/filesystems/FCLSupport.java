/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.filesystems;


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
    private ListenerList listeners;

    /* Add new listener to this object.
    * @param l the listener
    */
    synchronized final void addFileChangeListener(FileChangeListener fcl) {
        if (listeners == null) {
            listeners = new ListenerList(FileChangeListener.class);
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
        Object[] fcls;

        synchronized (this) {
            if (listeners == null) {
                return;
            }

            fcls = listeners.getAllListeners();
        }

        for (int i = 0; i < fcls.length; i++) {
            if (fcls[i] instanceof FileChangeListener) {
                dispatchEvent((FileChangeListener) fcls[i], fe, operation);
            }
        }
    }

    final static void dispatchEvent(FileChangeListener fcl, FileEvent fe, int operation) {
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
    }

    /** @return true if there is a listener
    */
    synchronized final boolean hasListeners() {
        return (listeners != null) && (listeners.getAllListeners().length != 0);
    }
}
