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


package org.netbeans.test.uml.robustness;
import java.awt.Point;
import java.io.*;
import org.netbeans.jemmy.*;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.uml.robustness.utils.RUtils;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.NewDiagramWizardOperator;
import org.netbeans.test.umllib.testcases.UMLMultiTestCase;
import org.netbeans.test.umllib.testcases.UMLMultiTestSuite;
import org.netbeans.test.umllib.util.LibProperties;
import org.netbeans.test.umllib.vrf.GenericVerifier;

/**
 *
 * @author yaa
 * @spec uml/UMLRobustness.xml
 */
public class RobustnessNaming extends UMLMultiTestSuite{
    private static String prName1 = "UMLProject1";
    private static String prName2 = "UMLProject2";
    private static String cldName1 = "DClass1";
    private static String cldName2 = "DClass2";
    private static String cldName3 = "DClass3";
    private static String cpdName1 = "DComponent1";
    private static String cpdName2 = "DComponent2";
    private static String cpdName3 = "DComponent3";
    
    private static final String workDir = System.getProperty("nbjunit.workdir");
    private static String OUT_LOG_FILE = "";
    private static String ERR_LOG_FILE = "";
    private static PrintStream myOut = null;
    private static PrintStream myErr = null;
    private static BufferedReader myIn = null;
    
    /** Need to be defined because of JUnit */
    /*
    public RobustnessNaming(String name) {
        super(name);
    }
     */
    
    protected UMLMultiTestCase[] cases(){
        return new UMLMultiTestCase[]{
            new CPD_NameClassResWords(),
            new CLD_NameInterfaceKeyWords(),
            new CPD_NameClassIllegalChars(),
            new CPD_RenameInterfaceResWords(),
            new CLD_RenameClassKeyWords(),
            new CLD_RenameInterfaceIllegalChars()
        };
    }
    
    public static NbTestSuite suite() {
        return new RobustnessNaming();
    }
    
    
    public class CPD_NameClassResWords extends AbstractTest{
        
        public CPD_NameClassResWords(){
            log("CPD_NameClassResWords simple constructor");
        }
        
        public CPD_NameClassResWords(DiagramOperator diagram, int elementIdx){
            super("CPD_NameClassResWords"+elementIdx);
            log("CPD_NameClassResWords "+elementIdx+" constructor");
            this.elementIdx = elementIdx;
            this.diagram = diagram;
        }
        
        public void prepare(){
            if (diagram == null)
                diagram = RUtils.openDiagram(prName1, cpdName2, NewDiagramWizardOperator.COMPONENT_DIAGRAM, workDir);
            //qa.uml.util.Utils.closeDialog("Element Navigation", "Cancel");
        }
        
        public UMLMultiTestCase create(){
            if (diagram == null)
                fail("Can't open diagram '" + cldName1 + "', project '" + prName1 + "'.");
            
            if (elementIdx < RUtils.resWords.length)
                return new CPD_NameClassResWords(diagram, elementIdx++);
            return null;
        }
        
        public void execute(){
            String illegalName = RUtils.resWords[elementIdx];
            
            try {
                Point p = diagram.getDrawingArea().getFreePoint();
                RUtils.closeInvalidValueDlg();
                DiagramElementOperator el = diagram.putElementOnDiagram(illegalName, ElementTypes.CLASS, p.x, p.y);
            }catch(Exception e){}
            
            try{
                DiagramElementOperator elem = new DiagramElementOperator(diagram, illegalName);
                fail(ElementTypes.CLASS + " has been named to reserved word '"+illegalName+"'");
            }catch(Exception e){
//            }finally {
//                new GenericVerifier(diagram).safeDeleteAllElements();
            }
        }
    }
    
    
    public class CLD_NameInterfaceKeyWords extends AbstractTest{
        
        public CLD_NameInterfaceKeyWords(){
            log("CLD_NameInterfaceKeyWords simple constructor");
        }
        
