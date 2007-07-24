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
package org.netbeans.modules.visualweb.insync.live;

import org.netbeans.modules.visualweb.api.designer.markup.MarkupService;
import org.netbeans.modules.visualweb.api.designerapi.DesignTimeTransferDataCreator;
import org.netbeans.modules.visualweb.designer.html.HtmlAttribute;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import com.sun.rave.designtime.markup.MarkupPosition;
import org.netbeans.modules.visualweb.extension.openide.loaders.SystemFileSystemSupport;
import org.netbeans.modules.visualweb.insync.ResultHandler;
import org.netbeans.modules.visualweb.insync.UndoEvent;
import org.netbeans.modules.visualweb.insync.Util;
import org.netbeans.modules.visualweb.insync.beans.BeansUnit;
import org.netbeans.modules.visualweb.insync.faces.FacesBean;
import org.netbeans.modules.visualweb.insync.faces.MarkupBean;
import org.netbeans.modules.visualweb.insync.live.LiveUnit.ClipImage;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.Customizer;
import java.beans.EventSetDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.sql.RowSet;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import org.openide.ErrorManager;
import javax.swing.event.ChangeListener;

import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.PasteAction;
import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileObject;
// XXX Not a NB cookie, can't use.
//import org.openide.cookies.UndoRedoCookie;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.PasteType;

import com.sun.rave.designtime.BeanCreateInfo;
import com.sun.rave.designtime.BeanCreateInfoSet;
import com.sun.rave.designtime.CategoryDescriptor;
import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.Customizer2;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignEvent;
import com.sun.rave.designtime.DesignInfo;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.DisplayItem;
import com.sun.rave.designtime.EventDescriptor;
import com.sun.rave.designtime.Position;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.event.DesignBeanListener;
import com.sun.rave.designtime.event.DesignContextListener;
import com.sun.rave.designtime.faces.FacesBindingPropertyEditor;
import com.sun.rave.designtime.markup.AttributePropertyEditor;
import org.netbeans.modules.visualweb.extension.openide.util.Trace;
import org.netbeans.modules.visualweb.insync.Unit;
import org.netbeans.modules.visualweb.propertyeditors.binding.ValueBindingAttributePropertyEditor;
import org.netbeans.modules.visualweb.propertyeditors.binding.ValueBindingPropertyEditor;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.w3c.dom.Element;

/**
 * The netbeans node associated with a live bean, either a container or a leaf
 *
 * @author Carl Quinn
 */
public class DesignBeanNode extends AbstractNode implements DesignBeanListener {
    
    final protected DesignBean liveBean;
    protected Class customizer;
    protected Customizer2 liveCustomizer;
    protected DataObject dataObject;
    
    /** Name of property set for general properties */
    final static public String GENERAL = "General"; // NOI18N
    final static public String GENERAL_HINT = "GeneralHint"; // NOI18N
    final static public String EVENTS = "Events"; // NOI18N
    final static public String EVENTS_HINT = "EventsHint"; // NOI18N
    
    //Name of property id
    final static public String PROPERTY_ID = "id"; // NOI18N
    //Display name of property id is different to list it at the top
    final static public String PROPERTY_ID_DISPLAY = NbBundle.getMessage(DesignBeanNode.class, "LBL_Id"); // NOI18N
    
    private final DesignContextListener designContextListener = new DesignBeanNodeDesignContextListener(this);

    static DesignBeanNode getInstance(DesignBean liveBean) {
        Children kids = liveBean.isContainer()
        ? new BeanChildren(liveBean)
        : Children.LEAF;
        
        List fixedLookupItems = new ArrayList();
        fixedLookupItems.add(liveBean);
        
        if (kids instanceof Index && liveBean instanceof MarkupDesignBean) {
            // Allow reordering of children for MarkupDesignBeans only
            // Add DesignBean
            fixedLookupItems.add(kids);
        }
        
        // Add also undo redo manager into lookup so it can be retrieved in outline top comp.
        UndoRedo undoRedo;
        DesignContext designContext = liveBean.getDesignContext();
        if (designContext instanceof LiveUnit) {
            FacesModel facesModel = ((LiveUnit)designContext).getModel();
            if (facesModel != null) {
                undoRedo = facesModel.getUndoManager();
            } else {
                undoRedo = null;
            }
        } else {
            undoRedo = null;
        }
        if (undoRedo != null) {
            fixedLookupItems.add(undoRedo);
        }
        
        Lookup fixedLookup = Lookups.fixed(fixedLookupItems.toArray());
        
        return new DesignBeanNode(liveBean, kids, fixedLookup, new DesignBeanNodeLookup());
    }
    
    /** Adding the <code>Designean</code> to the lookup.
     * @see org.openide.nodes.Node(Children, Lookup) */
    private DesignBeanNode(DesignBean liveBean, Children children, Lookup fixedLookup, DesignBeanNodeLookup designBeanNodeLookup) {
        super(children, new ProxyLookup(new Lookup[] {fixedLookup, designBeanNodeLookup}));
        
        // XXX To init the lookup with the node, see DesignBeanNodeLookup.
        designBeanNodeLookup.setNode(this);
        
        this.liveBean = liveBean;
        
        if (!getRegisteredCustomizer())
            getInfoCustomizer();
        
        assert Trace.trace("insync.live", "LBN  lb:" + liveBean + " bi:" + liveBean.getBeanInfo() +
                " cu:" + customizer + " lc:" + liveCustomizer);
        
        initDefaultIconBase();
        initHelpCtx();
        liveBean.addDesignBeanListener(this);
        // XXX #6484230 One needs to listen on design context as well, to catch rename events.
        DesignContext designContext = liveBean.getDesignContext();
        if (designContext != null) {
            designContext.addDesignContextListener(
                (DesignContextListener)WeakListeners.create(DesignContextListener.class, designContextListener, designContext));
        }

        
        setDataObject(retrieveJspDataObject(liveBean));
        
        // XXX #6473798 Sets non-null programatic name.
        // There should be some better way then toString().
        setName(liveBean.toString());
        
        updateToolTip();
    }
    
    
    private void initDefaultIconBase() {
        // XXX Better design the nodes provided for specific beans (see e.g. the DataObject vs. DataNode).
        if (liveBean instanceof SourceLiveRoot) {
            // Root icon.
            if (Util.isPageRootContainerDesignBean(liveBean)) {
                setIconBaseWithExtension("org/netbeans/modules/visualweb/insync/live/defaultPageRoot.png"); // NOI18N
            } else {
                setIconBaseWithExtension("org/netbeans/modules/visualweb/insync/live/defaultBeanRoot.png"); // NOI18N
            }
        } else if (liveBean instanceof MarkupDesignBean && ((MarkupDesignBean)liveBean).getElement() != null) {
            // Tag icon.
            setIconBaseWithExtension("org/netbeans/modules/visualweb/insync/live/defaultTag.png"); // NOI18N
        } else {
            // Bean icon.
            setIconBaseWithExtension("org/netbeans/modules/visualweb/insync/live/defaultBean.gif"); // NOI18N
        }
    }
    
    /**
     * help related stuff -- context help
     */
    private String helpKey;
    private String propertiesHelpKey;
    private void initHelpCtx() {
        helpKey = (String)liveBean.getBeanInfo().getBeanDescriptor().getValue(Constants.BeanDescriptor.HELP_KEY);
        propertiesHelpKey = (String)liveBean.getBeanInfo().getBeanDescriptor().getValue(Constants.BeanDescriptor.PROPERTIES_HELP_KEY);
        
        // #6472652 Fallback for the markup beans.
        if (liveBean instanceof MarkupDesignBean || Util.isPageRootContainerDesignBean(liveBean)) {
            // XXX This is not wanted for the fix.
//            if (helpKey == null) {
//                helpKey = "projrave_ui_elements_page"; // NOI18N
//            }
            if (propertiesHelpKey == null) {
                propertiesHelpKey = "projrave_ui_elements_propsheets_page_props"; // NOI18N
            }
        }
        
        if (propertiesHelpKey != null) {
            setValue("propertiesHelpID", propertiesHelpKey); // NOI18N
        }
    }
    
    public HelpCtx getHelpCtx() {
        return  helpKey != null ? new HelpCtx(helpKey) : null;
    }
    
    /**
     *
     */
    private boolean getRegisteredCustomizer() {
        String classname;
        DesignInfo lbi = liveBean.getDesignInfo();
        BeanInfo bi = liveBean.getBeanInfo();
        if (lbi != null) {
            classname = lbi.getBeanClass().getName();
        } else {
            if (bi != null)
                classname = bi.getBeanDescriptor().getBeanClass().getName();
            else
                classname = liveBean.getInstance().getClass().getName();
        }
        liveCustomizer = LiveUnit.getCustomizer(classname);
        
        assert Trace.trace("insync.live", "LBN.RC lbi:" + lbi + " bi:" + bi + " classname:" + classname +
                " lc:" + liveCustomizer);
        
        return liveCustomizer != null;
    }
    
    Customizer2 getCustomizer2() {
        return liveCustomizer;
    }
    
