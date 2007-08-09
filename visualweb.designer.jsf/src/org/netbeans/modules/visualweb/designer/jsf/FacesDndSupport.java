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

import java.awt.EventQueue;
import java.util.Arrays;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssValue;
import org.netbeans.modules.visualweb.api.designer.cssengine.StyleData;
import org.netbeans.modules.visualweb.api.designer.cssengine.XhtmlCss;
import org.netbeans.modules.visualweb.api.designer.markup.MarkupService;
import org.netbeans.modules.visualweb.api.designerapi.DesignTimeTransferDataCreator;
import org.netbeans.modules.visualweb.designer.html.HtmlAttribute;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;
import com.sun.rave.designtime.BeanCreateInfo;
import com.sun.rave.designtime.BeanCreateInfoSet;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignInfo;
import com.sun.rave.designtime.DesignProject;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.DisplayItem;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import com.sun.rave.designtime.markup.MarkupMouseRegion;
import com.sun.rave.designtime.markup.MarkupPosition;
import java.awt.Component;
import org.netbeans.modules.visualweb.insync.InSyncServiceProvider;
import org.netbeans.modules.visualweb.insync.ResultHandler;
import org.netbeans.modules.visualweb.insync.UndoEvent;
import org.netbeans.modules.visualweb.insync.Util;
import org.netbeans.modules.visualweb.insync.beans.Bean;
import org.netbeans.modules.visualweb.insync.faces.HtmlBean;
import org.netbeans.modules.visualweb.insync.faces.MarkupBean;
import org.netbeans.modules.visualweb.insync.live.LiveUnit;
import org.netbeans.modules.visualweb.insync.markup.MarkupUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.netbeans.modules.visualweb.project.jsf.api.Importable;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.netbeans.modules.visualweb.xhtml.Frame;
import org.netbeans.modules.visualweb.xhtml.FramesetFrameset;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.FocusManager;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.visualweb.api.designer.Designer;
import org.netbeans.modules.visualweb.api.designer.Designer.Box;
import org.netbeans.modules.visualweb.designer.jsf.ui.JsfTopComponent;
import org.netbeans.modules.visualweb.insync.faces.FacesPageUnit;
import org.netbeans.modules.visualweb.propertyeditors.UrlPropertyEditor;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Support of DnD for <code>FacesModel</code>.
 *
 * XXX Originaly in the designer, then moved into insync, and then into designer/jsf.
 *
 * @author Peter Zavadsky
 * @author Tor Norbye (old original code)
 */
class FacesDndSupport {

//    /** XXX Copy from designer/CssBox. Solve it better way. */
//    private static final int CSS_AUTO = Integer.MAX_VALUE - 1;

    // XXX Copy from designer/DndHandler.
    private static final int DROP_ABOVE  = -1;
    private static final int DROP_CENTER = 0;
    private static final int DROP_BELOW  = 1;

    // XXX Copy from designer/DndHandler.
    /** State indicating that a drop is not allowed */
    public static final int DROP_DENIED = 0;
    /** State indicating that the drop is allowed and will cause a link */
    public static final int DROP_PARENTED = 1;
    /** State indicating that the drop is allowed and the bean will be
     *  parented by one of the beans under the cursor */
    public static final int DROP_LINKED = 2;
    
    /** Directory prefix under the project root to place the web folder */
    private static final String WEB = "web"; // NOI18N
    /** Directory prefix under the web folder root to place the resource files */
    private static final String RESOURCES = "/resources/"; // NOI18N


    private final JsfForm jsfForm;
//    // XXX TODO Get rid of this.
//    private final FacesModel facesModel;
    
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    
    /**
     * Used during an importBean: set to the bean to select when
     * we're done processing. Different methods override which
     * item should be selected.
     */
    private DesignBean select; // have we found a target to select?
    
    
    /** Creates a new instance of FacesDnDSupport */
    public FacesDndSupport(JsfForm jsfForm) {
        if (jsfForm == null) {
            throw new NullPointerException("Null argument is not allowed."); // NOI18N
        }
        this.jsfForm = jsfForm;
//        this.facesModel = facesModel;
//        this.facesModel = jsfForm.getFacesModel();
    }

    
    /**
     *  XXX The JComponent has to be replaced by element. so it is possible
     * to react exactly.
     *
     * This method indicates if a component would accept an import of the given
     * set of data flavors prior to actually attempting to import it.
     *
     * @param comp  The component to receive the transfer.  This
     *  argument is provided to enable sharing of TransferHandlers by
     *  multiple components.
     * @param flavors  The data formats available
     * @return  true if the data can be inserted into the component, false otherwise.
     */
    public boolean canImport(JComponent comp, DataFlavor[] flavors, Transferable transferable) {
        // TODO Moving to NB winsys
        // Ensure that the toolbox, if in auto-hide mode, hides such that
        // the entire drawing canvas is visible and usable as a drop location
        //        WindowManager.getDefault().clearOverlappedWindow();

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

        // DesignerPane is always enabled and editable
        //DesignerPane c = webform.getPane();
        //if (!(c.isEditable() && c.isEnabled())) {
        //    return false;
        //}
//        boolean canImport = getImportFlavor(flavors) != null;
        boolean canImport = getImportFlavor(flavors, null) != null;

        return canImport;
    }
    
    
    /**
     * XXX This method is a consequence of not having swing component tree
     * corresponding to css boxes,
     * that's why we can't get in TransferHander.canImport(JComponent, DataFlavor[])
     * the notion above which jsf component the drag/drop is.
     * It needs to be changed later, or at least hacked here. For that reason
     * this method usage is not sufficient for correct evaluation.
     *
     * Try to find a flavor that can be used to import a Transferable.
     * The set of usable flavors are tried in the following order:
     * <ol>
     *     <li>First, an attempt is made to find a flavor matching the content type
     *         of the EditorKit for the component.
     *     <li>Second, an attempt to find a text/plain flavor is made.
     *     <li>Third, an attempt to find a flavor representing a String reference
     *         in the same VM is made.
     *     <li>Lastly, DataFlavor.stringFlavor is searched for.
     * </ol>
     */
    public static DataFlavor getImportFlavor(DataFlavor[] flavors) {
        return getImportFlavor(flavors, null);
    }
    
    private static DataFlavor getImportFlavor(DataFlavor[] flavors, Transferable transferable) {
        DataFlavor plainFlavor = null;
        DataFlavor refFlavor = null;
        DataFlavor stringFlavor = null;
        DataFlavor listFlavor = null;

        for (int i = 0; i < flavors.length; i++) {
            String mime = flavors[i].getMimeType();
            Class<?> clz = flavors[i].getRepresentationClass();

            if (clz.isAssignableFrom(DisplayItem.class)) {
                return flavors[i];
            }

            if (clz == DesignBean.class) {
                return flavors[i];
            }

            if (clz.isAssignableFrom(List.class)) {
                // We don't know what's in the list, and can't look until
                // we have a transferable... but it looks promising so
                // defer decision
                listFlavor = flavors[i];
            }

            if (clz.isAssignableFrom(org.openide.nodes.Node.class)) {
                listFlavor = flavors[i];
            } // TODO: check for org.openide.util.datatransfer.MultiTransferObject

            if ((plainFlavor == null) && mime.startsWith("text/plain")) {
                plainFlavor = flavors[i];
            } else if ((refFlavor == null) &&
                    mime.startsWith("application/x-java-jvm-local-objectref") &&
                    (flavors[i].getRepresentationClass() == java.lang.String.class)) {
                refFlavor = flavors[i];
            } else if ((stringFlavor == null) && flavors[i].equals(DataFlavor.stringFlavor)) {
                stringFlavor = flavors[i];
            }
            
            // XXX
            if (refFlavor == null && mime.startsWith("application/x-creator-")) { // NOI18N
                refFlavor = flavors[i];
            }
            
        }

        if (refFlavor != null) {
            return refFlavor;
        } else if (listFlavor != null) {
            return listFlavor;
        } else if (stringFlavor != null) {
            return stringFlavor;
        } else if (plainFlavor != null) {
            return plainFlavor;
        }

        return null;
    }
    
    
    private /*public*/ boolean importData(Designer designer, JComponent comp, Transferable t, /*Object transferData,*/
    Dimension dropSize, Location location, /*CoordinateTranslator coordinateTranslator,*/ UpdateSuspender updateSuspender, int dropAction) {
        Object transferData = null;
        try {
//            DataFlavor importFlavor = webform.getImportFlavor(t.getTransferDataFlavors());
            DataFlavor importFlavor = getImportFlavor(t.getTransferDataFlavors());

            if (importFlavor == null) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, 
                        new IllegalStateException("Unusable transfer flavors " + Arrays.asList(t.getTransferDataFlavors()))); // NOI18N

                return false;
            }

            // XXX What was before in SelectionTopComp.
            if (importFlavor.getMimeType().startsWith("application/x-creator-")) { // NOI18N
//                /*return*/ webform.tcImportComponentData(comp, t);
                return importComponentData(designer, comp, t, location.getCoordinates());
            } // TEMP

            Class rc = importFlavor.getRepresentationClass();

            transferData = t.getTransferData(importFlavor);

