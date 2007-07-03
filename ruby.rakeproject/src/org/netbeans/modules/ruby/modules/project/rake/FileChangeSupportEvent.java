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

package org.netbeans.modules.ruby.modules.project.rake;
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
