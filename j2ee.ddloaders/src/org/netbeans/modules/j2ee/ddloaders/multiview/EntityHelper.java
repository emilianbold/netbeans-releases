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
import org.netbeans.modules.j2ee.dd.api.ejb.Query;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.action.AddCmpFieldAction;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.action.AddFinderMethodAction;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.action.AddSelectMethodAction;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.entity.methodcontroller.EntityMethodController;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.src.Identifier;
import org.openide.src.MethodElement;
import org.openide.src.MethodParameter;
import org.openide.src.SourceException;
import org.openide.src.Type;

import java.lang.reflect.Modifier;

/**
 * @author pfiala
 */
public class EntityHelper extends EntityAndSessionHelper {

    public EntityHelper(FileObject ejbJarFile, Entity entity) {
        super(ejbJarFile, entity);
    }

    public void addCmpField() {
        new AddCmpFieldAction().addCmpField(beanClass, ejbJarFile);
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

    public MethodElement createAccessMethod(String fieldName, Type type, boolean get) {
        MethodElement method = new MethodElement();
        try {
            method.setName(Identifier.create(Utils.getMethodName(fieldName, get)));
        } catch (SourceException e) {
            Utils.notifyError(e);
        }
        if (get) {
            try {
                method.setReturn(type);
            } catch (SourceException e) {
                Utils.notifyError(e);
            }
        } else {
            try {
                method.setParameters(
                        new MethodParameter[]{new MethodParameter(fieldName, type, false)});
            } catch (SourceException e) {
                Utils.notifyError(e);
            }
        }
        Utils.addMethod(beanClass, method, false, Modifier.PUBLIC | Modifier.ABSTRACT);
        return Utils.getMethod(beanClass, method);
    }

    public MethodElement getSetterMethod(String fieldName, MethodElement getterMethod) {
        return getterMethod == null ?
                null : EntityMethodController.getSetterMethod(beanClass, fieldName, getterMethod);
    }

    public MethodElement getGetterMethod(String fieldName) {
        return EntityMethodController.getGetterMethod(beanClass, fieldName);
    }

    public void removeQuery(Query query) {
        ((Entity) ejb).removeQuery(query);
    }

    public boolean hasLocalInterface() {
        return ejb.getLocal() != null;
    }

    public boolean hasRemoteInterface() {
        return ejb.getRemote() != null;
    }

    public String getPrimkeyField() {
        return ((Entity) ejb).getPrimkeyField();
    }

    public String getPrimKeyClass() {
        return ((Entity) ejb).getPrimKeyClass();
    }

    public void setPrimkeyField(String fieldName) {
        ((Entity) ejb).setPrimkeyField(fieldName);
    }

    public void setPrimKeyClass(String className) {
        ((Entity) ejb).setPrimKeyClass(className);
    }
}
