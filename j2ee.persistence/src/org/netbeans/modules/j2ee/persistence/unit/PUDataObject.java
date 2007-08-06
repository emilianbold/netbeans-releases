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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.persistence.unit;

import java.awt.Image;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceMetadata;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.openide.DialogDisplayer;
import org.netbeans.api.xml.cookies.CheckXMLCookie;
import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.modules.schema2beans.Schema2BeansException;
import org.netbeans.modules.xml.multiview.DesignMultiViewDesc;
import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;
import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.netbeans.spi.xml.cookies.ValidateXMLSupport;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Multiview data object for persistence.xml.
 *
 * @author Martin Adamek
 * @author Erno Mononen
 */

public class PUDataObject extends XmlMultiViewDataObject {
    
    public static final String HELP_ID_DESIGN_PERSISTENCE_UNIT
            = "persistence_multiview_design_persistenceUnitNode";  // NO18N
    private ModelSynchronizer modelSynchronizer;
    /**
     * Update delay for model synchronizer.
     */
    public static final int UPDATE_DELAY = 200;
    private static final int TYPE_TOOLBAR = 0;
    private Persistence persistence;
    private static final String DESIGN_VIEW_ID = "persistence_multiview_design"; // NO18N
    
    /**
     * Creates a new instance of PUDataObject.
     */
    public PUDataObject(FileObject pf, PUDataLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        modelSynchronizer = new ModelSynchronizer(this);
        org.xml.sax.InputSource in = DataObjectAdapters.inputSource(this);
        CheckXMLCookie checkCookie = new CheckXMLSupport(in);
        getCookieSet().add(checkCookie);
        ValidateXMLCookie validateCookie = new ValidateXMLSupport(in);
        getCookieSet().add(validateCookie);
        parseDocument();
    }
    
    protected Node createNodeDelegate() {
        return new PUDataNode(this);
    }
    
