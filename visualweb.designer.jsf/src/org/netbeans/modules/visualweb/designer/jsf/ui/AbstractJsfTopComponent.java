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


import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.FocusManager;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.visualweb.api.designer.Designer;

import org.openide.ErrorManager;
import org.openide.awt.UndoRedo;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.ClipboardEvent;
import org.openide.util.datatransfer.ClipboardListener;
import org.openide.util.datatransfer.ExClipboard;
import org.openide.util.datatransfer.PasteType;
import org.openide.windows.TopComponent;
import org.w3c.dom.Element;

import org.netbeans.modules.visualweb.designer.jsf.JsfForm;
import org.netbeans.modules.visualweb.designer.jsf.JsfSupportUtilities;
import org.openide.util.WeakListeners;


/**
 * XXX Before as designer/../SelectionTopComp.
 *  
 * This class provides clipboard and selection functionality for
 * a TopComponent such as the design surface or the app outline.
 *
 * @author Tor Norbye
 */
public abstract class AbstractJsfTopComponent extends TopComponent implements ClipboardOwner {
//    protected transient WebForm webform = null;

    /////////////////////////////////////////////////////////////////////////
    // Cut, Copy, Paste, Delete support
    /////////////////////////////////////////////////////////////////////////
    // A lot of this is based on code in openide's ExplorerActions.java
    // and ExplorerActionsImpl.java files

    /** copy action performer */
    protected final transient CopyCutActionPerformer copyActionPerformer =
        new CopyCutActionPerformer(true);

    /** cut action performer */
    protected final transient CopyCutActionPerformer cutActionPerformer =
        new CopyCutActionPerformer(false);

    /** delete action performer */
    protected final transient DeleteActionPerformer deleteActionPerformer =
        new DeleteActionPerformer();

    /** Paste action performer. */
    private final transient OwnPaste pasteActionPerformer = new OwnPaste();

    /** tracker for all actions */
    private CBListener cblistener;

//    protected AbstractJsfTopComponent() {
//    }
    
    protected final JsfForm jsfForm;
    protected final Designer designer;

    public AbstractJsfTopComponent(/*WebForm webform*/ JsfForm jsfForm, Designer designer) {
//        this.webform = webform;
        this.jsfForm = jsfForm;
        this.designer = designer;
    }

//    public WebForm getWebForm() {
//        return webform;
//    }

    /* Activates copy/cut/paste actions.
    */
//    @Override
//    protected void componentActivated() {
    protected void designerActivated() {
//        super.componentActivated();

//        //Log.err.log("Component activated!");
//        if (cblistener == null) {
//            cblistener = new CBListener();
//        }
        // #104330 Using weak listener.
        // XXX Is this efficient to recreate it each time the comp is activated?
        cblistener = new CBListener();
        Clipboard c = getClipboard();
        if (c instanceof ExClipboard) {
            ExClipboard clip = (ExClipboard)c;
//            clip.addClipboardListener(cblistener);
            clip.addClipboardListener(WeakListeners.create(ClipboardListener.class, cblistener, clip));
        }

//        if (webform != null) {
//            activateActions();
//        }
//        activateActions();
        updatePasteAction();

//        if (webform != null) {
//            webform.getModel().setActivated(true);
//            webform.setModelActivated(true);
//        }
        jsfForm.setModelActivated(true);
    }

    /* Deactivates copy/cut/paste actions.
    */
//    @Override
//    protected void componentDeactivated() {
    protected void designerDeactivated() {
//        if (webform != null) {
////            webform.getModel().setActivated(false);
//            webform.setModelActivated(false);
//        }
        jsfForm.setModelActivated(false);

//        // XXX why not super.componentDeactivated?
//        //OutlineTopComp.getInstance().setCurrent(null);
//        Clipboard c = getClipboard();
//
//        if (c instanceof ExClipboard) {
//            ExClipboard clip = (ExClipboard)c;
//            clip.removeClipboardListener(cblistener);
//        }
        // XXX Removing the weak listener.
        cblistener = null;

//        if (webform != null) {
//            deactivateActions();
//        }
        deactivateActions();

        // Just leave the nodes visible until some other window
        // sets the activation - that way, the user can for example
        // move up to the main window and not loose sight of the
        // currently selected component
    }

//    protected abstract boolean isSelectionEmpty();

    protected abstract void deleteSelection();

