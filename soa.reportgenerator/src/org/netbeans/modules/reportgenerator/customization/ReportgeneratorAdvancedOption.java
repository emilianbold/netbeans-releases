package org.netbeans.modules.reportgenerator.customization;

import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;

public final class ReportgeneratorAdvancedOption extends AdvancedOption {

    public String getDisplayName() {
        return NbBundle.getMessage(ReportgeneratorAdvancedOption.class, "AdvancedOption_DisplayName_Reportgenerator");
    }

    public String getTooltip() {
        return NbBundle.getMessage(ReportgeneratorAdvancedOption.class, "AdvancedOption_Tooltip_Reportgenerator");
    }

    public OptionsPanelController create() {
        return new ReportgeneratorOptionsPanelController();
    }
}
