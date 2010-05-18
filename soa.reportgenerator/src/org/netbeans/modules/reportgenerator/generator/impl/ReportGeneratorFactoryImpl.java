package org.netbeans.modules.reportgenerator.generator.impl;

import java.io.File;
import java.io.OutputStream;

import org.netbeans.modules.reportgenerator.api.ReportCustomizationOptions;
import org.netbeans.modules.reportgenerator.api.ReportException;
import org.netbeans.modules.reportgenerator.api.ReportGenerator;
import org.netbeans.modules.reportgenerator.generator.ReportGeneratorFactory;
import org.netbeans.modules.reportgenerator.api.ReportType;
import org.netbeans.modules.reportgenerator.api.impl.pdf.PDFReportGenerator;

public class ReportGeneratorFactoryImpl extends ReportGeneratorFactory {

	@Override
	public ReportGenerator newReportGenerator(ReportType type, 
                                                  File reportFile,
                                                  ReportCustomizationOptions options) throws ReportException {
		ReportGenerator rGenerator = null;
		if(type.equals(ReportType.REPORT_HTML)) {
			
		} else if (type.equals(ReportType.REPORT_PDF)) {
			rGenerator = new PDFReportGenerator(reportFile, options);
			
		}
		
		return rGenerator;
	}

	
}
