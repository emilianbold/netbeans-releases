/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * TransformXMLTask.java
 *
 * Created on November 22, 2001, 11:41 AM
 */

package org.netbeans.xtest.pe;

import org.apache.tools.ant.*;
import java.io.*;
import org.netbeans.xtest.pe.xmlbeans.*;
import java.util.*;
import org.w3c.dom.*;

import org.netbeans.xtest.util.*;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;

/**
 *
 * @author  mb115822
 * @version 
 */
public class TransformXMLTask extends Task{

    
    public final static String TESTREPORT_FAILURES_XSL = "failures.xsl";
    public final static String TESTREPORT_XSL = "summary.xsl";
    public final static String TESTBAG_XSL = "testbag.xsl";
    public final static String TESTSUITES_XSL = "suite.xsl";
    public final static String SYSTEMINFO_XSL = "systeminfo.xsl";
    public final static String FRAMESET_XSL = "frameset.xsl";
    public final static String MAIN_NAVIGATOR_XSL = "main-navigator.xsl";
    
    // debugging flag - should be set to false :-)
    private static final boolean DEBUG = false;
    private static final void debugInfo(String message) {
        if (DEBUG) System.out.println("TransformXMLTask."+message);
    }
    
    
    /** Creates new TransformXMLTask */
    public TransformXMLTask() {
    }
    
    private File inputDir;
    private File outputDir;
    
    //private File suiteXSL;
    
