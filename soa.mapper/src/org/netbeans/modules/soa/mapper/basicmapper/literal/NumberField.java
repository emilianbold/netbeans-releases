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

package org.netbeans.modules.soa.mapper.basicmapper.literal;

import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;


/**
 * A dialog which displays a number field
 * @author rshankar
 * @version 1.0
 */
public class NumberField extends JTextField {
    /**
     * @param cols number of columns
     */
    public NumberField(int cols) {
        super(cols);
    }

    /**
     * @return Document document associated with the textfield
     */
    protected Document createDefaultModel() {
        return new NumberDocument();
    }
 
    private static class NumberDocument extends PlainDocument {
        public void insertString(int offs, String str, AttributeSet a) 
          throws BadLocationException {
            if (str == null) {
                return;
            }
            AbstractDocument.Content content = getContent();
            String oldStr = content.getString(0, content.length());
            char[] chars = str.toCharArray();
            String s = "";
            boolean foundNumber = false;
            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];
                if (c == '.') {
                    s += c;
                    if (oldStr.indexOf(".") == -1) {
                        foundNumber = true;
                    }
                } else if ((oldStr.indexOf("-") == -1 && offs == 0 && i == 0 && c == '-') 
                    || (c >= '0' && c <= '9')) {
                    s += c;
                    foundNumber = true;
                } else {
                    break;
                }
            }
            if (!foundNumber) {
                s = "";
            }
            super.insertString(offs, s, a);
        }
    }
}
