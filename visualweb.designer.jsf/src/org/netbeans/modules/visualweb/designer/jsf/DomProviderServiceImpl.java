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


package org.netbeans.modules.visualweb.designer.jsf;

import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.DesignInfo;
import com.sun.rave.designtime.markup.MarkupTableDesignInfo;
import java.awt.Component;
import java.awt.Container;
import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.FeatureDescriptor;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;
import org.netbeans.modules.visualweb.api.designer.Designer;
import org.netbeans.modules.visualweb.api.designer.DomProviderService;
import org.netbeans.modules.visualweb.api.designer.cssengine.StyleData;
import org.netbeans.modules.visualweb.api.designer.markup.MarkupService;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.faces.FacesDesignContext;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import com.sun.rave.designtime.markup.MarkupMouseRegion;
import org.netbeans.modules.visualweb.insync.InSyncServiceProvider;
import org.netbeans.modules.visualweb.insync.Util;
import org.netbeans.modules.visualweb.insync.faces.Entities;
import org.netbeans.modules.visualweb.insync.faces.FacesPageUnit;
import org.netbeans.modules.visualweb.insync.live.DesignBeanNode;
import org.netbeans.modules.visualweb.insync.live.LiveUnit;
import org.netbeans.modules.visualweb.insync.markup.MarkupUnit;
import org.netbeans.modules.visualweb.xhtml.F_Verbatim;
import java.util.ArrayList;
import java.util.List;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;
import org.netbeans.modules.visualweb.xhtml.Jsp_Directive_Include;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Implements <code>DomProviderService</code>.
 *
 * @author Peter Zavadsky
 * @author Tor Norbye (old original code)
 */
public class DomProviderServiceImpl implements DomProviderService {

    /** Flag to turn off vb expression editing in inline editing mode */
    private static final boolean NO_EDIT_VB_EXPR = !Boolean.getBoolean("rave.allow-vb-editing");

    /** Creates a new instance of DomProviderServiceImpl */
    public DomProviderServiceImpl() {
    }


//    public MarkupDesignBean getMarkupDesignBeanForElement(Element element) {
//        return MarkupUnit.getMarkupDesignBeanForElement(element);
//    }

    public MarkupMouseRegion getMarkupMouseRegionForElement(Element element) {
        return FacesPageUnit.getMarkupMouseRegionForElement(element);
    }

    public int getUnexpandedOffset(String unexpanded, int expandedOffset) {
        return Entities.getUnexpandedOffset(unexpanded, expandedOffset);
    }

    public int getExpandedOffset(String unexpanded, int unexpandedOffset) {
        return Entities.getExpandedOffset(unexpanded, unexpandedOffset);
    }

    public String expandHtmlEntities(String html, boolean warn, Node node) {
        return Entities.expandHtmlEntities(html, warn, node);
    }

//    public ClassLoader getContextClassLoaderForDesignContext(DesignContext designContext) {
//        return InSyncServiceProvider.get().getContextClassLoader(designContext);
//    }

    public String getHtmlStream(Node node) {
//        return InSyncServiceProvider.get().getHtmlStream(node);
        return Util.getHtmlStream(node);
    }

    public String getDomDocumentReplacedEventConstant() {
        return MarkupUnit.DOM_DOCUMENT_REPLACED;
    }

//    public Designer[] getDesignersForDataObject(DataObject jsfJspDataObject) {
//        return JsfForm.getDesignersForDataObject(jsfJspDataObject);
//    }

//    public Designer[] findDesignersForFileObject(FileObject jsfJspFileObject) {
//        return JsfForm.findDesignersForFileObject(jsfJspFileObject);
//    }

//    public Designer[] findDesignersForDesignContext(DesignContext designContext) {
//        return JsfForm.findDesignersForDesignContext(designContext);
//    }

    public Designer[] findDesignersForElement(Element element) {
        return JsfForm.findDesignersForElement(element);
    }
    
    public Designer[] findDesignersForNode(org.openide.nodes.Node node) {
        DesignBean bean = (DesignBean)node.getLookup().lookup(DesignBean.class);
        return bean == null ? new Designer[0] : JsfForm.findDesignersForDesignContext(bean.getDesignContext());
    }

//    public MarkupDesignBean getMarkupDesignBeanForComponentRootElement(Element element, Element parentBoxElement) {
//        if (element == null) {
//            return null;
//        }
//        MarkupDesignBean markupDesignBean = MarkupUnit.getMarkupDesignBeanForElement(element);
//
//        if (parentBoxElement != null) {
//            // XXX #6494207 There are some elements which have assigned beans but
//            // are not mapped to any boxes, and in that case we might have not find
//            // the root box.
//            if (markupDesignBean == MarkupUnit.getMarkupDesignBeanForElement(parentBoxElement)) {
//                markupDesignBean = null;
//            }
//        } else {
//            // XXX Is this needed now?
//            Node parentNode = element.getParentNode();
//            if (parentNode instanceof Element
//            && MarkupUnit.getMarkupDesignBeanForElement((Element)parentNode) == markupDesignBean) {
//                markupDesignBean = null;
//            }
//        }
//        
//        return markupDesignBean;
//    }
    
