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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
