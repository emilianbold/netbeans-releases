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

package org.openide.nodes;

import java.beans.*;

/**
 * @author  phrebejk
 */
public class BadBeanHidden extends Object implements java.io.Serializable {

    /** Holds value of property indexedProperty. */
    private String[] indexedProperty;

    /** Creates new BadBeanHidden */
    public BadBeanHidden() {

    }

    /** Indexed getter for property indexedProperty.
     * @param index Index of the property.
     * @return Value of the property at <CODE>index</CODE>.
     */
    public String getIndexedProperty(int index) {
        return this.indexedProperty[index];
    }    
    
    /** Indexed setter for property indexedProperty.
     * @param index Index of the property.
     * @param indexedProperty New value of the property at <CODE>index</CODE>.
     
    public void setIndexedProperty(int index, String indexedProperty) {
        this.indexedProperty[index] = indexedProperty;
    }
    */    
    
}
