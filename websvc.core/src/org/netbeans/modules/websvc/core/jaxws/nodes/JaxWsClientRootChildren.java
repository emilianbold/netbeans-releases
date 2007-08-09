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
package org.netbeans.modules.websvc.core.jaxws.nodes;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import java.beans.PropertyChangeListener;
import org.openide.util.RequestProcessor;

public class JaxWsClientRootChildren extends Children.Keys {
    JaxWsModel jaxWsModel;
    Client[] clients;
    JaxWsListener listener;
    FileObject srcRoot;
    
    private RequestProcessor.Task updateNodeTask = RequestProcessor.getDefault().create(new Runnable() {
        public void run() {
            updateKeys();
        }
    });
    
    public JaxWsClientRootChildren(JaxWsModel jaxWsModel, FileObject srcRoot) {
        this.jaxWsModel = jaxWsModel;
        this.srcRoot=srcRoot;
    }
    
    protected void addNotify() {
        listener = new JaxWsListener();
        jaxWsModel.addPropertyChangeListener(listener);
        updateKeys();
    }
    
    protected void removeNotify() {
        setKeys(Collections.EMPTY_SET);
        jaxWsModel.removePropertyChangeListener(listener);
    }
       
    private void updateKeys() {
        List keys = new ArrayList();
        clients = jaxWsModel.getClients();
        if (clients != null) {
            for (int i = 0; i < clients.length; i++) {
                keys.add(clients[i]);
            }
        }
        setKeys(keys);
    }

    protected Node[] createNodes(Object key) {
        if(key instanceof Client) {
            return new Node[] {new JaxWsClientNode(jaxWsModel, (Client)key, srcRoot)};
        }
        return new Node[0];
    }
    
    class JaxWsListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            updateNodeTask.schedule(2000);
        }        
    }

}