            if (rc == String.class) {
                // XXX #6332049 When in inline editing we shouldn't steal the paste
                // (at least for the JTextComponent's.
                // This is just a workaround, it shouldn't be done this way.
                // actions should be created based on context (and inline editing
                // context is diff from the designer pane one).
//                if(webform.getManager().isInlineEditing()) {
                if (designer.isInlineEditing()) {
                    Component focusOwner = FocusManager.getCurrentManager().getFocusOwner();
                    if(focusOwner instanceof JTextComponent) {
                        JTextComponent textComp = (JTextComponent)focusOwner;
                        textComp.paste();
                        return true;
                    } 
                }

                // XXX Flowlayout mode?
//                if (webform.getPane().getCaret() != null) {
//                if (webform.getPane().hasCaret()) {
                if (designer.hasPaneCaret()) {
//                    webform.getPane().getCaret().replaceSelection((String)transferData);
//                    webform.getPane().replaceSelection((String)transferData);
                    jsfForm.getDomDocumentImpl().insertString(designer, designer.getPaneCaretRange(), (String)transferData);
                    return true;
                }
            }
        
        
        if (!isValidTransferData(t, transferData)) {
            return false;
        }
        
//        LiveUnit unit = facesModel.getLiveUnit();
        LiveUnit unit = jsfForm.getLiveUnit();
        if (unit == null) {
            NotifyDescriptor d =
                new NotifyDescriptor.Message(NbBundle.getMessage(FacesDndSupport.class, "TXT_NoHtmlDrops"),
                    NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
//            dropSize = null;
//            insertPos = Position.NONE;
//            dropPoint = null;
            
//            clearDropMatch();

            return false;
        }

        // wrap process in a try to allow cleanup in the finally
        try {
            comp.setCursor(org.openide.util.Utilities.createProgressCursor(comp));

            // XXX TEMP First give the chance to the provider.
            // Later move it after the default behaviour.
            DesignTimeTransferDataCreator dataCreator = (DesignTimeTransferDataCreator)Lookup.getDefault().lookup(DesignTimeTransferDataCreator.class);
            if (dataCreator != null) {
                DisplayItem displayItem = dataCreator.getDisplayItem(t);
                if (displayItem != null) {
                    transferData = displayItem;
                }
            }
            
            // if we are importing to the same component that we exported from then don't actually do
            // anything if the drop location is inside the drag location and set shouldRemove to false
            // so that exportDone knows not to remove any data
            if (transferData instanceof DisplayItem) {
                // Create a new type
                DisplayItem item = (DisplayItem)transferData;

//                Location location = computePositions(null, DROP_CENTER, null, getDropPoint(), insertPos, true);
                // Todo: pass in a set instead
//                doImportItem(item, null, DROP_CENTER, null, null, location);
                return importBean(designer, new DisplayItem[] {item}, null, DROP_CENTER, null, null, location, /*coordinateTranslator,*/ updateSuspender);
            } else if (transferData instanceof DesignBean[]) {
                DesignBean[] beans = (DesignBean[])transferData;
//                Location location =
//                    computePositions(null, DROP_CENTER, null, getDropPoint(), insertPos, true);
//                DesignBean droppee = location.droppee;
//
//                if (droppee != null) {
//                    Location location2 =
//                        computePositions(droppee, DROP_CENTER, null, null, null, false);
//                    doBindOrMoveItems(dropAction, beans, t, droppee, DROP_CENTER, null, location2);
//                }
                
                DesignBean droppee = location.getDroppee();
                if (droppee != null) {
//                    location = computePositions(droppee1, DROP_CENTER, null, null, null, false);
//                    location = computeLocationForBean(droppee, DROP_CENTER, null, null, dropSize, facesModel);
                    location = computeLocationForBean(droppee, DROP_CENTER, null, null, dropSize, jsfForm);
                    return doBindOrMoveItems(dropAction, beans, t, droppee, DROP_CENTER, null, location, /*coordinateTranslator,*/ updateSuspender);
                }
                return false;
            } else if (transferData instanceof org.openide.nodes.Node) {
                org.openide.nodes.Node node = (org.openide.nodes.Node)transferData;
                DataObject dobj = (DataObject)node.getCookie(DataObject.class);

                if (dobj != null) {
                    FileObject fo = dobj.getPrimaryFile();
//                    String rel = DesignerUtils.getPageRelativePath(webform, fo);
//                    String rel = getPageRelativePath(facesModel.getProject(), fo);
                    String rel = getPageRelativePath(jsfForm.getProject(), fo);
                    
                    Project fileProject = FileOwnerQuery.getOwner(fo);

//                    if (fileProject != facesModel.getProject()) {
                    if (fileProject != jsfForm.getProject()) {
                        // Import file into our project first
//                        FileObject webitem = facesModel.getMarkupFile();
                        FileObject webitem = jsfForm.getMarkupFile();

                        try {
                            if (isImage(fo.getExt()) || isStylesheet(fo.getExt())) {
                                // Import web context relative rather than file relative
//                                DesignProject project = facesModel.getLiveUnit().getProject();
                                DesignProject project = jsfForm.getLiveUnit().getProject();
                                File file = FileUtil.toFile(fo);
                                URL url = file.toURI().toURL();
                                rel = RESOURCES + UrlPropertyEditor.encodeUrl(file.getName());
                                project.addResource(url, new URI(WEB + rel));
                            } else {
                                URL url = fo.getURL();
                                rel = JsfProjectUtils.addResource(webitem, url, true);
                            }
                        } catch (FileStateInvalidException fse) {
                            ErrorManager.getDefault().notify(fse);
                        }
                    }

                    if (isImage(fo.getExt())) {
//                        Location location =
//                            computePositions(null, DROP_CENTER, null, getDropPoint(), insertPos, true);
                        return importImage(designer, rel, location, /*coordinateTranslator,*/ updateSuspender);
                    } else if (isStylesheet(fo.getExt())) {
                        return importStylesheet(rel);
                    }

                    //} else if (node instanceof org.netbeans.modules.properties.KeyNode) {
                    //    // Compute the value binding expression:
                    //    //  #{bundle.key}
                    //    // But I need to ensure that the bundle file is included somewhere in
                    //    // the page, and use the variable in the above.
                    //    // The key can be found from this node via a cookie, but it looks like
                    //    // the properties code is using old-style openide.src api calls, so
                    //    // I'd hate to include it.
                }
            } else if (transferData instanceof File) {
                File f = (File)transferData;
//                Location location = computePositions(null, DROP_CENTER, null, getDropPoint(), insertPos, true);
                return importFile(designer, f, /*null,*/ location, /*coordinateTranslator,*/ updateSuspender);
            } else if (transferData instanceof String) {
                String s = (String)transferData;

                boolean success = false;
                // XXX Try to extract files from the string (flavor used when DnD e.g. from desktop, it doesn't look correct).
                File[] files = extractFilesFromString(s);
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        File file = files[i];
                        boolean imported = importFile(designer, file, /*null,*/ location, /*coordinateTranslator,*/ updateSuspender);
                        if (imported) {
                            success = true;
                        }
                    }
                    return success;
                } else {
                    // XXX Why?
                    s = Util.truncateString(s, 600);
//                    Location location =
//                        computePositions(null, DROP_CENTER, null, getDropPoint(), insertPos, true);
                    return importString(designer, s, location, /*coordinateTranslator,*/ updateSuspender);
                }
            } else if (transferData instanceof List) {
                // TODO: place this under a single undo unit?
                List list = (List)transferData;
                Iterator it = list.iterator();
                
                boolean success = false;
//                JPanel panel = null;
                // XXX
                importFilePanel = null;
                while (it.hasNext()) {
                    Object o = it.next();

                    if (o instanceof File) {
                        File f = (File)o;
//                        Location location = computePositions(null, DROP_CENTER, null, getDropPoint(), insertPos, true);
//                        panel = importFile(designer, f, /*panel,*/ location, /*coordinateTranslator,*/ updateSuspender);
                        boolean imported = importFile(designer, f, /*panel,*/ location, /*coordinateTranslator,*/ updateSuspender);
                        if (imported) {
                            success = true;
                        }
                    }
                }
                // XXX
                importFilePanel = null;
                return success;
            } else {
//                assert false : transferData;
                return false;
            }
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);

            return false;
        } finally {
            if (comp != null) {
                comp.setCursor(null);
            }
//            dropSize = null;
//            insertPos = Position.NONE;
//            dropPoint = null;

//            clearDropMatch();
        }
        
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return false;
        }
        
        return false;
    }
    
    /**
     * @param beans An empty list into which the created beans will be added, or null
     *    if caller is not interested in the result
     * @return true iff the bean palette item was inserted successfully
     */
    private boolean importBean(Designer designer, DisplayItem[] items, DesignBean origParent, int nodePos,
    String facet, List<DesignBean> createdBeans, Location location, /*CoordinateTranslator coordinateTranslator,*/ UpdateSuspender updateSuspender)
    throws IOException {
//        if(DesignerUtils.DEBUG) {
//            DesignerUtils.debugLog(getClass().getName() + ".importBean(DisplayItem[], DesignBean, int, String, List)");
//        }
        if(items == null) {
            throw(new IllegalArgumentException("Null items array."));
        }
        select = null;

//        Location location =
//            computePositions(origParent, nodePos, facet, getDropPoint(), insertPos, true);

        // It's a app outline drag: either move or link. Don't involve
        // the transfer handler.
        String[] classes = getClasses(items);

        if (classes != null) {
            DesignBean[] beans = null;
            boolean searchUp = true;

            // Can always "move" from the palette - it's an implied copy.
            // The explorer drag & drop is a bit weird about this - they
            // only pass "move" as the valid operation, not copy.
            int action = DnDConstants.ACTION_MOVE;

            if (location.getDroppee() == null) {
//                MarkupBean bean = facesModel.getFacesUnit().getDefaultParent();
                MarkupBean bean = jsfForm.getFacesPageUnit().getDefaultParent();

                if (bean != null) {
                    if (!(location instanceof LocationImpl)) {
                        location = new LocationImpl(location);
                    }
//                    ((LocationImpl)location).droppee = facesModel.getLiveUnit().getDesignBean(bean);
                    ((LocationImpl)location).droppee = jsfForm.getLiveUnit().getDesignBean(bean);
                }
            }

            int allowed =
                computeActions(location.getDroppee(), classes, beans, action, searchUp, nodePos);

            if (allowed == DnDConstants.ACTION_NONE) {
                return false;
            }
        }

//        Document document = webform.getDocument();
        List<DisplayItem> beanItems = new ArrayList<DisplayItem>(); // HACK remove after TP

        String description = NbBundle.getMessage(FacesDndSupport.class,
                (items.length > 1) ? "LBL_DropComponents" : "LBL_DropComponent"); // NOI18N
//        UndoEvent undoEvent = facesModel.writeLock(description);
        UndoEvent undoEvent = jsfForm.writeLock(description);
        
        // Don't want BeanPaletteItem.beanCreated; only want the
        // set operation. For now the dataconnectivity module relies on
        // this to name the instances, so we've gotta honor it.
        try {
//            String description =
//                NbBundle.getMessage(DndHandler.class,
//                    (items.length > 1) ? "DropComponents" : "DropComponent"); // NOI18N
//            document.writeLock(description);

            List<DesignBean> beans = createBeans(designer, location, items, beanItems, /*coordinateTranslator,*/ updateSuspender);

            if (beans.isEmpty()) {
                return false;
            }

            if (createdBeans != null) {
                createdBeans.addAll(beans);
            }

            beansCreated(beans, beanItems);

            processLinks(location.getDroppeeElement(), null, beans, false, true, false, updateSuspender);
//            Util.customizeCreation(beans.toArray(new DesignBean[beans.size()]), facesModel);
            jsfForm.customizeCreation(beans.toArray(new DesignBean[beans.size()]));

////            selectBean(select);
////            webform.getSelection().selectBean(select);
//            fireSelectedDesignBeanChanged(select);
//            select = null;
//
////            inlineEdit(beans);
////            webform.getManager().inlineEdit(beans);
//            fireInlineEdit((DesignBean[])beans.toArray(new DesignBean[beans.size()]));
            notifyBeansDesigner((DesignBean[])beans.toArray(new DesignBean[beans.size()]), select);
            select = null;

            // Try to activate the designer surface! requestActive() isn't
            // enough -- gotta force the multiview container to be activated
            // and the right tab fronted!
        } finally {
//            document.writeUnlock();
//            facesModel.writeUnlock(undoEvent);
            jsfForm.writeUnlock(undoEvent);
        }

        return true;
    }
    
    // XXX This is a hacky method giving info back to designer, this needs to be done differently.
    public void notifyBeansDesigner(DesignBean[] designBeans, DesignBean select) {
        fireSelectedDesignBeanChanged(select);
        fireInlineEdit(designBeans);
    }
    
    private void beansCreated(List<DesignBean> beans, List<DisplayItem> beanItems) {
        int n = beans.size();

        for (int i = 0; i < n; i++) {
            DesignBean lb = beans.get(i);

            try {
//                facesModel.beanCreated(lb);
                jsfForm.designBeanCreated(lb);
            } catch (Exception e) {
                ErrorManager.getDefault().notify(e);
            }
        }

        for (int i = 0; i < n; i++) {
            DisplayItem item = beanItems.get(i);

            if (item == null) {
                continue;
            }

//            // Customize creation if requested by the component
//            BeanCreateInfo bci = item.getBeanCreateInfo();
//            BeanCreateInfoSet bcis = item.getBeanCreateInfoSet();
//
//            if ((bci == null) && (bcis == null)) {
//                continue;
//            }
//
//            // At most one of the above should be set...
//            assert !((bci != null) && (bcis != null));

            // Multiple beans could have been created from this item....
            // check that and process them
            if (item instanceof BeanCreateInfoSet) {
                BeanCreateInfoSet bcis = (BeanCreateInfoSet)item;
                // I don't want to rely on bcis.getBeanClassNames().length because one or more
                // beans could have failed to have been created (missing class, or
                // designtime canCreate returned false, etc.) - which would lead to
                // getting out of sync.
                // So instead I count by checking for identical BeanCreateItems
                // in subsequent beanItems entries
                List<DesignBean> list = new ArrayList<DesignBean>();

                for (int j = i; j < n; j++) {
                    if (beanItems.get(j) == item) {
                        list.add(beans.get(j));
                    }
                }

                if (list.size() > 0) {
                    i += (list.size() - 1);

                    DesignBean[] createdBeans = list.toArray(new DesignBean[list.size()]);
                    Result result = bcis.beansCreatedSetup(createdBeans);
//                    ResultHandler.handleResult(result, facesModel);
                    jsfForm.handleResult(result);
                }
            } else if (item instanceof BeanCreateInfo) {
                BeanCreateInfo bci = (BeanCreateInfo)item;
                DesignBean bean = beans.get(i);
                Result result = bci.beanCreatedSetup(bean);
//                ResultHandler.handleResult(result, facesModel);
                jsfForm.handleResult(result);
            } else {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                        new IllegalStateException("Invalid item=" + item)); // NOI18N
            }
        }
    }
    
    
    /**
     * Multi-function method dealing with link handling for components.
     * I used to have separate methods which accomplished various aspects of link
     * handling, but these would vary slightly in how they handled certain aspects
     * and as a result inconsistent handling would result. Thus, all the logic is handled
     * by the same method -- both "previewing" what links are available as well as actually
     * handling the linking. The flags control the behaviors.
     *
     * @param origElement The first/innermost element to start with when searching
     *    the bean hierarchy for DesignBean and MarkupMouseRegions willing to link
     *    the given beans or bean classes.
     * @param classes Array of classes to be checked for link eligibility. This is
     *    separate from beans because we often want to check if linking is possible
     *    before we actually have created beans -- such as when we're about to
     *    drag &amp; drop. Obviously in this case we can't perform linking. This
     *     parameter can be null but then beans must not be null.
     * @param beans Can be null, but if not, should correspond exactly to the classes
     *    parameter -- same length, same order, etc. This list must be specified
     *    if handleLinks is true; you can't link on just class names.
     * @param selectFirst If set, don't ask the user which target to use if there are multiple
     *    possibilities; just pick the first one. If not set, all eligible link handlers
     *    in the parent chain up from the original element will be checked, and if more than
     *    one is willing to link, the user will be presented with a list and asked to choose.
     * @param handleLinks If true, actually perform the linking.
     * @param showLinkTarget If true, highlihght the link target and region. Also sets the
     *    recentDropTarget field.
     * @return DROP_DENIED if no beans/classes were linkable for any mouse regions or
     *    DesignBeans; otherwise returns DROP_LINKED. If showLinkTarget is set, recentDropTarget
     *    will be set to the most recent such eligible DesignBean.
     */
    public int processLinks(Element origElement, Class[] classes, List beans,
    boolean selectFirst, // if there are multiple hits; if not ask user
    boolean handleLinks, // actually do linking
    boolean showLinkTarget,
    UpdateSuspender updateSuspender) {
        
//        ErrorManager.getDefault().getInstance(DesignerUtils.class.getName()).isLoggable(ErrorManager.INFORMATIONAL);
//        if(DesignerUtils.DEBUG) {
//            DesignerUtils.debugLog(getClass().getName() + ".processLinks(Element, Class[], ArrayList, boolean, boolean, boolean)");
//        }
        if((classes == null && beans == null) ||
                (classes != null && beans != null && beans.size() != classes.length)) {
            throw(new IllegalArgumentException("One of the classes array or beans list must not be null. If both are not null, than the length of them must be the same."));
        }

        int dropType = DROP_DENIED;
        int n;

        if (classes != null) {
            n = classes.length;
        } else {
            //the assert below would hide the NPE - better have NPE if not IllegalArgumentException
            //assert beans != null;
            n = beans.size();
        }

        //the assertion below does not give anything (see the if above). 
        //assert (beans != null) || (classes != null);
        //the assertion below should be replaced by an if statement, so that the check happens even if
        //the asserions are turned off (see the if block above)
        //assert (beans == null) || (classes == null) || (beans.size() == classes.length);

        for (int i = 0; i < n; i++) {
            // XXX Incorrect code.
            List<Object> candidates = new ArrayList<Object>(n);
            Class clz;
            DesignBean lb = null;

            if (beans != null) {
                lb = (DesignBean)beans.get(i);
            }

            if (classes != null) {
                clz = classes[i];
            } else {
                clz = ((DesignBean)beans.get(i)).getInstance().getClass();
            }

            try {
                // See if this new bean should be wired to the bean we
                // dropped on (or some container up the parent chain that
                // can handle the bean drop)
                DesignBean prev = null;

                for (Element element = origElement; element != null; element = getParent(element)) {
//                    DesignBean droppee = element.getDesignBean();
                    DesignBean droppee = InSyncServiceProvider.get().getMarkupDesignBeanForElement(element);

                    if (droppee == null) {
                        continue;
                    }

//                    MarkupMouseRegion region = element.getMarkupMouseRegion();
                    MarkupMouseRegion region = InSyncServiceProvider.get().getMarkupMouseRegionForElement(element);

                    if ((region != null) && region.acceptLink(droppee, lb, clz)) {
                        if (!candidates.contains(element)) {
                            candidates.add(element);
                        }
                    }

                    if (prev == droppee) {
                        continue;
                    }

                    prev = droppee;

                    DesignInfo dbi = droppee.getDesignInfo();

                    if ((dbi != null) && dbi.acceptLink(droppee, lb, clz)) {
                        if (!candidates.contains(droppee) &&
                                ((beans == null) || !beans.contains(droppee))) {
                            candidates.add(droppee);
                        }
                    }
                }
            } catch (Exception e) {
                ErrorManager.getDefault().notify(e);
            }

            if (candidates.size() == 0) {
                continue;
            }

            dropType = DROP_LINKED;

            // Store either the chosen DesignBean, or the chosen MarkupMouseRegion.
            // However, we'll need both the region and the corresponding bean, so
            // store the element instead which will point to both.
            Object selected = null;

            if (selectFirst || (candidates.size() == 1)) {
                selected = candidates.get(0);
            } else {
                // Gotta ask the user
                // Code originally emitted by the form builder:
                JPanel panel = new JPanel(new GridBagLayout());
                GridBagConstraints gridBagConstraints;
                String labelDesc = NbBundle.getMessage(FacesDndSupport.class, "LBL_ChooseTargetLabel"); // NOI18N
                JLabel label = new JLabel(labelDesc);
                gridBagConstraints = new GridBagConstraints();
                gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
                gridBagConstraints.insets = new Insets(12, 12, 11, 11);
                gridBagConstraints.anchor = GridBagConstraints.WEST;
                gridBagConstraints.weightx = 1.0;
                panel.add(label, gridBagConstraints);

                ButtonGroup buttonGroup = new ButtonGroup();

                // Iterate reverse order since list was generates from the leaf
                // up the parent chain, and I want to display outermost parents first
                for (int j = candidates.size() - 1; j >= 0; j--) {
                    String name = "";
                    Object next = candidates.get(j);

                    if (next instanceof DesignBean) {
                        DesignBean dlb = (DesignBean)next;
                        name = dlb.getInstanceName();
                        // Bug Fix: 6477496 Do not show the component description in the dialog
//                        BeanInfo bi = dlb.getBeanInfo();
//
//                        if (bi != null) {
//                            BeanDescriptor bd = bi.getBeanDescriptor();
//
//                            if (bd != null) {
//                                String desc = bd.getShortDescription();
//
//                                if (desc == null) {
//                                    desc = bd.getDisplayName();
//
//                                    if (desc == null) {
//                                        desc = "";
//                                    }
//                                }
//
//                                name =
//                                    NbBundle.getMessage(FacesDnDSupport.class, "TXT_TargetDescriptor", // NOI18N
//                                        name, desc);
//                            }
//                        }
                    } else {
//                        assert next instanceof RaveElement;
                        if (!(next instanceof Element)) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                                    new IllegalStateException("Object is expected to be of Element type, object=" + next)); // NOI18N
                        }

//                        RaveElement element = (RaveElement)next;
//                        MarkupMouseRegion region = element.getMarkupMouseRegion();
                        Element element = (Element)next;
                        MarkupMouseRegion region = InSyncServiceProvider.get().getMarkupMouseRegionForElement(element);
                        assert region != null;

                        name = region.getDisplayName();
                        // Bug Fix: 6477496 Do not show the component description in the dialog
//                        if ((region.getDescription() != null) &&
//                                (region.getDescription().length() > 0)) {
//                            name = NbBundle.getMessage(FacesDnDSupport.class, "TXT_TargetDescriptor", // NOI18N
//                                    region.getDisplayName(), region.getDescription());
//                        } else {
//                            name = region.getDisplayName();
//                        }
                    }

                    JRadioButton radioButton = new JRadioButton(name);

                    if (j == (candidates.size() - 1)) {
                        radioButton.setSelected(true);
                    }

                    radioButton.putClientProperty("liveBean", next); // NOI18N
                    buttonGroup.add(radioButton);
                    
                    if (next == origElement || next == InSyncServiceProvider.get().getMarkupDesignBeanForElement(origElement)) {
                        // #6315394 Preselect the original drop target.
                        radioButton.setSelected(true);
                    }
                    
                    gridBagConstraints = new GridBagConstraints();
                    gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
                    gridBagConstraints.insets = new Insets(0, 12, 0, 11);
                    gridBagConstraints.anchor = GridBagConstraints.WEST;
                    panel.add(radioButton, gridBagConstraints);
                }

                JPanel filler = new JPanel();
                gridBagConstraints = new GridBagConstraints();
                gridBagConstraints.weighty = 1.0;
                panel.add(filler, gridBagConstraints);

                String title = NbBundle.getMessage(FacesDndSupport.class, "LBL_ChooseTarget"); // NOI18N
                DialogDescriptor dlg =
                    new DialogDescriptor(panel, title, true, DialogDescriptor.OK_CANCEL_OPTION,
                        DialogDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, 
                    // DialogDescriptor.BOTTOM_ALIGN,
                    null, //new HelpCtx("choose_target"), // NOI18N
                        null);

                Dialog dialog = DialogDisplayer.getDefault().createDialog(dlg);
//                dialog.show();
                dialog.setVisible(true);

                if (dlg.getValue().equals(DialogDescriptor.OK_OPTION)) {
                    Enumeration enm = buttonGroup.getElements();

                    while (enm.hasMoreElements()) {
                        JRadioButton button = (JRadioButton)enm.nextElement();

                        if (button.isSelected()) {
                            selected = button.getClientProperty("liveBean"); // NOI18N

                            break;
                        }
                    }
                } // else: Cancel, or Esc: do nothing; selected will stay null
            }

            if (showLinkTarget) {
                if (selected instanceof DesignBean) {
                    DesignBean droppee = (DesignBean)selected;

                    if (droppee instanceof MarkupDesignBean) {
//                        recentDropTarget = (MarkupDesignBean)droppee;
//                        showDropMatch(recentDropTarget, null, DROP_LINKED);
                        fireDropTargetChanged((MarkupDesignBean)droppee, null, DROP_LINKED);
                    }
                } else {
//                    assert selected instanceof RaveElement;
                    if (!(selected instanceof Element)) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                                new IllegalStateException("Object is expected to be of Element type, object=" + selected)); // NOI18N
                    }
                    

