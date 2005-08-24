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
package org.netbeans.modules.j2ee.ejbcore.api.methodcontroller;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.rmi.RemoteException;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.jmi.javamodel.Field;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.Parameter;
import org.netbeans.jmi.javamodel.Type;
import org.netbeans.modules.j2ee.common.JMIUtils;
import org.netbeans.modules.j2ee.dd.api.ejb.CmpField;
import org.netbeans.modules.j2ee.dd.api.ejb.CmrField;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbRelation;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbRelationshipRole;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.MethodParams;
import org.netbeans.modules.j2ee.dd.api.ejb.Query;
import org.netbeans.modules.j2ee.dd.api.ejb.QueryMethod;
import org.netbeans.modules.j2ee.dd.api.ejb.Relationships;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.AbstractMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
public class EntityMethodController extends AbstractMethodController {
    private Entity model;
    private EjbJar parent;
    private static final int MODIFIERS_PUBLIC_ABSTRACT = Modifier.PUBLIC | Modifier.ABSTRACT;

    public EntityMethodController(Entity model, ClassPath cp, EjbJar parent) {
        super(model,cp);
        this.model = model;
        this.parent = parent;
    }

    public List getMethods(CmpField field) {
        return getMethods(field.getFieldName());
    }

    public List getMethods(CmrField field) {
        return getMethods(field.getCmrFieldName());
    }

    public void deleteQueryMapping(Method method, FileObject dd) throws IOException {
        Query[] queries = model.getQuery();
        Query q = null;
        for (int i=0; i < queries.length; i++) {
            q = queries[i];
            String methodName = q.getQueryMethod().getMethodName();
            if (methodName.equals(method.getName())) {
                break;
            }
        }
        if (q != null) {
            model.removeQuery(q);
            parent.write(dd);
        }
    }

    /**
     * Deletes CMP field (consists of following subtasks):<br>
     * <ul>
     * <li>delete findBy<field> method from interfaces (it should be only in Home interfaces,
     * but local and remote interfaces are checked also)</li>
     * <li>delete get<field> and set<field> from local/remote/business interface</li>
     * <li>delete get<field> and set<field> from main bean class</li>
     * <li>delete findBy<field> query from deployment descriptor (ejb-jar.xml)</li>
     * <li>delete CMP field from deployment descriptor (ejb-jar.xml)</li>
     * </ul>
     * @param field
     * @param dd
     * @throws java.io.IOException
     */
    public void deleteField(CmpField field, FileObject dd) throws IOException {
        beginWriteJmiTransaction();
        boolean rollback = true;
        Query query = null;
        try {
            String fieldName = field.getFieldName();
            // find findBy<field> query in DD
            Query[] queries = model.getQuery();
            for (int i = 0; i < queries.length; i++) {
                query = queries[i];
                String queryMethodName = query.getQueryMethod().getMethodName();
                if (queryMethodName.equals(prependAndUpper(fieldName, "findBy"))) {
                    break;
                }
            }
            // remove findBy<field> method from local interfaces (should be only in local home)
            for (Iterator iter = getLocalInterfaces().iterator(); iter.hasNext();) {
                JavaClass ce = (JavaClass) iter.next();
                Method me = getFinderMethod(ce, fieldName, getGetterMethod(getBeanClass(), fieldName));
                if (me != null) {
                    JavaClass declaringClass = (JavaClass) me.getDeclaringClass();
                    registerClassForSave(declaringClass);
                    declaringClass.getContents().remove(me);
                }
            }
            // remove findBy<field> method from remote interfaces (should be only in remote home)
            for (Iterator iter = getRemoteInterfaces().iterator(); iter.hasNext();) {
                JavaClass ce = (JavaClass) iter.next();
                Method me = getFinderMethod(ce, fieldName, getGetterMethod(getBeanClass(), fieldName));
                if (me != null) {
                    JavaClass declaringClass = (JavaClass) me.getDeclaringClass();
                    registerClassForSave(declaringClass);
                    declaringClass.getContents().remove(me);
                }
            }
            removeMethodsFromBean(getMethods(fieldName));
            updateFieldAccessors(fieldName, false, false, false, false);
            rollback = false;
        } finally {
            endWriteJmiTransaction(rollback);
        }
        // remove findBy<field> query from DD
        if (query != null) {
            model.removeQuery(query);
        }
        // remove CMP field from DD
        model.removeCmpField(field);
        parent.write(dd);
    }

