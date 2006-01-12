/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.text.indent;

import java.io.IOException;
import java.io.Writer;
import java.util.regex.Pattern;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;

import org.netbeans.editor.Syntax;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.AbstractFormatLayer;
import org.netbeans.editor.ext.FormatTokenPosition;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.editor.ext.FormatSupport;
import org.netbeans.editor.ext.FormatWriter;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.text.api.XMLDefaultTokenContext;
import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;
import org.netbeans.modules.xml.text.syntax.XMLTokenIDs;

import org.openide.ErrorManager;

import org.netbeans.modules.xml.text.syntax.javacc.lib.JJEditorSyntax;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * @author  Marek Fukala
 */
public class XMLFormatter extends ExtFormatter {
    
    //at least one character
    private static final Pattern VALID_TAG_NAME = Pattern.compile("[\\w+|-]*"); // NOI18N
    
    private static final int WORKUNITS_MAX = 100;
    
    public XMLFormatter(Class kitClass) {
        super(kitClass);
    }
    
    public FormatSupport createFormatSupport(FormatWriter fw) {
        return new XMLFormatSupport(fw);
    }
    
    protected boolean acceptSyntax(Syntax syntax) {
        return (syntax instanceof JJEditorSyntax);
    }
    
    protected void initFormatLayers() {
        addFormatLayer(new StripEndWhitespaceLayer());
    }
    
