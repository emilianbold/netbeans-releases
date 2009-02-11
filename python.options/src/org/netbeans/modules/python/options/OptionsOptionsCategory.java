/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.python.options;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.spi.options.OptionsCategory;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public final class OptionsOptionsCategory extends OptionsCategory {

    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/netbeans/modules/python/options/py_25_32.png", false);
    }

    public String getCategoryName() {
        return NbBundle.getMessage(OptionsOptionsCategory.class, "OptionsCategory_Name_Options");
    }

    public String getTitle() {
        return NbBundle.getMessage(OptionsOptionsCategory.class, "OptionsCategory_Title_Options");
    }

    public OptionsPanelController create() {
        return new OptionsOptionsPanelController();
    }
}
