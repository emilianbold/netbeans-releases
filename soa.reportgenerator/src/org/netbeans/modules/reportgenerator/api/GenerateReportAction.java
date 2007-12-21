package org.netbeans.modules.reportgenerator.api;

import org.netbeans.modules.reportgenerator.generator.ReportGeneratorFactory;
import org.netbeans.modules.reportgenerator.spi.ReportCookie;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import org.netbeans.modules.reportgenerator.generator.DataObjectSettings;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Action which provides report generation facility.
 * An editor can add this action to its tool bar to get report
 * generation functionality.
 * 
 * @author radval
 *
 */
public class GenerateReportAction extends AbstractAction {

    public static final String ACCELERATOR = "alt shift F10"; // NOI18N
    
    private static final Icon icon = new ImageIcon(Utilities.loadImage(
    "org/netbeans/modules/reportgenerator/api/impl/resources/images/generateReport.png")); 
    
    private static final String label = NbBundle.getMessage(
    GenerateReportAction.class,"NAME_Generate_Report");

    private ReportGenerator mGenerator;
    private DataObject mDataObject;
    private ReportCookie myCookie;
    private File mReportFile;
        
    /**
     * Pass data object which should have ReportCookie added to its CookieSet.
     * The report will be generated in the same directory as this DataObject.
     * The name of the report file will be same as this DataObject but with
     * different file extension like pdf, html etc
     * @param dataObject
     * @param reportFile
     */
    public GenerateReportAction(DataObject dataObject, ReportCookie cookie) {
            super(label, icon); 
            putValue(NAME, label);
            putValue(SHORT_DESCRIPTION, label);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(ACCELERATOR));
            this.mDataObject = dataObject;
            myCookie = cookie;
            
            //pdf file
            String iepFilePath = FileUtil.toFile(dataObject.getPrimaryFile()).getAbsolutePath();
            int k = iepFilePath.lastIndexOf("."); // NOI18N
            String pdfFilePath = iepFilePath;

            if (k != -1) {
              pdfFilePath = iepFilePath.substring(0, k);
            }
            File pdfFile = new File(pdfFilePath + ".pdf");
            
            this.mReportFile = pdfFile;
            
            ReportCustomizationOptions option = DataObjectSettings.getOrStoreOptions(dataObject);
            try {
                this.mGenerator = ReportGeneratorFactory.getDefault().newReportGenerator(ReportType.REPORT_PDF, this.mReportFile, option);
            } catch(ReportException ex) {
                ErrorManager.getDefault().notify(ex);
            }
    }

    public GenerateReportAction(DataObject dataObject) {
        this(dataObject, null);
    }
    
    public void actionPerformed(ActionEvent e) {
            try {
                ReportCookie rCookie = myCookie;

                if (myCookie == null) {
                    rCookie = mDataObject.getCookie(ReportCookie.class);
                }
                else {
                    rCookie = myCookie;
                }
                if (rCookie != null) {
                    Report report = rCookie.generateReport();
                    
                    if(report != null) {
                        mGenerator.generateReport(report);
                    }
                }
            } catch(Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }
    }
}