    public boolean isPrincipalElement(Element element, Element parentBoxElement) {
        if (element == null) {
            return false;
        }
        MarkupDesignBean markupDesignBean = MarkupUnit.getMarkupDesignBeanForElement(element);

        if (parentBoxElement != null) {
            // XXX #6494207 There are some elements which have assigned beans but
            // are not mapped to any boxes, and in that case we might have not find
            // the root box.
            if (markupDesignBean == MarkupUnit.getMarkupDesignBeanForElement(parentBoxElement)) {
                markupDesignBean = null;
            }
        } else {
            // XXX Is this needed now?
            Node parentNode = element.getParentNode();
            if (parentNode instanceof Element
            && MarkupUnit.getMarkupDesignBeanForElement((Element)parentNode) == markupDesignBean) {
                markupDesignBean = null;
            }
        }
        
        return markupDesignBean != null;
    }

//    public boolean isValueBindingExpression(String value, boolean containsOK) {
//        return DesignBeanNode.isValueBindingExpression(value, containsOK);
//    }

//    public String computeFileName(Object location) {
//        return Util.computeFileName(location);
//    }

//    public int computeLineNumber(Object location, int lineno) {
//        return Util.computeLineNumber(location, lineno);
//    }

//    public URL getDocumentUrl(Document document) {
//        return Util.getDocumentUrl(document);
//    }

//    public void displayErrorForLocation(String message, Object location, int lineno, int column) {
//        Util.displayErrorForLocation(message, location, lineno, column);
//    }

//    public Element getHtmlBodyForDocument(Document document) {
//        return Util.getHtmlBodyForDocument(document);
//    }

//    public DocumentFragment getHtmlDomFragmentForDocument(Document document) {
//        return Util.getHtmlDomFragmentForDocument(document);
//    }

//    public boolean isFacesBean(MarkupDesignBean markupDesignBean) {
//        return Util.getFacesBean(markupDesignBean) != null;
//    }
    
//    public boolean isSpecialBean(DesignBean designBean) {
//        return Util.isSpecialBean(designBean);
//    }

//    public boolean isTrayBean(DesignBean designBean) {
//        return LiveUnit.isTrayBean(designBean);
//    }

//    public boolean isCssPositionable(DesignBean designBean) {
//        return LiveUnit.isCssPositionable(designBean);
//    }
    
//    public Element getElement(DesignBean designBean) {
//        return Util.getElement(designBean);
//    }
    
    public Element getSourceElement(Element componentRootElement) {
        MarkupDesignBean markupDesignBean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
        return Util.getElement(markupDesignBean);
    }

//    public Element getMarkupBeanElement(DesignBean designBean) {
//        MarkupBean markupBean = Util.getMarkupBean(designBean);
//        return markupBean == null ? null : markupBean.getElement();
//    }

//    public boolean setDesignProperty(MarkupDesignBean markupDesignBean, String attribute, int value) {
//        return Util.setDesignProperty(markupDesignBean, attribute, value);
//    }
//    public boolean setStyleAttribute(Element componentRootElement, String attribute, int value) {
////        MarkupDesignBean markupDesignBean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
////        if (markupDesignBean == null) {
////            return false;
////        }
////        return Util.setDesignProperty(markupDesignBean, attribute, value);
//        return JsfSupportUtilities.setStyleAttribute(componentRootElement, attribute, value);
//    }

//    public Element findHtmlElementDescendant(DocumentFragment df) {
////        return Util.findDescendant(HtmlTag.HTML.name, df);
//        return JsfSupportUtilities.findHtmlElementDescendant(df);
//    }

//    public void updateLocalStyleValuesForElement(Element e, StyleData[] setStyleData, StyleData[] removeStyleData) {
////        Util.updateLocalStyleValuesForElement(e, setStyleData, removeStyleData);
//        JsfSupportUtilities.updateLocalStyleValuesForElement(e, setStyleData, removeStyleData);
//    }

//    public long getContextGenearation(DesignContext context) {
//        if (context instanceof LiveUnit) {
//            return ((LiveUnit)context).getContextGeneration();
//        }
//        return 0L;
//    }

//    public boolean isWebFormFileObject(FileObject fileObject) {
////        return FacesModel.getInstance(fileObject) != null;
//        return JsfSupportUtilities.isWebFormFileObject(fileObject);
//    }

    public boolean isFocusedElement(Element element) {
        if (element == null) {
            return false;
        }
        MarkupDesignBean markupDesignBean = MarkupUnit.getMarkupDesignBeanForElement(element);
        return isFocus(markupDesignBean);
    }
    
    /** XXX Moved from DesignerActions.
     * Returns whether or not this component is the initial focus.
     * @param bean The bean associated with the component
     * @return whether or not that component is the initial focus
     */
    private static boolean isFocus(DesignBean bean) {
        if (bean == null) {
            return false;
        }

        DesignBean body = getWebuiBody(bean);

        if (body == null) {
            return false;
        }

        DesignProperty prop = body.getProperty("focus");  // NOI18N

        if ((prop != null) && (prop.getValue() != null)) {
            // The property points to the client id, not the instance name!
            return prop.getValue().equals(getClientId(bean));
        } else {
            return false;
        }
    }

    // XXX Moved from DesignerActions.
    /** Find the Body component of the page containing the given bean, if any */
    private static DesignBean getWebuiBody(DesignBean bean) {
        DesignBean parent = bean.getBeanParent();

        while (parent != null) {
            if (parent.getInstance() instanceof com.sun.rave.web.ui.component.Body
            || parent.getInstance() instanceof com.sun.webui.jsf.component.Body) {
                return parent;
            }

            parent = parent.getBeanParent();
        }

        return null;
    }
    
