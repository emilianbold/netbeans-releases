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
import java.awt.Image;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.visualweb.api.designer.Designer;
import org.netbeans.modules.visualweb.api.designer.DomProvider;
import org.netbeans.modules.visualweb.api.designer.DomProvider.DomPosition.Bias;
import org.netbeans.modules.visualweb.api.designer.DomProviderService;
import org.netbeans.modules.visualweb.api.designer.markup.MarkupService;
import org.netbeans.modules.visualweb.spi.designer.Decoration;
import org.netbeans.spi.palette.PaletteController;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Peter Zavadsky
 */
public final class Util {
    
    private Util() {}
    
    
    public static DomProvider createDomProvider() {
        return new TestDomProvider();
    }
    
    
    
    private static class TestDomProvider implements DomProvider {

        private final Document document;
        private final Element bodyElement;
        private static final DomProviderService domProviderService = new TestDomProviderService();
        
        public TestDomProvider() {
            this.document = createTestDocument();
            this.bodyElement = findBodyElement(document.getDocumentElement());
        }
        
        public Document getHtmlDom() {
            return document;
        }

        public Element getHtmlBody() {
            return bodyElement;
        }

        public PaletteController getPaletteController() {
            return null;
        }

        public boolean canImport(JComponent comp, DataFlavor[] transferFlavors, Transferable transferable) {
            return false;
        }

        public int computeActions(Element dropeeComponentRootElement, Transferable transferable) {
            return -1;
        }

        public int processLinks(Element origElement, Element componentRootElement) {
            return -1;
        }

        public boolean isGridMode() {
            return false;
        }

        public URL getBaseUrl() {
            return null;
        }

        public URL resolveUrl(String urlString) {
            try {
                return new URL(urlString);
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }

        public boolean canDropComponentsAtNode(Element[] componentRootElements, Node node) {
            return false;
        }

        public boolean isFormComponent(Element componentRootElement) {
            return false;
        }

        public int getDropType(Element origDropeeComponentRootElement, Element droppeeElement, Transferable t, boolean linkOnly) {
            return -1;
        }

        public int getDropTypeForComponent(Element origDropeeComponentRootElement, Element droppeeElement, Element componentRootElement, boolean linkOnly) {
            return -1;
        }

        public Element getComponentRootElementEquivalentTo(Element oldComponentRootElement) {
            return oldComponentRootElement;
        }

        public boolean canHighlightComponentRootElmenet(Element componentRootElement) {
            return false;
        }

        public boolean moveComponent(Element componentRootElement, Node parentNode, Node before) {
            return false;
        }

        public Designer[] getExternalDesigners(URL url) {
            return new Designer[0];
        }

        public boolean hasCachedExternalFrames() {
            return false;
        }

        public DomDocument getDomDocument() {
            return null;
        }

        public int compareBoundaryPoints(Node endPointA, int offsetA, Node endPointB, int offsetB) {
            return -1;
        }

        public DomPosition createDomPosition(Node node, int offset, Bias bias) {
            return null;
        }

        public DomPosition createDomPosition(Node node, boolean after) {
            return null;
        }

        public DomRange createDomRange(Node dotNode, int dotOffset, Node markNode, int markOffset) {
            return null;
        }

        public DomPosition first(DomPosition dot, DomPosition mark) {
            return null;
        }

        public DomPosition last(DomPosition dot, DomPosition mark) {
            return null;
        }

        public boolean isRenderedNode(Node node) {
            return node != null && node.getOwnerDocument() == document;
        }

        public void paintDesignerDecorations(Graphics2D g, Designer designer) {
        }

        public Decoration getDecoration(Element element) {
            return null;
        }

        public InlineEditorSupport createInlineEditorSupport(Element componentRootElement, String propertyName, String xpath) {
            return null;
        }

        public void importString(Designer designer, String string, Point canvasPos, Node documentPosNode, int documentPosOffset, Dimension dimension, boolean isGrid, Element droppeeElement, Element dropeeComponentRootElement) {
        }

        public boolean importData(Designer designer, JComponent comp, Transferable t, Point canvasPos, Node documentPosNode, int documentPosOffset, Dimension dimension, boolean isGrid, Element droppeeElement, Element dropeeComponentRootElement, int dropAction) {
            return false;
        }

        public DomProviderService getDomProviderService() {
            return domProviderService;
        }

    } // TestDomProvider.

    
    
    
    private static class TestDomProviderService implements DomProviderService {

