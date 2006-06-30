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

import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;

/**
 * @author pfiala
 */
public class CmpRelationshipsView extends SectionNodeView {

    CmpRelationshipsView(EjbJarMultiViewDataObject dataObject) {
        super(dataObject);
        EjbSectionNode rootNode = new EjbSectionNode(this, this, Utils.getBundleMessage("LBL_CmpRelationships"),
                Utils.ICON_BASE_DD_VALID);
        EjbJar ejbJar = dataObject.getEjbJar();
        rootNode.addChild(new CmpRelationShipsNode(this, ejbJar));
        setRootNode(rootNode);
    }

    public XmlMultiViewDataSynchronizer getModelSynchronizer() {
        return ((EjbJarMultiViewDataObject) getDataObject()).getModelSynchronizer();
    }
}