    protected abstract Transferable copy();

//    protected abstract DesignBean getPasteParent();
    protected abstract Element getPasteParentComponent();

//    /**
//     * This method returns the position at which a component should be
//     * pasted.
//     * <p>
//     * This implementation returns the current position of the mouse cursor.
//     * If the cursor is outside the visual editor or hasn't moved
//     * since the last time this method was invoked, this method will
//     * return a position one grid box below and to the right of the
//     * currently selected component. If no component is selected, this method
//     * returns <code>null</code>.
//     *
//     * @return the point at which to paste a component or <code>null</code> if
//     * there is no valid paste position
//     */
//    public /*protected*/ Point getPastePosition() {
////        Point p = webform.getManager().getMouseHandler().getCurrentPos();
//        Point p = designer.getCurrentPos();
//
//        if (p != null) {
//            // Ensure that if user pastes multiple times without moving
//            // the mouse, we don't reuse the mouse position but switch
//            // to an offset from selection instead
//            Point location = new Point(p);
////            webform.getManager().getMouseHandler().clearCurrentPos();
//            designer.clearCurrentPos();
//
//            return location;
//        } else {
////            Element e = webform.getSelection().getPositionElement();
//            Element e = designer.getPositionElement();
//
//            if (e == null) {
//                return null;
//            }
//
////            int top = CssLookup.getLength(e, XhtmlCss.TOP_INDEX);
////            int left = CssLookup.getLength(e, XhtmlCss.LEFT_INDEX);
//            int top = CssProvider.getValueService().getCssLength(e, XhtmlCss.TOP_INDEX);
//            int left = CssProvider.getValueService().getCssLength(e, XhtmlCss.LEFT_INDEX);
//
//            if ((top != CssValue.AUTO) || (left != CssValue.AUTO)) {
//                if (left == CssValue.AUTO) {
//                    left = 0;
//                }
//
//                if (top == CssValue.AUTO) {
//                    top = 0;
//                }
//
////                GridHandler gh = GridHandler.getInstance();
////                GridHandler gh = webform.getGridHandler();
////                return new Point(left + gh.getGridWidth(), top + gh.getGridHeight());
//                int gridWidth = designer.getGridWidth();
//                int gridHeight = designer.getGridHeight();
//                return new Point(left + gridWidth, top + gridHeight);
//            }
//
//            return null;
//        }
//    }

//    protected abstract MarkupPosition getPasteMarkupPosition();

//    protected abstract void selectBeans(DesignBean[] beans);
    protected abstract void selectComponents(Element[] coponentRootElements);

    /**
     * Do what it takes to show a popup menu at the most natural
     * place when the user has pressed e.g. shift-f10. Usually
     * you'll want to post the menu right under the primary selection
     * item.
     */
    protected abstract void showKeyboardPopup();

//    /** Called when this window is activated: make cut, copy, paste and delete
//        sensitive based on whether or not anything is selected and whether
//        the clipboard contains something we can absorb. */
//    public void activateActions() {
//        if (isSelectionEmpty()) {
//            disableCutCopyDelete();
//        } else {
//            enableCutCopyDelete();
//        }
//
//        updatePasteAction();
//    }

