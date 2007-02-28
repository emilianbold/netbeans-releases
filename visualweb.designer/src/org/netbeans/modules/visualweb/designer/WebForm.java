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
import org.netbeans.modules.visualweb.api.designer.HtmlDomProvider;
import org.netbeans.modules.visualweb.api.designer.HtmlDomProvider.InlineEditorSupport;
import org.netbeans.modules.visualweb.api.designer.HtmlDomProviderService;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssProvider;
import org.netbeans.modules.visualweb.api.designer.cssengine.CssValue;
import org.netbeans.modules.visualweb.api.designer.cssengine.StyleData;
import org.netbeans.modules.visualweb.api.designer.cssengine.XhtmlCss;
import org.netbeans.modules.visualweb.css2.CssBox;
import org.netbeans.modules.visualweb.css2.PageBox;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DisplayItem;
import com.sun.rave.designtime.markup.MarkupPosition;
import org.netbeans.modules.visualweb.designer.DocumentCache;
import org.netbeans.modules.visualweb.designer.ImageCache;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.Action;

import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import org.netbeans.core.spi.multiview.CloseOperationState;

import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.spi.palette.PaletteController;
import org.openide.ErrorManager;
import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sun.rave.designtime.markup.MarkupDesignBean;
import org.netbeans.modules.visualweb.text.Document;


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
    public static final WebForm EXTERNAL = new WebForm();

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

//    private static final Map<HtmlDomProvider, WebForm> htmlDomProvider2webForm = new WeakHashMap<HtmlDomProvider, WebForm>();

    private final HtmlDomProvider htmlDomProvider;
    
    private final HtmlDomProvider.HtmlDomProviderListener htmlDomProviderListener = new HtmlDomProviderListener(this);
    
//    protected FacesModel model;
    private SelectionManager selection;
    private DesignerTopComp view;
    private Document document;
    private boolean gridMode = false;
//    private ModelViewMapper mapper;
//    private CssLookup css;
    private CellRendererPane rendererPane;
//    private DocumentFragment html;
//    private DomSynchronizer domSyncer;
//    private RaveElement body;
    
//    private VirtualFormSupport virtualForms;
    private boolean virtualFormsEnabled;
    
    private ColorManager colors;
//    private DesignerActions actions;
    private InteractionManager manager;
    private GridHandler gridHandler;
//    private boolean isFragment;
//    private boolean isPortlet;
    private WebForm contextPage;
//    private Exception renderFailure;
    private boolean renderFailureShown;
