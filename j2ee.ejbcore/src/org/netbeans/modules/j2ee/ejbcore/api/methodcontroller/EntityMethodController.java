/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.ejbcore.api.methodcontroller;

import org.netbeans.modules.j2ee.common.method.MethodModel;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
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
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
public final class EntityMethodController extends AbstractMethodController {
    
    private final FileObject ejbClassFO;
    private final Entity model;
    private final EjbJar parent;
    private final Set<Modifier> modifiersPublicAbstract = new HashSet<Modifier>(2);

    public EntityMethodController(FileObject ejbClassFO, Entity model, EjbJar parent) {
        super(ejbClassFO, model);
        this.ejbClassFO= ejbClassFO;
        this.model = model;
        this.parent = parent;
        modifiersPublicAbstract.add(Modifier.PUBLIC);
        modifiersPublicAbstract.add(Modifier.ABSTRACT);
    }

    public List getMethods(CmpField field) {
        return getMethods(field.getFieldName());
    }

    public List getMethods(CmrField field) {
        return getMethods(field.getCmrFieldName());
    }

    public void deleteQueryMapping(ExecutableElement method, FileObject ddFileObject) throws IOException {
        Query[] queries = model.getQuery();
        for (Query query : queries) {
            String methodName = query.getQueryMethod().getMethodName();
            if (method.getSimpleName().contentEquals(methodName)) {
                model.removeQuery(query);
                parent.write(ddFileObject);
                return;
            }
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
    public void deleteField(CmpField field, FileObject ddFileObject) throws IOException {
        Query query = null;
        String fieldName = field.getFieldName();
        // find findBy<field> query in DD
        query = findQueryByCmpField(fieldName);
        // remove findBy<field> method from local interfaces (should be only in local home)
        for (String clazz : getLocalInterfaces()) {
            MethodModel method = getFinderMethod(clazz, fieldName, getGetterMethod(getBeanClass(), fieldName));
            if (method != null) {
                removeMethodFromClass(clazz, method);
            }
        }
        // remove findBy<field> method from remote interfaces (should be only in remote home)
        for (String clazz : getRemoteInterfaces()) {
            MethodModel method = getFinderMethod(clazz, fieldName, getGetterMethod(getBeanClass(), fieldName));
            if (method != null) {
                removeMethodFromClass(clazz, method);
            }
        }
        removeMethodsFromBean(getMethods(fieldName));
        updateFieldAccessors(fieldName, false, false, false, false);
        // remove findBy<field> query from DD
        if (query != null) {
            model.removeQuery(query);
        }
        // remove CMP field from DD
        model.removeCmpField(field);
        parent.write(ddFileObject);
    }

    private Query findQueryByCmpField(String fieldName) {
        Query[] queries = model.getQuery();
        for (Query query : queries) {
            String queryMethodName = query.getQueryMethod().getMethodName();
            if (prependAndUpper(fieldName, "findBy").equals(queryMethodName)) {
                return query;
            }
        }
        return null;
    }

    private void removeMethodsFromBean(List<MethodModel> methods) throws IOException {
        for (MethodModel method : methods) {
            // remove get/set<field> from local/remote/business interfaces
            if (hasLocal()) {
                ClassMethodPair classMethodPair = getInterface(method, true);
                if (classMethodPair != null) {
                    removeMethodFromClass(classMethodPair.getClassName(), classMethodPair.getMethodModel());
                }
            }
            if (hasRemote()) {
                ClassMethodPair classMethodPair = getInterface(method, false);
                if (classMethodPair != null) {
                    removeMethodFromClass(classMethodPair.getClassName(), classMethodPair.getMethodModel());
                }
            }
            // remove get/set<field> from main bean class
            removeMethodFromClass(getBeanClass(), method);
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
    public void deleteField(CmrField field, FileObject ddFileObject) throws IOException {
        List<MethodModel> methods = getMethods(field.getCmrFieldName());
        removeMethodsFromBean(methods);
        // remove relation from DD
        deleteRelationships(field.getCmrFieldName());
        parent.write(ddFileObject);
    }

    public static String getMethodName(String fieldName, boolean get) {
        String prefix = get ? "get" : "set"; //NOI18N;
        return prependAndUpper(fieldName, prefix);
    }

//    private static String getFieldName(String methodName) {
//        if (methodName.length() < 3) {
//            return null;
//        }
//        String prefix = methodName.substring(0, 3);
//        if (prefix.equals("set") || prefix.equals("get")) {
//            return lower(methodName.substring(3, methodName.length()));
//        }
//        return null;
//    }
    
    @Override
    public boolean hasJavaImplementation(MethodModel intfView) {
        return hasJavaImplementation(getMethodTypeFromInterface(intfView));
    }

    @Override
    public boolean hasJavaImplementation(MethodType methodType) {
        return !(isCMP() && (isFinder(methodType.getKind()) || isSelect(methodType.getKind())));
    }

    @Override
    public MethodType getMethodTypeFromImpl(MethodModel implView) {
        MethodType methodType = null;
        if (implView.getName().startsWith("ejbCreate") || implView.getName().startsWith("ejbPostCreate")) { //NOI18N
            methodType = new MethodType.CreateMethodType(implView);
        } else if (!implView.getName().startsWith("ejb")) { //NOI18N
            methodType = new MethodType.BusinessMethodType(implView);
        } else if (implView.getName().startsWith("ejbFind")) { //NOI18N
            methodType = new MethodType.FinderMethodType(implView);
        } else if (implView.getName().startsWith("ejbHome")) { //NOI18N
            methodType = new MethodType.HomeMethodType(implView);
        }
        return methodType;
    }

    @Override
    public MethodType getMethodTypeFromInterface(MethodModel clientView) {
        MethodType methodType;
        if (findInClass(model.getLocalHome(), clientView) || findInClass(model.getHome(), clientView)) {
            if (clientView.getName().startsWith("create")) { //NOI18N
                methodType = new MethodType.CreateMethodType(clientView);
            } else if (clientView.getName().startsWith("find")) { //NOI18N
                methodType = new MethodType.FinderMethodType(clientView);
            } else {
                methodType = new MethodType.HomeMethodType(clientView);
            }
        } else {
            methodType = new MethodType.BusinessMethodType(clientView);
        }
        return methodType;
    }

    public AbstractMethodController.GenerateFromImpl createGenerateFromImpl() {
        return new EntityGenerateFromImplVisitor();
    }

    public AbstractMethodController.GenerateFromIntf createGenerateFromIntf() {
        return new EntityGenerateFromIntfVisitor(ejbClassFO, model);
    }

    public void addSelectMethod(MethodModel selectMethod, String ejbql, FileObject ddFileObject) throws IOException {
        addMethodToClass(getBeanClass(), selectMethod);
        addEjbQl(selectMethod, ejbql, ddFileObject);
    }

    public void addEjbQl(MethodModel clientView, String ejbql, FileObject ddFileObject) throws IOException {
        if (isBMP()) {
            super.addEjbQl(clientView, ejbql, ddFileObject);
        }
        model.addQuery(buildQuery(clientView, ejbql));
        parent.write(ddFileObject);
    }

    public void addField(MethodModel.Variable field, FileObject ddFile, boolean localGetter, boolean localSetter,
        boolean remoteGetter, boolean remoteSetter, String description) throws IOException {
        String beanClass = getBeanClass();
        addSetterMethod(beanClass, field, modifiersPublicAbstract, false, model);
        addGetterMethod(beanClass, field, modifiersPublicAbstract, false, model);
        final String fieldName = field.getName();
        updateFieldAccessors(fieldName, localGetter, localSetter, remoteGetter, remoteSetter);
        CmpField cmpField = model.newCmpField();
        cmpField.setFieldName(field.getName());
        cmpField.setDescription(description);
        model.addCmpField(cmpField);
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

    private MethodModel addSetterMethod(String javaClass, MethodModel.Variable field, Set<Modifier> modifiers, boolean remote, Entity entity) {
        MethodModel method = createSetterMethod(javaClass, field, modifiers, remote);
        addMethod(javaClass, method, entity);
        return method;
    }

    private MethodModel addGetterMethod(String javaClass, MethodModel.Variable variable, Set<Modifier> modifiers, boolean remote, Entity entity) {
        MethodModel method = createGetterMethod(javaClass, variable, modifiers, remote);
        addMethod(javaClass, method, entity);
        return method;
    }

    private void addMethod(String javaClass, MethodModel method, Entity entity) {
        // try to add method as the last CMP field getter/setter in class
        try {
            addMethodToClass(javaClass, method);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }

        //TODO: RETOUCHE insert into specified position
//        List cmpFields = new ArrayList();
//        CmpField[] cmpFieldArray = e.getCmpField();
//        for (int i = 0; i < cmpFieldArray.length; i++) {
//            cmpFields.add(cmpFieldArray[i].getFieldName());
//        }
//        int index = -1;
//        for (Iterator it = javaClass.getContents().iterator(); it.hasNext();) {
//            Object elem = (Object) it.next();
//            if (elem instanceof Method) {
//                String fieldName = getFieldName(((Method) elem).getName());
//                if (cmpFields.contains(fieldName)) {
//                    index = javaClass.getContents().indexOf(elem);
//
//                }
//            }
//        }
//        if (index != -1) {
//            javaClass.getContents().add(index + 1, method);
//        } else {
//            javaClass.getContents().add(method);
//        }
    }

    private MethodModel createGetterMethod(String javaClass, MethodModel.Variable field, Set<Modifier> modifiers, boolean remote) {
        final String fieldName = field.getName();
        List<String> exceptions = remote ? Collections.<String>singletonList(RemoteException.class.getName()) : Collections.<String>emptyList();
        MethodModel method = MethodModel.create(
                getMethodName(fieldName, true),
                "void",
                "",
                Collections.singletonList(field),
                exceptions,
                modifiers
                );
        return method;
    }

    private MethodModel createSetterMethod(String javaClass, MethodModel.Variable field, Set<Modifier> modifiers, boolean remote) {
        final String fieldName = field.getName();
        List<String> exceptions = remote ? Collections.<String>singletonList(RemoteException.class.getName()) : Collections.<String>emptyList();
        MethodModel method = MethodModel.create(
                getMethodName(fieldName, false),
                "void",
                "",
                Collections.singletonList(field),
                exceptions,
                modifiers
                );
        return method;
    }

    private boolean isBMP() {
        return Entity.PERSISTENCE_TYPE_BEAN.equals(model.getPersistenceType());
    }

    public boolean isCMP() {
        return !isBMP();
    }

    private boolean isFinder(MethodType.Kind methodType) {
        return methodType == MethodType.Kind.FINDER;
    }

    private boolean isSelect(MethodType.Kind methodType) {
        return methodType == MethodType.Kind.SELECT;
    }

    public String createDefaultQL(MethodType methodType) {
        String ejbql = null;
        if (isFinder(methodType.getKind()) && isCMP()) {
            ejbql = "SELECT OBJECT(o) \nFROM " + model.getAbstractSchemaName() + " o";
        }

        if (isSelect(methodType.getKind())) {
            ejbql = "SELECT COUNT(o) \nFROM " + model.getAbstractSchemaName() + " o";
        }

        return ejbql;
    }

    private Query buildQuery(MethodModel clientView, String ejbql) {
        Query query = model.newQuery();
        QueryMethod queryMethod = query.newQueryMethod();
        queryMethod.setMethodName(clientView.getName());
        MethodParams mParams = queryMethod.newMethodParams();
        for (MethodModel.Variable parameter : clientView.getParameters()) {
            mParams.addMethodParam(parameter.getType());
        }
        queryMethod.setMethodParams(mParams);
        query.setQueryMethod(queryMethod);
        query.setEjbQl(ejbql);
        return query;
    }

    private static String prependAndUpper(String fullName, String prefix) {
        StringBuffer buffer = new StringBuffer(fullName);
        buffer.setCharAt(0, Character.toUpperCase(buffer.charAt(0)));
        return prefix+buffer.toString();
    }

//    private static String lower(String fullName) {
//        StringBuffer buffer = new StringBuffer(fullName);
//        buffer.setCharAt(0, Character.toLowerCase(buffer.charAt(0)));
//        return buffer.toString();
//    }

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
        Relationships relationships = parent.getSingleRelationships();
        if (relationships != null) {
            EjbRelation[] relations = relationships.getEjbRelation();
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
                            relationships.removeEjbRelation(relations[i]);
                        }
                    }
                }
                if (relationships.sizeEjbRelation() == 0) {
                    parent.setRelationships(null);
                }
            }
        }
    }

