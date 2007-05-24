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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.websvc.rest;

import java.io.IOException;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.openide.filesystems.FileObject;

/**
 * REST support utilitities across all project types.
 * 
 * @author Nam Nguyen
 */
public class RestUtils {
    
    /**
     *  Makes sure project is ready for REST development.
     *  @param source source file or directory as part of REST application project.
     */
    public static void ensureRestDevelopmentReady(FileObject source) throws IOException {
        Project p = FileOwnerQuery.getOwner(source);
        if (p != null) {
            ensureRestDevelopmentReady(p);
        }
    }
    
    /**
     *  Makes sure project is ready for REST development.
     *  @param project project to make REST development ready
     */
    public static void ensureRestDevelopmentReady(Project project) throws IOException {
        RestSupport restSupport = project.getLookup().lookup(RestSupport.class);
        restSupport.ensureRestDevelopmentReady();
    }
    
    /**
     *  Returns true if the project supports REST framework.
     *  @param project project to make REST development ready
     */
    public static boolean supportsRestDevelopment(Project project) {
        RestSupport restSupport = project.getLookup().lookup(RestSupport.class);
        return restSupport != null;
    }

}
