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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.project.ui;

import java.awt.event.ActionListener;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.javacard.Utils;
import org.netbeans.modules.javacard.api.Card;
import org.netbeans.modules.javacard.api.JavacardPlatform;
import org.netbeans.modules.javacard.project.JCProjectProperties;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Tim Boudreau
 */
public class BadPlatformOrDevicePanel extends javax.swing.JPanel implements LookupListener, ProblemHandler.UI, ActionListener {
    private Lookup.Result<JavacardPlatform> pRes;
    private Lookup.Result<Card> cRes;
    private ProblemHandler handler;
    private String problem;
    private JCProjectProperties props;

    public BadPlatformOrDevicePanel(String platform, String device) {
        this (platform, device, true);
    }

    public BadPlatformOrDevicePanel(String platform, String device, boolean showDontShowAgainCheckbox) {
        initComponents();
        if (!showDontShowAgainCheckbox) {
            jCheckBox1.setVisible(false);
        }
        props = new JCProjectProperties();
        props.setPlatformName(platform);
        props.setActiveDevice(device);
        selPanel.setProperties(props);
    }

    public String getPlatform() {
        return props.getPlatformName();
    }

    public String getDevice() {
        return props.getActiveDevice();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        pRes = selPanel.getLookup().lookupResult (JavacardPlatform.class);
        cRes = selPanel.getLookup().lookupResult (Card.class);
        pRes.addLookupListener(this);
        cRes.addLookupListener(this);
        pRes.allInstances();
        cRes.allInstances();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        cRes.removeLookupListener(this);
        pRes.removeLookupListener(this);
        cRes = null;
        pRes = null;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        selPanel = new org.netbeans.modules.javacard.project.ui.PlatformAndDevicePanel();
        jCheckBox1 = new javax.swing.JCheckBox();

        jLabel1.setText(org.openide.util.NbBundle.getMessage(BadPlatformOrDevicePanel.class, "BadPlatformOrDevicePanel.jLabel1.text")); // NOI18N

        jCheckBox1.setText(org.openide.util.NbBundle.getMessage(BadPlatformOrDevicePanel.class, "BadPlatformOrDevicePanel.jCheckBox1.text")); // NOI18N
        jCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 12, 0));
        jCheckBox1.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        jCheckBox1.addActionListener(this);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jCheckBox1)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE)
                    .add(selPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(selPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jCheckBox1)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }

    // Code for dispatching events from components to event handlers.

    public void actionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == jCheckBox1) {
            BadPlatformOrDevicePanel.this.dontShowDialogChecked(evt);
        }
    }// </editor-fold>//GEN-END:initComponents

    private static final String PREFS_KEY_DONT_SHOW_DLG = "dontShowBrokenPlatformDialog"; //NOI18N
    private void dontShowDialogChecked(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dontShowDialogChecked
        NbPreferences.forModule(BadPlatformOrDevicePanel.class).putBoolean(
                PREFS_KEY_DONT_SHOW_DLG, jCheckBox1.isSelected());
    }//GEN-LAST:event_dontShowDialogChecked

    public static boolean isShowBrokenPlatformDialog() {
        return Boolean.getBoolean ("JCProjectTest") ? false :
            NbPreferences.forModule(BadPlatformOrDevicePanel.class).getBoolean(
                PREFS_KEY_DONT_SHOW_DLG, true);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private org.netbeans.modules.javacard.project.ui.PlatformAndDevicePanel selPanel;
    // End of variables declaration//GEN-END:variables

    public void resultChanged(LookupEvent arg0) {
        JavacardPlatform platform = selPanel.getLookup().lookup(JavacardPlatform.class);
        Card card = selPanel.getLookup().lookup (Card.class);
        if (platform == null || !platform.isValid()) {
            setProblem(NbBundle.getMessage (BadPlatformOrDevicePanel.class,
                    "MSG_BAD_PLATFORM")); //NOI18N
            return;
        }
        if (card == null || !card.isValid()) {
            setProblem(NbBundle.getMessage(BadPlatformOrDevicePanel.class,
                    "MSG_BAD_CARD")); //NOI18N
            return;
        }
        props.setActiveDevice(card.getId());

        String pName = platform.getSystemName();
        if (pName != null) {
            props.setPlatformName(pName);
        }

        setProblem (null);
    }

    public void setProblemHandler(ProblemHandler handler) {
        this.handler = handler;
    }

    private final ChangeSupport supp = new ChangeSupport (this);
    public void addChangeListener(ChangeListener cl) {
        supp.addChangeListener(cl);
    }

    public void removeChangeListener(ChangeListener cl) {
        supp.removeChangeListener(cl);
    }

    private void setProblem(String problem) {
        this.problem = problem;
        if (handler != null) {
            handler.setProblem(problem);
        }
        supp.fireChange();
    }

    public String getProblem() {
        return problem;
    }
}
