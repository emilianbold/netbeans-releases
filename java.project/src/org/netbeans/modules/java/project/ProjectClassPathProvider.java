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

package org.netbeans.modules.java.project;

import java.util.Iterator;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

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
            // check all instances of classpath provider; e.g. freeform project can have more than one
            Iterator it = p.getLookup().lookup(new Lookup.Template(ClassPathProvider.class)).allInstances().iterator();
            while (it.hasNext()) {
                ClassPathProvider cpp = (ClassPathProvider)it.next();
                ClassPath cp = cpp.findClassPath(file, type);
                if (cp != null) {
                    return cp;
                }
            }
        }
        return null;
    }
    
}
