/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.projects;

import org.openide.TopManager;
import org.openide.filesystems.*;
import java.beans.PropertyVetoException;

/**
 *
 * @author  Petr Jiricka, Vita Stejskal
 * @version 1.0
 */
public final class FilterFileSystem extends MultiFileSystem {
	
    private final FileObject root;
    private final FileSystem del;

    public FilterFileSystem (FileObject root) throws FileStateInvalidException {
        super (new FileSystem [] { root.getFileSystem () });
        this.root = root;
        this.del = root.getFileSystem ();
        
        try {
            setSystemName (del.getSystemName () + " : " + root.getPackageNameExt ('/', '.')); //NOI18N
        } catch (PropertyVetoException e) {
            // ther shouldn't be any listener vetoing setSystemName
            TopManager.getDefault ().notifyException (e);
        }

        setPropagateMasks (true);
    }

    public final FileObject getRootFileObject () {
        return root;
    }

    protected FileObject findResourceOn (FileSystem fs, String res) {
        return fs.findResource (root.getPackageNameExt ('/', '.') + "/" + res); //NOI18N
    }
}
