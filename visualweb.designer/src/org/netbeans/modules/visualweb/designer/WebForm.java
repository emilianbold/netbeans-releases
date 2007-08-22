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

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import javax.swing.ActionMap;
import org.netbeans.modules.visualweb.api.designer.Designer;
import org.netbeans.modules.visualweb.api.designer.Designer.DesignerEvent;
import org.netbeans.modules.visualweb.api.designer.DomProvider;
import org.netbeans.modules.visualweb.api.designer.DomProvider.DomDocument;
import org.netbeans.modules.visualweb.api.designer.DomProvider.DomPosition;
import org.netbeans.modules.visualweb.api.designer.DomProvider.DomPosition.Bias;
import org.netbeans.modules.visualweb.api.designer.DomProvider.DomRange;
import org.netbeans.modules.visualweb.api.designer.DomProvider.InlineEditorSupport;
import org.netbeans.modules.visualweb.api.designer.DomProviderService;
import org.netbeans.modules.visualweb.api.designer.cssengine.StyleData;
import org.netbeans.modules.visualweb.css2.CssBox;
import org.netbeans.modules.visualweb.css2.ModelViewMapper;
import org.netbeans.modules.visualweb.css2.PageBox;
import org.netbeans.modules.visualweb.designer.ImageCache;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.event.EventListenerList;
import org.netbeans.modules.visualweb.spi.designer.Decoration;

import org.netbeans.spi.palette.PaletteController;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.UserDataHandler;


/**
 * Holds web-form related state.
 * <p>
 *
 * @author Peter Zavadsky
 * @author Tor Norbye (the original code)
 */
public class WebForm implements Designer {
    /** Represents a webform that is external - and cannot be previewed
     * in the designer.
     */
//    public static final WebForm EXTERNAL = new WebForm();

//    /** A weak Map<DataObject,WebForm>. <b>Access locked by LOCK_WEB_FORMS</b>. */
//    private static final Map webForms = new WeakHashMap();

//    /** A weak Map<FileObject, WebForm>. This is needed because we need to
//     * be able to clean up when we find out that a FileObject has been deleted. And there
//     * is no way to find the DataObject for a FileObject that was deleted. The alternative
//     * would be iterate through the <DataObject,WebForm> map, and for each data object
//     * key check if dataObject.getPrimaryFile() is the FileObject we're after.
//     * But that seems risky since this is a weak map - it could already have been
//     * discarded, since the system seems to already have disconnected the data object
//     * once the file is deleted.
//     * So for now we simply keep two maps in sync.
//     * <b>Access locked by LOCK_WEB_FORMS</b>.
//     */
//    private static final Map webFormsByFileObject = new WeakHashMap();


//    /** Lock for the webForms and webFormsByFileObject maps */
//    private static final Object LOCK_WEB_FORMS = new Object();

//    private static final Map<DomProvider, WebForm> domProvider2webForm = new WeakHashMap<DomProvider, WebForm>();

    private final DomProvider domProvider;
    
//    private final DomProvider.DomProviderListener domProviderListener = new domProviderListener(this);
    
//    protected FacesModel model;
    private SelectionManager selection;
//    private DesignerTopComp view;

    
//    private Document document;
//    private final DomDocument domDocument = DesignerPaneBase.createDomDocument(this);
    
//    private boolean gridMode = false;
//    private ModelViewMapper mapper;
//    private CssLookup css;
    
    private final CellRendererPane rendererPane = new CellRendererPane();
    
    private final DesignerPane designerPane;
    
//    private DocumentFragment html;
//    private DomSynchronizer domSyncer;
//    private RaveElement body;
    
//    private VirtualFormSupport virtualForms;
//    private boolean virtualFormsEnabled;
    
    private ColorManager colors;
//    private DesignerActions actions;
    private InteractionManager manager;
//    private GridHandler gridHandler;
//    private boolean isFragment;
//    private boolean isPortlet;
//    private WebForm contextPage;
//    private Exception renderFailure;
//    private boolean renderFailureShown;
//    private MarkupDesignBean renderFailureComponent;
    
//    /** Maps elements to css boxes. */
//    private final Map<Element, CssBox> element2cssBox = new WeakHashMap<Element, CssBox>();
    private static final String KEY_CSS_BOX_MAP = "vwpCssBoxMap"; // NOI18N
    
//    // XXX Suspicious listener, it should be removed.
//    private JspDataObjectListener jspDataObjectListener;

    // XXX Moved from Document.
    private ImageCache imageCache;
//    private DocumentCache frameCache;

    private final EventListenerList listenerList = new EventListenerList();

    /** Determines whether to paint size mask or not. Typically used for fragments and portlets.
     * The mask is painted in case the returned body element has width and height properties smaller
     * than the size of the viewport. */
    private boolean paintSizeMask;
    
//    private static class JspDataObjectListener implements PropertyChangeListener {
//        private final WebForm webForm;
//        public JspDataObjectListener(WebForm webForm) {
//            this.webForm = webForm;
//        }
//        public void propertyChange(final PropertyChangeEvent evt) {
//            // Immediately wipe out the paint box
//            if (evt.getPropertyName().equals(DataObject.PROP_PRIMARY_FILE)) {
//                if ((webForm.getPane() != null) && (webForm.getPane().getPaneUI() != null)) {
//                    webForm.getPane().getPaneUI().setPageBox(null);
//                }
//
////                    // Reconfigure the data object: throw away the old model
////                    // and find the new model associated with the new file object.
////                    InSyncService.getProvider().doOutsideOfRefactoringSession(new Runnable() {
////                        public void run() {
////                            // Do the stuff on UI thread as some stuff gets updated that requires to be on UI thread
////                            SwingUtilities.invokeLater(new Runnable() {
////                                public void run() {
////                                    replaceModel((FileObject)evt.getOldValue(), (FileObject)evt.getNewValue());
////                                }
////                            });
////                        }
////                    });
//            }
//        }
//    } // End of JspDataObjectListener.

    
//    private boolean cisClosing = false;

//    private WebForm() {
//        // XXX Get rid of this constructor.
//        this.domProvider = null;
//    }

    /**
     * Create a new webform object. dobj cannot be null.
     */
    private WebForm(DomProvider domProvider /*, FileObject fo*/) {
//        assert fo != null;
        if (domProvider == null /*|| fo == null*/) {
            throw new NullPointerException("The domProvider parameter can't be null!" // NOI18N
                    /*+ ", fo=" + fo*/); // NOI18N
        }
        
        this.domProvider = domProvider;
        
        this.designerPane = new DesignerPane(this);
//        associateModel(fo);
//        init(fo);
    }

//    private void associateModel(FileObject fo) {
//        model = FacesModel.getInstance(fo);
//
//        if (model == null) {
//            throw new IllegalArgumentException("Specified file is not webform-like fo=" + fo); // NOI18N
//        }
//
//        init(fo);
//    }

    
    public static WebForm createWebForm(DomProvider domProvider) {
        return new WebForm(domProvider);
    }
    
//    // Moved from DesignerUtils.
//    /** Locate the webform associated with the given data object. Will return
//     *  null if no such object is found.
//     * @param dobj The data object for which a webform should be located
//     * @param initialize If false, don't initialize a webform that has
//     *  not yet been open - return null instead.
//     */
//    public static WebForm findWebForm(DataObject dobj) {
//        if (isWebFormDataObject(dobj)) {
//            return getWebFormForDataObject(dobj);
//        }
//        
//        return null;
//    }
    

//    private static DesignerFinder getDesignerFinder() {
//        DesignerFinder designerFinder = (DesignerFinder)Lookup.getDefault().lookup(DesignerFinder.class);
//        if (designerFinder == null) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
//                    new NullPointerException("DesignerFinder impl not available!")); // NOI18N
//            return null;
//        }
//        return designerFinder;
//    }
//    
//    private static WebForm findDesigner(DataObject dataObject) {
//        DesignerFinder designerFinder = getDesignerFinder();
//        if (designerFinder == null) {
//            return null;
//        }
//        
//        Designer designer = designerFinder.getDesigner(dataObject);
//        return getWebFormForDesigner(designer);
//    }
//    
//    private static WebForm getWebFormForDesigner(Designer designer) {
//        if (designer instanceof WebForm) {
//            return (WebForm)designer;
//        } else {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
//                    new IllegalStateException("Found designer is not of WebForm type, designer=" + designer)); // TEMP
//            return null;
//        }
//    }
    
//    private static boolean isWebFormDataObject(DataObject dobj) {
////        return (dobj != null) && (FacesModel.getInstance(dobj.getPrimaryFile()) != null);
//        return (dobj != null) && getDomProviderService().isWebFormFileObject(dobj.getPrimaryFile());
//    }

//    public static boolean hasWebFormForDataObject(DataObject dobj) {
////        synchronized (LOCK_WEB_FORMS) {
////            return webForms.containsKey(dobj);
////        }
//    
////        return getDesignerFinder().hasDesigner(dobj);
//        
//        DomProviderFinder domProviderFinder = getDomProviderFinder();
//        if (domProviderFinder == null) {
//            return false;
//        }
//
//        if (!domProviderFinder.hasDomProvider(dobj)) {
//            return false;
//        }
//        
//        DomProvider domProvider = domProviderFinder.findDomProvider(dobj);
//        synchronized (domProvider2webForm) {
//            // XXX Check whether the model is still valid
//            return domProvider2webForm.containsKey(domProvider);
//        }
//    }
//    private static boolean hasWebFormForDataObject(DataObject jsfJspDataObject) {
//        if (jsfJspDataObject == null) {
//            return false;
//        }
//        return findWebFormForFileObject(jsfJspDataObject.getPrimaryFile()) != null;
//    }

    
//    public static WebForm getWebFormForDataObject(/*DomProvider domProvider,*/ DataObject dobj) {
//        if (/*domProvider == null ||*/ dobj == null) {
//            throw new NullPointerException("Parameter dobj can't be null!"); // NOI18N
//        }
//        
////        synchronized (LOCK_WEB_FORMS) {
////            WebForm webform = (WebForm)webForms.get(dobj);
////
////            if (webform == null) {
////                FileObject fo = dobj.getPrimaryFile();
////                webform = new WebForm(domProvider, fo);
////                webForms.put(dobj, webform);
////                webFormsByFileObject.put(fo, webform);
////
////                dobj.addPropertyChangeListener(webform.propertyListener);
////            }
////
////            return webform;
////        }
//        
////        return findDesigner(dobj);
//
//        DomProvider domProvider = getDomProviderFinder().getDomProvider(dobj);
//        if (domProvider == null) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, 
//                    new NullPointerException("No instance of DomProvider for data object, dobj=" + dobj)); // NOI18N
//            return null;
//        }
//        return getWebFormForDomProvider(domProvider);
//    }
     
//    public static WebForm getWebFormForDomProvider(DomProvider domProvider) {
//        if (domProvider == null) {
//            throw new NullPointerException("Can't create WebForm for null DomProvider!"); // NOI18N
//        }
//        synchronized (domProvider2webForm) {
//            WebForm webForm = domProvider2webForm.get(DomProvider);
//            if (webForm == null) {
//                webForm = new WebForm(domProvider);
//                domProvider2webForm.put(DomProvider, webForm);
//            }
//            return webForm;
//        }
//    }
    
    public static DomProviderService getDomProviderService() {
        DomProviderService domProviderService = (DomProviderService)Lookup.getDefault().lookup(DomProviderService.class);
        if (domProviderService == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, 
                    new NullPointerException("No instance of DomProviderService available!")); // NOI18N
            return new DummyDomProviderService();
        }
        return domProviderService;
    }
    
    
//    /** The DataObject/Model we're attached to has been reconfigured: it's now
//     * pointing to a new primary file. (This happens when you rename a folder
//     * for example.)   Since models are tied to FileObjects, not DataObjects,
//     * we need to switch models behind the scenes.
//     */
//    private void replaceModel(FileObject oldFo, FileObject newFo) {
//        if (oldFo != null) {
//            destroy();
//        }
//
//        if (newFo != null) {
//            associateModel(newFo);
////            getDomSynchronizer().requestRefresh();
//            domProvider.requestRefresh();
//        }
//    }


//    static MultiViewElement getDesignerMultiViewElement(DataObject dataObject) {
//        WebForm webForm = getWebFormForDataObject(dataObject);
//        if (webForm == null) {
//            return null;
//        }
//        return webForm.getTopComponent();
//    }
    
