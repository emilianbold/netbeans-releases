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

package data;

import java.awt.*;
import java.io.Serializable;

/**
 *  Simple visual javabean
 *
 * @author Jiri Vagner
 */
public class TestVisualBean extends Canvas implements Serializable {
    
    private Color textColor = Color.blue;
    private String text = "Lancia Lybra"; // NOI18N
    
    /** Creates a new instance of VisualTestBean */
    public TestVisualBean() {
        resize(100,40);
    }
    
    public void paint(Graphics g) {
        g.setColor(Color.blue);
        g.drawString(text,10, 10);
    }
    
    /**
     * Returns color value
     * @return Color value
     */
    public Color getColor() {
        return textColor;
    }
    
    /**
     * Sets new color
     * @param newColor value
     */
    public void setColor(Color newColor) {
        textColor = newColor;
        repaint();
    }
    
    /**
     * Returns text
     * @return text
     */
    public String getText() {
        return text;
    }
    
    /**
     * Sets text value
     * @param newText 
     */
    public void setText(String newText) {
        text = newText;
        repaint();
    }
}