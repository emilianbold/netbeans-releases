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

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

/**
 * This class encapsulates functionality required for working with EJB methods.
 * @author Chris Webster
 * @author Martin Adamek
 */
public abstract class EjbMethodController {
    
    public static EjbMethodController create(WorkingCopy workingCopy, ExecutableElement method) {
        TypeElement enclosingClass = (TypeElement) method.getEnclosingElement();
        assert ElementKind.CLASS == enclosingClass.getKind() : "Cannot find enclosing class for method " + method.getSimpleName();
        return createFromClass(workingCopy, enclosingClass);
    }
    
    public static EjbMethodController createFromClass(WorkingCopy workingCopy, TypeElement clazz) {
        FileObject fileObject = workingCopy.getFileObject();
        assert fileObject != null : "Cannot find FileObject for " + clazz.getQualifiedName();

        org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(fileObject);
        if (ejbModule == null) {
            return null;
        }
        DDProvider provider = DDProvider.getDefault();
        EjbJar ejbJar = null;
        EjbMethodController controller = null;
        try {
            ejbJar = provider.getMergedDDRoot(ejbModule.getMetadataUnit());
            EnterpriseBeans beans = ejbJar.getEnterpriseBeans();
            if (beans != null) {
                Session session = (Session) beans.findBeanByName(EnterpriseBeans.SESSION, Ejb.EJB_CLASS, clazz.getQualifiedName().toString());
                if (session != null) {
                    controller = new SessionMethodController(workingCopy, session);
                    // TODO EJB3: on Java EE 5.0 this always sets controller to null
                    if (!controller.hasLocal() && !controller.hasRemote()) {
                        // this is either an error or a web service 
                        controller = null;
                    }
                } else {
                    Entity entity = (Entity) beans.findBeanByName(EnterpriseBeans.ENTITY, Ejb.EJB_CLASS, clazz.getQualifiedName().toString());
                    if (entity != null) {
                        controller = new EntityMethodController(workingCopy, entity, ejbJar);
                    }
                }
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
        return controller;
    }
    
    /**
     * Find the implementation methods
     * @return MethodElement representing the implementation method or null.
     */
    public abstract List getImplementation(ExecutableElement intfView);
    public abstract ExecutableElement getPrimaryImplementation(ExecutableElement intfView);
    /**
     * @return true if intfView has a java implementation.
     */
    public abstract boolean hasJavaImplementation(ExecutableElement intfView);
    public abstract boolean hasJavaImplementation(MethodType methodType);
    
    /**
     * return interface method in the requested interface. 
     * @param beanImpl implementation method
     * @param local true if local method should be returned false otherwise
     */
    public abstract ExecutableElement getInterface(ExecutableElement beanImpl, boolean local);
    
    /** Return if the passed method is implementation of method defined 
     * in local or remote interface.
     * @param m Method from bean class.
     * @param methodType Type of method to define the search algorithm
     * @param local If <code>true</code> the local interface is searched,
     *              if <code>false</code> the remote interface is searched.
     */
//    public abstract boolean hasMethodInInterface(Method m, int methodType, boolean local);
    public abstract boolean hasMethodInInterface(ExecutableElement method, MethodType methodType, boolean local);
    
    /**
     * @param clientView of the method
     */
    public abstract MethodType getMethodTypeFromInterface(ExecutableElement clientView);
    public abstract MethodType getMethodTypeFromImpl(ExecutableElement implView);
//    public abstract int getMethodTypeFromImpl(Method implView);
    
    public abstract TypeElement getBeanClass();
    public abstract String getLocal();
    public abstract String getRemote();
    public abstract Collection<TypeElement> getLocalInterfaces();
    public abstract Collection<TypeElement> getRemoteInterfaces();
    public abstract boolean hasLocal();
    public abstract boolean hasRemote();
    public void addEjbQl(ExecutableElement clientView, String ejbql, FileObject ddFileObject) throws IOException {
        assert false: "ejbql not supported for this bean type";
    }
    
    public String createDefaultQL(MethodType methodType) {
        return null;
    }
    
    /**
     * create interface signature based on the given implementation
     */
    public abstract void createAndAddInterface(ExecutableElement beanImpl, boolean local);
    
    /**
     * create implementation methods based on the client method. 
     * @param clientView method which will be inserted into an interface
     * @param intf interface where element will be inserted. This can be the
     * use the business interface pattern.
     */
    public abstract void createAndAddImpl(ExecutableElement clientView);
    
    public abstract void delete(ExecutableElement interfaceMethod, boolean local);
    
    /** Checks if given method type is supported by controller.
     * @param type One of <code>METHOD_</code> constants in @link{MethodType}
     */
    public abstract boolean supportsMethodType(MethodType.Kind type);
    public abstract void createAndAdd(ExecutableElement clientView, boolean local, boolean component);
}
