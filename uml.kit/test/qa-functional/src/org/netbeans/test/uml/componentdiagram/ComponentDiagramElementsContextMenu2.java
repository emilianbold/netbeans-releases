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


package org.netbeans.test.uml.componentdiagram;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.DrawingAreaOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.ExpandedElementTypes;
import org.netbeans.test.umllib.LinkOperator;
import org.netbeans.test.umllib.LinkTypes;
import org.netbeans.test.umllib.NewDiagramWizardOperator;
import org.netbeans.test.umllib.UMLPaletteOperator;
import org.netbeans.test.umllib.exceptions.NotFoundException;
import org.netbeans.test.umllib.testcases.UMLTestCase;
import org.netbeans.test.umllib.util.LabelsAndTitles;
import org.netbeans.test.umllib.util.LibProperties;



/**
 *
 * @author psb
 * @spec UML/ComponentDiagram.xml
 */
public class ComponentDiagramElementsContextMenu2 extends UMLTestCase {

    //some system properties
    private static String contextPropItemName = "Properties";
    private static String umlPropertyWindowTitle = "Project Properties";
    private static String umlSourcePackagesLabel = "Source Packages";
    private static String umlSourcePackagesColumn = "Folder Label";
    private static String umlSourceUsageColumn = "Model?";
    private static String mainTreeTabName = "Projects";
    //common test properties
    private static String prName = "ComponentDiagramProjectECM2";
    private static String project = prName + "|Model";
    private static String sourceProject = "source";
    private static boolean codeSync = false;
    private static String defaultNewElementName = org.netbeans.test.uml.componentdiagram.utils.Utils.defaultNewElementName;
    private static String defaultReturnType = org.netbeans.test.uml.componentdiagram.utils.Utils.defaultReturnType;
    private static String defaultAttributeType = org.netbeans.test.uml.componentdiagram.utils.Utils.defaultAttributeType;
    private static String defaultAttributeVisibility = org.netbeans.test.uml.componentdiagram.utils.Utils.defaultAttributeVisibility;
    private static String defaultOperationVisibility = org.netbeans.test.uml.componentdiagram.utils.Utils.defaultOperationVisibility;
    private ProjectsTabOperator pto = null;
    private Node lastDiagramNode = null;
    private String lastTestCase = null;
    private static String workdir = System.getProperty("nbjunit.workdir");
    //
    private static String activityDiagramName0 = "cpD";
    private static String workPkg0 = "pkg";
    private static long counter = 0;
    //--
    private static ExpandedElementTypes elementType1 = ExpandedElementTypes.COMPONENT;
    private static String[] menuItems1 = {"Edit|Lock Edit", "Resize Element to Contents", "Reset Edges", "Hide|Parents|One Level", "Hide|Parents|All Levels", "Hide|Children|One Level", "Hide|Children|All Levels", "Show|Parents|One Level", "Show|Parents|All Levels", "Show|Children|One Level", "Show|Children|All Levels", "Generate Dependency Diagram", elementType1.toString() + "|Font", elementType1.toString() + "|Font Color", elementType1.toString() + "|Background Color", elementType1.toString() + "|Border Color"};
    //--
    private static LinkTypes elementType17 = LinkTypes.NESTED_LINK.NESTED_LINK(LibProperties.getCurrentToolName(ExpandedElementTypes.CLASS));
    private static ExpandedElementTypes[] elements17 = {ExpandedElementTypes.CLASS, ExpandedElementTypes.CLASS};
    private static String[] menuItems17 = {"Redefine Operations", "Find|Source Element", "Find|Target Element", elementType17.toString() + "|Border Color"};
    //--
    private static LinkTypes elementType18 = LinkTypes.DELEGATE;
    private static ExpandedElementTypes[] elements18 = {ExpandedElementTypes.CLASS, ExpandedElementTypes.CLASS};
    private static String[] menuItems18 = {"Labels|Reset Labels", "Find|Source Element", "Find|Target Element", elementType18.toString() + "|Border Color"};
    //--
    private static LinkTypes elementType19 = LinkTypes.ASSEMBLY;
    private static ExpandedElementTypes[] elements19 = {ExpandedElementTypes.COMPONENT, ExpandedElementTypes.INTERFACE};
    private static String[] menuItems19 = {"Labels|Reset Labels", "Find|Source Element", "Find|Target Element", LinkTypes.USAGE.toString() + "|Border Color"};
    //--
    private static LinkTypes elementType20 = LinkTypes.DEPENDENCY;
    private static ExpandedElementTypes[] elements20 = {ExpandedElementTypes.CLASS, ExpandedElementTypes.CLASS};
    private static String[] menuItems20 = {"Labels|Name", "Labels|Reset Labels", "Find|Source Element", "Find|Target Element", elementType20.toString() + "|Border Color"};
    //
    private static LinkTypes elementType21 = LinkTypes.COMMENT;
    private static String[] menuItems21 = {"Find|Source Element", "Find|Target Element", elementType21.toString() + "|Border Color"};
    //--
    private static LinkTypes elementType22 = LinkTypes.REALIZE;
    private static ExpandedElementTypes[] elements22 = {ExpandedElementTypes.CLASS, ExpandedElementTypes.CLASS};
    private static String[] menuItems22 = {"Labels|Name", "Labels|Reset Labels", "Labels|Stereotype", "Find|Source Element", "Find|Target Element", elementType22.toString() + "|Border Color"};
    //--
    private static LinkTypes elementType23 = LinkTypes.USAGE;
    private static ExpandedElementTypes[] elements23 = {ExpandedElementTypes.CLASS, ExpandedElementTypes.CLASS};
    private static String[] menuItems23 = {"Labels|Name", "Labels|Reset Labels", "Labels|Stereotype", "Find|Source Element", "Find|Target Element", elementType23.toString() + "|Border Color"};
    //--
    private static LinkTypes elementType24 = LinkTypes.PERMISSION;
    private static ExpandedElementTypes[] elements24 = {ExpandedElementTypes.CLASS, ExpandedElementTypes.CLASS};
    private static String[] menuItems24 = {"Labels|Name", "Labels|Reset Labels", "Labels|Stereotype", "Find|Source Element", "Find|Target Element", elementType24.toString() + "|Border Color"};
    //--
    private static LinkTypes elementType25 = LinkTypes.ABSTRACTION;
    private static ExpandedElementTypes[] elements25 = {ExpandedElementTypes.CLASS, ExpandedElementTypes.CLASS};
    private static String[] menuItems25 = {"Labels|Name", "Labels|Reset Labels", "Labels|Stereotype", "Find|Source Element", "Find|Target Element", elementType25.toString() + "|Border Color"};
    //--
    private static LinkTypes elementType26 = LinkTypes.DERIVATION_EDGE;
    private static ExpandedElementTypes[] elements26 = {ExpandedElementTypes.DERIVATION_CLASSIFIER, ExpandedElementTypes.TEMPLATE_CLASS};
    private static String[] menuItems26 = {"Labels|Reset Labels", "Labels|Binding", "Find|Source Element", "Find|Target Element", elementType26.toString() + "|Border Color"};
    //--
    private static LinkTypes elementType27 = LinkTypes.ASSOCIATION;
    private static ExpandedElementTypes[] elements27 = {ExpandedElementTypes.CLASS, ExpandedElementTypes.CLASS};
    private static String[] menuItems27 = {"Labels|Link Name", "Labels|Reset Labels", "Labels|Both End Names", "Labels|Both End Multiplicities", "Find|Source Element", "Find|Target Element", elementType27.toString() + "|Border Color", "Transform|To Ordinary Aggregate", "Transform|To Composite Aggregate", "Transform|Remove Aggregate", "Transform|Navigable", "Transform|Reverse Ends"};
    //--
    private static LinkTypes elementType28 = LinkTypes.AGGREGATION;
    private static ExpandedElementTypes[] elements28 = {ExpandedElementTypes.CLASS, ExpandedElementTypes.CLASS};
    private static String[] menuItems28 = {"Labels|Link Name", "Labels|Reset Labels", "Labels|Both End Names", "Labels|Both End Multiplicities", "Find|Source Element", "Find|Target Element", elementType28.toString() + "|Border Color", "Transform|To Ordinary Aggregate", "Transform|To Composite Aggregate", "Transform|Remove Aggregate", "Transform|Navigable", "Transform|Reverse Ends"};
    //--
    private static LinkTypes elementType29 = LinkTypes.COMPOSITION;
    private static ExpandedElementTypes[] elements29 = {ExpandedElementTypes.CLASS, ExpandedElementTypes.CLASS};
    private static String[] menuItems29 = {"Labels|Link Name", "Labels|Reset Labels", "Labels|Both End Names", "Labels|Both End Multiplicities", "Find|Source Element", "Find|Target Element", elementType29.toString() + "|Border Color", "Transform|To Ordinary Aggregate", "Transform|To Composite Aggregate", "Transform|Remove Aggregate", "Transform|Navigable", "Transform|Reverse Ends"};
    //--
    private static LinkTypes elementType30 = LinkTypes.NAVIGABLE_ASSOCIATION;
    private static ExpandedElementTypes[] elements30 = {ExpandedElementTypes.CLASS, ExpandedElementTypes.CLASS};
    private static String[] menuItems30 = {"Labels|Link Name", "Labels|Reset Labels", "Labels|Both End Names", "Labels|Both End Multiplicities", "Find|Source Element", "Find|Target Element", elementType30.toString() + "|Border Color", "Transform|To Ordinary Aggregate", "Transform|To Composite Aggregate", "Transform|Remove Aggregate", "Transform|Navigable", "Transform|Reverse Ends"};
    //--
    private static LinkTypes elementType31 = LinkTypes.NAVIGABLE_AGGREGATION;
    private static ExpandedElementTypes[] elements31 = {ExpandedElementTypes.CLASS, ExpandedElementTypes.CLASS};
    private static String[] menuItems31 = {"Labels|Link Name", "Labels|Reset Labels", "Labels|Both End Names", "Labels|Both End Multiplicities", "Find|Source Element", "Find|Target Element", elementType31.toString() + "|Border Color", "Transform|To Ordinary Aggregate", "Transform|To Composite Aggregate", "Transform|Remove Aggregate", "Transform|Navigable", "Transform|Reverse Ends"};
    //--
    private static LinkTypes elementType32 = LinkTypes.NAVIGABLE_COMPOSITION;
    private static ExpandedElementTypes[] elements32 = {ExpandedElementTypes.CLASS, ExpandedElementTypes.CLASS};
    private static String[] menuItems32 = {"Labels|Link Name", "Labels|Reset Labels", "Labels|Both End Names", "Labels|Both End Multiplicities", "Find|Source Element", "Find|Target Element", elementType32.toString() + "|Border Color", "Transform|To Ordinary Aggregate", "Transform|To Composite Aggregate", "Transform|Remove Aggregate", "Transform|Navigable", "Transform|Reverse Ends"};
    //--
    private static LinkTypes elementType33 = LinkTypes.ROLE_BINDING;
    private static ExpandedElementTypes[] elements33 = {ExpandedElementTypes.CLASS_ROLE, ExpandedElementTypes.DESIGN_PATTERN};
    private static String[] menuItems33 = {"Find|Source Element", "Find|Target Element", "Class|Border Color"};
    //common
    private static String[] commonMenuItems = {"Edit|Copy", "Edit|Cut", "Edit|Delete", "Edit|Paste", "Edit|Select All", "Edit|Invert Selection", "Edit|Select All Similar Elements", "Synchronize Element with Data", "Select in Model", "Properties", "Associate With...", "Apply Design Pattern..."};
    //
    JTreeOperator prTree;