        public CLD_NameInterfaceKeyWords(DiagramOperator diagram, int elementIdx){
            super("CLD_NameInterfaceKeyWords"+elementIdx);
            log("CLD_NameInterfaceKeyWords "+elementIdx+" constructor");
            this.elementIdx = elementIdx;
            this.diagram = diagram;
        }
        
        public void prepare(){
            
            if (diagram == null)
                diagram = RUtils.openDiagram(prName1, cldName2, NewDiagramWizardOperator.CLASS_DIAGRAM, workDir);
            //qa.uml.util.Utils.closeDialog("Element Navigation", "Cancel");
        }
        
        public UMLMultiTestCase create(){
            if (diagram == null)
                fail("Can't open diagram '" + cldName1 + "', project '" + prName1 + "'.");
            
            if (elementIdx < RUtils.keyWords.length)
                return new CLD_NameInterfaceKeyWords(diagram, elementIdx++);
            return null;
        }
        
        public void execute(){
            String illegalName = RUtils.keyWords[elementIdx];
            
            try {
                Point p = diagram.getDrawingArea().getFreePoint();
                RUtils.closeInvalidValueDlg();
                DiagramElementOperator el = diagram.putElementOnDiagram(illegalName, ElementTypes.INTERFACE, p.x, p.y);
            }catch(Exception e){}
            
            try{
                DiagramElementOperator elem = new DiagramElementOperator(diagram, illegalName);
                fail(ElementTypes.INTERFACE + " has been named as reserved word '"+illegalName+"'");
            }catch(Exception e){
//            }finally{
//                new GenericVerifier(diagram).safeDeleteAllElements();
            }
            
        }
    }
    
    
    
    public class CPD_NameClassIllegalChars extends AbstractTest{
        
        public CPD_NameClassIllegalChars(){
            log("CPD_NameClassIllegalChars simple constructor");
        }
        
        public CPD_NameClassIllegalChars(DiagramOperator diagram, int elementIdx){
            super("CPD_NameClassIllegalChars"+elementIdx);
            log("CPD_NameClassIllegalChars "+elementIdx+" constructor");
            this.elementIdx = elementIdx;
            this.diagram = diagram;
        }
        
        public void prepare(){
            if (diagram == null)
                diagram = RUtils.openDiagram(prName1, cpdName3, NewDiagramWizardOperator.COMPONENT_DIAGRAM, workDir);
            //qa.uml.util.Utils.closeDialog("Element Navigation", "Cancel");
        }
        
        public UMLMultiTestCase create(){
            if (diagram == null)
                fail("Can't open diagram '" + cldName1 + "', project '" + prName1 + "'.");
            
            if (elementIdx < RUtils.illegalChars.length)
                return new CPD_NameClassIllegalChars(diagram, elementIdx++);
            return null;
        }
        
        public void execute(){
            String illegalName = ElementTypes.CLASS.toString().substring(0, 3) + "_" + RUtils.illegalChars[elementIdx] + "_ch";
            
            try {
                Point p = diagram.getDrawingArea().getFreePoint();
                RUtils.closeInvalidValueDlg();
                DiagramElementOperator el = diagram.putElementOnDiagram(illegalName, ElementTypes.CLASS, p.x, p.y);
            }catch(Exception e){}
            
            try{
                DiagramElementOperator elem = new DiagramElementOperator(diagram, illegalName);
                fail(ElementTypes.CLASS + " has been named to value with illegal char '" + illegalName + "'");
            }catch(Exception e){
//            }finally{
//                new GenericVerifier(diagram).safeDeleteAllElements();
            }
            
        }
    }
    
    
    public class CPD_RenameInterfaceResWords extends AbstractTest{
        
        public CPD_RenameInterfaceResWords(){
            log("CPD_RenameInterfaceResWords simple constructor");
        }
        
        public CPD_RenameInterfaceResWords(DiagramOperator diagram, int elementIdx){
            super("CPD_RenameInterfaceResWords"+elementIdx);
            log("CPD_RenameInterfaceResWords "+elementIdx+" constructor");
            this.elementIdx = elementIdx;
            this.diagram = diagram;
        }
        
