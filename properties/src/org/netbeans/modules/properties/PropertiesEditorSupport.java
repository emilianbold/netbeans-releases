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

package org.netbeans.modules.properties;

import java.awt.EventQueue;
import java.awt.Image;
import java.beans.*;
import java.io.*;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.UndoRedo;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.Utilities;
import org.openide.windows.CloneableOpenSupport;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.windows.TopComponent;

/** 
 * Support for viewing .properties files (EditCookie) by opening them in a text editor.
 *
 * @author Petr Jiricka, Peter Zavadsky 
 * @see org.openide.text.CloneableEditorSupport
 */
public class PropertiesEditorSupport extends CloneableEditorSupport 
implements EditCookie, EditorCookie.Observable, PrintCookie, CloseCookie, Serializable {
    
    /** */
    static final String PROP_NON_ASCII_CHAR_READ = "org.netbeans.modules.properties.nonAsciiPresent";   //NOI18N
    /** New lines in this file was delimited by '\n'. */
    private static final byte NEW_LINE_N = 0;
    /** New lines in this file was delimited by '\r'. */
    private static final byte NEW_LINE_R = 1;
    /** New lines in this file was delimited by '\r\n'. */
    private static final byte NEW_LINE_RN = 2;
    
    /** */
    private FileStatusListener fsStatusListener;
    
    /** The type of new lines. Default is <code>NEW_LINE_N</code>. */
    private byte newLineType = NEW_LINE_N;
    
    /** Visible view of underlying file entry */
    transient PropertiesFileEntry myEntry;
    
    /** Generated serial version UID. */
    static final long serialVersionUID =1787354011149868490L;
    
    
    /** Constructor. */
    public PropertiesEditorSupport(PropertiesFileEntry entry) {
        super(new Environment(entry),
              org.openide.util.lookup.Lookups.singleton(entry.getDataObject()));
        this.myEntry = entry;
    }
    
    /** 
     * Overrides superclass method.
     * Should test whether all data is saved, and if not, prompt the user
     * to save. Called by my topcomponent when it wants to close its last topcomponent, but the table editor may still be open
     * @return <code>true</code> if everything can be closed
     */
    protected boolean canClose () {
        // if the table is open, can close without worries, don't remove the save cookie
        if (hasOpenedTableComponent()){
            return true;
        }else{
            DataObject propDO = myEntry.getDataObject();
            if (propDO == null || !propDO.isModified()) return true;
            return super.canClose();
        }
    }
    
    /** 
     * Overrides superclass method.
     * @return the {@link CloneableEditor} for this support
     */
    protected CloneableEditor createCloneableEditor() {
        return new PropertiesEditor(this);
    }
    
    /**
     *
     */
    final class FsStatusListener implements FileStatusListener, Runnable {
        
	/**
	 */
	public void annotationChanged(FileStatusEvent ev) {
            if (ev.isNameChange() && ev.hasChanged(myEntry.getFile())) {
                Mutex.EVENT.writeAccess(this);
            }
	}
        
	/**
	 */
	public void run() {
	    updateEditorDisplayNames();
	}
    }
    
    /**
     */
    private void attachStatusListener() {
        if (fsStatusListener != null) {
            return;                 //already attached
        }
        
        FileSystem fs;
        try {
            fs = myEntry.getFile().getFileSystem();
        } catch (FileStateInvalidException ex) {
            ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
            return;
        }
        
        fsStatusListener = new FsStatusListener();
        fs.addFileStatusListener(
                FileUtil.weakFileStatusListener(fsStatusListener, fs));
    }
    
    /**
     */
    private void updateEditorDisplayNames() {
        assert EventQueue.isDispatchThread();
        
        final String title = messageName();
        final String htmlTitle = messageHtmlName();
        Enumeration en = allEditors.getComponents();
        while (en.hasMoreElements()) {
            TopComponent tc = (TopComponent) en.nextElement();
            tc.setDisplayName(title);
            tc.setHtmlDisplayName(htmlTitle);
        }
    }
    
    /**
     */
    protected void initializeCloneableEditor(CloneableEditor editor) {
	((PropertiesEditor) editor).initialize(myEntry);
    }

    /**
     * Overrides superclass method. 
     * Let's the super method create the document and also annotates it
     * with Title and StreamDescription properities.
     * @param kit kit to user to create the document
     * @return the document annotated by the properties
     */
    protected StyledDocument createStyledDocument(EditorKit kit) {
        StyledDocument document = super.createStyledDocument(kit);
        
        // Set additional proerties to document.
        // Set document name property. Used in CloneableEditorSupport.
        document.putProperty(Document.TitleProperty, myEntry.getFile().toString());
        
        // Set dataobject to stream desc property.
        document.putProperty(Document.StreamDescriptionProperty, myEntry.getDataObject());
        
        // hook the document to listen for any changes to update changes by
        // reparsing the document
        document.addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { changed();}
            public void changedUpdate(javax.swing.event.DocumentEvent e) { changed();}            
            public void removeUpdate(javax.swing.event.DocumentEvent e) { changed();}
            private void changed() {
                myEntry.getHandler().autoParse();                
            }
        });
        
        return document;
    }

    /**
     * Reads the file from the stream, filter the guarded section
     * comments, and mark the sections in the editor. Overrides superclass method. 
     * @param document the document to read into
     * @param inputStream the open stream to read from
     * @param editorKit the associated editor kit
     * @throws <code>IOException</code> if there was a problem reading the file
     * @throws <code>BadLocationException</code> should not normally be thrown
     * @see #saveFromKitToStream
     */
    protected void loadFromStreamToKit(StyledDocument document, InputStream inputStream, EditorKit editorKit)
    throws IOException, BadLocationException {
        NewLineReader newLineReader = new NewLineReader(inputStream);
        
        try {
            editorKit.read(newLineReader, document, 0);
            newLineType = newLineReader.getNewLineType();
            if (newLineReader.wasNonAsciiCharacterRead()) {
                document.putProperty(PROP_NON_ASCII_CHAR_READ, Boolean.TRUE);
            }
        } finally {
            newLineReader.close();
        }
    }

    /** 
     * Adds new lines according actual value of <code>newLineType</code> variable.
     * Overrides superclass method.
     * @param document the document to write from
     * @param editorKit the associated editor kit
     * @param ouputStream the open stream to write to
     * @throws IOException if there was a problem writing the file
     * @throws BadLocationException should not normally be thrown
     * @see #loadFromStreamToKit
     */
    protected void saveFromKitToStream(StyledDocument document, EditorKit editorKit, OutputStream outputStream)
    throws IOException, BadLocationException {
        Writer writer = new NewLineWriter(outputStream, newLineType);
        
        try {
            editorKit.write(writer, document, 0, document.getLength());
        } finally {
            writer.flush();
            writer.close();
        }
    }
    
    /** 
     * Adds a save cookie if the document has been marked modified. Overrides superclass method. 
     * @return <code>true</code> if the environment accepted being marked as modified
     *    or <code>false</code> if it refused it and the document should still be unmodified
     */
    protected boolean notifyModified () {
        // Reparse file.
        myEntry.getHandler().autoParse();

        if (super.notifyModified()) {
            ((Environment)env).addSaveCookie();
            
            return true;
        } else {
            return false;
        }
    }
    
    protected Task reloadDocument(){
        Task tsk = super.reloadDocument();
        tsk.addTaskListener(new TaskListener(){
            public void taskFinished(Task task){
                myEntry.getHandler().autoParse();
            }
        });
        return tsk;
    }

    /** Overrides superclass method. Adds checking for opened Table component. */
    protected void notifyUnmodified () {
        super.notifyUnmodified();
        
        ((Environment)env).removeSaveCookie();
    }
    
    /**
     */
    public void open() {
	super.open();
	attachStatusListener();
    }
    
    /** Overrides superclass method. Adds checking for opened Table panel. */
    protected void notifyClosed() {
        // Close document only in case there is not open table editor.
        if(!hasOpenedTableComponent()) {
            boolean wasModified = isModified();
            super.notifyClosed();
            if (wasModified) {
                // #21850. Don't reparse invalid or virtual file.
                if(myEntry.getFile().isValid() && !myEntry.getFile().isVirtual()) {
                    myEntry.getHandler().reparseNowBlocking();
                }
            }
        }
    }

    /** 
     * Overrides superclass abstract method. 
     * Message to display when an object is being opened.
     * @return the message or null if nothing should be displayed
     */
    protected String messageOpening() {
        String name = myEntry.getDataObject().getPrimaryFile().getName()+"("+Util.getLocaleLabel(myEntry)+")"; // NOI18N
        
        return NbBundle.getMessage(
            PropertiesEditorSupport.class,
            "LBL_ObjectOpen", // NOI18N
            name
        );
    }
    
    /**
     * Overrides superclass abstract method. 
     * Message to display when an object has been opened.
     * @return the message or null if nothing should be displayed
     */
    protected String messageOpened() {
        String name = myEntry.getDataObject().getPrimaryFile().getName()+"("+Util.getLocaleLabel(myEntry)+")"; // NOI18N        
        
        return NbBundle.getMessage(
            PropertiesEditorSupport.class,
            "LBL_ObjectOpened", // NOI18N
            name
       );
    }
    
    /**
     */
    private String getRawMessageName() {
        return myEntry.getDataObject().getName()        
               + '(' + Util.getLocaleLabel(myEntry) + ')';
    }
    
    /**
     */
    private String addModifiedInfo(String name) {
        int version = isModified() ? (myEntry.getFile().canWrite() ? 1 : 2)
                                   : (myEntry.getFile().canWrite() ? 3 : 0);
        return NbBundle.getMessage(PropertiesEditorSupport.class,
                                   "LBL_EditorName",                    //NOI18N
                                   new Integer(version),
                                   name);
    }
    
    /** 
     * Overrides superclass abstract method. 
     * Constructs message that should be used to name the editor component.
     * @return name of the editor
     */
    protected String messageName () {
        if (!myEntry.getDataObject().isValid()) {
            return "";                                                  //NOI18N       
        }
        
        return addModifiedInfo(getRawMessageName());
    }

    /** */
    protected String messageHtmlName () {
        if (!myEntry.getDataObject().isValid()) {
            return null;
        }

        String rawName = getRawMessageName();
        
        String annotatedName = null;
        final FileObject entry = myEntry.getFile();
        try {
            FileSystem.Status status = entry.getFileSystem().getStatus();
            if (status != null) {
                Set<FileObject> files = Collections.singleton(entry);
                if (status instanceof FileSystem.HtmlStatus) {
                    FileSystem.HtmlStatus hStatus = (FileSystem.HtmlStatus)
                                                    status;
                    annotatedName = hStatus.annotateNameHtml(rawName, files);
                    if (rawName.equals(annotatedName)) {
                        annotatedName = null;
                    }
                    if ((annotatedName != null)
                            && (!annotatedName.startsWith("<html>"))) { //NOI18N
                        annotatedName = "<html>" + annotatedName;       //NOI18N
                    }
                }
                if (annotatedName == null) {
                    annotatedName = status.annotateName(rawName, files);
                }
            }
        } catch (FileStateInvalidException ex) {
            //do nothing and fall through
        }
        
        String name = (annotatedName != null) ? annotatedName : rawName;
        return addModifiedInfo(name);
    }
    
    /** 
     * Overrides superclass abstract method.
     * Is modified and is being closed.
     * @return text to show to the user
     */
    protected String messageSave () {
        String name = myEntry.getDataObject().getPrimaryFile().getName()+"("+Util.getLocaleLabel(myEntry)+")"; // NOI18N        
        
        return NbBundle.getMessage (
            PropertiesEditorSupport.class,
            "MSG_SaveFile", // NOI18N
            name
        );
    }
    
    /** 
     * Overrides superclass abstract method.
     * Text to use as tooltip for component.
     * @return text to show to the user
     */
    protected String messageToolTip () {
        // copied from DataEditorSupport, more or less
        FileObject fo = myEntry.getFile();
        return FileUtil.getFileDisplayName(fo);
    }
    
    /** Overrides superclass method. Gets <code>UndoRedo</code> manager which maps 
     * <code>UndoalbleEdit</code>'s to <code>StampFlag</code>'s. */
    protected UndoRedo.Manager createUndoRedoManager () {
        return new UndoRedoStampFlagManager();
    }
    
    /** 
     * Helper method. Hack on superclass <code>getUndoRedo()</code> method, to widen its protected modifier. 
     * Needs to be accessed from outside this class (in <code>PropertiesOpen</code>). 
     * @see PropertiesOpen 
     */
    UndoRedo.Manager getUndoRedoManager() {
        return super.getUndoRedo();
    }
    
    /** 
     * Helper method. Used only by <code>PropertiesOpen</code> support when closing last Table component.
     * Note: It's quite ugly by-pass of <code>notifyClosed()</code> method. Should be revised. 
     */
    void forceNotifyClosed() {
        super.notifyClosed();
    }
    
    /** Helper method. 
     * @return <code>newLineType</code>.*/
    byte getNewLineType() {
        return newLineType;
    }
    
    /** Helper method. Saves this entry. */
    private void saveThisEntry() throws IOException {
        super.saveDocument();
        // #32777 - it can happen that save operation was interrupted
        // and file is still modified. Mark it unmodified only when it is really
        // not modified.
        if (!env.isModified()) {
            myEntry.setModified(false);
        }
    }
    
    /** Helper method. 
     * @return whether there is an table view opened */
    public synchronized boolean hasOpenedTableComponent() {
        return ((PropertiesDataObject)myEntry.getDataObject()).getOpenSupport().hasOpenedTableComponent();
    }
    
    /**
     * Helper method.
     * @return whether there is an open editor component. */
    public synchronized boolean hasOpenedEditorComponent() {
        Enumeration en = allEditors.getComponents ();
        return en.hasMoreElements ();
    }

    /** Class which exist only due comaptibility with version 3.0. */    
    private static final class Env extends Environment {
        /** Generated Serialized Version UID. */
        static final long serialVersionUID = -9218186467757330339L;

        /** Used for deserialization. */
        private PropertiesFileEntry entry;

        /** */
        public Env(PropertiesFileEntry entry) {
            super(entry);
        }

        /** Adds passing entry field to superclass. */
        private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
                in.defaultReadObject();
                
                if(this.entry != null)
                    super.entry = this.entry;
        }
    }

    
    /** Nested class. Implementation of <code>ClonableEditorSupport.Env</code> interface. */
    private static class Environment implements CloneableEditorSupport.Env,
    PropertyChangeListener, SaveCookie {
        
        /** generated Serialized Version UID */
        static final long serialVersionUID = 354528097109874355L;
            
        /** Entry on which is support build. */
        protected PropertiesFileEntry entry;
            
        /** Lock acquired after the first modification and used in <code>save</code> method. */
        private transient FileLock fileLock;
            
        /** Spport for firing of property changes. */
        private transient PropertyChangeSupport propSupp;
        
        /** Support for firing of vetoable changes. */
        private transient VetoableChangeSupport vetoSupp;
            
            
        /** Constructor.
         * @param obj this support should be associated with
         */
        public Environment (PropertiesFileEntry entry) {
            this.entry = entry;
            entry.getFile().addFileChangeListener(new EnvironmentListener(this));
            entry.addPropertyChangeListener(this);
        }

        /** Implements <code>CloneableEditorSupport.Env</code> inetrface. Adds property listener. */
        public void addPropertyChangeListener(PropertyChangeListener l) {
            prop().addPropertyChangeListener (l);
        }

        
        /** Accepts property changes from entry and fires them to own listeners. */
        public void propertyChange(PropertyChangeEvent evt) {
            // We will handle the object invalidation here.
            if(DataObject.PROP_VALID.equals(evt.getPropertyName ())) { 
                // do not check it if old value is not true
                if(Boolean.FALSE.equals(evt.getOldValue())) return;

                // loosing validity
                PropertiesEditorSupport support = (PropertiesEditorSupport)findCloneableOpenSupport();
                if(support != null) {
                    
                    // mark the object as not being modified, so nobody
                    // will ask for save
                    unmarkModified();

                    support.close(false);
                }
            } else {
                firePropertyChange (
                    evt.getPropertyName(),
                    evt.getOldValue(),
                    evt.getNewValue()
                );
            }
        }
        
        /** Implements <code>CloneableEditorSupport.Env</code> inetrface. Removes property listener. */
        public void removePropertyChangeListener(PropertyChangeListener l) {
            prop().removePropertyChangeListener (l);
        }
            
        /** Implements <code>CloneableEditorSupport.Env</code> inetrface. Adds veto listener. */
        public void addVetoableChangeListener(VetoableChangeListener l) {
            veto().addVetoableChangeListener (l);
        }
            
        /** Implements <code>CloneableEditorSupport.Env</code> inetrface. Removes veto listener. */
        public void removeVetoableChangeListener(VetoableChangeListener l) {
            veto().removeVetoableChangeListener (l);
        }

        /** Overrides superclass method.
         * Note: in fact it returns <code>CloneableEditorSupport</code> instance.
         * @return the support or null if the environemnt is not in valid
         * state and the CloneableOpenSupport cannot be found for associated
         * entry object
         */
        public CloneableOpenSupport findCloneableOpenSupport() {
            return (PropertiesEditorSupport)entry.getCookieSet().getCookie(EditCookie.class);
        }

        /**
         * Implements <code>CloneableEditorSupport.Env</code> interface.
         * Test whether the support is in valid state or not.
         * It could be invalid after deserialization when the object it
         * referenced to does not exist anymore.
         * @return true or false depending on its state
         */
        public boolean isValid() {
            return entry.getDataObject().isValid();
        }
            
        /**
         * Implements <code>CloneableEditorSupport.Env</code> interface.
         * Test whether the object is modified or not.
         * @return true if the object is modified
         */
        public boolean isModified() {
            return entry.isModified();
        }

        /**
         * Implements <code>CloneableEditorSupport.Env</code> interface.
         * First of all tries to lock the primary file and
         * if it succeeds it marks the data object modified.
         * @exception IOException if the environment cannot be marked modified
         *   (for example when the file is readonly), when such exception
         *   is the support should discard all previous changes
         */
        public void markModified() throws java.io.IOException {
            if (fileLock == null || !fileLock.isValid()) {
                fileLock = entry.takeLock();
            }
            
            entry.setModified(true);
        }
            
        /**
         * Implements <code>CloneableEditorSupport.Env</code> interface.
         * Reverse method that can be called to make the environment
         * unmodified.
         */
        public void unmarkModified() {
            if (fileLock != null && fileLock.isValid()) {
                fileLock.releaseLock();
            }
            
            entry.setModified(false);
        }

        /**
         * Implements <code>CloneableEditorSupport.Env</code> interface.
         * Mime type of the document.
         * @return the mime type to use for the document
         */
        public String getMimeType() {
            return entry.getFile().getMIMEType();
        }
            
        /**
         * Implements <code>CloneableEditorSupport.Env</code> interface.
         * The time when the data has been modified. */
        public Date getTime() {
            // #32777 - refresh file object and return always the actual time
            entry.getFile().refresh(false);
            return entry.getFile().lastModified();
        }
            
        /**
         * Implements <code>CloneableEditorSupport.Env</code> interface.
         * Obtains the input stream.
         * @exception IOException if an I/O error occures
         */
        public InputStream inputStream() throws IOException {
            return entry.getFile().getInputStream();
        }
            
        /**
         * Implements <code>CloneableEditorSupport.Env</code> interface.
         * Obtains the output stream.
         * @exception IOException if an I/O error occures
         */
        public OutputStream outputStream() throws IOException {
            return entry.getFile().getOutputStream(fileLock);
        }

        /**
         * Implements <code>SaveCookie</code> interface. 
         * Invoke the save operation.
         * @throws IOException if the object could not be saved
         */
        public void save() throws IOException {
            // Do saving job. Note it gets editor support, not open support.
            ((PropertiesEditorSupport)findCloneableOpenSupport()).saveThisEntry();
        }
            
        /** Fires property change.
         * @param name the name of property that changed
         * @param oldValue old value
         * @param newValue new value
         */
        private void firePropertyChange (String name, Object oldValue, Object newValue) {
            prop().firePropertyChange (name, oldValue, newValue);
        }
            
        /** Fires vetoable change.
         * @param name the name of property that changed
         * @param oldValue old value
         * @param newValue new value
         */
        private void fireVetoableChange (String name, Object oldValue, Object newValue) throws PropertyVetoException {
                veto ().fireVetoableChange (name, oldValue, newValue);
        }
            
        /** Lazy getter for property change support. */
        private PropertyChangeSupport prop() {
            if (propSupp == null) {
                synchronized (this) {
                    if (propSupp == null) {
                        propSupp = new PropertyChangeSupport (this);
                    }
                }
            }
            
            return propSupp;
        }
            
        /** Lazy getter for vetoable support. */
        private VetoableChangeSupport veto() {
            if (vetoSupp == null) {
                synchronized (this) {
                    if (vetoSupp == null) {
                        vetoSupp = new VetoableChangeSupport (this);
                    }
                }
            }
            return vetoSupp;
        }
            
        /** Helper method. Adds save cookie to the entry. */
        private void addSaveCookie() {
            if (entry.getCookie(SaveCookie.class) == null) {
                entry.getCookieSet().add(this);
            }
            ((PropertiesDataObject)entry.getDataObject()).updateModificationStatus();
        }
            
        /** Helper method. Removes save cookie from the entry. */
        private void removeSaveCookie() {
            // remove Save cookie from the entry
            SaveCookie sc = (SaveCookie)entry.getCookie(SaveCookie.class);
            
            if (sc != null && sc.equals(this)) {
                entry.getCookieSet().remove(this);
            }
            
            PropertiesRequestProcessor.getInstance().post(new Runnable() {
                public void run() {
                    ((PropertiesDataObject)entry.getDataObject()).updateModificationStatus();
                }
            });
        }
            
        /** Called from the <code>EnvironmnetListener</code>
         * @param expected is the change expected
         * @param time of the change
         */
        private void fileChanged(boolean expected, long time) {
            if (expected) {
                // newValue = null means do not ask user whether to reload
                firePropertyChange (PROP_TIME, null, null);
            } else {
                firePropertyChange (PROP_TIME, null, new Date (time));
            }
        }

        
        /** Called from the <code>EnvironmentListener</code>.
         * The components are going to be closed anyway and in case of
         * modified document its asked before if to save the change. */
        private void fileRemoved() {
            try {
                fireVetoableChange(PROP_VALID, Boolean.TRUE, Boolean.FALSE);
            } catch(PropertyVetoException pve) {
                // Ignore it and close anyway. File doesn't exist anymore.
            }
            
            firePropertyChange(PROP_VALID, Boolean.TRUE, Boolean.FALSE);
        }
    } // End of nested class Environment.

    
    /** Weak listener on file object that notifies the <code>Environment</code> object
     * that a file has been modified. */
    private static final class EnvironmentListener extends FileChangeAdapter {
        
        /** Reference of <code>Environment</code> */
        private Reference<Environment> reference;
        
        /** @param environment <code>Environment<code> to use
         */
        public EnvironmentListener(Environment environment) {
            reference = new WeakReference<Environment>(environment);
        }
        
        /** Fired when a file is changed.
         * @param fe the event describing context where action has taken place
         */
        public void fileChanged(FileEvent evt) {
            Environment environment = reference.get();
            if (environment != null) {
                if(!environment.entry.getFile().equals(evt.getFile()) ) {
                    // If the FileObject was changed.
                    // Remove old listener from old FileObject.
                    evt.getFile().removeFileChangeListener(this);
                    // Add new listener to new FileObject.
                    environment.entry.getFile().addFileChangeListener(new EnvironmentListener(environment));
                    return;
                }

                // #16403. See DataEditorSupport.EnvListener.
                if(evt.getFile().isVirtual()) {
                    environment.entry.getFile().removeFileChangeListener(this);
                    // File doesn't exist on disk -> simulate env is invalid,
                    // even the fileObject could be valid, see VCS FS.
                    environment.fileRemoved();
                    environment.entry.getFile().addFileChangeListener(this);
                } else {
                    environment.fileChanged(evt.isExpected(), evt.getTime());
                }
            }
        }
    } // End of nested class EnvironmentListener.

    
    /** Inner class for opening editor view at a given key. */
    public class PropertiesEditAt implements EditCookie {

        /** Key at which should be pane opened. (Cursor will be at the position of that key). */
        private String key;
        
        
        /** Constructor. */
        PropertiesEditAt(String key) {
            this.key   = key;
        }
        
        
        /** Setter for <code>key</code>. */
        public void setKey(String key) {
            this.key = key;
        }
        
        /** Implementation of <code>EditCookie</code> interface. */
        public void edit() {
            PropertiesEditor editor = (PropertiesEditor)PropertiesEditorSupport.super.openCloneableTopComponent();
            editor.requestActive();
            
            Element.ItemElem item = myEntry.getHandler().getStructure().getItem(key);
            if (item != null) {
                int offset = item.getKeyElem().getBounds().getBegin().getOffset();
                if (editor.getPane() != null && editor.getPane().getCaret() !=null)
                    editor.getPane().getCaret().setDot(offset);
            }
        }
    } // End of inner class PropertiesEditAt.

    
    /** Cloneable top component to hold the editor kit. */
    public static class PropertiesEditor extends CloneableEditor
                                         implements Runnable {
        
        /** Holds the file being edited. */
        protected transient PropertiesFileEntry entry;
        
        /** Listener for entry's save cookie changes. */
        private transient PropertyChangeListener saveCookieLNode;
        
        /** */
        private transient boolean hasBeenActivated = false;
        
        /** Generated serial version UID. */
        static final long serialVersionUID =-2702087884943509637L;
        
        
        /** Constructor for deserialization */
        public PropertiesEditor() {
            super();
        }
        
        /** Creates new editor */
        public PropertiesEditor(PropertiesEditorSupport support) {
            super(support);
        }

        
        /** Initializes object, used in construction and deserialization. */
        private void initialize(PropertiesFileEntry entry) {
            this.entry = entry;
            
            Node n = entry.getNodeDelegate ();
            setActivatedNodes (new Node[] { n });
            
            updateName();
            
            // entry to the set of listeners
            saveCookieLNode = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (Node.PROP_COOKIE.equals(evt.getPropertyName()) ||
                    DataObject.PROP_NAME.equals(evt.getPropertyName())) {
                        PropertiesEditor.super.updateName();
                    }
                }
            };
            this.entry.addPropertyChangeListener(
            WeakListeners.propertyChange(saveCookieLNode, this.entry));
        }
        
        /**
         */
        protected void componentActivated() {
            super.componentActivated();
            if (!hasBeenActivated) {
                hasBeenActivated = true;
                if (Boolean.TRUE.equals(getEditorPane().getDocument().getProperty(
                                                       PROP_NON_ASCII_CHAR_READ))) {
                    EventQueue.invokeLater(this);
                }
            }
        }
        
        /**
         */
        public void run() {
            String msg = NbBundle.getMessage(getClass(),
                                             "MSG_OnlyLatin1Supported");//NOI18N
            Object msgObject;
            int nlIndex = msg.indexOf('\n');
            if (nlIndex == -1) {
                msgObject = msg;
            } else {
                StringBuilder buf = new StringBuilder(msg.length() + 40);
                buf.append("<html>");                                   //NOI18N

                int lastNlIndex = -1;
                do {
                    buf.append(msg.substring(lastNlIndex + 1, nlIndex));
                    buf.append("<br>");                                 //NOI18N
                    lastNlIndex = nlIndex;
                    nlIndex = msg.indexOf('\n', lastNlIndex + 1);
                } while (nlIndex != -1);
                buf.append(msg.substring(lastNlIndex + 1));
                msgObject = new JLabel(buf.toString());
            }
            
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(
                            msgObject,
                            NotifyDescriptor.WARNING_MESSAGE));
        }

        /**
         * Overrides superclass method. 
         * When closing last view, also close the document.
         * @return <code>true</code> if close succeeded
         */
        protected boolean closeLast () {
            return super.closeLast();
        }

        /** Overrides superclass method. Gets <code>Icon</code>. */
        public Image getIcon () {
            return Utilities.loadImage("org/netbeans/modules/properties/propertiesLocale.gif"); // NOI18N
        }
        
        /** Overrides superclass method. Gets help context. */
        public HelpCtx getHelpCtx() {
            return new HelpCtx(Util.HELP_ID_EDITLOCALE);
        }
        
        /** Getter for pane. */
        private JEditorPane getPane() {
            return pane;
        }
    } // End of nested class PropertiesEditor.
    

    /** This stream is able to filter various new line delimiters and replace them by \n. */
    static class NewLineReader extends BufferedReader {
        
        /** The count of types new line delimiters used in the file */
        int[] newLineTypes;
        /** */
        private boolean nonAsciiCharacterRead = false;
        
        
        /** Creates new stream.
         * @param is encapsulated input stream.
         * @param justFilter The flag determining if this stream should
         *        store the guarded block information. True means just filter,
         *        false means store the information.
         */
        public NewLineReader(InputStream is) throws IOException {
            super(new InputStreamReader(is, "8859_1")); // NOI18N
            
            newLineTypes = new int[] { 0, 0, 0 };
        }

        
        /** Overrides superclass method. Reads one character.
         * @return next char or -1 if the end of file was reached.
         * @exception IOException if any problem occured.
         */
        public int read() throws IOException {
            int nextToRead = super.read();
            
            if (nextToRead == -1)
                return -1;
            
            if (nextToRead == '\r') {
                nextToRead = super.read();
                
                while (nextToRead == '\r')
                    nextToRead = super.read();
                if (nextToRead == '\n') {
                    nextToRead = super.read();
                    newLineTypes[NEW_LINE_RN]++;
                    return '\n';
                } else {
                    newLineTypes[NEW_LINE_R]++;
                    return '\n';
                }
            }
            if (nextToRead == '\n') {
                nextToRead = super.read();
                newLineTypes[NEW_LINE_N]++;
                return '\n';
            }
            if (nextToRead > 0x7e) {
                nonAsciiCharacterRead = true;
            }
            
            return nextToRead;
        }

        /**
         * {@inheritdoc}
         */
        public int read(char cbuf[], int off, int len) throws IOException {
            int charsCount = super.read(cbuf, off, len);
            if (!nonAsciiCharacterRead && (charsCount > 0)) {
                final int upperLimit = off + len;
                for (int i = off; i < upperLimit; i++) {
                    if (cbuf[i] > 0x7e) {
                        nonAsciiCharacterRead = true;
                        break;
                    }
                }
            }
            return charsCount;
        }
        
        /** Gets new line type. */
        public byte getNewLineType() {
            if (newLineTypes[0] > newLineTypes[1]) {
                return (newLineTypes[0] > newLineTypes[2]) ? NEW_LINE_N : NEW_LINE_RN;
            } else {
                return (newLineTypes[1] > newLineTypes[2]) ? NEW_LINE_R : NEW_LINE_RN;
            }
        }
        
        /**
         */
        public boolean wasNonAsciiCharacterRead() {
            return nonAsciiCharacterRead;
        }
        
    } // End of nested class NewLineReader.
    
    
    /** This stream is used for changing the new line delimiters.
     * Replaces the '\n' by '\n', '\r' or "\r\n". */
    static class NewLineWriter extends BufferedWriter {
        
        /** The type of new line delimiter */
        byte newLineType;

        
        /** Creates new stream.
         * @param stream Underlaying stream
         * @param newLineType The type of new line delimiter
         */
        public NewLineWriter(OutputStream stream, byte newLineType) throws UnsupportedEncodingException {
            super(new OutputStreamWriter(stream, "8859_1"));
            
            this.newLineType = newLineType;
        }
        
        
        /** Write one character. New line char replaces according the <code>newLineType</code> character.
         * @param character character to write.
         */
        public void write(int character) throws IOException {
            if(character == '\r')
                // Do nothing.
                return;
            if(character == '\n') {
                if(newLineType == NEW_LINE_R) {
                    // Replace new line by \r.
                    super.write('\r');
                } else if(newLineType == NEW_LINE_N) {
                    // Replace new line by \n.
                    super.write('\n');
                } else if(newLineType == NEW_LINE_RN) {
                    // Replace new line by \r\n.
                    super.write('\r');
                    super.write('\n');
                }
            } else {
                super.write(character);
            }
        }
        
    } // End of nested class NewLineWriter.
    
    
    /** Inner class. UndoRedo manager which saves a StampFlag
     * for each UndoAbleEdit.
     */
    class UndoRedoStampFlagManager extends UndoRedo.Manager {
        
        /** Hash map of weak reference keys (UndoableEdit's) to their StampFlag's. */
        WeakHashMap<UndoableEdit,StampFlag> stampFlags
                = new WeakHashMap<UndoableEdit,StampFlag>(5);
        
        /** Overrides superclass method. Adds StampFlag to UndoableEdit. */
        public synchronized boolean addEdit(UndoableEdit anEdit) {
            stampFlags.put(anEdit, new StampFlag(System.currentTimeMillis(),
                ((PropertiesDataObject)PropertiesEditorSupport.this.myEntry.getDataObject()).getOpenSupport().atomicUndoRedoFlag ));
            return super.addEdit(anEdit);
        }
        
        /** Overrides superclass method. Adds StampFlag to UndoableEdit. */
        public boolean replaceEdit(UndoableEdit anEdit) {
            stampFlags.put(anEdit, new StampFlag(System.currentTimeMillis(), 
                ((PropertiesDataObject)PropertiesEditorSupport.this.myEntry.getDataObject()).getOpenSupport().atomicUndoRedoFlag ));
            return super.replaceEdit(anEdit);
        }
        
        /** Overrides superclass method. Updates time stamp for the edit. */
        public synchronized void undo() throws CannotUndoException {
            UndoableEdit anEdit = editToBeUndone();
            if(anEdit != null) {
                Object atomicFlag = stampFlags.get(anEdit).getAtomicFlag(); // atomic flag remains
                super.undo();
                stampFlags.put(anEdit, new StampFlag(System.currentTimeMillis(), atomicFlag));
            }
        }
        
        /** Overrides superclass method. Updates time stamp for that edit. */
        public synchronized void redo() throws CannotRedoException {
            UndoableEdit anEdit = editToBeRedone();
            if(anEdit != null) {
                Object atomicFlag = stampFlags.get(anEdit).getAtomicFlag(); // atomic flag remains
                super.redo();
                stampFlags.put(anEdit, new StampFlag(System.currentTimeMillis(), atomicFlag));
            }
        }
        
        /** Method which gets time stamp of next Undoable edit to be undone. 
         * @ return time stamp in milliseconds or 0 (if don't exit edit to be undone). */
        public long getTimeStampOfEditToBeUndone() {
            UndoableEdit nextUndo = editToBeUndone();
            if (nextUndo == null) {
                return 0L;
            } else {
                return stampFlags.get(nextUndo).getTimeStamp();
            }
        }
        
        /** Method which gets time stamp of next Undoable edit to be redone.
         * @ return time stamp in milliseconds or 0 (if don't exit edit to be redone). */
        public long getTimeStampOfEditToBeRedone() {
            UndoableEdit nextRedo = editToBeRedone();
            if (nextRedo == null) {
                return 0L;
            } else {
                return stampFlags.get(nextRedo).getTimeStamp();
            }
        }
        
        /** Method which gets atomic flag of next Undoable edit to be undone. 
         * @ return atomic flag in milliseconds or 0 (if don't exit edit to be undone). */
        public Object getAtomicFlagOfEditToBeUndone() {
            UndoableEdit nextUndo = editToBeUndone();
            if (nextUndo == null) {
                return null;
            } else {
                return (stampFlags.get(nextUndo)).getAtomicFlag();
            }
        }
        
        /** Method which gets atomic flag of next Undoable edit to be redone.
         * @ return time stamp in milliseconds or 0 (if don't exit edit to be redone). */
        public Object getAtomicFlagOfEditToBeRedone() {
            UndoableEdit nextRedo = editToBeRedone();
            if (nextRedo == null) {
                return null;
            } else {
                return (stampFlags.get(nextRedo)).getAtomicFlag();
            }
        }
        
    } // End of inner class UndoRedoTimeStampManager.

    /** Simple nested class for storing time stamp and atomic flag used 
     * in <code>UndoRedoStampFlagManager</code>.
     */
    static class StampFlag {
        
        /** Time stamp when was an UndoableEdit (to which is this class mapped via 
         * UndoRedoStampFlagManager,) was created, replaced, undone, or redone. */
        private long timeStamp;
        
        /** Atomic flag. If this object is not null it means that an UndoableEdit ( to which
         * is this class mapped via UndoRedoStampFlagManager,) was created as part of one 
         * action which could consist from more UndoableEdits in differrent editor supports.
         * These Undoable edits are marked with this (i.e. same) object. */
        private Object atomicFlag;
        
        /** Consructor. */
        public StampFlag(long timeStamp, Object atomicFlag) {
            this.timeStamp = timeStamp;
            this.atomicFlag = atomicFlag;            
        }
        
        /** Getter for time stamp. */
        public long getTimeStamp() {
            return timeStamp;
        }
        
        /** Setter for time stamp. */
        public void setTimeStamp(long timeStamp) {
            this.timeStamp = timeStamp;
        }
        
        /** Getter for atomic flag.
         @ return Returns null if is not linked with more Undoable edits.*/
        public Object getAtomicFlag() {
            return atomicFlag;
        }
    } // End of nested class TimeStamp.
}
