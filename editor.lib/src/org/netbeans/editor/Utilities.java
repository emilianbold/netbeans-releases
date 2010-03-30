/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.editor;

import java.awt.Rectangle;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.TextAction;
import javax.swing.text.Caret;
import javax.swing.plaf.TextUI;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Element;
import javax.swing.text.View;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.lib.drawing.DrawEngineDocView;
import org.netbeans.modules.editor.lib2.EditorPreferencesKeys;
import org.openide.util.NbBundle;

/**
* Various useful editor functions. Some of the methods have
* the same names and signatures like in javax.swing.Utilities but
* there is also many other useful methods.
* All the methods are static so there's no reason to instantiate Utilities.
*
* All the methods working with the document rely on that it is locked against
* modification so they don't acquire document read/write lock by themselves
* to guarantee the full thread safety of the execution.
* It's the user's task to lock the document appropriately
* before using methods described here.
*
* Most of the methods require org.netbeans.editor.BaseDocument instance
* not just the javax.swing.text.Document.
* The reason for that is to mark that the methods work on BaseDocument
* instances only, not on generic documents. To convert the Document
* to BaseDocument the simple conversion (BaseDocument)target.getDocument()
* can be done or the method getDocument(target) can be called.
* There are also other conversion methods like getEditorUI(), getKit()
* or getKitClass().
*
* @author Miloslav Metelka
* @version 0.10
*/

public class Utilities {

    private static final String WRONG_POSITION_LOCALE = "wrong_position"; // NOI18N

    /** Switch the case to capital letters. Used in changeCase() */
    public static final int CASE_UPPER = 0;

    /** Switch the case to small letters. Used in changeCase() */
    public static final int CASE_LOWER = 1;

    /** Switch the case to reverse. Used in changeCase() */
    public static final int CASE_SWITCH = 2;
    
    /** Fake TextAction for getting the info of the focused component */
    private static TextAction focusedComponentAction;    
    
    private Utilities() {
        // instantiation has no sense
    }

    /** Get the starting position of the row.
    * @param c text component to operate on
    * @param offset position in document where to start searching
    * @return position of the start of the row or -1 for invalid position
    */
    public static int getRowStart(JTextComponent c, int offset)
    throws BadLocationException {
        Rectangle2D r = modelToView(c, offset);
        if (r == null){
            return -1;
        }
        EditorUI eui = getEditorUI(c);
        if (eui != null){
            return viewToModel(c, eui.textLeftMarginWidth, r.getY());
        }
        return -1;
    }

    /** Get the starting position of the row.
    * @param doc document to operate on
    * @param offset position in document where to start searching
    * @return position of the start of the row or -1 for invalid position
    */
    public static int getRowStart(BaseDocument doc, int offset)
    throws BadLocationException {
        return getRowStart(doc, offset, 0);
    }

    /** Get the starting position of the row while providing relative count
    * of row how the given position should be shifted. This is the most
    * efficient way how to move by lines in the document based on some
    * position. There is no similair getRowEnd() method that would have
    * shifting parameter.
    * @param doc document to operate on
    * @param offset position in document where to start searching
    * @param lineShift shift the given offset forward/back relatively
    *  by some amount of lines
    * @return position of the start of the row or -1 for invalid position
    */
    public static int getRowStart(BaseDocument doc, int offset, int lineShift)
    throws BadLocationException {
        
        checkOffsetValid(doc, offset);

        if (lineShift != 0) {
            Element lineRoot = doc.getParagraphElement(0).getParentElement();
            int line = lineRoot.getElementIndex(offset);
            line += lineShift;
            if (line < 0 || line >= lineRoot.getElementCount()) {
                return -1; // invalid line shift
            }
            return lineRoot.getElement(line).getStartOffset();

        } else { // no shift
            return doc.getParagraphElement(offset).getStartOffset();
        }
    }

    /** Get the first non-white character on the line.
    * The document.isWhitespace() is used to test whether the particular
    * character is white space or not.
    * @param doc document to operate on
    * @param offset position in document anywhere on the line
    * @return position of the first non-white char on the line or -1
    *   if there's no non-white character on that line.
    */
    public static int getRowFirstNonWhite(BaseDocument doc, int offset)
    throws BadLocationException {
        
        checkOffsetValid(doc, offset);

        Element lineElement = doc.getParagraphElement(offset);
        return getFirstNonWhiteFwd(doc,
            lineElement.getStartOffset(),
            lineElement.getEndOffset() - 1
        );
    }

    /** Get the last non-white character on the line.
    * The document.isWhitespace() is used to test whether the particular
    * character is white space or not.
    * @param doc document to operate on
    * @param offset position in document anywhere on the line
    * @return position of the last non-white char on the line or -1
    *   if there's no non-white character on that line.
    */
    public static int getRowLastNonWhite(BaseDocument doc, int offset)
    throws BadLocationException {
        
        checkOffsetValid(doc, offset);

        Element lineElement = doc.getParagraphElement(offset);
        return getFirstNonWhiteBwd(doc,
            lineElement.getEndOffset() - 1,
            lineElement.getStartOffset()
        );
    }

    /** Get indentation on the current line. If this line is white then
    * return -1.
    * @param doc document to operate on
    * @param offset position in document anywhere on the line
    * @return indentation or -1 if the line is white
    */
    public static int getRowIndent(BaseDocument doc, int offset)
    throws BadLocationException {
        offset = getRowFirstNonWhite(doc, offset);
        if (offset == -1) {
            return -1;
        }
        return doc.getVisColFromPos(offset);
    }

    /** Get indentation on the current line. If this line is white then
    * go either up or down an return indentation of the first non-white row.
    * The <tt>getRowFirstNonWhite()</tt> is used to find the indentation
    * on particular line.
    * @param doc document to operate on
    * @param offset position in document anywhere on the line
    * @param downDir if this flag is set to true then if the row is white
    *   then the indentation of the next first non-white row is returned. If it's
    *   false then the indentation of the previous first non-white row is returned.
    * @return indentation or -1 if there's no non-white line in the specified direction
    */
    public static int getRowIndent(BaseDocument doc, int offset, boolean downDir)
    throws BadLocationException {
        int p = getRowFirstNonWhite(doc, offset);
        if (p == -1) {
            p = getFirstNonWhiteRow(doc, offset, downDir);
            if (p == -1) {
                return -1; // non-white line not found
            }
            p = getRowFirstNonWhite(doc, p);
            if (p == -1) {
                return -1; // non-white line not found
            }
        }
        return doc.getVisColFromPos(p);
    }

