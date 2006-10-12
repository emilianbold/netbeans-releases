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

package org.netbeans.modules.j2ee.earproject.ui;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.modules.j2ee.earproject.ui.customizer.VisualClassPathItem;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.openide.ErrorManager;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

/**
 * List of children of a containing node.
 * Each child node is represented by one key from some data model.
 * Remember to document what your permitted keys are!
 * Edit this template to work with the classes and logic of your data model.
 * @author vkraemer
 */
public class LogicalViewChildren extends Children.Keys  implements AntProjectListener {
    
    private final AntProjectHelper model;
    private java.util.Map<String, VisualClassPathItem> vcpItems;
    
    public LogicalViewChildren(AntProjectHelper model) {
        if (null == model) {
            throw new IllegalArgumentException("model cannot be null"); // NOI18N
        }
        this.model = model;
    }
    
    protected void addNotify() {
        super.addNotify();
        // set the children to use:
        updateKeys();
        // and listen to changes in the model too:
        model.addAntProjectListener(this);
    }
    
    private void updateKeys() {
        Project p = FileOwnerQuery.getOwner(model.getProjectDirectory());
        //#62823 debug
        if(p == null) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, 
                    new IllegalStateException("FileOwnerQuery.getOwner("+ model.getProjectDirectory() + ") returned null. " + // NOI18N
                    "Please report this with the situation description to issue #62823 " + // NOI18N
                    "(http://www.netbeans.org/issues/show_bug.cgi?id=62823)."));
            return ;
        }
        
        EarProject earProject = (EarProject) p.getLookup().lookup(EarProject.class);
        List<VisualClassPathItem> vcpis = earProject.getProjectProperties().getJarContentAdditional();
        vcpItems = new HashMap<String, VisualClassPathItem>();
        for (VisualClassPathItem vcpi : vcpis) {
            Object obj = vcpi.getObject();
            if (!(obj instanceof AntArtifact)) {
                continue;
            }
            AntArtifact aa = (AntArtifact) obj;
            Project vcpiProject = aa.getProject();
            J2eeModuleProvider jmp = (J2eeModuleProvider) vcpiProject.getLookup().lookup(J2eeModuleProvider.class);
            if (null != jmp) {
                vcpItems.put(vcpi.getRaw(), vcpi);
            }
        }
        setKeys(vcpItems.keySet());
    }
    
    protected void removeNotify() {
        model.removeAntProjectListener(this);
        setKeys(Collections.EMPTY_SET);
        super.removeNotify();
    }
    
    protected Node[] createNodes(Object key) {
        VisualClassPathItem vcpItem = vcpItems.get((String) key);
        return new Node[] { new ModuleNode(vcpItem, model.getProjectDirectory() ) };
    }
    
    public void modelChanged(Object ev) {
        // your data model changed, so update the children to match:
        updateKeys();
    }
    
    public void configurationXmlChanged(AntProjectEvent ape) {
        // unsafe to call Children.setKeys() while holding a mutext
        // here the caller holds ProjectManager.mutex() read access
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                updateKeys();
            }
        });
    }
 
    public void propertiesChanged(final AntProjectEvent ape) {
        // unsafe to call Children.setKeys() while holding a mutext
        // here the caller holds ProjectManager.mutex() read access
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                updateKeys();
            }
        });
    }

}
