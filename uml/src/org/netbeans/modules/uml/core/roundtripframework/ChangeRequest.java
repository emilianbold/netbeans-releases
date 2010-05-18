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

/*
 * File       : ChangeRequest.java
 * Created on : Oct 28, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;

/**
 * @author Aztec
 */
public class ChangeRequest implements IChangeRequest
{
    IElement        m_Before = null;
    IElement        m_After = null;
    int             m_ChangeType;
    int             m_Detail;
    int             m_ElementType;
    String          m_Language = null;
    IRelationProxy  m_Relation = null;
    IEventPayload   m_Payload = null;
    
    public ChangeRequest()
    {
        m_ChangeType = ChangeKind.CT_NONE;
        m_ElementType = RTElementKind.RCT_NONE;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IChangeRequest#getAfter()
     */
    public IElement getAfter()
    {
        return m_After;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IChangeRequest#getBefore()
     */
    public IElement getBefore()
    {
        return m_Before;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IChangeRequest#getElementType()
     */
    public int getElementType()
    {
         return m_ElementType;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IChangeRequest#getLanguage()
     */
    public String getLanguage()
    {
        return m_Language;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IChangeRequest#getPayload()
     */
    public IEventPayload getPayload()
    {
        return m_Payload;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IChangeRequest#getRelation()
     */
    public IRelationProxy getRelation()
    {
        return m_Relation;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IChangeRequest#getRequestDetailType()
     */
    public int getRequestDetailType()
    {
        return m_Detail;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IChangeRequest#getState()
     */
    public int getState()
    {
        return m_ChangeType;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IChangeRequest#setAfter(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
    public void setAfter(IElement element)
    {
        m_After = element;
        determineElementType(m_After);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IChangeRequest#setBefore(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
    public void setBefore(IElement element)
    {
        m_Before = element;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IChangeRequest#setElementType(int)
     */
    public void setElementType(int rtElementKind)
    {
        m_ElementType = rtElementKind;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IChangeRequest#setLanguage(java.lang.String)
     */
    public void setLanguage(String language)
    {
        m_Language = language;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IChangeRequest#setPayload(org.netbeans.modules.uml.core.eventframework.IEventPayload)
     */
    public void setPayload(IEventPayload payload)
    {
        m_Payload = payload;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IChangeRequest#setRelation(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy)
     */
    public void setRelation(IRelationProxy relation)
    {
        m_Relation = relation;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IChangeRequest#setRequestDetailType(int)
     */
    public void setRequestDetailType(int requestDetailKind)
    {
        m_Detail = requestDetailKind;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IChangeRequest#setState(int)
     */
    public void setState(int changeKind)
    {
        m_ChangeType = changeKind;
    }
    
    /**
     *
     * Sets the element type of this request.
     *
     * @param element[in] The element to check against.
     *
     * @return HRESULT
     *
     */

    protected void determineElementType(IElement element)
    {
        if(element == null) return;
        
        String type = element.getElementType();        
        int elKind = RTElementKind.RCT_NONE;

        if( "Attribute".equals(type))
        {
            elKind = RTElementKind.RCT_ATTRIBUTE;
        }
        else if("Class".equals(type))
        {
            elKind = RTElementKind.RCT_CLASS;
        }
        else if("ParameterableElement".equals(type))
        {
           elKind = RTElementKind.RCT_TEMPLATE_PARAMETER;
        }
        else if("Enumeration".equals(type))
        {
            elKind = RTElementKind.RCT_ENUMERATION;
        }
        else if("EnumerationLiteral".equals(type))
        {
            elKind = RTElementKind.RCT_ENUMERATION_LITERAL;
        }
        else if("Interface".equals(type))
        {
            elKind = RTElementKind.RCT_INTERFACE;
        }
        else if("Operation".equals(type))
        {
            elKind = RTElementKind.RCT_OPERATION;
        }
        else if("Package".equals(type) ||
                  "Model".equals(type) ||
                  "Subsystem".equals(type))
        {
            elKind = RTElementKind.RCT_PACKAGE;
        }
        else if("Association".equals(type) ||
                  "Aggregation".equals(type) ||
                  "Composition".equals(type) ||
                  "Dependency".equals(type) ||
                  "Implementation".equals(type) ||
                  "Usage".equals(type) ||
                  "Permission".equals(type) ||
                  "Generalization".equals(type) ||
                  "AssociationEnd".equals(type))
        {
            elKind = RTElementKind.RCT_RELATION;
        }
        else if ("Parameter".equals(type))
        {
            elKind = RTElementKind.RCT_PARAMETER;
        }
        else if ("NavigableEnd".equals(type))
        {
            elKind = RTElementKind.RCT_NAVIGABLE_END_ATTRIBUTE;
        }

        setElementType(elKind);
    }
    

}
