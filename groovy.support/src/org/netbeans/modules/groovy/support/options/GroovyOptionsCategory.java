package org.netbeans.modules.groovy.support.options;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.spi.options.OptionsCategory;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public final class GroovyOptionsCategory extends OptionsCategory {

    @Override
    public Icon getIcon() {
        return new ImageIcon(Utilities.loadImage("org/netbeans/modules/groovy/support/resources/groovy-options.png")); // NOI18N
    }

    public String getCategoryName() {
        return NbBundle.getMessage(GroovyOptionsCategory.class, "OptionsCategory_Name_Groovy");
    }

    public String getTitle() {
        return NbBundle.getMessage(GroovyOptionsCategory.class, "OptionsCategory_Title_Groovy");
    }

    public OptionsPanelController create() {
        return new GroovyPanelController();
    }
}