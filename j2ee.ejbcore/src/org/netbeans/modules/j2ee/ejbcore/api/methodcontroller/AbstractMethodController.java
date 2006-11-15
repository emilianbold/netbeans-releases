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
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
public abstract class AbstractMethodController extends EjbMethodController {
    
    private final WorkingCopy workingCopy;
    private final EntityAndSession model;
    protected Set classesForSave;
    private final boolean simplified;
    
    public AbstractMethodController(WorkingCopy workingCopy, EntityAndSession model) {
        this.workingCopy = workingCopy;
        this.model = model;
        this.simplified = model.getRoot().getVersion().doubleValue() > 2.1;
    }
    
    public interface GenerateFromImpl {
        void getInterfaceMethodFromImpl(MethodType methodType, TypeElement homeClass, TypeElement componentClass);
        TypeElement getDestinationInterface();
        ExecutableElement getInterfaceMethod();
    }
    
    public interface GenerateFromIntf {
        void getInterfaceMethodFromImpl(MethodType methodType);
        ExecutableElement getImplMethod();
        ExecutableElement getSecondaryMethod();
    }
    
    public abstract GenerateFromImpl createGenerateFromImpl();
    public abstract GenerateFromIntf createGenerateFromIntf();
    
    public final void createAndAdd(ExecutableElement clientView, boolean local, boolean isComponent) {
        TypeElement home = null;
        TypeElement component = null;
        ExecutableElement methodToOpen = null;
        if (local) {
            home = workingCopy.getElements().getTypeElement(model.getLocalHome());
            component = businessInterface(model.getLocal());
        } else {
            home = workingCopy.getElements().getTypeElement(model.getHome());
            component = businessInterface(model.getRemote());
        }
        if (isComponent) {
            addMethodToClass(component, clientView);
        } else {
            addMethodToClass(home, clientView);
        }
        TypeElement ejbClass = workingCopy.getElements().getTypeElement(model.getEjbClass());
        if (hasJavaImplementation(clientView)) {
            for (ExecutableElement me : getImplementationMethods(clientView)) {
                if (findInClass(ejbClass, me) == null) {
                    addMethodToClass(ejbClass, me);
                    methodToOpen = me;
                }
            }
        }
        if (!local && !simplified) {
            addExceptionIfNecessary(clientView, RemoteException.class.getName());
        }
        if (methodToOpen != null) {
            //TODO: RETOUCHE opening generated method in editor
//            StatementBlock stBlock = null;
//            if (methodToOpen.isValid())
//                stBlock = methodToOpen.getBody();
//            if (stBlock != null)
//                JMIUtils.openInEditor(stBlock);
        }
    }
    
    public final void createAndAddInterface(ExecutableElement beanImpl, boolean local) {
        MethodType methodType = getMethodTypeFromImpl(beanImpl);
        GenerateFromImpl generateFromImpl = createGenerateFromImpl();
        TypeElement home = null;
        TypeElement component = null;
        if (local) {
            home = workingCopy.getElements().getTypeElement(model.getLocalHome());
            component = businessInterface(model.getLocal());
        } else {
            home = workingCopy.getElements().getTypeElement(model.getHome());
            component = businessInterface(model.getRemote());
        }
        generateFromImpl.getInterfaceMethodFromImpl(methodType, home, component);
        ExecutableElement method = generateFromImpl.getInterfaceMethod();
        if (!local && !simplified) {
            addExceptionIfNecessary(method, RemoteException.class.getName());
        }
        modifyMethod(workingCopy, method, Collections.<Modifier>emptySet(), null, null, null, null, null);
        TypeElement destinationInterface = generateFromImpl.getDestinationInterface();
        addMethodToClass(destinationInterface, method);
    }
    
    public final void createAndAddImpl(ExecutableElement intfView) {
        MethodType methodType = getMethodTypeFromInterface(intfView);
        GenerateFromIntf generateFromIntf = createGenerateFromIntf();
        generateFromIntf.getInterfaceMethodFromImpl(methodType);
        ExecutableElement method = generateFromIntf.getImplMethod();
        TypeElement ejbClass = workingCopy.getElements().getTypeElement(model.getEjbClass());
        addMethodToClass(ejbClass, method);
    }
    
