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


package org.netbeans.test.uml.diagramcontextmenu;
import java.io.*;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.*;

import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.umllib.*;
import org.netbeans.test.uml.diagramcontextmenu.utils.DCMUtils;
import org.netbeans.test.umllib.testcases.UMLTestCase;
import org.netbeans.test.umllib.util.PopupConstants;

/**
 *
 * @author yaa
 * @spec UML/Diagram_ContextMenus.xml
 */
public class DiagramContextMenus_CLD extends UMLTestCase {
    private static String prName = "UMLProjectDCntMn";
    private static String cldName = "DClass";
    
    private static final String workDir = System.getProperty("nbjunit.workdir");
    private static String OUT_LOG_FILE = "";
    private static String ERR_LOG_FILE = "";
    private static PrintStream myOut = null;
    private static PrintStream myErr = null;
    private static BufferedReader myIn = null;
    
    private String lastTestCase=null;

    /** Need to be defined because of JUnit */
    public DiagramContextMenus_CLD(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(org.netbeans.test.uml.diagramcontextmenu.DiagramContextMenus_CLD.class);
        return suite;
    }
    
/**
 * @caseblock Class Diagram
 * @usecase Check context menu of class diagram
 */
    public void testItemCopy(){
        lastTestCase=getCurrentTestMethodName();
        String itemName = PopupConstants.EDIT + "|" + PopupConstants.COPY;
        JMenuItemOperator item = DCMUtils.checkDiagramPopupMenuItem(diagram, itemName);
        if(item==null){
            fail("Diagram popum menu item '" + itemName + "' not found");
        }else if(item.isEnabled()){
            fail("Diagram popum menu item '" + itemName + "' is enabled but should not be");
        }
    }

/**
 * @caseblock Class Diagram
 * @usecase Check context menu of class diagram
 */
    public void testItemCut(){
        lastTestCase=getCurrentTestMethodName();
        String itemName = PopupConstants.EDIT + "|" + PopupConstants.CUT;
        JMenuItemOperator item = DCMUtils.checkDiagramPopupMenuItem(diagram, itemName);
        if(item==null){
            fail("Diagram popum menu item '" + itemName + "' not found");
        }else if(item.isEnabled()){
            fail("Diagram popum menu item '" + itemName + "' is enabled but should not be");
        }
    }

/**
 * @caseblock Class Diagram
 * @usecase Check context menu of class diagram
 */
    public void testItemDelete(){
        lastTestCase=getCurrentTestMethodName();
        String itemName = PopupConstants.EDIT + "|" + PopupConstants.DELETE;
        JMenuItemOperator item = DCMUtils.checkDiagramPopupMenuItem(diagram, itemName);
        if(item==null){
            fail("Diagram popum menu item '" + itemName + "' not found");
        }else if(item.isEnabled()){
            fail("Diagram popum menu item '" + itemName + "' is enabled but should not be");
        }
    }

/**
 * @caseblock Class Diagram
 * @usecase Check context menu of class diagram
 */
    public void testItemPaste(){
        lastTestCase=getCurrentTestMethodName();
        String itemName = PopupConstants.EDIT + "|" + PopupConstants.PASTE;
        JMenuItemOperator item = DCMUtils.checkDiagramPopupMenuItem(diagram, itemName);
        if(item==null){
            fail("Diagram popum menu item '" + itemName + "' not found");
        }else if(item.isEnabled()){
            fail("Diagram popum menu item '" + itemName + "' is enabled but should not be");
        }
    }
    
/**
 * @caseblock Class Diagram
 * @usecase Check context menu of class diagram
 */
    public void testItemSelectAll(){
        lastTestCase=getCurrentTestMethodName();
        String itemName = PopupConstants.EDIT + "|" + PopupConstants.SELECT_ALL;
        JMenuItemOperator item = DCMUtils.checkDiagramPopupMenuItem(diagram, itemName);
        if(item==null){
            fail("Diagram popum menu item '" + itemName + "' not found");
        }else if(!item.isEnabled()){
            fail("Diagram popum menu item '" + itemName + "' is disabled but should not be");
        }
    }
    
/**
 * @caseblock Class Diagram
 * @usecase Check context menu of class diagram
 */
    public void testItemSelectAllSimilar(){
        lastTestCase=getCurrentTestMethodName();
        String itemName = PopupConstants.EDIT + "|" + PopupConstants.SELECT_ALL_SIMILAR;
        JMenuItemOperator item = DCMUtils.checkDiagramPopupMenuItem(diagram, itemName);
        if(item==null){
            fail("Diagram popum menu item '" + itemName + "' not found");
        }else if(item.isEnabled()){
            fail("Diagram popum menu item '" + itemName + "' is enabled but should not be");
        }
    }
    
/**
 * @caseblock Class Diagram
 * @usecase Check context menu of class diagram
 */
    public void testItemInvertSelection(){
        lastTestCase=getCurrentTestMethodName();
        String itemName = PopupConstants.EDIT + "|" + PopupConstants.INVERT_SELECTION;
        JMenuItemOperator item = DCMUtils.checkDiagramPopupMenuItem(diagram, itemName);
        if(item==null){
            fail("Diagram popum menu item '" + itemName + "' not found");
        }else if(!item.isEnabled()){
            fail("Diagram popum menu item '" + itemName + "' is disabled but should not be");
        }
    }
/**
 * @caseblock Class Diagram
 * @usecase Check context menu of class diagram
 */
    public void testItemSetDimensions(){
        lastTestCase=getCurrentTestMethodName();
        String itemName = PopupConstants.EDIT + "|" + PopupConstants.SET_DIMESIONS;
        JMenuItemOperator item = DCMUtils.checkDiagramPopupMenuItem(diagram, itemName);
        if(item==null){
            fail("Diagram popum menu item '" + itemName + "' not found");
        }else if(item.isEnabled()){
            fail("Diagram popum menu item '" + itemName + "' is enabled but should not be");
        }
    }

/**
 * @caseblock Class Diagram
 * @usecase Check context menu of class diagram
 */
    public void testItemLayoutHierarchical(){
        lastTestCase=getCurrentTestMethodName();
        String itemName = DCMUtils.DiagramPopupConstants.LAYOUT + "|" + DCMUtils.DiagramPopupConstants.LAYOUT_HIERARCHICAL;
        JMenuItemOperator item = DCMUtils.checkDiagramPopupMenuItem(diagram, itemName);
        if(item==null){
            fail("Diagram popum menu item '" + itemName + "' not found");
        }else if(!item.isEnabled()){
            fail("Diagram popum menu item '" + itemName + "' is disabled but should not be");
        }
    }

/**
 * @caseblock Class Diagram
 * @usecase Check context menu of class diagram
 */
    public void testItemLayoutOrthogonal(){
        lastTestCase=getCurrentTestMethodName();
        String itemName = DCMUtils.DiagramPopupConstants.LAYOUT + "|" + DCMUtils.DiagramPopupConstants.LAYOUT_ORTHOGONAL;
        JMenuItemOperator item = DCMUtils.checkDiagramPopupMenuItem(diagram, itemName);
        if(item==null){
            fail("Diagram popum menu item '" + itemName + "' not found");
        }else if(!item.isEnabled()){
            fail("Diagram popum menu item '" + itemName + "' is disabled but should not be");
        }
    }

/**
 * @caseblock Class Diagram
 * @usecase Check context menu of class diagram
 */
    public void testItemLayoutSymmetric(){
        lastTestCase=getCurrentTestMethodName();
        String itemName = DCMUtils.DiagramPopupConstants.LAYOUT + "|" + DCMUtils.DiagramPopupConstants.LAYOUT_SYMMETRIC;
        JMenuItemOperator item = DCMUtils.checkDiagramPopupMenuItem(diagram, itemName);
        if(item==null){
            fail("Diagram popum menu item '" + itemName + "' not found");
        }else if(!item.isEnabled()){
            fail("Diagram popum menu item '" + itemName + "' is disabled but should not be");
        }
    }

/**
 * @caseblock Class Diagram
 * @usecase Check context menu of class diagram
 */
    public void testItemLayoutIncremental(){
        lastTestCase=getCurrentTestMethodName();
        String itemName = DCMUtils.DiagramPopupConstants.LAYOUT + "|" + DCMUtils.DiagramPopupConstants.LAYOUT_INCREMENTAL;
        JMenuItemOperator item = DCMUtils.checkDiagramPopupMenuItem(diagram, itemName);
        if(item==null){
            fail("Diagram popum menu item '" + itemName + "' not found");
        }else if(!item.isEnabled()){
            fail("Diagram popum menu item '" + itemName + "' is disabled but should not be");
        }
    }

/**
 * @caseblock Class Diagram
 * @usecase Check context menu of class diagram
 */
    public void testItemLayoutProperties(){
        lastTestCase=getCurrentTestMethodName();
        String itemName = DCMUtils.DiagramPopupConstants.LAYOUT + "|" + DCMUtils.DiagramPopupConstants.LAYOUT_PROPERTIES;
        JMenuItemOperator item = DCMUtils.checkDiagramPopupMenuItem(diagram, itemName);
        if(item==null){
            fail("Diagram popum menu item '" + itemName + "' not found");
        }else if(!item.isEnabled()){
            fail("Diagram popum menu item '" + itemName + "' is disabled but should not be");
        }else{
            DCMUtils.pushDiagramPopupMenuItem(diagram, itemName);
            if(!DCMUtils.findAndCloseDialog(DCMUtils.DialogTitles.LAYOUT_PROPERTIES)){
                fail("Dialog with title '" + DCMUtils.DialogTitles.LAYOUT_PROPERTIES + "' not found or not closed correctly ("+itemName+")");
            }
        }
    }
 
/**
 * @caseblock Class Diagram
 * @usecase Check context menu of class diagram
 */
    public void testItemZoom(){
        lastTestCase=getCurrentTestMethodName();
        String itemName = DCMUtils.DiagramPopupConstants.ZOOM;
        JMenuItemOperator item = DCMUtils.checkDiagramPopupMenuItem(diagram, itemName);
        if(item==null){
            fail("Diagram popum menu item '" + itemName + "' not found");
        }else if(!item.isEnabled()){
            fail("Diagram popum menu item '" + itemName + "' is disabled but should not be");
        }else{
            DCMUtils.pushDiagramPopupMenuItem(diagram, itemName);
            if(!DCMUtils.findAndCloseDialog(DCMUtils.DialogTitles.ZOOM)){
                fail("Dialog with title '" + itemName + "' not found or not closed correctly");
            }
        }
    }
    
/**
 * @caseblock Class Diagram
 * @usecase Check context menu of class diagram
 */
    public void testItemZoomIn(){
        lastTestCase=getCurrentTestMethodName();
        String itemName = DCMUtils.DiagramPopupConstants.ZOOM_IN;
        JMenuItemOperator item = DCMUtils.checkDiagramPopupMenuItem(diagram, itemName);
        if(item==null){
            fail("Diagram popum menu item '" + itemName + "' not found");
        }else if(!item.isEnabled()){
            fail("Diagram popum menu item '" + itemName + "' is disabled but should not be");
        }
    }
    
/**
 * @caseblock Class Diagram
 * @usecase Check context menu of class diagram
 */
    public void testItemZoomOut(){
        lastTestCase=getCurrentTestMethodName();
        String itemName = DCMUtils.DiagramPopupConstants.ZOOM_OUT;
        JMenuItemOperator item = DCMUtils.checkDiagramPopupMenuItem(diagram, itemName);
        if(item==null){
            fail("Diagram popum menu item '" + itemName + "' not found");
        }else if(!item.isEnabled()){
            fail("Diagram popum menu item '" + itemName + "' is disabled but should not be");
        }
    }
    
/**
 * @caseblock Class Diagram
 * @usecase Check context menu of class diagram
 */
    public void testItemSynchronize(){
        lastTestCase=getCurrentTestMethodName();
        String itemName = DCMUtils.DiagramPopupConstants.SYNCHRONIZE;
        JMenuItemOperator item = DCMUtils.checkDiagramPopupMenuItem(diagram, itemName);
        if(item==null){
            fail("Diagram popum menu item '" + itemName + "' not found");
        }else if(!item.isEnabled()){
            fail("Diagram popum menu item '" + itemName + "' is disabled but should not be");
        }
    }

/**
 * @caseblock Class Diagram
 * @usecase Check context menu of class diagram
 */
    public void testItemAssociateWith(){
        lastTestCase=getCurrentTestMethodName();
        String itemName = DCMUtils.DiagramPopupConstants.ASSOCIATE_WITH;
        JMenuItemOperator item = DCMUtils.checkDiagramPopupMenuItem(diagram, itemName);
        if(item==null){
            fail("Diagram popum menu item '" + itemName + "' not found");
        }else if(!item.isEnabled()){
            fail("Diagram popum menu item '" + itemName + "' is disabled but should not be");
        }else{
            DCMUtils.pushDiagramPopupMenuItem(diagram, itemName);
            if(!DCMUtils.findAndCloseDialog(DCMUtils.DialogTitles.ASSOCIATE_WITH)){
                fail("Dialog with title '" + itemName + "' not found or not closed correctly");
            }
        }
    }

/**
 * @caseblock Class Diagram
 * @usecase Check context menu of class diagram
 */
    public void testItemApplyDesignPattern(){
        lastTestCase=getCurrentTestMethodName();
        String itemName = DCMUtils.DiagramPopupConstants.APPLY_DESIGN_PATTERN;
        JMenuItemOperator item = DCMUtils.checkDiagramPopupMenuItem(diagram, itemName);
        if(item==null){
            fail("Diagram popum menu item '" + itemName + "' not found");
        }else if(!item.isEnabled()){
            fail("Diagram popum menu item '" + itemName + "' is disabled but should not be");
        }else{
            DCMUtils.pushDiagramPopupMenuItem(diagram, itemName);
            if(!DCMUtils.findAndCloseDialog(DCMUtils.DialogTitles.APPLY_DESIGN_PATTERN)){
                fail("Dialog with title '" + itemName + "' not found or not closed correctly");
            }
        }
    }
    
/**
 * @caseblock Class Diagram
 * @usecase Check context menu of class diagram
 */
    public void testItemProperties(){
        lastTestCase=getCurrentTestMethodName();
        String itemName = DCMUtils.DiagramPopupConstants.PROPERTIES;
        JMenuItemOperator item = DCMUtils.checkDiagramPopupMenuItem(diagram, itemName);
        if(item==null){
            fail("Diagram popum menu item '" + itemName + "' not found");
        }else if(!item.isEnabled()){
            fail("Diagram popum menu item '" + itemName + "' is disabled but should not be");
        }else{
            DCMUtils.pushDiagramPopupMenuItem(diagram, itemName);
            if(!DCMUtils.findAndCloseDialog(DCMUtils.DialogTitles.PROPERTIES)){
                fail("Dialog with title '" + itemName + "' not found or not closed correctly");
            }
        }
    }

/**
 * @caseblock Class Diagram
 * @usecase Check context menu of class diagram
 */
    public void testCheckUnnecessaryItems(){
        lastTestCase=getCurrentTestMethodName();
        String[] itemsNecessary = new String[]{
            PopupConstants.EDIT + "|" + PopupConstants.COPY,
            PopupConstants.EDIT + "|" + PopupConstants.CUT,
            PopupConstants.EDIT + "|" + PopupConstants.DELETE,
            PopupConstants.EDIT + "|" + PopupConstants.PASTE,
            PopupConstants.EDIT + "|" + PopupConstants.SELECT_ALL,
            PopupConstants.EDIT + "|" + PopupConstants.SELECT_ALL_SIMILAR,
            PopupConstants.EDIT + "|" + PopupConstants.INVERT_SELECTION,
            PopupConstants.EDIT + "|" + PopupConstants.SET_DIMESIONS,
            DCMUtils.DiagramPopupConstants.LAYOUT + "|" + DCMUtils.DiagramPopupConstants.LAYOUT_HIERARCHICAL,
            DCMUtils.DiagramPopupConstants.LAYOUT + "|" + DCMUtils.DiagramPopupConstants.LAYOUT_ORTHOGONAL,
            DCMUtils.DiagramPopupConstants.LAYOUT + "|" + DCMUtils.DiagramPopupConstants.LAYOUT_SYMMETRIC,
            DCMUtils.DiagramPopupConstants.LAYOUT + "|" + DCMUtils.DiagramPopupConstants.LAYOUT_INCREMENTAL,
            DCMUtils.DiagramPopupConstants.LAYOUT + "|" + DCMUtils.DiagramPopupConstants.LAYOUT_PROPERTIES,
            DCMUtils.DiagramPopupConstants.ZOOM,
            DCMUtils.DiagramPopupConstants.ZOOM_IN,
            DCMUtils.DiagramPopupConstants.ZOOM_OUT,
            DCMUtils.DiagramPopupConstants.SYNCHRONIZE,
            DCMUtils.DiagramPopupConstants.ASSOCIATE_WITH,
            DCMUtils.DiagramPopupConstants.PROPERTIES,
            DCMUtils.DiagramPopupConstants.SELECT_IN_MODEL,
            DCMUtils.DiagramPopupConstants.APPLY_DESIGN_PATTERN
        };
        
        String result = DCMUtils.checkUnnecessaryItems(diagram, itemsNecessary, getLog());
        if(!result.equals("")){
            fail("Diagram context menu contains unnecessary items (see log also): "+result);
        }
    }
    
//------------------------------------------------------------------------------
    
