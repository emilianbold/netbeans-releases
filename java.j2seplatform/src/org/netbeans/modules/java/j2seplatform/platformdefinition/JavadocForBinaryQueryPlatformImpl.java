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

package org.netbeans.modules.java.j2seplatform.platformdefinition;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/**
 * Implementation of Javadoc query for the platform.
 */
public class JavadocForBinaryQueryPlatformImpl implements JavadocForBinaryQueryImplementation {

    /** Default constructor for lookup. */
    public JavadocForBinaryQueryPlatformImpl() {
    }
    
    public JavadocForBinaryQuery.Result findJavadoc(final URL b) {
        class R implements JavadocForBinaryQuery.Result {

            private JavaPlatform platform;

            public R (JavaPlatform plat) {
                this.platform = plat;
            }

            public URL[] getRoots() {
                ArrayList l = new ArrayList();
                for (Iterator i2 = this.platform.getJavadocFolders().iterator(); i2.hasNext();) {
                    URL u = (URL)i2.next();
                    l.add(u);
                }
                return (URL[])l.toArray(new URL[l.size()]);
            }

            public void addChangeListener(ChangeListener l) {
                // XXX not implemented
            }
            public void removeChangeListener(ChangeListener l) {
                // XXX not implemented
            }
        }
        JavaPlatformManager jpm = JavaPlatformManager.getDefault();
        JavaPlatform platforms[] = jpm.getInstalledPlatforms();
        for (int i=0; i<platforms.length; i++) {
            JavaPlatform jp = platforms[i];
            if (jp.getJavadocFolders().size() == 0) {
                continue;
            }
            Iterator it = jp.getBootstrapLibraries().entries().iterator();
            while (it.hasNext()) {
                ClassPath.Entry entry = (ClassPath.Entry)it.next();
                if (b.equals(entry.getURL())) {
                    return new R (jp);
                }
            }
        }
        return null;
    }
    
}
