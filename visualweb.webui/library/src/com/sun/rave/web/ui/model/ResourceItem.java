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
package com.sun.rave.web.ui.model;

/**
 * This interface describes a selectable item in the filehooser listbox.
 * The item has 5 values which can be implemented based on the resource type
 * in question:
 * a) An Object representing the value of the item
 * b) A key that will be used as the value of the <option> tag in the
 *    filechooser listbox
 * c) A label that will be the label of the <option> tag described above.
 * d) A boolean flag indicating whether the option should be disabled or not.
 * e) A boolean flag indicating if the resource being represented by this
 *    item is a container resource or a child resource. In the realm of
 *    of File systems this would translate to directory and file respectively.
 *
 *
 * @author deep
 */

public interface ResourceItem {

    /**
     * Returns an object representing the value of this resource item.
     * For the default case of the FileChooser this would be a File
     * object.
     *
     * @return an object which is the value of the ResourceItem.
     */
    public Object getItemValue();
    
    /**
     * Returns a String representing the item key.
     * 
     *
     * @return returns an object representing the resource item
     */
    public String getItemKey();    
    
    /**
     * Set the item key.
     * 
     *
     * @param key - the resource item key
     */
    public void setItemKey(String key);
        
    /**
     * Returns an object representing the resource item.
     * 
     *
     * @return returns an object representing the resource item
     */
    public String getItemLabel();
        
    /**
     * Returns an object representing the resource item.
     * 
     *
     * @return returns an object representing the resource item
     */
    public void setItemLabel(String label);
        
    /**
     * Returns an boolean value indicating if the item should be selectable
     * in the filechooser's listbox.
     * 
     *
     * @return true if the item in the listbox should be disabled. 
     */
    public boolean isItemDisabled();
        
    /**
     * Sets the item disabled flag. If set to true the item should 
     * not be selectable.
     * 
     * 
     *
     * @enabled flag when set to true indicates item is not selectable.
     */
    public void setItemDisabled(boolean disabled);
        
    /**
     * Returns a flag indicating if the resource item is a container. If true 
     * the item is a container item.
     * 
     *
     * @return true if the item is a container, false otherwise.
     */
    public boolean isContainerItem();

    public boolean equals(Object resourceItem);
        
}
