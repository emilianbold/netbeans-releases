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

package org.netbeans.modules.iep.editor.designer;

import javax.swing.text.PlainDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;

/**
 *  This class validates text for textboxes, textareas, etc.
 *
 *  @author Bing Lu
 */
public class JTextFieldFilter extends PlainDocument {

    /**
     *  The lowercase letters
     */
    public static final String LOWERCASE_CHARS = "abcdefghijklmnopqrstuvwxyz";
    //public static final JTextFieldFilter LOWERCASE = new JTextFieldFilter(LOWERCASE_CHARS);

    public static JTextFieldFilter newLowercase() {
        return new JTextFieldFilter(LOWERCASE_CHARS);
    }
    
    /**
     *  The uppercase letters
     */
    public static final String UPPERCASE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    //public static final JTextFieldFilter UPPERCASE = new JTextFieldFilter(UPPERCASE_CHARS);

    public static JTextFieldFilter newUppercase() {
        return new JTextFieldFilter(UPPERCASE_CHARS);
    }
    
    /**
     *  The lower + upper case letters
     */
    public static final String ALPHA_CHARS = LOWERCASE_CHARS + UPPERCASE_CHARS;
    //public static final JTextFieldFilter ALPHA = new JTextFieldFilter(ALPHA_CHARS);

    public static JTextFieldFilter newAlpha() {
        return new JTextFieldFilter(ALPHA_CHARS);
    }
    
    /**
     *  The numbers
     */
    public static final String NUMERIC_CHARS = "0123456789";
    //public static final JTextFieldFilter NUMERIC = new JTextFieldFilter(NUMERIC_CHARS);

    public static JTextFieldFilter newNumeric() {
        return new JTextFieldFilter(NUMERIC_CHARS);
    }
    
    /**
     *  The floating point format
     */
    public static final String FLOAT_CHARS = NUMERIC_CHARS + ".";
    //public static final JTextFieldFilter FLOAT = new JTextFieldFilter(FLOAT_CHARS);

    public static JTextFieldFilter newFloat() {
        return new JTextFieldFilter(FLOAT_CHARS);
    }
    
    public static final String FLOAT_CHARS_EXP = FLOAT_CHARS + "E" ;
    //public static final JTextFieldFilter FLOAT = new JTextFieldFilter(FLOAT_CHARS);

    public static JTextFieldFilter newFloatExp() {
        return new JTextFieldFilter(FLOAT_CHARS_EXP);
    }
    
    /**
     *  The alphanumeric combination
     */
    public static final String ALPHA_NUMERIC_CHARS = ALPHA_CHARS + NUMERIC_CHARS;
    
    public static JTextFieldFilter newAlphaNumeric() {
        return new JTextFieldFilter(ALPHA_NUMERIC_CHARS);
    }
    
    /**
     *  The alphanumeric combination
     */
    public static final String ALPHA_FLOAT_CHARS = ALPHA_CHARS + FLOAT_CHARS;

    public static JTextFieldFilter newAlphaFloat() {
        return new JTextFieldFilter(ALPHA_FLOAT_CHARS);
    }
    
    public static final String ALPHA_FLOAT_UNDERSCORE_CHARS = ALPHA_CHARS + FLOAT_CHARS + "_" + " ";

    public static JTextFieldFilter newAlphaFloatUnderscore() {
        return new JTextFieldFilter(ALPHA_FLOAT_UNDERSCORE_CHARS);
    }
    
    public static final String ALPHA_FLOAT_UNDERSCORE_SPACE_CHARS = ALPHA_FLOAT_UNDERSCORE_CHARS + " ";

    public static JTextFieldFilter newAlphaFloatUnderscoreSpace() {
        return new JTextFieldFilter(ALPHA_FLOAT_UNDERSCORE_SPACE_CHARS);
    }
    
    public static final String ALPHA_NUMERIC_UNDERSCORE_SPACE_CHARS = ALPHA_NUMERIC_CHARS + "_ ";

    public static JTextFieldFilter newAlphaNumericUnderscoreSpace() {
        return new JTextFieldFilter(ALPHA_NUMERIC_UNDERSCORE_SPACE_CHARS);
    }
    
    public static final String ALPHA_NUMERIC_UNDERSCORE_CHARS = ALPHA_NUMERIC_CHARS + "_";

    public static JTextFieldFilter newAlphaNumericUnderscore() {
        return new JTextFieldFilter(ALPHA_NUMERIC_UNDERSCORE_CHARS);
    }
    
    /**
     *  The accepted characters
     */
    protected String acceptedChars = null;
    /**
     *  The not accepted characters
     */
    protected boolean negativeAccepted = false;

    protected boolean emptyAccepted = false;

    /**
     *  Constructor for the JTextFieldFilter object
     *
     * @param  acceptedchars  This ...
     */
    public JTextFieldFilter(String acceptedchars) {
        acceptedChars = acceptedchars;
    }


    /**
     *  Sets the negativeAccepted attribute of the JTextFieldFilter object
     *
     * @param  negativeaccepted  The new negativeAccepted value
     */
    public void setNegativeAccepted(boolean negativeaccepted) {
        if (acceptedChars.equals(NUMERIC_CHARS) 
                || acceptedChars.equals(FLOAT_CHARS) 
                || acceptedChars.equals(ALPHA_NUMERIC_CHARS)) {
            negativeAccepted = negativeaccepted;
            acceptedChars += "-";
        }
    }

    public void setEmptyAccepted(boolean emptyAccepted) {
        this.emptyAccepted = emptyAccepted;
    }
    

    /**
     *  This method
     *
     * @param  offset                    This ...
     * @param  str                       This ...
     * @param  attr                      This ...
     * @exception  BadLocationException  Thrown when ...
     */
    public void insertString(int offset, String str, AttributeSet attr)
             throws BadLocationException {
        if (emptyAccepted && str.equals("")) {
            if (offset != 0) {
                return;
            }
        } else {       
                     
            if ((str == null) || str.equals("")) {
                return;
            }
            
            // FIXME!
            // Legal java name
            if (acceptedChars.equals(ALPHA_NUMERIC_CHARS)) {
                if (offset == 0) {
                    if (ALPHA_CHARS.indexOf(
                        str.valueOf(str.charAt(0))) == -1) {
                        return;
                    }
                }
            }
            
            if (acceptedChars.equals(UPPERCASE_CHARS)) {
                str = str.toUpperCase();
            } else if (acceptedChars.equals(LOWERCASE_CHARS)) {
                str = str.toLowerCase();
            }
    
            for (int i = 0; i < str.length(); i++) {
                if (acceptedChars.indexOf(str.valueOf(str.charAt(i))) == -1) {
                    return;
                }
            }
    
            if (acceptedChars.equals(FLOAT_CHARS) 
                || (acceptedChars.equals(FLOAT_CHARS + "-") && negativeAccepted)) {
                if (str.indexOf(".") != -1) {
                    if (getText(0, getLength()).indexOf(".") != -1) {
                        return;
                    }
                }
            }
    
            if (negativeAccepted && str.indexOf("-") != -1) {
                if (str.indexOf("-") != 0 || offset != 0) {
                    return;
                }
            }
        }

        super.insertString(offset, str, attr);
    }
}

