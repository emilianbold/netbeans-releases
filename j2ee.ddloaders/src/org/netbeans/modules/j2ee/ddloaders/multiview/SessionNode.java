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
