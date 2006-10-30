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
 * The Original Software is Forte for Java, Community Edition. The Initial
 * Developer of the Original Software is Sun Microsystems, Inc. Portions
 * Copyright 1997-2006 Sun Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.projects;

import org.openide.filesystems.*;
import java.beans.PropertyVetoException;
import java.io.IOException;
import org.openide.util.Exceptions;

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
        setSystemName();
        setPropagateMasks (true);
    }

    @SuppressWarnings("deprecation")
    private void setSystemName() {
        try {
            setSystemName(del.getSystemName() + " : " + root.getPath()); //NOI18N
        } catch (PropertyVetoException e) {
            // ther shouldn't be any listener vetoing setSystemName
            Exceptions.printStackTrace(e);
        }
    }

    public final FileObject getRootFileObject () {
        return root;
    }

    protected FileObject findResourceOn (FileSystem fs, String res) {
        return fs.findResource (root.getPath() + "/" + res); //NOI18N
    }

    protected java.util.Set createLocksOn (String name) throws IOException {
        String nn = root.getPath() + "/" + name;
        org.netbeans.core.startup.layers.LocalFileSystemEx.potentialLock (name, nn);
        return super.createLocksOn (name);
    }
}
