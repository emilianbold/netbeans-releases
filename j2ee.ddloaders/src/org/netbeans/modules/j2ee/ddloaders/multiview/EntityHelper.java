/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.action.AddCmpFieldAction;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.action.AddFinderMethodAction;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.action.AddSelectMethodAction;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.entity.EntityNode;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.src.ClassElement;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author pfiala
 */
public class EntityHelper {
    protected ClassElement beanClass;
    protected Entity entity;
    protected FileObject ejbJarFile;
    protected ClassElement localInterface;
    protected ClassElement remoteInterface;

    public EntityHelper(FileObject ejbJarFile, Entity entity) {
        this.entity = entity;
        this.ejbJarFile = ejbJarFile;
        beanClass = org.netbeans.modules.j2ee.ddloaders.multiview.Utils.getBeanClass(this.ejbJarFile, this.entity);
        localInterface = Utils.getBusinessInterface(entity.getLocal(), ejbJarFile, beanClass);
        remoteInterface = Utils.getBusinessInterface(entity.getRemote(), ejbJarFile, beanClass);
    }

    public void addCmpField() {
        EntityNode entityNode = org.netbeans.modules.j2ee.ddloaders.multiview.Utils.createEntityNode(ejbJarFile,
                entity);
        new AddCmpFieldAction() {
            protected void performAction(Node[] activatedNodes) {
                super.performAction(activatedNodes);
            }
        }.performAction(new Node[]{entityNode});
    }

    public void addFinderMethod() {
        EntityNode entityNode = org.netbeans.modules.j2ee.ddloaders.multiview.Utils.createEntityNode(ejbJarFile,
                entity);
        new AddFinderMethodAction() {
            protected void performAction(Node[] activatedNodes) {
                super.performAction(activatedNodes);
            }
        }.performAction(new Node[]{entityNode});
    }

    public void addSelectMethod() {
        EntityNode entityNode = org.netbeans.modules.j2ee.ddloaders.multiview.Utils.createEntityNode(ejbJarFile,
                entity);
        new AddSelectMethodAction() {
            protected void performAction(Node[] activatedNodes) {
                super.performAction(activatedNodes);
            }
        }.performAction(new Node[]{entityNode});
    }

}
