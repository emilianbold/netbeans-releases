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

package org.netbeans.modules.j2ee.ejbcore;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.entity.EntityNode;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.mdb.MessageNode;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.session.SessionNode;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbNodesFactory;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres.CallEjbDialog;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 *
 * @author Pavel Buzek
 */
public final class EjbNodesFactoryImpl implements EjbNodesFactory {
    
    public EjbNodesFactoryImpl() {
    }
    
    public Node createSessionNode (Session session, EjbJar model, ClassPath srcPath) {
        return new SessionNode (session, model, srcPath);
    }
    
    public Node createEntityNode (Entity entity, EjbJar model, ClassPath srcPath, FileObject ddFile) {
        return new EntityNode (entity, model, srcPath, ddFile);
    }
    
    public Node createMessageNode (MessageDriven mdb, EjbJar model, ClassPath srcPath) {
        return new MessageNode(mdb, model, srcPath);
    }
}