    /** Called when the when the component is deactivated. We no longer
        allow our paste types to be invoked so clear it - get rid of
        the action performers as well. */
    public void deactivateActions() {
        //        if (paste != null) {
        //            // TODO This was wrong, you cannot clear paste types, the PasteAction won't work then.
        //            // You need to follow new approach to provide context for actions like paste,
        //            // using ActionMap, see how it is used in CloneableEditor and follow that example.
        //            // FIXME Follow http://www.netbeans.org/project/www/download/dev/javadoc/OpenAPIs/org/openide/util/actions/CallbackSystemAction.html#setActionPerformer(org.openide.util.actions.ActionPerformer)
        //            // and be aware to change it for cut/copy/delete actions too (all callback actions used).
        //            //            paste.setPasteTypes(null);
        //        }
    }

//    private boolean isActivated() {
//        return this == TopComponent.getRegistry().getActivated();
//    }

//    /** Called when the selection is non zero and the component is active:
//        enable cut, copy and delete */
//    public void enableCutCopyDelete() {
//        if (!isActivated()) {
//            return;
//        }
//
//        copyActionPerformer.setEnabled(true);
//        cutActionPerformer.setEnabled(true);
//        deleteActionPerformer.setEnabled(true);
//    }
//
//    /** Called when the selection is removed: disable copy, cut, delete */
//    public void disableCutCopyDelete() {
//        if (!isActivated()) {
//            return;
//        }
//
//        copyActionPerformer.setEnabled(false);
//        cutActionPerformer.setEnabled(false);
//        deleteActionPerformer.setEnabled(false);
//    }

//    /** Paste the beans in the given transferable to the given parent
//     * and markup position.
//     */
////    public static DesignBean[] pasteBeans(WebForm webform, Transferable t, DesignBean parent,
////        MarkupPosition pos, Point location) {
//    public static Element[] pasteComponents(WebForm webform, Transferable t, Element parentComponentRootElement, Point location) {
////        // Make sure we're allowed to paste to the given parent.
////        // Arguably, I should not be enabling the paste action when the selected parent
////        // is "selected", but the parent used is computed very dynamically, so
////        // doing something like this would require recomputing the Paste state
////        // every pixel the mouse moves over the designer canvas. Instead we try
////        // to move the parent up until we find a suitable parent.
////        while (parent != null) {
////            DndHandler dndHandler = webform.getPane().getDndHandler();
////            int allowed = dndHandler.computeActions(parent, t, false, DropSupport.CENTER);
////
////            if ((allowed & DnDConstants.ACTION_COPY_OR_MOVE) != 0) {
////                break;
////            }
////
////            parent = parent.getBeanParent();
////            pos = null; // no longer valid - just use insync defaults
////        }
////
////        if (parent == null) {
////            // No valid parent found.
////            Toolkit.getDefaultToolkit().beep();
////
////            return null;
////        }
////
////        Document document = null;
////
////        //LiveUnit unit = (LiveUnit)parent.getDesignContext();
////        LiveUnit unit = webform.getModel().getLiveUnit();
////
////        String description = NbBundle.getMessage(SelectionTopComp.class, "Paste"); // NOI18N
////        UndoEvent undoEvent = webform.getModel().writeLock(description);
////        try {
//////            document = webform.getDocument();
//////
//////            //document.setAutoIgnore(true);
//////            String description = NbBundle.getMessage(SelectionTopComp.class, "Paste"); // NOI18N
//////            document.writeLock(description);
////
////            DesignBean[] beans = unit.pasteBeans(t, parent, pos);
////
////            if (beans == null) {
////                return null;
////            }
////
////            // Decide whether we need to strip out position coordinates
////            // from the beans being moved
////            boolean needPos = true;
////
////            if (parent != null) {
////                needPos = DndHandler.isGridContext(parent, pos);
////
////                if (!needPos) {
////                    location = null;
////                }
////            }
////
////            // Determine if the destination is a grid area
////            if (location != null) {
////                // Snap
////                GridHandler gh = GridHandler.getInstance();
////
////                if (gh.snap()) {
////                    // TODO - compute the right target box here
////                    CssBox gridBox = null;
////                    location.x = gh.snapX(location.x, gridBox);
////                    location.y = gh.snapY(location.y, gridBox);
////                }
////
////                // Position elements
////                Point topLeft = getTopLeft(beans);
////
////                for (int i = 0; i < beans.length; i++) {
////                    if (!(beans[i] instanceof MarkupDesignBean)) {
////                        continue;
////                    }
////
////                    MarkupDesignBean bean = (MarkupDesignBean)beans[i];
////
////                    // XXX I need to do this on the -rendered- element!
////                    Element element = bean.getElement();
////                    assert element != null;
////
////                    try {
//////                        webform.getDomSynchronizer().setUpdatesSuspended(bean, true);
////                        webform.setUpdatesSuspended(bean, true);
////
////                        if (!needPos) {
//////                            XhtmlCssEngine engine = webform.getMarkup().getCssEngine();
////                            List remove = new ArrayList(5);
////                            remove.add(new StyleData(XhtmlCss.POSITION_INDEX));
////                            remove.add(new StyleData(XhtmlCss.LEFT_INDEX));
////                            remove.add(new StyleData(XhtmlCss.TOP_INDEX));
////                            remove.add(new StyleData(XhtmlCss.RIGHT_INDEX));
////                            remove.add(new StyleData(XhtmlCss.BOTTOM_INDEX));
////// <removing design bean manipulation in engine>
//////                            engine.updateLocalStyleValues((RaveElement)element, null, remove);
////// ====
////                            CssProvider.getEngineService().updateLocalStyleValuesForElement(element,
////                                    null, (StyleData[])remove.toArray(new StyleData[remove.size()]));
////// </removing design bean manipulation in engine>
////
////                            continue;
////                        }
////
////                        List set = new ArrayList(5);
////                        List remove = new ArrayList(5);
//////                        Value val = CssLookup.getValue(element, XhtmlCss.POSITION_INDEX);
////                        CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.POSITION_INDEX);
////
//////                        if ((val == CssValueConstants.ABSOLUTE_VALUE) ||
//////                                (val == CssValueConstants.RELATIVE_VALUE) ||
//////                                (val == CssValueConstants.FIXED_VALUE)) {
////                        if (CssProvider.getValueService().isAbsoluteValue(cssValue)
////                        || CssProvider.getValueService().isRelativeValue(cssValue)
////                        || CssProvider.getValueService().isFixedValue(cssValue)) {
//////                            int top = CssLookup.getLength(element, XhtmlCss.TOP_INDEX);
//////                            int left = CssLookup.getLength(element, XhtmlCss.LEFT_INDEX);
////                            int top = CssUtilities.getCssLength(element, XhtmlCss.TOP_INDEX);
////                            int left = CssUtilities.getCssLength(element, XhtmlCss.LEFT_INDEX);
////
////                            if ((top != CssBox.AUTO) || (left != CssBox.AUTO)) {
////                                if (left == CssBox.AUTO) {
////                                    left = 0;
////                                }
////
////                                if (top == CssBox.AUTO) {
////                                    top = 0;
////                                }
////
////                                left = (location.x + left) - topLeft.x;
////                                top = (location.y + top) - topLeft.y;
////
////                                set.add(new StyleData(XhtmlCss.TOP_INDEX,
////                                        Integer.toString(top) + "px")); // NOI18N
////                                set.add(new StyleData(XhtmlCss.LEFT_INDEX,
////                                        Integer.toString(left) + "px")); // NOI18N
////                            } else {
////                                set.add(new StyleData(XhtmlCss.LEFT_INDEX,
////                                        Integer.toString(location.x) + "px")); // NOI18N
////                                set.add(new StyleData(XhtmlCss.TOP_INDEX,
////                                        Integer.toString(location.y) + "px")); // NOI18N
////                            }
////                        } else {
////                            set.add(new StyleData(XhtmlCss.POSITION_INDEX,
//////                                    CssConstants.CSS_ABSOLUTE_VALUE)); // NOI18N
////                                    CssProvider.getValueService().getAbsoluteValue()));
////                            set.add(new StyleData(XhtmlCss.LEFT_INDEX,
////                                    Integer.toString(location.x) + "px")); // NOI18N
////                            set.add(new StyleData(XhtmlCss.TOP_INDEX,
////                                    Integer.toString(location.y) + "px")); // NOI18N
////                        }
////
////                        remove.add(new StyleData(XhtmlCss.RIGHT_INDEX));
////                        remove.add(new StyleData(XhtmlCss.BOTTOM_INDEX));
////
//////                        XhtmlCssEngine engine = webform.getMarkup().getCssEngine();
////// <removing design bean manipulation in engine>
//////                        engine.updateLocalStyleValues((RaveElement)element, set, remove);
////// ====
////                        CssProvider.getEngineService().updateLocalStyleValuesForElement(element,
////                                (StyleData[])set.toArray(new StyleData[set.size()]),
////                                (StyleData[])remove.toArray(new StyleData[remove.size()]));
////// </removing design bean manipulation in engine>
////                    } finally {
//////                        webform.getDomSynchronizer().setUpdatesSuspended(bean, false);
////                        webform.setUpdatesSuspended(bean, false);
////                    }
////                }
////            } else if (needPos) {
////                // We're over a grid area but don't have a specified position;
////                // leave existing positions in the pasted components alone
////                // but don't create new positions to assign to other components.
////                // This means that if you cut a component and then paste it
////                // it will appear in the place it was before cutting it.
////            } else {
////                // Flow area: remove absolute positions for all children
////                for (int i = 0; i < beans.length; i++) {
////                    if (!(beans[i] instanceof MarkupDesignBean)) {
////                        // Not a visual component
////                        continue;
////                    }
////
////                    MarkupDesignBean bean = (MarkupDesignBean)beans[i];
////                    Element element = bean.getElement();
////
////                    try {
//////                        webform.getDomSynchronizer().setUpdatesSuspended(bean, true);
////                        webform.setUpdatesSuspended(bean, true);
//////                        CssLookup.removeLocalStyleValue(element, XhtmlCss.POSITION_INDEX);
//////                        CssLookup.removeLocalStyleValue(element, XhtmlCss.LEFT_INDEX);
//////                        CssLookup.removeLocalStyleValue(element, XhtmlCss.TOP_INDEX);
////                        CssProvider.getEngineService().removeLocalStyleValueForElement(element, XhtmlCss.POSITION_INDEX);
////                        CssProvider.getEngineService().removeLocalStyleValueForElement(element, XhtmlCss.LEFT_INDEX);
////                        CssProvider.getEngineService().removeLocalStyleValueForElement(element, XhtmlCss.TOP_INDEX);
////                    } finally {
//////                        webform.getDomSynchronizer().setUpdatesSuspended(bean, false);
////                        webform.setUpdatesSuspended(bean, false);
////                    }
////                }
////            }
////
////            return beans;
////        } finally {
//////            document.writeUnlock();
////            webform.getModel().writeUnlock(undoEvent);
////        }
//        
////        return webform.pasteBeans(t, parent, pos, location, GridHandler.getInstance());
////        return webform.pasteBeans(t, parent, pos, location, webform.getGridHandler());
//        return webform.pasteComponents(t, parentComponentRootElement, location, webform.getGridHandler());
//    }

//    /** Compute the leftmost and topmost positions among the given beans
//     */
//    private static Point getTopLeft(DesignBean[] beans) {
//        int minLeft = Integer.MAX_VALUE;
//        int minTop = Integer.MAX_VALUE;
//
//        for (int i = 0; i < beans.length; i++) {
//            Element element = FacesSupport.getElement(beans[i]);
//
//            if (element == null) {
//                // Not a visual component
//                continue;
//            }
//
////            Value val = CssLookup.getValue(element, XhtmlCss.POSITION_INDEX);
//            CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.POSITION_INDEX);
//
////            if ((val == CssValueConstants.ABSOLUTE_VALUE) ||
////                    (val == CssValueConstants.RELATIVE_VALUE) ||
////                    (val == CssValueConstants.FIXED_VALUE)) {
//            if (CssProvider.getValueService().isAbsoluteValue(cssValue)
//            || CssProvider.getValueService().isRelativeValue(cssValue)
//            || CssProvider.getValueService().isFixedValue(cssValue)) {
////                int top = CssLookup.getLength(element, XhtmlCss.TOP_INDEX);
////                int left = CssLookup.getLength(element, XhtmlCss.LEFT_INDEX);
//                int top = CssUtilities.getCssLength(element, XhtmlCss.TOP_INDEX);
//                int left = CssUtilities.getCssLength(element, XhtmlCss.LEFT_INDEX);
//
//                if ((top != CssBox.AUTO) || (left != CssBox.AUTO)) {
//                    if (left == CssBox.AUTO) {
//                        left = 0;
//                    }
//
//                    if (top == CssBox.AUTO) {
//                        top = 0;
//                    }
//
//                    if (top < minTop) {
//                        minTop = top;
//                    }
//
//                    if (left < minLeft) {
//                        minLeft = left;
//                    }
//                }
//            }
//        }
//
//        return new Point(minLeft, minTop);
//    }

    
//    private static final DataFlavor FLAVOR_DISPLAY_ITEM = new DataFlavor(
//            DataFlavor.javaJVMLocalObjectMimeType + "; class=" + DisplayItem.class.getName(), // NOI18N
//            "RAVE_PALETTE_ITEM"); // TODO get rid of such name.
    

