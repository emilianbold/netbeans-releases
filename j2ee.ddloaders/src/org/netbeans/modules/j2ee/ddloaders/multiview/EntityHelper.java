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
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.entity.methodcontroller.EntityMethodController;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.src.MethodElement;

/**
 * @author pfiala
 */
public class EntityHelper extends EntityAndSessionHelper {

    public EntityHelper(FileObject ejbJarFile, Entity entity) {
        super(ejbJarFile, entity);
    }

    public void addCmpField() {
        new AddCmpFieldAction() {
            protected void performAction(Node[] activatedNodes) {
                super.performAction(activatedNodes);
            }
        }.performAction(new Node[]{createEntityNode()});
    }

    public void addFinderMethod() {
        new AddFinderMethodAction() {
            protected void performAction(Node[] activatedNodes) {
                super.performAction(activatedNodes);
            }
        }.performAction(new Node[]{createEntityNode()});
    }

    public void addSelectMethod() {
        new AddSelectMethodAction() {
            protected void performAction(Node[] activatedNodes) {
                super.performAction(activatedNodes);
            }
        }.performAction(new Node[]{createEntityNode()});
    }

    public MethodElement getSetterMethod(String fieldName, MethodElement getterMethod) {
        return getterMethod == null ?
                null : EntityMethodController.getSetterMethod(beanClass, fieldName, getterMethod);
    }

    public MethodElement getGetterMethod(String fieldName) {
        return EntityMethodController.getGetterMethod(beanClass, fieldName);
    }
}
