package org.netbeans.modules.iep.editor.designer.actions;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;

import java.text.MessageFormat;
import javax.swing.AbstractAction;
import javax.swing.Action;

import javax.swing.JComponent;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.iep.editor.PlanDataObject;
import org.netbeans.modules.iep.editor.wizard.IEPWizardHelper;
import org.netbeans.modules.iep.editor.wizard.IEPWizardIterator;
import org.netbeans.modules.iep.editor.wizard.IEPWizardPanel2;
import org.netbeans.modules.iep.editor.wizard.IEPWizardPanel3;
import org.netbeans.modules.iep.model.IEPModel;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

public class IEPTemplateAction extends AbstractAction {

        private WizardDescriptor.Panel[] panels;
        
        private PlanDataObject mDataObject;
    public IEPTemplateAction(PlanDataObject dataObject) {
            this.mDataObject = dataObject;
            this.putValue(Action.NAME, NbBundle.getMessage(IEPTemplateAction.class, "IEPTemplateAction_NAME"));
            this.putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(IEPTemplateAction.class, "IEPTemplateAction_SHORT_DESCRIPTION"));
                
    }
    
    public void actionPerformed(ActionEvent e) {
            WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels());
            // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
            wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
            wizardDescriptor.setTitle(NbBundle.getMessage(IEPWizardIterator.class, "IEPVisualPanel2_title"));
            Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
            dialog.setVisible(true);
            dialog.toFront();
            boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
            if (!cancelled) {
                //if user has provided attribute list
                //use it and create a stream input.
                IEPModel model = mDataObject.getPlanEditorSupport().getModel();
                IEPWizardHelper.processUsingExistingSchema(model, wizardDescriptor);
        
            }
        
    }
        
        
        /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        //if panels are null first initialize each different
        //wizard paths
        Project project = FileOwnerQuery.getOwner(mDataObject.getPrimaryFile());
        
        if (panels == null) {
            panels = new WizardDescriptor.Panel[]{
                new IEPWizardPanel3(project)
            };
        
            String[] steps = createSteps();
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                if (steps[i] == null) {
                    // Default step name to component name of panel. Mainly
                    // useful for getting the name of the target chooser to
                    // appear in the list of steps.
                    steps[i] = c.getName();
                }
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i));
                    // Sets steps names for a panel
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE);
                }
            }
        }
        
        return panels;
    }
        
        // You could safely ignore this method. Is is here to keep steps which were
    // there before this wizard was instantiated. It should be better handled
    // by NetBeans Wizard API itself rather than needed to be implemented by a
    // client code.
    private String[] createSteps() {
        String[] res = new String[panels.length];
        for (int i = 0; i < res.length; i++) {
                res[i] = panels[i].getComponent().getName();
        }
        return res;
    }

}
