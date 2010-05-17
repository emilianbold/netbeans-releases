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
package org.netbeans.modules.visualweb.designer;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.lang.ref.WeakReference;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.netbeans.modules.visualweb.api.designer.DomProvider;
import org.netbeans.modules.visualweb.api.designer.DomProvider.DomPosition;
import org.netbeans.modules.visualweb.css2.ModelViewMapper;
import org.netbeans.modules.visualweb.css2.CssBox;

import org.netbeans.modules.visualweb.css2.PageBox;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;

import org.w3c.dom.Element;
import org.w3c.dom.Node;



/**
 * The transfer handler for the designer pane
 *
 * @todo I need to call canCreateFacet to check if a facet drop is allowed in those
 *  places where I use it
 * @todo I need to call canMoveBean instead of canCreateBean for reparenting/moving
 *  and of course these would need to do more specific acceptBean/child checking!
 *
 * @author  Tor Norbye
 * @author  Carl Quinn
 */
public class DndHandler /*extends TransferHandler*/ {

//    /** XXX Copy from DropSupport. */
//    private static final int DROP_ABOVE  = -1;
//    private static final int DROP_CENTER = 0;
//    private static final int DROP_BELOW  = 1;


    /** State indicating that a drop is not allowed */
    public static final int DROP_DENIED = DomProvider.DROP_DENIED;

    /** State indicating that the drop is allowed and will cause a link */
    public static final int DROP_PARENTED = DomProvider.DROP_PARENTED;

//    /** Directory prefix under the project root to place the web folder */
//    private static final String WEB = "web"; // NOI18N

//    /** Directory prefix under the web folder root to place the resource files */
//    private static final String RESOURCES = "/resources/"; // NOI18N

    /** State indicating that the drop is allowed and the bean will be
     *  parented by one of the beans under the cursor */
    public static final int DROP_LINKED = DomProvider.DROP_LINKED;
    
    private static final transient boolean DONT_SHOW_MATCHES =
        System.getProperty("designer.dontShowDropTarget") != null;

    // Current drag & drop transfer handler registry

//    /** XXX This is a hack. The designer needs access to the
//     * transferable as part of a DropTargetDragEvent notification (to
//     * for example determine whether the drop location is valid, based
//     * on the beans being dropped and the components under the
//     * cursor. For some reason, the DropTargetDragEvent does not
//     * provide any access to the transferable. So instead we provide a
//     * way for the designer to look up the most recent
//     * transferable. It's public here since there are a couple of
//     * other sources for drags (such as the document outline, and the
//     * server navigator, and rather than having the designer look in
//     * multiple places and figure out which transferable is most
//     * recent, all drag initiators will share this field.  The
//     * designer can also clear it when it knows it's done with it, to
//     * allow gc.
//     * JDK15 In JDK15 there is a new method on the drop target listener
//     * which will provide the transferable. This will allow us to rip
//     * all this out and simply obtain the transferable directly rather
//     * than relying on the drag source to set it.
//     */
//    private static transient Transferable transferable;
    
    private transient WebForm webform;

    /**
     * Used during an importBean: set to the bean to select when
     * we're done processing. Different methods override which
     * item should be selected.
     */
//    private transient DesignBean select; // have we found a target to select?
//    private MarkupDesignBean recentDropTarget;
    // XXX #123995 Leaks element which document might get replaced.
//    private transient Element recentDropTargetComponentRootElement;
    private transient WeakReference<Element> recentDropTargetComponentRootElementWRef = new WeakReference<Element>(null);
//    private transient DesignBean currentMatched;
    private transient Element currentMatchedComponentRootElement;
//    private transient MarkupMouseRegion currentRegion;
    private transient Element currentRegionElement;
    private transient String lastMessage;
    private transient Point dropPoint;
    private transient Dimension dropSize;
//    private transient Position insertPos = Position.NONE;
    private transient DomPosition insertPos = DomPosition.NONE;
    private transient int dropAction;

    public DndHandler(WebForm webform) {
        this.webform = webform;
        if(DesignerUtils.DEBUG) {
            DesignerUtils.debugLog(getClass().getName() + "()");
        }
        if(webform == null) {
            throw(new IllegalStateException("Null webform"));
        }
    }

//    private static final DataFlavor FLAVOR_DISPLAY_ITEM = new DataFlavor(
//            DataFlavor.javaJVMLocalObjectMimeType + "; class=" + DisplayItem.class.getName(), // NOI18N
//            "RAVE_PALETTE_ITEM"); // TODO get rid of such name.
    
    /**
     * This method causes a transfer to a component from a clipboard or a DND drop operation.  The
     * Transferable represents the data to be imported into the component.
     *
     * @param comp The component to receive the transfer.  This argument is provided to enable
     *  sharing of TransferHandlers by multiple components.
     * @param t The data to import
     * @return  true if the data was inserted into the component, false otherwise.
     */
    public boolean importData(final JComponent comp, final Transferable t) {
//        Object transferData = null;
//
//        try {
////            DataFlavor importFlavor = getImportFlavor(t.getTransferDataFlavors());
//            DataFlavor importFlavor = webform.getImportFlavor(t.getTransferDataFlavors());
//
//            if (importFlavor == null) {
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, 
//                        new IllegalStateException("Unusable transfer flavors " + Arrays.asList(t.getTransferDataFlavors()))); // NOI18N
//
//                return false;
//            }
//
//            // XXX What was before in SelectionTopComp.
//            if (importFlavor.getMimeType().startsWith("application/x-creator-")) { // NOI18N
//                // XXX Handling of cut/copied bean. Needs to be improved/moved later.
////                SelectionTopComp selectionTopComp;
////                
////                if (comp instanceof SelectionTopComp) {
////                    selectionTopComp = (SelectionTopComp)comp;
////                } else {
////                    selectionTopComp = (SelectionTopComp)SwingUtilities.getAncestorOfClass(SelectionTopComp.class, comp);
////                }
////                
////                if (selectionTopComp == null) {
////                    // XXX
////                    return false;
////                }
////                
//////                DesignBean parent = selectionTopComp.getPasteParent();
////                Element parentComponentRootElement = selectionTopComp.getPasteParentComponent();
//////                MarkupPosition pos = selectionTopComp.getPasteMarkupPosition();
////                Point location = selectionTopComp.getPastePosition();
//////                DesignBean[] beans = selectionTopComp.pasteBeans(webform, t, parent, pos, location);
//////                Element[] componentRootElements = SelectionTopComp.pasteComponents(webform, t, parentComponentRootElement, location);
////                
////                if (location != null) {
////                    GridHandler gridHandler = webform.getGridHandler();
////                    location.x = gridHandler.snapX(location.x);
////                    location.y = gridHandler.snapY(location.y);
////                }
////                Element[] componentRootElements = webform.pasteComponents(t, parentComponentRootElement, location);
////
//////                if ((beans != null) && (beans.length > 0)) {
//////                    selectionTopComp.selectBeans(beans);
//////                }
////                if (componentRootElements.length > 0) {
////                    selectionTopComp.selectComponents(componentRootElements);
////                }
////                return true;
//                return webform.tcImportComponentData(comp, t);
//            } // TEMP
//            
//            Class rc = importFlavor.getRepresentationClass();
//            
//            transferData = t.getTransferData(importFlavor);
//            
////            if (rc == DisplayItem.class) {
////                // Create a new type
////                transferData = t.getTransferData(importFlavor);
////
////                if (!(transferData instanceof DisplayItem)) {
////                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, 
////                            new IllegalStateException("Invalid transfer data=" + transferData));
////
////                    return false;
////                }
////            } else if (rc == DesignBean.class) {
////                transferData = t.getTransferData(importFlavor);
////
////                if (!(transferData instanceof DesignBean[])) {
////                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, 
////                            new IllegalStateException("Invalid transfer data=" + transferData));
////
////                    return false;
////                }
////            } else if (rc.isAssignableFrom(List.class)) {
////                transferData = t.getTransferData(importFlavor);
////
////                if (!(transferData instanceof List)) {
////                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, 
////                            new IllegalStateException("Invalid transfer data=" + transferData));
////
////                    return false;
////                }
////            } else if (rc.isAssignableFrom(org.openide.nodes.Node.class)) {
////                transferData = t.getTransferData(importFlavor);
////
////                if (!(transferData instanceof org.openide.nodes.Node)) {
////                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, 
////                            new IllegalStateException("Invalid transfer data=" + transferData));
////
////                    return false;
////                }
////            } else
//                
//            if (rc == String.class) {
////                transferData = t.getTransferData(importFlavor);
//
//                // XXX #6332049 When in inline editing we shouldn't steal the paste
//                // (at least for the JTextComponent's.
//                // This is just a workaround, it shouldn't be done this way.
//                // actions should be created based on context (and inline editing
//                // context is diff from the designer pane one).
//                if(webform.getManager().isInlineEditing()) {
//                    Component focusOwner = FocusManager.getCurrentManager().getFocusOwner();
//                    if(focusOwner instanceof JTextComponent) {
//                        JTextComponent textComp = (JTextComponent)focusOwner;
//                        textComp.paste();
//                        return true;
//                    } 
//                }
//
//                // XXX Flowlayout mode?
////                if (webform.getPane().getCaret() != null) {
//                if (webform.getPane().hasCaret()) {
////                    webform.getPane().getCaret().replaceSelection((String)transferData);
//                    webform.getPane().replaceSelection((String)transferData);
//                    return true;
//                }
//                
////                String s = (String)transferData;
////
////                File file = extractFileFromString(s);
////                if (file != null) {
////                    transferData = file;
////                }
//            }
//        } catch (Exception e) {
//            ErrorManager.getDefault().notify(e);
//
//            return false;
//        }
        
//        // XXX
////        if (dropPoint == null && insertPos == Position.NONE) {
//        if (dropPoint == null && insertPos == DomPosition.NONE) {
////            DesignerTopComp designerTC;
////            if (comp instanceof DesignerTopComp) {
////                designerTC = (DesignerTopComp)comp;
////            } else {
////                designerTC = (DesignerTopComp)SwingUtilities.getAncestorOfClass(DesignerTopComp.class, comp);
////            }
////            
////            if (designerTC != null) {
////                dropPoint = designerTC.getPastePosition();
////                // XXX #6185935 There should be always a point specified.
////                if (dropPoint == null) {
////                    // By default, the left upper corner.
////                    dropPoint = new Point(0,0);
////                }
////            }
////            dropPoint = webform.tcGetPastePosition();
//            dropPoint = webform.getPastePoint();
//        }

//        // Delay this operation so that the other listener, DesignerPane.
//        // DesignerDropListener, can provide a pixel position for this drop
//        // TODO Consider only doing this when in grid mode
//        // TODO Instead of creating a new runnable each time, at a minumum
//        //   keep the runnable around, or alternatively simply make this
//        //   object a Runnable and invokeLater on this.
////        final Object transfer = transferData;
//        SwingUtilities.invokeLater(new Runnable() {
//                public void run() {
////                    try {
////                        importDataDelayed(comp, t, transfer);
//                        importDataDelayed(comp, t);
////                    } catch (Exception e) {
////                        ErrorManager.getDefault().notify(e);
////                        return;
////                    }
//                }
//            });
//
//        // We don't actually know if the drop succeeded since we have to defer
//        // to pick up the grid coordinate...
//        // XXX TODO Provide your own impl of DropTarget to avoid scheduling later this task.
//        return true;
        // XXX #101880 Now we can do it sync (see DesignerPaneDropTarget#drop).
        return importDataExt(comp, t);
    }

