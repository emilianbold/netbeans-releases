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
import java.lang.ref.*;
import java.lang.reflect.*;
import java.util.*;
import java.text.MessageFormat;
import javax.swing.JEditorPane;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.Timer;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import javax.swing.text.EditorKit;
import javax.swing.text.BadLocationException;

import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.util.WeakListener;
import org.openide.util.NbBundle;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.util.RequestProcessor;
import org.openide.text.EditorSupport;
import org.openide.text.PositionRef;
import org.openide.cookies.EditCookie;
import org.openide.cookies.SaveCookie;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;
import org.openide.nodes.NodeAdapter;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.windows.CloneableOpenSupport;


/** Support for viewing porperties files (EditCookie) by opening them in a text editor */
public class PropertiesEditorSupport extends CloneableEditorSupport implements EditCookie, Serializable {

    /** Timer which countdowns the auto-reparsing time. */
    javax.swing.Timer timer;

    /** New lines in this file was delimited by '\n' */
    static final byte NEW_LINE_N = 0;

    /** New lines in this file was delimited by '\r' */
    static final byte NEW_LINE_R = 1;

    /** New lines in this file was delimited by '\r\n' */
    static final byte NEW_LINE_RN = 2;

    /** The type of new lines */
    byte newLineType = NEW_LINE_N;

    /** The flag saying if we should listen to the document modifications */
    private boolean listenToEntryModifs = true;

    private Document listenDocument;
    
    private boolean modificationListening = true;

    /** Listener to the document changes - entry. The superclass holds a saving manager
    * for the whole dataobject. */
    private EntrySavingManager entryModifL;

    
    
    /** Properties Settings */
    static final PropertiesSettings settings = new PropertiesSettings();

    //static final long serialVersionUID =1787354011149868490L;
    /** Constructor */
    public PropertiesEditorSupport(PropertiesFileEntry entry) {
        super (new Env(entry));
        this.myEntry = entry;
        //System.out.println("editor support constructor - " + entry.getFile().getName());
        //Thread.dumpStack();
        initialize();
    }

    public void initialize() {
        modificationListening = false;
        setMIMEType (PropertiesDataObject.MIME_PROPERTIES);
        initTimer();

        // listen to myself so I can add a listener for changes when the document is loaded
        addChangeListener(new ChangeListener() {
                              public void stateChanged(ChangeEvent evt) {
                                  if (isDocumentLoaded()) {
                                      setListening(true);
                                  }
                              }
                          });

        //PENDING
        // set actions
        /*setActions (new SystemAction [] {
          SystemAction.get (CutAction.class),
          SystemAction.get (CopyAction.class),
          SystemAction.get (PasteAction.class),
    });*/
    }

    /** Overrides teh super method. Adds checking for opened Table component.
    */
    protected void notifyUnmodified () {
        // unmark as modified only in case there is not opened Table component, otherwise just close
        if(!hasOpenTableComponent()) // TEMP
            super.notifyUnmodified ();
        
        if (modificationListening) {
            ((Env)env).removeSaveCookie ();
        }
    }
    
    /** Overrides the super method to add a save cookie if the 
    * document has been marked modified.
    *
    * @return true if the environment accepted being marked as modified
    *    or false if it refused it and the document should still be unmodified
    */
    protected boolean notifyModified () {
        if (super.notifyModified ()) {
            if (modificationListening) {
                ((Env)env).addSaveCookie ();
            }
            return true;
        } else {
            return false;
        }
    }
  
    /** Overrides super method. Adds checknig for opened Table panel. 
    */
    protected void notifyClosed() {
        // close document only in case there is not open table editor
        if(!hasOpenTableComponent()) {
            super.notifyClosed();
        }
    }
   
      
    void setRef(CloneableTopComponent.Ref ref) {
        allEditors = ref;
    }

// TEMP    
/*    Object writeReplace() throws ObjectStreamException {
        System.err.println("writeReplace="+myEntry.getFile().getName()); // TEMP
        return new SerialProxy(myEntry);
    }

    public static class SerialProxy implements Serializable {

//        static final long serialVersionUID =2675098551717845346L;
        public SerialProxy(PropertiesFileEntry serialEntry) {
            this.serialEntry = serialEntry;
        }

        private PropertiesFileEntry serialEntry;

        Object readResolve() throws ObjectStreamException {
            System.out.println("deserializing properties editor"); // TEMP
            System.out.println("serialEntry " + serialEntry); // TEMP
            System.out.println("name=" + serialEntry.getFile().getName()); // TEMP
            Object pe = serialEntry.getPropertiesEditor();
            //System.out.println("deserializing properties editor END");
            return pe;
        }
    } // end of SerialProxy */

