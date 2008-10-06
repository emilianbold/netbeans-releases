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
 * CLD_InterfaceTests.java
 *
 * Created on May 13, 2005, 10:41 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.test.uml.classdiagram;


import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;
import org.netbeans.junit.*;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.test.uml.classdiagram.utils.CLDUtils;
import org.netbeans.test.umllib.CompartmentOperator;
import org.netbeans.test.umllib.CompartmentTypes;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.NewDiagramWizardOperator;
import org.netbeans.test.umllib.customelements.ClassOperator;
import org.netbeans.test.umllib.exceptions.NotFoundException;
import org.netbeans.test.umllib.util.LabelsAndTitles;
import org.netbeans.test.umllib.vrf.DiagramElementVerifier;


/**
 *
 * @author lvv
 */
public class CLD_InterfaceElementAttributesOperationsTests extends ClassDiagramTestCase {
    
    private static String prName = "UMLProjectForInterfaceAO";
    private static String dpdName = "ClassDiagramForInterfaceAO";
    
    private static final String workDir = System.getProperty("nbjunit.workdir");
    private static String OUT_LOG_FILE = "";
    private static String ERR_LOG_FILE = "";
    private static PrintStream myOut = null;
    private static PrintStream myErr = null;
    private static BufferedReader myIn = null;
   
    EventTool eventTool = new EventTool();
    
    /** Need to be defined because of JUnit */
    public CLD_InterfaceElementAttributesOperationsTests(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(org.netbeans.test.uml.classdiagram.CLD_InterfaceElementAttributesOperationsTests.class);
        return suite;
    }

    /**
     * @Testcase_ID: Plan_CLD_InterfaceSymbol:Diagram:34 
     * @Testcase_ID: Plan_CLD_InterfaceSymbol:Diagram:35 
     * @Testcase_ID: Plan_CLD_InterfaceSymbol:Diagram:36 
     */

    // Huge TBD!!! to calls to attr/opCompartString
    String ATTR1_KY = "Attr1_KY";
    String ATTR1_KY_FQ = "public int Attr1_KY = 0";
    String ATTR2_KY = "Attr2_KY";
    String ATTR2_KY_FQ = "public int Attr2_KY = 0";
    String ATTR3_KY = "Attr3_KY";
    String ATTR3_KY_FQ = "public int Attr3_KY = 0";