    /** Need to be defined because of JUnit */
    public ComponentDiagramElementsContextMenu2(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(org.netbeans.test.uml.componentdiagram.ComponentDiagramElementsContextMenu2.class); 
        return suite;
    }

    private DiagramOperator createDiagram(String project, String workPkg, String diagram) {
        //
        org.netbeans.test.umllib.Utils.RetAll rt = org.netbeans.test.umllib.Utils.createDiagram(project, workPkg, diagram, NewDiagramWizardOperator.COMPONENT_DIAGRAM);
        pto = rt.pto;
        prTree = new JTreeOperator(pto);
        lastDiagramNode = rt.lastDiagramNode;
        return rt.dOp;
    }

    public void testCreate17() {
        testLinkContext(elementType17, elements17, menuItems17);
    }

    public void testCreate18() {
        testLinkContext(elementType18, elements18, menuItems18);
    }

    public void testCreate19() {
        testLinkContext(elementType19, elements19, menuItems19);
    }

    public void testCreate20() {
        testLinkContext(elementType20, elements20, menuItems20);
    }

    public void testCreate22() {
        testLinkContext(elementType22, elements22, menuItems22);
    }

    public void testCreate23() {
        testLinkContext(elementType23, elements23, menuItems23);
    }

    public void testCreate24() {
        testLinkContext(elementType24, elements24, menuItems24);
    }

