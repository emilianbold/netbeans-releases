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
import javax.swing.text.Document;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import org.netbeans.editor.Syntax;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.AbstractFormatLayer;
import org.netbeans.editor.ext.FormatTokenPosition;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.editor.ext.FormatLayer;
import org.netbeans.editor.ext.FormatSupport;
import org.netbeans.editor.ext.ExtFormatSupport;
import org.netbeans.editor.ext.FormatWriter;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.text.syntax.XMLDefaultTokenContext;
import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;
import org.netbeans.modules.xml.text.syntax.XMLTokenIDs;

import org.openide.ErrorManager;

import org.netbeans.modules.xml.text.syntax.javacc.lib.JJEditorSyntax;

/**
 * @author  Marek Fukala
 */
public class XMLFormatter extends ExtFormatter {

    //at least one character
    private static final Pattern VALID_TAG_NAME = Pattern.compile("\\w+"); // NOI18N

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
    
    public Writer reformat(BaseDocument doc, int startOffset, int endOffset,
            boolean indentOnly) throws BadLocationException, IOException {
        
        //TODO: we are not preserving whitespaces yet - make an editor option for that and make it enabled by default
        
        //System.out.println("reformat from "+ startOffset + " to " + endOffset + ": " +doc.getText(startOffset, endOffset-startOffset));
        
        int pos = Utilities.getRowStart(doc, endOffset);
        TokenItem token = null;
        XMLSyntaxSupport sup = (XMLSyntaxSupport)(doc.getSyntaxSupport().get(XMLSyntaxSupport.class));
        
        if (startOffset == endOffset){
            //pressed enter - called from indentNewLine
            //get first non-white token backward
            int nonWSTokenBwd = Utilities.getFirstNonWhiteBwd(doc, endOffset);
            //get first non-white token on the line
            int lineStart = Utilities.getRowStart(doc, nonWSTokenBwd);
            int firstNonWsOffsetOnLine = Utilities.getFirstNonWhiteFwd(doc, lineStart);
            
            token = sup.getTokenChain(firstNonWsOffsetOnLine , firstNonWsOffsetOnLine + 1);
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
            //found something else => indent to the some level
            int previousLineIndentation = Utilities.getRowIndent(doc, endOffset, false);
            changeRowIndent(doc, startOffset, previousLineIndentation);
            return null;
        }
        
        /*TokenItem ti = sup.getTokenChain(startOffset, endOffset + 1);
        do {
            System.out.println(ti);
        } while ((ti = ti.getNext()) != null);
         */
        
        int lastPairTokenRowOffset = -1;
        do {
            try{
                int fnw = Utilities.getRowFirstNonWhite(doc, pos);
                if (fnw == -1) fnw = pos;
                token = sup.getTokenChain(fnw, fnw+1);
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
                                if (token.getTokenID() == XMLTokenIDs.TAG) {
                                    if (token.getImage().substring(1).trim().equals(tag) &&
                                            token.getImage().startsWith("<") &&
                                            !token.getImage().startsWith("</")){
                                        //found an open tag with the searched name e.g. <table>
                                        if (poss == 0){
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
                                                
                                                while (rowOffset > poss){
                                                    deltahelp = Utilities.getRowFirstNonWhite(doc, rowOffset );
                                                    if (deltahelp > -1){
                                                        token = sup.getTokenChain(deltahelp, deltahelp+1);
                                                        if (token != null && (token.getTokenID() != XMLTokenIDs.TEXT) && token.getTokenContextPath().contains(XMLDefaultTokenContext.contextPath)){
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
        
        return null;
    }
    
    public int[] getReformatBlock(JTextComponent target, String typedText) {
        int [] i = super.getReformatBlock(target, typedText);
        
        try{
            BaseDocument doc = Utilities.getDocument(target);
            int dotPos = target.getCaret().getDot();
            XMLSyntaxSupport sup = (XMLSyntaxSupport)(doc.getSyntaxSupport().get(XMLSyntaxSupport.class));
            
            if (typedText.charAt(0) == '>') {
                //get the token before typed text
                TokenItem token = sup.getTokenChain(dotPos-1, dotPos);
                int start = token.getOffset();
                if (token.getTokenContextPath().contains(XMLDefaultTokenContext.contextPath) &&
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
                            token = sup.getTokenChain(dotPos, dotPos + 1);
                            if(token != null &&
                                    token.getTokenID() == XMLTokenIDs.TAG &&
                                    token.getImage().startsWith("</" + openTagName)) {
                                //found pair end tag => we can do the reformat!!!
                                int currentLineIndex = Utilities.getLineOffset(doc, token.getOffset());
                                //a. insert a new line on the current line
                                doc.atomicLock();
                                try {
                                    doc.insertString( dotPos, "\n" , null);
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
                                target.setCaretPosition(Utilities.getRowEnd(doc, newLineOffset));
                                
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
    
}
