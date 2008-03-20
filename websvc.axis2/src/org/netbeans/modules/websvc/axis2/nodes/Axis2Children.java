/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.websvc.axis2.nodes;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.axis2.Axis2ModelProvider;
import org.netbeans.modules.websvc.axis2.config.model.Axis2Model;
import org.netbeans.modules.websvc.axis2.config.model.Service;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import java.beans.PropertyChangeListener;
import org.netbeans.api.project.ProjectUtils;
import org.openide.util.RequestProcessor;

public class Axis2Children extends Children.Keys<Service> {
    private Axis2ModelProvider axis2ModelProvider;
    private Project project;
    private PropertyChangeListener axis2FileListener, axis2Listener;
    
    private RequestProcessor.Task updateNodeTask = RequestProcessor.getDefault().create(new Runnable() {
        public void run() {
            updateKeys();
        }
    });
    
    public Axis2Children(Project project) {
        this.project = project;
        this.axis2ModelProvider = project.getLookup().lookup(Axis2ModelProvider.class);
    }
    
    @Override
    protected void addNotify() {
        axis2FileListener = new Axis2FileListener();
        axis2ModelProvider.addPropertyChangeListener(axis2FileListener);
        Axis2Model axis2Model = axis2ModelProvider.getAxis2Model();
        if (axis2Model != null) {
            axis2Listener = new Axis2Listener();
            axis2Model.getRootComponent().addPropertyChangeListener(axis2Listener);
        }
        updateKeys();
    }
    
    @Override
    protected void removeNotify() {
        setKeys(Collections.<Service>emptyList());
        axis2ModelProvider.removePropertyChangeListener(axis2FileListener);
        Axis2Model axis2Model = axis2ModelProvider.getAxis2Model();
        if (axis2Model != null) {
            axis2Model.getRootComponent().removePropertyChangeListener(axis2Listener);
        }
    }
       
    private void updateKeys() {
        List<Service> keys = new ArrayList<Service>();
        Axis2Model axis2Model = axis2ModelProvider.getAxis2Model();
        if (axis2Model!= null) {
            keys = axis2Model.getRootComponent().getServices();
        }
        setKeys(keys);
    }

    protected Node[] createNodes(Service key) {
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        FileObject srcRoot = null;
        for (SourceGroup group:sourceGroups) {
            FileObject root = group.getRootFolder();
            if (root.getFileObject(key.getServiceClass().replace('.', '/')+".java") != null) {
                srcRoot = root;
                break;
            }
        }
        if (srcRoot != null) {
            return new Node[] {new Axis2ServiceNode(key, srcRoot)};
        } else {
            return new Node[] {};
        }
    }
    
    class Axis2Listener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            updateNodeTask.schedule(2000);
        }        
    }
    
    class Axis2FileListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if (Axis2ModelProvider.PROP_AXIS2.equals(evt.getPropertyName())) {
                if (evt.getNewValue() == null) {
                    Axis2Model oldModel = (Axis2Model)evt.getOldValue();
                    if (oldModel != null) {
                        oldModel.getRootComponent().removePropertyChangeListener(axis2Listener);
                        updateNodeTask.schedule(2000);
                    }
                } else {
                    Axis2Model oldModel = (Axis2Model)evt.getOldValue();
                    if (oldModel != null) {
                        oldModel.getRootComponent().removePropertyChangeListener(axis2Listener);
                    }
                    Axis2Model newModel = (Axis2Model)evt.getNewValue();
                    if (axis2Listener == null) axis2Listener = new Axis2Listener();
                    newModel.getRootComponent().addPropertyChangeListener(axis2Listener);               
                }
            }
        }        
    }
}
