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

import org.netbeans.modules.j2ee.dd.api.ejb.CmpField;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.action.AddFinderMethodAction;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.action.AddSelectMethodAction;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.entity.EntityNode;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.src.ClassElement;
import org.openide.src.MethodElement;
import org.openide.util.NbBundle;

import java.io.IOException;

/**
 * @author pfiala
 */
public class CmpFieldHelper extends EntityHelper {
    private MethodElement getterMethod;
    private MethodElement setterMethod;
    private CmpField field;

    public CmpFieldHelper(FileObject ejbJarFile, Entity entity, CmpField field) {
        super(ejbJarFile, entity);
        this.field = field;
        String fieldName = this.field.getFieldName();
        ClassElement beanClass = this.beanClass;
        getterMethod = Utils.getGetterMethod(beanClass, fieldName);
        if (getterMethod != null) {
            MethodElement getterMethod = this.getterMethod;
            setterMethod = Utils.getSetterMethod(beanClass, fieldName, getterMethod);
        } else {
            setterMethod = null;
        }
    }

    public String getType() {
        return getterMethod == null ? null : getterMethod.getReturn().getFullString();
    }

    public boolean hasLocalGetter() {
        return Utils.getBusinessMethod(localInterface, getterMethod) != null;
    }

    public boolean hasLocalSetter() {
        return Utils.getBusinessMethod(localInterface, setterMethod) != null;
    }

    public boolean hasRemoteGetter() {
        return Utils.getBusinessMethod(remoteInterface, getterMethod) != null;
    }

    public boolean hasRemoteSetter() {
        return Utils.getBusinessMethod(remoteInterface, setterMethod) != null;
    }

    public boolean deleteCmpField() {
        String message = NbBundle.getMessage(CmpFieldHelper.class, "MSG_ConfirmDeleteField", field.getFieldName());
        String title = NbBundle.getMessage(CmpFieldHelper.class, "MSG_ConfirmDeleteFieldTitle");
        NotifyDescriptor desc = new NotifyDescriptor.Confirmation(message, title, NotifyDescriptor.YES_NO_OPTION);
        if (NotifyDescriptor.YES_OPTION.equals(DialogDisplayer.getDefault().notify(desc))) {
            try {
                Utils.createFieldNode(ejbJarFile, entity, field).destroy();
            } catch (IOException e) {
                Utils.notifyError(e);
            }
            return true;
//            ClassElement beanClass = Utils.getBeanClass(ejbJarFile,
//                    entity);
//            EntityMethodController emc = (EntityMethodController) EntityMethodController.createFromClass(beanClass);
//            try {
//                emc.deleteField(field, ejbJarFile);
//                return true;
//            } catch (SourceException e) {
//                Utils.notifyError(e);
//            } catch (IOException e) {
//                Utils.notifyError(e);
//            }
        }
        return false;
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