    /** Visible view of underlying file entry */
    transient PropertiesFileEntry myEntry;

    /** Focuses existing component to open, or if none exists creates new.
    * @see OpenCookie#open
    */
    public void open () {
        CloneableTopComponent editor = openCloneableTopComponent2();
        editor.requestFocus();
    }


    /** Simply open for an editor. */
    protected final CloneableTopComponent openCloneableTopComponent2() {
        MessageFormat mf = new MessageFormat (NbBundle.getBundle(PropertiesEditorSupport.class).
                                              getString ("CTL_PropertiesOpen"));

        synchronized (allEditors) {
            try {
                CloneableTopComponent ret = (CloneableTopComponent)allEditors.getAnyComponent ();
                ret.open();
                return ret;
            } catch (java.util.NoSuchElementException ex) {
                // no opened editor
                TopManager.getDefault ().setStatusText (mf.format (
                                                            new Object[] {myEntry.getFile().getName()}));

                CloneableTopComponent editor = createCloneableTopComponent ();
                allEditors = editor.getReference ();
                editor.open();

                TopManager.getDefault ().setStatusText (NbBundle.getBundle(DataObject.class).getString ("CTL_ObjectOpened"));
                return editor;
            }
        }
    }




    /** Launches the timer for autoreparse */
    private void initTimer() {
        // initialize timer
        timer = new Timer(0, new java.awt.event.ActionListener() {
                              public void actionPerformed(java.awt.event.ActionEvent e) {
                                  myEntry.getHandler().autoParse();
                              }
                          });
        timer.setInitialDelay(settings.getAutoParsingDelay());
        timer.setRepeats(false);
    }

    /** Returns whether there is an open component (editor or open). */
    public synchronized boolean hasOpenComponent() {
        return (hasOpenTableComponent() || hasOpenEditorComponent());
    }

    private synchronized boolean hasOpenTableComponent() {
        //System.out.println("hasOpenComponent (table) " + myEntry.getFile().getPackageNameExt('/','.') + " " + ((PropertiesDataObject)myEntry.getDataObject()).getOpenSupport().hasOpenComponent());
        return ((PropertiesDataObject)myEntry.getDataObject()).getOpenSupport().hasOpenComponent();
    }

    /** Returns whether there is an open editor component. */
    public synchronized boolean hasOpenEditorComponent() {
        java.util.Enumeration en = allEditors.getComponents ();
        //System.out.println("hasOpenComponent (editor) " + myEntry.getFile().getPackageNameExt('/','.') + " " + en.hasMoreElements ());
        return en.hasMoreElements ();
    }

    public void saveThisEntry() throws IOException {
        super.saveDocument();
        myEntry.setModified(false);
    }

    public boolean close() {
        SaveCookie savec = (SaveCookie) myEntry.getCookie(SaveCookie.class);
        if ((savec != null) && hasOpenTableComponent()) {
            return false;
        }
        //System.out.println("closing");
        if (!super.close())
            return false;

        //System.out.println("closed - document open = " + isDocumentLoaded());

        closeDocumentEntry();
        myEntry.getHandler().reparseNowBlocking();
        return true;
    }

    /** Clears all data from memory.
    */
//    protected void closeDocument () {
//        System.err.println("in close document"); // TEMP
//        super.closeDocument();
//        closeDocumentEntry();
//    }

    /** Utility method which enables or disables listening to modifications
    * on asociated document.
    * <P>
    * Could be useful if we have to modify document, but do not want the
    * Save and Save All actions to be enabled/disabled automatically.
    * Initially modifications are listened to.
    * @param listenToModifs whether to listen to modifications
    */
    public void setModificationListening (final boolean listenToModifs) {
        //System.out.println("set modification listening - " + listenToModifs);
        this.listenToEntryModifs = listenToModifs;
        if (getDocument() == null) return;
        setListening(listenToEntryModifs);
    }

    /** Message to display when an object is being opened.
    * @return the message or null if nothing should be displayed
    */
    protected String messageOpening () {
        return NbBundle.getMessage (EditorSupport.class , "CTL_ObjectOpen", // NOI18N
            myEntry.getName(),
            myEntry.toString()        
        );
    }
    
