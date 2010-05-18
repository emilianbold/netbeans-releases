package org.netbeans.modules.reportgenerator.api;

/**
 * ReportElementFactory is the factory to create
 * various Report objects which are then populated
 * by a domain model for report generation.
 * @author radval
 *
 */
public abstract class ReportElementFactory {

private static ReportElementFactory mInstance;
	
	/**
	 * Get the default ReportElementFactory factory
	 * @return
	 * @throws ReportException
	 */
	public static ReportElementFactory getDefault() throws ReportException {
		
		if(mInstance == null) {
		
		String implClass = System.getProperty("org.netbeans.modules.reportgenerator.api.ReportElementFactory", "org.netbeans.modules.reportgenerator.api.impl.ReportElementFactoryImpl");
		try {
			
			Class cls = Class.forName(implClass);
			mInstance = (ReportElementFactory) cls.newInstance();
			
		} catch(Exception ex) {
			throw new ReportException("Failed to create ReportGeneratorFactory", ex);
		}
	
		}
		return mInstance;
	}

	/**
	 * create Report.
	 * @return
	 */
	public abstract Report createReport();
	
	/**
	 * create ReportHeader.
	 * @return
	 */
	public abstract ReportHeader createReportHeader();
	
	/**
	 * create ReportBody.
	 * @return
	 */
	public abstract ReportBody createReportBody();
	
	/**
	 * create ReportFooter.
	 * @return
	 */
	public abstract ReportFooter createReportFooter();
	
	/**
	 * create ReportSection.
	 * @return
	 */
	public abstract ReportSection createReportSection();
	
	/**
	 * create ReportElement.
	 * @return
	 */
	public abstract ReportElement createReportElement();
	
	/**
	 * create ReportAttribute.
	 * @return
	 */
	public abstract ReportAttribute createReportAttribute();
	
	
}
