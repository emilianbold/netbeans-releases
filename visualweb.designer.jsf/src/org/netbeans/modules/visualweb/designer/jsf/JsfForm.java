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


import org.netbeans.modules.visualweb.designer.jsf.palette.PaletteControllerFactory;
import org.netbeans.modules.visualweb.api.designer.Designer;
import org.netbeans.modules.visualweb.api.designer.DesignerFactory;
import org.netbeans.modules.visualweb.api.designer.DomProvider;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignEvent;
import com.sun.rave.designtime.DesignProject;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.DisplayItem;
import com.sun.rave.designtime.Position;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.event.DesignContextListener;
import com.sun.rave.designtime.event.DesignProjectListener;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import com.sun.rave.designtime.markup.MarkupPosition;
import java.awt.EventQueue;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import org.netbeans.modules.visualweb.designer.jsf.text.DomDocumentImpl;
import org.netbeans.modules.visualweb.insync.live.LiveUnit;
import org.netbeans.modules.visualweb.insync.markup.MarkupUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.netbeans.modules.visualweb.insync.models.FacesModelSet;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.swing.JComponent;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.modules.visualweb.api.designer.markup.MarkupService;
import org.netbeans.modules.visualweb.api.designtime.idebridge.DesigntimeIdeBridgeProvider;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;
import org.netbeans.modules.visualweb.designer.jsf.ui.ErrorPanelImpl;
import org.netbeans.modules.visualweb.designer.jsf.ui.JsfMultiViewElement;
import org.netbeans.modules.visualweb.designer.jsf.ui.NotAvailableMultiViewElement;
import org.netbeans.modules.visualweb.designer.jsf.ui.RenderErrorPanelImpl;
import org.netbeans.modules.visualweb.insync.ModelSet;
import org.netbeans.modules.visualweb.insync.ModelSetsListener;
import org.netbeans.modules.visualweb.insync.ResultHandler;
import org.netbeans.modules.visualweb.insync.UndoEvent;
import org.netbeans.modules.visualweb.insync.Unit;
import org.netbeans.modules.visualweb.insync.Util;
import org.netbeans.modules.visualweb.insync.faces.FacesBean;
import org.netbeans.modules.visualweb.insync.faces.FacesPageUnit;
import org.netbeans.modules.visualweb.insync.faces.MarkupBean;
import org.netbeans.spi.palette.PaletteController;
import org.openide.awt.UndoRedo;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;
import org.openide.util.WeakSet;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * Represents JSF form. Something like WebForm before, but only the JSF specific part of it.
 * TODO Factor out the maintaining of the maps into JsfFormManager.
 *
 * @author Peter Zavadsky
 * @author Tor Norbye (the original code copied from the old WebForm)
 */
public class JsfForm {

//    /** Weak <code>Map</code> between <code>FacesModel</code> and <code>JsfForm</code>. */
//    private static final Map<FacesModel, JsfForm> facesModel2jsfForm = new WeakHashMap<FacesModel, JsfForm>();
    private static final Set<JsfForm> jsfForms = new WeakSet<JsfForm>();

//    /** Weak <code>Map</code> between <code>JsfForm</code> and <code>Designer</code>. */
//    private static final Map<JsfForm, Set<Designer>> jsfForm2designerSet = new WeakHashMap<JsfForm, Set<Designer>>();
    private final Set<Designer> designers = new WeakSet<Designer>();
    
//    /** Weak <code>Marp</code> between <code>Designer</code> and <code>JsfMultiViewElement</code>. */
//    private static final Map<Designer, JsfMultiViewElement> designer2jsfMultiViewElement = new WeakHashMap<Designer, JsfMultiViewElement>();
    private static final Set<JsfMultiViewElement> jsfMultiViewElements = new WeakSet<JsfMultiViewElement>();
    
    /** Maps weakly <code>DesignProject</code> to <code>JsfDesignProjectListener</code>. */
    private static final Map<DesignProject, JsfDesignProjectListener> designProject2jsfDesignProjectListener = new WeakHashMap<DesignProject, JsfDesignProjectListener>();

    private static final ReadWriteLock jsfFormsLock = new ReentrantReadWriteLock();
            
    private static final ReadWriteLock jsfMultiViewElementsLock = new ReentrantReadWriteLock();
    
    private static final ReadWriteLock designersLock = new ReentrantReadWriteLock();
    
    private static final ReadWriteLock designProject2jsfDesignProjectListenersLock = new ReentrantReadWriteLock();
    

    /** <code>FacesModel</code> associated with this JSF form. */
    private FacesModel facesModel;
    
//    /** <code>Designer</code> associated with this JSF form. */
//    private final Designer designer;

    private DomSynchronizer domSynchronizer;

//    private DesignContext designContext;

//    private final PropertyChangeListener dataObjectListener = new DataObjectPropertyChangeListener(this);

    private /*final*/ DesignContextListener designContextListener /*= new JsfDesignContextListener(this)*/;

    private /*final*/ boolean isFragment;
    private /*final*/ boolean isPortlet;

//    private final EventListenerList listenerList = new EventListenerList();

    private /*final*/ PaletteController paletteController;

    private final DomProvider domProvider = new DomProviderImpl(this);
    
    // XXX Bad (old style) error handling.
    private Exception renderFailureException;
    // XXX Bad (old style) error handling.
    private MarkupDesignBean renderFailureComponent;
    // XXX Bad (old style) error handling.
    private boolean renderFailureShown;
    
    private final DomDocumentImpl domDocumentImpl = new DomDocumentImpl(this);
    
    // XXX Caching external jsf forms.
    private final ExternalDomProviderCache externalDomProviderCache = new ExternalDomProviderCache();
    
    // XXX Moved from the designer/../WebForm.
    // XXX For fragments, this represents the assigned context page.
    private JsfForm contextJsfForm;
    
    /** XXX Virutal forms support painting. */
    private boolean virtualFormsSupportEnabled;
    /** XXX Ajax transactions support painting. */
    private boolean ajaxTransactionsSupportEnabled;
    
    private final DndSupport dndSupport = new DndSupport(this);

    private final ReadWriteLock facesModelLock = new ReentrantReadWriteLock();
    

    /** Creates a new instance of JsfForm */
    private JsfForm(FacesModel facesModel, DataObject dataObject) {
//        if (facesModel == null) {
//            throw new NullPointerException("FacesModel may not be null!"); // NOI18N
//        }
        
//        if (facesModel.getLiveUnit() == null) {
//            log(new NullPointerException("Invalid FacesModel, it has null LiveUnit, facesModel=" + facesModel)); // NOI18N
//        }

//        associateFacesModel(dataObject.getPrimaryFile());
//        synchronized (facesModel2jsfForm) {
//        synchronized (jsfForms) {
//            this.facesModel = facesModel;
//        }
        setFacesModel(facesModel);

        init(dataObject);
        if (isValid()) {
            init();
        }
    }
    
    
    private void init(DataObject dataObject) {
        // XXX This needs to be moved to insync.
        initFragmentProperty(dataObject);
        
        initPaletteController(dataObject);
    }
    
    private void init() {
        // XXX This needs to be moved to insync.
        initPortletProperty();
        
        initListening();
    }
    
    private void initFragmentProperty(DataObject dataObject) {
        // Set isFragment/isPortlet fields.
        FileObject fo = dataObject.getPrimaryFile();
        this.isFragment = "jspf".equals(fo.getExt()); // NOI18N
    }
    
    private void initPortletProperty() {
        // XXX This needs to be moved to insync.
        if (getFacesModel().getFacesModelSet() != null) {
            isPortlet = getFacesModel().getFacesModelSet().getFacesContainer().isPortletContainer();
        } else {
            isPortlet = false;
        }
    }
    
    private void initPaletteController(DataObject jspDataObject) {
        Project project = FileOwnerQuery.getOwner(jspDataObject.getPrimaryFile());
        if (project == null) {
            log(new NullPointerException("There is no project found for DataObject, jspDataObject=" + jspDataObject)); // NOI18N
        }
        paletteController = PaletteControllerFactory.getDefault().createJsfPaletteController(project);
    }
     
    private void initListening() {        
//        // Set listening.
//        dataObject.addPropertyChangeListener(WeakListeners.propertyChange(dataObjectListener, dataObject));
        initDesignProjectListening();
        updateDnDListening();
    }
    
    
    private void setFacesModel(FacesModel facesModel) {
//        synchronized (jsfForms) {
        facesModelLock.writeLock().lock();
        try {
            this.facesModel = facesModel;
        } finally {
            facesModelLock.writeLock().unlock();
        }
    }
    
    private static FacesModel getFacesModel(FileObject fileObject) {
    	FacesModelSet facesModelSet = FacesModelSet.getInstance(fileObject);
    	if (facesModelSet != null) {    		
    		return facesModelSet.getFacesModel(fileObject);
    	}
    	return null;
    }
    
    private static FacesModel getFacesModel(DataObject dataObject) {
        if (dataObject == null) {
            throw new NullPointerException("DataObject may not be null!"); // NOI18N
        }
        
        return getFacesModel(dataObject.getPrimaryFile());
    }

    
    // XXX TEMP to try out.
    private static boolean LOAD_MODEL_SYNCHRONOUSLY = Boolean.getBoolean("vwp.designer.jsf.loadModelSync"); // NOI18N
    
