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
import java.awt.*;

import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.netbeans.modules.xml.multiview.ui.DefaultTablePanel;
import org.netbeans.modules.xml.multiview.ui.SectionPanel;
import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;
import org.netbeans.modules.j2ee.ddloaders.web.multiview.DDBeanTableModel;

import javax.swing.*;

/**
 *
 * @author Milan Kuchtiak
 */
public class Helper {

    public static File getDDFile(File dataDir) {
        String result = dataDir.getAbsolutePath() + "/projects/webapp/web/WEB-INF/web.xml";
        return new File(result);
    }

    public static DDBeanTableModel getContextParamsTableModel(final DDDataObject dObj) {
        final ToolBarMultiViewElement multiViewElement = getMultiViewElement(dObj);
        JPanel sectionPanel = getSectionPanel(multiViewElement);
        Component[] children = sectionPanel.getComponents();
        DefaultTablePanel tablePanel = null;
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof DefaultTablePanel) {
                tablePanel = (DefaultTablePanel) children[i];
                break;
            }
        }
        return (DDBeanTableModel) tablePanel.getModel();
    }

    private static JPanel getSectionPanel(final ToolBarMultiViewElement multiViewElement) {
        return new StepIterator() {
            JPanel sectionPanel;

            public boolean step() throws Exception {
                SectionPanel outerPanel = multiViewElement.getSectionView().findSectionPanel("context_params");
                sectionPanel = outerPanel == null ? null : outerPanel.getInnerPanel();
                return sectionPanel != null;
            }
        }.sectionPanel;
    }

    public static ToolBarMultiViewElement getMultiViewElement(final DDDataObject dObj) {
        return new StepIterator() {
            ToolBarMultiViewElement multiViewElement;

            public boolean step() throws Exception {
                multiViewElement = dObj.getActiveMVElement();
                return multiViewElement != null;
            }
        }.multiViewElement;
    }

    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex){}
    }

    public static void waitForDispatchThread() {
        final boolean[] finished = new boolean[]{false};
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                finished[0] = true;
            }
        });
        new StepIterator() {
            public boolean step() throws Exception {
                return finished[0];
            }
        };
    }
}
