/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.autoupdate.ui.wizards;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import org.jdesktop.layout.GroupLayout;
import org.netbeans.modules.autoupdate.ui.Utilities;
import org.openide.util.NbBundle;

/**
 *
 * @author  Jiri Rechtacek
 */
public class OperationDescriptionPanel extends javax.swing.JPanel {
    private String tpPrimaryTitleText;
    private String tpPrimaryPluginsText;
    private String tpDependingTitleText;
    private String tpDependingPluginsText;
    private static String ACD_TEXT_HTML;
    /** Creates new form OperationDescriptionPanel */
    public OperationDescriptionPanel (String primary, String primaryU, String depending, String dependingU, boolean hasRequired) {
        this.tpPrimaryTitleText = primary;
        this.tpPrimaryPluginsText = primaryU;
        this.tpDependingTitleText = depending;
        this.tpDependingPluginsText = dependingU;
        customInitComponents (hasRequired);
    }
    
    // XXX: cannot be designed by mattise
    private void customInitComponents (boolean hasRequired) {
        ACD_TEXT_HTML = NbBundle.getMessage (OperationDescriptionPanel.class, "ACD_Text_Html");
        tpPrimaryTitle = new javax.swing.JTextPane();
        tpPrimaryPlugins = new javax.swing.JTextPane();
        tpDependingTitle = new javax.swing.JTextPane();
        tpDependingPlugins = new javax.swing.JTextPane();

        tpPrimaryTitle.setContentType("text/html"); // NOI18N
        tpPrimaryTitle.setEditable(false);
        tpPrimaryTitle.setOpaque (false);
        tpPrimaryTitle.getAccessibleContext ().setAccessibleName (tpPrimaryTitleText);
        tpPrimaryTitle.getAccessibleContext ().setAccessibleName (ACD_TEXT_HTML);

        tpPrimaryPlugins.setContentType ("text/html"); // NOI18N
        tpPrimaryPlugins.setEditable(false);
        tpPrimaryPlugins.setOpaque (false);
        tpPrimaryPlugins.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent hlevt) {
                if (EventType.ACTIVATED == hlevt.getEventType()) {
                    if (hlevt.getURL () != null) {
                        Utilities.showURL(hlevt.getURL());
                    }
                }
            }
        });
        tpPrimaryPlugins.getAccessibleContext ().setAccessibleName (tpPrimaryPluginsText);
        tpPrimaryPlugins.getAccessibleContext ().setAccessibleName (ACD_TEXT_HTML);


        tpDependingTitle.setContentType ("text/html"); // NOI18N
        tpDependingTitle.setEditable(false);
        tpDependingTitle.setOpaque (false);
        tpDependingTitle.getAccessibleContext ().setAccessibleName (tpDependingTitleText);
        tpDependingTitle.getAccessibleContext ().setAccessibleName (ACD_TEXT_HTML);

        tpDependingPlugins.setContentType ("text/html"); // NOI18N
        tpDependingPlugins.setEditable(false);
        tpDependingPlugins.setOpaque (false);
        tpDependingPlugins.getAccessibleContext ().setAccessibleName (tpDependingPluginsText);
        tpDependingPlugins.getAccessibleContext ().setAccessibleName (ACD_TEXT_HTML);

        tpPrimaryTitle.setText(tpPrimaryTitleText);
        tpPrimaryPlugins.setText(tpPrimaryPluginsText);
        tpDependingTitle.setText(tpDependingTitleText);
        tpDependingPlugins.setText(tpDependingPluginsText);
        boolean hasPrimary = tpPrimaryPluginsText.length () > 0 || tpPrimaryTitleText.length () > 0;
        
        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup (getHorizontalGroup (layout, hasPrimary, hasRequired));
        layout.setVerticalGroup (getVerticalGroup (layout, hasPrimary, hasRequired));
        
        tpPrimaryPlugins.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent hlevt) {
                if (EventType.ACTIVATED == hlevt.getEventType()) {
                    if (hlevt.getURL () != null) {
                        Utilities.showURL(hlevt.getURL());
                    }
                }
            }
        });
        tpDependingPlugins.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent hlevt) {
                if (EventType.ACTIVATED == hlevt.getEventType()) {
                    if (hlevt.getURL () != null) {
                        Utilities.showURL(hlevt.getURL());
                    }
                }
            }
        });
        tpPrimaryTitle.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent hlevt) {
                if (EventType.ACTIVATED == hlevt.getEventType()) {
                    if (hlevt.getURL () != null) {
                        Utilities.showURL(hlevt.getURL());
                    }
                }
            }
        });
        tpDependingTitle.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent hlevt) {
                if (EventType.ACTIVATED == hlevt.getEventType()) {
                    if (hlevt.getURL () != null) {
                        Utilities.showURL(hlevt.getURL());
                    }
                }
            }
        });
    }
    
    private GroupLayout.ParallelGroup getVerticalGroup (GroupLayout layout, boolean hasPrimary, boolean hasRequired) {
        GroupLayout.ParallelGroup res = layout.createParallelGroup (GroupLayout.PREFERRED_SIZE);
        GroupLayout.SequentialGroup seq = layout.createSequentialGroup ();
        if (hasPrimary) {
            seq.add (tpPrimaryTitle, GroupLayout.DEFAULT_SIZE, 40, 40)
                .addPreferredGap (org.jdesktop.layout.LayoutStyle.RELATED)
                .add (tpPrimaryPlugins, GroupLayout.PREFERRED_SIZE, tpPrimaryPlugins.getPreferredSize ().height, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap (org.jdesktop.layout.LayoutStyle.RELATED)
                .add (0, 30, 30);
        }
        if (hasRequired) {
            seq.add (tpDependingTitle, GroupLayout.DEFAULT_SIZE, 80, 80)
                    .addPreferredGap (org.jdesktop.layout.LayoutStyle.RELATED)
                    .add (tpDependingPlugins, GroupLayout.PREFERRED_SIZE, tpDependingPlugins.getPreferredSize ().height, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap (org.jdesktop.layout.LayoutStyle.RELATED);
        }
        res.add (seq);
        return res;
    }
    
    private GroupLayout.ParallelGroup getHorizontalGroup (GroupLayout layout, boolean hasPrimary, boolean hasRequired) {
        GroupLayout.ParallelGroup res = layout.createParallelGroup (GroupLayout.LEADING);
        if (hasPrimary) {
            res.add (GroupLayout.TRAILING, layout.createSequentialGroup ()
                    .add (layout.createParallelGroup (GroupLayout.TRAILING)
                    .add (GroupLayout.LEADING, layout.createSequentialGroup ()
                    .add (49, 49, 49)
                    .add (tpPrimaryPlugins, GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE))
                    .add (GroupLayout.LEADING, layout.createSequentialGroup ()
                    .addContainerGap ()
                    .add (tpPrimaryTitle, GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)))
                    .addContainerGap ());
        }
        if (hasRequired) {
            res.add (GroupLayout.TRAILING, layout.createSequentialGroup ()
                    .add (layout.createParallelGroup (GroupLayout.TRAILING)
                    .add (GroupLayout.LEADING, layout.createSequentialGroup ()
                    .add (49, 49, 49)
                    .add (tpDependingPlugins, GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE))
                    .add (GroupLayout.LEADING, layout.createSequentialGroup ()
                    .addContainerGap ()
                    .add (tpDependingTitle, GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)))
                    .addContainerGap ());
        }
        return res;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tpPrimaryTitle = new javax.swing.JTextPane();
        tpPrimaryPlugins = new javax.swing.JTextPane();
        tpDependingTitle = new javax.swing.JTextPane();
        tpDependingPlugins = new javax.swing.JTextPane();

        tpPrimaryTitle.setContentType("text/html"); // NOI18N
        tpPrimaryTitle.setEditable(false);

        tpPrimaryPlugins.setContentType("text/html"); // NOI18N
        tpPrimaryPlugins.setEditable(false);

        tpDependingTitle.setContentType("text/html"); // NOI18N
        tpDependingTitle.setEditable(false);

        tpDependingPlugins.setContentType("text/html"); // NOI18N
        tpDependingPlugins.setEditable(false);

        tpPrimaryTitle.setText(tpPrimaryTitleText);
        tpPrimaryPlugins.setText(tpPrimaryPluginsText);
        tpDependingTitle.setText(tpDependingTitleText);
        tpDependingPlugins.setText(tpDependingPluginsText);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(49, 49, 49)
                        .add(tpDependingPlugins, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(tpDependingTitle, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(49, 49, 49)
                        .add(tpPrimaryPlugins, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(tpPrimaryTitle, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(tpPrimaryTitle)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tpPrimaryPlugins)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tpDependingTitle)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tpDependingPlugins, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(90, 90, 90))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextPane tpDependingPlugins;
    private javax.swing.JTextPane tpDependingTitle;
    private javax.swing.JTextPane tpPrimaryPlugins;
    private javax.swing.JTextPane tpPrimaryTitle;
    // End of variables declaration//GEN-END:variables
    
}
