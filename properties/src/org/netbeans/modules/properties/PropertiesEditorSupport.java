/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.properties;


import java.beans.*;
import java.io.*;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.WeakHashMap;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import org.openide.awt.UndoRedo;
import org.openide.cookies.EditCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.Node;
import org.openide.NotifyDescriptor;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.TopManager;
import org.openide.util.NbBundle;
import org.openide.util.WeakListener;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.CloneableTopComponent;


/** Support for viewing porperties files (EditCookie) by opening them in a text editor */
public class PropertiesEditorSupport extends CloneableEditorSupport 
implements EditCookie, PrintCookie, Serializable {
    
    /** New lines in this file was delimited by '\n'. */
    private static final byte NEW_LINE_N = 0;
    /** New lines in this file was delimited by '\r'. */
    private static final byte NEW_LINE_R = 1;
    /** New lines in this file was delimited by '\r\n'. */
    private static final byte NEW_LINE_RN = 2;
    
    /** The type of new lines. */
    private byte newLineType = NEW_LINE_N;
    
    /** Visible view of underlying file entry */
    transient PropertiesFileEntry myEntry;
    
    /** Generated serial version UID. */
    static final long serialVersionUID =1787354011149868490L;
    
    
    /** Constructor. */
    public PropertiesEditorSupport(PropertiesFileEntry entry) {
        super (new Environment(entry));
        this.myEntry = entry;
        initialize();
    }
    
    
    /** Sets MIME type for this support. */
    public void initialize() {
        setMIMEType (PropertiesDataObject.MIME_PROPERTIES);
    }

    /** Overrides superclass method. Closes component. */
    public boolean close() {
        SaveCookie savec = (SaveCookie) myEntry.getCookie(SaveCookie.class);
        if ((savec != null) && hasOpenedTableComponent()) {
            return false;
        }
        
        if (!super.close())
            return false;
        
        myEntry.getHandler().reparseNowBlocking();
        return true;
    }
    
    /** 
     * Overrides superclass method.
     * Should test whether all data is saved, and if not, prompt the user
     * to save. Called by my topcomponent when it wants to close its last topcomponent, but the table editor may still be open
     * @return <code>true</code> if everything can be closed
     */
    protected boolean canClose () {
        SaveCookie savec = (SaveCookie) myEntry.getCookie(SaveCookie.class);
        if (savec != null) {
            // if the table is open, can close without worries, don't remove the save cookie
            if (hasOpenedTableComponent())
                return true;
            
            // PENDING - is not thread safe
            MessageFormat format = new MessageFormat(NbBundle.getBundle(PropertiesEditorSupport.class).getString("MSG_SaveFile"));
            String msg = format.format(new Object[] { myEntry.getFile().getName()});
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.YES_NO_CANCEL_OPTION);
            Object ret = TopManager.getDefault().notify(nd);
            
            // cancel
            if (NotifyDescriptor.CANCEL_OPTION.equals(ret))
                return false;
            
            // yes
            if (NotifyDescriptor.YES_OPTION.equals(ret)) {
                try {
                    savec.save();
                } catch (IOException e) {
                    TopManager.getDefault().notifyException(e);
                    return false;
                }
            }
            
            // no
            if (NotifyDescriptor.NO_OPTION.equals(ret)) {
                return true;
            }
            
        }
        return true;
    }
    
    /** 
     * Overrides superclass method.
     * @return the {@link CloneableEditor} for this support
     */
    protected CloneableEditor createCloneableEditor() {
        return new PropertiesEditor(this);
    }
    
    /**
     * Overrides superclass method.
     * A method to create a new component. Overridden in subclasses.
     * @return the {@link Editor} for this support
     */
    protected CloneableTopComponent createCloneableTopComponent () {
        // initializes the document if not initialized
        prepareDocument();
        
        CloneableEditor editor = new PropertiesEditor(this);
        return editor;
    }

    /**
     * Overrides superclass method. 
     * Let's the super method create the document and also annotates it
     * with Title and StreamDescription properities.
     * @param kit kit to user to create the document
     * @return the document annotated by the properties
     */
    protected StyledDocument createStyledDocument(EditorKit kit) {
        StyledDocument doc = super.createStyledDocument(kit);
        
        // Set additional proerties to document.
        // Set document name property. Used in CloneableEditorSupport.
        doc.putProperty(Document.TitleProperty, myEntry.getFile().getPackageNameExt('/', '.'));
        
        // Set dataobject to stream desc property.
        doc.putProperty(Document.StreamDescriptionProperty, myEntry.getDataObject());
        
        return doc;
    }

    /**
     * Overrides superclass method. 
     * Read the file from the stream, filter the guarded section
     * comments, and mark the sections in the editor.
     * @param doc the document to read into
     * @param stream the open stream to read from
     * @param kit the associated editor kit
     * @throws <code>IOException</code> if there was a problem reading the file
     * @throws <code>BadLocationException</code> should not normally be thrown
     * @see #saveFromKitToStream
     */
    protected void loadFromStreamToKit (StyledDocument doc, InputStream stream, EditorKit kit) throws IOException, BadLocationException {
        NewLineInputStream is = new NewLineInputStream(stream);
        try {
            kit.read(is, doc, 0);
            newLineType = is.getNewLineType();
        } finally {
            is.close();
        }
    }

    /** 
     * Overrides superclass method.
     * Adds new lines according actual value of <code>newLineType</code> vraiable.
     * @param doc the document to write from
     * @param kit the associated editor kit
     * @param stream the open stream to write to
     * @throws IOException if there was a problem writing the file
     * @throws BadLocationException should not normally be thrown
     * @see #loadFromStreamToKit
     */
    protected void saveFromKitToStream(StyledDocument doc, EditorKit kit, OutputStream stream) throws IOException, BadLocationException {
        OutputStream os = new NewLineOutputStream(stream, newLineType);
        try {
            kit.write(os, doc, 0, doc.getLength());
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                }
            }
        }
    }
    
    /** 
     * Overrides superclass method. Adds a save cookie if the document has been marked modified.
     * @return true if the environment accepted being marked as modified
     *    or false if it refused it and the document should still be unmodified
     */
    protected boolean notifyModified () {
        // reparse file
        myEntry.getHandler().autoParse();
        
        if (super.notifyModified()) {
            
            ((Environment)env).addSaveCookie();
            
            return true;
        } else {
            return false;
        }
    }

    /** Overrides superclass method. Adds checking for opened Table component. */
    protected void notifyUnmodified () {
        super.notifyUnmodified();
        
        ((Environment)env).removeSaveCookie();
    }
    
    /** Overrides superclass method. Adds checking for opened Table panel. */
    protected void notifyClosed() {
        // Close document only in case there is not open table editor.
        if(!hasOpenedTableComponent()) {
            super.notifyClosed();
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
            CloneableEditorSupport.class, 
            "CTL_ObjectOpen", // NOI18N
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
            CloneableEditorSupport.class,
            "CTL_ObjectOpened", // NOI18N
            name
       );
    }

    /** 
     * Overrides superclass abstract method. 
     * Constructs message that should be used to name the editor component.
     * @return name of the editor
     */
    protected String messageName () {
        String name = myEntry.getDataObject().getPrimaryFile().getName()+"("+Util.getLocaleLabel(myEntry)+")"; // NOI18N
        
        if(isModified()) {
            return NbBundle.getMessage (
                CloneableEditorSupport.class,
                "LAB_EditorName_Modified", // NOI18N
                name
            );
        } else {
            return NbBundle.getMessage (
                CloneableEditorSupport.class,
                "LAB_EditorName_Uptodate", // NOI18N
                name
            );
        }
    }
    
    /** 
     * Overrides superclass abstract method.
     * Is modified and is being closed.
     * @return text to show to the user
     */
    protected String messageSave () {
        String name = myEntry.getDataObject().getPrimaryFile().getName()+"("+Util.getLocaleLabel(myEntry)+")"; // NOI18N        
        
        return NbBundle.getMessage (
            CloneableEditorSupport.class,
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
        // update tooltip
        FileObject fo = myEntry.getFile();
        
        return NbBundle.getMessage (
            CloneableEditorSupport.class,
            "LAB_EditorToolTip", // NOI18N
            fo.getPackageName ('.'),
            fo.getExt ()
        );
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
        myEntry.setModified(false);
    }
    
    /** Helper method. Sets <code>CloneableTopComponent.Ref</code> for this support. 
     * @see org.openide.windows.CloneableTopComponent.Ref */
    private void setRef(CloneableTopComponent.Ref ref) {
        allEditors = ref;
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
   
    
    /** Nested class. Implementation of <code>ClonableEditorSupport.Env</code> interface. */
    private static final class Environment implements CloneableEditorSupport.Env, SaveCookie {
        
        /** generated Serialized Version UID */
        static final long serialVersionUID = 354528097109874355L;
            
        /** Entry on which is support build. */
        private PropertiesFileEntry entry;
            
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
        }

        /** Implements <code>CloneableEditorSupport.Env</code> inetrface. Adds property listener. */
        public void addPropertyChangeListener(PropertyChangeListener l) {
            prop().addPropertyChangeListener (l);
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
            ((PropertiesDataObject)entry.getDataObject()).updateModificationStatus();
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
    } // End of nested class Environment.

    
    /** Weak listener on file object that notifies the <code>Environment</code> object
     * that a file has been modified. */
    private static final class EnvironmentListener extends FileChangeAdapter {
        
        /** Reference of <code>Environment</code> */
        private Reference reference;
        
        /** @param environment <code>Environment<code> to use
         */
        public EnvironmentListener(Environment environment) {
            reference = new WeakReference(environment);
        }
        
        /** Fired when a file is changed.
         * @param fe the event describing context where action has taken place
         */
        public void fileChanged(FileEvent evt) {
            Environment environment = (Environment)reference.get();
            if (environment != null) {
                if(!environment.entry.getFile().equals(evt.getFile()) ) {
                    // If the FileObject was changed.
                    // Remove old listener from old FileObject.
                    evt.getFile().removeFileChangeListener(this);
                    // Add new listener to new FileObject.
                    environment.entry.getFile().addFileChangeListener(new EnvironmentListener(environment));
                    return;
                }
                
                environment.fileChanged(evt.isExpected(), evt.getTime());
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
            editor.requestFocus();
            
            Element.ItemElem item = myEntry.getHandler().getStructure().getItem(key);
            if (item != null) {
                int offset = item.getKeyElem().getBounds().getBegin().getOffset();
                editor.getPane().getCaret().setDot(offset);
            }
        }
    } // End of inner class PropertiesEditAt.

    
    /** Cloneable top component to hold the editor kit. */
    public static class PropertiesEditor extends CloneableEditor {
        
        /** Holds the file being edited. */
        protected transient PropertiesFileEntry entry;
        
        /** Listener for entry's save cookie changes. */
        private transient PropertyChangeListener saveCookieLNode;
        
        /** Generated serial version UID. */
        static final long serialVersionUID =-2702087884943509637L;
        
        
        /** Constructor for deserialization */
        public PropertiesEditor() {
            super();
        }
        
        /** Creates new editor */
        public PropertiesEditor(PropertiesEditorSupport support) {
            super(support);
            initialize();
        }

        
        /** Initializes object, used in construction and deserialization. */
        private void initialize() {
            this.entry = ((PropertiesEditorSupport)cloneableEditorSupport()).myEntry;
            
            // add to CloneableEditorSupport - patch for a bug in deserialization
            ((PropertiesEditorSupport)cloneableEditorSupport()).setRef(getReference());
            
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
            WeakListener.propertyChange(saveCookieLNode, this.entry));
        }

        /**
         * Overrides superclass method. 
         * When closing last view, also close the document.
         * @return <code>true</code> if close succeeded
         */
        protected boolean closeLast () {
            boolean canClose = super.closeLast();
            if (!canClose)
                return false;
            
            if(!((PropertiesEditorSupport)cloneableEditorSupport()).hasOpenedTableComponent()) {
                entry.getHandler().reparseNowBlocking();
            }
            return true;
        }
        
        /**
         * Overrides superclass method.
         * Serialize this top component.
         * @param out the stream to serialize to
         */
        public void writeExternal (ObjectOutput out) throws IOException {
            super.writeExternal(out);
        }
        
        /**
         * Overrides superclass method. 
         * Deserialize this top component.
         * @param in the stream to deserialize from
         */
        public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
            super.readExternal(in);
            initialize();
        }
        
        /** Getter for pane. */
        private JEditorPane getPane() {
            return pane;
        }
    } // End of nested class PropertiesEditor.
    
    
    /** This stream is able to filter various new line delimiters and replace them by \n.
     */
    static class NewLineInputStream extends InputStream {
        
        /** Encapsulated input stream */
        BufferedInputStream bufis;
        
        /** Next character to read. */
        int nextToRead;
        
        /** The count of types new line delimiters used in the file */
        int[] newLineTypes;
        
        
        /** Creates new stream.
         * @param is encapsulated input stream.
         * @param justFilter The flag determining if this stream should
         *        store the guarded block information. True means just filter,
         *        false means store the information.
         */
        public NewLineInputStream(InputStream is) throws IOException {
            bufis = new BufferedInputStream(is);
            nextToRead = bufis.read();
            newLineTypes = new int[] { 0, 0, 0 };
        }

        
        /** Overrides superclass method. Reads one character.
         * @return next char or -1 if the end of file was reached.
         * @exception IOException if any problem occured.
         */
        public int read() throws IOException {
            if (nextToRead == -1)
                return -1;
            
            if (nextToRead == '\r') {
                nextToRead = bufis.read();
                while (nextToRead == '\r')
                    nextToRead = bufis.read();
                if (nextToRead == '\n') {
                    nextToRead = bufis.read();
                    newLineTypes[NEW_LINE_RN]++;
                    return '\n';
                } else {
                    newLineTypes[NEW_LINE_R]++;
                    return '\n';
                }
            }
            if (nextToRead == '\n') {
                nextToRead = bufis.read();
                newLineTypes[NEW_LINE_N]++;
                return '\n';
            }
            
            int oldNextToRead = nextToRead;
            nextToRead = bufis.read();
            
            return oldNextToRead;
        }

        /** Gets new line type. */
        public byte getNewLineType() {
            if (newLineTypes[0] > newLineTypes[1]) {
                return (newLineTypes[0] > newLineTypes[2]) ? NEW_LINE_N : NEW_LINE_RN;
            } else {
                return (newLineTypes[1] > newLineTypes[2]) ? NEW_LINE_R : NEW_LINE_RN;
            }
        }
    } // End of nested class NewLineInputStream.
    
    
    /** This stream is used for changing the new line delimiters.
     * It replaces the '\n' by '\n', '\r' or "\r\n"
     */
    static class NewLineOutputStream extends OutputStream {
        
        /** Underlaying stream. */
        OutputStream stream;
        
        /** The type of new line delimiter */
        byte newLineType;

        
        /** Creates new stream.
         * @param stream Underlaying stream
         * @param newLineType The type of new line delimiter
         */
        public NewLineOutputStream(OutputStream stream, byte newLineType) {
            this.stream = stream;
            this.newLineType = newLineType;
        }
        
        
        /** Write one character.
         * @param b char to write.
         */
        public void write(int b) throws IOException {
            if (b == '\r')
                return;
            if (b == '\n') {
                switch (newLineType) {
                    // Replace new line by \r.
                    case NEW_LINE_R:
                        stream.write('\r');
                        break;
                    // Replace new line by \r\n.
                    case NEW_LINE_RN:
                        stream.write('\r');
                    // Replace new line by \n.
                    case NEW_LINE_N:
                        stream.write('\n');
                        break;
                }
            } else {
                stream.write(b);
            }
        }
        
        /** Closes the underlaying stream.
         */
        public void close() throws IOException {
            stream.flush();
            stream.close();
        }
    } // End of nested class NewLineOutputStream.
    
    
    /** Inner class. UndoRedo manager which saves a StampFlag
     * for each UndoAbleEdit.
     */
    class UndoRedoStampFlagManager extends UndoRedo.Manager {
        
        /** Hash map of weak reference keys (UndoableEdit's) to their StampFlag's. */
        WeakHashMap stampFlags =  new WeakHashMap(5);
        
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
                Object atomicFlag = ((StampFlag)stampFlags.get(anEdit)).getAtomicFlag(); // atomic flag remains
                super.undo();
                stampFlags.put(anEdit, new StampFlag(System.currentTimeMillis(), atomicFlag));
            }
        }
        
        /** Overrides superclass method. Updates time stamp for that edit. */
        public synchronized void redo() throws CannotRedoException {
            UndoableEdit anEdit = editToBeRedone();
            if(anEdit != null) {
                Object atomicFlag = ((StampFlag)stampFlags.get(anEdit)).getAtomicFlag(); // atomic flag remains
                super.redo();
                stampFlags.put(anEdit, new StampFlag(System.currentTimeMillis(), atomicFlag));
            }
        }
        
        /** Method which gets time stamp of next Undoable edit to be undone. 
         * @ return time stamp in milliseconds or 0 (if don't exit edit to be undone). */
        public long getTimeStampOfEditToBeUndone() {
            UndoableEdit nextUndo = editToBeUndone();
            if(nextUndo == null)
                return 0L;
            else
                return ((StampFlag)stampFlags.get(nextUndo)).getTimeStamp();
        }
        
        /** Method which gets time stamp of next Undoable edit to be redone.
         * @ return time stamp in milliseconds or 0 (if don't exit edit to be redone). */
        public long getTimeStampOfEditToBeRedone() {
            UndoableEdit nextRedo = editToBeRedone();
            if(nextRedo == null)
                return 0L;
            else
                return ((StampFlag)stampFlags.get(nextRedo)).getTimeStamp();
        }
        
        /** Method which gets atomic flag of next Undoable edit to be undone. 
         * @ return atomic flag in milliseconds or 0 (if don't exit edit to be undone). */
        public Object getAtomicFlagOfEditToBeUndone() {
            UndoableEdit nextUndo = editToBeUndone();
            if(nextUndo == null)
                return null;
            else
                return ((StampFlag)stampFlags.get(nextUndo)).getAtomicFlag();
        }
        
        /** Method which gets atomic flag of next Undoable edit to be redone.
         * @ return time stamp in milliseconds or 0 (if don't exit edit to be redone). */
        public Object getAtomicFlagOfEditToBeRedone() {
            UndoableEdit nextRedo = editToBeRedone();
            if(nextRedo == null)
                return null;
            else
                return ((StampFlag)stampFlags.get(nextRedo)).getAtomicFlag();
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
