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

package org.netbeans.modules.wsdlextensions.scheduler.configeditor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import org.netbeans.modules.wsdlextensions.scheduler.utils.Utils;
import org.openide.util.NbBundle;

/**
 * Implements a container for another component with a standard
 * (html-enabled) description area.
 * 
 * @author  sunsoabi_edwong
 */
public class DescriptionContainer extends JPanel {

    private Component other;
    private String title;
    private String desc;
    
    /** Creates new form DescriptionContainer */
    public DescriptionContainer() {
        initComponents();
    }

    public void setOther(Component other) {
        if (other != null) {
            if (other != this.other) {
                if (this.other != null) {
                    pnlOther.remove(this.other);
                }
                this.other = other;
                pnlOther.add(this.other, BorderLayout.CENTER);
            }
        } else if (this.other != null) {
            pnlOther.remove(this.other);
            this.other = null;
        }
    }
    
    public Component getOther() {
        return other;
    }
    
    public void setDescription(String title, String desc) {
        if ((desc != null) && (desc.length() > 0)) {
            this.title = Utils.expungeMnemonicAmpersand(title);
            lblTitle.setText(this.title);
            btnHelp.setEnabled(true);
            
            this.desc = Utils.expungeMnemonicAmpersand(desc);
            if (!Utils.isHtml(this.desc)) {
                this.desc = Utils.toHtml(this.desc);
            }
            this.desc = this.desc.replace("<br>", "&nbsp; ");           //NOI18N
            edpDescrip.setText(this.desc);
            edpDescrip.setCaretPosition(0);
        } else {
            this.title = null;
            this.desc = null;
            lblTitle.setText(null);
            btnHelp.setEnabled(false);
            edpDescrip.setText(null);
        }
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getDescription() {
        return desc;
    }
    
    public void broadcastPropertyChange(String propKey, Object oldVal,
            Object nuVal) {
        firePropertyChange(propKey, oldVal, nuVal);
    }

    private void postAddingEdpDescript() {
        edpDescrip.setBackground(getBackground());

        HTMLEditorKit htmlKit = new HTMLEditorKit();
        if (htmlKit.getStyleSheet().getStyleSheets() == null) {
            StyleSheet css = new StyleSheet();
            Font f = new JLabel().getFont();
            css.addRule(new StringBuffer("body { font-size: ")          //NOI18N
                    .append(f.getSize()).append("; font-family: ")      //NOI18N
                    .append(f.getName()).append("; }").toString());     //NOI18N
            css.addStyleSheet(htmlKit.getStyleSheet());
            htmlKit.setStyleSheet(css);
        }
        edpDescrip.setEditorKit(htmlKit);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        splTopOtherBotDescrip = new javax.swing.JSplitPane();
        pnlOther = new javax.swing.JPanel();
        pnlDescrip = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        btnHelp = new javax.swing.JButton();
        scrDescrip = new javax.swing.JScrollPane();
        edpDescrip = new javax.swing.JEditorPane();

        splTopOtherBotDescrip.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        splTopOtherBotDescrip.setResizeWeight(0.8);
        splTopOtherBotDescrip.setOneTouchExpandable(true);

        pnlOther.setLayout(new java.awt.BorderLayout());
        splTopOtherBotDescrip.setTopComponent(pnlOther);

        lblTitle.setFont(new java.awt.Font("Tahoma", 1, 11));
        lblTitle.setLabelFor(edpDescrip);
        org.openide.awt.Mnemonics.setLocalizedText(lblTitle, NbBundle.getMessage(DescriptionContainer.class, "DescriptionContainer.lblTitle.text")); // NOI18N

        btnHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/wsdlextensions/scheduler/resources/help16x16.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnHelp, NbBundle.getMessage(DescriptionContainer.class, "DescriptionContainer.btnHelp.text")); // NOI18N
        btnHelp.setBorder(null);
        btnHelp.setContentAreaFilled(false);
        btnHelp.setDefaultCapable(false);

        scrDescrip.setBorder(null);
        scrDescrip.setMinimumSize(new java.awt.Dimension(400, 50));
        scrDescrip.setPreferredSize(new java.awt.Dimension(400, 50));

        edpDescrip.setBorder(null);
        edpDescrip.setEditable(false);
        edpDescrip.setOpaque(false);
        scrDescrip.setViewportView(edpDescrip);
        postAddingEdpDescript();

        org.jdesktop.layout.GroupLayout pnlDescripLayout = new org.jdesktop.layout.GroupLayout(pnlDescrip);
        pnlDescrip.setLayout(pnlDescripLayout);
        pnlDescripLayout.setHorizontalGroup(
            pnlDescripLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, pnlDescripLayout.createSequentialGroup()
                .addContainerGap()
                .add(pnlDescripLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, scrDescrip, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 574, Short.MAX_VALUE)
                    .add(pnlDescripLayout.createSequentialGroup()
                        .add(lblTitle, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 553, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnHelp)))
                .addContainerGap())
        );
        pnlDescripLayout.setVerticalGroup(
            pnlDescripLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnlDescripLayout.createSequentialGroup()
                .addContainerGap()
                .add(pnlDescripLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblTitle)
                    .add(btnHelp))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(scrDescrip, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        splTopOtherBotDescrip.setRightComponent(pnlDescrip);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(splTopOtherBotDescrip)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, splTopOtherBotDescrip, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnHelp;
    private javax.swing.JEditorPane edpDescrip;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlDescrip;
    private javax.swing.JPanel pnlOther;
    private javax.swing.JScrollPane scrDescrip;
    private javax.swing.JSplitPane splTopOtherBotDescrip;
    // End of variables declaration//GEN-END:variables
}
