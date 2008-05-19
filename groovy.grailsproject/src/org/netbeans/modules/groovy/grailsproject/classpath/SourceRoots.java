/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.grailsproject.classpath;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Adamek
 */
public class SourceRoots {

    private final FileObject projectRoot;
    
    public SourceRoots(FileObject projectRoot) {
        this.projectRoot = projectRoot;
    }
    
    public FileObject[] getRoots() {
        String[] paths = new String[] {
            "src/java", // NOI18N
            "src/groovy", // NOI18N
            "grails-app/conf", // NOI18N
            "grails-app/controllers", // NOI18N
            "grails-app/domain", // NOI18N
            "grails-app/services", // NOI18N
            "grails-app/taglib", // NOI18N
            "test/integration", // NOI18N
            "test/unit" // NOI18N
        };
        
        Set<FileObject> roots = new HashSet<FileObject>();
        for (String path : paths) {
            FileObject fo = projectRoot.getFileObject(path); // NOI18N
            if (fo != null) {
                roots.add(fo);
            }
        }
        return roots.toArray(new FileObject[roots.size()]);
    }
    
    public List<URL> getRootURLs() {
        List<URL> urls = new ArrayList<URL>();
        try {
            for (FileObject fileObject : getRoots()) {
                urls.add(FileUtil.toFile(fileObject).toURL());
            }
        } catch (MalformedURLException murle) {
            Exceptions.printStackTrace(murle);
        }
        return urls;
    }

}
