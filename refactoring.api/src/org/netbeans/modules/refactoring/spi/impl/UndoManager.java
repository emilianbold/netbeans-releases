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

package org.netbeans.modules.refactoring.spi.impl;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.refactoring.spi.BackupFacility;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.ProgressListener;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.openide.LifecycleManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Becicka
 */
public class UndoManager extends FileChangeAdapter implements DocumentListener, ChangeListener/*, GlobalPathRegistryListener */{
    
    /** stack of undo items */
    private LinkedList<LinkedList<UndoItem>> undoList;
    
    /** stack of redo items */
    private LinkedList<LinkedList<UndoItem>> redoList;

    /** set of all CloneableEditorSupports */
    private final HashSet<CloneableEditorSupport> allCES = new HashSet();
    
    private final HashMap<FileObject, CloneableEditorSupport> fileObjectToCES = new HashMap();
    
    /** map document -> CloneableEditorSupport */
    private final HashMap<Document, CloneableEditorSupport> documentToCES = new HashMap();
    
    /** map listener -> CloneableEditorSupport */ 
    private final HashMap<InvalidationListener, Collection<? extends CloneableEditorSupport>> listenerToCES = new HashMap();
    private boolean listenersRegistered = false;
    
    public static final String PROP_STATE = "state"; //NOI18N
    
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private boolean wasUndo = false;
    private boolean wasRedo = false;
    private boolean transactionStart;
    private boolean dontDeleteUndo = false;
    
    private IdentityHashMap<LinkedList, String> descriptionMap;
    private String description;
    private ProgressListener progress;
    
    private static UndoManager instance;


    public static UndoManager getDefault() {
        if (instance==null) {
            instance = new UndoManager();
        }
        return instance;
    }
    /** Creates a new instance of UndoManager */
    private UndoManager() {
        undoList = new LinkedList();
        redoList = new LinkedList();
        descriptionMap = new IdentityHashMap();
    }
    
    private UndoManager(ProgressListener progress) {
        this();
        this.progress = progress;
    }
    
    public void setUndoDescription(String desc) { 
        description = desc;
    }
    
    public String getUndoDescription() {
        if (undoList.isEmpty()) return null;
        return descriptionMap.get(undoList.getFirst());
    }
    
    public String getRedoDescription() { 
        if (redoList.isEmpty()) return null;
        return descriptionMap.get(redoList.getFirst());
    }
    
    /** called to mark transaction start
     */
    public void transactionStarted() {
        transactionStart = true;
        unregisterListeners();
        //RepositoryUpdater.getDefault().setListenOnChanges(false);
    }
    
    /**
     * called to mark end of transaction
     */
    public void transactionEnded(boolean fail) {
        try {
            description = null;
            dontDeleteUndo = true;
            if (fail && !undoList.isEmpty())
                undoList.removeFirst();
            else {
                // [TODO] (jb) this code disables undos for changes using org.openide.src
                if (isUndoAvailable() && getUndoDescription() == null) {
                    descriptionMap.remove(undoList.removeFirst());
                    dontDeleteUndo = false;
                }
                
            }
            
            invalidate(null);
            dontDeleteUndo = false;
        } finally {
            if (SwingUtilities.isEventDispatchThread()) {
                registerListeners();
            } else {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {

                        public void run() {
                            registerListeners();
                        }
                    });
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (InvocationTargetException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            fireStateChange();
        }
    }
    
    /** undo last transaction */
    public void undo() {
        //System.out.println("************* Starting UNDO");
        if (isUndoAvailable()) {
            boolean fail = true;
            try {
                transactionStarted();
                wasUndo = true;
                LinkedList undo =  (LinkedList) undoList.getFirst();
                fireProgressListenerStart(0, undo.size());
                undoList.removeFirst();
                Iterator undoIterator = undo.iterator();
                UndoItem item;
                redoList.addFirst(new LinkedList());
                descriptionMap.put(redoList.getFirst(), descriptionMap.remove(undo));
                while (undoIterator.hasNext()) {
                    fireProgressListenerStep();
                    item = (UndoItem) undoIterator.next();
                    item.undo();
                    if (item instanceof SessionUndoItem) {
                        addItem(item);
                    }
                }
                fail = false;
            } finally {
                try {
                    wasUndo = false;
                    transactionEnded(fail);
                } finally {
                    fireProgressListenerStop();
                    fireStateChange();
                }
            }
        }
    }
    
    /** redo last undo
     */
    public void redo() {
        //System.out.println("************* Starting REDO");
        if (isRedoAvailable()) {
            boolean fail = true;
            try {
                transactionStarted();
                wasRedo = true;
                LinkedList redo = redoList.getFirst();
                fireProgressListenerStart(1, redo.size());
                redoList.removeFirst();
                Iterator<UndoItem> redoIterator = redo.iterator();
                UndoItem item;
                description = descriptionMap.remove(redo);
                while (redoIterator.hasNext()) {
                    fireProgressListenerStep();
                    item = redoIterator.next();
                    item.redo();
                    if (item instanceof SessionUndoItem) {
                        addItem(item);
                    }
                }
                fail = false;
            } finally {
                try {
                    wasRedo = false;
                    transactionEnded(fail);
                } finally {
                    fireProgressListenerStop();
                    fireStateChange();
                }
            }
        }
    }
    
    /** clean undo/redo stacks */
    public void clear() {
        undoList.clear();
        redoList.clear();
        descriptionMap.clear();
        BackupFacility.getDefault().clear();
        fireStateChange();
    }
    
    public void addItem(RefactoringSession session) {
        addItem(new SessionUndoItem(session));
    }
    
    /** add new item to undo/redo list */
    public void addItem(UndoItem item) {
        if (wasUndo) {
            LinkedList redo = this.redoList.getFirst();
            redo.addFirst(item);
        } else {
            if (transactionStart) {
                undoList.addFirst(new LinkedList());
                descriptionMap.put(undoList.getFirst(), description);
                transactionStart = false;
            }
            LinkedList undo = this.undoList.getFirst();
            undo.addFirst(item);
        }
        if (! (wasUndo || wasRedo)) 
            redoList.clear();
    }
     
    public boolean isUndoAvailable() {
        return !undoList.isEmpty();
    }
    
    public boolean isRedoAvailable() {
        return !redoList.isEmpty();
    }
    
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        pcs.addPropertyChangeListener(pcl);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        pcs.removePropertyChangeListener(pcl);
    }

