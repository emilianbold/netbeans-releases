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


import org.netbeans.modules.visualweb.api.designer.Designer;
import org.netbeans.modules.visualweb.api.designer.DesignerFactory;
import org.netbeans.modules.visualweb.api.designer.HtmlDomProvider;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignEvent;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.Position;
import com.sun.rave.designtime.event.DesignContextListener;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import com.sun.rave.designtime.markup.MarkupMouseRegion;
import org.netbeans.modules.visualweb.insync.FacesDnDSupport;
//NB60 import org.netbeans.modules.visualweb.insync.faces.refactoring.MdrInSyncSynchronizer;
import org.netbeans.modules.visualweb.insync.live.LiveUnit;
import org.netbeans.modules.visualweb.insync.markup.MarkupUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;
import org.netbeans.api.project.Project;
import org.netbeans.spi.palette.PaletteController;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.WeakListeners;
import org.openide.util.WeakSet;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * Represents JSF form. Something like WebForm before, but only the JSF specific part of it.
 *
 * @author Peter Zavadsky
 * @author Tor Norbye (the original code copied from the old WebForm)
 */
class JsfForm {

    /** Weak <code>Map</code> between <code>FacesModel</code> and <code>JsfForm</code>. */
    private static final Map<FacesModel, JsfForm> facesModel2jsfForm = new WeakHashMap<FacesModel, JsfForm>();

    /** Weak <code>Map</code> between <code>JsfForm</code> and <code>Designer</code>.
     * TODO to make it map between <code>JsfForm</code> and <code><Designer>Set</code>. */
    private static final Map<JsfForm, Set<Designer>> jsfForm2designerSet = new WeakHashMap<JsfForm, Set<Designer>>();


    /** <code>FacesModel</code> associated with this JSF form. */
    private FacesModel facesModel;
//    /** <code>Designer</code> associated with this JSF form. */
//    private final Designer designer;

    private DomSynchronizer domSynchronizer;

//    private DesignContext designContext;

    private final PropertyChangeListener dataObjectListener = new DataObjectPropertyChangeListener(this);

    private /*final*/ DesignContextListener designContextListener /*= new JsfDesignContextListener(this)*/;

    private final boolean isFragment;
    private final boolean isPortlet;

    private final EventListenerList listenerList = new EventListenerList();

    private final PaletteController paletteController;

    private final HtmlDomProvider htmlDomProvider = new HtmlDomProviderImpl(this);
    
    private final DndSupport dndSupport = new DndSupport(this);

    // XXX Bad (old style) error handling.
    private Exception renderFailureException;
    // XXX Bad (old style) error handling.
    private MarkupDesignBean renderFailureComponent;


    /** Creates a new instance of JsfForm */
    private JsfForm(FacesModel facesModel, DataObject dataObject) {
        if (facesModel == null) {
            throw new NullPointerException("FacesModel may not be null!"); // NOI18N
        }

//        associateFacesModel(dataObject.getPrimaryFile());
        synchronized (facesModel2jsfForm) {
            this.facesModel = facesModel;
        }
        
        this.paletteController = PaletteControllerFactory.getDefault().createJsfPaletteController(facesModel.getProject());
        
        // Set isFragment/isPortlet fields.
        FileObject fo = dataObject.getPrimaryFile();
        this.isFragment = "jspf".equals(fo.getExt()); // NOI18N
        if (facesModel.getFacesModelSet() != null) {
            this.isPortlet = facesModel.getFacesModelSet().getFacesContainer().isPortletContainer();
        } else {
            this.isPortlet = false;
        }
        
//        this.designer = DesignerProvider.getDesigner(this);

        // Set listening.
        dataObject.addPropertyChangeListener(WeakListeners.propertyChange(dataObjectListener, dataObject));
        updateDnDListening();
    }
    
    
    private static FacesModel getFacesModel(FileObject fileObject) {
        return FacesModel.getInstance(fileObject);
    }
    
    private static FacesModel getFacesModel(DataObject dataObject) {
        if (dataObject == null) {
            throw new NullPointerException("DataObject may not be null!"); // NOI18N
        }
        
        return getFacesModel(dataObject.getPrimaryFile());
    }
        