//                    RaveElement element = (RaveElement)selected;
//                    DesignBean droppee = element.getDesignBean();
                    Element element = (Element)selected;
                    DesignBean droppee = InSyncServiceProvider.get().getMarkupDesignBeanForElement(element);

                    if (droppee instanceof MarkupDesignBean) {
//                        recentDropTarget = (MarkupDesignBean)droppee;
////                        showDropMatch(recentDropTarget, element.getMarkupMouseRegion(), DROP_LINKED);
//                        showDropMatch(recentDropTarget,
//                                InSyncServiceProvider.get().getMarkupMouseRegionForElement(element),
//                                DROP_LINKED);
//                        fireDropTargetChanged((MarkupDesignBean)droppee, InSyncServiceProvider.get().getMarkupMouseRegionForElement(element), DROP_LINKED);
                        fireDropTargetChanged((MarkupDesignBean)droppee, element, DROP_LINKED);
                    }
                }
            }

            if ((selected == null) || !handleLinks || (beans == null)) {
                return dropType;
            }

//            Document document = webform.getDocument();

            String description = NbBundle.getMessage(FacesDndSupport.class, "LBL_LinkComponent"); // NOI18N
//            UndoEvent undoEvent = facesModel.writeLock(description);
            UndoEvent undoEvent = jsfForm.writeLock(description);
            try {
//                String description = NbBundle.getMessage(DndHandler.class, "LinkComponent"); // NOI18N
//                document.writeLock(description);

                lb = (DesignBean)beans.get(i);

                try {
                    // If you drop on an existing component, see if they
                    // can be wired together
                    // Try to bind the two together - for example, if you
                    // drop a RowSet on a bean that has a RowSet property,
                    // the RowSet property is bound to this particular
                    // RowSet.
                    if (selected instanceof DesignBean) {
                        DesignBean droppee = (DesignBean)selected;
                        assert droppee.getDesignInfo().acceptLink(droppee, lb,
                            lb.getInstance().getClass());

                        MarkupDesignBean mbean = null;

                        if (droppee instanceof MarkupDesignBean) {
                            // link beans might perform lots and lots of
                            // updates on the element - that's the case
                            // for the data grid when you bind a table
                            // to it for example.  So batch up all these
                            // modifications into a single change event
                            // on the top level element.
                            mbean = (MarkupDesignBean)droppee;
//                            webform.getDomSynchronizer().setUpdatesSuspended(mbean, true);
//                            webform.setUpdatesSuspended(mbean, true);
                            updateSuspender.setSuspended(mbean, true);
                        }

                        try {
//                            facesModel.linkBeans(droppee, lb);
                            jsfForm.linkDesignBeans(droppee, lb);
                        } finally {
                            if (mbean != null) {
                                // Process queued up changes
//                                webform.getDomSynchronizer().setUpdatesSuspended(mbean, false);
//                                webform.setUpdatesSuspended(mbean, false);
                                updateSuspender.setSuspended(mbean, false);
                            }
                        }

                        // The target bean should be selected instead of
                        // the droppee!
                        select = droppee;
                    } else {
//                        assert selected instanceof RaveElement;
                        if (!(selected instanceof Element)) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                                    new IllegalStateException("Object is expected to be of Element type, object=" + selected)); // NOI18N
                        }

//                        RaveElement element = (RaveElement)selected;
//                        MarkupMouseRegion region = element.getMarkupMouseRegion();
                        Element element = (Element)selected;
                        MarkupMouseRegion region = InSyncServiceProvider.get().getMarkupMouseRegionForElement(element);
                        assert region != null;

//                        DesignBean droppee = element.getDesignBean();
                        DesignBean droppee = InSyncServiceProvider.get().getMarkupDesignBeanForElement(element);
                        assert droppee != null;
                        assert region.acceptLink(droppee, lb, clz);

                        MarkupDesignBean mbean = null;

                        if (droppee instanceof MarkupDesignBean) {
                            mbean = (MarkupDesignBean)droppee;
//                            webform.getDomSynchronizer().setUpdatesSuspended(mbean, true);
//                            webform.setUpdatesSuspended(mbean, true);
                            updateSuspender.setSuspended(mbean, true);
                        }

                        try {
                            Result r = region.linkBeans(droppee, lb);
//                            ResultHandler.handleResult(r, facesModel);
                            jsfForm.handleResult(r);
                        } finally {
                            if (mbean != null) {
//                                webform.getDomSynchronizer().setUpdatesSuspended(mbean, false);
//                                webform.setUpdatesSuspended(mbean, false);
                                updateSuspender.setSuspended(mbean, false);
                            }
                        }
                    }
                } catch (Exception e) {
                    ErrorManager.getDefault().notify(e);
                }
            } finally {
//                document.writeUnlock();
//                facesModel.writeUnlock(undoEvent);
                jsfForm.writeUnlock(undoEvent);
            }
        }

        return dropType;
    }
    
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }
    
    public static final String PROPERTY_DROP_TARGET = "dropTarget"; // NOI18N
    public static final String PROPERTY_SELECTED_DESIGN_BEAN = "selectedDesignBean"; // NOI18N
    public static final String PROPERTY_REFRESH = "refresh";
    public static final String PROPERTY_INLINE_EDIT = "inlineEdit"; // NOI18N
    
//    private void fireDropTargetChanged(MarkupDesignBean markupDesignBean, MarkupMouseRegion markupMouseRegion, int dropType) {
//        propertyChangeSupport.firePropertyChange(PROPERTY_DROP_TARGET, null, new DropInfo(markupDesignBean, markupMouseRegion, dropType)); // NOI18N
    private void fireDropTargetChanged(final MarkupDesignBean markupDesignBean, final Element regionElement, final int dropType) {
        // XXX It happens the model is not updated yet (source-rendered elements link)! (See scheduling in DomSynchronizer)
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                propertyChangeSupport.firePropertyChange(PROPERTY_DROP_TARGET, null, new DropInfo(markupDesignBean, regionElement, dropType)); // NOI18N
            }
        });
    }
    
    private void fireSelectedDesignBeanChanged(final DesignBean selected) {
        // XXX It happens the model is not updated yet (source-rendered elements link)! (See scheduling in DomSynchronizer)
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                propertyChangeSupport.firePropertyChange(PROPERTY_SELECTED_DESIGN_BEAN, null, selected);
            }
        });
    }
    
    /*private*/public void fireRefreshNeeded(final boolean refreshAll) {
        // XXX It happens the model is not updated yet (source-rendered elements link)! (See scheduling in DomSynchronizer)
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                propertyChangeSupport.firePropertyChange(PROPERTY_REFRESH, !refreshAll, refreshAll);
            }
        });
    }

    private void fireInlineEdit(final DesignBean[] designBeans) {
        // XXX It happens the model is not updated yet (source-rendered elements link)! (See scheduling in DomSynchronizer)
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                propertyChangeSupport.firePropertyChange(PROPERTY_INLINE_EDIT, null, designBeans);
            }
        });
    }

    
    // XXX Moved into insync/Util.
//    public void customizeCreation(List beans) {
//        int n = beans.size();
//
//        for (int i = 0; i < n; i++) {
//            DesignBean lb = (DesignBean)beans.get(i);
//            DesignInfo lbi = lb.getDesignInfo();
//
//            if (lbi != null) {
//                Customizer2 customizer = null; //lbi.getCreateCustomizer(lb);
//
//                if (customizer != null) {
//                    CustomizerDisplayer lcd =
//                        new CustomizerDisplayer(lb, customizer, customizer.getHelpKey(), facesModel);
//                    lcd.show();
//                }
//            }
//        }
//    }
    
    public String[] getClasses(DisplayItem[] items) {
        List<String> list = new ArrayList<String>(items.length);

        for (DisplayItem item : items) {
            if (item instanceof BeanCreateInfo) {
                BeanCreateInfo beanCreateInfo = ((BeanCreateInfo)item);
                String className = beanCreateInfo.getBeanClassName();
                if (className == null) {
                    // #112454 Bad impl of BeanCreateInfo.
                    info(new IllegalArgumentException("Bad implementation of BeanCreateInfo, " +
                            "it returns null bean class name, " +
                            "beanCreateInfo=" + beanCreateInfo)); // NOI18N
                } else {
                    list.add(className);
                }
            } else if (item instanceof BeanCreateInfoSet) {
                BeanCreateInfoSet beanCreateInfoSet = ((BeanCreateInfoSet)item);
                String[] cls = beanCreateInfoSet.getBeanClassNames();
                if (cls == null) {
                    // #112454 Bad impl of BeanCreateInfoSet.
                    info(new IllegalArgumentException("Bad implementation of BeanCreatInfoSet, " +
                            "it returns null array of bean class names, " +
                            "beanCreateInfoSet=" + beanCreateInfoSet)); // NOI18N
                } else {
                    for (int k = 0; k < cls.length; k++) {
                        String className = cls[k];
                        if (className == null) {
                            // #112454 Bad impl of BeanCreateImplSet.
                            info(new IllegalArgumentException("Bad implementation of BeanCreatInfoSet, " +
                                    "it returns null(s) in array of bean class names, " +
                                    "beanCreateInfoSet=" + beanCreateInfoSet +
                                    ", beanClasses=" + Arrays.asList(cls))); // NOI18N
                        } else {
                            list.add(cls[k]);
                        }
                    }
                }
            } else {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                        new IllegalStateException("Illegal item=" + item)); // NOI18N
            }
        }

        return list.toArray(new String[list.size()]);
    }
    
    private List<DesignBean> createBeans(Designer designer, Location location, DisplayItem[] items, List<DisplayItem> beanItems,
    /*CoordinateTranslator coordinateTranslator,*/ UpdateSuspender updateSuspender) throws IOException {
        DesignBean droppee = location.getDroppee();
        MarkupPosition position = location.getPos();
        String facet = location.getFacet();

        List<DesignBean> created = new ArrayList<DesignBean>(2 * items.length); // slop for BeanCreateInfoSets

        for (int i = 0; i < items.length; i++) {

//            if (!(items[i] instanceof BeanPaletteItem)) {
//                importItem(items[i], null, DROP_CENTER, null, null);
//
//                continue;
//            }

            DisplayItem item = (DisplayItem)items[i];
            
            // <change>
            // XXX There is a need to get class name even from the bean create info set.
//            String className = item.getBeanClassName();
// ====
            String className = null;
            // </change>

//            // Customize creation if requested by the component
//            BeanCreateInfo bci = item.getBeanCreateInfo();
//            BeanCreateInfoSet bcis = item.getBeanCreateInfoSet();

//            // At most one of the above should be set...
//            assert !((bci != null) && (bcis != null));

            String[] classes = null;
            int current = 0;
            int max = 0;

            if (item instanceof BeanCreateInfoSet) {
                BeanCreateInfoSet bcis = (BeanCreateInfoSet)item;
                // Set us up for multiple bean creation
                classes = bcis.getBeanClassNames();
                max = classes.length;
            } else if (item instanceof BeanCreateInfo) {
                className = ((BeanCreateInfo)item).getBeanClassName();
            } else {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                        new IllegalStateException("Illegal item=" + item)); // NOI18N
                continue;
            }

            do {
                // If we're creating multiple beans from a BeanCreateInfoSet
                // fetch the next class name
                if (current < max) {
                    className = classes[current++];
                }

//                DesignBean parent = Util.findParent(className, droppee, position.getUnderParent(), true, facesModel);
                DesignBean parent = jsfForm.findParent(className, droppee, position.getUnderParent(), true);

                if (parent != null) {
                    boolean droppingOnFrameset = parent.getInstance() instanceof FramesetFrameset;
                    boolean droppingOnFrame = parent.getInstance() instanceof Frame;

                    if (droppingOnFrameset || droppingOnFrame) {
                        //                if (!(droppingOnFrameset && (className.equals(HtmlBean.PACKAGE+"Frame") ||
                        //                                             className.equals(HtmlBean.PACKAGE+"FramesetFrameset")))) { // NOI18N
                        if (!(droppingOnFrameset &&
                                (className.equals(Frame.class.getName()) ||
                                className.equals(Frame.class.getName())))) {
                            NotifyDescriptor d =
                                new NotifyDescriptor.Message(NbBundle.getMessage(FacesDndSupport.class,
                                        "TXT_NoFrameDrops", item.getDisplayName()),
                                    NotifyDescriptor.WARNING_MESSAGE);
                            DialogDisplayer.getDefault().notify(d);

                            continue;
                        }
                    }
                }

                // Native method - is this result cached?
                //if (className.equals(Jsp_Directive_Include.class.getName())) {
                String savedClass = null;

                if (className.equals(HtmlBean.PACKAGE + "Jsp_Directive_Include")) { // NOI18N

                    // You're dropping a jsp:directive.include box. These cannot
                    // be CSS positioned (e.g. dropped at a particular pixel
                    // location over a grid layout area) - because this directive
                    // does not have a style attribute, and Jasper will scream
                    // bloody murder if we put it in.
                    // So instead we drop a <div>, and place the jsp directive
                    // inside it.
                    // I wanted to solve this more cleanly: If you're dropping
                    // a visual component and it doesn't have a "style" property,
                    // it doesn't support positioning, so apply the above
                    // <div>-wrapping trick. However, while I can check if a
                    // component about to be dropped has a style property through
                    // the beaninfo, I can't tell if it's a visual component
                    // or not until it's actually instantiated (via
                    // DesignBean.isVisualBean()) - so I can't do it this way or
                    // suddenly e.g. rowset beans would be parented by <div> since
                    // they don't have a style attribute....   So for now
                    // it's just a special case check for the one known visual
                    // component that doesn't have a style attribute.
                    //if (getDropPoint() != null && insertPos == null) {
                    savedClass = className;

                    // XXX Why not Div.class.getName() ?
                    className = HtmlBean.PACKAGE + "Div"; // NOI18N

                    //}
                    // Comment on above: we now ALWAYS want to insert a div, since
                    // even in flow context you want a div which specifies
                    // "position: relative" in order to ensure that absolutely
                    // positioned children within the fragment are relative
                    // to the jsp's top level corner, not the current viewport
                    // or absolutely positioned -ancestor- of the jsp include
                    // box.
                }

                // Adjust position, in case we're default inserting it
                // If we're default-positioin inserting into a form or body,
                // and it ends with a <br>, insert the new component before
                // the <br> so that it doesn't create a new line.
                if ((parent == null) &&
                        ((position == null) ||
                        ((position.getUnderParent() == null) &&
                        (position.getBeforeSibling() == null)))) {
                    // See if I have a Br
//                    MarkupBean formBean = facesModel.getFacesUnit().getDefaultParent();
                    MarkupBean formBean = jsfForm.getFacesPageUnit().getDefaultParent();

                    if (formBean != null) {
                        Bean[] children = formBean.getChildren();

                        if ((children != null) && (children.length > 0)) {
                            Bean b = children[children.length - 1];

                            if (b instanceof MarkupBean) {
                                MarkupBean mb = (MarkupBean)b;

                                if ((mb.getElement() != null) &&
                                        mb.getElement().getTagName().equals(HtmlTag.BR.name)) {
                                    position =
                                        new MarkupPosition(formBean.getElement(), mb.getElement());
                                }
                            }
                        }
                    }
                }

                DesignBean bean = createBean(className, parent, position, facet);
                select = bean;

                if (bean != null) {
                    created.add(bean);
                    beanItems.add(items[i]);
                }

                if (bean instanceof MarkupDesignBean) {
                    MarkupDesignBean mbean = (MarkupDesignBean)bean;
                    positionBean(designer, mbean, parent, mbean.getElement(), location, /*coordinateTranslator,*/ updateSuspender);

                    if ((savedClass != null) && bean.isContainer()) {
                        DesignBean child = createBean(savedClass, bean, null, null);

                        if (child != null) {
                            created.add(child);

                            // Ensure that the two lists are kept in sync
                            beanItems.add(null);
                            
                            // #104792 To select the fragment not the added surrounding div.
                            select = child;
                        }

                        // If inserted in flow, put a <div> with relative
                        // positioning around it to ensure that absolutely
                        // positioned children in the div are absolute relative
                        // to the jsp box, not whatever outer container is
                        // establishing the current absolute positions
//                        if (insertPos != Position.NONE) {
//                        if (!Util.isGridMode(facesModel)) {
                        if (!jsfForm.isGridMode()) {
                            DesignProperty styleProp = bean.getProperty("style"); // NOI18N

                            if (styleProp != null) {
                                String mods = "position: relative"; // NOI18N
                                String style = (String)styleProp.getValue();

                                if ((style != null) && (style.length() > 0)) {
                                    styleProp.setValue(style + "; " + mods);
                                } else {
                                    styleProp.setValue(mods);
                                }
                            }
                        }

                        /*
                        DesignProperty styleProp = bean.getProperty("style"); // NOI18N
                        if (styleProp != null) {
                            String mods = "overflow: hidden; width: 240px"; // NOI18N
                            String style = (String)styleProp.getValue();
                            if (style != null && style.length() > 0) {
                                styleProp.setValue(style + "; " + mods);
                            } else {
                                styleProp.setValue(mods);
                            }
                        }
                        */
                    }
                }
            } while (current < max);
        }

        // XXX This is cleared anyway.
