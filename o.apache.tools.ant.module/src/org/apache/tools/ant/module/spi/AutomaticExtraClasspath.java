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

package org.apache.tools.ant.module.spi;

import java.io.File;
import java.io.FileNotFoundException;
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
            throw new FileNotFoundException(obj.toString());
        } else {
            throw new IllegalArgumentException("url arg is not URL: " + obj); // NOI18N
        }
    }

    public File[] getClasspathItems() {
        return new File[] { file };
    }
}