//    /** Finds <code>WebForm</code> for provided <code>DesignContext</code>.
//     * XXX Get rid of this method. */
//    public static WebForm findWebFormForDesignContext(DesignContext designContext) {
////        Collection col;
////        synchronized (LOCK_WEB_FORMS) {
////            col = webFormsByFileObject.values();
////        }
////        
////        for (Iterator it = col.iterator(); it.hasNext(); ) {
////            WebForm webform = (WebForm)it.next();
////            // XXX LiveUnit is DesignContext.
////            if ((DesignContext)webform.getModel().getLiveUnit() == designContext) {
////                return webform;
////            }
////        }
////        return null;
//        
////        return getWebFormForDesigner(getDesignerFinder().findDesignerForDesignContext(designContext));
//        
////        DomProvider domProvider = getDomProviderFinder().findDomProvider(designContext);
////        if (domProvider == null) {
////            return null;
////        }
////        synchronized (domProvider2webForm) {
////            return domProvider2webForm.get(domProvider);
////        }
//        
//        Designer[] designers = getDomProviderService().findDesignersForDesignContext(designContext);
//        if (designers.length > 0 && designers[0] instanceof WebForm) {
//            return (WebForm)designers[0];
//        } else {
//            return null;
//        }
//    }
    
    public static WebForm[] findAllWebFormsForElement(Element element) {
        Designer[] designers = getDomProviderService().findDesignersForElement(element);
        List<WebForm> webForms = new ArrayList<WebForm>();
        for (Designer designer : designers) {
            if (designer instanceof WebForm) {
                webForms.add((WebForm)designer);
            }
        }
        return webForms.toArray(new WebForm[webForms.size()]);
    }
    
    public static WebForm findWebFormForElement(Element element) {
        Designer[] designers = getDomProviderService().findDesignersForElement(element);
        if (designers.length > 0 && designers[0] instanceof WebForm) {
            return (WebForm)designers[0];
        } else {
            return null;
        }
    }
    
    public static WebForm findWebFormForNode(org.openide.nodes.Node node) {
        Designer[] designers = getDomProviderService().findDesignersForNode(node);
        if (designers.length > 0 && designers[0] instanceof WebForm) {
            return (WebForm)designers[0];
        } else {
            return null;
        }
    }
    
//    public static WebForm getWebFormForDataObject(DataObject jsfJspDataObject) {
//        Designer[] designers = getDomProviderService().getDesignersForDataObject(jsfJspDataObject);
//        if (designers.length > 0 && designers[0] instanceof WebForm) {
//            return (WebForm)designers[0];
//        } else {
//            return null;
//        }
//    }
    
//    public static WebForm findWebFormForFileObject(FileObject jsfJspFileObject) {
////        synchronized (LOCK_WEB_FORMS) {
////            WebForm webform = (WebForm)webFormsByFileObject.get(fo);
////
////            return webform;
////        }
//        
////        return getWebFormForDesigner(getDesignerFinder().findDesignerForFileObject(fo));
//        
////        DataObject dobj;
////        try {
////            dobj = DataObject.find(fo);
////        } catch (DataObjectNotFoundException ex) {
////            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
////            return null;
////        }
////        
////        DomProvider domProvider = getDomProviderFinder().findDomProvider(dobj);
////        synchronized (domProvider2webForm) {
////            return domProvider2webForm.get(domProvider);
////        }
//        
//        Designer[] designers = getDomProviderService().findDesignersForFileObject(jsfJspFileObject);
//        if (designers.length > 0 && designers[0] instanceof WebForm) {
//            return (WebForm)designers[0];
//        } else {
//            return null;
//        }
//    }

//    // XXX This is very suspicious, but is just copies previous perf fixes.
//    // The FacesModel should fire events about being to be destroyed, and then
//    // this should be removed.
//    static void removeWebFormForFileObject(FileObject fo) {
//        synchronized (LOCK_WEB_FORMS) {
//            WebForm webform = (WebForm)webFormsByFileObject.get(fo);
//            webFormsByFileObject.remove(fo);
//            
//            if ((webform != null) && fo.isValid()) {
//                try {
//                    DataObject dobj = DataObject.find(fo);
//                    dobj.addPropertyChangeListener(webform.propertyListener);
//                    webForms.remove(dobj);
//                    
////                    // XXX Also JsfForm needs to be removed the same way.
////                    webform.destroyDomProvider(dobj);
//                } catch (DataObjectNotFoundException ex) {
//                    ErrorManager.getDefault().notify(ex);
//                }
//            }
//
//            // If fo.isValid() was false, or if the DataObject was not found,
//            // we'll leave the data object in the map. Not great... but it shouldn't
//            // cause any trouble since it's a weak map, and nobody should hold any
//            // references to it.
//        }
//    }
    
    public DomProvider getDomProvider() {
        return domProvider;
    }

    public String toString() {
//        if (getMarkup() != null) {
//            return "WebForm[" + getMarkup().getFileObject().getNameExt() + "]";
//        }
        return super.toString() + "[domProvider=" + domProvider + " ,selection=" + selection
                + ", designerPane=" + designerPane + ", gridmode=" + isGridMode() + "]";
    }

//    /** Look up a webform for a given model */
//    public static WebForm get(FacesModel model) {
//        if (model == null) {
//            return null;
//        }
//
//        FileObject fo = model.getMarkupFile();
//        DataObject dobj = null;
//
//        try {
//            dobj = DataObject.find(fo);
//        } catch (DataObjectNotFoundException ex) {
//            return null;
//        }
//
//        return DesignerUtils.getWebForm(dobj);
//    }

//    private void init(FileObject fo) {
//        isFragment = "jspf".equals(fo.getExt()); // NOI18N
//
//        FacesModel model = getModel();
//        if ((model != null) && (model.getFacesModelSet() != null)) {
//            isPortlet = model.getFacesModelSet().getFacesContainer().isPortletContainer();
//        }
//    }

    public void setCssBoxForElement(Element element, CssBox box) {
        // XXX Copied from the original impl (in RaveElement).
        Node parent = element.getParentNode();
        if ((parent instanceof Element) && getCssBoxForElement((Element)parent) == box) {
            return; // Don't duplicate a bean reference on all the children!
        }
        
//        synchronized (element2cssBox) {
//            element2cssBox.put(element, box);
//        }
        // #106433 There needs to be 1:N mapping for  element : box.
        // TODO Revise potential memory leak, boxes linked to the elements!
        Map<WebForm, CssBox> webform2box = (Map<WebForm, CssBox>)element.getUserData(KEY_CSS_BOX_MAP);
        if (webform2box == null) {
            webform2box = new HashMap<WebForm, CssBox>();
        }
        webform2box.put(this, box);
        element.setUserData(KEY_CSS_BOX_MAP, webform2box, CssBoxDataHandler.getDefault());
    }
    
    public CssBox getCssBoxForElement(Element element) {
//        synchronized (element2cssBox) {
//            return element2cssBox.get(element);
//        }
        if (element == null) {
            return null;
        }
        Map<WebForm, CssBox> webform2box = (Map<WebForm, CssBox>)element.getUserData(KEY_CSS_BOX_MAP);
        return webform2box == null ? null : webform2box.get(this);
    }
    
    // XXX Temporary, see DesignerService.copyBoxForElement.
    public void copyBoxForElement(Element fromElement, Element toElement) {
        CssBox box = getCssBoxForElement(fromElement);
        setCssBoxForElement(toElement, box);
    }
    
    /**
     * Locate the box that was created for the given element,
     * if any. May return null if the element has not been
     * rendered.
     */
    public CssBox findCssBoxForElement(Element element) {
//        RaveElement e = (RaveElement)element;
//        CssBox box = (CssBox)e.getBox();
        CssBox box = getCssBoxForElement(element);

        if (box != null) {
            // Work around the problem that children boxes when created may
            // overwrite the existing box reference in the element. I can't
            // simply at that point go and look to see if a reference already exists
            // and if so not point to my new box, since that would break
            // incremental layout etc. -- I create multiple generations of
            // boxes for each element, and in the box constructor I don't
            // know the parentage of the box yet.
            while ((box.getParent() != null) && (box.getParent().getElement() == element)) {
                box = box.getParent();
            }

//            e.setBox(box);
            setCssBoxForElement(element, box);

            return box;
        }

        return null;
    }
    

    
//    public FileObject getFile() {
//        return getModel().getMarkupFile();
//    }

//    public DataObject getDataObject() {
////        FileObject file = getModel().getMarkupFile();
////
////        try {
////            return DataObject.find(file);
////        } catch (DataObjectNotFoundException e) {
////            return null;
////        }
//        return getJspDataObject();
//    }

//    public DataObject getJspDataObject() {
////        FileObject file = getModel().getMarkupFile();
////
////        try {
////            return DataObject.find(file);
////        } catch (DataObjectNotFoundException dnfe) {
////            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, dnfe);
////
////            return null;
////        }
//        return domProvider.getJspDataObject();
//    }

//    public DataObject getJavaDataObject() {
//        FileObject file = getModel().getJavaFile();
//
//        try {
//            return DataObject.find(file);
//        } catch (DataObjectNotFoundException dnfe) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, dnfe);
//
//            return null;
//        }
//    }

//    /**
//     * Create the TopComponent for this webform, using the given
//     * editor support (may be null).
//     */
//    public void createTopComponent() {
//        //assert view == null;
//        view = new DesignerTopComp(this);
//    }

//    private void detachTopComponent() {
//        if (!isClosing && (view != null)) {
//            // Prevent recursion since detachTopComponent can be called via designer
//            // service from the data object editor support when it finds out that
//            // the buffer is being closed
//            isClosing = true;
//
//            try {
//                boolean success = view.forceClose();
//            } finally {
//                isClosing = false;
//            }
//        }
//
//        view = null;
//        document = null;
////        css = null;
////        mapper = null;
//        rendererPane = null;
//
//        if (selection != null) {
//            selection.releaseNodes();
//        }
//
//        selection = null;
//
////        // !EAT This should most likely go somewhere else
////        if ((OutlineTopComp.getInstance() != null) &&
////                (OutlineTopComp.getInstance().getWebForm() == this)) {
////            OutlineTopComp.getInstance().setCurrent(null);
////        }
//    }

//    /**
//     * Return the top component associated with the design view
//     */
//    public DesignerTopComp getTopComponent() {
//        if (view == null) {
//            view = new DesignerTopComp(this);
//        }
//
//        return view;
//    }

    public DesignerPane getPane() {
//        return getTopComponent().getDesignerPane();
        return designerPane;
    }

//    public FacesModel getModel() {
////        return model;
//        return domProvider.getFacesModel();
//    }

    public boolean hasSelection() {
        return selection != null;
    }

    public SelectionManager getSelection() {
        if (selection == null) {
            selection = new SelectionManager(this);
        }

        return selection;
    }

//    /**
//     * Get the document associated with this webform.
//     */
//    public Document getDocument() {
//        if (document == null) {
//            document = new Document(this);
//        }
//
//        return document;
//    }
//
//    public void setDocument(Document document) {
//        this.document = document;
//    }
    /**
     * Gets the DomDocument associated with this webform.
     */
    public DomDocument getDomDocument() {
//        return domDocument;
        return domProvider.getDomDocument();
    }
    

//    /**
//     * Get the document associated with this webform.
//     */
//    public org.w3c.dom.Document getJspDom() {
//// <separation of models>
////        MarkupUnit unit = model.getMarkupUnit();
////
////        if (unit == null) { // possible when project has closed
////            return null;
////        }
////
////        return (RaveDocument)unit.getDocument();
//// ====
////        return InSyncService.getProvider().getJspDomForMarkupFile(getModel().getMarkupFile());
//        return domProvider.getJspDom();
//// </separation of models>
//    }

    public org.w3c.dom.Document getHtmlDom() {
//        return InSyncService.getProvider().getHtmlDomForMarkupFile(getModel().getMarkupFile());
        return domProvider.getHtmlDom();
    }
    
    /**
     * Return the HTML DOM associated with the source JSPX DOM
     * returned by {@link getDom}.
     * @return A DocumentFragment which represents an HTML rendered,
     *   JavaScript mutated, &lt;f:verbatim&gt;/&lt;ui:tag&gt; expanded
     *   view of the source DOM.
     */
    public DocumentFragment getHtmlDomFragment() {
// <separation of models>
//        if (html == null) {
//            // XXX TODO There is not needed webform here.
//            FileObject markupFile = this.getModel().getMarkupFile();
////            html = FacesSupport.renderHtml(markupFile, null, !CssBox.noBoxPersistence);
//            html = InSyncService.getProvider().renderHtml(markupFile, null, !CssBox.noBoxPersistence);
//            // XXX FIXME Is this correct here?
//            FacesSupport.updateErrorsInComponent(this);
//
//            if (html != null) {
//                // Is this a page fragment?
//                if (isFragment || isPortlet) {
//                    // Just use the first element
//                    NodeList nl = html.getChildNodes();
//
//                    for (int i = 0, n = nl.getLength(); i < n; i++) {
//                        Node node = nl.item(i);
//
//                        if (node.getNodeType() == Node.ELEMENT_NODE) {
//                            body = (RaveElement)node;
//
//                            getDom().setRoot(body);
//
//                            break;
//                        }
//                    }
//
//                    WebForm page = getContextPage();
//
//                    if (page != null) {
//                        // XXX Force sync first??
//                        Element surroundingBody = page.getBody();
//
//                        if (surroundingBody != null) {
//                            RaveElement.setStyleParent(body, surroundingBody);
//
//                            // Make sure styles inherit right into the included content
//                            ((RaveDocument)getDom()).setCssEngine(page.getDom().getCssEngine());
//
//                            XhtmlCssEngine engine = CssLookup.getCssEngine(body);
//
//                            if (engine != null) {
//                                engine.clearTransientStyleSheetNodes();
//                            }
//                        }
//                    }
//                } else {
//                    body = null;
//                }
//
//                if (body == null) {
//                    body = findBody(html);
//
//                    //if (body == null) {
//                    //    // Insert one! Is this going to cause locking problems??? I
//                    //    // need to do this under a write lock...
//                    //}
//                    NodeList nl = html.getChildNodes();
//
//                    for (int i = 0, n = nl.getLength(); i < n; i++) {
//                        Node node = nl.item(i);
//
//                        if (node.getNodeType() == Node.ELEMENT_NODE) {
//                            getDom().setRoot((RaveElement)node);
//
//                            break;
//                        }
//                    }
//                }
//            }
//        }
//
//        return html;
// ====
//        DocumentFragment df = InSyncService.getProvider().getHtmlDomFragmentForMarkupFile(getModel().getMarkupFile());
//        DocumentFragment df = InSyncService.getProvider().getHtmlDomFragmentForDocument(getHtmlDom());
        DocumentFragment df = domProvider.getHtmlDocumentFragment();

        // XXX #6472138 This doesn't need to be here now, used only in the dump.
//        // XXX FIXME Is this correct here?
//        updateErrorsInComponent();
        
        return df;
// </separation of models>
    }

    /**
     * Return the &lt;body&gt; element associated with the rendered HTML
     * document
     */
    public Element getHtmlBody() {
//        return getHtmlBody(true);
        return domProvider.getHtmlBody();
    }