    /**
     * This method causes a transfer to a component from a clipboard or a DND drop operation.  The
     * Transferable represents the data to be imported into the component.
     *
     * @param comp The component to receive the transfer.  This argument is provided to enable
     *  sharing of TransferHandlers by multiple components.
     * @param t  The data to import.
     * @param transferData The data to be transferred
     * @return true if the data was inserted into the component, false otherwise.
     * @todo XXX Passing in a transferable here is undesirable; since we'return delayed the
     *   transferable may have been "released" by the system (this appears to be the
     *   case on OSX. See if there's a way to factor it out. Slightly tricky because we need
     *   it in a call to computeActions, and computeActions also needs the drop position which
     *   depends the delay...
     */
//    private void importDataDelayed(JComponent comp, Transferable t/*, Object transferData*/) {
    private boolean importDataExt(JComponent comp, Transferable t) {
        if (comp == null) {
            return false;
        }

//        assert transferData != null;

//        assert webform.getPane() == comp;

        // XXX
        if (dropPoint == null && insertPos == DomPosition.NONE) {
            dropPoint = webform.getPastePoint();
        }
        
//        InteractionManager.clearPalette(webform); // Item already in transferable
        InteractionManager.stopCnCForWebForm(webform); // Item already in transferable
        
        
        // Disambiguate drop position
//        if (insertPos != Position.NONE) {
        if (insertPos != DomPosition.NONE) {
//            if (insertPos.isRendered()) {
//            if (MarkupService.isRenderedNode(insertPos.getNode())) {
            if (insertPos.isRenderedPosition()) {
                insertPos = insertPos.getSourcePosition();
            }

//            if (insertPos != Position.NONE) {
            if (insertPos != DomPosition.NONE) {
                dropPoint = null;
            }
        }
        
        // XXX Compute location. Faking the same switch like below
//        Location location;
//        if (transferData instanceof DisplayItem) {
//            location = computeLocationForPositions(DROP_CENTER, null, getDropPoint(), insertPos, true);
//        } else if (transferData instanceof DesignBean[]) {
//            location = computeLocationForPositions(DROP_CENTER, null, getDropPoint(), insertPos, true);
//        } else if (transferData instanceof org.openide.nodes.Node) {
//            location = computeLocationForPositions(DROP_CENTER, null, getDropPoint(), insertPos, true);
//        } else if (transferData instanceof File) {
//            location = computeLocationForPositions(DROP_CENTER, null, getDropPoint(), insertPos, true);
//        } else if (transferData instanceof String) {
//            location = computeLocationForPositions(DROP_CENTER, null, getDropPoint(), insertPos, true);
//        } else if (transferData instanceof List) {
//            location = computeLocationForPositions(DROP_CENTER, null, getDropPoint(), insertPos, true);
//        } else {
//            location = null;
//        }
        
        
//        DomProvider.CoordinateTranslator coordinateTranslator = GridHandler.getInstance();
//        DomProvider.CoordinateTranslator coordinateTranslator = webform.getGridHandler();
  
//        DomProvider.Location location = computeLocationForPositions(DROP_CENTER, null, getDropPoint(), insertPos, true);
        Point canvasPos = getDropPoint();
//        Position documentPos = insertPos;
//        Position computedDocumentPos = computeDocumentPosition(canvasPos, documentPos);
        DomPosition documentPos = insertPos;
        DomPosition computedDocumentPos = computeDocumentPosition(canvasPos, documentPos);
        
        Node documentPosNode = computedDocumentPos.getNode();
        int documentPosOffset = computedDocumentPos.getOffset();

//        CssBox box = computeDroppeeCssBox(canvasPos);
        // XXX #149583 Possible NPE.
        PageBox pageBox = webform.getPane().getPageBox();
        CssBox box = canvasPos == null || pageBox == null ? null : ModelViewMapper.findBox(pageBox, canvasPos.x, canvasPos.y);
        Element droppeeElement = box == null ? null : box.getElement();
//        DesignBean droppeeBean = box == null ? null : getDroppee(box);
        Element dropeeComponentRootElement = box == null ? null : getDropeeComponent(box);
        boolean isGrid = (!webform.isGridMode() && ((box == null) || !box.isGrid())) ? false : true;
//        DesignBean defaultParentBean = webform.getDefaultParentBean();
//        Element defaultParentComponentRootElement = webform.getDefaultParentComponent();
//        return doComputeLocationForPositions(facet, canvasPos, documentPosNode, documentPosOffset, getDropSize(), isGrid, droppeeElement, droppeeBean, defaultParentBean);
        
        
//        Object transferData = null;
//        try {
//            DataFlavor importFlavor = webform.getImportFlavor(t.getTransferDataFlavors());
//
//            if (importFlavor == null) {
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, 
//                        new IllegalStateException("Unusable transfer flavors " + Arrays.asList(t.getTransferDataFlavors()))); // NOI18N
//
//                return /*false*/;
//            }
//
//            // XXX What was before in SelectionTopComp.
//            if (importFlavor.getMimeType().startsWith("application/x-creator-")) { // NOI18N
//                /*return*/ webform.tcImportComponentData(comp, t);
//            } // TEMP
//
//            Class rc = importFlavor.getRepresentationClass();
//
//            transferData = t.getTransferData(importFlavor);
//
//            if (rc == String.class) {
//                // XXX #6332049 When in inline editing we shouldn't steal the paste
//                // (at least for the JTextComponent's.
//                // This is just a workaround, it shouldn't be done this way.
//                // actions should be created based on context (and inline editing
//                // context is diff from the designer pane one).
//                if(webform.getManager().isInlineEditing()) {
//                    Component focusOwner = FocusManager.getCurrentManager().getFocusOwner();
//                    if(focusOwner instanceof JTextComponent) {
//                        JTextComponent textComp = (JTextComponent)focusOwner;
//                        textComp.paste();
//                        return /*true*/;
//                    } 
//                }
//
//                // XXX Flowlayout mode?
////                if (webform.getPane().getCaret() != null) {
//                if (webform.getPane().hasCaret()) {
////                    webform.getPane().getCaret().replaceSelection((String)transferData);
//                    webform.getPane().replaceSelection((String)transferData);
//                    return /*true*/;
//                }
//            }
        
//        DomProvider.Location location = WebForm.getDomProviderService().computeLocationForPositions(null, canvasPos, documentPosNode, documentPosOffset, getDropSize(), isGrid, droppeeElement,
//                /*droppeeBean,*/dropeeComponentRootElement, /*defaultParentBean*/defaultParentComponentRootElement);
//        doImportDataDelayed(comp, t, transferData, location/*, coordinateTranslator*/);
          boolean success = webform.importData(comp, t, /*transferData,*/ canvasPos, documentPosNode, documentPosOffset, getDropSize(), isGrid,
                    droppeeElement, dropeeComponentRootElement, /*defaultParentComponentRootElement, coordinateTranslator,*/ dropAction);

//        } catch (Exception e) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
////            return /*false*/;
//        }
        
        dropSize = null;
//        insertPos = Position.NONE;
        insertPos = DomPosition.NONE;
        dropPoint = null;
        clearDropMatch();
        
        return success;
    }
    
//    private void doImportDataDelayed(JComponent comp, Transferable t, Object transferData,
//    DomProvider.Location location, DomProvider.CoordinateTranslator coordinateTranslator) {
////        LiveUnit unit = webform.getModel().getLiveUnit();
////        if (unit == null) {
////            NotifyDescriptor d =
////                new NotifyDescriptor.Message(NbBundle.getMessage(DndHandler.class, "NoHtmlDrops"),
////                    NotifyDescriptor.WARNING_MESSAGE);
////            DialogDisplayer.getDefault().notify(d);
//////            dropSize = null;
//////            insertPos = Position.NONE;
//////            dropPoint = null;
////            
//////            clearDropMatch();
////
////            return;
////        }
////
////        // wrap process in a try to allow cleanup in the finally
////        try {
////            comp.setCursor(org.openide.util.Utilities.createProgressCursor(comp));
////
////            // if we are importing to the same component that we exported from then don't actually do
////            // anything if the drop location is inside the drag location and set shouldRemove to false
////            // so that exportDone knows not to remove any data
////            if (transferData instanceof DisplayItem) {
////                // Create a new type
////                DisplayItem item = (DisplayItem)transferData;
////
//////                Location location = computePositions(null, DROP_CENTER, null, getDropPoint(), insertPos, true);
////                // Todo: pass in a set instead
//////                doImportItem(item, null, DROP_CENTER, null, null, location);
////                importBean(new DisplayItem[] {item}, null, DROP_CENTER, null, null, location, coordinateTranslator);
////            } else if (transferData instanceof DesignBean[]) {
////                DesignBean[] beans = (DesignBean[])transferData;
//////                Location location =
//////                    computePositions(null, DROP_CENTER, null, getDropPoint(), insertPos, true);
//////                DesignBean droppee = location.droppee;
//////
//////                if (droppee != null) {
//////                    Location location2 =
//////                        computePositions(droppee, DROP_CENTER, null, null, null, false);
//////                    doBindOrMoveItems(dropAction, beans, t, droppee, DROP_CENTER, null, location2);
//////                }
////                DesignBean droppee = location.droppee;
////                if (droppee != null) {
//////                    location = computePositions(droppee1, DROP_CENTER, null, null, null, false);
////                    location = computeLocationForBean(droppee, DROP_CENTER, null, null, getDropSize(), webform.getModel());
////                    doBindOrMoveItems(dropAction, beans, t, droppee, DROP_CENTER, null, location);
////                }
////            } else if (transferData instanceof org.openide.nodes.Node) {
////                org.openide.nodes.Node node = (org.openide.nodes.Node)transferData;
////                DataObject dobj = (DataObject)node.getCookie(DataObject.class);
////
////                if (dobj != null) {
////                    FileObject fo = dobj.getPrimaryFile();
//////                    String rel = DesignerUtils.getPageRelativePath(webform, fo);
////                    String rel = getPageRelativePath(webform.getProject(), fo);
////                    
////                    Project fileProject = FileOwnerQuery.getOwner(fo);
////
////                    if (fileProject != webform.getProject()) {
////                        // Import file into our project first
////                        FileObject webitem = webform.getDataObject().getPrimaryFile();
////
////                        try {
////                            if (DesignerUtils.isImage(fo.getExt()) ||
////                                    DesignerUtils.isStylesheet(fo.getExt())) {
////                                // Import web context relative rather than file relative
////                                DesignProject project =
////                                    webform.getModel().getLiveUnit().getProject();
////                                File file = FileUtil.toFile(fo);
////                                URL url = file.toURI().toURL();
////                                rel = RESOURCES + UrlPropertyEditor.encodeUrl(file.getName());
////                                project.addResource(url, new URI(WEB + rel));
////                            } else {
////                                URL url = fo.getURL();
////                                rel = JsfProjectUtils.addResource(webitem, url, true);
////                            }
////                        } catch (FileStateInvalidException fse) {
////                            ErrorManager.getDefault().notify(fse);
////                        }
////                    }
////
////                    if (DesignerUtils.isImage(fo.getExt())) {
//////                        Location location =
//////                            computePositions(null, DROP_CENTER, null, getDropPoint(), insertPos, true);
////                        importImage(rel, location, coordinateTranslator);
////                    } else if (DesignerUtils.isStylesheet(fo.getExt())) {
////                        importStylesheet(rel);
////                    }
////
////                    //} else if (node instanceof org.netbeans.modules.properties.KeyNode) {
////                    //    // Compute the value binding expression:
////                    //    //  #{bundle.key}
////                    //    // But I need to ensure that the bundle file is included somewhere in
////                    //    // the page, and use the variable in the above.
////                    //    // The key can be found from this node via a cookie, but it looks like
////                    //    // the properties code is using old-style openide.src api calls, so
////                    //    // I'd hate to include it.
////                }
////            } else if (transferData instanceof File) {
////                File f = (File)transferData;
//////                Location location = computePositions(null, DROP_CENTER, null, getDropPoint(), insertPos, true);
////                importFile(f, null, location, coordinateTranslator);
////            } else if (transferData instanceof String) {
////                String s = (String)transferData;
////                
////                File file = extractFileFromString(s);
////                if (file != null) {
////                    importFile(file, null, location, coordinateTranslator);
////                } else {
////                
////                    s = DesignerUtils.truncateString(s, 600);
//////                    Location location =
//////                        computePositions(null, DROP_CENTER, null, getDropPoint(), insertPos, true);
////                    importString(s, location, coordinateTranslator);
////                }
////            } else if (transferData instanceof List) {
////                // TODO: place this under a single undo unit?
////                List list = (List)transferData;
////                Iterator it = list.iterator();
////                JPanel panel = null;
////
////                while (it.hasNext()) {
////                    Object o = it.next();
////
////                    if (o instanceof File) {
////                        File f = (File)o;
//////                        Location location = computePositions(null, DROP_CENTER, null, getDropPoint(), insertPos, true);
////                        panel = importFile(f, panel, location, coordinateTranslator);
////                    }
////                }
////            } else {
////                assert false : transferData;
////            }
////        } catch (Exception e) {
////            ErrorManager.getDefault().notify(e);
////
////            return;
////        } finally {
////            if (comp != null) {
////                comp.setCursor(null);
////            }
////
//////            dropSize = null;
//////            insertPos = Position.NONE;
//////            dropPoint = null;
//////            clearDropMatch();
////        }
//        webform.importData(comp, t, transferData, getDropSize(), location, coordinateTranslator, dropAction);
//    }

//    private static File extractFileFromString(String string) {
//        // We don't know if the String passed in to us represents
//        // an actual String, or a pointer to an actual file
//        // on disk. (For example, some operating systems where you
//        // drag an image file from the desktop and drop it on Creator
//        // will pass in e.g.   "file:/Users/luser/foo.gif". 
//        // In this case we should detect that we're really dealing
//        // with a path, not a literal String.
//        // To do that, we do an -experimental- URL parse, and if
//        // it suceeds, we assume we're dealing with a path, otherwise
//        // it's a plain string.
//        try {
//            // Try to construct a URL; if it's a url do a file
//            // import of that file
//            String urlString = string.trim();
//            URL url = new URL(urlString);
//            urlString = url.toExternalForm();
//
//            // looks like an okay url
//            if (url.getProtocol().equals("file")) { // NOI18N
//
//                // <markup_separation>
////                        String filename = MarkupUnit.fromURL(urlString);
//                // ====
//                String filename = InSyncService.getProvider().fromURL(urlString);
//                // </markup_separation>
//
//                if (filename != urlString) {
//                    File file = new File(filename);
//
//                    if (file.exists()) {
//                        return file;
//                    }
//
//                    // fall through for normal string handling
//                }
//            }
//        } catch (MalformedURLException mue) {
//            // It's just normal text; fall through
//            // NOTE: This is not an error condition!! We don't know that
//            // the string reprents a URL - it was just a hypothesis we're
//            // testing! When this fails we know that the hypothesis
//            // was wrong.
//        }
//        
//        return null;
//    }
    
    
//    private JPanel importFile(final File f, JPanel panel, Location location, CoordinateTranslator coordinateTranslator) {
//        if (f.exists()) {
//            String name = f.getName();
//            String extension = name.substring(name.lastIndexOf(".") + 1); // NOI18N
//            Project project = webform.getModel().getProject();
//
//            //String mime = FileUtil.getMIMEType(extension);
//            // They've only registered gif and jpg so not a big deal
//            if (DesignerUtils.isImage(extension)) {
////                Location location =
////                    computePositions(null, DROP_CENTER, null, getDropPoint(), insertPos, true);
//                importImage(f, location, coordinateTranslator);
//
//                return panel;
//            } else if (DesignerUtils.isStylesheet(extension)) {
//                importStylesheet(f);
//
//                return panel;
//            }
//
//// <dep> XXX Getting rid of dep on project/importpage.
//// TODO There should be a better API created.
////            panel = PageImport.importRandomFile(project, f, extension, panel);
//// ====
//            Lookup l = Lookup.getDefault();
//            Lookup.Template template = new Lookup.Template(Importable.class);
//            Iterator it = l.lookup(template).allInstances().iterator();
//            while (it.hasNext()) {
//                Object importable = it.next();
//                if(importable instanceof Importable.PageImportable) {
//                    panel = ((Importable.PageImportable)importable).importRandomFile(project, f, extension, panel);
//                    break;
//                }
//            }
//// </dep>
//
//            if (panel == null) {
//                JsfProjectUtils.importFile(webform.getModel().getProject(), f);
//            }
//        }
//
//        return panel;
//    }

//    private void importImage(final File file, Location location, CoordinateTranslator coordinateTranslator) {
//        try {
//            URL url = file.toURI().toURL();
//
//            // Import web context relative rather than file relative
//            //FileObject webitem = webform.getDataObject().getPrimaryFile();
//            //String local = JsfProjectHelper.addResource(webitem, url, true);
//            DesignProject project = webform.getModel().getLiveUnit().getProject();
//            String local = RESOURCES + UrlPropertyEditor.encodeUrl(file.getName());
//            project.addResource(url, new URI(WEB + local));
//
//            importImage(local, location, coordinateTranslator);
//        } catch (Exception ex) {
//            ErrorManager.getDefault().notify(ex);
//        }
//    }
//
//    private void importImage(final String local, Location location, CoordinateTranslator coordinateTranslator) {
//        // Import the file.
//        // If it's an image, just create an image component for it
//        // and drop it on the page.  (If there are multiple images,
//        // don't position them. This will happen automatically
//        // because we will clear the position after the first dropped
//        // image.)
//        // If it's a stylesheet, add it as a stylesheet.
//        // Otherwise consult the import mechanism (e.g. for html,
//        // jsp, and friends).
//        // For image I still need the position, so delay slightly.
////        Location location =
////            computePositions(null, DROP_CENTER, null, getDropPoint(), insertPos, true);
//        DesignBean droppee = location.droppee;
////        Document document = webform.getDocument();
//
//        String description = NbBundle.getMessage(DndHandler.class, "DropComponent"); // NOI18N
//        UndoEvent undoEvent = webform.getModel().writeLock(description);
//        try {
////            String description = NbBundle.getMessage(DndHandler.class, "DropComponent"); // NOI18N
////            document.writeLock(description);
//
//            String className;
//            String propertyName;
//
//            // XXX This should be decided by the parent bean.
//            // I.e. appropriate api is missing.
////            if (DesignerUtils.isBraveheartPage(webform.getJspDom())) {
//            // XXX This shouldn't be here resolved, but in parent bean.
//            if (InSyncService.getProvider().isBraveheartPage(webform.getJspDom())) {
//                className = "com.sun.rave.web.ui.component.ImageComponent"; // NOI18N
//                propertyName = "url";
//            } else {
//                className = "javax.faces.component.html.HtmlGraphicImage"; // NOI18N
//                propertyName = "value";
//            }
//
//            DesignBean parent = findParent(className, droppee, location.pos.getUnderParent(), true);
//            DesignBean bean = createBean(className, parent, location.pos, null);
//            select = bean;
//
//            if (bean instanceof MarkupDesignBean) {
//                MarkupDesignBean mbean = (MarkupDesignBean)bean;
//                positionBean(mbean, parent, mbean.getElement(), location, coordinateTranslator);
//            }
//
////            selectBean(select);
//            webform.getSelection().selectBean(select);
//            select = null;
//
//            DesignProperty prop = bean.getProperty(propertyName);
//
//            if (prop != null) {
//                prop.setValue(local);
//            }
//
//            //inlineEdit(beans);
//        } finally {
////            document.writeUnlock();
//            webform.getModel().writeUnlock(undoEvent);
//        }
//    }

