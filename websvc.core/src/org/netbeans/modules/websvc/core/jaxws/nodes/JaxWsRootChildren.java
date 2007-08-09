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
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import java.beans.PropertyChangeListener;
import org.openide.util.RequestProcessor;



public class JaxWsRootChildren extends Children.Keys {
    JaxWsModel jaxWsModel;
    Service[] services;
    JaxWsListener listener;
    FileObject[] srcRoots;

    private RequestProcessor.Task updateNodeTask = RequestProcessor.getDefault().create(new Runnable() {
        public void run() {
            updateKeys();
        }
    });
    
    public JaxWsRootChildren(JaxWsModel jaxWsModel, FileObject[] srcRoots) {
        this.jaxWsModel = jaxWsModel;
        this.srcRoots=srcRoots;
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
        services = jaxWsModel.getServices();
        if (services != null) {
            for (int i = 0; i < services.length; i++) {
                //WebServiceWrapper key = new WebServiceWrapper(webServiceDescription);
                keys.add(services[i]);
            }
        }
        setKeys(keys);
    }

    protected Node[] createNodes(Object key) {
        if(key instanceof Service) {
            String implClass = ((Service)key).getImplementationClass();
            for (FileObject srcRoot:srcRoots) {
                FileObject implClassFo = getImplementationClass(implClass, srcRoot);
                if (implClassFo!=null)
                    return new Node[] {new JaxWsNode(jaxWsModel, (Service)key, srcRoot, implClassFo)};
            }
        }
        return new Node[0];
    }
    
    class JaxWsListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            updateNodeTask.schedule(2000);
        }        
    }
    
    private FileObject getImplementationClass(String implClass, FileObject srcRoot) {
        if(implClass != null && srcRoot!=null) {
            return srcRoot.getFileObject(implClass.replace('.','/')+".java");
            //return JMIUtils.findClass(implBean, srcRoot);
        }
        return null;
    }
    
}