    /**
     *
     */
    private void getInfoCustomizer() {
        BeanDescriptor bd = liveBean.getBeanInfo().getBeanDescriptor();
        if (bd != null) {
            customizer = bd.getCustomizerClass();
            if (customizer != null) {
                if (Customizer2.class.isAssignableFrom(customizer)) {
                    try {
                        liveCustomizer = (Customizer2)customizer.newInstance();
                    } catch (Exception e) {
                    }
                }
                if (!Customizer.class.isAssignableFrom(customizer))
                    customizer = null;  // not a valid Customizer, throw away ref
            }
        }
    }
    
    /**
     * Get a cookie. Call super first, but if null, delegate to
     * the associated data object (if any). The delegated to data object
     * is generally the file representing the container for the bean.
     */
    public Node.Cookie getCookie(Class cl) {
        Node.Cookie c = super.getCookie(cl);
        if (c != null)
            return c;
        
        // XXX Just a hack for the rare cases the dataObject was not available at the beginning.
        // Rather the (faces)model should fire changes about file changes (if there are any).
        if (dataObject == null) {
            setDataObject(retrieveJspDataObject(liveBean));
        }
        
        if (dataObject != null)
            return dataObject.getCookie(cl);
        
        return null;
    }
    
    /**
     * Set a data object to associate this node with. Whenever the node is asked for a cookie it
     * doesn't hold, it will delegate the question to the associated data object.
     * <p>
     * NOTE: the caller MUST call setDataObject(null) on this node when done
     * with it to free up resources!
     * @param dataObject the data object to associate with the node
     */
    private void setDataObject(DataObject dataObject) {
        if (this.dataObject != dataObject) {
//            if (this.dataObject != null)
//                this.dataObject.removePropertyChangeListener(pListener);
            
            this.dataObject = dataObject;
            
            if (this.dataObject != null) {
                pListener = new PListener();
                this.dataObject.addPropertyChangeListener(WeakListeners.propertyChange(pListener, this.dataObject));
            } else {
                pListener = null;
            }
        }
    }
    
