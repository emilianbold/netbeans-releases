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
package org.netbeans.modules.j2ee.sun.ddloaders.multiview.ejb;

import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanNode;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.webservice.EndpointGroupNode;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule.EjbPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;

/**
 * @author Peter Williams
 */
public class EjbNode extends NamedBeanNode {

    // TODO session/entity/mdb specific icons
    public EjbNode(SectionNodeView sectionNodeView, DDBinding binding, final ASDDVersion version) {
        super(sectionNodeView, binding, Ejb.EJB_NAME, ICON_EJB_SESSION_NODE, version);
        
        enableRemoveAction();
        
        Ejb ejb = (Ejb) binding.getSunBean();
        
//        // References (should be blocked in a group)
//        addChild(new EjbRefGroupNode(sectionNodeView, ejb, version));
//        addChild(new ResourceRefGroupNode(sectionNodeView, ejb, version));                
//        addChild(new ResourceEnvRefGroupNode(sectionNodeView, ejb, version));
//        addChild(new ServiceRefGroupNode(sectionNodeView, ejb, version));
//        addChild(new MessageDestinationRefGroupNode(sectionNodeView, ejb, version));
        addChild(new ReferencesNode(sectionNodeView, ejb, version));

        // Webservice endpoints.
        addChild(new EndpointGroupNode(sectionNodeView, binding.getSunBean(), version));
    }

    protected SectionNodeInnerPanel createNodeInnerPanel() {
        return new EjbPanel(getSectionNodeView(), this, version);
    }
}
