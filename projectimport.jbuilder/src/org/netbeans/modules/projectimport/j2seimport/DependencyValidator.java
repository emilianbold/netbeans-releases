/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
