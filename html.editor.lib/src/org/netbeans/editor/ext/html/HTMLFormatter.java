/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.editor.ext.html;

import java.io.IOException;
import java.io.Writer;
import java.util.regex.Pattern;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AbstractDocument.DefaultDocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.AbstractFormatLayer;
import org.netbeans.editor.ext.FormatSupport;
import org.netbeans.editor.ext.FormatWriter;
import org.netbeans.editor.ext.html.HTMLSyntaxSupport;
import org.netbeans.editor.ext.html.dtd.DTD;
import org.netbeans.editor.ext.html.dtd.DTD.Element;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Formatter for html files.
 * @author Petr Pisl
 */

public class HTMLFormatter extends ExtFormatter {
    
    //at least one character
    private static final Pattern VALID_TAG_NAME = Pattern.compile("\\w+"); // NOI18N
    
    private static final String[] UNFORMATTABLE_TAGS = new String[]{"pre", "script"}; //NOI18N
    
    private static final int WORKUNITS_MAX = 100;
    
    /** Creates a new instance of HTMLFormater */
    public HTMLFormatter(Class kitClass) {
        super(kitClass);
    }
    
    protected boolean acceptSyntax(Syntax syntax) {
        return (syntax instanceof HTMLSyntax);
    }
    
    
    protected void initFormatLayers() {
        addFormatLayer(new OutLineLayer());
    }
    