    public static JsfForm getJsfForm(DataObject dataObject) {
        if (dataObject == null) {
            return null;
        }
        
        FacesModel facesModel = getFacesModel(dataObject);
        if (facesModel == null) {
            if (!dataObject.isTemplate()) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                        new IllegalArgumentException("There is no FacesModel available for non-template dataObject=" + dataObject)); // NOI18N
            }
            return null;
        }
        
        JsfForm jsfForm;
        synchronized (facesModel2jsfForm) {
            jsfForm = facesModel2jsfForm.get(facesModel);
            
            if (jsfForm == null) {
                jsfForm = new JsfForm(facesModel, dataObject);
                facesModel2jsfForm.put(facesModel, jsfForm);
            }
        }
        return jsfForm;
    }
    
    public static Designer createDesigner(DataObject jsfJspDataObject) {
        JsfForm jsfForm = JsfForm.getJsfForm(jsfJspDataObject);
        if (jsfForm == null) {
            return null;
        }
        
        return createDesigner(jsfForm);
    }

    private static Designer[] findDesigners(JsfForm jsfForm) {
        Set<Designer> designerSet;
        synchronized (jsfForm2designerSet) {
            designerSet = jsfForm2designerSet.get(jsfForm);
        }
        if (designerSet != null) {
            // XXX To be sure there are not lost some weak refs.
            designerSet = new HashSet<Designer>(designerSet);
        }
        return designerSet == null ? new Designer[0] : designerSet.toArray(new Designer[designerSet.size()]);
    }
    
    private static Designer createDesigner(JsfForm jsfForm) {
        // TODO There should be always created new designer.
        Designer designer;
        synchronized (jsfForm2designerSet) {
            Set<Designer> designerSet = jsfForm2designerSet.get(jsfForm);
            if (designerSet == null) {
                designerSet = new WeakSet/*<Designer>*/();
            }
            
            designer = DesignerFactory.createDesigner(jsfForm.getHtmlDomProvider());
            designerSet.add(designer);
            jsfForm2designerSet.put(jsfForm, designerSet);
        }
        return designer;
    }
    
    static Designer[] getDesignersForDataObject(DataObject jsfJspDataObject) {
        JsfForm jsfForm = getJsfForm(jsfJspDataObject);
        if (jsfForm == null) {
            return new Designer[0];
        }
        Designer[] designers = findDesigners(jsfForm);
        if (designers.length == 0) {
            Designer designer = createDesigner(jsfForm);
            return new Designer[] {designer};
        }
        
        return designers;
    }

    static Designer[] findDesignersForFileObject(FileObject jsfJspFileObject) {
        JsfForm jsfForm = findJsfForm(jsfJspFileObject);
        if (jsfForm == null) {
            return new Designer[0];
        }
        return findDesigners(jsfForm);
    }

    static Designer[] findDesignersForDesignContext(DesignContext designContext) {
        JsfForm jsfForm = findJsfForm(designContext);
        if (jsfForm == null) {
            return new Designer[0];
        }
        return findDesigners(jsfForm);
    }

    static Designer[] findDesignersForElement(Element element) {
        JsfForm jsfForm = findJsfForm(element);
        if (jsfForm == null) {
            return new Designer[0];
        }
        return findDesigners(jsfForm);
    }
    
    static JsfForm findJsfForm(DataObject dataObject) {
        if (dataObject == null) {
            return null;
        }
        
        FacesModel facesModel = getFacesModel(dataObject);
        if (facesModel == null) {
            return null;
        }
        return getJsfForm(facesModel);
    }

    private static JsfForm findJsfForm(FileObject fileObject) {
        if (fileObject == null) {
            return null;
        }
        
        FacesModel facesModel = getFacesModel(fileObject);
        if (facesModel == null) {
            return null;
        }
        return getJsfForm(facesModel);
    }
    
    private static JsfForm findJsfForm(DesignContext designContext) {
        if (designContext == null) {
            return null;
        }
        
        FacesModel facesModel = ((LiveUnit)designContext).getModel();
        if (facesModel == null) {
            return null;
        }
        return getJsfForm(facesModel);
    }
    
    private static JsfForm findJsfForm(Element element) {
        MarkupDesignBean markupDesignBean = MarkupUnit.getMarkupDesignBeanForElement(element);
        if (markupDesignBean == null) {
            return null;
        }
        DesignContext designContext = markupDesignBean.getDesignContext();
        return findJsfForm(designContext);
    }
    
    private static JsfForm getJsfForm(FacesModel facesModel) {
        synchronized (facesModel2jsfForm) {
            return facesModel2jsfForm.get(facesModel);
        }
    }

    
    HtmlDomProvider getHtmlDomProvider() {
        return htmlDomProvider;
    }

//    static boolean hasHtmlDomProvider(DataObject dataObject) {
//        if (dataObject == null) {
//            return false;
//        }
//        
//        FacesModel facesModel = getFacesModel(dataObject);
//        return hasJsfForm(facesModel);
//    }
//    
//    private static boolean hasJsfForm(FacesModel facesModel) {
//        synchronized (facesModel2jsfForm) {
//            return facesModel2jsfForm.containsKey(facesModel);
//        }
//    }
    
//    static boolean hasJsfForm(DataObject dataObject) {
//        FacesModel facesModel = getFacesModel(dataObject);
//        synchronized (facesModel2jsfForm) {
//            // XXX Also check whether this model is still valid (in the FacesModelSet).
//            return facesModel2jsfForm.containsKey(facesModel);
//        }
//    }

//    static Designer findDesignerForDesignContext(DesignContext designContext) {
//        FacesModel facesModel = ((LiveUnit)designContext).getModel();
//        
//        JsfForm jsfForm;
//        synchronized (facesModel2jsfForm) {
//            jsfForm = facesModel2jsfForm.get(facesModel);
//        }
//        return jsfForm == null ? null : jsfForm.getDesigner();
//    }
//    
//    static Designer findDesignerForFileObject(FileObject fo) {
//        FacesModel facesModel = getFacesModelForFileObject(fo);
//        
//        JsfForm jsfForm;
//        synchronized (facesModel2jsfForm) {
//            jsfForm = facesModel2jsfForm.get(facesModel);
//        }
//        return jsfForm == null ? null : jsfForm.getDesigner();
//    }
//    
//    
//    /** XXX */
//    Designer getDesigner() {
//        return designer;
//    }
    
//    private static void removeJsfFormForDataObject(DataObject dobj) {
//        synchronized (dataObject2jsfForm) {
//            dataObject2jsfForm.remove(dobj);
//        }
//    }

    
    private void updateDnDListening() {
        getDndSupport().updateDndListening();
    }
    
