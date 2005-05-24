/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.web.test.util;

import java.io.File;
import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;
import org.netbeans.modules.xml.multiview.ui.DefaultTablePanel;
import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;
import org.netbeans.modules.j2ee.ddloaders.web.multiview.DDBeanTableModel;

/**
 *
 * @author Milan Kuchtiak
 */
public class Helper {

    public static File getDDFile(File dataDir) {
        String result = dataDir.getAbsolutePath() + "/projects/webapp/web/WEB-INF/web.xml";
        return new File(result);
    }
    
    public static DDBeanTableModel getContextParamsTableModel(DDDataObject dObj) {
        ToolBarMultiViewElement mvEl = dObj.getActiveMVElement();
        javax.swing.JPanel sectionPanel = mvEl.getSectionView().findSectionPanel("context_params").getInnerPanel();
        if (sectionPanel==null) return null;
        java.awt.Component[] children = sectionPanel.getComponents();
        DefaultTablePanel tablePanel = null;
        for (int i=0;i<children.length;i++) {
            if (children[i] instanceof DefaultTablePanel) {
                tablePanel = (DefaultTablePanel)children[i];
                break;
            }
        }
        if (tablePanel==null) return null;
        return  (DDBeanTableModel)tablePanel.getModel();
    }
}