    private void fireStateChange() {
        pcs.firePropertyChange(PROP_STATE, null, null);
    }
    
    public void watch(Collection<? extends CloneableEditorSupport> ceSupports, InvalidationListener l) {
        synchronized (allCES) {
            registerListeners();
        }
        for (Iterator<? extends CloneableEditorSupport> it = ceSupports.iterator(); it.hasNext();) {
            final CloneableEditorSupport ces = it.next();
            final Document d = ces.getDocument();
            if (d!=null) {
                NbDocument.runAtomic((StyledDocument)d, new Runnable() {
                    public void run() {
                        synchronized(allCES) {
                            if (allCES.add(ces)) {
                                ces.addChangeListener(UndoManager.this);
                                d.addDocumentListener(UndoManager.this);
                                documentToCES.put(d, ces);
                                Object o = d.getProperty(Document.StreamDescriptionProperty);
                                if (o instanceof DataObject) {
                                    fileObjectToCES.put(((DataObject)o).getPrimaryFile(), ces);
                                }
                            }
                        }
                    }
                });
            } else {
                synchronized(allCES) {
                    if (allCES.add(ces)) {
                        ces.addChangeListener(UndoManager.this);
                    }
                }
            }
        }
        synchronized(allCES) {
            if (l != null) {
                listenerToCES.put(l, ceSupports);
            }
        }
    }
    
    public void stopWatching(InvalidationListener l) {
        //synchronized (undoStack) {
            synchronized (allCES) {
                listenerToCES.remove(l);
                clearIfPossible();
            }
        //}
    }
    
    private static java.lang.reflect.Field undoRedo;
    
