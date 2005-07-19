/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit.wizards;

import java.awt.Component;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.junit.GuiUtils;
import org.netbeans.modules.junit.SelfResizingPanel;
import org.openide.WizardDescriptor;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  Marian Petras
 */
class EmptyTestStepLocation implements WizardDescriptor.Panel {

    private Component visualComp;
    private JCheckBox chkSetUp;
    private JCheckBox chkTearDown;
    private JCheckBox chkCodeHints;

    EmptyTestStepLocation() {
        super();
        visualComp = createVisualComp();
    }

    private Component createVisualComp() {
        JCheckBox[] chkBoxes;
        
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

        JComponent box = new SelfResizingPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.add(optCode);
        box.add(Box.createVerticalStrut(11));
        box.add(optComments);

        /* tune layout of the components within the box: */
        optCode.setAlignmentX(0.0f);
        optComments.setAlignmentX(0.0f);

        return box;
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

    public void readSettings(Object settings) {
        TemplateWizard wizard = (TemplateWizard) settings;
        
        chkSetUp.setSelected(
                Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_SETUP)));
        chkTearDown.setSelected(
                Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_TEARDOWN)));
        chkCodeHints.setSelected(
                Boolean.TRUE.equals(wizard.getProperty(GuiUtils.CHK_HINTS)));
    }

    public void storeSettings(Object settings) {
        TemplateWizard wizard = (TemplateWizard) settings;
        
        wizard.putProperty(GuiUtils.CHK_SETUP,
                           Boolean.valueOf(chkSetUp.isSelected()));
        wizard.putProperty(GuiUtils.CHK_TEARDOWN,
                           Boolean.valueOf(chkTearDown.isSelected()));
        wizard.putProperty(GuiUtils.CHK_HINTS,
                           Boolean.valueOf(chkCodeHints.isSelected()));
    }

}
