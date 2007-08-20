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
package org.netbeans.modules.visualweb.designer;

import org.netbeans.modules.visualweb.api.designer.Designer;
import org.netbeans.modules.visualweb.api.designer.Designer.Box;
import org.netbeans.modules.visualweb.api.designer.DomProvider;
import org.netbeans.modules.visualweb.api.designer.DomProvider.DomPosition;
import org.netbeans.modules.visualweb.api.designer.DomProvider.DomPosition.Bias;
import org.netbeans.modules.visualweb.api.designer.markup.MarkupService;
import org.netbeans.modules.visualweb.css2.ExternalDocumentBox;
import org.netbeans.modules.visualweb.spi.designer.Decoration;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JPopupMenu;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.netbeans.modules.visualweb.api.designer.Designer.DesignerClickEvent;
import org.netbeans.modules.visualweb.api.designer.Designer.DesignerEvent;
import org.netbeans.modules.visualweb.api.designer.Designer.DesignerPopupEvent;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssValue;
import org.netbeans.modules.visualweb.api.designer.cssengine.XhtmlCss;
import org.netbeans.spi.palette.PaletteController;

import org.openide.ErrorManager;
import org.openide.awt.MouseUtils;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;
import org.w3c.dom.Element;

import org.netbeans.modules.visualweb.css2.BoxType;
import org.netbeans.modules.visualweb.css2.ContainerBox;
import org.netbeans.modules.visualweb.css2.CssBox;
import org.netbeans.modules.visualweb.css2.DomInspector;
import org.netbeans.modules.visualweb.css2.LineBox;
import org.netbeans.modules.visualweb.css2.LineBoxGroup;
import org.netbeans.modules.visualweb.css2.ModelViewMapper;
import org.netbeans.modules.visualweb.css2.PageBox;
import org.netbeans.modules.visualweb.css2.SpaceBox;
import org.netbeans.modules.visualweb.css2.TextBox;

import org.netbeans.modules.visualweb.designer.WebForm.DefaultDesignerEvent;


// For CVS archaeology: Most of the code in this file used to be in SelectionManager.java
import org.w3c.dom.Text;


// For CVS archaeology: Most of the code in this file used to be in SelectionManager.java


// For CVS archaeology: Most of the code in this file used to be in SelectionManager.java


// For CVS archaeology: Most of the code in this file used to be in SelectionManager.java


// For CVS archaeology: Most of the code in this file used to be in SelectionManager.java

/**
 * This class manages the interactions in the designer surface:
 * mouse gestures, keyboard gestures, and sequence operations like
 * drags, moves, resizes, etc.
 *
 * @author Tor Norbye
 */
public class InteractionManager {

    // XXX Moved from former MouseUtils_RAVE. What is this?
    private static int DOUBLE_CLICK_DELTA = 300;

    // Log info pertaining to selection drawing
    public static final boolean ENABLE_DOM_INSPECTOR = true; // DOM inspector

    // No cursor feedback - used to avoid triggering
    // box-search related methods during development
    private static final boolean SKIP_MOUSE_MOTION = false;
    private final WebForm webform;
    private InlineEditor inlineEditor; // TODO - make this an Interaction as well
    private Interaction interaction;

    // XXX Should the insert mode box logic reside within the PageBox?
    private CssBox insertModeBox = null;
    private CssBox selectedBox = null;
//    private DesignBean highlighted = null;
    private Element highlightedComponentRootElement;
//    private MarkupMouseRegion highlightedRegion = null;
    private Element highlightedRegionElement;

    /** Mouse handler tracking mouse motion, clicks, dragging, etc. */
    private MouseHandler mouseHandler;
    private Marquee sizer;
    private Dragger dragger;
    private Cursor insertCursor = null;
    private Cursor linkedCursor = null;

    /** Creates a new instance of InteractionManager */
    public InteractionManager(WebForm webform) {
        if (webform == null) {
            throw new NullPointerException("Parameter webform is null!"); // NOI18N
        }
        
        this.webform = webform;
    }

    /** Notify the selection manager that the box hierarchy has changed,
     * so update the insert box reference if necessary */
    public void updateInsertBox() {
        if (insertModeBox == null) {
            return;
        }

        if (insertModeBox instanceof PageBox) {
            insertModeBox = webform.getPane().getPageBox();

            return;
        }

        Element element = insertModeBox.getElement();
//        insertModeBox = CssBox.getBox(element);
        insertModeBox = webform.findCssBoxForElement(element);
    }

    /** Return the box whose contents are being edited. Will be null when we're
     * not in edit mode.
     * @todo Rename from "insert mode" to "text edit" mode or something like that.
     */
    public CssBox getInsertModeBox() {
        return insertModeBox;
    }

    void drawInlineEditorBox(Graphics2D g2d, Rectangle rect) {
        // Tied closely to selection nib painting so implemented in the SelectionManager
        webform.getSelection().paintInlineEditorBox(g2d, rect);
    }

    /** Set the view which should be in "insert mode" (have an insert
     * cursor and a thick component marker rectangle). May be null
     * to turn off insert mode.
     */
    public void setInsertBox(CssBox box, MouseEvent event) {
        DesignerPane pane = webform.getPane();

//        if ((box != null) && (box == pane.getPageBox()) && webform.getDocument().isGridMode()) {
//        if ((box != null) && (box == pane.getPageBox()) && webform.isGridModeDocument()) {
        if ((box != null) && (box == pane.getPageBox()) && webform.isGridMode()) {
            box = null;
        }

        if (insertModeBox != box) {
            insertModeBox = box;

            // See if this is a simple div which only contains a jsp include tag.
            // Those should not be inline editable.
            if (box != null) {
//                DesignBean bean = box.getDesignBean();
//                DesignBean bean = CssBox.getMarkupDesignBeanForCssBox(box);
//                if ((bean != null) && bean.getInstance() instanceof org.netbeans.modules.visualweb.xhtml.Div) {
//                    if ((bean.getChildBeanCount() == 1) &&
//                            bean.getChildBean(0).getInstance() instanceof org.netbeans.modules.visualweb.xhtml.Jsp_Directive_Include) {
//                        insertModeBox = null;
//
//                        return;
//                    }
//                }
                // XXX Doesn't allow to insert into include (fragment).
                Element componentRootElement = CssBox.getElementForComponentRootCssBox(box);
                if (WebForm.getDomProviderService().isIncludeComponentBox(componentRootElement)) {
                    insertModeBox = null;
                    return;
                }
            }

//            DesignerCaret dc = pane.getCaret();
            
//            boolean showCaret = (box != null) || !webform.getDocument().isGridMode();
//            boolean showCaret = (box != null) || !webform.isGridModeDocument();
            boolean showCaret = (box != null) || !webform.isGridMode();

//            if (showCaret == (dc != null)) {
            if (showCaret == pane.hasCaret()) {
                return;
            }

            if (showCaret) {
//                dc = pane.getPaneUI().createCaret();
//                pane.setCaret(dc);
                pane.createCaret();

//                Position pos = Position.NONE;
                DomPosition pos = DomPosition.NONE;

                if (event != null) {
//                    pos = pane.viewToModel(event.getPoint());
                    pos = webform.viewToModel(event.getPoint());

//                    if (pos != Position.NONE) {
                    if (pos != DomPosition.NONE) {
//                        pos = DesignerUtils.checkPosition(pos, true, /*webform*/webform.getManager().getInlineEditor());
//                        pos = ModelViewMapper.findValidPosition(pos, true, /*webform*/webform.getManager().getInlineEditor());
                        pos = ModelViewMapper.findValidPosition(webform, pos, true, /*webform*/webform.getManager().getInlineEditor());
                    }

//                    if ((pos != Position.NONE) && (box.getDesignBean() != null)) {
//                        pos = new Position(box.getDesignBean().getElement(), 0, Bias.FORWARD);
//                    MarkupDesignBean markupDesignBean = CssBox.getMarkupDesignBeanForCssBox(box);
//                    if ((pos != Position.NONE) && (markupDesignBean != null)) {
//                        pos = new Position(markupDesignBean.getElement(), 0, Bias.FORWARD);
                    Element componentRootElement = CssBox.getElementForComponentRootCssBox(box);
//                    if ((pos != Position.NONE) && (componentRootElement != null)) {
                    if ((pos != DomPosition.NONE) && (componentRootElement != null)) {
                        Element sourceElement = MarkupService.getSourceElementForElement(componentRootElement);
                        if (sourceElement != null) {
//                            pos = new Position(sourceElement, 0, Bias.FORWARD);
//                            pos = Position.create(sourceElement, 0, Bias.FORWARD);
                            pos = webform.createDomPosition(sourceElement, 0, Bias.FORWARD);
                        }
                    }
                }

//                pane.setCaretPosition(pos);
                pane.setCaretDot(pos);

                pane.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));

                if (event != null) {
//                    dc.mousePressed(event);
//                    dc.mouseClicked(event);
                    pane.mousePressed(event);
                    pane.mouseClicked(event);
                }

//                dc.setVisible(true);
                pane.setCaretVisible(true);
            } else {
                pane.setCaret(null);

                // XXX Which cursor do we set?
                //mouseHandler.mouseMoved(event);
            }

            /* TODO: let users edit text-node property children
            if (box != null && box.getDesignBean() != null) {
                DesignBean lb = box.getDesignBean();
                BeanInfo bi = lb.getBeanInfo();
                if (bi != null) {

                    BeanDescriptor bd = bi.getBeanDescriptor();
                    Object o = bd.getValue(Constants.BeanDescriptor.TEXT_NODE_PROPERTY);
                }
            }
            */
        }
    }

    /** Given a box, locate its nearest insert-box. Eg. for a text box,
     * the insert box would be the surrounding paragraph box (not for
     * example its surrounding italic span box.)
     */
    private CssBox findInsertBox(CssBox box) {
        if (!webform.isGridMode()) {
            // XXX No, go wrap up jsf rendered document fragments as
            // a single unit so we avoid editing outside its boundaries
            // This does however mean that things like jsf buttons
            // will be treated as isolated units
            return webform.getPane().getPageBox();
        }

        // This would be the first block or absolutely positioned
        // child of a grid box - where lineboxes don't count.

        /* No longer applicable - markup beans breaks this
        while (box != null && box.getDesignBean() != null) {
            box = box.getParent();
        }
         */
        CssBox leaf = box;

        while (box != null) {
            // We're within a grid panel - no possible insert box
            if (box.isGrid()) {
                return null;
            }

            if (box.getBoxType().isAbsolutelyPositioned()) {
                // Allow inserts on something like a <span>, disallow
                // it on something like an <img>
                if (box instanceof ContainerBox) {
                    break;
                } else {
                    return null;
                }
            }

            if ((box.getParent() == null) || box.getParent().isGrid()) {
                // We're the child of a grid box; if we're a block or
                // absolutely positioned child, this is an insert box,
                if ((box.getBoxType() != BoxType.LINEBOX) && box.isBlockLevel() &&
                        (box instanceof ContainerBox)) {
                    break;
                } else {
                    // if not we're unattached text within a grid panel;
                    // this is not editable with an insert box
                    return null;
                }
            }

            box = box.getParent();
        }

        // Ensure that we're not inside a jsf component, in which case
        // we should use the normal hiearchy instead
        CssBox curr = (leaf == null) ? null : leaf.getParent();

        while ((curr != null) && (curr != box)) {
//            DesignBean b = curr.getDesignBean();
//            DesignBean b = CssBox.getMarkupDesignBeanForCssBox(curr);
            Element componentRootElement = CssBox.getElementForComponentRootCssBox(curr);

//            if ((b != null) && FacesSupport.isFacesComponent(b)) {
//            if ((b != null) && isFacesComponent(b)) {
//            if (b != null && WebForm.getDomProviderService().isFacesComponentBean(b)) {
            if (componentRootElement != null && WebForm.getDomProviderService().isFacesComponent(componentRootElement)) {
                return null;
            }

            curr = curr.getParent();
        }

        return box;
    }

//    // XXX Moved from FacesSupport.
//    /** Indicate whether this bean represents a JSF bean (vs an html bean) */
//    private static boolean isFacesComponent(DesignBean bean) {
//        return bean.getInstance() instanceof UIComponent;
//    }


    /**
     * Report whether we're inline editing a component
     */
    public boolean isInlineEditing() {
        return inlineEditor != null;
    }

    public InlineEditor getInlineEditor() {
        return inlineEditor;
    }

    public void startInlineEditing(Element componentRootElement, String propertyName) {
        if (isInlineEditing() && getInlineEditor().isEditing(componentRootElement, propertyName)) {
            return;
        } else {
            finishInlineEditing(false);
        }

        CssBox box = ModelViewMapper.findBoxForComponentRootElement(webform.getPane().getPageBox(), componentRootElement);
////        webform.getTopComponent().requestActive();
//        webform.tcRequestActive();
        startInlineEditing(componentRootElement, propertyName, box, true, true, null, false);
    }
    
    /**
     * Inline edit the given DesignBean, if the bean supports it.
     * If so, inline edit the given bean and return true.
     * Otherwise, return false.
     *
     * @param ensureSelected If true, make sure that the component to be inline edited is also selected
     * @param selectText If true, select all the text in the inline section after initiating editing
     * @param initialEdit If a non empty string, initiate editing as if the user had typed the string
     * @param box The box corresponding to the component click, if any
     * @param property The component to be inline edited
     * @param propertyName May be null; if so, either the property corresponding to the box clicked should be edited,
     *    or the default property provided allow default is true
     * @param useDefault If true, start editing the property marked default (if any) (marked with a "*" in the metadata)
     */
    public boolean startInlineEditing(Element componentRootElement, /*MarkupDesignBean lb,*/ String property, CssBox box,
        boolean ensureSelected, boolean selectText, String initialEdit, boolean useDefault) {
        finishInlineEditing(false);

        if (box == null) {
//            box = webform.getMapper().findBox(lb);
            box = ModelViewMapper.findBoxForComponentRootElement(webform.getPane().getPageBox(), componentRootElement);
        }

//        inlineEditor = InlineEditor.getInlineEditor(webform, box, lb, property, useDefault);
        inlineEditor = InlineEditor.getInlineEditor(webform, box, componentRootElement, property, useDefault);

        if (inlineEditor != null) {
            SelectionManager sm = webform.getSelection();

//            if (ensureSelected && (!sm.isSelected(lb) || (sm.getNumSelected() > 1))) {
//                sm.setSelected(lb, true);
            if (ensureSelected && (!sm.isSelected(componentRootElement) || (sm.getNumSelected() > 1))) {
                sm.setSelected(componentRootElement, true);
            }

////            webform.getTopComponent().requestActive();
//            webform.tcRequestActive();
            
            DesignerPane pane = webform.getPane();
            pane.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
            inlineEditor.start(selectText, initialEdit);
            pane.repaint();

            return true;
        }

        return false;
    }

    /** If there is an inline editor, finish editing in it. */
    public void finishInlineEditing(boolean cancel) {
        if (inlineEditor != null) {
            try {
                inlineEditor.finish(cancel);
            } finally {
                inlineEditor = null;
            }

            webform.getPane().repaint();
        }
    }

