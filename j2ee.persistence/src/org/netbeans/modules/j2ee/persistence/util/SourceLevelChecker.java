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

package org.netbeans.modules.j2ee.persistence.util;

import org.netbeans.api.project.Project;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;

/**
 * A helper class for checking the source level of projects.
 * 
 * @author Erno Mononen
 */
public class SourceLevelChecker {
    
    private SourceLevelChecker() {
    }

    /**
     * Checks whether the source level of the given <code>project</code>
     * is <code>1.4</code> or lower.
     * 
     * @return true if the source level of the given project was 1.4 or lower, false 
     * otherwise.
     */
    public static boolean isSourceLevel14orLower(Project project) {
        SourceLevelQueryImplementation sl = project.getLookup().lookup(SourceLevelQueryImplementation.class);
        String srcLevel = sl.getSourceLevel(project.getProjectDirectory());
        return srcLevel != null ? Double.parseDouble(srcLevel) <= 1.4 : false;
    }
    
}