//    public MultiViewElement getDesignerMultiViewElement() {
//        JComponent component = designer.getDesignerComponent();
//        if (component instanceof MultiViewElement) {
//            return (MultiViewElement)component;
//        } else {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
//                    new IllegalStateException("Component is not of MultiViewElement type, component=" + component)); // NOI18N
//            return null;
//        }
//    }
    
//    private void associateFacesModel(FileObject fo) {
//        facesModel = FacesModel.getInstance(fo);
//
//        if (facesModel == null) {
//            throw new IllegalArgumentException("Specified file is not JSF form, fo=" + fo); // NOI18N
//        }
//
////        init(fo);
//    }
    
    private void replaceFacesModel(FileObject oldFo, FileObject newFo) {
        if (oldFo != null) {
//            designer.destroyDesigner();
            // XXX There would be weak listeners needed.
            getDomSynchronizer().unregisterDomListeners();
        }
        // XXX Force new DomSynchronizer.
        domSynchronizer = null;
        
        if (newFo != null) {
//            associateFacesModel(newFo);

            FacesModel newModel = FacesModel.getInstance(newFo);
            if (newModel == null) {
                throw new IllegalArgumentException("Null FacesModel for FileObject, fo=" + newFo); // NOI18N
            }
            synchronized (facesModel2jsfForm) {
                facesModel2jsfForm.remove(this.facesModel);
                this.facesModel = newModel;
                facesModel2jsfForm.put(this.facesModel, this);
            }
            updateDnDListening();
            
            getDomSynchronizer().requestRefresh();
        }
    }
    
    FacesModel getFacesModel() {
        synchronized (facesModel2jsfForm) {
            return facesModel;
        }
    }
    
//    public Document getJspDom() {
////        return InSyncServiceProvider.get().getJspDomForMarkupFile(getFacesModel().getMarkupFile());
//        return getFacesModel().getJspDom();
//    }
    
    // >> Impl of HtmlDomProvider
//    public Document getHtmlDom() {
////        return InSyncServiceProvider.get().getHtmlDomForMarkupFile(getFacesModel().getMarkupFile());
//        return getFacesModel().getHtmlDom();
//    }
    
//    public DocumentFragment getHtmlDocumentFragment() {
////        return InSyncServiceProvider.get().getHtmlDomFragmentForDocument(getHtmlDom());
//        return getFacesModel().getHtmlDomFragment();
//    }
    
//    public Element getHtmlBody() {
////        return InSyncServiceProvider.get().getHtmlBodyForMarkupFile(getFacesModel().getMarkupFile());
//        return getFacesModel().getHtmlBody();
//    }
    
    void setUpdatesSuspended(MarkupDesignBean markupDesignBean, boolean suspend) {
        getDomSynchronizer().setUpdatesSuspended(markupDesignBean, suspend);
    }
    
    boolean isRefreshPending() {
        return getDomSynchronizer().isRefreshPending();
    }
    
    void attachContext(DesignContext designContext) {
////        getDomSynchronizer().attachContext(context);
//        if (this.designContext == designContext) {
//            return;
//        }
//        
//        detachContext();
//        this.designContext = designContext;
//        
//        if (designContext != null) {
//            designContext.addDesignContextListener(designContextListener);
//        }
        updateDesignContextListening(designContext);
    }
     
    private void updateDesignContextListening(DesignContext designContext) {
        // XXX By reassigning removing the previous listening -> weak listeners.
        designContextListener = new JsfDesignContextListener(this);
        designContext.addDesignContextListener((DesignContextListener)WeakListeners.create(DesignContextListener.class, designContextListener, designContext));
    }
    
//    public void detachContext() {
////        // XXX Do not recreate the synchronizer (causes errors).
////        if (domSynchronizer != null) {
////            getDomSynchronizer().detachContext();
////        }
//        if (designContext != null) {
//            designContext.removeDesignContextListener(designContextListener);
//        }
//        
//        designContext = null;
//    }
    
    DocumentFragment createSourceFragment(MarkupDesignBean bean) {
        return getDomSynchronizer().createSourceFragment(bean);
    }
    
    void requestChange(MarkupDesignBean bean) {
        getDomSynchronizer().requestChange(bean);
    }
    
    void beanChanged(MarkupDesignBean bean) {
        getDomSynchronizer().beanChanged(bean);
    }
    
    void requestTextUpdate(MarkupDesignBean bean) {
        getDomSynchronizer().requestTextUpdate(bean);
    }
    // << Impl of HtmlDomProvider
    
    //////
    // XXX See DomSynchronizer
    void modelChanged() {
//        designer.modelChanged();
        fireModelChanged();
    }
    
    void nodeChanged(Node rendered, Node parent, boolean wasMove) {
//        designer.nodeChanged(rendered, parent, wasMove);
        fireNodeChanged(rendered, parent, wasMove);
    }
    
    
    void nodeRemoved(Node previouslyRendered, Node parent) {
//        designer.nodeRemoved(previouslyRendered, parent);
        fireNodeRemoved(previouslyRendered, parent);
    }
    
    void nodeInserted(Node rendered, Node parent) {
//        designer.nodeInserted(rendered, parent);
        fireNodeInserted(rendered, parent);
    }
    
    void updateErrorsInComponent() {
//        designer.updateErrorsInComponent();
        fireUpdateErrorsInComponent();
    }
    
    void updateGridMode() {
//        designer.updateGridMode();
        fireUpdateGridMode();
    }
    
    void documentReplaced() {
//        designer.documentReplaced();
        fireDocumentReplaced();
    }
    