//    /** The user double clicked on the component during -initial- inline editing
//     * (e.g. we entered inline editing as part of the first click in a double
//     * click) so cancel inline editing and process the double click in the normal
//     * way: as a request to open the default event handler. However if there is
//     * no default event handler, stay in inline edit mode. */
//    public void notifyEditedDoubleClick() {
////        if (webform.getActions().handleDoubleClick(true)) {
////        if (handleDoubleClick(true)) {
//        if (handleDoubleClick()) {
//            finishInlineEditing(true);
//        }
//    }
//    
//    // XXX Moved from DesignerActions.
//    /** Perform the equivalent of a double click on the first item
//     * in the selection (if any).
//     * @param selOnly If true, only do something if the selection is nonempty
//     * @todo Rename to handleDefaultAction
//     * @return True iff the double click resulted in opening an event handler
//     */
//    public boolean handleDoubleClick(/*boolean selOnly*/) {
//        CssBox box = null;
//        SelectionManager sm = webform.getSelection();
//
//        if (!sm.isSelectionEmpty()) {
////            Iterator it = sm.iterator();
////
////            while (it.hasNext()) {
////                DesignBean bean = (DesignBean)it.next();
//            for (Element componentRootElement : sm.getSelectedComponentRootElements()) {
////                DesignBean bean = WebForm.getDomProviderService().getMarkupDesignBeanForElement(componentRootElement);
////                if (bean != null) {
//                if (componentRootElement != null) {
////                    box = webform.getMapper().findBox(bean);
//                    box = ModelViewMapper.findBoxForComponentRootElement(webform.getPane().getPageBox(), componentRootElement);
//
//                    if (box != null) {
//                        break;
//                    }
//                }
//            }
////        } else if (selOnly) {
//        } else {
//            return false;
//        }
//
//        return handleDoubleClick(box);
//    }
//
//    // XXX Moved from DesignerActions.
//    /** Handle double clicks with the given box as the target.
//     * @todo Rename to handleDefaultAction
//     * @return Return true iff the double click resulted in opening an event handler
//     */
//    private boolean handleDoubleClick(CssBox box) {
//        if (box instanceof ExternalDocumentBox) {
//            ((ExternalDocumentBox)box).open();
//
//            return false;
//        } else if ((box != null) && (box.getTag() == HtmlTag.DIV) && (box.getBoxCount() == 1) &&
//                box.getBox(0) instanceof ExternalDocumentBox) {
//            // IF user has clicked on a large div containing only a
//            // jsp include, treat this as an attempt to open the page
//            // fragment child.
//            ((ExternalDocumentBox)box.getBox(0)).open();
//
//            return false;
//        } else {
//            return editEventHandler();
//        }
//    }
//    
//    // XXX Copied from DesignerActions.
//    /** Return true iff an event handler was found and created/opened */
//    private boolean editEventHandler() {
////        SelectionManager sm = webform.getSelection();
////
////        if (sm.isSelectionEmpty()) {
////            webform.getModel().openDefaultHandler();
////
////            return false;
////        }
////
////        // TODO - get the component under the mouse, not the
////        // whole selection!
////        DesignBean component = getDefaultSelectionBean();
////
////        if (component != null) {
////            // See if it's an XHTML element; if so just show it in
////            // the JSP source
//////            if (FacesSupport.isXhtmlComponent(component)) {
////            if (isXhtmlComponent(component)) {
//////                MarkupBean mb = FacesSupport.getMarkupBean(component);
////                MarkupBean mb = Util.getMarkupBean(component);
////                
////                MarkupUnit unit = webform.getMarkup();
////                // <markup_separation>
//////                Util.show(null, unit.getFileObject(),
//////                    unit.computeLine((RaveElement)mb.getElement()), 0, true);
////                // ====
//////                MarkupService.show(unit.getFileObject(), unit.computeLine((RaveElement)mb.getElement()), 0, true);
////                showLineAt(unit.getFileObject(), unit.computeLine(mb.getElement()), 0);
////                // </markup_separation>
////            } else {
////                webform.getModel().openDefaultHandler(component);
////            }
////
////            return true;
////        }
////
////        return false;
//        SelectionManager sm = webform.getSelection();
////        DesignBean component;
//        Element componentRootElement;
//        if (sm.isSelectionEmpty()) {
////            webform.getModel().openDefaultHandler();
////
////            return false;
////            component = null;
//            componentRootElement = null;
//        } else {
////            component = getDefaultSelectionBean();
////            Element componentRootElement = getDefaultSelectionComponentRootElement();
////            component = WebForm.getDomProviderService().getMarkupDesignBeanForElement(componentRootElement);
//            componentRootElement = getDefaultSelectionComponentRootElement();
//        }
//
////        return webform.editEventHandlerForDesignBean(component);
//        return webform.editEventHandlerForComponent(componentRootElement);
//    }
    
//    // XXX Copied from DesignerActions.
//    /** Return true iff the given DesignBean is an XHTML markup "component" */
//    private boolean isXhtmlComponent(DesignBean bean) {
////        MarkupBean mb = FacesSupport.getMarkupBean(bean);
//        MarkupBean mb = Util.getMarkupBean(bean);
//
//        return (mb != null) && !(mb instanceof FacesBean);
//    }

    // XXX Copy from DesignerActions
    // XXX Copy also in designer/jsf/../JsfDesignerListener.
