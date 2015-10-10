package org.mycompany.installer.wizard.components.actions;

import org.netbeans.installer.product.Registry;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.components.WizardAction;
import org.netbeans.installer.wizard.components.actions.*;

public class InitializeAction extends WizardAction {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public InitializeAction() {
        setProperty(TITLE_PROPERTY, 
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY, 
                DEFAULT_DESCRIPTION);

        downloadLogic = new DownloadConfigurationLogicAction();
        initReg = new InitializeRegistryAction();
    }
    private DownloadConfigurationLogicAction downloadLogic;
    private InitializeRegistryAction initReg;
    
    public void execute() {
        final Progress progress = new Progress();
        
        //getWizardUi().setProgress(progress);
        

        progress.setTitle(getProperty(PROGRESS_TITLE_PROPERTY));

        //progress.synchronizeDetails(false);

        if (initReg.canExecuteForward()) {
            initReg.setWizard(getWizard());
            initReg.execute();
        }
    
        if (downloadLogic.canExecuteForward()) {
            downloadLogic.setWizard(getWizard());
            downloadLogic.execute();
        }
    }
    
    @Override
    public boolean isCancelable() {
        return false;
    }

    public WizardActionUi getWizardUi() {
        return null; // this action does not have a ui
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_TITLE = ResourceUtils.getString(
            InitializeAction.class, 
            "IA.title"); // NOI18N
    public static final String PROGRESS_TITLE_PROPERTY = ResourceUtils.getString(
            InitializeAction.class, 
            "IA.progress.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION = ResourceUtils.getString(
            InitializeAction.class, 
            "IA.description"); // NOI18N
    
}
