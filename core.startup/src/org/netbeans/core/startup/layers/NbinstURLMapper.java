/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.startup.layers;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.*;

import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.modules.InstalledFileLocator;


/**
 * URLMapper for the nbinst URL protocol.
 * The mapper handles only the translation from URL into FileObjects.
 * The opposite conversion is not needed, it is handled by the default URLMapper.
 * The format of the nbinst URL is nbinst://host/path.
 * The host part is optional, if presents it specifies the name of the supplying module.
 * The path is mandatory and specifies the relative path from the ${netbeans.home}, ${netbeans.user}
 * or ${netbeans.dirs}.
 * @author  Tomas Zezula
 */
public class NbinstURLMapper extends URLMapper {
    
    public static final String PROTOCOL = "nbinst";     //NOI18N
    
    /** Creates a new instance of NbInstURLMapper */
    public NbinstURLMapper() {
    }

    /**
     * Returns FileObjects for given URL
     * @param url the URL for which the FileObjects should be find.
     * @return FileObject[], returns null in case of unknown protocol.
     */
    public FileObject[] getFileObjects(URL url) {         
        return (PROTOCOL.equals(url.getProtocol())) ? decodeURL (url) : null;
    }

    /**
     * Returns null, the translation into URL is doen by default URLMapper
     * @param fo
     * @param type
     * @return
     */
    public URL getURL(FileObject fo, int type) {
        return null;
    }

    /**
     * Resolves the nbinst URL into the array of the FileObjects.
     * @param url to be resolved
     * @return FileObject[], returns null if unknown url protocol.
     */
    static FileObject[] decodeURL (URL url) {
        assert url != null;
        try {
            URI uri = new URI (url.toExternalForm());
            String protocol = uri.getScheme();
            if (PROTOCOL.equals(protocol)) {
                String module = uri.getHost();
                String path = uri.getPath();
                if (path.length()>0) {
                    try {
                        File file = InstalledFileLocator.getDefault().locate(path.substring(1),module,false);
                        if (file != null) {
                            return new FileObject[] {URLMapper.findFileObject(file.toURI().toURL())};
                        }
                    }
                    catch (MalformedURLException mue) {
                        ErrorManager.getDefault().notify(mue);
                    }
                }
            }
        } catch (URISyntaxException use) {
            ErrorManager.getDefault().notify(use);
        }
        return null;
    }

}