//    private DesignBean getDefaultSelectionBean() {
    private Element getDefaultSelectionComponentRootElement() {
        // TODO - should this be a checkbox instead?
        SelectionManager sm = webform.getSelection();
//        DesignBean bean = sm.getPrimary();
//
//        if ((bean == null) && !sm.isSelectionEmpty()) {
        Element primaryComponentRootElement = sm.getPrimary();
//        DesignBean bean = WebForm.getDomProviderService().getMarkupDesignBeanForElement(primaryComponentRootElement);
        if (primaryComponentRootElement == null && !sm.isSelectionEmpty()) {
            // TODO - get the component under the mouse, not the
            // whole selection!
//            Iterator it = sm.iterator();
//
//            while (it.hasNext()) {
//                bean = (DesignBean)it.next();
            for (Element componentRootElement : sm.getSelectedComponentRootElements()) {
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
    
//    // XXX Copied from MarkupUtilities.
//    // XXX Copied from DesignerActions.
//    private  static void showLineAt(FileObject fo, int lineno, int column) {
//        DataObject dobj;
//        try {
//            dobj = DataObject.find(fo);
//        }
//        catch (DataObjectNotFoundException ex) {
//            ErrorManager.getDefault().notify(ex);
//            return;
//        }
//
//        // Try to open doc before showing the line. This SHOULD not be
//        // necessary, except without this the IDE hangs in its attempt
//        // to open the file when the file in question is a CSS file.
//        // Probably a bug in the xml/css module's editorsupport code.
//        // This has the negative effect of first flashing the top
//        // of the file before showing the destination line, so
//        // this operation is made conditional so only clients who
//        // actually need it need to use it.
//        EditorCookie ec = (EditorCookie)dobj.getCookie(EditorCookie.class);
//        if (ec != null) {
//            try {
//                ec.openDocument(); // ensure that it has been opened - REDUNDANT?
//                //ec.open();
//            }
//            catch (IOException ex) {
//                ErrorManager.getDefault().notify(ex);
//            }
//        }
//
//        LineCookie lc = (LineCookie)dobj.getCookie(LineCookie.class);
//        if (lc != null) {
//            Line.Set ls = lc.getLineSet();
//            if (ls != null) {
//                // -1: convert line numbers to be zero-based
//                Line line = ls.getCurrent(lineno-1);
//                // TODO - pass in a column too?
//                line.show(Line.SHOW_GOTO, column);
//            }
//        }
//    }

    
    /**
     * During drag and drop, given a mouse position, decide whether a caret
     * position applies (and if so, show the caret at that position, otherwise
     * hide it), show drag & drop feedback messages in the status message
     * area and highlight parents or link targets. If the dropped parameter
     * is set, perform drop setup too: set the insert position on the
     * drop handler, and clear other drop state. I used to have separate
     * feedback-while-dropping and now-dropping-setup-state methods, but
     * they went out of sync, so by combining them hopefully the feedback will
     * always reflect what will happen on a drop.
     * @todo I should have a method which sets the caret based on whether
     *     there was a selection
     * @todo Should return caret to original pos if you cancel out of a drop
     * @param committed If false, we're dragging but haven't dropped; just update
     *   status feedback and show caret etc. If true, we've committed to
     *   the drop, so set up the dnd handler, and clear out temporary caret
     *   feedback, etc.
     * @return The drop type
     */
    int updateDropState(Point p, boolean committed, Transferable transferable) {
        DesignerPane pane = webform.getPane();
//        CssBox box = webform.getMapper().findBox(p.x, p.y);
        CssBox box = ModelViewMapper.findBox(pane.getPageBox(), p.x, p.y);
        CssBox insertBox = findInsertBox(box);

//        if ((insertBox == pane.getPageBox()) && webform.getDocument().isGridMode()) {
//        if ((insertBox == pane.getPageBox()) && webform.isGridModeDocument()) {
        if ((insertBox == pane.getPageBox()) && webform.isGridMode()) {
            insertBox = null;
        }

//        Position pos = Position.NONE;
        DomPosition pos = DomPosition.NONE;

        if (!box.isGrid() /* && (insertBox != null)*/
        // XXX #97697 Do not process external document boxes, like fragments.
        // TODO Revise how to properly handle the fragments.
        && !(box instanceof ExternalDocumentBox)) {
            finishInlineEditing(true);
            assert getInlineEditor() == null;
//            pos = pane.viewToModel(p);
            pos = webform.viewToModel(p);

            //                if (getInlineEditor() == null ||
            //                        !webform.getSelection().getInlineEditor().isDocumentEditor()) {
            //                    boolean findNearest = !webform.isGridMode();
            //                    pos = Utilities.checkPosition(pos, findNearest, webformwebform.getManager().getInlineEditor());
            //                }
//            if (Document.isReadOnlyRegion(pos)) {
            if (isReadOnlyRegion(pos)) {
//                pos = Position.NONE;
                pos = DomPosition.NONE;
            }

            // See if the new position points to a location in a grid mode; for example,
            // you may be pointing at an absolutely positioned StaticText; the caret position
            // would now be adjusted to point to the left or right of this static text which
            // is really a grid location
//            if ((pos != Position.NONE) &&
            if ((pos != DomPosition.NONE) &&
                    (pos.getNode().getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)) {
                Element parent = (Element)pos.getNode();
//                CssBox parentBox = CssBox.getBox(parent);
                CssBox parentBox = webform.findCssBoxForElement(parent);

                if ((parentBox != null) && (parentBox.isGrid())) {
//                    pos = Position.NONE;
                    pos = DomPosition.NONE;
                }
            }
        }

        DndHandler dndHandler = pane.getDndHandler();
        int dropType;

        if (transferable != null) {
            dropType = dndHandler.getDropType(p /*, pos*/, transferable, false);
//        } else if (mouseHandler.paletteItems != null) {
//            dropType =
//                dndHandler.getDropType(p, // insertPos,
//                    mouseHandler.paletteItems, null, false);
        } else if (mouseHandler.isCnCInProgress()) {
            dropType = dndHandler.getDropType(p, mouseHandler.cncTransferable, false);
        } else {
            dropType = DndHandler.DROP_DENIED;
        }

        if (dropType == DndHandler.DROP_LINKED) {
            pane.setCursor(getLinkedCursor());
        } else if (dropType == DndHandler.DROP_PARENTED) {
            //pane.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            pane.setCursor(getInsertCursor());
        } else {
            assert dropType == DndHandler.DROP_DENIED;
            pane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }

//        // If we're doing a parent drop or a link, we should clear the
//        // caret if it's outside of the component!
//        MarkupDesignBean target = null;
//
//        // XXX Hack alert... getDropType sets the highlighted item so we can use
//        // that to infer the selected parent. We get DROP_DENIED a lot when
//        // using Dnd, so this is more reliable than just relying on
//        // DndHandler.getDropTarget(). I should debug this and clean it up.
////        if ((highlighted != null) && highlighted instanceof MarkupDesignBean) {
////            target = (MarkupDesignBean)highlighted;
//        target = WebForm.getDomProviderService().getMarkupDesignBeanForElement(highlightedComponentRootElement);
//        if (target != null) {
//            // XXX It was done already.
//        } else if ((dropType == DndHandler.DROP_LINKED) || (dropType == DndHandler.DROP_PARENTED)) {
////            target = dndHandler.getDropTarget();
////            target = dndHandler.getRecentDropTarget();
//            Element recentDropTargetComponnetRootElement = dndHandler.getRecentDropTargetComponentRootElement();
//            target = WebForm.getDomProviderService().getMarkupDesignBeanForElement(recentDropTargetComponnetRootElement);
//        }
//
//        if (target != null) {
//            Element element = target.getElement();
//            if (!pos.isInside(element)) { // XXX todo: check front/end positions!
        // XXX Hacks, see above (the commented out) original code.
        Element targetSourceElement = MarkupService.getSourceElementForElement(highlightedComponentRootElement);
        if (targetSourceElement == null) {
            targetSourceElement = MarkupService.getSourceElementForElement(dndHandler.getRecentDropTargetComponentRootElement());
        }

        if (targetSourceElement != null) {
            if (!pos.isInside(targetSourceElement)) { // XXX todo: check front/end positions!

                // The position is outside of the targeted component
//                pos = Position.NONE;
                pos = DomPosition.NONE;
            }
        }

        if (committed) {
//            if (pos != Position.NONE) {
            if (pos != DomPosition.NONE) {
                dndHandler.setInsertPosition(pos);
            } else {
                dndHandler.setDropPoint(p);
            }

            setInsertBox(null, null);

            // What do we do with the caret here?
            // Definitely hide it in grid mode, but what about flow?
            // Revert to original value if dnd is cancelled, and if a component
            // is dropped, the new component should be selected so the caret
            // won't be shown anyway...
            if (webform.isGridMode()) {
                pane.setCaret(null);
            }
        } else {
            // Update caret
//            if (pos != Position.NONE) {
            if (pos != DomPosition.NONE) {
//                if (pane.getCaret() == null) {
                if (!pane.hasCaret()) {
//                    DesignerCaret dc = pane.getPaneUI().createCaret();
//                    pane.setCaret(dc);
                    pane.createCaret();
                }

//                pane.getCaret().setVisible(true);
                pane.setCaretVisible(true);
//                pane.setCaretPosition(pos);
                pane.setCaretDot(pos);
            } else {
//                if (pane.getCaret() != null) {
                if (pane.hasCaret()) {
                    pane.setCaret(null);
                }
            }
        }

        return dropType;
    }

    /** We have a separate "current"/"highlighted" item, unrelated to selection,
     * which is used to for example indicate which box is being dropped upon.
     */
//    public void highlight(DesignBean bean, MarkupMouseRegion region) {
    public void highlight(Element componentRootElement, Element regionElement) {
//        if ((bean == highlighted) && (region == highlightedRegion)) {
        if ((componentRootElement == highlightedComponentRootElement) && (regionElement == highlightedRegionElement)) {
            return;
        }

//        highlighted = bean;
        highlightedComponentRootElement = componentRootElement;
//        highlightedRegion = region;
        highlightedRegionElement = regionElement;
        webform.getPane().repaint();
    }

    public boolean isHighlighted() {
//        return highlighted != null;
        return highlightedComponentRootElement != null;
    }

    /** Paint the selection on top of the given editor pane. */
    public void paint(Graphics2D g) {
        //getPageBox(); // ensure that colors etc. are synced
        webform.getColors().sync();

//        if (webform.isVirtualFormsEnabled()) {
////            VirtualFormSupport.paint(webform, g);
////            paintVirtualForms(webform, g);
//            webform.paintVirtualForms(g, new RenderContextImpl(webform));
//        }
        // XXX Generic support for various designer decorations (for now virtual forms, ajax transactions).
        webform.paintDesignerDecorations(g);

        // Paint both before and after
        webform.getSelection().paintSelection(g);
        paintInsertModeBox(g); // XXX REMOVE ME!

        if (inlineEditor != null) {
            inlineEditor.paint(g);
        }

        if (interaction != null) {
            interaction.paint(g);
        }

//        if (highlighted != null) {
        if (highlightedComponentRootElement != null) {
            Rectangle a = null;
//            ModelViewMapper mapper = webform.getMapper();

//            if (highlightedRegion != null) {
////                a = mapper.getRegionBounds(highlightedRegion);
//                a = webform.getPane().getPageBox().computeRegionBounds(highlightedRegion, null);
//
//                // Somehow the region isn't visible
//                highlightedRegion = null;
//            }
            if (highlightedRegionElement != null) {
                a = webform.getPane().getPageBox().computeRegionBounds(highlightedRegionElement, null);

                // Somehow the region isn't visible
                highlightedRegionElement = null;
            }

            if (a == null) {
//                a = mapper.getComponentBounds(highlighted);
//                a = ModelViewMapper.getComponentBounds(webform.getPane().getPageBox(), highlighted);
//                if (highlighted instanceof MarkupDesignBean) {
//                    a = ModelViewMapper.getComponentBounds(webform.getPane().getPageBox(),
//                            WebForm.getDomProviderService().getComponentRootElementForMarkupDesignBean((MarkupDesignBean) highlighted));
//                }
                a = ModelViewMapper.getComponentBounds(webform.getPane().getPageBox(), highlightedComponentRootElement);
            }

            if (a == null) {
                // Somehow the highlight rectangle hasn't been cleared
                // when the component disappeared. Ignore this one and
                // don't try again.
//                highlighted = null;
                highlightedComponentRootElement = null;
            } else {
                g.setColor(webform.getColors().dropTargetColor);
                g.drawRect(a.x, a.y, a.width, a.height);
            }
        }

        webform.getSelection().paintSelHierarchy(g);
    }

    private void paintInsertModeBox(Graphics2D g2d) {
        // Unfortunately editing the text under body gets translated
        // into a dom replacement, and I'm not handling that well
        if ((insertModeBox != null) && !(insertModeBox instanceof PageBox)) {
            Rectangle r =
                new Rectangle(insertModeBox.getAbsoluteX(), insertModeBox.getAbsoluteY(),
                    insertModeBox.getWidth(), insertModeBox.getHeight());
            drawInlineEditorBox(g2d, r);
        }
    }

    // Draw drag handle
    // When the mouse is moved inside some large components, like
    // tables, draw a dragging/moving handle. Shown when mouse moves
    // inside the component, removed when it leaves.
    public MouseHandler getMouseHandler() {
        if (mouseHandler == null) {
            mouseHandler = new MouseHandler();
        }

        return mouseHandler;
    }

    public static void translateMousePos(Point loc, Component source) {
        // This method is no longer necessary; I used to need to do
        // SwingUtilities.convertPoint(source, loc, destination)
        // here since mouse clicks could happen on embedded JButtons etc.
        // But now I paint only on the DesignerPane myself.
    }

    Cursor getInsertCursor() {
        if (insertCursor == null) {
            Component component = webform.getPane();
            Image image = null;
            Point p = null;

            /* This was only with JDK1.4 in Jaguar (10.2). Fixed in
               Panther (10.3).

            if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
                // Icons on mac look compressed - use a 32x32 (twice as large)
                // version of the pointer
                image = Utilities.loadImage("org/netbeans/modules/visualweb/designer/resources/insert-cursor-mac.gif"); //NOI18N
                // WEIRD! Would have thought it should be 16,16 or 17,17
                // here, but apparently while the image needs to be
                // twice the size, the hotspot needs to be in the 16x16
                // grid instead of the 32x32 grid!
                p = new Point(9,9);
            } else {
            */
            image = org.openide.util.Utilities.loadImage("org/netbeans/modules/visualweb/designer/resources/insert-cursor.gif"); //NOI18N
            p = new Point(9, 9);

            /* This doesn't work well - want to set our own hotspot
            insertCursor = Utilities.createCustomCursor(component, image,
                                               "CROSSINSERT_CURSOR"); //NOI18N
            */
            java.awt.Toolkit t = component.getToolkit();
            insertCursor = t.createCustomCursor(image, p, "CROSSINSERT_CURSOR");
        }

        return insertCursor;
    }

    /** Get cursor to use when dropping over a bean that only allows linking */
    Cursor getLinkedCursor() {
        if (linkedCursor == null) {
            Component component = webform.getPane();
            Image image = null;
            Point p = null;

            /* This was only with JDK1.4 in Jaguar (10.2). Fixed in
               Panther (10.3).

            if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
                // Icons on mac look compressed - use a 32x32 (twice as large)
                // version of the pointer
                image = Utilities.loadImage("org/netbeans/modules/visualweb/designer/linked-cursor-mac.gif"); //NOI18N
                // WEIRD! Would have thought it should be 16,16 or 17,17
                // here, but apparently while the image needs to be
                // twice the size, the hotspot needs to be in the 16x16
                // grid instead of the 32x32 grid!
                p = new Point(9,9);
            } else {
            */
            image = org.openide.util.Utilities.loadImage("org/netbeans/modules/visualweb/designer/resources/linked-cursor.gif"); //NOI18N
            p = new Point(9, 9);

            /* This doesn't work well - want to set our own hotspot
            linkedCursor = Utilities.createCustomCursor(component, image,
                                               "CROSSLINKED_CURSOR"); //NOI18N
            */
            java.awt.Toolkit t = component.getToolkit();
            linkedCursor = t.createCustomCursor(image, p, "CROSSLINKED_CURSOR");
        }

        return linkedCursor;
    }

//    public static void clearPalette(final WebForm webform) {
//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                if (webform != null) {
////                    webform.getManager().mouseHandler.clearPalette();
//                    webform.getManager().mouseHandler.doClearPalette();
//                } else {
//                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
//                            new IllegalStateException("No cleaning palette code. Should not be called!")); // NOI18N
////                    PaletteComponentModel palette = PaletteComponentModel.getInstance();
////
////                    if ((palette.getSelectedPalette() != null) &&
////                            (palette.getSelectedPalette().getSelectedPaletteSection() != null) &&
////                            (palette.getSelectedPalette().getSelectedPaletteSection()
////                                        .getSelectedPaletteItem() != null)) {
////                        palette.getSelectedPalette().getSelectedPaletteSection()
////                               .setSelectedPaletteItem(null);
////                    }
//                }
//            }
//        });
//    }
    public static void stopCnCForWebForm(WebForm webform) {
        if (webform == null) {
            return;
        }
        webform.getManager().mouseHandler.stopCnC();
    }
    
    
    private /*static*/ boolean isInsideBoxDecoration(CssBox box, int x, int y) {
        Decoration decoration = box.getDecoration();
        if (decoration != null) {
//            if (DesignerSettings.getInstance().isShowDecorations()) {
            if (webform.isShowDecorations()) {
                int x1 = box.getAbsoluteX() + box.getWidth();
                int y1 = box.getAbsoluteY();
                int x2 = x1 + decoration.getWidth();
                int y2 = y1 + decoration.getHeight();
                return x >= x1 && x <= x2 && y >= y1 && y <= y2;
            }
        }
        
        return false;
    }

    /** Return true iff the mouse event position is inside the
     * given box
     */
    private static boolean isInside(CssBox box, MouseEvent e) {
        return (e.getX() >= box.getAbsoluteX()) &&
        (e.getX() <= (box.getAbsoluteX() + box.getWidth())) && (e.getY() >= box.getAbsoluteY()) &&
        (e.getY() <= (box.getAbsoluteY() + box.getHeight()));
    }

    /** Return true iff the given box (curr) is below the root box */
    private static boolean isBelow(CssBox root, CssBox curr) {
        if (curr == root) {
            return true;
        }

        CssBox parent = curr.getParent();

        if (parent == null) {
            return false;
        }

        return isBelow(root, parent);
    }

    /** Return the text position at the given location - or Position.NONE if
     * we're not over a text flow area.
     * @todo Move to ModelViewMapper!
     */
//    public Position findTextPosition(int x, int y) {
    public DomPosition findTextPosition(int x, int y) {
//        ModelViewMapper mapper = webform.getMapper();
//        CssBox box = mapper.findBox(x, y);
        CssBox box = ModelViewMapper.findBox(webform.getPane().getPageBox(), x, y);
        CssBox insertBox = findInsertBox(box);

        if (insertBox != null) {
//            Position pos = ModelViewMapper.viewToModel(webform, x, y);
            DomPosition pos = ModelViewMapper.viewToModel(webform, x, y);

            if ((getInlineEditor() == null) || !getInlineEditor().isDocumentEditor()) {
//                pos = DesignerUtils.checkPosition(pos, true, /*webform*/webform.getManager().getInlineEditor());
//                pos = ModelViewMapper.findValidPosition(pos, true, /*webform*/webform.getManager().getInlineEditor());
                pos = ModelViewMapper.findValidPosition(webform, pos, true, /*webform*/webform.getManager().getInlineEditor());
            }

            return pos;
        } else {
//            return Position.NONE;
            return DomPosition.NONE;
        }
    }

    /**
     * For a given bean, find the closest ancestor (including itself) which
     * can be repositioned.
     * <p>
     * @param box The box whose bean we're trying to reposition
     */
//    private static MarkupDesignBean findMovableParent(CssBox box) {
    private static Element findMovableParentComponentRootElement(CssBox box) {
        // I'm using absolutely positioned rather than simply BoxType.isPositioned()
        // here because relative boxes are typically anchors from grid positioning
        // (like divs containing fragments and such)
        if (box.getBoxType().isAbsolutelyPositioned() || box.getParent().isGrid()) {
//            return box.getDesignBean();
            return CssBox.getElementForComponentRootCssBox(box);
        }

        CssBox prev = box;
        ContainerBox parent = box.getParent();

        while (parent != null) {
//            MarkupDesignBean prevMarkupDesignBean = CssBox.getMarkupDesignBeanForCssBox(prev);
            Element prevComponentRootElement = CssBox.getElementForComponentRootCssBox(prev);
            if (parent.isGrid()) {
//                return prev.getDesignBean();
                return prevComponentRootElement;
            }

//            MarkupDesignBean parentMarkupDesignBean = CssBox.getMarkupDesignBeanForCssBox(parent);
            Element parentComponentRootElement = CssBox.getElementForComponentRootCssBox(parent);
//            if ((parent.getDesignBean() != null) &&
////                    FacesSupport.isSpecialBean(/*webform, */parent.getDesignBean())) {
//                    Util.isSpecialBean(parent.getDesignBean())) {
//                return prev.getDesignBean();
//            if ((parentMarkupDesignBean != null) &&
////                    FacesSupport.isSpecialBean(/*webform, */parent.getDesignBean())) {
//                    Util.isSpecialBean(parentMarkupDesignBean)) {
            if (WebForm.getDomProviderService().isSpecialComponent(parentComponentRootElement)) {
                return prevComponentRootElement;
            }

            if (parent.getBoxType().isPositioned()) {
//                return parent.getDesignBean();
                return parentComponentRootElement;
            }

            prev = parent;
            parent = parent.getParent();
        }

        return null;
    }

    /**
     * Call when the selection objects have changed under us. Try to
     * Update to the current equivalent objects.
     */
    public void syncSelection(boolean update) {
        // XXX #113141 Cancel the inline editing.
//        finishInlineEditing(false);
        finishInlineEditing(true);
        
//        highlighted = null;
        highlightedComponentRootElement = null;
//        highlightedRegion = null;
        highlightedRegionElement = null;
    }

    // --------------------------------------------------------------------
    // Utility Methods
    // --------------------------------------------------------------------
    private void selectDomInspectorBox(CssBox box) {
        DomInspector.show(box);

        // #6475554 This seems to be just messing up.
//        // Also set nodes locally such that if this top component
//        // retains focus the node selection reflects the selected box
//        SelectionManager sm = webform.getSelection();
//        ArrayList nodes = new ArrayList(sm.getNumSelected());
//        DataObject dobj = webform.getDataObject();
//        Node n = new DomInspector.BoxNode(box, dobj);
//
//        //n.setDataObject(dobj);
//        nodes.add(n);
//
//        Node[] nds = (Node[])nodes.toArray(new Node[nodes.size()]);
//        DesignerTopComp topcomp = webform.getTopComponent();
//
//        if (topcomp.isShowing()) {
//            topcomp.requestActive();
//        }
//
//        DesignerUtils.setActivatedNodes(topcomp, nds);
//        webform.getPane().getPageBox().setSelected(box);
    }

    /** Return true iff this event corresponds to a "toggle" event,
     * e.g. an event where you want to toggle the selection instead
     * of replacing it. This is typically the case when some modifier
     * key is pressed.
     */
    public static boolean isToggleEvent(MouseEvent e) {
        // Shift does it - as does control, unless you're on the mac
        // where we reserve the control key for popup menu use
        // (one button mouse ui!)
        return e.isShiftDown() ||
        ((org.openide.util.Utilities.getOperatingSystem() != org.openide.util.Utilities.OS_MAC) &&
        e.isControlDown());
    }

    /** Return true iff this event corresponds to a "menu" event,
     * e.g. an event where you want to post a popup menu.
     * This is typically the case when the "right" mouse button
     * is pressed.
     */
    public static boolean isMenuEvent(MouseEvent e) {
        if (e.isShiftDown()) {
            return false;
        }

        return e.isPopupTrigger() || 
        // XXX This seems necessary on Windows. Temporary workaround!
        // (Actually, the problem is that it will return true only on button RELEASE
        // in the case of Windows, and I'm calling it from mousePressed. I'll need
        // to rework this a bit to work right on Windows.
        (e.getButton() != 1);
    }

    public static boolean isLinkingEvent(MouseEvent e) {
        // IMPORTANT -- keep in sync with code in Dragger (getDragAction)
        return e.isShiftDown() && e.isControlDown();
    }

    /** Return true iff this event corresponds to a "dom inspector"
     * event, e.g. an event which overrides normal mouse handling
     * and causes CSS boxes to be selected and displayed in the
     * property sheet instead.
     */
    public static boolean isDomEvent(MouseEvent e) {
        boolean isMac =
            org.openide.util.Utilities.getOperatingSystem() == org.openide.util.Utilities.OS_MAC;

        return (ENABLE_DOM_INSPECTOR && isMac) ? e.isAltDown() : (e.isControlDown() &&
        e.isAltDown());
    }

    /**
     * Class to watch the associated component and fire
     * hyperlink events on it when appropriate.
     */
    public class MouseHandler extends MouseAdapter implements MouseMotionListener {
        
        private Point currentPos = new Point(-1, -1);
        
        // XXX Moved from DesignerActions.
        private int menuPosX;
        private int menuPosY;

        /** Did the last mouse press change the selection? */
        private boolean dontCycleInClickHandler;
        private Timer cycleTimer;
//        String[] paletteItems = null;
        
        /** CnC (Click and Click) <code>Transferable</code>, if non-null then CnC is in progress.
         * Currently it may come from palette item or item in runtime window -> db sources. */
        private Transferable cncTransferable;
        
//        private ExplorerManager selectedManager = null;
//        private boolean paletteItemSelected = false;

        /** Report current or most recent known mouse position. */
        public Point getCurrentPos() {
            // TODO JDK15
            // With JDK 1.5, the mouse position can be read through
            // Component.getMousePosition() and the MouseInfo class.
            //
            // For now do ugly hack instead. The reason the menu position
            // is remember is that when we post a context menu, I want
            // the menu-post position to be returned, but the menu post
            // will cause a mouseExited event first which we'll use to
            // clear our position as is the case when the mouse actually
            // leave the window
            if ((currentPos.x >= 0) && (currentPos.y >= 0)) {
                return currentPos;
//            } else if (DesignerActions.menuPosX >= 0) { // JDK15 TODO Upgrade
            } else if (menuPosX >=0) {
//                return new Point(DesignerActions.menuPosX, DesignerActions.menuPosY);
                return new Point(menuPosX, menuPosY);
            } else {
                return null;
            }
        }

        /** Clear out the current mouse position such that the next
         * getCurrentPos will return null until a new position is
         * known.
         */
        public void clearCurrentPos() {
            currentPos.x = -1;
            currentPos.y = -1;
//            DesignerActions.menuPosX = -1; // JDK15 TODO Upgrade
//            DesignerActions.menuPosY = -1;
            menuPosX = -1;
            menuPosY = -1;
        }

        /**
           On mouse down we need to handle the following cases:
           - If we're in inline editing mode, forward the mouse click to
             the caret handler (where a click is used to set the caret for
             example)
           - If it's a double click, we should apply the default action to
             the selected component (which typically means opening the backing
             file to show its event handler
           - If it's a single click, we may enter into inline editing mode
             for this component if it supports it; we enter into inline editing
             mode after two consecutive clicks (not a double click)
           - In the DOM inspector, select the box under the pointer
           - We should also cycle the selection - but not if the user is about
             to double click! Therefore, the cycling operation has a brief
             delay (equal to the mouse double click interval) after which point
             we know that a double click isn't happening. If on the other hand
             a double click occurs before the timer has expired, the timer is
             cancelled.
         */
        public void mouseClicked(MouseEvent e) {
            DesignerPane pane = webform.getPane();

            if (ENABLE_DOM_INSPECTOR) {
                if (isDomEvent(e)) {
                    PageBox pageBox = webform.getPane().getPageBox();
                    CssBox box = pageBox.findCssBox(e.getX(), e.getY()); // Use Box interface instead?
                    selectDomInspectorBox(box);

                    return;
                } else {
                    PageBox pageBox = pane.getPageBox();
                    pageBox.setSelected(null);
                }
            }

//            DesignerCaret caret = pane.getCaret();
//            ModelViewMapper mapper = webform.getMapper();

            if (inlineEditor != null) {
                inlineEditor.checkInitialDoubleClick(e);
            }

//            if (!paletteItemSelected && (inlineEditor != null) && (caret != null)
//            if (!isCnCInProgress() && (inlineEditor != null) && (caret != null)
            if (!isCnCInProgress() && (inlineEditor != null) && (pane.hasCaret())
//            && inlineEditor.isEdited(mapper.findBox(e.getX(), e.getY()))
            && inlineEditor.isEdited(ModelViewMapper.findBox(pane.getPageBox(), e.getX(), e.getY()))
            /* && !MouseUtils_RAVE.isDoubleClick(e)*/) {
                // Reroute the mouse press to the insert box
//                caret.mouseClicked(e);
                pane.mouseClicked(e);

                return;
            }

            SelectionManager sm = webform.getSelection();
//            CssBox box = mapper.findBox(e.getX(), e.getY());
            CssBox box = ModelViewMapper.findBox(pane.getPageBox(), e.getX(), e.getY());
            boolean isBoxSelected = sm.isBelowSelected(box);
//            MarkupMouseRegion region = FacesSupport.findRegion(box.getElement());
//            MarkupMouseRegion region = findRegion(box.getElement());
//
//            if ((region != null) && region.isClickable()) {
//                Result r = region.regionClicked(e.getClickCount());
//                ResultHandler.handleResult(r, webform.getModel());
//                // #6353410 If there was performed click on the region
//                // then do not perform other actions on the same click.
//                return;
//            }
//            if (webform.handleMouseClickForElement(box.getElement(), e.getClickCount())) {
//                // #6353410 If there was performed click on the region
//                // then do not perform other actions on the same click.
//                return;
//            }
            DesignerClickEvent evt = new DefaultDesignerClickEvent(webform, box, e.getClickCount());
            webform.fireUserElementClicked(evt);
            if (evt.isConsumed()) {
                // #6353410 If there was performed click on the region
                // then do not perform other actions on the same click.
                return;
            }

//            if (!paletteItemSelected && (insertModeBox != null) && (caret != null)
//            if (!isCnCInProgress() && (insertModeBox != null) && (caret != null) 
            if (!isCnCInProgress() && (insertModeBox != null) && pane.hasCaret() 
//            && isInside(insertModeBox, e) && !MouseUtils_RAVE.isDoubleClick(e)) {
            && isInside(insertModeBox, e) && !MouseUtils.isDoubleClick(e)) {
                // Reroute the mouse press to the insert box
//                caret.mouseClicked(e);
                pane.mouseClicked(e);

                if ((inlineEditor == null) && isBoxSelected && !dontCycleInClickHandler) {
                    if (box.getBoxType() == BoxType.TEXT) {
                        box = box.getParent();
                    }

//                    if (startInlineEditing(box.getDesignBean(), null, box, true, true, null, false)) {
//                    if (startInlineEditing(CssBox.getMarkupDesignBeanForCssBox(box), null, box, true, true, null, false)) {
                    if (startInlineEditing(CssBox.getElementForComponentRootCssBox(box), null, box, true, true, null, false)) {
                        return;
                    }
                }

                return;
//            } else if (!webform.getDocument().isGridMode() && (caret != null)) {
//            } else if (!webform.isGridModeDocument() && (caret != null)) {
//            } else if (!webform.isGridModeDocument() && pane.hasCaret()) {
            } else if (!webform.isGridMode() && pane.hasCaret()) {
//                caret.mouseClicked(e);
                pane.mouseClicked(e);

                // fall through
            }

//            if (MouseUtils_RAVE.isDoubleClick(e)) {
            if (MouseUtils.isDoubleClick(e)) {
                if (!isInlineEditing()) {
                    if (isInsideBoxDecoration(box, e.getX(), e.getY())) {
                        processDefaultDecorationAction(box);
                    } else {
                        // LATER when I have markup beans check for Frame bean
                        // instead in the selection, don't use box located above
//                        webform.getActions().handleDoubleClick(box);
//                        handleDoubleClick(box);
                        DesignerEvent evt2 = new DefaultDesignerEvent(webform, box);
                        webform.fireUserActionPerformed(evt2);
                    }
                }
            } else if (isToggleEvent(e)) {
//                // Do nothing - already handled in mousePressed; but we don't
//                // want any selection cycling or inline editing
                // #94266 Toggle only on click not press or release
                selectAt(e, false);
            } else if (!dontCycleInClickHandler) {
                // Only cycle when we're clicking on faces beans; otherwise
                // clicking puts you in insert mode
                CssBox curr = box;

                while (curr != null) {
//                    if (curr.getDesignBean() != null) {
//                        FacesBean fb = FacesSupport.getFacesBean(curr.getDesignBean());
//
//                        if ((fb != null) &&
////                                !FacesSupport.isSpecialBean(/*webform, */curr.getDesignBean())) {
//                                !Util.isSpecialBean(curr.getDesignBean())) {
//                    MarkupDesignBean currMarkupDesignBean = CssBox.getMarkupDesignBeanForCssBox(curr);
                    Element currComponentRootElement = CssBox.getElementForComponentRootCssBox(curr);
//                    if (currMarkupDesignBean != null) {
////                        FacesBean fb = FacesSupport.getFacesBean(currMarkupDesignBean);
//                        FacesBean fb = Util.getFacesBean(currMarkupDesignBean);
//                        if ((fb != null)
//                        && !Util.isSpecialBean(currMarkupDesignBean)) {
//                            break;
//                        }
//                    }
//                    if (webform.isNormalAndHasFacesBean(currMarkupDesignBean)) {
                    if (webform.isNormalAndHasFacesComponent(currComponentRootElement)) {
                        break;
                    }

                    curr = curr.getParent();
                }

                if (curr == null) {
                    // Click not below a faces bean - try to enter insert mode instead
                    CssBox insertBox = findInsertBox(box);
                    setInsertBox(insertBox, e);
                } else {
//                    MarkupDesignBean currMarkupDesignBean = CssBox.getMarkupDesignBeanForCssBox(curr);
                    // Inline editing takes precedence over selection
                    // cycling!
//                    if ((inlineEditor == null) && sm.isSelected(curr.getDesignBean())) {
//                        if (startInlineEditing(curr.getDesignBean(), null, box, true, true, null,
                    Element componentRootElement = CssBox.getElementForComponentRootCssBox(curr);
                    if ((inlineEditor == null) && sm.isSelected(componentRootElement)) {
//                        if (startInlineEditing(currMarkupDesignBean, null, box, true, true, null,
                        if (startInlineEditing(componentRootElement, null, box, true, true, null,
                                    false)) {
                            return;
                        }
                    }

                    requestCycle(e.getX(), e.getY());
                }
            }
        }
        
        private void cancelCycleRequest() {
            if (cycleTimer != null) {
                cycleTimer.stop();
                cycleTimer = null;
            }
        }

        private void requestCycle(final int x, final int y) {
            cancelCycleRequest();

//            int delay = MouseUtils_RAVE.getDoubleClickInterval();
            int delay = DOUBLE_CLICK_DELTA;
            cycleTimer =
                new Timer(delay,
                    new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            cycleTimer = null;
                            cycleSelection(x, y);
                        }
                    });
            cycleTimer.setRepeats(false);
            cycleTimer.setCoalesce(true);
            cycleTimer.start();
        }

        private void cycleSelection(int x, int y) {
            // Cycle selection
//            ModelViewMapper mapper = webform.getMapper();
//            CssBox bx = mapper.findBox(x, y);
            CssBox bx = ModelViewMapper.findBox(webform.getPane().getPageBox(), x, y);

            while (bx != null) {
//                if (bx.getDesignBean() != null) {
//                if (CssBox.getMarkupDesignBeanForCssBox(bx) != null) {
                if (CssBox.getElementForComponentRootCssBox(bx) != null) {
                    break;
                }

                bx = bx.getParent();
            }

            SelectionManager sm = webform.getSelection();
            CssBox ancestor = null;

            if (bx != null) {
//                MarkupDesignBean boxMarkupDesignBean = CssBox.getMarkupDesignBeanForCssBox(bx);
//                if ((bx.getDesignBean() != null) && sm.isSelected(bx.getDesignBean())) {
                Element componentRootElement = CssBox.getElementForComponentRootCssBox(bx);
                if ((componentRootElement != null) && sm.isSelected(componentRootElement)) {
                    ancestor = bx;
                } else {
                    ancestor = sm.getSelectedAncestor(bx);
                }
            }

            if (ancestor != null) {
                // "ancestor" is the currently selected component
                // we're over: "cycle" outwards
//                DesignBean parent = null;
                Element parentComponentRootElement = null;

//                MarkupDesignBean ancestorMarkupDesignBean = CssBox.getMarkupDesignBeanForCssBox(ancestor);
//                if (webform.getActions().canSelectParent(ancestor.getDesignBean())) {
//                if (SelectionManager.canSelectParent(ancestor.getDesignBean())) {
//                    parent = ancestor.getDesignBean().getBeanParent();
//                if (SelectionManager.canSelectParent(ancestorMarkupDesignBean)) {
                Element ancestorComponentRootElement = CssBox.getElementForComponentRootCssBox(ancestor);
//                if (SelectionManager.canSelectParent(ancestorComponentRootElement)) {
                if (canSelectParent(ancestorComponentRootElement)) {
//                    parent = ancestorMarkupDesignBean.getBeanParent();
                    parentComponentRootElement = WebForm.getDomProviderService().getParentComponent(ancestorComponentRootElement);
                }

                boolean found = false;

//                while (parent != null) {
                while (parentComponentRootElement != null) {
                    // Find the visual (non-form, non-root) parent
                    // and select it
//                    Element element = FacesSupport.getElement(parent);
//                    Element element = Util.getElement(parent);
                    
//                    Element element = WebForm.getDomProviderService().getElement(parent);
//                    if (element != null) {
//                        CssBox box = mapper.findBox(element);
//                        CssBox box = ModelViewMapper.findBox(webform.getPane().getPageBox(), element);
                    CssBox box = ModelViewMapper.findBox(webform.getPane().getPageBox(), parentComponentRootElement);

                        if (box != null) {
//                            sm.selectComponents(new DesignBean[] { parent }, true);
//                            if (parent instanceof MarkupDesignBean) {
                                sm.selectComponents(new Element[] { parentComponentRootElement }, true);
                                found = true;

                                break;
//                            }
                        }
//                    }

//                    parent = parent.getBeanParent();
                    parentComponentRootElement = WebForm.getDomProviderService().getParentComponent(parentComponentRootElement);
                }

                if (!found && (bx != ancestor)) {
                    // Select the box itself, if different from ancestor
//                    sm.selectComponents(new DesignBean[] { bx.getDesignBean() }, true);
                    sm.selectComponents(new Element[] { CssBox.getElementForComponentRootCssBox(bx) }, true);
                }
            }
        }

        /**
           On mouse drag we need to handle the following cases:
           - If we're in inline editing mode, forward the mouse drag to
             the caret handler (where a drag is used to change the selected
             text range)
           - We may be dragging, resizing, or selection-swiping so update
             the handlers for these operations
         */
        public void mouseDragged(MouseEvent e) {
            currentPos.x = e.getX();
            currentPos.y = e.getY();

            if (interaction != null) {
                setInsertBox(null, null);
            }

            DesignerPane pane = webform.getPane();
//            DesignerCaret caret = pane.getCaret();
            
//            ModelViewMapper mapper = webform.getMapper();

//            if (!paletteItemSelected && (inlineEditor != null) && (caret != null)) {
//            if (!isCnCInProgress() && (inlineEditor != null) && (caret != null)) {
            if (!isCnCInProgress() && (inlineEditor != null) && pane.hasCaret()) {
//                caret.mouseDragged(e);
                pane.mouseDragged(e);

                return;
//            } else if (!paletteItemSelected && (insertModeBox != null) && (caret != null)
//            } else if (!isCnCInProgress() && (insertModeBox != null) && (caret != null)
            } else if (!isCnCInProgress() && (insertModeBox != null) && pane.hasCaret()
            && isInside(insertModeBox, e)) {
//                CssBox selBox = mapper.findBox(e.getX(), e.getY());
                CssBox selBox = ModelViewMapper.findBox(pane.getPageBox(), e.getX(), e.getY());

//                if (selBox.getDesignBean() == null) {
//                if (CssBox.getMarkupDesignBeanForCssBox(selBox) == null) {
                if (CssBox.getElementForComponentRootCssBox(selBox) == null) {
                    // Reroute the mouse press to the insert box
//                    caret.mouseDragged(e);
                    pane.mouseDragged(e);

                    return;
                } // else fall through: over something like a button in the insert box: let it

                // be selected in the normal way
//            } else if (!webform.getDocument().isGridMode() && (caret != null)) {
//            } else if (!webform.isGridModeDocument() && (caret != null)) {
//            } else if (!webform.isGridModeDocument() && pane.hasCaret()) {
            } else if (!webform.isGridMode() && pane.hasCaret()) {
//                caret.mouseDragged(e);
                pane.mouseDragged(e);

                // fall through
            }

            Point p = e.getPoint();
            translateMousePos(p, (Component)e.getSource());

            if (interaction != null) {
                interaction.mouseDragged(e);
            }

            // TODO Check for overlap with other components - if any
            // overlap is found, draw some kind of conflict icon
            // in the region! (This is only necessary when using layout
            // tables, not CSS2 - CSS2 allows overlap)
        }

        /**
           On mouse press we need to handle the following cases:
           - If it's a right click (platform dependent), and it's a
             popup trigger (which it will be on say Solaris but not
             Windows), popup a popup menu
           - If the mouse press is not over a component in the selection,
             the component should be selected
           - The mouse press can initiate various user operations depending
             on where the pointer is:
             * Over a component: this initiates a drag
             * Over a resize handle: this initiates a resize
             * Over the "background": this initiates a selection swipe
               operation
           - If we're in inline editing mode, forward the press event to the
             caret handler, which will use the event to move the caret and
             possibly immediately unselect its previous range)
         */
        public void mousePressed(MouseEvent e) {
            DesignerPane pane = webform.getPane();
//            ModelViewMapper mapper = webform.getMapper();

            pane.requestFocus();
            cancelCycleRequest();
            dontCycleInClickHandler = false;

            if (ENABLE_DOM_INSPECTOR) {
                if (isDomEvent(e)) {
                    // mouse clicked will take care of this but suspend
                    // normal click handling
                    return;
                }
            }

            //Point p = e.getPoint();
            Point p = SwingUtilities.convertPoint(e.getComponent(), e.getX(), e.getY(), pane);

            //translateMousePos(p, (Component)e.getSource());
            int x = p.x;
            int y = p.y;
            SelectionManager sm = webform.getSelection();
            PageBox pageBox = pane.getPageBox();
            int maxWidth = pageBox.getWidth();
            int maxHeight = pageBox.getHeight();
            int resize = sm.getSelectionHandleDir(x, y, maxWidth, maxHeight);

            CssBox selBox;
//            MarkupDesignBean sel;
            Element sel;

            if (resize == Cursor.MOVE_CURSOR) {
//                sel = sm.getSelectionHandleView(x, y, maxWidth, maxHeight);
                sel = sm.getSelectionHandleView(x, y, maxWidth, maxHeight);
//                selBox = mapper.findBox(sel);
                selBox = ModelViewMapper.findBoxForComponentRootElement(pageBox, sel);
            } else {
//                selBox = mapper.findBox(x, y);
                selBox = ModelViewMapper.findBox(pageBox, x, y);
//                sel = ModelViewMapper.findComponent(selBox);
//                sel = ModelViewMapper.findMarkupDesignBean(selBox);
                sel = ModelViewMapper.findElement(selBox);
            }

            if (inlineEditor != null) {
//                CssBox clickedBox = mapper.findBox(e.getX(), e.getY());
                CssBox clickedBox = ModelViewMapper.findBox(pageBox, e.getX(), e.getY());

                if (!inlineEditor.isEdited(clickedBox)) {
                    finishInlineEditing(false);
                }
            }

//            DesignerCaret caret = pane.getCaret();

//            if (!paletteItemSelected && (inlineEditor != null) && (caret != null)) {
//            if (!isCnCInProgress() && (inlineEditor != null) && (caret != null)) {
            if (!isCnCInProgress() && (inlineEditor != null) && pane.hasCaret()) {
//                if (((selBox.getDesignBean() == null) || (selBox == pageBox)) &&
//                if (((CssBox.getMarkupDesignBeanForCssBox(selBox) == null) || (selBox == pageBox)) &&
                if (((CssBox.getElementForComponentRootCssBox(selBox) == null) || (selBox == pageBox)) &&
                        inlineEditor.isEdited(selBox)) {
                    // Reroute the mouse press to the insert box
//                    caret.mousePressed(e);
                    pane.mousePressed(e);

                    //return;
                } // else fall through: over something like a button in the insert box: let it

                // be selected in the normal way
//            } else if (!paletteItemSelected && (insertModeBox != null) && (caret != null)
//            } else if (!isCnCInProgress() && (insertModeBox != null) && (caret != null)
            } else if (!isCnCInProgress() && (insertModeBox != null) && pane.hasCaret()
            && isInside(insertModeBox, e)) {
                // Reroute the mouse press to the insert box
//                caret.mousePressed(e);
                pane.mousePressed(e);

                //return;
                // else fall through: over something like a button in the insert box: let it
                // be selected in the normal way
//            } else if (!paletteItemSelected && !webform.getDocument().isGridMode()) {
//            } else if (!paletteItemSelected && !webform.isGridModeDocument()) {
//            } else if (!isCnCInProgress() && !webform.isGridModeDocument()) {
            } else if (!isCnCInProgress() && !webform.isGridMode()) {
//                if (caret == null) {
//                    caret = pane.getPaneUI().createCaret();
//                    pane.setCaret(caret);
//                }
                if (!pane.hasCaret()) {
                    pane.createCaret();
                }

//                caret.mousePressed(e);
                pane.mousePressed(e);

                // fall through
            }

            if (isMenuEvent(e)) {
                dontCycleInClickHandler = true;
                ensurePointSelected(e);

//                DesignerActions.menuPosX = e.getX();
//                DesignerActions.menuPosY = e.getY();
                menuPosX = e.getX();
                menuPosY = e.getY();

//                CssBox overBox = mapper.findBox(e.getX(), e.getY());
                CssBox overBox = ModelViewMapper.findBox(pageBox, e.getX(), e.getY());
//                RaveElement mouseElement = null;
                Element mouseElement = null;

                if (overBox != null) {
                    mouseElement = overBox.getElement();
                }

// <actions from layer>
//                DesignerActions actions = webform.getActions();
//                actions.createPopup(p.x, p.y, pane, sm.getSelectedNodes(), true, mouseElement);
// ====
                if (isInsideBoxDecoration(overBox, e.getX(), e.getY())) {
                    showDecorationPopupMenu(overBox, e.getComponent(), e.getX(), e.getY());
                } else {
//                    webform.getTopComponent().showPopupMenu(p.x, p.y);
//                    webform.tcShowPopupMenu(p.x, p.y);
                    webform.fireUserPopupActionPerformed(new DefaultDesignerPopupEvent(webform, e.getComponent(), null, null, p.x, p.y));
                }
// </actions from layer>

                return;
            }

            // In insert mode (component selected in the palette)
            // - let user swipe out a selection rectangle to set the
            // size of the inserted component
//            if (paletteItemSelected) {
            if (isCnCInProgress()) {
                setInsertBox(null, null);

                // XXX Marquee is not the right term here - we're
                // not wanting a selection. Make Marquee class more
                // flexible so we can set state which causes its
                // statusline updates to have the right icon, to
                // drop selection on release, to not use marching ants
                // (if the marquee starts using those for selection),
                // etc.
                // XXX Use subclass for this instead?
                CssBox gridBox = selBox.isGrid() ? selBox : null;
                sizer = new Marquee(webform, gridBox);
                sizer.setSelect(false);
//                sizer.setSnapToGrid(GridHandler.getInstance().snap());
//                sizer.setSnapToGrid(webform.getGridHandler().snap());
//                sizer.setSnapToGrid(GridHandler.getDefault().isSnap());
                sizer.setSnapToGrid(webform.isGridSnap());
                sizer.setInsertCursor(true);
                interaction = sizer;
                interaction.mousePressed(e);

                // TODO - status line position and size feedback?
                return;
            }

            if (y > sm.selectionViewPos) { // HACK FEATURE for now

                // User has selected one of the items in the selection hierarchy list
                sm.selectAncestor(x, y);

                return;
            }

            // Find out where the mouse press occurred.
            // If it's over a component - check modifier key, and
            // modify selection either by replacing it or adding to it
            // (or toggling a selected item).
            // Else, start a marque drag by forwarding to the marquee handler.
            if ((resize != Cursor.DEFAULT_CURSOR) && (resize != Cursor.MOVE_CURSOR)) {
//                MarkupDesignBean bean = sm.getSelectionHandleView(x, y, maxWidth, maxHeight);
                Element componentRootElement = sm.getSelectionHandleView(x, y, maxWidth, maxHeight);
//                MarkupDesignBean bean = WebForm.getDomProviderService().getMarkupDesignBeanForElement(componentRootElement);
//                CssBox box = mapper.findBox(bean);
                CssBox box = ModelViewMapper.findBoxForComponentRootElement(webform.getPane().getPageBox(), componentRootElement);
//                ArrayList bounds = mapper.getComponentRectangles(bean);
//                List bounds = ModelViewMapper.getComponentRectangles(webform.getPane().getPageBox(), bean);
                List bounds = ModelViewMapper.getComponentRectangles(webform.getPane().getPageBox(), componentRootElement);
                Rectangle a;

                if ((bounds != null) && (bounds.size() > 0)) {
                    a = (Rectangle)bounds.get(0);
                } else {
//                    a = mapper.getComponentBounds(bean);
//                    a = ModelViewMapper.getComponentBounds(webform.getPane().getPageBox(), bean);
                    a = ModelViewMapper.getComponentBounds(webform.getPane().getPageBox(), componentRootElement);
                }

                boolean preserveAspect =
//                    (Resizer.getResizeConstraints(webform, bean) &
//                        (Resizer.getResizeConstraints(webform, componentRootElement) &
//                    Constants.ResizeConstraints.MAINTAIN_ASPECT_RATIO) != 0;
                        Resizer.hasMaintainAspectRatioResizeConstraint(Resizer.getResizeConstraints(webform, componentRootElement));
//                interaction = new Resizer(webform, bean, box, resize, a, preserveAspect);
                interaction = new Resizer(webform, componentRootElement, box, resize, a, preserveAspect);
                interaction.mousePressed(e);
            } else {
                if (isLinkingEvent(e)) {
                    int size = 0;
                    List<Rectangle> selections = new ArrayList<Rectangle>(size);
                    List<CssBox> boxes = new ArrayList<CssBox>(size);
//                    ArrayList beans = new ArrayList(size);
                    Element[] componentRootElements = new Element[0];

                    //Iterator it = selected.iterator();
//                    dragger = new Dragger(webform, boxes, selections, beans);
                    dragger = new Dragger(webform, boxes, selections, componentRootElements);
                    interaction = dragger;
                    interaction.mousePressed(e);

                    return;
                }

                // See if user clicked on the "root" canvas (or a grid canvas)
                // or a component. Clicking on the background starts a marquee
                // selection whereas clicking on a component starts a drag
                // (or if you release over the same position, a selection.)
                // We want to allow marquee selection over grid areas if
                // control is pressed
                if ((sel == null) || (selBox == pane.getPageBox()) ||
                        (selBox.isGrid() && (e.isShiftDown() || e.isControlDown()))) {
                    setInsertBox(null, null);

                    if ((selBox != null) && selBox.isGrid()) {
                        // Start a Marquee selection
                        interaction = new Marquee(webform, selBox);
                        interaction.mousePressed(e);
                    } else {
                        // Clicking outside of components clears the selection
                        // Arguably the property sheet could show the nearest
                        // tag instead, e.g. at the caret position...
                        if (!sm.isSelectionEmpty()) {
                            sm.clearSelection(true);
                        }
                    }
                } else {
                    // Fix such that text selection swiping in grid
                    // mode isn't totally bjorken
                    if (((selBox.getBoxType() == BoxType.TEXT) ||
                            (selBox.getBoxType() == BoxType.SPACE)) &&
                            ((inlineEditor != null) && inlineEditor.isEdited(selBox))) {
                        // You've clicked on a text box as part of inline text editing;
                        // don't initiate a drag!
                        //dontCycleInClickHandler = true;
                        dontCycleInClickHandler = ensurePointSelected(e);

                        /* TODO  Hide caret when you've selected a component
                        // What if the caret we picked is outside when there IS
                        // an inside (albeit more distant) position we could have
                        // used?
                        if (selected != null && selected.size() > 0 &&
                                caret != null &&
                                caret.getDot() != Position.NONE) {
                            Position pos = caret.getDot();
                            Iterator it = selected.iterator();
                            boolean inside = false;
                            while (it.hasNext()) {
                                FormObject fob = (FormObject)it.next();
                                Element el = FacesSupport.getElement(fob.component);
                                if (pos.isInside(el)) {
                                    inside = true;
                                }
                            }
                            if (!inside) {
                                pane.hideCaret();
                            }
                        }
                         */
                        return; // XXX is this why I can't select boxes around text?
                    }

                    // Fix such that text selection swiping in grid
                    // mode isn't totally bjorken
                    if (((selBox.getBoxType() == BoxType.TEXT) || (selBox.getBoxType() == BoxType.SPACE))
                    && ((insertModeBox != null) && isBelow(insertModeBox, selBox))) {
                        // XXX #110083 Possible ClassCastException
//                        TextBox tb = (TextBox)selBox;
                        Text selBoxText;
                        DomPosition selBoxFirstPosition;
                        if (selBox instanceof TextBox) {
                            TextBox tb = (TextBox)selBox;
                            selBoxText = tb.getNode();
                            selBoxFirstPosition = tb.getFirstPosition();
                        } else if (selBox instanceof SpaceBox) {
                            SpaceBox sb = (SpaceBox)selBox;
                            selBoxText = sb.getNode();
                            selBoxFirstPosition = sb.getFirstPosition();
                        } else {
                            selBoxText = null;
                            selBoxFirstPosition = DomPosition.NONE;
                        }

                        if ((selBoxText != null)
//                                (DesignerUtils.checkPosition(tb.getFirstPosition(), false, /*webform*/webform.getManager().getInlineEditor()) != Position.NONE)) {
//                                (ModelViewMapper.isValidPosition(tb.getFirstPosition(), false, /*webform*/webform.getManager().getInlineEditor()))) {
                        && (ModelViewMapper.isValidPosition(webform, selBoxFirstPosition, false, /*webform*/webform.getManager().getInlineEditor()))) {
                            // You've clicked directly on text while in
                            // flow mode, and the text is not part of a
                            // jsf component: don't initiate a drag!
                            if (!sm.isBelowSelected(selBox) && !sm.isSelectionEmpty()) {
                                sm.clearSelection(true);
                            }

                            //dontCycleInClickHandler = true;
                            dontCycleInClickHandler = ensurePointSelected(e);

                            /* TODO  Hide caret when you've selected a component
                            // What if the caret we picked is outside when there IS
                            // an inside (albeit more distant) position we could have
                            // used?
                            // Hide caret when you select something without internal positions
                            if (selected != null && selected.size() > 0 &&
                                 caret != null &&
                                 caret.getDot() != Position.NONE) {
                                Position pos = caret.getDot();
                                Iterator it = selected.iterator();
                                boolean inside = false;
                                while (it.hasNext()) {
                                    FormObject fob = (FormObject)it.next();
                                    Element el = FacesSupport.getElement(fob.component);
                                    if (pos.isInside(el)) {
                                        inside = true;
                                    }
                                }
                                if (!inside) {
                                    pane.hideCaret();
                                }
                            }
                            */
                            return;
                        }
                    }

                    // See if we're resizing a table
                    interaction = selBox.getInternalResizer(x, y);

                    if (interaction != null) {
                        interaction.mousePressed(e);

                        return;
                    }

                    // Start a drag
                    // User has clicked on a component: either select or
                    // drag, depending on whether the current position
                    // is different from the original click position
                    //selectAt(e, false);
                    dontCycleInClickHandler = ensurePointSelected(e);

                    int size = sm.getNumSelected() + 1;
                    List<Rectangle> selections = new ArrayList<Rectangle>(size);
                    List<CssBox> boxes = new ArrayList<CssBox>(size);
//                    List<MarkupDesignBean> beans = new ArrayList<MarkupDesignBean>(size);
                    List<Element> componentRootElements = new ArrayList<Element>(size);
//                    Iterator it = sm.iterator();
//
//                    while (it.hasNext()) {
//                        MarkupDesignBean bean = (MarkupDesignBean)it.next();
                    for (Element componentRootElement : sm.getSelectedComponentRootElements()) {
//                        MarkupDesignBean bean = WebForm.getDomProviderService().getMarkupDesignBeanForElement(componentRootElement);
                        
//                        CssBox box = mapper.findBox(bean);
                        CssBox box = ModelViewMapper.findBoxForComponentRootElement(webform.getPane().getPageBox(), componentRootElement);

                        if (box == null) {
//                            ErrorManager.getDefault().log("No box found for element " + bean);
                            ErrorManager.getDefault().log("No box found for element=" + componentRootElement);

                            continue;
                        }

                        if (box == pane.getPageBox()) {
                            continue;
                        }

//                        Rectangle a = mapper.getComponentBounds(bean);
//                        Rectangle a = ModelViewMapper.getComponentBounds(webform.getPane().getPageBox(), bean);
                        Rectangle a = ModelViewMapper.getComponentBounds(webform.getPane().getPageBox(), componentRootElement);

                        if (a != null) {
                            addDragItem(selections, boxes, componentRootElements, /*beans,*/ a, p, box);
                        }
                    }

//                    if (!sm.isSelected(sel)) {
                    if (!sm.isSelected(sel)) {
                        // The currently clicked item is not in the selection
                        // set yet
//                        Rectangle a = mapper.findShape(sel);
//                        CssBox box = mapper.findBox(sel);
                        Rectangle a = ModelViewMapper.findShape(pageBox, sel);
//                        CssBox box = ModelViewMapper.findBox(pageBox, sel);
                        CssBox box = ModelViewMapper.findBoxForComponentRootElement(pageBox, sel);

                        if (a != null) {
                            addDragItem(selections, boxes, componentRootElements, /*beans,*/ a, p, box);
                        }
                    }

                    if (selections.size() > 0) {
//                        dragger = new Dragger(webform, boxes, selections, beans);
                        dragger = new Dragger(webform, boxes, selections, componentRootElements.toArray(new Element[componentRootElements.size()]));
                        interaction = dragger;
                        interaction.mousePressed(e);
                    }
                }
            }
        }

        /** If the given mouse event is not over a component in
         * the selection, select the component at that location
         * (subject to modifier keys for toggling rather than
         * add etc.)   It will also update the primary selection
         * object, if necessary, and in that case schedule a repaint.
         * @return true iff the selection was changed
         */
        private boolean ensurePointSelected(MouseEvent e) {
            // Make sure that the component is selected
//            ModelViewMapper mapper = webform.getMapper();
//            CssBox bx = mapper.findBox(e.getX(), e.getY());
            CssBox bx = ModelViewMapper.findBox(webform.getPane().getPageBox(), e.getX(), e.getY());

            /* TODO - this sorta works, but I've gotta suppress it
             * when you select a text node below a jsf component!
            // In flow mode, clicking on text should not select the
            // surrounding component
            if (!webform.getDocument().isGridMode() &&
                bx instanceof TextBox) {
                boolean old = isSelectionEmpty();
                clearSelection(true);
                return !old;
            }
            */
            SelectionManager sm = webform.getSelection();
            CssBox ancestor = null;

            if (bx != null) {
//                MarkupDesignBean boxMarkupDesignBean = CssBox.getMarkupDesignBeanForCssBox(bx);
//                if ((bx.getDesignBean() != null) && sm.isSelected(bx.getDesignBean())) {
                Element componentRootElement = CssBox.getElementForComponentRootCssBox(bx);
                if ((componentRootElement != null) && sm.isSelected(componentRootElement)) {
                    ancestor = bx;
                } else {
                    ancestor = sm.getSelectedAncestor(bx);
                }
            }

            // #94266 Toggle only on click not press or release
//            if ((ancestor == null) !! !isToggleEvent(e)) { // didn't click over part of the selection
//                selectAt(e, !isToggleEvent(e));
            if ((ancestor == null) && !isToggleEvent(e)) { // didn't click over part of the selection
                selectAt(e, true);

                return true;
            } else {
            
//                MarkupDesignBean ancestorMarkupDesignBean = CssBox.getMarkupDesignBeanForCssBox(ancestor);
//                if (sm.getPrimary() != ancestor.getDesignBean()) {
//                    sm.setPrimary(ancestor.getDesignBean());
                Element ancestorComponentRootElement = CssBox.getElementForComponentRootCssBox(ancestor);
                if (sm.getPrimary() != ancestorComponentRootElement) {
                    sm.setPrimary(ancestorComponentRootElement);
                    webform.getPane().repaint();
                }

                return false;
            }
        }

        private void addDragItem(List<Rectangle> selections, List<CssBox> boxes, List<Element> componentRootElements, /*List<MarkupDesignBean> beans,*/
            Rectangle r, Point p, CssBox box) {
//            MarkupDesignBean bean = box.getDesignBean();
//            MarkupDesignBean bean = CssBox.getMarkupDesignBeanForCssBox(box);
            Element componentRootElement = CssBox.getElementForComponentRootCssBox(box);
//            MarkupDesignBean pbean = findMovableParent(box);
//            MarkupDesignBean pbean = WebForm.getDomProviderService().getMarkupDesignBeanForElement(findMovableParentComponentRootElement(box));
            Element pComponentRootElement = findMovableParentComponentRootElement(box);

//            if (pbean == null) {
//                pbean = bean;
//            }
            if (pComponentRootElement == null) {
                pComponentRootElement = componentRootElement;
            }

//            if (pbean != bean) {
            if (pComponentRootElement != componentRootElement) {
                // We're an "unrepositionable" child in another container,
                // so we want to move the parent container.
//                bean = pbean;
                componentRootElement = pComponentRootElement;

                // Since the user may have chosen multiple children in
                // this container, make sure that we don't already have
                // the parent in the list - if so, just skip it
//                int n = beans.size();
//                for (int i = 0; i < n; i++) {
//                    if (beans.get(i) == bean) {
//                        return;
//                    }
//                }
                if (componentRootElements.contains(componentRootElement)) {
                    return;
                }

                // Change the view rectangle to reflect the parent instead
//                box = webform.getMapper().findBox(pbean);
//                box = ModelViewMapper.findBoxForComponentRootElement(webform.getPane().getPageBox(),
//                        WebForm.getDomProviderService().getComponentRootElementForMarkupDesignBean(pbean));
                box = ModelViewMapper.findBoxForComponentRootElement(webform.getPane().getPageBox(), pComponentRootElement);

                if (box == null) {
                    // This item not draggable for some reason
                    ErrorManager.getDefault().log("Didn't find box for " + pComponentRootElement); // NOI18N
                    return;
                }

                r = new Rectangle(box.getAbsoluteX(), box.getAbsoluteY(), box.getWidth(),
                        box.getHeight());
            }

            // XXX OUCH - is this going to break when the
            // view scrolls?
            // TODO - can I just modify r instead of creating
            // a new view here?
            Rectangle nr = new Rectangle(r.x - p.x, r.y - p.y, r.width, r.height);
            selections.add(nr);
            boxes.add(box);

            //assert bean == fo.component; // why search again??? pass it in!
//            beans.add(bean);
            componentRootElements.add(componentRootElement);
        }

        /** escape-like things: cancel dragging, select parent, etc. */
        public void escape() {
            DesignerPane pane = webform.getPane();
            PageBox pageBox = pane.getPageBox();
//            ModelViewMapper mapper = webform.getMapper();

            if (inlineEditor != null) {
                // Restore cursor:
                // XXX can I use my stashed position from cut & paste pointer tracking?  Also, JDK 1.5 has this directly lookupable!!
                // Don't have a mouse event: mouseMoved(e);
                pane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

                finishInlineEditing(true);
            } else if (ENABLE_DOM_INSPECTOR && (pageBox.getSelected() != null)) {
                CssBox parent = null;
                org.w3c.dom.Node parentNode = pageBox.getSelected().getElement().getParentNode();

                if (parentNode instanceof Element) {
//                    parent = mapper.findBox((Element)parentNode);
                    parent = ModelViewMapper.findBox(pageBox, (Element)parentNode);
                }

                if (parent == null) {
                    parent = pageBox.getSelected().getParent();
                }

                // Skip lineboxes since users shouldn't know they exist
                while (parent instanceof LineBox || parent instanceof LineBoxGroup) {
                    parent = parent.getParent();
                }

                if (parent != null) {
                    selectDomInspectorBox(parent);
                }

                return;
            }

            if (insertModeBox != null) {
                setInsertBox(null, null);

                // Restore cursor:
                // Don't have a mouse event: mouseMoved(e);
                // XXX can I use my stashed position from cut & paste pointer tracking?  Also, JDK 1.5 has this directly lookupable!!
                pane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                pane.repaint();
            } else if (interaction != null) {
                if (sizer != null) {
                    sizer = null;

//                    if (paletteItemSelected) { // XXX make part of sizer cleanup!
                    if (isCnCInProgress()) { // XXX make part of sizer cleanup!
//                        clearPalette();
                        stopCnC();
                    }
                }

                dragger = null;

                // Also get out of the insert mode
                interaction.cancel(pane);
                interaction = null;

                // Restore cursor:
                // Don't have a mouse event: mouseMoved(e);
                pane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
//            } else if (paletteItemSelected) {
            } else if (isCnCInProgress()) {
//                clearPalette();
                stopCnC();

                // Restore cursor:
                // Don't have a mouse event: mouseMoved(e);
                pane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            } else {
//                webform.getActions().selectParent();
//                DesignBean defaultSelectionBean = getDefaultSelectionBean();
//                if (defaultSelectionBean instanceof MarkupDesignBean) {
//                    SelectionManager.selectParent(WebForm.getDomProviderService().getComponentRootElementForMarkupDesignBean((MarkupDesignBean)defaultSelectionBean));
//                }
//                SelectionManager.selectParent(getDefaultSelectionComponentRootElement());
                webform.getSelection().selectComponent(getDefaultSelectionComponentRootElement());
            }
        }

        /**
           On mouse release we need to handle the following cases:
           - If it's a right click (platform dependent), and it's a
             popup trigger (which it will be on say Windows but not
             Solaris), popup a popup menu
           - If we were in a mouse-operation like dragging, resizing
             or selection swiping, that operation should be completed
           - If we're in inline editing mode, forward the press event
             to the caret handler, which uses the event to finalize
             the selection range swept during dragging)
        */
        public void mouseReleased(MouseEvent e) {
            DesignerPane pane = webform.getPane();
//            DesignerCaret caret = pane.getCaret();
//            ModelViewMapper mapper = webform.getMapper();

//            if (!paletteItemSelected && (inlineEditor != null) && (caret != null)) {
//            if (!isCnCInProgress() && (inlineEditor != null) && (caret != null)) {
            if (!isCnCInProgress() && (inlineEditor != null) && pane.hasCaret()) {
//                CssBox selBox = mapper.findBox(e.getX(), e.getY());
                CssBox selBox = ModelViewMapper.findBox(pane.getPageBox(), e.getX(), e.getY());

//                if ((selBox.getDesignBean() == null) && inlineEditor.isEdited(selBox)) {
//                if ((CssBox.getMarkupDesignBeanForCssBox(selBox) == null) && inlineEditor.isEdited(selBox)) {
                if ((CssBox.getElementForComponentRootCssBox(selBox) == null) && inlineEditor.isEdited(selBox)) {
                    // Reroute the mouse press to the insert box
//                    caret.mouseReleased(e);
                    pane.mouseReleased(e);

                    //return;
                } // else fall through: over something like a button in the insert box: let it

                // be selected in the normal way
//            } else if (!paletteItemSelected && (insertModeBox != null) && (caret != null)
//            } else if (!isCnCInProgress() && (insertModeBox != null) && (caret != null)
            } else if (!isCnCInProgress() && (insertModeBox != null) && pane.hasCaret()
            && isInside(insertModeBox, e)) {
//                CssBox selBox = mapper.findBox(e.getX(), e.getY());
                CssBox selBox = ModelViewMapper.findBox(pane.getPageBox(), e.getX(), e.getY());

//                if (selBox.getDesignBean() == null) {
//                if (CssBox.getMarkupDesignBeanForCssBox(selBox) == null) {
                if (CssBox.getElementForComponentRootCssBox(selBox) == null) {
                    // Reroute the mouse press to the insert box
//                    caret.mouseReleased(e);
                    pane.mouseReleased(e);

                    //return;
                } // else fall through: over something like a button in the insert box: let it

                // be selected in the normal way
//            } else if (!webform.getDocument().isGridMode() && (caret != null)) {
//            } else if (!webform.isGridModeDocument() && (caret != null)) {
//            } else if (!webform.isGridModeDocument() && pane.hasCaret()) {
            } else if (!webform.isGridMode() && pane.hasCaret()) {
//                caret.mouseReleased(e);
                pane.mouseReleased(e);

                // fall through
            }

            // XXX Do I have to compute a translated position here?
            // What if you release over a JButton?
            if (sizer != null) {
                // TODO Compute the size from the marquee!
                Rectangle r = sizer.getBounds();
                interaction.mouseReleased(e); // TODO - no selection here!
                interaction = null;
                sizer = null;

//                if (paletteItemSelected) {
                if (isCnCInProgress()) {
                    // XXX We need to get rid of this dep, change impl.
//                    Object item = getPaletteItem();
                    Transferable item = cncTransferable;
//                    clearPalette();
                    stopCnC();

                    // Insert Component
                    DndHandler dth = pane.getDndHandler();
                    dontCycleInClickHandler = true;

                    Point ep;

                    if ((r.width == 0) && (r.height == 0)) {
                        // Not insert-sizing, just dropping at the given coordinate:
                        // don't use sizer's position (which is generally snapped to grid)
                        ep = e.getPoint();
                    } else {
                        ep = new Point(r.x, r.y);
                    }

                    updateDropState(ep, true, null);

                    translateMousePos(ep, (Component)e.getSource());

                    Dimension s = r.getSize();

                    if ((s.width > 2) || (s.height > 2)) {
                        dth.setDropSize(s);
                    }

                    Transferable transferable = null;

                    if (item == null) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, 
                                new IllegalStateException("Invalid item=" + item)); // NOI18N
                    } else {
                        transferable = item;
                    }
//                    else if (item instanceof PaletteItem) {
//                        transferable =
//                            PaletteItemTransferable.createTransferable((PaletteItem)item); //component
//                    }

                    int dropType = dth.getDropType(ep, transferable, false);

                    if (dropType != DndHandler.DROP_DENIED) {
//                        DndHandler.setActiveTransferable(transferable);

                        // Drop it
                        if (transferable != null) {
                            dth.importData(pane, transferable);
                        }
                    }

                    // Restore cursor:
                    mouseMoved(e);
                }
            } else if (dragger != null) {
                // Handle dragging and selection of a new component
                // in the same place since clicking and releasing over
                // a component is both a (0,0) drag and selecting a new
                // component :)
                boolean hasMoved = dragger.hasMoved();
                interaction.mouseReleased(e);
                interaction = null;
                dragger = null;

                SelectionManager sm = webform.getSelection();

                // #94266 Toggle only on click not press or release
//                if (!hasMoved && !isToggleEvent(e) && (sm.getNumSelected() > 1)) {
//                    // You've pressed the mouse button when there
//                    // was more than one selected component, but
//                    // you didn't drag and didn't press a toggle
//                    // key: in this case you want to select only
//                    // the component that received the click.
//                    sm.clearSelection(false);
//                    selectAt(e, false);
//                }
            } else if (interaction != null) {
                interaction.mouseReleased(e);
                interaction = null;
            }

            // TODO: transfer to the renderers
            //StatusDisplayer.getDefault().clearPositionLabel();
        }

        /**
         * Select the component at a position pointed to by the mouse event.
         * This typically finds the "closest" (e.g. furthest down the
         * element tree) match; however, if an ancestor of the element
         * at the mouse position is already selected, we will operate on
         * it instead.
         *
         * @param e Mouse event. The two fields that are considered are
         *   the position, and the source. The source is used to translate
         *   the position to the coordinate system of the design pane,
         *   and the point is obviously used to determine which component
         *   in the pane is being pointed at.
         * @param keepAncestorSelection This flag controls whether a mouse
         *   click on a component that is contained within a selection
         *   should unselect the parent and select the closest element,
         *   or leave the parent selected. For example, when you right
         *   click, you want to keep the parent selection, but when you
         *   left click, you don't.
         */
        public void selectAt(MouseEvent e, boolean keepAncestorSelection) {
            // User has clicked on a component: either select or
            // drag, depending on whether the current position
            // is different from the original click position
            Point p = e.getPoint();
            translateMousePos(p, (Component)e.getSource());

            // TODO - consider impact of component hierarchy changes in GridHandler
            int x = p.x;
            int y = p.y;

            SelectionManager sm = webform.getSelection();

            if (p.y > sm.selectionViewPos) {
                sm.selectAncestor(p.x, p.y);
            }

//            MarkupDesignBean component = null;
            Element component = null;
            
//            ModelViewMapper mapper = webform.getMapper();
//            CssBox bx = mapper.findBox(x, y);
            CssBox bx = ModelViewMapper.findBox(webform.getPane().getPageBox(), x, y);

            if ((bx != null) && !(bx instanceof PageBox)) { // can't select body
//                component = ModelViewMapper.findComponent(bx);
//                component = ModelViewMapper.findMarkupDesignBean(bx);
                component = ModelViewMapper.findComponentRootElement(bx);
            }

            if (component != null) {
//                sm.setPrimary(component);
                sm.setPrimary(component);

//                CssBox sel = mapper.findBox(component);
                CssBox sel = ModelViewMapper.findBoxForComponentRootElement(webform.getPane().getPageBox(), component);
                CssBox insertBox = null;
                CssBox ancestor;

                if (keepAncestorSelection) {
                    ancestor = sm.getSelectedAncestor(sel);

                    if ((ancestor != null) && (ancestor != sel)) {
//                        component = ancestor.getDesignBean();
//                        component = CssBox.getMarkupDesignBeanForCssBox(ancestor);
                        component = CssBox.getElementForComponentRootCssBox(ancestor);
                    }
                } else {
//                    ancestor = sm.isSelected(component) ? sel : null;
                    ancestor = sm.isSelected(component) ? sel : null;
                }

                boolean alreadySelected = ancestor != null;

                // I would -like- to do control-select to toggle
                // selection - but many IDEs use the shift key for this.
                // Also, control-click often means right-click
                // (context menu) - on OSX with a one button mouse it's
                // how you get the popup menu for example.
                if (isToggleEvent(e)) {
                    dontCycleInClickHandler = true;

                    // Toggle selected component
                    if (alreadySelected) {
//                        sm.removeSelected(component, false);
                        sm.removeSelected(component, false);
                    } else {
//                        sm.addSelected(component, false);
                        sm.addSelected(component, false);
                    }
                } else if (alreadySelected && (e.getButton() == 1) /* &&
                    (isInlineEditable(component))*/) {
                    // Possibly go into inline mode (unless this component
                    // does not support that
                    insertBox = ancestor;
                } else {
                    // Replace selection
                    if (!alreadySelected) {
                        sm.clearSelection(false);
//                        sm.addSelected(component, false);
                        sm.addSelected(component, false);
                    }
                }

                insertBox = findInsertBox(insertBox);
                setInsertBox(insertBox, e);
                
//                sm.updateSelection();
                sm.updateSelectionImmediate();
                
                webform.getPane().repaint();

                // XXX I stopped disabling selection in clearSelection for false;
                // do I have to reenable this here?
                selectedBox = null;
            } else {
                if (bx != null) {
                    bx = findInsertBox(bx);

                    if (selectedBox == bx) {
                        setInsertBox(bx, e);
                    } else {
                        // Clicked outside of a component: clear selection
                        setInsertBox(null, null);
                    }
                } else {
                    setInsertBox(null, null);
                }

                selectedBox = bx;
                
                // #6450569 Nodes needs to update immediatelly.
//                sm.clearSelection(true);
                sm.clearSelectionImmediate();
            }
        }

        public void mouseEntered(MouseEvent e) {
//            if (!webform.getModel().isValid()) {
            if (!webform.isModelValid()) {
                return;
            }

//            // Check to see if a selection is potentially in effect
//            paletteItemSelected = false;

//            PaletteComponentModel palette = PaletteComponentModel.getInstance();
//
//            if ((palette.getSelectedPalette() != null) &&
//                    (palette.getSelectedPalette().getSelectedPaletteSection() != null) &&
//                    (palette.getSelectedPalette().getSelectedPaletteSection()
//                                .getSelectedPaletteItem() != null)) {
//                PaletteItem item =
//                    palette.getSelectedPalette().getSelectedPaletteSection().getSelectedPaletteItem();
//
//                if (item instanceof BeanPaletteItem) {
//                    BeanPaletteItem bitem = (BeanPaletteItem)item;
//
//                    BeanCreateInfo bci = bitem.getBeanCreateInfo();
//                    BeanCreateInfoSet bcis = bitem.getBeanCreateInfoSet();
//
//                    // At most one of the above should be set...
//                    assert !((bci != null) && (bcis != null));
//
//                    if (bcis != null) {
//                        // Set us up for multiple bean creation
//                        paletteItems = bcis.getBeanClassNames();
//                    } else if (bci != null) {
//                        paletteItems = new String[] { bci.getBeanClassName() };
//                    } else {
//                        paletteItems = new String[] { bitem.getBeanClassName() };
//                    }
//                } else {
//                    paletteItems = null;
//                }
//
//                paletteItemSelected = true;
//
//                return;
//            }

//            getSelectionFromPalette();
            if (e.getButton() == e.NOBUTTON) {
                // XXX #6245208 This is not DnD, allow CnC.
                setIgnoreCnC(false);
            }
            tryStartCnC();
        }
        
