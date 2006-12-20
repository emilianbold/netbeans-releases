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

package org.netbeans.modules.apisupport.jnlplauncher;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;

/**
 * Special locator for JNLP mode.
 * Currently just locates JARs with the special META-INF/clusterpath/$relpath
 * entry inserted by common.xml -> <makenjnlp>.
 * @author Jesse Glick
 */
public class InstalledFileLocatorImpl extends InstalledFileLocator {

    public InstalledFileLocatorImpl() {}

    public File locate(String relativePath, String codeNameBase, boolean localized) {
        if (localized) {
            int i = relativePath.lastIndexOf('.');
            String baseName, ext;
            if (i == -1 || i < relativePath.lastIndexOf('/')) {
                baseName = relativePath;
                ext = "";
            } else {
                baseName = relativePath.substring(0, i);
                ext = relativePath.substring(i);
            }
            Iterator<String> it = NbBundle.getLocalizingSuffixes();
            while (it.hasNext()) {
                String locName = baseName + it.next() + ext;
                File f = locate(locName, codeNameBase, false);
                if (f != null) {
                    return f;
                }
            }
        } else {
            String userdir = System.getProperty("netbeans.user");
            if (userdir != null) {
                File f = new File(userdir, relativePath.replace('/', File.separatorChar));
                if (f.exists()) {
                    return f;
                }
            }
            String resource = "META-INF/clusterpath/" + relativePath;
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            URL found = loader.getResource(resource);
            if (found != null) {
                String foundS = found.toExternalForm();
                String prefix = "jar:";
                String suffix = "!/" + resource;
                if (foundS.startsWith(prefix) && foundS.endsWith(suffix)) {
                    String infix = foundS.substring(prefix.length(), foundS.length() - suffix.length());
                    if (infix.startsWith("file:")) {
                        File jar = new File(URI.create(infix));
                        assert jar.isFile();
                        return jar;
                    }
                }
            }
        }
        return null;
    }

}