    // XXX Moved from DesignerActions.
    /** Get the client id for the given DesignBean */
    private static String getClientId(DesignBean bean) {
        Object instance = bean.getInstance();

        if (!(instance instanceof UIComponent)) {
            return null;
        }

        UIComponent uic = (UIComponent)instance;
        DesignContext dcontext = bean.getDesignContext();
        FacesContext fcontext = ((FacesDesignContext)dcontext).getFacesContext();

        return uic.getClientId(fcontext);
    }

    public boolean ignoreDesignBorder(Element element) {
        if (element == null) {
            return true;
        }
        MarkupDesignBean bean = MarkupUnit.getMarkupDesignBeanForElement(element);
//        // Only add design borders for boxes that correspond to a live bean
//        // Gotta do something more here for markup beans
//        if ((bean == null) || ((
//            // Deal with the fact that we get design-ids repeated on children
//            // now, e.g. <table design-id="foo"><tr design-id="foo"> ....
//            // We only want the outer most box to have the design border
////            parent != null) && (parent.getDesignBean() == bean))) {
//            parent != null) && (getMarkupDesignBeanForCssBox(parent) == bean))) {
//            return;
//        }

        // Special case: the page separator shouldn't have a design border even
        // though it renders a block box and may have dimensions set on it!
        // TODO: Mark it horizontal resizable only!
        if (bean.getInstance() instanceof com.sun.rave.web.ui.component.PageSeparator
        || bean.getInstance() instanceof com.sun.webui.jsf.component.PageSeparator) {
            return true;
        }
        return false;
    }

    public Element getSourceElementWhichRendersChildren(Element element) {
        MarkupDesignBean bean = MarkupUnit.getMarkupDesignBeanForElement(element);
        if (bean != null) {
            MarkupDesignBean parent = findClosestRendersChildren(bean);
            if (parent != null) {
                return parent.getElement();
            }
        }
        return null;
    }
    
    // XXX Moved from FacesSupport. !! Changed
    /** Find closest (not the outermost as before) renders-children bean above the given bean, or
     * the bean itself if there is no such parent.
     */
//    private MarkupDesignBean findRendersChildren(MarkupDesignBean bean) {
    private MarkupDesignBean findClosestRendersChildren(MarkupDesignBean bean) {
        // Similar to FacesSupport.findHtmlContainer(bean), but
        // we need to return the outermost html container itself, not
        // the parent, since we're not looking for its container but
        // the bean to be moved itself.
        MarkupDesignBean curr = bean;

//        for (; curr != null; curr = FacesSupport.getBeanParent(curr)) {
        for (; curr != null; curr = getBeanParent(curr)) {
            if (curr.getInstance() instanceof F_Verbatim) {
                // If you have a verbatim, we're okay to add html comps below it
                return bean;
            }

            if (curr.getInstance() instanceof UIComponent) {
                // Need to set the Thread's context classloader to be the Project's ClassLoader.
            	ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
            	try {
//                    Thread.currentThread().setContextClassLoader(InSyncService.getProvider().getContextClassLoader(curr));
                    Thread.currentThread().setContextClassLoader(InSyncServiceProvider.get().getContextClassLoader(curr.getDesignContext()));
                    if (((UIComponent)curr.getInstance()).getRendersChildren()) {
                    	bean = curr;
//                        // Can't break here - there could be an outer
//                        // renders-children parent
                        // XXX #112580 Find the closest renders-children bean.
                        return bean;
                    }               
            	} finally {
                    Thread.currentThread().setContextClassLoader(oldContextClassLoader);
            	}                
            }
        }

        return bean;
    }
    
    // XXX Moved from FacesSupport.
    /**
     * Return the parent of the given markup design bean, if the parent is
     * a MarkupDesignBean.
     */
    private static MarkupDesignBean getBeanParent(MarkupDesignBean bean) {
        DesignBean parent = bean.getBeanParent();

        if (parent instanceof MarkupDesignBean) {
            return (MarkupDesignBean)parent;
        }

        return null;
    }

