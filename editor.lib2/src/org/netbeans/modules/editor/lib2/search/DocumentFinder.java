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

package org.netbeans.modules.editor.lib2.search;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.lib2.DocUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author  Martin Roskanin
 */
public class DocumentFinder
{
    private static final Logger LOG = Logger.getLogger(EditorFindSupport.class.getName());
   
    /** Creates a new instance of DocumentFinder */
    private DocumentFinder()
    {
    }


    private static DocFinder getFinder(Document doc, Map searchProps, boolean oppositeDir, boolean blocksFinder){
        String text = (String)searchProps.get(EditorFindSupport.FIND_WHAT);
        if (text == null || text.length() == 0) {
            if (blocksFinder) {
                return FalseBlocksFinder.INSTANCE;
            } else {
                return FalseFinder.INSTANCE;
            }
        }

        Boolean b = (Boolean)searchProps.get(EditorFindSupport.FIND_BACKWARD_SEARCH);
        boolean bwdSearch = (b != null && b.booleanValue());
        if (oppositeDir) { // negate for opposite direction search
            bwdSearch = !bwdSearch;
        }

        b = (Boolean)searchProps.get(EditorFindSupport.FIND_MATCH_CASE);
        boolean matchCase = (b != null && b.booleanValue());
        b = (Boolean)searchProps.get(EditorFindSupport.FIND_SMART_CASE);
        boolean smartCase = (b != null && b.booleanValue());
        b = (Boolean)searchProps.get(EditorFindSupport.FIND_WHOLE_WORDS);
        boolean wholeWords = (b != null && b.booleanValue());

        if (smartCase && !matchCase) {
            int cnt = text.length();
            for (int i = 0; i < cnt; i++) {
                if (Character.isUpperCase(text.charAt(i))) {
                    matchCase = true;
                }
            }
        }

        b = (Boolean) searchProps.get(EditorFindSupport.FIND_REG_EXP);
        boolean regExpSearch = (b!=null && b.booleanValue());
        
        Pattern pattern = null;
        if (regExpSearch){
            try{
                pattern = PatternCache.getPattern(text, matchCase);
                if (pattern == null){
                    pattern =  (matchCase) ? Pattern.compile(text, Pattern.MULTILINE) : Pattern.compile(text, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE); // NOI18N
                    PatternCache.putPattern(text, matchCase, pattern);
                }
            }catch(PatternSyntaxException pse){
                if (!blocksFinder){
                    NotifyDescriptor msg = new NotifyDescriptor.Message(
                            pse.getDescription(), NotifyDescriptor.ERROR_MESSAGE);
                    msg.setTitle(NbBundle.getBundle(DocumentFinder.class).getString("pattern-error-dialog-title")); //NOI18N
                    DialogDisplayer.getDefault().notify(msg);
                }
                PatternCache.putPattern(text, matchCase, null);
                return null;
            }
        }else{
            PatternCache.clear();
        }

        if (blocksFinder) {
            if (wholeWords && !regExpSearch) {
                WholeWordsBlocksFinder wholeWordsBlocksFinder = new WholeWordsBlocksFinder();
                wholeWordsBlocksFinder.setParams(doc, text, matchCase);
                return wholeWordsBlocksFinder;
            } else {
                if (regExpSearch){
                    RegExpBlocksFinder regExpBlocksFinder = new RegExpBlocksFinder();
                    regExpBlocksFinder.setParams(pattern, matchCase);
                    return regExpBlocksFinder;
                }else{
                    StringBlocksFinder stringBlocksFinder = new StringBlocksFinder();
                    stringBlocksFinder.setParams(text, matchCase);
                    return stringBlocksFinder;
                }
            }
        } else {
            if (wholeWords && !regExpSearch) {
                if (bwdSearch) {
                    WholeWordsBwdFinder wholeWordsBwdFinder = new WholeWordsBwdFinder();
                    wholeWordsBwdFinder.setParams(doc, text, matchCase);
                    return wholeWordsBwdFinder;
                } else {
                    WholeWordsFwdFinder wholeWordsFwdFinder = new WholeWordsFwdFinder();
                    wholeWordsFwdFinder.setParams(doc, text, matchCase);
                    return wholeWordsFwdFinder;
                }
            } else {
                if (regExpSearch){
                    if (bwdSearch) {
                        RegExpBwdFinder regExpBwdFinder = new RegExpBwdFinder();
                        regExpBwdFinder.setParams(pattern, matchCase);
                        return regExpBwdFinder;
                    } else {
                        RegExpFwdFinder regExpFwdFinder = new RegExpFwdFinder();
                        regExpFwdFinder.setParams(pattern, matchCase);
                        return regExpFwdFinder;
                    }
                }else{
                    if (bwdSearch) {
                        StringBwdFinder stringBwdFinder = new StringBwdFinder();
                        stringBwdFinder.setParams(text, matchCase);
                        return stringBwdFinder;
                    } else {
                        StringFwdFinder stringFwdFinder = new StringFwdFinder();
                        stringFwdFinder.setParams(text, matchCase);
                        return stringFwdFinder;
                    }
                }
            }
        }
    }
    