        public void prepare(){
            if (diagram == null)
                diagram = RUtils.openDiagram(prName2, cpdName1, NewDiagramWizardOperator.COMPONENT_DIAGRAM, workDir);
            //qa.uml.util.Utils.closeDialog("Element Navigation", "Cancel");
        }
        
        public UMLMultiTestCase create(){
            if (diagram == null)
                fail("Can't open diagram '" + cldName1 + "', project '" + prName2 + "'.");
            
            if (elementIdx < RUtils.resWords.length)
                return new CPD_RenameInterfaceResWords(diagram, elementIdx++);
            return null;
        }
        
        public void execute(){
            String elementName = ElementTypes.INTERFACE.toString().substring(0, 3) + "_rw_" + elementIdx;
            String illegalName = RUtils.resWords[elementIdx];
            try {
                Point p = diagram.getDrawingArea().getFreePoint();
                DiagramElementOperator el = diagram.putElementOnDiagram(elementName, ElementTypes.INTERFACE, p.x, p.y);
                el.select();
                el.select();
                RUtils.closeInvalidValueDlg();
                LibProperties.getCurrentNamer(ElementTypes.INTERFACE).setName(diagram.getDrawingArea(), p.x, p.y, illegalName);
            }catch(Exception e){}
            
            try{
                DiagramElementOperator elem = new DiagramElementOperator(diagram, elementName);
            }catch(Exception e){
                fail(ElementTypes.INTERFACE + " "+elementName+" probably has been renamed to reserved word '"+illegalName+"'");
//            } finally {
//                new GenericVerifier(diagram).safeDeleteAllElements();
            }
        }
        
        
    }
    
    
    /////////////
    public class CLD_RenameClassKeyWords extends AbstractTest{
        
        public CLD_RenameClassKeyWords(){
            log("CLD_RenameClassKeyWords simple constructor");
        }
        
        public CLD_RenameClassKeyWords(DiagramOperator diagram, int elementIdx){
            super("CLD_RenameClassKeyWords"+elementIdx);
            log("CLD_RenameClassKeyWords "+elementIdx+" constructor");
            this.elementIdx = elementIdx;
            this.diagram = diagram;
        }
        
        public void prepare(){
            if (diagram == null)
                diagram = RUtils.openDiagram(prName2, cldName1, NewDiagramWizardOperator.CLASS_DIAGRAM, workDir);
            //qa.uml.util.Utils.closeDialog("Element Navigation", "Cancel");
        }
        
        public UMLMultiTestCase create(){
            if (diagram == null)
                fail("Can't open diagram '" + cldName1 + "', project '" + prName2 + "'.");
            
            if (elementIdx < RUtils.keyWords.length)
                return new CLD_RenameClassKeyWords(diagram, elementIdx++);
            return null;
        }
        
        public void execute(){
            String elementName = ElementTypes.CLASS.toString().substring(0, 3) + "_kw_" + elementIdx;
            String illegalName = RUtils.keyWords[elementIdx];
            
            try {
                Point p = diagram.getDrawingArea().getFreePoint();
                
                DiagramElementOperator el = diagram.putElementOnDiagram(elementName, ElementTypes.CLASS, p.x, p.y);
                el.select();
                el.select();
                RUtils.closeInvalidValueDlg();
                LibProperties.getCurrentNamer(ElementTypes.CLASS).setName(diagram.getDrawingArea(), p.x, p.y, illegalName);
            }catch(Exception e){}
            
            try{
                DiagramElementOperator elem = new DiagramElementOperator(diagram, elementName);
            }catch(Exception e){
                fail(ElementTypes.CLASS + " "+elementName+" has been renamed to keyword '"+illegalName+"'");
//            } finally {
//                new GenericVerifier(diagram).safeDeleteAllElements();
            }
        }
    }
    
    
    
    
    /////////////
    public class CLD_RenameInterfaceIllegalChars extends AbstractTest{
        // used to store element current element index
        
        public CLD_RenameInterfaceIllegalChars(){
            log("CLD_RenameInterfaceIllegalChars simple constructor");
        }
        
