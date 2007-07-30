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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsChangeEvent;
import org.netbeans.editor.SettingsChangeListener;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.structure.api.DocumentElement;
import org.netbeans.modules.editor.structure.api.DocumentModel;
import org.netbeans.modules.editor.structure.api.DocumentModelException;
import org.netbeans.modules.editor.structure.api.DocumentModelListener;
import org.netbeans.modules.xml.text.structure.XMLDocumentModelProvider;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldOperation;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

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
    
    private int foldsUpdateInterval = 500;
    private long foldsGenerationTime = -1;
    
    private DocumentModel model = null;
    
    //stores changes in document model between fold updates
    private Vector changes = new Vector();
    
    protected FoldOperation getOperation() {
        return operation;
    }
    
    public void init(FoldOperation operation) {
        this.operation = operation;
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
        Document doc = getDocument();
        //I do not know exactly why, but this method is called twice during the initialization
        //during first call getDocument() doesn't return an instance of BaseDocument
        if (!(doc instanceof BaseDocument)) return;
        
        //the initFolds is called when the document is disposed - I need to filter this call
        if (doc.getLength() > 0) {
            //start folds updater timer
            //put off the initial fold search due to the processor overhead during page opening
            timer = new Timer("XmlFoldManager[" + doc.getProperty(Document.StreamDescriptionProperty) + "]");
            restartTimer();
        }
    }
    
    
    //init the folds - it must be done since the folds
    //are created based on events fired from model and the model
    private void initModelAndFolds() {
        try {
            model = DocumentModel.getDocumentModel((BaseDocument)getDocument());
            //add changes listener which listenes to model changes
            model.addDocumentModelListener(this);
            //add all existing elements to the changes list
            //the changes will be subsequently transformed to folds
            addElementsRecursivelly(changes, model.getRootElement());
        } catch (DocumentModelException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    private void addElementsRecursivelly(Vector changes, DocumentElement de) {
        //add myself
        try {
            if(!de.equals(model.getRootElement()) && !isOneLineElement(de)) changes.add(new DocumentModelChangeInfo(de, DocumentModelChangeInfo.ELEMENT_ADDED));
        }catch(BadLocationException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        //add my children
        Iterator children = de.getChildren().iterator();
        while(children.hasNext()) {
            DocumentElement child = (DocumentElement)children.next();
            addElementsRecursivelly(changes, child);
        }
    }
    
    public void documentElementAdded(DocumentElement de) {
        if(debug) System.out.println("[xmlfolding] ADDED " + de);
        checkElement2FoldConsistency(de, false);
    }
    
    public void documentElementRemoved(DocumentElement de) {
        if(debug) System.out.println("[xmlfolding] REMOVED " + de);
        if(!de.equals(model.getRootElement()) && !de.getType().equals(XMLDocumentModelProvider.XML_CONTENT)) changes.add(new DocumentModelChangeInfo(de, DocumentModelChangeInfo.ELEMENT_REMOVED));
        checkElement2FoldConsistency(de, true);
        restartTimer();
    }
    
    public void documentElementChanged(DocumentElement de) {
        if(debug) System.out.println("[xmlfolding] CONTENT UPDATE " + de);
        checkElement2FoldConsistency(de, true);
    }
    
    public void checkElement2FoldConsistency(DocumentElement de, boolean removed) {
        //get leaf element for the changed position (got from the changed element)
        //this is has to be done since I need to recursivelly check all element's
        //ancestor, which cannot be done if the element was removed (in such situation
        //I cannot get parent).
        DocumentElement tested = removed ? model.getLeafElementForOffset(de.getStartOffset()) : de;
        boolean restartTimer = false;
        while(tested != null) {
            //do not check root element
            if(tested.equals(model.getRootElement())) break ;
            
            if(!tested.getType().equals(XMLDocumentModelProvider.XML_CONTENT)) {
                
                //check consistency of this
                try {
                    Fold existingFold = getFold(getOperation().getHierarchy(), tested);
                    boolean oneLineElement = isOneLineElement(tested);
                    if(existingFold != null && oneLineElement) {
                        //there is already a fold for the element,
                        //but the element was changed so now its end and start offsets
                        //are on the same line => remove the fold
                        changes.add(new DocumentModelChangeInfo(tested, DocumentModelChangeInfo.ELEMENT_REMOVED));
                        restartTimer = true;
                    }
                    if(existingFold == null && !oneLineElement) {
                        //there wasn't any fold for the element because its start == end,
                        //now the situation changed => add a fold
                        changes.add(new DocumentModelChangeInfo(tested, DocumentModelChangeInfo.ELEMENT_ADDED));
                        restartTimer = true;
                    }
                    if(existingFold != null && !oneLineElement) {
                        //there is already a fold, test if the fold corresponds to the element
                        //in the case of xml tag check also the element&fold name
                        if(getFoldTypeForElement(tested) != existingFold.getType()
                        || !existingFold.getDescription().equals("<"+tested.getName()+">")) {
                            //recreate the fold -- looks silly but works - there is no check for type and name in the updateFolds() method
                            changes.add(new DocumentModelChangeInfo(tested, DocumentModelChangeInfo.ELEMENT_REMOVED));
                            changes.add(new DocumentModelChangeInfo(tested, DocumentModelChangeInfo.ELEMENT_ADDED));
                        }
                    }
                    
                }catch(BadLocationException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
            
            tested = tested.getParentElement(); //switch to parent
        }
        
        if(restartTimer) restartTimer(); //restart if necessary
    }
    
    private FoldType getFoldTypeForElement(DocumentElement de) {
        //create folds of appropriate type
        if(de.getType().equals(XMLDocumentModelProvider.XML_TAG)
        || de.getType().equals(XMLDocumentModelProvider.XML_TAG)) {
            return  XmlFoldTypes.TAG;
        } else if(de.getType().equals(XMLDocumentModelProvider.XML_PI)) {
            return XmlFoldTypes.PI;
        } else if(de.getType().equals(XMLDocumentModelProvider.XML_DOCTYPE)) {
            return XmlFoldTypes.DOCTYPE;
        } else if(de.getType().equals(XMLDocumentModelProvider.XML_COMMENT)) {
            return XmlFoldTypes.COMMENT;
        } else if(de.getType().equals(XMLDocumentModelProvider.XML_CDATA)) {
            return XmlFoldTypes.CDATA;
        }
        return null;
    }
    
    public void documentElementAttributesChanged(DocumentElement de) {
        //do not handle
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
                try {
                    if(model == null) initModelAndFolds();
                    updateFolds();
                }catch(Exception e) {
                    //catch all exceptions to prevent the timer to be cancelled
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
        };
    }
    
    /** Applies changes in the document model to the fold hierarchy
     */
    
    private void updateFolds() {
        Document doc = getDocument();
        if(!(doc instanceof AbstractDocument)) return ;
        
        ((AbstractDocument)doc).readLock();
        try {
            FoldHierarchy fh = getOperation().getHierarchy();
            fh.lock();
            try {
                FoldHierarchyTransaction fhTran = getOperation().openTransaction();
                try {
                    Iterator changesItr = ((Vector)changes.clone()).iterator(); //clone the vector to prevent concurrent modifications
                    while(changesItr.hasNext()) {
                        DocumentModelChangeInfo chi = (DocumentModelChangeInfo)changesItr.next();
                        if(debug) System.out.println("[xmlfolding] processing change " + chi);
                        DocumentElement de = chi.getDocumentElement();
                        if(chi.getChangeType() == DocumentModelChangeInfo.ELEMENT_ADDED
                                && de.getStartOffset() < de.getEndOffset()
                                && !de.getType().equals(XMLDocumentModelProvider.XML_CONTENT)) {
                            String foldName = "";
                            FoldType type = XmlFoldTypes.TEXT; //fold of this type should not be ever used
                            
                            //create folds of appropriate type
                            if(de.getType().equals(XMLDocumentModelProvider.XML_TAG)
                            || de.getType().equals(XMLDocumentModelProvider.XML_EMPTY_TAG)) {
                                foldName = "<"+de.getName()+">";
                                type = XmlFoldTypes.TAG;
                            } else if(de.getType().equals(XMLDocumentModelProvider.XML_PI)) {
                                foldName = NbBundle.getMessage(XmlFoldManager.class, "LBL_PI"); //NOI18N
                                type = XmlFoldTypes.PI;
                            } else if(de.getType().equals(XMLDocumentModelProvider.XML_DOCTYPE)) {
                                foldName = NbBundle.getMessage(XmlFoldManager.class, "LBL_DOCTYPE"); //NOI18N
                                type = XmlFoldTypes.DOCTYPE;
                            } else if(de.getType().equals(XMLDocumentModelProvider.XML_COMMENT)) {
                                foldName = NbBundle.getMessage(XmlFoldManager.class, "LBL_COMMENT"); //NOI18N
                                type = XmlFoldTypes.COMMENT;
                            } else if(de.getType().equals(XMLDocumentModelProvider.XML_CDATA)) {
                                foldName = NbBundle.getMessage(XmlFoldManager.class, "LBL_CDATA"); //NOI18N
                                type = XmlFoldTypes.CDATA;
                            }
                            if(getFold(fh, de) == null) {
                                //add the fold only if really doesn't exist yet
                                if(debug) System.out.println("[XML folding] adding fold for " + de);
                                getOperation().addToHierarchy(type, foldName, false,
                                        Math.max(0, de.getStartOffset()) ,
                                        Math.min(getDocument().getLength(), de.getEndOffset() + 1),
                                        0, 0, null, fhTran);
                            }
                        } else if (chi.getChangeType() == DocumentModelChangeInfo.ELEMENT_REMOVED) {
                            if(debug) System.out.println("[XML folding] about to remove fold for " + chi.getDocumentElement());
                            //find appropriate fold for the document element
                            Fold existingFold = getFold(fh, de);
                            if(existingFold != null) {
                                if(debug) System.out.println("[XML folding] removing fold " + chi.getDocumentElement());
                                getOperation().removeFromHierarchy(existingFold, fhTran);
                            }
                        }
                    }
                    
                }catch(BadLocationException ble) {
                    ErrorManager.getDefault().notify(ble);
                }finally {
                    fhTran.commit();
                }
                
                if(debug) {
                    System.out.println("*************\n\n folds:\n\n");
                    dumpFolds(fh.getRootFold(), "");
                }
                
            } finally {
                fh.unlock();
            }
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        changes.clear();
        
        
    }
    
    private void dumpFolds(Fold f, String indent) {
        System.out.println(indent + f);
        for(int i = 0; i < f.getFoldCount(); i++) {
            dumpFolds(f.getFold(i), indent+"  ");
        }
    }
//    
//    private Fold getFold(FoldHierarchy fh, DocumentElement de) {
//        int startOffset = de.getStartOffset();
//        int endOffset = de.getEndOffset();
//        Iterator allFolds = FoldUtilities.findRecursive(fh.getRootFold()).iterator();
//        while(allFolds.hasNext()) {
//            Fold f = (Fold)allFolds.next();
//            if(debug) System.out.println("testing fold " + f);
//            if((f.getStartOffset()) == startOffset &&
//                    ((f.getEndOffset()-1) == endOffset || f.getEndOffset() == endOffset)) {
//                return f;
//            }
//        }
//        return null;
//    }
    
     private Fold getFold(FoldHierarchy fh, DocumentElement de) {
        int startOffset = de.getStartOffset();
        int endOffset = de.getEndOffset();
        
        Fold f = FoldUtilities.findNearestFold(fh, de.getStartOffset());
        //no fold found or doesn't start at the exact position
        if(f == null || f.getStartOffset() != de.getStartOffset()) return null;
        
        if(f.getEndOffset() == de.getEndOffset() || (f.getEndOffset()-1) == de.getEndOffset()) return f;
        
        //there may be a child inside the found fold which may match the boundaries of de
        //search them recursivelly
        return getFold(fh, f, de);
    }
    
    private Fold getFold(FoldHierarchy fh, Fold f, DocumentElement de) {
        for (int i = 0; i < f.getFoldCount(); i++) {
            Fold child = f.getFold(i);
            if(child.getStartOffset() == de.getStartOffset()) {
                if(child.getEndOffset() == de.getEndOffset()
                        || ((child.getEndOffset()-1) == de.getEndOffset()))
                    return f;
                else 
                    return getFold(fh, child, de);
            } else
                return null; //I suppose that the children are sorted so there cannot be next children with the same startoffset
        }
        return null;
    }
    
    private boolean isOneLineElement(DocumentElement de) throws BadLocationException {
        BaseDocument bdoc = (BaseDocument)de.getDocument();
        return Utilities.getLineOffset(bdoc, de.getStartOffset()) == Utilities.getLineOffset(bdoc, de.getEndOffset());
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
        public String toString() {
            return "" + (type == ELEMENT_ADDED ? "[ADD]" : "[REMOVE]") + " " + de;
        }
    }
    
    //enable/disable debugging messages for this class
    private static final boolean debug = false;
    private static final boolean lightDebug = debug || false;
    
}