//        insertPos = Position.NONE;

        //facesUnit.setInsertBefore(null);
        return created;
    }
    
    private DesignBean createBean(String className, DesignBean parent,
        com.sun.rave.designtime.Position pos, String facet) {
//        LiveUnit unit = facesModel.getLiveUnit();
        LiveUnit unit = jsfForm.getLiveUnit();

        if (parent != null) {
            // It's possible that we're adding to a unit other than
            // the web form one -- such as a Session Bean unit for
            // a rowset
            unit = (LiveUnit)parent.getDesignContext();

            // Ensure that the MarkupPosition is correct
            if (pos instanceof MarkupPosition) {
                MarkupPosition markupPos = (MarkupPosition)pos;

//                if (markupPos.getUnderParent() instanceof RaveElement) {
//                    RaveElement parentElement = (RaveElement)markupPos.getUnderParent();
                if (markupPos.getUnderParent() instanceof Element) {
                    Element parentElement = (Element)markupPos.getUnderParent();

//                    while (parentElement.getDesignBean() != parent) {
                    while (InSyncServiceProvider.get().getMarkupDesignBeanForElement(parentElement) != parent) {
//                        if (parentElement.getParentNode() instanceof RaveElement) {
//                            parentElement = (RaveElement)parentElement.getParentNode();
                        if (parentElement.getParentNode() instanceof Element) {
                            parentElement = (Element)parentElement.getParentNode();
                        } else {
                            break;
                        }
                    }

                    if ((parentElement != null) && (parentElement != markupPos.getUnderParent())) {
                        // The parent DesignBean is for a higher-up element. This can happen
                        // when the acceptChild/acceptParent calls force parenting up higher
                        // in the chain.
                        pos = new MarkupPosition(parentElement, null);
                    }
                }
            }
        }

        if (facet != null) {
            return unit.createFacet(facet, className, parent);
        }

        return unit.createBean(className, parent, pos);
    }
    
    /** Set the absolute position of the component. **/
    private void positionBean(Designer designer, MarkupDesignBean lb, DesignBean origParent, Element element,
    Location location, /*CoordinateTranslator coordinateTranslator,*/ UpdateSuspender updateSuspender) {
        // TODO - transfer this logic to computePositions
        if ((location.getCoordinates() == null) || (element == null)) {
            return;
        }

        DesignBean parent = origParent;

        // Only position beans dropped on a grid area or a form
        boolean grid = false;

        // XXX TODO: transfer this logic into Utilities instead and make
        // sure we do it the same way everywhere!
        if (element.getParentNode() instanceof Element) {
            Element pe = (Element)element.getParentNode();

            if (pe.getTagName().equals(HtmlTag.FSUBVIEW.name) &&
                    pe.getParentNode() instanceof Element) {
                pe = (Element)pe.getParentNode();
            }

            // The component may -render- a -rave-layout setting,
            // so look in the rendered HTML for the layout setting
            // rather than in the JSP DOM
//            RaveElement rendered = ((RaveElement)pe).getRendered();
            Element rendered = MarkupService.getRenderedElementForElement(pe);
            if (rendered != null) {
                pe = rendered;
            }

//            Value val = CssLookup.getValue(pe, XhtmlCss.RAVELAYOUT_INDEX);
            CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(pe, XhtmlCss.RAVELAYOUT_INDEX);

//            if (val == CssValueConstants.GRID_VALUE) {
            if (CssProvider.getValueService().isGridValue(cssValue)) {
                grid = true;
            } else if (pe.getTagName().endsWith(HtmlTag.FORM.name)) { // h:form too
                pe = (Element)pe.getParentNode();
//                val = CssLookup.getValue(pe, XhtmlCss.RAVELAYOUT_INDEX);
                CssValue cssValue2 = CssProvider.getEngineService().getComputedValueForElement(pe, XhtmlCss.RAVELAYOUT_INDEX);

//                if (val == CssValueConstants.GRID_VALUE) {
                if (CssProvider.getValueService().isGridValue(cssValue2)) {
                    grid = true;
                }
            }
        }

//        if (((parent == null) || grid || Util.isFormBean(facesModel, parent))) {
        if (((parent == null) || grid || jsfForm.isFormDesignBean(parent))) {
//            GridHandler gm = GridHandler.getInstance();
//            setInitialPosition(designer, facesModel, lb, element, location.getCoordinates(), location.getSize(), /*coordinateTranslator,*/ updateSuspender);
            setInitialPosition(designer, lb, element, location.getCoordinates(), location.getSize(), /*coordinateTranslator,*/ updateSuspender);
            select = lb;
        }
    }
    
    /**
     * Set the initial position for a given component.
     * It is assumed that the bean does not already have an associated position.
     *
     * @param editor The designer pane containing the element
     * @param element The element we want to set a style attribute for
     * @param pos The point where we want the element positioned. If null,
     *              this method has no effect.
     * @param size The size to assign to the component. If null, don't set a
     *              size, use the intrinsic size.
     */
    private static void setInitialPosition(Designer designer, /*FacesModel facesModel,*/ MarkupDesignBean bean, Element element, Point pos, Dimension size,
    /*CoordinateTranslator coordinateTranslator,*/ UpdateSuspender updateSuspender) {
        if (pos == null) {
            return;
        }

        DesignProperty styleProp = bean.getProperty("style"); // NOI18N

        if (styleProp == null) {
            // No style property - can't set position!!
            return;
        }

        String style = (String)styleProp.getValue();
        StringBuffer sb = new StringBuffer();

        if ((style != null) && (style.length() > 0)) {
            sb.append(style);
            sb.append("; ");
        }

        // Locate a grid layout parent
//        Document doc = editor.getDocument();
//        WebForm webform = doc.getWebForm();
//        XhtmlCssEngine engine = webform.getMarkup().getCssEngine();

//        // This model should already be locked when we attempt to do this
//        assert facesModel.isWriteLocked();

        int x = pos.x;
        int y = pos.y;

//        GridHandler gridHandler = GridHandler.getInstance();
        // See if we should translate the coordinates
        if (element.getParentNode() instanceof Element) {
            Element parent = (Element)element.getParentNode();
//            CssBox parentBox = CssBox.getBox(parent);
//
//            if (parentBox != null) {
//                // Translate coordinates from absolute/viewport
//                // to absolute coordinates relative to the target
//                // grid container
////                Point p = translateCoordinates(parentBox, x, y);
//                Point p = gridHandler.translateCoordinates(parentBox, x, y);
//                x = p.x;
//                y = p.y;
//            }
//            Point point = gridHandler.translateCoordinates(parent, x, y);
//            Point point = coordinateTranslator.translateCoordinates(parent, x, y);
            Point point = translateCoordinates(designer, parent, x, y);
            x = point.x;
            y = point.y;
        }

//        x = snapX(x);
//        y = snapY(y);
//        x = gridHandler.snapX(x);
//        y = gridHandler.snapY(y);
//        x = coordinateTranslator.snapX(x);
//        y = coordinateTranslator.snapY(y);
        x = designer.snapX(x, null);
        y = designer.snapY(y, null);

        // prevent multiple updates for the same element - only need a single refresh
        try {
//            webform.getDomSynchronizer().setUpdatesSuspended(bean, true);
//            webform.setUpdatesSuspended(bean, true);
            updateSuspender.setSuspended(bean, true);

            // TODO: Find the -rendered- element; I have to look up margins on it
            // since it could come from style classes. For example, for a Braveheart
            // button, if I have a CSS rule   .Btn2 { margin: 200px }  I won't find
            // this style looking at the JSP element (ui:button) I need to do lookup
            // on the rendered <input class="Btn2" ...> element.
            // The "top" and "left" properties are relative to the margin edge of the
            // component yet the position is specified relative to the border (visible) area
//            int leftMargin = CssLookup.getLength(element, XhtmlCss.MARGIN_LEFT_INDEX);
//            int topMargin = CssLookup.getLength(element, XhtmlCss.MARGIN_TOP_INDEX);
            int leftMargin = CssProvider.getValueService().getCssLength(element, XhtmlCss.MARGIN_LEFT_INDEX);
            int topMargin = CssProvider.getValueService().getCssLength(element, XhtmlCss.MARGIN_TOP_INDEX);
            x -= leftMargin;
            y -= topMargin;

            List set = new ArrayList(5);
            List remove = new ArrayList(3);

            sb.append("position: absolute; ");
            sb.append("left: ");
            sb.append(Integer.toString(x));
            sb.append("px; ");
            sb.append("top: ");
            sb.append(Integer.toString(y));
            sb.append("px");

            if (size != null) {
                if (!Util.setDesignProperty(bean, HtmlAttribute.WIDTH, size.width)) {
                    sb.append("; width: ");
                    sb.append(Integer.toString(size.width));
                    sb.append("px"); // NOI18N
                } else {
                    // Do I need to try to delete the width from the existing value string?
                    // The only way this could get here is if the component has had a chance
                    // to set widths/sizes with the create customizers
                }

                if (!Util.setDesignProperty(bean, HtmlAttribute.HEIGHT, size.height)) {
                    sb.append("; height: ");
                    sb.append(Integer.toString(size.height));
                    sb.append("px"); // NOI18N
                } else {
                    // Do I need to try to delete the width from the existing value string?
                    // The only way this could get here is if the component has had a chance
                    // to set widths/sizes with the create customizers
                }
            }

            styleProp.setValue(sb.toString());
        } finally {
//            webform.getDomSynchronizer().setUpdatesSuspended(bean, false);
//            webform.setUpdatesSuspended(bean, false);
            updateSuspender.setSuspended(bean, false);
        }
    }
    
    // XXX Moved from designer/../GridHandler.
    private static Point translateCoordinates(Designer designer, Element parent, int x, int y) {
//        CssBox parentBox = CssBox.getBox(parent);
//        CssBox parentBox = webForm.findCssBoxForElement(parent);
        Box parentBox = designer.findBoxForElement(parent);

        if (parentBox != null) {
            // Translate coordinates from absolute/viewport
            // to absolute coordinates relative to the target
            // grid container
//                Point p = translateCoordinates(parentBox, x, y);
//            return translateCoordinates(parentBox, x, y);
            return JsfSupportUtilities.translateCoordinates(parentBox, x, y);
        }
        
        return new Point(x, y);
    }
    

    // Moved to Util.