//    // XXX Helper method, see DesignerTopComp#updateErrors.
//    Element getHtmlBody(boolean updateErrors) {
//// <separation of models>
////        if (body == null) {
////            // XXX Initing by side effect.
////            getHtmlDom(); // will set body too, if possible
////        }
////
////        return body;
//// ====
////        return InSyncService.getProvider().getHtmlBodyForMarkupFile(getModel().getMarkupFile());
//        Element bodyElement =  domProvider.getHtmlBody();
//        
//        // XXX #6472138 FIXME Is this correct here?
//        if (updateErrors) {
//            updateErrorsInComponent();
//        }
//        
//        return bodyElement;
//// </separation of models>
//    }

//    private static RaveElement findBody(Node node) {
//        Element body = Util.findDescendant(HtmlTag.BODY.name, node);
//
//        if (body == null) {
//            body = Util.findDescendant(HtmlTag.FRAMESET.name, node);
//        }
//
//        // TODO -- make sure body is lowercase tag. If not offer to tidy it!
//        return (RaveElement)body;
//    }

//    /**
//     * Clears the HTML DOM associated with the source JSPX DOM
//     * returned by {@link getDom}.
//     * @param html A DocumentFragment which represents a rendered
//     * view of the JSPX DOM.
//     */
//    public void clearHtml() {
////        this.html = null;
////        this.body = null; // force new search
////        InSyncService.getProvider().clearHtmlForMarkupFile(getModel().getMarkupFile());
//        domProvider.clearHtml();
//    }

//    public MarkupUnit getMarkup() {
//        return getModel().getMarkupUnit();
//    }


    // XXX TODO Revise and provide safe solution.
//    boolean hasDomSynchronizer() {
//        return domSyncer != null;
//    }
//    
//    public DomSynchronizer getDomSynchronizer() {
//        if (domSyncer == null) {
//            domSyncer = new DomSynchronizer(this);
//        }
//
//        return domSyncer;
//    }

//    /**
//     * Set whether or not grid mode is in effect
//     */
//    private void setGridMode(boolean on) {
//        gridMode = on;
//
//        DesignerPane pane = getPane();
//
//        if (pane != null) {
//            pane.setGridMode(on);
//        }
//
//        // Gotta set the cursor to a pointer instead! How can I do
//        // this in a PLAF agnostic way?
//    }
//    private void updatePaneGrid(boolean gridMode) {
    public void setPaneGrid(boolean gridMode) {
        DesignerPane pane = getPane();
        if (pane != null) {
            pane.setGridMode(gridMode);
        }
    }

    /**
     * Return whether or not grid mode is in effect
     * @todo For increased readability, instead of !isGridMode() everywhere,
     *  have an isFlowMode() method with reverse logic that I use instead.
     */
    public boolean isGridMode() {
//        return gridMode;
        return domProvider.isGridMode();
    }


//    // XXX Moved from document.
//    /**
//     *  Return true if this document is in "grid mode" (objects
//     *  should be positioned by absolute coordinates instead of in
//     *  "flow" order.
//     *
//     *  @return true iff the document should be in grid mode
//     */
//    public boolean isGridModeDocument() {
//        Element body = getHtmlBody();
//
//        if (body == null) {
//            return false;
//        }
//
////        Value val = CssLookup.getValue(b, XhtmlCss.RAVELAYOUT_INDEX);
//        CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(body, XhtmlCss.RAVELAYOUT_INDEX);
//
////        return val == CssValueConstants.GRID_VALUE;
//        return CssProvider.getValueService().isGridValue(cssValue);
//    }
    
//    public boolean isBraveheartPage() {
//        return domProvider.isBraveheartPage();
//    }
    
//    public boolean isWoodstockPage() {
//        return domProvider.isWoodstockPage();
//    }
    
//    /**
//     * Return the mapper responsible for converting coordinates to
//     * and from the model.
//     * @return the mapper instance
//     */
//    public ModelViewMapper getMapper() {
//        if (mapper == null) {
//            mapper = new ModelViewMapper(this);
//        }
//
//        return mapper;
//    }

// <refactoring>
//    public CssLookup getCss() {
//        if (css == null) {
//            css = new CssLookup(this);
//        }
//
//        return css;
//    }
// </refactoring>

//    /**
//     * Return true iff this webform represents a fragment. Note - portlets are
//     * frequently fragments too so don't conclude from this method returning true
//     * that you are not dealing with a portlet!
//     */
//    public boolean isFragment() {
////        return isFragment;
//        return domProvider.isFragment();
//    }
//
//    /**
//     * Retru true iff this webform represents a portlet
//     */
//    public boolean isPortlet() {
////        return isPortlet;
//        return domProvider.isPortlet();
//    }

//    /**
//     * Return the project associated with this webform
//     */
//    public Project getProject() {
//        return getModel().getProject();
//    }

    /** Return a cell renderer pane suitable for use with the designer.
     * Used to stamp out combo boxes etc. into offscreen buffers, since
     * they refuse to paint themselves properly if they are not parented
     * to a heavyweight component (e.g. JFrame). The CellRendererPane prevents
     * additions/removals from causing unnecessary traffic and recomputations
     * in other components.
     */
    public CellRendererPane getRenderPane() {
        return rendererPane;
    }

//    public void setRenderPane(CellRendererPane rendererPane) {
//        this.rendererPane = rendererPane;
//    }

//    /** Return the virtual forms support associated with this webform */
//    public VirtualFormSupport getVirtualFormSupport() {
//        if (virtualForms == null) {
//            virtualForms = new VirtualFormSupport(this);
//        }
//
//        return virtualForms;
//    }
    
//    public void setVirtualFormsEnabled(boolean virtualFormsEnabled) {
//        this.virtualFormsEnabled = virtualFormsEnabled;
//    }
//    
//    public boolean isVirtualFormsEnabled() {
//        return virtualFormsEnabled;
//    }

    /**
     * Return the color manager for the webform which stores
     * color preferences and determines suitable colors for
     * glyphs and nibs painted on top of the webform
     */
    public ColorManager getColors() {
        if (colors == null) {
            colors = new ColorManager(this);
        }

        return colors;
    }

//    /**
//     * Return the action handler for the designer which implements
//     * various user-operations that can be performed on the webform
//     */
//    public DesignerActions getActions() {
//        if (actions == null) {
//            actions = new DesignerActions(this);
//        }
//
//        return actions;
//    }

//    /** Return true iff the webform has rendering problems associated with it */
//    public boolean hasRenderingErrors() {
////        return renderFailureComponent != null;
////        return getRenderFailureComponent() != null;
//        return domProvider.hasRenderingErrors();
//    }

//    // XXX Very suspicious, revise it.
//    /** Sets render failed values about a failure in rendering it to HTML.
//     * @param component The component which failed to render
//     * @param exception The exception thrown by the component
//     */
//    private void setRenderFailedValues(MarkupDesignBean renderFailureComponent, Exception renderFailureException) {
////        renderFailure = exception;
////        renderFailureComponent = component;
//        domProvider.setRenderFailedValues(renderFailureComponent, renderFailureException);
//    }
    
//    private void setRenderFailureValues() {
//        domProvider.setRenderFailureValues();
//    }
//    
//    private boolean hasRenderFailure() {
//        return domProvider.hasRenderFailure();
//    }

//    /** Return the exception associated with the current render failure for this page */
//    public Exception getRenderFailure() {
////        return renderFailure;
//        return domProvider.getRenderFailureException();
//    }

//    /** Return the component associated with the current render failure for this page */
//    public MarkupDesignBean getRenderFailureComponent() {
////        return renderFailureComponent;
//        return domProvider.getRenderFailureComponent();
//    }

//    /** Return true iff the current render failure (returned by
//     * {@link #getRenderFailure} has been shown to the user yet
//     */
//    public boolean isRenderFailureShown() {
////        return renderFailureShown;
//        return domProvider.isRenderFailureShown();
//    }
//
//    /** Record whether the current render failure (returned by
//     * {@link #getRenderFailure} has been shown to the user yet
//     */
//    public void setRenderFailureShown(boolean renderFailureShown) {
////        this.renderFailureShown = renderFailureShown;
//        domProvider.setRenderFailureShown(renderFailureShown);
//    }

    // XXX Moved to designer/jsf/../JsfForm.
//    /** XXX Moved from FacesSupport. Updates erros in the corresponding component.
//     * TODO Usage of this after renderHtml call is very suspicious, revise. */
//    public void updateErrorsInComponent() {
////        FileObject markupFile = getModel().getMarkupFile();
////// <missing designtime api>
//////        Exception renderFailure = facesunit.getRenderFailure();
//////        MarkupDesignBean renderFailureComponent =
//////            (MarkupDesignBean)facesunit.getRenderFailureComponent();
////// ====
////        Exception renderFailure = InSyncService.getProvider().getRenderFailure(markupFile);
//        
////        Exception renderFailure = domProvider.getRenderFailure();
//////        MarkupDesignBean renderFailureComponent = (MarkupDesignBean)InSyncService.getProvider().getRenderFailureComponent(markupFile);
////        MarkupDesignBean renderFailureComponent = domProvider.getRenderFailureMarkupDesignBean();
////        
////// </missing designtime api>
////
////        setRenderFailedValues(renderFailureComponent, renderFailure);
//        setRenderFailureValues();
//
////        if (renderFailure == null) {
//        if (!hasRenderFailure()) {
//            // Since we had a successful render now, we should remember this such
//            // that if a rendering error happens again, we will show the errorpanel
//            setRenderFailureShown(false);
//        }
//
//        // XXX #6472138 Put into AWT.
//        updateComponentForErrors();
//    }
//    
//    private void updateComponentForErrors() {
//        if (EventQueue.isDispatchThread()) {
//            doUpdateComponentForErrors();
//        } else {
//            EventQueue.invokeLater(new Runnable() {
//                public void run() {
//                    doUpdateComponentForErrors();
//                }
//            });
//        }
//    }
//    
//    private void doUpdateComponentForErrors() {
////        if (getTopComponent().isShowing()) {
////            // In case some kind of rendering error happened
////            // Ugh... I need to track this differently!
////            getTopComponent().updateErrors();
////        }
//        domProvider.tcUpdateErrors(this);
//    }
    

    /**
     * Return the interaction manager which is responsible for
     * coordinating mouse and keyboard gestures with designer modes
     * and actions.
     */
    public InteractionManager getManager() {
        if (manager == null) {
            manager = new InteractionManager(this);
        }

        return manager;
    }
    
//    public GridHandler getGridHandler() {
//        if (gridHandler == null) {
//            gridHandler = new GridHandler(/*this*/);
//        }
//        return gridHandler;
//    }

    // XXX Moved to designer/jsf/../JsfForm.
//    /** Get the context page for this fragment. This method should only return non-null
//     * for page fragments. The context page is a page which provides a "style context" for
//     * the fragment. Typically, the page is one of the pages which includes the page fragment,
//     * but that's not strictly necessary. The key thing is that the page fragment will pick
//     * up stylesheets etc. defined in the head of the context page.
//     * @return A context page for the fragment
//     */
//    public WebForm getContextPage() {
////        if (isFragment && (contextPage == null)) {
//        if (isFragment() && (contextPage == null)) {
//            // Find a page
//            Iterator it =
////                DesignerService.getDefault().getWebPages(getProject(), true, false).iterator();
////                    InSyncService.getProvider().getWebPages(getProject(), true, false).iterator();
//                    domProvider.getWebPageFileObjectsInThisProject().iterator();
//
//            while (it.hasNext()) {
//                FileObject fo = (FileObject)it.next();
//
//                try {
//                    DataObject dobj = DataObject.find(fo);
//
//                    // XXX Very suspicious, how come that context page is any random page
//                    // whitin project?? What actually the context page is good for?
//                    // It seems it is a wrong architecture.
//                    if (isWebFormDataObject(dobj)) {
//                        contextPage = getWebFormForDataObject(dobj);
//
//                        break;
//                    }
//                } catch (DataObjectNotFoundException dnfe) {
//                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, dnfe);
//                }
//            }
//        }
//
//        return contextPage;
//    }

    // XXX Moved to designer/jsf/../JsfForm.