        public CLD_RenameInterfaceIllegalChars(DiagramOperator diagram, int elementIdx){
            super("CLD_RenameInterfaceIllegalChars"+elementIdx);
            log("CLD_RenameInterfaceIllegalChars "+elementIdx+" constructor");
            this.elementIdx = elementIdx;
            this.diagram = diagram;
        }
        
        public void prepare(){
            if (diagram == null)
                diagram = RUtils.openDiagram(prName2, cldName3, NewDiagramWizardOperator.CLASS_DIAGRAM, workDir);
        }
        
        public UMLMultiTestCase create(){
            if (diagram == null)
                fail("Can't open diagram '" + cldName1 + "', project '" + prName2 + "'.");
            
            if (elementIdx < RUtils.illegalChars.length)
                return new CLD_RenameInterfaceIllegalChars(diagram, elementIdx++);
            return null;
        }
        
        public void execute(){
            String elementName = ElementTypes.INTERFACE.toString().substring(0,3) + "_ic_" + elementIdx;
            String illegalName = ElementTypes.INTERFACE.toString().substring(0,3) + "_" + RUtils.illegalChars[elementIdx] + "_ch";
            
            try {
                Point p = diagram.getDrawingArea().getFreePoint();
                DiagramElementOperator el = diagram.putElementOnDiagram(elementName, ElementTypes.INTERFACE, p.x, p.y);
                el.select();
                el.select();
                RUtils.closeInvalidValueDlg();
                LibProperties.getCurrentNamer(ElementTypes.INTERFACE).setName(diagram.getDrawingArea(), p.x, p.y, illegalName);
            }catch(Exception e){}
            
            try{
                DiagramElementOperator elem = new DiagramElementOperator(diagram, elementName);
            }catch(Exception e){
                fail(ElementTypes.INTERFACE  + " "+elementName+ " has been renamed to value with illegal char '"+illegalName+"'");
//            } finally {
//                new GenericVerifier(diagram).safeDeleteAllElements();
            }
            
        }
        
        
    }
    
    
    /**
     * Abstarct test class. It's purpose is to realize startUp and tearDown,
     * that are common for all other tests, only once
     */
    public abstract class AbstractTest extends UMLMultiTestCase{
        // used to store element current element index
        protected int elementIdx = 0;
        protected DiagramOperator diagram = null;

        public AbstractTest(){}
        
        public AbstractTest(String name){
            super(name);
        }
        
        public void cleanup(){
            org.netbeans.test.umllib.util.Utils.tearDown();
            if (diagram != null)
                diagram.closeAllDocuments();
            
        }
        
        
        public void setUp() throws FileNotFoundException{
            System.out.println("########  "+getName()+"  #######");
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 3000);
            JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", 3000);
            JemmyProperties.setCurrentTimeout("Waiter.WaitingTime", 3000);
            
            OUT_LOG_FILE = workDir + File.separator + "jout_" + getName() + ".log";
            ERR_LOG_FILE = workDir + File.separator + "jerr_" + getName() + ".log";
            
            myOut = new PrintStream(new FileOutputStream(OUT_LOG_FILE), true);
            myErr = new PrintStream(new FileOutputStream(ERR_LOG_FILE), true);
            JemmyProperties.setCurrentOutput(new TestOut(System.in, myOut, myErr));
        }
        
        public void tearDown() throws FileNotFoundException, IOException{
            closeAllModal();
            try{
                DiagramOperator diagram=new DiagramOperator("D");
                new GenericVerifier(diagram).safeDeleteAllElements();
            }catch(Exception ex){};
            
            /*
            try{
                JDialogOperator dlgError = new JDialogOperator("Unexpected Exception");
                JTextAreaOperator textarea = new JTextAreaOperator(dlgError);
                String str = textarea.getDisplayedText();
                int pos = str.indexOf("\n");
                if(pos != -1){str = str.substring(1, pos-1);}
                dlgError.close();
                fail(" " + str);
            }catch(TimeoutExpiredException e){}
            */
            myIn = new BufferedReader(new FileReader(ERR_LOG_FILE));
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
        }
    }
    
    
}

