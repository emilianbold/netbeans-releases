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
package org.netbeans.modules.web.refactoring.rename;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.web.refactoring.RefactoringUtil;
import org.openide.filesystems.FileObject;

/**
 * Handles move refactoring in tld files.
 *
 * @author Erno Mononen
 */
public class TldMove extends BaseTldRename{
    
    private final MoveRefactoring move;
    private final String clazz;
    
    public TldMove(MoveRefactoring move, FileObject source, String clazz) {
        super(source);
        this.clazz = clazz;
        this.move = move;
    }
    
    
    protected List<RenameItem> getAffectedClasses() {
        String newName = RefactoringUtil.constructNewName(move);
        return Collections.<RenameItem>singletonList(new RenameItem(newName, clazz));
    }
    
    protected AbstractRefactoring getRefactoring() {
        return move;
    }
    
    
    
}