    private static DataObject retrieveJspDataObject(DesignBean designBean) {
        DesignContext designContext = designBean.getDesignContext();
        if (designContext instanceof LiveUnit) {
            FacesModel facesModel = ((LiveUnit)designContext).getModel();
            if (facesModel == null) {
                return null;
            }
            
            FileObject jspFileObject = facesModel.getMarkupFile();
            if (jspFileObject == null) {
                return null;
            }
            
            try {
                return DataObject.find(jspFileObject);
            } catch (DataObjectNotFoundException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        
        return null;
    }
    
    
    // #5042202 Node delegating cookie has to also propagate the cookie change events
    // in order for the dependant cookie actions (other observers) are working correctly.
    private java.beans.PropertyChangeListener pListener;
    
    private class PListener implements java.beans.PropertyChangeListener {
        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            if (DataObject.PROP_COOKIE.equals(evt.getPropertyName()) && dataObject == evt.getSource())
                fireCookieChange();
        }
    }
    
    public boolean canRename() {
        return false;
    }
    
    public boolean canDestroy() {
        return !Util.isSpecialBean(liveBean) && !isHeadDesignBean(liveBean);
    }
    
    public void destroy() throws IOException {
        // Do not explicitelly remove the node, the node structure will be update
        // based on model changes.
//      super.destroy();
        
        // XXX #6475512 Insync works in AWT only (which wrong, but that how it is),
        // and DeleteAction is asynchronous - see DeleteAction.asynchronous method.
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    doDestroy();
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
        });
    }
    
    private void doDestroy() throws IOException {
        LiveUnit liveUnit = (LiveUnit) liveBean.getDesignContext();
        UndoEvent undoEvent = liveUnit.getModel().writeLock("destroy"); // TODO I18N
        try {
            liveUnit.deleteBean(liveBean);
        } finally {
            liveUnit.getModel().writeUnlock(undoEvent);
        }
    }
    
    public PasteType getDropType(Transferable t, int action, int index) {
        PasteType pasteType = super.getDropType(t, action, index);
        if (pasteType instanceof BeanCreateInfoPasteType) {
            ((BeanCreateInfoPasteType) pasteType).setIndex(index);
        } else if (pasteType instanceof BeanCreateInfoSetPasteType) {
            ((BeanCreateInfoSetPasteType) pasteType).setIndex(index);
        }
        return pasteType;
    }
    
    /**
     * Can this node be copied?
     *
     * @return <code>true</code>
     */
    public boolean canCopy() {
        return !Util.isSpecialBean(liveBean);
    }
    
    public Transferable clipboardCopy() throws IOException {
        if (Util.isSpecialBean(liveBean)) {
            return null;
        }
        
        Transferable deflt = super.clipboardCopy();
        ExTransferable enriched = ExTransferable.create(deflt);
        enriched.put((ExTransferable.Single)((LiveUnit)liveBean.getDesignContext()).copyBeans(new DesignBean[] {liveBean}));
        return enriched;
    }
    
    /**
     * Can this node be cut?
     *
     * @return <code>false</code>
     */
    public boolean canCut() {
        return !Util.isSpecialBean(liveBean);
    }
    
    static final String CutFlavorMime = "application/x-creator-components;class=" +  //NOI18N
            DesignBeanNode.class.getName();
    
    static final DataFlavor cutFlavor =
            new DataFlavor(CutFlavorMime,
            NbBundle.getMessage(DesignBeanNode.class, "CutComponents"));  //NOI18N
    
    public Transferable clipboardCut() throws IOException {
        if (Util.isSpecialBean(liveBean)) {
            return null;
        }
        
        Transferable deflt = super.clipboardCut();
        ExTransferable enriched = ExTransferable.create(deflt);
        ExTransferable.Single cutEnriched = new ExTransferable.Single(cutFlavor) {
            public Object getData() {
                return DesignBeanNode.this;
            }
        };
        enriched.put(cutEnriched);
        return enriched;
    }
    
    private static boolean isAncestorOf(DesignBean possibleAncestorDesignBean, DesignBean possibleDescendentDesignBean) {
        if (possibleAncestorDesignBean == null || possibleDescendentDesignBean == null) {
            return false;
        }
        for (DesignBean parentDesignBean = possibleDescendentDesignBean.getBeanParent();
        parentDesignBean != null;
        parentDesignBean = parentDesignBean.getBeanParent()) {
            if (possibleAncestorDesignBean == parentDesignBean) {
                return true;
            }
        }
        return false;
    }
    
    DataFlavor displayItemDataFlavor = new DataFlavor(
            DataFlavor.javaJVMLocalObjectMimeType + "; class=" + DisplayItem.class.getName(), // NOI18N
            "DISPLAY_ITEM_HUMAN_NAME"); // NOI18N
    
    static DesignTimeTransferDataCreator dataCreator = (DesignTimeTransferDataCreator)Lookup.getDefault().lookup(DesignTimeTransferDataCreator.class);
    
    /** Don't allow pastes
     */
    protected void createPasteTypes(Transferable t, List s) {
        super.createPasteTypes(t, s);
        DesignContext designContext = liveBean.getDesignContext();
        LiveUnit liveUnit = (LiveUnit) designContext;
        if (liveUnit.getBeansUnit() == null) {
            // This can happens when the leaked DesignBeanNode's get called called by the Netbeans APIs
			// for updating the status of Paste action
            return;
        }
        if (t.isDataFlavorSupported(LiveUnit.flavor)) {
            try {
                LiveUnit.ClipImage clipImage = (LiveUnit.ClipImage) t.getTransferData(LiveUnit.flavor);
                boolean paste = true;
                String[] childBeanClassNames = clipImage.getTypes();
                for (int i = 0; i < childBeanClassNames.length; i++) {
                    String childBeanClassName = childBeanClassNames[i];
                    Class childBeanClass;
                    childBeanClass = liveUnit.getBeansUnit().getBeanClass(childBeanClassName);
                    DesignInfo liveBeanDesignInfo = liveBean.getDesignInfo();
                    if ((liveBeanDesignInfo != null && liveBeanDesignInfo.acceptLink(liveBean, null, childBeanClass)) ||
                            designContext.canCreateBean(childBeanClassName, liveBean, null)) {
                        // OK
                    } else {
                        paste = false;
                        break;
                    }
                }
                if (paste) {
                    PasteType pasteType = new ClipImagePasteType(t);
                    s.add(pasteType);
                }
            } catch (UnsupportedFlavorException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            } catch (ClassNotFoundException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        } else if (t.isDataFlavorSupported(cutFlavor)) {
            try {
                DesignBeanNode designBeanNode = (DesignBeanNode) t.getTransferData(cutFlavor);
                DesignBean cutBean = designBeanNode.getDesignBean();
                if (cutBean == liveBean || isAncestorOf(cutBean, liveBean)
                || !liveUnit.canMoveBean(cutBean, liveBean, new MarkupPosition(-1))) {
                    return;
                }
                PasteType pasteType = new CutDesignBeanPasteType(t);
                s.add(pasteType);
            } catch (UnsupportedFlavorException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        } else if (t.isDataFlavorSupported(displayItemDataFlavor)){
            // XXX #6470368 Checking whether it can paste.
            if (liveUnit != null && liveUnit.getModel() != null) {
                try {
                    DisplayItem displayItem = (DisplayItem) t.getTransferData(displayItemDataFlavor);
                    if (displayItem instanceof BeanCreateInfo) {
                        BeanCreateInfo beanCreateInfo = (BeanCreateInfo) displayItem;
                        String childBeanClassName = beanCreateInfo.getBeanClassName();
                        Class childBeanClass;
                        try {
                            childBeanClass = liveUnit.getBeansUnit().getBeanClass(childBeanClassName);
                            DesignInfo liveBeanDesignInfo = liveBean.getDesignInfo();
                            if ((liveBeanDesignInfo != null && liveBeanDesignInfo.acceptLink(liveBean, null, childBeanClass)) ||
                                    designContext.canCreateBean(childBeanClassName, liveBean, null)) {
                                PasteType pasteType = new BeanCreateInfoPasteType(beanCreateInfo);
                                s.add(pasteType);
                            }
                        } catch (ClassNotFoundException e) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                        }
                    } else if (displayItem instanceof BeanCreateInfoSet) {
                        BeanCreateInfoSet beanCreateInfoSet = (BeanCreateInfoSet) displayItem;
                        PasteType pasteType = new BeanCreateInfoSetPasteType(beanCreateInfoSet);
                        s.add(pasteType);
                    }
                } catch (UnsupportedFlavorException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
        } else {
            // XXX #6470368 Checking whether it can paste.
            if (liveUnit != null && liveUnit.getModel() != null) {
                if (dataCreator != null) {
                    DisplayItem displayItem = dataCreator.getDisplayItem(t);
                    if (displayItem != null) {
                        if (displayItem instanceof BeanCreateInfo) {
                            BeanCreateInfo beanCreateInfo = (BeanCreateInfo) displayItem;
                            String childBeanClassName = beanCreateInfo.getBeanClassName();
                            Class childBeanClass;
                            try {
                                childBeanClass = liveUnit.getBeansUnit().getBeanClass(childBeanClassName);
                                DesignInfo liveBeanDesignInfo = liveBean.getDesignInfo();
                                if ((liveBeanDesignInfo != null && liveBeanDesignInfo.acceptLink(liveBean, null, childBeanClass)) ||
                                        designContext.canCreateBean(childBeanClassName, liveBean, null)) {
                                    PasteType pasteType = new BeanCreateInfoPasteType(beanCreateInfo);
                                    s.add(pasteType);
                                }
                            } catch (ClassNotFoundException e) {
                                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                            }
                        } else if (displayItem instanceof BeanCreateInfoSet) {
                            BeanCreateInfoSet beanCreateInfoSet = (BeanCreateInfoSet) displayItem;
                            PasteType pasteType = new BeanCreateInfoSetPasteType(beanCreateInfoSet);
                            s.add(pasteType);
                        }
                    }
                }
            }
        }
    }
    
    private class ClipImagePasteType extends PasteType {
        private final Transferable t;
        
        public ClipImagePasteType(Transferable t) {
            this.t = t;
        }
        
        public Transferable paste() throws IOException {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    ((LiveUnit) liveBean.getDesignContext()).pasteBeans(t, liveBean, null);
                }
            });
            return null;
        }
    }
    
    private class CutDesignBeanPasteType extends PasteType {
        private final Transferable t;
        
        public CutDesignBeanPasteType(Transferable t) {
            this.t = t;
        }
        
        public Transferable paste() throws IOException {
            try {
                DesignBeanNode designBeanNode = (DesignBeanNode) t.getTransferData(cutFlavor);
                Transferable t = ((LiveUnit)designBeanNode.getDesignBean().getDesignContext()).copyBeans(new DesignBean[] {designBeanNode.getDesignBean()});
                LiveUnit liveUnit = ((LiveUnit) liveBean.getDesignContext());
                String description = NbBundle.getMessage(DesignBeanNode.class, "PasteBean");  //NOI18N
                UndoEvent event = liveUnit.getModel().writeLock(description);
                try {
//                    liveUnit.getModel().getDnDSupport().moveBeans(new DesignBean[] {designBeanNode.getDesignBean()}, liveBean, new MarkupPosition(-1), null);
                    liveUnit.getModel().getJsfSupport().moveBeans(new DesignBean[] {designBeanNode.getDesignBean()}, liveBean);
                } finally {
                    liveUnit.getModel().writeUnlock(event);
                }
                return ExTransferable.EMPTY;
            } catch (UnsupportedFlavorException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            return null;
        }
    }
    
    private class BeanCreateInfoPasteType extends PasteType {
        private final BeanCreateInfo beanCreateInfo;
        private int index = -1;
        
        public BeanCreateInfoPasteType(BeanCreateInfo beanCreateInfo) {
            this(beanCreateInfo, -1);
        }
        
        public BeanCreateInfoPasteType(BeanCreateInfo beanCreateInfo, int index) {
            this.beanCreateInfo = beanCreateInfo;
            this.index = index;
        }
        
        void setIndex(int index) {
            this.index = index;
        }
        
        public Transferable paste() throws IOException {
            Result result = null;
            DesignContext designContext = liveBean.getDesignContext();
            LiveUnit liveUnit = (LiveUnit) designContext;
            String childBeanClassName = beanCreateInfo.getBeanClassName();
            Class childBeanClass;
            try {
                childBeanClass = liveUnit.getBeansUnit().getBeanClass(childBeanClassName);
                DesignInfo liveBeanDesignInfo = liveBean.getDesignInfo();
                if (liveBeanDesignInfo != null && liveBeanDesignInfo.acceptLink(liveBean, null, childBeanClass)) {
                    String description = NbBundle.getMessage(DesignBeanNode.class, "LinkBean");  //NOI18N
                    UndoEvent event = liveUnit.getModel().writeLock(description);
                    try {
                        DesignBean childBean = designContext.createBean(childBeanClassName, null, null);
                        if (childBean != null) {
                            result = liveBeanDesignInfo.linkBeans(liveBean, childBean);
                            ResultHandler.handleResult(result, liveUnit.getModel());
                        }
                    } finally {
                        liveUnit.getModel().writeUnlock(event);
                    }
                } else if (designContext.canCreateBean(childBeanClassName, liveBean, null)) {
                    String description = NbBundle.getMessage(DesignBeanNode.class, "CreateBean");  //NOI18N
                    UndoEvent event = liveUnit.getModel().writeLock(description);
                    try {
                        Position position = null;
                        if (index >= 0 && index <= liveBean.getChildBeanCount()) {
                            if (index == liveBean.getChildBeanCount()) {
                                position = new MarkupPosition(-1);
                            } else {
                                DesignBean childDesignBean = liveBean.getChildBean(index);
                                if (childDesignBean instanceof MarkupDesignBean) {
                                    position = new MarkupPosition(index, ((MarkupDesignBean) childDesignBean).getElement());
                                } else {
                                    position = new MarkupPosition(index);
                                }
                            }
                        }
                        
                        DesignBean childBean = designContext.createBean(childBeanClassName, liveBean, position);
                        if (childBean != null) {
                            DesignInfo childBeanDesignInfo = childBean.getDesignInfo();
                            if (childBeanDesignInfo != null) {
                                result = childBeanDesignInfo.beanCreatedSetup(childBean);
                                ResultHandler.handleResult(result, liveUnit.getModel());
                            }
                        }
                    } finally {
                        liveUnit.getModel().writeUnlock(event);
                    }
                }
            } catch (ClassNotFoundException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
            
            return ExTransferable.EMPTY;
        }
    }
    
    private class BeanCreateInfoSetPasteType extends PasteType {
        private final BeanCreateInfoSet beanCreateInfoSet;
        private int index = -1;
        
        public BeanCreateInfoSetPasteType(BeanCreateInfoSet beanCreateInfoSet) {
            this(beanCreateInfoSet, -1);
        }
        
        public BeanCreateInfoSetPasteType(BeanCreateInfoSet beanCreateInfoSet, int index) {
            this.beanCreateInfoSet = beanCreateInfoSet;
            this.index = index;
        }
        
        void setIndex(int index) {
            this.index = index;
        }
        
        public Transferable paste() throws IOException {
            Result result = null;
            DesignContext designContext = liveBean.getDesignContext();
            LiveUnit liveUnit = (LiveUnit) designContext;
            String[] childBeanClassNames = beanCreateInfoSet.getBeanClassNames();
            // TODO deal with all classes
            if (childBeanClassNames != null && childBeanClassNames.length > 0) {
                String childBeanClassName = childBeanClassNames[0];
                Class childBeanClass;
                try {
                    childBeanClass = liveUnit.getBeansUnit().getBeanClass(childBeanClassName);
                    DesignInfo liveBeanDesignInfo = liveBean.getDesignInfo();
                    if (liveBeanDesignInfo != null && liveBeanDesignInfo.acceptLink(liveBean, null, childBeanClass)) {
                        String description = NbBundle.getMessage(DesignBeanNode.class, "LinkBean");  //NOI18N
                        UndoEvent event = liveUnit.getModel().writeLock(description);
                        try {
                            DesignBean childBean = designContext.createBean(childBeanClassName, null, null);
                            if (childBean != null) {
                                result = beanCreateInfoSet.beansCreatedSetup(new DesignBean[] {childBean});
                                ResultHandler.handleResult(result, liveUnit.getModel());
                                DesignInfo childBeanDesignInfo = childBean.getDesignInfo();
                                if (childBeanDesignInfo != null) {
                                    result = childBeanDesignInfo.beanCreatedSetup(childBean);
                                    ResultHandler.handleResult(result, liveUnit.getModel());
                                }
                                liveBeanDesignInfo.linkBeans(liveBean, childBean);
                            }
                        } finally {
                            liveUnit.getModel().writeUnlock(event);
                        }
                    } else if (designContext.canCreateBean(childBeanClassName, liveBean, null)) {
                        String description = NbBundle.getMessage(DesignBeanNode.class, "CreateBean");  //NOI18N
                        UndoEvent event = liveUnit.getModel().writeLock(description);
                        try {
                            Position position = null;
                            if (index >= 0 && index <= liveBean.getChildBeanCount()) {
                                if (index == liveBean.getChildBeanCount()) {
                                    position = new MarkupPosition(-1);
                                } else {
                                    DesignBean childDesignBean = liveBean.getChildBean(index);
                                    if (childDesignBean instanceof MarkupDesignBean) {
                                        position = new MarkupPosition(index, ((MarkupDesignBean) childDesignBean).getElement());
                                    } else {
                                        position = new MarkupPosition(index);
                                    }
                                }
                            }
                            DesignBean childBean = designContext.createBean(childBeanClassName, liveBean, position);
                            if (childBean != null) {
                                result = beanCreateInfoSet.beansCreatedSetup(new DesignBean[] {childBean});
                                ResultHandler.handleResult(result, liveUnit.getModel());
                                DesignInfo childBeanDesignInfo = childBean.getDesignInfo();
                                if (childBeanDesignInfo != null) {
                                    result = childBeanDesignInfo.beanCreatedSetup(childBean);
                                    ResultHandler.handleResult(result, liveUnit.getModel());
                                }
                            }
                        } finally {
                            liveUnit.getModel().writeUnlock(event);
                        }
                    }
                } catch (ClassNotFoundException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
            return ExTransferable.EMPTY;
        }
    }
    
    /** Return the live bean that this node is representing
     */
    public DesignBean getDesignBean() {
        return liveBean;
    }
    
    
//    /** XXX Bad design. Getter shouldn't be overriden, instead there should be listening on changes
//     * of the bean name, but they are missing. */
//    public final String getName() {
//        return liveBean.getInstanceName();
//    }
    
    /** XXX Bad design. Getter shouldn't be overriden, instead there should be listening on changes
     * of the bean name, but they are missing.
     *
     * The nodes should display the component name. This ensures that when you're looking at the
     * node in the tray for example (an explorer which shows the node names) you see the instance
     * name.)
     */
    public final String getDisplayName() {
        String instanceName = liveBean.getInstanceName();
        if (instanceName == null) {
            instanceName = NbBundle.getMessage(DesignBeanNode.class, "LBL_InvalidDisplayName", liveBean);
        }
        
        String defaultPropertyName = getDefaultPropertyDisplayName(liveBean);
        if (defaultPropertyName == null) {
            return instanceName;
        } else {
            return instanceName + ":" + defaultPropertyName; // NOI18N
        }
    }
    
    private void updateToolTip() {
        if (liveBean instanceof SourceLiveRoot) {
            DesignContext designContext = liveBean.getDesignContext();
            if (designContext != null) {
                setShortDescription(designContext.getDisplayName() + " (" + getScopeLabel(designContext) + ")"); // NOI18N
            }
        } else {
            BeanInfo bi = liveBean.getBeanInfo();
            if (bi != null) {
                setShortDescription(liveBean.getInstanceName()
                + " (" + getFormattedClassName(bi.getBeanDescriptor().getBeanClass()) + ")"); // NOI18N
            }
        }
    }
    
    private static String getScopeLabel(DesignContext designContext) {
        return (String)designContext.getContextData(Constants.ContextData.SCOPE);
    }
    
    private static String getFormattedClassName(Class clazz) {
        if(clazz.isArray()) {
            return getFormattedClassName(clazz.getComponentType()) + "[]"; // NOI18N
        } else {
            String fullName = clazz.getName();
            int lastDot = fullName.lastIndexOf('.'); // NOI18N
            if(lastDot > -1 && fullName.length() > lastDot) {
                return fullName.substring(lastDot + 1);
            } else {
                return fullName;
            }
        }
    }
    
    
    /** XXX Copied from OutlineTreeRenderer. */
    private static String getDefaultPropertyDisplayName(DesignBean bean) {
        BeanInfo bi = bean.getBeanInfo();
//        label = bean.getInstanceName();
        
        // Add in the default property's value if applicable
        int defaultProp = bi.getDefaultPropertyIndex();
        String beanClassName = bi.getBeanDescriptor().getBeanClass().getName();
        if (beanClassName.startsWith("com.sun.sql.rowset.CachedRowSetXImpl")){
            // If the bean is of type CachedRowSet then get the SQL command and display as part of label text (bug#6332976)
            // XXX PeterZ this is a hack and may not be efficient, revisit after Thresher  (Winston)
            // The correct solution is to have a BeanInfo for com.sun.sql.rowset.CachedRowSetXImpl and set the default
            // property as "command"
            PropertyDescriptor[] pds = bi.getPropertyDescriptors();
            for (int i = 0; pds != null && i < pds.length; i++) {
                if (pds[i].getName().equals("command")){
                    String s = getDefaultPropertyValue(bean, pds[i]);
                    if (s != null) {
                        // XXX Should the format be localizable?
//                        label = label + ": " + s;
                        return s;
                    }
                }
            }
        }else  if (defaultProp != -1) {
            PropertyDescriptor defProp = bi.getPropertyDescriptors()[defaultProp];
            String s = getDefaultPropertyValue(bean, defProp);
            
            if (s != null) {
                // XXX Should the format be localizable?
//                label = label + ": " + s;
                return s;
            }
        } else {
            // Could use this instead:
            //if (FacesSupport.isXhtmlComponent(bean)) {
            // But we want the MarkupBean anyway
//                MarkupBean mb = FacesSupport.getMarkupBean(bean);
            MarkupBean mb = Util.getMarkupBean(bean);
            
            if ((mb != null) && !(mb instanceof FacesBean)) {
                String id = mb.getElement().getAttribute(HtmlAttribute.ID);
                
                if (id.length() == 0) {
                    id = mb.getElement().getAttribute(HtmlAttribute.NAME);
                }
                
                if (id.length() > 0) {
//                    label = label + ": " + id;
                    return id;
                }
            }
        }
        return null;
    }
    
    /** XXX Copied from OutlineTreeRenderer. */
    private static String getDefaultPropertyValue(DesignBean bean, PropertyDescriptor defProp) {
        // Boolean properties are not interesting but it turns
        // out rowsets have autoCommit (a boolean property) as
        // a default so they all show up as personRowset: false
        // etc -- we don't want to see booleans appended
        if (defProp.getPropertyType() == Boolean.TYPE) {
            return null;
        }
        
        DesignProperty prop = bean.getProperty(defProp.getName());
        
        if (prop == null) {
            return null;
        }
        
        String s = prop.getValueSource();
        
        if ((s == null) || (s.length() == 0)) {
            return null;
        }
        
        if (/*FacesSupport.*/isValueBindingExpression(s, false)) {
            return null;
        }
        
//        // I probably SHOULDN'T truncate when we're showing
//        // outputlink urls!
//        int slash = s.lastIndexOf('/');
//        
//        if (slash != -1) {
//            s = s.substring(slash + 1);
//        }
        
//        // Truncate long strings
//        s = DesignerUtils.truncateString(s, 30);
//        s = Util.truncateString(s, 30);
        
        return s;
    }
    
    /** XXX Copied from designer/FacesSupport.
     *
     * Return true if the given String represents a value binding expression.
     * @param s The string to check
     * @param containsOk Iff true, consider a String which has a value binding
     *   expression embedded anywhere as a value binding expression. For example,
     *   "You are #{Session1.age} years old" will return true for this method
     *   if containsOk is set, and false otherwise. If false, only consider
     *   Strings that begin with a value binding expression as being
     *   a value binding expression.
     * @return True iff the given expression is a value binding expression
     *   according to the type indicated by containsOk.
     */
    public static boolean isValueBindingExpression(String s, boolean containsOk) {
        assert s != null;
        
        // TODO: Use
        //  ((FacesDesignProperty)designProperty).isBound()
        // instead - so change to passing in a DesignProperty etc.
        if (containsOk) {
            return s.indexOf("#{") != -1; // NOI18N
        } else {
            return s.startsWith("#{"); // NOI18N
        }
    }
    
    // XXX TODO Figure out a better way then overriding this getter method.
    public Image getIcon(int type) {
        Image img = liveBean.getBeanInfo().getIcon(type);
        if (img == null) {
            img = super.getIcon(type); // Falls back to default icon.
        }
        return img;
    }
    
    // XXX TODO Figure out a better way then overriding this getter method.
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
    
// <actions from layers>
//    protected SystemAction[] systemActions;
// </actions from layers>
    
    public Action[] getActions(boolean context) {
// <actions from layers>
//        if (context) {
//            return super.getActions(context);
//        }
//        if (systemActions == null) {
//
//            ArrayList actions = new ArrayList(20);
//
//        /* !#@#!@#!@#! The NetBeans Action system only allows
//           action singletons - which means I can't do adapter classes
//           that store state about what they're delegating to!
//
//        // Add actions specified by the live bean info, if any
//        DesignInfo lbi = liveBean.getDesignInfo();
//        if (lbi != null) {
//            DisplayAction[] context = lbi.getContextItems(liveBean);
//            for (int i = 0; i < context.length; i++) {
//                DisplayAction action = context[i];
//                if (action instanceof DisplayActionSet) {
//                    DisplayActionSet actionSet = (DisplayActionSet)action;
//                    if (actionSet.isPopup()) {
//                        // Pullright
//                        actions.add(new PullrightAdapter(actionSet, this));
//                    } else {
//                        // It's a "flat" action - just insert its
//                        // children inline. I'm assuming that you
//                        // wouldn't have a flat DisplayActionSet
//                        // inside another flat DisplayActionSet - XXX check
//                        // with Joe if that's a valid assumption (seems
//                        // reasonable to me - why would you "nest" inline
//                        // groups?), if not I've gotta recurse here.
//                        addSeparator(actions);
//                        DisplayAction[] subitems = actionSet.getDisplayActions();
//                        for (int j = 0; j < subitems.length; j++) {
//                            DisplayAction subitem = subitems[j];
//                            if (subitem instanceof DisplayActionSet) {
//                                DisplayActionSet das = (DisplayActionSet)subitem;
//                                assert das.isPopup();
//                                actions.add(new PullrightAdapter(das, this));
//                            } else {
//                                actions.add(new DisplayActionAdapter(subitem));
//                            }
//                        }
//                        addSeparator(actions);
//                    }
//                } else {
//                    actions.add(new DisplayActionAdapter(action));
//                }
//            }
//        }
//        addSeparator(actions);
//        */
//
//            if (liveCustomizer != null) {
//                actions.add(SystemAction.get(CustomizeAction.class));
//                actions.add(null);
//            }
//            // TODO Disable these actions until such time as they can be implemented, see bug #6337233
//            // Using extra local in case we add some additional rules :)
//            boolean addCutCopyPasteActions = true;
//            if (liveBean.getInstance() instanceof RowSet) {
//                addCutCopyPasteActions = false;
//            }
//            if (addCutCopyPasteActions) {
//                actions.add(SystemAction.get(CutAction.class));
//                actions.add(SystemAction.get(CopyAction.class));
//                actions.add(SystemAction.get(PasteAction.class));
//            }
//            actions.add(SystemAction.get(DeleteAction.class));
//            systemActions = (SystemAction[])actions.toArray(new SystemAction[actions.size()]);
//        }
//        return systemActions;
// ====
//        return SystemFileSystemSupport.getActions(PATH_DESIGN_BEAN_NODES_ACTIONS);
        List actions = new ArrayList(Arrays.asList(SystemFileSystemSupport.getActions(PATH_DESIGN_BEAN_NODES_ACTIONS)));

        // XXX #94118 Bad diffing the actions for various nodes.
        if (!Util.isPageRootContainerDesignBean(liveBean)) {
            for (Iterator it = actions.iterator(); it.hasNext(); ) {
                Action action = (Action)it.next();
                if (action != null && action.getValue(ACTION_KEY_PAGE_BEAN_ONLY) == Boolean.TRUE) {
                    it.remove();
                }
            }
        }
        
        // XXX Suspicious postprocessing.
        // TODO Rather provide different subclasses of the node, handling different instances.
        // or better make some node providers for corresponding  beans,
        // reading each from different action folder.
        if (liveCustomizer == null) {
            actions.remove(SystemAction.get(CustomizeAction.class));
        }
        
        boolean addCutCopyPasteActions;
        if (liveBean.getInstance() instanceof RowSet) {
            addCutCopyPasteActions = false;
        } else {
            addCutCopyPasteActions = true;
        }
        if (!addCutCopyPasteActions) {
            actions.remove(SystemAction.get(CutAction.class));
            actions.remove(SystemAction.get(CopyAction.class));
            actions.remove(SystemAction.get(PasteAction.class));
        }
        
        return (Action[])actions.toArray(new Action[actions.size()]);
// </actions from layers>
    }
    
    /** XXX #94118 Hack to differenciate the presence of the actions in the popup.
     * There should be different types for the different nodes. */
    public final static String ACTION_KEY_PAGE_BEAN_ONLY = "actionKeyPageBeanOnly"; // NOI18N
    
    
    public Action getPreferredAction() {
        // #6465174 Make customize action the preferred one if is present.
        Action[] actions = getActions(false);
        Action customizeAction = SystemAction.get(CustomizeAction.class);
        List actionList = Arrays.asList(actions);
        if (actionList.contains(customizeAction)) {
            return customizeAction;
        } else {
            for (Iterator it = actionList.iterator(); it.hasNext(); ) {
                Action action = (Action)it.next();
                if (action != null) {
                    return action;
                }
            }
        }
        return super.getPreferredAction();
    }
    
    
// <actions from layers>
    /** Path to the action folder in the system file system. */
    private static final String PATH_DESIGN_BEAN_NODES_ACTIONS = "DesignBeanNodes/application/x-designtime/Actions"; // NOI18N
    /** Interface to retrieve the actions. */
    interface ActionProvider {
        Action[] getActions();
    }
// </actions from layers>
    
    /**
     * If the bean has a Customizer2, invoke it; otherwise, do nothing.
     */
    public void invokeCustomizer() {
        if (liveCustomizer != null)
            liveCustomizer.getCustomizerPanel(liveBean);
    }
    
    public String toString() {
        return super.toString() + "[liveBean=" + liveBean + "]"; // NOI18N
    }
    
    /**
     * Small class to implement a Node.Property for a DesignBeans's instanceName (id)
     */
    public static class IdLink extends Node.Property {
        DesignBean bean;
        
        IdLink(DesignBean bean) {
            super(String.class);
            this.bean = bean;
            setName(PROPERTY_ID);
            setDisplayName(PROPERTY_ID_DISPLAY);
            // search for property descriptor for id property's and use the info
            PropertyDescriptor[] propertyDescriptors = bean.getBeanInfo().getPropertyDescriptors();
            for (int i = 0; i < propertyDescriptors.length; i++) {
                PropertyDescriptor propertyDescriptor = propertyDescriptors[i];
                if (propertyDescriptor.getName().equals(PROPERTY_ID)) {
                    // the following code is similar to the one in PropLink inner class
                    setDisplayName(propertyDescriptor.getName());
                    Class propType = propertyDescriptor.getPropertyType();
                    String typeName = propType != null ? propType.getName() : "";
                    if (typeName.lastIndexOf(".") > -1)
                        typeName = typeName.substring(typeName.lastIndexOf(".") + 1);
                    String sdesc = propertyDescriptor.getShortDescription();
                    setValue("originalShortDescription", sdesc); // NOI18N
                    setShortDescription("" + propertyDescriptor.getDisplayName() +
                            " (" + typeName + ") \n" + sdesc); // NOI18N
                    break;
                }
            }
        }
        
        public boolean canRead() {
            return true;
        }
        
        public Object getValue() {
            return bean.getInstanceName();
        }
        
        public boolean canWrite() {
            if (((LiveUnit) bean.getDesignContext()).getState() == Unit.State.BUSTED) {
                return false;
            }
            return bean.canSetInstanceName();
        }
        
        public void setValue(Object val) {
            bean.setInstanceName((String)val, true);  // enable auto numbering
        }
        
        public boolean supportsDefaultValue() {
            return false;
        }
        
        public void restoreDefaultValue() {
        }
        
        public boolean isDefaultValue() {
            return false;
        }
        
        public String toString() {
            return "[IL name:" + getName() + " value:" + getValue() + "]";
        }
    }
    
    /**
     * Small class to implement a Node.Property for a DesignProperty
     */
    public static class PropLink extends Node.Property {
        DesignProperty property;
        PropertyDescriptor desc;
        
        public DesignProperty getDesignProperty() {
            return property;
        }
        
        PropLink(DesignProperty property) {
            super(property.getPropertyDescriptor().getPropertyType());
            this.property = property;
            this.desc = property.getPropertyDescriptor();
            setName(desc.getName());
            setDisplayName(desc.getName());
            Class propType = desc.getPropertyType();
            String typeName = propType != null ? propType.getName() : "";
            if (typeName.lastIndexOf(".") > -1)
                typeName = typeName.substring(typeName.lastIndexOf(".") + 1);
            String sdesc = desc.getShortDescription();
            // EAT: Needed by some property editors
            setValue("originalShortDescription", sdesc); // NOI18N
            // EAT: Need to escape the descriptions
            //  This is done in a couple of lcass, we should REALLY put this type of code
            //    in one place.  Another location this is done is com.sun.rave.toolbox.PaletteItemButton.initialize()
            setShortDescription("" + desc.getDisplayName() + " (" + typeName + ") \n" + sdesc);
            setExpert(desc.isExpert());
            setHidden(desc.isHidden());
            setPreferred(desc.isPreferred());
        }
        
        public boolean canRead() {
            return desc.getReadMethod() != null;
        }
        
        public Object getValue() {
            return property.getValue();
        }
        
        public boolean canWrite() {
            if (((LiveUnit) property.getDesignBean().getDesignContext()).getState() == Unit.State.BUSTED) {
                return false;
            }
            return desc.getWriteMethod() != null;
        }
        
        public void setValue(Object val) {
            //System.err.println("PL.setValue val:" + val + " => this:" + this);
            property.setValue(val);
        }
        
        public boolean supportsDefaultValue() {
            return canRead() && canWrite();
        }
        
        public void restoreDefaultValue() {
            property.unset();
        }
        
        public boolean isDefaultValue() {
            return !property.isModified();
        }
        
        public String getHtmlDisplayName() {
            if (isDefaultValue()) {
                return null;
            } else {
                return "<b>" + getDisplayName(); // NOI18N
            }
        }
        
        //Soft caching of property editor references to improve JTable
        //property sheet performance
        SoftReference propertyEditorRef=null;
        
        public PropertyEditor getPropertyEditor() {
            PropertyEditor propertyEditor = null;
            
            if (propertyEditorRef != null) {
                propertyEditor = (PropertyEditor) propertyEditorRef.get();
            }
            
            if (propertyEditor == null) {
                propertyEditor = ((BeansDesignProperty)property).getPropertyEditor();
                // Is this a FacesDesignProperty i.e. a bindable property
                if (property instanceof FacesDesignProperty) {
                    if (propertyEditor instanceof FacesBindingPropertyEditor){
                        // The property editor knows how to deal with FacesDesignProperty so just return it.
                        //
                    } else if (propertyEditor instanceof AttributePropertyEditor){
                        // The property editor is a editor for markup attributes. Wrap it in a special value binding property editor which knows how to deal with markup attributes.
                        ValueBindingAttributePropertyEditor valueBindingAttributePropertyEditor = new ValueBindingAttributePropertyEditor((AttributePropertyEditor)propertyEditor);
                        valueBindingAttributePropertyEditor.setDesignProperty(property);
                        propertyEditor = valueBindingAttributePropertyEditor;
                    } else {
                        // Wrap the property editor in a special value binding property editor.
                        ValueBindingPropertyEditor valueBindingPropertyEditor = new ValueBindingPropertyEditor(propertyEditor);
                        valueBindingPropertyEditor.setDesignProperty(property);
                        propertyEditor = valueBindingPropertyEditor;
                    }
                }
            }
            
            propertyEditorRef = new SoftReference(propertyEditor);
            return propertyEditor;
        }
        
        public String toString() {
            return "[PL name:" + getName() + " type:" + getValueType() + " value:" + getValue() + "]";
        }
        
        private static final ImageIcon BOUND_ICON = new ImageIcon(PropLink.class.getResource("bound.png"));
        
        // EAT: I talked to Joe about this and he did not like it, BUT I need it in order to
        // have some of the values set on the property being linked? to be passed through
        public Object getValue(String attributeName) {
            Object result = super.getValue(attributeName);
            // Did super return anything?
            if (result == null) {
                // Is this a FacesDesignProperty
                if (property instanceof FacesDesignProperty) {
                    if ("changeImmediate".equals(attributeName)) {
                        // For facesDesignProperties the ValueBindingPropertyEditor wraps the propertye descriptor returned PropertyEditor.
                        // Update the property value when the user clicks OK button in the CustomEditor dialog.
                        return Boolean.FALSE;
                    }
                    // Is the property bound?
                    if (((FacesDesignProperty)property).isBound()) {
                        // If the property editor is not an instance of FacesBindingPropertyEditor then
                        // show special icon and disallow in place editing. If the property editor is
                        // an instance of FacesBindingPropertyEditor then let it deal with the property sheet
                        if (!(((BeansDesignProperty)property).getPropertyEditor() instanceof FacesBindingPropertyEditor)) {
                            // Is the Property Sheet asking for valueIcon?
                            if ("valueIcon".equals(attributeName)) {
                                // return bound icon
                                return BOUND_ICON;
                            } else
                                // Is the Property Sheet asking for canEditAsText for inline editing?
                                if ("tooltip".equals(attributeName)) {
                                // Prevent inline editing
                                return ((FacesDesignProperty)property).getValueSource();
                                } else
                                    // Is the Property Sheet asking for canEditAsText for inline editing?
                                    if ("canEditAsText".equals(attributeName)) {
                                // Prevent inline editing
                                return Boolean.FALSE;
                                    }
                        }
                    }
                }
            }
            if (result == null && property.getPropertyDescriptor() != null)
                result = property.getPropertyDescriptor().getValue(attributeName);
            return result;
        }
    }
    
    /**
     * Small class to implement a Node.Property given a DesignProperty,.
     */
    public static class EventLink extends Node.Property {
        DesignEvent event;
        EventDescriptor desc;
        EventPropertyEditor editor;
        
        EventLink(DesignEvent event) {
            super(String.class);
            this.event = event;
            this.desc = event.getEventDescriptor();
            EventSetDescriptor esd = desc.getEventSetDescriptor();
            String name = desc.getName();
            String displayName = desc.getDisplayName();
            String shortDescription = desc.getShortDescription();
            if (esd != null) {
                if (esd.getName() != null)
                    name = esd.getName();
                if (esd.getDisplayName() != null)
                    displayName = esd.getDisplayName();
                if (esd.getShortDescription() != null)
                    // TODO - Use Entities.escape()
                    shortDescription = displayName + " - \n" + esd.getShortDescription();
            }
            setName(name);
            setDisplayName(name);
            setShortDescription(shortDescription);
            setExpert(desc.isExpert());
            setHidden(desc.isHidden());
            setPreferred(desc.isPreferred());
        }
        
        public boolean canRead() {
            return true;
        }
        
        public Object getValue() {
            return event.getHandlerName();
        }
        
        public boolean canWrite() {
            return true;
        }
        
        public void setValue(Object val) {
            if (val != null && !(val instanceof String))
                throw new IllegalArgumentException();
            event.setHandlerName((String)val);
        }
        
        public boolean supportsDefaultValue() {
            return true; //!CQ ? revert()/isModified()
        }
        
        public void restoreDefaultValue() {
            event.removeHandler();
        }
        
        public boolean isDefaultValue() {
            return !event.isHandled();
        }
        
        public PropertyEditor getPropertyEditor() {
            if (event instanceof BeansDesignEvent) {
                if (editor == null)
                    editor = new EventPropertyEditor((BeansDesignEvent)event);
                return editor;
            }
            return PropertyEditorManager.findEditor(String.class);
        }
        
        public String toString() {
            return "[EL name:" + getName() + " type:" + getValueType() + " value:" + getValue() + "]";
        }
    }
    
    /*
     * @see com.sun.rave.designtime.DesignBeanListener#beanContextActivated(com.sun.rave.designtime.DesignBean)
     */
    public void beanContextActivated(DesignBean designBean) {}
    
    /*
     * @see com.sun.rave.designtime.DesignBeanListener#beanContextDeactivated(com.sun.rave.designtime.DesignBean)
     */
    public void beanContextDeactivated(DesignBean designBean) {}
    
    /*
     * @see com.sun.rave.designtime.DesignBeanListener#instanceNameChanged(com.sun.rave.designtime.DesignBean, java.lang.String)
     */
    public void instanceNameChanged(DesignBean designBean, String oldInstanceName) {}
    
    /*
     * @see com.sun.rave.designtime.DesignBeanListener#beanChanged(com.sun.rave.designtime.DesignBean)
     */
    public void beanChanged(final DesignBean bean) {
        niceFirePropertyChange(PROPERTY_ID, null, bean.getInstanceName());
        //fix 5055048 Setting id on a tray component does not change identifier in the tray
        displayNameChanged();
    }
    
    private void displayNameChanged() {
        final DesignBean bean = liveBean;
        if (bean == null) {
            return;
        }
        if (SwingUtilities.isEventDispatchThread()) {
            fireDisplayNameChange(null, bean.getInstanceName());
        } else {
            SwingUtilities.invokeLater(new Runnable(){
                public void run() {
                    fireDisplayNameChange(null, bean.getInstanceName());
                }
            });
        }
    }
    
    /*
     * @see com.sun.rave.designtime.DesignBeanListener#propertyChanged(com.sun.rave.designtime.DesignProperty)
     */
    public void propertyChanged(DesignProperty prop, Object oldValue) {
        niceFirePropertyChange(prop.getPropertyDescriptor().getName(), oldValue, prop.getValue());
        displayNameChanged();
    }
    
    /*
     * @see com.sun.rave.designtime.DesignBeanListener#eventChanged(com.sun.rave.designtime.DesignEvent)
     */
    public void eventChanged(DesignEvent event) {
        niceFirePropertyChange(event.getEventDescriptor().getDisplayName(), null, event.getHandlerName());
    }
    
    /**
     * Fire a propertychange event, making sure it is delivered in the Swing event dispatch thread
     * so that NB doesn't FREAK out.
     *
     * @param name
     * @param o
     * @param n
     */
    protected void niceFirePropertyChange(final String name, final Object o, final Object n) {
        if (SwingUtilities.isEventDispatchThread()) {
            firePropertyChange(name, o, n);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        firePropertyChange(name, o, n);
                    } catch (Exception e) {
                        //ErrorManager.getDefault().notify(e);
                    }
                }
            });
        }
    }
    
    /**
     *
     */
    protected Set getSheetSet(Sheet sheet, String name, String descr) {
        Set ss = sheet.get(name);
        if (ss == null) {
            ss = new Sheet.Set();
            ss.setName(name);
            ss.setDisplayName(name);
            //ss.setExpert();
            // would like to set default expanded state too...
            if (descr != null)
                ss.setShortDescription(descr);
            sheet.put(ss);
        }
        return ss;
    }
    
    /**
     * Creates properties.
     */
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Set general = getSheetSet(sheet, NbBundle.getMessage(DesignBeanNode.class, GENERAL),
                NbBundle.getMessage(DesignBeanNode.class, GENERAL_HINT));
        
        IdLink il = new IdLink(liveBean);
        general.put(il);
        
        // prepopulate the sheet-set list from the beandescriptor categories
        Object catlistO = liveBean.getBeanInfo().getBeanDescriptor().getValue("propertyCategories");  //NOI18N
        if (catlistO instanceof CategoryDescriptor[]) {
            CategoryDescriptor[] catlist = (CategoryDescriptor[])catlistO;
            for (int i = 0; i < catlist.length; i++)
                getSheetSet(sheet, catlist[i].getName(), catlist[i].getShortDescription());
        }
        
        // Create property links for all properties in the live bean
        DesignProperty[] props = liveBean.getProperties();
        assert Trace.trace("insync.live", "LBN.createSheet for " + liveBean + " w/ " + props.length + " props");  //NOI18N
        
        for (int i = 0; i < props.length; i++) {
            PropertyDescriptor pd = props[i].getPropertyDescriptor();
            if (pd.isHidden()) // XXX Why not?|| pd.getName().equals(PROPERTY_ID))
                continue;
            
            PropLink pl = new PropLink(props[i]);
            Object catO = pd.getValue("category");  //NOI18N
            if (catO instanceof CategoryDescriptor) {
                CategoryDescriptor cat = (CategoryDescriptor)catO;
                Set ss = getSheetSet(sheet, cat.getName(), cat.getShortDescription());
                ss.put(pl);
            } else {
                general.put(pl);
            }
            //assert Trace.trace("insync.live", "  " + pl + " => " + props[i]);
        }
        
        // Create event links for all events in the live bean
        Set ess = getSheetSet(sheet, NbBundle.getMessage(DesignBeanNode.class, EVENTS),
                NbBundle.getMessage(DesignBeanNode.class, EVENTS_HINT));
        
        DesignEvent[] events = liveBean.getEvents();
        assert Trace.trace("insync.live", "LBN.createSheet for " + liveBean + " w/ " + events.length +
                " events");  //NOI18N
        
        for (int i = 0; i < events.length; i++) {
            EventDescriptor ed = events[i].getEventDescriptor();
            if (ed.isHidden() || (ed.getEventSetDescriptor() != null && ed.getEventSetDescriptor().isHidden()))
                continue;
            
            EventLink el = new EventLink(events[i]);
            ess.put(el);
            assert Trace.trace("insync.live", "  " + el + " => " + events[i]);  //NOI18N
        }
        
        // XXX If the bean represents the root container for Page, add some fake properties.
        if (Util.isPageRootContainerDesignBean(liveBean)) {
            DesignBeanNodeHelper.addFakePageProperties(sheet, liveBean);
        }
        
        return sheet;
    }
    
    public NewType[] getNewTypes() {
        return createNewTypes(liveBean);
    }
    
    private static NewType[] createNewTypes(DesignBean designBean) {
        if (designBean instanceof SourceLiveRoot) {
            return createRootNewTypes(designBean);
        }
        
        return createDefaultNewTypes(designBean);
    }
    
    
    // XXX NB #84384 Caching the new types for the java data object, for better result, see below.
    private static final Map javaDataObject2newTypes = new WeakHashMap();
    // XXX  #6448518 Very hacky method to retrieve the new types
    // from java | class | bean patterns node, and passing them here
    // to fake the old functionality.
    private static NewType[] createRootNewTypes(DesignBean designBean) {
        DesignContext designContext = designBean.getDesignContext();
        if (designContext == null) {
            return new NewType[0];
        }
        
        FacesModel facesModel = ((LiveUnit)designContext).getModel();
        if (facesModel == null) {
            return new NewType[0];
        }
        
        FileObject javaFileObject = facesModel.getJavaFile();
        if (javaFileObject == null) {
            return new NewType[0];
        }
        
        DataObject javaDataObject;
        try {
            javaDataObject = DataObject.find(javaFileObject);
        } catch (DataObjectNotFoundException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return new NewType[0];
        }
        if (javaDataObject == null) {
            return new NewType[0];
        }
        
        synchronized (javaDataObject2newTypes) {
            NewType[] newTypes = (NewType[])javaDataObject2newTypes.get(javaDataObject);
            if (newTypes != null) {
                return newTypes;
            }
        }
        
        Node javaNode = javaDataObject.getNodeDelegate();
//        // XXX NB #84384 Horrible hack: This needs to be here to spawn the process to init the nodes,
//        // in order bypass the temporary Please wait... nodes.
//        // Also we hope that the process (running in parallel RP), will be posted
//        // on time, so it is running when we ask for findChild, see below, (which waits for it).
//        // This is still not working 100%.
//        // See NB sources: java/src/../SourceChildren.
//        javaNode.getChildren().getNodes(true);
//        invokeAddNotifyOnChildren(javaNode.getChildren());
        
        NewType[] javaTypes = javaNode.getNewTypes();
        List typesJava = new ArrayList();
        for (int i = 0; i < javaTypes.length; i++) {
            if (i == 0 || i == 2) { // 1st and 3rd
                typesJava.add(javaTypes[i]);
            }
        }
        
        String designBeanName = designBean.getInstanceName();
        if (designBeanName == null) {
            return new NewType[0];
        }
        Node javaClassNode = javaNode.getChildren().findChild(designBeanName);
        if (javaClassNode == null) {
            return new NewType[0];
        }
        
        Node[] classChildren = javaClassNode.getChildren().getNodes(true);
        // XXX The last is the bean property node.
        Node beanNode = classChildren == null || classChildren.length == 0
                ? null
                : classChildren[classChildren.length - 1];
        if (beanNode == null) {
            return new NewType[0];
        }
        
        List types = new ArrayList();
        // XXX Take only first two (supposing property and indexed property).
        NewType[] beanTypes = beanNode.getNewTypes();
        for (int i = 0; i < beanTypes.length; i++) {
            types.add(beanTypes[i]);
            if (i == 1) { // Add first 2 only.
                break;
            }
        }
        types.addAll(typesJava);
        
        NewType[] newTypes = (NewType[])types.toArray(new NewType[types.size()]);
        synchronized (javaDataObject2newTypes) {
            javaDataObject2newTypes.put(javaDataObject, newTypes);
        }
        return newTypes;
    }
    
