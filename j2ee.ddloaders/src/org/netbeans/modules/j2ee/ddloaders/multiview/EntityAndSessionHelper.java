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

package org.netbeans.modules.j2ee.ddloaders.multiview;

import java.util.ArrayList;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.ejbcore.api.codegeneration.EntityAndSessionGenerator;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.AbstractMethodController;
import org.openide.filesystems.FileObject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.openide.nodes.Node;

/**
 * @author pfiala
 */
public abstract class EntityAndSessionHelper implements PropertyChangeListener, PropertyChangeSource {
    
    protected final EntityAndSession ejb;
    protected final EjbJarMultiViewDataObject ejbJarMultiViewDataObject;
    protected final FileObject ejbJarFile;
    
//    protected ClassPath sourceClassPath;
    
    private List listeners = new LinkedList();
    public AbstractMethodController abstractMethodController;
    
    public EntityAndSessionHelper(EjbJarMultiViewDataObject ejbJarMultiViewDataObject, EntityAndSession ejb) {
        this.ejb = ejb;
        this.ejbJarMultiViewDataObject = ejbJarMultiViewDataObject;
        this.ejbJarFile = ejbJarMultiViewDataObject.getPrimaryFile();
//        sourceClassPath = Utils.getSourceClassPath(ejbJarFile);
        ejbJarMultiViewDataObject.getEjbJar().addPropertyChangeListener(this);
    }
    
//    public JavaClass getLocalBusinessInterfaceClass() {
//        return abstractMethodController.getBeanInterface(true, true);
//    }
    
//    public JavaClass getRemoteBusinessInterfaceClass() {
//        return abstractMethodController.getBeanInterface(false, true);
//    }
    
    public void removeInterfaces(boolean local) {
//        JMIUtils.beginJmiTransaction(true);
//        boolean rollback = true;
//        try {
//            if (local) {
//                Utils.removeClass(sourceClassPath, ejb.getLocalHome());
//                removeBeanInterface(ejb.getLocal());
//                JavaClass businessInterfaceClass = getLocalBusinessInterfaceClass();
//                if (businessInterfaceClass != null) {
//                    removeBeanInterface(businessInterfaceClass.getName());
//                }
//            } else {
//                Utils.removeClass(sourceClassPath, ejb.getHome());
//                removeBeanInterface(ejb.getRemote());
//                JavaClass businessInterfaceClass = getRemoteBusinessInterfaceClass();
//                if (businessInterfaceClass != null) {
//                    removeBeanInterface(businessInterfaceClass.getName());
//                }
//            }
//            rollback = false;
//        } finally {
//            JMIUtils.endJmiTransaction(rollback);
//        }
//        if (local) {
//            ejb.setLocal(null);
//            ejb.setLocalHome(null);
//        } else {
//            ejb.setRemote(null);
//            ejb.setHome(null);
//        }
//        modelUpdatedFromUI();
    }
    
    public void modelUpdatedFromUI() {
        ejbJarMultiViewDataObject.modelUpdatedFromUI();
    }
    
    private void removeBeanInterface(String name) {
//        JMIUtils.removeInterface(getBeanClass(), name);
//        Utils.removeClass(sourceClassPath, name);
    }
    
    public String getEjbClass() {
        return ejb.getEjbClass();
    }
    
    public String getLocal() {
        return ejb.getLocal();
    }
    
    public String getLocalHome() {
        return ejb.getLocalHome();
    }
    
    public String getRemote() {
        return ejb.getRemote();
    }
    
    public String getHome() {
        return ejb.getHome();
    }
    
//    public String getBusinessInterfaceName(boolean local) {
//        JavaClass beanInterface = abstractMethodController.getBeanInterface(local, true);
//        if (beanInterface == null) {
//            return null;
//        }
//        String name = beanInterface.getName();
//        String componentInterfaceName = local ? ejb.getLocal() : ejb.getRemote();
//        if (componentInterfaceName == null || componentInterfaceName.equals(name)) {
//            return null;
//        } else {
//            return name;
//        }
//    }
    
