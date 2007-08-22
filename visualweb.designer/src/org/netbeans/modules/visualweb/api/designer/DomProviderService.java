/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.visualweb.api.designer;


import java.awt.Image;

import org.netbeans.modules.visualweb.api.designer.cssengine.StyleData;
import org.openide.filesystems.FileObject;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * Provides 'static' global service methods.
 * XXX FIXME Get rid of it.
 *
 * @author Peter Zavadsky
 */
public interface DomProviderService {

//    public MarkupDesignBean getMarkupDesignBeanForElement(Element element);
//    public MarkupMouseRegion getMarkupMouseRegionForElement(Element element);

//    /** Gets <coce>MarkupDesignBean</code> only if the specified element
//     * represents the root element generated for the component. */
//    public MarkupDesignBean getMarkupDesignBeanForComponentRootElement(Element element, Element parentBoxElement);

    public int getExpandedOffset(String unexpanded, int unexpandedOffset);
    public int getUnexpandedOffset(String unexpanded, int expandedOffset);
    public String expandHtmlEntities(String html, boolean warn, Node node);

//    public ClassLoader getContextClassLoaderForDesignContext(DesignContext designContext);

    public String getHtmlStream(Node node);

    public String getDomDocumentReplacedEventConstant();

    // XXX Methods we should get rid of.
//    /** If there is no <code>Designer</code> for the <code>DataObject</code> tries to create one. */
//    public Designer[] getDesignersForDataObject(DataObject jsfJspDataObject);
//    /** Just retrieves existing <code>FileObject</code>s for specified <code>DataObject</code>. */
//    public Designer[] findDesignersForFileObject(FileObject jsfJspFileObject);
//    /** Just retrieves existing <code>Designer</code>s for specified <code>DesignContext</code>. */
//    public Designer[] findDesignersForDesignContext(DesignContext designContext);
    /** Just retrieves existing <code>Designer</code>s for specified <code>Element</code>. */
    public Designer[] findDesignersForElement(Element element);
    /** Just retrieves existing <code>Designer</code>s for specified <code>org.openide.nodes.Node</code>. */
    public Designer[] findDesignersForNode(org.openide.nodes.Node node);

//    public boolean isValueBindingExpression(String value, boolean containsOK);

    // XXX Get rid of those.
//    public String computeFileName(Object location);
//    public int computeLineNumber(Object location, int lineno);

//    public URL getDocumentUrl(Document document);

//    public void displayErrorForLocation(String message, Object location, int lineno, int column);

//    public Element getHtmlBodyForDocument(Document document);
//    public DocumentFragment getHtmlDomFragmentForDocument(Document document);

    // XXX Get rid of these
//    public boolean isFacesBean(MarkupDesignBean bean);
//    public boolean isSpecialBean(DesignBean designBean);
//    public boolean isTrayBean(DesignBean designBean);
//    public boolean isCssPositionable(DesignBean designBean);
//    /** XXX Gets source element. */
//    public Element getElement(DesignBean designBean);
    // XXX Get rid of, source element used.
    public Element getSourceElement(Element componentRootElement);
//    /** XXX Gets source element. Is it the same like the #getElement method? */
//    public Element getMarkupBeanElement(DesignBean designBean);
    // XXX Modifying should be done outside of designer.
//    public boolean setDesignProperty(MarkupDesignBean bean, String attribute, int value);
//    public boolean setStyleAttribute(Element componentRootElement, String attribute, int value);

//    public Element findHtmlElementDescendant(DocumentFragment df);

//    public void updateLocalStyleValuesForElement(Element e, StyleData[] setStyleData, StyleData[] removeStyleData);

//    // XXX Bad architecture. Model itself should take care of this and not fire dummy events.
//    public long getContextGenearation(DesignContext context);

//    public boolean isWebFormFileObject(FileObject fileObject);

    public boolean isPrincipalElement(Element element, Element parentBoxElement);
    public boolean isFocusedElement(Element element);
    public boolean ignoreDesignBorder(Element element);

    // XXX Get rid of source elements usage.
    public Element getSourceElementWhichRendersChildren(Element element);

    public Element[] getChildComponentRootElements(Element componentRootElement);

//    // XXX Hack.
//    public MarkupDesignBean adjustRenderBeanHack(MarkupDesignBean renderBean);

//    // XXX Get rid of it.
//    public boolean isFacesComponentBean(DesignBean bean);
//    public boolean isEscapedDesignBean(DesignBean bean);

    public boolean isFacesComponent(Element componentRootElement);

//    /** Gets the component root element (rendered element) for specified bean. */
//    public Element getRenderedElement(DesignBean designBean);

    public String getRegionDisplayName(Element regionElement);

    public boolean isSameRegionOfElement(Element regionElement, Element element);

//    // XXX Get rid of this.
//    public Element getComponentRootElementForMarkupDesignBean(MarkupDesignBean markupDesignBean);
    public Element getComponentRootElementForElement(Element element);
    
    public String getInstanceName(Element componentRootElement);

    public boolean isIncludeComponentBox(Element componentRootElement);

    public boolean isSpecialComponent(Element componentRootElement);
    public boolean isTrayComponent(Element componentRootElement);
    public boolean isCssPositionable(Element componentRootElement);
    public boolean isEscapedComponent(Element componentRootElement);

    public Element getParentComponent(Element componentRootElement);
    public Element[] getChildComponents(Element componentRootElement);

    public boolean isContainerComponent(Element componentRootElement);
    public boolean isContainerTypeComponent(Element componentRootElement);

    // XXX Inline editing.
//    public String[] getEditablePropertyNames(Element componentRootElement);
    public String[] getEditableProperties(Element componentRootElement);

    public enum ResizeConstraint {
        TOP,
        LEFT,
        BOTTOM,
        RIGHT,
        MAINTAIN_ASPECT_RATIO,
        VERTICAL,
        HORIZONTAL,
        ANY
    } // End of enum.
    public ResizeConstraint[] getResizeConstraintsForComponent(Element componentRootElement);

    public boolean isRootContainerComponent(Element componentRootElement);

    // XXX Bad properties hacks, get rid of, it should be part of some action.
    public boolean hasDefaultProperty(Element componentRootElement);
    public boolean focusDefaultProperty(Element componentRootElement, String content);

    public Image getIcon(Element componentRootElement);

//    public org.openide.nodes.Node getNodeRepresentation(Element componentRootElement);
    public Element getComponentRootElementFromNode(org.openide.nodes.Node node);

//    // XXX Get rid of this.
//    public MarkupPosition getDefaultMarkupPositionUnderParent(DesignBean parent);

//    public DomProvider.Location computeLocationForPositions(String facet, Point canvasPos, Node documentPosNode, int documentPosOffset, Dimension dimension, boolean isGrid, Element droppeeElement, Element dropeeComponentRootElement, Element defaultParentComponentRootElement);

    // XXX Bad architecture of Table handling. Try to get rid of it.
    public boolean hasTableResizeSupport(Element tableComponentRootElement);
    public int testResizeColumn(Element tableComponentRootElement, int row, int column, int width);
    public int testResizeRow(Element tableComponentRootElement, int row, int column, int height);
    public void resizeColumn(Element tableComponentRootElement, int column, int width);
    public void resizeRow(Element tableComponentRootElement, int row, int height);
    
    // XXX
    public boolean areLinkedToSameBean(Element oneElement, Element otherElement);

}
