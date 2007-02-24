/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

/*
 * File       : Facility.java
 * Created on : Oct 23, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework;

import org.dom4j.Node;


/**
 * @author Aztec
 */
public class Facility implements IFacility
{

    /**
    * The is the XML DOM node that represents this element.
    */
    Node  m_Node;

    String  m_Description = null;

    IFacilityProperties m_Properties = new FacilityProperties();

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacility#addProperty(org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacilityProperty)
     */
    public void addProperty(IFacilityProperty pProperty)
    {
        if(pProperty != null)
        {
            m_Properties.add(pProperty);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacility#addProperty(java.lang.String, java.lang.String)
     */
    public void addProperty(String name, String value)
    {
        if(name != null && value != null)
        {
            IFacilityProperty prop = new FacilityProperty();
            prop.setName(name);
            prop.setValue(value);
            addProperty(prop);
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacility#getDescription()
     */
    public String getDescription()
    {
        return m_Description;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacility#getProperties()
     */
    public IFacilityProperties getProperties()
    {
        return m_Properties;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacility#getProperty(java.lang.String)
     */
    public IFacilityProperty getProperty(String name)
    {
        return m_Properties.get(name);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacility#getPropertyValue(java.lang.String)
     */
    public String getPropertyValue(String name)
    {
        IFacilityProperty prop = getProperty(name);
        if(prop != null) return prop.getValue();
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacility#setDescription(java.lang.String)
     */
    public void setDescription(String value)
    {
        m_Description = value;
    }

}
