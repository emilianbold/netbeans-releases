/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit.wizards;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.junit.GuiUtils;
import org.netbeans.modules.junit.SelfResizingPanel;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  Marian Petras
 */
class TestSuiteStepLocation implements WizardDescriptor.Panel<WizardDescriptor> {

    private Component visualComp;
    private JCheckBox chkSetUp;
    private JCheckBox chkTearDown;
    private JCheckBox chkCodeHints;

    TestSuiteStepLocation() {
        super();
        visualComp = createVisualComp();
    }

    private Component createVisualComp() {
        JCheckBox[] chkBoxes;
        
        JComponent infoLabel = GuiUtils.createMultilineLabel(
                NbBundle.getMessage(TestSuiteStepLocation.class,
                                    "TXT_ClassesInSuite"));             //NOI18N
        JComponent optCode = GuiUtils.createChkBoxGroup(
                NbBundle.getMessage(
                        GuiUtils.class,
                        "JUnitCfgOfCreate.groupOptCode"),               //NOI18N
                chkBoxes = GuiUtils.createCheckBoxes(new String[] {
                        GuiUtils.CHK_SETUP,
                        GuiUtils.CHK_TEARDOWN}));
        chkSetUp = chkBoxes[0];
        chkTearDown = chkBoxes[1];
        
        JComponent optComments = GuiUtils.createChkBoxGroup(
                NbBundle.getMessage(
                        GuiUtils.class,
                        "JUnitCfgOfCreate.groupOptComments"),           //NOI18N
                chkBoxes = GuiUtils.createCheckBoxes(new String[] {
                        GuiUtils.CHK_HINTS}));
        chkCodeHints = chkBoxes[0];

        JComponent bottomPanel = new SelfResizingPanel();
        bottomPanel.setLayout(new BorderLayout(0, 24));
        bottomPanel.add(infoLabel, BorderLayout.NORTH);
        JComponent box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.X_AXIS));
        box.add(optCode);
        box.add(Box.createHorizontalStrut(18));
        box.add(optComments);
        bottomPanel.add(box, BorderLayout.CENTER);
        
        /* tune layout of the components within the box: */
        infoLabel.setAlignmentX(0.0f);
        optCode.setAlignmentY(0.0f);
        optComments.setAlignmentY(0.0f);
     
        return bottomPanel;
    }

    public void addChangeListener(ChangeListener l) {
        // no listeners needed - the panel is always valid
    }

    public void removeChangeListener(ChangeListener l) {
        // no listeners needed - the panel is always valid
    }

    public Component getComponent() {
        return visualComp;
    }

    public HelpCtx getHelp() {
        //PENDING
        return null;
    }

    public boolean isValid() {
        return true;
    }

    public void readSettings(WizardDescriptor settings) {
        chkSetUp.setSelected(
                Boolean.TRUE.equals(settings.getProperty(GuiUtils.CHK_SETUP)));
        chkTearDown.setSelected(
                Boolean.TRUE.equals(settings.getProperty(GuiUtils.CHK_TEARDOWN)));
        chkCodeHints.setSelected(
                Boolean.TRUE.equals(settings.getProperty(GuiUtils.CHK_HINTS)));
    }

    public void storeSettings(WizardDescriptor settings) {
        settings.putProperty(GuiUtils.CHK_SETUP,
                           Boolean.valueOf(chkSetUp.isSelected()));
        settings.putProperty(GuiUtils.CHK_TEARDOWN,
                           Boolean.valueOf(chkTearDown.isSelected()));
        settings.putProperty(GuiUtils.CHK_HINTS,
                           Boolean.valueOf(chkCodeHints.isSelected()));
    }

}
