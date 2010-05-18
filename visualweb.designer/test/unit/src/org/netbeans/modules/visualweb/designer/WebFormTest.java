/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.visualweb.designer;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.net.URL;
import javax.swing.ActionMap;
import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import junit.framework.TestCase;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.visualweb.api.designer.Designer.Box;
import org.netbeans.modules.visualweb.api.designer.Designer.DesignerClickEvent;
import org.netbeans.modules.visualweb.api.designer.Designer.DesignerEvent;
import org.netbeans.modules.visualweb.api.designer.Designer.DesignerListener;
import org.netbeans.modules.visualweb.api.designer.Designer.DesignerPopupEvent;
import org.netbeans.modules.visualweb.api.designer.Designer.RenderContext;
import org.netbeans.modules.visualweb.api.designer.DomProvider;
import org.netbeans.modules.visualweb.api.designer.DomProvider.DomDocument;
import org.netbeans.modules.visualweb.api.designer.DomProvider.DomPosition;
import org.netbeans.modules.visualweb.api.designer.DomProvider.DomPosition.Bias;
import org.netbeans.modules.visualweb.api.designer.DomProvider.DomRange;
import org.netbeans.modules.visualweb.api.designer.DomProvider.InlineEditorSupport;
import org.netbeans.modules.visualweb.api.designer.DomProviderService;
import org.netbeans.modules.visualweb.css2.CssBox;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;
import org.netbeans.modules.visualweb.spi.designer.Decoration;
import org.netbeans.spi.palette.PaletteController;
import org.openide.nodes.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Peter Zavadsky
 */
public class WebFormTest extends NbTestCase {
    
    public WebFormTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of createWebForm method, of class WebForm.
     */
    public void testCreateWebForm() {
        System.out.println("createWebForm");
        DomProvider domProvider = Util.createDomProvider();
        WebForm webForm = WebForm.createWebForm(domProvider);
        assertNotNull("Null webForm for domProvider, domProvider=" + domProvider, webForm); // NOI18N
    }

