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
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
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
import org.openide.ErrorManager;
 
/**
 * Formatter for html files.
 * @author Petr Pisl
 */

public class HTMLFormatter extends ExtFormatter {
    
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
    
    
    public Writer reformat(BaseDocument doc, int startOffset, int endOffset,
    boolean indentOnly) throws BadLocationException, IOException {
	if (startOffset == endOffset){
	    return super.reformat(doc, startOffset, endOffset,  indentOnly);
	}
	int pos = Utilities.getRowStart(doc, endOffset);
        TokenItem token = null;
        HTMLSyntaxSupport sup = (HTMLSyntaxSupport)(doc.getSyntaxSupport().get(HTMLSyntaxSupport.class));
        int lastPairTokenRowOffset = -1;
	do {
	    try{
		int fnw = Utilities.getRowFirstNonWhite(doc, pos);
                if (fnw == -1) fnw = pos;
		token = sup.getTokenChain(fnw, fnw+1);
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
                                if (token.getTokenContextPath().contains(HTMLTokenContext.contextPath) &&
                                    HTMLSyntaxSupport.isTag(token)) {
                                    if (token.getImage().trim().equals(tag) &&
                                        token.getTokenID().getNumericID() == HTMLTokenContext.TAG_OPEN_ID){
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
                                                        token = sup.getTokenChain (deltahelp, deltahelp+1);
                                                        if (token != null && token.getTokenContextPath().contains(HTMLTokenContext.contextPath)){
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
                                        else{
                                            poss--;
                                        }
                                    }
                                    else {
                                        if (token.getImage().equals(tag)){
                                            poss++;
                                        }
                                    }
                                }
                                token = token.getPrevious();			
                            }
                            else {
                                    //reset the last found pair token
                                    lastPairTokenRowOffset = -1;
                                    break;
                                }
                            }
                    }
                }
	    }
	    catch (Exception e){
                ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
	    }
	    pos = Utilities.getRowStart(doc, pos-1);
	} while (pos > startOffset && pos > 0);
	    	
	return null;
    }
    
    public int[] getReformatBlock(JTextComponent target, String typedText) {
	int [] i = super.getReformatBlock(target, typedText);
	//System.out.println("getReformatBlock: |" + typedText + "|"); // NOI18N
	if (typedText.charAt(0) == '>') {
	    BaseDocument doc = Utilities.getDocument(target);
	    int dotPos = target.getCaret().getDot();
	    HTMLSyntaxSupport sup = (HTMLSyntaxSupport)(doc.getSyntaxSupport().get(HTMLSyntaxSupport.class));
	    try{
		TokenItem token = sup.getTokenChain(dotPos-1, dotPos);
		int start = token.getOffset();		
		if (token.getTokenID().getNumericID() == HTMLTokenContext.TAG_CLOSE_SYMBOL_ID){
		    do {
			token = token.getPrevious();
		    } while (token != null && !sup.isTag(token));
		    if (token != null && token.getTokenID().getNumericID() == HTMLTokenContext.TAG_CLOSE_ID){
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
				    }
				    else{
					poss--;
				    }
				}
				else {
				    if (token.getImage().equals(tag)){
					poss++;
				    }
				}
			    }
			    token = token.getPrevious();			
			}
		    }
		}
	    }
	    catch (Exception e){
                ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
	    }
	    
	}
	
	return i;
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
}
