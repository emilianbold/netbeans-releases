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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.session;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import javax.swing.SwingUtilities;
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
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                List keys = new ArrayList();
                if (model.getRemote() != null) {
                    keys.add(REMOTE_KEY);
                }
                if (model.getLocal()!=null) {
                    keys.add(LOCAL_KEY);
                }
                setKeys(keys);
            }
        });
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
            MethodsNode n = new MethodsNode(model, jar, srcPath, c, true);
            n.setIconBase("org/netbeans/modules/j2ee/ejbcore/resources/LocalMethodContainerIcon");
            n.setDisplayName(NbBundle.getMessage(EjbViewController.class, "LBL_LocalMethods"));
            return new Node[] { n };
        }
        if (REMOTE_KEY.equals(key)) {
            Children c = new MethodChildren(controller, controller.getRemoteInterfaces(), false);
            MethodsNode n = new MethodsNode(model, jar, srcPath, c, false);
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