    private List<MethodModel> getMethods(String propName) {
        assert propName != null;
        List<MethodModel> resultList = new LinkedList<MethodModel>();
        String ejbClass = getBeanClass();
        MethodModel getMethod = getGetterMethod(ejbClass, propName);
        if (getMethod != null) {
            resultList.add(getMethod);
            MethodModel setMethod = getSetterMethod(ejbClass, propName, getMethod.getReturnType());
            if (setMethod != null) {
                resultList.add(setMethod);
            }
        }
        return resultList;
    }

    public MethodModel getGetterMethod(String javaClass, String fieldName) {
        if (javaClass == null || fieldName == null) {
            return null;
        }
        MethodModel method =  MethodModel.create(
                getMethodName(fieldName, true),
                "void",
                "",
                Collections.<MethodModel.Variable>emptyList(),
                Collections.<String>emptyList(),
                Collections.<Modifier>emptySet()
                );
        return findInClass(javaClass, method) ? method : null;
    }

//    public static MethodModel getSetterMethod(TypeElement classElement, String fieldName, MethodModel getterMethod) {
//        return getSetterMethod(classElement, fieldName, getterMethod.getReturnType());
//    }

    public MethodModel getGetterMethod(String fieldName, boolean local) {
        return getGetterMethod(getBeanInterface(local, true), fieldName);
    }

