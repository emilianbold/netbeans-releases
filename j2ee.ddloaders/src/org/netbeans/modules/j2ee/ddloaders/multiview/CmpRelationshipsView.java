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
