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

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DisplayItem;
import com.sun.rave.designtime.Position;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import com.sun.rave.designtime.markup.MarkupMouseRegion;
import com.sun.rave.designtime.markup.MarkupPosition;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import org.netbeans.modules.visualweb.api.designer.HtmlDomProvider;
import org.netbeans.modules.visualweb.api.designerapi.DesignTimeTransferDataCreator;
import org.netbeans.modules.visualweb.insync.FacesDnDSupport;
import org.netbeans.modules.visualweb.insync.Util;
import org.netbeans.modules.visualweb.insync.live.LiveUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.w3c.dom.Element;
import static org.netbeans.modules.visualweb.api.designer.HtmlDomProvider.*;
import org.w3c.dom.Node;

/**
 * Place for JSF specific DnD support.
 * Factored out the complicated stuff from the designer/DnDHandler.
 *
 * @author Peter Zavadsky
 * @author Tor Norbye (the original moved code)
 */
class DndSupport {
    
    private final JsfForm jsfForm;
    /** XXX TEMP solution. Listener on DnD changes. */
    private PropertyChangeListener dndListener;
    
//    private MarkupDesignBean recentDropTarget;
    
    
    /** Creates a new instance of DndSupport */
    public DndSupport(JsfForm jsfForm) {
        this.jsfForm = jsfForm;
    }

    
    private FacesModel getFacesModel() {
        return jsfForm.getFacesModel();
    }
    
    DataFlavor getImportFlavor(DataFlavor[] flavors) {
        return FacesDnDSupport.getImportFlavor(flavors);
    }

//    MarkupPosition getDefaultMarkupPositionUnderParent(DesignBean parent) {
//        return FacesDnDSupport.getDefaultMarkupPositionUnderParent(parent, getFacesModel());
//    }


    String[] getClassNames(DisplayItem[] displayItems) {
        return getFacesModel().getDnDSupport().getClasses(displayItems);
    }
    
//    boolean importBean(DisplayItem[] items, DesignBean origParent, int nodePos,
//            String facet, List createdBeans, HtmlDomProvider.Location location,
//            HtmlDomProvider.CoordinateTranslator coordinateTranslator) throws IOException {
//        return getFacesModel().getDnDSupport().importBean(items, origParent, nodePos, facet, createdBeans,
//                new LocationImpl(location), new CoordinateTranslatorImpl(coordinateTranslator), jsfForm.getUpdateSuspender());
//    }

//    void importData(JComponent comp, Transferable t, Object transferData, Dimension dimension,
//            HtmlDomProvider.Location location, HtmlDomProvider.CoordinateTranslator coordinateTranslator, int dropAction) {
//        getFacesModel().getDnDSupport().importData(comp, t, transferData, dimension, new LocationImpl(location),
//                new CoordinateTranslatorImpl(coordinateTranslator), jsfForm.getUpdateSuspender(), dropAction);
//    }
    
//    void importString(String string, HtmlDomProvider.Location location, HtmlDomProvider.CoordinateTranslator coordinateTranslator) {
//        getFacesModel().getDnDSupport().importString(string, new LocationImpl(location), new CoordinateTranslatorImpl(coordinateTranslator), jsfForm.getUpdateSuspender());
//    }
    

    DesignBean[] pasteBeans(Transferable t, DesignBean parent, MarkupPosition pos, Point location, HtmlDomProvider.CoordinateTranslator coordinateTranslator, FacesDnDSupport.UpdateSuspender updateSuspender) {
        return getFacesModel().getDnDSupport().pasteBeans(t, parent, pos, location, new CoordinateTranslatorImpl(coordinateTranslator), jsfForm.getUpdateSuspender());
    }

    int computeActions(DesignBean droppee, Transferable transferable, boolean searchUp, int nodePos) {
        return getFacesModel().getDnDSupport().computeActions(droppee, transferable, searchUp, nodePos);
    }

    int processLinks(Element origElement, Class[] classes, List beans, boolean selectFirst, boolean handleLinks, boolean showLinkTarget) {
        return getFacesModel().getDnDSupport().processLinks(origElement, classes, beans, selectFirst, handleLinks, showLinkTarget, jsfForm.getUpdateSuspender());
    }

