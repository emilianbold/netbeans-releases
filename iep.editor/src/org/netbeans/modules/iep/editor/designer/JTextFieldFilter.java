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

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.text.PlainDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;

import org.openide.util.NbBundle;

/**
 *  This class validates text for textboxes, textareas, etc.
 *
 *  @author Bing Lu
 */
public class JTextFieldFilter extends PlainDocument {

    private JTextField mTextField;
    
    private Popup popup;
    
    private JLabel msgContent;
    
    private JPanel messagePanel;
    
    /**
     *  The lowercase letters
     */
    public static final String LOWERCASE_CHARS = "abcdefghijklmnopqrstuvwxyz";
    //public static final JTextFieldFilter LOWERCASE = new JTextFieldFilter(LOWERCASE_CHARS);

//    public static JTextFieldFilter newLowercase() {
//        return new JTextFieldFilter(LOWERCASE_CHARS);
//    }
    
    /**
     *  The uppercase letters
     */
    public static final String UPPERCASE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    //public static final JTextFieldFilter UPPERCASE = new JTextFieldFilter(UPPERCASE_CHARS);

//    public static JTextFieldFilter newUppercase() {
//        return new JTextFieldFilter(UPPERCASE_CHARS);
//    }
    
    /**
     *  The lower + upper case letters
     */
    public static final String ALPHA_CHARS = LOWERCASE_CHARS + UPPERCASE_CHARS;
    //public static final JTextFieldFilter ALPHA = new JTextFieldFilter(ALPHA_CHARS);

//    public static JTextFieldFilter newAlpha() {
//        return new JTextFieldFilter(ALPHA_CHARS);
//    }
    
    /**
     *  The numbers
     */
    public static final String NUMERIC_CHARS = "0123456789";
    //public static final JTextFieldFilter NUMERIC = new JTextFieldFilter(NUMERIC_CHARS);

    public static JTextFieldFilter newNumeric(JTextField textField) {
        return new JTextFieldFilter(NUMERIC_CHARS, textField);
    }
    
    /**
     *  The floating point format
     */
    public static final String FLOAT_CHARS = NUMERIC_CHARS + ".";
    //public static final JTextFieldFilter FLOAT = new JTextFieldFilter(FLOAT_CHARS);

    public static JTextFieldFilter newFloat(JTextField textField) {
        return new JTextFieldFilter(FLOAT_CHARS, textField);
    }
    
    public static final String FLOAT_CHARS_EXP = FLOAT_CHARS + "E" ;
    //public static final JTextFieldFilter FLOAT = new JTextFieldFilter(FLOAT_CHARS);

    public static JTextFieldFilter newFloatExp(JTextField textField) {
        return new JTextFieldFilter(FLOAT_CHARS_EXP, textField);
    }
    
    /**
     *  The alphanumeric combination
     */
    public static final String ALPHA_NUMERIC_CHARS = ALPHA_CHARS + NUMERIC_CHARS;
    
    public static JTextFieldFilter newAlphaNumeric(JTextField textField) {
        return new JTextFieldFilter(ALPHA_NUMERIC_CHARS, textField);
    }
    
    /**
     *  The alphanumeric combination
     */
//    public static final String ALPHA_FLOAT_CHARS = ALPHA_CHARS + FLOAT_CHARS;

//    public static JTextFieldFilter newAlphaFloat() {
//        return new JTextFieldFilter(ALPHA_FLOAT_CHARS);
//    }
    
    public static final String ALPHA_FLOAT_UNDERSCORE_CHARS = ALPHA_CHARS + FLOAT_CHARS + "_" + " ";

//    public static JTextFieldFilter newAlphaFloatUnderscore() {
//        return new JTextFieldFilter(ALPHA_FLOAT_UNDERSCORE_CHARS);
//    }
    
    public static final String ALPHA_FLOAT_UNDERSCORE_SPACE_CHARS = ALPHA_FLOAT_UNDERSCORE_CHARS + " ";

//    public static JTextFieldFilter newAlphaFloatUnderscoreSpace() {
//        return new JTextFieldFilter(ALPHA_FLOAT_UNDERSCORE_SPACE_CHARS);
//    }
    
