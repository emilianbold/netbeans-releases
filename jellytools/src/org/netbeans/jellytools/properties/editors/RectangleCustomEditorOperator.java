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

// editor for Rectangle
/** Class implementing all necessary methods for handling Rectangle Custom Editor
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @version 1.0 */
public class RectangleCustomEditorOperator extends NbDialogOperator {

    JTextFieldOperator _txtFieldX;
    JTextFieldOperator _txtFieldY;
    JTextFieldOperator _txtFieldWidth;
    JTextFieldOperator _txtFieldHeight;
    
    /** creates new RectangleCustomEditorOperator
     * @param title String title of custom editor */    
    public RectangleCustomEditorOperator(String title) {
        super(title);
    }
    
    /** creates new RectangleCustomEditorOperator
     * @param wrapper JDialogOperator wrapper for custom editor */    
    public RectangleCustomEditorOperator(JDialogOperator wrapper) {
        super((JDialog)wrapper.getSource());
    }
    
    /** setter for edited rectangle value
     * @param x int x
     * @param y int y
     * @param width int width
     * @param height int height */    
    public void setRectangleValue(String x, String y, String width, String height) {
        txtFieldX().setText(x);
        txtFieldY().setText(y);
        txtFieldWidth().setText(width);
        txtFieldHeight().setText(height);
    }
    
    /** getter for edited X value
     * @return int X */    
    public String getXValue() {
        return txtFieldX().getText();
    }

    /** setter for edited X value
     * @param value int X */    
    public void setXValue(String value) {
        txtFieldX().setText(value);
    }
    
    /** getter for edited Y value
     * @return int Y */    
    public String getYValue() {
        return txtFieldY().getText();
    }

    /** setter for edited Y value
     * @param value int Y */    
    public void setYValue(String value) {
        txtFieldY().setText(value);
    }
    
    /** getter for edited Width value
     * @return int Width */    
    public String getWidthValue() {
        return txtFieldWidth().getText();
    }

    /** setter for edited Width value
     * @param value int Width */    
    public void setWidthValue(String value) {
        txtFieldWidth().setText(value);
    }
    
    /** getter for edited Height value
     * @return int Height */    
    public String getHeightValue() {
        return txtFieldHeight().getText();
    }

    /** setter for edited Height value
     * @param value int Height */    
    public void setHeightValue(String value) {
        txtFieldHeight().setText(value);
    }
    
    /** getter for X JTextFieldOperator
     * @return JTextFieldOperator */    
    public JTextFieldOperator txtFieldX() {
        if(_txtFieldX==null) {
            _txtFieldX = new JTextFieldOperator(this, 0);
        }
        return _txtFieldX;
    }
    
    /** getter for Y JTextFieldOperator
     * @return JTextFieldOperator */    
    public JTextFieldOperator txtFieldY() {
        if(_txtFieldY==null) {
            _txtFieldY = new JTextFieldOperator(this, 1);
        }
        return _txtFieldY;
    }
    
    /** getter for Width JTextFieldOperator
     * @return JTextFieldOperator */    
    public JTextFieldOperator txtFieldWidth() {
        if(_txtFieldWidth==null) {
            _txtFieldWidth = new JTextFieldOperator(this, 2);
        }
        return _txtFieldWidth;
    }
    
    /** getter for Height JTextFieldOperator
     * @return JTextFieldOperator */    
    public JTextFieldOperator txtFieldHeight() {
        if(_txtFieldHeight==null) {
            _txtFieldHeight = new JTextFieldOperator(this, 3);
        }
        return _txtFieldHeight;
    }
    
    /** Performs verification by accessing all sub-components */    
    public void verify() {
        txtFieldHeight();
        txtFieldWidth();
        txtFieldX();
        txtFieldY();
    }
    
}
