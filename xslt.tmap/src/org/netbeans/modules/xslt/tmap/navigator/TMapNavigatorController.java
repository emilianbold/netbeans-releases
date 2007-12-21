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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.xslt.tmap.navigator;

import java.awt.Component;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**Щ
 *
 * @author Vitaly Bychkov
 */
public class TMapNavigatorController {
    
    private TMapNavigatorController() {
    }
    
    public static TopComponent getNavigatorTC() {
        return WindowManager.getDefault().findTopComponent("navigatorTC"); // NOI18N
    }
    
    public static void activateLogicalPanel() {
//        System.out.println("try to activate logical panel");
        switchNavPanel(TMapLogicalNavigatorPanel.getUName());
    }
    
    public static void switchNavPanel(final Object navPanelUid) {
        if (navPanelUid == null) {
            return;
        }
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
                
                int selIndex = navComboBox.getSelectedIndex();
                int numPanels = navComboBox.getItemCount();              
//                System.out.println("navPanelUID: "+navPanelUid);
                for (int i = 0; i < numPanels; i++) {
//                    System.out.println(i+ " navComboBox.getItemAt(i): "+navComboBox.getItemAt(i));
                    if (navPanelUid.equals(navComboBox.getItemAt(i))) {
                        if (i != selIndex) {
                            navComboBox.setSelectedIndex(i);
                        }
                        break;
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