    public void testCreate25() {
        testLinkContext(elementType25, elements25, menuItems25);
    }

    public void testCreate26() {
        testLinkContext(elementType26, elements26, menuItems26);
    }

    public void testCreate27() {
        testLinkContext(elementType27, elements27, menuItems27);
    }

    public void testCreate28() {
        testLinkContext(elementType28, elements28, menuItems28);
    }

    public void testCreate29() {
        testLinkContext(elementType29, elements29, menuItems29);
    }

    public void testCreate30() {
        testLinkContext(elementType30, elements30, menuItems30);
    }

    public void testCreate31() {
        testLinkContext(elementType31, elements31, menuItems31);
    }

    public void testCreate32() {
        testLinkContext(elementType32, elements32, menuItems32);
    }

    public void testCreate33() {
        testLinkContext(elementType33, elements33, menuItems33);
    }

    public void testLinkComment() {
        lastTestCase = getCurrentTestMethodName();
        String[] customMenuItems = menuItems21;
        String element = LibProperties.getProperties().getCurrentToolName(elementType21);
        LinkTypes elementType = elementType21;
        //
        String workPkg = workPkg0 + counter;
        String diagramName = activityDiagramName0 + counter;
        counter++;
        String localElName1 = "El1";
        //
        DiagramOperator d = createDiagram(project, workPkg, diagramName);
        //
        UMLPaletteOperator pl = new UMLPaletteOperator();
        DrawingAreaOperator drAr = d.getDrawingArea();
        //
        java.awt.Point a = drAr.getFreePoint();
        DiagramElementOperator dE1 = null;
        DiagramElementOperator dE2 = null;
        dE1 = d.putElementOnDiagram(localElName1, elementType1, a.x, a.y);
        //
        pl.selectTool(element);
        //
        drAr.clickMouse(dE1.getCenterPoint().x, dE1.getCenterPoint().y, 1);
        a = drAr.getFreePoint(150);
        drAr.clickMouse(a.x, a.y, 1);
        dE2 = new DiagramElementOperator(d, "", ElementTypes.LINK_COMMENT);
        //
        drAr.pushKey(KeyEvent.VK_ESCAPE);
        pl.waitSelection(element, false);
        new EventTool().waitNoEvent(500);
        //
        try {
            Thread.sleep(500);
        } catch (Exception ex) {
        }
        a = drAr.getFreePoint(100);
        drAr.clickMouse(a.x, a.y, 1, InputEvent.BUTTON3_MASK);
        drAr.pushKey(KeyEvent.VK_ESCAPE);
        //
        LinkOperator testedlink = LinkOperator.findLink(dE1, dE2, new LinkOperator.LinkByTypeChooser(elementType), 0);
        if (testedlink == null) {
            LinkOperator altLink = null;
            try {
                altLink = LinkOperator.findLink(dE1, dE2, new LinkOperator.LinkByTypeChooser(LinkTypes.ANY), 0);
            } catch (Exception ex) {
                fail("any link find failed.");
            }
            if (altLink != null) {
                fail("Can't find " + elementType + " link between elements, but the is " + altLink.getType() + " link.");
            }
        }
        //
        assertTrue("Can't find " + elementType + " link between elements", testedlink != null);
        drAr.clickMouse(testedlink.getNearCenterPoint().x, testedlink.getNearCenterPoint().y, 1, InputEvent.BUTTON3_MASK);
        verify(customMenuItems);
    }