//    private MarkupDesignBean renderFailureComponent;
    
    /** Maps elements to css boxes. */
    private final Map<Element, CssBox> element2cssBox = new WeakHashMap<Element, CssBox>();
    
    // XXX Suspicious listener, it should be removed.
    private JspDataObjectListener jspDataObjectListener;

    // XXX Moved from Document.
    private ImageCache imageCache;
    private DocumentCache frameCache;

    
    
    private static class JspDataObjectListener implements PropertyChangeListener {
        private final WebForm webForm;
        public JspDataObjectListener(WebForm webForm) {
            this.webForm = webForm;
        }
        public void propertyChange(final PropertyChangeEvent evt) {
            // Immediately wipe out the paint box
            if (evt.getPropertyName().equals(DataObject.PROP_PRIMARY_FILE)) {
                if ((webForm.getPane() != null) && (webForm.getPane().getPaneUI() != null)) {
                    webForm.getPane().getPaneUI().setPageBox(null);
                }

//                    // Reconfigure the data object: throw away the old model
//                    // and find the new model associated with the new file object.
//                    InSyncService.getProvider().doOutsideOfRefactoringSession(new Runnable() {
//                        public void run() {
//                            // Do the stuff on UI thread as some stuff gets updated that requires to be on UI thread
//                            SwingUtilities.invokeLater(new Runnable() {
//                                public void run() {
//                                    replaceModel((FileObject)evt.getOldValue(), (FileObject)evt.getNewValue());
//                                }
//                            });
//                        }
//                    });
            }
        }
    } // End of JspDataObjectListener.

    
    private boolean isClosing = false;

    private WebForm() {
        // XXX Get rid of this constructor.
        this.htmlDomProvider = null;
    }

    /**
     * Create a new webform object. dobj cannot be null.
     */
    private WebForm(HtmlDomProvider htmlDomProvider /*, FileObject fo*/) {
//        assert fo != null;
        if (htmlDomProvider == null /*|| fo == null*/) {
            throw new NullPointerException("The htmlDomProvider parameter can't be null!" // NOI18N
                    /*+ ", fo=" + fo*/); // NOI18N
        }
        
        this.htmlDomProvider = htmlDomProvider;
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

    
    public static WebForm createWebForm(HtmlDomProvider htmlDomProvider) {
        return new WebForm(htmlDomProvider);
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
    
    private static boolean isWebFormDataObject(DataObject dobj) {
//        return (dobj != null) && (FacesModel.getInstance(dobj.getPrimaryFile()) != null);
        return (dobj != null) && getHtmlDomProviderService().isWebFormFileObject(dobj.getPrimaryFile());
    }

//    public static boolean hasWebFormForDataObject(DataObject dobj) {
////        synchronized (LOCK_WEB_FORMS) {
////            return webForms.containsKey(dobj);
////        }
//    
////        return getDesignerFinder().hasDesigner(dobj);
//        
//        HtmlDomProviderFinder htmlDomProviderFinder = getHtmlDomProviderFinder();
//        if (htmlDomProviderFinder == null) {
//            return false;
//        }
//
//        if (!htmlDomProviderFinder.hasHtmlDomProvider(dobj)) {
//            return false;
//        }
//        
//        HtmlDomProvider htmlDomProvider = htmlDomProviderFinder.findHtmlDomProvider(dobj);
//        synchronized (htmlDomProvider2webForm) {
//            // XXX Check whether the model is still valid
//            return htmlDomProvider2webForm.containsKey(htmlDomProvider);
//        }
//    }
//    private static boolean hasWebFormForDataObject(DataObject jsfJspDataObject) {
//        if (jsfJspDataObject == null) {
//            return false;
//        }
//        return findWebFormForFileObject(jsfJspDataObject.getPrimaryFile()) != null;
//    }

    
//    public static WebForm getWebFormForDataObject(/*HtmlDomProvider htmlDomProvider,*/ DataObject dobj) {
//        if (/*htmlDomProvider == null ||*/ dobj == null) {
//            throw new NullPointerException("Parameter dobj can't be null!"); // NOI18N
//        }
//        
////        synchronized (LOCK_WEB_FORMS) {
////            WebForm webform = (WebForm)webForms.get(dobj);
////
////            if (webform == null) {
////                FileObject fo = dobj.getPrimaryFile();
////                webform = new WebForm(htmlDomProvider, fo);
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
//        HtmlDomProvider htmlDomProvider = getHtmlDomProviderFinder().getHtmlDomProvider(dobj);
//        if (htmlDomProvider == null) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, 
//                    new NullPointerException("No instance of HtmlDomProvider for data object, dobj=" + dobj)); // NOI18N
//            return null;
//        }
//        return getWebFormForHtmlDomProvider(htmlDomProvider);
//    }
     
//    public static WebForm getWebFormForHtmlDomProvider(HtmlDomProvider htmlDomProvider) {
//        if (htmlDomProvider == null) {
//            throw new NullPointerException("Can't create WebForm for null HtmlDomProvider!"); // NOI18N
//        }
//        synchronized (htmlDomProvider2webForm) {
//            WebForm webForm = htmlDomProvider2webForm.get(htmlDomProvider);
//            if (webForm == null) {
//                webForm = new WebForm(htmlDomProvider);
//                htmlDomProvider2webForm.put(htmlDomProvider, webForm);
//            }
//            return webForm;
//        }
//    }
    
    public static HtmlDomProviderService getHtmlDomProviderService() {
        HtmlDomProviderService htmlDomProviderService = (HtmlDomProviderService)Lookup.getDefault().lookup(HtmlDomProviderService.class);
        if (htmlDomProviderService == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, 
                    new NullPointerException("No instance of HtmlDomProviderService available!")); // NOI18N
            return new DummyHtmlDomProviderService();
        }
        return htmlDomProviderService;
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
//            htmlDomProvider.requestRefresh();
//        }
//    }


//    static MultiViewElement getDesignerMultiViewElement(DataObject dataObject) {
//        WebForm webForm = getWebFormForDataObject(dataObject);
//        if (webForm == null) {
//            return null;
//        }
//        return webForm.getTopComponent();
//    }
    
    /** Finds <code>WebForm</code> for provided <code>DesignContext</code>.
     * XXX Get rid of this method. */
    public static WebForm findWebFormForDesignContext(DesignContext designContext) {
//        Collection col;
//        synchronized (LOCK_WEB_FORMS) {
//            col = webFormsByFileObject.values();
//        }
//        
//        for (Iterator it = col.iterator(); it.hasNext(); ) {
//            WebForm webform = (WebForm)it.next();
//            // XXX LiveUnit is DesignContext.
//            if ((DesignContext)webform.getModel().getLiveUnit() == designContext) {
//                return webform;
//            }
//        }
//        return null;
        
//        return getWebFormForDesigner(getDesignerFinder().findDesignerForDesignContext(designContext));
        
//        HtmlDomProvider htmlDomProvider = getHtmlDomProviderFinder().findHtmlDomProvider(designContext);
//        if (htmlDomProvider == null) {
//            return null;
//        }
//        synchronized (htmlDomProvider2webForm) {
//            return htmlDomProvider2webForm.get(htmlDomProvider);
//        }
        
        Designer[] designers = getHtmlDomProviderService().findDesignersForDesignContext(designContext);
        if (designers.length > 0 && designers[0] instanceof WebForm) {
            return (WebForm)designers[0];
        } else {
            return null;
        }
    }
    
    public static WebForm[] findAllWebFormsForElement(Element element) {
        Designer[] designers = getHtmlDomProviderService().findDesignersForElement(element);
        List<WebForm> webForms = new ArrayList<WebForm>();
        for (Designer designer : designers) {
            if (designer instanceof WebForm) {
                webForms.add((WebForm)designer);
            }
        }
        return webForms.toArray(new WebForm[webForms.size()]);
    }
    
    public static WebForm findWebFormForElement(Element element) {
        Designer[] designers = getHtmlDomProviderService().findDesignersForElement(element);
        if (designers.length > 0 && designers[0] instanceof WebForm) {
            return (WebForm)designers[0];
        } else {
            return null;
        }
    }
    
    public static WebForm getWebFormForDataObject(DataObject jsfJspDataObject) {
        Designer[] designers = getHtmlDomProviderService().getDesignersForDataObject(jsfJspDataObject);
        if (designers.length > 0 && designers[0] instanceof WebForm) {
            return (WebForm)designers[0];
        } else {
            return null;
        }
    }
    
    public static WebForm findWebFormForFileObject(FileObject jsfJspFileObject) {
//        synchronized (LOCK_WEB_FORMS) {
//            WebForm webform = (WebForm)webFormsByFileObject.get(fo);
//
//            return webform;
//        }
        
//        return getWebFormForDesigner(getDesignerFinder().findDesignerForFileObject(fo));
        
//        DataObject dobj;
//        try {
//            dobj = DataObject.find(fo);
//        } catch (DataObjectNotFoundException ex) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//            return null;
//        }
//        
//        HtmlDomProvider htmlDomProvider = getHtmlDomProviderFinder().findHtmlDomProvider(dobj);
//        synchronized (htmlDomProvider2webForm) {
//            return htmlDomProvider2webForm.get(htmlDomProvider);
//        }
        
        Designer[] designers = getHtmlDomProviderService().findDesignersForFileObject(jsfJspFileObject);
        if (designers.length > 0 && designers[0] instanceof WebForm) {
            return (WebForm)designers[0];
        } else {
            return null;
        }
    }

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
////                    webform.destroyHtmlDomProvider(dobj);
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

    public String toString() {
//        if (getMarkup() != null) {
//            return "WebForm[" + getMarkup().getFileObject().getNameExt() + "]";
//        }

        return super.toString() + "[htmlDomProvider=" + htmlDomProvider + " ,selection=" + selection + ", view=" +
        view + ", gridmode=" + gridMode + "]";
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
        
        synchronized (element2cssBox) {
            element2cssBox.put(element, box);
        }
    }
    
    public CssBox getCssBoxForElement(Element element) {
        synchronized (element2cssBox) {
            return element2cssBox.get(element);
        }
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

    public DataObject getDataObject() {
//        FileObject file = getModel().getMarkupFile();
//
//        try {
//            return DataObject.find(file);
//        } catch (DataObjectNotFoundException e) {
//            return null;
//        }
        return getJspDataObject();
    }

    public DataObject getJspDataObject() {
//        FileObject file = getModel().getMarkupFile();
//
//        try {
//            return DataObject.find(file);
//        } catch (DataObjectNotFoundException dnfe) {
//            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, dnfe);
//
//            return null;
//        }
        return htmlDomProvider.getJspDataObject();
    }

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

    /**
     * Return the top component associated with the design view
     */
    public DesignerTopComp getTopComponent() {
        if (view == null) {
            view = new DesignerTopComp(this);
        }

        return view;
    }

    public DesignerPane getPane() {
        return getTopComponent().getDesignerPane();
    }

//    public FacesModel getModel() {
////        return model;
//        return htmlDomProvider.getFacesModel();
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

    /**
     * Get the document associated with this webform.
     */
    public Document getDocument() {
        if (document == null) {
            document = new Document(this);
        }

        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    /**
     * Get the document associated with this webform.
     */
    public org.w3c.dom.Document getJspDom() {
// <separation of models>
//        MarkupUnit unit = model.getMarkupUnit();
//
//        if (unit == null) { // possible when project has closed
//            return null;
//        }
//
//        return (RaveDocument)unit.getDocument();
// ====
//        return InSyncService.getProvider().getJspDomForMarkupFile(getModel().getMarkupFile());
        return htmlDomProvider.getJspDom();
// </separation of models>
    }

    public org.w3c.dom.Document getHtmlDom() {
//        return InSyncService.getProvider().getHtmlDomForMarkupFile(getModel().getMarkupFile());
        return htmlDomProvider.getHtmlDom();
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
        DocumentFragment df = htmlDomProvider.getHtmlDocumentFragment();

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
        return getHtmlBody(true);
    }

    // XXX Helper method, see DesignerTopComp#updateErrors.
    Element getHtmlBody(boolean updateErrors) {
// <separation of models>
//        if (body == null) {
//            // XXX Initing by side effect.
//            getHtmlDom(); // will set body too, if possible
//        }
//
//        return body;
// ====
//        return InSyncService.getProvider().getHtmlBodyForMarkupFile(getModel().getMarkupFile());
        Element bodyElement =  htmlDomProvider.getHtmlBody();
        
        // XXX #6472138 FIXME Is this correct here?
        if (updateErrors) {
            updateErrorsInComponent();
        }
        
        return bodyElement;
// </separation of models>
    }

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

    /**
     * Clears the HTML DOM associated with the source JSPX DOM
     * returned by {@link getDom}.
     * @param html A DocumentFragment which represents a rendered
     * view of the JSPX DOM.
     */
    public void clearHtml() {
//        this.html = null;
//        this.body = null; // force new search
//        InSyncService.getProvider().clearHtmlForMarkupFile(getModel().getMarkupFile());
        htmlDomProvider.clearHtml();
    }

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

    /**
     * Set whether or not grid mode is in effect
     */
    private void setGridMode(boolean on) {
        gridMode = on;

        DesignerPane pane = getPane();

        if (pane != null) {
            pane.setGridMode(on);
        }

        // Gotta set the cursor to a pointer instead! How can I do
        // this in a PLAF agnostic way?
    }

    /**
     * Return whether or not grid mode is in effect
     * @todo For increased readability, instead of !isGridMode() everywhere,
     *  have an isFlowMode() method with reverse logic that I use instead.
     */
    public boolean isGridMode() {
        return gridMode;
    }


    // XXX Moved from document.
    /**
     *  Return true if this document is in "grid mode" (objects
     *  should be positioned by absolute coordinates instead of in
     *  "flow" order.
     *
     *  @return true iff the document should be in grid mode
     */
    public boolean isGridModeDocument() {
        Element body = getHtmlBody();

        if (body == null) {
            return false;
        }

//        Value val = CssLookup.getValue(b, XhtmlCss.RAVELAYOUT_INDEX);
        CssValue cssValue = CssProvider.getEngineService().getComputedValueForElement(body, XhtmlCss.RAVELAYOUT_INDEX);

//        return val == CssValueConstants.GRID_VALUE;
        return CssProvider.getValueService().isGridValue(cssValue);
    }
    
    public boolean isBraveheartPage() {
        return htmlDomProvider.isBraveheartPage();
    }
    
    public boolean isWoodstockPage() {
        return htmlDomProvider.isWoodstockPage();
    }
    
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

    /**
     * Return true iff this webform represents a fragment. Note - portlets are
     * frequently fragments too so don't conclude from this method returning true
     * that you are not dealing with a portlet!
     */
    public boolean isFragment() {
//        return isFragment;
        return htmlDomProvider.isFragment();
    }

    /**
     * Retru true iff this webform represents a portlet
     */
    public boolean isPortlet() {
//        return isPortlet;
        return htmlDomProvider.isPortlet();
    }

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

    public void setRenderPane(CellRendererPane rendererPane) {
        this.rendererPane = rendererPane;
    }

//    /** Return the virtual forms support associated with this webform */
//    public VirtualFormSupport getVirtualFormSupport() {
//        if (virtualForms == null) {
//            virtualForms = new VirtualFormSupport(this);
//        }
//
//        return virtualForms;
//    }
    
    public void setVirtualFormsEnabled(boolean virtualFormsEnabled) {
        this.virtualFormsEnabled = virtualFormsEnabled;
    }
    
    public boolean isVirtualFormsEnabled() {
        return virtualFormsEnabled;
    }

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

    /** Return true iff the webform has rendering problems associated with it */
    public boolean hasRenderingErrors() {
//        return renderFailureComponent != null;
        return getRenderFailureComponent() != null;
    }

    // XXX Very suspicious, revise it.
    /** Sets render failed values about a failure in rendering it to HTML.
     * @param component The component which failed to render
     * @param exception The exception thrown by the component
     */
    private void setRenderFailedValues(MarkupDesignBean renderFailureComponent, Exception renderFailureException) {
//        renderFailure = exception;
//        renderFailureComponent = component;
        htmlDomProvider.setRenderFailedValues(renderFailureComponent, renderFailureException);
    }

    /** Return the exception associated with the current render failure for this page */
    public Exception getRenderFailure() {
//        return renderFailure;
        return htmlDomProvider.getRenderFailureException();
    }

    /** Return the component associated with the current render failure for this page */
    public MarkupDesignBean getRenderFailureComponent() {
//        return renderFailureComponent;
        return htmlDomProvider.getRenderFailureComponent();
    }

    /** Return true iff the current render failure (returned by
     * {@link #getRenderFailure} has been shown to the user yet
     */
    public boolean isRenderFailureShown() {
        return renderFailureShown;
    }

    /** Record whether the current render failure (returned by
     * {@link #getRenderFailure} has been shown to the user yet
     */
    public void setRenderFailureShown(boolean renderFailureShown) {
        this.renderFailureShown = renderFailureShown;
    }

    /** XXX Moved from FacesSupport. Updates erros in the corresponding component.
     * TODO Usage of this after renderHtml call is very suspicious, revise. */
    public void updateErrorsInComponent() {
//        FileObject markupFile = getModel().getMarkupFile();
//// <missing designtime api>
////        Exception renderFailure = facesunit.getRenderFailure();
////        MarkupDesignBean renderFailureComponent =
////            (MarkupDesignBean)facesunit.getRenderFailureComponent();
//// ====
//        Exception renderFailure = InSyncService.getProvider().getRenderFailure(markupFile);
        Exception renderFailure = htmlDomProvider.getRenderFailure();
//        MarkupDesignBean renderFailureComponent = (MarkupDesignBean)InSyncService.getProvider().getRenderFailureComponent(markupFile);
        MarkupDesignBean renderFailureComponent = htmlDomProvider.getRenderFailureMarkupDesignBean();
        
// </missing designtime api>

        setRenderFailedValues(renderFailureComponent, renderFailure);

        if (renderFailure == null) {
            // Since we had a successful render now, we should remember this such
            // that if a rendering error happens again, we will show the errorpanel
            setRenderFailureShown(false);
        }

        // XXX #6472138 Put into AWT.
        updateComponentForErrors();
    }
    
    private void updateComponentForErrors() {
        if (EventQueue.isDispatchThread()) {
            doUpdateComponentForErrors();
        } else {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    doUpdateComponentForErrors();
                }
            });
        }
    }
    
    private void doUpdateComponentForErrors() {
        if (getTopComponent().isShowing()) {
            // In case some kind of rendering error happened
            // Ugh... I need to track this differently!
            getTopComponent().updateErrors();
        }
    }
    

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
    
    public GridHandler getGridHandler() {
        if (gridHandler == null) {
            gridHandler = new GridHandler(this);
        }
        return gridHandler;
    }

    /** Get the context page for this fragment. This method should only return non-null
     * for page fragments. The context page is a page which provides a "style context" for
     * the fragment. Typically, the page is one of the pages which includes the page fragment,
     * but that's not strictly necessary. The key thing is that the page fragment will pick
     * up stylesheets etc. defined in the head of the context page.
     * @return A context page for the fragment
     */
    public WebForm getContextPage() {
//        if (isFragment && (contextPage == null)) {
        if (isFragment() && (contextPage == null)) {
            // Find a page
            Iterator it =
//                DesignerService.getDefault().getWebPages(getProject(), true, false).iterator();
//                    InSyncService.getProvider().getWebPages(getProject(), true, false).iterator();
                    htmlDomProvider.getWebPageFileObjectsInThisProject().iterator();

            while (it.hasNext()) {
                FileObject fo = (FileObject)it.next();

                try {
                    DataObject dobj = DataObject.find(fo);

                    // XXX Very suspicious, how come that context page is any random page
                    // whitin project?? What actually the context page is good for?
                    // It seems it is a wrong architecture.
                    if (isWebFormDataObject(dobj)) {
                        contextPage = getWebFormForDataObject(dobj);

                        break;
                    }
                } catch (DataObjectNotFoundException dnfe) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, dnfe);
                }
            }
        }

        return contextPage;
    }

    /** Set the associated context page for this page fragment. (Only allowed on
     * page fragments.)
     *  @see getContextPage()
     */
    public void setContextPage(WebForm contextPage) {
//        assert isFragment;
        assert isFragment();

        // XXX Context page notion from fragment should be removed.
        if (this.contextPage != contextPage) {
            // Force refresh such that the style links are recomputed
            clearHtml();
        }

        this.contextPage = contextPage;
    }