    void importString(String string, Point point/*, DomProvider.CoordinateTranslator coordinateTranslator*/) {
        setDropPoint(point);
        //setInsertPosition(getPasteMarkupPosition());

//        DomProvider.Location location =
//            computeLocationForPositions(DROP_CENTER, null, getDropPoint(), insertPos, true);
        Point canvasPos = getDropPoint();
//        Position documentPos = insertPos;
//        Position computedDocumentPos = computeDocumentPosition(canvasPos, documentPos);
        DomPosition documentPos = insertPos;
        DomPosition computedDocumentPos = computeDocumentPosition(canvasPos, documentPos);
        
        Node documentPosNode = computedDocumentPos.getNode();
        int documentPosOffset = computedDocumentPos.getOffset();

//        CssBox box = computeDroppeeCssBox(canvasPos);
        CssBox box = canvasPos == null ? null : ModelViewMapper.findBox(webform.getPane().getPageBox(), canvasPos.x, canvasPos.y);
        Element droppeeElement = box == null ? null : box.getElement();
//        DesignBean droppeeBean = box == null ? null : getDroppee(box);
        Element dropeeComponentRootElement = box == null ? null : getDropeeComponent(box);
        boolean isGrid = (!webform.isGridMode() && ((box == null) || !box.isGrid())) ? false : true;
//        DesignBean defaultParentBean = webform.getDefaultParentBean();
//        Element defaultParentComponentRootElement = webform.getDefaultParentComponent();
//        return doComputeLocationForPositions(facet, canvasPos, documentPosNode, documentPosOffset, getDropSize(), isGrid, droppeeElement, droppeeBean, defaultParentBean);
        
//        DomProvider.Location location = WebForm.getDomProviderService().computeLocationForPositions(null, canvasPos, documentPosNode, documentPosOffset, getDropSize(), isGrid, droppeeElement,
//                /*droppeeBean,*/dropeeComponentRootElement, /*defaultParentBean*/defaultParentComponentRootElement);
//        importString(string, location, coordinateTranslator);
        webform.importString(string, canvasPos, documentPosNode, documentPosOffset, getDropSize(), isGrid, droppeeElement, dropeeComponentRootElement /*, defaultParentComponentRootElement, coordinateTranslator*/);
    }
    
//    void importString(String string, DomProvider.Location location, DomProvider.CoordinateTranslator coordinateTranslator) {
////        // Import the string as part of an output text component
//////        Location location =
//////            computePositions(null, DROP_CENTER, null, getDropPoint(), insertPos, true);
////        DesignBean droppee = location.droppee;
////
//////        Document document = webform.getDocument();
////
////        String description = NbBundle.getMessage(DndHandler.class, "DropComponent"); // NOI18N
////        UndoEvent undoEvent = webform.getModel().writeLock(description);
////        try {
//////            String description = NbBundle.getMessage(DndHandler.class, "DropComponent"); // NOI18N
//////            document.writeLock(description);
////
////            String className;
////            String propertyName;
////
////            // XXX This should be decided by the parent bean.
////            // I.e. appropriate api is missing.
//////            if (DesignerUtils.isBraveheartPage(webform.getJspDom())) {
////            // XXX This shouldn't be here resolved, but in parent bean.
////            if (InSyncService.getProvider().isBraveheartPage(webform.getJspDom())) {
////                className = "com.sun.rave.web.ui.component.StaticText"; // NOI18N
////                propertyName = "text";
////            } else {
////                className = "javax.faces.component.html.HtmlOutputText"; // NOI18N
////                propertyName = "value";
////            }
////
////            DesignBean parent = findParent(className, droppee, location.pos.getUnderParent(), true);
////            DesignBean bean = createBean(className, parent, location.pos, null);
////            select = bean;
////
////            if (bean instanceof MarkupDesignBean) {
////                MarkupDesignBean mbean = (MarkupDesignBean)bean;
////                positionBean(mbean, parent, mbean.getElement(), location, coordinateTranslator);
////            }
////
//////            selectBean(select);
////            webform.getSelection().selectBean(select);
////            select = null;
////
////            DesignProperty prop = bean.getProperty(propertyName);
////
////            if (prop != null) {
////                // Clean up string a little
////                // TODO - should I look for <HTML> markup and if so unset the escape property?
////                string = string.replace('\n', ' ');
////                string = string.replace('\r', ' ');
////                prop.setValue(string);
////            }
////
////            //inlineEdit(beans);
////        } finally {
//////            document.writeUnlock();
////            webform.getModel().writeUnlock(undoEvent);
////        }
//        webform.importString(string, location, coordinateTranslator);
//    }

//    private void importStylesheet(final File file) {
//        try {
//            URL url = file.toURI().toURL();
//
//            // Import web context relative rather than file relative
//            //FileObject webitem = webform.getDataObject().getPrimaryFile();
//            //String local = JsfProjectHelper.addResource(webitem, url, true);
//            DesignProject project = webform.getModel().getLiveUnit().getProject();
//            String local = RESOURCES + UrlPropertyEditor.encodeUrl(file.getName());
//            project.addResource(url, new URI(WEB + local));
//
//            importStylesheet(local);
//        } catch (Exception ex) {
//            ErrorManager.getDefault().notify(ex);
//        }
//    }
//
//    private void importStylesheet(final String local) {
////        Document document = webform.getDocument();
//
//        //ArrayList beanItems = new ArrayList();
//        String description = NbBundle.getMessage(DndHandler.class, "DropComponent"); // NOI18N
//        UndoEvent undoEvent = webform.getModel().writeLock(description);
//        try {
////            String description = NbBundle.getMessage(DndHandler.class, "DropComponent"); // NOI18N
////            document.writeLock(description);
//
//            // Add stylesheet link
//            org.w3c.dom.Document dom = webform.getJspDom();
//            Element root = dom.getDocumentElement();
//            MarkupUnit markup = webform.getMarkup();
//            Element html = markup.findHtmlTag(root);
//            DesignBean bean = null;
//
//            if (html == null) {
//                DesignBean uihead = null;
//                LiveUnit lu = webform.getModel().getLiveUnit();
//                DesignBean[] heads = lu.getBeansOfType(com.sun.rave.web.ui.component.Head.class);
//
//                if ((heads != null) && (heads.length > 0)) {
//                    uihead = heads[0];
//
//                    if (uihead != null) {
//                        // No stylesheet link exists - add one
//                        // XXX TODO get rid of using xhtml directly, 
//                        // it should be shielded by api.
//                        bean = lu.createBean(com.sun.rave.web.ui.component.Link.class.getName(), uihead,
//                                new Position()); // NOI18N
//                        bean.getProperty("url").setValue(local); // NOI18N
//                    }
//                }
//            } else {
//                // Gotta replace with HtmlTag.LINK.name
//                Element head = Util.findChild(HtmlTag.HEAD.name, html, false);
//                // XXX TODO get rid of using xhtml directly, 
//                // it should be shielded by api.
//                bean = webform.getDocument().createBean(org.netbeans.modules.visualweb.xhtml.Link.class.getName(), head, null);
//                bean.getProperty("href").setValue(local); // NOI18N
//            }
//
//            if (bean == null) {
//                return;
//            }
//
//            bean.getProperty("rel").setValue("stylesheet"); // NOI18N
//            bean.getProperty("type").setValue("text/css"); // NOI18N
//        } finally {
////            document.writeUnlock();
//            webform.getModel().writeUnlock(undoEvent);
//        }
//
//        webform.refresh(true);
//    }

//    /**
//     * @return true iff the palette item was inserted successfully
//     * @param beans An empty list into which the created beans will be added, or null
//     *    if caller is not interested in the result
//     */
//    public boolean importItem(DisplayItem item, DesignBean parent, int nodePos, String facet,
//    List beans, DomProvider.CoordinateTranslator coordinateTranslator) throws IOException {
////        Location location = computePositions(parent, nodePos, facet, getDropPoint(), insertPos, true);
//        DomProvider.Location location = computeLocationForBean(parent, nodePos, facet, getDropPoint(), getDropSize(), webform);
////        return doImportItem(item, parent, nodePos, facet, beans, location);
//        return importBean(new DisplayItem[] {item}, parent, nodePos, facet, beans, location, coordinateTranslator);
//    }
    
//    private boolean doImportItem(DisplayItem item, DesignBean parent, int nodePos, String facet,
//    List beans, Location location) throws IOException {
//        if (item instanceof DisplayItem) {
//            return importBean(new DisplayItem[] { (DisplayItem)item }, parent, nodePos,
//                facet, beans, location);
//        } else {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
//                    new IllegalStateException("Unable to import item=" + item)); // NOI18N
//
//            return false;
//        }
//    }

//    public void bindOrMoveItems(int dropAction, DesignBean[] beans, Transferable t,
//    DesignBean dropNode, int nodePos, String facet) {
////        Location location = computePositions((DesignBean)dropNode, nodePos, facet, null, null, false);
//        Location location = computeLocationForBean((DesignBean)dropNode, nodePos, facet, null, getDropSize(), webform.getModel());
//        doBindOrMoveItems(dropAction, beans, t, dropNode, nodePos, facet, location);
//    }
//    
//    private void doBindOrMoveItems(int dropAction, DesignBean[] beans, Transferable t,
//    DesignBean dropNode, int nodePos, String facet, Location location) {
//        if(DesignerUtils.DEBUG) {
//            DesignerUtils.debugLog(getClass().getName() + ".bindOrMoveItems(int, DesignBean[], Transferable, DesignBean, int, String)");
//        }
//        if(t == null) {
//            throw(new IllegalArgumentException("Null transferable."));
//        }
//        if ((beans == null) || (beans.length == 0)) {
//            return;
//        }
//
//        // It's a app outline drag: either move or link. Don't involve
//        // the transfer handler.
//        int allowed = computeActions(dropNode, t, false, nodePos);
//
//        if (allowed == DnDConstants.ACTION_NONE) {
//            return;
//        }
//
//        if (dropAction == DnDConstants.ACTION_COPY) {
//            LiveUnit unit = webform.getModel().getLiveUnit();
//            Transferable newTransferable = unit.copyBeans(beans);
//
//            if (newTransferable == null) {
//                return;
//            }
//
////            Location location =
////                computePositions((DesignBean)dropNode, nodePos, facet, null, null, false);
//            DesignBean parent = location.droppee;
//            SelectionTopComp.pasteBeans(webform, t, parent, location.pos, null);
//
//            return;
//        } else if (nodePos != DROP_CENTER) {
//            // MOVE: fall through to handle
//        } else if ((dropAction == DnDConstants.ACTION_LINK) ||
//                ((dropAction == DnDConstants.ACTION_MOVE) &&
//                ((allowed & DnDConstants.ACTION_LINK) != 0))) {
//            // LINK
//            // (We treat a move when link is permitted as a link since move
//            // is where you haven't selected any modifier keys so
//            // it's the mode where we make a best guess as to what
//            // you want. It would be better if we had a modifier key
//            // to let the user FORCE move though. Perhaps we should rethink
//            // this since there IS a modifier key for link (ctrl-shift).
//            ArrayList list = new ArrayList(beans.length);
//
//            for (int i = 0; i < beans.length; i++) {
//                list.add(beans[i]);
//            }
//
//            assert nodePos == DROP_CENTER;
//            handleLinks((DesignBean)dropNode, list);
//
//            return;
//        } else if ((dropAction & DnDConstants.ACTION_MOVE) != 0) {
//            // MOVE: fall through to handle
//        }
//
//        // Move
////        Location location =
////            computePositions((DesignBean)dropNode, nodePos, facet, null, null, false);
//        DesignBean parent = location.droppee;
//        moveBeans(webform, beans, parent, location.pos);
//    }

//    /**
//     * Move the given beans to the given parent and markup position.
//     */
//    private static void moveBeans(WebForm webform, DesignBean[] beans, DesignBean parent,
//        MarkupPosition pos) {
//        if ((beans == null) || (beans.length == 0)) {
//            return;
//        }
//
////        Document document = null;
////
////        if (webform != null) {
////            // XXX what about locking on java-only buffers? (SessionBean1 etc.)
////            document = webform.getDocument();
////        }
//        FacesModel facesModel = webform == null ? null : webform.getModel();
//
//        LiveUnit lu = (LiveUnit)beans[0].getDesignContext();
//
//        UndoEvent undoEvent;
//        if (facesModel != null) {
//            String description =
//                NbBundle.getMessage(SelectionTopComp.class,
//                    (beans.length > 1) ? "MoveComponents" // NOI18N
//                                       : "MoveComponent"); // NOI18N
//            undoEvent = facesModel.writeLock(description);
//        } else {
//            undoEvent = null; // No undo event
//            lu.writeLock(undoEvent);
//        }
//        
//        try {
////            if (document != null) {
////                String description =
////                    NbBundle.getMessage(SelectionTopComp.class,
////                        (beans.length > 1) ? "MoveComponents" // NOI18N
////                                           : "MoveComponent"); // NOI18N
////                document.writeLock(description);
////            } else {
////                lu.writeLock(null); // No undo event
////            }
//
//            // Decide whether we need to strip out position coordinates
//            // from the beans being moved
//            boolean stripPos = !isGridContext(parent, pos);
//
//            for (int i = 0; i < beans.length; i++) {
//                if (!(beans[i] instanceof MarkupDesignBean)) {
//                    continue;
//                }
//
//                MarkupDesignBean bean = (MarkupDesignBean)beans[i];
//
//                if (stripPos) {
//                    Element e = bean.getElement();
//
//                    try {
////                        webform.getDomSynchronizer().setUpdatesSuspended(bean, true);
//                        webform.setUpdatesSuspended(bean, true);
////                        CssLookup.removeLocalStyleValue(e, XhtmlCss.POSITION_INDEX);
////                        CssLookup.removeLocalStyleValue(e, XhtmlCss.LEFT_INDEX);
////                        CssLookup.removeLocalStyleValue(e, XhtmlCss.TOP_INDEX);
//                        CssProvider.getEngineService().removeLocalStyleValueForElement(e, XhtmlCss.POSITION_INDEX);
//                        CssProvider.getEngineService().removeLocalStyleValueForElement(e, XhtmlCss.LEFT_INDEX);
//                        CssProvider.getEngineService().removeLocalStyleValueForElement(e, XhtmlCss.TOP_INDEX);
//                    } finally {
////                        webform.getDomSynchronizer().setUpdatesSuspended(bean, false);
//                        webform.setUpdatesSuspended(bean, false);
//                    }
//                }
//
//                lu.moveBean(bean, parent, pos);
//            }
//        } finally {
////            if (document != null) {
////                document.writeUnlock();
////            } else {
////                lu.writeUnlock(null);
////            }
//            if (facesModel != null) {
//                facesModel.writeUnlock(undoEvent);
//            } else {
//                lu.writeUnlock(undoEvent);
//            }
//        }
//    }
    
    
//    private String[] getClasses(DisplayItem[] items) {
//        List list = new ArrayList(items.length);
//
//        for (int i = 0, n = items.length; i < n; i++) {
//            DisplayItem item = items[i];
//            if (item instanceof BeanCreateInfo) {
//                list.add(((BeanCreateInfo)item).getBeanClassName());
//            } else if (item instanceof BeanCreateInfoSet) {
//                String[] cls = ((BeanCreateInfoSet)item).getBeanClassNames();
//                for (int k = 0; k < cls.length; k++) {
//                    list.add(cls[k]);
//                }
//            } else {
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
//                        new IllegalStateException("Illegal item=" + item)); // NOI18N
//            }
//        }
//
//        String[] classNames = (String[])list.toArray(new String[list.size()]);
//
//        return classNames;
//    }

//    /**
//     * @param beans An empty list into which the created beans will be added, or null
//     *    if caller is not interested in the result
//     * @return true iff the bean palette item was inserted successfully
//     */
//    private boolean importBean(DisplayItem[] items, DesignBean origParent, int nodePos,
//    String facet, List createdBeans, DomProvider.Location location, DomProvider.CoordinateTranslator coordinateTranslator) throws IOException {
//        if(DesignerUtils.DEBUG) {
//            DesignerUtils.debugLog(getClass().getName() + ".importBean(DisplayItem[], DesignBean, int, String, List)");
//        }
//        if(items == null) {
//            throw(new IllegalArgumentException("Null items array."));
//        }
//        select = null;
//
////        Location location =
////            computePositions(origParent, nodePos, facet, getDropPoint(), insertPos, true);
//
//        // It's a app outline drag: either move or link. Don't involve
//        // the transfer handler.
//        String[] classes = getClasses(items);
//
//        if (classes != null) {
//            DesignBean[] beans = null;
//            boolean searchUp = true;
//
//            // Can always "move" from the palette - it's an implied copy.
//            // The explorer drag & drop is a bit weird about this - they
//            // only pass "move" as the valid operation, not copy.
//            int action = DnDConstants.ACTION_MOVE;
//
//            if (location.droppee == null) {
//                MarkupBean bean = webform.getModel().getFacesUnit().getDefaultParent();
//
//                if (bean != null) {
//                    location.droppee = webform.getModel().getLiveUnit().getDesignBean(bean);
//                }
//            }
//
//            int allowed =
//                computeActions(location.droppee, classes, beans, action, searchUp, nodePos);
//
//            if (allowed == DnDConstants.ACTION_NONE) {
//                return false;
//            }
//        }
//
////        Document document = webform.getDocument();
//        List beanItems = new ArrayList(); // HACK remove after TP
//
//        String description =
//            NbBundle.getMessage(DndHandler.class,
//                (items.length > 1) ? "DropComponents" : "DropComponent"); // NOI18N
//        UndoEvent undoEvent = webform.getModel().writeLock(description);
//        
//        // Don't want BeanPaletteItem.beanCreated; only want the
//        // set operation. For now the dataconnectivity module relies on
//        // this to name the instances, so we've gotta honor it.
//        try {
////            String description =
////                NbBundle.getMessage(DndHandler.class,
////                    (items.length > 1) ? "DropComponents" : "DropComponent"); // NOI18N
////            document.writeLock(description);
//
//            List beans = createBeans(location, items, beanItems, coordinateTranslator);
//
//            if (beans.isEmpty()) {
//                return false;
//            }
//
//            if (createdBeans != null) {
//                createdBeans.addAll(beans);
//            }
//
//            beansCreated(beans, beanItems);
//
//            processLinks(location.droppeeElement, null, beans, false, true, false);
//            customizeCreation(beans);
//
////            selectBean(select);
//            webform.getSelection().selectBean(select);
//            select = null;
//
////            inlineEdit(beans);
//            webform.getManager().inlineEdit(beans);
//
//            // Try to activate the designer surface! requestActive() isn't
//            // enough -- gotta force the multiview container to be activated
//            // and the right tab fronted!
//        } finally {
////            document.writeUnlock();
//            webform.getModel().writeUnlock(undoEvent);
//        }
//
//        return true;
//        return webform.importBean(items, origParent, nodePos, facet, createdBeans, location, coordinateTranslator);
//    }

//    private DesignBean getDroppee(CssBox box) {
    private Element getDropeeComponent(CssBox box) {
//        DesignBean origDroppee = ModelViewMapper.findComponent(box);
//        DesignBean origDroppee = ModelViewMapper.findMarkupDesignBean(box);
        Element origDropeeComponentRootElement = ModelViewMapper.findElement(box);

        // XXX Moved to methods in DomProviderImpl.
////        if (webform.isGridMode() && (origDroppee == null) &&
////                (webform.getModel().getLiveUnit() != null)) {
////            MarkupBean bean = webform.getModel().getFacesUnit().getDefaultParent();
////
////            if (bean != null) {
////                origDroppee = webform.getModel().getLiveUnit().getDesignBean(bean);
////            }
////        }
////        if (webform.isGridMode() && (origDroppee == null)) {
////            origDroppee = webform.getDefaultParentBean();
////        }
//        if (webform.isGridMode() && (origDropeeComponentRootElement == null)) {
////            origDroppee = webform.getDefaultParentBean();
//            origDropeeComponentRootElement = webform.getDefaultParentComponent();
//        }

//        return origDroppee;
        return origDropeeComponentRootElement;
    }

//    private Position computeDocumentPosition(Point canvasPos, Position documentPos) {
    private DomPosition computeDocumentPosition(Point canvasPos, DomPosition documentPos) {
//        if (canvasPos == null && documentPos == null && webform.getPane().getCaret() != null) {
        if (canvasPos == null && documentPos == null && webform.getPane().hasCaret()) {
            // The user is trying to insert a component without
            // dropping in a particular location; use the caret
            // position
//            DesignerCaret caret = webform.getPane().getCaret();
//            if (!caret.isReadOnlyRegion()) {
//                documentPos = caret.getDot();
//            }
            DesignerPane pane = webform.getPane();
            if (!pane.isCaretReadOnlyRegion()) {
                documentPos = pane.getCaretPosition();
            }
        }
        
        Node documentPosNode;
        int documentPosOffset;
//        if (documentPos != null && documentPos != Position.NONE && !Document.isReadOnlyRegion(documentPos)) {
//        if (documentPos != null && documentPos != Position.NONE && !InteractionManager.isReadOnlyRegion(documentPos)) {
        if (documentPos != null && documentPos != DomPosition.NONE && !InteractionManager.isReadOnlyRegion(documentPos)) {
//            documentPosNode = documentPos.getNode();
//            documentPosOffset = documentPos.getOffset();
            return documentPos;
        } else {
//            documentPosNode = null;
//            documentPosOffset = -1;
//            return Position.NONE;
            return DomPosition.NONE;
        }
    }
    
//    private DomProvider.Location computeLocationForPositions(
//            int where,
//            String facet,
//            Point canvasPos,
//            Position documentPos,
//            boolean split
//    ) {
////        return doComputePositions(bean, where, facet, canvasPos, documentPosNode, documentPosOffset, split, getDropSize(), isGrid, droppeeElement, droppeeBean, webform.getModel());
//        Position computedDocumentPos = computeDocumentPosition(canvasPos, documentPos);
//        Node documentPosNode = computedDocumentPos.getNode();
//        int documentPosOffset = computedDocumentPos.getOffset();
//
////        CssBox box = computeDroppeeCssBox(canvasPos);
//        CssBox box = canvasPos == null ? null : ModelViewMapper.findBox(webform.getPane().getPageBox(), canvasPos.x, canvasPos.y);
//        Element droppeeElement = box == null ? null : box.getElement();
////        DesignBean droppeeBean = box == null ? null : getDroppee(box);
//        Element dropeeComponentRootElement = box == null ? null : getDropeeComponent(box);
//        boolean isGrid = (!webform.isGridMode() && ((box == null) || !box.isGrid())) ? false : true;
////        DesignBean defaultParentBean = webform.getDefaultParentBean();
//        Element defaultParentComponentRootElement = webform.getDefautlParentComponent();
////        return doComputeLocationForPositions(facet, canvasPos, documentPosNode, documentPosOffset, getDropSize(), isGrid, droppeeElement, droppeeBean, defaultParentBean);
//        return WebForm.getDomProviderService().computeLocationForPositions(facet, canvasPos, documentPosNode, documentPosOffset, getDropSize(), isGrid, droppeeElement,
//                /*droppeeBean,*/dropeeComponentRootElement, /*defaultParentBean*/defaultParentComponentRootElement);
//    }
    
//    private static DomProvider.Location doComputeLocationForPositions(String facet, Point canvasPos, Node documentPosNode, int documentPosOffset,
//    Dimension dropSize, boolean isGrid, Element droppeeElement, DesignBean droppeeBean, /*WebForm webform*/DesignBean defaultParentBean) {
//        DomProvider.Location location = new DomProvider.Location();
//        location.facet = facet;
//        location.coordinates = canvasPos;
////        location.size = getDropSize();
//        location.size = dropSize;
//
//        DesignBean parent = null;
//        Node under = null;
//        Node before = null;
//
//        Element element = null;
//
//        if (documentPosNode != null) {
//            // XXX TODO: split text nodes!
//            if (documentPosNode instanceof Text) {
//                if (documentPosOffset == 0) {
//                    before = documentPosNode;
//                    under = before.getParentNode();
//                } else {
//                    Text txt = (Text)documentPosNode;
//
//                    if (documentPosOffset < txt.getLength()) {
//                        before = txt.splitText(documentPosOffset);
//                        under = before.getParentNode();
//                    } else {
//                        before = txt.getNextSibling();
//                        under = txt.getParentNode();
//                    }
//                }
//            } else {
//                int offset = documentPosOffset;
//
//                if (offset < documentPosNode.getChildNodes().getLength()) {
//                    under = documentPosNode;
//                    before = under.getChildNodes().item(offset);
//                } else {
//                    // Just append - but we don't have an api for that (can only set "before")
//                    // so create a new blank text node as a hook
//                    // XXX should I really mutate the document here?
//                    // That doesn't sound right...
//                    under = documentPosNode;
//
//                    // XXX Manipulation of the doc may not be done here.
////                        Text txt = webform.getJspDom().createTextNode(" "); // NOI18N
//                    Text txt = under.getOwnerDocument().createTextNode(" "); // NOI18N
//                    under.appendChild(txt);
//                    before = txt;
//                }
//            }
//
//            if (parent == null) {
//                Node n = under;
//
////                    while (n instanceof RaveElement) {
////                        RaveElement xel = (RaveElement)n;
//                while (n instanceof Element) {
//                    Element xel = (Element)n;
//
////                        if (xel.getDesignBean() != null) {
////                            DesignBean lbean = (DesignBean)xel.getDesignBean();
////                    DesignBean lbean = InSyncService.getProvider().getMarkupDesignBeanForElement(xel);
//                    DesignBean lbean = WebForm.getDomProviderService().getMarkupDesignBeanForElement(xel);
//                    if (lbean != null) {
//                        if (lbean.isContainer()) {
//                            parent = lbean;
//
//                            break;
//                        }
//                    }
//
//                    n = n.getParentNode();
//                }
//            }
//
//            //!CQ: facesUnit.setInsertBefore(before);
//            // determine the integer offset of the before node within its parent
//            if ((under != null) && (under.getNodeType() == Node.ELEMENT_NODE)) {
//                location.droppeeElement = (Element)under;
//            }
//        } else if (canvasPos != null) {
//            // What position should we assign here??? For now, nothing.
//            // Let insync pick a position. The exact location in the source
//            // where the tag is inserted isn't very important since we're
//            // absolute positioning anyway.
//            under = null;
//            before = null;
//
////                CssBox box = webform.getMapper().findBox(canvasPos.x, canvasPos.y);
////                CssBox box = ModelViewMapper.findBox(webform.getPane().getPageBox(), canvasPos.x, canvasPos.y);
////
////                // In flow mode, don't do absolute positioning, ever, unless we're in
////                // a grid positioning area
////                if (!webform.isGridMode() && ((box == null) || !box.isGrid())) {
////                    location.coordinates = null;
////                }
////
////                location.droppeeElement = box.getElement();
////                parent = getDroppee(box);
//            if (!isGrid) {
//                location.coordinates = null;
//            }
//            location.droppeeElement = droppeeElement;
//            parent = droppeeBean;
//        }
//
//        //            else {
//        //                // No position specified. In this case send just nulls
//        //                // to insync and let insync figure it out. It will insert
//        //                // the component most likely as a child of the form component.
//        //            }
//
//        location.droppee = parent;
//
//        // If default-positioning, try to place the component before the <br/>, if
//        // the the br is the last element under the default parent.
//        if ((under == null) && (before == null)) {
//            if (parent == null) {
////                parent = webform.getDefaultParentBean();
//                parent = defaultParentBean;
//            }
//            location.pos = getDefaultMarkupPositionUnderParent(parent/*, webform*/);
//        } else {
//            location.pos = new MarkupPosition(under, before);
//        }
//
//        return location;
//        
//    }
    
//    private static DomProvider.Location computeLocationForBean(DesignBean bean, int where, String facet, Point canvasPos, Dimension dropSize, WebForm webform) {
//        if (bean == null) {
//            throw new NullPointerException("Bean can't be null!"); // NOI18N
//        }
//        
//        DomProvider.Location location = new DomProvider.Location();
//        location.facet = facet;
//        location.coordinates = canvasPos;
////        location.size = getDropSize();
//        location.size = dropSize;
//
////        if ((bean != null) && !LiveUnit.isCssPositionable(bean)) {
//        if (bean instanceof MarkupDesignBean && !WebForm.getDomProviderService().isCssPositionable(
//                WebForm.getDomProviderService().getComponentRootElementForMarkupDesignBean((MarkupDesignBean)bean))) {
//            location.coordinates = null;
//        }
//
//        DesignBean parent = null;
//        Node under = null;
//        Node before = null;
//
//        Element element = null;
//
//        if (bean != null) {
////            element = FacesSupport.getElement(bean);
////            element = Util.getElement(bean);
//            element = WebForm.getDomProviderService().getElement(bean);
//
//            // No, can still reposition these guys.
//            //if (element == null) {
//            //    bean = null;
//            //}
//            location.droppeeElement = element;
//        }
//
//        //location.droppeeChosen = true;
//        if (where == DROP_CENTER) { // child of bean
//            parent = bean;
//            under = element;
//            before = null;
//        } else if (where == DROP_ABOVE) { // before bean
//            parent = bean.getBeanParent();
//            before = element;
//
//            if (element != null) {
//                under = element.getParentNode();
//            } else { // after bean
//                under = null;
//            }
//        } else {
//            parent = bean.getBeanParent();
//            assert where == DROP_BELOW;
//            before = null;
//
//            for (int i = 0, n = parent.getChildBeanCount(); i < (n - 1); i++) {
//                if (parent.getChildBean(i) == bean) {
//                    DesignBean next = parent.getChildBean(i + 1);
////                    Element nextElement = FacesSupport.getElement(next);
////                    Element nextElement = Util.getElement(next);
//                    Element nextElement = WebForm.getDomProviderService().getElement(next);
//                    before = nextElement;
//
//                    break;
//                }
//            }
//
//            if (element != null) {
//                under = element.getParentNode();
//            } else { // after bean
//                under = null;
//            }
//        }
//
//        location.droppee = parent;
//
//        // If default-positioning, try to place the component before the <br/>, if
//        // the the br is the last element under the default parent.
//        if ((under == null) && (before == null)) {
//            location.pos = getDefaultMarkupPositionUnderParent(parent, webform);
//        } else {
//            location.pos = new MarkupPosition(under, before);
//        }
//
//        return location;
//    }
    
//    /**
//     * Compute the target markup and bean positions to be used for a
//     * component insert, reposition or live pointer feedback.  <p> Given
//     * some parameters (prechosen position in the bean hierarchy
//     * (typically when you drop on the application outline), or a mouse
//     * coordinate or caret position (typically when dropping over the
//     * design surface)) return the position to be used.  <p> If a parent
//     * bean is specified, it takes precedence over a specified insert
//     * document position, which in turn takes precedence of a specified
//     * drop point on the canvas.
//     *
//     * @param bean The bean the user has pointed to, or null. The
//     * "where" parameter will indicate if we pointed directly at, or
//     * above or below, this bean. (If you point at a bean and it's a
//     * container, it probably means that you want to drop the new
//     * component as a child of this bean; if on the other hand you
//     * pointed below it you want to insert it as its sibling,
//     * immediately following this bean.)
//     * @param where Ignored if <code>bean</code> is null. Otherwise if
//     * DROP_ABOVE, you're pointing above the bean, if
//     * DROP_CENTER, you're pointing right at the bean,
//     * and if DROP_BELOW, you're pointing below the
//     * bean. These three values mean that the target position is
//     * before, as a child of, and after the bean respectively.
//     * @param documentPos If not null, indicates a document position
//     * where the component should be inserted (flow mode).
//     * @param canvasPos If not null, indicates a position in the canvas
//     * the user has pointed (absolute positioning mode).
//     * @param split If true, split a text node if necessary such that
//     * we can potentially insert in the middle of the text. This mutates
//     * the document.
//     *
//     * @return The parent DesignBean to be used
//     */
//    private static Location doComputePositions(DesignBean bean, int where, String facet, Point canvasPos,
//    /*Position documentPos,*/ Node documentPosNode, int documentPosOffset, boolean split, Dimension dropSize,
//    boolean isGrid, Element droppeeElement, DesignBean droppeeBean, FacesModel facesModel) {
//        Location location = new Location();
//        location.facet = facet;
//        location.coordinates = canvasPos;
////        location.size = getDropSize();
//        location.size = dropSize;
//
//        if ((bean != null) && !LiveUnit.isCssPositionable(bean)) {
//            location.coordinates = null;
//        }
//
//        DesignBean parent = null;
//        Node under = null;
//        Node before = null;
//
//        Element element = null;
//
//        if (bean != null) {
//            element = FacesSupport.getElement(bean);
//
//            // No, can still reposition these guys.
//            //if (element == null) {
//            //    bean = null;
//            //}
//            location.droppeeElement = element;
//        }
//
//        if (bean == null) {
////            if ((canvasPos == null) && (documentPos == null) &&
////                    (webform.getPane().getCaret() != null)) {
////                // The user is trying to insert a component without
////                // dropping in a particular location; use the caret
////                // position
////                DesignerCaret caret = webform.getPane().getCaret();
////
////                if (!caret.isReadOnlyRegion()) {
////                    documentPos = caret.getDot();
////                }
//
////            if ((documentPos != null) && (documentPos != Position.NONE) &&
////                    !Document.isReadOnlyRegion(documentPos)) {
////                // XXX TODO: split text nodes!
////                if (documentPos.getNode() instanceof Text) {
////                    if (documentPos.getOffset() == 0) {
////                        before = documentPos.getNode();
////                        under = before.getParentNode();
////                    } else {
////                        Text txt = (Text)documentPos.getNode();
////
////                        if (documentPos.getOffset() < txt.getLength()) {
////                            before = txt.splitText(documentPos.getOffset());
////                            under = before.getParentNode();
////                        } else {
////                            before = txt.getNextSibling();
////                            under = txt.getParentNode();
////                        }
////                    }
////                } else {
////                    int offset = documentPos.getOffset();
////
////                    if (offset < documentPos.getNode().getChildNodes().getLength()) {
////                        under = documentPos.getNode();
////                        before = under.getChildNodes().item(offset);
////                    } else {
////                        // Just append - but we don't have an api for that (can only set "before")
////                        // so create a new blank text node as a hook
////                        // XXX should I really mutate the document here?
////                        // That doesn't sound right...
////                        under = documentPos.getNode();
////
////                        // XXX Manipulation of the doc may not be done here.
////                        Text txt = webform.getJspDom().createTextNode(" "); // NOI18N
////                        under.appendChild(txt);
////                        before = txt;
////                    }
////                }
//            if (documentPosNode != null) {
//                // XXX TODO: split text nodes!
//                if (documentPosNode instanceof Text) {
//                    if (documentPosOffset == 0) {
//                        before = documentPosNode;
//                        under = before.getParentNode();
//                    } else {
//                        Text txt = (Text)documentPosNode;
//
//                        if (documentPosOffset < txt.getLength()) {
//                            before = txt.splitText(documentPosOffset);
//                            under = before.getParentNode();
//                        } else {
//                            before = txt.getNextSibling();
//                            under = txt.getParentNode();
//                        }
//                    }
//                } else {
//                    int offset = documentPosOffset;
//
//                    if (offset < documentPosNode.getChildNodes().getLength()) {
//                        under = documentPosNode;
//                        before = under.getChildNodes().item(offset);
//                    } else {
//                        // Just append - but we don't have an api for that (can only set "before")
//                        // so create a new blank text node as a hook
//                        // XXX should I really mutate the document here?
//                        // That doesn't sound right...
//                        under = documentPosNode;
//
//                        // XXX Manipulation of the doc may not be done here.
////                        Text txt = webform.getJspDom().createTextNode(" "); // NOI18N
//                        Text txt = under.getOwnerDocument().createTextNode(" "); // NOI18N
//                        under.appendChild(txt);
//                        before = txt;
//                    }
//                }
//
//                if (parent == null) {
//                    Node n = under;
//
////                    while (n instanceof RaveElement) {
////                        RaveElement xel = (RaveElement)n;
//                    while (n instanceof Element) {
//                        Element xel = (Element)n;
//
////                        if (xel.getDesignBean() != null) {
////                            DesignBean lbean = (DesignBean)xel.getDesignBean();
//                        DesignBean lbean = InSyncService.getProvider().getMarkupDesignBeanForElement(xel);
//                        if (lbean != null) {
//                            if (lbean.isContainer()) {
//                                parent = lbean;
//
//                                break;
//                            }
//                        }
//
//                        n = n.getParentNode();
//                    }
//                }
//
//                //!CQ: facesUnit.setInsertBefore(before);
//                // determine the integer offset of the before node within its parent
//                if ((under != null) && (under.getNodeType() == Node.ELEMENT_NODE)) {
//                    location.droppeeElement = (Element)under;
//                }
//            } else if (canvasPos != null) {
//                // What position should we assign here??? For now, nothing.
//                // Let insync pick a position. The exact location in the source
//                // where the tag is inserted isn't very important since we're
//                // absolute positioning anyway.
//                under = null;
//                before = null;
//
////                CssBox box = webform.getMapper().findBox(canvasPos.x, canvasPos.y);
////                CssBox box = ModelViewMapper.findBox(webform.getPane().getPageBox(), canvasPos.x, canvasPos.y);
////
////                // In flow mode, don't do absolute positioning, ever, unless we're in
////                // a grid positioning area
////                if (!webform.isGridMode() && ((box == null) || !box.isGrid())) {
////                    location.coordinates = null;
////                }
////
////                location.droppeeElement = box.getElement();
////                parent = getDroppee(box);
//                if (!isGrid) {
//                    location.coordinates = null;
//                }
//                location.droppeeElement = droppeeElement;
//                parent = droppeeBean;
//            }
//
//            //            else {
//            //                // No position specified. In this case send just nulls
//            //                // to insync and let insync figure it out. It will insert
//            //                // the component most likely as a child of the form component.
//            //            }
//        } else {
//            //location.droppeeChosen = true;
//            if (where == DROP_CENTER) { // child of bean
//                parent = bean;
//                under = element;
//                before = null;
//            } else if (where == DROP_ABOVE) { // before bean
//                parent = bean.getBeanParent();
//                before = element;
//
//                if (element != null) {
//                    under = element.getParentNode();
//                } else { // after bean
//                    under = null;
//                }
//            } else {
//                parent = bean.getBeanParent();
//                assert where == DROP_BELOW;
//                before = null;
//
//                for (int i = 0, n = parent.getChildBeanCount(); i < (n - 1); i++) {
//                    if (parent.getChildBean(i) == bean) {
//                        DesignBean next = parent.getChildBean(i + 1);
//                        Element nextElement = FacesSupport.getElement(next);
//                        before = nextElement;
//
//                        break;
//                    }
//                }
//
//                if (element != null) {
//                    under = element.getParentNode();
//                } else { // after bean
//                    under = null;
//                }
//            }
//        }
//
//        location.droppee = parent;
//
//        // If default-positioning, try to place the component before the <br/>, if
//        // the the br is the last element under the default parent.
//        if ((under == null) && (before == null)) {
//            location.pos = getDefaultMarkupPositionUnderParent(parent, facesModel);
//        } else {
//            location.pos = new MarkupPosition(under, before);
//        }
//
//        return location;
//    }
    
//    private static MarkupPosition getDefaultMarkupPositionUnderParent(DesignBean parent/*, WebForm webform*/) {
////        Node under = null;
////        Node before = null;
////        if ((parent != null) && parent instanceof MarkupDesignBean) {
////            under = ((MarkupDesignBean)parent).getElement();
////        }
////
////        if (under == null) {
//////                under = webform.getModel().getFacesUnit().getDefaultParent().getElement();
////            under = facesModel.getFacesUnit().getDefaultParent().getElement();
////        }
////
////        if (under != null) {
////            NodeList children = under.getChildNodes();
////
////            if (children.getLength() > 0) {
////                Node last = children.item(children.getLength() - 1);
////
////                while (last != null) {
////                    if ((last.getNodeType() != Node.TEXT_NODE) ||
////                            !DesignerUtils.onlyWhitespace(last.getNodeValue())) {
////                        break;
////                    }
////
////                    last = last.getPreviousSibling();
////                }
////
////                if ((last != null) && (last.getNodeType() == Node.ELEMENT_NODE) &&
////                        last.getNodeName().equals(HtmlTag.BR.name)) {
////                    before = last;
////                }
////            }
////        }
////
////        return new MarkupPosition(under, before);
////        return webform.getDefaultMarkupPositionUnderParent(parent);
//        return WebForm.getDomProviderService().getDefaultMarkupPositionUnderParent(parent);
//    }

//    /** Figure out which kind of action we can do for the given
//     * transferable over the given droppee.
//     *
//     * @param droppee The target component
//     * @param transferable The transferable being considered dropped
//     *        or linked on the droppee. If it references multiple
//     *        components, it will set the allowable action union of
//     *        all the components.
//     * @param searchUp If true, you are permitted to search upwards
//     *        as well.
//     */
////    public int computeActions(DesignBean droppee, Transferable transferable, boolean searchUp,
////        int nodePos) {
//    public int computeActions(Element dropeeComponentRootElement, Transferable transferable, boolean searchUp,
//        int nodePos) {
////        if(DesignerUtils.DEBUG) {
////            DesignerUtils.debugLog(getClass().getName() + ".computeActions(DesignBean, Transferable, boolean, int)");
////        }
////        if(transferable == null) {
////            throw(new IllegalArgumentException("Null transferable."));
////        }
////        int action = DnDConstants.ACTION_NONE;
////        String[] classes = null;
////        DesignBean[] beans = null;
////        DataFlavor[] flavors = transferable.getTransferDataFlavors();
////
////        for (int j = 0; j < flavors.length; j++) {
////            Class clz = flavors[j].getRepresentationClass();
////
////            if (clz == DisplayItem.class) {
////                // Can always "move" from the palette - it's an implied copy.
////                // The explorer drag & drop is a bit weird about this - they
////                // only pass "move" as the valid operation, not copy.
////                action |= DnDConstants.ACTION_MOVE;
////
////                Object data;
////
////                try {
////                    data = transferable.getTransferData(flavors[j]);
////                } catch (Exception e) {
////                    ErrorManager.getDefault().notify(e);
////
////                    return action;
////                }
////
////                if (!(data instanceof DisplayItem)) {
////                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, 
////                            new IllegalStateException("Invalid DisplayItem transfer data: " + data)); // NOI18N
////
////                    return action;
////                }
////
////                List list = new ArrayList();
////                DisplayItem item = (DisplayItem)data;
////
////                if (item instanceof BeanCreateInfo) {
////                    BeanCreateInfo bci = (BeanCreateInfo)item;
////                    classes = new String[] { bci.getBeanClassName() };
////                } else if (item instanceof BeanCreateInfoSet) {
////                    BeanCreateInfoSet bcis = (BeanCreateInfoSet)item;
////                    classes = bcis.getBeanClassNames();
////                } else {
////                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
////                            new IllegalStateException("Illegal item=" + item)); // NOI18N
////                }
////
////                break;
////            } else if (clz == DesignBean.class) {
////                Object data;
////
////                try {
////                    data = transferable.getTransferData(flavors[j]);
////                } catch (IOException ex) {
////                    ErrorManager.getDefault().notify(ex);
////
////                    return action;
////                } catch (UnsupportedFlavorException ex) {
////                    ErrorManager.getDefault().notify(ex);
////
////                    return action;
////                }
////
////                if ((data != null) && data instanceof DesignBean[]) {
////                    beans = (DesignBean[])data;
////
////                    if (beans == null) {
////                        return action;
////                    }
////
////                    classes = new String[beans.length];
////
////                    for (int i = 0; i < beans.length; i++) {
////                        classes[i] = beans[i].getInstance().getClass().getName();
////                    }
////
////                    // See if we can move these beans. We can move if the
////                    // parent target location is not a child of any of the beans,
////                    // or the beans themselves
////                    boolean cannot = false;
////
////                    for (int i = 0; i < beans.length; i++) {
////                        DesignBean d = droppee;
////
////                        while (d != null) {
////                            if (d == beans[i]) {
////                                cannot = true;
////
////                                break;
////                            }
////
////                            d = d.getBeanParent();
////                        }
////                    }
////
////                    if (!cannot) {
////                        action |= DnDConstants.ACTION_MOVE;
////                    }
////
////                    break;
////                }
////            } else if (clz == LiveUnit.ClipImage.class) {
////                Object data;
////
////                try {
////                    data = transferable.getTransferData(flavors[j]);
////                } catch (Exception e) {
////                    ErrorManager.getDefault().notify(e);
////
////                    return action;
////                }
////
////                if (!(data instanceof LiveUnit.ClipImage)) {
////                    ErrorManager.getDefault().log("Invalid LiveUnit.ClipImage transfer data: " +
////                        data);
////
////                    return action;
////                }
////
////                LiveUnit.ClipImage luc = (LiveUnit.ClipImage)data;
////                classes = luc.getTypes();
////            }
////        }
////
////        if (classes == null) {
////            return action;
////        }
////
////        return computeActions(droppee, classes, beans, action, searchUp, nodePos);
////        return webform.computeActions(droppee, transferable, searchUp, nodePos);
//        return webform.computeActions(dropeeComponentRootElement, transferable, searchUp, nodePos);
//    }

//    private int computeActions(DesignBean origDroppee, String[] classes, DesignBean[] beans,
//        int action, boolean searchUp, int nodePos) {
//        DesignBean droppee = null;
//
//        if (nodePos == DROP_CENTER) { // Can only link if pointing at a node
//linkCheckFinished: 
//            for (int i = 0; i < classes.length; i++) {
//                try {
//                    Class clz = webform.getModel().getFacesUnit().getBeanClass(classes[i]);
//                    DesignBean lb = null;
//
//                    if (beans != null) {
//                        lb = beans[i];
//                    }
//
//                    droppee = origDroppee;
//
//                    for (droppee = origDroppee; droppee != null;
//                            droppee = droppee.getBeanParent()) {
//                        // Prevent self-linking
//                        if (beans != null) {
//                            boolean same = false;
//
//                            for (int j = 0; j < beans.length; j++) {
//                                if (droppee == beans[j]) {
//                                    same = true;
//
//                                    break;
//                                }
//                            }
//
//                            if (same) {
//                                if (!searchUp) {
//                                    break;
//                                } else {
//                                    continue;
//                                }
//                            }
//                        }
//
//                        DesignInfo dbi = droppee.getDesignInfo();
//
//                        if ((dbi != null) && dbi.acceptLink(droppee, lb, clz)) {
//                            action |= DnDConstants.ACTION_LINK;
//
//                            break linkCheckFinished;
//                        }
//
//                        if (!searchUp) {
//                            break;
//                        }
//                    }
//                } catch (Exception e) {
//                    ErrorManager.getDefault().notify(e);
//                }
//            }
//        } else {
//            // For pos=ABOVE or BELOW, the passed in node points to the specific
//            // node -sibling-, but we want the parent
//            origDroppee = origDroppee.getBeanParent();
//        }
//
//        // See if any of the droppee parents accept the new item as a
//        // child
//        for (int i = 0; i < classes.length; i++) {
//            DesignBean parent = findParent(classes[i], origDroppee, null, searchUp);
//
//            if (parent != null) {
//                action |= DnDConstants.ACTION_COPY;
//
//                break;
//            } else {
//                action &= ~DnDConstants.ACTION_MOVE;
//
//                break;
//            }
//        }
//
//        return action;
//    }

//    private List createBeans(Location location, DisplayItem[] items, List beanItems, CoordinateTranslator coordinateTranslator)
//        throws IOException {
//        DesignBean droppee = location.droppee;
//        MarkupPosition position = location.pos;
//        String facet = location.facet;
//
//        ArrayList created = new ArrayList(2 * items.length); // slop for BeanCreateInfoSets
//
//        for (int i = 0; i < items.length; i++) {
//
////            if (!(items[i] instanceof BeanPaletteItem)) {
////                importItem(items[i], null, DROP_CENTER, null, null);
////
////                continue;
////            }
//
//            DisplayItem item = (DisplayItem)items[i];
//            
//            // <change>
//            // XXX There is a need to get class name even from the bean create info set.
////            String className = item.getBeanClassName();
//// ====
//            String className = null;
//            // </change>
//
////            // Customize creation if requested by the component
////            BeanCreateInfo bci = item.getBeanCreateInfo();
////            BeanCreateInfoSet bcis = item.getBeanCreateInfoSet();
//
////            // At most one of the above should be set...
////            assert !((bci != null) && (bcis != null));
//
//            String[] classes = null;
//            int current = 0;
//            int max = 0;
//
//            if (item instanceof BeanCreateInfoSet) {
//                BeanCreateInfoSet bcis = (BeanCreateInfoSet)item;
//                // Set us up for multiple bean creation
//                classes = bcis.getBeanClassNames();
//                max = classes.length;
//            } else if (item instanceof BeanCreateInfo) {
//                className = ((BeanCreateInfo)item).getBeanClassName();
//            } else {
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
//                        new IllegalStateException("Illegal item=" + item)); // NOI18N
//                continue;
//            }
//
//            do {
//                // If we're creating multiple beans from a BeanCreateInfoSet
//                // fetch the next class name
//                if (current < max) {
//                    className = classes[current++];
//                }
//
//                DesignBean parent = findParent(className, droppee, position.getUnderParent(), true);
//
//                if (parent != null) {
//                    boolean droppingOnFrameset = parent.getInstance() instanceof FramesetFrameset;
//                    boolean droppingOnFrame = parent.getInstance() instanceof Frame;
//
//                    if (droppingOnFrameset || droppingOnFrame) {
//                        //                if (!(droppingOnFrameset && (className.equals(HtmlBean.PACKAGE+"Frame") ||
//                        //                                             className.equals(HtmlBean.PACKAGE+"FramesetFrameset")))) { // NOI18N
//                        if (!(droppingOnFrameset &&
//                                (className.equals(Frame.class.getName()) ||
//                                className.equals(Frame.class.getName())))) {
//                            NotifyDescriptor d =
//                                new NotifyDescriptor.Message(NbBundle.getMessage(DndHandler.class,
//                                        "NoFrameDrops", item.getDisplayName()),
//                                    NotifyDescriptor.WARNING_MESSAGE);
//                            DialogDisplayer.getDefault().notify(d);
//
//                            continue;
//                        }
//                    }
//                }
//
//                // Native method - is this result cached?
//                //if (className.equals(Jsp_Directive_Include.class.getName())) {
//                String savedClass = null;
//
//                if (className.equals(HtmlBean.PACKAGE + "Jsp_Directive_Include")) { // NOI18N
//
//                    // You're dropping a jsp:directive.include box. These cannot
//                    // be CSS positioned (e.g. dropped at a particular pixel
//                    // location over a grid layout area) - because this directive
//                    // does not have a style attribute, and Jasper will scream
//                    // bloody murder if we put it in.
//                    // So instead we drop a <div>, and place the jsp directive
//                    // inside it.
//                    // I wanted to solve this more cleanly: If you're dropping
//                    // a visual component and it doesn't have a "style" property,
//                    // it doesn't support positioning, so apply the above
//                    // <div>-wrapping trick. However, while I can check if a
//                    // component about to be dropped has a style property through
//                    // the beaninfo, I can't tell if it's a visual component
//                    // or not until it's actually instantiated (via
//                    // DesignBean.isVisualBean()) - so I can't do it this way or
//                    // suddenly e.g. rowset beans would be parented by <div> since
//                    // they don't have a style attribute....   So for now
//                    // it's just a special case check for the one known visual
//                    // component that doesn't have a style attribute.
//                    //if (getDropPoint() != null && insertPos == null) {
//                    savedClass = className;
//
//                    // XXX Why not Div.class.getName() ?
//                    className = HtmlBean.PACKAGE + "Div"; // NOI18N
//
//                    //}
//                    // Comment on above: we now ALWAYS want to insert a div, since
//                    // even in flow context you want a div which specifies
//                    // "position: relative" in order to ensure that absolutely
//                    // positioned children within the fragment are relative
//                    // to the jsp's top level corner, not the current viewport
//                    // or absolutely positioned -ancestor- of the jsp include
//                    // box.
//                }
//
//                // Adjust position, in case we're default inserting it
//                // If we're default-positioin inserting into a form or body,
//                // and it ends with a <br>, insert the new component before
//                // the <br> so that it doesn't create a new line.
//                if ((parent == null) &&
//                        ((position == null) ||
//                        ((position.getUnderParent() == null) &&
//                        (position.getBeforeSibling() == null)))) {
//                    // See if I have a Br
//                    MarkupBean formBean = webform.getModel().getFacesUnit().getDefaultParent();
//
//                    if (formBean != null) {
//                        Bean[] children = formBean.getChildren();
//
//                        if ((children != null) && (children.length > 0)) {
//                            Bean b = children[children.length - 1];
//
//                            if (b instanceof MarkupBean) {
//                                MarkupBean mb = (MarkupBean)b;
//
//                                if ((mb.getElement() != null) &&
//                                        mb.getElement().getTagName().equals(HtmlTag.BR.name)) {
//                                    position =
//                                        new MarkupPosition(formBean.getElement(), mb.getElement());
//                                }
//                            }
//                        }
//                    }
//                }
//
//                DesignBean bean = createBean(className, parent, position, facet);
//                select = bean;
//
//                if (bean != null) {
//                    created.add(bean);
//                    beanItems.add(items[i]);
//                }
//
//                if (bean instanceof MarkupDesignBean) {
//                    MarkupDesignBean mbean = (MarkupDesignBean)bean;
//                    positionBean(mbean, parent, mbean.getElement(), location, coordinateTranslator);
//
//                    if ((savedClass != null) && bean.isContainer()) {
//                        DesignBean child = createBean(savedClass, bean, null, null);
//
//                        if (child != null) {
//                            created.add(child);
//
//                            // Ensure that the two lists are kept in sync
//                            beanItems.add(null);
//                        }
//
//                        // If inserted in flow, put a <div> with relative
//                        // positioning around it to ensure that absolutely
//                        // positioned children in the div are absolute relative
//                        // to the jsp box, not whatever outer container is
//                        // establishing the current absolute positions
//                        if (insertPos != Position.NONE) {
//                            DesignProperty styleProp = bean.getProperty("style"); // NOI18N
//
//                            if (styleProp != null) {
//                                String mods = "position: relative"; // NOI18N
//                                String style = (String)styleProp.getValue();
//
//                                if ((style != null) && (style.length() > 0)) {
//                                    styleProp.setValue(style + "; " + mods);
//                                } else {
//                                    styleProp.setValue(mods);
//                                }
//                            }
//                        }
//
//                        /*
//                        DesignProperty styleProp = bean.getProperty("style"); // NOI18N
//                        if (styleProp != null) {
//                            String mods = "overflow: hidden; width: 240px"; // NOI18N
//                            String style = (String)styleProp.getValue();
//                            if (style != null && style.length() > 0) {
//                                styleProp.setValue(style + "; " + mods);
//                            } else {
//                                styleProp.setValue(mods);
//                            }
//                        }
//                        */
//                    }
//                }
//            } while (current < max);
//        }
//
//        insertPos = Position.NONE;
//
//        //facesUnit.setInsertBefore(null);
//        return created;
//    }

//    private DesignBean findParent(String className, DesignBean droppee, Node parentNode, boolean searchUp) {
////        if (webform.isGridMode() && (droppee == null) &&
////                (webform.getModel().getLiveUnit() != null)) {
////            MarkupBean bean = webform.getModel().getFacesUnit().getDefaultParent();
////
////            if (bean != null) {
////                droppee = webform.getModel().getLiveUnit().getDesignBean(bean);
////            }
////        }
////
////        DesignBean parent = droppee;
////
////        if (searchUp) {
////            for (; (parent != null) && !parent.isContainer(); parent = parent.getBeanParent()) {
////                ;
////            }
////        }
////
////        LiveUnit unit = webform.getModel().getLiveUnit();
////
////        if (searchUp) {
////            boolean isHtmlBean =
////                className.startsWith(HtmlBean.PACKAGE) &&
////                // f:verbatim is explicitly allowed where jsf components can go
////                // XXX Why not F_Verbatim.class.getName() ?
////                !(HtmlBean.PACKAGE + "F_Verbatim").equals(className); // NOI18N
////
////            if (isHtmlBean) {
////                // We can't drop anywhere below a "renders children" JSF
////                // component
////                parent = FacesSupport.findHtmlContainer(parent);
////            }
////        }
////
////        // Validate the parent: walk up the parent chain until you find
////        // a parent which will accept the child.
////        for (; parent != null; parent = parent.getBeanParent()) {
////            if (unit.canCreateBean(className, parent, null)) {
////                // Found it
////                break;
////            }
////
////            if (!searchUp) {
////                return null;
////            }
////        }
////
////        if ((parent == null) && (parentNode != null)) {
////            // Adjust hierarchy: we should pass in a parent
////            // pointer based on where we are: locate the closest
////            // jsf parent above
////            Node n = parentNode;
////            MarkupBean mb = null;
////
////            while (n != null) {
////                if (n instanceof Element) {
////                    Element e = (Element)n;
//////                    mb = FacesSupport.getMarkupBean(webform.getDocument(), e);
////                    mb = getMarkupBean(webform.getModel(), e);
////
////                    if (mb != null) {
////                        break;
////                    }
////                }
////
////                n = n.getParentNode();
////            }
////
////            if (mb != null) {
////                DesignBean lmb = webform.getModel().getLiveUnit().getDesignBean(mb);
////
////                if (lmb.isContainer()) {
////                    parent = lmb;
////                }
////            }
////
////            if (parent == null) {
////                parent = webform.getModel().getRootBean();
////            }
////        }
////
////        return parent;
//        return webform.findParent(className, droppee, parentNode, searchUp);
//    }

//    /**
//     * Given an element which possibly maps to a markup bean, return the corresponding bean.
//     */
//    private static MarkupBean getMarkupBean(FacesModel model, Element elem) {
////        FacesModel model = doc.getWebForm().getModel();
//
//        if (model == null) { // testsuite
//
//            return null;
//        }
//
//        FacesPageUnit facesunit = model.getFacesUnit();
//        MarkupBean bean = null;
//
//        if (facesunit != null) {
//            bean = facesunit.getMarkupBean(elem);
//
//            // Find component for this element:
//        }
//
//        return bean;
//    }
    
//    private DesignBean createBean(String className, DesignBean parent,
//        Position pos, String facet) {
//        LiveUnit unit = webform.getModel().getLiveUnit();
//
//        if (parent != null) {
//            // It's possible that we're adding to a unit other than
//            // the web form one -- such as a Session Bean unit for
//            // a rowset
//            unit = (LiveUnit)parent.getDesignContext();
//
//            // Ensure that the MarkupPosition is correct
//            if (pos instanceof MarkupPosition) {
//                MarkupPosition markupPos = (MarkupPosition)pos;
//
////                if (markupPos.getUnderParent() instanceof RaveElement) {
////                    RaveElement parentElement = (RaveElement)markupPos.getUnderParent();
//                if (markupPos.getUnderParent() instanceof Element) {
//                    Element parentElement = (Element)markupPos.getUnderParent();
//
////                    while (parentElement.getDesignBean() != parent) {
//                    while (InSyncService.getProvider().getMarkupDesignBeanForElement(parentElement) != parent) {
////                        if (parentElement.getParentNode() instanceof RaveElement) {
////                            parentElement = (RaveElement)parentElement.getParentNode();
//                        if (parentElement.getParentNode() instanceof Element) {
//                            parentElement = (Element)parentElement.getParentNode();
//                        } else {
//                            break;
//                        }
//                    }
//
//                    if ((parentElement != null) && (parentElement != markupPos.getUnderParent())) {
//                        // The parent DesignBean is for a higher-up element. This can happen
//                        // when the acceptChild/acceptParent calls force parenting up higher
//                        // in the chain.
//                        pos = new MarkupPosition(parentElement, null);
//                    }
//                }
//            }
//        }
//
//        if (facet != null) {
//            return unit.createFacet(facet, className, parent);
//        }
//
//        return unit.createBean(className, parent, pos);
//    }

//    private void beansCreated(List beans, List beanItems) {
//        int n = beans.size();
//
//        for (int i = 0; i < n; i++) {
//            DesignBean lb = (DesignBean)beans.get(i);
//
//            try {
//                webform.getModel().beanCreated(lb);
//            } catch (Exception e) {
//                ErrorManager.getDefault().notify(e);
//            }
//        }
//
//        for (int i = 0; i < n; i++) {
//            DisplayItem item = (DisplayItem)beanItems.get(i);
//
//            if (item == null) {
//                continue;
//            }
//
////            // Customize creation if requested by the component
////            BeanCreateInfo bci = item.getBeanCreateInfo();
////            BeanCreateInfoSet bcis = item.getBeanCreateInfoSet();
////
////            if ((bci == null) && (bcis == null)) {
////                continue;
////            }
////
////            // At most one of the above should be set...
////            assert !((bci != null) && (bcis != null));
//
//            // Multiple beans could have been created from this item....
//            // check that and process them
//            if (item instanceof BeanCreateInfoSet) {
//                BeanCreateInfoSet bcis = (BeanCreateInfoSet)item;
//                // I don't want to rely on bcis.getBeanClassNames().length because one or more
//                // beans could have failed to have been created (missing class, or
//                // designtime canCreate returned false, etc.) - which would lead to
//                // getting out of sync.
//                // So instead I count by checking for identical BeanCreateItems
//                // in subsequent beanItems entries
//                ArrayList list = new ArrayList();
//
//                for (int j = i; j < n; j++) {
//                    if (beanItems.get(j) == item) {
//                        list.add(beans.get(j));
//                    }
//                }
//
//                if (list.size() > 0) {
//                    i += (list.size() - 1);
//
//                    DesignBean[] createdBeans =
//                        (DesignBean[])list.toArray(new DesignBean[list.size()]);
//                    Result result = bcis.beansCreatedSetup(createdBeans);
//                    ResultHandler.handleResult(result, webform.getModel());
//                }
//            } else if (item instanceof BeanCreateInfo) {
//                BeanCreateInfo bci = (BeanCreateInfo)item;
//                DesignBean bean = (DesignBean)beans.get(i);
//                Result result = bci.beanCreatedSetup(bean);
//                ResultHandler.handleResult(result, webform.getModel());
//            } else {
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
//                        new IllegalStateException("Invalid item=" + item)); // NOI18N
//            }
//        }
//    }

//    /** Handle links where the target is a possibly nonvisual bean so has no element */
//    public void handleLinks(DesignBean droppee, ArrayList beans) {
////        Document document = webform.getDocument();
//
//        int n = beans.size();
//        String description =
//            NbBundle.getMessage(DndHandler.class, (n > 1) ? "LinkComponents" : "LinkComponent"); // NOI18N
//        UndoEvent undoEvent = webform.getModel().writeLock(description);
//        try {
////            int n = beans.size();
////            String description =
////                NbBundle.getMessage(DndHandler.class, (n > 1) ? "LinkComponents" : "LinkComponent"); // NOI18N
////            document.writeLock(description);
//
//            for (int i = 0; i < n; i++) {
//                DesignBean lb = (DesignBean)beans.get(i);
//
//                try {
//                    // If you drop on an existing component, see if they
//                    // can be wired together
//                    // Try to bind the two together - for example, if you
//                    // drop a RowSet on a bean that has a RowSet property,
//                    // the RowSet property is bound to this particular
//                    // RowSet.
//                    DesignInfo dbi = droppee.getDesignInfo();
//                    boolean canLink =
//                        (dbi != null) && dbi.acceptLink(droppee, lb, lb.getInstance().getClass());
//
//                    if (canLink) {
//                        MarkupDesignBean mbean = null;
//
//                        if (droppee instanceof MarkupDesignBean) {
//                            // link beans might perform lots and lots of
//                            // updates on the element - that's the case
//                            // for the data grid when you bind a table
//                            // to it for example.  So batch up all these
//                            // modifications into a single change event
//                            // on the top level element.
//                            mbean = (MarkupDesignBean)droppee;
////                            webform.getDomSynchronizer().setUpdatesSuspended(mbean, true);
//                            webform.setUpdatesSuspended(mbean, true);
//                        }
//
//                        try {
//                            webform.getModel().linkBeans(droppee, lb);
//                        } finally {
//                            if (mbean != null) {
//                                // Process queued up changes
////                                webform.getDomSynchronizer().setUpdatesSuspended(mbean, false);
//                                webform.setUpdatesSuspended(mbean, false);
//                            }
//                        }
//
//                        // The target bean should be selected instead of
//                        // the droppee!
//                        select = droppee;
//                    }
//                } catch (Exception e) {
//                    ErrorManager.getDefault().notify(e);
//                }
//            }
//        } finally {
////            document.writeUnlock();
//            webform.getModel().writeUnlock(undoEvent);
//        }
//    }

//    /**
//     * Multi-function method dealing with link handling for components.
//     * I used to have separate methods which accomplished various aspects of link
//     * handling, but these would vary slightly in how they handled certain aspects
//     * and as a result inconsistent handling would result. Thus, all the logic is handled
//     * by the same method -- both "previewing" what links are available as well as actually
//     * handling the linking. The flags control the behaviors.
//     *
//     * @param origElement The first/innermost element to start with when searching
//     *    the bean hierarchy for DesignBean and MarkupMouseRegions willing to link
//     *    the given beans or bean classes.
//     * @param classes Array of classes to be checked for link eligibility. This is
//     *    separate from beans because we often want to check if linking is possible
//     *    before we actually have created beans -- such as when we're about to
//     *    drag &amp; drop. Obviously in this case we can't perform linking. This
//     *     parameter can be null but then beans must not be null.
//     * @param beans Can be null, but if not, should correspond exactly to the classes
//     *    parameter -- same length, same order, etc. This list must be specified
//     *    if handleLinks is true; you can't link on just class names.
//     * @param selectFirst If set, don't ask the user which target to use if there are multiple
//     *    possibilities; just pick the first one. If not set, all eligible link handlers
//     *    in the parent chain up from the original element will be checked, and if more than
//     *    one is willing to link, the user will be presented with a list and asked to choose.
//     * @param handleLinks If true, actually perform the linking.
//     * @param showLinkTarget If true, highlihght the link target and region. Also sets the
//     *    recentDropTarget field.
//     * @return DROP_DENIED if no beans/classes were linkable for any mouse regions or
//     *    DesignBeans; otherwise returns DROP_LINKED. If showLinkTarget is set, recentDropTarget
//     *    will be set to the most recent such eligible DesignBean.
//     */
////    public int processLinks(Element origElement, Class[] classes, List beans,
//    public int processLinks(Element origElement, Class[] classes, Element componentRootElement,
//        boolean selectFirst, // if there are multiple hits; if not ask user
//        boolean handleLinks, // actually do linking
//        boolean showLinkTarget) {
//        
////        ErrorManager.getDefault()
////    .getInstance(DesignerUtils.class.getName()).isLoggable(ErrorManager.INFORMATIONAL);
////        if(DesignerUtils.DEBUG) {
////            DesignerUtils.debugLog(getClass().getName() + ".processLinks(Element, Class[], ArrayList, boolean, boolean, boolean)");
////        }
////        if((classes == null && beans == null) ||
////                (classes != null && beans != null && beans.size() != classes.length)) {
////            throw(new IllegalArgumentException("One of the classes array or beans list must not be null. If both are not null, than the length of them must be the same."));
////        }
////
////        int dropType = DROP_DENIED;
////        int n;
////
////        if (classes != null) {
////            n = classes.length;
////        } else {
////            //the assert below would hide the NPE - better have NPE if not IllegalArgumentException
////            //assert beans != null;
////            n = beans.size();
////        }
////
////        //the assertion below does not give anything (see the if above). 
////        //assert (beans != null) || (classes != null);
////        //the assertion below should be replaced by an if statement, so that the check happens even if
////        //the asserions are turned off (see the if block above)
////        //assert (beans == null) || (classes == null) || (beans.size() == classes.length);
////
////        for (int i = 0; i < n; i++) {
////            ArrayList candidates = new ArrayList(n);
////            Class clz;
////            DesignBean lb = null;
////
////            if (beans != null) {
////                lb = (DesignBean)beans.get(i);
////            }
////
////            if (classes != null) {
////                clz = classes[i];
////            } else {
////                clz = ((DesignBean)beans.get(i)).getInstance().getClass();
////            }
////
////            try {
////                // See if this new bean should be wired to the bean we
////                // dropped on (or some container up the parent chain that
////                // can handle the bean drop)
////                DesignBean prev = null;
////
////                for (Element element = origElement; element != null; element = FacesSupport.getParent(element)) {
//////                    DesignBean droppee = element.getDesignBean();
////                    DesignBean droppee = InSyncService.getProvider().getMarkupDesignBeanForElement(element);
////
////                    if (droppee == null) {
////                        continue;
////                    }
////
//////                    MarkupMouseRegion region = element.getMarkupMouseRegion();
////                    MarkupMouseRegion region = InSyncService.getProvider().getMarkupMouseRegionForElement(element);
////
////                    if ((region != null) && region.acceptLink(droppee, lb, clz)) {
////                        if (!candidates.contains(element)) {
////                            candidates.add(element);
////                        }
////                    }
////
////                    if (prev == droppee) {
////                        continue;
////                    }
////
////                    prev = droppee;
////
////                    DesignInfo dbi = droppee.getDesignInfo();
////
////                    if ((dbi != null) && dbi.acceptLink(droppee, lb, clz)) {
////                        if (!candidates.contains(droppee) &&
////                                ((beans == null) || !beans.contains(droppee))) {
////                            candidates.add(droppee);
////                        }
////                    }
////                }
////            } catch (Exception e) {
////                ErrorManager.getDefault().notify(e);
////            }
////
////            if (candidates.size() == 0) {
////                continue;
////            }
////
////            dropType = DROP_LINKED;
////
////            // Store either the chosen DesignBean, or the chosen MarkupMouseRegion.
////            // However, we'll need both the region and the corresponding bean, so
////            // store the element instead which will point to both.
////            Object selected = null;
////
////            if (selectFirst || (candidates.size() == 1)) {
////                selected = candidates.get(0);
////            } else {
////                // Gotta ask the user
////                // Code originally emitted by the form builder:
////                JPanel panel = new JPanel(new GridBagLayout());
////                GridBagConstraints gridBagConstraints;
////                String labelDesc = NbBundle.getMessage(DndHandler.class, "ChooseTargetLabel"); // NOI18N
////                JLabel label = new JLabel(labelDesc);
////                gridBagConstraints = new GridBagConstraints();
////                gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
////                gridBagConstraints.insets = new Insets(12, 12, 11, 11);
////                gridBagConstraints.anchor = GridBagConstraints.WEST;
////                gridBagConstraints.weightx = 1.0;
////                panel.add(label, gridBagConstraints);
////
////                ButtonGroup buttonGroup = new ButtonGroup();
////
////                // Iterate reverse order since list was generates from the leaf
////                // up the parent chain, and I want to display outermost parents first
////                for (int j = candidates.size() - 1; j >= 0; j--) {
////                    String name = "";
////                    Object next = candidates.get(j);
////
////                    if (next instanceof DesignBean) {
////                        DesignBean dlb = (DesignBean)next;
////                        name = dlb.getInstanceName();
////
////                        BeanInfo bi = dlb.getBeanInfo();
////
////                        if (bi != null) {
////                            BeanDescriptor bd = bi.getBeanDescriptor();
////
////                            if (bd != null) {
////                                String desc = bd.getShortDescription();
////
////                                if (desc == null) {
////                                    desc = bd.getDisplayName();
////
////                                    if (desc == null) {
////                                        desc = "";
////                                    }
////                                }
////
////                                name =
////                                    NbBundle.getMessage(DndHandler.class, "TargetDescriptor", // NOI18N
////                                        name, desc);
////                            }
////                        }
////                    } else {
//////                        assert next instanceof RaveElement;
////                        if (!(next instanceof Element)) {
////                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
////                                    new IllegalStateException("Object is expected to be of Element type, object=" + next)); // NOI18N
////                        }
////
//////                        RaveElement element = (RaveElement)next;
//////                        MarkupMouseRegion region = element.getMarkupMouseRegion();
////                        Element element = (Element)next;
////                        MarkupMouseRegion region = InSyncService.getProvider().getMarkupMouseRegionForElement(element);
////                        assert region != null;
////
////                        if ((region.getDescription() != null) &&
////                                (region.getDescription().length() > 0)) {
////                            name =
////                                NbBundle.getMessage(DndHandler.class, "TargetDescriptor", // NOI18N
////                                    region.getDisplayName(), region.getDescription());
////                        } else {
////                            name = region.getDisplayName();
////                        }
////                    }
////
////                    JRadioButton radioButton = new JRadioButton(name);
////
////                    if (j == (candidates.size() - 1)) {
////                        radioButton.setSelected(true);
////                    }
////
////                    radioButton.putClientProperty("liveBean", next); // NOI18N
////                    buttonGroup.add(radioButton);
////                    gridBagConstraints = new GridBagConstraints();
////                    gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
////                    gridBagConstraints.insets = new Insets(0, 12, 0, 11);
////                    gridBagConstraints.anchor = GridBagConstraints.WEST;
////                    panel.add(radioButton, gridBagConstraints);
////                }
////
////                JPanel filler = new JPanel();
////                gridBagConstraints = new GridBagConstraints();
////                gridBagConstraints.weighty = 1.0;
////                panel.add(filler, gridBagConstraints);
////
////                String title = NbBundle.getMessage(DndHandler.class, "ChooseTarget"); // NOI18N
////                DialogDescriptor dlg =
////                    new DialogDescriptor(panel, title, true, DialogDescriptor.OK_CANCEL_OPTION,
////                        DialogDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, 
////                    // DialogDescriptor.BOTTOM_ALIGN,
////                    null, //new HelpCtx("choose_target"), // NOI18N
////                        null);
////
////                Dialog dialog = DialogDisplayer.getDefault().createDialog(dlg);
////                dialog.show();
////
////                if (dlg.getValue().equals(DialogDescriptor.OK_OPTION)) {
////                    Enumeration enm = buttonGroup.getElements();
////
////                    while (enm.hasMoreElements()) {
////                        JRadioButton button = (JRadioButton)enm.nextElement();
////
////                        if (button.isSelected()) {
////                            selected = button.getClientProperty("liveBean"); // NOI18N
////
////                            break;
////                        }
////                    }
////                } // else: Cancel, or Esc: do nothing; selected will stay null
////            }
////
////            if (showLinkTarget) {
////                if (selected instanceof DesignBean) {
////                    DesignBean droppee = (DesignBean)selected;
////
////                    if (droppee instanceof MarkupDesignBean) {
////                        recentDropTarget = (MarkupDesignBean)droppee;
////                        showDropMatch(recentDropTarget, null, DROP_LINKED);
////                    }
////                } else {
//////                    assert selected instanceof RaveElement;
////                    if (!(selected instanceof Element)) {
////                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
////                                new IllegalStateException("Object is expected to be of Element type, object=" + selected)); // NOI18N
////                    }
////                    
////
//////                    RaveElement element = (RaveElement)selected;
//////                    DesignBean droppee = element.getDesignBean();
////                    Element element = (Element)selected;
////                    DesignBean droppee = InSyncService.getProvider().getMarkupDesignBeanForElement(element);
////
////                    if (droppee instanceof MarkupDesignBean) {
////                        recentDropTarget = (MarkupDesignBean)droppee;
//////                        showDropMatch(recentDropTarget, element.getMarkupMouseRegion(), DROP_LINKED);
////                        showDropMatch(recentDropTarget,
////                                InSyncService.getProvider().getMarkupMouseRegionForElement(element),
////                                DROP_LINKED);
////                    }
////                }
////            }
////
////            if ((selected == null) || !handleLinks || (beans == null)) {
////                return dropType;
////            }
////
//////            Document document = webform.getDocument();
////
////            String description = NbBundle.getMessage(DndHandler.class, "LinkComponent"); // NOI18N
////            UndoEvent undoEvent = webform.getModel().writeLock(description);
////            try {
//////                String description = NbBundle.getMessage(DndHandler.class, "LinkComponent"); // NOI18N
//////                document.writeLock(description);
////
////                lb = (DesignBean)beans.get(i);
////
////                try {
////                    // If you drop on an existing component, see if they
////                    // can be wired together
////                    // Try to bind the two together - for example, if you
////                    // drop a RowSet on a bean that has a RowSet property,
////                    // the RowSet property is bound to this particular
////                    // RowSet.
////                    if (selected instanceof DesignBean) {
////                        DesignBean droppee = (DesignBean)selected;
////                        assert droppee.getDesignInfo().acceptLink(droppee, lb,
////                            lb.getInstance().getClass());
////
////                        MarkupDesignBean mbean = null;
////
////                        if (droppee instanceof MarkupDesignBean) {
////                            // link beans might perform lots and lots of
////                            // updates on the element - that's the case
////                            // for the data grid when you bind a table
////                            // to it for example.  So batch up all these
////                            // modifications into a single change event
////                            // on the top level element.
////                            mbean = (MarkupDesignBean)droppee;
//////                            webform.getDomSynchronizer().setUpdatesSuspended(mbean, true);
////                            webform.setUpdatesSuspended(mbean, true);
////                        }
////
////                        try {
////                            webform.getModel().linkBeans(droppee, lb);
////                        } finally {
////                            if (mbean != null) {
////                                // Process queued up changes
//////                                webform.getDomSynchronizer().setUpdatesSuspended(mbean, false);
////                                webform.setUpdatesSuspended(mbean, false);
////                            }
////                        }
////
////                        // The target bean should be selected instead of
////                        // the droppee!
////                        select = droppee;
////                    } else {
//////                        assert selected instanceof RaveElement;
////                        if (!(selected instanceof Element)) {
////                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
////                                    new IllegalStateException("Object is expected to be of Element type, object=" + selected)); // NOI18N
////                        }
////
//////                        RaveElement element = (RaveElement)selected;
//////                        MarkupMouseRegion region = element.getMarkupMouseRegion();
////                        Element element = (Element)selected;
////                        MarkupMouseRegion region = InSyncService.getProvider().getMarkupMouseRegionForElement(element);
////                        assert region != null;
////
//////                        DesignBean droppee = element.getDesignBean();
////                        DesignBean droppee = InSyncService.getProvider().getMarkupDesignBeanForElement(element);
////                        assert droppee != null;
////                        assert region.acceptLink(droppee, lb, clz);
////
////                        MarkupDesignBean mbean = null;
////
////                        if (droppee instanceof MarkupDesignBean) {
////                            mbean = (MarkupDesignBean)droppee;
//////                            webform.getDomSynchronizer().setUpdatesSuspended(mbean, true);
////                            webform.setUpdatesSuspended(mbean, true);
////                        }
////
////                        try {
////                            Result r = region.linkBeans(droppee, lb);
////                            ResultHandler.handleResult(r, webform.getModel());
////                        } finally {
////                            if (mbean != null) {
//////                                webform.getDomSynchronizer().setUpdatesSuspended(mbean, false);
////                                webform.setUpdatesSuspended(mbean, false);
////                            }
////                        }
////                    }
////                } catch (Exception e) {
////                    ErrorManager.getDefault().notify(e);
////                }
////            } finally {
//////                document.writeUnlock();
////                webform.getModel().writeUnlock(undoEvent);
////            }
////        }
////
////        return dropType;
//        
////        return webform.processLinks(origElement, classes, beans, selectFirst, handleLinks, showLinkTarget);
//        return webform.processLinks(origElement, classes, componentRootElement, selectFirst, handleLinks, showLinkTarget);
//    }

//    /** Set the absolute position of the component. **/
//    private void positionBean(MarkupDesignBean lb, DesignBean origParent, Element element,
//    Location location, CoordinateTranslator coordinateTranslator) {
//        // TODO - transfer this logic to computePositions
//        if ((location.coordinates == null) || (element == null)) {
//            return;
//        }
//
//        DesignBean parent = origParent;
//
//        // Only position beans dropped on a grid area or a form
//        boolean grid = false;
//
//        // XXX TODO: transfer this logic into Utilities instead and make
//        // sure we do it the same way everywhere!
//        if (element.getParentNode() instanceof Element) {
//            Element pe = (Element)element.getParentNode();
//
//            if (pe.getTagName().equals(HtmlTag.FSUBVIEW.name) &&
//                    pe.getParentNode() instanceof Element) {
//                pe = (Element)pe.getParentNode();
//            }
//
//            // The component may -render- a -rave-layout setting,
//            // so look in the rendered HTML for the layout setting
//            // rather than in the JSP DOM
////            RaveElement rendered = ((RaveElement)pe).getRendered();
//            Element rendered = MarkupService.getRenderedElementForElement(pe);
//            if (rendered != null) {
//                pe = rendered;
//            }
//
////            Value val = CssLookup.getValue(pe, XhtmlCss.RAVELAYOUT_INDEX);
//            CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(pe, XhtmlCss.RAVELAYOUT_INDEX);
//
////            if (val == CssValueConstants.GRID_VALUE) {
//            if (CssProvider.getValueService().isGridValue(cssValue)) {
//                grid = true;
//            } else if (pe.getTagName().endsWith(HtmlTag.FORM.name)) { // h:form too
//                pe = (Element)pe.getParentNode();
////                val = CssLookup.getValue(pe, XhtmlCss.RAVELAYOUT_INDEX);
//                CssValue cssValue2 = CssProvider.getEngineService().getComputedValueForElement(pe, XhtmlCss.RAVELAYOUT_INDEX);
//
////                if (val == CssValueConstants.GRID_VALUE) {
//                if (CssProvider.getValueService().isGridValue(cssValue2)) {
//                    grid = true;
//                }
//            }
//        }
//
//        if (((parent == null) || grid || FacesSupport.isFormBean(webform, parent))) {
////            GridHandler gm = GridHandler.getInstance();
//            setInitialPosition(webform, lb, element, location.coordinates, location.size, coordinateTranslator);
//            select = lb;
//        }
//    }
    
//    /**
//     * Set the initial position for a given component.
//     * It is assumed that the bean does not already have an associated position.
//     *
//     * @param editor The designer pane containing the element
//     * @param element The element we want to set a style attribute for
//     * @param pos The point where we want the element positioned. If null,
//     *              this method has no effect.
//     * @param size The size to assign to the component. If null, don't set a
//     *              size, use the intrinsic size.
//     */
//    private static void setInitialPosition(WebForm webform, MarkupDesignBean bean, Element element, Point pos, Dimension size,
//    CoordinateTranslator coordinateTranslator) {
//        if (pos == null) {
//            return;
//        }
//
//        DesignProperty styleProp = bean.getProperty("style"); // NOI18N
//
//        if (styleProp == null) {
//            // No style property - can't set position!!
//            return;
//        }
//
//        String style = (String)styleProp.getValue();
//        StringBuffer sb = new StringBuffer();
//
//        if ((style != null) && (style.length() > 0)) {
//            sb.append(style);
//            sb.append("; ");
//        }
//
//        // Locate a grid layout parent
////        Document doc = editor.getDocument();
////        WebForm webform = doc.getWebForm();
////        XhtmlCssEngine engine = webform.getMarkup().getCssEngine();
//
//        // This model should already be locked when we attempt to do this
//        assert webform.getModel().isWriteLocked();
//
//        int x = pos.x;
//        int y = pos.y;
//
////        GridHandler gridHandler = GridHandler.getInstance();
//        // See if we should translate the coordinates
//        if (element.getParentNode() instanceof Element) {
//            Element parent = (Element)element.getParentNode();
////            CssBox parentBox = CssBox.getBox(parent);
////
////            if (parentBox != null) {
////                // Translate coordinates from absolute/viewport
////                // to absolute coordinates relative to the target
////                // grid container
//////                Point p = translateCoordinates(parentBox, x, y);
////                Point p = gridHandler.translateCoordinates(parentBox, x, y);
////                x = p.x;
////                y = p.y;
////            }
////            Point point = gridHandler.translateCoordinates(parent, x, y);
//            Point point = coordinateTranslator.translateCoordinates(parent, x, y);
//            if (point != null) {
//                x = point.x;
//                y = point.y;
//            }
//        }
//
////        x = snapX(x);
////        y = snapY(y);
////        x = gridHandler.snapX(x);
////        y = gridHandler.snapY(y);
//        x = coordinateTranslator.snapX(x);
//        y = coordinateTranslator.snapY(y);
//
//        // prevent multiple updates for the same element - only need a single refresh
//        try {
////            webform.getDomSynchronizer().setUpdatesSuspended(bean, true);
//            webform.setUpdatesSuspended(bean, true);
//
//            // TODO: Find the -rendered- element; I have to look up margins on it
//            // since it could come from style classes. For example, for a Braveheart
//            // button, if I have a CSS rule   .Btn2 { margin: 200px }  I won't find
//            // this style looking at the JSP element (ui:button) I need to do lookup
//            // on the rendered <input class="Btn2" ...> element.
//            // The "top" and "left" properties are relative to the margin edge of the
//            // component yet the position is specified relative to the border (visible) area
////            int leftMargin = CssLookup.getLength(element, XhtmlCss.MARGIN_LEFT_INDEX);
////            int topMargin = CssLookup.getLength(element, XhtmlCss.MARGIN_TOP_INDEX);
//            int leftMargin = CssBox.getCssLength(element, XhtmlCss.MARGIN_LEFT_INDEX);
//            int topMargin = CssBox.getCssLength(element, XhtmlCss.MARGIN_TOP_INDEX);
//            x -= leftMargin;
//            y -= topMargin;
//
//            List set = new ArrayList(5);
//            List remove = new ArrayList(3);
//
//            sb.append("position: absolute; ");
//            sb.append("left: ");
//            sb.append(Integer.toString(x));
//            sb.append("px; ");
//            sb.append("top: ");
//            sb.append(Integer.toString(y));
//            sb.append("px");
//
//            if (size != null) {
//                if (!setDesignProperty(bean, HtmlAttribute.WIDTH, size.width)) {
//                    sb.append("; width: ");
//                    sb.append(Integer.toString(size.width));
//                    sb.append("px"); // NOI18N
//                } else {
//                    // Do I need to try to delete the width from the existing value string?
//                    // The only way this could get here is if the component has had a chance
//                    // to set widths/sizes with the create customizers
//                }
//
//                if (!setDesignProperty(bean, HtmlAttribute.HEIGHT, size.height)) {
//                    sb.append("; height: ");
//                    sb.append(Integer.toString(size.height));
//                    sb.append("px"); // NOI18N
//                } else {
//                    // Do I need to try to delete the width from the existing value string?
//                    // The only way this could get here is if the component has had a chance
//                    // to set widths/sizes with the create customizers
//                }
//            }
//
//            styleProp.setValue(sb.toString());
//        } finally {
////            webform.getDomSynchronizer().setUpdatesSuspended(bean, false);
//            webform.setUpdatesSuspended(bean, false);
//        }
//    }
    
//    /** Attempt to set the given attribute on the bean to the given length
//     * and return true iff it succeeds.
//     */
//    public /*private*/ static boolean setDesignProperty(DesignBean bean, String attribute, int length, WebForm webform) {
////        DesignProperty prop = bean.getProperty(attribute);
////
////        if (prop != null) {
////            PropertyDescriptor desc = prop.getPropertyDescriptor();
////            Class clz = desc.getPropertyType();
////
////            // I can do == instead of isAssignableFrom because
////            // both String and Integer are final!
////            if (clz == String.class) {
////                prop.setValue(Integer.toString(length));
////
////                return true;
////            } else if (clz == Integer.TYPE) {
////                prop.setValue(new Integer(length));
////
////                return true;
////            }
////        }
////
////        return false;
//        return Util.setDesignProperty(bean, attribute, length);
//    }

//    void customizeCreation(List beans) {
////        int n = beans.size();
////
////        for (int i = 0; i < n; i++) {
////            DesignBean lb = (DesignBean)beans.get(i);
////            DesignInfo lbi = lb.getDesignInfo();
////
////            if (lbi != null) {
////                Customizer2 customizer = null; //lbi.getCreateCustomizer(lb);
////
////                if (customizer != null) {
////                    CustomizerDisplayer lcd =
////                        new CustomizerDisplayer(lb, customizer, customizer.getHelpKey(),
////                            webform.getModel());
////                    lcd.show();
////                }
////            }
////        }
//    }


