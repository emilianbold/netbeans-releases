/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
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
    
    // TODO customize method names to match custom task
    // property and type (handled by inner class) names
    
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
    
    /** Output destination. */
    private File destFile;
    
    public void setdestFile(File f) {
        destFile = f;
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
    
    private List resultsReports = new LinkedList(); // List<FileSet>
    public void addFileset(FileSet fs) {
        resultsReports.add(fs);
    }
    
    /* For nested text:
    private StringBuffer text;
    public void addText(String raw) {
        String s = getProject().replaceProperties(raw.trim());
        if (text == null) {
            text = new StringBuffer(s);
        } else {
            text.append(s);
        }
    }
    // <customtask>
    //     Some text...
    // </customtask>
     */
    
    /* Some sort of path (like classpath or similar):
    private Path path;
    public void setPath(Path p) {
        if (path == null) {
            path = p;
        } else {
            path.append(p);
        }
    }
    public Path createPath () {
        if (path == null) {
            path = new Path(project);
        }
        return path.createPath();
    }
    public void setPathRef(Reference r) {
        createPath().setRefid(r);
    }
    // <customtask path="foo:bar"/>
    // <customtask>
    //     <path>
    //         <pathelement location="foo"/>
    //     </path>
    // </customtask>
    // Etc.
     */
    
    /* One of a fixed set of choices:
    public static class FooBieBletch extends EnumeratedAttribute {
        public String[] getValues() {
            return new String[] {"foo", "bie", "bletch"};
        }
    }
    private String mode = "foo";
    public void setMode(FooBieBletch m) {
        mode = m.getValue();
    }
    // <customtask mode="bletch"/>
     */
    
    public void execute() throws BuildException {
        // TODO code here what the task actually does:
        
        // To log something:
        // log("Some message");
        // log("Serious message", Project.MSG_WARN);
        log("AnalyzeResultsTask running", Project.MSG_VERBOSE);
        
        if (refFile == null || !refFile.exists() || !refFile.isFile()) {
            throw new BuildException ("Missing reference file. refFile attribute must be set.");
        }
        if (destFile == null) {
            throw new BuildException ("Missing destination file. distFile attribute must be set.");
        }
        OutputStreamWriter ow = null;
        try {
            log("Processing "+refFile, Project.MSG_VERBOSE);
            ResultsHandler handler = new ResultsHandler ();
            SAXParser p = SAXParserFactory.newInstance().newSAXParser();
            p.parse(refFile, handler);
            
            Map refCases = handler.getCases();
            
                ow = new OutputStreamWriter(new FileOutputStream(destFile));
                ow.write("<testresults>\n<referenceresults>\n");
                printCases (ow, refCases.keySet().iterator(), null);
                ow.write("</referenceresults>\n");
                
	            Iterator it = resultsReports.iterator();
	            while (it.hasNext()) {
	                FileSet fs = (FileSet)it.next();
	                DirectoryScanner ds = fs.getDirectoryScanner(project);
	                File basedir = ds.getBasedir();
	                String[] files = ds.getIncludedFiles();
	                for (int i = 0; i<files.length; i++) {
	                    log("Processing "+files[i], Project.MSG_VERBOSE);
	                    ResultsHandler handler2 = new ResultsHandler ();
	                    p.parse(new File(basedir, files[i]), handler2);
	                    
	                    Map newCases = handler2.getCases();
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
		            printCases (ow, newCases.keySet().iterator(), refCases);
	                }
	                // process them...
	            }
                ow.write("</testresults>\n");
            
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
        
        // To signal an error:
        // throw new BuildException("Problem", location);
        // throw new BuildException(someThrowable, location);
        // throw new BuildException("Problem", someThrowable, location);
        
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
    
    /** Outputs the results of all cases in passed iterator.
     * When refCases is supplied it also prints the difference and result of ttest.
     */
    private void printCases (OutputStreamWriter ow, Iterator it, Map refCases) throws IOException {
        NumberFormat f = new DecimalFormat ("#######0.00");
        NumberFormat f2 = new DecimalFormat ("#######0.0000");
        while (it.hasNext()) {
            TestCaseResults oneCase = (TestCaseResults)it.next();
            ow.write("<testcase");
            ow.write(" name=\""+oneCase.getName()+"\"\n");
            ow.write(" threshold=\""+oneCase.getThreshold()+"\"");
            ow.write(" unit=\""+oneCase.getUnit()+"\"");
            ow.write(" order=\""+oneCase.getOrder()+"\"\n");
            ow.write(" average=\""+f.format(oneCase.getAverage())+"\"");
            ow.write(" stddev=\""+f.format(oneCase.getStdDev())+"\"");
            ow.write(" variance=\""+f.format(oneCase.getVariance())+"\">\n");
            Iterator it2 = oneCase.getValues().iterator();
            while (it2.hasNext()) {
                ow.write("\t<result value=\""+it2.next().toString()+"\"/>\n");
            }
            if (refCases != null) {
                Object o = refCases.get(oneCase);
                if (o != null) {
                    TestCaseResults refResult = (TestCaseResults)o;
                    ow.write("<difference value=\""+f.format(oneCase.getAverage()/refResult.getAverage()*100-100)+"\"/>\n");
                    
            	TestCaseResults.TTestValue tt = oneCase.getTTest();
            	if (tt != null) {
            	    ow.write ("<ttest p=\""+f2.format(tt.getP())+"\" tvalue=\""+f.format(tt.getT())+"\" df=\""+f.format(tt.getDF())+"\">\n");
            	    ow.write (tt.getComment()+"\n");
            	    ow.write ("</ttest>\n");
            	}
                }
            }
            ow.write("</testcase>\n");
        }
    }
    
    private class ResultsHandler extends DefaultHandler {
        
        private Map cases;
        
        public ResultsHandler () {
            cases = new TreeMap (); // TestCaseResults
        }
        
        public Map getCases () {
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
                    
                    TestCaseResults oneCase = new TestCaseResults (name, threshold, unit, order);
                    TestCaseResults theCase = (TestCaseResults)cases.get(oneCase);
                    if (theCase == null) {
                        cases.put(oneCase, oneCase);
                        theCase = oneCase;
                    }
                    theCase.addValue(value);
                }
                catch (NumberFormatException e) {
                    
                }
            }
        }
        
        public void endDocument() throws SAXException {
        }
        
    }
}