    public MethodModel getSetterMethod(String classElement, String fieldName, String type) {
        if (classElement == null) {
            return null;
        }
        if (type == null) {
            return null;
        }
        MethodModel method = MethodModel.create(
                getMethodName(fieldName, true), 
                "void",
                "",
                Collections.singletonList(MethodModel.Variable.create(type, "arg0")),
                Collections.<String>emptyList(),
                Collections.<Modifier>emptySet()
                );
        return findInClass(classElement, method) ? method : null;
    }

    public MethodModel getSetterMethod(String fieldName, boolean local) {
        MethodModel.Variable field = getField(fieldName, true);
        if (field == null) {
            return null;
        } else {
            return getSetterMethod(getBeanInterface(local, true), fieldName, field.getType());
        }
    }

    /**
     * Tries to find finder method for given CMP field
     * @param classElement class to look in
     * @param fieldName field for which we want to get the finder
     * @param getterMethod getter method for field, it is used for detection of field type
     * @return found method which conforms to: findBy<field>, null if method was not found
     */
    public MethodModel getFinderMethod(String classElement, String fieldName, MethodModel getterMethod) {
        if (getterMethod == null) {
            return null;
        }
        MethodModel method = MethodModel.create(
                getMethodName(fieldName, true),
                "void",
                "",
                Collections.singletonList(MethodModel.Variable.create(getterMethod.getReturnType(), "arg0")),
                Collections.<String>emptyList(),
                Collections.<Modifier>emptySet()
                );
        return findInClass(classElement, method) ? method : null;
    }

