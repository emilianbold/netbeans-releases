/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit.wizards;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.junit.GuiUtils;
import org.netbeans.modules.junit.SizeRestrictedPanel;
import org.openide.WizardDescriptor;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  Marian Petras
 */
class TestSuiteStepLocation implements WizardDescriptor.Panel {

    private Component visualComp;
    private LocationPanel locPanel;
    private ChangeListener locPanelListener;
    private List changeListeners;
    private JCheckBox chkSetUp;
    private JCheckBox chkTearDown;
    private JCheckBox chkCodeHints;

    TestSuiteStepLocation() {
        super();
        visualComp = createVisualComp();
    }

    private Component createVisualComp() {
        locPanel = new LocationPanel();
        
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

        /* set layout of the components: */
        JPanel selectionPanel = new SizeRestrictedPanel(new BorderLayout(0, 24), false, true);
        selectionPanel.add(locPanel, BorderLayout.CENTER);
        selectionPanel.add(infoLabel, BorderLayout.SOUTH);
        
        JComponent box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.add(selectionPanel);
        box.add(Box.createVerticalStrut(24));
        box.add(optCode);
        box.add(Box.createVerticalStrut(11));
        box.add(optComments);
        box.add(Box.createVerticalGlue());

        /* tune layout of the components within the box: */
        selectionPanel.setAlignmentX(0.0f);
        optCode.setAlignmentX(0.0f);
        optComments.setAlignmentX(0.0f);

        return box;
    }

    void setProject(Project project) {
        locPanel.setProject(project);
    }

    public void addChangeListener(ChangeListener l) {
        if (changeListeners == null) {
            changeListeners = new ArrayList(4);
            locPanelListener = new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    TestSuiteStepLocation.this.fireChange();
                }
            };
            locPanel.setChangeListener(locPanelListener);
        }
        changeListeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        if (changeListeners != null) {
            if (changeListeners.remove(l) && changeListeners.isEmpty()) {
                changeListeners = null;
                locPanel.setChangeListener(null);
                locPanelListener = null;
            }
        }
    }

    private void fireChange() {
        if (changeListeners == null) {
            return;
        }
        ChangeEvent e = new ChangeEvent(this);
        java.util.Iterator i = changeListeners.iterator();
        while (i.hasNext()) {
            ((ChangeListener) i.next()).stateChanged(e);
        }
    }

    public Component getComponent() {
        return visualComp;
    }

    public HelpCtx getHelp() {
        //PENDING
        return null;
    }

    public boolean isValid() {
        return locPanel.hasValidClassName();
    }

    public void readSettings(Object settings) {
        //PENDING
    }

    public void storeSettings(Object settings) {
        TemplateWizard wizard = (TemplateWizard) settings;
        
        wizard.putProperty(TestSuiteWizard.PROP_PACKAGE,
                           locPanel.getPackage());
        wizard.putProperty(TestSuiteWizard.PROP_CLASS_NAME,
                           locPanel.getClassName());
        
        wizard.putProperty(GuiUtils.CHK_SETUP,
                           Boolean.valueOf(chkSetUp.isSelected()));
        wizard.putProperty(GuiUtils.CHK_TEARDOWN,
                           Boolean.valueOf(chkTearDown.isSelected()));
        wizard.putProperty(GuiUtils.CHK_HINTS,
                           Boolean.valueOf(chkCodeHints.isSelected()));
    }

}
