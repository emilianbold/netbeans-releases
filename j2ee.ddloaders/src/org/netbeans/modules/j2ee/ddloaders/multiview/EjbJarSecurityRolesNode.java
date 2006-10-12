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

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.ejb.AssemblyDescriptor;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;

/**
 * Node representing the section for displaying the security roles table.
 *
 * @author ptliu
 */
public class EjbJarSecurityRolesNode extends EjbSectionNode {
    private EjbJar ejbJar;
    
    /**
     * Creates a new instance of EjbJarSecurityRolesNode
     */
    EjbJarSecurityRolesNode(SectionNodeView sectionNodeView, EjbJar ejbJar) {
        super(sectionNodeView, true, ejbJar, Utils.getBundleMessage("LBL_SecurityRoles"), 
                Utils.ICON_BASE_MISC_NODE);
                
        setExpanded(true);
        helpProvider = true;
        
        this.ejbJar = ejbJar;
    }
    
    protected SectionNodeInnerPanel createNodeInnerPanel() {
        EjbJarMultiViewDataObject dataObject = (EjbJarMultiViewDataObject) getSectionNodeView().getDataObject();
        SectionNodeView sectionNodeView = getSectionNodeView();
        EjbJarSecurityRolesTableModel model = new EjbJarSecurityRolesTableModel(sectionNodeView.getModelSynchronizer(), ejbJar);
        InnerTablePanel innerTablePanel = new InnerTablePanel(sectionNodeView, model);
        
        return innerTablePanel;     
    }
    
}
