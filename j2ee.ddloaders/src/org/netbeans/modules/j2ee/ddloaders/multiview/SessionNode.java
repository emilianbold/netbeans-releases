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

import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;

/**
 * @author pfiala
 */
public class SessionNode extends EjbNode {

    SessionNode(SectionNodeView sectionNodeView, Session session) {
        super(sectionNodeView, session, Utils.ICON_BASE_SESSION_NODE);
        addChild(new SessionOverviewNode(sectionNodeView, session));
        EjbJarMultiViewDataObject ejbJarMultiViewDataObject = (EjbJarMultiViewDataObject) sectionNodeView.getDataObject();
        SessionHelper helper = ejbJarMultiViewDataObject.getSessionHelper(session);
        addChild(new EjbImplementationAndInterfacesNode(sectionNodeView, session, helper));
        addChild(new BeanEnvironmentNode(sectionNodeView, session));
        addChild(new BeanDetailNode(sectionNodeView, session));
    }

}
