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

package org.netbeans.modules.refactoring.spi;

import java.util.*;
import org.netbeans.modules.refactoring.api.impl.APIAccessor;
import org.netbeans.modules.refactoring.api.impl.SPIAccessor;
import org.netbeans.modules.refactoring.api.*;
import org.openide.filesystems.FileObject;

/**
 * Container holding RefactoringElements
 * @author Jan Becicka
 */
public final class RefactoringElementsBag {
    ArrayList<Transaction> commits;
    ArrayList<Transaction> fileChanges;

    static {
        SPIAccessor.DEFAULT = new AccessorImpl();
    }
    
    private final List<RefactoringElementImplementation> delegate;
    private final RefactoringSession session;
    private Collection<FileObject> readOnlyFiles = new HashSet();
    
    /**
     * Creates an instance of RefactoringElementsBag
     */
    RefactoringElementsBag(RefactoringSession session, List<RefactoringElementImplementation> delegate) {
        this.session = session;
        this.delegate = delegate;
        this.commits = new ArrayList();
        this.fileChanges =  new ArrayList();
    }
    
    /**
     * Adds RefactoringElement to this bag.
     * If RefactoringElement is in read-only file - status of this element is 
     * changes to RefactoringElement.READ_ONLY
     * If RefactoringElement is in guarded block, all registered GuardedBlockHandler
     * are asked, if they can replace given RefactoringElement by it's own 
     * RefactoringElements. If there is no suitable replacement found, 
     * given element is added and it's status is set to RefactringElement.GUARDED
     * 
     * @param refactoring refactoring, which adds this RefactoringElement
     * @param el element to add
     * @return instance of Problem or null
     */
    public Problem add(AbstractRefactoring refactoring, RefactoringElementImplementation el) {
        Problem p = null;
//[retouche]        if (CheckUtils.isRefactoringElementReadOnly(el)) {
        if (false) {
//[retouche]            Resource resource = el.getJavaElement().getResource();
//[retouche]            FileObject file;
//[retouche]            if (resource == null) {
//[retouche]                file = el.getParentFile();
//[retouche]            } else {
//[retouche]                file = JavaModel.getFileObject(resource);
//[retouche]            }
//[retouche]            readOnlyFiles.add(file);    
//[retouche]            el.setEnabled(false);
//[retouche]            el.setStatus(el.READ_ONLY);
//[retouche]            delegate.add(el);
//[retouche]        } else if (CheckUtils.isRefactoringElementGuarded(el)) {
        } else if (false) {
            Iterator pIt=APIAccessor.DEFAULT.getGBHandlers(refactoring).iterator();
            ArrayList proposedChanges = new ArrayList();
            while(pIt.hasNext()) {
                GuardedBlockHandler ref = (GuardedBlockHandler) pIt.next();
                
                p = APIAccessor.DEFAULT.chainProblems(ref.handleChange(el, proposedChanges),  p);
                
                if (p != null && p.isFatal())
                    return p;
                
                if (!proposedChanges.isEmpty()) {
                    delegate.addAll(proposedChanges);
                    return p;
                } 
            }
            el.setEnabled(false);
            el.setStatus(el.GUARDED);
            delegate.add(el);
        } else {
            delegate.add(el);
        }
        return p;
    }
    
    /**
     * Adds all RefactringElements from given Collection using #add method
     * @param refactoring refactoring, which adds this RefactoringElement
     * @param c Collection of RefactoringElements
     * @return instance of Problem or null
     */
    public Problem addAll(AbstractRefactoring refactoring, Collection c) {
	Problem p = null;
        Iterator e = c.iterator();
	while (e.hasNext()) {
	    p = APIAccessor.DEFAULT.chainProblems(p, add(refactoring, (RefactoringElementImplementation) e.next()));
            if (p!=null && p.isFatal())
                return p;
	}
        return p;
    }
    
    
    public RefactoringSession getSession() {
        return session;
    }
    
    Collection<FileObject> getReadOnlyFiles() {
        return readOnlyFiles;
    }
    
    /**
     * commits are called after all changes are performed
     */
    public void registerTransaction(Transaction commit) {
        if (APIAccessor.DEFAULT.isCommit(session))
            commits.add(commit);
    }
    
    
    /**
     * fileChanges are performed after all element changes
     */
    public void registerFileChange(Transaction changes) {
        if (APIAccessor.DEFAULT.isCommit(session))
            fileChanges.add(changes);
    }    
}
