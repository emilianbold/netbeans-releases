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
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.*;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.*;
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
import org.openide.text.CloneableEditorSupport;
import org.openide.TopManager;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.*;
import org.openide.windows.*;


/** Support for opening properties files (OpenCookie) in visual editor */
public class PropertiesOpen extends CloneableOpenSupport implements OpenCookie {

    /** Main properties dataobject */
    PropertiesDataObject propDataObject;
    
    /** Listener for modificationc on dataobject, adding and removing save cookie */
    PropertyChangeListener modifL;

    /** UndoRedo manager for this properties open support */
    protected transient UndoRedo.Manager undoRedoManager;

    /** This object is used for marking all undoable edits performed as one atomic undoable action. */
    transient Object atomicUndoRedoFlag;
    

    /** Constructor */
    public PropertiesOpen(PropertiesDataObject propDataObject) {
        super(new Environment(propDataObject));
        this.propDataObject = propDataObject;
        
        this.propDataObject.addPropertyChangeListener(WeakListener.propertyChange(modifL = 
            new ModifiedListener(), this.propDataObject));
    }


    /** 
     * Overrides superclass method.
     * Tests whether all data is saved, and if not, prompt the user to save.
     * @return <code>true</code> if everything can be closed
     */
    protected boolean canClose() {
        SaveCookie savec = (SaveCookie)propDataObject.getCookie(SaveCookie.class);
        if (savec != null) {
            if (!shouldAskSave())
                return true;
            MessageFormat format = new MessageFormat(NbBundle.getBundle(PropertiesOpen.class).getString("MSG_SaveFile"));
            String msg = format.format(new Object[] { propDataObject.getName()});
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.YES_NO_CANCEL_OPTION);
            Object ret = TopManager.getDefault().notify(nd);


            if (NotifyDescriptor.YES_OPTION.equals(ret)) {
                try {
                    savec.save();
                    propDataObject.updateModificationStatus();
                }
                catch (IOException e) {
                    TopManager.getDefault().notifyException(e);
                    return false;
                }
            }

            propDataObject.updateModificationStatus();
            
            if (NotifyDescriptor.CANCEL_OPTION.equals(ret))
                return false;
            
        }
        return true;
    }
    
    /** 
     * Overrides superclass abstract method. 
     * A method to create a new component.
     * @return the cloneable top component for this support
     */
    protected CloneableTopComponent createCloneableTopComponent() {
        return new PropertiesCloneableTopComponent(propDataObject);
    }

    /**
     * Overrides superclass abstract method. 
     * Message to display when an object is being opened.
     * @return the message or null if nothing should be displayed
     */
    protected String messageOpening() {
        return NbBundle.getMessage(OpenSupport.class , "CTL_ObjectOpen", // NOI18N
            propDataObject.getName(),
            propDataObject.getPrimaryFile().toString()
        );
    }

    /** 
     * Overrides superclass abstract method.
     * Message to display when an object has been opened.
     * @return the message or null if nothing should be displayed
     */
    protected String messageOpened() {
        return NbBundle.getMessage(OpenSupport.class, "CTL_ObjectOpened"); // NOI18N
    }

    /** @return whether has open table view component. */
    public synchronized boolean hasOpenedTableComponent() {
        return !allEditors.isEmpty();
    }

    /** Gets UndoRedo manager for this OpenSupport. */
    public UndoRedo getUndoRedo () {
        if(undoRedoManager != null)
            return undoRedoManager;
        else
            return new CompoundUndoRedoManager(propDataObject);
    }
    
    /** Helper method. Sets <code>allEditors variable</code>. Used by deserialization. */
    private void setRef(CloneableTopComponent.Ref ref) {
        allEditors = ref;
    }

    /** Helper method. Closes documents. */
    private synchronized void closeDocuments() {
        closeEntry((PropertiesFileEntry)propDataObject.getPrimaryEntry());
        for (Iterator it = propDataObject.secondaryEntries().iterator(); it.hasNext(); ) {
            closeEntry((PropertiesFileEntry)it.next());
        }
    }

    /** Helper method. Closes entry. */
    private void closeEntry(PropertiesFileEntry entry) {
        PropertiesEditorSupport editorSupport = entry.getPropertiesEditor();
        if (editorSupport.hasOpenedEditorComponent())
            // Has opened editor view for this entry -> don't close document.
            return;
        else {
            // Hasn't opened editor view for this entry -> close document.
            editorSupport.forceNotifyClosed();
            entry.getHandler().reparseNowBlocking();
        }
    }

    /** 
     * Helper method. Should be called only if the object has SaveCookie
     * @return true if closing this editor whithout saving would result in loss of data
     *  because al least one of the modified files is not open in the code editor
     */
    private boolean shouldAskSave() {
        // for each entry : if there is a SaveCookie and no open editor component, return true.
        // if passed for all entries, return false
        PropertiesFileEntry entry = (PropertiesFileEntry)propDataObject.getPrimaryEntry();
        SaveCookie savec = (SaveCookie)entry.getCookie(SaveCookie.class);
        
        if ((savec != null) && !entry.getPropertiesEditor().hasOpenedEditorComponent())
            return true;
        for (Iterator it = propDataObject.secondaryEntries().iterator(); it.hasNext(); ) {
            entry = (PropertiesFileEntry)it.next();
            savec = (SaveCookie)entry.getCookie(SaveCookie.class);
            if ((savec != null) && !entry.getPropertiesEditor().hasOpenedEditorComponent())
                return true;
        }
        return false;
    }

    
    /** Nested class. Environment that connects the open support together with <code>DataObject</code>. */
    private static class Environment implements CloneableOpenSupport.Env, Serializable,
        PropertyChangeListener, VetoableChangeListener {
            
        /** Generated Serialized Version UID */
        static final long serialVersionUID = -1934890789745432531L;
        
        /** Object to serialize and be connected to. */
        private DataObject dataObject;
        
        /** Support for firing of property changes. */
        private transient PropertyChangeSupport propSupp;
        
        /** Support for firing of vetoable changes. */
        private transient VetoableChangeSupport vetoSupp;

        
        /** 
         * Constructor. Attaches itself as listener to 
         * the data object so, all property changes of the data object
         * are also rethrown to own listeners.
         * @param dataObject data object to be attached to
         */
        public Environment (DataObject dataObject) {
            this.dataObject = dataObject;
            dataObject.addPropertyChangeListener(WeakListener.propertyChange(this, dataObject));
        }

        
        /** Implements <code>CloneableOpenSupport.Env</code> interface. Adds property listener. */
        public void addPropertyChangeListener(PropertyChangeListener l) {
            prop().addPropertyChangeListener(l);
        }

        /** Implements <code>CloneableOpenSupport.Env</code> interface. Removes property listener. */
        public void removePropertyChangeListener(PropertyChangeListener l) {
            prop().removePropertyChangeListener(l);
        }

        /** Implements <code>CloneableOpenSupport.Env</code> interface. Adds veto listener. */
        public void addVetoableChangeListener(VetoableChangeListener l) {
            veto().addVetoableChangeListener(l);
        }

        /** Implements <code>CloneableOpenSupport.Env</code> interface. Removes veto listener. */
        public void removeVetoableChangeListener(VetoableChangeListener l) {
            veto().removeVetoableChangeListener(l);
        }

        /** 
         * Implements <code>CloneableOpenSupport</code> interface.
         * Method that allows environment to find its cloneable open support.
         * @return the support or null if the environemnt is not in valid 
         * state and the CloneableOpenSupport cannot be found for associated
         * data object
         */
        public CloneableOpenSupport findCloneableOpenSupport() {
            return (CloneableOpenSupport)dataObject.getCookie(CloneableOpenSupport.class);
        }
        
        /** 
         * Implements <code>CloneableOpenSupport.Env</code> interface.
         * Test whether the support is in valid state or not.
         * It could be invalid after deserialization when the object it
         * referenced to does not exist anymore.
         * @return true or false depending on its state
         */
        public boolean isValid() {
            return dataObject.isValid();
        }
        
        /**
         * Implements <code>CloneableOpenSupport.Env</code> interface. 
         * Test whether the object is modified or not.
         * @return true if the object is modified
         */
        public boolean isModified() {
            return dataObject.isModified();
        }

        /**
         * Implements <code>CloneableOpenSupport.Env</code> interface. 
         * Support for marking the environement modified.
         * @exception IOException if the environment cannot be marked modified
         *   (for example when the file is readonly), when such exception
         *   is the support should discard all previous changes
         */
        public void markModified() throws java.io.IOException {
            dataObject.setModified(true);
        }
        
        /** 
         * Implements <code>CloneableOpenSupport.Env</code> interface.
         * Reverse method that can be called to make the environment unmodified.
         */
        public void unmarkModified() {
            dataObject.setModified(false);
        }
        
        /** 
         * Implements <code>PropertyChangeListener</code> interface.
         * Accepts property changes from <code>DataObject</code> and fires them to own listeners.
         */
        public void propertyChange(PropertyChangeEvent evt) {
            if (DataObject.PROP_MODIFIED.equals (evt.getPropertyName())) {
                if (dataObject.isModified ()) {
                    dataObject.addVetoableChangeListener(this);
                } else {
                    dataObject.removeVetoableChangeListener(this);
                }
            }
            
            firePropertyChange (
                evt.getPropertyName(),
                evt.getOldValue(),
                evt.getNewValue()
            );
        }
        
        /**
         * Implements <code>VetoAbleChangeListener</code> interface. 
         * Accepts vetoable changes and fires them to own listeners.
         */
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            fireVetoableChange (
                evt.getPropertyName(),
                evt.getOldValue(),
                evt.getNewValue()
            );
        }
        
        /** Fires property change.
        * @param name the name of property that changed
        * @param oldValue old value
        * @param newValue new value
        */
        private void firePropertyChange (String name, Object oldValue, Object newValue) {
            prop().firePropertyChange(name, oldValue, newValue);
        }
        
        /** Fires vetoable change.
        * @param name the name of property that changed
        * @param oldValue old value
        * @param newValue new value
        */
        private void fireVetoableChange (String name, Object oldValue, Object newValue) throws PropertyVetoException {
            veto().fireVetoableChange(name, oldValue, newValue);
        }
        
        /** Lazy gets property change support. */
        private PropertyChangeSupport prop() {
            if(propSupp == null) {
                synchronized(this) {
                    if(propSupp == null) {
                        propSupp = new PropertyChangeSupport(this);
                    }
                }
            }
            return propSupp;
        }
        
        /** Lazy gets vetoable change support. */
        private VetoableChangeSupport veto() {
            if(vetoSupp == null) {
                synchronized(this) {
                    if(vetoSupp == null) {
                        vetoSupp = new VetoableChangeSupport(this);
                    }
                }
            }
            return vetoSupp;
        }
    } // End of inner class Environment.
    
    
    /** Inner class. Listens to modifications and updates save cookie. */
    private final class ModifiedListener implements SaveCookie, PropertyChangeListener {

        /** Gives notification that the DataObject was changed.
        * @param ev PropertyChangeEvent
        */
        public void propertyChange(PropertyChangeEvent evt) {
            // Data object changed, reset the UndoRedo manager.
            if(evt.getSource().equals(propDataObject))
                ((CompoundUndoRedoManager)PropertiesOpen.this.getUndoRedo()).reset(propDataObject);

            if ((evt.getSource() == propDataObject) && (DataObject.PROP_MODIFIED.equals(evt.getPropertyName()))) {
                if (((Boolean)evt.getNewValue()).booleanValue()) {
                    addSaveCookie();
                } else {
                    removeSaveCookie();
                }
            }
        }

        /** Implements <code>SaveCookie</code> interface. */
        public void save() throws IOException {
            // do saving job
            saveDocument();
        }

        /** Save the document in this thread.
        * Create "orig" document for the case that the save would fail.
        * @exception IOException on I/O error
        */
        public void saveDocument() throws IOException {
            final FileObject file = propDataObject.getPrimaryEntry().getFile();
            PropertiesFileEntry pfe = (PropertiesFileEntry)propDataObject.getPrimaryEntry();
            SaveCookie save = (SaveCookie)pfe.getCookie(SaveCookie.class);
            if (save != null)
                save.save();
            for (Iterator it = propDataObject.secondaryEntries().iterator(); it.hasNext();) {
                save = (SaveCookie)((PropertiesFileEntry)it.next()).getCookie(SaveCookie.class);
                if(save != null)
                    save.save();
            }
        }

        /** Adds save cookie to the dataobject. */
        private void addSaveCookie() {
            if(propDataObject.getCookie(SaveCookie.class) == null) {
                propDataObject.getCookieSet().add(this);
            }
        }
        
        /** Removes save cookie from the dataobject. */
        private void removeSaveCookie() {
            if(propDataObject.getCookie(SaveCookie.class) == this) {
                propDataObject.getCookieSet().remove(this);
            }
        }
    } // End of inner class ModifiedListener.

    
    /** Inner class for opening at a given key. */
    public class PropertiesOpenAt implements OpenCookie {

        /** Entry the key belongs to. */
        private PropertiesFileEntry entry;
        
        /** Key where to open at. */
        private String key;

        
        /** Construcor. */
        PropertiesOpenAt(PropertiesFileEntry entry, String key) {
            this.entry = entry;
            this.key   = key;
        }

        
        /** Setter for key property. */
        public void setKey(String key) {
            this.key = key;
        }

        /** Implememnts <code>OpenCookie</code>. Opens document. */
        public void open() {
            // Instead of PropertiesOpen.super.open() so we get reference to TopComponent.
            // Note: It is strange for me that calling PropetiesOpen.this.openCloneableTopComponent throw s exception at run-time.
            final PropertiesCloneableTopComponent editor = (PropertiesCloneableTopComponent)PropertiesOpen.super.openCloneableTopComponent();
            editor.requestFocus();
            
            BundleStructure bs = propDataObject.getBundleStructure();
            // Find indexes.
            int entryIndex = bs.getEntryIndexByFileName(entry.getFile().getName());
            int rowIndex   = bs.getKeyIndexByName(key);
            
            if ((entryIndex != -1) && (rowIndex != -1)) {
                final int row = rowIndex;
                final int column = entryIndex + 1;

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        JTable table = ((BundleEditPanel)editor.getComponent(0)).getTable();
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

                        table.editCellAt(row, column);
                    }
                });
            }
        }
    } // End of inner class PropertiesOpenAt.

    
    /** Cloneable top component which represents table view of resource bundles. */
    public static class PropertiesCloneableTopComponent extends CloneableTopComponent {

        /** Reference to underlying <code>PropertiesDataObject</code>. */
        private PropertiesDataObject propDataObject;
        
        /** Listener for changes on <code>propDataObject</code> name and cookie properties.
         * Changes display name of components accordingly. */
        private transient PropertyChangeListener dataObjectListener;
        
        /** Icon. */
        private static Image icon = null;

        /** Generated serial version UID. */
        static final long serialVersionUID =2836248291419024296L;
        
        
        /** Default constructor for deserialization. */
        public PropertiesCloneableTopComponent() {
        }

        /** Constructor.
        * @param propDataObject data object we belong to */
        public PropertiesCloneableTopComponent (PropertiesDataObject propDataObject) {
            super(propDataObject);
            this.propDataObject  = propDataObject;
            initialize();
        }

        
        /** Initializes this instance. Used by construction and deserialization. */
        private void initialize() {
            initComponents();
            
            // force closing panes in all workspaces, default is in current only
            setCloseOperation(TopComponent.CLOSE_EACH);
            
            // add to CloneableOpenSupport - patch for a bug in deserialization
            propDataObject.getOpenSupport().setRef(getReference());

            setName(propDataObject.getNodeDelegate().getDisplayName());

            // Listen to saving and renaming.
            propDataObject.addPropertyChangeListener(WeakListener.propertyChange(
                dataObjectListener = new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (DataObject.PROP_NAME.equals(evt.getPropertyName()) || DataObject.PROP_COOKIE.equals(evt.getPropertyName())) 
                            setName(propDataObject.getNodeDelegate().getDisplayName());
                    }
            }, propDataObject));
            
            // dock into editor mode if possible
            Workspace[] currentWs = TopManager.getDefault().getWindowManager().getWorkspaces();
            for (int i = currentWs.length; --i >= 0; ) {
                Mode editorMode = currentWs[i].findMode(CloneableEditorSupport.EDITOR_MODE);
                if (editorMode == null) {
                    editorMode = currentWs[i].createMode(
                        CloneableEditorSupport.EDITOR_MODE, 
                        getName(),
                        CloneableEditorSupport.class.getResource("/org/openide/resources/editorMode.gif")); // NOI18N
                }
                editorMode.dockInto(this);
            }
        }

        /** 
         * Overrides superclass method. When closing last view, also close the document.
         * @return <code>true</code> if close succeeded
         */
        protected boolean closeLast () {
            if (!propDataObject.getOpenSupport().canClose ()) {
                // if we cannot close the last window
                return false;
            }
            propDataObject.getOpenSupport().closeDocuments();

            return true;
        }

        /**
         * Overrides superclass method. 
         * Is called from the superclass <code>clone<code> method to create new component from this one.
         * This implementation only clones the object by calling super.clone method.
         * @return the copy of this object
         */
        protected CloneableTopComponent createClonedObject () {
            return new PropertiesCloneableTopComponent(propDataObject);
        }

        /** Overrides superclass method. Gets <code>Icon</code>. */
        public Image getIcon () {
            if (icon == null)
                icon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/org/netbeans/modules/properties/propertiesEditorMode.gif")); // NOI18N
            return icon;
        }

        /** Overrides superclass method. Gets help context. */
        public HelpCtx getHelpCtx () {
            return new HelpCtx (PropertiesCloneableTopComponent.class);
        }

        /** Overrides superclass method. Set the name of this top component. Handles saved/not saved state.
        * Notifies the window manager.
        * @param displayName the new display name
        */
        public void setName(String name) {
            String saveAwareName = name;
            if (propDataObject != null) {
                if (propDataObject.getCookie(SaveCookie.class) != null) {
                    saveAwareName = NbBundle.getMessage(
                        CloneableEditorSupport.class,
                        "LAB_EditorName_Modified", // NOI18N
                        name
                    );
                } else {
                    saveAwareName = NbBundle.getMessage(
                        CloneableEditorSupport.class,
                        "LAB_EditorName_Uptodate", // NOI18N
                        name
                    );
                }
            }

            super.setName(saveAwareName);
        }
        
        /** 
         * Overrides superclass method.
         * Gets compound UndoRedo manager from all UndozRedo managers from all editor supports. 
         */
        public UndoRedo getUndoRedo () {
            return propDataObject.getOpenSupport().getUndoRedo();
        }

        /** 
         * Overrides superclass method.
         * This method is called when parent window of this component has focus,
         * and this component is preferred one in it.
         * Overrides superclass's method. Sets action performer for Find action of this componnet.
         */
        protected void componentActivated() {
            // Set our action performer for Find action.
            RequestProcessor.postRequest(new Runnable() {
                public void run() {
                    CallbackSystemAction action = (CallbackSystemAction) SystemAction.get(FindAction.class);
                    action.setActionPerformer(FindPerformer.getFindPerformer(((BundleEditPanel)getComponent(0)).getTable()));
                }
            });
        }

        /**
         * Overrides superclass method.
         * This method is called when parent window of this component losts focus,
         * or when this component losts preferrence in the parent window.
         * Overrides superclass's method. Unsets action performer for Find action of this component.
         */
        protected void componentDeactivated() {
            // Unset our action performer for Find action.
            RequestProcessor.postRequest(new Runnable() {
                public void run() {
                    CallbackSystemAction action = (CallbackSystemAction) SystemAction.get(FindAction.class);
                    action.setActionPerformer(null);
                }
            });
        }

        /** Set editable specified cell of table view.
         * @param row Row index of cell to edit. 
         * @param column Column index of cell to edit. 
         */
