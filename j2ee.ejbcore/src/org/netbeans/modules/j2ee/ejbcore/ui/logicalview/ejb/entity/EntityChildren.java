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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.entity;

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
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EntityMethodController;
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
public final class EntityChildren extends Children.Keys<EntityChildren.KEY> implements PropertyChangeListener {
    
    // indexes into fields with results for model query
    private final static int REMOTE = 0;
    private final static int LOCAL = 1;
    private final static int CMP = 2;
    
    protected enum KEY { REMOTE, LOCAL, CMP_FIELDS }

    private final JavaSource javaSource;
    private final String ejbClass;
    private final EjbJar ejbModule;
    private final EntityMethodController controller;
    private final Entity model; // EJB 2.1
    
    public EntityChildren(JavaSource javaSource, final String ejbClass, EjbJar ejbModule) throws IOException {
        this.javaSource = javaSource;
        this.ejbClass = ejbClass;
        this.ejbModule = ejbModule;;
        this.controller = new EntityMethodController(ejbClass, ejbModule.getMetadataModel());
        this.model = ejbModule.getMetadataModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Entity>() {
            public Entity run(EjbJarMetadata metadata) throws Exception {
                return (Entity) metadata.findByEjbClass(ejbClass);
            }
        });
    }
    
    protected void addNotify() {
        super.addNotify();
        try {
            updateKeys();
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        //TODO: RETOUCHE listening on model for logical view changes
//        model.addPropertyChangeListener(this);
    }
    
    private void updateKeys() throws IOException {
        final boolean[] results = new boolean[] { false, false, false };
        
        ejbModule.getMetadataModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
            public Void run(EjbJarMetadata metadata) throws Exception {
                Entity entity = (Entity) metadata.findByEjbClass(ejbClass);
                if (entity != null) {
                    results[REMOTE] = entity.getRemote() != null;
                    results[LOCAL] = entity.getLocal() != null;
                    results[CMP] = Entity.PERSISTENCE_TYPE_CONTAINER.equals(entity.getPersistenceType());
                }
                return null;
            }
        });
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                List<KEY> keys = new ArrayList<KEY>();
                if (results[REMOTE]) {
                    keys.add(KEY.REMOTE);
                }
                if (results[LOCAL]) {
                    keys.add(KEY.LOCAL);
                }
                if (results[CMP]) {
                    keys.add(KEY.CMP_FIELDS);
                }
                setKeys(keys);
            }
        });
    }
    
    protected void removeNotify() {
//        model.removePropertyChangeListener(this);
        setKeys(Collections.<KEY>emptySet());
        super.removeNotify();
    }
     
    protected Node[] createNodes(KEY key) {
        if (key == KEY.LOCAL) {
            Children children = new MethodChildren(javaSource, controller, model, controller.getLocalInterfaces(), true, ejbModule.getDeploymentDescriptor());
            MethodsNode n = new MethodsNode(ejbClass, ejbModule, children, true);
            n.setIconBaseWithExtension("org/netbeans/modules/j2ee/ejbcore/resources/LocalMethodContainerIcon.gif");
            n.setDisplayName(NbBundle.getMessage(EjbViewController.class, "LBL_LocalMethods"));
            return new Node[] { n };
        }
        if (key == KEY.REMOTE) {
            Children children = new MethodChildren(javaSource, controller, model, controller.getRemoteInterfaces(), false, ejbModule.getDeploymentDescriptor());
            MethodsNode n = new MethodsNode(ejbClass, ejbModule, children, false);
            n.setIconBaseWithExtension("org/netbeans/modules/j2ee/ejbcore/resources/RemoteMethodContainerIcon.gif");
            n.setDisplayName(NbBundle.getMessage(EjbViewController.class, "LBL_RemoteMethods"));
            return new Node[] { n };
        }
        if (key == KEY.CMP_FIELDS) {
            try {
                CMPFieldsNode n = new CMPFieldsNode(controller, model, ejbModule.getDeploymentDescriptor());
                n.setIconBaseWithExtension("org/netbeans/modules/j2ee/ejbcore/resources/CMFieldContainerIcon.gif");
                n.setDisplayName(NbBundle.getMessage(EntityChildren.class, "LBL_CMPFields"));
                return new Node[] { n };
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        return null;
    }
    
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        //TODO add code for detecting class name changes 
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
