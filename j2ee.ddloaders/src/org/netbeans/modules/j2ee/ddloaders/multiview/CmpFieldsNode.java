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

import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.filesystems.FileObject;

/**
 * @author pfiala
 */
class CmpFieldsNode extends SectionNode {

    CmpFieldsNode(SectionNodeView sectionNodeView, Entity entity) {
        super(sectionNodeView, true, entity, "CMP Fields", Utils.ICON_BASE_MISC_NODE);
    }

    protected SectionInnerPanel createNodeInnerPanel() {
        final Entity entity = (Entity) key;
        final FileObject ejbJarFile = getSectionNodeView().getDataObject().getPrimaryFile();
        final InnerTablePanel innerTablePanel = new InnerTablePanel(getSectionNodeView(),
                new CmpFieldsTableModel(ejbJarFile, entity));
        innerTablePanel.getEditButton().setVisible(false);
        return innerTablePanel;

    }

}
