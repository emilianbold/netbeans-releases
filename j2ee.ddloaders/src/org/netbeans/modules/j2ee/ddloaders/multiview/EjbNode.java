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

import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;

/**
 * @author pfiala
 */
class EjbNode extends SectionNode {

    EjbNode(SectionNodeView sectionNodeView, Ejb ejb) {
        super(sectionNodeView, false, ejb, ejb.getDefaultDisplayName(), getIconBase(ejb));
        if (ejb instanceof Session) {
            Session session = (Session) ejb;
            addChild(new SessionOverviewNode(sectionNodeView, session));
            addChild(new EjbImplementationAndInterfacesNode(sectionNodeView, session));
            addChild(new BeanEnvironmentNode(sectionNodeView, session));
            addChild(new BeanDetailNode(sectionNodeView, session));
        } else if (ejb instanceof Entity) {
            Entity entity = (Entity) ejb;
            addChild(new EntityOverviewNode(sectionNodeView, entity));
            addChild(new EjbImplementationAndInterfacesNode(sectionNodeView, entity));
            if (Entity.PERSISTENCE_TYPE_CONTAINER.equals(entity.getPersistenceType())) {
                // TODO: uncomment following comments aftre implementation completion
                //addChild(new CmpFieldsNode(sectionNodeView, entity));
                //addChild(new SelectMethodsNode(sectionNodeView, entity));
            }
            addChild(new BeanDetailNode(sectionNodeView, entity));
        } else if (ejb instanceof MessageDriven) {
            MessageDriven messageDriven = (MessageDriven) ejb;
            addChild(new MessageDrivenOverviewNode(sectionNodeView, messageDriven));
            addChild(new MdbImplementationNode(sectionNodeView, messageDriven));
            addChild(new BeanEnvironmentNode(sectionNodeView, messageDriven));
            addChild(new BeanDetailNode(sectionNodeView, messageDriven));
        }
    }

    private static String getIconBase(Ejb ejb) {
        String iconBase;
        if (ejb instanceof Entity) {
            iconBase = Utils.ICON_BASE_ENTITY_NODE;
        } else if (ejb instanceof Session) {
            iconBase = Utils.ICON_BASE_SESSION_NODE;
        } else if (ejb instanceof MessageDriven) {
            iconBase = Utils.ICON_BASE_MESSAGE_DRIVEN_NODE;
        } else {
            iconBase = null;
        }
        return iconBase;
    }

    protected SectionInnerPanel createNodeInnerPanel() {
        return null;
    }
}
