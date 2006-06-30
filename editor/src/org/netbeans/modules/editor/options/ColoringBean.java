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

package org.netbeans.modules.editor.options;

import java.awt.Font;
import java.awt.Color;
import java.beans.*;

import org.netbeans.editor.Coloring;

public class ColoringBean implements java.io.Serializable {

    /** Encapsulated Coloring */
    transient Coloring coloring;

    /** example text */
    transient String example;

    /** Default Coloring */
    transient Coloring defaultColoring;

    boolean isDefault;

    public ColoringBean() {
    }

    public ColoringBean(Coloring coloring, String example, Coloring defaultColoring, boolean isDefault) {
        this.coloring = coloring;
        this.example = example;
        this.defaultColoring = defaultColoring;
        this.isDefault = isDefault;
    }

    public ColoringBean changeColoring( Coloring newColoring ) {
        return new ColoringBean( newColoring, example, defaultColoring, isDefault );
    }

    public boolean equals( Object o ) {
        if( o instanceof ColoringBean ) {
            ColoringBean c = (ColoringBean)o;
            return (
                       ( ( (coloring == null) && (c.coloring == null) ) ||
                         ( (coloring != null) && coloring.equals(c.coloring) ) ) &&
                       ( ( (example == null) && (c.example == null) ) ||
                         ( (example != null) && example.equals(c.example) ) ) &&
                       ( ( (defaultColoring == null) && (c.defaultColoring == null) ) ||
                         ( (defaultColoring != null) && defaultColoring.equals(c.defaultColoring) ) ) &&
                       (isDefault == c.isDefault) );
        }
        return false;
    }

}
