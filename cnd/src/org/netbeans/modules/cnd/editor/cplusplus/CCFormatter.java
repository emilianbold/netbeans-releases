/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.editor.cplusplus;

import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;

import org.netbeans.editor.TokenItem;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.Syntax;

import org.netbeans.editor.ext.AbstractFormatLayer;
import org.netbeans.editor.ext.FormatTokenPosition;
import org.netbeans.editor.ext.FormatSupport;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.editor.ext.FormatWriter;

/**
* CC indentation services are located here
*
*  duped from editor/libsrc/org/netbeans/editor/ext/java/JavaFormatter.java
*/

public class CCFormatter extends ExtFormatter {

    public CCFormatter(Class kitClass) {
        super(kitClass);
    }

    protected boolean acceptSyntax(Syntax syntax) {
        return (syntax instanceof CCSyntax);
    }

    public int[] getReformatBlock(JTextComponent target, String typedText) {
        int[] ret = null;
	BaseDocument doc = Utilities.getDocument(target);
        int dotPos = target.getCaret().getDot();
        if (doc != null) {
            /* Check whether the user has written the ending 'e'
             * of the first 'else' on the line.
             */
            if ("e".equals(typedText)) { // NOI18N
                try {
                    int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                    if (fnw >= 0 && fnw + 4 == dotPos
                        && "else".equals(doc.getText(fnw, 4)) // NOI18N
                    ) {
                        ret = new int[] { fnw, fnw + 4 };
                    }
                } catch (BadLocationException e) {
                }

            } else if (":".equals(typedText)) { // NOI18N
                try {
                    int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                    if (fnw >= 0 && fnw + 4 <= doc.getLength()
                        && "case".equals(doc.getText(fnw, 4)) // NOI18N
                    ) {
                        ret = new int[] { fnw, fnw + 4 };
                    } else {
                        if (fnw >= 0 & fnw + 7 <= doc.getLength()
                            && "default".equals(doc.getText(fnw, 7)) // NOI18N
                        ) {
                            ret = new int[] {fnw, fnw + 7 };
                        }
                    }
                } catch (BadLocationException e) {
                }
            
            } else {
                ret = super.getReformatBlock(target, typedText);
            }
        }
        
        return ret;
    }
    
    protected void initFormatLayers() {
        addFormatLayer(new StripEndWhitespaceLayer());
        addFormatLayer(new CCLayer());
    }
    
    public FormatSupport createFormatSupport(FormatWriter fw) {
        return new CCFormatSupport(fw);
    }
    
    public class StripEndWhitespaceLayer extends AbstractFormatLayer {

        public StripEndWhitespaceLayer() {
            super("cc-strip-whitespace-at-line-end"); // NOI18N
        }

        protected FormatSupport createFormatSupport(FormatWriter fw) {
            return new CCFormatSupport(fw);
        }

        public void format(FormatWriter fw) {
            CCFormatSupport ccfs = (CCFormatSupport)createFormatSupport(fw);

            FormatTokenPosition pos = ccfs.getFormatStartPosition();

            if (ccfs.isIndentOnly()) { 
		// don't do anything
            } else { // remove end-line whitespace
                while (pos.getToken() != null) {
                    pos = ccfs.removeLineEndWhitespace(pos);
                    if (pos.getToken() != null) {
                        pos = ccfs.getNextPosition(pos);
                    }
                }
            }
        }

    }
    
    public class CCLayer extends AbstractFormatLayer {

        public CCLayer() {
            super("cc-layer"); // NOI18N
        }

        protected FormatSupport createFormatSupport(FormatWriter fw) {
            return new CCFormatSupport(fw);
        }

        public void format(FormatWriter fw) {
            try {
                CCFormatSupport ccfs = (CCFormatSupport)createFormatSupport(fw);

                FormatTokenPosition pos = ccfs.getFormatStartPosition();

                if (ccfs.isIndentOnly()) {  // create indentation only
                    ccfs.indentLine(pos);

                } else { // regular formatting

                    while (pos != null) {

                        // Indent the current line
                        ccfs.indentLine(pos);

                        // Format the line by additional rules
                        formatLine(ccfs, pos);

                        // Goto next line
                        FormatTokenPosition pos2 = ccfs.findLineEnd(pos);
                        if (pos2 == null || pos2.getToken() == null)
                            break; // the last line was processed
                        
                        pos = ccfs.getNextPosition(pos2, javax.swing.text.Position.Bias.Forward);
                        if (pos == pos2)
                            break; // in case there is no next position
                        if (pos == null || pos.getToken() == null)
                            break; // there is nothing after the end of line
                        
                        FormatTokenPosition fnw = ccfs.findLineFirstNonWhitespace(pos);
                        if (fnw != null) {
                          pos = fnw;
                        } else { // no non-whitespace char on the line
                          pos = ccfs.findLineStart(pos);
                        }
                    }
                }
            } catch (IllegalStateException e) {
            }
        }


