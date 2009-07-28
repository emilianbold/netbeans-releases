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
package org.netbeans.modules.dlight.management.ui.spi.impl;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.netbeans.modules.dlight.api.execution.DLightTarget;
import org.netbeans.modules.dlight.api.execution.Validateable;
import org.netbeans.modules.dlight.api.execution.ValidationListener;
import org.netbeans.modules.dlight.api.execution.ValidationStatus;
import org.netbeans.modules.dlight.api.support.ValidateableSupport;
import org.netbeans.modules.dlight.api.tool.DLightConfiguration;
import org.netbeans.modules.dlight.api.tool.DLightTool;
import org.netbeans.modules.dlight.spi.collector.DataCollector;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.UIThread;
import org.netbeans.modules.dlight.util.UIUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author mt154047
 */
class EmptyDetailsViewPanel extends JPanel implements ValidationListener {

    private final DLightConfiguration configuration;
    private final DLightTool currentTool;
    private final DLightTarget targetToValidateWith;
    private final Map<Validateable<DLightTarget>, ValidationStatus> states = new HashMap<Validateable<DLightTarget>, ValidationStatus>();
    private final Map<Validateable<DLightTarget>, Integer> panels = new HashMap<Validateable<DLightTarget>, Integer>();
    private final List<JPanel> panelsList;