    public void addInterfaces(boolean local) {
        EntityAndSessionGenerator generator = getGenerator();
        String packageName = Utils.getPackage(ejb.getEjbClass());
//        FileObject packageFile = Utils.getPackageFile(sourceClassPath, packageName);
//        String ejbName = ejb.getEjbName();
//        try {
//            JMIUtils.beginJmiTransaction(true);
//            boolean rollback = true;
//            String componentInterfaceName;
//            String homeInterfaceName;
//            String oppositeComponentInterfaceName;
//            String oppositeHomeInterfaceName;
//            try {
//                if (local) {
//                    componentInterfaceName = EjbGenerationUtil.getLocalName(packageName, ejbName);
//                    homeInterfaceName = EjbGenerationUtil.getLocalHomeName(packageName, ejbName);
//                    oppositeComponentInterfaceName = ejb.getRemote();
//                    oppositeHomeInterfaceName = ejb.getHome();
//                    componentInterfaceName = generator.generateLocal(packageName, packageFile, componentInterfaceName, ejbName);
//                    if (oppositeComponentInterfaceName != null && oppositeHomeInterfaceName != null) {
//                        generateHomeInterface(oppositeHomeInterfaceName, homeInterfaceName, oppositeComponentInterfaceName, componentInterfaceName,  false, packageFile);
//                    } else {
//                        homeInterfaceName = generator.generateLocalHome(packageName, packageFile, homeInterfaceName, componentInterfaceName, ejbName);
//                        String businessInterfaceName = EjbGenerationUtil.getLocalBusinessInterfaceName(packageName, ejbName);
//                        generator.generateBusinessInterfaces(packageName, packageFile, businessInterfaceName, ejbName, ejb.getEjbClass(), componentInterfaceName);
//                    }
//                } else {
//                    componentInterfaceName = EjbGenerationUtil.getRemoteName(packageName, ejbName);
//                    homeInterfaceName = EjbGenerationUtil.getHomeName(packageName, ejbName);
//                    oppositeComponentInterfaceName = ejb.getLocal();
//                    oppositeHomeInterfaceName = ejb.getLocalHome();
//                    componentInterfaceName = generator.generateRemote(packageName, packageFile, componentInterfaceName, ejbName);
//                    if (oppositeComponentInterfaceName != null && oppositeHomeInterfaceName != null) {
//                        generateHomeInterface(oppositeHomeInterfaceName, homeInterfaceName, oppositeComponentInterfaceName, componentInterfaceName, true, packageFile);
//                    } else {
//                        homeInterfaceName = generator.generateHome(packageName, packageFile, homeInterfaceName, componentInterfaceName, ejbName);
//                        String businessInterfaceName = EjbGenerationUtil.getBusinessInterfaceName(packageName, ejbName);
//                        generator.generateBusinessInterfaces(packageName, packageFile, businessInterfaceName, ejbName, ejb.getEjbClass(), componentInterfaceName);
//                    }
//                }
//                if (local) {
//                    ejb.setLocal(componentInterfaceName);
//                    ejb.setLocalHome(homeInterfaceName);
//                } else {
//                    ejb.setRemote(componentInterfaceName);
//                    ejb.setHome(homeInterfaceName);
//                }
//                rollback = false;
//            } catch (JmiException jmie) {
//                ErrorManager.getDefault().notify(jmie);
//            } finally {
//                JMIUtils.endJmiTransaction(rollback);
//            }
//            modelUpdatedFromUI();
//        } catch (IOException e) {
//            Utils.notifyError(e);
//        }
    }
    
