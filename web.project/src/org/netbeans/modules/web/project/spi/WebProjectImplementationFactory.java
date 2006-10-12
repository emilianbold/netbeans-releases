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

package org.netbeans.modules.web.project.spi;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.support.ant.AntProjectHelper;

/** The purpose of this interface is to allow a module to provide an alternative
 * implementation of web project support on top of the standard NetBeans web
 * project metadata format. Web project implementation of AntBasedProjectType
 * will look for instances of this interface in lookup and delegate project 
 * creation to them. If no instance accepts a project the default web project
 * implementation will be used.
 *
 * @author Pavel Buzek
 */
public interface WebProjectImplementationFactory {
    /** Recognize if the project should be owned by your module 
     * or if the default implementation should be used.
     *
     * @return true if you want your {@link createProject} to be used for 
     * this project
     */
    boolean acceptProject(AntProjectHelper helper) throws IOException;
    
    /** Create your implementation of Project to completely bypass 
     * the web/project functionality.
     */
    Project createProject(AntProjectHelper helper) throws IOException;
}