        public int getExpandedOffset(String unexpanded, int unexpandedOffset) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int getUnexpandedOffset(String unexpanded, int expandedOffset) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String expandHtmlEntities(String html, boolean warn, Node node) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String getHtmlStream(Node node) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String getDomDocumentReplacedEventConstant() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Element getSourceElement(Element componentRootElement) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isPrincipalElement(Element element, Element parentBoxElement) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isFocusedElement(Element element) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean ignoreDesignBorder(Element element) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Element getSourceElementWhichRendersChildren(Element element) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Element[] getChildComponentRootElements(Element componentRootElement) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isFacesComponent(Element componentRootElement) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String getRegionDisplayName(Element regionElement) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isSameRegionOfElement(Element regionElement, Element element) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Element getComponentRootElementForElement(Element element) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String getInstanceName(Element componentRootElement) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isIncludeComponentBox(Element componentRootElement) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isSpecialComponent(Element componentRootElement) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isTrayComponent(Element componentRootElement) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isCssPositionable(Element componentRootElement) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isEscapedComponent(Element componentRootElement) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Element getParentComponent(Element componentRootElement) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Element[] getChildComponents(Element componentRootElement) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isContainerComponent(Element componentRootElement) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isContainerTypeComponent(Element componentRootElement) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String[] getEditableProperties(Element componentRootElement) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public ResizeConstraint[] getResizeConstraintsForComponent(Element componentRootElement) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isRootContainerComponent(Element componentRootElement) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean hasDefaultProperty(Element componentRootElement) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean focusDefaultProperty(Element componentRootElement, String content) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Image getIcon(Element componentRootElement) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Element getComponentRootElementFromNode(org.openide.nodes.Node node) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean hasTableResizeSupport(Element tableComponentRootElement) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int testResizeColumn(Element tableComponentRootElement, int row, int column, int width) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int testResizeRow(Element tableComponentRootElement, int row, int column, int height) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void resizeColumn(Element tableComponentRootElement, int column, int width) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void resizeRow(Element tableComponentRootElement, int row, int height) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean areLinkedToSameBean(Element oneElement, Element otherElement) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Node findPropertyNode(Node root, String xpaths) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    } // TestDomProviderService.
    
    private static Document createTestDocument() {
        try {
            DocumentBuilder documentBuilder = MarkupService.createRaveRenderedDocumentBuilder(true);
            return documentBuilder.parse(new InputSource(new StringReader(
                "<html>"
                + "<head>"
                + "<style type=\"text/css\">"
                + "h1 {color: #00ff00}"
                + "h2 {color: #dda0dd}"
                + "p {color: rgb(0,0,255)}"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<h1>This is header 1</h1>"
                + "<h2>This is header 2</h2>"
                + "<p>This is a paragraph</p>"
                // Test percentage
                + "<div style=\"width: 50%\">Here is only 50% width</div>"
                + "</body>"
                + "</html>"
            )));
        } catch (SAXException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ParserConfigurationException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    private static Element findBodyElement(Element element) {
        if (element == null) {
            return null;
        }
        if ("body".equals(element.getTagName())) { // TEMP
            return element;
        }
        
        Element[] children = getChildElements(element);
        for (Element child : children) {
            Element bodyElement = findBodyElement(child);
            if (bodyElement != null) {
                return bodyElement;
            }
        }
        return null;
    }
    
    private static Element[] getChildElements(Element element) {
        if (element == null) {
            return new Element[0];
        }
        
        List<Element> children = new ArrayList<Element>();
        NodeList nodeList = element.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            if (child instanceof Element) {
                children.add((Element)child);
            }
        }
        return children.toArray(new Element[children.size()]);
    }
}