    void updateDndListening() {
        dndListener = new DnDListener(jsfForm);
        // XXX Listening on dnd support, it should be on model.
        getFacesModel().getDnDSupport().addPropertyChangeListener(WeakListeners.propertyChange(dndListener, getFacesModel().getDnDSupport()));
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
            // XXX #6468896 To be able to drop files (images) from the outside world (desktop).
           return DROP_PARENTED;
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
                        if (FacesDnDSupport.isImage(fo.getExt())) {
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
                        } else if (FacesDnDSupport.isStylesheet(fo.getExt())) {
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
        if (jsfForm.getHtmlDomProvider().isBraveheartPage()) {
            return com.sun.rave.web.ui.component.ImageComponent.class.getName(); // NOI18N
        } else if (jsfForm.getHtmlDomProvider().isWoodstockPage()) {
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
                    showDropMatch(HtmlDomProviderImpl.getComponentRootElementForMarkupDesignBean((MarkupDesignBean)parent), DROP_PARENTED);
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
        LiveUnit liveUnit = getFacesModel().getLiveUnit();
        if (liveUnit == null) {
            return false;
        }
        return liveUnit.canCreateBean(className, parent, pos);
    }
    
    private DesignBean findParent(String className, DesignBean droppee, Node parentNode, boolean searchUp) {
        return Util.findParent(className, droppee, parentNode, searchUp, getFacesModel());
    }
    
    private Class getBeanClass(String className) throws ClassNotFoundException {
        return getFacesModel().getFacesUnit().getBeanClass(className);
    }
    
    private void showDropMatch(Element componentRootElement, int dropType) {
        jsfForm.fireShowDropMatch(componentRootElement, null, dropType);
    }
    
    private void clearDropMatch() {
        jsfForm.fireClearDropMatch();
    }

    void importString(String string, Point canvasPos, Node documentPosNode, int documentPosOffset, Dimension dimension, boolean isGrid,
            Element droppeeElement, DesignBean droppeeBean, DesignBean defaultParent, HtmlDomProvider.CoordinateTranslator coordinateTranslator) {
        getFacesModel().getDnDSupport().importString(string, canvasPos, documentPosNode, documentPosOffset, dimension, isGrid,
                droppeeElement, droppeeBean, defaultParent, new CoordinateTranslatorImpl(coordinateTranslator), jsfForm.getUpdateSuspender());
    }

    void importData(JComponent comp, Transferable t, Object transferData, Point canvasPos, Node documentPosNode, int documentPosOffset, Dimension dimension, boolean isGrid,
            Element droppeeElement, DesignBean droppeeBean, DesignBean defaultParent, HtmlDomProvider.CoordinateTranslator coordinateTranslator, int dropAction) {
        getFacesModel().getDnDSupport().importData(comp, t, transferData, canvasPos, documentPosNode, documentPosOffset, dimension, isGrid,
                droppeeElement, droppeeBean, defaultParent, new CoordinateTranslatorImpl(coordinateTranslator), jsfForm.getUpdateSuspender(), dropAction);
    }

    
    // XXX
    private static class CoordinateTranslatorImpl implements FacesDnDSupport.CoordinateTranslator {
        private final HtmlDomProvider.CoordinateTranslator coordinateTranslator;
        
        public CoordinateTranslatorImpl(HtmlDomProvider.CoordinateTranslator coordinateTranslator) {
            this.coordinateTranslator = coordinateTranslator;
        }
        
        public Point translateCoordinates(Element parent, int x, int y) {
            return coordinateTranslator.translateCoordinates(parent, x, y);
        }
        
        public int snapX(int x) {
            return coordinateTranslator.snapX(x);
        }
        
        public int snapY(int y) {
            return coordinateTranslator.snapY(y);
        }
    } // End of CoordinateTranslatorImpl.

    // XXX
    private static class LocationImpl implements FacesDnDSupport.Location {
        private final HtmlDomProvider.Location location;
        
        
        public LocationImpl(HtmlDomProvider.Location location) {
            this.location = location;
        }
        
        
        public DesignBean getDroppee() {
            return location.droppee;
        }
        
        public String getFacet() {
            return location.facet;
        }
        
        public Element getDroppeeElement() {
            return location.droppeeElement;
        }
        
        public MarkupPosition getPos() {
            return location.pos;
        }
        
        public Point getCoordinates() {
            return location.coordinates;
        }
        
        public Dimension getSize() {
            return location.size;
        }
    } // End of LocationImpl.

    
    // XXX Make FacesModel fire appropriate event changes and then this might be not needed.
    private static class DnDListener implements PropertyChangeListener {
        private final JsfForm jsfForm;
        
        public DnDListener(JsfForm jsfForm) {
            this.jsfForm = jsfForm;
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (FacesDnDSupport.PROPERTY_DROP_TARGET.equals(evt.getPropertyName())) {
                FacesDnDSupport.DropInfo dropInfo = (FacesDnDSupport.DropInfo)evt.getNewValue();
//                jsfForm.designer.showDropMatch(dropInfo.getMarkupDesignBean(), dropInfo.getMarkupMouseRegion(), dropInfo.getDropType());
//                jsfForm.fireShowDropMatch(dropInfo.getMarkupDesignBean(), dropInfo.getMarkupMouseRegion(), dropInfo.getDropType());
                Element componentRootElement = HtmlDomProviderImpl.getComponentRootElementForMarkupDesignBean(dropInfo.getMarkupDesignBean());
                jsfForm.fireShowDropMatch(componentRootElement, dropInfo.getRegionElement(), dropInfo.getDropType());
            } else if (FacesDnDSupport.PROPERTY_SELECTED_DESIGN_BEAN.equals(evt.getPropertyName())) {
//                jsfForm.designer.select((DesignBean)evt.getNewValue());
                jsfForm.fireSelect((DesignBean)evt.getNewValue());
            } else if (FacesDnDSupport.PROPERTY_REFRESH.equals(evt.getPropertyName())) {
//                jsfForm.designer.refreshForm(((Boolean)evt.getNewValue()).booleanValue());
//                jsfForm.fireRefreshForm(((Boolean)evt.getNewValue()).booleanValue());
                jsfForm.refreshModel(((Boolean)evt.getNewValue()).booleanValue());
            } else if (FacesDnDSupport.PROPERTY_INLINE_EDIT.equals(evt.getPropertyName())) {
//                jsfForm.designer.inlineEdit((DesignBean[])evt.getNewValue());
                jsfForm.fireInlineEdit((DesignBean[])evt.getNewValue());
            }
        }
    } // End of DnDListener.
}