//    public void clearHtml() {
////        InSyncServiceProvider.get().clearHtmlForMarkupFile(getFacesModel().getMarkupFile());
//        getFacesModel().clearHtml();
//    }
    // XXX
    //////
    
    private DomSynchronizer getDomSynchronizer() {
        if (domSynchronizer == null) {
            domSynchronizer = new DomSynchronizer(this);
        }
        return domSynchronizer;
    }
    
    FacesDnDSupport.UpdateSuspender getUpdateSuspender() {
        return getDomSynchronizer();
    }
    
    //////
    // XXX See HtmlDomProvider interface.
    void requestRefresh() {
        getDomSynchronizer().requestRefresh();
    }
    
    void refreshModel(boolean deep) {
        getFacesModel().refreshAndSyncNonPageBeans(deep);
        fireModelRefreshed();
    }
    
    public void refreshProject() {
        // XXX Moved from designer/DesignerTopComp#componentShowing.
        Project project = getFacesModel().getProject();
        // XXX uh oh, what if I refresh THIS project but not other projects.... and
        // you edit stylesheets from different projects? Notagood! Do I really need to
        // refresh ALL projects?
        if (project != null) {
            RefreshServiceImpl.refreshProject(project, false);

            // Prevent refreshing all for every update since a refresh could be
            // sort of expensive. (It doesn't actually update layout on all pages,
            // but does scan the entire project for pages and clears associated caches
            // etc.
        }
    }
    
//    public void destroyDomSynchronizer() {
//        // XXX Revise.
//        if (domSynchronizer != null) {
//            getDomSynchronizer().destroy();
//        }
//        domSynchronizer = null;
//    }
    // XXX
    //////
    
//    public boolean editEventHandlerForDesignBean(DesignBean designBean) {
//        if (designBean == null) {
////            webform.getModel().openDefaultHandler(component);
//            getFacesModel().openDefaultHandler();
//            return false;
//        } else {
//            // See if it's an XHTML element; if so just show it in
//            // the JSP source
////            if (FacesSupport.isXhtmlComponent(component)) {
//            if (isXhtmlComponent(designBean)) {
////                MarkupBean mb = FacesSupport.getMarkupBean(component);
//                MarkupBean mb = Util.getMarkupBean(designBean);
//                
////                MarkupUnit unit = webform.getMarkup();
//                MarkupUnit unit = getFacesModel().getMarkupUnit();
//                // <markup_separation>
////                Util.show(null, unit.getFileObject(),
////                    unit.computeLine((RaveElement)mb.getElement()), 0, true);
//                // ====
////                MarkupService.show(unit.getFileObject(), unit.computeLine((RaveElement)mb.getElement()), 0, true);
//                showLineAt(unit.getFileObject(), unit.computeLine(mb.getElement()), 0);
//                // </markup_separation>
//            } else {
////                webform.getModel().openDefaultHandler(component);
//                getFacesModel().openDefaultHandler(designBean);
//            }
//
//            return true;
//        }
//    }
    
//    // XXX Copied from DesignerActions.
//    /** Return true iff the given DesignBean is an XHTML markup "component" */
//    private static boolean isXhtmlComponent(DesignBean bean) {
////        MarkupBean mb = FacesSupport.getMarkupBean(bean);
//        MarkupBean mb = Util.getMarkupBean(bean);
//
//        return (mb != null) && !(mb instanceof FacesBean);
//    }
//    
//    // XXX Copied from MarkupUtilities.
//    // XXX Copied from DesignerActions.
//    private static void showLineAt(FileObject fo, int lineno, int column) {
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
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
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

    
    public String toString() {
        return super.toString() + "[facesModel=" + getFacesModel() + "]"; // NOI18N
    }
    
    // >>> DnD
//    public DataFlavor getImportFlavor(DataFlavor[] flavors) {
//        return FacesDnDSupport.getImportFlavor(flavors);
//    }
    
//    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
//        return getFacesModel().getDnDSupport().canImport(comp, transferFlavors);
//    }
    
//    public DesignBean[] pasteBeans(Transferable t, DesignBean parent, MarkupPosition pos, Point location, HtmlDomProvider.CoordinateTranslator coordinateTranslator) {
//        return getFacesModel().getDnDSupport().pasteBeans(t, parent, pos, location, new CoordinateTranslatorImpl(coordinateTranslator), getDomSynchronizer());
//    }
    
//    public void importData(JComponent comp, Transferable t, Object transferData, Dimension dimension, HtmlDomProvider.Location location, HtmlDomProvider.CoordinateTranslator coordinateTranslator, int dropAction) {
//        getFacesModel().getDnDSupport().importData(comp, t, transferData, dimension, new LocationImpl(location), new CoordinateTranslatorImpl(coordinateTranslator), getDomSynchronizer(), dropAction);
//    }
    
//    public DesignBean findHtmlContainer(DesignBean parent) {
//        return Util.findHtmlContainer(parent);
//    }
    
//    public String[] getClassNames(DisplayItem[] displayItems) {
//        return getFacesModel().getDnDSupport().getClasses(displayItems);
//    }
    
//    public boolean importBean(DisplayItem[] items, DesignBean origParent, int nodePos, String facet, List createdBeans, HtmlDomProvider.Location location, HtmlDomProvider.CoordinateTranslator coordinateTranslator)
//    throws IOException {
//        return getFacesModel().getDnDSupport().importBean(items, origParent, nodePos, facet, createdBeans, new LocationImpl(location), new CoordinateTranslatorImpl(coordinateTranslator), getDomSynchronizer());
//    }
    
