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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.refactoring.elements;

import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.modelimpl.trace.TraceXRef;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Vladimir Voskresensky
 */
public class CsmRefactoringElementImpl extends 
                SimpleRefactoringElementImplementation {

    private final CsmReference elem;
    private final CsmObject referencedObjectDecl;
    private final CsmObject referencedObjectDef;
    private final PositionBounds bounds;
    private final FileObject fo;

    public CsmRefactoringElementImpl(PositionBounds bounds, 
            CsmReference elem, CsmObject referencedObjectDecl, 
            CsmObject referencedObjectDef, FileObject fo) {
        this.elem = elem;
        this.bounds = bounds;
        this.fo = fo;
        this.referencedObjectDecl = referencedObjectDecl;
        this.referencedObjectDef = referencedObjectDef;
    }
        
    public String getText() {
        return TraceXRef.toString(elem, referencedObjectDecl, referencedObjectDef);
    }

    public String getDisplayText() {
        return getText();
    }

    public void performChange() {
    }

    public Lookup getLookup() {
        return Lookups.singleton(elem);
    }

    public FileObject getParentFile() {
        return fo;
    }

    public PositionBounds getPosition() {
        return bounds;
    }

    @Override
    public void openInEditor() {
        CsmUtilities.openSource((CsmOffsetable)elem);
    }
    
    public static RefactoringElementImplementation create(CsmReference ref,
            CsmObject referencedObjectDecl, CsmObject referencedObjectDef) {
        CsmFile csmFile = ref.getContainingFile();        
        FileObject fo = CsmUtilities.getFileObject(csmFile);
        PositionBounds bounds = CsmUtilities.createPositionBounds(ref);
        return new CsmRefactoringElementImpl(bounds, ref, referencedObjectDecl, referencedObjectDef, fo);
    }    
}
