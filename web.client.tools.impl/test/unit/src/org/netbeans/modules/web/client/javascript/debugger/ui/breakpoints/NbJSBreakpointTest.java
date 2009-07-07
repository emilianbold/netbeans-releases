/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.client.javascript.debugger.ui.breakpoints;

import org.netbeans.modules.web.client.javascript.debugger.ui.NbJSDTestBase;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.web.client.javascript.debugger.ui.breakpoints.NbJSBreakpointManager;
import org.netbeans.modules.web.client.javascript.debugger.ui.breakpoints.NbJSFileObjectBreakpoint;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.Line;
         
/**
 * @author joelle
 */
public class NbJSBreakpointTest extends NbJSDTestBase {

    /** Default constructor.
     * @param testName name of particular test case
    */
    public NbJSBreakpointTest(String testName) {
        super(testName);
    }   
    
      /** Creates suite from particular test cases. You can define order of testcases here. */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        List<NbJSDTestBase> tests = getTests();
        for( NbJSDTestBase test : tests ) {
            suite.addTest( test );
        }
        return suite;
    }
    
    public static List<NbJSDTestBase> getTests() {
        List<NbJSDTestBase> tests = new ArrayList<NbJSDTestBase>();
        tests.add(new NbJSBreakpointTest("testGetBreakpoints"));
        tests.add(new NbJSBreakpointTest("testCreateBreakpointConstructorWithNull"));
        tests.add(new NbJSBreakpointTest("testBreakpointEnableDefault"));
        tests.add(new NbJSBreakpointTest("testBreakpointEnableDisable"));
        tests.add(new NbJSBreakpointTest("testBreakpointSetLine"));
        tests.add(new NbJSBreakpointTest("testBreakpointGetFileObject"));
        tests.add(new NbJSBreakpointTest("testBreakpointGetLine"));
        tests.add(new NbJSBreakpointTest("testBreakpointGetFilePath"));
        tests.add(new NbJSBreakpointTest("testBreakpointToString"));
        return tests;
    } 

    public void testGetBreakpoints() throws Exception {
        FileObject vegetableFO = createVegJSFO();
        FileObject jsFO = createJSFO();
        addBreakpoint(jsFO, 1);
        addBreakpoint(vegetableFO, 1);
        addBreakpoint(jsFO, 2);
        // all
        assertEquals("two Javascript breakpoints", 3, NbJSBreakpointManager.getBreakpoints().length);
        // by files
        assertEquals("two Javascript breakpoints for fruit.js", 2, NbJSBreakpointManager.getBreakpoints(jsFO).length);
        assertEquals("one Javascript breakpoint for vegetable.js", 1, NbJSBreakpointManager.getBreakpoints(vegetableFO).length);
    }
    
    /** Test case 1. */
    public void testCreateBreakpointConstructorWithNull() {
        Breakpoint bp;
        try {
            Line line = null;
            bp = new NbJSFileObjectBreakpoint((Line)null);
        } catch (NullPointerException npe) {
            //Expecting NullPointerException
            return;
        }
        fail();
    }
    
    public void testBreakpointEnableDefault() throws Exception {

        FileObject jsFO = createJSFO();
        addBreakpoint(jsFO, 1);
        addBreakpoint(jsFO, 2);
        
        Breakpoint[] bps = NbJSBreakpointManager.getBreakpoints(jsFO);
        int enabledCount  = 0;
        for( Breakpoint bp : bps ){
            if (bp.isEnabled()){
                enabledCount++;
            }
        }
        assertEquals("two breakpoints should be enabled by default", 2, enabledCount);
  }
    
  public void testBreakpointEnableDisable() throws Exception {

        FileObject jsFO = createJSFO();
        NbJSFileObjectBreakpoint bp = addBreakpoint(jsFO, 1);
        
        assertTrue( bp.isEnabled());
        bp.disable();
        assertFalse( bp.isEnabled() );
        bp.disable();
        assertFalse( bp.isEnabled() );
        bp.enable();
        assertTrue( bp.isEnabled() );
        bp.enable();
        assertTrue( bp.isEnabled() );
  }
  
  public void testBreakpointGetLine() throws Exception {
        FileObject jsFO = createJSFO();
        Line line = createDummyLine(jsFO, 2);
        NbJSFileObjectBreakpoint bp = new NbJSFileObjectBreakpoint(line);
        Line tmpLine = bp.getLine();
        assertEquals( line, tmpLine );
        
  }
  
  
  public void testBreakpointSetLine() throws Exception {
        FileObject jsFO = createJSFO();
        NbJSFileObjectBreakpoint bp = addBreakpoint(jsFO, 1);
        
        Line line = createDummyLine(jsFO, 2);
        assertNotNull(line);
        bp.setLine(line);
        Line tmpLine = bp.getLine();
        assertEquals(line, tmpLine);
        
        try {
            bp.setLine(null);
        } catch (NullPointerException npe){
            return;
        }
        fail();
  }
  
  public void testBreakpointGetFileObject() throws Exception{
        FileObject jsFO = createJSFO();
        String foPath = FileUtil.toFile(jsFO).getAbsolutePath();
        assertNotNull(foPath);
        assertNotNull(jsFO);
        
        NbJSFileObjectBreakpoint bp = addBreakpoint(jsFO, 1);
        assertEquals( "File Path should be the same.", jsFO, bp.getFileObject() );
  }
  

  public void testBreakpointToString() throws Exception {
        FileObject jsFO = createJSFO();
        String strFruit = FileUtil.toFile(jsFO).getAbsolutePath() + ':' + 1;
        assertNotNull(jsFO);
        assertNotNull(strFruit);
        NbJSFileObjectBreakpoint bp = addBreakpoint(jsFO, 1);
        assertEquals( "File Path should be the same.", strFruit, bp.toString() );
  }
    
   public FileObject createVegJSFO() throws IOException {
        String[] vegetableContent = {
            "document.write('pea, cucumber, cauliflower, broccoli');",
        };
        File vegetableF = createJavaScript(vegetableContent, "vegetable.js");
        FileObject vegetableFO = FileUtil.toFileObject(vegetableF);
        return vegetableFO;
    }
    
}