//    // Tor: The name is up to you.
//    public void destroy() {
//        detachTopComponent();
//
////        if (domSyncer != null) {
////            domSyncer.destroy();
////            domSyncer = null;
////        }
//        htmlDomProvider.destroyDomSynchronizer();
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
    
    // XXX Moved from Document.
    /** Return a cache of webform boxes associated with this document
     * @todo Rename; it's no longer a box cache but rather a document
     *   cache! */
    public DocumentCache getFrameBoxCache() {
        if (frameCache == null) {
            frameCache = new DocumentCache();
        }

        return frameCache;
    }
    
    // XXX Moved from Document.
    /** Return true iff the document has cached frame boxes */
    public boolean hasCachedFrameBoxes() {
        return (frameCache != null) && (frameCache.size() > 0);
    }

    // XXX Moved from Document.
    /** Clear out caches for a "refresh" operation */
    private void flushCaches() {
        if (frameCache != null) {
            frameCache.flush();
        }

        if (imageCache != null) {
            imageCache.flush();
        }
    }


//    //////
//    // XXX See Designer interface.
    public void modelChanged() {
        getPane().getPaneUI().resetPageBox();
    }

    public void nodeChanged(Node rendered, Node parent, boolean wasMove) {
        PageBox pageBox = getPane().getPaneUI().getPageBox();
        pageBox.changed(rendered, parent, false);
    }

    public void nodeRemoved(Node previouslyRendered, Node parent) {
        PageBox pageBox = getPane().getPaneUI().getPageBox();
        pageBox.removed(previouslyRendered, parent);
    }
    
    public void nodeInserted(Node rendered, Node parent) {
        getPane().getPaneUI().getPageBox().inserted(rendered, parent);
    }
    
    public void updateGridMode() {
//        setGridMode(getDocument().isGridMode()); // XXX
        setGridMode(isGridModeDocument());
    }

    public void documentReplaced() {
        // Ensure that the caret is in the new DOM
        DesignerPane pane = getPane();

        if (pane != null) {
            if (pane.getCaret() != null) {
                pane.getCaret().detachDom();
                //pane.setCaret(null);
            }
            //                pane.showCaretAtBeginning();
        }
    }
    // XXX
    //////

