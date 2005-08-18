/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.text.folding;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsChangeEvent;
import org.netbeans.editor.SettingsChangeListener;
import org.netbeans.modules.editor.structure.api.DocumentElement;
import org.netbeans.modules.editor.structure.api.DocumentModel;
import org.netbeans.modules.editor.structure.api.DocumentModelException;
import org.netbeans.modules.editor.structure.api.DocumentModelListener;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldOperation;
import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;


/**
 * This class is an implementation of @see org.netbeans.spi.editor.fold.FoldManager
 * responsible for creating, deleting and updating code folds.
 *
 * @author  Marek Fukala
 */
public class XmlFoldManager implements FoldManager, SettingsChangeListener, DocumentModelListener {
    
    private FoldOperation operation;
    
    //timer performing periodicall folds update
    private Timer timer;
    private TimerTask timerTask;
    
    private int foldsUpdateInterval = 1000;
    private long foldsGenerationTime = -1;
    
    private DocumentModel model = null;
    
    //stores changes in document model between fold updates
    private Vector changes = new Vector();
    
    protected FoldOperation getOperation() {
        return operation;
    }
    
    public void init(FoldOperation operation) {
        this.operation = operation;
        this.timer = new Timer();
        Settings.addSettingsChangeListener(this);
//        foldsUpdateInterval = getSetting(JspSettings.CODE_FOLDING_UPDATE_TIMEOUT);
    }
    
    //fold hiearchy has been released
    public void release() {
        Settings.removeSettingsChangeListener(this);
        
        if(timer != null) {
            timer.cancel();
            timer = null;
        }
        
        if(model != null) {
            model.removeDocumentModelListener(this);
            model = null;
        }
    }
    
    public void initFolds(FoldHierarchyTransaction transaction) {
        //I do not know exactly why, but this method is called twice during the initialization
        //during first call getDocument() doesn't return an instance of BaseDocument
        if(!(getDocument() instanceof BaseDocument)) return ;
        
        //the initFolds is called when the document is disposed - I need to filter this call
        if(getDocument().getLength() > 0) {
            //init the folds lazily - the model creation may take a long time so do that in a background thread
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    //get document structure model
                    try {
                        Document document = getDocument();
                        if(!(document instanceof BaseDocument)) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new ClassCastException("<FoldHierarchyOperation>.getHierarchy().getComponent().getDocument() returned FilterDocument instead of AbstractDocument. This is likely caused by not yet resolved issue #49497")); //NOI18N
                            return ;
                        }
                        
                        model = DocumentModel.getDocumentModel((BaseDocument)document);
                        //add changes listener which listenes to model changes
                        model.addDocumentModelListener(XmlFoldManager.this);
                        
                        //init the folds - it must be done since the folds
                        //are created based on events fired from model and the model
                        initFolds();
                        updateFolds();
                    } catch (DocumentModelException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                }
            }, 0, Thread.currentThread().getPriority() - 1);
        }
    }
    
    private void initFolds() {
        //add all existing elements to the changes list
        //the changes will be subsequently transformed to folds
        addElementsRecursivelly(changes, model.getRootElement());
    }
    
    private void addElementsRecursivelly(Vector changes, DocumentElement de) {
        //add myself
        if(!de.equals(model.getRootElement())) changes.add(new DocumentModelChangeInfo(de, DocumentModelChangeInfo.ELEMENT_ADDED));
        //add my children
        Iterator children = de.getChildren().iterator();
        while(children.hasNext()) {
            DocumentElement child = (DocumentElement)children.next();
            if(!child.isLeaf()) addElementsRecursivelly(changes, child);
        }
    }
    
    public void documentElementAdded(DocumentElement de) {
        //add all containers except root element
        if(!de.equals(model.getRootElement())) changes.add(new DocumentModelChangeInfo(de, DocumentModelChangeInfo.ELEMENT_ADDED));
        restartTimer();
    }
    
    public void documentElementRemoved(DocumentElement de) {
        if(!de.equals(model.getRootElement())) changes.add(new DocumentModelChangeInfo(de, DocumentModelChangeInfo.ELEMENT_REMOVED));
        restartTimer();
    }
    
    public void documentElementChanged(DocumentElement de) {
        ;
    }
    
    public void documentElementAttributesChanged(DocumentElement de) {
        ;
    }
    
    
    private void restartTimer() {
        //test whether the FoldManager.release() was called.
        //if so, then do not try to update folds anymore
        if(timer == null) return ;
        
        if(timerTask != null) timerTask.cancel();
        timerTask = createTimerTask();
        timer.schedule(timerTask, foldsUpdateInterval);
    }
    
    private TimerTask createTimerTask() {
        return new TimerTask() {
            public void run() {
                if(lightDebug) System.out.println("updating folds...");
                updateFolds();
                if(lightDebug) System.out.println("done.");
            }
        };
    }
    
    private void updateFolds() {
        //check for 49497 :-(
        //very rarely the getOperation().getHierarchy().getComponent().getDocument()
        //returns FilterDocument - nobody understands why???
        Document document = getDocument();
        if(!(document instanceof AbstractDocument)) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new ClassCastException("<FoldHierarchyOperation>.getHierarchy().getComponent().getDocument() returned FilterDocument instead of AbstractDocument. This is likely caused by not yet resolved issue #49497")); //NOI18N
            return ;
        }
        
        ((AbstractDocument)document).readLock();
        try {
            FoldHierarchy fh = getOperation().getHierarchy();
            fh.lock();
            try {
                FoldHierarchyTransaction fhTran = getOperation().openTransaction();
                try {
                    Iterator changesItr = changes.iterator();
                    while(changesItr.hasNext()) {
                        DocumentModelChangeInfo chi = (DocumentModelChangeInfo)changesItr.next();
                        DocumentElement de = chi.getDocumentElement();
                        if(chi.getChangeType() == DocumentModelChangeInfo.ELEMENT_ADDED && !de.isLeaf()) {
                            getOperation().addToHierarchy(XmlFoldTypes.TAG, de.getName(), false,
                                    Math.max(0, de.getStartOffset() +1 ) ,
                                    Math.min(document.getLength(), de.getEndOffset()),
                                    0, 0, null, fhTran);
                        } else if (chi.getChangeType() == DocumentModelChangeInfo.ELEMENT_REMOVED) {
                            //find appropriate fold for the document element
                            //XXX this is very uneffective - I need a method like
                            //FoldUtilitites.getFold(int startOffset, int endOffset);
                            Iterator allFolds = FoldUtilities.findRecursive(fh.getRootFold(), XmlFoldTypes.TAG).iterator();
                            while(allFolds.hasNext()) {
                                Fold f = (Fold)allFolds.next();
                                //why the startoffset + 1??? - because we create the folds so they do not contain the starting < :-)
                                if(f.getStartOffset() == (de.getStartOffset() + 1) &&
                                        f.getEndOffset() == de.getEndOffset()) {
                                    //remove the fold
                                    getOperation().removeFromHierarchy(f, fhTran);
                                    break; //there should be only one fold for document element
                                }
                            }
                        }
                    }
                    
                }catch(BadLocationException ble) {
                    ErrorManager.getDefault().notify(ble);
                }finally {
                    fhTran.commit();
                }
            } finally {
                fh.unlock();
            }
        } finally {
            ((AbstractDocument)document).readUnlock();
        }
        changes.clear();
    }
    
    