    public Element[] getChildComponentRootElements(Element componentRootElement) {
        DesignBean lb  = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
        if (lb == null) {
            return new Element[0];
        }
        List<Element> childComponentRootElements = new ArrayList<Element>();
        for (int i = 0, n = lb.getChildBeanCount(); i < n; i++) {
            DesignBean child = lb.getChildBean(i);
            if (child instanceof MarkupDesignBean) {
                Element sourceElement = ((MarkupDesignBean)child).getElement();
                Element childComponentRootElement = MarkupService.getRenderedElementForElement(sourceElement);
                if (childComponentRootElement != null) {
                    childComponentRootElements.add(childComponentRootElement);
                }
            }
        }
        return childComponentRootElements.toArray(new Element[childComponentRootElements.size()]);
    }

//    // XXX Hack.
//    public MarkupDesignBean adjustRenderBeanHack(MarkupDesignBean renderBean) {
//        // Handle hyperlinks. We really need to render its surrounding content
//        // to see the CS stylerules for <a> apply
//        if (renderBean.getInstance() instanceof HtmlOutputText) {
//            DesignBean parent = renderBean.getBeanParent();
//
//            if ((parent != null) && (parent.getChildBeanCount() == 1) &&
//                    (parent.getInstance() instanceof HtmlCommandLink ||
//                    parent.getInstance() instanceof HtmlOutputLink)) {
//                renderBean = (MarkupDesignBean)parent;
//            }
//        }
//        
//        // Embedded table portions (rowgroups, columns) aren't happy being rendered
//        // without their surrounding table.
//        // It would be better to modify the preview code to actually go and -try- rendering
//        // components and then progressively retry on parents until it succeeds.
//        // But given that the code is freezing today I'm playing it safe
//        if (renderBean.getInstance() instanceof com.sun.rave.web.ui.component.TableColumn
//        || renderBean.getInstance() instanceof com.sun.webui.jsf.component.TableColumn) {
//            if (renderBean.getBeanParent() instanceof MarkupDesignBean) {
//                renderBean = (MarkupDesignBean)renderBean.getBeanParent();
//            } else {
//                return null;
//            }
//        } else if (renderBean.getBeanParent().getInstance() instanceof com.sun.rave.web.ui.component.TableColumn
//        || renderBean.getBeanParent().getInstance() instanceof com.sun.webui.jsf.component.TableColumn) {
//            // We also have to render components that are children of a TableColumn as part of the whole
//            // table as well, because their value binding expressions can involve data providers set up
//            // by the table. This is clearly not a clean solution. See comment above about trying arbitary
//            // rendering instead. This breaks once you nest components in a column inside a container
//            // component for example. Just doing a low risk, 90% fix now right before FCS.
//            if (renderBean.getBeanParent().getBeanParent() instanceof MarkupDesignBean) {
//                renderBean = (MarkupDesignBean)renderBean.getBeanParent().getBeanParent();
//            } else {
//                return null;
//            }
//        }
//
//        // Not else: a TableColumn can be inside a TableRowGroup so keep moving outwards if necessary:
//        if (renderBean.getInstance() instanceof com.sun.rave.web.ui.component.TableRowGroup
//        || renderBean.getInstance() instanceof com.sun.webui.jsf.component.TableRowGroup) {
//            if (renderBean.getBeanParent() instanceof MarkupDesignBean) {
//                renderBean = (MarkupDesignBean)renderBean.getBeanParent();
//            } else {
//                return null;
//            }
//        }
//        return renderBean;
//    }

//    public boolean isFacesComponentBean(DesignBean bean) {
//        return bean.getInstance() instanceof UIComponent;
//    }

//    public boolean isEscapedDesignBean(DesignBean bean) {
//        // See if the bean looks like an output text that has escape
//        // turned off. If so, it's multiline. All others are considered
//        // single line.
//        if (bean.getInstance() instanceof HtmlOutputText) {
//            DesignProperty escape = bean.getProperty("escape"); // NOI18N
//
//            if (escape != null) {
//                Object o = escape.getValue();
//
//                if (o instanceof Boolean) {
//                    return ((Boolean)o).booleanValue();
//                }
//            }
//        }
//
//        return true;
//    }

    public boolean isFacesComponent(Element componentRootElement) {
        MarkupDesignBean bean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
        if (bean == null) {
            return false;
        }
        return Util.getFacesBean(bean) != null;
    }

//    public Element getRenderedElement(DesignBean designBean) {
//        return MarkupService.getRenderedElementForElement(Util.getElement(designBean));
//    }

    public String getRegionDisplayName(Element regionElement) {
        MarkupMouseRegion region = FacesPageUnit.getMarkupMouseRegionForElement(regionElement);
        return region == null ? null : region.getDisplayName();
    }

    public boolean isSameRegionOfElement(Element regionElement, Element element) {
        if (regionElement == null) {
            return false;
        }
        MarkupMouseRegion region = FacesPageUnit.getMarkupMouseRegionForElement(regionElement);
        if (region == null) {
            return false;
        }
        
        return region == FacesPageUnit.getMarkupMouseRegionForElement(element);
    }

    public Element getComponentRootElementForElement(Element element) {
//        return getComponentRootElementForDesignBean(MarkupUnit.getMarkupDesignBeanForElement(element));
        return JsfSupportUtilities.getComponentRootElementForElement(element);
    }
    
    /*public*/ private Element getComponentRootElementForMarkupDesignBean(MarkupDesignBean markupDesignBean) {
//        return getComponentRootElementForDesignBean(markupDesignBean);
        return JsfSupportUtilities.getComponentRootElementForDesignBean(markupDesignBean);
    }
    
//    public static Element getComponentRootElementForDesignBean(DesignBean designBean) {
//        if (designBean instanceof MarkupDesignBean) {
//            return DomProviderImpl.getComponentRootElementForMarkupDesignBean((MarkupDesignBean)designBean);
//        }
//        return null;
//    }

    public String getInstanceName(Element componentRootElement) {
        MarkupDesignBean markupDesignBean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
        return markupDesignBean == null ? null : markupDesignBean.getInstanceName();
    }

    public boolean isIncludeComponentBox(Element componentRootElement) {
        DesignBean bean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
        if ((bean != null) && bean.getInstance() instanceof org.netbeans.modules.visualweb.xhtml.Div) {
            if ((bean.getChildBeanCount() == 1)
            && bean.getChildBean(0).getInstance() instanceof org.netbeans.modules.visualweb.xhtml.Jsp_Directive_Include) {
                return true;
            }
        }
        return false;
    }