//    /** XXX Temporary only until all modification stuff is moved from designer to designer/jsf. */
//    public void setUpdatesSuspended(MarkupDesignBean markupDesignBean, boolean suspend) {
//        htmlDomProvider.setUpdatesSuspended(markupDesignBean, suspend);
//    }
    
    public void setUpdatesSuspended(Element componentRootElement, boolean suspend) {
        htmlDomProvider.setUpdatesSuspended(componentRootElement, suspend);
    }

    public boolean isRefreshPending() {
        return htmlDomProvider.isRefreshPending();
    }

//    public void attachContext(DesignContext context) {
//        htmlDomProvider.attachContext(context);
    public void attachContext() {
        htmlDomProvider.attachContext();
    }
    
//    public void detachContext() {
//        htmlDomProvider.detachContext();
//    }

//    public DocumentFragment createSourceFragment(MarkupDesignBean bean) {
//        return htmlDomProvider.createSourceFragment(bean);
//    }

//    public void requestChange(MarkupDesignBean bean) {
//        htmlDomProvider.requestChange(bean);
//    }

//    public void beanChanged(MarkupDesignBean bean) {
//        htmlDomProvider.beanChanged(bean);
//    }

//    public void requestTextUpdate(MarkupDesignBean bean) {
//        htmlDomProvider.requestTextUpdate(bean);
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
//        htmlDomProvider.refreshProject();
//    }
    
    public void refreshModel(final boolean deep) {
        // #6483029 Refresh contained external forms (e.g. fragments) first.
        refreshExternalForms(deep);
        
        htmlDomProvider.refreshModel(deep);
    }
    
    private void refreshExternalForms(boolean deep) {
        DesignerPane designerPane = getPane();
        if (designerPane == null) {
            // XXX #6495248 This is not opened yet, so not initialized yet.
            // TODO The external forms may not be stored in ui components (boxes),
            // they need to be findable from the model directly.
            return;
        }
        
        PageBox pageBox = designerPane.getPageBox();
        if (pageBox == null) {
            return;
        }
        WebForm[] externalForms = pageBox.findExternalForms();
        for (WebForm externalForm : externalForms) {
            if (this == externalForm) {
                // XXX To prevent neverending loop if there is such case.
                continue;
            }
            externalForm.refreshModel(deep);
        }
    }
    
    private void modelRefreshed() {
        getManager().finishInlineEditing(false);

        flushCaches();

        DesignerPane pane = getPane();

        if (pane != null) {
            //            PageBox pageBox = pane.getPageBox();
            //
            //            if (pageBox != null) {
            //                pageBox.redoLayout(pane.isShowing());
            //            }
            //
            pane.getPaneUI().resetPageBox();
        }

        getSelection().updateSelection(); // trigger refresh in CSS viewer for example

        if (pane != null) {
            pane.repaint();
        }
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
////            WebForm webform = WebForm.getWebFormForDataObject(WebForm.findHtmlDomProvider(dobj), dobj);
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

//    public void contextChanged(DesignContext designContext) {
    private void designContextGenerationChanged() {
        if (view != null) {
            view.designContextGenerationChanged();
        }
    }

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
    DataFlavor getImportFlavor(DataFlavor[] flavors) {
        return htmlDomProvider.getImportFlavor(flavors);
    }

    boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
        return htmlDomProvider.canImport(comp, transferFlavors);
    }

    DesignBean[] pasteBeans(Transferable t, DesignBean parent, MarkupPosition pos, Point location, HtmlDomProvider.CoordinateTranslator coordinateTranslator) {
        return htmlDomProvider.pasteBeans(t, parent, pos, location, coordinateTranslator);
    }

    void importData(JComponent comp, Transferable t, Object transferData, Dimension dimension, HtmlDomProvider.Location location, HtmlDomProvider.CoordinateTranslator coordinateTranslator, int dropAction) {
        htmlDomProvider.importData(comp, t, transferData, dimension, location, coordinateTranslator, dropAction);
    }