    public static JsfForm getJsfForm(final DataObject dataObject) {
        if (dataObject == null) {
            return null;
        }

        FacesModel facesModel;
        if (LOAD_MODEL_SYNCHRONOUSLY) {
            facesModel = getFacesModel(dataObject);
            if (facesModel == null) {
                if (!dataObject.isTemplate()) {
                    log(new IllegalArgumentException("There is no FacesModel available for non-template dataObject=" + dataObject)); // NOI18N
                }
                return null;
            }
        } else {
            // XXX TODO Here should be a method which immediatelly returns FacesModel if it is already created.
            facesModel = FacesModelSet.getFacesModelIfAvailable(dataObject.getPrimaryFile());
        }
        
        JsfForm jsfForm;
//        synchronized (facesModel2jsfForm) {
//        synchronized (jsfForms) {
//            jsfForm = facesModel2jsfForm.get(facesModel);
            jsfForm = findJsfFormForFacesModel(facesModel);
            if (jsfForm == null) {
                jsfFormsLock.writeLock().lock();
                try {
                    jsfForm = new JsfForm(facesModel, dataObject);
//                    facesModel2jsfForm.put(facesModel, jsfForm);
                    jsfForms.add(jsfForm);
                } finally {
                    jsfFormsLock.writeLock().unlock();
                }
            }

        final JsfForm finalJsfForm = jsfForm;
        // XXX FacesModel was not loaded, do it now.
        if (facesModel == null) {
            ModelSet.addModelSetsListener(new ModelSetsListener() {
                public void modelSetAdded(ModelSet modelSet) {
                    Project project = modelSet.getProject();
                    FileObject fileObject = dataObject.getPrimaryFile();
                    Project jsfProject = FileOwnerQuery.getOwner(fileObject);
                    if (project == jsfProject) {
                        finalJsfForm.loadFacesModel(dataObject);                        
                        ModelSet.removeModelSetsListener(this);
                    }
                }

                public void modelSetRemoved(ModelSet modelSet) {
                }        
            });
            FacesModelSet.startModeling(dataObject.getPrimaryFile());            
        }
        
        return jsfForm;
    }
    
    // XXX Revise, the need for this method is suspicious.
    public static JsfForm findJsfFormForDomProvider(DomProvider domProvider) {
        if (!(domProvider instanceof DomProviderImpl)) {
            return null;
        }
        
        return ((DomProviderImpl)domProvider).getJsfForm();
    }
//    public static Designer createDesigner(DataObject jsfJspDataObject) {
//        JsfForm jsfForm = JsfForm.getJsfForm(jsfJspDataObject);
//        if (jsfForm == null) {
//            return null;
//        }
//        
//        return createDesigner(jsfForm);
//    }

    public /*private*/ static Designer[] findDesigners(JsfForm jsfForm) {
//        Set<Designer> designerSet;
//        synchronized (jsfForm2designerSet) {
//            designerSet = jsfForm2designerSet.get(jsfForm);
//        }
//        if (designerSet != null) {
//            // XXX To be sure there are not lost some weak refs.
//            designerSet = new HashSet<Designer>(designerSet);
//        }
//        return designerSet == null ? new Designer[0] : designerSet.toArray(new Designer[designerSet.size()]);
        return jsfForm == null ? new Designer[0] : jsfForm.getDesigners();
    }
    
//    private static Designer createDesigner(JsfForm jsfForm) {
//        // TODO There should be always created new designer.
//        Designer designer;
////        synchronized (jsfForm2designerSet) {
////            Set<Designer> designerSet = jsfForm2designerSet.get(jsfForm);
////            if (designerSet == null) {
////                designerSet = new WeakSet<Designer>();
////            }
//            
//            designer = DesignerFactory.createDesigner(jsfForm.getDomProvider());
////            designerSet.add(designer);
////            jsfForm2designerSet.put(jsfForm, designerSet);
//            jsfForm.addDesigner(designer);
////        }
//        return designer;
//    }
    Designer createDesigner() {
        Designer designer = DesignerFactory.createDesigner(domProvider);
        addDesigner(designer);
        return designer;
    }
    
    
    public static JsfMultiViewElement[] getJsfMultiViewElements() {
//        synchronized (jsfMultiViewElements) {
        jsfMultiViewElementsLock.readLock().lock();
        try {
            return jsfMultiViewElements.toArray(new JsfMultiViewElement[jsfMultiViewElements.size()]);
        } finally {
            jsfMultiViewElementsLock.readLock().unlock();
        }
    }
    
    private static JsfMultiViewElement[] findJsfMultiViewElements(JsfForm jsfForm) {
//        Designer[] designers = findDesigners(jsfForm);
        if (jsfForm == null) {
            return new JsfMultiViewElement[0];
        }
        
        Set<JsfMultiViewElement> multiViewElements;
//        synchronized (designer2jsfMultiViewElement) {
//        synchronized (jsfMultiViewElements) {
//            for (Designer designer : designers) {
//                JsfMultiViewElement jsfMultiViewElement = designer2jsfMultiViewElement.get(designer);
//                if (jsfMultiViewElement != null) {
//                    jsfMultiViewElements.add(jsfMultiViewElement);
//                }
//            }
        jsfMultiViewElementsLock.readLock().lock();
        try {
            multiViewElements = new HashSet<JsfMultiViewElement>(jsfMultiViewElements);
        } finally {
            jsfMultiViewElementsLock.readLock().unlock();
        }
        for (Iterator<JsfMultiViewElement> it = multiViewElements.iterator(); it.hasNext(); ) {
            JsfMultiViewElement multiViewElement = it.next();
            if (multiViewElement.getJsfForm() != jsfForm) {
                it.remove();
            }
        }
        return multiViewElements.toArray(new JsfMultiViewElement[multiViewElements.size()]);
    }
    
    static MultiViewElement createMultiViewElement(JsfForm jsfForm, Designer designer, DataObject jspDataObject) {
        if (jsfForm == null || designer == null) {
            return new NotAvailableMultiViewElement();
        }
        
        JsfMultiViewElement jsfMultiViewElement = new JsfMultiViewElement(jsfForm, designer, jspDataObject);
//        synchronized (designer2jsfMultiViewElement) {
//            designer2jsfMultiViewElement.put(designer, jsfMultiViewElement);
//        }
//        synchronized (jsfMultiViewElements) {
        jsfMultiViewElementsLock.writeLock().lock();
        try {
            jsfMultiViewElements.add(jsfMultiViewElement);
        } finally {
            jsfMultiViewElementsLock.writeLock().unlock();
        }
        return jsfMultiViewElement;
    }
    
    public static JsfMultiViewElement findJsfMultiViewElementForDesigner(Designer designer) {
        if (designer == null) {
            return null;
        }
//        synchronized (designer2jsfMultiViewElement) {
//            return designer2jsfMultiViewElement.get(designer);
//        }
        Set<JsfMultiViewElement> multiViewElements;
//        synchronized (jsfMultiViewElements) {
        jsfMultiViewElementsLock.readLock().lock();
        try {
            multiViewElements = new HashSet<JsfMultiViewElement>(jsfMultiViewElements);
        } finally {
            jsfMultiViewElementsLock.readLock().unlock();
        }
        
        for (JsfMultiViewElement multiViewElement : multiViewElements) {
            if (multiViewElement.getDesigner() == designer) {
                return multiViewElement;
            }
        }
        return null;
    }
    
    static Designer[] getDesigners(JsfForm jsfForm) {
        if (jsfForm == null) {
            return new Designer[0];
        }
        Designer[] designers = findDesigners(jsfForm);
        if (designers.length == 0) {
//            Designer designer = createDesigner(jsfForm);
            Designer designer = jsfForm.createDesigner();
            return new Designer[] {designer};
        }
        return designers;
    }
    
    static Designer[] getDesignersForDataObject(DataObject jsfJspDataObject) {
        JsfForm jsfForm = getJsfForm(jsfJspDataObject);
        if (jsfForm == null) {
            return new Designer[0];
        }
//        Designer[] designers = findDesigners(jsfForm);
//        if (designers.length == 0) {
//            Designer designer = createDesigner(jsfForm);
//            return new Designer[] {designer};
//        }
//        return designers;
        return getDesigners(jsfForm);
    }

//    static Designer[] findDesignersForFileObject(FileObject jsfJspFileObject) {
//        JsfForm jsfForm = findJsfForm(jsfJspFileObject);
//        if (jsfForm == null) {
//            return new Designer[0];
//        }
//        return findDesigners(jsfForm);
//    }

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
        return findJsfFormForFacesModel(facesModel);
    }
    
//    static JsfForm findJsfForm(FacesModel facesModel) {
//        return findJsfForm(facesModel);
//    }

    /*private*/ static JsfForm findJsfForm(FileObject fileObject) {
        if (fileObject == null) {
            return null;
        }
        
        FacesModel facesModel = getFacesModel(fileObject);
        if (facesModel == null) {
            return null;
        }
        return findJsfFormForFacesModel(facesModel);
    }
    
    /*private*/public static JsfForm findJsfForm(DesignContext designContext) {
        if (designContext == null) {
            return null;
        }
        
        FacesModel facesModel = ((LiveUnit)designContext).getModel();
        if (facesModel == null) {
            return null;
        }
        return findJsfFormForFacesModel(facesModel);
    }
    
    /*private*/ static JsfForm findJsfForm(Element element) {
        MarkupDesignBean markupDesignBean = MarkupUnit.getMarkupDesignBeanForElement(element);
        if (markupDesignBean == null) {
            return null;
        }
        DesignContext designContext = markupDesignBean.getDesignContext();
        return findJsfForm(designContext);
    }
    
    /*private*/ static JsfForm findJsfFormForFacesModel(FacesModel facesModel) {
        if (facesModel == null) {
            return null;
        }
//        synchronized (facesModel2jsfForm) {
//            return facesModel2jsfForm.get(facesModel);
//        }
        Set<JsfForm> forms;
//        synchronized (jsfForms) {
        jsfFormsLock.readLock().lock();
        try {
            forms = new HashSet<JsfForm>(jsfForms);
        } finally {
            jsfFormsLock.readLock().unlock();
        }
        for (JsfForm jsfForm : forms) {
            if (jsfForm != null && jsfForm.getFacesModel() == facesModel) {
                return jsfForm;
            }
        }
        return null;
    }

    
//    DomProvider getDomProvider() {
//        return domProvider;
//    }

