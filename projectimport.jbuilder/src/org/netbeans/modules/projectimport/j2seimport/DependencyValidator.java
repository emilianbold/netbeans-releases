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

package org.netbeans.modules.projectimport.j2seimport;

import java.util.Iterator;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Logger;


/**
 *
 * @author Radek Matous
 */
public final class DependencyValidator {
    private Stack solved = new Stack();
    private StringBuffer errorMessage = null;
    private static final Logger logger =
            LoggerFactory.getDefault().createLogger(DependencyValidator.class);


    private DependencyValidator(ProjectModel projectDefinition) {
        checkDependencies(projectDefinition);
    }
    
    public static DependencyValidator checkProject(ProjectModel projectDefinition) {
        return new DependencyValidator(projectDefinition);
    }
    
    public boolean isValid() {
        return errorMessage == null;
    }
    
    public String getErrorMessage() {
        return errorMessage != null ? errorMessage.toString() : ""; //NOI18N
    }
    
    private void checkDependencies(ProjectModel projectDefinition) {
        solved.push(projectDefinition);
        recursiveDependencyCheck(projectDefinition);
        ProjectModel retrievedProject = (ProjectModel)solved.pop();
        assert retrievedProject.equals(projectDefinition);
        assert solved.isEmpty();        
    }
    
    
    private void recursiveDependencyCheck(ProjectModel projectDefinition) {
        Set/*<Project>*/ subProjects = projectDefinition.getDependencies();
        if (subProjects != null && !subProjects.isEmpty()) {
            for (Iterator it = subProjects.iterator(); it.hasNext(); ) {
                ProjectModel subDefinition = (ProjectModel) it.next();
                if (solved.contains(subDefinition)) {
                    recursionDetected(subDefinition);
                    return;
                }
                solved.push(subDefinition);
                recursiveDependencyCheck(subDefinition);
                ProjectModel retrievedProject = (ProjectModel)solved.pop();            
                assert retrievedProject.equals(subDefinition);
            }
        }
    }
    
    private void recursionDetected(ProjectModel start) {
        int where = solved.search(start);
        assert where != -1 : "Cannot find start of the cycle."; // NOI18N
        ProjectModel rootOfCycle = (ProjectModel) solved.get(solved.size() - where);
        assert start == rootOfCycle;
        StringBuffer cycle = new StringBuffer();
        for (Iterator it = solved.iterator(); it.hasNext(); ) {
            cycle.append(((ProjectModel)it.next()).getName() + " --> "); // NOI18N            
        }
        cycle.append(rootOfCycle.getName() + " --> ..."); // NOI18N
        errorMessage = cycle;
        logger.warning("Cycle dependencies was detected. Detected cycle: " + cycle); // NOI18N
    }
    
}
