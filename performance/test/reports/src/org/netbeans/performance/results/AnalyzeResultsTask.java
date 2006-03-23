/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.performance.results;

import java.io.*;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import javax.xml.parsers.*;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Radim Kubacki
 */
public class AnalyzeResultsTask extends Task {
    
    /* For a simple option:
    private boolean opt;
    public void setOpt(boolean b) {
        opt = b;
    }
    // <customtask opt="true"/>
     */
    
    /* For a simple property based on a string:
    private String myprop;
    public void setMyprop(String s) {
        myprop = s;
    }
    // <customtask myprop="some text here"/>
     */
    
    /** File containing reference results. */
    private File refFile;
    
    public void setrefFile(File f) {
        refFile = f;
    }
    
    /* Custom nested elements:
    public static class Nestme {
        String val; // accessible from execute()
        public void setVal(String s) {
            val = s;
        }
    }
    private List nestmes = new LinkedList(); // List<Nestme>
    public Nestme createNestme() {
        Nestme n = new Nestme();
        nestmes.add(n);
        return n;
    }
    // Or:
    public void addNestme(Nestme n) {
        nestmes.add(n);
    }
    // <customtask>
    //     <nestme val="something"/>
    // </customtask>
     */
    
    private List<FileSet> resultsReports = new LinkedList();
    public void addFileset(FileSet fs) {
        resultsReports.add(fs);
    }
    
    public void execute() throws BuildException {
        // TODO code here what the task actually does:
        
        // To log something:
        // log("Some message");
        // log("Serious message", Project.MSG_WARN);
        log("AnalyzeResultsTask running", Project.MSG_VERBOSE);

        checkArgs();
        OutputStreamWriter ow = null;
        try {
            log("Processing reference results from "+refFile, Project.MSG_VERBOSE);
            ResultsHandler handler = new ResultsHandler();
            SAXParser p = SAXParserFactory.newInstance().newSAXParser();
            p.parse(refFile, handler);
            
            Map<TestResult,Collection<Integer>> refCases = handler.getCases();
            
            for (FileSet fs: resultsReports) {
                DirectoryScanner ds = fs.getDirectoryScanner(project);
                File basedir = ds.getBasedir();
                String[] files = ds.getIncludedFiles();
                for (int i = 0; i<files.length; i++) {
                    log("Processing "+files[i], Project.MSG_VERBOSE);
                    ResultsHandler handler2 = new ResultsHandler();
                    File srcFile = new File(basedir, files[i]);
                    File destFile = new File( srcFile.getParentFile(), "testreport-perf-comparision.xml");
                    p.parse(srcFile, handler2);
                    
                    Map<TestResult,Collection<Integer>> newCases = handler2.getCases();
//                    printTTest(refCases, newCases);
                    ow = new OutputStreamWriter(new FileOutputStream(destFile));
                    ow.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<testresults>\n");
                    printCases(ow, newCases, refCases);
                    ow.write("</testresults>\n");
                }
                // process them...
            }
            
        }
        catch (ParserConfigurationException ex) {
            throw new BuildException ("Cannot parse results file.", ex);
        }
        catch (FactoryConfigurationError er) {
            throw new BuildException ("Cannot parse results file.", er);
        }
        catch (SAXException e) {
            throw new BuildException ("Cannot parse results file.", e);
        }
        catch (java.io.IOException e) {
            throw new BuildException ("IOException when computing.", e);
        }
        finally {
            try { if (ow != null) ow.close(); } catch (IOException ioe) {}
        }
        
        // You can call other tasks too:
        // Zip zip = (Zip)project.createTask("zip");
        // zip.setZipfile(zipFile);
        // FileSet fs = new FileSet();
        // fs.setDir(baseDir);
        // zip.addFileset(fs);
        // zip.init();
        // zip.setLocation(location);
        // zip.execute();
    }

    private void printTTest(final Map refCases, final Map newCases) {
        Iterator it2 = newCases.keySet().iterator();
        while (it2.hasNext()) {
            TestCaseResults res = (TestCaseResults)it2.next();
            Object o = refCases.get(res);
            if (o != null) {
                TestCaseResults refResult = (TestCaseResults)o;
                if (refResult.getCount() <= 2 || res.getCount() <= 2) {
                    log("ttest skipped for "+refResult.getName()+" "+refResult.getOrder(), Project.MSG_VERBOSE);
                    continue;
                }
                log("Compare "+refResult.getName()+" "+refResult.getOrder(), Project.MSG_VERBOSE);
                log("\t"+refResult.getCount()+" against "+res.getCount()+" values", Project.MSG_VERBOSE);
                TestCaseResults.TTestValue tt = TestCaseResults.ttest(res, refResult);
                res.setTTest(tt);
            }
        }
    }
    
