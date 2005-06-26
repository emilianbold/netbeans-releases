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
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.entity.methodcontroller.EntityMethodController;
import org.netbeans.modules.j2ee.common.JMIUtils;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.Type;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Field;
import org.netbeans.jmi.javamodel.PrimitiveType;
import org.netbeans.jmi.javamodel.Parameter;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

import java.io.IOException;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.Iterator;

/**
 * @author pfiala
 */
public class CmpFieldHelper {

    private EntityHelper entityHelper;
    private CmpField field;
    public static final String PROPERTY_FIELD_ROW_CHANGED = "FIELD_ROW_CHANGED"; // NOI18N

    public CmpFieldHelper(EntityHelper entityHelper, CmpField field) {
        this.entityHelper = entityHelper;
        this.field = field;
    }

    public String getTypeString() {
        Type type = getType();
        return type == null ? null : type.getName();
    }

    public Type getType() {
        Method getterMethod = entityHelper.getGetterMethod(getFieldName());
        return getterMethod == null ? null : getterMethod.getType();
    }

    public void reloadType() {
        setType(getTypeString());
    }

    public void setType(String typeString) {
        setType(JMIUtils.resolveType(typeString));
    }

    public void setType(Type newType) {
        EntityMethodController entityMethodController = entityHelper.getEntityMethodController();
        entityMethodController.beginWriteJmiTransaction();
        boolean rollback = true;
        try {
            if (newType instanceof PrimitiveType && isPrimary()) {
                newType = JMIUtils.getPrimitiveTypeWrapper((PrimitiveType) newType);
            }
            String fieldName = getFieldName();
            Method getterMethod = entityHelper.getGetterMethod(fieldName);
            Method setterMethod = entityHelper.getSetterMethod(fieldName, getterMethod);
            boolean primary = isPrimary();
            JavaClass localBusinessInterfaceClass = entityHelper.getLocalBusinessInterfaceClass();
            entityMethodController.registerClassForSave(localBusinessInterfaceClass);
            JavaClass remoteBusinessInterfaceClass = entityHelper.getRemoteBusinessInterfaceClass();
            entityMethodController.registerClassForSave(remoteBusinessInterfaceClass);
            changeReturnType(Utils.getMethod(localBusinessInterfaceClass, getterMethod), newType);
            Utils.changeParameterType(Utils.getMethod(localBusinessInterfaceClass, setterMethod), newType);
            changeReturnType(Utils.getMethod(remoteBusinessInterfaceClass, getterMethod), newType);
            Utils.changeParameterType(Utils.getMethod(remoteBusinessInterfaceClass, setterMethod), newType);
            changeReturnType(getterMethod, newType);
            Utils.changeParameterType(setterMethod, newType);
            newType = JMIUtils.resolveType(getTypeString());
            if (primary) {
                entityHelper.setPrimKeyClass(newType);
            }
            JavaClass beanClass = entityHelper.getBeanClass();
            entityMethodController.registerClassForSave(beanClass);
            Method[] methods = JMIUtils.getMethods(beanClass);
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                String name = method.getName();
                boolean isCreate = "ejbCreate".equals(name);
                boolean isPostCreate = "ejbPostCreate".equals(name);
                if (isCreate && primary) {
                    changeReturnType(method, newType);
                }
                if (isCreate || isPostCreate) {
                    List parameters = method.getParameters();
                    for (Iterator it1 = parameters.iterator(); it1.hasNext();) {
                        Parameter parameter = (Parameter) it1.next();
                        if (fieldName.equals(parameter.getName())) {
                            parameter.setType(newType);
                            break;
                        }
                    }
                }
            }
            rollback = false;
        } finally {
            entityMethodController.endWriteJmiTransaction(rollback);
        }
        entityHelper.cmpFields.firePropertyChange(null);
        modelUpdatedFromUI();
    }

    private void modelUpdatedFromUI() {
        entityHelper.modelUpdatedFromUI();
    }

    private void changeReturnType(Method method, Type type) {
        if (method != null) {
            method.setType(type);
        }
    }

    public boolean hasLocalGetter() {
        return getLocalGetter() != null;
    }

    private Method getLocalGetter() {
        return entityHelper.getEntityMethodController().getGetterMethod(getFieldName(), true);
    }

    public boolean hasLocalSetter() {
        return getLocalSetter() != null;
    }

    private Method getLocalSetter() {
        return entityHelper.getEntityMethodController().getSetterMethod(getFieldName(), true);
    }

    public boolean hasRemoteGetter() {
        return getRemoteGetter() != null;
    }

    private Method getRemoteGetter() {
        return entityHelper.getEntityMethodController().getGetterMethod(getFieldName(), false);
    }

    public boolean hasRemoteSetter() {
        return getRemoteSetter() != null;
    }

    private Method getRemoteSetter() {
        return entityHelper.getEntityMethodController().getSetterMethod(getFieldName(), false);
    }

    public void setLocalGetter(boolean create) {
        entityHelper.updateFieldAccessor(getFieldName(), true, true, create);
    }

    public void setLocalSetter(boolean create) {
        entityHelper.updateFieldAccessor(getFieldName(), false, true, create);
    }

    public void setRemoteGetter(boolean create) {
        entityHelper.updateFieldAccessor(getFieldName(), true, false, create);
    }

    public void setRemoteSetter(boolean create) {
        entityHelper.updateFieldAccessor(getFieldName(), false, false, create);
    }

    public boolean deleteCmpField() {
        String message = NbBundle.getMessage(CmpFieldHelper.class, "MSG_ConfirmDeleteField", field.getFieldName());
        String title = NbBundle.getMessage(CmpFieldHelper.class, "MSG_ConfirmDeleteFieldTitle");
        NotifyDescriptor desc = new NotifyDescriptor.Confirmation(message, title, NotifyDescriptor.YES_NO_OPTION);
        if (NotifyDescriptor.YES_OPTION.equals(DialogDisplayer.getDefault().notify(desc))) {
            EntityMethodController entityMethodController = entityHelper.getEntityMethodController();
            entityMethodController.beginWriteJmiTransaction();
            boolean rollback = true;
            try {
                String fieldName = getFieldName();
                Method getterMethod = entityHelper.getGetterMethod(fieldName);
                Method setterMethod = entityHelper.getSetterMethod(fieldName, getterMethod);
                JavaClass localBusinessInterfaceClass = entityHelper.getLocalBusinessInterfaceClass();
                entityMethodController.registerClassForSave(localBusinessInterfaceClass);
                removeMethod(localBusinessInterfaceClass, getterMethod);
                removeMethod(localBusinessInterfaceClass, setterMethod);
                JavaClass remoteBusinessInterfaceClass = entityHelper.getRemoteBusinessInterfaceClass();
                entityMethodController.registerClassForSave(remoteBusinessInterfaceClass);
                removeMethod(remoteBusinessInterfaceClass, getterMethod);
                removeMethod(remoteBusinessInterfaceClass, setterMethod);
                try {
                    entityMethodController.deleteField(field, entityHelper.ejbJarFile);
                } catch (IOException e) {
                    Utils.notifyError(e);
                }
                rollback = false;
            } finally {
                entityMethodController.endWriteJmiTransaction(rollback);
            }
            modelUpdatedFromUI();
            return true;
        }
        return false;
    }


    private static void removeMethod(JavaClass interfaceClass, Method method) {
        if (Utils.getMethod(interfaceClass, method) != null) {
            Utils.removeMethod(interfaceClass, method);
        }
    }

    public void setFieldName(String newName) {
        //todo: launch refactoring instead of following code
        final IllegalArgumentException ex = FieldCustomizer.validateFieldName(newName);
        if (ex != null) {
            ErrorManager.getDefault().notify(ex);
            return;
        }
        String fieldName = getFieldName();
        Method[] methods = JMIUtils.getMethods(entityHelper.getBeanClass());

        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            String name = method.getName();
            if ("ejbCreate".equals(name) || "ejbPostCreate".equals(name)) {
                List parameters = method.getParameters();
                for (Iterator it = parameters.iterator(); it.hasNext();) {
                    Parameter parameter = (Parameter) it.next();
                    if (fieldName.equals(parameter.getName())) {
                        parameter.setName(newName);
                        break;
                    }
                }
            }
        }
        if (isPrimary()) {
            entityHelper.setPrimkeyFieldName(newName);
        }
        final int oldFieldRow = entityHelper.cmpFields.getFieldRow(field);
        Method localGetter = getLocalGetter();
        Method localSetter = getLocalSetter();
        Method remoteGetter = getRemoteGetter();
        Method remoteSetter = getRemoteSetter();
        String getterName = Utils.getMethodName(newName, true);
        String setterName = Utils.getMethodName(newName, false);
        field.setFieldName(newName);
        Method getterMethod = entityHelper.getGetterMethod(fieldName);
        Method setterMethod = entityHelper.getSetterMethod(fieldName, getterMethod);
        Utils.renameMethod(getterMethod, getterName);
        Utils.renameMethod(setterMethod, setterName);
        Utils.renameMethod(localGetter, getterName);
        Utils.renameMethod(localSetter, setterName);
        Utils.renameMethod(remoteGetter, getterName);
        Utils.renameMethod(remoteSetter, setterName);
        final int newFieldRow = entityHelper.cmpFields.getFieldRow(field);
        if (oldFieldRow != newFieldRow) {
            entityHelper.cmpFields.firePropertyChange(new PropertyChangeEvent(entityHelper.cmpFields,
                    PROPERTY_FIELD_ROW_CHANGED, new Integer(oldFieldRow), new Integer(newFieldRow)));
        }
        modelUpdatedFromUI();
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
        Field field = JavaModel.getDefaultExtent().getField().createField();
        field.setName(getFieldName());
        field.setType(JMIUtils.resolveType(getTypeString()));
        String title = Utils.getBundleMessage("LBL_EditCmpField");
        FieldCustomizer customizer = new FieldCustomizer(field, getDefaultDescription(),
                entityHelper.hasLocalInterface(), entityHelper.hasRemoteInterface(), hasLocalGetter(),
                hasLocalSetter(), hasRemoteGetter(), hasRemoteSetter());
        NotifyDescriptor nd = new NotifyDescriptor(customizer, title, NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.PLAIN_MESSAGE, null, null);
        while (true) {
            boolean resultIsOk = DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION;
            if (resultIsOk) {
                customizer.isOK();  // apply possible changes in dialog fields
                Utils.beginJmiTransaction(true);
                boolean rollback = true;
                try {
                    setFieldName(field.getName());
                    setType(field.getType());
                    setDescription(customizer.getDescription());
                    setLocalGetter(customizer.isLocalGetter());
                    setLocalSetter(customizer.isLocalSetter());
                    setRemoteGetter(customizer.isRemoteGetter());
                    setRemoteSetter(customizer.isRemoteSetter());
                    rollback = false;
                } finally {
                    Utils.endJmiTransaction(rollback);
                }
                modelUpdatedFromUI();
                return true;
            } else {
                return false;
            }
        }
    }

}
