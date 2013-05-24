/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.refactoring.elements;

import java.util.EnumSet;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceSupport;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.refactoring.plugins.CsmWhereUsedFilters;
import org.netbeans.modules.cnd.refactoring.support.ElementGripFactory;
import org.netbeans.modules.refactoring.spi.FiltersManager;
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
public class CsmRefactoringElementImpl extends SimpleRefactoringElementImplementation 
        implements FiltersManager.Filterable {
    private static final boolean LAZY = false;
    private final CsmReference elem;
    private final PositionBounds bounds;
    private final FileObject fo;
    private String displayText;
    private final Object enclosing;
    
    private final boolean isDecl;
    private final boolean isInMacros;
    private final boolean isInDeadCode;
    
    public CsmRefactoringElementImpl(PositionBounds bounds, 
            CsmReference elem, FileObject fo, String displayText) {
        this.elem = elem;
        this.bounds = bounds;
        this.fo = fo;
        assert displayText != null || LAZY;
        this.displayText = displayText;
        Object composite = ElementGripFactory.getDefault().putInComposite(fo, elem);
        if (composite==null) {
            composite = fo;
        }  
        this.enclosing = composite;
        this.isDecl = CsmReferenceResolver.getDefault().isKindOf(elem, EnumSet.of(CsmReferenceKind.DECLARATION, CsmReferenceKind.DEFINITION));
        this.isInMacros = CsmReferenceResolver.getDefault().isKindOf(elem, EnumSet.of(CsmReferenceKind.IN_PREPROCESSOR_DIRECTIVE));
        this.isInDeadCode = CsmReferenceResolver.getDefault().isKindOf(elem, EnumSet.of(CsmReferenceKind.IN_DEAD_BLOCK));
    }
        
    @Override
    public String getText() {
        return elem.getText().toString();
    }

    @Override
    public String getDisplayText() {
        if (displayText == null && LAZY) {
            displayText = CsmReferenceSupport.getContextLineHtml(elem, true).toString();
        }
        return displayText;
    }

    @Override
    public void performChange() {
    }

    @Override
    public Lookup getLookup() {
        return Lookups.fixed(elem, enclosing);
    }
    
    @Override
    public FileObject getParentFile() {
        return fo;
    }

    @Override
    public PositionBounds getPosition() {
        return bounds;
    }

    @Override
    public String toString() {
        return "{" + "bounds=" + bounds + ", displayText=" + displayText + ", enclosing=" + enclosing + ", fo=" + fo + '}'; // NOI18N
    }
    
    public static RefactoringElementImplementation create(CsmReference ref,boolean nameInBold) {
        CsmFile csmFile = ref.getContainingFile();
        FileObject fo = CsmUtilities.getFileObject(csmFile);
        PositionBounds bounds = CsmUtilities.createPositionBounds(ref);
        String displayText = LAZY ? null : CsmReferenceSupport.getContextLineHtml(ref, nameInBold).toString();
        return new CsmRefactoringElementImpl(bounds, ref, fo, displayText);
    }

    @Override
    public boolean filter(FiltersManager manager) {
        if (isDecl && !manager.isSelected(CsmWhereUsedFilters.DECLARATIONS.getKey())) {
            return false;
        }
        if (isInMacros && !manager.isSelected(CsmWhereUsedFilters.MACROS.getKey())) {
            return false;
        }
        if (isInDeadCode && !manager.isSelected(CsmWhereUsedFilters.DEAD_CODE.getKey())) {
            return false;
        }
        return true;
    }
}
