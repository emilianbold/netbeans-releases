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

package org.netbeans.modules.visualweb.designer.jsf;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DisplayItem;
import com.sun.rave.designtime.Position;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import com.sun.rave.designtime.markup.MarkupPosition;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.visualweb.api.designer.Designer;
import org.netbeans.modules.visualweb.api.designer.DomProvider;
import org.netbeans.modules.visualweb.api.designerapi.DesignTimeTransferDataCreator;
import org.netbeans.modules.visualweb.api.designtime.idebridge.DesigntimeIdeBridgeProvider;
import org.netbeans.modules.visualweb.insync.Util;
import org.netbeans.modules.visualweb.insync.live.LiveUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.netbeans.modules.visualweb.outline.api.OutlineSelector;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.w3c.dom.Element;
import static org.netbeans.modules.visualweb.api.designer.DomProvider.*;
import org.w3c.dom.Node;

/**
 * Place for JSF specific DnD support.
 * Factored out the complicated stuff from the designer/DnDHandler.
 * 
 * XXX TODO Merge with FacesDndSupport.
 * XXX TODO Separate FacesModel.JsfSupport impl.
 *
 * @author Peter Zavadsky
 * @author Tor Norbye (the original moved code)
 */
class DndSupport implements /*XXX*/FacesModel.JsfSupport {
    
    private final JsfForm jsfForm;
    /** XXX TEMP solution. Listener on DnD changes. */
    private PropertyChangeListener dndListener;
    
//    private MarkupDesignBean recentDropTarget;
    
    private final FacesDndSupport facesDndSupport;
    
    
    /** Creates a new instance of DndSupport */
    public DndSupport(JsfForm jsfForm) {
        this.jsfForm = jsfForm;
        this.facesDndSupport = new FacesDndSupport(jsfForm);
    }

    
//    private FacesModel getFacesModel() {
//        return jsfForm.getFacesModel();
//    }
    
    DataFlavor getImportFlavor(DataFlavor[] flavors) {
        return FacesDndSupport.getImportFlavor(flavors);
    }

//    MarkupPosition getDefaultMarkupPositionUnderParent(DesignBean parent) {
//        return FacesDndSupport.getDefaultMarkupPositionUnderParent(parent, getFacesModel());
//    }


    String[] getClassNames(DisplayItem[] displayItems) {
//        return getFacesModel().getDnDSupport().getClasses(displayItems);
        return facesDndSupport.getClasses(displayItems);
    }
    
//    boolean importBean(DisplayItem[] items, DesignBean origParent, int nodePos,
//            String facet, List createdBeans, DomProvider.Location location,
//            DomProvider.CoordinateTranslator coordinateTranslator) throws IOException {
//        return getFacesModel().getDnDSupport().importBean(items, origParent, nodePos, facet, createdBeans,
//                new LocationImpl(location), new CoordinateTranslatorImpl(coordinateTranslator), jsfForm.getUpdateSuspender());
//    }

//    void importData(JComponent comp, Transferable t, Object transferData, Dimension dimension,
//            DomProvider.Location location, DomProvider.CoordinateTranslator coordinateTranslator, int dropAction) {
//        getFacesModel().getDnDSupport().importData(comp, t, transferData, dimension, new LocationImpl(location),
//                new CoordinateTranslatorImpl(coordinateTranslator), jsfForm.getUpdateSuspender(), dropAction);
//    }
    
//    void importString(String string, DomProvider.Location location, DomProvider.CoordinateTranslator coordinateTranslator) {
//        getFacesModel().getDnDSupport().importString(string, new LocationImpl(location), new CoordinateTranslatorImpl(coordinateTranslator), jsfForm.getUpdateSuspender());
//    }
    

    DesignBean[] pasteBeans(Transferable t, DesignBean parent, MarkupPosition pos, Point location, FacesDndSupport.UpdateSuspender updateSuspender) {
//        return getFacesModel().getDnDSupport().pasteBeans(t, parent, pos, location, jsfForm.getUpdateSuspender());
        return facesDndSupport.pasteBeans(t, parent, pos, location, jsfForm.getUpdateSuspender());
    }

