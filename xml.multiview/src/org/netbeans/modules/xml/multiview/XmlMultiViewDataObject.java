/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.multiview;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.CloneableTopComponent;
import org.xml.sax.InputSource;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Date;

/**
 * XmlMultiviewDataObject.java
 *
 * Created on October 5, 2004, 10:49 AM
 * @author  mkuchtiak
 */
public abstract class XmlMultiViewDataObject extends MultiDataObject implements CookieSet.Factory {

    public static final String PROP_DOCUMENT_VALID = "document_valid"; //NOI18N
    protected XmlMultiViewEditorSupport editor;
    private org.xml.sax.SAXException saxError;
    boolean changedFromUI;

    private static final int PARSING_INIT_DELAY = 100;
    private RequestProcessor.Task synchronizeModelTask = null;
    private boolean updateFromModel = false;
    private boolean updatingFromModel = false;
    private boolean updatingModel = false;
    private Boolean overwriteUnparseable = Boolean.TRUE;
    private long handleUnparseableTimeout = 0;
    private static final int HANDLE_UNPARSABLE_TIMEOUT = 2000;
    protected boolean parseable;

    final SaveCookie saveCookie = new SaveCookie() {
        /** Implements <code>SaveCookie</code> interface. */
        public void save() throws java.io.IOException {
            editor.saveDocument();
        }
    };