    public EmptyDetailsViewPanel(DLightConfiguration dlightConfiguration, DLightTool tool, DLightTarget targetToValidateWith) {
        //find the proper tool
        this.configuration = dlightConfiguration;
        this.targetToValidateWith = targetToValidateWith;
        this.currentTool = tool;
        List<DataCollector<?>> collectors = configuration.getConfigurationOptions(false).getCollectors(currentTool);
        List<DataCollector<?>> toRepairList = new ArrayList<DataCollector<?>>();
        panelsList = new ArrayList<JPanel>();
        //get the first one
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel repairPanel = new JPanel();
        repairPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        repairPanel.setLayout(new BoxLayout(repairPanel, BoxLayout.Y_AXIS));
        if (collectors == null || collectors.size() == 0) {
            JPanel p = new JPanel();
            p.setBorder(new EmptyBorder(10, 10, 10, 10));
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            JEditorPane editorPane = UIUtilities.createJEditorPane(NbBundle.getMessage(EmptyDetailsViewPanel.class, "NoCollectorsFound"), true);//NOI18N
            p.add(editorPane);
            repairPanel.add(p);
            repairPanel.add(Box.createVerticalGlue());

        } else {
            for (final Validateable<DLightTarget> c : collectors) {
                if (c.getValidationStatus() == ValidationStatus.initialStatus()) {
                    c.addValidationListener(this);
                    //validate one more time
                    states.put(c, c.getValidationStatus());
                    JPanel p = new JPanel();
                    p.setBorder(new EmptyBorder(10, 10, 10, 10));
                    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
                    JEditorPane label = UIUtilities.createJEditorPane(NbBundle.getMessage(EmptyDetailsViewPanel.class, "Validating"), true);//NOI18N
                    p.add(label);
                    repairPanel.add(p);
                    repairPanel.add(Box.createVerticalGlue());
                    panelsList.add(p);
                    panels.put(c, panelsList.indexOf(p));
                    repair(c);
                } else if (!c.getValidationStatus().isKnown()) {
                    c.addValidationListener(this);
                    //validate one more time
                    states.put(c, c.getValidationStatus());
                    JPanel p = new JPanel();
                    p.setBorder(new EmptyBorder(10, 10, 10, 10));
                    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
                    JEditorPane label = UIUtilities.createJEditorPane(c.getValidationStatus().getReason(), false);//NOI18N
                    Dimension d = label.getPreferredSize();
                    label.setMaximumSize(new Dimension(d.width + 10, d.height));
                    label.setAlignmentX(Component.CENTER_ALIGNMENT);
                    p.add(label);
                    p.add(Box.createVerticalStrut(10));
                    JButton repairButton = new JButton(NbBundle.getMessage(EmptyDetailsViewPanel.class, "Repair"));//NOI18N
                    repairButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                    repairButton.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            repair(c);
                        }
                    });
                    p.add(repairButton);
                    p.add(Box.createVerticalGlue());
//                    p.add(Box.createRigidArea(new Dimension(20, 10)));
                    repairPanel.add(p);
                    repairPanel.add(Box.createVerticalGlue());
                    panelsList.add(p);
                    panels.put(c, panelsList.indexOf(p));

                } else {
                    ValidationStatus status = c.getValidationStatus();
                    JPanel p = new JPanel();
                    p.setBorder(new EmptyBorder(10, 10, 10, 10));
                    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
                    if (!status.isKnown()) {
                        p.add(UIUtilities.createJEditorPane(status.getReason(), false));//NOI18N
                    } else if (status.isValid()) {
                        String message = NbBundle.getMessage(EmptyDetailsViewPanel.class, "NextRun");//NOI18N
                        if (!configuration.getConfigurationOptions(false).areCollectorsTurnedOn()) {
                            message = NbBundle.getMessage(EmptyDetailsViewPanel.class, "DataCollectorDisabled");//NOI18N
                        }
                        p.add(UIUtilities.createJEditorPane(message, true));
                    } else if (status.isInvalid()) {
                        JEditorPane editorPane = UIUtilities.createJEditorPane(status.getReason(), false);//NOI18N
                        p.add(editorPane);
                    }
                    repairPanel.add(p);
                    repairPanel.add(Box.createVerticalGlue());
                    panelsList.add(p);
                    panels.put(c, panelsList.indexOf(p));
                }

            }
        }
        repairPanel.setAlignmentX(CENTER_ALIGNMENT);
        repairPanel.setAlignmentY(CENTER_ALIGNMENT);
        this.add(repairPanel);
        if (!toRepairList.isEmpty()) {
            for (DataCollector<?> c : toRepairList) {
                repair(c);
            }
        }
    }

    private void repair(final Validateable<DLightTarget> c) {
        final ValidateableSupport<DLightTarget> support = new ValidateableSupport<DLightTarget>(c);
        final Future<ValidationStatus> taskStatus = support.asyncValidate(targetToValidateWith, true);
        DLightExecutorService.submit(new Callable<Boolean>() {

            public Boolean call() throws Exception {
                ValidationStatus status = taskStatus.get();
                UIThread.invoke(new Runnable() {

                    public void run() {
                        updateUI(c);
                    }
                });
                return status.isKnown();
            }
        }, "EmptyDetailsViewPanel task");//NOI18N
    }

    private void updateUI(final Validateable<DLightTarget> v) {
        //get panel
        JPanel p = panelsList.get(panels.get(v));
        //we should renove all and set new message
        p.removeAll();
        final ValidationStatus status = v.getValidationStatus();
        if (!status.isKnown()) {
            JEditorPane label = UIUtilities.createJEditorPane(status.getReason(), false);//NOI18N
            Dimension d = label.getPreferredSize();
            label.setMaximumSize(new Dimension(d.width + 10, d.height));
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            p.add(label);
            p.add(Box.createVerticalStrut(10));
            JButton repairButton = new JButton(NbBundle.getMessage(EmptyDetailsViewPanel.class, "Repair"));//NOI18N
            repairButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            repairButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    repair(v);
                }
            });
            p.add(repairButton);
            p.add(Box.createVerticalGlue());
            p.repaint();
            repaint();
            return;
        }
        if (status.isValid()) {
            String message = NbBundle.getMessage(EmptyDetailsViewPanel.class, "NextRun");//NOI18N
            if (!configuration.getConfigurationOptions(false).areCollectorsTurnedOn()) {
                message = NbBundle.getMessage(EmptyDetailsViewPanel.class, "DataCollectorDisabled");//NOI18N
            }
            p.add(UIUtilities.createJEditorPane(message, true));
            p.repaint();
            repaint();
            return;
        }
        if (status.isInvalid()) {
            p.add(UIUtilities.createJEditorPane(status.getReason(), false));
            p.repaint();
            repaint();
            return;

        }
    }

    private void updateAllUI() {
        //get panel
        for (final Validateable<DLightTarget> v : states.keySet()) {
            JPanel p = panelsList.get(panels.get(v));
            //we should renove all and set new message
            p.removeAll();
            ValidationStatus status = v.getValidationStatus();
            if (!status.isKnown()) {
                JEditorPane label = UIUtilities.createJEditorPane(status.getReason(), false);//NOI18N
                Dimension d = label.getPreferredSize();
                label.setMaximumSize(new Dimension(d.width + 10, d.height));
                label.setAlignmentX(Component.CENTER_ALIGNMENT);
                p.add(label);
                p.add(Box.createVerticalStrut(10));
                JButton repairButton = new JButton(NbBundle.getMessage(EmptyDetailsViewPanel.class, "Repair"));//NOI18N
                repairButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                repairButton.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        repair(v);
                    }
                });
                p.add(repairButton);
                p.add(Box.createVerticalGlue());
                p.repaint();
                repaint();
            } else if (status.isValid()) {
                String message = NbBundle.getMessage(EmptyDetailsViewPanel.class, "NextRun");//NOI18N
                if (!configuration.getConfigurationOptions(false).areCollectorsTurnedOn()) {
                    message = NbBundle.getMessage(EmptyDetailsViewPanel.class, "DataCollectorDisabled");//NOI18N
                }
                p.add(UIUtilities.createJEditorPane(message, true));
            } else if (status.isInvalid()) {
                p.add(UIUtilities.createJEditorPane(status.getReason(), false));//NOI18N
            }
            p.repaint();
        }
        repaint();
    }

    public void validationStateChanged(final Validateable source, ValidationStatus oldStatus, ValidationStatus newStatus) {
        if (states.get(source) == null) {//but we need to update
            UIThread.invoke(new Runnable() {

                public void run() {
                    updateAllUI();
                }
            });
            return;//nothing to do
        }
        UIThread.invoke(new Runnable() {

            @SuppressWarnings("unchecked")
            public void run() {
                updateUI(source);
            }
        });
    }
}
