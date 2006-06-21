/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.apache.tools.ant.module.spi;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/** Factory method for urls.
 * 
 * @author Jaroslav Tulach
 */
final class AutomaticExtraClasspath implements AutomaticExtraClasspathProvider {
    private File file;
    
    
    private AutomaticExtraClasspath(File file) {
        this.file = file;
    }
    
    public static AutomaticExtraClasspathProvider url(Map<?,?> map) throws Exception {
        Object obj = map.get("url"); // NOI18N
        if (obj instanceof URL) {
            FileObject fo = URLMapper.findFileObject((URL)obj);
            File f = fo != null ? FileUtil.toFile(fo) : null;
            if (f != null) {
                AutomaticExtraClasspath aec = new AutomaticExtraClasspath(f);
                return aec;
            }
            throw new IllegalArgumentException("file does not exists: " + obj); // NOI18N
        } else {
            throw new IllegalArgumentException("url arg is not URL: " + obj); // NOI18N
        }
    }

    public File[] getClasspathItems() {
        return new File[] { file };
    }
}