        protected void formatLine(CCFormatSupport ccfs, FormatTokenPosition pos) {
            TokenItem token = ccfs.findLineStart(pos).getToken();
            while (token != null) {
/*                if (jfs.findLineEnd(jfs.getPosition(token, 0)).getToken() == token) {
                    break; // at line end
                }
 */
                if (token.getTokenContextPath() == ccfs.getTokenContextPath()) {
                    switch (token.getTokenID().getNumericID()) {
                        case CCTokenContext.LBRACE_ID: // '{'
                            if (!ccfs.isIndentOnly()) {
                            if (ccfs.getFormatNewlineBeforeBrace()) {
                                FormatTokenPosition lbracePos = ccfs.getPosition(token, 0);
                                // Look for first important token in backward direction
                                FormatTokenPosition imp = ccfs.findImportant(lbracePos,
                                        null, true, true); // stop on line start
                                if (imp != null && imp.getToken().getTokenContextPath()
                                                        == ccfs.getTokenContextPath()) {
                                    switch (imp.getToken().getTokenID().getNumericID()) {
                                        case CCTokenContext.BLOCK_COMMENT_ID:
                                        case CCTokenContext.LINE_COMMENT_ID:
                                            break; // comments are ignored

                                        default:
                                            // Check whether it isn't a "{ }" case
                                            FormatTokenPosition next = ccfs.findImportant(
                                                    lbracePos, null, true, false);
                                            if (next == null || next.getToken() == null ||
                                                next.getToken().getTokenID() != CCTokenContext.RBRACE) {
                                                // Insert new-line
                                                if (ccfs.canInsertToken(token)) {
                                                    ccfs.insertToken(token, ccfs.getValidWhitespaceTokenID(),
								     ccfs.getValidWhitespaceTokenContextPath(), "\n"); // NOI18N
                                                    ccfs.removeLineEndWhitespace(imp);
                                                    // bug fix: 10225 - reindent newly created line
                                                    ccfs.indentLine(lbracePos);
                                                }

                                                token = imp.getToken();
                                            }
                                            break;
                                    }// end switch
                                }

                            } else {
                                FormatTokenPosition lbracePos = ccfs.getPosition(token, 0);
                                
                                // Check that nothing exists before "{"
                                if (ccfs.findNonWhitespace(lbracePos, null, true, true) != null)
                                    break;
                                // Check that nothing exists after "{", but ignore comments
                                if (ccfs.getNextPosition(lbracePos) != null)
                                    if (ccfs.findImportant(ccfs.getNextPosition(lbracePos), null, true, false) != null)
                                        break;
                                
                                // check that on previous line is some stmt
                                FormatTokenPosition ftp = ccfs.findLineStart(lbracePos); // find start of current line
                                FormatTokenPosition endOfPreviousLine = ccfs.getPreviousPosition(ftp); // go one position back - means previous line
                                if (endOfPreviousLine == null || 
				    endOfPreviousLine.getToken().getTokenID() != CCTokenContext.WHITESPACE)
                                    break;
                                ftp = ccfs.findLineStart(endOfPreviousLine); // find start of the previous line - now we have limit position
                                ftp = ccfs.findImportant(lbracePos, ftp, false, true); // find something important till the limit
                                if (ftp == null)
                                    break;
                                
                                // check that previous line does not end with "{" or line comment
                                ftp = ccfs.findNonWhitespace(endOfPreviousLine, null, true, true);
                                if (ftp.getToken().getTokenID() == CCTokenContext.LINE_COMMENT ||
                                    ftp.getToken().getTokenID() == CCTokenContext.LBRACE)
                                    break;

                                // now move the "{" to the end of previous line
                                boolean remove = true;
                                while (remove)
                                {
                                    if (token.getPrevious() == endOfPreviousLine.getToken())
                                        remove = false;
                                    if (ccfs.canRemoveToken(token.getPrevious()))
                                        ccfs.removeToken(token.getPrevious());
                                    else
                                        break;  // should never get here!
                                }
                                // insert one space before "{"
                                if (ccfs.canInsertToken(token))
                                    ccfs.insertSpaces(token, 1);
                            }
                            } // !jfs.isIndentOnly()
                            break;

                        case CCTokenContext.LPAREN_ID:
                            if (ccfs.getFormatSpaceBeforeParenthesis()) {
                                TokenItem prevToken = token.getPrevious();
                                if (prevToken != null && prevToken.getTokenID() == CCTokenContext.IDENTIFIER) {
                                    if (ccfs.canInsertToken(token)) {
                                        ccfs.insertToken(token, ccfs.getWhitespaceTokenID(),
							 ccfs.getWhitespaceTokenContextPath(), " "); // NOI18N
                                    }
                                }
                            } else {
                                // bugfix 9813: remove space before left parenthesis
                                TokenItem prevToken = token.getPrevious();
                                if (prevToken != null && prevToken.getTokenID() == CCTokenContext.WHITESPACE &&
                                        prevToken.getImage().length() == 1) {
                                    TokenItem prevprevToken = prevToken.getPrevious();
                                    if (prevprevToken != null && prevprevToken.getTokenID() == CCTokenContext.IDENTIFIER)
                                    {
                                        if (ccfs.canRemoveToken(prevToken)) {
                                            ccfs.removeToken(prevToken);
                                        }
                                    }
                                }
                            }
                            break;
                           
                        case CCTokenContext.COMMA_ID:
			    TokenItem nextToken = token.getNext();
                            if( nextToken != null ) {
                                if (ccfs.getFormatSpaceAfterComma()) {
                                    // insert a space if one isn't already there
                                    if (nextToken.getTokenID() != CCTokenContext.WHITESPACE) {
                                        ccfs.insertToken(nextToken, 
                                                         ccfs.getValidWhitespaceTokenID(), 
                                                         ccfs.getWhitespaceTokenContextPath(), 
                                                         " "); //NOI18N
                                     }
                                } else {
                                    if (nextToken.getTokenID() == CCTokenContext.WHITESPACE) {
                                        ccfs.removeToken(nextToken);
                                    }
                                }
                            }
                            break;
                    } // end switch
                }
                token = token.getNext();
            } //end while loop
        } //end formatLine()
    } // end class CCLayer
}
