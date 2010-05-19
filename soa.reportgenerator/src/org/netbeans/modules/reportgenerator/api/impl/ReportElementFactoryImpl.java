package org.netbeans.modules.reportgenerator.api.impl;

import org.netbeans.modules.reportgenerator.api.Report;
import org.netbeans.modules.reportgenerator.api.ReportAttribute;
import org.netbeans.modules.reportgenerator.api.ReportBody;
import org.netbeans.modules.reportgenerator.api.ReportElement;
import org.netbeans.modules.reportgenerator.api.ReportElementFactory;
import org.netbeans.modules.reportgenerator.api.ReportFooter;
import org.netbeans.modules.reportgenerator.api.ReportHeader;
import org.netbeans.modules.reportgenerator.api.ReportSection;

public class ReportElementFactoryImpl extends ReportElementFactory {

	@Override
	public Report createReport() {
		return new ReportImpl();
	}

	@Override
	public ReportAttribute createReportAttribute() {
		return new ReportAttributeImpl(); 
	}

	@Override
	public ReportBody createReportBody() {
		return new ReportBodyImpl();
	}

	@Override
	public ReportElement createReportElement() {
		return new ReportElementImpl();
	}

	@Override
	public ReportFooter createReportFooter() {
		return new ReportFooterImpl();
	}

	@Override
	public ReportHeader createReportHeader() {
		return new ReportHeaderImpl();
	}

	@Override
	public ReportSection createReportSection() {
		return new ReportSectionImpl();
	}

		
}