    private List<ExecutableElement> getImplementationMethods(ExecutableElement intfView) {
        MethodType methodType = getMethodTypeFromInterface(intfView);
        GenerateFromIntf generateFromIntf = createGenerateFromIntf();
        generateFromIntf.getInterfaceMethodFromImpl(methodType);
        ExecutableElement primary = generateFromIntf.getImplMethod();
        ExecutableElement secondary = generateFromIntf.getSecondaryMethod();
        List<ExecutableElement> methods = null;
        if (secondary != null) {
            methods = Arrays.asList(new ExecutableElement[] {primary,secondary});
        } else {
            methods = Collections.singletonList(primary);
        }
        return methods;
    }
    
    public final List<ExecutableElement> getImplementation(ExecutableElement intfView) {
        List<ExecutableElement> methods = getImplementationMethods(intfView);
        List<ExecutableElement> result = new ArrayList<ExecutableElement>(methods.size());
        for (ExecutableElement method : methods) {
            result.add(findInClass(getBeanClass(), method));
        }
        return result;
    }
    
    public final ExecutableElement getInterface(ExecutableElement beanImpl, boolean local) {
        MethodType methodType = getMethodTypeFromImpl(beanImpl);
        assert methodType != null: "method cannot be used in interface";
        GenerateFromImpl generateFromImpl = createGenerateFromImpl();
        TypeElement home = null;
        TypeElement component = null;
        if (local) {
            home = workingCopy.getElements().getTypeElement(model.getLocalHome());
            component = businessInterface(model.getLocal());
        } else {
            home = workingCopy.getElements().getTypeElement(model.getHome());
            component = businessInterface(model.getRemote());
        }
        generateFromImpl.getInterfaceMethodFromImpl(methodType,home,component);
        return findInClass(generateFromImpl.getDestinationInterface(), generateFromImpl.getInterfaceMethod());
    }
    
    
    /** Performs the check if the method is defined in apporpriate interface
     * @return false if the interface is found but does not contain matching method.
     */
    public boolean hasMethodInInterface(ExecutableElement method, MethodType methodType, boolean local) {
        TypeElement intf = null;
        ExecutableElement wantedMethod = createMethodCopy(workingCopy, method);
        if (methodType.getKind() == MethodType.Kind.BUSINESS) {
            intf = findBusinessInterface(local ? model.getLocal() : model.getRemote());
        } else if (methodType.getKind() == MethodType.Kind.CREATE) {
            String name = chopAndUpper(method.getSimpleName().toString(), "ejb"); //NOI18N
            TypeElement type = workingCopy.getElements().getTypeElement(local ? model.getLocal() : model.getRemote());
            modifyMethod(workingCopy, wantedMethod, null, name, workingCopy.getTrees().getTree(type), null, null, null);
            intf = workingCopy.getElements().getTypeElement(local ? model.getLocalHome() : model.getHome());
        }
        if (wantedMethod.getSimpleName() == null || intf == null || wantedMethod.getReturnType() == null) {
            return true;
        }
        if (findInClass(intf, wantedMethod) != null) {
            return true;
        }
        return false;
    }
    
    private String chopAndUpper(String fullName, String chop) {
        StringBuffer stringBuffer = new StringBuffer(fullName);
        stringBuffer.delete(0, chop.length());
        stringBuffer.setCharAt(0, Character.toLowerCase(stringBuffer.charAt(0)));
        return stringBuffer.toString();
    }
    
    private void addExceptionIfNecessary(ExecutableElement method, String exceptionName) {
        TypeElement exceptionTypeElement = workingCopy.getElements().getTypeElement(exceptionName);
        List<? extends TypeMirror> exceptionsTypeMirrors = method.getThrownTypes();
        if (!containsTypeMirror(exceptionsTypeMirrors, exceptionTypeElement.asType())) {
            MethodTree methodTree = workingCopy.getTrees().getTree(method);
            TreeMaker treeMaker = workingCopy.getTreeMaker();
            ExpressionTree expressionTree = treeMaker.QualIdent(exceptionTypeElement);
            treeMaker.addMethodThrows(methodTree, expressionTree);
        }
    }
    
