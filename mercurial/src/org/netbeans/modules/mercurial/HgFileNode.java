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
 * "Portions Copyrighted [year] [name of copyright owner]" // NOI18N
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.mercurial;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

/**
 * Represents real or virtual (non-local) file.
 *
 * @author Padraig O'Briain
 */
public class HgFileNode {

    private final File file;

    public HgFileNode(File file) {
        this.file = file;
    }

    public String getName() {
        return file.getName();
    }


    public FileInformation getInformation() {
        return Mercurial.getInstance().getFileStatusCache().getStatus(file); 
    }

    public File getFile() {
        return file;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof HgFileNode && file.equals(((HgFileNode) o).file);
    }

    public int hashCode() {
        return file.hashCode();
    }

    public FileObject getFileObject() {
        return FileUtil.toFileObject(file);
    }

    public Object[] getLookupObjects() {
        List<Object> list = new ArrayList<Object>(2);
        list.add(file);
        FileObject fo = getFileObject();
        if (fo != null) {
            list.add(fo);
        }
        return list.toArray(new Object[list.size()]);
    }
}