    @Override
    public boolean supportsMethodType(MethodType.Kind methodType) {
        return !isSelect(methodType) || isCMP();
    }

    private void updateFieldAccessors(String fieldName, boolean localGetter, boolean localSetter, boolean remoteGetter,
            boolean remoteSetter) {
        updateFieldAccessor(fieldName, true, true, localGetter);
        updateFieldAccessor(fieldName, false, true, localSetter);
        updateFieldAccessor(fieldName, true, false, remoteGetter);
        updateFieldAccessor(fieldName, false, false, remoteSetter);
    }

    public void updateFieldAccessor(String fieldName, boolean getter, boolean local, boolean shouldExist) {
        MethodModel.Variable field = getField(fieldName, true);
        if (field == null) {
            return;
        }
        String businessInterface = getBeanInterface(local, true);
        if (businessInterface != null) {
            MethodModel method;
            if (getter) {
                method = getGetterMethod(businessInterface, fieldName);
            } else {
                method = getSetterMethod(businessInterface, fieldName, field.getType());
            }
            if (shouldExist) {
                if (method == null) {
                    if (getter) {
                        addGetterMethod(businessInterface, field, Collections.<Modifier>emptySet(), !local, model);
                    } else {
                        addSetterMethod(businessInterface, field, Collections.<Modifier>emptySet(), !local, model);
                    }
                }
            } else if (method != null) {
                try {
                    removeMethodFromClass(businessInterface, method);
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(e);
                }

            }
        }
    }

    private MethodModel.Variable getField(String fieldName, boolean create) {
        String beanClass = getBeanClass();
        MethodModel getterMethod = getGetterMethod(beanClass, fieldName);
        if (getterMethod == null) {
            if (!create) {
                return null;
            }
            MethodModel.Variable field = MethodModel.Variable.create(String.class.getName(), fieldName);
            createGetterMethod(getBeanClass(), field, modifiersPublicAbstract, false);
            return field;
        } else {
            String type = getterMethod.getReturnType();
            if (type == null) {
                return null;
            } else {
                return MethodModel.Variable.create(type, fieldName);
            }
        }
    }

}