//        private void getSelectionFromPalette() {
////            TopComponent comp = TopComponent.getRegistry().getActivated();
////            if ((comp != null) && (comp != webform.getTopComponent()) &&
////                    (comp instanceof ExplorerManager.Provider)) {
////                ExplorerManager.Provider provider = (ExplorerManager.Provider)comp;
////                ExplorerManager m = provider.getExplorerManager();
////
////                Node[] nodes = m.getSelectedNodes();
////
////                for (int i = 0; (nodes != null) && (i < nodes.length); i++) {
////                    RavePaletteItemSetCookie pisc =
////                        (RavePaletteItemSetCookie)nodes[i].getCookie(RavePaletteItemSetCookie.class);
////
////                    if (pisc != null) {
////                        if (pisc.hasPaletteItems()) {
////                            paletteItemSelected = true;
////                            selectedManager = m;
////                            paletteItems = pisc.getClassNames();
////
////                            return;
////                        }
////                    }
////                }
////            }
//            TopComponent commonPalette = findCommonPaletteWindow();
//            if (commonPalette != null && commonPalette == TopComponent.getRegistry().getActivated()) {
//                Lookup selectedItem = webform.getPaletteController().getSelectedItem();
////                String[] classNames = getClassNamesFromPaletteItem(selectedItem);
////                if (classNames.length > 0) {
////                    paletteItemSelected = true;
//////                    selectedManager = m;
//////                    paletteItems = classNames;
////                }
//                cncTransferable = getTransferableFromPaletteItem(selectedItem);
//            } else {
//                cncTransferable = null;
//            }
////            paletteItemSelected = cncTransferable != null;
//        }
        
        
        