    public void setUp() {
        System.out.println("########  " + getName() + "  #######");
        pto = ProjectsTabOperator.invoke();
        if (!codeSync) {
            org.netbeans.test.uml.componentdiagram.utils.Utils.commonComponentDiagramSetup(workdir, prName);
            //
            codeSync = true;
        }
    }

    public void tearDown() {
        org.netbeans.test.umllib.util.Utils.makeScreenShot(lastTestCase);
        //popup protection
        MainWindowOperator.getDefault().pushKey(KeyEvent.VK_ESCAPE);
        new EventTool().waitNoEvent(1000);
        //
        closeAllModal();
        org.netbeans.test.umllib.util.Utils.saveAll();
        if (lastDiagramNode != null) {
            lastDiagramNode.collapse();
            new Node(lastDiagramNode.tree(), lastDiagramNode.getParentPath()).collapse();
        }
        try {
            DiagramOperator d = new DiagramOperator("cpD");
            d.closeAllDocuments();
            d.waitClosed();
            new EventTool().waitNoEvent(1000);
        } catch (Exception ex) {
        }
        ;
        closeAllModal();
        //save
        org.netbeans.test.umllib.util.Utils.tearDown();
    }

    private void testElementContext(ExpandedElementTypes elementType, String[] customMenuItems) {
        lastTestCase = getCurrentTestNamesWithCheck()[1];
        String elementName = LibProperties.getCurrentDefaultName(elementType);
        String element = LibProperties.getProperties().getCurrentToolName(elementType);
        //
        String workPkg = workPkg0 + counter;
        String diagramName = activityDiagramName0 + counter;
        counter++;
        //
        DiagramOperator d = createDiagram(project, workPkg, diagramName);
        //
        int numChild = lastDiagramNode.getChildren().length;
        //
        UMLPaletteOperator pl = new UMLPaletteOperator();
        pl.waitComponentShowing(true);
        pl.waitComponentVisible(true);
        //
        try {
            pl.selectTool(element);
        } catch (NotFoundException ex) {
            fail("BLOCKING: Can't find '" + element + "' in paletter");
        }
        //
        DrawingAreaOperator drAr = d.getDrawingArea();
        java.awt.Point a = drAr.getFreePoint();
        drAr.clickMouse(a.x, a.y, 1);
        drAr.pushKey(KeyEvent.VK_ESCAPE);
        pl.waitSelection(element, false);
        new EventTool().waitNoEvent(500);
        //
        try {
            Thread.sleep(500);
        } catch (Exception ex) {
        }
        a = drAr.getFreePoint(100);
        drAr.clickMouse(a.x, a.y, 1, InputEvent.BUTTON3_MASK);
        drAr.pushKey(KeyEvent.VK_ESCAPE);
        //
        DiagramElementOperator dEl = null;
        try {
            dEl = new DiagramElementOperator(d, elementName, elementType, 0);
        } catch (Exception ex) {
            try {
                fail(element + " wasn't added to diagram, but object with type:" + new DiagramElementOperator(d, elementName).getType() + ": and element type :" + new DiagramElementOperator(d, elementName).getElementType() + ": was added whyle type should be :" + elementType + ": was added");
            } catch (Exception ex2) {
            }
            fail(element + " wasn't added to diagram.");
        }
        dEl.select();
        //call popup
        drAr.clickMouse(dEl.getCenterPoint().x, dEl.getCenterPoint().y, 1, InputEvent.BUTTON3_MASK);
        verify(customMenuItems);
    }

