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

package org.netbeans.installer;

/**
 *
 * @author  vlado
 */
public class DestinationItem {
    
    /**
     * Holds value of property value.
     */
    private String value;
    
    /**
     * Holds value of property description.
     */
    private String description;
    
    /** Creates a new instance of DestinationItem */
    public DestinationItem() {
    }
    
    public DestinationItem(java.lang.String value, java.lang.String description) {
        this.value = value;
        this.description = description;
    }
    
    /**
     * Getter for property value.
     * @return Value of property value.
     */
    public String getValue() {
        return this.value;
    }
    
    /**
     * Setter for property value.
     * @param value New value of property value.
     */
    public void setValue(String value) {
        this.value = value;
    }
    
    /**
     * Getter for property description.
     * @return Value of property description.
     */
    public String getDescription() {
        return this.description;
    }
    
    /**
     * Setter for property description.
     * @param description New value of property description.
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
}
