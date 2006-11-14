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

package org.netbeans.modules.editor.guards;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;

/**
 * Represents a simple guarded section.
 * It consists of one contiguous block.
 */
public final class SimpleSectionImpl extends GuardedSectionImpl {
    /** Text range of the guarded section. */
    private PositionBounds bounds;

    /**
     * Creates new section.
     * @param name Name of the new section.
     * @param bounds The range of the section.
     */
    SimpleSectionImpl(String name, PositionBounds bounds, GuardedSectionsImpl guards) {
        super(name, guards);
        this.bounds = bounds;
    }

    /**
     * Set the text of the section.
     * @param text the new text
     */
    public void setText(String text) {
        setText(bounds, text, true);
    }

    void markGuarded(StyledDocument doc) {
        markGuarded(doc, bounds, true);
    }

    /**
     * Unmarks the section as guarded.
     * @param doc The styled document where this section placed in.
     */
    void unmarkGuarded(StyledDocument doc) {
        markGuarded(doc, bounds, false);
    }

    public Position getCaretPosition() {
        return bounds.getBegin();
    }

    public String getText() {
        String text = ""; // NOI18N
        try {
            text = bounds.getText();
        } catch (BadLocationException ex) {
            // ignore
            Logger.getLogger("guards").log(Level.ALL, null, ex);
        }
        return text;
    }

    /*
    public String toString() {
      StringBuffer buf = new StringBuffer("SimpleSection:"+name); // NOI18N
      buf.append("\"");
      try {
        buf.append(bounds.getText());
      }
      catch (Exception e) {
        buf.append("EXCEPTION:"); // NOI18N
        buf.append(e.getMessage());
      }
      buf.append("\"");
      return buf.toString();
    }*/

    public Position getEndPosition() {
        return bounds.getEnd();
    }

    public boolean contains(Position pos, boolean allowHoles) {
        return bounds.getBegin().getOffset() <= pos.getOffset() &&
                bounds.getEnd().getOffset() >= pos.getOffset();
    }

    public Position getStartPosition() {
        return bounds.getBegin();
    }

    public void resolvePositions() throws BadLocationException {
        bounds.resolvePositions();
    }
}