//    public MarkupPosition getDefaultPositionUnderParent(DesignBean parent) {
//        return FacesDnDSupport.getDefaultMarkupPositionUnderParent(parent, getFacesModel());
//    }
    
//    public int computeActions(DesignBean droppee, Transferable transferable, boolean searchUp, int nodePos) {
//        return getFacesModel().getDnDSupport().computeActions(droppee, transferable, searchUp, nodePos);
//    }
    
//    public DesignBean findParent(String className, DesignBean droppee, Node parentNode, boolean searchUp) {
//        return Util.findParent(className, droppee, parentNode, searchUp, getFacesModel());
//    }
    
//    public int processLinks(Element origElement, Class[] classes, List beans, boolean selectFirst, boolean handleLinks, boolean showLinkTarget) {
//        return getFacesModel().getDnDSupport().processLinks(origElement, classes, beans, selectFirst, handleLinks, showLinkTarget, getDomSynchronizer());
//    }
    
//    public boolean setDesignProperty(DesignBean bean, String attribute, int length) {
//        return Util.setDesignProperty(bean, attribute, length);
//    }

    // <<< DnD
    
//    public void removeForDataObject(DataObject dobj) {
//        removeJsfFormForDataObject(dobj);
//    }
    
    boolean isFragment() {
        return isFragment;
    }

    boolean isPortlet() {
        return isPortlet;
    }

//    public DataObject getJspDataObject() {
//        FileObject file = getFacesModel().getMarkupFile();
//
//        try {
//            return DataObject.find(file);
//        } catch (DataObjectNotFoundException dnfe) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, dnfe);
//
//            return null;
//        }
//    }

    void addHtmlDomProviderListener(HtmlDomProvider.HtmlDomProviderListener l) {
        listenerList.add(HtmlDomProvider.HtmlDomProviderListener.class, l);
    }

    void removeHtmlDomProviderListener(HtmlDomProvider.HtmlDomProviderListener l) {
        listenerList.remove(HtmlDomProvider.HtmlDomProviderListener.class, l);
    }
    
    private HtmlDomProvider.HtmlDomProviderListener[] getHtmlDomProviderListeners() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        
        List<HtmlDomProvider.HtmlDomProviderListener> htmlDomProviderListeners = new ArrayList<HtmlDomProvider.HtmlDomProviderListener>();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i] == HtmlDomProvider.HtmlDomProviderListener.class) {
                htmlDomProviderListeners.add((HtmlDomProvider.HtmlDomProviderListener)listeners[i+1]);
            }          
        }
        return htmlDomProviderListeners.toArray(new HtmlDomProvider.HtmlDomProviderListener[htmlDomProviderListeners.size()]);
    }

    private void fireModelChanged() {
        HtmlDomProvider.HtmlDomProviderListener[] listeners = getHtmlDomProviderListeners();
        for (HtmlDomProvider.HtmlDomProviderListener listener : listeners) {
            listener.modelChanged();
        }
    }
    
    private void fireModelRefreshed() {
        HtmlDomProvider.HtmlDomProviderListener[] listeners = getHtmlDomProviderListeners();
        for (HtmlDomProvider.HtmlDomProviderListener listener : listeners) {
            listener.modelRefreshed();
        }
    }

    private void fireNodeChanged(Node rendered, Node parent, boolean wasMove) {
        HtmlDomProvider.HtmlDomProviderListener[] listeners = getHtmlDomProviderListeners();
        for (HtmlDomProvider.HtmlDomProviderListener listener : listeners) {
            listener.nodeChanged(rendered, parent, wasMove);
        }
    }

    private void fireNodeRemoved(Node previouslyRendered, Node parent) {
        HtmlDomProvider.HtmlDomProviderListener[] listeners = getHtmlDomProviderListeners();
        for (HtmlDomProvider.HtmlDomProviderListener listener : listeners) {
            listener.nodeRemoved(previouslyRendered, parent);
        }
    }

    private void fireNodeInserted(Node rendered, Node parent) {
        HtmlDomProvider.HtmlDomProviderListener[] listeners = getHtmlDomProviderListeners();
        for (HtmlDomProvider.HtmlDomProviderListener listener : listeners) {
            listener.nodeInserted(rendered, parent);
        }
    }

    private void fireUpdateErrorsInComponent() {
        HtmlDomProvider.HtmlDomProviderListener[] listeners = getHtmlDomProviderListeners();
        for (HtmlDomProvider.HtmlDomProviderListener listener : listeners) {
            listener.updateErrorsInComponent();
        }
    }

    private void fireUpdateGridMode() {
        HtmlDomProvider.HtmlDomProviderListener[] listeners = getHtmlDomProviderListeners();
        for (HtmlDomProvider.HtmlDomProviderListener listener : listeners) {
            listener.updateGridMode();
        }
    }

    private void fireDocumentReplaced() {
        HtmlDomProvider.HtmlDomProviderListener[] listeners = getHtmlDomProviderListeners();
        for (HtmlDomProvider.HtmlDomProviderListener listener : listeners) {
            listener.documentReplaced();
        }
    }

    void fireShowDropMatch(Element componentRootElement, Element regionElement, int dropType) {
        HtmlDomProvider.HtmlDomProviderListener[] listeners = getHtmlDomProviderListeners();
        for (HtmlDomProvider.HtmlDomProviderListener listener : listeners) {
            listener.showDropMatch(componentRootElement, regionElement, dropType);
        }
    }
    
    void fireClearDropMatch() {
        HtmlDomProvider.HtmlDomProviderListener[] listeners = getHtmlDomProviderListeners();
        for (HtmlDomProvider.HtmlDomProviderListener listener : listeners) {
            listener.clearDropMatch();
        }
    }

    void fireSelect(DesignBean designBean) {
        HtmlDomProvider.HtmlDomProviderListener[] listeners = getHtmlDomProviderListeners();
        for (HtmlDomProvider.HtmlDomProviderListener listener : listeners) {
            listener.select(designBean);
        }
    }