    /** Updates paste action.
    * @param path selected nodes
    */
    private void updatePasteAction() {
        Clipboard clipboard = getClipboard();
        Transferable trans = clipboard.getContents(this);

//        if (trans != null) {
//            DataFlavor[] df = trans.getTransferDataFlavors();
//            int n = 0;
//
//            if (df != null) {
//                n = df.length;
//            }
//
//            for (int i = 0; i < n; i++) {
//                DataFlavor flavor = df[i];
//
//		// XXX TODO Get rid of this dep, you can specify your own data flavor
//		// which can match, there will be created new data flavors avoiding
//		// usage of .
//                if (FLAVOR_DISPLAY_ITEM.equals(flavor)
//		|| (flavor.getRepresentationClass() == String.class)
//		|| flavor.getMimeType().startsWith("application/x-creator-")) { // NOI18N
//
//                    // Yes!
//                    PasteType[] pasteTypes = new PasteType[] { new Paste() };
//                    pasteActionPerformer.setPasteTypes(pasteTypes);
//
//                    return;
//                }
//            }
//        }
//        if (webform.canPasteTransferable(trans)) {
        if (jsfForm.canPasteTransferable(trans)) {
            // Yes!
            PasteType[] pasteTypes = new PasteType[] { new Paste() };
            pasteActionPerformer.setPasteTypes(pasteTypes);
            return;
        }

        pasteActionPerformer.setPasteTypes(null);
    }