    /** Outputs the results of all cases in passed iterator.
     * When refCases is supplied it also prints the difference and result of ttest.
     */
    private void printCases (OutputStreamWriter ow,
            Map<TestResult, Collection<Integer>> actual,
            Map<TestResult, Collection<Integer>> reference) throws IOException {
        NumberFormat f = new DecimalFormat ("#######0.00");
        NumberFormat f2 = new DecimalFormat ("#######0.0000");
        Set<TestResult> keys = actual.keySet();
        for (TestResult oneCase : keys) {
            Collection<Integer> values = actual.get(oneCase);
            TestResult.Statistics stat = TestResult.computeStatistics(values);
            ow.write("<testcase");
            ow.write(" name=\""+oneCase.getName()+"\"\n");
            ow.write(" threshold=\""+oneCase.getThreshold()+"\"");
            ow.write(" unit=\""+oneCase.getUnit()+"\"");
            ow.write(" order=\""+oneCase.getOrder()+"\"\n");
            ow.write(" average=\""+f.format(stat.getAverage())+"\"");
            ow.write(" stddev=\""+f.format(stat.getStdDev())+"\"");
            ow.write(" variance=\""+f.format(stat.getVariance())+"\">\n");
            for (int val : values) {
                ow.write("\t<result value=\""+val+"\"/>\n");
            }
            if (reference != null) {
                Collection<Integer> refValues = reference.get(oneCase);
                if (refValues != null) {
                    ow.write("<difference value=\""+
                            f.format(stat.getAverage()/TestResult.computeStatistics(refValues).getAverage()*100-100)+
                            "\"/>\n");
                    
		    /*
            	TestCaseResults.TTestValue tt = oneCase.getTTest();
            	if (tt != null) {
            	    ow.write ("<ttest p=\""+f2.format(tt.getP())+"\" tvalue=\""+f.format(tt.getT())+"\" df=\""+f.format(tt.getDF())+"\">\n");
            	    ow.write (tt.getComment()+"\n");
            	    ow.write ("</ttest>\n");
            	}
		    */
                }
            }
            ow.write("</testcase>\n");
        }
    }

    private void checkArgs() throws BuildException {
        if (refFile == null && resultsReports.isEmpty()) {
            // try to find last performance results and use them as a reference
            // look for one of results/testrun_??????-??????/xmlresults/testrun-performance.xml
            File dir = new File (getProject().getBaseDir(), "results");
            if (dir.isDirectory()) {
                File [] testrunDirs = dir.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                         return name.startsWith("testrun_") && name.length() == 21;
                    }
                });
                Arrays.sort(testrunDirs);
                if (testrunDirs.length > 0) {
                    refFile = new File(testrunDirs[testrunDirs.length-1], "xmlresults"+File.separator+"testrun-performance.xml");
                    log("Using default reference file "+refFile, Project.MSG_VERBOSE);
                }
                for (int i = 0; i < testrunDirs.length - 1; i++) {
                    File runFile = new File(testrunDirs[i], "xmlresults"+File.separator+"testrun-performance.xml");
                    log("Adding files for comparision: "+runFile, Project.MSG_VERBOSE);
                    FileSet fs = new FileSet();
                    fs.setFile(runFile);
                    addFileset(fs);
                }
            }
            // find results to compare with

        }
        if (refFile == null || !refFile.exists() || !refFile.isFile()) {
            throw new BuildException ("Missing reference file. refFile attribute must be set.");
        }
//        if (destFile == null) {
//            throw new BuildException ("Missing destination file. destFile attribute must be set.");
//        }
    }
    
    private class ResultsHandler extends DefaultHandler {
        
        private Map<TestResult, Collection<Integer>> cases;
        
        private String currSuite;
        
        public ResultsHandler () {
            cases = new TreeMap (); // TestCaseResults
        }
        
        public Map<TestResult, Collection<Integer>> getCases () {
            return cases;
        }
        
        public void startElement(String namespaceURI, String localName, String qName, org.xml.sax.Attributes atts) 
        throws org.xml.sax.SAXException {
//            System.out.println("namespaceURI "+namespaceURI);
//            System.out.println("localName "+localName);
//            System.out.println("qName "+qName);
//            System.out.println("atts "+atts);
            if ("PerformanceData".equals(qName)) {
                try {
                    String name = atts.getValue("name");
                    int order = Integer.parseInt(atts.getValue("runOrder"));
                    int threshold = Integer.parseInt(atts.getValue("threshold"));
                    String unit = atts.getValue("unit");
                    int value = Integer.parseInt(atts.getValue("value"));
                    
                    TestResult oneCase = new TestResult (name, threshold, unit, order, currSuite);
                    Collection<Integer> values = cases.get(oneCase);
                    if (values == null) {
                        values = new ArrayList ();
                        cases.put(oneCase, values);
                    }
                    values.add(value);
                }
                catch (NumberFormatException e) {
                    
                }
            }
            else if ("UnitTestSuite".equals(qName)) {
                currSuite = atts.getValue("name");
            }
        }
        
        public void endDocument() throws SAXException {
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            if ("UnitTestSuite".equals(qName)) {
                currSuite = null;
            }
        }
        
    }
}
