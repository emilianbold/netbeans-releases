/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.masterfs;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.URLMapper;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Implements URLMapper for MasterFileSystem.
 * @author  rm111737
 */
public final class MasterURLMapper extends URLMapper {
    /** Creates a new instance of MasterURLMapper */
    public MasterURLMapper() {
    }

    public FileObject[] getFileObjects(final URL url) {
        final FileSystem hfs = MasterFileSystem.getDefault();
        if (!url.getProtocol().equals("file")) return null;  //NOI18N
        FileObject retVal = hfs.findResource(url.getFile());
        if (!(retVal instanceof MasterFileObject)) return null;

        return new FileObject[]{retVal};
    }

    public URL getURL(final FileObject fo, final int type) {
        if (!(fo instanceof MasterFileObject)) return null;
        MasterFileObject hfo = (MasterFileObject) fo;
        File f = (hfo != null) ? hfo.getResource().getFile() : null;

        try {
            return (f != null) ? f.toURI().toURL() : null;
        } catch (MalformedURLException mfx) {
            return null;
        }
    }
}
