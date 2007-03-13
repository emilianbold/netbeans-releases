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
 * PropertyData.java
 *
 * Created on October 21, 2004, 9:51 PM
 */

package org.netbeans.modules.css.visual.model;

/**
 * Data Structure to hold the value of a property
 * @author  Winston Prakash
 * @version 1.0
 */
public class PropertyData {

    /**
     * Holds value of property value.
     */
    protected String value=CssStyleData.NOT_SET;


    public String toString(){
        String valueString = value;
        if (value.startsWith(CssStyleData.NOT_SET) || value.startsWith(CssStyleData.VALUE)){
            valueString = "";
        }
        return valueString;
    }

    /** Setter for property value.
     * @param value New value of property value.
     */
    public void setValue(String value) {
        this.value = value;
    }

    public String getValue(){
        return value;
    }

    public boolean isValueInteger(){
        if(Utils.isInteger(value)){
            return true;
        }else{
            return false;
        }
    }


    public boolean hasValue(){
        if (value.equals("") || value.startsWith(CssStyleData.NOT_SET) ||
                value.startsWith(CssStyleData.VALUE)){
            return false;
        }else{
            return true;
        }
    }
}
