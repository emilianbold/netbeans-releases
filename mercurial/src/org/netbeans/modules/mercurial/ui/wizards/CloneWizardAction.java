package org.netbeans.modules.mercurial.ui.wizards;

import java.awt.Component;
import java.awt.Dialog;
import java.text.MessageFormat;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;

import java.util.List;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.netbeans.modules.mercurial.ui.clone.CloneAction;

// An example action demonstrating how the wizard could be called from within
// your code. You can copy-paste the code below wherever you need.
public final class CloneWizardAction extends CallableSystemAction implements ChangeListener {
    
    private WizardDescriptor.Panel[] panels;
    
    private static CloneWizardAction instance;
    private WizardDescriptor wizardDescriptor;
    private CloneRepositoryWizardPanel cloneRepositoryWizardPanel;

    public static synchronized CloneWizardAction getInstance() {
        if (instance == null) {
            instance = new CloneWizardAction();
        }
        return instance;
    }

    public void performAction() {
        wizardDescriptor = new WizardDescriptor(getPanels());
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}")); // NOI18N
        wizardDescriptor.setTitle(org.openide.util.NbBundle.getMessage(CloneWizardAction.class, "CTL_Clone")); // NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            final String repository = (String) wizardDescriptor.getProperty("repository"); // NOI18N
            final String directory = (String) wizardDescriptor.getProperty("directory"); // NOI18N
            RequestProcessor rp = Mercurial.getInstance().getRequestProcessor();
            HgProgressSupport support = new HgProgressSupport() {
                public void perform() {
                    try {
                        List<String> list = HgCommand.doClone(repository, directory);
                        if(list != null && !list.isEmpty()){
                            //HgUtils.createIgnored(directory);
                            HgUtils.outputMercurialTabInRed(
                                    NbBundle.getMessage(CloneAction.class,
                                    "MSG_CLONE_TITLE")); // NOI18N
                            HgUtils.outputMercurialTabInRed(
                                    NbBundle.getMessage(CloneAction.class,
                                    "MSG_CLONE_TITLE_SEP")); // NOI18N
                            HgUtils.outputMercurialTab(list);

                            HgUtils.outputMercurialTabInRed(
                                    NbBundle.getMessage(CloneAction.class,
                                    "MSG_EXTERNAL_CLONE_FROM", repository)); // NOI18N
                            HgUtils.outputMercurialTabInRed(
                                    NbBundle.getMessage(CloneAction.class,
                                    "MSG_EXTERNAL_CLONE_TO", directory)); // NOI18N
                            HgUtils.outputMercurialTab(""); // NOI18N
                        }
                    } catch (HgException ex) {
                        NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
                        DialogDisplayer.getDefault().notifyLater(e);
                    }
                }
            };
            support.start(rp, null, org.openide.util.NbBundle.getMessage(CloneWizardAction.class, "LBL_Clone_Progress")); // NOI18N
        }
    }
    
    public void stateChanged(ChangeEvent e) {
        if(cloneRepositoryWizardPanel==null) {
            return;
        }
        if (wizardDescriptor != null) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", cloneRepositoryWizardPanel.getErrorMessage()); // NOI18N
        }
    }


    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            cloneRepositoryWizardPanel = new CloneRepositoryWizardPanel();
            panels = new WizardDescriptor.Panel[] {
                
                cloneRepositoryWizardPanel, new CloneDestinationDirectoryWizardPanel()
            };
            panels[0].addChangeListener(this);
            String[] steps = new String[panels.length];
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                // Default step name to component name of panel. Mainly useful
                // for getting the name of the target chooser to appear in the
                // list of steps.
                steps[i] = c.getName();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
                    // Sets steps names for a panel
                    jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
                    // Turn on subtitle creation on each step
                    jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE); // NOI18N
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE); // NOI18N
                    // Turn on numbering of all steps
                    jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE); // NOI18N
                }
            }
        }
        return panels;
    }
    
    public String getName() {
        return "Start Sample Wizard"; // NOI18N
    }
    
    public String iconResource() {
        return null;
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
}

