/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;

/**
 * @author pfiala
 */
class MessageDrivenNode extends EjbSectionNode {

    MessageDrivenNode(SectionNodeView sectionNodeView, MessageDriven messageDriven) {
        super(sectionNodeView, false, messageDriven, Utils.getEjbDisplayName(messageDriven),
                Utils.ICON_BASE_MESSAGE_DRIVEN_NODE);
        addChild(new MessageDrivenOverviewNode(sectionNodeView, messageDriven));
        addChild(new MdbImplementationNode(sectionNodeView, messageDriven));
        addChild(new BeanEnvironmentNode(sectionNodeView, messageDriven));
        addChild(new BeanDetailNode(sectionNodeView, messageDriven));
        helpProvider = true;
    }

    protected SectionNodeInnerPanel createNodeInnerPanel() {
        return null;
    }
}
