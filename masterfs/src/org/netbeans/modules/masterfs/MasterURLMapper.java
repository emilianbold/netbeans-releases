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
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;
import org.openide.ErrorManager;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URI;

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
        //TODO: review and simplify         
        FileObject retVal = null;
        String filePath = null;
        try {
            filePath = FileUtil.normalizeFile(new File(URI.create(url.toExternalForm()))).getAbsolutePath();
        } catch (IllegalArgumentException e) {
            StringBuffer sb = new StringBuffer();
            sb.append(e.getLocalizedMessage()).append(" [").append(url.toExternalForm()).append("]");//NOI18N
            ErrorManager.getDefault().notify(new IllegalArgumentException(sb.toString()));
            return null;
        }

        retVal = hfs.findResource(filePath);
        if (!(retVal instanceof MasterFileObject)) return null;
        
        return new FileObject[]{retVal};
    }

    public URL getURL(final FileObject fo, final int type) {
        if (!(fo instanceof MasterFileObject)) return null;
        MasterFileObject hfo = (MasterFileObject) fo;
        File f = (hfo != null) ? hfo.getResource().getFile() : null;

        try {
            return (f != null) ? fileToURL(f, fo) : null;
        } catch (MalformedURLException mfx) {
            return null;
        }
    }
    
    private static boolean isWindowsDriveRoot(File file) {
        return Utilities.isWindows() && file.getParent() == null;
    }
    
    static URL fileToURL(File file, FileObject fo) throws MalformedURLException {        
        URL retVal = null;
        if (isWindowsDriveRoot(file)) {
            retVal = new URL ("file:/"+file.getAbsolutePath ());//NOI18N            
        } else {
            if (fo.isFolder() && !fo.isValid()) {                
                String urlDef = file.toURI().toURL().toExternalForm();
                String pathSeparator = "/";//NOI18N
                if (!urlDef.endsWith(pathSeparator)) {
                    retVal = new URL (urlDef + pathSeparator);     
                }                  
            }
            retVal = (retVal == null) ? file.toURI().toURL() : retVal;                        
        }        
        return retVal;
    }

}
