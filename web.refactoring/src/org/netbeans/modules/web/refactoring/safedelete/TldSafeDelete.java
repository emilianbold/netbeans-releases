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
package org.netbeans.modules.web.refactoring.safedelete;

import java.io.IOException;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.web.refactoring.TldRefactoring;
import org.netbeans.modules.web.taglib.TLDDataObject;
import org.netbeans.modules.web.taglib.model.TagType;
import org.netbeans.modules.web.taglib.model.Taglib;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Safe delete for tld files.
 *
 * @author Erno Mononen
 */
public class TldSafeDelete extends TldRefactoring{
    
    private final String clazz;
    private final FileObject source;
    private final SafeDeleteRefactoring safeDelete;
    
    public TldSafeDelete(String clazz, SafeDeleteRefactoring safeDelete, FileObject source) {
        this.clazz = clazz;
        this.safeDelete = safeDelete;
        this.source = source;
    }
    
    
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        for(TaglibHandle taglibHandle : getTaglibs(source)){
            Taglib taglib = taglibHandle.getTaglib();
            for (TagType tagType : taglib.getTag()){
                if (clazz.equals(tagType.getTagClass())){
                    refactoringElements.add(safeDelete, new TagClassSafeDeleteElement(clazz, taglib, taglibHandle.getTldFile(), tagType));
                }
            }
        }
        
        return null;
    }
    
    private static class TagClassSafeDeleteElement extends TldRefactoringElement {
        
        private final TagType tagType;
        
        public TagClassSafeDeleteElement(String clazz, Taglib taglib, FileObject tldFile, TagType tagType) {
            super(clazz, taglib, tldFile);
            this.tagType = tagType;
        }
        
        
        public String getDisplayText() {
            return NbBundle.getMessage(TldSafeDelete.class, "TXT_TaglibTagClassSafeDelete", tagType.getName());
        }
        
        @Override
        public void undoChange() {
            taglib.addTag(tagType);
            write();
        }
        
        public void performChange() {
            taglib.removeTag(tagType);
            write();
        }

        private void write() {
            try {
                TLDDataObject tdo = (TLDDataObject) DataObject.find(tldFile);
                if (tdo != null) {
                    tdo.write(taglib);
                }
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
    }
    
}