/*        public void editCellAt(final int row,final int column) {
            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        JTable table = ((BundleEditPanel)getComponent(0)).getTable();
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

                        table.editCellAt(row, column);
                    }
                }
            );
        }*/

        /** Inits the subcomponents. Sets layout for this top component and adds <code>BundleEditPanel</code> to it. 
         * @see BundleEditPanel */
        private void initComponents() {
            GridBagLayout gridbag = new GridBagLayout();
            setLayout(gridbag);

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1.0;
            c.weighty = 1.0;
            c.gridwidth = GridBagConstraints.REMAINDER;
            JPanel panel = new BundleEditPanel(propDataObject, new PropertiesTableModel(propDataObject));
            gridbag.setConstraints(panel, c);
            add(panel);
        }

        /**
         * Overrides superclass method. Serialize this top component.
         * Subclasses wishing to store state must call the super method, then write to the stream.
         * @param out the stream to serialize to
         */
        public void writeExternal (ObjectOutput out) throws IOException {
            super.writeExternal(out);
            out.writeObject(propDataObject);

            // PATCH serialize secondaries this way due to bug (probably jdk1.2 only)
            // HashSet s contains elements which are referencies to secondary entries
            HashSet s = (HashSet)propDataObject.secondaryEntries();
            out.writeObject(s);
        }

        /** 
         * Overrides superclass method. Deserialize this top component.
         * Subclasses wishing to store state must call the super method, then read from the stream.
         * @param in the stream to deserialize from
         */
        public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
            super.readExternal(in);
            propDataObject = (PropertiesDataObject)in.readObject();
            
            // deserialize secondaries this way see writeExternal
            HashSet s = (HashSet)in.readObject();
            
            initialize();
        }
    } // End of nested class PropertiesCloneableTopComponent.


    /** 
     * <code>UndoRedo</code> manager for <code>PropertiesOpen</code> support. It contains weak references 
     * to all UndoRedo managers from all PropertiesEditor supports (for each entry of dataobject one manager). 
     * It uses it's "timeStamp" methods to find out which one of these managers comes to play. 
     */
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
        
        /** Implements <code>UndoRedo</code>. Test whether at least one of managers can Undo.
        * @return <code>true</code> if undo is allowed
        */
        public synchronized boolean canUndo () {
            for (Iterator it = managers.iterator(); it.hasNext(); ) {
                if( ((UndoRedo)it.next()).canUndo() )
                    return true;
            }
            return false;
        }

        /** Implements <code>UndoRedo</code>. Test whether at least one of managers can Redo.
        * @return <code>true</code> if redo is allowed
        */
        public synchronized boolean canRedo () {
            for (Iterator it = managers.iterator(); it.hasNext(); ) {
                if( ((UndoRedo)it.next()).canRedo() )
                    return true;
            }
            return false;
        }

        /** Implements <code>UndoRedo</code>. Undo an edit. It finds a manager which next undo edit has the highest 
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

        /** Implements <code>UndoRedo</code>. Redo a previously undone edit. It finds a manager which next undo edit has the highest 
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

        /** Implements <code>UndoRedo</code>. Empty implementation. Does nothing.
        * @param l the listener to add
        */
        public void addChangeListener (ChangeListener l) {
            // PENDING up to now listen on separate managers
        }

        /** Implements <code>UndoRedo</code>. Empty implementation. Does nothing.
        * @param l the listener to remove
        * @see #addChangeListener
        */
        public void removeChangeListener (ChangeListener l) {
            // PENDING
        }

        /** Implements <code>UndoRedo</code>. Get a human-presentable name describing the
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

        /** Implements <code>UndoRedo</code>. Get a human-presentable name describing the
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
        
    } // End of nested class CompoundUndoRedoManager.
    
}
