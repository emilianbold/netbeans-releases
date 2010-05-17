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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.navigator;

import java.awt.Component;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.modules.bpel.editors.multiview.DesignerMultiViewElementDesc;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 * it's utility class to controll bpelNavigator
 *
 */
public class BpelNavigatorController {
    public static final String BPEL_SOURCE_MVPID = "bpelsource"; // NOI18N
    public static final String BPEL_DESIGNER_MVPID
            = DesignerMultiViewElementDesc.PREFERRED_ID;
    
    private BpelNavigatorController() {
    }
    
    public static TopComponent getNavigatorTC() {
        return WindowManager.getDefault().findTopComponent("navigatorTC"); // NOI18N
    }
    
    /**
     * get current active editor panel in case it's bpel multyview editor then
     * switch accordingly bpelNavigator.
     */
    public static void switchNavigatorPanel() {
        String mvpId = getMVEditorActivePanelPrefferedId();
        if (mvpId == null)  {
            return;
        }
        
        if (mvpId.equals(BPEL_DESIGNER_MVPID)) {
            switchNavigatorPanel(true);
        } 
        /* next use case should be supported
         * editor switches from diagramm to source => navigator doesn't switch state 
        else if (mvpId.equals(BPEL_SOURCE_MVPID)) {
            switchNavigatorPanel(false);
        }*/
    }
    
    public static void switchNavigatorPanel(final boolean isLogicalPanel) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                TopComponent navigatorTC = getNavigatorTC();
                if (navigatorTC == null) {
                    return;
                }
                
                JComboBox navComboBox = getNavigatorComboBox(navigatorTC);
                if (navComboBox == null) {
                    return;
                }
                
                Object selectedNavPanel = navComboBox.getSelectedItem();
                if ((isLogicalPanel
                        && !(BpelNavigatorPanel.getUName().equals(selectedNavPanel)))
                        || (!isLogicalPanel
                        && BpelNavigatorPanel.getUName().equals(selectedNavPanel))) 
                {
                    int numPanels = navComboBox.getItemCount();
                    if (numPanels > 1) {
                        navComboBox.setSelectedIndex(navComboBox
                                    .getSelectedIndex() == 0 ? 1 : 0);
                    }
                }
            }
        });
        
    }
    
    private static JComboBox getNavigatorComboBox(TopComponent navigatorTC) {
        assert navigatorTC != null;
        JComboBox comboBox = null;
        Component[] components = navigatorTC.getComponents();
        for (Component elem : components) {
            if (elem instanceof JComboBox) {
                comboBox = (JComboBox)elem;
                break;
            }
        }
        return comboBox;
    }
    
    private static String getMVEditorActivePanelPrefferedId() {
        TopComponent activeTC = WindowManager.getDefault().getRegistry()
                                                                    .getActivated();
        MultiViewHandler mvh = MultiViews.findMultiViewHandler(activeTC);
        if (mvh == null) {
            return null;
        }
        
        MultiViewPerspective mvp = mvh.getSelectedPerspective();
        if (mvp != null) {
            return mvp.preferredID();
        }
        
        return null;
    }
}
