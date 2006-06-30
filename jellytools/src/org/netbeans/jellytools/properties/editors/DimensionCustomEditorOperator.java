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
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.*;

// editor for Dimension
/** Class implementing all necessary methods for handling Dimension Custom Editor */
public class DimensionCustomEditorOperator extends NbDialogOperator {

    JTextFieldOperator _txtFieldWidth;
    JTextFieldOperator _txtFieldHeight;

    /** Creates a new instance of DimensionCustomEditorOperator
     * @param title String title of custom editor */    
    public DimensionCustomEditorOperator(String title) {
        super(title);
    }
    
    /** Creates a new instance of DimensionCustomEditorOperator
     * @param wrapper JDialogOperator wrapper for custom editor */    
    public DimensionCustomEditorOperator(JDialogOperator wrapper) {
        super((JDialog)wrapper.getSource());
    }
    
    /** sets dimension value
     * @param width int width
     * @param height int height */    
    public void setDimensionValue(String width, String height) {
        txtFieldWidth().setText(width);
        txtFieldHeight().setText(height);
    }
    
    /** returns width value
     * @return ind width */    
    public String getWidthValue() {
        return txtFieldWidth().getText();
    }

    /** sets width value
     * @param value int width */    
    public void setWidthValue(String value) {
        txtFieldWidth().setText(value);
    }
    
    /** returns height value
     * @return int height */    
    public String getHeightValue() {
        return txtFieldHeight().getText();
    }

    /** sets height value
     * @param value int height */    
    public void setHeightValue(String value) {
        txtFieldHeight().setText(value);
    }
    
    /** getter for height text field operator
     * @return JTextFieldOperator */    
    public JTextFieldOperator txtFieldWidth() {
        if(_txtFieldWidth==null) {
            _txtFieldWidth = new JTextFieldOperator(this, 0);
        }
        return _txtFieldWidth;
    }
    
    /** getter for width text field operator
     * @return JTextFieldOperator */    
    public JTextFieldOperator txtFieldHeight() {
        if(_txtFieldHeight==null) {
            _txtFieldHeight = new JTextFieldOperator(this, 1);
        }
        return _txtFieldHeight;
    }
    
    /** Performs verification by accessing all sub-components */    
    public void verify() {
        txtFieldHeight();
        txtFieldWidth();
    }
    
}
