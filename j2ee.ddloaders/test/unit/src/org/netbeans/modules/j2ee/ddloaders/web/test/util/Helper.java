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
        if (SwingUtilities.isEventDispatchThread()) {
            return;
        }
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