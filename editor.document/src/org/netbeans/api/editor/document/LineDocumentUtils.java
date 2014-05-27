/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.api.editor.document;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Segment;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.editor.document.BaseFinderFactory;
import org.netbeans.modules.editor.document.DocumentServices;
import org.netbeans.modules.editor.document.implspi.CharClassifier;
import org.netbeans.modules.editor.lib2.AcceptorFactory;
import org.netbeans.modules.editor.lib2.DocumentPreferencesKeys;
import org.netbeans.spi.editor.document.DocumentFactory;
import org.openide.util.Lookup;
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
* The utilities were moved from the former Editor Library 2's Utilities
* and work with {@link LineDocument} only. The methods work only with document
* data. Utilities that connect document with the UI elements (Swing) can be
* still found in Editor Library (2).
*
* @author Miloslav Metelka
* @version 0.10
*/

public final class LineDocumentUtils {

    private static final String WRONG_POSITION_LOCALE = "wrong_position"; // NOI18N

//    /** Switch the case to capital letters. Used in changeCase() */
//    public static final int CASE_UPPER = 0;
//
//    /** Switch the case to small letters. Used in changeCase() */
//    public static final int CASE_LOWER = 1;
//
//    /** Switch the case to reverse. Used in changeCase() */
//    public static final int CASE_SWITCH = 2;
//    
    private LineDocumentUtils() {
        // instantiation has no sense
    }

    /** Get the starting position of the row.
    * @param doc document to operate on
    * @param offset position in document where to start searching
    * @return position of the start of the row or -1 for invalid position
    */
    public static int getRowStart(LineDocument doc, int offset)
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
    public static int getRowStart(LineDocument doc, int offset, int lineShift)
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
    public static int getRowFirstNonWhite(LineDocument doc, int offset)
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
    public static int getRowLastNonWhite(LineDocument doc, int offset)
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
    public static int getRowIndent(LineDocument doc, int offset)
    throws BadLocationException {
        offset = getRowFirstNonWhite(doc, offset);
        if (offset == -1) {
            return -1;
        }
        return doc.getVisColFromPos(offset);
    }
    */

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
    public static int getRowIndent(LineDocument doc, int offset, boolean downDir)
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
    */

    public static int getRowEnd(LineDocument doc, int offset)
    throws BadLocationException {
        checkOffsetValid(doc, offset);

        return doc.getParagraphElement(offset).getEndOffset() - 1;
    }
    
    public static int getWordStart(LineDocument doc, int offset)
    throws BadLocationException {
        return doc.find(new BaseFinderFactory.PreviousWordBwdFinder(getClassifier(doc), false, true),
                        offset, 0);
    }

    public static int getWordEnd(LineDocument doc, int offset)
    throws BadLocationException {
        int ret = doc.find(new BaseFinderFactory.NextWordFwdFinder(getClassifier(doc), false, true),
                        offset, -1);
        return (ret > 0) ? ret : doc.getLength();
    }

    public static int getNextWord(LineDocument doc, int offset)
    throws BadLocationException {
        Finder nextWordFinder = (Finder)doc.getProperty(DocumentPreferencesKeys.NEXT_WORD_FINDER);
        offset = doc.find(nextWordFinder, offset, -1);
        if (offset < 0) {
            offset = doc.getLength();
        }
        return offset;
    }

