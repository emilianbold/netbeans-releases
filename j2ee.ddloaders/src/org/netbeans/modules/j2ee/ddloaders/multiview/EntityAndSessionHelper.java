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

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.ejbjarproject.ejb.wizard.EjbGenerationUtil;
import org.netbeans.modules.j2ee.ejbjarproject.ejb.wizard.EntityAndSessionGenerator;
import org.netbeans.modules.j2ee.ejbjarproject.ejb.wizard.entity.EntityGenerator;
import org.netbeans.modules.j2ee.ejbjarproject.ejb.wizard.session.SessionGenerator;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.entity.EntityNode;
import org.openide.filesystems.FileObject;
import org.openide.src.ClassElement;
import org.openide.src.Identifier;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author pfiala
 */
public abstract class EntityAndSessionHelper implements PropertyChangeListener, PropertyChangeSource {

    protected final EntityAndSession ejb;
    protected final ClassElement beanClass;
    protected final EjbJarMultiViewDataObject ejbJarMultiViewDataObject;
    protected final FileObject ejbJarFile;
    private ClassElement localBusinessInterfaceClass;
    private ClassElement remoteBusinessInterfaceClass;
    private ClassElement localInterfaceClass;
    private ClassElement remoteInterfaceClass;
    private ClassElement localHomeClass;
    private ClassElement homeClass;

    protected ClassPath sourceClassPath;

    private List listeners = new LinkedList();

    public EntityAndSessionHelper(EjbJarMultiViewDataObject ejbJarMultiViewDataObject, EntityAndSession ejb) {
        this.ejb = ejb;
        this.ejbJarMultiViewDataObject = ejbJarMultiViewDataObject;
        this.ejbJarFile = ejbJarMultiViewDataObject.getPrimaryFile();
        sourceClassPath = Utils.getSourceClassPath(ejbJarFile);
        beanClass = Utils.getClassElement(sourceClassPath, ejb.getEjbClass());
        ejbJarMultiViewDataObject.getEjbJar().addPropertyChangeListener(this);
        attachListener(beanClass);
        updateLocalInterfaces();
        updateRemoteInterfaces();
    }

    private void updateLocalInterfaces() {
        localHomeClass = changeClass(localHomeClass, getClassElement(ejb.getLocalHome()));
        localInterfaceClass = changeClass(localInterfaceClass, getClassElement(ejb.getLocal()));
        localBusinessInterfaceClass = changeClass(localBusinessInterfaceClass, localInterfaceClass == null ? null :
                getBusinessInterfaceClass(localInterfaceClass));
    }

    private void updateRemoteInterfaces() {
        homeClass = changeClass(homeClass, getClassElement(ejb.getHome()));
        remoteInterfaceClass = changeClass(remoteInterfaceClass, getClassElement(ejb.getRemote()));
        remoteBusinessInterfaceClass = changeClass(remoteBusinessInterfaceClass, remoteInterfaceClass == null ? null :
                getBusinessInterfaceClass(remoteInterfaceClass));
    }


    public ClassElement getLocalBusinessInterfaceClass() {
        return localBusinessInterfaceClass;
    }

    public ClassElement getRemoteBusinessInterfaceClass() {
        return remoteBusinessInterfaceClass;
    }

    private ClassElement getBusinessInterfaceClass(ClassElement compInterfaceClass) {
        // get method interfaces
        List compInterfaces = new LinkedList(Arrays.asList(compInterfaceClass.getInterfaces()));
        // look for common candidates
        compInterfaces.retainAll(Arrays.asList(beanClass.getInterfaces()));
        if (compInterfaces.isEmpty()) {
            return compInterfaceClass;
        }
        ClassElement business = getClassElement(compInterfaces.get(0).toString());
        return business == null ? compInterfaceClass : business;
    }

    public void removeInterfaces(boolean local) {
        if (local) {
            ClassElement localInterfaceClass = this.localInterfaceClass;
            ClassElement localBusinessInterfaceClass = this.localBusinessInterfaceClass;
            String localHome = ejb.getLocalHome();
            ejb.setLocal(null);
            ejb.setLocalHome(null);
            Utils.removeClass(sourceClassPath, localHome);
            removeBeanInterface(localInterfaceClass.getName());
            if (localBusinessInterfaceClass != localInterfaceClass) {
                removeBeanInterface(localBusinessInterfaceClass.getName());
            }
        } else {
            ClassElement remoteInterfaceClass = this.remoteInterfaceClass;
            ClassElement remoteBusinessInterfaceClass = this.remoteBusinessInterfaceClass;
            String home = ejb.getHome();
            ejb.setRemote(null);
            ejb.setHome(null);
            Utils.removeClass(sourceClassPath, home);
            removeBeanInterface(remoteInterfaceClass.getName());
            if (remoteBusinessInterfaceClass != remoteInterfaceClass) {
                removeBeanInterface(remoteBusinessInterfaceClass.getName());
            }
        }
    }

