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

package org.netbeans.modules.j2ee.spi.ejbjar;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/** This class should be implemented by a module that provides nodes
 * for EJBs based on the elements from ejb-jar.xml (J2EE DD API).
 *
 * @author Pavel Buzek
 */
public interface EjbNodesFactory {
    /** This can be used to identify the EJB container node.*/
    public static final String CONTAINER_NODE_NAME = "EJBS"; // NOI18N
    
    Node createSessionNode (Session session, EjbJar model, ClassPath srcPath);
    Node createEntityNode (Entity entity, EjbJar model, ClassPath srcPath, FileObject ddFile);
    Node createMessageNode (MessageDriven mdb, EjbJar model, ClassPath srcPath);
    
}
