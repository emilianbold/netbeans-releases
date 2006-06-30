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

package org.netbeans.modules.j2ee.earproject;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.spi.ejbjar.EarImplementation;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarFactory;
import org.netbeans.modules.j2ee.spi.ejbjar.EarProvider;
import org.netbeans.modules.j2ee.api.ejbjar.Ear;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class ProjectEarProvider implements  EarProvider {
    
    public ProjectEarProvider () {
    }
    
    public Ear findEar (FileObject file) {
        Project project = FileOwnerQuery.getOwner (file);
        if (project != null && project instanceof EarProject) {
            EarProject ep = (EarProject) project;
            FileObject src = ep.getSourceDirectory ();
            if (src != null && src.equals (file) || FileUtil.isParentOf(src, file)) {
                return ep.getEar();
            }
            FileObject prjdir = ep.getProjectDirectory();
            if (prjdir != null && (prjdir.equals (file) || FileUtil.isParentOf(prjdir, file))) {
                return ep.getEar();
            }
        }
        return null;
    }
    
}