    public static final String ALPHA_NUMERIC_UNDERSCORE_SPACE_CHARS = ALPHA_NUMERIC_CHARS + "_ ";

//    public static JTextFieldFilter newAlphaNumericUnderscoreSpace() {
//        return new JTextFieldFilter(ALPHA_NUMERIC_UNDERSCORE_SPACE_CHARS);
//    }
    
    public static final String ALPHA_NUMERIC_UNDERSCORE_CHARS = ALPHA_NUMERIC_CHARS + "_";

//    public static JTextFieldFilter newAlphaNumericUnderscore() {
//        return new JTextFieldFilter(ALPHA_NUMERIC_UNDERSCORE_CHARS);
//    }
    
    public static JTextFieldFilter newAlphaNumericUnderscore(JTextField textField) {
        return new JTextFieldFilter(ALPHA_NUMERIC_UNDERSCORE_CHARS, textField);
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
     * @param textField owner text field
     */
    public JTextFieldFilter(String acceptedchars, JTextField textField) {
        acceptedChars = acceptedchars;
        mTextField = textField;
        mTextField.addFocusListener(new TextFieldFocusListener());
        init();
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
        boolean isCharValid = true;
        
        //by default we want to validate names
        boolean validate = true;
        
        //by default we always validate names even for non english locale
        //if system property iep.editor.validateNamesInTextField
        //is set to false then we will stop validating names
        String validateName = System.getProperty("iep.editor.validateNamesInTextField", "true");
        
        
        //if system property iep.editor.validateNamesInTextField  is set to false 
        //then we will not validate
        if(validateName != null && validateName.equals("false")) {
            validate = false;
        }
        
        if(validate) {
            if(popup != null) {
                popup.hide();
            }
            
            if (emptyAccepted && str.equals("")) {
                if (offset != 0) {
                    isCharValid = false;
                }
            } else {       
                         
                if ((str == null) || str.equals("")) {
                    isCharValid = false;
                }
                
                // FIXME!
                // Legal java name
                if (acceptedChars.equals(ALPHA_NUMERIC_CHARS)) {
                    if (offset == 0) {
                        if (ALPHA_CHARS.indexOf(
                            str.valueOf(str.charAt(0))) == -1) {
                            isCharValid = false;
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
                        isCharValid = false;
                    }
                }
        
                if (acceptedChars.equals(FLOAT_CHARS) 
                    || (acceptedChars.equals(FLOAT_CHARS + "-") && negativeAccepted)) {
                    if (str.indexOf(".") != -1) {
                        if (getText(0, getLength()).indexOf(".") != -1) {
                            isCharValid = false;
                        }
                    }
                }
        
                if (negativeAccepted && str.indexOf("-") != -1) {
                    if (str.indexOf("-") != 0 || offset != 0) {
                        isCharValid = false;
                    }
                }
            }
        }
        

        if(isCharValid) {
            super.insertString(offset, str, attr);
        } else {
            if(mTextField != null) {
                msgContent.setText("'" + str + "'");
                
                Point p = new Point(mTextField.getX(), mTextField.getY() + mTextField.getHeight() + 2);
                SwingUtilities.convertPointToScreen(p, mTextField.getParent());
                popup = PopupFactory.getSharedInstance().getPopup(null, messagePanel, p.x, p.y);
                popup.show();
            }
        }
        
    }
    
    private void init() {
        String msgStr = NbBundle.getMessage(JTextFieldFilter.class, "JTextFieldFilter.InvalidCharMessage");
        JLabel msg = new JLabel(msgStr + " ");
        msgContent = new JLabel();
        msgContent.setForeground(Color.RED);
        messagePanel = new JPanel();
        messagePanel.setBorder(BorderFactory.createLineBorder(Color.RED));
        messagePanel.setLayout(new FlowLayout());
        messagePanel.add(msg);
        messagePanel.add(msgContent);
    }
    
    class TextFieldFocusListener implements FocusListener {

        public void focusGained(FocusEvent e) {
        }

        public void focusLost(FocusEvent e) {
            if(popup != null) {
                popup.hide();
            }
        }
        
    }
}