    /** Message to display when an object has been opened.
    * @return the message or null if nothing should be displayed
    */
    protected String messageOpened () {
        return NbBundle.getMessage (EditorSupport.class, "CTL_ObjectOpened", // NOI18N
            myEntry.getName(),
            myEntry.toString()
        );
    }

    /** is modified and is being closed.
    *
    * @return text to show to the user
    */
    protected String messageSave () {
        return NbBundle.getMessage (
            EditorSupport.class,
            "MSG_SaveFile", // NOI18N
            myEntry.getName()        
        );
    }

    /** Constructs message that should be used to name the editor component.
    * @return name of the editor
    */
    protected String messageName () {
        String name = myEntry.getDataObject().getPrimaryFile().getName()+"("+Util.getPropertiesLabel(myEntry)+")"; // NOI18N
        
        if (isModified ()) {
            return NbBundle.getMessage (
                EditorSupport.class,
                "LAB_EditorName_Modified", // NOI18N
                name
            );
        } else {
            return NbBundle.getMessage (
                EditorSupport.class,
                "LAB_EditorName_Uptodate", // NOI18N
                name
            );
        }
    }

    /** Text to use as tooltip for component.
    * @return text to show to the user
    */
    protected String messageToolTip () {
        // update tooltip
        FileObject fo = myEntry.getFile();
        
        return NbBundle.getMessage (
            EditorSupport.class,
            "LAB_EditorToolTip", // NOI18N
            fo.getPackageName ('.'),
            fo.getExt ()
        );
    }

    /** Overrides superclass method.
    * @return the {@link CloneableEditor} for this support
    */
    protected CloneableEditor createCloneableEditor () {
        return new PropertiesEditor (this);
    }
            
    /* A method to create a new component. Overridden in subclasses.
    * @return the {@link Editor} for this support
    */
    protected CloneableTopComponent createCloneableTopComponent () {
        // initializes the document if not initialized
        prepareDocument ();

        CloneableEditor editor = new PropertiesEditor (this);
        return editor;
    }

    /** Let's the super method create the document and also annotates it
    * with Title and StreamDescription properities.
    *
    * @param kit kit to user to create the document
    * @return the document annotated by the properties
    */
    protected StyledDocument createStyledDocument (EditorKit kit) {
        StyledDocument doc = super.createStyledDocument (kit);
            
        // set document name property
        doc.putProperty(javax.swing.text.Document.TitleProperty,
            myEntry.getFile ().getPackageNameExt('/', '.')
        );
        // set dataobject to stream desc property
        doc.putProperty(javax.swing.text.Document.StreamDescriptionProperty,
            myEntry.getDataObject ()
        );
        
        return doc;
    }

