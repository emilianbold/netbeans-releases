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

package org.netbeans.modules.visualweb.insync.faces.refactoring;

import java.io.IOException;
import java.lang.ref.WeakReference;

import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

// NOTE:Copied from org.netbeans.modules.refactoring.java.DiffElement.
/**
 * Implementatation of RefactoringElementImplementation specific to refactoring
 * in java files.
 * 
 * @author Jan Becicka
 */
 public final class DiffElement extends SimpleRefactoringElementImplementation {
    private PositionBounds bounds;
    private String displayText;
    private FileObject parentFile;
    private Difference diff;
    private ModificationResult modification;
    private WeakReference<String> newFileContent;
    
    private DiffElement(Difference diff, PositionBounds bounds, FileObject parentFile, ModificationResult modification) {
        this.bounds = bounds;
        this.displayText = diff.getDescription();
        this.parentFile = parentFile;
        this.diff = diff;
        this.modification = modification;
    }

    public String getDisplayText() {
        return displayText;
    }

    public Lookup getLookup() {
        Object composite = ElementGripFactory.getDefault().get(parentFile, bounds.getBegin().getOffset());
        if (composite==null) 
            composite = parentFile;
        return Lookups.fixed(composite, diff);
    }
    
    public void setEnabled(boolean enabled) {
        diff.exclude(!enabled);
        newFileContent = null;
        super.setEnabled(enabled);
    }

    public PositionBounds getPosition() {
        return bounds;
    }

    public String getText() {
        return displayText;
    }

    public void performChange() {
    }

    public FileObject getParentFile() {
        return parentFile;
    }
    
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
    
    /**
     * Factory method for DiffElement
     * @param diff diff instance corresponding to thid Element
     * @param fileObject fileObject corresponding to this Element
     * @param modification 
     * @return ModificationResult corresponding to this change
     */
    public static DiffElement create(Difference diff, FileObject fileObject, ModificationResult modification) {
        PositionRef start = diff.getStartPosition();
        PositionRef end = diff.getEndPosition();
        PositionBounds bounds = new PositionBounds(start, end);
        return new DiffElement(diff, bounds, fileObject, modification);
    }    
}
