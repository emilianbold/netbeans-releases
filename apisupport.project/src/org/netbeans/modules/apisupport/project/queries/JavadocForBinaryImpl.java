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

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.openide.filesystems.URLMapper;

/**
 * Defines Javadoc locations for built modules.
 * @author Jesse Glick
 */
public final class JavadocForBinaryImpl implements JavadocForBinaryQueryImplementation {
    
    private static final String NB_ALL_INFIX = "nbbuild" + File.separatorChar + "build" + File.separatorChar + "javadoc" + File.separatorChar; // NOI18N
    private static final String EXT_INFIX = "build" + File.separatorChar + "javadoc" + File.separatorChar; // NOI18N
    
    /** Configurable for the unit test, since it is too cumbersome to create fake Javadoc in all the right places. */
    static boolean ignoreNonexistentRoots = true;
    
    private final NbModuleProject project;
    
    public JavadocForBinaryImpl(NbModuleProject project) {
        this.project = project;
    }

    public JavadocForBinaryQuery.Result findJavadoc(URL binaryRoot) {
        if (!binaryRoot.equals(Util.urlForJar(project.getModuleJarLocation()))) {
            return null;
        }
        String cnbdashes = project.getCodeNameBase().replace('.', '-');
        try {
            final List<URL> candidates = new ArrayList<URL>();
            NbPlatform platform = project.getPlatform(false);
            if (platform == null) {
                return null;
            }
            for (URL root : platform.getJavadocRoots()) {
                candidates.add(new URL(root, cnbdashes + "/")); // NOI18N
            }
            File dir;
            NbModuleProvider.NbModuleType type = Util.getModuleType(project);
            if (type == NbModuleProvider.NETBEANS_ORG) {
                dir = project.getNbrootFile(NB_ALL_INFIX + cnbdashes);
            } else {
                dir = new File(project.getProjectDirectoryFile(), EXT_INFIX + cnbdashes);
            }
            candidates.add(Util.urlForDir(dir));
            if (ignoreNonexistentRoots) {
                Iterator<URL> it = candidates.iterator();
                while (it.hasNext()) {
                    URL u = it.next();
                    if (URLMapper.findFileObject(u) == null) {
                        it.remove();
                    }
                }
            }
            return new JavadocForBinaryQuery.Result() {
                public URL[] getRoots() {
                    return candidates.toArray(new URL[candidates.size()]);
                }
                public void addChangeListener(ChangeListener l) {}
                public void removeChangeListener(ChangeListener l) {}
            };
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }
    
}