//    public DesignBean findHtmlContainer(DesignBean parent) {
//        return htmlDomProvider.findHtmlContainer(parent);
//    }

    String[] getClassNames(DisplayItem[] displayItems) {
        return htmlDomProvider.getClassNames(displayItems);
    }

    boolean importBean(DisplayItem[] items, DesignBean origParent, int nodePos, String facet, List createdBeans, HtmlDomProvider.Location location, HtmlDomProvider.CoordinateTranslator coordinateTranslator)
    throws IOException {
        return htmlDomProvider.importBean(items, origParent, nodePos, facet, createdBeans, location, coordinateTranslator);
    }

    MarkupPosition getDefaultMarkupPositionUnderParent(DesignBean parent) {
        return htmlDomProvider.getDefaultPositionUnderParent(parent);
    }

    int computeActions(DesignBean droppee, Transferable transferable, boolean searchUp, int nodePos) {
        return htmlDomProvider.computeActions(droppee, transferable, searchUp, nodePos);
    }

//    DesignBean findParent(String className, DesignBean droppee, Node parentNode, boolean searchUp) {
//        return htmlDomProvider.findParent(className, droppee, parentNode, searchUp);
//    }

//    int processLinks(Element origElement, Class[] classes, List beans, boolean selectFirst, boolean handleLinks, boolean showLinkTarget) {
    int processLinks(Element origElement, Class[] classes, Element componentRootElement, boolean selectFirst, boolean handleLinks, boolean showLinkTarget) {
        return htmlDomProvider.processLinks(origElement, classes, componentRootElement, selectFirst, handleLinks, showLinkTarget);
    }
    // <<< DnD

    // >>> DnD callbacks
    private void showDropMatch(Element componentRootElement, Element regionElement, int dropType) {
        getPane().getDndHandler().showDropMatch(componentRootElement, regionElement, dropType);
    }
    
    private void clearDropMatch() {
        getPane().getDndHandler().clearDropMatch();
    }
    
    public void select(final DesignBean designBean) {
//        getSelection().selectBean(designBean);
        if (designBean instanceof MarkupDesignBean) {
            // XXX Bad arch. Events are fired too early, no rendered tree was created yet.
            // Scheduling later to by pass the issue (of retrieving the component root element).
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    getSelection().selectBean(
                            WebForm.getHtmlDomProviderService().getComponentRootElementForMarkupDesignBean((MarkupDesignBean)designBean));
                }
            });
        }
        // #6338212 Do not request unnecessary activation.
//        getTopComponent().requestActive();
    }
    
