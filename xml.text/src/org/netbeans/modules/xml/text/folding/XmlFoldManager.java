/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.xml.text.folding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.structure.api.DocumentElement;
import org.netbeans.modules.editor.structure.api.DocumentModel;
import org.netbeans.modules.editor.structure.api.DocumentModelException;
import org.netbeans.modules.editor.structure.api.DocumentModelListener;
import org.netbeans.modules.editor.structure.api.DocumentModelStateListener;
import org.netbeans.modules.editor.structure.api.DocumentModelUtils;
import org.netbeans.modules.xml.text.structure.XMLDocumentModelProvider;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldOperation;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * This class is an implementation of @see org.netbeans.spi.editor.fold.FoldManager
 * responsible for creating, deleting and updating code folds.
 *
 * @author  Marek Fukala
 */
public class XmlFoldManager implements FoldManager {

//    private static long startTime;
    
    private FoldOperation operation;
    private DocumentModel model;
    private final DocumentModelStateListener DMLS = new DocumentModelStateListener() {

        public void sourceChanged() {
        }

        public void scanningStarted() {
        }

        public void updateStarted() {
//            startTime = System.currentTimeMillis(); //measure also model update
            XmlFoldManager.this.clearChanges();
        }

        public void updateFinished() {
//            System.out.println("DocumentModel update = " + (System.currentTimeMillis() - startTime) + "ms.");
            XmlFoldManager.this.updateFolds(null);
        }
    };
    private final ArrayList<DocumentElement> addedElements = new ArrayList<DocumentElement>();
    private final ArrayList<DocumentElement> removedElements = new ArrayList<DocumentElement>();
    private ArrayList<Fold> removedFolds = new ArrayList<Fold>();
    private final Hashtable<DocumentElement, Fold> myFolds = new Hashtable<DocumentElement, Fold>();
    private final DocumentModelListener DML = new DocumentModelListener() {

        public void documentElementAdded(DocumentElement de) {
            addedElements.add(de);
        }

        public void documentElementRemoved(DocumentElement de) {
            removedElements.add(de);
        }

        public void documentElementChanged(DocumentElement de) {
        }

        public void documentElementAttributesChanged(DocumentElement de) {
        }
    };

    protected FoldOperation getOperation() {
        return operation;
    }

    public void init(FoldOperation operation) {
        this.operation = operation;
    }

    //fold hiearchy has been released
    public void release() {
        if (model != null) {
            model.removeDocumentModelStateListener(DMLS);
            model.removeDocumentModelListener(DML);
            model = null;
        }
    }

