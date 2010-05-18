/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.websvc.rest.wadl.design.loader;

import java.awt.EventQueue;
import java.io.ByteArrayOutputStream;
import java.io.CharConversionException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;

import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.CloseOperationHandler;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.modules.xml.api.EncodingUtil;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.websvc.rest.wadl.design.multiview.MultiViewSupport;
import org.netbeans.modules.websvc.rest.wadl.design.undo.QuietUndoManager;
import org.netbeans.modules.websvc.rest.wadl.model.WadlModel;
import org.netbeans.modules.websvc.rest.wadl.model.WadlModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
//import org.netbeans.modules.xml.xam.ui.undo.QuietUndoManager;
import org.netbeans.modules.xml.xdm.nodes.Convertors;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.UndoRedo;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.DataEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;


/**
 *
 * @author Ayub Khan
 */
public class WadlEditorSupport extends DataEditorSupport
        implements WadlModelCookie, OpenCookie, EditCookie,
        EditorCookie.Observable, LineCookie, CloseCookie, PrintCookie {
    
    private transient WadlModel model;
    
    /** Used for managing the prepareTask listener. */
    private transient Task prepareTask2;

    /**
     *
     *
     */
    public WadlEditorSupport(WadlDataObject sobj) {
        super(sobj, new WadlEditorEnv(sobj));
        setMIMEType(WadlDataLoader.MIME_TYPE);
    }
    
    
    /**
     *
     *
     */
    public WadlEditorEnv getEnv() {
        return (WadlEditorEnv)env;
    }

    @Override
    protected Pane createPane() {
//        TopComponent tc = WadlMultiViewFactory.createMultiView(
//                (WadlDataObject) getDataObject());
//        // Note that initialization of the editor happens separately,
//        // and we only need to handle that during the initial creation
//        // of the text editor.
//        Mode editorMode = WindowManager.getDefault().findMode(
//                WadlEditorSupport.EDITOR_MODE);
//        if (editorMode != null) {
//            editorMode.dockInto(tc);
//        }
//        return (Pane) tc;
        MultiViewSupport mvs = ((WadlDataObject) getDataObject()).getCookie(MultiViewSupport.class);
        if (mvs == null) return super.createPane();
        return (Pane) mvs.createMultiView();
    }

    /**
     *
     *
     */
    public static boolean isLastView(TopComponent tc) {
        
        if (!(tc instanceof CloneableTopComponent))
            return false;
        
        boolean oneOrLess = true;
        Enumeration en =
            ((CloneableTopComponent)tc).getReference().getComponents();
        if (en.hasMoreElements()) {
            en.nextElement();
            if (en.hasMoreElements())
                oneOrLess = false;
        }
        
        return oneOrLess;
    }
    
    // Change method access to public
    @Override
    public void initializeCloneableEditor(CloneableEditor editor) {
        super.initializeCloneableEditor(editor);
        // Force the title to update so the * left over from when the
        // modified data object was discarded is removed from the title.
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                // Have to do this later to avoid infinite loop.
                updateTitles();
            }
        });
    }
    
    @Override
    protected void updateTitles() {
        // This method is invoked by DataEditorSupport.DataNodeListener
        // whenever the DataNode displayName property is changed. It is
        // also called when the CloneableEditorSupport is (un)modified.

        // Let the superclass handle the CloneableEditor instances.
        super.updateTitles();

        // We need to get the title updated on the MultiViewTopComponent.
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                // Create a list of TopComponents associated with the
                // editor's data object, starting with the active
                // TopComponent. Add all open TopComponents in any
                // mode that are associated with the DataObject.
                // [Note that EDITOR_MODE does not contain editors in
                // split mode.]
                List<TopComponent> associatedTCs = new ArrayList<TopComponent>();
                DataObject targetDO = getDataObject();
                TopComponent activeTC = TopComponent.getRegistry().getActivated();
                if (activeTC != null && targetDO == activeTC.getLookup().lookup(
                        DataObject.class)) {
                    associatedTCs.add(activeTC);
                }
                Set openTCs = TopComponent.getRegistry().getOpened();
                for (Object tc : openTCs) {
                    TopComponent tcc = (TopComponent) tc;
                    if (targetDO == tcc.getLookup().lookup(
                            DataObject.class)) {
                        associatedTCs.add(tcc);
                    }
                }
                for (TopComponent tc : associatedTCs) {
                    // Make sure this is a multiview window, and not just some
                    // window that has our DataObject (e.g. Projects, Files).
                    MultiViewHandler mvh = MultiViews.findMultiViewHandler(tc);
                    if (mvh != null) {
                        tc.setHtmlDisplayName(messageHtmlName());
                        String name = messageName();
                        tc.setDisplayName(name);
                        tc.setName(name);
                        tc.setToolTipText(messageToolTip());
                    }
                }
            }
        });
    }

    @Override
    protected UndoRedo.Manager createUndoRedoManager() {
        // Override so the superclass will use our proxy undo manager
        // instead of the default, then we can intercept edits.
        return new QuietUndoManager(super.createUndoRedoManager());
        // Note we cannot set the document on the undo manager right
        // now, as CES is probably trying to open the document.
    }

    /**
     * Returns the UndoRedo.Manager instance managed by this editor support.
     *
     * @return UndoRedo.Manager instance.
     */
    public QuietUndoManager getUndoManager() {
        return (QuietUndoManager) getUndoRedo();
    }

    @Override
    public Task prepareDocument() {
        Task task = super.prepareDocument();
        // Avoid listening to the same task more than once.
        if (task != prepareTask2) {
            prepareTask2 = task;
            task.addTaskListener(new TaskListener() {
                public void taskFinished(Task task) {
                    QuietUndoManager undo = getUndoManager();
                    StyledDocument doc = getDocument();
                    synchronized (undo) {
                        // Now that the document is ready, pass it to the manager.
                        undo.setDocument((AbstractDocument) doc);
                        if (!undo.isCompound()) {
                            // The superclass prepareDocument() adds the undo/redo
                            // manager as a listener -- we need to remove it since
                            // we will initially listen to the model instead.
                            doc.removeUndoableEditListener(undo);
                            // If not listening to document, then listen to model.
                            addUndoManagerToModel(undo);
                        }
                    }
                    prepareTask2 = null;
                }
            });
        }
        return task;
    }
    
    @Override
	public Task reloadDocument() {
        Task task = super.reloadDocument();
        task.addTaskListener(new TaskListener() {
            public void taskFinished(Task task) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        QuietUndoManager undo = getUndoManager();
                        StyledDocument doc = getDocument();
                        // The superclass reloadDocument() adds the undo
                        // manager as an undoable edit listener.
                        synchronized (undo) {
                            if (!undo.isCompound()) {
                                doc.removeUndoableEditListener(undo);
                            }
                        }
                    }
                });
            }
        });
        return task;
    }

    @Override
    protected void notifyClosed() {
        // Stop listening to the undoable edit sources when we are closed.
        QuietUndoManager undo = getUndoManager();
        StyledDocument doc = getDocument();
        synchronized (undo) {
            // May be null when closing the editor.
            if (doc != null) {
                doc.removeUndoableEditListener(undo);
                undo.endCompound();
                undo.setDocument(null);
            }
            try {
                WadlModel model = getModel();
                if (model != null) {
                    model.removeUndoableEditListener(undo);
                }
                // Must unset the model when no longer listening to it.
                undo.setModel(null);
            } catch (IOException ioe) {
                // Model is gone, but just removing the listener is not
                // going to matter anyway.
            }
        }
        super.notifyClosed();
    }
    
    public WadlModel getModel() throws IOException {
        if(model != null)
            return model;
        WadlDataObject dobj = getEnv().getWadlDataObject();
        ModelSource modelSource = Utilities.getModelSource(dobj.getPrimaryFile(), true);
        boolean validModelSource = modelSource != null &&
                modelSource.getLookup().lookup(Document.class) != null;
        if (!validModelSource) {
            throw new IOException(
                    NbBundle.getMessage(WadlEditorSupport.class,
                    "MSG_UnableToCreateModel"));
        }
        model = WadlModelFactory.getDefault().getModel(modelSource);
        return model;
    }

    /**
     * Adds the undo/redo manager to the document as an undoable edit
     * listener, so it receives the edits onto the queue. The manager
     * will be removed from the model as an undoable edit listener.
     *
     * <p>This method may be called repeatedly.</p>
     */
    public void addUndoManagerToDocument() {
        // This method may be called repeatedly.
        // Stop the undo manager from listening to the model, as it will
        // be listening to the document now.
        QuietUndoManager undo = getUndoManager();
        StyledDocument doc = getDocument();

        try {
            model = getModel();
        } catch (IOException ioe) {
            // Model is gone, but just removing the listener is not
            // going to matter anyway.
        }
        synchronized (undo) {
            if (model != null) {
                model.removeUndoableEditListener(undo);
            }
            // Must unset the model when no longer listening to it.
            undo.setModel(null);
            // Document may be null if the cloned views are not behaving correctly.
            if (doc != null) {
                // Ensure the listener is not added twice.
                doc.removeUndoableEditListener(undo);
                doc.addUndoableEditListener(undo);
                // Start the compound mode of the undo manager, such that when
                // we are hidden, we will treat all of the edits as a single
                // compound edit. This avoids having the user invoke undo
                // numerous times when in the model view.
                undo.beginCompound();
            }
        }
    }

    /**
     * Removes the undo/redo manager undoable edit listener from the
     * document, to stop receiving undoable edits. The manager will
     * be added to the model as an undoable edit listener.
     *
     * <p>This method may be called repeatedly.</p>
     */
    public void removeUndoManagerFromDocument() {
        // This method may be called repeatedly.
        QuietUndoManager undo = getUndoManager();
        StyledDocument doc = getDocument();
        synchronized (undo) {
            // May be null when closing the editor.
            if (doc != null) {
                doc.removeUndoableEditListener(undo);
                undo.endCompound();
            }
            // Have the undo manager listen to the model when it is not
            // listening to the document.
            addUndoManagerToModel(undo);
        }
    }

    /**
     * Add the undo/redo manager undoable edit listener to the model.
     *
     * <p>Caller should synchronize on the undo manager prior to calling
     * this method, to avoid thread concurrency issues.</p>
     *
     * @param  undo  the undo manager.
     */
    private void addUndoManagerToModel(QuietUndoManager undo) {
        // This method may be called repeatedly.
        try {
            model = getModel();
            if (model != null) {
                // Ensure the listener is not added twice.
                model.removeUndoableEditListener(undo);
                model.addUndoableEditListener(undo);
                // Ensure the model is sync'd when undo/redo is invoked,
                // otherwise the edits are added to the queue and eventually
                // cause exceptions.
                undo.setModel(model);
            }
        } catch (IOException ioe) {
            // Model is gone, but just removing the listener is not
            // going to matter anyway.
        }
    }

    /**
     * This method allows the close behavior of CloneableEditorSupport to be 
     * invoked from the SourceMultiViewElement. The close method of 
     * CloneableEditorSupport at least clears the undo queue and releases
     * the swing document. 
     */ 
    public boolean silentClose() {
        return super.close(false);
    }

    /**
     * Have the Wadl model sync with the document.
     */
    public void syncModel() {
        // Only sync the document if the change relates to loss of focus,
        // which indicates that we are switching from the source view.
        // Update the tree with the modified text.
        try {
            WadlModel model = getModel();
            if(model != null) {
                model.sync();
            }
        } catch (Throwable ioe) {
            // The document cannot be parsed
            NotifyDescriptor nd = new NotifyDescriptor.Message(
                    NbBundle.getMessage(MultiViewSupport.class, 
                    "MSG_NotWellformedWadl"), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
        
    }
    
    // following 4 methods are copied from schemaeditorsupport.
    
    @Override
    protected void loadFromStreamToKit(StyledDocument doc, InputStream in,
            EditorKit kit) throws IOException, BadLocationException {
        // Detect the encoding to get optimized reader if UTF-8.
        String enc = EncodingUtil.detectEncoding(in);
        if (enc == null) {
            enc = "UTF8"; // NOI18N
        }
        try {
            Reader reader = new InputStreamReader(in, enc);
            kit.read(reader, doc, 0);
        } catch (CharConversionException cce) {
        } catch (UnsupportedEncodingException uee) {
        }
    }
    
    @Override
    protected void saveFromKitToStream(StyledDocument doc, EditorKit kit,
            OutputStream out) throws IOException, BadLocationException {
        // Detect the encoding, using UTF8 if the encoding is not set.
        String enc = EncodingUtil.detectEncoding(doc);
        if (enc == null) {
            enc = "UTF8"; // NOI18N
        }
        try {
            // Test the encoding on a dummy stream.
            new OutputStreamWriter(new ByteArrayOutputStream(1), enc);
            // If that worked, we can go ahead with the encoding.
            Writer writer = new OutputStreamWriter(out, enc);
            kit.write(writer, doc, 0, doc.getLength());
        } catch (UnsupportedEncodingException uee) {
            // Safest option is to write nothing, preserving the original file.
            IOException ioex = new IOException("Unsupported encoding " + enc); // NOI18N
            ErrorManager.getDefault().annotate(ioex,
                    NbBundle.getMessage(WadlEditorSupport.class,
                    "MSG_WadlEditorSupport_Unsupported_Encoding", enc));
            throw ioex;
        }
    }
    
    
    @Override
    public void saveDocument() throws IOException {
        final StyledDocument doc = getDocument();
        // Save document using encoding declared in XML prolog if possible,
        // otherwise use UTF-8 (in such case it updates the prolog).
        String enc = EncodingUtil.detectEncoding(doc);
        if (enc == null) {
            enc = "UTF8"; // NOI18N
        }
        try {
            // Test the encoding on a dummy stream.
            new OutputStreamWriter(new ByteArrayOutputStream(1), enc);
            if (!checkCharsetConversion(Convertors.java2iana(enc))){
                return;
            }
            super.saveDocument();
            getDataObject().setModified(false);
            
        } catch (UnsupportedEncodingException uee) {
            NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(
                    java.text.MessageFormat.format(
                    NbBundle.getMessage(WadlEditorSupport.class,
                    "MSG_WadlEditorSupport_Use_UTF8"),
                    new Object[] {enc}));
            Object res = DialogDisplayer.getDefault().notify(descriptor);
            
            if (res.equals(NotifyDescriptor.YES_OPTION)) {
                // Update prolog to new valid encoding.
                try {
                    final int MAX_PROLOG = 1000;
                    int maxPrologLen = Math.min(MAX_PROLOG, doc.getLength());
                    final char prolog[] = doc.getText(0, maxPrologLen).toCharArray();
                    int prologLen = 0;
                    if (prolog[0] == '<' && prolog[1] == '?' && prolog[2] == 'x') {
                        for (int i = 3; i < maxPrologLen; i++) {
                            if (prolog[i] == '?' && prolog[i + 1] == '>') {
                                prologLen = i + 1;
                                break;
                            }
                        }
                    }
                    
                    final int passPrologLen = prologLen;
                    Runnable edit = new Runnable() {
                        public void run() {
                            try {
                                doc.remove(0, passPrologLen + 1);
                                doc.insertString(0, "<?xml version='1.0' encoding='UTF-8' ?>\n<!-- was: " +
                                        new String(prolog, 0, passPrologLen + 1) + " -->", null); // NOI18N
                            } catch (BadLocationException ble) {
                                if (System.getProperty("netbeans.debug.exceptions") != null) // NOI18N
                                    ble.printStackTrace();
                            }
                        }
                    };
                    NbDocument.runAtomic(doc, edit);
                    
                    super.saveDocument();
                    getDataObject().setModified(false);
                    
                } catch (BadLocationException lex) {
                    ErrorManager.getDefault().notify(lex);
                }
            }
        }
    }
    
    /**
     * Validate the selected encoding to determine if it is usuable or not.
     * If there is a problem, prompt the user to confirm the encoding.
     *
     * @param  encoding  the character set encoding to validate.
     * @return  true if encoding can be used, false otherwise.
     */
    private boolean checkCharsetConversion(String encoding) {
        boolean value = true;
        try {
            java.nio.charset.CharsetEncoder coder =
                    java.nio.charset.Charset.forName(encoding).newEncoder();
            if (!coder.canEncode(getDocument().getText(0,
                    getDocument().getLength()))){
                Object[] margs = new Object[] {
                    getDataObject().getPrimaryFile().getNameExt(),
                    encoding
                };
                String msg = NbBundle.getMessage(WadlEditorSupport.class,
                        "MSG_WadlEditorSupport_BadCharConversion", margs);
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg,
                        NotifyDescriptor.YES_NO_OPTION,
                        NotifyDescriptor.WARNING_MESSAGE);
                nd.setValue(NotifyDescriptor.NO_OPTION);
                DialogDisplayer.getDefault().notify(nd);
                if (nd.getValue() != NotifyDescriptor.YES_OPTION) {
                    value = false;
                }
            }
        } catch (BadLocationException ble) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ble);
        }
        return value;
    }
    
    
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * Env class extends DataEditorSupport.Env.
     */
    protected static class WadlEditorEnv extends DataEditorSupport.Env {
        
        static final long serialVersionUID =1099957785497677206L;
        
        public WadlEditorEnv(WadlDataObject obj) {
            super(obj);
        }
        
        public CloneableEditorSupport findTextEditorSupport() {
            return getWadlDataObject().getWadlEditorSupport();
        }
        
        public WadlDataObject getWadlDataObject(){
            return (WadlDataObject) getDataObject();
        }
        
        @Override
        protected FileObject getFile() {
            return getDataObject().getPrimaryFile();
        }
        
        @Override
        protected FileLock takeLock() throws IOException {
            return getDataObject().getPrimaryFile().lock();
        }
    }
    
    
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * Implementation of CloseOperationHandler for multiview. Ensures both
     * column view and xml editor are correctly closed, data saved, etc. Holds
     * a reference to DataObject only - to be serializable with the
     * multiview TopComponent without problems.
     */
    public static class CloseHandler implements CloseOperationHandler, Serializable {
        private static final long serialVersionUID =-3838395157610633251L;
        private DataObject dataObject;

        private CloseHandler() {
            super();
        }

        public CloseHandler(DataObject dobj) {
            dataObject = dobj;
        }

        private WadlEditorSupport getWadlEditorSupport() {
            return dataObject instanceof WadlDataObject ?
                    ((WadlDataObject) dataObject).getWadlEditorSupport() : null;
        }

        public boolean resolveCloseOperation(CloseOperationState[] elements) {
            WadlEditorSupport wadlEditor = getWadlEditorSupport();
            boolean canClose = wadlEditor != null ? wadlEditor.canClose() : true;
            // during the shutdown sequence this is called twice. The first time
            // through the multi-view infrastructure. The second time is done through
            // the TopComponent close. If the file is dirty and the user chooses
            // to discard changes, the second time will also ask whether the
            // to save or discard changes. 
            if (canClose) {
                dataObject.setModified(false);
            }
            return canClose;
        }
    }
}