    private void removeMethodsFromBean(List methods) {
        for (Iterator iter = methods.iterator(); iter.hasNext();) {
            Method method = (Method) iter.next();
            // remove get/set<field> from local/remote/business interfaces
            if (hasLocal()) {
                Method me = getInterface(method, true);
                if (me != null) {
                    JavaClass declaringClass = (JavaClass) me.getDeclaringClass();
                    registerClassForSave(declaringClass);
                    declaringClass.getContents().remove(method);
                }
            }
            if (hasRemote()) {
                Method me = getInterface(method, false);
                if (me != null) {
                    JavaClass declaringClass = (JavaClass) me.getDeclaringClass();
                    registerClassForSave(declaringClass);
                    declaringClass.getContents().remove(method);
                }
            }
            // remove get/set<field> from main bean class
            JavaClass declaringClass = (JavaClass) method.getDeclaringClass();
            registerClassForSave(declaringClass);
            declaringClass.getContents().remove(method);
        }
    }

    /**
     * Deletes CMP field (consists of following subtasks):<br>
     * <ul>
     * <li>delete get<field> and set<field> from local/remote/business interface</li>
     * <li>delete get<field> and set<field> from main bean class</li>
     * <li>delete relationship from deployment descriptor (ejb-jar.xml)</li>
     * </ul>
     * @param field
     * @param dd
     * @throws java.io.IOException
     */
    public void deleteField(CmrField field, FileObject dd) throws IOException {
        beginWriteJmiTransaction();
        boolean rollback = true;
        try {
            List methods = getMethods(field.getCmrFieldName());
            removeMethodsFromBean(methods);
            rollback = false;
        } finally {
            endWriteJmiTransaction(rollback);
        }
        // remove relation from DD
        deleteRelationships(field.getCmrFieldName());
        parent.write(dd);
    }

    public static String getMethodName(String fieldName, boolean get) {
        String prefix = get ? "get" : "set"; //NOI18N;
        return prependAndUpper(fieldName, prefix);
    }

    public boolean hasJavaImplementation(Method intfView) {
        return hasJavaImplementation(getMethodTypeFromInterface(intfView));
    }

    public boolean hasJavaImplementation(MethodType mt) {
        return !(isCMP() && (isFinder(mt) || isSelect(mt)));
    }

    public MethodType getMethodTypeFromImpl(Method implView) {
        MethodType mt = null;
        if (implView.getName().startsWith("ejbCreate")) { //NOI18N
            mt = new MethodType.CreateMethodType(implView);
        } else if (!implView.getName().startsWith("ejb")) { //NOI18N
            mt = new MethodType.BusinessMethodType(implView);
        } else if (implView.getName().startsWith("ejbFind")) { //NOI18N
            mt = new MethodType.FinderMethodType(implView);
        } else if (implView.getName().startsWith("ejbHome")) { //NOI18N
            mt = new MethodType.HomeMethodType(implView);
        }
        return mt;
    }

    public MethodType getMethodTypeFromInterface(Method clientView) {
        assert clientView.getDeclaringClass() != null: "declaring class cannot be null";
        String cName = clientView.getDeclaringClass().getName();
        MethodType mt;
        if (cName.equals(model.getLocalHome()) ||
            cName.equals(model.getHome())) {
            if (clientView.getName().startsWith("create")) { //NOI18N
                mt = new MethodType.CreateMethodType(clientView);
            } else if (clientView.getName().startsWith("find")) { //NOI18N
                mt = new MethodType.FinderMethodType(clientView);
            } else {
                mt = new MethodType.HomeMethodType(clientView);
            }
        } else {
            mt = new MethodType.BusinessMethodType(clientView);
        }
        return mt;
    }

