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
package org.netbeans.modules.refactoring.api;

import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;

/** Interface representing a refactoring element (object affected by a refactoring)
 * returned in a collection from {@link org.netbeans.modules.refactoring.api.AbstractRefactoring#prepare} operation.
 * <p>
 *
 * @see RefactoringElementImplementation
 * @author Martin Matula
 */
public final class RefactoringElement {
    /** Status corresponding to a normal element */
    public static final int NORMAL = 0;
    /** Status corresponding to an element that has a warning associated with it */
    public static final int WARNING = 1;
    /** Status flag that indicates that the element cannot be enabled (if a fatal
     * problem is associated with it) */
    public static final int GUARDED = 2;
    /** This element is in read-only file */
    public static final int READ_ONLY = 3;
    
    // delegate
    final RefactoringElementImplementation impl;
    
    RefactoringElement(RefactoringElementImplementation impl) {
        assert impl != null;
        this.impl = impl;
    }
    
    /** Returns text describing the refactoring element.
     * @return Text.
     */
    public String getText() {
        return impl.getText();
    }
    
    /** Returns text describing the refactoring formatted for display (using HTML tags).
     * @return Formatted text.
     */
    public String getDisplayText() {
        return impl.getDisplayText();
    }
    
    /** Indicates whether this refactoring element is enabled.
     * @return <code>true</code> if this element is enabled, otherwise <code>false</code>.
     */
    public boolean isEnabled() {
        return impl.isEnabled();
    }
    
    /** Enables/disables this element.
     * @param enabled If <code>true</code> the element is enabled, otherwise it is disabled.
     */
    public void setEnabled(boolean enabled) {
        impl.setEnabled(enabled);
    }
    
    /** 
     * Returns element associated with this refactoring element.
     * For instance Java Implementation returns internal representation of method
     * from getComposite() of RefectoringElement representing reference inside 
     * of method body.
     * @see org.netbeans.modules.refactoring.spi.ui.TreeElement
     * @see org.netbeans.modules.refactoring.spi.ui.TreeElementFactoryImplementation 
     * @return element.
     */
    public Object getComposite() {
        Object o = impl.getComposite();
        if (o==null)
            return getParentFile();
        return o;
    }
    
    /** Returns file that the element affects (relates to)
     * @return File
     */
    public FileObject getParentFile() {
        return impl.getParentFile();
    }
    
    /** Returns position bounds of the text to be affected by this refactoring element.
     */
    public PositionBounds getPosition() {
        return impl.getPosition();
    }
    
    /** Returns the status of this refactoring element (whether it is a normal element,
     * or a warning.
     * @return Status of this element.
     */
    public int getStatus() {
        return impl.getStatus();
    }
}
