/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.symfony.ui.wizards;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.modules.php.api.util.StringUtils;
import org.openide.awt.Mnemonics;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public class NewProjectConfigurationPanel extends JPanel {
    private static final long serialVersionUID = -178501081182418594L;
    private static final Pattern APP_PATTERN = Pattern.compile("\\S+"); // NOI18N

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    public NewProjectConfigurationPanel() {
        initComponents();

        DefaultItemListener defaultItemListener = new DefaultItemListener();
        frontendCheckBox.addItemListener(defaultItemListener);
        backendCheckBox.addItemListener(defaultItemListener);
        otherAppsCheckBox.addItemListener(defaultItemListener);
        otherAppsCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                enableOtherAppsTextField();
            }
        });
        otherAppsTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                processUpdate();
            }
            public void removeUpdate(DocumentEvent e) {
                processUpdate();
            }
            public void changedUpdate(DocumentEvent e) {
                processUpdate();
            }
            private void processUpdate() {
                fireChange();
            }
        });
        otherAppsTextField.addPropertyChangeListener("enabled", new PropertyChangeListener() { // NOI18N
            public void propertyChange(PropertyChangeEvent evt) {
                if (otherAppsCheckBox.isEnabled()) {
                    enableOtherAppsTextField();
                }
            }
        });
        otherAppsInfoLabel.addPropertyChangeListener("enabled", new PropertyChangeListener() { // NOI18N
            public void propertyChange(PropertyChangeEvent evt) {
                // always disabled label
                if (otherAppsInfoLabel.isEnabled()) {
                    otherAppsInfoLabel.setEnabled(false);
                }
            }
        });
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public List<String> getApps() {
        List<String> apps = new LinkedList<String>();
        if (frontendCheckBox.isSelected()) {
            apps.add("frontend"); // NOI18N
        }
        if (backendCheckBox.isSelected()) {
            apps.add("backend"); // NOI18N
        }
        apps.addAll(getOtherApps());
        return apps;
    }

    public String validateData() {
        for (String app : getOtherApps()) {
            if (!APP_PATTERN.matcher(app).matches()) {
                return NbBundle.getMessage(NewProjectConfigurationPanel.class, "MSG_InvalidAppName", app);
            }
        }
        return null;
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    void enableOtherAppsTextField() {
        otherAppsTextField.setEnabled(otherAppsCheckBox.isSelected());
    }

    private List<String> getOtherApps() {
        if (!otherAppsCheckBox.isSelected()) {
            return Collections.emptyList();
        }
        List<String> exploded = StringUtils.explode(otherAppsTextField.getText(), ","); // NOI18N
        List<String> otherApps = new ArrayList<String>(exploded.size());
        for (String app : exploded) {
            if (StringUtils.hasText(app)) {
                otherApps.add(app.trim());
            }
        }
        return otherApps;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        generateAppLabel = new JLabel();
        frontendCheckBox = new JCheckBox();
        backendCheckBox = new JCheckBox();
        otherAppsCheckBox = new JCheckBox();
        otherAppsTextField = new JTextField();
        otherAppsInfoLabel = new JLabel();

        generateAppLabel.setLabelFor(frontendCheckBox);

        Mnemonics.setLocalizedText(generateAppLabel, NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.generateAppLabel.text")); // NOI18N
        frontendCheckBox.setSelected(true);



        Mnemonics.setLocalizedText(frontendCheckBox, NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.frontendCheckBox.text")); // NOI18N
        Mnemonics.setLocalizedText(backendCheckBox, NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.backendCheckBox.text"));
        Mnemonics.setLocalizedText(otherAppsCheckBox, NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.otherAppsCheckBox.text"));
        Mnemonics.setLocalizedText(otherAppsInfoLabel, NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.otherAppsInfoLabel.text"));
        otherAppsInfoLabel.setEnabled(false);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(generateAppLabel)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(frontendCheckBox))
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(backendCheckBox))
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(otherAppsCheckBox)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(0, 0, 0)
                        .add(otherAppsInfoLabel))
                    .add(otherAppsTextField, GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(generateAppLabel)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(frontendCheckBox)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(backendCheckBox)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(otherAppsTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(otherAppsCheckBox))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(otherAppsInfoLabel))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JCheckBox backendCheckBox;
    private JCheckBox frontendCheckBox;
    private JLabel generateAppLabel;
    private JCheckBox otherAppsCheckBox;
    private JLabel otherAppsInfoLabel;
    private JTextField otherAppsTextField;
    // End of variables declaration//GEN-END:variables

    private final class DefaultItemListener implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            fireChange();
        }
    }
}
