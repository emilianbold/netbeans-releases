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

package org.netbeans.modules.uml.core.reverseengineering.reframework;

import java.util.List;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 */
public class RERealization implements IRERealization
{
    private Node m_GeneralizationNode = null;
    private List m_Children           = null;

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IREGeneralization#getDOMNode()
     */
    public Node getDOMNode()
    {
        return m_GeneralizationNode;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IREGeneralization#setDOMNode(org.dom4j.Node)
     */
    public void setDOMNode(Node value)
    {
        m_GeneralizationNode = value;
    }

    /**
     * Retrieves the number of super classes.
     * @param pVal [out] The number of super classes available.
     */
    public int getCount()
    {
        if (m_GeneralizationNode == null) return 0;

        return (m_Children != null?
                    m_Children
                  : (m_Children = XMLManip.selectNodeList(
                                 m_GeneralizationNode, "Interface"))).size();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IREGeneralization#item(int)
     */
    public IREInterface item(int index)
    {
        Node n = (Node)
                (m_Children != null?
                    m_Children
                  : (m_Children = XMLManip.selectNodeList(
                                         m_GeneralizationNode, "Interface")))
                                  .get(index);
        if (n != null)
        {
            IREInterface intf = new REInterface();
            intf.setDOMNode(n);
            return intf;
        }
        return null;
    }
}
