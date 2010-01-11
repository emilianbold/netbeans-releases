package org.mycompany;

import java.util.List;
import org.netbeans.installer.product.components.ProductConfigurationLogic;
import org.netbeans.installer.utils.helper.RemovalMode;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.Wizard;
import org.netbeans.installer.wizard.components.WizardComponent;

public class ConfigurationLogic extends ProductConfigurationLogic {

    private List<WizardComponent> wizardComponents;

    // constructor //////////////////////////////////////////////////////////////////
    public ConfigurationLogic() throws InitializationException {
        wizardComponents = Wizard.loadWizardComponents(
                WIZARD_COMPONENTS_URI,
                getClass().getClassLoader());
    }

    public List<WizardComponent> getWizardComponents() {
        return wizardComponents;
    }

    @Override
    public boolean allowModifyMode() {
        return false;
    }

    @Override
    public void install(Progress progress) throws InstallationException {
        
    }
    @Override
    public void uninstall(Progress progress) throws UninstallationException {
        
    }
    public RemovalMode getRemovalMode() {
        return RemovalMode.LIST;
    }

    @Override
    public boolean registerInSystem() {
        return true;
    }
    
    public static final String WIZARD_COMPONENTS_URI =
            "resource:" + // NOI18N
            "org/mycompany/wizard.xml"; // NOI18N

}
