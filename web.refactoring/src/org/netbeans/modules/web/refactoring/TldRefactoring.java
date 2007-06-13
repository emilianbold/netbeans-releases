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
package org.netbeans.modules.web.refactoring;

import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.web.taglib.model.Taglib;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;

/**
 * A base class for TLD refactorings.
 *
 * @author Erno Mononen
 */
public abstract class TldRefactoring implements WebRefactoring{
    
    public Problem preCheck() {
        return null;
    }
    
    protected abstract static class TldRefactoringElement extends SimpleRefactoringElementImplementation{
        
        protected final Taglib taglib;
        protected final FileObject tldFile;
        protected final String clazz;
        
        public TldRefactoringElement(String clazz, Taglib taglib, FileObject tldFile) {
            this.clazz = clazz;
            this.taglib = taglib;
            this.tldFile = tldFile;
        }
        
        public String getText() {
            return getDisplayText();
        }
        
        public void performChange() {
        }
        
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }
        
        public FileObject getParentFile() {
            return tldFile;
        }
        
        public PositionBounds getPosition() {
            return null;
        }
        
    }
}
