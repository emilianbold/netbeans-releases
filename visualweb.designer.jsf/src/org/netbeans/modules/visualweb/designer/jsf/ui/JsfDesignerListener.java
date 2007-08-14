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


package org.netbeans.modules.visualweb.designer.jsf.ui;


import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.markup.MarkupMouseRegion;
import java.awt.Component;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.modules.visualweb.api.designer.Designer;
import org.netbeans.modules.visualweb.api.designer.Designer.Box;
import org.netbeans.modules.visualweb.api.designer.Designer.DesignerClickEvent;
import org.netbeans.modules.visualweb.api.designer.Designer.DesignerEvent;
import org.netbeans.modules.visualweb.api.designer.Designer.DesignerListener;
import org.netbeans.modules.visualweb.api.designer.Designer.DesignerPopupEvent;
import org.netbeans.modules.visualweb.api.designer.Designer.ExternalBox;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;
import org.netbeans.modules.visualweb.designer.jsf.JsfForm;
import org.netbeans.modules.visualweb.designer.jsf.JsfSupportUtilities;
import org.netbeans.modules.visualweb.insync.faces.FacesPageUnit;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.w3c.dom.Element;


/**
 *
 * @author Peter Zavadsky
 */
class JsfDesignerListener implements DesignerListener {

    private final JsfTopComponent jsfTopComponent;
    
    public JsfDesignerListener(JsfTopComponent jsfTopComponent) {
        this.jsfTopComponent = jsfTopComponent;
    }

    /** The user double clicked on the component during -initial- inline editing
     * (e.g. we entered inline editing as part of the first click in a double
     * click) so cancel inline editing and process the double click in the normal
     * way: as a request to open the default event handler. However if there is
     * no default event handler, stay in inline edit mode. */
    public void userActionPerformed(DesignerEvent evt) {
//        if (webform.getActions().handleDoubleClick(true)) {
//        if (handleDoubleClick(true)) {
//        if (handleDoubleClick()) {
//            finishInlineEditing(true);
//        }
        if (handleUserAction()) {
            jsfTopComponent.getDesigner().finishInlineEditing(true);
        }
    }
    
    // XXX Moved from DesignerActions.
    /** Perform the equivalent of a double click on the first item
     * in the selection (if any).
     * @param selOnly If true, only do something if the selection is nonempty
     * @todo Rename to handleDefaultAction
     * @return True iff the double click resulted in opening an event handler
     */
//    public boolean handleDoubleClick(/*boolean selOnly*/) {
    private boolean handleUserAction() {
//        CssBox box = null;
        Box box = null;
        
//        SelectionManager sm = webform.getSelection();
//        if (!sm.isSelectionEmpty()) {
        if (jsfTopComponent.getDesigner().getSelectedCount() > 0) {
//            Iterator it = sm.iterator();
//
//            while (it.hasNext()) {
//                DesignBean bean = (DesignBean)it.next();
//            for (Element componentRootElement : sm.getSelectedComponentRootElements()) {
            for (Element componentRootElement : jsfTopComponent.getDesigner().getSelectedComponents()) {
//                DesignBean bean = WebForm.getDomProviderService().getMarkupDesignBeanForElement(componentRootElement);
//                if (bean != null) {
                if (componentRootElement != null) {
//                    box = webform.getMapper().findBox(bean);
//                    box = ModelViewMapper.findBoxForComponentRootElement(webform.getPane().getPageBox(), componentRootElement);
                    box = jsfTopComponent.getDesigner().findBoxForComponentRootElement(componentRootElement);
                    if (box != null) {
                        break;
                    }
                }
            }
//        } else if (selOnly) {
        }
//        else {
//            return false;
//        }

//        return handleDoubleClick(box);
        return handleUserAction(box);
    }

