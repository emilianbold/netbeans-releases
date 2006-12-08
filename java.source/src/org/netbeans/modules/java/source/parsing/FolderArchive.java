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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.tools.JavaFileObject;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;

/**
 *
 * @author Tomas Zezula
 */
public class FolderArchive implements Archive {

    final File root;

    /** Creates a new instance of FolderArchive */
    public FolderArchive (final File root) {
        assert root != null;
        this.root = root;
    }

    public Iterable<JavaFileObject> getFiles(String folderName, JavaFileFilterImplementation filter) throws IOException {
        assert folderName != null;
        final File folder = new File (this.root, folderName);
        if (folder.canRead()) {
            File[] content = folder.listFiles();            
            if (content != null) {
                List<JavaFileObject> result = new ArrayList<JavaFileObject>(content.length);
                for (File f : content) {
                    result.add(FileObjects.fileFileObject(f,this.root,filter));
                }
                return Collections.unmodifiableList(result);
            }
        }
        return Collections.<JavaFileObject>emptyList();
    }               
    
    public void clear () {
        
    }
    
}
