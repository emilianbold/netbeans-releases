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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.jellytools.properties.editors;

import javax.swing.JDialog;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling String Custom Editor
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @version 1.0 */
public class StringCustomEditorOperator extends NbDialogOperator {

    private JTextAreaOperator _txtArea;

    /** Creates new StringCustomEditorOperator
     * @param title String title of custom editor */
    public StringCustomEditorOperator(String title) {
        super(title);
    }
    
    /** Creates new StringCustomEditorOperator
     * @param wrapper JDialogOperator wrapper for custom editor */    
    public StringCustomEditorOperator(JDialogOperator wrapper) {
        super((JDialog)wrapper.getSource());
    }
    
    /** setter for edited String value
     * @return String */    
    public String getStringValue() {
        return txtArea().getText();
    }
    
    /** getter for edited String value
     * @param text String */    
    public void setStringValue(String text) {
        txtArea().setText(text);
    }
    
    /** getter for JTextFieldOperator with edited text
     * @return JTextFieldOperator */    
    public JTextAreaOperator txtArea() {
        if(_txtArea==null) {
            _txtArea = new JTextAreaOperator(this);
        }
        return _txtArea;
    }
        
    /** Performs verification by accessing all sub-components */    
    public void verify() {
        txtArea();
    }

}