    /**
     * Return the target parent or link-handler computed for the most recent
     * getDropType call. Be careful. This must be called after getDropType()
     * and the result will be clobbered as soon as additional getDropType
     * requests come in (which they often do from the mouse motion listener!)
     */
//    MarkupDesignBean getRecentDropTarget() {
//        return recentDropTarget;
    Element getRecentDropTargetComponentRootElement() {
//        return recentDropTargetComponentRootElement;
        return recentDropTargetComponentRootElementWRef.get();
    }

    /** Compute the list of class names for beans identified by the given palette item transferable */
    public int getDropType(Point p, Transferable t, boolean linkOnly) {
        if (t == null) {
            return DROP_DENIED;
        }
        
        CssBox box = ModelViewMapper.findBox(webform.getPane().getPageBox(), p.x, p.y);
//        DesignBean origDroppee = getDroppee(box);
        Element origDropeeComponentRootElement = getDropeeComponent(box);
        Element droppeeElement = box == null ? null : box.getElement();
        
//        DataFlavor importFlavor = getImportFlavor(t.getTransferDataFlavors());
//
//        if (importFlavor == null) {
//            DataFlavor[] flavors = t.getTransferDataFlavors();
//            ErrorManager.getDefault().log("Unusable transfer, data flavors="
//                    + (flavors == null ? null : java.util.Arrays.asList(t.getTransferDataFlavors()))); // NOI18N
//
//            return DROP_DENIED;
//        }
//
//        Class rc = importFlavor.getRepresentationClass();
//
//        if (rc == DisplayItem.class) {
//            // Create a new type
//            try {
//                Object transferData = t.getTransferData(importFlavor);
//
//                if (!(transferData instanceof DisplayItem)) {
//                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, 
//                            new IllegalStateException("Invalid transferData=" + transferData // NOI18N
//                            + ", from transferable=" + t)); // NOI18N
//                    return DROP_DENIED;
//                }
//
//                DisplayItem item = (DisplayItem)transferData;
//
//                return getDropTypeForDisplayItem(origDroppee, droppeeElement, item, linkOnly);
//            } catch (UnsupportedFlavorException ex) {
//                ErrorManager.getDefault().notify(ex);
//
//                return DROP_DENIED;
//            } catch (java.io.IOException ex) {
//                ErrorManager.getDefault().notify(ex);
//
//                return DROP_DENIED;
//            }
//        } else if (rc == DesignBean.class) {
//            try {
//                Object transferData = t.getTransferData(importFlavor);
//
//                if (!(transferData instanceof DesignBean[])) {
//                    ErrorManager.getDefault().log("Invalid DesignBean[] transfer data: " +
//                        transferData);
//
//                    return DROP_DENIED;
//                }
//
//                DesignBean[] beans = (DesignBean[])transferData;
//
//                String[] classNames = new String[beans.length];
//
//                for (int i = 0, n = beans.length; i < n; i++) {
//                    classNames[i] = beans[i].getInstance().getClass().getName();
//                }
//
//                return getDropTypeForClassNames(origDroppee, droppeeElement, classNames, null, linkOnly);
//            } catch (UnsupportedFlavorException ex) {
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//                return DROP_DENIED;
//            } catch (java.io.IOException ex) {
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//                return DROP_DENIED;
//            }
//        }
//        
//        // XXX TEMP First give the chance to the provider.
//        // FIXME This shouldn't be here at all, the original transferable
//        // should contain all the needed flavors.
//        DesignTimeTransferDataCreator dataCreator = (DesignTimeTransferDataCreator)Lookup.getDefault().lookup(DesignTimeTransferDataCreator.class);
//        if (dataCreator != null) {
//            DisplayItem displayItem = dataCreator.getDisplayItem(t);
//            if (displayItem != null) {
//                return getDropTypeForDisplayItem(origDroppee, droppeeElement, displayItem, linkOnly);
//            }
//        }
//        
//        // XXX The other hacked transferables.
//        if (rc == String.class/*Linux*/ || rc == List.class/*Windows/Solaris*/) {
//            // XXX #6468896 To be able to drop files (images) from the outside world (desktop).
//           return DROP_PARENTED;
//        } else if (rc == org.openide.nodes.Node.class) {
//            // XXX #6482097 Reflecting the impl in FacesDnDSupport.
//            // FIXME Later the impl has to be improved and moved over there.
//            Object transferData;
//            try {
//                transferData = t.getTransferData(importFlavor);
//                if (transferData instanceof org.openide.nodes.Node) {
//                    org.openide.nodes.Node node = (org.openide.nodes.Node)transferData;
//                    DataObject dobj = (DataObject)node.getCookie(DataObject.class);
//
//                    if (dobj != null) {
//                        FileObject fo = dobj.getPrimaryFile();
//                        if (isImage(fo.getExt())) {
////                            String className;
////                            // XXX This should be decided by the parent bean.
////                            // I.e. appropriate api is missing.
////                            // XXX This shouldn't be here resolved, but in the parent bean.
////                            if (webform.isBraveheartPage()) {
////                                className = com.sun.rave.web.ui.component.ImageComponent.class.getName(); // NOI18N
////                            } else if (webform.isWoodstockPage()) {
////                                // Use woodstock ImageComponent component
////                                className = com.sun.webui.jsf.component.ImageComponent.class.getName(); // NOI18N
////                            } else {
////                                className = javax.faces.component.html.HtmlGraphicImage.class.getName(); // NOI18N
////                            }
//                            String className = webform.getImageComponentClassName();
//                            
//                            String[] classNames = new String[] {className};
//
//                            return getDropTypeForClassNames(origDroppee, droppeeElement, classNames, null, linkOnly);
//                        } else if (isStylesheet(fo.getExt())) {
//                            return DROP_PARENTED;
//                        }
//                    }
//                }
//            } catch (IOException ex) {
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//            } catch (UnsupportedFlavorException ex) {
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//            }
//            return DROP_DENIED;
//        }
//
//        return DROP_DENIED;
        return webform.getDropType(/*origDroppee,*/origDropeeComponentRootElement, droppeeElement, t, linkOnly);
    }

//    // XXX Also in insync/FacesDnDSupport
//    /** Return true if the extension indicates that this is an image */
//    private static boolean isImage(String extension) {
//        return (extension.equalsIgnoreCase("jpg") || // NOI18N
//                extension.equalsIgnoreCase("gif") || // NOI18N
//                extension.equalsIgnoreCase("png") || // NOI18N
//                extension.equalsIgnoreCase("jpeg")); // NOI18N
//    }
    
//    // XXX Also in insync/FacesDnDSupport.
//    private static boolean isStylesheet(String extension) {
//        return extension.equalsIgnoreCase("css"); // NOI18N
//    }


//    /**
//     * Decide whether or not we can drop the given palette item
//     * at the given position.
//     * XXX TODO get rid of this method from the designer, it is JSF specific..
//     */
////    private int getDropTypeForDisplayItem(Point p, DisplayItem item, boolean linkOnly) {
//    private int getDropTypeForDisplayItem(DesignBean origDroppee, Element droppeeElement, DisplayItem item, boolean linkOnly) {
//        if(item == null) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, 
//                    new NullPointerException("Item is null")); // NOI18N
//            return DROP_DENIED;
//        } 
//        
////        String[] classNames = getClasses(new DisplayItem[] { item });
//        String[] classNames = webform.getClassNames(new DisplayItem[] {item});
//
//        return getDropTypeForClassNames(origDroppee, droppeeElement, classNames, null, linkOnly);
//    }

    
    /**
     * We have a potential drop match on the given bean - show it as selected.
     */
//    public void showDropMatch(MarkupDesignBean bean, MarkupMouseRegion region, int type) {
    public void showDropMatch(Element componentRootElement, Element regionElement, int type) {
//        recentDropTarget = bean;
//        recentDropTargetComponentRootElement = componentRootElement;
        recentDropTargetComponentRootElementWRef = new WeakReference<Element>(componentRootElement);
        
//        if (DONT_SHOW_MATCHES || ((currentMatched == bean) && (currentRegion == region))) {
        if (DONT_SHOW_MATCHES || ((currentMatchedComponentRootElement == componentRootElement) && (currentRegionElement == regionElement))) {
            return;
        }

//        currentMatched = bean;
        currentMatchedComponentRootElement = componentRootElement;
//        currentRegion = region;
        currentRegionElement = regionElement;

//        if (region != null) {
        if (regionElement != null) {
            // Regions only support linking, not parenting
            assert type == DROP_LINKED;
//            webform.getManager().highlight(bean, region);
            webform.getManager().highlight(componentRootElement, regionElement);
//            lastMessage =
//                NbBundle.getMessage(DndHandler.class, "LinkTarget", region.getDisplayName());
                        lastMessage =
                NbBundle.getMessage(DndHandler.class, "LinkTarget", webform.getDomProviderService().getRegionDisplayName(regionElement));

            StatusDisplayer.getDefault().setStatusText(lastMessage);
//        } else if (bean != null) {
        } else if (componentRootElement != null) {
//            FacesPageUnit facesUnit = webform.getModel().getFacesUnit();
//
//            if ((facesUnit == null) ||
////                    ((facesUnit.getDefaultParent() != FacesSupport.getMarkupBean(bean)) &&
//                    ((facesUnit.getDefaultParent() != Util.getMarkupBean(bean)) &&
//                    
////                    (bean.getElement() != webform.getBody().getSourceElement()))) {
//                    (bean.getElement() != MarkupService.getSourceElementForElement(webform.getHtmlBody())))) {
//            if (webform.canHighlightMarkupDesignBean(bean)) {
//                webform.getManager().highlight(bean, null);
            if (webform.canHighlightComponentRootElement(componentRootElement)) {
                webform.getManager().highlight(componentRootElement, null);
            } else {
                webform.getManager().highlight(null, null);
            }

            if (type == DROP_LINKED) {
                lastMessage =
//                    NbBundle.getMessage(DndHandler.class, "LinkTarget", bean.getInstanceName());
                        NbBundle.getMessage(DndHandler.class, "LinkTarget", webform.getDomProviderService().getInstanceName(componentRootElement));
                StatusDisplayer.getDefault().setStatusText(lastMessage);
            } else if (type == DROP_PARENTED) {
                lastMessage =
//                    NbBundle.getMessage(DndHandler.class, "ParentTarget", bean.getInstanceName());
                        NbBundle.getMessage(DndHandler.class, "ParentTarget", webform.getDomProviderService().getInstanceName(componentRootElement));
                StatusDisplayer.getDefault().setStatusText(lastMessage);
            } else {
                assert false : type;
            }
        } else {
            clearDropMatch();
        }
    }

