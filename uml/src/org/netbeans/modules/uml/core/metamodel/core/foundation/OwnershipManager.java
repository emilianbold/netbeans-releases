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


package org.netbeans.modules.uml.core.metamodel.core.foundation;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 * OwnershipManager used to simply control a temporary connection between the context
 * ( owner ) element and the element to be owned. Once an OwnershipManager object goes
 * out of scope, the connection is broken.
 */
public class OwnershipManager
{

    private IElement m_Owner = null;
    private IElement m_Child = null;
    private String m_TempElementName = null;

    /**
     * @param package1
     * @param packImport
     */
    public OwnershipManager(IElement owner, IElement child)
    {
        super();
        setOwnershipManager(owner, child, "UML:TempArea");
        createTempOwnership();
    }
    
    /**
     *
     */
    private void createTempOwnership()
    {
        String query = m_TempElementName +  "/*" ;
        ContactManager.addChild(m_Owner, m_TempElementName, query, m_Child);
    }
    
    public void setOwnershipManager(IElement owner, IElement child, String elemName)
    {
        m_Owner = owner;
        m_Child = child;
        m_TempElementName = elemName;
    }
    
    /**
     *
     */
    private OwnershipManager()
    {
        super();
    }
    
    protected void finalize()
    {
        stopManagingElement();
    }
    
    public void stopManagingElement()
    {
        try
        {
            if (m_Owner != null)
            {
                org.dom4j.Node node = m_Owner.getNode();
                org.dom4j.Node remNode = XMLManip.selectSingleNode(node, m_TempElementName);
                if (remNode != null)
                {
                    remNode.detach();
                    //node.removeChild(remNode);
                }
            }
        }
        catch (Throwable e)
        {
        }
        finally
        {
            m_Owner = null;
        }
    }
    
}
