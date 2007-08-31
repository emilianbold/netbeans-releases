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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.compapp.projects.jbi.ui;

import org.netbeans.modules.compapp.projects.jbi.JbiProject;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.JbiProjectProperties;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.VisualClassPathItem;
import org.netbeans.api.project.ant.AntArtifact;

import org.openide.nodes.Children;
import org.openide.nodes.Node;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import org.netbeans.modules.compapp.projects.jbi.api.JbiProjectConstants;
import org.netbeans.modules.compapp.projects.jbi.jeese.ui.JavaEEModuleNode;

/**
 * DOCUMENT ME!
 *
 * @author 
 * @version 
 */
public class JbiModuleViewChildren extends Children.Keys implements PropertyChangeListener {
    private final JbiProject project;
    
    /**
     * Creates a new JbiModuleViewChildren object.
     *
     * @param project DOCUMENT ME!
     */
    public JbiModuleViewChildren(JbiProject project) {
        this.project = project;
    }
    
    /**
     * DOCUMENT ME!
     */
    protected void addNotify() {
        super.addNotify();
        updateKeys();
        
        // and listen to changes in the model too:
        //model.addPropertyChangeListener(this);
    }
    
    private void updateKeys() {
        JbiProjectProperties epp = project.getProjectProperties();
        List keys = Collections.EMPTY_LIST;
        Object t = epp.get(JbiProjectProperties.JBI_CONTENT_ADDITIONAL);
        
        if (!(t instanceof List)) {
            assert false : "JBI content isn't a List???";  // NOI18N
            
            return;
        }
        
        List vcpis = (List) t;
        Iterator iter = vcpis.iterator();
        keys = new ArrayList();
        
        while (iter.hasNext()) {
            t = iter.next();
            
            if (!(t instanceof VisualClassPathItem)) {
                assert false : "JBI content element isn't a VCPI?????"; // NOI18N
                
                continue;
            }
            
            VisualClassPathItem vcpi = (VisualClassPathItem) t;
            Object obj = vcpi.getObject();
            AntArtifact aa;
            
            if (obj instanceof AntArtifact) {
//                aa = (AntArtifact) obj;
//                if (this.jeeTypes.contains(aa.getType())){
//                    continue;
//                }
            } else {
                continue;
            }
            
            keys.add(vcpi);
        }
        
        setKeys(keys);
    }
    
    /**
     * DOCUMENT ME!
     */
    protected void removeNotify() {
        //epp.removePropertyChangeListener(this);
        setKeys(Collections.EMPTY_SET);
        super.removeNotify();
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param key DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    protected Node[] createNodes(Object key) {
        // interpret your key here...usually one node generated, but could be zero or more
        VisualClassPathItem vcpi = (VisualClassPathItem) key;
        Object obj = vcpi.getObject();
        AntArtifact aa;
        Node[]  ret = null;
        if (obj instanceof AntArtifact) {
            aa = (AntArtifact) obj;
            if (aa.getType().startsWith(JbiProjectConstants.JAVA_EE_EAR_COMPONENT_ARCHIVE)){
                ret = new Node[] {new JavaEEModuleNode(vcpi, this.project )};
            } else {
                ret = new Node[] {new JbiModuleNode(vcpi)};
            }
        }
        
        return ret;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param ev DOCUMENT ME!
     */
    public void modelChanged(Object ev) {
        // your data model changed, so update the children to match:
        updateKeys();
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param pce DOCUMENT ME!
     */
    public void propertyChange(PropertyChangeEvent pce) {
        updateKeys();
    }
}
