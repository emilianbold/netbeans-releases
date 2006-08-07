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

package org.netbeans.modules.masterfs;

import java.net.URISyntaxException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.URLMapper;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URI;
import org.openide.util.Exceptions;

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
            filePath = FileUtil.normalizeFile(new File(new URI(url.toExternalForm()))).getAbsolutePath();
        } catch (URISyntaxException e) {
            StringBuilder sb = new StringBuilder();            
            sb.append(e.getLocalizedMessage()).append(" [").append(url.toExternalForm()).append(']');//NOI18N
            IllegalArgumentException iax = new IllegalArgumentException(sb.toString());
            if (Utilities.isWindows() && url.getAuthority() != null) {
                Exceptions.attachLocalizedMessage(iax,
                                                  "; might be because your user directory is on a Windows UNC path (issue #46813)? If so, try using mapped drive letters.");//NOI18N                
            }            
            Exceptions.printStackTrace(iax);
            return null;
        }

        retVal = hfs.findResource(filePath);
        if (!(retVal instanceof MasterFileObject)) return null;
        if (!retVal.isValid()) return null;
        return new FileObject[]{retVal};
    }

    public URL getURL(final FileObject fo, final int type) {
        if (type == URLMapper.NETWORK || !(fo instanceof MasterFileObject)) return null;        
        MasterFileObject hfo = (MasterFileObject) fo;
        File f = (hfo != null) ? hfo.getResource().getFile() : null;

        try {
            return (f != null) ? fileToURL(f, fo) : null;
        } catch (MalformedURLException mfx) {
            return null;
        }
    }
    
    private static boolean isWindowsDriveRoot(File file) {
        return (Utilities.isWindows () || (Utilities.getOperatingSystem () == Utilities.OS_OS2)) && file.getParent() == null;
    }
    
    static URL fileToURL(File file, FileObject fo) throws MalformedURLException {        
        URL retVal = null;
        if (isWindowsDriveRoot(file)) {
            retVal = new URL ("file:/"+file.getAbsolutePath ());//NOI18N            
        } else {
            if (fo.isFolder() && (!fo.isValid() || fo.isVirtual())) {                
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
