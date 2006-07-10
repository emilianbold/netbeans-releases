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
