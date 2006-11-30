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
import org.netbeans.modules.refactoring.api.impl.ProgressSupport;
import org.netbeans.modules.refactoring.api.impl.SPIAccessor;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.LifecycleManager;


/** Singleton class used to invoke refactorings.
 *
 * @author Martin Matula, Daniel Prusa, Jan Becicka
 */
public final class RefactoringSession {
    private final LinkedList<RefactoringElementImplementation> internalList;
    private final RefactoringElementsBag bag;
    private final Collection<RefactoringElement> refactoringElements;
    private final String description;
    private ProgressSupport progressSupport;
    private Collection<Runnable> commits;
    private Collection<Runnable> fileChanges;
    
    private RefactoringSession(String description) {
        internalList = new LinkedList();
        bag = SPIAccessor.DEFAULT.createBag(this, internalList);
        this.description = description;
        this.refactoringElements = new ElementsCollection();
        this.commits = new ArrayList();
        this.fileChanges =  new ArrayList();
    }
    
    /** 
     * Creates a new refactoring session.
     */
    public static RefactoringSession create(String description) {
        return new RefactoringSession(description);
    }

    /**
     * commits are called after all changes are performed
     */
    public void registerCommit(Runnable commit) {
        commits.add(commit);
    }
    
    
    /**
     * fileChanges are performed after all element changes
     */
    public void registerFileChange(Runnable changes) {
        fileChanges.add(changes);
    }
    
    /**
     * process all elements from elements bags,
     * do all fileChanges
     * and call all commits
     */
    public Problem doRefactoring(boolean saveAfterDone) {
        Iterator it = internalList.iterator();
        fireProgressListenerStart(0, internalList.size()+1);
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
                try {
                    for (Runnable commit:commits) {
                        commit.run();
                    }
                } finally {
                    commits.clear();
                }
            }
            if (saveAfterDone) {
                LifecycleManager.getDefault().saveAll();
            }
            try {
                for (Runnable fileChange:fileChanges) {
                    fileChange.run();
                }
            } finally {
                fileChanges.clear();
            }
            fireProgressListenerStep();
        } finally {
            fireProgressListenerStop();
        }
        return null;
    }
    
    /**
     * get elements from session
     */
    public Collection<RefactoringElement> getRefactoringElements() {
        return refactoringElements;
    }
    
    public synchronized void addProgressListener(ProgressListener listener) {
        if (progressSupport == null ) {
            progressSupport = new ProgressSupport();
        }
        progressSupport.addProgressListener(listener);
    }

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

                public void remove() {
                    throw new UnsupportedOperationException();
                }
                
                public RefactoringElement next() {
                    return new RefactoringElement(inner.next());
                }
                
                public boolean hasNext() {
                    return inner.hasNext();
                }
            };
        }

        public int size() {
            return internalList.size();
        }
    }
}
