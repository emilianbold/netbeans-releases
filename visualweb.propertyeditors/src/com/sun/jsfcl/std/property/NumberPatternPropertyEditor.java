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
package com.sun.jsfcl.std.property;

import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.PropertyEditor2;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyEditorSupport;
import java.text.DecimalFormat;
import javax.swing.JComboBox;

/**
 * @author eric
 *
 * @deprecated 
 */
public class NumberPatternPropertyEditor extends PropertyEditorSupport implements PropertyEditor2 {
    
    private DesignProperty designProperty;
    private String pattern;
    
    public void setDesignProperty( DesignProperty designProperty ) {
        this.designProperty = designProperty;
    }
    
    public Object getVaue() {
        return pattern;
    }
    
    public void setValue( Object value ) {
        pattern = (String)value;
        super.setValue( value );
    }
    public String getAsText() {
        return pattern;
    }
    
    public void setAsText(String string) {
        if( string != null && string.trim().length() != 0 ) {
            // validate the pattern string
            try {
                DecimalFormat format = (DecimalFormat)DecimalFormat.getInstance();
                format.applyPattern(string);
                pattern = string.trim();
            } catch( IllegalArgumentException e ) {
                throw new LocalizedMessageRuntimeException("Invalid pattern : \"" + string + "\"");
            }
        }
        else
            pattern = null;

        super.setValue( pattern );
    }
    
    public String getJavaInitializationString() {
        
        return "\"" + getAsText() + "\"";
    }
    
    public boolean supportsCustomEditor() {
        
        return true;
    }
    
    public Component getCustomEditor() {
        
        NumberPatternPanel customizerPanel = new NumberPatternPanel(this, designProperty);
        return customizerPanel;
    }
}