//    public void refreshForm(boolean deep) {
//        refresh(deep);
//    }
    
    public void inlineEdit(DesignBean[] designBeans) {
        List<Element> componentRootElements = new ArrayList<Element>();
        for (DesignBean designBean : designBeans) {
            if (designBean instanceof MarkupDesignBean) {
                Element componentRootElement = WebForm.getHtmlDomProviderService().getComponentRootElementForMarkupDesignBean((MarkupDesignBean)designBean);
                if (componentRootElement != null) {
                    componentRootElements.add(componentRootElement);
                }
            }
        }
        getManager().inlineEdit(componentRootElements.toArray(new Element[componentRootElements.size()]));
        // XXX #6484250 To activate the window after drop.
        getTopComponent().requestActive();
    }
    // <<< DnD callbacks

//    private void destroyHtmlDomProvider(DataObject dobj) {
//        htmlDomProvider.removeForDataObject(dobj);
//    }

//    public void destroyDesigner() {
//        destroy();
//    }

    void registerListeners() {
//        htmlDomProviderListener = new HtmlDomProviderListener(this);
//        htmlDomProvider.addHtmlDomProviderListener((HtmlDomProvider.HtmlDomProviderListener)WeakListeners.create(
//                HtmlDomProvider.HtmlDomProviderListener.class, htmlDomProviderListener, htmlDomProvider));
        
        // XXX FIXME There are more calls then needed. This is a hack to avoid multiple registering.
        htmlDomProvider.removeHtmlDomProviderListener(htmlDomProviderListener);
        htmlDomProvider.addHtmlDomProviderListener(htmlDomProviderListener);
                
        DataObject jspDataObject = getJspDataObject();
        if (jspDataObject != null) {
            jspDataObjectListener = new JspDataObjectListener(this);
            jspDataObject.addPropertyChangeListener(WeakListeners.propertyChange(jspDataObjectListener, jspDataObject));
        }
    }

    void unregisterListeners() {
        // XXX Or don't use weak listener, and remove it explicitely.
//        htmlDomProviderListener = null;
        htmlDomProvider.removeHtmlDomProviderListener(htmlDomProviderListener);
        
        jspDataObjectListener = null;
    }

    public URL getBaseUrl() {
        return htmlDomProvider.getBaseUrl();
    }

    public URL resolveUrl(String urlString) {
        return htmlDomProvider.resolveUrl(urlString);
    }

    public DocumentFragment renderHtmlForMarkupDesignBean(MarkupDesignBean markupDesignBean) {
        return htmlDomProvider.renderHtmlForMarkupDesignBean(markupDesignBean);
    }
    
    public PaletteController getPaletteController() {
        return htmlDomProvider.getPaletteController();
    }

    // XXX
//    boolean editEventHandlerForDesignBean(DesignBean designBean) {
//        return htmlDomProvider.editEventHandlerForDesignBean(designBean);
//    }
    boolean editEventHandlerForComponent(Element componentRootElement) {
        return htmlDomProvider.editEventHandlerForComponent(componentRootElement);
    }
    
    // >>> Designer impl
    public JComponent getDesignerComponent() {
        return getTopComponent();
    }

    // XXX Temp after moved TopComponent impl out >>>
    public JComponent getVisualRepresentation() {
        return getTopComponent().getVisualRepresentation();
    }

    public JComponent getToolbarRepresentation() {
        return getTopComponent().getToolbarRepresentation();
    }

    public Action[] getActions() {
        return getTopComponent().getActions();
    }

    public Lookup getLookup() {
        return getTopComponent().getLookup();
    }

    public void componentOpened() {
        getTopComponent().componentOpened();
    }

    public void componentClosed() {
        getTopComponent().componentClosed();
    }

    public void componentShowing() {
        getTopComponent().componentShowing();
    }

    public void componentHidden() {
        getTopComponent().componentHidden();
    }

    public void componentActivated() {
        getTopComponent().componentActivated();
    }

    public void componentDeactivated() {
        getTopComponent().componentDeactivated();
    }

    public UndoRedo getUndoRedo() {
        return getTopComponent().getUndoRedo();
    }

    public void setMultiViewCallback(MultiViewElementCallback multiViewElementCallback) {
        getTopComponent().setMultiViewCallback(multiViewElementCallback);
    }

    public CloseOperationState canCloseElement() {
        return getTopComponent().canCloseElement();
    }

    // XXX Temp after moved TopComponent impl out <<<
    // <<< Designer impl.

//    boolean canDropDesignBeansAtNode(DesignBean[] designBeans, Node node) {
//        return htmlDomProvider.canDropDesignBeansAtNode(designBeans, node);
//    }
    boolean canDropComponentsAtNode(Element[] componentRootElements, Node node) {
        return htmlDomProvider.canDropComponentsAtNode(componentRootElements, node);
    }

    boolean handleMouseClickForElement(Element element, int clickCount) {
        return htmlDomProvider.handleMouseClickForElement(element, clickCount);
    }

    // XXX, Also better name needed.
//    boolean isNormalAndHasFacesBean(MarkupDesignBean markupDesignBean) {
//        return htmlDomProvider.isNormalAndHasFacesBean(markupDesignBean);
//    }
    boolean isNormalAndHasFacesComponent(Element componentRootElement) {
        return htmlDomProvider.isNormalAndHasFacesComponent(componentRootElement);
    }

    boolean canHighlightMarkupDesignBean(MarkupDesignBean markupDesignBean) {
        return htmlDomProvider.canHighlightMarkupDesignBean(markupDesignBean);
    }

    public DesignBean createBean(String className, Node parent, Node before) {
        return htmlDomProvider.createBean(className, parent, before);
    }

    public boolean isFormBean(DesignBean designBean) {
        return htmlDomProvider.isFormBean(designBean);
    }

    public Element getDefaultParentMarkupBeanElement() {
        return htmlDomProvider.getDefaultParentMarkupBeanElement();
    }

//    public boolean moveBean(DesignBean bean, Node parentNode, Node before) {
//        return htmlDomProvider.moveBean(bean, parentNode, before);
//    }
    public boolean moveComponent(Element componentRootElement, Node parentNode, Node before) {
        return htmlDomProvider.moveComponent(componentRootElement, parentNode, before);
    }

//    boolean setPrerenderedBean(MarkupDesignBean markupDesignBean, DocumentFragment documentFragment) {
//        return htmlDomProvider.setPrerenderedBean(markupDesignBean, documentFragment);
//    }

    MarkupDesignBean getMarkupDesignBeanEquivalentTo(MarkupDesignBean oldBean) {
        return htmlDomProvider.getMarkupDesignBeanEquivalentTo(oldBean);
    }

    org.openide.nodes.Node getRootBeanNode() {
        return htmlDomProvider.getRootBeanNode();
    }

    public void deleteBean(DesignBean designBean) {
        htmlDomProvider.deleteBean(designBean);
    }