    private void testLinkContext(LinkTypes elementType, ExpandedElementTypes[] elements, String[] customMenuItems) {
        lastTestCase = getCurrentTestNamesWithCheck()[1];
        String elementName = LibProperties.getCurrentDefaultName(elementType);
        String element = LibProperties.getCurrentToolName(elementType);
        //
        String workPkg = workPkg0 + counter;
        String diagramName = activityDiagramName0 + counter;
        counter++;
        String localElName1 = "El1";
        String localElName2 = "El2";
        //
        DiagramOperator d = createDiagram(project, workPkg, diagramName);
        //
        UMLPaletteOperator pl = new UMLPaletteOperator();
        DrawingAreaOperator drAr = d.getDrawingArea();
        //
        java.awt.Point a = drAr.getFreePoint();
        DiagramElementOperator dE1 = null;
        DiagramElementOperator dE2 = null;
        dE1 = d.putElementOnDiagram(localElName1, elements[0], a.x, a.y);
        a = drAr.getFreePoint(150);
        dE2 = d.putElementOnDiagram(localElName2, elements[1], a.x, a.y);
        //
        try {
            pl.selectTool(element);
        } catch (NotFoundException ex) {
            fail("BLOCKING: Can't find '" + element + "' in paletter");
        }
        //
        drAr.clickMouse(dE1.getCenterPoint().x, dE1.getCenterPoint().y, 1);
        drAr.clickMouse(dE2.getCenterPoint().x, dE2.getCenterPoint().y, 1);
        //
        drAr.pushKey(KeyEvent.VK_ESCAPE);
        pl.waitSelection(element, false);
        new EventTool().waitNoEvent(500);
        //
        try {
            Thread.sleep(500);
        } catch (Exception ex) {
        }
        a = drAr.getFreePoint(100);
        drAr.clickMouse(a.x, a.y, 1, InputEvent.BUTTON3_MASK);
        drAr.pushKey(KeyEvent.VK_ESCAPE);
        //"workaround for assembly connector"
        if (elementType.equals(LinkTypes.ASSEMBLY)) {
            dE1 = new DiagramElementOperator(d, "", ExpandedElementTypes.PORT, 0);
            elementType = LinkTypes.USAGE;
        }
        //
        LinkOperator testedlink = null;
        try {
            /* In netbeans6.0 composition and navigable composition link are shown as aggregation in 
             * project view as they are special form of aggregation. See Issue 116868
             * Similar to navigable aggregation and navigable association.The default name
             * aggrgation and association are used in project modle.
             * As workaround for test, here set type to their default type.
            */
            LinkTypes elementTypeInProjectTree=elementType;
            if ((elementType==LinkTypes.COMPOSITION) ||(elementType==LinkTypes.NAVIGABLE_COMPOSITION))
                 elementTypeInProjectTree=LinkTypes.AGGREGATION;
            if (elementType==LinkTypes.NAVIGABLE_AGGREGATION)
                elementTypeInProjectTree=LinkTypes.AGGREGATION;
            if (elementType==LinkTypes.NAVIGABLE_ASSOCIATION)
                elementTypeInProjectTree=LinkTypes.ASSOCIATION;            
            testedlink = LinkOperator.findLink(dE1, dE2, new LinkOperator.LinkByTypeChooser(elementTypeInProjectTree), 0);
        } catch (Exception ex) {
            fail(element + " of type " + elementType + " wasn't added to diagram.");
        }
        if (testedlink == null) {
            LinkOperator altLink = null;
            try {
                altLink = LinkOperator.findLink(dE1, dE2, new LinkOperator.LinkByTypeChooser(LinkTypes.ANY), 0);
            } catch (Exception ex) {
                fail("any link find failed.");
            }            
            if (altLink != null) {
                fail("Can't find " + elementType + " link between elements, but the is " + altLink.getType() + " link.");
            }
        }
        //
        if (testedlink == null && elementType.equals(LinkTypes.ACTIVITY_EDGE)) {
            //fail("Can't find Activity Edge/MultiFlow link between elemens, may be test library limitation, please recheck manually.");
        } else {
            assertTrue("Can't find " + elementType + " link between elements", testedlink != null);
        }
        //
        if (elementType.equals(LinkTypes.ACTIVITY_EDGE)) {
            //tried to get popup for activity edge
            drAr.clickMouse((dE1.getCenterPoint().x + dE2.getCenterPoint().x) / 2, (dE1.getCenterPoint().y + dE2.getCenterPoint().y) / 2, 1, InputEvent.BUTTON3_MASK);
        } else {
            drAr.clickMouse(testedlink.getNearCenterPoint().x, testedlink.getNearCenterPoint().y, 1, InputEvent.BUTTON3_MASK);
        }
        verify(customMenuItems);
    }