    public void initFolds(FoldHierarchyTransaction transaction) {
        try {
            Document doc = getOperation().getHierarchy().getComponent().getDocument();
            //filtering of the PlainDocument set during the JEditorPane initializatin
            if (!(doc instanceof BaseDocument)) {
                return;
            }
            model = DocumentModel.getDocumentModel((BaseDocument) getDocument());
            if(model == null)
                return;
            model.addDocumentModelStateListener(DMLS);
            model.addDocumentModelListener(DML);
            updateFolds(transaction);
        } catch (DocumentModelException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    //start collecting changes
    private void clearChanges() {
        addedElements.clear();
        removedElements.clear();
    }

    private String getFoldName(DocumentElement de) {
        String foldName = "";

        //create folds of appropriate type
        if (de.getType().equals(XMLDocumentModelProvider.XML_TAG) || de.getType().equals(XMLDocumentModelProvider.XML_EMPTY_TAG)) {
            foldName = "<" + de.getName() + ">";
        } else if (de.getType().equals(XMLDocumentModelProvider.XML_PI)) {
            foldName = NbBundle.getMessage(XmlFoldManager.class, "LBL_PI"); //NOI18N
        } else if (de.getType().equals(XMLDocumentModelProvider.XML_DOCTYPE)) {
            foldName = NbBundle.getMessage(XmlFoldManager.class, "LBL_DOCTYPE"); //NOI18N
        } else if (de.getType().equals(XMLDocumentModelProvider.XML_COMMENT)) {
            foldName = NbBundle.getMessage(XmlFoldManager.class, "LBL_COMMENT"); //NOI18N
        } else if (de.getType().equals(XMLDocumentModelProvider.XML_CDATA)) {
            foldName = NbBundle.getMessage(XmlFoldManager.class, "LBL_CDATA"); //NOI18N
        }
        return foldName;
    }

    private FoldType getFoldTypeForElement(DocumentElement de) {
        //create folds of appropriate type
        if (de.getType().equals(XMLDocumentModelProvider.XML_TAG) || de.getType().equals(XMLDocumentModelProvider.XML_TAG)) {
            return XmlFoldTypes.TAG;
        } else if (de.getType().equals(XMLDocumentModelProvider.XML_PI)) {
            return XmlFoldTypes.PI;
        } else if (de.getType().equals(XMLDocumentModelProvider.XML_DOCTYPE)) {
            return XmlFoldTypes.DOCTYPE;
        } else if (de.getType().equals(XMLDocumentModelProvider.XML_COMMENT)) {
            return XmlFoldTypes.COMMENT;
        } else if (de.getType().equals(XMLDocumentModelProvider.XML_CDATA)) {
            return XmlFoldTypes.CDATA;
        }
        return null;
    }

    /** The heart of this class. This method parses the JSP page and based on
     * syntax parser information creates appropriate folds.
     */
    private synchronized void updateFolds(FoldHierarchyTransaction transaction) {
        
        DocumentElement[] all = DocumentModelUtils.elements(model);
        
//        //debug>>>
//        System.out.println("-----------------------------");
//        System.out.println("ADDED:");
//        for(DocumentElement de : addedElements) {
//            System.out.println(de);
//        }
//        System.out.println("-----------------------------");
//        System.out.println("REMOVED:");
//        for(DocumentElement de : removedElements) {
//            System.out.println(de);
//        }
//        System.out.println("-----------------------------");
//        System.out.println("DISCARDED FOLDS");
//        for(Fold f : removedFolds) {
//            System.out.println(f);
//        }
//        System.out.println("-----------------------------");
//        System.out.println("MY FOLDS MAP");
//        for(DocumentElement de : myFolds.keySet()) {
//            System.out.println(de + " ====> " + myFolds.get(de));
//        }
//        System.out.println("-----------------------------");
//        System.out.println("ELEMENTS");
//        for(int i = 0; i < all.length ; i++) {
//            System.out.println(all[i]);
//        }
//        System.out.println("-----------------------------");
//        //<<<debug
        
        long a = System.currentTimeMillis();
        
        FoldHierarchy fh = getOperation().getHierarchy();
        //lock the document for changes
        getDocument().readLock();
        try {
            //lock the hierarchy
            fh.lock();
            try {
                //open new transaction
                FoldHierarchyTransaction fhTran = transaction == null ? getOperation().openTransaction() : transaction;
                try {
                    
                    //remove outdated folds
                    for (DocumentElement de : removedElements) {
                        Fold f = myFolds.get(de);
                        if (f == null) {
                            continue;
                        }
                        if (!isFoldable(de) || model.getRootElement() == de || isOneLineElement(de)) {
                            continue;
                        }
                        if (removedFolds.contains(f)) {
                            myFolds.remove(de);
                            continue; //already removed by the infrastructure
                        }
                        
//                        System.out.println("removing fold for " + de);
                        getOperation().removeFromHierarchy(f, fhTran);
                        myFolds.remove(de);
                    }

                    //add new folds
                    List<DocumentElement> elementsToAdd = transaction == null ? addedElements : Arrays.asList(all);
                    for (DocumentElement de : elementsToAdd) {
                        if (isFoldable(de) && !isOneLineElement(de) && model.getRootElement() != de) {
                            addFold(de, fhTran);
                        }
                    }

                    //check consistency
                    for (int i = 0; i < all.length; i++) {
                        DocumentElement de = all[i];
                        if (!isFoldable(de) || de == model.getRootElement()) {
                            continue;
                        }
                        Fold f = myFolds.get(de);
                        boolean ole = isOneLineElement(de);
                        if (ole && f != null) {
                            if(!removedFolds.contains(f)) {
                                //remove the fold
//                                System.out.println("consistency - removing fold for " + de);
                                getOperation().removeFromHierarchy(f, fhTran);
                            }
                            myFolds.remove(de);
                        } else if (!ole && f == null) {
                            //add the fold
                            addFold(de, fhTran);
                        }
                    }
                    
                } catch (BadLocationException ble) {
                    //when the document is closing the hierarchy returns different empty document, grrrr
                    Document fhDoc = getOperation().getHierarchy().getComponent().getDocument();
                    if (fhDoc.getLength() > 0) {
                        Exceptions.printStackTrace(ble);
                    }
                } finally {
                    if (transaction == null) {
                        fhTran.commit(); //the given transaction from initFolds() will be commited by the infr.
                    }
                }
            } finally {
                fh.unlock();
            }
        } finally {
            getDocument().readUnlock();
            removedFolds.clear();
        }
        
//        System.out.println("XML Folds Update = " + (System.currentTimeMillis() - a) + "ms.");
        
    }

    private void addFold(DocumentElement de, FoldHierarchyTransaction fhTran) throws BadLocationException {
        int so = de.getStartOffset();
        int eo = de.getEndOffset();
        if (so >= 0 && eo >= 0 && so < eo && eo <= getDocument().getLength()) {
            Fold newFold = getOperation().addToHierarchy(getFoldTypeForElement(de), getFoldName(de), false, so, eo + 1, getFoldName(de).length() + 1, 0, null, fhTran);
            if (newFold != null) {
                myFolds.put(de, newFold);
            }
        }
    }

    private boolean isOneLineElement(DocumentElement de) {
        try {
            return Utilities.getLineOffset(getDocument(), de.getStartOffset()) == Utilities.getLineOffset(getDocument(), de.getEndOffset());
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
    }

    private boolean isFoldable(DocumentElement de) {
        return getFoldTypeForElement(de) != null;
    }

    private BaseDocument getDocument() {
        return (BaseDocument) getOperation().getHierarchy().getComponent().getDocument();
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
        removedFolds.add(epmtyFold);
    }

    public void removeDamagedNotify(Fold damagedFold) {
        removedFolds.add(damagedFold);
    }

    public void expandNotify(Fold expandedFold) {
    }
}