    int computeActions(DesignBean droppee, Transferable transferable) {
//        return getFacesModel().getDnDSupport().computeActions(droppee, transferable);
        return facesDndSupport.computeActions(droppee, transferable);
    }

    int processLinks(Element origElement, List<DesignBean> beans) {
        return processLinks(origElement, null, beans, true, true, false);
    }
    
    private int processLinks(Element origElement, Class[] classes, List<DesignBean> beans, boolean selectFirst, boolean handleLinks, boolean showLinkTarget) {
//        return getFacesModel().getDnDSupport().processLinks(origElement, classes, beans, selectFirst, handleLinks, showLinkTarget, jsfForm.getUpdateSuspender());
        return facesDndSupport.processLinks(origElement, classes, beans, selectFirst, handleLinks, showLinkTarget, jsfForm.getUpdateSuspender());
    }

    void updateDndListening() {
        dndListener = new DnDListener(jsfForm);
        // XXX Listening on dnd support, it should be on model.
//        getFacesModel().getDnDSupport().addPropertyChangeListener(WeakListeners.propertyChange(dndListener, getFacesModel().getDnDSupport()));
        facesDndSupport.addPropertyChangeListener(WeakListeners.propertyChange(dndListener, facesDndSupport));
    }

    int getDropType(DesignBean origDroppee, Element droppeeElement, Transferable t, boolean linkOnly) {
        DataFlavor importFlavor = getImportFlavor(t.getTransferDataFlavors());

        if (importFlavor == null) {
            DataFlavor[] flavors = t.getTransferDataFlavors();
            ErrorManager.getDefault().log("Unusable transfer, data flavors="
                    + (flavors == null ? null : java.util.Arrays.asList(t.getTransferDataFlavors()))); // NOI18N

            return DROP_DENIED;
        }

        Class rc = importFlavor.getRepresentationClass();

        if (rc == DisplayItem.class) {
            // Create a new type
            try {
                Object transferData = t.getTransferData(importFlavor);

                if (!(transferData instanceof DisplayItem)) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, 
                            new IllegalStateException("Invalid transferData=" + transferData // NOI18N
                            + ", from transferable=" + t)); // NOI18N
                    return DROP_DENIED;
                }

                DisplayItem item = (DisplayItem)transferData;

                return getDropTypeForDisplayItem(origDroppee, droppeeElement, item, linkOnly);
            } catch (UnsupportedFlavorException ex) {
                ErrorManager.getDefault().notify(ex);

                return DROP_DENIED;
            } catch (java.io.IOException ex) {
                ErrorManager.getDefault().notify(ex);

                return DROP_DENIED;
            }
        } else if (rc == DesignBean.class) {
            try {
                Object transferData = t.getTransferData(importFlavor);

                if (!(transferData instanceof DesignBean[])) {
                    ErrorManager.getDefault().log("Invalid DesignBean[] transfer data: " +
                        transferData);

                    return DROP_DENIED;
                }

                DesignBean[] beans = (DesignBean[])transferData;

                String[] classNames = new String[beans.length];

                for (int i = 0, n = beans.length; i < n; i++) {
                    classNames[i] = beans[i].getInstance().getClass().getName();
                }

                return getDropTypeForClassNames(origDroppee, droppeeElement, classNames, null, linkOnly);
            } catch (UnsupportedFlavorException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                return DROP_DENIED;
            } catch (java.io.IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                return DROP_DENIED;
            }
        }
        
        // XXX TEMP First give the chance to the provider.
        // FIXME This shouldn't be here at all, the original transferable
        // should contain all the needed flavors.
        DesignTimeTransferDataCreator dataCreator = (DesignTimeTransferDataCreator)Lookup.getDefault().lookup(DesignTimeTransferDataCreator.class);
        if (dataCreator != null) {
            DisplayItem displayItem = dataCreator.getDisplayItem(t);
            if (displayItem != null) {
                return getDropTypeForDisplayItem(origDroppee, droppeeElement, displayItem, linkOnly);
            }
        }
        
        // XXX The other hacked transferables.
        if (rc == String.class/*Linux*/ || rc == List.class/*Windows/Solaris*/) {
            if (rc == List.class) {
                // XXX #99457 There needs to be more fine grained decision.
                try {
                    java.util.List list = (java.util.List) t.getTransferData(importFlavor);
                    // XXX #135094 Possible NPE.
                    if (list == null) {
                        return DROP_DENIED;
                    }
                    for (Object element : list) {
                        if (element instanceof File) {
                            File file = (File)element;
                            // XXX Copy also in FacesDndSupport. 
                            if (file.exists()) {
                                String name = file.getName();
                                String extension = name.substring(name.lastIndexOf(".") + 1); // NOI18N
                    //            Project project = facesModel.getProject();
                                Project project = jsfForm.getProject();

                                // XXX #95601 Skip the file if it is already inside the project.
                                if (FileOwnerQuery.getOwner(file.toURI()) == project) {
                    //                return panel;
                                    return DROP_DENIED;
                                }

                                //String mime = FileUtil.getMIMEType(extension);
                                // They've only registered gif and jpg so not a big deal
                                if (FacesDndSupport.isImage(extension)) {
                    //                Location location =
                    //                    computePositions(null, DROP_CENTER, null, getDropPoint(), insertPos, true);
                    //                return panel;
                                    return DROP_PARENTED;
                                } else if (FacesDndSupport.isStylesheet(extension)) {
                    //                return panel;
                                    return DROP_PARENTED;
                                }
                                
                                // XXX TODO Also missing how to check whether Importable.PageImportable can do the import.
                            }
                        }
                    }
                }
                catch (UnsupportedFlavorException ex) {
                    log(ex);
                    return DROP_DENIED;
                }
                catch (IOException ex) {
                    log(ex);
                    return DROP_DENIED;
                }
            } else if (rc == String.class) {
                // TODO Also more fine grained decision.
                return DROP_PARENTED;
            }
            return DROP_DENIED;
        } else if (rc == org.openide.nodes.Node.class) {
            // XXX #6482097 Reflecting the impl in FacesDnDSupport.
            // FIXME Later the impl has to be improved and moved over there.
            Object transferData;
            try {
                transferData = t.getTransferData(importFlavor);
                if (transferData instanceof org.openide.nodes.Node) {
                    org.openide.nodes.Node node = (org.openide.nodes.Node)transferData;
                    DataObject dobj = (DataObject)node.getCookie(DataObject.class);

                    if (dobj != null) {
                        FileObject fo = dobj.getPrimaryFile();
//                        if (isImage(fo.getExt())) {
                        if (FacesDndSupport.isImage(fo.getExt())) {
//                            String className;
//                            // XXX This should be decided by the parent bean.
//                            // I.e. appropriate api is missing.
//                            // XXX This shouldn't be here resolved, but in the parent bean.
//                            if (webform.isBraveheartPage()) {
//                                className = com.sun.rave.web.ui.component.ImageComponent.class.getName(); // NOI18N
//                            } else if (webform.isWoodstockPage()) {
//                                // Use woodstock ImageComponent component
//                                className = com.sun.webui.jsf.component.ImageComponent.class.getName(); // NOI18N
//                            } else {
//                                className = javax.faces.component.html.HtmlGraphicImage.class.getName(); // NOI18N
//                            }
                            
//                            String className = webform.getImageComponentClassName();
                            String className = getImageComponentClassName();
                            
                            String[] classNames = new String[] {className};

                            return getDropTypeForClassNames(origDroppee, droppeeElement, classNames, null, linkOnly);
//                        } else if (isStylesheet(fo.getExt())) {
                        } else if (FacesDndSupport.isStylesheet(fo.getExt())) {
                            return DROP_PARENTED;
                        }
                    }
                }
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            } catch (UnsupportedFlavorException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
            return DROP_DENIED;
        }

        return DROP_DENIED;
    }
    
    private String getImageComponentClassName() {
        // XXX This should be decided by the parent bean.
        // I.e. appropriate api is missing.
        // XXX This shouldn't be here resolved, but in the parent bean.
//        if (jsfForm.getDomProvider().isBraveheartPage()) {
        if (jsfForm.isBraveheartPage()) {
            return com.sun.rave.web.ui.component.ImageComponent.class.getName(); // NOI18N
//        } else if (jsfForm.getDomProvider().isWoodstockPage()) {
        } else if (jsfForm.isWoodstockPage()) {
            // Use woodstock ImageComponent component
            return com.sun.webui.jsf.component.ImageComponent.class.getName(); // NOI18N
        } else {
            return javax.faces.component.html.HtmlGraphicImage.class.getName(); // NOI18N
        }
    }
    
    /**
     * Decide whether or not we can drop the given palette item
     * at the given position.
     * XXX TODO get rid of this method from the designer, it is JSF specific..
     */
