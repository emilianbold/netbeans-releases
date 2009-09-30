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
package org.netbeans.modules.mashup.db.ui.wizard;

import java.awt.Toolkit;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * @author Jonathan Giron
 * @version $Revision$
 */
public class ColumnNameTextField extends JTextField {
    
    private static transient final Logger mLogger = Logger.getLogger(ColumnNameTextField.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    /**
     * Extends a plain document to enforce character limitataions for a field name
     * textfield.
     */
    protected class FieldNameDocument extends PlainDocument {
        /**
         * Inserts a string into the text field.
         * 
         * @param offs is the offset to insert
         * @param str is the string to insert
         * @param a is the attribute.
         * @throws BadLocationException if the string cannot be inserted.
         */
        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
            char[] source = str.toCharArray();

            if (offs == 0 && !Character.isLetter(source[0])) {
                // First character of field name must be a letter.
                toolkit.beep();
                return;
            } else if (str.length() == 1) {
                // Check individual char if illegal, beep and refuse if true.
                if (!isValidChar(source[0])) {
                    toolkit.beep();
                    return;
                }
            } else {
                // Must be a pasted string, check all characters and display error message
                // if it contains illegal chars.
                boolean isBadString = false;
                for (int i = 0; i < source.length; i++) {
                    if (!isValidChar(source[i])) {
                        isBadString = true;
                        break;
                    }
                }

                if (isBadString) {
                    toolkit.beep();
                    String nbBundle1 = mLoc.t("BUND209: String({0})contains invalid characters.\nLegal column name characters include letters, numbers, '$' and '#'.",str);
                    DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(nbBundle1.substring(15)));
                    return;
                }
            }

            super.insertString(offs, str.toUpperCase(), a);
        }

        private boolean isValidChar(final char c) {
            return Character.isDigit(c) || Character.isLetter(c) || ('_' == c) || ('$' == c) || ('#' == c);
        }
    }

    private Toolkit toolkit;

    /**
     * Creates a new instance of ColumnNameTextField.
     * 
     * @param value is the value to create with.
     * @param columns is used to construct this object's subclass
     */
    public ColumnNameTextField() {
        toolkit = Toolkit.getDefaultToolkit();
    }

    /**
     * Creates a FieldNameDocument as the default model.
     * 
     * @return Document that is created.
     */
    protected Document createDefaultModel() {
        return new FieldNameDocument();
    }
}