//        /**
//         * Locate whichever palette items are selected in other components,
//         * and return these. It will return a PaletteItem, if one is
//         * found directly, or a Transferable, if the Node only provides
//         * that.
//         */
//        private Transferable getSelectedPaletteItemTransferable() {
////            PaletteComponentModel palette = PaletteComponentModel.getInstance();
////
////            if ((palette.getSelectedPalette() != null) &&
////                    (palette.getSelectedPalette().getSelectedPaletteSection() != null) &&
////                    (palette.getSelectedPalette().getSelectedPaletteSection()
////                                .getSelectedPaletteItem() != null)) {
////                return palette.getSelectedPalette().getSelectedPaletteSection()
////                              .getSelectedPaletteItem();
////            } else 
//            
////            if (selectedManager != null) {
////                Node[] nodes = selectedManager.getSelectedNodes();
////
////                if (nodes == null) {
////                    return null;
////                }
////
////                for (int i = 0; i < nodes.length; i++) {
////                    RavePaletteItemSetCookie pisc =
////                        (RavePaletteItemSetCookie)nodes[i].getCookie(RavePaletteItemSetCookie.class);
////
////                    if ((pisc != null) && pisc.hasPaletteItems()) {
////                        // instead provide items directly, but that
////                        // means PaletteItem has to move into openide
////                        // etc. so I'll revisit this after TP.
////                        // (In addition to inefficiency, the problem with
////                        // this is it actually affects the clipbard.)
////                        try {
////                            return nodes[i].clipboardCopy();
////                        } catch (Exception ex) {
////                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
////                            return null;
////                        }
////                    }
////                }
////            }
////
////            return null;
//            Lookup selectedItem = webform.getPaletteController().getSelectedItem();
//            return getTransferableFromPaletteItem(selectedItem);
//        }