    public Writer reformat(final BaseDocument doc, final int startOffset, final int endOffset,
            final boolean indentOnly) throws BadLocationException, IOException {
        
        //TODO: we are not preserving whitespaces yet - make an editor option for that and make it enabled by default
        
        //System.out.println("reformat from "+ startOffset + " to " + endOffset + ": " +doc.getText(startOffset, endOffset-startOffset));
        
        
        TokenItem token = null;
        final XMLSyntaxSupport sup = (XMLSyntaxSupport)(doc.getSyntaxSupport().get(XMLSyntaxSupport.class));
        if(sup == null) return null; //do not format anything what is not a XML
        
        if (startOffset == endOffset){
            //pressed enter - called from indentNewLine
            //get first non-white token backward
            int nonWSTokenBwd = Utilities.getFirstNonWhiteBwd(doc, endOffset);
            //get first non-white token on the line
            token = sup.getTokenChain(nonWSTokenBwd, nonWSTokenBwd + 1);
            if(token != null &&
                    token.getTokenID() == XMLTokenIDs.TAG &&
                    token.getImage().endsWith(">") &&
                    !token.getImage().endsWith("/>")) {
                //the enter was pressed after a tag which is not an empty tag
                //find start token of the tag
                do {
                    token = token.getPrevious();
                } while(token != null && token.getTokenID() != XMLTokenIDs.TAG);
                
                if(token != null &&
                        token.getTokenID() == XMLTokenIDs.TAG &&
                        token.getImage().startsWith("<") &&
                        !token.getImage().startsWith("</")) {
                    //found an open tag => test whether it has a matching close tag between endOffset and the open tag offset
                    //if so, do not increase the indentation
                    int[] match = sup.findMatchingBlock(token.getOffset(), false);
                    if((match != null && match[0] > endOffset) || match == null) {
                        //increase indentation
                        int previousLineIndentation = Utilities.getRowIndent(doc, token.getOffset());
                        int newLineIndent = previousLineIndentation + getShiftWidth();
                        changeRowIndent(doc, startOffset, newLineIndent);
                        return null;
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
    
    protected void doReformat(BaseDocument origdoc, XMLSyntaxSupport origsup, int startOffset, int endOffset,
            boolean indentOnly, boolean progress) throws BadLocationException {
        ProgressHandle ph = null;
        if(progress) {
            ph = ProgressHandleFactory.createHandle(NbBundle.getBundle(XMLFormatter.class).getString("MSG_code_reformat"));//NOI18N
            ph.start();
            ph.switchToDeterminate(WORKUNITS_MAX);
        }
        
        //copy the entire original document into a fakeone
        BaseDocument doc = new HackedBaseDocument(origdoc.getKitClass(), false);
        doc.insertString(0, origdoc.getText(0, origdoc.getLength()), null);
        XMLSyntaxSupport sup = new XMLSyntaxSupport(doc);
        
        //line boundaries of the reformatted area
        int firstLine = Utilities.getLineOffset(origdoc, startOffset);
        int lastLine = Utilities.getLineOffset(origdoc, endOffset);
        int linesToReformat = lastLine - firstLine;
        //reformat the fake document
        doc.atomicLock();
        try {
            int pos = Utilities.getRowStart(doc, endOffset);
            int lastPairTokenRowOffset = -1;
            int lastPerc = 0;
            TokenItem token = null;
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
                        //if we backtracked over the lastpair token => reset
                        if(token.getOffset() <= lastPairTokenRowOffset) lastPairTokenRowOffset = -1;
                        
                        if (token.getTokenID() == XMLTokenIDs.TAG && token.getImage().startsWith("</")) {
                            //the tag is an end tag (starts with </)
                            String tag = token.getImage().substring(2); //cut off the '</'
                            int poss = -1;
                            //search backward for a pair token
                            while ( token != null) {
                                //It must be ensured that we do not backtrack the tokens over
                                //last found pair token. The lastPairTokenRowOffset variable
                                //is used to remember the last pair token offset
                                //fix for #49411
                                if( token.getOffset() > lastPairTokenRowOffset) {
                                    if (token.getTokenID() == XMLTokenIDs.TAG
                                            && !sup.isSingletonTag(token)) {
                                        if (token.getImage().substring(1).trim().equals(tag) &&
                                                token.getImage().startsWith("<") &&
                                                !token.getImage().startsWith("</")){
                                            //found an open tag with the searched name e.g. <table>
                                            if (poss == 0){
                                                doc.remove(pos, fnw-pos);
                                                fnw = Utilities.getRowFirstNonWhite(doc, token.getOffset());
                                                poss = Utilities.getRowStart(doc, fnw);
                                                doc.insertString(pos, doc.getText(poss, fnw-poss), null);
                                                if (!indentOnly){
                                                    int rowOffset = Utilities.getRowStart(doc, Utilities.getRowStart(doc, pos) - 1);
                                                    int indentation = Utilities.getRowIndent(doc, pos) + this.getShiftWidth();
                                                    int delta = 0;
                                                    int deltahelp;
                                                    
                                                    while (rowOffset > poss){
                                                        deltahelp = Utilities.getRowFirstNonWhite(doc, rowOffset );
                                                        if (deltahelp > -1){
                                                            token = sup.getTokenChain(deltahelp, deltahelp+1);
                                                            if (token != null && token.getTokenContextPath().contains(XMLDefaultTokenContext.contextPath)){
                                                                changeRowIndent(doc, rowOffset, indentation);
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
                                            } else{
                                                poss--;
                                            }
                                        } else {
                                            //close tag e.g. </table>
                                            if (token.getImage().length() > 1 && token.getImage().substring(2).equals(tag)){
                                                //close tag with the some name as the searched tag => increase deepness
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
        
        try{
            BaseDocument doc = Utilities.getDocument(target);
            int dotPos = target.getCaret().getDot();
            XMLSyntaxSupport sup = (XMLSyntaxSupport)(doc.getSyntaxSupport().get(XMLSyntaxSupport.class));
            if(sup == null) return i; //do not format anything what is not a XML
            
            if (typedText.charAt(0) == '>') {
                //get the token before typed text
                TokenItem token = sup.getTokenChain(dotPos-1, dotPos);
                TokenItem origToken = token;
                if (token.getTokenID() == XMLDefaultTokenContext.TAG &&
                        !token.getImage().endsWith("/>")) {
                    // > is a xml token which interests us
                    
                    //find tag beninning (skip whitespaces inside the tag (</table >)
                    do {
                        token = token.getPrevious();
                    } while (token != null && !(token.getTokenID() == XMLTokenIDs.TAG));
                    if (token != null && token.getTokenID() == XMLTokenIDs.TAG) {
                        if(token.getImage().startsWith("</")) {
                            //found close tag
                            String tag = token.getImage().substring(2).trim(); //cut of '</'
                            int poss = -1;
                            
                            //find whether there is an open tag pair to this close tag
                            while ( token != null){
                                if (token.getTokenID() == XMLTokenIDs.TAG) {
                                    if (token.getImage().substring(1).trim().equals(tag) &&
                                            token.getImage().startsWith("<") &&
                                            !token.getImage().startsWith("</")){
                                        //we found and open tag with the some name as the searched one
                                        
                                        if (poss == 0){
                                            //we are on the some deepness level => this is our pair tag
                                            int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                                            return new int [] {fnw, dotPos};
                                        } else{
                                            //decrease nesting level
                                            poss--;
                                        }
                                    } else {
                                        if (token.getImage().length() > 1 && token.getImage().substring(2).equals(tag)){
                                            //we found an end tag with the some name as the searched one => nest deeper
                                            poss++;
                                        }
                                    }
                                }
                                token = token.getPrevious();
                            }
                        } else {
                            //found an open tag => auto include close tag
                            String tagname = token.getImage().substring(1); //cut '<'
                            //test whether there is a matching close tag,
                            //if so, do not autocomplete.
                            
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
                
            }
            
            // "smart enter" :-) feature
        /*
         <tag>|</tag>
         
         ENTER key pressed =>
         
         <tag>
             |
         </tag>
         
         **/
            if(typedText.charAt(0) == '\n') {
                //enter pressed
                //check following situation:
            /*
             <tag>
             |</tag>
             */
                //1. does the previous line ends with an open tag with tagname X?
                //2. does the token on the carret position is an end tag with X tagname?
                //if both of the assumptions are true then:
                //a. insert endline on the carret position
                //b. indent the new line
                //c. move the cursor to the end of the new line
                
                //check #1
                int endOfPrevLineOffset = Utilities.getRowStart(doc, dotPos) -1 ;
                if(endOfPrevLineOffset > 0) { //do not reformat when enter pressed on the first line
                    TokenItem token = sup.getTokenChain(endOfPrevLineOffset -1 , endOfPrevLineOffset );
                    if(token != null &&
                            token.getTokenID() == XMLTokenIDs.TAG &&
                            token.getImage().equals(">")) {
                        //found an end of a tag -> we needs to decide whether it is an open tag
                        //find tag beninning (skip whitespaces inside the tag (</table >)
                        do {
                            token = token.getPrevious();
                        } while (token != null && token.getTokenID() != XMLTokenIDs.TAG);
                        
                        if(token != null &&
                                token.getTokenID() == XMLTokenIDs.TAG &&
                                token.getImage().startsWith("<") &&
                                !token.getImage().startsWith("</")) {
                            //an open tag
                            String openTagName = token.getImage().substring(1);
                            //check #2
                            
                            boolean applySmartEnter = false;
                            int lineEnd = Utilities.getRowEnd(doc, dotPos);
                            int closingTagOffset = -1;
                            token = sup.getTokenChain(lineEnd - 1, lineEnd);
                            
                            if (token != null && token.getTokenID() == XMLTokenIDs.TAG
                                    && ">".equals(token.getImage())){
                                TokenItem tagNameToken = token.getPrevious();
                                
                                if (tagNameToken != null && tagNameToken.getImage().startsWith("</")){ //NOI18N
                                    String tagName = tagNameToken.getImage().substring("</".length());
                                    
                                    if (tagName.equalsIgnoreCase(openTagName)){
                                        applySmartEnter = true;
                                        closingTagOffset = tagNameToken.getOffset();
                                    }
                                }
                            }
                            
                            if(applySmartEnter) {
                                //found pair end tag => we can do the reformat!!!
                                int currentLineIndex = Utilities.getLineOffset(doc, token.getOffset());
                                //a. insert a new line on the current line
                                doc.atomicLock();
                                try {
                                    doc.insertString( closingTagOffset, "\n" , null);
                                } catch( BadLocationException exc ) {
                                    //do nothing
                                } finally {
                                    doc.atomicUnlock();
                                }
                                
                                //b. indent the new line
                                int newLineOffset = Utilities.getRowStartFromLineOffset(doc, currentLineIndex);
                                int previousLineIndentation = Utilities.getRowIndent(doc, Utilities.getRowStartFromLineOffset(doc, currentLineIndex - 1));
                                int newLineIndent = previousLineIndentation + getShiftWidth();
                                changeRowIndent(doc, newLineOffset, newLineIndent);
                                
                                //c. set cursor to the end of the new line
                                target.setCaretPosition(Math.min(Utilities.getFirstNonWhiteFwd(doc, Utilities.getRowStart(doc, newLineOffset)), 
                                        Utilities.getRowEnd(doc, newLineOffset)));
                                
                                //return end tag line start and end offset to reformat the end tag correctly
                                //get first non white offset from the line after the newly inserted line
                                int start = Utilities.getRowStartFromLineOffset(doc, currentLineIndex+1);
                                int end = Utilities.getRowEnd(doc, start);
                                return new int[]{start, end};
                            }
                        }
                    }
                }
            }
        } catch (Exception e){
            ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
        }
        
        return i;
    }
    
    
    
    
    public class StripEndWhitespaceLayer extends AbstractFormatLayer {
        
        public StripEndWhitespaceLayer() {
            super("xml-strip-whitespace-at-line-end-layer"); // NOI18N
        }
        
        protected FormatSupport createFormatSupport(FormatWriter fw) {
            return new XMLFormatSupport(fw);
        }
        
        public void format(FormatWriter fw) {
            XMLFormatSupport xfs = (XMLFormatSupport)createFormatSupport(fw);
            
            FormatTokenPosition pos = xfs.getFormatStartPosition();
            
            if ( (xfs.isLineStart(pos) == false) ||
                    xfs.isIndentOnly() ) { // don't do anything
                
            } else { // remove end-line whitespace
                while (pos.getToken() != null) {
                    pos = xfs.removeLineEndWhitespace(pos);
                    if (pos.getToken() != null) {
                        pos = xfs.getNextPosition(pos);
                    }
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
