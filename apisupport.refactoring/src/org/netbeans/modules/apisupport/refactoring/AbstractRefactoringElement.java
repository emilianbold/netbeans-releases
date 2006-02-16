/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.refactoring;

import javax.swing.text.Position;
import org.netbeans.jmi.javamodel.Element;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImpl;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;

/**
 *
 * @author Milos Kleint
 */
public abstract class AbstractRefactoringElement extends SimpleRefactoringElementImpl implements RefactoringElementImplementation {
    
    private int status = RefactoringElementImplementation.NORMAL;

    protected String name;
    protected FileObject parentFile;
    protected boolean enabled = true;

    public AbstractRefactoringElement() {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getText() {
        return getDisplayText();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Element getJavaElement() {
        return null;
    }

    public FileObject getParentFile() {
        return parentFile;
    }
    
    /** start and end positions of text (must be 2-element array); default [0, 0] */
    protected int[] location() {
        return new int[] {0, 0};
    }
    private int[] loc; // cached

    public PositionBounds getPosition() {
        try {
            DataObject dobj = DataObject.find(getParentFile());
            if (dobj != null) {
                EditorCookie.Observable obs = (EditorCookie.Observable)dobj.getCookie(EditorCookie.Observable.class);
                if (obs != null && obs instanceof CloneableEditorSupport) {
                    CloneableEditorSupport supp = (CloneableEditorSupport)obs;

                    if (loc == null) {
                        loc = location();
                    }
                PositionBounds bounds = new PositionBounds(
                        supp.createPositionRef(loc[0], Position.Bias.Forward),
                        supp.createPositionRef(Math.max(loc[0], loc[1]), Position.Bias.Forward)
                        );
                
                return bounds;
            }
            }
        } catch (DataObjectNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void performChange() { }
    
}