//    private void fireRefreshForm(boolean deep) {
//        HtmlDomProvider.HtmlDomProviderListener[] listeners = getHtmlDomProviderListeners();
//        for (HtmlDomProvider.HtmlDomProviderListener listener : listeners) {
//            listener.refreshForm(deep);
//        }
//    }

    void fireInlineEdit(DesignBean[] designBeans) {
        HtmlDomProvider.HtmlDomProviderListener[] listeners = getHtmlDomProviderListeners();
        for (HtmlDomProvider.HtmlDomProviderListener listener : listeners) {
            listener.inlineEdit(designBeans);
        }
    }

    private void fireDesignContextActivated(DesignContext designContext) {
        HtmlDomProvider.HtmlDomProviderListener[] listeners = getHtmlDomProviderListeners();
        for (HtmlDomProvider.HtmlDomProviderListener listener : listeners) {
            listener.designContextActivated(designContext);
        }
    }

    private void fireDesignContextDeactivated(DesignContext designContext) {
        HtmlDomProvider.HtmlDomProviderListener[] listeners = getHtmlDomProviderListeners();
        for (HtmlDomProvider.HtmlDomProviderListener listener : listeners) {
            listener.designContextDeactivated(designContext);
        }
    }

    private void fireDesignContextChanged(DesignContext designContext) {
        HtmlDomProvider.HtmlDomProviderListener[] listeners = getHtmlDomProviderListeners();
        for (HtmlDomProvider.HtmlDomProviderListener listener : listeners) {
            listener.designContextChanged(designContext);
        }
    }

    private void fireDesignBeanCreated(DesignBean designBean) {
        HtmlDomProvider.HtmlDomProviderListener[] listeners = getHtmlDomProviderListeners();
        for (HtmlDomProvider.HtmlDomProviderListener listener : listeners) {
            listener.designBeanCreated(designBean);
        }
    }

    private void fireDesignBeanDeleted(DesignBean designBean) {
        HtmlDomProvider.HtmlDomProviderListener[] listeners = getHtmlDomProviderListeners();
        for (HtmlDomProvider.HtmlDomProviderListener listener : listeners) {
            listener.designBeanDeleted(designBean);
        }
    }

    private void fireDesignBeanMoved(DesignBean designBean, DesignBean designBean0, Position position) {
        HtmlDomProvider.HtmlDomProviderListener[] listeners = getHtmlDomProviderListeners();
        for (HtmlDomProvider.HtmlDomProviderListener listener : listeners) {
            listener.designBeanMoved(designBean, designBean0, position);
        }
    }

    private void fireDesignBeanContextActivated(DesignBean designBean) {
        HtmlDomProvider.HtmlDomProviderListener[] listeners = getHtmlDomProviderListeners();
        for (HtmlDomProvider.HtmlDomProviderListener listener : listeners) {
            listener.designBeanContextActivated(designBean);
        }
    }

    private void fireDesignBeanContextDeactivated(DesignBean designBean) {
        HtmlDomProvider.HtmlDomProviderListener[] listeners = getHtmlDomProviderListeners();
        for (HtmlDomProvider.HtmlDomProviderListener listener : listeners) {
            listener.designBeanContextDeactivated(designBean);
        }
    }

    private void fireDesignBeanNameChanged(DesignBean designBean, String string) {
        HtmlDomProvider.HtmlDomProviderListener[] listeners = getHtmlDomProviderListeners();
        for (HtmlDomProvider.HtmlDomProviderListener listener : listeners) {
            listener.designBeanNameChanged(designBean, string);
        }
    }

    private void fireDesignBeanChanged(DesignBean designBean) {
        HtmlDomProvider.HtmlDomProviderListener[] listeners = getHtmlDomProviderListeners();
        for (HtmlDomProvider.HtmlDomProviderListener listener : listeners) {
            listener.designBeanChanged(designBean);
        }
    }

    private void fireDesignPropertyChanged(DesignProperty designProperty, Object object) {
        HtmlDomProvider.HtmlDomProviderListener[] listeners = getHtmlDomProviderListeners();
        for (HtmlDomProvider.HtmlDomProviderListener listener : listeners) {
            listener.designPropertyChanged(designProperty, object);
        }
    }

    private void fireDesignEventChanged(DesignEvent designEvent) {
        HtmlDomProvider.HtmlDomProviderListener[] listeners = getHtmlDomProviderListeners();
        for (HtmlDomProvider.HtmlDomProviderListener listener : listeners) {
            listener.designEventChanged(designEvent);
        }
    }

//    public URL getBaseUrl() {
//        MarkupUnit markupUnit = getFacesModel().getMarkupUnit();
//        if (markupUnit == null) {
//            // #6457856 NPE
//            return null;
//        }
//        return markupUnit.getBase();
//    }

//    public URL resolveUrl(String urlString) {
////        return InSyncServiceProvider.get().resolveUrl(getBaseUrl(), getJspDom(), urlString);
//        return Util.resolveUrl(getBaseUrl(), getJspDom(), urlString);
//    }

