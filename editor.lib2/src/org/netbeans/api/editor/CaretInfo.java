/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.api.editor;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.text.Position;
import org.netbeans.api.annotations.common.CheckForNull;

/**
 * Info about a single caret - see {@link EditorCaret}.
 *
 * @author Miloslav Metelka
 * @author Ralph Ruijs
 */
public final class CaretInfo {
    
    // -J-Dorg.netbeans.api.editor.CaretInfo.level=FINEST
    private static final Logger LOG = Logger.getLogger(CaretInfo.class.getName());
    
    // -J-Dorg.netbeans.api.editor.CaretInfo.EDT.level=FINE - check that setDot() and other operations in EDT only
    private static final Logger LOG_EDT = Logger.getLogger(CaretInfo.class.getName() + ".EDT");

    static {
        // Compatibility debugging flags mapping to logger levels
        if (Boolean.getBoolean("netbeans.debug.editor.caret.focus") && LOG.getLevel().intValue() < Level.FINE.intValue())
            LOG.setLevel(Level.FINE);
        if (Boolean.getBoolean("netbeans.debug.editor.caret.focus.extra") && LOG.getLevel().intValue() < Level.FINER.intValue())
            LOG.setLevel(Level.FINER);
    }
    
    private final EditorCaret parent;
    protected Position dotPos;
    protected Position markPos;
    private Point magicCaretPosition;
    private boolean invalid;

    public CaretInfo(EditorCaret parent) {
        this.parent = parent;
    }

    public CaretInfo(EditorCaret parent, Position dotPos, Position markPos) {
        this(parent);
        this.dotPos = dotPos;
        this.markPos = markPos;
    }

    /**
     * Get position of the caret itself.
     * @return non-null position of the caret placement. The position may be virtual
     *  so methods in {@link VirtualPositions} may be used if necessary.
     */
    @CheckForNull
    public Position getDotPosition() {
        return dotPos;
    }

    /**
     * Return either the same object like {@link #getDotPosition()} if there's no selection
     * or return position denoting the other end of an existing selection (which is either before
     * or after the dot position depending of how the selection was created).
     * @return non-null position of the caret placement. The position may be virtual
     *  so methods in {@link VirtualPositions} may be used if necessary.
     */
    @CheckForNull
    public Position getMarkPosition() {
        return markPos;
    }
    
    public int getDot() {
        return (dotPos != null) ? dotPos.getOffset() : 0;
    }

    public int getMark() {
        return (markPos != null) ? markPos.getOffset() : 0;
    }
    
    public void setDot(int offset) {
        if (LOG_EDT.isLoggable(Level.FINE)) { // Only permit operations in EDT
            if (!SwingUtilities.isEventDispatchThread()) {
                throw new IllegalStateException("CaretInfo.setDot() not in EDT: offset=" + offset); // NOI18N
            }
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("setDot: offset=" + offset); //NOI18N
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.INFO, "setDot call stack", new Exception());
            }
        }

        parent.setDotCaret(offset, this, true);
    }

    public void moveDot(int offset) {
        if (LOG_EDT.isLoggable(Level.FINE)) { // Only permit operations in EDT
            if (!SwingUtilities.isEventDispatchThread()) {
                throw new IllegalStateException("CaretInfo.moveDot() not in EDT: offset=" + offset); // NOI18N
            }
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("moveDot: offset=" + offset); //NOI18N
        }

        parent.moveDotCaret(offset, this);
    }

    /**
     * Determine if this caret is still valid.
     * <br/>
     * The caret may become invalid by automatic caret merging by document removals
     * or as an effect of {@link EditorCaret#removeLastCaret() }
     * or {@link EditorCaret#replaceCarets(java.util.List) }.
     * @return true if this caret is valid or false otherwise.
     */
    public boolean isValid() {
        return !invalid;
    }

    /**
     * @return true if there's a selection or false if there's no selection for this caret.
     */
    public boolean isSelection() {
        return dotPos != markPos;
    }

    public Position getSelectionStart() {
        return dotPos; // TBD - possibly inspect virtual columns etc.
    }

    public Position getSelectionEnd() {
        return dotPos; // TBD - possibly inspect virtual columns etc.
    }
    
    public void setMagicCaretPosition(Point newMagicCaretPosition) {
        this.magicCaretPosition = newMagicCaretPosition;
    }

    public Point getMagicCaretPosition() {
        return magicCaretPosition;
    }
    
    /* Remove from CaretInfo ? */
    public void setDotPos(Position dotPos) { this.dotPos = dotPos; }
    public void setMarkPos(Position markPos) { this.markPos = markPos; }
    
    private char dotChar;
    public void setDotChar(char dotChar) {
        this.dotChar = dotChar;
    }
    public char getDotChar() {
        return this.dotChar;
    }

    private Rectangle caretBounds;
    public void setCaretBounds(Rectangle newCaretBounds) {
        this.caretBounds = newCaretBounds;
    }
    public Rectangle getCaretBounds() {
        return this.caretBounds;
    }
}