    /** Get the end position of the row right before the new-line character.
    * @param c text component to operate on
    * @param offset position in document where to start searching
    * @param relLine shift offset forward/back by some amount of lines
    * @return position of the end of the row or -1 for invalid position
    */
    public static int getRowEnd(JTextComponent c, int offset)
    throws BadLocationException {
        Rectangle2D r = modelToView(c, offset);
        if (r == null){
            return -1;
        }
        return viewToModel(c, Integer.MAX_VALUE, r.getY());
    }
    
    public static int getRowEnd(BaseDocument doc, int offset)
    throws BadLocationException {
        checkOffsetValid(doc, offset);

        return doc.getParagraphElement(offset).getEndOffset() - 1;
    }
    
    private static int findBestSpan(JTextComponent c, int lineBegin, int lineEnd, int x)
    throws BadLocationException{
        if (lineBegin == lineEnd){
            return lineEnd;
        } 
        int low = lineBegin;
        int high = lineEnd;
        while (low <= high) {
            
            if (high - low < 3){
                int bestSpan = Integer.MAX_VALUE;
                int bestPos = -1;
                for (int i = low; i<=high; i++){
                    Rectangle tempRect = c.modelToView(i);
                    if (Math.abs(x-tempRect.x) < bestSpan){
                        bestSpan = Math.abs(x-tempRect.x);
                        bestPos = i;
                    }
                }
                return bestPos;
            }
            
            int mid = (low + high) / 2;
            
            Rectangle tempRect = c.modelToView(mid);
            if (tempRect.x > x){
                high = mid;
            } else if (tempRect.x < x) {
                low = mid;
            } else {
                return mid;
            }
        }
        return lineBegin;
    }

    /** Get the position that is one line above and visually at some
    * x-coordinate value.
    * @param doc document to operate on
    * @param offset position in document from which the current line is determined
    * @param x float x-coordinate value
    * @return position of the character that is at the one line above at
    *   the required x-coordinate value
    */
    public static int getPositionAbove(JTextComponent c, int offset, int x)
    throws BadLocationException {
        int rowStart = getRowStart(c, offset);
        int endInit = c.getUI().getNextVisualPositionFrom(c,
                              rowStart, Position.Bias.Forward, javax.swing.SwingConstants.WEST, null);

        if (x == BaseKit.MAGIC_POSITION_MAX){
            return endInit;
        }

        EditorUI eui = getEditorUI(c);
        if (eui == null){
            return offset; //skip
        }
        
        Rectangle2D r = modelToView(c, endInit);
        if (r == null){
            return offset; //skip
        }
        
        if (x == eui.textLeftMarginWidth){
            return getRowStart(c, endInit);
        }
        
        int end = viewToModel(c, Math.max(eui.textLeftMarginWidth, r.getX() + 2*r.getWidth()), r.getY());
        Rectangle tempRect = c.modelToView(end);
        if (tempRect == null || tempRect.x < x){
            end = endInit;
        }
        
        int start = viewToModel(c, Math.max(eui.textLeftMarginWidth, r.getX() - 2*r.getWidth()),r.getY());
        tempRect = c.modelToView(start);
        if (tempRect == null || tempRect.x > x){
            start = getRowStart(c, end);
        }
        
        int best = findBestSpan(c, start, end, x);
        
        if (best<c.getDocument().getLength()){
			// #56056
			int tmp = best + 1;
			int nextVisualPosition = c.getUI().getNextVisualPositionFrom(c,
					tmp, javax.swing.text.Position.Bias.Backward, javax.swing.SwingConstants.WEST, null);
            if (nextVisualPosition<best && nextVisualPosition >= 0){
				// #164820
				// We are in the collapsed fold, now try to find which position
				// is the best, whether foldEnd or foldStart
				tempRect = c.modelToView(nextVisualPosition);
				if (tempRect == null) {
					return nextVisualPosition;
				}
				int leftX = tempRect.x;
				int nextVisualPositionRight = c.getUI().getNextVisualPositionFrom(c,
						nextVisualPosition, javax.swing.text.Position.Bias.Forward, javax.swing.SwingConstants.EAST, null);
				tempRect = c.modelToView(nextVisualPositionRight);
				if (tempRect == null) {
					return nextVisualPosition;
				}
				int rightX = tempRect.x;

				if (Math.abs(leftX - x) < Math.abs(rightX - x)) {
					return nextVisualPosition;
				} else {
					return nextVisualPositionRight;
				}
			}
        }

        return best;
    }

    /** Get the position that is one line above and visually at some
    * x-coordinate value.
    * @param c text component to operate on
    * @param offset position in document from which the current line is determined
    * @param x float x-coordinate value
    * @return position of the character that is at the one line above at
    *   the required x-coordinate value
    */
    public static int getPositionBelow(JTextComponent c, int offset, int x)
    throws BadLocationException {
	int startInit = getRowEnd(c, offset) + 1;

        Rectangle2D r = modelToView(c, startInit);
        if (r == null){
            return offset; // skip
        }
        
        EditorUI eui = getEditorUI(c);
        if (eui != null && x ==eui.textLeftMarginWidth){
            return startInit;
        }
        
        int start = viewToModel(c, Math.min(Integer.MAX_VALUE, r.getX() + x - 2*r.getWidth()), r.getY());
        Rectangle tempRect = c.modelToView(start);
        if (tempRect!=null && tempRect.x > x){
            start = startInit;
        }
        
        int end = viewToModel(c, Math.min(Integer.MAX_VALUE, r.getX() + x + 2*r.getWidth()), r.getY());
        tempRect = c.modelToView(end);
        if (tempRect!=null && tempRect.x < x){
            end = getRowEnd(c, start);
        }
        
        int best = findBestSpan(c, start, end, x);
        
        if (best>0){
            // #70254 - make sure $best is not in collapsed fold area. Try
            // getNextVisualPositionFrom to EAST from the position $best-1.
            // If the resulted next visual position is not equal to $best,
            // $best is in the collapsed fold and foldEnd or foldStart 
            // should be returned.
            int tmp = best - 1;
            int nextVisualPosition = c.getUI().getNextVisualPositionFrom(c,
                    tmp, javax.swing.text.Position.Bias.Forward, javax.swing.SwingConstants.EAST, null);
            if (nextVisualPosition>best && nextVisualPosition <= c.getDocument().getLength()){
                // We are in the collapsed fold, now try to find which position
                // is the best, whether foldEnd or foldStart
                tempRect = c.modelToView(nextVisualPosition);
                if (tempRect == null){
                    return nextVisualPosition;
                }
                int rightX = tempRect.x;
                int nextVisualPositionLeft = c.getUI().getNextVisualPositionFrom(c,
                        nextVisualPosition, javax.swing.text.Position.Bias.Backward, javax.swing.SwingConstants.WEST, null);
                tempRect = c.modelToView(nextVisualPositionLeft);
                if (tempRect == null){
                    return nextVisualPosition;
                }
                int leftX = tempRect.x;
                
                if (Math.abs(leftX - x) > Math.abs(rightX - x)){
                    return nextVisualPosition;
                } else {
                    return nextVisualPositionLeft;
                }
            }
        }
        
        return best;
    }

