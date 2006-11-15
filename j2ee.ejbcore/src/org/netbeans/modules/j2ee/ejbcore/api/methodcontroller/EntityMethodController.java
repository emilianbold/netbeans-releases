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

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
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
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
public final class EntityMethodController extends AbstractMethodController {
    
    private final WorkingCopy workingCopy;
    private final Entity model;
    private final EjbJar parent;
    private final Set<Modifier> modifiersPublicAbstract = new HashSet(2);

    public EntityMethodController(WorkingCopy workingCopy, Entity model, EjbJar parent) {
        super(workingCopy, model);
        this.workingCopy = workingCopy;
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
        for (TypeElement clazz : getLocalInterfaces()) {
            ExecutableElement method = getFinderMethod(clazz, fieldName, getGetterMethod(getBeanClass(), fieldName));
            if (method != null) {
                TypeElement declaringClass = (TypeElement) method.getEnclosingElement();
                removeMethodFromClass(declaringClass, method);
            }
        }
        // remove findBy<field> method from remote interfaces (should be only in remote home)
        for (TypeElement clazz : getRemoteInterfaces()) {
            ExecutableElement method = getFinderMethod(clazz, fieldName, getGetterMethod(getBeanClass(), fieldName));
            if (method != null) {
                TypeElement declaringClass = (TypeElement) method.getEnclosingElement();
                removeMethodFromClass(declaringClass, method);
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
            if (queryMethodName.equals(prependAndUpper(fieldName, "findBy"))) {
                return query;
            }
        }
        return null;
    }

    private void removeMethodsFromBean(List<ExecutableElement> methods) {
        for (ExecutableElement method : methods) {
            // remove get/set<field> from local/remote/business interfaces
            if (hasLocal()) {
                ExecutableElement methodToRemove = getInterface(method, true);
                if (methodToRemove != null) {
                    TypeElement declaringClass = (TypeElement) methodToRemove.getEnclosingElement();
                    removeMethodFromClass(declaringClass, methodToRemove);
                }
            }
            if (hasRemote()) {
                ExecutableElement methodToRemove = getInterface(method, false);
                if (methodToRemove != null) {
                    TypeElement declaringClass = (TypeElement) methodToRemove.getEnclosingElement();
                    removeMethodFromClass(declaringClass, methodToRemove);
                }
            }
            // remove get/set<field> from main bean class
            TypeElement declaringClass = (TypeElement) method.getEnclosingElement();
            removeMethodFromClass(declaringClass, method);
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
        List methods = getMethods(field.getCmrFieldName());
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
    
    public boolean hasJavaImplementation(ExecutableElement intfView) {
        return hasJavaImplementation(getMethodTypeFromInterface(intfView));
    }

    public boolean hasJavaImplementation(MethodType methodType) {
        return !(isCMP() && (isFinder(methodType) || isSelect(methodType)));
    }

    public MethodType getMethodTypeFromImpl(ExecutableElement implView) {
        MethodType methodType = null;
        if (implView.getSimpleName().toString().startsWith("ejbCreate") || implView.getSimpleName().toString().startsWith("ejbPostCreate")) { //NOI18N
            methodType = new MethodType.CreateMethodType(ElementHandle.create(implView));
        } else if (!implView.getSimpleName().toString().startsWith("ejb")) { //NOI18N
            methodType = new MethodType.BusinessMethodType(ElementHandle.create(implView));
        } else if (implView.getSimpleName().toString().startsWith("ejbFind")) { //NOI18N
            methodType = new MethodType.FinderMethodType(ElementHandle.create(implView));
        } else if (implView.getSimpleName().toString().startsWith("ejbHome")) { //NOI18N
            methodType = new MethodType.HomeMethodType(ElementHandle.create(implView));
        }
        return methodType;
    }

    public MethodType getMethodTypeFromInterface(ExecutableElement clientView) {
        assert clientView.getEnclosingElement() != null: "declaring class cannot be null";
        String cName = ((TypeElement) clientView.getEnclosingElement()).getQualifiedName().toString();
        MethodType methodType;
        if (cName.equals(model.getLocalHome()) ||
            cName.equals(model.getHome())) {
            if (clientView.getSimpleName().toString().startsWith("create")) { //NOI18N
                methodType = new MethodType.CreateMethodType(ElementHandle.create(clientView));
            } else if (clientView.getSimpleName().toString().startsWith("find")) { //NOI18N
                methodType = new MethodType.FinderMethodType(ElementHandle.create(clientView));
            } else {
                methodType = new MethodType.HomeMethodType(ElementHandle.create(clientView));
            }
        } else {
            methodType = new MethodType.BusinessMethodType(ElementHandle.create(clientView));
        }
        return methodType;
    }

    public AbstractMethodController.GenerateFromImpl createGenerateFromImpl() {
        return new EntityGenerateFromImplVisitor(workingCopy);
    }

    public AbstractMethodController.GenerateFromIntf createGenerateFromIntf() {
        return new EntityGenerateFromIntfVisitor(workingCopy, model);
    }

    public void addSelectMethod(ExecutableElement selectMethod, String ejbql, FileObject ddFileObject) throws IOException {
        addMethodToClass(getBeanClass(), selectMethod);
        addEjbQl(selectMethod,ejbql, ddFileObject);
    }

    public void addEjbQl(ExecutableElement clientView, String ejbql, FileObject ddFileObject) throws IOException {
        if (isBMP()) {
            super.addEjbQl(clientView, ejbql, ddFileObject);
        }
        model.addQuery(buildQuery(clientView, ejbql));
        parent.write(ddFileObject);
    }

    public void addField(VariableElement field, FileObject ddFile, boolean localGetter, boolean localSetter,
        boolean remoteGetter, boolean remoteSetter, String description) throws IOException {
        TypeElement beanClass = getBeanClass();
        addSetterMethod(beanClass, field, modifiersPublicAbstract, false, model);
        addGetterMethod(beanClass, field, modifiersPublicAbstract, false, model);
        final String fieldName = field.getSimpleName().toString();
        updateFieldAccessors(fieldName, localGetter, localSetter, remoteGetter, remoteSetter);
        CmpField cmpField = model.newCmpField();
        cmpField.setFieldName(field.getSimpleName().toString());
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

    private ExecutableElement addSetterMethod(TypeElement javaClass, VariableElement field, Set<Modifier> modifiers, boolean remote, Entity entity) {
        ExecutableElement method = createSetterMethod(javaClass, field, modifiers, remote);
        addMethod(javaClass, method, entity);
        return method;
    }

    private ExecutableElement addGetterMethod(TypeElement javaClass, VariableElement variable, Set<Modifier> modifiers, boolean remote, Entity entity) {
        ExecutableElement method = createGetterMethod(javaClass, variable, modifiers, remote);
        addMethod(javaClass, method, entity);
        return method;
    }

    private void addMethod(TypeElement javaClass, ExecutableElement method, Entity entity) {
        // try to add method as the last CMP field getter/setter in class
        addMethodToClass(javaClass, method);
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

    private ExecutableElement createGetterMethod(TypeElement javaClass, VariableElement field, Set<Modifier> modifiers, boolean remote) {
        final String fieldName = field.getSimpleName().toString();
        ExecutableElement method = createMethod(workingCopy, getMethodName(fieldName, true));
        MethodTree methodTree = null;
        if (remote) {
            TypeElement element = workingCopy.getElements().getTypeElement(RemoteException.class.getName());
            ExpressionTree throwsClause = workingCopy.getTreeMaker().QualIdent(element);
            methodTree = modifyMethod(
                    workingCopy,
                    method, 
                    modifiers, 
                    null, 
                    workingCopy.getTreeMaker().PrimitiveType(TypeKind.VOID), 
                    Collections.singletonList(field.asType()), 
                    Collections.<ExpressionTree>singletonList(throwsClause),
                    null
                    );
        } else {
            methodTree = modifyMethod(
                    workingCopy,
                    method, 
                    modifiers, 
                    null, 
                    workingCopy.getTreeMaker().PrimitiveType(TypeKind.VOID), 
                    Collections.singletonList(field.asType()), 
                    null,
                    null
                    );
        }
        Trees trees = workingCopy.getTrees();
        return (ExecutableElement) trees.getElement(trees.getPath(workingCopy.getCompilationUnit(), methodTree));
    }

    private ExecutableElement createSetterMethod(TypeElement javaClass, VariableElement field, Set<Modifier> modifiers, boolean remote) {
        final String fieldName = field.getSimpleName().toString();
        ExecutableElement method = createMethod(workingCopy, getMethodName(fieldName, false));
        MethodTree methodTree = null;
        if (remote) {
            TypeElement element = workingCopy.getElements().getTypeElement(RemoteException.class.getName());
            ExpressionTree throwsClause = workingCopy.getTreeMaker().QualIdent(element);
            methodTree = modifyMethod(
                    workingCopy,
                    method, 
                    modifiers, 
                    null, 
                    workingCopy.getTreeMaker().PrimitiveType(TypeKind.VOID), 
                    Collections.singletonList(field.asType()), 
                    Collections.<ExpressionTree>singletonList(throwsClause),
                    null
                    );
        } else {
            methodTree = modifyMethod(
                    workingCopy,
                    method, 
                    modifiers, 
                    null, 
                    workingCopy.getTreeMaker().PrimitiveType(TypeKind.VOID), 
                    Collections.singletonList(field.asType()), 
                    null,
                    null
                    );
        }
        Trees trees = workingCopy.getTrees();
        return (ExecutableElement) trees.getElement(trees.getPath(workingCopy.getCompilationUnit(), methodTree));
    }

    private boolean isBMP() {
        return Entity.PERSISTENCE_TYPE_BEAN.equals(model.getPersistenceType());
    }

    public boolean isCMP() {
        return !isBMP();
    }

    /** @deprecated
     */
    private boolean isFinder(MethodType methodType) {
        return methodType instanceof MethodType.FinderMethodType;
    }

    /** @deprecated
     */
    private boolean isSelect(MethodType methodType) {
        return methodType instanceof MethodType.SelectMethodType;
    }

    private boolean isFinder(int methodType) {
        return methodType == MethodType.METHOD_TYPE_FINDER;
    }

    private boolean isSelect(int methodType) {
        return methodType == MethodType.METHOD_TYPE_SELECT;
    }

    public String createDefaultQL(MethodType methodType) {
        String ejbql = null;
        if (isFinder(methodType) && isCMP()) {
            ejbql = "SELECT OBJECT(o) \nFROM " + model.getAbstractSchemaName() + " o";
        }

        if (isSelect(methodType)) {
            ejbql = "SELECT COUNT(o) \nFROM " + model.getAbstractSchemaName() + " o";
        }

        return ejbql;
    }

    private Query buildQuery(ExecutableElement clientView, String ejbql) {
        Query query = model.newQuery();
        QueryMethod queryMethod = query.newQueryMethod();
        queryMethod.setMethodName(clientView.getSimpleName().toString());
        MethodParams mParams = queryMethod.newMethodParams();
        Types types = workingCopy.getTypes();
        for (VariableElement param : clientView.getParameters()) {
            TypeMirror typeMirror = param.asType();
            Element element = types.asElement(typeMirror);
            if (ElementKind.CLASS == element.getKind()) {
                mParams.addMethodParam(((TypeElement) element).getQualifiedName().toString());
            } else {
                mParams.addMethodParam(element.getSimpleName().toString());
            }
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

    private List<ExecutableElement> getMethods(String propName) {
        assert propName != null;
        List<ExecutableElement> resultList = new LinkedList<ExecutableElement>();
        TypeElement ejbClass = getBeanClass();
        ExecutableElement getMethod = getGetterMethod(ejbClass, propName);
        if (getMethod != null) {
            resultList.add(getMethod);
            ExecutableElement setMethod = getSetterMethod(ejbClass, propName, getMethod.getReturnType());
            if (setMethod != null) {
                resultList.add(setMethod);
            }
        }
        return resultList;
    }

    public ExecutableElement getGetterMethod(TypeElement javaClass, String fieldName) {
        if (javaClass == null || fieldName == null) {
            return null;
        }
        ExecutableElement method = createMethod(workingCopy, getMethodName(fieldName, true));
        MethodTree methodTree = modifyMethod(workingCopy, method, null, null, null, Collections.<TypeMirror>emptyList(), null, null);
        Trees trees = workingCopy.getTrees();
        Element element = trees.getElement(trees.getPath(workingCopy.getCompilationUnit(), methodTree));
        return findInClass(javaClass, (ExecutableElement) element);
    }

    public static ExecutableElement getSetterMethod(TypeElement classElement, String fieldName, ExecutableElement getterMethod) {
        return getSetterMethod(classElement, fieldName, getterMethod);
    }

    public ExecutableElement getGetterMethod(String fieldName, boolean local) {
        return getGetterMethod(getBeanInterface(local, true), fieldName);
    }

    public ExecutableElement getSetterMethod(TypeElement classElement, String fieldName, TypeMirror type) {
        if (classElement == null) {
            return null;
        }
        if (type == null) {
            return null;
        }
        List<TypeMirror> parameters = Collections.singletonList(type);
        ExecutableElement method = createMethod(workingCopy, getMethodName(fieldName, true));
        MethodTree methodTree = modifyMethod(workingCopy, method, null, null, null, parameters, null, null);
        Trees trees = workingCopy.getTrees();
        Element element = trees.getElement(trees.getPath(workingCopy.getCompilationUnit(), methodTree));
        return findInClass(classElement, (ExecutableElement) element);
    }

    public ExecutableElement getSetterMethod(String fieldName, boolean local) {
        VariableElement field = getField(fieldName, true);
        if (field == null) {
            return null;
        } else {
            return getSetterMethod(getBeanInterface(local, true), fieldName, field.asType());
        }
    }

    /**
     * Tries to find finder method for given CMP field
     * @param classElement class to look in
     * @param fieldName field for which we want to get the finder
     * @param getterMethod getter method for field, it is used for detection of field type
     * @return found method which conforms to: findBy<field>, null if method was not found
     */
    public ExecutableElement getFinderMethod(TypeElement classElement, String fieldName, ExecutableElement getterMethod) {
        if (getterMethod == null) {
            return null;
        }
        List<TypeMirror> params = Collections.singletonList(getterMethod.getReturnType());
        ExecutableElement method = createMethod(workingCopy, getMethodName(fieldName, true));
        MethodTree methodTree = modifyMethod(workingCopy, method, null, null, null, params, null, null);
        Trees trees = workingCopy.getTrees();
        Element element = trees.getElement(trees.getPath(workingCopy.getCompilationUnit(), methodTree));
        return findInClass(classElement, (ExecutableElement) element);
    }

    public boolean supportsMethodType(int methodType) {
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
        VariableElement field = getField(fieldName, true);
        if (field == null) {
            return;
        }
        TypeElement businessInterface = getBeanInterface(local, true);
        if (businessInterface != null) {
            ExecutableElement method;
            if (getter) {
                method = getGetterMethod(businessInterface, fieldName);
            } else {
                method = getSetterMethod(businessInterface, fieldName, field.asType());
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
                removeMethodFromClass(businessInterface, method);
            }
        }
    }

    private VariableElement getField(String fieldName, boolean create) {
        TypeElement beanClass = getBeanClass();
        ExecutableElement getterMethod = getGetterMethod(beanClass, fieldName);
        TreeMaker treeMaker = workingCopy.getTreeMaker();
        Trees trees = workingCopy.getTrees();
        if (getterMethod == null) {
            if (!create) {
                return null;
            }
            VariableTree fieldTree = treeMaker.Variable(
                    treeMaker.Modifiers(Collections.<Modifier>emptySet()),
                    fieldName,
                    trees.getTree(workingCopy.getElements().getTypeElement(String.class.getName())),
                    null
                    );
            VariableElement field = (VariableElement) trees.getElement(trees.getPath(workingCopy.getCompilationUnit(), fieldTree));
            createGetterMethod(getBeanClass(), field, modifiersPublicAbstract, false);
            return field;
        } else {
            TypeMirror type = getterMethod.getReturnType();
            if (type == null) {
                return null;
            } else {
                VariableTree fieldTree = treeMaker.Variable(
                        treeMaker.Modifiers(Collections.<Modifier>emptySet()),
                        fieldName,
                        treeMaker.Type(type),
                        null
                        );
                return (VariableElement) trees.getElement(trees.getPath(workingCopy.getCompilationUnit(), fieldTree));
            }
        }
    }

}