//    boolean canCreateBean(String className, DesignBean parent, Position pos) {
//        return htmlDomProvider.canCreateBean(className, parent, pos);
//    }

    DesignBean getDefaultParentBean() {
        return htmlDomProvider.getDefaultParentBean();
    }

    JComponent getErrorPanel() {
        HtmlDomProvider.ErrorPanel errorPanel = htmlDomProvider.getErrorPanel(new ErrorPanelCallbackImpl(this));
        if (errorPanel instanceof JComponent) {
            return (JComponent)errorPanel;
        } else {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                    new IllegalStateException("The provided error panel is not of JComponent type, errorPanel=" + errorPanel)); // NOI18N
            return null;
        }
    }

    public void syncModel() {
        htmlDomProvider.syncModel();
    }

    boolean isSourceDirty() {
        return htmlDomProvider.isSourceDirty();
    }

    Transferable copyBeans(DesignBean[] beans) {
        return htmlDomProvider.copyBeans(beans);
    }

    public HtmlDomProvider.WriteLock writeLock(String message) {
        return htmlDomProvider.writeLock(message);
    }
    
    public void writeUnlock(HtmlDomProvider.WriteLock writeLock) {
        htmlDomProvider.writeUnlock(writeLock);
    }

    boolean isModelValid() {
        return htmlDomProvider.isModelValid();
    }

    void readLock() {
        htmlDomProvider.readLock();
    }

    void readUnlock() {
        htmlDomProvider.readUnlock();
    }

    void setModelActivated(boolean activated) {
        htmlDomProvider.setModelActivated(activated);
    }

    UndoRedo getUndoManager() {
        return htmlDomProvider.getUndoManager();
    }

    public boolean isModelBusted() {
        return htmlDomProvider.isModelBusted();
    }

//    DesignBean[] getBeansOfType(Class clazz) {
//        return htmlDomProvider.getBeansOfType(clazz);
//    }

//    Project getProject() {
//        return htmlDomProvider.getProject();
//    }

    boolean isWriteLocked() {
        return htmlDomProvider.isWriteLocked();
    }

//    Class getBeanClass(String className) throws ClassNotFoundException {
//        return htmlDomProvider.getBeanClass(className);
//    }

    public boolean isPage() {
        return htmlDomProvider.isPage();
    }

    public boolean isAlive() {
        return htmlDomProvider.isAlive();
    }

//    String getImageComponentClassName() {
//        return htmlDomProvider.getImageComponentClassName();
//    }

    void paintVirtualForms(Graphics2D g, HtmlDomProvider.RenderContext renderContext) {
        htmlDomProvider.paintVirtualForms(g, renderContext);
    }

    public boolean isFormComponent(Element componentRootElement) {
        return htmlDomProvider.isFormComponent(componentRootElement);
    }

    int getDropType(DesignBean origDroppee, Element droppeeElement, Transferable t, boolean linkOnly) {
        return htmlDomProvider.getDropType(origDroppee, droppeeElement, t, linkOnly);
    }

