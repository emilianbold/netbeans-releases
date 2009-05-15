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

/*
 * MissingClientPanel.java
 *
 * Created on Jul 9, 2008, 4:55:42 PM
 */

package org.netbeans.modules.bugtracking.kenai;

import java.awt.Font;
import java.net.URL;
import javax.swing.JLabel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.openide.awt.HtmlBrowser;

/**
 *
 * @author Tomas Stupka
 */
public class MissingJiraSupportPanel extends javax.swing.JPanel implements HyperlinkListener {

    /** Creates new form MissingClientPanel */
    public MissingJiraSupportPanel() {
        initComponents();
        HTMLEditorKit kit = (HTMLEditorKit) textPane.getEditorKit();
        StyleSheet css = kit.getStyleSheet();
        if (css.getStyleSheets() == null) {
            StyleSheet css2 = new StyleSheet();
            Font f = new JLabel().getFont();
            int size = f.getSize();
            css2.addRule(new StringBuffer("body { font-size: ").append(size) // NOI18N
                    .append("; font-family: ").append(f.getName()).append("; }").toString()); // NOI18N
            css2.addStyleSheet(css);
            kit.setStyleSheet(css2);
        }
        textPane.setOpaque(false);
        textPane.setBackground(new java.awt.Color(0,0,0,0)); // windows and nimbus workaround see issue 145826
        textPane.addHyperlinkListener(this);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        textPane.setBorder(null);
        textPane.setContentType("text/html");
        textPane.setEditable(false);
        textPane.setText(org.openide.util.NbBundle.getMessage(MissingJiraSupportPanel.class, "MissingJiraSupportPanel.textPane.text")); // NOI18N
        textPane.setOpaque(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(textPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 667, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(textPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    final javax.swing.JTextPane textPane = new javax.swing.JTextPane();
    // End of variables declaration//GEN-END:variables

    public void hyperlinkUpdate(HyperlinkEvent e) {
        if(e.getEventType() != HyperlinkEvent.EventType.ACTIVATED) return;
        URL url = e.getURL();
        assert url != null;
        HtmlBrowser.URLDisplayer displayer = HtmlBrowser.URLDisplayer.getDefault ();
        assert displayer != null : "HtmlBrowser.URLDisplayer found.";           // NOI18N
        if (displayer != null) {
            displayer.showURL (url);
        } else {
            BugtrackingManager.LOG.info("No URLDisplayer found.");              // NOI18N
        }
    }

}
