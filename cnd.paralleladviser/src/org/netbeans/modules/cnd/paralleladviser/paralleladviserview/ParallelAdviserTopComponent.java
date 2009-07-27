/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.cnd.paralleladviser.paralleladviserview;

import java.util.Collection;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.paralleladviser.api.ParallelAdviser;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Top component which displays parallel adviser view.
 *
 * @author Nick Krasilnikov
 */
public final class ParallelAdviserTopComponent extends TopComponent {

    private static ParallelAdviserTopComponent instance;
    // path to the icon used by the component and its open action
    public static final String ICON_PATH = "org/netbeans/modules/cnd/paralleladviser/paralleladviserview/resources/paralleladviser.png"; // NOI18N
    private static final String PREFERRED_ID = "ParallelAdviserTopComponent"; // NOI18N

    private ParallelAdviserTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(ParallelAdviserTopComponent.class, "CTL_ParallelAdviserTopComponent")); // NOI18N
        setToolTipText(NbBundle.getMessage(ParallelAdviserTopComponent.class, "HINT_ParallelAdviserTopComponent")); // NOI18N
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        updateTips();
    }

    /**
     * Updates tips.
     */
    public void updateTips() {
        removeAll();
        
        Collection<Advice> tips = ParallelAdviser.getTips();

        JPanel tipPanels = null;
        JPanel lastTipPanel = tipPanels;
        
        for (Advice advice : tips) {
            JPanel newTipPanel = new JPanel();
            newTipPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
            newTipPanel.setLayout(new java.awt.BorderLayout());
            newTipPanel.add(advice.getComponent(), java.awt.BorderLayout.PAGE_START);

            newTipPanel.setBackground(Color.WHITE);

            if(tipPanels == null) {
                tipPanels = newTipPanel;
            }
            if(lastTipPanel != null) {
                lastTipPanel.add(newTipPanel, java.awt.BorderLayout.CENTER);
            }
            lastTipPanel = newTipPanel;
        }

        JScrollPane pane = new JScrollPane();
        pane.getVerticalScrollBar().setUnitIncrement(16);
        pane.setViewportView(tipPanels);

        add(pane, BorderLayout.CENTER);
        validate();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(ParallelAdviserTopComponent.class, "NoViewAvailable")); // NOI18N
        jButton1.setBorderPainted(false);
        jButton1.setEnabled(false);
        add(jButton1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized ParallelAdviserTopComponent getDefault() {
        if (instance == null) {
            instance = new ParallelAdviserTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the MacroExpansionTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized ParallelAdviserTopComponent findInstance() {
        TopComponent win = CsmUtilities.getTopComponentInEQ(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(ParallelAdviserTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system."); // NOI18N
            return getDefault();
        }
        if (win instanceof ParallelAdviserTopComponent) {
            return (ParallelAdviserTopComponent) win;
        }
        Logger.getLogger(ParallelAdviserTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID + // NOI18N
                "' ID. That is a potential source of errors and unexpected behavior."); // NOI18N
        return getDefault();
    }

    protected
    @Override
    String preferredID() {
        return PREFERRED_ID;
    }

    public
    @Override
    int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

}