    private void verify(String[] add) {
        JPopupMenuOperator pop = new JPopupMenuOperator();
        pop.waitComponentShowing(true);
        try {
            Thread.sleep(500);
        } catch (Exception ex) {
        }
        //workaround for 78301
        pop.pushKey(KeyEvent.VK_LEFT);
        //
        try {
            Thread.sleep(500);
        } catch (Exception ex) {
        }
        //
        pop = new JPopupMenuOperator();
        //
        String fails = "";
        for (int i = 0; i < commonMenuItems.length; i++) {
            JMenuItemOperator it = null;
            try {
                it = pop.showMenuItem(commonMenuItems[i]);
                if (it == null) {
                    fails += "Null item " + commonMenuItems[i] + ";\n";
                    org.netbeans.test.umllib.util.Utils.makeScreenShot();
                }
            } catch (Exception ex) {
                fails += "Timeout on selection of " + commonMenuItems[i] + ";\n";
                org.netbeans.test.umllib.util.Utils.makeScreenShot();
            }
            //returns back from inner popup
            if (commonMenuItems[i].indexOf("|") > -1) {
                new EventTool().waitNoEvent(500);
                pop.pushKey(KeyEvent.VK_LEFT);
                new EventTool().waitNoEvent(500);
                pop.pushKey(KeyEvent.VK_LEFT);
            }
        }
        if (add != null) {
            for (int i = 0; i < add.length; i++) {
                JMenuItemOperator it = null;
                try {
                    it = pop.showMenuItem(add[i]);
                    if (it == null) {
                        fails += "Null item " + add[i] + ";\n";
                        org.netbeans.test.umllib.util.Utils.makeScreenShot(lastTestCase);
                    }
                } catch (Exception ex) {
                    fails += "Timeout on selection of " + add[i] + ";\n";
                    org.netbeans.test.umllib.util.Utils.makeScreenShot(lastTestCase);
                }
                //returns back from inner popup
                if (add[i].indexOf("|") > -1) {
                    new EventTool().waitNoEvent(500);
                    pop.pushKey(KeyEvent.VK_LEFT);
                    new EventTool().waitNoEvent(500);
                    pop.pushKey(KeyEvent.VK_LEFT);
                }
            }
        }
        //
        assertTrue("There are some problems with context menu: " + fails, fails.length() == 0);
    }
}