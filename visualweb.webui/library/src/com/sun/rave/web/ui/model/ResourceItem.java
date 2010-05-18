/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
