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

package org.netbeans.modules.j2ee.spi.ejbjar;

import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.openide.nodes.Node;

/** 
 * This class should be implemented by a module that provides nodes
 * for EJBs based on the elements from ejb-jar.xml (J2EE DD API).
 *
 * @author Pavel Buzek
 */
public interface EjbNodesFactory {
   
    /** This can be used to identify the EJB container node.*/
    public static final String CONTAINER_NODE_NAME = "EJBS"; // NOI18N
    
    Node createSessionNode(String ejbClass, EjbJar ejbModule, Project project);
    Node createEntityNode(String ejbClass, EjbJar ejbModule, Project project);
    Node createMessageNode(String ejbClass, EjbJar ejbModule, Project project);
    
}
