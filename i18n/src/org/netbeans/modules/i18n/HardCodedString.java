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
