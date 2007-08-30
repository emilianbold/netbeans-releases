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


package org.netbeans.modules.visualweb.project.jsfloader;



import org.netbeans.modules.visualweb.palette.api.CodeClipPaletteActions;
import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.ref.WeakReference;
import java.util.Enumeration;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import javax.swing.text.BadLocationException;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;
import org.openide.DialogDisplayer;

import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditor;
import org.openide.text.DataEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;
import org.openide.windows.Workspace;

import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;

import org.netbeans.modules.visualweb.api.insync.InSyncService;
import org.netbeans.modules.visualweb.palette.api.CodeClipDragAndDropHandler;

/**
 * Editor support for JSF JSP data objects.
 *
 * @author  Peter Zavadsky
 * @author  Tor Norbye (part copied from formerly changed HTMLEditorSupport + strange designer stuff)
 */
public final class JsfJspEditorSupport extends DataEditorSupport
        implements OpenCookie, EditCookie, EditorCookie.Observable, PrintCookie {

    private String encoding;
    private static final String defaultEncoding = "UTF-8"; // NOI18N
    
    /** SaveCookie for this support instance. The cookie is adding/removing
     * data object's cookie set depending on if modification flag was set/unset. */
    private final SaveCookie saveCookie = new SaveCookie() {
        /** Implements <code>SaveCookie</code> interface. */
        public void save() throws IOException {
            JsfJspEditorSupport.this.saveDocument();
            JsfJspEditorSupport.this.getDataObject().setModified(false);
        }
    };


    /** Constructor. */
    JsfJspEditorSupport(JsfJspDataObject obj) {
        super(obj, new Environment(obj));
        encoding = null;
        setMIMEType("text/x-jsp"); // NOI18N
    }


    public void openDesigner() {
        JsfJavaEditorSupport jsfJavaEditorSupport = getJsfJavaEditorSupport(false);
        if (jsfJavaEditorSupport == null) {
            // XXX See #6438557
            return;
        }
        jsfJavaEditorSupport.openDesigner();
    }


    // XXX Making it accessible within this package.
    protected boolean canClose() {
        return super.canClose();
    }

    // XXX Making it accessible within this package.
    protected void notifyClosed() {
        super.notifyClosed();
        JsfJspDataObject dataObject = (JsfJspDataObject) getDataObject();
//        org.netbeans.modules.visualweb.api.designerapi.DesignerServiceHack.getDefault().destroyWebFormForFileObject(dataObject.getPrimaryFile());
    }

    /**
     * Overrides superclass method. Adds adding of save cookie if the document has been marked modified.
     * @return true if the environment accepted being marked as modified
     *    or false if it has refused and the document should remain unmodified
     */
    protected boolean notifyModified() {
        if(!super.notifyModified()) {
            return false;
        }
        
        addSaveCookie();
        
        updateMultiViewDisplayName();
        
        return true;
    }
    
    /** Overrides superclass method. Adds removing of save cookie. */
    protected void notifyUnmodified() {
        super.notifyUnmodified();
        
        removeSaveCookie();
        
        updateMultiViewDisplayName();
    }
    
    /** Helper method. Adds save cookie to the data object. */
    private void addSaveCookie() {
        JsfJspDataObject obj = (JsfJspDataObject)getDataObject();
        
        // XXX Adds save cookie to the data object.
        if(obj.getPureCookie(SaveCookie.class) == null) {
            obj.getCookieSet0().add(saveCookie);
            obj.setModified(true);
        }
    }
    
    /** Helper method. Removes save cookie from the data object. */
    private void removeSaveCookie() {
        JsfJspDataObject obj = (JsfJspDataObject)getDataObject();
        
        // We must use getPureCookie here to make sure we do not get the CompoundCookie
        Node.Cookie cookie = obj.getPureCookie(SaveCookie.class);
        
        if(cookie != null && cookie.equals(saveCookie)) {
            obj.getCookieSet0().remove(saveCookie);
            obj.setModified(false);
        }
    }
    
    @Override
    public void saveDocument() throws IOException {
        saveDocument(true);
    }
    
    /** Save the document in this thread.
     * @param parse true if the parser should be started, otherwise false
     * @param forceSave if true save always, otherwise only when is modified
     * @exception IOException on I/O error
     */
    private void saveDocument(boolean forceSave) throws IOException {
        if (forceSave || isModified()) {
            ((JsfJspDataObject)getDataObject()).updateFileEncoding(true);
            
            encoding = ((JsfJspDataObject)getDataObject()).getFileEncoding();

            if (!isSupportedEncoding(encoding)){
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                NbBundle.getMessage (JsfJspEditorSupport.class, "MSG_BadEncodingDuringSave", //NOI18N
                    new Object [] { getDataObject().getPrimaryFile().getNameExt(),
                                    encoding, 
                                    defaultEncoding} ), 
                NotifyDescriptor.YES_NO_OPTION,
                NotifyDescriptor.WARNING_MESSAGE);
                nd.setValue(NotifyDescriptor.NO_OPTION);       
                DialogDisplayer.getDefault().notify(nd);
                if(nd.getValue() != NotifyDescriptor.YES_OPTION) return;
            }
            else {
                try {
                    java.nio.charset.CharsetEncoder coder = java.nio.charset.Charset.forName(encoding).newEncoder();
                    if (!coder.canEncode(getDocument().getText(0, getDocument().getLength()))){
                        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                        NbBundle.getMessage (JsfJspEditorSupport.class, "MSG_BadCharConversion", //NOI18N
                        new Object [] { getDataObject().getPrimaryFile().getNameExt(),
                                        encoding}),
                            NotifyDescriptor.YES_NO_OPTION,
                            NotifyDescriptor.WARNING_MESSAGE);
                            nd.setValue(NotifyDescriptor.NO_OPTION);
                            DialogDisplayer.getDefault().notify(nd);
                            if(nd.getValue() != NotifyDescriptor.YES_OPTION) return;                
                    }
                }
                catch (javax.swing.text.BadLocationException e){
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
            super.saveDocument();
        }
    }
    
    @Override
    public void open(){
        ((JsfJspDataObject)getDataObject()).updateFileEncoding(false);
        encoding = ((JsfJspDataObject)getDataObject()).getFileEncoding(); //use encoding from fileobject
        
        if (!isSupportedEncoding(encoding)) {
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                NbBundle.getMessage (JsfJspEditorSupport.class, "MSG_BadEncodingDuringLoad", //NOI18N
                    new Object [] { getDataObject().getPrimaryFile().getNameExt(),
                                    encoding, 
                                    defaultEncoding} ), 
                NotifyDescriptor.YES_NO_OPTION,
                NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            if(nd.getValue() != NotifyDescriptor.YES_OPTION) return;
        }
        
        super.open();
    }
    
    protected void editJsp() {
        ((JsfJspDataObject)getDataObject()).updateFileEncoding(false);
        encoding = ((JsfJspDataObject)getDataObject()).getFileEncoding(); //use encoding from fileobject
        
        if (!isSupportedEncoding(encoding)) {
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                NbBundle.getMessage (JsfJspEditorSupport.class, "MSG_BadEncodingDuringLoad", //NOI18N
                    new Object [] { getDataObject().getPrimaryFile().getNameExt(),
                                    encoding, 
                                    defaultEncoding} ), 
                NotifyDescriptor.YES_NO_OPTION,
                NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            if(nd.getValue() != NotifyDescriptor.YES_OPTION) return;
        }
        
        final JsfJavaEditorSupport support = getJsfJavaEditorSupport(false);
        if (support == null) return;
        support.openJsp();        
    }
    
    
    protected void loadFromStreamToKit(StyledDocument doc, InputStream stream, EditorKit kit) throws IOException, BadLocationException {
        ((JsfJspDataObject)getDataObject()).updateFileEncoding(false);
        super.loadFromStreamToKit(doc, stream, kit);
    }
    
    private static boolean isSupportedEncoding(String encoding){
        if(encoding == null) {
            return false;
        }
        try{
            return java.nio.charset.Charset.isSupported(encoding);
        } catch (java.nio.charset.IllegalCharsetNameException icne){
            return false;
        }
    }
    
    protected CloneableEditor createCloneableEditor() {
        return new JspEditorTopComponent((JsfJspDataObject)getDataObject());
    }
    
    /** Trick to get the superclass method equivalent. */
    private final Object getLock() {
        return allEditors;
    }
    
    /** Creates multiview element. */
    MultiViewElement createMultiViewElement() {
        //synchronized (allEditors) {
        synchronized (getLock()) {
            CloneableTopComponent editor = createCloneableTopComponent();
            editor.setReference(allEditors);

            return (MultiViewElement)editor;
        }
    }
    
    /** XXX Making it accessible whitin this package. And overrides it
     * the way to also include status of corresponding java file. */
    protected String messageName() {
        DataObject jspObj = getDataObject();
        if(!jspObj.isValid()) {
            return ""; // NOI18N
        }
        
        String name;
        // #6293578 They don't want to show extensions in any case.
//        if(org.openide.loaders.DataNode.getShowFileExtensions()) {
//            name = jspObj.getPrimaryFile().getNameExt();
//        } else {
        name = jspObj.getPrimaryFile().getName();
//        }
        return addFlagsToName(name, jspObj);
    }
    
    /** XXX Helper only. */
    private String addFlagsToName(String name, DataObject jspObj) {
        JsfJavaEditorSupport jsfJavaEditorSupport = getJsfJavaEditorSupport(jspObj.getPrimaryFile(), true);
        int version = 3;
        if(isModified() || (jsfJavaEditorSupport != null && jsfJavaEditorSupport.isModified())) {
            if(jspObj.getPrimaryFile().isReadOnly()) {
                version = 2;
            } else {
                version = 1;
            }
        } else if(jspObj.getPrimaryFile().isReadOnly()) {
            version = 0;
        }
        
        return NbBundle.getMessage(DataObject.class, "LAB_EditorName", new Integer(version), name );
    }
    
    /** XXX Making it accessible whitin this package. */
    protected String messageToolTip() {
        return super.messageToolTip();
    }
    
    // XXX PROBLEM NB didn't solve the issues we asked for(NB #59046, #59043), that
    // way there is no real support for multiviews containing two or more editors
    // which is our case, and also there seems to be no way to hack it (like this overriding).
//    /** XXX Overriding superclass to select the correct tab in multiview. */
//    protected Pane openAt(final org.openide.text.PositionRef pos, final int column) {
//        Pane pane = super.openAt(pos, column);
//        if(pane instanceof TopComponent) {
//            JsfJavaEditorSupport jsfJavaEditorSupport = getJsfJavaEditorSupport(false);
//            jsfJavaEditorSupport.viewJspSource((TopComponent)pane);
//        }
//        return pane;
//    }
    
    
    /** Nested class. Environment for this support. Extends <code>DataEditorSupport.Env</code> abstract class. */
    private static class Environment extends DataEditorSupport.Env {
        
        private static final long serialVersionUID = 3035543168452715818L;
        
        /** Constructor. */
        public Environment(JsfJspDataObject obj) {
            super(obj);
        }
        
        
        /** Implements abstract superclass method. */
        protected FileObject getFile() {
            return getDataObject().getPrimaryFile();
        }
        
        /** Implements abstract superclass method.*/
        protected FileLock takeLock() throws IOException {
            return ((JsfJspDataObject)getDataObject()).getPrimaryEntry().takeLock();
        }
        
        /**
         * Overrides superclass method.
         * @return text editor support (instance of enclosing class)
         */
        public CloneableOpenSupport findCloneableOpenSupport() {
            return (JsfJspEditorSupport)getDataObject().getCookie(JsfJspEditorSupport.class);
        }
    } // End of nested Environment class.
    
    
    /** To be accessible whitin package. Tricky method to get the superclass equivalent. */
    Env env() {
        return (Env)env;
    }
    
    private static class JspEditorTopComponent extends CloneableEditor
            implements MultiViewElement {
        private static final long serialVersionUID =-3126744316624172415L;
        
        private transient MultiViewElementCallback multiViewElementCallback;
        
        private transient JComponent toolbar;
        
        private JsfJspDataObject jsfJspDataObject;
        
        private PaletteController jspPaletteController;
        
        public JspEditorTopComponent() {
            super();
        }        
        
        public JspEditorTopComponent(JsfJspDataObject jsfJspDataObject) {
            super((JsfJspEditorSupport)jsfJspDataObject.getCookie(JsfJspEditorSupport.class));
            this.jsfJspDataObject = jsfJspDataObject;
            initialize();
        }
        

        
        
        /** Overriding super class to get it open in multiview. */
        public void open(Workspace workspace) {
            if(discard()) {
                JsfJspEditorSupport support = (JsfJspEditorSupport)jsfJspDataObject.getCookie(JsfJspEditorSupport.class);
                ErrorManager.getDefault().log(ErrorManager.WARNING,
                        "Can not open " + this + " component," // NOI18N
                        + " its support environment is not valid" // NOI18N
                        + " [support=" + support + ", env=" // NOI18N
                        + (support == null ? null : support.env()) + "]"); // NOI18N
            } else {
                // Open in multiview.
                final JsfJavaEditorSupport jsfJavaEditorSupport = Utils.findCorrespondingJsfJavaEditorSupport(jsfJspDataObject.getPrimaryFile(), false);
                if(jsfJavaEditorSupport == null) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                            new IllegalStateException("Can't find JsfJavaEditorSupport for " + jsfJspDataObject)); // NOI18N
                } else {
                    CloneableTopComponent editor = jsfJavaEditorSupport.doOpenDesigner();
                    // XXX Trick to use designer comp to find multiview, the jsp component might not be part of multiview yet.
                    //jsfJavaEditorSupport.viewJspSource(editor);
                }
            }
        }
        
        private boolean discard() {
            JsfJspEditorSupport support = (JsfJspEditorSupport)jsfJspDataObject.getCookie(JsfJspEditorSupport.class);
            return support == null || !support.env().isValid();
        }
        
        
        private void initialize() {
            if(jsfJspDataObject != null) {
                setActivatedNodes(new Node[] {jsfJspDataObject.getNodeDelegate()});
            }
            
            initializePalette();
        }
        
               
        // XXX PaletteController
        private void initializePalette( ) {
            String paletteFolderName = "CreatorJspPalette";
            PaletteController controller;
            try {
//                controller = PaletteFactory.createPalette("CreatorJspPalette", new JspPaletteActions()); // NOI18N
                controller = PaletteFactory.createPalette(paletteFolderName, new CodeClipPaletteActions(paletteFolderName, this), null, new CodeClipDragAndDropHandler()); // NOI18N
                
            } catch (java.io.IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                controller = null;
//                System.out.println("Java Palette is null." );
            }
            jspPaletteController = controller;
            return;            
        }
        
        public CloseOperationState canCloseElement() {
            // If this is not the last cloned jsp editor component, closing is OK.
            if(!isLastView(multiViewElementCallback.getTopComponent())) {
                return CloseOperationState.STATE_OK;
            }
            
            // XXX I don't understand this yet. Copied from FormDesigner.
            // Returns a placeholder state - to be sure our CloseHandler is called.
            return MultiViewFactory.createUnsafeCloseState(
                    "ID_CLOSING_JSP", // dummy ID // NOI18N
                    MultiViewFactory.NOOP_CLOSE_ACTION,
                    MultiViewFactory.NOOP_CLOSE_ACTION);
        }
        
        public void setMultiViewCallback(MultiViewElementCallback callback) {
            multiViewElementCallback = callback;
            
// XXX This smells really badly, is this supposed to be the 'typical' way to
// deserialize the needed stuff? (comes from FormEditorSupport)
            // needed for deserialization...
            JsfJspEditorSupport jsfJspEditorSupport = (JsfJspEditorSupport)jsfJspDataObject.getCookie(JsfJspEditorSupport.class);
            if(jsfJspEditorSupport != null) {
                // this is used (or misused?) to obtain the deserialized
                // multiview topcomponent and set it to JsfJavaEditorSupport
//                jsfJspEditorSupport.setMultiView((CloneableTopComponent)multiViewElementCallback.getTopComponent());
                JsfJavaEditorSupport jsfJavaEditorSupport = jsfJspEditorSupport.getJsfJavaEditorSupport(false);
                if (jsfJavaEditorSupport != null) {
                    jsfJavaEditorSupport.updateMultiViewDisplayName();
                    jsfJavaEditorSupport.updateMultiViewToolTip();
                }
            }
        }
        
        public JComponent getToolbarRepresentation() {
            if(toolbar == null) {
                JEditorPane pane = getEditorPane();
                if (pane != null) {
                    Document doc = pane.getDocument();
                    if(doc instanceof NbDocument.CustomToolbar) {
                        toolbar = ((NbDocument.CustomToolbar)doc).createToolbar(pane);
                    }
                }
                if(toolbar == null) {
                    // attempt to create own toolbar??
                    toolbar = new javax.swing.JPanel();
                }
            }
            return toolbar;
        }
        
        public JComponent getVisualRepresentation() {
            return this;
        }
        
        public void componentDeactivated() {
            super.componentDeactivated();
        }
        
        public void componentActivated() {
            super.componentActivated();
            // XXX #6299978 NB #61886 Multiview doesn't handle focus transfer,
            // once fixed in NB, remove this hack.
            if(pane != null) {
                pane.requestFocusInWindow();
            }
            
            InSyncService.getProvider().jspDataObjectTopComponentActivated(jsfJspDataObject);
        }
        
        public void componentHidden() {
            super.componentHidden();
            
            InSyncService.getProvider().jspDataObjectTopComponentHidden(jsfJspDataObject);
        }
        
        public void componentShowing() {
            super.componentShowing();
            
            InSyncService.getProvider().jspDataObjectTopComponentShown(jsfJspDataObject);
        }
        
        public void componentClosed() {
            super.componentClosed();
        }
        
        public void componentOpened() {
            super.componentOpened();
        }
        
        public void requestVisible() {
            if(multiViewElementCallback != null) {
                multiViewElementCallback.requestVisible();
            } else {
                super.requestVisible();
            }
        }
        
        public void requestActive() {
            if(multiViewElementCallback != null) {
                multiViewElementCallback.requestActive();
            } else {
                super.requestActive();
            }
        }
        
        public Action[] getActions() {
            // need to delegate to multiview's actions because of the way editor
            // constructs actions : NbEditorKit.NbBuildPopupMenuAction
            return multiViewElementCallback != null ?
                multiViewElementCallback.createDefaultActions() : super.getActions();
        }
        
        public void writeExternal(ObjectOutput out) throws IOException {
            super.writeExternal(out);
            
            out.writeObject(jsfJspDataObject);
        }
        
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            super.readExternal(in);
            
            jsfJspDataObject = (JsfJspDataObject)in.readObject();
            initialize();
        }
        
        private static boolean isLastView(TopComponent tc) {
            if (!(tc instanceof CloneableTopComponent))
                return false;
            
            boolean oneOrLess = true;
            Enumeration en = ((CloneableTopComponent)tc).getReference().getComponents();
            if (en.hasMoreElements()) {
                en.nextElement();
                if (en.hasMoreElements())
                    oneOrLess = false;
            }
            return oneOrLess;
        }
        
        
        private WeakReference lookupWRef = new WeakReference(null);
        
        /** Adds <code>NavigatorLookupHint</code> into the original lookup,
         * for the navigator. */
        public Lookup getLookup() {
            Lookup lookup = (Lookup)lookupWRef.get();
            
            if (lookup == null) {
                Lookup superLookup = super.getLookup();
                if (jspPaletteController == null) {
                    lookup = new ProxyLookup(new Lookup[] {superLookup});
                } else {
                    lookup = new ProxyLookup(new Lookup[] {superLookup, Lookups.singleton(jspPaletteController)});
                }
                lookupWRef = new WeakReference(lookup);
            }
            
            return lookup;
        }
    }
    
    /*
     * EAT! This should really be getContainingMultiView or something, but will stick to this for now.
     */
    protected JsfJavaEditorSupport getJsfJavaEditorSupport(boolean quietly) {
        return getJsfJavaEditorSupport(getDataObject().getPrimaryFile(), quietly);
    }
    
    protected JsfJavaEditorSupport getJsfJavaEditorSupport(FileObject fileObject, boolean quietly) {
        JsfJavaEditorSupport jsfJavaEditorSupport = Utils.findCorrespondingJsfJavaEditorSupport(fileObject, quietly);
        return jsfJavaEditorSupport;
    }
    
    public void updateMultiViewDisplayName() {
        CloneableTopComponent topComponent = allEditors.getArbitraryComponent();
        if (topComponent != null)
            topComponent.toString();
        // XXX Also handle renaming of the multiview.
        final JsfJavaEditorSupport jsfJavaEditorSupport = getJsfJavaEditorSupport(true);
        if (jsfJavaEditorSupport == null)
            return;
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                jsfJavaEditorSupport.updateMultiViewDisplayName();
            }
        });
    }
    
}
