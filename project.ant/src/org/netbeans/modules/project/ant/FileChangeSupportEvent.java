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

package org.netbeans.modules.project.ant;
import java.io.File;

import java.util.EventObject;

import org.openide.filesystems.FileObject;

import org.openide.filesystems.FileUtil;

/**
 * Event indicating that a file named by a given path was created, deleted, or changed.
 * @author Jesse Glick
 */
public final class FileChangeSupportEvent extends EventObject {
    
    public static final int EVENT_CREATED = 0;
    public static final int EVENT_DELETED = 1;
    public static final int EVENT_MODIFIED = 2;
    
    private final int type;
    private final File path;
    
    FileChangeSupportEvent(FileChangeSupport support, int type, File path) {
        super(support);
        this.type = type;
        this.path = path;
    }
    
    public int getType() {
        return type;
    }
    
    public File getPath() {
        return path;
    }
    
    public FileObject getFileObject() {
        return FileUtil.toFileObject(path);
    }
    
    public String toString() {
        return "FCSE[" + "CDM".charAt(type) + ":" + path + "]"; // NOI18N
    }
    
}