    /** Get start of the current word. If there are no more words till
    * the begining of the document, this method returns -1.
    * @param c text component to operate on
    * @param offset position in document from which the current line is determined
    */
    public static int getWordStart(JTextComponent c, int offset)
    throws BadLocationException {
        return getWordStart((BaseDocument)c.getDocument(), offset);
    }

    public static int getWordStart(BaseDocument doc, int offset)
    throws BadLocationException {
        return doc.find(new FinderFactory.PreviousWordBwdFinder(doc, false, true),
                        offset, 0);
    }

    public static int getWordEnd(JTextComponent c, int offset)
    throws BadLocationException {
        return getWordEnd((BaseDocument)c.getDocument(), offset);
    }

    public static int getWordEnd(BaseDocument doc, int offset)
    throws BadLocationException {
        int ret = doc.find(new FinderFactory.NextWordFwdFinder(doc, false, true),
                        offset, -1);
        return (ret > 0) ? ret : doc.getLength();
    }

    public static int getNextWord(JTextComponent c, int offset)
    throws BadLocationException {
        int nextWordOffset = getNextWord((BaseDocument)c.getDocument(), offset);
        int nextVisualPosition = -1;
        if (nextWordOffset > 0){
            nextVisualPosition = c.getUI().getNextVisualPositionFrom(c,
                    nextWordOffset - 1, Position.Bias.Forward, javax.swing.SwingConstants.EAST, null);
        }
        return (nextVisualPosition == -1) ? nextWordOffset : nextVisualPosition;
    }

    public static int getNextWord(BaseDocument doc, int offset)
    throws BadLocationException {
        Finder nextWordFinder = (Finder)doc.getProperty(EditorPreferencesKeys.NEXT_WORD_FINDER);
        offset = doc.find(nextWordFinder, offset, -1);
        if (offset < 0) {
            offset = doc.getLength();
        }
        return offset;
    }

    public static int getPreviousWord(JTextComponent c, int offset)
    throws BadLocationException {
        int prevWordOffset = getPreviousWord((BaseDocument)c.getDocument(), offset);
        int nextVisualPosition = c.getUI().getNextVisualPositionFrom(c,
                              prevWordOffset, Position.Bias.Forward, javax.swing.SwingConstants.WEST, null);
        if (nextVisualPosition == 0 && prevWordOffset == 0){
            return 0;
        }
        return (nextVisualPosition + 1 == prevWordOffset) ?  prevWordOffset : nextVisualPosition + 1;
    }

    public static int getPreviousWord(BaseDocument doc, int offset)
    throws BadLocationException {
        Finder prevWordFinder = (Finder)doc.getProperty(EditorPreferencesKeys.PREVIOUS_WORD_FINDER);
        offset = doc.find(prevWordFinder, offset, 0);
        if (offset < 0) {
            offset = 0;
        }
        return offset;
    }

    /** Get first white character in document in forward direction
    * @param doc document to operate on
    * @param offset position in document where to start searching
    * @return position of the first white character or -1
    */
    public static int getFirstWhiteFwd(BaseDocument doc, int offset)
    throws BadLocationException {
        return getFirstWhiteFwd(doc, offset, -1);
    }

    /** Get first white character in document in forward direction
    * @param doc document to operate on
    * @param offset position in document where to start searching
    * @param limitPos position in document (greater or equal than offset) where
    *   the search will stop reporting unsuccessful search by returning -1
    * @return position of the first non-white character or -1
    */
    public static int getFirstWhiteFwd(BaseDocument doc, int offset, int limitPos)
    throws BadLocationException {
        return doc.find(new FinderFactory.WhiteFwdFinder(doc), offset, limitPos);
    }

    /** Get first non-white character in document in forward direction
    * @param doc document to operate on
    * @param offset position in document where to start searching
    * @return position of the first non-white character or -1
    */
    public static int getFirstNonWhiteFwd(BaseDocument doc, int offset)
    throws BadLocationException {
        return getFirstNonWhiteFwd(doc, offset, -1);
    }

    /** Get first non-white character in document in forward direction
    * @param doc document to operate on
    * @param offset position in document where to start searching
    * @param limitPos position in document (greater or equal than offset) where
    *   the search will stop reporting unsuccessful search by returning -1
    * @return position of the first non-white character or -1
    */
    public static int getFirstNonWhiteFwd(BaseDocument doc, int offset, int limitPos)
    throws BadLocationException {
        return doc.find(new FinderFactory.NonWhiteFwdFinder(doc), offset, limitPos);
    }

    /** Get first white character in document in backward direction.
    * The character right before the character at position offset will
    * be searched as first.
    * @param doc document to operate on
    * @param offset position in document where to start searching
    * @return position of the first white character or -1
    */
    public static int getFirstWhiteBwd(BaseDocument doc, int offset)
    throws BadLocationException {
        return getFirstWhiteBwd(doc, offset, 0);
    }

    /** Get first white character in document in backward direction.
    * The character right before the character at position offset will
    * be searched as first.
    * @param doc document to operate on
    * @param offset position in document where to start searching
    * @param limitPos position in document (lower or equal than offset) where
    *   the search will stop reporting unsuccessful search by returning -1
    * @return position of the first white character or -1
    */
    public static int getFirstWhiteBwd(BaseDocument doc, int offset, int limitPos)
    throws BadLocationException {
        return doc.find(new FinderFactory.WhiteBwdFinder(doc), offset, limitPos);
    }

    /** Get first non-white character in document in backward direction.
    * The character right before the character at position offset will
    * be searched as first.
    * @param doc document to operate on
    * @param offset position in document where to start searching
    * @return position of the first non-white character or -1
    */
    public static int getFirstNonWhiteBwd(BaseDocument doc, int offset)
    throws BadLocationException {
        return getFirstNonWhiteBwd(doc, offset, 0);
    }

