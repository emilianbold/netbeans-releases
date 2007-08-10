/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.etl.ui;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.openide.util.Lookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.netbeans.modules.etl.model.ETLDefinition;
import org.netbeans.modules.etl.ui.model.impl.ETLCollaborationModel;
import org.netbeans.modules.etl.ui.view.ETLCollaborationTopComponent;
import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.xml.sax.InputSource;

import com.sun.org.apache.xerces.internal.util.XMLChar;
import org.netbeans.modules.etl.ui.view.ETLEditorTopView;

/**
 * Represents a ETL file.
 *
 * @author  radval
 */
public class ETLDataObject extends MultiDataObject {
    
    public ETLDataObject(FileObject fObj, MultiFileLoader loader)
    throws DataObjectExistsException {
        super(fObj, loader);
        CookieSet set = getCookieSet();
        
        editorSupport = new ETLEditorSupport(this);
        // editor support defines MIME type understood by EditorKits registry
        set.add(editorSupport);
        
        // Add check and validate cookies
        InputSource is = DataObjectAdapters.inputSource(this);
        set.add(new CheckXMLSupport(is));
        try {
            mModel = new ETLCollaborationModel(this);
        } catch(Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        // add validate action here
    }
    
    protected Node createNodeDelegate() {
        if(this.mNode == null) {
            this.mNode = new ETLNode(this);
        }
        return this.mNode;
    }
       
    /**
     * subclasses should look updateServices() and additionalInitialLookup()
     */
    public final Lookup getLookup() {
        if (myLookup.get() == null) {
            
            Lookup lookup;
            List<Lookup> list = new LinkedList<Lookup>();
            
            list.add(Lookups.fixed( new Object[]{this}));
            lookup = new ProxyLookup(list.toArray(new Lookup[list.size()]));
            myLookup.compareAndSet(null, lookup);
            isLookupInit.compareAndSet( false, true );
        }
        return myLookup.get();
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // If you add context help, change to:
        // return new HelpCtx (MyDataObject.class);
    }
    
    @Override
    protected FileObject handleRename(String name) throws IOException {
        ETLEditorSupport weSupport = getETLEditorSupport();
        String oldName = this.getName();
        weSupport.updateTitles();
        //Do we need to change the name attribute of the ETL def?
        
        //logic to keep the status of the save badge (*) intact.
        boolean modified = weSupport.getEnv().isModified();
        if (modified) {
            weSupport.getEnv().unmarkModified();
        }
        FileObject fo =  super.handleRename(name);
        if (modified) {
            weSupport.getEnv().markModified();
        }
        firePropertyChange(DataObject.PROP_NAME, oldName, name);
        return fo;
    }
    
    protected void handleDelete() throws IOException {
        //this is work around when file is modified in editor and
        //editor has lock
        getETLEditorSupport().getEnv().unmarkModified();
        
        super.handleDelete();
    }
    
    protected DataObject handleCreateFromTemplate(DataFolder df, String name) throws IOException {
        ETLDataObject dataObject = (ETLDataObject) super.handleCreateFromTemplate(df, name);
        String doName = dataObject.getName();
        //make sure the the name is a valid NMTOKEN.
        if (!XMLChar.isValidNmtoken(doName)) {
            return dataObject;
        }
        
        SaveCookie sCookie = (SaveCookie) dataObject.getCookie(SaveCookie.class);
        if(sCookie != null) {
            sCookie.save();
        }
        return dataObject;
    }
    
    public void initialize(WizardDescriptor descriptor) {
        try {
            DataObjectHelper dHelper = new DataObjectHelper(this);
            dHelper.initializeETLDataObject(descriptor, this, getETLEditorSupport());
            // the first time data object is created, it cannot be got through lookup.
            // find a better way to do this.
            DataObjectProvider.activeDataObject = this;
        } catch(Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    @Override
    public void setModified(boolean modified) {
        super.setModified(modified);
        if (modified) {
            getCookieSet().add(getSaveCookie());
        } else {
            getCookieSet().remove(getSaveCookie());
        }
    }
    
    private SaveCookie getSaveCookie() {
        return new SaveCookie() {
            public void save() throws IOException {
                getETLEditorSupport().synchDocument();
                getETLEditorSupport().saveDocument();
            }
            
            @Override
            public int hashCode() {
                return getClass().hashCode();
            }
            
            @Override
            public boolean equals(Object other) {
                return getClass().equals(other.getClass());
            }
        };
    }
    
   public ETLEditorTopView getEditorView() {
        if(this.view == null) {
            view = new ETLEditorTopView(getModel());
        }
        return this.view;
    }
    
    public ETLEditorSupport getETLEditorSupport() {
        return editorSupport;
    }
    
    public ETLDefinition getETLDefinition() throws Exception {
        return this.mModel.getETLDefinition();
    }
    
    public ETLCollaborationModel getModel() {
        return this.mModel;
    }
    
    public ETLCollaborationTopComponent getETLEditorTC() throws Exception {
        if(this.mTC == null) {
            this.mTC = new ETLCollaborationTopComponent(this);
        }
        return this.mTC;
    }
    
    private static final long serialVersionUID = 6338889116068357651L;
    private transient ETLEditorSupport editorSupport;
    private ETLEditorTopView view;
    private transient ETLCollaborationTopComponent mTC;
    private transient ETLCollaborationModel mModel;
    private transient AtomicReference<Lookup> myLookup =
            new AtomicReference<Lookup>();    
    private transient AtomicReference<InstanceContent> myServices =
            new AtomicReference<InstanceContent>();
    private transient AtomicBoolean isLookupInit = new AtomicBoolean(false);            
    private Node mNode = null;
    public static final String ETL_ICON = "org/netbeans/modules/etl/ui/resources/images/ETLDefinition.png";
}

