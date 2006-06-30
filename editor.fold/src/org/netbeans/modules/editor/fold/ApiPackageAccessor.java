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
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldHierarchyEvent;
import org.netbeans.api.editor.fold.FoldStateChange;
import org.netbeans.api.editor.fold.FoldType;

/**
 * Accessor for the package-private functionality in org.netbeans.api.editor.fold.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class ApiPackageAccessor {
    
    private static ApiPackageAccessor INSTANCE;
    
    public static ApiPackageAccessor get() {
        return INSTANCE;
    }

    /**
     * Register the accessor. The method can only be called once
     * - othewise it throws IllegalStateException.
     * 
     * @param accessor instance.
     */
    public static void register(ApiPackageAccessor accessor) {
        if (INSTANCE != null) {
            throw new IllegalStateException("Already registered"); // NOI18N
        }
        INSTANCE = accessor;
    }
    
    public abstract FoldHierarchy createFoldHierarchy(FoldHierarchyExecution execution);

    public abstract Fold createFold(FoldOperationImpl operation,
    FoldType type, String description, boolean collapsed,
    Document doc, int startOffset, int endOffset,
    int startGuardedLength, int endGuardedLength,
    Object extraInfo)
    throws BadLocationException;
    
    public abstract FoldHierarchyEvent createFoldHierarchyEvent(FoldHierarchy source,
    Fold[] removedFolds, Fold[] addedFolds, FoldStateChange[] foldStateChanges,
    int affectedStartOffset, int affectedEndOffset);

    public abstract FoldStateChange createFoldStateChange(Fold fold);

    public abstract void foldSetCollapsed(Fold fold, boolean collapsed);
    
    public abstract void foldSetParent(Fold fold, Fold parent);

    public abstract void foldExtractToChildren(Fold fold, int index, int length, Fold targetFold);

    public abstract Fold foldReplaceByChildren(Fold fold, int index);

    public abstract void foldSetDescription(Fold fold, String description);

    public abstract void foldSetStartOffset(Fold fold, Document doc, int startOffset)
    throws BadLocationException;
    
    public abstract void foldSetEndOffset(Fold fold, Document doc, int endOffset)
    throws BadLocationException;
    
    public abstract boolean foldIsStartDamaged(Fold fold);

    public abstract boolean foldIsEndDamaged(Fold fold);

    public abstract boolean foldIsExpandNecessary(Fold fold);

    public abstract void foldInsertUpdate(Fold fold, DocumentEvent evt);

    public abstract void foldRemoveUpdate(Fold fold, DocumentEvent evt);
    
    public abstract FoldOperationImpl foldGetOperation(Fold fold);
    
    public abstract int foldGetRawIndex(Fold fold);

    public abstract void foldSetRawIndex(Fold fold, int rawIndex);

    public abstract void foldUpdateRawIndex(Fold fold, int rawIndexDelta);

    public abstract Object foldGetExtraInfo(Fold fold);

    public abstract void foldStateChangeCollapsedChanged(FoldStateChange fsc);
    
    public abstract void foldStateChangeDescriptionChanged(FoldStateChange fsc);

    public abstract void foldStateChangeStartOffsetChanged(FoldStateChange fsc,
    int originalStartOffset);
    
    public abstract void foldStateChangeEndOffsetChanged(FoldStateChange fsc,
    int originalEndOffset);

}
