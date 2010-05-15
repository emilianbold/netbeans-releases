package org.netbeans.modules.reportgenerator.generator;

import org.netbeans.modules.reportgenerator.api.*;
import java.io.File;
import java.io.OutputStream;

/**
 * ReportGeneratorFactory is the factory for creating various
 * types of ReportGenerator.
 * 
 * Example there could be one for pdf reports, one for html reports etc.
 * @author radval
 *
 */
public abstract class ReportGeneratorFactory {

	private static ReportGeneratorFactory mInstance;
	
	public static ReportGeneratorFactory getDefault() throws ReportException {
		
		if(mInstance == null) {
		
		String implClass = System.getProperty("org.netbeans.modules.reportgenerator.generator.ReportGeneratorFactory", "org.netbeans.modules.reportgenerator.generator.impl.ReportGeneratorFactoryImpl");
		try {
			
			Class cls = Class.forName(implClass);
			mInstance = (ReportGeneratorFactory) cls.newInstance();
			
		} catch(Exception ex) {
			throw new ReportException("Failed to create ReportGeneratorFactory", ex);
		}
	
		}
		return mInstance;
	}
	
	
        
    /**
     * Get a ReportGenerator for a given type.
     * @param type ReportType
     * @param reportFile Report File where report will be generated
     * @return
     * @throws ReportException
     */    
	public abstract ReportGenerator newReportGenerator(ReportType type, File reportFile, ReportCustomizationOptions options) throws ReportException;
	
        
}
