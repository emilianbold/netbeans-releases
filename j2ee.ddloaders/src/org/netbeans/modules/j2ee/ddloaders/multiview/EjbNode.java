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
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;

import javax.swing.*;

/**
 * @author pfiala
 */
class EjbNode extends SectionNode {

    EjbNode(SectionNodeView sectionNodeView, Ejb ejb) {
        super(sectionNodeView, false, ejb, ejb.getDefaultDisplayName(), getIconBase(ejb));
        if (ejb instanceof Session) {
            addChild(new SessionOverviewNode(sectionNodeView, (Session) ejb));
            addChild(new EjbImplementationAndInterfacesNode(sectionNodeView, ejb));
            addChild(new BeanEnvironmentNode(sectionNodeView, ejb));
            addChild(new BeanDetailNode(sectionNodeView, ejb));
        } else if (ejb instanceof Entity) {
            addChild(new EntityCmpOverviewNode(sectionNodeView, (Entity) ejb));
            addChild(new EjbImplementationAndInterfacesNode(sectionNodeView, ejb));
            addChild(new BeanDetailNode(sectionNodeView, ejb));
        } else if (ejb instanceof MessageDriven) {
            addChild(new MessageDrivenOverviewNode(sectionNodeView, (MessageDriven) ejb));
            addChild(new EjbImplementationNode(sectionNodeView, ejb));
            addChild(new BeanEnvironmentNode(sectionNodeView, ejb));
            addChild(new BeanDetailNode(sectionNodeView, ejb));
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
        SectionNodeView sectionNodeView = getSectionNodeView();
        XmlMultiViewDataObject dataObject = (XmlMultiViewDataObject) sectionNodeView.getDataObject();
        if (key instanceof Session) {
            return null;
        } else if (key instanceof Entity) {
            return null;
        } else if (key instanceof MessageDriven) {
//            return new MessageDrivenPanel(sectionNodeView, dataObject, (EjbJar) key);
            return new SectionInnerPanel(sectionNodeView) {
                public JComponent getErrorComponent(String errorId) {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                public void setValue(JComponent source, Object value) {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                public void linkButtonPressed(Object ddBean, String ddProperty) {
                    //To change body of implemented methods use File | Settings | File Templates.
                }
            };
        } else {
            return null;
        }

    }
}