//    static boolean hasDomProvider(DataObject dataObject) {
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

    
    private void initDesignProjectListening() {
        LiveUnit liveUnit = getFacesModel().getLiveUnit();
        if (liveUnit == null) {
            log(new NullPointerException("Invalid FacesModel, it has null LiveUnit, facesModel=" + getFacesModel())); // NOI18N
            return;
        }
        DesignProject designProject = liveUnit.getProject();
        if (designProject == null) {
            // Log issue?
            return;
        }
        JsfDesignProjectListener jsfDesignProjectListener;
//        synchronized (designProject2jsfDesignProjectListener) {
        designProject2jsfDesignProjectListenersLock.readLock().lock();
        try {
            jsfDesignProjectListener = designProject2jsfDesignProjectListener.get(designProject);
        } finally {
            designProject2jsfDesignProjectListenersLock.readLock().unlock();
        }
        if (jsfDesignProjectListener == null) {
            designProject2jsfDesignProjectListenersLock.writeLock().lock();
            try {
                jsfDesignProjectListener = new JsfDesignProjectListener();
                designProject.addDesignProjectListener(WeakListeners.create(DesignProjectListener.class, jsfDesignProjectListener, designProject));
                designProject2jsfDesignProjectListener.put(designProject, jsfDesignProjectListener);
            } finally {
                designProject2jsfDesignProjectListenersLock.writeLock().lock();
            }
        }
    }
    
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
    
//    private void replaceFacesModel(FileObject oldFo, FileObject newFo) {
//        if (oldFo != null) {
////            designer.destroyDesigner();
//            // XXX There would be weak listeners needed.
//            getDomSynchronizer().unregisterDomListeners();
//        }
//        // XXX Force new DomSynchronizer.
//        domSynchronizer = null;
//        
//        if (newFo != null) {
////            associateFacesModel(newFo);
//
////            FacesModel newModel = FacesModel.getInstance(newFo);
//            FacesModel newModel = getFacesModel(newFo);
//            if (newModel == null) {
//                throw new IllegalArgumentException("Null FacesModel for FileObject, fo=" + newFo); // NOI18N
//            }
//////            synchronized (facesModel2jsfForm) {
////            synchronized (jsfForms) {
//////                facesModel2jsfForm.remove(this.facesModel);
////                this.facesModel = newModel;
//////                facesModel2jsfForm.put(this.facesModel, this);
////            }
//            setFacesModel(newModel);
//            
//            updateDnDListening();
//            
//            getDomSynchronizer().requestRefresh();
//        }
//    }
    
    private FacesModel getFacesModel() {
//        synchronized (jsfForms) {
        facesModelLock.readLock().lock();
        try {
            return facesModel;
        } finally {
            facesModelLock.readLock().unlock();
        }
    }
    
//    public Document getJspDom() {
////        return InSyncServiceProvider.get().getJspDomForMarkupFile(getFacesModel().getMarkupFile());
//        return getFacesModel().getJspDom();
//    }
    
    // >> Impl of DomProvider
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
    
//    boolean isRefreshPending() {
//        return getDomSynchronizer().isRefreshPending();
//    }
    
//    private void attachContext(DesignContext designContext) {
//////        getDomSynchronizer().attachContext(context);
////        if (this.designContext == designContext) {
////            return;
////        }
////        
////        detachContext();
////        this.designContext = designContext;
////        
////        if (designContext != null) {
////            designContext.addDesignContextListener(designContextListener);
////        }
//        updateDesignContextListening(designContext);
//    }
     
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
    // << Impl of DomProvider
    
    //////
    // XXX See DomSynchronizer
    void modelChanged() {
//        designer.modelChanged();
//        fireModelChanged();
        notifyViewsModelChanged();
    }
    
