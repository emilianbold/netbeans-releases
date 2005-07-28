/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.dbschema.jdbcimpl.wizard;

import java.io.IOException;
import java.util.*;

import javax.swing.event.ChangeListener;

import org.openide.loaders.TemplateWizard;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.WizardDescriptor;

/** Iterator implementation which can iterate through two
* panels which forms dbschema template wizard
*/
public class DBSchemaWizardIterator implements TemplateWizard.Iterator {

    static final long serialVersionUID = 9197272899287477324L;

    ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.dbschema.jdbcimpl.resources.Bundle"); //NOI18N

    private WizardDescriptor.Panel panels[];
    private static String panelNames[];
    private static final int PANEL_COUNT = 3;
    private int panelIndex;
    private static DBSchemaWizardIterator instance;
    private TemplateWizard wizardInstance;
    private boolean guiInitialized;
    private DBSchemaWizardData myData;

    public DBSchemaWizardIterator() {
        super();
        panelIndex = 0;
    }

    public static synchronized DBSchemaWizardIterator singleton() {
        if(instance == null)
            instance = new DBSchemaWizardIterator();

        return instance;
    }

    public Set instantiate(TemplateWizard wiz) throws IOException {
        myData.setName(wiz.getTargetName());
        myData.setDestinationPackage(wiz.getTargetFolder());
        
        CaptureSchema capture = new CaptureSchema(myData);
        capture.start();
        
        return null;///Collections.singleton(null);
    }

    public org.openide.WizardDescriptor.Panel current() {
        return panels[panelIndex];
    }

    public String name() {
        return panelNames[panelIndex];
    }

    public boolean hasNext() {
        return panelIndex < PANEL_COUNT - 1;
    }

    public boolean hasPrevious() {
        return panelIndex > 0;
    }

    public void nextPanel() {
        if (panelIndex == 1) {//== connection panel
            ((DBSchemaConnectionPanel) panels[1].getComponent()).initData();
            if (! (((DBSchemaTablesPanel) panels[2].getComponent()).init()))
                return;
        }
        
        panelIndex++;
    }

    public void previousPanel() {
        panelIndex--;
    }

    public void addChangeListener(ChangeListener l) {
    }

    public void removeChangeListener(ChangeListener l) {
    }

    public void initialize(TemplateWizard wizard) {
        wizardInstance = wizard;
        String[] prop = (String[]) wizard.getProperty("WizardPanel_contentData"); // NOI18N
        String[] stepsNames = new String[] {
            wizard.targetChooser().getClass().toString().trim().equalsIgnoreCase("class org.openide.loaders.TemplateWizard2") ? bundle.getString("TargetLocation") :
                prop[0],
                bundle.getString("TargetLocation"),
                bundle.getString("ConnectionChooser"),
                bundle.getString("TablesChooser")
        };
        wizardInstance.putProperty("WizardPanel_autoWizardStyle", Boolean.TRUE); //NOI18N
        wizardInstance.putProperty("WizardPanel_contentDisplayed", Boolean.TRUE); //NOI18N
        wizardInstance.putProperty("WizardPanel_contentNumbered", Boolean.TRUE); //NOI18N
        wizardInstance.putProperty("WizardPanel_contentData", stepsNames); //NOI18N
        
        if(!guiInitialized) {
            initialize();
            
            myData = new DBSchemaWizardData();
            panels = new WizardDescriptor.Panel[PANEL_COUNT];            
            
            DBSchemaTargetPanel targetPanel = new DBSchemaTargetPanel();
            targetPanel.setPanel(wizard.targetChooser());

            java.awt.Component panel = targetPanel.getComponent();
            if (panel instanceof javax.swing.JComponent) {
                ((javax.swing.JComponent) panel).putClientProperty("WizardPanel_contentData", stepsNames); //NOI18N
                ((javax.swing.JComponent) panel).putClientProperty("WizardPanel_contentSelectedIndex", new Integer(0)); //NOI18N
            }
            
            panels[0] = targetPanel.getPanel();
            panels[1] = new DBSchemaConnectionWizardPanel(myData);
            panels[2] = new DBSchemaTablesWizardPanel(myData);
        }
        
        panelIndex = 0;
    }

    public void uninitialize(TemplateWizard wiz) {
        if (wiz.getValue() == NotifyDescriptor.CANCEL_OPTION)
            ((DBSchemaTablesPanel) panels[2].getComponent()).uninit();
        
        panels = null;
        myData = null;
        guiInitialized = false;
    }

    protected void initialize() {
        if(panelNames == null) {
            panelNames = new String[PANEL_COUNT];
            panelNames[0] = ""; //NOI18N
            panelNames[1] = ""; //NOI18N
            panelNames[2] = ""; //NOI18N
        }
    }

}