    public boolean isSpecialComponent(Element componentRootElement) {
//        MarkupDesignBean markupDesignBean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
//        if (markupDesignBean == null) {
//            return false;
//        }
//        return Util.isSpecialBean(markupDesignBean);
        return JsfSupportUtilities.isSpecialComponent(componentRootElement);
    }

    public boolean isTrayComponent(Element componentRootElement) {
        MarkupDesignBean markupDesignBean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
        if (markupDesignBean == null) {
            return false;
        }
        return LiveUnit.isTrayBean(markupDesignBean);
    }

    public boolean isCssPositionable(Element componentRootElement) {
        MarkupDesignBean markupDesignBean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
        if (markupDesignBean == null) {
            return false;
        }
        return LiveUnit.isCssPositionable(markupDesignBean);
    }

    public boolean isEscapedComponent(Element componentRootElement) {
        DesignBean bean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
        if (bean == null) {
            return true;
        }
        return isEscapedDesignBean(bean);
    }
    
    static boolean isEscapedDesignBean(DesignBean bean) {
        // See if the bean looks like an output text that has escape
        // turned off. If so, it's multiline. All others are considered
        // single line.
        if (bean.getInstance() instanceof HtmlOutputText) {
            DesignProperty escape = bean.getProperty("escape"); // NOI18N

            if (escape != null) {
                Object o = escape.getValue();

                if (o instanceof Boolean) {
                    return ((Boolean)o).booleanValue();
                }
            }
        }

        return true;
    }

    public Element getParentComponent(Element componentRootElement) {
//        MarkupDesignBean markupDesignBean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
//        if (markupDesignBean == null) {
//            return null;
//        }
//
//        DesignBean parent = markupDesignBean.getBeanParent();
//        return parent instanceof MarkupDesignBean ? getComponentRootElementForMarkupDesignBean((MarkupDesignBean)parent) : null;
        return JsfSupportUtilities.getParentComponent(componentRootElement);
    }

    public Element[] getChildComponents(Element componentRootElement) {
        MarkupDesignBean markupDesignBean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
        if (markupDesignBean == null) {
            return new Element[0];
        }
        
        List<Element> children = new ArrayList<Element>();
        for (int i = 0; i < markupDesignBean.getChildBeanCount(); i++) {
            DesignBean childBean = markupDesignBean.getChildBean(i);
            if (childBean instanceof MarkupDesignBean) {
                Element child = getComponentRootElementForMarkupDesignBean((MarkupDesignBean)childBean);
                if (child != null) {
                    children.add(child);
                }
            }
        }

        return children.toArray(new Element[children.size()]);
    }

    public boolean isContainerComponent(Element componentRootElement) {
        MarkupDesignBean markupDesignBean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
        if (markupDesignBean == null) {
            return false;
        }
        
        BeanInfo bi = markupDesignBean.getBeanInfo();
        if (bi != null) {
            BeanDescriptor bd = bi.getBeanDescriptor();
            Object o = bd.getValue(Constants.BeanDescriptor.IS_CONTAINER);
            boolean notContainer = o == Boolean.FALSE;
            return !notContainer;
        }
        return true;
    }
    
    public boolean isContainerTypeComponent(Element componentRootElement) {
        MarkupDesignBean markupDesignBean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
        if (markupDesignBean == null) {
            return false;
        }
        
        return markupDesignBean.isContainer();
    }
    
//    /**
//     * If the given component supports inline text editing, return the
//     * String property name which stores the text that is inline
//     * editable.
//     */
//    public String[] getEditablePropertyNames(Element componentRootElement) {
//        DesignBean bean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
//        return getEditablePropertyNames(bean);
//    }
    
    public static String[] getEditablePropertyNames(DesignBean bean) {
        if (bean == null) {
            return new String[0];
        }
        
//        BeanInfo bi = bean.getBeanInfo();
//
//        if (bi != null) {
//            BeanDescriptor bd = bi.getBeanDescriptor();
//            Object o = bd.getValue(Constants.BeanDescriptor.INLINE_EDITABLE_PROPERTIES);
//
//            if (o instanceof String[]) {
//                String[] source = (String[])o;
        String[] source = getEditableProperties(bean);
        if (source != null) {
            List<String> names = new ArrayList<String>(source.length);

            for (int i = 0; i < source.length; i++) {
                String name;
                int index = source[i].indexOf(':');

                if (index == -1) {
                    if ((source.length > 0) && (source[i].charAt(0) == '*')) {
                        name = source[i].substring(1);
                    } else {
                        name = source[i];
                    }
                } else {
                    int start = 0;

                    if ((source.length > 0) && (source[i].charAt(0) == '*')) {
                        start = 1;
                    }

                    name = source[i].substring(start, index);
                }

                DesignProperty property = bean.getProperty(name);

                if ((property != null) && isEditingAllowed(property)) {
                    names.add(name);
                }
            }

            return names.toArray(new String[names.size()]);
        }
//        }

        return new String[0];
    }

    public String[] getEditableProperties(Element componentRootElement) {
        MarkupDesignBean markupDesignBean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
        if (markupDesignBean == null) {
            return new String[0];
        }
        return getEditableProperties(markupDesignBean);
    }
    
