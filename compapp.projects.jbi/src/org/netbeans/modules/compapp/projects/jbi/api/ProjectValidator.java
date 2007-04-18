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

package org.netbeans.modules.compapp.projects.jbi.api;

import java.util.List;
import org.netbeans.api.project.Project;

/**
 * An SPI that a component project type can implement to validate all the 
 * instances of that project type in a composite application project.
 *
 * For example, if BPEL Project System wants to do validation across multiple
 * BPEL projects, it can implement this interface and put it in the BPEL 
 * project's lookup. 
 * 
 * When a composite application project builds, each unique component project 
 * type has one chance to validate all the component projects of that type.
 *
 * @author jqian
 */
public interface ProjectValidator {
    
    /**
     * Validates all the component projects in a composite application project.
     *
     * @param componentProjects  a list of component projects in a composite
     *                           application project
     * @return  an error/warning message if validation fails, or null otherwise
     */
    String validateProjects(List<Project> componentProjects);
    
}
