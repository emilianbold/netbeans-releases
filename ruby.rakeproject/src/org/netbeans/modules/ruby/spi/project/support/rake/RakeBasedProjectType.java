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

package org.netbeans.modules.ruby.spi.project.support.rake;

import java.io.IOException;
import org.netbeans.api.project.Project;

/**
 * Plugin for an Ant project type.
 * Register one instance to default lookup in order to define an Ant project type.
 * @author Jesse Glick
 */
public interface RakeBasedProjectType {

    /**
     * Get a unique type identifier for this kind of project.
     * No two registered {@link RakeBasedProjectType} instances may share the same type.
     * The type is stored in <code>nbproject/project.xml</code> in the <code>type</code> element.
     * It is forbidden for the result of this method to change from call to call.
     * @return the project type
     */
    String getType();
    
    /**
     * Create the project object with a support class.
     * Normally the project should retain a reference to the helper object in
     * order to implement various required methods.
     * Do <em>not</em> do any caching here; the infrastructure will call this
     * method only when the project needs to be loaded into memory.
     * @param helper a helper object encapsulating the generic project structure
     * @return a project implementation
     * @throws IOException if there is some problem loading additional data
     */
    Project createProject(RakeProjectHelper helper) throws IOException;
    
    /**
     * Get the simple name of the XML element that should be used to store
     * the project's specific configuration data in <code>nbproject/project.xml</code>
     * (inside <code>&lt;configuration&gt;</code>) or <code>nbproject/private/private.xml</code>
     * (inside <code>&lt;project-private&gt;</code>).
     * It is forbidden for the result of this method to change from call to call.
     * @param shared if true, refers to <code>project.xml</code>, else refers to
     *               <code>private.xml</code>
     * @return a simple name; <samp>data</samp> is recommended but not required
     */
    String getPrimaryConfigurationDataElementName(boolean shared);
    
    /**
     * Get the namespace of the XML element that should be used to store
     * the project's specific configuration data in <code>nbproject/project.xml</code>
     * (inside <code>&lt;configuration&gt;</code>) or <code>nbproject/private/private.xml</code>
     * (inside <code>&lt;project-private&gt;</code>).
     * It is forbidden for the result of this method to change from call to call.
     * @param shared if true, refers to <code>project.xml</code>, else refers to
     *               <code>private.xml</code>
     * @return an XML namespace, e.g. <samp>http://www.netbeans.org/ns/j2se-project</samp>
     *         or <samp>http://www.netbeans.org/ns/j2se-project-private</samp>
     */
    String getPrimaryConfigurationDataElementNamespace(boolean shared);

}
