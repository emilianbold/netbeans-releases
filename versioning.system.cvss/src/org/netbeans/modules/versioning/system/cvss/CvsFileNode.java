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

package org.netbeans.modules.versioning.system.cvss;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.InstanceContent;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

/**
 * Represents real or virtual (non-local) file.
 *
 * @author Maros Sandor
 */
public class CvsFileNode {

    private final File file;

    public CvsFileNode(File file) {
        this.file = file;
    }

    public String getName() {
        return file.getName();
    }


    public FileInformation getInformation() {
        return CvsVersioningSystem.getInstance().getStatusCache().getStatus(file); 
    }

    public File getFile() {
        return file;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof CvsFileNode && file.equals(((CvsFileNode) o).file);
    }

    public int hashCode() {
        return file.hashCode();
    }

    public FileObject getFileObject() {
        return FileUtil.toFileObject(file);
    }

    public Object[] getLookupObjects() {
        List list = new ArrayList(2);
        list.add(this);
        FileObject fo = getFileObject();
        if (fo != null) {
            list.add(fo);
        }
        return list.toArray(new Object[list.size()]);
    }


}
