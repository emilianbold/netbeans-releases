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
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.action.AddFinderMethodAction;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.action.AddSelectMethodAction;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.entity.CMPFieldNode;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.entity.EntityNode;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.entity.methodcontroller.EntityMethodController;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.src.ClassElement;
import org.openide.src.Identifier;
import org.openide.src.MethodElement;
import org.openide.src.MethodParameter;
import org.openide.src.SourceException;
import org.openide.src.Type;
import org.openide.util.NbBundle;

import java.io.IOException;

/**
 * @author pfiala
 */
public class CmpFieldHelper {

    private MethodElement getterMethod;
    private MethodElement setterMethod;
    private EntityHelper entityHelper;
    private CmpField field;

    public CmpFieldHelper(EntityHelper entityHelper, CmpField field) {
        this.entityHelper = entityHelper;
        this.field = field;
        String fieldName = this.field.getFieldName();
        getterMethod = entityHelper.getGetterMethod(fieldName);
        if (getterMethod != null) {
            MethodElement getterMethod = this.getterMethod;
            setterMethod = entityHelper.getSetterMethod(fieldName, getterMethod);
        } else {
            setterMethod = null;
        }
    }

    public String getType() {
        return getterMethod == null ? null : getterMethod.getReturn().getFullString();
    }

    public void setType(String newType) {
        Identifier identifier = Identifier.create(newType);
        Type type = Type.createClass(identifier);
        try {
            getterMethod.setReturn(type);
        } catch (SourceException e) {
            Utils.notifyError(e);
        }
        if (setterMethod != null) {
            MethodParameter[] parameters = setterMethod.getParameters();
            parameters[0].setType(type);
            try {
                setterMethod.setParameters(parameters);
            } catch (SourceException e) {
                Utils.notifyError(e);
            }
        }
    }

    public boolean hasLocalGetter() {
        ClassElement interfaceElement = entityHelper.localBusinessInterfaceClass;
        MethodElement method = getterMethod;
        return getBusinessMethod(interfaceElement, method) != null;
    }

    private static MethodElement getBusinessMethod(ClassElement interfaceElement, MethodElement method) {
        return Utils.getMethod(interfaceElement, method);
    }

    public boolean hasLocalSetter() {
        return getBusinessMethod(entityHelper.localBusinessInterfaceClass, setterMethod) != null;
    }

    public boolean hasRemoteGetter() {
        return getBusinessMethod(entityHelper.remoteBusinessInterfaceClass, getterMethod) != null;
    }

    public boolean hasRemoteSetter() {
        return getBusinessMethod(entityHelper.remoteBusinessInterfaceClass, setterMethod) != null;
    }

    public void setLocalGetter(boolean create) {
        if (create) {
            Utils.addMethod(entityHelper.localBusinessInterfaceClass, getterMethod, false);
        } else {
            Utils.removeBusinessMethod(entityHelper.localBusinessInterfaceClass, getterMethod);
        }
    }

    public void setLocalSetter(boolean create) {
        if (create) {
            Utils.addMethod(entityHelper.localBusinessInterfaceClass, setterMethod, false);
        } else {
            Utils.removeBusinessMethod(entityHelper.localBusinessInterfaceClass, setterMethod);
        }
    }

    public void setRemoteGetter(boolean create) {
        if (create) {
            Utils.addMethod(entityHelper.remoteBusinessInterfaceClass, getterMethod, true);
        } else {
            Utils.removeBusinessMethod(entityHelper.remoteBusinessInterfaceClass, getterMethod);
        }
    }

    public void setRemoteSetter(boolean create) {
        if (create) {
            Utils.addMethod(entityHelper.remoteBusinessInterfaceClass, setterMethod, true);
        } else {
            Utils.removeBusinessMethod(entityHelper.remoteBusinessInterfaceClass, setterMethod);
        }
    }

    public boolean deleteCmpField() {
        String message = NbBundle.getMessage(CmpFieldHelper.class, "MSG_ConfirmDeleteField", field.getFieldName());
        String title = NbBundle.getMessage(CmpFieldHelper.class, "MSG_ConfirmDeleteFieldTitle");
        NotifyDescriptor desc = new NotifyDescriptor.Confirmation(message, title, NotifyDescriptor.YES_NO_OPTION);
        if (NotifyDescriptor.YES_OPTION.equals(DialogDisplayer.getDefault().notify(desc))) {
            removeMethod(entityHelper.localBusinessInterfaceClass, getterMethod);
            removeMethod(entityHelper.localBusinessInterfaceClass, setterMethod);
            removeMethod(entityHelper.remoteBusinessInterfaceClass, getterMethod);
            removeMethod(entityHelper.remoteBusinessInterfaceClass, setterMethod);
            try {
                EntityMethodController ec = (EntityMethodController) EntityMethodController.createFromClass(
                        entityHelper.beanClass);
                new CMPFieldNode(field, ec, entityHelper.ejbJarFile).destroy();
            } catch (IOException e) {
                Utils.notifyError(e);
            }
            return true;
        }
        return false;
    }

    private static void removeMethod(ClassElement interfaceElement, MethodElement method) {
        MethodElement businessMethod = getBusinessMethod(interfaceElement, method);
        if (businessMethod != null) {
            Utils.removeMethod(interfaceElement, method);
        }
    }

    public void setFieldName(String newName) {
        //todo: launch refactoring instead of following code
        MethodElement localGetter = getBusinessMethod(entityHelper.localBusinessInterfaceClass, getterMethod);
        MethodElement localSetter = getBusinessMethod(entityHelper.localBusinessInterfaceClass, setterMethod);
        MethodElement remoteGetter = getBusinessMethod(entityHelper.remoteBusinessInterfaceClass, getterMethod);
        MethodElement remoteSetter = getBusinessMethod(entityHelper.remoteBusinessInterfaceClass, setterMethod);
        Identifier getterName = Identifier.create(Utils.getMethodName(newName, true));
        Identifier setterName = Identifier.create(Utils.getMethodName(newName, false));
        field.setFieldName(newName);
        Utils.renameMethod(getterMethod, getterName);
        Utils.renameMethod(setterMethod, setterName);
        Utils.renameMethod(localGetter, getterName);
        Utils.renameMethod(localSetter, setterName);
        Utils.renameMethod(remoteGetter, getterName);
        Utils.renameMethod(remoteSetter, setterName);
    }

}
