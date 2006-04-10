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

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.modules.InstalledFileLocator;

/**
 * Able to find javadoc in the appropriate NbPlatform for the given URL.
 *
 * @author Jesse Glick, Martin Krauskopf
 */
public final class GlobalJavadocForBinaryImpl implements JavadocForBinaryQueryImplementation {
    
    public JavadocForBinaryQuery.Result findJavadoc(URL binaryRoot) {
        try {
            if (!binaryRoot.getProtocol().equals("jar")) { // NOI18N
                // XXX probably shouldn't just return null in this case
                Util.err.log(binaryRoot + " is not an archive file."); // NOI18N
                return null;
            }
            URL jar = FileUtil.getArchiveFile(binaryRoot);
            if (!jar.getProtocol().equals("file")) { // NOI18N
                Util.err.log(binaryRoot + " is not an archive file."); // NOI18N
                return null;
            }
            if (jar.toExternalForm().endsWith("/xtest/lib/junit.jar")) { // NOI18N
                // #68685 hack - associate reasonable Javadoc with XTest's version of junit
                File f = InstalledFileLocator.getDefault().locate("modules/ext/junit-3.8.2.jar", "org.netbeans.modules.junit", false); // NOI18N
                if (f == null) {
                    // For compat with NB 5.0.
                    f = InstalledFileLocator.getDefault().locate("modules/ext/junit-3.8.1.jar", "org.netbeans.modules.junit", false); // NOI18N
                }
                if (f != null) {
                    return JavadocForBinaryQuery.findJavadoc(FileUtil.getArchiveRoot(f.toURI().toURL()));
                }
            }
            File binaryRootF = new File(URI.create(jar.toExternalForm()));
            NbPlatform supposedPlaf = null;
            for (Iterator it = NbPlatform.getPlatforms().iterator(); it.hasNext(); ) {
                NbPlatform plaf = (NbPlatform) it.next();
                if (binaryRootF.getAbsolutePath().startsWith(plaf.getDestDir().getAbsolutePath())) {
                    supposedPlaf = plaf;
                    break;
                }
            }
            if (supposedPlaf == null) {
                Util.err.log(binaryRootF + " does not correspond to a known platform"); // NOI18N
                return null;
            }
            // XXX this will only work for modules following regular naming conventions:
            String n = binaryRootF.getName();
            if (!n.endsWith(".jar")) { // NOI18N
                Util.err.log(binaryRootF + " is not a *.jar"); // NOI18N
                return null;
            }
            String cnbdashes = n.substring(0, n.length() - 4);
            final List/*<URL>*/ candidates = new ArrayList();
            URL[] roots = supposedPlaf.getJavadocRoots();
            Util.err.log("Platform in " + supposedPlaf.getDestDir() + " claimed to have Javadoc roots " + Arrays.asList(roots));
            for (int i = 0; i < roots.length; i++) {
                // 1. user may insert directly e.g ...nbbuild/build/javadoc/org-openide-actions[.zip]
                candidates.add(roots[i]);
                // 2. or whole bunch of javadocs e.g. ...nbbuild/build/javadoc/
                candidates.add(new URL(roots[i], cnbdashes + '/'));
            }
            Iterator it = candidates.iterator();
            while (it.hasNext()) {
                URL u = (URL) it.next();
                if (URLMapper.findFileObject(u) == null) {
                    Util.err.log("No such Javadoc candidate URL " + u);
                    it.remove();
                }
            }
            return new JavadocForBinaryQuery.Result() {
                public URL[] getRoots() {
                    return (URL[]) candidates.toArray(new URL[candidates.size()]);
                }
                public void addChangeListener(ChangeListener l) {}
                public void removeChangeListener(ChangeListener l) {}
            };
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }
    
}
