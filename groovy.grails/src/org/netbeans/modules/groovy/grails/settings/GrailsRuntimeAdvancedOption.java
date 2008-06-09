package org.netbeans.modules.groovy.grails.settings;

import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;

public final class GrailsRuntimeAdvancedOption extends AdvancedOption {

    public String getDisplayName() {
        return NbBundle.getMessage(GrailsRuntimeAdvancedOption.class, "AdvancedOption_DisplayName_GrailsRuntime");
    }

    public String getTooltip() {
        return NbBundle.getMessage(GrailsRuntimeAdvancedOption.class, "AdvancedOption_Tooltip_GrailsRuntime");
    }

    public OptionsPanelController create() {
        return new GrailsRuntimeOptionsPanelController();
    }
}