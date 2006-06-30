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

package org.netbeans.modules.editor.fold;

import javax.swing.event.DocumentEvent;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldOperation;

/**
 * Abstract implementation of the fold manager.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class AbstractFoldManager implements FoldManager {

    public static final FoldType REGULAR_FOLD_TYPE = new FoldType("regular"); // NOI18N
    
    private FoldOperation operation;
    
    protected FoldOperation getOperation() {
        return operation;
    }
    
    public void init(FoldOperation operation) {
        this.operation = operation;
    }
    
    public void initFolds(FoldHierarchyTransaction transaction) {
    }
    
    public void insertUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
    }
    
    public void removeUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
    }
    
    public void changedUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
    }
    
    public void removeEmptyNotify(Fold epmtyFold) {
    }
    
    public void removeDamagedNotify(Fold damagedFold) {
    }
    
    public void expandNotify(Fold expandedFold) {
    }

    public void release() {
    }

}