//        private void clearPalette() {
//            paletteItemSelected = false;
//
////            PaletteComponentModel palette = PaletteComponentModel.getInstance();
////
////            if ((palette.getSelectedPalette() != null) &&
////                    (palette.getSelectedPalette().getSelectedPaletteSection() != null) &&
////                    (palette.getSelectedPalette().getSelectedPaletteSection()
////                                .getSelectedPaletteItem() != null)) {
////                palette.getSelectedPalette().getSelectedPaletteSection().setSelectedPaletteItem(null);
////            } else 
//            if (selectedManager != null) {
//                try {
//                    selectedManager.setSelectedNodes(new Node[0]);
//                } catch (Exception e) {
//                    ErrorManager.getDefault().notify(e);
//                }
//
//                selectedManager = null;
//            }
//
//            paletteItems = null;
//        }
        
//        private void doClearPalette() {
////            TopComponent comp = TopComponent.getRegistry().getActivated();
////            if ((comp != null) && (comp instanceof ExplorerManager.Provider)) {
////                ExplorerManager.Provider provider = (ExplorerManager.Provider)comp;
////                final ExplorerManager m = provider.getExplorerManager();
////    
////                Node[] nodes = m.getSelectedNodes();
////    
////                for (int i = 0; (nodes != null) && (i < nodes.length); i++) {
////                    RavePaletteItemSetCookie pisc =
////                        (RavePaletteItemSetCookie)nodes[i].getCookie(RavePaletteItemSetCookie.class);
////    
////                    if (pisc != null) {
////                        if (pisc.hasPaletteItems()) {
////                            SwingUtilities.invokeLater(new Runnable() {
////                                public void run() {
////                                    try {
////                                        m.setSelectedNodes(new Node[0]);
////                                    } catch (Exception e) {
////                                        ErrorManager.getDefault().notify(e);
////                                    }
////                                }
////                            });
////                        }
////                    }
////                }
////            }
//
////            paletteItemSelected = false;
//
////            TopComponent commonPalette = findCommonPaletteWindow();
////            if (commonPalette != null && commonPalette == TopComponent.getRegistry().getActivated()) {
////                webform.getPaletteController().clearSelection();
////            }
//
////            paletteItems = null;
//            cncTransferable = null;
//        }

        /** Tries to start the CnC (Click and Click) operation.
         * Currently it simulates the operation from common palette or runtime window. */
        private void tryStartCnC() {
            if (isIgnoreCnC()) {
                // XXX #6245208 Should mean DnD is in progress or was and was escaped.
                setIgnoreCnC(false);
                cncTransferable = null;
                return;
            }
            
            TopComponent activated = TopComponent.getRegistry().getActivated();
            if (activated == null) {
                cncTransferable = null;
                return;
            }
            
            if (activated == findCommonPaletteWindow()) {
                cncTransferable = getTransferableFromCommonPalette(webform);
            } else if (activated instanceof ExplorerManager.Provider) {
                cncTransferable = getTransferableFromExplorer(activated);
            } else {
                cncTransferable = null;
            }
        }
        
        private boolean isCnCInProgress() {
            return cncTransferable != null;
        }
        
        /** Stops CnC (Click and Click) operation. */
        private void stopCnC() {
            cncTransferable = null;
            // XXX #6476367 They want to also deselect the palette item, like it was before.
            clearPaletteSelection();
        }
        
        private void clearPaletteSelection() {
            WebForm webForm = InteractionManager.this.webform;
            if (webForm == null) {
                return;
            }
            PaletteController paletteController = webForm.getPaletteController();
            if (paletteController == null) {
                return;
            }
            paletteController.clearSelection();
        }
        

        public void mouseExited(MouseEvent e) {
            //clearCurrentPos();
        // Don't clear out the menu position, since mouseExited will be
            // called when the popup menu pops up too and we don't want this
            // to clear the menu position we're trying to stash away
            currentPos.x = -1;
            currentPos.y = -1;

//            paletteItemSelected = false;
//            selectedManager = null;
            stopCnC();

//            if (!webform.getModel().isValid()) {
            if (!webform.isModelValid()) {
                return;
            }

            webform.getPane().getDndHandler().clearDropMatch();
        }

        // track the moving of the mouse.
        public void mouseMoved(MouseEvent e) {
            // When pages are deleted mouse motion can happen over
            // the page briefly before it's taken down; ensure that
            // this is not the case here so we don't try to look at
            // the model
//            if (!webform.getModel().isValid()) {
            if (!webform.isModelValid()) {
                return;
            }

            currentPos.x = e.getX();
            currentPos.y = e.getY();

            // When debugging mouse and findview events, mouse motion
            // triggering events is really annoying.
            if (SKIP_MOUSE_MOTION) {
                return;
            }

            if (dragger != null) {
                // HACK
                // Workaround for bug on Mac OSX where if the control
                // key is pressed when we start dragging, we receive mouse
                // moved events rather than mouse dragged events.
                // They should however be treated as mouse drags.
                try {
                    interaction.mouseDragged(e);
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ex);
                    escape();
                }

                return;
            }

            // Change the cursor to MOVE cursor over components.
            // Change it to directions over selection handles.
            // Change it to insert-cursor over flow-layout panels
            //   (such as table cells)
            // Change it to table-column-resize-cursor over table borders.
            // Change it to pos-insert-cursor when a selection has
            //   been made in the toolbox so clicking will dropp
            //   (and dragging will drop & resize to drag area)
            // User has clicked on a component: either select or
            // drag, depending on whether the current position
            // is different from the original click position
            Point p = e.getPoint();

