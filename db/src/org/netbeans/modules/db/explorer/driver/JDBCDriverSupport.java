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

package org.netbeans.modules.db.explorer.driver;

import java.io.File;
import java.net.URL;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 * A helper class for working with JDBC Drivers.
 *
 * @author Andrei Badea
 */
public final class JDBCDriverSupport {

    private JDBCDriverSupport() {
    }

    /**
     * Return if the driver file(s) exists and can be loaded.
     * @return true if defined driver file(s) exists; otherwise false
     */
    public static boolean isAvailable(JDBCDriver driver) {
        URL[] urls = driver.getURLs();
        for (int i = 0; i < urls.length; i++) {
            if (URLMapper.findFileObject(urls[i]) == null) {
                return false;
            }
        }
        return true;
    }
}
