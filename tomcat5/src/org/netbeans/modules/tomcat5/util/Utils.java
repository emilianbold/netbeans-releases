/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tomcat5.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import org.openide.filesystems.FileUtil;

/**
 * Utility class.
 *
 * @author sherold
 */
public class Utils {
    
    /** Creates a new instance of Utils */
    private Utils() {
    }
    
    /** Return URL representation of the specified file. */
    public static URL fileToUrl(File file) throws MalformedURLException {
        URL url = file.toURI().toURL();
        if (FileUtil.isArchiveFile(url)) {
            url = FileUtil.getArchiveRoot(url);
        }
        return url;
    }
    
    /** Return string representation of the specified URL. */
    public static String urlToString(URL url) {
        if ("jar".equals(url.getProtocol())) { // NOI18N
            URL fileURL = FileUtil.getArchiveFile(url);
            if (FileUtil.getArchiveRoot(fileURL).equals(url)) {
                // really the root
                url = fileURL;
            } else {
                // some subdir, just show it as is
                return url.toExternalForm();
            }
        }
        if ("file".equals(url.getProtocol())) { // NOI18N
            File f = new File(URI.create(url.toExternalForm()));
            return f.getAbsolutePath();
        }
        else {
            return url.toExternalForm();
        }
    }
    
}