    // Implements ClipboardOwner
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }

    /** If our clipboard is not found return the default system
        clipboard. */
    private static Clipboard getClipboard() {
        Clipboard c = (Clipboard)Lookup.getDefault().lookup(Clipboard.class);

        if (c == null) {
            c = Toolkit.getDefaultToolkit().getSystemClipboard();
        }

        return c;
    }

    @Override
    public UndoRedo getUndoRedo() {
//        if (webform != null) {
////            return webform.getModel().getUndoManager();
//            return webform.getUndoManager();
//        }
//
//        return super.getUndoRedo();
        return jsfForm.isValid() ? jsfForm.getUndoManager() : UndoRedo.NONE;
    }

    /** Remove any items from the list that are children of any other
     * components in the list.
     * This is a slow implementation (n^3) but n will always (in real
     * scenarios be small).
     */
//    protected void removeChildrenBeans(ArrayList list) {
//        Iterator it = list.listIterator();
//
//        while (it.hasNext()) {
//            DesignBean lb = (DesignBean)it.next();
//
//            // See if any other item in this list is a parent
//            for (int i = 0, n = list.size(); i < n; i++) {
//                DesignBean parent = (DesignBean)list.get(i);
//
//                if ((lb != parent) && isBelow(parent, lb)) {
//                    it.remove();
//
//                    break;
//                }
//            }
//        }
//    }
    protected void removeChildrenComponents(List<Element> list) {
        Iterator<Element> it = list.listIterator();

        while (it.hasNext()) {
//            DesignBean lb = (DesignBean)it.next();
            Element element = it.next();

            // See if any other item in this list is a parent
//            for (int i = 0, n = list.size(); i < n; i++) {
//                DesignBean parent = (DesignBean)list.get(i);
//
//                if ((lb != parent) && isBelow(parent, lb)) {
//                    it.remove();
//
//                    break;
//                }
//            }
            for (Element parent : list) {
                if (element != parent && isBelow(parent, element)) {
                    it.remove();

                    break;
                }
            }
        }
    }


    /** Determine if the given bean is below the given other potential parent */