    private static String[] getEditableProperties(DesignBean bean) {
        BeanInfo bi = bean.getBeanInfo();

        if (bi != null) {
            BeanDescriptor bd = bi.getBeanDescriptor();
            Object o = bd.getValue(Constants.BeanDescriptor.INLINE_EDITABLE_PROPERTIES);

            if (o instanceof String[]) {
                return (String[])o;
            }
        }

        return null;
    }

    /**
     * Return true if inline editing is allowed for the given property (assuming it
     * has already been marked via metadata for inline editing; -that- is not checked here)
     */
    static boolean isEditingAllowed(DesignProperty property) {
        // TODO: Change types above from DesignProperty to FacesDesignProperty, and
        // call property.isBound() instead of the below!
        if (NO_EDIT_VB_EXPR) {
            String value = property.getValueSource();

            // TODO: Change types above from DesignProperty to FacesDesignProperty, and
            // call property.isBound() instead of the below!
//            if ((value != null) && FacesSupport.isValueBindingExpression(value, false)) {
            if ((value != null) && DesignBeanNode.isValueBindingExpression(value, false)) {
                return false;
            }
        }

        return true;
    }

    static String getSpecialInitValue(DesignProperty designProperty) {
        if (!NO_EDIT_VB_EXPR) {
            String value = designProperty.getValueSource();

//            if ((value != null) && FacesSupport.isValueBindingExpression(value, false)) {
            if ((value != null) && DesignBeanNode.isValueBindingExpression(value, false)) {
                return value;
            }
        }

        return null;
    }

    public ResizeConstraint[] getResizeConstraintsForComponent(Element componentRootElement) {
        MarkupDesignBean bean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
        int constraints = getResizeConstraintsForMarkupDesignBean(bean);
        
        if (constraints == Constants.ResizeConstraints.NONE) {
            return new ResizeConstraint[0];
        }
        
        List<ResizeConstraint> resizeConstraints = new ArrayList<ResizeConstraint>();
        if ((constraints & Constants.ResizeConstraints.TOP) != 0) {
            resizeConstraints.add(ResizeConstraint.TOP);
        }
        if ((constraints & Constants.ResizeConstraints.LEFT) != 0) {
            resizeConstraints.add(ResizeConstraint.LEFT);
        }
        if ((constraints & Constants.ResizeConstraints.BOTTOM) != 0) {
            resizeConstraints.add(ResizeConstraint.BOTTOM);
        }
        if ((constraints & Constants.ResizeConstraints.RIGHT) != 0) {
            resizeConstraints.add(ResizeConstraint.RIGHT);
        }
        if ((constraints & Constants.ResizeConstraints.MAINTAIN_ASPECT_RATIO) != 0) {
            resizeConstraints.add(ResizeConstraint.MAINTAIN_ASPECT_RATIO);
        }
        if ((constraints & Constants.ResizeConstraints.VERTICAL) != 0) {
            resizeConstraints.add(ResizeConstraint.VERTICAL);
        }
        if ((constraints & Constants.ResizeConstraints.HORIZONTAL) != 0) {
            resizeConstraints.add(ResizeConstraint.HORIZONTAL);
        }
        if ((constraints & Constants.ResizeConstraints.ANY) != 0) {
            resizeConstraints.add(ResizeConstraint.ANY);
        }

        return resizeConstraints.toArray(new ResizeConstraint[resizeConstraints.size()]);
    }
    
    private static int getResizeConstraintsForMarkupDesignBean(MarkupDesignBean bean) {
        int constraints = Constants.ResizeConstraints.ANY;
        if (bean == null) {
            return constraints;
        }

        // Special case: The Jsp Include box is not resizable.
        // If I build a BeanDescriptor for it I can inject
        // this value right on it, but I also want to make it
        // as NOT POSITIONABLE.
        if (bean.getInstance() instanceof Jsp_Directive_Include) {
            return Constants.ResizeConstraints.NONE;
        }

        BeanInfo bi = bean.getBeanInfo();
        if (bi != null) {
            BeanDescriptor bd = bi.getBeanDescriptor();
            Object o = bd.getValue(Constants.BeanDescriptor.RESIZE_CONSTRAINTS);

            if ((o != null) && o instanceof Integer) {
                constraints = ((Integer)o).intValue();
            }
        }

        return constraints;
    }

    public boolean isRootContainerComponent(Element componentRootElement) {
        MarkupDesignBean markupDesignBean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
        if (markupDesignBean == null) {
            return false;
        }
        
        DesignContext designContext = markupDesignBean.getDesignContext();
        if (designContext == null) {
            return false;
        }
        
        return markupDesignBean == designContext.getRootContainer();
    }

    public boolean hasDefaultProperty(Element componentRootElement) {
        MarkupDesignBean lb = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
        BeanInfo bi = lb.getBeanInfo();
        if (bi != null) {
            int defaultProp = bi.getDefaultPropertyIndex();

            if (defaultProp != -1) {
                return true;
            }
        }
        return false;
    }

