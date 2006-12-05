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

import org.netbeans.modules.j2ee.common.method.MethodModelSupport;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.common.method.MethodModelSupport;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
public abstract class AbstractMethodController extends EjbMethodController {
    
    private final FileObject ejbClassFO;
    private final EntityAndSession model;
    protected Set classesForSave;
    private final boolean simplified;
    
    public AbstractMethodController(FileObject ejbClassFO, EntityAndSession model) {
        this.ejbClassFO = ejbClassFO;
        this.model = model;
        this.simplified = model.getRoot().getVersion().doubleValue() > 2.1;
    }
    
    public interface GenerateFromImpl {
        void getInterfaceMethodFromImpl(MethodType methodType, String homeClass, String componentClass);
        String getDestinationInterface();
        MethodModel getInterfaceMethod();
    }
    
    public interface GenerateFromIntf {
        void getInterfaceMethodFromImpl(MethodType methodType);
        MethodModel getImplMethod();
        MethodModel getSecondaryMethod();
    }
    
    public abstract GenerateFromImpl createGenerateFromImpl();
    public abstract GenerateFromIntf createGenerateFromIntf();
    
    @Override
    public final MethodModel createAndAdd(MethodModel clientView, boolean local, boolean isComponent) {
        String home = null;
        String component = null;
        if (local) {
            home = model.getLocalHome();
            component = findBusinessInterface(model.getLocal());
        } else {
            home = model.getHome();
            component = findBusinessInterface(model.getRemote());
        }
        if (isComponent) {
            try {
                addMethodToClass(component, clientView);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }

        } else {
            try {
                addMethodToClass(home, clientView);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
        String ejbClass = model.getEjbClass();
        if (hasJavaImplementation(clientView)) {
            for (MethodModel me : getImplementationMethods(clientView)) {
                try {
                    if (!findInClass(ejbClass, me)) {
                        addMethodToClass(ejbClass, me);
                    }
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        }
        MethodModel result = clientView;
        if (!local && !simplified) {
            result = addExceptionIfNecessary(clientView, RemoteException.class.getName());
        }
        return result;
            //TODO: RETOUCHE opening generated method in editor
//        if (methodToOpen != null) {
//            StatementBlock stBlock = null;
//            if (methodToOpen.isValid())
//                stBlock = methodToOpen.getBody();
//            if (stBlock != null)
//                JMIUtils.openInEditor(stBlock);
//        }
    }
    
    @Override
    public final void createAndAddInterface(MethodModel beanImpl, boolean local) {
        MethodType methodType = getMethodTypeFromImpl(beanImpl);
        GenerateFromImpl generateFromImpl = createGenerateFromImpl();
        String home = null;
        String component = null;
        if (local) {
            home = model.getLocalHome();
            component = findBusinessInterface(model.getLocal());
        } else {
            home = model.getHome();
            component = findBusinessInterface(model.getRemote());
        }
        generateFromImpl.getInterfaceMethodFromImpl(methodType, home, component);
        MethodModel method = generateFromImpl.getInterfaceMethod();
        if (!local && !simplified) {
            method = addExceptionIfNecessary(method, RemoteException.class.getName());
        }
        method = MethodModelSupport.createMethodModel(
                method.getName(), 
                method.getReturnType(),
                method.getBody(),
                null,
//                method.getClassName(),
                method.getParameters(),
                method.getExceptions(),
                Collections.<Modifier>emptySet()
                );

        String destinationInterface = generateFromImpl.getDestinationInterface();
        try {
            addMethodToClass(destinationInterface, method);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }

    }
    
    @Override
    public final void createAndAddImpl(MethodModel intfView) {
        MethodType methodType = getMethodTypeFromInterface(intfView);
        GenerateFromIntf generateFromIntf = createGenerateFromIntf();
        generateFromIntf.getInterfaceMethodFromImpl(methodType);
        MethodModel method = generateFromIntf.getImplMethod();
        String ejbClass = model.getEjbClass();
        try {
            addMethodToClass(ejbClass, method);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }

    }
    
    private List<MethodModel> getImplementationMethods(MethodModel intfView) {
        MethodType methodType = getMethodTypeFromInterface(intfView);
        GenerateFromIntf generateFromIntf = createGenerateFromIntf();
        generateFromIntf.getInterfaceMethodFromImpl(methodType);
        MethodModel primary = generateFromIntf.getImplMethod();
        MethodModel secondary = generateFromIntf.getSecondaryMethod();
        List<MethodModel> methods = null;
        if (secondary != null) {
            methods = Arrays.asList(new MethodModel[] {primary, secondary});
        } else {
            methods = Collections.singletonList(primary);
        }
        return methods;
    }
    
    @Override
    public final List<MethodModel> getImplementation(MethodModel intfView) {
        List<MethodModel> methods = getImplementationMethods(intfView);
        List<MethodModel> result = new ArrayList<MethodModel>(methods.size());
        for (MethodModel method : methods) {
            boolean exists = findInClass(getBeanClass(), method);
            if (exists) {
                result.add(method);
            }
        }
        return result;
    }
    
    @Override
    public final MethodModel getInterface(MethodModel beanImpl, boolean local) {
        MethodType methodType = getMethodTypeFromImpl(beanImpl);
        assert methodType != null: "method cannot be used in interface";
        GenerateFromImpl generateFromImpl = createGenerateFromImpl();
        String home = null;
        String component = null;
        if (local) {
            home = model.getLocalHome();
            component = findBusinessInterface(model.getLocal());
        } else {
            home = model.getHome();
            component = findBusinessInterface(model.getRemote());
        }
        generateFromImpl.getInterfaceMethodFromImpl(methodType,home,component);
        MethodModel interfaceMethodModel = generateFromImpl.getInterfaceMethod();
        boolean exists = findInClass(generateFromImpl.getDestinationInterface(), interfaceMethodModel);
        return exists ? interfaceMethodModel : null;
    }
    
    
    /** Performs the check if the method is defined in apporpriate interface
     * @return false if the interface is found but does not contain matching method.
     */
    @Override
    public boolean hasMethodInInterface(MethodModel method, MethodType methodType, boolean local) {
        String intf = null;
        if (methodType.getKind() == MethodType.Kind.BUSINESS) {
            intf = findBusinessInterface(local ? model.getLocal() : model.getRemote());
        } else if (methodType.getKind() == MethodType.Kind.CREATE) {
            String name = chopAndUpper(method.getName(), "ejb"); //NOI18N
            String type = local ? model.getLocal() : model.getRemote();
            method = MethodModelSupport.createMethodModel(
                    name,
                    type,
                    method.getBody(),
                    null,//method.getClassName(),
                    method.getParameters(),
                    method.getExceptions(),
                    method.getModifiers()
                    );
            intf = local ? model.getLocalHome() : model.getHome();
        }
        if (method.getName() == null || intf == null || method.getReturnType() == null) {
            return true;
        }
        if (findInClass(intf, method)) {
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
    
    private MethodModel addExceptionIfNecessary(MethodModel method, String exceptionName) {
        if (!method.getExceptions().contains(exceptionName)) {
            List<String> exceptions = new ArrayList(method.getExceptions());
            exceptions.add(exceptionName);
            return MethodModelSupport.createMethodModel(
                    method.getName(),
                    method.getReturnType(),
                    method.getBody(),
                    null,//method.getClassName(),
                    method.getParameters(),
                    exceptions,
                    method.getModifiers()
                    );
        }
        return method;
    }
    
    private String findBusinessInterface(String compInterfaceName) {
        String beanClass = model.getEjbClass();
        if (compInterfaceName == null || beanClass == null) {
            return null;
        }
        // get bean interfaces
        List<String> beanInterfaces = getInterfaces(beanClass);
        // get method interfaces
        List<String> compInterfaces = getInterfaces(compInterfaceName);
        // look for common candidates
        compInterfaces.retainAll(beanInterfaces);
        if (compInterfaces.isEmpty()) {
            return compInterfaceName;
        }
        String business = compInterfaces.get(0);
        return business == null ? compInterfaceName : business;
    }
    
    private List<String> getInterfaces(final String className) {
        JavaSource javaSource = JavaSource.forFileObject(ejbClassFO);
        final List<String> result = new ArrayList<String>();
        try {
            javaSource.runUserActionTask(new AbstractTask<CompilationController>() {
                public void run(CompilationController controller) throws Exception {
                    controller.toPhase(Phase.ELEMENTS_RESOLVED);
                    TypeElement typeElement = controller.getElements().getTypeElement(className);
                    Types types = controller.getTypes();
                    for (TypeMirror interfaceType : typeElement.getInterfaces()) {
                        Element element = types.asElement(interfaceType);
                        String interfaceFqn = ((TypeElement) element).getQualifiedName().toString();
                        result.add(interfaceFqn);
                    }
                }
            }, true);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
        return result;
    }
    
    @Override
    public final String getBeanClass() {
        return model.getEjbClass() == null ? null : model.getEjbClass();
    }
    
    @Override
    public final List<String> getLocalInterfaces() {
        if (!hasLocal()) {
            return Collections.<String>emptyList();
        }
        List<String> resultList = new ArrayList<String>(2);
        if (model.getLocalHome() != null) {
            resultList.add(model.getLocalHome());
        }
        if (model.getLocal() != null) {
            resultList.add(findBusinessInterface(model.getLocal()));
        }
        
        return resultList;
    }
    
    @Override
    public final List<String> getRemoteInterfaces() {
        if (!hasRemote()) {
            return Collections.<String>emptyList();
        }
        List<String> resultList = new ArrayList<String>(2);
        if (model.getHome() != null) {
            resultList.add(model.getHome());
        }
        if (model.getRemote() != null) {
            resultList.add(findBusinessInterface(model.getRemote()));
        }
        return resultList;
    }
    
    @Override
    public final void delete(String clazz, MethodModel interfaceMethod, boolean local) {
        List<MethodModel> impls = getImplementation(interfaceMethod);
        boolean checkOther = local ? hasRemote() : hasLocal();
        if (!impls.isEmpty()) {
            for (MethodModel impl : impls) {
                if (impl != null) { // could be null here if the method is missing
                    if (((checkOther && getInterface(impl, !local) == null)) || !checkOther) {
                        try {
                            removeMethodFromClass(impl.getClassName(), impl);
                        } catch (IOException e) {
                            ErrorManager.getDefault().notify(e);
                        }

                    }
                }
            }
            try {
                removeMethodFromClass(clazz, interfaceMethod);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }

        }
    }
    
    @Override
    public boolean hasRemote() {
        String intf = model.getHome();
        if (!simplified) {
            if (intf == null) {
                return false;
            }
        }
        intf = model.getRemote();
        if (intf == null || findBusinessInterface(intf) == null) {
            return false;
        }
        return true;
    }
    
    @Override
    public boolean hasLocal() {
        String intf = model.getLocalHome();
        if (!simplified) {
            if (intf == null) {
                return false;
            }
        }
        intf = model.getLocal();
        if (intf == null || findBusinessInterface(intf) == null) {
            return false;
        }
        return true;
    }
    
    @Override
    public MethodModel getPrimaryImplementation(MethodModel intfView) {
        List<MethodModel> impls = getImplementation(intfView);
        return impls.isEmpty() ? null : impls.get(0);
    }
    
    @Override
    public String getRemote() {
        return model.getRemote();
    }
    
    @Override
    public String getLocal() {
        return model.getLocal();
    }
    
//    public final MethodModel addMethod(MethodModel method, boolean local, boolean isComponent) {
//        TypeElement javaClass = getBeanInterface(local, isComponent);
//        assert javaClass != null;
//        addMethodToClass(javaClass, method);
//        if (!local) {
//            method = addExceptionIfNecessary(method, RemoteException.class.getName());
//        }
//        createBeanMethod(method);
//        return method;
//    }
    
    public String getBeanInterface(boolean local, boolean isComponent) {
        if (isComponent) {
            return findBusinessInterface(local ? model.getLocal() : model.getRemote());
        } else {
            String className = local ? model.getLocalHome() : model.getHome();
            return className;
        }
    }
    
    private void createBeanMethod(MethodModel method) throws IOException {
        String beanClass = model.getEjbClass();
        if (hasJavaImplementation(method)) {
            List<MethodModel> implMethods = getImplementationMethods(method);
            for (MethodModel me : implMethods) {
                if (!findInClass(beanClass, me)) {
                    addMethodToClass(beanClass, method);
                }
            }
        }
    }
    
    public final void removeMethod(MethodModel method, boolean local, boolean isComponent) {
        String clazz = getBeanInterface(local, isComponent);
        assert clazz != null;
        if (!local) {
            method = addExceptionIfNecessary(method, RemoteException.class.getName());
        }
        try {
            removeMethodFromClass(clazz, method);
            createBeanMethod(method);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }

    }

    // util candidates
    // -------------------------------------------------------------------------
    
    protected boolean findInClass(final String clazz, final MethodModel methodModel) {
        try {
            return methodFindInClass(clazz, methodModel);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
            return false;
        }

    }

    private boolean methodFindInClass(final String clazz, final MethodModel methodModel) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(ejbClassFO);
        final boolean [] result = new boolean[] {false};
        javaSource.runUserActionTask(new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = controller.getElements().getTypeElement(clazz);
                for (ExecutableElement method : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                    if (MethodModelSupport.isSameMethod(controller, typeElement, method, methodModel)) {
                        result[0] = true;
                        return;
                    }
                }
            }
        }, true);
        return result[0];
    }
    
    protected void addMethodToClass(final String className, final MethodModel method) throws IOException {
        FileObject fileObject = resolveFileObjectForClass(ejbClassFO, className);
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.ELEMENTS_RESOLVED);
                Trees trees = workingCopy.getTrees();
                TypeElement clazz = workingCopy.getElements().getTypeElement(className);
                ClassTree classTree = trees.getTree(clazz);
                MethodTree methodTree = MethodModelSupport.createMethodTree(workingCopy, method);
                ClassTree modifiedClassTree = workingCopy.getTreeMaker().addClassMember(classTree, methodTree);
                workingCopy.rewrite(classTree, modifiedClassTree);
            }
        }).commit();
    }