//    private int getDropTypeForDisplayItem(Point p, DisplayItem item, boolean linkOnly) {
    private int getDropTypeForDisplayItem(DesignBean origDroppee, Element droppeeElement, DisplayItem item, boolean linkOnly) {
        if(item == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, 
                    new NullPointerException("Item is null")); // NOI18N
            return DROP_DENIED;
        } 
        
//        String[] classNames = getClasses(new DisplayItem[] { item });
//        String[] classNames = webform.getClassNames(new DisplayItem[] {item});
        String[] classNames = getClassNames(new DisplayItem[] {item});

        return getDropTypeForClassNames(origDroppee, droppeeElement, classNames, null, linkOnly);
    }

    /**
     * Decide whether or not we can drop the given palette item at the given position.
    * @todo implement using computeActions and computePosition instead of custom solution here... e.g.
     <pre>
    public int getDropType(Point p, String[] classNames, boolean linkOnly) {
        int allowed = computeActions(dropNode, t, false, nodePos);
        if (allowed == DnDConstants.ACTION_NONE) {
            return;
        }
        if (dropAction == DnDConstants.ACTION_COPY) {
        ... XXX call computeActions
    }
     </pre>
     * XXX TODO get rid of this method from the designer, it is JSF specific.
    */
    int getDropTypeForClassNames(DesignBean origDroppee, Element droppeeElement, String[] classNames, DesignBean[] beans, boolean linkOnly) {
//        if(DesignerUtils.DEBUG) {
//            DesignerUtils.debugLog(getClass().getName() + ".getDropType(Point, PaletteItem, boolean)");
//        }
//        if(p == null) {
//            throw(new IllegalArgumentException("Null drop point."));
//        }
        if(classNames == null) {
            throw(new IllegalArgumentException("Null class names array."));
        }

//        recentDropTarget = null;

//        // No... call computePositions and use location.coordinates instead... see @todo above
////        CssBox box = webform.getMapper().findBox(p.x, p.y);
//        CssBox box = ModelViewMapper.findBox(webform.getPane().getPageBox(), p.x, p.y);
//        DesignBean origDroppee = getDroppee(box);

        if (origDroppee == null) {
            if (linkOnly) {
                return DROP_DENIED;
            }

//            if (origDroppee instanceof MarkupDesignBean) {
//                recentDropTarget = (MarkupDesignBean)origDroppee;
//            }

//            LiveUnit unit = webform.getModel().getLiveUnit();

//            if (unit != null) {
                for (int i = 0; i < classNames.length; i++) {
                    // Do anything smart about facets here? E.g. what if you
                    // point over a facet table header? A drop in the app outline
                    // would offer to replace it. Should the interactive link feedback
                    // allow this too?
//                    if (unit.canCreateBean(classNames[i], null, null)) {
//                    if (webform.canCreateBean(classNames[i], null, null)) {
                    if (canCreateBean(classNames[i], null, null)) {
                        showDropMatch(null, DROP_PARENTED);

                        return DROP_PARENTED;
                    }
                }
//            }

            clearDropMatch();

            return DROP_DENIED;
        }

        // None of the droppee ancestors accepted the drop items
        // as a potential child - but perhaps they will accept
        // a link?
//        Class[] classes = new Class[classNames.length];
//        ArrayList beanList = null;
//
//        if (beans != null) {
//            beanList = new ArrayList(beans.length);
//        }
//
//        for (int i = 0; i < classNames.length; i++) {
//            try {
//                Class clz = webform.getModel().getFacesUnit().getBeanClass(classNames[i]);
//
//                if (clz != null) {
//                    classes[i] = clz;
//                }
//
//                if (beans != null) {
//                    beanList.add(beans[i]);
//                }
//            } catch (Exception e) {
//                ErrorManager.getDefault().notify(e);
//            }
//        }
//        
//        if (beans == null) {
//            beanList = null;
//        }
        List<Class> classList = new ArrayList<Class>();
        List<DesignBean> beanList = beans == null ? null : new ArrayList<DesignBean>();
        for (int i = 0; i < classNames.length; i++) {
            try {
//                Class clazz = webform.getModel().getFacesUnit().getBeanClass(classNames[i]);
//                Class clazz = webform.getBeanClass(classNames[i]);
                Class clazz = getBeanClass(classNames[i]);
                if (clazz != null) {
                    classList.add(clazz);
                }
                if (beans != null) {
                    beanList.add(beans[i]);
                }
            } catch (ClassNotFoundException ex) {
                // XXX #6492649 It means the class can't be found so no drop should happen.
                // FIXME The API should be improved and not controlled via exceptions.
                continue;
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                continue;
            }
        }
        Class[] classes = classList.toArray(new Class[classList.size()]);

//        RaveElement droppeeElement = (RaveElement)box.getElement();
//        Element droppeeElement = box.getElement();
        
        int dropType = processLinks(droppeeElement, classes, beanList, true, false, true);

        if (dropType != DROP_DENIED) {
            return dropType;
        }

        if (linkOnly) {
            clearDropMatch();

            return DROP_DENIED;
        }

        // See if any of the droppee parents accept the new item as a
        // child
        for (int i = 0; i < classNames.length; i++) {
            Node parentNode = null; // XXX todo figure out better parent node
//            DesignBean parent = findParent(classNames[i], origDroppee, parentNode, true);
//            DesignBean parent = webform.findParent(classNames[i], origDroppee, parentNode, true);
            DesignBean parent = findParent(classNames[i], origDroppee, parentNode, true);

            if (parent != null) {
                if (parent instanceof MarkupDesignBean) {
//                    recentDropTarget = (MarkupDesignBean)parent;
//                    showDropMatch(recentDropTarget, null, DROP_PARENTED);
                    showDropMatch(DomProviderImpl.getComponentRootElementForMarkupDesignBean((MarkupDesignBean)parent), DROP_PARENTED);
                } else {
                    clearDropMatch();
                }

                return DROP_PARENTED;
            }
        }

        showDropMatch(null, DROP_DENIED);

        return DROP_DENIED;
    }
    
    private boolean canCreateBean(String className, DesignBean parent, Position pos) {
//        LiveUnit liveUnit = getFacesModel().getLiveUnit();
        LiveUnit liveUnit = jsfForm.getLiveUnit();
        if (liveUnit == null) {
            return false;
        }
        return liveUnit.canCreateBean(className, parent, pos);
    }
    
    private DesignBean findParent(String className, DesignBean droppee, Node parentNode, boolean searchUp) {
//        return Util.findParent(className, droppee, parentNode, searchUp, getFacesModel());
        return jsfForm.findParent(className, droppee, parentNode, searchUp);
    }
    
    private Class getBeanClass(String className) throws ClassNotFoundException {
//        return getFacesModel().getFacesUnit().getBeanClass(className);
        return jsfForm.getFacesPageUnit().getBeanClass(className);
    }
    
    private void showDropMatch(Element componentRootElement, int dropType) {
//        jsfForm.fireShowDropMatch(componentRootElement, null, dropType);
        jsfForm.showDropMatch(componentRootElement, null, dropType);
    }
    
    private void clearDropMatch() {
//        jsfForm.fireClearDropMatch();
        jsfForm.clearDropMatch();
    }

    void importString(Designer designer, String string, Point canvasPos, Node documentPosNode, int documentPosOffset, Dimension dimension, boolean isGrid,
            Element droppeeElement, DesignBean droppeeBean, DesignBean defaultParent/*, DomProvider.CoordinateTranslator coordinateTranslator*/) {
//        getFacesModel().getDnDSupport().importString(string, canvasPos, documentPosNode, documentPosOffset, dimension, isGrid,
//                droppeeElement, droppeeBean, defaultParent, new CoordinateTranslatorImpl(coordinateTranslator), jsfForm.getUpdateSuspender());
        facesDndSupport.importString(designer, string, canvasPos, documentPosNode, documentPosOffset, dimension, isGrid,
                droppeeElement, droppeeBean, defaultParent, /*new CoordinateTranslatorImpl(coordinateTranslator),*/ jsfForm.getUpdateSuspender());
    }

    boolean importData(Designer designer, JComponent comp, Transferable t, /*Object transferData,*/ Point canvasPos, Node documentPosNode, int documentPosOffset, Dimension dimension, boolean isGrid,
            Element droppeeElement, DesignBean droppeeBean, DesignBean defaultParent/*, DomProvider.CoordinateTranslator coordinateTranslator*/, int dropAction) {
//        getFacesModel().getDnDSupport().importData(comp, t, transferData, canvasPos, documentPosNode, documentPosOffset, dimension, isGrid,
//                droppeeElement, droppeeBean, defaultParent, new CoordinateTranslatorImpl(coordinateTranslator), jsfForm.getUpdateSuspender(), dropAction);
        return facesDndSupport.importData(designer, comp, t, /*transferData,*/ canvasPos, documentPosNode, documentPosOffset, dimension, isGrid,
                droppeeElement, droppeeBean, defaultParent, /*new CoordinateTranslatorImpl(coordinateTranslator),*/ jsfForm.getUpdateSuspender(), dropAction);

    }

    boolean canImport(JComponent comp, DataFlavor[] transferFlavors, Transferable transferable) {
        return facesDndSupport.canImport(comp, transferFlavors, transferable);
    }

    // XXX >>> JsfSupport
    public void moveBeans(DesignBean[] designBeans, DesignBean liveBean) {
        facesDndSupport.moveBeans(designBeans, liveBean, new MarkupPosition(-1), null);
    }

    public void selectAndInlineEdit(DesignBean[] beans, DesignBean bean) {
        facesDndSupport.notifyBeansDesigner(beans, bean);
    }

    public void refresh(boolean deep) {
        facesDndSupport.fireRefreshNeeded(deep);
    }
    // XXX <<< JsfSupport

    
