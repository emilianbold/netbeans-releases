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
package org.netbeans.api.editor.guards;

import java.beans.PropertyVetoException;
import javax.swing.text.Position;
import org.netbeans.modules.editor.guards.GuardedSectionImpl;

/**
 * Represents one guarded section.
 */
public class GuardedSection {
    
    private final GuardedSectionImpl impl;

    /**
     * Creates new section.
     * @param name Name of the new section.
     */
    GuardedSection(GuardedSectionImpl impl) {
        assert impl != null;
        this.impl = impl;
        impl.attach(this);
    }
    
    /**
     * Get the name of the section.
     * @return the name
     */
    public String getName() {
        return impl.getName();
    }

    /**
     * Set the name of the section.
     * @param name the new name
     * @exception PropertyVetoException if the new name is already in use
     */
    public void setName(String name) throws PropertyVetoException {
        impl.setName(name);
    }

    /**
     * Removes the section and the text of the section from the Document.
     * The section will then be invalid
     * and it will be impossible to use its methods.
     */
    public void deleteSection() {
        impl.deleteSection();
    }

    /**
     * Tests if the section is still valid - it is not removed from the
     * source.
     */
    public boolean isValid() {
        return impl.isValid();
    }

    /**
     * Removes the section from the Document, but retains the text contained
     * within. The method should be used to unprotect a region of code
     * instead of calling NbDocument.
     */
    public void removeSection() {
        impl.removeSection();
    }
    
    /**
     * Gets the begin of section. To this position is set the caret
     * when section is open in the editor.
     * @return the position to place the caret.
     */
    public Position getCaretPosition() {
        return impl.getCaretPosition();
    }
    
    /**
     * Gets the text contained in the section.
     * @return The text contained in the section.
     */
    public String getText() {
        return impl.getText();
    }

    /**
     * Assures that a position is not inside the guarded section. Complex guarded sections
     * that contain portions of editable text can return true if the tested position is
     * inside one of such portions provided that permitHoles is true.
     * @param pos position in question
     * @param permitHoles if false, guarded section is taken as a monolithic block
     * without any holes in it regardless of its complexity.
     * @return <code>true</code> if the position is inside section.
     */
    public boolean contains(Position pos, boolean permitHoles) {
        return impl.contains(pos, permitHoles);
    }
    
    /**
     * Returns the end position of the whole guarded section.
     * @return the end position of the guarded section.
     */
    public Position getEndPosition() {
        return impl.getEndPosition();
    }
    
    /** 
     * Returns the start position of the whole guarded section.
     * @return the start position of the guarded section.
     */
    public Position getStartPosition() {
        return impl.getStartPosition();
    }
    
    GuardedSectionImpl getImpl() {
        return impl;
    }

}