    /** Should test whether all data is saved, and if not, prompt the user
    * to save. Called by my topcomponent when it wants to close its last topcomponent, but the table editor may still be open
    *
    * @return <code>true</code> if everything can be closed
    */
    protected boolean canClose () {
        SaveCookie savec = (SaveCookie) myEntry.getCookie(SaveCookie.class);
        if (savec != null) {
            // if the table is open, can close without worries, don't remove the save cookie
            if (hasOpenTableComponent())
                return true;

            // PENDING - is not thread safe
            MessageFormat format = new MessageFormat(NbBundle.getBundle(PropertiesEditorSupport.class).
                                   getString("MSG_SaveFile"));
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
                }
                catch (IOException e) {
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

    /** Read the file from the stream, filter the guarded section
    * comments, and mark the sections in the editor.
    *
    * @param doc the document to read into
    * @param stream the open stream to read from
    * @param kit the associated editor kit
    * @throws IOException if there was a problem reading the file
    * @throws BadLocationException should not normally be thrown
    * @see #saveFromKitToStream
    */
    protected void loadFromStreamToKit (StyledDocument doc, InputStream stream, EditorKit kit) throws IOException, BadLocationException {

        NewLineInputStream is = new NewLineInputStream(stream);
        try {
            kit.read(is, doc, 0);
            newLineType = is.getNewLineType();
        }
        finally {
            is.close();
        }
    }

    /** Store the document and add the special comments signifying
    * guarded sections.
    *
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
        }
        finally {
            if (os != null) {
                try {
                    os.close();
                }
                catch (IOException e) {
                }
            }
        }
    }


    /** Does part of the cleanup - removes a listener.
    */
    private void closeDocumentEntry () {
        // listen to modifs
        if (listenToEntryModifs) {
            getEntryModifL().clearSaveCookie();

            setListening(false);
        }
    }

    private void setListening(boolean listen) {
        if (listen) {
            if ((getDocument() == null) || (listenDocument == getDocument()))
                return;
            if (listenDocument != null) // also holds that listenDocument != getDocument()
                listenDocument.removeDocumentListener(getEntryModifL());
            listenDocument = getDocument();
            listenDocument.addDocumentListener(getEntryModifL());
        }
        else {
            if (listenDocument != null) {
                listenDocument.removeDocumentListener(getEntryModifL());
                listenDocument = null;
            }
        }
    }

    /** Visible view of the underlying method. */
//    public Editor openAt(PositionRef pos) { // TEMP
//        return super.openAt(pos);
//    }

    /** Forcibly create one editor component. Then set the caret
    * to the given position.
    * @param pos where to place the caret
    * @return always non-<code>null</code> editor
    */
    protected CloneableEditor openAt(PositionRef pos) {
        // copied from super class
        int column = -1;// patch
        PropertiesEditor e = (PropertiesEditor) openCloneableTopComponent();
        e.open();
        int offset;
        if (column >= 0) {
            javax.swing.text.Element el = NbDocument.findLineRootElement (getDocument ());
            el = el.getElement (el.getElementIndex (pos.getOffset ()));
            offset = el.getStartOffset () + column;
            if (offset > el.getEndOffset ()) {
                offset = el.getEndOffset ();
            }
        } else {
            offset = pos.getOffset ();
        }
        prepareDocument ().waitFinished ();
        e.getPane().getCaret().setDot(offset);
        return e;
    }

    
    /** Returns a EditCookie for editing at a given position. */
    public PropertiesEditAt getViewerAt(String key) {
        return new PropertiesEditAt (key);
    }

    /** Class for opening at a given key. */
    public class PropertiesEditAt implements EditCookie {

        private String key;

        PropertiesEditAt(String key) {
            this.key   = key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public void edit() {
            Element.ItemElem item = myEntry.getHandler().getStructure().getItem(key);
            if (item != null) {
                PositionRef pos = item.getKeyElem().getBounds().getBegin();
                PropertiesEditorSupport.this.openAt(pos);
            }
            else {
                PropertiesEditorSupport.this.edit();
            }
        }

    }


    /** Returns an entry saving manager. */
    private synchronized EntrySavingManager getEntryModifL () {
        if (entryModifL == null) {
            entryModifL = new EntrySavingManager();
            // listens whether to add or remove SaveCookie
            myEntry.addPropertyChangeListener(entryModifL);
        }
        return entryModifL;
    }

    /** Make modifiedApendix accessible for inner classes. */
    /*String getModifiedAppendix() {
        // TEMP due changes in EditorSupport
        return " *";
        //return modifiedAppendix;
    }*/

    /** Implementation of the default Environment for EditorSupport
    */
    private static final class Env implements CloneableEditorSupport.Env, SaveCookie
        /*PropertyChangeListener, VetoableChangeListener*/ {
        /** generated Serialized Version UID */
        //static final long serialVersionUID = 354528097109874355L;

        /** Entry on which is Support build */
        private PropertiesFileEntry entry;
        
        
        /** object to connected to*/
//        private transient DataObject obj;

        /** The file object this environment is associated to.
        * This file object can be changed by a call to refresh file.
        */
//        private transient FileObject fileObject;

        /** Lock acquired after the first modification and used in save.
        * Transient => is not serialized.
        */
        private transient FileLock fileLock;
                        
        /** support for firing of property changes
        */
        private transient PropertyChangeSupport propSupp;
        /** support for firing of vetoable changes
        */
        private transient VetoableChangeSupport vetoSupp;
        
        
        /** Constructor.
        * @param obj this support should be associated with
        */
        public Env (PropertiesFileEntry entry) {
            this.entry = entry;
            entry.getFile().addFileChangeListener(new EnvListener(this));
        }
        
        /** Getter for file associated with this environment.
        * @return the file input/output operation should be performed on
        */
        protected FileObject getFileObject() {
            return entry.getFile(); // TEMP
        }
        
        /** Getter for data object.
        */
        protected final DataObject getDataObject () {
            return entry.getDataObject(); // TEMP
        }
        
        /** Locks the file.
        * @return the lock on the file getFile ()
        * @exception IOException if the file cannot be locked
        */
        protected FileLock takeLock () throws IOException {
            return entry.takeLock(); // TEMP
        }
        
        /** Adds property listener.
         */
        public void addPropertyChangeListener(PropertyChangeListener l) {
            prop ().addPropertyChangeListener (l);
        }

        /** Removes property listener.
         */
        public void removePropertyChangeListener(PropertyChangeListener l) {
            prop ().removePropertyChangeListener (l);
        }
        
        /** Adds veto listener.
         */
        public void addVetoableChangeListener(VetoableChangeListener l) {
            veto ().addVetoableChangeListener (l);
        }

        /** Removes veto listener.
         */
        public void removeVetoableChangeListener(VetoableChangeListener l) {
            veto ().removeVetoableChangeListener (l);
        }
                        
        /** Gives notification that the DataObject was changed.
        * @param ev PropertyChangeEvent
        */
//        public void propertyChange(PropertyChangeEvent ev) {
//            if (ev.getPropertyName().equals(DataObject.PROP_PRIMARY_FILE)) {
//                changeFile ();
//            }

            // connected            
//            if (DataObject.PROP_MODIFIED.equals (ev.getPropertyName())) {
//                if (getDataObject ().isModified ()) {
//                    getDataObject ().addVetoableChangeListener(this);
//                } else {
//                    getDataObject ().removeVetoableChangeListener(this);
//                }
//            }
            
//            firePropertyChange (
//                ev.getPropertyName (),
//                ev.getOldValue (),
//                ev.getNewValue ()
//            );
//        }

        /** Accepts vetoable changes and fires them to own listeners.
        */
//        public void vetoableChange(PropertyChangeEvent ev) throws PropertyVetoException {
//            fireVetoableChange (
//                ev.getPropertyName (),
//                ev.getOldValue (),
//                ev.getNewValue ()
//            );
//        }
        
        /** Fires property change.
        * @param name the name of property that changed
        * @param oldValue old value
        * @param newValue new value
        */
        protected void firePropertyChange (String name, Object oldValue, Object newValue) {
            prop ().firePropertyChange (name, oldValue, newValue);
        }

        /** Fires vetoable change.
        * @param name the name of property that changed
        * @param oldValue old value
        * @param newValue new value
        */
        protected void fireVetoableChange (String name, Object oldValue, Object newValue) 
        throws PropertyVetoException {
            veto ().fireVetoableChange (name, oldValue, newValue);
        }
                
        /** Lazy getter for change support.
        */
        private PropertyChangeSupport prop () {
            if (propSupp == null) {
                synchronized (this) {
                    if (propSupp == null) {
                        propSupp = new PropertyChangeSupport (this);
                    }
                }
            }
            return propSupp;
        }
        
        /** Lazy getter for veto support.
        */
        private VetoableChangeSupport veto () {
            if (vetoSupp == null) {
                synchronized (this) {
                    if (vetoSupp == null) {
                        vetoSupp = new VetoableChangeSupport (this);
                    }
                }
            }
            return vetoSupp;
        }

        /** Getter for the file to work on.
        * @return the file
        */
//        private FileObject getFileImpl () {
//            if (fileObject == null) {
//                fileObject = getFileObject();
//                fileObject.addFileChangeListener (new EnvListener (this));
//            }
//            return fileObject;
//        }
        
        /** Method that allows subclasses to notify this environment that
        * the file associated with this support has changed and that 
        * the environment should listen on modifications of different 
        * file object.
        */
        protected final void changeFile () {
//            fileObject = null; // TEMP
            

            boolean lockAgain;
            if (fileLock != null) {
                fileLock.releaseLock ();
                lockAgain = true;
            } else {
                lockAgain = false;
            }

//            fileObject = getFileImpl (); // TEMP

            if (lockAgain) { // refresh lock
                try {
                    fileLock = takeLock ();
                } catch (IOException e) {
                    TopManager.getDefault ().getErrorManager ().notify (
                      ErrorManager.INFORMATIONAL,
                      e
                    );
                }
            }
            
        }
                
        /** Mime type of the document.
        * @return the mime type to use for the document
        */
        public String getMimeType() {
            return getFileObject().getMIMEType();
        }

        /** The time when the data has been modified
        */
        public Date getTime() {
            return getFileObject().lastModified();
        }
        
        /** Test whether the support is in valid state or not.
        * It could be invalid after deserialization when the object it
        * referenced to does not exist anymore.
        *
        * @return true or false depending on its state
        */
        public boolean isValid () {
            return getDataObject ().isValid ();
        }

        /** Test whether the object is modified or not.
         * @return true if the object is modified
         */
        public boolean isModified() {
            return entry.isModified(); // TEMP
        }
                        
        /** First of all tries to lock the primary file and
        * if it succeeds it marks the data object modified.
        *
        * @exception IOException if the environment cannot be marked modified
        *   (for example when the file is readonly), when such exception
        *   is the support should discard all previous changes
        */
        public void markModified() throws java.io.IOException {
            if (fileLock == null || !fileLock.isValid()) {
                fileLock = takeLock ();
            }

            entry.setModified(true); // TEMP
            getDataObject ().setModified (true);
        }
        
        /** Reverse method that can be called to make the environment 
        * unmodified.
        */
        public void unmarkModified() {
            if (fileLock != null && fileLock.isValid()) {
                fileLock.releaseLock();
            }
  
            entry.setModified(false);          
        }

        /** Obtains the input stream.
        * @exception IOException if an I/O error occures
        */
        public InputStream inputStream() throws IOException {
            return getFileObject().getInputStream(); // TEMP
        }
        
        /** Obtains the output stream.
        * @exception IOException if an I/O error occures
        */
        public OutputStream outputStream() throws IOException {
            return getFileObject().getOutputStream(fileLock); // TEMP
        }
                        
        /** Method that allows environment to find its 
         * cloneable open support.
        * @return the support or null if the environemnt is not in valid 
        * state and the CloneableOpenSupport cannot be found for associated
        * entry object
        */
        public CloneableOpenSupport findCloneableOpenSupport() {
            return (PropertiesEditorSupport)entry.getCookieSet().getCookie(EditCookie.class); // TEMP
        }
        
        /** Invoke the save operation.
        * @throws IOException if the object could not be saved
        */
        public void save() throws java.io.IOException {
            System.err.println("Saving thru Env"); // TEMP
            ((CloneableEditorSupport)findCloneableOpenSupport ()).saveDocument ();
        }
/*
        void clearSaveCookie() {
            DataObject dataObj = findDataObject();
            // remove save cookie (if save was succesfull)
            dataObj.setModified(false);
            releaseFileLock();
        }
*/
        /** Adds save cookie to the DO.
        */
        final void addSaveCookie() {
            DataObject dataObj = getDataObject ();
            // add Save cookie to the data object
            if (dataObj instanceof MultiDataObject) {
                if (dataObj.getCookie(SaveCookie.class) == null) {
                    ((MultiDataObject)dataObj).getCookieSet().add(this);
                }
            }
        }
        /** Removes save cookie from the DO.
        */
        final void removeSaveCookie() {
            DataObject dataObj = getDataObject ();
            // add Save cookie to the data object
            if (dataObj instanceof MultiDataObject) {
                if (dataObj.getCookie(SaveCookie.class) == this) {
                    ((MultiDataObject)dataObj).getCookieSet().remove(this);
                }
            }
        }
        
        /** Called from the EnvListener
        * @param expected is the change expected
        * @param time of the change
        */
        final void fileChanged (boolean expected, long time) {
            if (expected) {
                // newValue = null means do not ask user whether to reload
                firePropertyChange (PROP_TIME, null, null);
            } else {
                firePropertyChange (PROP_TIME, null, new Date (time));
            }
        }
        
    } // end of Env
    
    /** Listener on file object that notifies the Env object
    * that a file has been modified.
    */
    private static final class EnvListener extends FileChangeAdapter {
        /** Reference (Env) */
        private Reference env;
        
        /** @param env environement to use
        */
        public EnvListener (Env env) {
            this.env = new WeakReference (env);
        }

        /** Fired when a file is changed.
        * @param fe the event describing context where action has taken place
        */
        public void fileChanged(FileEvent fe) {
            Env env = (Env)this.env.get ();
            if (env != null) {
                if(!env.getFileObject().equals(fe.getFile()) ) {
                    fe.getFile().removeFileChangeListener (this);
                    env.getFileObject().addFileChangeListener(new EnvListener(env));
                    return;
                }

                env.fileChanged (fe.isExpected (), fe.getTime ());
            }
        }
                
    } // end of EnvListener

    
    /** Cloneable top component to hold the editor kit.
    */
    public static class PropertiesEditor extends CloneableEditor {

        /** Holds the file being edited */
        protected transient PropertiesFileEntry entry;

//        private transient PropertiesEditorSupport propSupport;

        /** Listener for entry's save cookie changes */
        private transient PropertyChangeListener saveCookieLNode;
        /** Listener for entry's name changes */
//        private transient NodeAdapter nodeL;

        //static final long serialVersionUID =-2702087884943509637L;
        /** Constructor for deserialization */
        public PropertiesEditor() {
            super();
        }

        /** Creates new editor */
        public PropertiesEditor(PropertiesEditorSupport support) {
            super(support);
            initMe();
        }

        /** initialization after construction and deserialization */
        private void initMe() {
            this.entry = ((PropertiesEditorSupport)cloneableEditorSupport()).myEntry;

            // add to EditorSupport - patch for a bug in deserialization
            ((PropertiesEditorSupport)cloneableEditorSupport()).setRef(getReference());

// TEMP>>
/*            entry.getNodeDelegate().addNodeListener (
                new WeakListener.Node(nodeL =
                                          new NodeAdapter () {
                                              public void propertyChange (PropertyChangeEvent ev) {
                                                  if (ev.getPropertyName ().equals (Node.PROP_DISPLAY_NAME)) {
                                                      System.err.println("Node prop displayName"); // TEMP
                                                      PropertiesEditor.super.updateName();
                                                  }
                                              }
                                          }
                                     )); */
// TEMP<<
                        
            Node n = entry.getNodeDelegate ();
            setActivatedNodes (new Node[] { n });

            updateName();

            // entry to the set of listeners
            saveCookieLNode = new PropertyChangeListener() {
                                  public void propertyChange(PropertyChangeEvent evt) {
                                      if (PresentableFileEntry.PROP_COOKIE.equals(evt.getPropertyName()) ||
                                              PresentableFileEntry.PROP_NAME.equals(evt.getPropertyName())) {
                                          PropertiesEditor.super.updateName();
                                      }
                                  }
                              };
            this.entry.addPropertyChangeListener(
                WeakListener.propertyChange(saveCookieLNode, this.entry));
        }

        /** Getter for pane */
        public JEditorPane getPane() {
            return pane;
        }
        
        /** When closing last view, also close the document.
         * @return <code>true</code> if close succeeded
        */
        protected boolean closeLast () {
/*            // instead of super
            if (!propSupport.canClose ()) {
                // if we cannot close the last window
                return false;
            }

            boolean doCloseDoc = !propSupport.hasOpenTableComponent();
            //SaveCookie savec = (SaveCookie) entry.getCookie(SaveCookie.class);
            try {
                if (doCloseDoc) {
                    // propSupport.closeDocument (); by reflection
                    Method closeDoc = EditorSupport.class.getDeclaredMethod("closeDocument", new Class[0]);
                    closeDoc.setAccessible(true);
                    closeDoc.invoke(propSupport, new Object[0]);
                }

//                 if (propSupport.lastSelected == this) {
//                  propSupport.lastSelected = null; by reflection 
                Field lastSel = EditorSupport.class.getDeclaredField("lastSelected");
                lastSel.setAccessible(true);
                if (lastSel.get(propSupport) == this)
                    lastSel.set(propSupport, null);
            }
            catch (Exception e) {
                if (Boolean.getBoolean("netbeans.debug.exceptions"))
                    e.printStackTrace();
            }

            // end super */
            boolean canClose = super.closeLast();
            if (!canClose)
              return false;
            if(!((PropertiesEditorSupport)cloneableEditorSupport()).hasOpenTableComponent()) {
                ((PropertiesEditorSupport)cloneableEditorSupport()).closeDocumentEntry();
                entry.getHandler().reparseNowBlocking();
            }
            return true;
        }

        /** Updates the name of this top component according to
        * the existence of the save cookie in ascoiated data object
        */
/*        protected void updateName () {
            if (entry == null) {
                setName("");
                return;
            }
            else {
                String name = entry.getFile().getName();
                if (entry.getCookie(SaveCookie.class) != null)
                    setName(name + ((PropertiesEditorSupport)cloneableEditorSupport()).getModifiedAppendix()); // TEMP
                else
                    setName(name);
            }
        }*/

        /* Serialize this top component.
        * @param out the stream to serialize to
        */
        public void writeExternal (ObjectOutput out)
        throws IOException {
            super.writeExternal(out);
        }

        /* Deserialize this top component.
        * @param in the stream to deserialize from
        */
        public void readExternal (ObjectInput in)
        throws IOException, ClassNotFoundException {
            super.readExternal(in);
            initMe();
        }

    } // end of PropertiesEditor inner class

    /** EntrySavingManager manages two tasks concerning saving:<P>
    * 1) It tracks changes in document asociated with ther entry and
    *    sets modification flag appropriately.<P>
    * 2) This class also implements functionality of SaveCookie interface
    */
    private final class EntrySavingManager implements DocumentListener, SaveCookie, PropertyChangeListener {

        /*********** Implementation of the DocumentListener *******/

        /** Gives notification that an attribute or set of attributes changed.
        * @param ev event describing the action
        */
        public void changedUpdate(DocumentEvent ev) {
            // do nothing - just an attribute
        }

        /** Gives notification that there was an insert into the document.
        * @param ev event describing the action
        */
        public void insertUpdate(DocumentEvent ev) {
            modified();
            changeStructureStatus();
        }

        /** Gives notification that a portion of the document has been removed.
        * @param ev event describing the action
        */
        public void removeUpdate(DocumentEvent ev) {
            modified();
            changeStructureStatus();
        }

        private void changeStructureStatus() {
            int delay = settings.getAutoParsingDelay();
            myEntry.getHandler().setDirty(true);
            if (delay > 0) {
                timer.setInitialDelay(delay);
                timer.restart();
            }
        }

        /** Gives notification that the DataObject was changed.
        * @param ev PropertyChangeEvent
        */
        public void propertyChange(PropertyChangeEvent ev) {
            if ((ev.getSource() == myEntry) &&
                    (PropertiesFileEntry.PROP_MODIFIED.equals(ev.getPropertyName()))) {

                if (((Boolean) ev.getNewValue()).booleanValue()) {
                    addSaveCookie();
                } else {
                    removeSaveCookie();
                }
            }
        }

        /******* Implementation of the Save Cookie *********/

        public void save () throws IOException {
            // do saving job
            saveThisEntry();
        }

        void clearSaveCookie() {
            // remove save cookie (if save was succesfull)
            myEntry.setModified(false);
        }

        /** Sets modification flag.
        */
        private void modified () {
            myEntry.setModified(true);
        }
        /** Adds save cookie to the DO. Only if a component is open, otherwise saves it right away
        */
        private void addSaveCookie() {
            if (myEntry.getCookie(SaveCookie.class) == null) {
                myEntry.getCookieSet().add(this);
            }
            ((PropertiesDataObject)myEntry.getDataObject()).updateModificationStatus();
            if (!hasOpenComponent()) {
                RequestProcessor.postRequest(new Runnable() {
                                                 public void run() {
                                                     myEntry.getPropertiesEditor().open();
                                                 }
                                             });
            }

        }
        /** Removes save cookie from the DO.
        */
        private void removeSaveCookie() {
            // remove Save cookie from the data object
            if (myEntry.getCookie(SaveCookie.class) == this) {
                myEntry.getCookieSet().remove(this);
            }
            ((PropertiesDataObject)myEntry.getDataObject()).updateModificationStatus();
        }

    } // end of EntrySavingManager inner class


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

        /** Reads one character.
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
                }
                else {
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

        public byte getNewLineType() {
            if (newLineTypes[0] > newLineTypes[1]) {
                return (newLineTypes[0] > newLineTypes[2]) ? (byte) 0 : 2;
            }
            else {
                return (newLineTypes[1] > newLineTypes[2]) ? (byte) 1 : 2;
            }
        }
    }


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
                case NEW_LINE_R:
                    stream.write('\r');
                    break;
                case NEW_LINE_RN:
                    stream.write('\r');
                case NEW_LINE_N:
                    stream.write('\n');
                    break;
                }
            }
            else {
                stream.write(b);
            }
        }

        /** Closes the underlaying stream.
        */
        public void close() throws IOException {
            stream.flush();
            stream.close();
        }
    }


}