    /**
     * Generates local or remote home interface based on provided opposite existing remote or local interface.
     * It will copy all create methods and findByPrimaryKey method from existing opposite home interface.
     *
     * @param oldHome name of home interface from which create and findByPrimary methods will be taken
     * @param newHome name of home interface to create
     * @param oldBusiness name of existing business interface (returned by create and findByPrimaryKey method of home interface)
     * @param newBusiness name of already created new business interface (will be returned by create and findByPrimaryKey method of new home interface)
     * @param generateRemote if new home interface is remote
     * @param foForClasspath any file from same classpath as oldHome interface used just for resolving of class by name
     * @throws java.io.IOException
     */
    private static void generateHomeInterface(String oldHome, String newHome, String oldBusiness, String newBusiness, boolean remote, FileObject foForClasspath) throws IOException {
        boolean rollback = true;
//        try {
//            JMIUtils.beginJmiTransaction(true);
//            JavaClass oldIfClass = JMIUtils.findClass(oldHome, foForClasspath);
//            if (oldIfClass != null) {
//                FileObject oldIfFO = JavaModel.getFileObject(oldIfClass.getResource());
//                if (oldIfFO != null) {
//                    JavaClass newIfClass = JMIUtils.createClass(oldIfClass, newHome);
//                    newIfClass.setModifiers(Modifier.PUBLIC | Modifier.INTERFACE);
//                    FileObject newIfFo = oldIfFO.getParent().createData(newIfClass.getSimpleName(), oldIfFO.getExt());
//                    Resource resource = JavaModel.getResource(newIfFo);
//                    resource.setPackageName(oldIfClass.getResource().getPackageName());
//                    resource.getClassifiers().add(newIfClass);
//                    for (Iterator it = oldIfClass.getResource().getImports().iterator(); it.hasNext();) {
//                        Import imp = (Import) it.next();
//                        resource.addImport((Import) imp.duplicate());
//                    }
//                    // add EJBHome or EJBLocalHome as supertype
//                    if (remote) {
//                        newIfClass.getInterfaceNames().add(JMIUtils.createMultipartId(newIfClass, "javax.ejb.EJBHome"));
//                        newIfClass.setJavadocText("This is the remote home interface for " + newIfClass.getSimpleName() + " enterprise bean");
//                    } else {
//                        newIfClass.getInterfaceNames().add(JMIUtils.createMultipartId(newIfClass, "javax.ejb.EJBLocalHome"));
//                        newIfClass.setJavadocText("This is the local home interface for " + newIfClass.getSimpleName() + " enterprise bean");
//                    }
//                    for (Iterator it = getImportantHomeMethods(oldIfClass).iterator(); it.hasNext();) {
//                        Method originalMethod = (Method) it.next();
//                        Method method = JMIUtils.duplicate(originalMethod);
//                        method.setModifiers(0);
//                        if (oldBusiness != null && newBusiness != null) {
//                            if (method.getType().getName().indexOf(oldBusiness.substring(oldBusiness.lastIndexOf(".") + 1)) != -1) {
//                                method.setTypeName(JMIUtils.createMultipartId(method, newBusiness));
//                            }
//                        }
//                        if (remote) {
//                            JMIUtils.addException(method, RemoteException.class.getName());
//                        }
//                        newIfClass.getContents().add(method);
//                    }
//                }
//            }
//            rollback = false;
//        } catch (JmiException jmie) {
//            ErrorManager.getDefault().notify(jmie);
//        } finally {
//            JMIUtils.endJmiTransaction(rollback);
//        }
    }
    
    /**
     * Fids all create methods and findByPrimaryKey method in home interface
     *
     * @param homeInterface
     * @return collection of method or empty collection if no methods found
     */
//        private static Collection getImportantHomeMethods(JavaClass homeInterface) {
//            Collection result = new ArrayList();
//        for (Iterator it = homeInterface.getContents().iterator(); it.hasNext();) {
//            ClassMember classMember = (ClassMember) ((ClassMember) it.next()).duplicate();
//            if (classMember instanceof Method && Modifier.isInterface(homeInterface.getModifiers())) {
//                Method method = (Method) classMember;
//                if (method.getName().startsWith("create") || method.getName().equals("findByPrimaryKey")) {
//                    if (!result.contains(method)){
//                        result.add(method);
//                    }
//                }
//            }
//        }
//        return result;
//    }
    
    protected abstract EntityAndSessionGenerator getGenerator();
    
//    public JavaClass getLocalHomeInterfaceClass() {
//        return abstractMethodController.getBeanInterface(true, false);
//        
//    }
    
//    public JavaClass getHomeInterfaceClass() {
//        return abstractMethodController.getBeanInterface(false, false);
//    }
    
    protected Node createEntityNode() {
//        return Utils.createEntityNode(ejbJarFile, sourceClassPath, (Entity) ejb);
        return null;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listeners.add(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.remove(listener);
    }
    
    protected void firePropertyChange(PropertyChangeEvent evt) {
        for (Iterator iterator = listeners.iterator(); iterator.hasNext();) {
            ((PropertyChangeListener) iterator.next()).propertyChange(evt);
        }
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
    }
    
//    public JavaClass getBeanClass() {
//        return abstractMethodController.getBeanClass();
//    }
}
