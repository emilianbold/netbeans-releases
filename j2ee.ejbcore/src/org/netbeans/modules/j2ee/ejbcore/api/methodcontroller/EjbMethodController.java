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
import java.util.Collection;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.jmi.javamodel.Feature;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.modules.j2ee.common.JMIUtils;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.javacore.internalapi.JavaMetamodel;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Utilities;

/**
 * This class encapsulates functionality required for working with EJB methods.
 * @author Chris Webster
 * @author Martin Adamek
 */
public abstract class EjbMethodController {
    
    public static EjbMethodController create(Feature feature) {
        JavaClass jc = JMIUtils.getDeclaringClass(feature);
        return createFromClass(jc);
    }
    
    public static EjbMethodController createFromClass(JavaClass jc) {
        FileObject fo = JavaModel.getFileObject(jc.getResource());
        ClassPath cp = Util.getFullClasspath(fo);
        assert cp != null : "Cannot find ClassPath for " + String.valueOf(fo);

        org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbModule = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(fo);
        if (ejbModule == null) {
            return null;
        }
        DDProvider provider = DDProvider.getDefault();
        EjbJar ejbJar = null;
        EjbMethodController controller = null;
        try {
            ejbJar = provider.getDDRoot(ejbModule.getDeploymentDescriptor());
            EnterpriseBeans beans = ejbJar.getEnterpriseBeans();
            if (beans != null) {
                Session s = (Session) beans.findBeanByName(
                        EnterpriseBeans.SESSION,
                        Ejb.EJB_CLASS, jc.getName());
                if (s != null) {
                    controller = new SessionMethodController(s,cp);
                    if (!controller.hasLocal() && !controller.hasRemote()) {
                        // this is either an error or a web service 
                        controller = null;
                    }
                } else {
                    Entity e = (Entity) beans.findBeanByName(
                        EnterpriseBeans.ENTITY,
                        Ejb.EJB_CLASS, jc.getName());
                    if (e != null) {
                        controller = new EntityMethodController(e, cp, ejbJar);
                    }
                }
            }
        } catch (IOException ioe) {
            // Cannot read dd
        }
        return controller;
    }
    
    /**
     * Find the implementation methods
     * @return MethodElement representing the implementation method or null.
     */
    public abstract List getImplementation(Method intfView);
    public abstract Method getPrimaryImplementation(Method intfView);
    /**
     * @return true if intfView has a java implementation.
     */
    public abstract boolean hasJavaImplementation(Method intfView);
    public abstract boolean hasJavaImplementation(MethodType mt);
    
    /**
     * return interface method in the requested interface. 
     * @param beanImpl implementation method
     * @param local true if local method should be returned false otherwise
     */
    public abstract Method getInterface(Method beanImpl,
                                               boolean local);
    
    /** Return if the passed method is implementation of method defined 
     * in local or remote interface.
     * @param m Method from bean class.
     * @param methodType Type of method to define the search algorithm
     * @param local If <code>true</code> the local interface is searched,
     *              if <code>false</code> the remote interface is searched.
     */
//    public abstract boolean hasMethodInInterface(Method m, int methodType, boolean local);
    public abstract boolean hasMethodInInterface(Method m, MethodType methodType, boolean local);
    
    /**
     * @param clientView of the method
     */
    public abstract MethodType getMethodTypeFromInterface(Method clientView);
    public abstract MethodType getMethodTypeFromImpl(Method implView);
//    public abstract int getMethodTypeFromImpl(Method implView);
    
    public abstract JavaClass getBeanClass();
    public abstract String getLocal();
    public abstract String getRemote();
    public abstract Collection getLocalInterfaces();
    public abstract Collection getRemoteInterfaces();
    public abstract boolean hasLocal();
    public abstract boolean hasRemote();
    public void addEjbQl(Method clientView, String ejbql, FileObject dd) throws IOException {
        assert false: "ejbql not supported for this bean type";
    }
    
    public String createDefaultQL(MethodType mt) {
        return null;
    }
    
    /**
     * create interface signature based on the given implementation
     */
    public abstract void createAndAddInterface(Method beanImpl, boolean local);
    
    /**
     * create implementation methods based on the client method. 
     * @param clientView method which will be inserted into an interface
     * @param intf interface where element will be inserted. This can be the
     * use the business interface pattern.
     */
    public abstract void createAndAddImpl(Method clientView);
    
    public abstract void delete(Method interfaceMethod, boolean local);
    
    /** Checks if given method type is supported by controller.
     * @param type One of <code>METHOD_</code> constants in @link{MethodType}
     */
    public abstract boolean supportsMethodType(int type);
    public abstract void createAndAdd(Method clientView, boolean local, boolean component);
}