//    private static boolean isBelow(DesignBean parent, DesignBean bean) {
    private static boolean isBelow(Element parent, Element element) {
        if (element == null) {
            return false;
        } else if (element == parent) {
            return true;
        } else {
//            return isBelow(parent, WebForm.getDomProviderService().getParentComponent(element));
            return isBelow(parent, JsfSupportUtilities.getParentComponent(element));
        }
    }

    protected void installActions() {
        ActionMap map = getActionMap();

        map.put(DefaultEditorKit.copyAction, copyActionPerformer);
        map.put(DefaultEditorKit.cutAction, cutActionPerformer);

        map.put("delete", deleteActionPerformer); // or false

        map.put(DefaultEditorKit.pasteAction, pasteActionPerformer);

        // Popup menu from the keyboard        
        map.put("org.openide.actions.PopupAction",
            new AbstractAction() {
                public void actionPerformed(ActionEvent evt) {
                    SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                showKeyboardPopup();
                            }
                        });
                }
            });
    }

    /** Own implementation of paste action. */
    private static class OwnPaste extends AbstractAction {
        private PasteType[] pasteTypes;

        private OwnPaste() {
        }

        public boolean isEnabled() {
            //            updateActionsState();
            return super.isEnabled();
        }

        public void setPasteTypes(PasteType[] arr) {
            synchronized (this) {
                this.pasteTypes = arr;
            }

            setEnabled(arr != null);
        }

        public void actionPerformed(ActionEvent e) {
            throw new IllegalStateException("Should not be invoked at all. Paste types: " +
                java.util.Arrays.asList(pasteTypes)); // NOI18N
        }

        public Object getValue(String s) {
            //            updateActionsState();
            if ("delegates".equals(s)) { // NOI18N

                return pasteTypes;
            }

            return super.getValue(s);
        }
    }

    private static class Paste extends PasteType {
        public Transferable paste() throws IOException {
            Clipboard clipboard = getClipboard();
            final Transferable t = clipboard.getContents(this);

            if (t != null) {
                // XXX TODO This is very suspicious, and calling invokeAndWait very dangerous, investigate later!
                // We run into deadlocks without this;
                // !#$!@#!@ ModuleActions thread
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                            public void run() {
                                pasteSynchronous(t);
                            }
                        });
                } catch (InvocationTargetException ite) {
                    ErrorManager.getDefault().notify(ite.getCause());
                    ErrorManager.getDefault().notify(ite);
                } catch (InterruptedException ie) {
                    ErrorManager.getDefault().notify(ie);
                }
            }

            //This makes this not clear the clipboard
            return t;

            // to clear clipboard, return ExTransferable.EMPTY instead...
        }

        private void pasteSynchronous(Transferable t) {
// XXX Trying to fix the paste type impl, but the default handler doesn't work as expected.
// Needs to have a closer look at it.
            Component focusOwner = FocusManager.getCurrentManager().getFocusOwner();
            if (focusOwner instanceof JTextComponent) {
                // Inline editing.
                JTextComponent textComponent = (JTextComponent)focusOwner;
                textComponent.paste();
            } else if (focusOwner instanceof JComponent) {
                // DesignerPane.
                JComponent component = (JComponent)focusOwner;
		TransferHandler th = component.getTransferHandler();
                if (th != null) {
                    th.importData(component, t);
                }
            }
            
//            // See if it's a plain String paste
//            DataFlavor[] df = t.getTransferDataFlavors();
//            int n = 0;
//
//            if (df != null) {
//                n = df.length;
//            }
//
//            for (int i = 0; i < n; i++) {
//                DataFlavor flavor = df[i];
//
//                if (FLAVOR_DISPLAY_ITEM.equals(flavor)
//		|| flavor.getMimeType().startsWith("application/x-creator-")) { // NOI18N
//
//                    DesignBean parent = getPasteParent();
//                    MarkupPosition pos = getPasteMarkupPosition();
//                    Point location = getPastePosition();
//                    DesignBean[] beans = pasteBeans(webform, t, parent, pos, location);
//
//                    if ((beans != null) && (beans.length > 0)) {
//                        selectBeans(beans);
//                    }
//
//                    return;
//                } else if (flavor.getRepresentationClass() == String.class) {
//                    try {
//                        String content = (String)t.getTransferData(flavor);
//
//                        // XXX #6332049 When in inline editing we shouldn't steal the paste
//                        // (at least for the JTextComponent's.
//                        // This is just a workaround, it shouldn't be done this way.
//                        // actions should be created based on context (and inline editing
//                        // context is diff from the designer pane one).
//                        if(webform.getManager().isInlineEditing()) {
//                            Component comp = FocusManager.getCurrentManager().getFocusOwner();
//                            if(comp instanceof JTextComponent) {
//                                JTextComponent textComp = (JTextComponent)comp;
//                                textComp.paste();
//                                return;
//                            } 
//                        }
//                        
//                        if (webform.getPane().getCaret() != null) {
//                            webform.getPane().getCaret().replaceSelection(content);
//                        } else {
//                            Point location = getPastePosition();
//                            DndHandler handler = webform.getPane().getDndHandler();
////                            handler.setDropPoint(location);
////
////                            //handler.setInsertPosition(getPasteMarkupPosition());
////                            handler.importString(content);
//                            handler.importString(content, location, GridHandler.getInstance());
//                        }
//                    } catch (Exception ex) {
//                        ErrorManager.getDefault().notify(ex);
//                    }
//
//                    return;
//                }
//            }
        }
    }

    /** Class which performs delete action */
    class DeleteActionPerformer extends AbstractAction {
        public void actionPerformed(final ActionEvent e) {
            // XXX #6491546 The delete performer is invoked not in AWT thread.
            if (EventQueue.isDispatchThread()) {
                doActionPerformed(e);
            } else {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        doActionPerformed(e);
                    }
                });
            }
        }
        
        private void doActionPerformed(ActionEvent e) {
            // XXX #6333143 When in inline editing, delegate to inline editor.
//            if(webform.getManager().isInlineEditing()) {
//                webform.getManager().getInlineEditor().invokeDeleteNextCharAction(e);
//                return;
//            }
            if (designer.isInlineEditing()) {
                designer.invokeDeleteNextCharAction(e);
                return;
            }

            if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
                // On the mac only, the delete key in the designer causes
                // a DeleteAction to be performed. During inline editing
                // this cancels us out and -deletes the component- which
                // is really bad. By ensuring that the source is the top
                // component I allow users to still delete the inlined edited
                // component from the toolbar delete button or the context menu.
                // TODO: file NetBeans platform bug
//                if (webform.getManager().isInlineEditing() &&
//                        (e.getSource() == webform.getTopComponent())) {
                if (designer.isInlineEditing() && (e.getSource() == this)) {
                    // The delete event is also received by the delete forward action 
                    return;
                }
            }

            performAction(null);
        }

        /** Perform delete action. */
        public void performAction(SystemAction action) {
            // We run into deadlocks without this; !#$!@#!@ ModuleActions thread
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
//                    webform.getManager().finishInlineEditing(false);
                    designer.finishInlineEditing(false);
                    deleteSelection();
                }
            });
        }
    }

