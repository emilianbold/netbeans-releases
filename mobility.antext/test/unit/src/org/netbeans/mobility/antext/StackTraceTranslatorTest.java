/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
        
        
        String result = instance.translate(stackTrace, false);
        assertEquals(expResult, result);
                
    }
}