    /** Get first non-white character in document in backward direction.
    * The character right before the character at position offset will
    * be searched as first.
    * @param doc document to operate on
    * @param offset position in document where to start searching
    * @param limitPos position in document (lower or equal than offset) where
    *   the search will stop reporting unsuccessful search by returning -1
    * @return position of the first non-white character or -1
    */
    public static int getFirstNonWhiteBwd(BaseDocument doc, int offset, int limitPos)
    throws BadLocationException {
        return doc.find(new FinderFactory.NonWhiteBwdFinder(doc), offset, limitPos);
    }

    /** Return line offset (line number - 1) for some position in the document
    * @param doc document to operate on
    * @param offset position in document where to start searching
    */
    public static int getLineOffset(BaseDocument doc, int offset)
    throws BadLocationException {
        
        checkOffsetValid(offset, doc.getLength() + 1);

        Element lineRoot = doc.getParagraphElement(0).getParentElement();
        return lineRoot.getElementIndex(offset);
    }

    /** Return start offset of the line
    * @param lineIndex line index starting from 0
    * @return start position of the line or -1 if lineIndex was invalid
    */
    public static int getRowStartFromLineOffset(BaseDocument doc, int lineIndex) {
        Element lineRoot = doc.getParagraphElement(0).getParentElement();
        if (lineIndex < 0 || lineIndex >= lineRoot.getElementCount()) {
            return -1; // invalid line number

        } else {
            return lineRoot.getElement(lineIndex).getStartOffset();
        }
    }

    /** Return visual column (with expanded tabs) on the line.
    * @param doc document to operate on
    * @param offset position in document for which the visual column should be found
    * @return visual column on the line determined by position
    */
    public static int getVisualColumn(BaseDocument doc, int offset)
    throws BadLocationException {
        
        int docLen = doc.getLength();
        if (offset == docLen + 1) { // at ending extra '\n' => make docLen to proceed without BLE
            offset = docLen;
        }

        return doc.getVisColFromPos(offset);
    }

    /** Get the identifier around the given position or null if there's no identifier
    * @see getIdentifierBlock()
    */
    public static String getIdentifier(BaseDocument doc, int offset)
    throws BadLocationException {
        int[] blk = getIdentifierBlock(doc, offset);
        return (blk != null) ? doc.getText(blk[0], blk[1] - blk[0]) : null;
    }


    /** Get the identifier around the given position or null if there's no identifier
     * around the given position. The identifier is not verified against SyntaxSupport.isIdentifier().
     * @param c JTextComponent to work on
     * @param offset position in document - usually the caret.getDot()
     * @return the block (starting and ending position) enclosing the identifier
     * or null if no identifier was found
     */
    public static int[] getIdentifierBlock(JTextComponent c, int offset)
    throws BadLocationException {
        CharSequence id = null;
        int[] ret = null;
        Document doc = c.getDocument();
        int idStart = javax.swing.text.Utilities.getWordStart(c, offset);
        if (idStart >= 0) {
            int idEnd = javax.swing.text.Utilities.getWordEnd(c, idStart);
            if (idEnd >= 0) {
                id = DocumentUtilities.getText(doc, idStart, idEnd - idStart);
                ret = new int[] { idStart, idEnd };
                CharSequence trim = CharSequenceUtilities.trim(id);
                if (trim.length() == 0 || (trim.length() == 1 && !Character.isJavaIdentifierPart(trim.charAt(0)))) {
                    int prevWordStart = javax.swing.text.Utilities.getPreviousWord(c, offset);
                    if (offset == javax.swing.text.Utilities.getWordEnd(c,prevWordStart )){
                        ret = new int[] { prevWordStart, offset };
                    } else {
                        return null;
                    }
                } else if ((id != null) && (id.length() != 0)  && (CharSequenceUtilities.indexOf(id, '.') != -1)){ //NOI18N
                    int index = offset - idStart;
                    int begin = CharSequenceUtilities.lastIndexOf(id.subSequence(0, index), '.');
                    begin = (begin == -1) ? 0 : begin + 1; //first index after the dot, if exists
                    int end = CharSequenceUtilities.indexOf(id, '.', index);
                    end = (end == -1) ? id.length() : end;
                    ret = new int[] { idStart+begin, idStart+end };
                }
            }
        }
        return ret;
    }
    
    
    
    /** Get the identifier around the given position or null if there's no identifier
    * around the given position. The identifier must be
    * accepted by SyntaxSupport.isIdnetifier() otherwise null is returned.
    * @param doc document to work on
    * @param offset position in document - usually the caret.getDot()
    * @return the block (starting and ending position) enclosing the identifier
    *   or null if no identifier was found
    */
    public static int[] getIdentifierBlock(BaseDocument doc, int offset)
    throws BadLocationException {
        int[] ret = null;
        int idStart = getWordStart(doc, offset);
        if (idStart >= 0) {
            int idEnd = getWordEnd(doc, idStart);
            if (idEnd >= 0) {
                String id = doc.getText(idStart, idEnd - idStart);
                if (doc.getSyntaxSupport().isIdentifier(id)) {
                    ret = new int[] { idStart, idEnd };
                } else { // not identifier by syntax support
                    id = getWord(doc, offset); // try right at offset
                    if (doc.getSyntaxSupport().isIdentifier(id)) {
                        ret = new int[] { offset, offset + id.length() };
                    }
                }
            }
        }
        return ret;
    }

    
    /** Get the word around the given position .
     * @param c component to work with
     * @param offset position in document - usually the caret.getDot()
     * @return the word.
     */
    public static String getWord(JTextComponent c, int offset)
    throws BadLocationException {
        int[] blk = getIdentifierBlock(c, offset);
        Document doc = c.getDocument();
        return (blk != null) ? doc.getText(blk[0], blk[1] - blk[0]) : null;
    }
    
    
    /** Get the selection if there's any or get the identifier around
    * the position if there's no selection.
    * @param c component to work with
    * @param offset position in document - usually the caret.getDot()
    * @return the block (starting and ending position) enclosing the identifier
    *   or null if no identifier was found
    */
    public static int[] getSelectionOrIdentifierBlock(JTextComponent c, int offset)
    throws BadLocationException {
        Document doc = c.getDocument();
        Caret caret = c.getCaret();
        int[] ret;
        if (Utilities.isSelectionShowing(caret)) {
            ret = new int[] { c.getSelectionStart(), c.getSelectionEnd() }; 
        } else if (doc instanceof BaseDocument){
            ret = getIdentifierBlock((BaseDocument)doc, offset);
        } else {
            ret = getIdentifierBlock(c, offset);
        }
        return ret;
    }