    static{
        try {
            //obviously hack. See 108616 and 48427
            undoRedo = org.openide.text.CloneableEditorSupport.class.getDeclaredField("undoRedo"); //NOI18N
            undoRedo.setAccessible(true);
        } catch (NoSuchFieldException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void discardAllEdits(InvalidationListener l) {
        for (CloneableEditorSupport s:listenerToCES==null||l==null?allCES:listenerToCES.get(l)) {
            try {
                org.openide.awt.UndoRedo.Manager manager = (org.openide.awt.UndoRedo.Manager) undoRedo.get(s);
                if (manager!=null) {
                    //if manager not initialized - there is nothing to discard
                    //#114485
                    manager.discardAllEdits();
                }
            } catch (SecurityException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalAccessException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
//    TODO: 
//    public void pathsAdded(GlobalPathRegistryEvent event) {
//    }
//
//    public void pathsRemoved(GlobalPathRegistryEvent event) {
//        assert event != null : "event == null"; // NOI18N
//        if (event.getId().equals(ClassPath.SOURCE)) {
//            clear();
//        }
//    }

    private void registerListeners() {
        if (listenersRegistered) return;
        // TODO: 
        // GlobalPathRegistry.getDefault().addGlobalPathRegistryListener(this);
        Util.addFileSystemsListener(this);
        for (CloneableEditorSupport ces:allCES) {
            ces.addChangeListener(this);
        }
        for (Document doc:documentToCES.keySet()) {
            doc.addDocumentListener(this);
        }
        listenersRegistered = true;
    }
    
    private void unregisterListeners() {
        if (!listenersRegistered) return;
        Util.removeFileSystemsListener(this);
        //TODO: 
        //GlobalPathRegistry.getDefault().removeGlobalPathRegistryListener(this);
        for (CloneableEditorSupport ces:allCES) {
            ces.removeChangeListener(this);
        }
        for (Document doc:documentToCES.keySet()) {
            doc.removeDocumentListener(this);
        }
        listenersRegistered = false;
    }
    
    private void invalidate(CloneableEditorSupport ces) {
        synchronized (undoList) {
            if (!(wasRedo || wasUndo) && !dontDeleteUndo) {
                clear();
            } 
            synchronized (allCES) {
                if (ces == null) {
                    // invalidate all
                    for (InvalidationListener lis:listenerToCES.keySet()) {
                        lis.invalidateObject();
                        discardAllEdits(lis);
                    }
                    listenerToCES.clear();
                } else {
                    for (Iterator<Map.Entry<InvalidationListener, Collection<? extends CloneableEditorSupport>>> it = listenerToCES.entrySet().iterator(); it.hasNext();) {
                        Map.Entry<InvalidationListener, Collection<? extends CloneableEditorSupport>> e = it.next();
                        if ((e.getValue()).contains(ces)) {
                            e.getKey().invalidateObject();
                            it.remove();
                        }
                    }
                    /*ces.removeChangeListener(this);
                    allCES.remove(ces);
                    Document d = ces.getDocument();
                    if (d != null) {
                        d.removeDocumentListener(this);
                        documentToCES.remove(d);
                    }
                     */
                }
                clearIfPossible();
            }
        }
    }
    
    private void clearIfPossible() {
        if (listenerToCES.isEmpty() && undoList.isEmpty() && redoList.isEmpty()) {
            unregisterListeners();
            allCES.clear();
            documentToCES.clear();
            fileObjectToCES.clear();
        }
    }        
    
    // FileChangeAdapter ........................................................
    
    public void fileChanged(FileEvent fe) {   
        CloneableEditorSupport ces = fileObjectToCES.get(fe.getFile());
        if (ces!=null) {
            invalidate(ces);
        }
    }
    
    public void fileDeleted(FileEvent fe) {
        fileChanged(fe);
    }

    public void fileRenamed(FileRenameEvent fe) {
        fileChanged(fe);
    }
    
    // DocumentListener .........................................................
    
    public void changedUpdate(DocumentEvent e) {
    }

    public void insertUpdate(DocumentEvent e) {        
        invalidate(documentToCES.get(e.getDocument()));
    }

    public void removeUpdate(DocumentEvent e) {
        invalidate(documentToCES.get(e.getDocument()));
    }
        
    public void stateChanged(ChangeEvent e) {
        synchronized (allCES) {
            CloneableEditorSupport ces = (CloneableEditorSupport) e.getSource();
            Document d = ces.getDocument();
            for (Iterator it = documentToCES.entrySet().iterator(); it.hasNext();) {
                Map.Entry en = (Map.Entry) it.next();
                if (en.getValue() == ces) {
                    ((Document) en.getKey()).removeDocumentListener(this);
                    it.remove();
                    break;
                }
            }
            if (d != null) {
                documentToCES.put(d, ces);
                d.addDocumentListener(this);
            }
        }
    }
    
    public void saveAll() {
        synchronized (allCES) {
            unregisterListeners();
        }
        try {
            LifecycleManager.getDefault().saveAll();
        } finally {
            synchronized (allCES) {
                registerListeners();
            }
        }
    }

    private void fireProgressListenerStart(int type, int count) {
        stepCounter = 0;
        if (progress == null)
            return;
        progress.start(new ProgressEvent(this, ProgressEvent.START, type, count));
    }
    
    private int stepCounter = 0;
    /** Notifies all registered listeners about the event.
     */
    private void fireProgressListenerStep() {
        if (progress == null)
            return;
        progress.step(new ProgressEvent(this, ProgressEvent.STEP, 0, ++stepCounter));
    }

    /** Notifies all registered listeners about the event.
     */
    private void fireProgressListenerStop() {
        if (progress == null)
            return;
        progress.stop(new ProgressEvent(this, ProgressEvent.STOP));
    }
    
    private interface UndoItem {
        void undo();
        void redo();
    }
    
    private final class SessionUndoItem implements UndoItem {
        
         private RefactoringSession change;
        
        public SessionUndoItem (RefactoringSession change) {
            this.change = change;
        }
        
        public void undo() {
            change.undoRefactoring(false);
        }
        
        public void redo() {
            change.doRefactoring(false);
        }
    }
}
