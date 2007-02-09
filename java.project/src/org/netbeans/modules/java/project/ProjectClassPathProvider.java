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

package org.netbeans.modules.java.project;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.FileObject;

/**
 * Supplies classpath information according to project file owner.
 * @author Jesse Glick
 */
public class ProjectClassPathProvider implements ClassPathProvider {

    /** Default constructor for lookup. */
    public ProjectClassPathProvider() {}
    
    public ClassPath findClassPath(FileObject file, String type) {
        Project p = FileOwnerQuery.getOwner(file);
        if (p != null) {
            ClassPathProvider cpp = p.getLookup().lookup(ClassPathProvider.class);
            if (cpp != null) {
                return cpp.findClassPath(file, type);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
    
}