    /**
     * Test of getDomProviderService method, of class WebForm.
     */
    public void testGetDomProviderService() {
        System.out.println("getDomProviderService");
        DomProvider domProvider = Util.createDomProvider();
        WebForm webForm = WebForm.createWebForm(domProvider);
        assertNotNull("Null webForm for domProvider, domProvider=" + domProvider, webForm); // NOI18N
        DomProviderService domProviderService = webForm.getDomProviderService();
        assertNotNull("There may not be a null DomProviderService!", domProviderService); // NOI18N
    }

//    /**
//     * Test of findAllWebFormsForElement method, of class WebForm.
//     */
//    public void testFindAllWebFormsForElement() {
//        System.out.println("findAllWebFormsForElement");
//        Element element = null;
//        WebForm[] expResult = null;
//        WebForm[] result = WebForm.findAllWebFormsForElement(element);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of findWebFormForElement method, of class WebForm.
//     */
//    public void testFindWebFormForElement() {
//        System.out.println("findWebFormForElement");
//        Element element = null;
//        WebForm expResult = null;
//        WebForm result = WebForm.findWebFormForElement(element);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of findWebFormForNode method, of class WebForm.
//     */
//    public void testFindWebFormForNode() {
//        System.out.println("findWebFormForNode");
//        Node node = null;
//        WebForm expResult = null;
//        WebForm result = WebForm.findWebFormForNode(node);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getDomProvider method, of class WebForm.
//     */
//    public void testGetDomProvider() {
//        System.out.println("getDomProvider");
//        WebForm instance = null;
//        DomProvider expResult = null;
//        DomProvider result = instance.getDomProvider();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of toString method, of class WebForm.
//     */
//    public void testToString() {
//        System.out.println("toString");
//        WebForm instance = null;
//        String expResult = "";
//        String result = instance.toString();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setCssBoxForElement method, of class WebForm.
//     */
//    public void testSetCssBoxForElement() {
//        System.out.println("setCssBoxForElement");
//        Element element = null;
//        CssBox box = null;
//        WebForm instance = null;
//        instance.setCssBoxForElement(element, box);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getCssBoxForElement method, of class WebForm.
//     */
//    public void testGetCssBoxForElement() {
//        System.out.println("getCssBoxForElement");
//        Element element = null;
//        WebForm instance = null;
//        CssBox expResult = null;
//        CssBox result = instance.getCssBoxForElement(element);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of copyBoxForElement method, of class WebForm.
//     */
//    public void testCopyBoxForElement() {
//        System.out.println("copyBoxForElement");
//        Element fromElement = null;
//        Element toElement = null;
//        WebForm instance = null;
//        instance.copyBoxForElement(fromElement, toElement);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of findCssBoxForElement method, of class WebForm.
//     */
//    public void testFindCssBoxForElement() {
//        System.out.println("findCssBoxForElement");
//        Element element = null;
//        WebForm instance = null;
//        CssBox expResult = null;
//        CssBox result = instance.findCssBoxForElement(element);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getPane method, of class WebForm.
//     */
//    public void testGetPane() {
//        System.out.println("getPane");
//        WebForm instance = null;
//        DesignerPane expResult = null;
//        DesignerPane result = instance.getPane();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of hasSelection method, of class WebForm.
//     */
//    public void testHasSelection() {
//        System.out.println("hasSelection");
//        WebForm instance = null;
//        boolean expResult = false;
//        boolean result = instance.hasSelection();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getSelection method, of class WebForm.
//     */
//    public void testGetSelection() {
//        System.out.println("getSelection");
//        WebForm instance = null;
//        SelectionManager expResult = null;
//        SelectionManager result = instance.getSelection();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getDomDocument method, of class WebForm.
//     */
//    public void testGetDomDocument() {
//        System.out.println("getDomDocument");
//        WebForm instance = null;
//        DomDocument expResult = null;
//        DomDocument result = instance.getDomDocument();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getHtmlDom method, of class WebForm.
//     */
//    public void testGetHtmlDom() {
//        System.out.println("getHtmlDom");
//        WebForm instance = null;
//        Document expResult = null;
//        Document result = instance.getHtmlDom();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of getHtmlBody method, of class WebForm.
     */
    public void testGetHtmlBody() {
        System.out.println("getHtmlBody");
        DomProvider domProvider = Util.createDomProvider();
        WebForm webForm = WebForm.createWebForm(domProvider);
        assertNotNull("Null WebForm instance for domProvider, domProvider=" + domProvider, webForm); // NOI18N
        Element body = webForm.getHtmlBody();
        assertNotNull("Null body elment for webForm, webForm=" + webForm, body); // NOI18N
        String tagName = body.getTagName();
        assertEquals("Incorrect body element, it was tagName=" // NOI18N
                + tagName
                + ", expected=" // NOI18N
                + HtmlTag.BODY.name,
            body.getTagName(),
            HtmlTag.BODY.name);
    }

//    /**
//     * Test of setPaneGrid method, of class WebForm.
//     */
//    public void testSetPaneGrid() {
//        System.out.println("setPaneGrid");
//        boolean gridMode = false;
//        WebForm instance = null;
//        instance.setPaneGrid(gridMode);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isGridMode method, of class WebForm.
//     */
//    public void testIsGridMode() {
//        System.out.println("isGridMode");
//        WebForm instance = null;
//        boolean expResult = false;
//        boolean result = instance.isGridMode();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getRenderPane method, of class WebForm.
//     */
//    public void testGetRenderPane() {
//        System.out.println("getRenderPane");
//        WebForm instance = null;
//        CellRendererPane expResult = null;
//        CellRendererPane result = instance.getRenderPane();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getColors method, of class WebForm.
//     */
//    public void testGetColors() {
//        System.out.println("getColors");
//        WebForm instance = null;
//        ColorManager expResult = null;
//        ColorManager result = instance.getColors();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getManager method, of class WebForm.
//     */
//    public void testGetManager() {
//        System.out.println("getManager");
//        WebForm instance = null;
//        InteractionManager expResult = null;
//        InteractionManager result = instance.getManager();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getImageCache method, of class WebForm.
//     */
//    public void testGetImageCache() {
//        System.out.println("getImageCache");
//        WebForm instance = null;
//        ImageCache expResult = null;
//        ImageCache result = instance.getImageCache();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of hasCachedFrameBoxes method, of class WebForm.
//     */
//    public void testHasCachedFrameBoxes() {
//        System.out.println("hasCachedFrameBoxes");
//        WebForm instance = null;
//        boolean expResult = false;
//        boolean result = instance.hasCachedFrameBoxes();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of changeNode method, of class WebForm.
//     */
//    public void testChangeNode() {
//        System.out.println("changeNode");
//        org.w3c.dom.Node rendered = null;
//        org.w3c.dom.Node parent = null;
//        Element[] changedElements = null;
//        WebForm instance = null;
//        instance.changeNode(rendered, parent, changedElements);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of removeNode method, of class WebForm.
//     */
//    public void testRemoveNode() {
//        System.out.println("removeNode");
//        org.w3c.dom.Node previouslyRendered = null;
//        org.w3c.dom.Node parent = null;
//        WebForm instance = null;
//        instance.removeNode(previouslyRendered, parent);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of insertNode method, of class WebForm.
//     */
//    public void testInsertNode() {
//        System.out.println("insertNode");
//        org.w3c.dom.Node rendered = null;
//        org.w3c.dom.Node parent = null;
//        WebForm instance = null;
//        instance.insertNode(rendered, parent);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of detachDomDocument method, of class WebForm.
//     */
//    public void testDetachDomDocument() {
//        System.out.println("detachDomDocument");
//        WebForm instance = null;
//        instance.detachDomDocument();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getExternalDomProviders method, of class WebForm.
//     */
//    public void testGetExternalDomProviders() {
//        System.out.println("getExternalDomProviders");
//        WebForm instance = null;
//        DomProvider[] expResult = null;
//        DomProvider[] result = instance.getExternalDomProviders();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of resetAll method, of class WebForm.
//     */
//    public void testResetAll() {
//        System.out.println("resetAll");
//        WebForm instance = null;
//        instance.resetAll();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of canImport method, of class WebForm.
//     */
//    public void testCanImport() {
//        System.out.println("canImport");
//        JComponent comp = null;
//        DataFlavor[] transferFlavors = null;
//        Transferable transferable = null;
//        WebForm instance = null;
//        boolean expResult = false;
//        boolean result = instance.canImport(comp, transferFlavors, transferable);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of computeActions method, of class WebForm.
//     */
//    public void testComputeActions() {
//        System.out.println("computeActions");
//        Element dropeeComponentRootElement = null;
//        Transferable transferable = null;
//        WebForm instance = null;
//        int expResult = 0;
//        int result = instance.computeActions(dropeeComponentRootElement, transferable);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of processLinks method, of class WebForm.
//     */
//    public void testProcessLinks() {
//        System.out.println("processLinks");
//        Element origElement = null;
//        Element componentRootElement = null;
//        WebForm instance = null;
//        int expResult = 0;
//        int result = instance.processLinks(origElement, componentRootElement);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of showDropMatch method, of class WebForm.
//     */
//    public void testShowDropMatch() {
//        System.out.println("showDropMatch");
//        Element componentRootElement = null;
//        Element regionElement = null;
//        int dropType = 0;
//        WebForm instance = null;
//        instance.showDropMatch(componentRootElement, regionElement, dropType);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of clearDropMatch method, of class WebForm.
//     */
//    public void testClearDropMatch() {
//        System.out.println("clearDropMatch");
//        WebForm instance = null;
//        instance.clearDropMatch();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of selectComponentDelayed method, of class WebForm.
//     */
//    public void testSelectComponentDelayed() {
//        System.out.println("selectComponentDelayed");
//        Element componentRootElement = null;
//        WebForm instance = null;
//        instance.selectComponentDelayed(componentRootElement);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of inlineEditComponents method, of class WebForm.
//     */
//    public void testInlineEditComponents() {
//        System.out.println("inlineEditComponents");
//        Element[] componentRootElements = null;
//        WebForm instance = null;
//        instance.inlineEditComponents(componentRootElements);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getBaseUrl method, of class WebForm.
//     */
//    public void testGetBaseUrl() {
//        System.out.println("getBaseUrl");
//        WebForm instance = null;
//        URL expResult = null;
//        URL result = instance.getBaseUrl();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of resolveUrl method, of class WebForm.
//     */
//    public void testResolveUrl() {
//        System.out.println("resolveUrl");
//        String urlString = "";
//        WebForm instance = null;
//        URL expResult = null;
//        URL result = instance.resolveUrl(urlString);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getPaletteController method, of class WebForm.
//     */
//    public void testGetPaletteController() {
//        System.out.println("getPaletteController");
//        WebForm instance = null;
//        PaletteController expResult = null;
//        PaletteController result = instance.getPaletteController();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of canDropComponentsAtNode method, of class WebForm.
//     */
//    public void testCanDropComponentsAtNode() {
//        System.out.println("canDropComponentsAtNode");
//        Element[] componentRootElements = null;
//        org.w3c.dom.Node node = null;
//        WebForm instance = null;
//        boolean expResult = false;
//        boolean result = instance.canDropComponentsAtNode(componentRootElements, node);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isNormalAndHasFacesComponent method, of class WebForm.
//     */
//    public void testIsNormalAndHasFacesComponent() {
//        System.out.println("isNormalAndHasFacesComponent");
//        Element componentRootElement = null;
//        WebForm instance = null;
//        boolean expResult = false;
//        boolean result = instance.isNormalAndHasFacesComponent(componentRootElement);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of moveComponent method, of class WebForm.
//     */
//    public void testMoveComponent() {
//        System.out.println("moveComponent");
//        Element componentRootElement = null;
//        org.w3c.dom.Node parentNode = null;
//        org.w3c.dom.Node before = null;
//        WebForm instance = null;
//        boolean expResult = false;
//        boolean result = instance.moveComponent(componentRootElement, parentNode, before);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getDefaultParentComponent method, of class WebForm.
//     */
//    public void testGetDefaultParentComponent() {
//        System.out.println("getDefaultParentComponent");
//        WebForm instance = null;
//        Element expResult = null;
//        Element result = instance.getDefaultParentComponent();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isModelValid method, of class WebForm.
//     */
//    public void testIsModelValid() {
//        System.out.println("isModelValid");
//        WebForm instance = null;
//        boolean expResult = false;
//        boolean result = instance.isModelValid();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of readLock method, of class WebForm.
//     */
//    public void testReadLock() {
//        System.out.println("readLock");
//        WebForm instance = null;
//        instance.readLock();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of readUnlock method, of class WebForm.
//     */
//    public void testReadUnlock() {
//        System.out.println("readUnlock");
//        WebForm instance = null;
//        instance.readUnlock();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isModelBusted method, of class WebForm.
//     */
//    public void testIsModelBusted() {
//        System.out.println("isModelBusted");
//        WebForm instance = null;
//        boolean expResult = false;
//        boolean result = instance.isModelBusted();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isFormComponent method, of class WebForm.
//     */
//    public void testIsFormComponent() {
//        System.out.println("isFormComponent");
//        Element componentRootElement = null;
//        WebForm instance = null;
//        boolean expResult = false;
//        boolean result = instance.isFormComponent(componentRootElement);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getDropType method, of class WebForm.
//     */
//    public void testGetDropType() {
//        System.out.println("getDropType");
//        Element origDropeeComponentRootElement = null;
//        Element droppeeElement = null;
//        Transferable t = null;
//        boolean linkOnly = false;
//        WebForm instance = null;
//        int expResult = 0;
//        int result = instance.getDropType(origDropeeComponentRootElement, droppeeElement, t, linkOnly);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getDropTypeForComponent method, of class WebForm.
//     */
//    public void testGetDropTypeForComponent() {
//        System.out.println("getDropTypeForComponent");
//        Element origDropeeComponentRootElement = null;
//        Element droppeeElement = null;
//        Element componentRootElement = null;
//        boolean linkOnly = false;
//        WebForm instance = null;
//        int expResult = 0;
//        int result = instance.getDropTypeForComponent(origDropeeComponentRootElement, droppeeElement, componentRootElement, linkOnly);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getComponentRootElementEquivalentTo method, of class WebForm.
//     */
//    public void testGetComponentRootElementEquivalentTo() {
//        System.out.println("getComponentRootElementEquivalentTo");
//        Element oldComponentRootElement = null;
//        WebForm instance = null;
//        Element expResult = null;
//        Element result = instance.getComponentRootElementEquivalentTo(oldComponentRootElement);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of canHighlightComponentRootElement method, of class WebForm.
//     */
//    public void testCanHighlightComponentRootElement() {
//        System.out.println("canHighlightComponentRootElement");
//        Element componentRootElement = null;
//        WebForm instance = null;
//        boolean expResult = false;
//        boolean result = instance.canHighlightComponentRootElement(componentRootElement);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of createInlineEditorSupport method, of class WebForm.
//     */
//    public void testCreateInlineEditorSupport() {
//        System.out.println("createInlineEditorSupport");
//        Element componentRootElement = null;
//        String propertyName = "";
//        WebForm instance = null;
//        InlineEditorSupport expResult = null;
//        InlineEditorSupport result = instance.createInlineEditorSupport(componentRootElement, propertyName);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of importString method, of class WebForm.
//     */
//    public void testImportString() {
//        System.out.println("importString");
//        String string = "";
//        Point canvasPos = null;
//        org.w3c.dom.Node documentPosNode = null;
//        int documentPosOffset = 0;
//        Dimension dimension = null;
//        boolean isGrid = false;
//        Element droppeeElement = null;
//        Element dropeeComponentRootElement = null;
//        Element defaultParentComponentRootElement = null;
//        WebForm instance = null;
//        instance.importString(string, canvasPos, documentPosNode, documentPosOffset, dimension, isGrid, droppeeElement, dropeeComponentRootElement, defaultParentComponentRootElement);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of importData method, of class WebForm.
//     */
//    public void testImportData() {
//        System.out.println("importData");
//        JComponent comp = null;
//        Transferable t = null;
//        Point canvasPos = null;
//        org.w3c.dom.Node documentPosNode = null;
//        int documentPosOffset = 0;
//        Dimension dimension = null;
//        boolean isGrid = false;
//        Element droppeeElement = null;
//        Element dropeeComponentRootElement = null;
//        Element defaultParentComponentRootElement = null;
//        int dropAction = 0;
//        WebForm instance = null;
//        boolean expResult = false;
//        boolean result = instance.importData(comp, t, canvasPos, documentPosNode, documentPosOffset, dimension, isGrid, droppeeElement, dropeeComponentRootElement, defaultParentComponentRootElement, dropAction);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of startInlineEditing method, of class WebForm.
//     */
//    public void testStartInlineEditing() {
//        System.out.println("startInlineEditing");
//        Element componentRootElement = null;
//        String propertyName = "";
//        WebForm instance = null;
//        instance.startInlineEditing(componentRootElement, propertyName);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of selectComponent method, of class WebForm.
//     */
//    public void testSelectComponent() {
//        System.out.println("selectComponent");
//        Element componentRootElement = null;
//        WebForm instance = null;
//        instance.selectComponent(componentRootElement);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getSelectedCount method, of class WebForm.
//     */
//    public void testGetSelectedCount() {
//        System.out.println("getSelectedCount");
//        WebForm instance = null;
//        int expResult = 0;
//        int result = instance.getSelectedCount();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getSelectedComponents method, of class WebForm.
//     */
//    public void testGetSelectedComponents() {
//        System.out.println("getSelectedComponents");
//        WebForm instance = null;
//        Element[] expResult = null;
//        Element[] result = instance.getSelectedComponents();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of modelToView method, of class WebForm.
//     */
//    public void testModelToView() {
//        System.out.println("modelToView");
//        DomPosition pos = null;
//        WebForm instance = null;
//        Rectangle expResult = null;
//        Rectangle result = instance.modelToView(pos);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of viewToModel method, of class WebForm.
//     */
//    public void testViewToModel() {
//        System.out.println("viewToModel");
//        Point pt = null;
//        WebForm instance = null;
//        DomPosition expResult = null;
//        DomPosition result = instance.viewToModel(pt);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isInlineEditing method, of class WebForm.
//     */
//    public void testIsInlineEditing() {
//        System.out.println("isInlineEditing");
//        WebForm instance = null;
//        boolean expResult = false;
//        boolean result = instance.isInlineEditing();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of createDomPosition method, of class WebForm.
//     */
//    public void testCreateDomPosition() {
//        System.out.println("createDomPosition");
//        org.w3c.dom.Node node = null;
//        int offset = 0;
//        Bias bias = null;
//        WebForm instance = null;
//        DomPosition expResult = null;
//        DomPosition result = instance.createDomPosition(node, offset, bias);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of createDomRange method, of class WebForm.
//     */
//    public void testCreateDomRange() {
//        System.out.println("createDomRange");
//        org.w3c.dom.Node dotNode = null;
//        int dotOffset = 0;
//        org.w3c.dom.Node markNode = null;
//        int markOffset = 0;
//        WebForm instance = null;
//        DomRange expResult = null;
//        DomRange result = instance.createDomRange(dotNode, dotOffset, markNode, markOffset);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of compareBoundaryPoints method, of class WebForm.
//     */
//    public void testCompareBoundaryPoints() {
//        System.out.println("compareBoundaryPoints");
//        org.w3c.dom.Node endPointA = null;
//        int offsetA = 0;
//        org.w3c.dom.Node endPointB = null;
//        int offsetB = 0;
//        WebForm instance = null;
//        int expResult = 0;
//        int result = instance.compareBoundaryPoints(endPointA, offsetA, endPointB, offsetB);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of first method, of class WebForm.
//     */
//    public void testFirst() {
//        System.out.println("first");
//        DomPosition dot = null;
//        DomPosition mark = null;
//        WebForm instance = null;
//        DomPosition expResult = null;
//        DomPosition result = instance.first(dot, mark);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of last method, of class WebForm.
//     */
//    public void testLast() {
//        System.out.println("last");
//        DomPosition dot = null;
//        DomPosition mark = null;
//        WebForm instance = null;
//        DomPosition expResult = null;
//        DomPosition result = instance.last(dot, mark);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of findExternalForm method, of class WebForm.
//     */
//    public void testFindExternalForm() {
//        System.out.println("findExternalForm");
//        URL url = null;
//        WebForm instance = null;
//        WebForm expResult = null;
//        WebForm result = instance.findExternalForm(url);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of reuseCssStyle method, of class WebForm.
//     */
//    public void testReuseCssStyle() {
//        System.out.println("reuseCssStyle");
//        WebForm webForm = null;
//        WebForm instance = null;
//        instance.reuseCssStyle(webForm);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of findBox method, of class WebForm.
//     */
//    public void testFindBox() {
//        System.out.println("findBox");
//        int x = 0;
//        int y = 0;
//        WebForm instance = null;
//        Box expResult = null;
//        Box result = instance.findBox(x, y);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of findBoxForSourceElement method, of class WebForm.
//     */
//    public void testFindBoxForSourceElement() {
//        System.out.println("findBoxForSourceElement");
//        Element sourceElement = null;
//        WebForm instance = null;
//        Box expResult = null;
//        Box result = instance.findBoxForSourceElement(sourceElement);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of findBoxForComponentRootElement method, of class WebForm.
//     */
//    public void testFindBoxForComponentRootElement() {
//        System.out.println("findBoxForComponentRootElement");
//        Element componentRootElement = null;
//        WebForm instance = null;
//        Box expResult = null;
//        Box result = instance.findBoxForComponentRootElement(componentRootElement);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of findBoxForElement method, of class WebForm.
//     */
//    public void testFindBoxForElement() {
//        System.out.println("findBoxForElement");
//        Element element = null;
//        WebForm instance = null;
//        Box expResult = null;
//        Box result = instance.findBoxForElement(element);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getPrimarySelectedComponent method, of class WebForm.
//     */
//    public void testGetPrimarySelectedComponent() {
//        System.out.println("getPrimarySelectedComponent");
//        WebForm instance = null;
//        Element expResult = null;
//        Element result = instance.getPrimarySelectedComponent();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of computeNextPosition method, of class WebForm.
//     */
//    public void testComputeNextPosition() {
//        System.out.println("computeNextPosition");
//        DomPosition pos = null;
//        WebForm instance = null;
//        DomPosition expResult = null;
//        DomPosition result = instance.computeNextPosition(pos);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of computePreviousPosition method, of class WebForm.
//     */
//    public void testComputePreviousPosition() {
//        System.out.println("computePreviousPosition");
//        DomPosition pos = null;
//        WebForm instance = null;
//        DomPosition expResult = null;
//        DomPosition result = instance.computePreviousPosition(pos);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isInsideEditableRegion method, of class WebForm.
//     */
//    public void testIsInsideEditableRegion() {
//        System.out.println("isInsideEditableRegion");
//        DomPosition pos = null;
//        WebForm instance = null;
//        boolean expResult = false;
//        boolean result = instance.isInsideEditableRegion(pos);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of finishInlineEditing method, of class WebForm.
//     */
//    public void testFinishInlineEditing() {
//        System.out.println("finishInlineEditing");
//        boolean cancel = false;
//        WebForm instance = null;
//        instance.finishInlineEditing(cancel);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of invokeDeleteNextCharAction method, of class WebForm.
//     */
//    public void testInvokeDeleteNextCharAction() {
//        System.out.println("invokeDeleteNextCharAction");
//        ActionEvent evt = null;
//        WebForm instance = null;
//        instance.invokeDeleteNextCharAction(evt);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of inlineCopyText method, of class WebForm.
//     */
//    public void testInlineCopyText() {
//        System.out.println("inlineCopyText");
//        boolean isCut = false;
//        WebForm instance = null;
//        Transferable expResult = null;
//        Transferable result = instance.inlineCopyText(isCut);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getPrimarySelection method, of class WebForm.
//     */
//    public void testGetPrimarySelection() {
//        System.out.println("getPrimarySelection");
//        WebForm instance = null;
//        Element expResult = null;
//        Element result = instance.getPrimarySelection();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getSelectedContainer method, of class WebForm.
//     */
//    public void testGetSelectedContainer() {
//        System.out.println("getSelectedContainer");
//        WebForm instance = null;
//        Element expResult = null;
//        Element result = instance.getSelectedContainer();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setSelectedComponents method, of class WebForm.
//     */
//    public void testSetSelectedComponents() {
//        System.out.println("setSelectedComponents");
//        Element[] componentRootElements = null;
//        boolean update = false;
//        WebForm instance = null;
//        instance.setSelectedComponents(componentRootElements, update);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of clearSelection method, of class WebForm.
//     */
//    public void testClearSelection() {
//        System.out.println("clearSelection");
//        boolean update = false;
//        WebForm instance = null;
//        instance.clearSelection(update);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of syncSelection method, of class WebForm.
//     */
//    public void testSyncSelection() {
//        System.out.println("syncSelection");
//        boolean update = false;
//        WebForm instance = null;
//        instance.syncSelection(update);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of updateSelectedNodes method, of class WebForm.
//     */
//    public void testUpdateSelectedNodes() {
//        System.out.println("updateSelectedNodes");
//        WebForm instance = null;
//        instance.updateSelectedNodes();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getPageBox method, of class WebForm.
//     */
//    public void testGetPageBox() {
//        System.out.println("getPageBox");
//        WebForm instance = null;
//        Box expResult = null;
//        Box result = instance.getPageBox();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getPastePoint method, of class WebForm.
//     */
//    public void testGetPastePoint() {
//        System.out.println("getPastePoint");
//        WebForm instance = null;
//        Point expResult = null;
//        Point result = instance.getPastePoint();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getPaneActionMap method, of class WebForm.
//     */
//    public void testGetPaneActionMap() {
//        System.out.println("getPaneActionMap");
//        WebForm instance = null;
//        ActionMap expResult = null;
//        ActionMap result = instance.getPaneActionMap();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of paneRequestFocus method, of class WebForm.
//     */
//    public void testPaneRequestFocus() {
//        System.out.println("paneRequestFocus");
//        WebForm instance = null;
//        instance.paneRequestFocus();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of createPaneComponent method, of class WebForm.
//     */
//    public void testCreatePaneComponent() {
//        System.out.println("createPaneComponent");
//        WebForm instance = null;
//        JComponent expResult = null;
//        JComponent result = instance.createPaneComponent();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of updatePaneViewPort method, of class WebForm.
//     */
//    public void testUpdatePaneViewPort() {
//        System.out.println("updatePaneViewPort");
//        WebForm instance = null;
//        instance.updatePaneViewPort();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of hasPaneCaret method, of class WebForm.
//     */
//    public void testHasPaneCaret() {
//        System.out.println("hasPaneCaret");
//        WebForm instance = null;
//        boolean expResult = false;
//        boolean result = instance.hasPaneCaret();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getPaneCaretRange method, of class WebForm.
//     */
//    public void testGetPaneCaretRange() {
//        System.out.println("getPaneCaretRange");
//        WebForm instance = null;
//        DomRange expResult = null;
//        DomRange result = instance.getPaneCaretRange();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setPaneCaret method, of class WebForm.
//     */
//    public void testSetPaneCaret() {
//        System.out.println("setPaneCaret");
//        DomPosition pos = null;
//        WebForm instance = null;
//        instance.setPaneCaret(pos);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of resetPanePageBox method, of class WebForm.
//     */
//    public void testResetPanePageBox() {
//        System.out.println("resetPanePageBox");
//        WebForm instance = null;
//        instance.resetPanePageBox();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of redoPaneLayout method, of class WebForm.
//     */
//    public void testRedoPaneLayout() {
//        System.out.println("redoPaneLayout");
//        boolean immediate = false;
//        WebForm instance = null;
//        instance.redoPaneLayout(immediate);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isRenderedNode method, of class WebForm.
//     */
//    public void testIsRenderedNode() {
//        System.out.println("isRenderedNode");
//        org.w3c.dom.Node node = null;
//        WebForm instance = null;
//        boolean expResult = false;
//        boolean result = instance.isRenderedNode(node);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of addDesignerListener method, of class WebForm.
//     */
//    public void testAddDesignerListener() {
//        System.out.println("addDesignerListener");
//        DesignerListener l = null;
//        WebForm instance = null;
//        instance.addDesignerListener(l);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of removeDesignerListener method, of class WebForm.
//     */
//    public void testRemoveDesignerListener() {
//        System.out.println("removeDesignerListener");
//        DesignerListener l = null;
//        WebForm instance = null;
//        instance.removeDesignerListener(l);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of fireUserActionPerformed method, of class WebForm.
//     */
//    public void testFireUserActionPerformed() {
//        System.out.println("fireUserActionPerformed");
//        DesignerEvent evt = null;
//        WebForm instance = null;
//        instance.fireUserActionPerformed(evt);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of fireUserPopupActionPerformed method, of class WebForm.
//     */
//    public void testFireUserPopupActionPerformed() {
//        System.out.println("fireUserPopupActionPerformed");
//        DesignerPopupEvent evt = null;
//        WebForm instance = null;
//        instance.fireUserPopupActionPerformed(evt);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of fireUserElementClicked method, of class WebForm.
//     */
//    public void testFireUserElementClicked() {
//        System.out.println("fireUserElementClicked");
//        DesignerClickEvent evt = null;
//        WebForm instance = null;
//        instance.fireUserElementClicked(evt);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of fireSelectionChanged method, of class WebForm.
//     */
//    public void testFireSelectionChanged() {
//        System.out.println("fireSelectionChanged");
//        DesignerEvent evt = null;
//        WebForm instance = null;
//        instance.fireSelectionChanged(evt);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of paintDesignerDecorations method, of class WebForm.
//     */
//    public void testPaintDesignerDecorations() {
//        System.out.println("paintDesignerDecorations");
//        Graphics2D g = null;
//        WebForm instance = null;
//        instance.paintDesignerDecorations(g);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of createRenderContext method, of class WebForm.
//     */
//    public void testCreateRenderContext() {
//        System.out.println("createRenderContext");
//        WebForm instance = null;
//        RenderContext expResult = null;
//        RenderContext result = instance.createRenderContext();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setPaintSizeMask method, of class WebForm.
//     */
//    public void testSetPaintSizeMask() {
//        System.out.println("setPaintSizeMask");
//        boolean paintSizeMask = false;
//        WebForm instance = null;
//        instance.setPaintSizeMask(paintSizeMask);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isPaintSizeMask method, of class WebForm.
//     */
//    public void testIsPaintSizeMask() {
//        System.out.println("isPaintSizeMask");
//        WebForm instance = null;
//        boolean expResult = false;
//        boolean result = instance.isPaintSizeMask();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getDecoration method, of class WebForm.
//     */
//    public void testGetDecoration() {
//        System.out.println("getDecoration");
//        Element element = null;
//        WebForm instance = null;
//        Decoration expResult = null;
//        Decoration result = instance.getDecoration(element);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isShowDecorations method, of class WebForm.
//     */
//    public void testIsShowDecorations() {
//        System.out.println("isShowDecorations");
//        WebForm instance = null;
//        boolean expResult = false;
//        boolean result = instance.isShowDecorations();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getDefaultFontSize method, of class WebForm.
//     */
//    public void testGetDefaultFontSize() {
//        System.out.println("getDefaultFontSize");
//        WebForm instance = null;
//        int expResult = 0;
//        int result = instance.getDefaultFontSize();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getPageSizeWidth method, of class WebForm.
//     */
//    public void testGetPageSizeWidth() {
//        System.out.println("getPageSizeWidth");
//        WebForm instance = null;
//        int expResult = 0;
//        int result = instance.getPageSizeWidth();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getPageSizeHeight method, of class WebForm.
//     */
//    public void testGetPageSizeHeight() {
//        System.out.println("getPageSizeHeight");
//        WebForm instance = null;
//        int expResult = 0;
//        int result = instance.getPageSizeHeight();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isGridShow method, of class WebForm.
//     */
//    public void testIsGridShow() {
//        System.out.println("isGridShow");
//        WebForm instance = null;
//        boolean expResult = false;
//        boolean result = instance.isGridShow();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isGridSnap method, of class WebForm.
//     */
//    public void testIsGridSnap() {
//        System.out.println("isGridSnap");
//        WebForm instance = null;
//        boolean expResult = false;
//        boolean result = instance.isGridSnap();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getGridWidth method, of class WebForm.
//     */
//    public void testGetGridWidth() {
//        System.out.println("getGridWidth");
//        WebForm instance = null;
//        int expResult = 0;
//        int result = instance.getGridWidth();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getGridHeight method, of class WebForm.
//     */
//    public void testGetGridHeight() {
//        System.out.println("getGridHeight");
//        WebForm instance = null;
//        int expResult = 0;
//        int result = instance.getGridHeight();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getGridTraceWidth method, of class WebForm.
//     */
//    public void testGetGridTraceWidth() {
//        System.out.println("getGridTraceWidth");
//        WebForm instance = null;
//        int expResult = 0;
//        int result = instance.getGridTraceWidth();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getGridTraceHeight method, of class WebForm.
//     */
//    public void testGetGridTraceHeight() {
//        System.out.println("getGridTraceHeight");
//        WebForm instance = null;
//        int expResult = 0;
//        int result = instance.getGridTraceHeight();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getGridOffset method, of class WebForm.
//     */
//    public void testGetGridOffset() {
//        System.out.println("getGridOffset");
//        WebForm instance = null;
//        int expResult = 0;
//        int result = instance.getGridOffset();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of adjustX method, of class WebForm.
//     */
//    public void testAdjustX() {
//        System.out.println("adjustX");
//        int x = 0;
//        WebForm instance = null;
//        int expResult = 0;
//        int result = instance.adjustX(x);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of adjustY method, of class WebForm.
//     */
//    public void testAdjustY() {
//        System.out.println("adjustY");
//        int y = 0;
//        WebForm instance = null;
//        int expResult = 0;
//        int result = instance.adjustY(y);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of snapX method, of class WebForm.
//     */
//    public void testSnapX() {
//        System.out.println("snapX");
//        int x = 0;
//        Box parent = null;
//        WebForm instance = null;
//        int expResult = 0;
//        int result = instance.snapX(x, parent);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of snapY method, of class WebForm.
//     */
//    public void testSnapY() {
//        System.out.println("snapY");
//        int y = 0;
//        Box parent = null;
//        WebForm instance = null;
//        int expResult = 0;
//        int result = instance.snapY(y, parent);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setInitialFocusMarkCssBox method, of class WebForm.
//     */
//    public void testSetInitialFocusMarkCssBox() {
//        System.out.println("setInitialFocusMarkCssBox");
//        CssBox cssBox = null;
//        WebForm instance = null;
//        instance.setInitialFocusMarkCssBox(cssBox);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getInitialFocusMarkCssBox method, of class WebForm.
//     */
//    public void testGetInitialFocusMarkCssBox() {
//        System.out.println("getInitialFocusMarkCssBox");
//        WebForm instance = null;
//        CssBox expResult = null;
//        CssBox result = instance.getInitialFocusMarkCssBox();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

}