    // XXX Moved from DesignerActions.
    /** Handle double clicks with the given box as the target.
     * @todo Rename to handleDefaultAction
     * @return Return true iff the double click resulted in opening an event handler
     */
//    private boolean handleDoubleClick(CssBox box) {
    private boolean handleUserAction(Box box) {
//        if (box instanceof ExternalDocumentBox) {
//            ((ExternalDocumentBox)box).open();
        if (box instanceof ExternalBox) {
//            ((ExternalDocumentBox)box).open();
            openExternalBox((ExternalBox)box);
            return false;
//        } else if ((box != null) && (box.getTag() == HtmlTag.DIV) && (box.getBoxCount() == 1) &&
//                box.getBox(0) instanceof ExternalDocumentBox) {
        // XXX This is a hack.
        } else if ((box != null) && (box.getTag() == HtmlTag.DIV)) {
            Box[] children = box.getChildren();
            if (children.length == 1 && children[0] instanceof ExternalBox) {
            // IF user has clicked on a large div containing only a
            // jsp include, treat this as an attempt to open the page
            // fragment child.
//            ((ExternalDocumentBox)box.getBox(0)).open();
                openExternalBox((ExternalBox)children[0]);
                return false;
            }
        }
        
        return editEventHandler();
    }
    
    // XXX Copied from DesignerActions.
    /** Return true iff an event handler was found and created/opened */
    private boolean editEventHandler() {
//        SelectionManager sm = webform.getSelection();
//
//        if (sm.isSelectionEmpty()) {
//            webform.getModel().openDefaultHandler();
//
//            return false;
//        }
//
//        // TODO - get the component under the mouse, not the
//        // whole selection!
//        DesignBean component = getDefaultSelectionBean();
//
//        if (component != null) {
//            // See if it's an XHTML element; if so just show it in
//            // the JSP source
////            if (FacesSupport.isXhtmlComponent(component)) {
//            if (isXhtmlComponent(component)) {
////                MarkupBean mb = FacesSupport.getMarkupBean(component);
//                MarkupBean mb = Util.getMarkupBean(component);
//                
//                MarkupUnit unit = webform.getMarkup();
//                // <markup_separation>
////                Util.show(null, unit.getFileObject(),
////                    unit.computeLine((RaveElement)mb.getElement()), 0, true);
//                // ====
////                MarkupService.show(unit.getFileObject(), unit.computeLine((RaveElement)mb.getElement()), 0, true);
//                showLineAt(unit.getFileObject(), unit.computeLine(mb.getElement()), 0);
//                // </markup_separation>
//            } else {
//                webform.getModel().openDefaultHandler(component);
//            }
//
//            return true;
//        }
//
//        return false;
//        SelectionManager sm = webform.getSelection();
////        DesignBean component;
        Element componentRootElement;
//        if (sm.isSelectionEmpty()) {
        if (jsfTopComponent.getDesigner().getSelectedCount() == 0) {
//            webform.getModel().openDefaultHandler();
//
//            return false;
//            component = null;
            componentRootElement = null;
        } else {
//            component = getDefaultSelectionBean();
//            Element componentRootElement = getDefaultSelectionComponentRootElement();
//            component = WebForm.getDomProviderService().getMarkupDesignBeanForElement(componentRootElement);
            componentRootElement = getDefaultSelectionComponentRootElement();
        }

//        return webform.editEventHandlerForDesignBean(component);
//        return webform.editEventHandlerForComponent(componentRootElement);
        return jsfTopComponent.getJsfForm().editEventHandlerForComponent(componentRootElement);
    }

