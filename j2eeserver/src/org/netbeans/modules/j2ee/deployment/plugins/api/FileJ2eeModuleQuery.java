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

package org.netbeans.modules.j2ee.deployment.plugins.api;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.filesystems.FileObject;

/**
 * Finds the J2EE module for a file.
 * 
 * @author sherold
 */
public class FileJ2eeModuleQuery {
    
    /** Creates a new instance of FileJ2eeModuleQuery */
    private FileJ2eeModuleQuery() {
    }
    
    /**
     * Finds a J2EE module which owns the specified file.
     * 
     * @param fileObject the file
     * 
     * @return J2EE module which owns the specified file, or null if there is no
     *         J2EE module containing it. 
     */
    public static J2eeModule getJ2eeModule(FileObject fileObject) {
        if (fileObject == null) {
            throw new NullPointerException("FileObject parameter cannot be null."); // NOI18N
        }
        
        Project project = FileOwnerQuery.getOwner(fileObject);
        if (project == null) {
            return null;
        }
        
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (j2eeModuleProvider == null) {
            return null;
        }
        
        return j2eeModuleProvider.getJ2eeModule();
    }
}