//            if ((sizer != null) || paletteItemSelected) {
            if ((sizer != null) || isCnCInProgress()) {
                updateDropState(p, false, null);

                return;
            }

            translateMousePos(p, (Component)e.getSource());

            int x = p.x;
            int y = p.y;

            // First see if we're over a selection dragbar. If so,
            // show resize cursor
            SelectionManager sm = webform.getSelection();
            DesignerPane pane = webform.getPane();
            PageBox pageBox = pane.getPageBox();
            int maxWidth = pageBox.getWidth();
            int maxHeight = pageBox.getHeight();
            int resize = sm.getSelectionHandleDir(x, y, maxWidth, maxHeight);

            if (resize != Cursor.DEFAULT_CURSOR) {
                pane.setCursor(Cursor.getPredefinedCursor(resize));

                return;
            }

            // Else, see if we're over a component. If so, show motion
            // cursor.
//            ModelViewMapper mapper = webform.getMapper();
//            CssBox selBox = mapper.findBox(x, y);
//            DesignBean sel = mapper.findComponent(x, y);
            CssBox selBox = ModelViewMapper.findBox(pageBox, x, y);
//            MarkupDesignBean selMarkupDesignBean = CssBox.getMarkupDesignBeanForCssBox(selBox);
            Element selComponentRootElement = CssBox.getElementForComponentRootCssBox(selBox);
