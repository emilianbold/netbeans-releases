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




package org.netbeans.test.uml.sqd;


import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.File;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.SaveAllAction;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.junit.NbTestSuite;

import org.netbeans.test.uml.sqd.utils.CustomPropertiesHelper;
import org.netbeans.test.uml.sqd.utils.PropertyVerifier;
import org.netbeans.test.uml.sqd.utils.Util;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.customelements.SequenceDiagramOperator;
import org.netbeans.test.umllib.project.Project;
import org.netbeans.test.umllib.testcases.UMLTestCase;




public class SQDCombinedFragmentPropertiesTest extends UMLTestCase {
    
        
    public SQDCombinedFragmentPropertiesTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(SQDCombinedFragmentPropertiesTest.class);
        return suite;
    }
        
        
    /******************const section*************************/
    private String PROJECT_NAME = "SQD_umlCFPT";      
    private String JAVA_PROJECT_NAME = "SQD_java";      
    private String EXCEPTION_DLG = "Exception";
    private String PKG_PATH = "Model|sqd";
    private String DIAGRAM = "EmptySequenceDiagram";
    private String PATH_TO_DIAGRAM = "Model|sqd|"+DIAGRAM+"|"+DIAGRAM;
    private String ELEMENT_NAME = "FRAGMENT";        
    private String PROPS_NAME = "Properties";
    private final String PROJECT_PATH = System.getProperty("nbjunit.workdir");
    //public final String OPEN_DIAGRAM = "Open";
    //public final String DELETE_DIAGRAM = "Delete";
    
   
    /********************************************************/
    
    
    
    Util util = new Util(PROJECT_NAME);
    private EventTool eventTool = new EventTool();
    private boolean failedByBug = false;
    private static PropertyVerifier verifier = null;            
    private static DiagramElementOperator fragment = null;
    private static SequenceDiagramOperator dia = null;
            
    
    private static boolean initialized = false;
    
    private String lastTestCase=null;
    
    
    protected void setUp() {
        eventTool.waitNoEvent(2000);                   
        if (!initialized){                 
                //util.closeStartupException();
                //associating java project
                //util.associateJavaProject(PROJECT_NAME, JAVA_PROJECT_NAME);                
                Project.openProject(this.XTEST_PROJECT_DIR+File.separator+"Project-SQD");
                org.netbeans.test.umllib.Utils.createUMLProjectFromJavaProject(PROJECT_NAME, JAVA_PROJECT_NAME, PROJECT_PATH);
                
                eventTool.waitNoEvent(2000);
                                        
                //setting up environment
                util.addDiagram(DIAGRAM, PKG_PATH);
                dia = new SequenceDiagramOperator(DIAGRAM);
                try
                {
                    new TopComponentOperator(DIAGRAM+" - UML Documentation").close();
                }catch(Exception ex)
                {
                    
                }
                dia.createGenericElementOnDiagram(null, ElementTypes.COMBINED_FRAGMENT);
                fragment = new DiagramElementOperator(dia, new DiagramElementOperator.ElementByTypeChooser(ElementTypes.COMBINED_FRAGMENT),0);
                //eventTool.waitNoEvent(1500);
                //dia = new SequenceDiagramOperator(DIAGRAM);
                
                eventTool.waitNoEvent(1500);
                Point p = dia.getDrawingArea().getFreePoint(50);
                dia.getDrawingArea().clickMouse(p.x, p.y, 1);
                fragment.waitSelection(false);
                eventTool.waitNoEvent(1500);
                //setting up verifier
                verifier = new PropertyVerifier(PROPS_NAME);                
                initialized = true;
                //
                dia = new SequenceDiagramOperator(DIAGRAM);
                try{Thread.sleep(100);}catch(Exception ex){}
        }else{
            eventTool.waitNoEvent(1500);
            Point p = dia.getDrawingArea().getFreePoint(50);
            dia.getDrawingArea().clickMouse(p.x, p.y, 1);
            fragment.waitSelection(false);
            eventTool.waitNoEvent(1500);
            
        }
        dia.getDrawingArea().pushKey(KeyEvent.VK_TAB);
        try{Thread.sleep(100);}catch(Exception ex){}
        org.netbeans.test.umllib.util.Utils.makeScreenShotCustom(null,"beforeWait");
        fragment.waitSelection(true);
    }
    
    
    private void cleanIDE(){
        try{
            //EditorOperator.closeDiscardAll();            
            eventTool.waitNoEvent(500);	            
        }catch(Exception e){}
    }
    
   
     public void testCheckNameAndAlias(){
        lastTestCase=getCurrentTestMethodName();;
        String propertyName = "Name";
        String oldValue = "";
        final String newValue = "New"+ELEMENT_NAME+"Val";
        String renderer = PropertyVerifier.STRING_RENDERER;
        boolean editable = true;
            boolean result = verifier.verifyProperty(propertyName, renderer, editable, oldValue, newValue);
            if (!result){
                fail("testCheckName failed. Reason unknown");
            }
            new SequenceDiagramOperator(DIAGRAM).makeComponentVisible();
            new SequenceDiagramOperator(DIAGRAM).getDrawingArea().clickMouse(9,40,1);
            try{Thread.sleep(100);}catch(Exception ex){}
        dia.getDrawingArea().pushKey(KeyEvent.VK_TAB);
        fragment.waitSelection(true);
            propertyName = "Alias";
            oldValue = "New"+ELEMENT_NAME+"Val";
            String newAlias = "newalias";
                    
            result = verifier.verifyProperty(propertyName, renderer, editable, oldValue, newAlias);
            if (!result){
                fail("Verification failed");
            }            
    }
    
     
    public void testCheckVisibility(){
        lastTestCase=getCurrentTestMethodName();;
        String propertyName = "Visibility";
        String oldValue = "public";
        final String newValue = "package";
        String renderer = PropertyVerifier.COMBOBOX_RENDERER;
        boolean editable = false;
            
            boolean result = verifier.verifyProperty(propertyName, renderer, editable, oldValue, newValue);
            if (!result){
                fail("testCheckVisiblity failed. Reason unknown");
            }            
    } 
     
    
    public void testCheckTaggedValues(){
        lastTestCase=getCurrentTestMethodName();;
        String propertyName = "Tagged Values";
        String oldValue = "";
        final String newValue = "nam=val";
        String renderer = PropertyVerifier.CUSTOM_RENDERER;
        boolean editable = false;
                
            PropertyVerifier custom = new PropertyVerifier(PROPS_NAME){                
                protected boolean setNewValue(String propertyName, String newValue){
                    Property prop = new Property(properties, propertyName);
                    new CustomPropertiesHelper().addTaggedValue("nam", "als","val", prop);
                    return true;
                }                
            };
            
            boolean result = custom.verifyProperty(propertyName, renderer, editable, oldValue, newValue);
            if (!result){
                fail("Verification failed");
            }            
    }
    
    
    public void testCheckStereotypes(){
        lastTestCase=getCurrentTestMethodName();;
        String propertyName = "Stereotypes";
        String oldValue = "";
        final String newValue = "ster";
        String renderer = PropertyVerifier.CUSTOM_RENDERER;
        boolean editable = false;                
            
            PropertyVerifier custom = new PropertyVerifier(PROPS_NAME){                
                protected boolean setNewValue(String propertyName, String newValue){                    
                    Property prop = new Property(properties, propertyName);
                    new CustomPropertiesHelper().addStereotype(newValue, prop);
                    return true;
                }                
            };
            
            boolean result = custom.verifyProperty(propertyName, renderer, editable, oldValue, newValue);
            if (!result){
                fail("Verification failed");
            }            
    }
    
    
    
    public void testCheckConstraints(){
        lastTestCase=getCurrentTestMethodName();;
        String propertyName = "Constraints";
        String oldValue = "";
        final String newValue = "nam:exp";
        String renderer = PropertyVerifier.CUSTOM_RENDERER;
        boolean editable = false;
                
            
            PropertyVerifier custom = new PropertyVerifier(PROPS_NAME){                
                protected boolean setNewValue(String propertyName, String newValue){
                    Property prop = new Property(properties, propertyName);
                    new CustomPropertiesHelper().addConstraints("nam", "exp", prop);
                    return true;
                }                
            };
            
            boolean result = custom.verifyProperty(propertyName, renderer, editable, oldValue, newValue);
            if (!result){
                fail("Verification failed");
            }            
    }
    
    
    
    public void testPhysicalFileLocation(){        
        lastTestCase=getCurrentTestMethodName();;
        String propertyName = "Physical File Location";
        String oldValue = "";        
        final String newValue = "";
        String renderer = PropertyVerifier.STRING_RENDERER;
        boolean editable = false;
                
            boolean result = verifier.verifyProperty(propertyName, renderer, editable, oldValue, newValue);
            if (!result){
                fail("testPhysicalFileLocation failed. Reason unknown");
            }            
    }
    
    
    
    public void tearDown() {
        org.netbeans.test.umllib.util.Utils.makeScreenShot(lastTestCase);
        long timeoutValDlg = JemmyProperties.getCurrentTimeout("DialogWaiter.WaitDialogTimeout");
        try{                        
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 1000);
            new JDialogOperator(EXCEPTION_DLG).close();
            if (!failedByBug){
                fail("Unexpected Exception dialog was found");
            }            
        }catch(Exception excp){
        }finally{
            org.netbeans.test.umllib.util.Utils.saveAll();
            closeAllModal();
            if (failedByBug){
                failedByBug = false;                        
            }                        
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", timeoutValDlg);            
            //TODO: should be removed later
            util.closeSaveDlg();
        }        
    }
    
    
      
    
    public void testCheckOperator(){
        lastTestCase=getCurrentTestMethodName();;
        //TODO: implement checkElement method for correct check.
        String propertyName = "Operator";
        final String lineName = fragment.getSubjectVNs().get(0);
        String oldValue = "assert";
        final String newValue = "loop";
        String renderer = PropertyVerifier.COMBOBOX_RENDERER;
        boolean editable = false;
            
            /*
            PropertyVerifier custom = new PropertyVerifier(PROPS_NAME){                
                protected boolean checkElement(){
                    try{
                        DiagramElementOperator el = new DiagramElementOperator(dia, new DiagramElementOperator.ElementByTypeChooser(ElementTypes.COMBINED_FRAGMENT), 0);
                        new CompartmentOperator(el,)
                        return true;
                    }catch(Exception e){
                        return false;
                    }
                }                
            }; 
            */
            
            boolean result = verifier.verifyProperty(propertyName, renderer, editable, oldValue, newValue);
            if (!result){
                fail("testCheckOperator failed. Reason unknown");
            }
            
     }
     
    
    
    
    
}