//    private int getSetting(String settingName){
//        JTextComponent tc = getOperation().getHierarchy().getComponent();
//        return SettingsUtil.getInteger(org.netbeans.editor.Utilities.getKitClass(tc), settingName, JspSettings.defaultCodeFoldingUpdateInterval);
//    }
    
    public void settingsChange(SettingsChangeEvent evt) {
        // Get folding presets
//        if(evt.getSettingName() == JspSettings.CODE_FOLDING_UPDATE_TIMEOUT) {
//            foldsUpdateInterval = getSetting(JspSettings.CODE_FOLDING_UPDATE_TIMEOUT);
//            restartTimer();
//        }
    }
    
    private Document getDocument() {
        return getOperation().getHierarchy().getComponent().getDocument();
    }
    
    /** Returns a time in milliseconds for how long code folds were generated.
     * This time doesn't involve running of any code from fold hirarchy.
     */
    public long getLastFoldsGenerationTime() {
        return foldsGenerationTime;
    }
    
    public void insertUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        //we listen only to the document model
    }
    
    public void removeUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        //we listen only to the document model
    }
    
    public void changedUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        //we listen only to the document model
    }
    
    public void removeEmptyNotify(Fold epmtyFold) {
    }
    public void removeDamagedNotify(Fold damagedFold) {
    }
    public void expandNotify(Fold expandedFold) {
    }
    
    
//    private boolean isOneRowElement(DocumentElement de) {
//        Utilities.get de.getStartOffset()
//    }
    
    private static final class DocumentModelChangeInfo {
        static final int ELEMENT_ADDED = 1;
        static final int ELEMENT_REMOVED = 2;
        
        private DocumentElement de;
        private int type;
        
        public DocumentModelChangeInfo(DocumentElement de, int changeType) {
            this.de = de;
            this.type = changeType;
        }
        public DocumentElement getDocumentElement() {
            return de;
        }
        public int getChangeType() {
            return type;
        }
    }
    
    //enable/disable debugging messages for this class
    private static final boolean debug = false;
    private static final boolean lightDebug = debug || false;
    
}
