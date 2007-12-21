/*
 * ReportElementOptionsCategory.java
 * 
 * Created on Oct 15, 2007, 6:53:22 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.reportgenerator.customization;

import org.netbeans.spi.options.OptionsCategory;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;

/**
 *
 * @author radval
 */
public class ReportOptionsCategory extends OptionsCategory {

    @Override
    public String getCategoryName() {
        return NbBundle.getMessage(ReportOptionsCategory.class, "ReportOptionsCategory_CategoryName");
    }

    @Override
    public String getTitle() {
        return NbBundle.getMessage(ReportOptionsCategory.class, "ReportOptionsCategory_Title");
    }

    @Override
    public OptionsPanelController create() {
        return new ReportOptionsPanelController();
    }

}