    public void setUp() throws FileNotFoundException{
        System.out.println("########  "+getName()+"  #######");
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 2000);
        JemmyProperties.setCurrentTimeout("Waiter.WaitingTime", 1000);
        JemmyProperties.setCurrentTimeout("JMenuOperator.WaitPopupTimeout", 3000);
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 3000);
        JemmyProperties.setCurrentTimeout("WindowWaiter.WaitWindowTimeout", 3000);
        
        OUT_LOG_FILE = workDir + File.separator + "jout_" + getName() + ".log";
        ERR_LOG_FILE = workDir + File.separator + "jerr_" + getName() + ".log";
        
        myOut = new PrintStream(new FileOutputStream(OUT_LOG_FILE), true);
        myErr = new PrintStream(new FileOutputStream(ERR_LOG_FILE), true);
        JemmyProperties.setCurrentOutput(new TestOut(System.in, myOut, myErr));

        diagram = DCMUtils.openDiagram(prName, cldName, NewDiagramWizardOperator.CLASS_DIAGRAM, workDir);
        if (diagram == null){
            fail("Can't open diagram '" + cldName + "', project '" + prName + "'.");
        }
    }
    
    public void tearDown() throws FileNotFoundException, IOException, InterruptedException{
        org.netbeans.test.umllib.util.Utils.makeScreenShot(lastTestCase);
        //org.netbeans.test.umllib.util.Utils.tearDown();
        diagram.getDrawingArea().pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
        try{
            JDialogOperator dlgError = new JDialogOperator("Unexpected Exception");
            JTextAreaOperator textarea = new JTextAreaOperator(dlgError);
            String str = textarea.getDisplayedText();
            int pos = str.indexOf("\n");
            if(pos != -1){str = str.substring(1, pos-1);}
            dlgError.close();
            fail(" " + str);
        }catch(TimeoutExpiredException e){}
        
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
    
    private DiagramOperator diagram = null;
}
       