    /** Get the selection or identifier at the current caret position
     * @see getSelectionOrIdentifierBlock(JTextComponent, int)
     */
    public static int[] getSelectionOrIdentifierBlock(JTextComponent c) {
        try {
            return getSelectionOrIdentifierBlock(c, c.getCaret().getDot());
        } catch (BadLocationException e) {
            return null;
        }
    }

    /** Get the identifier before the given position (ending at given offset)
    * or null if there's no identifier
    */
    public static String getIdentifierBefore(BaseDocument doc, int offset)
    throws BadLocationException {
        int wordStart = getWordStart(doc, offset);
        if (wordStart != -1) {
            String word = new String(doc.getChars(wordStart,
                                                  offset - wordStart), 0, offset - wordStart);
            if (doc.getSyntaxSupport().isIdentifier(word)) {
                return word;
            }
        }
        return null;
    }

    /** Get the selection if there's any or get the identifier around
    * the position if there's no selection.
    */
    public static String getSelectionOrIdentifier(JTextComponent c, int offset)
    throws BadLocationException {
        Document doc = c.getDocument();
        Caret caret = c.getCaret();
        String ret;
        if (Utilities.isSelectionShowing(caret)) {
            ret = c.getSelectedText();
	    if (ret != null) return ret;
        } 
	if (doc instanceof BaseDocument){
	    ret = getIdentifier((BaseDocument) doc, offset);
        } else {
	    ret = getWord(c, offset);
	}
        return ret;
    }

    /** Get the selection or identifier at the current caret position */
    public static String getSelectionOrIdentifier(JTextComponent c) {
        try {
            return getSelectionOrIdentifier(c, c.getCaret().getDot());
        } catch (BadLocationException e) {
            return null;
        }
    }

    /** Get the word at given position.
    */
    public static String getWord(BaseDocument doc, int offset)
    throws BadLocationException {
        int wordEnd = getWordEnd(doc, offset);
        if (wordEnd != -1) {
            return new String(doc.getChars(offset, wordEnd - offset), 0,
                              wordEnd - offset);
        }
        return null;
    }


    /** Change the case for specified part of document
    * @param doc document to operate on
    * @param offset position in document determines the changed area begining
    * @param len number of chars to change
    * @param type either CASE_CAPITAL, CASE_SMALL or CASE_SWITCH
    */
    public static boolean changeCase(final BaseDocument doc, final int offset, final int len, final int type)
    throws BadLocationException {
        final char[] orig = doc.getChars(offset, len);
        final char[] changed = (char[])orig.clone();
        for (int i = 0; i < orig.length; i++) {
            switch (type) {
            case CASE_UPPER:
                changed[i] = Character.toUpperCase(orig[i]);
                break;
            case CASE_LOWER:
                changed[i] = Character.toLowerCase(orig[i]);
                break;
            case CASE_SWITCH:
                if (Character.isUpperCase(orig[i])) {
                    changed[i] = Character.toLowerCase(orig[i]);
                } else if (Character.isLowerCase(orig[i])) {
                    changed[i] = Character.toUpperCase(orig[i]);
                }
                break;
            }
        }
        // check chars for difference and possibly change document
        for (int i = 0; i < orig.length; i++) {
            if (orig[i] != changed[i]) {
                final BadLocationException[] badLocationExceptions = new BadLocationException [1];
                doc.runAtomicAsUser (new Runnable () {
                    public @Override void run () {
                        try {
                            doc.remove(offset, orig.length);
                            doc.insertString(offset, new String(changed), null);
                        } catch (BadLocationException ex) {
                            badLocationExceptions [0] = ex;
                        }
                    }
                });
                if (badLocationExceptions [0] != null)
                    throw badLocationExceptions [0];
                return true; // changed
            }
        }
        return false;
    }

    /** Tests whether the line contains no characters except the ending new-line.
    * @param doc document to operate on
    * @param offset position anywhere on the tested line
    * @return whether the line is empty or not
    */
    public static boolean isRowEmpty(BaseDocument doc, int offset)
    throws BadLocationException {
        Element lineElement = doc.getParagraphElement(offset);
        return (lineElement.getStartOffset() + 1 == lineElement.getEndOffset());
    }

    public static int getFirstNonEmptyRow(BaseDocument doc, int offset, boolean downDir)
    throws BadLocationException {
        while (offset != -1 && isRowEmpty(doc, offset)) {
            offset = getRowStart(doc, offset, downDir ? +1 : -1);
        }
        return offset;
    }

    /** Tests whether the line contains only whitespace characters.
    * @param doc document to operate on
    * @param offset position anywhere on the tested line
    * @return whether the line is empty or not
    */
    public static boolean isRowWhite(BaseDocument doc, int offset)
    throws BadLocationException {
        Element lineElement = doc.getParagraphElement(offset);
        offset = doc.find(new FinderFactory.NonWhiteFwdFinder(doc),
              lineElement.getStartOffset(), lineElement.getEndOffset() - 1);
        return (offset == -1);
    }

    public static int getFirstNonWhiteRow(BaseDocument doc, int offset, boolean downDir)
    throws BadLocationException {
        if (isRowWhite(doc, offset)) {
            if (downDir) { // search down for non-white line
                offset = getFirstNonWhiteFwd(doc, offset);
            } else { // search up for non-white line
                offset = getFirstNonWhiteBwd(doc, offset);
            }
        }
        return offset;
    }

    /**
     * Reformat a block of code.
     * <br/>
     * The document should not be locked prior entering of this method.
     * <br/>
     * The method should be called from AWT thread so that the given offsets are more stable.
     * 
     * @param doc document to work with
     * @param startOffset offset at which the formatting starts
     * @param endOffset offset at which the formatting ends
     * @return length of the reformatted code
     */
    public static int reformat (final BaseDocument doc, final int startOffset, final int endOffset)
    throws BadLocationException {
        final Formatter formatter = doc.getFormatter();
        formatter.reformatLock();
        try {
            final Object[] result = new Object [1];
            doc.runAtomicAsUser (new Runnable () {
                public @Override void run () {
                    try {
                        result [0] = formatter.reformat (doc, startOffset, endOffset);
                    } catch (BadLocationException ex) {
                        result [0] = ex;
                    }
                }
            });
            if (result [0] instanceof BadLocationException)
                throw (BadLocationException) result [0];
            return (Integer) result [0];
        } finally {
            formatter.reformatUnlock();
        }
    }

