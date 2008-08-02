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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
 * ClassDiagramTestCase.java
 *
 * Created on December 2, 2005, 1:58 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.uml.classdiagram;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTextAreaOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.exceptions.UnexpectedElementSelectionException;
import org.netbeans.test.umllib.testcases.UMLTestCase;



/**
 *
 * @author Alexandr Scherbatiy
 */
public class ClassDiagramTestCase extends UMLTestCase{
    
    
    
    /** Creates a new instance of ClassDiagramTestCase */
    public ClassDiagramTestCase(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        return new NbTestSuite(org.netbeans.test.uml.classdiagram.ClassDiagramTestCase.class);
    }
    
    
    
    public void tearDown() throws FileNotFoundException, IOException{
        
        String workDir = System.getProperty("nbjunit.workdir");
        
        try{
            JDialogOperator dlgError = new JDialogOperator("Unexpected Exception");
            JTextAreaOperator textarea = new JTextAreaOperator(dlgError);
            String str = textarea.getDisplayedText();
            int pos = str.indexOf("\n");
            if(pos != -1){str = str.substring(1, pos-1);}
            dlgError.close();
            fail(" " + str);
        }catch(TimeoutExpiredException e1){
        }
        
        try{

            String OUT_LOG_FILE = workDir + File.separator + "jout_" + getName() + ".log";
            String ERR_LOG_FILE = workDir + File.separator + "jerr_" + getName() + ".log";
        
            PrintStream myOut = new PrintStream(new FileOutputStream(OUT_LOG_FILE), true);
            PrintStream myErr = new PrintStream(new FileOutputStream(ERR_LOG_FILE), true);
            
            BufferedReader myIn = new BufferedReader(new FileReader(ERR_LOG_FILE));
            String line;
            do {
                line = myIn.readLine();
                if (line!=null && line.indexOf("Exception")!=-1){
                    if ((line.indexOf("Unexpected Exception")==-1) &&
                            (line.indexOf("TimeoutExpiredException")==-1)){
                        //fail(line);
                    }
                }
            } while (line != null);
            
        }catch(Exception e){
            e.printStackTrace(getLog());
        }
        org.netbeans.test.umllib.util.Utils.tearDown();
    }
    
    
    protected void parseFindSourceTargetElement(UnexpectedElementSelectionException e){
        
        e.printStackTrace(getLog());
        String description = e.getDescription();
        UnexpectedElementSelectionException.Status status = e.getStatus();
        Object element = e.getElement();
        
        if ( element instanceof  DiagramElementOperator ){
            
            if (status == UnexpectedElementSelectionException.Status.NOTSELECTED ){
                //fail(//6299347, description);
                fail(description);
            }
        }
        
        fail(e);
    }
    
    protected DiagramOperator diagram = null;
    
}
