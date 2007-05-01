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
package org.netbeans.modules.sql.framework.ui.editor.property;

import java.beans.PropertyEditor;
import java.util.List;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public interface IPropertyCustomizer {

    /**
     * get a list of IOption objects
     * 
     * @return list of options
     */
    public List getOptions();

    /**
     * get the property editor
     * 
     * @return property editor
     */
    public PropertyEditor getPropertyEditor();

    /**
     * get the name of the group where this property belongs
     * 
     * @return name of property group name
     */
    public String getPropertyGroupName();

    /**
     * get the name of the property
     * 
     * @return name of the property
     */
    public String getPropertyName();

    /**
     * set a list of IOption objects
     * 
     * @return list of options
     */
    public void setOptions(List options);

    /**
     * set the property editor
     * 
     * @param editor property editor
     */
    public void setPropertyEditor(PropertyEditor editor);

    /**
     * set the name of the group where this property belongs
     * 
     * @param gName of property group name
     */
    public void setPropertyGroupName(String gName);

    /**
     * set the name of the property
     * 
     * @param name of the property
     */
    public void setPropertyName(String name);
}

