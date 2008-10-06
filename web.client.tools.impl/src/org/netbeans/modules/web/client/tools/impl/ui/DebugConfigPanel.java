/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.client.tools.impl.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JPanel;
import org.netbeans.modules.web.client.tools.api.WebClientToolsProjectUtils;
import org.netbeans.modules.web.client.tools.impl.DebugConstants;
import org.openide.DialogDescriptor;

/**
 *
 * @author  Quy Nguyen <quynguyen@netbeans.org>
 */
public class DebugConfigPanel extends JPanel {
    
    private final boolean ffBrowserSupported;
    private final boolean ieBrowserSupported;
    private final Preferences globalPrefs;
    
    /** Creates new form ClientDebugPanel */
    public DebugConfigPanel(boolean ffSupported, boolean ieSupported, Preferences globalPrefs) {
        initComponents();
        
        this.ffBrowserSupported = ffSupported;
        this.ieBrowserSupported = ieSupported;
        this.globalPrefs = globalPrefs;
        
        debugCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                adjustBrowserRadioButtons(false);
            }
        });
        
        debugCheckBox.setSelected(globalPrefs.getBoolean(DebugConstants.CLIENT_DEBUG, true));
        adjustBrowserRadioButtons(true);
    }

    private void adjustBrowserRadioButtons(boolean init) {
        if (init) {
            if (ffBrowserSupported && !ieBrowserSupported) {
                firefoxRadioButton.setSelected(true);
            } else if (ieBrowserSupported && !ffBrowserSupported) {
                internetExplorerRadioButton.setSelected(true);
            } else {
                String defBrowser = ffBrowserSupported ? 
                    WebClientToolsProjectUtils.Browser.FIREFOX.name() :
                    WebClientToolsProjectUtils.Browser.INTERNET_EXPLORER.name();
                
                String browserValue = globalPrefs.get(DebugConstants.BROWSER, defBrowser);
                WebClientToolsProjectUtils.Browser browser = WebClientToolsProjectUtils.Browser.valueOf(browserValue);
                
                firefoxRadioButton.setSelected(browser == WebClientToolsProjectUtils.Browser.FIREFOX);
                internetExplorerRadioButton.setSelected(browser == WebClientToolsProjectUtils.Browser.INTERNET_EXPLORER);
            }
        }
        
        firefoxRadioButton.setEnabled(debugCheckBox.isSelected() && ffBrowserSupported);
        internetExplorerRadioButton.setEnabled(debugCheckBox.isSelected() && ieBrowserSupported);
    }
    
    public ActionListener getPanelCloseHandler() {
        return new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == DialogDescriptor.OK_OPTION) {
                    boolean debugEnabled = debugCheckBox.isSelected();
                    boolean showDialog = !doNotShowAgain.isSelected();
                    String browser = firefoxRadioButton.isSelected() ? WebClientToolsProjectUtils.Browser.FIREFOX.name() : WebClientToolsProjectUtils.Browser.INTERNET_EXPLORER.name();

                    globalPrefs.putBoolean(DebugConstants.CLIENT_DEBUG, debugEnabled);
                    globalPrefs.putBoolean(DebugConstants.DISPLAY_CONFIG, showDialog);
                    globalPrefs.put(DebugConstants.BROWSER, browser);
                    try {
                        globalPrefs.sync();
                    } catch (BackingStoreException ex) {
                        Log.getLogger().log(Level.WARNING, "Could not save preferences", ex);
                    }
                }
            }
        };
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        browserButtonGroup = new javax.swing.ButtonGroup();
        debugCheckBox = new javax.swing.JCheckBox();
        firefoxRadioButton = new javax.swing.JRadioButton();
        internetExplorerRadioButton = new javax.swing.JRadioButton();
        debugPropertyMsg = new javax.swing.JLabel();
        doNotShowAgain = new javax.swing.JCheckBox();

        org.openide.awt.Mnemonics.setLocalizedText(debugCheckBox, org.openide.util.NbBundle.getMessage(DebugConfigPanel.class, "DebugConfigPanel.debugCheckBox.text")); // NOI18N

        browserButtonGroup.add(firefoxRadioButton);
        firefoxRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(firefoxRadioButton, org.openide.util.NbBundle.getMessage(DebugConfigPanel.class, "DebugConfigPanel.firefoxRadioButton.text")); // NOI18N

        browserButtonGroup.add(internetExplorerRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(internetExplorerRadioButton, org.openide.util.NbBundle.getMessage(DebugConfigPanel.class, "DebugConfigPanel.internetExplorerRadioButton.text")); // NOI18N

        debugPropertyMsg.setText(org.openide.util.NbBundle.getMessage(DebugConfigPanel.class, "DebugConfigPanel.debugPropertyMsg.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(doNotShowAgain, org.openide.util.NbBundle.getMessage(DebugConfigPanel.class, "DebugConfigPanel.doNotShowAgain.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(debugCheckBox)
                    .add(debugPropertyMsg, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 404, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(doNotShowAgain)
                    .add(layout.createSequentialGroup()
                        .add(24, 24, 24)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(internetExplorerRadioButton)
                            .add(firefoxRadioButton))))
                .addContainerGap(58, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(debugCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(firefoxRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(internetExplorerRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(12, 12, 12)
                .add(debugPropertyMsg, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 81, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 15, Short.MAX_VALUE)
                .add(doNotShowAgain)
                .addContainerGap())
        );

        debugCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DebugConfigPanel.class, "DebugConfigPanel.debugCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        firefoxRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DebugConfigPanel.class, "DebugConfigPanel.firefoxRadioButton.AccessibleContext.accessibleDescription")); // NOI18N
        internetExplorerRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DebugConfigPanel.class, "DebugConfigPanel.internetExplorerRadioButton.AccessibleContext.accessibleDescription")); // NOI18N
        doNotShowAgain.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DebugConfigPanel.class, "DebugConfigPanel.doNotShowAgain.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup browserButtonGroup;
    private javax.swing.JCheckBox debugCheckBox;
    private javax.swing.JLabel debugPropertyMsg;
    private javax.swing.JCheckBox doNotShowAgain;
    private javax.swing.JRadioButton firefoxRadioButton;
    private javax.swing.JRadioButton internetExplorerRadioButton;
    // End of variables declaration//GEN-END:variables

}