//    /** Set the associated context page for this page fragment. (Only allowed on
//     * page fragments.)
//     *  @see getContextPage()
//     */
//    public void setContextPage(WebForm contextPage) {
////        assert isFragment;
//        assert isFragment();
//
//        // XXX Context page notion from fragment should be removed.
//        if (this.contextPage != contextPage) {
//            // Force refresh such that the style links are recomputed
//            clearHtml();
//        }
//
//        this.contextPage = contextPage;
//    }

//    // Tor: The name is up to you.
//    public void destroy() {
//        detachTopComponent();
//
////        if (domSyncer != null) {
////            domSyncer.destroy();
////            domSyncer = null;
////        }
//        domProvider.destroyDomSynchronizer();
//
//        if (colors != null) {
//            colors.resetPageBox();
//            colors = null;
//        }
//    }

    // XXX Moved from Document.
    /**
     * Return a cache of images for this document
     */
    public ImageCache getImageCache() {
        if (imageCache == null) {
            imageCache = new ImageCache();
        }

        return imageCache;
    }
    
//    // XXX Moved from Document.
//    /** Return a cache of webform boxes associated with this document
//     * @todo Rename; it's no longer a box cache but rather a document
//     *   cache! */
//    public DocumentCache getFrameBoxCache() {
//        if (frameCache == null) {
//            frameCache = new DocumentCache();
//        }
//
//        return frameCache;
//    }
    
    // XXX Moved from Document.
    /** Return true iff the document has cached frame boxes */
    public boolean hasCachedFrameBoxes() {
//        return (frameCache != null) && (frameCache.size() > 0);
        return domProvider.hasCachedExternalFrames();
    }

    // XXX Moved from Document.
    /** Clear out caches for a "refresh" operation */
    private void flushCaches() {
        // XXX Moved to designer/jsf/../JsfForm.
//        if (frameCache != null) {
//            frameCache.flush();
//        }

        if (imageCache != null) {
            imageCache.flush();
        }
    }


//    //////
//    // XXX See Designer interface.
//    public void modelChanged() {
//        getPane().getPaneUI().resetPageBox();
//    }

//    public void nodeChanged(Node rendered, Node parent, boolean wasMove) {
    public void changeNode(Node rendered, Node parent, Element[] changedElements) {
        PageBox pageBox = getPane().getPaneUI().getPageBox();
        pageBox.changed(rendered, parent, changedElements);
    }

//    public void nodeRemoved(Node previouslyRendered, Node parent) {
    public void removeNode(Node previouslyRendered, Node parent) {
        PageBox pageBox = getPane().getPaneUI().getPageBox();
        pageBox.removed(previouslyRendered, parent);
    }
    
//    public void nodeInserted(Node rendered, Node parent) {
    public void insertNode(Node rendered, Node parent) {
        getPane().getPaneUI().getPageBox().inserted(rendered, parent);
    }
    
//    public void updateGridMode() {
////        setGridMode(getDocument().isGridMode()); // XXX
////        setGridMode(isGridModeDocument());
//        // XXX
//        updatePaneGrid(domProvider.isGridMode());
//    }

//    public void documentReplaced() {
    public void detachDomDocument() {
        // Ensure that the caret is in the new DOM
        DesignerPane pane = getPane();

        if (pane != null) {
//            if (pane.getCaret() != null) {
//                pane.getCaret().detachDom();
            if (pane.hasCaret()) {
                pane.caretDetachDom();

                //pane.setCaret(null);
            }
            //                pane.showCaretAtBeginning();
        }
    }
    // XXX
    //////

//    /** XXX Temporary only until all modification stuff is moved from designer to designer/jsf. */
//    public void setUpdatesSuspended(MarkupDesignBean markupDesignBean, boolean suspend) {
//        domProvider.setUpdatesSuspended(markupDesignBean, suspend);
//    }
    
//    public void setUpdatesSuspended(Element componentRootElement, boolean suspend) {
//        domProvider.setUpdatesSuspended(componentRootElement, suspend);
//    }

//    public boolean isRefreshPending() {
//        return domProvider.isRefreshPending();
//    }

////    public void attachContext(DesignContext context) {
////        domProvider.attachContext(context);
//    public void attachContext() {
//        domProvider.attachContext();
//    }
    
//    public void detachContext() {
//        domProvider.detachContext();
//    }

//    public DocumentFragment createSourceFragment(MarkupDesignBean bean) {
//        return domProvider.createSourceFragment(bean);
//    }

//    public void requestChange(MarkupDesignBean bean) {
//        domProvider.requestChange(bean);
//    }

//    public void beanChanged(MarkupDesignBean bean) {
//        domProvider.beanChanged(bean);
//    }

//    public void requestTextUpdate(MarkupDesignBean bean) {
//        domProvider.requestTextUpdate(bean);
//    }
    
    
//    ////// Moved form DesignerActions
//    // XXX Refreshing. Get rid of it, it represents an arch flaw.
//    /** Refresh the document
//     * @deep If true, go all the way down to the insync markup unit
//     *   and force a sync at that level, then invalidate all the page
//     *   and image caches etc.  If false, only flush the designer
//     *   portion: the layout tree, the rendered html sections from
//     *   markup, and the style, fragment and image caches.
//     */
//    public void refresh(boolean deep) {
//        getManager().finishInlineEditing(false);
//
////        // >>> Insync part
////        if (deep) {
////            MarkupUnit unit = getMarkup();
////
////            if (unit.getState() == Unit.State.MODELDIRTY) {
////                getModel().flush();
////            }
////
////            if (unit.getState() == Unit.State.CLEAN) {
////                unit.setSourceDirty();
////            }
////
////            if (unit.getState() == Unit.State.SOURCEDIRTY) {
////                getModel().sync();
////            }
////        }
////
//////        CssLookup.refreshEffectiveStyles(webform.getDom());
////        CssProvider.getEngineService().refreshStylesForDocument(getJspDom());
////        // XXX Should this be here too (or the above?).
////        CssProvider.getEngineService().refreshStylesForDocument(getHtmlDom());
////        
////        // XXX
//////        StyleSheetCache.getInstance().flush();
////        CssProvider.getEngineService().flushStyleSheetCache();
////        clearHtml();
////        // <<< Insync part.
//        getModel().refresh(deep);
//
//        
//        getDocument().flushCaches();
//
//        DesignerPane pane = getPane();
//
//        if (pane != null) {
//            //            PageBox pageBox = pane.getPageBox();
//            //
//            //            if (pageBox != null) {
//            //                pageBox.redoLayout(pane.isShowing());
//            //            }
//            //
//            pane.getPaneUI().resetPageBox();
//        }
//
//        getSelection().updateSelection(); // trigger refresh in CSS viewer for example
//
//        if (pane != null) {
//            pane.repaint();
//        }
//    }
//    public void refreshProject() {
//        domProvider.refreshProject();
//    }
    
//    public void refreshModel(final boolean deep) {
//        // #6483029 Refresh contained external forms (e.g. fragments) first.
//        refreshExternalForms(deep);
//        
//        domProvider.refreshModel(deep);
//    }
//    
//    private void refreshExternalForms(boolean deep) {
//        DesignerPane designerPane = getPane();
//        if (designerPane == null) {
//            // XXX #6495248 This is not opened yet, so not initialized yet.
//            // TODO The external forms may not be stored in ui components (boxes),
//            // they need to be findable from the model directly.
//            return;
//        }
//        
//        PageBox pageBox = designerPane.getPageBox();
//        if (pageBox == null) {
//            return;
//        }
//        WebForm[] externalForms = pageBox.findExternalForms();
//        for (WebForm externalForm : externalForms) {
//            if (this == externalForm) {
//                // XXX To prevent neverending loop if there is such case.
//                continue;
//            }
//            externalForm.refreshModel(deep);
//        }
//    }
    
    public DomProvider[] getExternalDomProviders() {
        DesignerPane designerPane = getPane();
        if (designerPane == null) {
            // XXX #6495248 This is not opened yet, so not initialized yet.
            // TODO The external forms may not be stored in ui components (boxes),
            // they need to be findable from the model directly.
            return new DomProvider[0];
        }
        
        PageBox pageBox = designerPane.getPageBox();
        if (pageBox == null) {
            return new DomProvider[0];
        }
        WebForm[] externalForms = pageBox.findExternalForms();
        
        List<DomProvider> domProviders = new ArrayList<DomProvider>();
        for (WebForm externalForm : externalForms) {
            if (externalForm == null || externalForm == this) {
                continue;
            }
            domProviders.add(externalForm.domProvider);
        }
        return domProviders.toArray(new DomProvider[domProviders.size()]);
    }
    
//    private void modelRefreshed() {
    public void resetAll() {
        getManager().finishInlineEditing(false);

        flushCaches();

        final DesignerPane pane = getPane();

        // #106167 This seems to be redundant here (see designer/jsf/../DomSynchronizer#processRefresh,
        // and /../JsfTopComponent#modelChanged.
//        if (pane != null) {
//            //            PageBox pageBox = pane.getPageBox();
//            //
//            //            if (pageBox != null) {
//            //                pageBox.redoLayout(pane.isShowing());
//            //            }
//            //
//            pane.getPaneUI().resetPageBox();
//        }

        // XXX #106332 Bad architecture, there were changed beans instanes in the hierarchy,
        // and at this moment the rendered doc is not regenerated.
        EventQueue.invokeLater(new Runnable() {
            public void run() {
        //        getSelection().updateSelection(); // trigger refresh in CSS viewer for example
                getSelection().updateNodes();

                if (pane != null) {
                    pane.repaint();
                }
            }
        });
    }
    

//    /** Refresh all forms in the project */
//    public static void refreshProject(Project project, boolean deep) {
//        FileObject fobj = JsfProjectUtils.getDocumentRoot(project);
//        refreshFolder(fobj, deep);
//    }
//
//    /** Refresh the given DataObject, if it's a webform */
//    public static void refreshDataObject(DataObject dobj, boolean deep) {
//        if (hasWebFormForDataObject(dobj)) {
////            WebForm webform = WebForm.getWebFormForDataObject(WebForm.findDomProvider(dobj), dobj);
////            webform.getActions().refresh(deep);
////            WebForm webform = WebForm.findWebForm(dobj);
//            // XXX Really get, not find only? Revise.
//            WebForm webform = getWebFormForDataObject(dobj);
//            if (webform != null) {
////                webform.refresh(deep);
//                webform.refreshModel(deep);
//            }
//        }
//    }
//
//    private static void refreshFolder(FileObject folder, boolean deep) {
//        FileObject[] children = folder.getChildren();
//
//        for (int i = 0; i < children.length; i++) {
//            FileObject fo = children[i];
//
//            if (fo.isFolder()) {
//                refreshFolder(fo, deep);
//            } else {
//                try {
//                    DataObject dobj = DataObject.find(fo);
////                    refresh(dobj, deep);
//                    refreshDataObject(dobj, deep);
//                } catch (DataObjectNotFoundException dnfe) {
//                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, dnfe);
//                }
//            }
//        }
//    }
    // XXX
    //////

    //////
    // XXX Temporary, see Designer interface
//    public void contextActivated(DesignContext designContext) {
//        if (view != null) {
//            view.contextActivated(designContext);
//        }
//    }

//    public void contextDeactivated(DesignContext designContext) {
//        if (view != null) {
//            view.contextDeactivated(designContext);
//        }
//    }

////    public void contextChanged(DesignContext designContext) {
//    private void designContextGenerationChanged() {
////        if (view != null) {
////            view.designContextGenerationChanged();
////        }
//        domProvider.tcDesignContextGenerationChanged(this);
//    }

//    public void beanCreated(DesignBean designBean) {
//        if (view != null) {
//            view.beanCreated(designBean);
//        }
//    }

//    public void beanDeleted(DesignBean designBean) {
//        if (view != null) {
//            view.beanDeleted(designBean);
//        }
//    }

//    public void beanMoved(DesignBean designBean, DesignBean designBean0, Position position) {
//        if (view != null) {
//            view.beanMoved(designBean, designBean0, position);
//        }
//    }

//    public void beanContextActivated(DesignBean designBean) {
//        if (view != null) {
//            view.beanContextActivated(designBean);
//        }
//    }

//    public void beanContextDeactivated(DesignBean designBean) {
//        if (view != null) {
//            view.beanContextDeactivated(designBean);
//        }
//    }

//    public void instanceNameChanged(DesignBean designBean, String string) {
//        if (view != null) {
//            view.instanceNameChanged(designBean, string);
//        }
//    }

