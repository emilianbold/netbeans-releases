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

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.*;
import javax.swing.Action;
import javax.swing.event.ChangeListener;
import javax.swing.*;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.openide.actions.FindAction;
import org.openide.awt.UndoRedo;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.OpenSupport;
import org.openide.nodes.Node;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.*;
import org.openide.windows.*;
import org.openide.util.Utilities;
import org.openide.DialogDescriptor;
import org.openide.filesystems.FileUtil;


/** 
 * Support for opening bundle of .properties files (OpenCookie) in table view editor. 
 * 
 * @author Petr Jiricka, Peter Zavadsky
 */
public class PropertiesOpen extends CloneableOpenSupport
                            implements OpenCookie, CloseCookie {

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
        
        this.propDataObject.addPropertyChangeListener(WeakListeners.propertyChange(modifL = 
            new ModifiedListener(), this.propDataObject));
    }


    /** 
     * Tests whether all data is saved, and if not, prompts the user to save.
     *
     * @return <code>true</code> if everything can be closed
     */
    protected boolean canClose() {
        SaveCookie saveCookie = propDataObject.getCookie(SaveCookie.class);
        if (saveCookie == null) {
            return true;
        }
        stopEditing();
        if (!shouldAskSave()) {
            return true;
        }
        
        /* Create and display a confirmation dialog - Save/Discard/Cancel: */
        String title = NbBundle.getMessage(PropertiesOpen.class,
                                           "CTL_Question");         //NOI18N
        String question = NbBundle.getMessage(PropertiesOpen.class,
                                              "MSG_SaveFile",       //NOI18N
                                              propDataObject.getName());
        String optionSave = NbBundle.getMessage(PropertiesOpen.class,
                                                "CTL_Save");        //NOI18N
        String optionDiscard = NbBundle.getMessage(PropertiesOpen.class,
                                                   "CTL_Discard");  //NOI18N
        NotifyDescriptor descr = new DialogDescriptor(
                question,
                title,                              //title
                true,                               //modal
                new Object[] {optionSave,
                              optionDiscard,
                              NotifyDescriptor.CANCEL_OPTION},
                optionSave,                         //default option
                DialogDescriptor.DEFAULT_ALIGN,     //alignment of the options
                null,                               //help context
                (ActionListener) null);
        descr.setMessageType(NotifyDescriptor.QUESTION_MESSAGE);
        Object answer = DialogDisplayer.getDefault().notify(descr);
        
        /* Save the file if the answer was "Save": */
        if (answer == optionSave) {
            try {
                saveCookie.save();
                propDataObject.updateModificationStatus();
            }
            catch (IOException e) {
                ErrorManager.getDefault().notify(e);
                return false;
            }
        }
        propDataObject.updateModificationStatus();

        return (answer == optionSave || answer == optionDiscard);
    }
    
    private void stopEditing() {
        Enumeration en = allEditors.getComponents();
        while (en.hasMoreElements()) {
            Object o = en.nextElement();
            if (o instanceof PropertiesCloneableTopComponent) {
                BundleEditPanel bep = (BundleEditPanel)((PropertiesCloneableTopComponent)o).getComponent(0);
                bep.stopEditing();
            }
        }
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
        return NbBundle.getMessage(PropertiesOpen.class, "LBL_ObjectOpen", // NOI18N
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
        return NbBundle.getMessage(PropertiesOpen.class, "LBL_ObjectOpened"); // NOI18N
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
            
            // #17221. Don't reparse invalid or virtual file.
            if(entry.getFile().isValid() && !entry.getFile().isVirtual()) {
                entry.getHandler().autoParse();
            }
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
        public Environment(PropertiesDataObject dataObject) {
            this.dataObject = dataObject;
            dataObject.addPropertyChangeListener(WeakListeners.propertyChange(this, dataObject));
            dataObject.addVetoableChangeListener(WeakListeners.vetoableChange(this, dataObject));
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
            return (CloneableOpenSupport)dataObject.getCookie(OpenCookie.class);
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
            if(DataObject.PROP_MODIFIED.equals(evt.getPropertyName())) {
                if(dataObject.isModified()) {
                    dataObject.addVetoableChangeListener(this);
                } else {
                    dataObject.removeVetoableChangeListener(this);
                }
            } else if(DataObject.PROP_VALID.equals(evt.getPropertyName ())) { 
                // We will handle the object invalidation here.
                // Do not check it if old value is not true.
                if(Boolean.FALSE.equals(evt.getOldValue())) return;

                // Loosing validity.
                PropertiesOpen support = (PropertiesOpen)findCloneableOpenSupport();
                if(support != null) {
                    
                    // Mark the object as not being modified, so nobody
                    // will ask for save.
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
            stopEditing();
            // do saving job
            saveDocument();
        }

        /** Save the document in this thread.
        * Create "orig" document for the case that the save would fail.
        * @exception IOException on I/O error
        */
        public void saveDocument() throws IOException {
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
                propDataObject.getCookieSet0().add(this);
            }
        }
        
        /** Removes save cookie from the dataobject. */
        private void removeSaveCookie() {
            if(propDataObject.getCookie(SaveCookie.class) == this) {
                propDataObject.getCookieSet0().remove(this);
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
            editor.requestActive();
            
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
        
        /** Generated serial version UID. */
        static final long serialVersionUID =2836248291419024296L;
        
        
        /** Default constructor for deserialization. */
        public PropertiesCloneableTopComponent() {
        }
        
        /** Constructor.
        * @param propDataObject data object we belong to */
        public PropertiesCloneableTopComponent (PropertiesDataObject propDataObject) {
            this.propDataObject  = propDataObject;

            initialize();
        }

        /**
         */
        public void open() {
            if (discard()) {
                return;
            }
            super.open();
        }

        public void requestActive() {
            super.requestActive();
            getComponent(0).requestFocusInWindow();
        }
        
        public boolean canClose () {
            ((BundleEditPanel)getComponent(0)).stopEditing();
            return super.canClose();
        }
        
        /** Initializes this instance. Used by construction and deserialization. */
        private void initialize() {
            initComponents();
            setupActions();
            setActivatedNodes(new Node[] {propDataObject.getNodeDelegate()});

            dataObjectListener = new NameUpdater();
            propDataObject.addPropertyChangeListener(
                    WeakListeners.propertyChange(dataObjectListener,
                                                 propDataObject));
            
            updateName();
        }
        
        /* Based on class DataNode.PropL. */
        final class NameUpdater implements PropertyChangeListener,
                                           FileStatusListener,
                                           Runnable {
            
            /** */
            private static final int NO_ACTION = 0;
            /** */
            private static final int ACTION_UPDATE_NAME = 1;
            /** */
            private static final int ACTION_UPDATE_DISPLAY_NAME = 2;
            
            /** weak version of this listener */
            private FileStatusListener weakL;
            /** previous filesystem we were attached to */
            private FileSystem previous;
            
            /** */
            private final int action;
            
            /**
             */
            NameUpdater() {
                this(NO_ACTION);
                updateStatusListener();
            }
            
            /**
             */
            NameUpdater(int action) {
                this.action = action;
            }
            
            /** Updates listening on a status of filesystem. */
            private void updateStatusListener() {
                if (previous != null) {
                    previous.removeFileStatusListener(weakL);
                }
                try {
                    previous = propDataObject.getPrimaryFile().getFileSystem();
                    if (weakL == null) {
                        weakL = org.openide.filesystems.FileUtil
                                .weakFileStatusListener(this, previous);
                    }
                    previous.addFileStatusListener(weakL);
                } catch (FileStateInvalidException ex) {
                    previous = null;
                }
            }
            
            /**
             * Notifies listener about change in annotataion of a few files.
             */
            public void annotationChanged(FileStatusEvent ev) {
                if (!ev.isNameChange()) {
                    return;
                }
                
                boolean thisChanged = false;
                for (FileObject fo : propDataObject.files()) {
                    if (ev.hasChanged(fo)) {
                        thisChanged = true;
                        break;
                    }
                }
                if (thisChanged) {
                    Mutex.EVENT.writeAccess(
                            new NameUpdater(ACTION_UPDATE_DISPLAY_NAME));
                }
            }
            
            /**
             */
            public void propertyChange(PropertyChangeEvent e) {
                if (!propDataObject.isValid()) {
                    return;
                }
                
                final String property = e.getPropertyName();
                if (property == null) {
                    return;
                }
                if (property.equals(DataObject.PROP_NAME)) {
                    Mutex.EVENT.writeAccess(
                            new NameUpdater(ACTION_UPDATE_NAME));
                } else if (property.equals(DataObject.PROP_PRIMARY_FILE)) {
                    updateStatusListener();
                    Mutex.EVENT.writeAccess(
                            new NameUpdater(ACTION_UPDATE_NAME));
                } else if (property.equals(DataObject.PROP_COOKIE)
                           || property.equals(DataObject.PROP_FILES)) {
                    Mutex.EVENT.writeAccess(
                            new NameUpdater(ACTION_UPDATE_DISPLAY_NAME));
                }
            }
            
            /**
             */
            public void run() {
                assert EventQueue.isDispatchThread();
                
                if (action == ACTION_UPDATE_NAME) {
                    updateName();
                } else if (action == ACTION_UPDATE_DISPLAY_NAME) {
                    updateDisplayName();
                } else {
                    assert false;
                }
            }
            
        }
        
        /**
         * Sets up action Find that it is activated/deactivated appropriately
         * and so that it does what it should do.
         */
        private void setupActions() {
            JTable bundleTable = ((BundleEditPanel) getComponent(0)).getTable();
            FindAction findAction = SystemAction.get(FindAction.class);
            Action action = FindPerformer.getFindPerformer(bundleTable);
            getActionMap().put(findAction.getActionMapKey(), action);
        }
        
        /**
         */
        private void updateName() {
            assert EventQueue.isDispatchThread();
            
            final String name = propDataObject.getName();
            final String displayName = displayName();
            final String htmlDisplayName = htmlDisplayName();
            final String toolTip = messageToolTip();
            
            Enumeration<CloneableTopComponent> en = getReference().getComponents();
            while (en.hasMoreElements()) {
                CloneableTopComponent tc = en.nextElement();
                tc.setName(name);
                tc.setDisplayName(displayName);
                tc.setHtmlDisplayName(htmlDisplayName);
                tc.setToolTipText(toolTip);
            }
        }
        
        /**
         */
        private void updateDisplayName() {
            assert EventQueue.isDispatchThread();
            
            final String displayName = displayName();
            final String htmlDisplayName = htmlDisplayName();
            
            Enumeration<CloneableTopComponent> en = getReference().getComponents();
            while (en.hasMoreElements()) {
                CloneableTopComponent tc = en.nextElement();
                tc.setDisplayName(displayName);
                tc.setHtmlDisplayName(htmlDisplayName);
            }
        }
        
        /**
         */
        private String addModifiedInfo(String name) {
            boolean modified
                    = propDataObject.getCookie(SaveCookie.class) != null;
            int version = modified ? 1 : 3;
            return NbBundle.getMessage(PropertiesCloneableTopComponent.class,
                                       "LBL_EditorName",                //NOI18N
                                       new Integer(version),
                                       name);
        }

        /**
         * Builds a display name for this component.
         *
         * @return  the created display name
         * @see  #htmlDisplayName
         */
        private String displayName() {
            String nameBase = propDataObject.getNodeDelegate().getDisplayName();
            return addModifiedInfo(nameBase);
        }
        
        /**
         * Builds a HTML display name for this component.
         *
         * @return  the created display name
         * @see  #displayName()
         */
        private String htmlDisplayName() {
            final Node node = propDataObject.getNodeDelegate();
            String displayName = node.getHtmlDisplayName();
            if (displayName != null) {
                if (!displayName.startsWith("<html>")) {                //NOI18N
                    displayName = "<html>" + displayName;               //NOI18N
                }
            } else {
                displayName = node.getDisplayName();
            }
            return addModifiedInfo(displayName);
        }
        
        /** Gets string for tooltip. */
        private String messageToolTip() {
            FileObject fo = propDataObject.getPrimaryFile();
            return FileUtil.getFileDisplayName(fo);
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
            return Utilities.loadImage("org/netbeans/modules/properties/propertiesEditorMode.gif"); // NOI18N
        }

        /** Overrides superclass method. Gets help context. */
        public HelpCtx getHelpCtx () {
            return new HelpCtx(Util.HELP_ID_MODIFYING);
        }
        
        protected String preferredID() {
            return getName();
        }
        
        public int getPersistenceType() {
            return PERSISTENCE_ONLY_OPENED;
        }
        
        /** 
         * Overrides superclass method.
         * Gets compound UndoRedo manager from all UndozRedo managers from all editor supports. 
         */
        public UndoRedo getUndoRedo () {
            return propDataObject.getOpenSupport().getUndoRedo();
        }

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
            JPanel panel = new BundleEditPanel(propDataObject, new PropertiesTableModel(propDataObject.getBundleStructure()));
            gridbag.setConstraints(panel, c);
            add(panel);
        }
        
        /** This component should be discarded if the associated environment
         *  is not valid.
         */
        private boolean discard () {
            return propDataObject == null;
        }
        

        /**
         * Overrides superclass method. Serialize this top component.
         * Subclasses wishing to store state must call the super method, then write to the stream.
         * @param out the stream to serialize to
         */
        public void writeExternal (ObjectOutput out) throws IOException {
            super.writeExternal(out);
            out.writeObject(propDataObject);
        }

        /** 
         * Overrides superclass method. Deserialize this top component.
         * Subclasses wishing to store state must call the super method, then read from the stream.
         * @param in the stream to deserialize from
         */
        public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
            super.readExternal(in);

            propDataObject = (PropertiesDataObject)in.readObject();
            
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
        private WeakSet<Manager> managers = new WeakSet<Manager>(5);
        
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
            
            for (Iterator<Manager> it = managers.iterator(); it.hasNext(); ) {
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
            
            for (Iterator<Manager> it = managers.iterator(); it.hasNext(); ) {
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
            for (Manager manager : managers) {
                if (manager.canUndo()) {
                    return true;
                }
            }
            return false;
        }

        /** Implements <code>UndoRedo</code>. Test whether at least one of managers can Redo.
         * @return <code>true</code> if redo is allowed
         */
        public synchronized boolean canRedo () {
            for (Manager manager : managers) {
                if (manager.canRedo()) {
                    return true;
                }
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
                        for (Iterator<Manager> it = managers.iterator(); it.hasNext(); ) {
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
                        for (Iterator<Manager> it = managers.iterator(); it.hasNext(); ) {
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
