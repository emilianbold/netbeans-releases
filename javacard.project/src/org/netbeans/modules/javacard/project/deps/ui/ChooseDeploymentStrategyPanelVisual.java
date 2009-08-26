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

package org.netbeans.modules.javacard.project.deps.ui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.javacard.project.deps.DependencyKind;
import org.netbeans.modules.javacard.project.deps.DeploymentStrategy;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

final class ChooseDeploymentStrategyPanelVisual extends JPanel implements ItemListener {
    private final ChangeSupport supp = new ChangeSupport(this);
    private static final String CLIENT_PROP_DEP_KIND = "_dependencyKind";
    private final WizardDescriptor wiz;

    ChooseDeploymentStrategyPanelVisual(WizardDescriptor wiz) {
        this.wiz = wiz;
        setLayout (new GridBagLayout());
        setBorder (BorderFactory.createEmptyBorder (12,12,12,12));
    }

    void setDependencyKind(DependencyKind kind) {
        removeAll();
        ButtonGroup grp = new ButtonGroup();
        if (kind == null) {
            return;
        }
        List<DeploymentStrategy> l = new ArrayList<DeploymentStrategy> (kind.supportedDeploymentStrategies());
        Collections.sort(l);
        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.gridy = 0;
        buttonConstraints.fill = GridBagConstraints.BOTH;
        buttonConstraints.anchor = GridBagConstraints.NORTHWEST;
        buttonConstraints.weightx = 1.0F;
        buttonConstraints.weighty = 1.0F;
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridy = 1;
        labelConstraints.fill = GridBagConstraints.HORIZONTAL;
        labelConstraints.anchor = GridBagConstraints.NORTHWEST;
        labelConstraints.insets = new Insets(0, 20, 12, 0);
        labelConstraints.weightx = 1.0F;
        labelConstraints.weighty = 1.0F;
        for (DeploymentStrategy d : l) {
            JRadioButton button = new JRadioButton (d.toString());
            button.putClientProperty(CLIENT_PROP_DEP_KIND, d);
            button.addItemListener(this);
            grp.add(button);
            JLabel desc = new JLabel (d.getDescription());
            add (button, buttonConstraints);
            add (desc, labelConstraints);
            buttonConstraints.gridy +=2;
            labelConstraints.gridy += 2;
        }
        //XXX remove once build-time support for new project metadata is
        //implemented
        labelConstraints.gridy++;
        JLabel lbl = new JLabel(NbBundle.getMessage(ChooseDeploymentStrategyPanelVisual.class, "WARNING_DEPLOYMENT_STRATEGY"));
        add (lbl, labelConstraints);
    }

    public DeploymentStrategy getDeploymentStrategy() {
        for (Component c : getComponents()) {
            if (c instanceof JRadioButton && ((JRadioButton)c).isSelected()) {
                JRadioButton r = (JRadioButton) c;
                return (DeploymentStrategy) r.getClientProperty(CLIENT_PROP_DEP_KIND);
            }
        }
        return null;
    }

    public void removeChangeListener(ChangeListener listener) {
        supp.removeChangeListener(listener);
    }

    public void fireChange() {
        supp.fireChange();
    }

    public void addChangeListener(ChangeListener listener) {
        supp.addChangeListener(listener);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ChooseDeploymentStrategyPanelVisual.class,
                "WIZARD_STEP_CHOOSE_DEPLOYMENT_STRATEGY"); //NOI18N
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    public void itemStateChanged(ItemEvent e) {
        fireChange();
    }

    boolean updating;
    void setDeploymentStrategy(DeploymentStrategy deploymentStrategy) {
        updating = true;
        try {
            for (Component c : getComponents()) {
                if (deploymentStrategy == null) {
                    if (c instanceof JRadioButton) {
                        ((JRadioButton) c).setSelected(false);
                    }
                } else if (c instanceof JRadioButton && deploymentStrategy == ((JRadioButton) c).getClientProperty(CLIENT_PROP_DEP_KIND)) {
                    ((JRadioButton)c).setSelected(true);
                }
            }
        } finally {
            updating = false;
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}