    private static FindReplaceResult findReplaceImpl(String replaceText, Document doc, int startOffset, int endOffset, Map props,
                             boolean oppositeDir) throws BadLocationException{
        int ret[] = new int[2];
        if (endOffset == -1){
            endOffset = doc.getLength();
        }
        if (startOffset>endOffset){
            int temp = startOffset;
            startOffset = endOffset;
            endOffset = temp;
        }
        DocFinder finder = getFinder(doc, props, oppositeDir, false);
        if (finder == null){
            return null;
        }
        finder.reset();

        Boolean b = (Boolean)props.get(EditorFindSupport.FIND_BACKWARD_SEARCH);
        boolean back = (b != null && b.booleanValue());
        if (oppositeDir) {
            back = !back;
        }
        b = (Boolean)props.get(EditorFindSupport.FIND_BLOCK_SEARCH);
        boolean blockSearch = (b != null && b.booleanValue());
        Position blockStartPos = (Position) props.get(EditorFindSupport.FIND_BLOCK_SEARCH_START);
        int blockSearchStartOffset = (blockStartPos != null) ? blockStartPos.getOffset() : 0;
        Position pos = (Position) props.get(EditorFindSupport.FIND_BLOCK_SEARCH_END);
        int blockSearchEndOffset = (pos != null) ? pos.getOffset() : doc.getLength();

        if (blockSearchStartOffset > blockSearchEndOffset) {
            LOG.log(Level.WARNING, "end="+blockSearchEndOffset+" < start="+blockSearchStartOffset);
            //Changing places start and end block position
            int tmp = blockSearchStartOffset;
            blockSearchStartOffset = blockSearchEndOffset;
            blockSearchEndOffset = tmp;
        }
        CharSequence docText = DocumentUtilities.getText(doc).subSequence(0, doc.getLength()); // exclude the artificial last \n
        CharSequence blockText = blockSearch
                ? docText.subSequence(blockSearchStartOffset, blockSearchEndOffset)
                : docText;
        int initOffset;
        if (back && !blockSearch)
            initOffset = (endOffset<doc.getLength()) ? endOffset : startOffset;
        else if (back && blockSearch)
            initOffset = endOffset - startOffset;
        else if (!back && blockSearch) 
            initOffset = startOffset - blockSearchStartOffset;
         else
            initOffset = startOffset;
        if (initOffset < 0 || initOffset > blockText.length ()) {
            LOG.log (
                Level.INFO,
                "Index: " + initOffset +
                "\nOffset: " + startOffset + "-" + endOffset +
                "\nBlock: " + blockSearchStartOffset + "-" + blockSearchEndOffset +
                "\nLength : " + blockText.length ()
            );
            initOffset = Math.max (initOffset, 0);
            initOffset = Math.min (initOffset, blockText.length ());
        }
        int findRet = finder.find(initOffset, blockText);
        if (!finder.isFound()){
            ret[0]  = -1;
            return new FindReplaceResult(ret, replaceText);
        }
        if (blockSearch)
            ret[0] = blockSearchStartOffset + findRet;
        else
            ret[0] = findRet;
        
        if (finder instanceof StringFinder){
            int length = ((StringFinder)finder).getFoundLength();
            ret[1] = ret [0] + length;
        }
        
        if (finder instanceof RegExpFinder){
            Matcher matcher = ((RegExpFinder)finder).getMatcher();
            if (matcher != null && replaceText != null){
                CharSequence foundString = docText.subSequence(ret[0], ret[1]);
                matcher.reset(foundString);
                if (matcher.find()){
                    try{
                        replaceText = matcher.replaceFirst(convertStringForMatcher(replaceText));
                    }catch(IndexOutOfBoundsException ioobe){
                        NotifyDescriptor msg = new NotifyDescriptor.Message(
                                ioobe.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
                        msg.setTitle(NbBundle.getBundle(DocumentFinder.class).getString("pattern-error-dialog-title")); //NOI18N
                        DialogDisplayer.getDefault().notify(msg);
                        return null;
                    }
                }
            }
        }
        return new FindReplaceResult(ret, replaceText);
    }

    /**
     * Finds in document 
     *
     * @param doc document where to find
     * @param startOffset offset in the document where the search will start
     * @param endOffset offset where the search will end with reporting
     *   that nothing was found.
     * @param props find properties
     */
    public static int[] find(Document doc, int startOffset, int endOffset, Map props,
                             boolean oppositeDir) throws BadLocationException{
        FindReplaceResult result = findReplaceImpl(null, doc, startOffset, endOffset, props, oppositeDir);
        if (result == null){
            return null;
        }

        return result.getFoundPositions();
    }

    public static int[] findBlocks(Document doc, int startOffset, int endOffset, 
                    Map props, int blocks[]) throws BadLocationException{
        BlocksFinder finder =(BlocksFinder) getFinder(doc, props, false, true);
        if (finder == null){
            return blocks;
        }
        CharSequence cs = DocumentUtilities.getText(doc, startOffset, endOffset - startOffset);
        if (cs==null){
            return null;
        }
        synchronized (finder) {
            finder.reset();
            finder.setBlocks(blocks);
            finder.find(startOffset, cs);
            int ret [] = finder.getBlocks();
            return ret;
        }
    }
    
    /**
     *  Finds the searching string and substitute replace expression in case of
     *  regexp backreferences.
     *  @return FindReplaceResult, that contains positions of found string and substituted replace expression
     */
    public static FindReplaceResult findReplaceResult(String replaceString, Document doc, int startOffset, int endOffset, Map props,
                             boolean oppositeDir) throws BadLocationException{
        return findReplaceImpl(replaceString, doc, startOffset, endOffset, props, oppositeDir);
    }
    

    private static String convertStringForMatcher(String text) {
        String res = null;
        if (text != null){
            String[] sGroups = text.split("\\\\\\\\", text.length()); //NOI18N
            res = "";                         //NOI18N
            for(int i=0;i<sGroups.length;i++){
                String tmp = sGroups[i];
                tmp = tmp.replace("\\" + "r", "\r"); //NOI18N
                tmp = tmp.replace("\\" + "n", "\n"); //NOI18N
                tmp = tmp.replace("\\" + "t", "\t"); //NOI18N
                res += tmp;
                if (i != sGroups.length - 1){
                    res += "\\\\";                   //NOI18N
                }
            }
        }
        return res;
    }

    private interface DocFinder{
        
        public int find(int initOffset, CharSequence data);        
        
        public boolean isFound();
        
        public void reset();
    }
    
    private static final class FalseBlocksFinder extends AbstractBlocksFinder {
        public static final FalseBlocksFinder INSTANCE = new FalseBlocksFinder();

        public int find(int initOffset, CharSequence data) {
            return -1;
        }

        private FalseBlocksFinder() {}
    }
    
    /** Request non-existent position immediately */
    private static class FalseFinder extends AbstractFinder implements StringFinder {
        public static final FalseFinder INSTANCE = new FalseFinder();

        public int find(int initOffset, CharSequence data) {
            return -1;
        }

        public int getFoundLength() {
            return 0;
        }

        private FalseFinder() {}
    }
    
    private static abstract class AbstractBlocksFinder extends AbstractFinder
        implements BlocksFinder {

        private static int[] EMPTY_INT_ARRAY = new int[0];

        private int[] blocks = EMPTY_INT_ARRAY;

        private int blocksInd;

        private boolean closed;

        @Override
        public void reset() {
            super.reset();
            blocksInd = 0;
            closed = false;
        }

        public final int[] getBlocks() {
            if (!closed) { // not closed yet
                closeBlocks();
                closed = true;
            }
            return blocks;
        }

        public final void setBlocks(int[] blocks) {
            this.blocks = blocks;
            blocksInd = 0;
            closed = false;
        }

        protected final void addBlock(int blkStartPos, int blkEndPos) {
            if (blocksInd + 2 > blocks.length) {
                int[] dbl = new int[Math.max(10, (blocksInd + 1) * 2)];
                System.arraycopy(blocks, 0, dbl, 0, blocks.length);
                blocks = dbl;
            }
            
            blocks[blocksInd] = blkStartPos;
            blocks[blocksInd + 1] = blkEndPos;
            
            blocksInd += 2;
        }

        /** Insert closing sequence [-1, -1] */
        protected final void closeBlocks() {
            addBlock(-1, -1);
        }

        public final String debugBlocks() {
            StringBuffer buf = new StringBuffer();
            int ind = 0;
            while (blocks[ind] != -1) {
                buf.append((ind/2 + 1) + ": [" + blocks[ind] + ", " + blocks[ind + 1] + "]\n"); // NOI18N
                ind+= 2;
            }
            return buf.toString();
        }

    }
    
    /** Finder that constructs [begin-pos, end-pos] blocks.
    * This is useful for highlight-search draw layer.
    * The block-finders are always forward-search finders.
    */
    private interface BlocksFinder extends DocFinder {

        /** Set the array into which the finder puts
        * the position blocks. If the length of array is not sufficient
        * the finder extends the array. The last block is set to [-1, -1].
        */
        public void setBlocks(int[] blocks);

        /** Get the array filled with position blocks. It is either
        * original array passed to setBlocks() or the new array
        * if the finder extended the array.
        */
        public int[] getBlocks();

    }


    /** Abstract finder implementation. The only <CODE>find()</CODE>
    * method must be redefined.
    */
    private static abstract class AbstractFinder implements DocFinder {

        /** Was the string found? */
        protected boolean found;

        /** Was the string found? */
        public final boolean isFound() {
            return found;
        }

        /** Reset the finder */
        public void reset() {
            found = false;
        }

    }

    
    /** Finder that looks for some search expression expressed by string.
    * It can be either simple string
    * or some form of regular expression expressed by string.
    */
    private interface StringFinder extends DocFinder {

        /** Get the length of the found string. This is useful
        * for regular expressions, because the length of the regular
        * expression can be different than the length of the string
        * that matched the expression.
        */
        public int getFoundLength();

    }

    
    /** String forward finder that finds whole words only
    * and that creates position blocks.
    * There are some speed optimizations attempted.
    */
    private static final class WholeWordsBlocksFinder extends AbstractBlocksFinder {

        char chars[];

        int stringInd;

        boolean matchCase;

        boolean insideWord;

        boolean firstCharWordPart;

        boolean wordFound;

        Document doc;

        public WholeWordsBlocksFinder() {
        }

        public void setParams(Document doc, String s, boolean matchCase){
            this.matchCase = matchCase;
            this.doc = doc;
            chars = (matchCase ? s : s.toLowerCase()).toCharArray();
            firstCharWordPart = DocUtils.isIdentifierPart(doc, chars[0]);
        }
        
        @Override
        public void reset() {
            super.reset();
            insideWord = false;
            wordFound = false;
            stringInd = 0;
        }

        public int find(int initOffset, CharSequence data) {
            int offset = 0;
            int limitPos = data.length();
            int limitOffset = limitPos - 1;
            while (offset >= 0 && offset < limitPos) {
                char ch = data.charAt(offset);

                if (!matchCase) {
                    ch = Character.toLowerCase(ch);
                }

                // whole word already found but must verify next char
                if (wordFound) {
                    if (DocUtils.isIdentifierPart(doc, ch)) { // word continues
                        insideWord = firstCharWordPart;
                        offset -= chars.length - 1;
                    } else {
                        int blkEnd = initOffset + offset;
                        addBlock(blkEnd - chars.length, blkEnd);
                        insideWord = false;
                        offset++;
                    }
                    wordFound = false;
                    stringInd = 0;
                    continue;
                }

                if (stringInd == 0) { // special case for first char
                    if (ch != chars[0] || insideWord) { // first char doesn't match
                        insideWord = DocUtils.isIdentifierPart(doc, ch);
                        offset++;
                    } else { // first char matches
                        stringInd = 1; // matched and not inside word
                        if (chars.length == 1) {
                            if (offset == limitOffset) {
                                int blkStart = initOffset + offset;
                                addBlock(blkStart, blkStart + 1);
                            } else {
                                wordFound = true;
                            }
                        }
                        offset++;
                    }
                } else { // already matched at least one char
                    if (ch == chars[stringInd]) { // matches current char
                        stringInd++;
                        if (stringInd == chars.length) { // found whole string
                            if (offset == limitOffset) {
                                int blkEnd = initOffset + 1;
                                addBlock(blkEnd - stringInd, blkEnd);
                            } else {
                                wordFound = true;
                            }
                        }
                        offset++;
                    } else { // current char doesn't match, stringInd > 0
                        offset += 1 - stringInd;
                        stringInd = 0;
                        insideWord = firstCharWordPart;
                    }
                }

            }
            return offset;
        }

    }

    
    /** String forward finder that creates position blocks */
    private static final class StringBlocksFinder
        extends AbstractBlocksFinder {

        char chars[];

        int stringInd;

        boolean matchCase;

        public StringBlocksFinder() {
        }

        public void setParams(String s, boolean matchCase){
            this.matchCase = matchCase;
            chars = (matchCase ? s : s.toLowerCase()).toCharArray();
        }
        
        @Override
        public void reset() {
            super.reset();
            stringInd = 0;
        }

        public int find(int initOffset, CharSequence data) {
            int offset = 0;
            int endPos = data.length();
            while (offset >= 0 && offset < endPos) {
                char ch = data.charAt(offset);

                if (!matchCase) {
                    ch = Character.toLowerCase(ch);
                }
                if (ch == chars[stringInd]) {
                    
                    stringInd++;
                    if (stringInd == chars.length) {
                        int blkEnd = initOffset + offset + 1;
                        addBlock(blkEnd - stringInd, blkEnd);
                        stringInd = 0;
                    }
                    offset++;
                } else {
                    offset += 1 - stringInd;
                    stringInd = 0;
                }

            }
            return offset;
        }

    }
   
    
    private static final class WholeWordsBwdFinder extends GenericBwdFinder
        implements StringFinder {

        char chars[];

        int stringInd;

        boolean matchCase;

        boolean insideWord;

        boolean lastCharWordPart;

        boolean wordFound;

        int endInd;

        Document doc;

        public WholeWordsBwdFinder() {
        }
        
        public void setParams(Document doc, String s, boolean matchCase){
            this.doc = doc;
            this.matchCase = matchCase;
            chars = (matchCase ? s : s.toLowerCase()).toCharArray();
            endInd = chars.length - 1;
            DocUtils.isIdentifierPart(doc, chars[endInd]);
        }

        public int getFoundLength() {
            return chars.length;
        }

        @Override
        public void reset() {
            super.reset();
            insideWord = false;
            wordFound = false;
            stringInd = endInd;
        }

        protected int scan(char ch, boolean lastChar) {
            if (!matchCase) {
                ch = Character.toLowerCase(ch);
            }

            // whole word already found but must verify next char
            if (wordFound) {
                if (DocUtils.isIdentifierPart(doc, ch)) { // word continues
                    wordFound = false;
                    insideWord = lastCharWordPart;
                    stringInd = endInd;
                    return endInd;
                } else {
                    found = true;
                    return 1;
                }
            }

            if (stringInd == endInd) { // special case for last char
                if (ch != chars[endInd] || insideWord) { // first char doesn't match
                    insideWord = DocUtils.isIdentifierPart(doc, ch);
                    return -1;
                } else { // first char matches
                    stringInd = endInd - 1; // matched and not inside word
                    if (chars.length == 1) {
                        if (lastChar) {
                            found = true;
                            return 0;
                        } else {
                            wordFound = true;
                            return -1;
                        }
                    }
                    return -1;
                }
            } else { // already matched at least one char
                if (ch == chars[stringInd]) { // matches current char
                    stringInd--;
                    if (stringInd == -1) { // found whole string
                        if (lastChar) {
                            found = true;
                            return 0;
                        } else {
                            wordFound = true;
                            return -1;
                        }
                    }
                    return -1; // successfully matched char, go to next char
                } else { // current char doesn't match, stringInd > 0
                    int back = chars.length - 2 - stringInd;
                    stringInd = endInd;
                    insideWord = lastCharWordPart;
                    return back;
                }
            }
        }
    }

    
    /** Generic forward finder that simplifies the search process. */
    private static abstract class GenericFwdFinder extends AbstractFinder {

        public final int find(int initOffset, CharSequence chars) {
            int offset = initOffset;//0;
            int limitPos = chars.length();
            int limitOffset = limitPos - 1;
            while (offset >= 0 && offset < limitPos) {
                offset += scan(chars.charAt(offset), (offset == limitOffset));
                if (found) {
                    break;
                }
            }
            return offset;
        }

        /** This function decides if it found a desired string or not.
        * The function receives currently searched character and flag if it's
        * the last one that is searched or not.
        * @return if the function decides that
        * it found a desired string it sets <CODE>found = true</CODE> and returns
        * how many characters back the searched string begins in forward
        * direction (0 stands for current character).
        * For example if the function looks for word 'yes' and it gets
        * 's' as parameter it sets found = true and returns -2.
        * If the string is not yet found it returns how many characters it should go
        * in forward direction (in this case it would usually be 1).
        * The next searched character will be that one requested.
        */
        protected abstract int scan(char ch, boolean lastChar);

    }

    /** Generic backward finder that simplifies the search process. */
    private static abstract class GenericBwdFinder extends AbstractFinder {

        public final int find(int initOffset, CharSequence chars) {
            int offset = (initOffset != 0) ? initOffset-1 : chars.length() - 1;
            int offset2;
            int limitPos = 0;
            int limitOffset = chars.length();
            while (offset >= 0 && offset < limitOffset) {
                offset += scan(chars.charAt(offset), (offset == limitOffset));
                if (found) {
                    break;
                }
            }
            return offset;
        }

        /** This function decides if it found a desired string or not.
        * The function receives currently searched character and flag if it's
        * the last one that is searched or not.
        * @return if the function decides that
        * it found a desired string it sets <CODE>found = true</CODE> and returns
        * how many characters back the searched string begins in backward
        * direction (0 stands for current character). It is usually 0 as the
        * finder usually decides after the last required character but it's
        * not always the case e.g. for whole-words-only search it can be 1 or so.
        * If the string is not yet found it returns how many characters it should go
        * in backward direction (in this case it would usually be -1).
        * The next searched character will be that one requested.
        */
        protected abstract int scan(char ch, boolean lastChar);

    }

    
    private static final class WholeWordsFwdFinder extends GenericFwdFinder
        implements StringFinder {

        char chars[];

        int stringInd;

        boolean matchCase;

        Document doc;

        boolean insideWord;

        boolean firstCharWordPart;

        boolean wordFound;

        public WholeWordsFwdFinder() {
        }

        public void setParams(Document doc, String s, boolean matchCase){
            this.doc = doc;
            this.matchCase = matchCase;
            chars = (matchCase ? s : s.toLowerCase()).toCharArray();
            firstCharWordPart = DocUtils.isIdentifierPart(doc, chars[0]);
        }
        
        public int getFoundLength() {
            return chars.length;
        }

        @Override
        public void reset() {
            super.reset();
            insideWord = false;
            wordFound = false;
            stringInd = 0;
        }

        protected int scan(char ch, boolean lastChar) {
            if (!matchCase) {
                ch = Character.toLowerCase(ch);
            }

            // whole word already found but must verify next char
            if (wordFound) {
                if (DocUtils.isIdentifierPart(doc, ch)) { // word continues
                    wordFound = false;
                    insideWord = firstCharWordPart;
                    stringInd = 0;
                    return 1 - chars.length;
                } else {
                    found = true;
                    return -chars.length;
                }
            }

            if (stringInd == 0) { // special case for first char
                if (ch != chars[0] || insideWord) { // first char doesn't match
                    insideWord = DocUtils.isIdentifierPart(doc, ch);
                    return 1;
                } else { // first char matches
                    stringInd = 1; // matched and not inside word
                    if (chars.length == 1) {
                        if (lastChar) {
                            found = true;
                            return 0;
                        } else {
                            wordFound = true;
                            return 1;
                        }
                    }
                    return 1;
                }
            } else { // already matched at least one char
                if (ch == chars[stringInd]) { // matches current char
                    stringInd++;
                    if (stringInd == chars.length) { // found whole string
                        if (lastChar) {
                            found = true;
                            return 1 - chars.length; // how many chars back the string starts
                        } else {
                            wordFound = true;
                            return 1;
                        }
                    }
                    return 1; // successfully matched char, go to next char
                } else { // current char doesn't match, stringInd > 0
                    int back = 1 - stringInd;
                    stringInd = 0;
                    insideWord = firstCharWordPart;
                    return back; // go back to search from the next to first char
                }
            }
        }

    }
    
    private static class StringBwdFinder extends GenericBwdFinder
        implements StringFinder {

        char chars[];

        int stringInd;

        boolean matchCase;

        int endInd;

        public StringBwdFinder() {
        }

        public void setParams(String s, boolean matchCase){
            this.matchCase = matchCase;
            chars = (matchCase ? s : s.toLowerCase()).toCharArray();
            endInd = chars.length - 1;
        }
        
        public int getFoundLength() {
            return chars.length;
        }

        @Override
        public void reset() {
            super.reset();
            stringInd = endInd;
        }

        protected int scan(char ch, boolean lastChar) {
            if (!matchCase) {
                ch = Character.toLowerCase(ch);
            }
            if (ch == chars[stringInd]) {
                stringInd--;
                if (stringInd == -1) {
                    found = true;
                    return 0;
                }
                return -1;
            } else {
                if (stringInd == endInd) {
                    return -1;
                } else {
                    int back = chars.length - 2 - stringInd;
                    stringInd = endInd;
                    return back;
                }
            }
        }

    }

    private static final class StringFwdFinder extends GenericFwdFinder
        implements StringFinder {

        char chars[];

        int stringInd;

        boolean matchCase;

        public StringFwdFinder() {
        }
        
        public void setParams(String s, boolean matchCase){
            this.matchCase = matchCase;
            chars = (matchCase ? s : s.toLowerCase()).toCharArray();
        }

        public int getFoundLength() {
            return chars.length;
        }

        @Override
        public void reset() {
            super.reset();
            stringInd = 0;
        }

        protected int scan(char ch, boolean lastChar) {
            if (!matchCase) {
                ch = Character.toLowerCase(ch);
            }
            if (ch == chars[stringInd]) {
                stringInd++;
                if (stringInd == chars.length) { // found whole string
                    found = true;
                    return 1 - stringInd; // how many chars back the string starts
                }
                return 1; // successfully matched char, go to next char
            } else {
                if (stringInd == 0) {
                    return 1;
                } else {
                    int back = 1 - stringInd;
                    stringInd = 0;
                    return back;
                }
            }
        }

    }
    
    
    private abstract static class RegExpFinder extends AbstractFinder implements StringFinder{
        public abstract Matcher getMatcher();
    }
    
    // -----------------  regexp ----------------------
    private static class RegExpBwdFinder extends RegExpFinder{

        boolean matchCase;
        Pattern pattern;
        int length = 0;
        Matcher matcher;
        

        public RegExpBwdFinder() {
        }

        public Matcher getMatcher(){
            return matcher;
        }
        
        public void setParams(Pattern pattern, boolean matchCase){
            this.matchCase = matchCase;
            this.pattern = pattern;            
        }
        
        public int getFoundLength() {
            return length;
        }

        @Override
        public void reset() {
            super.reset();
            length = 0;
        }

        private int lineFind (int lineStart, int lineEnd, CharSequence chars){
            matcher = pattern.matcher(chars.subSequence(lineStart, lineEnd));
            int ret = -1;
            while (matcher.find()){
                int start = matcher.start();
                int end = matcher.end(); 
                length = end - start;
                if (length <= 0){
                    found = false;
                    return -1;
                }
                ret = start;
            }
            return ret;
        }
        
        public int find(int initOffset, CharSequence chars) {
            char ch;
            
            int charsEnd = (initOffset !=0) ? initOffset-1 : chars.length() - 1;
            int lineEnd = charsEnd;
            int lineStart = charsEnd;
            for (int i = charsEnd; i>=0; i--){
                ch = chars.charAt(i);
                if (ch == '\n' || i==0){
                    int retFind = lineFind (lineStart+((i==0)?0:1), lineEnd+1, chars);
                    if (retFind!=-1){
                        found = true;
                        return i + retFind + ((i==0)?0:1);
                    }
                    lineStart--;
                    lineEnd = lineStart;
                }else{
                    lineStart--;                    
                }
            }
            return -1;
        }

    }

    private static final class RegExpFwdFinder extends RegExpFinder{

        Pattern pattern;
        boolean matchCase;
        int length = 0;
        Matcher matcher;

        public RegExpFwdFinder() {
        }

        public Matcher getMatcher(){
            return matcher;
        }
        
        public void setParams(Pattern pattern, boolean matchCase){
            this.matchCase = matchCase;
            this.pattern = pattern;
        }
        
        public int getFoundLength() {
            return length;
        }

        @Override
        public void reset() {
            super.reset();
            length = 0;
        }

        public int find(int initOffset, CharSequence chars) {
            matcher = pattern.matcher(chars);
            if (matcher.find(initOffset)){
                found = true;
                int start = matcher.start();
                int end = matcher.end(); 
                length = end - start;
                if (length <= 0){
                    found = false;
                    return -1;
                }
                return start;
            }else{
                return -1;
            }
            
        }
    }

    /** String forward finder that creates position blocks */
    private static final class RegExpBlocksFinder
        extends AbstractBlocksFinder {

        Pattern pattern;
        
        int stringInd;

        boolean matchCase;

        public RegExpBlocksFinder() {
        }
        
        public void setParams(Pattern pattern, boolean matchCase){
            this.pattern = pattern;
            this.matchCase = matchCase;
        }

        @Override
        public void reset() {
            super.reset();
            stringInd = 0;
        }

        public int find(int initOffset, CharSequence data) {
            Matcher matcher = pattern.matcher(data);
            int ret = 0;
            while (matcher.find()){
                int start = initOffset + matcher.start();
                int end = initOffset + matcher.end(); 
                addBlock(start, end);
                ret = start;
            }
            return ret;
        }

    }
    
    private static class PatternCache{
        
        private static String cache_str;
        private static boolean cache_matchCase;
        private static Pattern cache_pattern;
        
        private PatternCache(){
        }
        
        public static void putPattern(String str, boolean matchCase, Pattern pattern){
            cache_str = str;
            cache_matchCase = matchCase;
            cache_pattern = pattern;
        }
        
        public static Pattern getPattern(String str, boolean matchCase){
            if (str == null) return null;
            if (str.equals(cache_str) && matchCase == cache_matchCase){
                return cache_pattern;
            }
            return null;
        }
        
        public static void clear(){
            cache_str = null;
            cache_matchCase = false;
            cache_pattern = null;
        }
    }

    public static class FindReplaceResult{
        private int[] positions;
        private String replacedString;
        
        public FindReplaceResult(int[] positions, String replacedString){
            this.positions = positions;
            this.replacedString = replacedString;
        }
        
        public String getReplacedString(){
            return replacedString;
        }
        
        public int[] getFoundPositions(){
            return positions;
        }
    }
    
}