    public void clearDropMatch() {
//        currentMatched = null;
        currentMatchedComponentRootElement = null;
        webform.getManager().highlight(null, null);

        if (StatusDisplayer.getDefault().getStatusText() == lastMessage) {
            StatusDisplayer.getDefault().setStatusText("");
        }

        lastMessage = null;
    }

//    /** Get rid of this method from designer. */
//    public int getDropTypeForClassNamesEx(Point p, String[] classNames, DesignBean[] beans, boolean linkOnly) {
    public int getDropTypeForComponent(Point p, Element componentRootElement, boolean linkOnly) {
        if(p == null) {
            throw(new IllegalArgumentException("Null drop point."));
        }
        // No... call computePositions and use location.coordinates instead... see @todo above
//        CssBox box = webform.getMapper().findBox(p.x, p.y);
        CssBox box = ModelViewMapper.findBox(webform.getPane().getPageBox(), p.x, p.y);
//        DesignBean origDroppee = getDroppee(box);
        Element origDropeeComponentRootElement = getDropeeComponent(box);
        Element droppeeElement = box == null ? null : box.getElement();
//        return webform.getDropTypeForClassNames(origDroppee, droppeeElement, classNames, beans, linkOnly);
        return webform.getDropTypeForComponent(/*origDroppee,*/origDropeeComponentRootElement, droppeeElement, componentRootElement, linkOnly);
    }
    
//    /**
//     * Decide whether or not we can drop the given palette item at the given position.
//    * @todo implement using computeActions and computePosition instead of custom solution here... e.g.
//     <pre>
//    public int getDropType(Point p, String[] classNames, boolean linkOnly) {
//        int allowed = computeActions(dropNode, t, false, nodePos);
//        if (allowed == DnDConstants.ACTION_NONE) {
//            return;
//        }
//        if (dropAction == DnDConstants.ACTION_COPY) {
//        ... XXX call computeActions
//    }
//     </pre>
//     * XXX TODO get rid of this method from the designer, it is JSF specific.
//    */
//    private int getDropTypeForClassNames(DesignBean origDroppee, Element droppeeElement, String[] classNames, DesignBean[] beans, boolean linkOnly) {
//        if(DesignerUtils.DEBUG) {
//            DesignerUtils.debugLog(getClass().getName() + ".getDropType(Point, PaletteItem, boolean)");
//        }
////        if(p == null) {
////            throw(new IllegalArgumentException("Null drop point."));
////        }
//        if(classNames == null) {
//            throw(new IllegalArgumentException("Null class names array."));
//        }
//
//        recentDropTarget = null;
//
////        // No... call computePositions and use location.coordinates instead... see @todo above
//////        CssBox box = webform.getMapper().findBox(p.x, p.y);
////        CssBox box = ModelViewMapper.findBox(webform.getPane().getPageBox(), p.x, p.y);
////        DesignBean origDroppee = getDroppee(box);
//
//        if (origDroppee == null) {
//            if (linkOnly) {
//                return DROP_DENIED;
//            }
//
////            if (origDroppee instanceof MarkupDesignBean) {
////                recentDropTarget = (MarkupDesignBean)origDroppee;
////            }
//
////            LiveUnit unit = webform.getModel().getLiveUnit();
//
////            if (unit != null) {
//                for (int i = 0; i < classNames.length; i++) {
//                    // Do anything smart about facets here? E.g. what if you
//                    // point over a facet table header? A drop in the app outline
//                    // would offer to replace it. Should the interactive link feedback
//                    // allow this too?
////                    if (unit.canCreateBean(classNames[i], null, null)) {
//                    if (webform.canCreateBean(classNames[i], null, null)) {
//                        showDropMatch(null, null, DROP_PARENTED);
//
//                        return DROP_PARENTED;
//                    }
//                }
////            }
//
//            clearDropMatch();
//
//            return DROP_DENIED;
//        }
//
//        // None of the droppee ancestors accepted the drop items
//        // as a potential child - but perhaps they will accept
//        // a link?
////        Class[] classes = new Class[classNames.length];
////        ArrayList beanList = null;
////
////        if (beans != null) {
////            beanList = new ArrayList(beans.length);
////        }
////
////        for (int i = 0; i < classNames.length; i++) {
////            try {
////                Class clz = webform.getModel().getFacesUnit().getBeanClass(classNames[i]);
////
////                if (clz != null) {
////                    classes[i] = clz;
////                }
////
////                if (beans != null) {
////                    beanList.add(beans[i]);
////                }
////            } catch (Exception e) {
////                ErrorManager.getDefault().notify(e);
////            }
////        }
////        
////        if (beans == null) {
////            beanList = null;
////        }
//        List<Class> classList = new ArrayList<Class>();
//        List<DesignBean> beanList = beans == null ? null : new ArrayList<DesignBean>();
//        for (int i = 0; i < classNames.length; i++) {
//            try {
////                Class clazz = webform.getModel().getFacesUnit().getBeanClass(classNames[i]);
//                Class clazz = webform.getBeanClass(classNames[i]);
//                if (clazz != null) {
//                    classList.add(clazz);
//                }
//                if (beans != null) {
//                    beanList.add(beans[i]);
//                }
//            } catch (ClassNotFoundException ex) {
//                // XXX #6492649 It means the class can't be found so no drop should happen.
//                // FIXME The API should be improved and not controlled via exceptions.
//                continue;
//            } catch (Exception ex) {
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//                continue;
//            }
//        }
//        Class[] classes = classList.toArray(new Class[classList.size()]);
//
////        RaveElement droppeeElement = (RaveElement)box.getElement();
////        Element droppeeElement = box.getElement();
//        
//        int dropType = processLinks(droppeeElement, classes, beanList, true, false, true);
//
//        if (dropType != DROP_DENIED) {
//            return dropType;
//        }
//
//        if (linkOnly) {
//            clearDropMatch();
//
//            return DROP_DENIED;
//        }
//
//        // See if any of the droppee parents accept the new item as a
//        // child
//        for (int i = 0; i < classNames.length; i++) {
//            Node parentNode = null; // XXX todo figure out better parent node
////            DesignBean parent = findParent(classNames[i], origDroppee, parentNode, true);
//            DesignBean parent = webform.findParent(classNames[i], origDroppee, parentNode, true);
//
//            if (parent != null) {
//                if (parent instanceof MarkupDesignBean) {
//                    recentDropTarget = (MarkupDesignBean)parent;
//                    showDropMatch(recentDropTarget, null, DROP_PARENTED);
//                } else {
//                    clearDropMatch();
//                }
//
//                return DROP_PARENTED;
//            }
//        }
//
//        showDropMatch(null, null, DROP_DENIED);
//
//        return DROP_DENIED;
//    }

//    /**
//     * Try to find a flavor that can be used to import a Transferable.
//     * The set of usable flavors are tried in the following order:
//     * <ol>
//     *     <li>First, an attempt is made to find a flavor matching the content type
//     *         of the EditorKit for the component.
//     *     <li>Second, an attempt to find a text/plain flavor is made.
//     *     <li>Third, an attempt to find a flavor representing a String reference
//     *         in the same VM is made.
//     *     <li>Lastly, DataFlavor.stringFlavor is searched for.
//     * </ol>
//     */
//    public DataFlavor getImportFlavor(DataFlavor[] flavors) {
////        DataFlavor plainFlavor = null;
////        DataFlavor refFlavor = null;
////        DataFlavor stringFlavor = null;
////        DataFlavor listFlavor = null;
////
////        for (int i = 0; i < flavors.length; i++) {
////            String mime = flavors[i].getMimeType();
////            Class clz = flavors[i].getRepresentationClass();
////
////            if (clz.isAssignableFrom(DisplayItem.class)) {
////                return flavors[i];
////            }
////
////            if (clz == DesignBean.class) {
////                return flavors[i];
////            }
////
////            if (clz.isAssignableFrom(List.class)) {
////                // We don't know what's in the list, and can't look until
////                // we have a transferable... but it looks promising so
////                // defer decision
////                listFlavor = flavors[i];
////            }
////
////            if (clz.isAssignableFrom(org.openide.nodes.Node.class)) {
////                listFlavor = flavors[i];
////            } // TODO: check for org.openide.util.datatransfer.MultiTransferObject
////
////            if ((plainFlavor == null) && mime.startsWith("text/plain")) {
////                plainFlavor = flavors[i];
////            } else if ((refFlavor == null) &&
////                    mime.startsWith("application/x-java-jvm-local-objectref") &&
////                    (flavors[i].getRepresentationClass() == java.lang.String.class)) {
////                refFlavor = flavors[i];
////            } else if ((stringFlavor == null) && flavors[i].equals(DataFlavor.stringFlavor)) {
////                stringFlavor = flavors[i];
////            }
////        }
////
////        if (refFlavor != null) {
////            return refFlavor;
////        } else if (listFlavor != null) {
////            return listFlavor;
////        } else if (stringFlavor != null) {
////            return stringFlavor;
////        } else if (plainFlavor != null) {
////            return plainFlavor;
////        }
////
////        return null;
//        return webform.getImportFlavor(flavors);
//    }

//    /**
//     * This method indicates if a component would accept an import of the given
//     * set of data flavors prior to actually attempting to import it.
//     *
//     * @param comp  The component to receive the transfer.  This
//     *  argument is provided to enable sharing of TransferHandlers by
//     *  multiple components.
//     * @param flavors  The data formats available
//     * @return  true if the data can be inserted into the component, false otherwise.
//     */
//    public boolean canImport(JComponent comp, DataFlavor[] flavors) {
//        // TODO Moving to NB winsys
//        // Ensure that the toolbox, if in auto-hide mode, hides such that
//        // the entire drawing canvas is visible and usable as a drop location
//        //        WindowManager.getDefault().clearOverlappedWindow();
//
//        //the following assert is changed by an if statement
//        //assert comp == webform.getPane();
//        if(DesignerUtils.DEBUG) {
//            DesignerUtils.debugLog(getClass().getName() + ".canImport(JComponent, DataFlavor[])");
//        }
//        if(comp != webform.getPane()) {
//            throw(new IllegalArgumentException("Wrong component."));
//        }
//        if(flavors == null) {
//            throw(new IllegalArgumentException("Null transferable."));
//        }
//
//        // DesignerPane is always enabled and editable
//        //DesignerPane c = webform.getPane();
//        //if (!(c.isEditable() && c.isEnabled())) {
//        //    return false;
//        //}
//        boolean canImport = getImportFlavor(flavors) != null;
//
//        return canImport;
//    }

