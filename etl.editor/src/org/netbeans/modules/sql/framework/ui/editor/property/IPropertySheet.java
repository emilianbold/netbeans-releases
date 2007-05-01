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

import java.awt.Component;
import java.util.HashMap;
import java.util.List;

import org.openide.nodes.Node;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public interface IPropertySheet {

    /**
     * commit the changes in property to the bean
     */
    public void commitChanges();

    /**
     * get a list of IProperty objects
     * 
     * @return list of IProperty objects
     */
    public List getProperties();

    /**
     * get a property group based on its name
     * 
     * @return property group
     */
    public IPropertyGroup getPropertyGroup(String groupName);

    /**
     * get thr ui component used for displaying properties
     * 
     * @return ui component
     */
    public Component getPropertySheet();

    /**
     * get current property values
     * 
     * @return map of property name and values
     */
    public HashMap getPropertyValues();

    /**
     * set the bean whose properties are reflected
     * 
     * @param bean bean
     */
    public void setBean(Object bean);

    /**
     * set the bean whose properties are reflected. set the template node too.
     * 
     * @param bean bean
     * @param node xml desc node
     */
    public void setBean(Object bean, Node node);

}