    public static int getPreviousWord(LineDocument doc, int offset)
    throws BadLocationException {
        Finder prevWordFinder = (Finder)doc.getProperty(DocumentPreferencesKeys.PREVIOUS_WORD_FINDER);
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
    public static int getFirstWhiteFwd(LineDocument doc, int offset)
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
    public static int getFirstWhiteFwd(LineDocument doc, int offset, int limitPos)
    throws BadLocationException {
        return doc.find(new BaseFinderFactory.WhiteFwdFinder(getClassifier(doc)), offset, limitPos);
    }

    /** Get first non-white character in document in forward direction
    * @param doc document to operate on
    * @param offset position in document where to start searching
    * @return position of the first non-white character or -1
    */
    public static int getFirstNonWhiteFwd(LineDocument doc, int offset)
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
    public static int getFirstNonWhiteFwd(LineDocument doc, int offset, int limitPos)
    throws BadLocationException {
        return doc.find(new BaseFinderFactory.NonWhiteFwdFinder(getClassifier(doc)), offset, limitPos);
    }
    
    private static CharClassifier getClassifier(Document doc) {
        return as(doc, CharClassifier.class);
    }
    
    private static final CharClassifier DEFAULT_CLASSIFIER = new CharClassifier() {

        @Override
        public boolean isIdentifierPart(char ch) {
            return AcceptorFactory.LETTER_DIGIT.accept(ch);
        }

        @Override
        public boolean isWhitespace(char ch) {
            return AcceptorFactory.WHITESPACE.accept(ch);
        }
        
    };

    /** Get first white character in document in backward direction.
    * The character right before the character at position offset will
    * be searched as first.
    * @param doc document to operate on
    * @param offset position in document where to start searching
    * @return position of the first white character or -1
    */
    public static int getFirstWhiteBwd(LineDocument doc, int offset)
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
    public static int getFirstWhiteBwd(LineDocument doc, int offset, int limitPos)
    throws BadLocationException {
        return doc.find(new BaseFinderFactory.WhiteBwdFinder(getClassifier(doc)), offset, limitPos);
    }

    /** Get first non-white character in document in backward direction.
    * The character right before the character at position offset will
    * be searched as first.
    * @param doc document to operate on
    * @param offset position in document where to start searching
    * @return position of the first non-white character or -1
    */
    public static int getFirstNonWhiteBwd(LineDocument doc, int offset)
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
    public static int getFirstNonWhiteBwd(LineDocument doc, int offset, int limitPos)
    throws BadLocationException {
        return doc.find(new BaseFinderFactory.NonWhiteBwdFinder(getClassifier(doc)), offset, limitPos);
    }

    /** Return line offset (line number - 1) for some position in the document
    * @param doc document to operate on
    * @param offset position in document where to start searching
    */
    public static int getLineOffset(LineDocument doc, int offset)
    throws BadLocationException {
        
        checkOffsetValid(doc, offset);

        Element lineRoot = doc.getParagraphElement(0).getParentElement();
        return lineRoot.getElementIndex(offset);
    }

    /** Return start offset of the line
    * @param lineIndex line index starting from 0
    * @return start position of the line or -1 if lineIndex was invalid
    */
    public static int getRowStartFromLineOffset(LineDocument doc, int lineIndex) {
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
    public static int getVisualColumn(LineDocument doc, int offset)
    throws BadLocationException {
        
        int docLen = doc.getLength();
        if (offset == docLen + 1) { // at ending extra '\n' => make docLen to proceed without BLE
            offset = docLen;
        }

        return doc.getVisColFromPos(offset);
    }
    */

    /** Get the word at given position.
    */
    public static String getWord(LineDocument doc, int offset)
    throws BadLocationException {
        int wordEnd = getWordEnd(doc, offset);
        if (wordEnd != -1) {
            Segment s = new Segment();
            doc.getText(offset, wordEnd - offset, s);
            return new String(s.array, 0, wordEnd - offset);
        }
        return null;
    }

    /** Tests whether the line contains no characters except the ending new-line.
    * @param doc document to operate on
    * @param offset position anywhere on the tested line
    * @return whether the line is empty or not
    */
    public static boolean isRowEmpty(LineDocument doc, int offset)
    throws BadLocationException {
        Element lineElement = doc.getParagraphElement(offset);
        return (lineElement.getStartOffset() + 1 == lineElement.getEndOffset());
    }

    public static int getFirstNonEmptyRow(LineDocument doc, int offset, boolean downDir)
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
    public static boolean isRowWhite(LineDocument doc, int offset)
    throws BadLocationException {
        Element lineElement = doc.getParagraphElement(offset);
        offset = doc.find(new BaseFinderFactory.NonWhiteFwdFinder(getClassifier(doc)),
              lineElement.getStartOffset(), lineElement.getEndOffset() - 1);
        return (offset == -1);
    }

    public static int getFirstNonWhiteRow(LineDocument doc, int offset, boolean downDir)
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

    /** Count of rows between these two positions */
    public static int getRowCount(LineDocument doc, int startPos, int endPos)
    throws BadLocationException {
        if (startPos > endPos) {
            return 0;
        }
        Element lineRoot = doc.getParagraphElement(0).getParentElement();
        return lineRoot.getElementIndex(endPos) - lineRoot.getElementIndex(startPos) + 1;
    }

    /** Get the total count of lines in the document */
    public static int getRowCount(LineDocument doc) {
        return doc.getParagraphElement(0).getParentElement().getElementCount();
    }

    /** Get the visual column corresponding to the position after pressing
     * the TAB key.
     * @param doc document to work with
     * @param offset position at which the TAB was pressed
    public static int getNextTabColumn(LineDocument doc, int offset)
    throws BadLocationException {
        int col = getVisualColumn(doc, offset);
        // FIXME - consult CodeStylePreferences
        Preferences prefs = CodeStylePreferences.get(doc).getPreferences();
        int tabSize = prefs.getInt(DocumentPreferencesKeys.SPACES_PER_TAB, DocumentPreferencesDefaults.defaultSpacesPerTab);
        int tabSize = DocumentPreferencesDefaults.defaultSpacesPerTab;
        return tabSize <= 0 ? col : (col + tabSize) / tabSize * tabSize;
    }
        */

    public static String debugPosition(LineDocument doc, int offset) {
        return debugPosition(doc, offset, ":");
    }

    /**
     * @param doc non-null document.
     * @param offset offset to translate to line and column info.
     * @param separator non-null separator of line and column info (either single charater or a string).s
     * @return non-null line and column info for the given offset.
     * @since 1.40
     */
    public static String debugPosition(LineDocument doc, int offset, String separator) {
        String ret;

        if (offset >= 0) {
            try {
                int line = getLineOffset(doc, offset) + 1;
                int col = offset - getRowStart(doc, offset) + 1;
                ret = String.valueOf(line) + separator + String.valueOf(col); // NOI18N
            } catch (BadLocationException e) {
                ret = NbBundle.getBundle(LineDocumentUtils.class).getString( WRONG_POSITION_LOCALE )
                      + ' ' + offset + " > " + doc.getLength(); // NOI18N
            }
        } else {
            ret = String.valueOf(offset);
        }

        return ret;
    }
    
    public static String offsetToLineColumnString(LineDocument doc, int offset) {
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

    private static void checkOffsetValid(Document doc, int offset) throws BadLocationException {
        checkOffsetValid(offset, doc.getLength() + 1);
    }

    private static void checkOffsetValid(int offset, int limitOffset) throws BadLocationException {
        if (offset < 0 || offset > limitOffset) { 
            throw new BadLocationException("Invalid offset=" + offset // NOI18N
                + " not within <0, " + limitOffset + ">", // NOI18N
                offset);
        }
    }
    
    private static final Object NOT_FOUND = new Object();
    
    /**
     * Locates the appropriate service for the document. May return {@code null}
     * if the interface/service is not supported by the document.
     * <p/>
     * For example, if a code needs to perform an atomic action on the document,
     * it can do so as follows:
     * <code><pre>
     * Document d = ... ; // some parameter ?
     * AtomicLockDocument ald = LineDocumentUtils.as(d, AtomicLockDocument.class); // obtain the optional interface
     * Runnable r = new Runnable() {
     *      public void run() { /* ... the code to execute ... * / }
     * };
     * 
     * if (ald != null) {
     *    ald.runAtomic(r);
     * } else {
     *    r.run();
     * }
     * 
     * </pre></code>
     * @param <T> service type
     * @param d the document
     * @param documentService the service interface
     * @return the service implementation or {@code null} if the service is not available
     */
    public static @CheckForNull <T> T as(@NullAllowed Document d, Class<T> documentService) {
        if (d == null) {
            return null;
        }
        return as(d, documentService, false);
    }
    
    private static class V<T> {
        final T delegate;

        public V(T delegate) {
            this.delegate = delegate;
        }
        
    }
    
    /**
     * Locates the appropriate service for the document. 
     * A fallback (dummy) implementation may be returned if the document does not
     * support the service natively. An exception will be thrown
     * if the stub is not available.
     * 
     * @param d the document instance
     * @param documentService the requested service/interface
     */
    public static @NonNull <T> T asRequired(@NonNull Document d, Class<T> documentService) {
        return as(d, documentService, true);
    }
    
    private static @CheckForNull <T> T as(@NonNull Document d, Class<T> documentService, boolean useStub) {
        if (d == null) {
            throw new NullPointerException("null document");
        }
        T res;
        
        if (documentService.isInstance(d)) {
            return (T)d;
        }
        Object serv = d.getProperty(documentService);
        if (serv != null) {
            if (serv instanceof V) {
                if (useStub) {
                    res = (T)((V<T>)serv).delegate;
                    if (res == null) {
                        throw new IllegalArgumentException();
                    }
                    return res;
                } else {
                    return null;
                }
            }
            if (serv == NOT_FOUND) {
                if (!useStub) {
                    return null;
                }
                // fall through, make a wrapper
            } else {
                return (T)serv;
            }
        }
        
        Lookup lkp = DocumentServices.getInstance().getLookup(d);
        serv = lkp.lookup(documentService);
        Object v = serv;
        if (serv == null) {
            if (useStub) {
                lkp = DocumentServices.getInstance().getStubLookup(d);
                serv = lkp.lookup(documentService);
                v = new V(serv);
                d.putProperty(documentService, new V(serv));
                if (serv == null) {
                    throw new IllegalArgumentException();
                }
            }
        } else {
            d.putProperty(documentService, v == null ? NOT_FOUND : v);
        }
        return (T)serv;
    }

    /**
     * Creates an empty document not attached to any environment, of the given
     * MIME type. The created document's type may differ for individual MIME types.
     * If the document cannot be created for the MIME type, an {@link IllegalArgumentException}
     * is thrown.
     * 
     * @param mimeType
     * @return LineDocument instance
     */
    public static @NonNull LineDocument  createDocument(String mimeType) {
        DocumentFactory f = MimeLookup.getLookup(mimeType).lookup(DocumentFactory.class);
        if (f == null) {
            throw new IllegalArgumentException("No document available for MIME type: " + mimeType);
        }
        Document doc = f.createDocument(mimeType);
        if (doc == null) {
            throw new IllegalArgumentException("Could not create document for MIME type: " + mimeType);
        }
        LineDocument ldoc = as(doc, LineDocument.class);
        if (ldoc == null) {
            throw new IllegalArgumentException("Could not create document for MIME type: " + mimeType);
        }
        return ldoc;
    }
}
