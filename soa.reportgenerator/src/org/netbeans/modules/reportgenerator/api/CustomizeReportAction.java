package org.netbeans.modules.reportgenerator.api;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.reportgenerator.generator.DataObjectSettings;
import org.netbeans.modules.reportgenerator.customization.ReportCustomizationPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;

import org.openide.loaders.DataObject;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Action which provides report generation customization option.
 * An editor can add this action to its tool bar to get report
 * generation customization option.
 * 
 * Typical options may include generate verbose report. Select report
 * generation type (pdf/html etc)
 * 
 * @author radval
 *
 */
public class CustomizeReportAction extends AbstractAction {

	public static final String ACCELERATOR = "alt shift F11"; // NOI18N
	
	private static final Icon icon = ImageUtilities.loadImageIcon("org/netbeans/modules/reportgenerator/api/impl/resources/images/customizeReport.png", false); 
	
	private static final String label = NbBundle.getMessage(
    GenerateReportAction.class,"NAME_Customize_Report");
       
        private DataObject mDataObject;
        
        public CustomizeReportAction(DataObject dataObject) {
            super(label, icon); 
            this.mDataObject = dataObject;
            putValue(NAME, label);
	    putValue(SHORT_DESCRIPTION, label);
	    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(ACCELERATOR));
        }
        
	public void actionPerformed(ActionEvent e) {
            
            //OptionsDisplayer.getDefault().open("ReportElementCustomizationPanel"); //NOI18N
            //OptionsDisplayer.getDefault().open("Advanced"); //NOI18N
            
            ReportCustomizationOptions options = DataObjectSettings.getOrStoreOptions(mDataObject);
            ReportCustomizationPanel panel = new ReportCustomizationPanel(options);
            panel.setPreferredSize(new Dimension(300, 300));
            
            String title = NbBundle.getMessage(CustomizeReportAction.class, "NAME_Customize_Report");
            DialogDescriptor dd = new DialogDescriptor(panel, title, true, DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION, null);
            if(DialogDisplayer.getDefault().notify(dd) == DialogDescriptor.OK_OPTION) {
                options.setGenerateVerboseReport(panel.isGenerateVerboseReport());
                options.setIncludeOnlyElementsWithDocumentation(panel.isIncludeOnlyElementsWithDocumentation());
            }
	}
        
}
