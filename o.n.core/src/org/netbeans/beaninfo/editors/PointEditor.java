/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.beaninfo.editors;

import java.awt.Point;
import org.netbeans.core.UIException;
import org.openide.util.NbBundle;

/** A property editor for Point class.
* @author   Petr Hamernik
* @version  0.10, 21 Jul, 1998
*/
public class PointEditor extends ArrayOfIntSupport {

    public PointEditor() {
        super("java.awt.Point", 2); // NOI18N
    }

    /** Abstract method for translating the value from getValue() method to array of int. */
    int[] getValues() {
        Point p = (Point) getValue();
        return new int[] { p.x, p.y };
    }

    /** Abstract method for translating the array of int to value
    * which is set to method setValue(XXX)
    */
    void setValues(int[] val) {
        if ((val[0] < 0) || (val[1] < 0)) {
            String msg = NbBundle.getMessage(DimensionEditor.class, 
                "CTL_NegativeSize"); //NOI18N
            IllegalArgumentException iae = new IllegalArgumentException (
                "Negative value"); //NOI18N
            UIException.annotateUser(iae, iae.getMessage(), msg, null,
                                     new java.util.Date());
            throw iae;
        }
        else
            setValue(new Point(val[0], val[1]));
    }

    public boolean supportsCustomEditor () {
        return true;
    }

    public java.awt.Component getCustomEditor () {
        return new PointCustomEditor (this, env);
    }

    /** @return the format of value set in property editor. */
    String getHintFormat() {
        return NbBundle.getMessage (PointEditor.class, "CTL_HintFormatPE");
    }

    /** Provides name of XML tag to use for XML persistence of the property value */
    protected String getXMLValueTag () {
        return "Point"; // NOI18N
    }

}
