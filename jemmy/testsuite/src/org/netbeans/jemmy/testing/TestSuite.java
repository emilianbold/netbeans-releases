package org.netbeans.jemmy.testing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Vector;

import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.Test;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class TestSuite extends Task {
    Vector tests;
    File runDir = null;
    File resultDir = null;
    File testsLocation = null;
    File resourcesFile = null;
    File timeoutsFile = null;
    boolean robot = false;
    public TestSuite() {
	tests = new Vector();
    }
    public void setTestList(File testList) throws IOException {
	BufferedReader reader = new BufferedReader(new FileReader(testList));
	String test;
	while((test = reader.readLine()) != null) {
	    tests.add(test);
	}
    }
    public void setResultDir(File runDir) {
	this.resultDir = runDir;
    }
    public void setTestsLocation(File testsLocation) {
	this.testsLocation = testsLocation;
    }
    public void setResources(File resourcesFile) {
	this.resourcesFile = resourcesFile;
    }
    public void setTimeouts(File timeoutsFile) {
	this.timeoutsFile = timeoutsFile;
    }
    public void setRobotDispatching(boolean v) {
	robot = v;
    }
    public void execute() throws BuildException {
	try {
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyy.MM.dd_hh:mm:ss");
	    String start = dateFormat.format(Calendar.getInstance().getTime());
	    System.out.println("Start time: " + start);
	    initDirs(start);
	    initJemmy();
	    String pwd = System.getProperty("user.dir");
	    int passed = 0, failed = 0;
	    for(int i = 0; i < tests.size(); i++) {
		try {
		    String testName = (String)tests.get(i);
		    if(!testName.startsWith("#")) {
			File testDir = createTestDir(testName);
			JemmyProperties.setCurrentOutput(createTestOutput(testDir));
			//		    copyContent(createTestLocationDir(testName), testDir);
			System.setProperty("user.dir", createTestLocationDir(testName).getAbsolutePath());
			Test test = new Test(testName);
			if(test.startTest(null) == Test.TEST_PASSED_STATUS) {
			    System.out.println(testName + " PASSED");
			    passed++;
			} else {
			    System.out.println(testName + " FAILED");
			    failed++;
			}
			System.out.flush();
		    }
		} catch(IOException e) {
		    e.printStackTrace();
		}
	    }
	    System.out.println("End   time: " + dateFormat.format(Calendar.getInstance().getTime()));
	    System.out.println("PASSED: " + passed);
	    System.out.println("FAILED: " + failed);
	    System.out.flush();
	} catch(IOException e) {
	    e.printStackTrace();
	}
    }
    private File createTestDir(String test) {
	File result = 
	    new File(runDir.getAbsolutePath() + 
		     System.getProperty("file.separator") + 
		     getLastTestName(test));
	result.mkdir();
	return(result);
    }
    private File createTestLocationDir(String test) {
	return(new File(testsLocation.getAbsolutePath()));
    }
    private TestOut createTestOutput(File testDir) throws IOException {
	return(new TestOut(System.in,
			   new PrintWriter(new FileWriter(testDir.getAbsolutePath() + 
							  System.getProperty("file.separator") +
							  "output")),
			   new PrintWriter(new FileWriter(testDir.getAbsolutePath() + 
							  System.getProperty("file.separator") +
							  "errput")),
			   new PrintWriter(new FileWriter(testDir.getAbsolutePath() + 
							  System.getProperty("file.separator") +
							  "golden"))));
    }
    private String getLastTestName(String test) {
	StringTokenizer token = new StringTokenizer(test, ".");
	String lastname = test;
	while(token.hasMoreTokens()) {
	    lastname = token.nextToken();
	}
	return(lastname);
    }
    private void copyContent(File fromDir, File toDir) throws IOException {
	File[] files = fromDir.listFiles();
	FileReader reader;
	FileWriter writer;
	for(int i = 0; i < files.length; i++) {
	    if(!files[i].isDirectory()) {
		reader = new FileReader(files[i]);
		writer = new FileWriter(toDir.getAbsolutePath() + 
					System.getProperty("file.separator") +
					files[i].getName());
		int c;
		while((c = reader.read()) != -1) {
		    writer.write(c);
		}
		writer.flush();
	    }
	}
    }
    private void initDirs(String start) {
	if(resultDir == null) {
	    setResultDir(new File(System.getProperty("user.dir") +
				  System.getProperty("file.separator") +
				  "run"));
	}
	if(runDir == null) {
	    runDir = new File(resultDir.getAbsolutePath() +
			      System.getProperty("file.separator") +
			      start);
	}
	runDir.mkdirs();
	System.out.println("Run directory: " + runDir.getAbsolutePath());
	if(testsLocation == null) {
	    setTestsLocation(runDir);
	}
	System.out.println("Test location: " + testsLocation.getAbsolutePath());
	if(resourcesFile == null) {
	    setResources(new File(runDir.getAbsolutePath() +
				  System.getProperty("file.separator") +
				  "resources"));
	}
	System.out.println("Resources    : " + resourcesFile.getAbsolutePath());
	if(timeoutsFile == null) {
	    setTimeouts(new File(runDir.getAbsolutePath() +
				 System.getProperty("file.separator") +
				 "timeouts"));
	}
	System.out.println("Timeouts     : " + timeoutsFile.getAbsolutePath());
    }
    private void initJemmy() throws IOException {
	JemmyProperties.
	    getCurrentBundleManager().
	    loadBundleFromFile(resourcesFile.getAbsolutePath(), "");
	JemmyProperties.
	    getCurrentTimeouts().
	    loadDefaults(timeoutsFile.getAbsolutePath());
	JemmyProperties.getProperties().initDispatchingModel(true, robot);
    }
}