//    public DocumentFragment renderHtmlForMarkupDesignBean(MarkupDesignBean markupDesignBean) {
//        return FacesPageUnit.renderHtml(getFacesModel(), markupDesignBean);
//    }

//    public Exception getRenderFailure() {
//        FacesPageUnit facesPageUnit = getFacesModel().getFacesUnit();
//        if (facesPageUnit == null) {
//            return null;
//        }
//        return facesPageUnit.getRenderFailure();
//    }

//    public MarkupDesignBean getRenderFailureMarkupDesignBean() {
//        FacesPageUnit facesPageUnit = getFacesModel().getFacesUnit();
//        if (facesPageUnit == null) {
//            return null;
//        }
//        DesignBean designBean = facesPageUnit.getRenderFailureComponent();
//        if (designBean instanceof MarkupDesignBean) {
//            return (MarkupDesignBean)designBean;
//        } else {
//            return null;
//        }
//    }

//    public List<FileObject> getWebPageFileObjectsInThisProject() {
////        return InSyncServiceProvider.get().getWebPages(getFacesModel().getProject(), true, false);
//        return Util.getWebPages(getFacesModel().getProject(), true, false);
//    }

    PaletteController getPaletteController() {
        return paletteController;
    }

//    public boolean isBraveheartPage() {
//        return Util.isBraveheartPage(getJspDom());
//    }

//    public boolean isWoodstockPage() {
//        return Util.isWoodstockPage(getJspDom());
//    }

    static void refreshDesignersInProject(Project project) {
        JsfForm[] jsfForms = findJsfFormsForProject(project);
        for (JsfForm jsfForm : jsfForms) {
            jsfForm.refreshProject();
        }
    }
    
    private static JsfForm[] findJsfFormsForProject(Project project) {
        if (project == null) {
            return new JsfForm[0];
        }
        List<JsfForm> projectJsfForms = new ArrayList<JsfForm>();
        Set<JsfForm> allJsfForms;
        synchronized (jsfForm2designerSet) {
            allJsfForms = jsfForm2designerSet.keySet();
        }
        for (JsfForm jsfForm : allJsfForms) {
            if (project == jsfForm.getFacesModel().getProject()
            && !projectJsfForms.contains(jsfForm)) {
                projectJsfForms.add(jsfForm);
            }
        }
        return projectJsfForms.toArray(new JsfForm[projectJsfForms.size()]);
    }

    void setRenderFailureValues(MarkupDesignBean renderFailureComponent, Exception renderFailureException) {
        this.renderFailureComponent = renderFailureComponent;
        this.renderFailureException = renderFailureException;
    }

    Exception getRenderFailureException() {
        return renderFailureException;
    }

    MarkupDesignBean getRenderFailureComponent() {
        return renderFailureComponent;
    }

    DndSupport getDndSupport() {
        return dndSupport;
    }
    
//    public boolean canDropDesignBeansAtNode(DesignBean[] designBeans, Node node) {
//        DesignBean parent = null;
//        while (node != null) {
////            if (curr instanceof RaveElement) {
////                parent = ((RaveElement)curr).getDesignBean();
//            if (node instanceof Element) {
////                parent = InSyncService.getProvider().getMarkupDesignBeanForElement((Element)curr);
////                parent = WebForm.getHtmlDomProviderService().getMarkupDesignBeanForElement((Element)curr);
//                parent = MarkupUnit.getMarkupDesignBeanForElement((Element)node);
//
//                if (parent != null) {
//                    break;
//                }
//            }
//
//            node = node.getParentNode();
//        }
//
//        if (parent == null) {
//            return true;
//        }
//
//        // See if ALL the beans being dragged can be dropped here
////        LiveUnit unit = webform.getModel().getLiveUnit();
//        LiveUnit unit = getFacesModel().getLiveUnit();
//
////        for (int i = 0, n = beans.size(); i < n; i++) {
////            DesignBean bean = (DesignBean)beans.get(i);
//        for (DesignBean bean : designBeans) {
//            String className = bean.getInstance().getClass().getName();
//
//            if (!unit.canCreateBean(className, parent, null)) {
//                return false;
//            }
//
//            // Ensure that we're not trying to drop a html bean on a
//            // renders-children parent
//            boolean isHtmlBean = className.startsWith(HtmlBean.PACKAGE);
//
//            if (isHtmlBean) {
//                // We can't drop anywhere below a "renders children" JSF
//                // component
////                if (parent != FacesSupport.findHtmlContainer(webform, parent)) {
////                if (parent != webform.findHtmlContainer(parent)) {
//                if (parent != Util.findHtmlContainer(parent)) {
//                    return false;
//                }
//            }
//        }
//
//        return true;
//    }

//    public boolean handleMouseClickForElement(Element element, int clickCount) {
//        MarkupMouseRegion region = findRegion(element);
//
//        if ((region != null) && region.isClickable()) {
//            Result r = region.regionClicked(clickCount);
//            ResultHandler.handleResult(r, getFacesModel());
//            // #6353410 If there was performed click on the region
//            // then do not perform other actions on the same click.
//            return true;
//        }
//        return false;
//    }
    