    public Writer reformat(final BaseDocument doc, final int startOffset, final int endOffset,
            final boolean indentOnly) throws BadLocationException, IOException {
        
        
        final HTMLSyntaxSupport sup = (HTMLSyntaxSupport)(doc.getSyntaxSupport().get(HTMLSyntaxSupport.class));
        
        //do indentation in awt
        if (startOffset == endOffset){
            //pressed enter - called from indentNewLine
            //get first non-white token backward
            int nonWSTokenBwd = Utilities.getFirstNonWhiteBwd(doc, endOffset);
            if(nonWSTokenBwd != -1) {
                TokenItem token = sup.getTokenChain(nonWSTokenBwd, nonWSTokenBwd + 1);
                if(token != null
                        && token.getTokenID() == HTMLTokenContext.TAG_CLOSE_SYMBOL
                        && !token.getImage().equals("/>")) {
                    //the enter was pressed after a tag which is not an empty tag
                    //find start token of the tag
                    do {
                        token = token.getPrevious();
                    } while(token != null && token.getTokenID() != HTMLTokenContext.TAG_OPEN);
                    
                    if(token != null && !sup.isSingletonTag(token)) {
                        //found an open tag => test whether it has a matching close tag between endOffset and the open tag offset
                        //if so, do not increase the indentation
                        int[] match = sup.findMatchingBlock(token.getOffset(), false);
                        if((match != null && match[0] > endOffset) || match == null) {
                            //test if the tag has optional end -> if so not not indent
                            TokenItem tagNameToken = token;
                            DTD dtd = sup.getDTD();
                            if(tagNameToken != null && dtd != null) {
                                Element elem = dtd.getElement(tagNameToken.getImage().toUpperCase());
                                if(elem != null && !elem.hasOptionalEnd()) {
                                    //increase indentation
                                    int previousLineIndentation = Utilities.getRowIndent(doc, token.getOffset());
                                    int newLineIndent = previousLineIndentation + getShiftWidth();
                                    changeRowIndent(doc, startOffset, newLineIndent);
                                    return null;
                                }
                            }
                        }
                        
                    }
                }
            }
            //found something else => indent to the some level
            int previousLineIndentation = Utilities.getRowIndent(doc, endOffset, false);
            changeRowIndent(doc, startOffset, previousLineIndentation);
            return null;
        }
        
        //do the reformat in non-awt thread && show progressbar when REFORMATTING bigger files
        if(!indentOnly && (endOffset - startOffset) > 50000) {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    try {
                        doReformat(doc, sup, startOffset, endOffset, indentOnly, true);
                    }catch(BadLocationException ble) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ble);
                    }
                }
            },0,Math.max(0,Thread.currentThread().getPriority() - 1));
        } else doReformat(doc, sup, startOffset, endOffset, indentOnly, false);
        
        return null;
    }
    
    protected void doReformat(BaseDocument origdoc, HTMLSyntaxSupport origsup, int startOffset, int endOffset,
            boolean indentOnly, boolean progress) throws BadLocationException {
        ProgressHandle ph = null;
        if(progress) {
            ph = ProgressHandleFactory.createHandle(NbBundle.getBundle(HTMLFormatter.class).getString("MSG_code_reformat"));//NOI18N
            ph.start();
            ph.switchToDeterminate(WORKUNITS_MAX);
        }
        
        //copy the entire original document into a fakeone
        BaseDocument doc = new HackedBaseDocument(origdoc.getKitClass(), false);
        doc.insertString(0, origdoc.getText(0, origdoc.getLength()), null);
        HTMLSyntaxSupport sup = new HTMLSyntaxSupport(doc);
        
        //line boundaries of the reformatted area
        int firstLine = Utilities.getLineOffset(origdoc, startOffset);
        int lastLine = Utilities.getLineOffset(origdoc, endOffset);
        int linesToReformat = lastLine - firstLine;
        //reformat the fake document
        doc.atomicLock();
        try {
            int lastPairTokenRowOffset = -1;
            int pos = Utilities.getRowStart(doc, endOffset);
            TokenItem token = null;
            int lastPerc = 0;
            do {
                try{
                    int fnw = Utilities.getRowFirstNonWhite(doc, pos);
                    if (fnw == -1) fnw = pos;
                    token = sup.getTokenChain(fnw, fnw+1);
                    
                    //count progress
                    if(progress) {
                        int lineOfs = Utilities.getLineOffset(doc, fnw);
                        int perc = 100 -((lineOfs*100) / linesToReformat);
                        if((perc % 10) == 0 && lastPerc < perc) {
                            lastPerc = perc;
                            ph.progress((int)(perc*0.8)); //show progress up to 80%
                        }
                    }
                    
                    if (token != null && token.getNext() != null){
                        //now we need the token itself - there is only a tag symbol on the begginning of the line
                        token = token.getNext();
                        
                        //if we backtracked over the lastpair token => reset
                        if(token.getOffset() <= lastPairTokenRowOffset) lastPairTokenRowOffset = -1;
                        
                        if (token.getTokenContextPath().contains(HTMLTokenContext.contextPath) &&
                                token.getTokenID().getNumericID() == HTMLTokenContext.TAG_CLOSE_ID) {
                            //the tag is an end tag
                            String tag = token.getImage();
                            int poss = -1;
                            //search backward for a pair token
                            while ( token != null) {
                                //It must be ensured that we do not backtrack the tokens over
                                //last found pair token. The lastPairTokenRowOffset variable
                                //is used to remember the last pair token offset
                                //fix for #49411
                                if( token.getOffset() > lastPairTokenRowOffset) {
                                    if (token.getTokenContextPath().contains(HTMLTokenContext.contextPath)
                                    && HTMLSyntaxSupport.isTag(token)
                                    && !sup.isSingletonTag(token)) {
                                        if (token.getImage().trim().equals(tag) &&
                                                token.getTokenID().getNumericID() == HTMLTokenContext.TAG_OPEN_ID){
                                            if (poss == 0){
                                                if(isFormattableTag(token.getImage()) || indentOnly) {
                                                    doc.remove(pos, fnw-pos);
                                                    fnw = Utilities.getRowFirstNonWhite(doc, token.getOffset());
                                                    poss = Utilities.getRowStart(doc, fnw);
                                                    doc.insertString(pos, doc.getText(poss, fnw-poss), null);
                                                    int tagIndentation = Utilities.getRowIndent(doc, pos);
                                                    if (!indentOnly){
                                                        int rowOffset = Utilities.getRowStart(doc, Utilities.getRowStart(doc, pos) - 1);
                                                        int indentation = Utilities.getRowIndent(doc, pos) + this.getShiftWidth();
                                                        int delta = 0;
                                                        int deltahelp;
                                                        
                                                        String  unformattable = null;
                                                        while (rowOffset > poss){
                                                            deltahelp = Utilities.getRowFirstNonWhite(doc, rowOffset );
                                                            if (deltahelp > -1){
                                                                token = sup.getTokenChain(deltahelp, deltahelp+1);
                                                                
                                                                boolean unformattableJustFound = false;
                                                                TokenItem t = token;
                                                                //check whether the line contains an open tag from the list of unformattable tags
                                                                while(t != null && (Utilities.getRowStart(doc, t.getOffset()) == Utilities.getRowStart(doc, token.getOffset()))) {
                                                                    if(t.getTokenID() == HTMLTokenContext.TAG_OPEN && unformattable != null && t.getImage().equalsIgnoreCase(unformattable)) {
                                                                        unformattable = null; //we found an end of the unformattable area
                                                                        unformattableJustFound = false;
                                                                    }
                                                                    if(t.getTokenID() == HTMLTokenContext.TAG_CLOSE) {
                                                                        //an unformattable area start
                                                                        unformattable = !isFormattableTag(t.getImage().trim()) ? t.getImage().trim() : null;
                                                                        unformattableJustFound = true;
                                                                    }
                                                                    t = t.getNext();
                                                                }
                                                                
                                                                //reformat only when there isn't any unformattable tag
                                                                if ((unformattableJustFound || unformattable == null) && token != null &&
                                                                        token.getTokenContextPath().contains(HTMLTokenContext.contextPath)){
                                                                    changeRowIndent(doc, rowOffset, indentation);
                                                                    int htmlindent = Utilities.getRowIndent(doc, rowOffset);
                                                                    delta = delta + Utilities.getRowFirstNonWhite(doc, rowOffset ) - deltahelp;
                                                                }
                                                            }
                                                            rowOffset = Utilities.getRowStart(doc, rowOffset-1);
                                                        }
                                                        pos = pos + delta;
                                                        //remember last found pair token offset
                                                        lastPairTokenRowOffset = poss;
                                                    }
                                                    break;
                                                }
                                            } else{
                                                poss--;
                                            }
                                        } else {
                                            if (token.getImage().equals(tag)){
                                                poss++;
                                            }
                                        }
                                    }
                                    token = token.getPrevious();
                                } else {
                                    //reset the last found pair token
                                    lastPairTokenRowOffset = -1;
                                    break;
                                }
                            }
                        }
                    }
                } catch (Exception e){
                    ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
                }
                pos = Utilities.getRowStart(doc, pos-1);
            } while (pos > startOffset && pos > 0);
        }finally{
            doc.atomicUnlock();
        }
        
        //copy the affected area line by line (it preserves annotations bindend to j.s.t.Position-s)
        origdoc.atomicLock();
        try {
            for(int i = firstLine; i <= lastLine; i++) {
                //offsets of the reformatted area in the fake document
                int fakeStartOffset = Utilities.getRowStartFromLineOffset(doc, i);
                int fakeNWF = Utilities.getFirstNonWhiteFwd(doc, fakeStartOffset);
                int fakeIndent = (fakeNWF - fakeStartOffset);
                if(fakeNWF > 0) {
                    //offsets of the affected part in the original document
                    int newStartOffset = Utilities.getRowStartFromLineOffset(origdoc, i);
                    int newNWF = Utilities.getFirstNonWhiteFwd(origdoc, newStartOffset);
                    int newIndent = newNWF - newStartOffset;
                    
                    if(newIndent != fakeIndent) {
                        if(newNWF > 0)
                            origdoc.remove(newStartOffset, newNWF - newStartOffset); //remove original formatting
                        origdoc.insertString(newStartOffset, doc.getText(fakeStartOffset, fakeNWF - fakeStartOffset), null);
                    }
                }
            }
        }finally{
            origdoc.atomicUnlock();
        }
        
        if(progress) ph.finish();
    }
    
    public int[] getReformatBlock(JTextComponent target, String typedText) {
        int [] i = super.getReformatBlock(target, typedText);
        //System.out.println("getReformatBlock: |" + typedText + "|"); // NOI18N
        char lastChar = typedText.charAt(typedText.length() - 1);
        try{
            BaseDocument doc = Utilities.getDocument(target);
            int dotPos = target.getCaret().getDot();
            HTMLSyntaxSupport sup = (HTMLSyntaxSupport)(doc.getSyntaxSupport().get(HTMLSyntaxSupport.class));
            
            if (lastChar == '>') {
                
                TokenItem token = sup.getTokenChain(dotPos-1, dotPos);
                TokenItem origToken = token;
                int start = token.getOffset();
                if (token.getTokenID().getNumericID() == HTMLTokenContext.TAG_CLOSE_SYMBOL_ID &&
                        !token.getImage().endsWith("/>")){
                    do {
                        token = token.getPrevious();
                    } while (token != null && !sup.isTag(token));
                    if(token == null) return i;
                    if (token.getTokenID().getNumericID() == HTMLTokenContext.TAG_CLOSE_ID){
                        String tag = token.getImage().trim();
                        int poss = -1;
                        
                        while ( token != null){
                            if (sup.isTag(token)) {
                                if (token.getImage().trim().equals(tag) &&
                                        token.getTokenID().getNumericID() == HTMLTokenContext.TAG_OPEN_ID){
                                    ;
                                    if (poss == 0){
                                        int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                                        return new int [] {fnw, dotPos};
                                    } else{
                                        poss--;
                                    }
                                } else {
                                    if (token.getImage().equals(tag)){
                                        poss++;
                                    }
                                }
                            }
                            token = token.getPrevious();
                        }
                    } else {
                        //found an open tag => auto include close tag
                        String tagname = token.getImage();
                        
                        //check if the tag has optional end
                        //in the case there isn't any DTD information disable the autocomplete
                        //do the same if the tag has no entry in the DTD
                        DTD dtd = sup.getDTD();
                        if(dtd != null) {
                            DTD.Element tag = dtd.getElement( tagname.toUpperCase());
                            if(tag != null && !tag.hasOptionalEnd()) { //test required end
                                //test if the '>' char is the lastnonwhite char on the line
                                //to prevent autocompletion of tags written before a text
                                int fnwfw = Utilities.getFirstNonWhiteFwd(doc, origToken.getOffset() + origToken.getImage().length());
                                if(fnwfw != -1 && Utilities.getLineOffset(doc, origToken.getOffset()) < Utilities.getLineOffset(doc, fnwfw)) {
                                    int[] match = sup.findMatchingBlock(token.getOffset(), false);
                                    if((match != null && match[0] < dotPos) || match == null) {
                                        //there isn't a _real_ matching tag => autocomplete
                                        //note: the test for match index is necessary since the '<'  in <tag> matches the '>' character on the end of the tag.
                                        if(VALID_TAG_NAME.matcher(tagname).matches()) { //check the tag name
                                            doc.atomicLock();
                                            try {
                                                doc.insertString( dotPos, "</"+tagname+">" , null);
                                            } catch( BadLocationException exc ) {
                                                //do nothing
                                            } finally {
                                                doc.atomicUnlock();
                                            }
                                            //return cursor back
                                            target.setCaretPosition(dotPos);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    //}
                }
                
                
            }
            
            if(lastChar == '\n') {
                // just pressed enter
                
                int newCursorPos = smartEnter(doc, dotPos, sup);
                
                if (newCursorPos != -1){
                    target.setCaretPosition(newCursorPos);
                    //return end tag line start and end offset to reformat the end tag correctly
                    //get first non white offset from the line after the newly inserted line
                    int closingTagLine = Utilities.getLineOffset(doc, newCursorPos) + 1;
                    int start = Utilities.getRowStartFromLineOffset(doc, closingTagLine);
                    int end = Utilities.getRowEnd(doc, start);
                    return new int[]{start, end};
                }
            }
            
            
        } catch (Exception e){
            ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
        }
        
        return i;
    }
    
    private boolean isFormattableTag(String tagName) {
        for(int i = 0; i < UNFORMATTABLE_TAGS.length; i++) {
            if(tagName.equalsIgnoreCase(UNFORMATTABLE_TAGS[i])) {
                return false;
            }
        }
        return true;
    }
    

    /**
     * "smart enter" :-) feature
     * <tag>|</tag>
     * ENTER key pressed =>
     * <tag>
     *  |
     * </tag>
     *
     * @return new caret position, -1 if smart enter was not applied
     */
    protected int smartEnter(BaseDocument doc, int dotPos, HTMLSyntaxSupport sup) throws BadLocationException {
        int newCaretPos = -1; // return value
         /*
          check following situation:
             1. does the previous line ends with an open tag with tagname X?
             2. does the token on the carret position is an end tag with X tagname?
          #59499: the 2nd condition should be wider, like "is the line starting at caret postion terminated by X tagname"?
          if both of the assumptions are true then:
           a. insert endline on the carret position
           b. indent the new line
           c. move the cursor to the end of the new line
         */
                
                //check #1
        int endOfPrevLineOffset = Utilities.getRowStart(doc, dotPos) -1 ;
        if(endOfPrevLineOffset > 0) { //do not reformat when enter pressed on the first line
            TokenItem token = sup.getTokenChain(endOfPrevLineOffset -1 , endOfPrevLineOffset );
            if(token != null &&
                    token.getTokenID() == HTMLTokenContext.TAG_CLOSE_SYMBOL) {
                //found an end of a tag -> we needs to decide whether it is an open tag
                //find tag beninning (skip whitespaces inside the tag (</table >)
                do {
                    token = token.getPrevious();
                } while (token != null && token.getTokenID() != HTMLTokenContext.TAG_OPEN);
                
                if(token != null &&
                        token.getTokenID() == HTMLTokenContext.TAG_OPEN) {
                    //an open tag
                    String openTagName = token.getImage();
                    //check #2
                    
                    boolean applySmartEnter = false;
                    int lineEnd = Utilities.getRowEnd(doc, dotPos);
                    int closingTagOffset = -1;
                    token = sup.getTokenChain(lineEnd - 1, lineEnd);
                    
                    if (token.getTokenID() == HTMLTokenContext.TAG_CLOSE_SYMBOL && ">".equals(token.getImage())){
                        TokenItem tagNameToken = token.getPrevious();
                        
                        if (tagNameToken != null && openTagName.equalsIgnoreCase(tagNameToken.getImage())){
                            TokenItem tsToken = tagNameToken.getPrevious();
                            
                            if (tsToken != null && tsToken.getTokenID() == HTMLTokenContext.TAG_OPEN_SYMBOL && "</".equals(tsToken.getImage())){
                                applySmartEnter = true;
                                closingTagOffset = tsToken.getOffset();
                            }
                        }
                    }
                    
                    if(applySmartEnter) {
                        //found pair end tag => we can do the reformat!!!
                        int currentLineIndex = Utilities.getLineOffset(doc, token.getOffset());
                        //a. insert a new line on the current line
                        doc.atomicLock();
                        try {
                            doc.insertString(closingTagOffset, "\n" , null);
                            
                            //b. indent the new line
                            int newLineOffset = Utilities.getRowStartFromLineOffset(doc, currentLineIndex);
                            int previousLineIndentation = Utilities.getRowIndent(doc, Utilities.getRowStartFromLineOffset(doc, currentLineIndex - 1));
                            int newLineIndent = previousLineIndentation + getShiftWidth();
                            changeRowIndent(doc, newLineOffset, newLineIndent);
                            
                            //c. set cursor to the beginning of the new line
                            newCaretPos = Math.min(Utilities.getFirstNonWhiteFwd(doc, Utilities.getRowStart(doc, newLineOffset)),
                                    Utilities.getRowEnd(doc, newLineOffset));
                            
                        } catch( BadLocationException exc ) {
                            //do nothing
                        } finally {
                            doc.atomicUnlock();
                        }
                    }
                }
            }
        }
        
        return newCaretPos;
    }
    
    public class OutLineLayer extends AbstractFormatLayer {
        
        public OutLineLayer() {
            super("html-out-line"); //NOI18N
        }
        
        public void format(FormatWriter fw) {
            //System.out.println("format in layer"); // NOI18N
            BaseDocument doc = (BaseDocument)fw.getDocument();
            int dotPos = fw.getOffset();
            
            if (doc != null && dotPos > 0) {
                try {
                    int firstNonEmptyRow = 0;
                    firstNonEmptyRow = Utilities.getFirstNonWhiteRow(doc, dotPos-1, false);
                    if (firstNonEmptyRow == -1)
                        return;
                    int rstart = Utilities.getRowStart(doc, firstNonEmptyRow);
                    int fNonWhite = Utilities.getFirstNonWhiteFwd(doc, rstart);
                    doc.insertString(dotPos, doc.getText(rstart, fNonWhite-rstart), null);
                }catch(BadLocationException e) {
                    ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
                }
                
            }
        }
        
        
    }
    
//some of not needed methods are removed - I do not need them in the fake document
    private class HackedBaseDocument extends BaseDocument {
        public HackedBaseDocument(Class kitClass, boolean addToRegistry) {
            super(kitClass, addToRegistry);
        }
        boolean notifyModifyCheckStart(int offset, String vetoExceptionText) throws BadLocationException {
            return false;
        }
        protected void fireInsertUpdate(DocumentEvent e) {
            //do nothing
        }
        protected void postRemoveUpdate(DefaultDocumentEvent chng) {
            //do ng
        }
    }
    
}
