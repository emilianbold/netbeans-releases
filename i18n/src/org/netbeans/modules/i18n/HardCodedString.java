/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.i18n;


import javax.swing.text.Position;


/**
 * Object representing found hard coded string in internationalized document.
 * @author  Peter Zavadsky
 */
public class HardCodedString extends Object {

    /** Actual text representing hard coded string. */
    private String text;
    
    /** Start position of hard coded string. */
    private Position startPosition;
    
    /** End position of hard coded string. */
    private Position endPosition;
    
    
    /** Creates new <code>HardCodedString</code>. */
    public HardCodedString(String text, Position startPosition, Position endPosition) {
        this.text = text;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }
    

    /** Getter for hard coded value. Text without double quotes. */
    public String getText() {
        return text;
    }

    /** Getter for start position.  */
    public Position getStartPosition() {
        return startPosition;
    }

    /** Getter for end position. */
    public Position getEndPosition() {
        return endPosition;
    }
    
    /** Gets length of hard coded string double quotes included. */
    public int getLength() {
        return endPosition.getOffset() - startPosition.getOffset();
    }
}
