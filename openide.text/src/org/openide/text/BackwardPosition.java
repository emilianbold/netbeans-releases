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

import javax.swing.event.*;
import javax.swing.text.*;


/** Position that stays at the same place if someone inserts
* directly to its offset.
*
* @author Jaroslav Tulach
*/
class BackwardPosition extends Object implements Position, DocumentListener {
    /** positions current offset */
    private int offset;

    /** Constructor.
    */
    private BackwardPosition(int offset) {
        this.offset = offset;
    }

    /** @param doc document
    * @param offset offset
    * @return new instance of the position
    */
    public static Position create(Document doc, int offset) {
        BackwardPosition p = new BackwardPosition(offset);
        doc.addDocumentListener(org.openide.util.WeakListeners.document(p, doc));

        return p;
    }

    //
    // Position
    //

    /** @return the offset
    */
    public int getOffset() {
        return offset;
    }

    //
    // document listener
    //

    /** Updates */
    public void insertUpdate(DocumentEvent e) {
        // less, not less and equal
        if (e.getOffset() < offset) {
            offset += e.getLength();
        }
    }

    /** Updates */
    public void removeUpdate(DocumentEvent e) {
        int o = e.getOffset();

        if (o < offset) {
            offset -= e.getLength();

            // was the position in deleted range? => go to its beginning
            if (offset < o) {
                offset = o;
            }
        }
    }

    /** Nothing */
    public void changedUpdate(DocumentEvent e) {
    }
}