    public AbstractMethodController.GenerateFromImpl createGenerateFromImpl() {
        return new EntityGenerateFromImplVisitor(model);
    }

    public AbstractMethodController.GenerateFromIntf createGenerateFromIntf() {
        return new EntityGenerateFromIntfVisitor(model);
    }

    public void addSelectMethod(Method selectMethod, String ql, FileObject dd)
    throws IOException {
        getBeanClass().getContents().add(selectMethod);
        addEjbQl(selectMethod,ql, dd);
    }

    public void addEjbQl(Method clientView, String ejbql, FileObject dd) throws IOException {
        if (isBMP()) {
            super.addEjbQl(clientView, ejbql, dd);
        }

        model.addQuery(buildQuery(clientView, ejbql));
        parent.write(dd);
    }

    public void addField(Field field, FileObject ddFile, boolean localGetter, boolean localSetter,
            boolean remoteGetter, boolean remoteSetter, String description) throws IOException {
        beginWriteJmiTransaction();
        boolean rollback = true;
        try {
            JavaClass beanClass = getBeanClass();
            registerClassForSave(beanClass);
            addGetterMethod(beanClass, field, MODIFIERS_PUBLIC_ABSTRACT, false);
            addSetterMethod(beanClass, field, MODIFIERS_PUBLIC_ABSTRACT, false);
            final String fieldName = field.getName();
            updateFieldAccessors(fieldName, localGetter, localSetter, remoteGetter, remoteSetter);
            rollback = false;
        } finally {
            endWriteJmiTransaction(rollback);
        }
        CmpField f = model.newCmpField();
        f.setFieldName(field.getName());
        f.setDescription(description);
        model.addCmpField(f);
        parent.write(ddFile);
    }

    public void validateNewCmpFieldName(String name) {
        CmpField[] cmpField = model.getCmpField();
        for (int i = 0; i < cmpField.length; i++) {
            CmpField field = cmpField[i];
            if (name.equals(field.getFieldName())) {
                throw new IllegalArgumentException(NbBundle.getMessage(EntityMethodController.class,
                        "MSG_Duplicate_Field_Name", name));
            }
        }
    }

    private static void addSetterMethod(JavaClass javaClass, Field field, int modifiers, boolean remote) {
        addMethod(javaClass, createSetterMethod(javaClass, field, modifiers, remote));
    }

    private static void addGetterMethod(JavaClass javaClass, Field fe, int modifiers, boolean remote) {
        addMethod(javaClass, createGetterMethod(javaClass, fe, modifiers, remote));
    }

    private static void addMethod(JavaClass javaClass, Method method) {
        javaClass.getContents().add(method);
    }

    private static void removeMethod(JavaClass javaClass, Method method) {
        javaClass.getContents().remove(method);
    }

    private static Method createGetterMethod(JavaClass javaClass, Field field, int modifiers, boolean remote) {
        Method method = JMIUtils.createMethod(javaClass);
        method.setModifiers(modifiers);
        method.setType(field.getType());
        method.setName(getMethodName(field.getName(), true));
        if (remote) {
            JMIUtils.addException(method, RemoteException.class.getName());
        }
        return method;
    }

    private static Method createSetterMethod(JavaClass javaClass, Field field, int modifiers, boolean remote) {
        Method method = JMIUtils.createMethod(javaClass);
        method.setModifiers(modifiers);
        method.setType(JMIUtils.resolveType("void"));
        final String fieldName = field.getName();
        method.setName(getMethodName(fieldName, false));
        method.getParameters().add(JMIUtils.createParameter(method, fieldName, field.getType(), false));
        if (remote) {
            JMIUtils.addException(method, RemoteException.class.getName());
        }
        return method;
    }

    private boolean isBMP() {
        return Entity.PERSISTENCE_TYPE_BEAN.equals(model.getPersistenceType());
    }

    public boolean isCMP() {
        return !isBMP();
    }

    /** @deprecated
     */
    private boolean isFinder(MethodType mt) {
        return mt instanceof MethodType.FinderMethodType;
    }

    /** @deprecated
     */
    private boolean isSelect(MethodType mt) {
        return mt instanceof MethodType.SelectMethodType;
    }