    public boolean focusDefaultProperty(Element componentRootElement, final String content) {
        MarkupDesignBean lb = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
        BeanInfo bi = lb.getBeanInfo();
        int defaultProp;
        if (bi != null) {
            defaultProp = bi.getDefaultPropertyIndex();
            if (defaultProp == -1) {
                return false;
            }
        } else {
            return false;
        }
        
        FeatureDescriptor defProp = bi.getPropertyDescriptors()[defaultProp];

        // How do we launch the property sheet editing a
        // particular property?
        final JTable jt =
//                            org.netbeans.modules.visualweb.designer.DesignerUtils.findPropSheetTable(true, true);
                findPropSheetTable(true, true);

        if (jt == null) {
            return false;
        }

        TableModel model = jt.getModel();

        // Set focus of jt?
        for (int row = 0, n = model.getRowCount(); row < n; row++) {
            Object o = model.getValueAt(row, 0);

            if (!(o instanceof FeatureDescriptor)) {
                continue;
            }

            FeatureDescriptor desc = (FeatureDescriptor)o;

            if (defProp.getName().equals(desc.getName())) {
                // Edit the cell XXX only if readonly!
                if (desc instanceof org.openide.nodes.Node.Property) {
                    org.openide.nodes.Node.Property prop = (org.openide.nodes.Node.Property)desc;

                    if (!prop.canWrite()) {
                        return false;
                    }
                }

                final int r = row;
//                final String content = event.getActionCommand();
                SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            jt.editCellAt(r, 1, null);
                            jt.requestFocus();

                            Object ce = jt.getCellEditor(r, 1);

                            // Hack Alert: try to transfer the
                            // original keypress into the text field
                            Component comp =
                                getInplaceEditorComponentForSheetCellEditor(ce);

                            if (comp instanceof javax.swing.text.JTextComponent) {
                                javax.swing.text.JTextComponent jtc =
                                    (javax.swing.text.JTextComponent)comp;
                                jtc.replaceSelection(content);
                            }
                        }
                    });

                return true;
            }
        }
        return false;
    }
    
    // XXX Moved from DesignerUtils.
    /** Locate the JTable within the property sheet in the IDE.
     * WARNING: Implementation hacks!
     * @param focus If set, focus the top component
     * @param visible If set, ensure the top component is fronted
     */
    private static JTable findPropSheetTable(boolean focus, boolean visible) {
        WindowManager mgr = WindowManager.getDefault();
        TopComponent properties = mgr.findTopComponent("properties"); // NOI18N
        
        if ((properties != null) && (visible || properties.isShowing())) {
            if (focus) {
                properties.requestActive();
            }
            
            if (visible) {
                properties.requestVisible();
            }
            
            return findTable(properties);
        }
        
        return null;
    }

    /** Fish the given Container hierarchy for a JTable */
    private static JTable findTable(Container c) {
//        if(DEBUG) {
//            debugLog(DesignerUtils.class.getName() + ".findTable(Container)");
//        }
        if(c == null) {
            return(null);
        }
        if (c instanceof JTable) {
            return (JTable)c;
        }
        
        int n = c.getComponentCount();
        
        for (int i = 0; i < n; i++) {
            Component comp = c.getComponent(i);
            
            if (comp instanceof JTable) {
                return (JTable)comp;
            }
            
            if (comp instanceof Container) {
                JTable table = findTable((Container)comp);
                
                if (table != null) {
                    return table;
                }
            }
        }
        
        return null;
    }

    // XXX Using reflection, But it is still better than changing NB code
    // The task from UI point of view looks very strange... why the text isn't inserted into the component, as user expect,
    // but surprisinlgy the focus is moved into property sheet? That kind of solutions cause problems like this.
    private static Component getInplaceEditorComponentForSheetCellEditor(Object ce) {
        if (ce == null) {
            return null;
        }

        Object inplaceEditor;

        try {
            ClassLoader cl =
                org.openide.explorer.propertysheet.PropertySheet.class.getClassLoader();
            Class sheetCellEditorClass =
                Class.forName("org.openide.explorer.propertysheet.SheetCellEditor", true, cl); // NOI18N
            java.lang.reflect.Method getInplaceEditorMethod =
                sheetCellEditorClass.getDeclaredMethod("getInplaceEditor", new Class[0]); // NOI18N
            getInplaceEditorMethod.setAccessible(true);
            inplaceEditor = getInplaceEditorMethod.invoke(ce, new Object[0]);
        } catch (ClassNotFoundException cnfe) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, cnfe);
            inplaceEditor = null;
        } catch (NoSuchMethodException nsme) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, nsme);
            inplaceEditor = null;
        } catch (IllegalAccessException iae) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, iae);
            inplaceEditor = null;
        } catch (java.lang.reflect.InvocationTargetException ite) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ite);
            inplaceEditor = null;
        }

        if (inplaceEditor instanceof org.openide.explorer.propertysheet.InplaceEditor) {
            return ((org.openide.explorer.propertysheet.InplaceEditor)inplaceEditor).getComponent();
        } else {
            return null;
        }
    }

    public Image getIcon(Element componentRootElement) {
        MarkupDesignBean bean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
        if (bean == null) {
            return null;
        }
        
        BeanInfo bi = bean.getBeanInfo();
        if (bi != null) {
            return bi.getIcon(BeanInfo.ICON_COLOR_16x16);
        }
        return null;
    }