    public void testInterfaceInsertAttributeKeys(){
	
	try {
	    String INT_NAME =  "IntInsAttrKeys";
	    //  Plan_CLD_InterfaceSymbol:Diagram:34
	    DiagramElementOperator tInt = createElement(diagram, ElementTypes.INTERFACE, INT_NAME);
	    CompartmentOperator attrs 
		= new CompartmentOperator(tInt, CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
	    diagram.pushKey(KeyEvent.VK_A, InputEvent.ALT_MASK | InputEvent.SHIFT_MASK);
	    new ClassOperator.classOperatorUtil().attributeNaturalWayNaming(null, null, ATTR1_KY);	    
	    deFocusEls(diagram);
	    verifyElementFQInInterfaceSymbol(tInt, ATTR1_KY_FQ);
	    verifyAttributeFQInTree(prName, INT_NAME, ATTR1_KY_FQ);
	    
	    // Plan_CLD_InterfaceSymbol:Diagram:35
	    CompartmentOperator attr1 = selectCompartment(tInt, ATTR1_KY_FQ, 0);
	    diagram.pushKey(KeyEvent.VK_A, InputEvent.ALT_MASK | InputEvent.SHIFT_MASK);
	    new ClassOperator.classOperatorUtil().attributeNaturalWayNaming(null, null, ATTR2_KY);	    
	    deFocusEls(diagram);
	    verifyElementFQInInterfaceSymbol(tInt, ATTR2_KY_FQ);
	    verifyAttributeFQInTree(prName, INT_NAME, ATTR2_KY_FQ);

	    // Plan_CLD_InterfaceSymbol:Diagram:36
	    attr1 = selectCompartment(tInt, ATTR1_KY_FQ, 0);
	    diagram.pushKey(KeyEvent.VK_INSERT);
	    new ClassOperator.classOperatorUtil().attributeNaturalWayNaming(null, null, ATTR3_KY);	    
	    deFocusEls(diagram);
	    verifyElementFQInInterfaceSymbol(tInt, ATTR3_KY_FQ);
	    verifyAttributeFQInTree(prName, INT_NAME, ATTR3_KY_FQ);

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Test failed with exception: "+e.getMessage());
        }finally{
            verifier.safeDeleteAllElements();
        }

    }


    /**
     * @Testcase_ID: Plan_CLD_InterfaceSymbol:Diagram:37 
     * @Testcase_ID: Plan_CLD_InterfaceSymbol:Diagram:38 
     */
    
    // Huge TBD!!! to calls to attr/opCompartString
    String ATTR1 = "Attr1";
    String ATTR1_FQ = "public int Attr1 = 0";
    String ATTR2 = "Attr2";
    String ATTR2_FQ = "public int Attr2 = 0";
    String ATTR_DEF_FQ = "public static int Unnamed = 0";

    public void testInterfaceInsertAttributeMenu(){
	
	try {
	    String INT_NAME =  "IntInsAttrMenu";
	    //  Plan_CLD_InterfaceSymbol:Diagram:37
	    DiagramElementOperator tInt = createElement(diagram, ElementTypes.INTERFACE, INT_NAME);
	    CompartmentOperator attrs = 
		new CompartmentOperator(tInt, CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT);
	    attrs.getPopup().pushMenu(LabelsAndTitles.POPUP_ADD_ATTRIBUTE);
	    new ClassOperator.classOperatorUtil().attributeNaturalWayNaming(null, null, ATTR1);	    
	    deFocusEls(diagram);
	    verifyElementFQInInterfaceSymbol(tInt, ATTR1_FQ);
	    verifyAttributeFQInTree(prName, INT_NAME, ATTR1_FQ);
	    
	    // Plan_CLD_InterfaceSymbol:Diagram:38
	    CompartmentOperator attr1 = selectCompartment(tInt, ATTR1_FQ, 0);
	    attr1.getPopup().pushMenu(LabelsAndTitles.POPUP_ADD_ATTRIBUTE);
	    new ClassOperator.classOperatorUtil().attributeNaturalWayNaming(null, null, ATTR2);	    
	    deFocusEls(diagram); 
	    verifyElementFQInInterfaceSymbol(tInt, ATTR2_FQ);
	    verifyAttributeFQInTree(prName, INT_NAME, ATTR2_FQ);

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Test failed with exception: "+e.getMessage());
        }finally{
            verifier.safeDeleteAllElements();
        }

    }



    /**
     * @Testcase_ID: Plan_CLD_InterfaceSymbol:Diagram:42
     * @Testcase_ID: Plan_CLD_InterfaceSymbol:Diagram:43 
     * @Testcase_ID: Plan_CLD_InterfaceSymbol:Diagram:44 
     */

    // Huge TBD!!! to calls to attr/opCompartString
    String OP1_KY = "Op1_KY";
    String OP1_KY_FQ = "public void  Op1_KY(  )";
    String OP2_KY = "Op2_KY";
    String OP2_KY_FQ = "public void  Op2_KY(  )";
    String OP3_KY = "Op3_KY";
    String OP3_KY_FQ = "public void  Op3_KY(  )";

    public void testInterfaceInsertOperationKeys(){
	
	try {
	    String INT_NAME =  "IntInsOpsKeys";
	    //  Plan_CLD_InterfaceSymbol:Diagram:42
	    DiagramElementOperator tInt = createElement(diagram, ElementTypes.INTERFACE, INT_NAME);
	    CompartmentOperator ops 
		= new CompartmentOperator(tInt, CompartmentTypes.OPERATION_LIST_COMPARTMENT);
	    diagram.pushKey(KeyEvent.VK_O, InputEvent.ALT_MASK | InputEvent.SHIFT_MASK);
	    new ClassOperator.classOperatorUtil().operationNaturalWayNaming(null, null, OP1_KY, (String[])null,(String[])null, true);	    
	    verifyElementFQInInterfaceSymbol(tInt, OP1_KY_FQ);
	    verifyOperationFQInTree(prName, INT_NAME, OP1_KY_FQ);
	    
	    // Plan_CLD_InterfaceSymbol:Diagram:43
	    CompartmentOperator op1 = selectCompartment(tInt, OP1_KY_FQ, 0);
	    diagram.pushKey(KeyEvent.VK_O, InputEvent.ALT_MASK | InputEvent.SHIFT_MASK);
	    new ClassOperator.classOperatorUtil().operationNaturalWayNaming(null, null, OP2_KY, (String[])null,(String[])null, true);	    
	    verifyElementFQInInterfaceSymbol(tInt, OP2_KY_FQ);
	    verifyOperationFQInTree(prName, INT_NAME, OP2_KY_FQ);

	    // Plan_CLD_InterfaceSymbol:Diagram:44
	    op1 = selectCompartment(tInt, OP1_KY_FQ, 0);
	    diagram.pushKey(KeyEvent.VK_INSERT);
	    new ClassOperator.classOperatorUtil().operationNaturalWayNaming(null, null, OP3_KY, (String[])null,(String[])null, true);	    
	    // workaround for delete all when there is only one interface with selected op
	    selectCompartment(tInt, INT_NAME, 0);
	    verifyElementFQInInterfaceSymbol(tInt, OP3_KY_FQ);
	    verifyOperationFQInTree(prName, INT_NAME, OP3_KY_FQ);

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Test failed with exception: "+e.getMessage());
        }finally{
            verifier.safeDeleteAllElements();
        }

    }



    /**
     * @Testcase_ID: Plan_CLD_InterfaceSymbol:Diagram:45
     * @Testcase_ID: Plan_CLD_InterfaceSymbol:Diagram:46 
     */

    // Huge TBD!!! to calls to attr/opCompartString
    String OP1 = "Op1";
    String OP1_FQ = opCompartString("public", "void", OP1);
    String OP2 = "Op2";
    String OP2_FQ = opCompartString("public", "void", OP2);

    public void testInterfaceInsertOperationMenu(){
	
	try {
	    String INT_NAME =  "IntInsOpsMenu";
	    //  Plan_CLD_InterfaceSymbol:Diagram:42
	    DiagramElementOperator tInt = createElement(diagram, ElementTypes.INTERFACE, INT_NAME);
	    CompartmentOperator ops 
		= new CompartmentOperator(tInt, CompartmentTypes.OPERATION_LIST_COMPARTMENT);
	    pushMenuInPopup(ops.getPopup(), LabelsAndTitles.POPUP_ADD_OPERATION);
	    new ClassOperator.classOperatorUtil().operationNaturalWayNaming(null, null, OP1, (String[])null,(String[])null, true);	    
	    verifyElementFQInInterfaceSymbol(tInt, OP1_FQ);
	    verifyOperationFQInTree(prName, INT_NAME, OP1_FQ);
	    
	    // Plan_CLD_InterfaceSymbol:Diagram:43
	    CompartmentOperator op1 = selectCompartment(tInt, OP1_FQ, 0);
	    pushMenuInPopup(op1.getPopup(), LabelsAndTitles.POPUP_ADD_OPERATION);
	    new ClassOperator.classOperatorUtil().operationNaturalWayNaming(null, null, OP2, (String[])null,(String[])null, true);	    
	    // workaround for delete all when there is only one interface with selected op
	    selectCompartment(tInt, INT_NAME, 0);
	    verifyElementFQInInterfaceSymbol(tInt, OP2_FQ);
	    verifyOperationFQInTree(prName, INT_NAME, OP2_FQ);


	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Test failed with exception: "+e.getMessage());
        }finally{
            verifier.safeDeleteAllElements();
        }

    }



//various service calls, to be librarized

    public static DiagramElementOperator createElement(DiagramOperator diagram, 
						       ElementTypes elementType, 
						       String name) 
	throws NotFoundException
    {
        Point p = diagram.getDrawingArea().getFreePoint();
	DiagramElementOperator el =  diagram.putElementOnDiagram(name, elementType, p.x, p.y);
	new EventTool().waitNoEvent(500);
        return el;
    }
    

    public static CompartmentOperator selectCompartment(DiagramElementOperator interf, 
						      String elementFQ, 
						      int index) 
	throws NotFoundException
    {
	CompartmentOperator attr = getCompartment(interf, elementFQ, 0);
	attr.clickOnCenter();	    
	new EventTool().waitNoEvent(500);
	return attr;
    }

    public static CompartmentOperator getCompartment(DiagramElementOperator interf, 
						     String attrFQ, 
						     int index) 
	throws NotFoundException
    {	
	CompartmentOperator attr 
	    = new CompartmentOperator(interf, 
				      CompartmentOperator
				      .waitForCompartment(interf, 
							  new CompartmentOperator
							      .CompartmentByNameChooser(attrFQ),
							  index));
	return attr;
    }


    public static boolean verifyElementFQInInterfaceSymbol(DiagramElementOperator interf,
							   String attrFQ) 
	throws NotFoundException
    {
	CompartmentOperator attr = getCompartment(interf, attrFQ, 0);
        assertTrue("Element wasn't found in element [attrFQ=\"" + attrFQ + "\"]",
		   attr != null);
	return attr != null;
    }


    public static Node verifyAttributeFQInTree(String umlProjectName,
					    String classNameFQ,
					    String attrFQ) 
    {
	Node attr = getElementFQInTree(umlProjectName, classNameFQ, "Attributes", attrFQ);
        assertTrue("The specified attribute wasn't found " 
		   + "[attrFQ=\"" + attrFQ + "\" class=" + classNameFQ + " project=" + umlProjectName + "]", 
		   attr != null);
	return attr;
    }


    public static Node verifyOperationFQInTree(String umlProjectName,
					       String classNameFQ,
					       String opFQ) 
    {
	Node op = getElementFQInTree(umlProjectName, classNameFQ, "Operations", opFQ);
        assertTrue("The specified operation wasn't found " 
		   + "[opFQ=\"" + opFQ + "\" class=" + classNameFQ + " project=" + umlProjectName + "]", 
		   op != null);
	return op;
    }


    public static Node getElementFQInTree(String umlProjectName,
						String classNameFQ,
						String subnode,
						String attrFQ) 
    {
	String classTreePath = fqClassNameToTreePath(classNameFQ);
	String ctp = umlProjectName + "|Model|" + classTreePath;
	Node cnode = getPath(ctp);
	if (cnode.isChildPresent(subnode)) {
	    return getPath(ctp + "|" + subnode + "|" + attrFQ);
	} else {
	    return getPath(ctp + "|" + attrFQ);
	}
    }
    
	
    public static String fqClassNameToTreePath(String classNameFQ) {
	StringTokenizer st = new StringTokenizer(classNameFQ, ".");
	String res = null;
	if (st.hasMoreTokens()) {
	    res = st.nextToken(); 
	    while(st.hasMoreTokens()) {
		res += "|" + st.nextToken();
	    }
	}
	return res;
    }



    public static Node getPath(String path) {
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        JTreeOperator prTree = new JTreeOperator(pto);
        TreePath elementPath = prTree.findPath(path);
	Node n = null;
	if (elementPath != null) {
	    n = new Node(prTree, elementPath);
	}
        return n;    
        
    }


    /* 
     * A bunch of methods will be here	
     *
     * For now Java notation only, standard UML to be done later
     */
    public static String attrCompartString(String visibility,
					   String type,
					   String name) 
    {
	// TBD!!! 
	return visibility + " " + type + " " +name;
	
    }


    public static String opCompartString(String visibility,
					 String retType,
					 String name) 
    {
	return opCompartString(visibility, retType, name, null, null);
    }


    public static String opCompartString(String visibility,
					 String retType,
					 String name,
					 String parTypes[],
					 String parNames[]) 
    {
	// TBD!!!
	String res = "";
	if (visibility != null && !visibility.equals("")) {
	    res += visibility + " ";
	}
	if (retType != null && !retType.equals("")) {
	    res += retType;
	}
	if (name != null && !name.equals("")) {
	    res += "  " + name + "(";
	}
	if (parTypes == null || parTypes.length == 0) {
	    res += "  )";
	} else {
	    // TBD!!!
	    ;
	}	
	return res;	
    }


    public static void deFocusEls(DiagramOperator diagram) {
	Point a = diagram.getDrawingArea().getFreePoint();
	System.out.println("The deFocus point is "+a);
        diagram.clickMouse(a.x, a.y, 1);
        new EventTool().waitNoEvent(500);
    }


    /*
     * Default implementation picks up child popup if already opened
     * (and it does open automatically just because the mouse was there)
     */
    public static void pushMenuInPopup(JPopupMenuOperator jpo, String menuPath) {
	Container ppm = jpo.getSource().getParent();
	while(ppm.getParent() != null ) {
	    ppm = ppm.getParent();
	}
	StringTokenizer st = new StringTokenizer(menuPath, "|");	
	final String nt = st.nextToken();
	
	JPopupMenuOperator pp = 
	    new JPopupMenuOperator(JPopupMenuOperator.waitJPopupMenu(ppm, new ComponentChooser() {
		    public boolean checkComponent(Component comp) {
			if(comp instanceof JPopupMenu) {
			    ComponentSearcher searcher = new ComponentSearcher((Container)comp);
			    return (searcher.findComponent(new JMenuItemOperator.
				JMenuItemByLabelFinder(nt)) != null);
			} else {
			    return(false);
			}
		    }
		    public String getDescription() {
			return("Popup containing \"" + nt + "\" menu item");
		    }		
		}));
				   
	//JMenuItemOperator mio = new JMenuItemOperator(pp, nt, 0);
	//mio.enterMouse();
	pp.pushMenu(menuPath);
    }




//------------------------------------------------------------------------------
    
    protected void setUp() throws FileNotFoundException{
        System.out.println("########  "+getName()+"  #######");
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 5000);
        JemmyProperties.setCurrentTimeout("Waiter.WaitingTime", 3000);
        JemmyProperties.setCurrentTimeout("DiagramElementOperator.WaitDiagramElementTime", 5000);
        
        OUT_LOG_FILE = workDir + File.separator + "jout_" + getName() + ".log";
        ERR_LOG_FILE = workDir + File.separator + "jerr_" + getName() + ".log";
        
        myOut = new PrintStream(new FileOutputStream(OUT_LOG_FILE), true);
        myErr = new PrintStream(new FileOutputStream(ERR_LOG_FILE), true);
        JemmyProperties.setCurrentOutput(new TestOut(System.in, myOut, myErr));
        
        diagram = CLDUtils.openDiagram(prName, dpdName, NewDiagramWizardOperator.CLASS_DIAGRAM, workDir);
        if (diagram == null){
            fail("Can't open diagram '" + dpdName + "', project '" + prName + "'.");
        }
        verifier =  new DiagramElementVerifier(diagram, ElementTypes.INTERFACE, "CLD_", getLog());
    }
    
    private DiagramElementVerifier verifier = null;
}
