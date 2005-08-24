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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.session;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.SessionMethodController;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.EjbViewController;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.MethodsNode;

import org.openide.nodes.*;
import org.openide.util.NbBundle;


/**
 * @author Chris Webster
 */
public class SessionChildren extends Children.Keys implements PropertyChangeListener {
    private static final String REMOTE_KEY = "remote"; //NOI18N
    private static final String LOCAL_KEY = "local"; //NOI18N
    
    private final Session model;
    private final ClassPath srcPath;
    private final SessionMethodController controller;
    private final EjbJar jar;
    
    public SessionChildren(Session model, ClassPath srcPath, EjbJar jar) {
        this.srcPath = srcPath;
        this.model = model;
        this.jar = jar;
        controller = new SessionMethodController(model, srcPath);
    }
    
    protected void addNotify() {
        super.addNotify();
        updateKeys();
        model.addPropertyChangeListener(this);
        srcPath.addPropertyChangeListener(this);
    }
    
    private void updateKeys() {
        List keys = new ArrayList();
        if (model.getRemote() != null) {
            keys.add(REMOTE_KEY);
        }
        if (model.getLocal()!=null) {
            keys.add(LOCAL_KEY);
        }
        setKeys(keys);
    }
    
    protected void removeNotify() {
        model.removePropertyChangeListener(this);
        srcPath.removePropertyChangeListener(this);
        setKeys(Collections.EMPTY_SET);
        super.removeNotify();
    }
     
    protected Node[] createNodes(Object key) {
        if (LOCAL_KEY.equals(key)) {
            Children c = new MethodChildren(controller, controller.getLocalInterfaces(), true);
            MethodsNode n = new MethodsNode(model, jar, srcPath, c);
            n.setIconBase("org/netbeans/modules/j2ee/ejbcore/resources/LocalMethodContainerIcon");
            n.setDisplayName(NbBundle.getMessage(EjbViewController.class, "LBL_LocalMethods"));
            return new Node[] { n };
        }
        if (REMOTE_KEY.equals(key)) {
            Children c = new MethodChildren(controller, controller.getRemoteInterfaces(), false);
            MethodsNode n = new MethodsNode(model, jar, srcPath, c);
            n.setIconBase("org/netbeans/modules/j2ee/ejbcore/resources/RemoteMethodContainerIcon");
            n.setDisplayName(NbBundle.getMessage(EjbViewController.class, "LBL_RemoteMethods"));
            return new Node[] { n };
        }
        return null;
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
        updateKeys();
    }
    
}