//    void nodeChanged(Node rendered, Node parent) {
////        designer.nodeChanged(rendered, parent, wasMove);
////        fireNodeChanged(rendered, parent, wasMove);
//        notifyViewsNodeChanged(rendered, parent, null);
//    }

    void nodeChanged(Node rendered, Node parent, Element[] changedElements) {
//        designer.nodeChanged(rendered, parent, wasMove);
//        fireNodeChanged(rendered, parent, wasMove);
        notifyViewsNodeChanged(rendered, parent, changedElements);
    }

    
    void nodeRemoved(Node previouslyRendered, Node parent) {
//        designer.nodeRemoved(previouslyRendered, parent);
//        fireNodeRemoved(previouslyRendered, parent);
        notifyViewsNodeRemoved(previouslyRendered, parent);
    }
    
    void nodeInserted(Node rendered, Node parent) {
//        designer.nodeInserted(rendered, parent);
//        fireNodeInserted(rendered, parent);
        notifyViewsNodeInserted(rendered, parent);
    }
    
    // XXX Moved from designer/../WebForm.
    /** XXX Moved from FacesSupport. Updates erros in the corresponding component.
     * TODO Usage of this after renderHtml call is very suspicious, revise. */
    public void updateErrorsInComponent() {
//        designer.updateErrorsInComponent();
//        fireUpdateErrorsInComponent();
        
//        FileObject markupFile = getModel().getMarkupFile();
//// <missing designtime api>
////        Exception renderFailure = facesunit.getRenderFailure();
////        MarkupDesignBean renderFailureComponent =
////            (MarkupDesignBean)facesunit.getRenderFailureComponent();
//// ====
//        Exception renderFailure = InSyncService.getProvider().getRenderFailure(markupFile);
        
//        Exception renderFailure = domProvider.getRenderFailure();
////        MarkupDesignBean renderFailureComponent = (MarkupDesignBean)InSyncService.getProvider().getRenderFailureComponent(markupFile);
//        MarkupDesignBean renderFailureComponent = domProvider.getRenderFailureMarkupDesignBean();
//        
//// </missing designtime api>
//
//        setRenderFailedValues(renderFailureComponent, renderFailure);
        updateRenderFailureValues();

//        if (renderFailure == null) {
        if (!hasRenderFailure()) {
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
//        if (getTopComponent().isShowing()) {
//            // In case some kind of rendering error happened
//            // Ugh... I need to track this differently!
//            getTopComponent().updateErrors();
//        }
//        domProvider.tcUpdateErrors(this);
        notifyViewsUpdateErrors();
    }
    
    
    void updateGridMode() {
//        designer.updateGridMode();
//        fireUpdateGridMode(isGridMode());
        notifyViewsGridModeUpdated(isGridMode());
    }
    
    public boolean isGridMode() {
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
        return Util.isGridMode(getFacesModel());
    }
    
    void documentReplaced() {
//        designer.documentReplaced();
//        fireDocumentReplaced();
        notifyViewsDocumentReplaced();
    }
    
    void showDropMatch(Element componentRootElement, Element regionElement, int dropType) {
        notifyViewsShowDropMatch(componentRootElement, regionElement, dropType);
    }
    
    void clearDropMatch() {
        notifyViewsClearDropMatch();
    }
    
    void selectComponent(Element componentRootElement) {
        notifyViewsSelectComponent(componentRootElement);
    }
    
    void inlineEditComponents(Element[] componentRootElements) {
        notifyViewsInlineEditComponents(componentRootElements);
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
    
    FacesDndSupport.UpdateSuspender getUpdateSuspender() {
        return getDomSynchronizer();
    }
    
    //////
    // XXX See DomProvider interface.
    void requestRefresh() {
        getDomSynchronizer().requestRefresh();
    }
    
    public void refreshModelWithExternals(boolean deep) {
        JsfForm[] externals = findExternals();
        for (JsfForm jsfForm : externals) {
            jsfForm.refreshModel(deep);
        }
        
        refreshModel(deep);
    }
    
    private JsfForm[] findExternals() {
        Designer[] designers = findDesigners(this);
        if (designers.length == 0) {
            return new JsfForm[0];
        }
        Designer designer = designers[0];
        DomProvider[] domProviders = designer.getExternalDomProviders();
        List<JsfForm> externals = new ArrayList<JsfForm>();
        for (DomProvider domProvider : domProviders) {
            JsfForm external = findJsfFormForDomProvider(domProvider);
            if (external == null) {
                continue;
            }
            if (external == this) {
                // Skip this one.
                continue;
            }
            externals.add(external);
        }
        return externals.toArray(new JsfForm[externals.size()]);
    }
    
    public void refreshModel(boolean deep) {
        getFacesModel().refreshAndSyncNonPageBeans(deep);
        // XXX Moved from designer/../WebForm.
        externalDomProviderCache.flush();
        
//        fireModelRefreshed();
        notifyViewsModelRefreshed();
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
    
//    public DesignBean[] pasteBeans(Transferable t, DesignBean parent, MarkupPosition pos, Point location, DomProvider.CoordinateTranslator coordinateTranslator) {
//        return getFacesModel().getDnDSupport().pasteBeans(t, parent, pos, location, new CoordinateTranslatorImpl(coordinateTranslator), getDomSynchronizer());
//    }
    
//    public void importData(JComponent comp, Transferable t, Object transferData, Dimension dimension, omProvider.Location location, DomProvider.CoordinateTranslator coordinateTranslator, int dropAction) {
//        getFacesModel().getDnDSupport().importData(comp, t, transferData, dimension, new LocationImpl(location), new CoordinateTranslatorImpl(coordinateTranslator), getDomSynchronizer(), dropAction);
//    }
    
//    public DesignBean findHtmlContainer(DesignBean parent) {
//        return Util.findHtmlContainer(parent);
//    }
    
//    public String[] getClassNames(DisplayItem[] displayItems) {
//        return getFacesModel().getDnDSupport().getClasses(displayItems);
//    }
    
//    public boolean importBean(DisplayItem[] items, DesignBean origParent, int nodePos, String facet, List createdBeans, DomProvider.Location location, DomProvider.CoordinateTranslator coordinateTranslator)
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
    
    public boolean isFragment() {
        return isFragment;
    }

    public boolean isPortlet() {
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

//    void addDomProviderListener(DomProvider.DomProviderListener l) {
//        listenerList.add(DomProvider.DomProviderListener.class, l);
//    }
//
//    void removeDomProviderListener(DomProvider.DomProviderListener l) {
//        listenerList.remove(DomProvider.DomProviderListener.class, l);
//    }
    
//    private DomProvider.DomProviderListener[] getDomProviderListeners() {
//        // Guaranteed to return a non-null array
//        Object[] listeners = listenerList.getListenerList();
//        
//        List<DomProvider.DomProviderListener> dDomProviderListeners = new ArrayList<DomProvider.DomProviderListener>();
//        // Process the listeners last to first, notifying
//        // those that are interested in this event
//        for (int i = listeners.length-2; i>=0; i-=2) {
//            if (listeners[i] == DomProvider.DomProviderListener.class) {
//                domProviderListeners.add((DomProvider.DomProviderListener)listeners[i+1]);
//            }          
//        }
//        return domProviderListeners.toArray(new DomProvider.DomProviderListener[domProviderListeners.size()]);
//    }

//    private void fireModelChanged() {
//        DomProvider.DomProviderListener[] listeners = getDomProviderListeners();
//        for (DomProvider.DomProviderListener listener : listeners) {
//            listener.modelChanged();
//        }
//    }
    private void notifyViewsModelChanged() {
        JsfMultiViewElement[] jsfMultiViewElements = findJsfMultiViewElements(this);
        for (JsfMultiViewElement jsfMultiViewElement : jsfMultiViewElements) {
            jsfMultiViewElement.modelChanged();
        }
    }
    
//    private void fireModelRefreshed() {
//        DomProvider.DomProviderListener[] listeners = getDomProviderListeners();
//        for (DomProvider.DomProviderListener listener : listeners) {
//            listener.modelRefreshed();
//        }
//    }
    private void notifyViewsModelRefreshed() {
        JsfMultiViewElement[] jsfMultiViewElements = findJsfMultiViewElements(this);
        for (JsfMultiViewElement jsfMultiViewElement : jsfMultiViewElements) {
            jsfMultiViewElement.modelRefreshed();
        }
    }

//    private void fireNodeChanged(Node rendered, Node parent, boolean wasMove) {
//        DomProvider.DomProviderListener[] listeners = getDomProviderListeners();
//        for (DomProvider.DomProviderListener listener : listeners) {
//            listener.nodeChanged(rendered, parent, wasMove);
//        }
//    }
    private void notifyViewsNodeChanged(Node node, Node parent, Element[] changedElements) {
        JsfMultiViewElement[] jsfMultiViewElements = findJsfMultiViewElements(this);
        for (JsfMultiViewElement jsfMultiViewElement : jsfMultiViewElements) {
            jsfMultiViewElement.nodeChanged(node, parent, changedElements);
        }
    }

//    private void fireNodeRemoved(Node previouslyRendered, Node parent) {
//        DomProvider.DomProviderListener[] listeners = getDomProviderListeners();
//        for (DomProvider.DomProviderListener listener : listeners) {
//            listener.nodeRemoved(previouslyRendered, parent);
//        }
//    }
    private void notifyViewsNodeRemoved(Node node, Node parent) {
        JsfMultiViewElement[] jsfMultiViewElements = findJsfMultiViewElements(this);
        for (JsfMultiViewElement jsfMultiViewElement : jsfMultiViewElements) {
            jsfMultiViewElement.nodeRemoved(node, parent);
        }
    }

//    private void fireNodeInserted(Node rendered, Node parent) {
//        DomProvider.DomProviderListener[] listeners = getDomProviderListeners();
//        for (DomProvider.DomProviderListener listener : listeners) {
//            listener.nodeInserted(rendered, parent);
//        }
//    }
    private void notifyViewsNodeInserted(Node node, Node parent) {
        JsfMultiViewElement[] jsfMultiViewElements = findJsfMultiViewElements(this);
        for (JsfMultiViewElement jsfMultiViewElement : jsfMultiViewElements) {
            jsfMultiViewElement.nodeInserted(node, parent);
        }
    }

//    private void fireUpdateErrorsInComponent() {
//        DomProvider.DomProviderListener[] listeners = getDomProviderListeners();
//        for (DomProvider.DomProviderListener listener : listeners) {
//            listener.updateErrorsInComponent();
//        }
//    }

//    private void fireUpdateGridMode(boolean gridMode) {
//        DomProvider.DomProviderListener[] listeners = getDomProviderListeners();
//        for (DomProvider.DomProviderListener listener : listeners) {
//            listener.gridModeUpdated(gridMode);
//        }
//    }
    private void notifyViewsGridModeUpdated(boolean gridMode) {
        JsfMultiViewElement[] jsfMultiViewElements = findJsfMultiViewElements(this);
        for (JsfMultiViewElement jsfMultiViewElement : jsfMultiViewElements) {
            jsfMultiViewElement.gridModeUpdated(gridMode);
        }
    }

//    private void fireDocumentReplaced() {
//        DomProvider.DomProviderListener[] listeners = getDomProviderListeners();
//        for (DomProvider.DomProviderListener listener : listeners) {
//            listener.documentReplaced();
//        }
//    }
    private void notifyViewsDocumentReplaced() {
        JsfMultiViewElement[] jsfMultiViewElements = findJsfMultiViewElements(this);
        for (JsfMultiViewElement jsfMultiViewElement : jsfMultiViewElements) {
            jsfMultiViewElement.documentReplaced();
        }
    }

//    void fireShowDropMatch(Element componentRootElement, Element regionElement, int dropType) {
//        DomProvider.DomProviderListener[] listeners = getDomProviderListeners();
//        for (DomProvider.DomProviderListener listener : listeners) {
//            listener.showDropMatch(componentRootElement, regionElement, dropType);
//        }
//    }
    private void notifyViewsShowDropMatch(Element componentRootElement, Element regionElement, int dropType) {
        JsfMultiViewElement[] jsfMultiViewElements = findJsfMultiViewElements(this);
        for (JsfMultiViewElement jsfMultiViewElement : jsfMultiViewElements) {
            jsfMultiViewElement.showDropMatch(componentRootElement, regionElement, dropType);
        }
    }
    
//    void fireClearDropMatch() {
//        DomProvider.DomProviderListener[] listeners = getDomProviderListeners();
//        for (DomProvider.DomProviderListener listener : listeners) {
//            listener.clearDropMatch();
//        }
//    }
    private void notifyViewsClearDropMatch() {
        JsfMultiViewElement[] jsfMultiViewElements = findJsfMultiViewElements(this);
        for (JsfMultiViewElement jsfMultiViewElement : jsfMultiViewElements) {
            jsfMultiViewElement.clearDropMatch();
        }
    }

//    void fireSelect(DesignBean designBean) {
//        Element componentRootElement = JsfSupportUtilities.getComponentRootElementForDesignBean(designBean);
//        DomProvider.DomProviderListener[] listeners = getDomProviderListeners();
//        for (DomProvider.DomProviderListener listener : listeners) {
////            listener.select(designBean);
//            listener.select(componentRootElement);
//        }
//    }
    private void notifyViewsSelectComponent(Element componentRootElement) {
        JsfMultiViewElement[] jsfMultiViewElements = findJsfMultiViewElements(this);
        for (JsfMultiViewElement jsfMultiViewElement : jsfMultiViewElements) {
            jsfMultiViewElement.selectComponent(componentRootElement);
        }
    }

//    private void fireRefreshForm(boolean deep) {
//        DomProvider.DomProviderListener[] listeners = getDomProviderListeners();
//        for (DomProvider.DomProviderListener listener : listeners) {
//            listener.refreshForm(deep);
//        }
//    }

//    void fireInlineEdit(DesignBean[] designBeans) {
//        List<Element> componentRootElements = new ArrayList<Element>();
//        for (DesignBean designBean : designBeans) {
//            Element componentRootElement = JsfSupportUtilities.getComponentRootElementForDesignBean(designBean);
//            if (componentRootElement != null) {
//                componentRootElements.add(componentRootElement);
//            }
//        }
//        DomProvider.DomProviderListener[] listeners = getDomProviderListeners();
//        for (DomProvider.DomProviderListener listener : listeners) {
////            listener.inlineEdit(designBeans);
//            listener.inlineEdit(componentRootElements.toArray(new Element[componentRootElements.size()]));
//        }
//    }
    private void notifyViewsInlineEditComponents(Element[] componentRootElements) {
        JsfMultiViewElement[] jsfMultiViewElements = findJsfMultiViewElements(this);
        for (JsfMultiViewElement jsfMultiViewElement : jsfMultiViewElements) {
            jsfMultiViewElement.inlineEditComponents(componentRootElements);
        }
    }

//    private void fireDesignContextActivated(DesignContext designContext) {
//        DomProvider.DomProviderListener[] listeners = getDomProviderListeners();
//        for (DomProvider.DomProviderListener listener : listeners) {
//            listener.designContextActivated(designContext);
//        }
//    }

//    private void fireDesignContextDeactivated(DesignContext designContext) {
//        DomProvider.DomProviderListener[] listeners = getDomProviderListeners();
//        for (DomProvider.DomProviderListener listener : listeners) {
//            listener.designContextDeactivated(designContext);
//        }
//    }
    
    private void notifyViewsUpdateErrors() {
        JsfMultiViewElement[] jsfMultiViewElements = JsfForm.findJsfMultiViewElements(this);
        for (JsfMultiViewElement jsfMultiViewElement : jsfMultiViewElements) {
            jsfMultiViewElement.updateErrors();
        }
    }
    
    private void notifyViewsDesignContextGenerationChanged() {
        JsfMultiViewElement[] jsfMultiViewElements = JsfForm.findJsfMultiViewElements(this);
        for (JsfMultiViewElement jsfMultiViewElement : jsfMultiViewElements) {
            jsfMultiViewElement.designContextGenerationChanged();
        }
    }

    // XXX Hack to skip firing events if the generation is the same. Old code moved from designer.
    private long generationSeen = 0L;    
    
    private void designContextChanged(DesignContext designContext) {
        long currentGeneration;
        if (designContext instanceof LiveUnit) {
            currentGeneration = ((LiveUnit)designContext).getContextGeneration();
        } else {
            currentGeneration = 0L;
        }
        
        if (currentGeneration == generationSeen) {
            // XXX Skip event firing.
            return;
        }
        generationSeen = currentGeneration;
        
//        DomProvider.DomProviderListener[] listeners = getDomProviderListeners();
//        for (DomProvider.DomProviderListener listener : listeners) {
////            listener.designContextChanged(designContext);
//            listener.designContextGenerationChanged();
//        }
        notifyViewsDesignContextGenerationChanged();
    }

//    private void fireDesignBeanCreated(DesignBean designBean) {
//        DomProvider.DomProviderListener[] listeners = getDomProviderListeners();
//        for (DomProvider.DomProviderListener listener : listeners) {
//            listener.designBeanCreated(designBean);
//        }
//    }

//    private void fireDesignBeanDeleted(DesignBean designBean) {
//        DomProvider.DomProviderListener[] listeners = getDomProviderListeners();
//        for (DomProvider.DomProviderListener listener : listeners) {
//            listener.designBeanDeleted(designBean);
//        }
//    }

//    private void fireDesignBeanMoved(DesignBean designBean, DesignBean designBean0, Position position) {
//        DomProvider.DomProviderListener[] listeners = getDomProviderListeners();
//        for (DomProvider.DomProviderListener listener : listeners) {
//            listener.designBeanMoved(designBean, designBean0, position);
//        }
//    }

//    private void fireDesignBeanContextActivated(DesignBean designBean) {
//        DomProvider.DomProviderListener[] listeners = getDomProviderListeners();
//        for (DomProvider.DomProviderListener listener : listeners) {
//            listener.designBeanContextActivated(designBean);
//        }
//    }

//    private void fireDesignBeanContextDeactivated(DesignBean designBean) {
//        DomProvider.DomProviderListener[] listeners = getDomProviderListeners();
//        for (DomProvider.DomProviderListener listener : listeners) {
//            listener.designBeanContextDeactivated(designBean);
//        }
//    }

//    private void fireDesignBeanNameChanged(DesignBean designBean, String string) {
//        DomProvider.DomProviderListener[] listeners = getDomProviderListeners();
//        for (DomProvider.DomProviderListener listener : listeners) {
//            listener.designBeanNameChanged(designBean, string);
//        }
//    }

//    private void fireDesignBeanChanged(DesignBean designBean) {
//        DomProvider.DomProviderListener[] listeners = getDomProviderListeners();
//        for (DomProvider.DomProviderListener listener : listeners) {
//            listener.designBeanChanged(designBean);
//        }
//    }

//    private void fireDesignPropertyChanged(DesignProperty designProperty, Object object) {
//        DomProvider.DomProviderListener[] listeners = getDomProviderListeners();
//        for (DomProvider.DomProviderListener listener : listeners) {
//            listener.designPropertyChanged(designProperty, object);
//        }
//    }

//    private void fireDesignEventChanged(DesignEvent designEvent) {
//        DomProvider.DomProviderListener[] listeners = getDomProviderListeners();
//        for (DomProvider.DomProviderListener listener : listeners) {
//            listener.designEventChanged(designEvent);
//        }
//    }

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

    public PaletteController getPaletteController() {
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
//        synchronized (jsfForm2designerSet) {
//            allJsfForms = jsfForm2designerSet.keySet();
//        }
//        synchronized (jsfForms) {
        jsfFormsLock.readLock().lock();
        try {
            allJsfForms = new HashSet<JsfForm>(jsfForms);
        } finally {
            jsfFormsLock.readLock().unlock();
        }
        for (JsfForm jsfForm : allJsfForms) {
            if (project == jsfForm.getFacesModel().getProject()
            && !projectJsfForms.contains(jsfForm)) {
                projectJsfForms.add(jsfForm);
            }
        }
        return projectJsfForms.toArray(new JsfForm[projectJsfForms.size()]);
    }

    private void updateRenderFailureValues() {
        Exception failure = getRenderFailure();
        MarkupDesignBean renderFailureComponent = getRenderFailureMarkupDesignBean();
        setRenderFailureValues(renderFailureComponent, failure);
    }
    
    private /*public*/ Exception getRenderFailure() {
        FacesPageUnit facesPageUnit = getFacesModel().getFacesUnit();
        if (facesPageUnit == null) {
            return null;
        }
        return facesPageUnit.getRenderFailure();
    }

    private /*public*/ MarkupDesignBean getRenderFailureMarkupDesignBean() {
        FacesPageUnit facesPageUnit = getFacesModel().getFacesUnit();
        if (facesPageUnit == null) {
            return null;
        }
        DesignBean designBean = facesPageUnit.getRenderFailureComponent();
        if (designBean instanceof MarkupDesignBean) {
            return (MarkupDesignBean)designBean;
        } else {
            return null;
        }
    }
    
    private void setRenderFailureValues(MarkupDesignBean renderFailureComponent, Exception renderFailureException) {
        this.renderFailureComponent = renderFailureComponent;
        this.renderFailureException = renderFailureException;
    }
    
    private boolean hasRenderFailure() {
        return getRenderFailureException() != null;
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

    FacesModel.JsfSupport getJsfSupport() {
        // XXX 
        return getDndSupport();
    }

    public Element getHtmlBody() {
//        return domProvider.getHtmlBody();
        return getHtmlBody(true);
    }

    // XXX Side effect, updating errors, old code.
    public Element getHtmlBody(boolean updateErrors) {
        Element body = getFacesModel().getHtmlBody();
        
        // XXX #6472138 FIXME Is this correct here?
        if (updateErrors) {
            updateErrorsInComponent();
        }
        return body;
    }
    
    public Document getJspDom() {
//        return domProvider.getJspDom();
        return getFacesModel().getJspDom();
    }
    
    Document getHtmlDom() {
//        return domProvider.getHtmlDom();
        return getFacesModel().getHtmlDom();
    }
    
    public Element createComponent(String className, Node parent, Node before) {
//        return domProvider.createComponent(className, parent, before);
        DesignBean designBean = createBean(className, parent, before);
        return designBean instanceof MarkupDesignBean ? ((MarkupDesignBean)designBean).getElement() : null;
    }
    
    // XXX Copy also in insync/FacesDnDSupport.
    /*public*/ private DesignBean createBean(String className, Node parent, Node before) {
        MarkupPosition pos = new MarkupPosition(parent, before);
        DesignBean parentBean = /*FacesSupport.*/Util.findParentBean(parent);
        LiveUnit unit = getFacesModel().getLiveUnit();
        return unit.createBean(className, parentBean, pos);
    }
    
    public boolean moveComponent(Element componentRootElement, Node parentNode, Node before) {
//        return domProvider.moveComponent(componentRootElement, parentNode, before);
        MarkupDesignBean bean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
        if (bean == null) {
            return false;
        }
        
//        LiveUnit lu = getFacesModel().getLiveUnit();
        LiveUnit lu = getLiveUnit();
        MarkupPosition markupPos = new MarkupPosition(parentNode, before);
        DesignBean parentBean = null;
        Node e = parentNode;

        while (e != null) {
//            if (e instanceof RaveElement) {
//                parentBean = ((RaveElement)e).getDesignBean();
            if (e instanceof Element) {
//                parentBean = InSyncService.getProvider().getMarkupDesignBeanForElement((Element)e);
                parentBean = MarkupUnit.getMarkupDesignBeanForElement((Element)e);
                
                if (parentBean != null) {
                    break;
                }
            }

            e = e.getParentNode();
        }

        if (bean == parentBean) {
            return false;
        }

        return lu.moveBean(bean, parentBean, markupPos);
    }

    public boolean isInlineEditing() {
        Designer[] designers = findDesigners(this);
        for (Designer designer : designers) {
            if (designer.isInlineEditing()) {
                return true;
            }
        }
        
        return false;
    }

//    public WriteLock writeLock(String message) {
//        return domProvider.writeLock(message);
//    }
//
//    public void writeUnlock(WriteLock writeLock) {
//        domProvider.writeUnlock(writeLock);
//    }
    public UndoEvent writeLock(String message) {
        return getFacesModel().writeLock(message);
    }

    public void writeUnlock(UndoEvent writeLock) {
        getFacesModel().writeUnlock(writeLock);
    }

//    public void deleteComponent(Element componentRootElement) {
//        domProvider.deleteComponent(componentRootElement);
//    }

    DomDocumentImpl getDomDocumentImpl() {
        return domDocumentImpl;
    }
    
    public /*private*/ void syncModel() {
//        domProvider.syncModel();
        if (isValid()) {
            getFacesModel().sync();
        }
    }
    
    public void setModelActivated(boolean activated) {
//        domProvider.setModelActivated(activated);
        getFacesModel().setActivated(activated);
    }
    
    boolean isModelValid() {
        // XXX
        MarkupUnit markupUnit = getFacesModel().getMarkupUnit();
        if (markupUnit == null) {
            return false;
        }
        return getFacesModel().isValid();
    }
    
    public boolean isModelBusted() {
//        return domProvider.isModelBusted();
        return getFacesModel().isBusted();
    }
    
    public /*private*/ void clearHtml() {
//        domProvider.clearHtml();
        getFacesModel().clearHtml();
    }
    
    public DataObject getJspDataObject() {
//        return domProvider.getJspDataObject();
        FileObject file = getMarkupFile();

        try {
            return DataObject.find(file);
        } catch (DataObjectNotFoundException ex) {
            log(ex);

            return null;
        }
    }
    
    public void deleteDesignBean(DesignBean designBean) {
        getFacesModel().getLiveUnit().deleteBean(designBean);
    }
    
    public void setUpdatesSuspended(Element componentRootElement, boolean suspend) {
//        domProvider.setUpdatesSuspended(componentRootElement, suspend);
        MarkupDesignBean markupDesignBean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
        setUpdatesSuspended(markupDesignBean, suspend);
    }
    
    public boolean isWriteLocked() {
        return getFacesModel().isWriteLocked();
    }
    
    public void snapToGrid(Designer designer) {
        getDomDocumentImpl().snapToGrid(designer);
    }
    
    public void align(Designer designer, Alignment alignment) {
        getDomDocumentImpl().align(designer, alignment);
    }

    /** Alignments. */
    public enum Alignment {
        SNAP_TO_GRID,
        TOP,
        MIDDLE,
        BOTTOM,
        LEFT,
        CENTER,
        RIGHT
    } // End of Alignment.

    
    private static final DataFlavor FLAVOR_DISPLAY_ITEM = new DataFlavor(
            DataFlavor.javaJVMLocalObjectMimeType + "; class=" + DisplayItem.class.getName(), // NOI18N
            "RAVE_PALETTE_ITEM"); // TODO get rid of such name.
    
    public boolean canPasteTransferable(Transferable trans) {
//        return domProvider.canPasteTransferable(trans);
        if (trans != null) {
            DataFlavor[] df = trans.getTransferDataFlavors();
            int n = 0;

            if (df != null) {
                n = df.length;
            }

            for (int i = 0; i < n; i++) {
                DataFlavor flavor = df[i];

		// XXX TODO Get rid of this dep, you can specify your own data flavor
		// which can match, there will be created new data flavors avoiding
		// usage of .
                if (FLAVOR_DISPLAY_ITEM.equals(flavor)
		|| (flavor.getRepresentationClass() == String.class)
		|| flavor.getMimeType().startsWith("application/x-creator-")) { // NOI18N
                    // Yes!
                    return true;
                }
            }
        }
        return false;
    }
    
    public UndoRedo getUndoManager() {
//        return domProvider.getUndoManager();
        return getFacesModel().getUndoManager();
    }
    
    public int computeActions(Element componentRootElement, Transferable transferable) {
//        return domProvider.computeActions(componentRootElement, transferable);
        MarkupDesignBean droppee = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
        return getDndSupport().computeActions(droppee, transferable);
    }
    
    public void attachContext() {
//        domProvider.attachContext();
        DesignContext context = getFacesModel().getLiveUnit();
        if (context != null) {
//            attachContext(context);
            updateDesignContextListening(context);
        }
    }
    
    
    public boolean hasRenderingErrors() {
//        return domProvider.hasRenderingErrors();
        return getRenderFailureComponent() != null;
    }
    
    public JComponent getErrorPanel(ErrorPanelCallback errorPanelCallback) {
//        return (JComponent)domProvider.getErrorPanel(errorPanelCallback);
        FacesModel facesModel = getFacesModel();
        if (facesModel.isBusted()) {
            return new ErrorPanelImpl(facesModel, facesModel.getErrors(), errorPanelCallback);
        } else {
            return new RenderErrorPanelImpl(this, errorPanelCallback, new RenderErrorPanelImpl.RenderFailureProvider() {
                public Exception getRenderFailureException() {
                    return JsfForm.this.getRenderFailureException();
                }
                public MarkupDesignBean getRenderFailureComponent() {
                    return JsfForm.this.getRenderFailureComponent();
                }
            });
        }
    }

    // XXX
    public interface ErrorPanel {
        public void updateErrors();
    } // End of  ErrorPanel.
    // XXX Hack for the impls. Ged rid of this.
    public interface ErrorPanelCallback {
        public void updateTopComponentForErrors();
        public void setRenderFailureShown(boolean shown);
//        public Exception getRenderFailure();
//        public MarkupDesignBean getRenderFailureComponent();
        public void handleRefresh(boolean showErrors);
    } // End of ErrorPanelCallback.

    
    public boolean isSourceDirty() {
//        return domProvider.isSourceDirty();
        MarkupUnit markupUnit = getFacesModel().getMarkupUnit();
        if (markupUnit != null) {
            return markupUnit.getState() == Unit.State.SOURCEDIRTY;
        } else {
            // XXX #6478973 Model could be corrupted, until #6480764 is fixed.
            log(new IllegalStateException("The FacesModel is corrupted, its markup unit is null, facesModel=" + getFacesModel())); // NOI18N
        }
        return false;
    }
    
    public void dumpHtmlMarkupForNode(org.openide.nodes.Node node) {
//        domProvider.dumpHtmlMarkupForNode(node);
        DesignBean designBean = node.getLookup().lookup(DesignBean.class);
        if (designBean instanceof MarkupDesignBean) {
            MarkupDesignBean markupDesignBean = (MarkupDesignBean)designBean;
            Element sourceElement = markupDesignBean.getElement();
            Element renderedElement = MarkupService.getRenderedElementForElement(sourceElement);
            if (renderedElement == null || sourceElement == renderedElement) {
                log("Markup design bean not renderable, markup design bean=" + markupDesignBean); // NOI18N
                dumpHtmlMarkupDesignBeanHtml();
                return;
            }
            log("Rendered markup design bean=" + markupDesignBean // NOI18N
                    + "\n" + Util.getHtmlStream(renderedElement)); // NOI18N
        } else {
            log("Design bean not renderable, design bean=" + designBean); // NOI18N
            dumpHtmlMarkupDesignBeanHtml();
        }
    }
    
    private void dumpHtmlMarkupDesignBeanHtml() {
        DocumentFragment df = getHtmlDomFragment();
        Element html = Util.findDescendant(HtmlTag.HTML.name, df);
        if (html == null) {
            html = getHtmlBody(false);
        }
        log("Rendered html element markup design bean=" + MarkupUnit.getMarkupDesignBeanForElement(html)
                + "\n" + Util.getHtmlStream(html)); // NOI18N
    }
   
    public DocumentFragment getHtmlDomFragment() {
//        return domProvider.getHtmlDocumentFragment();
        return getFacesModel().getHtmlDomFragment();
    }
    
    public Element getDefaultParentComponent() {
//        return domProvider.getDefaultParentComponent();
//        LiveUnit liveUnit = getFacesModel().getLiveUnit();
        LiveUnit liveUnit = getLiveUnit();
        if (liveUnit != null) {
//            MarkupBean bean = getFacesModel().getFacesUnit().getDefaultParent();
            MarkupBean bean = getFacesPageUnit().getDefaultParent();

            if (bean != null) {
                DesignBean designBean = liveUnit.getDesignBean(bean);
                Element defaultParentElement = designBean instanceof MarkupDesignBean
                        ? JsfSupportUtilities.getComponentRootElementForMarkupDesignBean((MarkupDesignBean)designBean)
                        : null;
//                return defaultParentElement == null ? findDefaultParentComponent() : defaultParentElement;
                return defaultParentElement;
            }
        }

        return null;
    }
    
//    private Element findDefaultParentComponent() {
//        Element bodyElement = getHtmlBody(false);
//        if (isFragment()) {
//            Element fSubViewElement = Util.findDescendant(HtmlTag.FSUBVIEW.name, bodyElement);
//            return fSubViewElement == null ? bodyElement : fSubViewElement;
//        } else {
//            return bodyElement;
//        }
//    }
    
    public Transferable copyComponents(Element[] componentRootElements) {
//        return domProvider.copyComponents(componentRootElements);
        List<DesignBean> beans = new ArrayList<DesignBean>();
        for (Element componentRootElement : componentRootElements) {
            DesignBean bean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
            if (bean != null) {
                beans.add(bean);
            }
        }
        LiveUnit liveUnit = getFacesModel().getLiveUnit();
        return liveUnit.copyBeans(beans.toArray(new DesignBean[beans.size()]));
    }
    
    public void deleteComponents(Element[] componentRootElements) {
        getDomDocumentImpl().deleteComponents(componentRootElements);
    }
    
//    public boolean canDropDesignBeansAtNode(DesignBean[] designBeans, Node node) {
//        DesignBean parent = null;
//        while (node != null) {
////            if (curr instanceof RaveElement) {
////                parent = ((RaveElement)curr).getDesignBean();
//            if (node instanceof Element) {
////                parent = InSyncService.getProvider().getMarkupDesignBeanForElement((Element)curr);
////                parent = WebForm.getDomProviderService().getMarkupDesignBeanForElement((Element)curr);
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

    
//    private static class DataObjectPropertyChangeListener implements PropertyChangeListener {
//        
//        private final JsfForm jsfForm;
//        
//        public DataObjectPropertyChangeListener(JsfForm jsfForm) {
//            this.jsfForm = jsfForm;
//        }
//        
//        public void propertyChange(final PropertyChangeEvent evt) {
//            // Immediately wipe out the paint box
//            if (evt.getPropertyName().equals(DataObject.PROP_PRIMARY_FILE)) {
////                if ((getPane() != null) && (getPane().getPaneUI() != null)) {
////                    getPane().getPaneUI().setPageBox(null);
////                }
//                
///*//NB6.0
//                // Reconfigure the data object: throw away the old model
//                // and find the new model associated with the new file object.
////                InSyncServiceProvider.get().doOutsideOfRefactoringSession(new Runnable() {
//                MdrInSyncSynchronizer.get().doOutsideOfRefactoringSession(new Runnable() {
//                    public void run() {
// */
//                // Do the stuff on UI thread as some stuff gets updated that requires to be on UI thread
//                SwingUtilities.invokeLater(new Runnable() {
//                    public void run() {
//                        jsfForm.replaceFacesModel((FileObject)evt.getOldValue(), (FileObject)evt.getNewValue());
//                    }
//                });
///*                  }
//                });
////*/
//            }
//        }
//    } // End of DataObjectPropertyChangeListener.
    

    private static class JsfDesignContextListener implements DesignContextListener {
        
        private final JsfForm jsfForm;
        
        public JsfDesignContextListener(JsfForm jsfForm) {
            this.jsfForm = jsfForm;
        }
        
        public void contextActivated(DesignContext designContext) {
            jsfForm.getDomSynchronizer().contextActivated(designContext);
//            jsfForm.designer.contextActivated(designContext);
//            jsfForm.fireDesignContextActivated(designContext);
        }
        
        public void contextDeactivated(DesignContext designContext) {
            jsfForm.getDomSynchronizer().contextDeactivated(designContext);
//            jsfForm.designer.contextDeactivated(designContext);
//            jsfForm.fireDesignContextDeactivated(designContext);
        }
        
        public void contextChanged(DesignContext designContext) {
            jsfForm.getDomSynchronizer().contextChanged(designContext);
//            jsfForm.designer.contextChanged(designContext);
            jsfForm.designContextChanged(designContext);
        }
        
        public void beanCreated(DesignBean designBean) {
            jsfForm.getDomSynchronizer().beanCreated(designBean);
//            jsfForm.designer.beanCreated(designBean);
//            jsfForm.fireDesignBeanCreated(designBean);
        }
        
        public void beanDeleted(DesignBean designBean) {
            jsfForm.getDomSynchronizer().beanDeleted(designBean);
//            jsfForm.designer.beanDeleted(designBean);
//            jsfForm.fireDesignBeanDeleted(designBean);
        }
        
        public void beanMoved(DesignBean designBean, DesignBean designBean0, Position position) {
            jsfForm.getDomSynchronizer().beanMoved(designBean, designBean0, position);
//            jsfForm.designer.beanMoved(designBean, designBean0, position);
//            jsfForm.fireDesignBeanMoved(designBean, designBean0, position);
        }
        
        public void beanContextActivated(DesignBean designBean) {
            jsfForm.getDomSynchronizer().beanContextActivated(designBean);
//            jsfForm.designer.beanContextActivated(designBean);
//            jsfForm.fireDesignBeanContextActivated(designBean);
        }
        
        public void beanContextDeactivated(DesignBean designBean) {
            jsfForm.getDomSynchronizer().beanContextDeactivated(designBean);
//            jsfForm.designer.beanContextDeactivated(designBean);
//            jsfForm.fireDesignBeanContextDeactivated(designBean);
        }
        
        public void instanceNameChanged(DesignBean designBean, String string) {
            jsfForm.getDomSynchronizer().instanceNameChanged(designBean, string);
//            jsfForm.designer.instanceNameChanged(designBean, string);
//            jsfForm.fireDesignBeanNameChanged(designBean, string);
        }
        
        public void beanChanged(DesignBean designBean) {
            jsfForm.getDomSynchronizer().beanChanged(designBean);
//            jsfForm.designer.beanChanged(designBean);
//            jsfForm.fireDesignBeanChanged(designBean);
        }
        
        public void propertyChanged(DesignProperty designProperty, Object object) {
            jsfForm.getDomSynchronizer().propertyChanged(designProperty, object);
//            jsfForm.designer.propertyChanged(designProperty, object);
//            jsfForm.fireDesignPropertyChanged(designProperty, object);
        }
        
        public void eventChanged(DesignEvent designEvent) {
            jsfForm.getDomSynchronizer().eventChanged(designEvent);
//            jsfForm.designer.eventChanged(designEvent);
//            jsfForm.fireDesignEventChanged(designEvent);
        }
    } // End of JsfDesignContextListener.
    
    
//    private static class CoordinateTranslatorImpl implements FacesDnDSupport.CoordinateTranslator {
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
    
    
//    private static class LocationImpl implements FacesDnDSupport.Location {
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
    

    public boolean hasCachedExternalFrames() {
        return externalDomProviderCache.size() > 0;
    }
    
    Designer[] getExternalDesigners(URL url) {
        JsfForm external = findExternalForm(url);
        
        if (external == null) {
            return new Designer[0];
        }

        // XXX Side-effect. Moved from the designer.
        if (!hasRecursiveContextJsfForm(external)) {
            external.setContextJsfForm(this);
        }
        
//        Designer[] designers = findDesigners(external);
//        if (designers.length == 0) {
//            Designer designer = createDesigner(external);
//            return new Designer[] {designer};
//        }
//        return designers;
        return getDesigners(external);
    }
    
    // XXX Copied/modified from designer/../ExternalDocumentBox.
    private JsfForm findExternalForm(URL url) {
//        DocumentCache cache = webform.getDocument().getFrameBoxCache();
//        DocumentCache cache = webform.getFrameBoxCache();
        ExternalDomProviderCache cache = externalDomProviderCache;

        JsfForm frameForm = cache.get(url);
        if (frameForm != null) {
            return frameForm;
        }

        // According to HTML4.01 section 16.5: "The contents of the
        // IFRAME element, on the other hand, should only be displayed
        // by user agents that do not support frames or are configured
        // not to display frames."
        // Thus, we don't walk the children array; instead, we
        // fetch the url document and display that instead
        if (url == null) {
            return null;
        }

        FileObject fo = URLMapper.findFileObject(url);

        if (fo != null) {
            frameForm = loadPage(fo);
        }

        if (frameForm == null) {
            frameForm = loadPage(url);
        }

//        if ((frameForm != null) && (frameForm != WebForm.EXTERNAL)) {
        if (frameForm != null) {
            cache.put(url, frameForm);
        }

//        // Set the cell renderer pane if necessary
//        if ((frameForm != null) && (frameForm.getRenderPane() == null)) {
//            frameForm.setRenderPane(webform.getRenderPane());
//        }

        return frameForm;
    }

    private static JsfForm loadPage(URL url) {
        //Log.err.log("URL box loading not yet implemented");
//        return WebForm.EXTERNAL;
        return null;

//        /*
//        // Compute document base for the other document
//        //        try {
//        //            url = new URL(getBase(), href);
//        //        } catch (MalformedURLException mfe) {
//        //            try {
//        //                ErrorManager.getDefault().notify(mfe);
//        //                url = new URL(href);
//        //            } catch (MalformedURLException mfe2) {
//        //                ErrorManager.getDefault().notify(mfe);
//        //                url = null;
//        //            }
//        //        }
//        //        if (url != null) {
//        StringBuffer sb = new StringBuffer();
//        try {
//            InputStream uis = url.openStream();
//            Reader r = new BufferedReader(new InputStreamReader(uis));
//            int c;
//            while ((c = r.read()) != -1) {
//                sb.append((char)c);
//            }
//        } catch (IOException ioe) {
//            ErrorManager.getDefault().notify(ioe);
//            return false;
//        }
//        String str = sb.toString();
//
//        // Construct a document containing the string buffer
//        StringContent content = new StringContent(str.length()+5);
//        try {
//            content.insertString(0, str);
//        } catch (Exception e) {
//            ErrorManager.getDefault().notify(e);
//            return false;
//        }
//        AbstractDocument adoc = new PlainDocument(content);
//        DataObject dobj = null;
//        String filename = url.toString(); // only used for diagnostic messages, right?
//
//        MarkupUnit markup = new MarkupUnit(dobj, adoc, filename, MarkupUnit.ALLOW_XML);
//        markup.sync();
//        //if (!markup.getState().equals(markup.getState().CLEAN)) {
//        if (!markup.getState().equals(Unit.State.CLEAN)) {
//            return false;
//        }
//
//        CellRendererPane renderPane = webform.getPane().getRenderPane();
//        Log.err.log("FrameBox initialization for external urls not yet done");
//        */
//        /* XXX Not yet implemented
//        frameForm = new WebForm(markup, renderPane);
//        DesignerPane pane = null;
//        Document document = new Document(frameForm);
//        frameForm.setDocument(document);
//        return success;
//        */
    }

    private static JsfForm loadPage(FileObject fobj) {
        DataObject dobj = null;

        try {
            dobj = DataObject.find(fobj);
        } catch (DataObjectNotFoundException ex) {
            return null;
        }

        /*
        // Wrapper which handles errors
        LiveFacesCookie c = LiveFacesCookie.getInstanceFor(dobj);
        if (c == null) {
            ErrorManager.getDefault().log("Data object " + dobj + " ain't got no insync cookie!");
            return false;
        }
        FacesModel model = getDocument().getWebForm().getModel();
        model.syncFromDoc();
        if (model.getMarkup().getState().isInvalid()) {
            return false;
        }
        markup = model.getMarkup();
         */

        // XXX Does this work for a form which is not yet open?
//        WebForm frameForm = WebForm.findWebForm(dobj);
//        WebForm frameForm = WebForm.getDesignersWebFormForDataObject(dobj);
        JsfForm frameForm = getJsfForm(dobj);

//        if ((frameForm != null) && (frameForm.getModel() != null)) {
//            frameForm.getModel().sync();
        if (frameForm != null) {
            frameForm.syncModel();
        }

        return frameForm;
    }

    // XXX Moved from designer/../WebForm.
    /** Get the context page for this fragment. This method should only return non-null
     * for page fragments. The context page is a page which provides a "style context" for
     * the fragment. Typically, the page is one of the pages which includes the page fragment,
     * but that's not strictly necessary. The key thing is that the page fragment will pick
     * up stylesheets etc. defined in the head of the context page.
     * @return A context page for the fragment
     */
    JsfForm getContextJsfForm() {
//        if (isFragment && (contextPage == null)) {
        if (isFragment() && (contextJsfForm == null)) {
            // Find a page
            Iterator it =
//                DesignerService.getDefault().getWebPages(getProject(), true, false).iterator();
//                    InSyncService.getProvider().getWebPages(getProject(), true, false).iterator();
//                    domProvider.getWebPageFileObjectsInThisProject().iterator();
                    Util.getWebPages(getFacesModel().getProject(), true, false).iterator();

            while (it.hasNext()) {
                FileObject fo = (FileObject)it.next();

                try {
                    DataObject dobj = DataObject.find(fo);

                    // XXX Very suspicious, how come that context page is any random page
                    // whitin project?? What actually the context page is good for?
                    // It seems it is a wrong architecture.
//                    if (isWebFormDataObject(dobj)) {
                    if (JsfSupportUtilities.isWebFormDataObject(dobj)) {
//                        contextJsfForm = getWebFormForDataObject(dobj);
                        contextJsfForm = getJsfForm(dobj);
                        break;
                    }
                } catch (DataObjectNotFoundException ex) {
                    log(ex);
                }
            }
        }

        return contextJsfForm;
    }
    
    // XXX Moved from designer/../WebForm.
    /** Set the associated context page for this page fragment. (Only allowed on
     * page fragments.)
     *  @see getContextPage()
     */
    private void setContextJsfForm(JsfForm contextJsfForm) {
//        assert isFragment;
        if (!isFragment()) {
            return;
        }

        // XXX Context page notion from fragment should be removed.
        if (this.contextJsfForm != contextJsfForm) {
            // Force refresh such that the style links are recomputed
            clearHtml();
        }

        this.contextJsfForm = contextJsfForm;
    }
    
    private boolean hasRecursiveContextJsfForm(JsfForm contextJsfForm) {
        if (contextJsfForm == null) {
            return false;
        }
        JsfForm jsf = this;
        while (jsf != null) {
            JsfForm context = jsf.getContextJsfForm();
            if (context == contextJsfForm) {
                return true;
            }
            jsf = context;
        }
        return false;
    }

    
    public boolean isBraveheartPage() {
        return Util.isBraveheartPage(getJspDom());
    }

    public boolean isWoodstockPage() {
        return Util.isWoodstockPage(getJspDom());
    }

    public void setRenderFailureShown(boolean shown) {
        renderFailureShown = shown;
    }
    
    public boolean isRenderFailureShown() {
        return renderFailureShown;
    }
    
    public boolean editEventHandlerForComponent(Element componentRootElement) {
        DesignBean designBean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
        if (designBean == null) {
//            webform.getModel().openDefaultHandler(component);
            getFacesModel().openDefaultHandler();
            return false;
        } else {
            // See if it's an XHTML element; if so just show it in
            // the JSP source
//            if (FacesSupport.isXhtmlComponent(component)) {
            if (isXhtmlComponent(designBean)) {
//                MarkupBean mb = FacesSupport.getMarkupBean(component);
                MarkupBean mb = Util.getMarkupBean(designBean);
                
//                MarkupUnit unit = webform.getMarkup();
                MarkupUnit unit = getFacesModel().getMarkupUnit();
                // <markup_separation>
//                Util.show(null, unit.getFileObject(),
//                    unit.computeLine((RaveElement)mb.getElement()), 0, true);
                // ====
//                MarkupService.show(unit.getFileObject(), unit.computeLine((RaveElement)mb.getElement()), 0, true);
                showLineAt(unit.getFileObject(), unit.computeLine(mb.getElement()), 0);
                // </markup_separation>
            } else {
//                webform.getModel().openDefaultHandler(component);
                getFacesModel().openDefaultHandler(designBean);
            }

            return true;
        }
    }
    
    /** Return true iff the given DesignBean is an XHTML markup "component" */
    private static boolean isXhtmlComponent(DesignBean bean) {
//        MarkupBean mb = FacesSupport.getMarkupBean(bean);
        MarkupBean mb = Util.getMarkupBean(bean);

        return (mb != null) && !(mb instanceof FacesBean);
    }

    // XXX Copied from DesignerActions.
    private static void showLineAt(FileObject fo, int lineno, int column) {
        DataObject dobj;
        try {
            dobj = DataObject.find(fo);
        } catch (DataObjectNotFoundException ex) {
            log(ex);
            return;
        }

        // Try to open doc before showing the line. This SHOULD not be
        // necessary, except without this the IDE hangs in its attempt
        // to open the file when the file in question is a CSS file.
        // Probably a bug in the xml/css module's editorsupport code.
        // This has the negative effect of first flashing the top
        // of the file before showing the destination line, so
        // this operation is made conditional so only clients who
        // actually need it need to use it.
        EditorCookie ec = (EditorCookie)dobj.getCookie(EditorCookie.class);
        if (ec != null) {
            try {
                ec.openDocument(); // ensure that it has been opened - REDUNDANT?
                //ec.open();
            } catch (IOException ex) {
                log(ex);
                return;
            }
        }

        LineCookie lc = (LineCookie)dobj.getCookie(LineCookie.class);
        if (lc != null) {
            Line.Set ls = lc.getLineSet();
            if (ls != null) {
                // -1: convert line numbers to be zero-based
                Line line = ls.getCurrent(lineno-1);
                // TODO - pass in a column too?
                line.show(Line.SHOW_GOTO, column);
            }
        }
    }

    private Designer[] getDesigners() {
        Set<Designer> ds;
//        synchronized (designers) {
        designersLock.readLock().lock();
        try {
            ds = new HashSet<Designer>(designers);
        } finally {
            designersLock.readLock().unlock();
        }
        return ds.toArray(new Designer[ds.size()]);
    }

    private void addDesigner(Designer designer) {
//        synchronized (designers) {
        designersLock.writeLock().lock();
        try {
            designers.add(designer);
        } finally {
            designersLock.writeLock().unlock();
        }
    }
    
    public boolean isRenderedNode(Node node) {
        if (node == null) {
            return false;
        }
        
        return node.getOwnerDocument() == getHtmlDom();
    }

    public void setVirtualFormsSupportEnabled(boolean enabled) {
        virtualFormsSupportEnabled = enabled;
        
        // TODO fire model event and in handler repaint the component.
        tcRepaint();
    }
    
    public boolean isVirtualFormsSupportEnabled() {
        return virtualFormsSupportEnabled;
    }
    
    public void setAjaxTransactionsSupportEnabled(boolean enabled) {
        ajaxTransactionsSupportEnabled = enabled;
        
        // TODO fire model event and in handler repaint the component.
        tcRepaint();
    }
    
    public boolean isAjaxTransactionsSupportEnabled() {
        return ajaxTransactionsSupportEnabled;
    }

    public void tcRepaint() {
        Designer[] designers = findDesigners(this);
        
        for (Designer designer : designers) {
            JsfMultiViewElement jsfMultiViewElement = findJsfMultiViewElementForDesigner(designer);
            if (jsfMultiViewElement == null) {
                continue;
            }
            jsfMultiViewElement.getJsfTopComponent().repaint();
        }
    }
    

    /** XXX #101837 Closing the component when facesModel goes away. */
    private static class JsfDesignProjectListener implements DesignProjectListener {
        public void contextOpened(DesignContext designContext) {
            // No op.
        }

        public void contextClosed(DesignContext designContext) {
            JsfForm jsfForm = JsfForm.findJsfForm(designContext);
            JsfMultiViewElement[] jsfMultiViewElements = findJsfMultiViewElements(jsfForm);
            for (JsfMultiViewElement jsfMultiViewElement : jsfMultiViewElements) {
                jsfMultiViewElement.closeMultiView();
            }
        }
    } // End of DesignProjectListener.

    DocumentFragment renderMarkupDesignBean(MarkupDesignBean markupDesignBean) {
        return renderMarkupDesignBean(markupDesignBean, true);
    }
    
    DocumentFragment renderMarkupDesignBean(MarkupDesignBean markupDesignBean, boolean markRendered) {
        return FacesPageUnit.renderHtml(getFacesModel(), markupDesignBean, markRendered);
    }
    
    LiveUnit getLiveUnit() {
        return getFacesModel().getLiveUnit();
    }
    
    Project getProject() {
        return getFacesModel().getProject();
    }
    
    FileObject getMarkupFile() {
        return getFacesModel().getMarkupFile();
    }
    
    FacesPageUnit getFacesPageUnit() {
        return getFacesModel().getFacesUnit();
    }
    
    void customizeCreation(DesignBean[] designBeans) {
        Util.customizeCreation(designBeans, getFacesModel());
    }
    
    void designBeanCreated(DesignBean designBean) {
        getFacesModel().beanCreated(designBean);
    }
    
    public void handleResult(Result result) {
        ResultHandler.handleResult(result, getFacesModel());
    }
    
    void linkDesignBeans(DesignBean droppee, DesignBean lb) {
        getFacesModel().linkBeans(droppee, lb);
    }
    
    DesignBean findParent(String className, DesignBean droppee, Node parentNode, boolean searchUp) {
        return Util.findParent(className, droppee, parentNode, searchUp, getFacesModel());
    }
    
    boolean isFormDesignBean(DesignBean designBean) {
        return Util.isFormBean(getFacesModel(), designBean);
    }
    
    MarkupUnit getMarkupUnit() {
        return getFacesModel().getMarkupUnit();
    }
    
    public org.openide.nodes.Node getRootBeanNode() {
        FacesModel facesModel = getFacesModel();
        DesignBean rootBean = facesModel.getRootBean();
        if (rootBean == null) {
            // XXX If the model is busted then it is supposed to be OK, there is an error, see e.g. #6478860.
            if (!facesModel.isBusted()) {
                log(new IllegalStateException("Invalid FacesModel, it is not busted and its root design bean is null, facesModel=" + facesModel)); // NOI18N
            }
            return null;
        } else {
            return DesigntimeIdeBridgeProvider.getDefault().getNodeRepresentation(rootBean);
        }
    }
    
    
    private static void log(Throwable ex) {
        Logger logger = getLogger();
        logger.log(Level.INFO, null, ex);
    }
    
    private static void log(String message) {
        Logger logger = getLogger();
        logger.log(Level.INFO, message);
    }
    
    private static void notify(Throwable ex) {
        Logger logger = getLogger();
        logger.log(Level.SEVERE, null, ex);
    }
    
    private static Logger getLogger() {
        return Logger.getLogger(JsfForm.class.getName());
    }
    
    
    public boolean isValid() {
        return getFacesModel() != null;
    }

    private void loadFacesModel(final DataObject dataObject) {
        FacesModel facesModel;
        try {
            facesModel = getFacesModel(dataObject);
        // XXX FacesModel throws runtime exceptions, which is wrong.    
        } catch (Exception ex) {
            loadingFailed(new IllegalStateException("FacesModel was not loaded for DataObject, dataObject=" + dataObject, ex));
            return;
        }
        if (facesModel == null) {
            loadingFailed(new NullPointerException("No FacesModel for DataObject, dataObject=" + dataObject));
            return;
        }
        
        setFacesModel(facesModel);
        
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                init();
                notifyViewsModelLoaded();
            }
        });
    }
    
    private void loadingFailed(Exception ex) {
        notify(ex);
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                notifyViewsModelLoadingFailed();
            }
        });
    }
    
    private void notifyViewsModelLoaded() {
        JsfMultiViewElement[] jsfMultiViewElements = findJsfMultiViewElements(this);
        for (JsfMultiViewElement jsfMultiViewElement : jsfMultiViewElements) {
            jsfMultiViewElement.modelLoaded();
        }
    }
    
    private void notifyViewsModelLoadingFailed() {
        JsfMultiViewElement[] jsfMultiViewElements = findJsfMultiViewElements(this);
        for (JsfMultiViewElement jsfMultiViewElement : jsfMultiViewElements) {
            jsfMultiViewElement.closeMultiView();
        }
    }
    
}

