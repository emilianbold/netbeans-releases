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

package org.netbeans.modules.javadoc.search;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.netbeans.modules.javadoc.httpfs.HTTPURLMapper;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;

/**
 * Utility class for producing direct URLs. It
 * supports file based and HTTP based resources.
 * Others such as javadoc in archive cannot be
 * supported because there is not well know URL
 * protocol available.
 *
 * @author Petr Kuzel
 */
public final class JavadocURLMapper {

    /**
     *
     * @param fileObject
     * @return try to find direct (IDE indepenedent) URL
     */
    public static URL findURL(FileObject fileObject) {
        File file = FileUtil.toFile(fileObject);
        if (file != null) {
            try {
                return file.toURI().toURL();
            } catch (MalformedURLException ex) {
                // pass it
            }
        }

        HTTPURLMapper mapper = new HTTPURLMapper();
        URL url = mapper.getURL(fileObject, URLMapper.EXTERNAL);
        if (url != null) {
            return url;
        }

        return URLMapper.findURL(fileObject, URLMapper.EXTERNAL);
    }
}