    // XXX Copy also in designer/../InteractionManager
//    private DesignBean getDefaultSelectionBean() {
    private Element getDefaultSelectionComponentRootElement() {
//        // TODO - should this be a checkbox instead?
//        SelectionManager sm = webform.getSelection();
////        DesignBean bean = sm.getPrimary();
////
////        if ((bean == null) && !sm.isSelectionEmpty()) {
//        Element primaryComponentRootElement = sm.getPrimary();
        Element primaryComponentRootElement = jsfTopComponent.getDesigner().getPrimarySelection();
//        DesignBean bean = WebForm.getDomProviderService().getMarkupDesignBeanForElement(primaryComponentRootElement);
//        if (primaryComponentRootElement == null && !sm.isSelectionEmpty()) {
        if (primaryComponentRootElement == null && jsfTopComponent.getDesigner().getSelectedCount() > 0) {
            // TODO - get the component under the mouse, not the
            // whole selection!
//            Iterator it = sm.iterator();
//
//            while (it.hasNext()) {
//                bean = (DesignBean)it.next();
//            for (Element componentRootElement : sm.getSelectedComponentRootElements()) {
            for (Element componentRootElement : jsfTopComponent.getDesigner().getSelectedComponents()) {
//                bean = WebForm.getDomProviderService().getMarkupDesignBeanForElement(componentRootElement);
                primaryComponentRootElement = componentRootElement;
//                if (bean != null) {
                if (primaryComponentRootElement != null) {
                    break;
                }
            }
        }

//        return bean;
        return primaryComponentRootElement;
    }
    
    private static void openExternalBox(ExternalBox externalBox) {
        JsfForm externalJsfForm = JsfForm.findJsfFormForDomProvider(externalBox.getExternalDomProvider());
//        if (frameForm == null) {
        if (externalJsfForm == null) {
            java.awt.Toolkit.getDefaultToolkit().beep();

            return;
        }

//        DataObject dobj = frameForm.getDataObject();
        DataObject dobj = externalJsfForm.getJspDataObject();
        if (dobj == null) {
            // #107543 The data object is missing. Notify user?
            return;
        }
        
        OpenCookie oc = (OpenCookie)dobj.getCookie(OpenCookie.class);

        if (oc != null) {
            oc.open();
        }
    }

    public void selectionChanged(DesignerEvent evt) {
        Designer designer = evt.getDesigner();
        Element[] selectedComponents = designer.getSelectedComponents();
        
        if (selectedComponents.length > 0) {
            List<Node> nodes = new ArrayList<Node>(selectedComponents.length);
            for (Element selectedComponent : selectedComponents) {
                Node n = JsfSupportUtilities.getNodeRepresentation(selectedComponent);
                nodes.add(n);
            }

            Node[] nds = nodes.toArray(new Node[nodes.size()]);
            jsfTopComponent.setActivatedNodes(nds);
        } else {
            Node[] nodes;
            Node rootNode = jsfTopComponent.getJsfForm().getRootBeanNode();
            nodes = rootNode == null ? new Node[0] : new Node[] {rootNode};
            
            jsfTopComponent.setActivatedNodes(nodes);
        }
    }

    public void userPopupActionPerformed(DesignerPopupEvent evt) {
        Component component = evt.getComponent();
        int x = evt.getX();
        int y = evt.getY();
        Point point = SwingUtilities.convertPoint(component, x, y, jsfTopComponent);
        jsfTopComponent.showPopup(evt.getActions(), evt.getContext(), point.x, point.y);
    }

    public void userElementClicked(DesignerClickEvent evt) {
        MarkupMouseRegion region = findRegion(evt.getBox().getElement());

        if ((region != null) && region.isClickable()) {
            Result r = region.regionClicked(evt.getClickCount());
//            ResultHandler.handleResult(r, getFacesModel());
            jsfTopComponent.getJsfForm().handleResult(r);
            // #6353410 If there was performed click on the region
            // then do not perform other actions on the same click.
//            return true;
            evt.consume();
        }
//        return false;
    }
    
    /** Locate the closest mouse region to the given element */
    private static MarkupMouseRegion findRegion(Element element) {
        while (element != null) {
//            if (element.getMarkupMouseRegion() != null) {
//                return element.getMarkupMouseRegion();
//            }
//            MarkupMouseRegion region = InSyncService.getProvider().getMarkupMouseRegionForElement(element);
            MarkupMouseRegion region = FacesPageUnit.getMarkupMouseRegionForElement(element);
            if (region != null) {
                return region;
            }

            if (element.getParentNode() instanceof Element) {
                element = (Element)element.getParentNode();
            } else {
                break;
            }
        }

        return null;
    }
    
}
