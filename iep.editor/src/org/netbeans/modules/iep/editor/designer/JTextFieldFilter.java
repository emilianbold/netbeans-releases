/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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