//    public void beanChanged(DesignBean designBean) {
//        if (view != null) {
//            view.beanChanged(designBean);
//        }
//    }

//    public void propertyChanged(DesignProperty designProperty, Object object) {
//        if (view != null) {
//            view.propertyChanged(designProperty, object);
//        }
//    }

//    public void eventChanged(DesignEvent designEvent) {
//        if (view != null) {
//            view.eventChanged(designEvent);
//        }
//    }
    // XXX
    //////

    // >>> DnD
//    DataFlavor getImportFlavor(DataFlavor[] flavors) {
//        return domProvider.getImportFlavor(flavors);
//    }

    boolean canImport(JComponent comp, DataFlavor[] transferFlavors, Transferable transferable) {
        return domProvider.canImport(comp, transferFlavors, transferable);
    }

//    DesignBean[] pasteBeans(Transferable t, DesignBean parent, MarkupPosition pos, Point location, DomProvider.CoordinateTranslator coordinateTranslator) {
//        return domProvider.pasteBeans(t, parent, pos, location, coordinateTranslator);
//    }
//    Element[] pasteComponents(Transferable t, Element parentComponentRootElement, Point location) {
//        return domProvider.pasteComponents(t, parentComponentRootElement, location);
//    }

//    void importData(JComponent comp, Transferable t, Object transferData, Dimension dimension, DomProvider.Location location, DomProvider.CoordinateTranslator coordinateTranslator, int dropAction) {
//        domProvider.importData(comp, t, transferData, dimension, location, coordinateTranslator, dropAction);
//    }

//    void importString(String string, DomProvider.Location location, DomProvider.CoordinateTranslator coordinateTranslator) {
//        domProvider.importString(string, location, coordinateTranslator);
//    }

//    public DesignBean findHtmlContainer(DesignBean parent) {
//        return domProvider.findHtmlContainer(parent);
//    }

//    String[] getClassNames(DisplayItem[] displayItems) {
//        return domProvider.getClassNames(displayItems);
//    }

//    boolean importBean(DisplayItem[] items, DesignBean origParent, int nodePos, String facet, List createdBeans, DomProvider.Location location, DomProvider.CoordinateTranslator coordinateTranslator)
//    throws IOException {
//        return domProvider.importBean(items, origParent, nodePos, facet, createdBeans, location, coordinateTranslator);
//    }

//    MarkupPosition getDefaultMarkupPositionUnderParent(DesignBean parent) {
//        return domProvider.getDefaultPositionUnderParent(parent);
//    }

//    int computeActions(DesignBean droppee, Transferable transferable, boolean searchUp, int nodePos) {
//        return domProvider.computeActions(droppee, transferable, searchUp, nodePos);
//    }
    int computeActions(Element dropeeComponentRootElement, Transferable transferable) {
        return domProvider.computeActions(dropeeComponentRootElement, transferable);
    }

//    DesignBean findParent(String className, DesignBean droppee, Node parentNode, boolean searchUp) {
//        return domProvider.findParent(className, droppee, parentNode, searchUp);
//    }

//    int processLinks(Element origElement, Class[] classes, List beans, boolean selectFirst, boolean handleLinks, boolean showLinkTarget) {
    int processLinks(Element origElement, Element componentRootElement) {
        return domProvider.processLinks(origElement, componentRootElement);
    }
    // <<< DnD

    // >>> DnD callbacks
    public void showDropMatch(Element componentRootElement, Element regionElement, int dropType) {
        getPane().getDndHandler().showDropMatch(componentRootElement, regionElement, dropType);
    }
    
    public void clearDropMatch() {
        getPane().getDndHandler().clearDropMatch();
    }
    
//    /*public*/private void select(final /*DesignBean designBean*/Element componentRootElement) {
    public void selectComponentDelayed(final /*DesignBean designBean*/Element componentRootElement) {
//        getSelection().selectBean(designBean);
//        if (designBean instanceof MarkupDesignBean) {
        if (componentRootElement != null) {
            // XXX Bad arch. Events are fired too early, no rendered tree was created yet.
            // Scheduling later to by pass the issue (of retrieving the component root element).
            EventQueue.invokeLater(new Runnable() {
                public void run() {
//                    getSelection().selectBean(
//                            WebForm.getDomProviderService().getComponentRootElementForMarkupDesignBean((MarkupDesignBean)designBean));
                    getSelection().selectBean(componentRootElement);
                }
            });
        }
        // #6338212 Do not request unnecessary activation.
//        getTopComponent().requestActive();
    }
    
//    public void refreshForm(boolean deep) {
//        refresh(deep);
//    }
    
//    /*public*/private void inlineEdit(/*DesignBean[] designBeans*/Element[] componentRootElements) {
    public void inlineEditComponents(Element[] componentRootElements) {
//        List<Element> componentRootElements = new ArrayList<Element>();
//        for (DesignBean designBean : designBeans) {
//            if (designBean instanceof MarkupDesignBean) {
//                Element componentRootElement = WebForm.getDomProviderService().getComponentRootElementForMarkupDesignBean((MarkupDesignBean)designBean);
//                if (componentRootElement != null) {
//                    componentRootElements.add(componentRootElement);
//                }
//            }
//        }
//        getManager().inlineEdit(componentRootElements.toArray(new Element[componentRootElements.size()]));
        getManager().inlineEdit(componentRootElements);
        // XXX #6484250 To activate the window after drop.
//        getTopComponent().requestActive();
//        tcRequestActive();
    }
    // <<< DnD callbacks

//    private void destroyDomProvider(DataObject dobj) {
//        domProvider.removeForDataObject(dobj);
//    }

//    public void destroyDesigner() {
//        destroy();
//    }

//    public void registerListeners() {
////        domProviderListener = new DomProviderListener(this);
////        domProvider.addDomProviderListener((DomProvider.DomProviderListener)WeakListeners.create(
////                DomProvider.DomProviderListener.class, domProviderListener, domProvider));
//        
////        // XXX FIXME There are more calls then needed. This is a hack to avoid multiple registering.
////        domProvider.removeDomProviderListener(domProviderListener);
////        domProvider.addDomProviderListener(domProviderListener);
//                
//        DataObject jspDataObject = getJspDataObject();
//        if (jspDataObject != null) {
//            jspDataObjectListener = new JspDataObjectListener(this);
//            jspDataObject.addPropertyChangeListener(WeakListeners.propertyChange(jspDataObjectListener, jspDataObject));
//        }
//    }
//
//    public void unregisterListeners() {
////        // XXX Or don't use weak listener, and remove it explicitely.
//////        domProviderListener = null;
////        domProvider.removeDomProviderListener(domProviderListener);
//        
//        jspDataObjectListener = null;
//    }

    public URL getBaseUrl() {
        return domProvider.getBaseUrl();
    }

    public URL resolveUrl(String urlString) {
        return domProvider.resolveUrl(urlString);
    }

//    public DocumentFragment renderHtmlForMarkupDesignBean(MarkupDesignBean markupDesignBean) {
//        return domProvider.renderHtmlForMarkupDesignBean(markupDesignBean);
//    }
    
    public PaletteController getPaletteController() {
        return domProvider.getPaletteController();
    }

//    // XXX
////    boolean editEventHandlerForDesignBean(DesignBean designBean) {
////        return domProvider.editEventHandlerForDesignBean(designBean);
////    }
//    boolean editEventHandlerForComponent(Element componentRootElement) {
//        return domProvider.editEventHandlerForComponent(componentRootElement);
//    }
    
    // >>> Designer impl
//    public JComponent getDesignerComponent() {
//        return getTopComponent();
//    }

    // XXX Temp after moved TopComponent impl out >>>
//    public JComponent getVisualRepresentation() {
//        return getTopComponent().getVisualRepresentation();
//    }

//    public JComponent getToolbarRepresentation() {
//        return getTopComponent().getToolbarRepresentation();
//    }

//    public Action[] getActions() {
//        return getTopComponent().getActions();
//    }

//    public Lookup getLookup() {
//        return getTopComponent().getLookup();
//    }

//    public void componentOpened() {
//        getTopComponent().componentOpened();
//    }

//    public void componentClosed() {
//        getTopComponent().componentClosed();
//    }

//    public void componentShowing() {
//        getTopComponent().componentShowing();
//    }

//    public void componentHidden() {
//        getTopComponent().componentHidden();
//    }

//    public void componentActivated() {
//        getTopComponent().componentActivated();
//    }

//    public void componentDeactivated() {
//        getTopComponent().componentDeactivated();
//    }

//    public UndoRedo getUndoRedo() {
//        return getTopComponent().getUndoRedo();
//    }

//    public void setMultiViewCallback(MultiViewElementCallback multiViewElementCallback) {
//        getTopComponent().setMultiViewCallback(multiViewElementCallback);
//    }

//    public CloseOperationState canCloseElement() {
//        return getTopComponent().canCloseElement();
//    }

    // XXX Temp after moved TopComponent impl out <<<
    // <<< Designer impl.

//    boolean canDropDesignBeansAtNode(DesignBean[] designBeans, Node node) {
//        return domProvider.canDropDesignBeansAtNode(designBeans, node);
//    }
    boolean canDropComponentsAtNode(Element[] componentRootElements, Node node) {
        return domProvider.canDropComponentsAtNode(componentRootElements, node);
    }

//    boolean handleMouseClickForElement(Element element, int clickCount) {
//        return domProvider.handleMouseClickForElement(element, clickCount);
//    }

    // XXX, Also better name needed.
//    boolean isNormalAndHasFacesBean(MarkupDesignBean markupDesignBean) {
//        return domProvider.isNormalAndHasFacesBean(markupDesignBean);
//    }
    boolean isNormalAndHasFacesComponent(Element componentRootElement) {
        return domProvider.isNormalAndHasFacesComponent(componentRootElement);
    }

//    boolean canHighlightMarkupDesignBean(MarkupDesignBean markupDesignBean) {
//        return domProvider.canHighlightMarkupDesignBean(markupDesignBean);
//    }

//    public DesignBean createBean(String className, Node parent, Node before) {
//        return domProvider.createBean(className, parent, before);
//    }
//    public Element createComponent(String className, Node parent, Node before) {
//        return domProvider.createComponent(className, parent, before);
//    }


//    public boolean isFormBean(DesignBean designBean) {
//        return domProvider.isFormBean(designBean);
//    }

//    public Element getDefaultParentMarkupBeanElement() {
//        return domProvider.getDefaultParentMarkupBeanElement();
//    }

//    public boolean moveBean(DesignBean bean, Node parentNode, Node before) {
//        return domProvider.moveBean(bean, parentNode, before);
//    }
    public boolean moveComponent(Element componentRootElement, Node parentNode, Node before) {
        return domProvider.moveComponent(componentRootElement, parentNode, before);
    }

//    boolean setPrerenderedBean(MarkupDesignBean markupDesignBean, DocumentFragment documentFragment) {
//        return domProvider.setPrerenderedBean(markupDesignBean, documentFragment);
//    }

//    MarkupDesignBean getMarkupDesignBeanEquivalentTo(MarkupDesignBean oldBean) {
//        return domProvider.getMarkupDesignBeanEquivalentTo(oldBean);
//    }

//    org.openide.nodes.Node getRootBeanNode() {
//        return domProvider.getRootBeanNode();
//    }

//    public void deleteBean(DesignBean designBean) {
//        domProvider.deleteBean(designBean);
//    }
//    public void deleteComponent(Element componentRootElement) {
//        domProvider.deleteComponent(componentRootElement);
//    }

//    boolean canCreateBean(String className, DesignBean parent, Position pos) {
//        return domProvider.canCreateBean(className, parent, pos);
//    }

//    DesignBean getDefaultParentBean() {
//        return domProvider.getDefaultParentBean();
//    }
    public Element getDefaultParentComponent() {
        return domProvider.getDefaultParentComponent();
    }

//    JComponent getErrorPanel() {
//        DomProvider.ErrorPanel errorPanel = domProvider.getErrorPanel(new ErrorPanelCallbackImpl(this));
//        if (errorPanel instanceof JComponent) {
//            return (JComponent)errorPanel;
//        } else {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
//                    new IllegalStateException("The provided error panel is not of JComponent type, errorPanel=" + errorPanel)); // NOI18N
//            return null;
//        }
//    }

//    public void syncModel() {
//        domProvider.syncModel();
//    }

//    boolean isSourceDirty() {
//        return domProvider.isSourceDirty();
//    }

//    Transferable copyBeans(DesignBean[] beans) {
//        return domProvider.copyBeans(beans);
//    }
//    Transferable copyComponents(Element[] componentRootElements) {
//        return domProvider.copyComponents(componentRootElements);
//    }

//    public DomProvider.WriteLock writeLock(String message) {
//        return domProvider.writeLock(message);
//    }
//    
//    public void writeUnlock(DomProvider.WriteLock writeLock) {
//        domProvider.writeUnlock(writeLock);
//    }

    public boolean isModelValid() {
        return domProvider.isModelValid();
    }

    void readLock() {
        domProvider.readLock();
    }

    void readUnlock() {
        domProvider.readUnlock();
    }