    private boolean isFinder(int mt) {
        return mt == MethodType.METHOD_TYPE_FINDER;
    }

    private boolean isSelect(int mt) {
        return mt == MethodType.METHOD_TYPE_SELECT;
    }

    public String createDefaultQL(MethodType mt) {
        String ql = null;
        if (isFinder(mt) && isCMP()) {
            ql = "SELECT OBJECT(o) \nFROM " + model.getAbstractSchemaName() + " o";
        }

        if (isSelect(mt)) {
            ql = "SELECT COUNT(o) \nFROM " + model.getAbstractSchemaName() + " o";
        }

        return ql;
    }

    private Query buildQuery(Method clientView, String ejbql) {
        Query q = model.newQuery();
        QueryMethod qm = q.newQueryMethod();
        qm.setMethodName(clientView.getName());
        MethodParams mParams = qm.newMethodParams();
        Parameter[] params = (Parameter[]) clientView.getParameters().toArray(new Parameter[clientView.getParameters().size()]);
        for (int i =0; i < params.length; i++) {
            mParams.addMethodParam(params[i].getType().getName());
        }
        qm.setMethodParams(mParams);
        q.setQueryMethod(qm);
        q.setEjbQl(ejbql);
        return q;
    }

    private static String prependAndUpper(String fullName, String prefix) {
        StringBuffer sb = new StringBuffer(fullName);
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        return prefix+sb.toString();
    }

    private boolean isEjbUsed(EjbRelationshipRole role, String ejbName, String fieldName) {
        return role != null &&
               role.getRelationshipRoleSource() != null &&
               ejbName.equals(role.getRelationshipRoleSource().getEjbName()) &&
               fieldName.equals(role.getCmrField().getCmrFieldName());
    }

    private boolean relationContainsField(EjbRelation relation, String ejbName, String fieldName) {
        return
            isEjbUsed(relation.getEjbRelationshipRole(), ejbName, fieldName) ||
               isEjbUsed(relation.getEjbRelationshipRole2(), ejbName, fieldName);
    }

    private void deleteRelationships(String fieldName) {
        String ejbName = model.getEjbName();
        Relationships r = parent.getSingleRelationships();
        if (r != null) {
            EjbRelation[] relations = r.getEjbRelation();
            if (relations != null) {
                for (int i = 0; i < relations.length; i++) {
                    if (relationContainsField(relations[i], ejbName, fieldName)) {
                        boolean uniDirectional = false;
                        EjbRelationshipRole role = relations[i].getEjbRelationshipRole();
                        if (isEjbUsed(role, ejbName, fieldName)) {
                            role.setCmrField(null);
                        } else {
                            uniDirectional = role.getCmrField()==null;
                        }
                        role = relations[i].getEjbRelationshipRole2();
                        if (isEjbUsed(role, ejbName, fieldName)) {
                            role.setCmrField(null);
                        } else {
                            uniDirectional = role.getCmrField()==null;
                        }
                        if (uniDirectional) {
                            r.removeEjbRelation(relations[i]);
                        }
                    }
                }
                if (r.sizeEjbRelation() == 0) {
                    parent.setRelationships(null);
                }
            }
        }
    }

    private List getMethods(String propName) {
        assert propName != null;
        JMIUtils.beginJmiTransaction();
        try {
            List l = new LinkedList();
            JavaClass ce = getBeanClass();
            Method getMethod = getGetterMethod(ce, propName);
            if (getMethod != null) {
                l.add(getMethod);
                Method setMethod = getSetterMethod(ce, propName, getMethod.getType());
                if (setMethod != null) {
                    l.add(setMethod);
                }
            }
            return l;
        } finally {
            JMIUtils.endJmiTransaction();
        }
    }

    public static Method getGetterMethod(JavaClass javaClass, String fieldName) {
        if (javaClass == null || fieldName == null) {
            return null;
        }
        return javaClass.getMethod(getMethodName(fieldName, true), Collections.EMPTY_LIST, false);
    }

