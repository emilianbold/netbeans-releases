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

package org.netbeans.modules.form;

import org.openide.nodes.Node;

/**
 * This interface can be implemented by property editors to express that the
 * current property value in the property editor is a
 * bean with its properties and to make these properties accessible.
 *
 * @author Tomas Stupka
 *
 */
public interface BeanPropertyEditor {

    /**
     * @return true if the current value is a bean not directly
     *         suported by the editor
     */
    public boolean valueIsBeanProperty();

    /**
    * Called to initialize the editor with a specified type. If succesfull,
    * the value should be available via the getValue method.     
    * An Exception should be thrown when the value cannot be set.
     
    * @param class type to initialize the editor with
    * @exception Exception thrown when the value cannot be set
    */
    public void intializeFromType(Class type) throws Exception;
    
    /**
     * @return properties from the current value
     */
    public Node.Property[] getProperties();
    
}
