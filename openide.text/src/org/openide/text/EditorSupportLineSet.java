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
package org.openide.text;

import java.lang.ref.Reference;
import java.util.*;

import javax.swing.event.*;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;


/** Line set for an EditorSupport.
*
* @author Jaroslav Tulach, David Konecny
*/
final class EditorSupportLineSet extends DocumentLine.Set {
    /** support we are attached to */
    private CloneableEditorSupport support;

    /** Constructor.
    * @param support support to work with
    * @param doc document to use
    */
    public EditorSupportLineSet(CloneableEditorSupport support, StyledDocument doc) {
        super(doc, support);
        this.support = support;
    }

    /** Shares the whm with other line sets based on the same support.
     */
    Map<Line,Reference<Line>> findWeakHashMap() {
        return support.findWeakHashMap();
    }

    /** Creates a Line for given offset.
    * @param offset the begining of line
    * @return line that should represent the given line
    */
    public Line createLine(int offset) {
        PositionRef ref = new PositionRef(support.getPositionManager(), offset, Position.Bias.Forward);

        return new SupportLine(support.getLookup(), ref, support);
    }

    /** Line for my work.
    */
    private static final class SupportLine extends DocumentLine {
        static final long serialVersionUID = 7282223299866986051L;

        /** Position reference to a place in document
        */
        public SupportLine(org.openide.util.Lookup obj, PositionRef ref, CloneableEditorSupport support) {
            super(obj, ref);
        }

        /** Shows the line.
        * @param kind one of SHOW_XXX constants.
        * @column the column of this line which should be selected
        */
        public void show(int kind, int column) {
            CloneableEditorSupport support = pos.getCloneableEditorSupport();

            if ((kind == SHOW_TRY_SHOW) && !support.isDocumentLoaded()) {
                return;
            }

            CloneableEditorSupport.Pane editor;
            
            if (kind == SHOW_REUSE || kind == SHOW_REUSE_NEW) {
                editor = support.openReuse(pos, column, kind);
            } else {
                editor = support.openAt(pos, column);
                if (kind == SHOW_TOFRONT) editor.getComponent().toFront();
            }
            if (kind != SHOW_TRY_SHOW && kind != SHOW_SHOW) {
                editor.getComponent().requestActive();
            }
        }

        /** This method will be used for annotation of part of the text on the line.*/
        public Line.Part createPart(int column, int length) {
            DocumentLine.Part part = new DocumentLine.Part(
                    this,
                    new PositionRef(
                        pos.getCloneableEditorSupport().getPositionManager(), pos.getOffset() + column,
                        Position.Bias.Forward
                    ), length
                );
            addLinePart(part);

            return part;
        }

        /** Let's the support determine the name */
        public String getDisplayName() {
            CloneableEditorSupport support = pos.getCloneableEditorSupport();

            return support.messageLine(this);
        }

        public String toString() {
            return "SupportLine@" + Integer.toHexString(System.identityHashCode(this)) + " at line: " +
            getLineNumber(); // NOI18N
        }
    }

    /** Line set for closed EditorSupport.
    *
    * @author Jaroslav Tulach
    */
    static class Closed extends Line.Set implements ChangeListener {
        /** support we are attached to */
        private CloneableEditorSupport support;

        /** line set to delegate to or null if the editor is still closed,
        * is set to non null when the editor opens
        */
        private Line.Set delegate;

        /** Constructor.
        * @param support support to work with
        * @param doc document to use
        */
        public Closed(CloneableEditorSupport support) {
            this.support = support;
            support.addChangeListener(org.openide.util.WeakListeners.change(this, support));
        }

        /** Shares the whm with other line sets based on the same support.
         */
        Map<Line,Reference<Line>> findWeakHashMap() {
            return support.findWeakHashMap();
        }

        /** Returns a set of line objects sorted by their
        * line numbers. This immutable list will contains all lines held by this
        * line set.
        *
        * @return list of element type {@link Line}
        */
        public List<? extends Line> getLines() {
            if (delegate != null) {
                return delegate.getLines();
            }

            // PENDING
            return new ArrayList<Line>();
        }

        /** Find line object in the line set corresponding to original line number.
        * That is, finds the line in the current document which originally had the indicated line number.
        * If there have been modifications of that line, find one as close as possible.
        *
        * @param line number of the line
        * @return line object
        * @exception IndexOutOfBoundsException if <code>line</code> is an invalid index for the original set of lines
        */
        public Line getOriginal(int line) throws IndexOutOfBoundsException {
            if (delegate != null) {
                return delegate.getOriginal(line);
            }

            return getCurrent(line);
        }

        /** Find line object in the line set corresponding to current line number.
        *
        * @param line number of the line
        * @return line object
        * @exception IndexOutOfBoundsException if <code>line</code> is an invalid index for the original set of lines
        */
        public Line getCurrent(int line) throws IndexOutOfBoundsException {
            PositionRef ref = new PositionRef(support.getPositionManager(), line, 0, Position.Bias.Forward);

            // obj can be null, sorry...
            org.openide.util.Lookup obj = support.getLookup();

            return this.registerLine(new SupportLine(obj, ref, support));
        }

        /** Arrives when the document is opened.
        */
        public synchronized void stateChanged(ChangeEvent ev) {
            if (delegate == null) {
                StyledDocument doc = support.getDocument();

                if (doc != null) {
                    delegate = new EditorSupportLineSet(support, doc);
                }
            } else {
                if (support.getDocument() == null) {
                    delegate = null;
                }
            }
        }
    }
}
