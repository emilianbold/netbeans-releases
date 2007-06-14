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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.web.jsf.refactoring;

import java.io.IOException;
import java.lang.ref.WeakReference;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.web.jsf.refactoring.Modifications.Difference;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Petr Pisl
 */
public abstract class DiffElement extends SimpleRefactoringElementImplementation {
    private final Difference diff;
    private final Modifications modification;
    private WeakReference<String> newFileContent;
    private final FileObject parentFile;
    
    public DiffElement(Difference diff, FileObject parentFile, Modifications modification) {
        this.diff = diff;
        this.modification = modification;
        this.parentFile = parentFile;
    }
    
    public String getDisplayText(){
        return diff.getDesription();
    }
    
    public String getText(){
        return diff.getDesription();
    }
    
    public Lookup getLookup() {
        return Lookups.fixed(parentFile, diff);
    }
    
    public void setEnabled(boolean enabled) {
        diff.setExclude(!enabled);
        newFileContent = null;
        super.setEnabled(enabled);
    }
    
    public FileObject getParentFile() {
        return parentFile;
    }
    
    @Override
    protected String getNewFileContent() {
        String result;
        if (newFileContent !=null) {
            result = newFileContent.get();
            if (result!=null)
                return result;
        }
        try {
            result = modification.getResultingSource(parentFile);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
        newFileContent = new WeakReference(result);
        return result;
    }
    
    
    public static class ChangeFQCNElement extends DiffElement{
        private final Occurrences.OccurrenceItem occurence;
        
        public ChangeFQCNElement(Difference diff, Occurrences.OccurrenceItem occurence, Modifications modification) {
            super(diff, occurence.getFacesConfig(), modification);
            this.occurence = occurence;
        }
        
        public void performChange() {
            occurence.performChange();
        }
        
        @Override
        public void undoChange() {
            occurence.undoChange();
        }
        
        public PositionBounds getPosition() {
            return occurence.getChangePosition();
        }
    }
}
