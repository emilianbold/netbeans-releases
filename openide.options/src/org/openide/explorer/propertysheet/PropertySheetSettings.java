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
package org.openide.explorer.propertysheet;

import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import java.awt.Color;

import javax.swing.UIManager;


/**
* Settings for the property sheet.
* @see PropertySheet
*
* @deprecated None of the settings in this class are supported in the new property sheet.  The entire implementation
*            has been gutted to do nothing.
*
* @author Jan Jancura, Ian Formanek
* @version 0.11, May 16, 1998
*/
public class PropertySheetSettings extends SystemOption {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -3820718202747868830L;

    /** Property variables. */
    static int propertyPaintingStyle = -1;
    static boolean plastic = false;

    /** When it's true only writable properties are showen. */
    static boolean displayWritableOnly = false;
    static int sortingMode = -1;
    static Color valueColor = null;
    private static Color disabledColor;
    static PropertySheetSettings propertySheetSettings = null;

    static PropertySheetSettings getDefault() {
        return propertySheetSettings;
    }

    public String displayName() {
        return null;
    }

    public HelpCtx getHelpCtx() {
        return null;
    }

    /*
    * Sets property showing mode.
    * @deprecated Relic of the original property sheet implementation.  Display of properties
    * is handled by the look and feel.
    */
    public void setPropertyPaintingStyle(int style) {
    }

    /*
    * Returns mode of showing properties.
    * @deprecated Relic of the original property sheet implementation.  Display of properties
    * is handled by the look and feel.
    *
    * @return <CODE>int</CODE> mode of showing properties.
    * @see #setExpert
    */
    public int getPropertyPaintingStyle() {
        return -1;
    }

    /*
    * Sets sorting mode.
    *
    * @param sortingMode New sorting mode.
    */
    public void setSortingMode(int sortingMode) {
    }

    /*
    * Returns sorting mode.
    *
    * @return Sorting mode.
    */
    public int getSortingMode() {
        return -1;
    }

    /*
    * Sets buttons in sheet to be plastic.
    * @deprecated Relic of the original property sheet implementation.  Display of properties
    * is handled by the look and feel.
    */
    public void setPlastic(boolean plastic) {
    }

    /*
    * Returns true if buttons in sheet are plastic.
    * @deprecated Relic of the original property sheet implementation.  Display of properties
    * is handled by the look and feel.
    */
    public boolean getPlastic() {
        return false;
    }

    /*
    * Sets foreground color of values.
    * @deprecated Relic of the original property sheet implementation.  Display of properties
    * is handled by the look and feel.
    */
    public void setValueColor(Color color) {
    }

    /*
    * Gets foreground color of values.
    */
    public Color getValueColor() {
        return null;
    }

    /*
    * Sets foreground color of disabled property.
    * @deprecated Relic of the original property sheet implementation.  Display of properties
    * is handled by the look and feel.
    */
    public void setDisabledPropertyColor(Color color) {
    }

    /*
    * Gets foreground color of values.
    * @deprecated Relic of the original property sheet implementation.  Display of properties
    * is handled by the look and feel.
    */
    public Color getDisabledPropertyColor() {
        return null;
    }

    /*
    * Setter method for visibleWritableOnly property. If is true only writable
    * properties are showen in propertysheet.
    */
    public void setDisplayWritableOnly(boolean b) {
    }

    /*
    * Getter method for visibleWritableOnly property. If is true only writable
    * properties are showen in propertysheet.
    * @deprecated Relic of the original property sheet implementation.  The new propertysheet
     * implementation does not support this kind of filtering of properties.    */
    public boolean getDisplayWritableOnly() {
        return false;
    }
}
