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
 * File       : PreRequest.java
 * Created on : Nov 6, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ITransitionElement;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IParameterDirectionKind;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceAccessor;

/**
 * @author Aztec
 */
public class PreRequest implements IPreRequest
{
    protected IElement              m_PreElement = null;
    protected IElement              m_OrigElement = null;
    protected IRequestProcessor     m_Proc = null;
    protected String                m_File = null;
    protected String                m_Language = null;
    protected int                   m_Detail = 0;
    protected IEventPayload         m_Payload = null;
    protected IElement              m_PreOwnerElement = null;
    protected IElement              m_ElementWithArtifact = null;
    
    public PreRequest()
    {
        m_Detail = RequestDetailKind.RDT_NONE;
    }
    
    public PreRequest(IElement preElement, 
                        IElement pClone,
                        IElement elementWithArtifact,
                        IRequestProcessor proc, 
                        int detail,
                        IEventPayload payload,
                        IElement clonedOwner)
    {
        m_PreElement = pClone;
        m_OrigElement = preElement;
        m_Proc = proc;
        m_Detail = detail;
        m_Payload = payload;
        m_PreOwnerElement = clonedOwner;
        m_ElementWithArtifact = elementWithArtifact;
        
        if(m_Proc != null)
            m_Language = m_Proc.getLanguage();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IPreRequest#createChangeRequest(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, int, int)
     */
    public IChangeRequest createChangeRequest(
        IElement pElement,
        int type,
        int detail)
    {
        if (pElement == null) return null;
        
        IChangeRequest newReq = null;
        
        String elementType = pElement.getElementType();
        
        if ("Parameter".equals(elementType))
        {
            newReq = new ParameterChangeRequest();
        }
        else
        {
            // TODO : What do we get for relmod?
        }

        if (newReq == null)
        {
            newReq = new ChangeRequest();
        }

        populateChangeRequest(newReq);

        newReq.setAfter(pElement);
        newReq.setState(type);

        // Now allow the PreRequest object make sure this ChangeRequest is absolutely
        // ready to go...

        preProcessRequest(newReq);
        
        return newReq;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IPreRequest#getDetail()
     */
    public int getDetail()
    {
        return m_Detail;       
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IPreRequest#getDupeElement()
     */
    public IElement getDupeElement()
    {
        // C++ code returns null
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IPreRequest#getElementWithArtifact()
     */
    public IElement getElementWithArtifact()
    {
        return (m_PreElement != null)?m_ElementWithArtifact : null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IPreRequest#getFileName()
     */
    public String getFileName()
    {
        return m_File;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IPreRequest#getLanguage()
     */
    public String getLanguage()
    {
        return m_Language;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IPreRequest#getModifiedNamespace()
     */
    public INamespace getModifiedNamespace()
    {
        // C++ code returns null
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IPreRequest#getOrigElement()
     */
    public IElement getOrigElement()
    {
        // C++ code returns null
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IPreRequest#getPreOwnerElement()
     */
    public IElement getPreOwnerElement()
    {
        return (m_PreElement != null)?m_PreOwnerElement : null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IPreRequest#getRequestProcessor(org.netbeans.modules.uml.core.roundtripframework.IRequestProcessor)
     */
    public IRequestProcessor getRequestProcessor(IRequestProcessor proc)
    {
        return m_Proc;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IPreRequest#inCreateState(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
    public boolean inCreateState(IElement preElement)
    {
        boolean inCreate = false;

        int detail = getDetail();
        if (detail != RequestDetailKind.RDT_ELEMENT_DELETED)
        {
            INamedElement element = (preElement instanceof INamedElement)?
                        (INamedElement)preElement : null;

            // see if we have a transition element.
            ITransitionElement tElement = (preElement instanceof ITransitionElement)?
                                   (ITransitionElement)preElement : null;
                                   
            if (tElement != null)
            {
                // such objects are always considered to be in a "create state"
                inCreate = true;
            }
            else if(element != null)
            {
                String type = element.getElementType();
      
                // We only care about Class and Interface at this
                // point. So find out if the preElement is currently
                // named with the default "no name" value. If it is,
                // the request is a create request.
      
                if ("Class".equals(type) ||
                    "Interface".equals(type) ||
                    "Attribute".equals(type) ||
                    "NavigableEnd".equals(type) ||
                    "Operation".equals(type) ||
                    "Enumeration".equals(type) ||
                    "EnumerationLiteral".equals(type))
                {
                    inCreate = isDefaultName(element);
                }

                if (!inCreate &&
                   ("Attribute".equals(type) ||
                    "Parameter".equals(type) ) )
                {
                    ITypedElement pAttr = (element instanceof ITypedElement)?
                                            (ITypedElement)element : null;

                    if (pAttr != null)
                    {
                        // For attributes if we are a simple 
                        // prerequest ( not a namechange prerequest ) we want to 
                        // check the type. If the type is still unset, we are in
                        // a create state.
         
                        IClassifier pType = pAttr.getType();

                        if (pType == null)
                        {
                            inCreate = true;
                        }
                        else
                        {
                            String name = pType.getName();

                            if(name == null || name.trim().length() <= 0)
                            {
                                inCreate = true;
                            }
                        }
                    }
                }
            }
        }
        return inCreate;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IPreRequest#origElement()
     */
    public IElement origElement()
    {
        return m_OrigElement;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IPreRequest#payload()
     */
    public IEventPayload payload()
    {
        return m_Payload;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IPreRequest#populateChangeRequest(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest)
     */
    public void populateChangeRequest(IChangeRequest req)
    {
        if (req == null) return;
        
        req.setBefore(m_PreElement);
        req.setLanguage(m_Language);
        req.setRequestDetailType(m_Detail);
        req.setPayload(m_Payload);
                
        IParameterChangeRequest parmReq 
            = (req instanceof IParameterChangeRequest)?
                        (IParameterChangeRequest)req : null;
                                
        IOperation pOp = (m_PreOwnerElement instanceof IOperation)
                                        ?(IOperation)m_PreOwnerElement : null;                        
        if (parmReq != null && pOp != null)
            parmReq.setBeforeOperation(pOp);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IPreRequest#postEvent(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
    public boolean postEvent(IElement pElement)
    {
        return (pElement != null) ? pElement.isSame(m_PreElement) : false;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IPreRequest#postEvent(org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy)
     */
    public boolean postEvent(IRelationProxy pRel)
    {
        // C++ code returns false.
        return false;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IPreRequest#preElement()
     */
    public IElement preElement()
    {
        return m_PreElement;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IPreRequest#preProcessRequest(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest)
     */
    public void preProcessRequest(IChangeRequest req)
    {
        if(req != null)
        {
            IElement preElement = req.getBefore();
       
            if (preElement != null)
            {
                if (inCreateState(preElement))
                {
                    // The pre-name IS the default "no name", so we need to change this
                    // request into a CREATE request rather than a MODIFY
        
                    req.setState(ChangeKind.CT_CREATE);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IPreRequest#relation()
     */
    public IRelationProxy relation()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IPreRequest#setDupeElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
    public void setDupeElement(IElement val)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IPreRequest#setModifiedNamespace(org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace)
     */
    public void setModifiedNamespace(INamespace newVal)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IPreRequest#setOrigElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
    public void setOrigElement(IElement val)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IPreRequest#setPreOwnerElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
    public void setPreOwnerElement(IElement element)
    {
        // TODO Auto-generated method stub

    }
    
    protected boolean isDefaultName(INamedElement pElement)
    {
        boolean retval = false;

        if (pElement != null)
        {
            // if the element is a parameter, and it is a return parameter, 
            // it is NEVER a create

            boolean check = true;
            if (pElement instanceof IParameter)
            {
                int dir = ((IParameter) pElement).getDirection();
                if ( dir == IParameterDirectionKind.PDK_RESULT)
                {
                    retval = false;
                    check = false;
                }
           }

           if(check)
           {
               retval = isDefaultName(pElement.getName());
           }
        }
        return retval;
    }

    protected boolean isDefaultName(String name)
    {
        boolean retval = false;

        String defaultName = PreferenceAccessor.instance().getDefaultElementName();

   
        if( name == null ||
            name.trim().length() == 0 ||
            name.equals(defaultName))
        {
           retval = true;
        }

        return retval;
    }
}