//    int getDropTypeForClassNames(DesignBean origDroppee, Element droppeeElement, String[] classNames, DesignBean[] beans, boolean linkOnly) {
//        return htmlDomProvider.getDropTypeForClassNames(origDroppee, droppeeElement, classNames, beans, linkOnly);
//    }
    int getDropTypeForComponent(DesignBean origDroppee, Element droppeeElement, Element componentRootElement, boolean linkOnly) {
        return htmlDomProvider.getDropTypeForComponent(origDroppee, droppeeElement, componentRootElement, linkOnly);
    }

    Element getComponentRootElementEquivalentTo(Element oldComponentRootElement) {
        return htmlDomProvider.getComponentRootElementEquivalentTo(oldComponentRootElement);
    }

    boolean canHighlightComponentRootElement(Element componentRootElement) {
        return htmlDomProvider.canHighlightComponentRootElmenet(componentRootElement);
    }

    InlineEditorSupport createInlineEditorSupport(Element componentRootElement, String propertyName) {
        return htmlDomProvider.createInlineEditorSupport(componentRootElement, propertyName);
    }
    
    
    private static class HtmlDomProviderListener implements HtmlDomProvider.HtmlDomProviderListener {
        private final WebForm webForm;
        
        public HtmlDomProviderListener(WebForm webForm) {
            this.webForm = webForm;
        }
        
        public void modelChanged() {
            webForm.modelChanged();
        }
        
        public void modelRefreshed() {
            webForm.modelRefreshed();
        }

        public void nodeChanged(Node rendered, Node parent, boolean wasMove) {
            webForm.nodeChanged(rendered, parent, wasMove);
        }

        public void nodeRemoved(Node previouslyRendered, Node parent) {
            webForm.nodeRemoved(previouslyRendered, parent);
        }

        public void nodeInserted(Node rendered, Node parent) {
            webForm.nodeInserted(rendered, parent);
        }

        public void updateErrorsInComponent() {
            webForm.updateErrorsInComponent();
        }

        public void updateGridMode() {
            webForm.updateGridMode();
        }

        public void documentReplaced() {
            webForm.documentReplaced();
        }

        public void showDropMatch(Element componentRootElement, Element regionElement, int dropType) {
            webForm.showDropMatch(componentRootElement, regionElement, dropType);
        }
        
        public void clearDropMatch() {
            webForm.clearDropMatch();
        }

        public void select(DesignBean designBean) {
            webForm.select(designBean);
        }

//        public void refreshForm(boolean deep) {
//            webForm.refreshForm(deep);
//        }

        public void inlineEdit(DesignBean[] designBeans) {
            webForm.inlineEdit(designBeans);
        }

//        public void designContextActivated(DesignContext designContext) {
//            webForm.contextActivated(designContext);
//        }

//        public void designContextDeactivated(DesignContext designContext) {
//            webForm.contextDeactivated(designContext);
//        }

        public void designContextGenerationChanged() {
            webForm.designContextGenerationChanged();
        }

//        public void designBeanCreated(DesignBean designBean) {
//            webForm.beanCreated(designBean);
//        }

//        public void designBeanDeleted(DesignBean designBean) {
//            webForm.beanDeleted(designBean);
//        }

//        public void designBeanMoved(DesignBean designBean, DesignBean designBean0, Position position) {
//            webForm.beanMoved(designBean, designBean0, position);
//        }

//        public void designBeanContextActivated(DesignBean designBean) {
//            webForm.beanContextActivated(designBean);
//        }

//        public void designBeanContextDeactivated(DesignBean designBean) {
//            webForm.beanContextDeactivated(designBean);
//        }

//        public void designBeanNameChanged(DesignBean designBean, String string) {
//            webForm.instanceNameChanged(designBean, string);
//        }

//        public void designBeanChanged(DesignBean designBean) {
//            webForm.beanChanged(designBean);
//        }

//        public void designPropertyChanged(DesignProperty designProperty, Object object) {
//            webForm.propertyChanged(designProperty, object);
//        }

//        public void designEventChanged(DesignEvent designEvent) {
//            webForm.eventChanged(designEvent);
//        }
    } // End of HtmlDomProviderListener.
    

    /** Dummy impl of <code>HtmlDomProviderService</code>. */
    private static class DummyHtmlDomProviderService implements HtmlDomProviderService {
//        public HtmlDomProvider getHtmlDomProvider(DataObject dataObject) {
//            return null;
//        }
//
//        public HtmlDomProvider findHtmlDomProvider(DataObject dobj) {
//            return null;
//        }
//
//        public HtmlDomProvider findHtmlDomProvider(DesignContext designContext) {
//            return null;
//        }
//
//        public boolean hasHtmlDomProvider(DataObject dataObject) {
//            return false;
//        }
//
//        public HtmlDomProviderService getHtmlDomProviderService() {
//            return this;
//        }

        // HtmlDomProviderService >>
        public MarkupDesignBean getMarkupDesignBeanForElement(Element element) {
            return null;
        }

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
        
        public ClassLoader getContextClassLoaderForDesignContext(DesignContext designContext) {
            // XXX
            return Thread.currentThread().getContextClassLoader();
        }
        
        public String getHtmlStream(Node node) {
            return node == null ? "" : node.toString(); // NOI18N
        }
        // HtmlDomProviderService <<

        public String getDomDocumentReplacedEventConstant() {
            // XXX
            return "DOMDocumentReplaced"; // NOI18N
        }

        public Designer[] getDesignersForDataObject(DataObject jsfJspDataObject) {
            return new Designer[0];
        }

        public Designer[] findDesignersForFileObject(FileObject jsfJspFileObject) {
            return new Designer[0];
        }

        public Designer[] findDesignersForDesignContext(DesignContext designContext) {
            return new Designer[0];
        }

        public Designer[] findDesignersForElement(Element element) {
            return new Designer[0];
        }

        public MarkupDesignBean getMarkupDesignBeanForComponentRootElement(Element element, Element parentBoxElement) {
            return null;
        }
        
//        public boolean isValueBindingExpression(String value, boolean containsOK) {
//            return false;
//        }

        public String computeFileName(Object location) {
            return location == null ? null : location.toString();
        }

        public int computeLineNumber(Object location, int lineno) {
            return lineno;
        }

        public URL getDocumentUrl(org.w3c.dom.Document document) {
            return null;
        }

        public void displayErrorForLocation(String message, Object location, int lineno, int column) {
        }

        public Element getHtmlBodyForDocument(org.w3c.dom.Document document) {
            return null;
        }

        public DocumentFragment getHtmlDomFragmentForDocument(org.w3c.dom.Document document) {
            return null;
        }

        public boolean editEventHandlerForDesignBean(DesignBean component) {
            return false;
        }

//        public boolean isSpecialBean(DesignBean designBean) {
//            return false;
//        }
        
        public Element getElement(DesignBean designBean) {
            return null;
        }

        public Element getMarkupBeanElement(DesignBean designBean) {
            return null;
        }

//        public boolean isFacesBean(MarkupDesignBean bean) {
//            return false;
//        }

//        public boolean setDesignProperty(MarkupDesignBean bean, String attribute, int value) {
//            return false;
//        }
        public boolean setStyleAttribute(Element componentRootElement, String attribute, int value) {
            return false;
        }

        public Element findHtmlElementDescendant(DocumentFragment df) {
            return null;
        }

        public void updateLocalStyleValuesForElement(Element e, StyleData[] setStyleData, StyleData[] removeStyleData) {
            // XXX.
        }

//        public boolean isTrayBean(DesignBean designBean) {
//            return false;
//        }

//        public boolean isCssPositionable(DesignBean designBean) {
//            return false;
//        }

        public long getContextGenearation(com.sun.rave.designtime.DesignContext context) {
            return 0L;
        }

        public boolean isWebFormFileObject(FileObject fileObject) {
            return false;
        }

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

        public MarkupDesignBean adjustRenderBeanHack(MarkupDesignBean renderBean) {
            return renderBean;
        }

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

        public Element getRenderedElement(DesignBean designBean) {
            return null;
        }

        public String getRegionDisplayName(Element regionElement) {
            return regionElement == null ? null : regionElement.getLocalName();
        }

        public boolean isSameRegionOfElement(Element regionElement, Element element) {
            return regionElement == element;
        }

        public Element getComponentRootElementForMarkupDesignBean(MarkupDesignBean markupDesignBean) {
            return null;
        }

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

        public String[] getEditablePropertyNames(Element componentRootElement) {
            return new String[0];
        }

        public String[] getEditableProperties(Element componentRootElement) {
            return new String[0];
        }

        public int getResizeConstraintsForComponent(Element componentRootElement) {
            return -1;
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
    } // End of DummyHtmlDomProviderService.
    
    
    private static class ErrorPanelCallbackImpl implements HtmlDomProvider.ErrorPanelCallback {
        private final WebForm webForm;
        
        public ErrorPanelCallbackImpl(WebForm webForm) {
            this.webForm = webForm;
        }
        
        public void updateTopComponentForErrors() {
            webForm.getTopComponent().updateErrors();
        }

        public void setRenderFailureShown(boolean shown) {
            webForm.setRenderFailureShown(shown);
        }

        public Exception getRenderFailure() {
            return webForm.getRenderFailure();
        }

        public MarkupDesignBean getRenderFailureComponent() {
            return webForm.getRenderFailureComponent();
        }

        public void handleRefresh(boolean showErrors) {
            // Continue from the error panel to the designview
            webForm.getTopComponent().showErrors(showErrors);
            // 6274302: See if the user has cleared the error
//            webform.refresh(true);
            webForm.refreshModel(true);
        }
    } // End of ErrorPanelCallbackImpl.
}
