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
        UMLXMLManip.setNodeTextValue(this, "UML:UseCaseDetail.body", body, false);

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
