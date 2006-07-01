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
package org.openide.text;

import org.openide.util.WeakListeners;

import javax.swing.text.*;


/** Listener to changes in the document.
*
* @author Jaroslav Tulach
*/
final class LineListener extends Object implements javax.swing.event.DocumentListener {
    /** original count of lines */
    private int orig;

    /** document to work with */
    public final StyledDocument doc;

    /** root element of all lines */
    private Element root;

    /** last tested amount of lines */
    private int lines;

    /** operations on lines */
    private LineStruct struct;

    /** Support necessary for getting Set of lines*/
    CloneableEditorSupport support;

    /** Creates new LineListener */
    public LineListener(StyledDocument doc, CloneableEditorSupport support) {
        this.doc = doc;
        this.struct = new LineStruct();
        root = NbDocument.findLineRootElement(doc);
        orig = lines = root.getElementCount();
        this.support = support;

        doc.addDocumentListener(org.openide.util.WeakListeners.document(this, doc));
    }

    /** Getter for amount of lines */
    public int getOriginalLineCount() {
        return orig;
    }

    /** Convertor between old and new line sets */
    public int getLine(int i) {
        return struct.convert(i, true /*originalToCurrent*/);
    }

    /** Convertor between old and new line sets */
    public int getOld(int i) {
        return struct.convert(i, false /*currentToOriginal*/);
    }

    public void removeUpdate(javax.swing.event.DocumentEvent p0) {
        int elem = root.getElementCount();
        int delta = lines - elem;
        lines = elem;

        int lineNumber = NbDocument.findLineNumber(doc, p0.getOffset());

        if (delta > 0) {
            struct.deleteLines(lineNumber, delta);
        }

        if (support == null) {
            return;
        }

        Line.Set set = support.getLineSet();

        if (!(set instanceof DocumentLine.Set)) {
            return;
        }

        // Notify lineSet there was changed range of lines.
        ((DocumentLine.Set) set).linesChanged(lineNumber, lineNumber + delta, p0);

        if (delta > 0) {
            // Notify Line.Set there was moved range of lines.
            ((DocumentLine.Set) set).linesMoved(lineNumber, elem);
        }
    }

    public void changedUpdate(javax.swing.event.DocumentEvent p0) {
    }

    public void insertUpdate(javax.swing.event.DocumentEvent p0) {
        int elem = root.getElementCount();

        int delta = elem - lines;
        lines = elem;

        int lineNumber = NbDocument.findLineNumber(doc, p0.getOffset());

        if (delta > 0) {
            struct.insertLines(lineNumber, delta);
        }

        if (support == null) {
            return;
        }

        Line.Set set = support.getLineSet();

        if (!(set instanceof DocumentLine.Set)) {
            return;
        }

        // Nptify Line.Set there was changed range of lines.
        ((DocumentLine.Set) set).linesChanged(lineNumber, lineNumber, p0);

        if (delta > 0) {
            // Notify Line.Set there was moved range of lines.
            ((DocumentLine.Set) set).linesMoved(lineNumber, elem);
        }
    }
}
