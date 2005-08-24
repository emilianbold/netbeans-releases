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
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.ejbcore.api.codegeneration.EjbGenerationUtil;
import org.netbeans.modules.j2ee.ejbcore.api.codegeneration.EntityAndSessionGenerator;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.AbstractMethodController;
import org.netbeans.modules.j2ee.common.JMIUtils;
import org.openide.filesystems.FileObject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
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

    protected ClassPath sourceClassPath;

    private List listeners = new LinkedList();
    public AbstractMethodController abstractMethodController;

    public EntityAndSessionHelper(EjbJarMultiViewDataObject ejbJarMultiViewDataObject, EntityAndSession ejb) {
        this.ejb = ejb;
        this.ejbJarMultiViewDataObject = ejbJarMultiViewDataObject;
        this.ejbJarFile = ejbJarMultiViewDataObject.getPrimaryFile();
        sourceClassPath = Utils.getSourceClassPath(ejbJarFile);
        ejbJarMultiViewDataObject.getEjbJar().addPropertyChangeListener(this);
    }

    public JavaClass getLocalBusinessInterfaceClass() {
        return abstractMethodController.getBeanInterface(true, true);
    }

    public JavaClass getRemoteBusinessInterfaceClass() {
        return abstractMethodController.getBeanInterface(false, true);
    }

    public void removeInterfaces(boolean local) {
        JMIUtils.beginJmiTransaction(true);
        boolean rollback = true;
        try {
            if (local) {
                Utils.removeClass(sourceClassPath, ejb.getLocalHome());
                removeBeanInterface(ejb.getLocal());
                JavaClass businessInterfaceClass = getLocalBusinessInterfaceClass();
                if (businessInterfaceClass != null) {
                    removeBeanInterface(businessInterfaceClass.getName());
                }
            } else {
                JavaClass businessInterfaceClass = getRemoteBusinessInterfaceClass();
                if (businessInterfaceClass != null) {
                    removeBeanInterface(businessInterfaceClass.getName());
                }
                removeBeanInterface(ejb.getRemote());
                Utils.removeClass(sourceClassPath, ejb.getHome());
            }
            rollback = false;
        } finally {
            JMIUtils.endJmiTransaction(rollback);
        }
        if (local) {
            ejb.setLocal(null);
            ejb.setLocalHome(null);
        } else {
            ejb.setRemote(null);
            ejb.setHome(null);
        }
        modelUpdatedFromUI();
    }

    public void modelUpdatedFromUI() {
        ejbJarMultiViewDataObject.modelUpdatedFromUI();
    }

    private void removeBeanInterface(String name) {
        JMIUtils.removeInterface(getBeanClass(), name);
        Utils.removeClass(sourceClassPath, name);
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
        JavaClass beanInterface = abstractMethodController.getBeanInterface(local, true);
        if (beanInterface == null) {
            return null;
        }
        String name = beanInterface.getName();
        String componentInterfaceName = local ? ejb.getLocal() : ejb.getRemote();
        if (componentInterfaceName == null || componentInterfaceName.equals(name)) {
            return null;
        } else {
            return name;
        }
    }

    public void addInterfaces(boolean local) {
        EntityAndSessionGenerator generator = getGenerator();
        String packageName = Utils.getPackage(ejb.getEjbClass());
        FileObject packageFile = Utils.getPackageFile(sourceClassPath, packageName);
        String ejbName = ejb.getEjbName();
        try {
            JMIUtils.beginJmiTransaction(true);
            boolean rollback = true;
            String componentInterfaceName;
            String homeInterfaceName;
            try {
                if (local) {
                    componentInterfaceName = EjbGenerationUtil.getLocalName(packageName, ejbName);
                    componentInterfaceName = generator.generateLocal(packageName, packageFile, componentInterfaceName, ejbName);
                    homeInterfaceName = EjbGenerationUtil.getLocalHomeName(packageName, ejbName);
                    homeInterfaceName = generator.generateLocalHome(packageName, packageFile, homeInterfaceName,
                            componentInterfaceName, ejbName);
                    String businessInterfaceName = EjbGenerationUtil.getLocalBusinessInterfaceName(packageName, ejbName);
                    generator.generateBusinessInterfaces(packageName, packageFile, businessInterfaceName, ejbName, ejb.getEjbClass(),
                            componentInterfaceName);
                } else {
                    componentInterfaceName = EjbGenerationUtil.getRemoteName(packageName, ejbName);
                    componentInterfaceName = generator.generateRemote(packageName, packageFile, componentInterfaceName, ejbName);
                    homeInterfaceName = EjbGenerationUtil.getHomeName(packageName, ejbName);
                    homeInterfaceName = generator.generateHome(packageName, packageFile, homeInterfaceName,
                            componentInterfaceName, ejbName);
                    String businessInterfaceName = EjbGenerationUtil.getBusinessInterfaceName(packageName, ejbName);
                    generator.generateBusinessInterfaces(packageName, packageFile, businessInterfaceName, ejbName, ejb.getEjbClass(),
                            componentInterfaceName);
                }
                rollback = false;
            } finally {
                JMIUtils.endJmiTransaction(rollback);
            }
            if (local) {
                ejb.setLocal(componentInterfaceName);
                ejb.setLocalHome(homeInterfaceName);
            } else {
                ejb.setRemote(componentInterfaceName);
                ejb.setHome(homeInterfaceName);
            }
            modelUpdatedFromUI();
        } catch (IOException e) {
            Utils.notifyError(e);
        }
    }

    protected abstract EntityAndSessionGenerator getGenerator();

    public JavaClass getLocalHomeInterfaceClass() {
        return abstractMethodController.getBeanInterface(true, false);

    }

    public JavaClass getHomeInterfaceClass() {
        return abstractMethodController.getBeanInterface(false, false);
    }

    protected Node createEntityNode() {
        return Utils.createEntityNode(ejbJarFile, sourceClassPath, (Entity) ejb);
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

    public JavaClass getBeanClass() {
        return abstractMethodController.getBeanClass();
    }
}