//    void setModelActivated(boolean activated) {
//        domProvider.setModelActivated(activated);
//    }

//    UndoRedo getUndoManager() {
//        return domProvider.getUndoManager();
//    }

    public boolean isModelBusted() {
        return domProvider.isModelBusted();
    }

//    DesignBean[] getBeansOfType(Class clazz) {
//        return domProvider.getBeansOfType(clazz);
//    }

//    Project getProject() {
//        return domProvider.getProject();
//    }

//    boolean isWriteLocked() {
//        return domProvider.isWriteLocked();
//    }

//    Class getBeanClass(String className) throws ClassNotFoundException {
//        return domProvider.getBeanClass(className);
//    }

//    public boolean isPage() {
//        return domProvider.isPage();
//    }

//    public boolean isAlive() {
//        return domProvider.isAlive();
//    }

//    String getImageComponentClassName() {
//        return domProvider.getImageComponentClassName();
//    }

//    void paintVirtualForms(Graphics2D g,DomProvider.RenderContext renderContext) {
//        domProvider.paintVirtualForms(g, renderContext);
//    }

    public boolean isFormComponent(Element componentRootElement) {
        return domProvider.isFormComponent(componentRootElement);
    }

    int getDropType(/*DesignBean origDroppee,*/Element origDropeeComponentRootElement, Element droppeeElement, Transferable t, boolean linkOnly) {
        return domProvider.getDropType(/*origDroppee,*/origDropeeComponentRootElement, droppeeElement, t, linkOnly);
    }

//    int getDropTypeForClassNames(DesignBean origDroppee, Element droppeeElement, String[] classNames, DesignBean[] beans, boolean linkOnly) {
//        return domProvider.getDropTypeForClassNames(origDroppee, droppeeElement, classNames, beans, linkOnly);
//    }
    int getDropTypeForComponent(/*DesignBean origDroppee,*/Element origDropeeComponentRootElement, Element droppeeElement, Element componentRootElement, boolean linkOnly) {
        return domProvider.getDropTypeForComponent(/*origDroppee,*/origDropeeComponentRootElement, droppeeElement, componentRootElement, linkOnly);
    }

    Element getComponentRootElementEquivalentTo(Element oldComponentRootElement) {
        return domProvider.getComponentRootElementEquivalentTo(oldComponentRootElement);
    }

    boolean canHighlightComponentRootElement(Element componentRootElement) {
        return domProvider.canHighlightComponentRootElmenet(componentRootElement);
    }

    InlineEditorSupport createInlineEditorSupport(Element componentRootElement, String propertyName) {
        return domProvider.createInlineEditorSupport(componentRootElement, propertyName);
    }

//    void dumpHtmlMarkupForNode(org.openide.nodes.Node node) {
//        domProvider.dumpHtmlMarkupForNode(node);
//    }

    boolean canPasteTransferable(Transferable trans) {
        return domProvider.canPasteTransferable(trans);
    }

    void importString(String string, Point canvasPos, Node documentPosNode, int documentPosOffset, Dimension dimension, boolean isGrid,
    Element droppeeElement, Element dropeeComponentRootElement, Element defaultParentComponentRootElement) {
        domProvider.importString(this, string, canvasPos, documentPosNode, documentPosOffset, dimension, isGrid,
                droppeeElement, dropeeComponentRootElement, defaultParentComponentRootElement);
    }

    boolean importData(JComponent comp, Transferable t, /*Object transferData,*/ Point canvasPos, Node documentPosNode, int documentPosOffset, Dimension dimension, boolean isGrid,
    Element droppeeElement, Element dropeeComponentRootElement, Element defaultParentComponentRootElement, int dropAction) {
        return domProvider.importData(this, comp, t, /*transferData,*/ canvasPos, documentPosNode, documentPosOffset, dimension, isGrid,
                droppeeElement, dropeeComponentRootElement, defaultParentComponentRootElement, dropAction);
    }

    // >> Designer implementation
    public void startInlineEditing(Element componentRootElement, String propertyName) {
        getManager().startInlineEditing(componentRootElement, propertyName);
    }

    public void selectComponent(Element componentRootElement) {
        getSelection().selectComponent(componentRootElement);
    }
    
    public int getSelectedCount() {
        return getSelection().getNumSelected();
    }
    
    public Element[] getSelectedComponents() {
        return getSelection().getSelectedComponentRootElements();
    }
    
//    public void snapToGrid() {
//        getGridHandler().snapToGrid();
//    }

//    public void align(Designer.Alignment alignment) {
//        getGridHandler().align(alignment);
//    }
    // << Designer Implmenetation
    
    
    // XXX Model <-> View mapping >>>
    /**
     * Converts the given location in the model to a place in
     * the view coordinate system.
     * The component must have a non-zero positive size for
     * this translation to be computed.
     *
     * @param tc the text component for which this UI is installed
     * @param pos the local location in the model to translate >= 0
     * @return the coordinates as a rectangle, null if the model is not painted
     * @exception BadLocationException  if the given position does not
     *   represent a valid location in the associated document
     * @see DesignerPaneBaseUI#modelToView
     */
    public Rectangle modelToView(/*DesignerPaneBase tc, Position pos*/ DomPosition pos) {
        if(DesignerUtils.DEBUG) {
            DesignerUtils.debugLog(getClass().getName() + ".modelToView(DesignerPaneBase, Position)");
        }
//        if(tc == null) {
//            throw(new IllegalArgumentException("Null designer pane."));
//        }
        if(pos == null) {
            throw(new IllegalArgumentException("Null position."));
        }
        
//        WebForm webform = editor.getWebForm();
        
//        if (!webform.getModel().isValid()) {
//        if (!webform.isModelValid()) {
        if (!isModelValid()) {
            return null;
        }
        
//        Document doc = editor.getDocument();
        
        // XXX Lock insync
//        doc.readLock();
//        webform.getMarkup().readLock();
//        webform.readLock();
        readLock();
        
        try {
//            return pageBox.modelToView(pos);
//            return ModelViewMapper.modelToView(pageBox, pos);
            return ModelViewMapper.modelToView(getPane().getPageBox(), pos);
        } finally {
            // XXX Unlock insync
//            doc.readUnlock();
//            webform.getMarkup().readUnlock();
//            webform.readUnlock();
            readUnlock();
        }
    }
    
    /**
     * Converts the given place in the view coordinate system
     * to the nearest representative location in the model.
     * The component must have a non-zero positive size for
     * this translation to be computed.
     *
     * @param tc the text component for which this UI is installed
     * @param pt the location in the view to translate.  This
     *  should be in the same coordinate system as the mouse events.
     * @return the offset from the start of the document >= 0,
     *   -1 if not painted
     * @see DesignerPaneBaseUI#viewToModel
     */
//    public Position viewToModel(/*DesignerPaneBase tc,*/ Point pt) {
    public DomPosition viewToModel(/*DesignerPaneBase tc,*/ Point pt) {
//        Position pos = Position.NONE;
//        Document doc = editor.getDocument();
        
        // XXX Lock insync
//        doc.readLock();
//        WebForm webform = editor.getWebForm();
//        webform.getMarkup().readLock();
//        webform.readLock();
        readLock();
        
        try {
//            pos = ModelViewMapper.viewToModel(doc.getWebForm(), pt.x, pt.y); //, alloc, biasReturn);
            return ModelViewMapper.viewToModel(this, pt.x, pt.y); //, alloc, biasReturn);
            
            // I'm now relying on clients to do this themselves!
            //assert offs == Position.NONE || Position.isSourceNode(offs.getNode());
        } finally {
//            doc.readUnlock();
//            webform.getMarkup().readUnlock();
//            webform.readUnlock();
            readUnlock();
        }
        
//        return pos;
    }

    public boolean isInlineEditing() {
        return getManager().isInlineEditing();
    }

    public DomPosition createDomPosition(Node node, int offset, Bias bias) {
        return domProvider.createDomPosition(node, offset, bias);
    }

    public DomPosition createDomPosition(Node node, boolean after) {
        return domProvider.createDomPosition(node, after);
    }
    
    public DomRange createDomRange(Node dotNode, int dotOffset, Node markNode, int markOffset) {
        return domProvider.createDomRange(dotNode, dotOffset, markNode, markOffset);
    }

    public int compareBoundaryPoints(Node endPointA, int offsetA, Node endPointB, int offsetB) {
        return domProvider.compareBoundaryPoints(endPointA, offsetA, endPointB, offsetB);
    }

    DomPosition first(DomPosition dot, DomPosition mark) {
        return domProvider.first(dot, mark);
    }

    DomPosition last(DomPosition dot, DomPosition mark) {
        return domProvider.last(dot, mark);
    }

    // XXX Model <-> View mapping <<<

    public WebForm findExternalForm(URL url) {
        // XXX There could be more designers per one domProvider.
        Designer[] externalDesigners = domProvider.getExternalDesigners(url);
        if (externalDesigners.length > 0 && externalDesigners[0] instanceof WebForm) {
            return (WebForm)externalDesigners[0];
        } else {
//            return WebForm.EXTERNAL;
            return null;
        }
    }
    
    public void reuseCssStyle(WebForm webForm) {
        if (webForm == null) {
            return;
        }
        domProvider.reuseCssStyle(webForm.domProvider);
    }
    
    
