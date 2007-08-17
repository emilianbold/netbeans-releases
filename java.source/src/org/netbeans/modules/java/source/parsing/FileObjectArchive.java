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

package org.netbeans.modules.java.source.parsing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.tools.JavaFileObject;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.openide.filesystems.FileObject;

/**
 *
 * @author tom
 */
public class FileObjectArchive implements Archive {
    
    private final FileObject root;
    
    /** Creates a new instance of FileObjectArchive */
    public FileObjectArchive (final FileObject root) {
        this.root = root;
    }
    
    public Iterable<JavaFileObject> getFiles(String folderName, ClassPath.Entry entry, Set<JavaFileObject.Kind> kinds, JavaFileFilterImplementation filter) throws IOException {
        FileObject folder = root.getFileObject(folderName);        
        if (folder == null || !(entry == null || entry.includes(folder))) {
            return Collections.<JavaFileObject>emptySet();
        }
        FileObject[] children = folder.getChildren();
        List<JavaFileObject> result = new ArrayList<JavaFileObject>(children.length);
        for (FileObject fo : children) {
            if (fo.isData() && (entry == null || entry.includes(fo))) {
                if (kinds == null || kinds.contains (FileObjects.getKind(fo.getExt()))) {
                    result.add(FileObjects.nbFileObject(fo,filter,false));
                }
            }
        }
        return result;
    }
    
    public void clear() {
    }

}
