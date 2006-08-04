/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * StackTraceTranslatorTest.java
 * JUnit based test
 *
 * Created on 16 November 2005, 13:52
 */
package org.netbeans.mobility.antext;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import junit.framework.*;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Lukas Waldmann
 */
public class StackTraceTranslatorTest extends NbTestCase {
    
    public StackTraceTranslatorTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    static Test suite() {
        TestSuite suite = new TestSuite(StackTraceTranslatorTest.class);
        
        return suite;
    }
    
    /**
     * Test of translate method, of class org.netbeans.mobility.antext.StackTraceTranslator.
     */
    public void testTranslate() {
        System.out.println("translate");
        
        String stackTrace = "java.lang.RuntimeException\n"+
                "at javax.microedition.midlet.MIDletProxy.startApp(+7)\n"+
                "at com.sun.midp.midlet.Scheduler.schedule(+270)\n"+
                "at com.sun.midp.main.Main.runLocalClass(+28)\n"+
                "at com.sun.midp.main.Main.main(+116)";
        String expResult = "java.lang.RuntimeException\n\t" +
                "at javax.microedition.midlet.MIDletProxy.startApp(MIDletProxy.java:44)\n\t" +
                "at com.sun.midp.midlet.Scheduler.schedule(Scheduler.java:372)\n\t" +
                "at com.sun.midp.main.Main.runLocalClass(Main.java:461)\n\t" +
                "at com.sun.midp.main.Main.main(Main.java:126)\n";
        
        Project p=new Project();
        Path midp=new Path(p,getGoldenFile("midpapi20.zip").getAbsolutePath());
        
        StackTraceTranslator instance = new StackTraceTranslator(p.getBaseDir(),midp.list());
        
        
        String result = instance.translate(stackTrace);
        assertEquals(expResult, result);
                
    }
}