    private void removeBeanInterface(Identifier identifier) {
        Utils.removeInterface(beanClass, identifier.getFullName());
        Utils.removeClass(sourceClassPath, identifier.getFullName());
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

    public String getBusinessInterfaceName(boolean local) {
        if (local) {
            if (localBusinessInterfaceClass == localInterfaceClass) {
                return null;
            } else {
                return localBusinessInterfaceClass.getVMName();
            }
        } else {
            if (remoteBusinessInterfaceClass == remoteInterfaceClass) {
                return null;
            } else {
                return remoteBusinessInterfaceClass.getVMName();
            }
        }
    }

    public void addInterfaces(boolean local) {
        EntityAndSessionGenerator generator;
        if (ejb instanceof Entity) {
            generator = new EntityGenerator();
        } else {
            generator = new SessionGenerator();
        }
        String packageName = Utils.getPackage(ejb.getEjbClass());
        FileObject packageFile = Utils.getPackageFile(sourceClassPath, packageName);
        String ejbName = ejb.getEjbName();
        try {
            if (local) {
                String localInterfaceName = EjbGenerationUtil.getLocalName(packageName, ejbName);
                localInterfaceName = generator.generateLocal(packageName, packageFile, localInterfaceName, ejbName);
                String localHomeInterfaceName = EjbGenerationUtil.getLocalHomeName(packageName, ejbName);
                localHomeInterfaceName = generator.generateLocalHome(packageName, packageFile, localHomeInterfaceName,
                        localInterfaceName, ejbName);
                String businessInterfaceName = EjbGenerationUtil.getLocalBusinessInterfaceName(packageName, ejbName);
                generator.generateBusinessInterfaces(packageName, packageFile, businessInterfaceName, ejbName, ejb.getEjbClass(),
                        localInterfaceName);
                ejb.setLocal(localInterfaceName);
                ejb.setLocalHome(localHomeInterfaceName);
            } else {
                String remoteInterfaceName = EjbGenerationUtil.getRemoteName(packageName, ejbName);
                remoteInterfaceName = generator.generateRemote(packageName, packageFile, remoteInterfaceName, ejbName);
                String homeInterfaceName = EjbGenerationUtil.getHomeName(packageName, ejbName);
                homeInterfaceName = generator.generateHome(packageName, packageFile, homeInterfaceName,
                        remoteInterfaceName, ejbName);
                String businessInterfaceName = EjbGenerationUtil.getBusinessInterfaceName(packageName, ejbName);
                generator.generateBusinessInterfaces(packageName, packageFile, businessInterfaceName, ejbName, ejb.getEjbClass(),
                        remoteInterfaceName);
                ejb.setRemote(remoteInterfaceName);
                ejb.setHome(homeInterfaceName);
            }
        } catch (IOException e) {
            Utils.notifyError(e);
        }
    }

    private ClassElement getClassElement(String className) {
        return className == null ? null : Utils.getClassElement(sourceClassPath, className);
    }

    public ClassElement getLocalHomeInterfaceClass() {
        return localHomeClass;

    }

    public ClassElement getHomeInterfaceClass() {
        return homeClass;
    }

    protected EntityNode createEntityNode() {
        return Utils.createEntityNode(ejbJarFile, sourceClassPath, (Entity) ejb);
    }

    private ClassElement changeClass(ClassElement origClass, ClassElement newClass) {
        if (origClass != newClass) {
            detachListener(origClass);
            attachListener(newClass);
            firePropertyChange(new PropertyChangeEvent(ejb, "IntefaceChanged", origClass, newClass));
        }
        return newClass;
    }

    private void attachListener(ClassElement classElement) {
        if (classElement != null) {
            classElement.addPropertyChangeListener(this);
        }
    }

    private void detachListener(ClassElement classElement) {
        if (classElement != null) {
            classElement.removePropertyChangeListener(this);
        }
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
        if (evt.getSource() == ejb) {
            updateLocalInterfaces();
            updateRemoteInterfaces();
        }
    }
}