//    /** Attempt to set the given attribute on the bean to the given length
//     * and return true iff it succeeds.
//     */
//    public /*private*/ static boolean setDesignProperty(DesignBean bean, String attribute, int length) {
//        DesignProperty prop = bean.getProperty(attribute);
//
//        if (prop != null) {
//            PropertyDescriptor desc = prop.getPropertyDescriptor();
//            Class clz = desc.getPropertyType();
//
//            // I can do == instead of isAssignableFrom because
//            // both String and Integer are final!
//            if (clz == String.class) {
//                prop.setValue(Integer.toString(length));
//
//                return true;
//            } else if (clz == Integer.TYPE) {
//                prop.setValue(new Integer(length));
//
//                return true;
//            }
//        }
//
//        return false;
//    }
    
    
    private boolean doBindOrMoveItems(int dropAction, DesignBean[] beans, Transferable t,
    DesignBean dropNode, int nodePos, String facet, Location location, /*CoordinateTranslator coordinateTranslator,*/
    UpdateSuspender updateSuspender) {
//        if(DesignerUtils.DEBUG) {
//            DesignerUtils.debugLog(getClass().getName() + ".bindOrMoveItems(int, DesignBean[], Transferable, DesignBean, int, String)");
//        }
        if(t == null) {
            throw(new IllegalArgumentException("Null transferable."));
        }
        if ((beans == null) || (beans.length == 0)) {
            return false;
        }

        // It's a app outline drag: either move or link. Don't involve
        // the transfer handler.
        int allowed = computeActions(dropNode, t, false, nodePos);

        if (allowed == DnDConstants.ACTION_NONE) {
            return false;
        }

        if (dropAction == DnDConstants.ACTION_COPY) {
//            LiveUnit unit = facesModel.getLiveUnit();
            LiveUnit unit = jsfForm.getLiveUnit();
            Transferable newTransferable = unit.copyBeans(beans);

            if (newTransferable == null) {
                return false;
            }

//            Location location =
//                computePositions((DesignBean)dropNode, nodePos, facet, null, null, false);
            DesignBean parent = location.getDroppee();
            pasteBeans(/*webform,*/ t, parent, location.getPos(), null, /*coordinateTranslator,*/ updateSuspender);

            return true;
        } else if (nodePos != DROP_CENTER) {
            // MOVE: fall through to handle
        } else if ((dropAction == DnDConstants.ACTION_LINK) ||
                ((dropAction == DnDConstants.ACTION_MOVE) &&
                ((allowed & DnDConstants.ACTION_LINK) != 0))) {
            // LINK
            // (We treat a move when link is permitted as a link since move
            // is where you haven't selected any modifier keys so
            // it's the mode where we make a best guess as to what
            // you want. It would be better if we had a modifier key
            // to let the user FORCE move though. Perhaps we should rethink
            // this since there IS a modifier key for link (ctrl-shift).
            List<DesignBean> list = new ArrayList<DesignBean>(beans.length);

            for (int i = 0; i < beans.length; i++) {
                list.add(beans[i]);
            }

            assert nodePos == DROP_CENTER;
            handleLinks((DesignBean)dropNode, list, updateSuspender);

            return true;
        } else if ((dropAction & DnDConstants.ACTION_MOVE) != 0) {
            // MOVE: fall through to handle
        }

        // Move
//        Location location =
//            computePositions((DesignBean)dropNode, nodePos, facet, null, null, false);
        DesignBean parent = location.getDroppee();
        moveBeans(/*webform,*/ beans, parent, location.getPos(), updateSuspender);
        return true;
    }


    public int computeActions(DesignBean droppee, Transferable transferable) {
        return computeActions(droppee, transferable, false, DROP_CENTER);
    }

    /** Figure out which kind of action we can do for the given
     * transferable over the given droppee.
     *
     * @param droppee The target component
     * @param transferable The transferable being considered dropped
     *        or linked on the droppee. If it references multiple
     *        components, it will set the allowable action union of
     *        all the components.
     * @param searchUp If true, you are permitted to search upwards
     *        as well.
     */
    private int computeActions(DesignBean droppee, Transferable transferable, boolean searchUp,
        int nodePos) {
//        if(DesignerUtils.DEBUG) {
//            DesignerUtils.debugLog(getClass().getName() + ".computeActions(DesignBean, Transferable, boolean, int)");
//        }
        if(transferable == null) {
            throw(new IllegalArgumentException("Null transferable."));
        }
        int action = DnDConstants.ACTION_NONE;
        String[] classes = null;
        DesignBean[] beans = null;
        DataFlavor[] flavors = transferable.getTransferDataFlavors();

        for (int j = 0; j < flavors.length; j++) {
            Class clz = flavors[j].getRepresentationClass();

            if (clz == DisplayItem.class) {
                // Can always "move" from the palette - it's an implied copy.
                // The explorer drag & drop is a bit weird about this - they
                // only pass "move" as the valid operation, not copy.
                action |= DnDConstants.ACTION_MOVE;

                Object data;

                try {
                    data = transferable.getTransferData(flavors[j]);
                } catch (Exception e) {
                    ErrorManager.getDefault().notify(e);

                    return action;
                }

                if (!(data instanceof DisplayItem)) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, 
                            new IllegalStateException("Invalid DisplayItem transfer data: " + data)); // NOI18N

                    return action;
                }

                List list = new ArrayList();
                DisplayItem item = (DisplayItem)data;

                if (item instanceof BeanCreateInfo) {
                    BeanCreateInfo bci = (BeanCreateInfo)item;
                    classes = new String[] { bci.getBeanClassName() };
                } else if (item instanceof BeanCreateInfoSet) {
                    BeanCreateInfoSet bcis = (BeanCreateInfoSet)item;
                    classes = bcis.getBeanClassNames();
                } else {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                            new IllegalStateException("Illegal item=" + item)); // NOI18N
                }

                break;
            } else if (clz == DesignBean.class) {
                Object data;

                try {
                    data = transferable.getTransferData(flavors[j]);
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);

                    return action;
                } catch (UnsupportedFlavorException ex) {
                    ErrorManager.getDefault().notify(ex);

                    return action;
                }

                if ((data != null) && data instanceof DesignBean[]) {
                    beans = (DesignBean[])data;

                    if (beans == null) {
                        return action;
                    }

                    classes = new String[beans.length];

                    for (int i = 0; i < beans.length; i++) {
                        classes[i] = beans[i].getInstance().getClass().getName();
                    }

                    // See if we can move these beans. We can move if the
                    // parent target location is not a child of any of the beans,
                    // or the beans themselves
                    boolean cannot = false;

                    for (int i = 0; i < beans.length; i++) {
                        DesignBean d = droppee;

                        while (d != null) {
                            if (d == beans[i]) {
                                cannot = true;

                                break;
                            }

                            d = d.getBeanParent();
                        }
                    }

                    if (!cannot) {
                        action |= DnDConstants.ACTION_MOVE;
                    }

                    break;
                }
            } else if (clz == LiveUnit.ClipImage.class) {
                Object data;

                try {
                    data = transferable.getTransferData(flavors[j]);
                } catch (Exception e) {
                    ErrorManager.getDefault().notify(e);

                    return action;
                }

                if (!(data instanceof LiveUnit.ClipImage)) {
                    ErrorManager.getDefault().log("Invalid LiveUnit.ClipImage transfer data: " +
                        data);

                    return action;
                }

                LiveUnit.ClipImage luc = (LiveUnit.ClipImage)data;
                classes = luc.getTypes();
            }
        }

        if (classes == null) {
            return action;
        }

        return computeActions(droppee, classes, beans, action, searchUp, nodePos);
    }

    private int computeActions(DesignBean origDroppee, String[] classes, DesignBean[] beans,
        int action, boolean searchUp, int nodePos) {
        DesignBean droppee = null;

        if (nodePos == DROP_CENTER) { // Can only link if pointing at a node
linkCheckFinished: 
            for (int i = 0; i < classes.length; i++) {
                try {
//                    Class clz = facesModel.getFacesUnit().getBeanClass(classes[i]);
                    Class clz = jsfForm.getFacesPageUnit().getBeanClass(classes[i]);
                    DesignBean lb = null;

                    if (beans != null) {
                        lb = beans[i];
                    }

                    droppee = origDroppee;

                    for (droppee = origDroppee; droppee != null;
                            droppee = droppee.getBeanParent()) {
                        // Prevent self-linking
                        if (beans != null) {
                            boolean same = false;

                            for (int j = 0; j < beans.length; j++) {
                                if (droppee == beans[j]) {
                                    same = true;

                                    break;
                                }
                            }

                            if (same) {
                                if (!searchUp) {
                                    break;
                                } else {
                                    continue;
                                }
                            }
                        }

                        DesignInfo dbi = droppee.getDesignInfo();

                        if ((dbi != null) && dbi.acceptLink(droppee, lb, clz)) {
                            action |= DnDConstants.ACTION_LINK;

                            break linkCheckFinished;
                        }

                        if (!searchUp) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        } else {
            // For pos=ABOVE or BELOW, the passed in node points to the specific
            // node -sibling-, but we want the parent
            origDroppee = origDroppee.getBeanParent();
        }

        // See if any of the droppee parents accept the new item as a
        // child
        for (int i = 0; i < classes.length; i++) {
//            DesignBean parent = Util.findParent(classes[i], origDroppee, null, searchUp, facesModel);
            DesignBean parent = jsfForm.findParent(classes[i], origDroppee, null, searchUp);

            if (parent != null) {
                action |= DnDConstants.ACTION_COPY;

                break;
            } else {
                action &= ~DnDConstants.ACTION_MOVE;

                break;
            }
        }

        return action;
    }

    // Moved to Util.
//    public /*private*/ DesignBean findParent(String className, DesignBean droppee, Node parentNode,
//        boolean searchUp) {
//        if (isGridMode() && (droppee == null) && (facesModel.getLiveUnit() != null)) {
//            MarkupBean bean = facesModel.getFacesUnit().getDefaultParent();
//
//            if (bean != null) {
//                droppee = facesModel.getLiveUnit().getDesignBean(bean);
//            }
//        }
//
//        DesignBean parent = droppee;
//
//        if (searchUp) {
//            for (; (parent != null) && !parent.isContainer(); parent = parent.getBeanParent()) {
//                ;
//            }
//        }
//
//        LiveUnit unit = facesModel.getLiveUnit();
//
//        if (searchUp) {
//            boolean isHtmlBean =
//                className.startsWith(HtmlBean.PACKAGE) &&
//                // f:verbatim is explicitly allowed where jsf components can go
//                // XXX Why not F_Verbatim.class.getName() ?
//                !(HtmlBean.PACKAGE + "F_Verbatim").equals(className); // NOI18N
//
//            if (isHtmlBean) {
//                // We can't drop anywhere below a "renders children" JSF
//                // component
//                parent = findHtmlContainer(parent);
//            }
//        }
//
//        // Validate the parent: walk up the parent chain until you find
//        // a parent which will accept the child.
//        for (; parent != null; parent = parent.getBeanParent()) {
//            if (unit.canCreateBean(className, parent, null)) {
//                // Found it
//                break;
//            }
//
//            if (!searchUp) {
//                return null;
//            }
//        }
//
//        if ((parent == null) && (parentNode != null)) {
//            // Adjust hierarchy: we should pass in a parent
//            // pointer based on where we are: locate the closest
//            // jsf parent above
//            Node n = parentNode;
//            MarkupBean mb = null;
//
//            while (n != null) {
//                if (n instanceof Element) {
//                    Element e = (Element)n;
////                    mb = FacesSupport.getMarkupBean(webform.getDocument(), e);
//                    mb = getMarkupBean(facesModel, e);
//
//                    if (mb != null) {
//                        break;
//                    }
//                }
//
//                n = n.getParentNode();
//            }
//
//            if (mb != null) {
//                DesignBean lmb = facesModel.getLiveUnit().getDesignBean(mb);
//
//                if (lmb.isContainer()) {
//                    parent = lmb;
//                }
//            }
//
//            if (parent == null) {
//                parent = facesModel.getRootBean();
//            }
//        }
//
//        return parent;
//    }

    // Moved to Util.
//    /**
//     *  Return true if this document is in "grid mode" (objects
//     *  should be positioned by absolute coordinates instead of in
//     *  "flow" order.
//     *
//     *  @return true iff the document should be in grid mode
//     */
//    private boolean isGridMode() {
//        Element b = facesModel.getHtmlBody();
//
//        if (b == null) {
//            return false;
//        }
//
////        Value val = CssLookup.getValue(b, XhtmlCss.RAVELAYOUT_INDEX);
//        CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(b, XhtmlCss.RAVELAYOUT_INDEX);
//
////        return val == CssValueConstants.GRID_VALUE;
//        return CssProvider.getValueService().isGridValue(cssValue);
//    }
    
    /** Handle links where the target is a possibly nonvisual bean so has no element */
    public void handleLinks(DesignBean droppee, List beans, UpdateSuspender updateSuspender) {
//        Document document = webform.getDocument();

        int n = beans.size();
        String description = NbBundle.getMessage(FacesDndSupport.class, (n > 1) ? "LBL_LinkComponents" : "LBL_LinkComponent"); // NOI18N
//        UndoEvent undoEvent = facesModel.writeLock(description);
        UndoEvent undoEvent = jsfForm.writeLock(description);
        try {
//            int n = beans.size();
//            String description =
//                NbBundle.getMessage(DndHandler.class, (n > 1) ? "LinkComponents" : "LinkComponent"); // NOI18N
//            document.writeLock(description);

            for (int i = 0; i < n; i++) {
                DesignBean lb = (DesignBean)beans.get(i);

                try {
                    // If you drop on an existing component, see if they
                    // can be wired together
                    // Try to bind the two together - for example, if you
                    // drop a RowSet on a bean that has a RowSet property,
                    // the RowSet property is bound to this particular
                    // RowSet.
                    DesignInfo dbi = droppee.getDesignInfo();
                    boolean canLink =
                        (dbi != null) && dbi.acceptLink(droppee, lb, lb.getInstance().getClass());

                    if (canLink) {
                        MarkupDesignBean mbean = null;

                        if (droppee instanceof MarkupDesignBean) {
                            // link beans might perform lots and lots of
                            // updates on the element - that's the case
                            // for the data grid when you bind a table
                            // to it for example.  So batch up all these
                            // modifications into a single change event
                            // on the top level element.
                            mbean = (MarkupDesignBean)droppee;
//                            webform.getDomSynchronizer().setUpdatesSuspended(mbean, true);
//                            webform.setUpdatesSuspended(mbean, true);
                            updateSuspender.setSuspended(mbean, true);
                        }

                        try {
//                            facesModel.linkBeans(droppee, lb);
                            jsfForm.linkDesignBeans(droppee, lb);
                        } finally {
                            if (mbean != null) {
                                // Process queued up changes
//                                webform.getDomSynchronizer().setUpdatesSuspended(mbean, false);
//                                webform.setUpdatesSuspended(mbean, false);
                                updateSuspender.setSuspended(mbean, false);
                            }
                        }

                        // The target bean should be selected instead of
                        // the droppee!
                        select = droppee;
                    }
                } catch (Exception e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        } finally {
//            document.writeUnlock();
//            facesModel.writeUnlock(undoEvent);
            jsfForm.writeUnlock(undoEvent);
        }
    }
    

    /**
     * Move the given beans to the given parent and markup position.
     */
    public /*private*/ /*static*/ void moveBeans(/*WebForm webform,*/ DesignBean[] beans, DesignBean parent,
    MarkupPosition pos, UpdateSuspender updateSuspender) {
        if ((beans == null) || (beans.length == 0)) {
            return;
        }

//        Document document = null;
//
//        if (webform != null) {
//            // XXX what about locking on java-only buffers? (SessionBean1 etc.)
//            document = webform.getDocument();
//        }
//        FacesModel facesModel = webform == null ? null : webform.getModel();

        LiveUnit lu = (LiveUnit)beans[0].getDesignContext();

        UndoEvent undoEvent;
//        if (facesModel != null) {
        if (jsfForm != null) {
            String description =
                NbBundle.getMessage(FacesDndSupport.class,
                    (beans.length > 1) ? "LBL_MoveComponents" // NOI18N
                                       : "LBL_MoveComponent"); // NOI18N
//            undoEvent = facesModel.writeLock(description);
            undoEvent = jsfForm.writeLock(description);
        } else {
            undoEvent = null; // No undo event
            lu.writeLock(undoEvent);
        }
        
        try {
//            if (document != null) {
//                String description =
//                    NbBundle.getMessage(SelectionTopComp.class,
//                        (beans.length > 1) ? "MoveComponents" // NOI18N
//                                           : "MoveComponent"); // NOI18N
//                document.writeLock(description);
//            } else {
//                lu.writeLock(null); // No undo event
//            }

            // Decide whether we need to strip out position coordinates
            // from the beans being moved
            boolean stripPos = !isGridContext(parent, pos);

            for (int i = 0; i < beans.length; i++) {
                if (!(beans[i] instanceof MarkupDesignBean)) {
                    continue;
                }

                MarkupDesignBean bean = (MarkupDesignBean)beans[i];

                if (stripPos) {
                    Element e = bean.getElement();

                    try {
//                        webform.getDomSynchronizer().setUpdatesSuspended(bean, true);
//                        webform.setUpdatesSuspended(bean, true);
                        if (updateSuspender != null) {
                            updateSuspender.setSuspended(bean, true);
                        }
                        
//                        CssLookup.removeLocalStyleValue(e, XhtmlCss.POSITION_INDEX);
//                        CssLookup.removeLocalStyleValue(e, XhtmlCss.LEFT_INDEX);
//                        CssLookup.removeLocalStyleValue(e, XhtmlCss.TOP_INDEX);
                        Util.removeLocalStyleValueForElement(e, XhtmlCss.POSITION_INDEX);
                        Util.removeLocalStyleValueForElement(e, XhtmlCss.LEFT_INDEX);
                        Util.removeLocalStyleValueForElement(e, XhtmlCss.TOP_INDEX);
                    } finally {
//                        webform.getDomSynchronizer().setUpdatesSuspended(bean, false);
//                        webform.setUpdatesSuspended(bean, false);
                        if (updateSuspender != null) {
                            updateSuspender.setSuspended(bean, false);
                        }
                    }
                }

                lu.moveBean(bean, parent, pos);
            }
        } finally {
//            if (document != null) {
//                document.writeUnlock();
//            } else {
//                lu.writeUnlock(null);
//            }
//            if (facesModel != null) {
//                facesModel.writeUnlock(undoEvent);
            if (jsfForm != null) {
                jsfForm.writeUnlock(undoEvent);
            } else {
                lu.writeUnlock(undoEvent);
            }
        }
    }

    private boolean importImage(Designer designer, final File file, Location location, /*CoordinateTranslator coordinateTranslator,*/ UpdateSuspender updateSuspender) {
        try {
            URL url = file.toURI().toURL();

            // Import web context relative rather than file relative
            //FileObject webitem = webform.getDataObject().getPrimaryFile();
            //String local = JsfProjectHelper.addResource(webitem, url, true);
//            DesignProject project = facesModel.getLiveUnit().getProject();
            DesignProject project = jsfForm.getLiveUnit().getProject();
            String local = RESOURCES + UrlPropertyEditor.encodeUrl(file.getName());
            project.addResource(url, new URI(WEB + local));

            return importImage(designer, local, location, /*coordinateTranslator,*/ updateSuspender);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return false;
    }

    private boolean importImage(Designer designer, final String local, Location location, /*CoordinateTranslator coordinateTranslator,*/ UpdateSuspender updateSuspender) {
        // Import the file.
        // If it's an image, just create an image component for it
        // and drop it on the page.  (If there are multiple images,
        // don't position them. This will happen automatically
        // because we will clear the position after the first dropped
        // image.)
        // If it's a stylesheet, add it as a stylesheet.
        // Otherwise consult the import mechanism (e.g. for html,
        // jsp, and friends).
        // For image I still need the position, so delay slightly.
//        Location location =
//            computePositions(null, DROP_CENTER, null, getDropPoint(), insertPos, true);
        DesignBean droppee = location.getDroppee();
//        Document document = webform.getDocument();

        String description = NbBundle.getMessage(FacesDndSupport.class, "LBL_DropComponent"); // NOI18N
//        UndoEvent undoEvent = facesModel.writeLock(description);
        UndoEvent undoEvent = jsfForm.writeLock(description);
        try {
//            String description = NbBundle.getMessage(DndHandler.class, "DropComponent"); // NOI18N
//            document.writeLock(description);

            String className;
            String propertyName;

            // XXX This should be decided by the parent bean.
            // I.e. appropriate api is missing.
//            if (DesignerUtils.isBraveheartPage(webform.getJspDom())) {
            // XXX This shouldn't be here resolved, but in parent bean.
//            if (InSyncServiceProvider.get().isWoodstockPage(facesModel.getJspDom())) {
            if (InSyncServiceProvider.get().isWoodstockPage(jsfForm.getJspDom())) {
                // Use woodstock ImageComponent component
                className = com.sun.webui.jsf.component.ImageComponent.class.getName(); // NOI18N
                propertyName = "url";
//            } else if (InSyncServiceProvider.get().isBraveheartPage(facesModel.getJspDom())) {
            } else if (InSyncServiceProvider.get().isBraveheartPage(jsfForm.getJspDom())) {
                className = com.sun.rave.web.ui.component.ImageComponent.class.getName(); // NOI18N
                propertyName = "url";
            } else {
                className = javax.faces.component.html.HtmlGraphicImage.class.getName(); // NOI18N
                propertyName = "value";
            }

//            DesignBean parent = Util.findParent(className, droppee, location.getPos().getUnderParent(), true, facesModel);
            DesignBean parent = jsfForm.findParent(className, droppee, location.getPos().getUnderParent(), true);
            DesignBean bean = createBean(className, parent, location.getPos(), null);
            select = bean;

            if (bean instanceof MarkupDesignBean) {
                MarkupDesignBean mbean = (MarkupDesignBean)bean;
                positionBean(designer, mbean, parent, mbean.getElement(), location, /*coordinateTranslator,*/ updateSuspender);
            }

//            selectBean(select);
//            webform.getSelection().selectBean(select);
            fireSelectedDesignBeanChanged(select);
            select = null;

            DesignProperty prop = bean.getProperty(propertyName);

            if (prop != null) {
                prop.setValue(local);
            }

            //inlineEdit(beans);
        } finally {
//            document.writeUnlock();
//            facesModel.writeUnlock(undoEvent);
            jsfForm.writeUnlock(undoEvent);
        }
        return true;
    }

    private boolean importStylesheet(final File file) {
        try {
            URL url = file.toURI().toURL();

            // Import web context relative rather than file relative
            //FileObject webitem = webform.getDataObject().getPrimaryFile();
            //String local = JsfProjectHelper.addResource(webitem, url, true);
//            DesignProject project = facesModel.getLiveUnit().getProject();
            DesignProject project = jsfForm.getLiveUnit().getProject();
            String local = RESOURCES + UrlPropertyEditor.encodeUrl(file.getName());
            project.addResource(url, new URI(WEB + local));

            return importStylesheet(local);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return false;
    }

    private boolean importStylesheet(final String local) {
//        Document document = webform.getDocument();

        //ArrayList beanItems = new ArrayList();
        String description = NbBundle.getMessage(FacesDndSupport.class, "LBL_DropComponent"); // NOI18N
//        UndoEvent undoEvent = facesModel.writeLock(description);
        UndoEvent undoEvent = jsfForm.writeLock(description);
        try {
//            String description = NbBundle.getMessage(DndHandler.class, "DropComponent"); // NOI18N
//            document.writeLock(description);

            // Add stylesheet link
//            org.w3c.dom.Document dom = facesModel.getJspDom();
            org.w3c.dom.Document dom = jsfForm.getJspDom();
            Element root = dom.getDocumentElement();
//            MarkupUnit markup = facesModel.getMarkupUnit();
            MarkupUnit markup = jsfForm.getMarkupUnit();
            Element html = markup.findHtmlTag(root);
            DesignBean bean = null;

            if (html == null) {
                DesignBean uihead = null;
//                LiveUnit lu = facesModel.getLiveUnit();
                LiveUnit lu = jsfForm.getLiveUnit();
                DesignBean[] heads = null;
                
//                Project project = facesModel.getProject();
                Project project = jsfForm.getProject();
                if (project != null) {                    
                    if (J2eeModule.JAVA_EE_5.equals(JsfProjectUtils.getJ2eePlatformVersion(project))) {
                        // JSF 1.2 - hence use woodstock Head component
                        heads = lu.getBeansOfType(com.sun.webui.jsf.component.Head.class);                       
                    } else {
                        // JSF 1.1
                        heads = lu.getBeansOfType(com.sun.rave.web.ui.component.Head.class);
                    }
                }

                if ((heads != null) && (heads.length > 0)) {
                    uihead = heads[0];

                    if (uihead != null) {
                        if (J2eeModule.JAVA_EE_5.equals(JsfProjectUtils.getJ2eePlatformVersion(project))) {
                            // JSF 1.2 - hence woodstock Link component
                            bean = lu.createBean(com.sun.webui.jsf.component.Link.class.getName(), uihead,
                                    new com.sun.rave.designtime.Position()); // NOI18N
                            bean.getProperty("url").setValue(local); // NOI18N                            
                        } else {
                            // No stylesheet link exists - add one
                            // XXX TODO get rid of using xhtml directly, 
                            // it should be shielded by api.
                            bean = lu.createBean(com.sun.rave.web.ui.component.Link.class.getName(), uihead,
                                    new com.sun.rave.designtime.Position()); // NOI18N
                            bean.getProperty("url").setValue(local); // NOI18N
                        }
                    }
                }
            } else {
                // Gotta replace with HtmlTag.LINK.name
                Element head = Util.findChild(HtmlTag.HEAD.name, html, false);
                // XXX TODO get rid of using xhtml directly, 
                // it should be shielded by api.
                bean = /*webform.getDocument().*/createBean(org.netbeans.modules.visualweb.xhtml.Link.class.getName(), head, null);
                bean.getProperty("href").setValue(local); // NOI18N
            }

            if (bean == null) {
                return false;
            }

            bean.getProperty("rel").setValue("stylesheet"); // NOI18N
            bean.getProperty("type").setValue("text/css"); // NOI18N
        } finally {
//            document.writeUnlock();
//            facesModel.writeUnlock(undoEvent);
            jsfForm.writeUnlock(undoEvent);
        }

//        webform.refresh(true);
        fireRefreshNeeded(true);
        return true;
    }

    // XXX
    private JPanel importFilePanel;
    private boolean importFile(Designer designer, final File f, /*JPanel panel,*/ Location location, /*CoordinateTranslator coordinateTranslator,*/ UpdateSuspender updateSuspender) {
        if (f.exists()) {
            String name = f.getName();
            String extension = name.substring(name.lastIndexOf(".") + 1); // NOI18N
//            Project project = facesModel.getProject();
            Project project = jsfForm.getProject();
            
            // XXX #95601 Skip the file if it is already inside the project.
            if (FileOwnerQuery.getOwner(f.toURI()) == project) {
//                return panel;
                return true;
            }

            //String mime = FileUtil.getMIMEType(extension);
            // They've only registered gif and jpg so not a big deal
            if (/*DesignerUtils.*/isImage(extension)) {
//                Location location =
//                    computePositions(null, DROP_CENTER, null, getDropPoint(), insertPos, true);
                importImage(designer, f, location, /*coordinateTranslator,*/ updateSuspender);

//                return panel;
                return true;
            } else if (/*DesignerUtils.*/isStylesheet(extension)) {
                importStylesheet(f);

//                return panel;
                return true;
            }

// <dep> XXX Getting rid of dep on project/importpage.
// TODO There should be a better API created.
//            panel = PageImport.importRandomFile(project, f, extension, panel);
// ====
            Lookup l = Lookup.getDefault();
            Lookup.Template<Importable.PageImportable> template = new Lookup.Template<Importable.PageImportable>(Importable.PageImportable.class);
            Iterator<? extends Importable.PageImportable> it = l.lookup(template).allInstances().iterator();
            while (it.hasNext()) {
                Importable.PageImportable pageImportable = it.next();
//                panel = pageImportable.importRandomFile(project, f, extension, panel);
                importFilePanel = pageImportable.importRandomFile(project, f, extension, importFilePanel);
                break;
            }
// </dep>

//            if (panel == null) {
            if (importFilePanel == null) {
//                JsfProjectUtils.importFile(facesModel.getProject(), f);
                JsfProjectUtils.importFile(jsfForm.getProject(), f);
            }
        }

//        return panel;
        return false;
    }
    
    private /*public*/ boolean importString(Designer designer, String string, Location location, /*CoordinateTranslator coordinateTranslator,*/ UpdateSuspender updateSuspender) {
        // Import the string as part of an output text component
//        Location location =
//            computePositions(null, DROP_CENTER, null, getDropPoint(), insertPos, true);
        DesignBean droppee = location.getDroppee();

//        Document document = webform.getDocument();

        String description = NbBundle.getMessage(FacesDndSupport.class, "LBL_DropComponent"); // NOI18N
//        UndoEvent undoEvent = facesModel.writeLock(description);
        UndoEvent undoEvent = jsfForm.writeLock(description);
        try {
//            String description = NbBundle.getMessage(DndHandler.class, "DropComponent"); // NOI18N
//            document.writeLock(description);

            String className;
            String propertyName;

            // XXX This should be decided by the parent bean.
            // I.e. appropriate api is missing.
//            if (DesignerUtils.isBraveheartPage(webform.getJspDom())) {
            // XXX This shouldn't be here resolved, but in parent bean.
//            if (InSyncServiceProvider.get().isWoodstockPage(facesModel.getJspDom())) {
            if (InSyncServiceProvider.get().isWoodstockPage(jsfForm.getJspDom())) {
                // JSF 1.2 - hence use woodstock StaticText component
                className = com.sun.webui.jsf.component.StaticText.class.getName(); // NOI18N
                propertyName = "text";
//            } else if (InSyncServiceProvider.get().isBraveheartPage(facesModel.getJspDom())) {
            } else if (InSyncServiceProvider.get().isBraveheartPage(jsfForm.getJspDom())) {
                className = com.sun.rave.web.ui.component.StaticText.class.getName(); // NOI18N
                propertyName = "text";
            } else {
                className = javax.faces.component.html.HtmlOutputText.class.getName(); // NOI18N
                propertyName = "value";
            }

//            DesignBean parent = Util.findParent(className, droppee, location.getPos().getUnderParent(), true, facesModel);
            DesignBean parent = jsfForm.findParent(className, droppee, location.getPos().getUnderParent(), true);
            DesignBean bean = createBean(className, parent, location.getPos(), null);
            select = bean;

            if (bean instanceof MarkupDesignBean) {
                MarkupDesignBean mbean = (MarkupDesignBean)bean;
                positionBean(designer, mbean, parent, mbean.getElement(), location, /*coordinateTranslator,*/ updateSuspender);
            }

//            selectBean(select);
//            webform.getSelection().selectBean(select);
            fireSelectedDesignBeanChanged(select);
            select = null;

            DesignProperty prop = bean.getProperty(propertyName);

            if (prop != null) {
                // Clean up string a little
                // TODO - should I look for <HTML> markup and if so unset the escape property?
                string = string.replace('\n', ' ');
                string = string.replace('\r', ' ');
                prop.setValue(string);
            }

            //inlineEdit(beans);
        } finally {
//            document.writeUnlock();
//            facesModel.writeUnlock(undoEvent);
            jsfForm.writeUnlock(undoEvent);
        }
        return true;
    }
    
    /** Create a new bean of the given type, positioned below parent
     * before the given node. Returns the created element. */
    private DesignBean createBean(String className, Node parent, Node before) {
        MarkupPosition pos = new MarkupPosition(parent, before);
        DesignBean parentBean = /*FacesSupport.*/Util.findParentBean(parent);
//        LiveUnit unit = facesModel.getLiveUnit();
        LiveUnit unit = jsfForm.getLiveUnit();
        DesignBean bean = unit.createBean(className, parentBean, pos);

        return bean;
    }

    // Moved to Util.
//    /** Given a node, return the nearest DesignBean that "contains" it */
//    public /*private*/ static DesignBean findParentBean(Node node) {
//        while (node != null) {
////            if (node instanceof RaveElement) {
////                RaveElement element = (RaveElement)node;
//            if (node instanceof Element) {
//                Element element = (Element)node;
//
////                if (element.getDesignBean() != null) {
////                    return element.getDesignBean();
////                }
//                MarkupDesignBean markupDesignBean = InSyncServiceProvider.get().getMarkupDesignBeanForElement(element);
//                if (markupDesignBean != null) {
//                    return markupDesignBean;
//                }
//            }
//
//            node = node.getParentNode();
//        }
//
//        return null;
//    }

    
//    private static Location computeLocationForBean(DesignBean bean, int where, String facet, Point canvasPos, Dimension dropSize, FacesModel facesModel) {
    private static Location computeLocationForBean(DesignBean bean, int where, String facet, Point canvasPos, Dimension dropSize, JsfForm jsfForm) {
        if (bean == null) {
            throw new NullPointerException("Bean can't be null!"); // NOI18N
        }
        
        LocationImpl location = new LocationImpl();
        location.facet = facet;
        location.coordinates = canvasPos;
//        location.size = getDropSize();
        location.size = dropSize;

        if ((bean != null) && !LiveUnit.isCssPositionable(bean)) {
            location.coordinates = null;
        }

        DesignBean parent = null;
        Node under = null;
        Node before = null;

        Element element = null;

        if (bean != null) {
            element = Util.getElement(bean);

            // No, can still reposition these guys.
            //if (element == null) {
            //    bean = null;
            //}
            location.droppeeElement = element;
        }

        //location.droppeeChosen = true;
        if (where == DROP_CENTER) { // child of bean
            parent = bean;
            under = element;
            before = null;
        } else if (where == DROP_ABOVE) { // before bean
            parent = bean.getBeanParent();
            before = element;

            if (element != null) {
                under = element.getParentNode();
            } else { // after bean
                under = null;
            }
        } else {
            parent = bean.getBeanParent();
            assert where == DROP_BELOW;
            before = null;

            for (int i = 0, n = parent.getChildBeanCount(); i < (n - 1); i++) {
                if (parent.getChildBean(i) == bean) {
                    DesignBean next = parent.getChildBean(i + 1);
                    Element nextElement = Util.getElement(next);
                    before = nextElement;

                    break;
                }
            }

            if (element != null) {
                under = element.getParentNode();
            } else { // after bean
                under = null;
            }
        }

        location.droppee = parent;

        // If default-positioning, try to place the component before the <br/>, if
        // the the br is the last element under the default parent.
        if ((under == null) && (before == null)) {
            if (parent == null) {
//                Element parentElement = facesModel.getFacesUnit().getDefaultParent().getElement();
                Element parentElement = jsfForm.getFacesPageUnit().getDefaultParent().getElement();
                parent = MarkupUnit.getMarkupDesignBeanForElement(parentElement);
            }
            location.pos = getDefaultMarkupPositionUnderParent(parent /*, facesModel*/);
        } else {
            location.pos = new MarkupPosition(under, before);
        }

        return location;
    }
    
    public /*private*/ static MarkupPosition getDefaultMarkupPositionUnderParent(DesignBean parent /*, FacesModel facesModel*/) {
        Node under = null;
        Node before = null;
        if ((parent != null) && parent instanceof MarkupDesignBean) {
            under = ((MarkupDesignBean)parent).getElement();
        }

//        if (under == null) {
////                under = webform.getModel().getFacesUnit().getDefaultParent().getElement();
//            under = facesModel.getFacesUnit().getDefaultParent().getElement();
//        }

        if (under != null) {
            NodeList children = under.getChildNodes();

            if (children.getLength() > 0) {
                Node last = children.item(children.getLength() - 1);

                while (last != null) {
                    if ((last.getNodeType() != Node.TEXT_NODE) ||
                            !JsfSupportUtilities.onlyWhitespace(last.getNodeValue())) {
                        break;
                    }

                    last = last.getPreviousSibling();
                }

                if ((last != null) && (last.getNodeType() == Node.ELEMENT_NODE) &&
                        last.getNodeName().equals(HtmlTag.BR.name)) {
                    before = last;
                }
            }
        }
        
        return new MarkupPosition(under, before);
    }
    
    // XXX Moved to JsfSupportUtilities.
//    /** Return true iff the string contains only whitespace */
//    private static boolean onlyWhitespace(String s) {
////        if(DEBUG) {
////            debugLog(DesignerUtils.class.getName() + ".onlyWhitespace(String)");
////        }
//        if(s == null) {
//            return true;
//        }
//        int n = s.length();
//        
//        for (int i = 0; i < n; i++) {
//            char c = s.charAt(i);
//            
//            /* See the "empty-cells" documentation in CSS2.1 for example:
//             * it sounds like only SOME of the whitespace characters are
//             * truly considered ignorable whitespace: \r, \n, \t, and space.
//             * So do something more clever in some of these cases.
//             */
//            if (!Character.isWhitespace(c)) {
//                return false;
//            }
//        }
//        
//        return true;
//    }
    
    /** Return the relative path of the given GenericItem to the page folder */
    private static String getPageRelativePath(Project project, FileObject fo) {
        FileObject webroot;
        webroot = JsfProjectUtils.getDocumentRoot(project);
        
        String rootName = webroot.getPath();
        String fileName = fo.getPath();
        
        if (fileName.startsWith(rootName)) {
            return fileName.substring(rootName.length());
        }
        
        return null;
    }

    
    /** Return true if the extension indicates that this is an image */
    public static boolean isImage(String extension) {
        return (extension.equalsIgnoreCase("jpg") || // NOI18N
                extension.equalsIgnoreCase("gif") || // NOI18N
                extension.equalsIgnoreCase("png") || // NOI18N
                extension.equalsIgnoreCase("jpeg")); // NOI18N
    }
    
    public static boolean isStylesheet(String extension) {
        return extension.equalsIgnoreCase("css"); // NOI18N
    }
    
    /** Tries to extract files from the string. Only if all tokens 
     * (delimited by the default delimiter except the space char) mean file, otherwise null is returned.
     * @return Array of files or <code>null</code> */
    private static File[] extractFilesFromString(String string) {
        List<File> files = new ArrayList<File>();
        // XXX Do not use space as delimiter (the file name might contain it).
        StringTokenizer st = new StringTokenizer(string, "\t\n\r\f"); // NOI18N
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            File file = extractFileFromString(s);
            if (file == null) {
                // All tokens have to be files, otherwise don't extract any.
                return null;
            }
            files.add(file);
        }
        return files.toArray(new File[files.size()]);
    }
    
    private static File extractFileFromString(String string) {
        // We don't know if the String passed in to us represents
        // an actual String, or a pointer to an actual file
        // on disk. (For example, some operating systems where you
        // drag an image file from the desktop and drop it on Creator
        // will pass in e.g.   "file:/users/home/image.gif". 
        // In this case we should detect that we're really dealing
        // with a path, not a literal String.
        // To do that, we do an -experimental- URL parse, and if
        // it suceeds, we assume we're dealing with a path, otherwise
        // it's a plain string.
        try {
            // Try to construct a URL; if it's a url do a file
            // import of that file
            String urlString = string.trim();
            URL url = new URL(urlString);
            urlString = url.toExternalForm();

            // looks like an okay url
            if (url.getProtocol().equals("file")) { // NOI18N

                // <markup_separation>
//                        String filename = MarkupUnit.fromURL(urlString);
                // ====
                String filename = InSyncServiceProvider.get().fromURL(urlString);
                // </markup_separation>

                if (filename != urlString) {
                    File file = new File(filename);

                    if (file.exists()) {
                        return file;
                    }

                    // fall through for normal string handling
                }
            }
        } catch (MalformedURLException mue) {
            // It's just normal text; fall through
            // NOTE: This is not an error condition!! We don't know that
            // the string reprents a URL - it was just a hypothesis we're
            // testing! When this fails we know that the hypothesis
            // was wrong.
        }
        
        return null;
    }

    // Moved to Util.
//    /** Strip the given string to the given maximum length of
//     * characters. If the string is not that long, just return
//     * it.  If it needs to be truncated, truncate it and append
//     * "...".  maxLength must be at least 4. */
//    public static String truncateString(String s, int maxLength) {
//        assert maxLength >= 4;
//        
////        if(DEBUG) {
////            debugLog(DesignerUtils.class.getName() + ".truncateString(String, int)");
////        }
//        if(s == null) {
//            throw(new IllegalArgumentException("Null string to truncate."));// NOI18N
//        }
//        
//        if (s.length() > maxLength) {
//            // Should "..." be localizable?
//            return s.substring(0, maxLength - 3) + "...";
//        } else {
//            return s;
//        }
//    }

    // Moved to Util.
//    /**
//     * Return the element for the live bean. May be null, for non faces beans for example.
//     */
//    private static Element getElement(DesignBean lb) {
//        if (lb instanceof MarkupDesignBean) {
//            return ((MarkupDesignBean)lb).getElement();
//        } else {
//            return null;
//        }
//    }

    // Moved to Util.
//    /**
//     * Find the nearest DesignBean container that allows html children.
//     * This will typically be the parent you pass in, but if there
//     * are any beans up in the hierarchy that renders their own
//     * children, then the outermost such parent's parent will be used,
//     * since "renders children" jsf components cannot contain markup.
//     */
//    public static DesignBean findHtmlContainer(DesignBean parent) {
//        DesignBean curr = parent;
//
//        for (; curr != null; curr = curr.getBeanParent()) {
//            if (curr.getInstance() instanceof F_Verbatim) {
//                // If you have a verbatim, we're okay to add html comps below it
//                return parent;
//            }
//
//            if (curr.getInstance() instanceof UIComponent) {
//                // XXX Maybe now, whitin insync one could provide a better check for the classloader.
//                
//				// Need to set the Thread's context classloader to be the Project's ClassLoader.
//            	ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
//            	try {
//            		Thread.currentThread().setContextClassLoader(InSyncServiceProvider.get().getContextClassLoader(curr));
//	            	if (((UIComponent)curr.getInstance()).getRendersChildren()) {
//	            		parent = curr.getBeanParent();
//
//						// Can't break here - there could be an outer
//                        // renders-children parent
//	            	}
//            	} finally {
//            		Thread.currentThread().setContextClassLoader(oldContextClassLoader);
//            	}
//            }
//        }
//
//        return parent;
//    }

    // Moved to Util.
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

    /** Paste the beans in the given transferable to the given parent
     * and markup position.
     */
    public /*static*/ DesignBean[] pasteBeans(/*WebForm webform,*/ Transferable t, DesignBean parent,
    MarkupPosition pos, Point location, /*CoordinateTranslator coordinateTranslator,*/ UpdateSuspender updateSuspender) {
        // Make sure we're allowed to paste to the given parent.
        // Arguably, I should not be enabling the paste action when the selected parent
        // is "selected", but the parent used is computed very dynamically, so
        // doing something like this would require recomputing the Paste state
        // every pixel the mouse moves over the designer canvas. Instead we try
        // to move the parent up until we find a suitable parent.
        while (parent != null) {
//            DndHandler dndHandler = webform.getPane().getDndHandler();
            int allowed = computeActions(parent, t, false, DROP_CENTER);

            if ((allowed & DnDConstants.ACTION_COPY_OR_MOVE) != 0) {
                break;
            }

            parent = parent.getBeanParent();
            pos = null; // no longer valid - just use insync defaults
        }

        if (parent == null) {
            // XXX #110353 There is no rendered component corresponding to the fragment root?!
            // Hacking the problem here so it works like before.
            LiveUnit liveUnit = jsfForm.getLiveUnit();
            FacesPageUnit facesPageUnit = jsfForm.getFacesPageUnit();
            parent = liveUnit == null || facesPageUnit == null ? null : liveUnit.getDesignBean(facesPageUnit.getDefaultParent());
            
            if (parent == null) {
                // No valid parent found.
                Toolkit.getDefaultToolkit().beep();
                return null;
            }
        }

//        Document document = null;

        //LiveUnit unit = (LiveUnit)parent.getDesignContext();
//        LiveUnit unit = facesModel.getLiveUnit();
        LiveUnit unit = jsfForm.getLiveUnit();

        String description = NbBundle.getMessage(FacesDndSupport.class, "LBL_Paste"); // NOI18N
//        UndoEvent undoEvent = facesModel.writeLock(description);
        UndoEvent undoEvent = jsfForm.writeLock(description);
        try {
//            document = webform.getDocument();
//
//            //document.setAutoIgnore(true);
//            String description = NbBundle.getMessage(SelectionTopComp.class, "Paste"); // NOI18N
//            document.writeLock(description);

            DesignBean[] beans = unit.pasteBeans(t, parent, pos);

            if (beans == null) {
                return null;
            }

            // Decide whether we need to strip out position coordinates
            // from the beans being moved
            boolean needPos = true;

            if (parent != null) {
                needPos = isGridContext(parent, pos);

                if (!needPos) {
                    location = null;
                }
            }

            // Determine if the destination is a grid area
            if (location != null) {
//                // Snap
//                GridHandler gh = GridHandler.getInstance();
//
//                if (gh.snap()) {
//                    // TODO - compute the right target box here
////                    CssBox gridBox = null;
//                    location.x = gh.snapX(location.x, null);
//                    location.y = gh.snapY(location.y, null);
//                }
                // The location was snapped before.
//                if (coordinateTranslator != null) {
//                    location.x = coordinateTranslator.snapX(location.x);
//                    location.y = coordinateTranslator.snapY(location.y);
//                }

                // Position elements
                Point topLeft = getTopLeft(beans);

                for (int i = 0; i < beans.length; i++) {
                    if (!(beans[i] instanceof MarkupDesignBean)) {
                        continue;
                    }

                    MarkupDesignBean bean = (MarkupDesignBean)beans[i];

                    // XXX I need to do this on the -rendered- element!
                    Element element = bean.getElement();
                    assert element != null;

                    try {
//                        webform.getDomSynchronizer().setUpdatesSuspended(bean, true);
//                        webform.setUpdatesSuspended(bean, true);
                        if (updateSuspender != null) {
                            updateSuspender.setSuspended(bean, true);
                        }

                        if (!needPos) {
//                            XhtmlCssEngine engine = webform.getMarkup().getCssEngine();
                            List<StyleData> remove = new ArrayList<StyleData>(5);
                            remove.add(new StyleData(XhtmlCss.POSITION_INDEX));
                            remove.add(new StyleData(XhtmlCss.LEFT_INDEX));
                            remove.add(new StyleData(XhtmlCss.TOP_INDEX));
                            remove.add(new StyleData(XhtmlCss.RIGHT_INDEX));
                            remove.add(new StyleData(XhtmlCss.BOTTOM_INDEX));
// <removing design bean manipulation in engine>
//                            engine.updateLocalStyleValues((RaveElement)element, null, remove);
// ====
                            Util.updateLocalStyleValuesForElement(element,
                                    null, remove.toArray(new StyleData[remove.size()]));
// </removing design bean manipulation in engine>

                            continue;
                        }

                        List<StyleData> set = new ArrayList<StyleData>(5);
                        List<StyleData> remove = new ArrayList<StyleData>(5);
//                        Value val = CssLookup.getValue(element, XhtmlCss.POSITION_INDEX);
                        CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.POSITION_INDEX);

//                        if ((val == CssValueConstants.ABSOLUTE_VALUE) ||
//                                (val == CssValueConstants.RELATIVE_VALUE) ||
//                                (val == CssValueConstants.FIXED_VALUE)) {
                        if (CssProvider.getValueService().isAbsoluteValue(cssValue)
                        || CssProvider.getValueService().isRelativeValue(cssValue)
                        || CssProvider.getValueService().isFixedValue(cssValue)) {
//                            int top = CssLookup.getLength(element, XhtmlCss.TOP_INDEX);
//                            int left = CssLookup.getLength(element, XhtmlCss.LEFT_INDEX);
                            int top = CssProvider.getValueService().getCssLength(element, XhtmlCss.TOP_INDEX);
                            int left = CssProvider.getValueService().getCssLength(element, XhtmlCss.LEFT_INDEX);

                            if ((top != CssValue.AUTO) || (left != CssValue.AUTO)) {
                                if (left == CssValue.AUTO) {
                                    left = 0;
                                }

                                if (top == CssValue.AUTO) {
                                    top = 0;
                                }

                                left = (location.x + left) - topLeft.x;
                                top = (location.y + top) - topLeft.y;

                                set.add(new StyleData(XhtmlCss.TOP_INDEX,
                                        Integer.toString(top) + "px")); // NOI18N
                                set.add(new StyleData(XhtmlCss.LEFT_INDEX,
                                        Integer.toString(left) + "px")); // NOI18N
                            } else {
                                set.add(new StyleData(XhtmlCss.LEFT_INDEX,
                                        Integer.toString(location.x) + "px")); // NOI18N
                                set.add(new StyleData(XhtmlCss.TOP_INDEX,
                                        Integer.toString(location.y) + "px")); // NOI18N
                            }
                        } else {
                            set.add(new StyleData(XhtmlCss.POSITION_INDEX,
//                                    CssConstants.CSS_ABSOLUTE_VALUE)); // NOI18N
                                    CssProvider.getValueService().getAbsoluteValue()));
                            set.add(new StyleData(XhtmlCss.LEFT_INDEX,
                                    Integer.toString(location.x) + "px")); // NOI18N
                            set.add(new StyleData(XhtmlCss.TOP_INDEX,
                                    Integer.toString(location.y) + "px")); // NOI18N
                        }

                        remove.add(new StyleData(XhtmlCss.RIGHT_INDEX));
                        remove.add(new StyleData(XhtmlCss.BOTTOM_INDEX));

//                        XhtmlCssEngine engine = webform.getMarkup().getCssEngine();
// <removing design bean manipulation in engine>
//                        engine.updateLocalStyleValues((RaveElement)element, set, remove);
// ====
                        Util.updateLocalStyleValuesForElement(element,
                                set.toArray(new StyleData[set.size()]),
                                remove.toArray(new StyleData[remove.size()]));
// </removing design bean manipulation in engine>
                    } finally {
//                        webform.getDomSynchronizer().setUpdatesSuspended(bean, false);
//                        webform.setUpdatesSuspended(bean, false);
                        if (updateSuspender != null) {
                            updateSuspender.setSuspended(bean, false);
                        }
                    }
                }
            } else if (needPos) {
                // We're over a grid area but don't have a specified position;
                // leave existing positions in the pasted components alone
                // but don't create new positions to assign to other components.
                // This means that if you cut a component and then paste it
                // it will appear in the place it was before cutting it.
            } else {
                // Flow area: remove absolute positions for all children
                for (int i = 0; i < beans.length; i++) {
                    if (!(beans[i] instanceof MarkupDesignBean)) {
                        // Not a visual component
                        continue;
                    }

                    MarkupDesignBean bean = (MarkupDesignBean)beans[i];
                    Element element = bean.getElement();

                    try {
//                        webform.getDomSynchronizer().setUpdatesSuspended(bean, true);
//                        webform.setUpdatesSuspended(bean, true);
                        if (updateSuspender != null) {
                            updateSuspender.setSuspended(bean, true);
                        }
                        
//                        CssLookup.removeLocalStyleValue(element, XhtmlCss.POSITION_INDEX);
//                        CssLookup.removeLocalStyleValue(element, XhtmlCss.LEFT_INDEX);
//                        CssLookup.removeLocalStyleValue(element, XhtmlCss.TOP_INDEX);
                        Util.removeLocalStyleValueForElement(element, XhtmlCss.POSITION_INDEX);
                        Util.removeLocalStyleValueForElement(element, XhtmlCss.LEFT_INDEX);
                        Util.removeLocalStyleValueForElement(element, XhtmlCss.TOP_INDEX);
                    } finally {
//                        webform.getDomSynchronizer().setUpdatesSuspended(bean, false);
//                        webform.setUpdatesSuspended(bean, false);
                        if (updateSuspender != null) {
                            updateSuspender.setSuspended(bean, false);
                        }
                    }
                }
            }

            return beans;
        } finally {
//            document.writeUnlock();
//            facesModel.writeUnlock(undoEvent);
            jsfForm.writeUnlock(undoEvent);
        }
    }

    /** Compute the leftmost and topmost positions among the given beans
     */
    private static Point getTopLeft(DesignBean[] beans) {
        int minLeft = Integer.MAX_VALUE;
        int minTop = Integer.MAX_VALUE;

        for (int i = 0; i < beans.length; i++) {
            Element element = Util.getElement(beans[i]);

            if (element == null) {
                // Not a visual component
                continue;
            }

//            Value val = CssLookup.getValue(element, XhtmlCss.POSITION_INDEX);
            CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.POSITION_INDEX);

//            if ((val == CssValueConstants.ABSOLUTE_VALUE) ||
//                    (val == CssValueConstants.RELATIVE_VALUE) ||
//                    (val == CssValueConstants.FIXED_VALUE)) {
            if (CssProvider.getValueService().isAbsoluteValue(cssValue)
            || CssProvider.getValueService().isRelativeValue(cssValue)
            || CssProvider.getValueService().isFixedValue(cssValue)) {
//                int top = CssLookup.getLength(element, XhtmlCss.TOP_INDEX);
//                int left = CssLookup.getLength(element, XhtmlCss.LEFT_INDEX);
                int top = CssProvider.getValueService().getCssLength(element, XhtmlCss.TOP_INDEX);
                int left = CssProvider.getValueService().getCssLength(element, XhtmlCss.LEFT_INDEX);

                if ((top != CssValue.AUTO) || (left != CssValue.AUTO)) {
                    if (left == CssValue.AUTO) {
                        left = 0;
                    }

                    if (top == CssValue.AUTO) {
                        top = 0;
                    }

                    if (top < minTop) {
                        minTop = top;
                    }

                    if (left < minLeft) {
                        minLeft = left;
                    }
                }
            }
        }

        return new Point(minLeft, minTop);
    }
    
    /**
     * Report whether the given position is in grid context
     */
    private static boolean isGridContext(DesignBean parent, MarkupPosition pos) {
        if (parent.getInstance() instanceof javax.faces.component.UIForm
        || parent.getInstance() instanceof org.netbeans.modules.visualweb.xhtml.Form) {
            // Look at its parent
            parent = parent.getBeanParent();

            if (parent == null) {
                return false;
            }
        }

        Element element = Util.getElement(parent);

        if (element == null) {
            return false;
        }

//        Value val = CssLookup.getValue(element, XhtmlCss.RAVELAYOUT_INDEX);
        CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(element, XhtmlCss.RAVELAYOUT_INDEX);

//        return val == CssValueConstants.GRID_VALUE;
        return CssProvider.getValueService().isGridValue(cssValue);
    }

//    /** XXX Provides the auto value as <code>AUTO</code>, revise that, it looks very dangerous. */
//    private static int getCssLength(Element element, int property) {
////        Value val = getValue(element, property);
//        CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(element, property);
////        if (val == CssValueConstants.AUTO_VALUE) {
//        if (CssProvider.getValueService().isAutoValue(cssValue)) {
//            return CSS_AUTO;
//        }
//        
////        return (int)val.getFloatValue();
//        return (int)cssValue.getFloatValue();
//    }

    // Moved to Util.
//    /**
//     * Return true iff the given DesignBean is the form bean for this form OR THE LIVE BEAN CONTAINER,
//     * since it acts like the form bean in many ways (not draggable, not deletable, etc.)
//     */
//    private static boolean isFormBean(FacesModel facesModel, DesignBean bean) {
////        FacesModel model = webform.getModel();
//
//        if (bean == facesModel.getRootBean()) {
//            return true;
//        }
//
//        FacesPageUnit facesUnit = facesModel.getFacesUnit();
//        MarkupBean formBean = facesUnit.getDefaultParent();
//
//        return getFacesBean(bean) == formBean;
//    }
//    
//    /**
//     * Return the FacesBean for the live bean. May be null, for non faces live beans.
//     *
//     * @param lb The live bean to get the faces bean for. May be null.
//     * @return the FacesBean corresponding to the live bean, or null.
//     */
//    private static FacesBean getFacesBean(DesignBean lb) {
//        if (!(lb instanceof BeansDesignBean)) {
//            return null;
//        }
//
//        Bean b = ((BeansDesignBean)lb).getBean();
//
//        if (b instanceof FacesBean) {
//            return (FacesBean)b;
//        }
//
//        return null;
//    }

    // Moved to Util.
//    /**
//     * Return true iff the given DesignBean is the body bean, or the form bean, OR THE LIVE BEAN
//     * CONTAINER. These beans have special behavior since they are not draggable, not deletable,
//     * etc.
//     * TODO Move this into insync.
//     * @todo Prevent deletion of f:subview in page fragments!
//     */
//    public static boolean isSpecialBean(/*WebForm webform,*/ DesignBean bean) {
////        FacesModel model = webform.getModel();
//        if (bean == null) {
//            // XXX Log NPE?
//            return false;
//        }
//        DesignContext context = bean.getDesignContext();
//        // XXX Casting is error-prone.
//        FacesModel model = ((LiveUnit)context).getModel();
//        
//        if (bean == model.getRootBean()) {
//            return true;
//        }
//
//        FacesPageUnit facesUnit = model.getFacesUnit();
//
//        if (facesUnit != null) {
//            MarkupBean formBean = facesUnit.getDefaultParent();
//            MarkupBean markup = getMarkupBean(bean);
//
//            if (markup == null) {
//                return false;
//            }
//
//            if (markup == formBean) {
//                return true;
//            }
//
////            RaveElement e = (RaveElement)markup.getElement();
////            if (e.getRendered() != null) {
////                e = (RaveElement)e.getRendered();
//            Element e = markup.getElement();
//            Element rendered = MarkupService.getRenderedElementForElement(e);
//            if (rendered != null) {
//                e = rendered;
//
//                // Anything from the body or up is special -- cannot be removed
////                Node curr = webform.getBody();
//                Node curr = InSyncServiceProvider.get().getHtmlBodyForMarkupFile(model.getMarkupFile());
//
//                while (curr != null) {
//                    if (curr == e) {
//                        return true;
//                    }
//
//                    curr = curr.getParentNode();
//                }
//            } else {
//                // Anything from the body or up is special -- cannot be removed
////                Node curr = webform.getBody().getSource();
//                Element body = InSyncServiceProvider.get().getHtmlBodyForMarkupFile(model.getMarkupFile());
//                Node curr = MarkupService.getSourceElementForElement(body);
//
//                while (curr != null) {
//                    if (curr == e) {
//                        return true;
//                    }
//
//                    curr = curr.getParentNode();
//                }
//            }
//        }
//
//        return false;
//    }

    // Moved to Util.
//    /**
//     * Return the MarkupBean for the live bean. May be null, for non markup live beans.
//     *
//     * @param lb The live bean to get the faces bean for. May be null.
//     * @return the MarkupBean corresponding to the live bean, or null.
//     */
//    public static MarkupBean getMarkupBean(DesignBean lb) {
//        if (!(lb instanceof BeansDesignBean)) {
//            return null;
//        }
//
//        Bean b = ((BeansDesignBean)lb).getBean();
//
//        if (b instanceof MarkupBean) {
//            return (MarkupBean)b;
//        }
//
//        return null;
//    }
    
    
    /**
     * Return the parent element of the given element. Takes DocumentFragments
     * etc. into consideration. Returns null when there is no such parent.
     */
    private static Element getParent(Element element) {
//        if (element.getStyleParent() != null) {
//            return (RaveElement)element.getStyleParent();
//        if (element instanceof CSSEngine.StyleElementLink
//        && ((CSSEngine.StyleElementLink)element).getStyleParent() != null) {
//            return (RaveElement)((CSSEngine.StyleElementLink)element).getStyleParent();
        Element styleParent = CssProvider.getEngineService().getStyleParentForElement(element);
        if (styleParent != null) {
            return styleParent;
        } else if ((element.getParentNode() != null) &&
                (element.getParentNode().getNodeType() == Node.ELEMENT_NODE)) {
            return (Element)element.getParentNode();
        } else {
            return null;
        }
    }
    
    private /*public*/ static Location computeLocationForPositions(String facet, Point canvasPos, Node documentPosNode, int documentPosOffset,
    Dimension dropSize, boolean isGrid, Element droppeeElement, DesignBean droppeeBean, /*WebForm webform*/DesignBean defaultParentBean) {
        LocationImpl location = new LocationImpl();
        location.facet = facet;
        location.coordinates = canvasPos;
//        location.size = getDropSize();
        location.size = dropSize;

        DesignBean parent = null;
        Node under = null;
        Node before = null;

        Element element = null;

        if (documentPosNode != null) {
            // XXX TODO: split text nodes!
            if (documentPosNode instanceof Text) {
                if (documentPosOffset == 0) {
                    before = documentPosNode;
                    under = before.getParentNode();
                } else {
                    Text txt = (Text)documentPosNode;

                    if (documentPosOffset < txt.getLength()) {
                        before = txt.splitText(documentPosOffset);
                        under = before.getParentNode();
                    } else {
                        before = txt.getNextSibling();
                        under = txt.getParentNode();
                    }
                }
            } else {
                int offset = documentPosOffset;

                if (offset < documentPosNode.getChildNodes().getLength()) {
                    under = documentPosNode;
                    before = under.getChildNodes().item(offset);
                } else {
                    // Just append - but we don't have an api for that (can only set "before")
                    // so create a new blank text node as a hook
                    // XXX should I really mutate the document here?
                    // That doesn't sound right...
                    under = documentPosNode;

                    // XXX Manipulation of the doc may not be done here.
//                        Text txt = webform.getJspDom().createTextNode(" "); // NOI18N
                    Text txt = under.getOwnerDocument().createTextNode(" "); // NOI18N
                    under.appendChild(txt);
                    before = txt;
                }
            }

            if (parent == null) {
                Node n = under;

//                    while (n instanceof RaveElement) {
//                        RaveElement xel = (RaveElement)n;
                while (n instanceof Element) {
                    Element xel = (Element)n;

//                        if (xel.getDesignBean() != null) {
//                            DesignBean lbean = (DesignBean)xel.getDesignBean();
//                    DesignBean lbean = InSyncService.getProvider().getMarkupDesignBeanForElement(xel);
                    DesignBean lbean = MarkupUnit.getMarkupDesignBeanForElement(xel);
                    if (lbean != null) {
                        if (lbean.isContainer()) {
                            parent = lbean;

                            break;
                        }
                    }

                    n = n.getParentNode();
                }
            }

            //!CQ: facesUnit.setInsertBefore(before);
            // determine the integer offset of the before node within its parent
            if ((under != null) && (under.getNodeType() == Node.ELEMENT_NODE)) {
                location.droppeeElement = (Element)under;
            }
        } else if (canvasPos != null) {
            // What position should we assign here??? For now, nothing.
            // Let insync pick a position. The exact location in the source
            // where the tag is inserted isn't very important since we're
            // absolute positioning anyway.
            under = null;
            before = null;

//                CssBox box = webform.getMapper().findBox(canvasPos.x, canvasPos.y);
//                CssBox box = ModelViewMapper.findBox(webform.getPane().getPageBox(), canvasPos.x, canvasPos.y);
//
//                // In flow mode, don't do absolute positioning, ever, unless we're in
//                // a grid positioning area
//                if (!webform.isGridMode() && ((box == null) || !box.isGrid())) {
//                    location.coordinates = null;
//                }
//
//                location.droppeeElement = box.getElement();
//                parent = getDroppee(box);
            if (!isGrid) {
                location.coordinates = null;
            }
            location.droppeeElement = droppeeElement;
            parent = droppeeBean;
        }

        //            else {
        //                // No position specified. In this case send just nulls
        //                // to insync and let insync figure it out. It will insert
        //                // the component most likely as a child of the form component.
        //            }

        location.droppee = parent;

        // If default-positioning, try to place the component before the <br/>, if
        // the the br is the last element under the default parent.
        if ((under == null) && (before == null)) {
            if (parent == null) {
//                parent = webform.getDefaultParentBean();
                parent = defaultParentBean;
            }
            location.pos = getDefaultMarkupPositionUnderParent(parent/*, webform*/);
        } else {
            location.pos = new MarkupPosition(under, before);
        }

        return location;
        
    }

    public void importString(Designer designer, String string, Point canvasPos, Node documentPosNode, int documentPosOffset, Dimension dropSize, boolean isGrid,
    Element droppeeElement, DesignBean droppeeBean, DesignBean defaultParent, /*CoordinateTranslator coordinateTranslator,*/ UpdateSuspender updateSuspender) {
        Location location = computeLocationForPositions(null, canvasPos, documentPosNode, documentPosOffset, dropSize, isGrid, droppeeElement, droppeeBean, defaultParent);
        importString(designer, string, location, /*coordinateTranslator,*/ updateSuspender);
    }

    public boolean importData(Designer designer, JComponent comp, Transferable t, /*Object transferData,*/ Point canvasPos, Node documentPosNode, int documentPosOffset, Dimension dropSize, boolean isGrid,
    Element droppeeElement, DesignBean droppeeBean, DesignBean defaultParent, /*CoordinateTranslator coordinateTranslator,*/ UpdateSuspender updateSuspender, int dropAction) {
        Location location = computeLocationForPositions(null, canvasPos, documentPosNode, documentPosOffset, dropSize, isGrid, droppeeElement, droppeeBean, defaultParent);
        return importData(designer, comp, t, /*transferData,*/ dropSize, location, /*coordinateTranslator,*/ updateSuspender, dropAction);
    }

    private static boolean isValidTransferData(Transferable t, Object transferData) {
        DataFlavor importFlavor = getImportFlavor(t.getTransferDataFlavors());
        if (importFlavor == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, 
                    new IllegalStateException("Unusable transfer flavors " + Arrays.asList(t.getTransferDataFlavors()))); // NOI18N

            return false;
        }
        
        Class<?> rc = importFlavor.getRepresentationClass();
        try {
            if (rc == DisplayItem.class) {
                // Create a new type
                transferData = t.getTransferData(importFlavor);

                if (!(transferData instanceof DisplayItem)) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, 
                            new IllegalStateException("Invalid transfer data=" + transferData));

                    return false;
                }
            } else if (rc == DesignBean.class) {
                transferData = t.getTransferData(importFlavor);

                if (!(transferData instanceof DesignBean[])) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, 
                            new IllegalStateException("Invalid transfer data=" + transferData));

                    return false;
                }
            } else if (rc.isAssignableFrom(List.class)) {
                transferData = t.getTransferData(importFlavor);

                if (!(transferData instanceof List)) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, 
                            new IllegalStateException("Invalid transfer data=" + transferData));

                    return false;
                }
            } else if (rc.isAssignableFrom(org.openide.nodes.Node.class)) {
                transferData = t.getTransferData(importFlavor);

                if (!(transferData instanceof org.openide.nodes.Node)) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, 
                            new IllegalStateException("Invalid transfer data=" + transferData));

                    return false;
                }
            }
        } catch (UnsupportedFlavorException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return false;
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return false;
        }
        
        return true;
    }

    
    public interface Location {
        public DesignBean getDroppee();
        public String getFacet();
        public Element getDroppeeElement();
        public MarkupPosition getPos();
        public Point getCoordinates();
        public Dimension getSize();
    } // End of Location.

    
    private static class LocationImpl implements Location {
        private DesignBean droppee;
        private String facet;
        private Element droppeeElement;
        private MarkupPosition pos;
        private Point coordinates;
        private Dimension size;
        
        public LocationImpl() {}
        
        public LocationImpl(Location location) {
            this.droppee        = location.getDroppee();
            this.facet          = location.getFacet();
            this.droppeeElement = location.getDroppeeElement();
            this.pos            = location.getPos();
            this.coordinates    = location.getCoordinates();
            this.size           = location.getSize();
        }
        
        public DesignBean getDroppee() {
            return droppee;
        }
        public String getFacet() {
            return facet;
        }
        public Element getDroppeeElement() {
            return droppeeElement;
        }
        public MarkupPosition getPos() {
            return pos;
        }
        public Point getCoordinates() {
            return coordinates;
        }
        public Dimension getSize() {
            return size;
        }
    } // End of LocationImpl.
    
    