    public static Method getSetterMethod(JavaClass classElement, String fieldName, Method getterMethod) {
        return getSetterMethod(classElement, fieldName, getterMethod.getType());
    }

    public Method getGetterMethod(String fieldName, boolean local) {
        JMIUtils.beginJmiTransaction();
        try {
            return getGetterMethod(getBeanInterface(local, true), fieldName);
        } finally {
            JMIUtils.endJmiTransaction();
        }
    }

    public static Method getSetterMethod(JavaClass classElement, String fieldName, Type type) {
        if (classElement == null) {
            return null;
        }
        JMIUtils.beginJmiTransaction();
        try {
            if (type == null) {
                return null;
            }
            type = JMIUtils.resolveType(type.getName());
            String methodName = getMethodName(fieldName, false);
            List parameters = Arrays.asList(new Type[]{type});
            try {
                return classElement.getMethod(methodName, parameters, false);
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                return null;
            }
        } finally {
            JMIUtils.endJmiTransaction();
        }
    }

    public Method getSetterMethod(String fieldName, boolean local) {
        JMIUtils.beginJmiTransaction();
        try {
            Field field = getField(fieldName, true);
            if (field == null) {
                return null;
            } else {
                return getSetterMethod(getBeanInterface(local, true), fieldName, field.getType());
            }
        } finally {
            JMIUtils.endJmiTransaction();
        }
    }

    /**
     * Tries to find finder method for given CMP field
     * @param classElement class to look in
     * @param fieldName field for which we want to get the finder
     * @param getterMethod getter method for field, it is used for detection of field type
     * @return found method which conforms to: findBy<field>, null if method was not found
     */
    public static Method getFinderMethod(JavaClass classElement, String fieldName, Method getterMethod) {
        if (getterMethod == null) {
            return null;
        }
        return classElement .getMethod(prependAndUpper(fieldName, "findBy"),
                Arrays.asList(new Type[]{getterMethod.getType()}), false);
    }

    public boolean supportsMethodType(int mt) {
        return !isSelect(mt) || isCMP();
    }

    private void updateFieldAccessors(String fieldName, boolean localGetter, boolean localSetter, boolean remoteGetter,
            boolean remoteSetter) {
        updateFieldAccessor(fieldName, true, true, localGetter);
        updateFieldAccessor(fieldName, false, true, localSetter);
        updateFieldAccessor(fieldName, true, false, remoteGetter);
        updateFieldAccessor(fieldName, false, false, remoteSetter);
    }

    public void updateFieldAccessor(String fieldName, boolean getter, boolean local, boolean shouldExist) {
        beginWriteJmiTransaction();
        boolean rollback = true;
        try {
            Field field = getField(fieldName, true);
            if (field == null) {
                return;
            }
            JavaClass businessInterface = getBeanInterface(local, true);
            if (businessInterface != null) {
            registerClassForSave(businessInterface);
                Method method;
                if (getter) {
                    method = getGetterMethod(businessInterface, fieldName);
                } else {
                    method = getSetterMethod(businessInterface, fieldName, field.getType());
                }
                if (shouldExist) {
                    if (method == null) {
                        if (getter) {
                            addGetterMethod(businessInterface, field, 0, !local);
                        } else {
                            addSetterMethod(businessInterface, field, 0, !local);
                        }
                    }
                } else if (method != null) {
                    removeMethod(businessInterface, method);
                }
            }
            rollback = false;
        } finally {
            endWriteJmiTransaction(rollback);
        }
    }

    private Field getField(String fieldName, boolean create) {
        JavaClass beanClass = getBeanClass();
        Method getterMethod = getGetterMethod(beanClass, fieldName);
        if (getterMethod == null) {
            if (!create) {
                return null;
            }
            Field field = JMIUtils.createField(beanClass, fieldName, "String"); //NOI18N
            createGetterMethod(getBeanClass(), field, MODIFIERS_PUBLIC_ABSTRACT, false);
            return field;
        } else {
            Type type = getterMethod.getType();
            if (type == null) {
                return null;
            } else {
                return JMIUtils.createField(beanClass, fieldName, type.getName());
            }
        }
    }

}