    /**
     * Reformat the line around the given position.
     * <br/>
     * The document should not be locked prior entering of this method.
     * <br/>
     * The method should be called from AWT thread so that the given offsets are more stable.
     * 
     */
    public static void reformatLine(BaseDocument doc, int pos)
    throws BadLocationException {
        int lineStart = getRowStart(doc, pos);
        int lineEnd = getRowEnd(doc, pos);
        reformat(doc, lineStart, lineEnd);
    }

    /** Count of rows between these two positions */
    public static int getRowCount(BaseDocument doc, int startPos, int endPos)
    throws BadLocationException {
        if (startPos > endPos) {
            return 0;
        }
        Element lineRoot = doc.getParagraphElement(0).getParentElement();
        return lineRoot.getElementIndex(endPos) - lineRoot.getElementIndex(startPos) + 1;
    }

    /** Get the total count of lines in the document */
    public static int getRowCount(BaseDocument doc) {
        return doc.getParagraphElement(0).getParentElement().getElementCount();
    }

    /** @deprecated
     * @see Formatter.insertTabString()
     */
    public static String getTabInsertString(BaseDocument doc, int offset)
    throws BadLocationException {
        int col = getVisualColumn(doc, offset);
        Formatter f = doc.getFormatter();
        boolean expandTabs = f.expandTabs();
        if (expandTabs) {
            int spacesPerTab = f.getSpacesPerTab();
            int len = (col + spacesPerTab) / spacesPerTab * spacesPerTab - col;
            return new String(Analyzer.getSpacesBuffer(len), 0, len);
        } else { // insert pure tab
            return "\t"; // NOI18N
        }
    }

    /** Get the visual column corresponding to the position after pressing
     * the TAB key.
     * @param doc document to work with
     * @param offset position at which the TAB was pressed
     */
    public static int getNextTabColumn(BaseDocument doc, int offset)
    throws BadLocationException {
        int col = getVisualColumn(doc, offset);
        int tabSize = doc.getFormatter().getSpacesPerTab();
        return (col + tabSize) / tabSize * tabSize;
    }

    public static void setStatusText(JTextComponent c, String text) {
        EditorUI eui = getEditorUI(c);
        StatusBar sb = eui == null ? null : eui.getStatusBar();
        if (sb != null) {
            sb.setText(StatusBar.CELL_MAIN, text);
        }
    }

    public static void setStatusText(JTextComponent c, String text, int importance) {
        EditorUI eui = getEditorUI(c);
        StatusBar sb = eui == null ? null : eui.getStatusBar();
        if (sb != null) {
            sb.setText(text, importance);
        }
    }

    public static void setStatusText(JTextComponent c, String text,
                                     Coloring extraColoring) {
        EditorUI eui = getEditorUI(c);
        StatusBar sb = eui == null ? null : eui.getStatusBar();
        if (sb != null) {
            sb.setText(StatusBar.CELL_MAIN, text, extraColoring);
        }
    }

    public static void setStatusBoldText(JTextComponent c, String text) {
        EditorUI eui = getEditorUI(c);
        StatusBar sb = eui == null ? null : eui.getStatusBar();
        if (sb != null) {
            sb.setBoldText(StatusBar.CELL_MAIN, text);
        }
    }

    public static String getStatusText(JTextComponent c) {
        EditorUI eui = getEditorUI(c);
        StatusBar sb = eui == null ? null : eui.getStatusBar();
        return (sb != null) ? sb.getText(StatusBar.CELL_MAIN) : null;
    }

    public static void clearStatusText(JTextComponent c) {
        setStatusText(c, ""); // NOI18N
    }

    public static void insertMark(BaseDocument doc, Mark mark, int offset)
    throws BadLocationException, InvalidMarkException {
        mark.insert(doc, offset);
    }

    public static void moveMark(BaseDocument doc, Mark mark, int newOffset)
    throws BadLocationException, InvalidMarkException {
        mark.move(doc, newOffset);
    }

    public static void returnFocus() {
         JTextComponent c = getLastActiveComponent();
         if (c != null) {
             requestFocus(c);
         }
    }

    public static void requestFocus(JTextComponent c) {
        if (c != null) {
            if (!ImplementationProvider.getDefault().activateComponent(c)) {
                Frame f = EditorUI.getParentFrame(c);
                if (f != null) {
                    f.requestFocus();
                }
                c.requestFocus();
            }
        }
    }

