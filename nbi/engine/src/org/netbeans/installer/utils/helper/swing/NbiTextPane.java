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
 *
 * $Id$
 */
package org.netbeans.installer.utils.helper.swing;

import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import org.netbeans.installer.utils.helper.Text;
import org.netbeans.installer.utils.helper.Text.ContentType;

/**
 *
 * @author Kirill Sorokin
 */
public class NbiTextPane extends JTextPane {
    public NbiTextPane() {
        super();
        
        setEditable(false);
        setBorder(new EmptyBorder(0, 0, 0, 0));
        
        putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, true);
    }
    
    public void setText(String text) {
        if ((text == null) || text.equals("")) {
            if (getContentType().equals("text/plain")) {
                super.setText(" ");
            } else if (getContentType().equals("text/html")) {
                super.setText("&nbsp;");
            }
        } else {
            super.setText(text);
        }
    }
    
    public void setText(Text text) {
        setContentType(text.getContentType());
        setText(text.getText());
    }
    
    public void setText(CharSequence chars) {
        setText(chars.toString());
    }
    
    public void clearText() {
        setText("");
    }
    
    public void setContentType(ContentType contentType) {
        super.setContentType(contentType.toString());
    }
}