//    public org.openide.nodes.Node getNodeRepresentation(Element componentRootElement) {
////        MarkupDesignBean markupDesignBean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
////        return DesigntimeIdeBridgeProvider.getDefault().getNodeRepresentation(markupDesignBean);
//        return JsfSupportUtilities.getNodeRepresentation(componentRootElement);
//    }

    public Element getComponentRootElementFromNode(org.openide.nodes.Node node) {
//        DesignBean bean = (DesignBean)node.getLookup().lookup(DesignBean.class);
//        if (bean == null) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
//                new NullPointerException("No DesignBean for node=" + node)); // NOI18N
//            return null;
//        }
//
//        return bean instanceof MarkupDesignBean ? getComponentRootElementForMarkupDesignBean((MarkupDesignBean)bean) : null;
        return JsfSupportUtilities.getComponentRootElementFromNode(node);
    }

//    public MarkupPosition getDefaultMarkupPositionUnderParent(DesignBean parent) {
//        return parent instanceof MarkupDesignBean ? FacesDndSupport.getDefaultMarkupPositionUnderParent(parent) : null;
//    }

//    public DomProvider.Location computeLocationForPositions(String facet, Point canvasPos, Node documentPosNode, int documentPosOffset, Dimension dimension, boolean isGrid, Element droppeeElement, Element dropeeComponentRootElement, Element defaultParentComponentRootElement) {
//        MarkupDesignBean droppeeBean = MarkupUnit.getMarkupDesignBeanForElement(dropeeComponentRootElement);
//        MarkupDesignBean defaultParentBean = MarkupUnit.getMarkupDesignBeanForElement(defaultParentComponentRootElement);
//        FacesDndSupport.Location location = FacesDndSupport.computeLocationForPositions(facet, canvasPos, documentPosNode, documentPosOffset, dimension, isGrid, droppeeElement, droppeeBean, defaultParentBean);
//        DomProvider.Location l = new DomProvider.Location();
//        l.coordinates = location.getCoordinates();
//        l.droppee = location.getDroppee();
//        l.droppeeElement = location.getDroppeeElement();
//        l.facet = location.getFacet();
//        l.pos = location.getPos();
//        l.size = location.getSize();
//        return l;
//    }

    
    private static final Map<MarkupDesignBean, MarkupTableDesignInfo> markupDesignBean2markupTableDesignInfo = new WeakHashMap<MarkupDesignBean, MarkupTableDesignInfo>();
    
    private static MarkupTableDesignInfo getMarkupTableDesignInfo(MarkupDesignBean markupDesignBean) {
        if (markupDesignBean == null) {
            return null;
        }

        MarkupTableDesignInfo markupTableDesignInfo;
        synchronized (markupDesignBean2markupTableDesignInfo) {
            markupTableDesignInfo = markupDesignBean2markupTableDesignInfo.get(markupDesignBean);
        }
        if (markupTableDesignInfo != null) {
            return markupTableDesignInfo;
        }
        
        DesignInfo info = markupDesignBean.getDesignInfo();
        if (info instanceof MarkupTableDesignInfo) {
            markupTableDesignInfo = (MarkupTableDesignInfo)info;
        }
        if (markupTableDesignInfo == null) {
            return null;
        }

        synchronized (markupDesignBean2markupTableDesignInfo) {
            markupDesignBean2markupTableDesignInfo.put(markupDesignBean, markupTableDesignInfo);
        }
        return markupTableDesignInfo;
    }
    
    public boolean hasTableResizeSupport(Element tableComponentRootElement) {
        MarkupDesignBean markupDesignBean = MarkupUnit.getMarkupDesignBeanForElement(tableComponentRootElement);
        return getMarkupTableDesignInfo(markupDesignBean) != null;
    }

    public int testResizeColumn(Element tableComponentRootElement, int row, int column, int width) {
        MarkupDesignBean markupDesignBean = MarkupUnit.getMarkupDesignBeanForElement(tableComponentRootElement);
        MarkupTableDesignInfo markupTableDesignInfo = getMarkupTableDesignInfo(markupDesignBean);
        if (markupTableDesignInfo == null) {
            return 0;
        }
        
        return markupTableDesignInfo.testResizeColumn(markupDesignBean, row, column, width);
    }

    public int testResizeRow(Element tableComponentRootElement, int row, int column, int height) {
        MarkupDesignBean markupDesignBean = MarkupUnit.getMarkupDesignBeanForElement(tableComponentRootElement);
        MarkupTableDesignInfo markupTableDesignInfo = getMarkupTableDesignInfo(markupDesignBean);
        if (markupTableDesignInfo == null) {
            return 0;
        }
        
        return markupTableDesignInfo.testResizeRow(markupDesignBean, row, column, height);
    }

    public void resizeColumn(Element tableComponentRootElement, int column, int width) {
        MarkupDesignBean markupDesignBean = MarkupUnit.getMarkupDesignBeanForElement(tableComponentRootElement);
        MarkupTableDesignInfo markupTableDesignInfo = getMarkupTableDesignInfo(markupDesignBean);
        if (markupTableDesignInfo == null) {
            return;
        }
        
        markupTableDesignInfo.resizeColumn(markupDesignBean, column, width);
    }

    public void resizeRow(Element tableComponentRootElement, int row, int height) {
        MarkupDesignBean markupDesignBean = MarkupUnit.getMarkupDesignBeanForElement(tableComponentRootElement);
        MarkupTableDesignInfo markupTableDesignInfo = getMarkupTableDesignInfo(markupDesignBean);
        if (markupTableDesignInfo == null) {
            return;
        }
        
        markupTableDesignInfo.resizeRow(markupDesignBean, row, height);
    }

}