    private TypeElement businessInterface(String compInterfaceName) {
        TypeElement compInterface = workingCopy.getElements().getTypeElement(compInterfaceName);
        TypeElement beanClass = workingCopy.getElements().getTypeElement(model.getEjbClass());
        if (compInterface == null || beanClass == null) {
            return null;
        }
        // get bean interfaces
        List<? extends TypeMirror> beanInterfaces = beanClass.getInterfaces();
        // get method interfaces
        List<? extends TypeMirror> compInterfaces = compInterface.getInterfaces();
        // look for common candidates
        compInterfaces.retainAll(beanInterfaces);
        if (compInterfaces.isEmpty()) {
            return compInterface;
        }
        TypeMirror typeMirror = compInterfaces.get(0);
        TypeElement business = null;
        if (TypeKind.DECLARED == typeMirror.getKind()) {
            DeclaredType declaredType = (DeclaredType) typeMirror;
            Element element = declaredType.asElement();
            if (ElementKind.CLASS == element.getKind()) {
                business = (TypeElement) element;
            }
        }
        return business == null ? compInterface : business;
    }
    
    private TypeElement findBusinessInterface(String compInterfaceName) {
        TypeElement compInterface = workingCopy.getElements().getTypeElement(compInterfaceName);
        TypeElement beanClass = workingCopy.getElements().getTypeElement(model.getEjbClass());
        if (compInterface == null || beanClass == null) {
            return null;
        }
        // get bean interfaces
        List<? extends TypeMirror> beanInterfaces = beanClass.getInterfaces();
        // get method interfaces
        List<? extends TypeMirror> compInterfaces = compInterface.getInterfaces();
        // look for common candidates
        TypeElement business = null;
        for (TypeMirror typeMirror : compInterfaces) {
            if (beanInterfaces.contains(typeMirror)) {
                if (TypeKind.DECLARED == typeMirror.getKind()) {
                    DeclaredType declaredType = (DeclaredType) typeMirror;
                    Element element = declaredType.asElement();
                    if (ElementKind.CLASS == element.getKind()) {
                        business = (TypeElement) element;
                    }
                }
                break;
            }
        }
        return business == null ? compInterface : business;
    }
    
    public final TypeElement getBeanClass() {
        return model.getEjbClass() == null ? null : workingCopy.getElements().getTypeElement(model.getEjbClass());
    }
    
    public final List<TypeElement> getLocalInterfaces() {
        if (!hasLocal()) {
            return Collections.<TypeElement>emptyList();
        }
        List<TypeElement> resultList = new ArrayList<TypeElement>(2);
        if (model.getLocalHome() != null) {
            resultList.add(workingCopy.getElements().getTypeElement(model.getLocalHome()));
        }
        if (model.getLocal() != null) {
            resultList.add(businessInterface(model.getLocal()));
        }
        
        return resultList;
    }
    
    public final List<TypeElement> getRemoteInterfaces() {
        if (!hasRemote()) {
            return Collections.<TypeElement>emptyList();
        }
        List<TypeElement> resultList = new ArrayList<TypeElement>(2);
        if (model.getHome() != null) {
            resultList.add(workingCopy.getElements().getTypeElement(model.getHome()));
        }
        if (model.getRemote() != null) {
            resultList.add(businessInterface(model.getRemote()));
        }
        return resultList;
    }
    
    public final void delete(ExecutableElement interfaceMethod, boolean local) {
        List<ExecutableElement> impls = getImplementation(interfaceMethod);
        boolean checkOther = local?hasRemote():hasLocal();
        if (!impls.isEmpty()) {
            for (ExecutableElement impl : impls) {
                if (impl != null) { // could be null here if the method is missing
                    if (((checkOther && getInterface(impl, !local) == null)) ||
                            !checkOther) {
                        TypeElement typeElement = (TypeElement) impl.getEnclosingElement();
                        removeMethodFromClass(typeElement, impl);
                    }
                }
            }
            TypeElement typeElement = (TypeElement) interfaceMethod.getEnclosingElement();
            removeMethodFromClass(typeElement, interfaceMethod);
        }
    }
    