    /** Get the most recent drop point that we've been notified about */
    public Point getDropPoint() {
        return dropPoint;
    }

    /** Set the most recent drop point that we've been notified about */
    public void setDropPoint(Point p) {
        dropPoint = p;
    }

    /** Get the most recent drop size that we've been notified about */
    public Dimension getDropSize() {
        return dropSize;
    }

    /** Set the most recent drop point that we've been notified about */
    public void setDropSize(Dimension d) {
        dropSize = d;
    }

    /** Set the element to insert this tag immediately preeceding
     *  in the DOM */
//    public void setInsertPosition(Position insertPos) {
    public void setInsertPosition(DomPosition insertPos) {
        this.insertPos = insertPos;
    }

    /** Set the drop action in effect so the importData method knows
     * how to react. */
    public void setDropAction(int dropAction) {
        this.dropAction = dropAction;
    }

//    /** Get the currently active transferable used during drag &amp; drop. */
//    public static Transferable getActiveTransferable() {
//        return transferable;
//    }
//
//    public static void setActiveTransferable(Transferable t) {
//        transferable = t;
//    }

//    /**
//     * Report whether the given position is in grid context
//     */
//    public static boolean isGridContext(DesignBean parent, MarkupPosition pos) {
//        if (parent.getInstance() instanceof javax.faces.component.UIForm ||
//                parent.getInstance() instanceof org.netbeans.modules.visualweb.xhtml.Form) {
//            // Look at its parent
//            parent = parent.getBeanParent();
//
//            if (parent == null) {
//                return false;
//            }
//        }
//
//        Element element = FacesSupport.getElement(parent);
//
//        if (element == null) {
//            return false;
//        }
//
////        Value val = CssLookup.getValue(element, XhtmlCss.RAVELAYOUT_INDEX);
//        CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.RAVELAYOUT_INDEX);
//
////        return val == CssValueConstants.GRID_VALUE;
//        return CssProvider.getValueService().isGridValue(cssValue);
//    }


    
}