//            DesignBean sel = ModelViewMapper.findComponent(pageBox, x, y);
//            DesignBean sel = ModelViewMapper.findMarkupDesignBean(pageBox, x, y);
            Element sel = ModelViewMapper.findElement(pageBox, x, y);
            
            int internalResize = selBox.getInternalResizeDirection(x, y);
            
            if (internalResize != Cursor.DEFAULT_CURSOR) {
                pane.setCursor(Cursor.getPredefinedCursor(internalResize));
            } else if ((selectedBox != null) && isInside(selectedBox, e)) {
                pane.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
            } else if ((inlineEditor != null) && inlineEditor.isEdited(selBox)) {
                // If you're pointing at an inline component like a button within a flow,
                // make it selectable. But if you're pointing below a rendered component that
                // has focus, like an output text, show a text cursor.
//                if ((selBox != pageBox) && (selBox.getDesignBean() != null)) {
//                if ((selBox != pageBox) && (selMarkupDesignBean != null)) {
                if ((selBox != pageBox) && (selComponentRootElement != null)) {
                    pane.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                } else {
                    pane.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
                }
            } else if ((insertModeBox != null) && isInside(insertModeBox, e)) {
                // If you're pointing at an inline component like a button within a flow,
                // make it selectable. But if you're pointing below a rendered component that
                // has focus, like an output text, show a text cursor.
//                if ((selBox != pageBox) && (selBox.getDesignBean() != null)) {
//                if ((selBox != pageBox) && (selMarkupDesignBean != null)) {
                if ((selBox != pageBox) && (selComponentRootElement != null)) {
                    pane.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                } else {
                    pane.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
                }
            } else if ((sel != null) && (selBox != pageBox)) {
                pane.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            } else {
                pane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }

    } // End of MouseHandler.

    
    private static class DefaultDesignerClickEvent implements DesignerClickEvent {
        private boolean consumed;
        private final int clickCount;
        private final Designer designer;
        private final Box box;
        
        public DefaultDesignerClickEvent(Designer designer, Box box, int clickCount) {
            this.designer = designer;
            this.box = box;
            this.clickCount = clickCount;
        }

        public boolean isConsumed() {
            return consumed;
        }

        public void consume() {
            consumed = true;
        }

        public int getClickCount() {
            return clickCount;
        }

        public Designer getDesigner() {
            return designer;
        }

        public Box getBox() {
            return box;
        }
        
    } // DefaultDesignerClickEvent.
    

    /** XXX Copied from DesignerActions and SelectionManager later. */
    private static boolean canSelectParent(/*DesignBean designBean*/Element componentRootElement) {
        if (componentRootElement == null) {
            return false;
        }

//        DesignBean designBean = WebForm.getDomProviderService().getMarkupDesignBeanForElement(componentRootElement);
//        if (designBean == null) {
//            return false;
//        }
//        
//        DesignBean parent = designBean.getBeanParent();
//
////        WebForm webform = WebForm.findWebFormForDesignContext(designBean.getDesignContext());
////        if (webform == null) {
////            return false;
////        }
////        FacesModel model = webform.getModel();
//        DesignContext designContext = designBean.getDesignContext();
//        if (designContext == null) {
//            return false;
//        }
        Element parentComponentRootElement = WebForm.getDomProviderService().getParentComponent(componentRootElement);

//        while (parent != null) {
        if (parentComponentRootElement != null) {
//            if (parent == model.getRootBean()) {
//            if (parent == designContext.getRootContainer()) {
            if (WebForm.getDomProviderService().isRootContainerComponent(parentComponentRootElement)) {
                return false;
            }

//            if (Util.isSpecialBean(/*webform, */parent)) {
//            if (parent instanceof MarkupDesignBean && WebForm.getDomProviderService().isSpecialComponent(
//                    WebForm.getDomProviderService().getComponentRootElementForMarkupDesignBean((MarkupDesignBean)parent))) {
            if (WebForm.getDomProviderService().isSpecialComponent(parentComponentRootElement)) {
                return false;
            }

            /* No longer necessary
            if (unit.isVisualBean(parent)) {
                return true;
            } else {
                parent = parent.getLiveParent();
            }
             */
            return true;
        }

        return false;
    }
    
    
//    // XXX Moved from FacesSupport.
//    /** Locate the closest mouse region to the given element */
//    private static MarkupMouseRegion findRegion(Element element) {
//        while (element != null) {
////            if (element.getMarkupMouseRegion() != null) {
////                return element.getMarkupMouseRegion();
////            }
////            MarkupMouseRegion region = InSyncService.getProvider().getMarkupMouseRegionForElement(element);
//            MarkupMouseRegion region = WebForm.getDomProviderService().getMarkupMouseRegionForElement(element);
//            if (region != null) {
//                return region;
//            }
//
//            if (element.getParentNode() instanceof Element) {
//                element = (Element)element.getParentNode();
//            } else {
//                break;
//            }
//        }
//
//        return null;
//    }
    
    private static void processDefaultDecorationAction(final CssBox box) {
        Decoration decoration = box.getDecoration();
        if (decoration == null) {
            return;
        }
        
        Action defaultAction = decoration.getDefaultAction();
        final Action action;
        if (defaultAction instanceof ContextAwareAction) {
            action = ((ContextAwareAction)defaultAction).createContextAwareInstance(decoration.getContext());
        } else {
            action = defaultAction;
        }
        
        if (action != null) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    action.actionPerformed(new ActionEvent(box, -1, null));
                }
            });
        }
    }
    
    private void showDecorationPopupMenu(final CssBox box, Component component, int x, int y) {
        Decoration decoration = box.getDecoration();
        if (decoration == null) {
            return;
        }
        
        Action[] actions = decoration.getActions();
        if (actions.length == 0) {
            return;
        }
        
//        JPopupMenu popup = Utilities.actionsToPopup(actions, decoration.getContext());
//        
//        // XXX Move somehow to the associated top component?
////        popup.show(webform.getTopComponent(), x, y);
//        webform.tcShowPopupMenu(popup, x, y);
        webform.fireUserPopupActionPerformed(new DefaultDesignerPopupEvent(webform, component, actions, decoration.getContext(), x, y));
    }
    
    
    static class DefaultDesignerPopupEvent implements DesignerPopupEvent {
        private final Designer designer;
        private final Component component;
        private final Action[] actions;
        private final Lookup context;
        private final int x;
        private final int y;

        public DefaultDesignerPopupEvent(
                Designer designer,
                Component component,
                Action[] actions,
                Lookup context,
                int x,
                int y
        ) {
            this.designer = designer;
            this.component = component;
            this.actions = actions;
            this.context = context;
            this.x = x;
            this.y = y;
        }
        
        public Component getComponent() {
            return component;
        }

        public Action[] getActions() {
            return actions;
        }

        public Lookup getContext() {
            return context;
        }

        public Designer getDesigner() {
            return designer;
        }

        public Box getBox() {
            return null;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
        
    } // End of DefaultDesignerPopupEvent.
    
    
    /** Put the designer in inline-editing mode for the first
     * eligible component in the inserted set of beans  */
//    void inlineEdit(final List beans) {
    void inlineEdit(final Element[] componentRootElements) {
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
//                    int n = beans.size();
//                    for (int i = 0; i < n; i++) {
//                        DesignBean lb = (DesignBean)beans.get(i);
                    for (Element componentRootElement : componentRootElements) {
//                        if (!(lb instanceof MarkupDesignBean)) {
//                            continue;
//                        }
                        if (componentRootElement == null) {
                            continue;
                        }

                        boolean editing =
//                            startInlineEditing((MarkupDesignBean)lb, null,
                                startInlineEditing(componentRootElement, null, null, false, true, null, true);

                        if (editing) {
                            return;
                        }

                        // See if any of the first level children are willing to inline edit as well
                        // For example, for a hyperlink or a commandlink, we want the child output text
                        // to be inline edited. For a TabSet we want the Tab to be edited.
                        // We could consider searching recursively here but that's probably overkill.
//                        for (int j = 0, m = lb.getChildBeanCount(); j < m; j++) {
//                            DesignBean lbc = lb.getChildBean(j);
                        Element[] children = WebForm.getDomProviderService().getChildComponents(componentRootElement);
                        for (Element child : children) {

//                            if (lbc instanceof MarkupDesignBean) {
//                                MarkupDesignBean mlbc = (MarkupDesignBean)lbc;
                            if (child != null) {

//                                editing =
//                                    startInlineEditing(mlbc, null, null,
//                                        false, true, null, true);
                                editing = startInlineEditing(child, null, null, false, true, null, true);
                                if (editing) {
                                    return;
                                }
                            }
                        }
                    }
                }
            });
    }

//    private static void paintVirtualForms(WebForm webform, Graphics2D g) {
//        VirtualFormRenderer virtualFormRenderer = (VirtualFormRenderer)Lookup.getDefault().lookup(VirtualFormRenderer.class);
//        if (virtualFormRenderer == null) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
//                    new NullPointerException("No VirtualFormRenderer is available!")); // NOI18N
//        } else {
//            virtualFormRenderer.paintVirtualForms(g, new RenderContextImpl(webform));
//        }
//    }

    
//    private static class RenderContextImpl implements /*VirtualFormRenderer*/DomProvider.RenderContext {
//        private final WebForm webForm;
//        
//        public RenderContextImpl(WebForm webForm) {
//            this.webForm = webForm;
//        }
//        
////        public DesignContext getDesignContext() {
////            return webForm.getModel().getLiveUnit();
////        }
////        public DesignBean[] getBeansOfType(Class clazz) {
////            return webForm.getBeansOfType(clazz);
////        }
//
////        public Project getProject() {
////            return webForm.getProject();
////        }
//        
//        public Dimension getVieportDimension() {
//            return webForm.getPane().getPageBox().getViewport().getExtentSize();
//        }
//
//        public Point getViewportPosition() {
//            return webForm.getPane().getPageBox().getViewport().getViewPosition();
//        }
//
//        public int getNonTabbedTextWidth(char[] s, int offset, int length, FontMetrics metrics) {
//            return DesignerUtils.getNonTabbedTextWidth(s, offset, length, metrics);
//        }
//
//        public Rectangle getBoundsForComponent(Element componentRootElement) {
//            if (componentRootElement != null) {
//                return ModelViewMapper.getComponentBounds(webForm.getPane().getPageBox(), componentRootElement);
//            } else {
//                return null;
//            }
//        }
//    } // End of RenderContextImpl.


    // XXX Moved from Document.
    /**
     * Report whether the given node is in a read-only region of
     * the document or not.
     */
//    static boolean isReadOnlyRegion(Position pos) {
    static boolean isReadOnlyRegion(DomPosition pos) {
        org.w3c.dom.Node node = pos.getNode();

        // XXX FIXME Determine if this node is in a DocumentFragment which means
        // it's read only
        while (node != null) {
            node = node.getParentNode();

            if (node instanceof org.w3c.dom.Document) {
                break;
            }
        }

        return node == null;
    }
    
    private static TopComponent findCommonPaletteWindow() {
        return findTopComponentOfId("CommonPalette"); // NOI18N
    }

//    private static TopComponent findRuntimeWindow() {
//        return findTopComponentOfId("runtime"); // NOI18N
//    }
    
    private static TopComponent findTopComponentOfId(String id) {
        TopComponent tc = WindowManager.getDefault().findTopComponent(id);
        if (tc == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new NullPointerException("TopComponent of id=" + id + " not found!")); // NOI18N
            return null;
        }
        return tc;
    }

    private static Transferable getTransferableFromCommonPalette(WebForm webForm) {
        Lookup paletteItem = webForm.getPaletteController().getSelectedItem();
        if (paletteItem == null) {
            return null;
        }
        
        Node node = (Node)paletteItem.lookup(Node.class);
        if (node == null) {
            return null;
        }
        try {
            return node.clipboardCopy();
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        return null;
    }

    private static Transferable getTransferableFromExplorer(TopComponent tc) {
        if (tc instanceof ExplorerManager.Provider) {
            ExplorerManager.Provider explorerManagerProvider = (ExplorerManager.Provider)tc;
            Node[] selectedNodes = explorerManagerProvider.getExplorerManager().getSelectedNodes();
            if (selectedNodes != null && selectedNodes.length == 1) {
                // XXX Only when one item selected?
                Node node = selectedNodes[0];
                if (node != null) {
                    try {
                        return node.clipboardCopy();
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    }
                }
            }
        }
        return null;
    }

    /** XXX #6245208 Flag saying whether to ignore CnC because of progressing DnD. */
    private boolean ignoreCnC;
    void setIgnoreCnC(boolean ignore) {
        ignoreCnC = ignore;
    }
    private boolean isIgnoreCnC() {
        return ignoreCnC;
    }
    

    // XXX Moved from designer/jsf/../AbstractJsfTopComponent.
    /**
     * This method returns the position at which a component should be
     * pasted.
     * <p>
     * This implementation returns the current position of the mouse cursor.
     * If the cursor is outside the visual editor or hasn't moved
     * since the last time this method was invoked, this method will
     * return a position one grid box below and to the right of the
     * currently selected component. If no component is selected, this method
     * returns <code>null</code>.
     *
     * @return the point at which to paste a component or <code>null</code> if
     * there is no valid paste position
     */
    public /*protected*/ Point getPastePoint() {
//        Point p = webform.getManager().getMouseHandler().getCurrentPos();
//        Point p = designer.getCurrentPos();
        Point p = getMouseHandler().getCurrentPos();

        if (p != null) {
            // Ensure that if user pastes multiple times without moving
            // the mouse, we don't reuse the mouse position but switch
            // to an offset from selection instead
            Point location = new Point(p);
//            webform.getManager().getMouseHandler().clearCurrentPos();
//            designer.clearCurrentPos();
            getMouseHandler().clearCurrentPos();

            return location;
        } else {
//            Element e = webform.getSelection().getPositionElement();
//            Element e = designer.getPositionElement();
            Element e = webform.getSelection().getPositionElement();

            if (e == null) {
                return null;
            }

//            int top = CssLookup.getLength(e, XhtmlCss.TOP_INDEX);
//            int left = CssLookup.getLength(e, XhtmlCss.LEFT_INDEX);
            int top = CssProvider.getValueService().getCssLength(e, XhtmlCss.TOP_INDEX);
            int left = CssProvider.getValueService().getCssLength(e, XhtmlCss.LEFT_INDEX);

            if ((top != CssValue.AUTO) || (left != CssValue.AUTO)) {
                if (left == CssValue.AUTO) {
                    left = 0;
                }

                if (top == CssValue.AUTO) {
                    top = 0;
                }

//                GridHandler gh = GridHandler.getInstance();
//                GridHandler gh = webform.getGridHandler();
//                return new Point(left + gh.getGridWidth(), top + gh.getGridHeight());
//                int gridWidth = designer.getGridWidth();
//                int gridHeight = designer.getGridHeight();
//                GridHandler gh = GridHandler.getDefault();
//                int gridWidth = gh.getGridWidth();
//                int gridHeight = gh.getGridHeight();
                int gridWidth = webform.getGridWidth();
                int gridHeight = webform.getGridHeight();
                return new Point(left + gridWidth, top + gridHeight);
            }

            return null;
        }
    }
    

}
