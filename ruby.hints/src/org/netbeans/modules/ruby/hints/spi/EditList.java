/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ruby.hints.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.ruby.Formatter;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

/**
 * A list of edits to be made to a document.  This should probably be combined with the many
 * other similar abstractions in other classes; ModificationResult, Diff, etc.
 * 
 * @todo Take out the offsetOrdinal number, and manage that on the edit list side
 *  (order of entry for duplicates should insert a new ordinal)
 * @todo Make formatting more explicit; allow to add a "format" region edit. These must
 *   be sorted such that they don't overlap after edits and are all applied last.
 * 
 * @author Tor Norbye
 */
public class EditList {
    private BaseDocument doc;
    private List<Edit> edits;
    private boolean format;
    
    public EditList(BaseDocument doc) {
        this.doc = doc;
        edits = new ArrayList<Edit>();
    }
  
    @Override
    public String toString() {
        return "EditList(" + edits + ")";
    }

    public EditList replace(int offset, int removeLen, String insertText, boolean format, int offsetOrdinal) {
        edits.add(new Edit(offset, removeLen, insertText, format, offsetOrdinal));
        
        return this;
    }
    
    public void applyToDocument(BaseDocument otherDoc/*, boolean narrow*/) {
        EditList newList = new EditList(otherDoc);
        newList.format = format;
        /*
        if (narrow) {
            OffsetRange range = getRange();
            int start = range.getStart();
            int lineno = NbDocument.findLineNumber((StyledDocument) otherDoc,start);
            lineno = Math.max(0, lineno-3);
            start = NbDocument.findLineOffset((StyledDocument) otherDoc,lineno);

            List newEdits = new ArrayList<Edit>(edits.size());
            newList.edits = newEdits;
            for (Edit edit : edits) {
                newEdits.add(new Edit(edit.offset-start, edit.removeLen, edit.insertText, edit.format, edit.offsetOrdinal));
            }
        } else {
         */
            newList.edits = edits;
        //}
        newList.apply();
    }

    public void apply() {
        apply(-1);
    }
    
    public void format() {
        this.format = true;
    }

    /** Apply the given list of edits in the current document. If positionOffset is a position
     * within one of the regions, return a document Position that corresponds to it.
     */
    public Position apply(int positionOffset) {
        if (edits.size() == 0) {
            if (positionOffset >= 0) {
                try {
                    return doc.createPosition(0);
                } catch (BadLocationException ble) {
                    Exceptions.printStackTrace(ble);
                }
            }
            return null;
        }

        Position position = null;

        Collections.sort(edits);
        Collections.reverse(edits);
        Formatter formatter = new Formatter();

        try {
            doc.atomicLock();
            int lastOffset = edits.get(0).offset;
            Position lastPos = doc.createPosition(lastOffset, Position.Bias.Forward);
            
            // Apply edits in reverse order (to keep offsets accurate)
            for (Edit edit : edits) {
                if (edit.removeLen > 0) {
                    doc.remove(edit.offset, edit.removeLen);
                }
                if (edit.getInsertText() != null) {
                    doc.insertString(edit.offset, edit.insertText, null);
                    int end = edit.offset + edit.insertText.length();
                    if (edit.getOffset() <= positionOffset && end >= positionOffset) {
                        position = doc.createPosition(positionOffset); // Position of the comment
                    }
                    if (edit.format) {
                        formatter.reindent(doc, edit.offset, end);
                    }
                }
            }
            
            if (format) {
                int firstOffset = edits.get(edits.size()-1).offset;
                lastOffset = lastPos.getOffset();
                formatter.reindent(doc, firstOffset, lastOffset);
            }
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        } finally {
            doc.atomicUnlock();
        }

        return position;
    }
    
    public OffsetRange getRange() {
        int minOffset = edits.get(0).offset;
        int maxOffset = minOffset;
        for (Edit edit : edits) {
            if (edit.offset < minOffset) {
                minOffset = edit.offset;
            }
            if (edit.offset > maxOffset) {
                maxOffset = edit.offset;
            }
        }
        
        return new OffsetRange(minOffset, maxOffset);
    }
    
    public int firstLine(BaseDocument doc) {
        OffsetRange range = getRange();
        
        return NbDocument.findLineNumber((StyledDocument)doc, range.getStart());
    }
}
