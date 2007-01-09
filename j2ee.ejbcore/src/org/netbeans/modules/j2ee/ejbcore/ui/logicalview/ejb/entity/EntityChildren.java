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
import java.util.*;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;


/**
 * @author Chris Webster
 * @author Martin Adamek
 */
public final class EntityChildren extends Children.Keys<EntityChildren.KEY> implements PropertyChangeListener {
    
    protected enum KEY { REMOTE, LOCAL, CMP_FIELDS }
    
    private final Entity model;
    private final ClassPath srcPath;
//    private final EntityMethodController controller;
//    private final EjbJar jar;
//    private final FileObject ddFile;
    
    public EntityChildren(Entity model, ClassPath srcPath, EjbJar jar, FileObject ddFile) {
        this.srcPath = srcPath;
        this.model = model;
        //TODO: RETOUCHE
//        this.jar = jar;
//        this.ddFile = ddFile;
//        controller = new EntityMethodController(null, model, jar);
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
                List<KEY> keys = new ArrayList<KEY>();
                if (model.getRemote() != null) {
                    keys.add(KEY.REMOTE);
                }
                if (model.getLocal()!=null) {
                    keys.add(KEY.LOCAL);
                }
                if (Entity.PERSISTENCE_TYPE_CONTAINER.equals(model.getPersistenceType())) {
                    keys.add(KEY.CMP_FIELDS);
                }
                setKeys(keys);
            }
        });
    }
    
    protected void removeNotify() {
        model.removePropertyChangeListener(this);
        srcPath.removePropertyChangeListener(this);
        setKeys(Collections.<KEY>emptySet());
        super.removeNotify();
    }
     
    protected Node[] createNodes(KEY key) {
        //TODO: RETOUCHE
//        if (key == KEY.LOCAL) {
//            Children c = new MethodChildren(controller, model, controller.getLocalInterfaces(), true, ddFile);
//            MethodsNode n = new MethodsNode(model, jar, srcPath, c, true);
//            n.setIconBaseWithExtension("org/netbeans/modules/j2ee/ejbcore/resources/LocalMethodContainerIcon.gif");
//            n.setDisplayName(NbBundle.getMessage(EjbViewController.class, "LBL_LocalMethods"));
//            return new Node[] { n };
//        }
//        if (key == KEY.REMOTE) {
//            Children c = new MethodChildren(controller, model, controller.getRemoteInterfaces(), false, ddFile);
//            MethodsNode n = new MethodsNode(model, jar, srcPath, c, false);
//            n.setIconBaseWithExtension("org/netbeans/modules/j2ee/ejbcore/resources/RemoteMethodContainerIcon.gif");
//            n.setDisplayName(NbBundle.getMessage(EjbViewController.class, "LBL_RemoteMethods"));
//            return new Node[] { n };
//        }
//        if (key == KEY.CMP_FIELDS) {
//            CMPFieldsNode n = new CMPFieldsNode(controller,model,jar, ddFile);
//            n.setIconBaseWithExtension("org/netbeans/modules/j2ee/ejbcore/resources/CMFieldContainerIcon.gif");
//            n.setDisplayName(NbBundle.getMessage(EntityChildren.class, "LBL_CMPFields"));
//            return new Node[] { n };
//        }
        return null;
    }
    
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        //TODO add code for detecting class name changes 
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                updateKeys();
            }
        });
    }
    
}