//    private static class DomProviderListener implements DomProvider.DomProviderListener {
//        private final WebForm webForm;
//        
//        public DomProviderListener(WebForm webForm) {
//            this.webForm = webForm;
//        }
//        
////        public void modelChanged() {
////            webForm.modelChanged();
////        }
//        
////        public void modelRefreshed() {
////            webForm.modelRefreshed();
////        }
//
////        public void nodeChanged(Node rendered, Node parent, boolean wasMove) {
////            webForm.nodeChanged(rendered, parent, wasMove);
////        }
//
////        public void nodeRemoved(Node previouslyRendered, Node parent) {
////            webForm.nodeRemoved(previouslyRendered, parent);
////        }
//
////        public void nodeInserted(Node rendered, Node parent) {
////            webForm.nodeInserted(rendered, parent);
////        }
//
////        public void updateErrorsInComponent() {
////            webForm.updateErrorsInComponent();
////        }
//
////        public void updateGridMode() {
////            webForm.updateGridMode();
////        }
////        public void gridModeUpdated(boolean gridMode) {
////            webForm.updatePaneGrid(gridMode);
////        }
//
////        public void documentReplaced() {
////            webForm.documentReplaced();
////        }
//
////        public void showDropMatch(Element componentRootElement, Element regionElement, int dropType) {
////            webForm.showDropMatch(componentRootElement, regionElement, dropType);
////        }
//        
////        public void clearDropMatch() {
////            webForm.clearDropMatch();
////        }
//
////        public void select(/*DesignBean designBean*/ Element componentRootElement) {
//////            webForm.select(designBean);
////            webForm.select(componentRootElement);
////        }
//
////        public void refreshForm(boolean deep) {
////            webForm.refreshForm(deep);
////        }
//
////        public void inlineEdit(/*DesignBean[] designBeans*/ Element[] componentRootElements) {
//////            webForm.inlineEdit(designBeans);
////            webForm.inlineEdit(componentRootElements);
////        }
//
////        public void designContextActivated(DesignContext designContext) {
////            webForm.contextActivated(designContext);
////        }
//
////        public void designContextDeactivated(DesignContext designContext) {
////            webForm.contextDeactivated(designContext);
////        }
//
////        public void designContextGenerationChanged() {
////            webForm.designContextGenerationChanged();
////        }
//
////        public void designBeanCreated(DesignBean designBean) {
////            webForm.beanCreated(designBean);
////        }
//
////        public void designBeanDeleted(DesignBean designBean) {
////            webForm.beanDeleted(designBean);
////        }
//
////        public void designBeanMoved(DesignBean designBean, DesignBean designBean0, Position position) {
////            webForm.beanMoved(designBean, designBean0, position);
////        }
//
////        public void designBeanContextActivated(DesignBean designBean) {
////            webForm.beanContextActivated(designBean);
////        }
//
////        public void designBeanContextDeactivated(DesignBean designBean) {
////            webForm.beanContextDeactivated(designBean);
////        }
//
////        public void designBeanNameChanged(DesignBean designBean, String string) {
////            webForm.instanceNameChanged(designBean, string);
////        }
//
////        public void designBeanChanged(DesignBean designBean) {
////            webForm.beanChanged(designBean);
////        }
//
////        public void designPropertyChanged(DesignProperty designProperty, Object object) {
////            webForm.propertyChanged(designProperty, object);
////        }
//
////        public void designEventChanged(DesignEvent designEvent) {
////            webForm.eventChanged(designEvent);
////        }
//    } // End of DomProviderListener.
    

    /** Dummy impl of <code>DomProviderService</code>. */
    private static class DummyDomProviderService implements DomProviderService {
//        public DomProvider getDomProvider(DataObject dataObject) {
//            return null;
//        }
//
//        public DomProvider findDomProvider(DataObject dobj) {
//            return null;
//        }
//
//        public DomProvider findDomProvider(DesignContext designContext) {
//            return null;
//        }
//
//        public boolean hasDomProvider(DataObject dataObject) {
//            return false;
//        }
//
//        public DomProviderService getDomProviderService() {
//            return this;
//        }

//        // DomProviderService >>
//        public MarkupDesignBean getMarkupDesignBeanForElement(Element element) {
//            return null;
//        }

//        public MarkupMouseRegion getMarkupMouseRegionForElement(Element element) {
//            return null;
//        }

        public int getExpandedOffset(String unexpanded, int unexpandedOffset) {
            // XXX
            return unexpandedOffset;
        }
        
        public int getUnexpandedOffset(String unexpanded, int expandedOffset) {
            // XXX
            return expandedOffset;
        }

        public String expandHtmlEntities(String html, boolean warn, Node node) {
            // XXX
            return html;
        }
        
//        public ClassLoader getContextClassLoaderForDesignContext(DesignContext designContext) {
//            // XXX
//            return Thread.currentThread().getContextClassLoader();
//        }
        
        public String getHtmlStream(Node node) {
            return node == null ? "" : node.toString(); // NOI18N
        }
        // DomProviderService <<

        public String getDomDocumentReplacedEventConstant() {
            // XXX
            return "DOMDocumentReplaced"; // NOI18N
        }

//        public Designer[] getDesignersForDataObject(DataObject jsfJspDataObject) {
//            return new Designer[0];
//        }

//        public Designer[] findDesignersForFileObject(FileObject jsfJspFileObject) {
//            return new Designer[0];
//        }

//        public Designer[] findDesignersForDesignContext(DesignContext designContext) {
//            return new Designer[0];
//        }

        public Designer[] findDesignersForElement(Element element) {
            return new Designer[0];
        }

//        public MarkupDesignBean getMarkupDesignBeanForComponentRootElement(Element element, Element parentBoxElement) {
//            return null;
//        }
        
//        public boolean isValueBindingExpression(String value, boolean containsOK) {
//            return false;
//        }

//        public String computeFileName(Object location) {
//            return location == null ? null : location.toString();
//        }

//        public int computeLineNumber(Object location, int lineno) {
//            return lineno;
//        }

//        public URL getDocumentUrl(org.w3c.dom.Document document) {
//            return null;
//        }

//        public void displayErrorForLocation(String message, Object location, int lineno, int column) {
//        }

//        public Element getHtmlBodyForDocument(org.w3c.dom.Document document) {
//            return null;
//        }

//        public DocumentFragment getHtmlDomFragmentForDocument(org.w3c.dom.Document document) {
//            return null;
//        }

//        public boolean editEventHandlerForDesignBean(DesignBean component) {
//            return false;
//        }

//        public boolean isSpecialBean(DesignBean designBean) {
//            return false;
//        }
        
//        public Element getElement(DesignBean designBean) {
//            return null;
//        }
        
        public Element getSourceElement(Element componentRootElement) {
            return null;
        }

//        public Element getMarkupBeanElement(DesignBean designBean) {
//            return null;
//        }

//        public boolean isFacesBean(MarkupDesignBean bean) {
//            return false;
//        }

//        public boolean setDesignProperty(MarkupDesignBean bean, String attribute, int value) {
//            return false;
//        }
//        public boolean setStyleAttribute(Element componentRootElement, String attribute, int value) {
//            return false;
//        }

//        public Element findHtmlElementDescendant(DocumentFragment df) {
//            return null;
//        }

//        public void updateLocalStyleValuesForElement(Element e, StyleData[] setStyleData, StyleData[] removeStyleData) {
//            // XXX.
//        }

//        public boolean isTrayBean(DesignBean designBean) {
//            return false;
//        }

//        public boolean isCssPositionable(DesignBean designBean) {
//            return false;
//        }

//        public long getContextGenearation(DesignContext context) {
//            return 0L;
//        }

//        public boolean isWebFormFileObject(FileObject fileObject) {
//            return false;
//        }

        public boolean isPrincipalElement(Element element, Element parentBoxElement) {
            return false;
        }

        public boolean isFocusedElement(Element element) {
            return false;
        }

        public boolean ignoreDesignBorder(Element element) {
            return true;
        }

        public Element getSourceElementWhichRendersChildren(Element element) {
            return element;
        }

        public Element[] getChildComponentRootElements(Element componentRootElement) {
            return new Element[0];
        }

//        public MarkupDesignBean adjustRenderBeanHack(MarkupDesignBean renderBean) {
//            return renderBean;
//        }

//        public boolean isFacesComponentBean(DesignBean bean) {
//            return false;
//        }
//
//        public boolean isEscapedDesignBean(DesignBean bean) {
//            return false;
//        }

        public boolean isFacesComponent(Element componentRootElement) {
            return false;
        }

//        public Element getRenderedElement(DesignBean designBean) {
//            return null;
//        }

        public String getRegionDisplayName(Element regionElement) {
            return regionElement == null ? null : regionElement.getLocalName();
        }

        public boolean isSameRegionOfElement(Element regionElement, Element element) {
            return regionElement == element;
        }

//        public Element getComponentRootElementForMarkupDesignBean(MarkupDesignBean markupDesignBean) {
//            return null;
//        }

        public String getInstanceName(Element componentRootElement) {
            return null;
        }

        public boolean isIncludeComponentBox(Element componentRootElement) {
            return false;
        }

        public boolean isSpecialComponent(Element componentRootElement) {
            return false;
        }

        public boolean isTrayComponent(Element componentRootElement) {
            return false;
        }

        public boolean isCssPositionable(Element componentRootElement) {
            return false;
        }
        
        public boolean isEscapedComponent(Element componentRootElement) {
            return false;
        }

        public Element getParentComponent(Element componentRootElement) {
            return null;
        }

        public boolean isContainerComponent(Element componentRootElement) {
            return false;
        }

//        public String[] getEditablePropertyNames(Element componentRootElement) {
//            return new String[0];
//        }

        public String[] getEditableProperties(Element componentRootElement) {
            return new String[0];
        }

//        public int getResizeConstraintsForComponent(Element componentRootElement) {
//            return -1;
//        }
        public DomProviderService.ResizeConstraint[] getResizeConstraintsForComponent(Element componentRootElement) {
            return new DomProviderService.ResizeConstraint[0];
        }

        public Element[] getChildComponents(Element componentRootElement) {
            return new Element[0];
        }

        public boolean isRootContainerComponent(Element componentRootElement) {
            return false;
        }

        public boolean isContainerTypeComponent(Element componentRootElement) {
            return false;
        }

        public boolean hasDefaultProperty(Element componentRootElement) {
            return false;
        }

        public boolean focusDefaultProperty(Element componentRootElement, String content) {
            return false;
        }

        public Image getIcon(Element componentRootElement) {
            return null;
        }

//        public org.openide.nodes.Node getNodeRepresentation(Element componentRootElement) {
//            return null;
//        }

        public Element getComponentRootElementFromNode(org.openide.nodes.Node node) {
            return null;
        }

//        public MarkupPosition getDefaultMarkupPositionUnderParent(DesignBean parent) {
//            return null;
//        }

//        public DomProvider.Location computeLocationForPositions(String facet, Point canvasPos, Node documentPosNode, int documentPosOffset, Dimension dimension, boolean isGrid, Element droppeeElement, Element dropeeComponentRootElement, Element defaultParentComponentRootElement) {
//            return null;
//        }

        public Designer[] findDesignersForNode(org.openide.nodes.Node node) {
            return new Designer[0];
        }

        public Element getComponentRootElementForElement(Element element) {
            return null;
        }

        public boolean hasTableResizeSupport(Element tableComponentRootElement) {
            return false;
        }

        public int testResizeColumn(Element tableComponentRootElement, int row, int column, int width) {
            return 0;
        }

        public int testResizeRow(Element tableComponentRootElement, int row, int column, int height) {
            return 0;
        }

        public void resizeColumn(Element tableComponentRootElement, int column, int width) {
        }

        public void resizeRow(Element tableComponentRootElement, int row, int height) {
        }

        public boolean areLinkedToSameBean(Element oneElement, Element otherElement) {
            return false;
        }
    } // End of DummyDomProviderService.
    
    
//    private static class ErrorPanelCallbackImpl implements DomProvider.ErrorPanelCallback {
//        private final WebForm webForm;
//        
//        public ErrorPanelCallbackImpl(WebForm webForm) {
//            this.webForm = webForm;
//        }
//        
//        public void updateTopComponentForErrors() {
//            webForm.getTopComponent().updateErrors();
//        }
//
//        public void setRenderFailureShown(boolean shown) {
//            webForm.setRenderFailureShown(shown);
//        }
//
////        public Exception getRenderFailure() {
////            return webForm.getRenderFailure();
////        }
////
////        public MarkupDesignBean getRenderFailureComponent() {
////            return webForm.getRenderFailureComponent();
////        }
//
//        public void handleRefresh(boolean showErrors) {
//            // Continue from the error panel to the designview
//            webForm.getTopComponent().showErrors(showErrors);
//            // 6274302: See if the user has cleared the error
////            webform.refresh(true);
//            webForm.refreshModel(true);
//        }
//    } // End of ErrorPanelCallbackImpl.

    public Box findBox(int x, int y) {
        return ModelViewMapper.findBox(getPane().getPageBox(), x, y);
    }

    public Box findBoxForSourceElement(Element sourceElement) {
        return findCssBoxForElement(sourceElement);
    }

    public Box findBoxForComponentRootElement(Element componentRootElement) {
        return ModelViewMapper.findBoxForComponentRootElement(getPane().getPageBox(), componentRootElement);
    }
    
    public Box findBoxForElement(Element element) {
        return findCssBoxForElement(element);
    }
    
//    public int snapX(int x, Box positionedBy) {
////        return getGridHandler().snapX(x, positionedBy);
//        return GridHandler.getDefault().snapX(x, positionedBy);
//    }
//
//    public int snapY(int y, Box positionedBy) {
////        return getGridHandler().snapY(y, positionedBy);
//        return GridHandler.getDefault().snapY(y, positionedBy);
//    }

    public Element getPrimarySelectedComponent() {
        // XXX
        getSelection().pickPrimary();
        return getSelection().getPrimary();
    }

    public DomPosition computeNextPosition(DomPosition pos) {
        return ModelViewMapper.computeArrowRight(this, pos);
    }

    public DomPosition computePreviousPosition(DomPosition pos) {
        return ModelViewMapper.computeArrowLeft(this, pos);
    }

    /** Return true iff the position is within the editable portion of the document. */
//    public boolean isWithinEditableRegion(Position pos) {
    public boolean isInsideEditableRegion(DomPosition pos) {
//        WebForm webform = component.getDocument().getWebForm();

        InlineEditor editor = getManager().getInlineEditor();

        if (editor != null) {
//            Position editableRegionStart = editor.getBegin();
//            Position editableRegionEnd = editor.getEnd();
            DomPosition editableRegionStart = editor.getBegin();
            DomPosition editableRegionEnd = editor.getEnd();

//            assert editableRegionStart != Position.NONE;
//            assert editableRegionEnd != Position.NONE;

            return pos.isLaterThan(editableRegionStart) && pos.isEarlierThan(editableRegionEnd);
        }

//        assert !pos.isRendered() : pos;
//        if (MarkupService.isRenderedNode(pos.getNode())) {
        if (isRenderedNode(pos.getNode())) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException("Node is expected to be not rendered, node=" + pos.getNode()));
        }
        

        if (!isGridMode()) {
            // In page flow mode, all regions are editable. Note - this
            // may not be true when I start allowing sub-grids
            return true;
        }

        CssBox box = getManager().getInsertModeBox();

        if (box == null) {
            return false;
        }

        //Position editableRegionStart = Position.create(box.getSourceElement());
        //Position editableRegionEnd = new Position(null, editableRegionStart.getNode(),
        //                                               editableRegionStart.getOffset()+1);
        Element componentRootElement = CssBox.getElementForComponentRootCssBox(box);
//        Position editableRegionStart =
//            new Position(box.getDesignBean().getElement(), 0, Bias.FORWARD);
                // XXX Possible NPE?
//                new Position(CssBox.getMarkupDesignBeanForCssBox(box).getElement(), 0, Bias.FORWARD);
//                new Position(WebForm.getDomProviderService().getSourceElement(componentRootElement), 0, Bias.FORWARD);
//        DomPosition editableRegionStart = DesignerPaneBase.createDomPosition(WebForm.getDomProviderService().getSourceElement(componentRootElement), 0, Bias.FORWARD);
        DomPosition editableRegionStart = createDomPosition(WebForm.getDomProviderService().getSourceElement(componentRootElement), 0, Bias.FORWARD);
        