    public void setInputDir(File inputDir) {
        this.inputDir = inputDir;
    }
    
    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }
    
    /*
    public void setSuiteXSL(File suiteXSL) {
        this.suiteXSL = suiteXSL;
    }
    */
    
    //private static final XSL_PACKAGE = "org.netbeans.xtest.pe.xsls";
    
    private File getXSLFile(String name) throws IOException {
      String xtestHome = getProject().getProperty("xtest.home");
      if (xtestHome == null) {
          debugInfo("getXSLFile(): xtest.home not set !!!!, have to be set to xtest home");
          new IOException("xtest.home not set !!!!, have to be set to xtest home");          
      }
      File xtestHomeDir = new File(xtestHome);
      File requestedXSL = new File(xtestHomeDir,"lib"+File.separator+"xsl"+File.separator+name);
      if (!requestedXSL.exists()) {
          debugInfo("getXSLFile(): requested XSL file does not exist:"+requestedXSL);
          new IOException("requested XSL file does not exist:"+requestedXSL);       
      }
      return requestedXSL;      
    }
    
    public void execute () throws BuildException {
        try {
            log("Transforming report's XMLs to HTMLs");
            int inputDirType = ResultsUtils.resolveResultsDir(inputDir);
            switch (inputDirType) {
                case ResultsUtils.TESTBAG_DIR:
                    transformTestBag(inputDir,outputDir);
                    transformUnitSuites(inputDir,outputDir);
                    break;                    
                case ResultsUtils.TESTREPORT_DIR:
                    transformReport(inputDir,outputDir);
                    transformSystemInfo(inputDir,outputDir);
                    transformFailuresReport(inputDir,outputDir);
                    transformFrameSet(inputDir,outputDir);
                    transformMainNavigator(inputDir,outputDir);
            }
        } catch (Exception te) {
            debugInfo("execute(): Exception !!! : "+te);
            new BuildException(te);
        }
    }
        

    
    
    public static Transformer getTransformer(File xsl) throws TransformerConfigurationException {
        TransformerFactory tFactory = TransformerFactory.newInstance();
        StreamSource xslSource = new StreamSource(xsl);
        Transformer transformer = tFactory.newTransformer(xslSource);
        debugInfo("getTransformer(): got transformer from this xsl:"+xsl);
        return transformer;
    }
    
    public static Document transform(Document xml, Transformer transformer) {
        return null;
    }
    
    public static void transform(Transformer transformer, Source xmlSource, Result outputTarget) throws TransformerException {
        try {
            SAXParserFactoryUtil.setXTestParser();
            debugInfo("transform(Transformer,Source,Result) set XTest required XML parser");
            transformer.transform(xmlSource, outputTarget);
        } finally {
            SAXParserFactoryUtil.revertXTestParser();
            debugInfo("transform(Transformer,Source,Result) reverted XTest required XML parser");
        }
    }
    
    
    public static void transform(File inputXML, File outputXML, Transformer transformer) throws TransformerException, IOException {
        StreamSource xmlSource = new StreamSource(inputXML);
        FileOutputStream outputFileStream = new FileOutputStream(outputXML);
        StreamResult xmlResult = new StreamResult(outputFileStream);
        transform(transformer,xmlSource,xmlResult);
        outputFileStream.close();
    }
    
    public static void transform(File inputXML, File outputXML, File xsl) throws TransformerException {
        Transformer transformer = getTransformer(xsl);
        transform(inputXML,outputXML,xsl);
    }
    
    public void transformUnitSuites(File inputDir, File outputDir) throws TransformerException, IOException {
        debugInfo("transformUnitSuites(): inputDir="+inputDir+" outputDir="+outputDir);
        File suiteInputDir = new File(inputDir,PEConstants.XMLRESULTS_DIR+File.separatorChar+PEConstants.TESTSUITES_SUBDIR);
        File suiteOutputDir = new File(outputDir,PEConstants.HTMLRESULTS_DIR+File.separatorChar+PEConstants.TESTSUITES_SUBDIR);
        if (!suiteOutputDir.mkdirs()) {
            throw new IOException("TransformXML, cannot create output dir: "+suiteOutputDir);
        }
        String[] suites = ResultsUtils.listSuites(suiteInputDir);
        debugInfo("transformUnitSuites(): got suites: "+suites.length);
        Transformer transformer = getTransformer(getXSLFile(TESTSUITES_XSL));
        for (int i=0; i<suites.length; i++) {
            debugInfo("transformUnitSuites(): processing suite: "+suites[i]);
            File inputSuite = new File(suiteInputDir,"TEST-"+suites[i]+".xml");
            File outputSuite = new File(suiteOutputDir,"TEST-"+suites[i]+".html");
            outputSuite.createNewFile();            
            transform(inputSuite,outputSuite,transformer);
        }
    }
    
    
    // this will create a page full of failures/errors over whole report   
    public void transformFailuresReport(File inputRoot, File outputRoot) throws TransformerException, IOException {
       debugInfo("transformFailuresReport(): inputRoot="+inputRoot+" outputRoot="+outputRoot); 
       
       File failuresInputDir = ResultsUtils.getXMLResultDir(inputRoot);
       File failuresInputFile = new File(failuresInputDir,PEConstants.TESTREPORT_FAILURES_XML_FILE);
       
       File failuresOutputDir = ResultsUtils.getHTMLResultDir(outputRoot);
       File failuresOutputFile = new File(failuresOutputDir,PEConstants.TESTREPORT_FAILURES_HTML_FILE);       
              
       Transformer transformer = getTransformer(getXSLFile(TESTREPORT_FAILURES_XSL));      
       transform(failuresInputFile,failuresOutputFile,transformer);
    }
    
    // this will create a page full of failures/errors over whole report   
    public void transformReport(File inputRoot, File outputRoot) throws TransformerException, IOException {
       debugInfo("transformReport(): inputRoot="+inputRoot+" outputRoot="+outputRoot); 
       
       File reportInputDir = ResultsUtils.getXMLResultDir(inputRoot);
       File reportInputFile = new File(reportInputDir,PEConstants.TESTREPORT_XML_FILE);
       
       File reportOutputDir = ResultsUtils.getHTMLResultDir(outputRoot);
       File reportOutputFile = new File(reportOutputDir,PEConstants.TESTREPORT_HTML_FILE);       
              
       Transformer transformer = getTransformer(getXSLFile(TESTREPORT_XSL));      
       transform(reportInputFile,reportOutputFile,transformer);
    }
    
    public void transformTestBag(File inputRoot, File outputRoot) throws TransformerException, IOException {
       debugInfo("transformTestBag(): inputRoot="+inputRoot+" outputRoot="+outputRoot); 
       
       File testBagInputDir = ResultsUtils.getXMLResultDir(inputRoot);
       File testBagInputFile = new File(testBagInputDir,PEConstants.TESTBAG_XML_FILE);
       
       File testBagOutputDir = ResultsUtils.getHTMLResultDir(outputRoot);
       File testBagOutputFile = new File(testBagOutputDir,PEConstants.TESTBAG_HTML_FILE);       
              
       Transformer transformer = getTransformer(getXSLFile(TESTBAG_XSL));      
       transform(testBagInputFile,testBagOutputFile,transformer);
    }
    
    public void transformSystemInfo(File inputRoot, File outputRoot) throws TransformerException, IOException {
       debugInfo("transformSystemInfo(): inputRoot="+inputRoot+" outputRoot="+outputRoot); 
       
       File siInputDir = ResultsUtils.getXMLResultDir(inputRoot);
       File siInputFile = new File(siInputDir,PEConstants.TESTREPORT_XML_FILE);
       
       File siOutputDir = ResultsUtils.getHTMLResultDir(outputRoot);
       File siOutputFile = new File(siOutputDir,PEConstants.SYSTEMINFO_HTML_FILE);       
              
       Transformer transformer = getTransformer(getXSLFile(SYSTEMINFO_XSL));      
       transform(siInputFile,siOutputFile,transformer);
    }
    
    public void transformFrameSet(File inputRoot, File outputRoot) throws TransformerException, IOException {
       debugInfo("transformFrameSet(): inputRoot="+inputRoot+" outputRoot="+outputRoot); 
       
       File fsInputDir = ResultsUtils.getXMLResultDir(inputRoot);
       File fsInputFile = new File(fsInputDir,PEConstants.TESTREPORT_XML_FILE);
       
       File fsOutputDir = ResultsUtils.getHTMLResultDir(outputRoot);
       File fsOutputFile = new File(fsOutputDir,PEConstants.INDEX_HTML_FILE);       
              
       Transformer transformer = getTransformer(getXSLFile(FRAMESET_XSL));      
       transform(fsInputFile,fsOutputFile,transformer);
    }
    
    public void transformMainNavigator(File inputRoot, File outputRoot) throws TransformerException, IOException {
       debugInfo("transformMainNavigator(): inputRoot="+inputRoot+" outputRoot="+outputRoot); 
       
       File mnInputDir = ResultsUtils.getXMLResultDir(inputRoot);
       File mnInputFile = new File(mnInputDir,PEConstants.TESTREPORT_XML_FILE);
       
       File mnOutputDir = ResultsUtils.getHTMLResultDir(outputRoot);
       File mnOutputFile = new File(mnOutputDir,PEConstants.MAIN_NAVIGATOR_HTML_FILE);       
              
       Transformer transformer = getTransformer(getXSLFile(MAIN_NAVIGATOR_XSL));      
       transform(mnInputFile,mnOutputFile,transformer);
    }

}
