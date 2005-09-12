/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
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
import org.openide.text.PositionRef;

/**
 *
 * @author Milos Kleint
 */
public abstract class AbstractRefactoringElement extends SimpleRefactoringElementImpl implements RefactoringElementImplementation {
    
    private int status = RefactoringElementImplementation.NORMAL;

    protected String name;
    protected FileObject parentFile;
    protected boolean enabled = true;

    /** Creates a new instance of AbstractWhereUsedRefactoringElement */
    public AbstractRefactoringElement() {
    }

    /** Indicates whether this refactoring element is enabled.
     * @return <code>true</code> if this element is enabled, otherwise <code>false</code>.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /** Returns text describing the refactoring element.
     * @return Text.
     */
    public String getText() {
        return getDisplayText();
    }

    /** Enables/disables this element.
     * @param enabled If <code>true</code> the element is enabled, otherwise it is disabled.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** Returns Java element associated with this refactoring element.
     * @return MDR Java element.
     */
    public Element getJavaElement() {
        return null;
    }

    /** Returns file that the element affects (relates to)
     * @return File
     */
    public FileObject getParentFile() {
        return parentFile;
    }

    /** Returns position bounds of the text to be affected by this refactoring element.
     */
    public PositionBounds getPosition() {
        try {
            DataObject dobj = DataObject.find(getParentFile());
            if (dobj != null) {
                EditorCookie.Observable obs = (EditorCookie.Observable)dobj.getCookie(EditorCookie.Observable.class);
                if (obs != null && obs instanceof CloneableEditorSupport) {
                    CloneableEditorSupport supp = (CloneableEditorSupport)obs;
                
                PositionBounds bounds = new PositionBounds(
                        supp.createPositionRef(0, Position.Bias.Forward),
                        supp.createPositionRef(0, Position.Bias.Forward)
                        );
                
                return bounds;
            }
            }
        } catch (DataObjectNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /** Returns the status of this refactoring element (whether it is a normal element,
     * or a warning.
     * @return Status of this element.
     */
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    /** Performs the change represented by this refactoring element.
     */
    public void performChange() { }
}
