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

package org.netbeans.modules.cnd.editor.fortran;

import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;

import org.netbeans.editor.TokenItem;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseImageTokenID;
import org.netbeans.editor.BaseTokenID;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.Syntax;

import org.netbeans.editor.ext.AbstractFormatLayer;
import org.netbeans.editor.ext.FormatTokenPosition;
import org.netbeans.editor.ext.FormatSupport;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.editor.ext.FormatWriter;

/**
* F indentation services are located here
*
*  duped from editor/libsrc/org/netbeans/editor/ext/java/JavaFormatter.java
*/

public class FFormatter extends ExtFormatter {

    public FFormatter(Class kitClass) {
        super(kitClass);
    }

    protected boolean acceptSyntax(Syntax syntax) {
        return (syntax instanceof FSyntax);
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
                    if (fnw >= 0 && fnw + 14 <= doc.getLength() &&
                        ("else".equals(doc.getText(fnw, (dotPos-fnw))) || // NOI18N
                         "endtype".equals(doc.getText(fnw, 7)) || // NOI18N
                         "end type".equals(doc.getText(fnw, 8)) || // NOI18N
                         "endwhere".equals(doc.getText(fnw, 8)) || // NOI18N
                         "end where".equals(doc.getText(fnw, 9)) || // NOI18N
                         "elsewhere".equals(doc.getText(fnw, 9)) || // NOI18N
                         "endmodule".equals(doc.getText(fnw, 9))  || // NOI18N
                         "end module".equals(doc.getText(fnw, 10))|| // NOI18N
                         "endinterface".equals(doc.getText(fnw, 12)) || // NOI18N
                         "end interface".equals(doc.getText(fnw, 13))|| // NOI18N
                         "endstructure".equals(doc.getText(fnw, 12)) || // NOI18N
                         "end structure".equals(doc.getText(fnw, 13))|| // NOI18N
                         "endsubroutine".equals(doc.getText(fnw, 13))|| // NOI18N
                         "end subroutine".equals(doc.getText(fnw, 14)))) { // NOI18N
                        ret = new int[] { fnw, dotPos };
                    }
                } catch (BadLocationException e) {
                }
            } else if ("f".equals(typedText)) { // NOI18N
                try {
                    int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                    if (fnw >= 0 && fnw + 7 <= doc.getLength() &&
                        ("endif".equals(doc.getText(fnw, 5))  || // NOI18N
                         "end if".equals(doc.getText(fnw, 6)) || // NOI18N
                         "else if".equals(doc.getText(fnw, 7)))) { // NOI18N
                        ret = new int[] { fnw, dotPos };
                    }
                } catch (BadLocationException e) {
                }
            } else if ("k".equals(typedText)) { // NOI18N
                try {
                    int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                    if (fnw >= 0 && fnw + 9 <= doc.getLength() &&
                        ("endblock".equals(doc.getText(fnw, 8)) || // NOI18N
                         "end block".equals(doc.getText(fnw, 9)))) { // NOI18N
                        ret = new int[] { fnw, dotPos };
                    }
                } catch (BadLocationException e) {
                }
            } else if ("l".equals(typedText)) { // NOI18N
                try {
                    int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                    if (fnw >= 0 && fnw + 10 <= doc.getLength() &&
                        ("endforall".equals(doc.getText(fnw, 9)) || // NOI18N
                         "end forall".equals(doc.getText(fnw, 10)))) { // NOI18N
                        ret = new int[] { fnw, dotPos };
                    }
                } catch (BadLocationException e) {
                }
            } else if ("m".equals(typedText)) { // NOI18N
                try {
                    int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                    if (fnw >= 0 && fnw + 11 <= doc.getLength() &&
                        ("endprogram".equals(doc.getText(fnw, 10)) || // NOI18N
                         "end program".equals(doc.getText(fnw, 11)))) { // NOI18N
                        ret = new int[] { fnw, dotPos };
                    }
                } catch (BadLocationException e) {
                }
            } else if ("n".equals(typedText)) { // NOI18N
                try {
                    int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                    if (fnw >= 0 && fnw + 12 <= doc.getLength() &&
                        ("endunion".equals(doc.getText(fnw, 8)) || // NOI18N
                         "end union".equals(doc.getText(fnw, 9)) || // NOI18N
                         "endfunction".equals(doc.getText(fnw, 11)) || // NOI18N
                         "end function".equals(doc.getText(fnw, 12)))) { // NOI18N
                        ret = new int[] { fnw, dotPos };
                    }
                } catch (BadLocationException e) {
                }
            } else if ("o".equals(typedText)) { // NOI18N
                try {
                    int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                    if (fnw >= 0 && fnw + 6 <= doc.getLength() &&
                        ("enddo".equals(doc.getText(fnw, 5)) || // NOI18N
                         "end do".equals(doc.getText(fnw, 6)))) { // NOI18N
                        ret = new int[] { fnw, dotPos };
                    }
                } catch (BadLocationException e) {
                }
            } else if ("p".equals(typedText)) { // NOI18N
                try {
                    int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                    if (fnw >= 0 && fnw + 7 <= doc.getLength() &&
                        ("endmap".equals(doc.getText(fnw, 6)) || // NOI18N
                         "end map".equals(doc.getText(fnw, 7)))) { // NOI18N
                        ret = new int[] { fnw, dotPos };
                    }
                } catch (BadLocationException e) {
                }
            } else if ("t".equals(typedText)) { // NOI18N
                try {
                    int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                    if (fnw >= 0 && fnw + 10 <= doc.getLength() &&
                        ("endselect".equals(doc.getText(fnw, 9)) || // NOI18N
                         "end select".equals(doc.getText(fnw, 10)))) { // NOI18N
                        ret = new int[] { fnw, dotPos };
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
        addFormatLayer(new FortranLayer());
    }
    
    public FormatSupport createFormatSupport(FormatWriter fw) {
        return new FFormatSupport(fw);
    }
    
    public class StripEndWhitespaceLayer extends AbstractFormatLayer {

        public StripEndWhitespaceLayer() {
            super("fortran-strip-whitespace-at-line-end"); // NOI18N
        }

        protected FormatSupport createFormatSupport(FormatWriter fw) {
            return new FFormatSupport(fw);
        }

        public void format(FormatWriter fw) {
            FFormatSupport fs = (FFormatSupport)createFormatSupport(fw);

            FormatTokenPosition pos = fs.getFormatStartPosition();

            if (fs.isIndentOnly()) { 
		fs.indentLine(pos);
            } else { // remove end-line whitespace
                while (pos.getToken() != null) {
                    pos = fs.removeLineEndWhitespace(pos);
                    if (pos.getToken() != null) {
                        pos = fs.getNextPosition(pos);
                    }
                }
            }
        }

    }
    
    public class FortranLayer extends AbstractFormatLayer {

        public FortranLayer() {
            super("fortran-layer"); // NOI18N
        }

        protected FormatSupport createFormatSupport(FormatWriter fw) {
            return new FFormatSupport(fw);
        }

        public void format(FormatWriter fw) {
            try {
                FFormatSupport fs = (FFormatSupport)createFormatSupport(fw);

                FormatTokenPosition pos = fs.getFormatStartPosition();

                if (fs.isIndentOnly()) {  
		    // create indentation only
                    fs.indentLine(pos);

                } else { // regular formatting

                    while (pos != null) {

                        // Indent the current line
                        fs.indentLine(pos);

                        // Format the line by additional rules defined 
			// by settings in fortran indent engine
			// such as, space after comma, etc.
			// XXX not needed yet
			//   formatLine(fs, pos);

                        // Goto next line
                        FormatTokenPosition pos2 = fs.findLineEnd(pos);
                        if (pos2 == null || pos2.getToken() == null)
                            break; // the last line was processed
                        
                        pos = fs.getNextPosition(pos2, javax.swing.text.Position.Bias.Forward);
                        if (pos == pos2)
                            break; // in case there is no next position
                        if (pos == null || pos.getToken() == null)
                            break; // there is nothing after the end of line
                        
                        FormatTokenPosition fnw = fs.findLineFirstNonWhitespace(pos);
                        if (fnw != null) {
                          pos = fnw;
                        } else { // no non-whitespace char on the line
                          pos = fs.findLineStart(pos);
                        }
                    }
                }
            } catch (IllegalStateException e) {
            }
        }
	/*
	 * this method is used for formatting a line of code with code style
	 * changes, such as, putting a space after a comma. 
	 * XXX not needed yet
        protected void formatLine(FFormatSupport fs, FormatTokenPosition pos) {
            TokenItem token = fs.findLineStart(pos).getToken();
            while (token != null) {
                if (token.getTokenContextPath() == fs.getTokenContextPath()) {
                    switch (token.getTokenID().getNumericID()) {
                           
                        case FTokenContext.COMMA_ID:
			    if (fs.getFormatSpaceAfterComma()) {
                                TokenItem nextToken = token.getNext();
				// insert a space if one isn't already there
				if (nextToken.getTokenID() != FTokenContext.WHITESPACE) {
                                    fs.insertToken(nextToken, 
                                                     fs.getValidWhitespaceTokenID(), 
						     fs.getWhitespaceTokenContextPath(), 
						     " "); //NOI18N
				 }
                            }
                            break;
                    } // end switch
                }
                token = token.getNext();
            } //end while loop
        } //end formatLine()
*/
    } // end class FortranLayer
}