    /** Creates a new instance of XmlMultiViewDataObject */
    public XmlMultiViewDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        getCookieSet().add(XmlMultiViewEditorSupport.class, this);
        documentUpdated();
    }
    
    public org.openide.nodes.Node.Cookie createCookie(Class clazz) {
        if (clazz.isAssignableFrom(XmlMultiViewEditorSupport.class)) {
            return createEditorSupport();
        } else {
            return null;
        }
    }
    
    /** Gets editor support for this data object. */
    private synchronized XmlMultiViewEditorSupport createEditorSupport() {
        if(editor == null) {
            editor = new XmlMultiViewEditorSupport(this);
            editor.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (EditorCookie.Observable.PROP_DOCUMENT.equals(evt.getPropertyName())) {
                        documentUpdated();
                    }
                }
            });
        }
        return editor;
    }
    
    public XmlMultiViewEditorSupport getEditorSupport() {
        return (XmlMultiViewEditorSupport)getCookie(XmlMultiViewEditorSupport.class);
    }

    /**
     * Called on close-discard option.
     * The data model is updated from corresponding file object(s).
     */
    protected void reloadModelFromFileObject() throws java.io.IOException {
        editor.openDocument();
    }
    
    /** Update data model from document text . Called when something is changed in xml editor. 
    * @return true if model was succesfully created, false otherwise
    */
    protected abstract boolean updateModelFromDocument() throws java.io.IOException;
    
    /** Similar to updateModelFromDocument() but data model is not modified.
     */
    protected void validateSource(){
    }
    /** Update text document from data model. Called when something is changed in visual editor.
    */
    protected abstract String generateDocumentFromModel();
    
    /** enables to switch quickly to XML perspective in multi view editor
     */
    public void goToXmlView() {
        getEditorSupport().goToXmlPerspective();
    }
    
    public boolean isChangedFromUI() {
        return changedFromUI;
    }

    protected void setSaxError(org.xml.sax.SAXException saxError) {
        org.xml.sax.SAXException oldError = this.saxError;
        this.saxError=saxError;
        if (oldError==null) {
            if (saxError != null) {
                firePropertyChange(PROP_DOCUMENT_VALID, Boolean.TRUE, Boolean.FALSE);
            }
        } else {
            if (saxError == null) {
                firePropertyChange(PROP_DOCUMENT_VALID, Boolean.FALSE, Boolean.TRUE);
            }
        }
    }
    
    public org.xml.sax.SAXException getSaxError() {
        return saxError;
    }

    /** This method is used for obtaining the current source of xml document.
    * First try if document is open in editor. If not, provide the input from
    * underlayed file object.
    * @return The InputStream from swing document or from file
    * @exception IOException if some problem occurs
    */
    protected InputStream createInputStream() throws java.io.IOException {
        return createEditorSupport().getInputStream();
    }
    
    /** This method is used for obtaining the current source of xml document.
    * First try if document is open in editor. If not, provide the input from
    * underlayed file object.
    * @return The InputSource from swing document or null
    * @exception IOException if some problem occurs
    */
    protected InputSource getFileObjectInputSource() throws java.io.IOException {
        return new InputSource(new FileReader(org.openide.filesystems.FileUtil.toFile(getPrimaryFile())));
    }
    
    /** This method is used for obtaining the current source of xml document.
    * First try if document is open in editor. If not, provide the input from
    * underlayed file object.
    * @return The InputSource from swing document or null
    * @exception IOException if some problem occurs
    */
    protected InputSource createInputSource() throws java.io.IOException {
        if ((editor != null) && (editor.isDocumentLoaded())) {
            // loading from the memory (Document)
            final javax.swing.text.Document doc = editor.getDocument();
            final String[] str = new String[1];
            // safely take the text from the document
            Runnable run = new Runnable() {
                public void run() {
                    try {
                        str[0] = doc.getText(0, doc.getLength());
                    }
                    catch (javax.swing.text.BadLocationException e) {
                        // impossible
                    }
                }
            };
            
            doc.render(run);
            return new InputSource(new StringReader(str[0]));
        } 
        else {
            // loading from the file
            return new InputSource(getPrimaryFile().getInputStream());
        }
    }
    
    protected void updateDocument() {
        if (handleUnparseableTimeout == -1) {
            return;
        }
        if (!parseable) {
            long time = new Date().getTime();
            handleUnparseableTimeout = time;
            if (time > handleUnparseableTimeout) {
                handleUnparseableTimeout = -1;
                Utils.runInAwtDispatchThread(new Runnable() {
                    public void run() {
                        String message = NbBundle.getMessage(XmlMultiViewDataObject.class,
                                "TXT_OverwriteUnparsableDocument", getPrimaryFile().getNameExt());
                        NotifyDescriptor desc = new NotifyDescriptor.Confirmation(message,
                                NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.WARNING_MESSAGE);
                        DialogDisplayer.getDefault().notify(desc);
                        overwriteUnparseable = Boolean.valueOf(desc.getValue() == NotifyDescriptor.YES_OPTION);
                        handleUnparseableTimeout = new Date().getTime() + HANDLE_UNPARSABLE_TIMEOUT;
                        synchronizeModelTask.run();
                    }
                });
            } else if (!overwriteUnparseable.booleanValue()) {
                return;
            }
        }
        final String newDoc = generateDocumentFromModel();
        try {
            javax.swing.text.Document doc = getEditorSupport().openDocument();
            Utils.replaceDocument(doc, newDoc);
            setSaxError(null);
        } catch (javax.swing.text.BadLocationException e) {
            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, e);
        } catch (IOException e) {
            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, e);
        } finally {
            changedFromUI = false;
        }
    }
    
    /** Display Name for MultiView editor
     */
    protected String getDisplayName() {
        return getPrimaryFile().getNameExt();
    }
    
    /** Icon for XML View */
    protected java.awt.Image getXmlViewIcon() {
        return org.openide.util.Utilities.loadImage("org/netbeans/modules/xml/multiview/resources/xmlObject.gif"); //NOI18N
    }
    
    public void modelUpdatedFromUI() {
        changedFromUI=true;
        modelChanged();
    }

    /** MultiViewDesc for MultiView editor
     */
    protected abstract DesignMultiViewDesc[] getMultiViewDesc();
    
    public void setLastOpenView(int index) {
        getEditorSupport().setLastOpenView(index);
    }

    /** Returns true if xml file is parseable(data model can be created),
     *  Method is called before switching to the design view from XML view when the document isn't parseable.
     */
    public boolean isDocumentParseable() {
        waitForSync();
        return parseable;
    }
    
    /** Used to detect if data model has already been created or not.
     * Method is called before switching to the design view from XML view when the document isn't parseable.
     */
    protected abstract boolean isModelCreated();

    public void checkParseable() {
        if (!isDocumentParseable()) {
            NotifyDescriptor desc = new org.openide.NotifyDescriptor.Message(
                    NbBundle.getMessage(XmlMultiViewDataObject.class, "TXT_DocumentUnparsable",
                            getPrimaryFile().getNameExt()), NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
            if (!isModelCreated()) {
                goToXmlView();
            }
        }
    }
    /** provides renaming of super top component */
    protected FileObject handleRename(String name) throws IOException {
        FileObject retValue = super.handleRename(name);
        org.openide.windows.TopComponent mvtc = getEditorSupport().getMVTC();
        if (mvtc!=null) {
            mvtc.setDisplayName(getDisplayName());
            mvtc.setToolTipText(getPrimaryFile().getPath());
        }
        return retValue;
    }

    /**
     * Set whether the object is considered modified.
     * Also fires a change event.
     * If the new value is <code>true</code>, the data object is added into a {@link #getRegistry registry} of opened data objects.
     * If the new value is <code>false</code>,
     * the data object is removed from the registry.
     */
    public void setModified(boolean modif) {
        super.setModified(modif);
        if (modif) {
            // Add save cookie
            if (getCookie(SaveCookie.class) == null) {
                getCookieSet().add(saveCookie);
            }
        } else {
            // Remove save cookie
            if(saveCookie.equals(getCookie(SaveCookie.class))) {
                getCookieSet().remove(saveCookie);
            }

        }
    }

    public void modelChanged() {
        if (!updatingModel) {
            synchronizeModel(true);
        }
    }

    public void documentUpdated() {
        if (!updatingFromModel) {
            synchronizeModel(false);
        }
    }

    private void sync() {
        if (updateFromModel) {
            updatingFromModel = true;
            try {
                updateDocument();
            } finally {
                updatingFromModel = false;
            }
            validateSource();
        } else {
            updatingModel = true;
            try {
                updateModelFromDocument();
            } catch (IOException e) {
                synchronizeModel(updateFromModel);
            } finally {
                updatingModel = false;
            }
        }
    }

    private void synchronizeModel(boolean updateFromModel) {
        this.updateFromModel = updateFromModel;
        getSynchronizeModelTask().schedule(PARSING_INIT_DELAY);
    }

    private RequestProcessor.Task getSynchronizeModelTask() {
        if (synchronizeModelTask == null) {
            synchronizeModelTask = RequestProcessor.getDefault().create(new Runnable() {
                public void run() {
                    sync();
                }
            });
        }
        return synchronizeModelTask;
    }

    public boolean canClose() {
        final CloneableTopComponent topComponent = ((CloneableTopComponent) editor.getMVTC());
        Enumeration enumeration = topComponent.getReference().getComponents();
        if (enumeration.hasMoreElements()) {
            enumeration.nextElement();
            if (enumeration.hasMoreElements()) {
                return true;
            }
        }
        getSynchronizeModelTask().schedule(PARSING_INIT_DELAY);
        waitForSync();
        return !isModified();
    }

    protected void waitForSync() {
        if (synchronizeModelTask != null) {
            synchronizeModelTask.waitFinished();
        }
    }
    
    public org.netbeans.core.api.multiview.MultiViewPerspective getSelectedPerspective() {
        return getEditorSupport().getSelectedPerspective();
    }
    
    /** Enable to focus specific object in Multiview Editor
     *  The default implementation opens the XML View.
     */
    public void showElement(Object element) {
        getEditorSupport().edit();
    }
}
