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


import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.text.MessageFormat;
import java.util.*;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.openide.actions.FindAction;
import org.openide.awt.UndoRedo;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.FileEntry;
import org.openide.loaders.DataObject;
import org.openide.loaders.OpenSupport;
import org.openide.NotifyDescriptor;
import org.openide.text.EditorSupport;
import org.openide.TopManager;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListener;
import org.openide.util.WeakSet;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;
import org.openide.windows.Workspace;
import org.openide.windows.Mode;
import org.openide.windows.CloneableOpenSupport;


/** Support for opening properties files (OpenCookie) in visual editor */
public class PropertiesOpen extends CloneableOpenSupport implements OpenCookie {

    /** Main properties dataobject */
    PropertiesDataObject obj;
    
    /** Listener for modificationc on dataobject, adding and removing save cookie */
    PropertyChangeListener modifL;

    /** UndoRedo manager for this properties open support */
    protected transient UndoRedo.Manager undoRedoManager;

    /** This object is used for marking all undoable edits performed as one atomic undoable action. */
    transient Object atomicUndoRedoFlag;
    

    /** Constructor */
    public PropertiesOpen(PropertiesDataObject obj) {
        super(new Env(obj));
        this.obj = obj;
        
        this.obj.addPropertyChangeListener(WeakListener.propertyChange(modifL = 
            new ModifiedListener(), this.obj));
    }

    void setRef(CloneableTopComponent.Ref ref) {
        allEditors = ref;
    }

    /** Only here because of a compiler bug */
    public final CloneableTopComponent openCloneableTopComponentPublic() {
        return openCloneableTopComponent();
    }
    /** A method to create a new component. Must be overridden in subclasses.
    * @return the cloneable top component for this support
    */
    protected CloneableTopComponent createCloneableTopComponent () {
        return new PropertiesCloneableTopComponent(obj);
    }

    /** Opens the table at a given key */
    public PropertiesOpenAt getOpenerForKey(PropertiesFileEntry entry, String key) {
        return new PropertiesOpenAt(key);
    }

    public synchronized boolean hasOpenComponent() {
        java.util.Enumeration en = allEditors.getComponents ();
        return en.hasMoreElements ();
    }

    /** Gets UndoRedo manager for this OpenSupport */
    public UndoRedo getUndoRedo () {
        if(undoRedoManager != null)
            return undoRedoManager;
        else
            return new CompoundUndoRedoManager(obj);
    }

    private synchronized void closeDocuments() {
        closeEntry((PropertiesFileEntry)obj.getPrimaryEntry());
        for (Iterator it = obj.secondaryEntries().iterator(); it.hasNext(); ) {
            closeEntry((PropertiesFileEntry)it.next());
        }
    }

    private void closeEntry(PropertiesFileEntry entry) {
        PropertiesEditorSupport pes = entry.getPropertiesEditor();
        if (pes.hasOpenEditorComponent())
            return;
        else {
            // make possible to close document as well if exist
            entry.getPropertiesEditor().forceNotifyClosed();
            entry.getHandler().reparseNowBlocking();
        }
    }