    protected void removeMethodFromClass(final String className, final MethodModel methodModel) throws IOException {
        FileObject fileObject = resolveFileObjectForClass(ejbClassFO, className);
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.ELEMENTS_RESOLVED);
                if (methodFindInClass(className, methodModel)) {
                    TypeElement foundClass = workingCopy.getElements().getTypeElement(className);
                    Trees trees = workingCopy.getTrees();
                    ClassTree classTree = trees.getTree(foundClass);
                    MethodTree methodTree = MethodModelSupport.createMethodTree(workingCopy, methodModel);
                    ClassTree modifiedClassTree = workingCopy.getTreeMaker().removeClassMember(classTree, methodTree);
                    workingCopy.rewrite(classTree, modifiedClassTree);
                }
            }
        }).commit();;
    }

    /**
     * Tries to find {@link FileObject} which contains class with given className.<br>
     * This method will enter javac context for referenceFileObject to find class by its className,
     * therefore don't call this method within other javac context.
     * 
     * @param referenceFileObject helper file for entering javac context
     * @param className fully-qualified class name to resolve file for
     * @return resolved file or null if not found
     */
    private FileObject resolveFileObjectForClass(FileObject referenceFileObject, final String className) throws IOException {
        final FileObject[] result = new FileObject[1];
        JavaSource javaSource = JavaSource.forFileObject(referenceFileObject);
        javaSource.runUserActionTask(new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                TypeElement typeElement = controller.getElements().getTypeElement(className);
                result[0] = SourceUtils.getFile(typeElement, controller.getClasspathInfo());
            }
        }, true);
        return result[0];
    }
    
}