//    // XXX Moved from FacesSupport.
//    /** Locate the closest mouse region to the given element */
//    private static MarkupMouseRegion findRegion(Element element) {
//        while (element != null) {
////            if (element.getMarkupMouseRegion() != null) {
////                return element.getMarkupMouseRegion();
////            }
////            MarkupMouseRegion region = InSyncService.getProvider().getMarkupMouseRegionForElement(element);
//            MarkupMouseRegion region = FacesPageUnit.getMarkupMouseRegionForElement(element);
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

    
    private static class DataObjectPropertyChangeListener implements PropertyChangeListener {
        
        private final JsfForm jsfForm;
        
        public DataObjectPropertyChangeListener(JsfForm jsfForm) {
            this.jsfForm = jsfForm;
        }
        
        public void propertyChange(final PropertyChangeEvent evt) {
            // Immediately wipe out the paint box
            if (evt.getPropertyName().equals(DataObject.PROP_PRIMARY_FILE)) {
//                if ((getPane() != null) && (getPane().getPaneUI() != null)) {
//                    getPane().getPaneUI().setPageBox(null);
//                }
                
/*//NB6.0
                // Reconfigure the data object: throw away the old model
                // and find the new model associated with the new file object.
//                InSyncServiceProvider.get().doOutsideOfRefactoringSession(new Runnable() {
                MdrInSyncSynchronizer.get().doOutsideOfRefactoringSession(new Runnable() {
                    public void run() {
 */
                // Do the stuff on UI thread as some stuff gets updated that requires to be on UI thread
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        jsfForm.replaceFacesModel((FileObject)evt.getOldValue(), (FileObject)evt.getNewValue());
                    }
                });
/*                  }
                });
//*/
            }
        }
    } // End of DataObjectPropertyChangeListener.
    

    private static class JsfDesignContextListener implements DesignContextListener {
        
        private final JsfForm jsfForm;
        
        public JsfDesignContextListener(JsfForm jsfForm) {
            this.jsfForm = jsfForm;
        }
        
        public void contextActivated(DesignContext designContext) {
            jsfForm.getDomSynchronizer().contextActivated(designContext);
//            jsfForm.designer.contextActivated(designContext);
            jsfForm.fireDesignContextActivated(designContext);
        }
        
        public void contextDeactivated(DesignContext designContext) {
            jsfForm.getDomSynchronizer().contextDeactivated(designContext);
//            jsfForm.designer.contextDeactivated(designContext);
            jsfForm.fireDesignContextDeactivated(designContext);
        }
        
        public void contextChanged(DesignContext designContext) {
            jsfForm.getDomSynchronizer().contextChanged(designContext);
//            jsfForm.designer.contextChanged(designContext);
            jsfForm.fireDesignContextChanged(designContext);
        }
        
        public void beanCreated(DesignBean designBean) {
            jsfForm.getDomSynchronizer().beanCreated(designBean);
//            jsfForm.designer.beanCreated(designBean);
            jsfForm.fireDesignBeanCreated(designBean);
        }
        
        public void beanDeleted(DesignBean designBean) {
            jsfForm.getDomSynchronizer().beanDeleted(designBean);
//            jsfForm.designer.beanDeleted(designBean);
            jsfForm.fireDesignBeanDeleted(designBean);
        }
        
        public void beanMoved(DesignBean designBean, DesignBean designBean0, Position position) {
            jsfForm.getDomSynchronizer().beanMoved(designBean, designBean0, position);
//            jsfForm.designer.beanMoved(designBean, designBean0, position);
            jsfForm.fireDesignBeanMoved(designBean, designBean0, position);
        }
        
        public void beanContextActivated(DesignBean designBean) {
            jsfForm.getDomSynchronizer().beanContextActivated(designBean);
//            jsfForm.designer.beanContextActivated(designBean);
            jsfForm.fireDesignBeanContextActivated(designBean);
        }
        
        public void beanContextDeactivated(DesignBean designBean) {
            jsfForm.getDomSynchronizer().beanContextDeactivated(designBean);
//            jsfForm.designer.beanContextDeactivated(designBean);
            jsfForm.fireDesignBeanContextDeactivated(designBean);
        }
        
        public void instanceNameChanged(DesignBean designBean, String string) {
            jsfForm.getDomSynchronizer().instanceNameChanged(designBean, string);
//            jsfForm.designer.instanceNameChanged(designBean, string);
            jsfForm.fireDesignBeanNameChanged(designBean, string);
        }
        
        public void beanChanged(DesignBean designBean) {
            jsfForm.getDomSynchronizer().beanChanged(designBean);
//            jsfForm.designer.beanChanged(designBean);
            jsfForm.fireDesignBeanChanged(designBean);
        }
        
        public void propertyChanged(DesignProperty designProperty, Object object) {
            jsfForm.getDomSynchronizer().propertyChanged(designProperty, object);
//            jsfForm.designer.propertyChanged(designProperty, object);
            jsfForm.fireDesignPropertyChanged(designProperty, object);
        }
        
        public void eventChanged(DesignEvent designEvent) {
            jsfForm.getDomSynchronizer().eventChanged(designEvent);
//            jsfForm.designer.eventChanged(designEvent);
            jsfForm.fireDesignEventChanged(designEvent);
        }
    } // End of JsfDesignContextListener.
    
    
//    private static class CoordinateTranslatorImpl implements FacesDnDSupport.CoordinateTranslator {
//        private final HtmlDomProvider.CoordinateTranslator coordinateTranslator;
//        
//        public CoordinateTranslatorImpl(HtmlDomProvider.CoordinateTranslator coordinateTranslator) {
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
    
    
//    private static class LocationImpl implements FacesDnDSupport.Location {
//        private final HtmlDomProvider.Location location;
//        
//        
//        public LocationImpl(HtmlDomProvider.Location location) {
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
}