//        Position editableRegionEnd =
//            new Position(editableRegionStart.getNode(),
//                editableRegionStart.getNode().getChildNodes().getLength(), Bias.BACKWARD);
//        DomPosition editableRegionEnd = DesignerPaneBase.createDomPosition(editableRegionStart.getNode(), editableRegionStart.getNode().getChildNodes().getLength(), Bias.BACKWARD);
        DomPosition editableRegionEnd = createDomPosition(editableRegionStart.getNode(), editableRegionStart.getNode().getChildNodes().getLength(), Bias.BACKWARD);

        return pos.isLaterThan(editableRegionStart) && pos.isEarlierThan(editableRegionEnd);
    }

    
    public void finishInlineEditing(boolean cancel) {
        getManager().finishInlineEditing(cancel);
    }

    public void invokeDeleteNextCharAction(ActionEvent evt) {
        InlineEditor inlineEditor = getManager().getInlineEditor();
        if (inlineEditor != null) {
            inlineEditor.invokeDeleteNextCharAction(evt);
        }
    }

    public Transferable inlineCopyText(boolean isCut) {
        InlineEditor inlineEditor = getManager().getInlineEditor();
        if (inlineEditor != null) {
            return inlineEditor.copyText(isCut);
        }
        return null;
    }

    public Element getPrimarySelection() {
        return getSelection().getPrimary();
    }

    public Element getSelectedContainer() {
        return getSelection().getSelectedContainer();
    }

    public void setSelectedComponents(Element[] componentRootElements, boolean update) {
        getSelection().selectComponents(componentRootElements, update);
    }

    public void clearSelection(boolean update) {
        getSelection().clearSelection(update);
    }

    public void syncSelection(boolean update) {
        getManager().syncSelection(update);
        getSelection().syncSelection(update);
    }

    public void updateSelectedNodes() {
        getSelection().updateNodes();
    }

////    public void updateSelection() {
//    public void updateNodes() {
////        getSelection().updateSelection();
//        getSelection().updateNodes();
//    }

    public Box getPageBox() {
        return getPane().getPageBox();
    }

//    public Point getCurrentPos() {
//        return getManager().getMouseHandler().getCurrentPos();
//    }
//
//    public void clearCurrentPos() {
//        getManager().getMouseHandler().clearCurrentPos();
//    }
//
//    public Element getPositionElement() {
//        return getSelection().getPositionElement();
//    }
//
//    public int getGridWidth() {
////        return getGridHandler().getGridWidth();
//        return GridHandler.getDefault().getGridWidth();
//    }
//
//    public int getGridHeight() {
////        return getGridHandler().getGridHeight();
//        return GridHandler.getDefault().getGridHeight();
//    }
    
    public Point getPastePoint() {
        return getManager().getPastePoint();
    }

//    public void addWeakPreferenceChangeListener(PreferenceChangeListener l) {
//        DesignerSettings.getInstance().addWeakPreferenceChangeListener(l);
//    }

    public ActionMap getPaneActionMap() {
        return getPane().getActionMap();
    }

    public void paneRequestFocus() {
        getPane().requestFocus();
    }

    public JComponent createPaneComponent() {
        return getPane();
    }

    public void updatePaneViewPort() {
        getPane().updateViewport();
    }

    public boolean hasPaneCaret() {
        return getPane().hasCaret();
    }

    public DomRange getPaneCaretRange() {
        return getPane().getCaretRange();
    }
    
    public void setPaneCaret(DomPosition pos) {
        getPane().setCaretDot(pos);
    }

    public void resetPanePageBox() {
        getPane().getPaneUI().resetPageBox();
    }

    public void redoPaneLayout(boolean immediate) {
        getPane().getPageBox().redoLayout(immediate);
    }

//    public void performEscape() {
//        getManager().getMouseHandler().escape();
//    }

    
    // XXX
    public boolean isRenderedNode(Node node) {
        return domProvider.isRenderedNode(node);
    }
    
//    // XXX
//    public void tcEnableCutCopyDelete() {
//        domProvider.tcEnableCutCopyDelete(this);
//    }
//    public void tcDisableCutCopyDelete() {
//        domProvider.tcDisableCutCopyDelete(this);
//    }
    
//    public void tcSetActivatedNodes(org.openide.nodes.Node[] nodes) {
//        domProvider.tcSetActivatedNodes(this, nodes);
//    }
    
//    public org.openide.nodes.Node[] tcGetActivatedNodes() {
//        return domProvider.tcGetActivatedNodes(this);
//    }
    
//    public void tcRequestActive() {
//        domProvider.tcRequestActive(this);
//    }
    
//    public void tcShowPopupMenu(int x, int y) {
//        domProvider.tcShowPopupMenu(this, x, y);
//    }
//    
//    public void tcShowPopupMenu(JPopupMenu popup, int x, int y) {
//        domProvider.tcShowPopupMenu(this, popup, x, y);
//    }
//    
//    public void tcShowPopupMenuForEvent(MouseEvent evt) {
//        domProvider.tcShowPopupMenuForEvent(this, evt);
//    }
    
//    public boolean tcImportComponentData(JComponent comp, Transferable t) {
//        return domProvider.tcImportComponentData(this, comp, t);
//    }
    
//    public Point tcGetPastePosition() {
//        return domProvider.tcGetPastePosition(this);
//    }
    
//    public void tcRepaint() {
//        domProvider.tcRepaint(this);
//    }
    
//    public boolean tcSeenEscape(ActionEvent evt) {
//        return domProvider.tcSeenEscape(this, evt);
//    }
    
//    public void tcDeleteSelection() {
//        domProvider.tcDeleteSelection(this);
//    }

    
    public void addDesignerListener(DesignerListener l) {
        listenerList.add(DesignerListener.class, l);
    }

    public void removeDesignerListener(DesignerListener l) {
        listenerList.remove(DesignerListener.class, l);
    }
    
    private DesignerListener[] getDesignerListeners() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        
        List<DesignerListener> designerListeners = new ArrayList<DesignerListener>();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i] == DesignerListener.class) {
                designerListeners.add((DesignerListener)listeners[i+1]);
            }          
        }
        return designerListeners.toArray(new DesignerListener[designerListeners.size()]);
    }
    
    
    public static class DefaultDesignerEvent implements DesignerEvent {
        private final Designer designer;
        private final Box box;
        
        public DefaultDesignerEvent(Designer designer, Box box) {
            this.designer = designer;
            this.box = box;
        }

        public Designer getDesigner() {
            return designer;
        }

        public Box getBox() {
            return box;
        }
    } // End of DefaultDesignerEvent.
    
    
    public void fireUserActionPerformed(DesignerEvent evt) {
        DesignerListener[] designerListeners = getDesignerListeners();
        for (DesignerListener l : designerListeners) {
            l.userActionPerformed(evt);
        }
    }
    
    public void fireUserPopupActionPerformed(DesignerPopupEvent evt) {
        DesignerListener[] designerListeners = getDesignerListeners();
        for (DesignerListener l : designerListeners) {
            l.userPopupActionPerformed(evt);
        }
    }
    
    public void fireUserElementClicked(DesignerClickEvent evt) {
        DesignerListener[] designerListeners = getDesignerListeners();
        for (DesignerListener l : designerListeners) {
            l.userElementClicked(evt);
        }
    }
    
    public void fireSelectionChanged(DesignerEvent evt) {
        DesignerListener[] designerListeners = getDesignerListeners();
        for (DesignerListener l : designerListeners) {
            l.selectionChanged(evt);
        }
    }
    
    
    private static class CssBoxDataHandler implements UserDataHandler {
        private static final CssBoxDataHandler INSTANCE = new CssBoxDataHandler();
        
        public static CssBoxDataHandler getDefault() {
            return INSTANCE;
        }
        
        public void handle(short operation, String key, Object data, Node src, Node dst) {
            // No op.
        }
    } // End of CssBoxDataHandler.
    
    
    void paintDesignerDecorations(Graphics2D g) {
        domProvider.paintDesignerDecorations(g, this);
    }
    
    public Designer.RenderContext createRenderContext() {
        return new RenderContextImpl(this);
    }
    
    
    private static class RenderContextImpl implements Designer.RenderContext {
        private final WebForm webForm;
        
        public RenderContextImpl(WebForm webForm) {
            this.webForm = webForm;
        }
        
//        public DesignContext getDesignContext() {
//            return webForm.getModel().getLiveUnit();
//        }
//        public DesignBean[] getBeansOfType(Class clazz) {
//            return webForm.getBeansOfType(clazz);
//        }

//        public Project getProject() {
//            return webForm.getProject();
//        }
        
        public Dimension getVieportDimension() {
            return webForm.getPane().getPageBox().getViewport().getExtentSize();
        }

        public Point getViewportPosition() {
            return webForm.getPane().getPageBox().getViewport().getViewPosition();
        }

        public int getNonTabbedTextWidth(char[] s, int offset, int length, FontMetrics metrics) {
            return DesignerUtils.getNonTabbedTextWidth(s, offset, length, metrics);
        }

        public Rectangle getBoundsForComponent(Element componentRootElement) {
            if (componentRootElement != null) {
                return ModelViewMapper.getComponentBounds(webForm.getPane().getPageBox(), componentRootElement);
            } else {
                return null;
            }
        }
    } // End of RenderContextImpl.


    public void setPaintSizeMask(boolean paintSizeMask) {
        this.paintSizeMask = paintSizeMask;
    }
    
    public boolean isPaintSizeMask() {
        return paintSizeMask;
    }

    
    public Decoration getDecoration(Element element) {
        return domProvider.getDecoration(element);
    }
    
    public boolean isShowDecorations() {
        return domProvider.isShowDecorations();
    }
    
    public int getDefaultFontSize() {
        return domProvider.getDefaultFontSize();
    }
    
    public int getPageSizeWidth() {
        return domProvider.getPageSizeWidth();
    }
    
    public int getPageSizeHeight() {
        return domProvider.getPageSizeHeight();
    }
    
    public boolean isGridShow() {
        return domProvider.isGridShow();
    }
    
    public boolean isGridSnap() {
        return domProvider.isGridSnap();
    }
    
    public int getGridWidth() {
        return domProvider.getGridWidth();
    }
    
    public int getGridHeight() {
        return domProvider.getGridHeight();
    }
    
    public int getGridTraceWidth() {
        return domProvider.getGridTraceWidth();
    }
    
    public int getGridTraceHeight() {
        return domProvider.getGridTraceHeight();
    }
    
    public int getGridOffset() {
        return domProvider.getGridOffset();
    }
    
    /** Adjust the given mouse X position to account for insets in parent
     * components etc., such that the resulting position matches the canvas
     * pixel in the view the mouse is over.
     */
    public int adjustX(int x) {
        x += DesignerPane.getAdjustX();

        return x;
    }

    /** Adjust the given mouse X position to account for insets in parent
     * components etc., such that the resulting position matches the canvas
     * pixel in the view the mouse is over.
     */
    public int adjustY(int y) {
        y += DesignerPane.getAdjustY();

        return y;
    }
    
    /** Snap the given X position. If snap to grid is turned off, it simply
     * returns the original position.
     * @param x The horizontal position to be snapped to the grid
     * @param parent A positioning parent to snap to, or null to use
     *   the default viewport (0,0)
     * @todo Handle case where the x coordinate is less than the parent box left.
     *   This can happen when the user drags the component outside the grid area.
     */
//    public int snapX(int x, CssBox parent) {
    public int snapX(int x, Box parent) {
        boolean snap = isGridSnap();
        int gridWidth = getGridWidth();
        int gridOffset = getGridOffset();
        return doSnapX(x, parent == null ? 0 : parent.getAbsoluteX(), snap, gridWidth, gridOffset);
    }
    
    public int snapX(int x) {
        return snapX(x, null);
    }
    
    private static int doSnapX(int x, int absX, boolean snap, int gridWidth, int gridOffset) {
//        int root = 0; // X coordinate of positioning parent

//        if (parent != null) {
//            root = parent.getAbsoluteX();
//            x -= root;
//        }
        // X coordinate of positioning parent
        int root = absX;
        x -= root;

        if (snap) {
            x = (((x + gridOffset + (gridWidth / 2)) / gridWidth) * gridWidth) - gridOffset;
        }

        x += root;

        return x;
    }

    /** Snap the given Y position. If snap to grid is turned off, it simply
     * returns the original position.
     * @param y The vertical position to be snapped to the grid
     * @param parent A positioning parent to snap to, or null to use
     *   the default viewport (0,0)
     * @todo Handle case where the y coordinate is less than the parent box top.
     *   This can happen when the user drags the component above the grid area.
     */
//    public int snapY(int y, CssBox parent) {
    public int snapY(int y, Box parent) {
        boolean snap = isGridSnap();
        int gridHeight = getGridHeight();
        int gridOffset = getGridOffset();
        return doSnapY(y, parent == null ? 0 : parent.getAbsoluteY(), snap, gridHeight, gridOffset);
    }
    
    public int snapY(int y) {
        return snapY(y, null);
    }
    
    private static int doSnapY(int y, int absY, boolean snap, int gridHeight, int gridOffset) {
//        int root = 0; // Y coordinate of positioning parent

//        if (parent != null) {
//            root = parent.getAbsoluteY();
//            y -= root;
//        }
        // Y coordinate of positioning parent
        int root = absY;
        y -= root;

        if (snap) {
            y = (((y + gridOffset + (gridHeight / 2)) / gridHeight) * gridHeight) - gridOffset;
        }

        y += root;

        return y;
    }
}
