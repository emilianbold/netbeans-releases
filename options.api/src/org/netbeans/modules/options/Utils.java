/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.options;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.swing.KeyStroke;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;

import org.openide.util.Utilities;


/**
 *
 * @author Administrator
 */
public class Utils {

    
    private static Map filesCache = new HashMap ();
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