//    // XXX Moved from formder designer/outline/DropSupport.
//    private static final int DROP_ABOVE = -1;
//    private static final int DROP_CENTER = 0;
//    private static final int DROP_BELOW = 1;
    
    /** Class which performs copy and cut actions */
    class CopyCutActionPerformer extends AbstractAction {
        /** determine if adapter is used for copy or cut action. */
        boolean isCopy;

        /** Create new adapter */
        public CopyCutActionPerformer(boolean b) {
            isCopy = b;
        }

        public void actionPerformed(ActionEvent e) {
            performAction(null);
        }

        /** Perform copy or cut action. */
        public void performAction(SystemAction action) {
//            if (webform.getManager().isInlineEditing()) {
            if (designer.isInlineEditing()) {
//                Transferable t = webform.getManager().getInlineEditor().copyText(!isCopy);
                Transferable t = designer.inlineCopyText(!isCopy);

                if (t != null) {
//                    getClipboard().setContents(t, SelectionTopComp.this);
                        getClipboard().setContents(t, AbstractJsfTopComponent.this);

                    return;
                }
            }

            Transferable t = copy();
            // XXX Happened NPE.
            // FIXME Why was this performer enabled?
            if (t == null) {
                return;
            }

            // XXX #110353 Incorrect check for allowing cut/copy.
//            boolean pastable = false;
//
//            //Check if a non-pastable component is selected, if so, pretend nothing was selected
////            DndHandler dndHandler = webform.getPane().getDndHandler();
//
////            for (DesignBean parent = getPasteParent(); parent != null;
////                    parent = parent.getBeanParent()) {
//            for (Element parentComponentRootElement = getPasteParentComponent(); parentComponentRootElement != null;
////            parentComponentRootElement = WebForm.getDomProviderService().getParentComponent(parentComponentRootElement)) {
//            parentComponentRootElement = JsfSupportUtilities.getParentComponent(parentComponentRootElement)) {
////                int allowed = dndHandler.computeActions(parent, t, false, /*DropSupport.CENTER*/DROP_CENTER);
////                int allowed = dndHandler.computeActions(parentComponentRootElement, t, false, /*DropSupport.CENTER*/DROP_CENTER);
////                int allowed = webform.computeActions(parentComponentRootElement, t);
//                int allowed = jsfForm.computeActions(parentComponentRootElement, t);
//
//                if ((allowed & DnDConstants.ACTION_COPY_OR_MOVE) != 0) {
//                    pastable = true;
//
//                    break;
//                }
//            }

//            if ((t != null) && pastable) {
            if (t != null) {
                // XXX why the string selection??
//                getClipboard().setContents(t, SelectionTopComp.this);
                getClipboard().setContents(t, AbstractJsfTopComponent.this);

                if (!isCopy) { // cut: we've copied, so now delete...

                    // We run into deadlocks without this;
                    // !#$!@#!@ ModuleActions thread
                    SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
//                                webform.getManager().finishInlineEditing(false);
                                designer.finishInlineEditing(false);
                                deleteSelection();
                            }
                        });
                }
            }
        }
    }

    private class CBListener implements ClipboardListener {
        /** This method is called when content of clipboard is changed.
        * @param ev event describing the action
        */
        public void clipboardChanged(ClipboardEvent ev) {
            if (!ev.isConsumed()) {
                // We can only do this from the event thread
                if (SwingUtilities.isEventDispatchThread()) {
                    updatePasteAction();
                } else {
                    SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                updatePasteAction();
                            }
                        });
                }
            }
        }
    }
}