//    public interface CoordinateTranslator {
//        public Point translateCoordinates(Element parent, int x, int y);
//        public int snapX(int x);
//        public int snapY(int y);
//    } // End of CoordinateTranslator.
    
    public interface UpdateSuspender {
        public void setSuspended(MarkupDesignBean markupDesignBean, boolean suspend);
    } // End of UpdateSuspender.
    
    public static class DropInfo {
        private MarkupDesignBean markupDesignBean;
//        private MarkupMouseRegion markupMouseRegion;
        private Element regionElement;
        private int dropType;
//        public DropInfo(MarkupDesignBean markupDesignBean, MarkupMouseRegion markupMouseRegion, int dropType) {
        public DropInfo(MarkupDesignBean markupDesignBean, Element regionElement, int dropType) {
            this.markupDesignBean = markupDesignBean;
//            this.markupMouseRegion = markupMouseRegion;
            this.regionElement = regionElement;
            this.dropType = dropType;
        }
        public MarkupDesignBean getMarkupDesignBean() {
            return markupDesignBean;
        }
//        public MarkupMouseRegion getMarkupMouseRegion() {
//            return markupMouseRegion;
//        }
        public Element getRegionElement() {
            return regionElement;
        }
        public int getDropType() {
            return dropType;
        }
    } // End of DropInfo.

    
    private boolean importComponentData(Designer designer, JComponent comp, Transferable t, Point dropPoint) {
//        JsfMultiViewElement jsfMultiViewElement = JsfForm.findJsfMultiViewElementForDesigner(designer);
//        if (jsfMultiViewElement == null) {
//            return false;
//        }
        
        JsfTopComponent jsfTopComponent;

        if (comp instanceof JsfTopComponent) {
            jsfTopComponent = (JsfTopComponent)comp;
        } else {
            jsfTopComponent = (JsfTopComponent)SwingUtilities.getAncestorOfClass(JsfTopComponent.class, comp);
        }

        if (jsfTopComponent == null) {
            // XXX
            return false;
        }

        // XXX This shouldn't be needed here again (it was already processed before - see call stack).
//                DesignBean parent = selectionTopComp.getPasteParent();
        Element parentComponentRootElement = jsfTopComponent.getPasteParentComponent();
//                MarkupPosition pos = selectionTopComp.getPasteMarkupPosition();
//        Point location = jsfTopComponent.getPastePosition();
//        Point location = designer.getPastePoint();
        Point location = dropPoint;
        
//                DesignBean[] beans = selectionTopComp.pasteBeans(webform, t, parent, pos, location);
//                Element[] componentRootElements = SelectionTopComp.pasteComponents(webform, t, parentComponentRootElement, location);

        if (location != null) {
//            GridHandler gridHandler = webform.getGridHandler();
//            location.x = gridHandler.snapX(location.x);
//            location.y = gridHandler.snapY(location.y);
            location.x = designer.snapX(location.x, null);
            location.y = designer.snapY(location.y, null);
        }
//        Element[] componentRootElements = webform.pasteComponents(t, parentComponentRootElement, location);
//        Element[] componentRootElements = pasteComponents(t, parentComponentRootElement, location, jsfForm.getUpdateSuspender());
        pasteComponents(jsfTopComponent, t, parentComponentRootElement, location, jsfForm.getUpdateSuspender());

////                if ((beans != null) && (beans.length > 0)) {
////                    selectionTopComp.selectBeans(beans);
////                }
//        if (componentRootElements.length > 0) {
////            selectionTopComp.selectComponents(componentRootElements);
//            jsfTopComponent.selectComponents(componentRootElements);
//        }
        return true;
        
    }

    private /*public Element[]*/ void pasteComponents(final JsfTopComponent jsfTopComponent, Transferable t, Element parentComponentRootElement, Point location, UpdateSuspender updateSuspender) {
        MarkupDesignBean parent = MarkupUnit.getMarkupDesignBeanForElement(parentComponentRootElement);
        final DesignBean[] designBeans = pasteBeans(t, parent, null, location, /*jsfForm.getUpdateSuspender()*/updateSuspender);
        
        if (designBeans == null) {
//            return new Element[0];
            return;
        }

        // XXX #105306 It needs to be invoked later (see also fireSelectedDesignBean);
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                List<Element> elements = new ArrayList<Element>();
                for (DesignBean designBean : designBeans) {
                    if (designBean instanceof MarkupDesignBean) {
                        Element element = JsfSupportUtilities.getComponentRootElementForMarkupDesignBean((MarkupDesignBean)designBean);
                        if (element != null) {
                            elements.add(element);
                        }
                    }
                }
                if (elements.size() > 0) {
                    jsfTopComponent.selectComponents(elements.toArray(new Element[elements.size()]));
                }
            }
        });
//        if (designBeans.length > 0) {
//            fireSelectedDesignBeanChanged(designBeans);
//        }
    }

    
    private static Logger getLogger() {
        return Logger.getLogger(FacesDndSupport.class.getName());
    }
    
    private static void info(Exception ex) {
        getLogger().log(Level.INFO, null, ex);
    }
}
