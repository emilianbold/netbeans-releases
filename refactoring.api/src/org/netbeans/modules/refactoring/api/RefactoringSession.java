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
package org.netbeans.modules.refactoring.api;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import org.netbeans.modules.refactoring.api.impl.ProgressSupport;
import org.netbeans.modules.refactoring.api.impl.SPIAccessor;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.Transaction;
import org.netbeans.modules.refactoring.spi.impl.UndoManager;
import org.openide.LifecycleManager;


/** Class used to invoke refactorings.
 *
 * @author Martin Matula, Daniel Prusa, Jan Becicka
 */
public final class RefactoringSession {
    private final LinkedList<RefactoringElementImplementation> internalList;
    private final RefactoringElementsBag bag;
    private final Collection<RefactoringElement> refactoringElements;
    private final String description;
    private ProgressSupport progressSupport;
    private UndoManager undoManager = UndoManager.getDefault();
    boolean realcommit = true;
    
    private RefactoringSession(String description) {
        internalList = new LinkedList();
        bag = SPIAccessor.DEFAULT.createBag(this, internalList);
        this.description = description;
        this.refactoringElements = new ElementsCollection();
    }
    
    /** 
     * Creates a new refactoring session.
     * @param description textual description of this session
     * @return instance of RefactoringSession
     */
    public static RefactoringSession create(String description) {
        return new RefactoringSession(description);
    }


    /**
     * process all elements from elements bags,
     * do all fileChanges
     * and call all commits
     * @param saveAfterDone save all if true
     * @return instance of Problem or null, if everything is OK
     */
    public Problem doRefactoring(boolean saveAfterDone) {
        Iterator it = internalList.iterator();
        fireProgressListenerStart(0, internalList.size()+1);
        if (realcommit) {
            undoManager.transactionStarted();
            undoManager.setUndoDescription(description);
        }
        try {
            try {
                while (it.hasNext()) {
                    fireProgressListenerStep();
                    RefactoringElementImplementation element = (RefactoringElementImplementation) it.next();
                    if (element.isEnabled() && !((element.getStatus() == RefactoringElement.GUARDED) || (element.getStatus() == RefactoringElement.READ_ONLY))) {
                        element.performChange();
                    }
                }
            } finally {
                for (Transaction commit:SPIAccessor.DEFAULT.getCommits(bag)) {
                    commit.commit();
                }
            }
            if (saveAfterDone) {
                LifecycleManager.getDefault().saveAll();
            }
            for (RefactoringElementImplementation fileChange:SPIAccessor.DEFAULT.getFileChanges(bag)) {
                if (fileChange.isEnabled()) {
                    fileChange.performChange();
                }
            }
            fireProgressListenerStep();
        } finally {
            fireProgressListenerStop();
            if (realcommit) {
                undoManager.addItem(this);
                undoManager.transactionEnded(false);
                realcommit=false;
            }
        }
        return null;
    }
    
    /**
     * do undo of previous doRefactoring()
     * @param saveAfterDone save all if true
     * @return instance of Problem or null, if everything is OK
     */
    public Problem undoRefactoring(boolean saveAfterDone) {
        try {
            ListIterator it = internalList.listIterator(internalList.size());
            fireProgressListenerStart(0, internalList.size()+1);
            ArrayList<RefactoringElementImplementation> fileChanges = SPIAccessor.DEFAULT.getFileChanges(bag);
            ArrayList<Transaction> commits = SPIAccessor.DEFAULT.getCommits(bag);
            for (ListIterator<RefactoringElementImplementation> fileChangeIterator = fileChanges.listIterator(fileChanges.size()); fileChangeIterator.hasPrevious();) {
                RefactoringElementImplementation f = fileChangeIterator.previous();
                if (f.isEnabled()) {
                    f.undoChange();
                }
            }
            for (ListIterator<Transaction> commitIterator = commits.listIterator(commits.size()); commitIterator.hasPrevious();) {
                commitIterator.previous().rollback();
            }
            
            while (it.hasPrevious()) {
                fireProgressListenerStep();
                RefactoringElementImplementation element = (RefactoringElementImplementation) it.previous();
                if (element.isEnabled() && !((element.getStatus() == RefactoringElement.GUARDED) || (element.getStatus() == RefactoringElement.READ_ONLY))) {
                    element.undoChange();
                }
            }
            if (saveAfterDone) {
                LifecycleManager.getDefault().saveAll();
            }
            fireProgressListenerStep();
        } finally {
            fireProgressListenerStop();
        }
        return null;
    }
    
    /**
     * get elements from session
     * @return collection of RefactoringElements
     */
    public Collection<RefactoringElement> getRefactoringElements() {
        return refactoringElements;
    }
    
    /**
     *  Adds progress listener to this RefactoringSession
     * @param listener to add
     */
    public synchronized void addProgressListener(ProgressListener listener) {
        if (progressSupport == null ) {
            progressSupport = new ProgressSupport();
        }
        progressSupport.addProgressListener(listener);
    }

    /**
     * Remove progress listener from this RefactoringSession
     * @param listener to remove
     */
    public synchronized void removeProgressListener(ProgressListener listener) {
        if (progressSupport != null ) {
            progressSupport.removeProgressListener(listener); 
        }
    }

    RefactoringElementsBag getElementsBag() {
        return bag;
    }

    private void fireProgressListenerStart(int type, int count) {
        if (progressSupport != null) {
            progressSupport.fireProgressListenerStart(this, type, count);
        }
    }

    private void fireProgressListenerStep() {
        if (progressSupport != null) {
            progressSupport.fireProgressListenerStep(this);
        }
    }

    private void fireProgressListenerStop() {
        if (progressSupport != null) {
            progressSupport.fireProgressListenerStop(this);
        }
    }
    
    private class ElementsCollection extends AbstractCollection<RefactoringElement> {
        public Iterator<RefactoringElement> iterator() {
            return new Iterator() {
                private final Iterator<RefactoringElementImplementation> inner = internalList.iterator();
                private final Iterator<RefactoringElementImplementation> inner2 = SPIAccessor.DEFAULT.getFileChanges(bag).iterator();

                public void remove() {
                    throw new UnsupportedOperationException();
                }
                
                public RefactoringElement next() {
                    if (inner.hasNext()) {
                        return new RefactoringElement(inner.next());
                    } else {
                        return new RefactoringElement(inner2.next());
                    }
                }
                
                public boolean hasNext() {
                    return (inner.hasNext() || inner2.hasNext());
                }
            };
        }

        public int size() {
            return internalList.size() + SPIAccessor.DEFAULT.getFileChanges(bag).size();
        }
    }
}
