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
package org.netbeans.modules.dm.virtual.db.ui.wizard;

import java.awt.Toolkit;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;


import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * @author Ahimanikya Satapathy
 */
public class ColumnNameTextField extends JTextField {

    protected class FieldNameDocument extends PlainDocument {

        @Override
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
                    DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Message(NbBundle.getMessage(ColumnNameTextField.class, "MSG_Invalid_Characters", str)));
                    return;
                }
            }

            super.insertString(offs, str.toUpperCase(), a);
        }

        private boolean isValidChar(final char c) {
            return Character.isDigit(c) || Character.isLetter(c) || ('_' == c) || ('$' == c) || ('#' == c); // NOI18N
        }
    }
    private Toolkit toolkit;

    public ColumnNameTextField() {
        toolkit = Toolkit.getDefaultToolkit();
    }

    @Override
    protected Document createDefaultModel() {
        return new FieldNameDocument();
    }
}