//    // XXX Using reflection for the hack, to spawn the process of initing the child nodes.
//    private static void invokeAddNotifyOnChildren(Children children) {
//        try {
//            Method method = Children.class.getDeclaredMethod("addNotify", new Class[0]); // NOI18N
//            method.setAccessible(true);
//            method.invoke(children, new Object[0]);
//        } catch (SecurityException ex) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//        } catch (NoSuchMethodException ex) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//        } catch (IllegalArgumentException ex) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//        } catch (IllegalAccessException ex) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//        } catch (InvocationTargetException ex) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//        }
//    }
    
    private static NewType[] createDefaultNewTypes(DesignBean designBean) {
        String[] preferredChildren;
        BeanDescriptor bd = designBean.getBeanInfo().getBeanDescriptor();
        if (bd != null) {
            preferredChildren = (String[])bd.getValue(Constants.BeanDescriptor.PREFERRED_CHILD_TYPES);
        } else {
            preferredChildren = new String[0];
        }
        
        // XXX Why the api returns null instead of empty array?
        if (preferredChildren == null) {
            return new NewType[0];
        }
        
        List newTypes = new ArrayList();
        for (int i = 0; i < preferredChildren.length; i++) {
            newTypes.add(new DesignBeanNewType(designBean, preferredChildren[i]));
            
//            if (preferredChildren.length == 1) {
//                // Add a single inline item
//                String className = preferredChildren[0];
//                String displayName = getBeanDisplayName(className);
//
//                if (displayName != null) {
//                    String label =
//                        NbBundle.getMessage(DesignerActions.class, "AddOneItem", displayName);
//                    JMenuItem item = new JMenuItem(label);
//                    item.putClientProperty(CHILD_PROP, className);
//                    item.addActionListener(new AddItemHandler(bean));
//
//                    //menu.add(item);
//                    addItem = item; // Added in the middle of the bean-specific menus
//                }
//            } else {
//                // Pullright
//                JMenu submenu =
//                    new JMenu(NbBundle.getMessage(SelectionManager.class, "AddItem")); // NOI18N
//                submenu.putClientProperty(BEAN_PROP, bean);
//
//                //menu.add(submenu);
//                addItem = submenu; // Added in the middle of the bean-specific menus
//                submenu.addMenuListener(this); // submenu contents created dynamically
//            }
        }
        
        return (NewType[])newTypes.toArray(new NewType[newTypes.size()]);
    }
    
    
    private static class DesignBeanNewType extends NewType {
        private final DesignBean parent;
        private final String className;
        
        public DesignBeanNewType(DesignBean parent, String className) {
            this.parent = parent;
            this.className = className;
        }
        
        public void create() throws IOException {
            LiveUnit liveUnit = (LiveUnit)parent.getDesignContext();
            if (liveUnit == null) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                        new NullPointerException("No LiveUnit for designBean=" + parent)); // NOI18N
                return;
            }
            FacesModel facesModel = liveUnit.getModel();
            UndoEvent undoEvent = facesModel.writeLock(getName());
            try {
                DesignBean bean = liveUnit.createBean(className, parent, null);
                
                if (bean != null) {
                    try {
                        facesModel.beanCreated(bean);
                    } catch (Exception e) {
                        // XXX Why is catching Exception, just following the old hacky code.
                        e.printStackTrace();
                    }
                }
                
//                List beans = new ArrayList(1);
//                beans.add(bean);
                DesignBean[] beans = new DesignBean[] {bean};
                
//                DndHandler dnd = webform.getPane().getDndHandler();
//                dnd.customizeCreation(beans);
                Util.customizeCreation(beans, facesModel);
////                dnd.selectBean(bean);
//                webform.getSelection().selectBean(bean);
////                dnd.inlineEdit(beans);
//                webform.getManager().inlineEdit(beans);
                
//                facesModel.getDnDSupport().notifyBeansDesigner(beans, bean);
                facesModel.getJsfSupport().selectAndInlineEdit(beans, bean);
            } finally {
//                doc.writeUnlock();
                facesModel.writeUnlock(undoEvent);
            }
        }
        
        public String getName() {
            LiveUnit unit = (LiveUnit)parent.getDesignContext();
            return getBeanDisplayName(unit, className);
        }
    } // End of DesignBeanNewType.
    
    private static String getBeanDisplayName(LiveUnit unit, String className) {
        BeansUnit sourceUnit = unit.getSourceUnit();
        try {
            Class beanClass = sourceUnit.getBeanClass(className);
            BeanInfo beanInfo = BeansUnit.getBeanInfo(beanClass, unit.getModel().getFacesModelSet().getProjectClassLoader());
            
            if (beanInfo != null) {
                BeanDescriptor bds = beanInfo.getBeanDescriptor();
                
                if (bds != null) {
                    String displayName = bds.getDisplayName();
                    
                    return displayName;
                }
            }
        } catch (ClassNotFoundException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        
        return null;
    }
    
    
    /**
     * Children object for a list of live beans
     */
    public static class BeanChildren extends Children.Keys implements DesignContextListener, Index {
        
        /** Permits changing order of children. */
        private Index indexSupport;
        
        /** Optional holder for the keys, to be used when changing them dynamically. */
        protected DesignBean parent;
        
        public BeanChildren(DesignBean parent) {
            this.parent = parent;
            indexSupport = new IndexSupport();
        }
        
        protected void refreshKeys() {
            DesignBean[] lbeans = parent.getChildBeans();
            if (lbeans == null || lbeans.length == 0) {
                setKeys(Collections.EMPTY_SET);
                return;
            }
            
            List myKeys = new LinkedList();
            assert Trace.trace("insync.live", "LBN.refreshKeys refreshing keys for " + lbeans.length
                    + " beans");
            for (int i = 0; i < lbeans.length; i++) {
                assert Trace.trace("insync.live", "   bean:" + lbeans[i]);
                myKeys.add(lbeans[i]);
            }
            setKeys(myKeys);
        }
        
        /**
         * Called when the parent node is expanded; now we need to create nodes for the children.
         */
        protected void addNotify() {
            assert Trace.trace("insync.live", "LBN.addNotify");
            super.addNotify();
            parent.getDesignContext().addDesignContextListener(this);
            refreshKeys();
        }
        
        /**
         * Called when the parent node is collapsed: cleanup
         */
        protected void removeNotify() {
            assert Trace.trace("insync.live", "LBN.removeNotify");
            parent.getDesignContext().removeDesignContextListener(this);
            setKeys(Collections.EMPTY_SET);
            super.removeNotify();
        }
        
        /**
         * Create nodes for the specified key object
         */
        protected Node[] createNodes(Object key) {
            DesignBean lbean = (DesignBean)key;
            Node node = ((SourceDesignBean)lbean).getNode();
            return new Node[] { node };
        }
        
        /** Reacts to changes */
        
        public void beanContextActivated(DesignBean designBean) {}
        public void beanContextDeactivated(DesignBean designBean) {}
        public void instanceNameChanged(DesignBean designBean, String oldInstanceName) {}
        
        public void beanChanged(DesignBean lbean) {
            assert Trace.trace("insync.live", "LBN.beanChanged " + lbean);
            if (lbean.getBeanParent() == parent)
                refreshKeys();
        }
        
        public void propertyChanged(DesignProperty prop, Object oldValue) {}
        public void eventChanged(DesignEvent event) {}
        
        public void contextActivated(DesignContext context) {}
        public void contextDeactivated(DesignContext context) {}
        public void contextChanged(DesignContext context) {
            refreshKeys();
        }
        
        public void beanCreated(DesignBean lbean) {
            assert Trace.trace("insync.live", "LBN.beanCreated " + lbean);
            if (lbean.getBeanParent() == parent)
                refreshKeys();
        }
        
        public void beanMoved(DesignBean lbean, DesignBean oldParent, Position pos) {
            assert Trace.trace("insync.live", "LBN.beanMoved " + lbean);
            if (lbean.getBeanParent() == parent || oldParent == parent) {
                // #6480729 First refresh the old parent to get rid of the moved node.
                refreshKeysForDesignBean(oldParent);
                refreshKeys();
            }
        }
        
        private static void refreshKeysForDesignBean(DesignBean designBean) {
            if (designBean instanceof SourceDesignBean) {
                DesignBeanNode designBeanNode = ((SourceDesignBean)designBean).getNode();
                Children children = designBeanNode.getChildren();
                if (children instanceof BeanChildren) {
                    ((BeanChildren)children).refreshKeys();
                }
            }
        }
        
        
        public void beanDeleted(DesignBean lbean) {
            assert Trace.trace("insync.live", "LBN.beanDeleted " + lbean);
            if (lbean.getBeanParent() == parent)
                refreshKeys();
        }
        
        public void addChangeListener(ChangeListener l) {
            indexSupport.addChangeListener(l);
        }
        
        public void removeChangeListener(ChangeListener l) {
            indexSupport.removeChangeListener(l);
        }
        
        public void exchange(int x, int y) {
            indexSupport.exchange(x, y);
        }
        
        public int indexOf(Node node) {
            return indexSupport.indexOf(node);
        }
        
        public void moveUp(int i) {
            indexSupport.moveUp(i);
        }
        
        public void moveDown(int i) {
            indexSupport.moveDown(i);
        }
        
        public void move(int x, int y) {
            indexSupport.move(x, y);
        }
        
        public void reorder() {
            indexSupport.reorder();
        }
        
        public void reorder(int[] i) {
            indexSupport.reorder(i);
        }
        
        /**
         * Allows re-ordering of the child nodes.
         */
        private class IndexSupport extends Index.Support {
            
            public void reorder(final int[] perm) {
                // Call the reorder on EQT later so that the Explorer Nodes get a chance to adjust
                // after insertion
                SwingUtilities.invokeLater(
                        new Runnable() {
                    public void run() {
                        ((LiveUnit) parent.getDesignContext()).reorderBeanChidren(parent, perm);
                    }
                });
            }
            
            public int getNodesCount() {
                return BeanChildren.this.getNodesCount();
            }
            
            public Node[] getNodes() {
                return BeanChildren.this.getNodes();
            }
        }
    }
    
    
    /** Determines whether the <code>DesignBean</code> represents head element. */
    private static boolean isHeadDesignBean(DesignBean designBean) {
        if (designBean == null) {
            return false;
        }
        
        MarkupBean markup = Util.getMarkupBean(designBean);
        if (markup == null) {
            return false;
        }
        
        Element element = markup.getElement();
        Element rendered = MarkupService.getRenderedElementForElement(element);
        if (rendered == null) {
            return false;
        }
        
        return HtmlTag.HEAD.getTagName().equals(rendered.getTagName());
    }
    
    
    private static class DesignBeanNodeDesignContextListener implements DesignContextListener {
        private final DesignBeanNode designBeanNode;
        
        public DesignBeanNodeDesignContextListener(DesignBeanNode designBeanNode) {
            this.designBeanNode = designBeanNode;
        }
        
        public void contextActivated(DesignContext designContext) {}

        public void contextDeactivated(DesignContext designContext) {}

        public void contextChanged(DesignContext designContext) {
            designBeanNode.displayNameChanged();
        }

        public void beanCreated(DesignBean designBean) {}

        public void beanDeleted(DesignBean designBean) {}

        public void beanMoved(DesignBean designBean, DesignBean designBean0, Position position) {}

        public void beanContextActivated(DesignBean designBean) {}

        public void beanContextDeactivated(DesignBean designBean) {}

        public void instanceNameChanged(DesignBean designBean, String string) {}

        public void beanChanged(DesignBean designBean) {}

        public void propertyChanged(DesignProperty designProperty, Object object) {}

        public void eventChanged(DesignEvent designEvent) {}
    } // End of DesingBeanNodeDesignContextListener.
}