    /**
     * Saves the document.
     * @see EditorCookie#saveDocument
     */
    public void save(){
        EditorCookie edit = (EditorCookie) getCookie(EditorCookie.class);
        if (edit != null){
            try {
                edit.saveDocument();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    /**
     * Parses the document.
     * @return true if document could be parsed (it was valid), false otwherwise.
     */
    public boolean parseDocument() {
        if (persistence==null) {
            try {
                persistence = getPersistence();
            } catch (RuntimeException ex) { // must catch RTE (thrown by schema2beans when document is not valid)
                Logger.getLogger("global").log(Level.INFO, null, ex);
                return false;
            }
        } else {
            try{
                java.io.InputStream is = getEditorSupport().getInputStream();
                Persistence newPersistence = null;
                try {
                    newPersistence = Persistence.createGraph(is, true);
                } catch (RuntimeException ex) { // must catch RTE (thrown by schema2beans when document is not valid)
                    Logger.getLogger("global").log(Level.INFO, null, ex);
                    return false;
                }
                if (newPersistence!=null) {
                    persistence.merge(newPersistence, org.netbeans.modules.schema2beans.BaseBean.MERGE_UPDATE);
                }
            } catch (IOException ioe){
                Logger.getLogger("global").log(Level.INFO, null, ioe);
                return false;
            }
        }
        return true;
    }
    
    
    /**
     * Checks whether the preferred view can be displayed and switches to the
     * xml view and displays an appropriate warning if not. In case that
     * the preferred view is the design view, it
     * can be displayed if <ol><li>document is valid (parseable) and</li>
     *<li>the target server is attached></li></ol>.
     *@return true if the preferred view can be displayed, false otherwise.
     */
    public boolean viewCanBeDisplayed() {
        
        boolean switchView = false;
        NotifyDescriptor nd = null;
        
        if (!parseDocument() && getSelectedPerspective().preferredID().startsWith(DESIGN_VIEW_ID)) {
            nd = new org.openide.NotifyDescriptor.Message(
                    NbBundle.getMessage(PUDataObject.class, "TXT_DocumentUnparsable",
                    getPrimaryFile().getNameExt()), NotifyDescriptor.WARNING_MESSAGE);
            switchView = true;
            
        } else if (!ProviderUtil.isValidServerInstanceOrNone(FileOwnerQuery.getOwner(getPrimaryFile()))
        && getSelectedPerspective().preferredID().startsWith(DESIGN_VIEW_ID)){
            
            nd = new org.openide.NotifyDescriptor.Message(
                    NbBundle.getMessage(PUDataObject.class, "TXT_ServerMissing"),
                    NotifyDescriptor.WARNING_MESSAGE);
            switchView = true;
        }
        
        if (switchView){
            DialogDisplayer.getDefault().notify(nd);
            // postpone the "Switch to XML View" action to the end of event dispatching thread
            // this enables to finish the current action first (e.g. painting particular view)
            // see the issue 67580
            SwingUtilities.invokeLater(new Runnable(){
                public void run() {
                    goToXmlView();
                }
            });
        }
        return !switchView;

    }
    
    
    /**
     * Gets the object graph representing the contents of the 
     * persistence.xml deployment desciptor with which this data object 
     * is associated.
     *
     * @return the persistence graph.
     *
     */
    public Persistence getPersistence(){
        if (persistence==null) {
            try {
                persistence = PersistenceMetadata.getDefault().getRoot(getPrimaryFile());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        assert persistence != null;
        return persistence;
    }
    
    /**
     * Adds given persistence unit and schedules update of data.
     */
    public void addPersistenceUnit(PersistenceUnit persistenceUnit){
        ProviderUtil.makePortableIfPossible(FileOwnerQuery.getOwner(getPrimaryFile()), persistenceUnit);
        getPersistence().addPersistenceUnit(persistenceUnit);
        modelUpdatedFromUI();
    }
    
    /**
     * Removes given persistence unit and schedules update of data.
     */
    public void removePersistenceUnit(PersistenceUnit persistenceUnit){
        getPersistence().removePersistenceUnit(persistenceUnit);
        modelUpdatedFromUI();
    }
    
    /**
     * Adds given clazz to the list of given persistence unit's managed
     * classes and schedules update of data.
     * @param persistenceUnit
     * @param clazz fully qualified name of the class to be added.
     * @return true if given class was added, false otherwise (for example when
     * it was already added).
     */
    public boolean addClass(PersistenceUnit persistenceUnit, String clazz){
        String[] existing = persistenceUnit.getClass2();
        for (int i = 0; i < existing.length; i++) {
            if (clazz.equals(existing[i])){
                return false;
            }
        }
        persistenceUnit.addClass2(clazz);
        modelUpdatedFromUI();
        return true;
    }
    
    /**
     * Removes given class from the list of given persistence unit's managed
     * classes and schedules update of data.
     * @param persistenceUnit
     * @param clazz fully qualified name of the class to be removed.
     */
    public void removeClass(PersistenceUnit persistenceUnit, String clazz){
        persistenceUnit.removeClass2(clazz);
        
        modelUpdatedFromUI();
    }
    
    
    
    
    protected DesignMultiViewDesc[] getMultiViewDesc() {
        return new DesignMultiViewDesc[]{new DesignView(this,TYPE_TOOLBAR)};
    }
    
    private static class DesignView extends DesignMultiViewDesc {
        
        private static final long serialVersionUID = 1L;
        private int type;
        
        DesignView(PUDataObject dObj, int type) {
            super(dObj, NbBundle.getMessage(PUDataObject.class, "LBL_Design"));
            this.type=type;
        }
        
        public org.netbeans.core.spi.multiview.MultiViewElement createElement() {
            PUDataObject dObj = (PUDataObject)getDataObject();
            return new PersistenceToolBarMVElement(dObj);
        }
        
        public java.awt.Image getIcon() {
            return org.openide.util.Utilities.loadImage("org/netbeans/modules/j2ee/persistence/unit/PersistenceIcon.gif"); //NOI18N
        }
        
        public String preferredID() {
            return DESIGN_VIEW_ID + String.valueOf(type);
        }
        
        public HelpCtx getHelpCtx() {
            return new HelpCtx(HELP_ID_DESIGN_PERSISTENCE_UNIT); //NOI18N
        }
        
    }
    
    public void showElement(Object element) {
        Object target=null;
        if (element instanceof PersistenceUnit) {
            openView(0);
            target=element;
        }
        if (target!=null) {
            final Object key=target;
            org.netbeans.modules.xml.multiview.Utils.runInAwtDispatchThread(new Runnable() {
                public void run() {
                    getActiveMultiViewElement0().getSectionView().openPanel(key);
                }
            });
        }
    }
    
    protected String getPrefixMark() {
        return null;
    }
    
    /** Enable to get active MultiViewElement object
     */
    public ToolBarMultiViewElement getActiveMultiViewElement0() {
        return (ToolBarMultiViewElement)super.getActiveMultiViewElement();
    }
    
    public void modelUpdatedFromUI() {
        setModified(true);
        modelSynchronizer.requestUpdateData();
    }
    
    public void updateDataFromModel(FileLock lock) throws IOException{
        modelSynchronizer.updateDataFromModel(getPersistence(), lock, true);
    }
    
    public boolean isDeleteAllowed() {
        return true;
    }
    
    public boolean isCopyAllowed() {
        return true;
    }
    
    public boolean isMoveAllowed(){
        return true;
    }
    
    protected Image getXmlViewIcon() {
        return org.openide.util.Utilities.loadImage("org/netbeans/modules/j2ee/persistence/unit/PersistenceIcon.gif"); //NOI18N
    }
    
    private class ModelSynchronizer extends XmlMultiViewDataSynchronizer {
        
        public ModelSynchronizer(XmlMultiViewDataObject dataObject) {
            super(dataObject, UPDATE_DELAY);
        }
        
        protected boolean mayUpdateData(boolean allowDialog) {
            return true;
        }
        
        protected void updateDataFromModel(Object model, FileLock lock, boolean modify) {
            if (model == null) {
                return;
            }
            try {
                Writer out = new StringWriter();
                ((Persistence) model).write(out);
                out.close();
                getDataCache().setData(lock, out.toString(), modify);
            } catch (IOException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            } catch (Schema2BeansException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            } finally {
                if (lock != null){
                    lock.releaseLock();
                }
            }
        }
        
        protected Object getModel() {
            return getPersistence();
        }
        
        protected void reloadModelFromData() {
            parseDocument();
        }
        
    }
}