    public boolean hasRemote() {
        String intf = model.getHome();
        if (!simplified) {
            if (intf == null || workingCopy.getElements().getTypeElement(intf) == null) {
                return false;
            }
        }
        intf = model.getRemote();
        if (intf == null || findBusinessInterface(intf) == null) {
            return false;
        }
        return true;
    }
    
    public boolean hasLocal() {
        String intf = model.getLocalHome();
        if (!simplified) {
            if (intf == null || workingCopy.getElements().getTypeElement(intf) == null) {
                return false;
            }
        }
        intf = model.getLocal();
        if (intf == null || findBusinessInterface(intf) == null) {
            return false;
        }
        return true;
    }
    
    public ExecutableElement getPrimaryImplementation(ExecutableElement intfView) {
        List<ExecutableElement> impls = getImplementation(intfView);
        return impls.isEmpty() ? null : impls.get(0);
    }
    
    public String getRemote() {
        return model.getRemote();
    }
    
    public String getLocal() {
        return model.getLocal();
    }
    
    public final void addMethod(ExecutableElement method, boolean local, boolean isComponent) {
        TypeElement javaClass = getBeanInterface(local, isComponent);
        assert javaClass != null;
        addMethodToClass(javaClass, method);
        if (!local) {
            addExceptionIfNecessary(method, RemoteException.class.getName());
        }
        createBeanMethod(method);
    }
    
    public TypeElement getBeanInterface(boolean local, boolean isComponent) {
        if (isComponent) {
            return businessInterface(local ? model.getLocal() : model.getRemote());
        } else {
            String className = local ? model.getLocalHome() : model.getHome();
            return workingCopy.getElements().getTypeElement(className);
        }
    }
    
    private void createBeanMethod(ExecutableElement method) {
        TypeElement beanClass = workingCopy.getElements().getTypeElement(model.getEjbClass());
        if (hasJavaImplementation(method)) {
            List<ExecutableElement> implMethods = getImplementationMethods(method);
            for (ExecutableElement me : implMethods) {
                if (findInClass(beanClass, me) == null) {
                    addMethodToClass(beanClass, method);
                }
            }
        }
    }
    
    public final void removeMethod(ExecutableElement method, boolean local, boolean isComponent) {
        TypeElement clazz = getBeanInterface(local, isComponent);
        assert clazz != null;
        if (!local) {
            addExceptionIfNecessary(method, RemoteException.class.getName());
        }
        removeMethodFromClass(clazz, method);
        createBeanMethod(method);
    }
    
    public final void updateMethod(ExecutableElement method, boolean local, boolean isComponent, boolean shouldExist) {
        TypeElement javaClass = getBeanInterface(local, isComponent);
        assert javaClass != null;
        ExecutableElement foundMethod = findInClass(javaClass, method);
        if (shouldExist) {
            if (foundMethod == null) {
                if (!local) {
                    addExceptionIfNecessary(method, RemoteException.class.getName());
                }
                addMethodToClass(javaClass, method);
            }
        } else {
            if (foundMethod != null) {
                Trees trees = workingCopy.getTrees();
                ClassTree classTree = trees.getTree(javaClass);
                MethodTree methodTree = trees.getTree(method);
                workingCopy.getTreeMaker().removeClassMember(classTree, methodTree);
            }
        }
    }
    
    // util candidates
    // -------------------------------------------------------------------------
    
    protected ExecutableElement findInClass(TypeElement clazz, ExecutableElement method) {
        ExecutableType methodType = (ExecutableType) method.asType();
        Types types = workingCopy.getTypes();
        for (ExecutableElement m : ElementFilter.methodsIn(clazz.getEnclosedElements())) {
            if (types.isSubsignature((ExecutableType) m.asType(), methodType)) {
                return m;
            }
        }
        return null;
    }
    
