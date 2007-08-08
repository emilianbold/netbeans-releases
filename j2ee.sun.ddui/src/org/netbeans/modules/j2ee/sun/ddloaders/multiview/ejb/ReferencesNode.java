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
package org.netbeans.modules.j2ee.sun.ddloaders.multiview.ejb;

import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.BaseSectionNode;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.EjbRefGroupNode;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.ResourceEnvRefGroupNode;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.ResourceRefGroupNode;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.ServiceRefGroupNode;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.jms.MessageDestinationRefGroupNode;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;


/**
 * @author Peter Williams
 */
public class ReferencesNode extends BaseSectionNode {

    ReferencesNode(SectionNodeView sectionNodeView, Ejb ejb, final ASDDVersion version) {
        super(sectionNodeView, new Children.Array(), ejb, version, 
                NbBundle.getMessage(ReferencesNode.class, "LBL_ReferencesHeader"), 
                ICON_BASE_REFERENCES_NODE);
        this.helpProvider = true;

        // References
        addChild(new EjbRefGroupNode(sectionNodeView, ejb, version));
        addChild(new ResourceRefGroupNode(sectionNodeView, ejb, version));
        addChild(new ResourceEnvRefGroupNode(sectionNodeView, ejb, version));
        addChild(new ServiceRefGroupNode(sectionNodeView, ejb, version));
        addChild(new MessageDestinationRefGroupNode(sectionNodeView, ejb, version));
    }
    
}
