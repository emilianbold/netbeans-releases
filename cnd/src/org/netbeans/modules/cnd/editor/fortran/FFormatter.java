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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.editor.fortran;

import java.io.IOException;
import java.io.Writer;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Syntax;

import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.AbstractFormatLayer;
import org.netbeans.editor.ext.FormatSupport;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.editor.ext.FormatTokenPosition;
import org.netbeans.editor.ext.FormatWriter;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;

/** Fortran indentation services */
public class FFormatter extends ExtFormatter {

    public FFormatter(Class kitClass) {
        super(kitClass);
    }

    @Override
    protected boolean acceptSyntax(Syntax syntax) {
        return (syntax instanceof FSyntax);
    }
    
    @Override
    protected void initFormatLayers() {
        addFormatLayer(new FortranLayer());
    }
    
    @Override
    public Writer reformat(BaseDocument doc, int startOffset, int endOffset,
            boolean indentOnly) throws BadLocationException, IOException {
        return super.reformat(doc, startOffset, endOffset, indentOnly);
    }

    @Override
    public int reformat(BaseDocument doc, int startOffset, int endOffset)
    throws BadLocationException {
        return super.reformat(doc, startOffset, endOffset);
    }

    
    @Override
    public void shiftLine(BaseDocument doc, int dotPos, boolean right) throws BadLocationException {
        StatusDisplayer.getDefault().setStatusText(
                NbBundle.getBundle(FFormatter.class).getString("MSG_NoFortranShifting")); // NOI18N
    }
    
    @Override
    public int[] getReformatBlock(JTextComponent target, String typedText) {
        int[] ret = null;
        BaseDocument doc = Utilities.getDocument(target);
        int dotPos = target.getCaret().getDot();
        if (doc != null) {
            ret = getKeywordBasedReformatBlock(doc, dotPos, typedText);
            if (ret == null) {
                ret = super.getReformatBlock(target, typedText);
            }
        }
        return ret;
    }

    private int[] getKeywordBasedReformatBlock(BaseDocument doc, int dotPos, String typedText) {
    /* Check whether the user has written the ending 'e'
    * of the first 'else' on the line.
    */
        int[] ret = null;
        if ("e".equals(typedText) || "E".equals(typedText)) { // NOI18N
            try {
                int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                if (checkCase(doc, fnw, "else")) { // NOI18N
                    ret = new int[]{fnw, fnw + 4};
                } else if (checkCase(doc, fnw, "endsubroutine")) { // NOI18N
                    ret = new int[]{fnw, fnw + 13};
                } else if (checkCase(doc, fnw, "end subroutine")) { // NOI18N
                    ret = new int[]{fnw, fnw + 14}; 
                }
            } catch (BadLocationException e) {
            }
         } else if ("o".equals(typedText) || "O".equals(typedText)) { // NOI18N
            try {
                int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                if (checkCase(doc, fnw, "enddo")) { // NOI18N
                    ret = new int[]{fnw, fnw + 5};
                } else if (checkCase(doc, fnw, "end do")) { // NOI18N
                    ret = new int[]{fnw, fnw + 6};
                }
            } catch (BadLocationException e) {
            }
         } else if ("f".equals(typedText) || "F".equals(typedText)) { // NOI18N
            try {
                int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                if (checkCase(doc, fnw, "endif")) { // NOI18N
                    ret = new int[]{fnw, fnw + 5};
                } else if (checkCase(doc, fnw, "end if")) { // NOI18N
                    ret = new int[]{fnw, fnw + 6};
             }
             } catch (BadLocationException e) {
             }
         } else if ("m".equals(typedText) || "M".equals(typedText)) { // NOI18N
             try {
                 int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                 if (checkCase(doc, fnw, "endprogram")) { // NOI18N
                     ret = new int[]{fnw, fnw + 10};
                 } else if (checkCase(doc, fnw, "end program")) { // NOI18N
                     ret = new int[]{fnw, fnw + 11}; 
                 }
              } catch (BadLocationException e) {
              }
        }
        if (ret == null && typedText != null &&
            typedText.length() == 1 && Character.isLetter(typedText.charAt(0))) {
            try {
                int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                if (checkCase(doc, fnw, typedText+"\n") || // NOI18N
                    dotPos == doc.getLength() && checkCase(doc, fnw, typedText)) { // NOI18N
                    ret = new int[]{fnw, fnw + 1};
                }
            } catch (BadLocationException e) {
            }
        }
        return ret;
    }

    private boolean checkCase(BaseDocument doc, int fnw, String what) throws BadLocationException {
        return fnw >= 0 && fnw + what.length() <= doc.getLength() && what.equalsIgnoreCase(doc.getText(fnw, what.length()));
    }
 
    public class FortranLayer extends AbstractFormatLayer {

        public FortranLayer() {
            super("fortran-layer"); // NOI18N
        }

        @Override
        protected FormatSupport createFormatSupport(FormatWriter fw) {
            return new FFormatSupport(fw);
            //StatusDisplayer.getDefault().setStatusText(
            // NbBundle.getBundle(FFormatter.class).getString("MSG_NoFortranReformatting")); // NOI18N
            //return null; 
        }

        public void format(FormatWriter fw) {
            try {
                FFormatSupport ffs = (FFormatSupport) createFormatSupport(fw);
                FormatTokenPosition pos = ffs.getFormatStartPosition();
                if (ffs.getFreeFormat()) {
                    if (ffs.isIndentOnly()) { // create indentation only
                        ffs.indentLine(pos);
                    } else { // regular formatting
                        while (pos != null) {
                            // Indent the current line
                            ffs.indentLine(pos);
                            // Format the line by additional rules
                            //formatLine(ccfs, pos);
                            // Goto next line
                            FormatTokenPosition pos2 = ffs.findLineEnd(pos);
                            if (pos2 == null || pos2.getToken() == null) {
                                break;
                            } // the last line was processed
                            pos = ffs.getNextPosition(pos2, javax.swing.text.Position.Bias.Forward);
                            if (pos == pos2) {
                                break;
                            } // in case there is no next position
                            if (pos == null || pos.getToken() == null) {
                                break;
                            } // there is nothing after the end of line
                            FormatTokenPosition fnw = ffs.findLineFirstNonWhitespace(pos);
                            if (fnw != null) {
                                pos = fnw;
                            } else { // no non-whitespace char on the line
                                pos = ffs.findLineStart(pos);
                            }
                        }
                    } 
                }
            } catch (IllegalStateException e) {
            } 
        }

    } // end class FortranLayer
}
