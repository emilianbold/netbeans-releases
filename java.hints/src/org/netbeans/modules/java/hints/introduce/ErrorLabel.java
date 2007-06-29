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
package org.netbeans.modules.java.hints.introduce;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.openide.util.Utilities;

/**
 * A special label that displays an error message if the text in the document it is
 * tracking is not valid.
 * 
 * @author S. Aubrecht
 */
class ErrorLabel extends JLabel {
    
    /**
     * Property that is fired when the valid/invalid status of the tracked text document changes.
     */
    public static final String PROP_IS_VALID = "isValid";
    
    private Document document;
    private Validator validator;
    private boolean isValid = true;
    
    /** Creates a new instance of InputErrorDisplayer 
     * @param doc Document to track for editing changes, e.g. from a JTextField
     * @param validator The logic that decides whether the text is valid or not.
     */
    public ErrorLabel( Document doc, Validator validator ) {
        setText( null );
        setIcon( null );
        
        assert null != doc;
        assert null != validator;
        
        this.document = doc;
        this.validator = validator;
        
        doc.addDocumentListener( new DocumentListener() {
            public void insertUpdate(DocumentEvent arg0) {
                revalidateText();
            }

            public void removeUpdate(DocumentEvent arg0) {
                revalidateText();
            }

            public void changedUpdate(DocumentEvent arg0) {
                revalidateText();
            }
        });
    }
    
    /**
     * @return True if the text in the tracked document is valid.
     */
    public boolean isInputTextValid() {
        return isValid;
    }
    
    protected void revalidateText() {
        boolean oldStatus = isValid;
        String errMessage = null;
        try     {
            errMessage = validator.validate( document.getText( 0, document.getLength() ) );
        } catch (BadLocationException ex) {
            //ignore
            return;
        }
        isValid = errMessage == null;
        setText( errMessage );
        setIcon( null == errMessage ? null : getErrorIcon() );
            
        firePropertyChange( PROP_IS_VALID, oldStatus, isValid);
    }
    
    protected Icon getErrorIcon() {
        return new ImageIcon( Utilities.loadImage("org/netbeans/modules/java/editor/resources/error-glyph.gif") );
    }
    
    /**
     * Validates the given text.
     */
    public static interface Validator {
        /**
         * Check the given text and return error message if the text is not valid or null if there are no errors.
         * @param text Text to be checked for errors.
         * @return Error message to be displayed to the user or null if there no errors in the document.
         */
        public String validate( String text );
    }
}