    public static void runInEventDispatchThread(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    public static String debugPosition(BaseDocument doc, int offset) {
        return debugPosition(doc, offset, ":");
    }

    /**
     * @param doc non-null document.
     * @param offset offset to translate to line and column info.
     * @param separator non-null separator of line and column info (either single charater or a string).s
     * @return non-null line and column info for the given offset.
     * @since 1.40
     */
    public static String debugPosition(BaseDocument doc, int offset, String separator) {
        String ret;

        if (offset >= 0) {
            try {
                int line = getLineOffset(doc, offset) + 1;
                int col = getVisualColumn(doc, offset) + 1;
                ret = String.valueOf(line) + separator + String.valueOf(col); // NOI18N
            } catch (BadLocationException e) {
                ret = NbBundle.getBundle(BaseKit.class).getString( WRONG_POSITION_LOCALE )
                      + ' ' + offset + " > " + doc.getLength(); // NOI18N
            }
        } else {
            ret = String.valueOf(offset);
        }

        return ret;
    }
    
    public static String offsetToLineColumnString(BaseDocument doc, int offset) {
        return String.valueOf(offset) + "[" + debugPosition(doc, offset) + "]"; // NOI18N
    }

    /** Display the identity of the document together with the title property
     * and stream-description property.
     */
    public static String debugDocument(Document doc) {
        return "<" + System.identityHashCode(doc) // NOI18N
            + ", title='" + doc.getProperty(Document.TitleProperty)
            + "', stream='" + doc.getProperty(Document.StreamDescriptionProperty)
            + ", " + doc.toString() + ">"; // NOI18N
    }

    public static void performAction(Action a, ActionEvent evt, JTextComponent target) {
        if (a instanceof BaseAction) {
            ((BaseAction)a).actionPerformed(evt, target);
        } else {
            a.actionPerformed(evt);
        }
    }

    /** Returns last activated component. If the component was closed, 
     *  then previous component is returned */
    public static JTextComponent getLastActiveComponent() {
        return EditorRegistry.lastFocusedComponent();
    }
    
    /**
     * Fetches the text component that currently has focus. It delegates to 
     * TextAction.getFocusedComponent().
     * @return the component
     */
    public static JTextComponent getFocusedComponent() {
        /** Fake action for getting the focused component */
        class FocusedComponentAction extends TextAction {
            
            FocusedComponentAction() {
                super("focused-component"); // NOI18N
            }
            
            /** adding this method because of protected final getFocusedComponent */
            JTextComponent getFocusedComponent2() {
                return getFocusedComponent();
            }
            
            public @Override void actionPerformed(ActionEvent evt){}
        }
        
        if (focusedComponentAction == null) {
            focusedComponentAction = new FocusedComponentAction();
        }
        
        return ((FocusedComponentAction)focusedComponentAction).getFocusedComponent2();
    }

    /** Helper method to obtain instance of EditorUI (extended UI)
     * from the existing JTextComponent.
     * It doesn't require any document locking.
     * @param target JTextComponent for which the extended UI should be obtained
     * @return extended ui instance or null if the component.getUI()
     *   does not return BaseTextUI instance.
     */
    public static EditorUI getEditorUI(JTextComponent target) {
        TextUI ui = target.getUI();
        return (ui instanceof BaseTextUI) 
            ? ((BaseTextUI)ui).getEditorUI()
            : null;
    }

    /** Helper method to obtain instance of editor kit from existing JTextComponent.
    * If the kit of the component is not an instance
    * of the <tt>org.netbeans.editor.BaseKit</tt> the method returns null.
    * The method doesn't require any document locking.
    * @param target JTextComponent for which the editor kit should be obtained
    * @return BaseKit instance or null
    */
    public static BaseKit getKit(JTextComponent target) {
        if (target == null) return null; // #19574
        EditorKit ekit = target.getUI().getEditorKit(target);
        return (ekit instanceof BaseKit) ? (BaseKit)ekit : null;
    }

    /** 
     * Gets the class of an editor kit installed in <code>JTextComponent</code>.
     * The method doesn't require any document locking.
     * 
     * <div class="nonnormative">
     * <p>WARNING: The implementation class of an editor kit is most likely
     * not what you want. Please see {@link BaseKit#getKit(Class)} for more
     * details.
     * 
     * <p>Unfortunatelly, there are still places in editor libraries where
     * an editor kit class is required.
     * One of them is the editor settings infrastructure built around the
     * <code>Settings</code> class. So, if you really need it go ahead and use it,
     * there is nothing wrong with the method itself.
     * </div>
     * 
     * @param target The <code>JTextComponent</code> to get the kit class for.
     *   Can be <code>null</code>.
     * @return The implementation class of the editor kit or <code>null</code>
     *   if the <code>target</code> is <code>null</code>.
     */
    public static Class getKitClass(JTextComponent target) {
        EditorKit kit = (target != null) ? target.getUI().getEditorKit(target) : null;
        return (kit != null) ? kit.getClass() : null;
    }

    /** Helper method to obtain instance of BaseDocument from JTextComponent.
    * If the document of the component is not an instance
    * of the <tt>org.netbeans.editor.BaseDocument</tt> the method returns null.
    * The method doesn't require any document locking.
    * @param target JTextComponent for which the document should be obtained
    * @return BaseDocument instance or null
    */
    public static BaseDocument getDocument(JTextComponent target) {
        Document doc = target.getDocument();
        return (doc instanceof BaseDocument) ? (BaseDocument)doc : null;
    }

    /** Get the syntax-support class that belongs to the document of the given
    * component. Besides using directly this method, the <tt>SyntaxSupport</tt>
    * can be obtained by calling <tt>doc.getSyntaxSupport()</tt>.
    * The method can return null in case the document is not
    * an instance of the BaseDocument.
    * The method doesn't require any document locking.
    * @param target JTextComponent for which the syntax-support should be obtained
    * @return SyntaxSupport instance or null
    */
    public static SyntaxSupport getSyntaxSupport(JTextComponent target) {
        Document doc = target.getDocument();
        return (doc instanceof BaseDocument) ? ((BaseDocument)doc).getSyntaxSupport() : null;
    }

    /**
     * Get first view in the hierarchy that is an instance of the given class.
     * It allows to skip various wrapper-views around the doc-view that holds
     * the child views for the lines.
     *
     * @param component component from which the root view is fetched.
     * @param rootViewClass class of the view to return.
     * @return view being instance of the requested class or null if there
     *  is not one.
     */
    public static View getRootView(JTextComponent component, Class rootViewClass) {
        View view = null;
        TextUI textUI = component.getUI();
        if (textUI != null) {
            view = textUI.getRootView(component);
            while (view != null && !rootViewClass.isInstance(view)
                && view.getViewCount() == 1 // must be wrapper view
            ) {
                view = view.getView(0); // get the only child
            }
        }
        
        return view;
    }
    
    /**
     * Get the view that covers the whole area of the document
     * and holds a child view for each line in the document
     * (or for a bunch of lines in case there is a code folding present).
     */
    public static View getDocumentView(JTextComponent component) {
        return getRootView(component, DrawEngineDocView.class);
    }

    /**
     * Creates nice textual description of sequence of KeyStrokes. Usable for
     * displaying MultiKeyBindings. The keyStrokes are delimited by space.
     * @param Array of KeyStrokes representing the actual sequence.
     * @return String describing the KeyStroke sequence.
     */
    public static String keySequenceToString( KeyStroke[] seq ) {
        StringBuilder sb = new StringBuilder();
        for( int i=0; i<seq.length; i++ ) {
            if( i>0 ) sb.append( ' ' );  // NOI18N
            sb.append( keyStrokeToString( seq[i] ) );
        }
        return sb.toString();
    }

    /**
     * Creates nice textual representation of KeyStroke.
     * Modifiers and an actual key label are concated by plus signs
     * @param the KeyStroke to get description of
     * @return String describing the KeyStroke
     */
    public static String keyStrokeToString( KeyStroke stroke ) {
        String modifText = KeyEvent.getKeyModifiersText( stroke.getModifiers() );
        String keyText = (stroke.getKeyCode() == KeyEvent.VK_UNDEFINED) ? 
            String.valueOf(stroke.getKeyChar()) : getKeyText(stroke.getKeyCode());
        if( modifText.length() > 0 ) return modifText + '+' + keyText;
        else return keyText;
    }
    
    /** @return slight modification of what KeyEvent.getKeyText() returns.
     *  The numpad Left, Right, Down, Up get extra result.
     */
    private static String getKeyText(int keyCode) {
        String ret = KeyEvent.getKeyText(keyCode);
        if (ret != null) {
            switch (keyCode) {
                case KeyEvent.VK_KP_DOWN:
                    ret = prefixNumpad(ret, KeyEvent.VK_DOWN);
                    break;
                case KeyEvent.VK_KP_LEFT:
                    ret = prefixNumpad(ret, KeyEvent.VK_LEFT);
                    break;
                case KeyEvent.VK_KP_RIGHT:
                    ret = prefixNumpad(ret, KeyEvent.VK_RIGHT);
                    break;
                case KeyEvent.VK_KP_UP:
                    ret = prefixNumpad(ret, KeyEvent.VK_UP);
                    break;
            }
        }
        return ret;
    }
    
    private static String prefixNumpad(String key, int testKeyCode) {
        if (key.equals(KeyEvent.getKeyText(testKeyCode))) {
            key = NbBundle.getBundle(BaseKit.class).getString("key-prefix-numpad") + key;
        }
        return key;
    }

    private static void checkOffsetValid(Document doc, int offset) throws BadLocationException {
        checkOffsetValid(offset, doc.getLength());
    }

    private static void checkOffsetValid(int offset, int limitOffset) throws BadLocationException {
        if (offset < 0 || offset > limitOffset) { 
            throw new BadLocationException("Invalid offset=" + offset // NOI18N
                + " not within <0, " + limitOffset + ">", // NOI18N
                offset);
        }
    }

    /** 
     * Writes a <code>Throwable</code> to a log file.
     * 
     * <p class="nonnormative">The method is internally using 
     * <code>org.netbeans.editor</code> logger and <code>Level.INFO</code>.
     * 
     * @param t The exception that will be logged.
     * @deprecated Use java.util.logging.Logger instead with the proper name,
     * log level and message.
     */
    public static void annotateLoggable(Throwable t) {
        Logger.getLogger("org.netbeans.editor").log(Level.INFO, null, t); //NOI18N
    }
    
    /**
     * Check whether caret's selection is visible and there is at least
     * one selected character showing.
     * 
     * @param caret non-null caret.
     * @return true if selection is visible and there is at least one selected character.
     */
    public static boolean isSelectionShowing(Caret caret) {
        return caret.isSelectionVisible() && caret.getDot() != caret.getMark();
    }
    
    /**
     * @see isSelectionShowing(Caret)
     * @param component non-null component.
     * @return if selection is showing for component's caret.
     */
    public static boolean isSelectionShowing(JTextComponent component) {
        Caret caret = component.getCaret();
        return (caret != null) && isSelectionShowing(caret);
    }
     
    /**
     * Gets the mime type of a document. If the mime type can't be determined
     * this method will return <code>null</code>. This method should work reliably
     * for Netbeans documents that have their mime type stored in a special
     * property. For any other documents it will probably just return <code>null</code>.
     * 
     * @param doc The document to get the mime type for.
     * 
     * @return The mime type of the document or <code>null</code>.
     * @see NbEditorDocument#MIME_TYPE_PROP
     */
    /* package */ static String getMimeType(Document doc) {
        return (String)doc.getProperty(BaseDocument.MIME_TYPE_PROP); //NOI18N
    }

    /**
     * Gets the mime type of a document in <code>JTextComponent</code>. If
     * the mime type can't be determined this method will return <code>null</code>.
     * It tries to determine the document's mime type first and if that does not
     * work it uses mime type from the <code>EditorKit</code> attached to the
     * component.
     * 
     * @param component The component to get the mime type for.
     * 
     * @return The mime type of a document opened in the component or <code>null</code>.
     */
    /* package */ static String getMimeType(JTextComponent component) {
        Document doc = component.getDocument();
        String mimeType = getMimeType(doc);
        if (mimeType == null) {
            EditorKit kit = component.getUI().getEditorKit(component);
            if (kit != null) {
                mimeType = kit.getContentType();
            }
        }
        return mimeType;
    }

    //#182648: JTextComponent.modelToView returns a Rectangle, which contains integer positions,
    //but the views are layed-out using doubles. The rounding (truncating) truncating errors case problems
    //with navigation (up/down, end line). Below are methods that return exact double-based rectangle
    //for the given position, and also double-based viewToModel method:
    static Rectangle2D modelToView(JTextComponent tc, int pos) throws BadLocationException {
	return modelToView(tc, pos, Position.Bias.Forward);
    }

    static Rectangle2D modelToView(JTextComponent tc, int pos, Position.Bias bias) throws BadLocationException {
	Document doc = tc.getDocument();
	if (doc instanceof AbstractDocument) {
	    ((AbstractDocument)doc).readLock();
	}
	try {
	    Rectangle alloc = getVisibleEditorRect(tc);
	    if (alloc != null) {
                View rootView = tc.getUI().getRootView(tc);
		rootView.setSize(alloc.width, alloc.height);
		Shape s = rootView.modelToView(pos, alloc, bias);
		if (s != null) {
		  return s.getBounds2D();
		}
	    }
	} finally {
	    if (doc instanceof AbstractDocument) {
		((AbstractDocument)doc).readUnlock();
	    }
	}
	return null;
    }

    private static Position.Bias[] discardBias = new Position.Bias[1];
    static int viewToModel(JTextComponent tc, double x, double y) {
	return viewToModel(tc, x, y, discardBias);
    }

    static int viewToModel(JTextComponent tc, double x, double y, Position.Bias[] biasReturn) {
	int offs = -1;
	Document doc = tc.getDocument();
	if (doc instanceof AbstractDocument) {
	    ((AbstractDocument)doc).readLock();
	}
	try {
	    Rectangle alloc = getVisibleEditorRect(tc);
	    if (alloc != null) {
                View rootView = tc.getUI().getRootView(tc);
		rootView.setSize(alloc.width, alloc.height);
		offs = rootView.viewToModel((float) x, (float) y, alloc, biasReturn);
	    }
	} finally {
	    if (doc instanceof AbstractDocument) {
		((AbstractDocument)doc).readUnlock();
	    }
	}
        return offs;
    }
    
    private static Rectangle getVisibleEditorRect(JTextComponent tc) {
	Rectangle alloc = tc.getBounds();
	if ((alloc.width > 0) && (alloc.height > 0)) {
	    alloc.x = alloc.y = 0;
	    Insets insets = tc.getInsets();
	    alloc.x += insets.left;
	    alloc.y += insets.top;
	    alloc.width -= insets.left + insets.right;
	    alloc.height -= insets.top + insets.bottom;
	    return alloc;
	}
	return null;
    }
}
