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
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.action.FieldCustomizer;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.entity.CMPFieldNode;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.entity.methodcontroller.EntityMethodController;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.src.ClassElement;
import org.openide.src.FieldElement;
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
        initAccessMethods();
    }

    public void initAccessMethods() {
        String fieldName = field.getFieldName();
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

    public void setType(String type) {
        initAccessMethods();
        Type newType = createType(type);
        if (isPrimary()) {
            entityHelper.setPrimKeyClass(type);
            Identifier primaryMethod = Identifier.create("findByPrimaryKey");
            ClassElement classElement = entityHelper.getLocalHomeInterfaceClass();
            Type[] origArguments = new Type[]{createType(getType())};
            if (classElement != null) {
                MethodElement method = classElement.getMethod(primaryMethod, origArguments);
                changeParameterType(method, newType);
            }
            classElement = entityHelper.getHomeInterfaceClass();
            if (classElement != null) {
                MethodElement method = classElement.getMethod(primaryMethod, origArguments);
                changeParameterType(method, newType);
            }
            MethodElement method = entityHelper.beanClass.getMethod(Identifier.create("ejbCreate"), origArguments);
            changeParameterType(method, newType);
            changeReturnType(method, newType);
            method = entityHelper.beanClass.getMethod(Identifier.create("ejbPostCreate"), origArguments);
            changeParameterType(method, newType);
        }
        ClassElement localBusinessInterfaceClass = entityHelper.getLocalBusinessInterfaceClass();
        ClassElement remoteBusinessInterfaceClass = entityHelper.getRemoteBusinessInterfaceClass();
        changeReturnType(getBusinessMethod(localBusinessInterfaceClass, getterMethod), newType);
        changeParameterType(getBusinessMethod(localBusinessInterfaceClass, setterMethod), newType);
        changeReturnType(getBusinessMethod(remoteBusinessInterfaceClass, getterMethod), newType);
        changeParameterType(getBusinessMethod(remoteBusinessInterfaceClass, setterMethod), newType);
        changeReturnType(getterMethod, newType);
        changeParameterType(setterMethod, newType);
        modelUpdatedFromUI();
    }

    private void modelUpdatedFromUI() {
        entityHelper.modelUpdatedFromUI();
    }

    private static Type createType(String type) {
        return Type.createClass(Identifier.create(type));
    }

    private void changeParameterType(final MethodElement method, Type type) {
        if (method != null) {
            MethodParameter[] parameters = method.getParameters();
            parameters[0].setType(type);
            try {
                method.setParameters(parameters);
            } catch (SourceException e) {
                Utils.notifyError(e);
            }
        }
    }

    private void changeReturnType(MethodElement method, Type type) {
        if (method != null) {
            try {
                method.setReturn(type);
                modelUpdatedFromUI();
            } catch (SourceException e) {
                Utils.notifyError(e);
            }

        }
    }

    public boolean hasLocalGetter() {
        ClassElement interfaceElement = entityHelper.getLocalBusinessInterfaceClass();
        MethodElement method = getterMethod;
        return getBusinessMethod(interfaceElement, method) != null;
    }

    private static MethodElement getBusinessMethod(ClassElement interfaceElement, MethodElement method) {
        return Utils.getMethod(interfaceElement, method);
    }

    public boolean hasLocalSetter() {
        return getBusinessMethod(entityHelper.getLocalBusinessInterfaceClass(), setterMethod) != null;
    }

    public boolean hasRemoteGetter() {
        return getBusinessMethod(entityHelper.getRemoteBusinessInterfaceClass(), getterMethod) != null;
    }

    public boolean hasRemoteSetter() {
        return getBusinessMethod(entityHelper.getRemoteBusinessInterfaceClass(), setterMethod) != null;
    }

    public void setLocalGetter(boolean create) {
        ClassElement businessInterfaceClass = entityHelper.getLocalBusinessInterfaceClass();
        if (create) {
            Utils.addMethod(businessInterfaceClass, getterMethod, false, 0);
        } else {
            Utils.removeBusinessMethod(businessInterfaceClass, getterMethod);
        }
    }

    public void setLocalSetter(boolean create) {
        ClassElement localBusinessInterfaceClass = entityHelper.getLocalBusinessInterfaceClass();
        if (create) {
            Utils.addMethod(localBusinessInterfaceClass, setterMethod, false, 0);
        } else {
            Utils.removeBusinessMethod(localBusinessInterfaceClass, setterMethod);
        }
    }

    public void setRemoteGetter(boolean create) {
        ClassElement remoteBusinessInterfaceClass = entityHelper.getRemoteBusinessInterfaceClass();
        if (create) {
            Utils.addMethod(remoteBusinessInterfaceClass, getterMethod, true, 0);
        } else {
            Utils.removeBusinessMethod(remoteBusinessInterfaceClass, getterMethod);
        }
    }

    public void setRemoteSetter(boolean create) {
        ClassElement remoteBusinessInterfaceClass = entityHelper.getRemoteBusinessInterfaceClass();
        if (create) {
            Utils.addMethod(remoteBusinessInterfaceClass, setterMethod, true, 0);
        } else {
            Utils.removeBusinessMethod(remoteBusinessInterfaceClass, setterMethod);
        }
    }

    public boolean deleteCmpField() {
        String message = NbBundle.getMessage(CmpFieldHelper.class, "MSG_ConfirmDeleteField", field.getFieldName());
        String title = NbBundle.getMessage(CmpFieldHelper.class, "MSG_ConfirmDeleteFieldTitle");
        NotifyDescriptor desc = new NotifyDescriptor.Confirmation(message, title, NotifyDescriptor.YES_NO_OPTION);
        if (NotifyDescriptor.YES_OPTION.equals(DialogDisplayer.getDefault().notify(desc))) {
            ClassElement localBusinessInterfaceClass = entityHelper.getLocalBusinessInterfaceClass();
            removeMethod(localBusinessInterfaceClass, getterMethod);
            removeMethod(localBusinessInterfaceClass, setterMethod);
            ClassElement remoteBusinessInterfaceClass = entityHelper.getRemoteBusinessInterfaceClass();
            removeMethod(remoteBusinessInterfaceClass, getterMethod);
            removeMethod(remoteBusinessInterfaceClass, setterMethod);
            try {
                EntityMethodController ec = (EntityMethodController) EntityMethodController.createFromClass(
                        entityHelper.beanClass);
                new CMPFieldNode(field, ec, entityHelper.ejbJarFile).destroy();
                modelUpdatedFromUI();
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
        if (isPrimary()) {
            entityHelper.setPrimkeyField(newName);
        }
        ClassElement localBusinessInterfaceClass = entityHelper.getLocalBusinessInterfaceClass();
        MethodElement localGetter = getBusinessMethod(localBusinessInterfaceClass, getterMethod);
        MethodElement localSetter = getBusinessMethod(localBusinessInterfaceClass, setterMethod);
        ClassElement remoteBusinessInterfaceClass = entityHelper.getRemoteBusinessInterfaceClass();
        MethodElement remoteGetter = getBusinessMethod(remoteBusinessInterfaceClass, getterMethod);
        MethodElement remoteSetter = getBusinessMethod(remoteBusinessInterfaceClass, setterMethod);
        Identifier getterName = Identifier.create(Utils.getMethodName(newName, true));
        Identifier setterName = Identifier.create(Utils.getMethodName(newName, false));
        field.setFieldName(newName);
        modelUpdatedFromUI();
        Utils.renameMethod(getterMethod, getterName);
        Utils.renameMethod(setterMethod, setterName);
        Utils.renameMethod(localGetter, getterName);
        Utils.renameMethod(localSetter, setterName);
        Utils.renameMethod(remoteGetter, getterName);
        Utils.renameMethod(remoteSetter, setterName);
    }

    public void setDescription(String s) {
        field.setDescription(s);
        modelUpdatedFromUI();
    }

    public String getDefaultDescription() {
        return field.getDefaultDescription();
    }

    public String getFieldName() {
        return field.getFieldName();
    }

    public boolean isPrimary() {
        return getFieldName().equals(entityHelper.getPrimkeyField());
    }

    public boolean edit() {
        FieldElement element = new FieldElement();
        try {
            element.setName(Identifier.create(getFieldName()));
            element.setType(createType(getType()));
        } catch (SourceException e) {
            Utils.notifyError(e);
            return false;
        }
        String title = Utils.getBundleMessage("LBL_EditCmpField");
        FieldCustomizer customizer = new FieldCustomizer(element, getDefaultDescription(),
                entityHelper.hasLocalInterface(), entityHelper.hasRemoteInterface(), hasLocalGetter(),
                hasLocalSetter(), hasRemoteGetter(), hasRemoteSetter());
        NotifyDescriptor nd = new NotifyDescriptor(customizer, title, NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.PLAIN_MESSAGE, null, null);
        boolean resultIsOk = DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION;
        customizer.isOK();  // apply possible changes in dialog fields
        if (resultIsOk) {
            setFieldName(element.getName().toString());
            setType(element.getType().toString());
            setDescription(customizer.getDescription());
            setLocalGetter(customizer.isLocalGetter());
            setLocalSetter(customizer.isLocalSetter());
            setRemoteGetter(customizer.isRemoteGetter());
            setRemoteSetter(customizer.isRemoteSetter());
            modelUpdatedFromUI();
            return true;
        }
        return false;
    }
}