    /** Should test whether all data is saved, and if not, prompt the user
    * to save.
    *
    * @return <code>true</code> if everything can be closed
    */
    protected boolean canClose () {
        SaveCookie savec = (SaveCookie) obj.getCookie(SaveCookie.class);
        if (savec != null) {
            if (!shouldAskSave())
                return true;
            MessageFormat format = new MessageFormat(NbBundle.getBundle(PropertiesOpen.class).getString("MSG_SaveFile"));
            String msg = format.format(new Object[] { obj.getName()});
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.YES_NO_CANCEL_OPTION);
            Object ret = TopManager.getDefault().notify(nd);


            if (NotifyDescriptor.YES_OPTION.equals(ret)) {
                try {
                    savec.save();
                    obj.updateModificationStatus();
                }
                catch (IOException e) {
                    TopManager.getDefault().notifyException(e);
                    return false;
                }
            }

            obj.updateModificationStatus();
            
            if (NotifyDescriptor.CANCEL_OPTION.equals(ret))
                return false;
            
        }
        return true;
    }

    /** Message to display when an object is being opened.
    * @return the message or null if nothing should be displayed
    */
    protected String messageOpening () {
//        DataObject obj = entry.getDataObject ();

        return NbBundle.getMessage (OpenSupport.class , "CTL_ObjectOpen", // NOI18N
            obj.getName(),
            obj.getPrimaryFile().toString()
        );
    }

    /** Message to display when an object has been opened.
    * @return the message or null if nothing should be displayed
    */
    protected String messageOpened () {
        return NbBundle.getMessage (OpenSupport.class, "CTL_ObjectOpened"); // NOI18N
    }
    
    
    /** Returns true if closing this editor whithout saving would result in loss of data
    *  because al least one of the modified files is not open in the code editor.
    *  Should be called only if the object has SaveCookie
    */
    private boolean shouldAskSave() {
        // for each entry : if there is a SaveCookie and no open editor component, return true.
        // if passed for all entries, return false
        PropertiesFileEntry entry = (PropertiesFileEntry)obj.getPrimaryEntry();
        SaveCookie savec = (SaveCookie)entry.getCookie(SaveCookie.class);
        if ((savec != null) && !entry.getPropertiesEditor().hasOpenEditorComponent())
            return true;
        for (Iterator it = obj.secondaryEntries().iterator(); it.hasNext(); ) {
            entry = (PropertiesFileEntry)it.next();
            savec = (SaveCookie)entry.getCookie(SaveCookie.class);
            if ((savec != null) && !entry.getPropertiesEditor().hasOpenEditorComponent())
                return true;
        }
        return false;
    }

    // copied from OpenSupport
    /** Environment that connects the support together with DataObject.
    */
    public static class Env extends Object implements CloneableOpenSupport.Env, java.io.Serializable,
    PropertyChangeListener, VetoableChangeListener {
        /** generated Serialized Version UID */
//        static final long serialVersionUID = -1934890789745432531L;
        /** object to serialize and be connected to*/
        private DataObject obj;
        
        /** support for firing of property changes
        */
        private transient PropertyChangeSupport propSupp;
        /** support for firing of vetoable changes
        */
        private transient VetoableChangeSupport vetoSupp;

        /** Constructor. Attaches itself as listener to 
        * the data object so, all property changes of the data object
        * are also rethrown to own listeners.
        *
        * @param obj data object to be attached to
        */
        public Env (DataObject obj) {
            this.obj = obj;
            obj.addPropertyChangeListener (WeakListener.propertyChange (this, obj));
        }
        
        /** Getter for data object.
        */
        protected final DataObject getDataObject () {
            return obj;
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
            return getDataObject ().isModified ();
        }

        /** Support for marking the environement modified.
        * @exception IOException if the environment cannot be marked modified
        *   (for example when the file is readonly), when such exception
        *   is the support should discard all previous changes
        */
        public void markModified() throws java.io.IOException {
            getDataObject ().setModified (true);
        }
        
        /** Reverse method that can be called to make the environment 
        * unmodified.
        */
        public void unmarkModified() {
            getDataObject ().setModified (false);
        }
        
        /** Method that allows environment to find its 
         * cloneable open support.
        * @return the support or null if the environemnt is not in valid 
        * state and the CloneableOpenSupport cannot be found for associated
        * data object
        */
        public CloneableOpenSupport findCloneableOpenSupport() {
            return (CloneableOpenSupport)getDataObject ().getCookie (CloneableOpenSupport.class);
        }
        
        /** Accepts property changes from DataObject and fires them to
        * own listeners.
        */
        public void propertyChange(PropertyChangeEvent ev) {
            if (DataObject.PROP_MODIFIED.equals (ev.getPropertyName())) {
                if (getDataObject ().isModified ()) {
                    getDataObject ().addVetoableChangeListener(this);
                } else {
                    getDataObject ().removeVetoableChangeListener(this);
                }
            }
            
            firePropertyChange (
                ev.getPropertyName (),
                ev.getOldValue (),
                ev.getNewValue ()
            );
        }
        
        /** Accepts vetoable changes and fires them to own listeners.
        */
        public void vetoableChange(PropertyChangeEvent ev) throws PropertyVetoException {
            fireVetoableChange (
                ev.getPropertyName (),
                ev.getOldValue (),
                ev.getNewValue ()
            );
        }
        
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
    } // end of Env
    
    
    /** Class for opening at a given key. */
    public class PropertiesOpenAt implements OpenCookie {

        private String key;

        PropertiesOpenAt(String key) {
            this.key   = key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public void open() {
            PropertiesCloneableTopComponent editor = (PropertiesCloneableTopComponent)openCloneableTopComponentPublic();
            PropertiesOpen.this.open();
            BundleStructure bs = obj.getBundleStructure();
            // find the entry
            int entryIndex = bs.getEntryIndexByFileName(obj.getPrimaryEntry().getFile().getName());
            int rowIndex   = bs.getKeyIndexByName(key);
            if ((entryIndex != -1) && (rowIndex != -1)) {
                editor.editCellAt(rowIndex, entryIndex + 1);
            }
            editor.requestFocus();
        }

    }

    /** Cloneable top component which represents table view of resource bundles. */
    public static class PropertiesCloneableTopComponent extends CloneableTopComponent {

        private PropertiesDataObject dobj;
        private transient PropertyChangeListener cookieL;
        private transient JPanel mainPanel;

        private static Image icon = null;

        /** The string which will be appended to the name of top component
        * when top component becomes modified */
        protected String modifiedAppendix = " *";

        static final long serialVersionUID =2836248291419024296L;
        /** Default constructor for deserialization */
        public PropertiesCloneableTopComponent() {
        }

        /** Constructor
        * @param obj data object we belong to
        */
        public PropertiesCloneableTopComponent (final PropertiesDataObject obj) {
            super (obj);
            dobj  = obj;
            initMe();
        }

        public Image getIcon () {
            if (icon == null)
                icon = Toolkit.getDefaultToolkit().getImage(getClass().getResource (
                            "/org/netbeans/modules/properties/propertiesEditorMode.gif"));
            return icon;
        }

        private void initMe() {
            // force closing panes in all workspaces, default is in current only
            setCloseOperation(TopComponent.CLOSE_EACH);
            // add to OpenSupport - patch for a bug in deserialization
            dobj.getOpenSupport().setRef(getReference());

            setName(dobj.getNodeDelegate().getDisplayName());

            // listen to saving and renaming
            dobj.addPropertyChangeListener(WeakListener.propertyChange(cookieL =
                                               new PropertyChangeListener() {
                                                   public void propertyChange(PropertyChangeEvent evt) {
                                                       if (DataObject.PROP_NAME.equals(evt.getPropertyName()) || DataObject.PROP_COOKIE.equals(evt.getPropertyName())) 
                                                           setName(dobj.getNodeDelegate().getDisplayName());
                                                   }
                                               }, dobj));

            initComponents();

            // dock into editor mode if possible
            Workspace[] currentWs = TopManager.getDefault().getWindowManager().getWorkspaces();
            for (int i = currentWs.length; --i >= 0; ) {
                Mode editorMode = currentWs[i].findMode(EditorSupport.EDITOR_MODE);
                if (editorMode == null) {
                    editorMode = currentWs[i].createMode(
                                     EditorSupport.EDITOR_MODE, getName(),
                                     EditorSupport.class.getResource(
                                         "/org/openide/resources/editorMode.gif"
                                     )
                                 );
                }
                editorMode.dockInto(this);
            }
        }

        public HelpCtx getHelpCtx () {
            return new HelpCtx (PropertiesCloneableTopComponent.class);
        }

        /** Set editable specified cell of table view.
         * @param row Row index of cell to edit. 
         * @param column Column index of cell to edit. 
         */
        public void editCellAt(final int row,final int column) {
            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        JTable table = ((BundleEditPanel)mainPanel).getTable();
                        // Autoscroll to cell if possible and necessary.
                        if (table.getAutoscrolls()) { 
                            Rectangle cellRect = table.getCellRect(row, column, false);
                            if (cellRect != null) {
                                table.scrollRectToVisible(cellRect);
                            }
                        }
                        // Update selection & edit.
                        table.getColumnModel().getSelectionModel().setSelectionInterval(row, column);
                        table.getSelectionModel().setSelectionInterval(row, column);

                        ((BundleEditPanel)mainPanel).stopEditing();
                        table.editCellAt(row, column);
                    }
                }
            );
        }

        /** Inits the subcomponents. */
        private void initComponents() {
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();
            setLayout (gridbag);

            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1.0;
            c.weighty = 1.0;
            c.gridwidth = GridBagConstraints.REMAINDER;
            mainPanel = new BundleEditPanel(dobj, new PropertiesTableModel(dobj));
            gridbag.setConstraints(mainPanel, c);
            add (mainPanel);
        }

        /** Set the name of this top component. Handles saved/not saved state.
        * Notifies the window manager.
        * @param displayName the new display name
        */
        public void setName (final String name) {
            String saveAwareName = name;
            if (dobj != null)
                if (dobj.getCookie(SaveCookie.class) != null)
                    saveAwareName = name + modifiedAppendix;
                else
                    saveAwareName = name;

            if ((saveAwareName != null) && (saveAwareName.equals(getName())))
                return;
            super.setName(saveAwareName);
        }
        
        /** 
        * Overrides superclass method.
        * Gets compound UndoRedo manager from all UndozRedo managers from all editor supports. 
        */
        public UndoRedo getUndoRedo () {
            return dobj.getOpenSupport().getUndoRedo();
        }

        /** When closing last view, also close the document.
         * @return <code>true</code> if close succeeded
        */
        protected boolean closeLast () {
            if (!dobj.getOpenSupport().canClose ()) {
                // if we cannot close the last window
                return false;
            }
            dobj.getOpenSupport().closeDocuments();

            return true;
        }

        /** Is called from the clone method to create new component from this one.
        * This implementation only clones the object by calling super.clone method.
        * @return the copy of this object
        */
        protected CloneableTopComponent createClonedObject () {

            PropertiesCloneableTopComponent pctc = new PropertiesCloneableTopComponent (dobj);


            /*String PROPERTIES_MODE = "org.netbeans.modules.properties";
            Workspace cur = TopManager.getDefault().getWindowManager().getCurrentWorkspace();
            Mode m = cur.findMode(PROPERTIES_MODE);
            if (m == null) {
              m = cur.createMode(PROPERTIES_MODE, 
                                 NbBundle.getBundle(PropertiesOpen.class).getString("LAB_PropertiesModeName"),
                                 null);
            } */
            // PENDING
            //m.setBounds(new Rectangle(x, y, width, height));

            return pctc;
        }

        /** This method is called when parent window of this component has focus,
        * and this component is preferred one in it.
        * Overrides superclass's method. Sets action performer for Find action of this componnet.
        */
        protected void componentActivated () {
            // Set our action performer for Find action.
            RequestProcessor.postRequest(new Runnable() {
                public void run() {
                    CallbackSystemAction action = (CallbackSystemAction) SystemAction.get(FindAction.class);
                    action.setActionPerformer(FindPerformer.getFindPerformer(((BundleEditPanel)mainPanel).getTable()));
                }
            });
        }

        /**
        * This method is called when parent window of this component losts focus,
        * or when this component losts preferrence in the parent window.
        * Overrides superclass's method. Unsets action performer for Find action of this component.
        */
        protected void componentDeactivated () {
            // Unset our action performer for Find action.
            RequestProcessor.postRequest(new Runnable() {
                public void run() {
                    CallbackSystemAction action = (CallbackSystemAction) SystemAction.get(FindAction.class);
                    action.setActionPerformer(null);
                }
            });
        }

        /** Serialize this top component.
        * Subclasses wishing to store state must call the super method, then write to the stream.
        * @param out the stream to serialize to
        */
        public void writeExternal (ObjectOutput out)
        throws IOException {
            super.writeExternal(out);
            out.writeObject(dobj);

            // PATCH serialize secondaries this way due to bug (probably jdk1.2 only)
            // HashSet s contains elements which are referencies to secondary entries
            HashSet s = (HashSet)dobj.secondaryEntries();
            out.writeObject(s);
        }

        /** Deserialize this top component.
        * Subclasses wishing to store state must call the super method, then read from the stream.
        * @param in the stream to deserialize from
        */
        public void readExternal (ObjectInput in)
        throws IOException, ClassNotFoundException {
            super.readExternal(in);
            dobj = (PropertiesDataObject)in.readObject();
            
            // deserialize secondaries this way see writeExternal
            HashSet s = (HashSet)in.readObject();
            
            initMe();
        }

    }


    /** Listens to modifications and updates save cookie. */
    private final class ModifiedListener implements SaveCookie, PropertyChangeListener {

        /** Gives notification that the DataObject was changed.
        * @param ev PropertyChangeEvent
        */
        public void propertyChange(PropertyChangeEvent ev) {
            // data object changed, reset the UndoRedo manager
            if(ev.getSource().equals(obj))
                ((CompoundUndoRedoManager)PropertiesOpen.this.getUndoRedo()).reset(obj);

            if ((ev.getSource() == obj) &&
                    (DataObject.PROP_MODIFIED.equals(ev.getPropertyName()))) {
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
            saveDocument();
        }

        /** Save the document in this thread.
        * Create "orig" document for the case that the save would fail.
        * @exception IOException on I/O error
        */
        public void saveDocument () throws IOException {
            final FileObject file = obj.getPrimaryEntry().getFile();
            PropertiesFileEntry pfe = (PropertiesFileEntry)obj.getPrimaryEntry();
            SaveCookie save = (SaveCookie)pfe.getCookie(SaveCookie.class);
            if (save != null)
                save.save();
            for (Iterator it = obj.secondaryEntries().iterator(); it.hasNext();) {
                save = (SaveCookie)((PropertiesFileEntry)it.next()).getCookie(SaveCookie.class);
                if (save != null)
                    save.save();
            }
        }

        /** Adds save cookie to the dataobject.
        */
        private void addSaveCookie() {
            if (obj.getCookie(SaveCookie.class) == null) {
                obj.getCookieSet().add(this);
            }
        }
        
        /** Removes save cookie from the dataobject.
        */
        private void removeSaveCookie() {
            if (obj.getCookie(SaveCookie.class) == this) {
                obj.getCookieSet().remove(this);
            }
        }

    } // end of SavingManager inner class

    /** UndoRedo manager for PropertiesOpen support. It contains weak references to all UndoRedo  
     * managers from all PropertiesEditor supports (for each entry of dataobject one manager). 
     * It uses it's "timeStamp" methods to find out
     * which one of these managers comes to play. */
    private static class CompoundUndoRedoManager implements UndoRedo {
        
        /** Set of weak references to all "underlying" editor support undoredo managers. */
        private WeakSet managers = new WeakSet(5);
        
        // Constructor
        
        /** Collects all UndoRedo managers from all editor support of all entries. */
        public CompoundUndoRedoManager(PropertiesDataObject obj) {
            init(obj);
        }

        /** Initialize set of managers. */
        private void init(PropertiesDataObject obj) {
            managers.add( ((PropertiesFileEntry)obj.getPrimaryEntry()).getPropertiesEditor().getUndoRedoManager());
            for (Iterator it = obj.secondaryEntries().iterator(); it.hasNext(); ) {
                managers.add( ((PropertiesFileEntry)it.next()).getPropertiesEditor().getUndoRedoManager() );
            } 
        }

        /** Resets the managers. Used when data object has changed. */
        public synchronized void reset(PropertiesDataObject obj) {
            managers.clear();
            init(obj);
        }

        /** Gets manager which undo edit comes to play.*/
        private UndoRedo getNextUndo() {
            UndoRedo chosenManager = null;
            long time = 0L; // time to compare with
            long timeManager; // time of next undo of actual manager
            
            for (Iterator it = managers.iterator(); it.hasNext(); ) {
                PropertiesEditorSupport.UndoRedoStampFlagManager manager = (PropertiesEditorSupport.UndoRedoStampFlagManager)it.next();
                timeManager = manager.getTimeStampOfEditToBeUndone();
                if(timeManager > time) {
                    time = timeManager;
                    chosenManager = manager;
                }
            }
            return chosenManager;
        }
        
        /** Gets manager which redo edit comes to play.*/
        private UndoRedo getNextRedo() {
            UndoRedo chosenManager = null;
            long time = 0L; // time to compare with
            long timeManager; // time of next redo of actual manager
            
            for (Iterator it = managers.iterator(); it.hasNext(); ) {
                PropertiesEditorSupport.UndoRedoStampFlagManager manager = (PropertiesEditorSupport.UndoRedoStampFlagManager)it.next();
                timeManager = manager.getTimeStampOfEditToBeRedone();
                if(timeManager > time) {
                    time = timeManager;
                    chosenManager = manager;
                }
            }
            return chosenManager;
        }
        
        //////////////////////////
        // UndoRedo implementation
        
        /** Test whether at least one of managers can Undo.
        * @return <code>true</code> if undo is allowed
        */
        public synchronized boolean canUndo () {
            for (Iterator it = managers.iterator(); it.hasNext(); ) {
                if( ((UndoRedo)it.next()).canUndo() )
                    return true;
            }
            return false;
        }

        /** Test whether at least one of managers can Redo.
        * @return <code>true</code> if redo is allowed
        */
        public synchronized boolean canRedo () {
            for (Iterator it = managers.iterator(); it.hasNext(); ) {
                if( ((UndoRedo)it.next()).canRedo() )
                    return true;
            }
            return false;
        }

        /** Undo an edit. It finds a manager which next undo edit has the highest 
        * time stamp and makes undo on it.
        * @exception CannotUndoException if it fails
        */
        public synchronized void undo () throws CannotUndoException {
            PropertiesEditorSupport.UndoRedoStampFlagManager chosenManager = (PropertiesEditorSupport.UndoRedoStampFlagManager)getNextUndo();

            if(chosenManager == null)
                throw new CannotUndoException();
            else {
                Object atomicFlag = chosenManager.getAtomicFlagOfEditToBeUndone();
                if(atomicFlag == null) // not linked with other edits as one atomic action
                    chosenManager.undo();
                else { // atomic undo compound from more edits in underlying managers
                    boolean undone;
                    do { // the atomic action can consists from more undo edits from same manager
                        undone = false;
                        for (Iterator it = managers.iterator(); it.hasNext(); ) {
                            PropertiesEditorSupport.UndoRedoStampFlagManager manager = (PropertiesEditorSupport.UndoRedoStampFlagManager)it.next();
                            if(atomicFlag.equals(manager.getAtomicFlagOfEditToBeUndone())) {
                                manager.undo();
                                undone = true;
                            }
                        }
                    } while(undone);
                }
            }
        }

        /** Redo a previously undone edit. It finds a manager which next undo edit has the highest 
        * time stamp and makes undo on it.
        * @exception CannotRedoException if it fails
        */
        public synchronized void redo () throws CannotRedoException {
            PropertiesEditorSupport.UndoRedoStampFlagManager chosenManager = (PropertiesEditorSupport.UndoRedoStampFlagManager)getNextRedo();

            if(chosenManager == null)
                throw new CannotRedoException();
            else {
                Object atomicFlag = chosenManager.getAtomicFlagOfEditToBeRedone();
                if(atomicFlag == null) // not linked with other edits as one atomic action
                    chosenManager.redo();
                else { // atomic redo compound from more edits in underlying managers
                    boolean redone;
                    do { // the atomic action can consists from more redo edits from same manager
                        redone = false;
                        for (Iterator it = managers.iterator(); it.hasNext(); ) {
                            PropertiesEditorSupport.UndoRedoStampFlagManager manager = (PropertiesEditorSupport.UndoRedoStampFlagManager)it.next();
                            if(atomicFlag.equals(manager.getAtomicFlagOfEditToBeRedone())) {
                                manager.redo();
                                redone = true;
                            }
                        }
                    } while(redone);
                }
            }
        }

        /** Empty implementation. Does nothing.
        * @param l the listener to add
        */
        public void addChangeListener (ChangeListener l) {
            // PENDING up to now listen on separate managers
        }

        /** Empty implementation. Does nothing.
        * @param l the listener to remove
        * @see #addChangeListener
        */
        public void removeChangeListener (ChangeListener l) {
            // PENDING
        }

        /** Get a human-presentable name describing the
        * undo operation.
        * @return the name
        */
        public synchronized String getUndoPresentationName () {
            UndoRedo chosenManager = getNextUndo();

            if(chosenManager == null)
                return "Undo"; // NOI18N // AbstractUndoableEdit.UndoName is not accessible
            else
                return chosenManager.getUndoPresentationName();
        }

        /** Get a human-presentable name describing the
        * redo operation.
        * @return the name
        */
        public synchronized String getRedoPresentationName () {
            UndoRedo chosenManager = getNextRedo();
            if(chosenManager == null)
                return "Redo"; // NOI18N // AbstractUndoableEdit.RedoName is not accessible
            else
                return chosenManager.getRedoPresentationName();
        }
        
    } // end of CompoundUndoRedoManager
    
}
