/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
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
