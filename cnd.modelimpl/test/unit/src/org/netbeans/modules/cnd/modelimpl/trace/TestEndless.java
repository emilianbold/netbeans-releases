/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelimpl.trace;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.test.BaseTestSuite;
import org.openide.filesystems.FileSystem;

/**
 * Test that goes forever
 * (Created for the sake of investigation IZ #106124)
 * @author Vladimir Kvashin
 */
public class TestEndless extends BaseTestSuite {
    
    public TestEndless() {
        super("C/C++ Endless Test");
//	this.addTestSuite(TestEndless.Worker.class);
	for (int i = 0; i < 1000; i++) {
	    this.addTest(new TestEndless.Worker());
	}
	System.setProperty("cnd.modelimpl.parser.threads", "4");
    }
    
    public static Test suite() {
	TestSuite suite = new TestEndless();
	return suite;
    }
    
    
    public static class Worker extends TraceModelTestBase {

	public Worker() {
	    super("testEndless");
	}

	@Override
	protected void setUp() throws Exception {
	    System.setProperty("parser.report.errors", "true");
	    super.setUp();
	}

	protected void postSetUp() {
	    // init flags needed for file model tests
	    getTraceModel().setDumpModel(true);
	    getTraceModel().setDumpPPState(true);
	}

	public void testEndless() throws Exception {
//	    int pass = 0;
//	    do {
//		//System.out.printf("Pass %d\n", ++pass);
//		justParse(++pass); // NOI18N
//	    }
//	    while( ! new File("/tmp/stop-endless-test").exists());
	    for( int pass = 0; pass < 10; pass++ ) {
		justParse(pass); // NOI18N
	    }
	}

	private void justParse(int pass) throws Exception {
	    String source = "file_" + pass;
	    File workDir = getWorkDir();
	    workDir.mkdirs();
	    File testFile = new File(workDir, source);
	    writeSource(testFile, pass);
	    File output = new File(workDir, source+".dat");
	    PrintStream streamOut = new PrintStream(output);
	    PrintStream oldOut = System.out;
	    File error = new File(workDir, source+".err");
	    PrintStream streamErr = new PrintStream(error);
	    PrintStream oldErr = System.err;

	    try {
		System.out.println("testing " + testFile);     
		// redirect output and err
		System.setOut(streamOut);
		System.setErr(streamErr);
		performModelTest(testFile, streamOut, streamErr);
	    } finally {
		// restore err and out
		streamOut.close();
		streamErr.close();
		System.setOut(oldOut);
		System.setErr(oldErr);
	    }
	}
	
	private void writeSource(File file, int pass) throws FileNotFoundException {
	    PrintStream ps = null;
	    try {
		//file.getParentFile().mkdirs();
		ps = new PrintStream(file);
		ps.printf("class TheSame {};\n");
		ps.printf("class Different%d {};\n", pass);
	    }
	    finally {
		if( ps != null ) {
		    ps.close();
		}
	    }
	}
    }
}