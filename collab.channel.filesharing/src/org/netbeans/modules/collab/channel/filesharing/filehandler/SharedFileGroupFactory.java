/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.channel.filesharing.filehandler;

import org.openide.filesystems.FileObject;


/**
 * SharedFileGroupFactory
 *
 * @author  ayub.khan@sun.com
 * @version                1.0
 */
public class SharedFileGroupFactory extends Object {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private static SharedFileGroupFactory instance = null;

    /**
     *
     */
    public SharedFileGroupFactory() {
        super();
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////                       

    /**
     *
     * @return SharedFileGroupFactory
     */
    public static SharedFileGroupFactory getDefault() {
        if (instance == null) {
            instance = new SharedFileGroupFactory();
        }

        return instance;
    }

    /**
     *
     * @param fileGroupName
     * @param user
     */
    public static LazySharedFileGroup createLazySharedFileGroup(
        String fileGroupName, String user, String projectName, SharedFileGroupManager manager, String[] fileNames,
        FileObject[] fileObjects
    ) {
        return new LazySharedFileGroup(fileGroupName, user, projectName, manager, fileNames, fileObjects);
    }

    /**
     *
     * @param fileGroupName
     * @param user
     */
    public static SharedFileGroup createSharedFileGroup(
        String fileGroupName, String user, String projectName, SharedFileGroupManager manager
    ) {
        return new SharedFileGroup(fileGroupName, user, projectName, manager);
    }
}