    protected void addMethodToClass(TypeElement clazz, ExecutableElement method) {
        Trees trees = workingCopy.getTrees();
        ClassTree classTree = trees.getTree(clazz);
        MethodTree methodTree = trees.getTree(method);
        workingCopy.getTreeMaker().addClassMember(classTree, methodTree);
    }
    
    protected void removeMethodFromClass(TypeElement clazz, ExecutableElement method) {
        Trees trees = workingCopy.getTrees();
        ClassTree classTree = trees.getTree(clazz);
        MethodTree methodTree = trees.getTree(method);
        workingCopy.getTreeMaker().removeClassMember(classTree, methodTree);
    }
    
    private boolean containsTypeMirror(List<? extends TypeMirror> types, TypeMirror type) {
        for (TypeMirror typeMirror : types) {
            if (workingCopy.getTypes().isSameType(typeMirror, type)) {
                return true;
            }
        }
        return false;
    }
    
    public static MethodTree modifyMethod(WorkingCopy workingCopy, ExecutableElement method, Set<Modifier> modifiers, CharSequence name, 
            Tree returnType, List<TypeMirror> parameters, List<ExpressionTree> throwsList, BlockTree body) {
        Trees trees = workingCopy.getTrees();
        MethodTree methodTree = trees.getTree(method);
        TreeMaker treeMaker = workingCopy.getTreeMaker();
        List<VariableTree> createdParams = new ArrayList<VariableTree>();
        if (parameters != null) {
            int index = 0;
            for (TypeMirror typeMirror : parameters) {
                VariableTree variableTree = treeMaker.Variable(
                        treeMaker.Modifiers(Collections.<Modifier>emptySet()),
                        "arg" + index,
                        treeMaker.Type(typeMirror),
                        null
                        );
                createdParams.add(variableTree);
                index++;
            }
        }
        return treeMaker.Method(
                modifiers == null ? methodTree.getModifiers() : treeMaker.Modifiers(modifiers),
                name == null ? methodTree.getName() : name,
                returnType == null ? methodTree.getReturnType() : returnType,
                methodTree.getTypeParameters(),
                createdParams,
                throwsList == null ? methodTree.getThrows() : throwsList,
                body == null ? methodTree.getBody() : body,
                (ExpressionTree) methodTree.getDefaultValue()
                );
    }
    
    public static ExecutableElement createMethodCopy(WorkingCopy workingCopy, ExecutableElement method) {
        Trees trees = workingCopy.getTrees();
        MethodTree methodTree = trees.getTree(method);
        MethodTree resultTree = workingCopy.getTreeMaker().Method(
                methodTree.getModifiers(),
                methodTree.getName(),
                methodTree.getReturnType(),
                methodTree.getTypeParameters(),
                methodTree.getParameters(),
                methodTree.getThrows(),
                methodTree.getBody(),
                (ExpressionTree) methodTree.getDefaultValue()
                );
        TreePath treePath = trees.getPath(workingCopy.getCompilationUnit(), resultTree);
        return (ExecutableElement) trees.getElement(treePath);
    }
    
    public static ExecutableElement createMethod(WorkingCopy workingCopy, CharSequence name) {
        TreeMaker treeMaker = workingCopy.getTreeMaker();
        MethodTree resultTree = treeMaker.Method(
                treeMaker.Modifiers(Collections.<Modifier>emptySet()),
                name,
                treeMaker.PrimitiveType(TypeKind.VOID),
                Collections.<TypeParameterTree>emptyList(),
                Collections.<VariableTree>emptyList(),
                Collections.<ExpressionTree>emptyList(),
                treeMaker.Block(Collections.<StatementTree>emptyList(), false),
                null
                );
        Trees trees = workingCopy.getTrees();
        TreePath treePath = trees.getPath(workingCopy.getCompilationUnit(), resultTree);
        return (ExecutableElement) trees.getElement(treePath);
    }
    
}