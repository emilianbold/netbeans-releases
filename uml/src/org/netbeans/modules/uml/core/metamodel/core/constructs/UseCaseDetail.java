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


package org.netbeans.modules.uml.core.metamodel.core.constructs;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public class UseCaseDetail extends NamedElement implements IUseCaseDetail
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCaseDetail#addSubDetail(org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCaseDetail)
     */
    public void addSubDetail(final IUseCaseDetail subDetail)
    {
        new ElementConnector< IUseCaseDetail >().addChildAndConnect(
                                                this, 
                                                false, 
                                                "UML:UseCaseDetail.subDetail", 
                                                "UML:UseCaseDetail.subDetail/*",
                                                subDetail,
                                                new IBackPointer<IUseCaseDetail>() {
                                                    public void execute(IUseCaseDetail detail) {
                                                        subDetail.setParentDetail(detail);
                                                    }
                                                }
        );

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCaseDetail#createSubDetail()
     */
    public IUseCaseDetail createSubDetail()
    {
        TypedFactoryRetriever<IUseCaseDetail> retriever = 
                                        new TypedFactoryRetriever<IUseCaseDetail>();
        return retriever.createType("UseCaseDetail");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCaseDetail#getBody()
     */
    public String getBody()
    {
        return XMLManip.retrieveNodeTextValue(m_Node,"UML:UseCaseDetail.body");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCaseDetail#getParentDetail()
     */
    public IUseCaseDetail getParentDetail()
    {
        ElementCollector< IUseCaseDetail > col = new ElementCollector< IUseCaseDetail> ();
        return col.retrieveSingleElementWithAttrID(this, "parent", IUseCaseDetail.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCaseDetail#getSubDetails()
     */
    public ETList <IUseCaseDetail> getSubDetails()
    {
        ElementCollector< IUseCaseDetail > col = new ElementCollector< IUseCaseDetail> ();
        return col
            .retrieveElementCollection(
                    (IElement)this, 
                    "UML:UseCaseDetail.subDetail/UML:UseCaseDetail", IUseCaseDetail.class);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCaseDetail#removeSubDetail(org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCaseDetail)
     */
    public void removeSubDetail(final IUseCaseDetail subDetail)
    {
        new ElementConnector< IUseCaseDetail >().
        removeElement(this, 
                        subDetail,
                        "UML:UseCaseDetail.subDetail", 
                        new IBackPointer<IUseCaseDetail>() {
                            public void execute(IUseCaseDetail detail) {
                                subDetail.setParentDetail(detail);
                            }
                        } 
        );

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCaseDetail#setBody(java.lang.String)
     */
    public void setBody(String body)
    {
        if(body == null || body.trim().length() == 0)
            body = "";
        UMLXMLManip.setNodeTextValue(m_Node, "UML:UseCaseDetail.body", body, false);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCaseDetail#setParentDetail(org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCaseDetail)
     */
    public void setParentDetail(final IUseCaseDetail parentDetail)
    {
        new ElementConnector< IUseCaseDetail > ()
             .addChildAndConnect( 
                        this, 
                        true, 
                        "parent",
                        "parent", 
                        parentDetail, 
                        new IBackPointer<IUseCaseDetail>() {
                            public void execute(IUseCaseDetail detail) {
                                if (parentDetail != null)
                                    parentDetail.addSubDetail(detail);
                            }
                        }
        );
    }
    
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:UseCaseDetail", doc, node);
    }

}