//    // XXX
//    private static class CoordinateTranslatorImpl implements FacesDndSupport.CoordinateTranslator {
//        private final DomProvider.CoordinateTranslator coordinateTranslator;
//        
//        public CoordinateTranslatorImpl(DomProvider.CoordinateTranslator coordinateTranslator) {
//            this.coordinateTranslator = coordinateTranslator;
//        }
//        
//        public Point translateCoordinates(Element parent, int x, int y) {
//            return coordinateTranslator.translateCoordinates(parent, x, y);
//        }
//        
//        public int snapX(int x) {
//            return coordinateTranslator.snapX(x);
//        }
//        
//        public int snapY(int y) {
//            return coordinateTranslator.snapY(y);
//        }
//    } // End of CoordinateTranslatorImpl.

//    // XXX
//    private static class LocationImpl implements FacesDndSupport.Location {
//        private final DomProvider.Location location;
//        
//        
//        public LocationImpl(DomProvider.Location location) {
//            this.location = location;
//        }
//        
//        
//        public DesignBean getDroppee() {
//            return location.droppee;
//        }
//        
//        public String getFacet() {
//            return location.facet;
//        }
//        
//        public Element getDroppeeElement() {
//            return location.droppeeElement;
//        }
//        
//        public MarkupPosition getPos() {
//            return location.pos;
//        }
//        
//        public Point getCoordinates() {
//            return location.coordinates;
//        }
//        
//        public Dimension getSize() {
//            return location.size;
//        }
//    } // End of LocationImpl.

    
    // XXX Make FacesModel fire appropriate event changes and then this might be not needed.
    private static class DnDListener implements PropertyChangeListener {
        private final JsfForm jsfForm;
        
        public DnDListener(JsfForm jsfForm) {
            this.jsfForm = jsfForm;
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (FacesDndSupport.PROPERTY_DROP_TARGET.equals(evt.getPropertyName())) {
                FacesDndSupport.DropInfo dropInfo = (FacesDndSupport.DropInfo)evt.getNewValue();
//                jsfForm.designer.showDropMatch(dropInfo.getMarkupDesignBean(), dropInfo.getMarkupMouseRegion(), dropInfo.getDropType());
//                jsfForm.fireShowDropMatch(dropInfo.getMarkupDesignBean(), dropInfo.getMarkupMouseRegion(), dropInfo.getDropType());
                Element componentRootElement = DomProviderImpl.getComponentRootElementForMarkupDesignBean(dropInfo.getMarkupDesignBean());
//                jsfForm.fireShowDropMatch(componentRootElement, dropInfo.getRegionElement(), dropInfo.getDropType());
                jsfForm.showDropMatch(componentRootElement, dropInfo.getRegionElement(), dropInfo.getDropType());
            } else if (FacesDndSupport.PROPERTY_SELECTED_DESIGN_BEAN.equals(evt.getPropertyName())) {
                DesignBean designBean = (DesignBean)evt.getNewValue();
                Element componentRootElement = JsfSupportUtilities.getComponentRootElementForDesignBean(designBean);
//                jsfForm.designer.select((DesignBean)evt.getNewValue());
//                jsfForm.fireSelect((DesignBean)evt.getNewValue());
                if (componentRootElement == null) {
                    org.openide.nodes.Node node = DesigntimeIdeBridgeProvider.getDefault().getNodeRepresentation(designBean);
                    if (node != null) {
                        // XXX Might be still a hidden element, select in outline only.
                        OutlineSelector.getDefault().selectNodes(new org.openide.nodes.Node[] {node});
                    }
                } else {
                    jsfForm.selectComponent(componentRootElement);
                }
            } else if (FacesDndSupport.PROPERTY_REFRESH.equals(evt.getPropertyName())) {
//                jsfForm.designer.refreshForm(((Boolean)evt.getNewValue()).booleanValue());
//                jsfForm.fireRefreshForm(((Boolean)evt.getNewValue()).booleanValue());
                jsfForm.refreshModel(((Boolean)evt.getNewValue()).booleanValue());
            } else if (FacesDndSupport.PROPERTY_INLINE_EDIT.equals(evt.getPropertyName())) {
                DesignBean[] designBeans = (DesignBean[])evt.getNewValue();
                List<Element> componentRootElements = new ArrayList<Element>();
                for (DesignBean designBean : designBeans) {
                    Element componentRootElement = JsfSupportUtilities.getComponentRootElementForDesignBean(designBean);
                    if (componentRootElement != null) {
                        componentRootElements.add(componentRootElement);
                    }
                }
//                jsfForm.designer.inlineEdit((DesignBean[])evt.getNewValue());
//                jsfForm.fireInlineEdit((DesignBean[])evt.getNewValue());
                jsfForm.inlineEditComponents(componentRootElements.toArray(new Element[componentRootElements.size()]));
            }
        }
    } // End of DnDListener.
    
    
    private static void log(Exception ex) {
        Logger logger = Logger.getLogger(DndSupport.class.getName());
        logger.log(Level.INFO, null, ex);
    }
}
