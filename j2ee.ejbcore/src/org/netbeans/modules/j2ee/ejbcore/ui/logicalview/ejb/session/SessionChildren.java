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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.SessionMethodController;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.EjbViewController;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.MethodsNode;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;


/**
 * @author Chris Webster
 * @author Martin Adamek
 */

public final class SessionChildren extends Children.Keys<SessionChildren.Key> implements PropertyChangeListener {
    
    // indexes into fields with results for model query
    private final static int REMOTE = 0;
    private final static int LOCAL = 1;

    public enum Key {REMOTE, LOCAL};
    
    private final JavaSource javaSource;
    private final String ejbClass;
    private final EjbJar ejbModule;;
    private final SessionMethodController controller;
    
    public SessionChildren(JavaSource javaSource, String ejbClass, EjbJar ejbModule) {
        this.javaSource = javaSource;
        this.ejbClass = ejbClass;
        this.ejbModule = ejbModule;
        controller = new SessionMethodController(ejbClass, ejbModule.getMetadataModel());
    }
    
    protected void addNotify() {
        super.addNotify();
        try {
            updateKeys();
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        //TODO: RETOUCHE listening on model for logical view
//        session.addPropertyChangeListener(this);
    }
    
    private void updateKeys() throws IOException {
        final boolean[] results = new boolean[] { false, false };
        
        ejbModule.getMetadataModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
            public Void run(EjbJarMetadata metadata) throws Exception {
                Session entity = (Session) metadata.findByEjbClass(ejbClass);
                if (entity != null) {
                    results[REMOTE] = entity.getRemote() != null;
                    results[LOCAL] = entity.getLocal() != null;
                }
                return null;
            }
        });

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                List<Key> keys = new ArrayList<Key>();
                if (results[REMOTE]) { keys.add(Key.REMOTE); }
                if (results[LOCAL]) { keys.add(Key.LOCAL); }
                setKeys(keys);
            }
        });
    }
    
    protected void removeNotify() {
//        session.removePropertyChangeListener(this);
        setKeys(Collections.<Key>emptyList());
        super.removeNotify();
    }
    
    protected Node[] createNodes(Key key) {
        if (Key.LOCAL.equals(key)) {
            Children children = new MethodChildren(javaSource, controller, controller.getLocalInterfaces(), true);
            MethodsNode n = new MethodsNode(ejbClass, ejbModule, children, true);
            n.setIconBaseWithExtension("org/netbeans/modules/j2ee/ejbcore/resources/LocalMethodContainerIcon.gif");
            n.setDisplayName(NbBundle.getMessage(EjbViewController.class, "LBL_LocalMethods"));
            return new Node[] { n };
        }
        if (Key.REMOTE.equals(key)) {
            Children children = new MethodChildren(javaSource, controller, controller.getRemoteInterfaces(), false);
            MethodsNode n = new MethodsNode(ejbClass, ejbModule, children, false);
            n.setIconBaseWithExtension("org/netbeans/modules/j2ee/ejbcore/resources/RemoteMethodContainerIcon.gif");
            n.setDisplayName(NbBundle.getMessage(EjbViewController.class, "LBL_RemoteMethods"));
            return new Node[] { n };
        }
        return null;
    }
    
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        SwingUtilities.invokeLater(new Runnable() {
            
            public void run() {
                try {
                    updateKeys();
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
        });
    }
    
}
