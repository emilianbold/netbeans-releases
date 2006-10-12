package org.netbeans.modules.xml.wsdl.ui.wizard;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.wsdl.ui.view.OperationType;
import org.netbeans.modules.xml.wsdl.ui.view.PartAndElementOrTypeTableModel;
import org.netbeans.modules.xml.wsdl.ui.view.PortTypeConfigurationPanel;
import org.openide.WizardDescriptor;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author radval
 */
public class WizardNewWSDLStep implements WizardDescriptor.FinishablePanel {
    private WizardDescriptor.Panel mPanel;

    
    public WizardNewWSDLStep(WizardDescriptor.Panel panel) {
        this.mPanel = panel;
    }
    
    public boolean isFinishPanel() {
        return true;
    }

    public Component getComponent() {
        return mPanel.getComponent();
    }

    public HelpCtx getHelp() {
        return mPanel.getHelp();
    }

    public void readSettings(Object settings) {
        mPanel.readSettings(settings);
    }

    public void storeSettings(Object settings) {
    	//hack calling mPanel.storeSettings null out the title
    	String title = (String) ((WizardDescriptor)settings).getProperty ("NewFileWizard_Title"); // NOI18N
        mPanel.storeSettings(settings);
        ((WizardDescriptor)settings).putProperty ("NewFileWizard_Title", title); // NOI18N
    }

    public boolean isValid() {
        return mPanel.isValid();
    }

    public void addChangeListener(ChangeListener l) {
        mPanel.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        mPanel.removeChangeListener(l);
    }
    
}
