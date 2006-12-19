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

package org.netbeans.modules.options;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;

public class Utils {


    private static Map<String, FileObject> filesCache = new HashMap<String, FileObject> ();
    public static FileObject getFileObject (String name, String ext, boolean create)
    throws IOException {
        FileObject r = (FileObject) filesCache.get (name + '.' + ext);
        if (r != null) return r;
        FileSystem fs = Repository.getDefault ().
            getDefaultFileSystem ();
        FileObject rootFolder = fs.getRoot ();
        FileObject optionsFolder = rootFolder.getFileObject ("Options");
        if (optionsFolder == null) {
            if (create) 
                optionsFolder = rootFolder.createFolder ("Options");
            else 
                return null;
        }
        FileObject fileObject = optionsFolder.getFileObject (name, ext);
        if (fileObject == null) {
            if (create)
                fileObject = optionsFolder.createData (name, ext);
            else
                return null;
        }
        filesCache.put (name + '.' + ext, fileObject);
        return fileObject;
    }
    
    public static Enumeration getInputStreams (String name, String ext)
    throws IOException {
        ClassLoader classLoader = (ClassLoader) Lookup.getDefault ().
                lookup (ClassLoader.class);
        return classLoader.getResources ("META-INF/options/" + name + "." + ext);
    }
}
