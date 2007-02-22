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

/*
 * StringEditor.java
 *
 * Created on January 23, 2006, 10:05 AM
 *
 */

package org.netbeans.modules.xml.schema.ui.basic.editors;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyEditorSupport;
import org.openide.util.NbBundle;

/**
 * A property editor for String class
 * @author Ajit Bhate
 */
public class StringEditor extends PropertyEditorSupport {

    public final static String EMPTY = "";

    /** Creates a new instance of StringEditor */
    public StringEditor() {
    }
    
    public String getAsText() {
        Object value = super.getValue();
        return value==null?EMPTY:super.getAsText();
    }

    /** sets new value */
    public void setAsText(String s) {
        if ( EMPTY.equals(s) && getValue() == null ) // NOI18N
            return;
        setValue(s);
    }

    public boolean isPaintable() {
        return true;
    }
    
    public void paintValue(Graphics g, Rectangle rectangle) {
        String paintableString=getPaintableString();
        
        FontMetrics metrics=g.getFontMetrics();
        g.drawString(paintableString,rectangle.x,
                rectangle.y+(rectangle.height-metrics.getHeight())/2+
                metrics.getAscent());
    }
    
    protected String getPaintableString() {
        String value=(String)getValue();
        return value==null?NbBundle.getMessage(StringEditor.class,"LBL_Null"):value;
    }
}
